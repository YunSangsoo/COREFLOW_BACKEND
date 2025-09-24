package com.kh.coreflow.chatting.model.websocket;


import com.kh.coreflow.model.dto.UserDto.UserDeptPoscode;
import com.kh.coreflow.security.model.provider.JWTProvider;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class StompHandler implements ChannelInterceptor {
	
	private final JWTProvider jwtProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor
                .getAccessor(message, StompHeaderAccessor.class);
        

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 2. 헤더에서 토큰 추출 ("Bearer " 제거 포함)
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwtToken = authHeader.substring(7);
                
                
                //3. 토큰 유효성 검증
                if (jwtProvider.validateToken(jwtToken)) {
                    //4. 토큰으로부터 Authentication 객체 생성 (기존 로직과 동일)
                   // Authentication authentication = jwtProvider.getAuthentication(jwtToken);
                	
        			Long userNo = jwtProvider.getUserNo(jwtToken);
        			// 3) 토큰에서 권한 추출
        			List<String> roles = jwtProvider.getRoles(jwtToken); // JWTProvider에서 역할 리스트 반환
        			List<GrantedAuthority> authorities = roles.stream() // 읽어온 권한 리스트를 GrantedAuthority로 변환
        				    .map(SimpleGrantedAuthority::new)
        				    .collect(Collectors.toList());
        			// 4) 권한에서 부서코드 추출
        			Long depId = jwtProvider.getDeptcode(jwtToken);
        			// 5) 권한에서 직위코드 추출
        			Long posId = jwtProvider.getPoscode(jwtToken);
        			
        			UserDeptPoscode principal =  UserDeptPoscode.builder()
        			        .userNo(userNo)
        			        .depId(depId)
        			        .posId(posId)
        			        .build();
        			
        			UsernamePasswordAuthenticationToken authToken // UsernamePasswordAuthenticationToken에 적용
        		    	= new UsernamePasswordAuthenticationToken(principal, null, authorities);
        			SecurityContextHolder.getContext().setAuthentication(authToken);

                    //5. STOMP 세션에 사용자 정보 저장
                    accessor.setUser(authToken);
                } else {
                    // 유효하지 않으면 예외 발생
                    throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
                }
            } else {
                // 토큰이 없으면 예외 발생
                throw new IllegalArgumentException("Authorization 헤더가 없거나 형식이 올바르지 않습니다.");
            }
        }
        return message;
    }
    
}
