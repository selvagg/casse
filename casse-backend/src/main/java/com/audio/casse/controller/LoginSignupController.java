package com.audio.casse.controller;

import com.audio.casse.models.LoginUserDetails;
import com.audio.casse.models.User;
import com.audio.casse.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class LoginSignupController {

    private final UserService userService;

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    @GetMapping("/signup")
    public String showSignupPage(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String registerUser(@ModelAttribute User user, Model model) {
        // Check if passwords match
        if (!user.getPassword().equals(user.getPassword())) {
            model.addAttribute("error", "Passwords do not match.");
            return "signup";
        }

        userService.registerUser(user);
        return "redirect:/login";
    }

    @GetMapping("/home")
    public String showHomePage(Model model) {
        LoginUserDetails currentUser = userService.getCurrentPrincipal();
        model.addAttribute("firstName", currentUser.getFirstName());
        model.addAttribute("lastName", currentUser.getLastName());
        model.addAttribute("email", currentUser.getEmail());
        return "home";
    }
}
