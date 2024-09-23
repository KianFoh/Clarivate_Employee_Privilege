package com.example.clarivate_employee_privilege.navbar_menu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.clarivate_employee_privilege.R;

public class AddMerchantFragment extends Fragment {

    public AddMerchantFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_merchant, container, false);

        return view;
    }


}