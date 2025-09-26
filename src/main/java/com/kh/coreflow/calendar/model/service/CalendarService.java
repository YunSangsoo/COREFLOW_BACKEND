package com.kh.coreflow.calendar.model.service;

import java.util.List;
import java.util.Map;

import com.kh.coreflow.calendar.model.dto.CalendarDto;

public interface CalendarService {
    List<CalendarDto.SummaryRes> getVisibleCalendars(Long userNo);
    CalendarDto.DetailRes getCalendar(Long calId);

    Long createCalendar(Long ownerUserNo, CalendarDto.CreateReq req);
    void updateCalendar(Long ownerUserNo, Long calId, CalendarDto.UpdateReq req);
    void deleteCalendar(Long ownerUserNo, Long calId);
    CalendarDto.ShareListRes getCalendarShares(Long calId, Long userNo);
    void applyCalendarShares(Long calId, Long userNo, String mode, CalendarDto.ShareUpsertReq req);
    
    List<Map<String, Object>> getDepartments(Long parentId);
    List<Map<String, Object>> getPositions();
    List<Map<String, Object>> findMembers(String query, Integer limit, Long depId);
    
    String getMyRoleForCalendar(Long userNo, Long calId);
    

}