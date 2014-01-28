package read_ufmf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class FrameIndex {

	public long[] loc;
	public double[] timestamp;
	private String DICT_START_CHAR = "d";
	private String ARRAY_START_CHAR = "a";
	private byte[] buf = new byte[1024];
	private int bytePos = 0;
	public String key;
	
	public void readFrameDict(InputStream is) throws IOException {
			
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
				key = new String(keyBytes,"UTF-8");
				
				//read chunktype
				is.read(buf,bytePos,1);
				bytePos+=1;
				byte [] chunkBytes = Arrays.copyOfRange(buf, bytePos-1, bytePos);
				chunktype = new String(chunkBytes,"UTF-8");
				
				if (chunktype.trim().equals(ARRAY_START_CHAR)){

					is.read(buf,bytePos,1);
					bytePos+=1;
					byte [] dtypeBytes = Arrays.copyOfRange(buf, bytePos-1, bytePos);
					String dtypeString = new String(dtypeBytes,"UTF-8");
					
					char dtypechar = dtypeString.trim().charAt(0);
					String[] javaclass = dtypechar2javaclass(dtypechar);
			
			// get total number of bytes
					is.read(buf,bytePos,4);
					long numbytes = readUnsignedLongLittleEndian(buf,bytePos);
					bytePos += 4;
			
			// get number of bytes per element of datatype "javaclass"
					int bytesperelement = Integer.parseInt(javaclass[1]);
					
			// get number of elements to read, each having number of bytes "bytesperelement"
					int n = (int) (numbytes/bytesperelement);

					long[] lgarray = new long[n];
					double[] dbarray = new double[n];
					
					int count = 0;
					
					while (count < n) {
			
						byte[] tempbuf = new byte[bytesperelement];
						is.read(tempbuf,0,bytesperelement);
						count += 1;
						
						switch(dtypechar) {
				
							case 'q':
								
								ByteBuffer bbq = ByteBuffer.wrap(Arrays.copyOfRange(tempbuf,0,bytesperelement));
								bbq.order(ByteOrder.LITTLE_ENDIAN);
					
								long i = bbq.getLong();
								lgarray[count-1] = i;
								loc = lgarray;
								
							case 'd':
								ByteBuffer bbd = ByteBuffer.wrap(Arrays.copyOfRange(tempbuf, 0, bytesperelement));
								bbd.order(ByteOrder.LITTLE_ENDIAN);
					
								double k = bbd.getDouble();
								dbarray[count-1] = k;
								timestamp = dbarray;
							}
			
						}
					
			}
				
	}
			
	}
	
	private static String[] dtypechar2javaclass(char dtypechar) {
	String javaclass[] = {null,null};
	
	switch(dtypechar) {
	
		case 'c':
		case 's':
		case 'p':	
			javaclass[0] = "char";
			javaclass[1] = "1";
			break;
		case 'b':
			javaclass[0] = "int8";
			javaclass[1] = "1";
			break;
		case 'B':
			javaclass[0] = "uint8";
			javaclass[1] = "1";
			break;
		case 'h':
			javaclass[0] = "int16";
			javaclass[1] = "2";
			break;
		case 'H':
			javaclass[0] = "uint16";
			javaclass[1] = "2";
			break;
		case 'i':
		case 'l':
			javaclass[0] = "int32";
			javaclass[1] = "4";
			break;
		case 'I':
		case 'L':
			javaclass[0] = "uint32";
			javaclass[1] = "4";
			break;
		case 'q':
			javaclass[0] = "int64";
			javaclass[1] = "8";
			break;
		case 'Q':
			javaclass[0] = "uint64";
			javaclass[1] = "8";
			break;
		case 'f':
			javaclass[0] = "float";
			javaclass[1] = "4";
			break;
		case 'd':
			javaclass[0] = "double";
			javaclass[1] = "8";
			break;
		default:
			System.err.printf("Unknown data type %s", dtypechar);
	}
	return javaclass;
	}
	
	private static long readUnsignedLongLittleEndian(byte[] buf, int start) {

		return (long)(buf[start+3] & 0xff) << 24 | (long)(buf[start+2] & 0xff) << 16 |
				(long)(buf[start+1] & 0xff) <<  8 | (long)(buf[start] & 0xff);
	}
}
