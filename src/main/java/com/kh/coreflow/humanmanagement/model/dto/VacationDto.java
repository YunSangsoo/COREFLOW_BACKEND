package com.kh.coreflow.humanmanagement.model.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class VacationDto {

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class VacationInfo{
		private int workLv;
		private int workPrd;
		private int vacAmount;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MemberChoice{
		private int userNo;
		private String userName;
		private String depName;
		private String posName;
		private Date hireDate;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MemberVacation{
		private int userNo;
		private String userName;
		private int vacId;
		private String vacName;
		private Date vacStart;
		private Date vacEnd;
		private float vacAmount;
		private int status;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PutVacStatus{
		private int vacId;
		private int status;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class LoginUser{
		private int userNo;
		private String userName;
		private String depName;
		private String posName;
		private Date hireDate;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class VacType{
		private int vacCode;
		private String vacName;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PutVacation{
		private int vacCode;
		private Date vacStart;
		private Date vacEnd;
		private float vacAmount;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class AvailableVacations{
		private int vacAmount;
		private float vacRemaining;
	}
}