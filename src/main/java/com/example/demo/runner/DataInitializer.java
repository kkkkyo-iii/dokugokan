package com.example.demo.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.movie.Movie;
import com.example.demo.movie.MovieService;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import com.example.demo.voting.AwkwardnessVote;
import com.example.demo.voting.AwkwardnessVoteRepository;
import com.example.demo.voting.MovieTagVote;
import com.example.demo.voting.MovieTagVoteRepository;
import com.example.demo.voting.Tag;
import com.example.demo.voting.TagRepository;
import com.example.demo.voting.TagVoteType;

@Component
public class DataInitializer implements CommandLineRunner {

    private final TagRepository tagRepository;
    private final MovieService movieService;
    private final UserRepository userRepository;
    private final AwkwardnessVoteRepository awkwardnessVoteRepository;
    private final MovieTagVoteRepository movieTagVoteRepository;

    public DataInitializer(TagRepository tagRepository, 
                           MovieService movieService, 
                           UserRepository userRepository, 
                           AwkwardnessVoteRepository awkwardnessVoteRepository, 
                           MovieTagVoteRepository movieTagVoteRepository) {
        this.tagRepository = tagRepository;
        this.movieService = movieService;
        this.userRepository = userRepository;
        this.awkwardnessVoteRepository = awkwardnessVoteRepository;
        this.movieTagVoteRepository = movieTagVoteRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        
        // ★ タグの投入は完了しているためスキップします
        // (もしタグテーブルをリセットした場合は、以前のコードでタグを再投入する必要があります)

        // ★★★ 投票サンプルデータの投入 ★★★
        //createSampleVotes();
    }

    @Transactional
    public void createSampleVotes() {
        System.out.println("デモ用サンプルデータの作成を開始します...");

        // 1. デモ対象の映画を取得 (『ジョーカー』 TMDB ID: 475557)
        // ※ APIから取得してDBに保存します
        Integer demoMovieTmdbId = 475557; 
        Movie demoMovie = movieService.findOrCreateMovieByTmdbId(demoMovieTmdbId);
        
        System.out.println("映画: " + demoMovie.getTitle() + " を準備しました。");

        // 2. デモ用のサンプルユーザーを10人作成
        User u1 = createUser("demo1");
        User u2 = createUser("demo2");
        User u3 = createUser("demo3");
        User u4 = createUser("demo4");
        User u5 = createUser("demo5");
        User u6 = createUser("demo6");
        User u7 = createUser("demo7");
        User u8 = createUser("demo8");
        User u9 = createUser("demo9");
        User u10 = createUser("demo10");

        // 3. 必要なタグを取得 (存在しない場合はnullになるので注意)
        Tag tagViolence = tagRepository.findByName("暴力・流血").orElse(null);
        Tag tagMental = tagRepository.findByName("精神的に疲れる").orElse(null);
        Tag tagNoHope = tagRepository.findByName("理不尽・救いがない").orElse(null);
        
        Tag tagDeep = tagRepository.findByName("考えさせられる").orElse(null);
        Tag tagActing = tagRepository.findByName("キャストがいい").orElse(null); 
        Tag tagShock = tagRepository.findByName("衝撃の結末").orElse(null);
        Tag tagDepress = tagRepository.findByName("気分が落ち込む").orElse(null);

        // 4. 「気まずさ」投票 (10人中8人が「気まずい」)
        // これで円グラフが「赤色(気まずい)」で埋まります
        voteAwkward(u1, demoMovie, true);
        voteAwkward(u2, demoMovie, true);
        voteAwkward(u3, demoMovie, true);
        voteAwkward(u4, demoMovie, true);
        voteAwkward(u5, demoMovie, true);
        voteAwkward(u6, demoMovie, true);
        voteAwkward(u7, demoMovie, true);
        voteAwkward(u8, demoMovie, true);
        voteAwkward(u9, demoMovie, false); // 気まずくない
        voteAwkward(u10, demoMovie, false); // 気まずくない

        // 5. 「気まずい理由」タグ投票
        // 多くの人が「暴力・流血」「精神的に疲れる」を選んだ状態を作ります
        if (tagViolence != null) {
            voteTag(u1, demoMovie, tagViolence, TagVoteType.AWKWARD_REASON);
            voteTag(u2, demoMovie, tagViolence, TagVoteType.AWKWARD_REASON);
            voteTag(u3, demoMovie, tagViolence, TagVoteType.AWKWARD_REASON);
            voteTag(u4, demoMovie, tagViolence, TagVoteType.AWKWARD_REASON);
            voteTag(u5, demoMovie, tagViolence, TagVoteType.AWKWARD_REASON);
        }
        if (tagMental != null) {
            voteTag(u6, demoMovie, tagMental, TagVoteType.AWKWARD_REASON);
            voteTag(u7, demoMovie, tagMental, TagVoteType.AWKWARD_REASON);
            voteTag(u8, demoMovie, tagMental, TagVoteType.AWKWARD_REASON);
            voteTag(u1, demoMovie, tagMental, TagVoteType.AWKWARD_REASON);
        }
        if (tagNoHope != null) {
            voteTag(u2, demoMovie, tagNoHope, TagVoteType.AWKWARD_REASON);
            voteTag(u3, demoMovie, tagNoHope, TagVoteType.AWKWARD_REASON);
        }

        // 6. 「読後感」タグ投票
        if (tagDeep != null) {
            voteTag(u1, demoMovie, tagDeep, TagVoteType.IMPRESSION);
            voteTag(u2, demoMovie, tagDeep, TagVoteType.IMPRESSION);
            voteTag(u3, demoMovie, tagDeep, TagVoteType.IMPRESSION);
            voteTag(u4, demoMovie, tagDeep, TagVoteType.IMPRESSION);
        }
        if (tagDepress != null) {
            voteTag(u5, demoMovie, tagDepress, TagVoteType.IMPRESSION);
            voteTag(u6, demoMovie, tagDepress, TagVoteType.IMPRESSION);
            voteTag(u7, demoMovie, tagDepress, TagVoteType.IMPRESSION);
        }
        if (tagShock != null) {
            voteTag(u8, demoMovie, tagShock, TagVoteType.IMPRESSION);
            voteTag(u9, demoMovie, tagShock, TagVoteType.IMPRESSION);
        }
        if (tagActing != null) {
            voteTag(u10, demoMovie, tagActing, TagVoteType.IMPRESSION);
        }

        System.out.println("デモ用サンプルデータの作成が完了しました。");
    }

    // --- ヘッダーメソッド ---

    private User createUser(String username) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            User user = new User();
            user.setUsername(username);
            user.setPassword("pass"); // デモ用なのでハッシュ化省略(ログインには使えません)
            user.setRole("ROLE_USER");
            return userRepository.save(user);
        });
    }

    private void voteAwkward(User user, Movie movie, boolean isAwkward) {
        if (awkwardnessVoteRepository.findByUserAndMovie(user, movie).isEmpty()) {
            AwkwardnessVote vote = new AwkwardnessVote();
            vote.setUser(user);
            vote.setMovie(movie);
            vote.setAwkward(isAwkward);
            awkwardnessVoteRepository.save(vote);
        }
    }

    private void voteTag(User user, Movie movie, Tag tag, TagVoteType type) {
        // 簡易的に重複チェックなしで保存（デモ用）
        // ※ 厳密には findByUserAndMovieAndTagAndVoteType でチェックすべきですが、
        //    今回は createSampleVotes 自体を1回しか実行しない運用でカバーします。
        MovieTagVote vote = new MovieTagVote();
        vote.setUser(user);
        vote.setMovie(movie);
        vote.setTag(tag);
        vote.setVoteType(type);
        movieTagVoteRepository.save(vote);
    }
}