package com.meeting.calendar_assistant.model;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MeetingValidator implements ConstraintValidator<ValidMeeting, Meeting> {
    @Override
    public boolean isValid(Meeting meeting, ConstraintValidatorContext context) {
        if (meeting.getStartTime() == null || meeting.getEndTime() == null) {
            return true; // Allow other validators to handle null values
        }
        return meeting.getEndTime().isAfter(meeting.getStartTime());
    }
}
