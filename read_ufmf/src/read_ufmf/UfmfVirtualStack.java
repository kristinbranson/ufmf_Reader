package read_ufmf;

import ij.*;
import ij.process.*;

/**
 * Extension of ImageJ VirtualStack that allows UFMF file to be accessed without loading all
 * images into memory
 * 
 * @author Austin Edwards
 * @version 1.0
 * @see VirtualStack
 * @see UfmfFile
 * @see CommonKeyFrameStack
 *
 */
public class UfmfVirtualStack extends VirtualStack {

	/**
	 * Name of UFMF file
	 */
	private String fileName;
	
	/**
	 * Directory containing UFMF file
	 */
	private String fileDir;
	
	/**
	 * UFMF file associated with stack
	 */
	private UfmfFile raf;
	
	/**
	 * Image depth
	 */
	private int depth;
	
	/**
	 * Segment of movie currently loaded into memory
	 */
	private CommonKeyFrameStack currentStack;
	
	/**
	 * ImagePlus to be initialized
	 */
	private ImagePlus imp;
	
	/**
	 * Creates UfmfVirtualStack by reading given UFMF file
	 * 
	 * @param path		path to UFMF file
	 * @param fileName	name of UFMF file
	 * @param fileDir	directory containing UFMF file
	 */
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
	
	/**
	 * Does nothing
	 */
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
	
	/**
	 * Returns depth of current stack
	 * 
	 * @return depth of current stack
	 */
	public int getBitDepth(){
		if (depth == -1) {
			depth = currentStack.keyframeIm.getBitDepth();
		}
		return depth;
	}
	
	/**
	 * Returns file name of UFMF file
	 * 
	 * @return String containing name of file
	 */
	public String getFileName(){
		return fileName;
	}
	
	/**
	 * Returns directory containing UFMF file
	 * 
	 * @return String of directory containing file
	 */
	public String getDirectory(){
		return fileDir;
	}
	
	/**
	 * Returns ImageProcessor for given frame number
	 * Overrides method in ImageStack
	 * 
	 * @return ImageProcessor
	 */
	public ImageProcessor getProcessor(int frameNumber) {
		frameNumber -= 1;
		
		if(frameNumber<0 || frameNumber>=raf.getNumFrames()){
			return null;
		}
		
		// Checks to see if frame is in current stack
		if (!currentStack.containsFrame(frameNumber)) {
			currentStack = raf.getStackForFrame(frameNumber);
		}
		
		if (currentStack == null){
			return null;
		}
		return currentStack.getImage(frameNumber);
		
	}
	
	/**
	 * Returns number of frames in UFMF file
	 * 
	 * @return integer of frames in UFMF file
	 */
	public int getSize() {
		return raf.getNumFrames();
	}
	
	/**
	 * Returns frame label
	 * 
	 * @return String frame label
	 */
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

	/**
	 * Returns ImagePlus object
	 * 
	 * @return returns ImagePlus
	 */
	public ImagePlus getImagePlus() {
		return imp;
	}

	/**
	 * Sets ImagePlus value of current instance of UFMFVirtualStack to current ImagePlus value
	 * 
	 * @param imp	ImagePlus
	 */
	public void setImagePlus(ImagePlus imp) {
		this.imp = imp;
	}

	/**
	 * Returns true if UFMF file is null, false otherwise
	 * 
	 * @return true if UFMF file is null
	 */
	public boolean fileIsNull() {
		if (raf == null || raf.getNumFrames()==0 || getProcessor(1)==null){
			IJ.showMessage("UfmfVirtualStack","Error: Frames missing or empty");
			return true;
		}
		return false;
	}
	
	

}
