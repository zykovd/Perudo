package com.suai.perudo.web;

/**
 * Created by dmitry on 20.12.18.
 */

public class ChatMessage {
    private String senderName;
    private String message;

    public ChatMessage(String senderName, String message) {
        this.senderName = senderName;
        this.message = message;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return senderName + " : " + message;
    }
}
