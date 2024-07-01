package com.example.decoratemycakebackend.domain.cake.dto;

import com.example.decoratemycakebackend.domain.cake.entity.*;
import com.example.decoratemycakebackend.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@Setter
public class CakeViewResponseDto {
    private String message;
    private String nickname;
    private CakeName cakeName;
    private String birthday;
    private String cakeUrl;
    private Integer candleCount;
    private Integer cakeCreatedYear;
    private Integer dDay;
    private CandleCreatePermission candleCreatePermission;
    private CandleViewPermission candleViewPermission;
    private CandleCountPermission candleCountPermission;

    public static CakeViewResponseDto toDto(Cake cake, Member member, String message, Integer dDay) {

        return CakeViewResponseDto.builder()
                .message(message)
                .nickname(member.getNickname())
                .cakeName(cake.getCakeName())
                .cakeUrl(cake.getCakeUrl())
                .birthday(member.getBirthday().toString())
                .dDay(dDay)
                .candleCount(cake.getCandles().size())
                .cakeCreatedYear(cake.getCreatedYear())
                .candleCreatePermission(cake.getCandleCreatePermission())
                .candleViewPermission(cake.getCandleViewPermission())
                .candleCountPermission(cake.getCandleCountPermission())
                .build();
    }

    public static CakeViewResponseDto toDtoForFriend(Cake cake, Member member, String message, Integer dDay) {
        int candleCount = cake.getCandleCountPermission() == CandleCountPermission.ANYONE ? cake.getCandles().size() : -1;

        return CakeViewResponseDto.builder()
                .message(message)
                .cakeUrl(cake.getCakeUrl())
                .nickname(member.getNickname())
                .cakeName(cake.getCakeName())
                .birthday(member.getBirthday().toString())
                .dDay(dDay)
                .candleCount(candleCount)
                .cakeCreatedYear(cake.getCreatedYear())
                .candleCreatePermission(cake.getCandleCreatePermission())
                .candleViewPermission(cake.getCandleViewPermission())
                .candleCountPermission(cake.getCandleCountPermission())
                .build();
    }
}