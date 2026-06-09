package com.santediagnostics.controllers;

import java.time.LocalDateTime;
import java.time.Duration;

public class TestRequest {
    private LocalDateTime orderedAt;
    private Integer tatHours;

    public LocalDateTime getExpectedCompletion(){
        if(orderedAt == null || tatHours == null) return null;
        return orderedAt.plusHours(tatHours);
    }
    public long getRemainingSeconds(){
        LocalDateTime expected = getExpectedCompletion();
        if (expected == null) return 0;

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expected)) return 0;

        return Duration.between(now,expected).getSeconds();

    }

    public String getTimeRemainingFormatted(){
        long seconds  = getRemainingSeconds();
        if (seconds <= 0) return "Overdue";

        long hours = seconds /3600;
        long minutes = (seconds % 3600) /60;
        long secs = seconds % 60;

        if (hours > 0) {
            return String.format("%dh %02dm", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%02d:%02d", minutes, secs);
        } else {
            return String.format("%d sec", secs);
        }
    }

    public Integer getTatHours(){return tatHours;}
    public void setTatHours(Integer tatHours){this.tatHours = tatHours;}
}