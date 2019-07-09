package com.myapplicationdev.android.p09_gettingmylocations;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class MainActivity extends AppCompatActivity {
    String folderLocation;
    FusedLocationProviderClient client;
    TextView tvData;
    Button btnStart, btnStop, btnCheck;
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvData = findViewById(R.id.tvData);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnCheck = findViewById(R.id.btnCheck);

        FragmentManager fm = getSupportFragmentManager();
        final SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);

        folderLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/P09";
        File folder = new File(folderLocation);
        if (folder.exists() == false) {
            boolean result = folder.mkdir();
            if (result == true) {
                Log.d("File Read/Write", "Folder created");
            } else {
                Log.d("File Read/Write", "Folder creation failed");
            }
        }
        client = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        if (checkPermission()) {
            Task<Location> task = client.getLastLocation();
            task.addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(final Location location) {

                    if (location != null) {
                        tvData.setText("Last known location when this Activity started:\nLatitude: " + location.getLatitude() +
                                "\nLongitude: " + location.getLongitude());
                        mapFragment.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(GoogleMap googleMap) {
                                map = googleMap;
                                LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 11));

                                Marker cp = map.addMarker(new
                                        MarkerOptions()
                                        .position(pos)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                                UiSettings ui = map.getUiSettings();
                                ui.setZoomControlsEnabled(true);
                                int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
                                if (permissionCheck == PermissionChecker.PERMISSION_GRANTED){
                                    map.setMyLocationEnabled(true);
                                }else{
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                                }
                            }
                        });
                    } else {
                        Toast.makeText(MainActivity.this, "No Last Known Location found", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            String msg = "Permission not granted to retrieve location info";
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, DetectorService.class);
                startService(i);
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, DetectorService.class);
                stopService(i);

            }
        });

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File targetFile = new File(folderLocation, "location.txt");
                if (targetFile.exists() == true) {
                    String data = "";
                    try {
                        FileReader reader = new FileReader(targetFile);
                        BufferedReader br = new BufferedReader(reader);

                        String line = br.readLine();
                        while (line != null) {
                            data += line + "\n";
                            line = br.readLine();
                        }

                        br.close();
                        reader.close();
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Failed to read!", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                    Log.d("Content", data);
                    Toast.makeText(MainActivity.this, data, Toast.LENGTH_LONG).show();
                } else{
                    Toast.makeText(MainActivity.this, "File does not exist!", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private boolean checkPermission() {
        int permissionCheck_Coarse = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionCheck_Fine = ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCheck_Read = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionCheck_Write = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);


        if (permissionCheck_Coarse == PermissionChecker.PERMISSION_GRANTED
                || permissionCheck_Fine == PermissionChecker.PERMISSION_GRANTED
                || permissionCheck_Read == PermissionChecker.PERMISSION_GRANTED
                || permissionCheck_Write == PermissionChecker.PERMISSION_GRANTED
        ) {
            return true;
        } else {
            return false;
        }
    }

}
