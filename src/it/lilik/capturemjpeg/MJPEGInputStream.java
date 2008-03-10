/**
 * 
 */
package it.lilik.capturemjpeg;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * @author Alessio Caiazza
 * @author Cosimo Cecchi
 *
 */
public class MJPEGInputStream extends FilterInputStream {

	private static final int BYTES_TO_READ = 256;
	private String boundary;	
	private Object lock;
	
	/**
	 * @param arg0
	 */
	public MJPEGInputStream(InputStream arg0, String boundary, Object lock) {
		super(arg0);
		this.boundary = boundary;
		this.lock = lock;
	}
	
	public byte[] readImage() throws IOException {
		synchronized (lock) {
			byte tmp[] = new byte[BYTES_TO_READ];
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			char delimiter[] = (boundary).toCharArray();
			byte magicMIME[] = {(byte) 0xff,
			                    (byte) 0xd8};
			byte partialMatch[] = new byte[delimiter.length];
			int bytesRead = 0;
			int last = 0;
			
			/* This cycle splits the stream into several ByteArrayInputStreams containing
			 * the real image, and pushes them to our internal buffer.
			 */
			last = 0;
			boolean lookinForMagicMIME = true;
			while ((bytesRead = read(tmp)) != -1) {
				
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
						        return os.toByteArray();
							}
						} else if (last > 0) {
							partialMatch[last++] = tmp[i]; 
							os.write(partialMatch, 0 , last);
							last = 0;
						} else {
							os.write(tmp[i]);
						}
					}
					
				} //end of for
	
			} // end of while
			throw new IOException("No image available");
		}
	}

	/**
	 * Block until reading is finished
	 */
	public void waitReading() {
		synchronized (lock) {
			return;
		}
	}
}
