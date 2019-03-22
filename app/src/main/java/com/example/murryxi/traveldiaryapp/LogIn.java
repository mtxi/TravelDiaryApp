package com.example.murryxi.traveldiaryapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class LogIn extends AppCompatActivity
{
    private EditText user_email, user_pass;
    private FirebaseAuth fbAuth;
    private DatabaseReference dbRef;
    private Button signinBtn;
    private TextView loginView;
    private String uid;

    private static final String TAG = "LogIn";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        signinBtn = (Button)findViewById(R.id.btn_login);
        user_email = (EditText)findViewById(R.id.login_email);
        user_pass = (EditText)findViewById(R.id.login_password);
        dbRef = FirebaseDatabase.getInstance().getReference("users");

        fbAuth = FirebaseAuth.getInstance();

        signinBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Toast.makeText(LogIn.this, "Please wait...", Toast.LENGTH_LONG).show();
                String email = user_email.getText().toString().trim();
                String password = user_pass.getText().toString().trim();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password))
                {
                    fbAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if (task.isSuccessful())
                            {
                                dbRef.addValueEventListener(new ValueEventListener()
                                {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                    {
                                        Log.d(TAG, "signInWithEmail:success");
                                        fbAuth.getCurrentUser();

                                        // after successful log in, switch to View Journal Entries screen (Home)
                                        Intent i = new Intent(LogIn.this, ViewJournalEntries.class);
                                        startActivity(i);

                                        for (DataSnapshot snap : dataSnapshot.getChildren())
                                        {
                                            // get currently logged in user's ID
                                            uid = fbAuth.getCurrentUser().getUid();
                                            // get any registered user ID from database
                                            String uid_in_db = (String) snap.child("user_id").getValue();

                                            // if current user's ID matches one in database
                                            if (uid.equals(uid_in_db))
                                            {
                                                // get logged in user's username
                                                final String name = (String) snap.child("username").getValue();
                                                // display welcome message to user
                                                Toast welcome = Toast.makeText(LogIn.this, "Welcome, " + name, Toast.LENGTH_SHORT);
                                                welcome.show();

                                                // end the loop
                                                break;
                                            }
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                            else
                            {
                                Toast.makeText(LogIn.this, "Incorrect email or password", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(LogIn.this, "Please complete all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



}
