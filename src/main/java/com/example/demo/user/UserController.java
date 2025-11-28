package com.example.demo.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
        this.userService = userService;
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
        userService.registerUser(user); // ★ ロジックをServiceに委任
        return "redirect:/login"; 
    }
}