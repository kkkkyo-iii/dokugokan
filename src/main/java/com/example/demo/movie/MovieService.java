package com.example.demo.movie;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

@Service
public class MovieService {

    private final WebClient webClient;
    private final MovieInformationRepository movieRepository;
    private final GenreRepository genreRepository;
    
    private final FavoriteService favoriteService;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final VotingService votingService;

    @Value("${tmdb.api.key}")
    private String apiKey;

    // コンストラクタ (変更なし)
    public MovieService(WebClient.Builder webClientBuilder, MovieInformationRepository movieRepository, GenreRepository genreRepository,FavoriteService favoriteService, 
 UserRepository userRepository, TagRepository tagRepository, VotingService votingService) {
        this.webClient = webClientBuilder.build();
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
        this.favoriteService = favoriteService;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.votingService = votingService;
    }

    /**
     * [変更後] TMDB APIで映画を検索する（DBには保存しない）
     * @param query 検索キーワード
     * @return 映画情報のDTOリスト
     */
    public List<TmdbMovieDto> searchMoviesFromApi(String query) {
        // 1. TMDB APIで映画を「検索」する
        TmdbResponseDto response = webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/search/movie")
                        .queryParam("api_key", apiKey)
                        .queryParam("query", query)
                        .queryParam("language", "ja-JP")
                        .build())
                .retrieve()
                .bodyToMono(TmdbResponseDto.class)
                .block();

        if (response == null || response.getResults() == null) {
            return List.of();
        }

        // 2. DTOのリストをそのまま返す (DB保存ロジックはここから削除)
        return response.getResults();
    }

    /**
     * [新規] TMDB IDを元に、DBから映画を検索する。
     * もしDBになければ、APIで詳細を取得してDBに保存してから返す。
     * @param tmdbId TMDBの映画ID
     * @return DBに保存済みのMovieエンティティ
     */
    @Transactional
    public Movie findOrCreateMovieByTmdbId(Integer tmdbId) {
        
        // 1. まずDBに存在するか確認
        return movieRepository.findByTmdbId(tmdbId)
            .orElseGet(() -> {
                // 2. DBになければ、APIで詳細情報を取得（以前のロジックをここに移動）
                TmdbMovieDto detailDto = webClient.get()
                        .uri("/movie/{movieId}?api_key={apiKey}&language=ja-JP", tmdbId, apiKey)
                        .retrieve()
                        .bodyToMono(TmdbMovieDto.class)
                        .block();
                
                if (detailDto == null) {
                    // APIから取得できない場合はエラーにする
                    throw new RuntimeException("Movie not found in TMDB with id: " + tmdbId);
                }

                // 3. DTOをMovieエンティティに変換してDBに保存する
                Movie movie = new Movie();
                movie.setTmdbId(detailDto.getTmdbId());
                movie.setTitle(detailDto.getTitle());
                movie.setOverview(detailDto.getOverview());
                movie.setPosterPath(detailDto.getPosterPath());
                movie.setBackdropPath(detailDto.getBackdropPath());
                movie.setReleaseDate(detailDto.getReleaseDate());
                
                // 4. ジャンル情報を処理する
                if (detailDto.getGenres() != null) {
                    Set<Genre> genres = detailDto.getGenres().stream().map(genreDto -> {
                        // DBに同じジャンルが存在するか確認し、なければ新しいGenreを作成
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
                
                // 5. DBに保存し、保存されたエンティティを返す
                return movieRepository.save(movie);
            });
    }
    /**
     * 映画詳細ページに必要なすべての情報を取得する
     * @param movie 映画エンティティ
     * @param username ログイン中のユーザー名 (未ログインならnull)
     * @return Modelに追加するための情報のMap
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

        // 3. タグのグループ分け (Controllerからロジックを移動)
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

        // 4. 投票結果 (VotingServiceから取得)
        VoteResultDto voteResults = votingService.getVoteResults(movie.getId());
        modelData.put("voteResults", voteResults);

        // 5. ユーザーの過去の投票内容 (VotingServiceから取得)
        UserVoteStatusDto userVoteStatus = new UserVoteStatusDto();
        if (username != null) {
          
                userVoteStatus = votingService.getUserVoteStatus(user, movie);
            
        }
        modelData.put("userVoteStatus", userVoteStatus);

        return modelData;
    }
    
    /**
     * [新規] 指定されたタグIDに紐づく映画リストとタグ情報を取得する
     */
    public Map<String, Object> getMoviesByTag(Long tagId) {
        Map<String, Object> modelData = new LinkedHashMap<>();

        // 1. タグ情報を取得 (タイトル表示用)
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found with id: " + tagId));

        // 2. そのタグが投票されている映画を検索
        List<Movie> movies = movieRepository.findByTagId(tagId);

        // 3. Mapに格納
        modelData.put("tagName", tag.getName());
        modelData.put("movies", movies);

        return modelData;
    }
    /**
     * ジャンルIDを指定して、ジャンル情報とそれに紐づく映画リストを取得する
     * @param genreId ジャンルID
     * @return Modelに追加するための情報のMap (genreName, movies)
     */
    public Map<String, Object> getMoviesByGenre(Long genreId) {
        Map<String, Object> modelData = new LinkedHashMap<>();

        // 1. ジャンル情報を取得 (Controllerからロジックを移動)
        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new RuntimeException("Genre not found with id: " + genreId));

        // 2. 映画リストを取得 (Controllerからロジックを移動)
        // ※ MovieInformationRepository のフィールド名は movieRepository のはず
        List<Movie> movies = movieRepository.findByGenresId(genreId); 

        // 3. Mapに格納して返す
        modelData.put("genreName", genre.getName());
        modelData.put("movies", movies);

        return modelData;
    }
    /**
     * 全てのタグを取得する (検索画面用)
     * @return タグのリスト
     */
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }
    /**
     * 検索画面用に「読後感タグ」のみを取得する
     * @return 読後感タグのリスト
     */
    public List<Tag> getSearchableTags() {
        // "IMPRESSION" タイプのタグだけをDBから取得して返す
        return tagRepository.findByTagType(TagVoteType.IMPRESSION);
        }
}
