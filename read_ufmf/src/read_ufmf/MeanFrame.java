package read_ufmf;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class MeanFrame {
	
	public int meani;
	public int framei;
	public boolean dopermute;
	public float[][] img;
	public short[] sz = {0,0};
	public double timestamp;
	public String Name;
	
	public MeanFrame(int i) {
		
		this.Name = "MeanFrame" + String.valueOf(i);
		
	}
	
	byte[] meanbuf = new byte[24];
	
	public void getImage(String movie, int i, Header header) throws IOException{
		
		InputStream is = new FileInputStream(movie);
		
		is.skip(header.mean2file[i-1]);
		
		int KEYFRAME_CHUNK = 0;
		String MEAN_KEYFRAME_TYPE = "mean";
		
		int chunktype = is.read();

		if (chunktype != KEYFRAME_CHUNK){
			System.err.println("Expecting keyframe chunk");
		}
		
		is.read(meanbuf,0,5);
		
		String keyframetype = new String(meanbuf,"UTF-8");
		
		if (!keyframetype.trim().equals(MEAN_KEYFRAME_TYPE)) {
			System.err.printf("Expected keyframetype %s at start of mean keyframe");
		}
		
		is.read(meanbuf,6,1);
				
		byte [] datatypeBytes = Arrays.copyOfRange(meanbuf, 6, 7);
		String datatype = new String(datatypeBytes,"UTF-8");
		
		String[] javaclass = {null,null};
		
		if (datatype.trim().equals("f")) {
			javaclass[0] = "float";
			javaclass[1] = "4";
		};
		
		is.read(meanbuf,7,4);
		
		sz[0] = Header.readShortLittleEndian(meanbuf,7);
		sz[1] = Header.readShortLittleEndian(meanbuf,9);
		
		int height = sz[0];
		int width = sz[1];
		
		is.read(meanbuf,11,8);
		
		ByteBuffer bbd = ByteBuffer.wrap(Arrays.copyOfRange(meanbuf, 11, 19));
		bbd.order(ByteOrder.LITTLE_ENDIAN);
		
		timestamp = bbd.getDouble();
		
		int bytesperelement = Integer.parseInt(javaclass[1]);
		byte[] meanframebuf = new byte[bytesperelement];
		float[][] fltarray = new float[height*header.bytes_per_pixel][width*header.bytes_per_pixel];
		
		for (int row = 0; row < height; row++){
			for (int col = 0; col < width; col++){
				
				is.read(meanframebuf,0,bytesperelement);
			
				ByteBuffer bbf = ByteBuffer.wrap(Arrays.copyOfRange(meanframebuf,0,bytesperelement)).order(ByteOrder.LITTLE_ENDIAN);

				fltarray[row][col] = bbf.getFloat();
			}
		}
		
		img = fltarray;
		is.close();
	}

}
