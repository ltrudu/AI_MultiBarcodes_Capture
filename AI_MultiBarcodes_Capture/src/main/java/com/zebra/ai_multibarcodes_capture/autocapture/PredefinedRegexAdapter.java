package com.zebra.ai_multibarcodes_capture.autocapture;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.ai_multibarcodes_capture.R;
import com.zebra.ai_multibarcodes_capture.autocapture.models.PredefinedRegex;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying predefined regex patterns.
 */
public class PredefinedRegexAdapter extends RecyclerView.Adapter<PredefinedRegexAdapter.ViewHolder> {

    private List<PredefinedRegex> regexList = new ArrayList<>();
    private OnRegexSelectedListener listener;

    public interface OnRegexSelectedListener {
        void onRegexSelected(PredefinedRegex regex);
    }

    public void setOnRegexSelectedListener(OnRegexSelectedListener listener) {
        this.listener = listener;
    }

    public void setRegexList(List<PredefinedRegex> regexList) {
        this.regexList = regexList != null ? regexList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_predefined_regex, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PredefinedRegex regex = regexList.get(position);
        holder.bind(regex);
    }

    @Override
    public int getItemCount() {
        return regexList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCategory;
        private final TextView tvDescription;
        private final TextView tvPattern;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPattern = itemView.findViewById(R.id.tvPattern);
        }

        void bind(PredefinedRegex regex) {
            tvCategory.setText(regex.getCategory());
            tvDescription.setText(regex.getDescription());
            tvPattern.setText(regex.getPattern());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRegexSelected(regex);
                }
            });
        }
    }
}
