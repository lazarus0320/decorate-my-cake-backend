package com.example.decoratemycakebackend.domain.candle.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CandleGetResponseDto {
    private long candleId;
    private String candleName;
    private String candleTitle;
    private String candleContent;
    private LocalDate candleCreatedAt;
    private String message;
    private String writer;
    private boolean isPrivate;
    private int totalCandleCount;

}
