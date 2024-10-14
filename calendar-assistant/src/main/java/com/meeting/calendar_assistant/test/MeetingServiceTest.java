package com.meeting.calendar_assistant.test;

import com.meeting.calendar_assistant.exception.MeetingConflictException;
import com.meeting.calendar_assistant.exception.EmployeeNotFoundException;
import com.meeting.calendar_assistant.model.Employee;
import com.meeting.calendar_assistant.model.Meeting;
import com.meeting.calendar_assistant.repository.EmployeeRepository;
import com.meeting.calendar_assistant.repository.MeetingRepository;
import com.meeting.calendar_assistant.service.MeetingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MeetingServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private MeetingRepository meetingRepository;

    @InjectMocks
    private MeetingService meetingService;

    private Employee employee;
    private static final Long EMPLOYEE_ID = 1L; // Constant for employee ID

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employee = new Employee();
        employee.setId(EMPLOYEE_ID);
    }

    @Test
    void testBookMeeting_Success() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(1);
        String title = "Team Sync";

        when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
        when(meetingRepository.findByEmployee_Id(EMPLOYEE_ID)).thenReturn(Collections.emptyList());
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Meeting meeting = meetingService.bookMeeting(EMPLOYEE_ID, startTime, endTime, title);

        assertNotNull(meeting);
        assertEquals(startTime, meeting.getStartTime());
        assertEquals(endTime, meeting.getEndTime());
        assertEquals(employee, meeting.getEmployee());
        assertEquals(title, meeting.getTitle());

        verify(meetingRepository).save(meeting);
    }

    @Test
    void testBookMeeting_Conflict() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(1);
        String title = "New Meeting";

        Meeting existingMeeting = new Meeting(startTime, endTime, employee, "Existing Meeting");

        when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
        when(meetingRepository.findByEmployee_Id(EMPLOYEE_ID)).thenReturn(Collections.singletonList(existingMeeting));

        MeetingConflictException thrown = assertThrows(MeetingConflictException.class, () -> {
            meetingService.bookMeeting(EMPLOYEE_ID, startTime, endTime, title);
        });

        assertEquals("Meeting time conflicts with existing meetings", thrown.getMessage());
    }

    @Test
    void testBookMeeting_EmployeeNotFound() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(1);
        String title = "Meeting Title";

        when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.empty());

        EmployeeNotFoundException thrown = assertThrows(EmployeeNotFoundException.class, () -> {
            meetingService.bookMeeting(EMPLOYEE_ID, startTime, endTime, title);
        });

        assertEquals("Employee not found", thrown.getMessage());
    }

    @Test
    void testBookMeeting_StartTimeAfterEndTime() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(2);
        LocalDateTime endTime = startTime.minusHours(1); // End time is before start time
        String title = "Invalid Meeting";

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            meetingService.bookMeeting(EMPLOYEE_ID, startTime, endTime, title);
        });

        assertEquals("Start time must be before end time", thrown.getMessage());
    }

    @Test
    void testBookMeeting_OverlappingPartialMeeting() {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(2);
        String title = "New Meeting";

        Meeting existingMeeting = new Meeting(startTime.minusMinutes(30), startTime.plusMinutes(30), employee, "Existing Meeting");

        when(employeeRepository.findById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
        when(meetingRepository.findByEmployee_Id(EMPLOYEE_ID)).thenReturn(Collections.singletonList(existingMeeting));

        MeetingConflictException thrown = assertThrows(MeetingConflictException.class, () -> {
            meetingService.bookMeeting(EMPLOYEE_ID, startTime, endTime, title);
        });

        assertEquals("Meeting time conflicts with existing meetings", thrown.getMessage());
    }
}
