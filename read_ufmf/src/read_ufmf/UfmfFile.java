package read_ufmf;

import ij.IJ;
import ij.io.FileInfo;
import ij.io.ImageReader;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import read_ufmf.UfmfHeader;
import ucar.unidata.io.RandomAccessFile;

/**
 * An implementation RandomAccessFile for reading UFMF files.
 * 
 * 
 * @author Austin Edwards
 * @see RandomAccessFile
 * @see ImageStackLocator
 * 
 */

public class UfmfFile extends RandomAccessFile {

	/**
	 * Main UFMF header
	 */
	private UfmfHeader header;
	public FrameIndex frameindex = new FrameIndex();
	public KeyFrameIndex keyframeindex = new KeyFrameIndex();
	
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
		
		int ver = readInt();
		long indexloc = readLong();
		int max_height = readShort();
		int max_width = readShort();
		
		int isfixedsize = read();
		
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

		skipBytes(indexloc-getFilePointer());
		
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
			frame2meanloc[j] = mean2file[frame2mean[j]];
		}
		
		
		return new UfmfHeader(s.toString(), ver, indexloc, max_height, max_width, isfixedsize,
			coding, ncolors, bytes_per_pixel, frame2file, nframes, timestamps,
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
			System.out.printf("Error reading index: dictionary does not start with '%s'.", DICT_START_CHAR);
			}
		
		int nkeys = read();
		
		for (int j = 0; j < nkeys; j++) {
			
			byte[] buf = new byte[12];
			
			//read length of key
			int l = read();
			
			//read key
			read(buf,0,l+1);
			String key = new String(buf,"UTF-8");
			
			//read chunktype
			chunktype = Character.toString((char)read());
			
			if (chunktype.trim().equals(DICT_START_CHAR)){
				
				unread();
				
				if (key.trim().equals("frame")) {
					
					frameindex.readFrameDict();
					
				}
				
				else if (key.trim().equals("keyframe")){

					keyframeindex.readKeyFrameDict();
					
				}
			}
			
		}
			
	}
	
	/**
	 * Frame index class
	 * 
	 */
	
	public class FrameIndex {

		public long[] loc;
		public double[] timestamp;
		private String DICT_START_CHAR = "d";
		private String ARRAY_START_CHAR = "a";
		public String key;
		
		public void readFrameDict() throws IOException {
				
				String chunktype = Character.toString((char)read());
				
				if (!chunktype.trim().equals(DICT_START_CHAR)){
					System.out.printf("Error reading index: dictionary does not start with '%s'.", DICT_START_CHAR);
					}
				
				// get number of keys in frames
				
				int nkeys = read();
				
				for (int j = 0; j < nkeys; j++) {
					
					byte[] buf = new byte[24];
					//read length of key
					int l = read();

					//read key
					
					read(buf,0,l+1);
					key = new String(buf,"UTF-8");
					
					//read chunktype
					chunktype = Character.toString((char)read());
					
					if (chunktype.trim().equals(ARRAY_START_CHAR)){
						
						char dtype = (char)read();
						String[] javaclass = dtypechar2javaclass(dtype);
						
				// get total number of bytes
						long numbytes = readInt() & 0xFFFFFFFFL;
						
				// get number of bytes per element of datatype "javaclass"
						int bytesperelement = Integer.parseInt(javaclass[1]);
						
				// get number of elements to read, each having number of bytes "bytesperelement"
						int n = (int) (numbytes/bytesperelement);
						
				// initialize size of loc and timestamp arrays		
						switch(dtype) {
							case 'q': loc = new long[n];
							case 'd': timestamp = new double[n];
						}
						
				// read in index		
						
						if (dtype == 'q') {
							byte[] longbuf = new byte[(int) numbytes];
							read(longbuf);
							for(int i = 0; i < numbytes; i+=bytesperelement) {
								loc[(i/bytesperelement)] = readUnsignedLongLittleEndian(longbuf,i);	
							}	
						}
						else if (dtype == 'd') {
							for(int i = 0; i < numbytes; i+=bytesperelement) {
								double k = readDouble();
								timestamp[(i/bytesperelement)] = k;
							}
						}
					}
				}
		}
		
		/**
		 * Reads unsigned long values little endian
		 * 
		 * @param buf
		 * @param start
		 * @return
		 */
		
		private long readUnsignedLongLittleEndian(byte[] buf, int start) {

			return (long)(buf[start+7] & 0xff) << 56 | (long)(buf[start+6] & 0xff) << 48 |
					(long)(buf[start+5] & 0xff) << 40 | (long)(buf[start+4] & 0xff) << 32 |
					(long)(buf[start+3] & 0xff) << 24 | (long)(buf[start+2] & 0xff) << 16 |
					(long)(buf[start+1] & 0xff) <<  8 | (long)(buf[start] & 0xff);
				}
		
		/**
		 * Translates encoded data type to Java data type
		 * 
		 * @param dtype
		 * @return String array containing two elements: data type and bytes per pixel
		 */

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
	
	/**
	 * Keyframe index class
	 */
	
	public class KeyFrameIndex {

		private String DICT_START_CHAR = "d";
		private byte[] buf = new byte[1024];
		
		public FrameIndex meanindex = new FrameIndex();
		
		public void readKeyFrameDict() throws IOException {
				
				String chunktype = Character.toString((char)read());
				
				if (!chunktype.trim().equals(DICT_START_CHAR)){
					System.out.printf("Error reading index: dictionary does not start with '%s'.", DICT_START_CHAR);
					}
				
				// get number of keys in frames
				int nkeys = read();
				
				for (int j = 0; j < nkeys; j++) {
					
					//read length of key
					int l = read();
					
					//read key
					read(buf,0,l+1);
					String key = new String(buf,"UTF-8");
					
					//read chunktype
					chunktype = Character.toString((char)read());
					
					if (chunktype.trim().equals(DICT_START_CHAR)){
						
						unread();
						
						if (key.trim().equals("mean")) {
							meanindex.readFrameDict();
						
						}
					}
				}
		}
	}
	
	/**
	 * Returns Stack containing queried frame
	 * 
	 * @param frameNumber
	 * @return CommonBackgroundStack containing queried frame
	 */
	
	public CommonBackgroundStack getStackForFrame (int frameNumber) {
		
		if (frameNumber <0 || frameNumber > getNumFrames()) {
			IJ.showMessage("UfmfReader", "FrameIndexError; UfmfFile");
			return null;
		}
		
		//find correct imageStackLocator
		ImageStackLocator isl = findStackLocForFrame(frameNumber);
		
		//read stack from file
		CommonBackgroundStack stack = null;
		try {
			stack = new CommonBackgroundStack(isl, this);
			
		} catch (IOException e) {
			IJ.showMessage("UfmfReader",
		"Getting Stack for Frame was unsuccessful in MmfFile.\n\n Error: " +e);
			return null;
		}
		return stack;
		
	}
	
	/**
	 * Retrieve ImageStackLocator with location and other info about stack containing
	 *   queried frame
	 * 
	 * @param frameNumber	queried frame 
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
	 * 
	 * 
	 * @param bak
	 * @param backgroundFileInfo
	 * @return
	 * @throws IOException
	 */
	
	BackgroundRemovedImage readBRI(ImageProcessor bak, FileInfo backgroundFileInfo) throws IOException {
		
		BackgroundRemovedImageHeader h = new BackgroundRemovedImageHeader(this);
		BackgroundRemovedImage bri = new BackgroundRemovedImage(h, bak);
		for (int j = 0; j<h.getNumimgs(); j++) {
			bri.addSubImage(readBRISubIm(bak, backgroundFileInfo));
		}
		return bri;
	}
	
	private BRISubImage readBRISubIm(ImageProcessor bak, FileInfo backgroundFileInfo) throws IOException {
		Rectangle r = new Rectangle();
		
		r.x = (readByte() & 0xFF) + 256*(readByte() & 0xFF);
		r.y = (readByte() & 0xFF) + 256*(readByte() & 0xFF);
		r.width = (readByte() & 0xFF) + 256*(readByte() & 0xFF);
		r.height = (readByte() & 0xFF) + 256*(readByte() & 0xFF);
		
		FileInfo fi = (FileInfo) backgroundFileInfo.clone();
		
		fi.width = r.width;
		fi.height = r.height;
		byte buf[] = new byte[fi.width*fi.height*header.bytes_per_pixel];
		
		float[] ubuf = readByteToUnsignedFloat(buf);
		
		FloatProcessor ip = new FloatProcessor(r.width, r.height, ubuf);
		
		
		return new BRISubImage(ip,r);
		
	}

	private float[] readByteToUnsignedFloat(byte[] buf) throws IOException {
		
		float[] ubuf = new float[buf.length];
		read(buf);
		for(int i=0; i<buf.length; i++) {
			ubuf[i] = buf[i] & 0xFF;
			}
		return ubuf;
		
	}
}

