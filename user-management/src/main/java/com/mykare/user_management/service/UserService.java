package com.mykare.user_management.service;

import org.springframework.http.ResponseEntity;

import com.mykare.user_management.model.User;
import com.mykare.user_management.webModel.UserWebModel;

public interface UserService {

	
	ResponseEntity<?> deleteUserDetails(Integer userId, String requestingUserEmail);

	ResponseEntity<?> getAllUsers(String requestingUserEmail);

	ResponseEntity<?> register(UserWebModel userWebModel, String ipAddress);

}
