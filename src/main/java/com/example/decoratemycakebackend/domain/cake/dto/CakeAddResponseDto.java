package com.example.decoratemycakebackend.domain.cake.dto;

import com.example.decoratemycakebackend.domain.cake.entity.CandleCountPermission;
import com.example.decoratemycakebackend.domain.cake.entity.CandleCreatePermission;
import com.example.decoratemycakebackend.domain.cake.entity.CandleViewPermission;
import com.example.decoratemycakebackend.domain.candle.dto.CandleListDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CakeAddResponseDto {
    private String nickname;
    private String cakeName;
    //private LocalDate createdAt;
    private int cakecreatedYear;
    private List<CandleListDto> candleList;
    private int totalCandle;
    //private int totalPage;
    //private int offset;
    private CakeSetting setting;
    //private CakePageInfoDto pageInfo;

    @Getter
    @Builder
    public static class CakeSetting {
        private CandleCreatePermission candleCreatePermission;
        private CandleViewPermission candleViewPermission;
        private CandleCountPermission candleCountPermission;
    }
}
