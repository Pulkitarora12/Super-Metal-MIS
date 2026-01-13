package com.notes.notes.service.taskModuleServices.impl;

import com.notes.notes.dto.taskManagementDTO.TemplateDTO;
import com.notes.notes.entity.authEntities.User;
import com.notes.notes.entity.taskModuleEntities.Task;
import com.notes.notes.entity.taskModuleEntities.TaskTemplate;
import com.notes.notes.repository.authRepo.UserRepository;
import com.notes.notes.repository.taskModuleRepositories.TaskRepository;
import com.notes.notes.repository.taskModuleRepositories.TaskTemplateRepository;
import com.notes.notes.service.taskModuleServices.TaskService;
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

        LocalDate today = LocalDate.now();

        LocalDate startDate = template.getStartDate();

        LocalDate actualTaskDate;

        if (startDate.isAfter(today)) {
            actualTaskDate = startDate;
        }

        else {
            LocalDate baseDate = today;

            switch (template.getTaskFrequency()) {

                case DAILY:
                    actualTaskDate = baseDate.plusDays(1);
                    break;

                case WEEKLY:
                    actualTaskDate = baseDate.with(
                            DayOfWeek.of(template.getRecurrenceDay())
                    );
                    if (!actualTaskDate.isAfter(baseDate)) {
                        actualTaskDate = actualTaskDate.plusWeeks(1);
                    }
                    break;

                case MONTHLY: {
                    int day = template.getRecurrenceDay();

                    LocalDate candidate = baseDate.withDayOfMonth(
                            Math.min(day, baseDate.lengthOfMonth())
                    );

                    if (!candidate.isAfter(baseDate)) {
                        LocalDate nextMonth = baseDate.plusMonths(1);
                        candidate = nextMonth.withDayOfMonth(
                                Math.min(day, nextMonth.lengthOfMonth())
                        );
                    }

                    actualTaskDate = candidate;
                    break;
                }

                case QUARTERLY: {
                    int day = template.getRecurrenceDay();

                    LocalDate candidate = baseDate.withDayOfMonth(
                            Math.min(day, baseDate.lengthOfMonth())
                    );

                    if (!candidate.isAfter(baseDate)) {
                        LocalDate nextQuarter = baseDate.plusMonths(3);
                        candidate = nextQuarter.withDayOfMonth(
                                Math.min(day, nextQuarter.lengthOfMonth())
                        );
                    }

                    actualTaskDate = candidate;
                    break;
                }

                case YEARLY: {
                    int month = template.getRecurrenceMonth();
                    int day = template.getRecurrenceDay();
                    int year = baseDate.getYear();

                    LocalDate candidate = LocalDate.of(
                            year,
                            month,
                            Math.min(
                                    day,
                                    Month.of(month).length(Year.isLeap(year))
                            )
                    );

                    if (!candidate.isAfter(baseDate)) {
                        int nextYear = year + 1;
                        candidate = LocalDate.of(
                                nextYear,
                                month,
                                Math.min(
                                        day,
                                        Month.of(month).length(Year.isLeap(nextYear))
                                )
                        );
                    }

                    actualTaskDate = candidate;
                    break;
                }

                default:
                    throw new IllegalArgumentException("Invalid task frequency");
            }
        }

    /* =====================================================
       FINAL DATE SAFETY CHECK
       ===================================================== */
        if (actualTaskDate.isAfter(template.getFinalDate())) {
            template.setActive(false);
            template.setNextRunDate(null);
            return;
        }

    /* =====================================================
       NEXT RUN DATE (CREATION DATE)
       ===================================================== */
        LocalDate nextRunDate =
                actualTaskDate.minusDays(template.getFlashTime());

        template.setNextRunDate(nextRunDate);
    }


}
