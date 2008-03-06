/**
 * 
 */
package it.lilik.capturemjpeg.utils;

import java.io.ByteArrayInputStream;

/**
 * A circular buffer of {@link java.io.ByteArrayInputStream}
 * 
 * @author Alessio Caiazza
 * @author Cosimo Cecchi
 *
 */
public class CircularBuffer {
	
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
		this.firstIdx = 0;
		this.lastIdx = 0;
		this.empty = true;
		this.buffer = new ByteArrayInputStream[size];
	}
	
	/**
	 * Creates a <code>CircularBuffer</code> with
     *  internel buffer of 
     * {@link it.lilik.capturemjpeg.utils.CircularBuffer#DEFAULT_SIZE}
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
			//TODO:change Exception type
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
     * Returns the size of the buffer.
     *
     * @return the size of the buffer.
     */ 
	public int getSize() {
		return this.size;
	}
}
