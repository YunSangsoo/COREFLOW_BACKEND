package com.kh.coreflow.chatting.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kh.coreflow.chatting.model.dto.ChattingDto.MissedCallRequest;
import com.kh.coreflow.chatting.model.dto.ChattingDto.chatMessages;
import com.kh.coreflow.chatting.model.dto.ChattingDto.chatProfile;
import com.kh.coreflow.chatting.model.dto.ChattingDto.chatProfileDetail;
import com.kh.coreflow.chatting.model.dto.ChattingDto.chatRooms;
import com.kh.coreflow.chatting.model.dto.ChattingDto.userFavorite;
import com.kh.coreflow.chatting.model.service.ChattingService;
import com.kh.coreflow.common.model.service.FileService;
import com.kh.coreflow.common.model.vo.FileDto.customFile;
import com.kh.coreflow.model.dto.UserDto.UserDeptPoscode;
import com.kh.coreflow.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/chatting")
@RestController
public class ChattingController {
	
	@Autowired
	ChattingService chattingService;
	
	@Autowired
	FileService fileService;
	
	@GetMapping("myProfile")
	public ResponseEntity<chatProfile> myProfile(
			@AuthenticationPrincipal UserDeptPoscode user,
			@AuthenticationPrincipal CustomUserDetails userDetails
			){
		chatProfile prof = chattingService.getMyProfile(user.getUserNo());
		
		if(prof!=null) {
			customFile profile = fileService.getFile("CP",prof.getUserNo());
			if(profile==null) {
				customFile tempProfile = new customFile();
				tempProfile.setChangeName("CHAT_PROFILE_DEFAULT.jpg");
				tempProfile.setImageCode("CP");
				tempProfile.setOriginName("CHAT_PROFILE_DEFAULT.jpg");
				profile=tempProfile;
			}
			prof.setProfile(profile);
			return ResponseEntity.ok(prof);
		}else {
			return ResponseEntity.badRequest().build();
		}
	}
	
	@GetMapping("/user")
	public ResponseEntity<List<chatProfile>> chatUser(
			@AuthenticationPrincipal UserDeptPoscode user
			){
		List<chatProfile> list = chattingService.getChatProfiles(user.getUserNo());
		for(chatProfile el : list) {
			customFile profile = fileService.getFile("CP",el.getUserNo());
			if(profile==null) {
				customFile tempProfile = new customFile();
				tempProfile.setChangeName("CHAT_PROFILE_DEFAULT.jpg");
				tempProfile.setImageCode("CP");
				tempProfile.setOriginName("CHAT_PROFILE_DEFAULT.jpg");
				profile=tempProfile;
			}
			el.setProfile(profile);
		}
		
		return ResponseEntity.ok(list);
	}

	@GetMapping("/searchUser")
	public ResponseEntity<List<chatProfile>> searchUser(
			@AuthenticationPrincipal UserDeptPoscode user,
			@RequestParam(name = "query") String query
			){
		List<chatProfile> list = chattingService.findChatProfiles(user.getUserNo(),query);
		if(list!=null) {
			for(chatProfile el : list) {
				customFile profile = fileService.getFile("CP",el.getUserNo());
				if(profile==null) {
					customFile tempProfile = new customFile();
					tempProfile.setChangeName("CHAT_PROFILE_DEFAULT.jpg");
					tempProfile.setImageCode("CP");
					tempProfile.setOriginName("CHAT_PROFILE_DEFAULT.jpg");
					profile=tempProfile;
				}
				el.setProfile(profile);
			}
			return ResponseEntity.ok(list);
		}else {
			return ResponseEntity.badRequest().build();
		}
	}
	
	@GetMapping("/favorites")
	public ResponseEntity<List<chatProfile>> favoriteUser(
			@AuthenticationPrincipal UserDeptPoscode user
			){
		List<chatProfile> list = chattingService.getFavoriteProfiles(user.getUserNo());
		
		for(chatProfile el : list) {
			customFile profile = fileService.getFile("CP",el.getUserNo());
			if(profile==null) {
				customFile tempProfile = new customFile();
				tempProfile.setChangeName("CHAT_PROFILE_DEFAULT.jpg");
				tempProfile.setImageCode("CP");
				tempProfile.setOriginName("CHAT_PROFILE_DEFAULT.jpg");
				profile=tempProfile;
			}
			el.setProfile(profile);
		}
		
		return ResponseEntity.ok(list);
	}
	
	
	@PostMapping("/favorites")
	public ResponseEntity<Void> insertFavorite(
			@AuthenticationPrincipal UserDeptPoscode user,
			@RequestBody userFavorite favUser
			){
		favUser.setUserNo(user.getUserNo());
		int result = chattingService.insertFavoriteProfiles(favUser);
		if(result>0)
			return ResponseEntity.ok().build();
		else
			return ResponseEntity.badRequest().build();
	}

	@DeleteMapping("/favorites/{favUserNo}")
	public ResponseEntity<Void> deleteFavorite(
			@AuthenticationPrincipal UserDeptPoscode user,
			@PathVariable("favUserNo") Long favUserNo
			){
		userFavorite favUser = new userFavorite();
		favUser.setUserNo(user.getUserNo());
		favUser.setFavoriteUserNo(favUserNo);
		int result = chattingService.deleteFavoriteProfiles(favUser);
		if(result>0)
			return ResponseEntity.ok().build();
		else
			return ResponseEntity.badRequest().build();
	}
	
	@GetMapping("/private/{userNo}")
	public ResponseEntity<chatRooms> openPrivateChat(
			@AuthenticationPrincipal UserDeptPoscode user,
			@PathVariable("userNo") Long partnerUserNo
			){
		
		HashMap<String,Object> mappingParameter = new HashMap<String,Object>();
		List<Long> privateMember = new ArrayList<Long>();
		privateMember.add(user.getUserNo());
		privateMember.add(partnerUserNo);
		
		
		mappingParameter.put("participantUserNos",privateMember);
		mappingParameter.put("partner", partnerUserNo);
		chatRooms resultRoom = chattingService.openChat(user.getUserNo(),privateMember,"PRIVATE");
		if(resultRoom == null) {
			Long answer = chattingService.makeChat(user.getUserNo(),mappingParameter,"PRIVATE");
			resultRoom = chattingService.openChat(user.getUserNo(),privateMember,"PRIVATE");
			if(resultRoom==null) {
				return ResponseEntity.badRequest().build();
			}
		}
		return ResponseEntity.ok(resultRoom);
	}
	
	@GetMapping("/room/{roomId}/messages")
	public ResponseEntity<List<chatMessages>> getMessages(
			@PathVariable("roomId") Long roomId,
			@AuthenticationPrincipal UserDeptPoscode user
			){
		List<chatMessages> list = chattingService.getMessages(roomId,user.getUserNo());
		if(list!=null) {
			for(chatMessages el : list) {
				if(el.getType()==chatMessages.MessageType.FILE) {
					customFile file = fileService.getFile("CM",el.getMessageId());
					el.setFile(file);
				}
			}
			return ResponseEntity.ok(list);
		}else {
			return ResponseEntity.badRequest().build();
		}
	}
	
	
	@GetMapping("/myChattingRooms")
	public ResponseEntity<List<chatRooms>> myChattingRooms(
			@AuthenticationPrincipal UserDeptPoscode user
			){
		List<chatRooms> list = chattingService.getmyChattingRooms(user.getUserNo());
		return ResponseEntity.ok(list);
	}
	
	@PostMapping("/public")
	public ResponseEntity<chatRooms> MakePublicRoom(
			@AuthenticationPrincipal UserDeptPoscode user,
			@RequestBody Map<String,Object> newChatParam
			){
		Long roomId = chattingService.makeChat(user.getUserNo(),newChatParam,"PUBLIC");
		chatRooms returnRoom = chattingService.getRoom(user.getUserNo(),roomId);
		return ResponseEntity.ok(returnRoom);
	}
	
	@GetMapping("/room/{roomId}")
	public ResponseEntity<chatRooms> getChatRoom(
			@PathVariable("roomId") Long roomId,
			@AuthenticationPrincipal UserDeptPoscode user
			){
		chatRooms getRoom = chattingService.getRoom(user.getUserNo(),roomId);
		return ResponseEntity.ok(getRoom);
	}
	
	@PostMapping("/room/{roomId}/read")
	public ResponseEntity<?> updateLastReadAt(
	        @PathVariable("roomId") Long roomId,
	        @AuthenticationPrincipal UserDeptPoscode user
	) {
	    Long userNo = user.getUserNo();
	    int answer = chattingService.updateLastReadAt(roomId, userNo);
	    if(answer>0) {
		    return ResponseEntity.ok().build();
	    }else {
	    	return ResponseEntity.badRequest().build();
	    }
	}
	

	@PostMapping("/state")
	public ResponseEntity<chatProfile> updateState(
			@RequestBody Map<String,Object> statusParam,
	        @AuthenticationPrincipal UserDeptPoscode user
			){
		Long userNo = user.getUserNo();
		String status = (String)statusParam.get("state");
		int answer = chattingService.updateState(status,userNo);
		if(answer>0) {
			chatProfile returnProfile= chattingService.getMyProfile(userNo);
			
			if(returnProfile!=null) {
				customFile profile = fileService.getFile("CP",returnProfile.getUserNo());
				if(profile==null) {
					customFile tempProfile = new customFile();
					tempProfile.setChangeName("CHAT_PROFILE_DEFAULT.jpg");
					tempProfile.setImageCode("CP");
					tempProfile.setOriginName("CHAT_PROFILE_DEFAULT.jpg");
					profile=tempProfile;
				}
				returnProfile.setProfile(profile);
				return ResponseEntity.ok(returnProfile);
			}else {
				return ResponseEntity.badRequest().build();
			}
		}
		else
			return ResponseEntity.badRequest().build();
	}
	
	@GetMapping("/room/{roomId}/user")
	public ResponseEntity<List<chatProfile>> getRoomUsers(
	        @PathVariable("roomId") Long roomId
			){
		List<chatProfile> list = chattingService.getRoomUsers(roomId);
		if(list!=null) {
			
			for(chatProfile el : list) {
				customFile profile = fileService.getFile("CP",el.getUserNo());
				if(profile==null) {
					customFile tempProfile = new customFile();
					tempProfile.setChangeName("CHAT_PROFILE_DEFAULT.jpg");
					tempProfile.setImageCode("CP");
					tempProfile.setOriginName("CHAT_PROFILE_DEFAULT.jpg");
					profile=tempProfile;
				}
				el.setProfile(profile);
			}
			return ResponseEntity.ok(list);
		}else {
			return ResponseEntity.badRequest().build();
		}
		
	}
	
	@PostMapping("/room/join")
	public ResponseEntity<Void> joinUser(
		@RequestBody Map<String,Object> param){
		int answer = chattingService.setJoinUser(param);
		if(answer>0)
			return ResponseEntity.ok().build();
		else
			return ResponseEntity.badRequest().build();
	}
	
	@GetMapping("/profile/{userNo}")
	public ResponseEntity<chatProfileDetail> getProfileDetail(
			@PathVariable("userNo") Long userNo
			){
		chatProfileDetail prof = chattingService.getProfileDetail(userNo);

		if(prof!=null) {
			customFile profile = fileService.getFile("CP",prof.getUserNo());
			if(profile==null) {
				customFile tempProfile = new customFile();
				tempProfile.setChangeName("CHAT_PROFILE_DEFAULT.jpg");
				tempProfile.setImageCode("CP");
				tempProfile.setOriginName("CHAT_PROFILE_DEFAULT.jpg");
				profile=tempProfile;
			}
			prof.setProfile(profile);
			return ResponseEntity.ok(prof);
		}else {
			return ResponseEntity.badRequest().build();
		}
	}
	
	@PostMapping("/profile/image")
	public ResponseEntity<chatProfileDetail> changeProfileImage(
			@RequestParam("file") MultipartFile file,
	        @AuthenticationPrincipal UserDeptPoscode user
			){
		customFile profileImage = fileService.setOrChangeOneImage(file,user.getUserNo(),"CP");
		if(profileImage!= null) {
			chatProfileDetail myProfile = chattingService.getProfileDetail(user.getUserNo());
			myProfile.setProfile(profileImage);
			return ResponseEntity.ok(myProfile);
		}
		else
			return ResponseEntity.badRequest().build();
	}
	
	@PostMapping("/room/{roomId}/files")
	public ResponseEntity<List<chatMessages>> uploadFileOnRoom(
			@RequestParam("files") List<MultipartFile> files,
			@PathVariable("roomId") Long roomId,
	        @AuthenticationPrincipal UserDeptPoscode user
			){
		List<chatMessages> messages = new ArrayList<chatMessages>();
		int result = 0;
		for(MultipartFile file : files) {
			chatMessages message = new chatMessages();
			message.setUserNo(user.getUserNo());
	    	message.setIsFile("T");
	    	message.setType(chatMessages.MessageType.FILE);
	    	message.setRoomId(roomId);
	    	result += chattingService.insertMessage(message);
	    	customFile image = fileService.setOrChangeOneImage(file,message.getMessageId(),"CM");
	    	message.setMessageText(image.getChangeName());
	    	messages.add(message);
		}
    	
    	if(result >0)
    		return ResponseEntity.ok(messages);
    	else {
    		return ResponseEntity.badRequest().build();
    	}
	}
	
	@DeleteMapping("/room/{roomId}/leave")
	public ResponseEntity<Void> leaveRoom(
			@PathVariable("roomId") Long roomId,
	        @AuthenticationPrincipal UserDeptPoscode user
			){
		
		int result = chattingService.leaveRoom(roomId,user.getUserNo());
		
    	if(result >0)
    		return ResponseEntity.ok().build();
    	else {
    		return ResponseEntity.badRequest().build();
    	}
	}
	@PatchMapping("/room/alarm")
	public ResponseEntity<chatRooms> roomAlarmChange(
			@RequestBody Map<String,Object> param,
	        @AuthenticationPrincipal UserDeptPoscode user
			){
		Long roomId = Long.valueOf((int)param.get("roomId"));
		chatRooms bodyRoom = chattingService.getRoom(user.getUserNo(),roomId);
		bodyRoom.setAlarm((String)param.get("alarm"));
		int result = chattingService.alarmChange(bodyRoom);
		if(result>0)
    		return ResponseEntity.ok(bodyRoom);
		else
    		return ResponseEntity.badRequest().build();
	}
	
	@PostMapping("/room/missed-call")
    public ResponseEntity<chatMessages> createMissedCallMessage(
    		@RequestBody MissedCallRequest request,
	        @AuthenticationPrincipal UserDeptPoscode user) {
		chatMessages resultMessages = chattingService.createMissedCallMessage(user.getUserNo(), request.getPartnerNo());

    	if(resultMessages !=null)
    		return ResponseEntity.ok(resultMessages);
    	else {
    		return ResponseEntity.badRequest().build();
    	}
    }
}
