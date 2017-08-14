package com.suryajeet945.accelerometerdatasaver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;

    TextView X_value,WindowSizeValue;
    TextView Y_value,PolyValue;
    TextView Z_value,GraphLenghtValue;
    TextView currentDataLength;
    EditText fileName;
    Button saveButton;
    Button startButton;
    Button stopButton;
    boolean capturingData =false;
    public List<AccelerationData>Data=new ArrayList<>();
    GraphView
            graphView_normal, graphView_normalf,
            graphView_x,graphView_xf,
            graphView_y,graphView_yf,
            graphView_z,graphView_zf;
    List<Double> normalData =new ArrayList<>(500);
    List<Double> xData =new ArrayList<>(500);
    List<Double> yData =new ArrayList<>(500);
    List<Double> zData =new ArrayList<>(500);

    int index;//=Utility.WindowSize;
    double filteredData=0;
    List<Double>filteredDataList=new ArrayList<>();

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    Toolbar toolbar;
    HandlerClass handlerClass=new HandlerClass();
    SharedPreferences sharedPreferences;//=getSharedPreferences("UtilityData",MODE_PRIVATE);

    private LineGraphSeries<DataPoint>
            series_x,series_xf,
            series_y,series_yf,
            series_z,series_zf,
            series_normal, series_normalf;
    ExecutorService sGolayThreadsX,sGolayThreadsY,sGolayThreadsZ,sGolayThreadsNormal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences=getSharedPreferences("UtilityData",MODE_PRIVATE);

        GetSavedUtilityData();
        InitializeGraphAndSeries();

        toolbar =(Toolbar)findViewById(R.id.myToolBar);
        setSupportActionBar(toolbar);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        this.sGolayThreadsNormal = Executors.newFixedThreadPool(1);
        this.sGolayThreadsX=Executors.newFixedThreadPool(1);
        this.sGolayThreadsY=Executors.newFixedThreadPool(1);
        this.sGolayThreadsZ=Executors.newFixedThreadPool(1);

        X_value=(TextView)findViewById(R.id.x_value);
        Y_value=(TextView)findViewById(R.id.y_value);
        Z_value=(TextView)findViewById(R.id.z_value);

        WindowSizeValue=(TextView)findViewById(R.id.window_value);
        PolyValue=(TextView)findViewById(R.id.poly_value);
        GraphLenghtValue=(TextView)findViewById(R.id.graph_value);

        WindowSizeValue.setText(Integer.toString(Utility.WindowSize));
        PolyValue.setText(Integer.toString(Utility.PolyOrder));
        GraphLenghtValue.setText(Integer.toString(Utility.GraphSize));

        currentDataLength=(TextView)findViewById(R.id.countTextView);

        fileName=(EditText)findViewById(R.id.fileName);

        startButton=(Button)findViewById(R.id.stratButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                capturingData =true;
                Data=new ArrayList<AccelerationData>();
             InitializeGraphAndSeries();
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
                generateNoteOnSD(MainActivity.this,fileName.getText().toString()+".txt",GetStringValueFromList(Data));
                //generateNoteOnSD(MainActivity.this,fileName.getText().toString()+"norm", GetStringValueFromListofDoubles(normalData));
                //generateNoteOnSD(MainActivity.this,fileName.getText().toString()+"normalf",GetStringValueFromListofDoubles(filteredDataList));
            }
        });

        Utility.sGolay=new SGolay(Utility.PolyOrder,Utility.WindowSize);

    }
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
        String s="X XF Y YF Z ZF N NF  %data formate";
        for (AccelerationData accelerationData:Data){
            s+=     accelerationData.x+" "+accelerationData.xf+" "+
                    accelerationData.y+" "+accelerationData.yf+" "+
                    accelerationData.z+" "+accelerationData.zf+" "+
                    accelerationData.normal+" "+accelerationData.normalf +"\n";
        }
        return s;
    }
    public String GetStringValueFromListofDoubles(List<Double>data){
        String s="";
        for (Double accelerationData:data){
            s+=accelerationData+"\n";
        }
        return s;
    }
    class HandlerClass extends Handler {
        HandlerClass() {
        }

        public void handleMessage(Message msg) {
            DataAndFilteredData dataAndFilteredData=(DataAndFilteredData ) msg.obj;
            switch (msg.what) {
                case Utility.X:
                    series_x.appendData(
                            new DataPoint(dataAndFilteredData.index,dataAndFilteredData.data),true,Utility.GraphSize);

                    return;
                case Utility.XF:
                    series_xf.appendData(
                            new DataPoint(dataAndFilteredData.index,dataAndFilteredData.filteredData),true,Utility.GraphSize);

                    return;
                case Utility.Y:
                    series_y.appendData(
                            new DataPoint(dataAndFilteredData.index,dataAndFilteredData.data),true,Utility.GraphSize);

                    return;
                case Utility.YF:
                    series_yf.appendData(
                            new DataPoint(dataAndFilteredData.index,dataAndFilteredData.filteredData),true,Utility.GraphSize);

                    return;
                case Utility.Z:
                    series_z.appendData(
                            new DataPoint(dataAndFilteredData.index,dataAndFilteredData.data),true,Utility.GraphSize);

                    return;
                case Utility.ZF:
                    series_zf.appendData(
                            new DataPoint(dataAndFilteredData.index,dataAndFilteredData.filteredData),true,Utility.GraphSize);

                    return;
                case Utility.Normal:
                    series_normal.appendData(
                            new DataPoint(dataAndFilteredData.index,dataAndFilteredData.data),true,Utility.GraphSize);

                    return;
                case Utility.NormalF:
                    series_normalf.appendData
                            (
                            new DataPoint(dataAndFilteredData.index,dataAndFilteredData.filteredData),true,Utility.GraphSize);

                    return;
                default:
                    return;
            }
        }
    }


    protected void onPause() {
        super.onPause();
      //  mSensorManager.unregisterListener(this);
    }
    int delay=0;
    double normal;
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (capturingData && sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            /*delay++;
            if (delay == 5) {
                delay = 0;
            }*/
            if (delay == 0) {
                float x, y, z;
                x = sensorEvent.values[0];
                y = sensorEvent.values[1];
                z = sensorEvent.values[2];

                normal=Math.sqrt(x * x + y * y + z * z);
                final AccelerationData accelerationData = new AccelerationData(index,x,y,z,normal,0,0,0,0);

                normalData.add(normal);
                xData.add((double)x);
                yData.add((double)y);
                zData.add((double)z);

                X_value.setText(Float.toString(x));
                Y_value.setText(Float.toString(y));
                Z_value.setText(Float.toString(z));


                if (normalData.size()>2*Utility.WindowSize+1) {
                    if(Utility.IsFilterX){
                        final RealMatrix frameData = GetFrameData(index, Utility.WindowSize, xData);
                        final DataAndFilteredData dataAndFilteredData=new DataAndFilteredData(index,accelerationData.x,0);
                        sGolayThreadsX.execute(new Runnable() {
                            @Override
                            public void run() {
                                double filteredData = Utility.sGolay.GetFiltredData(frameData);
                                dataAndFilteredData.filteredData = filteredData;
                                accelerationData.xf=filteredData;
                                filteredDataList.add(filteredData);
                                handlerClass.obtainMessage(Utility.X, dataAndFilteredData).sendToTarget();
                                handlerClass.obtainMessage(Utility.XF, dataAndFilteredData).sendToTarget();
                            }
                        });
                    }
                    if(Utility.IsFilterY){
                        final RealMatrix frameData = GetFrameData(index, Utility.WindowSize, yData);
                        final DataAndFilteredData dataAndFilteredData=new DataAndFilteredData(index,accelerationData.y,0);
                        sGolayThreadsY.execute(new Runnable() {
                            @Override
                            public void run() {
                                double filteredData = Utility.sGolay.GetFiltredData(frameData);
                                dataAndFilteredData.filteredData = filteredData;
                                accelerationData.yf=filteredData;
                                filteredDataList.add(filteredData);
                                handlerClass.obtainMessage(Utility.Y, dataAndFilteredData).sendToTarget();
                                handlerClass.obtainMessage(Utility.YF, dataAndFilteredData).sendToTarget();
                            }
                        });
                    }
                    if(Utility.IsFilterZ){
                        final RealMatrix frameData = GetFrameData(index, Utility.WindowSize, zData);
                        final DataAndFilteredData dataAndFilteredData=new DataAndFilteredData(index,accelerationData.normal,0);
                        sGolayThreadsZ.execute(new Runnable() {
                            @Override
                            public void run() {
                                double filteredData = Utility.sGolay.GetFiltredData(frameData);
                                dataAndFilteredData.filteredData = filteredData;
                                accelerationData.zf=filteredData;
                                filteredDataList.add(filteredData);
                                handlerClass.obtainMessage(Utility.Z, dataAndFilteredData).sendToTarget();
                                handlerClass.obtainMessage(Utility.ZF, dataAndFilteredData).sendToTarget();
                            }
                        });
                    }
                    if(Utility.IsFilterNormal) {
                        final RealMatrix frameData = GetFrameData(index, Utility.WindowSize, normalData);
                        final DataAndFilteredData dataAndFilteredData=new DataAndFilteredData(index,accelerationData.normal,0);
                       sGolayThreadsNormal.execute(new Runnable() {
                            @Override
                            public void run() {
                                double filteredData = Utility.sGolay.GetFiltredData(frameData);
                                dataAndFilteredData.filteredData = filteredData;
                                accelerationData.normalf=filteredData;
                                filteredDataList.add(filteredData);
                                handlerClass.obtainMessage(Utility.Normal, dataAndFilteredData).sendToTarget();
                                handlerClass.obtainMessage(Utility.NormalF, dataAndFilteredData).sendToTarget();
                           }
                        });
                    }
                    if(Utility.IsFilterX||Utility.IsFilterY||Utility.IsFilterZ||Utility.IsFilterNormal) {
                        index++;
                    }

                }else {
                    if(Utility.IsFilterX){
                        final DataAndFilteredData dataAndFilteredData=new DataAndFilteredData(index,accelerationData.x,0);
                        handlerClass.obtainMessage(Utility.X, dataAndFilteredData).sendToTarget();
                    }
                    if(Utility.IsFilterY){
                        final DataAndFilteredData dataAndFilteredData=new DataAndFilteredData(index,accelerationData.y,0);
                        handlerClass.obtainMessage(Utility.Y, dataAndFilteredData).sendToTarget();
                    }
                    if(Utility.IsFilterZ){
                        final DataAndFilteredData dataAndFilteredData=new DataAndFilteredData(index,accelerationData.z,0);
                        handlerClass.obtainMessage(Utility.Z, dataAndFilteredData).sendToTarget();
                    }
                    if(Utility.IsFilterNormal) {
                        final DataAndFilteredData dataAndFilteredData=new DataAndFilteredData(index,accelerationData.normal,0);
                        handlerClass.obtainMessage(Utility.Normal, dataAndFilteredData).sendToTarget();
                    }

                }

                if (Data == null) {
                    Data = new ArrayList<>();
                } else {
                    Data.add(accelerationData);
                }
                currentDataLength.setText(Integer.toString(Data.size()));
                Log.d("Sensor", accelerationData.toString());
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void SaveUtilityDataBoolean(String propertyName,boolean data){
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean(propertyName,data);
        editor.apply();
    }
    public void SaveUtilityDataInt(String propertyName,int data){
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putInt(propertyName,data);
        editor.apply();
    }
    public void GetSavedUtilityData(){
        Utility.IsFilterX= sharedPreferences.getBoolean(Utility.IsFilterXString,false);
        Utility.IsFilterY= sharedPreferences.getBoolean(Utility.IsFilterYString,false);
        Utility.IsFilterZ= sharedPreferences.getBoolean(Utility.IsFilterZString,false);
        Utility.IsFilterNormal= sharedPreferences.getBoolean(Utility.IsFilterNormalString,true);
        Utility.WindowSize=sharedPreferences.getInt(Utility.WindowSizeString,15);
        Utility.PolyOrder=sharedPreferences.getInt(Utility.PolyOrderString,7);
        Utility.GraphSize=sharedPreferences.getInt(Utility.GraphSizeString,200);

        index=Utility.WindowSize;
       // InitializeGraphAndSeries();
    }
    @Override
    public void onResume(){
        super.onResume();
        capturingData=false;
        Utility.sGolay=new SGolay(Utility.PolyOrder,Utility.WindowSize);
        GetSavedUtilityData();
        GraphVisibility();

        WindowSizeValue.setText(Integer.toString(Utility.WindowSize));
        PolyValue.setText(Integer.toString(Utility.PolyOrder));
        GraphLenghtValue.setText(Integer.toString(Utility.GraphSize));



    }
    public void InitializeGraphAndSeries(){
        graphView_x =(GraphView)findViewById(R.id.graph_x);
        graphView_x.getViewport().setScalable(true);
        graphView_y =(GraphView)findViewById(R.id.graph_y);
        graphView_y.getViewport().setScalable(true);
        graphView_z =(GraphView)findViewById(R.id.graph_z);
        graphView_z.getViewport().setScalable(true);
        graphView_normal =(GraphView)findViewById(R.id.graph_normal);
        graphView_normal.getViewport().setScalable(true);

        graphView_xf =(GraphView)findViewById(R.id.graph_xf);
        graphView_xf.getViewport().setScalable(true);
        graphView_yf =(GraphView)findViewById(R.id.graph_yf);
        graphView_yf.getViewport().setScalable(true);
        graphView_zf =(GraphView)findViewById(R.id.graph_zf);
        graphView_zf.getViewport().setScalable(true);
        graphView_normalf =(GraphView)findViewById(R.id.graph_normalf);
        graphView_normalf.getViewport().setScalable(true);


        graphView_x.getViewport().setYAxisBoundsManual(true);
        graphView_x.getViewport().setMinY(-15);
        graphView_x.getViewport().setMaxY(15);
        graphView_y.getViewport().setYAxisBoundsManual(true);
        graphView_y.getViewport().setMinY(-15);
        graphView_y.getViewport().setMaxY(15);
        graphView_z.getViewport().setYAxisBoundsManual(true);
        graphView_z.getViewport().setMinY(-15);
        graphView_z.getViewport().setMaxY(15);
        graphView_normal.getViewport().setYAxisBoundsManual(true);
        graphView_normal.getViewport().setMinY(0);
        graphView_normal.getViewport().setMaxY(30);

        graphView_xf.getViewport().setYAxisBoundsManual(true);
        graphView_xf.getViewport().setMinY(-15);
        graphView_xf.getViewport().setMaxY(15);
        graphView_yf.getViewport().setYAxisBoundsManual(true);
        graphView_yf.getViewport().setMinY(-15);
        graphView_yf.getViewport().setMaxY(15);
        graphView_zf.getViewport().setYAxisBoundsManual(true);
        graphView_zf.getViewport().setMinY(-15);
        graphView_zf.getViewport().setMaxY(15);
        graphView_normalf.getViewport().setYAxisBoundsManual(true);
        graphView_normalf.getViewport().setMinY(0);
        graphView_normalf.getViewport().setMaxY(30);

        // data
        series_x = new LineGraphSeries<DataPoint>();
        series_x.setTitle("x");
        series_x.setColor(Color.RED);
        series_y = new LineGraphSeries<DataPoint>();
        series_y.setTitle("y");
        series_y.setColor(Color.RED);
        series_z = new LineGraphSeries<DataPoint>();
        series_z.setTitle("z");
        series_z.setColor(Color.RED);
        series_normal = new LineGraphSeries<DataPoint>();
        series_normal.setTitle("sqrt(x*x+y*y+z*z)");
        series_normal.setColor(Color.RED);

        series_xf = new LineGraphSeries<DataPoint>();
        series_xf.setTitle("Filtered x");
        series_xf.setColor(Color.GREEN);
        series_yf = new LineGraphSeries<DataPoint>();
        series_yf.setTitle("Filtered y");
        series_yf.setColor(Color.GREEN);
        series_zf = new LineGraphSeries<DataPoint>();
        series_zf.setTitle("Filtered z");
        series_zf.setColor(Color.GREEN);
        series_normalf = new LineGraphSeries<DataPoint>();
        series_normalf.setTitle("Filtered normal");
        series_normalf.setColor(Color.GREEN);

        graphView_x.removeAllSeries();
        graphView_xf.removeAllSeries();
        graphView_y.removeAllSeries();
        graphView_yf.removeAllSeries();
        graphView_z.removeAllSeries();
        graphView_zf.removeAllSeries();
        graphView_normal.removeAllSeries();
        graphView_normalf.removeAllSeries();

        graphView_x.addSeries(series_x);
        graphView_y.addSeries(series_y);
        graphView_z.addSeries(series_z);
        graphView_normal.addSeries(series_normal);
        graphView_xf.addSeries(series_xf);
        graphView_yf.addSeries(series_yf);
        graphView_zf.addSeries(series_zf);
        graphView_normalf.addSeries(series_normalf);

        graphView_x.getLegendRenderer().setVisible(true);
        graphView_x.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graphView_y.getLegendRenderer().setVisible(true);
        graphView_y.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graphView_z.getLegendRenderer().setVisible(true);
        graphView_z.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graphView_normal.getLegendRenderer().setVisible(true);
        graphView_normal.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graphView_xf.getLegendRenderer().setVisible(true);
        graphView_xf.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graphView_yf.getLegendRenderer().setVisible(true);
        graphView_yf.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graphView_zf.getLegendRenderer().setVisible(true);
        graphView_zf.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graphView_normalf.getLegendRenderer().setVisible(true);
        graphView_normalf.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        GraphVisibility();

    }
    public void GraphVisibility(){
        graphView_x.setVisibility(Utility.IsFilterX==true?View.VISIBLE:View.GONE);
        graphView_xf.setVisibility(Utility.IsFilterX==true?View.VISIBLE:View.GONE);
        graphView_y.setVisibility(Utility.IsFilterY==true?View.VISIBLE:View.GONE);
        graphView_yf.setVisibility(Utility.IsFilterY==true?View.VISIBLE:View.GONE);
        graphView_z.setVisibility(Utility.IsFilterZ==true?View.VISIBLE:View.GONE);
        graphView_zf.setVisibility(Utility.IsFilterZ==true?View.VISIBLE:View.GONE);
        graphView_normal.setVisibility(Utility.IsFilterNormal==true?View.VISIBLE:View.GONE);
        graphView_normalf.setVisibility(Utility.IsFilterNormal==true?View.VISIBLE:View.GONE);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.settings) {
            Intent intent=new Intent(MainActivity.this,SettingActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
