package com.kh.coreflow.notice.model.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kh.coreflow.common.model.service.FileService;
import com.kh.coreflow.notice.model.dao.NoticeDao;
import com.kh.coreflow.notice.model.dto.NoticeDto.NoticeDetail;
import com.kh.coreflow.notice.model.dto.NoticeDto.NoticeInsert;
import com.kh.coreflow.notice.model.dto.NoticeDto.NoticeResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService{
	private final NoticeDao dao;
	private final FileService fileService;
	
	@Override
	public List<NoticeResponse> notiList(Map<String, Object> params) {
		return dao.notiList(params);
	}

	@Override
	@Transactional
	public int notiInsert(NoticeInsert insertParams, List<MultipartFile> files) {
		int answer = dao.notiInsert(insertParams);
		fileService.setOrChangeImage(files, insertParams.getNotiId(), "N");
		return answer;
	}

	@Override
	public NoticeDetail notiDetail(int notiId) {
		return dao.notiDetail(notiId);
	}

	@Override
	@Transactional
	public int notiUpdate(NoticeInsert insertParams, List<MultipartFile> files) {
		int answer = dao.notiUpdate(insertParams);
		fileService.setOrChangeImage(files, insertParams.getNotiId(), "N");
		return answer;
	}

	@Override
	public int notiDelete(Map<String, Object> params) {
		return dao.notiDelete(params);
	}
}