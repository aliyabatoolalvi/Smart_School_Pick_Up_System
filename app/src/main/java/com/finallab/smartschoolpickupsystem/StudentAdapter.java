package com.finallab.smartschoolpickupsystem;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;


import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {
    private final List<Student> students;
    private Context context;

    public StudentAdapter(List<Student> students,Context context) {
        this.students = students;
        this.context = context;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_studentguardian, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = students.get(position);
        holder.studentName.setText(student.getSname());
        holder.registration.setText("Reg No: " + student.getReg());
        holder.section.setText("Section: " + student.getSection());
        holder.studentClass.setText("Class: " + student.getStudentClass());

        Log.d("StudentAdapter", "Image URL: " + student.getImage()); // Debugging

        Glide.with(context)
                .load(TextUtils.isEmpty(student.getImage()) ? R.drawable.img_5 : student.getImage().trim()) // Provide fallback if the URL is empty or invalid
                .placeholder(R.drawable.img_5) // Show placeholder while loading
                .error(R.drawable.img_5) // Show default error image if it fails
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        Log.e("GlideError", "Failed to load image: " + student.getImage(), e);
                        return false; // Keep the default error image
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                                   DataSource dataSource, boolean isFirstResource) {
                        Log.d("GlideSuccess", "Image Loaded Successfully: " + student.getImage());
                        return false;
                    }
                })
                .into(holder.studentImage);

    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    static class StudentViewHolder extends RecyclerView.ViewHolder {

        TextView studentName, registration, section, studentClass;
        ImageView studentImage;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.studentName);
            registration = itemView.findViewById(R.id.registration);
            section = itemView.findViewById(R.id.section);
            studentClass = itemView.findViewById(R.id.studentClass);
            studentImage = itemView.findViewById(R.id.studentImage);
        }
    }
}




