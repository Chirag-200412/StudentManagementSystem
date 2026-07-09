package com.example.studentmanagementsystem;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

    private JSONArray studentList;

    public StudentAdapter(JSONArray studentList) {
        this.studentList = studentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout layout = new LinearLayout(parent.getContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(30, 15, 30, 15);
        layout.setLayoutParams(params);
        layout.setPadding(40, 40, 40, 40);

        layout.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        TextView tvId = new TextView(parent.getContext());
        tvId.setLayoutParams(textParams);
        tvId.setTextSize(14);
        tvId.setTextColor(android.graphics.Color.parseColor("#FF9800"));
        layout.addView(tvId);

        TextView tvName = new TextView(parent.getContext());
        tvName.setLayoutParams(textParams);
        tvName.setTextSize(18);
        tvName.setTextColor(android.graphics.Color.parseColor("#333333"));
        tvName.setPadding(0, 10, 0, 0);
        layout.addView(tvName);

        TextView tvRoll = new TextView(parent.getContext());
        tvRoll.setLayoutParams(textParams);
        tvRoll.setTextSize(15);
        tvRoll.setTextColor(android.graphics.Color.parseColor("#666666"));
        tvRoll.setPadding(0, 6, 0, 0);
        layout.addView(tvRoll);

        TextView tvCourse = new TextView(parent.getContext());
        tvCourse.setLayoutParams(textParams);
        tvCourse.setTextSize(15);
        tvCourse.setTextColor(android.graphics.Color.parseColor("#666666"));
        tvCourse.setPadding(0, 6, 0, 0);
        layout.addView(tvCourse);

        // 🔥 EXTRA NEW CUSTOM PARAMETERS FOR COMPLAINTS, ATTENDANCE & RESULTS
        TextView tvAttendance = new TextView(parent.getContext());
        tvAttendance.setLayoutParams(textParams);
        tvAttendance.setTextSize(15);
        tvAttendance.setTextColor(android.graphics.Color.parseColor("#059669")); // Green color for metrics
        tvAttendance.setPadding(0, 6, 0, 0);
        layout.addView(tvAttendance);

        TextView tvResult = new TextView(parent.getContext());
        tvResult.setLayoutParams(textParams);
        tvResult.setTextSize(15);
        tvResult.setTextColor(android.graphics.Color.parseColor("#2563EB")); // Blue color for scores
        tvResult.setPadding(0, 6, 0, 0);
        layout.addView(tvResult);

        return new ViewHolder(layout, tvId, tvName, tvRoll, tvCourse, tvAttendance, tvResult);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject student = studentList.getJSONObject(position);

            // Base core parameters extraction
            holder.tvDisplayId.setText("ID / Target Key: " + student.getString("id"));
            holder.tvDisplayName.setText("Title/Name: " + student.getString("NAME"));
            holder.tvDisplayRoll.setText("Roll / Sub-Index: " + student.getString("roll"));
            holder.tvDisplayCourse.setText("Course / Message: " + student.getString("course"));

            // 🔥 SAFELY INJECT NEW COLUMNS (Agar data Students node ka hai toh metrics dikhenge, aur Complaints ka hai toh default safely hide/handle honge)
            if (student.has("attendance")) {
                holder.tvDisplayAttendance.setVisibility(View.VISIBLE);
                holder.tvDisplayAttendance.setText("Attendance: " + student.getString("attendance"));
            } else {
                holder.tvDisplayAttendance.setVisibility(View.GONE);
            }

            if (student.has("result")) {
                holder.tvDisplayResult.setVisibility(View.VISIBLE);
                holder.tvDisplayResult.setText("Result: " + student.getString("result"));
            } else {
                holder.tvDisplayResult.setVisibility(View.GONE);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return studentList.length();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDisplayId, tvDisplayName, tvDisplayRoll, tvDisplayCourse, tvDisplayAttendance, tvDisplayResult;

        public ViewHolder(@NonNull View itemView, TextView id, TextView name, TextView roll, TextView course, TextView attendance, TextView result) {
            super(itemView);
            this.tvDisplayId = id;
            this.tvDisplayName = name;
            this.tvDisplayRoll = roll;
            this.tvDisplayCourse = course;
            this.tvDisplayAttendance = attendance; // Joda naya structural element
            this.tvDisplayResult = result;         // Joda naya grade mapping element
        }
    }
}