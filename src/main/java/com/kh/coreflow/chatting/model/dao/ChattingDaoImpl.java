package com.kh.coreflow.chatting.model.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.coreflow.chatting.model.dto.ChattingDto.chatMessages;
import com.kh.coreflow.chatting.model.dto.ChattingDto.chatProfile;
import com.kh.coreflow.chatting.model.dto.ChattingDto.chatProfileDetail;
import com.kh.coreflow.chatting.model.dto.ChattingDto.chatRooms;
import com.kh.coreflow.chatting.model.dto.ChattingDto.userFavorite;
import com.kh.coreflow.model.dto.UserDto.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ChattingDaoImpl implements ChattingDao {
	
	private final SqlSessionTemplate session;
	
	@Override
	public List<chatProfile> getChatProfiles(Long userNo) {
		return session.selectList("chat.getChatProfiles",userNo);
	}

	@Override
	public chatProfile getMyProfile(Long userNo) {
		return session.selectOne("chat.getMyProfile",userNo);
	}

	@Override
	public int insertMyProfile(chatProfile myProfile) {
		return session.insert("chat.insertMyProfile",myProfile);
	}

	@Override
	public List<chatProfile> getFavoriteProfiles(Long userNo) {
		return session.selectList("chat.getFavoriteProfiles",userNo);
	}

	@Override
	public int insertFavoriteProfiles(userFavorite favUser) {
		return session.insert("chat.insertFavoriteProfiles",favUser);
	}

	@Override
	public int deleteFavoriteProfiles(userFavorite favUser) {
		return session.delete("chat.deleteFavoriteProfiles",favUser);
	}
	
	@Override
	public int makeChatRoom(chatRooms newChatRoom) {
		return session.insert("chat.makeChatRoom",newChatRoom);
	}

	@Override
	public int insertMessage(chatMessages message) {
		return session.insert("chat.insertMessage",message);
	}

	@Override
	public List<chatMessages> getMessages(Long roomId, Long userNo) {
		Map<String, Object> params = new HashMap<>();
	    
	    params.put("roomId", roomId);
	    params.put("userNo", userNo);
	    
		return session.selectList("chat.getMessages",params);
	}

	@Override
	public List<chatRooms> getmyChattingRooms(Long userNo) {
		return session.selectList("chat.getMyChattingRooms",userNo);
	}

	@Override
	public List<chatMessages> getLastMessagesForRooms(List<chatRooms> myRooms) {
		return session.selectList("chat.selectLastMessagesForRooms",myRooms);
	}

	@Override
	public Long findRoomByMember(List<Long> privateMember, String type) {
		Map<String, Object> params = new HashMap<>();
	    
	    params.put("userNos", privateMember);
	    params.put("userCount", privateMember.size());
	    params.put("type", type);
	    
	    return session.selectOne("chat.findRoomByMember", params);
	}

	@Override
	public int makeChatJoin(Long roomId, List<Long> chatRoomJoin) {
		Map<String, Object> params = new HashMap<>();
	    params.put("roomId", roomId);
	    params.put("userNos", chatRoomJoin);
	    return session.insert("chat.insertChatRoomJoins", params);
	}

	@Override
	public User findUserByUserNo(Long userNo) {
		return session.selectOne("chat.findUserByUserNo",userNo);
	}

	@Override
	public chatRooms getRoom(Long userNo,Long roomId) {
		chatRooms result = session.selectOne("chat.getRoom",roomId);
		List<chatProfile> partners = getRoomUsers(roomId);
		result.setPartner(partners.stream().filter(partner->(userNo!=partner.getUserNo())).collect(Collectors.toList()));
		return result;
	}

	@Override
	public List<Long> getParticipantUserNos(Long roomId) {
		return session.selectList("chat.getParticipantUserNos",roomId);
	}

	@Override
	public chatRooms getUpdatedChatRoomInfo(Long userNo,Long roomId, chatMessages message) {
	    chatRooms returnRoomInfo = session.selectOne("chat.getRoom",roomId);
	    int unReadCount = session.selectOne("chat.getCountMessageSubscribe",userNo);
	    returnRoomInfo.setLastMessage(message);
	    returnRoomInfo.setUnreadCount(unReadCount);
		return returnRoomInfo;
	}

	@Override
	public int updateLastReadAt(long roomId, Long userNo) {
		Map<String, Object> params = new HashMap<>();
	    params.put("roomId", roomId);
	    params.put("userNo", userNo);
		return session.update("chat.updateLastReadAt",params);
	}

	@Override
	public int updateState(String status, Long userNo) {
		String statusCode = session.selectOne("chat.findState",status);
		Map<String, Object> params = new HashMap<>();
	    params.put("status", statusCode);
	    params.put("userNo", userNo);
	    int answer = session.update("chat.updateState",params);
	    return answer;
	}

	@Override
	public List<chatProfile> findChatProfiles(Long userNo, String query) {
		Map<String, Object> params = new HashMap<>();
	    params.put("userNo", userNo);
	    params.put("query", query);
		return session.selectList("chat.findChatProfiles",params);
	}

	@Override
	public List<chatProfile> getRoomUsers(Long roomId) {
		return session.selectList("chat.getRoomUsers",roomId);
	}

	@Override
	public int setJoinUser(Map<String, Object> getParam) {
		return session.insert("chat.setJoinUser",getParam);
	}

	@Override
	public chatProfileDetail getProfileDetail(Long userNo) {
		return session.selectOne("chat.getProfileDetail",userNo);
	}

	@Override
	public int changeMessage(chatMessages message) {
		return session.update("chat.changeMessage",message);
	}

	@Override
	public int leaveRoom(Long roomId, Long userNo) {
		Map<String, Object> params = new HashMap<>();
	    params.put("roomId", roomId);
	    params.put("userNo", userNo);
		return session.delete("chat.leaveRoom",params);
	}

	@Override
	public int alarmChange(chatRooms bodyRoom) {
		return session.update("chat.alarmChange",bodyRoom);
	}

}
