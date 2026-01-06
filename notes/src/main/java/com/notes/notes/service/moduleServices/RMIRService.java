package com.notes.notes.service.moduleServices;

import com.notes.notes.dto.moduleDTOS.RMIRRequestDTO;
import com.notes.notes.entity.moduleEntities.RMIR;

import java.util.List;

public interface RMIRService {
    RMIR saveRMIR(RMIRRequestDTO dto);

    List<RMIR> getAllEntries();

    public void deleteRMIR(Long id);

}