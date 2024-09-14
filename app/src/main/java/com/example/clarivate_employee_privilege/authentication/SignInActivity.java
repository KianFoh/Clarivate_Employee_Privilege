package com.example.clarivate_employee_privilege.authentication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clarivate_employee_privilege.MainActivity;
import com.example.clarivate_employee_privilege.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class SignInActivity extends AppCompatActivity {

    private GoogleSignInClient googleSignInClient ;
    private ActivityResultLauncher<Intent> google_signInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);

        //<-----------------------------------------Google SignIn---------------------------------->
        //Configure Sign in to request user data eg. ID, Email etc
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();

        //Build GoogleSignInClient with the configuration
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        //Activity Launcher then get data from result
        google_signInLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                // Handle the result
                Intent data = result.getData();
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);
            }
        });

        //Listeners
        findViewById(R.id.google_sign_in_button).setOnClickListener(v -> google_signIn());
    }

    //<-----------------------------------------Google SignIn---------------------------------->
    //Intent Google SignIn interface
    private void google_signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        google_signInLauncher.launch(signInIntent);
    }

    //Check for SignIn Error, Get Data from SignIn acc, Verify Email Domain
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            String email = account.getEmail();

            //Check if the email domain matches Clarivate Email
            if (email != null && email.endsWith("@gmail.com")) {
                //Domain match
                redirectToMainActivity();
            } else {
                //Domain does not match
                Toast.makeText(this, "Sign-in is restricted to Clarivate Employee email only.", Toast.LENGTH_SHORT).show();
                //Sign the user out
                googleSignInClient.signOut();
            }

        } catch (ApiException e) {
            String error = e.toString();
            Log.d("ERROR", error);
            // Handle sign-in failure
            Toast.makeText(this, "Sign-in failed :" + error, Toast.LENGTH_SHORT).show();
        }
    }

    //Intent to MainActivity
    private void redirectToMainActivity() {
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}