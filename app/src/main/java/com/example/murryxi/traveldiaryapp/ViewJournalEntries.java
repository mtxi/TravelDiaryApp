package com.example.murryxi.traveldiaryapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ViewJournalEntries extends AppCompatActivity {

    private RecyclerView imgRecycleView;
    private PhotoAdapter imgAdapter;
    private FloatingActionButton addEntryBtn;
    private BottomNavigationView botNavigationView;
    private ActionBar toolbar;

    public DatabaseReference dbRef;
    private List<PhotoUpload> imgUploads;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_img);

        addEntryBtn = findViewById(R.id.btn_add_entry);
        botNavigationView = findViewById(R.id.bottom_navigation);
        toolbar = getSupportActionBar();

        imgRecycleView = findViewById(R.id.recycler_view);
        imgRecycleView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(ViewJournalEntries.this);
        imgRecycleView.setLayoutManager(layoutManager);

        imgUploads = new ArrayList<>();

        /* we only need a reference to realtime database - image URLs are already stored here */
        dbRef = FirebaseDatabase.getInstance().getReference("uploads");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                /* add array of images to recycle view */
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren())
                {
                    PhotoUpload u = postSnapshot.getValue(PhotoUpload.class);
                    imgUploads.add(u);
                }
                /* set adapter to recycle view */
                imgAdapter = new PhotoAdapter(ViewJournalEntries.this, imgUploads);
                imgRecycleView.setAdapter(imgAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(ViewJournalEntries.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        /* switches to addJournalEntry activity when clicked */
        addEntryBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                addJournalEntry();
            }
        });

        botNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                Intent in;
                switch (item.getItemId())
                {
                    case R.id.nav_home:
                        toolbar.setTitle("Recent Entries");
                        in = new Intent (getBaseContext(), ViewJournalEntries.class);
                        startActivity(in);
                        overridePendingTransition(0,0);
                        break;
                    case R.id.nav_location:
                        toolbar.setTitle("Maps");
                        overridePendingTransition(0,0);
                        break;
                    case R.id.nav_trips:
                        toolbar.setTitle("Trips");
                        in = new Intent (getBaseContext(), ViewScheduledTrips.class);
                        startActivity(in);
                        overridePendingTransition(0,0);
                        break;
                    case R.id.nav_statistics:
                        toolbar.setTitle("Travel Statistics");
                        overridePendingTransition(0,0);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

    }

    /* add new journal entry */
    private void addJournalEntry()
    {
        Intent intent = new Intent(this, AddJournalEntry.class);
        startActivity(intent);
    }
}