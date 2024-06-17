package com.example.gpsreader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android_lib.Region;
import com.example.android_lib.SubRegion;
import com.example.android_lib.RestrictedRegion;
import com.example.android_lib.Cryptography;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int FINE_PERMISSION_CODE = 1;
    private static final String TAG = "MainActivity";
    private GoogleMap myMap;
    private GPS gps;
    private TextView latitudeTextView;
    private TextView longitudeTextView;
    private Button addRegionButton;
    private Button showRegionsButton;
    private RegionManager regionManager;
    private DatabaseReference reference;
    private List<Region> loadedRegions;
    private boolean zoom = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gps = new GPS(this, this);
        latitudeTextView = findViewById(R.id.latitudeTextView);
        longitudeTextView = findViewById(R.id.longitudeTextView);
        addRegionButton = findViewById(R.id.addRegionButton);
        showRegionsButton = findViewById(R.id.showRegionsButton);
        Button saveDataButton = findViewById(R.id.saveDataButton);

        requestLocationUpdates();
        gps.startGPS();

        regionManager = new RegionManager();
        regionManager.start();

        // Carrega as regiões do Firebase ao iniciar o aplicativo
        loadRegionsFromFirebase();

        addRegionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    LatLng currentLocation = new LatLng(gps.getLatitude(), gps.getLongitude());
                    Region region = new Region("Localização", currentLocation.latitude, currentLocation.longitude, 1);
                    regionManager.addRegion(region);
                    if (regionManager.getNotification()) {
                        String regionTypeMessage = "Região adicionada à fila!";
                        if (region instanceof SubRegion) {
                            region.setType("Sub Região");
                            regionTypeMessage = "Sub Região adicionada à fila!";
                        } else if (region instanceof RestrictedRegion) {
                            region.setType("Região Restrita");
                            regionTypeMessage = "Região Restrita adicionada à fila!";
                        } else {
                            region.setType("Região");
                        }
                        Toast.makeText(MainActivity.this, regionTypeMessage, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Região não adicionada à fila, verifique a proximidade das regiões!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao adicionar região: ", e);
                    Toast.makeText(MainActivity.this, "Erro ao adicionar região", Toast.LENGTH_SHORT).show();
                }
            }
        });

        saveDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        showRegionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadRegionsFromFirebaseAndShow();
            }
        });

        reference = FirebaseDatabase.getInstance().getReference();
        loadedRegions = new ArrayList<>();
        getLastLocation();
    }

    private void getLastLocation() {
        gps.getLastLocation(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    updateMap(location.getLatitude(), location.getLongitude());
                } else {
                    requestLocationUpdates();
                }
            }
        });
    }

    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);

        gps.requestLocationUpdates(locationRequest, location -> {
            if (location != null) {
                updateMap(location.getLatitude(), location.getLongitude());
            }
        });
    }

    public void updateMap(double latitude, double longitude) {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                myMap = googleMap;
                LatLng currentLocation = new LatLng(latitude, longitude);
                myMap.clear();
                myMap.addMarker(new MarkerOptions().position(currentLocation).title("Sua Localização"));
                if (!zoom) {
                    myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 20));
                    zoom = true;
                } else {
                    myMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                }
                latitudeTextView.setText("Latitude: " + latitude);
                longitudeTextView.setText("Longitude: " + longitude);
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Habilite a permissão de localização no seu dispositivo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;
        gps.getLastLocation(location -> {
            if (location != null) {
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                myMap.clear();
                myMap.addMarker(new MarkerOptions().position(currentLocation).title("Sua Localização"));
                if (!zoom) {
                    myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 20));
                    zoom = true;
                } else {
                    myMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                }
            }
        });
    }

    private void saveData() {
        DataUploader dataUploader = new DataUploader(regionManager, this);
        dataUploader.start();
    }

    private void showRegions() {
        List<Region> regions = new ArrayList<>(loadedRegions);
        regions.addAll(regionManager.getRegionQueue());
        Intent intent = new Intent(MainActivity.this, RegionListActivity.class);
        intent.putExtra("regions", new ArrayList<>(regions));
        startActivity(intent);
    }

    private void loadRegionsFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Regiões");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Gson gson = new Gson();
                List<Region> regions = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String encryptedRegionJson = snapshot.getValue(String.class);
                    if (encryptedRegionJson != null) {
                        try {
                            String regionJson = Cryptography.decrypt(encryptedRegionJson);
                            Region region = gson.fromJson(regionJson, Region.class);

                            // Verifica o tipo de região usando instanceof
                            if (region instanceof SubRegion) {
                                region = gson.fromJson(regionJson, SubRegion.class);
                            } else if (region instanceof RestrictedRegion) {
                                region = gson.fromJson(regionJson, RestrictedRegion.class);
                            }

                            region.setLoadedFromFirebase(true); // Marca a região como carregada do Firebase
                            regions.add(region);
                            Log.d(TAG, "Região carregada: " + region.getType() + ", tipo: " + region.getClass().getSimpleName());
                        } catch (Exception e) {
                            Log.e(TAG, "Erro ao carregar região do Firebase: ", e);
                        }
                    }
                }
                loadedRegions.clear(); // Limpa as regiões carregadas anteriormente
                loadedRegions.addAll(regions); // Adiciona as novas regiões carregadas
                regionManager.loadRegionsFromFirebase(regions); // Adiciona as regiões carregadas na manager
                Toast.makeText(MainActivity.this, "Regiões carregadas do Firebase.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Erro ao carregar regiões do Firebase: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadRegionsFromFirebaseAndShow() {
        loadRegionsFromFirebase();
        showRegions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gps.stopGPS();
        regionManager.stopRegionManager();
    }
}
