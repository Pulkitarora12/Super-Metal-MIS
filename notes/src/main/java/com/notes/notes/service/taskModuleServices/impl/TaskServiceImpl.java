package com.notes.notes.service.taskModuleServices.impl;

import com.notes.notes.entity.authEntities.User;
import com.notes.notes.entity.taskModuleEntities.Task;
import com.notes.notes.entity.taskModuleEntities.TaskStatusHistory;
import com.notes.notes.repository.taskModuleRepositories.TaskAssignmentRepository;
import com.notes.notes.repository.taskModuleRepositories.TaskCommentRepository;
import com.notes.notes.repository.taskModuleRepositories.TaskRepository;
import com.notes.notes.repository.taskModuleRepositories.TaskStatusHistoryRepository;
import com.notes.notes.service.taskModuleServices.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final TaskStatusHistoryRepository taskStatusHistoryRepository;

    public TaskServiceImpl(TaskRepository taskRepository,
                           TaskAssignmentRepository taskAssignmentRepository,
                           TaskCommentRepository taskCommentRepository,
                           TaskStatusHistoryRepository taskStatusHistoryRepository) {
        this.taskRepository = taskRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.taskCommentRepository = taskCommentRepository;
        this.taskStatusHistoryRepository = taskStatusHistoryRepository;
    }

    @Override
    @Transactional
    public Task createTask(String title,
                           String description,
                           Task.TaskPriority priority,
                           User creator) {

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority);
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
            return task; // no-op
        }

        task.setStatus(newStatus);
        Task updatedTask = taskRepository.save(task);

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