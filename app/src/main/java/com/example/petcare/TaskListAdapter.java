package com.example.petcare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class TaskListAdapter extends ArrayAdapter<Task> {

    public TaskListAdapter(Context context, List<Task> tasks) {
        super(context, 0, tasks);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_task_row, parent, false);
        }

        Task task = getItem(position);

        CheckBox check = convertView.findViewById(R.id.rowCheck);
        TextView title = convertView.findViewById(R.id.rowTitle);
        TextView subtitle = convertView.findViewById(R.id.rowSubtitle);
        TextView time = convertView.findViewById(R.id.rowTime);
        TextView missedBadge = convertView.findViewById(R.id.rowMissedBadge);

        if (task != null) {

            check.setChecked(task.isCompleted());

            title.setText(
                    task.getTitle() == null ? "" : task.getTitle()
            );

            String description = task.getDescription();
            String date = task.getDate();

            if (description == null) {
                description = "";
            }

            if (date == null) {
                date = "";
            }

            String repeatSuffix =
                    "daily".equals(task.getRepeatType())
                            ? "  •  Repeats daily"
                            : "weekly".equals(task.getRepeatType())
                            ? "  •  Repeats weekly"
                            : "";

            subtitle.setText(
                    (description.isEmpty() ? date : date + "  •  " + description)
                            + repeatSuffix
            );

            time.setText(
                    task.getTime() == null ? "" : task.getTime()
            );

            missedBadge.setVisibility(
                    TaskTimeUtils.isOverdue(task) ? View.VISIBLE : View.GONE
            );
        }

        return convertView;
    }
}