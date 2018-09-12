package com.example.johnsaunders.mydiary;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Calendar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class editText extends AppCompatActivity {

    private TextView bodyText;
    private TextView updatedTime;
    private TextView titleTxt;
    private Button deleteBtn;

    private ImageButton backButton;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;

    private String title;
    private String content;
    private String updated;
    private String id;
    private int numOfNotes;

    private Spinner fontSpinner;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_text);
        bodyText = findViewById(R.id.bodyTxt);
        updatedTime = findViewById(R.id.updatedTime);
        backButton = findViewById(R.id.backBtn);
        titleTxt = findViewById(R.id.titleTxt);
        fontSpinner = findViewById(R.id.fontSelection);
        deleteBtn = findViewById(R.id.deleteBtn);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        String[] fonts = new String[]{"8", "9", "10","11","12","14","18","24","30"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, fonts);

        fontSpinner.setAdapter(adapter);

        setUpAdapter();
        setUpContent();
        setListeners();

    }

    private void setUpAdapter(){
        database.getReference().child(mAuth.getUid()).child("font")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String fontSize = dataSnapshot.getValue().toString();

                        bodyText.setTextSize(Integer.parseInt(fontSize));


                        String[] fonts = new String[]{"8", "9", "10","11","12","14","18","24","30"};

                        int indexOfDefaultFont = Arrays.asList(fonts).indexOf(fontSize);


                        fontSpinner.setSelection(indexOfDefaultFont);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    private void setUpContent(){
        if(getIntent().hasExtra("title") && getIntent().hasExtra("content") && getIntent().hasExtra("updated")){
            title = getIntent().getStringExtra("title");
            getSupportActionBar().setTitle(title);
            content = getIntent().getStringExtra("content");
            updated = getIntent().getStringExtra("updated");
            id = getIntent().getStringExtra("id");
            titleTxt.setText(title);
            bodyText.setText(content);
            updatedTime.setText(updated);

            numOfNotes = getIntent().getIntExtra("num_of_notes",0);

            getAndSetFont();
        }
        else{
            bodyText.setHint("What's going on...");
            title = "";
            content = "";
            updated = getTime();;
            id = getIntent().getStringExtra("id");
        }
    }

    private void getAndSetFont(){
        database.getReference().child(mAuth.getUid()).child("font")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        bodyText.setTextSize(Integer.parseInt(dataSnapshot.getValue().toString()));
                        System.out.println("here: " + dataSnapshot.getValue().toString());
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    public void setListeners(){

        /*-----------------buttons------------*/
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(content.equals("")){
                    database.getReference().child(mAuth.getUid()).child("Notes").child(id).removeValue();
                    startActivity(new Intent(getApplicationContext(),list_view.class));
                }
                else if(title.equals("")){
                    Toast.makeText(getApplicationContext(), "Enter a title",Toast.LENGTH_LONG).show();
                }
                else {
                    startActivity(new Intent(getApplicationContext(),list_view.class));
                }
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(editText.this);

                alertDialog.setMessage("Are you sure you want to delete this note?");

                alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteNote();
                        startActivity(new Intent(getApplicationContext(),list_view.class));
                    }
                });
                alertDialog.show();
            }
        });

        /*-------------text listeners--------------*/
        fontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int item = Integer.parseInt(parent.getItemAtPosition(position).toString());
                bodyText.setTextSize(item);
                database.getReference().child(mAuth.getUid()).child("font").setValue(item + "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        bodyText.addTextChangedListener(new TextWatcher(){
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                Runnable saveData = new Runnable() {
                    public void run() {


                        saveBodyText();
                    }
                };
                saveData.run();
            }

            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        });

        titleTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Runnable saveTitle = new Runnable() {
                    @Override
                    public void run() {
                        updated = getTime();
                        title = titleTxt.getText().toString();
                        database.getReference().child(mAuth.getUid()).child("Notes").child(id).child("title").setValue(title);
                        database.getReference().child(mAuth.getUid()).child("Notes").child(id).child("updated").setValue(updated);
                        System.out.println("called at title text change");
                        updateTime();

                        //update action bar
                        getSupportActionBar().setTitle(title);
                    }
                };
                saveTitle.run();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void deleteNote(){

        final int thisNote = Integer.parseInt(id);

        if(thisNote == numOfNotes){
            database.getReference().child(mAuth.getUid()).child("Notes").child(String.valueOf(thisNote)).removeValue();
            return;
        }

        database.getReference().child(mAuth.getUid()).child("Notes")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {


                    for(int current = thisNote; current < numOfNotes; current++){
                        DatabaseReference ref = database.getReference().child(mAuth.getUid()).child("Notes").child(String.valueOf(current));

                        String contentReplace = dataSnapshot.child(String.valueOf(current + 1)).child("content").getValue().toString();
                        ref.child("content").setValue(contentReplace);

                        String titleReplace = dataSnapshot.child(String.valueOf(current + 1)).child("title").getValue().toString();
                        ref.child("title").setValue(titleReplace);

                        String updateReplace = dataSnapshot.child(String.valueOf(current + 1)).child("updated").getValue().toString();
                        ref.child("updated").setValue(updateReplace);
                    }

                    database.getReference().child(mAuth.getUid()).child("Notes").child(String.valueOf(numOfNotes)).removeValue();

                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });




    }

    public void saveBodyText(){
        final Thread saveText = new Thread(new Runnable() {
            @Override
            public void run() {


                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        updated = getTime();
                        content = bodyText.getText().toString();
                        DatabaseReference ref = database.getReference().child(mAuth.getUid()).child("Notes").child(id);
                        ref.child("updated").setValue(updated);
                        ref.child("content").setValue(content);
                    }
                });
                t.start();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.progressBarEditTxt).setVisibility(View.VISIBLE);
                    }
                });

                try{Thread.sleep(1000);}catch (InterruptedException e){}
                while(t.isAlive()){
                    //wait until done saving
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.progressBarEditTxt).setVisibility(View.INVISIBLE);
                        updateTime();
                    }
                });

            }
        });
        saveText.start();
    }

    private void updateTime(){
        String currentTime = getTime();

        String dayNow = currentTime.substring(8,11);
        String dayUpdated = updated.substring(8,11);

        String monthNow = getNumMonth(currentTime.substring(4, 8));
        String monthUpdated = getNumMonth(updated.substring(4, 8));

        String yearNow = currentTime.substring(24,28);
        String yearUpdated = updated.substring(24,28);

        if(dayNow.equals(dayUpdated) && monthNow.equals(monthUpdated) && yearNow.equals(yearUpdated)){
            String hourNow = currentTime.substring(11,13);
            String minNow = currentTime.substring(14,16);

            String hourUpdated = updated.substring(11,13);
            String minUpdated = updated.substring(14,16);

            if(hourNow.equals(hourUpdated) && minNow.equals(minUpdated)){
                updatedTime.setText("a moment ago");
            }
            else if(hourNow.equals(hourUpdated)){
                int minNowInt = Integer.parseInt(minNow);
                int minUpdatedInt = Integer.parseInt(minUpdated);
                updatedTime.setText((minNowInt-minUpdatedInt) + " minutes ago");
            }
            else{
                int hourNowInt = Integer.parseInt(hourNow);
                int hourUpdatedInt = Integer.parseInt(hourUpdated);
                updatedTime.setText((hourNowInt - hourUpdatedInt) + " hours ago");
            }
        }
        else{
            updatedTime.setText(monthUpdated + "/" + dayUpdated +"/"+yearUpdated);
        }
    }
    public static String getNumMonth(String month){
        if(month.equals("Jan"))
            return "1";
        else if(month.equals("Feb"))
            return "2";
        else if(month.equals("Mar"))
            return "3";
        else if(month.equals("Apr"))
            return "4";
        else if(month.equals("May"))
            return "5";
        else if(month.equals("Jun"))
            return "6";
        else if(month.equals("Jul"))
            return "7";
        else if(month.equals("Aug"))
            return "8";
        else if(month.equals("Sep"))
            return "9";
        else if(month.equals("Oct"))
            return "10";
        else if(month.equals("Nov"))
            return "11";
        else
            return "12";
    }

    private String getTime(){
        String time = Calendar.getInstance().getTime().toString();
        if(time.contains("+00:00")){
            int index = time.indexOf("+00:00");
            return time.substring(0,index) + time.substring(index+6);
        }
        return time;
    }



}
