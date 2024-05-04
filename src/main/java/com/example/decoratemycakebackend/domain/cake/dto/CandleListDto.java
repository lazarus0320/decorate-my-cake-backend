package com.example.decoratemycakebackend.domain.cake.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CandleListDto {
    private Long candleId;
    private String candleName;
    private String candleTitle;
    private String candleContent;
    private String candleCreatedAt;
    private String writer;
    private boolean isPrivate;
}
