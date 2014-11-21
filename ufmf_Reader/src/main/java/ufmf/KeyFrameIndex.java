package ufmf;

import java.io.IOException;
import ucar.unidata.io.RandomAccessFile;


/**
 * Index object for keyframe
 * 
 * @author Austin Edwards
 * @version 1.0
 * @see FrameIndex
 *
 */
public class KeyFrameIndex {

	/**
	 * String idcode for indicating beginning of dictionary
	 */
	private String DICT_START_CHAR = "d";
	
	/**
	 * Byte buffer for reading keys
	 */
	private byte[] buf = new byte[1024];
	
	/**
	 * Frame index
	 */
	public FrameIndex meanindex;
	
	/**
	 * Constructs keyframe index from UFMF file
	 * 
	 * @param raf	UFMF file
	 * @throws IOException
	 */
	public KeyFrameIndex(RandomAccessFile raf) throws IOException {
			
			String chunktype = Character.toString((char) raf.read());
			
			if (!chunktype.trim().equals(DICT_START_CHAR)){
				System.out.printf("Error reading index: dictionary does not start with '%s'.", DICT_START_CHAR);
				}
			
			// get number of keys in frames
			int nkeys = raf.read();
			
			for (int j = 0; j < nkeys; j++) {
				
				//read length of key
				int l = raf.read();
				
				//read key
				raf.read(buf,0,l+1);
				String key = new String(buf,"UTF-8");
				
				//read chunktype
				chunktype = Character.toString((char) raf.read());
				
				if (chunktype.trim().equals(DICT_START_CHAR)){
					
					raf.unread();
					
					if (key.trim().equals("mean")) {
						meanindex = new FrameIndex(raf);
					
					}
				}
			}
	}
}