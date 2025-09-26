package com.kh.coreflow.calendar.model.dao;

import com.kh.coreflow.calendar.model.dto.EventDto;
import com.kh.coreflow.calendar.model.dto.EventDto.EventTypeDto;

import java.util.List;

public interface EventDao {
    List<EventDto.Res> selectEventsByCalendarAndPeriod(Long calendarId, java.sql.Timestamp from, java.sql.Timestamp to);

    int countRoomConflicts(Long roomId, java.sql.Timestamp startAt, java.sql.Timestamp endAt);
    int countRoomConflictsExcludingSelf(Long eventId, Long roomId, java.sql.Timestamp startAt, java.sql.Timestamp endAt);

    Long insertEvent(Long userNo, EventDto.Req req);
    int updateEvent(Long userNo, Long eventId, EventDto.Req req);
    int logicalDeleteEvent(Long userNo, Long eventId);
    
    //라벨
    List<EventDto.LabelRes> selectAllLabels();
    Long insertLabel(EventDto.LabelReq req);
    int updateLabel(Long labelId, EventDto.LabelReq req);
    int deleteLabel(Long labelId);

	int batchInsertParticipants(Long eventId, List<Long> attendeeUserNos, String kind);

	int deleteParticipantsByEventId(Long eventId);

	int insertParticipantIfAbsent(Long eventId, Long u, String kind);
	default int batchInsertParticipantsIfAbsent(Long eventId, List<Long> userNos, String kind) {
	    int sum = 0;
	    if (userNos == null) return 0;
	    for (Long u : userNos) {
	        sum += insertParticipantIfAbsent(eventId, u, kind);
	    }
	    return sum;
	}

	List<EventDto.EventTypeDto> selectAllEventTypes();
	int insertEventType(EventDto.EventTypeDto dto);
	int updateEventTypeName(EventDto.EventTypeDto dto);
	int deleteEventType(Long typeId);
	
	EventDto.DetailRes selectEventDetailById(Long eventId);
	int countHrOrAdmin(Long userNo);
	int updateEventRoomLink(Long eventId, Long roomId, Long userNo);

	List<EventDto.Member> selectAttendeesByEventId(Long eventId);
	List<EventDto.Member> selectSharersByEventId(Long eventId);
	
	boolean hasAdminAuthority(Long userNo);
	Long selectEventCreator(Long eventId);

}