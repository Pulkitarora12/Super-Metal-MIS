package com.notes.notes.repository.taskModuleRepositories;

import com.notes.notes.entity.taskModuleEntities.Task;
import com.notes.notes.entity.authEntities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // 1. Find all tasks created by a specific user (For "Created by Me" tab)
    List<Task> findByCreator(User creator);

    // 2. Find a task by its unique Task Number (For Search functionality)
    Optional<Task> findByTaskNo(String taskNo);

    @Query("SELECT MAX(t.taskId) FROM Task t")
    Long findMaxTaskId();

    // 3. Find tasks by status (Optional, useful for filters like "Show all Closed tasks")
    List<Task> findByStatus(Task.TaskStatus status);

//    List<Task> findByCreatorAndPriority(User creator, Task.TaskPriority priority);
//
//    List<Task> findByCreatorAndStatus(User creator, Task.TaskStatus status);
//
//    List<Task> findByCreatorAndPriorityAndStatus(
//            User creator,
//            Task.TaskPriority priority,
//            Task.TaskStatus status
//    );
}