package com.kh.coreflow.common.controller;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;

import com.kh.coreflow.common.model.service.FileService;
import com.kh.coreflow.common.model.vo.FileDto.customFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RequestMapping
@RestController
public class FileController {

	@Value("${file.upload-dir}")
    private String uploadDir;
	
	@Autowired
	FileService service;
	
	@GetMapping("/api/download/{imageCode}/{changeName}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String imageCode, 
            @PathVariable String changeName) throws MalformedURLException {
        
        // 1. 파일의 전체 경로로부터 Resource 객체 생성
        String fullPath = Paths.get(uploadDir, imageCode, changeName).toString();
        Resource resource = new UrlResource("file:" + fullPath);

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("파일을 찾을 수 없거나 읽을 수 없습니다.");
        }
        
        // 2. 원본 파일명을 헤더에 담기 위해 DB 조회 등이 필요할 수 있음 (여기서는 changeName 사용)
        customFile file = service.findFile(imageCode, changeName);
        String downloadFilename = file.getOriginName(); // 예시

        // 3. Content-Disposition 헤더 설정
        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(downloadFilename, StandardCharsets.UTF_8)
                .build();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

}
