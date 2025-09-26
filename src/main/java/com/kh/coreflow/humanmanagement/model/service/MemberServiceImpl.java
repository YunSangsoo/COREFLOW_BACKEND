package com.kh.coreflow.humanmanagement.model.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kh.coreflow.common.model.service.FileService;
import com.kh.coreflow.common.model.vo.FileDto.customFile;
import com.kh.coreflow.humanmanagement.model.dao.MemberDao;
import com.kh.coreflow.humanmanagement.model.dto.MemberDto.Department;
import com.kh.coreflow.humanmanagement.model.dto.MemberDto.DepartmentLite;
import com.kh.coreflow.humanmanagement.model.dto.MemberDto.MemberLite;
import com.kh.coreflow.humanmanagement.model.dto.MemberDto.MemberPatch;
import com.kh.coreflow.humanmanagement.model.dto.MemberDto.MemberPost;
import com.kh.coreflow.humanmanagement.model.dto.MemberDto.MemberResponse;
import com.kh.coreflow.humanmanagement.model.dto.MemberDto.Position;
import com.kh.coreflow.mail.service.MailService;
import com.kh.coreflow.model.dao.AuthDao;
import com.kh.coreflow.model.dto.UserDto.User;
import com.kh.coreflow.model.dto.UserDto.UserAuthority;
import com.kh.coreflow.security.factory.UserFactory;
import com.kh.coreflow.validator.UserValidator;
import com.kh.coreflow.validator.ValidationResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService{
	private final MemberDao dao;
	private final AuthDao authDao;
	private final PasswordEncoder encoder;
	private final UserFactory userFactory;
	private final MailService mailService;
	private final FileService fileService;
	private final UserValidator validator;
	
	@Override
	public List<Department> deptList() {
		return dao.deptList();
	}

	@Override
	public List<Department> deptDetailList(int parentId) {
		return dao.deptDetailList(parentId);
	}

	@Override
	public List<Position> posiList() {
		return dao.posiList();
	}
	
	@Override
	public List<MemberResponse> memberList(Map<String, String> searchParams) {
		return dao.memberList(searchParams);
	}

	@Override
	public MemberResponse memberDetail(Long userNo) {
		return dao.memberDetail(userNo);
	}

	@Override
	public int memberInsert(MemberPost member, MultipartFile image) {
		// 유효성 검사
		ValidationResult validation = validator.validateStep1(member);
		if(!validation.isValid()) {
			throw new IllegalArgumentException(validation.getMessage());
		}
		User user = userFactory.createIncompleteUser(member);
		
		String tempPwd = user.getUserPwd();
		user.setUserPwd(encoder.encode(tempPwd));
		List<String> roles = getAuth(user.getDepId());
		user.setRoles(roles);
		
		//DB MEMBER에 저장하면서 자동으로 userNo생성
		int result = dao.memberInsert(user);
		user.setUserNo(authDao.findUserNoByEmail(user.getEmail()));
		
		UserAuthority auth = new UserAuthority();
		auth.setUserNo(user.getUserNo());
		auth.setRoles(roles);
		authDao.insertUserRole(auth);
		
		customFile profile = fileService.setOrChangeOneImage(image, user.getUserNo(), "P");
		user.setProfile(profile);
		
		mailService.sendTemporaryPassword(user, tempPwd);
		
		return result;
	}
	
	private List<String> getAuth(Long depId){
		List<String> roles = new ArrayList<String>();
		roles.add("ROLE_USER");
		if(depId == 3 || depId == 4 || depId == 5) {
			roles.add("ROLE_HR");
		}
		return roles;
	}
	
	@Override
	@Transactional
	public int memberUpdate(MemberPatch member, MultipartFile file) {
		int answer = dao.memberUpdate(member);
		if(file!=null) {
			fileService.setOrChangeOneImage(file, member.getUserNo(), "P");
		}
		return answer;
	}

	@Override
	public int memberDelete(Long userNo) {
		return dao.memberDelete(userNo);
	}

	@Override
	public List<MemberLite> search(String query, Integer limit, Long depId) {
        return dao.searchMembers(query, limit, depId);
	}

	@Override
	public List<DepartmentLite> findAll() {
        return dao.findAll();
	}
}