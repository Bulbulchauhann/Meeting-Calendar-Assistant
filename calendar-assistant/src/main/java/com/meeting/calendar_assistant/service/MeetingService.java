package com.meeting.calendar_assistant.service;

import com.meeting.calendar_assistant.exception.MeetingConflictException;
import com.meeting.calendar_assistant.exception.EmployeeNotFoundException;
import com.meeting.calendar_assistant.model.Employee;
import com.meeting.calendar_assistant.model.Meeting;
import com.meeting.calendar_assistant.repository.EmployeeRepository;
import com.meeting.calendar_assistant.repository.MeetingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MeetingService {

    private static final Logger logger = LoggerFactory.getLogger(MeetingService.class);
    private static final int SLOT_INCREMENT_MINUTES = 30;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    // Book a meeting
    public Meeting bookMeeting(Long employeeId, LocalDateTime startTime, LocalDateTime endTime, String title) {
        validateMeetingTimes(startTime, endTime);
        Employee employee = findEmployeeById(employeeId);

        checkMeetingConflicts(employeeId, startTime, endTime);

        Meeting meeting = new Meeting(startTime, endTime, employee, title);
        return meetingRepository.save(meeting);
    }

    // Find free slots for two employees
    public List<LocalDateTime> findFreeSlots(Long employee1Id, Long employee2Id, int durationInMinutes) {
        List<Meeting> allMeetings = getAllMeetings(employee1Id, employee2Id);
        return findAvailableSlots(allMeetings, durationInMinutes);
    }

    // Find conflicted participants
    public List<Long> findConflictedParticipants(List<Long> participantIds, LocalDateTime startTime, LocalDateTime endTime) {
        List<Long> conflictedParticipants = new ArrayList<>();
        for (Long participantId : participantIds) {
            if (hasMeetingConflict(participantId, startTime, endTime)) {
                conflictedParticipants.add(participantId);
            }
        }
        return conflictedParticipants;
    }

    // Helper methods
    private void validateMeetingTimes(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }
    }

    private Employee findEmployeeById(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
    }

    private void checkMeetingConflicts(Long employeeId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Meeting> existingMeetings = meetingRepository.findByEmployee_Id(employeeId);
        logger.info("Existing meetings for employee {}: {}", employeeId, existingMeetings);

        for (Meeting meeting : existingMeetings) {
            if (isOverlapping(meeting.getStartTime(), meeting.getEndTime(), startTime, endTime)) {
                logger.error("Meeting conflict detected for employee {}: {} overlaps with requested time {} to {}",
                        employeeId, meeting, startTime, endTime);
                throw new MeetingConflictException("Meeting time conflicts with existing meetings");
            }
        }
    }

    private boolean hasMeetingConflict(Long participantId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Meeting> meetings = meetingRepository.findByEmployee_Id(participantId);
        return meetings.stream().anyMatch(meeting -> isOverlapping(meeting.getStartTime(), meeting.getEndTime(), startTime, endTime));
    }

    private boolean isOverlapping(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private List<Meeting> getAllMeetings(Long employee1Id, Long employee2Id) {
        List<Meeting> allMeetings = new ArrayList<>();
        allMeetings.addAll(meetingRepository.findByEmployee_Id(employee1Id));
        allMeetings.addAll(meetingRepository.findByEmployee_Id(employee2Id));
        allMeetings.sort((m1, m2) -> m1.getStartTime().compareTo(m2.getStartTime()));
        return allMeetings;
    }

    private List<LocalDateTime> findAvailableSlots(List<Meeting> allMeetings, int durationInMinutes) {
        List<LocalDateTime> freeSlots = new ArrayList<>();
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        LocalDateTime lastEndTime = startOfDay;

        for (Meeting meeting : allMeetings) {
            if (meeting.getStartTime().isAfter(lastEndTime)) {
                addFreeSlots(freeSlots, lastEndTime, meeting.getStartTime(), durationInMinutes);
            }
            lastEndTime = meeting.getEndTime().isAfter(lastEndTime) ? meeting.getEndTime() : lastEndTime;
        }

        if (lastEndTime.isBefore(endOfDay)) {
            addFreeSlots(freeSlots, lastEndTime, endOfDay, durationInMinutes);
        }
        return freeSlots;
    }

    private void addFreeSlots(List<LocalDateTime> freeSlots, LocalDateTime gapStart, LocalDateTime gapEnd, int durationInMinutes) {
        while (gapStart.plusMinutes(durationInMinutes).isBefore(gapEnd)) {
            freeSlots.add(gapStart);
            gapStart = gapStart.plusMinutes(SLOT_INCREMENT_MINUTES);
        }
    }
}
