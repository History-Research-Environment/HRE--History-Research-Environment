package hre.dbla;
/*************************************************************************************
 * DatabaseH2handler handles data transfer to/from H2 database
 * ************************************************************************************
 * NOTE preliminary version implemented
 * and only basic database functions implemented
 * - Open database connection
 * - Close database connection
 * - Request SQL data
 * - Update SQL data table
 ***************************************************************************************
 * v0.00.0016 2019-12-20 - first version by N. Tolleshaug
 * v0.00.0017 2020-02-14 - added no empty database parameter in connect (N. Tolleshaug)
 * v0.00.0017 2020-03-19 - Added getDatabaseConnectPath() to get database file path
 * 						   Removed changeSQLdata - not used as handled through ResultSet
 * v0.00.0026 2021-05-27 - Added code for connecting to server (N. Tolleshaug)
 * 			  2021-05-28 - Added code for commit command (N. Tolleshaug)
 * 			  2021-10-01 - Handling of local/remote connect to database (N. Tolleshaug)
 * v0.00.0027 2022-05-19 - Added code list of table names (N. Tolleshaug)
 * *************************************************************************************
 */

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import hre.gui.HGlobal;
import hre.tmgjava.HCException;
import hre.tmgjava.TMGglobal;


/**
 * DatabaseH2handler implements interface HDDatabaseSQLhandler
 * Handles data transfer to/from H2 database
 * @author Nils Tolleshaug
 * @version 2019-12-20
 *
 */
public class HDDatabaseH2handler implements  HDDatabaseSQLhandler {

	Connection dataH2conn = null;
	String connectString;
	boolean remote;

/**
 * User H2 database login data
 */
	static String userId = "";
	static String passWord = "";

/**
 * Opens connection to H2 database - Constructor
 * @param urlH2loc file system location for H2 database
 * @throws HDException
 */
	public HDDatabaseH2handler(boolean remote, String urlH2loc, String[] logonData, HDServerH2tcp tcpServ) throws HDException {
		super();
		this.remote = remote;

		if (remote) {
			boolean isUpdatable = false;
			try {
				//tcpServ = new HDServerH2tcp(); //create a new server object
				dataH2conn = tcpServ.remoteConnect(urlH2loc,logonData);
				dataH2conn.commit();

				DatabaseMetaData metadata;
				metadata = dataH2conn.getMetaData();
		        isUpdatable = metadata.supportsResultSetConcurrency(
		                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		        System.out.println(" Remote connected: " + urlH2loc);

			} catch (SQLException sqle) {
				System.out.println(" Remote updateable check failed: " + sqle.getMessage());
				sqle.printStackTrace();
			}

	        if (!isUpdatable) {
	        	System.out.println("The remote database does not support updatable result sets.");
	        }


		} else try {

    		Class.forName("org.h2.Driver");

    		userId = logonData[1];
    		passWord = logonData[2];
/**
 * If urlH2loc points to a non exiting database - ;IFEXISTS=TRUE
 * disable creation of a new empty H2 database
 */
    		connectString = "jdbc:h2:" + urlH2loc + ";IFEXISTS=TRUE";
    		dataH2conn =  DriverManager.getConnection(connectString, userId, passWord);
    	} catch(SQLException sqle) {
    		closeSQLconnect();
    		throw new HDException("HDDatabaseH2handler - Connect SQL error:\n" + sqle.getMessage());
    	} catch(ClassNotFoundException clnf) {
    		throw new HDException("Class Not Found error:\n" + clnf.getMessage());
    	}
	}


/**
 * boolean ifTableExist(String tablename)
 * @param tablename
 * @return
 * @throws HDException
 * @throws SQLException
 */
	@Override
	public boolean ifTableExist(String tableName) throws HDException {
		try {
    		DatabaseMetaData dataBaseMetadata = dataH2conn.getMetaData();

    	// check if table name table is there
    			ResultSet tables = dataBaseMetadata.getTables(null, null, tableName, null);
    			if (tables.next()) return true; else return false;

		} catch(SQLException sqle) {
			if (HGlobal.DEBUG) System.out.println("Close H2 Database error" + sqle.getMessage());
			throw new HDException("Close H2 Database error: \n" + sqle.getMessage());
		}
	}

	public Connection getConnection() {
		return dataH2conn;
	}

/**
 * getDatabaseConnectPath()
 * @return connect string
 * @author NTo
 */
	@Override
	public String getDatabaseConnectPath() {
		return connectString;
	}

/**
 * commitUpdates() of database
 * @throws SQLException
 */
	@Override
	public void commitUpdates() throws SQLException {
		dataH2conn.commit();
	}

/**
 * Close connection to H2 database
 * @throws HDException
 */
	@Override
	public void closeSQLconnect() throws HDException {
		try {
			if (dataH2conn != null) {

		// Attempt to disconnect remote database
				if (remote) dataH2conn.createStatement().execute("SHUTDOWN");
        // Normal close
				dataH2conn.commit();
				dataH2conn.close();
			}
			dataH2conn = null;
		} catch(SQLException exc) {
			if (HGlobal.DEBUG) System.out.println("Close H2 Database error: " + exc.getMessage());
			throw new HDException("Close H2 Database error: \n" + exc.getMessage());
		}
	}

/**
 * Request data from H2 database
 * @param sqlRequest SQL request string
 * @return ResultSet - data returned from SQL request to H2 database
 * @throws HDException
 */
	@Override
	public ResultSet requestSQLdata(String sqlRequest) throws HDException {
    	try {
			java.sql.Statement stmt = dataH2conn.createStatement(
	    			ResultSet.TYPE_SCROLL_INSENSITIVE,
	                ResultSet.CONCUR_UPDATABLE);
			//System.out.println(" SQL request: " + sqlRequest);
	    	return stmt.executeQuery(sqlRequest);
		} catch(SQLException exc) {
			if (HGlobal.DEBUG) System.out.println("HDDatabaseH2handler - requestSQLdata error! " + exc.getMessage());
			throw new HDException("HDDatabaseH2handler - Request record SQL error: \n" + exc.getMessage());
		}
	}


/**
 * Update record in SQL database
 * @param sqlRequest SQL update string
 * @return boolean success
 * @throws HDException
 */
	@Override
	public boolean changeSQLdata(String sqlRequest) throws HDException {

		try {
			Statement stmt = dataH2conn.createStatement();
	    	stmt.executeUpdate(sqlRequest);
	    	return true;
		} catch(SQLException exc) {
			if (HGlobal.DEBUG) System.out.println("Database change error");
			throw new HDException("Update record SQL error: \n" + exc.getMessage());
		}
	}

/**
 * ResultSet requestTableList()
 * @return
 * @throws HCException
 */
	@Override
	public ResultSet requestTableList() throws HDException {
		String sqlRequest = "SHOW TABLES";
	   	try {
			Statement stmt = dataH2conn.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		    	return stmt.executeQuery(sqlRequest);
		} catch(SQLException exc) {
			if (TMGglobal.DEBUG) System.out.println("Database request error!");
			throw new HDException("Request table SQL error: \n" + exc.getMessage());
		}
	}
} // End DatabaseH2handler