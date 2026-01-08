package com.notes.notes.service.moduleServices.impl;

import com.notes.notes.dto.moduleDTOS.RMIRRequestDTO;
import com.notes.notes.entity.moduleEntities.Observation;
import com.notes.notes.entity.moduleEntities.RMIR;
import com.notes.notes.repository.moduleRepo.RMIRRepository;
import com.notes.notes.service.googleSheetServices.RMIRGoogleSheetsService;
import com.notes.notes.service.moduleServices.RMIRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RMIRServiceImpl implements RMIRService {

    @Autowired
    RMIRRepository rmirRepository;

    @Autowired
    RMIRGoogleSheetsService rmirGoogleSheetsService;

    private static final Set<String> activeUsers = ConcurrentHashMap.newKeySet();
    private static final Set<Long> activeDeletes = ConcurrentHashMap.newKeySet();

    @Override
    public RMIR saveRMIR(RMIRRequestDTO dto) {

        String user = dto.getInspector();

        if (!activeUsers.add(user)) {
            throw new IllegalStateException(
                    "Previous RMIR entry is still being processed"
            );
        }

        try {
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

            rmir.setCreatedDate(dto.getCreatedDate());
            rmir.setCreatedTime(dto.getCreatedTime());

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

            // Save to database
            RMIR savedRMIR = rmirRepository.save(rmir);

            // ✅ NEW: Sync to Google Sheets
            try {
                rmirGoogleSheetsService.addRMIRToSheet(savedRMIR);

            } catch (Exception e) {
                System.err.println("⚠️ Failed to sync RMIR to Google Sheets: " + e.getMessage());
                // Don't throw - we still want the DB save to succeed
            }

            return savedRMIR;
        } finally {
            activeUsers.remove(user);
        }

    }

    @Override
    public List<RMIR> getAllEntries() {
        return rmirRepository.findAll(
                Sort.by(Sort.Direction.DESC, "createdDate", "createdTime")
        );
    }

    @Override
    public void deleteRMIR(Long id) {

        if (!activeDeletes.add(id)) {
            throw new IllegalStateException(
                    "This RMIR entry is already being deleted"
            );
        }

        try {
            if (!rmirRepository.existsById(id)) {
                throw new RuntimeException("RMIR entry not found with id: " + id);
            }

            rmirRepository.deleteById(id);

            try {
                rmirGoogleSheetsService.deleteRMIRFromSheet(id);
            } catch (Exception e) {
                System.err.println("⚠️ Failed to delete RMIR from Google Sheets: " + e.getMessage());
            }

        } finally {
            activeDeletes.remove(id);
        }
    }

    // ✅ NEW: Update method (add this if you need update functionality)
    public RMIR updateRMIR(Long id, RMIRRequestDTO dto) {
        RMIR rmir = rmirRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RMIR entry not found with id: " + id));

        // Update basic fields
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

        rmir.setCreatedDate(dto.getCreatedDate());
        rmir.setCreatedTime(dto.getCreatedTime());

        // Clear and rebuild observations
        rmir.getObservations().clear();
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

        // Save to database
        RMIR updatedRMIR = rmirRepository.save(rmir);

        // Sync to Google Sheets
        try {
            rmirGoogleSheetsService.updateRMIRInSheet(updatedRMIR);
        } catch (Exception e) {
            System.err.println("⚠️ Failed to update RMIR in Google Sheets: " + e.getMessage());
        }

        return updatedRMIR;
    }
}