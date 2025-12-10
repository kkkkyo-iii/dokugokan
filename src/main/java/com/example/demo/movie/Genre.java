package com.example.demo.movie;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "genres")
@Data
@NoArgsConstructor
public class Genre {

	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "genres_id_seq")
    @SequenceGenerator(name = "genres_id_seq", sequenceName = "genres_id_seq", allocationSize = 1)
    private Long id;
	
	// TMDBのジャンルID
	@Column(name = "tmdb_genre_id", unique = true, nullable = false)
	private Integer tmdbGenreId;

	// ジャンル名 (例: "アクション", "コメディ")
	@Column(nullable = false)
	private String name;
}
