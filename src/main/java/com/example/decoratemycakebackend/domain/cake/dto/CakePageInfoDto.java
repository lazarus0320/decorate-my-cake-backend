package com.example.decoratemycakebackend.domain.cake.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CakePageInfoDto {
    private int startPage;
    private int endPage;
    private boolean hasPrev;
    private boolean hasNext;
    private int displayPageNum;
}
