package com.wds.maytapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageStatus {
    private String status; // "sent", "delivered", "read", etc.
}
