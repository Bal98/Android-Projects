package com.example.cropdetector;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.spec.ECField;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG="MainActvity";

    private static final String modelPath= "classifierLite.tflite";
    private static final String labelPath= "labelsList.txt";

    ImageView cropImage;
    Button predictBtn;
    Button uploadBtn;
    TextView resultText;
    TextView texloc;
    Bitmap img;
    AssetManager assetManager;
    String name;
    public static String latitudes;
    public static String longitudes;
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        cropImage = findViewById(R.id.cropImage);
        predictBtn = findViewById(R.id.predictBtn);
        uploadBtn = findViewById(R.id.uploadBtn);
        resultText = findViewById(R.id.resultText);
        texloc  = findViewById(R.id.textView3);
        cropImage.setOnClickListener(this);
        predictBtn.setOnClickListener(this);
        uploadBtn.setOnClickListener(this);
        assetManager = getAssets();
        latitudes="1234";
        longitudes="1234";
        permission();
        LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        LocationListener listener = new LocationUtil(this,texloc);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,listener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id)
        {
            case R.id.gallery:
                Intent in = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(in,101);
                break;
            case R.id.camera:
                Intent camin = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(camin,102);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==102 && resultCode == Activity.RESULT_OK)
        {
            img = (Bitmap)data.getExtras().get("data");
            cropImage.setImageBitmap(img);
        }
        if((requestCode==101 || requestCode==103) && resultCode==Activity.RESULT_OK)
        {
            Uri imguri = data.getData();
            try {
                img = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imguri);
                cropImage.setImageBitmap(img);
            }catch(Exception e)
            {
                Toast.makeText(this,"image cannot be loaded",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.cropImage)
        {
            Intent in = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(in,103);
        }
        if(view.getId()==R.id.predictBtn)
        {
            Classifier classifier = new Classifier(this,assetManager,modelPath,labelPath);
            String res = classifier.predict(img);
            name="";
            for(int i=0;i<res.length();i++)
            {
                if(res.charAt(i)=='\n')
                    break;
                name+= res.charAt(i);
            }
            resultText.setText(res);
        }

        if(view.getId()==R.id.uploadBtn)
        {
            Toast.makeText(this, "upload is clicked", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onClick: "+latitudes+ " "+longitudes);
            UploadUtil up = new UploadUtil(this,img,name,latitudes,longitudes);
            up.upload();
        }

    }
    public void permission()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},101);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},102);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},103);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==101 && grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            Toast.makeText(this, "Enjoy the app", Toast.LENGTH_SHORT).show();
        else if(requestCode==102 && grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            Toast.makeText(this,"Enjoy the app",Toast.LENGTH_LONG).show();
        else if(requestCode == 103 && grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            Toast.makeText(this,"enjoy the app",Toast.LENGTH_LONG).show();
        else
            permission();
    }

}