package com.example.demo.movie;


import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "movies")
@Data
@NoArgsConstructor
public class Movie {

    // 映画ID (主キー)
	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "movies_id_seq")
    @SequenceGenerator(name = "movies_id_seq", sequenceName = "movies_id_seq", allocationSize = 1)
    private Long id;

    // TMDBのID (一意のキー)
    @Column(name = "tmdb_id", unique = true, nullable = false)
    private Integer tmdbId;

    // タイトル
    @Column(nullable = false)
    private String title;

    // 概要
    @Column(length = 1000) // DBによっては文字数制限が必要
    private String overview;

    // ポスター画像のパス
    @Column(name = "poster_path")
    private String posterPath;
    
    // 背景画像のパス
    @Column(name = "backdrop_path")
    private String backdropPath;

    // 公開日
    @Column(name = "release_date")
    private LocalDate releaseDate;
    
    
    // ジャンルとの関連付け (多対多)
    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(
      name = "movie_genres", 
      joinColumns = @JoinColumn(name = "movie_id"), 
      inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private Set<Genre> genres = new HashSet<>();
    
}