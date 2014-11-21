package ufmf;

/**
 * Header for image stack
 * 
 * @author Austin Edwards
 * @version 1.0
 * 
 */

public class ImageStackHeader implements Comparable<ImageStackHeader>, Cloneable {

	/**
	 * Number of frames in stack
	 */
	public final int nframes;
	
	/**
	 * Location of keyframe in file
	 */
	public final long loc;

	/**
	 * Creates ImageStackHeader
	 * 
	 * @param nframes	Number of frames in stack
	 * @param loc		Location of keyframe in file
	 */
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
