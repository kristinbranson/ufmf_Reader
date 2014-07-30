package read_ufmf;

import java.io.*;

import read_ufmf.UfmfFile.MeanFrame;
import ij.io.OpenDialog;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.process.FloatProcessor;
import ij.IJ;

//public class ufmf_Reader {
public class ufmf_Reader implements PlugIn {

	private String path;
	private String fileName;
	private String fileDir;
	
	public void run(String arg) {

		getPath(arg);
		if (null == path) {
			return;
		}

		try {
			UfmfFile raf = new UfmfFile(path,"r");
			raf.parse();
			MeanFrame[] MeanFrames = raf.readAllMeans(raf.header);
			int depth = raf.header.nframes;
			ImageStack stack = new ImageStack(raf.header.max_width, raf.header.max_height);
			ImageProcessor ip = null;
			
			
	
			for (int z = 1000; z < 1020; z++) {
				
				float[][] slice = new float[raf.header.max_height][raf.header.max_width];
				slice = raf.getFrame(raf.header, z, MeanFrames).img;
				
				ip = new FloatProcessor(slice);
			
				IJ.showProgress( z, depth );
				IJ.showStatus( "Reading: " + z + "/" + depth );
				stack.addSlice( null, ip );
			}
			
			ImagePlus imp = new ImagePlus( fileName.replaceAll( ".ufmf$", "" ), stack );
			imp.setDisplayRange(0.0,255.0 );
			imp.show();
			
		} catch ( Exception e )
		{
			System.out.println(e.getMessage());
			IJ.error( "Opening ufmf failed.\n" + e.getMessage() );
		}
		
		
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
		dir = dir.replace('\\', '/'); // for Windows
		if (!dir.endsWith("/")) dir += "/";
		fileName = od.getFileName();
		fileDir = dir;
		path = fileDir + fileName;
		return path;
	}
	
	public static void main(String[] args) {
        // set the plugins.dir property to make the plugin appear in the Plugins menu
        Class<?> plugin_class = ufmf_Reader.class;
        String url = plugin_class.getResource("/" + plugin_class.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring(5, url.length() - plugin_class.getName().length() - 6);
        System.setProperty("plugins.dir", pluginsDir);

        // start ImageJ
        new ImageJ();
        // run the plugin
        IJ.runPlugIn(plugin_class.getName(), "");
	}
}