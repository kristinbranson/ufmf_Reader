package read_ufmf;

import ij.*;
import ij.process.*;

public class UfmfVirtualStack extends VirtualStack {

	private String fileName;
	private String fileDir;
	
	private UfmfFile raf;
	
	private int depth;
	
	private CommonBackgroundStack currentStack;
	
	private ImagePlus imp;
	
	public UfmfVirtualStack(String path, String fileName, String fileDir){
		this.fileName = fileName;
		this.fileDir = fileDir;
		depth = -1;
		
		try {
			raf = new UfmfFile(path, "r");
			raf.parse();
		} catch (Exception e){
			IJ.showMessage("MmfVirtualStack","Opening of: \n \n"+path+"\n \n was unsuccessful.\n\n Error: " +e);
			return;
		}
		
		imp = null;
		
		currentStack = raf.getStackForFrame(1);
		
	}
	
	public boolean fileIsNul() {
		if (raf == null || raf.getNumFrames() == 0 || getProcessor(1)==null) {
			IJ.showMessage("MmfVirtualStack","Error: Frames missing or empty");
			return true;
		}
		return false;
	}
	
	public void addSlice(String name){
		return;
	}
	
	/**
	 * Does nothing
	 */
	public void deleteLastSlice(){
		return;
	}
	
	/**
	 * Does nothing
	 */
	public void deleteSlice(int n){
		return;
	}
	
	public int getBitDepth(){
		if (depth == -1) {
			depth = currentStack.backgroundIm.getBitDepth();
		}
		return depth;
	}
	
	public String getFileName(){
		return fileName;
	}
	
	public ImageProcessor getProcessor(int frameNumber) {
		frameNumber -= 1;
		
		if(frameNumber<0 || frameNumber>=raf.getNumFrames()){
			return null;
		}
		
		if (!currentStack.containsFrame(frameNumber)) {
			currentStack = raf.getStackForFrame(frameNumber);
		}
		
		if (currentStack == null){
			return null;
		}
		return currentStack.getImage(frameNumber);
		
	}
	
	public int getSize() {
		return raf.getNumFrames();
	}
	
	public String getSliceLabel(int n){
		String label = getFileName() + "_Frame_"+n;
		return label;
	}
	
	/**
	 * Does nothing
	 */
	public void setPixels(){
		return;
	}

	public ImagePlus getImagePlus() {
		return imp;
	}

	public void setImagePlus(ImagePlus imp) {
		this.imp = imp;
	}

	public boolean fileIsNull() {
		if (raf == null || raf.getNumFrames()==0 || getProcessor(1)==null){
			IJ.showMessage("UfmfVirtualStack","Error: Frames missing or empty");
			return true;
		}
		return false;
	}
	
	

}
