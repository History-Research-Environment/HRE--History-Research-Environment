package hre.tmgjava;
/***********************************************************************************
 * DatabaseH2handler handles data transfer to/from H2 database
 * v0.00.0017 2020-02-14 added no empty database parameter in connect (N. Tolleshaug)
 * v0.00.0025 2021-02.22 - Added create table and delete table actions(N. Tolleshaug)
 * v0.00.0031 2024-04.12 - Added and changed updateTableInBase (N. Tolleshaug)
 * ***********************************************************************************
 */
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import hre.bila.HBException;


/**
 * DatabaseH2handler
 * Handles data transfer to/from H2 database
 * @see document
 * @author NTo - Nils Tolleshaug
 * @since 2020-03-05
 */
public class HREdatabaseHandler {

	Connection dataH2conn = null;
	
	static long proOffset = 1000000000000000L;
	static long null_RPID  = 1999999999999999L;

/**
 * User H2 database login data
 */
	static String userId = "SA";
	static String passWord = "";

/**
 * Opens connection to H2 database - Constructor
 * @param urlH2loc file system location for H2 database
 * @throws HCException
 */
	public HREdatabaseHandler(String urlH2loc) throws HCException {

		String connectString;
    	try {
    		Class.forName("org.h2.Driver");
 
/**
 * If urlH2loc points to a non exiting database - ;IFEXISTS=TRUE
 * disable creation of a new empty H2 database
 */
    		connectString = "jdbc:h2:" + urlH2loc + ";IFEXISTS=TRUE";
    		if (TMGglobal.DEBUG)
    			System.out.println("Database H2 connect: " + connectString);
    		dataH2conn =  DriverManager.getConnection(connectString, userId, passWord);

    	} catch(SQLException exc) {
    		closeSQLconnect();
    		throw new HCException("Connect SQL error:\n" + exc.getMessage());
    	} catch(ClassNotFoundException clx) {
    		throw new HCException("Class Not Found error:\n" + clx.getMessage());
    	}
	}

/**
 * Close connection to H2 database
 * @throws HCException
 */

	public void closeSQLconnect() throws HCException {
		if (TMGglobal.DEBUG) System.out.println("Close H2 Database!");
		try {
			if (dataH2conn != null) {dataH2conn.close();
				dataH2conn = null;
			//if (TMGglobal.DEBUG)
				System.out.println(" H2 Database Closed!");
			}
		} catch(SQLException sqle) {
			if (TMGglobal.DEBUG) System.out.println("Database H2 close error: " + sqle.getMessage());
			throw new HCException("Close Database error: \n" + sqle.getMessage());
		}
	}

/**
 * Request table data from H2 database
 * @param sqlRequest SQL request string
 * @return ResultSet - data returned from SQL request to H2 database
 * @throws HCException
 */
	public ResultSet requestSQLdata(String tableName) throws HCException {
		String sqlRequest = "SELECT * FROM " + tableName;
    	try {
			Statement stmt = dataH2conn.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
	    	return stmt.executeQuery(sqlRequest);
		} catch(SQLException exc) {
			if (TMGglobal.DEBUG) System.out.println("Database request error!");
			throw new HCException("Request record SQL error: \n" + exc.getMessage());
		}
	}
	
/**
 * Generate SELECT SQL command String according to:
 * SELECT column1, column2, ...FROM table_name WHERE condition;
 * @param columSelect table columns to be returned
 * @param tableName SQL table name
 * @param whereCond condition for select
 * @return String sqlRequestString - SQL search string
 */
	public String setSelectSQL(String columSelect, String tableName, String whereCond) {
		String sqlRequestString = "SELECT " + columSelect + " FROM " + tableName;
		if (whereCond.length() > 0) sqlRequestString = sqlRequestString + " WHERE " + whereCond;
		if (TMGglobal.DEBUG)
			System.out.println("Request SQL String: \n" + sqlRequestString);
		return sqlRequestString;
	}
	
/**
 * requestTabledata(String tableName, String selectSQL)	
 * @param tableName
 * @param selectSQL
 * @return
 * @throws HCException
 */
	public ResultSet requestTabledata(String tableName, String selectSQL) throws HCException {
    	try {
			Statement stmt = dataH2conn.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
	    	return stmt.executeQuery(selectSQL);
		} catch(SQLException exc) {
			if (TMGglobal.DEBUG) System.out.println("Database request error!");
			throw new HCException("Request record SQL error: \n" + exc.getMessage());
		}
	}	
	
/**
 * lastRowPID(String tableName, int dataBaseIndex)	
 * @param tableName
 * @param dataBaseIndex
 * @return
 * @throws HBException
 */
	public long lastRowPID(String tableName) throws HBException {
		ResultSet lastPID;
		String selectSQL = "SELECT PID FROM " + tableName + " ORDER BY PID DESC LIMIT 1";
		try {
			lastPID = requestTabledata(tableName, selectSQL);
	        if (lastPID.first()) return lastPID.getLong("PID");
		} catch (SQLException | HCException sqle) {
			throw new HBException(" HBBusinessLayer - lastRowPID error: " + sqle.getMessage());
		}
		return proOffset;
	}

/**
 * ResultSet requestTableList()
 * @return
 * @throws HCException
 */
	public ResultSet requestTableList() throws HCException {
		String sqlRequest = "SHOW TABLES";
	   	try {
			Statement stmt = dataH2conn.createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		    	return stmt.executeQuery(sqlRequest);
		} catch(SQLException exc) {
			if (TMGglobal.DEBUG) System.out.println("Database request error!");
			throw new HCException("Request table SQL error: \n" + exc.getMessage());
		}
	}

/**
 * 	CREATE INDEX NE2 ON T403_PERSON_NAME_ELEMENTS(OWNER_RPID);
 *	ANALYZE TABLE T403_PERSON_NAME_ELEMENTS;
 *	ANALYZE;
 * setIndexingTable(String indexName, String tableName, String columnName)
 * @throws HCException
 */
	public void createIndexTable(String indexName, String tableName, String columnName) throws HCException {
		String sqlRequest = "CREATE INDEX " + indexName + " ON " + tableName + "(" + columnName + ");"
				+ "ANALYZE TABLE " + tableName + ";" + "ANALYZE;";
	   	try {
			Statement stmt = dataH2conn.createStatement();
		    	 stmt.executeUpdate(sqlRequest);
		} catch(SQLException exc) {
			if (TMGglobal.DEBUG) System.out.println("Database request error!");
			throw new HCException("Request table SQL error: \n" + exc.getMessage());
		}
	}

/**
 * Add Column in H2 Base
 * @param tableName
 * @param newColName
 * @param varType
 * @throws HCException
 */
	public void alterColumnInBase(String tableName, String colName, String varType) throws HCException {
		String sqlRequest = "ALTER TABLE " + tableName + " ADD " + colName + " " + varType;
    	try {
			Statement stmt = dataH2conn.createStatement();
	    	stmt.executeUpdate(sqlRequest);
		} catch(SQLException exc) {
			if (TMGglobal.DEBUG) System.out.println("Database request error!");
			throw new HCException("Alter column SQL error: \n" + exc.getMessage());
		}
	}

/**
 * Create table in H2 base
 * @param tableName
 * @param columnStructure
 * @throws HCException
 */
	public void createTableInBase(String tableName, String columnStructure) throws HCException {
		String sqlRequest = "CREATE TABLE " + tableName + "(" + columnStructure + ");";
    	try {
			Statement stmt = dataH2conn.createStatement();
	    	stmt.executeUpdate(sqlRequest);
		} catch(SQLException exc) {
			if (TMGglobal.DEBUG) System.out.println("Database request error!");
			throw new HCException("Create table SQL error: \n" + exc.getMessage());
		}
	}
	
	
/**
 * updateTableInBase(String tableName, String sqlCommand)	
 * @param tableName
 * @param sqlCommand
 * @throws HCException
 */
	public void updateTableInBase(String tableName, String sqlCommand) throws HCException {
		updateTableInBase(tableName, sqlCommand, "");
	}

/**
 * Update table in H2 base
 * @param tableName
 * @throws HCException
 */
	public void updateTableInBase(String tableName, String sqlCommand, String condition) throws HCException {
		//String sqlRequest = "DROP TABLE " + tableName;
		String sqlRequest;
		if (condition.length() > 0)
		sqlRequest = sqlCommand + " " + tableName + " " + condition;
		else sqlRequest = sqlCommand + " " + tableName;
    	try {
			Statement stmt = dataH2conn.createStatement();
	    	stmt.executeUpdate(sqlRequest);
		} catch(SQLException exc) {
			if (TMGglobal.DEBUG) System.out.println("Database update error!");
			throw new HCException("Update table SQL error: \n" + exc.getMessage());
		}
	}

/**
 * public void createNewUser
 * @param userid
 * @param password
 * @throws HCException
 */
	public void createNewUser(String userid, String password) throws HCException {
		String sqlRequest = "CREATE USER " + userid + " PASSWORD '" + password + "';"
							+ " GRANT ALTER ANY SCHEMA TO " + userid + ";";
    	try {
			Statement stmt = dataH2conn.createStatement();
	    	stmt.executeUpdate(sqlRequest);
		} catch(SQLException exc) {
			if (TMGglobal.DEBUG) System.out.println("Database delete error!");
			throw new HCException("Create User SQL error: \n" + exc.getMessage());
		}
	}
/**
 * createNClob(String clobContent)
 * @param clobContent
 * @return Clob
 */
	public Clob createNClob(String clobContent) {
		Clob myClob = null;
	    try {
	    	myClob = createNClob();
			int nrOfChar = myClob.setString(1, clobContent);
			if (TMGglobal.DEBUG)
				System.out.println("Charac in Clob: " + nrOfChar);
	        return myClob;
	    } catch (HCException hre) {
			if (TMGglobal.DEBUG) System.out.println("Write Clob error: " + hre.getMessage());
			hre.printStackTrace();
	    } catch (SQLException sqle) {
	    	if (TMGglobal.DEBUG) System.out.println("Write Clob error: " + sqle.getMessage());
			sqle.printStackTrace();
		}
	    return myClob;
	}

/**
 * createClob()
 * @return Clob object
 * @throws HCException
 */
	public Clob createNClob() throws HCException {
		Clob myClob = null;
		try {
			myClob = this.dataH2conn.createNClob();
			return myClob;
		} catch (SQLException sqle) {
			throw new HCException("Request record SQL error: \n" + sqle.getMessage());
			//e.printStackTrace();
		}
	}


} // End DatabaseH2handler