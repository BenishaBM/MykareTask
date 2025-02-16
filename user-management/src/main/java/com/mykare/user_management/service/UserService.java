package com.mykare.user_management.service;

import org.springframework.http.ResponseEntity;

import com.mykare.user_management.webModel.UserWebModel;

public interface UserService {

	ResponseEntity<?> register(UserWebModel userWebModel);

	ResponseEntity<?> deleteUserDetails(Integer userId, String requestingUserEmail);

	ResponseEntity<?> getAllUsers(String requestingUserEmail);

}
