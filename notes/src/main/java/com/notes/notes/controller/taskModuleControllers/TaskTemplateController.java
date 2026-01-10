package com.notes.notes.controller.taskModuleControllers;

import com.notes.notes.dto.taskManagementDTO.CreateTemplateRequestDTO;
import com.notes.notes.entity.authEntities.User;
import com.notes.notes.entity.taskModuleEntities.TaskTemplate;
import com.notes.notes.service.taskModuleServices.TaskTemplateService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/task-templates")
public class TaskTemplateController {

    private final TaskTemplateService taskTemplateService;

    public TaskTemplateController(TaskTemplateService taskTemplateService) {
        this.taskTemplateService = taskTemplateService;
    }

    /* ================= CREATE TEMPLATE PAGE ================= */

    @GetMapping("/new")
    public String createTemplatePage(Model model) {
        model.addAttribute("template", new CreateTemplateRequestDTO());
        return "tasks/createTemplate";
    }

    /* ================= CREATE TEMPLATE ================= */

    @PostMapping
    public String createTemplate(@Valid @ModelAttribute("template") CreateTemplateRequestDTO dto,
                                 @ModelAttribute("loggedInUser") User loggedInUser,
                                 RedirectAttributes redirectAttributes) {

        taskTemplateService.createTemplate(dto, loggedInUser);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Task template created successfully."
        );

        return "redirect:/task-templates";
    }

    /* ================= VIEW ALL TEMPLATES ================= */

    @GetMapping
    public String listTemplates(Model model) {

        List<TaskTemplate> templates = taskTemplateService.getAllTemplates();
        model.addAttribute("templates", templates);

        return "tasks/templateList";
    }

    /* ================= ACTIVATE TEMPLATE ================= */

    @PostMapping("/{templateId}/activate")
    public String activateTemplate(@PathVariable Long templateId,
                                   RedirectAttributes redirectAttributes) {

        taskTemplateService.activateTemplate(templateId);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Template activated successfully."
        );

        return "redirect:/task-templates";
    }

    /* ================= DEACTIVATE TEMPLATE ================= */

    @PostMapping("/{templateId}/deactivate")
    public String deactivateTemplate(@PathVariable Long templateId,
                                     RedirectAttributes redirectAttributes) {

        taskTemplateService.deactivateTemplate(templateId);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Template deactivated successfully."
        );

        return "redirect:/task-templates";
    }

    /* ================= DELETE TEMPLATE ================= */

    @PostMapping("/{templateId}/delete")
    public String deleteTemplate(@PathVariable Long templateId,
                                 RedirectAttributes redirectAttributes) {

        taskTemplateService.deleteTemplate(templateId);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Template deleted successfully."
        );

        return "redirect:/task-templates";
    }

    @GetMapping("/{templateId}/create-task")
    public String createTaskFromTemplate(@PathVariable Long templateId,
                                         Model model) {

        TaskTemplate template = taskTemplateService.getTemplateById(templateId);

        // Prefill values for task creation page
        model.addAttribute("prefillTitle", template.getTemplateName());
        model.addAttribute("prefillDescription", template.getDescription());
        model.addAttribute("prefillPriority", template.getPriority());
        model.addAttribute("sourceTemplateId", template.getTemplateId());
        model.addAttribute("prefillDueDate", template.getDueDate());

        // Reuse existing task creation page
        return "tasks/create";
    }

    @GetMapping("/{templateId}")
    public String viewTemplate(@PathVariable Long templateId,
                               Model model) {

        TaskTemplate template = taskTemplateService.getTemplateById(templateId);
        model.addAttribute("template", template);

        return "tasks/templateView";
    }
}
