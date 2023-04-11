package com.example.outfitowl;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
                if (Username.getText().toString().equals("eee")&& Password.getText().toString().equals("eee")){
                    startActivity(new Intent(Login.this, wardrobe.class));
                    finish();
                }else{
                    Toast.makeText(Login.this,"Login failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        SignUp = findViewById(R.id.signupText);
        ForgotPassword = findViewById(R.id.forgotPassword);

        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, signUp.class));
            }
        });


    }
}