package com.stemmo.armtrack;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;


/**
 * Applicazione per testare la lettura degli accelerometri
 * @author SM
 */
public class MainActivity extends Activity implements SensorEventListener {

    String ARMTRACK_TAG = "ArmTrack";

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private Sensor senGyro;

    private TextView txtAccelX, txtAccelY, txtAccelZ;
    private TextView txtEsito;
    private boolean isRecording = false;
    private Context ctxt;
    private SensorEventListener sensList;
    private Button btnStart, btnStop, btnSave;

    private long startTime;
    private long nReadNumber;

    /** Directory to store user credentials. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".store/drive_sample");
    // errore private static final java.io.File DATA_STORE_DIR = new java.io.File(Context.getFilesDir());
    /**
     * Be sure to specify the name of your application. If the application name is {@code null} or
     * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
     */
    private static final String APPLICATION_NAME = "";

    private static final String UPLOAD_FILE_PATH = "Enter File Path";
    private static final String DIR_FOR_DOWNLOADS = "Enter Download Directory";
    private static final java.io.File UPLOAD_FILE = new java.io.File(UPLOAD_FILE_PATH);



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

                startTime = System.currentTimeMillis();
                nReadNumber = 0;
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

        // bottone SAVE
        btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    httpTransport =  new com.google.api.client.http.javanet.NetHttpTransport(); // era GoogleNetHttpTransport.newTrustedTransport();
                    dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
                    // authorization
                    Credential credential = authorize();
                    // set up the global Drive instance
                    drive = new Drive.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();

                    // View.header1("Starting Resumable Media Upload");
                    File uploadedFile = uploadFile(false);
                }
                catch (Exception exp){
                    Log.e( ARMTRACK_TAG, "Errore: "+exp.getMessage());
                }
            }
        }) ;

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

        txtEsito = findViewById(R.id.txtEsito);

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
        txtAccelX.setText(String.format("%.3f", vals[0]));
        txtAccelY.setText(String.format("%.3f", vals[1]));
        txtAccelZ.setText(String.format("%.3f", vals[2]));

        if(++nReadNumber == 100){
            long diff = System.currentTimeMillis() - startTime;
            txtEsito.setText(String.format("Tempo: %d mills", diff));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.i( ARMTRACK_TAG, String.format("AccuracyChanged = %d", i));
    }



    /**
     * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single
     * globally shared instance across your application.
     */
    private static FileDataStoreFactory dataStoreFactory;

    /** Global instance of the HTTP transport. */
    private static HttpTransport httpTransport;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /** Global Drive API client. */
    private static Drive drive;


    /** Authorizes the installed application to access user's protected data. */
    private static Credential authorize() throws Exception {
        // load client secrets
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new InputStreamReader(MainActivity.class.getResourceAsStream("/client_secrets.json")));
        if (clientSecrets.getDetails().getClientId().startsWith("Enter")
                || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
            System.out.println(
                    "Enter Client ID and Secret from https://code.google.com/apis/console/?api=drive "
                            + "into drive-cmdline-sample/src/main/resources/client_secrets.json");
            System.exit(1);
        }
        // set up authorization code flow
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets,
                Collections.singleton(DriveScopes.DRIVE_FILE)).setDataStoreFactory(dataStoreFactory)
                .build();
        // authorize
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }



    /** Uploads a file using either resumable or direct media upload. */
    private static File uploadFile(boolean useDirectUpload) throws IOException {
        File fileMetadata = new File();
        //SM fileMetadata.setTitle(UPLOAD_FILE.getName());
        fileMetadata.setName(UPLOAD_FILE.getName());

        FileContent mediaContent = new FileContent("image/jpeg", UPLOAD_FILE);

        //SM Drive.Files.Insert insert = drive.files().insert(fileMetadata, mediaContent);
        Drive.Files.Create insert = drive.files().create(fileMetadata, mediaContent);

        MediaHttpUploader uploader = insert.getMediaHttpUploader();
        uploader.setDirectUploadEnabled(useDirectUpload);
        // uploader.setProgressListener(new FileUploadProgressListener());
        return insert.execute();
    }
}
