package read_ufmf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import ucar.unidata.io.RandomAccessFile;

public class UfmfFile extends RandomAccessFile {
	
	public UfmfHeader header;
	public FrameIndex frameindex = new FrameIndex();
	public KeyFrameIndex keyframeindex = new KeyFrameIndex();

	
	public UfmfFile(String location, String mode) throws IOException {
		super(location, mode);
		order (LITTLE_ENDIAN);
	}
	
	public void parse() throws IOException{
        //stackLocations = new ArrayList<ImageStackLocator>();
        seek(0);
        header = readFileHeader();
        int frame = 0;
        
    }

	public UfmfHeader readFileHeader() throws IOException {
		
		int maxnmeanscached = 5;
		
		byte[] buf = new byte[1024];
		
		read(buf, 0, 4);
		String s = new String(buf,"UTF-8");
		
		int ver = readInt();
		long indexloc = readLong();
		
		int max_height = readShort();
		int max_width = readShort();
		
		int isfixedsize = read();
		
		int l = read();
		
		read(buf,4,l);
		byte [] codingBytes = Arrays.copyOfRange(buf, 4, 4+l);
		String coding = new String(codingBytes,"UTF-8");
		
		coding = coding.toLowerCase();
		
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
		
		for (int j = 0; j < timestamps.length; j++){
			for (int i = 0; i < nmeans-1; i++){
				
				if ((timestamps[j] >= meantimestamps[i]) & (timestamps[j] < meantimestamps[i+1])) {	
					frame2mean[j] = i;
				}
				
			}
			frame2meanloc[j] = mean2file[frame2mean[j]];
		}
		
		int nmeanscached = Math.min(maxnmeanscached, nmeans);
		
		
		return new UfmfHeader(s, ver, indexloc, max_height, max_width, isfixedsize,
			coding, ncolors, bytes_per_pixel, frame2file, nframes, timestamps,
			mean2file, nmeans, meantimestamps, frame2mean, frame2meanloc, nr, nc,
			nmeanscached);
		
	}
	
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
		
		private long readUnsignedLongLittleEndian(byte[] buf, int start) {

			return (long)(buf[start+7] & 0xff) << 56 | (long)(buf[start+6] & 0xff) << 48 |
					(long)(buf[start+5] & 0xff) << 40 | (long)(buf[start+4] & 0xff) << 32 |
					(long)(buf[start+3] & 0xff) << 24 | (long)(buf[start+2] & 0xff) << 16 |
					(long)(buf[start+1] & 0xff) <<  8 | (long)(buf[start] & 0xff);
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
	
	public MeanFrame readMean(UfmfHeader header, int i) throws IOException{
		
		MeanFrame meanframe = new MeanFrame(header, i);
		return meanframe;
		
	}
	
	public MeanFrame[] readAllMeans(UfmfHeader header) throws IOException{
		int maxnmeanscached = 5;
		int nmeanscached = Math.min(maxnmeanscached, header.nmeans);
		MeanFrame[] allMeanFrames = new MeanFrame[header.nmeans];
		
		for (int i = 0; i < header.nmeans; i++){
			
			allMeanFrames[i] = new MeanFrame(header, i);
			
			if (i == 0) {
				header.nr = allMeanFrames[i].sz[0];
				header.nc = allMeanFrames[i].sz[1];
			}

		}
		return allMeanFrames;
	}

	public class MeanFrame {
		
		public int meani;
		public int framei;
		public boolean dopermute;
		public float[][] img;
		public int[] sz = {0,0};
		public double timestamp;
		public String Name;
		
		public MeanFrame(UfmfHeader header, int i) throws IOException {
			
			this.Name = "MeanFrame" + String.valueOf(i);
			this.getImage(i);
		}
		
		byte[] meanbuf = new byte[24];
		
		public void getImage(int i) throws IOException{

			seek(header.mean2file[i]);
			
			int KEYFRAME_CHUNK = 0;
			String MEAN_KEYFRAME_TYPE = "mean";
			
			int chunktype = read();
			
			if (chunktype != KEYFRAME_CHUNK){
				System.err.println("Expecting keyframe chunk");
				return;
			}
			
			int l = read();
			
			read(meanbuf,0,l);
			
			String keyframetype = new String(meanbuf,"UTF-8");
			if (!keyframetype.trim().equals(MEAN_KEYFRAME_TYPE)) {
				System.err.printf("Expected keyframetype %s at start of mean keyframe");
				return;
			}

			String datatype = Character.toString((char)read());
			String[] javaclass = {null,null};
			
			if (datatype.trim().equals("f")) {
				javaclass[0] = "float";
				javaclass[1] = "1";
			}
			else {
				System.err.println("Unexpected image datatype");
				return;
			}
			
			sz[0] = readShort();
			sz[1] = readShort();

			
			int height = sz[0];
			int width = sz[1];
			this.img = new float[height*header.bytes_per_pixel][width*header.bytes_per_pixel];
			
			timestamp = readDouble();
			
			int row = 0;
			int col = 0;
			
			for (int j = 0; j < (height*width*header.bytes_per_pixel-1); j++){
				
				this.img[row][col] = readFloat();
				col+=1;
				if (col == width){
					row+=1;
					col=0;
				}
				
			}
		}
	}
	
	public class Frame {
		
		public int nboxes = 0;
		private int ncolors;
		private int maxh;
		private int maxw;
		private int framei;
		public float[][] img;
		MeanFrame meanframe;
		private int FRAME_CHUNK = 1;
		public double timestamp;
		private int[] data;
		
		public Frame(UfmfHeader header, int i, MeanFrame[] MeanFrame) throws IOException {
			
			this.ncolors = header.ncolors;
			this.maxh = header.max_height;
			this.maxw = header.max_width;
			this.framei = i;
			this.readFrame(header, MeanFrame);
		}
		
		public void readFrame(UfmfHeader header, MeanFrame[] MeanFrame) throws IOException {
			
			int meani = header.frame2mean[this.framei];
			meanframe = MeanFrame[meani];
			
			img = meanframe.img;
			
			seek(header.frame2file[framei]);
			
			int chunktype = read();
			
			if (chunktype != FRAME_CHUNK) {
				System.err.println("Expecting frame chunk");
				return;
			}
			
			timestamp = readDouble();
			
			
			byte[] nboxesbuf = new byte[4];
			
			if (header.ver == 4) {
				
				read(nboxesbuf,0,4);
				nboxes = (nboxesbuf[0] & 0xFF) + ((nboxesbuf[1] & 0xFF)<<8) + ((nboxesbuf[2] & 0xFF)<<16) + ((nboxesbuf[3] & 0xFF)<<24);
			}
			
			else if (header.ver == 2){
				nboxes = readShort();
			}
			
			if (!header.dataclass.trim().equals("uint8")) {	
				System.err.printf("Unexpected datatype %s.", header.dataclass);
			}
			if (header.isfixedsize == 1) {
				int[][] bb = new int[nboxes][2];
				
				for (int col2 = 0; col2<nboxes; col2++) {
					
					bb[col2][1] = read();
					
				}
				for (int col1 = 0; col1<nboxes; col1++) {
				
					bb[col1][0] = read();
				
				}
			
				data = new int[nboxes*header.max_width*header.max_height*header.bytes_per_pixel];
			
				for (int i = 0; i < data.length; i++) {
				
					data[i] = read() & 0xFF;
				
				}
				
			}
 			
			else {
				
				int[] bb = new int[nboxes*4];
				int[] data = new int[nboxes*header.max_width*header.max_height*header.bytes_per_pixel];
				
				if (framei+1 == header.nframes) {
					int idx = 0;
					for (int i = 0; i < nboxes; i++) {
						
						skipBytes(4);
						
						int height = (int) readShort();
						int width = (int) readShort();
						
						
						for (int j = idx; j < idx+width*height*header.bytes_per_pixel; j++) {
							data[j] = read() & 0xFF;
						}
						
						idx = idx+width*height*header.bytes_per_pixel;
						
						//TO DO: Reshape databuf
					}
					
					
				}
				
				else {

					//total number of bytes in frame
					
					int nbytes = (int) (header.frame2file[framei+1]-header.frame2file[framei]+1);
					byte[] cache = new byte[nbytes];
					//unsigned cache
					int[] ucache = new int[nbytes];
					
					read(cache,0,nbytes*header.bytes_per_pixel);
					for (int u = 0; u < cache.length; u++) {
						
						ucache[u] = cache[u] & 0xFF;
					}
										
					int cacheidx = 0;
					int width = 0;
					int height = 0;
					
					for (int i = 0; i < nboxes; i++) {
						System.out.println(nboxes);
						
						int[] tmp = Arrays.copyOfRange(ucache, cacheidx, cacheidx+8);
						
						for (int j = 0; j < 4; j++) {
							
							bb[4*i+j] = tmp[2*j] + 256*tmp[2*j+1];
						}
						
						width = bb[4*i+3]; height = bb[4*i+2];
						int k = cacheidx;
						
						for (int w = bb[4*i+1]-1; w < bb[4*i+1]+width-1; w++) {
							for (int h = bb[4*i]-1; h < bb[4*i]+height-1; h++ ) {
													
								img[w][h] = ucache[k+8];
								
								k++;
									
								}
							}	
						cacheidx=cacheidx+8+width*height*header.bytes_per_pixel;
					}
				}
				
			}
			
	}
		

	
}
	public Frame getFrame(UfmfHeader header, int i, MeanFrame[] MeanFrames) throws IOException{
		System.out.println(i);
		Frame frame = new Frame(header, i, MeanFrames);
		return frame;
	}

}	
