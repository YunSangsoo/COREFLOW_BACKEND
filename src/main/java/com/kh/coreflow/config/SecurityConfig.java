package com.kh.coreflow.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import com.kh.coreflow.security.filter.JWTAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http,
			JWTAuthenticationFilter jwtFilter) throws Exception{
		http
			// cors관련 빈객체 등록
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			// CSRF는 SPA어플리케이션에서 사용하지 않음
			.csrf(csrf -> csrf.disable())
			.exceptionHandling(e -> e
				    // 인증 실패 (로그인 안 한 사용자 → 401)
				    .authenticationEntryPoint((req, res, ex) -> {
				        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED");
				    })
				    // 인가 실패 (권한 없는 사용자 → 403)
				    .accessDeniedHandler((req, res, ex) -> {
				        res.sendError(HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN");
				    }))
					.sessionManagement(
							management -> 
							management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
					.authorizeHttpRequests(auth
						-> auth
						.requestMatchers("/vacation/member/**","/attendance/member/**").hasAnyRole("ADMIN","HR")
						.requestMatchers("/auth/login/**","/auth/find-pwd/**","/auth/logout/**","/auth/refresh/**").permitAll()
						.requestMatchers("/login**","/error").permitAll()
						.requestMatchers("/api/approvals/documents").permitAll()
						.requestMatchers("/images/**","/download/**").permitAll()
						.requestMatchers("/ws/**").permitAll()
						.requestMatchers("/**").authenticated()
					);
		http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
		
		return http.build();
	}

	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		
		// 허용 Origin설정
		config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000", "http://3.37.61.74"));
		
		// 허용 메서드
		config.setAllowedMethods(List.of("GET","POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setExposedHeaders(List.of("Location", "Authorization"));
		config.setAllowCredentials(true); // 세션, 쿠키 허용
		config.setMaxAge(3600L); //요청정보 캐싱시간
		
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		
		return source;
	}
	
	@Bean
	PasswordEncoder passwordEncoder() {
		PasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder;
	}
	
	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
