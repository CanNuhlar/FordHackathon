package com.example.gurcuff.hackathon;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Gurcuff on 5.11.2017.
 */

@SuppressLint("ValidFragment")
public class FirstFragment extends Fragment implements LocationListener {


    private double lat, lon;
    private LocationManager mLocationManager;
    private String weatherAPIUrl;
    private double temp;
    private String curWeather;
    private String temparature;
    private TextView welcomeText;
    private TextView weatherStatus;
    private String displayName;

    @SuppressLint("ValidFragment")
    public FirstFragment(String displayName) {
        this.displayName = displayName;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments


        return inflater.inflate(R.layout.first_fragment, container, false);
    }



    private Location getLastKnownLocation() {
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            @SuppressLint("MissingPermission") Location l = mLocationManager.getLastKnownLocation(provider);


            if (l == null) {
                continue;
            }
            if (bestLocation == null
                    || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        if (bestLocation == null) {
            return null;
        }
        return bestLocation;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //View inf = view.inflate(R.layout.first_fragment, container, false);


        welcomeText = (TextView) view.findViewById(R.id.welcomeText);




        weatherStatus = (TextView) view.findViewById(R.id.weatherStatus);



        welcomeText.setText("Welcome again,\n" + displayName);

        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location lastLoc = getLastKnownLocation();
        //lon = lastLoc.getLongitude();
        //lat = lastLoc.getLatitude();
        lat = 40.8762;
        lon = 29.3889;



        weatherAPIUrl = "http://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=312d37ed668d9b3b00297afb57a82957";
        StringRequest request = new StringRequest(weatherAPIUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String string) {
                parseJsonData(string);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getActivity().getApplicationContext(), "Some error occurred!!", Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(getContext());
        rQueue.add(request);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Home");
    }
    void parseJsonData(String jsonString) {
        try {
            JSONObject object = new JSONObject(jsonString);
            JSONObject weatherArray = object.getJSONArray("weather").getJSONObject(0);
            curWeather = weatherArray.get("description").toString();
            System.out.println(weatherArray.get("description").toString());
            weatherStatus.setText(curWeather);
            //todo fix json parse
            temparature = "13.0 °";

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}