package com.example.clarivate_employee_privilege.navbar_menu.merchants;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import java.util.List;

public class MerchantsSearchAdapter extends ArrayAdapter<String> {

    public MerchantsSearchAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
    }
}