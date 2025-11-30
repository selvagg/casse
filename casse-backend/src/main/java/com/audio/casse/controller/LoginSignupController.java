package com.audio.casse.controller;

import java.util.Objects;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginSignupController {

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }
}
