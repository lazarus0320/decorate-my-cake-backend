package com.example.decoratemycakebackend.domain.cake.service;

import com.example.decoratemycakebackend.domain.cake.dto.CakeViewResponseDto;
import com.example.decoratemycakebackend.domain.cake.entity.Cake;
import com.example.decoratemycakebackend.domain.cake.repository.CakeRepository;
import com.example.decoratemycakebackend.domain.candle.dto.CandleListDto;
import com.example.decoratemycakebackend.domain.friend.service.FriendRequestService;
import com.example.decoratemycakebackend.domain.member.entity.Member;
import com.example.decoratemycakebackend.domain.member.repository.MemberRepository;
import com.example.decoratemycakebackend.global.error.CustomException;
import com.example.decoratemycakebackend.global.error.ErrorCode;
import com.example.decoratemycakebackend.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.decoratemycakebackend.global.util.BirthdayUtil.getNextBirthday;

@Service
@RequiredArgsConstructor
public class FriendCakeService {

    private static final int PAGE_SIZE = 10;
    private final CakeRepository cakeRepository;
    private final MemberRepository memberRepository;
    private final FriendRequestService friendRequestService;

    public CakeViewResponseDto getCakeFromSomeone(String someoneEmail) { //친구 이메일 입력 받기, 친구 관계인지 확인 친구 아니면 예외
        Member currentMember = memberRepository.findByEmail(SecurityUtil.getCurrentUserEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        Member someone = memberRepository.findByEmail(someoneEmail)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));


        // 올해의 타인의 케이크 정보 가져오기
        Cake someoneCake = getCake(someoneEmail, LocalDateTime.now().getYear());

        // 타인의 나이 계산
        int age = getSomeoneAge(someone);
        return getCakeAndCandleData(currentMember, someone, someoneCake, age);
    }

    /** 친구의 케이크 열람시에는 생일로부터의 기간에 따라 다른 정보를 표시하는게 아니라,
        단순히 친구가 설정한 케이크 설정에 따라 데이터 조회 범위가 달라진다.
     **/
    private int getSomeoneAge(Member someone) {

        LocalDate friendBirthday = someone.getBirthday();
        LocalDate today = LocalDate.now();
        LocalDate nextBirthday = getNextBirthday(today, friendBirthday);

        return nextBirthday.getYear() - friendBirthday.getYear();

    }

    private Cake getCake(String email, int cakeCreatedYear) {
        return cakeRepository.findByEmailAndCreatedYear(email, cakeCreatedYear)
                .orElse(null);
    }

    private CakeViewResponseDto getCakeAndCandleData(Member currentMember, Member someone, Cake someoneCake, int age) {
        // 케이크가 없는 경우
        if (someoneCake == null) {
            return cakeViewResponseDtoWithMessage(someone, "친구가 아직 케이크를 만들지 않았습니다!");
        }

        switch (someoneCake.getCandleViewPermission()) {
            case ANYONE:
                return getCakeAndCandlesForSomeone(someoneCake, someone, age);
            case ONLY_FRIENDS:
                // 친구관계 확인
                if (friendRequestService.isFriend(currentMember, someone)) {
                    return getCakeAndCandlesForSomeone(someoneCake, someone, age);
                } else {
                    throw new CustomException(ErrorCode.NOT_FRIEND);
                }
            case ONLY_ME:
                return cakeViewResponseDtoWithMessage(someone, "비공개 된 케이크입니다.");
            default:
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private CakeViewResponseDto getCakeAndCandlesForSomeone(Cake someoneCake, Member someone, int age) {
        List<CandleListDto> candleList = someoneCake.getCandles().stream()
                .map(CandleListDto::from)
                .collect(Collectors.toList());

        return CakeViewResponseDto.toDtoForFriend(someoneCake, someone, candleList, getBirthdayMessage(someone, age));
    }

    private CakeViewResponseDto cakeViewResponseDtoWithMessage(Member someone, String message) {
        return CakeViewResponseDto.builder()
                .nickname(someone.getNickname())
                .birthday(someone.getBirthday().toString())
                .message(message)
                .build();
    }

    private String getBirthdayMessage(Member member, int age) {
        return member.getNickname() + "님의 " + age + "살 생일을 축하해주세요!!";
    }

}
