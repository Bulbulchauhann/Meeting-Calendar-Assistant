package com.meeting.calendar_assistant.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MeetingRequest {
    private Long employeeId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String title;
}
