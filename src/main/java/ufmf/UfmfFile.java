package main.java.ufmf;

import ij.IJ;
import ij.io.FileInfo;
import ij.io.ImageReader;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import main.java.ufmf.UfmfHeader;
import ucar.unidata.io.RandomAccessFile;

/**
 * An implementation RandomAccessFile for reading UFMF files.
 * 
 * 
 * @author Austin Edwards
 * @version 1.0
 * @see RandomAccessFile
 * @see ImageStackLocator
 * 
 */

public class UfmfFile extends RandomAccessFile {

	/**
	 * Main UFMF header
	 */
	private UfmfHeader header;
	public FrameIndex frameindex;
	public KeyFrameIndex keyframeindex;
	
	/**
	 * Index of UFMF ImageStacks within UFMF file
	 */
	private ArrayList<ImageStackLocator> stackLocations;
	private boolean parsed = false;
	
	/**
	 * Returns number of UFMF ImageStacks
	 * @return Number of UFMF ImageStacks
	 */
	public int getNumStacks() {
		return stackLocations.size();
	}
	
	/**
	 * Returns main UFMF header
	 * @return UFMF header
	 */
	public UfmfHeader getHeader() {
		if (!parsed) {
			try {
				parse();
			} catch (IOException e) {
				return null;
			}
		}
		return header;
	}
	
	/**
	 * Returns total number of frames in the UFMF movie
	 * @return total number of frames in movie
	 */
	public int getNumFrames(){
		if (!parsed) {
			try {
				parse();
			} catch (IOException e) {
				return -1;
			}
		}
		return stackLocations.get(stackLocations.size()-1).getLastFrame() -
				stackLocations.get(0).getStartFrame() + 1;
	}
	
	/**
	 * Reads main UFMF header, including keyframe locations
	 * @throws IOException
	 */
	public void parse() throws IOException {
		stackLocations = new ArrayList<ImageStackLocator>();
		seek(0);
		header = readFileHeader();
		int frame = 0;
		
		for (int i = 0; i < header.nmeans; i++) {
			ImageStackHeader h = readImageStackHeader(i);
			stackLocations.add(new ImageStackLocator(h, frame));
			frame += h.nframes;	
		}
		parsed = true;
	}
	
	

	/**
	 * Creates an UFMF file.
	 * 
	 * @param location		Location of the file
	 * @param mode			Access mode, either: r, rw, rws, or rwd
	 * @param bufferSize	Size of buffer
	 * @throws IOException
	 */
	public UfmfFile(String location, String mode, int bufferSize) throws IOException {
		super(location, mode, bufferSize);
		order (LITTLE_ENDIAN);
	}

	/**
	 * Creates an UFMF file.
	 * 
	 * @param bufferSize	Size of buffer
	 */
	public UfmfFile(int bufferSize) {
		super(bufferSize);
		order (LITTLE_ENDIAN);
	}

	/**
	 * Creates an UFMF file.
	 * 
	 * @param location		Location of the file
	 * @param mode			Access mode, either: r, rw, rws, or rwd
	 * @throws IOException
	 */
	public UfmfFile(String location, String mode) throws IOException {
		super(location, mode);
		order (LITTLE_ENDIAN);
	}
	
	/**
	 * Copies keyframe stack info from main header into keyframe headers
	 * 
	 * @param i					
	 * @return ImageStackHeader Header for each keyframe stack
	 */
	private ImageStackHeader readImageStackHeader(int i) {
		
		int nframes = header.framespermean[i];
		long loc = header.mean2file[i];
		return new ImageStackHeader( nframes, loc);
	}
	
	/**
	 * Processes (and returns) the main UFMF header, positioning the file pointer at the beginning of the first ImageStack.
	 * 
	 * @return The main MMF header
	 * @throws IOException
	 */
	
	public UfmfHeader readFileHeader() throws IOException{
		long pos = getFilePointer();
		StringBuilder s = new StringBuilder();
		char c;
		
		for (int i = 0; i < 4; i++) {
			c = (char) readUnsignedByte();
			s.append(c); // should spell 'ufmf'
		}
		
		if (!s.toString().toLowerCase().equals("ufmf")) {
			IJ.showMessage("Invalid UFMF file: first four bytes must be 'ufmf'.");
		}
		
		int ver = readInt();
		
		if (ver < 2 || ver > 4) {
			IJ.showMessage("Only UFMF versions 2-4 are supported: ");
			return null;
		}
		
		long indexloc = readLong();
		int max_height = readShort();
		int max_width = readShort();
		
		int isfixedsize = 0;
		
		if (ver >= 4) {
			isfixedsize = read();
		}
		
		int l = read();
		
		StringBuilder codingStringBuilder = new StringBuilder();
		
		for (int i = 0; i < l; i++) {
			c = (char) readUnsignedByte();
			codingStringBuilder.append(c); // should spell 'ufmf'
		}
		
		String coding = codingStringBuilder.toString().toLowerCase();

		int ncolors = 0;
		int bytes_per_pixel = 0; 
		
		if (coding.equals("mono8")) {
			ncolors = 1;
			bytes_per_pixel = 1;
		}
		else if (coding.equals("rgb24")){
			ncolors = 3;
			bytes_per_pixel = 3;
		}
		
		String dataclass = "uint8";

		skipBytes((int) (indexloc-getFilePointer()));
		
		readDict();
		
		long[] frame2file;
		int nframes;
		double[] timestamps;
		
		long[] mean2file;
		int nmeans;
		int[] framespermean;
		double[] meantimestamps;
		
		int[] frame2mean;
		long[] frame2meanloc;
		
		int nr = 0;
		int nc = 0;
		
		frame2file = frameindex.loc;
		nframes = frame2file.length;
		timestamps = frameindex.timestamp;
		
		mean2file = keyframeindex.meanindex.loc;
		nmeans = mean2file.length;
		meantimestamps = keyframeindex.meanindex.timestamp;
		
		frame2mean = new int[nframes];
		Arrays.fill(frame2mean, nmeans-1);
		
		frame2meanloc = new long[nframes];
		framespermean = new int[nmeans];
		
		for (int j = 0; j < timestamps.length; j++){
			for (int i = 0; i < nmeans-1; i++){
				
				if ((timestamps[j] >= meantimestamps[i]) & (timestamps[j] < meantimestamps[i+1])) {	
					frame2mean[j] = i;
					framespermean[i]++;
				}
			}
			
			if (timestamps[j] >= meantimestamps[nmeans-1]) {	
				frame2mean[j] = nmeans-1;
				framespermean[nmeans-1]++;
			}
			frame2meanloc[j] = mean2file[frame2mean[j]];
			
		}
		
		return new UfmfHeader(pos, s.toString(), ver, indexloc, max_height, max_width, isfixedsize,
			coding, ncolors, bytes_per_pixel, dataclass, frame2file, nframes, timestamps,
			mean2file, nmeans, framespermean, meantimestamps, frame2mean, frame2meanloc, nr, nc);
		
	}
	
	/**
	 * Reads index dictionary
	 * 
	 * @throws IOException
	 */
	
	public void readDict() throws IOException {
		
		String DICT_START_CHAR = "d";

		String chunktype = Character.toString((char)read());
		
		if (!chunktype.equals(DICT_START_CHAR)){
			IJ.showMessage("Error reading index: dictionary does not start with "+DICT_START_CHAR+".");
			}
		
		int nkeys = read();
		for (int j = 0; j < nkeys; j++) {			
			
			int l = readShort();

			StringBuilder keySB = new StringBuilder();
			
			char c;
			for (int i = 0; i < l; i++) {
				c = (char) readUnsignedByte();
				keySB.append(c);
			}
			
			String key = keySB.toString();
			
			//read chunktype
			chunktype = Character.toString((char)read());
			
			if (chunktype.trim().equals(DICT_START_CHAR)){
				
				unread();
				
				if (key.trim().equals("frame")) {
					frameindex = new FrameIndex(this);
				}
				
				else if (key.trim().equals("keyframe")){
					keyframeindex = new KeyFrameIndex(this);
				}
			}
		}
	}
	
	/**
	 * Returns stack containing frame
	 * 
	 * @param frameNumber
	 * @return CommonKeyFrameStack containing frame
	 */
	
	public CommonKeyFrameStack getStackForFrame (int frameNumber) {
		
		if (frameNumber <0 || frameNumber > getNumFrames()) {
			IJ.showMessage("UfmfReader", "FrameIndexError; UfmfFile");
			return null;
		}
		
		//find correct imageStackLocator
		ImageStackLocator isl = findStackLocForFrame(frameNumber);
		
		//read stack from file
		CommonKeyFrameStack stack = null;
		try {
			stack = new CommonKeyFrameStack(isl, this, header);
			
		} catch (IOException e) {
			IJ.showMessage("UfmfReader",
		"Getting Stack for Frame was unsuccessful in MmfFile.\n\n Error: " +e);
			return null;
		}
		
		return stack;
		
	}
	
	/**
	 * Retrieve ImageStackLocator with location and other info about stack containing frame
	 * 
	 * @param frameNumber		frame 
	 * @return ImageStackLocator containing frame
	 */
	
	private ImageStackLocator findStackLocForFrame(int frameNumber) {
		if (frameNumber<0 || frameNumber>=getNumFrames()) {
			return null;
		}
		ImageStackLocator isl;
		for (int i=0; i<stackLocations.size(); i++){
			isl = stackLocations.get(i);
			if (isl.getStartFrame()<=frameNumber && isl.getLastFrame()>=frameNumber){
				return isl;
			}
		}
		int[] startFrames = new int[stackLocations.size()];
		int[] endFrames = new int[stackLocations.size()];
		
		String msg = "frame: " + frameNumber + " not found in startFrames: " + startFrames + " - end frames - " + endFrames;
		IJ.showMessage("mmfReader",msg);
		return null;
		
	}
	
	/**
	 * Processes data for a frame image, converting it to a FrameImage object
	 * 
	 * @param frameImage
	 * @param keyframeFileInfo
	 * @return
	 * @throws IOException
	 */
	
	FrameImage readFrameImage(ImageProcessor frameImage,
			FileInfo frameFileInfo, UfmfHeader ufmfh, int framei) throws IOException {
		
		FrameImageHeader h = new FrameImageHeader(this, ufmfh, framei);
		FrameImage frameimage = new FrameImage(h, frameImage);
		
		for (int j = 0; j<h.getNumimgs(); j++) {
			frameimage.addSubImage(readFrameSubIm(frameImage, frameFileInfo));
		}
		return frameimage;
	}
	
	/**
	 * Processes and returns subimage for a frame
	 * 
	 * @param frameImage
	 * @param frameFileInfo
	 * @return
	 * @throws IOException
	 */
	
	private FrameSubImage readFrameSubIm(ImageProcessor frameImage, FileInfo frameFileInfo) throws IOException {
		
		Rectangle r = new Rectangle();
		
		// reads coordinates of subimage
		
		r.x = (readByte() & 0xFF) + 256*(readByte() & 0xFF);
		r.y = (readByte() & 0xFF) + 256*(readByte() & 0xFF);
		r.width = (readByte() & 0xFF) + 256*(readByte() & 0xFF);
		r.height = (readByte() & 0xFF) + 256*(readByte() & 0xFF);
		
		FileInfo fi = (FileInfo) frameFileInfo.clone();
		
		fi.width = r.width;
		fi.height = r.height;
		
		byte buf[] = new byte[fi.width*fi.height*header.bytes_per_pixel];
		
		switch (fi.fileType) {
		
		case FileInfo.GRAY32_FLOAT:
			float[] ubuf = readByteToUnsignedFloat(buf);
			FloatProcessor ip = new FloatProcessor(r.width, r.height, ubuf);
			return new FrameSubImage(ip,r);
		default:
			ImageReader ir = new ImageReader(fi);
			read(buf);
			ByteArrayInputStream bis = new ByteArrayInputStream(buf);
	    	Object pixels = ir.readPixels(bis);
	    	ByteProcessor bp = new ByteProcessor(fi.width, fi.height, (byte[]) pixels);
	    	return new FrameSubImage(bp,r);
		}
	}

	/**
	 * Reads unsigned float array
	 * 
	 * @param buf	byte array to be read, translated to float array
	 * @return 		array of unsigned floats
	 * @throws IOException
	 */
	private float[] readByteToUnsignedFloat(byte[] buf) throws IOException {
		
		float[] ubuf = new float[buf.length];
		read(buf);
		for(int i=0; i<buf.length; i++) {
			ubuf[i] = buf[i] & 0xFF;
			}
		return ubuf;
		
	}
}

