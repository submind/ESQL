package stevens.cs.cs562.file;

import java.io.FileWriter;
import java.io.IOException;

public class JavaFileWriter {
	private FileWriter fw = null;

	public JavaFileWriter(FileWriter fw) {
		super();
		this.fw = fw;
	}
	
	public JavaFileWriter(String path) throws IOException{
		this.fw = new FileWriter(path);
	}
	
	//write code string with front tabs
	public void write(int numOfTabs, String code) throws IOException{
		if(fw == null) return;
		
		String newLine = System.getProperty("line.separator");


		for(int i=0;i<numOfTabs;i++)
		{
		    fw.write("\t");
		}
		
		fw.write(code+newLine);
	}
	//close write stream
	public void close() throws IOException{
		if(fw != null)
			fw.close();
	}
}
