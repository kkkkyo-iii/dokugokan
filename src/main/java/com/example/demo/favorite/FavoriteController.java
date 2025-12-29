package com.example.demo.favorite;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.movie.Movie; // ★ 1. Movieをimport

@Controller
public class FavoriteController {

	private final FavoriteService favoriteService;
    
	public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    // お気に入り登録処理
	@PostMapping("/favorites/add/{movieId}")
    public String addFavorite(@PathVariable Long movieId, @AuthenticationPrincipal UserDetails userDetails) {
        
        Movie movie = favoriteService.addFavorite(movieId, userDetails.getUsername());

        //  MovieのTMDB IDを使って正しいURLにリダイレクト
        return "redirect:/movie/" + movie.getTmdbId(); 
    }

    // お気に入り解除処理
	@PostMapping("/favorites/remove/{movieId}")
    public String removeFavorite(@PathVariable Long movieId, @AuthenticationPrincipal UserDetails userDetails) {
        // ★ 2. Serviceを呼び出し、Movieオブジェクトを受け取る
        Movie movie = favoriteService.removeFavorite(movieId, userDetails.getUsername());
        
        // ★ 3. MovieのTMDB IDを使って正しいURLにリダイレクト
        return "redirect:/movie/" + movie.getTmdbId(); 
    }
}