package com.wds.maytapi.dto;
import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
class ConversationData {
    private String id;
    private String name;

    @JsonCreator
    public static ConversationData fromString(String value) {
        ConversationData data = new ConversationData();
        data.id = value;
        return data;
    }
}