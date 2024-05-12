package com.example.decoratemycakebackend.domain.member.service;

import com.example.decoratemycakebackend.domain.member.dto.MemberDto;
import com.example.decoratemycakebackend.domain.member.dto.SignUpDto;
import com.example.decoratemycakebackend.domain.member.entity.Member;
import com.example.decoratemycakebackend.domain.member.mapper.MemberMapper;
import com.example.decoratemycakebackend.domain.member.repository.MemberRepository;
import com.example.decoratemycakebackend.global.auth.JwtToken;
import com.example.decoratemycakebackend.global.auth.JwtTokenProvider;
import com.example.decoratemycakebackend.global.error.CustomException;
import com.example.decoratemycakebackend.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
    private final MemberRepository memberRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final MemberMapper memberMapper;

    public JwtToken logIn(String username, String password) {
        // 1. username + password 기반으로 Authentication 객체 생성
        // 이때 authentication은 인증 여부를 확인하는 authenticated 값이 false

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);

    // 2. 실제 검증. authenticate() 메서드를 통해 요청된 Member에 대한 검증 진행
    // authenticate 메서드가 실행될 때 CustomUserDetailsService에서 만든 loadUserByUsername 메서드 실행

        try {
            Authentication authentication = authenticationManagerBuilder
                    .getObject().authenticate(authenticationToken);

            return jwtTokenProvider.generateToken(authentication);
        } catch (AuthenticationException e) {
            throw new CustomException(ErrorCode.LOGIN_FAILURE);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

    }

    public MemberDto signUp(SignUpDto signUpDto) {
        if (memberRepository.existsByEmail(signUpDto.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_DUPLICATION);
        }

        // 넘겨받은 비밀번호를 인코딩하여 DB에 저장한다.
        String encodedPassword = passwordEncoder.encode(signUpDto.getPassword());
        List<String> roles = new ArrayList<>();
        roles.add("MEMBER");

        Member savedMember = memberRepository.save(memberMapper.toMember(signUpDto, encodedPassword, roles));
        return memberMapper.toMemberDto(savedMember);
    }

    public void logout(String email) {
        jwtTokenProvider.deleteRefreshToken(email);
    }
}
