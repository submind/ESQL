package stevens.cs.cs562;

import java.io.IOException;
import java.util.HashMap;

import stevens.cs.cs562.db.Database;
import stevens.cs.cs562.file.FAI;
import stevens.cs.cs562.file.JavaFileWriter;
import stevens.cs.cs562.file.MFT;

public class QPE {
	
	public static int outputJavaHeader(JavaFileWriter writer, String className, int otabs) throws IOException{
		int tabs=otabs;
		writer.write(tabs, "package stevens.cs.cs562.result;");
		writer.write(tabs, "import java.sql.Connection;");
		writer.write(tabs, "import java.sql.ResultSet;");
		writer.write(tabs, "import java.sql.SQLException;");
		writer.write(tabs, "import java.util.LinkedList;");
		writer.write(tabs, "import java.util.List;");
		writer.write(tabs, "import java.sql.Statement;");
		writer.write(tabs, "import java.sql.DriverManager;");
		writer.write(tabs, ""); //new line
		
		writer.write(tabs, "public class "+ className +" {");
		
		return tabs;
	}
	
	public static int outputMain(JavaFileWriter writer, int otabs) throws IOException{
		int tabs = otabs;
		writer.write(tabs, "public static void main(String[] args) throws SQLException{");
		return tabs;
	}
	
	public static int outputJavaFooter(JavaFileWriter writer, int otabs) throws IOException{
		int tabs = otabs;
		writer.write(tabs, "}");
		return tabs;
	}
	
	public static int outputComment(JavaFileWriter writer, String comment, int otabs) throws IOException{
		int tabs = otabs;
		
		writer.write(tabs, "/*"+comment+"*/");
		
		return tabs;
	}
	
	public static int outputMFTInitialLoop(JavaFileWriter writer, FAI fai, int otabs) throws IOException{
		int tabs = otabs;
		
		writer.write(tabs, "boolean found = false;");
		writer.write(tabs, "for(MFT mft_entry:mft){");
		int t = 0;
		String s_check_gv = "";
		
		
		//generate condition
		for(String gv: fai.getGroupby()){
			String typeString = "String", operationString = ".equals";
			HashMap<String, String> clumnType = Database.getDBInfo();
			
			if(clumnType.get(gv).equals("int")){
				typeString = "Int";
				operationString = "==";
			}
			
			if(t == 0){
				s_check_gv = "mft_entry."+gv+operationString+"(rs.get"+typeString+"(\""+gv+"\"))";
				t++;
			}else{
				s_check_gv = s_check_gv+" && "+"mft_entry."+gv+operationString+"(rs.get"+typeString+"(\""+gv+"\"))";
			}
		}
		//add where clause
		String whereString = "";
		if(fai.getWhere()!=null && !fai.getWhere().isEmpty())
			whereString = " && " + fai.getWhere();
		s_check_gv = s_check_gv + whereString;
		writer.write(tabs+1, "if("+s_check_gv+"){");
		writer.write(tabs+2, "found = true;");
		writer.write(tabs+1, "}");
		writer.write(tabs, "}");//for loop
		writer.write(tabs, "if(!found"+whereString+"){");
		QPE.outputComment(writer, "add new entry", tabs+1);
		writer.write(tabs+1, "MFT new_e = instance.new MFT();");
		//assign new values to mft object
		for(String gv: fai.getGroupby()){
			String typeString = "String";
			HashMap<String, String> clumnType = Database.getDBInfo();
			
			if(clumnType.get(gv).equals("int")){
				typeString = "Int";
			}
			writer.write(tabs+1, "new_e."+gv+"=rs.get"+typeString+"(\""+gv+"\");");
		}
		for(String fv:fai.getFvect()){
			writer.write(tabs+1, "new_e."+fv+"=0;");
		}
		writer.write(tabs+1, "mft.add(new_e);");
		writer.write(tabs, "}");//if not found
		
		return tabs;
	}
	
	public static int outputMFTUpdateLoop(JavaFileWriter writer,FAI fai, String fvect, int otabs) throws IOException{
		int tabs = otabs;
		writer.write(tabs, "for(MFT mft_entry:mft){");
		//update based on aggregation
		boolean gv_0 = false;
		for(String fv:fai.getOri_fvect()){
			if(fv.indexOf("_0_") != -1)
				gv_0 = true;
		}
		int diff = gv_0?0:1;
		
		String[] tokens = fvect.split("_");//[avg,1,quant]
		//fulfill suchthat condition
		writer.write(tabs+1, "if("+fai.getSuchthat().get(Integer.parseInt(tokens[1])-diff)+"){");
		if(tokens[0].equals("sum")){
			writer.write(tabs+2, "mft_entry."+fvect+" += "+"rs.getInt(\"quant\");");
		}else if (tokens[0].equals("count")) {
			writer.write(tabs+2, "mft_entry."+fvect+" ++;");
		}else if (tokens[0].equals("max")) {
			writer.write(tabs+2, "mft_entry."+fvect+" = Math.max("+"mft_entry."+fvect+",rs.getInt(\"quant\"));");
		}else if (tokens[0].equals("min")) {
			writer.write(tabs+2, "mft_entry."+fvect+" = Math.min("+"mft_entry."+fvect+",rs.getInt(\"quant\"));");
		}else if (tokens[0].equals("avg")){
			String fv = "sum_"+tokens[1]+"_"+tokens[2];
			writer.write(tabs+2, "mft_entry."+fv+" += "+"rs.getInt(\"quant\");");
			fv = "amount_"+tokens[1]+"_"+tokens[2];
			writer.write(tabs+2, "mft_entry."+fv+" ++;");
		}
		
		writer.write(tabs+1, "}");//if end
		writer.write(tabs, "}");//mft loop end
		
		return tabs;
	}
	
	public static int outputMFTCurrentRowOnSelect(JavaFileWriter writer, FAI fai, int otabs) throws IOException {
		int tabs = otabs;
		//print group valuable
		for(String gv: fai.getGroupby()){
			writer.write(tabs, "System.out.print(mft_entry."+gv+"+\"\\t\");");
		}
		//print avg_1_quant
		
		for(String sel:fai.getSelect()){
			if(sel.indexOf("_") == -1)
				continue;
			//repleace all avg to sum/amount
			sel = sel.replaceAll("sum_", "mft_entry.sum_");
			sel = sel.replaceAll("count_", "mft_entry.count_");
			sel = sel.replaceAll("avg_", "mft_entry.avg_");
			sel = sel.replaceAll("max_", "mft_entry.max_");
			sel = sel.replaceAll("min_", "mft_entry.min_");
			//replace every avg_x_quant to (sum_x_quant/amount_x_quant)
			int index = -1;
			do{
				index = sel.indexOf("mft_entry.avg_", index+1);
				if(index != -1){
					String s_pre = sel.substring(0, index);
					String s_mid = sel.substring(index+"mft_entry.avg".length(), index+"mft_entry.avg_x_quant".length());
					String s_last = sel.substring(index+"mft_entry.avg_x_quant".length());
					sel = s_pre+"(mft_entry.amount" + s_mid+"==0?0:"+"(mft_entry.sum" + s_mid+ "/mft_entry.amount" + s_mid + "))"+s_last;
				}
			}while(index != -1);
			
			writer.write(tabs, "System.out.print("+sel+"+\"\\t\");");
			
		}
		writer.write(tabs, "System.out.println();");
		
		return tabs;
	}

	public static void generateJavaFile(String fp, String dstDir) throws IOException {

		// read esql file
		String className = fp.substring(fp.lastIndexOf("/")+1, fp.lastIndexOf("."));
		String javaFileName = dstDir+className+".java";
		JavaFileWriter writer = new JavaFileWriter(javaFileName);
		// parse esql file to get FAI
		FAI fai = new FAI(fp);
		//tabs
		int tabs = 0;
		/*----------1.output java file header----------*/
		QPE.outputJavaHeader(writer, className, tabs);
		
		// define MFStructure
		
		HashMap<String, String> clumnType = Database.getDBInfo();
		
		MFT mft = new MFT(fai, clumnType);
		/*----------2.output MFT structure----------*/
		tabs = tabs + 1;
		mft.outputMFT(writer, tabs);
		
		/*----------3.output main ----------*/
		QPE.outputMain(writer, tabs);
		writer.write(tabs, className+" instance = new "+className+"();");
		tabs = tabs + 1;
		/*----------4.output database connection----------*/
		Database.outputDBConnection(writer, tabs);
		
		/*----------5.Initialize MFT structure----------*/
		QPE.outputComment(writer, "Initialize MFT structure", tabs);
		writer.write(tabs, "List<MFT> mft = new LinkedList<MFT>();");
		writer.write(tabs, "String usr=\""+Database.getUsr()+"\";");
		writer.write(tabs, "String pwd=\""+Database.getPwd()+"\";");
		writer.write(tabs, "String url=\""+Database.getUrl()+"\";");
		writer.write(tabs, "ResultSet rs = null;");
		int looptabs = tabs;
		Database.outputFetchLoopBegin(writer,  tabs);
		QPE.outputMFTInitialLoop(writer, fai, tabs+2);
		Database.outputFetchLoopEnd(writer, tabs+1);
		
		/*----------6.MFT structure data fill----------*/
		QPE.outputComment(writer, "Data filling...", tabs);
		tabs = looptabs;
		//each fvect has a while loop
		for(String fv:fai.getOri_fvect()){
			writer.write(tabs, "rs.beforeFirst();");
			Database.outputFetchLoopBeginReusable(writer,tabs);
			QPE.outputMFTUpdateLoop(writer, fai, fv, tabs+2);
			Database.outputFetchLoopEnd(writer,tabs+1);
		}
		
		/*----------7.output mftable structure based on having condition----------*/
		writer.write(tabs, "for(MFT mft_entry:mft){");
		String havingConditionString = fai.getHaving();
		if(havingConditionString != null && !havingConditionString.isEmpty())
			writer.write(tabs+1, "if("+havingConditionString+"){");
		else
			writer.write(tabs+1, "{");
		QPE.outputMFTCurrentRowOnSelect(writer, fai, tabs+2);
		writer.write(tabs+1, "}");//if end
		writer.write(tabs, "}");//for end
		
		/*----------8.output java file footer----------*/
		tabs = 1;
		QPE.outputJavaFooter(writer, tabs);
		tabs = 0;
		QPE.outputJavaFooter(writer, tabs);
		/*----------9. close writer to output buffer----------*/
		writer.close();
		/*----------finished----------*/
		System.out.println("Generation finished.");
	}

}
