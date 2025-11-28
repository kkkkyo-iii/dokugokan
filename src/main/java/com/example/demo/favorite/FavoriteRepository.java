package com.example.demo.favorite;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import com.example.demo.user.User;
import com.example.demo.movie.Movie;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    // UserとMovieを元に、お気に入り情報が存在するか検索する
    Optional<Favorite> findByUserAndMovie(User user, Movie movie);
}