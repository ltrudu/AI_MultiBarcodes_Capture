package com.zebra.ai_multibarcodes_capture.filemanagement;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.ai_multibarcodes_capture.R;
import com.zebra.ai_multibarcodes_capture.helpers.Constants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

public class BrowserActivity extends AppCompatActivity {

    private FileAdapter fileAdapter;
    private ArrayList<File> filesList;
    private ListView listView;
    private Button btnCreateFile;
    private Button btnDeleteSelectedFiles;
    private Button btnOpenFile;
    private Button btnReload;
    private Button btnClose;
    private Button btnShare;
    private Button btnRename;

    ConstraintLayout clFileList;
    ConstraintLayout clPopup;
    Button btPopupYes;
    Button btPopupNo;

    private File baseFolder;
    private int fileCounter = 0;
    private String filePrefix = Constants.FILE_DEFAULT_PREFIX;
    private String fileExtension = Constants.FILE_EXTENSION_TXT;

    private boolean bCanMultiSelect = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        Intent intent = getIntent();
        String folderPath = intent.getStringExtra(Constants.FILEBROWSER_EXTRA_FOLDER_PATH);

        if (folderPath != null) {
            baseFolder = new File(folderPath);

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
        btPopupNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clPopup.setVisibility(View.GONE);
                clFileList.setVisibility(View.VISIBLE);
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

        getFileList();

        btnCreateFile = findViewById(R.id.btCreateFile);
        btnCreateFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewFile();
            }
        });

        btnDeleteSelectedFiles = findViewById(R.id.btDeleteSelected);
        btnDeleteSelectedFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSelectedFiles();
            }
        });

        btnRename = findViewById(R.id.btRenameSelected);
        btnRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                renameSelectedFile();
            }
        });

        btnOpenFile = findViewById(R.id.btSelectOneFile);
        btnOpenFile.setOnClickListener(new View.OnClickListener() {
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

        //btnReload.setVisibility(View.VISIBLE);
        btnOpenFile.setVisibility(View.VISIBLE);
        btnDeleteSelectedFiles.setVisibility(View.VISIBLE);
        btnCreateFile.setVisibility(View.VISIBLE);
        btnShare.setVisibility(View.VISIBLE);
    }

    private void finishWithFileSelectArguments()
    {
        Intent resultIntent = new Intent();
        int fileNamePosition = listView.getCheckedItemPosition();
        if(fileNamePosition != -1) {
            File fileName = filesList.get(fileNamePosition);
            resultIntent.putExtra(Constants.FILEBROWSER_RESULT_FILENAME, FileUtil.getFileNameWithoutExtension(fileName));
            resultIntent.putExtra(Constants.FILEBROWSER_RESULT_FILE_EXTENSION, FileUtil.getFileExtension(fileName));
            resultIntent.putExtra(Constants.FILEBROWSER_RESULT_FILEPATH, fileName.getPath());
            setResult(RESULT_OK, resultIntent);
            finish();
        }
        else
        {
            Toast.makeText(this, "Selectionnez un fichier s'il vous plait.", Toast.LENGTH_LONG).show();
        }
    }

    private void getFileList() {
        File[] files = baseFolder.listFiles();
        if(filesList == null)
            filesList = new ArrayList<>();
        filesList.clear();
        fileCounter = 0;
        if (files != null) {
            for (File file : files) {
                if(file.getName().contains(fileExtension)) {
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
                        File newFile = new File(baseFolder, newFileName + fileExtension);
                        if(newFile.exists())
                        {
                            Toast.makeText(BrowserActivity.this, "Le fichier existe déjà.", Toast.LENGTH_LONG).show();
                            clFileList.setVisibility(View.VISIBLE);
                            clPopup.setVisibility(View.GONE);
                            return;
                        }
                        try {
                                filesList.remove(fileNamePosition);
                                fileName.renameTo(newFile);
                                filesList.add(newFile);
                                fileAdapter.notifyDataSetChanged();
                                clFileList.setVisibility(View.VISIBLE);
                                clPopup.setVisibility(View.GONE);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(BrowserActivity.this, "Erreur pour renommer le fichier", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

            clFileList.setVisibility(View.GONE);
            clPopup.setVisibility(View.VISIBLE);
        }
        else
        {
            Toast.makeText(this, "Selectionnez un fichier s'il vous plait.", Toast.LENGTH_LONG).show();
        }
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
                    File newFile = new File(baseFolder, newFileName + fileExtension);
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
                            clFileList.setVisibility(View.VISIBLE);
                            clPopup.setVisibility(View.GONE);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(BrowserActivity.this, "Failed to create file", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        clFileList.setVisibility(View.GONE);
        clPopup.setVisibility(View.VISIBLE);

    }

    private void deleteSelectedFiles() {
        SparseBooleanArray idPositions = listView.getCheckedItemPositions();
        ArrayList<File> toDelete = new ArrayList<>();
        int id = 0;
        for (File file : filesList) {
            if(idPositions.get(id) == true) {
                toDelete.add(file);
                file.delete();
            }
            id++;
        }
        filesList.removeAll(toDelete);
        fileAdapter.notifyDataSetChanged();
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
