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

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * This class provides the method <code>byte[] readImage()</code> which returns
 * a byte array containing a complete JPEG image extracted from the {@link java.io.InputStream}
 * provided, which should be a MotionJPEG stream.
 * @author Alessio Caiazza
 * @author Cosimo Cecchi
 *
 */
public class MJPEGInputStream extends FilterInputStream {

	private static final int BYTES_TO_READ = 256;
	private String boundary;	
	
	/**
	 * Creates a MotionJPEG parser from <code>arg0<code> {@link java.io.InputStream}
	 * @param arg0 
	 * @param boundary the boundary string delimiting every images
	 */
	public MJPEGInputStream(InputStream arg0, String boundary) {
		super(arg0);
		this.boundary = boundary;
	}
	
	/**
	 * Returns a byte array containing the next JPEG image.
	 * 
	 * @return a byte array containing the next JPEG image
	 * @throws IOException if no image is available
	 */
	public byte[] readImage() throws IOException {
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
