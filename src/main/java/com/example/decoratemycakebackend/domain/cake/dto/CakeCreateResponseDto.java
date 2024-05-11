package com.example.decoratemycakebackend.domain.cake.dto;

import com.example.decoratemycakebackend.domain.cake.entity.CandleCountPermission;
import com.example.decoratemycakebackend.domain.cake.entity.CandleCreatePermission;
import com.example.decoratemycakebackend.domain.cake.entity.CandleViewPermission;
import com.example.decoratemycakebackend.domain.candle.entity.Candle;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CakeCreateResponseDto {
    private String message;
    private String nickname;
    private String cakeName;
    private String birthday;
    private int cakeCreatedYear;
    private List<Candle> candleList;
    private CandleCreatePermission candleCreatePermission;
    private CandleViewPermission candleViewPermission;
    private CandleCountPermission candleCountPermission;

}
