package read_ufmf;

import read_ufmf.ImageStackHeader;

public class ImageStackLocator extends ImageStackHeader {

	public int startFrame;
	public int lastFrame;
	
	public ImageStackLocator (ImageStackHeader h, int startFrame) {
		this(h.nframes, h.loc, startFrame);
	}
	
	public ImageStackLocator(int nframes, long loc, int startFrame) {
		super(nframes, loc);
		this.startFrame = startFrame;
		this.lastFrame = startFrame + nframes - 1;
	}
	
	public boolean containsFrame(int frame) {
		return (startFrame <= frame && lastFrame >= frame);
	}

	public int getStartFrame() {
		return startFrame;
	}
	
	public int getLastFrame() {
		return lastFrame;
	}
	
}
