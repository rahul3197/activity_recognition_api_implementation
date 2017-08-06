package com.suryajeet945.accelerometerdatasaver;

/**
 * Created by cc on 07-08-2017.
 */

public class MatrixOperations {
    private MatrixOperations(){}
    public static float[][] MatrixMultiply(Matrix x,Matrix y){
        float[][]result=new float[x.data.length][y.data[0].length];
        for (int i=0;i<x.data.length;i++){
            for (int j=0;j<y.data[0].length;i++){
                result[i][j]=Sum(MatrixRow(x.data,i),MatrixColumn(y.data,j));
            }
        }
        return result;

    }
    public static float Sum(float[] row,float[] column){
        float sum=0;
        for (int i=0;i<row.length;i++){
            sum+=row[i]*column[i];
        }
        return sum;
    }
    public static float[] MatrixColumn(float[][]matrix,int column){
        float[] result=new float[matrix.length];
        for (int i=0;i<matrix.length;i++){
            result[i]=matrix[i][column];
        }
        return result;
    }
    public static float[]MatrixRow(float[][]matrix,int row){
        return matrix[row];
    }
}
