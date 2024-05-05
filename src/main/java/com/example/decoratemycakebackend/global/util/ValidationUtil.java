package com.example.decoratemycakebackend.global.util;

import com.example.decoratemycakebackend.global.error.CustomException;
import com.example.decoratemycakebackend.global.error.ErrorCode;

public class ValidationUtil {

    public static void validateEmailMatch(String email, String requestEmail) {
        if (!email.equals(requestEmail)) {
            throw new CustomException(ErrorCode.ACCOUNT_NOT_MATCHED);
        }
    }
}
