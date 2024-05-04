package com.example.decoratemycakebackend.domain.cake.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CakeViewRequestDto {
    private String email;
    private int cakeCreatedYear;
}
