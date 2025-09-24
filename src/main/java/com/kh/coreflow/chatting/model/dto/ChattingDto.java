package com.kh.coreflow.chatting.model.dto;

import java.util.Date;
import java.util.List;

import com.kh.coreflow.common.model.vo.FileDto.customFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ChattingDto {
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class chatMessages{
		private Long messageId;
		private Long roomId;
		private Long userNo;
		private String messageText;
		private Date sentAt;
		private String isFile;

		// 클라이언트의 메시지 유형을 관리할 속성
		public enum MessageType{
			ENTER, EXIT, TALK, FILE, VIDEO_CALL_INVITE
		}
		private MessageType type;
		
		private String userName;
		private customFile file;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class chatProfile{
		private Long userNo;
		private String userName;
		private String status;
		
		private customFile profile;
	}
	

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class chatProfileDetail{
		private Long userNo;
		private String userName;
		private String status;
		private String email;
		private String phone;
		private String depName;
		private String posName;

		private customFile profile;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class chatRoomJoin{
		private Long userNo;
		private Long roomId;
		private Date joinedAt;
		private Date lastReadAt;
		private String alarm;
		private String highlight;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class chatRooms{
		private Long roomId;
		private Long userNo;
		private String roomName;
		private String roomType;
		private String status;
		private Date createdAt;
		private chatMessages lastMessage;
		
		private int unreadCount;
		
		private List<chatProfile> partner;
		
		private String alarm;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class userFavorite{
		private Long favoriteId;
		private Long userNo;
		private Long favoriteUserNo;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class SignalMessage {
	    private String type; // 메시지 타입 ("offer", "answer", "ice")
	    private Long from;   // 보낸 사람의 userNo
	    private Long to;     // 받는 사람의 userNo
	    private Object data; // 실제 WebRTC 데이터 (SDP 또는 ICE Candidate)
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class MissedCallRequest {
        private Long partnerNo;
    }
	
}
