package com.kh.coreflow.calendar.model.dto;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class CalendarDto {

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class SummaryRes {
		private Long calId;
		private String calName;
		private String color;
		private Integer isPersonal; // 1/0
		private Integer defaultForMe; // 1/0
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class DetailRes {
		private Long calId;
		private Long depId;
		private Long ownerUserNo;
		private String calName;
		private String color;
		private String defaultRole;
		private String deletedYn;
		private Date createDate;
		private Date updateDate;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class CreateReq {
		private String name;
		private String color;
		private String defaultRole; // 기본 공유 권한
		private Long depId; // 선택
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class CreateRes {
		private Long calId;
		private String name; // = CAL_NAME
		private String color;
		private String defaultRole;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class UpdateReq {
		private String name;
		private String color;
		private String defaultRole;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class ShareUser {
		private Long userNo;
		private String role; // NONE | BUSY_ONLY | READER | CONTRIBUTOR | EDITOR
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class ShareDept {
		private Long depId;
		private String role;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class SharePos {
		private Long posId;
		private String role;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class ShareListRes {
		private java.util.List<ShareUser> users;
		private java.util.List<ShareDept> departments;
		private java.util.List<SharePos> positions;
		private String defaultRole; // CALENDAR.DEFAULT_ROLE
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class ShareUpsertReq {
		 @JsonAlias("members")
		private java.util.List<ShareUser> users;
		private java.util.List<ShareDept> departments;
		private java.util.List<SharePos> positions;
	}
}
