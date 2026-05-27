package com.friendify.app.shared.notification;

import java.util.Map;

public class NotificationEvent {

    private String channel;
    private String recipient;
    private String templateCode;
    private Map<String, Object> param;
    private String subject;
    private String body;

    public NotificationEvent() {
    }

    public NotificationEvent(
            String channel,
            String recipient,
            String templateCode,
            Map<String, Object> param,
            String subject,
            String body) {
        this.channel = channel;
        this.recipient = recipient;
        this.templateCode = templateCode;
        this.param = param == null ? Map.of() : Map.copyOf(param);
        this.subject = subject;
        this.body = body;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public Map<String, Object> getParam() {
        return param;
    }

    public void setParam(Map<String, Object> param) {
        this.param = param == null ? Map.of() : Map.copyOf(param);
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public static final class Builder {
        private String channel;
        private String recipient;
        private String templateCode;
        private Map<String, Object> param = Map.of();
        private String subject;
        private String body;

        private Builder() {
        }

        public Builder channel(String channel) {
            this.channel = channel;
            return this;
        }

        public Builder recipient(String recipient) {
            this.recipient = recipient;
            return this;
        }

        public Builder templateCode(String templateCode) {
            this.templateCode = templateCode;
            return this;
        }

        public Builder param(Map<String, Object> param) {
            this.param = param == null ? Map.of() : Map.copyOf(param);
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public NotificationEvent build() {
            return new NotificationEvent(channel, recipient, templateCode, param, subject, body);
        }
    }
}
