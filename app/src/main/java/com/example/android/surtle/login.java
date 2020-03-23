package com.example.android.surtle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class login extends AppCompatActivity {

    private static final int RC_SIGN_IN = 101;
    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    SignInButton signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        Toast.makeText(getApplicationContext(), "Initialization Successful",Toast.LENGTH_SHORT).show();

        signInButton = (SignInButton)findViewById(R.id.googleSignInButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleButtonClicked();
            }
        });
        isUserSignedIn();
    }

    private boolean isUserSignedIn(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            signInSucessful();
            return true;
        } else {
            // No user is signed in
            signInFailed();
            return false;
        }
    }

    private void signInSucessful(){
        Log.d("TAG", "signInWithCredential:success");
        FirebaseUser user = mAuth.getCurrentUser();

        Intent i = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(i);
        finish();
        Toast.makeText(getApplicationContext(), "User logged in successfully",Toast.LENGTH_SHORT).show();
        //update userData to UserInfo Class
        UserInfo userInfo = (UserInfo) getApplication();
        userInfo.setUid(user.getUid());
        userInfo.setEmail(user.getEmail());
        userInfo.setName(user.getDisplayName());
        userInfo.setPhotoUrl(user.getPhotoUrl());
    }

    private void signInFailed(){
        Log.w("TAG", "signInWithCredential:failure");
        Toast.makeText(getApplicationContext(), "Could not log in",Toast.LENGTH_SHORT).show();
    }

    public void googleButtonClicked(){
        Toast.makeText(getApplicationContext(), "GoogleSignIn Button Clicked",Toast.LENGTH_SHORT).show();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("authentication", "Google sign in failed");
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("TAG", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                           signInSucessful();
                        } else {
                            // If sign in fails, display a message to the user.
                            signInFailed();
                        }

                        // ...
                    }
                });
    }

}
