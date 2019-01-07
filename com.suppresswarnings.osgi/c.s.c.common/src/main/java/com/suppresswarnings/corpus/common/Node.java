package com.suppresswarnings.corpus.common;


public interface Node {
	public int TYPE_BIAS   = 100;
	public int TYPE_INPUT  = 101;
	public int TYPE_HIDDEN = 102;
	public int TYPE_OUTPUT = 103;
	public int INDEX_BIAS   = -1;
	public int LEVEL_INPUT  = 0;
	public int LEVEL_OUTPUT = -1;
	public Activation getFx();
	public void connect();
	public void upLink(Link link);
	public void downLink(Link link);
	public void countDown();
	public void countUp();
	public void forward();
	public void propagate();
	public void receive();
	public void gradient(double error);
	public void assign(double value);
	public double value();
}
