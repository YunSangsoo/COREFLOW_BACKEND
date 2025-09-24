package com.kh.coreflow.security.factory;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

import com.kh.coreflow.humanmanagement.model.dao.MemberDao;
import com.kh.coreflow.humanmanagement.model.dto.MemberDto.MemberPost;
import com.kh.coreflow.model.dto.UserDto.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserFactory {

	private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private final MemberDao dao;

    public static String generateRandomPassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHAR_POOL.length());
            sb.append(CHAR_POOL.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 인사, 관리자 권한(ROLE_HR, ROLE_ADMIN)을 가져야 쓸 수 있는 계정생성
     * 수정 필요
     */
    public User createIncompleteUser(MemberPost member) {
        return User.builder()
                .email(member.getEmail())
                .userPwd(generateRandomPassword(10))
                .userName(member.getUserName())	// 프론트 입력값
                .depId(dao.findDepId(member.getDepName())) 	// 프론트 select-option
                .posId(dao.findPodId(member.getPosName()))   // 프론트 select-option
                .profile(member.getProfile())	// 기본 프로필
                .hireDate(member.getHireDate())	// 계정 생성일
                .phone(member.getPhone())
                .extension(member.getExtension())
                .address(member.getAddress())
                .addressDetail(member.getAddressDetail())
                .status("T")	// 재직여부
                .build();
    }
}
