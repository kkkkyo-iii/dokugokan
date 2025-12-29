package com.example.demo.movie.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

//TmdbResponseDto.java
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbResponseDto {
 
 private List<TmdbMovieDto> results;


 @JsonProperty("page")
 private int page;

 @JsonProperty("total_pages")
 private int totalPages;
 
 @JsonProperty("total_results")
 private int totalResults;
}