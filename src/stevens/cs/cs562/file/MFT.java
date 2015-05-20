package stevens.cs.cs562.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MFT {
	
	private List<String> lines;
	
	public MFT(FAI fai,HashMap<String, String> columnType){
		
		lines = new ArrayList<String>();
		
		lines.add("public class MFT{");
		for(String gv:fai.getGroupby()){
			lines.add("\t"+columnType.get(gv)+" "+gv+";");
		}
		
		for(String fv:fai.getFvect()){
			lines.add("\t"+columnType.get(fv.substring(fv.lastIndexOf("_")+1))+" "+fv+";");
		}
		
		lines.add("}");
		
		//select=[cust, avg_1_quant, avg_2_quant, avg_3_quant], 
		//number=3, groupby=[cust], 
		//fvect=[sum_1_quant, amount_1_quant, sum_2_quant, amount_2_quant, sum_3_quant, amount_3_quant], 
		//suchthat=[t.year==1997 && t.cust.equals(mft_entry.cust) && t.state.equals("NY"), t.year==1997 && t.cust.equals(mft_entry.cust) && t.state.equals("CT"), t.year==1997 && t.cust.equals(mft_entry.cust) && t.state.equals("NJ")], 
		//having=avg_1_quant>avg_2_quant && avg_1_quant>avg_3_quant]
		
	}
	
	public List<String> getMFT(){
		return lines;
	}
	
	public int outputMFT(JavaFileWriter writer, int otabs) throws IOException{
		int tabs = otabs;
		
		for(String s:lines){
			writer.write(tabs, s);
		}
		
		return tabs;
	}
}
