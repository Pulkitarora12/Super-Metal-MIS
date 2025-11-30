package com.notes.notes.service;

import com.notes.notes.dto.MasterCreateDTO;
import com.notes.notes.entity.Master;

import java.util.List;

public interface MasterService {
    Master createMaster(MasterCreateDTO masterDTO);

    List<Master> getAllMasters();

    Master getMasterById(Long id);

    Master updateMaster(Long id, MasterCreateDTO masterDTO);

    void deleteMaster(Long id);
}