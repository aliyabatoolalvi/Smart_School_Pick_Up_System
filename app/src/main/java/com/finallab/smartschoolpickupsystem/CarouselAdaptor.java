package com.finallab.smartschoolpickupsystem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CarouselAdaptor extends RecyclerView.Adapter<CarouselAdaptor.ViewHolder> {

    private final List<Integer> imageList;

    // Constructor to initialize the image list
    public CarouselAdaptor(List<Integer> imageList) {
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each carousel item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.carouselitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Set the image resource for each item
        holder.imageView.setImageResource(imageList.get(position));
    }

    @Override
    public int getItemCount() {
        // Return the total number of items in the list
        return imageList.size();
    }

    // ViewHolder class to hold reference to the ImageView
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.carouselImage);
        }
    }
}
