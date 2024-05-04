package com.example.decoratemycakebackend.domain.cake.entity;

import com.example.decoratemycakebackend.domain.member.entity.Member;
import com.example.decoratemycakebackend.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Builder
public class Cake extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "cake_id", nullable = false)
    private Long id;
    private String name; // 케이크 이름과 케이크 이미지 이름 동일함
    private int createdYear;
    private String email;

    @Enumerated(EnumType.STRING)
    private CandleCreatePermission candleCreatePermission;

    @Enumerated(EnumType.STRING)
    private CandleViewPermission candleViewPermission;

    @Enumerated(EnumType.STRING)
    private CandleCountPermission candleCountPermission;

    @OneToMany(mappedBy = "cake", cascade = CascadeType.ALL)
    private List<Candle> candles = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public int getCakeCreatedYear() {
        return getCreatedAt().getYear();
    }
}
