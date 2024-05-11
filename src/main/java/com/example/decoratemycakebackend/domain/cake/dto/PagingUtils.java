package com.example.decoratemycakebackend.domain.cake.dto;

public class PagingUtils {
    public static int totalPages(int totalElements, int elementsPerPage) {
        return ((totalElements - 1) / elementsPerPage) + 1;
    }

    public static int endPage(int currentPage, int displayPageNum, int totalPages) {
        int endPage = (((currentPage - 1) / displayPageNum) + 1) * displayPageNum;
        return Math.min(endPage, totalPages);
    }

    public static int startPage(int currentPage, int displayPageNum) {
        return ((currentPage - 1) / displayPageNum) * displayPageNum + 1;
    }

    public static boolean hasPrev(int startPage) {
        return startPage != 1;
    }

    public static boolean hasNext(int endPage, int totalPages) {
        return endPage != totalPages;
    }
}