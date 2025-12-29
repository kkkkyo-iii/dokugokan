package com.example.demo.common;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.movie.Movie;
import com.example.demo.movie.TrendingMovie;
import com.example.demo.movie.TrendingMovieRepository;
import com.example.demo.voting.MovieTagVoteRepository;



@Controller
public class HomeController {

    private final TrendingMovieRepository trendingMovieRepository;
    private final MovieTagVoteRepository voteRepository;

    public HomeController(TrendingMovieRepository trendingRepo, MovieTagVoteRepository voteRepo) {
        this.trendingMovieRepository = trendingRepo;
        this.voteRepository = voteRepo;
    }

    @GetMapping("/")
    public String showHomePage(Model model) {
        model.addAttribute("isTopPage", true);

        // 1. 急上昇トレンド（計算済みのテーブルから取得）
        List<TrendingMovie> trends = trendingMovieRepository.findAllByOrderByRankOrderAsc();
        List<Movie> trendMovies = trends.stream().map(TrendingMovie::getMovie).collect(Collectors.toList());
        model.addAttribute("trendMovies", trendMovies);

        // 2. 新着投票順（リアルタイム取得）
        List<Movie> latestMovies = voteRepository.findLatestVotedMovies(PageRequest.of(0, 10));
        model.addAttribute("latestMovies", latestMovies);

        return "home";
    }
}