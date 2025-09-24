package com.kh.coreflow.chatting.model.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kh.coreflow.chatting.model.dto.ChattingDto.chatMessages;
import com.kh.coreflow.chatting.model.dto.ChattingDto.chatProfile;
import com.kh.coreflow.chatting.model.dto.ChattingDto.chatProfileDetail;
import com.kh.coreflow.chatting.model.dto.ChattingDto.chatRooms;
import com.kh.coreflow.chatting.model.dto.ChattingDto.userFavorite;
import com.kh.coreflow.model.dto.UserDto.User;

public interface ChattingDao {

	List<chatProfile> getChatProfiles(Long userNo);

	chatProfile getMyProfile(Long userNo);

	List<chatProfile> getFavoriteProfiles(Long userNo);

	int insertFavoriteProfiles(userFavorite favUser);

	int deleteFavoriteProfiles(userFavorite favUser);

	int makeChatRoom(chatRooms newChatRoom);

	int insertMyProfile(chatProfile myProfile);

	int insertMessage(chatMessages message);

	List<chatMessages> getMessages(Long roomId);

	List<chatRooms> getmyChattingRooms(Long userNo);

	List<chatMessages> getLastMessagesForRooms(List<chatRooms> myRooms);

	Long findRoomByMember(List<Long> privateMember, String type);

	int makeChatJoin(Long roomId, List<Long> list);

	User findUserByUserNo(Long userNo);

	chatRooms getRoom(Long userNo,Long roomId);

	List<Long> getParticipantUserNos(Long roomId);

	chatRooms getUpdatedChatRoomInfo(Long userNo, Long roomId, chatMessages message);

	int updateLastReadAt(long roomId, Long userNo);

	int updateState(String status, Long userNo);

	List<chatProfile> findChatProfiles(Long userNo, String query);

	List<chatProfile> getRoomUsers(Long roomId);

	int setJoinUser(Map<String, Object> getParam);

	chatProfileDetail getProfileDetail(Long userNo);

	int changeMessage(chatMessages message);

	int leaveRoom(Long roomId, Long userNo);

	int alarmChange(chatRooms bodyRoom);

}
