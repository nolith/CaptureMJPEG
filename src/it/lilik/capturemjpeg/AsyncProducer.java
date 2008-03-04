/**
 * 
 */
package it.lilik.capturemjpeg;

import java.io.ByteArrayInputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

/**
 * @author nolith
 *
 */
class AsyncProducer implements Runnable {

	
	protected static final int HTTP_TIMEOUT = 5000;
	protected HttpClient client;
	protected HttpMethod method;
	protected boolean shouldStop;
	protected boolean isChangePending;
	protected ByteArrayInputStream buffer[];
	
	
	
	/**
	 * @return the shouldStop
	 */
	public boolean getShouldStop() {
		return shouldStop;
	}

	/**
	 * @param shouldStop the shouldStop to set
	 */
	public void setShouldStop(boolean shouldStop) {
		this.shouldStop = shouldStop;
	}

	/**
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
	 * @param method
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
	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
