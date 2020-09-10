package com.suppresswarnings.ai;

import java.util.Arrays;
import java.util.List;

/**
 * 训练样本数据
 * 包含x向量和y向量
 */
public class Data {
    double[] x;
    double[] y;
    public Data(List<Double> list, double[] target) {
        x = new double[list.size()];
        for(int i=0;i<list.size();i++) {
            x[i] = list.get(i);
        }
        y = target;
    }
    public void setY(double[] y){this.y = y;}

    @Override
    public String toString() {
        return "Data{("+x.length+","+y.length+") x=" + Arrays.toString(x) + ", y=" + Arrays.toString(y) + '}';
    }
}
