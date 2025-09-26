package com.kh.coreflow.calendar.model.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.kh.coreflow.calendar.model.dao.CalendarDao;
import com.kh.coreflow.calendar.model.dto.CalendarDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {

    private final CalendarDao calendarDao;

    @Override
    public List<CalendarDto.SummaryRes> getVisibleCalendars(Long userNo) {
        return calendarDao.selectVisibleCalendars(userNo);
    }

    @Override
    public CalendarDto.DetailRes getCalendar(Long calId) {
        return calendarDao.selectById(calId);
    }

    @Transactional
    @Override
    public Long createCalendar(Long ownerUserNo, CalendarDto.CreateReq req) {
        return calendarDao.insertCalendar(ownerUserNo, req);
    }

    @Transactional
    @Override
    public void updateCalendar(Long userNo, Long calId, CalendarDto.UpdateReq req) {
        // 1) 소유자 수정 시도
        int rows = calendarDao.updateCalendar(userNo, calId, req);
        if (rows > 0) return;

        // 2) 소유자가 아니라면: 효과적 권한(개인 > 직급 > 부서) 확인
        final String role = calendarDao.selectEffectiveCalendarRoleForUser(userNo, calId);
        final Set<String> canEdit = Set.of("EDITOR", "CONTRIBUTOR"); // 관리자/편집가능

        if (role != null && canEdit.contains(role)) {
            int r2 = calendarDao.updateCalendarByEditor(calId, req); // WHERE: calId + not deleted
            if (r2 == 0) throw new IllegalStateException("수정 실패(대상 없음)");
        } else {
            throw new IllegalStateException("수정 권한이 없습니다.");
        }
    }

    @Transactional
    @Override
    public void deleteCalendar(Long userNo, Long calId) {
        // 1) 소유자 먼저 시도 (기존 쿼리: WHERE CAL_ID=? AND OWNER_USER_NO=?)
        int rows = calendarDao.deleteCalendar(userNo, calId);
        if (rows > 0) return;
        // 2) 소유자가 아니면 효과적 권한 확인 (개인>직급>부서)
        final String role = calendarDao.selectEffectiveCalendarRoleForUser(userNo, calId);
        if (role != null && "EDITOR".equalsIgnoreCase(role)) {
            // 소유자 조건 없이 삭제(논리삭제) — 새 쿼리 호출
            int r2 = calendarDao.deleteCalendarByEditor(calId, userNo);
            if (r2 > 0) return;
            throw new IllegalStateException("삭제 실패(대상 없음)");
        }
        // 3) 권한 없음
        throw new IllegalStateException("삭제 권한이 없습니다.");
    }
    
    @Override
    @Transactional(readOnly = true)
    public CalendarDto.ShareListRes getCalendarShares(Long calId, Long userNo) {
        if (!calendarDao.isCalendarOwner(userNo, calId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다. (소유자만 조회 가능)");
        }
        return CalendarDto.ShareListRes.builder()
            .users(calendarDao.selectCalendarMemberShares(calId))
            .departments(calendarDao.selectCalendarDeptShares(calId))
            .positions(calendarDao.selectCalendarPosShares(calId))
            .defaultRole(calendarDao.selectCalendarDefaultRole(calId))
            .build();
    }

    @org.springframework.transaction.annotation.Transactional
    @Override
    public void applyCalendarShares(Long calId, Long userNo, String mode, CalendarDto.ShareUpsertReq req) {
        // 소유자만 허용
    	if (!calendarDao.isCalendarOwner(userNo, calId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다. (소유자만 변경 가능)");
        }
        final boolean replace = "replace".equalsIgnoreCase(mode);

        if (replace) {
            calendarDao.deleteAllMemberShares(calId);
            calendarDao.deleteAllDeptShares(calId);
            calendarDao.deleteAllPosShares(calId);
            if (req.getUsers() != null && !req.getUsers().isEmpty()) {
                calendarDao.insertMemberSharesBulk(calId, req.getUsers(), userNo);
            }
            if (req.getDepartments() != null && !req.getDepartments().isEmpty()) {
                calendarDao.insertDeptSharesBulk(calId, req.getDepartments(), userNo);
            }
            if (req.getPositions() != null && !req.getPositions().isEmpty()) {
                calendarDao.insertPosSharesBulk(calId, req.getPositions(), userNo);
            }
        } else {
            if (req.getUsers() != null && !req.getUsers().isEmpty()) {
                calendarDao.deleteMemberSharesInList(calId, req.getUsers());
                calendarDao.insertMemberSharesBulk(calId, req.getUsers(), userNo);
            }
            if (req.getDepartments() != null && !req.getDepartments().isEmpty()) {
                calendarDao.deleteDeptSharesInList(calId, req.getDepartments());
                calendarDao.insertDeptSharesBulk(calId, req.getDepartments(), userNo);
            }
            if (req.getPositions() != null && !req.getPositions().isEmpty()) {
                calendarDao.deletePosSharesInList(calId, req.getPositions());
                calendarDao.insertPosSharesBulk(calId, req.getPositions(), userNo);
            }
        }
    }
    
    @Override
    public List<Map<String, Object>> getDepartments(Long parentId) {
        return calendarDao.selectDepartmentsByParent(parentId);
    }

    @Override
    public List<Map<String, Object>> getPositions() {
        return calendarDao.selectAllPositions();
    }

    @Override
    public List<Map<String, Object>> findMembers(String query, Integer limit, Long depId) {
        return calendarDao.searchMembers(query, limit, depId);
    }
    @Override
    public String getMyRoleForCalendar(Long userNo, Long calId) {
        String r = calendarDao.selectEffectiveCalendarRoleForUser(userNo, calId);
        return r == null ? "NONE" : r.toUpperCase();
    }
}
