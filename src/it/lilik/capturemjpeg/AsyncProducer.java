/*
 *   
 */
package it.lilik.capturemjpeg;

import it.lilik.capturemjpeg.utils.CircularBuffer;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
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
	private static final int BYTES_TO_READ = 128;
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
	 * It changes the URI.<br>
	 * A new connection will be performed after a complete
	 * image reading.
	 * @param method the method to set
	 */
	public void setMethod(HttpMethod method) {
		synchronized (this.method) {
			this.method = method;
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
		setMethod(this.method);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		// TODO Auto-generated method stub
		while (!this.shouldStop) {
			BufferedInputStream is = null;
			InputStream responseBody = null;
			String boundary;

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
					Header contentType = this.method.getRequestHeader("content-type");
					String contentTypeS = contentType.toString();
					int startIndex = contentTypeS.indexOf("boundary=");
					int endIndex = contentTypeS.indexOf(';', startIndex);
					boundary = contentTypeS.substring(startIndex + 9, endIndex);
				}
				/* Now flip flop to parse the images. We look for the JPEG magic MIME identifier,
				 * which is composed by the first two bites set to 0xff and 0xd8. We stop when we find
				 * another "--$boundary" string.
				 */
				
				try {
					byte tmp[] = new byte[BYTES_TO_READ];
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					char delimiter[] = ("--" + boundary).toCharArray();
					byte magicMIME[] = {(byte) 0xff,
					                    (byte) 0xd8};
					byte partialMatch[] = new byte[delimiter.length];
					int bytesRead = 0;
					int last = 0;
					boolean foundStart = false;
					
					/* This first cycle ensures we start parsing the stream on the first
					 * magic MIME identifier.
					 */
					/*while (true) {
						int data;
						data = is.read();
						
						if ((byte) data == magicMIME[0]) {
							foundStart = true;
							is.mark(2);
							continue;
						}
						
						if (foundStart && ((byte) data) == magicMIME[1]) {
							is.reset();
							break;
						}
	
						if (foundStart)
							foundStart = false;
					} */
					/* This cycle splits the stream into several ByteArrayInputStreams containing
					 * the real image, and pushs them to our internal buffer.
					 */
					int startIdx = 0;
					last = 0;
					boolean lookinForMagicMIME = true;
					while ((bytesRead = is.read(tmp)) != -1) {
						for (int i = 0; i < bytesRead; i++) {
							if (lookinForMagicMIME) {
								if (tmp[i] == magicMIME[last]) {
									partialMatch[last] = tmp[i];
									last++;
									
									if (last == 2) {
										os.write(partialMatch, 0, 2);
										last = 0;
										lookinForMagicMIME = false;
									}
								} else {
									last = 0;
								}
							}else { //lookinForBoundary
								if (tmp[i] == delimiter[last]) {
									partialMatch[last] = tmp[i];
									last++;
									
									if (last == delimiter.length) {
										this.buffer.push(new ByteArrayInputStream(os.toByteArray()));
										lookinForMagicMIME = true;
										last = 0;
									}
								} else if (last > 0) {
									partialMatch[last++] = tmp[i];
									os.write(partialMatch, 0 , last);
									last = 0;
								} else {
									os.write(tmp[i]);
								}
							}
							
						}

					} 
				}catch (IOException e) {
					// TODO: handle exception
				}
				
				
				
			}
		}

	}

	/**
	 * Dummy main for testing purpose
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
