package com.example.android.surtle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;


public class Profile extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        updateUserInfoUI();

        //set buttonOnClickListener to Start Scanner Activity
        Button button = (Button) findViewById(R.id.scanButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Boolean test = false;
                Intent i = new Intent(getApplicationContext(),Scanner.class);

                if (test){
                    //start rate Activity with preset scan result
                    i = new Intent(getApplicationContext(), rate.class);
                    i.putExtra("scanResult", "8901057510028");
                }
                startActivity(i);
                finish();
            }
        });

        //firebase media database
        FirebaseStorage storage = FirebaseStorage.getInstance();

        //get sign_out_button reference
        Button sign_out_button = (Button) findViewById(R.id.sign_out_button);
        sign_out_button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                FirebaseAuth.getInstance().signOut();
                if (FirebaseAuth.getInstance().getUid() == null){
                    Log.d("session", "logged user out sucessfully");
                    //end this activity
                    finish();
                }else{
                    Log.d("session", "failed to log out");
                }

            }
        });


    }

    void updateUserInfoUI(){
        //update username
        UserInfo userInfo = (UserInfo) this.getApplication();
        TextView nameTextView = (TextView)findViewById(R.id.usernameTextView);
        nameTextView.setText(userInfo.getName());

        //update profile picture
//        ImageView profilePicture = (ImageView)findViewById(R.id.profilePictureImageView);
//        new Do
//        profilePicture.setImage
    }


}
