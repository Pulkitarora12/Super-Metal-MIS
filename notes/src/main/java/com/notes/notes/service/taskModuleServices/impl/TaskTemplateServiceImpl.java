package com.notes.notes.service.taskModuleServices.impl;

import com.notes.notes.dto.taskManagementDTO.TemplateDTO;
import com.notes.notes.entity.authEntities.User;
import com.notes.notes.entity.taskModuleEntities.Task;
import com.notes.notes.entity.taskModuleEntities.TaskTemplate;
import com.notes.notes.repository.authRepo.UserRepository;
import com.notes.notes.repository.taskModuleRepositories.TaskRepository;
import com.notes.notes.repository.taskModuleRepositories.TaskTemplateRepository;
import com.notes.notes.service.taskModuleServices.TaskTemplateService;
import com.notes.notes.validator.TaskTemplateValidator;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;

@Service
public class TaskTemplateServiceImpl implements TaskTemplateService {

    private final TaskTemplateRepository taskTemplateRepository;
    private final UserRepository userRepository;
    private final TaskTemplateValidator taskTemplateValidator;
    private final TaskRepository taskRepository;

    public TaskTemplateServiceImpl(TaskTemplateRepository taskTemplateRepository, UserRepository userRepository, TaskTemplateValidator taskTemplateValidator, TaskRepository taskRepository) {
        this.taskTemplateRepository = taskTemplateRepository;
        this.userRepository = userRepository;
        this.taskTemplateValidator = taskTemplateValidator;
        this.taskRepository = taskRepository;
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

        taskTemplateValidator.validateRecurrenceFields(dto);

        TaskTemplate newTaskTemplate = new TaskTemplate();
        newTaskTemplate.setTitle(dto.getTitle());
        newTaskTemplate.setDescription(dto.getDescription());
        newTaskTemplate.setPriority(dto.getPriority());
        newTaskTemplate.setFlashTime(dto.getFlashTime());
        newTaskTemplate.setTaskFrequency(dto.getTaskFrequency());
        newTaskTemplate.setCreator(creator);
        newTaskTemplate.setActive(false); // Initially inactive

        newTaskTemplate.setRecurrenceDay(dto.getRecurrenceDay());
        newTaskTemplate.setRecurrenceMonth(dto.getRecurrenceMonth());
        newTaskTemplate.setFinalDate(dto.getFinalDate());
        newTaskTemplate.setStartDate(dto.getStartDate());

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

        // ✅ SAVE TEMPLATE FIRST
        TaskTemplate savedTemplate = taskTemplateRepository.save(newTaskTemplate);

        // ✅ CALCULATE NEXT RUN DATE FOR FUTURE
        calculateAndSetNextRunDate(savedTemplate);

        return taskTemplateRepository.save(savedTemplate);
    }

    @Override
    public void deleteTaskTemplateById(Long id) {

        TaskTemplate taskTemplate = taskTemplateRepository.findById(id).orElse(null);
        if (taskTemplate != null) {
            List<Task> tasks = taskRepository.findBySourceTemplate(taskTemplate);
            for (Task task : tasks) {
                task.setSourceTemplate(null);
            }
            taskRepository.saveAll(tasks);
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
    public TaskTemplate activateTask(Long id) {

        TaskTemplate template = taskTemplateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TaskTemplate not found"));

        // 🚫 Prevent duplicate task creation
        if (template.isActive()) {
            return template;
        }

        template.setActive(true);
        taskTemplateRepository.save(template);

        return template;
    }

    @Override
    public void calculateAndSetNextRunDate(TaskTemplate template) {

        // =====================================================
        // STEP 1: Determine previous DUE DATE
        // =====================================================
        LocalDate previousDueDate;

        if (template.getNextRunDate() == null) {
            // First run → due date comes from startDate / recurrence rules
            previousDueDate = template.getStartDate();
        } else {
            // nextRunDate = creationDate = dueDate - flashTime
            previousDueDate =
                    template.getNextRunDate().plusDays(template.getFlashTime());
        }

        LocalDate nextDueDate;

        // =====================================================
        // STEP 2: Move DUE DATE based on frequency
        // =====================================================
        switch (template.getTaskFrequency()) {

            case DAILY:
                if (template.getNextRunDate() == null) {
                    // first task → due on start date
                    nextDueDate = template.getStartDate();
                } else {
                    nextDueDate = previousDueDate.plusDays(1);
                }
                break;

            case WEEKLY:
                if (template.getNextRunDate() == null) {
                    nextDueDate = template.getStartDate();
                } else {
                    nextDueDate = previousDueDate.plusWeeks(1);
                }
                break;

            case MONTHLY: {
                int day = template.getRecurrenceDay();
                LocalDate candidate = previousDueDate.plusMonths(1);
                nextDueDate = candidate.withDayOfMonth(
                        Math.min(day, candidate.lengthOfMonth())
                );
                break;
            }

            case QUARTERLY: {
                int day = template.getRecurrenceDay();
                LocalDate candidate = previousDueDate.plusMonths(3);
                nextDueDate = candidate.withDayOfMonth(
                        Math.min(day, candidate.lengthOfMonth())
                );
                break;
            }

            case YEARLY: {
                int day = template.getRecurrenceDay();
                int month = template.getRecurrenceMonth();
                int year = previousDueDate.getYear() + 1;

                nextDueDate = LocalDate.of(
                        year,
                        month,
                        Math.min(
                                day,
                                Month.of(month).length(Year.isLeap(year))
                        )
                );
                break;
            }

            default:
                throw new IllegalArgumentException("Invalid task frequency");
        }

        // =====================================================
        // STEP 3: FINAL DATE CHECK (create once, then stop)
        // =====================================================
        if (nextDueDate.isAfter(template.getFinalDate())) {
            template.setActive(false);
            template.setNextRunDate(null);
            return;
        }

        // =====================================================
        // STEP 4: Derive NEXT CREATION DATE
        // =====================================================
        LocalDate nextCreationDate =
                nextDueDate.minusDays(template.getFlashTime());

        template.setNextRunDate(nextCreationDate);
    }
}
