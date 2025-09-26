package com.kh.coreflow.conference.service;

import java.sql.Timestamp;
import java.util.List;

import com.kh.coreflow.conference.model.dto.ConferenceRoomDto;
import com.kh.coreflow.conference.model.dto.ConferenceRoomDto.AvailabilityRes;
import com.kh.coreflow.conference.model.dto.ConferenceRoomDto.CreateReq;
import com.kh.coreflow.conference.model.dto.ConferenceRoomDto.ReservationCreateReq;
import com.kh.coreflow.conference.model.dto.ConferenceRoomDto.ReservationRes;
import com.kh.coreflow.conference.model.dto.ConferenceRoomDto.ReservationTimeUpdateReq;

public interface ConferenceRoomService {

	List<ConferenceRoomDto.Res> getRooms(String buildingName, String floor, String status, Integer minCapacity);
    ConferenceRoomDto.Res getRoom(Long roomId);
	Long createRoom(CreateReq req, Long createUserNo);
	// 회의실 예약
	Long createRoomReservation(ReservationCreateReq req, Long userNo);
	List<ReservationRes> getMyReservations(Long userNo, Long roomId, Timestamp fromTs, Timestamp toTs);
    ReservationRes confirmReservation(Long resvId, Long userNo);
    void cancelReservation(Long resvId, Long userNo);
    ReservationRes attachEvent(Long resvId, Long eventId, Long userNo);
    ReservationRes updateReservationTime(Long resvId, ReservationTimeUpdateReq req, Long userNo);
    List<AvailabilityRes> searchAvailability(Timestamp startAt, Timestamp endAt,
                                             Integer minCapacity, String buildingName, String floor);
  
    ConferenceRoomDto.RoomDetailRes getRoomDetail(Long actorUserNo, Long roomId, String fromStr, String toStr);
    void updateRoom(Long roomId, ConferenceRoomDto.CreateReq req, Long userNo);
    void deleteRoom(Long roomId, Long userNo);


}
