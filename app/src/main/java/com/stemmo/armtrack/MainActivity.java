package com.stemmo.armtrack;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Applicazione per testare la lettura degli accelerometri
 * @author SM
 */
public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private Sensor senGyro;

    private TextView txtAccelX, txtAccelY, txtAccelZ;
    private boolean isRecording = false;
    private Context ctxt;
    private SensorEventListener sensList;
    private Button btnStart, btnStop;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar toolbar = findViewById(R.id.toolbar);
        // setSupportActionBar(toolbar);

        // Bottone START
        btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                senSensorManager.registerListener( sensList, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
            }
        });

        // bottone STOP
        btnStop = findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                senSensorManager.unregisterListener(sensList);
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
            }
        });
        btnStop.setEnabled(false);


        ctxt = getApplicationContext();
        sensList=this;

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();

                if(isRecording) {
                    Toast.makeText( ctxt, "Stopped", Toast.LENGTH_SHORT);
                    senSensorManager.unregisterListener(sensList);
                }
                else {
                    Toast.makeText( ctxt, "Started", Toast.LENGTH_SHORT);
                    senSensorManager.registerListener( sensList, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
                }
            }
        });

        // Campi output dei valori accelerometri
        txtAccelX = findViewById(R.id.txtAccelX);
        txtAccelY = findViewById(R.id.txtAccelY);
        txtAccelZ = findViewById(R.id.txtAccelZ);

        // Attivazione servizi sensori accelerometro e giroscopio
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senGyro = senSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }


    /**
     *
     */
    protected void onPause(){
        super.onPause();
        senSensorManager.unregisterListener(this);
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
