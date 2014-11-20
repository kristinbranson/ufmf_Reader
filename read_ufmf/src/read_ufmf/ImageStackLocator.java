package read_ufmf;

import read_ufmf.ImageStackHeader;

/**
 * Extension of ImageStackHeader with added methods for finding first and last frame in a stack
 * 	and determining if a frame is within the stack
 * 
 * @author Austin Edwards
 * @version 1.0
 * @see ImageStackHeader
 * 
 */
public class ImageStackLocator extends ImageStackHeader {

	/**
	 * Index of the first frame in the stack
	 */
	public int startFrame;
	
	/**
	 * Index of the last frame in the stack
	 */
	public int lastFrame;
	
	/**
	 * Creates an ImageStackLocator using info from the ImageStackHeader
	 * 
	 * @param h				header
	 * @param startFrame	index of first frame in the stack
	 */
	public ImageStackLocator (ImageStackHeader h, int startFrame) {
		this(h.nframes, h.loc, startFrame);
	}
	
	/**
	 * Creates ImageStackLocator from scratch using given parameters
	 * 
	 * @param nframes 		number of frames in the stack
	 * @param loc			location of keyframe in file
	 * @param startFrame	index of first frame in the stack
	 */
	public ImageStackLocator(int nframes, long loc, int startFrame) {
		super(nframes, loc);
		this.startFrame = startFrame;
		this.lastFrame = startFrame + nframes - 1;
	}
	
	/**
	 * Returns true if frame is within stack
	 * 
	 * @param frame
	 * @return true if frame is within stack, false otherwise
	 */
	public boolean containsFrame(int frame) {
		return (startFrame <= frame && lastFrame >= frame);
	}

	/**
	 * Returns index of first frame in stack
	 * 
	 * @return index of last frame in stack
	 */
	public int getStartFrame() {
		return startFrame;
	}
	
	/**
	 * Returns index of last frame in stack
	 * 
	 * @return index of last frame in stack
	 */
	public int getLastFrame() {
		return lastFrame;
	}
	
}
