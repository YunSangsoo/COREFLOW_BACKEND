package com.kh.coreflow.security.model.provider;

import java.security.Key;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JWTProvider {

	private final Key key;
	private final Key refreshKey;
	
	public JWTProvider(
			@Value("${jwt.secret}")
			String secretBase64,
			@Value("${jwt.refresh-secret}")
			String refreshSecretBase64
			) {
		byte[] keyBytes = Decoders.BASE64.decode(secretBase64);
		this.key = Keys.hmacShaKeyFor(keyBytes);
		this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecretBase64));
	}

	public String createAccessToken(Long userNo, Long depId, Long posId, List<String> roles, int minutes) {
		Date now = new Date();
		return Jwts.builder()
				.setSubject(String.valueOf(userNo)) // 페이로드에 저장할 id
				.claim("roles", roles)
				.claim("depId", depId)
				.claim("posId", posId)
				.setIssuedAt(now) // 토큰 발행시간
				.setExpiration(new Date(now.getTime()+(1000L * 60 * minutes)))
				.signWith(key, SignatureAlgorithm.HS256) // 서명에 사용할 키값과, 알고리즘
				.compact();
	}

	public String createRefreshToken(Long userNo, int i) {
		Date now = new Date();
		return Jwts.builder()
				.setSubject(String.valueOf(userNo)) // 페이로드에 저장할 id
				.setIssuedAt(now) // 토큰 발행시간
				.setExpiration(new Date(System.currentTimeMillis() + (1000 * 60 * 60 * 24 * i))) // 토큰 만료시간
				.signWith(refreshKey, SignatureAlgorithm.HS256) // 서명에 사용할 키값과, 알고리즘
				.compact();
	}

	public Long getUserNo(String token) {
		return Long.valueOf(
				Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getSubject()
				);
	}

	public Long parseRefresh(String token) {
		return Long.valueOf(
				Jwts.parserBuilder()
				.setSigningKey(refreshKey)
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getSubject()
				);
	}

	public List<String> getRoles(String token) {
		Claims claims = Jwts.parserBuilder()
	            .setSigningKey(key)   // AccessToken용 키
	            .build()
	            .parseClaimsJws(token)
	            .getBody();

	    Object rolesObject = claims.get("roles"); // 토큰 생성 시 claim에 넣은 roles
	    if (rolesObject instanceof List<?> rolesList) {
	        return rolesList.stream()
	        	.filter(role -> role instanceof String)
	        	.map(role -> (String) role)
	        	.toList();
	    }

	    return List.of();
	}
	
	public Long getDeptcode(String token) {
		Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
		
		return getLongValue(claims.get("depId"));
	}
	
	public Long getPoscode(String token) {
		Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
		
		return getLongValue(claims.get("posId"));
	}
	
	private int getIntValue(Object obj) {
	    if (obj == null) return 0; // 기본값
	    if (obj instanceof Integer i) return i;
	    if (obj instanceof Long l) return l.intValue();
	    return Integer.parseInt(obj.toString());
	}

	private long getLongValue(Object obj) {
	    if (obj == null) return 0L;
	    if (obj instanceof Long l) return l;
	    if (obj instanceof Integer i) return i.longValue();
	    return Long.parseLong(obj.toString());
	}
	
	public boolean validateToken(String token) {
		try {
			getClaims(token);
			return true;
		} catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
			log.info("잘못된 JWT 서명입니다.");
		} catch (ExpiredJwtException e) {
			log.info("만료된 JWT 토큰입니다.");
		} catch (UnsupportedJwtException e) {
			log.info("지원되지 않는 JWT 토큰입니다.");
		} catch (IllegalArgumentException e) {
			log.info("JWT 토큰이 잘못되었습니다.");
		}
		return false;
	}
	
	public Authentication getAuthentication(String accessToken) {
		// 토큰 복호화
		Claims claims = getClaims(accessToken);
		// 클레임에서 userNo 추출
		long userNo = Long.parseLong(claims.getSubject());
		// 권한 정보 생성
		List<String> roles = getRoles(accessToken);
		List<SimpleGrantedAuthority> authorities = roles.stream()
														.map(SimpleGrantedAuthority::new)
														.toList();
		// UsernamePasswordAuthenticationToken을 만들어 반환
		return new UsernamePasswordAuthenticationToken(userNo, null, authorities);
	}
	
	private Claims getClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token)
				.getBody();
	}
	
}
