package com.example.decoratemycakebackend.domain.cake.dto;

import com.example.decoratemycakebackend.domain.cake.entity.CandleCountPermission;
import com.example.decoratemycakebackend.domain.cake.entity.CandleCreatePermission;
import com.example.decoratemycakebackend.domain.cake.entity.CandleViewPermission;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CakeCreateRequestDto {
    private String cakeName;
    private String email;
    private CandleCreatePermission candleCreatePermission;
    private CandleViewPermission candleViewPermission;
    private CandleCountPermission candleCountPermission;
}
