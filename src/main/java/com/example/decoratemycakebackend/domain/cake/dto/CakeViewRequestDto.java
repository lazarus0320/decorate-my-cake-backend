package com.example.decoratemycakebackend.domain.cake.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CakeViewRequestDto {
    @Schema(description = "이메일", example = "test1234@gmail.com", required = true)
    private String email;
    @Schema(description = "케이크 생성년도", example = "2024", required = true)
    private int createdYear;
}