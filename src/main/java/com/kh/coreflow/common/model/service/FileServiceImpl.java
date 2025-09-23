package com.kh.coreflow.common.model.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kh.coreflow.common.model.dao.FileDao;
import com.kh.coreflow.common.model.vo.FileDto.customFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceImpl implements FileService {
	@Value("${file.upload-dir}")
    private String uploadDir;
	
	@Autowired
	private FileDao fileDao;

	@Override
	public String saveFile(MultipartFile upfile, String imageCode) {
		
		String projectRootPath = new File("").getAbsolutePath();
		
		String serverFolderPath = Paths.get(projectRootPath, uploadDir, imageCode).toString();
		
	    //String serverFolderPath = uploadDir + imageCode + "/";
	    File dir = new File(serverFolderPath);
	    if(!dir.exists()) {
	        dir.mkdirs();
	    }
	    
	    String originName = upfile.getOriginalFilename();
	    String currentTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
	    int random = (int)(Math.random() *90000 + 10000);
	    
	    String ext = "";
	    if (originName != null && originName.contains(".")) {
	        ext = originName.substring(originName.lastIndexOf("."));
	    }
	    
	    String changeName = currentTime + random + ext;
	    
	    try {
	    	upfile.transferTo(new File(Paths.get(serverFolderPath, changeName).toString()));
	    } catch (IOException e) {
	        log.error("파일 저장 실패: {}", e.getMessage());
	        return null; // 또는 실패 시 null 반환
	    }
	    
	    return changeName; 
	}
	
	@Override
	public boolean deleteFile(String changeName, String imageCode) {
		
	    // 1. 파일명이 유효한지 확인합니다.
	    if (changeName == null || changeName.isEmpty()) {
	        log.warn("삭제할 파일명이 없습니다.");
	        return false;
	    }
	    
		String projectRootPath = new File("").getAbsolutePath();
		
		String fullPath = Paths.get(projectRootPath, uploadDir, imageCode,changeName).toString();
		
	    File fileToDelete = new File(fullPath);

	    // 3. 파일이 실제로 존재하는지 확인하고 삭제를 시도합니다.
	    if (fileToDelete.exists()) {
	        if (fileToDelete.delete()) {
	            log.info("파일 삭제 성공: {}", fullPath);
	            return true; // 파일 삭제 성공
	        } else {
	            log.error("파일 삭제 실패: {}", fullPath);
	            return false; // 파일 삭제 실패
	        }
	    } else {
	        log.warn("삭제할 파일이 존재하지 않습니다: {}", fullPath);
	        return false; // 파일이 존재하지 않음
	    }
	}

	@Override
	public customFile getFile(String imageCode, Long refId) {
		return fileDao.getFile(imageCode, refId);
	}

	@Override
	public List<customFile> getFiles(String imageCode, Long refId) {
		return fileDao.getFiles(imageCode, refId);
	}

	@Override
	@Transactional
	public customFile setOrChangeOneImage(MultipartFile file, Long refId, String fileCode) {
		
		customFile profileImage = new customFile();
		profileImage.setOriginName(file.getOriginalFilename());
		profileImage.setImageCode(fileCode);
		String path = saveFile(file, fileCode);
		profileImage.setChangeName(path);
		profileImage.setImgOrder(1L);
		profileImage.setRefId(refId);
		profileImage.setMimeType(file.getContentType());
		
		customFile existImage = fileDao.getFile(profileImage.getImageCode(), refId);
		if(existImage !=null) {
			profileImage.setImgId(existImage.getImgId());
			deleteFile(existImage.getChangeName(),existImage.getImageCode());
			int answer = fileDao.changeOneImage(profileImage);
			if(answer>0)
				return profileImage;
			else
				return null;
		}
		else {
			int answer = fileDao.insertOneImage(profileImage);
			if(answer > 0 )
				return profileImage;
			else
				return null;
		}
	}

	@Override
	public customFile findFile(String fileCode, String changeName) {
		// TODO Auto-generated method stub
		return fileDao.findFile(fileCode,changeName);
	}
	
	@Override
	@Transactional
	public List<customFile> setOrChangeImage(List<MultipartFile> files,Long refId, String fileCode){
		if(files.size()==0||files==null)
			return null;
		int answer = 0;
		List<customFile> cFiles = new ArrayList<customFile>();
		Long order = 1L;
		for(MultipartFile file : files) {
			customFile cFile = new customFile();
			cFile.setOriginName(file.getOriginalFilename());
			cFile.setImageCode(fileCode);
			String path = saveFile(file, fileCode);
			cFile.setChangeName(path);
			cFile.setImgOrder(order++);
			cFile.setRefId(refId);
			cFile.setMimeType(file.getContentType());
			cFiles.add(cFile);
		}
		List<customFile> existFiles = fileDao.getFiles(fileCode, refId);
		if(existFiles.size()>0) {
			answer=0;
			for(customFile existFile : existFiles) {
				deleteFile(existFile.getChangeName(),existFile.getImageCode());
				answer+=fileDao.removeFile(fileCode,refId);
			}
			if(answer==0)
				return null;
		}
		answer=0;
		log.info("files : {}",cFiles);
		for(customFile file: cFiles ) {
			answer+=fileDao.insertOneImage(file);
		}
		if(answer > 0 )
			return cFiles;
		else
			return null;
	}
}
