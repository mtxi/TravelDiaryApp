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

public class ViewScheduledTrips extends AppCompatActivity
{
    private BottomNavigationView botNavigationView;
    private ActionBar toolbar;
    private RecyclerView tripRecycleView;
    private TripListAdapter tripListAdapter;
    private FloatingActionButton addTripBtn;

    public DatabaseReference dbRef;
    private FirebaseAuth fbAuth;
    private FirebaseAuth.AuthStateListener fbAuthListener;
    private FirebaseUser currentUser;
    private List<TripInfo> tripInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_scheduled_trips);

        addTripBtn = findViewById(R.id.btn_add_trip);
        botNavigationView = findViewById(R.id.bottom_navigation);
        toolbar = getSupportActionBar();

        tripRecycleView = findViewById(R.id.trips_recycler_view);
        tripRecycleView.setHasFixedSize(true);

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
                    Intent i = new Intent (ViewScheduledTrips.this, Register.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                }
            }
        };

        fbAuth.addAuthStateListener(fbAuthListener);

        LinearLayoutManager layoutManager = new LinearLayoutManager(ViewScheduledTrips.this);
        tripRecycleView.setLayoutManager(layoutManager);

        tripInfos = new ArrayList<>();

        dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid()).child("user_trips");

        dbRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                /* clear array to avoid duplicate views */
                tripInfos.clear();

                /* add array of journal entries to recycle view */
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren())
                {
                    TripInfo u = postSnapshot.getValue(TripInfo.class);
                    tripInfos.add(u);
                }
                /* set adapter to recycle view */
                tripListAdapter = new TripListAdapter(ViewScheduledTrips.this, tripInfos);
                tripRecycleView.setAdapter(tripListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(ViewScheduledTrips.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        addTripBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                addNewTrip();
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
                    in = new Intent(getApplicationContext(), AddNewTrip.class);
                    startActivity(in);
                    overridePendingTransition(0,0);
                    break;
                case R.id.nav_trips:
                    Intent i = new Intent (getApplicationContext(), ViewScheduledTrips.class);
                    startActivity(i);
                    overridePendingTransition(0,0);
                    break;
                case R.id.nav_statistics:
                    toolbar.setTitle("Travel Statistics");
                    overridePendingTransition(0,0);
                    break;
                default:
                    break;
            }
            return true;
        }
     });
    }

    private void addNewTrip()
    {
        Intent intent = new Intent(this, AddNewTrip.class);
        startActivity(intent);
    }
}
