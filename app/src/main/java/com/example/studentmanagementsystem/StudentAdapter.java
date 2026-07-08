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

        return new ViewHolder(layout, tvId, tvName, tvRoll, tvCourse);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject student = studentList.getJSONObject(position);

            // यहाँ हमने "name" को बदलकर "NAME" कर दिया है जो आपके सर्वर से मैच करता है
            holder.tvDisplayId.setText("ID: " + student.getString("id"));
            holder.tvDisplayName.setText("Name: " + student.getString("NAME"));
            holder.tvDisplayRoll.setText("Roll No: " + student.getString("roll"));
            holder.tvDisplayCourse.setText("Course: " + student.getString("course"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return studentList.length();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDisplayId, tvDisplayName, tvDisplayRoll, tvDisplayCourse;

        public ViewHolder(@NonNull View itemView, TextView id, TextView name, TextView roll, TextView course) {
            super(itemView);
            this.tvDisplayId = id;
            this.tvDisplayName = name;
            this.tvDisplayRoll = roll;
            this.tvDisplayCourse = course;
        }
    }
}