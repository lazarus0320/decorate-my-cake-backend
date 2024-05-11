package com.example.decoratemycakebackend.domain.member.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberCakeDto {

    private Long id;
    private String email;
    private String nickname;
    private LocalDate birthday;


}


/*{
    "data": {
        "nickname": "민지훈",
        "email" : "astar5327@naver.com",
        "cakeName": "white_cake",
        "birthday": "1997-03-20",
        "cakeCreatedYear": 2024,
        "candleList": [
            {
                "candleId" : 21L,
                "candleName": "yellow_candle",
                "candleTitle" : "누구야 생일축하해!",
                "candleContent" : "캔들 내용 블라블라",
                "candleCreatedAt" : "2024-02-23",
                "writer" : "오예진",
                "isPrivate" : true
            },
            {
                "candleId" : 22L,
                "candleName": "blue_candle",
                "candleTitle" : "누구야 생일축하해!",
                "candleContent" : "캔들 내용 블라블라",
                "candleCreatedAt" : "2024-02-23",
                "writer" : "남윤서",
                "isPrivate" : false
            },
            {
                "candleId" : 23L,
                "candleName": "green_candle",
                "candleTitle" : "누구야 생일축하해!",
                "candleContent" : "캔들 내용 블라블라",
                "candleCreatedAt" : "2024-02-23",
                "writer" : "김동희",
                "isPrivate" : true
            }
        ],

    },
    "setting": {
        "candleMakePermission": "anyone",
        "candleViewPermission": "only me",
        "candleCountPermission": true
    }
}*/