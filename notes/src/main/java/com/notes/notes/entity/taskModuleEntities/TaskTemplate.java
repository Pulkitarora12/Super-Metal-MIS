package com.notes.notes.entity.taskModuleEntities;

import com.notes.notes.entity.authEntities.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "task_templates")
public class TaskTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long templateId;

    @Column(nullable = false, unique = true)
    private String templateName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecurrenceFrequency recurrenceFrequency; // WEEKLY, MONTHLY, QUARTERLY, YEARLY

    // For recurrence date specification
    private Integer dayOfWeek; // 1-7 (Monday to Sunday) - used when WEEKLY
    private Integer dayOfMonth; // 1-31 - used when MONTHLY or QUARTERLY
    private Integer monthOfYear; // 1-12 - used when YEARLY or QUARTERLY

    @Enumerated(EnumType.STRING)
    private Task.TaskPriority priority;

    @Column(nullable = false)
    private Integer daysBeforeToFlash; // days before due date to create task

    @Column(nullable = false)
    private Boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator; // Admin who created this template

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum RecurrenceFrequency { WEEKLY, MONTHLY, QUARTERLY, YEARLY }
}