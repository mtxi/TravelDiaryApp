package com.example.murryxi.traveldiaryapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddJournalEntry extends AppCompatActivity
{
    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView chooseImgBtn;
    //private FloatingActionButton postBtn;
    private EditText chooseCaption;
    private ImageView previewImg;
    private ProgressBar progressBar;
    private ImageView addLocationBtn;
    private TextView entryDate;
    public TextView location;

    public double lat;
    public double lng;
    public String placeName;

    private BottomNavigationView botNavigationView;
    private ActionBar toolbar;
    private Uri imgUri;

    private StorageReference clStorageRef;
    private DatabaseReference rtDatabaseRef;
    private FirebaseAuth fbAuth;
    private DatabaseReference dbUsers;
    private FirebaseUser currentUser;

    private StorageTask uplTask;

    DatePickerDialog pickerDialog;
    @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chooseImgBtn = findViewById(R.id.btn_add_entry);
        chooseCaption = findViewById(R.id.choose_caption);
        previewImg = findViewById(R.id.image_view);
        progressBar = findViewById(R.id.progress_bar);
        addLocationBtn = findViewById(R.id.btn_add_location);
        location = findViewById(R.id.entry_location);
        entryDate = findViewById(R.id.entry_date);

        Intent i = getIntent();
        String placeName = i.getStringExtra("Location");
        location.setText(placeName);

        Calendar cdr = Calendar.getInstance();
        String format = new SimpleDateFormat("E, MMMM d, yyyy", Locale.UK).format(cdr.getTime());

        entryDate.setText(format);

        botNavigationView = findViewById(R.id.bottom_navigation);
        toolbar = getSupportActionBar();

        /* create references to access cloud storage and realtime database */
        clStorageRef = FirebaseStorage.getInstance().getReference("uploads");

        fbAuth = FirebaseAuth.getInstance();
        currentUser = fbAuth.getCurrentUser();
        dbUsers = FirebaseDatabase.getInstance().getReference("users");
        rtDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid());

        entryDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final Calendar cdr = Calendar.getInstance();
                int day = cdr.get(Calendar.DAY_OF_MONTH);
                int month = cdr.get(Calendar.MONTH);
                int year = cdr.get(Calendar.YEAR);

                pickerDialog = new DatePickerDialog(AddJournalEntry.this, new DatePickerDialog.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
                    {
                        cdr.set(Calendar.YEAR, year);
                        cdr.set(Calendar.MONTH, month);
                        cdr.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        String format = new SimpleDateFormat("E, MMMM d, yyyy", Locale.UK).format(cdr.getTime());
                        entryDate.setText(format);
                    }
                }, year, month, day);

                pickerDialog.show();
            }
        });

        chooseCaption.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                chooseCaption.setFocusable(true);
                chooseCaption.requestFocus(View.FOCUS_RIGHT);

            }
        });

        /* Hide soft keyboard when clicking outside Edittext*/
        chooseCaption.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if(!hasFocus)
                {
                    hideKeyboard(v);
                }
            }

        });

        chooseImgBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openChooseFile();
            }
        });


        addLocationBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent (AddJournalEntry.this, PickEntryLocation.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityIfNeeded(i, 0);
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
                        toolbar.setTitle("Travel Statistics");
                        i = new Intent (getApplicationContext(), ViewVisitedPlaces.class);
                        startActivity(i);
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
    protected void onRestoreInstanceState(Bundle savedState) {
        super.onRestoreInstanceState(savedState);
    }

    /* lets user choose image from phone gallery */
    private void openChooseFile()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    public void hideKeyboard(View view)
    {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        /* if the user has chosen an image from phone gallery */
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null)
        {
            imgUri = data.getData();

            /* use Glide to preview selected image to imageView */
            GlideApp.with(this).load(imgUri).into(previewImg);

        }

    }

    private String getFileExt(Uri uri)
    {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void uploadTextImg()
    {
        /* if there is a path reference to the chosen image */
        if (imgUri != null)
        {

            final StorageReference fileRef = clStorageRef.child(System.currentTimeMillis() + "." + getFileExt(imgUri));

            fileRef.putFile(imgUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                {
                    if (!task.isSuccessful())
                    {
                        throw task.getException();
                     }
                    /* get image URL to download the image file */
                    return fileRef.getDownloadUrl();
            }
            }).addOnCompleteListener(new OnCompleteListener<Uri>()
            {
                @Override
                public void onComplete(@NonNull Task<Uri> task)
                {
                    /* if uploading entry is complete */
                    if (task.isSuccessful())
                    {
                        final Uri dlUri = task.getResult();
                        final String date = entryDate.getText().toString().trim();
                        final String tmpImgCaption = chooseCaption.getText().toString().trim();
                        JournalEntry p = new JournalEntry();
                        final String tmpPlaceName = p.getEntryLocation();
                        final double tmpLat = p.getPlaceLat();
                        final double tmpLong = p.getPlaceLong();
                        final boolean isVisited = true;

                        Intent i = getIntent();
                        final String cityVisited = i.getStringExtra("cityVisited");
                        final String countryVisited = i.getStringExtra("countryVisited");
                        final String entryKey = rtDatabaseRef.child("user_entries").push().getKey();

                        Toast.makeText(getApplicationContext(), "Post Successful", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);

                        Intent finish = new Intent(AddJournalEntry.this, ViewJournalEntries.class);
                        finish.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(finish);
                        /* send journal entry data to realtime database */
                        final JournalEntry upl = new JournalEntry(entryKey, date, tmpImgCaption, dlUri.toString(), tmpPlaceName, tmpLat, tmpLong);
                        /* add all data to current user's ID key */
                        dbUsers.addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                rtDatabaseRef.child("user_entries").child(entryKey).setValue(upl);

                                if (!dataSnapshot.hasChild(countryVisited))
                                {
                                    rtDatabaseRef.child("visited_countries").child(countryVisited).setValue(isVisited);
                                }

                                if (!dataSnapshot.hasChild(cityVisited))
                                {
                                    rtDatabaseRef.child("visited_cities").child(cityVisited).setValue(isVisited);
                                }

                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError)
                            {

                            }
                        });

                        /* after successful post, reset text field and image view*/
                        chooseCaption.setText(null);
                        previewImg.setImageResource(0);
                        location.setText(null);

                    }
                    else
                    {
                        Toast.makeText(AddJournalEntry.this, "Post failed:" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e)
                {

                    Toast.makeText(AddJournalEntry.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            fileRef.putFile(imgUri).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>()
            {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    /* updates the progress bar while uploading the journal entry*/
                    double uplProgress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    progressBar.setProgress((int) uplProgress);

                }
            });
        }
        else
        {
            Toast.makeText(this, "Please select image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.post_option, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mybutton)
        {
            uploadTextImg();
        }
        return super.onOptionsItemSelected(item);
    }

}