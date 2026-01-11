package com.notes.notes.repository.taskModuleRepositories;

import com.notes.notes.entity.taskModuleEntities.TaskTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface TaskTemplateRepository extends JpaRepository<TaskTemplate, Long> {
    Optional<TaskTemplate> findByTitle(String name);
}
