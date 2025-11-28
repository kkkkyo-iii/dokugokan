package com.example.demo.movie;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.movie.dto.TmdbMovieDto;
import com.example.demo.voting.Tag;


@Controller
public class MovieController {

	private final MovieService movieService;


	public MovieController(MovieService movieService) {
		this.movieService = movieService;
		}

	/**
	 * トップページを表示する.
	 */
	@GetMapping("/")
    public String showHomePage(Model model) { 
        
        // ★ トップページであることを示すフラグを渡す
        model.addAttribute("isTopPage", true);
        
        return "home";
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
        
        return "search"; // search.html を表示
    }

    /**
     * [修正] 検索を「実行」する (POSTリクエスト)
     * search.htmlのフォームから送信された時はここに来る
     */
    @PostMapping("/movies/search") // @GetMapping から @PostMapping に変更
    public String executeSearch(@RequestParam("query") String query, Model model) {
        // 1. Serviceを呼び出し、DB保存されていない「DTOのリスト」を取得
        List<TmdbMovieDto> movieDtos = movieService.searchMoviesFromApi(query);

        // 2. DTOのリストを "movies" という名前でViewに渡す
        model.addAttribute("movies", movieDtos);

        return "result"; // result.html (結果一覧) を表示
    }

	/**
	 * ★ 3. [修正] 映画の詳細ページを表示する（DBになければAPIから取得して保存）
	 * @param tmdbId TMDBのID
	 */
	@GetMapping("/movie/{tmdbId}") // URLを /movies/{id} から /movie/{tmdbId} に変更
	public String showMovieDetails(@PathVariable("tmdbId") Integer tmdbId, Model model,
			@AuthenticationPrincipal UserDetails userDetails) {

		// 1. Serviceを呼び出し、DB検索 or API取得＆保存 を実行
        Movie movie = movieService.findOrCreateMovieByTmdbId(tmdbId);
        
        // 2. ログイン中のユーザー名を取得
        String username = (userDetails != null) ? userDetails.getUsername() : null;

        // 3. Serviceを呼び出し、詳細ページに必要な全データを取得
        Map<String, Object> detailData = movieService.getMovieDetailData(movie, username);

        // 4. 取得したデータをまとめてModelに追加
        model.addAllAttributes(detailData);
        
        return "detail";
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

        return "tag-movies"; // 新しいHTMLファイルを表示
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

		return "genre-movies";
	}

}