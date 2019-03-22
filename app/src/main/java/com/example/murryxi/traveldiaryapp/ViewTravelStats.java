package com.example.murryxi.traveldiaryapp;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.ArrayMap;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.JsonElement;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.BubbleLayout;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import static com.mapbox.mapboxsdk.style.expressions.Expression.division;
import static com.mapbox.mapboxsdk.style.expressions.Expression.exponential;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.versionedparcelable.NonParcelField;

import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;

public class ViewTravelStats extends AppCompatActivity implements
        OnMapReadyCallback,  MapboxMap.OnMapClickListener, PermissionsListener
{
    MapView mapView_stats;
    String mapBoxToken;
    private MapboxMap mapboxMap;
    private boolean markerSelected = false;

    private static final String SYMBOL_ICON_ID = "SYMBOL_ICON_ID";
    private static final String SOURCE_ID = "SOURCE_ID";
    private static final String LAYER_ID = "LAYER_ID";

    private PermissionsManager permissionsManager;

    private String geoJsonSourceId = "geoJsonSourceLayerId";
    private String symbolIcon = "symbolIconId";
    public DatabaseReference dbRef;
    public DatabaseReference queryRef;
    public FirebaseUser currentUser;
    private ActionBar toolbar;
    private FirebaseAuth fbAuth;
    private MarkerInfoAdapter markerAdapter;
    private FeatureCollection featureCollection;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private BottomNavigationView botNavigationView;
    final List<Feature> featureList = new ArrayList<>();
    private List<JournalEntry> markerInfo = new ArrayList<>();
    private RecyclerView markerRecycleView;
    private CardView markerInfoView;
    private TextView location;
    private ImageView imgView;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_stats);

        mapBoxToken = BuildConfig.MAPBOX_ACCESS_TOKEN;
        mapView_stats = findViewById(R.id.mapView_stats);
        mapView_stats.onCreate(savedInstanceState);
        botNavigationView = findViewById(R.id.bottom_navigation);

        fbAuth = FirebaseAuth.getInstance();
        currentUser = fbAuth.getCurrentUser();
        markerInfoView = findViewById(R.id.single_location_cardview);

        toolbar = getSupportActionBar();
        mapView_stats.getMapAsync(this);

        location = findViewById(R.id.cv_title_location);
        imgView = findViewById(R.id.cv_location_image);

        dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid()).child("user_entries");

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
                        i = new Intent (getApplicationContext(), ViewTravelStats.class);
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
    public void onMapReady(@NonNull final MapboxMap mapboxMap)
    {
        this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(Style.DARK, new Style.OnStyleLoaded()
        {
            @Override
            public void onStyleLoaded(@NonNull Style style)
            {
                enableUserLocation(style);
                markerInfoView.setVisibility(View.INVISIBLE);
                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                        12.099, -79.045), 3));
                featureCollection = FeatureCollection.fromFeatures(new Feature[] {});
                featureList.clear();
                if (featureCollection != null)
                {
                    List<Double> latCoord = new ArrayList<>();
                    List<Double> lngCoord = new ArrayList<>();
                    List<LatLng> points = new ArrayList<>();
                    ArrayMap<String, LatLng> keyPoints = new ArrayMap<>();
                    dbRef.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            //markerCoordinates.clear();

                            for (DataSnapshot snapshot : dataSnapshot.getChildren())
                            {
                                JournalEntry location = snapshot.getValue(JournalEntry.class);
                                latCoord.add(location.showLat());
                                lngCoord.add(location.showLong());

                            }

                            for (int i = 0; i<lngCoord.size(); i++)
                            {
                                /*LatLng pt = new LatLng(lngCoord.get(i), latCoord.get(i));
                                points.add(pt);*/

                                featureList.add(Feature.fromGeometry(
                                        Point.fromLngLat(lngCoord.get(i), latCoord.get(i))));
                            }


                            featureCollection = FeatureCollection.fromFeatures(featureList);

                            style.addSource(new GeoJsonSource("marker-source", featureCollection,
                                    new GeoJsonOptions().withCluster(true).withClusterMaxZoom(2)));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                            Toast.makeText(ViewTravelStats.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
                }


                SymbolLayer unclustered = new SymbolLayer("unclustered-points", "marker-source");

                style.addImage("my-marker-image", BitmapFactory.decodeResource(ViewTravelStats.this.getResources(), R.drawable.ic_red_marker));
                style.addLayer(new SymbolLayer("marker-layer", "marker-source").withProperties(PropertyFactory.iconImage("my-marker-image")
                ));
                style.addSource(new GeoJsonSource("selected-marker"));
                style.addLayer(new SymbolLayer("selected-marker-layer", "selected-marker")
                        .withProperties(PropertyFactory.iconImage("my-marker-image")
                                ));


                mapboxMap.addOnMapClickListener(ViewTravelStats.this);
            }
        });
    }

    @SuppressWarnings({"MissingPermission"})
    private void enableUserLocation(@NonNull Style loadedMapStyle)
    {
        if (PermissionsManager.areLocationPermissionsGranted(this))
        {
            LocationComponent lComponent = mapboxMap.getLocationComponent();

            lComponent.activateLocationComponent(this, loadedMapStyle);

            lComponent.setLocationComponentEnabled(true);

            lComponent.setCameraMode(CameraMode.TRACKING);

            lComponent.setRenderMode(RenderMode.GPS);

        }
        else
        {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain)
    {
        Toast.makeText(this, "Using your current location...?", Toast.LENGTH_LONG).show();
    }


    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style)
                {
                    enableUserLocation(style);
                }
            });
        } else {
            Toast.makeText(this, "Sorry, don't know where you are", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initRecycleView()
    {
        markerRecycleView.setHasFixedSize(true);
        markerRecycleView.setVisibility(View.INVISIBLE);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, true);
        markerRecycleView.setLayoutManager(layoutManager);
        markerRecycleView.setItemAnimator(new DefaultItemAnimator());

    }

    private void addGeoJsonSource(@NonNull Style loadedMapStyle)
    {
        try
        {
            loadedMapStyle.addSource(new GeoJsonSource("waypoints", new URL("C:\\Users\\murryXi\\AndroidStudioProjects\\TravelDiaryApp\\app\\src\\main\\assets\\waypoints.geojson")));
        }
    }
    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        Style style = mapboxMap.getStyle();
        if (style != null) {
            final SymbolLayer selectedMarkerSymbolLayer =
                    (SymbolLayer) style.getLayer("selected-marker-layer");

            final PointF pixel = mapboxMap.getProjection().toScreenLocation(point);
            List<Feature> features = mapboxMap.queryRenderedFeatures(pixel, "marker-layer");
            List<Feature> selectedFeature = mapboxMap.queryRenderedFeatures(
                    pixel, "selected-marker-layer");

            if (selectedFeature.size() > 0 && markerSelected) {
                return false;
            }

            if (features.isEmpty())
            {
                if(markerSelected)
                {
                    deselectMarker(selectedMarkerSymbolLayer);
                }
                return false;
            }

            GeoJsonSource source = style.getSourceAs("selected-marker");
            if (source != null) {
                source.setGeoJson(FeatureCollection.fromFeatures(
                        new Feature[]{Feature.fromGeometry(features.get(0).geometry())}));
            }

            if (markerSelected)
            {
                deselectMarker(selectedMarkerSymbolLayer);
            }
            if (features.size() > 0)
            {
                selectMarker(selectedMarkerSymbolLayer);
                Toast.makeText(this, "Points: " + point.getLatitude() + ", " + point.getLongitude(), Toast.LENGTH_LONG).show();
                markerInfoView.setVisibility(View.VISIBLE);
                dbRef.orderByChild("placeLat").equalTo(point.getLatitude())
                        .addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren())
                        {
                            JournalEntry j = snapshot.getValue(JournalEntry.class);
                            if ((point.getLatitude() == j.showLat()) && (point.getLongitude() == j.showLong()))
                            {
                                location.setText(j.getPlaceName());
                                GlideApp.with(ViewTravelStats.this)
                                        .load(j.getImgUrl())
                                        .centerCrop()
                                        .placeholder(R.mipmap.ic_launcher)
                                        .transition(DrawableTransitionOptions.withCrossFade())
                                        .into(imgView);
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                        Toast.makeText(ViewTravelStats.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

            }

        }
        return true;
    }

    private void selectMarker(final SymbolLayer iconLayer)
    {
        ValueAnimator markerAnimator = new ValueAnimator();
        markerAnimator.setObjectValues(1f, 2f);
        markerAnimator.setDuration(300);
        markerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                iconLayer.setProperties(PropertyFactory.iconSize((float) animation.getAnimatedValue()));
                /*dbRef.addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        /* clear array to avoid duplicate views
                        markerInfo.clear();

                        /* add array of journal entries to recycle view
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren())
                        {
                            JournalEntry u = postSnapshot.getValue(JournalEntry.class);
                            markerInfo.add(u);
                        }
                        /* set adapter to recycle view
                        markerAdapter = new MarkerInfoAdapter(ViewTravelStats.this, markerInfo);
                        markerRecycleView.setAdapter(markerAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                        Toast.makeText(ViewTravelStats.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });*/
            }
        });
        markerAnimator.start();
        markerSelected=true;
    }

    private void deselectMarker(final SymbolLayer iconLayer)
    {
        ValueAnimator markerAnimator = new ValueAnimator();
        markerAnimator.setObjectValues(2f,1f);
        markerAnimator.setDuration(300);
        markerAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                iconLayer.setProperties(
                        PropertyFactory.iconSize((float) markerAnimator.getAnimatedValue()));
            }
        });

        markerAnimator.start();
        markerSelected = false;
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
    protected void onStart() {
        super.onStart();
        mapView_stats.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView_stats.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView_stats.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView_stats.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView_stats.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView_stats.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView_stats.onSaveInstanceState(outState);
    }
}
