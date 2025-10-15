# MaytAPI Integration - Working Implementation Guide

**Date:** October 14, 2025  
**Status:** ✅ ALL FEATURES TESTED AND WORKING

## ⚠️ CRITICAL: DO NOT MODIFY THESE WORKING IMPLEMENTATIONS

This document describes all currently working features. Any future changes should be additive only and must not break existing functionality.

---

## 1. ✅ WORKING: Image Sending (System to Customer)

### Backend Implementation
**File:** `src/main/java/com/wds/maytapi/service/MaytApiService.java`

**Critical Method:** `sendMedia(String toNumber, String mediaUrl, String caption, String mediaType)`

**Working Format:**
```java
// MUST use this exact payload structure for MaytAPI
Map<String, Object> request = new HashMap<>();
request.put("to_number", formatPhoneNumber(toNumber));
request.put("type", "media");  // MUST be "media", not "image"
request.put("message", base64Data);  // MUST be base64 data URI format
if (caption != null && !caption.isEmpty()) {
    request.put("text", caption);  // Caption goes in "text" field
}
```

**Required Base64 Format:**
```
data:image/png;base64,iVBORw0KG...
data:image/jpeg;base64,/9j/4AAQ...
```

**Key Points:**
- ✅ Downloads image from Cloudinary URL
- ✅ Converts to base64 with data URI prefix
- ✅ Uses `type="media"` (NOT `type="image"`)
- ✅ Image data goes in `message` field (NOT `url` or `text`)
- ✅ Caption goes in `text` field (optional)

**DO NOT:**
- ❌ Change `type` from "media" to "image"
- ❌ Send URL directly instead of base64
- ❌ Put image data in `url` or `text` fields
- ❌ Remove the data URI prefix (data:image/png;base64,...)

---

## 2. ✅ WORKING: Audio Sending (System to Customer)

### Backend Implementation
**File:** `src/main/java/com/wds/maytapi/service/MaytApiService.java`

**Method:** `sendMedia(String toNumber, String mediaUrl, String caption, String mediaType)`

**Working Format for Audio:**
```java
// Same as images - uses base64 encoding
request.put("type", "media");
request.put("message", base64AudioData);  // data:audio/mp3;base64,...
if (caption != null) {
    request.put("text", caption);
}
```

**Supported Audio Formats:**
- ✅ MP3 (audio/mp3)
- ✅ OGG (audio/ogg) - WhatsApp voice notes
- ✅ WAV (audio/wav)
- ✅ M4A (audio/mp4)
- ✅ AAC (audio/aac)

**Key Points:**
- ✅ Downloads audio from Cloudinary URL
- ✅ Converts to base64 with data URI prefix
- ✅ Auto-detects MIME type from file extension
- ✅ Works for both individuals and groups
- ✅ Cloudinary uses `/video/upload` endpoint for audio

**Frontend Integration:**
- Upload audio file via file picker
- Cloudinary uploads to `/video/upload` endpoint
- Preview with HTML5 audio player
- Send to backend like images

**Incoming Audio:**
- ✅ Auto-downloads to `downloads/audio/PHONE_TIMESTAMP.ogg`
- ✅ Handles both PTT (push-to-talk) and regular audio
- ✅ Works for individual and group messages

**DO NOT:**
- ❌ Change the base64 encoding logic
- ❌ Remove audio MIME type detection
- ❌ Modify the Cloudinary video upload endpoint

---

## 2. ✅ WORKING: Text Message Sending

### Backend Implementation
**File:** `src/main/java/com/wds/maytapi/service/MaytApiService.java`

**Method:** `sendTextMessage(String toNumber, String message)`

**Working Format:**
```java
SendMessageRequest request = SendMessageRequest.builder()
    .to_number(formatPhoneNumber(toNumber))
    .message(message)
    .type("text")
    .build();
```

**Key Points:**
- ✅ Works for individual users (phone numbers)
- ✅ Works for groups (with @g.us suffix)
- ✅ Phone formatting handled automatically

---

## 3. ✅ WORKING: Group Messaging

### Phone Number Formatting
**File:** `src/main/java/com/wds/maytapi/service/MaytApiService.java`

**Method:** `formatPhoneNumber(String phone)`

**Critical Logic:**
```java
// If already a JID (group or user), return as is
if (phone.endsWith("@g.us") || phone.endsWith("@c.us")) {
    return phone;
}
// For regular numbers, add country code
String cleaned = phone.replaceAll("[^0-9]", "");
if (!cleaned.startsWith("91") && cleaned.length() == 10) {
    cleaned = "91" + cleaned; // India country code
}
return cleaned;
```

**Key Points:**
- ✅ Groups: Use format `120363XXXXXXXXX@g.us`
- ✅ Individual: Use phone number (auto-formatted)
- ✅ Preserves @g.us and @c.us suffixes

---

## 4. ✅ WORKING: Incoming Webhook Processing

### Backend Implementation
**File:** `src/main/java/com/wds/maytapi/controller/WhatsappController.java`

**Endpoint:** `POST /api/whatsapp/webhook`

**Features:**
- ✅ Receives incoming text messages
- ✅ Auto-downloads incoming images to `downloads/images/`
- ✅ Auto-downloads incoming audio to `downloads/audio/`
- ✅ Auto-downloads incoming documents to `downloads/documents/`
- ✅ Logs location and vCard messages
- ✅ Auto-responds to "hello" messages

**Key Points:**
- ✅ Uses `Map<String, Object>` to handle dynamic webhook payloads
- ✅ Handles error webhooks gracefully
- ✅ Downloads media with timestamp-based filenames

**DO NOT:**
- ❌ Change webhook payload type back to `WebhookPayload` class
- ❌ Remove the Map-based processing logic
- ❌ Modify the media download paths

---

## 5. ✅ WORKING: Angular UI with Cloudinary Upload

### Frontend Implementation
**Files:**
- `whatsapp-ui/src/app/whatsapp-tester/whatsapp-tester.component.ts`
- `whatsapp-ui/src/app/whatsapp-tester/whatsapp-tester.component.html`

**Features:**
- ✅ Upload image from file picker
- ✅ Upload to Cloudinary (cloud: dejkcs9v6, preset: whatsapp_unsigned)
- ✅ Send image URL to backend
- ✅ Support for text, image, document message types
- ✅ Group messaging support

**Critical Cloudinary Config:**
```typescript
cloudName: 'dejkcs9v6'
uploadPreset: 'whatsapp_unsigned'
```

**DO NOT:**
- ❌ Change Cloudinary cloud name or preset
- ❌ Remove the image upload flow
- ❌ Modify the backend API call structure

---

## 6. ✅ WORKING: CORS Configuration

### Backend Implementation
**File:** `src/main/java/com/wds/maytapi/config/WebConfig.java`

**Allowed Origins:**
- `http://localhost:4200` (Angular dev server)

**DO NOT:**
- ❌ Remove CORS configuration
- ❌ Change allowed origins without testing Angular app

---

## 7. API Endpoints Reference

### Send Text Message
```
POST http://localhost:8080/api/whatsapp/send
?phone=919876543210
&message=Hello World
```

### Send Image
```
POST http://localhost:8080/api/whatsapp/send-image
?phone=919876543210
&imageUrl=https://res.cloudinary.com/dejkcs9v6/image/upload/v1234567890/xyz.png
&caption=Check this out
```

### Send Document
```
POST http://localhost:8080/api/whatsapp/send-document
?phone=919876543210
&documentUrl=https://example.com/file.pdf
&caption=Important document
```

### Send Audio
```
POST http://localhost:8080/api/whatsapp/send-audio
?phone=919876543210
&audioUrl=https://res.cloudinary.com/dejkcs9v6/video/upload/v1234567890/audio.mp3
&caption=Listen to this
```

### Webhook
```
POST http://localhost:8080/api/whatsapp/webhook
(Receives webhooks from MaytAPI)
```

---

## 8. Testing Checklist

Before making ANY changes to the codebase, verify these work:

- [ ] Send text message to individual (via UI and Postman)
- [ ] Send text message to group (via UI and Postman)
- [ ] Upload image and send (via UI)
- [ ] Upload audio and send (via UI)
- [ ] Receive incoming text message
- [ ] Receive incoming image (should auto-download)
- [ ] Receive incoming audio (should auto-download)
- [ ] Auto-reply to "hello" message
- [ ] No CORS errors in browser console
- [ ] No errors in Spring Boot logs

---

## 9. Common Pitfalls to Avoid

### ❌ DON'T DO THIS:
1. Change `type="media"` to `type="image"` in sendMedia
2. Send image URL directly instead of base64
3. Use `WebhookPayload` class for webhook processing
4. Remove phone number formatting logic
5. Change Cloudinary configuration
6. Modify CORS allowed origins without testing
7. Remove the base64 conversion in sendMedia

### ✅ DO THIS INSTEAD:
1. Keep all working code as-is
2. Add new features as separate methods/endpoints
3. Test thoroughly before committing
4. Document any new changes in this file
5. Maintain backward compatibility

---

## 10. Future Enhancements (Safe to Add)

These can be added WITHOUT breaking existing functionality:

- [x] ~~Send audio messages~~ ✅ **COMPLETED**
- [ ] Send video messages (new endpoint)
- [ ] Send location (new endpoint)
- [ ] Send vCard/contacts (new endpoint)
- [ ] Database storage for messages
- [ ] Message history UI
- [ ] Scheduled messages
- [ ] Bulk messaging
- [ ] Message templates
- [ ] Analytics dashboard
- [ ] Voice recording directly from browser
- [ ] Audio playback controls in UI

**Rule:** Always create NEW methods/endpoints. Never modify working ones.

---

## Version History

| Date | Feature | Status |
|------|---------|--------|
| Oct 14, 2025 | Audio sending & receiving | ✅ Working |
| Oct 14, 2025 | Image sending (base64) | ✅ Working |
| Oct 14, 2025 | Text messaging | ✅ Working |
| Oct 14, 2025 | Group messaging | ✅ Working |
| Oct 14, 2025 | Incoming webhooks | ✅ Working |
| Oct 14, 2025 | Cloudinary integration | ✅ Working |
| Oct 14, 2025 | Angular UI | ✅ Working |

---

## Emergency Rollback

If something breaks, restore these critical files:
1. `MaytApiService.java` - Image sending logic
2. `WhatsappController.java` - Webhook processing
3. `whatsapp-tester.component.ts` - UI logic
4. `WebConfig.java` - CORS config

**Backup Location:** Consider creating a backup of these files before major changes.

---

**Remember:** Working code is sacred. Don't fix what isn't broken! 🔒
