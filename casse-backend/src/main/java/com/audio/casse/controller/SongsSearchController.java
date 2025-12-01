package com.audio.casse.controller;

import com.audio.casse.models.Song;
import com.audio.casse.repository.SongsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for searching songs based on various criteria.
 * Provides endpoints for general search, and specific searches by title, artists, album, composer, tags, and email.
 */
@RestController
@RequestMapping("/api/music")
@AllArgsConstructor
@Tag(name = "Songs Search", description = "Operations related to searching for songs")
public class SongsSearchController {

    private final SongsRepository songsRepository;

    /**
     * Searches for songs based on a general query string.
     * If the query is empty or null, all songs are returned.
     *
     * @param query The search query string.
     * @return A list of songs matching the query.
     */
    @Operation(summary = "Search for songs",
               description = "Searches for songs across various fields. Returns all songs if no query is provided.")
    @GetMapping("/search")
    public List<Song> searchMusic(
            @Parameter(description = "The search query string. If empty, all songs are returned.")
            @RequestParam(name = "q", required = false) String query) {
        if (query == null || query.trim().isEmpty()) {
            return (List<Song>) songsRepository.findAll();
        }
        return songsRepository.searchAny(query);
    }

    /**
     * Searches for songs by title.
     *
     * @param title The title to search for.
     * @param exactMatch If true, performs an exact match search; otherwise, performs a partial match.
     * @return A list of songs matching the title.
     */
    @Operation(summary = "Search songs by title",
               description = "Searches for songs based on their title, with an option for exact matching.")
    @GetMapping("/search/title")
    public List<Song> searchByTitle(
            @Parameter(description = "The title of the song to search for.")
            @RequestParam(name = "q") String title,
            @Parameter(description = "If true, performs an exact match search; otherwise, performs a partial match.")
            @RequestParam(name = "exact-match", defaultValue = "false") boolean exactMatch) {
        if (exactMatch) {
            return songsRepository.findByTitle(title);
        }
        return songsRepository.searchByTitle(title);
    }

    /**
     * Searches for songs by artists.
     *
     * @param artists The artists' names to search for.
     * @param exactMatch If true, performs an exact match search; otherwise, performs a partial match.
     * @return A list of songs matching the artists.
     */
    @Operation(summary = "Search songs by artists",
               description = "Searches for songs based on their artists, with an option for exact matching.")
    @GetMapping("/search/artists")
    public List<Song> searchByArtists(
            @Parameter(description = "The artists' names to search for.")
            @RequestParam(name = "q") String artists,
            @Parameter(description = "If true, performs an exact match search; otherwise, performs a partial match.")
            @RequestParam(name = "exact-match", defaultValue = "false") boolean exactMatch) {
        if (exactMatch) {
            return songsRepository.findByArtists(artists);
        }
        return songsRepository.searchByArtists(artists);
    }

    /**
     * Searches for songs by album.
     *
     * @param album The album name to search for.
     * @param exactMatch If true, performs an exact match search; otherwise, performs a partial match.
     * @return A list of songs matching the album.
     */
    @Operation(summary = "Search songs by album",
               description = "Searches for songs based on their album, with an option for exact matching.")
    @GetMapping("/search/album")
    public List<Song> searchByAlbum(
            @Parameter(description = "The album name to search for.")
            @RequestParam(name = "q") String album,
            @Parameter(description = "If true, performs an exact match search; otherwise, performs a partial match.")
            @RequestParam(name = "exact-match", defaultValue = "false") boolean exactMatch) {
        if (exactMatch) {
            return songsRepository.findByAlbum(album);
        }
        return songsRepository.searchByAlbum(album);
    }

    /**
     * Searches for songs by composer.
     *
     * @param composer The composer's name to search for.
     * @param exactMatch If true, performs an exact match search; otherwise, performs a partial match.
     * @return A list of songs matching the composer.
     */
    @Operation(summary = "Search songs by composer",
               description = "Searches for songs based on their composer, with an option for exact matching.")
    @GetMapping("/search/composer")
    public List<Song> searchByComposer(
            @Parameter(description = "The composer's name to search for.")
            @RequestParam(name = "q") String composer,
            @Parameter(description = "If true, performs an exact match search; otherwise, performs a partial match.")
            @RequestParam(name = "exact-match", defaultValue = "false") boolean exactMatch) {
        if (exactMatch) {
            return songsRepository.findByComposer(composer);
        }
        return songsRepository.searchByComposer(composer);
    }

    /**
     * Searches for songs by tags.
     *
     * @param tags The tags to search for.
     * @param exactMatch If true, performs an exact match search; otherwise, performs a partial match.
     * @return A list of songs matching the tags.
     */
    @Operation(summary = "Search songs by tags",
               description = "Searches for songs based on their tags, with an option for exact matching.")
    @GetMapping("/search/tags")
    public List<Song> searchByTags(
            @Parameter(description = "The tags to search for.")
            @RequestParam(name = "q") String tags,
            @Parameter(description = "If true, performs an exact match search; otherwise, performs a partial match.")
            @RequestParam(name = "exact-match", defaultValue = "false") boolean exactMatch) {
        if (exactMatch) {
            return songsRepository.findByTags(tags);
        }
        return songsRepository.searchByTags(tags);
    }

    /**
     * Searches for songs by email.
     *
     * @param email The email to search for.
     * @param exactMatch If true, performs an exact match search; otherwise, performs a partial match.
     * @return A list of songs matching the email.
     */
    @Operation(summary = "Search songs by email",
               description = "Searches for songs based on the associated email, with an option for exact matching.")
    @GetMapping("/search/email")
    public List<Song> searchByEmail(
            @Parameter(description = "The email to search for.")
            @RequestParam(name = "q") String email,
            @Parameter(description = "If true, performs an exact match search; otherwise, performs a partial match.")
            @RequestParam(name = "exact-match", defaultValue = "false") boolean exactMatch) {
        if (exactMatch) {
            return songsRepository.findByEmail(email);
        }
        return songsRepository.searchByEmail(email);
    }
}
