package com.mykare.user_management.service.serviceImpl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.mykare.user_management.Response;
import com.mykare.user_management.model.User;
import com.mykare.user_management.repository.UserRepository;
import com.mykare.user_management.service.UserService;
import com.mykare.user_management.webModel.UserWebModel;

@Service
public class UserServiceImpl implements UserService{
	
	@Autowired
	UserRepository userRepository;
	
    @Autowired
    private LocationService locationService;

	@Override
	public ResponseEntity<?> register(UserWebModel userWebModel,String ipAddress) {
	    // Check if email already exists
	    Optional<User> existingUser = userRepository.findByEmailId(userWebModel.getEmailId());
	    if (existingUser.isPresent()) {
	        return ResponseEntity.badRequest().body(new Response(0, "fail", "Email already in use. Please use a different email."));
	    }

	    // Create new user entity
	    User newUser = new User();
	    newUser.setUserName(userWebModel.getUserName());
	    newUser.setEmailId(userWebModel.getEmailId());
	    newUser.setGender(userWebModel.getGender());
	    newUser.setUserType(userWebModel.getUserType());
        // Set IP address
	    newUser.setIpAddress(ipAddress);
        
        // Get country from IP
	    newUser.setCountry(locationService.getCountryFromIp(ipAddress));

	    // Encrypt password before saving
	    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	    newUser.setPassword(passwordEncoder.encode(userWebModel.getPassword()));

	    // Save user to database
	    userRepository.save(newUser);

	    return ResponseEntity.ok(new Response(1, "success", "User registered successfully!"));
	}

	@Override
	public ResponseEntity<?> deleteUserDetails(Integer userId, String requestingUserEmail) {
	    Optional<User> requestingUser = userRepository.findByEmailId(requestingUserEmail);
	    
	    // Check if requesting user is an admin
	    if (requestingUser.isEmpty() || !requestingUser.get().getUserType().equalsIgnoreCase("ADMIN")) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN)
	                .body(new Response(0, "Fail", "Access denied. Only admin users can delete accounts."));
	    }

	    // Check if user exists
	    Optional<User> userToDelete = userRepository.findById(userId);
	    if (userToDelete.isEmpty()) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(new Response(0, "Fail", "User not found."));
	    }

	    // Delete user
	    userRepository.delete(userToDelete.get());

	    return ResponseEntity.ok(new Response(1, "Success", "User deleted successfully."));
	}

	public ResponseEntity<?> getAllUsers(String requestingUserEmail) {
	    Optional<User> requestingUser = userRepository.findByEmailId(requestingUserEmail);

	    // Check if the requesting user is an admin
	    if (requestingUser.isEmpty() || !requestingUser.get().getUserType().equalsIgnoreCase("ADMIN")) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN)
	                .body(new Response(0, "Fail", "Access denied. Only admin users can fetch all users."));
	    }

	    // Fetch all users
	    List<User> users = userRepository.findAll();

	    return ResponseEntity.ok(new Response(1, "Success", users));
	}

}
