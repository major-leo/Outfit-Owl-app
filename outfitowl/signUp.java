package com.example.outfitowl;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.core.content.IntentCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ktx.Firebase;

public class signUp extends AppCompatActivity {

    private EditText displayName;
    private EditText email;
    private EditText username;
    private EditText password;
    private EditText confirmPassword;
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
                    confirmPassword.setTextColor(ContextCompat.getColor(signUp.this, R.color.error_color));
                } else {
                    confirmPassword.setTextColor(ContextCompat.getColor(signUp.this, R.color.black));
                }
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
                    return;
                }

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
}