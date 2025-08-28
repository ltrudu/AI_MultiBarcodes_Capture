package com.zebra.ai_multibarcodes_capture.filemanagement;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import java.io.File;
import java.util.ArrayList;

public class FileAdapter extends ArrayAdapter<File> {

    private Context mContext;
    private ArrayList<File> fileList;

    public FileAdapter(Context context, ArrayList<File> files) {
        super(context, 0, files);
        this.mContext = context;
        this.fileList = files;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        File file = getItem(position);
        if (convertView == null) {
            //convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_multiple_choice, parent, false);
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_single_choice, parent, false);
        }
        CheckedTextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(file.getName());
        return convertView;
    }
}
