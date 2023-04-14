package com.example.outfitowl;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;


import android.util.Patterns;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;

public class signUp extends AppCompatActivity {

    // Declare variables
    private EditText displayName;
    private EditText email;
    private EditText username;
    private EditText password;
    private EditText confirmPassword;
    private ImageView profilePic;
    private String profilePicURL = "";
    private Uri imagePath;
    private Button signUpButton;
    private TextView signUpText;
    private FirebaseDatabase database;
    private DatabaseReference reference;


    // Declare ActivityResultLauncher for getting an image from the user's gallery
    private ActivityResultLauncher<Intent> photoActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initializes user input components
        displayName = findViewById(R.id.displayName);
        email = findViewById(R.id.email);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        signUpButton = findViewById(R.id.signUpButton);
        signUpText = findViewById(R.id.signUpText);
        profilePic = findViewById(R.id.profileImg);

        // Initialize ActivityResultLauncher for getting an image from the user's gallery
        photoActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imagePath = result.getData().getData();
                        getImageView();
                    }
                }
        );

        // check if confirm password matches password after every change on confirm password
        confirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String passwordStr = password.getText().toString();
                String confirmPasswordStr = editable.toString();

                if (!confirmPasswordStr.equals(passwordStr)) {
                    confirmPassword.setBackground(ContextCompat.getDrawable(signUp.this, R.drawable.custom_textbar_error));
                    confirmPassword.setTextColor(ContextCompat.getColor(signUp.this, R.color.error_color));
                } else {
                    confirmPassword.setBackground(ContextCompat.getDrawable(signUp.this, R.drawable.custom_textbar));
                    confirmPassword.setTextColor(ContextCompat.getColor(signUp.this, R.color.black));
                }
            }
        });

        // Set an OnClickListener for profilePic to choose an image from the gallery
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoIntent = new Intent(Intent.ACTION_PICK);
                photoIntent.setType("image/*");
                photoActivityResultLauncher.launch(photoIntent);

            }
        });

        // Set an OnClickListener for signUpButton to validate input and sign up the user
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Initialize Firebase database and reference
                database = FirebaseDatabase.getInstance();
                reference = database.getReference("user");

                // Get user input from EditTexts
                String name = displayName.getText().toString();
                String emailStr = email.getText().toString();
                String usernameStr = username.getText().toString();
                String passwordStr = password.getText().toString();
                String confirmPasswordStr = confirmPassword.getText().toString();

                // Validate user input and return if not valid
                if (!confirmPasswordStr.equals(passwordStr)) {
                    Toast.makeText(signUp.this,"Passwords need to match", Toast.LENGTH_SHORT).show();
                    confirmPassword.setBackground(ContextCompat.getDrawable(signUp.this, R.drawable.custom_textbar_error));
                    return;
                }else{
                    confirmPassword.setBackground(ContextCompat.getDrawable(signUp.this, R.drawable.custom_textbar));
                }

                if (name.isEmpty()){
                    Toast.makeText(signUp.this,"Display name cannot be empty", Toast.LENGTH_SHORT).show();
                    displayName.setBackground(ContextCompat.getDrawable(signUp.this, R.drawable.custom_textbar_error));
                    return;
                }else{
                    displayName.setBackground(ContextCompat.getDrawable(signUp.this, R.drawable.custom_textbar));
                }

                if (emailStr.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()){
                    Toast.makeText(signUp.this,"Email must be in valid email format", Toast.LENGTH_SHORT).show();
                    email.setBackground(ContextCompat.getDrawable(signUp.this, R.drawable.custom_textbar_error));
                    return;
                }else{
                    email.setBackground(ContextCompat.getDrawable(signUp.this, R.drawable.custom_textbar));
                }

                if (usernameStr.isEmpty()){
                    Toast.makeText(signUp.this,"Username cannot be empty", Toast.LENGTH_SHORT).show();
                    username.setBackground(ContextCompat.getDrawable(signUp.this, R.drawable.custom_textbar_error));
                    return;
                }else{
                    username.setBackground(ContextCompat.getDrawable(signUp.this, R.drawable.custom_textbar));
                }
                if (passwordStr.isEmpty() || passwordStr.length() < 6){
                    Toast.makeText(signUp.this,"Password cannot be empty", Toast.LENGTH_SHORT).show();
                    password.setBackground(ContextCompat.getDrawable(signUp.this, R.drawable.custom_textbar_error));
                    return;
                }else{
                    password.setBackground(ContextCompat.getDrawable(signUp.this, R.drawable.custom_textbar));
                }

                String encodedEmail = encodeEmail(emailStr);

                // Check if the username is unique
                isUsernameUnique(usernameStr, new OnUniqueUsernameCheckListener() {
                    @Override
                    public void onResult(boolean isUnique) {
                        if (!isUnique) {
                            Toast.makeText(signUp.this,"Username is already taken", Toast.LENGTH_SHORT).show();
                            username.setBackground(ContextCompat.getDrawable(signUp.this, R.drawable.custom_textbar_error));
                        }else{
                            // Sign up user with FirebaseAuth
                            username.setBackground(ContextCompat.getDrawable(signUp.this, R.drawable.custom_textbar));
                            FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailStr, passwordStr).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()){
                                        uploadImage(new Runnable() {
                                            @Override
                                            public void run() {
                                                user user = new user(profilePicURL, usernameStr, encodedEmail, name);
                                                reference.child(usernameStr).setValue(user);
                                                Intent intent = new Intent(signUp.this, Login.class);
                                                startActivity(intent);
                                            }
                                        });
                                        Toast.makeText(signUp.this, "Sign Up successful", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(signUp.this, "Sign Up failed: " + Objects.requireNonNull(task.getException()).getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });

        // Set an OnClickListener for signUpText to navigate to the Login activity
        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(signUp.this, Login.class));
            }
        });

    }

    // Method to check if the username is unique in the database
    private void isUsernameUnique(String username, OnUniqueUsernameCheckListener listener) {
        reference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listener.onResult(!snapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Database error occurred: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Interface for the unique username check listener
    public interface OnUniqueUsernameCheckListener {
        void onResult(boolean isUnique);
    }

    // Method to set the selected image from the gallery to the ImageView
    private void getImageView(){
        Bitmap bitmap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        profilePic.setImageBitmap(bitmap);
    }

    // Method to upload the profile image to Firebase Storage
    private void uploadImage(Runnable onSuccess){
        if (imagePath == null) {
            onSuccess.run();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        FirebaseStorage.getInstance().getReference("profileImage/"+ UUID.randomUUID().toString()).putFile(imagePath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    task.getResult().getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.isSuccessful()){
                                profilePicURL = task.getResult().toString();
                                onSuccess.run();
                            }
                        }
                    });
                    Toast.makeText(signUp.this,"Image Uploaded successfully", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(signUp.this,"Image Uploaded failed", Toast.LENGTH_SHORT).show();
                }

                progressDialog.dismiss();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progress = 100.0 * snapshot.getBytesTransferred()/snapshot.getTotalByteCount();
                progressDialog.setMessage("Uploaded "+(int) progress + "%");
            }
        });

    }

    public static String encodeEmail(String email) {
        return email.replace(".", ",");
    }

    public static String decodeEmail(String encodedEmail) {
        return encodedEmail.replace(",", ".");
    }
}