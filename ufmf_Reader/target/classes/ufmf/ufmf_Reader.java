package main.java.ufmf;

import java.io.*;


import ij.*;
import ij.io.*;
import ij.plugin.*;

/**
 * An ImageJ plugin which converts a UFMF file into an ImagePlus and plays the UFMF movie.
 * ImageJ is opened upon running the reader, and a dialog is opened to choose the UFMF file.
 * The movie is stored as a VirtualStack.
 * 
 * @author Austin Edwards
 * @version 1.0
 * @see ImagePlus
 * @see UFMFVirtualStack
 *
 */

public class ufmf_Reader implements PlugIn {

	/**
	 * Extension of VirtualStack containing images from UFMF movie
	 */
	private UfmfVirtualStack vStack;
	
	/**
	 * Full path to the UFMF file
	 */
	private String path;
	
	/**
	 * File name of UFMF file
	 */
	private String fileName;
	
	/**
	 * Director of the UFMF file
	 */
	private String fileDir;
	
	/**
	 * ImagePlus containing the UFMF movie
	 */
	private ImagePlus imp;
	
	public void run(String arg) {
		
		getPath(arg);
		if (null == path) {		// if dialog is cancelled
			return;
		}
		
		//Construct VirtualStack
		try{
			vStack = new UfmfVirtualStack(path, fileName, fileDir);
		} catch(Exception e) {
			IJ.showMessage("ufmf_Reader", "Virtual stack construction was unsuccessful.\n\n Error: "+e);
		}
		
		if (vStack.fileIsNull()) {
			return;
		}
		
		//Construct ImagePlus and add FileInfo
		imp = new ImagePlus(WindowManager.makeUniqueName(fileName), vStack);
		vStack.setImagePlus(imp);
		imp.getCalibration().fps = 10;
		FileInfo fi = new FileInfo();
		fi.directory = fileDir;
		imp.setFileInfo(fi);
		
		//Play movie
		imp.show("Playing Ufmf: " + fileName);
	}
	
	/**
	 * Opens dialog to obtain UFMF file path. Sets parameters fileName, fileDir and path.
	 *
	 * @return String containing path to file
	 */
	private String getPath(String arg) {
		if (null != arg) {
			if (0 == arg.indexOf("http://") || new File(arg).exists()) {
				return arg;
			}
		}
		
		OpenDialog od = new OpenDialog("Choose a .ufmf file", null);
		String dir = od.getDirectory();
		if (null == dir) return null;
		dir = dir.replace('\\','/');
		if (!dir.endsWith("/")) dir += "/";
		fileName = od.getFileName();
		fileDir = dir;
		path = fileDir + fileName;
		return path;
	}
	
	/**
	 * Opens ImageJ and plays the UFMF movie
	*/ 
	public static void main(String[] args) {
		
		Class<?> clazz = ufmf_Reader.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.','/') + ".class").toString();
		String pluginsDir = url.substring(5, url.length() - clazz.getName().length()-6);
		System.setProperty("plugins.dir", pluginsDir);
		
		new ImageJ();
		
		IJ.runPlugIn(clazz.getName(), "");
	}
	
}
