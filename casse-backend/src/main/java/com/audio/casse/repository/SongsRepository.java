package com.audio.casse.repository;

import com.audio.casse.models.Song;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongsRepository extends ElasticsearchRepository<Song, String> {

    // Exact match methods using multi_match on .keyword fields
    @Query("""
        { "multi_match": { "query": "?0", "fields": ["artists.keyword"] } }
    """)
    List<Song> findByArtists(String artist);

    @Query("""
        { "multi_match": { "query": "?0", "fields": ["title.keyword"] } }
    """)
    List<Song> findByTitle(String title);

    @Query("""
        { "multi_match": { "query": "?0", "fields": ["composer.keyword"] } }
    """)
    List<Song> findByComposer(String composer);

    @Query("""
        { "multi_match": { "query": "?0", "fields": ["album.keyword"] } }
    """)
    List<Song> findByAlbum(String album);

    @Query("""
        { "multi_match": { "query": "?0", "fields": ["tags.keyword"] } }
    """)
    List<Song> findByTags(String genre);

    @Query("""
        { "multi_match": { "query": "?0", "fields": ["email"] } }
    """)
    List<Song> findByEmail(String email);

    // full-text search across multiple fields
    @Query("""
        { "multi_match": { "query": "?0", "fields": ["title","artists","composer","album","tags","email"] } }
    """)
    List<Song> searchAny(String keyword);

    // Partial match methods using multi_match on .autocomplete fields
    @Query("""
        { "multi_match": { "query": "?0", "fields": ["title.autocomplete"] } }
    """)
    List<Song> searchByTitle(String title);

    @Query("""
        { "multi_match": { "query": "?0", "fields": ["artists.autocomplete"] } }
    """)
    List<Song> searchByArtists(String artist);

    @Query("""
        { "multi_match": { "query": "?0", "fields": ["composer.autocomplete"] } }
    """)
    List<Song> searchByComposer(String composer);

    @Query("""
        { "multi_match": { "query": "?0", "fields": ["album.autocomplete"] } }
    """)
    List<Song> searchByAlbum(String album);

    @Query("""
        { "multi_match": { "query": "?0", "fields": ["tags.autocomplete"] } }
    """)
    List<Song> searchByTags(String tags);

    @Query("""
        { "multi_match": { "query": "?0", "fields": ["email"] } }
    """)
    List<Song> searchByEmail(String email);
}
