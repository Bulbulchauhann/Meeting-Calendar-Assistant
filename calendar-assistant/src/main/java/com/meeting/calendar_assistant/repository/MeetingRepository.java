package com.meeting.calendar_assistant.repository;

import com.meeting.calendar_assistant.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    // Retrieve meetings by employee ID
    List<Meeting> findByEmployee_Id(Long employeeId);

    // Retrieve meetings within a specific time range
    List<Meeting> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    // Retrieve meetings for an employee within a specific time range
    List<Meeting> findByEmployee_IdAndStartTimeBetween(Long employeeId, LocalDateTime startDate, LocalDateTime endDate);

    // Delete meetings associated with an employee ID
    void deleteByEmployee_Id(Long employeeId);
}
