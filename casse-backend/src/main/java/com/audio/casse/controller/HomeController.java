package com.audio.casse.controller;

import com.audio.casse.models.Song;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for handling home-related requests.
 * Provides endpoints for displaying the home page and redirecting to it.
 */
@Controller
@Tag(name = "Home", description = "Home page and redirection operations")
public class HomeController {

    /**
     * Displays the home page.
     * If a user is authenticated via OAuth2, their name will be displayed.
     * An empty Song object is added to the model for potential form usage.
     *
     * @param principal The authenticated OAuth2 user principal, if available.
     * @param model The model to which attributes are added for the view.
     * @return The name of the home view template.
     */
    @Operation(summary = "Display the home page",
               description = "Shows the home page, displaying the authenticated user's name or 'Guest'.")
    @GetMapping("/home")
    public String home(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal != null) {
            model.addAttribute("name", principal.getAttribute("name"));
        } else {
            model.addAttribute("name", "Guest");
        }
        model.addAttribute("song", new Song()); // Add an empty Song object to the model
        return "home";
    }

    /**
     * Redirects the root URL ("/") to the home page ("/home").
     *
     * @return A redirect string to the /home endpoint.
     */
    @Operation(summary = "Redirect to home page",
               description = "Redirects requests from the root URL to the /home endpoint.")
    @GetMapping("/")
    public String redirectToHome() {
        return "redirect:/home";
    }
}
