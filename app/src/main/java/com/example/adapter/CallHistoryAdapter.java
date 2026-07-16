package com.example.adapter;

import android.provider.CallLog;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.data.models.CallLogEntry;
import com.example.databinding.ItemCallHistoryBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CallHistoryAdapter extends RecyclerView.Adapter<CallHistoryAdapter.ViewHolder> {
    private List<CallLogEntry> history = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
    private OnCallClickListener listener;

    public interface OnCallClickListener {
        void onCallClick(CallLogEntry entry);
    }

    public void setOnCallClickListener(OnCallClickListener listener) {
        this.listener = listener;
    }

    public void setHistory(List<CallLogEntry> history) {
        this.history = history;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCallHistoryBinding binding = ItemCallHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CallLogEntry entry = history.get(position);
        holder.binding.tvName.setText(entry.getName() != null ? entry.getName() : entry.getNumber());
        holder.binding.tvDate.setText(dateFormat.format(new Date(entry.getDate())));
        
        // Format duration
        try {
            long seconds = Long.parseLong(entry.getDuration());
            if (seconds == 0) {
                holder.binding.tvDuration.setText("0s");
            } else if (seconds < 60) {
                holder.binding.tvDuration.setText(seconds + "s");
            } else {
                holder.binding.tvDuration.setText((seconds / 60) + "m " + (seconds % 60) + "s");
            }
        } catch (Exception e) {
            holder.binding.tvDuration.setText(entry.getDuration());
        }

        int iconRes;
        switch (entry.getType()) {
            case android.provider.CallLog.Calls.INCOMING_TYPE: iconRes = android.R.drawable.sym_call_incoming; break;
            case android.provider.CallLog.Calls.OUTGOING_TYPE: iconRes = android.R.drawable.sym_call_outgoing; break;
            case android.provider.CallLog.Calls.MISSED_TYPE: iconRes = android.R.drawable.sym_call_missed; break;
            case android.provider.CallLog.Calls.REJECTED_TYPE: iconRes = android.R.drawable.sym_call_missed; break;
            default: iconRes = android.R.drawable.sym_action_call;
        }
        holder.binding.ivCallType.setImageResource(iconRes);

        holder.binding.btnCallAction.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCallClick(entry);
            }
        });
    }

    @Override
    public int getItemCount() { return history.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemCallHistoryBinding binding;
        ViewHolder(ItemCallHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
