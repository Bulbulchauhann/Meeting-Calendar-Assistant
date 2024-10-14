package com.meeting.calendar_assistant.repository;

import com.meeting.calendar_assistant.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Find employees by name containing a specified string (case insensitive)
    List<Employee> findByNameContainingIgnoreCase(String name);

    // Find employees with at least one meeting
    List<Employee> findByMeetingsIsNotNull();
}
