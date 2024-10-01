package com.example.clarivate_employee_privilege.navbar_menu.merchants;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.clarivate_employee_privilege.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterButton_Adapter extends RecyclerView.Adapter<FilterButton_Adapter.ButtonViewHolder> {
    // List of button labels
    private List<String> buttonList;
    // Listener for button click events
    private OnButtonClickListener onButtonClickListener;
    // List to keep track of filter states
    private List<Boolean> buttonStates;

    // Interface for handling button click events
    public interface OnButtonClickListener {
        void onButtonClick(List<String> selectedCategories);
    }

    // Constructor for ButtonAdapter
    public FilterButton_Adapter(List<String> buttonList, OnButtonClickListener onButtonClickListener) {
        this.buttonList = buttonList;
        this.onButtonClickListener = onButtonClickListener;
        // Initialize button states to false (not toggled)
        this.buttonStates = new ArrayList<>(Collections.nCopies(buttonList.size(), false));
    }

    @NonNull
    @Override
    public ButtonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the button layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.merchants_filter_button, parent, false);
        return new ButtonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ButtonViewHolder holder, int position) {
        // Set the button text and click listener
        String buttonText = buttonList.get(position);
        holder.button.setText(buttonText);
        // Set initial button colors based on state
        updateButtonColors(holder.button, buttonStates.get(position));

        holder.button.setOnClickListener(v -> {
            // Toggle the button state
            boolean newState = !buttonStates.get(position);

            // If "All" is clicked and it's the only one toggled, do nothing
            if (buttonText.equals("All") && buttonStates.contains(true) && buttonStates.indexOf(true) == position) {
                return;
            }

            buttonStates.set(position, newState);

            // If "All" is toggled, untoggle all other buttons
            if (buttonText.equals("All") && newState) {
                for (int i = 0; i < buttonStates.size(); i++) {
                    if (i != position && buttonStates.get(i)) {
                        buttonStates.set(i, false);
                        notifyItemChanged(i);
                    }
                }
            } else if (newState) {
                // If any other button is toggled, untoggle "All"
                int allIndex = buttonList.indexOf("All");
                if (allIndex != -1 && buttonStates.get(allIndex)) {
                    buttonStates.set(allIndex, false);
                    notifyItemChanged(allIndex);
                }
            }

            // Ensure "All" is toggled if no other buttons are toggled
            if (!buttonStates.contains(true)) {
                int allIndex = buttonList.indexOf("All");
                if (allIndex != -1 && !buttonStates.get(allIndex)) {
                    buttonStates.set(allIndex, true);
                    notifyItemChanged(allIndex);
                }
            }

            // Update button colors based on new state
            updateButtonColors(holder.button, newState);

            // Collect all selected categories
            List<String> selectedCategories = new ArrayList<>();
            for (int i = 0; i < buttonStates.size(); i++) {
                if (buttonStates.get(i)) {
                    selectedCategories.add(buttonList.get(i));
                }
            }

            // Notify the listener with the selected categories
            onButtonClickListener.onButtonClick(selectedCategories);
        });
    }

    @Override
    public int getItemCount() {
        // Return the total number of buttons
        return buttonList.size();
    }

    // ViewHolder class for the button
    static class ButtonViewHolder extends RecyclerView.ViewHolder {
        Button button;

        ButtonViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find the button in the layout
            button = itemView.findViewById(R.id.merchants_filterbutton);
        }
    }

    // Helper method to update button colors based on state
    private void updateButtonColors(Button button, boolean isToggled) {
        if (isToggled) {
            button.setBackgroundColor(Color.WHITE);
            button.setTextColor(Color.BLACK);
        } else {
            button.setBackgroundColor(Color.BLACK);
            button.setTextColor(Color.WHITE);
        }
    }

    // Method to update categories
    public void updateCategories(List<String> newCategoryNames) {
        // Store previous states in a map
        Map<String, Boolean> previousStates = new HashMap<>();
        for (int i = 0; i < buttonList.size(); i++) {
            previousStates.put(buttonList.get(i), buttonStates.get(i));
        }

        // Remove categories that are no longer present
        for (int i = buttonList.size() - 1; i >= 0; i--) {
            if (!newCategoryNames.contains(buttonList.get(i))) {
                // Untoggle the button if it is toggled
                if (buttonStates.get(i)) {
                    buttonStates.set(i, false);
                    notifyItemChanged(i);
                }
                buttonList.remove(i);
                buttonStates.remove(i);
                notifyItemRemoved(i);
            }
        }

        // Add new categories and update existing ones
        for (int i = 0; i < newCategoryNames.size(); i++) {
            String category = newCategoryNames.get(i);
            if (!buttonList.contains(category)) {
                buttonList.add(i, category);
                buttonStates.add(i, previousStates.getOrDefault(category, false));
                notifyItemInserted(i);
            } else {
                buttonStates.set(i, previousStates.get(category));
                notifyItemChanged(i);
            }
        }
    }

    public void toggleButton(String buttonText) {
        int position = buttonList.indexOf(buttonText);
        if (position != -1) {
            boolean newState = !buttonStates.get(position);
            buttonStates.set(position, newState);
            notifyItemChanged(position);

            // Collect all selected categories
            List<String> selectedCategories = new ArrayList<>();
            for (int i = 0; i < buttonStates.size(); i++) {
                if (buttonStates.get(i)) {
                    selectedCategories.add(buttonList.get(i));
                }
            }

            // Notify the listener with the selected categories
            onButtonClickListener.onButtonClick(selectedCategories);
        }
    }

    public List<String> getSelectedCategories() {
        List<String> selectedCategories = new ArrayList<>();
        for (int i = 0; i < buttonStates.size(); i++) {
            if (buttonStates.get(i)) {
                selectedCategories.add(buttonList.get(i));
            }
        }
        return selectedCategories;
    }
}