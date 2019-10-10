package com.suppresswarnings.spring;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

public class Converter {

    static int unit = 9;
    static int pixel = 4;
    static int[] sparse = {230, 5, 155, 55, 130, 105, 205, 30, 80, 180};
    static int[][] sudo = {{8, 1, 2, 7, 5, 3, 6, 4, 9}, {9, 4, 3, 6, 8, 2, 1, 7, 5}, {6, 7, 5, 4, 9, 1, 2, 8, 3}, {1, 5, 4, 2, 3, 7, 8, 9, 6}, {3, 6, 9, 8, 4, 5, 7, 2, 1}, {2, 8, 7, 1, 6, 9, 5, 3, 4}, {5, 2, 1, 9, 7, 4, 3, 6, 8}, {4, 3, 8, 5, 2, 6, 9, 1, 7}, {7, 9, 6, 3, 1, 8, 4, 5, 2}
    };
    public String formatWidth(String str, int width) {
        if (str.length() < width)
            return formatWidth('0' + str, width);
        else
            return str;
    }
    public int[] transfer(int[] data, int pixel) {
        int[][] line = new int[unit][4];
        int counter = 0;
        int start = 0;
        for (int i = 0, pointer = 0; i < data.length; i += pixel, pointer++) {
            if (pointer == unit) {
                pointer = 0;
                if (counter == unit) {
                    counter = 0;
                }
                int[][] temp = new int[unit][4];
                int[] array = sudo[counter];
                if (line.length < array.length) {
                    temp = line;
                } else {
                    for (int j = 0; j < line.length; j++) {
                        int move = array[j] - 1;
                        temp[move] = line[j];
                    }
                }

                for (int k = start, index = 0; k < data.length; k += pixel, index++) {
                    if (index >= temp.length) {
                        break;
                    }
                    int[] arr = temp[index];
                    data[k] = arr[0];
                    data[k + 1] = arr[1];
                    data[k + 2] = arr[2];
                    data[k + 3] = arr[3];
                }
                line = new int[unit][4];
                start = i;
                counter += 1;
            }
            line[pointer] = new int[]{data[i + 0], data[i + 1], data[i + 2], data[i + 3]};
        }
        return data;
    }

    /**
     * from horizon to vertical
     * @param data
     * @param width
     * @param height
     * @param pixel
     * @return
     */
    public int[] deflate(int[] data, int width, int height, int pixel) {
        int[][][] cube = new int[height][width][pixel];
        int index = 0;
        for (int y = 0; y < height; y++) {
            cube[y] = new int[width][pixel];
            for (int x = 0; x < width; x++) {
                cube[y][x] = new int[]{data[index + 0], data[index + 1], data[index + 2], data[index + 3]};
                index += pixel;
            }
        }

        index = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int[] pixels = cube[y][x];
                data[index + 0] = pixels[0];
                data[index + 1] = pixels[1];
                data[index + 2] = pixels[2];
                data[index + 3] = pixels[3];
                index += pixel;
            }
        }
        return data;
    }

    /**
     * from vertical to horizon
     * @param data
     * @param width
     * @param height
     * @param pixel
     * @return
     */
    public int[] inflate(int[] data, int width, int height, int pixel) {
        int[][][] cube = new int[height][width][pixel];
        int index = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cube[y][x] = new int[]{data[index + 0], data[index + 1], data[index + 2], data[index + 3]};
                index += pixel;
            }
        }

        index = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int[] pixels = cube[y][x];
                data[index + 0] = pixels[0];
                data[index + 1] = pixels[1];
                data[index + 2] = pixels[2];
                data[index + 3] = pixels[3];
                index += pixel;
            }
        }
        return data;
    }
    public static int[] str2arr(String str, boolean reverse) {
        if (reverse) {
            str = new StringBuilder(str).reverse().toString();
        }
        String[] arr = str.split("");
        int[] key = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            int k = Integer.parseInt(arr[i]);
            int u = sudo[i][k % 8];
            key[i] = u - 1;
        }

        return key;
    }

    public int[] encode(int[] data, int[] key, int pixel) {
        for (int i = 0, j = 0; i < data.length; i += pixel, j++) {
            if (j == key.length) {
                j = 0;
            }
            int xor = sparse[sudo[j][key[j]]];
            data[i] = xor ^ data[i];
            data[i + 1] = xor ^ data[i + 1];
            data[i + 2] = xor ^ data[i + 2];
        }
        return data;
    }


    public int[] detransfer(int[] data, int pixel) {
        int[][] line = new int[unit][pixel];
        int counter = 0;
        int start = 0;
        for (int i = 0, pointer = 0; i < data.length; i += pixel, pointer++) {
            if (pointer == unit) {
                pointer = 0;
                if (counter == unit) {
                    counter = 0;
                }
                int[][] temp = new int[unit][pixel];
                int[] array = sudo[counter];
                if (line.length < array.length) {
                    temp = line;
                } else {
                    for (int j = 0; j < line.length; j++) {
                        int move = array[j] - 1;
                        temp[j] = line[move];
                    }
                }

                for (int k = start, index = 0; k < data.length; k += pixel, index++) {
                    if (index >= temp.length) {
                        break;
                    }
                    int[] arr = temp[index];
                    data[k + 0] = arr[0];
                    data[k + 1] = arr[1];
                    data[k + 2] = arr[2];
                    data[k + 3] = arr[3];
                }
                line = new int[unit][pixel];
                start = i;
                counter += 1;
            }
            line[pointer] = new int[]{data[i + 0], data[i + 1], data[i + 2], data[i + 3]};
        }
        return data;
    }

    public int[] encrypt(int[] data, int W, int H, String key) {
        data = transfer(data, pixel);
        data = deflate(data, W, H, pixel);
        data = transfer(data, pixel);
        data = encode(data, str2arr(key, false), pixel);

//        data = inflate(data, W, H, pixel);
//        data = transfer(data, pixel);
//        data = deflate(data, W, H, pixel);
//        data = transfer(data, pixel);
//        data = encode(data, str2arr(key, true), pixel);
        return data;
    }

    public int[] decrypt(int[] data, int W, int H, String key) {
        data = encode(data, str2arr(key, false), pixel);
        data = detransfer(data, pixel);
        data = inflate(data, W, H, pixel);
        data = detransfer(data, pixel);

//        data = deflate(data, W, H, pixel);
//        data = encode(data, str2arr(key, false), pixel);
//        data = detransfer(data, pixel);
//        data = inflate(data, W, H, pixel);
//        data = detransfer(data, pixel);
        return data;
    }


    public static String byteToHex(byte[] bytes){
        String strHex = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < bytes.length; n++) {
            strHex = Integer.toHexString(bytes[n] & 0xFF);
            sb.append((strHex.length() == 1) ? "0" + strHex : strHex);
        }
        return sb.toString().trim();
    }

    public static int[] read(BufferedImage bufferedImage) {
        int[] data = new int[bufferedImage.getWidth() * bufferedImage.getHeight() * 4];
        int index = 0;
        for(int i=0;i<bufferedImage.getHeight();i++) {
            for(int j=0;j<bufferedImage.getWidth();j++) {
                int rgb = bufferedImage.getRGB(j, i);
                Color c = new Color(rgb, true);
                data[index+0] = c.getRed();
                data[index+1] = c.getGreen();
                data[index+2] = c.getBlue();
                data[index+3] = c.getAlpha();
                index += 4;
            }
        }
        return data;
    }

    public static void write(int[] date, int W, int H, String input) throws IOException {
        BufferedImage bufferedImage2 = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB_PRE);
        int index = 0;
        for(int i=0;i<bufferedImage2.getHeight();i++) {
            for(int j=0;j<bufferedImage2.getWidth();j++) {
                Color c = new Color(date[index],date[index+1],date[index+2],date[index+3]);
                bufferedImage2.setRGB(j, i, c.getRGB());
                index += 4;
            }
        }
        File file = new File(input+ ".new.png");
        ImageIO.write(bufferedImage2, "png",  file);
    }

    public static void main(String[] args) throws IOException {

        String keystr = "123456";
        Converter that = new Converter();

        if(!true) Files.list(Paths.get("/Users/lijiaming/company/mine/raw/")).forEach(path ->{
            File file = path.toFile();
            System.out.println(file.getAbsolutePath());
            BufferedImage bufferedImage = null;
            try {
                bufferedImage = ImageIO.read(file);
                int W = bufferedImage.getWidth();
                int H = bufferedImage.getHeight();
                int[] data = read(bufferedImage);
                data = that.encrypt(data, W, H, keystr);
                write(data, W, H, "/Users/lijiaming/company/mine/encode/"+file.getName()+".encrypt");
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        String input = "/Users/lijiaming/company/mine/encode/身份证-018.jpeg.encrypt.new.png";
        BufferedImage bufferedImage = ImageIO.read(new File(input));
        int W = bufferedImage.getWidth();
        int H = bufferedImage.getHeight();
        int[] data = read(bufferedImage);

        long start = System.currentTimeMillis();
        data = that.decrypt(data, W, H, keystr);
        long end = System.currentTimeMillis();
        System.out.println(end - start);

        write(data, W, H, "/Users/lijiaming/company/mine/decrypt");
    }
}
