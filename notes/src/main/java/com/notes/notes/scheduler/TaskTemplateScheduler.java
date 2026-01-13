package com.notes.notes.scheduler;

import com.notes.notes.repository.taskModuleRepositories.TaskTemplateRepository;
import com.notes.notes.service.taskModuleServices.TaskService;
import com.notes.notes.service.taskModuleServices.TaskTemplateService;
import org.springframework.stereotype.Component;

@Component
public class TaskTemplateScheduler {

    private final TaskTemplateRepository taskTemplateRepository;
    private final TaskService taskService;
    private final TaskTemplateService taskTemplateService;

    public TaskTemplateScheduler(TaskTemplateRepository taskTemplateRepository,
                                 TaskService taskService,
                                 TaskTemplateService taskTemplateService) {
        this.taskTemplateRepository = taskTemplateRepository;
        this.taskService = taskService;
        this.taskTemplateService = taskTemplateService;
    }

}
