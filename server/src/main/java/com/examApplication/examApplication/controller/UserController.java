package com.examApplication.examApplication.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.examApplication.examApplication.dto.LoginRequestDTO;
import com.examApplication.examApplication.dto.UserAccessRequestDTO;
import com.examApplication.examApplication.dto.UserDTO;
import com.examApplication.examApplication.entity.Exam;
import com.examApplication.examApplication.entity.auth.User;
import com.examApplication.examApplication.repository.UserRepository;
import com.examApplication.examApplication.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder; 

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/login")
    public User login(@RequestBody LoginRequestDTO loginRequest) {
        return userService.login(loginRequest.getEmail(), loginRequest.getPassword());
    }
    
    @PutMapping("/updateStatus")
    public ResponseEntity<Void> updateStatus(@RequestBody UserAccessRequestDTO dto) {
        System.out.println("📩 Received update request: " + dto);
        userService.updateStatus(dto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @GetMapping("/profile")
    public ResponseEntity<String> getStudentProfile(Authentication authentication) {
        return ResponseEntity.ok("Profile details for: " + authentication.getName());
    }
    @DeleteMapping("/delete/{id}")
	public List<User> deleteStudent(@PathVariable int id){
		return userService.deleteUser(id);
	}

    @GetMapping("/pending-users")
    public ResponseEntity<List<User>> getPendingUsers() {
        return ResponseEntity.ok(userService.getPendingUsers());
    }

    @PutMapping("/admin/approve/{id}")
    public ResponseEntity<List<User>> approveUser(@PathVariable int id) {
        List<User> updatedPendingUsers = userService.approveUser(id);
        return ResponseEntity.ok(updatedPendingUsers); // ✅ Send updated list to frontend
    }

    @DeleteMapping("/reject/{id}")
    public ResponseEntity<List<User>> rejectUser(@PathVariable int id) {
        return ResponseEntity.ok(userService.rejectUser(id));
    }

}