package com.example.demo.movie;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager; // 追加
import org.springframework.transaction.support.TransactionTemplate; // 追加
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.favorite.FavoriteService;
import com.example.demo.movie.dto.TmdbMovieDto;
import com.example.demo.movie.dto.TmdbResponseDto;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import com.example.demo.voting.Tag;
import com.example.demo.voting.TagRepository;
import com.example.demo.voting.TagVoteType;
import com.example.demo.voting.VotingService;
import com.example.demo.voting.dto.UserVoteStatusDto;
import com.example.demo.voting.dto.VoteResultDto;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class MovieService {

    private final WebClient webClient;
    private final MovieInformationRepository movieRepository;
    private final GenreRepository genreRepository;
    
    private final FavoriteService favoriteService;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final VotingService votingService;

    // ★ トランザクション制御用 (非同期スレッドでJPAを使うために必須)
    private final TransactionTemplate transactionTemplate;

    @Value("${tmdb.api.key}")
    private String apiKey;

    public MovieService(WebClient.Builder webClientBuilder, 
                        MovieInformationRepository movieRepository, 
                        GenreRepository genreRepository,
                        FavoriteService favoriteService, 
                        UserRepository userRepository, 
                        TagRepository tagRepository, 
                        VotingService votingService,
                        PlatformTransactionManager transactionManager) { // 追加
        this.webClient = webClientBuilder.build();
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
        this.favoriteService = favoriteService;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.votingService = votingService;
        
        // ★ TransactionTemplateの作成
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /**
     * [変更後] TMDB APIで映画を検索する（DBには保存しない）
     * 非同期 + ページング対応
     */
    public Mono<TmdbResponseDto> searchMoviesFromApi(String query, int page) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/search/movie")
                        .queryParam("api_key", apiKey)
                        .queryParam("query", query)
                        .queryParam("language", "ja-JP")
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .bodyToMono(TmdbResponseDto.class);
    }

    /**
     * [非同期化] TMDB IDを元に、DBまたはAPIから映画を取得する
     * TransactionTemplate と Schedulers を組み合わせて安全に処理
     */
    public Mono<Movie> findOrCreateMovieByTmdbId(Integer tmdbId) {
        
        // 1. DB検索
        return Mono.justOrEmpty(movieRepository.findByTmdbId(tmdbId))
            .switchIfEmpty(
                // 2. DBになければAPIから取得
                webClient.get()
                    .uri("/movie/{movieId}?api_key={apiKey}&language=ja-JP", tmdbId, apiKey)
                    .retrieve()
                    .bodyToMono(TmdbMovieDto.class)
                    // ★【重要】ここから下の処理は、DB用の別スレッド(boundedElastic)で行う
                    .publishOn(Schedulers.boundedElastic()) 
                    .map(detailDto -> {
                        // ★ トランザクション開始 (これでDetached entityエラーを防ぐ)
                        return transactionTemplate.execute(status -> {
                            
                            // 3. DTO -> Entity 変換
                            Movie movie = new Movie();
                            movie.setTmdbId(detailDto.getTmdbId());
                            movie.setTitle(detailDto.getTitle());
                            movie.setOverview(detailDto.getOverview());
                            movie.setPosterPath(detailDto.getPosterPath());
                            movie.setBackdropPath(detailDto.getBackdropPath());
                            movie.setReleaseDate(detailDto.getReleaseDate());
                            
                            // ジャンル処理
                            if (detailDto.getGenres() != null) {
                                Set<Genre> genres = detailDto.getGenres().stream().map(genreDto -> {
                                    // トランザクション内なので安全に取得可能
                                    return genreRepository.findByTmdbGenreId(genreDto.getTmdbGenreId())
                                            .orElseGet(() -> {
                                                Genre newGenre = new Genre();
                                                newGenre.setTmdbGenreId(genreDto.getTmdbGenreId());
                                                newGenre.setName(genreDto.getName());
                                                return newGenre;
                                            });
                                }).collect(Collectors.toSet());
                                movie.setGenres(genres);
                            }
                            
                            // 4. DB保存
                            return movieRepository.save(movie);
                        });
                        // ★ トランザクション終了
                    })
            );
    }

    /**
     * 映画詳細ページに必要なすべての情報を取得する
     */
    public Map<String, Object> getMovieDetailData(Movie movie, String username) {
        Map<String, Object> modelData = new LinkedHashMap<>();

        // 1. 映画本体
        modelData.put("movie", movie);

        // 2. お気に入り状態
        boolean isFavorited = false;
        User user = null;
        if (username != null) {
             user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
            	isFavorited = favoriteService.isFavorited(user, movie);
            }
        }
        modelData.put("isFavorited", isFavorited);

        // 3. タグのグループ分け
        List<Tag> allTags = tagRepository.findAll();
        Map<String, List<Tag>> awkwardReasonTags = allTags.stream()
                .filter(tag -> tag.getTagType() == TagVoteType.AWKWARD_REASON)
                .collect(Collectors.groupingBy(
                    Tag::getCategoryHeadline, 
                    LinkedHashMap::new, 
                    Collectors.toList()
                ));
        Map<String, List<Tag>> impressionTags = allTags.stream()
                .filter(tag -> tag.getTagType() == TagVoteType.IMPRESSION)
                .collect(Collectors.groupingBy(
                    Tag::getCategoryHeadline, 
                    LinkedHashMap::new, 
                    Collectors.toList()
                ));
        modelData.put("awkwardReasonTagGroups", awkwardReasonTags);
        modelData.put("impressionTagGroups", impressionTags);

        // 4. 投票結果
        VoteResultDto voteResults = votingService.getVoteResults(movie.getId());
        modelData.put("voteResults", voteResults);

        // 5. ユーザーの過去の投票内容
        UserVoteStatusDto userVoteStatus = new UserVoteStatusDto();
        if (username != null) {
             userVoteStatus = votingService.getUserVoteStatus(user, movie);
        }
        modelData.put("userVoteStatus", userVoteStatus);

        return modelData;
    }
    
    /**
     * 指定されたタグIDに紐づく映画リストとタグ情報を取得する
     */
    public Map<String, Object> getMoviesByTag(Long tagId) {
        Map<String, Object> modelData = new LinkedHashMap<>();

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found with id: " + tagId));

        List<Movie> movies = movieRepository.findByTagId(tagId);

        modelData.put("tagName", tag.getName());
        modelData.put("movies", movies);

        return modelData;
    }

    /**
     * ジャンルIDを指定して、ジャンル情報とそれに紐づく映画リストを取得する
     */
    public Map<String, Object> getMoviesByGenre(Long genreId) {
        Map<String, Object> modelData = new LinkedHashMap<>();

        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new RuntimeException("Genre not found with id: " + genreId));

        List<Movie> movies = movieRepository.findByGenresId(genreId); 

        modelData.put("genreName", genre.getName());
        modelData.put("movies", movies);

        return modelData;
    }

    /**
     * 全てのタグを取得する (検索画面用)
     */
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    /**
     * 検索画面用に「読後感タグ」のみを取得する
     */
    public List<Tag> getSearchableTags() {
        return tagRepository.findByTagType(TagVoteType.IMPRESSION);
    }
}