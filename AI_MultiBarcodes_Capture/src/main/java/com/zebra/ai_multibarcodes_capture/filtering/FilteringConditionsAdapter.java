package com.zebra.ai_multibarcodes_capture.filtering;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.ai_multibarcodes_capture.R;
import com.zebra.ai_multibarcodes_capture.filtering.models.FilteringCondition;
import com.zebra.ai_multibarcodes_capture.filtering.models.EFilteringConditionType;
import com.zebra.ai_multibarcodes_capture.helpers.EBarcodesSymbologies;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying filtering conditions.
 */
public class FilteringConditionsAdapter extends RecyclerView.Adapter<FilteringConditionsAdapter.ViewHolder> {

    private List<FilteringCondition> conditions = new ArrayList<>();
    private OnConditionClickListener listener;

    public interface OnConditionClickListener {
        void onEditClick(FilteringCondition condition);
        void onDeleteClick(FilteringCondition condition);
    }

    public void setOnConditionClickListener(OnConditionClickListener listener) {
        this.listener = listener;
    }

    public void setConditions(List<FilteringCondition> conditions) {
        this.conditions = conditions != null ? conditions : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_auto_capture_condition, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FilteringCondition condition = conditions.get(position);
        holder.bind(condition);
    }

    @Override
    public int getItemCount() {
        return conditions.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvConditionType;
        private final TextView tvConditionDetails;
        private final TextView tvConditionDescription;
        private final ImageButton btnEditCondition;
        private final ImageButton btnDeleteCondition;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvConditionType = itemView.findViewById(R.id.tvConditionType);
            tvConditionDetails = itemView.findViewById(R.id.tvConditionDetails);
            tvConditionDescription = itemView.findViewById(R.id.tvConditionDescription);
            btnEditCondition = itemView.findViewById(R.id.btnEditCondition);
            btnDeleteCondition = itemView.findViewById(R.id.btnDeleteCondition);
        }

        void bind(FilteringCondition condition) {
            if (condition.getType() == EFilteringConditionType.CONTAINS_REGEX) {
                tvConditionType.setText(itemView.getContext().getString(R.string.filtering_condition_type_regex));
                tvConditionDetails.setText(itemView.getContext().getString(R.string.filtering_regex_format, condition.getRegex()));
            } else if (condition.getType() == EFilteringConditionType.SYMBOLOGY) {
                tvConditionType.setText(itemView.getContext().getString(R.string.filtering_condition_type_symbology));
                EBarcodesSymbologies symbology = EBarcodesSymbologies.fromInt(condition.getSymbology());
                tvConditionDetails.setText(itemView.getContext().getString(R.string.filtering_symbology_format, symbology.getName()));
            } else if (condition.getType() == EFilteringConditionType.COMPLEX) {
                tvConditionType.setText(itemView.getContext().getString(R.string.filtering_condition_type_complex));
                EBarcodesSymbologies symbology = EBarcodesSymbologies.fromInt(condition.getSymbology());
                tvConditionDetails.setText(itemView.getContext().getString(R.string.filtering_complex_format, symbology.getName(), condition.getRegex()));
            }

            // Show description if not empty
            String description = condition.getDescription();
            if (description != null && !description.isEmpty()) {
                tvConditionDescription.setText(description);
                tvConditionDescription.setVisibility(View.VISIBLE);
            } else {
                tvConditionDescription.setVisibility(View.GONE);
            }

            btnEditCondition.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(condition);
                }
            });

            btnDeleteCondition.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(condition);
                }
            });
        }
    }
}
