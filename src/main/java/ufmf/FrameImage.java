package main.java.ufmf;

import ij.process.ImageProcessor;
import java.util.ArrayList;


/**
 * Contains keyframe image and frame subimages to reconstruct frame
 * 
 * @author Austin Edwards
 * @version 1.0
 * @see FrameImageHeader
 * @see FrameSubImage
 *
 */

@SuppressWarnings("unused")
public class FrameImage {

	/**
	 * A list of all the frame subimages needed
	 */
	private ArrayList<FrameSubImage> subimg;
	
	/**
	 * Keyframe image for this frame
	 */
	private ImageProcessor keyframeIm;
	
	/**
	 * Header containing metadata for this frame
	 */
	private FrameImageHeader header;
	
	/**
	 * Creates a frame image
	 * 
	 * @param header		header for this frame
	 * @param keyframeIm	keyframe image for this frame
	 */
	
	public FrameImage(FrameImageHeader header,
			ImageProcessor keyframeIm) {
		this.keyframeIm = keyframeIm;
		this.header = header;
		subimg = new ArrayList<FrameSubImage>();
	}
	
	/**
	 * Adds an image to the list of subimages
	 * 
	 * @param img 
	 */
	public void addSubImage (FrameSubImage img) {
		subimg.add(img);
	}
	
	/**
	 * Constructs movie frame from the keyframe image and the subimages
	 * 
	 * @return Constructed frame
	 */
	public ImageProcessor restoreImage() {
		ImageProcessor ip = keyframeIm.duplicate();
		for (int j = 0; j<subimg.size(); j++) {
			subimg.get(j).insertIntoImage(ip);
		}
		return ip;
	}
}
