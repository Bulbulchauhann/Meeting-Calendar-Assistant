package com.meeting.calendar_assistant.validation;

import com.meeting.calendar_assistant.model.Meeting;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MeetingValidator implements ConstraintValidator<ValidMeeting, Meeting> {

    @Override
    public boolean isValid(Meeting meeting, ConstraintValidatorContext context) {
        if (meeting == null) {
            return true; // Consider null meeting as valid (to avoid validation on null objects)
        }
        // Check if startTime and endTime are set and validate their order
        return meeting.getStartTime() != null && meeting.getEndTime() != null &&
                meeting.getEndTime().isAfter(meeting.getStartTime());
    }
}
