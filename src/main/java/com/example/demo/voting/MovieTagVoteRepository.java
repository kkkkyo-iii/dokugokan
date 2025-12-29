package com.example.demo.voting;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.movie.Movie;
import com.example.demo.user.User;
import com.example.demo.voting.dto.MovieScoreDto;
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
	List<TagVoteCountDto> findTagVoteCountsByMovie(@Param("movieId") Long movieId,
			@Param("voteType") TagVoteType voteType);

	// 新着順: 最新の投票順に映画を取得（重複排除）
	// 最新10件などを取得する際に使用
	// GROUP BY を使い、各映画ごとの「最新の投票日時 (MAX(v.createdAt))」でソートします
	@Query("SELECT v.movie FROM MovieTagVote v GROUP BY v.movie ORDER BY MAX(v.createdAt) DESC")
	List<Movie> findLatestVotedMovies(Pageable pageable);

	// ★トレンド集計用: 指定期間（since）以降に投票したユーザー数を映画ごとに集計
	@Query("SELECT new com.example.demo.voting.dto.MovieScoreDto(v.movie, COUNT(DISTINCT v.user)) " +
			"FROM MovieTagVote v " +
			"WHERE v.createdAt >= :since " +
			"GROUP BY v.movie " +
			"ORDER BY COUNT(DISTINCT v.user) DESC")
	List<MovieScoreDto> findTrendingMovies(@Param("since") LocalDateTime since, Pageable pageable);
}
