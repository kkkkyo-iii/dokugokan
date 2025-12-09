package com.example.demo.favorite;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.movie.Movie;
import com.example.demo.user.User;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    // UserとMovieを元に、お気に入り情報が存在するか検索する
    Optional<Favorite> findByUserAndMovie(User user, Movie movie);
    
   // 追加: 特定ユーザーのお気に入りを全て取得（IDの降順＝新しい順）
    List<Favorite> findByUserOrderByIdDesc(User user);
}