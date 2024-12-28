package com.example.clarivate_employee_privilege.navbar_menu.merchant_detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.clarivate_employee_privilege.R;

import java.util.ArrayList;
import java.util.List;

public class FullscreenImageFragment extends Fragment {

    private static final String ARG_IMAGE_LIST = "image_list";
    private static final String ARG_IMAGE_POSITION = "image_position";

    public static FullscreenImageFragment newInstance(List<String> imageList, int position) {
        FullscreenImageFragment fragment = new FullscreenImageFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_IMAGE_LIST, new ArrayList<>(imageList));
        args.putInt(ARG_IMAGE_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fullscreen_image, container, false);

        ViewPager2 viewPager = view.findViewById(R.id.fullscreen_viewpager);
        ImageView backButton = view.findViewById(R.id.fullscreen_back);

        if (getArguments() != null) {
            List<String> imageList = getArguments().getStringArrayList(ARG_IMAGE_LIST);
            int position = getArguments().getInt(ARG_IMAGE_POSITION);

            FullscreenImageAdapter adapter = new FullscreenImageAdapter(imageList);
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(position, false);
        }

        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        return view;
    }
}