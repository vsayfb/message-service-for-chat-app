package com.example.message_service.dto;

import lombok.Data;

@Data
public class RoomMember {

    private String userId;
    private String username;
    private String profilePicture;
    private String memberId;
}
