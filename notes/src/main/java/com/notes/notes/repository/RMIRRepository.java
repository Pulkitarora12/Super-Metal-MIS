package com.notes.notes.repository;

import com.notes.notes.entity.RMIR;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RMIRRepository extends JpaRepository<RMIR,Long> {
}
