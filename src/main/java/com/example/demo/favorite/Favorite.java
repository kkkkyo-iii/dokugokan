package com.example.demo.favorite;

import com.example.demo.movie.Movie;
import com.example.demo.user.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
// user_id と movie_id の組み合わせが重複しないように制約をかける
@Table(name = "favorites", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "movie_id"}))
@Data
@NoArgsConstructor
public class Favorite {

	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "favorites_id_seq")
    @SequenceGenerator(name = "favorites_id_seq", sequenceName = "favorites_id_seq", allocationSize = 1)
    private Long id;

    // 多対一: 多くの「お気に入り」は、一人の「ユーザー」に紐づく
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 多対一: 多くの「お気に入り」は、一本の「映画」に紐づく
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;
}