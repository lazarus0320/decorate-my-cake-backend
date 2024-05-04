package com.example.decoratemycakebackend.domain.cake.dto;

import com.example.decoratemycakebackend.domain.cake.entity.CandleCountPermission;
import com.example.decoratemycakebackend.domain.cake.entity.CandleCreatePermission;
import com.example.decoratemycakebackend.domain.cake.entity.CandleViewPermission;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CakeViewResponseDto {
    private String nickname;
    private String message;
    private String cakeName;
    private String birthday;
    private int cakeCreatedYear;
    private List<CandleListDto> candleList;
    private CakeSetting setting;

    @Getter
    @Builder
    public static class CakeSetting {
        private CandleCreatePermission candleCreatePermission;
        private CandleViewPermission candleViewPermission;
        private CandleCountPermission candleCountPermission;
    }
}