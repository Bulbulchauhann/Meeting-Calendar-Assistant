package com.meeting.calendar_assistant.controller;

import com.meeting.calendar_assistant.exception.MeetingConflictException;
import com.meeting.calendar_assistant.model.Meeting;
import com.meeting.calendar_assistant.model.MeetingRequest;
import com.meeting.calendar_assistant.service.MeetingService;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    private static final Logger logger = LoggerFactory.getLogger(MeetingController.class);

    @Autowired
    private MeetingService meetingService;

    @PostMapping
    public ResponseEntity<Meeting> bookMeeting(@RequestBody MeetingRequest meetingRequest) {
        try {
            // Extract parameters from the request body
            Long employeeId = meetingRequest.getEmployeeId();
            LocalDateTime startTime = meetingRequest.getStartTime();
            LocalDateTime endTime = meetingRequest.getEndTime();
            String title = meetingRequest.getTitle();

            // Ensure these parameters are valid
            if (employeeId == null || startTime == null || endTime == null || title == null) {
                return ResponseEntity.badRequest().body(null); // Bad request for missing arguments
            }

            // Book the meeting using the service
            Meeting meeting = meetingService.bookMeeting(employeeId, startTime, endTime, title);
            return ResponseEntity.status(HttpStatus.CREATED).body(meeting);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null); // Bad request for invalid arguments
        } catch (MeetingConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null); // Conflict in booking
        } catch (Exception e) {
            // Log the exception and return a 500 status code
            e.printStackTrace(); // Replace with a logger in a real application
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/free-slots")
    public ResponseEntity<List<LocalDateTime>> findFreeSlots(
            @RequestParam Long employee1Id,
            @RequestParam Long employee2Id,
            @RequestParam int duration) {
        List<LocalDateTime> freeSlots = meetingService.findFreeSlots(employee1Id, employee2Id, duration);
        return ResponseEntity.ok(freeSlots);
    }

    @PostMapping("/conflicts")
    public ResponseEntity<List<Long>> findConflicts(
            @RequestBody List<Long> participantIds,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        List<Long> conflicts = meetingService.findConflictedParticipants(participantIds, startTime, endTime);
        return ResponseEntity.ok(conflicts);
    }

    @GetMapping("/health") // Health check endpoint
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Service is up and running!");
    }
}
