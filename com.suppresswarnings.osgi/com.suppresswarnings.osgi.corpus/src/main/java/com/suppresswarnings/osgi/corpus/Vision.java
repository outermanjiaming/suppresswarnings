package com.suppresswarnings.osgi.corpus;

import java.awt.Color;
import java.awt.Graphics;
import java.util.concurrent.LinkedBlockingDeque;

import javax.swing.JPanel;

import com.suppresswarnings.osgi.data.TTL;

public class Vision extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1489893999445184662L;
	Content content;
	int n = 0;
	public Vision(Content ctx) {
		super();
		this.content = ctx;
	}
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int i = 80;
		int j = 50;
		n = 0;
		LinkedBlockingDeque<TTL> ttl = content.ttl;
		ttl.stream().forEach(e -> {
			long now = System.currentTimeMillis();
			long left = e.ttl() - now;
			String key;
			Color color = g.getColor();
			if(left < 0) {
				g.setColor(Color.RED);
				key = "########";
			} else {
				key = "" + left;
			}

			g.drawRect(i, j + n * 50, 80, 30);
			g.drawString(e.key(), i + 5, j + 10 + n * 50);
			g.drawString(key, i + 5, j + 25 + n * 50);
			g.setColor(color);
			n ++;
		});
	}
}
