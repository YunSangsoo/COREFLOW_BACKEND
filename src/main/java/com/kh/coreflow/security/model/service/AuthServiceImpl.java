package com.kh.coreflow.security.model.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.coreflow.common.model.service.FileService;
import com.kh.coreflow.mail.service.MailService;
import com.kh.coreflow.model.dao.AuthDao;
import com.kh.coreflow.model.dto.UserDto.AuthResult;
import com.kh.coreflow.model.dto.UserDto.User;
import com.kh.coreflow.model.dto.UserDto.UserAuthority;
import com.kh.coreflow.security.model.provider.JWTProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService{
	
	private final AuthDao authDao;
	private final MailService mailService;
	private final PasswordEncoder encoder;
	private final FileService fileService;
	private final JWTProvider jwt;
	
	@Override
	public boolean existsByEmail(String email) {
		return authDao.findUserByEmail(email).isPresent();
	}

	@Override
	public AuthResult login(String email, String userPwd) {
		// 1. 사용자 정보 조회
				User user = authDao.findUserByEmail(email)
						.orElseThrow(() -> new IllegalArgumentException("등록되지 않은 이메일입니다"));;
				
				if(!encoder.matches(userPwd, user.getUserPwd())) {
					throw new BadCredentialsException("비밀번호 오류");
				}
				
				Long userNo = user.getUserNo();
				UserAuthority userAuth = authDao.findUserAuthorityByUserNo(userNo);
				if (userAuth == null) {
				    throw new IllegalArgumentException("권한 정보 없음");
				}
				log.info("권한 조회: userNo={} roles={} depId={}", userNo, userAuth.getRoles(), user.getDepId());
				Long depId = user.getDepId();
				Long posId = user.getPosId();
				
				// 2. 토큰 발급
				String accessToken = jwt.createAccessToken(userNo, depId, posId, userAuth.getRoles(), 30); // 30분
				String refreshToken = jwt.createRefreshToken(user.getUserNo(), 7); // 7일
				
				User userNoPassword = User.builder()
										.userNo(user.getUserNo())
										.email(user.getEmail())
										.userName(user.getUserName())
										.profile(user.getProfile())
										.roles(userAuth.getRoles())
										.depId(user.getDepId())
										.build();
				
				return AuthResult.builder()
						.accessToken(accessToken)
						.refreshToken(refreshToken)
						.user(userNoPassword)
						.build();
	}

	@Override
	@Transactional
	public AuthResult signUp(String email, String userPwd) {
		// 1) Users테이블에 데이터 추가
		User user = User.builder()
						.email(email)
						.userName(email.split("@")[0])
						.userPwd(encoder.encode(userPwd))
						.build();
		
		authDao.insertUser(user);
				
		// 3) 권한추가
		UserAuthority auth = UserAuthority.builder()
											.userNo(user.getUserNo())
											.roles(List.of("ROLE_USER"))
											.build();
		authDao.insertUserRole(auth);
		
		Long userNo = user.getUserNo();
		UserAuthority userAuth = authDao.findUserAuthorityByUserNo(userNo);
		if (userAuth == null) {
		    throw new IllegalArgumentException("권한 정보 없음");
		}
		Long depId = user.getDepId();
		Long posId = user.getPosId();
		
		//토큰 발급
		String accessToken = jwt.createAccessToken(userNo, depId, posId, userAuth.getRoles(), 30); // 30분
		String refreshToken = jwt.createRefreshToken(user.getUserNo(), 7); // 7일
				
		user = authDao.findUserByUserNo(user.getUserNo())	// 비밀번호 제외 필요
				.orElseThrow(() -> new RuntimeException("가입 후 사용자 정보를 조회할 수 없습니다"));
				
		return AuthResult.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.user(user)
				.build();
	}

	@Override
	public AuthResult refreshByCookie(String refreshCookie) {
		Long userNo = jwt.parseRefresh(refreshCookie);
		
		User user = authDao.findUserByUserNo(userNo)
				.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다"));
		UserAuthority userAuth = authDao.findUserAuthorityByUserNo(userNo);
		if (userAuth == null) {
			throw new IllegalArgumentException("권한 정보 없음");
		}
		Long depId = user.getDepId();
		Long posId = user.getPosId();

		// 2. 토큰 발급
		String accessToken = jwt.createAccessToken(userNo, depId, posId, userAuth.getRoles(), 30); // 30분			
		
		User userNoPassword = User.builder()
								.userNo(user.getUserNo())
								.email(user.getEmail())
								.userName(user.getUserName())
								.profile(user.getProfile())
								.roles(userAuth.getRoles())
								.depId(user.getDepId())
								.build();

		return AuthResult.builder()
				.accessToken(accessToken)
				.refreshToken(refreshCookie)
				.user(userNoPassword)
				.build();
	}

	@Override
	public Optional<User> findUserByUserNo(Long userNo) {
		Optional<User> userOpt = authDao.findUserByUserNo(userNo);
		if(userOpt.isPresent()) {
		    User user = userOpt.get();
		    UserAuthority roles = authDao.findUserAuthorityByUserNo(userNo);
		    user.setRoles(roles.getRoles());
		}
		return userOpt;
	}

	@Override
	public boolean findUserPwd(String userName, String email) {
		User user = authDao.findUserPwd(userName, email);
		if(user == null) {
			return false;
		}
		
		String tempPwd = createTempPwd();
		String encodedPwd = encoder.encode(tempPwd);
		authDao.updatePwd(user.getEmail(), encodedPwd);
		mailService.sendTempPwd(user, tempPwd);
		
		return true;
	}

	@Override
	public String createTempPwd() {
		return UUID.randomUUID().toString().replace("-", "").substring(0,10);
	}

	@Override
	public Optional<User> findUserByEmail(String email) {
		return authDao.findUserByEmail(email);
	}
	
	

}
