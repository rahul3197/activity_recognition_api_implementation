package com.suryajeet945.accelerometerdatasaver;

/**
 * Created by cc on 27-07-2017.
 */

public  class AccelerationData{
    float x;
    float y;
    float z;
    public AccelerationData(float x,float y,float z){
        this.x=x;
        this.y=y;
        this.z=z;
    }
    public double NormalData(){
        return Math.sqrt(x*x+y*y+z*z);
    }
}