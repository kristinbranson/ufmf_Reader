package main.java.ufmf;

import ij.io.FileInfo;
import ij.io.ImageReader;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import ucar.unidata.io.RandomAccessFile;


/**
 * Contains metadata for image from UFMF file, and provides methods to convert from
 * 	UFMF image to ImageProcessor
 * 
 * @author Austin Edwards
 * @version 1.0
 * 
 */

@SuppressWarnings("unused")
public class IplImageHeader {
	
	/**
	 * Binary indicating the type of data to be read
	 */
	private int chunktype;
	
	/**
	 * Length of string indicating type of frame being restored
	 */
	private int l;
	
	/**
	 * String indicating type of frame being restored (ex: 'mean')
	 */
	
	private String frametype;
	
	/**
	 * Image width in pixels
	 */
	private int width; 
	
	/**
	 * Image height in pixels
	 */
	private int height;
	
	/**
	 * Type of data to be read
	 */
	private String datatype;
	
	/**
	 * Timestamp for frame 
	 */
	private double timestamp;
	
	/**
	 * Creates IplImageHeader from the UFMF file at the current file pointer location
	 * 
	 * @param raf			UFMF File
	 * @throws IOException
	 */
	
	public IplImageHeader(RandomAccessFile raf) throws IOException{
		long pos = raf.getFilePointer();
		chunktype = raf.read();
		if (chunktype != 0){
			System.err.println("Expecting keyframe chunk");
			return;
		}
		
		l = raf.read();
		
		StringBuilder s = new StringBuilder();
		char c;
		
		for (int i = 0; i < l; i++) {
			c = (char) raf.readUnsignedByte();
			s.append(c); // should spell 'mean'
		}
		frametype = s.toString();
		
		datatype = Character.toString((char) raf.read());
		String[] javaclass = {null,null};
		
		if (datatype.trim().equals("f")) {
			javaclass[0] = "float";
			javaclass[1] = "1";
		}
		else {
			System.err.println("Unexpected image datatype");
			return;
		}
		
		height = (int) raf.readShort();
		width = (int) raf.readShort();
		
		timestamp = raf.readDouble();
		
		
	}
	
	/**
	 * Returns metadata for image
	 * 
	 * @return metadata for image
	 * @throws IOException
	 */
	
	public FileInfo getFileInfo() throws IOException {
		
		int bytesPerPixel = 1;
		
		FileInfo fi = new FileInfo();
		
		fi.height = height;
		fi.width = width;
		fi.fileType = FileInfo.GRAY32_FLOAT;
		return fi;
		
	}
	
	/**
	 * Reads image data from the UFMF file and returns it as an ImageProcessor
	 * 
	 * @param raf	UFMF file
	 * @return ImageProcessor containing image data read from UFMF File
	 * @throws IOException
	 */
	public ImageProcessor getImageData(RandomAccessFile raf) throws IOException {
		
		FileInfo fi = getFileInfo();
		ImageReader ir = new ImageReader(fi);
		int nbytes = fi.width*fi.height;
		float[] buf = new float[nbytes];
		
		raf.readFloat(buf,0,nbytes);
		return new FloatProcessor(fi.width, fi.height, (float []) buf);
			
	}
	
	 /**
     * Reads and returns an ImageProcessor of the image at the location of the current file pointer in the UFMF
     * 
     * @param raf 	The UFMF file
     * @return ImageProcessor of the image at the location of the current file pointer in the UFMF
     * @throws IOException
     */
    public static ImageProcessor loadIplImage (RandomAccessFile raf) throws IOException {
    	IplImageHeader im = new IplImageHeader(raf);
    	return im.getImageData(raf);
    	
    }
	
}
