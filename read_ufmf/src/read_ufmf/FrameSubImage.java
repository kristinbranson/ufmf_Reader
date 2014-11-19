package read_ufmf;

import java.awt.Rectangle;

import ij.process.Blitter;
import ij.process.ImageProcessor;

/**
 * Subimage with associated coordinates within frame
 * 
 * @author Austin Edwards
 *
 */
public class FrameSubImage {

	/**
	 * Subimage
	 */
	private ImageProcessor ip;
	
	/**
	 * Coordinates of subimage within frame
	 */
	private Rectangle loc;
	
	/**
	 * Creates FrameSubImage
	 * 
	 * @param ip
	 * @param loc
	 */
	public FrameSubImage(ImageProcessor ip, Rectangle loc) {
		this.ip = ip;
		this.loc = loc;
	}
	
	/**
	 * Inserts subimage into frame image at appropriate location
	 * @param img		frame image
	 * @see FrameImage
	 */
	public void insertIntoImage (ImageProcessor img) {
		img.copyBits(ip, loc.x, loc.y, Blitter.COPY);
	}
}
