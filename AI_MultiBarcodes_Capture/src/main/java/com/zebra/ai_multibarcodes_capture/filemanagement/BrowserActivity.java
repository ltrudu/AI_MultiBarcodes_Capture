package com.zebra.ai_multibarcodes_capture.filemanagement;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.ai_multibarcodes_capture.R;
import com.zebra.ai_multibarcodes_capture.helpers.Constants;
import com.zebra.ai_multibarcodes_capture.helpers.BaseActivity;
import com.zebra.ai_multibarcodes_capture.helpers.LogUtils;
import com.zebra.ai_multibarcodes_capture.helpers.ThemeHelpers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

import static com.zebra.ai_multibarcodes_capture.helpers.Constants.*;

public class BrowserActivity extends BaseActivity {

    private FileAdapter fileAdapter;
    private ArrayList<File> filesList;
    private ListView listView;
    private Button btnSelectOneFile;
    private Button btnReload;
    private Button btnClose;
    private Button btnShare;
    private View viewOverlay;

    ConstraintLayout clFileList;
    ConstraintLayout clPopup;
    Button btPopupYes;
    Button btPopupNo;
    Toolbar tbManage;

    private File baseFolder;
    private File currentFolder;
    private int fileCounter = 0;
    private String filePrefix = Constants.FILE_DEFAULT_PREFIX;
    private String fileExtension = EExportMode.TEXT.getExtension();

    private boolean bCanMultiSelect = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply theme before setting content view
        ThemeHelpers.applyTheme(this);

        setContentView(R.layout.activity_browser);

        ThemeHelpers.configureSystemBars(this, R.id.cl_activity_browser);
        ThemeHelpers.applyCustomFont(this);

        Intent intent = getIntent();
        String folderPath = intent.getStringExtra(Constants.FILEBROWSER_EXTRA_FOLDER_PATH);

        if (folderPath != null) {
            baseFolder = new File(folderPath);
            currentFolder = baseFolder;

            // Ensure the directory exists
            if (!baseFolder.exists()) {
                baseFolder.mkdirs();
            }

            // Use the folder path as needed
            LogUtils.d("Folder Path", "The folder path is: " + baseFolder.getPath());
        } else {
            LogUtils.e("Folder Path", getString(R.string.folder_path_missing));
            Toast.makeText(this, getString(R.string.folder_path_missing_toast), Toast.LENGTH_LONG).show();
            finish();
        }

        listView = findViewById(R.id.lvFiles);
        clPopup = findViewById(R.id.cl_question);
        clFileList = findViewById(R.id.cl_browser);
        clPopup.setVisibility(View.GONE);
        clFileList.setVisibility(View.VISIBLE);
        btPopupNo = findViewById(R.id.btNo);
        tbManage = findViewById(R.id.tbManage);
        viewOverlay = findViewById(R.id.viewOverlay);

        viewOverlay.setVisibility(View.GONE);

        setSupportActionBar(tbManage);
        tbManage.setNavigationIcon(R.drawable.ic_menu_white);
        tbManage.setNavigationOnClickListener(view -> {
            PopupMenu popup = new PopupMenu(BrowserActivity.this, view);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.browser_menu, popup.getMenu());

            // Show/hide menu items based on selection state
            boolean hasSelection = fileAdapter != null && fileAdapter.getSelectedPosition() != -1;
            popup.getMenu().findItem(R.id.action_rename).setVisible(hasSelection);
            popup.getMenu().findItem(R.id.action_delete).setVisible(hasSelection);

            // Force PopupMenu to show icons
            try {
                java.lang.reflect.Field mFieldPopup = popup.getClass().getDeclaredField("mPopup");
                mFieldPopup.setAccessible(true);
                Object menuPopupHelper = mFieldPopup.get(popup);
                java.lang.reflect.Method setForceIcons = menuPopupHelper.getClass().getDeclaredMethod("setForceShowIcon", boolean.class);
                setForceIcons.invoke(menuPopupHelper, true);
            } catch (Exception e) {
                LogUtils.e("BrowserActivity", "Failed to force show icons in popup menu", e);
            }

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_new_file) {
                    createNewFile();
                    return true;
                } else if (id == R.id.action_new_folder) {
                    createNewFolder();
                    return true;
                } else if (id == R.id.action_delete) {
                    deleteSelectedFileOrFolder();
                    return true;
                } else if (id == R.id.action_rename) {
                    renameSelectedFileOrFolder();
                    return true;
                }
                return false;
            });
            popup.show();
        });


        btPopupYes = findViewById(R.id.btYes);

        fileExtension = intent.getStringExtra(Constants.FILEBROWSER_EXTRA_EXTENSION);
        if(fileExtension == null)
            fileExtension = Constants.FILE_DEFAULT_EXTENSION;

        filePrefix = intent.getStringExtra(Constants.FILEBROWSER_EXTRA_PREFIX);
        if(filePrefix == null)
            filePrefix = Constants.FILE_DEFAULT_PREFIX;

        bCanMultiSelect = intent.getBooleanExtra(Constants.FILEBROWSER_EXTRA_MULTISELECT, false);

        // Completely disable ListView's built-in selection and click handling
        // All selection and navigation is handled manually in FileAdapter
        listView.setChoiceMode(ListView.CHOICE_MODE_NONE);
        listView.setSelector(android.R.color.transparent); // Remove default selector
        listView.setOnItemClickListener(null); // Remove any item click listeners
        listView.setOnItemLongClickListener(null); // Remove long click listeners
        listView.setOnItemSelectedListener(null); // Remove selection listeners
        listView.setClickable(false); // Disable ListView clicking
        listView.setLongClickable(false); // Disable long clicking
        listView.setItemsCanFocus(false); // Prevent items from getting focus
        listView.setFocusable(false); // Disable ListView focus
        listView.setFocusableInTouchMode(false); // Disable touch mode focus

        btnSelectOneFile = findViewById(R.id.btSelectOneFile);
        btnSelectOneFile.setOnClickListener(v -> finishWithFileSelectArguments());

        btnReload = findViewById(R.id.btReload);
        btnReload.setOnClickListener(v -> getFileList());

        btnClose = findViewById(R.id.btClose);
        btnClose.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            setResult(RESULT_CANCELED, resultIntent);
            finish();
        });

        btnShare = findViewById(R.id.btShare);
        btnShare.setOnClickListener(v -> shareSelectedFile());

        // Initially hide buttons until a file is selected
        updateButtonVisibility(false);
    }

    @Override
    protected void onResume() {
        getFileList();
        super.onResume();
    }

    private void navigateToFolder(File folder) {
        if (folder.getName().equals("..")) {
            // Navigate to parent directory
            currentFolder = currentFolder.getParentFile();
            if (currentFolder == null || !currentFolder.getAbsolutePath().startsWith(baseFolder.getAbsolutePath())) {
                currentFolder = baseFolder;
            }
        } else if (folder.isDirectory()) {
            // Navigate to subdirectory
            currentFolder = folder;
        }
        
        // Clear any ListView selection state that might have occurred
        listView.clearChoices();
        listView.setItemChecked(-1, false);
        
        getFileList();
        
        // Clear again after getting the file list to be extra sure
        listView.clearChoices();
        for (int i = 0; i < listView.getChildCount(); i++) {
            listView.setItemChecked(i, false);
        }
    }

    private void finishWithFileSelectArguments()
    {
        Intent resultIntent = new Intent();
        if(fileAdapter != null && fileAdapter.getSelectedPosition() != -1) {
            File fileName = fileAdapter.getSelectedFile();
            if (fileName != null && fileName.isFile()) {
                resultIntent.putExtra(Constants.FILEBROWSER_RESULT_FILENAME, FileUtil.getFileNameWithoutExtension(fileName));
                resultIntent.putExtra(Constants.FILEBROWSER_RESULT_FILE_EXTENSION, FileUtil.getFileExtension(fileName));
                resultIntent.putExtra(Constants.FILEBROWSER_RESULT_FILEPATH, fileName.getPath());
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, getString(R.string.please_select_file_not_folder), Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            Toast.makeText(this, getString(R.string.please_select_file_or_folder), Toast.LENGTH_LONG).show();
        }
    }

    private void getFileList() {
        File[] files = currentFolder.listFiles();
        if(filesList == null)
            filesList = new ArrayList<>();
        filesList.clear();
        fileCounter = 0;
        
        // Add parent directory (..) if not at base folder
        if (!currentFolder.equals(baseFolder)) {
            File parentDir = new File("..");
            filesList.add(parentDir);
        }
        
        if (files != null) {
            // First add directories
            for (File file : files) {
                if (file.isDirectory()) {
                    filesList.add(file);
                }
            }
            
            // Then add files with the specified extension
            for (File file : files) {
                if (file.isFile() && file.getName().contains(fileExtension)) {
                    filesList.add(file);
                    fileCounter++;
                }
            }
            
            if(fileAdapter == null)
            {
                fileAdapter = new FileAdapter(BrowserActivity.this,filesList );
                // Set the parent ListView for proper selection handling
                fileAdapter.setParentListView(listView);
                // Set up the delete listener
                fileAdapter.setOnFileDeleteListener(new FileAdapter.OnFileDeleteListener() {
                    @Override
                    public void onFileDelete(File file, int position) {
                        if (file.isDirectory()) {
                            deleteFolder(file);
                        } else {
                            deleteFile(file);
                        }
                    }
                });
                // Set up the folder click listener for navigation
                fileAdapter.setOnFolderClickListener(new FileAdapter.OnFolderClickListener() {
                    @Override
                    public void onFolderClick(File folder) {
                        navigateToFolder(folder);
                    }
                });
                // Set up the selection change listener for button visibility
                fileAdapter.setOnSelectionChangeListener(new FileAdapter.OnSelectionChangeListener() {
                    @Override
                    public void onSelectionChanged(boolean hasFileSelected) {
                        updateButtonVisibility(hasFileSelected);
                    }
                });
                listView.setAdapter(fileAdapter);
            }
            // Clear selection when file list changes
            if(fileAdapter != null) {
                fileAdapter.clearSelection();
            }
            fileAdapter.notifyDataSetChanged();
            
            // Force clear ListView selection after adapter update
            listView.post(() -> {
                listView.clearChoices();
                for (int i = 0; i < listView.getChildCount(); i++) {
                    listView.setItemChecked(i, false);
                }
            });
        }
    }

    private void renameSelectedFileOrFolder()
    {
        if(fileAdapter != null && fileAdapter.getSelectedPosition() != -1) {
            final File fileName = fileAdapter.getSelectedFile();
            if (fileName == null || fileName.getName().equals("..")) {
                Toast.makeText(this, getString(R.string.cannot_rename_parent_folder), Toast.LENGTH_LONG).show();
                return;
            }
            if(fileName.isFile())
            {
                renameFile(fileName);
            }
            else
            {
                renameFolder(fileName);
            }
        }
        else
        {
            Toast.makeText(this, getString(R.string.please_select_file_or_folder), Toast.LENGTH_LONG).show();
        }
    }

    private void renameFile(File fileName) {
        final int fileNamePosition = fileAdapter.getSelectedPosition();
        ((TextView)findViewById(R.id.txtTitle)).setText(getString(R.string.rename));
        ((TextView)findViewById(R.id.txtMessage)).setText(getString(R.string.rename_file));
        ((EditText)findViewById(R.id.etData)).setText(FileUtil.getFileNameWithoutExtension(fileName));
        ((TextView)findViewById(R.id.txtDataLabelRight)).setVisibility(View.VISIBLE);
        ((TextView)findViewById(R.id.txtDataLabelRight)).setText(fileExtension);
        btPopupYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newFileName = ((EditText) findViewById(R.id.etData)).getText().toString();
                if (newFileName.isEmpty() == false) {
                    File newFile = new File(currentFolder, newFileName + fileExtension);
                    if(newFile.exists())
                    {
                        Toast.makeText(BrowserActivity.this, getString(R.string.file_already_exists), Toast.LENGTH_LONG).show();
                        viewOverlay.setVisibility(View.GONE);
                        clPopup.setVisibility(View.GONE);
                        fileAdapter.clearSelection();
                        return;
                    }
                    try {
                            filesList.remove(fileNamePosition);
                            fileName.renameTo(newFile);
                            filesList.add(newFile);
                            fileAdapter.notifyDataSetChanged();
                            viewOverlay.setVisibility(View.GONE);
                            clPopup.setVisibility(View.GONE);
                            fileAdapter.clearSelection();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(BrowserActivity.this, getString(R.string.error_renaming_file), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        btPopupNo.setOnClickListener(v -> {
            viewOverlay.setVisibility(View.GONE);
            clPopup.setVisibility(View.GONE);
        });

        viewOverlay.setVisibility(View.VISIBLE);
        clPopup.setVisibility(View.VISIBLE);
    }

   private void renameFolder(File folderName) {
        final int fileNamePosition = fileAdapter.getSelectedPosition();
        ((TextView)findViewById(R.id.txtTitle)).setText(getString(R.string.rename));
        ((TextView)findViewById(R.id.txtMessage)).setText(getString(R.string.rename_folder));
        ((EditText)findViewById(R.id.etData)).setText(folderName.getName());
        ((TextView)findViewById(R.id.txtDataLabelRight)).setVisibility(View.GONE);
        btPopupYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newFolderName = ((EditText) findViewById(R.id.etData)).getText().toString();
                if (newFolderName.isEmpty() == false) {
                    File newFile = new File(currentFolder, newFolderName);
                    if(newFile.exists())
                    {
                        Toast.makeText(BrowserActivity.this, getString(R.string.folder_already_exists), Toast.LENGTH_LONG).show();
                        viewOverlay.setVisibility(View.GONE);
                        clPopup.setVisibility(View.GONE);
                        fileAdapter.clearSelection();
                        return;
                    }
                    try {
                            filesList.remove(fileNamePosition);
                            folderName.renameTo(newFile);
                            filesList.add(newFile);
                            fileAdapter.notifyDataSetChanged();
                            viewOverlay.setVisibility(View.GONE);
                            clPopup.setVisibility(View.GONE);
                            fileAdapter.clearSelection();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(BrowserActivity.this, getString(R.string.error_renaming_file), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        btPopupNo.setOnClickListener(v -> {
            viewOverlay.setVisibility(View.GONE);
            clPopup.setVisibility(View.GONE);
        });

        viewOverlay.setVisibility(View.VISIBLE);
        clPopup.setVisibility(View.VISIBLE);
    }

    private void createNewFolder()
    {
        ((TextView)findViewById(R.id.txtTitle)).setText(getString(R.string.create));
        ((TextView)findViewById(R.id.txtMessage)).setText(getString(R.string.create_new_folder));
        ((EditText)findViewById(R.id.etData)).setText(FileUtil.getTodayFolder(false).getName());
        ((TextView)findViewById(R.id.txtDataLabelRight)).setVisibility(View.GONE);
        btPopupYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newFolder = ((EditText) findViewById(R.id.etData)).getText().toString();
                if (newFolder.isEmpty() == false) {
                    File newFile = new File(currentFolder, newFolder);
                    if(newFile.exists())
                    {
                        Toast.makeText(BrowserActivity.this, getString(R.string.folder_already_exists), Toast.LENGTH_LONG).show();
                        viewOverlay.setVisibility(View.GONE);
                        clPopup.setVisibility(View.GONE);
                        return;
                    }
                    try {
                        if (newFile.mkdir()) {
                            filesList.add(newFile);
                            fileCounter++;
                            fileAdapter.notifyDataSetChanged();
                            viewOverlay.setVisibility(View.GONE);
                            clPopup.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(BrowserActivity.this, getString(R.string.failed_to_create_folder, newFolder), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        btPopupNo.setOnClickListener(v -> {
            viewOverlay.setVisibility(View.GONE);
            clPopup.setVisibility(View.GONE);
        });

        viewOverlay.setVisibility(View.VISIBLE);
        clPopup.setVisibility(View.VISIBLE);
    }

    private void createNewFile() {
        ((TextView)findViewById(R.id.txtTitle)).setText(getString(R.string.create));
        ((TextView)findViewById(R.id.txtMessage)).setText(getString(R.string.create_new_file));
        ((EditText)findViewById(R.id.etData)).setText(FileUtil.createNewFileName(filePrefix));
        ((TextView)findViewById(R.id.txtDataLabelRight)).setVisibility(View.VISIBLE);
        ((TextView)findViewById(R.id.txtDataLabelRight)).setText(fileExtension);
        btPopupYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newFileName = ((EditText) findViewById(R.id.etData)).getText().toString();
                if (newFileName.isEmpty() == false) {
                    File newFile = new File(currentFolder, newFileName + fileExtension);
                    if(newFile.exists())
                    {
                        Toast.makeText(BrowserActivity.this, getString(R.string.file_exists_already), Toast.LENGTH_LONG).show();
                        clFileList.setVisibility(View.VISIBLE);
                        clPopup.setVisibility(View.GONE);
                        return;
                    }
                    try {
                        if (newFile.createNewFile()) {
                            filesList.add(newFile);
                            fileCounter++;
                            fileAdapter.notifyDataSetChanged();
                            
                            // Automatically select the newly created file
                            int newFilePosition = filesList.indexOf(newFile);
                            if (newFilePosition != -1) {
                                listView.setItemChecked(newFilePosition, true);
                                listView.smoothScrollToPosition(newFilePosition);
                            }
                            
                            viewOverlay.setVisibility(View.GONE);
                            clPopup.setVisibility(View.GONE);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(BrowserActivity.this, getString(R.string.failed_to_create_file), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        btPopupNo.setOnClickListener(v -> {
            viewOverlay.setVisibility(View.GONE);
            clPopup.setVisibility(View.GONE);
        });

        viewOverlay.setVisibility(View.VISIBLE);
        clPopup.setVisibility(View.VISIBLE);
    }

    private void deleteSelectedFileOrFolder() {
        if(fileAdapter != null && fileAdapter.getSelectedPosition() != -1) {
            File selectedFile = fileAdapter.getSelectedFile();
            if (selectedFile != null) {
                if (selectedFile.getName().equals("..")) {
                    Toast.makeText(this, getString(R.string.cannot_delete_parent_folder), Toast.LENGTH_LONG).show();
                    return;
                }
                if(selectedFile.isDirectory()) {
                    deleteFolder(selectedFile);
                } else {
                    deleteFile(selectedFile);
                }
            }
        } else {
            Toast.makeText(this, getString(R.string.please_select_file_or_folder), Toast.LENGTH_LONG).show();
        }
    }

    private void deleteFile(File file)
    {
        ((TextView)findViewById(R.id.txtTitle)).setText(getString(R.string.delete_file));
        ((TextView)findViewById(R.id.txtMessage)).setText(getString(R.string.are_you_sure));
        ((EditText)findViewById(R.id.etData)).setText(file.getName());
        ((TextView)findViewById(R.id.txtDataLabelRight)).setVisibility(View.VISIBLE);
        ((TextView)findViewById(R.id.txtDataLabelRight)).setText(fileExtension);
        btPopupYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(file.exists())
                {
                    file.delete();
                    filesList.remove(file);
                    fileAdapter.notifyDataSetChanged();
                }
                viewOverlay.setVisibility(View.GONE);
                clPopup.setVisibility(View.GONE);
            }
        });
        btPopupNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Reset any swiped rows back to normal state
                if (fileAdapter != null) {
                    fileAdapter.resetSwipeState();
                }
                viewOverlay.setVisibility(View.GONE);
                clPopup.setVisibility(View.GONE);
            }
        });

        viewOverlay.setVisibility(View.VISIBLE);
        clPopup.setVisibility(View.VISIBLE);
    }


    private void deleteFolder(File file)
    {
        ((TextView)findViewById(R.id.txtTitle)).setText(getString(R.string.delete_folder));
        ((TextView)findViewById(R.id.txtMessage)).setText(getString(R.string.are_you_sure));
        ((EditText)findViewById(R.id.etData)).setText(file.getName());
        ((TextView)findViewById(R.id.txtDataLabelRight)).setVisibility(View.GONE);
        btPopupYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(file.exists())
                {
                    FileUtil.deleteFolderRecursively(file);
                    filesList.remove(file);
                    fileAdapter.notifyDataSetChanged();
                }
                viewOverlay.setVisibility(View.GONE);
                clPopup.setVisibility(View.GONE);
                fileAdapter.clearSelection();
            }
        });
        btPopupNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Reset any swiped rows back to normal state
                if (fileAdapter != null) {
                    fileAdapter.resetSwipeState();
                }
                viewOverlay.setVisibility(View.GONE);
                clPopup.setVisibility(View.GONE);
                fileAdapter.clearSelection();
            }
        });

        viewOverlay.setVisibility(View.VISIBLE);
        clPopup.setVisibility(View.VISIBLE);
    }

    private String getMimeType()
    {
        switch(fileExtension)
        {
            case ".txt":
                return "text/txt";
            case ".csv":
                return "text/csv";
            case ".xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }
        return "text/txt";
    }

    private void shareSelectedFile()
    {
        File cacheFolder = new File(getExternalCacheDir(), Constants.PROVIDER_CACHE_FOLDER);
        if(cacheFolder.exists() == false)
        {
            cacheFolder.mkdirs();
        }
        else
        {
            FileUtil.deleteAllFiles(cacheFolder);
        }

        if(fileAdapter != null && fileAdapter.getSelectedPosition() != -1) {
            File fileName = fileAdapter.getSelectedFile();
            if (fileName == null || fileName.getName().equals("..") || fileName.isDirectory()) {
                Toast.makeText(this, getString(R.string.cannot_share_folders), Toast.LENGTH_LONG).show();
                return;
            }
            File cacheFile = FileUtil.copyToCacheFolder(this, fileName);

            Uri processedFileURIForShareTo = FileProvider.getUriForFile(BrowserActivity.this, Constants.PROVIDER_AUTHORITY, cacheFile);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(getMimeType());
            intent.putExtra(Intent.EXTRA_STREAM, processedFileURIForShareTo);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share Data"));
        }
        else
        {
            Toast.makeText(this, getString(R.string.please_select_file_or_folder), Toast.LENGTH_LONG).show();
        }
    }

    private void updateButtonVisibility(boolean hasFileSelected) {
        int visibility = hasFileSelected ? View.VISIBLE : View.GONE;
        btnSelectOneFile.setVisibility(visibility);
        btnShare.setVisibility(visibility);
    }
}
