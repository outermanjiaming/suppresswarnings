/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.corpus.common;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamListener;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;

public class Eye {
	AtomicBoolean shot = new AtomicBoolean(true);
	Brain brain;
	public Eye(Brain brain) {
		this.brain = brain;
		this.prepare();
	}
	public void prepare(){
		Webcam webcam = Webcam.getDefault();
		webcam.setViewSize(WebcamResolution.VGA.getSize());
		webcam.addWebcamListener(new WebcamListener() {
			
			@Override
			public void webcamOpen(WebcamEvent we) {
				System.out.println("Open webcam");
			}
			
			@Override
			public void webcamImageObtained(WebcamEvent we) {
				if(shot.get()) {
					shot.set(false);
					BufferedImage image = we.getImage();
					if(brain != null) {
						brain.see(image);
					}
					System.out.println("========================== ");
				}
			}
			
			@Override
			public void webcamDisposed(WebcamEvent we) {
				System.out.println("Dispose webcam");
			}
			
			@Override
			public void webcamClosed(WebcamEvent we) {
				System.out.println("Close webcam");
			}
		});
		
		WebcamPanel panel = new WebcamPanel(webcam);
		panel.setFPSDisplayed(false);
		panel.setDisplayDebugInfo(false);
		panel.setImageSizeDisplayed(false);
		panel.setMirrored(true);
		panel.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				shot.set(true);
			}
		});
		
		JFrame window = new JFrame("Information");
		window.add(panel);
		window.setResizable(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setVisible(true);
	}
	
	public void see() {
		shot.set(true);
	}
	
		public static void main(String[] args) throws InterruptedException {
			
			Eye eye = new Eye(null);
			eye.prepare();
			
		}
	}
