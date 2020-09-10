package com.suppresswarnings.ai;

/**
 * 图片窗口
 * 包含窗口像素，窗口所在的坐标。
 */
public class Frame {
    int[][] data;
    int x;
    int y;

    public Frame(int[][] data, int x, int y) {
        this.data = data;
        this.x = x;
        this.y = y;
    }
    public int[][] getData() {
        return data;
    }
    public void setData(int[][] data) {
        this.data = data;
    }
    public Frame setX(int x) {
        this.x = x;
        return this;
    }
    public Frame setY(int y) {
        this.y = y;
        return this;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
}
