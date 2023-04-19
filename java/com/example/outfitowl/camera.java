package com.example.outfitowl;

import static com.example.outfitowl.signUp.encodeEmail;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class camera extends AppCompatActivity {
    // Declare variables
    private Button uploadClothing;
    private ImageView uploadImage;
    private EditText itemName;
    private Spinner spinner;
    private String itemType;
    private ProgressBar progressBar;
    private Uri imageUri;
    private String userEmail = null;
    private String userName = null;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("wardrobe");
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Initializes user input components and views
        uploadClothing = findViewById(R.id.uploadClothing);
        uploadImage = findViewById(R.id.uploadImage);
        itemName = findViewById(R.id.itemName);
        spinner = findViewById(R.id.itemType);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        uploadImage.setVisibility(View.VISIBLE);

        // Get current user's email
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            userEmail =  encodeEmail(currentUser.getEmail());
        }
        //get username from user email
        if (userEmail != null) {
            getUsername(userEmail, new OnUsernameRetrievedListener() {
                @Override
                public void onUsernameRetrieved(String username) {
                    userName = username;
                }
            });
        }

        // Set up photoPicker and uploader for if camera permission is denied
        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK){
                            Intent data = result.getData();
                            imageUri = data.getData();
                            uploadImage.setImageURI(imageUri);
                        }else{
                            Toast.makeText(camera.this, "No Image Selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Set click listeners for image upload and clothing upload
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasCameraPermission()) {
                    ImagePicker.Companion.with(camera.this)
                            .crop()
                            .compress(1024)
                            .maxResultSize(1080,1080)
                            .start();
                }else{
                    Intent photoPicker = new Intent();
                    photoPicker.setAction(Intent.ACTION_GET_CONTENT);
                    photoPicker.setType("image/*");
                    activityResultLauncher.launch(photoPicker);
                }

            }
        });

        //upload image and image details to firebase
        uploadClothing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageUri != null){
                    uploadToFirebase(imageUri, itemType);
                }else{
                    Toast.makeText(camera.this, "Please Select an Image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //when enter button is clicked close the keyboard
        itemName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Close the keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
                return false;
            }

        });

        // Set up item type spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinner_items, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        //get spinner value
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                itemType = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //set default item type to be others
                itemType = parent.getItemAtPosition(0).toString();
            }
        });



        //set up bottom navigation to switch between pages
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottom_camera);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.bottom_wardrobe) {
                startActivity(new Intent(getApplicationContext(), wardrobe.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.bottom_outfits) {
                startActivity(new Intent(getApplicationContext(), outfits.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.bottom_camera) {
                return true;
            } else if (itemId == R.id.bottom_social) {
                startActivity(new Intent(getApplicationContext(), social.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (itemId == R.id.bottom_profile) {
                startActivity(new Intent(getApplicationContext(), profile.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }
            return false;
        });
    }

    //method to upload image to firebase and image data to firebase realtime
    private void uploadToFirebase(Uri uri, String type){
        String name = itemName.getText().toString();
        FirebaseStorage.getInstance().getReference("itemImage/"+ UUID.randomUUID().toString()).putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    task.getResult().getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.isSuccessful()){
                                //if uploading image is successful upload data to firebase realtime database
                                imageData ImageData = new imageData(task.getResult().toString(), name);
                                databaseReference.child(userName).child(type).setValue(ImageData);

                            }
                        }
                    });
                    Toast.makeText(camera.this,"Image Uploaded successfully", Toast.LENGTH_SHORT).show();
                    uploadImage.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(camera.this, wardrobe.class);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(camera.this,"Image Uploaded failed", Toast.LENGTH_SHORT).show();
                }
                uploadImage.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
            }
            //set progress bar
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                uploadImage.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.INVISIBLE);
                uploadImage.setVisibility(View.VISIBLE);
                Toast.makeText(camera.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });;

    }

    //method for checking if app has camera permissions
    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    // Set up image Picker and uploader for if camera permission is granted
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            // Get the Uri for the selected image
            assert data != null;
            imageUri = data.getData();

            // Use the Uri to load the image, e.g., with an ImageView
            uploadImage.setImageURI(imageUri);
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            // Handle the error
            Toast.makeText(this, ImagePicker.Companion.getError(data), Toast.LENGTH_SHORT).show();
        }
    }

    //methods to get the current user's usernames
    public interface OnUsernameRetrievedListener {
        void onUsernameRetrieved(String username);
    }

    private void getUsername(String email, OnUsernameRetrievedListener listener) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("user");
        Query query = usersRef.orderByChild("email").equalTo(email);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String username = userSnapshot.child("username").getValue(String.class);
                    listener.onUsernameRetrieved(username);
                    break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "Error: ", error.toException());
            }
        });
    }
}