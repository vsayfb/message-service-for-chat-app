package com.example.message_service.external.dto;

import java.util.HashSet;

import lombok.Data;

@Data
public class NewMemberResponse {

    private String id;
    private String roomId;
    private HashSet<String> sessionIds;
    private String joinedAt;
}
