package com.example.murryxi.traveldiaryapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class JournalEntryDetail extends AppCompatActivity {

    TextView postLocation, postText, postDate;
    ImageView imgView;
    private DatabaseReference dbRef;
    private DatabaseReference dbUsers;
    private FirebaseAuth fbAuth;
    private FirebaseUser currentUser;
    public String key, imgSrc, desc, place, date, newText = null;
    public double lat;
    public double lng;
    public boolean diffActivity = true;
    private static final int PICK_IMAGE_REQUEST = 1;

    Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_detail);

        postLocation = findViewById(R.id.entry_location);
        postText = findViewById(R.id.caption);
        postDate = findViewById(R.id.entry_date);
        imgView = findViewById(R.id.imgPreview);

        fbAuth = FirebaseAuth.getInstance();
        currentUser = fbAuth.getCurrentUser();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle b = getIntent().getExtras();
        Bundle c = getIntent().getExtras();
        imgSrc = getIntent().getStringExtra("image");
        place = getIntent().getStringExtra("location");
        desc = getIntent().getStringExtra("text");
        key = getIntent().getStringExtra("id");
        date = getIntent().getStringExtra("date");
        lat = b.getDouble("lat");
        lng = c.getDouble("lng");

        dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid());
        dbUsers = FirebaseDatabase.getInstance().getReference("users");

        postDate.setText(date);
        postLocation.setText(place);
        postText.setText(desc);
        //GlideApp.with(JournalEntryDetail.this).clear(imgView);
        GlideApp.with(JournalEntryDetail.this)
                .load(imgSrc)
                .centerCrop()
                .into(imgView);
    }

    public void deleteEntry()
    {
        Query entryQuery = dbRef.child("user_entries").orderByChild("entryID").equalTo(key);
        entryQuery.addListenerForSingleValueEvent(new ValueEventListener()
       {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot)
           {
               for (DataSnapshot postsnapshot :dataSnapshot.getChildren())
               {

                   postsnapshot.getRef().removeValue();

               }

           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError)
           {
           }
       });

            Intent in = new Intent(JournalEntryDetail.this, ViewJournalEntries.class);
            startActivity(in);
    }

    public void editEntry()
    {
        setContentView(R.layout.activity_main);
        ImageView chooseNewImg = findViewById(R.id.btn_add_entry);
        //FloatingActionButton updateEntry = findViewById(R.id.btn_upload_image);
        TextView editText = findViewById(R.id.choose_caption);
        ImageView editImg = findViewById(R.id.image_view);
        ImageView editLocation = findViewById(R.id.btn_add_location);
        TextView location = findViewById(R.id.entry_location);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) chooseNewImg.getLayoutParams();
        params.height = 0;
        params.width = 0;

        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) editLocation.getLayoutParams();
        params2.height = 0;
        params2.width = 0;

        editText.setText(desc);

        editText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                newText = editText.getText().toString().trim();
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                newText = editText.getText().toString().trim();
            }
        });
        location.setText(place);

        GlideApp.with(JournalEntryDetail.this)
                .load(imgSrc)
                .centerCrop()
                .into(editImg);

        final DatabaseReference editDb = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());


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
                editEntry();
                break;
            case R.id.delete_jentry:
                AlertDialog.Builder deleteAlert = new AlertDialog.Builder(JournalEntryDetail.this);
                deleteAlert.setTitle("Delete Entry");
                deleteAlert.setMessage("Are you sure you want to delete?");
                deleteAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        deleteEntry();
                    }
                });
                deleteAlert.setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                });
                deleteAlert.show();
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