package org.example.demo4.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfileUpdateRequest {

    @Size(max = 50, message = "昵称过长")
    private String nickname;

    private String avatar;

    private String cover;

    @Size(max = 500, message = "自我介绍过长")
    private String bio;

    private String gender;

    private LocalDate birthday;

    private String phone;

    private String school;

    private String college;

    private String studentId;

    private String interests;
}
