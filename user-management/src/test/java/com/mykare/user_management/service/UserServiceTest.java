package com.mykare.user_management.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.mykare.user_management.Response;
import com.mykare.user_management.model.User;
import com.mykare.user_management.repository.UserRepository;
import com.mykare.user_management.webModel.UserWebModel;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User adminUser;
    private User normalUser;
    private List<User> userList;

    @BeforeEach
    void setUp() {
        // Create an Admin User
        adminUser = new User();
        adminUser.setUserId(1);
        adminUser.setUserName("Admin User");
        adminUser.setEmailId("admin@example.com");
        adminUser.setGender("Male");
        adminUser.setUserType("ADMIN");

        // Create a Normal User
        normalUser = new User();
        normalUser.setUserId(2);
        normalUser.setUserName("Normal User");
        normalUser.setEmailId("user@example.com");
        normalUser.setGender("Female");
        normalUser.setUserType("USER");

        // Create a List of Users
        userList = Arrays.asList(adminUser, normalUser);
    }

    @Test
    void testGetAllUsers_Success_AdminUser() {
        when(userRepository.findByEmailId("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(userRepository.findAll()).thenReturn(userList);

        ResponseEntity<?> response = userService.getAllUsers("admin@example.com");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Response);

        Response responseBody = (Response) response.getBody();
        assertEquals(1, responseBody.getStatus());
        assertEquals("Success", responseBody.getMessage());
        assertNotNull(responseBody.getData());
        assertEquals(2, ((List<?>) responseBody.getData()).size());
    }

    @Test
    void testGetAllUsers_Failure_NonAdminUser() {
        when(userRepository.findByEmailId("user@example.com")).thenReturn(Optional.of(normalUser));

        ResponseEntity<?> response = userService.getAllUsers("user@example.com");

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody() instanceof Response);

        Response responseBody = (Response) response.getBody();
        assertEquals(0, responseBody.getStatus());
        assertEquals("Fail", responseBody.getMessage());
        assertEquals("Access denied. Only admin users can fetch all users.", responseBody.getData());
    }

    @Test
    void testGetAllUsers_Failure_UserNotFound() {
        when(userRepository.findByEmailId("unknown@example.com")).thenReturn(Optional.empty());

        ResponseEntity<?> response = userService.getAllUsers("unknown@example.com");

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody() instanceof Response);

        Response responseBody = (Response) response.getBody();
        assertEquals(0, responseBody.getStatus());
        assertEquals("Fail", responseBody.getMessage());
        assertEquals("Access denied. Only admin users can fetch all users.", responseBody.getData());
    }

    @Test
    void testGetAllUsers_ExceptionHandling() {
        when(userRepository.findByEmailId("admin@example.com")).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<?> response = userService.getAllUsers("admin@example.com");

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof Response);

        Response responseBody = (Response) response.getBody();
        assertEquals(-1, responseBody.getStatus());
        assertEquals("Fail", responseBody.getMessage());
        assertEquals("An error occurred while fetching users.", responseBody.getData());
    }

    @Test
    public void testDeleteUserDetails_AdminUser_Success() {
        Integer userId = 1;
        String adminEmail = "admin@example.com";
        User adminUser = new User();
        adminUser.setUserId(100);
        adminUser.setUserName("Admin");
        adminUser.setEmailId(adminEmail);
        adminUser.setUserType("ADMIN");

        User userToDelete = new User();
        userToDelete.setUserId(userId);
        userToDelete.setUserName("User1");
        userToDelete.setEmailId("user1@example.com");
        userToDelete.setUserType("USER");

        when(userRepository.findByEmailId(adminEmail)).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(userId)).thenReturn(Optional.of(userToDelete));

        ResponseEntity<?> response = userService.deleteUserDetails(userId, adminEmail);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, ((Response) response.getBody()).getStatus());
        verify(userRepository, times(1)).delete(userToDelete);
    }

    @Test
    public void testDeleteUserDetails_NonAdminUser_Forbidden() {
        Integer userId = 1;
        String nonAdminEmail = "user@example.com";
        User nonAdminUser = new User();
        nonAdminUser.setUserId(101);
        nonAdminUser.setUserName("User");
        nonAdminUser.setEmailId(nonAdminEmail);
        nonAdminUser.setUserType("USER");

        when(userRepository.findByEmailId(nonAdminEmail)).thenReturn(Optional.of(nonAdminUser));

        ResponseEntity<?> response = userService.deleteUserDetails(userId, nonAdminEmail);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied. Only admin users can delete accounts.", ((Response) response.getBody()).getMessage());
        verify(userRepository, never()).delete(any());
    }

    @Test
    public void testDeleteUserDetails_UserNotFound() {
        Integer userId = 1;
        String adminEmail = "admin@example.com";
        User adminUser = new User();
        adminUser.setUserId(100);
        adminUser.setUserName("Admin");
        adminUser.setEmailId(adminEmail);
        adminUser.setUserType("ADMIN");

        when(userRepository.findByEmailId(adminEmail)).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = userService.deleteUserDetails(userId, adminEmail);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found.", ((Response) response.getBody()).getMessage());
        verify(userRepository, never()).delete(any());
    }

    @Test
    public void testDeleteUserDetails_ExceptionHandling() {
        Integer userId = 1;
        String adminEmail = "admin@example.com";
        User adminUser = new User();
        adminUser.setUserId(100);
        adminUser.setUserName("Admin");
        adminUser.setEmailId(adminEmail);
        adminUser.setUserType("ADMIN");

        when(userRepository.findByEmailId(adminEmail)).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(userId)).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.deleteUserDetails(userId, adminEmail));

        assertEquals("Database error", exception.getMessage());
    }
    
    @Test
    void testRegister_Success() {
        UserWebModel newUserWebModel = new UserWebModel();
        newUserWebModel.setUserName("New User");
        newUserWebModel.setEmailId("newuser@example.com");
        newUserWebModel.setGender("Non-Binary");
        newUserWebModel.setUserType("USER");
        newUserWebModel.setPassword("password123");

        when(userRepository.findByEmailId("newuser@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        ResponseEntity<?> response = userService.register(newUserWebModel);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Response);

        Response responseBody = (Response) response.getBody();
        assertEquals(1, responseBody.getStatus());
        assertEquals("success", responseBody.getMessage());
        assertEquals("User registered successfully!", responseBody.getData());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        UserWebModel existingUserWebModel = new UserWebModel();
        existingUserWebModel.setUserName("Existing User");
        existingUserWebModel.setEmailId("user@example.com");
        existingUserWebModel.setGender("Female");
        existingUserWebModel.setUserType("USER");
        existingUserWebModel.setPassword("password123");

        when(userRepository.findByEmailId("user@example.com")).thenReturn(Optional.of(normalUser));

        ResponseEntity<?> response = userService.register(existingUserWebModel);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Response);

        Response responseBody = (Response) response.getBody();
        assertEquals(0, responseBody.getStatus());
        assertEquals("fail", responseBody.getMessage());
        assertEquals("Email already in use. Please use a different email.", responseBody.getData());

        verify(userRepository, never()).save(any(User.class));
    }
}
