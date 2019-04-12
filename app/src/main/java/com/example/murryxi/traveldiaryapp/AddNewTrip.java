package com.example.murryxi.traveldiaryapp;

import android.support.v7.app.ActionBar;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.JsonArray;
import com.vikktorn.picker.City;
import com.vikktorn.picker.CityPicker;
import com.vikktorn.picker.Country;
import com.vikktorn.picker.CountryPicker;
import com.vikktorn.picker.State;
import com.vikktorn.picker.StatePicker;
import com.vikktorn.picker.OnCityPickerListener;
import com.vikktorn.picker.OnCountryPickerListener;
import com.vikktorn.picker.OnStatePickerListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddNewTrip extends AppCompatActivity implements OnStatePickerListener, OnCountryPickerListener, OnCityPickerListener
{
    private static final int PICK_IMAGE_REQUEST = 1;
    //private Button pickCountry, pickCity, pickState;
    private TextView selectCountry, selectCity, selectDate, selectPhoto;
    private EditText tripNotes;
    private CountryPicker countryPicker;
    private StatePicker statePicker;
    private CityPicker cityPicker;
    private ImageView bgImageView;
    private Uri imgUri;
    private FloatingActionButton addTripBtn;
    private BottomNavigationView botNavigationView;
    private ActionBar toolbar;

    private StorageReference storageRef;
    private DatabaseReference dbRef;
    private FirebaseAuth fbAuth;
    private DatabaseReference dbUsers;
    private FirebaseUser currentUser;

    DatePickerDialog pickerDialog;
    TextView showDate;

    public static int countryID;
    public static int cityID;
    public static int stateID;

    public static List<City> cityList;
    public static List<State> stateList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_trip);

        selectCountry = findViewById(R.id.choose_country);
        selectCity = findViewById(R.id.choose_city);
        selectPhoto = findViewById(R.id.choose_photo);
        selectDate = findViewById(R.id.choose_date);
        bgImageView = findViewById(R.id.image_view);
        addTripBtn = findViewById(R.id.btn_add_new_trip);
        botNavigationView = findViewById(R.id.bottom_navigation);
        tripNotes = findViewById(R.id.trip_notes);

        toolbar = getSupportActionBar();

        fbAuth = FirebaseAuth.getInstance();
        currentUser = fbAuth.getCurrentUser();
        dbUsers = FirebaseDatabase.getInstance().getReference("users");
        storageRef = FirebaseStorage.getInstance().getReference("newTrip_photo");
        dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid());

        cityList = new ArrayList<>();
        stateList = new ArrayList<>();


        try
        {
            getStateJson();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        try
        {
            getCityJson();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        selectDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final Calendar cdr = Calendar.getInstance();
                int day = cdr.get(Calendar.DAY_OF_MONTH);
                int month = cdr.get(Calendar.MONTH);
                int year = cdr.get(Calendar.YEAR);

                pickerDialog = new DatePickerDialog(AddNewTrip.this, new DatePickerDialog.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
                    {
                        selectDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                    }
                }, year, month, day);

                pickerDialog.show();
            }
        });

        countryPicker = new CountryPicker.Builder().with(this).listener(this).build();

       selectCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                countryPicker.showDialog(getSupportFragmentManager());
            }
        });

        selectCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                statePicker.showDialog(getSupportFragmentManager());
            }
        });

        selectPhoto.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                imagePicker();
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
                        overridePendingTransition(0,0);
                    default:
                        break;
                }
                return true;
            }
        });

    }

    /* choose background image for card view (optional) */
    private void imagePicker()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    private String getFileExt(Uri uri)
    {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
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

            /* use Glide to preview selected image to image View */
            GlideApp.with(this).load(imgUri).centerCrop().into(bgImageView);
        }

    }

    private void addNewTrip()
    {
        /* if there is a reference path to the chosen image */
        if (imgUri != null) {
            final StorageReference fileRef = storageRef.child(System.currentTimeMillis() + "." + getFileExt(imgUri));

            fileRef.putFile(imgUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    /* get image URL to download the image file */
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    /* if uploading entry is complete */
                    if (task.isSuccessful()) {
                        final Uri dlUri = task.getResult();
                        final String tmpCountry = selectCountry.getText().toString().trim();
                        final String tmpCity = selectCity.getText().toString().trim();
                        final String tmpDate = selectDate.getText().toString().trim();
                        final String tmpNotes = tripNotes.getText().toString().trim();

                        // get unique ID of the trip for future reference
                        final String tripKey = dbRef.child("user_trips").push().getKey();

                        Toast.makeText(getApplicationContext(), "New trip has been added", Toast.LENGTH_LONG).show();


                        /* send trip details to realtime database */
                        final TripInfo info = new TripInfo(tripKey, tmpCountry, tmpCity, tmpDate, tmpNotes, dlUri.toString());

                        /* add all data to current user */
                        dbUsers.addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                dbRef.child("user_trips").child(tripKey).setValue(info);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        /* after successful post, reset text field and image view*/
                        selectCountry.setText(null);
                        bgImageView.setImageResource(0);
                        selectCity.setText(null);
                        selectDate.setText(null);

                        Intent finish = new Intent(AddNewTrip.this, ViewJournalEntries.class);
                        finish.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(finish);

                    } else {
                        Toast.makeText(AddNewTrip.this, "Failed to add trip:" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(AddNewTrip.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            fileRef.putFile(imgUri).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(AddNewTrip.this, "Adding Trip...", Toast.LENGTH_SHORT).show();
                    /* updates the progress bar while uploading the journal entry*/
                    //double uplProgress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    //Toast.makeText(AddNewTrip.this, "Adding Trip: %" + Math.round(uplProgress), Toast.LENGTH_LONG).show();

                }
            });
        }
        else {
                Toast.makeText(this, "Please select image", Toast.LENGTH_SHORT).show();
            }
    }

    @Override
    public void onSelectCountry(Country country)
    {
        selectCountry.setText(country.getName());
        countryID = country.getCountryId();
        statePicker.equalStateObject.clear();
        cityPicker.equalCityObject.clear();

        for (int i = 0; i < stateList.size(); i++)
        {
            statePicker = new StatePicker.Builder().with(this).listener(this).build();
            State stateInfo = new State();
            if(stateList.get(i).getCountryId() == countryID)
            {
                stateInfo.setStateId(stateList.get(i).getStateId());
                stateInfo.setStateName(stateList.get(i).getStateName());
                stateInfo.setCountryId(stateList.get(i).getCountryId());
                stateInfo.setFlag(country.getFlag());
                statePicker.equalStateObject.add(stateInfo);
            }
        }

    }

    @Override
    public void onSelectState(State state)
    {
        /*cityPicker.equalCityObject.clear();*/
        selectCity.setText(state.getStateName());
        stateID = state.getStateId();

        for (int i = 0; i < cityList.size(); i++)
        {
            cityPicker = new CityPicker.Builder().with(this).listener(this).build();
            City cityInfo = new City();
            if (cityList.get(i).getStateId() == stateID)
            {
                cityInfo.setCityId(cityList.get(i).getCityId());
                cityInfo.setCityName(cityList.get(i).getCityName());
                cityInfo.setStateId(cityList.get(i).getStateId());

                /*cityPicker.equalCityObject.add(cityInfo);*/
            }
        }
    }

    @Override
    public void onSelectCity(City city)
    {
        /*cityName.setText(city.getCityName());*/
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture)
    {

    }

    public void getStateJson() throws JSONException
    {
        String json = null;
        try
        {
            InputStream inputStream = getAssets().open("states.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        JSONObject jsonObject = new JSONObject(json);
        JSONArray events = jsonObject.getJSONArray("states");
        for (int j = 0; j < events.length(); j++)
        {
            JSONObject cit = events.getJSONObject(j);
            State stateInfo = new State();

            stateInfo.setStateId(Integer.parseInt(cit.getString("id")));
            stateInfo.setStateName(cit.getString("name"));
            stateInfo.setCountryId(Integer.parseInt(cit.getString("country_id")));
            stateList.add(stateInfo);
        }
    }

    public void getCityJson() throws JSONException
    {
        String json = null;
        try
        {
            InputStream inputStream = getAssets().open("cities.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        JSONObject jsonObject = new JSONObject(json);
        JSONArray events = jsonObject.getJSONArray("cities");

        for (int j = 0; j < events.length(); j++)
        {
            JSONObject cit = events.getJSONObject(j);
            City cityData = new City();

            cityData.setCityId(Integer.parseInt(cit.getString("id")));
            cityData.setCityName(cit.getString("name"));
            cityData.setStateId(Integer.parseInt(cit.getString("state_id")));
            cityList.add(cityData);
        }
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
            fbAuth.signOut();
        }
        return super.onOptionsItemSelected(item);
    }

}
