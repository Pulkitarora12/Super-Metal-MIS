package com.notes.notes.controller;

import com.notes.notes.dto.FieldDataCreateDTO;
import com.notes.notes.dto.MasterCreateDTO;
import com.notes.notes.dto.MasterFieldCreateDTO;
import com.notes.notes.entity.Master;
import com.notes.notes.service.MasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/masters")
public class MasterController {

    @Autowired
    private MasterService masterService;

    @GetMapping("/new")
    public String showCreateMasterForm(Model model) {
        MasterCreateDTO dto = new MasterCreateDTO();

        // Initialize 1 empty field
        MasterFieldCreateDTO fieldDTO = new MasterFieldCreateDTO();
        fieldDTO.setFieldData(List.of(new FieldDataCreateDTO())); // At least 1 data input

        dto.setFields(List.of(fieldDTO)); // Set default list

        model.addAttribute("master", dto);
        return "user/master-create-form";
    }

    @PostMapping("/create")
    public String createMaster(@ModelAttribute("master") MasterCreateDTO masterDTO) {
        Master savedMaster = masterService.createMaster(masterDTO);
        return "redirect:/masters/" + savedMaster.getId();
    }

    @GetMapping("/{id}")
    public String viewMaster(@PathVariable Long id, Model model) {
        Master master = masterService.getMasterById(id);
        if (master == null) {
            model.addAttribute("error", "Master not found!");
            return "user/master-details";
        }
        model.addAttribute("master", master);
        return "user/master-details";
    }

    @GetMapping
    public String listAllMasters(Model model) {
        List<Master> masters = masterService.getAllMasters();
        model.addAttribute("masters", masters);
        return "user/master-list";
    }

//    @GetMapping("/{id}/edit")
//    public String showEditMasterForm(@PathVariable Long id, Model model) {
//        Master master = masterService.getMasterById(id);
//        if (master == null) {
//            model.addAttribute("error", "Master not found!");
//            return "redirect:/masters";
//        }
//
//        model.addAttribute("master", master);
//        return "user/master-edit-form";
//    }
//
//    @PostMapping("/{id}/update")
//    public String updateMaster(@PathVariable Long id, @ModelAttribute("master") MasterCreateDTO masterDTO) {
//        masterService.updateMaster(id, masterDTO);
//        return "redirect:/masters/" + id;
//    }

    @PostMapping("/{id}/delete")
    public String deleteMaster(@PathVariable Long id) {
        masterService.deleteMaster(id);
        return "redirect:/masters"; // Redirect back to master list after deletion
    }

}
