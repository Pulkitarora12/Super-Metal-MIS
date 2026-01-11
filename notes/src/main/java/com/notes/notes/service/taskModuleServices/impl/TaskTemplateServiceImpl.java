package com.notes.notes.service.taskModuleServices.impl;

import com.notes.notes.dto.taskManagementDTO.TemplateDTO;
import com.notes.notes.entity.authEntities.User;
import com.notes.notes.entity.taskModuleEntities.Task;
import com.notes.notes.entity.taskModuleEntities.TaskTemplate;
import com.notes.notes.repository.authRepo.UserRepository;
import com.notes.notes.repository.taskModuleRepositories.TaskTemplateRepository;
import com.notes.notes.service.taskModuleServices.TaskService;
import com.notes.notes.service.taskModuleServices.TaskTemplateService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskTemplateServiceImpl implements TaskTemplateService {

    private final TaskTemplateRepository taskTemplateRepository;
    private final TaskService taskService;
    private final UserRepository userRepository;

    public TaskTemplateServiceImpl(TaskTemplateRepository taskTemplateRepository, TaskService taskService, UserRepository userRepository) {
        this.taskTemplateRepository = taskTemplateRepository;
        this.taskService = taskService;
        this.userRepository = userRepository;
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

        // 🔒 VALIDATION: main assignee is mandatory
        if (dto.getMainAssigneeId() == null) {
            throw new IllegalArgumentException("Main assignee is required");
        }

        TaskTemplate newTaskTemplate = new TaskTemplate();
        newTaskTemplate.setTitle(dto.getTitle());
        newTaskTemplate.setDescription(dto.getDescription());
        newTaskTemplate.setPriority(dto.getPriority());
        newTaskTemplate.setFlashTime(dto.getFlashTime());
        newTaskTemplate.setTaskFrequency(dto.getTaskFrequency());
        newTaskTemplate.setCreator(creator);
        newTaskTemplate.setActive(false);

        /* ================= MAIN ASSIGNEE ================= */
        User mainAssignee = userRepository.findById(dto.getMainAssigneeId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Main assignee not found"));

        newTaskTemplate.setMainAssignee(mainAssignee);

        /* ============== SUPPORTING ASSIGNEES ============== */
        if (dto.getSupportingAssigneeIds() != null &&
                !dto.getSupportingAssigneeIds().isEmpty()) {

            List<User> supportingAssignees =
                    userRepository.findAllById(dto.getSupportingAssigneeIds());

            // Optional safety: remove main assignee if selected twice
            supportingAssignees.removeIf(
                    u -> u.getUserId().equals(mainAssignee.getUserId())
            );

            newTaskTemplate.setAssignees(supportingAssignees);
        }

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
    public TaskTemplate deactivate(Long id) {
        TaskTemplate taskTemplate = taskTemplateRepository.findById(id).orElse(null);
        if (taskTemplate != null) {
            taskTemplate.setActive(false);
            taskTemplateRepository.save(taskTemplate);
        }
        return null;
    }

    @Transactional
    @Override
    public TaskTemplate activateAndCreateTask(Long id, User creator) {

        TaskTemplate template = taskTemplateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TaskTemplate not found"));

        // 🚫 Prevent duplicate task creation
        if (template.isActive()) {
            return template;
        }

        template.setActive(true);
        taskTemplateRepository.save(template);

        // 🔥 AUTO CREATE TASK
        taskService.createTaskFromTemplate(template, creator);

        return template;
    }


}
