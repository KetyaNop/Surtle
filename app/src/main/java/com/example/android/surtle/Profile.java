package com.example.android.surtle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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
//                Intent i = new Intent(getApplicationContext(),Scanner.class);
//                startActivity(i);

                //start rate Activity with preset scan result
                Intent i = new Intent(getApplicationContext(), rate.class);
//                i.putExtra("alternative_product_code", productCode);
                i.putExtra("scanResult", "8901057510028");
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
                //end this activity
                finish();
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
