package com.example.gpsreader;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class GPS extends Thread {
    private final Context context;
    private final FusedLocationProviderClient fusedLocationProviderClient;
    private boolean running = true;
    private final MainActivity mainActivity;
    private double currentLatitude;
    private double currentLongitude;
    private long lastUpdateTime;

    public GPS(Context context, MainActivity mainActivity) {
        this.context = context;
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {
        while (running) {
            getLastLocation(location -> {
                if (location != null) {
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();
                    mainActivity.runOnUiThread(() -> mainActivity.updateMap(currentLatitude, currentLongitude));
                    lastUpdateTime = System.currentTimeMillis();
                }
            });
            try {
                Thread.sleep(5000); // Aguarda 5 segundos antes de tentar obter a localização novamente
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void startGPS() {
        start();
    }

    public void stopGPS() {
        running = false;
    }

    public double getLatitude() {
        return currentLatitude;
    }

    public double getLongitude() {
        return currentLongitude;
    }

    public boolean isRunning() {
        return running;
    }

    public void getLastLocation(OnSuccessListener<Location> onSuccessListener) {
        long startTime = System.currentTimeMillis(); // Início da medição
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(location -> {

            //------------------------------------------
            //Implementação Tarefa 4
            long endTime = System.currentTimeMillis(); // Fim da medição
            long executionTime = endTime - startTime;
            Log.d("GPS", "Tempo de execução da Tarefa 1 (Leitura de Localização): " + executionTime + " ms");
            //-------------------------------------------

            if (location != null) {
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
                onSuccessListener.onSuccess(location);
                lastUpdateTime = System.currentTimeMillis();
            }
        });
    }

    public void requestLocationUpdates(LocationRequest locationRequest, com.google.android.gms.location.LocationListener locationListener) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationListener, Looper.getMainLooper());
        lastUpdateTime = System.currentTimeMillis();
    }
}
