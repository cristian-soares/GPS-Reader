package com.example.gpsreader;

import com.example.android_lib.Region;
import com.example.android_lib.SubRegion;
import com.example.android_lib.RestrictedRegion;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

public class RegionManager extends Thread {
    private final ArrayBlockingQueue<Region> regionQueue;
    private final Semaphore semaphore;
    private boolean running = true;
    private static final int MIN_DISTANCE_METERS = 30;
    private static final int MIN_SUBRESTRICTED_DISTANCE_METERS = 5;
    private boolean notification = false;
    private boolean lastAddedSubRegion = false; // Para alternar entre SubRegion e RestrictedRegion

    public RegionManager() {
        this.regionQueue = new ArrayBlockingQueue<>(100); // Fila de até 100 elementos
        this.semaphore = new Semaphore(1);
    }

    public void addRegion(Region region) {
        try {
            semaphore.acquire();
            boolean isDuplicateRegion = false;
            boolean isSubRestrictedRegion = false;

            for (Region processedRegion : regionQueue) {
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
                regionQueue.offer(region);
            }
            semaphore.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
