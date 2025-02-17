package com.mykare.user_management.controller;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mykare.user_management.Response;
import com.mykare.user_management.model.User;
import com.mykare.user_management.repository.UserRepository;
import com.mykare.user_management.security.UserDetailsImpl;
import com.mykare.user_management.security.jwt.JwtResponse;
import com.mykare.user_management.security.jwt.JwtUtils;
import com.mykare.user_management.service.UserService;
import com.mykare.user_management.service.serviceImpl.LocationService;
import com.mykare.user_management.webModel.UserWebModel;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/user")
public class UserController {

	public static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	JwtUtils jwtUtils;

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserService userService;

	@Autowired
	UserRepository userRepository;
	
    @Autowired
    private LocationService locationService;

	@PostMapping("register")
	@Operation(summary = "Register a new user")
	public ResponseEntity<?> userRegister(@RequestBody UserWebModel userWebModel,HttpServletRequest request) {
		try {
			logger.info("User register controller start");
			 String ipAddress = request.getRemoteAddr();
		        if ("0:0:0:0:0:0:0:1".equals(ipAddress) || "127.0.0.1".equals(ipAddress)) {
		            ipAddress = locationService.getIpAddress();
		        }
			return userService.register(userWebModel,ipAddress);
		} catch (Exception e) {
			logger.error("userRegister Method Exception {}" + e);
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new Response(-1, "Fail", e.getMessage()));
		}

	}
	@PostMapping("login")
	@Operation(summary = "User login and token generation")
	public ResponseEntity<?> login(@RequestBody UserWebModel userWebModel) {
		try {
			Optional<User> checkUser = userRepository.findByEmailId(userWebModel.getEmailId());

			if (checkUser.isPresent()) {
				User user = checkUser.get();

				// Authenticate user with email and password
				Authentication authentication = authenticationManager.authenticate(
						new UsernamePasswordAuthenticationToken(userWebModel.getEmailId(), userWebModel.getPassword()));

				SecurityContextHolder.getContext().setAuthentication(authentication);


				// Generate JWT token
				String jwt = jwtUtils.generateJwtToken(authentication);
				UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

				logger.info("Login successful for user: {}", user.getEmailId());

				// Return response with JWT and refresh token
				return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), 1 ));
			} else {
				return ResponseEntity.badRequest().body(new Response(-1, "Fail", "Invalid email or password"));
			}
		} catch (BadCredentialsException e) {
			logger.error("Login failed: Invalid credentials");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new Response(-1, "Fail", "Invalid email or password"));
		} catch (Exception e) {
			logger.error("Error at login() -> {}", e.getMessage(), e);
			return ResponseEntity.internalServerError()
					.body(new Response(-1, "Fail", "An error occurred during login"));
		}
	}
	
	@DeleteMapping("/deleteUser/{userId}")
	@Operation(summary = "Delete a user by ID")
	public ResponseEntity<?> deleteUserDetails(@PathVariable("userId") Integer userId, 
	                                          @RequestParam("requestingUserEmail") String requestingUserEmail) {
	    try {
	        logger.info("Received request to delete user with userId: {}", userId);
	        
	        // Call service layer
	        ResponseEntity<?> response = userService.deleteUserDetails(userId, requestingUserEmail);

	        logger.info("User deletion response: {}", response.getBody());
	        return response;
	    } catch (Exception e) {
	        logger.error("Exception in deleteUserDetails: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", "An error occurred while deleting the user."));
	    }
	}

	@GetMapping("/getAllUsers")
	@Operation(summary = "Get all registered users")
	public ResponseEntity<?> getAllUsers(@RequestParam("requestingUserEmail") String requestingUserEmail) {
	    try {
	        logger.info("Received request to fetch all users from: {}", requestingUserEmail);

	        // Call service layer
	        ResponseEntity<?> response = userService.getAllUsers(requestingUserEmail);

	        logger.info("Get all users response: {}", response.getBody());
	        return response;
	    } catch (Exception e) {
	        logger.error("Exception in getAllUsers: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(new Response(-1, "Fail", "An error occurred while fetching users."));
	    }
	}



}
