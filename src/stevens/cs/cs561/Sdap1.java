package stevens.cs.cs561;

import java.sql.*;
import java.util.LinkedList;

/*
 * author: Baokun Wang CWID： 10392710
 * 1.
 * This program can be executed in the following way: 
 * $java Sdap1
 * 2.
 * The reason why I use LinkedList to store output results is:
 * The insert operation of LinkedList costs only O(1), and what's more, 
 * not like ArrayList, LinkedList don't need to allocate memory in advance, 
 * it allocates memory only when new data is being inserted.
 * 3.
 * Class Sales_avg_states is used to store the average sale corresponding to customer and product pair in different states.
 * The average number is got from sum number dividing count number.
 * Class Sales_max_min_avg is used to store the maximum, minimum and average sale corresponding to customer and product pair.
 * The average number is got from sum number dividing count number.
 * All of these two classes have toString function used for generating formated output based on the data stored.
 * */

public class Sdap1 {
	/*cust prod ny_sum ny_count nj_sum nj_count ct_sum ct_count*/
	public class Sales_avg_states{
		String cust;// customer name
		String prod;// product name
		long ny_sum; // sum of quantity in NY state
		long ny_count;// count of days 
		long nj_sum;// sum of quantity in NJ state
		long nj_count;//count of days
		long ct_sum;//sum of quantity in CT state
		long ct_count;//count of days
		/*
		 * Average group by state 
		 * CUSTOMER PRODUCT NY_AVG NJ_AVG CT_AVG
		 * ======== ======= ====== ====== ======
		 * Sam 		Egg 	  2893 	  234   1435
		 * Helen 	Cookies    159   2342     56
		 * Bloom 	Butter    3087    923   1512
		 */
		public String toString(){
			return String.format("%1$-9s%2$-8s%3$6d%4$7d%5$7d", cust,prod,
					ny_sum==0?0:ny_sum/ny_count,nj_sum==0?0:nj_sum/nj_count,ct_sum==0?0:ct_sum/ct_count);
		}
	}
	/*cust prod max_q max_date max_st min_q min_date min_st sum count*/
	public class Sales_max_min_avg{
		String cust;// customer name
		String prod;// product name
		long max_q;//maximum quantity 
		String max_date;//the date corresponding to the maximum quantity
		String max_st;//the state corresponding to the maximum quantity
		long min_q;//minimum quantity
		String min_date;//the date corresponding to the minimum quantity
		String min_st;//the state corresponding to the minimum quantity
		long sum;//sum of the products sales to specific customer
		long count;//count to days 
		/*
		 * CUSTOMER  PRODUCT MAX_Q DATE 	  ST MIN_Q DATE 	  ST AVG_Q 
		 * ========= ======= ===== ========== == ===== ========== == ===== 
		 * Bloom	 Pepsi    2893 01/01/1996 NJ    12 09/25/1991 NY  1435 
		 * Sam 		 Milk 	   159 02/15/1992 NJ 	 1 03/23/1994 CT 	56 
		 * Emily 	 Bread 	  3087 07/01/1995 NY 	 2 02/02/1991 NJ  1512 
		 * Knuth 	 Soap 	   234 12/15/1992 CT 	11 04/23/1994 NY   121
		 * */
		public String toString(){
			return String.format("%1$-10s%2$-7s%3$6d%4$11s%5$3s%6$6d%7$11s%8$3s%9$6d", cust,prod,max_q,
					max_date,max_st,min_q,min_date,min_st,sum==0?0:sum/count);
		}
	}

	public static void main(String[] args) {
		String usr = "salesuser";
		String pwd = "sales123";
		String url = "jdbc:postgresql://localhost:5432/sales";

		/* Load Database Driver */
		try {
			Class.forName("org.postgresql.Driver");
			System.out.println("Success loading Driver!");
		} catch (Exception e) {
			System.out.println("Fail loading Driver!");
			e.printStackTrace();
		}

		/*Container used to store output*/
		LinkedList<Sales_avg_states> sass = new LinkedList<Sales_avg_states>();
		LinkedList<Sales_max_min_avg> smmas = new LinkedList<Sales_max_min_avg>();
		/*Sdap1 instance*/
		Sdap1 sdap1 = new Sdap1();
		/* Business logic */
		try {
			Connection conn = DriverManager.getConnection(url, usr, pwd);
			System.out.println("Success connecting server!");

			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM Sales");
			Boolean find = false;
			while (rs.next()) {
				/*Get data from database*/
				String cust = rs.getString("cust");
				String prod = rs.getString("prod");
				int day = rs.getInt("day");
				int month = rs.getInt("month");
				int year = rs.getInt("year");
				//Format the date
				String date = (month<10?"0"+month:month)+"/"+(day<10?"0"+day:day)+"/"+year;//combination of 'month/day/year'
				String state = rs.getString("state");
				long quant = rs.getLong("quant");
				find = false;
				/*
				 * data structure:
				 * cust prod ny_sum ny_count nj_sum nj_count ct_sum ct_count 
				 */
				/*check if it is already stored*/
				for(Sales_avg_states sas: sass){
					if(sas.cust.equals(cust) && sas.prod.equals(prod)){
						find = true;
						if(state.equals("NY")){
							sas.ny_sum += quant;
							sas.ny_count ++;
						}
						if(state.equals("NJ")){
							sas.nj_sum += quant;
							sas.nj_count ++;
						}
						if(state.equals("CT")){
							sas.ct_sum += quant;
							sas.ct_count ++;
						}
					}
				}
				/*if it is a new customer and product pair, then save it*/
				if(!find){
					Sales_avg_states sas = sdap1.new Sales_avg_states();
					sas.cust = cust;
					sas.prod = prod;
					if(state.equals("NY")){
						sas.ny_sum = quant;
						sas.ny_count = 1;
					}
					if(state.equals("NJ")){
						sas.nj_sum = quant;
						sas.nj_count = 1;
					}
					if(state.equals("CT")){
						sas.ct_sum = quant;
						sas.ct_count = 1;
					}
					sass.add(sas);
				}
				
				/* 
				* data structure:
				* cust prod max_q max_date max_st min_q min_date min_st sum count
				*/
				find = false;
				/*check if it is already stored*/
				for(Sales_max_min_avg smma:smmas){
					if(smma.cust.equals(cust) && smma.prod.equals(prod)){
						find = true;
						if(quant > smma.max_q){
							smma.max_q = quant;
							smma.max_date = date;
							smma.max_st = state;
						}
						if(quant < smma.min_q){
							smma.min_q = quant;
							smma.min_date = date;
							smma.min_st = state;
						}
						smma.sum += quant;
						smma.count ++;
					}
				}
				/*if it is a new customer and product pair, then save it*/
				if(!find){
					Sales_max_min_avg smma = sdap1.new Sales_max_min_avg();
					smma.cust = cust;
					smma.prod = prod;
					smma.max_q = quant;
					smma.max_date = date;
					smma.max_st = state;
					smma.min_q = quant;
					smma.min_date = date;
					smma.min_st = state;
					smma.sum = quant;
					smma.count = 1;
					smmas.add(smma);
				}
			}
		} catch (SQLException e) {
			System.out
					.println("Connection URL or username or password errors!");
			e.printStackTrace();
		}
		/*Print outputs*/
		System.out.println("CUSTOMER PRODUCT NY_AVG NJ_AVG CT_AVG");
		System.out.println("======== ======= ====== ====== ======");
		for(Sales_avg_states sas: sass){
			System.out.println(sas.toString());
		}
		System.out.println();
		System.out.println("CUSTOMER  PRODUCT MAX_Q DATE       ST MIN_Q DATE       ST AVG_Q");
		System.out.println("========= ======= ===== ========== == ===== ========== == =====");
		for(Sales_max_min_avg smma:smmas){
			System.out.println(smma.toString());
		}
	}

}
