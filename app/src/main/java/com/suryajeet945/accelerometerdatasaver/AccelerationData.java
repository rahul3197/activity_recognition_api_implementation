package com.suryajeet945.accelerometerdatasaver;

/**
 * Created by cc on 27-07-2017.
 */

public  class AccelerationData{
    float x;
    float y;
    float z;
    double xf,yf,zf,normal, normalf;
    int index;

    public AccelerationData(int index,float x,float y,float z,double normal,double xf,double yf,double zf,double normalf){
        this.index=index;
        this.x=x;
        this.y=y;
        this.z=z;
        this.normal=normal;
        this.normalf = normalf;
        this.xf=xf;
        this.yf=yf;
        this.zf=zf;
    }
    public double NormalData(){
        return Math.sqrt(x*x+y*y+z*z);
    }
}