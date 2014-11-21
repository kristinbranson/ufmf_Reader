package main.java.ufmf;

/**
 * The main header for the UFMF file
 * 
 * @author Austin Edwards
 * @version 1.0
 * @see UfmfFile
 *
 */
class UfmfHeader {

	//public int MAXNMEANSCACHED = 5;
	
	public long pos;
	
	public String s;
	public int ver;
	public long indexloc;
	
	public int max_height;
	public int max_width;
	
	public int isfixedsize;
	
	public String coding;
	public int ncolors;
	public int bytes_per_pixel;
	
	public String dataclass;
	
	public long[] frame2file;
	public int nframes;
	public double[] timestamps;
	
	public long[] mean2file;
	public int nmeans;
	public int[] framespermean;
	public double[] meantimestamps;
	
	public int[] frame2mean;
	public long[] frame2meanloc;
	
	public int nr;
	public int nc;
	
	public int[] cachedmeans_idx;
	
	public int nmeanscached;
	
	public String movie;
	
	public UfmfHeader(long pos, String s, int ver, long indexloc, int max_height, int max_width, int isfixedsize,
			String coding, int ncolors, int bytes_per_pixel, String dataclass, long[] frame2file, int nframes,
			double[] timestamps, long[] mean2file, int nmeans, int[] framespermean, double[] meantimestamps, int[] frame2mean,
			long[] frame2meanloc, int nr, int nc) {
		super();
		
		this.pos = pos;
		this.s = s;
		this.ver = ver;
		this.indexloc = indexloc;
		this.max_height = max_height;
		this.max_width = max_width;
		this.isfixedsize = isfixedsize;
		this.coding = coding;
		this.ncolors = ncolors;
		this.bytes_per_pixel = bytes_per_pixel;
		this.dataclass = dataclass;
		
		this.frame2file = frame2file;
		this.nframes = nframes;
		this.timestamps = timestamps;
		this.mean2file = mean2file;
		this.nmeans = nmeans;
		this.framespermean = framespermean;
		this.meantimestamps = meantimestamps;
		this.frame2mean = frame2mean;
		this.frame2meanloc = frame2meanloc;
		this.nr = nr;
		this.nc = nc;
		
	}

	
}
