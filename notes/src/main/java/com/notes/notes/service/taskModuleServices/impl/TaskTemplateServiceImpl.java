package com.notes.notes.service.taskModuleServices.impl;

import com.notes.notes.dto.taskManagementDTO.TemplateDTO;
import com.notes.notes.entity.authEntities.User;
import com.notes.notes.entity.taskModuleEntities.Task;
import com.notes.notes.entity.taskModuleEntities.TaskTemplate;
import com.notes.notes.repository.taskModuleRepositories.TaskTemplateRepository;
import com.notes.notes.service.taskModuleServices.TaskService;
import com.notes.notes.service.taskModuleServices.TaskTemplateService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskTemplateServiceImpl implements TaskTemplateService {

    private final TaskTemplateRepository taskTemplateRepository;
    private final TaskService taskService;

    public TaskTemplateServiceImpl(TaskTemplateRepository taskTemplateRepository, TaskService taskService) {
        this.taskTemplateRepository = taskTemplateRepository;
        this.taskService = taskService;
    }

    @Override
    public List<TaskTemplate> findAll() {
        return taskTemplateRepository.findAll();
    }

    @Override
    public TaskTemplate findById(Long id) {
        return taskTemplateRepository.findById(id).orElse(null);
    }

    @Override
    public TaskTemplate findByName(String name) {
        return taskTemplateRepository.findByTitle(name).orElse(null);
    }

    @Override
    public TaskTemplate createTaskTemplate(TemplateDTO dto, User creator) {

        TaskTemplate newTaskTemplate = new TaskTemplate();
        newTaskTemplate.setTitle(dto.getTitle());
        newTaskTemplate.setDescription(dto.getDescription());
        newTaskTemplate.setPriority(dto.getPriority());
        newTaskTemplate.setFlashTime(dto.getFlashTime());
        newTaskTemplate.setTaskFrequency(dto.getTaskFrequency());
        newTaskTemplate.setCreator(creator);
        newTaskTemplate.setCreatedAt(LocalDateTime.now());
        newTaskTemplate.setActive(false);

        return taskTemplateRepository.save(newTaskTemplate);
    }

    @Override
    public void deleteTaskTemplateById(Long id) {

        TaskTemplate taskTemplate = taskTemplateRepository.findById(id).orElse(null);
        if (taskTemplate != null) {
            taskTemplateRepository.delete(taskTemplate);
        }
    }

    @Override
    public TaskTemplate activate(Long id) {

        TaskTemplate taskTemplate = taskTemplateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TaskTemplate not found"));

        if (!taskTemplate.isActive()) {
            taskTemplate.setActive(true);
            taskTemplateRepository.save(taskTemplate);
        }

        return taskTemplate;
    }

    @Override
    public TaskTemplate deactivate(Long id) {
        TaskTemplate taskTemplate = taskTemplateRepository.findById(id).orElse(null);
        if (taskTemplate != null) {
            taskTemplate.setActive(false);
            taskTemplateRepository.save(taskTemplate);
        }
        return null;
    }

    @Override
    public Task createTaskFromTemplate(Long id, User creator) {

        TaskTemplate taskTemplate = taskTemplateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TaskTemplate not found"));

        if (!taskTemplate.isActive()) {
            throw new IllegalStateException("TaskTemplate is not active");
        }

        return taskService.createTask(
                taskTemplate.getTitle(),
                taskTemplate.getDescription(),
                taskTemplate.getPriority(),
                creator,
                null,
                taskTemplate.getId()
        );
    }


}
