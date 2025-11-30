package com.audio.casse.controller;

import com.audio.casse.models.Song;
import com.audio.casse.repository.SongsRepository;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/music")
@AllArgsConstructor
public class SongsSearchController {

    private final SongsRepository songsRepository;

    @GetMapping("/search")
    public List<Song> searchMusic(@RequestParam(name = "q", required = false) String query) {
        if (query == null || query.trim().isEmpty()) {
            return (List<Song>) songsRepository.findAll();
        }
        return songsRepository.searchAny(query);
    }

    @GetMapping("/search/title")
    public List<Song> searchByTitle(
            @RequestParam(name = "q") String title,
            @RequestParam(name = "exact-match", defaultValue = "false") boolean exactMatch) {
        if (exactMatch) {
            return songsRepository.findByTitle(title);
        }
        return songsRepository.searchByTitle(title);
    }

    @GetMapping("/search/artists")
    public List<Song> searchByArtists(
            @RequestParam(name = "q") String artists,
            @RequestParam(name = "exact-match", defaultValue = "false") boolean exactMatch) {
        if (exactMatch) {
            return songsRepository.findByArtists(artists);
        }
        return songsRepository.searchByArtists(artists);
    }

    @GetMapping("/search/album")
    public List<Song> searchByAlbum(
            @RequestParam(name = "q") String album,
            @RequestParam(name = "exact-match", defaultValue = "false") boolean exactMatch) {
        if (exactMatch) {
            return songsRepository.findByAlbum(album);
        }
        return songsRepository.searchByAlbum(album);
    }

    @GetMapping("/search/composer")
    public List<Song> searchByComposer(
            @RequestParam(name = "q") String composer,
            @RequestParam(name = "exact-match", defaultValue = "false") boolean exactMatch) {
        if (exactMatch) {
            return songsRepository.findByComposer(composer);
        }
        return songsRepository.searchByComposer(composer);
    }

    @GetMapping("/search/tags")
    public List<Song> searchByTags(
            @RequestParam(name = "q") String tags,
            @RequestParam(name = "exact-match", defaultValue = "false") boolean exactMatch) {
        if (exactMatch) {
            return songsRepository.findByTags(tags);
        }
        return songsRepository.searchByTags(tags);
    }

    @GetMapping("/search/email")
    public List<Song> searchByEmail(
            @RequestParam(name = "q") String email,
            @RequestParam(name = "exact-match", defaultValue = "false") boolean exactMatch) {
        if (exactMatch) {
            return songsRepository.findByEmail(email);
        }
        return songsRepository.searchByEmail(email);
    }
}
