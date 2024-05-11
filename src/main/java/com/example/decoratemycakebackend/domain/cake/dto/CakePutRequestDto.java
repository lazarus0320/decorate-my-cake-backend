package com.example.decoratemycakebackend.domain.cake.dto;

import com.example.decoratemycakebackend.domain.cake.entity.CandleCountPermission;
import com.example.decoratemycakebackend.domain.cake.entity.CandleCreatePermission;
import com.example.decoratemycakebackend.domain.cake.entity.CandleViewPermission;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CakePutRequestDto {
    @Schema(description = "이메일", example = "test1234@gmail.com", required = true)
    private String email;
    @Schema(description = "케이크 생성년도", example = "2024", required = true)
    private int createdYear;

    @Schema(description = "캔들 생성 허용 범위", example = "ANYONE", required = true)
    private CandleCreatePermission candleCreatePermission;
    @Schema(description = "캔들 열람 허용 범위", example = "ANYONE", required = true)
    private CandleViewPermission candleViewPermission;
    @Schema(description = "캔들 개수 열람 허용 범위", example = "ANYONE", required = true)
    private CandleCountPermission candleCountPermission;
}