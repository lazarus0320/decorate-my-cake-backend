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

import static com.example.decoratemycakebackend.global.util.ValidationUtil.validateEmailMatch;

@Service
@Slf4j
@RequiredArgsConstructor
public class CakeService {
    private final CakeRepository cakeRepository;
    private final MemberRepository memberRepository;

    public CakeCreateResponseDto createCake(CakeCreateRequestDto request) {

        // 프론트에서 보낸 email과 로그인 된 유저의 email 일치 여부 확인
        String email = SecurityUtil.getCurrentUserEmail();
        validateEmailMatch(email, request.getEmail());
        // 멤버 정보 DB에서 조회
        Member member = getMember(email);
        // 케이크 정보 생성
        Cake cake = createCake(request, member, email);
        // DB에 정보 저장후 멤버 정보 업데이트
        saveCakeAndUpdateMember(cake, member);
        // 케이크 설정 정보 생성
        CakeCreateResponseDto.CakeSetting cakeSetting = createCakeSetting(cake);
        // 케이크 정보 및 설정 정보 반환
        return createCakeCreateResponseDto(cakeSetting, cake);
    }

    public CakeViewResponseDto getCakeAndCandles(CakeViewRequestDto request) {
        // 친구의 케이크를 조회할 수도 있으므로 로그인 한 유저의 이메일과 일치 여부 확인하지 않음
        String email = request.getEmail();

        Member member = getMember(email);
        Cake cake = getCake(email, request.getCakeCreatedYear());

        // 생일까지 남은기간 계산
        LocalDate today = LocalDate.now();
        LocalDate birthday = member.getBirthday();
        LocalDate nextBirthday = getNextBirthday(today, birthday);

        long daysUntilBirthday = ChronoUnit.DAYS.between(today, nextBirthday);
        int age = nextBirthday.getYear() - birthday.getYear();

        if (isBirthdayToday(daysUntilBirthday)) {
            return getBirthdayCakeViewResponseDto(member, age, cake);
        } else {
            return getBeforeBirthdayCakeViewResponseDto(member, birthday, age, daysUntilBirthday, cake);
        }
    }

    private Member getMember(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Cake createCake(CakeCreateRequestDto request, Member member, String email) {
        return Cake.builder()
                .name(request.getCakeName())
                .member(member)
                .email(email)
                .createdYear(LocalDateTime.now().getYear())
                .candleCreatePermission(request.getCandleCreatePermission())
                .candleViewPermission(request.getCandleViewPermission())
                .candleCountPermission(request.getCandleCountPermission())
                .candles(Collections.emptyList())
                .build();
    }

    private void saveCakeAndUpdateMember(Cake cake, Member member) {
        cakeRepository.save(cake);
        member.getCakes().add(cake);
        memberRepository.save(member);
    }

    private CakeCreateResponseDto.CakeSetting createCakeSetting(Cake cake) {
        return CakeCreateResponseDto.CakeSetting.builder()
                .candleCreatePermission(cake.getCandleCreatePermission())
                .candleViewPermission(cake.getCandleViewPermission())
                .candleCountPermission(cake.getCandleCountPermission())
                .build();
    }

    private CakeCreateResponseDto createCakeCreateResponseDto(CakeCreateResponseDto.CakeSetting cakeSetting, Cake cake) {
        return CakeCreateResponseDto.builder()
                .setting(cakeSetting)
                .cakeName(cake.getName())
                .cakeCreatedYear(cake.getCreatedYear())
                .candleList(cake.getCandles())
                .nickname(cake.getMember().getNickname())
                .build();
    }

    private Cake getCake(String email, int cakeCreatedYear) {
        return cakeRepository.findByEmailAndCreatedYear(email, cakeCreatedYear)
                .orElse(null);
    }

    private LocalDate getNextBirthday(LocalDate today, LocalDate birthday) {
        LocalDate thisYearBirthday = LocalDate.of(today.getYear(), birthday.getMonthValue(), birthday.getDayOfMonth());
        return today.isBefore(thisYearBirthday) ? thisYearBirthday : thisYearBirthday.plusYears(1);
    }

    private boolean isBirthdayToday(long daysUntilBirthday) {
        return daysUntilBirthday == 365;
    }

    private CakeViewResponseDto getBirthdayCakeViewResponseDto(Member member, int age, Cake cake) {
        // 생일 당일인데 케이크 없으면 만들도록 유도
        if (cake == null) {
            return CakeViewResponseDto.builder()
                    .nickname(member.getNickname())
                    .birthday(member.getBirthday().toString())
                    .message(recommandToCreateCake(member, age))
                    .build();
        }
        // 생일 당일에 케이크가 있으면 모든 캔들 정보 공개
        List<CandleListDto> candleList = cake.getCandles().stream()
                .map(this::toCandleListDto)
                .collect(Collectors.toList());

        CakeViewResponseDto.CakeSetting cakeSetting = createCakeViewResponseDtoCakeSetting(cake);

        return CakeViewResponseDto.builder()
                .message(getBirthdayMessage(member, age))
                .nickname(member.getNickname())
                .cakeName(cake.getName())
                .birthday(member.getBirthday().toString())
                .cakeCreatedYear(cake.getCreatedYear())
                .candleList(candleList)
                .setting(cakeSetting)
                .build();
    }

    private CakeViewResponseDto getBeforeBirthdayCakeViewResponseDto(Member member, LocalDate birthday, int age, long daysUntilBirthday, Cake cake) {
        // D-Day가 30일보다 많이 남은경우
        if (daysUntilBirthday > 30) {
            return CakeViewResponseDto.builder()
                    .nickname(member.getNickname())
                    .birthday(birthday.toString())
                    .message(getDDayMessage(daysUntilBirthday))
                    .build();
        }
        // D-Day가 30일 이하로 남은 경우
        // 케이크 안 만들었다면 케이크 만들도록 유도
        if (cake == null) {
            return CakeViewResponseDto.builder()
                    .nickname(member.getNickname())
                    .birthday(birthday.toString())
                    .message(recommandToCreateCake(member, age))
                    .build();
        }
        // 케이크 만들었다면 캔들 정보 일부만 공개
        List<CandleListDto> candleList = cake.getCandles().stream()
                .map(candle -> CandleListDto.builder()
                        .candleName(candle.getName())
                        .writer(candle.getWriter())
                        .build())
                .collect(Collectors.toList());

        CakeViewResponseDto.CakeSetting cakeSetting = createCakeViewResponseDtoCakeSetting(cake);

        return CakeViewResponseDto.builder()
                .nickname(member.getNickname())
                .cakeName(cake.getName())
                .birthday(member.getBirthday().toString())
                .cakeCreatedYear(cake.getCreatedYear())
                .candleList(candleList)
                .setting(cakeSetting)
                .build();
    }

    private CakeViewResponseDto.CakeSetting createCakeViewResponseDtoCakeSetting(Cake cake) {
        return CakeViewResponseDto.CakeSetting.builder()
                .candleCreatePermission(cake.getCandleCreatePermission())
                .candleViewPermission(cake.getCandleViewPermission())
                .candleCountPermission(cake.getCandleCountPermission())
                .build();
    }

    private String recommandToCreateCake(Member member, int age) {
        return member.getNickname() + "님의 " + age + "살 생일 케이크를 만들어 보세요!";
    }

    private String getBirthdayMessage(Member member, int age) {
        return member.getNickname() + "님의 " + age + "살 생일을 축하합니다!!";
    }

    private String getDDayMessage(long daysUntilBirthday) {
        return "생일까지 D-" + daysUntilBirthday + "일 남았습니다!";
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