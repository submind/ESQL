package stevens.cs.cs562.result;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.sql.Statement;
import java.sql.DriverManager;

public class Dependent {
	public class MFT{
		String prod;
		int month;
		int sum_1_quant;
		int amount_1_quant;
		int sum_2_quant;
		int amount_2_quant;
		int count_3_quant;
	}
	public static void main(String[] args) throws SQLException{
	Dependent instance = new Dependent();
		try {
			Class.forName("org.postgresql.Driver");
			System.out.println("Success loading Driver!");
		} catch (Exception e) {
			System.out.println("Fail loading Driver!");
			e.printStackTrace();
		}
		/*Initialize MFT structure*/
		List<MFT> mft = new LinkedList<MFT>();
		String usr="salesadmin";
		String pwd="admin+123";
		String url="jdbc:postgresql://localhost:5432/sales";
		ResultSet rs = null;
		try {
			Connection conn = DriverManager.getConnection(url, usr, pwd);
			System.out.println("Success connecting server!");
			Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
			rs = stmt.executeQuery("select * from sales");
			while (rs.next()) {
				boolean found = false;
				for(MFT mft_entry:mft){
					if(mft_entry.prod.equals(rs.getString("prod")) && mft_entry.month==(rs.getInt("month")) && rs.getInt("year")==1997){
						found = true;
					}
				}
				if(!found && rs.getInt("year")==1997){
					/*add new entry*/
					MFT new_e = instance.new MFT();
					new_e.prod=rs.getString("prod");
					new_e.month=rs.getInt("month");
					new_e.sum_1_quant=0;
					new_e.amount_1_quant=0;
					new_e.sum_2_quant=0;
					new_e.amount_2_quant=0;
					new_e.count_3_quant=0;
					mft.add(new_e);
				}
			}
		} catch (SQLException e) {
			System.out.println("Connection URL or username or password errors!");
			e.printStackTrace();
		}
		/*Data filling...*/
		rs.beforeFirst();
		try {
			while (rs.next()) {
				for(MFT mft_entry:mft){
					if(rs.getInt("year")==1997 && rs.getString("prod").equals(mft_entry.prod) && mft_entry.month-1==rs.getInt("month")){
						mft_entry.sum_1_quant += rs.getInt("quant");
						mft_entry.amount_1_quant ++;
					}
				}
			}
		} catch (SQLException e) {
			System.out.println("Connection URL or username or password errors!");
			e.printStackTrace();
		}
		rs.beforeFirst();
		try {
			while (rs.next()) {
				for(MFT mft_entry:mft){
					if(rs.getInt("year")==1997 && rs.getString("prod").equals(mft_entry.prod) && mft_entry.month+1==rs.getInt("month")){
						mft_entry.sum_2_quant += rs.getInt("quant");
						mft_entry.amount_2_quant ++;
					}
				}
			}
		} catch (SQLException e) {
			System.out.println("Connection URL or username or password errors!");
			e.printStackTrace();
		}
		rs.beforeFirst();
		try {
			while (rs.next()) {
				for(MFT mft_entry:mft){
					if(rs.getInt("year")==1997 && rs.getString("prod").equals(mft_entry.prod) && mft_entry.month==rs.getInt("month") && rs.getInt("quant")>(mft_entry.amount_1_quant==0?0:mft_entry.sum_1_quant/mft_entry.amount_1_quant) && rs.getInt("quant")<(mft_entry.amount_2_quant==0?0:mft_entry.sum_2_quant/mft_entry.amount_2_quant)){
						mft_entry.count_3_quant ++;
					}
				}
			}
		} catch (SQLException e) {
			System.out.println("Connection URL or username or password errors!");
			e.printStackTrace();
		}
		for(MFT mft_entry:mft){
			{
				System.out.print(mft_entry.prod+"\t");
				System.out.print(mft_entry.month+"\t");
				System.out.print(mft_entry.count_3_quant+"\t");
				System.out.println();
			}
		}
	}
}
