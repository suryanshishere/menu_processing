package com.shelfpulse.activation_automation.dto.websocket;

public class JoinEateryRoomDto {
    private Long eateryId;

    public JoinEateryRoomDto() {
    }

    public JoinEateryRoomDto(Long eateryId) {
        this.eateryId = eateryId;
    }

    public Long getEateryId() {
        return eateryId;
    }

    public void setEateryId(Long eateryId) {
        this.eateryId = eateryId;
    }
}
