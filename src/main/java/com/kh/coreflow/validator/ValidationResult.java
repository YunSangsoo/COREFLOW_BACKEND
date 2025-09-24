package com.kh.coreflow.validator;

public class ValidationResult {
	private boolean valid;
	private String message;
	
	public ValidationResult(boolean valid, String message) {
		this.valid = valid;
		this.message = message;
	}
	
	public boolean isValid() { return valid; }
	public String getMessage() { return message; }
	
	// 통과
	public static ValidationResult ok() {
		return new ValidationResult(true, null);
	}
	// 실패
	public static ValidationResult fail(String message) {
		return new ValidationResult(false, message);
	}
}
