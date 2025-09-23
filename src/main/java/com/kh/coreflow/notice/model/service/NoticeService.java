package com.kh.coreflow.notice.model.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.kh.coreflow.notice.model.dto.NoticeDto.NoticeDetail;
import com.kh.coreflow.notice.model.dto.NoticeDto.NoticeInsert;
import com.kh.coreflow.notice.model.dto.NoticeDto.NoticeResponse;

public interface NoticeService {

	List<NoticeResponse> notiList(Map<String, Object> params);

	int notiInsert(NoticeInsert insertParams, List<MultipartFile> files);

	NoticeDetail notiDetail(int notiId);

	int notiUpdate(NoticeInsert insertParams, List<MultipartFile> files);

	int notiDelete(Map<String, Object> params);
}