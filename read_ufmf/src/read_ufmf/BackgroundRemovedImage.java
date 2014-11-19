package read_ufmf;

import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.Map;

@SuppressWarnings("unused")
public class BackgroundRemovedImage {

	private ArrayList<BRISubImage> subimg;
	
	private ImageProcessor backgroundImg;
	
	private BackgroundRemovedImageHeader bgheader;
	
	public BackgroundRemovedImage(BackgroundRemovedImageHeader bgheader,
			ImageProcessor backgroundImg) {
		this.backgroundImg = backgroundImg;
		this.bgheader = bgheader;
		subimg = new ArrayList<BRISubImage>();
	}
	
	public void addSubImage (BRISubImage img) {
		subimg.add(img);
	}
	
	public ImageProcessor restoreImage() {
		ImageProcessor ip = backgroundImg.duplicate();
		for (int j = 0; j<subimg.size(); j++) {
			subimg.get(j).insertIntoImage(ip);
		}
		return ip;
	}
}
