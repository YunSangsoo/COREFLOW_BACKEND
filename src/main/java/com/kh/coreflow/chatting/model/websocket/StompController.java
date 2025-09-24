package com.kh.coreflow.chatting.model.websocket;

import java.util.List;
import java.util.Set;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import java.util.stream.Collectors;

import com.kh.coreflow.chatting.model.dto.ChattingDto.chatMessages;
import com.kh.coreflow.chatting.model.dto.ChattingDto.chatRooms;
import com.kh.coreflow.chatting.model.service.ChattingService;
import com.kh.coreflow.common.model.service.FileService;
import com.kh.coreflow.common.model.vo.FileDto.customFile;
import com.kh.coreflow.model.dto.UserDto.User;
import com.kh.coreflow.model.dto.UserDto.UserDeptPoscode;
import com.kh.coreflow.security.model.provider.JWTProvider;
import com.kh.coreflow.security.model.service.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@RequiredArgsConstructor
public class StompController {
	
	private final ChattingService service;
	private final JWTProvider jwtProvider;
    private final SimpMessagingTemplate messagingTemplate;
    private final FileService fileService;
    private final SimpUserRegistry userRegistry;
    private final AuthService authService;

    //들어오는 메시지를 처리
    @MessageMapping("/chat/enter/{roomNo}")
    //결과를 /topic/room/{roomNo}를 구독하는 모든 클라이언트에게 전송
    @SendTo("/topic/room/{roomNo}")
    public chatMessages handleEnter(
            @DestinationVariable int roomNo,
            chatMessages message // @Payload 생략 가능
    ) {
        message.setType(chatMessages.MessageType.ENTER);
        message.setMessageText(message.getUserName() + "님이 입장하셨습니다.");
        // ChatMessage 객체를 반환하면 @SendTo 경로로 자동 브로드캐스팅됩니다.
        return message;
    }

    @MessageMapping("/chat/exit/{roomNo}")
    @SendTo("/topic/room/{roomNo}")
    public chatMessages handleExit(
            @DestinationVariable int roomNo,
            chatMessages message
    ) {
        //service.exitChatRoom(message); // 서비스 로직 호출

        message.setType(chatMessages.MessageType.EXIT);
        message.setMessageText(message.getUserName() + "님이 퇴장하셨습니다.");

        return message;
    }
    
    //메시지 전송을 위한 컨트롤러
    @MessageMapping("/chat/message/{roomId}")
    //@SendTo("/topic/room/{roomId}")
    public chatMessages handleMessage(
            @DestinationVariable Long roomId,
            Authentication auth,
            chatMessages message
    ) {
    	Long userNo = ((UserDeptPoscode)auth.getPrincipal()).getUserNo();
    	message.setUserNo(userNo);
    	User user = authService.findUserByUserNo(userNo).get();
    	message.setUserName(user.getUserName());
    	int result = service.insertMessage(message);
    	if(result>0) {
	    	String roomTopic = "/topic/room/" + roomId;
	        Set<String> activeSubscribers = userRegistry.findSubscriptions(s -> s.getDestination().equals(roomTopic))
	                .stream()
	                .map(s -> s.getSession().getUser().getName())
	                .collect(Collectors.toSet());
	        
	        // 3. 기존처럼 해당 채팅방을 구독 중인 모두에게 메시지를 보내기
	    	//messagingTemplate.convertAndSend("/topic/room/" + roomId, message);
	        messagingTemplate.convertAndSend(roomTopic, message);
	        List<Long> participantUserNos = service.getParticipantUserNos(roomId);
            for (Long participantUserNo : participantUserNos) {
                // service.getUpdatedChatRoomInfo()는 마지막 메시지가 포함된 최신 ChatRooms 객체를 반환하는 메소드 (구현 필요)
                // 4. 현재 참여자가 '토픽 구독자'가 아닌 경우에만 (즉, 채팅방을 보고 있지 않은 경우)
                //    개인 알림을 보내 채팅방 목록을 업데이트 해줍니다.
                if (!activeSubscribers.contains(String.valueOf(participantUserNo))) {
                    chatRooms updatedRoomInfo = service.getUpdatedChatRoomInfo(participantUserNo, roomId, message);
                    // "/queue/user/{userNo}" 형태의 개인 채널로 메시지를 보냅니다.
                    messagingTemplate.convertAndSendToUser(
                        String.valueOf(participantUserNo), // Spring Security Principal 이름 (보통 String)
                        "/queue/updates", // 개인 알림을 받을 구독 주소
                        updatedRoomInfo   // 최신화된 채팅방 정보 객체
                    );
                }
            }
    	}
    	if(result >0)
    		return message;
    	else {
    		return null;
    	}
    }
    

    @MessageMapping("/chat/file/{roomId}")
    public chatMessages handleFile(
            @DestinationVariable Long roomId,
            Authentication auth,
            chatMessages message
    ) {
    	Long userNo = ((UserDeptPoscode)auth.getPrincipal()).getUserNo();
    	message.setUserNo(userNo);
    	User user = authService.findUserByUserNo(userNo).get();
    	message.setUserName(user.getUserName());
    	customFile file = fileService.findFile("CM",message.getMessageText());
    	message.setMessageText(file.getOriginName());
    	message.setIsFile("T");
    	message.setType(chatMessages.MessageType.FILE);
    	int result = service.changeMessage(message);
    	message.setFile(file);

        // 3. 기존처럼 해당 채팅방을 구독 중인 모두에게 메시지를 보내기
    	messagingTemplate.convertAndSend("/topic/room/" + roomId, message);
    	
    	List<Long> participantUserNos = service.getParticipantUserNos(roomId);
    	
    	for (Long participantUserNo : participantUserNos) {
            // service.getUpdatedChatRoomInfo()는 마지막 메시지가 포함된 최신 ChatRooms 객체를 반환하는 메소드 (구현 필요)
            chatRooms updatedRoomInfo = service.getUpdatedChatRoomInfo(participantUserNo, roomId, message);
            
            // "/queue/user/{userNo}" 형태의 개인 채널로 메시지를 보냅니다.
            messagingTemplate.convertAndSendToUser(
                String.valueOf(participantUserNo), // Spring Security Principal 이름 (보통 String)
                "/queue/updates", // 개인 알림을 받을 구독 주소
                updatedRoomInfo   // 최신화된 채팅방 정보 객체
            );
        }
    	
    	if(result >0)
    		return message;
    	else {
    		return null;
    	}
    }
    
    
    
}