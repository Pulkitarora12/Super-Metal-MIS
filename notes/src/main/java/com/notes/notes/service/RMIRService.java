package com.notes.notes.service;

import com.notes.notes.dto.RMIRRequestDTO;
import com.notes.notes.entity.RMIR;

import java.util.List;

public interface RMIRService {
    void saveRMIR(RMIRRequestDTO dto);

    List<RMIR> getAllEntries();

    public void deleteRMIR(Long id);

}