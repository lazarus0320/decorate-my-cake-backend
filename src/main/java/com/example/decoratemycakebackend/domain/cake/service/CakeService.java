package com.example.decoratemycakebackend.domain.cake.service;

import com.example.decoratemycakebackend.domain.cake.dto.*;
import com.example.decoratemycakebackend.domain.cake.entity.Cake;
import com.example.decoratemycakebackend.domain.cake.repository.CakeRepository;
import com.example.decoratemycakebackend.domain.member.entity.Member;
import com.example.decoratemycakebackend.domain.member.repository.MemberRepository;
import com.example.decoratemycakebackend.global.error.CustomException;
import com.example.decoratemycakebackend.global.error.ErrorCode;
import com.example.decoratemycakebackend.global.s3.S3Service;
import com.example.decoratemycakebackend.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.decoratemycakebackend.global.util.BirthdayUtil.getNextBirthday;

@Service
@RequiredArgsConstructor
public class CakeService {
    private final CakeRepository cakeRepository;
    private final MemberRepository memberRepository;
    private final S3Service s3Service;

    //전체 케이크 email로 가져오는거 creatat 필요없음
    // stream과 정적 팩토리 메서드의 사용으로 코드 개선함
    public List<CakeViewResponseDto> getAllCakesByEmail() {
        String email = SecurityUtil.getCurrentUserEmail();
        Member member = getMember(email);

        List<Cake> cakes = cakeRepository.findAllByMemberEmail(email);

        return cakes.stream()
                .map(cake -> CakeViewResponseDto.toDto(cake, member, null, null))
                .collect(Collectors.toList());
    }

    public CakePutResponseDto updateCake(CakePutRequestDto request) {
        String email = SecurityUtil.getCurrentUserEmail();

        Member member = getMember(email);

        Cake cake = cakeRepository.findByEmailAndCreatedYear(email, request.getCreatedYear())
                .orElseThrow(() -> new CustomException(ErrorCode.CAKE_NOT_FOUND));

        // S3로부터 케이크 이미지 URL 가져오기
        String imageUrl = s3Service.getImageUrl(String.valueOf(request.getCakeName()));

        // 권한 필드 업데이트
        cake.updatePermissions(request, imageUrl);

        Cake updatedCake = cakeRepository.save(cake);

        return CakePutResponseDto.builder()
                .cakeName(updatedCake.getCakeName())
                .cakeUrl(imageUrl)
                .candleCreatePermission(updatedCake.getCandleCreatePermission())
                .candleViewPermission(updatedCake.getCandleViewPermission())
                .candleCountPermission(updatedCake.getCandleCountPermission())
                .build();
    }

    public void deleteCake(CakeDeleteRequestDto request) {
        String email = SecurityUtil.getCurrentUserEmail();
        Member member = getMember(email);

        Cake cake = cakeRepository.findByEmailAndCreatedYear(email, request.getCakeCreatedYear())
                .orElseThrow(() -> new CustomException(ErrorCode.CAKE_NOT_FOUND));

        cakeRepository.delete(cake);

    }

    public CakeCreateResponseDto createCake(CakeCreateRequestDto request) {

        String email = SecurityUtil.getCurrentUserEmail();
        // 멤버 정보 DB에서 조회
        Member member = getMember(email);

        // 생일까지 남은기간 계산
        Integer daysUntilBirthday = calculateDaysUntilBirthday(LocalDate.now(), member.getBirthday());
        // 다음 생일의 연도 계산
        int nextBirthdayYear = LocalDate.now().plusDays(daysUntilBirthday).getYear();

        // 이미 해당 년도에 생성한 케이크가 있는지 확인
        cakeRepository.findByEmailAndCreatedYear(email, nextBirthdayYear)
                .ifPresent(cake -> {
                    throw new CustomException(ErrorCode.ALREADY_CREATED_CAKE);
                });

        // D-30보다 많이 남은 경우, 케이크 생성 불가 안내
        if (daysUntilBirthday > 30) {
            return CakeCreateResponseDto.builder()
                    .birthday(member.getBirthday().toString())
                    .dDay(daysUntilBirthday)
                    .message("생일로부터 D-" + daysUntilBirthday + "일 남았습니다. 케이크 생성은 D-30일부터 가능합니다.")
                    .build();
        }

        // S3로부터 케이크 이미지 URL 가져오기
        String imageUrl = s3Service.getImageUrl(String.valueOf(request.getCakeName()));

        // 케이크 정보 생성
        Cake cake = createCake(request, member, email, nextBirthdayYear, imageUrl);
        // DB에 정보 저장후 멤버 정보 업데이트
        saveCakeAndUpdateMember(cake, member);
        // 케이크 설정 정보 생성
        return createCakeCreateResponseDto(cake, daysUntilBirthday);
    }

    public CakeViewResponseDto getCakeData(Integer createdYear) {
        // 로그인 한 유저의 케이크 정보를 가져오는 기능임.
        String email = SecurityUtil.getCurrentUserEmail();

        Member member = getMember(email);

        // 프론트가 createdYear 입력시 -> 해당 연도 케이크 정보 반환
        // 미입력시 -> 가장 최신 연도의 케이크 정보 가져오기
        Optional<Cake> cake = (createdYear != null)
                ? cakeRepository.findByEmailAndCreatedYear(email, createdYear)
                : cakeRepository.findLatestCakeByEmail(email);

        // 생일까지 남은기간 계산
        LocalDate birthday = member.getBirthday();
        Integer daysUntilBirthday = calculateDaysUntilBirthday(LocalDate.now(), member.getBirthday());
        int age = calculateAge(birthday, LocalDate.now());

        if (isBirthdayToday(daysUntilBirthday)) {
            return buildBirthdayCakeViewResponseDto(member, age, cake, daysUntilBirthday);
        } else {
            return buildBeforeBirthdayCakeViewResponseDto(member, birthday, age, daysUntilBirthday, cake);
        }
    }

    private Integer calculateDaysUntilBirthday(LocalDate today, LocalDate birthday) {
        LocalDate nextBirthday = getNextBirthday(today, birthday);
        return (int)ChronoUnit.DAYS.between(today, nextBirthday);
    }

    private int calculateAge(LocalDate birthday, LocalDate today) {
        LocalDate nextBirthday = getNextBirthday(today, birthday);
        return nextBirthday.getYear() - birthday.getYear();
    }

    private Member getMember(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Cake createCake(CakeCreateRequestDto request, Member member, String email, int createdYear, String imageUrl) {
        return Cake.builder()
                .cakeName(request.getCakeName())
                .member(member)
                .email(email)
                .cakeUrl(imageUrl)
                .createdYear(createdYear)
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

    private CakeCreateResponseDto createCakeCreateResponseDto(Cake cake, Integer daysUntilBirthday) {
        return CakeCreateResponseDto.builder()
                .cakeUrl(cake.getCakeUrl())
                .candleCreatePermission(cake.getCandleCreatePermission())
                .candleViewPermission(cake.getCandleViewPermission())
                .candleCountPermission(cake.getCandleCountPermission())
                .cakeName(cake.getCakeName())
                .dDay(daysUntilBirthday)
                .cakeCreatedYear(cake.getCreatedYear())
                .nickname(cake.getMember().getNickname())
                .build();
    }


    private boolean isBirthdayToday(long daysUntilBirthday) {
        return daysUntilBirthday == 365;
    }


    private CakeViewResponseDto buildBirthdayCakeViewResponseDto(Member member, int age, Optional<Cake> cakeOptional, Integer daysUntilBirthday) {
        return cakeOptional.map(cake -> {
            return CakeViewResponseDto.toDto(cake, member, getBirthdayMessage(member, age), daysUntilBirthday);
            //    케이크 없으면 만들도록 유도
        }).orElse(buildRecommendToCreateCakeDto(member, age, daysUntilBirthday));
    }

    private CakeViewResponseDto buildRecommendToCreateCakeDto(Member member, int age, Integer daysUntilBirthday) {
        return CakeViewResponseDto.builder()
                .nickname(member.getNickname())
                .birthday(member.getBirthday().toString())
                .dDay(daysUntilBirthday)
                .message(member.getNickname() + "님의 " + age + "살 생일 케이크를 만들어 보세요!")
                .build();
    }

    private CakeViewResponseDto buildBeforeBirthdayCakeViewResponseDto(Member member, LocalDate birthday, int age, Integer daysUntilBirthday, Optional<Cake> cakeOptional) {

        // D-Day가 30일보다 많이 남은경우, D-day 남은 날짜 반환
        if (daysUntilBirthday > 30) {
            return buildDDayMessageDto(member, birthday, daysUntilBirthday);
        }
        // TODO: 캔들 열람 기능은 candle 부분으로 이동되어야 함.
        // D-Day가 30일 이하로 남은 경우
        // 케이크 만든게 있다면 케이크의 일부 데이터만 반환
        return cakeOptional.map(cake -> buildCakeWithPartialCandleInfoDto(member, cake, daysUntilBirthday))
                // 케이크 안 만들었다면 케이크 만들도록 유도
                .orElse(buildRecommendToCreateCakeDto(member, age, daysUntilBirthday));
    }

    private CakeViewResponseDto buildDDayMessageDto(Member member, LocalDate birthday, Integer daysUntilBirthday) {
        // D-30보다 많이 남은 경우, 케이크 생성이 안되는 것이 정상임.
        return CakeViewResponseDto.builder()
                .nickname(member.getNickname())
                .dDay(daysUntilBirthday)
                .birthday(birthday.toString())
                .message("생일까지 D-" + daysUntilBirthday + " 남았습니다. 케이크 생성은 생일 D-30일부터 가능합니다.")
                .build();
    }

    private CakeViewResponseDto buildCakeWithPartialCandleInfoDto(Member member, Cake cake, Integer daysUntilBirthday) {
        // from을 쓰지 않고 일부 데이터만 꺼내오기
//        List<CandleListDto> candleListDto = cake.getCandles().stream()
//                .map(candle -> CandleListDto.builder()
//                        .candleName(candle.getCandleName())
//                        .writer(candle.getWriter())
//                        .build())
//                .collect(Collectors.toList());

        return CakeViewResponseDto.toDto(cake, member, null, daysUntilBirthday);
    }

    private String getBirthdayMessage(Member member, int age) {
        return member.getNickname() + "님의 " + age + "살 생일을 축하합니다!!";
    }
}