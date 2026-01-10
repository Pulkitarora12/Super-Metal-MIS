package com.notes.notes.dto.taskManagementDTO;

import com.notes.notes.entity.taskModuleEntities.Task.TaskPriority;
import com.notes.notes.entity.taskModuleEntities.TaskTemplate.RecurrenceFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateTemplateRequestDTO {

    @NotBlank(message = "Template name is required")
    private String templateName;

    private String description;

    @NotNull(message = "Due Date is required")
    private LocalDate dueDate;

    @NotNull(message = "Recurrence frequency is required")
    private RecurrenceFrequency recurrenceFrequency;

    @NotNull(message = "Priority is required")
    private TaskPriority priority;

    @NotNull(message = "Days before to flash is required")
    @Min(value = 0, message = "Days before to flash must be non-negative")
    private Integer daysBeforeToFlash;
}