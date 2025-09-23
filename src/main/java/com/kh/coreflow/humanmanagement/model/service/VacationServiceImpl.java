package com.kh.coreflow.humanmanagement.model.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

import com.kh.coreflow.humanmanagement.model.dao.VacationDao;
import com.kh.coreflow.humanmanagement.model.dto.VacationDto.AvailableVacations;
import com.kh.coreflow.humanmanagement.model.dto.VacationDto.LoginUser;
import com.kh.coreflow.humanmanagement.model.dto.VacationDto.MemberChoice;
import com.kh.coreflow.humanmanagement.model.dto.VacationDto.MemberVacation;
import com.kh.coreflow.humanmanagement.model.dto.VacationDto.VacType;
import com.kh.coreflow.humanmanagement.model.dto.VacationDto.VacationInfo;
import com.kh.coreflow.model.dao.AuthDao;
import com.kh.coreflow.model.dto.UserDto.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VacationServiceImpl implements VacationService{
	private final VacationDao dao;
	private final AuthDao authDao;

	public List<VacationInfo> vacInfo() {
		return dao.vacInfo();
	}

	@Override
	public List<MemberVacation> allVacation(Map<String, Object> params) {
		return dao.allVacation(params);
	}
	
	@Override
	public List<MemberChoice> memChoice(String userName) {
		return dao.memChoice(userName);
	}

	@Override
	public List<MemberVacation> memVacation(Map<String, Object> params) {
		return dao.memVacation(params);
	}

	@Override
	public int vacStatusUpdate(Map<String, Object> params) {
		return dao.vacStatusUpdate(params);
	}
	
	@Override
	public LoginUser loginUserProfile(long userNo) {
		return dao.loginUserProfile(userNo);
	}

	@Override
	public List<MemberVacation> perVacation(Map<String, Object> params) {
		return dao.perVacation(params);
	}

	@Override
	public List<VacType> vacType() {
		return dao.vacType();
	}
	
	@Override
	public int putPerVac(Map<String, Object> params) {
		return dao.putPerVac(params);
	}

	@Override
	public AvailableVacations availableVacations(Map<String, Object> params) {
		List<MemberVacation> perVacation = dao.perVacation(params); // 신청 휴가 내역
		
		// 경력(연차) 조회
		User user = authDao.findUserByUserNo((Long)params.get("userNo"))
				.orElseThrow(() -> new RuntimeException("사용자 조회 실패"));
		LocalDate YearOfEmployment = user.getHireDate()
				.toInstant().atZone(ZoneId.of("Asia/Seoul")).toLocalDate();
		int experience = (Integer)params.get("year") - YearOfEmployment.getYear();
		System.out.println("experience = " + experience);
		
		params.put("experience", experience);
		int availableVac = dao.availableVac(params); // 사용 가능 휴가 조회
		
		float usedVacAmount = 0f;	
		if (perVacation != null) {
			usedVacAmount  = (float) perVacation.stream() // 사용한 연차 조회
                .filter(v -> v.getStatus() == 2 && v.getVacName().equals("연차"))
                .mapToDouble(MemberVacation::getVacAmount)
                .sum();
		}
		
		float vacRemaining = availableVac - usedVacAmount;
		
		return new AvailableVacations(availableVac, vacRemaining);
	}
}