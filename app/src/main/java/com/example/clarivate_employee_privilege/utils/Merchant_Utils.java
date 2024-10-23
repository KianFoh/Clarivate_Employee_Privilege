package com.example.clarivate_employee_privilege.utils;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonArray;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class Merchant_Utils {

    public static void addNewFields(LinearLayout layout, String hint, Context context) {
        TextInputLayout newTextInputLayout = new TextInputLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dpToPx(5, context));
        newTextInputLayout.setLayoutParams(params);
        newTextInputLayout.setHintEnabled(false);
        newTextInputLayout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);

        TextInputEditText newTextInputEditText = new TextInputEditText(newTextInputLayout.getContext());
        newTextInputEditText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        newTextInputEditText.setHint(hint);
        newTextInputEditText.setMaxLines(1);
        newTextInputEditText.setSingleLine(true);
        newTextInputEditText.setHorizontallyScrolling(true);

        newTextInputLayout.addView(newTextInputEditText);
        layout.addView(newTextInputLayout);
    }

    public static void validateImageURLs(List<String> imageURLList, LinearLayout layout, CountDownLatch latch, AtomicBoolean hasError) {
        // Iterate through each child view in the layout
        for (int i = 0; i < layout.getChildCount(); i++) {
            TextInputLayout textInputLayout = (TextInputLayout) layout.getChildAt(i);
            TextInputEditText editText = (TextInputEditText) textInputLayout.getEditText();

            if (editText != null) {
                String url = editText.getText().toString().trim();

                if (url.isEmpty()) {
                    latch.countDown(); // Decrement latch for empty URLs
                    continue; // Ignore empty URLs
                }

                // Use Picasso to check if the image URL is valid
                Picasso.get().load(url).fetch(new Callback() {
                    @Override
                    public void onSuccess() {
                        textInputLayout.setError(null); // Clear any previous error
                        imageURLList.add(url); // Add to valid URLs if the image is successfully loaded
                        latch.countDown(); // Decrement latch on success
                    }

                    @Override
                    public void onError(Exception e) {
                        textInputLayout.setError("Invalid image URL"); // Set error message
                        hasError.set(true); // Set error flag
                        latch.countDown(); // Decrement latch on error
                    }
                });
            }
            else {
                latch.countDown(); // Decrement latch if editText is null
            }
        }
    }

    public static int dpToPx(int dp, Context context) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics()));
    }

    public static void removeAllViewsExceptFirst(LinearLayout layout) {
        for (int i = layout.getChildCount() - 1; i > 0; i--) {
            layout.removeViewAt(i);
        }
    }

    public static void getTextInputValues(List<String> list, LinearLayout layout) {
        for (int i = 0; i < layout.getChildCount(); i++) {
            TextInputLayout textInputLayout = (TextInputLayout) layout.getChildAt(i);
            TextInputEditText editText = (TextInputEditText) textInputLayout.getEditText();
            if (editText != null && editText.getText() != null && !editText.getText().toString().trim().isEmpty()) {
                list.add(editText.getText().toString());
            }
        }
    }

    public static JsonArray convertListToJsonArray(List<String> list) {
        JsonArray jsonArray = new JsonArray();
        for (String item : list) {
            jsonArray.add(item);
        }
        return jsonArray;
    }

    public static void clearErrors(TextInputLayout[] mainFields, LinearLayout... layouts) {
        // Clear errors for main fields
        for (TextInputLayout field : mainFields) {
            field.setError(null);
        }

        // Clear errors for layout fields
        for (LinearLayout layout : layouts) {
            for (int i = 0; i < layout.getChildCount(); i++) {
                View view = layout.getChildAt(i);
                if (view instanceof TextInputLayout) {
                    ((TextInputLayout) view).setError(null);
                }
            }
        }
    }


    public static void handleError(String inputfield, String error, TextInputLayout name, TextInputLayout type, LinearLayout addressLayout, LinearLayout imageLayout, TextInputLayout discount, TextInputLayout terms, Context context) {
        switch (inputfield) {
            case "Name":
                name.setError(("** " + error));
                break;
            case "Category":
                type.setError(("** " + error));
                break;
            case "Address":
                ((TextInputLayout) addressLayout.getChildAt(0)).setError(("** " + error));
                break;
            case "Images":
                ((TextInputLayout) imageLayout.getChildAt(0)).setError(("** " + error));
                break;
            case "Discount":
                discount.setError(("** " + error));
                break;
            case "Terms":
                terms.setError(("** " + error));
                break;
            default:
                Toast_Utils.showToast(context, error, false);
                break;
        }
    }
}