package read_ufmf;

public class ImageStackHeader implements Comparable<ImageStackHeader>, Cloneable {

	public final int nframes;
	public final long loc; //location of meanframe in file
	
	public ImageStackHeader (int nframes, long loc) {
		super();
		this.nframes = nframes;
		this.loc = loc;
		
	}

	@Override
	public int compareTo(ImageStackHeader o) {
		return (int) (this.loc - o.loc);
	}
	
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	
}
