package com.reservalink.api.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MpWebhookRequest(
        String id,
        String type,
        String action,
        MpWebhookData data,
        @JsonProperty("live_mode")
        Boolean liveMode,
        @JsonProperty("api_version")
        String apiVersion,
        @JsonProperty("date_created")
        String dateCreated,
        @JsonProperty("user_id")
        String userId
) {
    public MpWebhookType typeEnum() {
        return MpWebhookType.from(type);
    }
}
