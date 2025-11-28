package com.example.demo.voting;

import com.example.demo.movie.Movie;
import com.example.demo.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
// 1人のユーザーが、1つの映画の、1つのタグに、1種類の投票しかできないように制約をかける
@Table(name = "movie_tag_votes", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "movie_id", "tag_id", "vote_type"}))
@Data
@NoArgsConstructor
public class MovieTagVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 投票した「ユーザー」
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 投票対象の「映画」
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    // 投票した「タグ」
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    // 投票の種類 (「気まずい理由」か「読後感」か)
    @Enumerated(EnumType.STRING) // Enumの値を文字列としてDBに保存
    @Column(name = "vote_type", nullable = false)
    private TagVoteType voteType;
}