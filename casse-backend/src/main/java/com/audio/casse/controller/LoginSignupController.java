package com.audio.casse.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginSignupController {

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }
}
