package com.notes.notes.repository.taskModuleRepositories;

import com.notes.notes.entity.taskModuleEntities.TaskTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface TaskTemplateRepository extends JpaRepository<TaskTemplate, Long> {

    Optional<TaskTemplate> findByTitle(String name);

    List<TaskTemplate> findByIsActiveTrueAndNextRunDate(LocalDate date);
// This will fetch all active templates whose nextRunDate matches today

    List<TaskTemplate> findByIsActiveTrueAndNextRunDateLessThan(LocalDate date);
}
