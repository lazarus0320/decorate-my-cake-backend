package com.example.decoratemycakebackend.domain.cake.service;

import com.example.decoratemycakebackend.domain.cake.dto.*;
import com.example.decoratemycakebackend.domain.cake.entity.Cake;
import com.example.decoratemycakebackend.domain.cake.repository.CakeRepository;
import com.example.decoratemycakebackend.domain.candle.dto.CandleListDto;
import com.example.decoratemycakebackend.domain.member.entity.Member;
import com.example.decoratemycakebackend.domain.member.repository.MemberRepository;
import com.example.decoratemycakebackend.global.error.CustomException;
import com.example.decoratemycakebackend.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.decoratemycakebackend.global.util.BirthdayUtil.getNextBirthday;
import static com.example.decoratemycakebackend.global.util.ValidationUtil.validateCurrentEmail;

@Service
@RequiredArgsConstructor
public class CakeService {
    private static final int PAGE_SIZE = 10;
    private final CakeRepository cakeRepository;
    private final MemberRepository memberRepository;

    //전체 케이크 email로 가져오는거 creatat 필요없음
    // stream과 정적 팩토리 메서드의 사용으로 코드 개선함
    public List<CakeViewResponseDto> getCakesByEmail(String email) {
        Member member = getMember(email);

        List<Cake> cakes = cakeRepository.findAllByMemberEmail(email);

        return cakes.stream()
                .map(cake -> {
                    List<CandleListDto> candleListDto = cake.getCandles().stream()
                            .map(CandleListDto::from)
                            .collect(Collectors.toList());

                    return CakeViewResponseDto.toDto(cake, member, candleListDto, null);
                })
                .collect(Collectors.toList());
    }

    public CakePutResponseDto updateCake(CakePutRequestDto request) {
        String email = request.getEmail();
        validateCurrentEmail(email);

        Member member = getMember(email);

        Cake cake = cakeRepository.findByEmailAndCreatedYear(email, request.getCreatedYear())
                .orElseThrow(() -> new CustomException(ErrorCode.CAKE_NOT_FOUND));

        // 권한 필드 업데이트
        cake.updatePermissions(request.getCandleCreatePermission(),
                request.getCandleViewPermission(),
                request.getCandleCountPermission());

        Cake updatedCake = cakeRepository.save(cake);

        return CakePutResponseDto.builder()
                .cakeName(updatedCake.getCakeName())
                .candleCreatePermission(updatedCake.getCandleCreatePermission())
                .candleViewPermission(updatedCake.getCandleViewPermission())
                .candleCountPermission(updatedCake.getCandleCountPermission())
                .build();
    }

    public void deleteCake(CakeDeleteRequestDto request) {
        String email = request.getEmail();
        Member member = getMember(email);

        Cake cake = cakeRepository.findByEmailAndCreatedYear(email, request.getCakecreatedYear())
                .orElseThrow(() -> new CustomException(ErrorCode.CAKE_NOT_FOUND));

        cakeRepository.delete(cake);
    }

    public CakeCreateResponseDto createCake(CakeCreateRequestDto request) {

        // 프론트에서 보낸 email과 로그인 된 유저의 email 일치 여부 확인
        String email = request.getEmail();
        validateCurrentEmail(email);
        // 멤버 정보 DB에서 조회
        Member member = getMember(email);

        // 생일까지 남은기간 계산
        LocalDate today = LocalDate.now();
        LocalDate birthday = member.getBirthday();
        LocalDate nextBirthday = getNextBirthday(today, birthday);
        long daysUntilBirthday = ChronoUnit.DAYS.between(today, nextBirthday);

        // D-30보다 많이 남은 경우, 케이크 생성 불가 안내
        if (daysUntilBirthday > 30) {
            // 에러를 띄우면 아래와 같은 동적 메시지를 반환할 수 없으므로 폐기했음.
            //throw new CustomException(ErrorCode.FORBIDDEN_CREATE_CAKE);
            return CakeCreateResponseDto.builder()
                    .birthday(member.getBirthday().toString())
                    .message("생일로부터 D-" + daysUntilBirthday + "일 남았습니다. 케이크 생성은 D-30일부터 가능합니다.")
                    .build();
        }

        // 케이크 정보 생성
        Cake cake = createCake(request, member, email);
        // DB에 정보 저장후 멤버 정보 업데이트
        saveCakeAndUpdateMember(cake, member);
        // 케이크 설정 정보 생성
        return createCakeCreateResponseDto(cake);
    }

    public CakeViewResponseDto getCakeAndCandles(CakeViewRequestDto request) {
        // 친구의 케이크를 조회할 수도 있으므로 로그인 한 유저의 이메일과 일치 여부 확인하지 않음
        String email = request.getEmail();

        Member member = getMember(email);
        Optional<Cake> cake = cakeRepository.findByEmailAndCreatedYear(email, request.getCreatedYear());

        // 생일까지 남은기간 계산
        LocalDate today = LocalDate.now();
        LocalDate birthday = member.getBirthday();
        LocalDate nextBirthday = getNextBirthday(today, birthday);
        long daysUntilBirthday = ChronoUnit.DAYS.between(today, nextBirthday);
        int age = nextBirthday.getYear() - birthday.getYear();

        if (isBirthdayToday(daysUntilBirthday)) {
            return buildBirthdayCakeViewResponseDto(member, age, cake);
        } else {
            return buildBeforeBirthdayCakeViewResponseDto(member, birthday, age, daysUntilBirthday, cake);
        }
    }

    private Member getMember(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Cake createCake(CakeCreateRequestDto request, Member member, String email) {
        return Cake.builder()
                .cakeName(request.getCakeName())
                .member(member)
                .email(email)
                .createdYear(request.getCreatedYear())
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

    private CakeCreateResponseDto createCakeCreateResponseDto(Cake cake) {
        return CakeCreateResponseDto.builder()
                .candleCreatePermission(cake.getCandleCreatePermission())
                .candleViewPermission(cake.getCandleViewPermission())
                .candleCountPermission(cake.getCandleCountPermission())
                .cakeName(cake.getCakeName())
                .cakeCreatedYear(cake.getCreatedYear())
                .candleList(cake.getCandles())
                .nickname(cake.getMember().getNickname())
                .build();
    }


    private boolean isBirthdayToday(long daysUntilBirthday) {
        return daysUntilBirthday == 365;
    }


    private CakeViewResponseDto buildBirthdayCakeViewResponseDto(Member member, int age, Optional<Cake> cakeOptional) {
        return cakeOptional.map(cake -> {
            List<CandleListDto> candleList = cake.getCandles().stream()
                    .map(CandleListDto::from)
                    .collect(Collectors.toList());

            return CakeViewResponseDto.toDto(cake, member, candleList, getBirthdayMessage(member, age));
        //    케이크 없으면 만들도록 유도
        }).orElse(buildRecommendToCreateCakeDto(member, age));
    }

    private CakeViewResponseDto buildRecommendToCreateCakeDto(Member member, int age) {
        return CakeViewResponseDto.builder()
                .nickname(member.getNickname())
                .birthday(member.getBirthday().toString())
                .message(member.getNickname() + "님의 " + age + "살 생일 케이크를 만들어 보세요!")
                .build();
    }

    private CakeViewResponseDto buildBeforeBirthdayCakeViewResponseDto(Member member, LocalDate birthday, int age, long daysUntilBirthday, Optional<Cake> cakeOptional) {

        // D-Day가 30일보다 많이 남은경우, D-day 남은 날짜 반환
        if (daysUntilBirthday > 30) {
            return buildDDayMessageDto(cakeOptional, member, birthday, daysUntilBirthday);
        }
        // D-Day가 30일 이하로 남은 경우
        // 케이크 만든게 있다면 케이크의 일부 데이터만 반환
        return cakeOptional.map(cake -> buildCakeWithPartialCandleInfoDto(member, cake))
                // 케이크 안 만들었다면 케이크 만들도록 유도
                .orElse(buildRecommendToCreateCakeDto(member, age));
    }

    private CakeViewResponseDto buildDDayMessageDto(Optional<Cake> cake, Member member, LocalDate birthday, long daysUntilBirthday) {
        // D-30보다 많이 남은 경우, 케이크 생성이 안되는 것이 정상임.
        return CakeViewResponseDto.builder()
                .nickname(member.getNickname())
                .birthday(birthday.toString())
                .message("생일까지 D-" + daysUntilBirthday + " 남았습니다. 케이크 생성은 생일 D-30일부터 가능합니다.")
                .build();
    }

    private CakeViewResponseDto buildCakeWithPartialCandleInfoDto(Member member, Cake cake) {
        // from을 쓰지 않고 일부 데이터만 꺼내오기
        List<CandleListDto> candleListDto = cake.getCandles().stream()
                .map(candle -> CandleListDto.builder()
                        .candleName(candle.getName())
                        .writer(candle.getWriter())
                        .build())
                .collect(Collectors.toList());

        return CakeViewResponseDto.toDto(cake, member, candleListDto, null);
    }

    private String getBirthdayMessage(Member member, int age) {
        return member.getNickname() + "님의 " + age + "살 생일을 축하합니다!!";
    }
}