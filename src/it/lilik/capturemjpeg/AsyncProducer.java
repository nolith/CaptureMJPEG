/*
 *   
 */
package it.lilik.capturemjpeg;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

/**
 * This class produces JPEG images in <code>ByteArrayInputStream</code>
 * getting them from a MJPEG stream.
 * 
 * @author Alessio Caiazza 
 * @author Cosimo Cecchi
 *
 */
class AsyncProducer extends Thread {

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
	
	
	
	/**
	 * @return the shouldStop
	 */
	public boolean getShouldStop() {
		return shouldStop;
	}

	/**
	 * It stops this thread when the current image if finished
	 * @param shouldStop the shouldStop to set
	 */
	public void setShouldStop(boolean shouldStop) {
		this.shouldStop = shouldStop;
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.isChangePending = true;
			this.method.setFollowRedirects(true);
		}
	}
	
	private void setCredential(String username, String password) {
		UsernamePasswordCredentials creds = 
			new UsernamePasswordCredentials(username, password);
		client.getState().setCredentials(AuthScope.ANY, creds);
	}

	/**
	 * @param method
	 */
	public AsyncProducer(HttpMethod method) {
		this(method, null, null);
	}

	/**
	 * This constructor provides support for HTTP AUTH
	 * 
	 * @param method the MJPEG stream URI
	 * @param username HTTP AUTH username
	 * @param password HTTP AUTH password
	 */
	public AsyncProducer(HttpMethod method, String username, String password) {
		this.method = method;
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
		
		//TODO: check if correct
		setURL(this.method.getPath());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		BufferedInputStream is = null;
		InputStream responseBody = null;
		MJPEGInputStream mis = null;
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
					
					mis = new MJPEGInputStream(is, boundary);
					this.isChangePending = false;
				}	//end syncronyzed			
			} //end if(isChangePending)
			
			synchronized (this.method) {
				try {
					this.buffer.push(new ByteArrayInputStream(mis.readImage()));
				} catch (IOException e) {
					e.printStackTrace();
				}
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

}
