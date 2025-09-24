package com.kh.coreflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.kh.coreflow.chatting.model.websocket.StompHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker // STOMP 메세징 활성화
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer{

    private final StompHandler stompHandler; // 인터셉터 주입
    
	@Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 WebSocket 연결을 시작할 엔드포인트를 지정
        // SockJS는 WebSocket을 지원하지 않는 브라우저를 위한 대체 옵션
        registry.addEndpoint("/ws").setAllowedOriginPatterns("http://localhost:5173").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // "/topic", "/queue"로 시작하는 목적지를 가진 메시지를 브로커에게 라우팅
        // 메시지를 구독 중인 클라이언트에게 전달하는 역할
        registry.enableSimpleBroker("/topic", "/queue");

        // "/app"으로 시작하는 목적지를 가진 메시지를 @MessageMapping 어노테이션이 붙은 메소드로 라우팅
        // 클라이언트가 서버로 메시지를 보낼 때 사용하는 접두사
        registry.setApplicationDestinationPrefixes("/app");
        
        registry.setUserDestinationPrefix("/user");
    }
    
    // 클라이언트의 인바운드 메시지를 처리하는 채널에 인터셉터를 등록합니다.
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompHandler);
    }
}
