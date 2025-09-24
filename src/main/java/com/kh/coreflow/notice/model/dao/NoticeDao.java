package com.kh.coreflow.notice.model.dao;

import java.util.List;
import java.util.Map;

import com.kh.coreflow.notice.model.dto.NoticeDto.NoticeDetail;
import com.kh.coreflow.notice.model.dto.NoticeDto.NoticeInsert;
import com.kh.coreflow.notice.model.dto.NoticeDto.NoticeResponse;

public interface NoticeDao {

	List<NoticeResponse> notiList();
	
	List<NoticeResponse> notiList(Map<String, Object> params);

	int notiInsert(NoticeInsert insertParams);

	NoticeDetail notiDetail(int notiId);

	int notiUpdate(NoticeInsert insertParams);

	int notiDelete(Map<String, Object> params);
}