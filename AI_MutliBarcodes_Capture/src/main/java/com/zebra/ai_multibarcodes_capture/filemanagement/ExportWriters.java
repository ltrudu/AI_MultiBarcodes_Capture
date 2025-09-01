package com.zebra.ai_multibarcodes_capture.filemanagement;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class ExportWriters {

    private static final String TAG = "ExportWriters";

    public static boolean saveData(Context context, String captureFilePath, Map<String, Integer> barcodeQuantityMap, Map<String, Integer> barcodeSymbologyMap, Date currentDate)
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
                return saveDataCSV(context, targetFile, barcodeQuantityMap,barcodeSymbologyMap, currentDate);
            case ".xlsx":
                return saveDataXSLX(context, targetFile, barcodeQuantityMap, barcodeSymbologyMap, currentDate);
            default:
                return saveDataTXT(context, targetFile, barcodeQuantityMap, barcodeSymbologyMap, currentDate);
        }
    }

    public static boolean saveDataTXT(Context context, File targetFile, Map<String, Integer> barcodeQuantityMap, Map<String, Integer> barcodeSymbologyMap, Date currentDate)
	{
		try
		{
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String currentDateString = dateFormat.format(currentDate) + " " + sdf.format(currentDate);

        if(targetFile.length() == 0) {
            FileWriter headerFileWriter = new FileWriter(targetFile, true);
            headerFileWriter.append("-----------------------------------------\n");
            headerFileWriter.append("Capture file:" + targetFile.getName() + "\n");
            headerFileWriter.append("Created the:" + currentDateString + "\n");
            headerFileWriter.append("-----------------------------------------\n");
            headerFileWriter.close();
        }

        // Append data to the file
        FileWriter fileWriter = new FileWriter(targetFile, true);

        for (Map.Entry<String, Integer> entry : barcodeQuantityMap.entrySet()) {
            String value = entry.getKey();
            if(value.isEmpty())
                continue;
            int quantity = entry.getValue();
            int symbology = barcodeSymbologyMap.getOrDefault(value, 0); // Get symbology, default to 0 if not found (shouldn't happen)
            String data = "Value:" + value + "\nSymbology:" + symbology + "\nQuantity:" + quantity + "\nCapture Date:" + currentDateString + "\n-----------------------------------------\n";
            fileWriter.append(data);
        }

        fileWriter.close();

        Toast.makeText(context, context.getString(com.zebra.ai_multibarcodes_capture.R.string.file_saved_at, targetFile.getPath()), Toast.LENGTH_LONG).show();
        return true;
    } catch (IOException e) {
        Toast.makeText(context, context.getString(com.zebra.ai_multibarcodes_capture.R.string.error_saving_file, targetFile.getPath()), Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error saving file:" + targetFile.getPath());
        e.printStackTrace();
        return false;
    }
    }

    public static boolean saveDataCSV(Context context, File dataFile, Map<String, Integer> barcodeQuantityMap, Map<String, Integer> barcodeSymbologyMap, Date nowDate) {
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
            String currentTime = sdf.format(nowDate);
            // Append data to the file
            FileWriter fileWriter = new FileWriter(dataFile, true); // Append mode

            for (Map.Entry<String, Integer> entry : barcodeQuantityMap.entrySet()) {
                String value = entry.getKey();
                if(value.isEmpty())
                    continue;
                int quantity = entry.getValue();
                int symbology = barcodeSymbologyMap.getOrDefault(value, 0); // Get symbology, default to 0 if not found (shouldn't happen)
                String data = currentTime + ";" + symbology + ";" + value + ";" + quantity + "\n";
                fileWriter.append(data);
            }
            fileWriter.close();
            Toast.makeText(context, context.getString(com.zebra.ai_multibarcodes_capture.R.string.file_saved_at, dataFile.getPath()), Toast.LENGTH_LONG).show();
            return true;
        } catch (IOException e) {
            Toast.makeText(context, context.getString(com.zebra.ai_multibarcodes_capture.R.string.error_saving_file, dataFile.getPath()), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Error saving file:" + dataFile.getPath());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean saveDataXSLX(Context context, File dataFile, Map<String, Integer> barcodeQuantityMap, Map<String, Integer> barcodeSymbologyMap, Date nowDate) {
        Workbook workbook = null;
        FileInputStream fis = null;
        FileOutputStream fos = null;

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

            for (Map.Entry<String, Integer> entry : barcodeQuantityMap.entrySet()) {
                String value = entry.getKey();
                if(value.isEmpty())
                    continue;
                int quantity = entry.getValue();
                int symbology = barcodeSymbologyMap.getOrDefault(value, 0); // Get symbology, default to 0 if not found (shouldn't happen)

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
                String currentTime = sdf.format(nowDate);

                // Time cell as numeric with time format
                Cell timeCell = row.createCell(0);
                timeCell.setCellValue(currentTime);
                timeCell.setCellStyle(timeStyle);

                Cell symbologyCell = row.createCell(1);
                symbologyCell.setCellValue(symbology);
                symbologyCell.setCellStyle(integerStyle);

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
                Toast.makeText(context, context.getString(com.zebra.ai_multibarcodes_capture.R.string.file_saved_at, dataFile.getPath()), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Log.e("ExcelAppend", "Error closing resources", e);
            }

        }
        return true;
    }
}
