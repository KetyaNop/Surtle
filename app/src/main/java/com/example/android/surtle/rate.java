package com.example.android.surtle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.zagum.switchicon.SwitchIconView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


public class rate extends AppCompatActivity {
    Boolean buttonState = true;//change true to products.[productCode].vote.[UID] if products.[productCode].vote.[UID] !== null
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private StorageReference rootStorageRef;
    FirebaseDatabase database = FirebaseDatabase.getInstance();

    String pCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        //getUserID
        UserInfo userInfo = (UserInfo) this.getApplication();
        String UID = userInfo.getUid();

        //get Product Code
        String productCode = getIntent().getStringExtra("scanResult");
        pCode = productCode;

        //get alternative product code
        String alternative_product_code = getIntent().getStringExtra("alternative_product_code");

        //get product_imageView reference
        final ImageView product_imageView = (ImageView) findViewById(R.id.product_image_view);
        //imageFileName reference
        DatabaseReference imageFileNameRef = database.getReference("products").child(pCode).child("imageFileName");
        // Read from the database
        imageFileNameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                if (value != null){
                    //download file
                    StorageReference fileRef = rootStorageRef.child(value);
                    File localFile = null;
                    try {
                        localFile = File.createTempFile("images", "jpg");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    final File finalLocalFile = localFile;

                    fileRef.getFile(localFile)
                            .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    // Successfully downloaded data to local file
                                    // break image to bitMap
                                    Bitmap myBitmap = BitmapFactory.decodeFile(finalLocalFile.getAbsolutePath());
                                    product_imageView.setImageBitmap(myBitmap);
                                    // hide add image button
                                    Button add_image_button = (Button)findViewById(R.id.add_image_button);
                                    add_image_button.setVisibility(View.GONE);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle failed download
                            // ...
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("database", "Failed to read value.", error.toException());
            }
        });

        Log.d("Initialization", "UID: " + UID + ", userInfo: " + userInfo+", productCode: "+productCode);

        //get productReference
        final DatabaseReference productRef = database.getReference("products").child(productCode);
        //get userVote Reference
        final DatabaseReference userVoteRef = database.getReference("products").child(productCode).child("vote").child(UID);
        //
        if (alternative_product_code != null){
            //add alternative_product code to current branch
            productRef.child("alternative").child(alternative_product_code).child(UID).setValue(true);
            //add product key to alternative_product branch
            final DatabaseReference alternative_product_databaseRef = database.getReference("products").child(alternative_product_code);
            alternative_product_databaseRef.child("alternative").child(productCode).child(UID).setValue(true);
        }

        //getButton Reference
        final SwitchIconView thumbsDownButton = (SwitchIconView)findViewById(R.id.thumbsDownButton);
        final SwitchIconView thumbsUpButton = (SwitchIconView)findViewById(R.id.thumbsUpButton);
        Button submitButton = (Button)findViewById(R.id.add_alternative_button);

        //setOnClickListener for both buttons
        thumbsDownButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buttonState = false;
                updateButtonColor();
                userVoteRef.setValue(buttonState);
            }
        });
        thumbsUpButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buttonState = true;
                updateButtonColor();
                userVoteRef.setValue(buttonState);
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

        //set event listener for add_image_button
        Button add_image_button = (Button) findViewById(R.id.add_image_button);
        add_image_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //send intent to take photo
                dispatchTakePictureIntent();
            }
        });

        //initialize cloud storage reference
        rootStorageRef = FirebaseStorage.getInstance().getReference();

        //get add alternative button
        Button add_simillar_button = (Button) findViewById(R.id.add_alternative_button);
        add_simillar_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //start scanning intent with message of this product
                Log.d("add_alternative", "add alternative button clicked");
                Log.d("add_alternative", "starting scanner");
                Intent i = new Intent(getApplicationContext(), Scanner.class);
                i.putExtra("productCode", pCode);
                startActivity(i);
            }
        });

        //add alternative products to the ui
        //loop though products.[Product Code].alternative

    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            uploadImagetoFirebase(imageBitmap);

            ImageView product_imageView = (ImageView) findViewById(R.id.product_image_view);
            //display new image to product image view
            product_imageView.setImageBitmap(imageBitmap);
        }
    }

    private String createFileName(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        return imageFileName;
    }

    private void uploadImagetoFirebase(Bitmap bitmap){
        //get fileName
        final String fileName = pCode+"/"+createFileName()+".jpg";
        // Create a storage reference from our app
        StorageReference storageRef = rootStorageRef;

        // Create a reference to "newfile.jpg"
        StorageReference fileRef = storageRef.child(fileName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //compress bitmaop data to JPEG
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = fileRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.d("database", "unsucessfull upload");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                //upload imageUrl to database
                Log.d("database", "sucessfull upload");
                Log.d("database", "uploading filename: "+fileName+" to: "+"pruducts."+pCode+".imageFileName");
                database.getReference("products").child(pCode).child("imageFileName").setValue(fileName);
            }
        });
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
