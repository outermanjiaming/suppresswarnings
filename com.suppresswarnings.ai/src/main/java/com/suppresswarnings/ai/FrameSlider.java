package com.suppresswarnings.ai;

import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * 滑动窗口
 * 指定图片像素，指定窗口大小，指定步长，指定起始坐标，然后开始滑动并提供窗口像素。
 */
public class FrameSlider implements Spliterator<Frame> {
    int startx = 0;
    int starty = 0;
    int x0 = 0;
    int y0 = 0;
    int width = 10;
    int height = 10;
    int stepx;
    int stepy;
    int w;
    int h;
    int[][] image;
    transient int size;
    public FrameSlider(int[][] image, int width, int height, int stepx, int stepy, int startx, int starty) {
        this.image = image;
        this.width = width;
        this.height = height;
        this.stepx = stepx;
        this.stepy = stepy;
        this.startx = startx;
        this.starty = starty;
        this.x0 = startx;
        this.y0 = starty;
        this.w = image.length;
        this.h = image[0].length;
        int m = ((w - width - startx) / stepx + 1);
        int n = ((h - height - starty) / stepy + 1);
        this.size = m * n;
    }
    public FrameSlider(int[][] image, int width, int height, int stepx, int stepy) {
        this.image = image;
        this.width = width;
        this.height = height;
        this.stepx = stepx;
        this.stepy = stepy;
        this.h = image[0].length;
        int m = ((w - width) / stepx + 1);
        int n = ((h - height) / stepy + 1);
        this.size = m * n;
    }

    @Override
    public boolean tryAdvance(Consumer<? super Frame> action) {
        if(size <= 0) return false;
        size --;
        startx += stepx;
        if(startx + width > w) {
            startx = x0;
            starty += stepy;
            if(starty + height > h) {
                starty = y0;
            }
        }
        int[][] frame = Util.frame(width, height, startx, starty, image);
        Frame one = new Frame(frame, startx, starty);
        action.accept(one);
        return true;
    }

    @Override
    public Spliterator<Frame> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return size;
    }

    @Override
    public int characteristics() {
        return 0;
    }

}
