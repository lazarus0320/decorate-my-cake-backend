package com.example.decoratemycakebackend.domain.member.controler;

import com.example.decoratemycakebackend.domain.member.dto.*;
import com.example.decoratemycakebackend.domain.member.service.MemberService;
import com.example.decoratemycakebackend.global.auth.JwtToken;
import com.example.decoratemycakebackend.global.auth.JwtTokenProvider;
import com.example.decoratemycakebackend.global.s3.S3Service;
import com.example.decoratemycakebackend.global.util.ResponseDto;
import com.example.decoratemycakebackend.global.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "회원 관리 API", description = "회원 관련 API endpoints")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final S3Service s3Service;

    @Operation(summary = "로그인", description = "이메일과 비밀번호를 사용하여 로그인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    @PostMapping("/login")
    public ResponseEntity<ResponseDto<JwtToken>> logIn(@RequestBody LogInDto logInDto) {
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

    @Operation(summary = "로그아웃", description = "현재 로그인 된 계정의 로그아웃")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        String email = SecurityUtil.getCurrentUserEmail();
        memberService.logout(email);
        return ResponseEntity.ok(new ResponseDto<>("로그아웃 되었습니다.", null));
    }

    @Operation(summary = "프로필 사진 업로드", description = "png 또는 jpg, jpeg 파일 형식 업로드만 가능. 리사이징 된 이미지를 반환함.")
    @PostMapping(value = "/upload/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseDto<?>> uploadProfileImage(@RequestParam("file")MultipartFile file) throws IOException {
        String imageUrl = memberService.uploadProfileImg(file);
        return ResponseEntity.ok(new ResponseDto<>("프로필 이미지 업로드가 완료되었습니다.", imageUrl));
    }

    @Operation(summary = "프로필 사진 URL 조회", description = "현재 로그인한 사용자의 프로필 사진 URL을 반환합니다.")
    @GetMapping("/profile")
    public ResponseEntity<ResponseDto<?>> getProfileImageUrl() {
        String imageUrl = memberService.getProfileImgUrl();
        return ResponseEntity.ok(new ResponseDto<>("프로필 이미지 URL 조회가 완료되었습니다.", imageUrl));
    }

    @Operation(summary = "닉네임 변경", description = "현재 로그인한 사용자의 닉네임을 변경합니다.")
    @PutMapping("/nickname")
    public ResponseEntity<ResponseDto<?>> updateNickname(@RequestParam("nickname") String newNickname) {
        memberService.updateNickname(newNickname);
        return ResponseEntity.ok(new ResponseDto<>("닉네임 변경이 완료되었습니다.", null));
    }

    @Operation(summary = "계정 설정 조회", description = "로그인한 사용자의 닉네임과 프로필 이미지 URL을 반환합니다.")
    @GetMapping("/settings")
    public ResponseEntity<ResponseDto<?>> getAccountSettings() {
        AccountSettingsDto accountSettings = memberService.getAccountSettings();
        return ResponseEntity.ok(new ResponseDto<>("계정 설정 조회가 완료되었습니다.", accountSettings));
    }
}
