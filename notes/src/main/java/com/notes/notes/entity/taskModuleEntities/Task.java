package com.notes.notes.entity.taskModuleEntities;

import com.notes.notes.entity.authEntities.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Getter
@Setter
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;

    @Column(nullable = false, unique = true)
    private String taskNo; // e.g., TSK-101 (You will generate this in Service)

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private TaskPriority priority; // LOW, MEDIUM, HIGH, CRITICAL

    @Enumerated(EnumType.STRING)
    private TaskStatus status; // CREATED, IN_PROGRESS, REVIEW, SUBMITTED, CLOSED

    private LocalDate dueDate;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator; // Link to your existing User entity

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "template_id", nullable = true)
    private TaskTemplate sourceTemplate;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.status = TaskStatus.CREATED;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Enums defined inside for simplicity (or move to separate files)
    public enum TaskPriority { LOW, MEDIUM, HIGH, CRITICAL }
    public enum TaskStatus { CREATED, IN_PROGRESS, REVIEW, SUBMITTED, CLOSED }
}