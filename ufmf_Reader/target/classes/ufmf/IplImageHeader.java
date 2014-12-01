package main.java.ufmf;

import ij.io.FileInfo;
import ij.io.ImageReader;
import ij.process.ByteProcessor;
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
	private char chunktype;
	
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
	private char datatype;
	private String[] javaclass;
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
		
		chunktype = (char) (raf.read() & 0xFF);
		
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
		datatype = (char) raf.read();
		
		javaclass = dtypechar2javaclass(datatype);
		
		width = (int) raf.readShort();
		height = (int) raf.readShort();
		
		timestamp = raf.readDouble();
		
		
	}
	
	/**
	 * Returns metadata for image
	 * 
	 * @return metadata for image
	 * @throws IOException
	 */
	
	public FileInfo getFileInfo() throws IOException {
		
		FileInfo fi = new FileInfo();
		
		fi.height = height;
		fi.width = width;
		
		if (javaclass[0].equals("float")){
			fi.fileType = FileInfo.GRAY32_FLOAT;
		}
		else if(javaclass[0].equals("uint8")) {
			fi.fileType = FileInfo.GRAY8;
		}
		
		return fi;
		
	}
	
	/**
	 * Reads image data from the UFMF file and returns it as an ImageProcessor
	 * 
	 * @param raf	UFMF file
	 * @return ImageProcessor containing image data read from UFMF File
	 * @throws IOException
	 */
	public ImageProcessor getImageData(RandomAccessFile raf, UfmfHeader header) throws IOException {
		
		FileInfo fi = getFileInfo();
		ImageReader ir = new ImageReader(fi);
		int nbytes = fi.width*fi.height*header.bytes_per_pixel;
		
		switch (fi.fileType) {
		
		case FileInfo.GRAY8:
			byte bytebuf[] = new byte[nbytes];
			raf.read(bytebuf);
	    	ByteArrayInputStream bis = new ByteArrayInputStream(bytebuf);
	    	Object pixels = ir.readPixels(bis);
	    	return new ByteProcessor(fi.width, fi.height, (byte[]) pixels);
	    	
		case FileInfo.GRAY32_FLOAT:
			float[] buf = new float[nbytes];
			raf.readFloat(buf,0,nbytes);
			return new FloatProcessor(fi.width, fi.height, (float []) buf);
		
		
	    
	    default:
	    	throw new IOException("unhandled data type: " + fi.toString());
		}
	}
		
	 /**
     * Reads and returns an ImageProcessor of the image at the location of the current file pointer in the UFMF
     * 
     * @param raf 	The UFMF file
     * @return ImageProcessor of the image at the location of the current file pointer in the UFMF
     * @throws IOException
     */
    public static ImageProcessor loadIplImage (RandomAccessFile raf, UfmfHeader header) throws IOException {
    	IplImageHeader im = new IplImageHeader(raf);
    	return im.getImageData(raf, header);
    	
    }
    
    private String[] dtypechar2javaclass(char dtype) {
    	
		String javaclass[] = {null,null};
	

			switch(dtype) {
		
			case 'c':
			case 's':
			case 'p':	
				javaclass[0] = "char";
				javaclass[1] = "1";
				break;
			case 'b':
				javaclass[0] = "int8";
				javaclass[1] = "1";
				break;
			case 'B':
				javaclass[0] = "uint8";
				javaclass[1] = "1";
				break;
			case 'h':
				javaclass[0] = "int16";
				javaclass[1] = "2";
				break;
			case 'H':
				javaclass[0] = "uint16";
				javaclass[1] = "2";
				break;
			case 'i':
			case 'l':
				javaclass[0] = "int32";
				javaclass[1] = "4";
				break;
			case 'I':
			case 'L':
				javaclass[0] = "uint32";
				javaclass[1] = "4";
				break;
			case 'q':
				javaclass[0] = "int64";
				javaclass[1] = "8";
				break;
			case 'Q':
				javaclass[0] = "uint64";
				javaclass[1] = "8";
				break;
			case 'f':
				javaclass[0] = "float";
				javaclass[1] = "4";
				break;
			case 'd':
				javaclass[0] = "double";
				javaclass[1] = "8";
				break;
			default:
				System.err.printf("Unknown data type %s", dtype);
			}
			return javaclass;
	}
	
}
