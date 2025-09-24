package com.kh.coreflow.humanmanagement.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AttendanceDto {
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AttendanceInfo{
		private Long attId;
		private String attDate;
		private String userName;
		private String depName;
		private String posName;
		private String checkInTime;
		private String checkOutTime;
		private String vacName;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PutCheckIn{
		private Long attId;
		private String attDate;
		private String checkInTime;
		private int status;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PutCheckOut{
		private Long attId;
		private String checkOutTime;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class VacType{
		private Long vacCode;
		private String vacName;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class VacTypeUpdate{
		private Long attId;
		private int vacCode;
	}
}