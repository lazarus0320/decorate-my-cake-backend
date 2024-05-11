package com.example.decoratemycakebackend.global.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponsePDto<T> {
    private final String message;
    private final T data;
    private final T pageInfo;
}
