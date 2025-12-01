package com.notes.notes.controller;

import com.notes.notes.dto.ProductionEntryRequestDTO;
import com.notes.notes.entity.FieldData;
import com.notes.notes.entity.Master;
import com.notes.notes.entity.MasterField;
import com.notes.notes.entity.ProductionEntry;
import com.notes.notes.repository.MasterRepository;
import com.notes.notes.service.MasterService;
import com.notes.notes.service.impl.ProductionEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/production-entry")
public class ProductionEntryController {

    @Autowired
    private ProductionEntryService service;

    @Autowired
    private MasterRepository  masterRepository;

    @Autowired
    private MasterService masterService;

    @GetMapping
    public String showProductionEntryForm(@AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        List<String> machineNames = masterService.getMachineNames();
        List<String> rejectionReasons = masterService.getRejectionReasons();
        List<Map<String, String>> partDetails = masterService.getPartDetailsList();
        List<String> operations = masterService.getOperations();
        List<String> operators = masterService.getOperators();
        List<String> downtimeReasons = masterService.getDowntimeReasons();
        String username = userDetails.getUsername();

        ProductionEntryRequestDTO dto = new ProductionEntryRequestDTO();
        dto.setInspector(username);

        // Send to frontend
        model.addAttribute("machineNames", machineNames);
        model.addAttribute("rejectionReasons", rejectionReasons);
        model.addAttribute("partDetails", partDetails);
        model.addAttribute("operations", operations);
        model.addAttribute("operators", operators);
        model.addAttribute("downtimeReasons", downtimeReasons);
        model.addAttribute("username", username);

        model.addAttribute("dto", dto);

        return "user/production_entry";
    }


    @PostMapping
    public String saveProduction(@ModelAttribute ProductionEntryRequestDTO dto,
                                 RedirectAttributes redirectAttributes) {
        service.saveProduction(dto);
        redirectAttributes.addFlashAttribute("success", "Production entry saved successfully!");
        return "redirect:/production-entry";
    }

    @GetMapping("/getAll")
    public String getProductionHistory(Model model) {
        model.addAttribute("entries", service.getAllEntries());
        return "user/production_history";
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportProductionCSV() {
        List<ProductionEntry> entries = service.getAllEntries();

        StringBuilder csvData = new StringBuilder();
        csvData.append("ID,Date,Shift,Line,Machine,Operation,Operator 1,Operator 2,Part No,Part Name,Remarks\n");

        for (ProductionEntry entry : entries) {
            csvData.append(entry.getId()).append(",")
                    .append(entry.getDate()).append(",")
                    .append(entry.getShift()).append(",")
                    .append(entry.getLine()).append(",")
                    .append(entry.getMachine()).append(",")
                    .append(entry.getOperation()).append(",")
                    .append(entry.getOperator1()).append(",")
                    .append(entry.getOperator2()).append(",")
                    .append(entry.getPartNo()).append(",")
                    .append(entry.getPartName()).append(",")
                    .append(entry.getRemarks()).append("\n");
        }

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=production_history.csv")
                .contentType(org.springframework.http.MediaType.parseMediaType("text/csv"))
                .body(csvData.toString());
    }

    @GetMapping("/{id}/timeslots")
    public String viewTimeSlots(@PathVariable Long id, Model model) {
        model.addAttribute("timeSlots", service.getTimeSlotsByEntry(id));
        model.addAttribute("entryId", id);
        return "user/timeslots";
    }

    @GetMapping("/{id}/downtime")
    public String viewDowntime(@PathVariable Long id, Model model) {
        model.addAttribute("downtimes", service.getDowntimeByEntry(id));
        model.addAttribute("entryId", id);
        return "user/downtime";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/delete")
    public String deleteEntry(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.deleteEntry(id);
            redirectAttributes.addFlashAttribute("success", "Production entry deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete entry.");
        }
        return "redirect:/production-entry/getAll";
    }

}
