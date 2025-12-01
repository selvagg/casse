package com.audio.casse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for handling login and signup related requests.
 * Provides endpoints for displaying the login page.
 */
@Controller
@Tag(name = "Authentication", description = "Login and signup operations")
public class LoginSignupController {

    /**
     * Displays the login page.
     *
     * @return The name of the login view template.
     */
    @Operation(summary = "Show login page",
               description = "Displays the user login page.")
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }
}
