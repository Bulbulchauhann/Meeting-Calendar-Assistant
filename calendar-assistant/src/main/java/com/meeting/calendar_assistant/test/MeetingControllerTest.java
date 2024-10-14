package com.meeting.calendar_assistant.test;

import com.meeting.calendar_assistant.controller.MeetingController;
import com.meeting.calendar_assistant.exception.MeetingConflictException;
import com.meeting.calendar_assistant.model.Meeting;
import com.meeting.calendar_assistant.model.MeetingRequest;
import com.meeting.calendar_assistant.service.MeetingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MeetingControllerTest {

    @InjectMocks
    private MeetingController meetingController;

    @Mock
    private MeetingService meetingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void bookMeeting_Success() {
        Long employeeId = 1L;
        LocalDateTime startTime = LocalDateTime.of(2024, 10, 15, 10, 0);
        LocalDateTime endTime = startTime.plusHours(1);
        String title = "Team Meeting";

        MeetingRequest meetingRequest = new MeetingRequest();
        meetingRequest.setEmployeeId(employeeId);
        meetingRequest.setStartTime(startTime);
        meetingRequest.setEndTime(endTime);
        meetingRequest.setTitle(title);

        Meeting mockMeeting = new Meeting();
        mockMeeting.setStartTime(startTime);
        mockMeeting.setEndTime(endTime);
        mockMeeting.setTitle(title);

        when(meetingService.bookMeeting(employeeId, startTime, endTime, title)).thenReturn(mockMeeting);

        ResponseEntity<Meeting> response = meetingController.bookMeeting(meetingRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(title, response.getBody().getTitle());

        verify(meetingService, times(1)).bookMeeting(employeeId, startTime, endTime, title);
    }

    @ParameterizedTest
    @MethodSource("invalidMeetingTimesProvider")
    void bookMeeting_BadRequest(Long employeeId, LocalDateTime startTime, LocalDateTime endTime, String title) {
        MeetingRequest meetingRequest = new MeetingRequest();
        meetingRequest.setEmployeeId(employeeId);
        meetingRequest.setStartTime(startTime);
        meetingRequest.setEndTime(endTime);
        meetingRequest.setTitle(title);

        when(meetingService.bookMeeting(employeeId, startTime, endTime, title))
                .thenThrow(new IllegalArgumentException("Invalid meeting times"));

        ResponseEntity<Meeting> response = meetingController.bookMeeting(meetingRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        verify(meetingService, times(1)).bookMeeting(employeeId, startTime, endTime, title);
    }

    private static Stream<Arguments> invalidMeetingTimesProvider() {
        return Stream.of(
                Arguments.of(1L, LocalDateTime.of(2024, 10, 10, 10, 0), LocalDateTime.now(), "Invalid Meeting 1"),
                Arguments.of(1L, LocalDateTime.now(), LocalDateTime.now().minusHours(1), "Invalid Meeting 2")
        );
    }

    @Test
    void bookMeeting_Conflict() {
        Long employeeId = 1L;
        LocalDateTime startTime = LocalDateTime.of(2024, 10, 15, 10, 0);
        LocalDateTime endTime = startTime.plusHours(1);
        String title = "Conflicting Meeting";

        MeetingRequest meetingRequest = new MeetingRequest();
        meetingRequest.setEmployeeId(employeeId);
        meetingRequest.setStartTime(startTime);
        meetingRequest.setEndTime(endTime);
        meetingRequest.setTitle(title);

        when(meetingService.bookMeeting(employeeId, startTime, endTime, title))
                .thenThrow(new MeetingConflictException("Meeting conflict"));

        ResponseEntity<Meeting> response = meetingController.bookMeeting(meetingRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNull(response.getBody());

        verify(meetingService, times(1)).bookMeeting(employeeId, startTime, endTime, title);
    }

    @Test
    void findFreeSlots_Success() {
        Long employee1Id = 1L;
        Long employee2Id = 2L;
        int duration = 60;

        List<LocalDateTime> freeSlots = new ArrayList<>();
        freeSlots.add(LocalDateTime.of(2024, 10, 16, 10, 0));

        when(meetingService.findFreeSlots(employee1Id, employee2Id, duration)).thenReturn(freeSlots);

        ResponseEntity<List<LocalDateTime>> response = meetingController.findFreeSlots(employee1Id, employee2Id, duration);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(freeSlots, response.getBody());

        verify(meetingService, times(1)).findFreeSlots(employee1Id, employee2Id, duration);
    }

    @Test
    void findConflicts_Success() {
        List<Long> participantIds = List.of(1L, 2L, 3L);
        LocalDateTime startTime = LocalDateTime.of(2024, 10, 15, 10, 0);
        LocalDateTime endTime = startTime.plusHours(2);

        List<Long> conflictedParticipants = List.of(1L, 3L);

        when(meetingService.findConflictedParticipants(participantIds, startTime, endTime))
                .thenReturn(conflictedParticipants);

        ResponseEntity<List<Long>> response = meetingController.findConflicts(participantIds, startTime, endTime);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(conflictedParticipants, response.getBody());

        verify(meetingService, times(1)).findConflictedParticipants(participantIds, startTime, endTime);
    }
}
