package com.zebra.ai_multibarcodes_capture.filemanagement;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

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

        TextView textView; // Use TextView as common base, then cast if needed
        if (file.getName().equals("..")) {
            if (convertView == null || ! (convertView.getTag() instanceof String) || !((String) convertView.getTag()).equals("parent_dir")) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
                convertView.setTag("parent_dir");
            }
            textView = (TextView) convertView.findViewById(android.R.id.text1);
        } else {
            if (convertView == null || ! (convertView.getTag() instanceof String) || !((String) convertView.getTag()).equals("file_folder")) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_single_choice, parent, false);
                convertView.setTag("file_folder");
            }
            textView = (CheckedTextView) convertView.findViewById(android.R.id.text1);
        }
        
        String displayName;
        if (file.getName().equals("..")) {
            displayName = ".. (Parent Directory)";
        } else if (file.isDirectory()) {
            displayName = "📁 " + file.getName();
        } else {
            displayName = "📄 " + file.getName();
        }
        
        textView.setText(displayName);
        return convertView;
    }
}
