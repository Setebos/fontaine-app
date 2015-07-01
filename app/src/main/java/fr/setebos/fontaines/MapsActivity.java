package fr.setebos.fontaines;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    GPSTracker gps;
    Button btnShowLocation;
    Marker positionMarker = null;
    private ProgressDialog pDialog;
    // URL to get contacts JSON
    private static String url = "https://download.data.grandlyon.com/wfs/grandlyon?SERVICE=WFS&VERSION=2.0.0&outputformat=GEOJSON&maxfeatures=200&request=GetFeature&typename=epo_eau_potable.epobornefont";

    private static final String MARKER_ID = "gid";
    private static final String MARKER_LATITUDE = "latitude";
    private static final String MARKER_LONGITUDE = "longitude";

    // contacts JSONArray
    JSONArray markers = null;

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> markersList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        markersList = new ArrayList<HashMap<String, String>>();
        new GetMarkers().execute();
    }

    @Override
    public void onMapReady(final GoogleMap map) {

        btnShowLocation = (Button) findViewById(R.id.localizationBtn);

        btnShowLocation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                gps = new GPSTracker(MapsActivity.this);
                if(positionMarker != null) {
                    positionMarker.remove();
                }
                if (gps.canGetLocation()) {

                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();

                    LatLng myPosition = new LatLng(latitude, longitude);
                    positionMarker = map.addMarker(new MarkerOptions().position(myPosition).title("Ma position"));
                    map.moveCamera(CameraUpdateFactory.newLatLng(myPosition));

                    // \n is for new line
                    // Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                } else {
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }
            }
        });
    }

    /**
     * Async task class to get json by making HTTP call
     * */
    private class GetMarkers extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MapsActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    markers = jsonObj.getJSONArray("features");

                    // looping through All Contacts
                    for (int i = 0; i < markers.length(); i++) {
                        JSONObject c = markers.getJSONObject(i);

                        String id = c.getString(MARKER_ID);
                        String latitude = c.getString(MARKER_LATITUDE);
                        String longitude = c.getString(MARKER_LONGITUDE);

                        // tmp hashmap for single contact
                        HashMap<String, String> marker = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        marker.put(MARKER_ID, id);
                        marker.put(MARKER_LATITUDE, latitude);
                        marker.put(MARKER_LONGITUDE, longitude);

                        // adding contact to contact list
                        markersList.add(marker);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
        }

    }
}