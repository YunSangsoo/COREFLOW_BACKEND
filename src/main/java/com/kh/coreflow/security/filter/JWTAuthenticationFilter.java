package com.kh.coreflow.security.filter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.kh.coreflow.model.dto.UserDto.UserDeptPoscode;
import com.kh.coreflow.security.model.provider.JWTProvider;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter{

	private final JWTProvider jwt;
	
	// accessToken인증 확인용 필터
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		// 요청 경로를 가져옴
        String requestURI = request.getRequestURI();

        // ✅ 로그인, 회원가입, 토큰 재발급 등의 인증 관련 경로는 필터를 건너뛰도록 설정
        if (requestURI.startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ 웹소켓 연결 경로도 필터를 건너뛰도록 설정
        if (requestURI.startsWith("/ws")) {
            filterChain.doFilter(request, response);
            return;
        }
		
		// 1) 요청 header에서 Authorization 추출
		String header = request.getHeader("Authorization");
		if(header != null && header.startsWith("Bearer ")) {
			try {
			// 2) 토큰에서 userNo추출
			String token = header.substring(7).trim();
			Long userNo = jwt.getUserNo(token);
			// 3) 토큰에서 권한 추출
			List<String> roles = jwt.getRoles(token); // JWTProvider에서 역할 리스트 반환
			List<GrantedAuthority> authorities = roles.stream() // 읽어온 권한 리스트를 GrantedAuthority로 변환
				    .map(SimpleGrantedAuthority::new)
				    .collect(Collectors.toList());
			// 4) 권한에서 부서코드 추출
			Long depId = jwt.getDeptcode(token);
			// 5) 권한에서 직위코드 추출
			Long posId = jwt.getPoscode(token);
			
			UserDeptPoscode principal =  UserDeptPoscode.builder()
			        .userNo(userNo)
			        .depId(depId)
			        .posId(posId)
			        .build();
			
			UsernamePasswordAuthenticationToken authToken // UsernamePasswordAuthenticationToken에 적용
		    	= new UsernamePasswordAuthenticationToken(principal, null, authorities);
			
			// 인증처리 끝
			SecurityContextHolder.getContext().setAuthentication(authToken);
			
			}catch (ExpiredJwtException e) {
				SecurityContextHolder.clearContext();
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED); // 401상태
				return;
			}
		}
		
		filterChain.doFilter(request, response);
	}

}
