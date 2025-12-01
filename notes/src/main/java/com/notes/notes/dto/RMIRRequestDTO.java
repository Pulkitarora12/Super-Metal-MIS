package com.notes.notes.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RMIRRequestDTO {
    private String partNo;
    private String partName;
    private String rmSize;
    private String grade;
    private String stdGW;
    private String bundleGrade;
    private String bundleNo;
    private Double bundleGW;
    private Double bundleNW;
    private String bundleSize;
    private String supplier;
    private String inspector;
    private String remarks;

    private List<ObservationDTO> observations = new ArrayList<>();

    @Data
    public static class ObservationDTO {
        private Double l;
        private Double b;
        private Double h;
        private String gwSheet;
    }
}
