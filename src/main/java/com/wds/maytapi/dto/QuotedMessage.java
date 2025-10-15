package com.wds.maytapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuotedMessage {
    @JsonProperty("_serialized")
    private String serialized;
    
    private String id;
    private String type; // "text", "image", etc.
    private String text;
    private String caption;
    private String filename;
    private String mime;
    private Long timestamp;
    private User user;
    private String url;
}
