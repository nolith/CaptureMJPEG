/*
 *   
 */
package it.lilik.capturemjpeg;

import java.io.ByteArrayInputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
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
class AsyncProducer implements Runnable {

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
	protected ByteArrayInputStream buffer[];
	
	
	
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
	 * It changes the URI.<br>
	 * A new connection will be performed after a complete
	 * image reading.
	 * @param method the method to set
	 */
	public void setMethod(HttpMethod method) {
		this.method = method;
		this.isChangePending = true;
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
		this.isChangePending = true;
		this.shouldStop = false;
		// create a singular HttpClient object
		this.client = new HttpClient();

		// establish a connection within 5 seconds
		this.client.getHttpConnectionManager().getParams()
				.setConnectionTimeout(HTTP_TIMEOUT);

		if (username != null && password != null ) {
			this.setCredential(username, password);
		}
 
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		// TODO Auto-generated method stub

	}

	/**
	 * Dummy main for testing purpose
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
