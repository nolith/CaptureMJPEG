/**
 * 
 */
package it.lilik.capturemjpeg.utils;

import java.io.ByteArrayInputStream;

/**
 * A circular buffer of <code>ByteArrayInputStream</code>
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
	 * @param size
	 */
	public CircularBuffer(int size) {
		this.size = size;
		this.firstIdx = 0;
		this.lastIdx = 0;
		this.empty = true;
		this.buffer = new ByteArrayInputStream[size];
	}
	
	/**
	 * 
	 */
	public CircularBuffer() {
		this(CircularBuffer.DEFAULT_SIZE);
	}

	/**
	 * @return the lastIdx
	 */
	public synchronized int getLastIdx() {
		return lastIdx;
	}

	/**
	 * @return the firstIdx
	 */
	public synchronized int getFirstIdx() {
		return firstIdx;
	}	
	
	/**
	 * Add a an element to the <code>CircularBuffer</code>
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
	 * It counts elements in buffer 
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
	 * Get the first available element.
	 * 
	 * @return the first element
	 * @throws IndexOutOfVoundsExceptions
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
	 * 
	 * @return the status of the buffer
	 */
	public synchronized boolean isEmpty() {
		return this.empty;
	}

	public int getSize() {
		return this.size;
	}
}
