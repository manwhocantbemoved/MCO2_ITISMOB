package com.example.taskease;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.VH> {

    public interface OnStartClick { void onStart(TaskItem task); }

    private List<TaskItem> tasks = new ArrayList<>();
    private final OnStartClick listener;

    public TaskAdapter(OnStartClick listener) {
        this.listener = listener;
    }

    public void setTasks(List<TaskItem> newTasks) {
        tasks = newTasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        TaskItem t = tasks.get(position);
        holder.name.setText(t.name);
        holder.info.setText(t.priority + " priority - " + t.duration + " min");
        holder.start.setText("Start Focus Timer (" + t.duration + "m)");
        holder.start.setOnClickListener(v -> listener.onStart(t));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, info;
        Button start;

        VH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvItemName);
            info = itemView.findViewById(R.id.tvItemInfo);
            start = itemView.findViewById(R.id.btnItemFocus);
        }
    }
}