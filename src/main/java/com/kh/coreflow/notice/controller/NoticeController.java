package com.kh.coreflow.notice.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kh.coreflow.notice.model.dto.NoticeDto.NoticeDetail;
import com.kh.coreflow.notice.model.dto.NoticeDto.NoticeInsert;
import com.kh.coreflow.notice.model.dto.NoticeDto.NoticeResponse;
import com.kh.coreflow.notice.model.dto.NoticeDto.NoticeSearch;
import com.kh.coreflow.notice.model.service.NoticeService;
import com.kh.coreflow.common.model.service.FileService;
import com.kh.coreflow.common.model.vo.FileDto.customFile;
import com.kh.coreflow.model.dto.UserDto.UserDeptcode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
public class NoticeController {
	private final NoticeService service;
	private final FileService fileService;
	
	// 공지 조회 + 검색(제목, 내용, 작성자) 
	@GetMapping("/notice/main")
	public ResponseEntity<List<NoticeResponse>> notice(
			@AuthenticationPrincipal UserDeptcode auth,
			@ModelAttribute NoticeSearch noticeSearch
			){
		
		List<NoticeResponse> notiList;
		
		long depId = auth.getDepId();
//		long posId = auth.getPosId();
		int posId = noticeSearch.getPosId();
		
		String searchType = noticeSearch.getSearchType();
		String keyword = noticeSearch.getKeyword();
		
		Map<String, Object> params = new HashMap<>();
		params.put("depId", depId);
		params.put("posId", posId);
		
//		log.info("depId : {}",depId);
//		log.info("posId : {}",posId);
		
		if(keyword != null && !keyword.trim().isEmpty()) {
			params.put("searchType", searchType);
			params.put("keyword", keyword);
		}
		
		notiList = service.notiList(params);
				
		if(notiList != null && !notiList.isEmpty()) {
			return ResponseEntity.ok(notiList);
		}else {
			return ResponseEntity.noContent().build();
		}
	}

	// 공지 등록(첨부파일)
	@PostMapping("/notice/insert")
	public ResponseEntity<Void> noticeInsert(
			@AuthenticationPrincipal UserDeptcode auth,
			@RequestBody NoticeInsert insertParams,
			@RequestParam(value = "files", required=false) List<MultipartFile> files
			){
		//NOTICE 테이블에 저장하고 NOTI ID 갖고 오기
		insertParams.setUserNo(auth.getUserNo());
		log.info("insert Params : {}",insertParams);
		
		// 파일 똥코드
		List<customFile> notiFile = new ArrayList<>();
		if(insertParams.getFiles().size() > 0) {
			notiFile = fileService.setOrChangeImage(files, insertParams.getNotiId(), "N");
			log.info("notiFile : {}",notiFile);
			insertParams.setFiles(notiFile);
		}
		
		int result = service.notiInsert(insertParams);
		
		if(result > 0) {
			return ResponseEntity.created(URI.create("/notice/insert")).build();
		}else {
			return ResponseEntity.badRequest().build();
		}
	}
	
	// 공지 상세 조회(첨부파일)
	@GetMapping("/notice/detail/{notiId}")
	public ResponseEntity<NoticeDetail> noticeDetail(
			@PathVariable int notiId,
			@RequestParam(value = "files", required=false) List<MultipartFile> files
			){

		NoticeDetail notiDetail = service.notiDetail(notiId);
		notiDetail.setFiles(files);
		
		if(notiDetail != null) {
			if(notiDetail.getParentDepId() == null) {
				notiDetail.setParentDepId(notiDetail.getChildDepId());
				notiDetail.setChildDepId(null);
			}
			return ResponseEntity.ok(notiDetail);
		}else {
			return ResponseEntity.notFound().build();
		}
	}
	
	// 공지 수정(첨부파일)
	@PatchMapping("/notice/update/{notiId}")
	public ResponseEntity<Void> noticeUpdate(
			@PathVariable int notiId,
			@AuthenticationPrincipal UserDeptcode auth,
			@RequestBody NoticeInsert insertParams
			){
		long userNo = auth.getUserNo();
		
		Map<String,Object> params = new HashMap<>();
		params.put("notiId", notiId);
		params.put("userNo", userNo);
		params.put("insertParams", insertParams);
		
		log.info("insertParams : {}",insertParams);
		
		int result = service.notiUpdate(params);
		
		if(result > 0) {
			return ResponseEntity.noContent().build();
		}else {
			return ResponseEntity.notFound().build();
		}
	}
	
	// 공지 삭제
	@DeleteMapping("/notice/detail/{notiId}")
	public ResponseEntity<Void> noticeDelete(
			@PathVariable int notiId,
			@AuthenticationPrincipal UserDeptcode auth
			){
		long userNo = auth.getUserNo();
		Map<String,Object> params = new HashMap<>();
		params.put("notiId", notiId);
		params.put("userNo", userNo);
		
		int result = service.notiDelete(params);
		
		if(result > 0) {
			return ResponseEntity.noContent().build();
		}else {
			return ResponseEntity.notFound().build();
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
}
