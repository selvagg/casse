package com.audio.casse.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "music_id3")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Song {

    @Id
    private String _id; // Assuming Elasticsearch generates an ID or you provide one

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "standard"),
            otherFields = {
                    @InnerField(suffix = "autocomplete", type = FieldType.Text, analyzer = "autocomplete", searchAnalyzer = "standard"),
                    @InnerField(suffix = "keyword", type = FieldType.Keyword, normalizer = "lowercase_fold")
            }
    )
    private String title;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "standard"),
            otherFields = {
                    @InnerField(suffix = "autocomplete", type = FieldType.Text, analyzer = "autocomplete", searchAnalyzer = "standard"),
                    @InnerField(suffix = "keyword", type = FieldType.Keyword, normalizer = "lowercase_fold")
            }
    )
    private String artists;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "standard"),
            otherFields = {
                    @InnerField(suffix = "autocomplete", type = FieldType.Text, analyzer = "autocomplete", searchAnalyzer = "standard"),
                    @InnerField(suffix = "keyword", type = FieldType.Keyword, normalizer = "lowercase_fold")
            }
    )
    private String album;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "standard"),
            otherFields = {
                    @InnerField(suffix = "autocomplete", type = FieldType.Text, analyzer = "autocomplete", searchAnalyzer = "standard"),
                    @InnerField(suffix = "keyword", type = FieldType.Keyword, normalizer = "lowercase_fold")
            }
    )
    private String composer;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "standard"),
            otherFields = {
                    @InnerField(suffix = "autocomplete", type = FieldType.Text, analyzer = "autocomplete", searchAnalyzer = "standard"),
                    @InnerField(suffix = "keyword", type = FieldType.Keyword, normalizer = "lowercase_fold")
            }
    )
    private String tags;

    @Field(type = FieldType.Keyword, normalizer = "lowercase_fold")
    private String email;

    private String storageAccessKey;

    @Field(type = FieldType.Text)
    private String albumArt;
}
