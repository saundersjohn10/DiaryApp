package com.example.johnsaunders.mydiary;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.TreeSet;

public class list_view extends AppCompatActivity {

    //instance variables
    public RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private Button logoutBtn;
    private ImageView newNote;
    private TextView nameLbl;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    int noteID; //an int that keeps track of the number of notes, a way to keep track of the notes and a way of saving them
    private TreeSet<Note> notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);

        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        logoutBtn = findViewById(R.id.logoutBtn);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        newNote = findViewById(R.id.newNoteBtn);
        nameLbl = findViewById(R.id.nameLbl);

        notes = new TreeSet<>();
        setUpContent();
        setUpButtons();

    }

    public void setUpButtons(){
        //logout button pressed
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut(); //firebase logs out
                startActivity(new Intent(getApplicationContext(), Login.class)); //go back to login screen
                Toast.makeText(getApplicationContext(), "Successfully Logged Out",Toast.LENGTH_LONG).show();
            }
        });

        //trying to create a new note
        newNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), editText.class);
                i.putExtra("id",(noteID+1) + ""); //adds the id of this new note
                startActivity(i);
            }
        });
    }

    public void setUpContent(){

        database.getReference().child(mAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        //sets who is logged in currently
                        nameLbl.setText(dataSnapshot.child("First").getValue() + " " + dataSnapshot.child("Last").getValue());

                        /*----------------set up cards showing entries------------*/

                        //if the person has no notes, make one for them
                        if(dataSnapshot.child("Notes").child("1").getValue() == null){
                            noteID = 1;
                            notes.add(new Note("What's going on...","Your first note!",getTime(), 1));
                        }

                        //loop through the notes the person has
                        for (DataSnapshot snapshot : dataSnapshot.child("Notes").getChildren()) {

                            noteID = Integer.parseInt(snapshot.getKey().toString()); //current note id
                            String contents = snapshot.child("content").getValue().toString(); //the contents of the note
                            String lastUpdated = snapshot.child("updated").getValue().toString(); //updated text
                            String title = snapshot.child("title").getValue().toString(); //title of note

                            //construct new note from information gathered
                            Note n = new Note(contents,title,lastUpdated,noteID);
                            notes.add(n); //add to list
                        }

                        //set the adapter
                        adapter = new notes_adapter(notes, getApplicationContext());
                        recyclerView.setAdapter(adapter);

                        findViewById(R.id.loading).setVisibility(View.INVISIBLE); //stop loading

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });
    }

    public String formatTime(String updated){
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
                return "a moment ago";
            }
            else if(hourNow.equals(hourUpdated)){
                int minNowInt = Integer.parseInt(minNow);
                int minUpdatedInt = Integer.parseInt(minUpdated);
                int minDiff = minNowInt-minUpdatedInt;
                if(minDiff != 1){
                    return minDiff + " minutes ago";
                }
                else{
                    return "a minute ago";
                }
            }
            else{
                int hourNowInt = Integer.parseInt(hourNow);
                int hourUpdatedInt = Integer.parseInt(hourUpdated);
                int hourDiff = hourNowInt - hourUpdatedInt;
                if(hourDiff != 1){
                    return (hourNowInt - hourUpdatedInt) + " hours ago";
                }
                else{
                    return "an hour ago";
                }
            }
        }
        else{
            return monthUpdated + "/" + dayUpdated +"/"+yearUpdated;
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

    /*---------------------------inner class that creates custom view of notes------------------*/
    public class notes_adapter extends RecyclerView.Adapter<com.example.johnsaunders.mydiary.list_view.notes_adapter.ViewHolder> {

        private TreeSet<Note> listItems;

        public notes_adapter(TreeSet<Note> listItems, Context context) {
            this.listItems = listItems;
        }


        //associates the view of one note to the adapter of notes
        @Override
        public com.example.johnsaunders.mydiary.list_view.notes_adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_list, parent, false);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int itemPosition = recyclerView.getChildLayoutPosition(v);

                    Object[] notesArray = listItems.descendingSet().toArray();

                    Note noteClicked = (Note) notesArray[itemPosition];


                    Intent i = new Intent(getApplicationContext(), editText.class);
                    i.putExtra("title", noteClicked.getTitle());
                    i.putExtra("content", noteClicked.getContents());
                    i.putExtra("updated", formatTime(noteClicked.getDate()));
                    i.putExtra("id", noteClicked.getId() + "");
                    i.putExtra("num_of_notes",noteID);

                    startActivity(i);
                }
            });

            return new com.example.johnsaunders.mydiary.list_view.notes_adapter.ViewHolder(v);
        }


        @Override
        public void onBindViewHolder(com.example.johnsaunders.mydiary.list_view.notes_adapter.ViewHolder holder, int position) {
            Object[] notesArray = listItems.descendingSet().toArray();

            Note note = (Note) notesArray[position];

            holder.dateTxt.setText(formatTime(note.getDate()));
            holder.titleTxt.setText(note.getTitle());
            String preview;
            try{
                preview = note.getContents().substring(0,52) + "...";
            }
            catch (StringIndexOutOfBoundsException e){
                preview = note.getContents();
            }
            holder.previewTxt.setText(preview);
        }

        @Override
        public int getItemCount() {
            return listItems.size();
        }


        class ViewHolder extends RecyclerView.ViewHolder {
            public TextView dateTxt;
            public TextView titleTxt;
            public TextView previewTxt;


            public ViewHolder(View v) {
                super(v);

                dateTxt = itemView.findViewById(R.id.dateTxt);
                titleTxt = itemView.findViewById(R.id.titleTxt);
                previewTxt = itemView.findViewById(R.id.previewTxt);
            }
        }
    }


}
