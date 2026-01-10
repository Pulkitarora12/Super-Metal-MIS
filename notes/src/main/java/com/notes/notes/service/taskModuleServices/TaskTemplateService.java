package com.notes.notes.service.taskModuleServices;

import com.notes.notes.dto.taskManagementDTO.CreateTemplateRequestDTO;
import com.notes.notes.entity.authEntities.User;
import com.notes.notes.entity.taskModuleEntities.TaskTemplate;

import java.util.List;

public interface TaskTemplateService {

    void createTemplate(CreateTemplateRequestDTO dto, User creator);

    List<TaskTemplate> getAllTemplates();

    void activateTemplate(Long templateId);

    void deactivateTemplate(Long templateId);

    void deleteTemplate(Long templateId);

    TaskTemplate getTemplateById(Long templateId);
}
