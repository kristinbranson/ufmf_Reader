package read_ufmf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import ij.IJ;
import ij.io.FileInfo;
import ij.process.ImageProcessor;

public class CommonBackgroundStack {

	private ImageStackLocator h;
	
	ArrayList<BackgroundRemovedImage> bri;
	
	ImageProcessor backgroundIm;
	
	private FileInfo fi;
	
	public CommonBackgroundStack(ImageStackLocator isl, UfmfFile f) throws IOException {
		bri = new ArrayList<BackgroundRemovedImage>();
		try {
			h = (ImageStackLocator) isl.clone();
			
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		f.seek(isl.loc);
		IplImageHeader iph = new IplImageHeader(f);
		fi = iph.getFileInfo();
		backgroundIm = iph.getImageData(f);
		
		for (int j = 0; j < h.nframes; j++) {
			
			bri.add(f.readBRI(backgroundIm, fi));
		}
	}
	
	public FileInfo getFi() {
		return fi;
	}
	
	public int getStartFrame() {
		return h.getStartFrame();
	}
	
	public int getLastFrame(){
		return h.getLastFrame();
	}
	
	public boolean containsFrame (int frameNumber) {
		return (getStartFrame() <= frameNumber && getLastFrame() >= frameNumber);
	}
	
	ImageProcessor getImage(int frameNumber) {
		if (!containsFrame(frameNumber)) {
			return null;
		}
		return bri.get(frameNumber - h.getStartFrame()).restoreImage();
	}
	
}
