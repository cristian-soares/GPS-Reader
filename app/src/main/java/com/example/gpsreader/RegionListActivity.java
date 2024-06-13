package com.example.gpsreader;

import static android.service.controls.ControlsProviderService.TAG;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_lib.Region;

import java.util.ArrayList;

public class RegionListActivity extends AppCompatActivity {

    private ListView regionListView;
    private ArrayList<Region> regions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_region_list);

        regionListView = findViewById(R.id.regionListView);

        if (getIntent() != null && getIntent().hasExtra("regions")) {
            regions = (ArrayList<Region>) getIntent().getSerializableExtra("regions");
            Log.d(TAG, "Regiões recebidas: " + regions.size());
        }

        ArrayList<String> regionDetails = new ArrayList<>();
        for (Region region : regions) {
            String regionType = region.getClass().getSimpleName();
            Log.d(TAG, "Região: " + region.getName() + ", Tipo: " + regionType);

            String regionDetail = "Nome: " + region.getName() + "\n" +
                    "Latitude: " + region.getLatitude() + "\n" +
                    "Longitude: " + region.getLongitude() + "\n" +
                    "Tipo: " + regionType + "\n" +
                    "Usuário: " + region.getUser() + "\n" +
                    "Timestamp: " + region.getTimestamp();
            regionDetails.add(regionDetail);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, regionDetails);
        regionListView.setAdapter(adapter);
    }
}
