package com.example.demo.movie.dto;

import com.example.demo.movie.Movie;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // 引数なしコンストラクタ（念のため）
@AllArgsConstructor // ★重要: JPQLの SELECT new ... で使われます
public class MovieScoreDto {

    private Movie movie;
    
    private Long score; // 投票数などの集計結果
}