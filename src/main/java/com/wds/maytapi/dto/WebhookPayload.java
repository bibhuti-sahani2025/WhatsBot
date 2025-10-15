package com.wds.maytapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookPayload {
    private String conversation;
    private String conversation_name;
    private WebhookMessage message;
    private Long phoneId;
    private String phone_id;
    private String productId;
    private String product_id;
    private String receiver;
    private String reply;
    private Long timestamp;
    private String type; // "message"
    private User user;
}
