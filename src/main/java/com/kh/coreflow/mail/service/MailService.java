package com.kh.coreflow.mail.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.kh.coreflow.model.dto.UserDto.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailService {

	private final JavaMailSender mailSender;
	
	// 계정 생성시
	public void sendTemporaryPassword(User user, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("CoreFlow 계정 생성 안내");
        message.setText(String.format(
        	    "안녕하세요, %s사원님의 계정이 생성되었습니다.\n임시 비밀번호는 아래와 같습니다.\n\n%s\n\n로그인 후 반드시 비밀번호를 변경해주세요.",
        	    user.getUserName(), tempPassword
        	));

        mailSender.send(message);
    }
	
	// 비밀번호 찾기 시도
	public void sendTempPwd(User user, String tempPwd) {
		SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("CoreFlow 계정 임시 비밀번호 안내");
        message.setText(String.format(
        	    "안녕하세요, %s사원님의 임시 비밀번호가 생성되었습니다.\n임시 비밀번호는 아래와 같습니다.\n\n%s\n\n로그인 후 반드시 비밀번호를 변경해주세요.",
        	    user.getUserName(), tempPwd
        	));

        mailSender.send(message);
	}
}
