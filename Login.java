package com.example.johnsaunders.mydiary;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class Login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private TextView emailTxt;
    private TextView passwordTxt;
    private Button loginBtn;
    private Button signUpBtn;
    private ProgressBar loading;
    private TextView forgotPwd;
    private EditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance(); //authorization
        database = FirebaseDatabase.getInstance();
        loading = findViewById(R.id.progressBarLogin);
        loading.bringToFront();


        //checks to see if the user is already signed in
        if(mAuth.getCurrentUser() != null){
            loading.setVisibility(View.VISIBLE); //set loading sign
            database.getReference().child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //display a message saying you already signed in
                    Toast.makeText(getApplicationContext(), "Already logged in as " + dataSnapshot.child("First").getValue() + " " + dataSnapshot.child("Last").getValue(),Toast.LENGTH_LONG).show();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
            loading.setVisibility(View.INVISIBLE);
            startActivity(new Intent(getApplicationContext(), list_view.class));//go to list screen
        }

        //set instance variables
        emailTxt = findViewById(R.id.emailTxt);
        passwordTxt = findViewById(R.id.passwordTxt);
        loginBtn = findViewById(R.id.loginBtn);
        signUpBtn = findViewById(R.id.signUpBtn);
        forgotPwd = findViewById(R.id.forgotPwd);

        setUpButtons();

    }

    private void setUpButtons(){

        //login button pressed
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*----------------check for invalid formats of authentication---------------*/
                TextView emailError = findViewById(R.id.emailErrorTxtLogin);
                emailError.setText("Enter a valid email");
                TextView passwordError = findViewById(R.id.passwordErrorTxtLogin);
                passwordError.setText("Enter a valid password");

                //the email and password entered
                String email = emailTxt.getText().toString();
                String password = passwordTxt.getText().toString();

                //if there isn't an '@' or '.' in the email and the password is less than 6 characters an error is shown
                if((!email.contains("@")||!email.contains(".")) && password.length() < 6){
                    emailError.setVisibility(View.VISIBLE);
                    passwordError.setVisibility(View.VISIBLE);
                }
                else if(!email.contains("@")||!email.contains("."))
                    emailError.setVisibility(View.VISIBLE);
                else if(password.length() < 6)
                    passwordError.setVisibility(View.VISIBLE);
                else{ //if it reaches here, it is a valid email
                    emailError.setVisibility(View.INVISIBLE);
                    passwordError.setVisibility(View.INVISIBLE);
                    login(email,password); //call the login method
                }
            }
        });

        //sign up button pressed
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //add to the next activity the email and password entered, assuming that is credentials the user wants
                Intent i = new Intent(getApplicationContext(), newUser.class);
                i.putExtra("email", emailTxt.getText().toString());
                i.putExtra("password", passwordTxt.getText().toString());

                startActivity(i);
            }
        });

        //'forgot my password' button pressed
        forgotPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //create popup asking user to input email address
                input = new EditText(Login.this);
                input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Login.this);
                alertDialog.setView(input);
                alertDialog.setMessage("Enter your email and receive an email showing you how to reset your password.");

                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loading.setVisibility(View.VISIBLE);
                        tryAndSendEmail();
                        dialog.cancel();
                    }
                });
                alertDialog.show(); //show popup

            }
        });
    }

    private void tryAndSendEmail(){
        final String email = input.getText().toString();
        if((!email.contains("@"))|| (!email.contains("."))){ //check for a valid email
            Toast.makeText(getApplicationContext(), email + " is not a valid email", Toast.LENGTH_LONG).show();
            loading.setVisibility(View.INVISIBLE);
        }
        else{
            //send the email
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getApplicationContext(), "Email sent to " + email, Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "No email associated with " + email, Toast.LENGTH_LONG).show();
                    }
                    loading.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    //method to login and start new activity
    private void login(String email, String password){
        loading.setVisibility(View.VISIBLE); //show loading

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

            //an onComplete method that is used when done logging in
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){ //successfully logged in

                    database.getReference().child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //show a welcome toast with the user's first and last name
                            Toast.makeText(getApplicationContext(), "Welcome " + dataSnapshot.child("First").getValue() + " " + dataSnapshot.child("Last").getValue(),Toast.LENGTH_LONG).show();
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
                    loading.setVisibility(View.INVISIBLE); //stop the loading
                    startActivity(new Intent(getApplicationContext(), list_view.class));
                }
                else{
                    //not a valid login
                    loading.setVisibility(View.INVISIBLE); //stop the loading

                    //show an error message
                    TextView error = findViewById(R.id.emailErrorTxtLogin);
                    error.setVisibility(View.VISIBLE);
                    error.setText("No account exists with that username or password");

                }
            }
        });
    }
}
