package com.notes.notes.repository;

import com.notes.notes.entity.FieldData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FieldDataRepository extends JpaRepository<FieldData, Long> {

    // Fetch all data records for a specific field
    List<FieldData> findByFieldId(Long fieldId);
}

