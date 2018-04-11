package com.suppresswarnings.osgi.neuralnetwork;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.DecimalFormat;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Display extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7962556170449642969L;
	public static String serializeTo = "D:/lijiaming/NN.dnn.024";
	public static int max = 10000;
	public long confirm = 10000;
	public void confirm(long v) {
		this.confirm = v;
	}
	public Display(String string) {
		super(string);
	}
	
	public static double convert(double origin) {
		return origin;
	}
	
	public boolean predict(NN nn, double[] x, int label) {
		System.out.println("[lijiaming] target: " + label + " input: " + Arrays.toString(x));
		nn.forward(x);
		double[] output = nn.output();
		int result = Util.argmax(output);
		return result == label;
	}
	
	public static void main(String[] args) throws Exception {
		int size = 5;
		NN nn = (NN) Util.deserialize(serializeTo);//new NN(size, sizey, new int[]{8});//  
		Display show = new Display("Display Of Neuro");
		NNPanel bgp = new NNPanel(nn);
		JTextField text = new JTextField(50);
		JButton btn = new JButton("Input");
		show.setSize(1600, 900);
		show.setLayout(new BorderLayout());
		show.getContentPane().add(bgp, BorderLayout.CENTER);
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0,2,0,0));
		panel.add(text);
		panel.add(btn);
		show.getContentPane().add(panel, BorderLayout.SOUTH);
		show.setVisible(true);
		show.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		btn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String txt = text.getText();
				if(txt == null || txt.length() < 3) {
					System.out.println("txt: " + txt);
					return;
				}
				String[] arg = txt.split(",");
				if(arg.length < nn.inputSize) {
					System.out.println("length: " + arg.length + " should be " + nn.inputSize);
					return;
				}
				double[] x = new double[nn.inputSize];
				int i=0;
				for(;i<x.length;i++) {
					x[i] = convert(Double.parseDouble(arg[i]));
				}

				nn.forward(x);
				
				show.repaint();
				show.setTitle("[" + show.confirm + "] correct <" + nn + ">");
				text.setText("");
			}
		});
		show.addMouseWheelListener(new MouseWheelListener() {
			double temp = 0;
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				temp += e.getWheelRotation();
				max += temp;
				
				double[] x = Util.random(1, size)[0];
				nn.forward(x);
				
				System.out.println();
				show.setTitle("Predicting... " + max);
				show.repaint();
		      }
		});
		
		show.addMouseListener(new MouseListener() {
			boolean running = false;
			@Override public void mouseReleased(MouseEvent e) {}
			@Override public void mousePressed(MouseEvent e) {}
			@Override public void mouseExited(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
	      	@Override
			public void mouseClicked(MouseEvent e) {
				show.confirm(max);
				if(running) return;
				running = true;
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						System.out.println("start: " + show.confirm);
						int step = 0;
						int epoch = max;
						int size = 1000;
						double[][] inputs = Util.random(size, 5);
						
						Util.print(inputs[1]);
						System.out.println();
						Util.print(get024(inputs[1]));
						System.out.println();
						
						while(step < epoch) {
							double error = 0;
							for(int i=0;i<size;i++) { 
								double[] input = inputs[i];
								double[] output = get024(inputs[i]);
								error += nn.train(input, output);
							}
							System.out.println(error);
							if(error < 1e-4) break;
							step ++;
							if(step % 100 == 0) {
								show.setTitle("Training... " + step + " / " + epoch + " Err: " + error);
								show.repaint();
							}
						}
						System.out.println(nn.toString());
						Util.serialize(nn, serializeTo);
						show.repaint();
						System.out.println("end: " + nn.toString());
						running = false;
					}
				}).start();
				
			}  
		});  
	}
	public static double[] get024(double[] len5) {
		double[] len3 = new double[3];
		len3[0] = (len5[0] + len5[1]) / 2;
		len3[1] = len5[2];
		len3[2] = (len5[3] + len5[4]) / 2;
		return len3;
	}
}

class NNPanel extends JPanel {
	static final int startx = 10;
	static final int starty = 10;
	static final int stepx = 500;
	static final int stepy = 80;
	static final int width = 45;
	static final int height = 25;

	/**
	 * 
	 */
	private static final long serialVersionUID = -1830722600055306899L;
	DecimalFormat f = new DecimalFormat("0.0000");
	NN nn;
	NNPanel(NN n){
		this.nn = n;
		this.setBackground(Color.BLACK);
		this.setOpaque(true);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		for(Cell cell : nn.input.cells) {
			drawCell(g, cell);
			for(Edge edge : cell.down) {
				drawEdge(g, edge);
			}
		}
		for(Layer hidden : nn.hiddenLayer) {
			for(Cell cell : hidden.cells) {
				drawCell(g, cell);
				for(Edge edge : cell.down) {
					drawEdge(g, edge);
				}
			}
		}
		for(Cell cell : nn.output.cells) {
			drawCell(g, cell);
			for(Edge edge : cell.down) {
				drawEdge(g, edge);
			}
		}
	}
	public void drawEdge(Graphics g, Edge edge) {
		Cell up = edge.up();
		Cell down = edge.down();
		int x1 = up.level * stepx + startx + width;
		int y1 = up.index * stepy + starty + height;
		int x2 = down.level * stepx + startx;
		int y2 = down.index * stepy + starty + height;
		if(edge.weight > 0) g.setColor(Color.CYAN);
		else g.setColor(Color.YELLOW);
		
		if(Math.abs(edge.weight) < 1e-10) {
			System.out.println();
		} else {
			g.drawLine(x1, y1, x2, y2);
		}
	}
	public void drawCell(Graphics g, Cell cell) {
		int x = cell.level * stepx + startx;
		int y = cell.index * stepy + starty;
		g.setColor(Color.GREEN);
		if(cell.type == Cell.TYPE_BIAS) g.drawRoundRect(x, y, width, height, 20, 20);
		else g.drawRect(x, y, width, height);
		g.setColor(Color.WHITE);
		g.drawString(f.format(cell.placeholder), x + 2, y + 18);
		int down = 0;
		for(Edge d : cell.down) if(Math.abs(d.weight) > 1e-10) down++;
		g.drawString("" + down, x + 33, y + 43);
	}
}
