package com.example.murryxi.traveldiaryapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
                        in = new Intent(getApplicationContext(), ViewVisitedPlaces.class);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.actionbarmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mybutton)
        {
            AlertDialog.Builder logOutAlert = new AlertDialog.Builder(ViewJournalEntries.this);
            logOutAlert.setTitle("Log Out");
            logOutAlert.setMessage("Are you sure you want to log out?");
            logOutAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    fbAuth.signOut();
                }
            });
            logOutAlert.setNegativeButton("No", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.cancel();
                }
            });
            logOutAlert.show();
        }
        return super.onOptionsItemSelected(item);
    }

    /* add new journal entry */
    private void addJournalEntry()
    {
        Intent intent = new Intent(this, AddJournalEntry.class);
        startActivity(intent);
    }
}