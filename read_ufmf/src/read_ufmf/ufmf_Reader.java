package read_ufmf;

import java.io.*;


import ij.*;
import ij.io.*;
import ij.plugin.*;

public class ufmf_Reader implements PlugIn {

	private UfmfVirtualStack vStack;
	
	private String path;
	
	private String fileName;
	
	private String fileDir;
	
	private ImagePlus imp;
	
	public void run(String arg) {
		
		getPath(arg);
		if (null == path) {
			return;
		}
		
		try{
			vStack = new UfmfVirtualStack(path, fileName, fileDir);
		} catch(Exception e) {
			IJ.showMessage("ufmf_Reader", "Virtual stack construction was unsuccessful.\n\n Error: "
					+e);
		}
		
		if (vStack.fileIsNull()) {
			return;
		}
		
		imp = new ImagePlus(WindowManager.makeUniqueName(fileName), vStack);
		vStack.setImagePlus(imp);
		imp.getCalibration().fps = 10;
		FileInfo fi = new FileInfo();
		fi.directory = fileDir;
		imp.setFileInfo(fi);
		
		imp.show("Playing Ufmf: " + fileName);
	}
	
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
	
	public static void main(String[] args) {
		
		Class<?> clazz = ufmf_Reader.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.','/') + ".class").toString();
		String pluginsDir = url.substring(5, url.length() - clazz.getName().length()-6);
		System.setProperty("plugins.dir", pluginsDir);
		
		new ImageJ();
		
		IJ.runPlugIn(clazz.getName(), "");
	}
	
}
