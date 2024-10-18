package hre.dbla;
/***************************************************************
 * Database Abstraction - Specification XXXX
 * *************************************************************
 * v0.00.0016 2019-12-20 added methods for convert SQL string.
 * v0.00.0016 2019-12-20 added methods for convert ResultSet .
 * *************************************************************
 * NOTE - Only preliminary code implementd
 */

import java.sql.ResultSet;

/**
 * DatabaseAbstraction contains methods that can hide the HRE program system from
 * changes in the SQL table structure.
 * @see document
 * @author Nils Tolleshaug
 * @version 2019-12-20
 */

public class HDDatabaseAbstraction {
/**
 * Parameters to control coversion
 * Not in use in current version
 */
	String dataBaseEngine = "H2";
	String dataBaseversion = "DB14";

/**
 * Convert output from SQL database
 * @param resultSet - ResultSet to be modified
 * @return modified ResultSet
 */
	public static ResultSet updateResultSet(ResultSet resultSet) {
		ResultSet outSet;
/**
 * Dummy statement - real code will include a possible adjustment of data
 */
		outSet = resultSet;
		return outSet;
	}

/**
 * Convert SQL request string
 * @param sqlSearch - SQL request string to be converted
 * @return modified SQL request String
 */
	public static String updateSqlString(String sqlSearch) {
		String outString;
/**
 * Dummy statement - real code will include a possible adjustment of data
 */
		outString = sqlSearch;
		return outString;
	}

} // End DatabaseAbstraction
