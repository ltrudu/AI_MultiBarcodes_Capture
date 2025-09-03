package com.zebra.ai_multibarcodes_capture.filemanagement;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.zebra.ai_multibarcodes_capture.helpers.Constants;
import com.zebra.ai_multibarcodes_capture.helpers.LogUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FileUtil {
    private static final String TAG = "FileUtil";
    public static File getLatestModifiedFile(String directoryPath, String extension) {
        File directory = new File(directoryPath);
        List<File> files = new ArrayList<>();
        if (directory.exists() && directory.isDirectory()) {
            File[] fileArray = directory.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    if(s.contains(extension))
                        return true;
                    return false;
                }
            });
            if (fileArray != null) {
                Collections.addAll(files, fileArray);
            }
        }
        return getLatestModifiedFile(files);
    }

    private static File getLatestModifiedFile(List<File> files) {
        if (files.isEmpty()) {
            return null;
        }
        Collections.sort(files, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        return files.get(0);
    }

    public static String getFileNameWithoutExtension(File file) {
        if (file == null || file.getName() == null) {
            return null;
        }

        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex == -1) {
            return fileName; // No extension found
        } else {
            return fileName.substring(0, dotIndex); // Return name without extension
        }
    }

    public static String getFileExtension(File file) {
        if (file == null || file.getName() == null) {
            return null;
        }

        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return ""; // No extension found or empty extension
        } else {
            return fileName.substring(dotIndex); // Return extension with the .
        }
    }

    public static String readFileToString(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line);
                content.append(System.lineSeparator());
            }
        }
        return content.toString();
    }

    public static void removeLastLine(File file) throws IOException {
        List<String> lines = new ArrayList<>();

        // Read file into a list of strings
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        if (!lines.isEmpty()) {
            // Remove the last line
            lines.remove(lines.size() - 1);
        }

        // Write the modified content back to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    public static void deleteAllFiles(File fileToDelete)
    {
        if (fileToDelete.isDirectory()) {
            for (File file : fileToDelete.listFiles()) {
                if (file.isFile()) {
                    file.delete();
                }
                else if(file.isDirectory())
                    deleteAllFiles(file);
            }
        }
        else
            fileToDelete.delete();
    }

    public static boolean deleteFolderRecursively(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!deleteFolderRecursively(file)) {
                        return false;
                    }
                }
            }
        }
        return folder.delete();
    }

    public static File copyToFolder(File sourceFile, File destinationFolder)
    {
        try {
            File destinationFile = new File(destinationFolder, sourceFile.getName());
            if (destinationFile.exists())
                destinationFile.delete();

            try (InputStream in = new FileInputStream(sourceFile);
                 OutputStream out = new FileOutputStream(destinationFile)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }

                System.out.println("File copied successfully!");

            } catch (IOException e) {
                e.printStackTrace();
            }
            return destinationFile;
        }
        catch(Exception e)
        {
            return null;
        }
    }

    public static File copyToCacheFolder(Context context, File source)
    {
        File cacheDir = context.getExternalCacheDir();
        File destCacheDir = new File(cacheDir, Constants.PROVIDER_CACHE_FOLDER);
        if(destCacheDir.exists() == false)
        {
            LogUtils.d(TAG, "Creating directory: " + destCacheDir.getAbsolutePath());
            destCacheDir.mkdirs();
        }
        File cacheFile = copyToFolder(source, destCacheDir);
        return cacheFile;
    }

    public static String createNewFileName(String prefix)
    {
        Date nowDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH_mm_ss");
        String currentDateandTime = sdf.format(nowDate);
        String newFileName = prefix + currentDateandTime;
        return newFileName;
    }

    public static String createNewFile(File folder, String prefix, EExportMode exportMode) throws IOException {
        String fileName = createNewFileName(prefix);
        File newFile = new File(folder, fileName + exportMode.getExtension());
        newFile.createNewFile();
        return fileName;
    }

    public static String createNewFileAndReturnFullPath(File folder, String prefix, EExportMode exportMode) throws IOException {
        String fileName = createNewFileName(prefix);
        File newFile = new File(folder, fileName + exportMode.getExtension());
        newFile.createNewFile();
        return newFile.getPath();
    }

    public static String getTodayDateString()
    {
        Date nowDate = new Date();
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        String currentDate = dateFormat.format(nowDate);
        return currentDate;
    }

    public static File getBaseFolder()
    {
        File targetFolder = null;
        try {
            targetFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), Constants.FILE_TARGET_FOLDER);
            if (targetFolder.exists() == false) {
                LogUtils.d(TAG, "Creating directory: " + targetFolder.getAbsolutePath());
                targetFolder.mkdirs();
            }
            return targetFolder;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static File getTodayFolder(boolean create)
    {
        File targetFolder = null;
        File dateFolder = null;
        try {
            targetFolder = getBaseFolder();
            if(targetFolder != null) {
                dateFolder = new File(targetFolder, getTodayDateString());
                if (create && dateFolder.exists() == false) {
                    LogUtils.d(TAG, "Creating directory: " + dateFolder.getAbsolutePath());
                    dateFolder.mkdirs();
                }
            }
            else
            {
                return null;
            }
            return dateFolder;
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
