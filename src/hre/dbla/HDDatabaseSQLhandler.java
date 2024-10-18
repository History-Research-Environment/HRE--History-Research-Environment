package hre.dbla;
import java.sql.Connection;
/***********************************************************************************
 * Interface DatabaseSQLhandler with abstracts methods for transfer to/from H2 database
 * ***********************************************************************************
 * NOTE first version implemented
 * and only basic database functions
 * - Open database connection
 * - Close database connection
 * - Request SQL data
 *************************************************************************************
  * v0.00.0016 2019-12-20 first version by N. Tolleshaug
  * v0.00.0016 2020-03-19 added getDatabaseConnectPath() to return database path from H2
  *                       removed update as update are handled with ResultSet operation.
  * v0.00.0026 2021-05-26 added new methods for database operation(N. Tolleshaug)
  *************************************************************************************
 */
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * An interface with abstract methods for sub classes used to connect to
 * a specific database as ex. class DatabaseH2handler extends DatabaseHandler
 * @see document
 * @author N. Tolleshaug
 * @version 2019-12-20
 */
interface HDDatabaseSQLhandler {

/**
 * commitUpdates() of database updates
 * @throws SQLException
 */
	abstract void commitUpdates() throws SQLException;

/**
 * Process SQL search in database
 * @param sqlRequest - SQL search String
 * @return - ResultSet from SQL search
 * @throws Exception
 */
	abstract ResultSet requestSQLdata(String sqlRequest) throws HDException;

/**
 * Update record in SQL database
 * @return boolean success
 * @throws HDException
*/
	abstract boolean changeSQLdata(String sqlRequest) throws HDException;


/**
 * Check if database have a table in the database
 * @param tablename
 * @return
 * @throws HDException
 */
	abstract boolean ifTableExist(String tableName) throws HDException;

/**
 * Closes the connection to SQL database
 * @throws Exception
 */

	abstract void closeSQLconnect() throws HDException;
/**
 * getDatabaseConnectPath()
 * @return the connect string used in database connect
 */
	abstract String getDatabaseConnectPath();
	
/**
 * Connection getConnection()	
 * @return
 * @throws HDException
 */
	abstract Connection getConnection() throws HDException;

/**
 * ResultSet requestTableList()
 * @return ResultSet with list of table names
 * @throws HDException
 */
	abstract ResultSet requestTableList() throws HDException;

} // End interface HDDatabaseSQLhandler
