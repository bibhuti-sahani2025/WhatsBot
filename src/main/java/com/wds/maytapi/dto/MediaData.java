package com.wds.maytapi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MediaData {
    private String url;
    private String mimeType;
    private Long fileLength;
    private String fileName;
    private String caption;
    // Add other media fields as needed
}
