package com.example.stepcounter;

import static com.google.android.material.internal.ContextUtils.getActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity implements SensorEventListener, StepListener {
    private TextView TvSteps;
    private Button BtnStart, BtnStop;
    private StepDetector StepDetector;
    private SensorManager sensorManager;
    private Sensor accelerator;
    private int numSteps;
    private TextView distance;
    public TextView TvCal;
    private boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerator = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        StepDetector = new StepDetector();
        StepDetector.registerListener(this);

        TvSteps = findViewById(R.id.tv_steps);
        BtnStart = findViewById(R.id.btn_start);
        BtnStop = findViewById(R.id.btn_stop);
        distance = findViewById(R.id.TV_DISTANCE);
        TvCal=findViewById(R.id.TV_CALORIES);

        BtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                isRunning = true;
                numSteps = 0;
                sensorManager.registerListener(MainActivity.this, accelerator, SensorManager.SENSOR_DELAY_FASTEST);
                Toast.makeText(MainActivity.this, "Steps detection has started", Toast.LENGTH_SHORT).show();
            }
        });

        BtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                isRunning = false;
                sensorManager.unregisterListener(MainActivity.this);
                Toast.makeText(MainActivity.this, "Step counter has been deactivated", Toast.LENGTH_SHORT).show();
                numSteps = 0;
                TvSteps.setText("0");
                distance.setText("0");
                TvCal.setText("0");
            }
        });

        // Schedule the first distance update after 5 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateDistance();
            }
        }, 5000);
    }

    private void updateDistance() {
        if (numSteps >= 5) {
            double distanceInMeters = kiloMeters();
            String distanceText;
            if (distanceInMeters >= 1000) {
                double distanceInKilometers = distanceInMeters / 1000;
                distanceText = String.format("%.2f km", distanceInKilometers);
            } else {
                distanceText = String.format("%.2f m", distanceInMeters);
            }
            distance.setText(distanceText);
        }

        // Schedule the next update after 5 seconds
        if (isRunning) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateDistance();
                }
            }, 5000);
        }
    }

    public double kiloMeters() {
        double feet = numSteps * 2.5;
        double dist = feet / 3.281;
        return dist;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            StepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void step(long timeNs) {
        numSteps++;
        TvSteps.setText(String.valueOf(numSteps));

        if (numSteps % 5 == 0) {
            updateDistance();
            updateCalories();
        }
    }

    private void updateCalories() {
        double calories = calculateCalories(numSteps);
        String caloriesText = String.format("%.2f cal", calories);
        TvCal.setText(caloriesText);
    }

    private double calculateCalories(int numSteps) {
        double caloriesPerStep = 0.04; // Adjust this value according to your needs
        return numSteps * caloriesPerStep;
    }

}