package com.example.demo.movie;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieInformationRepository  extends JpaRepository<Movie, Long> {
    
    /**
     * TMDBのIDを元に、映画がデータベースに存在するか検索するためのメソッド.
     * Spring Data JPAの命名規則に従うことで、メソッドの中身を自動で生成してくれる.
     * @param tmdbId TMDBの映画ID
     * @return 検索結果の映画エンティティ (見つからない場合は空のOptional)
     */
    Optional<Movie> findByTmdbId(Integer tmdbId);
    
    List<Movie> findByGenresId(Long genreId);
    
    /**
     * 指定されたタグIDが投票されている映画を検索する
     * (MovieTagVoteテーブルを介して検索し、重複を除外して返す)
     */
    @Query("SELECT DISTINCT v.movie FROM MovieTagVote v WHERE v.tag.id = :tagId")
    List<Movie> findByTagId(@Param("tagId") Long tagId);
}
