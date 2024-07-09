package com.example.message_service.external.dto;

import lombok.Data;

@Data
public class NewMemberResponse {

    private String id;
    private String roomId;
    private String[] sessionIds;
    private String joinedAt;
}
