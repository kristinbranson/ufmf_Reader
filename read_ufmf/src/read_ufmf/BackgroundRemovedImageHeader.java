package read_ufmf;

import java.io.IOException;

import ucar.unidata.io.RandomAccessFile;

public class BackgroundRemovedImageHeader {

	private long pos;
	private int chunktype;
	private int numimgs;
	private double timestamp;
	
	public BackgroundRemovedImageHeader(RandomAccessFile raf) throws IOException {
		
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
		numimgs = (numimgsbuf[0] & 0xFF) + ((numimgsbuf[1] & 0xFF)<<8) + ((numimgsbuf[2] & 0xFF)<<16) + ((numimgsbuf[3] & 0xFF)<<24);
		
	}
	
	public int getNumimgs() {
		return numimgs;
	}
	
}
