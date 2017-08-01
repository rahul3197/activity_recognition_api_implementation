package com.example;

import org.apache.commons.math3.analysis.function.Log;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class MyClass {
    public static void main(String[] args) {
        // Create a real matrix with two rows and three columns, using a factory
// method that selects the implementation class for us.
        double[][] matrixData = { {1,2,3}, {2,5,3}};
        RealMatrix m = MatrixUtils.createRealMatrix(matrixData);

// One more with three rows, two columns, this time instantiating the
// RealMatrix implementation class directly.
        double[][] matrixData2 = { {1,2}, {2,5}, {1, 7}};
        RealMatrix n = new Array2DRowRealMatrix(matrixData2);

// Note: The constructor copies  the input double[][] array in both cases.

// Now multiply m by n
        RealMatrix p = m.multiply(n);
        System.out.println(p.getRowDimension());    // 2
        System.out.println(p.getColumnDimension()); // 2
        System.out.println(p);
// Invert p, using LU decomposition
        RealMatrix pInverse = new LUDecomposition(p).getSolver().getInverse();
        System.out.print("Hello Suraj chu..");

    }
}
