package com.example.decoratemycakebackend.domain.cake.service;

import com.example.decoratemycakebackend.domain.cake.dto.*;
import com.example.decoratemycakebackend.domain.cake.entity.Cake;
import com.example.decoratemycakebackend.domain.cake.entity.Candle;
import com.example.decoratemycakebackend.domain.cake.repository.CakeRepository;
import com.example.decoratemycakebackend.domain.member.entity.Member;
import com.example.decoratemycakebackend.domain.member.repository.MemberRepository;
import com.example.decoratemycakebackend.global.error.CustomException;
import com.example.decoratemycakebackend.global.error.ErrorCode;
import com.example.decoratemycakebackend.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CakeService {
    private final CakeRepository cakeRepository;
    private final MemberRepository memberRepository;

    public CakeCreateResponseDto createCake(CakeCreateRequestDto request) {

        String email = SecurityUtil.getCurrentUserEmail();

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Cake cake = Cake.builder()
                .name(request.getCakeName())
                .member(member)
                .email(email)
                .createdYear(LocalDateTime.now().getYear())
                .candleCreatePermission(request.getCandleCreatePermission())
                .candleViewPermission(request.getCandleViewPermission())
                .candleCountPermission(request.getCandleCountPermission())
                .candles(Collections.emptyList())
                .build();

        // Cake 저장
        cakeRepository.save(cake);
        // Cake 정보를 member에 매핑
        member.getCakes().add(cake);
        // 멤버 업데이트 정보 저장
        memberRepository.save(member);

        CakeCreateResponseDto.CakeSetting cakeSetting = CakeCreateResponseDto.CakeSetting.builder()
                .candleCreatePermission(cake.getCandleCreatePermission())
                .candleViewPermission(cake.getCandleViewPermission())
                .candleCountPermission(cake.getCandleCountPermission())
                .build();


        return CakeCreateResponseDto.builder()
                .setting(cakeSetting)
                .cakeName(cake.getName())
                .cakeCreatedYear(cake.getCreatedYear())
                .candleList(cake.getCandles())
                .nickname(cake.getMember().getNickname())
                .build();
    }

    public CakeViewResponseDto getCakeAndCandles(CakeViewRequestDto request) {

        // member 정보 불러오기
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // cake 정보 불러오기, 없으면 null
        Cake cake = cakeRepository.findByEmailAndCreatedYear(member.getEmail(), request.getCakeCreatedYear())
                .orElse(null);

        // 현재 날짜와 생일까지 남은 일 수 계산
        LocalDate today = LocalDate.now();
        LocalDate birthday = member.getBirthday();
        LocalDate thisYearBirthday = LocalDate.of(today.getYear(), birthday.getMonthValue(), birthday.getDayOfMonth());

        // 오늘 날짜가 올해의 생일 이전인 경우 올해의 생일 사용, 그렇지 않으면 내년의 생일 사용
        LocalDate nextBirthday = today.isBefore(thisYearBirthday) ? thisYearBirthday : thisYearBirthday.plusYears(1);

        long daysUntilBirthday = ChronoUnit.DAYS.between(today, nextBirthday);
        int age = nextBirthday.getYear() - member.getBirthday().getYear();
        log.debug("birthday: {}", age);
        log.debug("birthday: {}", birthday);
        log.debug("nextBirthday: {}", nextBirthday);
        log.debug("today: {}", today);
        log.debug("daysUntilBirthday: {}", daysUntilBirthday);
        // 생일 당일이 아닌 경우
        if (daysUntilBirthday != 365) {
            // 생일까지 30일 남게 남은 경우(케이크 생성 불가)
            if (daysUntilBirthday > 30) {
                return CakeViewResponseDto.builder()
                        .nickname(member.getNickname())
                        .birthday(birthday.toString())
                        .message("생일까지 D-" + daysUntilBirthday + "일 남았습니다!")
                        .build();
            }
            // 30일 이하로 남은 경우


            // 케이크가 없는 경우
            if (cake == null) {
                return CakeViewResponseDto.builder()
                        .nickname(member.getNickname())
                        .birthday(birthday.toString())
                        .message(member.getNickname() + "님의 " + age + "살 생일 케이크를 만들어 보세요!")
                        .build();
            }

            // 케이크가 있는 경우: 케이크 공개하되, 캔들은 그림과 작성자 정보만 공개
            List<CandleListDto> candleList = cake.getCandles().stream()
                    .map(candle -> CandleListDto.builder()
                            .candleName(candle.getName())
                            .writer(candle.getWriter())
                            .build())
                    .collect(Collectors.toList());

            CakeViewResponseDto.CakeSetting cakeSetting = CakeViewResponseDto.CakeSetting.builder()
                    .candleCreatePermission(cake.getCandleCreatePermission())
                    .candleViewPermission(cake.getCandleViewPermission())
                    .candleCountPermission(cake.getCandleCountPermission())
                    .build();

            return CakeViewResponseDto.builder()
                    .nickname(member.getNickname())
                    .cakeName(cake.getName())
                    .birthday(member.getBirthday().toString())
                    .cakeCreatedYear(cake.getCreatedYear())
                    .candleList(candleList)
                    .setting(cakeSetting)
                    .build();
        }

        // 생일 당일인 경우: 모든 정보 공개

        // 케이크가 없는 경우
        if (cake == null) {
            return CakeViewResponseDto.builder()
                    .nickname(member.getNickname())
                    .birthday(birthday.toString())
                    .message(member.getNickname() + "님의 " + age + "살 생일 케이크를 만들어 보세요!")
                    .build();
        }

        List<CandleListDto> candleList = cake.getCandles().stream()
                .map(this::toCandleListDto)
                .collect(Collectors.toList());

        CakeViewResponseDto.CakeSetting cakeSetting = CakeViewResponseDto.CakeSetting.builder()
                .candleCreatePermission(cake.getCandleCreatePermission())
                .candleViewPermission(cake.getCandleViewPermission())
                .candleCountPermission(cake.getCandleCountPermission())
                .build();

        return CakeViewResponseDto.builder()
                .message(member.getNickname() + "님의 " + age + "살 생일을 축하합니다!!")
                .nickname(member.getNickname())
                .cakeName(cake.getName())
                .birthday(member.getBirthday().toString())
                .cakeCreatedYear(cake.getCreatedYear())
                .candleList(candleList)
                .setting(cakeSetting)
                .build();
    }

    private CandleListDto toCandleListDto(Candle candle) {
        return CandleListDto.builder()
                .candleId(candle.getId())
                .candleName(candle.getName())
                .candleTitle(candle.getTitle())
                .candleContent(candle.getContent())
                .candleCreatedAt(candle.getCreatedAt().toString())
                .writer(candle.getWriter())
                .isPrivate(Boolean.parseBoolean(candle.getIsPrivate()))
                .build();
    }
}
