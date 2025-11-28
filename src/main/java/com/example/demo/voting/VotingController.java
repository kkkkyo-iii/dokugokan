package com.example.demo.voting;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.movie.Movie; // ★ 1. Movieのimportを追加
import com.example.demo.movie.MovieInformationRepository;

@Controller
public class VotingController {

    private final VotingService votingService;
    private final MovieInformationRepository movieRepository; // ★ 3. Repositoryのフィールドを追加

    // ★ 4. コンストラクタを修正
    public VotingController(VotingService votingService, MovieInformationRepository movieRepository) {
        this.votingService = votingService;
        this.movieRepository = movieRepository;
    }

    /**
     * 投票フォームからの送信を処理する
     */
    @PostMapping("/vote/{movieId}") // ここで受け取るのはDBのID
    public String handleVote(
            @PathVariable Long movieId,
            @RequestParam(name = "awkward") boolean awkward,
            @RequestParam(name = "awkwardReasonTagIds", required = false) List<Long> awkwardReasonTagIds,
            @RequestParam(name = "impressionTagIds", required = false) List<Long> impressionTagIds,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // 1. ログイン中のユーザー名を取得
        String username = userDetails.getUsername();

        // 2. Serviceに全ての処理を委任
        votingService.processVotes(movieId, username, awkward, awkwardReasonTagIds, impressionTagIds);

        // ★ 5. [修正] 正しいURLにリダイレクトする
        // DBのID (movieId) を使って、映画のTMDB ID (tmdbId) を検索
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        Integer tmdbId = movie.getTmdbId();

        // 3. 処理完了後、TMDB IDを使った正しい詳細ページURLに戻る
        return "redirect:/movie/" + tmdbId; // "/movies/" ではなく "/movie/"
    }
}