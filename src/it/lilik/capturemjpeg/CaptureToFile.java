/**
 * 
 */
package it.lilik.capturemjpeg;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.apache.commons.httpclient.methods.GetMethod;

/**
 * @author Alessio Caiazza
 * @author Cosimo Cecchi
 *
 */
public class CaptureToFile {

	private String path;
	private String url;
	private AsyncProducer client;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 2 || args.length == 3 || args.length > 4) {
			System.err.println("Wrong parameters");
			System.out.println("Usage:");
			System.exit(-1);
		}
		String user = null , pass = null;
		if (args.length == 4) {
			user = args[2];
			pass = args[3];
		}
		CaptureToFile ctf = new CaptureToFile(args[0],
				args[1], user, pass);
		
		ctf.start();
		//start capturing images from buffer
		try {
			ctf.capture(20);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			ctf.stop();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	public CaptureToFile(String path, String url, 
			String user, String password) {
		this.path = path;		
		this.url = url;
		client  = new AsyncProducer(new GetMethod(url),
				user, password);
	}
	
	public void capture(int images) throws FileNotFoundException {
        File fout;
		FileOutputStream fos;
		BufferedOutputStream os;
		ByteArrayInputStream img;
		byte buff[] = new byte[1024];
		int readBytes;
		for( int i = 0; i < images; i++ ) {
			fout = new File(getPath() + String.valueOf(i) + ".jpg");
			fos = new FileOutputStream(fout);
			os = new BufferedOutputStream(fos);
			
			//TODO: is this the better way?
			while( !client.isImageAvailable() ) {
				Thread.yield();
			}
			
			img = client.pop();
			try {
				while((readBytes = img.read(buff)) != -1) {
					os.write(buff, 0, readBytes);
				}
				os.close();
				img.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @return the path
	 */
	public synchronized String getPath() {
		return path;
	}

	public CaptureToFile(String path, String url) {
		this(path, url, null, null );
	}
	
	public void start() {
		client.start();
	}
	
	public void stop() throws InterruptedException {
		client.setShouldStop(true);
		client.join();
	}

}
