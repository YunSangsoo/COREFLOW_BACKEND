package com.kh.coreflow.chatting.model.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Controller;

import com.kh.coreflow.chatting.model.dto.ChattingDto.SignalMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebRTCController {
	
	// 특정 사용자에게 메시지를 보낼 때 사용하는 Spring WebSocket 메시징 템플릿
    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry userRegistry; // 사용자 접속 상태 확인용

    /**
     * WebRTC 시그널링 메시지를 중개하는 핸들러
     * @MessageMapping("/webrtc/signal")
     * - 클라이언트가 "/app/webrtc/signal" 주소로 메시지를 보내면 이 메서드가 처리합니다.
     */
    @MessageMapping("/webrtc/signal")
    public void sendSignal(@Payload SignalMessage signalMessage) {
        String recipientId = String.valueOf(signalMessage.getTo());
        
        if ("offer".equals(signalMessage.getType())) {
            SimpUser user = userRegistry.getUser(recipientId);
            boolean isReadyForCall = false;
            if (user != null) {
                isReadyForCall = user.getSessions().stream()
                    .flatMap(session -> session.getSubscriptions().stream())
                    .anyMatch(sub -> sub.getDestination() != null && sub.getDestination().endsWith("/queue/webrtc"));
            }

            if (!isReadyForCall) {
                // 오프라인이거나 준비되지 않았다면, 'user-offline' 응답을 보내고 여기서 종료합니다.
                SignalMessage offlineResponse = new SignalMessage("user-offline", signalMessage.getTo(), signalMessage.getFrom(), null);
                messagingTemplate.convertAndSendToUser(
                    String.valueOf(signalMessage.getFrom()),
                    "/queue/webrtc",
                    offlineResponse
                );
                return; // 여기서 함수를 종료하여 아래 전달 로직이 실행되지 않도록 함
            }
        }

        // 2. 'offer'가 정상이거나, 'answer', 'ice' 등 다른 모든 시그널은 상태 체크 없이 바로 상대방에게 전달합니다.
        messagingTemplate.convertAndSendToUser(
            recipientId,
            "/queue/webrtc",
            signalMessage
        );
    }
}
