package com.notes.notes.service.taskModuleServices.impl;

import com.notes.notes.dto.taskManagementDTO.CreateTemplateRequestDTO;
import com.notes.notes.entity.authEntities.User;
import com.notes.notes.entity.taskModuleEntities.TaskTemplate;
import com.notes.notes.repository.taskModuleRepositories.TaskTemplateRepository;
import com.notes.notes.service.taskModuleServices.TaskTemplateService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TaskTemplateServiceImpl implements TaskTemplateService {

    private final TaskTemplateRepository taskTemplateRepository;

    public TaskTemplateServiceImpl(TaskTemplateRepository taskTemplateRepository) {
        this.taskTemplateRepository = taskTemplateRepository;
    }

    @Override
    public void createTemplate(CreateTemplateRequestDTO dto, User creator) {

        TaskTemplate template = new TaskTemplate();

        template.setTemplateName(dto.getTemplateName());
        template.setDescription(dto.getDescription());
        template.setPriority(dto.getPriority());
        template.setDueDate(dto.getDueDate());

        // Recurrence fields — SAVED AS-IS (NO LOGIC)
        template.setRecurrenceFrequency(dto.getRecurrenceFrequency());

        template.setDaysBeforeToFlash(dto.getDaysBeforeToFlash());
        template.setCreator(creator);
        template.setIsActive(true);

        taskTemplateRepository.save(template);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskTemplate> getAllTemplates() {
        return taskTemplateRepository.findAll();
    }

    @Override
    public void activateTemplate(Long templateId) {

        TaskTemplate template = taskTemplateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found"));

        template.setIsActive(true);
    }

    @Override
    public void deactivateTemplate(Long templateId) {

        TaskTemplate template = taskTemplateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found"));

        template.setIsActive(false);
    }

    @Override
    public void deleteTemplate(Long templateId) {

        TaskTemplate template = taskTemplateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found"));

        taskTemplateRepository.delete(template);
    }

    @Override
    public TaskTemplate getTemplateById(Long templateId) {
        return taskTemplateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found"));
    }
}
