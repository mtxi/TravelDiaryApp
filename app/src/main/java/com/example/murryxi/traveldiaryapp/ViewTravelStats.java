package com.example.murryxi.traveldiaryapp;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vikktorn.picker.State;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ViewTravelStats extends AppCompatActivity
{
    private TextView textCountry;
    private TextView textCity;
    private TextView numCity;
    private TextView numCountry;
    private TextView progressText;
    private LinearLayout expand1;
    private LinearLayout expand2;
    private boolean isExpand;
    private DatabaseReference dbRef;
    private FirebaseAuth fbAuth;
    private FirebaseUser currentUser;
    public List<String> getCityList;
    private ListView countryListView;
    private ListView cityListView;
    private ArrayAdapter<String> countryListAdapter;
    private ArrayAdapter<String> cityListAdapter;
    private List<String> cityList;
    private ProgressBar progressBar;

    List<String> countryList = new ArrayList<>();
    List<String> getCountryList = new ArrayList<>();

    // ArrayList to store contents from JSON
    List<String> continentJson = new ArrayList<>();
    List<String> countryJson = new ArrayList<>();

    // ArrayList to store unique continents
    List<String> newContinentJson = new ArrayList<>();
    List<String> cList = new ArrayList<>();

    // ArrayLists to collect group of indexes which specifies a continent in JSON
    List<Integer> euIndex = new ArrayList<>();
    List<Integer> naIndex = new ArrayList<>();
    List<Integer> asIndex = new ArrayList<>();
    List<Integer> saIndex = new ArrayList<>();

    // ArrayLists to store countries visited based on continent
    List<String> europe = new ArrayList<>();
    List<String> asia = new ArrayList<>();
    List<String> na = new ArrayList<>();
    List<String> sa = new ArrayList<>();

    private PieChart pieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_stats);

        textCountry = findViewById(R.id.text_country_visited);
        textCity = findViewById(R.id.text_city_visited);
        expand1 = findViewById(R.id.expandableLayout1);
        expand2 = findViewById(R.id.expandableLayout2);
        countryListView = findViewById(R.id.list_visited_countries);
        cityListView = findViewById(R.id.list_visited_cities);
        numCity = findViewById(R.id.number_city_visited);
        numCountry = findViewById(R.id.number_country_visited);
        pieChart = findViewById(R.id.linechart);
        progressBar = findViewById(R.id.progress);
        progressText = findViewById(R.id.text_progress2);

        expand1.setVisibility(View.GONE);
        expand2.setVisibility(View.GONE);
        isExpand = false;

        //pieChart = findViewById(R.id.piechart);

        fbAuth = FirebaseAuth.getInstance();
        currentUser = fbAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference().child("users");

        textCountry.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(!isExpand)
                {
                    expand1.setVisibility(View.VISIBLE);
                    isExpand = true;
                }
                else
                {
                    expand1.setVisibility(View.GONE);
                    isExpand = false;
                }
            }
        });

        textCity.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(!isExpand)
                {
                    expand2.setVisibility(View.VISIBLE);
                    isExpand = true;
                }
                else
                {
                    expand2.setVisibility(View.GONE);
                    isExpand = false;
                }
            }
        });

        preparePieChart();
        populateCityList();

        try
        {
            getContinentsJson();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        String eu = "Europe";
        String as = "Asia";
        String na = "North America";
        String sa = "South America";
        for (int i = 0 ; i < continentJson.size(); i++)
        {
            // remove duplicates from continents list - should be 7 unique elements
            if (!newContinentJson.contains(continentJson.get(i)))
            {
                newContinentJson.add(continentJson.get(i));
            }

            if(eu.equals(continentJson.get(i)))
            {
                euIndex.add(i);
            }
            else if(as.equals(continentJson.get(i)))
            {
                asIndex.add(i);
            }
            else if (na.equals(continentJson.get(i)))
            {
                naIndex.add(i);
            }
            else if(sa.equals(continentJson.get(i)))
            {
                saIndex.add(i);
            }

        }

    }

    private void preparePieChart()
    {
        dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid()).child("visited_countries");
        dbRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                for (DataSnapshot d : dataSnapshot.getChildren())
                {
                    countryList.add(d.getKey());
                }

                getCountryList.addAll(countryList);
                int numCo = getCountryList.size();
                numCountry.setText(String.valueOf(numCo));

                for (int i = 0; i < euIndex.size(); i++)
                {
                    europe.add(countryJson.get(euIndex.get(i)));
                }
                for (int i = 0; i < naIndex.size(); i++)
                {
                    na.add(countryJson.get(naIndex.get(i)));
                }
                for (int i = 0; i < asIndex.size(); i++)
                {
                    asia.add(countryJson.get(asIndex.get(i)));
                }
                for (int i = 0; i < saIndex.size(); i++)
                {
                    sa.add(countryJson.get(saIndex.get(i)));
                }
                europe.retainAll(getCountryList);
                na.retainAll(getCountryList);
                asia.retainAll(getCountryList);
                sa.retainAll(getCountryList);

                int numEu = europe.size();
                int numNa = na.size();
                int numAs = asia.size();
                int numSa = sa.size();

                /* Preparing the pie chart */
                List<PieEntry> entries = new ArrayList<>();

                entries.add(new PieEntry(numEu, "Europe"));
                entries.add(new PieEntry(numNa, "N. America"));
                entries.add(new PieEntry(numAs, "Asia"));
                entries.add(new PieEntry(numSa, "S. America"));

                PieDataSet set = new PieDataSet(entries, "Countries visited per continent");
                PieData data = new PieData(set);
                pieChart.setData(data);
                pieChart.getDescription().setEnabled(false);
                pieChart.setExtraOffsets(5, 10, 5, 5);
                pieChart.setDrawEntryLabels(false);

                Legend l = pieChart.getLegend();
                l.setEnabled(true);

                pieChart.setDrawHoleEnabled(true);
                pieChart.setHoleColor(Color.WHITE);

                pieChart.animateY(1400, Easing.EaseInOutQuad); // Rotate Event
                pieChart.setRotationAngle(0);

                // enable rotation of the chart by touch
                pieChart.setRotationEnabled(true);
                pieChart.setHighlightPerTapEnabled(true);
                set.setDrawIcons(true);
                set.setIconsOffset(new MPPointF(0, 40));
                set.setSelectionShift(5f);
                set.setDrawValues(true);

                List<Integer> colors = new ArrayList<>();
                for (int c : ColorTemplate.MATERIAL_COLORS)
                {
                    colors.add(c);
                }
                set.setColors(colors);
                PieData data2 = new PieData(set);
                data2.setValueTextSize(11f);
                data2.setValueTextColor(Color.WHITE);
                pieChart.setData(data);

                pieChart.invalidate();

                /* Setting the progress bar meter */
                double totalVisited = (numAs + numEu + numNa + numSa);
                int totalCountries = 195;

                double percentage = 0.0;
                percentage = (totalVisited / totalCountries) * 100;

                progressBar.setMax(totalCountries);
                progressBar.setProgress((int)totalVisited);
                DecimalFormat form = new DecimalFormat("0.00");
                String progress = form.format(percentage) + "% " + "of the world!";
                progressText.setText(progress);

                Toast.makeText(ViewTravelStats.this, "+ " + totalVisited + percentage, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        countryListAdapter = new ArrayAdapter<String>(this, R.layout.list_row, getCountryList);
        countryListView.setAdapter(countryListAdapter);
    }

    public void getContinentsJson() throws JSONException
    {
        String json = null;
        try
        {
            InputStream is = getAssets().open("csvjson.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        JSONArray jsonArray = new JSONArray(json);

        countryJson = new ArrayList<>();
        continentJson = new ArrayList<>();

        countryJson.clear();
        continentJson.clear();

        final int jsonSize = json.length();
        for (int i = 0; i < jsonSize; i++)
        {
            JSONObject obj = jsonArray.getJSONObject(i);

            countryJson.add(obj.getString("Country_Name"));
            continentJson.add(obj.getString("Continent_Name"));
        }

    }

    private void populateCityList()
    {
        cityList = new ArrayList<>();
        getCityList = new ArrayList<>();

        dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid()).child("visited_cities");
        dbRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                for (DataSnapshot d : dataSnapshot.getChildren())
                {
                    cityList.add(d.getKey());
                }

                getCityList.addAll(cityList);
                int numCi = getCityList.size();
                numCity.setText(String.valueOf(numCi));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        cityListAdapter = new ArrayAdapter<String>(this, R.layout.list_row, getCityList);
        cityListView.setAdapter(cityListAdapter);
    }

    public interface MyCallback
    {
        void onCallBack(List<String> value);
    }
}
