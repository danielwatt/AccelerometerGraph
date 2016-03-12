package com.example.daniel.accelerometergraph;

import android.app.Service;
import android.content.Intent;
import android.nfc.Tag;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

public class MyService extends WearableListenerService {

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);

        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataMap dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();
                String path = dataEvent.getDataItem().getUri().getPath();
                if (path.equals("/accelerometer-data")) {
                    float x = dataMap.getFloat("x");
                    float y = dataMap.getFloat("y");
                    float z = dataMap.getFloat("z");
                    long timestamp = dataMap.getLong("timestamp");

                    Log.d("MY SERVICE",String.format("x:%f, y:%f, z:%f time:%d",x,y,z, timestamp));
                }
            }
        }
    }
}
