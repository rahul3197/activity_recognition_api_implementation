package com.suryajeet945.accelerometerdatasaver;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.MyClass;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    TextView X_value;
    TextView Y_value;
    TextView Z_value;
    TextView currentDataLength;
    EditText fileName;
    Button saveButton;
    Button startButton;
    Button stopButton;

    boolean capturingData =false;
    public List<AccelerationData>Data=new ArrayList<>();

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //MyClass myClass=new MyClass();

        X_value=(TextView)findViewById(R.id.x_value);
        Y_value=(TextView)findViewById(R.id.y_value);
        Z_value=(TextView)findViewById(R.id.z_value);
        currentDataLength=(TextView)findViewById(R.id.countTextView);

        fileName=(EditText)findViewById(R.id.fileName);

        startButton=(Button)findViewById(R.id.stratButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                capturingData =true;
                Data=new ArrayList<AccelerationData>();
            }
        });
        stopButton=(Button)findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                capturingData =false;
            }
        });
        saveButton=(Button)findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateNoteOnSD(MainActivity.this,fileName.getText().toString(),GetStringValueFromList(Data));
            }
        });
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }
    public void generateNoteOnSD(Context context, String sFileName, String sBody) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String GetStringValueFromList(List<AccelerationData>data){
        String s="";
        for (AccelerationData accelerationData:Data){
            s+=accelerationData.x+" "+accelerationData.y+" "+accelerationData.z+"\n";
        }
        return s;
    }
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (capturingData && sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            X_value.setText(Float.toString(sensorEvent.values[0]));
            Y_value.setText(Float.toString(sensorEvent.values[1]));
            Z_value.setText(Float.toString(sensorEvent.values[2]));
            AccelerationData accelerationData = new AccelerationData(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
            if (Data == null) {
                Data = new ArrayList<>();
            } else {
                Data.add(accelerationData);
            }
            currentDataLength.setText(Integer.toString(Data.size()));
            Log.d("Sensor",accelerationData.toString());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
