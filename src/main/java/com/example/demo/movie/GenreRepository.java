package com.example.demo.movie;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
    
    // TMDBのジャンルIDを元に、ジャンルがデータベースに存在するか検索するためのメソッド
    Optional<Genre> findByTmdbGenreId(Integer tmdbGenreId);
}