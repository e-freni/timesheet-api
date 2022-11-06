package com.jaewa.timesheet.controller.dto;

import lombok.Getter;

@Getter
public class ChangePasswordDto {

    private String username;
    private String oldPassword;
    private String newPassword;

}
