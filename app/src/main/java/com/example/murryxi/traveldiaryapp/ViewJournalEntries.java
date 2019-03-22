package com.example.murryxi.traveldiaryapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private Button logOutBtn;


    public DatabaseReference dbRef;
    private FirebaseAuth fbAuth;
    private FirebaseAuth.AuthStateListener fbAuthListener;
    private FirebaseUser currentUser;
    private List<JournalEntry> imgUploads;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_img);

        addEntryBtn = findViewById(R.id.btn_add_entry);
        logOutBtn = findViewById(R.id.btn_log_out);
        botNavigationView = findViewById(R.id.bottom_navigation);
        toolbar = getSupportActionBar();

        imgRecycleView = findViewById(R.id.recycler_view);
        imgRecycleView.setHasFixedSize(true);

        fbAuth = FirebaseAuth.getInstance();
        currentUser = fbAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference().child("users");

        /* check if a user is logged in */
        fbAuthListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                if (fbAuth.getCurrentUser()==null)
                {
                    Intent logIn_intent = new Intent (ViewJournalEntries.this, Register.class);
                    logIn_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(logIn_intent);
                }
            }
        };

        fbAuth.addAuthStateListener(fbAuthListener);

        LinearLayoutManager layoutManager = new LinearLayoutManager(ViewJournalEntries.this);
        imgRecycleView.setLayoutManager(layoutManager);

        imgUploads = new ArrayList<>();

        /* we only need a reference to realtime database - image URLs are already stored here */
        if (fbAuth.getCurrentUser()!=null)
        {
            dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid()).child("user_entries");
        }
        else
        {
            Intent logIn_intent = new Intent (ViewJournalEntries.this, Register.class);
            logIn_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(logIn_intent);
        }

        dbRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                /* clear array to avoid duplicate views */
                imgUploads.clear();

                /* add array of journal entries to recycle view */
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren())
                {
                    JournalEntry u = postSnapshot.getValue(JournalEntry.class);
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

        logOutBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                fbAuth.signOut();
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
                        in = new Intent (getApplicationContext(), ViewJournalEntries.class);
                        startActivity(in);
                        overridePendingTransition(0,0);
                        break;
                    case R.id.nav_location:
                        toolbar.setTitle("Maps");
                        overridePendingTransition(0,0);
                        break;
                    case R.id.nav_trips:
                        Intent i = new Intent (getApplicationContext(), ViewScheduledTrips.class);
                        startActivity(i);
                        overridePendingTransition(0,0);
                        break;
                    case R.id.nav_statistics:
                        toolbar.setTitle("Travel Stats");
                        in = new Intent(getApplicationContext(), ViewTravelStats.class);
                        startActivity(in);
                        overridePendingTransition(0,0);
                        break;
                    default:
                        break;
                }
                return true;
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