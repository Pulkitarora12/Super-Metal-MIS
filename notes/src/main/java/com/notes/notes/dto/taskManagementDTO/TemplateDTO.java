package com.notes.notes.dto.taskManagementDTO;

import com.notes.notes.entity.taskModuleEntities.Task;
import com.notes.notes.entity.taskModuleEntities.TaskTemplate;
import lombok.Data;

@Data
public class TemplateDTO {

    private String title;
    private String description;
    private Long flashTime;
    private Task.TaskPriority priority;
    private TaskTemplate.TaskFrequency taskFrequency;
}
