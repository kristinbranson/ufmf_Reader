package read_ufmf;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Arrays;

public class KeyFrameIndex {

	private String DICT_START_CHAR = "d";
	private byte[] buf = new byte[1024];
	private int bytePos = 0;
	
	public FrameIndex meanindex = new FrameIndex();
	
	public void readKeyFrameDict(InputStream is) throws IOException {
			
			is.read(buf,bytePos,1);
			bytePos+=1;
			
			String chunktype = new String(buf,"UTF-8");
			
			if (!chunktype.trim().equals(DICT_START_CHAR)){
				System.out.printf("Error reading index: dictionary does not start with '%s'.", DICT_START_CHAR);
				}
			
			// get number of keys in frames
			
			is.read(buf,bytePos,1);
			int nkeys = buf[bytePos];
			
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
				//System.out.println(chunktype);
				
				if (chunktype.trim().equals(DICT_START_CHAR)){
					
					PushbackInputStream pis = new PushbackInputStream(is);
					
					byte[] dbyte = {'d'};
					pis.unread(dbyte);
					is = pis;
					
					if (key.trim().equals("mean")) {
						
						meanindex.readFrameDict(is);
					
					}
				}
			}
	}
}
