package com.example.decoratemycakebackend.domain.cake.controller;

import com.example.decoratemycakebackend.domain.cake.dto.CakeCreateRequestDto;
import com.example.decoratemycakebackend.domain.cake.dto.CakeDeleteRequestDto;
import com.example.decoratemycakebackend.domain.cake.dto.CakePutRequestDto;
import com.example.decoratemycakebackend.domain.cake.dto.CakeViewRequestDto;
import com.example.decoratemycakebackend.domain.cake.service.CakeService;
import com.example.decoratemycakebackend.domain.cake.service.FriendCakeService;
import com.example.decoratemycakebackend.global.util.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "케이크 관리 API", description = "케이크 관리 API endpoints")
@RestController
@RequestMapping("/cakes")
@RequiredArgsConstructor
public class CakeController {

    private final CakeService cakeService;
    private final FriendCakeService friendCakeService;


    @Operation(summary = "나의 역대 케이크 전체 조회", description = "각 연도별 케이크 목록 조회")
    @GetMapping("/list")
    public ResponseEntity<ResponseDto<?>> getCakes(@RequestParam String email) {
        return ResponseEntity.ok(new ResponseDto<>("나의 역대 케이크 조회가 완료되었습니다.", cakeService.getCakesByEmail(email)));
    }

    @Operation(summary = "케이크 수정", description = "케이크 수정<br>" +
            "캔들 생성 허용 범위: ANYONE, ONLY_FRIENDS<br>" +
            "캔들 열람 허용 범위: ONLY_ME, ONLY_FRIENDS, ANYONE<br>" +
            "캔들 개수 열람 허용 범위: ANYONE, ONLY_ME")
    @PutMapping("/put")
    public ResponseEntity<ResponseDto<?>> updateCake(@RequestBody CakePutRequestDto requestDto) {
        return ResponseEntity.ok(new ResponseDto<>("케이크 설정을 수정했습니다!", cakeService.updateCake(requestDto)));
    }

    @Operation(summary = "케이크 제거", description = "케이크 제거")
    @DeleteMapping("/delete")
    public ResponseEntity<ResponseDto<?>> deleteCake(@RequestBody CakeDeleteRequestDto requestDto) {
        cakeService.deleteCake(requestDto);
        return ResponseEntity.ok(new ResponseDto<>("케이크 삭제가 완료되었습니다.", null));
    }

    @Operation(summary = "케이크 생성", description = "케이크 생성<br>" +
            "캔들 생성 허용 범위: ANYONE, ONLY_FRIENDS<br>" +
            "캔들 열람 허용 범위: ONLY_ME, ONLY_FRIENDS, ANYONE<br>" +
            "캔들 개수 열람 허용 범위: ANYONE, ONLY_ME<br>" +
            "케이크가 생성 실패의 경우 메시지 출력 예시: 생일까지 D-313 남았습니다. 케이크 생성은 생일 D-30일부터 가능합니다.")
    @PostMapping("/create")
    public ResponseEntity<ResponseDto<?>> createCake(@Valid @RequestBody CakeCreateRequestDto request) {
        return ResponseEntity.ok(new ResponseDto<>("케이크 생성이 완료되었습니다.", cakeService.createCake(request)));
    }

    @Operation(summary = "단일 케이크와 하위 캔들 정보 열람", description = "특정 연도 케이크에 대한 정보, 설정, 캔들에 대한 정보 열람 가능<br>"+
            "케이크가 없는 경우 메시지 출력 예시1: (D-30 초과) 생일까지 D-313 남았습니다. 케이크 생성은 생일 D-30일부터 가능합니다.<br>" +
            "케이크가 없는 경우 메시지 출력 예시2: (D-30 이하) 민교수님의 28살 생일 케이크를 만들어 보세요!")
    @GetMapping("/view")
    public ResponseEntity<ResponseDto<?>> getCakeAndCandles(@Valid @ModelAttribute CakeViewRequestDto request) {
        return ResponseEntity.ok(new ResponseDto<>("케이크 및 캔들 열람이 완료되었습니다.", cakeService.getCakeAndCandles(request)));
    }

    @Operation(summary = "친구의 당해 케이크와 하위 캔들 정보 열람", description = "친구의 당해 년도 케이크에 대한 정보, 설정, 캔들에 대한 정보 열람 가능")
    @GetMapping("/friends/view/{friendEmail}")
    public ResponseEntity<ResponseDto<?>> getCakeForFriend(@PathVariable String friendEmail) {
        return ResponseEntity.ok(new ResponseDto<>("친구의 케이크 및 캔들 열람이 완료되었습니다.", friendCakeService.getCakeFromSomeone(friendEmail)));
    }

}
