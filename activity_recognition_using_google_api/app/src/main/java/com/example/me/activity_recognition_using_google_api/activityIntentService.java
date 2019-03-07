package com.example.me.activity_recognition_using_google_api;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class activityIntentService extends IntentService{
    protected static final String TAG = "Activity";
    //Call the super IntentService constructor with the name for the worker thread//
    public activityIntentService() {
        super(TAG);
    }
    @Override
    public void onCreate()
    {
        SharedPreferences.Editor a =PreferenceManager.getDefaultSharedPreferences(this).edit();
        a.putString("started_intent","true");
        a.apply();
        super.onCreate();
    }

//Define an onHandleIntent() method, which will be called whenever an activity detection update is available//

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
//Check whether the Intent contains activity recognition data//
        SharedPreferences.Editor a =PreferenceManager.getDefaultSharedPreferences(this).edit();
        a.putString("has result",ActivityRecognitionResult.hasResult(intent)+"");
        if(ActivityRecognitionResult.hasResult(intent))
        {

            //If data is available, then extract the ActivityRecognitionResult from the Intent//
            Log.w("intent","has result");
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            ArrayList<DetectedActivity> detectedActivities = (ArrayList)result.getProbableActivities();

            a.putString(track_activity.DETECTED_ACTIVITY,detectedActivitiesToJson(detectedActivities));


        }
        a.apply();
    }
    static String getActivityString(Context context, int detectedActivityType){
        Resources resources=context.getResources();
        switch(detectedActivityType)
        {
            case DetectedActivity.ON_BICYCLE:
                return resources.getString(R.string.bicycle);
            case DetectedActivity.ON_FOOT:
                return resources.getString(R.string.foot);
            case DetectedActivity.RUNNING:
                return resources.getString(R.string.running);
            case DetectedActivity.STILL:
                return resources.getString(R.string.still);
            case DetectedActivity.TILTING:
                return resources.getString(R.string.tilting);
            case DetectedActivity.WALKING:
                return resources.getString(R.string.walking);
            case DetectedActivity.IN_VEHICLE:
                return resources.getString(R.string.vehicle);
            default:
                return resources.getString(R.string.unknown_activity);


        }

    }

    static final int[] POSSIBLE_ACTIVITIES = {

            DetectedActivity.STILL,
            DetectedActivity.ON_FOOT,
            DetectedActivity.WALKING,
            DetectedActivity.RUNNING,
            DetectedActivity.IN_VEHICLE,
            DetectedActivity.ON_BICYCLE,
            DetectedActivity.TILTING,
            DetectedActivity.UNKNOWN
    };

static String detectedActivitiesToJson(ArrayList<DetectedActivity> detectedAcivitiesList){
    Type type =new TypeToken<ArrayList<DetectedActivity>>(){}.getType();
    return new Gson().toJson(detectedAcivitiesList,type);
}
    static ArrayList<DetectedActivity> detectedActivitiesFromJson(String jsonArray) {
        Type listType = new TypeToken<ArrayList<DetectedActivity>>(){}.getType();
        ArrayList<DetectedActivity> detectedActivities = new Gson().fromJson(jsonArray, listType);
        if (detectedActivities == null) {
            detectedActivities = new ArrayList<>();
        }
        return detectedActivities;
}
}