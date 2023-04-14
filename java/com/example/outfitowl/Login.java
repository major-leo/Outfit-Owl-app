package com.example.outfitowl;

import static com.example.outfitowl.signUp.decodeEmail;
import static com.example.outfitowl.signUp.encodeEmail;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
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
                AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                View dialogViewForgot = getLayoutInflater().inflate(R.layout.dialog_forgotpassword, null);
                View dialogViewUser = getLayoutInflater().inflate(R.layout.dialog_forgotusername, null);
                EditText emailBox = dialogViewForgot.findViewById(R.id.emailBox);
                TextView userInfo = dialogViewUser.findViewById(R.id.usernameInfo);

                builder.setView(dialogViewForgot);
                AlertDialog dialog = builder.create();

                if(dialog.getWindow() != null){
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

                }
                dialog.show();

                dialog.findViewById(R.id.resetButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String userEmail = emailBox.getText().toString().trim();

                        if(TextUtils.isEmpty(userEmail) &&  !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()){
                            Toast.makeText(Login.this,"Enter your registered email address", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        FirebaseAuth.getInstance().sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    dialog.dismiss();
                                    String encodedEmail = encodeEmail(userEmail);
                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("user");
                                    Query checkUserDatabase = reference.orderByChild("email").equalTo(encodedEmail);
                                    checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.exists()){
                                                String getUsername = null;
                                                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                                    String eachEmail = userSnapshot.child("email").getValue(String.class);
                                                    if (Objects.equals(eachEmail, encodedEmail)) {
                                                        getUsername = userSnapshot.child("username").getValue(String.class);
                                                        break;
                                                    }
                                                }

                                                if (getUsername != null) {
                                                    getUsername += " please check your email for a password reset link.";
                                                    userInfo.setText(getUsername);
                                                } else {
                                                    Toast.makeText(Login.this, "Username does not exist", Toast.LENGTH_SHORT).show();
                                                }
                                                userInfo.setText(getUsername);
                                                builder.setView(dialogViewUser);
                                                AlertDialog dialog = builder.create();

                                                if(dialog.getWindow() != null){
                                                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

                                                }
                                                dialog.show();

                                                dialog.findViewById(R.id.okButton).setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                            }else{
                                                Toast.makeText(Login.this,"Username does not exist", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }else{
                                    Toast.makeText(Login.this,"Unable to send reset link", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                });

                dialogViewForgot.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

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
                    String decodeEmail = decodeEmail(userEmail);
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(decodeEmail, userPassword)
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