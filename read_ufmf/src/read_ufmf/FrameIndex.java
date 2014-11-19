package read_ufmf;

import ucar.unidata.io.RandomAccessFile;

import java.io.IOException;


/**
 * 
 * 
 * @author edwardsa
 *
 */
public class FrameIndex {

	public long[] loc;
	public double[] timestamp;
	private String DICT_START_CHAR = "d";
	private String ARRAY_START_CHAR = "a";
	public String key;
	
	public FrameIndex(RandomAccessFile raf) throws IOException {
			
			String chunktype = Character.toString((char) raf.read());
			
			if (!chunktype.trim().equals(DICT_START_CHAR)){
				System.out.printf("Error reading index: dictionary does not start with '%s'.", DICT_START_CHAR);
				}
			
			// get number of keys in frames
			
			int nkeys = raf.read();
			
			for (int j = 0; j < nkeys; j++) {
				
				byte[] buf = new byte[24];
				//read length of key
				int l = raf.read();

				//read key
				
				raf.read(buf,0,l+1);
				key = new String(buf,"UTF-8");
				
				//read chunktype
				chunktype = Character.toString((char) raf.read());
				
				if (chunktype.trim().equals(ARRAY_START_CHAR)) {
					
					char dtype = (char) raf.read();
					String[] javaclass = dtypechar2javaclass(dtype);
					
			// get total number of bytes
					long numbytes = raf.readInt() & 0xFFFFFFFFL;
					
			// get number of bytes per element of datatype "javaclass"
					int bytesperelement = Integer.parseInt(javaclass[1]);
					
			// get number of elements to read, each having number of bytes "bytesperelement"
					int n = (int) (numbytes/bytesperelement);
					
			// initialize size of loc and timestamp arrays		
					switch(dtype) {
						case 'q': loc = new long[n];
						case 'd': timestamp = new double[n];
					}
					
			// read in index		
					
					if (dtype == 'q') {
						byte[] longbuf = new byte[(int) numbytes];
						raf.read(longbuf);
						for(int i = 0; i < numbytes; i+=bytesperelement) {
							loc[(i/bytesperelement)] = readUnsignedLongLittleEndian(longbuf,i);	
						}	
					}
					else if (dtype == 'd') {
						for(int i = 0; i < numbytes; i+=bytesperelement) {
							double k = raf.readDouble();
							timestamp[(i/bytesperelement)] = k;
						}
					}
				}
			}
	}
			
	private long readUnsignedLongLittleEndian(byte[] buf, int start) {

		return (long)(buf[start+7] & 0xff) << 56 | (long)(buf[start+6] & 0xff) << 48 |
				(long)(buf[start+5] & 0xff) << 40 | (long)(buf[start+4] & 0xff) << 32 |
				(long)(buf[start+3] & 0xff) << 24 | (long)(buf[start+2] & 0xff) << 16 |
				(long)(buf[start+1] & 0xff) <<  8 | (long)(buf[start] & 0xff);
			}
	
	/**
	 * Translates encoded data type to Java data type
	 * 
	 * @param dtype
	 * @return String array containing two elements: data type and bytes per pixel
	 */

	private String[] dtypechar2javaclass(char dtype) {
	
		String javaclass[] = {null,null};
	

			switch(dtype) {
		
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
				System.err.printf("Unknown data type %s", dtype);
			}
			return javaclass;
	}
	}