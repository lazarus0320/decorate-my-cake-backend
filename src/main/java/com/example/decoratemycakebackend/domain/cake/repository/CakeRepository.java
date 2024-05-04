package com.example.decoratemycakebackend.domain.cake.repository;

import com.example.decoratemycakebackend.domain.cake.entity.Cake;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CakeRepository extends JpaRepository<Cake, Long> {
    Optional<Cake> findByEmailAndCreatedYear(String email, int year);
}
