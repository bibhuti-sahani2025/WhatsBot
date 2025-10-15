package com.wds.maytapi.controller;

import com.wds.maytapi.dto.MessageResponse;
import com.wds.maytapi.service.MaytApiService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/whatsapp")
@RequiredArgsConstructor
public class WhatsappController {

    private final MaytApiService maytApiService;

    /**
     * Send text message
     * POST /api/whatsapp/send
     */
    @PostMapping("/send")
    public ResponseEntity<MessageResponse> sendMessage(
            @RequestParam String phone,
            @RequestParam String message) {

        log.info("Received request to send message to: {}", phone);
        MessageResponse response = maytApiService.sendTextMessage(phone, message);
        return ResponseEntity.ok(response);
    }

    /**
     * Send image
     * POST /api/whatsapp/send-image
     */
    @PostMapping("/send-image")
    public ResponseEntity<MessageResponse> sendImage(
            @RequestParam String phone,
            @RequestParam String imageUrl,
            @RequestParam(required = false) String caption) {

        log.info("Received request to send image to: {}", phone);
        MessageResponse response = maytApiService.sendMedia(phone, imageUrl, caption, "image");
        return ResponseEntity.ok(response);
    }

    /**
     * Send document
     * POST /api/whatsapp/send-document
     */
    @PostMapping("/send-document")
    public ResponseEntity<MessageResponse> sendDocument(
            @RequestParam String phone,
            @RequestParam String documentUrl,
            @RequestParam(required = false) String caption) {

        log.info("Received request to send document to: {}", phone);
        MessageResponse response = maytApiService.sendMedia(phone, documentUrl, caption, "document");
        return ResponseEntity.ok(response);
    }

    /**
     * Send audio (voice message/audio file)
     * POST /api/whatsapp/send-audio
     */
    @PostMapping("/send-audio")
    public ResponseEntity<MessageResponse> sendAudio(
            @RequestParam String phone,
            @RequestParam String audioUrl,
            @RequestParam(required = false) String caption) {

        log.info("Received request to send audio to: {}", phone);
        MessageResponse response = maytApiService.sendMedia(phone, audioUrl, caption, "audio");
        return ResponseEntity.ok(response);
    }

    /**
     * Send location
     * POST /api/whatsapp/send-location
     */
    @PostMapping("/send-location")
    public ResponseEntity<MessageResponse> sendLocation(
            @RequestParam String phone,
            @RequestParam String latitude,
            @RequestParam String longitude,
            @RequestParam(required = false) String address) {

        log.info("Received request to send location to: {}", phone);
        MessageResponse response = maytApiService.sendLocation(phone, latitude, longitude, address);
        return ResponseEntity.ok(response);
    }

    /**
     * Get messages
     * GET /api/whatsapp/messages
     */
    @GetMapping("/messages")
    public ResponseEntity<MessageResponse> getMessages(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {

        log.info("Retrieving messages - page: {}, limit: {}", page, limit);
        MessageResponse response = maytApiService.getMessages(page, limit);
        return ResponseEntity.ok(response);
    }

    /**
     * Get phone status
     * GET /api/whatsapp/status
     */
    @GetMapping("/status")
    public ResponseEntity<MessageResponse> getStatus() {
        log.info("Checking WhatsApp status");
        MessageResponse response = maytApiService.getStatus();
        return ResponseEntity.ok(response);
    }

    /**
     * Webhook endpoint to receive incoming messages
     * POST /api/whatsapp/webhook
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody(required = false) java.util.Map<String, Object> payload) {
        log.info("Webhook received: {}", payload);
        if (payload == null) {
            return ResponseEntity.badRequest().body("Empty payload");
        }
        try {
            String type = (String) payload.get("type");
            // If it's an error webhook, log and ignore
            if ("error".equals(type)) {
                log.warn("Webhook received error from WhatsApp API: {}", payload.get("message"));
                return ResponseEntity.ok("Error webhook acknowledged");
            }
            // If it's a message webhook, process it
            if ("message".equals(type)) {
                processIncomingWebhook(payload);
                return ResponseEntity.ok("Message webhook acknowledged");
            }
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            log.error("Error processing webhook: ", e);
            return ResponseEntity.status(500).body("Error processing webhook");
        }
    }
    
    /**
     * Process incoming webhook with Map structure
     */
    @SuppressWarnings("unchecked")
    private void processIncomingWebhook(java.util.Map<String, Object> payload) {
        try {
            // Extract message data
            java.util.Map<String, Object> message = (java.util.Map<String, Object>) payload.get("message");
            java.util.Map<String, Object> user = (java.util.Map<String, Object>) payload.get("user");
            
            if (message == null || user == null) {
                log.warn("Missing message or user data in webhook");
                return;
            }
            
            String messageType = (String) message.get("type");
            String senderPhone = (String) user.get("phone");
            
            log.info("Processing {} message from {}", messageType, senderPhone);
            
            switch (messageType) {
                case "text":
                    String text = (String) message.get("body");
                    if (text == null) text = (String) message.get("text");
                    handleTextMessage(senderPhone, text);
                    break;
                    
                case "ptt":
                case "audio":
                    String audioUrl = (String) message.get("url");
                    if (audioUrl != null) {
                        String timestamp = java.time.LocalDateTime.now()
                            .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                        String filename = senderPhone + "_" + timestamp + ".ogg";
                        downloadAndProcessMedia(audioUrl, filename, "audio");
                    }
                    break;
                    
                case "image":
                    String imageUrl = (String) message.get("url");
                    if (imageUrl != null) {
                        String timestamp = java.time.LocalDateTime.now()
                            .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                        String filename = senderPhone + "_" + timestamp + ".jpg";
                        downloadAndProcessMedia(imageUrl, filename, "image");
                    }
                    break;
                    
                case "document":
                    String docUrl = (String) message.get("url");
                    String docFilename = (String) message.get("filename");
                    if (docUrl != null) {
                        if (docFilename == null) {
                            docFilename = "document_" + System.currentTimeMillis();
                        }
                        downloadAndProcessMedia(docUrl, docFilename, "document");
                    }
                    break;
                    
                case "location":
                    // Location data might be in different fields
                    log.info("Received location from {}: {}", senderPhone, message);
                    break;
                    
                case "vcard":
                    log.info("Received vCard from {}: {}", senderPhone, message);
                    break;
                    
                default:
                    log.info("Unhandled message type: {} from {}", messageType, senderPhone);
            }
            
        } catch (Exception e) {
            log.error("Error processing incoming webhook: ", e);
        }
    }
    
    private void handleTextMessage(String phone, String text) {
        log.info("Processing text message from {}: {}", phone, text);
        // Your text processing logic
        if (text != null && text.toLowerCase().contains("hello")) {
            maytApiService.sendTextMessage(phone,
                    "Hello! Thanks for contacting us. How can we help you?");
        }
    }
    
    private void downloadAndProcessMedia(String mediaUrl, String filename, String type) {
        try {
            // Determine subfolder based on type
            String subfolder = "";
            switch (type.toLowerCase()) {
                case "image":
                    subfolder = "images";
                    break;
                case "audio":
                    subfolder = "audio";
                    break;
                case "video":
                    subfolder = "video";
                    break;
                case "document":
                    subfolder = "documents";
                    break;
                default:
                    subfolder = "other";
            }
            Path outputPath = Paths.get("downloads", subfolder, filename);
            Files.createDirectories(outputPath.getParent());
            if (Files.exists(outputPath)) {
                log.info("File already exists, skipping download: {}", outputPath);
                return;
            }
            URL url = new URL(mediaUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            try (InputStream in = connection.getInputStream();
                 FileOutputStream out = new FileOutputStream(outputPath.toFile())) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            log.info("Media downloaded successfully: {}", outputPath);
            // Add your processing logic here (store in database, send to another service, etc.)
        } catch (Exception e) {
            log.error("Error downloading media from {}: {}", mediaUrl, e.getMessage(), e);
        }
    }

}
