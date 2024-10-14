package com.meeting.calendar_assistant.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@ValidMeeting // Custom validation to ensure end time is after start time
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Start time cannot be null")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;

    @NotNull(message = "End time cannot be null")
    @Future(message = "End time must be in the future")
    private LocalDateTime endTime;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @NotNull(message = "Title cannot be null")
    private String title;

    public Meeting(LocalDateTime startTime, LocalDateTime endTime, Employee employee, String title) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.employee = employee;
        this.title = title;
    }
}
