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

    @Autowired
    RMIRGoogleSheetsService rmirGoogleSheetsService;

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

        // Save to database
        RMIR savedRMIR = rmirRepository.save(rmir);

        // ✅ NEW: Sync to Google Sheets
        try {
            rmirGoogleSheetsService.addRMIRToSheet(savedRMIR);
        } catch (Exception e) {
            System.err.println("⚠️ Failed to sync RMIR to Google Sheets: " + e.getMessage());
            // Don't throw - we still want the DB save to succeed
        }
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

        // Delete from database (cascades to Observations)
        rmirRepository.deleteById(id);

        // ✅ NEW: Delete from Google Sheets
        try {
            rmirGoogleSheetsService.deleteRMIRFromSheet(id);
        } catch (Exception e) {
            System.err.println("⚠️ Failed to delete RMIR from Google Sheets: " + e.getMessage());
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