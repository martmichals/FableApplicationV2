package com.example.fableapplicationv2;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {
    public static final String TAG = "SignInActivity";

    //Firebase objects
    FirebaseAuth firebaseAuth;

    //All fields in the activity
    private EditText emailEditText;
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        //Initialize Firebase authentication
        firebaseAuth = FirebaseAuth.getInstance();

        //Initializing all the fields in the activity
        emailEditText = findViewById(R.id.idEmailEditText);
        passwordEditText = findViewById(R.id.idPasswordEditText);
    }

    //Tries to log in user and send to main activity
    public void onLoginClick(View v){
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        logIn(email, password);
    }

    //Method working with Firebase in order to log the user in
    public void logIn(final String email, final String password){
        Log.d(this.TAG, "Logging in user: " + email);

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(SignInActivity.TAG, "Log in for " + email + " successful");
                        } else {
                            Log.w(TAG, "Email sign in was a failure", task.getException());
                        }
                        checkLogInState();
                    }
                });
    }

    public void checkLogInState(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
            startActivity(intent);
        }else{
            Toast.makeText(SignInActivity.this, getString(R.string.logInFailure),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
