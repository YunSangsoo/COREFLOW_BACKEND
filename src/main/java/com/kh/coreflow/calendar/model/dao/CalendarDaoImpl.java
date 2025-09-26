package com.kh.coreflow.calendar.model.dao;

import com.kh.coreflow.calendar.model.dto.CalendarDto;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CalendarDaoImpl implements CalendarDao {

    private final SqlSessionTemplate sql;

    @Override
    public List<CalendarDto.SummaryRes> selectVisibleCalendars(Long userNo) {
        Map<String,Object> p = new HashMap<>();
        p.put("userNo", userNo);
        return sql.selectList("calendar.selectVisibleCalendars", p);
    }

    @Override
    public CalendarDto.DetailRes selectById(Long calId) {
        Map<String,Object> p = new HashMap<>();
        p.put("calId", calId);
        return sql.selectOne("calendar.selectById", p);
    }

    @Override
    public Long insertCalendar(Long ownerUserNo, CalendarDto.CreateReq req) {
        Map<String,Object> p = new HashMap<>();
        p.put("ownerUserNo", ownerUserNo);
        p.put("name", req.getName());
        p.put("color", req.getColor());
        p.put("defaultRole", req.getDefaultRole());
        p.put("deptId", req.getDepId());
        sql.insert("calendar.insertCalendar", p); // selectKey로 calId 채워짐
        return (Long)p.get("calId");
    }

    @Override
    public int updateCalendar(Long ownerUserNo, Long calId, CalendarDto.UpdateReq req) {
        Map<String,Object> p = new HashMap<>();
        p.put("ownerUserNo", ownerUserNo);
        p.put("calId", calId);
        p.put("name", req.getName());
        p.put("color", req.getColor());
        p.put("defaultRole", req.getDefaultRole());
        return sql.update("calendar.updateCalendar", p);
    }

    @Override
    public int deleteCalendar(Long ownerUserNo, Long calId) {
        Map<String,Object> p = new HashMap<>();
        p.put("ownerUserNo", ownerUserNo);
        p.put("calId", calId);
        return sql.update("calendar.deleteCalendar", p);
    }
    @Override
    public int deleteCalendarByEditor(Long calId, Long userNo) {
        Map<String, Object> p = Map.of("calId", calId, "userNo", userNo);
        return sql.update("calendar.deleteCalendarByEditor", p);
    }
    
    @Override
    public boolean existsCalendarAccess(Long userNo, Long calId) {
        Map<String,Object> p = new HashMap<>();
        p.put("userNo", userNo);
        p.put("calId", calId);
        Integer cnt = sql.selectOne("calendar.existsCalendarAccess", p);
        return cnt != null && cnt > 0;
    }
    
 // 효과적 권한 조회 (개인 > 직급 > 부서)
    @Override
    public String selectEffectiveCalendarRoleForUser(Long userNo, Long calId) {
        Map<String,Object> p = new HashMap<>();
        p.put("userNo", userNo);
        p.put("calId", calId);
        return sql.selectOne("calendar.selectEffectiveCalendarRoleForUser", p);
    }

    // 에디터 수정 쿼리 (권한은 서비스 선검증)
    @Override
    public int updateCalendarByEditor(Long calId, CalendarDto.UpdateReq req) {
        Map<String,Object> p = new HashMap<>();
        p.put("calId", calId);
        p.put("name", req.getName());
        p.put("color", req.getColor());
        p.put("defaultRole", req.getDefaultRole());
        return sql.update("calendar.updateCalendarByEditor", p);
    }
    
    @Override
    public boolean isCalendarOwner(Long userNo, Long calId) {
        java.util.Map<String,Object> p = new java.util.HashMap<>();
        p.put("userNo", userNo);
        p.put("calId", calId);
        Integer v = sql.selectOne("calendar.isCalendarOwner", p);
        return v != null && v == 1;
    }

    @Override
    public String selectCalendarDefaultRole(Long calId) {
        return sql.selectOne("calendar.selectCalendarDefaultRole", calId);
    }

    @Override
    public java.util.List<CalendarDto.ShareUser> selectCalendarMemberShares(Long calId) {
        return sql.selectList("calendar.selectCalendarMemberShares", calId);
    }
    @Override
    public java.util.List<CalendarDto.ShareDept> selectCalendarDeptShares(Long calId) {
        return sql.selectList("calendar.selectCalendarDeptShares", calId);
    }
    @Override
    public java.util.List<CalendarDto.SharePos> selectCalendarPosShares(Long calId) {
        return sql.selectList("calendar.selectCalendarPosShares", calId);
    }

    @Override
    public int deleteAllMemberShares(Long calId) {
        return sql.delete("calendar.deleteAllMemberShares", calId);
    }
    @Override
    public int deleteAllDeptShares(Long calId) {
        return sql.delete("calendar.deleteAllDeptShares", calId);
    }
    @Override
    public int deleteAllPosShares(Long calId) {
        return sql.delete("calendar.deleteAllPosShares", calId);
    }

    @Override
    public int deleteMemberSharesInList(Long calId, java.util.List<CalendarDto.ShareUser> users) {
        java.util.Map<String,Object> p = new java.util.HashMap<>();
        p.put("calId", calId);
        p.put("list", users);
        return sql.delete("calendar.deleteMemberSharesInList", p);
    }
    @Override
    public int deleteDeptSharesInList(Long calId, java.util.List<CalendarDto.ShareDept> depts) {
        java.util.Map<String,Object> p = new java.util.HashMap<>();
        p.put("calId", calId);
        p.put("list", depts);
        return sql.delete("calendar.deleteDeptSharesInList", p);
    }
    @Override
    public int deletePosSharesInList(Long calId, java.util.List<CalendarDto.SharePos> poses) {
        java.util.Map<String,Object> p = new java.util.HashMap<>();
        p.put("calId", calId);
        p.put("list", poses);
        return sql.delete("calendar.deletePosSharesInList", p);
    }

    @Override
    public int insertMemberSharesBulk(Long calId, java.util.List<CalendarDto.ShareUser> users, Long actorUserNo) {
        java.util.Map<String,Object> p = new java.util.HashMap<>();
        p.put("calId", calId); p.put("list", users); p.put("actorUserNo", actorUserNo);
        return sql.insert("calendar.insertMemberSharesBulk", p);
    }
    @Override
    public int insertDeptSharesBulk(Long calId, java.util.List<CalendarDto.ShareDept> depts, Long actorUserNo) {
        java.util.Map<String,Object> p = new java.util.HashMap<>();
        p.put("calId", calId); p.put("list", depts); p.put("actorUserNo", actorUserNo);
        return sql.insert("calendar.insertDeptSharesBulk", p);
    }
    @Override
    public int insertPosSharesBulk(Long calId, java.util.List<CalendarDto.SharePos> poses, Long actorUserNo) {
        java.util.Map<String,Object> p = new java.util.HashMap<>();
        p.put("calId", calId); p.put("list", poses); p.put("actorUserNo", actorUserNo);
        return sql.insert("calendar.insertPosSharesBulk", p);
    }
    
    @Override
    public List<Map<String, Object>> selectDepartmentsByParent(Long parentId) {
    	 Map<String, Object> param = new HashMap<>();
    	    param.put("parentId", parentId);   // HashMap은 value null OK
    	    return sql.selectList("calendar.selectDepartmentsByParent", param);
    }

    @Override
    public List<Map<String, Object>> selectAllPositions() {
        return sql.selectList("calendar.selectAllPositions");
    }

    @Override
    public List<Map<String, Object>> searchMembers(String query, Integer limit, Long depId) {
    	Map<String,Object> param = new HashMap<>();
    	param.put("query", query);
    	param.put("limit", (limit == null ? 20 : limit));
    	param.put("depId", depId);
    	return sql.selectList("calendar.searchMembers", param);
    }
}
