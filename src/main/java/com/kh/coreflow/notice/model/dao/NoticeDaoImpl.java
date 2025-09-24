package com.kh.coreflow.notice.model.dao;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.coreflow.notice.model.dto.NoticeDto.NoticeDetail;
import com.kh.coreflow.notice.model.dto.NoticeDto.NoticeInsert;
import com.kh.coreflow.notice.model.dto.NoticeDto.NoticeResponse;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NoticeDaoImpl implements NoticeDao{
	private final SqlSessionTemplate session;

	@Override
	public List<NoticeResponse> notiList() {
		return session.selectList("notice.notiList");
	}
	
	@Override
	public List<NoticeResponse> notiList(Map<String, Object> params) {
		return session.selectList("notice.notiList",params);
	}

	@Override
	public int notiInsert(NoticeInsert insertParams) {
		return session.insert("notice.notiInsert",insertParams);
	}

	@Override
	public NoticeDetail notiDetail(int notiId) {
		return session.selectOne("notice.notiDetail",notiId);
	}

	@Override
	public int notiUpdate(NoticeInsert insertParams) {
		return session.update("notice.notiUpdate",insertParams);
	}

	@Override
	public int notiDelete(Map<String, Object> params) {
		return session.delete("notice.notiDelete",params);
	}
}