package stevens.cs.cs562.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import stevens.cs.cs562.file.JavaFileWriter;

public class Database {
	private static HashMap<String, String> column_type = null;

	private static String usr = "salesadmin";
	private static String pwd = "admin+123";
	private static String url = "jdbc:postgresql://localhost:5432/sales";
	
	public static String getUsr() {
		return usr;
	}

	public static String getPwd() {
		return pwd;
	}

	public static String getUrl() {
		return url;
	}

	// get column's types
	public static HashMap<String, String> getDBInfo() {

		if (column_type == null) {
			column_type = new HashMap<String, String>();
			/* Load Database Driver */
			try {
				Class.forName("org.postgresql.Driver");
				System.out.println("Success loading Driver!");
			} catch (Exception e) {
				System.out.println("Fail loading Driver!");
				e.printStackTrace();
			}

			// get data type
			try {
				Connection conn = DriverManager.getConnection(url, usr, pwd);
				System.out.println("Success connecting server!");

				Statement stmt = conn.createStatement();
				ResultSet rs = stmt
						.executeQuery("select column_name, data_type from information_schema.columns where table_name='sales'");
				while (rs.next()) {
					if (rs.getString("data_type").indexOf("character") != -1) {
						column_type.put(rs.getString("column_name"), "String");
					} else if (rs.getString("data_type").equals("integer")) {
						column_type.put(rs.getString("column_name"), "int");
					}
				}
			} catch (SQLException e) {
				System.out
						.println("Connection URL or username or password errors!");
				e.printStackTrace();
			}
		}

		return column_type;
	}

	// output database connection string
	public static int outputDBConnection(JavaFileWriter writer,int otabs) throws IOException {
		int tabs = otabs;

		writer.write(tabs, "try {");
		writer.write(tabs + 1, "Class.forName(\"org.postgresql.Driver\");");
		writer.write(tabs + 1,
				"System.out.println(\"Success loading Driver!\");");
		writer.write(tabs, "} catch (Exception e) {");
		writer.write(tabs + 1, "System.out.println(\"Fail loading Driver!\");");
		writer.write(tabs + 1, "e.printStackTrace();");
		writer.write(tabs, "}");

		return tabs;
	}

	public static int outputFetchLoopBegin(JavaFileWriter writer, int otabs) throws IOException {
		int tabs = otabs;

		writer.write(tabs, "try {");
		writer.write(tabs + 1,
				"Connection conn = DriverManager.getConnection(url, usr, pwd);");
		writer.write(tabs + 1,
				"System.out.println(\"Success connecting server!\");");

		writer.write(
				tabs + 1,
				"Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);");
		writer.write(tabs + 1,
				"rs = stmt.executeQuery(\"select * from sales\");");
		writer.write(tabs + 1, "while (rs.next()) {");

		return tabs + 1;
	}

	public static int outputFetchLoopBeginReusable(JavaFileWriter writer,
			int otabs) throws IOException {
		int tabs = otabs;

		writer.write(tabs, "try {");
		writer.write(tabs + 1, "while (rs.next()) {");

		return tabs + 1;
	}

	public static int outputFetchLoopEnd(JavaFileWriter writer, int otabs) throws IOException {
		int tabs = otabs;

		writer.write(tabs, "}");
		writer.write(tabs - 1, "} catch (SQLException e) {");
		writer.write(tabs,
				"System.out.println(\"Connection URL or username or password errors!\");");
		writer.write(tabs, "e.printStackTrace();");
		writer.write(tabs - 1, "}");

		return tabs - 1;
	}
}
