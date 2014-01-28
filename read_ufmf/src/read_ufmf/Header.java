package read_ufmf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class Header {
	
	public int MAXNMEANSCACHED = 5;
	
	private byte[] buf = new byte[1024];
	private int bytePos = 0;
	
	
	// ufmf
	public String s;
	
	public void checkUfmf(InputStream is) throws IOException{
		
		is.read(buf, bytePos, 4);
		bytePos += 4;
		
		s = new String(buf,"UTF-8");
		
	}
	
	//version
	public int ver;
	
	public void getVersion(InputStream is) throws IOException{
		
		is.read(buf,bytePos,4);
		bytePos += 4;
		ver = readIntLittleEndian(buf,4);
		
	}
	
	public long indexloc;
	
	public void getIndexLoc(InputStream is) throws IOException {
		
		is.read(buf,bytePos,8);
		bytePos += 8;
		
		indexloc = readLongLittleEndian(buf,8);
		
	}
	
	public int max_height;
	public int max_width;
	
	
	public void getSize(InputStream is) throws IOException {
		
		is.read(buf,bytePos,4);
		bytePos += 4;
		
		max_height = readShortLittleEndian(buf,16);
		max_width = readShortLittleEndian(buf,18);
		
	}
	
	int isfixedsize;
	
	public void isFixedSize(InputStream is) throws IOException {
		is.read(buf, bytePos, 1);
		bytePos += 1;
		isfixedsize = buf[20];
	}
	
	//coding length
	private int l;
	
	//coding
	public String coding;
	public int ncolors;
	public int bytes_per_pixel;
	
	public void getCoding(InputStream is) throws IOException {
		
		is.read(buf,bytePos,1);
		bytePos +=1;
		
		l = buf[21];

		is.read(buf,bytePos,l);
		bytePos += l;
		
		byte [] codingBytes = Arrays.copyOfRange(buf, 22, 22+l);
		coding = new String(codingBytes,"UTF-8");
		
		coding = coding.toLowerCase();
		
		if (coding.equals("mono8")) {
			ncolors = 1;
			bytes_per_pixel = 1;
		}
		else if (coding.equals("rgb24")){
			ncolors = 3;
			bytes_per_pixel = 3;
		}
		
	}
	
	public String dataclass = "uint8";

	public void skipToIndex(InputStream is) throws IOException {
		
		is.skip(indexloc-bytePos);
		
	}

	public long[] frame2file;
	public int nframes;
	public double[] timestamps;
	
	public long[] mean2file;
	public int nmeans;
	public double[] meantimestamps;
	
	public int[] frame2mean;
	public long[] frame2meanloc;
	
	public void getFrameInfo(Index index) {
		
		frame2file = Index.frameindex.loc;
		nframes = frame2file.length;
		timestamps = Index.frameindex.timestamp;
		
		mean2file = Index.keyframeindex.meanindex.loc;
		nmeans = mean2file.length;
		System.out.println(nmeans);
		meantimestamps = Index.keyframeindex.meanindex.timestamp;
		
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
		
	}
	
	public int nr;
	public int nc;

	public void storeMeanSize(MeanFrame meanframe) {
		
		nr = meanframe.sz[0];
		nc = meanframe.sz[1];

	}
	
	public int[] cachedmeans_idx;
	
	public void myParse(MeanFrame[] allMeanFrames, int nmeanscached) {
		
			int[] result = new int[nmeanscached];
		
		    for(int i=0;i<nmeanscached;i++) {
		       result[i] = i+1;
		    }
		    
		    cachedmeans_idx = result;
		    
		 }
	
	private static int readIntLittleEndian(byte[] buf, int start) {
		
		return (buf[start]) + (buf[start+1]<<8) + (buf[start+2]<<16) + (buf[start+3]<<24);
		
	}

	private static long readLongLittleEndian(byte[] buf, int start) {
        
		return (long)(buf[start+7]) << 56 | (long)(buf[start+6]&0xff) << 48 |
            (long)(buf[start+5] & 0xff) << 40 | (long)(buf[start+4] & 0xff) << 32 |
            (long)(buf[start+3] & 0xff) << 24 | (long)(buf[start+2] & 0xff) << 16 |
            (long)(buf[start+1] & 0xff) <<  8 | (long)(buf[start] & 0xff);	
	}

	public static short readShortLittleEndian(byte[] buf, int start) {
		
		return (short) ((buf[start+1] << 8) | (buf[start] & 0xff));
		
	}
}
