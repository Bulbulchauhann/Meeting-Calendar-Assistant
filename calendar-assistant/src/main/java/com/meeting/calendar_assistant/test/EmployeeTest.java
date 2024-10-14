package com.meeting.calendar_assistant.test;

import com.meeting.calendar_assistant.model.Employee;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmployeeTest {

    private final Validator validator;

    public EmployeeTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidName() {
        Employee employee = new Employee("Bulbul");
        assertEquals("Bulbul", employee.getName());
    }

    @Test
    void testEmptyName() {
        Employee employee = new Employee("");
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        assertEquals(2, violations.size()); // Expect two violations
        assertTrue(violations.stream().anyMatch(v -> "Name cannot be blank".equals(v.getMessage())));
        assertTrue(violations.stream().anyMatch(v -> "Name must be between 1 and 100 characters".equals(v.getMessage())));
    }

    @Test
    void testWhitespaceName() {
        Employee employee = new Employee("   ");
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        assertEquals(1, violations.size()); // Expect one violation
        assertEquals("Name cannot be blank", violations.iterator().next().getMessage());
    }

    @Test
    void testNameSizeValid() {
        Employee employee = new Employee("A");
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        assertEquals(0, violations.size()); // Expect no violations
    }

    @Test
    void testNameSizeTooLong() {
        Employee employee = new Employee("A".repeat(101)); // Invalid name
        Set<ConstraintViolation<Employee>> violations = validator.validate(employee);

        assertEquals(1, violations.size()); // Expect one violation
        assertEquals("Name must be between 1 and 100 characters", violations.iterator().next().getMessage());
    }
}
