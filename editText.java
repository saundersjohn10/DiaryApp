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

    //components
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

        //set up font spinner
        String[] fonts = new String[]{"8", "9", "10","11","12","14","18","24","30"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, fonts);
        fontSpinner.setAdapter(adapter);

        setUpContent();
        getFontSelection();
        setListeners();
    }

    //gets the previously chosen font selection
    private void getFontSelection(){
        database.getReference().child(mAuth.getUid()).child("font")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String fontSize = dataSnapshot.getValue().toString(); //the font
                        bodyText.setTextSize(Integer.parseInt(fontSize));

                        //gets the index of the font and sets it
                        String[] fonts = new String[]{"8", "9", "10","11","12","14","18","24","30"};
                        int indexOfDefaultFont = Arrays.asList(fonts).indexOf(fontSize);
                        fontSpinner.setSelection(indexOfDefaultFont);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    //set up previous data from the note
    private void setUpContent(){
        if(getIntent().hasExtra("title") && getIntent().hasExtra("content") && getIntent().hasExtra("updated")){
            title = getIntent().getStringExtra("title");
            getSupportActionBar().setTitle(title); //resets title
            content = getIntent().getStringExtra("content");
            updated = getIntent().getStringExtra("updated");
            id = getIntent().getStringExtra("id");
            titleTxt.setText(title);
            bodyText.setText(content);
            updatedTime.setText(updated);
            numOfNotes = getIntent().getIntExtra("num_of_notes",0);
        }
        else{
            bodyText.setHint("What's going on...");
            title = "";
            content = "";
            updated = getTime();;
            id = getIntent().getStringExtra("id");
        }
    }

    public void setListeners(){

        /*-----------------buttons------------*/
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(content.equals("")){
                    //if the user never inputted any content, delete this note
                    database.getReference().child(mAuth.getUid()).child("Notes").child(id).removeValue();
                    startActivity(new Intent(getApplicationContext(),list_view.class));
                }
                else if(title.equals("")){ //if the user never set a title
                    Toast.makeText(getApplicationContext(), "Please enter a title",Toast.LENGTH_LONG).show();
                }
                else {
                    startActivity(new Intent(getApplicationContext(),list_view.class));
                }
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show alert asking if they are sure they would like to delete the note
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
        //if the user picks a new font size
        fontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int item = Integer.parseInt(parent.getItemAtPosition(position).toString());
                bodyText.setTextSize(item); //change the font
                database.getReference().child(mAuth.getUid()).child("font").setValue(item + ""); //upload the font size
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        //body text in the note is changed
        bodyText.addTextChangedListener(new TextWatcher(){
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                //thread saving the text
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

        //the title text is changed
        titleTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Runnable saveTitle = new Runnable() {
                    @Override
                    public void run() {
                        updated = getTime(); //update time
                        title = titleTxt.getText().toString();
                        database.getReference().child(mAuth.getUid()).child("Notes").child(id).child("title").setValue(title); //upload changes
                        database.getReference().child(mAuth.getUid()).child("Notes").child(id).child("updated").setValue(updated);
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

    //deletes the current note
    private void deleteNote(){
        /* The trouble with deleting a note is that the notes are numbered in the database,
        * therefore a program to keep or update the numbering of the notes had to be created
        * */
        final int thisNote = Integer.parseInt(id);
        if(thisNote == numOfNotes){ //the last note in the database, easy removal
            database.getReference().child(mAuth.getUid()).child("Notes").child(String.valueOf(thisNote)).removeValue();
            return;
        }
        database.getReference().child(mAuth.getUid()).child("Notes")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    //loop through notes, starting with current and adding 1 to the number of any note after it
                    for(int current = thisNote; current < numOfNotes; current++){
                        DatabaseReference ref = database.getReference().child(mAuth.getUid()).child("Notes").child(String.valueOf(current));

                        String contentReplace = dataSnapshot.child(String.valueOf(current + 1)).child("content").getValue().toString();
                        ref.child("content").setValue(contentReplace);

                        String titleReplace = dataSnapshot.child(String.valueOf(current + 1)).child("title").getValue().toString();
                        ref.child("title").setValue(titleReplace);

                        String updateReplace = dataSnapshot.child(String.valueOf(current + 1)).child("updated").getValue().toString();
                        ref.child("updated").setValue(updateReplace);
                    }
                    database.getReference().child(mAuth.getUid()).child("Notes").child(String.valueOf(numOfNotes)).removeValue(); //remove last

                }
                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
    }

    //save the body text
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
