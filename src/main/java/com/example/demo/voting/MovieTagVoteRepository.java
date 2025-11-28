package com.example.demo.voting;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.movie.Movie;
import com.example.demo.user.User;
import com.example.demo.voting.dto.TagVoteCountDto;

@Repository
public interface MovieTagVoteRepository extends JpaRepository<MovieTagVote, Long> {
	
	List<MovieTagVote> findByUserAndMovieAndVoteType(User user, Movie movie, TagVoteType voteType);
	/**
	* 映画IDと投票タイプを指定して、タグの得票数を集計し、多い順に並べる
    * @param movieId 映画ID
    * @param voteType 投票タイプ (AWKWARD_REASON or IMPRESSION)
    * @return タグの集計結果(DTO)リスト
    */
   @Query("SELECT new com.example.demo.voting.dto.TagVoteCountDto(v.tag.id, v.tag.name, COUNT(v)) " +
          "FROM MovieTagVote v " +
          "WHERE v.movie.id = :movieId AND v.voteType = :voteType " +
          "GROUP BY v.tag.id, v.tag.name " +
          "ORDER BY COUNT(v) DESC")
   List<TagVoteCountDto> findTagVoteCountsByMovie(@Param("movieId") Long movieId, @Param("voteType") TagVoteType voteType);
}
