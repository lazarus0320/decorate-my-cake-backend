package com.example.decoratemycakebackend.domain.candle.dto;

import com.example.decoratemycakebackend.domain.candle.entity.Candle;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandleListDto {
    private Long candleId;
    private String candleName;
    private String candleTitle;
    private String candleContent;
    private String candleCreatedAt;
    private String writer;
    private boolean isPrivate;

    // Entity -> Dto
    public static CandleListDto from(Candle candle) {
        return new CandleListDto(
                candle.getId(),
                candle.getName(),
                candle.getTitle(),
                candle.getContent(),
                candle.getCreatedAt().toString(),
                candle.getWriter(),
                candle.isPrivate()
        );
    }

}
