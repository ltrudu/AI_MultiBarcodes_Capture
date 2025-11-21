package com.zebra.ai_multibarcodes_capture.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zebra.ai_multibarcodes_capture.R;
import com.zebra.ai_multibarcodes_capture.models.LanguageItem;

import java.util.List;

public class LanguageAdapter extends ArrayAdapter<LanguageItem> {
    
    private LayoutInflater inflater;

    public LanguageAdapter(@NonNull Context context, @NonNull List<LanguageItem> languages) {
        super(context, 0, languages);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent, R.layout.spinner_language_item);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent, R.layout.spinner_language_dropdown_item);
    }

    private View createItemView(int position, View convertView, ViewGroup parent, int layoutRes) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(layoutRes, parent, false);
            holder = new ViewHolder();
            holder.flagImageView = convertView.findViewById(R.id.flagImageView);
            holder.languageTextView = convertView.findViewById(R.id.languageTextView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LanguageItem languageItem = getItem(position);
        if (languageItem != null) {
            holder.flagImageView.setText(languageItem.getFlagEmoji());
            holder.languageTextView.setText(languageItem.getLanguageName());
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView flagImageView;
        TextView languageTextView;
    }
}