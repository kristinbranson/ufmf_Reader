package read_ufmf;

import java.awt.Rectangle;

import ij.process.Blitter;
import ij.process.ImageProcessor;

public class BRISubImage {

	private ImageProcessor ip;
	
	private Rectangle loc;
	
	public BRISubImage(ImageProcessor ip, Rectangle loc) {
		this.ip = ip;
		this.loc = loc;
	}
	
	public void insertIntoImage (ImageProcessor img) {
		img.copyBits(ip, loc.x, loc.y, Blitter.COPY);
	}
}
