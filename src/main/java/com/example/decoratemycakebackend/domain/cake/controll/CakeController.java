package com.example.decoratemycakebackend.domain.cake.controll;

import com.example.decoratemycakebackend.domain.cake.dto.CakeCreateRequestDto;
import com.example.decoratemycakebackend.domain.cake.dto.CakeViewRequestDto;
import com.example.decoratemycakebackend.domain.cake.service.CakeService;
import com.example.decoratemycakebackend.global.util.ResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "케이크 관리 API", description = "케이크 관리 API endpoints")
@RestController
@RequestMapping("/cake")
@RequiredArgsConstructor
public class CakeController {

    private final CakeService cakeService;

    @Operation(summary = "케이크 생성", description = "케이크 생성")
    @PostMapping("/create")
    public ResponseEntity<ResponseDto<?>> createFriendRequest(@Valid @RequestBody CakeCreateRequestDto request) {
        return ResponseEntity.ok(new ResponseDto<>("케이크 생성이 완료되었습니다.", cakeService.createCake(request)));
    }

    @Operation(summary = "단일 케이크와 하위 캔들 정보 열람", description = "특정 연도 케이크에 대한 정보, 설정, 캔들에 대한 정보 열람 가능")
    @PostMapping("/view")
    public ResponseEntity<ResponseDto<?>> getFriendRequest(@Valid @RequestBody CakeViewRequestDto request) {
        return ResponseEntity.ok(new ResponseDto<>("케이크 및 캔들 열람이 완료되었습니다.", cakeService.getCakeAndCandles(request)));
    }
}
