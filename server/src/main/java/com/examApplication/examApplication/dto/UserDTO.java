package com.examApplication.examApplication.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {
    private int userId;
    private String name;
    private String email;
    private String roleName;
    private boolean isActive;
    private boolean isLocked;
    private Date createdAt;
}
