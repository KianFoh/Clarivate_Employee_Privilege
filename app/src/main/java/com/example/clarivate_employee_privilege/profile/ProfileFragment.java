package com.example.clarivate_employee_privilege.profile;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.example.clarivate_employee_privilege.MainActivity;
import com.example.clarivate_employee_privilege.R;
import com.example.clarivate_employee_privilege.api.SocketService;
import com.example.clarivate_employee_privilege.api.SocketServiceManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {

    private GoogleSignInClient googleSignInClient;
    private SocketService socketService;
    private Profile_API profileAPI;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize GoogleSignInClient
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        // Initialize Profile_API
        profileAPI = new Profile_API(requireContext());

        // Register the permissions callback
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean cameraGranted = result.getOrDefault(Manifest.permission.CAMERA, false);
                    if (cameraGranted != null && cameraGranted) {
                        // Permission is granted. Continue the action or workflow in your app.
                        Profile_Utils.startScanCardActivity(requireActivity());
                    } else {
                        // Explain to the user that the feature is unavailable because the
                        // features requires a permission that the user has denied.
                        Profile_Utils.handlePermissionDenied(requireActivity());
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Get user details
        SharedPreferences sharedpreferences = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String username = sharedpreferences.getString("username", "Not found");
        String email = sharedpreferences.getString("email", "Not found");
        String image = sharedpreferences.getString("profile_image", "Not found");
        boolean isAdmin = sharedpreferences.getBoolean("isAdmin", false);

        // Display user details
        ImageView profile_pic = view.findViewById(R.id.profile_pic);
        if (image.equals("Not found")) {
            // Set default profile image
            profile_pic.setImageResource(R.drawable.round_account_circle_24);
        } else {
            // Load profile image form URL
            Picasso.get().load(image).into(profile_pic);
        }
        ((TextView) view.findViewById(R.id.name)).setText(username);
        ((TextView) view.findViewById(R.id.admin_email)).setText(email);

        int visibility = isAdmin ? View.GONE : View.VISIBLE;
        view.findViewById(R.id.admin).setVisibility(visibility);

        // Listeners
        view.findViewById(R.id.sign_out).setOnClickListener(v -> Profile_Utils.signOut(requireActivity(), googleSignInClient, socketService));
        view.findViewById(R.id.admin).setOnClickListener(v -> Profile_Utils.showAddAdminDialog(requireContext(), profileAPI));
        view.findViewById(R.id.scan_card).setOnClickListener(v -> requestPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA}));

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) context;
            SocketServiceManager socketServiceManager = mainActivity.getSocketServiceManager();
            socketService = socketServiceManager.getSocketService();
        }
    }
}