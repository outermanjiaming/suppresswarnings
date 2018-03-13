package com.suppresswarnings.osgi.nn.impl;

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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.suppresswarnings.osgi.nn.LossFunction;
import com.suppresswarnings.osgi.nn.Util;

public class Visual extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7962556170449642969L;
	public static String serializeTo = "iris.visual.ser";
	public static long max = 100000000;
	public long confirm = 10000;
	public void confirm(long v) {
		this.confirm = v;
	}
	public Visual(String string) {
		super(string);
	}
	public static void main2(String[] args) throws IOException {
		double x = Files.lines(Paths.get("linyu2.V3.DA")).count();
		System.out.println(x);
	}
	
	public static double convert(double origin) {
		return origin / 10.0d;
	}
	
	public boolean predict(NN nn, double[] x, int label) {
		System.out.println("[lijiaming] target: " + label + " input: " + Arrays.toString(x));
		nn.forward(x);
		double[] output = nn.output();
		int result = Util.argmax(output);
		return result == label;
	}
	
	public static void main(String[] args) throws Exception {
		int size = 4;
		int sizey= 3;
		NN nn = (NN) Util.deserialize(serializeTo);//new NN(size, sizey, new int[]{8});//  
		Visual show = new Visual("Visual Of Neuro");
		NNPanel bgp = new NNPanel(nn);
		JTextField text = new JTextField(50);
		JButton btn = new JButton("Input");
		show.setSize(1080, 950);
		show.setLayout(new BorderLayout());
		show.getContentPane().add(bgp, BorderLayout.CENTER);
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0,2,0,0));
		panel.add(text);
		panel.add(btn);
		show.getContentPane().add(panel, BorderLayout.SOUTH);
		show.setVisible(true);
		show.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		List<double[]> list = Files.lines(Paths.get("iris_test.csv")).skip(1).map(line -> {
			String[] argv = line.split(",");
			double[] value= new double[argv.length];
			for(int i=0;i<argv.length;i++) {
				String v = argv[i];
				value[i] = Double.valueOf(v);
			}
			return value;
		}).collect(Collectors.toList());
		
		int all = list.size();
		
		double[][] matrix = new double[all][size];
		double[][] targets= new double[all][sizey];
		int right = 0;
		for(int i=0;i<all;i++) {
			double[] value = list.get(i);
			System.out.println("[lijiaming] target: "+ Arrays.toString(value));
			double[] inputx = new double[size];
			int n=0;
			for(;n<size;n++) {
				inputx[n] = convert(value[n]);
			}
			int label = (int) value[n];
			
			if(show.predict(nn, inputx, label)) right ++;
			
			double[] inputy = new double[sizey];
			inputy[label] = 1.0;
			
			matrix[i] = inputx;
			targets[i]= inputy;
		}
		show.setTitle("Correct: " + right + " / " + all);
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
				int label = (int)Double.parseDouble(arg[i]);
				boolean correct = show.predict(nn, x, label);
				Util.print(x);
				System.out.println();
				show.repaint();
				show.setTitle("[" + show.confirm + "] correct <" + correct + ">");
				text.setText("");
			}
		});
		show.addMouseWheelListener(new MouseWheelListener() {
			double temp = 0;
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				temp += e.getWheelRotation();
				max += temp;
				show.setTitle("["+show.confirm+"] Click to confirm: "+max);
				double[][] matrix = Util.random(1, size);
				double[] x = matrix[0];
				double[] y = new double[sizey];
				nn.forward(x);
				double[] output = nn.output();
				double error = LossFunction.MSE.f(output, y);
				boolean right = error < 0.000001;
				Util.print(x);
				System.out.println(" <" + right + "> ");
				Util.print(output);
				System.out.println();
				System.out.println();
				show.repaint();
				show.setTitle("[" + show.confirm + "]" + right);
		      }
		});
		
		show.addMouseListener(new MouseListener() {
			@Override public void mouseReleased(MouseEvent e) {}
			@Override public void mousePressed(MouseEvent e) {}
			@Override public void mouseExited(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
	      	@Override
			public void mouseClicked(MouseEvent e) {
				show.confirm(max);
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						System.out.println("start: " + show.confirm);
						boolean b = true;
						while(show.confirm -- > 0) {
							for(int i=0;i<all;i++) {
								double[] x = matrix[i];
								double[] y = targets[i];
								if(b) {
									System.out.print("[lijiaming] target: ");Util.print(y);System.out.println();
								}
								nn.forward(x);
								nn.loss(y);
								nn.backprop(nn.gradients);
								
							}
							b = false;
							double error = nn.error;
							nn.clear();
							if(show.confirm % 10 == 0) {
								show.repaint();
								show.setTitle("[" + show.confirm + "] Total:" + error);
							}
							if(error < 1e-10) break;
						}
						show.repaint();
						Util.serialize(nn, serializeTo);
						System.out.println("end: " + nn.toString());
					}
				}).start();
				
			}  
		});  
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
