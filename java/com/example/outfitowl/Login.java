package com.example.outfitowl;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.ktx.Firebase;

import java.util.Objects;

public class Login extends AppCompatActivity {

    private EditText Username;
    private EditText Password;
    private Button LoginButton;
    private TextView SignUp;
    private TextView ForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Username = findViewById(R.id.username);
        Password = findViewById(R.id.password);
        LoginButton = findViewById(R.id.loginButton);

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateUsername() || validatePassword()){
                    checkUser();
                }
            }
        });

        SignUp = findViewById(R.id.signupText);

        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, signUp.class));
            }
        });

        ForgotPassword = findViewById(R.id.forgotPassword);

        ForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, forgotPassword.class));
            }
        });

    }

    private Boolean validateUsername(){
        String val = Username.getText().toString();
        if(val.isEmpty()){
            Toast.makeText(Login.this,"Username cannot be empty", Toast.LENGTH_SHORT).show();
            Username.setBackground(ContextCompat.getDrawable(Login.this, R.drawable.custom_textbar_error));
            return false;
        }else{
            Username.setBackground(ContextCompat.getDrawable(Login.this, R.drawable.custom_textbar));
            return true;
        }
    }

    private Boolean validatePassword(){
        String val = Password.getText().toString();
        if(val.isEmpty()){
            Toast.makeText(Login.this,"Password cannot be empty", Toast.LENGTH_SHORT).show();
            Password.setBackground(ContextCompat.getDrawable(Login.this, R.drawable.custom_textbar_error));
            return false;
        }else{
            return true;
        }
    }

    private void checkUser(){
        String userUsername = Username.getText().toString().trim();
        String userPassword = Password.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("user");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);
        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Username.setBackground(ContextCompat.getDrawable(Login.this, R.drawable.custom_textbar));
                    String userEmail = snapshot.child(userUsername).child("email").getValue(String.class);
                    if (userEmail == null){
                        Toast.makeText(Login.this, "Email address does not exist.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(userEmail, userPassword)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Password.setBackground(ContextCompat.getDrawable(Login.this, R.drawable.custom_textbar));
                                        // Sign in success
                                        Toast.makeText(Login.this, "Login successful.", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(Login.this, wardrobe.class));
                                    }else{
                                        // Sign in fails
                                        try {
                                            throw Objects.requireNonNull(task.getException());
                                        } catch (FirebaseAuthInvalidUserException e) {
                                            Toast.makeText(Login.this, "Invalid email address.", Toast.LENGTH_SHORT).show();
                                        } catch (FirebaseAuthInvalidCredentialsException e) {
                                            Toast.makeText(Login.this, "Incorrect password.", Toast.LENGTH_SHORT).show();
                                            Password.setBackground(ContextCompat.getDrawable(Login.this, R.drawable.custom_textbar_error));
                                        } catch (Exception e) {
                                            Toast.makeText(Login.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                }
                            });
                }else{
                    Toast.makeText(Login.this,"Username does not exist", Toast.LENGTH_SHORT).show();
                    Username.setBackground(ContextCompat.getDrawable(Login.this, R.drawable.custom_textbar_error));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}