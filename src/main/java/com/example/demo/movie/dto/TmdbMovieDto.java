package com.example.demo.movie.dto;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // DTOにない項目は無視する
public class TmdbMovieDto {

    @JsonProperty("id")
    private Integer tmdbId;

    private String title;
    
    private String overview;
    
    @JsonProperty("poster_path")
    private String posterPath;
    
    @JsonProperty("backdrop_path")
    private String backdropPath;
    
    @JsonProperty("release_date")
    private LocalDate releaseDate;
    
    // APIからは直接`status`が返されないことが多いので、詳細は別APIで取得想定
    // ここではnullのままにしておく
    private String status; 
    
    private List<GenreDto> genres; // この行を追加
}