package com.wds.maytapi.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMediaRequest {
    private String to_number;
    private String message;
    private String url; // Media URL
    private String type; // image, video, audio, document
}
