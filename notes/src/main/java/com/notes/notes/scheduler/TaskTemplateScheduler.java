package com.notes.notes.scheduler;

import com.notes.notes.entity.taskModuleEntities.TaskTemplate;
import com.notes.notes.repository.taskModuleRepositories.TaskTemplateRepository;
import com.notes.notes.service.taskModuleServices.TaskService;
import com.notes.notes.service.taskModuleServices.TaskTemplateService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class TaskTemplateScheduler {

    private final TaskTemplateRepository taskTemplateRepository;
    private final TaskService taskService;
    private final TaskTemplateService taskTemplateService;

    private static final Logger log = LoggerFactory.getLogger(TaskTemplateScheduler.class);

    public TaskTemplateScheduler(TaskTemplateRepository taskTemplateRepository,
                                 TaskService taskService,
                                 TaskTemplateService taskTemplateService) {
        this.taskTemplateRepository = taskTemplateRepository;
        this.taskService = taskService;
        this.taskTemplateService = taskTemplateService;
    }

    @Transactional
    @Scheduled(cron = "0 * * * * *")
    public void runDailyTemplates() {

        LocalDate today = LocalDate.now();

        List<TaskTemplate> dueTemplates =
                taskTemplateRepository
                        .findByIsActiveTrueAndNextRunDate(today);

        if (dueTemplates.isEmpty()) {
            log.info("Scheduler: No templates due on {}", today);
            return;
        }

        log.info("Scheduler: {} template(s) due on {}", dueTemplates.size(), today);

        for (TaskTemplate template : dueTemplates) {

            try {
                taskService.createTaskFromTemplate(
                        template,
                        template.getCreator()
                );

                taskTemplateService.calculateAndSetNextRunDate(template);
                taskTemplateRepository.save(template);

                log.info("Scheduler: Task created for template [{}]", template.getTitle());

            } catch (Exception e) {
                log.error("Scheduler: Failed for template [{}]", template.getTitle(), e);
            }
        }
    }

}
