package com.example.outfitowl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import androidx.core.content.IntentCompat;
import android.util.Patterns;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import java.util.regex.Matcher;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ktx.Firebase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

public class signUp extends AppCompatActivity {

    private EditText displayName;
    private EditText email;
    private EditText username;
    private EditText password;
    private EditText confirmPassword;
    private ImageView profilePic;
    private Uri imagePath;
    private Button signUpButton;
    private TextView signUpText;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        displayName = findViewById(R.id.displayName);
        email = findViewById(R.id.email);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        signUpButton = findViewById(R.id.signUpButton);
        signUpText = findViewById(R.id.signUpText);
        profilePic = findViewById(R.id.profileImg);

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

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoIntent = new Intent(Intent.ACTION_PICK);
                photoIntent.setType("image/*");
                startActivityForResult(photoIntent, 1);

            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database = FirebaseDatabase.getInstance();
                reference = database.getReference("user");

                String name = displayName.getText().toString();
                String emailStr = email.getText().toString();
                String usernameStr = username.getText().toString();
                String passwordStr = password.getText().toString();
                String confirmPasswordStr = confirmPassword.getText().toString();

                if (!confirmPasswordStr.equals(passwordStr)) {
                    Toast.makeText(signUp.this,"Passwords need to match", Toast.LENGTH_SHORT).show();
                    confirmPassword.setBackground(ContextCompat.getDrawable(signUp.this, R.drawable.custom_textbar_error));
                    return;
                }

                if (name.isEmpty()){
                    Toast.makeText(signUp.this,"Display name cannot be empty", Toast.LENGTH_SHORT).show();
                    displayName.setBackground(ContextCompat.getDrawable(signUp.this, R.drawable.custom_textbar_error));
                    return;
                }

                if (emailStr.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()){
                    Toast.makeText(signUp.this,"Email must be in valid email format", Toast.LENGTH_SHORT).show();
                    email.setBackground(ContextCompat.getDrawable(signUp.this, R.drawable.custom_textbar_error));
                    return;
                }
                if (usernameStr.isEmpty()){
                    Toast.makeText(signUp.this,"Username cannot be empty", Toast.LENGTH_SHORT).show();
                    username.setBackground(ContextCompat.getDrawable(signUp.this, R.drawable.custom_textbar_error));
                    return;
                }
                if (passwordStr.isEmpty()){
                    Toast.makeText(signUp.this,"Password cannot be empty", Toast.LENGTH_SHORT).show();
                    password.setBackground(ContextCompat.getDrawable(signUp.this, R.drawable.custom_textbar_error));
                    return;
                }
                uploadImage();
                user user = new user("", usernameStr, emailStr, name,passwordStr);
                reference.child(usernameStr).setValue(user);

                Toast.makeText(signUp.this,"Sign Up successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(signUp.this, Login.class);
                startActivity(intent);


            }
        });

        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(signUp.this, Login.class));
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK && data != null){
            imagePath = data.getData();

            getImageView();

        }
    }

    private void getImageView(){
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        profilePic.setImageBitmap(bitmap);
    }

    private void uploadImage(){
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        FirebaseStorage.getInstance().getReference("profileImage/"+ UUID.randomUUID().toString()).putFile(imagePath).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    Toast.makeText(signUp.this,"Image Uploaded successfully", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(signUp.this,"Image Uploaded failed", Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }
        });

    }
}