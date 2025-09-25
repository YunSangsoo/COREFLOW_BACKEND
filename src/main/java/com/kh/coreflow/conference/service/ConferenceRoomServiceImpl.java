package com.kh.coreflow.conference.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.kh.coreflow.conference.dao.ConferenceRoomDao;
import com.kh.coreflow.conference.model.dto.ConferenceRoomDto;
import com.kh.coreflow.conference.model.dto.ConferenceRoomDto.AvailabilityRes;
import com.kh.coreflow.conference.model.dto.ConferenceRoomDto.CreateReq;
import com.kh.coreflow.conference.model.dto.ConferenceRoomDto.ReservationRes;
import com.kh.coreflow.conference.model.dto.ConferenceRoomDto.ReservationStatus;
import com.kh.coreflow.conference.model.dto.ConferenceRoomDto.ReservationTimeUpdateReq;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConferenceRoomServiceImpl implements ConferenceRoomService{
	
	private final ConferenceRoomDao dao;

    @Override
    @Transactional(readOnly = true)
    public List<ConferenceRoomDto.Res> getRooms(String buildingName, String floor, String status, Integer minCapacity) {
        Map<String, Object> params = new HashMap<>();
        params.put("buildingName", buildingName);
        params.put("floor", floor);
        params.put("status", status);
        params.put("minCapacity", minCapacity);
        return dao.selectRooms(params);
    }
    @Override
    @Transactional(readOnly = true)
    public ConferenceRoomDto.Res getRoom(Long roomId) {
        return dao.selectRoomById(roomId);
    }
	@Override
	@Transactional
	public Long createRoom(CreateReq req, Long createUserNo) {
		if (req == null || req.getRoomName()== null || req.getBuildingName().trim().isEmpty()) {
			throw new IllegalArgumentException("roomName은 필수입니다.");
		}
		Map<String, Object> p = new HashMap<>();
		p.put("req", req);
		p.put("createUserNo", createUserNo);
		dao.insertRoom(p);
		return (Long) p.get("roomId");
	}

	@Override
	@Transactional
	public Long createRoomReservation(ConferenceRoomDto.ReservationCreateReq req, Long userNo) {
	    if (req == null || req.getRoomId() == null || req.getStartAt() == null || req.getEndAt() == null) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "roomId, startAt, endAt are required");
	    }
	    if (!req.getStartAt().before(req.getEndAt())) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startAt must be before endAt");
	    }
	    // 사용 가능 방인지
	    if (dao.existsActiveRoom(req.getRoomId()) == 0) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "room not found or inactive");
	    }

	    if (dao.existsReservationOverlap(req.getRoomId(), req.getStartAt(), req.getEndAt()) > 0) {
	        throw new ResponseStatusException(HttpStatus.CONFLICT, "reservation time overlap");
	    }

	    // ✅ INSERT 파라미터(매퍼 키와 일치)
	    Map<String, Object> p = new HashMap<>();
	    p.put("roomId",   req.getRoomId());
	    p.put("userNo",   userNo);
	    p.put("startAt",  req.getStartAt());
	    p.put("endAt",    req.getEndAt());
	    p.put("status",   "CONFIRMED");
	    p.put("title",    req.getTitle());   // 없으면 null
	    p.put("eventId", req.getEventId());
	    int affected = dao.insertReservation(p);     // <selectKey>로 resvId 세팅
	    Long resvId = (Long) p.get("resvId");
	    if (affected <= 0 || resvId == null) {
	        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to create reservation");
	    }
	    return resvId;
	}

    @Override
    @Transactional(readOnly = true)
    public List<ReservationRes> getMyReservations(Long userNo, Long roomId, Timestamp fromTs, Timestamp toTs) {
        Map<String, Object> p = new HashMap<>();
        p.put("userNo", userNo);
        p.put("roomId", roomId);
        p.put("from", fromTs);
        p.put("to", toTs);
        return dao.selectMyReservations(p);
    }

    @Override
    @Transactional
    public ReservationRes confirmReservation(Long resvId, Long userNo) {
        ReservationRes cur = dao.findReservationById(resvId); // DAO: ReservationRes findReservationById(Long)
        if (cur == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (!ReservationStatus.HOLD.equals(cur.getStatus()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "only HOLD can be confirmed");

        if (dao.existsReservationOverlapExcludingId(cur.getRoomId(), cur.getStartAt(), cur.getEndAt(), resvId) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "reservation time overlap");
        }

        dao.updateReservationStatus(resvId, ReservationStatus.CONFIRMED, userNo);
        return dao.findReservationById(resvId);
    }

    @Override
    @Transactional
    public void cancelReservation(Long resvId, Long userNo) {
        ReservationRes cur = dao.findReservationById(resvId);
        if (cur == null) return; // idempotent
        dao.updateReservationStatus(resvId, ReservationStatus.CANCELLED, userNo);
    }

    @Override
    @Transactional
    public ReservationRes attachEvent(Long resvId, Long eventId, Long userNo) {
        ReservationRes cur = dao.findReservationById(resvId);
        if (cur == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        Map<String, Timestamp> et = dao.findEventTime(eventId);
        if (et == null || et.get("startAt") == null || et.get("endAt") == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "event not found");
        }
        Timestamp evStart = et.get("startAt");
        Timestamp evEnd   = et.get("endAt");
        if (!evStart.before(evEnd)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "event time invalid");
        }

        if (dao.existsReservationOverlapExcludingId(cur.getRoomId(), evStart, evEnd, resvId) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "reservation time overlap");
        }

        dao.updateReservationTime(resvId, evStart, evEnd, userNo);
        dao.attachReservationEvent(resvId, eventId, userNo);

        if (ReservationStatus.HOLD.equals(cur.getStatus())) {
            dao.updateReservationStatus(resvId, ReservationStatus.CONFIRMED, userNo);
        }

        return dao.findReservationById(resvId);
    }

    @Override
    @Transactional
    public ReservationRes updateReservationTime(Long resvId, ReservationTimeUpdateReq dto, Long userNo) {
        Objects.requireNonNull(dto, "time payload");
        if (dto.getStartAt() == null || dto.getEndAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startAt, endAt are required");
        }
        if (!dto.getStartAt().before(dto.getEndAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startAt must be before endAt");
        }

        ReservationRes cur = dao.findReservationById(resvId);
        if (cur == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        if (dao.existsReservationOverlapExcludingId(cur.getRoomId(), dto.getStartAt(), dto.getEndAt(), resvId) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "reservation time overlap");
        }

        dao.updateReservationTime(resvId, dto.getStartAt(), dto.getEndAt(), userNo);
        return dao.findReservationById(resvId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailabilityRes> searchAvailability(Timestamp startAt, Timestamp endAt,
                                                    Integer minCapacity, String buildingName, String floor) {
        if (startAt == null || endAt == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from/to required");
        }
        if (!startAt.before(endAt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must be before to");
        }
        Map<String, Object> p = new HashMap<>();
        p.put("startAt", startAt);
        p.put("endAt", endAt);
        p.put("minCapacity", minCapacity);
        p.put("buildingName", buildingName);
        p.put("floor", floor);
        return dao.selectAvailability(p);
    }
    
    private static final DateTimeFormatter SPACE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static LocalDateTime parseFlexible(String v) {
        if (v == null) throw new DateTimeParseException("null", "null", 0);
        String s = v.trim();
        // 1) 'yyyy-MM-dd HH:mm:ss'
        try { return LocalDateTime.parse(s, SPACE_FMT); } catch (Exception ignore) {}
        // 2) ISO_LOCAL_DATE_TIME
        try { return LocalDateTime.parse(s); } catch (Exception ignore) {}
        // 3) 'yyyy-MM-dd'만 온 경우 00:00:00 보정
        if (s.length() == 10) return LocalDateTime.parse(s + " 00:00:00", SPACE_FMT);
        // 4) OffsetDateTime, Instant 등
        try { return OffsetDateTime.parse(s).toLocalDateTime(); } catch (Exception ignore) {}
        try { return Instant.parse(s).atZone(ZoneId.systemDefault()).toLocalDateTime(); } catch (Exception ignore) {}
        throw new DateTimeParseException("Unparseable datetime: " + v, v, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public ConferenceRoomDto.RoomDetailRes getRoomDetail(Long actorUserNo, Long roomId, String fromStr, String toStr) {
        // 1) 회의실 요약(상세 조립용)
        ConferenceRoomDto.Room room = dao.selectRoomByIdForDetail(roomId);
        if (room == null) return null;

        // 2) 기간 파싱
        LocalDateTime fromLdt = parseFlexible(fromStr);
        LocalDateTime toLdt   = parseFlexible(toStr);
        Timestamp from = Timestamp.valueOf(fromLdt);
        Timestamp to   = Timestamp.valueOf(toLdt);

        // 3) 기간 내 예약(이벤트) 조회
        List<ConferenceRoomDto.RoomReservationRes> reservations =
                dao.selectReservationsByRoomAndPeriod(roomId, from, to);

        // 4) 조립
        return ConferenceRoomDto.RoomDetailRes.builder()
                .roomId(room.getRoomId())
                .roomName(room.getRoomName())
                .location(room.getLocation())
                .capacity(room.getCapacity())
                .equipments(room.getEquipments())     // mapper에서 NULL 반환(컬럼 미보유시)
                .description(room.getDescription())   // mapper에서 NULL 반환(컬럼 미보유시)
                .reservations(reservations)
                .build();
    }
    
    @Override
    @Transactional
    public void updateRoom(Long roomId, ConferenceRoomDto.CreateReq req, Long userNo) {
        int updated = dao.updateRoom(roomId, req, userNo);
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "회의실을 찾을 수 없습니다.");
        }
    }
    
    @Override
    @Transactional
    public void deleteRoom(Long roomId, Long userNo) {
        // 1) 이 회의실을 참조하는 ‘삭제되지 않은’ 이벤트가 있으면 삭제 금지
        int using = dao.countActiveEventsByRoom(roomId);
        if (using > 0) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "이 회의실과 연결된 일정이 있어 삭제할 수 없습니다."
            );
        }

        // 2) 실제 삭제
        int affected = dao.deleteRoom(roomId);
        if (affected == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "회의실을 찾을 수 없습니다.");
        }
    }
}


