package com.example.decoratemycakebackend.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AccountSettingsDto {
    private String nickname;
    private String profileImg;
}
