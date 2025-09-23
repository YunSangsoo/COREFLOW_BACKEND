package com.kh.coreflow.notice.model.dto;

import java.util.Date;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.kh.coreflow.common.model.vo.FileDto.customFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class NoticeDto {
	
	// 공지 조회
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class NoticeResponse{
		public long notiId;
		public String userName;
		public String title;
		public String enrollDate;
		public String essential;
		public String status;
	}
	
	// 공지 검색(제목, 내용, 작성자)
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class NoticeSearch{
		private String searchType;
		private String keyword;
		private int posId;
	}
	
	
	// 공지 등록(첨부파일)
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class NoticeInsert{
		private Long notiId;
		private long userNo;
		private String title;
		private String content;
		private String essential;
		private String endDate;
		private String endTime;
		private Integer depId;
		private Integer posId;
		private List<customFile> files;
	}
	
	// 공지 상세 조회(첨부파일)
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class NoticeDetail{
		private int notiId;
		private String essential;
		private String title;
		private long writer;
		private String userName;
		private String posName;
		private Long depId;
		private Long posId;
		private String enrollDate;
		private String updateDate;
		private String content;
		private Long parentDepId;
		private Long childDepId;
		private String endDate;
		private String endTime;
		private List<MultipartFile> files;
	}
	
	// 공지 삭제
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class NotiDelete{
		private int notiId;
	}
}
