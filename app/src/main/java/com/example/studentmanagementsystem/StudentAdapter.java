package com.example.studentmanagementsystem;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

    private final JSONArray studentList;

    public StudentAdapter(JSONArray studentList) {
        this.studentList = studentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Linear stacking matrix programmatic cards layout
        LinearLayout cardContainer = new LinearLayout(parent.getContext());
        cardContainer.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(30, 15, 30, 15);
        cardContainer.setLayoutParams(params);
        cardContainer.setPadding(45, 40, 45, 40);
        cardContainer.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        TextView tvTitle = new TextView(parent.getContext());
        tvTitle.setLayoutParams(textParams);
        tvTitle.setTextSize(16);
        tvTitle.setTextColor(Color.parseColor("#333333"));
        tvTitle.setTypeface(null, Typeface.BOLD);
        cardContainer.addView(tvTitle);

        TextView tvDetails = new TextView(parent.getContext());
        tvDetails.setLayoutParams(textParams);
        tvDetails.setTextSize(13);
        tvDetails.setTextColor(Color.parseColor("#666666"));
        tvDetails.setPadding(0, 10, 0, 0);
        cardContainer.addView(tvDetails);

        // Horizontal Row embedded below description layout elements
        LinearLayout layoutActions = new LinearLayout(parent.getContext());
        layoutActions.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, 25, 0, 0);
        layoutActions.setLayoutParams(rowParams);

        Button btnTick = new Button(parent.getContext());
        btnTick.setText("✔️ APPROVE");
        btnTick.setBackgroundColor(Color.parseColor("#10B981"));
        btnTick.setTextColor(Color.WHITE);
        btnTick.setTextSize(11);
        btnTick.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams pTick = new LinearLayout.LayoutParams(0, 95, 1f);
        pTick.setMargins(0, 0, 15, 0);
        btnTick.setLayoutParams(pTick);

        Button btnCut = new Button(parent.getContext());
        btnCut.setText("✖️ DECLINE");
        btnCut.setBackgroundColor(Color.parseColor("#EF4444"));
        btnCut.setTextColor(Color.WHITE);
        btnCut.setTextSize(11);
        btnCut.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams pCut = new LinearLayout.LayoutParams(0, 95, 1f);
        btnCut.setLayoutParams(pCut);

        layoutActions.addView(btnTick);
        layoutActions.addView(btnCut);
        cardContainer.addView(layoutActions);

        return new ViewHolder(cardContainer, tvTitle, tvDetails, layoutActions, btnTick, btnCut);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject student = studentList.getJSONObject(position);

            final String idValue = student.optString("id").trim();
            final String nameValue = student.optString("NAME").trim();
            final String rollValue = student.optString("roll").trim();
            String courseValue = student.optString("course").trim();

            // 🔥 DOUBLE PROTECT FIX: Agar array elements khali hain toh instantly element hide aur structural dimensions zero
            if (nameValue.isEmpty() && rollValue.isEmpty()) {
                holder.itemView.setVisibility(View.GONE);
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                return;
            } else {
                holder.itemView.setVisibility(View.VISIBLE);
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));
            }

            holder.textTitle.setText(nameValue);

            StringBuilder sbDesc = new StringBuilder();
            sbDesc.append(rollValue.isEmpty() ? "ID: " + idValue : rollValue).append("\n");
            sbDesc.append("Statement: ").append(courseValue);

            if (student.has("attendance")) {
                sbDesc.append("\nAttendance: ").append(student.optString("attendance"));
            }
            if (student.has("result")) {
                sbDesc.append("\nResult/CGPA: ").append(student.optString("result"));
            }

            holder.textDetails.setText(sbDesc.toString());

            // Check dynamic conditions for showing approval mini row buttons
            if (nameValue.contains("Paid Due") || courseValue.contains("clearance") || nameValue.contains("Fee Clearance")) {
                holder.layoutActions.setVisibility(View.VISIBLE);

                // ✔️ APPROVE CLICK ACTION
                holder.btnTick.setOnClickListener(v -> {
                    if (idValue.isEmpty()) {
                        Toast.makeText(v.getContext(), "Error: Invalid transaction link ID!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();

                    // Directly wipe balance under principal student node trees
                    rootRef.child("Students").child(idValue).child("due_fee").setValue("0");
                    rootRef.child("PaymentApprovals").child(idValue).removeValue();

                    Toast.makeText(v.getContext(), "Cleared & Approved Successfully!", Toast.LENGTH_LONG).show();
                });

                // ✖️ DECLINE CLICK ACTION
                holder.btnCut.setOnClickListener(v -> {
                    if (!idValue.isEmpty()) {
                        FirebaseDatabase.getInstance().getReference("PaymentApprovals").child(idValue).removeValue();
                        Toast.makeText(v.getContext(), "Request Dismissed!", Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                holder.layoutActions.setVisibility(View.GONE);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return studentList != null ? studentList.length() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textDetails;
        LinearLayout layoutActions;
        Button btnTick, btnCut;

        public ViewHolder(@NonNull View itemView, TextView title, TextView details, LinearLayout actionsLayout, Button tick, Button cut) {
            super(itemView);
            this.textTitle = title;
            this.textDetails = details;
            this.layoutActions = actionsLayout;
            this.btnTick = tick;
            this.btnCut = cut;
        }
    }
}