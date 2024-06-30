package com.example.gpsreader;

import android.util.Log;
import android.widget.Toast;

import com.example.android_lib.Cryptography;
import com.example.android_lib.Region;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.List;

public class DataUploader extends Thread {
    private final RegionManager regionManager;
    private final MainActivity mainActivity;

    public DataUploader(RegionManager regionManager, MainActivity mainActivity) {
        this.regionManager = regionManager;
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Regiões");
        List<Region> regionQueue = regionManager.getRegionQueue();
        boolean hasNewRegions = false;
        long startTime = System.currentTimeMillis(); // Início da medição

        for (Region region : regionQueue) {
            if (!region.isLoadedFromFirebase()) {
                hasNewRegions = true;
                Gson gson = new Gson();
                String regionJson = gson.toJson(region);
                String encryptedRegionJson = Cryptography.encrypt(regionJson);
                databaseReference.push().setValue(encryptedRegionJson);
            }
        }

        if (hasNewRegions) {
            mainActivity.runOnUiThread(() -> Toast.makeText(mainActivity, "Novas regiões gravadas no BD", Toast.LENGTH_SHORT).show());
            regionManager.clearQueue(); // Limpa a fila após gravar os dados no Firebase
        } else {
            mainActivity.runOnUiThread(() -> Toast.makeText(mainActivity, "Sem regiões novas para gravar", Toast.LENGTH_SHORT).show());
        }

        //-------------------------------------------------
        //Implementação Tarefa 4
        long endTime = System.currentTimeMillis(); // Fim da medição
        long executionTime = endTime - startTime;
        Log.d("DataUploader", "Tempo de execução da Tarefa 4 (Envio de Dados): " + executionTime + " ms");
        //-------------------------------------------------

        try {
            Thread.sleep(1500); // Espera um pouco antes de parar a thread
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
