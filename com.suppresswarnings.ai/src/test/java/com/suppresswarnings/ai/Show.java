package com.suppresswarnings.ai;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Show extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = 6491242556168785338L;
    public Show(String string) {
        super(string);
    }
    public static void main(String[] args) throws Exception {
        Show show = new Show("Hello");
        int[][] img = Util.readImage(new File("/Users/mingo/Downloads/id1.png"));
        BackgroundPanel bgp =new BackgroundPanel(img); //参数是一个Image对象,
        show.getContentPane().add(bgp);
        show.setSize(img.length,img[0].length);
        show.setVisible(true);
        show.setDefaultCloseOperation(EXIT_ON_CLOSE);
        bgp.requestFocus();
       /* Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            public void eventDispatched(AWTEvent event) {
                if (((KeyEvent) event).getID() == KeyEvent.KEY_PRESSED) {
                    System.out.println(((KeyEvent) event).getKeyCode());
                }
            }
        }, AWTEvent.KEY_EVENT_MASK);*/
    }
}
class BackgroundPanel extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = -282392135192671752L;
    public static final String saveTo = "/Users/mingo/work/code/files/png/idcard-model-352x50x10.ser";
    public static final String saveYesNoTo = "/Users/mingo/work/code/files/png/idcard-yes-no-model-352x50x2.ser";
    FrameNormalizer normalizer = new FrameNormalizer();
    int[][] image;
    int w;
    int h;
    int startx = 255;
    int starty = 380;
    int width = 18;
    int height = 24;
    boolean yeah = true;
    NN nn;
    NN yesno;
    public BackgroundPanel(int[][] im) {
        this.image = im;
        this.w = image.length;
        this.h = image[0].length;
        this.nn = (NN)Util.deserialize(saveTo);
        this.yesno = (NN)Util.deserialize(saveYesNoTo);
        this.setOpaque(true);
        this.addKeyListener(new KeyAdapter(){
            public void keyPressed(KeyEvent e){
                System.out.print(".");
                switch(e.getKeyCode()){
                    case KeyEvent.VK_DOWN:  starty += 1; break;
                    case KeyEvent.VK_UP:    starty -= 1; break;
                    case KeyEvent.VK_LEFT:  startx -= 1; break;
                    case KeyEvent.VK_RIGHT: startx += 1; break;
                    case KeyEvent.VK_ENTER: saveImage();
                    case KeyEvent.VK_SPACE: predict(); break;
                    case KeyEvent.VK_K: kick(); break;
                    default:break;
                }
                repaint();
            }
        });
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                startx = e.getX() ;
                starty = e.getY();
                repaint();
            }
        });
    }

    public void kick() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        final BackgroundPanel panel = this;
        executorService.submit(()->{
            int step = 1;
            for(int y=0;y<h;y+=step) {
                int starty = y;
                int count = 0;
                for(int x=0;x<w;x++) {
                    int startx = x;
                    int[][] frame = new int[width][height];
                    for (int i = 0; i < width; i++) {
                        if(startx + i >= w) {
                            startx = 0;
                        }
                        for (int j = 0; j < height; j++) {
                            if(starty + j >= h) {
                                if(startx + i >= w) {
                                    break;
                                }else {
                                    starty = 0;
                                }
                            }
                            frame[i][j] = image[startx + i][starty + j];
                        }
                    }
                    this.startx = startx;
                    this.starty = starty;
                    panel.repaint();
                    try {
                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int result = predict(frame);
                    if(result > 0) {
                        count += 1;
                        try {
                            TimeUnit.MILLISECONDS.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if(count < 1) {
                    step = 5;
                } else {
                    step = 1;
                }
            }
        });
    }

    public void predict() {
        int[][] frame = new int[width][height];
        for(int i=0;i<width;i++) {
            for(int j=0;j<height;j++) {
                frame[i][j] = image[startx+i][starty+j];
            }
        }
        predict(frame);
    }

    public int predict(int[][] frame) {
        StopWatch watch = new StopWatch();
        watch.start();
        FrameSlider dotSlider = new FrameSlider(frame, 3, 3, 2, 2, 0, 0);
        List<Double> collect = StreamSupport.stream(dotSlider, false)
                .map(normalizer)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        Data data = new Data(collect, new double[0]);
        double[] test = yesno.test(data.x);
        int index = Util.argmax(test);

        if (index == 0) {
            double[] result = nn.test(data.x);
            int number = Util.argmax(result);
            System.out.println("here is the number: " + number + " which probability is " + result[number]);
            if(result[number] > 0.75) {
                drawIn(startx, starty-25, Util.readImage(new File("/Users/mingo/work/code/files/png/idcard-best/" + number + ".png")));
                return number;
            }
        } else {
            System.out.println("oh shit, here is non number.");
        }
        watch.stop();
        System.out.println("cost " + watch.duration() + "ms");
        return -1;
    }

    public void saveImage() {
        String filename = String.format("frame-%d_%d.jpg", startx, starty);
        System.out.println("save image: " + filename);
        int[][] frame = new int[width][height];
        for(int i=0;i<width;i++) {
            for(int j=0;j<height;j++) {
                frame[i][j] = image[startx+i][starty+j];
            }
        }
        Util.printImage(frame, "/Users/mingo/work/code/files/png/idcard-predict/"+ filename);
    }
    public void drawIn(int startx, int starty, int[][] small) {
        Util.drawIn(image, startx, starty, small);
    }
    public void paintComponent(Graphics g) {
        super.paintComponents(g);
        BufferedImage nbi = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int b = image[x][y];
                nbi.setRGB(x, y, b);
            }
        }
        if(startx + width >= w) {
            return;
        }
        if(starty + height >= h) {
            return;
        }

        if(yeah) {
            int rgb = Color.RED.getRGB();
            for(int i=0;i<width;i++) {
                nbi.setRGB(startx+i, starty, rgb);
            }
            for(int j=0;j<height;j++) {
                nbi.setRGB(startx, starty + j, rgb);
            }
        }

        g.drawImage(nbi, 0, 0, this.getWidth(), this.getHeight(), null);
    }
}