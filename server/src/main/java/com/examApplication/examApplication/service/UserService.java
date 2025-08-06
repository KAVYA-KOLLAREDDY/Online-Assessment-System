package com.examApplication.examApplication.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import com.examApplication.examApplication.dto.UserAccessRequestDTO;
import com.examApplication.examApplication.dto.UserDTO;
import com.examApplication.examApplication.entity.Exam;
import com.examApplication.examApplication.entity.auth.Role;
import com.examApplication.examApplication.entity.auth.User;
import com.examApplication.examApplication.exception.StudentNotFoundException;
import com.examApplication.examApplication.model.Status;
import com.examApplication.examApplication.repository.RoleRepository;
import com.examApplication.examApplication.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;	
    @Autowired
    private RoleRepository roleRepository;

    public User login(String email, String password) {
        User user = userRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        Role role = roleRepository.findByRoleName("ADMIN")
                .orElseThrow(() -> new RuntimeException("Role - ADMIN NOT FOUND!"));

        if ((!user.getRole().getRoleName().equals(role.getRoleName())) && user.getStatus() != Status.APPROVED) {
            throw new IllegalStateException("Your account is pending approval. Please wait for admin approval.");
        }

        return user;
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
            .map(user -> UserDTO.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .roleName(user.getRole() != null ? user.getRole().getRoleName() : "N/A")
                .isActive(Boolean.TRUE.equals(user.getIsActive()))
                .isLocked(Boolean.TRUE.equals(user.getIsLocked()))
                .createdAt(user.getCreatedAt())
                .build())
            .collect(Collectors.toList());
    }
    public void updateStatus(UserAccessRequestDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setIsActive(dto.isActive());
        user.setIsLocked(dto.isLocked());

        System.out.println("🔧 Updating user: " + user.getUserId() + " to isActive=" + dto.isActive());
        userRepository.save(user);
    }


    public User saveStudent(User user) {
        return userRepository.save(user);
    }

    public List<User> getPendingUsers() {
        return userRepository.findByStatus(Status.PENDING);
    }

    public List<User> approveUser(int id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus(Status.APPROVED);
        userRepository.save(user);

        return userRepository.findByStatus(Status.PENDING);
    }

    public List<User> rejectUser(int id) {
        userRepository.deleteById(id);
        return userRepository.findByStatus(Status.PENDING);
    }

    public List<User> deleteUser(int id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Exam with ID " + id + " not found");
        }
        userRepository.deleteById(id);
        return userRepository.findAll();
    }

   

}
