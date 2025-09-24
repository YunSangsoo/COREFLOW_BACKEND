package com.kh.coreflow.common.model.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.coreflow.common.model.vo.FileDto.customFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class FileDaoImpl implements FileDao{

	private final SqlSessionTemplate session;
	
	@Override
	public customFile getFile(String imageCode, Long refId) {
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("imageCode", imageCode);
		param.put("refId", refId);
		return session.selectOne("image.getFile",param);
	}
	
	@Override
	public List<customFile> getFiles(String imageCode, Long refId) {
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("imageCode", imageCode);
		param.put("refId", refId);
		return session.selectList("image.getFiles",param);
	}

	@Override
	public int changeOneImage(customFile profileImage) {
		return session.update("image.updateOneImage",profileImage);
	}

	@Override
	public int insertOneImage(customFile profileImage) {
		return session.insert("image.insertOneImage",profileImage);
	}

	@Override
	public customFile findFile(String fileCode, String changeName) {
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("fileCode", fileCode);
		param.put("changeName", changeName);
		return session.selectOne("image.findFile",param);
	}

	@Override
	public int removeFile(String fileCode, Long refId) {
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("fileCode", fileCode);
		param.put("refId", refId);
		return session.delete("image.removeFile",param);
	}
}