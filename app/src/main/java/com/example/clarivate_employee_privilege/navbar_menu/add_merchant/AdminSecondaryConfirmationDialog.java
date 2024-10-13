package com.example.clarivate_employee_privilege.navbar_menu.add_merchant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.clarivate_employee_privilege.R;

public class AdminSecondaryConfirmationDialog extends DialogFragment {

    private Runnable onConfirmCallback;

    public void setOnConfirmCallback(Runnable onConfirmCallback) {
        this.onConfirmCallback = onConfirmCallback;
    }
    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(1050, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.admin_secondary_confirmation, container, false);

        ImageButton closeButton = view.findViewById(R.id.adminsecond_confirm_close);
        Button cancelButton = view.findViewById(R.id.adminsecond_confirm_cancel);
        Button sureButton = view.findViewById(R.id.adminsecond_confirm_sure);

        closeButton.setOnClickListener(v -> dismiss());
        cancelButton.setOnClickListener(v -> dismiss());
        sureButton.setOnClickListener(v -> {
            if (onConfirmCallback != null) {
                onConfirmCallback.run();
            }
            dismiss();
        });

        return view;
    }
}