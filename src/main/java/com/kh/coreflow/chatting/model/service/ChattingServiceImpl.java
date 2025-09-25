package com.kh.coreflow.chatting.model.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.kh.coreflow.model.dto.UserDto.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.coreflow.chatting.model.dao.ChattingDao;
import com.kh.coreflow.chatting.model.dto.ChattingDto.chatMessages;
import com.kh.coreflow.chatting.model.dto.ChattingDto.chatProfile;
import com.kh.coreflow.chatting.model.dto.ChattingDto.chatProfileDetail;
import com.kh.coreflow.chatting.model.dto.ChattingDto.chatRooms;
import com.kh.coreflow.chatting.model.dto.ChattingDto.userFavorite;
import com.kh.coreflow.model.dao.AuthDao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChattingServiceImpl implements ChattingService {
	
	@Autowired
	private ChattingDao chattingDao;
	
	@Autowired
	private AuthDao authDao;
	
	@Autowired
	private final SimpMessagingTemplate messagingTemplate;
	
	@Override
	public List<chatProfile> getChatProfiles(Long userNo) {
		return chattingDao.getChatProfiles(userNo);
	}

	@Override
	public chatProfile getMyProfile(Long userNo) {
		chatProfile myProfile = chattingDao.getMyProfile(userNo);
		if(myProfile==null) {
			User myInfo = chattingDao.findUserByUserNo(userNo);
			myProfile = new chatProfile().builder()
					.userNo(userNo)
					.userName(myInfo.getUserName())
					.build();
			int result = chattingDao.insertMyProfile(myProfile);
		}
		return chattingDao.getMyProfile(userNo);
	}

	@Override
	public List<chatProfile> getFavoriteProfiles(Long userNo) {
		return chattingDao.getFavoriteProfiles(userNo);
	}

	@Override
	public int insertFavoriteProfiles(userFavorite favUser) {
		return chattingDao.insertFavoriteProfiles(favUser);
	}

	@Override
	public int deleteFavoriteProfiles(userFavorite favUser) {
		return chattingDao.deleteFavoriteProfiles(favUser);
	}
	
	@Override
	@Transactional
	public Long makeChat(Long userNo, Map<String, Object> newChatParam, String type) {
		Long roomId = 0L;
		chatRooms newChatRoom = new chatRooms();
		newChatRoom.setRoomType(type);
		newChatRoom.setUserNo(userNo);
		if(type.equals("PRIVATE")) {
			Long partnerNo = (Long)newChatParam.get("partner");
			User partner = chattingDao.findUserByUserNo(partnerNo);
			newChatRoom.setRoomName(partner.getUserName() + "님과의 채팅");
			newChatParam.remove("partner");
		}else {
			newChatRoom.setRoomName((String)newChatParam.get("roomName"));
		}
		
		int answer = chattingDao.makeChatRoom(newChatRoom);
		if(answer>0) {
			roomId = Long.valueOf(newChatRoom.getRoomId());
			answer = chattingDao.makeChatJoin(roomId,(List<Long>)newChatParam.get("participantUserNos"));
			return roomId;
		}
		return roomId;
	}

	@Override
	public int insertMessage(chatMessages message) {
		return chattingDao.insertMessage(message);
		
	}

	@Override
	public List<chatMessages> getMessages(Long roomId, Long userNo) {
		return chattingDao.getMessages(roomId,userNo);
	}

	@Override
	public List<chatRooms> getmyChattingRooms(Long userNo) {
		List<chatRooms> myRooms = chattingDao.getmyChattingRooms(userNo);

        if (myRooms != null && !myRooms.isEmpty()) {
            List<chatMessages> lastMessages = chattingDao.getLastMessagesForRooms(myRooms);
            Map<Long, chatMessages> lastMessageMap = lastMessages.stream().collect(Collectors.toMap(chatMessages::getRoomId, Function.identity()));

            myRooms.forEach(room -> {
                chatMessages lastMsg = lastMessageMap.get(room.getRoomId());
                if (lastMsg != null) {
                    room.setLastMessage(lastMsg);
                }
            
            });
        }
        
        return myRooms;
	}

	@Override
	@Transactional
	public chatRooms openChat(Long userNo,List<Long> privateMember,String type) {
		Long roomId = chattingDao.findRoomByMember(privateMember,type);
		if(roomId==null)
			return null;
		return chattingDao.getRoom(userNo,roomId);
	}

	@Override
	public chatRooms getRoom(Long userNo,Long roomId) {
		return chattingDao.getRoom(userNo,roomId);
	}

	@Override
	public List<Long> getParticipantUserNos(Long roomId) {
		return chattingDao.getParticipantUserNos(roomId);
	}

	@Override
	public chatRooms getUpdatedChatRoomInfo(Long userNo, Long roomId, chatMessages message) {
		return chattingDao.getUpdatedChatRoomInfo(userNo, roomId,message);
	}

	@Override
	public int updateLastReadAt(long roomId, Long userNo) {
		return chattingDao.updateLastReadAt(roomId,userNo);
	}
	
	@Override
	public int updateState(String status, Long userNo) {
		return chattingDao.updateState(status,userNo);
	}

	@Override
	public List<chatProfile> findChatProfiles(Long userNo, String query) {
		return chattingDao.findChatProfiles(userNo,query);
	}

	@Override
	public List<chatProfile> getRoomUsers(Long roomId) {
		return chattingDao.getRoomUsers(roomId);
	}

	@Override
	public int setJoinUser(Map<String, Object> getParam) {
		return chattingDao.setJoinUser(getParam);
	}

	@Override
	public chatProfileDetail getProfileDetail(Long userNo) {
		return chattingDao.getProfileDetail(userNo);
	}

	@Override
	public int changeMessage(chatMessages message) {
		return chattingDao.changeMessage(message);
	}

	@Override
	public int leaveRoom(Long roomId, Long userNo) {
		return chattingDao.leaveRoom(roomId,userNo);
	}

	@Override
	public int alarmChange(chatRooms bodyRoom) {
		return chattingDao.alarmChange(bodyRoom);
	}
	
	@Transactional
	@Override
	public chatMessages createMissedCallMessage(Long senderNo, Long partnerNo) {
		// 1. 두 사용자 간의 채팅방 ID를 찾습니다.
		List<Long> userList = new ArrayList<Long>();
		userList.add(senderNo);
		userList.add(partnerNo);
		Long roomId = chattingDao.findRoomByMember(userList, "PRIVATE");
		chatProfile senderProfile = chattingDao.getMyProfile(senderNo);
        // 2. DB에 저장할 ChatMessage 객체를 생성합니다.
        chatMessages missedCallMessage = new chatMessages();
        missedCallMessage.setRoomId(roomId);
        missedCallMessage.setUserNo(senderNo);
        missedCallMessage.setUserName(senderProfile.getUserName());
        missedCallMessage.setType(chatMessages.MessageType.VIDEO_CALL_INVITE);
        missedCallMessage.setMessageText("영상통화를 걸었습니다.");
        
        // 3. 메시지를 DB에 저장합니다.
        int result = chattingDao.insertMessage(missedCallMessage);
        
        if (result > 0) {
            // 4. 저장이 성공하면, 해당 채팅방 토픽으로 메시지를 방송합니다.
            messagingTemplate.convertAndSend(
                "/topic/room/" + roomId, 
                missedCallMessage
            );
            return missedCallMessage;
        }
        else
        	return null;
	}

}
