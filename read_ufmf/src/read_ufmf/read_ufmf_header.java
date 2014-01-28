package read_ufmf;

//import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Math;
//import java.awt.image.*;

//import javax.imageio.ImageIO;


public class read_ufmf_header {

	public static void main(String[] args) throws IOException {
		
		Header header = new Header();
		InputStream is;
		String movie = "movie.ufmf";
		is = new FileInputStream(movie);
		
		// reads in "ufmf"
		header.checkUfmf(is);
		
		if (!header.s.trim().equals("ufmf")) {
			System.out.print("Invalid file format, must be ufmf");
			is.close();
			return;
		
		}
				
		// read in version number
		
		header.getVersion(is);
		
		if (header.ver < 2){
			System.out.print("Only UFMF versions 2-4 are supported");
			is.close();
			return;
		}
		
		// read in location of index
		
		header.getIndexLoc(is);
		
		// read in MaxHeight and MaxWidth
		
		header.getSize(is);
		
		// whether it is fixed size patches: 1
		
		if (header.ver >= 4) {
			header.isFixedSize(is);	
		}
		else {
			header.isfixedsize = 0;
		}
		
		// read in coding length
		
		header.getCoding(is);
		
		// skip to location of index
		
		header.skipToIndex(is);
		
		// create new index for header, read in header
		long checkstart = System.nanoTime();
		Index index = new Index();
		Index.readDict(is);

		System.out.printf("Took %.3f seconds to read index%n", (System.nanoTime() - checkstart) / 1e9);
		
		// populate header fields with vals from index
		
		header.getFrameInfo(index);
		
		
		
		int nmeanscached = Math.min(header.MAXNMEANSCACHED, header.nmeans);
		MeanFrame[] allMeanFrames = new MeanFrame[nmeanscached];
		
		is.close();
		
		for (int i = 0; i < nmeanscached; i++){
			
			long start = System.nanoTime();
			
			allMeanFrames[i] = new MeanFrame(i);
			
			allMeanFrames[i].getImage(movie,i+1,header);
			
			if (i == 0) {
				header.storeMeanSize(allMeanFrames[i]);
			}
			
			System.out.printf("Took %.3f seconds to read image # %,d%n", (System.nanoTime() - start) / 1e9, i);
			
			//BufferedImage theImage = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_RGB);
			
			//for(int y = 0; y<1024; y++){
			//	for(int x = 0; x<1024; x++){
			//		int value = (int) allMeanFrames[i].img[y][x] << 16 | (int) allMeanFrames[i].img[y][x] << 8 | (int) allMeanFrames[i].img[y][x];
			//		theImage.setRGB(x, y, value);
			//	}
			//}
			
			
			//String outputfilename = "meanframe_" + String.valueOf(i)+".bmp";
			//File outputfile = new File(outputfilename);
		    //ImageIO.write(theImage, "png", outputfile);

		}
		
		header.myParse(allMeanFrames,nmeanscached);
		
	}

}

	
