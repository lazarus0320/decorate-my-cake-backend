package com.example.decoratemycakebackend.domain.member.controller;

import com.example.decoratemycakebackend.domain.member.dto.LogInDto;
import com.example.decoratemycakebackend.domain.member.dto.MemberDto;
import com.example.decoratemycakebackend.domain.member.dto.RefreshTokenRequest;
import com.example.decoratemycakebackend.domain.member.dto.SignUpDto;
import com.example.decoratemycakebackend.domain.member.service.MemberService;
import com.example.decoratemycakebackend.global.auth.JwtToken;
import com.example.decoratemycakebackend.global.auth.JwtTokenProvider;
import com.example.decoratemycakebackend.global.util.ResponseDto;
import com.example.decoratemycakebackend.global.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원 관리 API", description = "회원 관련 API endpoints")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "로그인", description = "이메일과 비밀번호를 사용하여 로그인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    @PostMapping("/login")
    public ResponseEntity<ResponseDto<JwtToken>> logIn(LogInDto logInDto) {
        String email = logInDto.getEmail();
        String password = logInDto.getPassword();


        JwtToken jwtToken = memberService.logIn(email, password);
        log.debug("request email = {}, password = {}", email, password);
        log.debug("jwtToken accessToken = {}, refreshToken = {}", jwtToken.getAccessToken(), jwtToken.getRefreshToken());

        return ResponseEntity.ok(new ResponseDto<>("로그인 성공.", jwtToken));

    }

    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "409", description = "이메일 중복")
    })
    @PostMapping("/signup")
    public ResponseEntity<ResponseDto<MemberDto>> signUp(@Valid @RequestBody SignUpDto signUpDto) {
        MemberDto savedMemberDto = memberService.signUp(signUpDto);
        String message = String.format("환영합니다, %s님!", savedMemberDto.getNickname());
        return ResponseEntity.ok(new ResponseDto<>(message, savedMemberDto));
    }

    @Operation(summary = "로그인 이후 api 요청 테스트", description = "로그인 이후 api 요청 테스트")
    @PostMapping("/test")
    public String test() {
        return SecurityUtil.getCurrentUserEmail();
    }

    @Operation(summary = "Refresh Token 재발급", description = "Access Token 만료시 기존 Refresh Token을 이쪽으로 보내서 새로운 Access Token 받아가기")
    @PostMapping("/refresh")
    public ResponseEntity<ResponseDto<?>> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        JwtToken token = jwtTokenProvider.refreshAccessToken(refreshTokenRequest.getRefreshToken());
        return ResponseEntity.ok(new ResponseDto<>("Access Token 재발급 완료.", token));
    }
}
