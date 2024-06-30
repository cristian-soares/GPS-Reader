package com.example.gpsreader;

import android.util.Log;

import com.example.android_lib.Region;
import com.example.android_lib.SubRegion;
import com.example.android_lib.RestrictedRegion;
import com.example.android_lib.Cryptography;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

public class RegionManager extends Thread {
    private final ArrayBlockingQueue<Region> regionQueue;
    private final List<Region> processedRegions; // Lista de regiões processadas (inclui carregadas do Firebase)
    private final Semaphore semaphore;
    private boolean running = true;
    private static final int MIN_DISTANCE_METERS = 30;
    private static final int MIN_SUBRESTRICTED_DISTANCE_METERS = 5;
    private boolean notification = false;
    private boolean lastAddedSubRegion = false; // Para alternar entre SubRegion e RestrictedRegion

    public RegionManager() {
        this.regionQueue = new ArrayBlockingQueue<>(100); // Fila de até 100 elementos
        this.processedRegions = new CopyOnWriteArrayList<>(); // Lista de regiões processadas
        this.semaphore = new Semaphore(1);
    }

    public void addRegion(Region region) {
        long startTime = System.currentTimeMillis(); // Início da medição
        try {
            semaphore.acquire();
            boolean isDuplicateRegion = false;
            boolean isSubRestrictedRegion = false;

            // Verifica se a região é duplicada ou está muito próxima de uma região processada
            for (Region processedRegion : processedRegions) {
                float distance = processedRegion.calculateDistance(region.getLatitude(), region.getLongitude());
                if (distance < MIN_DISTANCE_METERS) {
                    if (distance > MIN_SUBRESTRICTED_DISTANCE_METERS) {
                        isSubRestrictedRegion = true;
                    } else {
                        isDuplicateRegion = true;
                        break;
                    }
                }
            }

            if (!isDuplicateRegion) {
                if (isSubRestrictedRegion) {
                    if (lastAddedSubRegion) {
                        region = new RestrictedRegion(region.getName(), region.getLatitude(), region.getLongitude(), region.getUser(), region);
                        region.setType("Região Restrita");
                    } else {
                        region = new SubRegion(region.getName(), region.getLatitude(), region.getLongitude(), region.getUser(), region);
                        region.setType("Sub Região");
                    }
                    lastAddedSubRegion = !lastAddedSubRegion;
                } else {
                    region.setType("Região");
                }

                if (!region.isLoadedFromFirebase()) {
                    regionQueue.offer(region); // Adiciona a região na fila
                }
                processedRegions.add(region); // Adiciona a região à lista de regiões processadas
                notification = true;
                System.out.println("Região adicionada: " + region.getClass().getSimpleName());
            } else {
                notification = false;
                System.out.println("Região duplicada ou muito próxima, não adicionada.");
            }
            semaphore.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //-----------------------------------------------------
        //Implementação Tarefa 4
        long endTime = System.currentTimeMillis(); // Fim da medição
        long executionTime = endTime - startTime;
        Log.d("RegionManager", "Tempo de execução da Tarefa 2 (Inserção de Regiões): " + executionTime + " ms");
        //------------------------------------------------------
    }

    public boolean getNotification() {
        return notification;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Region> getRegionQueue() {
        return new CopyOnWriteArrayList<>(regionQueue);
    }

    public void stopRegionManager() {
        running = false;
    }

    public void clearQueue() {
        try {
            semaphore.acquire();
            regionQueue.clear();
            semaphore.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void loadRegionsFromFirebase(List<Region> regions) {
        try {
            semaphore.acquire();
            for (Region region : regions) {
                region.setLoadedFromFirebase(true);
                processedRegions.add(region); // Adiciona as regiões carregadas do Firebase à lista de regiões processadas
            }
            semaphore.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
