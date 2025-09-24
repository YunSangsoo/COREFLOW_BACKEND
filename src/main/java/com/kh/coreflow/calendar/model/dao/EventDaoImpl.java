package com.kh.coreflow.calendar.model.dao;

import com.kh.coreflow.calendar.model.dto.EventDto;
import com.kh.coreflow.calendar.model.dto.EventDto.EventTypeDto;

import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class EventDaoImpl implements EventDao {

    private final SqlSessionTemplate sql;

    @Override
    public List<EventDto.Res> selectEventsByCalendarAndPeriod(Long calendarId, java.sql.Timestamp from, java.sql.Timestamp to) {
        Map<String,Object> p = new HashMap<>();
        p.put("calendarId", calendarId);
        p.put("from", from);
        p.put("to", to);
        return sql.selectList("event.selectEventsByCalendarAndPeriod", p);
    }

    @Override
    public int countRoomConflicts(Long roomId, java.sql.Timestamp startAt, java.sql.Timestamp endAt) {
        Map<String,Object> p = new HashMap<>();
        p.put("roomId", roomId);
        p.put("startAt", startAt);
        p.put("endAt", endAt);
        return sql.selectOne("event.countRoomConflicts", p);
    }

    @Override
    public int countRoomConflictsExcludingSelf(Long eventId, Long roomId, java.sql.Timestamp startAt, java.sql.Timestamp endAt) {
        Map<String,Object> p = new HashMap<>();
        p.put("eventId", eventId);
        p.put("roomId", roomId);
        p.put("startAt", startAt);
        p.put("endAt", endAt);
        return sql.selectOne("event.countRoomConflictsExcludingSelf", p);
    }

    @Override
    public Long insertEvent(Long userNo, EventDto.Req req) {
        Map<String,Object> p = new HashMap<>();
        p.put("userNo", userNo);
        p.put("req", req);
        sql.insert("event.insertEvent", p); // selectKey로 req.eventId 채움
        return req.getEventId();
    }

    @Override
    public int updateEvent(Long userNo, Long eventId, EventDto.Req req) {
        Map<String,Object> p = new HashMap<>();
        p.put("userNo", userNo);
        p.put("eventId", eventId);
        p.put("req", req);
        return sql.update("event.updateEvent", p);
    }

    @Override
    public int logicalDeleteEvent(Long userNo, Long eventId) {
        Map<String,Object> p = new HashMap<>();
        p.put("userNo", userNo);
        p.put("eventId", eventId);
        return sql.update("event.logicalDeleteEvent", p);
    }
    
    @Override
    public int deleteParticipantsByEventId(Long eventId) {
      return sql.delete("event.deleteParticipantsByEventId", eventId);
    }

    @Override
    public int batchInsertParticipants(Long eventId, List<Long> userNos, String kind) {
      Map<String,Object> p = new HashMap<>();
      p.put("eventId", eventId);
      p.put("userNos", userNos);
      p.put("kind", kind); // "ATTENDEE" or "SHARER"
      // INSERT ALL은 영향 행 수가 딱 떨어지지 않을 수 있어 0/1 반환에 연연하지 않아도 됩니다.
      return sql.insert("event.batchInsertParticipants", p);
    }

    @Override
    public int insertParticipantIfAbsent(Long eventId, Long u, String kind) {
    	Map<String,Object> p = new HashMap<>();
    	p.put("eventId", eventId);
    	p.put("userNo", u);
    	p.put("kind", kind);
    	return sql.insert("event.insertParticipantIfAbsent", p);
    }
    
    // 라벨
    @Override
    public List<EventDto.LabelRes> selectAllLabels() {
        return sql.selectList("event.selectAllLabels");
    }

    @Override
    public Long insertLabel(EventDto.LabelReq req) {
        Map<String,Object> p = new HashMap<>();
        p.put("req", req);
        sql.insert("event.insertLabel", p);
        return (Long) p.get("labelId");
    }

    @Override
    public int updateLabel(Long labelId, EventDto.LabelReq req) {
        Map<String,Object> p = new HashMap<>();
        p.put("labelId", labelId);
        p.put("req", req);
        return sql.update("event.updateLabel", p);
    }

    @Override
    public int deleteLabel(Long labelId) {
        return sql.delete("event.deleteLabel", labelId);
    }

	@Override
	public List<EventDto.EventTypeDto> selectAllEventTypes() {
		return sql.selectList("event.selectAllEventTypes");
	}
	@Override
	public int insertEventType(EventTypeDto dto) {
		return sql.insert("event.insertEventType", dto);
	}
	@Override
	public int updateEventTypeName(EventTypeDto dto) {
		return sql.update("event.updateEventTypeName", dto);
	}
	@Override
	public int deleteEventType(Long typeId) {
		return sql.delete("event.deleteEventType", typeId);
	}
    
	@Override
    public EventDto.DetailRes selectEventDetailById(Long eventId) {
        return sql.selectOne("event.selectEventDetailById", eventId);
    }

    @Override
    public int countHrOrAdmin(Long userNo) {
        return sql.selectOne("event.countHrOrAdmin", userNo);
    }
    
    @Override
    public int updateEventRoomLink(Long eventId, Long roomId, Long userNo) {
        Map<String,Object> p = new HashMap<>();
        p.put("eventId", eventId);
        p.put("roomId", roomId);
        p.put("userNo", userNo);
        return sql.update("event.updateEventRoomLink", p);
    }
    
    @Override
    public List<EventDto.Member> selectAttendeesByEventId(Long eventId) {
      return sql.selectList("event.selectAttendeesByEventId", eventId);
    }

    @Override
    public List<EventDto.Member> selectSharersByEventId(Long eventId) {
      return sql.selectList("event.selectSharersByEventId", eventId);
    }
    
    @Override
    public boolean hasAdminAuthority(Long userNo) {
        Integer n = sql.selectOne("event.hasAdminAuthority", userNo);
        return n != null && n > 0;
    }

    @Override
    public Long selectEventCreator(Long eventId) {
        return sql.selectOne("event.selectEventCreator", eventId);
    }
}
