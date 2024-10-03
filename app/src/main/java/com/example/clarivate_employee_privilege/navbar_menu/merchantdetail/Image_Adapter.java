package com.example.clarivate_employee_privilege.navbar_menu.merchantdetail;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clarivate_employee_privilege.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class Image_Adapter extends RecyclerView.Adapter<Image_Adapter.ImageViewHolder> {
    private List<String> imgList;

    public Image_Adapter(List<String> imgList) {
        this.imgList = imgList;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.merchantdetail_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imgList.get(position);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.merchant_image_placeholder) // Placeholder image
                    .fit() // Automatically resize the image to fit the ImageView
                    .centerCrop() // Crop to fit the ImageView
                    .into(holder.imageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            // Image loaded successfully
                            Log.d("Image_Adapter", "Image loaded successfully");
                        }

                        @Override
                        public void onError(Exception e) {
                            // Error loading image, set placeholder directly
                            holder.imageView.setImageResource(R.drawable.merchant_image_placeholder);
                            Log.d("Image_Adapter", "Error loading image: " + e.getMessage());
                        }
                    });
        } else {
            holder.imageView.setImageResource(R.drawable.merchant_image_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return imgList.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.merchantdetail_img);
        }
    }
}