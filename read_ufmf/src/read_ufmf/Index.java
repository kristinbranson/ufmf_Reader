package read_ufmf;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Arrays;

public class Index {
	
	public static FrameIndex frameindex = new FrameIndex();
	public static KeyFrameIndex keyframeindex = new KeyFrameIndex();

	public static void readDict(InputStream is) throws IOException {
		
		String DICT_START_CHAR = "d";
		String chunktype;
		
		byte[] buf = new byte[1024];
		
		is.read(buf,0,1);
		
		chunktype = new String(buf,"UTF-8");
		
		if (!chunktype.trim().equals(DICT_START_CHAR)){
			System.out.printf("Error reading index: dictionary does not start with '%s'.", DICT_START_CHAR);
			}
		
		is.read(buf,1,1);
		
		int nkeys = buf[1];
		
		for (int j = 0; j < nkeys; j++) {
			
			int bytePos = 0;
			
			//read length of key
			is.read(buf,bytePos,1);
			int l = buf[bytePos];
			bytePos += 1;
			
			//read key
			is.read(buf,bytePos,l+1);
			bytePos += (l+1);
			byte [] keyBytes = Arrays.copyOfRange(buf, bytePos-(l+1), bytePos);
			String key = new String(keyBytes,"UTF-8");

			//read chunktype
			is.read(buf,bytePos,1);
			bytePos+=1;
			byte [] chunkBytes = Arrays.copyOfRange(buf, bytePos-1, bytePos);
			chunktype = new String(chunkBytes,"UTF-8");
			
			if (chunktype.trim().equals(DICT_START_CHAR)){
				
				PushbackInputStream pis = new PushbackInputStream(is);
				
				byte[] dbyte = {'d'};
				pis.unread(dbyte);
				is = pis;
				
				if (key.trim().equals("frame")) {
					
					frameindex.readFrameDict(is);
					
				}
				
				else if (key.trim().equals("keyframe")){
					keyframeindex.readKeyFrameDict(is);
					
				}
			}
			
		}
			
	}
	
}
