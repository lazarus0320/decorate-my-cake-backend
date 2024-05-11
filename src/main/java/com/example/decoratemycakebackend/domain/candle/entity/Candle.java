package com.example.decoratemycakebackend.domain.candle.entity;

import com.example.decoratemycakebackend.domain.cake.entity.Cake;
import com.example.decoratemycakebackend.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Builder
public class Candle extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "candle_id", nullable = false)
    private Long id;
    private String name; // 캔들과 캔들 이미지 이름 동일함.
    private String content; // 캔들 메시지의 내용
    private String title; // 캔들 메시지의 제목
    private boolean isPrivate; // 비밀글이면 true, 공개글이면 false
    private String writer; // 기본적으로 작성자의 닉네임, 프론트 측에서 변경 가능함.

    // isPrivate() getter 메서드 추가
    public boolean isPrivate() {
        return isPrivate;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="cake_id")
    private Cake cake;
}