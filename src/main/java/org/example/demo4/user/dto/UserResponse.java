package org.example.demo4.user.dto;

import lombok.Data;
import org.example.demo4.user.User;
import org.example.demo4.user.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String cover;
    private String bio;
    private String gender;
    private LocalDate birthday;
    private String phone;
    private String school;
    private String college;
    private String studentId;
    private String interests;
    private UserRole role;
    private Long storeId;
    private LocalDateTime createdAt;

    public static UserResponse from(User u) {
        UserResponse r = new UserResponse();
        r.setId(u.getId());
        r.setUsername(u.getUsername());
        r.setNickname(u.getNickname());
        r.setAvatar(u.getAvatar());
        r.setCover(u.getCover());
        r.setBio(u.getBio());
        r.setGender(u.getGender());
        r.setBirthday(u.getBirthday());
        r.setPhone(u.getPhone());
        r.setSchool(u.getSchool());
        r.setCollege(u.getCollege());
        r.setStudentId(u.getStudentId());
        r.setInterests(u.getInterests());
        r.setRole(u.getRole());
        r.setStoreId(u.getStoreId());
        r.setCreatedAt(u.getCreatedAt());
        return r;
    }
}
