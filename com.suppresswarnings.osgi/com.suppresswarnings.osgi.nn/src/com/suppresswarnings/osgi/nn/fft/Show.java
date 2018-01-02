package com.suppresswarnings.osgi.nn.fft;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Show extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7912751210578150102L;
	public Show(String title){
		super(title);
	}
	
	public void init(Complex[] x) {
		this.setSize(800, 300);
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.getContentPane().add(new YXpanel(x));
	}
	
	class YXpanel extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4117511872880111769L;
		Complex[] data;
		int width = 740;
		int height = 220;
		int b = 2;
		public YXpanel(Complex[] x) {
			this.data = x;
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			g.setColor(Color.BLACK);
			g.fillRect(20, 20, width, height);
			g.setColor(Color.GREEN);
			int start = 50;
			int center = 130;
			int step = 10;
			int xstep = start;
			double ystep = (double)height/data.length;
			int yy = start;
			for(int i=0;i<data.length;i++) {
				int y = center;
				if(data[i].r() > 1) y -= 2*data[i].r();
				else y -= (int)(100*data[i].r());
				g.setColor(Color.GREEN);
				g.drawOval(xstep, y, b, b);
				g.setColor(Color.GREEN);
				g.drawOval(xstep, center, 1, 1);
				g.drawOval(start, yy, 1, 1);
				xstep += step;
				yy += ystep;
			}
		}
		
	}
}
