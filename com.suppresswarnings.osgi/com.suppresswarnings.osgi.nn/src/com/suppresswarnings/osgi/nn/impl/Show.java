package com.suppresswarnings.osgi.nn.impl;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.suppresswarnings.osgi.nn.PointMatrix;
import com.suppresswarnings.osgi.nn.Util;
import com.suppresswarnings.osgi.nn.cnn.Digit;
import com.suppresswarnings.osgi.nn.cnn.MNIST;

public class Show extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6491242556168785338L;
	public static String serializeTo = "D:/lijiaming/digit.nn.best";
	public Show(String string) {
		super(string);
	}
	public static void main(String[] args) throws Exception {
		Show show = new Show("Hello");
		BackgroundPanel bgp =new BackgroundPanel(new ImageIcon("D:/tmp/trible.png").getImage()); //参数是一个Image对象,
        bgp.setBounds(0,0,300,300);  
		show.getContentPane().add(bgp);
		show.setSize(330, 330);
		show.setVisible(true);
		show.setDefaultCloseOperation(EXIT_ON_CLOSE);
		MNIST mnist = new MNIST(MNIST.TYPE_TEST);
		mnist.init();
		TestMnist test = (TestMnist) Util.deserialize(serializeTo);
		NN nn = (NN) Util.deserialize(serializeTo + ".nn");
		show.addMouseListener(new MouseListener() {
			int count = 0;
			int sum = 0;
	      public void mouseReleased(MouseEvent e) {}            
	      public void mousePressed(MouseEvent e) {}             
	      public void mouseExited(MouseEvent e) {}          
	      public void mouseEntered(MouseEvent e) {}         
	      @Override
	      public void mouseClicked(MouseEvent e) {
			if(mnist.hasNext()) {
				Digit digit = mnist.next();
				bgp.change(Util.getImage(digit.data));
				bgp.repaint();
				
				test.pm.feedMatrix(digit.data, PointMatrix.TYPE_CONVOLUTION);
				double[][] v = test.view.normalizeAndTake();
				double[] input = test.descendLayer.descend(v);
				nn.forward(input);
				double[] result = nn.output();
				int r = Util.argmax(result);
				int t = Util.argmax(digit.label);
				boolean right = (r == t);
				if(right) count ++;
				sum ++;
				show.setTitle(r + " == " + t + " ? " + right + " right=" + count + " / " + sum);
				if(!right) JOptionPane.showConfirmDialog(show, "Wrong guess!");
			} else {
				mnist.close();
			}
	      }  
		});  
	}
}
class BackgroundPanel extends JPanel  
{  
    /**
	 * 
	 */
	private static final long serialVersionUID = -282392135192671752L;
	Image im;  
    public BackgroundPanel(Image im)  
    {  
        this.im=im;  
        this.setOpaque(false);                    //设置控件不透明,若是false,那么就是透明
    }
    public void change(Image nw) {
    	this.im = nw;
    }
    //Draw the background again,继承自Jpanle,是Swing控件需要继承实现的方法,而不是AWT中的Paint()
    public void paintComponent(Graphics g)       //绘图类,详情可见博主的Java 下 java-Graphics 
    {  
        super.paintComponents(g);  
        g.drawImage(im,0,0,this.getWidth(),this.getHeight(),this);  //绘制指定图像中当前可用的图像。图像的左上角位于该图形上下文坐标空间的 (x, y)。图像中的透明像素不影响该处已存在的像素

    }  
}