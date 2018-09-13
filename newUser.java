package com.example.johnsaunders.mydiary;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class newUser extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    private Button creatUser;
    private TextView firstNameTxt;
    private TextView lastNameTxt;
    private TextView emailTxt;
    private TextView passwordTxt;
    private TextView reEnter;
    private ProgressBar loading;

    private Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        extras = getIntent().getExtras(); //the login information passed from the login screen

        //instance variables
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        creatUser = findViewById(R.id.createUser);
        firstNameTxt = findViewById(R.id.inputFName);
        lastNameTxt = findViewById(R.id.inputLName);
        emailTxt = findViewById(R.id.inputEmail);
        passwordTxt = findViewById(R.id.inputPassword);
        reEnter = findViewById(R.id.inputPassword2);
        loading = findViewById(R.id.progressBarNewUser);


        enterOldInformation();

        //the button that is pressed when done entering information
        creatUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading.setVisibility(View.VISIBLE);
                if (verifyInformation()){
                    final String email = emailTxt.getText().toString();
                    final String password = passwordTxt.getText().toString();
                    final String firstName = firstNameTxt.getText().toString();
                    final String lastName = lastNameTxt.getText().toString();
                    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            mAuth.signInWithEmailAndPassword(email,password);
                            database.getReference().child(mAuth.getUid()).child("First").setValue(firstName);
                            database.getReference().child(mAuth.getUid()).child("Last").setValue(lastName);
                            database.getReference().child(mAuth.getUid()).child("font").setValue("14");
                            Toast.makeText(getApplicationContext(), "Welcome " + firstName + " " + lastName,Toast.LENGTH_LONG).show();
                            startActivity(new Intent(getApplicationContext(), list_view.class));
                        }
                    });
                }
                loading.setVisibility(View.INVISIBLE);
            }
        });
    }

    //enters the information into the text boxes that the user already filled out before
    private void enterOldInformation(){
        String email = extras.getString("email");
        String password  = extras.getString("password");

        if(!email.equals("")){
            emailTxt.setText(email);
        }
        if(!password.equals("")){
            passwordTxt.setText(password);
        }
    }

    //method that checks information given to see if it is acceptable
    private boolean verifyInformation(){
        String firstName = firstNameTxt.getText().toString();
        String lastName = lastNameTxt.getText().toString();
        String email = emailTxt.getText().toString();
        String password = passwordTxt.getText().toString();
        String matchPassword = reEnter.getText().toString();
        boolean proceed = true;

        if(firstName.length() < 3){
            findViewById(R.id.errorFirstName).setVisibility(View.VISIBLE);
            proceed = false;
        }
        else findViewById(R.id.errorFirstName).setVisibility(View.INVISIBLE);

        if(lastName.length() < 3){
            findViewById(R.id.errorLastName).setVisibility(View.VISIBLE);
            proceed = false;
        }
        else findViewById(R.id.errorLastName).setVisibility(View.INVISIBLE);

        TextView emailError = findViewById(R.id.errorEmail);
        if(!email.contains("@")){
            emailError.setText("Email must contain \"@\"");
            emailError.setVisibility(View.VISIBLE);
            proceed = false;
        }
        else if(!email.contains(".")){
            emailError.setText("Email must contain \".\"");
            emailError.setVisibility(View.VISIBLE);
            proceed = false;
        }
        else if(email.length() == 0){
            emailError.setText("Please enter your email");
            emailError.setVisibility(View.VISIBLE);
            proceed = false;
        }
        else
            emailError.setVisibility(View.INVISIBLE);

        if(password.length() <= 7){
            findViewById(R.id.passwordError).setVisibility(View.VISIBLE);
            proceed = false;
        }
        else findViewById(R.id.passwordError).setVisibility(View.INVISIBLE);

        if(!password.equals(matchPassword)){
            findViewById(R.id.errorPasswordMatch).setVisibility(View.VISIBLE);
            proceed = false;
        }
        else findViewById(R.id.errorPasswordMatch).setVisibility(View.INVISIBLE);

        return proceed;
    }



}
