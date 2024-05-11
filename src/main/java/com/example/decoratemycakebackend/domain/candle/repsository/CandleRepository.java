package com.example.decoratemycakebackend.domain.candle.repsository;

import com.example.decoratemycakebackend.domain.cake.entity.Cake;
import com.example.decoratemycakebackend.domain.candle.entity.Candle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandleRepository extends JpaRepository<Candle, Long> {
    List<Candle> findByCake(Cake cake);
}
