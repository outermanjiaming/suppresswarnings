package com.suppresswarnings.ai;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 窗口像素数值归一化为(0~1)
 */
public class FrameNormalizer implements Function<Frame, List<Double>> {

    @Override
    public List<Double> apply(Frame frame) {
        int[][] data = frame.data;
        List<Double> result = new ArrayList<Double>();
        int w = data.length;
        int h = data[0].length;
        int size = w * h;
        double sum = 0;
        int r = 0;
        int g = 0;
        int b = 0;
        for(int i=0;i<w;i++) {
            for(int j=0;j<h;j++) {
                int color = data[i][j];
                Color pixel = new Color(color);
                if(pixel.getRed() > 240) r ++;
                if(pixel.getGreen() > 240) g ++;
                if(pixel.getBlue() > 240) b ++;
                double rgb = pixel.getRed()*0.299 + pixel.getGreen()*0.587 + pixel.getBlue()*0.114;
                sum += rgb / 255;
            }
        }
        result.add(sum / size);
        result.add(r * 1.0 / size);
        result.add(g * 1.0 / size);
        result.add(b * 1.0 / size);
        return result;
    }
}
