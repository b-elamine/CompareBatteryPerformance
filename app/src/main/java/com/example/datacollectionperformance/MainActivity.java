package com.example.datacollectionperformance;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
 private SensorManager sensorManager;
    private Sensor accelerometer;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private TextView batteryUsageTextView;
    private TextView batteryUsageAfterTextView;
    private TextView accelerometer_data;
    private TextView performanceTextView;
    private Button startButton;
    private Button stopButton;
    private boolean isDataCollectionStarted;
    private int currentFrequency;
    private long startTime;
    private long endTime;
    private long processingTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AccelerometerApp::WakeLock");

        batteryUsageTextView = findViewById(R.id.battery_usage_before_textview);
        batteryUsageAfterTextView = (TextView) findViewById(R.id.battery_usage_after_textview);
        accelerometer_data = (TextView) findViewById(R.id.accelerometer_data);

        performanceTextView = findViewById(R.id.performance_textview);
        startButton = findViewById(R.id.start_button);
        stopButton = findViewById(R.id.stop_button);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDataCollection();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopDataCollection();
            }
        });
    }

    private void startDataCollection() {
        isDataCollectionStarted = true;
        currentFrequency = SensorManager.SENSOR_DELAY_FASTEST; // Change the frequency here

        sensorManager.registerListener(this, accelerometer, currentFrequency);
        wakeLock.acquire();
        startTime = System.currentTimeMillis();
        batteryUsageAfterTextView.setText("Battery Usage After: ");
        batteryUsageTextView.setText("Battery Usage: " + getBatteryUsage() + " %");

    }

    private void stopDataCollection() {
        if (isDataCollectionStarted) {
            sensorManager.unregisterListener(this);
            wakeLock.release();
            endTime = System.currentTimeMillis();
            processingTime = endTime - startTime;

            // Log or display the battery usage and performance metrics
            batteryUsageAfterTextView.setText("Battery Usage After: " + getBatteryUsage() + " %");
            performanceTextView.setText("Processing Time: " + processingTime + " ms");

            isDataCollectionStarted = false;
        }
    }

    private String getBatteryUsage() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);

        // Get battery level
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        //battery level
        float batteryPct = (level / (float) scale) * 100;
        // Get temperature
        int temperature = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);

        // Create a formatted string with the battery information
        String batteryInfo = "Battery Level: " + batteryPct + "%\n"
                + "Temperature: " + temperature + " °C\n";

        return batteryInfo;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Process accelerometer data here
        accelerometer_data.setText(Float.toString((event.values[0]+event.values[1]+event.values[2])/3));

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Handle accuracy changes if needed
    }
}
