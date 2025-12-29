package com.example.demo.voting;

import com.example.demo.movie.Movie;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trending_movies")
@Data
@NoArgsConstructor
public class TrendingMovie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // トレンド入りした映画
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    // スコア（期間内の投票人数など）
    @Column(nullable = false)
    private Long score;
    
    // 表示順位
    @Column(name = "rank_order", nullable = false)
    private Integer rankOrder;
}