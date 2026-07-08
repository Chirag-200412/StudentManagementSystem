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
        // यहाँ हम सीधे जावा कोड से ही बिना XML के एक सुंदर कार्ड (LinearLayout) बना रहे हैं
        LinearLayout layout = new LinearLayout(parent.getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(24, 12, 24, 12);
        layout.setLayoutParams(params);
        layout.setPadding(32, 32, 32, 32);
        
        // बैकग्राउंड के लिए डिफ़ॉल्ट बॉर्डर/फ्रेम देना
        layout.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

        // चारों टेक्स्टव्यू डायनामिकली बनाना
        TextView tvId = new TextView(parent.getContext());
        tvId.setTextSize(14);
        tvId.setTextColor(android.graphics.Color.parseColor("#FF9800"));
        layout.addView(tvId);

        TextView tvName = new TextView(parent.getContext());
        tvName.setTextSize(18);
        tvName.setTextColor(android.graphics.Color.parseColor("#333333"));
        tvName.setPadding(0, 8, 0, 0);
        layout.addView(tvName);

        TextView tvRoll = new TextView(parent.getContext());
        tvRoll.setTextSize(14);
        tvRoll.setTextColor(android.graphics.Color.parseColor("#666666"));
        tvRoll.setPadding(0, 4, 0, 0);
        layout.addView(tvRoll);

        TextView tvCourse = new TextView(parent.getContext());
        tvCourse.setTextSize(14);
        tvCourse.setTextColor(android.graphics.Color.parseColor("#666666"));
        tvCourse.setPadding(0, 4, 0, 0);
        layout.addView(tvCourse);

        return new ViewHolder(layout, tvId, tvName, tvRoll, tvCourse);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject student = studentList.getJSONObject(position);
            holder.tvDisplayId.setText("ID: " + student.getString("id"));
            holder.tvDisplayName.setText("Name: " + student.getString("name"));
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