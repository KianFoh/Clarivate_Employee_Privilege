package com.example.clarivate_employee_privilege.navbar_menu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.example.clarivate_employee_privilege.R;
import com.squareup.picasso.Picasso;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        ImageView img = view.findViewById(R.id.home_pic);
        Picasso.get().load("https://lh5.googleusercontent.com/p/AF1QipNMQVtmIuSIiGdzilPPpVGoFBEa-mKyUX3XUCyS=w408-h306-k-no").into(img);

        return view;
    }


}