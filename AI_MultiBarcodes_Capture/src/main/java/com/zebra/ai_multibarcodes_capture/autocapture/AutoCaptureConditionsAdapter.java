package com.zebra.ai_multibarcodes_capture.autocapture;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zebra.ai_multibarcodes_capture.R;
import com.zebra.ai_multibarcodes_capture.autocapture.models.AutoCaptureCondition;
import com.zebra.ai_multibarcodes_capture.autocapture.models.EAutoCaptureConditionType;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying auto capture conditions.
 */
public class AutoCaptureConditionsAdapter extends RecyclerView.Adapter<AutoCaptureConditionsAdapter.ViewHolder> {

    private List<AutoCaptureCondition> conditions = new ArrayList<>();
    private OnConditionClickListener listener;

    public interface OnConditionClickListener {
        void onEditClick(AutoCaptureCondition condition);
        void onDeleteClick(AutoCaptureCondition condition);
    }

    public void setOnConditionClickListener(OnConditionClickListener listener) {
        this.listener = listener;
    }

    public void setConditions(List<AutoCaptureCondition> conditions) {
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
        AutoCaptureCondition condition = conditions.get(position);
        holder.bind(condition);
    }

    @Override
    public int getItemCount() {
        return conditions.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvConditionType;
        private final TextView tvConditionDetails;
        private final ImageButton btnEditCondition;
        private final ImageButton btnDeleteCondition;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvConditionType = itemView.findViewById(R.id.tvConditionType);
            tvConditionDetails = itemView.findViewById(R.id.tvConditionDetails);
            btnEditCondition = itemView.findViewById(R.id.btnEditCondition);
            btnDeleteCondition = itemView.findViewById(R.id.btnDeleteCondition);
        }

        void bind(AutoCaptureCondition condition) {
            if (condition.getType() == EAutoCaptureConditionType.NUMBER_OF_BARCODES) {
                tvConditionType.setText(itemView.getContext().getString(R.string.condition_type_number_of_barcodes));
                tvConditionDetails.setText(itemView.getContext().getString(R.string.condition_number_format, condition.getCount()));
            } else {
                tvConditionType.setText(itemView.getContext().getString(R.string.condition_type_contains_regex));
                tvConditionDetails.setText(itemView.getContext().getString(R.string.condition_regex_format, condition.getCount(), condition.getRegex()));
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
