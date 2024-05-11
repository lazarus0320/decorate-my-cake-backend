package com.example.decoratemycakebackend.domain.cake.dto;

import com.example.decoratemycakebackend.domain.cake.entity.Cake;
import com.example.decoratemycakebackend.domain.cake.entity.CandleCountPermission;
import com.example.decoratemycakebackend.domain.cake.entity.CandleCreatePermission;
import com.example.decoratemycakebackend.domain.cake.entity.CandleViewPermission;
import com.example.decoratemycakebackend.domain.candle.dto.CandleListDto;
import com.example.decoratemycakebackend.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Builder
@Setter
public class CakeViewResponseDto {
    private String message;
    private String nickname;
    private String cakeName;
    private String birthday;
    private int candleCount;
    private int cakeCreatedYear;
    private List<CandleListDto> candleList;
    private CandleCreatePermission candleCreatePermission;
    private CandleViewPermission candleViewPermission;
    private CandleCountPermission candleCountPermission;

    public static CakeViewResponseDto toDto(Cake cake, Member member, List<CandleListDto> candleList, String message) {

        return CakeViewResponseDto.builder()
                .message(message)
                .nickname(member.getNickname())
                .cakeName(cake.getCakeName())
                .birthday(member.getBirthday().toString())
                .candleCount(cake.getCandles().size())
                .cakeCreatedYear(cake.getCreatedYear())
                .candleList(candleList)
                .candleCreatePermission(cake.getCandleCreatePermission())
                .candleViewPermission(cake.getCandleViewPermission())
                .candleCountPermission(cake.getCandleCountPermission())
                .build();
    }

    public static CakeViewResponseDto toDtoForFriend(Cake cake, Member member, List<CandleListDto> candleList, String message) {
        int candleCount = cake.getCandleCountPermission() == CandleCountPermission.ANYONE ? cake.getCandles().size() : -1;

        return CakeViewResponseDto.builder()
                .message(message)
                .nickname(member.getNickname())
                .cakeName(cake.getCakeName())
                .birthday(member.getBirthday().toString())
                .candleCount(candleCount)
                .cakeCreatedYear(cake.getCreatedYear())
                .candleList(candleList)
                .candleCreatePermission(cake.getCandleCreatePermission())
                .candleViewPermission(cake.getCandleViewPermission())
                .candleCountPermission(cake.getCandleCountPermission())
                .build();
    }
}
