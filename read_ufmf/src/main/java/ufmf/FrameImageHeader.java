package main.java.ufmf;

import java.io.IOException;
import ucar.unidata.io.RandomAccessFile;

/**
 * Metadata for frame
 * 
 * @author Austin Edwards
 * @version 1.0
 * @see FrameImage
 * 
 */

@SuppressWarnings("unused")
public class FrameImageHeader {

	/**
	 * The file pointer for beginning of header
	 */
	private long pos;
	
	/**
	 * Binary ID code for following chunk of data
	 */
	private int chunktype;
	
	/**
	 * Number of subimages in frame
	 */
	private int numsubimgs;
	
	/**
	 * Timestamp for frame
	 */
	private double timestamp;
	
	public FrameImageHeader(RandomAccessFile raf) throws IOException {
		
		pos = raf.getFilePointer();
		chunktype = raf.read();
		
		if (chunktype != 1) {
			// need to change this to imageJ error
			System.err.println("Expecting frame chunk");
			return;
		}
		
		timestamp = raf.readDouble();
		
		byte[] numimgsbuf = new byte[4];
		raf.read(numimgsbuf,0,4);
		numsubimgs = (numimgsbuf[0] & 0xFF) + ((numimgsbuf[1] & 0xFF)<<8) + ((numimgsbuf[2] & 0xFF)<<16) + ((numimgsbuf[3] & 0xFF)<<24);
		
	}
	
	public int getNumimgs() {
		return numsubimgs;
	}

}
