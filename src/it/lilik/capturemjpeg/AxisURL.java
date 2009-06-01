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

import java.awt.Dimension;

/**
 * This class can handle URL generation for a specific host (Axis camera).<br/>
 * 
 * Use the {@link it.lilik.capturemjpeg.AxisURL#getURL()} method to obtain a valid
 * url string. See <a href="http://www.axis.com/techsup/cam_servers/dev/cam_http_api.php#sw_requirements_image_requests_mjpg"/>
 * for a description of all the accepted parameters.
 * @author Alessio Caiazza, Cosimo Cecchi
 *
 */
public class AxisURL {
	
	/** the hostname or ip **/
	private String host;
	/** the desired framerate **/
	private Integer desiredFPS;
	/** the required framerate **/
	private Integer requiredFPS;
	/** the desired resolution **/
	private Dimension resolution;
	/** the number of the camera we want to watch **/
	private Integer camera;
	/** sets the desired level of color/grayscale **/
	private Integer colorLevel;
	/** whether to show/hide the clock **/
	private Boolean showClock;
	/** whether to show/hide the date **/
	private Boolean showDate;
	/** whether the camera should generate a quad image **/
	private Boolean quad;
	/** whether we should show the text **/
	private Boolean showText;
	/** rotation to be applied to the image (clockwise) **/
	private Integer rotation;
	
	/**
	 * Creates a <code>AxisURL</code> for the specified <code>host</code><br/>
	 * 
	 * <code>host</code> must be an IP or an hostname, 
	 * without http:// or any trailing slash.
	 *    
	 * @param host the hostname or ip
	 */
	public AxisURL(String host) {
		this.host = host;
		desiredFPS = null;
		requiredFPS = null;
		resolution = null;
		camera = null;
		colorLevel = null;
		showClock = null;
		showDate = null;
		quad = null;
		showText = null;
		rotation = null;
	}
	
	/**
	 * Transforms the <code>AxisURL</code> object in a correct URL string
	 * @return the URL in string form
	 */
	public String getURL() {
		StringBuffer res = new StringBuffer("http://" + host + "/axis-cgi/mjpg/video.cgi?");
		
		if (desiredFPS != null)
			res.append("des_fps=" + desiredFPS + "&");
		if (requiredFPS != null)
			res.append("req_fps=" + requiredFPS + "&");
		if (resolution != null)
			res.append("resolution=" + resolution.width + "x" + resolution.height + "&");
		if (camera != null)
			res.append("camera=" + camera + "&");
		if (colorLevel != null)
			res.append("colorlevel=" + colorLevel+ "&");
		if (showClock != null)
			res.append("clock=" + (showClock.booleanValue() ? "1" : "0") + "&");
		if (showDate != null)
			res.append("date=" + (showDate.booleanValue() ? "1" : "0") + "&");
		if (quad != null)
			res.append("quad=" + (quad.booleanValue() ? "1" : "0") + "&");
		if (showText != null)
			res.append("clock=" + (showText.booleanValue() ? "1" : "0") + "&");
		if (rotation != null)
			res.append("rotation=" + rotation + "&");
		
		return res.deleteCharAt(res.length()-1).toString();
	}
	
	/**
	 * Sets the desired framerate.<br/>
	 * This is encoded as <code>des_fps</code> parameter into the url.
	 * @param desiredFPS the framerate
	 * @return a reference to <code>this</code>
	 */

	public AxisURL setDesiredFPS(int desiredFPS) {
		this.desiredFPS = new Integer (desiredFPS);
		
		return this;
	}

	/**
	 * Sets the required framerate.<br/>
	 * This is encoded as <code>req_fps</code> parameter into the url.
	 * @param requiredFPS the framerate
	 * @return a reference to <code>this</code>
	 */
	public AxisURL setRequiredFPS(int requiredFPS) {
		this.requiredFPS = new Integer(requiredFPS);
		
		return this;
	}
	
	/**
	 * Sets the desired resolution.<br/>
	 * This is encoded as <code>resolution</code> parameter into the url.
	 * @param width desired width of the image
	 * @param height desired height of the image
	 * @return a reference to <code>this</code>
	 */
	public AxisURL setResolution(int width, int height) {
		this.resolution = new Dimension (width, height);
		
		return this;
	}
	
	/**
	 * Selects the source camera (valid for video servers with more than a camera).<br/>
	 * This is encoded as <code>camera</code> parameter into the url.
	 * @param camera the number of the camera to be shown.
	 * @return a reference to <code>this</code>
	 */
	public AxisURL setCamera(int camera) {
		this.camera = new Integer(camera);
		
		return this;
	}
	
	/**
	 * Sets the desired level of color/grayscale.
	 * <br/> (0 = grayscale, 100 = full color).<br/>
	 * This is encoded as <code>colorlevel</code> parameter into the url.
	 * <p>If you pass a colorlevel out of the bounds it will be automatically adjusted
	 * to the minimum or the maximum accordingly.
	 * @param colorLevel the desired color level
	 * @return a reference to <code>this</code>
	 */
	public AxisURL setColorLevel(int colorLevel) {
		if (colorLevel < 0)
			colorLevel = 0;
		if (colorLevel > 100)
			colorLevel = 100;
		this.colorLevel = new Integer(colorLevel);
		
		return this;
	}
	
	/**
	 * Show/hides the timestamp.<br/>
	 * This is encoded as <code>clock</code> parameter into the url.
	 * @param showClock whether to show the timestamp.
	 * @return a reference to <code>this</code>
	 */
	public AxisURL setShowClock(boolean showClock) {
		this.showClock = new Boolean(showClock);
		
		return this;
	}

	/**
	 * Show/hides the date.<br/>
	 * This is encoded as <code>date</code> parameter into the url.
	 * @param showDate whether to show the date.
	 * @return a reference to <code>this</code>
	 */
	public AxisURL setShowDate(boolean showDate) {
		this.showDate = new Boolean (showDate);
		
		return this;
	}
	
	/**
	 * Generates a quad image.<br/>
	 * This is encoded as <code>quad</code> parameter into the url.
	 * @param quad whether to show a quad image.
	 * @return a reference to <code>this</code>
	 */
	public AxisURL setQuad(boolean quad) {
		this.quad = new Boolean (quad);
		
		return this;
	}

	/**
	 * Show/hides the text.<br/>
	 * This is encoded as <code>text</code> parameter into the url.
	 * @param showText whether to show the text.
	 * @return a reference to <code>this</code>
	 */
	public AxisURL setShowText(boolean showText) {
		this.showText = new Boolean (showText);
		
		return this;
	}

	/**
	 * Rotates the image clockwise (0, 90, 180, 270).<br/>
	 * This is encoded as <code>rotation</code> parameter into the url.
	 * <p>If the provided rotation parameter is not valid, it will be rounded
	 * to the nearest valid one.
	 * @param rotation angle of rotation to be applied.
	 * @return a reference to <code>this</code>
	 */
	public AxisURL setRotation(int rotation) {
		if (rotation < 45)
			rotation = 0;
		else if (rotation < 135)
			rotation = 90;
		else if (rotation < 225)
			rotation = 180;
		else
			rotation = 270;
		this.rotation = new Integer(rotation);
		
		return this;
	}
	
}
