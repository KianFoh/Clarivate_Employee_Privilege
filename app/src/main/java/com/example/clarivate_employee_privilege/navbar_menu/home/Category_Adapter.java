// CategoryAdapter.java
package com.example.clarivate_employee_privilege.navbar_menu.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clarivate_employee_privilege.R;

import java.util.List;
import java.util.stream.Collectors;

public class Category_Adapter extends RecyclerView.Adapter<Category_Adapter.CategoryViewHolder> {

    private List<String> categoryList;
    private OnCategoryClickListener onCategoryClickListener;

    public Category_Adapter(List<String> categoryList, OnCategoryClickListener onCategoryClickListener) {
        this.categoryList = categoryList;
        this.onCategoryClickListener = onCategoryClickListener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.merchants_filter_button, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = categoryList.get(position);
        holder.categoryTextView.setText(category);
        holder.itemView.setOnClickListener(v -> onCategoryClickListener.onCategoryClick(category));
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public void updateCategories(List<String> newCategoryList) {
        // Exclude the "All" category
        this.categoryList = newCategoryList.stream()
                .filter(category -> !category.equals("All"))
                .collect(Collectors.toList());
        notifyDataSetChanged();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryTextView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryTextView = itemView.findViewById(R.id.merchants_filterbutton);
        }
    }

    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }
}