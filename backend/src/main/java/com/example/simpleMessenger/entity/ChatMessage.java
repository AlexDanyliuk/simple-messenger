package com.example.simpleMessenger.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "chat_message")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String chatId;
    private Long senderId;
    private Long recipientId;

    @Convert(converter = com.example.simpleMessenger.config.MessageEncryptionConverter.class)
    private String content;
    private Date timestamp;
    private boolean isRead = false;

    private Date deliveredAt;
    private Date readAt;
    private Date editedAt;
    private boolean pinned = false;

    // File attachment fields
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Long fileSize;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Column(columnDefinition = "TEXT")
    private String reactionsRaw = "{}";

    @Transient
    @JsonProperty("reactions")
    public Map<String, List<Long>> getReactions() {
        if (reactionsRaw == null || reactionsRaw.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            Map<String, List<Long>> parsed = OBJECT_MAPPER.readValue(
                    reactionsRaw,
                    new TypeReference<Map<String, List<Long>>>() {}
            );
            return parsed != null ? parsed : new LinkedHashMap<>();
        } catch (Exception ex) {
            return new LinkedHashMap<>();
        }
    }

    @JsonProperty("reactions")
    public void setReactions(Map<String, List<Long>> reactions) {
        Map<String, List<Long>> sanitized = new LinkedHashMap<>();
        if (reactions != null) {
            reactions.forEach((emoji, users) -> {
                if (emoji != null && !emoji.isBlank()) {
                    sanitized.put(emoji, users == null ? new ArrayList<>() : new ArrayList<>(users));
                }
            });
        }
        try {
            this.reactionsRaw = OBJECT_MAPPER.writeValueAsString(sanitized);
        } catch (Exception ex) {
            this.reactionsRaw = "{}";
        }
    }
}