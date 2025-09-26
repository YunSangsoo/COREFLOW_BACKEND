package com.kh.coreflow.humanmanagement.controller;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kh.coreflow.common.model.service.FileService;
import com.kh.coreflow.common.model.vo.FileDto.customFile;
import com.kh.coreflow.humanmanagement.model.dto.MemberDto;
import com.kh.coreflow.humanmanagement.model.dto.MemberDto.Department;
import com.kh.coreflow.humanmanagement.model.dto.MemberDto.MemberPatch;
import com.kh.coreflow.humanmanagement.model.dto.MemberDto.MemberPost;
import com.kh.coreflow.humanmanagement.model.dto.MemberDto.MemberResponse;
import com.kh.coreflow.humanmanagement.model.dto.MemberDto.Position;
import com.kh.coreflow.humanmanagement.model.service.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
public class MemberController {
	private final MemberService service;
	private final FileService fileService;
	
	// 부모 부서 조회
	@CrossOrigin(origins="http://localhost:5173")
	@GetMapping("/departments")
	public ResponseEntity<List<Department>> deptList(){
		List<Department> deptList = service.deptList();
		
		if(deptList != null && !deptList.isEmpty()) {
			return ResponseEntity.ok(deptList);
		}else {
			return ResponseEntity.noContent().build();
		}
	}
	
	// 자식 부서 조회
	@CrossOrigin(origins="http://localhost:5173")
	@GetMapping("/departments/{parentId}")
	public ResponseEntity<List<Department>> deptDetailList(
			@PathVariable Integer parentId
			){
		List<Department> deptDetailList = service.deptDetailList(parentId);
		
		if(deptDetailList != null && !deptDetailList.isEmpty()) {
			return ResponseEntity.ok(deptDetailList);
		}else {
			return ResponseEntity.noContent().build();
		}
	}
	
	// 직위 조회
	@CrossOrigin(origins="http://localhost:5173")
	@GetMapping("/positions")
	public ResponseEntity<List<Position>> posiList(){
		List<Position> posiList = service.posiList();
		
		if(posiList != null) {
			return ResponseEntity.ok(posiList);
		}else {
			return ResponseEntity.noContent().build();
		}
	}
	
	// 사원 조회
	@CrossOrigin(origins="http://localhost:5173")
	@GetMapping("/members")
	public ResponseEntity<List<MemberResponse>> memberList(
			@RequestParam Map<String,String> searchParams
			){		
		List<MemberResponse> memberList = service.memberList(searchParams);
		
		if(!memberList.isEmpty()) {
			return ResponseEntity.ok(memberList);
		}else {
			return ResponseEntity.noContent().build();
		}
	}
	
	// 사원 상세 조회
	@CrossOrigin(origins="http://localhost:5173")
	@PreAuthorize("hasAnyRole('ADMIN','HR')")
	@GetMapping("/members/{userNo}")
	public ResponseEntity<MemberResponse> memberDetail(
			@PathVariable Long userNo
			){
		customFile profile = fileService.getFile("P",userNo);
		if(profile == null) {
			customFile tempProfile = new customFile();
			tempProfile.setChangeName("CHAT_PROFILE_DEFAULT.jpg");
			tempProfile.setImageCode("P");
			tempProfile.setOriginName("CHAT_PROFILE_DEFAULT.jpg");
			profile = tempProfile;
		}
		
		MemberResponse member = service.memberDetail(userNo);
		member.setProfile(profile);
				
		if(member != null) {	
			return ResponseEntity.ok(member);
		}else {
			return ResponseEntity.notFound().build();
		}
	}
	
	// 사원 등록
	@CrossOrigin(origins="http://localhost:5173")
	@PreAuthorize("hasAnyRole('ADMIN','HR')")
	@PostMapping("/members")
	public ResponseEntity<Void> memberInsert(
			@RequestPart("data") MemberPost member,
			@RequestPart(value = "profile", required = false) MultipartFile profile
			){
		int result = service.memberInsert(member, profile);
		
		if(result > 0) {
			return ResponseEntity.created(URI.create("/members")).build();
		}else {
			return ResponseEntity.badRequest().build();
		}
	}
	
	// 사원 정보 수정
	@CrossOrigin(origins="http://localhost:5173")
	@PreAuthorize("hasAnyRole('ADMIN','HR')")
	@PatchMapping("/members/{userNo}")
	public ResponseEntity<Void> memberUpdate(
			@PathVariable Long userNo,
			@RequestPart(value= "memberdata") MemberPatch member,
			@RequestPart(value = "profile", required=false) MultipartFile file
			){
		member.setUserNo(userNo);
		int result = service.memberUpdate(member,file);
		
		if(result > 0) {
			return ResponseEntity.noContent().build();
		}else {
			return ResponseEntity.notFound().build();
		}
	}
	
	// 사원 정보 삭제
	@CrossOrigin(origins="http://localhost:5173")
	@PreAuthorize("hasAnyRole('ADMIN','HR')")
	@DeleteMapping("members/{userNo}")
	public ResponseEntity<Void> memberDelete(
			@PathVariable Long userNo
			) {
		int result = service.memberDelete(userNo);
		
		if(result > 0) {
			return ResponseEntity.noContent().build();
		}else {
			return ResponseEntity.notFound().build();
		}
	}
	
	// 남건후
    @GetMapping("/CalendarMembers")
    public ResponseEntity<List<MemberDto.MemberLite>> search(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(required = false, defaultValue = "30") Integer limit,
            @RequestParam(required = false) Long depId
    ) {
        return ResponseEntity.ok(service.search(query, limit, depId));
    }
    @GetMapping("/CalendarDepartments")
    public ResponseEntity<List<MemberDto.DepartmentLite>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }
}