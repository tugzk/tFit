package com.example.thedietitianfyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thedietitianfyp.utils.Save;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;



import java.util.Locale;

public class Login extends AppCompatActivity {

    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        new LoginProcess().execute();
    }

    public class LoginProcess extends AsyncTask<Void, Void, Void> {

        EditText emailID, password, editTextTargetWeight;
        FirebaseAuth mFirebaseAuth; // store credentials of users to firebase
        TextView txtSignUp, txtReset;
        Button btnLogin;

        @Override
        protected Void doInBackground(Void... voids) {
            emailID= findViewById(R.id.editTextloginEmail);
            password = findViewById(R.id.editTexLoginPassword);
            txtSignUp = findViewById(R.id.textViewSignup);
            txtReset = findViewById(R.id.textViewForgotPassword);
            btnLogin = findViewById(R.id.buttonLogin);
            mFirebaseAuth = FirebaseAuth.getInstance();
            editTextTargetWeight = (EditText) findViewById(R.id.editTextTargetWeight);

            mAuthStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser mFireBaseUser = mFirebaseAuth.getCurrentUser();
                    if(mFireBaseUser != null && editTextTargetWeight != null) { // does not skip through signup
                        Toast.makeText(Login.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(Login.this, MainActivity.class);
                        startActivity(i);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                    else {
                        //Toast.makeText(Login.this, "Please Login", Toast.LENGTH_SHORT).show();
                    }
                }
            };

            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = emailID.getText().toString();
                    String pwd  = password.getText().toString();

                    //Check email
                    if(email.isEmpty()) {
                        emailID.setError("Please enter email");
                        emailID.requestFocus();
                    }
                    // Check password
                    else if(pwd.isEmpty()) {
                        password.setError("Please enter password");
                        password.requestFocus();
                    }
                    // Check both
                    else if (email.isEmpty() && pwd.isEmpty()) {
                        Toast.makeText(Login.this, "Fields are empty!", Toast.LENGTH_SHORT).show();
                    }
                    else if (!(email.isEmpty() && pwd.isEmpty())) {
                        mFirebaseAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(!task.isSuccessful()) {
                                    Toast.makeText(Login.this, "Login Error! Try again", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    // Save session if user completes login
                                    // Save value is true for session
                                    Save.save(getApplicationContext(), "session", "true");

                                    Intent intToHome = new Intent(Login.this, MainActivity.class);
                                    startActivity(intToHome);
                                }
                            }
                        });
                    }
                    else{
                        Toast.makeText(Login.this, "Error!, please try again!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            txtSignUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intSignUp = new Intent(Login.this, SignUp.class);
                    startActivity(intSignUp);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            });

            txtReset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent resetInt = new Intent(Login.this, ResetPassword.class);
                    startActivity(resetInt);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            });
            mFirebaseAuth.addAuthStateListener(mAuthStateListener);
            return null;
        }
    }
}