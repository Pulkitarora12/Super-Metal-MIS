package com.notes.notes.service.impl;

import com.notes.notes.dto.RMIRRequestDTO;
import com.notes.notes.entity.Observation;
import com.notes.notes.entity.RMIR;
import com.notes.notes.repository.RMIRRepository;
import com.notes.notes.service.RMIRService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RMIRServiceImpl implements RMIRService {

    @Autowired
    RMIRRepository rmirRepository;

    @Override
    public void saveRMIR(RMIRRequestDTO dto) {

        RMIR rmir = new RMIR();

        rmir.setPartNo(dto.getPartNo());
        rmir.setPartName(dto.getPartName());
        rmir.setRmSize(dto.getRmSize());
        rmir.setGrade(dto.getGrade());
        rmir.setStdGW(dto.getStdGW());
        rmir.setBundleGrade(dto.getBundleGrade());
        rmir.setBundleNo(dto.getBundleNo());
        rmir.setBundleGW(dto.getBundleGW());
        rmir.setBundleNW(dto.getBundleNW());
        rmir.setBundleSize(dto.getBundleSize());
        rmir.setSupplier(dto.getSupplier());
        rmir.setInspector(dto.getInspector());
        rmir.setRemarks(dto.getRemarks());

        // Handle Observations
        if (dto.getObservations() != null) {
            dto.getObservations().forEach(obsDto -> {
                Observation observation = new Observation();
                observation.setL(obsDto.getL());
                observation.setB(obsDto.getB());
                observation.setH(obsDto.getH());
                observation.setGwSheet(obsDto.getGwSheet());
                observation.setRmir(rmir);

                rmir.getObservations().add(observation);
            });
        }

        rmirRepository.save(rmir);
    }

    @Override
    public List<RMIR> getAllEntries() {
        return rmirRepository.findAll();
    }

    @Override
    public void deleteRMIR(Long id) {
        if (!rmirRepository.existsById(id)) {
            throw new RuntimeException("RMIR entry not found with id: " + id);
        }
        rmirRepository.deleteById(id);
    }
}
