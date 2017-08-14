package com.suryajeet945.accelerometerdatasaver;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;

public class SettingActivity extends AppCompatActivity {
    ToggleButton toggleButtonX,toggleButtonY,toggleButtonZ,toggleButtonNormal;
    EditText editTextWindowSize,editTextPolyOrder, editTextGraphSize;
    Button saveButton;
    Toolbar toolbar;
    SharedPreferences sharedPreferences;//=getSharedPreferences("UtilityData",MODE_PRIVATE);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        toolbar =(Toolbar)findViewById(R.id.myToolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences=getSharedPreferences("UtilityData",MODE_PRIVATE);

        toggleButtonX=(ToggleButton)findViewById(R.id.x_toggle);
        toggleButtonY=(ToggleButton)findViewById(R.id.y_toggle);
        toggleButtonZ=(ToggleButton)findViewById(R.id.z_toggle);
        toggleButtonNormal=(ToggleButton)findViewById(R.id.normal_toggle);

        editTextGraphSize =(EditText)findViewById(R.id.editTextGraphLength);
        editTextPolyOrder=(EditText)findViewById(R.id.editTextPolOrder);
        editTextWindowSize=(EditText)findViewById(R.id.editTextWindowSize);

        saveButton=(Button)findViewById(R.id.saveButton);

        toggleButtonX.setChecked(Utility.IsFilterX);
        toggleButtonY.setChecked(Utility.IsFilterY);
        toggleButtonZ.setChecked(Utility.IsFilterZ);
        toggleButtonNormal.setChecked(Utility.IsFilterNormal);
        editTextWindowSize.setText(Integer.toString(Utility.WindowSize));
        editTextPolyOrder.setText(Integer.toString(Utility.PolyOrder));
        editTextGraphSize.setText(Integer.toString(Utility.GraphSize));

        toggleButtonX.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                SaveUtilityDataBoolean(Utility.IsFilterXString,isChecked);
            }
        });
        toggleButtonY.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                SaveUtilityDataBoolean(Utility.IsFilterYString,isChecked);
            }
        });
        toggleButtonZ.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                SaveUtilityDataBoolean(Utility.IsFilterZString,isChecked);
            }
        });
        toggleButtonNormal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                SaveUtilityDataBoolean(Utility.IsFilterNormalString,isChecked);
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveUtilityData();
            }
        });

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
    public void SaveUtilityData(){
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putInt(Utility.WindowSizeString,Integer.parseInt(editTextWindowSize.getText().toString()));
        editor.putInt(Utility.PolyOrderString,Integer.parseInt(editTextPolyOrder.getText().toString()));
        editor.putInt(Utility.GraphSizeString,Integer.parseInt(editTextGraphSize.getText().toString()));
        editor.apply();
    }
}
