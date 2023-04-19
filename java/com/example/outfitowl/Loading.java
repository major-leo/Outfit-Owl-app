package com.example.outfitowl;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class Loading extends AppCompatActivity {
    //camera request code to see if permission has been granted
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    //storage request code to see if permission has been granted
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 102;
    //boolean to see if the reasoning box has been shown to the user
    private boolean showRationale = false;
    //counter for how many times the program has attempted to request for camera permission
    private int counter = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        if (hasCameraPermission()) {
            //checks if camera permission has been granted
            Toast.makeText(this, "Camera Permission Already Granted", Toast.LENGTH_SHORT).show();
            navigateToLoginActivity();
        } else {
            showCameraPermissionExplanation();
        }
    }

    //progress to the login screen function
    private void navigateToLoginActivity() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(Loading.this, Login.class));
                finish();
            }
        }, 1000);
    }

    //function to check if camera permissions have been granted
    private boolean hasCameraPermission() {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }


    //function to request for camera permissions
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST_CODE);
    }

    //method to checks if camera permission has been denied if so show reasoning for camera
    private void showCameraPermissionExplanation() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            showRationale = true;
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("Outfit Owl uses the camera to easily add new clothing items to your digital wardrobe. If you prefer not to grant camera access, you can still upload images from your device. To re-enable camera permissions in the future, please visit the app settings")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestCameraPermission();
                        }
                    })
                    .create().show();

        } else {
            requestCameraPermission();
        }
    }

    //handle result of permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //if granted
                Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_SHORT).show();
                navigateToLoginActivity();
            } else {
                //if permission denied the first time show rational
                if (showRationale) {
                    Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
                    navigateToLoginActivity();
                } else {
                    //if user denies permission twice android no longer allows request
                    //increase counter for how many time the program attempts to show request
                    counter += 1;
                    showCameraPermissionExplanation();
                }
            }
        }

        if(counter == 3){
            //if the app has been prevented from asking for permission 3 times then the user has already denied permissions
            //progress to login screen
            Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            navigateToLoginActivity();
        }
    }
}