package com.notes.notes.service.taskModuleServices;

import com.notes.notes.entity.authEntities.User;
import com.notes.notes.entity.taskModuleEntities.Task;
import com.notes.notes.entity.taskModuleEntities.TaskAssignment;

import java.util.List;
import java.util.Optional;

public interface TaskService {

    // Create a new task
    Task createTask(
            String title,
            String description,
            Task.TaskPriority priority,
            User creator
    );

    // Update task status and log history
    Task updateTaskStatus(
            Long taskId,
            Task.TaskStatus newStatus,
            User changedBy
    );

    // Fetch task by DB id
    Optional<Task> getTaskById(Long taskId);

    // Fetch task by business key (TSK-xxx)
    Optional<Task> getTaskByTaskNo(String taskNo);

    // Tasks created by a specific user
    List<Task> getTasksCreatedByUser(User creator);

    // Fetch tasks by status
    List<Task> getTasksByStatus(Task.TaskStatus status);

}