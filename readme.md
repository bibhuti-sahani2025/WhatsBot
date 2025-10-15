## MaytAPI Spring Boot Integration

## Setup Instructions

1. **Get MaytAPI Credentials:**
   - Sign up at https://maytapi.com
   - Get your Product ID, Phone ID, and API Token
   - Add them to application.yml

2. **Configure application.yml:**
   ```yaml
   maytapi:
     base-url: https://api.maytapi.com/api
     product-id: YOUR_PRODUCT_ID
     phone-id: YOUR_PHONE_ID
     api-token: YOUR_API_TOKEN
   ```

3. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

## API Endpoints

### Send Text Message
```bash
POST http://localhost:8080/api/whatsapp/send
?phone=919876543210
&message=Hello from Spring Boot!
```

### Send Image
```bash
POST http://localhost:8080/api/whatsapp/send-image
?phone=919876543210
&imageUrl=https://example.com/image.jpg
&caption=Check this out!
```

### Send Document
```bash
POST http://localhost:8080/api/whatsapp/send-document
?phone=919876543210
&documentUrl=https://example.com/file.pdf
&caption=Important document
```

### Send Audio
```bash
POST http://localhost:8080/api/whatsapp/send-audio
?phone=919876543210
&audioUrl=https://example.com/audio.mp3
&caption=Check this audio
```

### Get Messages
```bash
GET http://localhost:8080/api/whatsapp/messages?page=1&limit=20
```

### Get Status
```bash
GET http://localhost:8080/api/whatsapp/status
```

### Webhook (Configure in MaytAPI Dashboard)
```
POST http://your-server.com/api/whatsapp/webhook
```

## Testing with cURL

```bash
# Send text message
curl -X POST "http://localhost:8080/api/whatsapp/send?phone=919876543210&message=Hello%20World"

# Get status
curl -X GET "http://localhost:8080/api/whatsapp/status"
```

## Next Steps

1. Add database layer (JPA/Hibernate)
2. Implement message history storage
3. Add authentication/authorization
4. Create scheduled messages feature
5. Add bulk messaging capability
6. Implement message templates
7. Add analytics and reporting"# WhatsBot" 
