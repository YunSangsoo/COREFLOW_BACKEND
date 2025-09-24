package com.kh.coreflow.security.controller;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kh.coreflow.common.model.service.FileService;
import com.kh.coreflow.common.model.vo.FileDto.customFile;
import com.kh.coreflow.model.dto.UserDto.AuthResult;
import com.kh.coreflow.model.dto.UserDto.FindPwdRequest;
import com.kh.coreflow.model.dto.UserDto.LoginRequest;
import com.kh.coreflow.model.dto.UserDto.User;
import com.kh.coreflow.personal.model.service.UserService;
import com.kh.coreflow.security.model.provider.JWTProvider;
import com.kh.coreflow.security.model.service.AuthService;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

	private final AuthService service;
	private final UserService userService;
	private final FileService fileService;
	private final JWTProvider jwt;
	public static final String REFRESH_COOKIE = "REFRESH_TOKEN0";
	
	@Autowired
	private final ServletContext servlet;
	
	
	@PostMapping("/login")
	public ResponseEntity<AuthResult> login(@RequestBody LoginRequest req){
		
		//1) 사용자가 존재하느지 확인
		boolean exists = service.existsByEmail(req.getEmail());
		
		if(!exists) {
			return ResponseEntity.notFound().build();
		}
		try {
			AuthResult result = service.login(req.getEmail(), req.getPassword());
			
			// refreshToken은 http-only쿠키로 설정하여 반환
			ResponseCookie refreshCookie = 
					ResponseCookie
					.from(REFRESH_COOKIE, result.getRefreshToken())
					.httpOnly(true)
					.secure(false) // https에서만 사용하는지 여부
					.path("/")
					.sameSite("Lax")
					.maxAge(Duration.ofDays(7)) // 만료시간
					.build();
			
			return ResponseEntity.ok()
					.header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
					.body(result);
			
		}catch (BadCredentialsException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); //401
		}	
	}
	
	@PostMapping("/refresh")
	public ResponseEntity<AuthResult> refresh(
			HttpServletRequest request,
			@CookieValue(name = REFRESH_COOKIE, required = false)
			String refreshCookie
			){
		// 전체 쿠키 확인
	    if (request.getCookies() != null) {
	        for (var c : request.getCookies()) {
	            //System.out.println("Cookie: " + c.getName() + " = " + c.getValue());
	        }
	    } else {
	        //System.out.println("No cookies received");
	    }
		
		if(refreshCookie == null || refreshCookie.isBlank()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		//쿠키가 있으면 쿠키를 검증하여 새로운 accessToken생성
		AuthResult result = service.refreshByCookie(refreshCookie);
		return ResponseEntity.ok(result);
	}
	
	@PostMapping("/find-pwd")
	public ResponseEntity<?> findUserPwd(@RequestBody FindPwdRequest request){
		boolean result = service.findUserPwd(request.getUserName(), request.getEmail());
		return result ? ResponseEntity.ok("임시 비밀번호가 메일로 발송되었습니다.")
				: ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 계정정보가 존재하지 않습니다");
	}
	
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest request){
		
		// 1. 클라이언트 헤더에서 id값 추출
		String accessToken = resolveAccessToken(request);
		Long userId = jwt.getUserNo(accessToken);
		
		// 리프레쉬토큰 제거
		ResponseCookie refreshCookie = 
				ResponseCookie
				.from(REFRESH_COOKIE, "")
				.httpOnly(true)
				.secure(false) // https에서만 사용하는지 여부
				.path("/")
				.sameSite("Lax")
				.maxAge(0) // 만료시간
				.build();
		return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, refreshCookie.toString()).build();
	}
	
	@GetMapping("/me")
	public ResponseEntity<AuthResult> getUserInfo(HttpServletRequest req){
		
		// 1. 요청 헤더에서 jwt토큰 추출
		String jwtToken = resolveAccessToken(req);
		if(jwtToken == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		// 2. JWT토큰에서 ID값 추출하기
		Long userNo = jwt.getUserNo(jwtToken);
		
		// 사용자 정보 조회
		Optional<User> user = service.findUserByUserNo(userNo);
		if(user.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		
		String refreshToken = resolveRefreshTokenFromCookie(req); // 쿠키에서 읽는 함수
		
		// 옵셔널 설정 해제
		User actualUser = user.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 사용자입니다"));
		actualUser.setUserPwd(null); // 마이페이지에서 password안보이게 처리

	    AuthResult result = AuthResult.builder()
	                                  .accessToken(jwtToken)   // 기존 accessToken 그대로
	                                  .refreshToken(refreshToken) // 있으면 전달
	                                  .user(actualUser)
	                                  .build();
		
		return ResponseEntity.ok(result);
	}
	
	@PutMapping("/{userNo}/phone")
    public ResponseEntity<Void> updatePhone(
    		@PathVariable Long userNo, 
    		@RequestBody Map<String, String> body) {
		userService.updatePhone(userNo, body.get("phone"));
        return ResponseEntity.ok().build();
    }
	
	@PutMapping("/{userNo}/address")
    public ResponseEntity<Void> updateAddress(
    		@PathVariable Long userNo, 
    		@RequestBody Map<String, String> body) {
        userService.updateAddress(userNo, body.get("address"), body.get("addressDetail"));
        return ResponseEntity.ok().build();
    }

    // 비밀번호 수정
    @PutMapping("/{userNo}/password")
    public ResponseEntity<Void> updatePassword(
    		@PathVariable Long userNo, 
    		@RequestBody Map<String, String> body) {
        userService.updatePassword(userNo, body.get("currentPassword"), body.get("newPassword"));
        return ResponseEntity.ok().build();
    }

    // 프로필 이미지 수정
    @PutMapping("/{userNo}/profile")
    public ResponseEntity<?> updateProfile(
    		@PathVariable Long userNo, 
    		@RequestPart("profile") MultipartFile profile) {
    	
    	if(profile != null && !profile.isEmpty()) {
            //String url = userService.updateProfileImage(userNo, profile); 미사용
    		customFile url = fileService.setOrChangeOneImage(profile,userNo,"P");
    		
            Optional<User> userOpt = service.findUserByUserNo(userNo);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                				.body("User not found");
            }
            User user = userOpt.get();
            user.setProfile(url);
            log.info("img:{}",url);
            
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.badRequest().body("No file uploaded");
    }

	private String resolveAccessToken(HttpServletRequest request) {
		String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
	}
	
	private String resolveRefreshTokenFromCookie(HttpServletRequest request) {
	    if (request.getCookies() == null) {
	        return null;
	    }

	    for (Cookie cookie : request.getCookies()) {
	        if ("refreshToken".equals(cookie.getName())) { // 쿠키 이름은 서버에서 설정한 이름과 동일해야 함
	            return cookie.getValue();
	        }
	    }

	    return null; // 쿠키에 refreshToken이 없는 경우
	}
}
