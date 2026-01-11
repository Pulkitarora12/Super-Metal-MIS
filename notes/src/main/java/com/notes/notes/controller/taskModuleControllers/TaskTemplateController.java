    package com.notes.notes.controller.taskModuleControllers;

    import com.notes.notes.dto.taskManagementDTO.TemplateDTO;
    import com.notes.notes.entity.authEntities.User;
    import com.notes.notes.entity.taskModuleEntities.Task;
    import com.notes.notes.entity.taskModuleEntities.TaskTemplate;
    import com.notes.notes.service.taskModuleServices.TaskService;
    import com.notes.notes.service.taskModuleServices.TaskTemplateService;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.*;

    import java.util.List;

    @Controller
    @RequestMapping("/template")
    public class TaskTemplateController {

        private final TaskTemplateService taskTemplateService;
        private final TaskService taskService;

        public TaskTemplateController(TaskTemplateService taskTemplateService, TaskService taskService) {
            this.taskTemplateService = taskTemplateService;
            this.taskService = taskService;
        }

        /* ===================== LIST ALL TEMPLATES ===================== */

        @GetMapping
        public String getAllTemplates(Model model) {

            List<TaskTemplate> templates = taskTemplateService.findAll();
            model.addAttribute("templates", templates);

            return "tasks/templateList";
        }

        /* ===================== SHOW CREATE FORM ===================== */

        @GetMapping("/new")
        public String showCreateTemplateForm(Model model) {

            model.addAttribute("templateDTO", new TemplateDTO());
            return "tasks/createTemplate";
        }

        /* ===================== CREATE TEMPLATE ===================== */

        @PostMapping("/new")
        public String createTemplate(
                @ModelAttribute TemplateDTO templateDTO,
                @ModelAttribute("loggedInUser") User loggedInUser
        ) {

            taskTemplateService.createTaskTemplate(templateDTO, loggedInUser);
            return "redirect:/template";
        }

        /* ===================== ACTIVATE TEMPLATE ===================== */

        @PostMapping("/{id}/activate")
        public String activateTemplate(@PathVariable Long id,
                                    @ModelAttribute("loggedInUser") User loggedInUser) {

            taskTemplateService.activate(id);
            return "redirect:/tasks/new?templateId=" + id;
        }

        /* ===================== DEACTIVATE TEMPLATE ===================== */

        @PostMapping("/{id}/deactivate")
        public String deactivateTemplate(@PathVariable Long id) {

            taskTemplateService.deactivate(id);
            return "redirect:/template";
        }

        /* ===================== DELETE TEMPLATE ===================== */

        @PostMapping("/{id}/delete")
        public String deleteTemplate(@PathVariable Long id) {

            taskTemplateService.deleteTaskTemplateById(id);
            return "redirect:/template";
        }

        @GetMapping("/{id}/view")
        public String viewTemplate(@PathVariable Long id, Model model) {
            TaskTemplate taskTemplate = taskTemplateService.findById(id);
            model.addAttribute("taskTemplate", taskTemplate);
            return "tasks/viewTemplate";
        }

        @GetMapping("/{id}/tasks")
        public String viewTasksCreatedFromTemplate(
                @PathVariable Long id,
                Model model,
                @ModelAttribute("loggedInUser") User loggedInUser
        ) {

            // 🔒 ADMIN CHECK
            if (!loggedInUser.isAdmin()) {
                throw new SecurityException("Access denied");
            }

            TaskTemplate template = taskTemplateService.findById(id);

            List<Task> tasks = taskService.getTasksByTemplate(template);

            model.addAttribute("tasks", tasks);
            model.addAttribute("templateTitle", template.getTitle());

            return "tasks/templateTasks";
        }
    }
