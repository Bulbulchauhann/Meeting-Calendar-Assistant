package com.meeting.calendar_assistant.test;

import com.meeting.calendar_assistant.model.Employee;
import com.meeting.calendar_assistant.model.Meeting;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class MeetingTest {

    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid Meeting: No violations expected")
    public void validMeeting() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = LocalDateTime.now().plusDays(2);
        Meeting meeting = createMeeting(startTime, endTime);
        Set<ConstraintViolation<Meeting>> violations = validator.validate(meeting);
        assertTrue(violations.isEmpty(), "Expected no violations for a valid meeting");
    }

    @Test
    @DisplayName("Null Start Time: Violation expected")
    public void startTimeNull() {
        Meeting meeting = new Meeting();
        meeting.setEndTime(LocalDateTime.now().plusDays(1));
        meeting.setEmployee(new Employee());
        meeting.setTitle("Test Meeting");
        assertViolationMessage(meeting, "Start time cannot be null");
    }

    @Test
    @DisplayName("Null End Time: Violation expected")
    public void endTimeNull() {
        Meeting meeting = new Meeting();
        meeting.setStartTime(LocalDateTime.now().plusDays(1));
        meeting.setEmployee(new Employee());
        meeting.setTitle("Test Meeting");
        assertViolationMessage(meeting, "End time cannot be null");
    }

    @Test
    @DisplayName("Start Time in the Past: Violation expected")
    public void startTimeInPast() {
        Meeting meeting = createMeeting(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        assertViolationMessage(meeting, "Start time must be in the future");
    }

    @Test
    @DisplayName("End Time Before Start Time: Violation expected")
    public void endTimeBeforeStartTime() {
        Meeting meeting = createMeeting(LocalDateTime.now().plusDays(1), LocalDateTime.now());
        assertViolationMessage(meeting, "End time must be after start time"); // Ensure your validation message is correct
    }

    // Helper method to create a valid Meeting object
    private Meeting createMeeting(LocalDateTime startTime, LocalDateTime endTime) {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setName("John Doe");

        Meeting meeting = new Meeting();
        meeting.setStartTime(startTime);
        meeting.setEndTime(endTime);
        meeting.setEmployee(employee);
        meeting.setTitle("Valid Meeting");
        return meeting;
    }

    // Helper method to assert specific validation violation messages
    private void assertViolationMessage(Meeting meeting, String expectedMessage) {
        Set<ConstraintViolation<Meeting>> violations = validator.validate(meeting);
        assertFalse(violations.isEmpty(), "Expected at least one violation");
        assertEquals(expectedMessage, violations.iterator().next().getMessage());
    }
}
