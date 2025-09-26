package com.kh.coreflow.conference.dao;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.kh.coreflow.conference.model.dto.ConferenceRoomDto;
import com.kh.coreflow.conference.model.dto.ConferenceRoomDto.AvailabilityRes;
import com.kh.coreflow.conference.model.dto.ConferenceRoomDto.ReservationRes;

public interface ConferenceRoomDao {

	List<ConferenceRoomDto.Res> selectRooms(Map<String, Object> params);

	ConferenceRoomDto.Res selectRoomById(Long roomId);

	int insertRoom(Map<String, Object> param);

	int deleteReservationsByEventId(Long eventId, Long userNo);

	// 회의실/예약 존재·검사
	int existsActiveRoom(Long roomId);

	int existsReservationOverlap(Long roomId, Timestamp startAt, Timestamp endAt);

	int existsReservationOverlapExcludingId(Long roomId, Timestamp startAt, Timestamp endAt, Long excludeResvId);

	// 예약 CRUD
	int insertReservation(Map<String, Object> p); // IN: roomId,userNo,startAt,endAt,status,title,expiresAt OUT: resvId

	int updateReservationStatus(Long resvId, String status, Long userNo);

	int updateReservationTime(Long resvId, Timestamp startAt, Timestamp endAt, Long userNo);

	int attachReservationEvent(Long resvId, Long eventId, Long userNo);

	ReservationRes findReservationById(Long resvId);

	List<ReservationRes> selectMyReservations(Map<String, Object> p);

	// 이벤트 시간 조회(이벤트 테이블 스키마에 맞춰 구현)
	Map<String, Timestamp> findEventTime(Long eventId);

	// 가용성 조회
	List<AvailabilityRes> selectAvailability(Map<String, Object> p);

    ConferenceRoomDto.Room selectRoomByIdForDetail(Long roomId);

    List<ConferenceRoomDto.RoomReservationRes> selectReservationsByRoomAndPeriod(Long roomId, Timestamp from, Timestamp to);
    
    int updateRoom(@Param("roomId") Long roomId,
            @Param("p") ConferenceRoomDto.CreateReq p,
            @Param("userNo") Long userNo);
    
    int deleteRoom(@Param("roomId") Long roomId);
    int countActiveEventsByRoom(@Param("roomId") Long roomId);

}
