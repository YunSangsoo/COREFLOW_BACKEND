package com.kh.coreflow.conference.controller;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.kh.coreflow.conference.model.dto.ConferenceRoomDto;
import com.kh.coreflow.conference.service.ConferenceRoomService;
import com.kh.coreflow.model.dto.UserDto.UserDeptPoscode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
@Slf4j
public class ConferenceRoomController {

    private final ConferenceRoomService service;
    private final com.kh.coreflow.calendar.model.service.EventService eventService;

    /* 회의실 목록 조회 */
    @GetMapping
    public ResponseEntity<List<ConferenceRoomDto.Res>> list(
            @RequestParam(required = false) String buildingName,
            @RequestParam(required = false) String floor,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer minCapacity
    ) {
        log.info("[ROOM] list building={}, floor={}, status={}, minCap={}",
                buildingName, floor, status, minCapacity);
        List<ConferenceRoomDto.Res> rooms = service.getRooms(buildingName, floor, status, minCapacity);
        return ResponseEntity.ok(rooms);
    }

    /* 회의실 단건 조회 */
    @GetMapping("/{roomId:\\d+}")
    public ResponseEntity<ConferenceRoomDto.Res> get(@PathVariable Long roomId) {
        log.info("[ROOM] get roomId={}", roomId);
        ConferenceRoomDto.Res room = service.getRoom(roomId);
        return (room != null) ? ResponseEntity.ok(room) : ResponseEntity.notFound().build();
    }

    /* 회의실 생성 */
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @PostMapping
    public ResponseEntity<Long> create(
            @AuthenticationPrincipal UserDeptPoscode me,
            @RequestBody ConferenceRoomDto.CreateReq req
    ) {
        if (me == null) return ResponseEntity.status(401).build();
        Long id = service.createRoom(req, me.getUserNo());
        return ResponseEntity.created(URI.create("/rooms/" + id)).body(id);
    }

    /* 회의실 예약 생성(HOLD 또는 즉시 확정) */
    @PostMapping("/reservations")
    public ResponseEntity<Long> createReservation(
            @AuthenticationPrincipal UserDeptPoscode me,
            @RequestBody ConferenceRoomDto.ReservationCreateReq req
    ) {
        if (me == null) return ResponseEntity.status(401).build();
        Long id = service.createRoomReservation(req, me.getUserNo());
        if (req.getEventId() != null && req.getRoomId() != null) {
            eventService.updateEventRoomLink(me.getUserNo(), req.getEventId(), req.getRoomId());
        }
        return ResponseEntity.created(URI.create("/rooms/reservations/" + id)).body(id);
    }

    /* 내 예약 조회 (옵션: roomId, from, to) */
    @GetMapping("/{roomId}/my-reservations")
    public ResponseEntity<List<ConferenceRoomDto.ReservationRes>> myReservations(
            @AuthenticationPrincipal UserDeptPoscode me,
            @PathVariable Long roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        if (me == null) return ResponseEntity.status(401).build();
        Timestamp fromTs = (from != null) ? Timestamp.valueOf(from) : null;
        Timestamp toTs   = (to   != null) ? Timestamp.valueOf(to)   : null;
        List<ConferenceRoomDto.ReservationRes> list =
                service.getMyReservations(me.getUserNo(), roomId, fromTs, toTs);
        return ResponseEntity.ok(list);
    }

    /* 도면 업로드/조회/목록 */

    @PreAuthorize("hasAnyRole('HR','ADMIN')") // HR/ADMIN만 업로드 가능
    @PostMapping(value = "/floormaps", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadFloorMap(@RequestPart("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        String ct = Optional.ofNullable(file.getContentType()).orElse("");
        if (!ct.contains("svg")) return ResponseEntity.status(415).build(); // SVG만 허용

        // 저장 디렉토리 (운영 환경에 맞게 조정)
        Path dir = Paths.get(System.getProperty("user.home"), "coreflow", "floormaps");
        Files.createDirectories(dir);

        String filename = "floor-" + System.currentTimeMillis() + ".svg";
        Path dest = dir.resolve(filename);
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

        // 절대 URL (예: http://localhost:8081/api/rooms/floormaps/floor-...svg)
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/rooms/floormaps/")
                .path(filename)
                .toUriString();

        return ResponseEntity.ok(Map.of("url", url, "filename", filename));
    }

    // 정적 매핑 파일 없이 컨트롤러에서 직접 서빙
    @GetMapping(value = "/floormaps/{name:.+}", produces = "image/svg+xml")
    public ResponseEntity<byte[]> getFloorMap(@PathVariable("name") String name) throws Exception {
        Path file = Paths.get(System.getProperty("user.home"), "coreflow", "floormaps", name);
        if (!Files.exists(file)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Files.readAllBytes(file));
    }

    // 업로드 폴더 경로 (공통)
    private Path floorMapDir() throws Exception {
        Path dir = Paths.get(System.getProperty("user.home"), "coreflow", "floormaps");
        Files.createDirectories(dir);
        return dir;
    }

    // 도면 리스트 응답 DTO
    @lombok.Data @lombok.AllArgsConstructor @lombok.NoArgsConstructor @lombok.Builder
    public static class FloorMapMeta {
        private String filename;   // floor-...svg
        private String url;        // 절대 URL (/api/rooms/floormaps/xxx.svg)
        private String title;      // SVG <title> (없으면 null)
        private long size;         // 바이트
        private Instant modifiedAt;// 마지막 수정시각
    }

    // SVG <title> 추출 (없으면 null)
    private String extractSvgTitle(Path p) {
        try {
            byte[] bytes = Files.readAllBytes(p);
            String s = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            Matcher m = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(s);
            return m.find() ? m.group(1).trim() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /** 도면 목록 보기 */
    @GetMapping("/floormaps")
    public ResponseEntity<List<FloorMapMeta>> listFloorMaps() throws Exception {
        Path dir = floorMapDir();
        try (Stream<Path> stream = Files.list(dir)) {
            List<FloorMapMeta> list = stream
                .filter(p -> Files.isRegularFile(p) && p.getFileName().toString().toLowerCase().endsWith(".svg"))
                .map(p -> {
                    String filename = p.getFileName().toString();
                    String url = org.springframework.web.servlet.support.ServletUriComponentsBuilder
                            .fromCurrentContextPath()
                            .path("/api/rooms/floormaps/")
                            .path(filename)
                            .toUriString();
                    String title = extractSvgTitle(p);
                    try {
                        return FloorMapMeta.builder()
                                .filename(filename)
                                .url(url)
                                .title(title)
                                .size(Files.size(p))
                                .modifiedAt(Files.getLastModifiedTime(p).toInstant())
                                .build();
                    } catch (Exception e) {
                        return FloorMapMeta.builder()
                                .filename(filename).url(url).title(title)
                                .size(0L).modifiedAt(Instant.EPOCH).build();
                    }
                })
                .sorted(Comparator.comparing(FloorMapMeta::getModifiedAt).reversed())
                .collect(Collectors.toList());
            return ResponseEntity.ok(list);
        }
    }

    /* 가용성 조회 */
    @GetMapping("/availability")
    public ResponseEntity<List<ConferenceRoomDto.AvailabilityRes>> availability(
            @RequestParam(name = "from", required = false) String from,
            @RequestParam(name = "to",   required = false) String to,
            @RequestParam(name = "startAt", required = false) String startAt,
            @RequestParam(name = "endAt",   required = false) String endAt,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) String buildingName,
            @RequestParam(required = false) String floor
    ) {
        String a = (startAt != null && !startAt.isBlank()) ? startAt : from;
        String b = (endAt   != null && !endAt.isBlank())   ? endAt   : to;

        if (a == null || b == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "startAt/endAt 또는 from/to 중 하나는 반드시 전달해야 합니다."
            );
        }

        LocalDateTime s = parseFlexibleDateTime(a);
        LocalDateTime e = parseFlexibleDateTime(b);

        return ResponseEntity.ok(
            service.searchAvailability(
                Timestamp.valueOf(s),
                Timestamp.valueOf(e),
                minCapacity,
                buildingName,
                floor
            )
        );
    }
    private static LocalDateTime parseFlexibleDateTime(String v) {
        String x = v.trim();
        try {
            return LocalDateTime.parse(x, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception ignore) { /* 다음 시도 */ }
        try {
            return LocalDateTime.parse(x);
        } catch (Exception ignore) { /* 다음 시도 */ }
        if (x.length() > 10 && x.charAt(10) == ' ') {
            String iso = x.substring(0, 10) + 'T' + x.substring(11);
            try {
                return LocalDateTime.parse(iso);
            } catch (Exception ignore) {}
        }
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "잘못된 날짜/시간 형식입니다: " + v + " (허용: yyyy-MM-dd HH:mm:ss 또는 ISO-8601)"
        );
    }

    // 예약 취소 (idempotent)
    @PostMapping("/reservations/{resvId}/cancel")
    public ResponseEntity<Void> cancelReservation(
            @AuthenticationPrincipal UserDeptPoscode me,
            @PathVariable Long resvId
    ) {
        if (me == null) return ResponseEntity.status(401).build();
        service.cancelReservation(resvId, me.getUserNo());
        return ResponseEntity.noContent().build();
    }

    // 예약 시간 변경(겹침 검사 포함)
    @PostMapping("/reservations/{resvId}/time")
    public ResponseEntity<ConferenceRoomDto.ReservationRes> updateReservationTime(
            @AuthenticationPrincipal UserDeptPoscode me,
            @PathVariable Long resvId,
            @RequestBody ConferenceRoomDto.ReservationTimeUpdateReq req
    ) {
        if (me == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(service.updateReservationTime(resvId, req, me.getUserNo()));
    }

    // 이벤트와 연결(이벤트 시간으로 동기화, HOLD였다면 확정)
    @PostMapping("/reservations/{resvId}/attach-event")
    public ResponseEntity<ConferenceRoomDto.ReservationRes> attachEvent(
            @AuthenticationPrincipal UserDeptPoscode me,
            @PathVariable Long resvId,
            @RequestBody ConferenceRoomDto.ReservationAttachEventReq req
    ) {
        if (me == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(service.attachEvent(resvId, req.getEventId(), me.getUserNo()));
    }
    
    /**
     * 회의실 상세 + 기간 내 예약현황
     * GET /conference-rooms/{roomId}/detail?from=YYYY-MM-DD HH:mm:ss&to=YYYY-MM-DD HH:mm:ss
     */

    @GetMapping("/{roomId}/detail")
    public ResponseEntity<ConferenceRoomDto.RoomDetailRes> getDetail(
            @AuthenticationPrincipal UserDeptPoscode me,
            @PathVariable Long roomId,
            @RequestParam String from,
            @RequestParam String to
    ) {
        if (me == null) return ResponseEntity.status(401).build();

        ConferenceRoomDto.RoomDetailRes res =
                service.getRoomDetail(me.getUserNo(), roomId, from, to);

        if (res == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(res);
    }
    
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @PutMapping("/{roomId}")
    public ResponseEntity<Void> update(
            @AuthenticationPrincipal UserDeptPoscode me,
            @PathVariable Long roomId,
            @RequestBody ConferenceRoomDto.CreateReq req   // UpdateReq가 따로 없으면 CreateReq 재사용
    ) {
        if (me == null) return ResponseEntity.status(401).build();
        service.updateRoom(roomId, req, me.getUserNo());
        return ResponseEntity.noContent().build(); // 204
    }
    
    @PreAuthorize("hasAnyRole('HR','ADMIN')")
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDeptPoscode me,
            @PathVariable Long roomId
    ) {
        if (me == null) return ResponseEntity.status(401).build();
        service.deleteRoom(roomId, me.getUserNo());
        return ResponseEntity.noContent().build(); // 204
    }
}

