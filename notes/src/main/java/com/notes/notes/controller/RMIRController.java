package com.notes.notes.controller;

import com.notes.notes.dto.RMIRRequestDTO;
import com.notes.notes.entity.RMIR;
import com.notes.notes.repository.RMIRRepository;
import com.notes.notes.service.RMIRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/rmir-entry")
public class RMIRController {

    @Autowired
    private RMIRService service;

    @Autowired
    private RMIRRepository repository;

    @GetMapping
    public String showForm(Model model) {
        model.addAttribute("dto", new RMIRRequestDTO());
        return "user/rmir_entry";
    }

    @PostMapping
    public String submitRMIR(@ModelAttribute RMIRRequestDTO dto,
                             RedirectAttributes redirectAttributes) {
        service.saveRMIR(dto);
        redirectAttributes.addFlashAttribute("success", "RMIR submitted successfully!");
        return "redirect:/rmir-entry";
    }
    @GetMapping("/getAll")
    public String getRMIRHistory(Model model) {
        model.addAttribute("entries", service.getAllEntries());
        return "user/rmir_history";
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportToCSV() {
        List<RMIR> entries = service.getAllEntries();

        StringBuilder csvData = new StringBuilder();
        csvData.append("ID,Part No,Part Name,RM Size,Grade Master,Bundle Grade,Bundle No,Bundle GW,Bundle NW,Bundle Size,Supplier,Inspector,Remarks\n");

        for (RMIR entry : entries) {
            csvData.append(entry.getId()).append(",")
                    .append(entry.getPartNo()).append(",")
                    .append(entry.getPartName()).append(",")
                    .append(entry.getRmSize()).append(",")
                    .append(entry.getGradeMaster()).append(",")
                    .append(entry.getBundleGrade()).append(",")
                    .append(entry.getBundleNo()).append(",")
                    .append(entry.getBundleGW()).append(",")
                    .append(entry.getBundleNW()).append(",")
                    .append(entry.getBundleSize()).append(",")
                    .append(entry.getSupplier()).append(",")
                    .append(entry.getInspector()).append(",")
                    .append(entry.getRemarks()).append("\n");
        }

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=rmir_history.csv")
                .contentType(org.springframework.http.MediaType.parseMediaType("text/csv"))
                .body(csvData.toString());
    }

    @GetMapping("/{id}/observations")
    public String viewObservations(@PathVariable Long id, Model model) {
        RMIR rmir = repository.findById(id).orElse(null);
        if (rmir != null) {
            model.addAttribute("observations", rmir.getObservations());
            model.addAttribute("entryId", id);
        }
        return "user/rmir_observations"; // Make this page
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/delete")
    public String deleteRMIR(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.deleteRMIR(id);
            redirectAttributes.addFlashAttribute("success", "RMIR entry deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete RMIR entry.");
        }
        return "redirect:/rmir-entry/getAll";  // Adjust if needed
    }

}

