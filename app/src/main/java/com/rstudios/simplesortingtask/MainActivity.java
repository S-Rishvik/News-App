package com.rstudios.simplesortingtask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.facebook.ads.AdError;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.NativeAdsManager;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import static com.rstudios.simplesortingtask.Contractor.placement_id;

public class MainActivity extends AppCompatActivity implements NativeAdsManager.Listener{
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 100;
    @BindView(R.id.main_recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.main_progressBar)
    ProgressBar progressBar;
    @BindView(R.id.main_error)
    TextView errorText;
    private RequestQueue requestQueue;
    private NativeListRecyclerAdapter listRecyclerAdapter;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Geocoder geocoder;
    private NativeAdsManager mNativeAdsManager;
    private ArrayList<ListItem> arrayList;
    private FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        AudienceNetworkAds.initialize(this);
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.defaults);
        mFirebaseRemoteConfig.fetch(0)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
//                            Toast.makeText(MainActivity.this, "Fetch Succeeded",
//                                    Toast.LENGTH_SHORT).show();
                            // After config data is successfully fetched, it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.fetchAndActivate();
                        } else {
                            Toast.makeText(MainActivity.this, "Fetch Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        locationRequest = new LocationRequest().setInterval(1000).setFastestInterval(1000).setNumUpdates(1).setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder=new Geocoder(getApplicationContext());
        getLocation();
    }

    void parseJSON(JSONObject jsonObject) throws JSONException {
        JSONArray jsonArray = jsonObject.getJSONArray("articles");
        arrayList = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            ListItem listItem = new ListItem(object);
            arrayList.add(listItem);
        }
        if(!mFirebaseRemoteConfig.getBoolean("show_ads")){
            recyclerView.setAdapter(new ListRecyclerAdapter(getApplicationContext(),arrayList));
            progressBar.setVisibility(View.GONE);
            return;
        }
        mNativeAdsManager = new NativeAdsManager(this, placement_id, 5);
        mNativeAdsManager.loadAds();
        mNativeAdsManager.setListener(this);
    }
    void getJSON(String url) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            parseJSON(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        progressBar.setVisibility(View.GONE);
                        errorText.setVisibility(View.VISIBLE);
                    }
                });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(50000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(jsonObjectRequest);
    }
    void getLocation()  {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSIONS_REQUEST_LOCATION);
            return;
        }
        else if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            enableGPS();
            return;
        }
        mFusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location=locationResult.getLocations().get(0);
                Log.i("only once","SSR");
                try {
                    Address address =  geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1).get(0);
                    getJSON("https://newsapi.org/v2/top-headlines?country="+address.getCountryCode()+"&apiKey=06ea1d1712274203b4985c97762f4456");
                } catch (IOException e) {
                    e.printStackTrace();
                    getJSON("https://newsapi.org/v2/top-headlines?q=world&apiKey=06ea1d1712274203b4985c97762f4456");
                    Toast.makeText(getApplicationContext(),"Failed to retrieve user location",Toast.LENGTH_SHORT).show();
                }
            }
        }, Looper.getMainLooper());
    }
    private void enableGPS(){
        LocationSettingsRequest.Builder settingsBuilder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        settingsBuilder.setAlwaysShow(true);
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this).checkLocationSettings(settingsBuilder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                } catch (ApiException ex) {
                    switch (ex.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) ex;
                                resolvableApiException.startResolutionForResult(MainActivity.this, 777);
                            } catch (IntentSender.SendIntentException e) { }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            Toast.makeText(getApplicationContext(),"Settings Change Unavailable",Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        });
    }

    @Override
    public void onAdsLoaded() {
        Log.i("MainActivity","Ad loaded");
        recyclerView.setAdapter(new NativeListRecyclerAdapter(getApplicationContext(), arrayList,this,mNativeAdsManager));
        progressBar.setVisibility(View.GONE);
    }
    @Override
    public void onAdError(AdError adError) {
        Log.i("MainActivity","Load Ad error");
        progressBar.setVisibility(View.GONE);
        errorText.setVisibility(View.VISIBLE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        getLocation();
                    }
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "To find your location automatically, turn on location services", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setActionTextColor(Color.parseColor("#FFAB00"))
                            .setAction("Enable", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                                        getLocation();
                                    } else {
                                        Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);
                                    }
                                }
                            });
                    snackbar.show();
                }
                break;
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 777:
                if (resultCode != RESULT_OK) {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "To find your location automatically, turn on location services", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setActionTextColor(Color.parseColor("#FFAB00"))
                            .setAction("Enable", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    getLocation();
                                }
                            });
                    snackbar.show();
                }
                else
                    getLocation();
                break;
        }
    }
}