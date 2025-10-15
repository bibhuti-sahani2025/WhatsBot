package com.wds.maytapi.service;

import com.wds.maytapi.config.MaytAPIConfig;
import com.wds.maytapi.dto.MessageResponse;
import com.wds.maytapi.dto.SendMessageRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaytApiService {
    
    private final RestTemplate restTemplate;
    private final MaytAPIConfig properties;
    
    /**
     * Send a text message
     */
    public MessageResponse sendTextMessage(String toNumber, String message) {
        try {
            String url = String.format("%s/%s/%s/sendMessage", 
                properties.getBaseUrl(), 
                properties.getProductId(), 
                properties.getPhoneId());
            
            SendMessageRequest request = SendMessageRequest.builder()
                .to_number(formatPhoneNumber(toNumber))
                .message(message)
                .type("text")
                .build();
            
            HttpHeaders headers = createHeaders();
            HttpEntity<SendMessageRequest> entity = new HttpEntity<>(request, headers);
            
            log.info("Sending text message to: {}", toNumber);
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, Map.class);
            
            return new MessageResponse(true, "Message sent successfully", response.getBody());
            
        } catch (Exception e) {
            log.error("Error sending message: {}", e.getMessage(), e);
            return new MessageResponse(false, "Failed to send message: " + e.getMessage(), null);
        }
    }
    
    /**
     * Send media (image, video, document)
     * MaytAPI expects: type="media" with message=base64 data
     */
    public MessageResponse sendMedia(String toNumber, String mediaUrl, 
                                     String caption, String mediaType) {
        try {
            String url = String.format("%s/%s/%s/sendMessage", 
                properties.getBaseUrl(), 
                properties.getProductId(), 
                properties.getPhoneId());
            
            // Download the image from URL and convert to base64
            log.info("Downloading media from: {}", mediaUrl);
            String base64Data = downloadAndConvertToBase64(mediaUrl, mediaType);
            
            // Check base64 size (WhatsApp has limits)
            long base64SizeKB = base64Data.length() / 1024;
            log.info("Base64 data size: {} KB", base64SizeKB);
            
            if (base64SizeKB > 5000) { // If larger than 5MB
                log.warn("Media size ({} KB) exceeds recommended limit. This might fail.", base64SizeKB);
            }
            
            Map<String, Object> request = new HashMap<>();
            request.put("to_number", formatPhoneNumber(toNumber));
            request.put("type", "media");
            request.put("message", base64Data);
            if (caption != null && !caption.isEmpty()) {
                request.put("text", caption);
            }
            
            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            log.info("Sending {} to: {}", mediaType, toNumber);
            log.info("Request payload (base64 truncated): to_number={}, type=media, text={}, size={} KB", 
                formatPhoneNumber(toNumber), caption, base64SizeKB);
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, Map.class);
            
            log.info("Response from MaytAPI: {}", response.getBody());
            return new MessageResponse(true, "Media sent successfully", response.getBody());
            
        } catch (Exception e) {
            log.error("Error sending media: {}", e.getMessage(), e);
            return new MessageResponse(false, "Failed to send media: " + e.getMessage(), null);
        }
    }
    
    /**
     * Send location
     * MaytAPI expects: type="location" with latitude, longitude, and optional text (address)
     */
    public MessageResponse sendLocation(String toNumber, String latitude, String longitude, String address) {
        try {
            String url = String.format("%s/%s/%s/sendMessage", 
                properties.getBaseUrl(), 
                properties.getProductId(), 
                properties.getPhoneId());
            
            Map<String, Object> request = new HashMap<>();
            request.put("to_number", formatPhoneNumber(toNumber));
            request.put("type", "location");
            request.put("latitude", latitude);
            request.put("longitude", longitude);
            if (address != null && !address.isEmpty()) {
                request.put("text", address);
            }
            
            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            log.info("Sending location to: {} (lat: {}, lng: {})", toNumber, latitude, longitude);
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, Map.class);
            
            log.info("Response from MaytAPI: {}", response.getBody());
            return new MessageResponse(true, "Location sent successfully", response.getBody());
            
        } catch (Exception e) {
            log.error("Error sending location: {}", e.getMessage(), e);
            return new MessageResponse(false, "Failed to send location: " + e.getMessage(), null);
        }
    }
    
    /**
     * Download media from URL and convert to base64 with data URI format
     */
    private String downloadAndConvertToBase64(String mediaUrl, String mediaType) throws Exception {
        URL url = new URL(mediaUrl);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try (InputStream is = url.openStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
        }
        
        byte[] mediaBytes = baos.toByteArray();
        String base64 = Base64.getEncoder().encodeToString(mediaBytes);
        
        // Determine MIME type from URL
        String mimeType = detectMimeType(mediaUrl, mediaType);
        
        return "data:" + mimeType + ";base64," + base64;
    }
    
    /**
     * Detect MIME type from URL or mediaType parameter
     */
    private String detectMimeType(String mediaUrl, String mediaType) {
        String lowerUrl = mediaUrl.toLowerCase();
        
        // Images
        if (lowerUrl.contains(".png")) {
            return "image/png";
        } else if (lowerUrl.contains(".jpg") || lowerUrl.contains(".jpeg")) {
            return "image/jpeg";
        } else if (lowerUrl.contains(".gif")) {
            return "image/gif";
        } else if (lowerUrl.contains(".webp")) {
            return "image/webp";
        }
        // Audio files
        else if (lowerUrl.contains(".mp3")) {
            return "audio/mp3";
        } else if (lowerUrl.contains(".ogg") || lowerUrl.contains(".oga")) {
            return "audio/ogg";
        } else if (lowerUrl.contains(".wav")) {
            return "audio/wav";
        } else if (lowerUrl.contains(".m4a")) {
            return "audio/mp4";
        } else if (lowerUrl.contains(".aac")) {
            return "audio/aac";
        }
        // Documents
        else if (lowerUrl.contains(".pdf")) {
            return "application/pdf";
        } else if (lowerUrl.contains(".doc")) {
            return "application/msword";
        } else if (lowerUrl.contains(".xls")) {
            return "application/vnd.ms-excel";
        }
        // Default based on mediaType parameter
        else if ("audio".equals(mediaType)) {
            return "audio/mp3";
        } else {
            return "image/jpeg"; // default for images
        }
    }
    
    /**
     * Get messages (retrieve conversation history)
     */
    public MessageResponse getMessages(int page, int limit) {
        try {
            String url = String.format("%s/%s/%s/getMessages?page=%d&limit=%d", 
                properties.getBaseUrl(), 
                properties.getProductId(), 
                properties.getPhoneId(),
                page, limit);
            
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, Map.class);
            
            return new MessageResponse(true, "Messages retrieved", response.getBody());
            
        } catch (Exception e) {
            log.error("Error getting messages: {}", e.getMessage(), e);
            return new MessageResponse(false, "Failed to get messages: " + e.getMessage(), null);
        }
    }
    
    /**
     * Get phone status and info
     */
    public MessageResponse getStatus() {
        try {
            String url = String.format("%s/%s/%s/status", 
                properties.getBaseUrl(), 
                properties.getProductId(), 
                properties.getPhoneId());
            
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, Map.class);
            
            return new MessageResponse(true, "Status retrieved", response.getBody());
            
        } catch (Exception e) {
            log.error("Error getting status: {}", e.getMessage(), e);
            return new MessageResponse(false, "Failed to get status: " + e.getMessage(), null);
        }
    }
    
    /**
     * Create HTTP headers with authentication
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-maytapi-key", properties.getApiToken());
        return headers;
    }
    
    /**
     * Format phone number (ensure country code)
     */
    private String formatPhoneNumber(String phone) {
        // If already a JID (group or user), return as is
        if (phone.endsWith("@g.us") || phone.endsWith("@c.us")) {
            return phone;
        }
        // Remove any non-digit characters
        String cleaned = phone.replaceAll("[^0-9]", "");
        // Add country code if not present
        if (!cleaned.startsWith("91") && cleaned.length() == 10) {
            cleaned = "91" + cleaned; // India country code
        }
        return cleaned;
    }
}
