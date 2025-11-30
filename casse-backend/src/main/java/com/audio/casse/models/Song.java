package com.audio.casse.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "music_id3")
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
}
