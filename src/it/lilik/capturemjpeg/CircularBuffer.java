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

import java.io.ByteArrayInputStream;

/**
 * A circular buffer of {@link java.io.ByteArrayInputStream}
 * 
 * @author Alessio Caiazza
 * @author Cosimo Cecchi
 *
 */
class CircularBuffer {
	
	/** data storage */
	private ByteArrayInputStream buffer[];
	/** last item in <code>CircularBuffer</code> */
	private int lastIdx;
	/** first valid item in <code>CircularBuffer</code> */
	private int firstIdx;
	/** size of <code>CircularBuffer</code> */
	private int size;
	
	private boolean empty;

	/** default size */
	static final int DEFAULT_SIZE = 5;

	/**
	 * Creates a new <code>CircularBuffer</code> with
     * the specified size. 
	 * @param size the size of the buffer.
	 */
	public CircularBuffer(int size) {
		this.size = size;
		this.clear();
	}
	
	/**
	 * Creates a <code>CircularBuffer</code> with
     *  internel buffer of 
     * {@link it.lilik.capturemjpeg.CircularBuffer#DEFAULT_SIZE}
	 */
	public CircularBuffer() {
		this(CircularBuffer.DEFAULT_SIZE);
	}
	
	/**
	 * Adds a an element to the <code>CircularBuffer</code>
	 * 
	 * @param data the new element
	 */
	public synchronized void push(ByteArrayInputStream data) {
		buffer[this.lastIdx] = data;
		if( this.lastIdx == this.firstIdx && !this.empty) {
			//we have overwritten an element
			this.firstIdx++;
			this.firstIdx %= this.size;
		} 
		if (this.empty)
			this.empty = false;
		this.lastIdx++;
		this.lastIdx %= this.size; 
	}
	
	/**
	 * Returns the  number of elements into the buffer 
	 * @return the number of valid elements
	 */
	public synchronized int getLength() {
		if (this.empty)
			return 0;
		int len = this.lastIdx - this.firstIdx;
		if (len < 0)
			len = this.size + len;
		return len == 0 ? this.size-1 : len;
	}
	

	/**
	 * Gets the first available element.
	 * 
	 * @return the first element
	 * @throws IndexOutOfBoundsExceptions
	 */
	public synchronized ByteArrayInputStream pop() {
		if (isEmpty()) {
			throw new IndexOutOfBoundsException("Empty buffer");
		}
		ByteArrayInputStream res = buffer[this.firstIdx];
		buffer[this.firstIdx] = null;
		this.firstIdx++;
		this.firstIdx %= this.size;
		if (this.firstIdx == this.lastIdx)
			this.empty = true;
		return res;
	}
	
	/**
	 * Returns the status of the buffer.
	 * @return <code>true</code> if empty, otherwise <code>false</code>
	 */
	public synchronized boolean isEmpty() {
		return this.empty;
	}
	
	/**
	 * Clears the contents of the buffer.
	 */
	public void clear () {
		this.firstIdx = 0;
		this.lastIdx = 0;
		this.empty = true;
		this.buffer = new ByteArrayInputStream[size];
	}

	/**
     * Returns the size of the buffer.
     *
     * @return the size of the buffer.
     */ 
	public int getSize() {
		return this.size;
	}
}
