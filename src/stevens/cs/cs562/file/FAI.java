package stevens.cs.cs562.file;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import stevens.cs.cs562.db.Database;

public class FAI {
	private List<String> select;
	private int number;
	private List<String> groupby;
	private List<String> fvect;
	private List<String> ori_fvect;
	private List<String> suchthat;
	private String having;
	private String where;

	public String getWhere() {
		return where;
	}

	public FAI(String select, int number, String groupby, String fvect,
			String suchthat, String having, String where) {
		super();
		parseFai(select, number, groupby, fvect, suchthat, having, where);
	}

	public FAI(String fp) {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(fp);
			// load a properties file
			prop.load(input);
			// get the property value and print it out
			parseFai(prop.getProperty("select"),
					Integer.parseInt(prop.getProperty("number")),
					prop.getProperty("groupby"), prop.getProperty("f-vect"),
					prop.getProperty("suchthat"), prop.getProperty("having"),
					prop.getProperty("where"));

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/*
	 * select=cust,avg_1_quant,avg_2_quant,avg_3_quant number=3 groupby=cust
	 * where=year=1997 f-vect=avg_1_quant,avg_2_quant,avg_3_quant
	 * suchthat=t.year==1997 && t.cust.equals(mft_entry.cust) &&
	 * t.state.equals("NY"),t.year==1997 && t.cust.equals(mft_entry.cust) &&
	 * t.state.equals("CT"),t.year==1997 && t.cust.equals(mft_entry.cust) &&
	 * t.state.equals("NJ") having=avg_1_quant>avg_2_quant and
	 * avg_1_quant>avg_3_quant
	 */
	private void parseFai(String select, int number, String groupby,
			String fvect, String suchthat, String having, String where) {
		/* 1.select count,sum,avg,max,min */
		this.select = new ArrayList<String>();
		String[] splits = select.split(",");
		for (String s : splits) {
			// replace divid(/) to decimal result
			s = s.replaceAll("/", "/(double)");
			this.select.add(s);
		}
		/* 2.number */
		this.number = number;
		/* 3.groupby */
		this.groupby = new ArrayList<String>();
		splits = groupby.split(",");
		for (String s : splits) {
			this.groupby.add(s);
		}
		/* added where */
		if (where != null && !where.isEmpty()) {
			// convert where to code, year=1997 => rs.getInt("year")==1997
			where = where.replaceAll(" and ", " && ");
			where = where.replaceAll(" or ", " || ");

			String[] symbols = { "=", "<", ">", "<=", ">=" };

			for (String symbol : symbols) {
				int index = where.indexOf(symbol);
				if (index != -1) {
					String gv = where.substring(0, index);
					String typeString = "Int", operationString = symbol
							.equals("=") ? "==" : symbol;
					if (Database.getDBInfo().get(gv).equals("int")) {
						where = "rs.get" + typeString + "(\"" + gv + "\")"
								+ operationString
								+ where.substring(index + symbol.length());
					}
				}
			}
			this.where = where;
		} else {
			this.where = where;
		}
		/* 4.f-vect */
		this.ori_fvect = new ArrayList<String>();
		this.fvect = new ArrayList<String>();
		splits = fvect.split(",");
		for (String s : splits) {
			// add to original fvect
			this.ori_fvect.add(s);
			// add to new fvect
			String[] s_t = s.split("_");
			if (s_t[0].equals("avg")) {// avg -> sum, amount
				this.fvect.add("sum_" + s_t[1] + "_" + s_t[2]);
				this.fvect.add("amount_" + s_t[1] + "_" + s_t[2]);
			} else {
				this.fvect.add(s);
			}
		}
		/* 5.such that */
		this.suchthat = new ArrayList<String>();
		splits = suchthat.split(",");
		for (String s : splits) {
			// add where clause
			if (this.where != null && !this.where.isEmpty())
				s = this.where + " && " + s;
			this.suchthat.add(s);
		}
		/* 6. having */
		// sample: avg_1_quant>avg_2_quant and avg_1_quant>avg_3_quant and
		// sum_1_quant<sum_2_quant
		if (having != null && !having.isEmpty()) {
			having = having.replaceAll(" and ", " && ");
			having = having.replaceAll(" or ", " || ");
			having = having.replaceAll(" = ", " == ");
			having = having.replaceAll("sum_", "mft_entry.sum_");
			having = having.replaceAll("count_", "mft_entry.count_");
			having = having.replaceAll("avg_", "mft_entry.avg_");
			having = having.replaceAll("max_", "mft_entry.max_");
			having = having.replaceAll("min_", "mft_entry.min_");
			// replace every avg_x_quant to (sum_x_quant/amount_x_quant)
			int index = -1;
			do {
				index = having.indexOf("mft_entry.avg_", index + 1);
				if (index != -1) {
					String s_pre = having.substring(0, index);
					String s_mid = having.substring(
							index + "mft_entry.avg".length(), index
									+ "mft_entry.avg_x_quant".length());
					String s_last = having.substring(index
							+ "mft_entry.avg_x_quant".length());
					having = s_pre + "(mft_entry.amount" + s_mid+"==0?0:"+"(mft_entry.sum" + s_mid+ "/mft_entry.amount" + s_mid + "))" + s_last;
				}
			} while (index != -1);
		}
		this.having = having;

	}

	public List<String> getSelect() {
		return select;
	}

	public int getNumber() {
		return number;
	}

	public List<String> getGroupby() {
		return groupby;
	}

	public List<String> getFvect() {
		return fvect;
	}

	public List<String> getSuchthat() {
		return suchthat;
	}

	public String getHaving() {
		return having;
	}

	public List<String> getOri_fvect() {
		return ori_fvect;
	}

	@Override
	public String toString() {
		return "FAI [select=" + select + ", number=" + number + ", groupby="
				+ groupby + ", fvect=" + fvect + ", suchthat=" + suchthat
				+ ", having=" + having + "]";
	}

}
