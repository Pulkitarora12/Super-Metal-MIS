package com.notes.notes.service.impl;

import com.notes.notes.dto.ProductionEntryRequestDTO;
import com.notes.notes.entity.DowntimeEntry;
import com.notes.notes.entity.ProductionEntry;
import com.notes.notes.entity.TimeSlot;
import com.notes.notes.repository.DownTimeRepository;
import com.notes.notes.repository.ProductionEntryRepository;
import com.notes.notes.repository.TimeSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductionEntryService {

    @Autowired
    private ProductionEntryRepository repository;

    @Autowired
    private ProductionGoogleSheetsService productionGoogleSheetsService;

    public ProductionEntry saveProduction(ProductionEntryRequestDTO dto) {
        ProductionEntry entry = new ProductionEntry();
        entry.setDate(dto.getDate());
        entry.setShift(dto.getShift());
        entry.setLine(dto.getLine());
        entry.setMachine(dto.getMachine());
        entry.setOperation(dto.getOperation());
        entry.setOperator1(dto.getOperator1());
        entry.setOperator2(dto.getOperator2());
        entry.setPartNo(dto.getPartNo());
        entry.setPartName(dto.getPartName());
        entry.setRemarks(dto.getRemarks());
        entry.setSheetSize(dto.getSheetSize());
        entry.setInspector(dto.getInspector());

        // Time slots
        List<TimeSlot> timeSlots = dto.getTimeSlots().stream().map(ts -> {
            TimeSlot t = new TimeSlot();
            t.setFromTime(ts.getFromTime());
            t.setToTime(ts.getToTime());
            t.setProduced(ts.getProduced());
            t.setSegregated(ts.getSegregated());
            t.setRejected(ts.getRejected());
            t.setReason(ts.getReason());
            t.setRemarks(ts.getRemarks());
            t.setProductionEntry(entry);
            return t;
        }).collect(Collectors.toList());

        // Downtime
        List<DowntimeEntry> downtimeList = dto.getDowntimeEntries().stream().map(d -> {
            DowntimeEntry down = new DowntimeEntry();
            down.setReason(d.getReason());
            down.setMinutes(d.getMinutes());
            down.setProductionEntry(entry);
            return down;
        }).collect(Collectors.toList());

        entry.setTimeSlots(timeSlots);
        entry.setDowntimeEntries(downtimeList);

        // Save to database
        ProductionEntry savedEntry = repository.save(entry);

        // ✅ NEW: Sync to Google Sheets
        try {
            productionGoogleSheetsService.addProductionEntryToSheet(savedEntry);
        } catch (Exception e) {
            System.err.println("⚠️ Failed to sync ProductionEntry to Google Sheets: " + e.getMessage());
            // Don't throw - we still want the DB save to succeed
        }

        return savedEntry;
    }

    public List<ProductionEntry> getAllEntries() {
        List<ProductionEntry> entries = repository.findAll();
        return entries;
    }

    public List<TimeSlot> getTimeSlotsByEntry(Long id) {
        ProductionEntry entry = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found"));
        return entry.getTimeSlots();
    }

    public List<DowntimeEntry> getDowntimeByEntry(Long id) {
        ProductionEntry entry = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found"));
        return entry.getDowntimeEntries();
    }

    public void deleteEntry(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Entry not found");
        }

        // Delete from database (cascades to TimeSlots and DowntimeEntries)
        repository.deleteById(id);

        // ✅ NEW: Delete from Google Sheets
        try {
            productionGoogleSheetsService.deleteProductionEntryFromSheet(id);
        } catch (Exception e) {
            System.err.println("⚠️ Failed to delete ProductionEntry from Google Sheets: " + e.getMessage());
        }
    }

    // ✅ NEW: Update method (add this if you need update functionality)
    public ProductionEntry updateProduction(Long id, ProductionEntryRequestDTO dto) {
        ProductionEntry entry = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found with ID: " + id));

        // Update basic fields
        entry.setDate(dto.getDate());
        entry.setShift(dto.getShift());
        entry.setLine(dto.getLine());
        entry.setMachine(dto.getMachine());
        entry.setOperation(dto.getOperation());
        entry.setOperator1(dto.getOperator1());
        entry.setOperator2(dto.getOperator2());
        entry.setPartNo(dto.getPartNo());
        entry.setPartName(dto.getPartName());
        entry.setRemarks(dto.getRemarks());
        entry.setSheetSize(dto.getSheetSize());
        entry.setInspector(dto.getInspector());

        // Clear and rebuild time slots
        entry.getTimeSlots().clear();
        List<TimeSlot> timeSlots = dto.getTimeSlots().stream().map(ts -> {
            TimeSlot t = new TimeSlot();
            t.setFromTime(ts.getFromTime());
            t.setToTime(ts.getToTime());
            t.setProduced(ts.getProduced());
            t.setSegregated(ts.getSegregated());
            t.setRejected(ts.getRejected());
            t.setReason(ts.getReason());
            t.setRemarks(ts.getRemarks());
            t.setProductionEntry(entry);
            return t;
        }).collect(Collectors.toList());
        entry.getTimeSlots().addAll(timeSlots);

        // Clear and rebuild downtime entries
        entry.getDowntimeEntries().clear();
        List<DowntimeEntry> downtimeList = dto.getDowntimeEntries().stream().map(d -> {
            DowntimeEntry down = new DowntimeEntry();
            down.setReason(d.getReason());
            down.setMinutes(d.getMinutes());
            down.setProductionEntry(entry);
            return down;
        }).collect(Collectors.toList());
        entry.getDowntimeEntries().addAll(downtimeList);

        // Save to database
        ProductionEntry updatedEntry = repository.save(entry);

        // Sync to Google Sheets
        try {
            productionGoogleSheetsService.updateProductionEntryInSheet(updatedEntry);
        } catch (Exception e) {
            System.err.println("⚠️ Failed to update ProductionEntry in Google Sheets: " + e.getMessage());
        }

        return updatedEntry;
    }
}