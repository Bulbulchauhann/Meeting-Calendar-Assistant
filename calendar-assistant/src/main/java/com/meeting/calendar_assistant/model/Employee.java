package com.meeting.calendar_assistant.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be blank")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    private String name;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Meeting> meetings = new ArrayList<>();

    public Employee(String name) {
        this.name = name;
    }

    public void addMeeting(Meeting meeting) {
        meetings.add(meeting);
        meeting.setEmployee(this);
    }

    public void removeMeeting(Meeting meeting) {
        meetings.remove(meeting);
        meeting.setEmployee(null);
    }

    @Override
    public String toString() {
        return String.format("Employee{id=%d, name='%s'}", id, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        Employee employee = (Employee) o;
        return id != null && id.equals(employee.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
