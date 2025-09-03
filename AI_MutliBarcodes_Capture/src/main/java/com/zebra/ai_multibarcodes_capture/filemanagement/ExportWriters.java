package com.zebra.ai_multibarcodes_capture.filemanagement;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.zebra.ai_multibarcodes_capture.helpers.EBarcodesSymbologies;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class ExportWriters {

    private static final String TAG = "ExportWriters";

    public static class loadedData
    {
        public Map<Integer, String> barcodeValues;
        public Map<Integer, Integer> barcodeQuantityMap;
        public Map<Integer, Integer> barcodeSymbologyMap;
        public Map<Integer, Date> barcodeDateMap;
    }

    public static loadedData loadData(Context context, String sessionFilePath)
    {
        // Retrieve Today Folder
        File targetFile = new File(sessionFilePath);
        if (targetFile.exists() == false) {
            Toast.makeText(context, context.getString(com.zebra.ai_multibarcodes_capture.R.string.error_could_not_create_file, sessionFilePath), Toast.LENGTH_LONG).show();
            return null;
        }
        String fileExtension = FileUtil.getFileExtension(targetFile);
        switch(fileExtension)
        {
            case ".csv":
                return loadDataCSV(context, targetFile);
            case ".xlsx":
                return loadDataXSLX(context, targetFile);
            default:
                return loadDataTXT(context, targetFile);
        }
    }
    public static boolean saveData(Context context, String captureFilePath, Map<Integer, String> barcodeValueMap, Map<Integer, Integer> barcodeQuantityMap, Map<Integer, Integer> barcodeSymbologyMap, Map<Integer, Date> barcodeDateMap)
    {
        // Retrieve Today Folder
        File targetFile = new File(captureFilePath);
        if (targetFile.exists() == false) {
            try {
                targetFile.createNewFile();
            }
            catch (Exception e)
            {
                Toast.makeText(context, context.getString(com.zebra.ai_multibarcodes_capture.R.string.error_could_not_create_file, captureFilePath), Toast.LENGTH_LONG).show();
                return false;
            }
        }
        String fileExtension = FileUtil.getFileExtension(targetFile);
        switch(fileExtension)
        {
            case ".csv":
                return saveDataCSV(context, targetFile, barcodeValueMap, barcodeQuantityMap,barcodeSymbologyMap, barcodeDateMap);
            case ".xlsx":
                return saveDataXSLX(context, targetFile, barcodeValueMap, barcodeQuantityMap, barcodeSymbologyMap, barcodeDateMap);
            default:
                return saveDataTXT(context, targetFile, barcodeValueMap, barcodeQuantityMap, barcodeSymbologyMap, barcodeDateMap);
        }
    }

    public static loadedData loadDataTXT(Context context, File sessionFile)
    {
        loadedData loadedData = new loadedData();
        loadedData.barcodeQuantityMap = new HashMap<>();
        loadedData.barcodeSymbologyMap = new HashMap<>();
        loadedData.barcodeDateMap = new HashMap<>();
        loadedData.barcodeValues = new HashMap<>();

        if (!sessionFile.exists() || sessionFile.length() == 0) {
            Log.w(TAG, "Session file does not exist or is empty: " + sessionFile.getPath());
            return loadedData;
        }

        try (Scanner scanner = new Scanner(sessionFile)) {
            String currentValue = null;
            String currentSymbology = null;
            Integer currentQuantity = null;
            Date currentDate = null;
            Integer barcodeUniqueIndex = 0;

            // Skip header lines until we find the first barcode entry
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                
                // Skip empty lines and header separator lines
                if (line.isEmpty() || line.startsWith("---------") || 
                    line.startsWith("Capture file:") || line.startsWith("Created the:")) {
                    continue;
                }
                
                // Parse barcode entry fields
                if (line.startsWith("Value:")) {
                    currentValue = line.substring("Value:".length()).trim();
                } else if (line.startsWith("Symbology:")) {
                    currentSymbology = line.substring("Symbology:".length()).trim();
                } else if (line.startsWith("Quantity:")) {
                    try {
                        currentQuantity = Integer.parseInt(line.substring("Quantity:".length()).trim());
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "Invalid quantity format: " + line);
                        currentQuantity = 1; // Default to 1 if parsing fails
                    }
                } else if (line.startsWith("Capture Date:")) {
                    String dateString = line.substring("Capture Date:".length()).trim();
                    currentDate = parseDateFromString(dateString);
                    
                    // We've reached the end of a barcode entry, process it
                    if (currentValue != null && !currentValue.isEmpty()) {
                        // Add new barcode
                        loadedData.barcodeValues.put(barcodeUniqueIndex, currentValue);
                        // Add to quantity map
                        loadedData.barcodeQuantityMap.put(barcodeUniqueIndex,
                            loadedData.barcodeQuantityMap.getOrDefault(currentValue, 0) + 
                            (currentQuantity != null ? currentQuantity : 1));
                        
                        // Add to symbology map
                        if (currentSymbology != null) {
                            EBarcodesSymbologies symbology = EBarcodesSymbologies.fromName(currentSymbology);
                            loadedData.barcodeSymbologyMap.put(barcodeUniqueIndex, symbology.getIntValue());
                        }
                        
                        // Add to date map (use the most recent date if barcode appears multiple times)
                        if (currentDate != null) {
                            Date existingDate = loadedData.barcodeDateMap.get(currentValue);
                            if (existingDate == null || currentDate.after(existingDate)) {
                                loadedData.barcodeDateMap.put(barcodeUniqueIndex, currentDate);
                            }
                        }
                    }
                    
                    // Reset for next barcode entry
                    currentValue = null;
                    currentSymbology = null;
                    currentQuantity = null;
                    currentDate = null;

                    // Increase barcode unique index
                    barcodeUniqueIndex++;
                }
            }
            
            Log.d(TAG, "Successfully loaded " + loadedData.barcodeQuantityMap.size() + 
                  " barcodes from file: " + sessionFile.getName());
            
        } catch (IOException e) {
            Log.e(TAG, "Error reading session file: " + sessionFile.getPath(), e);
            Toast.makeText(context, "Error reading session file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return loadedData;
    }
    
    /**
     * Helper method to parse date string from the TXT file format
     * Handles the format: "Tuesday, January 21, 2025 14:30:45"
     */
    private static Date parseDateFromString(String dateString) {
        try {
            // Try the full format first (as saved by saveDataTXT)
            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault());
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            
            // Split date and time parts
            String[] parts = dateString.split(" ");
            if (parts.length >= 4) {
                // Reconstruct date part (everything except the last part which should be time)
                StringBuilder datePartBuilder = new StringBuilder();
                for (int i = 0; i < parts.length - 1; i++) {
                    if (i > 0) datePartBuilder.append(" ");
                    datePartBuilder.append(parts[i]);
                }
                String datePart = datePartBuilder.toString();
                String timePart = parts[parts.length - 1];
                
                try {
                    Date datePortion = dateFormat.parse(datePart);
                    Date timePortion = sdf.parse(timePart);
                    
                    // Combine date and time
                    return new Date(datePortion.getTime() + 
                                   (timePortion.getTime() % (24 * 60 * 60 * 1000)));
                } catch (ParseException e) {
                    Log.w(TAG, "Could not parse date with full format, trying alternative formats");
                }
            }
            
            // Fallback: try common date formats
            String[] formats = {
                "EEEE, MMMM dd, yyyy HH:mm:ss",
                "MMM dd, yyyy HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "dd/MM/yyyy HH:mm:ss"
            };
            
            for (String format : formats) {
                try {
                    SimpleDateFormat fallbackSdf = new SimpleDateFormat(format, Locale.getDefault());
                    return fallbackSdf.parse(dateString);
                } catch (ParseException e) {
                    // Continue to next format
                }
            }
            
        } catch (Exception e) {
            Log.w(TAG, "Error parsing date: " + dateString, e);
        }
        
        // If all else fails, return current date
        Log.w(TAG, "Could not parse date, using current date: " + dateString);
        return new Date();
    }

    public static boolean saveDataTXT(Context context, File targetFile, Map<Integer, String> barcodeValueMap, Map<Integer, Integer> barcodeQuantityMap, Map<Integer, Integer> barcodeSymbologyMap, Map<Integer, Date> barcodeDateMap)
	{
		try
		{
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        Date currentDate = new Date();


            if(targetFile.length() == 0)
            {
                String currentDateString = dateFormat.format(currentDate) + " " + sdf.format(currentDate);
                FileWriter headerFileWriter = new FileWriter(targetFile, true);
                headerFileWriter.append("-----------------------------------------\n");
                headerFileWriter.append("Capture file:" + targetFile.getName() + "\n");
                headerFileWriter.append("Created the:" + currentDateString + "\n");
                headerFileWriter.append("-----------------------------------------\n");
                headerFileWriter.close();
            }

        // Append data to the file
        FileWriter fileWriter = new FileWriter(targetFile, true);

        for (Map.Entry<Integer, String> entry : barcodeValueMap.entrySet()) {
            Integer barcodeUniqueID = entry.getKey();
            String value = entry.getValue();
            if(value.isEmpty())
                continue;
            int quantity = barcodeQuantityMap.getOrDefault(barcodeUniqueID, 1);
            int symbology = barcodeSymbologyMap.getOrDefault(barcodeUniqueID, EBarcodesSymbologies.UNKNOWN.getIntValue()); // Get symbology, default to 0 if not found (shouldn't happen)
            String barcodeDateString = dateFormat.format(barcodeDateMap.getOrDefault(barcodeUniqueID, currentDate)) + " " + sdf.format(barcodeDateMap.getOrDefault(barcodeUniqueID, currentDate));
            EBarcodesSymbologies symbologyEnum = EBarcodesSymbologies.fromInt(symbology);
            String data = "Value:" + value + "\nSymbology:" + symbologyEnum.getName() + "\nQuantity:" + quantity + "\nCapture Date:" + barcodeDateString + "\n-----------------------------------------\n";
            fileWriter.append(data);
        }

        fileWriter.close();

        Toast.makeText(context, context.getString(com.zebra.ai_multibarcodes_capture.R.string.file_saved_at, targetFile.getPath()), Toast.LENGTH_SHORT).show();
        return true;
    } catch (IOException e) {
        Toast.makeText(context, context.getString(com.zebra.ai_multibarcodes_capture.R.string.error_saving_file, targetFile.getPath()), Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error saving file:" + targetFile.getPath());
        e.printStackTrace();
        return false;
    }
    }

    public static loadedData loadDataCSV(Context context, File sessionFile)
    {
        loadedData loadedData = new loadedData();
        loadedData.barcodeQuantityMap = new HashMap<>();
        loadedData.barcodeSymbologyMap = new HashMap<>();
        loadedData.barcodeDateMap = new HashMap<>();
        loadedData.barcodeValues = new HashMap<>();
        Integer barcodeUniqueIndex = 0;

        if (!sessionFile.exists() || sessionFile.length() == 0) {
            Log.w(TAG, "Session file does not exist or is empty: " + sessionFile.getPath());
            return loadedData;
        }

        try (Scanner scanner = new Scanner(sessionFile)) {
            boolean headerSkipped = false;
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                
                // Skip empty lines
                if (line.isEmpty()) {
                    continue;
                }
                
                // Skip header line (Date;Symbology;Data;Quantitiy)
                if (!headerSkipped) {
                    if (line.startsWith("Date;Symbology;Data;")) {
                        headerSkipped = true;
                        continue;
                    }
                    // If first line is not header, assume it's data and process it
                    headerSkipped = true;
                }
                
                // Parse CSV line: Time;Symbology;Data;Quantity
                String[] parts = line.split(";");
                if (parts.length >= 4) {
                    try {
                        String timeString = parts[0].trim();
                        String symbologyName = parts[1].trim();
                        String barcodeValue = parts[2].trim();
                        String quantityString = parts[3].trim();
                        
                        // Skip empty barcode values
                        if (barcodeValue.isEmpty()) {
                            continue;
                        }

                        // Insert new barcode
                        loadedData.barcodeValues.put(barcodeUniqueIndex, barcodeValue);

                        // Parse quantity
                        int quantity = 1;
                        try {
                            quantity = Integer.parseInt(quantityString);
                        } catch (NumberFormatException e) {
                            Log.w(TAG, "Invalid quantity format in CSV: " + quantityString + ", using default 1");
                        }
                        
                        // Add to quantity map (accumulate if barcode already exists)
                        loadedData.barcodeQuantityMap.put(barcodeUniqueIndex,
                            loadedData.barcodeQuantityMap.getOrDefault(barcodeValue, 0) + quantity);
                        
                        // Add to symbology map
                        if (!symbologyName.isEmpty()) {
                            EBarcodesSymbologies symbology = EBarcodesSymbologies.fromName(symbologyName);
                            loadedData.barcodeSymbologyMap.put(barcodeUniqueIndex, symbology.getIntValue());
                        }
                        
                        // Parse and add to date map
                        Date currentDate = parseDateFromTimeString(timeString);
                        if (currentDate != null) {
                            // Use the most recent date if barcode appears multiple times
                            Date existingDate = loadedData.barcodeDateMap.get(barcodeValue);
                            if (existingDate == null || currentDate.after(existingDate)) {
                                loadedData.barcodeDateMap.put(barcodeUniqueIndex, currentDate);
                            }
                        }
                        
                    } catch (Exception e) {
                        Log.w(TAG, "Error parsing CSV line: " + line, e);
                        // Continue processing other lines
                    }
                } else {
                    Log.w(TAG, "Invalid CSV line format (expected 4 parts): " + line);
                }
                // Increment unique index
                barcodeUniqueIndex++;
            }
            
            Log.d(TAG, "Successfully loaded " + loadedData.barcodeQuantityMap.size() + 
                  " barcodes from CSV file: " + sessionFile.getName());
            
        } catch (IOException e) {
            Log.e(TAG, "Error reading CSV session file: " + sessionFile.getPath(), e);
            Toast.makeText(context, "Error reading CSV session file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return loadedData;
    }
    
    /**
     * Helper method to parse time string from CSV format (HH:mm:ss)
     * Creates a Date object with today's date and the specified time
     */
    private static Date parseDateFromTimeString(String timeString) {
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            Date timeOnly = timeFormat.parse(timeString);
            
            // Create a date with today's date and the parsed time
            Date today = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String todayDateString = dateFormat.format(today);
            
            SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return fullFormat.parse(todayDateString + " " + timeString);
            
        } catch (ParseException e) {
            Log.w(TAG, "Could not parse time string: " + timeString, e);
            return new Date(); // Return current date/time if parsing fails
        }
    }

    public static boolean saveDataCSV(Context context, File dataFile, Map<Integer, String> barcodeValueMap, Map<Integer, Integer> barcodeQuantityMap, Map<Integer, Integer> barcodeSymbologyMap, Map<Integer, Date> barcodeDateMap) {
        try {
            // Create the file if it doesn't exist
            if (!dataFile.exists() || (dataFile.exists() && dataFile.length() == 0)) {
                dataFile.createNewFile();
                // Append data to the file
                FileWriter fileWriter = new FileWriter(dataFile, true); // Append mode
                fileWriter.append("Date;Symbology;Data;Quantitiy" + "\n");
                fileWriter.close();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            Date currentDate = new Date();
            // Append data to the file
            FileWriter fileWriter = new FileWriter(dataFile, true); // Append mode

            for (Map.Entry<Integer, String> entry : barcodeValueMap.entrySet()) {
                Integer barcodeUniqueID = entry.getKey();
                String value = entry.getValue();
                if(value.isEmpty())
                    continue;
                int quantity = barcodeQuantityMap.getOrDefault(barcodeUniqueID, 1);
                int symbology = barcodeSymbologyMap.getOrDefault(barcodeUniqueID, EBarcodesSymbologies.UNKNOWN.getIntValue()); // Get symbology, default to 0 if not found (shouldn't happen)
                String currentTime = sdf.format(barcodeDateMap.getOrDefault(barcodeUniqueID, currentDate));
                EBarcodesSymbologies symbologyEnum = EBarcodesSymbologies.fromInt(symbology);
                String data = currentTime + ";" + symbologyEnum.getName() + ";" + value + ";" + quantity + "\n";
                fileWriter.append(data);
            }
            fileWriter.close();
            Toast.makeText(context, context.getString(com.zebra.ai_multibarcodes_capture.R.string.file_saved_at, dataFile.getPath()), Toast.LENGTH_SHORT).show();
            return true;
        } catch (IOException e) {
            Toast.makeText(context, context.getString(com.zebra.ai_multibarcodes_capture.R.string.error_saving_file, dataFile.getPath()), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error saving file:" + dataFile.getPath());
            e.printStackTrace();
            return false;
        }
    }

    public static loadedData loadDataXSLX(Context context, File sessionFile)
    {
        loadedData loadedData = new loadedData();
        loadedData.barcodeQuantityMap = new HashMap<>();
        loadedData.barcodeSymbologyMap = new HashMap<>();
        loadedData.barcodeDateMap = new HashMap<>();
        loadedData.barcodeValues = new HashMap<>();
        Integer barcodeUniqueIndex = 0;

        if (!sessionFile.exists() || sessionFile.length() == 0) {
            Log.w(TAG, "Session file does not exist or is empty: " + sessionFile.getPath());
            return loadedData;
        }

        Workbook workbook = null;
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(sessionFile);
            workbook = new XSSFWorkbook(fis);
            
            // Get the first sheet (should be "Data" sheet)
            Sheet sheet = workbook.getSheetAt(0);
            
            // Iterate through rows, starting from row 1 (skip header row 0)
            int lastRowNum = sheet.getLastRowNum();
            int processedRows = 0;
            
            for (int rowIndex = 1; rowIndex <= lastRowNum; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue; // Skip empty rows
                }
                
                try {
                    // Read cells: Date, Symbology, Data, Quantity (columns 0, 1, 2, 3)
                    Cell timeCell = row.getCell(0);
                    Cell symbologyCell = row.getCell(1);
                    Cell dataCell = row.getCell(2);
                    Cell quantityCell = row.getCell(3);
                    
                    // Extract barcode value (Data column)
                    String barcodeValue = getCellValueAsString(dataCell);
                    if (barcodeValue == null || barcodeValue.trim().isEmpty()) {
                        continue; // Skip rows with empty barcode values
                    }
                    barcodeValue = barcodeValue.trim();

                    // Insert new barcode
                    loadedData.barcodeValues.put(barcodeUniqueIndex, barcodeValue);
                    
                    // Extract quantity
                    int quantity = 1; // Default quantity
                    if (quantityCell != null) {
                        try {
                            if (quantityCell.getCellType() == CellType.NUMERIC) {
                                quantity = (int) quantityCell.getNumericCellValue();
                            } else {
                                String quantityStr = getCellValueAsString(quantityCell);
                                if (quantityStr != null && !quantityStr.trim().isEmpty()) {
                                    quantity = Integer.parseInt(quantityStr.trim());
                                }
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Invalid quantity in Excel row " + rowIndex + ", using default 1", e);
                        }
                    }
                    
                    // Add to quantity map (accumulate if barcode already exists)
                    loadedData.barcodeQuantityMap.put(barcodeUniqueIndex,
                        loadedData.barcodeQuantityMap.getOrDefault(barcodeValue, 0) + quantity);
                    
                    // Extract symbology
                    String symbologyName = getCellValueAsString(symbologyCell);
                    if (symbologyName != null && !symbologyName.trim().isEmpty()) {
                        EBarcodesSymbologies symbology = EBarcodesSymbologies.fromName(symbologyName.trim());
                        loadedData.barcodeSymbologyMap.put(barcodeUniqueIndex, symbology.getIntValue());
                    }
                    
                    // Extract time/date
                    Date currentDate = extractDateFromCell(timeCell);
                    if (currentDate != null) {
                        // Use the most recent date if barcode appears multiple times
                        Date existingDate = loadedData.barcodeDateMap.get(barcodeValue);
                        if (existingDate == null || currentDate.after(existingDate)) {
                            loadedData.barcodeDateMap.put(barcodeUniqueIndex, currentDate);
                        }
                    }
                    
                    processedRows++;
                    barcodeUniqueIndex++;
                    
                } catch (Exception e) {
                    Log.w(TAG, "Error processing Excel row " + rowIndex, e);
                    // Continue processing other rows
                }
            }
            
            Log.d(TAG, "Successfully loaded " + loadedData.barcodeQuantityMap.size() + 
                  " unique barcodes from " + processedRows + " rows in Excel file: " + sessionFile.getName());
            
        } catch (Exception e) {
            Log.e(TAG, "Error reading Excel session file: " + sessionFile.getPath(), e);
            Toast.makeText(context, "Error reading Excel session file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            // Close resources
            try {
                if (workbook != null) workbook.close();
                if (fis != null) fis.close();
            } catch (IOException e) {
                Log.w(TAG, "Error closing Excel file resources", e);
            }
        }

        return loadedData;
    }
    
    /**
     * Helper method to extract string value from Excel cell
     */
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // Handle date/time formatted cells
                    Date date = cell.getDateCellValue();
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                    return sdf.format(date);
                } else {
                    // Handle numeric cells
                    double numValue = cell.getNumericCellValue();
                    if (numValue == Math.floor(numValue)) {
                        return String.valueOf((int) numValue);
                    } else {
                        return String.valueOf(numValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                // Evaluate formula and get the result
                try {
                    return cell.getRichStringCellValue().getString();
                } catch (Exception e) {
                    Log.w(TAG, "Could not evaluate formula cell", e);
                    return cell.getCellFormula();
                }
            case BLANK:
            case _NONE:
            default:
                return null;
        }
    }
    
    /**
     * Helper method to extract Date from Excel cell (handles time-formatted cells)
     */
    private static Date extractDateFromCell(Cell cell) {
        if (cell == null) {
            return new Date(); // Return current date if cell is null
        }
        
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                // Cell contains a date/time value
                Date cellDate = cell.getDateCellValue();
                
                // Since saveDataXSLX only saves time (HH:mm:ss), we need to combine with today's date
                // Extract just the time portion and combine with today's date
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                String timeString = timeFormat.format(cellDate);
                
                return parseDateFromTimeString(timeString);
                
            } else {
                // Try to parse as string
                String cellValue = getCellValueAsString(cell);
                if (cellValue != null && !cellValue.trim().isEmpty()) {
                    return parseDateFromTimeString(cellValue.trim());
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Error extracting date from Excel cell", e);
        }
        
        return new Date(); // Return current date if parsing fails
    }

    public static boolean saveDataXSLX(Context context, File dataFile, Map<Integer, String> barcodeValueMap, Map<Integer, Integer> barcodeQuantityMap, Map<Integer, Integer> barcodeSymbologyMap, Map<Integer, Date> barcodeDateMap) {
        Workbook workbook = null;
        FileInputStream fis = null;
        FileOutputStream fos = null;

        Date currentDate = new Date();

        try {
            // Check if file exists, if not create it
            if (!dataFile.exists() || (dataFile.exists() && dataFile.length() == 0)) {
                workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("Data");

                // Create header row
                Row headerRow = sheet.createRow(0);
                String[] headers = {"Date", "Symbology", "Data", "Quantity"};
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

            for (Map.Entry<Integer, String> entry : barcodeValueMap.entrySet()) {
                Integer barcodeUniqueIdentifier = entry.getKey();
                String value = entry.getValue();
                if(value.isEmpty())
                    continue;
                int quantity = barcodeQuantityMap.getOrDefault(barcodeUniqueIdentifier, 1);
                int symbology = barcodeSymbologyMap.getOrDefault(barcodeUniqueIdentifier, EBarcodesSymbologies.UNKNOWN.getIntValue()); // Get symbology, default to 0 if not found (shouldn't happen)

                // Get the last row number and create a new row
                int lastRowNum = sheet.getLastRowNum();
                Row row = sheet.createRow(lastRowNum + 1);

                // Create cell styles
                CreationHelper createHelper = workbook.getCreationHelper();

                // Time cell style (for HHmmss format)
                CellStyle timeStyle = workbook.createCellStyle();
                timeStyle.setDataFormat(createHelper.createDataFormat().getFormat("HH:mm:ss"));

                // Plain text style for column 2 (symbology)
                CellStyle plainTextStyle = workbook.createCellStyle();
                plainTextStyle.setDataFormat(createHelper.createDataFormat().getFormat("@"));

                // Integer Style for quantity and symbology
                CellStyle integerStyle = workbook.createCellStyle();
                integerStyle.setDataFormat(createHelper.createDataFormat().getFormat("0"));

                // Get current time
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                String currentTime = sdf.format(barcodeDateMap.getOrDefault(barcodeUniqueIdentifier, currentDate));

                // Time cell as numeric with time format
                Cell timeCell = row.createCell(0);
                timeCell.setCellValue(currentTime);
                timeCell.setCellStyle(timeStyle);

                Cell symbologyCell = row.createCell(1);
                EBarcodesSymbologies symbologyEnum = EBarcodesSymbologies.fromInt(symbology);
                symbologyCell.setCellValue(symbologyEnum.getName());
                symbologyCell.setCellStyle(plainTextStyle);

                // String cell style
                Cell dataCell = row.createCell(2);
                dataCell.setCellValue(value);
                dataCell.setCellStyle(plainTextStyle);

                // Integer cell style
                Cell quantityCell = row.createCell(3);
                quantityCell.setCellValue(quantity);
                quantityCell.setCellStyle(integerStyle);
            }

            // Write to file
            fos = new FileOutputStream(dataFile);
            workbook.write(fos);
        } catch (Exception e) {
            Log.e("ExcelAppend", "Error appending to Excel file", e);
            e.printStackTrace();
            return false;
        } finally {
            // Close resources
            try {
                if (workbook != null) workbook.close();
                if (fos != null) fos.close();
                Toast.makeText(context, context.getString(com.zebra.ai_multibarcodes_capture.R.string.file_saved_at, dataFile.getPath()), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e("ExcelAppend", "Error closing resources", e);
            }

        }
        return true;
    }
}
