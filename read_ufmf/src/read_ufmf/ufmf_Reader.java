package ufmf;

import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import ufmf.UfmfFile.Frame;
import ufmf.UfmfFile.MeanFrame;

public class ufmf_Reader {

	public static void main(String args[]) throws IOException {

		String path = "/Users/edwardsa/workspace/ufmf/movie.ufmf";
		UfmfFile raf = new UfmfFile(path,"r");
		raf.header = raf.readFileHeader();
		MeanFrame[] meanframes = raf.readAllMeans(raf.header);
		int i = 13047;
		Frame frame = raf.getFrame(raf.header,i);
		frame.readFrame(raf.header, meanframes);

			BufferedImage theImage = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_RGB);
			
			for(int y = 0; y<1024; y++){
				for(int x = 0; x<1024; x++){
					
					int value = (int) frame.img[y][x] << 16 | (int) frame.img[y][x] << 8 | (int) frame.img[y][x];
						theImage.setRGB(x, y, value);
				}
			}

			String outputfilename = "frame" + String.valueOf(i)+".bmp";
			File outputfile = new File(outputfilename);
			ImageIO.write(theImage, "png", outputfile);
	}	
}