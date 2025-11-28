package com.example.demo.voting;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.movie.Movie;
import com.example.demo.user.User;

@Repository
public interface AwkwardnessVoteRepository extends JpaRepository<AwkwardnessVote, Long> {

    /**
     * 特定のユーザーが特定の映画に投票した記録を検索する (重複投票防止・更新用)
     * @param user ユーザー
     * @param movie 映画
     * @return 投票記録 (存在しない場合は空のOptional)
     */
    Optional<AwkwardnessVote> findByUserAndMovie(User user, Movie movie);

    /**
     * 映画IDと気まずさのT/Fを指定して、票数をカウントする
     * @param movie 映画
     * @param awkward true (気まずい) or false (気まずくない)
     * @return 票数
     */
    long countByMovieAndAwkward(Movie movie, boolean awkward);
}