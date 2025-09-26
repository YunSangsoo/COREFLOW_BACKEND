package com.kh.coreflow.conference.dao;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.coreflow.conference.model.dto.ConferenceRoomDto;
import com.kh.coreflow.conference.model.dto.ConferenceRoomDto.AvailabilityRes;
import com.kh.coreflow.conference.model.dto.ConferenceRoomDto.ReservationRes;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ConferenceRoomDaoImpl implements ConferenceRoomDao{

	private final SqlSessionTemplate sqlSession;

	@Override
    public List<ConferenceRoomDto.Res> selectRooms(Map<String, Object> params) {
        return sqlSession.selectList("conferenceRoom.selectRooms", params);
    }
    @Override
    public ConferenceRoomDto.Res selectRoomById(Long roomId) {
        return sqlSession.selectOne("conferenceRoom.selectRoomById", roomId);
    }
    @Override
    public int insertRoom(Map<String, Object> param) {
        return sqlSession.insert("conferenceRoom.insertRoom", param);
    }
    @Override
    public int existsActiveRoom(Long roomId) {
        Integer v = sqlSession.selectOne("conferenceRoom.existsActiveRoom", roomId);
        return v == null ? 0 : v;
    }
    @Override
    public int existsReservationOverlap(Long roomId, Timestamp startAt, Timestamp endAt) {
        Map<String, Object> p = new HashMap<>();
        p.put("roomId", roomId);
        p.put("startAt", startAt);
        p.put("endAt", endAt);
        Integer v = sqlSession.selectOne("conferenceRoom.existsReservationOverlap", p);
        return v == null ? 0 : v;
    }
    @Override
    public int existsReservationOverlapExcludingId(Long roomId, Timestamp startAt, Timestamp endAt, Long excludeResvId) {
        Map<String, Object> p = new HashMap<>();
        p.put("roomId", roomId);
        p.put("startAt", startAt);
        p.put("endAt", endAt);
        p.put("excludeResvId", excludeResvId);
        Integer v = sqlSession.selectOne("conferenceRoom.existsReservationOverlapExcludingId", p);
        return v == null ? 0 : v;
    }
    @Override
    public ReservationRes findReservationById(Long resvId) {
        return sqlSession.selectOne("conferenceRoom.findReservationById", resvId);
    }
    @Override
    public int insertReservation(Map<String, Object> param) {
        // param IN: roomId,userNo,startAt,endAt,status,title,expiresAt
        // mapper에서 selectKey/useGeneratedKeys로 param.put("resvId", ...) 세팅
        return sqlSession.insert("conferenceRoom.insertReservation", param);
    }
    @Override
    public int updateReservationStatus(Long resvId, String status, Long userNo) {
        Map<String, Object> p = new HashMap<>();
        p.put("resvId", resvId);
        p.put("status", status);
        p.put("userNo", userNo);
        return sqlSession.update("conferenceRoom.updateReservationStatus", p);
    }
    @Override
    public int updateReservationTime(Long resvId, Timestamp startAt, Timestamp endAt, Long userNo) {
        Map<String, Object> p = new HashMap<>();
        p.put("resvId", resvId);
        p.put("startAt", startAt);
        p.put("endAt", endAt);
        p.put("userNo", userNo);
        return sqlSession.update("conferenceRoom.updateReservationTime", p);
    }
    @Override
    public int attachReservationEvent(Long resvId, Long eventId, Long userNo) {
        Map<String, Object> p = new HashMap<>();
        p.put("resvId", resvId);
        p.put("eventId", eventId);
        p.put("userNo", userNo);
        return sqlSession.update("conferenceRoom.attachReservationEvent", p);
    }
    @Override
    public List<ReservationRes> selectMyReservations(Map<String, Object> param) {
        return sqlSession.selectList("conferenceRoom.selectMyReservations", param);
    }
    @Override
    public Map<String, Timestamp> findEventTime(Long eventId) {
        // mapper는 START_AT, END_AT를 각각 startAt, endAt 키로 리턴
        return sqlSession.selectOne("conferenceRoom.findEventTime", eventId);
    }
    @Override
    public List<AvailabilityRes> selectAvailability(Map<String, Object> param) {
        return sqlSession.selectList("conferenceRoom.selectAvailability", param);
    }
    
    @Override
    public int deleteReservationsByEventId(Long eventId, Long userNo) {
        Map<String, Object> p = new HashMap<>();
        p.put("eventId", eventId);
        p.put("userNo", userNo);
        return sqlSession.update("conferenceRoom.deleteReservationsByEventId", p);
    }
    
    @Override
    public ConferenceRoomDto.Room selectRoomByIdForDetail(Long roomId) {
        return sqlSession.selectOne("conferenceRoom.selectRoomByIdForDetail", roomId);
    }
    
    @Override
    public List<ConferenceRoomDto.RoomReservationRes> selectReservationsByRoomAndPeriod(Long roomId, Timestamp from, Timestamp to) {
        Map<String,Object> p = new HashMap<>();
        p.put("roomId", roomId);
        p.put("from", from);
        p.put("to", to);
        return sqlSession.selectList("conferenceRoom.selectReservationsByRoomAndPeriod", p);
    }
    
    @Override
    public int updateRoom(Long roomId, ConferenceRoomDto.CreateReq p, Long userNo) {
        Map<String,Object> params = new HashMap<>();
        params.put("roomId", roomId);
        params.put("p", p);
        params.put("userNo", userNo);
        return sqlSession.update("conferenceRoom.updateRoom", params);
    }
    
    @Override
    public int deleteRoom(Long roomId) {
        return sqlSession.delete("conferenceRoom.deleteRoom", roomId);
    }

    @Override
    public int countActiveEventsByRoom(Long roomId) {
        return sqlSession.selectOne("conferenceRoom.countActiveEventsByRoom", roomId);
    }
}
