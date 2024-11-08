// app/src/main/java/com/example/clarivate_employee_privilege/profile/ProfileFragment.java
package com.example.clarivate_employee_privilege.navbar_menu.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;

import com.example.clarivate_employee_privilege.Main_Activity;
import com.example.clarivate_employee_privilege.R;
import com.example.clarivate_employee_privilege.utils.App_Utils;
import com.example.clarivate_employee_privilege.utils.Permission_Utils;
import com.example.clarivate_employee_privilege.websocket.Event_Bus;
import com.example.clarivate_employee_privilege.websocket.Socket_Service;
import com.example.clarivate_employee_privilege.websocket.Socket_Service_Manager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class Profile_Fragment extends Fragment {

    private GoogleSignInClient googleSignInClient;
    private Socket_Service socketService;
    private Profile_API profileAPI;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private String username, cardId;
    private Boolean previousIsAdmin = null;
    private SharedPreferences sharedpreferences;

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

        // Register the permissions callback using PermissionUtils
        requestPermissionLauncher = Permission_Utils.registerForCameraPermission(
                this,
                () -> Profile_Utils.startScanCardActivity(requireActivity()),
                () -> Permission_Utils.handlePermissionDenied(requireActivity())
        );

        // Get user details
        sharedpreferences = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        username = sharedpreferences.getString("username", "Not found");

        App_Utils.setToolbarTitle(requireActivity(), "Profile");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Get user details
        SharedPreferences sharedpreferences = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String email = sharedpreferences.getString("email", "Not found");
        String image = sharedpreferences.getString("profile_image", "Not found");

        // Display user details
        ImageView profile_pic = view.findViewById(R.id.profile_pic);
        Profile_Utils.loadProfileImage(image, profile_pic);

        ImageView card_pic = view.findViewById(R.id.scan_card);
        Profile_Utils.loadCardImage(cardId, card_pic, requestPermissionLauncher, requireActivity());

        ((TextView) view.findViewById(R.id.profile_name)).setText(App_Utils.truncateText(username, 15));
        ((TextView) view.findViewById(R.id.profile_email)).setText(email);

        // Listeners
        view.findViewById(R.id.profile_signout).setOnClickListener(v -> {
            Profile_Utils.signOut(requireActivity(), googleSignInClient, socketService);
        });
        requireActivity().findViewById(R.id.toolbar_more).setOnClickListener(v -> showPopupMenu(v));

        observeEventBus();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (previousIsAdmin) {
            requireActivity().findViewById(R.id.toolbar_more).setVisibility(View.VISIBLE);
        }

        Log.d("ProfileFragment", "Reloading image from URL: " + cardId);

        sharedpreferences = requireActivity().getSharedPreferences("name_card " + username, Context.MODE_PRIVATE);
        cardId = sharedpreferences.getString("card_id", "Not found");

        ImageView card_pic = getView().findViewById(R.id.scan_card);
        Profile_Utils.loadCardImage(cardId, card_pic, requestPermissionLauncher, requireActivity());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Main_Activity) {
            Main_Activity mainActivity = (Main_Activity) context;
            Socket_Service_Manager socketServiceManager = mainActivity.getSocketServiceManager();
            socketService = socketServiceManager.getSocketService();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        requireActivity().findViewById(R.id.toolbar_more).setVisibility(View.GONE);
    }

     private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.profile_manageadmin:
                        Profile_Utils.showAddAdminDialog(requireContext(), profileAPI);
                        return true;
                    case R.id.profile_downloadrequests:
                        Profile_API.downloadRequests(requireContext());
                        return true;
                    default:
                        return false;
                }
            }
        });
        popupMenu.show();
    }

    public void observeEventBus() {
        // Observe the admin status updates
        // Check if the isAdmin status has changed
        Event_Bus.getInstance().getIsadminLiveData().observe(getViewLifecycleOwner(), isAdmin -> {
            if (previousIsAdmin == null || !previousIsAdmin.equals(isAdmin)) {
                previousIsAdmin = isAdmin; // Update the previous isAdmin status
                getActivity().runOnUiThread(() -> {
                    View view = getView();
                    if (isAdmin) {
                        getActivity().findViewById(R.id.toolbar_more).setVisibility(View.VISIBLE);
                        ((TextView) view.findViewById(R.id.user_label)).setText("Admin");
                        Log.d("ProfileFragment", "Admin Status Update");
                    } else {
                        ((TextView) view.findViewById(R.id.user_label)).setText("Employee");
                        Log.d("ProfileFragment", "Employee Status UI updated");
                    }
                });
            }
        });
    }
}