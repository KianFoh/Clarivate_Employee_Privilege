package com.example.clarivate_employee_privilege.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clarivate_employee_privilege.R;

public class Toast_Utils {
    public static void showToast(Context context, String message, boolean isSuccess) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout;
        TextView text;


        if (isSuccess) {
            layout = inflater.inflate(R.layout.custom_toast_success, null);
            text = layout.findViewById(R.id.toastsucccess_message);
        }
        else {
            layout = inflater.inflate(R.layout.custom_toast_error, null);
            text = layout.findViewById(R.id.toasterror_message);
        }

        text.setText(message);

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);

        // Set the position of the Toast to be higher
        // toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 200); // Adjust the offset as needed

        toast.show();
    }
}