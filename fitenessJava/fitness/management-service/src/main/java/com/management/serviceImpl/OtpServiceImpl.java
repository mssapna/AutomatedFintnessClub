package com.management.serviceImpl;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.management.entity.OTPEntity;
import com.management.entity.User;
import com.management.exception.EmailNotFoundException;
import com.management.exception.InvalidOtpException;
import com.management.repository.OtpRepository;
import com.management.repository.UserRepository;
import com.management.service.OtpService;

public class OtpServiceImpl implements OtpService
{
	@Autowired
	private JavaMailSender javaMailSender;
	@Autowired
	private OtpRepository otpRepository;
	@Autowired
	private UserRepository userRepository;



	@Override
	public String generateOtp() {
		Random random = new Random();
		int otp = 100000 + random.nextInt(900000);
		return String.valueOf(otp);
	}

	@Override
	public void sendOtpEmail(String toEmail, String otp) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(toEmail);
		message.setSubject("Your OTP");
		message.setText("Your OTP is: " + otp);

		javaMailSender.send(message);
	}
	@Override
	public User forgetPassword(String email) {
				
//					log.info("Checking if email is present or not");
		Optional<User> user = Optional.ofNullable(userRepository.findByEmail(email));
		System.out.println("email"+email);
		if (user.isPresent()) {
			//				log.info("Email is valid");
			String otp = generateOtp();

			Timestamp expirationTime = Timestamp.from(Instant.now().plus(Duration.ofMinutes(5)));

			sendOtpEmail(email, otp);
			saveOtp(email, otp, expirationTime);
			return user.get();
		} else {
//			log.info("Email is not valid");
			throw new EmailNotFoundException("Email not found");			
		}
		
	}
			
			


	@Override
	public void saveOtp(String email, String otp, Timestamp expirationTime) {
		Optional<OTPEntity> existingOtp = otpRepository.findByEmail(email);

		if (existingOtp.isPresent()) {
			existingOtp.get().setOtp(otp);
			existingOtp.get().setExpirationTime(expirationTime);
			otpRepository.save(existingOtp.get());
		} else {
			OTPEntity newOtp = new OTPEntity();
			newOtp.setEmail(email);
			newOtp.setOtp(otp);
			newOtp.setExpirationTime(expirationTime);
			otpRepository.save(newOtp);
		}
	}

	@Override
	public boolean verifyOtp(String email, String enteredOtp) {

		OTPEntity otpEntity = otpRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("OTP not found"));

		Timestamp expirationTime = otpEntity.getExpirationTime();

		LocalDateTime expirationLocalDateTime = expirationTime.toInstant().atZone(ZoneId.systemDefault())
				.toLocalDateTime();

		if (expirationLocalDateTime.isBefore(LocalDateTime.now())) {
			return false;
		}

		else if (!enteredOtp.equals(otpEntity.getOtp())) {
				throw new InvalidOtpException("Entered Correct otp");
		} else {
			return true;
		}

	}

}
