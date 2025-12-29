package com.example.demo.common;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.demo.movie.Movie;
import com.example.demo.voting.Tag;
import com.example.demo.voting.TagRepository;
import com.example.demo.voting.TrendingMovie;
import com.example.demo.voting.TrendingMovieRepository;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final TagRepository tagRepository;
    private final TrendingMovieRepository trendingMovieRepository; // 追加

    // コンストラクタで両方のリポジトリを注入
    public GlobalControllerAdvice(TagRepository tagRepository, TrendingMovieRepository trendingMovieRepository) {
        this.tagRepository = tagRepository;
        this.trendingMovieRepository = trendingMovieRepository;
    }

    // ★修正: header.htmlに合わせて名前を "rankingTags" に変更
    @ModelAttribute("rankingTags")
    public List<Tag> populateRanking() {
        return tagRepository.findTop10ByOrderByTotalVoteCountDesc();
    }

    // ★追加: header.htmlに必要な "headerTrendMovies" を提供
    @ModelAttribute("headerTrendMovies")
    public List<Movie> populateHeaderTrends() {
        // トレンドテーブルから表示順で全件取得
        List<TrendingMovie> trends = trendingMovieRepository.findAllByOrderByRankOrderAsc();
        
        // Movie型に変換し、上位5件だけを返す
        return trends.stream()
                .map(TrendingMovie::getMovie)
                .limit(5) 
                .collect(Collectors.toList());
    }
}