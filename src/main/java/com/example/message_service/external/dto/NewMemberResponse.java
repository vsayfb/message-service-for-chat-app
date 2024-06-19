package com.example.message_service.external.dto;

import lombok.Data;

@Data
public class NewMemberResponse {

    private String memberId;
    private String roomId;
    private String userId;
    private String username;
    private String joinedAt;
}
