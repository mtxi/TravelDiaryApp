package com.example.murryxi.traveldiaryapp;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;

public class AddJournalEntry extends AppCompatActivity
{
    private static final int PICK_IMAGE_REQUEST = 1;

    private FloatingActionButton chooseImgBtn;
    private FloatingActionButton postBtn;
    private EditText chooseCaption;
    private ImageView previewImg;
    private ProgressBar progressBar;

    private BottomNavigationView botNavigationView;
    private ActionBar toolbar;

    private Uri imgUri;

    private StorageReference clStorageRef;
    private DatabaseReference rtDatabaseRef;

    private StorageTask uplTask;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chooseImgBtn = findViewById(R.id.btn_add_entry);
        postBtn = findViewById(R.id.btn_upload_image);
        chooseCaption = findViewById(R.id.choose_caption);
        previewImg = findViewById(R.id.image_view);
        progressBar = findViewById(R.id.progress_bar);

        botNavigationView = findViewById(R.id.bottom_navigation);
        toolbar = getSupportActionBar();

        /* create references to access cloud storage and realtime database */
        clStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        rtDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        chooseImgBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openChooseFile();
            }
        });

        postBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (uplTask != null && uplTask.isInProgress())
                {
                    Toast.makeText(AddJournalEntry.this, "Upload in progress", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    uploadTextImg();
                }
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
                        return true;
                    case R.id.nav_location:
                        toolbar.setTitle("Maps");
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.nav_trips:
                        toolbar.setTitle("Trips");
                        in = new Intent (getBaseContext(), ViewScheduledTrips.class);
                        startActivity(in);
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.nav_statistics:
                        toolbar.setTitle("Travel Statistics");
                        overridePendingTransition(0,0);
                        return true;
                    default:
                        break;
                }
                return false;
            }
        });

    }

    /* lets user choose image from phone gallery */
    private void openChooseFile()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
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

            /* use Glide to load selected image to imageView */
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
                    /* if uploading post is complete */
                    if (task.isSuccessful())
                    {
                        Uri dlUri = task.getResult();
                        String tmpImgCaption = chooseCaption.getText().toString().trim();
                        Toast.makeText(getApplicationContext(), "Post Successful", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);

                        /* send text and image URL to realtime database */
                        PhotoUpload upl = new PhotoUpload(tmpImgCaption, dlUri.toString());
                        rtDatabaseRef.push().setValue(upl);

                        /* after successful post, reset text field and image view*/
                        chooseCaption.setText(null);
                        previewImg.setImageResource(0);

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
                    /* updates the progress bar while uploading the image and text*/
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

}
