package com.example.demo.movie.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GenreDto {
    @JsonProperty("id")
    private Integer tmdbGenreId;
    
    private String name;
}