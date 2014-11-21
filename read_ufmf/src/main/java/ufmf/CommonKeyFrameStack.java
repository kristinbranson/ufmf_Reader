package main.java.ufmf;

import java.io.IOException;
import java.util.ArrayList;

import ij.io.FileInfo;
import ij.process.ImageProcessor;

/**
 * Stack of KeyFrame image and associated Frame images
 * 
 * @author Austin Edwards
 * @version 1.0
 * @see ImageStackLocator
 * @see FrameImage
 *
 */

public class CommonKeyFrameStack {

	/**
	 * The ImageStackLocator object containing metadata, including locations, for stack
	 */
	private ImageStackLocator h;
	
	/**
	 * List containing frames
	 */
	ArrayList<FrameImage> frameimage;
	
	/**
	 * Keyframe for stack
	 */
	ImageProcessor keyframeIm;
	
	/**
	 * Metadata for keyframe image
	 */
	private FileInfo fi;
	
	/**
	 * Creates a CommonKeyFrameStack using the UFMF file and info from ImageStackLocator
	 * 
	 * @param isl			Header containing data and locations for stack images
	 * @param f				UFMF file
	 * @throws IOException
	 */
	public CommonKeyFrameStack(ImageStackLocator isl, UfmfFile f) throws IOException {
		frameimage = new ArrayList<FrameImage>();
		try {
			h = (ImageStackLocator) isl.clone();
			
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		f.seek(isl.loc);
		IplImageHeader iph = new IplImageHeader(f);
		fi = iph.getFileInfo();
		keyframeIm = iph.getImageData(f);
		
		for (int j = 0; j < h.nframes; j++) {
			frameimage.add(f.readFrameImage(keyframeIm, fi));
		}
	}
	
	/**
	 * Returns FileInfo, which is metadata for keyframe image
	 * @return FileInfo
	 */
	public FileInfo getFi() {
		return fi;
	}
	
	/**
	 * Returns the index of the first frame in the stack
	 * @return index of the first frame in the stack
	 */
	public int getStartFrame() {
		return h.getStartFrame();
	}
	
	/**
	 * Returns the index of the last frame in the stack
	 * @return index of the last frame in the stack
	 */
	public int getLastFrame(){
		return h.getLastFrame();
	}
	
	/**
	 * Returns true if the stack contains frame
	 * 
	 * @param frameNumber	The index of the frame
	 * @return true if the stack contains the frame, otherwise false
	 */
	public boolean containsFrame (int frameNumber) {
		return (getStartFrame() <= frameNumber && getLastFrame() >= frameNumber);
	}
	
	/**
	 * Constructs and returns frame
	 * 
	 * @param frameNumber
	 * @return Constructed movie frame (or null, if the frame is not in the stack)
	 * @see FrameImage
	 */
	ImageProcessor getImage(int frameNumber) {
		if (!containsFrame(frameNumber)) {
			return null;
		}
		return frameimage.get(frameNumber - h.getStartFrame()).restoreImage();
	}
	
}
