package com.suppresswarnings.osgi.nn;

public interface Link {
	public void update(double delta);
	public double delta();
	public void multiply();
	public double value();
	public void assign(double value);
	public Node down();
	public Node up();
	public void down(Node down);
	public void up(Node up);
}
