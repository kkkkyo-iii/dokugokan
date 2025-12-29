package com.example.demo.user;

import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
        return "user/login"; // login.html を表示
    }

    // 新規登録ページを表示
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User()); // 空のUserオブジェクトを渡す
        return "user/register"; 
    }

    // 新規登録処理
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user) {
        userService.registerUser(user); // ロジックをServiceに委任
        return "redirect:/login"; 
    }
 // マイページ表示
    @GetMapping("/mypage")
    public String showMyPage(@AuthenticationPrincipal UserDetails userDetails, 
                             @RequestParam(name = "page", defaultValue = "1") int page, // 追加
                             Model model) {
        
        String username = userDetails.getUsername();
        User user = userService.findByUsername(username);

        Page<Movie> moviePage = favoriteService.getFavoriteMovies(user, page);

        model.addAttribute("user", user);
        model.addAttribute("moviePage", moviePage); 
        model.addAttribute("favoriteMovies", moviePage.getContent()); 

        return "user/mypage"; 
    }
}

