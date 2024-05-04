package com.example.decoratemycakebackend.domain.cake.dto;

import com.example.decoratemycakebackend.domain.cake.entity.CandleCountPermission;
import com.example.decoratemycakebackend.domain.cake.entity.CandleCreatePermission;
import com.example.decoratemycakebackend.domain.cake.entity.CandleViewPermission;
import com.example.decoratemycakebackend.domain.cake.entity.Candle;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CakeCreateResponseDto {
    private CakeSetting setting;

    private String nickname;
    private String cakeName;
    private int cakeCreatedYear;
    private List<Candle> candleList;

    @Getter
    @Builder
    public static class CakeSetting {
        private CandleCreatePermission candleCreatePermission;
        private CandleViewPermission candleViewPermission;
        private CandleCountPermission candleCountPermission;
    }
}
