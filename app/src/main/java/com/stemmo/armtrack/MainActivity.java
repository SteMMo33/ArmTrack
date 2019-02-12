package com.stemmo.armtrack;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private Sensor senGyro;
    private TextView txtAccelX, txtAccelY, txtAccelZ;
    private boolean isRecording = false;
    private Context ctxt;
    private View thisView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar toolbar = findViewById(R.id.toolbar);
        // setSupportActionBar(toolbar);

        ctxt = getApplicationContext();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();

                if(isRecording) {
                    Toast.makeText( ctxt, "Stopped", Toast.LENGTH_SHORT);
                    senSensorManager.unregisterListener(senAccelerometer);
                }
                else {
                    Toast.makeText( ctxt, "Started", Toast.LENGTH_SHORT);
                    senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
                }
            }
        });

        txtAccelX = findViewById(R.id.txtAccelX);
        txtAccelY = findViewById(R.id.txtAccelY);
        txtAccelZ = findViewById(R.id.txtAccelZ);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senGyro = senSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float[] vals = sensorEvent.values;
        txtAccelX.setText(Float.toString(vals[0]));
        txtAccelY.setText(Float.toString(vals[1]));
        txtAccelZ.setText(Float.toString(vals[2]));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
