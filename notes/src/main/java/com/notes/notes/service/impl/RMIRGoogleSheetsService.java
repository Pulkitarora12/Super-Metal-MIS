package com.notes.notes.service.impl;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import com.notes.notes.entity.Observation;
import com.notes.notes.entity.RMIR;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class RMIRGoogleSheetsService {

    @Autowired
    private Sheets sheetsClient;

    @Value("${google.sheets.spreadsheet.id}")
    private String spreadsheetId;

    private static final String RMIR_SHEET = "RMIREntries";
    private static final String OBSERVATION_SHEET = "Observations";

    // ==================== RMIR OPERATIONS ====================

    /**
     * CREATE: Add new RMIR entry to Google Sheets
     * Also adds all associated observations
     */
    public void addRMIRToSheet(RMIR rmir) {
        try {
            // Add main RMIR entry
            List<Object> row = convertRMIRToRow(rmir);
            ValueRange body = new ValueRange().setValues(Arrays.asList(row));

            sheetsClient.spreadsheets().values()
                    .append(spreadsheetId, RMIR_SHEET, body)
                    .setValueInputOption("RAW")
                    .execute();

            System.out.println("✅ RMIR added to Google Sheets: ID " + rmir.getId());

            // Add all observations
            if (rmir.getObservations() != null && !rmir.getObservations().isEmpty()) {
                for (Observation observation : rmir.getObservations()) {
                    addObservationToSheet(observation);
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Error adding RMIR to Google Sheets: " + e.getMessage());
        }
    }

    /**
     * UPDATE: Update existing RMIR entry in Google Sheets
     * Also updates all associated observations
     */
    public void updateRMIRInSheet(RMIR rmir) {
        try {
            // Find and update main RMIR entry
            int rowNumber = findRowByRMIRId(rmir.getId());

            if (rowNumber == -1) {
                System.out.println("⚠️ RMIR not found in sheet, adding new row");
                addRMIRToSheet(rmir);
                return;
            }

            List<Object> row = convertRMIRToRow(rmir);
            ValueRange body = new ValueRange().setValues(Arrays.asList(row));
            String range = RMIR_SHEET + "!A" + rowNumber + ":N" + rowNumber; // A to N columns

            sheetsClient.spreadsheets().values()
                    .update(spreadsheetId, range, body)
                    .setValueInputOption("RAW")
                    .execute();

            System.out.println("✅ RMIR updated in Google Sheets: ID " + rmir.getId());

            // Delete and re-add all observations (simpler than selective update)
            deleteAllObservationsForRMIR(rmir.getId());
            if (rmir.getObservations() != null && !rmir.getObservations().isEmpty()) {
                for (Observation observation : rmir.getObservations()) {
                    addObservationToSheet(observation);
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Error updating RMIR in Google Sheets: " + e.getMessage());
        }
    }

    /**
     * DELETE: Delete RMIR entry from Google Sheets
     * Also deletes all associated observations
     */
    public void deleteRMIRFromSheet(Long rmirId) {
        try {
            // Delete all observations first
            deleteAllObservationsForRMIR(rmirId);

            // Delete main RMIR entry
            int rowNumber = findRowByRMIRId(rmirId);

            if (rowNumber == -1) {
                System.out.println("⚠️ RMIR not found in sheet");
                return;
            }

            DeleteDimensionRequest deleteRequest = new DeleteDimensionRequest()
                    .setRange(new DimensionRange()
                            .setSheetId(getSheetId(RMIR_SHEET))
                            .setDimension("ROWS")
                            .setStartIndex(rowNumber - 1)
                            .setEndIndex(rowNumber));

            List<Request> requests = new ArrayList<>();
            requests.add(new Request().setDeleteDimension(deleteRequest));

            BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest()
                    .setRequests(requests);

            sheetsClient.spreadsheets().batchUpdate(spreadsheetId, body).execute();

            System.out.println("✅ RMIR deleted from Google Sheets: ID " + rmirId);

        } catch (IOException e) {
            System.err.println("❌ Error deleting RMIR from Google Sheets: " + e.getMessage());
        }
    }

    // ==================== OBSERVATION OPERATIONS ====================

    /**
     * Add a single observation to Google Sheets
     */
    private void addObservationToSheet(Observation observation) {
        try {
            List<Object> row = convertObservationToRow(observation);
            ValueRange body = new ValueRange().setValues(Arrays.asList(row));

            sheetsClient.spreadsheets().values()
                    .append(spreadsheetId, OBSERVATION_SHEET, body)
                    .setValueInputOption("RAW")
                    .execute();

            System.out.println("  ✅ Observation added: ID " + observation.getId());

        } catch (IOException e) {
            System.err.println("  ❌ Error adding Observation: " + e.getMessage());
        }
    }

    /**
     * Delete all observations for a specific RMIR entry
     */
    private void deleteAllObservationsForRMIR(Long rmirId) throws IOException {
        String range = OBSERVATION_SHEET + "!B:B"; // Column B contains RMIR_ID
        ValueRange response = sheetsClient.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            return;
        }

        List<Request> deleteRequests = new ArrayList<>();

        // Collect all rows to delete (iterate backwards to handle row shifting)
        for (int i = values.size() - 1; i >= 0; i--) {
            List<Object> row = values.get(i);
            if (row != null && !row.isEmpty()) {
                String cellValue = row.get(0).toString();
                if (cellValue.equals(rmirId.toString())) {
                    DeleteDimensionRequest deleteRequest = new DeleteDimensionRequest()
                            .setRange(new DimensionRange()
                                    .setSheetId(getSheetId(OBSERVATION_SHEET))
                                    .setDimension("ROWS")
                                    .setStartIndex(i)
                                    .setEndIndex(i + 1));

                    deleteRequests.add(new Request().setDeleteDimension(deleteRequest));
                }
            }
        }

        if (!deleteRequests.isEmpty()) {
            BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest()
                    .setRequests(deleteRequests);
            sheetsClient.spreadsheets().batchUpdate(spreadsheetId, body).execute();
            System.out.println("  ✅ Deleted " + deleteRequests.size() + " Observations for RMIR: " + rmirId);
        }
    }

    // ==================== CONVERTER METHODS ====================

    /**
     * Convert RMIR to row of values (14 columns: A-N)
     */
    private List<Object> convertRMIRToRow(RMIR rmir) {
        List<Object> row = new ArrayList<>();
        row.add(rmir.getId());
        row.add(rmir.getPartNo() != null ? rmir.getPartNo() : "");
        row.add(rmir.getPartName() != null ? rmir.getPartName() : "");
        row.add(rmir.getRmSize() != null ? rmir.getRmSize() : "");
        row.add(rmir.getGrade() != null ? rmir.getGrade() : "");
        row.add(rmir.getStdGW() != null ? rmir.getStdGW() : "");
        row.add(rmir.getBundleGrade() != null ? rmir.getBundleGrade() : "");
        row.add(rmir.getBundleNo() != null ? rmir.getBundleNo() : "");
        row.add(rmir.getBundleGW() != null ? rmir.getBundleGW() : "");
        row.add(rmir.getBundleNW() != null ? rmir.getBundleNW() : "");
        row.add(rmir.getBundleSize() != null ? rmir.getBundleSize() : "");
        row.add(rmir.getSupplier() != null ? rmir.getSupplier() : "");
        row.add(rmir.getInspector() != null ? rmir.getInspector() : "");
        row.add(rmir.getRemarks() != null ? rmir.getRemarks() : "");
        return row;
    }

    /**
     * Convert Observation to row of values (6 columns: A-F)
     */
    private List<Object> convertObservationToRow(Observation observation) {
        List<Object> row = new ArrayList<>();
        row.add(observation.getId());
        row.add(observation.getRmir() != null ? observation.getRmir().getId() : "");
        row.add(observation.getL() != null ? observation.getL() : "");
        row.add(observation.getB() != null ? observation.getB() : "");
        row.add(observation.getH() != null ? observation.getH() : "");
        row.add(observation.getGwSheet() != null ? observation.getGwSheet() : "");
        return row;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Find row number by RMIR ID
     */
    private int findRowByRMIRId(Long id) throws IOException {
        String range = RMIR_SHEET + "!A:A";
        ValueRange response = sheetsClient.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            return -1;
        }

        for (int i = 0; i < values.size(); i++) {
            List<Object> row = values.get(i);
            if (row != null && !row.isEmpty()) {
                String cellValue = row.get(0).toString();
                if (cellValue.equals(id.toString())) {
                    return i + 1; // Sheet rows are 1-indexed
                }
            }
        }
        return -1;
    }

    /**
     * Get sheet ID by sheet name (needed for delete operations)
     */
    private Integer getSheetId(String sheetName) throws IOException {
        Spreadsheet spreadsheet = sheetsClient.spreadsheets()
                .get(spreadsheetId)
                .execute();

        for (Sheet sheet : spreadsheet.getSheets()) {
            if (sheet.getProperties().getTitle().equals(sheetName)) {
                return sheet.getProperties().getSheetId();
            }
        }

        // Default to 0 if not found (for backwards compatibility)
        return 0;
    }
}