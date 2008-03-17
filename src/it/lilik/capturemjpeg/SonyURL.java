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
    
    Copyright (c) 2008 - Alessio Caiazza, Cosimo Cecchi
 */
package it.lilik.capturemjpeg;

/**
 * This class can handle URL generation for a specific host (Sony camera).<br/>
 * 
 * Use the {@link it.lilik.capturemjpeg.SonyURL#getURL()} method to obtain a valid
 * url string.
 * @author Alessio Caiazza, Cosimo Cecchi
 *
 */
public class SonyURL {
	
	/** the hostname or ip **/
	private String host;
	/** the desired framerate **/
	private Integer speed;
	
	/**
	 * Creates a <code>SonyBuilder</code> for the specified <code>host</code><br/>
	 * 
	 * <code>host</code> must be an IP or an hostname, 
	 * without http:// or any trailing slash.
	 *    
	 * @param host the hostname or ip
	 */
	public SonyURL(String host) {
		this.host = host;
		speed = null;
	}
	
	/**
	 * Transforms the <code>SonyURL</code> object in a correct URL string
	 * @return the URL in string form
	 */
	public String getURL() {
		String res = "http://" + host + "/image";
		
		if(speed != null)
			return res + "?speed=" + speed;
		
		return res;
	}
	
	/**
	 * Sets the framerate.<br/>
	 * This is encoded as <code>speed</code> parameter into the url.
	 * @param speed the framerate
	 * @return a reference to <code>this</code>
	 */
	public SonyURL setFPS(int speed) {
		this.speed = new Integer(speed);
		
		return this;
	}
}
