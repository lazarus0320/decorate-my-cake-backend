package com.example.decoratemycakebackend.domain.cake.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CakeViewFriendRequestDto {
    private String email;
    private int cakeCreatedYear;
    private boolean isPrivate;
}
