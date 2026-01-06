package com.notes.notes.service.taskModuleServices.impl;

import com.notes.notes.entity.authEntities.AppRole;
import com.notes.notes.entity.authEntities.User;
import com.notes.notes.entity.taskModuleEntities.Task;
import com.notes.notes.entity.taskModuleEntities.TaskStatusHistory;
import com.notes.notes.repository.authRepo.UserRepository;
import com.notes.notes.repository.taskModuleRepositories.TaskAssignmentRepository;
import com.notes.notes.repository.taskModuleRepositories.TaskCommentRepository;
import com.notes.notes.repository.taskModuleRepositories.TaskRepository;
import com.notes.notes.repository.taskModuleRepositories.TaskStatusHistoryRepository;
import com.notes.notes.service.taskModuleServices.TaskAssignmentService;
import com.notes.notes.service.taskModuleServices.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final TaskStatusHistoryRepository taskStatusHistoryRepository;
    private final UserRepository userRepository;
    private final TaskAssignmentService taskAssignmentService;

    public TaskServiceImpl(TaskRepository taskRepository,
                           TaskAssignmentRepository taskAssignmentRepository,
                           TaskCommentRepository taskCommentRepository,
                           TaskStatusHistoryRepository taskStatusHistoryRepository, UserRepository userRepository, TaskAssignmentService taskAssignmentService) {
        this.taskRepository = taskRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.taskCommentRepository = taskCommentRepository;
        this.taskStatusHistoryRepository = taskStatusHistoryRepository;
        this.userRepository = userRepository;
        this.taskAssignmentService = taskAssignmentService;
    }

    @Override
    @Transactional
    public Task createTask(String title,
                           String description,
                           Task.TaskPriority priority,
                           User creator, LocalDate dueDate) {

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority);
        task.setDueDate(dueDate);
        task.setCreator(creator);

        // Generate task number (can be improved later)
        task.setTaskNo("TSK-" + UUID.randomUUID().toString().substring(0, 8));

        Task savedTask = taskRepository.save(task);

        // Initial status history
        TaskStatusHistory history = new TaskStatusHistory();
        history.setTask(savedTask);
        history.setChangedBy(creator);
        history.setOldStatus(null);
        history.setNewStatus(Task.TaskStatus.CREATED);

        List<User> admins =
                userRepository.findByRole_RoleName(AppRole.ROLE_ADMIN);

        for (User admin : admins) {
            boolean alreadyAssigned =
                    taskAssignmentService.isUserAssignedToTask(task, admin);

            if (!alreadyAssigned) {
                taskAssignmentService.addSupportingAssignee(task, admin);
            }
        }

        taskStatusHistoryRepository.save(history);

        return savedTask;
    }

    @Override
    @Transactional
    public Task updateTaskStatus(Long taskId,
                                 Task.TaskStatus newStatus,
                                 User changedBy) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        Task.TaskStatus oldStatus = task.getStatus();

        if (oldStatus == newStatus) {
            return task;
        }

        task.setStatus(newStatus);
        Task updatedTask = taskRepository.save(task);

        // ---- STATUS HISTORY ----
        TaskStatusHistory history = new TaskStatusHistory();
        history.setTask(updatedTask);
        history.setChangedBy(changedBy);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        taskStatusHistoryRepository.save(history);

        return updatedTask;
    }

    @Override
    @Transactional
    public void deleteTask(Task task) {

        // 1️⃣ Delete dependent entities first (safe & explicit)
        taskAssignmentRepository.deleteByTask(task);
        taskCommentRepository.deleteByTask(task);
        taskStatusHistoryRepository.deleteByTask(task);

        // 2️⃣ Delete task
        taskRepository.delete(task);
    }

//    @Override
//    public List<Task> getCreatedTasksWithFilter(
//            User creator,
//            Task.TaskPriority priority,
//            Task.TaskStatus status
//    ) {
//
//        // 3️⃣ Both selected
//        if (priority != null && status != null) {
//            return taskRepository
//                    .findByCreatorAndPriorityAndStatus(creator, priority, status);
//        }
//
//        // 1️⃣ Only priority selected
//        if (priority != null) {
//            return taskRepository
//                    .findByCreatorAndPriority(creator, priority);
//        }
//
//        // 2️⃣ Only status selected
//        if (status != null) {
//            return taskRepository
//                    .findByCreatorAndStatus(creator, status);
//        }
//
//        // No filter
//        return taskRepository.findByCreator(creator);
//    }

//    @Override
//    public List<Task> filterTasks(
//            List<Task> tasks,
//            Task.TaskPriority priority,
//            Task.TaskStatus status
//    ) {
//        return tasks.stream()
//                .filter(task ->
//                        (priority == null || task.getPriority() == priority) &&
//                                (status == null || task.getStatus() == status)
//                )
//                .toList();
//    }

//    @Override
//    public List<Task> searchCreatedTasks(
//            User creator,
//            String search,
//            Task.TaskPriority priority,
//            Task.TaskStatus status
//    ) {
//
//        List<Task> tasks = taskRepository
//                .findByCreatorAndTaskNoContainingIgnoreCaseOrCreatorAndTitleContainingIgnoreCase(
//                        creator, search,
//                        creator, search
//                );
//
//        // Reuse existing filter logic
//        return filterTasks(tasks, priority, status);
//    }

    @Override
    public List<Task> searchAndFilterTasks(
            List<Task> tasks,
            String search,
            Task.TaskPriority priority,
            Task.TaskStatus status,
            String progress
    ) {

        LocalDate today = LocalDate.now();

        return tasks.stream()

                // 🔍 Search: Task No OR Title
                .filter(task ->
                        search == null || search.isBlank()
                                || task.getTaskNo().toLowerCase().contains(search.toLowerCase())
                                || task.getTitle().toLowerCase().contains(search.toLowerCase())
                )

                // 🎯 Priority filter
                .filter(task ->
                        priority == null || task.getPriority() == priority
                )

                // 📌 Status filter
                .filter(task ->
                        status == null || task.getStatus() == status
                )

                // ⏳ Progress filter: PENDING / COMPLETED / OVERDUE
                .filter(task -> {

                    if (progress == null || progress.isBlank()) {
                        return true; // no progress filter
                    }

                    if ("COMPLETED".equalsIgnoreCase(progress)) {
                        return task.getStatus() == Task.TaskStatus.CLOSED;
                    }

                    if ("OVERDUE".equalsIgnoreCase(progress)) {
                        return task.getStatus() != Task.TaskStatus.CLOSED
                                && task.getDueDate() != null
                                && task.getDueDate().isBefore(today);
                    }

                    if ("PENDING".equalsIgnoreCase(progress)) {
                        return task.getStatus() != Task.TaskStatus.CLOSED
                                && (task.getDueDate() == null
                                || !task.getDueDate().isBefore(today));
                    }

                    return true; // unknown value → ignore safely
                })

                .toList();
    }

    @Override
    public Optional<Task> getTaskById(Long taskId) {
        return taskRepository.findById(taskId);
    }

    @Override
    public Optional<Task> getTaskByTaskNo(String taskNo) {
        return taskRepository.findByTaskNo(taskNo);
    }

    @Override
    public List<Task> getTasksCreatedByUser(User creator) {
        return taskRepository.findByCreator(creator);
    }

    @Override
    public List<Task> getTasksByStatus(Task.TaskStatus status) {
        return taskRepository.findByStatus(status);
    }
}