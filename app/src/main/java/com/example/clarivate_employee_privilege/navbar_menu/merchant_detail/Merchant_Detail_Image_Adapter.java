package com.example.clarivate_employee_privilege.navbar_menu.merchant_detail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clarivate_employee_privilege.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class Merchant_Detail_Image_Adapter extends RecyclerView.Adapter<Merchant_Detail_Image_Adapter.ImageViewHolder> {
    private List<String> imgList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(String imageUrl);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public Merchant_Detail_Image_Adapter(List<String> imgList) {
        this.imgList = imgList;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.merchant_detail_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imgList.get(position);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.clarivate_logo_black)
                    .fit()
                    .centerCrop()
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.clarivate_logo_black);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(imageUrl);
            }
        });
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