package com.example.demo.voting;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrendingMovieRepository extends JpaRepository<TrendingMovie, Long> {
    // 表示順（ランキング順）に取得
    List<TrendingMovie> findAllByOrderByRankOrderAsc();
    
    // 更新時に全消しするためのメソッド
    void deleteAll();
}