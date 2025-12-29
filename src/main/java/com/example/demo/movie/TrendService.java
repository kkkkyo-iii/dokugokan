package com.example.demo.movie;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.context.event.ApplicationReadyEvent; // 追加
import org.springframework.context.event.EventListener; // 追加
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.movie.dto.MovieScoreDto;
import com.example.demo.voting.MovieTagVoteRepository;

@Service
public class TrendService {

    private final MovieTagVoteRepository voteRepository;
    private final TrendingMovieRepository trendingMovieRepository;

    public TrendService(MovieTagVoteRepository voteRepository, TrendingMovieRepository trendingMovieRepository) {
        this.voteRepository = voteRepository;
        this.trendingMovieRepository = trendingMovieRepository;
    }

    // ★追加: アプリ起動時に一度だけトレンド集計を実行する
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        updateTrendingMovies();
    }

    // 定期実行: 1時間に1回トレンドを更新 (毎時0分0秒)
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void updateTrendingMovies() {
        System.out.println("トレンド集計を開始します...");

        // 1. 集計: 過去1週間(7日)で投票ユーザー数が多い映画 TOP10
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        // MovieTagVoteRepositoryに findTrendingMovies が実装済みである前提
        List<MovieScoreDto> scores = voteRepository.findTrendingMovies(since, PageRequest.of(0, 10));

        // 2. 入れ替え: 現在のトレンドテーブルを空にして、新しい結果を保存
        trendingMovieRepository.deleteAll();

        if (scores.isEmpty()) {
            System.out.println("集計期間内の投票データがありませんでした。");
            return;
        }

        List<TrendingMovie> newTrends = scores.stream()
            .map(scoreDto -> {
                TrendingMovie tm = new TrendingMovie();
                tm.setMovie(scoreDto.getMovie());
                tm.setScore(scoreDto.getScore());
                tm.setRankOrder(scores.indexOf(scoreDto) + 1); // 1位から順に番号づけ
                return tm;
            })
            .collect(Collectors.toList());

        trendingMovieRepository.saveAll(newTrends);
        System.out.println("トレンド情報を更新しました: " + LocalDateTime.now() + " (件数: " + newTrends.size() + ")");
    }
}