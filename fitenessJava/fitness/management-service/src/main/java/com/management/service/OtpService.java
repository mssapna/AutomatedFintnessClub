package com.management.service;

import java.sql.Timestamp;

import com.management.entity.User;

public interface OtpService {
	String generateOtp();

	void sendOtpEmail(String toEmail, String otp);

	void saveOtp(String email, String otp, Timestamp expirationTime);

	boolean verifyOtp(String email, String enteredOtp);

	User forgetPassword(String email); 

}
