package com.kh.coreflow.calendar;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.kh.coreflow.calendar.model.dto.CalendarDto;
import com.kh.coreflow.calendar.model.service.CalendarService;
import com.kh.coreflow.model.dto.UserDto.UserDeptPoscode;
import com.kh.coreflow.security.CustomUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 최종 URL: /api/calendars/... (server.servlet.context-path=/api) */
@RestController
@RequestMapping("/calendar")
@RequiredArgsConstructor
@Slf4j
public class CalendarController {

    private final CalendarService calendarService; // 프로젝트에서 쓰는 Service 타입명에 맞춰주세요

    // 생성 응답용 작은 타입 (Java 16+ 이면 record 사용)
    public record CalIdResponse(Long calId) {}

    @GetMapping("/visible")
    public ResponseEntity<List<CalendarDto.SummaryRes>> getVisible(
            @AuthenticationPrincipal UserDeptPoscode me
    ) {
        if (me == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(calendarService.getVisibleCalendars(me.getUserNo()));
    }

    @GetMapping("/{calId}")
    public ResponseEntity<CalendarDto.DetailRes> getCalendar(@PathVariable Long calId) {
        return ResponseEntity.ok(calendarService.getCalendar(calId));
    }

    @PostMapping
    public ResponseEntity<CalIdResponse> createCalendar(
            @AuthenticationPrincipal UserDeptPoscode me,
            @Valid @RequestBody CalendarDto.CreateReq req
    ) {
        if (me == null) return ResponseEntity.status(401).build();

        Long id = calendarService.createCalendar(me.getUserNo(), req);

        // Location: /calendar/{id} (현재 요청 기준으로 안전하게 구성)
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity
                .created(location)               // 201 Created + Location
                .body(new CalIdResponse(id));    // { "calId": id }
    }

    @PutMapping("/{calId}")
    public ResponseEntity<Void> updateCalendar(
            @AuthenticationPrincipal UserDeptPoscode me,
            @PathVariable Long calId,
            @Valid @RequestBody CalendarDto.UpdateReq req
    ) {
        if (me == null) return ResponseEntity.status(401).build();
        calendarService.updateCalendar(me.getUserNo(), calId, req);
        return ResponseEntity.noContent().build(); // 204
    }

    @DeleteMapping("/{calId}")
    public ResponseEntity<Void> deleteCalendar(
            @AuthenticationPrincipal UserDeptPoscode me,
            @PathVariable Long calId
    ) {
        if (me == null) return ResponseEntity.status(401).build();
        calendarService.deleteCalendar(me.getUserNo(), calId);
        return ResponseEntity.noContent().build(); // 204
    }
    
    @GetMapping("/{calId}/shares")
    public ResponseEntity<CalendarDto.ShareListRes> getShares(
            @AuthenticationPrincipal(expression = "userNo") Long userNo, // ← 핵심
            @PathVariable Long calId
    ) {
        if (userNo == null) return ResponseEntity.status(401).build();
        CalendarDto.ShareListRes res = calendarService.getCalendarShares(calId, userNo);
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{calId}/shares")
    public ResponseEntity<Void> putShares(
            @AuthenticationPrincipal(expression = "userNo") Long userNo, // ← 이 한 줄이면 충분
            @PathVariable Long calId,
            @RequestParam(name = "mode", defaultValue = "merge") String mode,
            @Valid @RequestBody CalendarDto.ShareUpsertReq req
    ) {
        if (userNo == null) return ResponseEntity.status(401).build();
        calendarService.applyCalendarShares(calId, userNo, mode, req);
        return ResponseEntity.noContent().build();
    }
    
 // 조직: 부서 트리
    @GetMapping("/org/departments/tree")
    public ResponseEntity<List<Map<String, Object>>> getDepartments(@RequestParam(required=false) Long parentId) {
        return ResponseEntity.ok(calendarService.getDepartments(parentId));
    }

    // 조직: 직급 목록
    @GetMapping("/org/positions")
    public ResponseEntity<List<Map<String, Object>>> getPositions() {
        return ResponseEntity.ok(calendarService.getPositions());
    }

    // 멤버 검색
    @GetMapping("/members")
    public ResponseEntity<List<Map<String, Object>>> searchMembers(
            @RequestParam(required=false) String query,
            @RequestParam(required=false) Integer limit,
            @RequestParam(required=false) Long depId
    ) {
        return ResponseEntity.ok(calendarService.findMembers(query, limit, depId));
    }
}