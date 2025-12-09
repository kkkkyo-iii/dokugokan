package com.example.demo.user;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.favorite.FavoriteService;
import com.example.demo.movie.Movie;

@Controller
public class UserController {

	private final UserService userService;
	private final FavoriteService favoriteService;

	public UserController(UserService userService,FavoriteService favoriteService) {
        this.userService = userService;
        this.favoriteService = favoriteService;
    }

    // ログインページを表示
    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // login.html を表示
    }

    // 新規登録ページを表示
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User()); // 空のUserオブジェクトを渡す
        return "register"; // register.html を表示
    }

    // 新規登録処理
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user) {
        userService.registerUser(user); // ロジックをServiceに委任
        return "redirect:/login"; 
    }
 // マイページ表示
    @GetMapping("/mypage")
    public String showMyPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        // 1. ログイン中のユーザー名を取得
        String username = userDetails.getUsername();
        
        // 2. ユーザー情報を取得
        User user = userService.findByUsername(username);

        // 3.  FavoriteService を使って映画リストを取得
        List<Movie> favoriteMovies = favoriteService.getFavoriteMovies(user); 

        // 4. 画面に渡す
        model.addAttribute("user", user);
        model.addAttribute("favoriteMovies", favoriteMovies);

        return "mypage"; 
    }
}

