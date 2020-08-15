package com.example.cropdetector;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class LocationUtil implements  LocationListener{

    Context context;
    LocationManager locationManager;
    TextView textloc;
    public LocationUtil(Context context,TextView view)
    {
        this.context = context;
        this.textloc  = view;
    }
    @Override
    public void onLocationChanged(@NonNull Location location) {
        String loc="";
        loc="Latitudes:-"+location.getLatitude()+"  Longitudes:-"+location.getLongitude();
        textloc.setText(loc);
        MainActivity.longitudes=""+location.getLongitude();
        MainActivity.latitudes=""+location.getLatitude();
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
    }


}
