package com.example.daniel.accelerometergraph;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements DataApi.DataListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private String TAG = "IMPORTANT!";
    private GoogleApiClient mGoogleApiClient;
    private ArrayList<String> xVals;
    private ArrayList<Entry> yValsX;
    private ArrayList<Entry> yValsY;
    private ArrayList<Entry> yValsZ;
    private ArrayList<Entry> yValsMag;
    private LineDataSet dataSetX;
    private LineDataSet dataSetY;
    private LineDataSet dataSetZ;
    private LineDataSet dataSetMag;
    private LineChart chart;
    private boolean magnitudeFlag;
    private int highFiveCount;
    private float highFiveThreshold = 2.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // Request access only to the Wearable API
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();

        chart = (LineChart) findViewById(R.id.chart);
        xVals = new ArrayList<String>();
        yValsX = new ArrayList<Entry>();
        yValsY = new ArrayList<Entry>();
        yValsZ = new ArrayList<Entry>();
        yValsMag = new ArrayList<Entry>();

        dataSetX = new LineDataSet(yValsX, "X");
        dataSetY = new LineDataSet(yValsY, "Y");
        dataSetZ = new LineDataSet(yValsZ, "Z");
        dataSetMag = new LineDataSet (yValsMag,"Magnitude");

        dataSetX.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSetX.setColor(Color.RED);
        dataSetX.setLineWidth(2f);
        dataSetX.setCircleRadius(0f);
        dataSetX.setFillColor(Color.RED);
        dataSetX.setDrawCircleHole(false);

        dataSetY.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSetY.setColor(Color.GREEN);
        dataSetY.setLineWidth(2f);
        dataSetY.setCircleRadius(0f);
        dataSetY.setFillColor(Color.GREEN);
        dataSetY.setDrawCircleHole(false);

        dataSetZ.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSetZ.setColor(Color.BLUE);
        dataSetZ.setLineWidth(2f);
        dataSetZ.setCircleRadius(0f);
        dataSetZ.setFillColor(Color.BLUE);
        dataSetZ.setDrawCircleHole(false);

        dataSetMag.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSetMag.setColor(Color.BLACK);
        dataSetMag.setLineWidth(4f);
        dataSetMag.setCircleRadius(0f);
        dataSetMag.setFillColor(Color.BLACK);
        dataSetMag.setDrawCircleHole(false);

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(dataSetX);
        dataSets.add(dataSetY);
        dataSets.add(dataSetZ);
        dataSets.add(dataSetMag);

        LineData lineData = new LineData(xVals, dataSets);

        magnitudeFlag = false;
        highFiveCount = 0;
        chart.setData(lineData);
    }

    private void setData(float x, float y, float z, long timestamp) {

        xVals.add(timestamp + "");
        xVals.add("");

        Entry entryX = new Entry(x, yValsX.size());
        yValsX.add(entryX);
        dataSetX.addEntry(entryX);

        Entry entryY = new Entry(y, yValsY.size());
        yValsY.add(entryY);
        dataSetY.addEntry(entryY);

        Entry entryZ = new Entry(z, yValsZ.size());
        yValsZ.add(entryZ);
        dataSetZ.addEntry(entryZ);

        float magnitude = (float) Math.sqrt(x*x+y*y+z*z);
        if (magnitude > highFiveThreshold && magnitudeFlag == false){
            highFiveCount++;
            magnitudeFlag = true;
            Toast toast = Toast.makeText(this, String.format("HIGH FIVE: %d", highFiveCount), Toast.LENGTH_SHORT);
            toast.show();

        }else{
            magnitudeFlag = false;
        }
        Entry entryMag = new Entry(magnitude, yValsMag.size());
        yValsMag.add(entryMag);
        dataSetMag.addEntry(entryMag);

        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(dataSetX);
        dataSets.add(dataSetY);
        dataSets.add(dataSetZ);
        dataSets.add(dataSetMag);



        LineData lineData = new LineData(xVals, dataSets);

        chart.setData(lineData);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected: " + connectionHint);
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended: " + cause);
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG, "onConnectionFailed: " + result);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        for (DataEvent dataEvent : dataEventBuffer) {
            if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                DataMap dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();
                String path = dataEvent.getDataItem().getUri().getPath();
                if (path.equals("/accelerometer-data")) {
                    float x = dataMap.getFloat("x");
                    float y = dataMap.getFloat("y");
                    float z = dataMap.getFloat("z");
                    long timestamp = dataMap.getLong("timestamp");

                    Log.d("Listener", String.format("x:%f, y:%f, z:%f time:%d", x, y, z, timestamp));
                    setData(x, y, z, timestamp);
                    chart.notifyDataSetChanged();
                    chart.invalidate();

                }
            }
        }
    }

    public void pauseButton(View v){
        if(mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
        }else{
            mGoogleApiClient.connect();
        }
    }

    public void resetButton(View v){
        xVals = new ArrayList<String>();
        yValsX = new ArrayList<Entry>();
        yValsY = new ArrayList<Entry>();
        yValsZ = new ArrayList<Entry>();
        yValsMag = new ArrayList<Entry>();
    }
}
