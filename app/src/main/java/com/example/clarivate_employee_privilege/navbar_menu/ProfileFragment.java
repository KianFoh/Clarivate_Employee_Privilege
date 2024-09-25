package com.example.clarivate_employee_privilege.navbar_menu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
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
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
    private View view; // Declare a member variable for the View

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);

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
        ((TextView)view.findViewById(R.id.admin_email)).setText(email);

        int visibility = isAdmin ? View.GONE : View.VISIBLE;
        view.findViewById(R.id.admin).setVisibility(visibility);


        // Listeners
        view.findViewById(R.id.sign_out).setOnClickListener(v -> signOut());
        view.findViewById(R.id.admin).setOnClickListener(v -> showAddAdminDialog());

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

    // Show Add Admin Dialog
    private void showAddAdminDialog() {
        // Inflate the add_admin_form.xml layout
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.admin_manage, null);

        // Create the AlertDialog with custom style
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.CustomDialog);
        builder.setView(dialogView);

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Set up the close button
        ImageButton close = dialogView.findViewById(R.id.toast_close);
        close.setOnClickListener(v -> dialog.dismiss());

        // Set up the add admin button
        Button submit = dialogView.findViewById(R.id.add_admin);
        submit.setOnClickListener(v -> {
            String email = ((TextInputLayout)dialogView.findViewById(R.id.admin_email)).getEditText().getText().toString();
            Log.d("EMAIL", email);
            add_admin(email, dialog);
        });

        // Set up the remove admin button
        Button remove = dialogView.findViewById(R.id.remove_admin);
        remove.setOnClickListener(v -> {
            String email = ((TextInputLayout)dialogView.findViewById(R.id.admin_email)).getEditText().getText().toString();
            Log.d("EMAIL", email);
            remove_admin(email, dialog);
        });

        // Set the dialog window size to custom width and height
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    // Add Admin
    private void add_admin(String body, AlertDialog dialog) {
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
                    Context context = requireActivity();
                    String message = "Failed to add admin";
                    ToastUtils.showToast(context, message, false);
                });
            }

            @Override
            public void handleSuccessResponse(Response response) {
                Log.d("API_CALL_ADD_ADMIN", "Admin added successfully");
                // Show success message
                requireActivity().runOnUiThread(() -> {
                    Context context = requireActivity();
                    String message = body + " added as Admin";
                    ToastUtils.showToast(context, message, true);
                    dialog.dismiss();
                });
            }

            @Override
            public void handleFailResponse(Response response, String responseBody) {
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                String error = jsonObject.get("error").getAsString();
                Log.e("API_CALL_ADD_ADMIN", "API call failed: " + error);
                requireActivity().runOnUiThread(() -> {
                    TextInputLayout email_v = dialog.findViewById(R.id.admin_email);
                    email_v.setError(error);
                });
            }
        });
    }

    private void remove_admin(String email, AlertDialog dialog) {
        Headers headers = new Headers.Builder()
                .add("Authorization", "Bearer " + requireActivity()
                        .getSharedPreferences("user_info", Context.MODE_PRIVATE)
                        .getString("google_idToken", ""))
                .build();

        // Build the URL with the email as a query parameter
        String url = getString(R.string.api_url) + "/remove_admin?email=" + email;

        Request request = new Request.Builder()
                .url(url)
                .delete()
                .headers(headers)
                .build();

        CallAPI.getClient().newCall(request).enqueue(new CustomCallback(requireActivity(), request) {

            @Override
            public void onFailure(Call call, IOException e) {
                requireActivity().runOnUiThread(() -> {
                    // Show fail API call message
                    Log.d("ERROR", e.toString());
                    Context context = requireActivity();
                    String message = "Failed to remove admin";
                    ToastUtils.showToast(context, message, false);
                });
            }

            @Override
            public void handleSuccessResponse(Response response) {
                Log.d("API_CALL_REMOVE_ADMIN", "Admin removed successfully");
                // Show success message
                requireActivity().runOnUiThread(() -> {
                    Context context = requireActivity();
                    String message = email + " removed from Admin";
                    ToastUtils.showToast(context, message, true);
                    dialog.dismiss();
                });
            }

            @Override
            public void handleFailResponse(Response response, String responseBody) {
                JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
                String error = jsonObject.get("error").getAsString();
                Log.e("API_CALL_REMOVE_ADMIN", "API call failed: " + error);
                requireActivity().runOnUiThread(() -> {
                    TextInputLayout email_v = dialog.findViewById(R.id.admin_email);
                    email_v.setError(error);
                });
            }
        });
    }
}