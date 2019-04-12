package com.example.murryxi.traveldiaryapp;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.ArrayMap;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.JsonElement;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.geojson.gson.GeometryGeoJson;
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
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.annotation.FillOptions;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import static com.mapbox.mapboxsdk.style.expressions.Expression.division;
import static com.mapbox.mapboxsdk.style.expressions.Expression.exponential;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;

public class ViewVisitedPlaces extends AppCompatActivity implements
        OnMapReadyCallback,  MapboxMap.OnMapClickListener, PermissionsListener
{
    MapView mapView_stats;
    String mapBoxToken;
    private MapboxMap mapboxMap;
    private boolean markerSelected = false;

    private static final String SYMBOL_ICON_ID = "SYMBOL_ICON_ID";
    private static final String SOURCE_ID = "SOURCE_ID";
    private static final String LAYER_ID = "LAYER_ID";
    private static final String MARKER_ID = "id";
    private static final String IMAGE_URL = "imgUrl";
    private static final String PLACE_NAME = "location";
    private static final String FILL_COLOR = "color";
    private static final String COUNTRY_NAME = "name";


    private PermissionsManager permissionsManager;

    private static final String geoJsonSourceId = "geoJsonSourceLayerId";
    private static final String geoJsonLayerId = "polygonFillLayer";
    private static final String MARKER_LAYER_ID = "MARKER_LAYER_ID";
    private String symbolIcon = "symbolIconId";
    public DatabaseReference dbRef;
    public DatabaseReference dbRef2;
    public FirebaseUser currentUser;
    private ActionBar toolbar;
    private FirebaseAuth fbAuth;
    private MarkerInfoAdapter markerAdapter;
    private Location deviceLocation;
    private LocationComponent lComponent;
    private FeatureCollection featureCollection;
    private FeatureCollection fCollection;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private BottomNavigationView botNavigationView;
    final List<Feature> featureList = new ArrayList<>();
    final List<String> visitedCountries = new ArrayList<>();
    final List<Feature> featureKey = new ArrayList<>();
    private List<String> keys = new ArrayList<>();
    private List<JournalEntry> markerInfo = new ArrayList<>();
    private List<Double> latCoord = new ArrayList<>();
    private List<Double> lngCoord = new ArrayList<>();
    private List<String> places = new ArrayList<>();
    private List<String> imgUrl = new ArrayList<>();
    private List<String> countryList = new ArrayList<>();
    private RecyclerView markerRecycleView;
    private CardView markerInfoView;
    private FloatingActionButton showStats;
    private TextView location, distance, coordinates;
    private ImageView imgView;
    private LatLng homePoint;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_places);

        mapBoxToken = BuildConfig.MAPBOX_ACCESS_TOKEN;
        mapView_stats = findViewById(R.id.mapView_stats);
        mapView_stats.onCreate(savedInstanceState);
        botNavigationView = findViewById(R.id.bottom_navigation);
        showStats = findViewById(R.id.btn_view_stats);

        fbAuth = FirebaseAuth.getInstance();
        currentUser = fbAuth.getCurrentUser();
        markerInfoView = findViewById(R.id.single_location_cv);
       // markerRecycleView = findViewById(R.id.map_marker_rv);

        toolbar = getSupportActionBar();
        mapView_stats.getMapAsync(this);

        location = findViewById(R.id.cv_title_location);
        distance = findViewById(R.id.distance_from_home);
        coordinates = findViewById(R.id.coordinates);

        homePoint = new LatLng(53.3425, -6.26583);

        imgView = findViewById(R.id.cv_location_image);

        dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid()).child("user_entries");
        dbRef2 = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid()).child("visited_countries");

        showStats.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent(getApplicationContext(), ViewTravelStats.class);
                startActivity(i);
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
    public void onMapReady(@NonNull final MapboxMap mapboxMap)
    {
        this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded()
        {
            @Override
            public void onStyleLoaded(@NonNull Style style)
            {
                //initRecycleView();
                markerInfoView.setVisibility(View.INVISIBLE);
                addGeoJsonSource(style);
                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                        homePoint), 4));
                featureCollection = FeatureCollection.fromFeatures(new Feature[] {});
                featureList.clear();
                countryList.clear();
                visitedCountries.clear();
                if (featureCollection != null)
                {
                    List<String> markerKey = new ArrayList<>();
                    dbRef.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {

                            for (DataSnapshot snapshot : dataSnapshot.getChildren())
                            {
                                JournalEntry location = snapshot.getValue(JournalEntry.class);
                                // get values from journal entry object
                                latCoord.add(location.showLat());
                                lngCoord.add(location.showLong());
                                places.add(location.getPlaceName());
                                imgUrl.add(location.getImgUrl());
                                markerKey.add(location.getEntryID());

                            }

                            /* Add features for the points(markers) to the features list */
                            for (int i = 0; i < places.size(); i++)
                            {

                                featureList.add(Feature.fromGeometry(
                                        Point.fromLngLat(lngCoord.get(i), latCoord.get(i))));
                                featureList.get(i).addStringProperty(MARKER_ID, markerKey.get(i));
                                featureList.get(i).addStringProperty(IMAGE_URL, imgUrl.get(i));
                                featureList.get(i).addStringProperty(PLACE_NAME, places.get(i));
                            }

                            featureCollection = FeatureCollection.fromFeatures(featureList);
                            style.addSource(new GeoJsonSource("marker-source", featureCollection,
                                    new GeoJsonOptions().withCluster(true).withClusterMaxZoom(2).withClusterRadius(20)));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                            Toast.makeText(ViewVisitedPlaces.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

                    // getting countries list from database requires a different database reference path
                    dbRef2.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            for (DataSnapshot d : dataSnapshot.getChildren())
                            {
                                countryList.add(d.getKey());
                            }

                            visitedCountries.addAll(countryList);

                            List<Feature> polygonFeatures = fCollection.features();
                            List<Integer> indexList = new ArrayList<>();
                            List<Integer> indexList2 = new ArrayList<>();
                            indexList.clear();
                            indexList2.clear();
                            for (int i = 0; i < polygonFeatures.size(); i++)
                            {
                                Feature polyFeature = polygonFeatures.get(i);
                                for (int j = 0; j < countryList.size(); j++)
                                {
                                    if (polyFeature.getStringProperty(COUNTRY_NAME).equals(countryList.get(j)))
                                    {
                                        indexList.add(i);
                                    }
                                }

                                indexList2.add(i);
                                indexList2.removeAll(indexList);

                            }
                            Toast.makeText(ViewVisitedPlaces.this, "yas" + indexList2.size(), Toast.LENGTH_SHORT).show();

                            for (int i = 0; i < indexList.size(); i++)
                            {
                                Feature isVisited = polygonFeatures.get(indexList.get(i));
                                isVisited.addStringProperty(FILL_COLOR, "#58B6D5");
                            }
                            for (int i = 0; i < indexList2.size(); i++)
                            {
                                Feature notVisited = polygonFeatures.get(indexList2.get(i));
                                notVisited.addStringProperty(FILL_COLOR, "#104B5F");
                            }
                            fCollection = FeatureCollection.fromFeatures(polygonFeatures);
                            style.addSource(new GeoJsonSource(geoJsonSourceId,
                                    fCollection));
                            style.addLayer(new FillLayer(geoJsonLayerId, geoJsonSourceId).withProperties(fillOpacity(0.1f), fillColor(get(FILL_COLOR))));
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {

                        }
                    });
                }

                style.addImage("my-marker-image", BitmapFactory.decodeResource(ViewVisitedPlaces.this.getResources(), R.mipmap.ic_airplane));
                style.addLayer(new SymbolLayer("marker-layer", "marker-source").withProperties(PropertyFactory.iconImage("my-marker-image")
                ));
                style.addSource(new GeoJsonSource("selected-marker"));
                style.addLayer(new SymbolLayer("selected-marker-layer", "selected-marker")
                        .withProperties(PropertyFactory.iconImage("my-marker-image")
                                ));
                style.addImage("marker-icon-id-2",
                        BitmapFactory.decodeResource(
                                ViewVisitedPlaces.this.getResources(), R.mipmap.ic_home_location));

                addHomePoint(style);

                // add a marker pointing to home (Dublin)
                SymbolLayer symbolLayer = new SymbolLayer("layer-id-2", "source-id-2");
                symbolLayer.withProperties(PropertyFactory.iconImage("marker-icon-id-2"));
                style.addLayer(symbolLayer);

                mapboxMap.addOnMapClickListener(ViewVisitedPlaces.this);
            }
        });
    }

    @SuppressWarnings({"MissingPermission"})
    private void enableUserLocation(@NonNull Style loadedMapStyle)
    {
        if (PermissionsManager.areLocationPermissionsGranted(this))
        {
            lComponent = mapboxMap.getLocationComponent();

            lComponent.activateLocationComponent(this, loadedMapStyle);

            lComponent.setLocationComponentEnabled(true);

            lComponent.setCameraMode(CameraMode.TRACKING);

            lComponent.setRenderMode(RenderMode.GPS);

            LatLng currentLocation = new LatLng(lComponent.getLastKnownLocation());
            Toast.makeText(ViewVisitedPlaces.this, "C: " + currentLocation.getLatitude() + ", " + currentLocation.getLatitude(), Toast.LENGTH_SHORT).show();
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

    private void addGeoJsonSource(@NonNull Style loadedMapStyle)
    {
        fCollection = FeatureCollection.fromFeatures(new Feature[] {});
        String geoJson = loadGeoJsonFromAsset(getApplicationContext(), "countries.geo.json");
        fCollection = FeatureCollection.fromJson(geoJson);

    }

    private void addHomePoint(@NonNull Style style)
    {
        GeoJsonSource geoJsonSource = new GeoJsonSource("source-id-2", Feature.fromGeometry(Point.fromLngLat(-6.26583, 53.3425)));
        style.addSource(geoJsonSource);
    }
    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        Style style = mapboxMap.getStyle();
        if (style != null)
        {
            FillLayer fillLayer = (FillLayer) style.getLayer(geoJsonLayerId);
            final SymbolLayer selectedMarkerSymbolLayer =
                    (SymbolLayer) style.getLayer("selected-marker-layer");

            final PointF pixel = mapboxMap.getProjection().toScreenLocation(point);
            RectF rectF = new RectF(pixel.x - 10, pixel.y - 10, pixel.x + 10, pixel.y + 10);

            List<Feature> features = mapboxMap.queryRenderedFeatures(pixel, "marker-layer");
            List<Feature> selectedFeature = mapboxMap.queryRenderedFeatures(
                    pixel, "selected-marker-layer");
            List<Feature> fillFeature = mapboxMap.queryRenderedFeatures(rectF, geoJsonLayerId);

            if (selectedFeature.size() > 0 && markerSelected) {
                return false;
            }

            if (features.isEmpty()) {
                if (markerSelected) {
                    deselectMarker(selectedMarkerSymbolLayer);
                    markerInfoView.setVisibility(View.GONE);
                }
                return false;
            }

            GeoJsonSource source = style.getSourceAs("selected-marker");
            if (source != null) {
                source.setGeoJson(FeatureCollection.fromFeatures(
                        new Feature[]{Feature.fromGeometry(features.get(0).geometry())}));
            }

            if (markerSelected) {
                deselectMarker(selectedMarkerSymbolLayer);
            }

            if (features.size() > 0) {
                Feature feature = features.get(0);
                selectMarker(selectedMarkerSymbolLayer);
                markerInfoView.setVisibility(View.VISIBLE);
                botNavigationView.setVisibility(View.GONE);
                String name = feature.getStringProperty(PLACE_NAME);
                String url = feature.getStringProperty(IMAGE_URL);
                location.setText(name);
                GlideApp.with(ViewVisitedPlaces.this)
                        .load(url)
                        .optionalCenterCrop()
                        .placeholder(R.mipmap.ic_launcher)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imgView);

                // get the marker's coordinates
                LatLng currentLatLng = new LatLng(point.getLatitude(), point.getLongitude());
                DecimalFormat form = new DecimalFormat("0.0000");
                String coordinate = form.format(point.getLatitude()) + ", " + form.format(point.getLongitude());
                coordinates.setText(coordinate);

                DecimalFormat form1 = new DecimalFormat("0.00");
                double dist = currentLatLng.distanceTo(homePoint);
                double meterToKm = dist / 1000;
                String km = form1.format(meterToKm) + " km";
                distance.setText(km);

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
            public void onAnimationUpdate(ValueAnimator animation)
            {
                iconLayer.setProperties(
                        PropertyFactory.iconSize((float) markerAnimator.getAnimatedValue()));
            }
        });

        markerAnimator.start();
        markerSelected = false;
    }


    static String loadGeoJsonFromAsset(Context context, String filename)
    {
        try {
            // Load GeoJSON file from local asset folder
            InputStream is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
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
