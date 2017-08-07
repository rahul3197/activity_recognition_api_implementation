package com.suryajeet945.accelerometerdatasaver;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;

    TextView X_value;
    TextView Y_value;
    TextView Z_value;
    TextView currentDataLength;
    EditText fileName;
    Button saveButton;
    Button startButton;
    Button stopButton;
    int frameLength=15;
    int polynomialOrder=4;
    int index=frameLength;
    boolean capturingData =false;
    public List<AccelerationData>Data=new ArrayList<>();
    GraphView graph_x;
    List<Double>normleData=new ArrayList<>(500);

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    SGolay sGolay;
    HandlerClass handlerClass=new HandlerClass();

    private LineGraphSeries<DataPoint> series_x;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }


        graph_x=(GraphView)findViewById(R.id.graph_x);
        //graph_x.getViewport().setScalable(true); // enables horizontal zooming and scrolling
        //graph_x.getViewport().setScalableY(true); // enables vertical zooming and scrolling
        // data
        series_x = new LineGraphSeries<DataPoint>();
        series_x.setTitle("X");
        series_x.setColor(Color.RED);

        graph_x.addSeries(series_x);

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
       // mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        //mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sGolay=new SGolay(polynomialOrder,frameLength);
       // SGolayCalculator();

    }
   /* public void SGolayCalculator (){
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    if(Data.size()>2*frameLength+1){
                        sGolay.GetFiltredData(GetFrameData(index,frameLength,Data));
                        index++;
                        handlerClass.obtainMessage(1).sendToTarget();
                    }
                }
            }
        });
        thread.start();
    }*/
    public RealMatrix GetFrameDataFromAcceData(int index,int frameSize,List<AccelerationData>data){
        double[] frameData=new double[2*frameSize+1];
        for (int i=-frameSize;i<=frameSize && i<data.size();i++){
            frameData[i+frameSize]=data.get(index+i).x;
        }
        RealMatrix frameDataMatrix=new Array2DRowRealMatrix(frameData);
        return  frameDataMatrix.transpose();
    }
    public RealMatrix GetFrameData(int index, int frameSize, List<Double> data){
        double[] frameData=new double[2*frameSize+1];
        for (int i=-frameSize;i<=frameSize && index+i<data.size();i++){
            frameData[i+frameSize]=data.get(index+i);
        }
        RealMatrix frameDataMatrix=new Array2DRowRealMatrix(frameData);
        return  frameDataMatrix.transpose();
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
    class HandlerClass extends Handler {
        HandlerClass() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    series_x.appendData(
                            new DataPoint(index,sGolay.GetFiltredData(GetFrameData(index,frameLength,normleData))),false,300);
                    return;
                case 2:
                    series_x.appendData(
                            new DataPoint(index,((AccelerationData )msg.obj).x),false,100);
                    return;
                default:
                    return;
            }
        }
    }
    protected void onResume() {
        super.onResume();
        //mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
int delay=0;
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (capturingData && sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            delay++;
            if (delay == 50) {
                delay = 0;
            }
            if (delay == 0) {
                float x, y, z;
                x = sensorEvent.values[0];
                y = sensorEvent.values[1];
                z = sensorEvent.values[2];


                normleData.add(Math.sqrt(x * x + y * y + z * z));
                X_value.setText(Float.toString(x));
                Y_value.setText(Float.toString(y));
                Z_value.setText(Float.toString(z));
                AccelerationData accelerationData = new AccelerationData(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
                handlerClass.obtainMessage(2, accelerationData).sendToTarget();
                if (Data == null) {
                    Data = new ArrayList<>();
                } else {
                    Data.add(accelerationData);
                }
                currentDataLength.setText(Integer.toString(Data.size()));
                Log.d("Sensor", accelerationData.toString());
            /*if(normleData.size()>2*frameLength+1) {
                handlerClass.obtainMessage(1).sendToTarget();
            }*/
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
