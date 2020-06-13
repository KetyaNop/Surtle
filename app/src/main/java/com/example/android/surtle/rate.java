package com.example.android.surtle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.zagum.switchicon.SwitchIconView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class rate extends AppCompatActivity {
    Boolean buttonState = true;//change true to products.[productCode].vote.[UID] if products.[productCode].vote.[UID] !== null
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        //getUserID
        UserInfo userInfo = (UserInfo) this.getApplication();
        String UID = userInfo.getUid();

        //get Product Code
        String productCode = getIntent().getStringExtra("scanResult");

        Log.d("Initialization", "UID: " + UID + ", userInfo: " + userInfo+", productCode: "+productCode);

        //connect to Firebase Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //get productReference
        final DatabaseReference productRef = database.getReference("products").child(productCode);
        //get userVote Reference
        final DatabaseReference userVoteRef = database.getReference("products").child(productCode).child("vote").child(UID);

        //getButton Reference
        final SwitchIconView thumbsDownButton = (SwitchIconView)findViewById(R.id.thumbsDownButton);
        final SwitchIconView thumbsUpButton = (SwitchIconView)findViewById(R.id.thumbsUpButton);
        Button submitButton = (Button)findViewById(R.id.submitButton);

        //setOnClickListener for both buttons
        thumbsDownButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buttonState = false;
                updateButtonColor();
            }
        });
        thumbsUpButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buttonState = true;
                updateButtonColor();
            }
        });

        //update database when submit button is clicked
        submitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                userVoteRef.setValue(buttonState);
                finish();
            }
        });

        //get total number of vote
        // Read from the database
        productRef.child("vote").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                //get products.[productCode].vote and store in Object value
                Object value = dataSnapshot.getValue();

                //add null handler
                if (value != null){
                    Log.d("productRef", "Value is: " + value.toString());

                    //cast Object value to Map<String, Boolean>
                    Map<String, Boolean> map = (Map<String, Boolean>) value;

                    // Get keys and values
                    //get total votes and total positive votes
                    int totalVotes = 0;
                    int positiveVotes = 0;
                    for (Map.Entry<String, Boolean> entry : map.entrySet()) {
                        String k = entry.getKey();
                        Boolean v = entry.getValue();
                        Log.d("productRef", "Key: " + k + ", Value: " + v);
                        totalVotes++; //update number of total vote
                        if (v)
                            positiveVotes++; //update number of total positive vote
                        updateRatingDisplayUI(positiveVotes, totalVotes); //update the UI for displaying the rating
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("productRef", "Failed to read value.", error.toException());
            }
        });


        ImageView imageView = (ImageView)this.findViewById(R.id.product_image_view);

        //set event listener for add_image_button
        Button add_image_button = (Button) findViewById(R.id.add_image_button);
        add_image_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //send intent to take photo
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    void updateButtonColor(){
        //getButton Reference
        final SwitchIconView thumbsDownButton = (SwitchIconView)findViewById(R.id.thumbsDownButton);
        final SwitchIconView thumbsUpButton = (SwitchIconView)findViewById(R.id.thumbsUpButton);

        Log.d("ButtonColor", buttonState.toString());
        if (buttonState){
            thumbsUpButton.setIconEnabled(true);
            thumbsDownButton.setIconEnabled(false);
        }else {
            thumbsUpButton.setIconEnabled(false);
            thumbsDownButton.setIconEnabled(true);
        }
    }

    void updateRatingDisplayUI(int numPosVotes, int totalVotes){
        //get ratingTextView reference
        TextView ratingTextView = (TextView)findViewById(R.id.ratingTextView);
        ratingTextView.setText(Integer.toString(numPosVotes)+"/"+ Integer.toString(totalVotes));
    }
}
