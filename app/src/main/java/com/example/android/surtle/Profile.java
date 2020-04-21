package com.example.android.surtle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


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
                Intent i = new Intent(getApplicationContext(),Scanner.class);
                startActivity(i);
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
