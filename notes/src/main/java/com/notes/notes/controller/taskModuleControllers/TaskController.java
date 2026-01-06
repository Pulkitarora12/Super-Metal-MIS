package com.notes.notes.controller.taskModuleControllers;

import com.notes.notes.entity.authEntities.User;
import com.notes.notes.entity.taskModuleEntities.Task;
import com.notes.notes.entity.taskModuleEntities.TaskAssignment;
import com.notes.notes.service.authServices.UserService;
import com.notes.notes.service.taskModuleServices.*;
import com.notes.notes.util.TimeUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskAssignmentService taskAssignmentService;
    private final TaskCommentService taskCommentService;
    private final TaskStatusHistoryService taskStatusHistoryService;
    private final UserService userService;

    public TaskController(TaskService taskService,
                          TaskAssignmentService taskAssignmentService,
                          TaskCommentService taskCommentService,
                          TaskStatusHistoryService taskStatusHistoryService,
                          UserService userService) {
        this.taskService = taskService;
        this.taskAssignmentService = taskAssignmentService;
        this.taskCommentService = taskCommentService;
        this.taskStatusHistoryService = taskStatusHistoryService;
        this.userService = userService;
    }

    @GetMapping
    public String listTasks(Model model,
                            @RequestParam(required = false) String search,
                            @RequestParam(required = false) Task.TaskPriority priority,
                            @RequestParam(required = false) Task.TaskStatus status,
                            @RequestParam(required = false) String progress,
                            @ModelAttribute("loggedInUser") User loggedInUser) {

        // ================= CREATED TASKS =================
        List<Task> createdTasks = taskService.searchAndFilterTasks(
                taskService.getTasksCreatedByUser(loggedInUser),
                search,
                priority,
                status,
                progress
        );

        model.addAttribute("createdTasks", createdTasks);

        // ================= ASSIGNED TASKS =================
        List<Task> assignedTasks = taskAssignmentService
                .getAssignmentsByUser(loggedInUser)
                .stream()
                .map(TaskAssignment::getTask)
                .toList();

        assignedTasks = taskService.searchAndFilterTasks(
                assignedTasks,
                search,
                priority,
                status,
                progress
        );

        model.addAttribute("assignedTasks", assignedTasks);

        // ================= UI STATE =================
        model.addAttribute("search", search);
        model.addAttribute("selectedPriority", priority);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedProgress", progress);

        return "tasks/list";
    }


    @GetMapping("/new")
    public String createTaskPage() {
        return "tasks/create";
    }

    @PostMapping
    public String createTask(@RequestParam String title,
                             @RequestParam String description,
                             @RequestParam Task.TaskPriority priority,
                             @RequestParam LocalDate dueDate,
                             @ModelAttribute("loggedInUser") User loggedInUser) {
        System.out.println("CREATE TASK HIT");
        System.out.println(title + " | " + description + " | " + priority);
        Task task = taskService.createTask(title, description, priority, loggedInUser, dueDate);

        return "redirect:/tasks/" + task.getTaskId() + "/assign";
    }

    @GetMapping("/{taskId}")
    public String taskDetails(@PathVariable Long taskId, Model model,
                        @ModelAttribute("loggedInUser") User loggedInUser,
                              RedirectAttributes redirectAttributes) {

        Task task = taskService.getTaskById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (!canAccessTask(task, loggedInUser)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "You are not authorized to access this task."
            );
            return "redirect:/tasks";
        }


        String remainingTime = TimeUtil.getRemainingTime(task.getDueDate());
        model.addAttribute("remainingTime", remainingTime);

        model.addAttribute("task", task);
        model.addAttribute("assignees",
                taskAssignmentService.getAssigneesByTask(task));
        model.addAttribute("comments",
                taskCommentService.getCommentsByTask(task));
        model.addAttribute("history",
                taskStatusHistoryService.getHistoryByTask(task));

        return "tasks/details";
    }

    @PostMapping("/{taskId}/status")
    public String updateStatus(@PathVariable Long taskId,
                               @RequestParam Task.TaskStatus status,
                               @ModelAttribute("loggedInUser") User loggedInUser,
                               RedirectAttributes redirectAttributes) {

        Task task = taskService.getTaskById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (task.getStatus() == Task.TaskStatus.CLOSED) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "This task is closed and can no longer be modified."
            );
            return "redirect:/tasks/" + taskId;
        }

        if (status == Task.TaskStatus.CLOSED &&
                !task.getCreator().getUserId().equals(loggedInUser.getUserId())) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Only the task creator can close this task."
            );

            return "redirect:/tasks/" + taskId;
        }

        taskService.updateTaskStatus(taskId, status, loggedInUser);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Task status updated successfully."
        );

        return "redirect:/tasks/" + taskId;
    }

    @GetMapping("/{taskId}/assign")
    public String assignPage(@PathVariable Long taskId, Model model) {

        Task task = taskService.getTaskById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        model.addAttribute("task", task);
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("assignees",
                taskAssignmentService.getAssigneesByTask(task));

        return "tasks/assign";
    }

    @PostMapping("/{taskId}/assign")
    public String assignUser(@PathVariable Long taskId,
                             @RequestParam(required = false) Long userId,
                             @RequestParam(required = false) List<Long> userIds,
                             @RequestParam String role,
                             RedirectAttributes redirectAttributes) {

        Task task = taskService.getTaskById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (task.getStatus() == Task.TaskStatus.CLOSED) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Assignees cannot be modified after task closure."
            );
            return "redirect:/tasks/" + taskId + "/assign";
        }

        if ("MAIN".equalsIgnoreCase(role)) {
            if (userId != null) {
                User user = userService.getUserById(userId);
                taskAssignmentService.assignMainAssignee(task, user);
            }
        } else {
            if (userIds != null) {
                for (Long uid : userIds) {
                    try {
                        User user = userService.getUserById(uid);
                        taskAssignmentService.addSupportingAssignee(task, user);
                    } catch (IllegalStateException ignored) {}
                }
            }
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Assignees updated successfully."
        );

        return "redirect:/tasks/" + taskId + "/assign";
    }

    @PostMapping("/{taskId}/comment")
    public String addComment(@PathVariable Long taskId,
                             @RequestParam String content,
                             @ModelAttribute("loggedInUser") User loggedInUser,
                             RedirectAttributes redirectAttributes) {

        Task task = taskService.getTaskById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (task.getStatus() == Task.TaskStatus.CLOSED) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Comments are disabled for closed tasks."
            );
            return "redirect:/tasks/" + taskId;
        }

        taskCommentService.addComment(task, loggedInUser, content, null);
        return "redirect:/tasks/" + taskId;
    }

    @PostMapping("/{taskId}/delete")
    public String deleteTask(@PathVariable Long taskId,
                             @ModelAttribute("loggedInUser") User loggedInUser,
                             RedirectAttributes redirectAttributes) {

        Task task = taskService.getTaskById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        // Rule 1: Only creator can delete
        if (!task.getCreator().getUserId().equals(loggedInUser.getUserId())) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Only the task creator can delete this task."
            );
            return "redirect:/tasks/" + taskId;
        }

        // Rule 2: Closed tasks cannot be deleted
        if (task.getStatus() == Task.TaskStatus.CLOSED) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Closed tasks cannot be deleted."
            );
            return "redirect:/tasks/" + taskId;
        }

        taskService.deleteTask(task);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Task deleted successfully."
        );

        return "redirect:/tasks";
    }

    private boolean canAccessTask(Task task, User user) {
        // Creator can always access
        if (task.getCreator().getUserId().equals(user.getUserId())) {
            return true;
        }

        // Assigned users can access
        return taskAssignmentService.isUserAssignedToTask(task, user);
    }
}
