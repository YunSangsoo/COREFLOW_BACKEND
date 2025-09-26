package com.kh.coreflow.humanmanagement.model.dao;

import java.util.List;
import java.util.Map;

import com.kh.coreflow.humanmanagement.model.dto.VacationDto.AvailableVacations;
import com.kh.coreflow.humanmanagement.model.dto.VacationDto.LoginUser;
import com.kh.coreflow.humanmanagement.model.dto.VacationDto.MemberChoice;
import com.kh.coreflow.humanmanagement.model.dto.VacationDto.MemberVacation;
import com.kh.coreflow.humanmanagement.model.dto.VacationDto.VacType;
import com.kh.coreflow.humanmanagement.model.dto.VacationDto.VacationInfo;

public interface VacationDao {

	List<VacationInfo> vacInfo();

	List<MemberVacation> allVacation(Map<String, Object> params);
	
	List<MemberChoice> memChoice(String userName);

	List<MemberVacation> memVacation(Map<String, Object> params);

	int vacStatusUpdate(Map<String, Object> params);
	
	LoginUser loginUserProfile(long userNo);
	
	List<MemberVacation> perVacation(Map<String, Object> params);

	List<VacType> vacType();

	int putPerVac(Map<String, Object> params);

	int availableVac(Map<String, Object> params);
}