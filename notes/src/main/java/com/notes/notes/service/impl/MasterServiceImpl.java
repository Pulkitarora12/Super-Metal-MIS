package com.notes.notes.service.impl;

import com.notes.notes.dto.FieldDataCreateDTO;
import com.notes.notes.dto.MasterCreateDTO;
import com.notes.notes.dto.MasterFieldCreateDTO;
import com.notes.notes.entity.FieldData;
import com.notes.notes.entity.Master;
import com.notes.notes.entity.MasterField;
import com.notes.notes.repository.MasterRepository;
import com.notes.notes.service.MasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MasterServiceImpl implements MasterService {

    @Autowired
    private MasterRepository masterRepository;

    @Override
    public Master createMaster(MasterCreateDTO masterDTO) {

        Master master = new Master();
        master.setName(masterDTO.getName());

        if (masterDTO.getFields() != null) {
            for (MasterFieldCreateDTO fieldDTO : masterDTO.getFields()) {

                MasterField field = new MasterField();
                field.setFieldName(fieldDTO.getFieldName());
                field.setDataType(fieldDTO.getDataType());
                field.setMaster(master);

                if (fieldDTO.getFieldData() != null) {
                    for (FieldDataCreateDTO dataDTO : fieldDTO.getFieldData()) {
                        FieldData data = new FieldData();
                        data.setValue(dataDTO.getValue());
                        data.setField(field);
                        field.getFieldData().add(data);
                    }
                }

                master.getFields().add(field);
            }
        }

        return masterRepository.save(master);
    }

    @Override
    public Master getMasterById(Long id) {
        return masterRepository.findById(id).orElse(null);
    }

    @Override
    public List<Master> getAllMasters() {
        return masterRepository.findAll();
    }

    @Override
    public Master updateMaster(Long id, MasterCreateDTO masterDTO) {
        Master master = masterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Master not found"));

        // Update Master Name
        master.setName(masterDTO.getName());

        // Update / Add Fields
        if (masterDTO.getFields() != null) {
            for (MasterFieldCreateDTO fieldDTO : masterDTO.getFields()) {

                // Find existing field or create new
                MasterField field = master.getFields().stream()
                        .filter(f -> f.getFieldName().equals(fieldDTO.getFieldName()))
                        .findFirst()
                        .orElse(new MasterField());

                field.setFieldName(fieldDTO.getFieldName());
                field.setDataType(fieldDTO.getDataType());
                field.setMaster(master);

                // Update / Add field data
                if (fieldDTO.getFieldData() != null) {
                    for (FieldDataCreateDTO dataDTO : fieldDTO.getFieldData()) {

                        FieldData data = field.getFieldData().stream()
                                .filter(d -> d.getValue().equals(dataDTO.getValue()))
                                .findFirst()
                                .orElse(new FieldData());

                        data.setValue(dataDTO.getValue());
                        data.setField(field);

                        if (!field.getFieldData().contains(data)) {
                            field.getFieldData().add(data);
                        }
                    }
                }

                if (!master.getFields().contains(field)) {
                    master.getFields().add(field);
                }
            }
        }

        return masterRepository.save(master);
    }

    @Override
    public void deleteMaster(Long id) {
        masterRepository.deleteById(id);
    }


}