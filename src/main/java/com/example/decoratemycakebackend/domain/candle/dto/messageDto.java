package com.example.decoratemycakebackend.domain.candle.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class messageDto {
    private String message;
    private Page<CandleListDto> candleList;

}
