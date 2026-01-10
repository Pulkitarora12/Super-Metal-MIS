package com.notes.notes.dto.taskManagementDTO;

import com.notes.notes.entity.taskModuleEntities.Task.TaskPriority;
import com.notes.notes.entity.taskModuleEntities.TaskTemplate.RecurrenceFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CreateTemplateRequestDTO {

    @NotBlank(message = "Template name is required")
    private String templateName;

    private String description;

    @NotNull(message = "Recurrence frequency is required")
    private RecurrenceFrequency recurrenceFrequency;

    // For WEEKLY
    private Integer dayOfWeek; // 1-7 (Monday to Sunday)

    // For MONTHLY
    private Integer dayOfMonth; // 1-31

    // For QUARTERLY
    private Integer quarterlyDay; // 1-31
    private Integer quarterlyMonth; // 1, 4, 7, 10 (Jan, Apr, Jul, Oct)

    // For YEARLY
    private Integer yearlyDay; // 1-31
    private Integer yearlyMonth; // 1-12

    @NotNull(message = "Priority is required")
    private TaskPriority priority;

    @NotNull(message = "Days before to flash is required")
    @Min(value = 0, message = "Days before to flash must be non-negative")
    private Integer daysBeforeToFlash;
}