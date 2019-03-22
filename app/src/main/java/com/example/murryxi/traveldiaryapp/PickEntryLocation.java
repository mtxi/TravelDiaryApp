package com.example.murryxi.traveldiaryapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static java.lang.String.valueOf;

import com.mapbox.mapboxsdk.geometry.LatLng;

public class PickEntryLocation extends AppCompatActivity implements OnMapReadyCallback
{
    public static final String TAG = "PickEntryLocation";
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;

    private DatabaseReference fbRef;

    private ActionBar toolbar;
    private TextView location;
    private String geoJsonSourceId = "geoJsonSourceLayerId";
    private String symbolIcon = "symbolIconId";
    private Button addLocationButton;

    MapView mapView_location;
    String mapBoxToken;
    private MapboxMap mapboxMap;
    private static String placeName;
    private static double placeLat;
    private static double placeLong;
    public LatLng placeCoords;
    private FloatingActionButton searchLocation;
    public TextView locationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry_location);
        toolbar = getSupportActionBar();

        locationTextView = findViewById(R.id.textView_location_info);

        mapBoxToken = BuildConfig.MAPBOX_ACCESS_TOKEN;
        mapView_location = findViewById(R.id.mapView_select_location);
        mapView_location.onCreate(savedInstanceState);

        addLocationButton = findViewById(R.id.temp_btn_add_location);

        mapView_location.getMapAsync(this);

        fbRef = FirebaseDatabase.getInstance().getReference("Users").child("uploads");


    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap)
    {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded()
        {
            @Override
            public void onStyleLoaded(@NonNull Style style)
            {
                startLocationSearch();

                style.addImage(symbolIcon, BitmapFactory.decodeResource(
                        PickEntryLocation.this.getResources(), R.mipmap.blue_map_marker
                ));

                setUpLayer(style);

                setUpSource(style);
            }
        });
    }


    public void startLocationSearch()
    {
        searchLocation = findViewById(R.id.btn_search_location);
        searchLocation.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent in = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(mapBoxToken)
                        .placeOptions(PlaceOptions.builder()
                        .backgroundColor(Color.parseColor("#EEEEEE"))
                        .limit(10)
                        .build(PlaceOptions.MODE_CARDS))
                        .build(PickEntryLocation.this);

                startActivityForResult(in, REQUEST_CODE_AUTOCOMPLETE);
            }
        });
    }

    private void setUpSource(@NonNull Style loadedMapStyle)
    {
        loadedMapStyle.addSource(new GeoJsonSource(geoJsonSourceId));
    }

    private void setUpLayer(@NonNull Style loadedMapStyle)
    {
        loadedMapStyle.addLayer(new SymbolLayer("SYMBOL_LAYER_ID", geoJsonSourceId).withProperties(iconImage(symbolIcon),
                iconOffset(new Float[] {0f, -8f})
        ));
    }


    private void displayAddButton()
    {
        addLocationButton.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE)
        {
            // display 'Select Location' button when a place has been selected
            displayAddButton();

            final CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

            /* Display location name on map */
            locationTextView.setText(String.format(getString(R.string.marker_location_info), selectedCarmenFeature.placeName().toString()));
            placeCoords = new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                ((Point) selectedCarmenFeature.geometry()).longitude());
            placeLat = placeCoords.getLatitude();
            placeLong = placeCoords.getLongitude();

            /* get textView content to String */
            placeName = locationTextView.getText().toString().trim();

            addLocationButton.setOnClickListener(
                    new View.OnClickListener(){
                        @Override
                        public void onClick(View v)
                        {

                            AddJournalEntry a = new AddJournalEntry();
                            JournalEntry p = new JournalEntry();

                            a.lat = placeLat;
                            a.lng = placeLong;
                            a.placeName = placeName;

                            // send data to AddJournalEntry class to be pushed to database
                            p.setLatitude(placeLat);
                            p.setLongitude(placeLong);
                            p.setPlaceName(placeName);

                            Toast.makeText(PickEntryLocation.this, "Location: " + a.lat + a.lng + a.placeName, Toast.LENGTH_SHORT).show();

                            Intent i = new Intent(PickEntryLocation.this, AddJournalEntry.class);
                            i.putExtra("Location", a.placeName);
                            i.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                            startActivityIfNeeded(i, 0);
                            /*openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivityIfNeeded(openMainActivity, 0);*/

                        }
                    }
            );

            if (mapboxMap != null)
            {
                Style style = mapboxMap.getStyle();
                if (style != null)
                {
                    GeoJsonSource source = style.getSourceAs(geoJsonSourceId);
                    if (source != null)
                    {
                        source.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[] {Feature.fromJson(selectedCarmenFeature.toJson())}
                        ));
                    }

                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                            .target(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                                    ((Point) selectedCarmenFeature.geometry()).longitude()))
                            .zoom(14)
                            .build()), 2000);
                }
            }

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView_location.onStart();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mapView_location.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mapView_location.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView_location.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView_location.onSaveInstanceState(outState);
    }


}
