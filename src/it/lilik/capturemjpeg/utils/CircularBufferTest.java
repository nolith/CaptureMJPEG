/**
 * 
 */
package it.lilik.capturemjpeg.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * @author Alessio Caiazza
 * @author Cosimo Cecchi
 *
 */
public class CircularBufferTest extends TestCase {

	private CircularBuffer buff;
	private byte[][] data;
	private static final int DATASETS = 10;
	private static final int DATASETS_LEN = 5;
	
	/**
	 * 
	 */
	public CircularBufferTest() {
		super();
		data = new byte[DATASETS][];
		for( int i = 0; i < DATASETS; i++ ) {
			data[i] = new byte[DATASETS_LEN];
			for( int j = 0; j < DATASETS_LEN; j++ )
				data[i][j] = (byte)(j+i);
		}
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		this.buff = new CircularBuffer();
	}


	/**
	 * Test method for {@link it.lilik.capturemjpeg.utils.CircularBuffer#CircularBuffer(int)}.
	 */
	public void testCircularBufferInt() {
		CircularBuffer buff = new CircularBuffer(5);
		assertEquals(5, buff.getSize());
	}

	/**
	 * Test method for {@link it.lilik.capturemjpeg.utils.CircularBuffer#CircularBuffer()}.
	 */
	public void testCircularBuffer() {
		CircularBuffer buff = new CircularBuffer();
		assertEquals(CircularBuffer.DEFAULT_SIZE, buff.getSize());
	}

	/*
	 * Test method for {@link it.lilik.capturemjpeg.utils.CircularBuffer#getLastIdx()}.
	 *
	public void testGetLastIdx() {
		fail("Not yet implemented"); // TODO
	}*/

	/*
	 * Test method for {@link it.lilik.capturemjpeg.utils.CircularBuffer#getFirstIdx()}.
	 *
	public void testGetFirstIdx() {
		fail("Not yet implemented"); // TODO
	}*/

	/**
	 * Test method for {@link it.lilik.capturemjpeg.utils.CircularBuffer#push(java.io.ByteArrayInputStream)}.
	 */
	public void testPush() {

		assertEquals(0, buff.getLength());
		buff.push(new ByteArrayInputStream(data[0]));
		assertEquals(1,buff.getLength());
	}

	/**
	 * Test method for {@link it.lilik.capturemjpeg.utils.CircularBuffer#pop()}.
	 */
	public void testPop() {
		try {
			buff.pop();
			//should never been reached
			assertTrue("empty buffer must throw exception", false);
		}catch (IndexOutOfBoundsException e) {
			assertTrue("empty buffer must throw exception",true);
		}
		try {
			//pusp pop sequence
			for( int i = 0; i < DATASETS; i++) {
				buff.push(new ByteArrayInputStream(data[0]));
				assertEquals("Step " + i, 1, buff.getLength());
				buff.pop();
				assertTrue("Step " + i,buff.isEmpty());
			}
		}catch (IndexOutOfBoundsException e) {
			assertTrue("not empty buffer never throws exception",false);
		}
		try {
			buff.pop();
			//should never been reached
			assertTrue("empty buffer must throw exception", false);
		}catch (IndexOutOfBoundsException e) {
			assertTrue("empty buffer must throw exception",true);
		}
	}

	/**
	 * Test method for {@link it.lilik.capturemjpeg.utils.CircularBuffer#isEmpty()}.
	 */
	public void testIsEmpty() {
		assertEquals(true, buff.isEmpty());
		for( int i = 0; i < buff.getSize(); i++ ) {
			buff.push(new ByteArrayInputStream(data[0]));
			assertEquals(false, buff.isEmpty());
		}
		
	}

	/**
	 * Test method for {@link it.lilik.capturemjpeg.utils.CircularBuffer#getLength()}.
	 */
	public void testLength() {
		assertEquals(0, buff.getLength());
		for( int i = 0; i < DATASETS; i++ ) {
			buff.push(new ByteArrayInputStream(data[0]));
			if (i >= buff.getSize()-1)
				assertEquals(buff.getSize()-1, buff.getLength());
			else
				assertEquals(i+1, buff.getLength());
		}
	}
	
	/**
	 * Test method for correctness of stored data
	 */
	public void testCorrectness() {
		int len = 3;
		ByteArrayInputStream tmp;
		byte tmp_b[] = new byte[DATASETS_LEN];
		CircularBuffer buff = new CircularBuffer(len);
		buff.push(new ByteArrayInputStream(data[0]));
		tmp = buff.pop();
		try {
			tmp.read(tmp_b);
			for( int i = 0; i < DATASETS_LEN; i++ )
				assertEquals(data[0][i], tmp_b[i]);
		} catch (IOException e) {
			assertTrue("Error in reading", false);
		}
		for(int i = 0; i < len+1; i++)
			buff.push(new ByteArrayInputStream(data[i]));
		for(int i = 0; i < len; i++) {
			tmp = buff.pop();
			try {
				tmp.read(tmp_b);
				for( int j= 0; j < DATASETS_LEN; j++ )
					assertEquals(data[i+1][j], tmp_b[j]);
			} catch (IOException e) {
				assertTrue("Error in reading", false);
			}
		}
	}
}
