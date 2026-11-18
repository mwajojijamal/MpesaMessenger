package com.example.mpesamessenger;

public class MpesaMessage {
    private String body;
    private String time;
    private boolean isBlurred;

    public MpesaMessage(String body, String time) {
        this.body = body;
        this.time = time;
        this.isBlurred = true;
    }

    public String getBody() { return body; }
    public String getTime() { return time; }
    public boolean isBlurred() { return isBlurred; }
    public void toggleBlur() { this.isBlurred = !this.isBlurred; }
}