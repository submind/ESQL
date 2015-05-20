package stevens.cs.cs562.test;

import java.io.IOException;

import stevens.cs.cs562.QPE;

public class MediansTest {

	public static void main(String[] args){
		String fp = "data/Medians.esql", dstDirString = "src/stevens/cs/cs562/result/";
		try {
			QPE.generateJavaFile(fp, dstDirString);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
