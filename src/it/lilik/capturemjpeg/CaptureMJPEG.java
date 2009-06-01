/*
	This file is part of CaptureMJPEG.

    CaptureMJPEG is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CaptureMJPEG is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser Public License for more details.

    You should have received a copy of the GNU Lesser Public License
    along with CaptureMJPEG.  If not, see <http://www.gnu.org/licenses/>.
    
    Copyright (c) 2008-09 - Alessio Caiazza, Cosimo Cecchi
 */
package it.lilik.capturemjpeg;

import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import javax.imageio.ImageIO;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * This class produces JPEG images from Motion JPEG stream.<br/>
 * It searches for a callback function called <code>void captureMJPEGEvent(PImage img)</code>
 * into the parent {@link processing.core.PApplet}<br/>
 * <p>
 * 
 * <b>Example</b><br/>
 * <pre>
import it.lilik.capturemjpeg.*;

private CaptureMJPEG capture;
private PImage next_img = null;

void setup() {
  size(400, 300);
  background(0);
  capture = new CaptureMJPEG(this,
			"http://mynetworkcamera/image?speed=20",
			"admin",
			"password");
  capture.startCapture();
  frameRate(20);
}
	
void draw() {
  if (next_img != null) {
    image(next_img, 0, 0);
  }
}
	
void captureMJPEGEvent(PImage img) {
  next_img = img;
}
</pre>
 *
 *
 * @author Alessio Caiazza 
 * @author Cosimo Cecchi
 *
 */
public class CaptureMJPEG extends Thread {

	/** timeout for HTTP request    */
	private static final int HTTP_TIMEOUT = 5000;
	/** client */
	private HttpClient client;
	/** method */
	private HttpMethod method;
	/** used for stopping this thread */
	private boolean shouldStop;
	/** <code>true</code> if there are pending changes in <code>method</code> */
	private boolean isChangePending;
	/** circular buffer for images */
	private CircularBuffer buffer;
	
	/** the parent <code>PApplet</code> **/
	private PApplet parent;
	/** callback method **/
	private Method captureEventMethod;
	/** Last processed image **/
	private PImage lastImage;
	
	/** adaptive PApplet size **/
	private boolean adaptFrameSize;
	
	private boolean changeFrameSize;
	
	
	/**
	 * Checks if the running thread is in stopping state.
	 * If <code>true</code> the thread is stopped or it will
	 * stop after the next image was captured. 
	 * 
	 * @return the internal status
	 */
	public boolean isStopping() {
		return shouldStop;
	}

	/**
	 * Starts the capture cycle.
	 */
	public void startCapture() {
		this.start();
	}
	/**
	 * Stops this thread when the current image if finished
	 * 
	 */
	public void stopCapture() {
		this.shouldStop = true;
	}

	/**
	 * Changes the URI.<br>
	 * A new connection will be performed after a complete
	 * image reading.
	 * @param url the url of the MJPEG stream
	 * 
	 */
	public void setURL(String url) {
		synchronized (this.method) {
			this.method.releaseConnection();
			try {
				this.method.setURI(new URI(url, false));
			} catch (URIException e) {
				e.printStackTrace();
			}
			this.isChangePending = true;
			this.method.setFollowRedirects(true);
		}
	}
	
	/**
	 * Sets username and password for HTTP Auth.
	 * 
	 * @param username the username
	 * @param password the password
	 */
	public void setCredential(String username, String password) {
		UsernamePasswordCredentials creds = 
			new UsernamePasswordCredentials(username, password);
		client.getState().setCredentials(AuthScope.ANY, creds);
	}

	/**
	 * Creates a <code>CaptureMJPEG</code> without HTTP Auth credential
	 */
	public CaptureMJPEG(PApplet parent, String url) {
		this(parent, url, null, null);
	}
	
	/**
	 * Creates a <code>CaptureMJPEG</code> with HTTP Auth credential
	 * 
	 * @param parent the <code>PApplet</code> which uses this object
	 * @param url the MJPEG stream URI
	 * @param username HTTP AUTH username
	 * @param password HTTP AUTH password
	 */
	public CaptureMJPEG(PApplet parent,
			String url, String username, String password) {
		this.lastImage = new PImage();
		this.lastImage.init(parent.width, parent.height, PImage.RGB);
		this.method = new GetMethod(url);
		this.shouldStop = false;
		this.adaptFrameSize = false;
		this.changeFrameSize = false;
		buffer = new CircularBuffer();
		// create a singular HttpClient object
		this.client = new HttpClient();

		// establish a connection within 5 seconds
		this.client.getHttpConnectionManager().getParams()
				.setConnectionTimeout(HTTP_TIMEOUT);

		if (username != null && password != null ) {
			this.setCredential(username, password);
		}
		setURL(url);
		//set the ThreadName useful for debug
		setName("CaptureMJPEG");
		setPriority(Thread.MAX_PRIORITY);
		
		this.parent = parent;

	    parent.registerDispose(this);	    
	    //register callback
	    try {
	        captureEventMethod =
	          parent.getClass().getMethod("captureMJPEGEvent",
	                            new Class[] { PImage.class });
	    } catch (Exception e) {
	        // no such method, or an error...which is fine, just ignore.
	    }
	}
	
	
	
	/**
	 * Sets the adaptive frame size behavior.
	 * @see it.lilik.capturemjpeg.CaptureMJPEG#setAdaptFrameSize
	 * @return the adaptFrameSize
	 */
	public boolean isAdaptFrameSize() {
		return adaptFrameSize;
	}

	/**
	 * If <code>true</code> when a stream is initialized
	 * the <code>parent</code> size is set to the image size.
	 * 
	 * @param adaptFrameSize the adaptFrameSize to set
	 */
	public void setAdaptFrameSize(boolean adaptFrameSize) {
		this.adaptFrameSize = adaptFrameSize;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		BufferedInputStream is = null;
		InputStream responseBody = null;
		String boundary = "";
		
		while (!this.shouldStop) {

			if (this.isChangePending) {
				synchronized (this.method) {
					// establish the connection
					try {
						this.client.executeMethod(this.method);
						responseBody = this.method.getResponseBodyAsStream();
						if (this.method.getStatusCode() == 404) {
							ErrorImage error = new ErrorImage ("Unable to find '" +
									this.method.getPath() + "' on host '" +
									this.method.getURI().getHost() + "'");
							setErrorImage (error);
							continue;
						}
						is = new BufferedInputStream (responseBody);
					} catch (Exception e) {
						ErrorImage error;
						try {
							error = new ErrorImage ("Unable to connect to '" +
									this.method.getURI().getHost() + "' (" +
									e.getLocalizedMessage() + ")");
							setErrorImage (error);
						} catch (URIException e1) {
							e1.printStackTrace();
						}

						
						continue;
					}
					
					// automagically guess the boundary
					Header contentType = this.method.getResponseHeader("Content-Type");
					String contentTypeS = contentType.toString();
					int startIndex = contentTypeS.indexOf("boundary=");
					int endIndex = contentTypeS.indexOf(';', startIndex);
					if (endIndex == -1) {//boundary is the last option
						/* some servers, like mjpeg-streamer puts
						 * a '\r' character at the end of each line.
						 */
						if((endIndex = contentTypeS.indexOf('\r',
								startIndex)) == -1)  
							endIndex = contentTypeS.length();
					}
					boundary = contentTypeS.substring(startIndex + 9, endIndex);
					//some cameras put -- on boundary, some not
					if (boundary.charAt(0) != '-' && 
							boundary.charAt(1) != '-')
						boundary = "--" + boundary;
					
					synchronized (lastImage) {
						this.buffer.clear();
						this.isChangePending = false;
						if(this.adaptFrameSize)
							this.changeFrameSize = true;
					}
				}	//end synchronized			
			} //end if(isChangePending)

			byte[] img;
			MJPEGInputStream mis = new MJPEGInputStream(is, boundary);
			try {
				synchronized (method) {
					img = mis.readImage();
				}
				synchronized (lastImage) {
					if (captureEventMethod != null) {
				            try {
											PImage tmp = getPImage(new ByteArrayInputStream(img));
				            	captureEventMethod.invoke(parent, new Object[] { 
				            		  this.assign(tmp) });
				            } catch (Exception e) {
				              System.err.println("Disabling captureEvent() for " + 
				            		  			 parent.getName() +
				                                 " because of an error.");
				              e.printStackTrace();
				              captureEventMethod = null;
				            }
					} else {
			        	this.buffer.push(new ByteArrayInputStream(img));
			        }
				}
			} catch (IOException e) {
				ErrorImage error = new ErrorImage (e.getLocalizedMessage());

				setErrorImage(error);
			}
		}
		
		this.method.releaseConnection();
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/*
	 * Bypass Default PImage costructur forcing
	 * a load like in Processing < 1.0
	 *
	 */
	private PImage getPImage(ByteArrayInputStream bais) throws IOException {
		PImage tmp = null;
		BufferedImage bi = (BufferedImage)ImageIO.read(bais);

		int width = bi.getWidth(null);
		int height = bi.getHeight(null);
		int[] pixels = new int[width * height];
		PixelGrabber pg =
				new PixelGrabber(bi, 0, 0, width, height, pixels, 0, width);
		try {
				pg.grabPixels();
		} catch (InterruptedException e) { }

		tmp = new PImage(width,height);
		tmp.loadPixels();
		for (int i = 0; i < width * height; i++) {
				tmp.pixels[i] = pixels[i];
		}
		tmp.updatePixels();

		return tmp;	
	}

	private void setErrorImage(ErrorImage error) {
		if (captureEventMethod != null) {
			try {
				PImage pi = new PImage (error);
		    	captureEventMethod.invoke(parent, new Object[] { 
		        		  this.assign(pi) });
			} catch (Exception exc) {
		          System.err.println("Disabling captureEvent() for " + 
				  			 parent.getName() +
		                     " because of an error.");
		          exc.printStackTrace();
		          captureEventMethod = null;
			}
		} else {
			this.buffer.push (error.getAsInputStream());
		}
	}

	/**
	 * Callback method. It's invoked by processing on stop.
	 */
	public void dispose() {
		this.stopCapture();
	}
	
	/**
	 * Provides the oldest image not yet provided.
	 * If there's no such image, provides the last provided.
	 * 
	 * @return a <code>PImage</code>
	 */
	public PImage getImage() {
		synchronized (lastImage) {
			if (!isImageAvailable())
				return lastImage;
			try {
				PImage tmp = getPImage(buffer.pop());
				return assign(tmp);
			} catch (IOException e1) {
				e1.printStackTrace();
				return lastImage;
			}
		}
	}
	
	/**
	 * Return <code>true</code> if there is at least one image available into
	 * the internal buffer.
	 * 
	 * @return the availability status
	 */
	public boolean isImageAvailable() {
		return !this.buffer.isEmpty();
	}
	
	/**
	 * Assigns <code>tmp</tmp> to <code>lastImage</code> without creating a new <code>PImage</code> 
	 * @param tmp the new <code>PImage</code>
	 * @return a reference to <code>lastImage</code>
	 */
	private PImage assign(PImage tmp) {
		if(this.changeFrameSize) {
			this.changeFrameSize = false;
			this.parent.frame.setSize(tmp.width,
					tmp.height);
		}
		if( lastImage.height != tmp.height || lastImage.width != tmp.width)
			lastImage.init(tmp.width, tmp.height, PImage.RGB);
		lastImage.copy(tmp, 0, 0, tmp.width, tmp.height,
				0, 0, lastImage.width, lastImage.height);
		return lastImage;
	}

}
