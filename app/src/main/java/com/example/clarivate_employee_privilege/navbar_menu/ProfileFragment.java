package com.example.clarivate_employee_privilege.navbar_menu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.clarivate_employee_privilege.MainActivity;
import com.example.clarivate_employee_privilege.R;
import com.example.clarivate_employee_privilege.api.CallAPI;
import com.example.clarivate_employee_privilege.api.CustomCallback;
import com.example.clarivate_employee_privilege.api.SocketService;
import com.example.clarivate_employee_privilege.api.SocketServiceManager;
import com.example.clarivate_employee_privilege.authentication.SignInActivity;
import com.example.clarivate_employee_privilege.utils.ToastUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileFragment extends Fragment {

    private GoogleSignInClient googleSignInClient;
    private SocketService socketService;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize GoogleSignInClient
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);


        // Get user details
        SharedPreferences sharedpreferences = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String username = sharedpreferences.getString("username", "Not found");
        String email = sharedpreferences.getString("email", "Not found");
        String image = sharedpreferences.getString("profile_image", "Not found");
        boolean isAdmin = sharedpreferences.getBoolean("isAdmin", false);

        // Display user details
        ImageView profile_pic = view.findViewById(R.id.profile_pic);
        if (image.equals("Not found")){
            // Set default profile image
            profile_pic.setImageResource(R.drawable.round_account_circle_24);
        }
        else {
            // Load profile image form URL
            Picasso.get().load(image).into(profile_pic);
        }
        ((TextView)view.findViewById(R.id.name)).setText(username);
        ((TextView)view.findViewById(R.id.email)).setText(email);

        int visibility = isAdmin ? View.GONE : View.VISIBLE;
        view.findViewById(R.id.admin_form).setVisibility(visibility);


        // Listeners
        view.findViewById(R.id.sign_out).setOnClickListener(v -> signOut());
        view.findViewById(R.id.add_admin).setOnClickListener(v -> add_admin(
                ((EditText) view.findViewById(R.id.admin_email)).getText().toString()
        ));

        return view;
    }

    // Get the SocketService instance from the MainActivity
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) context;
            SocketServiceManager socketServiceManager = mainActivity.getSocketServiceManager();
            socketService = socketServiceManager.getSocketService();
        }
    }

    // SignOut
    private void signOut() {

        // Disconnect the socket
        if (socketService != null) {
            socketService.disconnectSocket();
        }

        googleSignInClient.signOut()
                .addOnCompleteListener(requireActivity(), task -> {

                    // Redirect user to signIn activity
                    Intent i = new Intent(requireActivity(), SignInActivity.class);
                    startActivity(i);
                    requireActivity().finish();

                });
    }

    // Add Admin
    private void add_admin(String body) {

        Headers headers = new Headers.Builder()
                .add("Authorization", "Bearer " + requireActivity()
                    .getSharedPreferences("user_info", Context.MODE_PRIVATE)
                    .getString("google_idToken", ""))
                .build();

        Request request = new Request.Builder()
                .url(getString(R.string.api_url) + "/add_admin")
                .post(RequestBody.create(body, MediaType.get("application/json; charset=utf-8")))
                .headers(headers)
                .build();

        CallAPI.getClient().newCall(request).enqueue(new CustomCallback(requireActivity(), request) {

            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() -> {
                    // Show fail api call message
                    Log.d("ERROR", e.toString());
                    requireActivity().runOnUiThread(() -> {
                        Context context = requireActivity();
                        String message = "Failed to add admin";
                        ToastUtils.showToast(context, message);
                    });
                });
            }
            @Override
            public void handleResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Show success message
                    requireActivity().runOnUiThread(() -> {
                        Context context = requireActivity();
                        String message = body + " added as Admin";
                        ToastUtils.showToast(context, message);
                    });
                }
                else {
                    // show repose error
                    Log.e("API_CALL", "API call failed: " + response.message());
                    requireActivity().runOnUiThread(() -> {
                        Context context = requireActivity();
                        String message = "Failed to add admin: " + response.message();
                        ToastUtils.showToast(context, message);
                    });
                }
            }
        });

    }
}