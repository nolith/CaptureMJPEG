/**
 * 
 */
package it.lilik.capturemjpeg;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Method;

import javax.imageio.ImageIO;

import org.apache.commons.httpclient.methods.GetMethod;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * @author Alessio Caiazza
 * @author Cosimo Cecchi
 *
 */
public class CaptureMJPEG_factory implements Runnable {

	private PApplet parent;
	private AsyncProducer client;
	private Method captureEventMethod;
	private Thread runner;
	private boolean shouldStop;
	private PImage lastImage;
	
	
	
	
	/**
	 * @param parent
	 */
	public CaptureMJPEG_factory(PApplet parent,
								String url,
								String user,
								String password) {
		lastImage = null;
		shouldStop = false;
		this.parent = parent;
		client = new AsyncProducer(new GetMethod(url), user, password);
		
	    parent.registerDispose(this);
	    
	    //register callback
	    try {
	        captureEventMethod =
	          parent.getClass().getMethod("captureMJPEGEvent",
	                            new Class[] { PImage.class });
	    } catch (Exception e) {
	        // no such method, or an error.. which is fine, just ignore
	    }
	    
		runner = new Thread(this);
		runner.setName("CaptureMJPEG_factory");
	}

	
	public void startCapture() {
		shouldStop = false;
		client.start();
		runner.start();
	}
	
	public void stopCapture() {
		client.setShouldStop(true);
		shouldStop = true;
	}
	
	public boolean isImageAvailable() {
		return client.isImageAvailable();
	}
	
	/**
	 * Provide the last <code>PImage</code> available.
	 * @return the last <code>PImage</code> or <code>null</code> if no image are available
	 */
	public PImage getImage() {
		if (!isImageAvailable()) {
			System.out.println("Nulla ciccio!");
			return lastImage;
		}
		try {
			return lastImage = new PImage((BufferedImage)ImageIO
					.read(client.pop()));
		} catch (IOException e1) {
			e1.printStackTrace();
			return lastImage;
		}
	}
	/**
	 * Callback for processing
	 */
	public void dispose() {
		this.stopCapture();
		try {
			client.join();
			runner.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while(!shouldStop) {
			
			while( !client.isImageAvailable() ) {
				Thread.yield();
			}
			
			BufferedImage img = null;
			try {
				img = (BufferedImage)ImageIO.read (client.pop());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			if (captureEventMethod != null) {
	            try {
	              captureEventMethod.invoke(parent, 
	            		  new Object[] { new PImage(img) });
	            } catch (Exception e) {
	              System.err.println("Disabling captureEvent() for " + 
	            		  			 parent.getName() +
	                                 " because of an error.");
	              e.printStackTrace();
	              captureEventMethod = null;
	            }
	        }
		}
	}

}
