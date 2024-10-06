package com.example.clarivate_employee_privilege.navbar_menu.merchant_detail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog;

import com.example.clarivate_employee_privilege.R;
import com.example.clarivate_employee_privilege.utils.App_Utils;

public class Merchant_Detail_Utils {

    // Show Add Admin Dialog
    public static AlertDialog showDeleteMerchantDialog(Context context, Merchant_Detail_API merchantApi, String merchantId) {
        // Inflate the delete merchant form layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.merchant_detail_delete, null);
        AlertDialog dialog;

        // Create the AlertDialog with custom style
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialog);
        builder.setView(dialogView);

        // Show the dialog
        dialog = builder.create();
        dialog.show();

        // Set up the close button
        ImageButton close = dialogView.findViewById(R.id.merchantdetail_delete_close);
        Button cancel = dialogView.findViewById(R.id.merchantdetail_delete_cancel);
        close.setOnClickListener(v -> {
            dialog.dismiss();
        });
        cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        // Set up the delete merchant button
        Button delete = dialogView.findViewById(R.id.merchantdetail_delete_delete);
        delete.setOnClickListener(v -> {
            App_Utils.disableButton(v);
            merchantApi.deleteMerchant(context, dialog, merchantId, () -> v.setEnabled(true));
        });

        // Set the dialog window size to custom width and height
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        return dialog;
    }

}
