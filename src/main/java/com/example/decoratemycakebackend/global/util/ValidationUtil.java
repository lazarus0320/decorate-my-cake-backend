package com.example.decoratemycakebackend.global.util;

import com.example.decoratemycakebackend.global.error.CustomException;
import com.example.decoratemycakebackend.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ValidationUtil {

    public static void validateCurrentEmail(String email) {
        // 지금 시큐리티에 올라가있는 이메일과 같은지 확인
        if (!email.equals(SecurityUtil.getCurrentUserEmail())) {
            throw new CustomException(ErrorCode.ACCOUNT_NOT_MATCHED);
        }
    }
}