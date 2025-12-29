package com.example.demo.movie;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.voting.Tag;

import reactor.core.publisher.Mono;

@Controller
public class MovieController {

	private final MovieService movieService;


	public MovieController(MovieService movieService) {
		this.movieService = movieService;
		}



	/**
     * [新規] 検索「フォーム」を表示する (GETリクエスト)
     * トップページのボタンを押した時はここに来る
     */
    @GetMapping("/movies/search")
    public String showSearchForm(Model model) {
        // 検索画面でタグ一覧を表示するためにデータを渡す
        List<Tag> searchableTags = movieService.getSearchableTags();
        model.addAttribute("impressionTags", searchableTags);
        
        return "movies/search"; // search.html を表示
    }

    /**
     *  非同期 + ページング対応
     * 戻り値を Mono<String> にし、mapの中でModelをセットする
     */
    @GetMapping("/movies/result") 
    public Mono<String> executeSearch(@RequestParam("query") String query, 
                                      @RequestParam(name = "page", defaultValue = "1") int page,
                                      Model model) {
        
        // Serviceは Mono<TmdbResponseDto> を返す
        return movieService.searchMoviesFromApi(query, page)
            .map(response -> {
                // 非同期でデータ取得が完了した後に実行される処理
                if (response != null) {
                    model.addAttribute("movies", response.getResults());
                    
                    // ページング用の情報をModelにセット
                    model.addAttribute("currentPage", response.getPage());
                    model.addAttribute("totalPages", response.getTotalPages());
                    model.addAttribute("query", query);
                } else {
                    model.addAttribute("movies", List.of());
                }
                
                // HTMLテンプレート名を返す
                return "movies/result";
            });
    }

	/**
	 *  映画の詳細ページを表示する（DBになければAPIから取得して保存）
	 * @param tmdbId TMDBのID
	 */
    @GetMapping("/movie/{tmdbId}")
    public Mono<String> showMovieDetails(@PathVariable("tmdbId") Integer tmdbId, Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 1. Serviceから Mono<Movie> を取得
        return movieService.findOrCreateMovieByTmdbId(tmdbId)
            .map(movie -> {
                // 2. 映画情報が取れたら、詳細データを作成するための準備
                String username = (userDetails != null) ? userDetails.getUsername() : null;
     
                // 3. 必要な情報を一括取得 (ここは同期メソッドのままでOK)
                Map<String, Object> detailData = movieService.getMovieDetailData(movie, username);    
                model.addAllAttributes(detailData);
                
                return "movies/detail";
            });
    }
	/**
     * [新規] 指定されたタグIDに紐づく映画の一覧ページを表示する
     */
    @GetMapping("/movies/by_tag/{id}")
    public String showMoviesByTag(@PathVariable("id") Long id, Model model) {
        
        // Serviceからデータを取得
        Map<String, Object> tagData = movieService.getMoviesByTag(id);
        
        // Modelに追加
        model.addAllAttributes(tagData);

        return "movies/tag"; // 新しいHTMLファイルを表示
    }
	
	/**
	 * 指定されたジャンルIDに紐づく映画の一覧ページを表示する
	 */
	@GetMapping("/movies/by_genre/{id}")
	public String showMoviesByGenre(@PathVariable("id") Long id, Model model) {
		// (このメソッドは変更なし)
		// ★ 5. Serviceに必要な情報をまとめて取得させる
		Map<String, Object> genreData = movieService.getMoviesByGenre(id);
		
		model.addAllAttributes(genreData);

		return "movies/genre";
	}

}