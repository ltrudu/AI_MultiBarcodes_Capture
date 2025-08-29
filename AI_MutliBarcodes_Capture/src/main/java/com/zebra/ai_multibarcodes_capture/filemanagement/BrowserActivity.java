package com.zebra.ai_multibarcodes_capture.filemanagement;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

public class BrowserActivity extends AppCompatActivity {

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
    private String fileExtension = Constants.FILE_EXTENSION_TXT;

    private boolean bCanMultiSelect = false;
    private GestureDetector gestureDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

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
            Log.d("Folder Path", "The folder path is: " + baseFolder.getPath());
        } else {
            Log.e("Folder Path", "Folder path is missing!");
            Toast.makeText(this, "Folder path is missing !!!", Toast.LENGTH_LONG).show();
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
        tbManage.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(BrowserActivity.this, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.browser_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if (id == R.id.action_new_file) {
                            createNewFile();
                            return true;
                        } else if (id == R.id.action_new_folder) {
                            createNewFolder();
                            return true;
                        }
                        else if(id == R.id.action_delete)
                        {
                            deleteSelectedFiles();
                            return true;
                        }
                        else if(id == R.id.action_rename)
                        {
                            renameSelectedFile();
                            return true;
                        }
                        return false;
                    }
                });
                popup.show();
            }
        });


        btPopupNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewOverlay.setVisibility(View.GONE);
                clPopup.setVisibility(View.GONE);
            }
        });
        btPopupYes = findViewById(R.id.btYes);

        fileExtension = intent.getStringExtra(Constants.FILEBROWSER_EXTRA_EXTENSION);
        if(fileExtension == null)
            fileExtension = Constants.FILEBROWSER_DEFAULT_EXTENSION;

        filePrefix = intent.getStringExtra(Constants.FILEBROWSER_EXTRA_PREFIX);
        if(filePrefix == null)
            filePrefix = Constants.FILEBROWSER_DEFAULT_PREFIX;

        bCanMultiSelect = intent.getBooleanExtra(Constants.FILEBROWSER_EXTRA_MULTISELECT, false);

        if(bCanMultiSelect)
        {
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE | ListView.CHOICE_MODE_MULTIPLE_MODAL);
        }
        else
        {
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }

        // Add double-tap detection for folder navigation
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                int position = listView.pointToPosition((int) e.getX(), (int) e.getY());
                if (position != ListView.INVALID_POSITION) {
                    File selectedFile = filesList.get(position);
                    if (selectedFile.isDirectory() || selectedFile.getName().equals("..")) {
                        navigateToFolder(selectedFile);
                        return true;
                    }
                }
                return false;
            }
        });

        listView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false; // Let the ListView handle single taps for selection
        });

        btnSelectOneFile = findViewById(R.id.btSelectOneFile);
        btnSelectOneFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishWithFileSelectArguments();
            }
        });

        btnReload = findViewById(R.id.btReload);
        btnReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFileList();
            }
        });

        btnClose = findViewById(R.id.btClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent resultIntent = new Intent();
                setResult(RESULT_CANCELED, resultIntent);
                finish();
            }
        });

        btnShare = findViewById(R.id.btShare);
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareSelectedFile();
            }
        });

        btnSelectOneFile.setVisibility(View.VISIBLE);
        btnShare.setVisibility(View.VISIBLE);
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
        getFileList();
    }

    private void finishWithFileSelectArguments()
    {
        Intent resultIntent = new Intent();
        int fileNamePosition = listView.getCheckedItemPosition();
        if(fileNamePosition != -1) {
            File fileName = filesList.get(fileNamePosition);
            if (fileName.isFile()) {
                resultIntent.putExtra(Constants.FILEBROWSER_RESULT_FILENAME, FileUtil.getFileNameWithoutExtension(fileName));
                resultIntent.putExtra(Constants.FILEBROWSER_RESULT_FILE_EXTENSION, FileUtil.getFileExtension(fileName));
                resultIntent.putExtra(Constants.FILEBROWSER_RESULT_FILEPATH, fileName.getPath());
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Veuillez sélectionner un fichier, pas un dossier.", Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            Toast.makeText(this, "Selectionnez un fichier s'il vous plait.", Toast.LENGTH_LONG).show();
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
                listView.setAdapter(fileAdapter);
            }
            fileAdapter.notifyDataSetChanged();
        }
    }

    private void renameSelectedFile()
    {
        final int fileNamePosition = listView.getCheckedItemPosition();
        if(fileNamePosition != -1) {
            final File fileName = filesList.get(fileNamePosition);
            if (fileName.getName().equals("..") || fileName.isDirectory()) {
                Toast.makeText(this, "Impossible de renommer les dossiers ou le dossier parent.", Toast.LENGTH_LONG).show();
                return;
            }
            ((TextView)findViewById(R.id.txtTitle)).setText("Renommer");
            ((TextView)findViewById(R.id.txtMessage)).setText("Renommer un fichier");
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
                            Toast.makeText(BrowserActivity.this, "Le fichier existe déjà.", Toast.LENGTH_LONG).show();
                            viewOverlay.setVisibility(View.GONE);
                            clPopup.setVisibility(View.GONE);
                            return;
                        }
                        try {
                                filesList.remove(fileNamePosition);
                                fileName.renameTo(newFile);
                                filesList.add(newFile);
                                fileAdapter.notifyDataSetChanged();
                                viewOverlay.setVisibility(View.GONE);
                                clPopup.setVisibility(View.GONE);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(BrowserActivity.this, "Erreur pour renommer le fichier", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

            viewOverlay.setVisibility(View.VISIBLE);
            clPopup.setVisibility(View.VISIBLE);
        }
        else
        {
            Toast.makeText(this, "Selectionnez un fichier s'il vous plait.", Toast.LENGTH_LONG).show();
        }
    }

    private void createNewFolder()
    {
        ((TextView)findViewById(R.id.txtTitle)).setText("Créer");
        ((TextView)findViewById(R.id.txtMessage)).setText("Créer un nouveau dossier");
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
                        Toast.makeText(BrowserActivity.this, "Le dossier existe déjà.", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(BrowserActivity.this, "Failed to create folder:" + newFolder, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        viewOverlay.setVisibility(View.VISIBLE);
        clPopup.setVisibility(View.VISIBLE);
    }

    private void createNewFile() {
        ((TextView)findViewById(R.id.txtTitle)).setText("Créer");
        ((TextView)findViewById(R.id.txtMessage)).setText("Créer un nouveau fichier");
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
                        Toast.makeText(BrowserActivity.this, "Le fichier existe déjà.", Toast.LENGTH_LONG).show();
                        clFileList.setVisibility(View.VISIBLE);
                        clPopup.setVisibility(View.GONE);
                        return;
                    }
                    try {
                        if (newFile.createNewFile()) {
                            filesList.add(newFile);
                            fileCounter++;
                            fileAdapter.notifyDataSetChanged();
                            viewOverlay.setVisibility(View.GONE);
                            clPopup.setVisibility(View.GONE);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(BrowserActivity.this, "Failed to create file", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        viewOverlay.setVisibility(View.VISIBLE);
        clPopup.setVisibility(View.VISIBLE);
    }

    private void deleteSelectedFiles() {
        SparseBooleanArray idPositions = listView.getCheckedItemPositions();
        int id = 0;
        for (File file : filesList) {
            if(idPositions.get(id) == true) {
                if (file.getName().equals("..")) {
                    Toast.makeText(this, "Impossible de supprimer le dossier parent.", Toast.LENGTH_LONG).show();
                    continue;
                }
                if(file.isDirectory())
                {
                    deleteFolder(file);
                }
                else {
                    deleteFile(file);
                }
            }
            id++;
        }
    }

    private void deleteFile(File file)
    {
        ((TextView)findViewById(R.id.txtTitle)).setText("Delete File");
        ((TextView)findViewById(R.id.txtMessage)).setText("Are you sure ?");
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

        viewOverlay.setVisibility(View.VISIBLE);
        clPopup.setVisibility(View.VISIBLE);
    }


    private void deleteFolder(File file)
    {
        ((TextView)findViewById(R.id.txtTitle)).setText("Delete Folder");
        ((TextView)findViewById(R.id.txtMessage)).setText("Are you sure ?");
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

        int fileNamePosition = listView.getCheckedItemPosition();
        if(fileNamePosition != -1) {
            File fileName = filesList.get(fileNamePosition);
            if (fileName.getName().equals("..") || fileName.isDirectory()) {
                Toast.makeText(this, "Impossible de partager les dossiers.", Toast.LENGTH_LONG).show();
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
            Toast.makeText(this, "Selectionnez un fichier s'il vous plait.", Toast.LENGTH_LONG).show();
        }
    }
}
