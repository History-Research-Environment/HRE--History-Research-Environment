package hre.dbla;
/***************************************************************************************
* DatabaseLayer for all database operations
* **************************************************************************************
* NOTE only preliminary implementation
* For operation on HRE database on remote server it must be possible
* for multiple users to open HRE databases. This is not foreseen in this
* Database Layer design. Need further design work in project.
****************************************************************************************
* v0.00.0016 2019-12-20 - First version (N. Tolleshaug)
* v0.00.0016 2020-03-19 - removed updateTableData as update is handled with
* 						  operations on ResultSet
* v0.00.0022 2020-05-23 - Corrected error in close database (N. Tolleshaug)
* v0.00.0026 2021-05-27 - Added code for start/stop server (N. Tolleshaug)
* 			 2021-05-28 - Added code for commit command (N. Tolleshaug)
* 			 2021-09-09 - Implemented SSL for TCP server   (N. Tolleshaug)
* 			 2021-10-01 - Handling of local/remote connect to database (N. Tolleshaug)
* 			 2021-10-03 - Transfer of remote connect to OpenProjectData (N. Tolleshaug)
* v0.00.0026 2022-01-02 - Added new method requestTableRows (N. Tolleshaug)
* v0.00.0023 2025-01-22 - Added test for database index = 0 problem (N. Tolleshaug)
****************************************************************************************/

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.gui.HGlobal;

/**
 * class DatabaseLayer includes all methods and administration of complex requests
 * for data from the HRE database
 * @author Nils Tolleshaug
 * @version 2019-12-20
 */

public class HDDatabaseLayer {

	HDServerH2tcp tcpServ;

/**
 * ArrayList containing reference to connected databases. Ref. to 	HDDatabaseSQLhandler
 */
	public ArrayList<HDDatabaseSQLhandler> connectedDataBases = new ArrayList<>();

/**
 * HDDatabaseLayer constructor
 */
	public HDDatabaseLayer() {
		if (HGlobal.DEBUG) System.out.println("DatabaseLayer - Initiated!");
	}

/**
 * Start server
 * @throws HDException
 */
	public String startServer(String portNumber) throws HDException {
        if (tcpServ == null) tcpServ = new HDServerH2tcp(portNumber); //create a new server object
        return tcpServ.startTCPserver(); //starts the tcp server
	}

/**
 * Stop server
 */
	public void stopServer() {
		if (tcpServ != null)
			tcpServ.stopTCPserver();
	}

/**
 * public String getConnectStatus()
 * @return
 */
    public String getConnectStatus() {
    	return tcpServ.getConnectStatus();
    }

/**
 * Establish connection to the SQL database
 * @param projectH2loc location of database in filesystem
 * @param dataBaseEngine type of SQL database
 * @return int index of connected database
 * @throws HBException
 */

	public int connectSQLdatabase(String projectH2loc, String[] logonData) throws HDException {
		boolean remote = false;
		return connectSQLdatabase(remote, projectH2loc, logonData);
	}

	public int connectSQLdatabase(boolean remote, String projectH2loc, String[] logonData) throws HDException {

		String iPportNr = "";
		HDDatabaseSQLhandler pointSQLhandler;
		try {
		   // Connect to SQL database
	        if (HGlobal.DEBUG) System.out.println("DatabaseLayer -  Connect to SQL Database: " + logonData[0]);
	        if (logonData[0].startsWith("H2")) {
	        // Remote = true - call tcp server
	        	if (remote) {
	        		String [] data = logonData[0].split("/");
	        		iPportNr = data[1];

/**
 * Get port number from first entry in the HGlobal.userServers list
 */
	        		String serverPort = HGlobal.userServers.get(0)[2];
	        		if (tcpServ == null)
	        			tcpServ = new HDServerH2tcp(serverPort); //create a new server object
	        	}
	        	String[] serverLogon = {
	        					iPportNr,
	        					logonData[1],
	        					logonData[2]};
	        	pointSQLhandler = new HDDatabaseH2handler(remote, projectH2loc, serverLogon, tcpServ);
	        } else throw new HDException("DatabaseLayer - Not known SQL database - " + logonData[0] + " : \n");

/**
 *  Include the open Database into ArrayList openDatabases
 *  and return Index of ArrayList
 */
	        connectedDataBases.add(pointSQLhandler);
	        int databaseIndex = connectedDataBases.indexOf(pointSQLhandler);

	        return databaseIndex;

		} catch(HDException hde) {
			if (HGlobal.DEBUG) System.out.println("DatabaseLayer - Error connect database!");
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("DatabaseLayer - connect database - Error:  " + hde.getMessage());
				HB0711Logging.printStackTraceToFile(hde);
			}
			throw new HDException("DatabaseLayer - connect database - Error:\n" + hde.getMessage());
		}
	}

/**
 * Test if a table exist in the database
 * @param tableName
 * @param openDatabaseIndex
 * @return
 * @throws HDException
 */
	public boolean ifTableExist(String tableName, int openDatabaseIndex) throws HDException {
		return connectedDataBases.get(openDatabaseIndex).ifTableExist(tableName);
	}

/**
 * Commit updated of database
 * @param openDatabaseIndex
 * @throws SQLException
 */
	public void commitUpdates(int openDatabaseIndex) throws SQLException {
		connectedDataBases.get(openDatabaseIndex).commitUpdates();
	}

/**
 * Closes the database connection
 * @param openDatabaseIndex index of openDatabases
 * @throws HDException
 */
	public void closeDatabaseConnection(int openDatabaseIndex) throws HDException {
		if (HGlobal.DEBUG)
			System.out.println("DatabaseLayer - closeDatabase close index: " + openDatabaseIndex);
		try {
			closeDataBase(openDatabaseIndex);
		// Remove database entry in connectedDataBases and set in null for database pointer
			connectedDataBases.remove(openDatabaseIndex);
			connectedDataBases.add(openDatabaseIndex, null);
		} catch (Exception exe) {
			throw new HDException("DatabaseLayer - close database failed\n" + exe.getMessage());
		}
	}

/**
 * Process a single SQL request to SQL database
 * @param sqlSearchString SQL search string
 * @return ResultSet with table data from SQL database
 * @throws HDException
 */
	public ResultSet requestTableData(String sqlSearchString, int dbIndex) throws HDException {
		ResultSet pointResultSet = null;
		// Adjust SQL request string according to setting in DatabaseAbstraction
		String updatedString = HDDatabaseAbstraction.updateSqlString(sqlSearchString);
		if (HGlobal.DEBUG) 
			 System.out.println("HDDatabaseLayer Request SQL: " + sqlSearchString + " Index: " + dbIndex);
		try {
			if (connectedDataBases.get(dbIndex) != null)
				pointResultSet = connectedDataBases.get(dbIndex).requestSQLdata(updatedString);
			else System.out.println("\nHDDatabaseLayer Request " + " Arraylist size: " + connectedDataBases.size() 
																+ " Index: " + dbIndex + "SQL: " + sqlSearchString);
	        if (HGlobal.DEBUG) 
	        	System.out.println("HDDatabaseLayer - requestTableData\nDatabase path: "
					+ connectedDataBases.get(dbIndex).getDatabaseConnectPath() + " \nRequest SQL: " + sqlSearchString);

	     // Adjust returned ResultSet according to setting in DatabaseAbstraction
	        return HDDatabaseAbstraction.updateResultSet(pointResultSet);

		} catch(HDException hde ) {
			if (HGlobal.DEBUG) System.out.println("Database layer - requestTableData - SQL error: \n" + hde.getMessage());
			throw new HDException("Database layer - requestTableData - SQL error: \n" + hde.getMessage());
		}
	}

/**
 * requestTableRows(String selectSQL,int dbIndex)
 * @param selectSQL
 * @param dbIndex
 * @return
 * @throws HDException
 */
	public long requestTableRows(String selectSQL,int dbIndex) throws HDException {
		try {
			ResultSet rowsInTable = connectedDataBases.get(dbIndex).requestSQLdata(selectSQL);

	        if (HGlobal.DEBUG) System.out.println("HDDatabaseLayer - requestTableRows\nDatabase path: "
					+ connectedDataBases.get(dbIndex).getDatabaseConnectPath()
					+ " \nRequest SQL: " + selectSQL);

			rowsInTable.next();
			return rowsInTable.getLong(1);

		} catch(HDException | SQLException exc) {
			if (HGlobal.DEBUG) System.out.println("Database layer - requestTableData - SQL error: \n" + exc.getMessage());
			throw new HDException("Database layer - requestTableData - SQL error: \n" + exc.getMessage());
		}
	}

/**
 * Process a single update to table in SQL database
 * @param sqlSearchString SQL update string.
 * @param dbIndex open database index
 * @return boolean success
 * @throws HDException
*/
	public boolean updateTableData(String sqlCommandString, int dbIndex) throws HDException {

		// Adjust SQL request string according to setting in DatabaseAbstraction
		String updatedString = HDDatabaseAbstraction.updateSqlString(sqlCommandString);
		try {
	        if (HGlobal.DEBUG) System.out.println("Request SQL String: \n" + sqlCommandString);
	        return connectedDataBases.get(dbIndex).changeSQLdata(updatedString);
		} catch(HDException exc) {
			if (HGlobal.DEBUG) System.out.println("updateTableData - SQL error: \n" + exc.getMessage());
			throw new HDException("updateTableData - SQL error: \n" + exc.getMessage());
		}
	}

/**
 * getDatabasePath
 * @param index
 * @return
 */
	public String getDatabasePath(int index) {
		return connectedDataBases.get(index).getDatabaseConnectPath();
	}

	public Connection getConnection(int index) throws HDException {
		return connectedDataBases.get(index).getConnection();
	}

/**
 * Closes the connection to the database
 * @param dbIndex - openDatabase index
 */
	public void closeDataBase(int dbIndex) {
		try {
			connectedDataBases.get(dbIndex).closeSQLconnect();
		} catch(Exception exc) {
			if (HGlobal.DEBUG) System.out.println("DatabaseLayer - closeDataBase() - H2 Error: \n" + exc.getMessage());
		}
	}

/**
 * requestTableNames(int dbIndex)
 * @param dbIndex
 * @throws HDException
 */
	public ResultSet requestTableNames(int dbIndex) throws HDException {
		return connectedDataBases.get(dbIndex).requestTableList();
	}
} // End DatabaseLayer
