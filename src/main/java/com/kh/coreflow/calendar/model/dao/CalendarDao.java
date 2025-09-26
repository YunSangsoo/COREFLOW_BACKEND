package com.kh.coreflow.calendar.model.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.kh.coreflow.calendar.model.dto.CalendarDto;

public interface CalendarDao {
    List<CalendarDto.SummaryRes> selectVisibleCalendars(Long userNo);
    CalendarDto.DetailRes selectById(Long calId);

    Long insertCalendar(Long ownerUserNo, CalendarDto.CreateReq req);
    int updateCalendar(
    	    @Param("userNo") Long userNo,
    	    @Param("calId") Long calId,
    	    @Param("req") CalendarDto.UpdateReq req
    	);
    int deleteCalendar(Long ownerUserNo, Long calId);

    boolean existsCalendarAccess(Long userNo, Long calId);
    
    boolean isCalendarOwner(Long userNo, Long calId);
    String selectCalendarDefaultRole(Long calId);

    java.util.List<CalendarDto.ShareUser> selectCalendarMemberShares(Long calId);
    java.util.List<CalendarDto.ShareDept> selectCalendarDeptShares(Long calId);
    java.util.List<CalendarDto.SharePos>  selectCalendarPosShares(Long calId);

    int deleteAllMemberShares(Long calId);
    int deleteAllDeptShares(Long calId);
    int deleteAllPosShares(Long calId);

    int deleteMemberSharesInList(Long calId, java.util.List<CalendarDto.ShareUser> users);
    int deleteDeptSharesInList(Long calId, java.util.List<CalendarDto.ShareDept> depts);
    int deletePosSharesInList(Long calId, java.util.List<CalendarDto.SharePos> poses);

    int insertMemberSharesBulk(Long calId, java.util.List<CalendarDto.ShareUser> users, Long actorUserNo);
    int insertDeptSharesBulk(Long calId, java.util.List<CalendarDto.ShareDept> depts, Long actorUserNo);
    int insertPosSharesBulk(Long calId, java.util.List<CalendarDto.SharePos> poses, Long actorUserNo);
    
    List<Map<String, Object>> selectDepartmentsByParent(Long parentId);
    List<Map<String, Object>> selectAllPositions();
    List<Map<String, Object>> searchMembers(String query, Integer limit, Long depId);
    
    String selectEffectiveCalendarRoleForUser(
    	    @Param("userNo") Long userNo,
    	    @Param("calId") Long calId
    	);
    int updateCalendarByEditor(
    	    @Param("calId") Long calId,
    	    @Param("req") CalendarDto.UpdateReq req
    	);
    int deleteCalendarByEditor(
    	    @Param("calId") Long calId,
    	    @Param("userNo") Long userNo
    	);
}
