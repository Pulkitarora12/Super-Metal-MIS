package com.notes.notes.service.taskModuleServices.impl;

import com.notes.notes.entity.authEntities.User;
import com.notes.notes.entity.taskModuleEntities.Task;
import com.notes.notes.entity.taskModuleEntities.TaskAssignment;
import com.notes.notes.repository.taskModuleRepositories.TaskAssignmentRepository;
import com.notes.notes.service.taskModuleServices.TaskAssignmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskAssignmentServiceImpl implements TaskAssignmentService {

    private final TaskAssignmentRepository taskAssignmentRepository;

    public TaskAssignmentServiceImpl(TaskAssignmentRepository taskAssignmentRepository) {
        this.taskAssignmentRepository = taskAssignmentRepository;
    }

    @Override
    @Transactional
    public TaskAssignment assignMainAssignee(Task task, User user) {

        // Ensure only one MAIN_ASSIGNEE exists
        TaskAssignment existingMain =
                taskAssignmentRepository.findByTaskAndRoleType(
                        task, TaskAssignment.AssignmentRole.MAIN_ASSIGNEE);

        if (existingMain != null) {
            taskAssignmentRepository.delete(existingMain);
        }

        TaskAssignment assignment = new TaskAssignment();
        assignment.setTask(task);
        assignment.setUser(user);
        assignment.setRoleType(TaskAssignment.AssignmentRole.MAIN_ASSIGNEE);

        return taskAssignmentRepository.save(assignment);
    }

    @Override
    @Transactional
    public TaskAssignment addSupportingAssignee(Task task, User user) {

        if (taskAssignmentRepository.existsByTaskAndUser(task, user)) {
            throw new IllegalStateException("User already assigned to task");
        }

        TaskAssignment assignment = new TaskAssignment();
        assignment.setTask(task);
        assignment.setUser(user);
        assignment.setRoleType(TaskAssignment.AssignmentRole.SUPPORTING);

        return taskAssignmentRepository.save(assignment);
    }

    @Override
    @Transactional
    public void removeAssignee(Task task, User user) {

        List<TaskAssignment> assignments = taskAssignmentRepository.findByTask(task);

        assignments.stream()
                .filter(a -> a.getUser().equals(user))
                .forEach(taskAssignmentRepository::delete);
    }

    @Override
    public List<TaskAssignment> getAssigneesByTask(Task task) {
        return taskAssignmentRepository.findByTask(task);
    }

    @Override
    public boolean isUserAssignedToTask(Task task, User user) {
        return taskAssignmentRepository.existsByTaskAndUser(task, user);
    }

    @Override
    public List<TaskAssignment> getAssignmentsByUser(User user) {
        return taskAssignmentRepository.findByUser(user);
    }

    @Override
    public TaskAssignment getAssignment(Task task, User user) {
        return taskAssignmentRepository.findByTaskAndUser(task, user);
    }
}
