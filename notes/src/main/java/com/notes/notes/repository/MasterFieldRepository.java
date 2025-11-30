package com.notes.notes.repository;

import com.notes.notes.entity.MasterField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasterFieldRepository extends JpaRepository<MasterField, Long> {

    // Fetch all fields for a specific master
    List<MasterField> findByMasterId(Long masterId);
}