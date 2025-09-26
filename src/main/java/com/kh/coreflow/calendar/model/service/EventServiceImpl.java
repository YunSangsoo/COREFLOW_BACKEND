package com.kh.coreflow.calendar.model.service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.kh.coreflow.calendar.model.dao.CalendarDao;
import com.kh.coreflow.calendar.model.dao.EventDao;
import com.kh.coreflow.calendar.model.dto.EventDto;
import com.kh.coreflow.common.exception.ConflictException;
import com.kh.coreflow.conference.dao.ConferenceRoomDao;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

	private final EventDao eventDao;
	private final CalendarDao calendarDao;
	private final ConferenceRoomDao conferenceRoomDao;

	// "yyyy-MM-dd HH:mm:ss" (공백형) 우선 지원
	private static final DateTimeFormatter FMT_SPACE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	/** 프런트에서 오는 문자열을 견고하게 파싱 (공백/ISO/UTC 모두 수용) */
	private static LocalDateTime parseClientDateTime(String s) {
		if (s == null)
			throw new DateTimeParseException("null datetime", "null", 0);
		s = s.trim();

		// 1) "yyyy-MM-dd HH:mm:ss"
		try {
			return LocalDateTime.parse(s, FMT_SPACE);
		} catch (DateTimeParseException ignore) {
		}

		// 2) ISO_LOCAL_DATE_TIME (yyyy-MM-dd'T'HH:mm:ss)
		try {
			return LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		} catch (DateTimeParseException ignore) {
		}

		// 3) 날짜만 온 경우 → 자정으로 보정
		if (s.length() == 10) { // yyyy-MM-dd
			return LocalDateTime.parse(s + " 00:00:00", FMT_SPACE);
		}

		// 4) Offset/UTC 형태 수용 (예: 2025-08-31T00:00:00Z, +09:00 등)
		try {
			return OffsetDateTime.parse(s).toLocalDateTime();
		} catch (Exception ignore) {
		}
		try {
			return Instant.parse(s).atZone(ZoneId.systemDefault()).toLocalDateTime();
		} catch (Exception ignore) {
		}

		throw new DateTimeParseException("Unparseable datetime: " + s, s, 0);
	}

	@Override
	public List<EventDto.Res> getEvents(Long userNo, Long calendarId, String fromStr, String toStr) {
	    LocalDateTime fromLdt = parseClientDateTime(fromStr);
	    LocalDateTime toLdt   = parseClientDateTime(toStr);
	    Timestamp from = Timestamp.valueOf(fromLdt);
	    Timestamp to   = Timestamp.valueOf(toLdt);

	    List<EventDto.Res> list = eventDao.selectEventsByCalendarAndPeriod(calendarId, from, to);

	    String role = effectiveRole(userNo, calendarId); // NONE / BUSY_ONLY / READER / ...
	    if ("NONE".equalsIgnoreCase(role)) {
	        return List.of(); // 전혀 보이지 않음
	    }
	    if ("BUSY_ONLY".equalsIgnoreCase(role)) {
	        return list.stream().map(this::maskAsBusyOnly).toList();
	    }
	    return list; // READER 이상은 그대로
	}

	private EventDto.Res maskAsBusyOnly(EventDto.Res src) {
	    EventDto.Res r = new EventDto.Res();
	    r.setEventId(src.getEventId());
	    r.setCalId(src.getCalId());
	    r.setStartAt(src.getStartAt());
	    r.setEndAt(src.getEndAt());
	    r.setAllDayYn(src.getAllDayYn());

	    // 제목/세부정보 모두 가림
	    r.setTitle("");              // ← 완전 숨김 원하면 빈 문자열 (또는 "바쁨")
	    r.setStatus("BUSY");         // 프론트가 표시만 ‘바쁨’ 블록으로 처리 가능
	    r.setLocationText(null);
	    r.setNote(null);
	    r.setRoomId(null);
	    r.setLabelId(null);
	    r.setTypeId(null);
	    r.setTypeName(null);
	    r.setTypeCode(null);
	    r.setRrule(null);
	    r.setExdates(null);
	    r.setCreateByUserNo(null);
	    r.setUpdateUserNo(null);
	    return r;
	}

	// ─────────────────────────────────────────────────────────
	// 권한 헬퍼들 (ADMIN/캘린더 역할 판정 + 생성/수정/삭제 허용 규칙)
	// ─────────────────────────────────────────────────────────
	private boolean isAdmin(Long userNo) {
		// AUTHORITIES.AUTHORITY (대소문자 혼재 가능) → mapper에서 UPPER 비교
		return eventDao.hasAdminAuthority(userNo);
	}

	private String effectiveRole(Long userNo, Long calId) {
		// 캘린더 유효 권한: 개인 > 직급 > 부서 > 기본권한
		String r = calendarDao.selectEffectiveCalendarRoleForUser(userNo, calId);
		return r == null ? "NONE" : r.toUpperCase();
	}

	private boolean canCreateEvent(boolean admin, String role) {
		// 생성: ADMIN || OWNER || EDITOR || CONTRIBUTOR
		return admin || "OWNER".equals(role) || "EDITOR".equals(role) || "CONTRIBUTOR".equals(role);
	}

	private boolean canEditAny(boolean admin, String role) {
		// OWNER/EDITOR/ADMIN은 해당 캘린더 모든 일정 수정 가능
		return admin || "OWNER".equals(role) || "EDITOR".equals(role);
	}

	private boolean canEditOwn(boolean admin, String role, boolean isAuthor) {
		// CONTRIBUTOR는 본인 작성건만
		if (canEditAny(admin, role))
			return true;
		return "CONTRIBUTOR".equals(role) && isAuthor;
	}

	// ─────────────────────────────────────────────────────────
	// 생성
	// ─────────────────────────────────────────────────────────
	@Transactional
	@Override
	public Long createEvent(Long userNo, EventDto.Req req) {
		if (req == null || req.getCalId() == null) {
			throw new IllegalArgumentException("캘린더 정보가 없습니다.");
		}

		// 1) 권한 체크: ADMIN || OWNER || EDITOR || CONTRIBUTOR
		final boolean admin = isAdmin(userNo);
		final String role = effectiveRole(userNo, req.getCalId());
		if (!canCreateEvent(admin, role)) {
			throw new IllegalStateException("권한이 없습니다. (생성은 소유자/관리자/편집가능만)");
		}

		// 2) 회의실 겹침 검사(있을 때만)
		if (req.getRoomId() != null) {
			int conflicts = eventDao.countRoomConflicts(req.getRoomId(), req.getStartAt(), req.getEndAt());
			if (conflicts > 0)
				throw new ConflictException("회의실 시간이 겹칩니다.");
		}

		// 3) 생성 + 참석자/공유자 저장
		Long eventId = eventDao.insertEvent(userNo, req); // CREATE_BY_USER_NO = userNo 저장
		saveParticipantsSafe(eventId, req);
		return eventId;
	}

	// ─────────────────────────────────────────────────────────
	// 수정
	// ─────────────────────────────────────────────────────────
	@Transactional
	@Override
	public void updateEvent(Long userNo, Long eventId, EventDto.Req req) {
		if (req == null || req.getCalId() == null) {
			throw new IllegalArgumentException("캘린더 정보가 없습니다.");
		}

		// 0) 현재 이벤트 상태 조회(작성자/원본 calId 확보)
		EventDto.DetailRes cur = eventDao.selectEventDetailById(eventId);
		if (cur == null)
			throw new IllegalStateException("수정 대상이 없거나 삭제된 일정입니다.");

		final boolean admin = isAdmin(userNo);
		final boolean isAuthor = cur.getCreateByUserNo() != null && cur.getCreateByUserNo().equals(userNo);

		final Long oldCalId = cur.getCalId();
		final Long newCalId = req.getCalId();

		// 1) 대상(새) 캘린더 권한 확인: ADMIN/OWNER/EDITOR 또는 (CONTRIBUTOR && 본인작성)
		final String roleNew = effectiveRole(userNo, newCalId);
		final boolean allowedOnNew = canEditAny(admin, roleNew) || ("CONTRIBUTOR".equals(roleNew) && isAuthor);
		if (!allowedOnNew) {
			throw new org.springframework.security.access.AccessDeniedException("권한이 없습니다. (컨트리뷰터는 본인 생성 일정만)");
		}

		// 2) 캘린더 이동(update로 calId가 바뀌는 경우) → 원본 캘린더에 대한 권한도 확인
		if (!java.util.Objects.equals(oldCalId, newCalId)) {
			final String roleOld = effectiveRole(userNo, oldCalId);
			final boolean allowedOnOld = canEditAny(admin, roleOld) || ("CONTRIBUTOR".equals(roleOld) && isAuthor);
			if (!allowedOnOld) {
				throw new org.springframework.security.access.AccessDeniedException("원본 캘린더에 대한 수정 권한이 없습니다.");
			}
		}

		// 3) 회의실 겹침 검사(자기 자신 제외)
		if (req.getRoomId() != null) {
			int conflicts = eventDao.countRoomConflictsExcludingSelf(eventId, req.getRoomId(), req.getStartAt(),
					req.getEndAt());
			if (conflicts > 0)
				throw new com.kh.coreflow.common.exception.ConflictException("회의실 시간이 겹칩니다.");
		}

		// 4) 업데이트 + 참석자/공유자 재저장
		int rows = eventDao.updateEvent(userNo, eventId, req);
		if (rows == 0)
			throw new IllegalStateException("수정 대상이 없거나 삭제된 일정입니다.");

		eventDao.deleteParticipantsByEventId(eventId);
		saveParticipantsSafe(eventId, req);
	}

	// ─────────────────────────────────────────────────────────
	// 삭제
	// ─────────────────────────────────────────────────────────
	@Transactional
	@Override
	public void deleteEvent(Long userNo, Long eventId) {
		// calId/creator를 알아야 권한 평가 가능 → 상세 조회
		EventDto.DetailRes cur = eventDao.selectEventDetailById(eventId);
		if (cur == null)
			throw new IllegalStateException("삭제 대상이 없거나 이미 삭제되었습니다.");

		final boolean admin = isAdmin(userNo);
		final String role = effectiveRole(userNo, cur.getCalId());
		boolean isAuthor = (cur.getCreateByUserNo() != null && cur.getCreateByUserNo().equals(userNo));

		boolean allowed = canEditAny(admin, role) || ("CONTRIBUTOR".equals(role) && isAuthor);
		if (!allowed) {
			throw new IllegalStateException("권한이 없습니다. (컨트리뷰터는 본인 생성 일정만)");
		}

		int rows = eventDao.logicalDeleteEvent(userNo, eventId);
		if (rows == 0)
			throw new IllegalStateException("삭제 대상이 없거나 이미 삭제되었습니다.");

		// 회의실 예약 논리삭제(STATUS=DELETED) & 링크 해제
		conferenceRoomDao.deleteReservationsByEventId(eventId, userNo);
		eventDao.updateEventRoomLink(eventId, null, userNo);

		// 참석자/공유자 정리
		eventDao.deleteParticipantsByEventId(eventId);
	}

	/** 서비스 레벨 방어: null 제거, distinct, 교집합 제거 */
	private void saveParticipantsSafe(Long eventId, EventDto.Req req) {
		List<Long> attendees = toDistinct(req.getAttendeeUserNos());
		List<Long> sharers = toDistinct(req.getShareUserNos());

		// DB UNIQUE (EVENT_ID, USER_NO) 대비 교집합 제거
		sharers.removeAll(attendees);

		if (!attendees.isEmpty()) {
			eventDao.batchInsertParticipantsIfAbsent(eventId, attendees, "ATTENDEE");
		}
		if (!sharers.isEmpty()) {
			eventDao.batchInsertParticipantsIfAbsent(eventId, sharers, "SHARER");
		}
	}

	private static List<Long> toDistinct(List<Long> src) {
		if (src == null)
			return List.of();
		return src.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
	}

	// ─────────────────────────────────────────────────────────
	// 라벨
	// ─────────────────────────────────────────────────────────
	@Override
	public List<EventDto.LabelRes> labelList() {
		return eventDao.selectAllLabels();
	}

	@Override
	@Transactional
	public Long labelCreate(EventDto.LabelReq req) {
		return eventDao.insertLabel(req);
	}

	@Override
	@Transactional
	public void labelUpdate(Long labelId, EventDto.LabelReq req) {
		if (eventDao.updateLabel(labelId, req) == 0) {
			throw new IllegalStateException("수정 대상이 없습니다.");
		}
	}

	@Override
	@Transactional
	public void labelDelete(Long labelId) {
		if (eventDao.deleteLabel(labelId) == 0) {
			throw new IllegalStateException("삭제 대상이 없습니다.");
		}
	}

	@Override
	public List<EventDto.EventTypeDto> getEventTypes() {
		return eventDao.selectAllEventTypes();
	}

	private String toCode(String name) {
		String base = name == null ? "" : name.trim();
		base = java.text.Normalizer.normalize(base, java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "")
				.replaceAll("[^A-Za-z0-9]+", "_").replaceAll("^_|_$", "").toUpperCase();
		return base.isEmpty() ? "TYPE" : base;
	}

	@Override
	public EventDto.EventTypeDto createEventType(String typeName) {
		EventDto.EventTypeDto dto = EventDto.EventTypeDto.builder().typeName(typeName).typeCode(toCode(typeName))
				.build();
		eventDao.insertEventType(dto); // dto.typeId 채워짐
		return dto;
	}

	@Override
	public void updateEventType(Long typeId, String typeName) {
		eventDao.updateEventTypeName(EventDto.EventTypeDto.builder().typeId(typeId).typeName(typeName).build());
	}

	@Override
	public void deleteEventType(Long typeId) {
		eventDao.deleteEventType(typeId);
	}

	// ─────────────────────────────────────────────────────────
	// 상세 조회 + canEdit/canDelete 계산 (프론트 버튼 제어용)
	// ─────────────────────────────────────────────────────────
	@Override
	@Transactional(readOnly = true)
	public EventDto.DetailRes getEventDetail(Long me, Long eventId) {
	    EventDto.DetailRes res = eventDao.selectEventDetailById(eventId);
	    if (res == null) return null;

	    // 권한 체크: BUSY_ONLY/NONE은 상세 불가
	    String role = effectiveRole(me, res.getCalId());
	    if ("NONE".equalsIgnoreCase(role) || "BUSY_ONLY".equalsIgnoreCase(role)) {
	        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "읽기 권한이 없습니다.");
	    }

	    // 이하 기존 로직 유지
	    res.setAttendees(eventDao.selectAttendeesByEventId(eventId));
	    res.setSharers(eventDao.selectSharersByEventId(eventId));

	    boolean isAuthor = res.getCreateByUserNo() != null && res.getCreateByUserNo().equals(me);
	    boolean admin = isAdmin(me);
	    boolean canAny = canEditAny(admin, role);
	    boolean canOwn = "CONTRIBUTOR".equals(role) && isAuthor;
	    res.setCanEdit(canAny || canOwn);
	    res.setCanDelete(canAny || canOwn);
	    return res;
	}

	// ─────────────────────────────────────────────────────────
	// 선택: 별도 WithAuth 메서드들도 동일 규칙으로 보강
	// ─────────────────────────────────────────────────────────
	@Override
	@Transactional
	public void updateEventWithAuth(Long me, Long eventId, Object updateReq) {
		EventDto.DetailRes cur = eventDao.selectEventDetailById(eventId);
		if (cur == null)
			throw new ConflictException("이벤트가 존재하지 않습니다.");

		boolean admin = isAdmin(me);
		String role = effectiveRole(me, cur.getCalId());
		boolean isAuthor = (cur.getCreateByUserNo() != null && cur.getCreateByUserNo().equals(me));

		if (!(canEditAny(admin, role) || ("CONTRIBUTOR".equals(role) && isAuthor))) {
			throw new IllegalStateException("수정 권한이 없습니다. (컨트리뷰터는 본인 생성 일정만)");
		}
		// 실제 업데이트 로직은 기존 흐름 사용 (예: eventDao.updateEvent(...))
	}

	@Override
	@Transactional
	public void deleteEventWithAuth(Long me, Long eventId) {
		EventDto.DetailRes cur = eventDao.selectEventDetailById(eventId);
		if (cur == null)
			return;

		boolean admin = isAdmin(me);
		String role = effectiveRole(me, cur.getCalId());
		boolean isAuthor = (cur.getCreateByUserNo() != null && cur.getCreateByUserNo().equals(me));

		if (!(canEditAny(admin, role) || ("CONTRIBUTOR".equals(role) && isAuthor))) {
			throw new IllegalStateException("삭제 권한이 없습니다. (컨트리뷰터는 본인 생성 일정만)");
		}
		// 기존 논리삭제 흐름 사용 (예: eventDao.logicalDeleteEvent(eventId, me);)
	}

	@Override
	@Transactional
	public void updateEventRoomLink(Long userNo, Long eventId, Long roomId) {
		eventDao.updateEventRoomLink(eventId, roomId, userNo);
	}
}
