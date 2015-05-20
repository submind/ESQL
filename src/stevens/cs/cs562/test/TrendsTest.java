package stevens.cs.cs562.test;

import java.io.IOException;

import stevens.cs.cs562.QPE;

public class TrendsTest {

	public static void main(String[] args){
		String fp = "data/Trends.esql", dstDirString = "src/stevens/cs/cs562/result/";
		try {
			QPE.generateJavaFile(fp, dstDirString);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
