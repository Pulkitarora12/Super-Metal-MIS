package com.notes.notes.dto;

import com.notes.notes.entity.DataType;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MasterFieldCreateDTO {
    private String fieldName;
    private DataType dataType;
    private List<FieldDataCreateDTO> fieldData;
}