package com.example.premisha.mapdirection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //private static final Object ContextCompat = ;
    /* private static final Object ActivityCompat = ; */
    private GoogleMap mMap;
    private static final int LOCATION_REQUEST;

    static {
        LOCATION_REQUEST = 500;
    }
    ArrayList<LatLng> listPoints = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        Activity thisActivity = null;
        if (ContextCompat.checkSelfPermission( thisActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ContextCompat ) {
        PackageManager.PERMISSION_GRANTED && ContextCompat));
            ContextCompat.RequestPermissions(thisActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST);
            return;
    }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                //reset marker when already 2
                if (listPoints.size()==2){
                    listPoints.clear();
                    mMap.clear();
                }
                //save first point select
                listPoints.add(latLng);
                //create marker
                MarkerOptions.position(latLng);

                if(listPoints.size())== 1)
            //add first marker to the map
                    MarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

                else
            //add second marker to the map
                    MarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                mMap.addMarker(MarkerOptions);
        //TODO:request get direction code below
               if(listPoints.size()==2)
                   //create the url to get the request from the 1st marker to 2nd marker
                   String url= getRequestUrl (listPoints.get(0),listPoints.get(1));
               TaskRequestDirectsions taskRequestDirectsions=new TaskRequestDirectsions();
               taskRequestDirectsions.execute(url);

               }
            }
        }
        );
    }
    private String getRequestUrl (LatLng origin, LatLng dest) {
        //value of origin
        String str_org = "origin" + origin.latitude + "," + origin.longitude;
        //value of destination
        String str_des = "destination" + dest.latitude + "," + dest.longitude;
        //set value enable to sensor
        String sensor = "sensor=false";
        //mode for find direction
        String mode = "mode=driving";
        //build the full param
        String param = str_org + "&" + str_des + "&" + sensor + "&" + mode;
        //output format
        String output = "json";
        //create url to request
        String url = "https://maps.googleapis.com/maps/directions/" + output + "?" + param;
        return url;
    }
    private String requestDirection (String reqUrl){
    String responseString="";
        InputStream inputStream=null;
        HttpURLConnection httpURLConnection=null;
        try {
            URL url = new URL(reqUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            //get the response result
            inputStream= httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader= new InputStreamReader(inputStream);
            BufferedReader bufferedReader=new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer=new StringBuffer();
            String line="";
            while((line=bufferedReader.readLine()) != null){
                stringBuffer.append(line);
            }

            responseString =stringBuffer.toString();
            bufferedReader.close();
            inputStream.close();
            }
            catch (Exception e){
            e.printStackTrace();
            }
            finally {
            if (inputStream!=null){
                inputStream.close();
            }
        httpURLConnection.disconnect();
        }
        return responseString;
    }

@SuppressLint("MissingPermission")
@Override
public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    switch (requestCode) {
        case LOCATION_REQUEST:

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
            break;
    }
    }
    public class TaskRequestDirectsions extends AsyncTask<String,Void,String>{

    @Override
        protected String doInBackground(String... String){
        String responseString ="";
        try {
            responseString = requestDirection(String[0]);
            }
            catch(IOException e){
            e.printStackTrace();
            }
            return  responseString;
    }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //parse json here
            TaskParser taskParser=new TaskParser();
            taskParser.execute(s);
        }
    }
    public class TaskParser extends AsyncTask<String,Void,List<List<HashMap<String,String>>>> {


        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject=null;
            List<List<HashMap<String,String>>> routes =null;

            try {
                jsonObject = new JSONObject(strings[0]);
                DataParser dataParser = new DataParser();
                routes = ((DataParser) dataParser).parse(jsonObject);
            }
            catch (JSONException e){
                e.printStackTrace();
            }

            return routes;
        }
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            //get results route and display into the map
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                GoogleMap mMap;
                mMap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
    }
}








