/*
 *   
 */
package it.lilik.capturemjpeg;

import it.lilik.capturemjpeg.utils.CircularBuffer;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import javax.imageio.ImageIO;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
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
 * It searchs for a callback function called <code>void captureMJPEGEvent(PImage img)</code>
 * into the parent <code>PApplet</code><br/>
 * <br/>
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
	protected static final int HTTP_TIMEOUT = 5000;
	/** client */
	protected HttpClient client;
	/** method */
	protected HttpMethod method;
	/** used for stopping this thread */
	protected boolean shouldStop;
	/** <code>true</code> if there are pending changes in <code>method</code> */
	protected boolean isChangePending;
	/** circular buffer for images */
	protected CircularBuffer buffer;
	
	/** the parent <code>PApplet</code> **/
	private PApplet parent;
	/** callback method **/
	private Method captureEventMethod;
	/** Last processed image **/
	private PImage lastImage;
	
	
	
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
			//this.method.setPath(url);
			this.method.releaseConnection();
			try {
				this.method.setURI(new URI(url, false));
			} catch (URIException e) {
				// TODO Auto-generated catch block
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
	private void setCredential(String username, String password) {
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
	        // no such method, or an error.. which is fine, just ignore
	    }
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
						is = new BufferedInputStream (responseBody);
					} catch (HttpException e) {
						System.err.println("Http error connecting to '" + this.method.getPath() + "'");
						System.err.println(e.getMessage());
						continue;
					} catch (IOException e) {
						System.err.println("Unable to connect to '" + this.method.getPath() + "'");
					}
					
					// automagically guess the boundary
					Header contentType = this.method.getResponseHeader("Content-Type");
					String contentTypeS = contentType.toString();
					int startIndex = contentTypeS.indexOf("boundary=");
					int endIndex = contentTypeS.indexOf(';', startIndex);
					if (endIndex == -1) {//boundary is the last option
						/* some servers, like mjpg-stremer puts
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
					
					this.isChangePending = false;
				}	//end synchronized			
			} //end if(isChangePending)
			
			/* Now flip flop to parse the images. We look for the JPEG magic MIME identifier,
			 * which is composed by the first two bites set to 0xff and 0xd8. We stop when we find
			 * another "--$boundary" string.
			 */
			byte[] img;
			MJPEGInputStream mis = new MJPEGInputStream(is, boundary);
			try {
				synchronized (method) {
					img = mis.readImage();
				}
				if (captureEventMethod != null) {
					synchronized (lastImage) {
			            try {
			            	PImage tmp = null;
			            	tmp = new PImage((BufferedImage)ImageIO.read(
					            			new ByteArrayInputStream(img)));
			            	captureEventMethod.invoke(parent, new Object[] { 
			            		  this.assign(tmp) });
			            } catch (Exception e) {
			              System.err.println("Disabling captureEvent() for " + 
			            		  			 parent.getName() +
			                                 " because of an error.");
			              e.printStackTrace();
			              captureEventMethod = null;
			            }
					}
		        }else {
		        	this.buffer.push(new ByteArrayInputStream(img));
		        }
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		this.method.releaseConnection();
		try {
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
				PImage tmp = new PImage(ImageIO.read(buffer.pop()));
				return assign(tmp);
			} catch (IOException e1) {
				e1.printStackTrace();
				return lastImage;
			}
		}
	}
	
	/**
	 * Return the first image available as {@link java.io.ByteArrayInputStream}.
	 * @return the first image available
	 */
	public ByteArrayInputStream pop() {
		return this.buffer.pop();
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
		if( lastImage.height != tmp.height || lastImage.width != tmp.width)
			lastImage.init(tmp.width, tmp.height, PImage.RGB);
		lastImage.copy(tmp, 0, 0, tmp.width, tmp.height,
				0, 0, lastImage.width, lastImage.height);
		return lastImage;
	}

}
