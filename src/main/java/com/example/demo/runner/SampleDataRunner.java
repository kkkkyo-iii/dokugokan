package com.example.demo.runner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.movie.Movie;
import com.example.demo.movie.MovieInformationRepository;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import com.example.demo.voting.AwkwardnessVote;
import com.example.demo.voting.AwkwardnessVoteRepository;
import com.example.demo.voting.MovieTagVote;
import com.example.demo.voting.MovieTagVoteRepository;
import com.example.demo.voting.Tag;
import com.example.demo.voting.TagRepository;
import com.example.demo.voting.TagVoteType;

/**
 * ポートフォリオ用に「ジョーカー」のサンプルデータを投入するランナー
 */
@Component
public class SampleDataRunner implements CommandLineRunner {

    private final MovieInformationRepository movieRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final AwkwardnessVoteRepository awkwardnessVoteRepository;
    private final MovieTagVoteRepository movieTagVoteRepository;
    private final PasswordEncoder passwordEncoder;

    public SampleDataRunner(MovieInformationRepository movieRepository,
                            TagRepository tagRepository,
                            UserRepository userRepository,
                            AwkwardnessVoteRepository awkwardnessVoteRepository,
                            MovieTagVoteRepository movieTagVoteRepository,
                            PasswordEncoder passwordEncoder) {
        this.movieRepository = movieRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.awkwardnessVoteRepository = awkwardnessVoteRepository;
        this.movieTagVoteRepository = movieTagVoteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("=== サンプルデータの投入を開始します (JOKER) ===");

        // 1. 映画「ジョーカー」を作成 (存在しなければ)
        // TMDB ID: 475557
        Movie joker = movieRepository.findByTmdbId(475557).orElseGet(() -> {
            Movie m = new Movie();
            m.setTmdbId(475557);
            m.setTitle("ジョーカー");
            m.setOverview("孤独で心優しいアーサー・フレックが、悪のカリスマ「ジョーカー」に変貌していく衝撃のサスペンス・エンターテイメント。都会の片隅でピエロメイクの大道芸人をしながら、母と二人で慎ましく暮らすアーサーだったが...");
            m.setPosterPath("/udDclJoHjfjb8Ekgsd4FDteOkCU.jpg"); // 実際のパスに近いダミー
            m.setBackdropPath("/n6bUvigpRFqSwmPp1m2YADdbRBc.jpg");
            m.setReleaseDate(LocalDate.of(2019, 10, 4));
            return movieRepository.save(m);
        });

        // 2. ダミーユーザーを20人作成 (投票用)
        for (int i = 1; i <= 20; i++) {
            String username = "user" + i;
            if (userRepository.findByUsername(username).isEmpty()) {
                User u = new User();
                u.setUsername(username);
                u.setPassword(passwordEncoder.encode("password"));
                u.setRole("ROLE_USER");
                userRepository.save(u);
            }
        }
        List<User> users = userRepository.findAll();

        // 3. 投票データを投入
        // ジョーカーなので「気まずい」を多めに、「重い」タグを多めにする
        Random random = new Random();

        // タグを取得 (DataInitializerで入れたタグ名を指定)
        Tag violence = tagRepository.findByName("暴力・流血").orElse(null);
        Tag depressing = tagRepository.findByName("気分が落ち込む").orElse(null);
        Tag heavy = tagRepository.findByName("考え込む").orElse(null);
        Tag serious = tagRepository.findByName("シリアスな展開").orElse(null);
        Tag acting = tagRepository.findByName("キャストがいい").orElse(null); // ホアキン・フェニックス用
        Tag deep = tagRepository.findByName("深い").orElse(null);

        for (User user : users) {
            // --- A. 気まずさ投票 (80%の確率で「気まずい」) ---
            if (awkwardnessVoteRepository.findByUserAndMovie(user, joker).isEmpty()) {
                AwkwardnessVote av = new AwkwardnessVote();
                av.setUser(user);
                av.setMovie(joker);
                // 8割 true, 2割 false
                av.setAwkward(random.nextInt(10) < 8); 
                awkwardnessVoteRepository.save(av);
            }

            // --- B. タグ投票 (ランダムに付与) ---
            // ※ カウンターキャッシュ(totalVoteCount)の更新も忘れずに行う
            
            // 1. 気まずい理由 (暴力・流血)
            if (violence != null && random.nextBoolean()) {
                createTagVote(user, joker, violence, TagVoteType.AWKWARD_REASON);
            }

            // 2. 読後感 (気分が落ち込む、考え込む、など)
            List<Tag> impressions = Arrays.asList(depressing, heavy, serious, acting, deep);
            for (Tag tag : impressions) {
                if (tag != null && random.nextInt(10) < 6) { // 60%の確率で投票
                    createTagVote(user, joker, tag, TagVoteType.IMPRESSION);
                }
            }
        }
        
        System.out.println("=== サンプルデータの投入が完了しました ===");
    }

    private void createTagVote(User user, Movie movie, Tag tag, TagVoteType type) {
        // 重複チェック
        if (movieTagVoteRepository.findByUserAndMovieAndVoteType(user, movie, type)
                .stream().anyMatch(v -> v.getTag().equals(tag))) {
            return;
        }

        MovieTagVote vote = new MovieTagVote();
        vote.setUser(user);
        vote.setMovie(movie);
        vote.setTag(tag);
        vote.setVoteType(type);
        // トレンド集計用に、直近1週間のランダムな日時を入れる
        vote.setCreatedAt(LocalDateTime.now().minusDays(new Random().nextInt(5))); 
        
        movieTagVoteRepository.save(vote);

        // ★重要: タグの総得票数を更新 (ランキング用)
        tagRepository.incrementVoteCount(tag.getId());
    }
}