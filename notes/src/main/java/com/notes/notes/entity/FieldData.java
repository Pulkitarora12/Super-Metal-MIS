package com.notes.notes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
public class FieldData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String value;   // Store everything as String and parse based on dataType

    @ManyToOne
    @JoinColumn(name = "field_id")
    private MasterField field;
}
