package com.zebra.ai_multibarcodes_capture.filemanagement;

import android.util.Log;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExportWriters {
    public static void appendDataToTxtFile(File dataFile, String data) {
        try {
            // Create the file if it doesn't exist
            if (!dataFile.exists()) {
                dataFile.createNewFile();
            }
            // Append data to the file
            FileWriter fileWriter = new FileWriter(dataFile, true); // Append mode
            fileWriter.append(data + "\n");
            fileWriter.close();
        } catch (IOException e) {
            // Handle exceptions, e.g., log the error
            e.printStackTrace();
        }
    }

    public static void appendDataToCsvFile(File dataFile, String data, String symbology, Date nowDate) {
        try {
            // Create the file if it doesn't exist
            if (!dataFile.exists()) {
                dataFile.createNewFile();
                // Append data to the file
                FileWriter fileWriter = new FileWriter(dataFile, true); // Append mode
                fileWriter.append("Heure;Symbologie;Donnee" + "\n");
                fileWriter.close();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
            String currentTime = sdf.format(nowDate);
            // Append data to the file
            FileWriter fileWriter = new FileWriter(dataFile, true); // Append mode
            fileWriter.append(currentTime + ";" + symbology + ";" + data + "\n");
            fileWriter.close();
        } catch (IOException e) {
            // Handle exceptions, e.g., log the error
            e.printStackTrace();
        }
    }

    public static void appendDataToExcelFile(File dataFile, String data, String symbology, Date nowDate) {
        Workbook workbook = null;
        FileInputStream fis = null;
        FileOutputStream fos = null;

        try {
            // Check if file exists, if not create it
            if (!dataFile.exists()) {
                workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("Data");

                // Create header row
                Row headerRow = sheet.createRow(0);
                String[] headers = {"Heure", "Symbologie", "Donnee"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                }
            } else {
                // Read existing workbook
                fis = new FileInputStream(dataFile);
                workbook = new XSSFWorkbook(fis);
                fis.close();
            }

            // Get the first sheet
            Sheet sheet = workbook.getSheetAt(0);

            // Get the last row number and create a new row
            int lastRowNum = sheet.getLastRowNum();
            Row row = sheet.createRow(lastRowNum + 1);

            // Create cell styles
            CreationHelper createHelper = workbook.getCreationHelper();

            // Time cell style (for HHmmss format)
            CellStyle timeStyle = workbook.createCellStyle();
            timeStyle.setDataFormat(createHelper.createDataFormat().getFormat("HH:mm:ss"));

            // Get current time
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            String currentTime = sdf.format(nowDate);

            // Create cells with data

            // Time cell as numeric with time format
            Cell timeCell = row.createCell(0);
            timeCell.setCellValue(currentTime);
            timeCell.setCellStyle(timeStyle);

            row.createCell(1).setCellValue(symbology);

            // Integer cell style (0 decimals)
            CellStyle integerStyle = workbook.createCellStyle();
            integerStyle.setDataFormat(createHelper.createDataFormat().getFormat("0"));
            Cell dataCell = row.createCell(2);
            dataCell.setCellValue(data);
            dataCell.setCellStyle(integerStyle);

            // Write to file
            fos = new FileOutputStream(dataFile);
            workbook.write(fos);
        } catch (IOException e) {
            Log.e("ExcelAppend", "Error appending to Excel file", e);
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (workbook != null) workbook.close();
                if (fos != null) fos.close();
            } catch (IOException e) {
                Log.e("ExcelAppend", "Error closing resources", e);
            }
        }
    }

}
