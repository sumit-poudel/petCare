package com.example.petcare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskClick {
        void onClick(Task task);
    }

    public interface OnTaskLongClick {
        void onLongClick(Task task);
    }

    private final List<Task> tasks;
    private final OnTaskClick clickListener;
    private final OnTaskLongClick longClickListener;

    public TaskAdapter(
            List<Task> tasks,
            OnTaskClick clickListener,
            OnTaskLongClick longClickListener
    ) {
        this.tasks = tasks;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_row, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull TaskViewHolder holder,
            int position
    ) {
        Task task = tasks.get(position);

        holder.check.setChecked(task.isCompleted());

        holder.title.setText(task.getTitle() != null ? task.getTitle() : "");

        String description = task.getDescription();
        String date = task.getDate();

        if (description == null) description = "";
        if (date == null) date = "";

        String repeatSuffix =
                "daily".equals(task.getRepeatType())
                        ? "  •  Repeats daily"
                        : "weekly".equals(task.getRepeatType())
                        ? "  •  Repeats weekly"
                        : "";

        holder.subtitle.setText(
                (description.isEmpty() ? date : date + "  •  " + description)
                        + repeatSuffix
        );

        holder.time.setText(task.getTime() != null ? task.getTime() : "");

        holder.missedBadge.setVisibility(
                TaskTimeUtils.isOverdue(task) ? View.VISIBLE : View.GONE
        );

        holder.itemView.setOnClickListener(v -> clickListener.onClick(task));
        holder.itemView.setOnLongClickListener(v -> {
            longClickListener.onLongClick(task);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox check;
        TextView title;
        TextView subtitle;
        TextView time;
        TextView missedBadge;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            check = itemView.findViewById(R.id.rowCheck);
            title = itemView.findViewById(R.id.rowTitle);
            subtitle = itemView.findViewById(R.id.rowSubtitle);
            time = itemView.findViewById(R.id.rowTime);
            missedBadge = itemView.findViewById(R.id.rowMissedBadge);
        }
    }
}