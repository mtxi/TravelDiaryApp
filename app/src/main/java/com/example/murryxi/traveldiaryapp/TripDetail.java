package com.example.murryxi.traveldiaryapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class TripDetail extends AppCompatActivity
{
    TextView tripLocation, tripCountdown, tripDate, tripNotes;
    ImageView tripHeader;
    private DatabaseReference dbRef;
    private FirebaseAuth fbAuth;
    private FirebaseUser currentUser;
    private DatabaseReference dbUsers;
    public String key, picUrl, place, notes, days, date, newNotes, country = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

        tripHeader = findViewById(R.id.imgPreview);
        tripLocation = findViewById(R.id.location);
        tripCountdown = findViewById(R.id.daysLeft);
        tripDate = findViewById(R.id.date);
        tripNotes = findViewById(R.id.notes);

        fbAuth = FirebaseAuth.getInstance();
        currentUser = fbAuth.getCurrentUser();

        Intent in = getIntent();
        key = in.getStringExtra("id");
        picUrl = in.getStringExtra("image");
        place = in.getStringExtra("destination");
        notes = in.getStringExtra("notes");
        days = in.getStringExtra("countdown");
        date = in.getStringExtra("date");
        country = in.getStringExtra("country");

        dbUsers = FirebaseDatabase.getInstance().getReference("users");

        tripLocation.setText(place);
        tripNotes.setText(notes);
        tripCountdown.setText(days);
        tripDate.setText(date);
        //GlideApp.with(JournalEntryDetail.this).clear(imgView);
        GlideApp.with(TripDetail.this)
                .load(picUrl)
                .centerCrop()
                .into(tripHeader);

    }

    public void editTrip()
    {
        setContentView(R.layout.activity_add_new_trip);
        FloatingActionButton updateBtn = findViewById(R.id.btn_add_new_trip);
        TextView sameLocation = findViewById(R.id.choose_city);
        TextView sameDate = findViewById(R.id.choose_date);
        ImageView samePicture = findViewById(R.id.image_view);
        EditText editNotes = findViewById(R.id.trip_notes);

        sameLocation.setText(place);
        sameDate.setText(date);
        editNotes.setText(notes);

        GlideApp.with(TripDetail.this)
                .load(picUrl)
                .centerCrop()
                .into(samePicture);

        editNotes.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                newNotes = editNotes.getText().toString().trim();
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                newNotes = editNotes.getText().toString().trim();
            }
        });

        final DatabaseReference editDb = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        updateBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dbUsers.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        final TripInfo newInfo = new TripInfo(key, country, place, date, newNotes, picUrl);
                        editDb.child("user_trips").child(key).setValue(newInfo);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("YES", databaseError.getMessage());
                    }
                });

                Toast.makeText(TripDetail.this, "Text updated successfully", Toast.LENGTH_SHORT).show();
                finish();
                overridePendingTransition(0,0);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.options_single_jentry, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.edit_jentry:
                editTrip();
                break;
            case R.id.delete_jentry:
                break;
            case android.R.id.home:
                this.finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
