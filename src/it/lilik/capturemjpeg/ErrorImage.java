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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * This class builds a {@link java.awt.image.BufferedImage} from an error string.
 * The image built this way will be black with the error in white painted over it.
 * 
 * @author Alessio Caiazza
 * @author Cosimo Cecchi
 *
 */
class ErrorImage extends BufferedImage {
	
	/**
	 * Builds a {@link java.awt.image.BufferedImage} from the given error string
	 * <code>err</code>.
	 * 
	 * @param err the string message to be printed over the image.
	 */

	public ErrorImage (String err) {
		//creates the BufferedImage
		super (300, 300, BufferedImage.TYPE_INT_RGB);
		//get the drawing area
		Graphics2D graph = this.createGraphics();
		
		//split the error string on spaces.
		String splitted[] = err.split("\\s");
		
		graph.setColor (Color.white);
		
		int line = 1;
		for (int i = 0; i < splitted.length; i++) {
			//we reassemble the string with no more than 45 chars/line
			StringBuffer str = new StringBuffer(splitted[i]);
			while((i+1 < splitted.length) &&
					(str.length() + splitted[i+1].length()) < 45 ) {
				i++;
				str.append(" " + splitted[i]);
			}
			graph.drawString(str.toString(), 10, 20*(line++));
			
		}	
	}
	
	/**
	 * Gets the {@link java.io.ByteArrayInputStream} containing the image.
	 * 
	 * @return a {@link java.io.ByteArrayInputStream}
	 */
	
	public ByteArrayInputStream getAsInputStream () {
		ByteArrayOutputStream bos = new ByteArrayOutputStream ();
		try {
			ImageIO.write(this, "jpg", bos);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new ByteArrayInputStream (bos.toByteArray());
	}
}
