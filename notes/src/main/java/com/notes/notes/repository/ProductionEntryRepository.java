package com.notes.notes.repository;

import com.notes.notes.entity.ProductionEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductionEntryRepository extends JpaRepository<ProductionEntry,Long> {
}
