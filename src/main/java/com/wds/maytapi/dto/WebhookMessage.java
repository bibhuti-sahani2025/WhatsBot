package com.wds.maytapi.dto;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data; 
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WebhookMessage {
    @JsonProperty("_serialized")
    private String serialized;
    
    private String id;
    private boolean fromMe;
    private String type; // "text", "image", "ptt", "location", etc.
    private Long timestamp;
    
    // Text message
    private String text;
    
    // Media messages
    private String url;
    private String mime;
    private String filename;
    private String caption;
    
    // Location message
    private String payload; // For location: "lat,lng"
    
    // Quoted/Replied message
    private QuotedMessage quoted;
    
    // Status tracking (for sent messages)
    private List<MessageStatus> statuses;
}
