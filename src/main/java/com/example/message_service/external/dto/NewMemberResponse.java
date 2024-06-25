package com.example.message_service.external.dto;

import lombok.Data;

@Data
public class NewMemberResponse {

    private String id;
    private String username;
    private String userId;
    private String roomId;
    private String joinedAt;
}
