package com.example.clarivate_employee_privilege;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clarivate_employee_privilege.authentication.SignInActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ImageView img = findViewById(R.id.img);
        Picasso.get().load("https://lh5.googleusercontent.com/p/AF1QipNMQVtmIuSIiGdzilPPpVGoFBEa-mKyUX3XUCyS=w408-h306-k-no").into(img);

        user_Authentication ();

        //Recreate GoogleSignInClient
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        //Listeners
        findViewById(R.id.sign_out).setOnClickListener(v -> signOut());
    }

    //SignOut
    private void signOut() {
        googleSignInClient.signOut()
                .addOnCompleteListener(this, task -> {
                    //Redirect user to signIn activity
                    user_Authentication();
                });
    }

    //Check user sign in status if not sign in redirect to sign in page
    private void user_Authentication (){
        GoogleSignInAccount acc = GoogleSignIn.getLastSignedInAccount(this);
        if (acc == null){
            Intent i = new Intent(this, SignInActivity.class);
            startActivity(i);
            finish();
        }
    }


}
