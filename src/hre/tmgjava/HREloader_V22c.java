package hre.tmgjava;
/*******************************************************************************
 * v0.00.0018 2020-02-29 - First version (N. Tolleshaug)
 * v0.00.0021 2020-04-28 - Dump metadata for HRE tables (N. Tolleshaug)
 * v0.00.0022 2020-06-30 - Substitute \\ with / (N. Tolleshaug)
 * 			  2020-07-30 - Updated for DDL v20a 2020-07-26
 * v0.00.0025 2020-11-15 - Removed DDL19a (N. Tolleshaug)
 * 			  2020-12-29 - Added new columns to LIFE_FORMS and EVENT (N. Tolleshaug)
 * 			  2020-12-29 - Changed to T552_LOCATION_NAMES for placedata (N. Tolleshaug)
 * 			  2021-02-22 - Delete and create new T302 in database (N. Tolleshaug)
 * 			  2021-04-08 - Drop all _CL_ tables in database
 * v0.00.0026 2021-07-09 - Removed fields added to DDL21a (N. Tolleshaug)
 * v0.00.0027 2022-02-27 - Implemented handling event tags, witness and roles(N. Tolleshaug)
 * v0.00.0028 2023-03-10 - Added "CAPTION" to T676_DIGT (N. Tolleshaug)
 * v0.01.0029 2023-05-01 - Implemented v22a (N. Tolleshaug)
 * v0.01.0030 2023-07-02 - Removed alterColumnInTable("T450_EVNT","EVNT_PERS_RPID".. (N. Tolleshaug)
 * 			  2023-07-06 - Added new indexing of tables (N. Tolleshaug)
 * 			  2023-07-10 - Removed alterColumnInTable("T404_PARTNER","LAST_PARTNER".. (N. Tolleshaug)
 * v0.01.0031 2023-10-20 - Updated for v22b database
 * 			  2023-10-20 - Added new role fields for T404_PARTNER
 * 			  2024-04-12 - Added boolean "IS_IMPORTED" for T126_Projects
 * 			  2024-08-06 - Added alterColumnInTable("T404_PARTNER","IMP_TMG","BOOLEAN");
 * 			  2024-08-06 - Added alterColumnInTable("T450_EVNT","IMP_TMG","BOOLEAN");
 * 			  2024-09-04 - Removed alterColumnInTable("T404_PARTNER","IMP_TMG","BOOLEAN");
 * v0.01.0032 2024-12-22 - Updated for v22c database
 *********************************************************************************/
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
/**
 *
 * @author NTo
 *
 */
public class HREloader_V22c {

	HREdatabaseHandler pointHREbase;
	String urlH2loc;
	TMGHREconverter tmgHreConverter;
	ResultSet HRE_Tables;

/**
 * HREloader
 * @param urlH2loc
 * @throws HCException
 */
	public HREloader_V22c(String urlH2loc, TMGHREconverter tmgHreConverter) throws HCException {
		int nrOfTables = 24;
		int completed = 0;
		this.urlH2loc = urlH2loc;
		this.tmgHreConverter = tmgHreConverter;
		try {

			System.out.println(" HREloader database: vV22c - HRE database connected!");

			pointHREbase = new HREdatabaseHandler(urlH2loc);
			
	// Update table T126 - IS_IMPORTED
			updateTableInBase("T126_PROJECTS", "UPDATE", "SET IS_IMPORTED = TRUE WHERE PROJECT_CODE = 1");		
			
	//Delete all rows in T461_EVNT_ROLE but keep table header
			updateTableInBase("T461_EVNT_ROLE", "DELETE FROM");

/**
 * Create new user - remove when moved to HRE
 */
			createNewUser("NTo","Hre-2021");
			tmgHreConverter.setStatusMessage(" Created user " + "NTo" + " with all rights ");

			createNewUser("Don","Hre-2021");
			tmgHreConverter.setStatusMessage(" Created user " + "Don" + " with all rights ");


	// Delete/Drop all tables with _CL_ in table name
			HRE_Tables = pointHREbase.requestTableList();
			try {
				HRE_Tables.beforeFirst();
				int nrOfTablesDeleted = 0;
				while (HRE_Tables.next()) {
					String tableName = HRE_Tables.getString("TABLE_NAME");
					if (tableName.startsWith("C5")) {
						updateTableInBase(tableName, "DROP TABLE");
						nrOfTablesDeleted++;
					}
				}
				tmgHreConverter.setStatusMessage(" *** DROP'ed " + nrOfTablesDeleted + " C5 tables");
				if (TMGglobal.DUMP) System.out.println("Tables Dropped: " + nrOfTablesDeleted);

			} catch (SQLException sqle) {
				System.out.println("HREloader - Delete CL table error: " + sqle.getMessage());
				sqle.printStackTrace();
			}

			reportProgress(completed, nrOfTables);
			TMGglobal.T102 = tableLoader("T102_PACKAGE_DEFN");
			completed++;

			reportProgress(completed, nrOfTables);
			TMGglobal.T104 = tableLoader("T104_SCHEMA_DEFN");
			completed++;

			reportProgress(completed, nrOfTables);
			TMGglobal.T131 = tableLoader("T131_USER");
			completed++;

			reportProgress(completed, nrOfTables);
			TMGglobal.T160 = tableLoader("T160_NAME_STYLE");
			completed++;

			reportProgress(completed, nrOfTables);
			TMGglobal.T162 = tableLoader("T162_NAME_STYLE_OUTPUT");
			completed++;
			
			reportProgress(completed, nrOfTables);
			TMGglobal.T167 = tableLoader("T167_MEMO_SET");
			completed++;
			
			reportProgress(completed, nrOfTables);
			TMGglobal.T170 = tableLoader("T170_DATE");
			completed++;
			
			reportProgress(completed, nrOfTables);
			TMGglobal.T204 = tableLoader("T204_FLAG_TRAN");
			completed++;
			
			reportProgress(completed, nrOfTables);
			TMGglobal.T251 = tableLoader("T251_FLAG_DEFN");
			completed++;
			
			reportProgress(completed, nrOfTables);
			TMGglobal.T252 = tableLoader("T252_FLAG_VALU");
			completed++;

			reportProgress(completed, nrOfTables);
			TMGglobal.T401 = tableLoader("T401_PERS");
			completed++;

			reportProgress(completed, nrOfTables);
			TMGglobal.T402 = tableLoader("T402_PERS_NAME");
			completed++;

			reportProgress(completed, nrOfTables);
			TMGglobal.T403 = tableLoader("T403_PERS_NAME_ELEMNTS");
			completed++;
			
			reportProgress(completed, nrOfTables);
			TMGglobal.T404 = tableLoader("T404_PARTNER");
			completed++;

			reportProgress(completed, nrOfTables);
			TMGglobal.T405 = tableLoader("T405_PARENT_RELATION");
			completed++;

			reportProgress(completed, nrOfTables);
			TMGglobal.T450 = tableLoader("T450_EVNT");
			completed++;

			reportProgress(completed, nrOfTables);
			TMGglobal.T451 = tableLoader("T451_EVNT_ASSOC");
			completed++;

			reportProgress(completed, nrOfTables);
			TMGglobal.T460 = tableLoader("T460_EVNT_DEFN");
			completed++;

			reportProgress(completed, nrOfTables);
			TMGglobal.T461 = tableLoader("T461_EVNT_ROLE");
			completed++;

			reportProgress(completed, nrOfTables);
			TMGglobal.T551 = tableLoader("T551_LOCN");
			completed++;

			reportProgress(completed, nrOfTables);
			TMGglobal.T552 = tableLoader("T552_LOCN_NAME");
			completed++;

			reportProgress(completed, nrOfTables);
			TMGglobal.T553 = tableLoader("T553_LOCN_ELEMNTS");
			completed++;

			reportProgress(completed, nrOfTables);
			TMGglobal.T676 = tableLoader("T676_DIGT");
			completed++;
			
			reportProgress(completed, nrOfTables);
			TMGglobal.T677 = tableLoader("T677_DIGT_NAME");
			completed++;
			
			reportProgress(completed, nrOfTables);

			if (TMGglobal.DUMP)
				dumpDataBaseMetadata(TMGglobal.T401);

			if (TMGglobal.DUMP)
				dumpDataBaseMetadata(TMGglobal.T402);

			if (TMGglobal.DUMP)
				dumpDataBaseMetadata(TMGglobal.T403);

			if (TMGglobal.DUMP)
				dumpDataBaseMetadata(TMGglobal.T450);
			
			if (TMGglobal.DUMP)
				dumpDataBaseMetadata(TMGglobal.T451);

			if (TMGglobal.DUMP)
				dumpDataBaseMetadata(TMGglobal.T552);

			if (TMGglobal.DUMP)
				dumpDataBaseMetadata(TMGglobal.T553);

			if (TMGglobal.DUMP)
				dumpDataBaseMetadata(TMGglobal.T676);

			if (TMGglobal.DUMP)
				dumpDataBaseMetadata(TMGglobal.T170);

			if (TMGglobal.DUMP)
				System.out.println();

		} catch(HCException hde) {
			if (TMGglobal.DEBUG) 			
				System.out.println("HREloader - H2 Database error: " + hde.getMessage());
			hde.printStackTrace();
			throw new HCException ("HREloader - H2 Database error: " + hde.getMessage());
		}
	}

/**
 * HREdatabaseHandler getDataBasePointer()
 * @return
 */
	protected HREdatabaseHandler getDataBasePointer() {
		return pointHREbase;
	}

/**
 * tableLoader(String tableName)
 * @param tableName
 * @return
 * @throws HCException
 */
	protected ResultSet tableLoader(String tableName) throws HCException {
		ResultSet hreTable = null;
		hreTable = pointHREbase.requestSQLdata(tableName);
		if (TMGglobal.DEBUG) System.out.println("H2 Database tableloaded: " + tableName);
		return hreTable;
	}

/**
 * Get database version from Table T101_SCHEMA_DEFNS
 * @param dbIndex
 * @return
*/
	protected String getDatabaseVersion() throws HCException {
        try {
        	TMGglobal.T104.first();
			if (TMGglobal.DEBUG) System.out.println("Found databaseVersion - VERSION_NAME: " +
					TMGglobal.T104.getNString("VERSION_NAME"));
			return TMGglobal.T104.getNString("VERSION_NAME");

		} catch (SQLException sqle) {
			if (TMGglobal.DEBUG) System.out.println("HREloader - getDatabaseVersion - SQLException: "
													+ sqle.getMessage());
			throw new HCException ("HREloader - getDatabaseVersion - SQLException: " + sqle.getMessage());
		}
	}

/**
 * void createIndexTable(String indexName, String tableName, String columnName)
 * @param indexName
 * @param tableName
 * @param columnName
 * @throws HCException
 */
	protected void createIndexTable(String indexName, String tableName, String columnName) throws HCException {
		pointHREbase.createIndexTable(indexName, tableName, columnName);
	}

/**
 * addColumnToTable(String tableName, String newColName, String varType)
 * @param tableName
 * @param newColName
 * @param varType
 * @throws HCException
 */
	protected void alterColumnInTable(String tableName, String newColName, String varType) throws HCException {
		pointHREbase.alterColumnInBase(tableName, newColName, varType);
	}
/**
 * 	Create table
 * @param tableName
 * @param columnStructure
 * @throws HCException
 */
	protected void createTableInBase(String tableName, String columnStructure) throws HCException {
		pointHREbase.createTableInBase(tableName, columnStructure);
	}
/**
 * Delete table
 * @param tableName
 * @throws HCException
 */
	protected void updateTableInBase(String tableName, String sqlCommand, String condition) throws HCException {
		pointHREbase.updateTableInBase(tableName, sqlCommand, condition);
	}
	
	protected void updateTableInBase(String tableName, String sqlCommand) throws HCException {
		pointHREbase.updateTableInBase(tableName, sqlCommand);
	}	

/**
 * public void createNewUser
 * @param userid
 * @param password
 * @throws HCException
 */
	protected void createNewUser(String userid, String password) throws HCException {
		pointHREbase.createNewUser(userid, password);
	}

/**
 * testDataBase
 * @param resultSet
 * @throws HCException
 */
	private void dumpDataBaseMetadata(ResultSet resultSet) throws HCException {
		ResultSetMetaData metaData = null;
		int nrCols;
		try {
			metaData = resultSet.getMetaData();
			nrCols =  metaData.getColumnCount();
			System.out.println("\nH2 metadata for: " + metaData.getTableName(1));
			for (int i = 1; i < nrCols + 1; i++) {
				System.out.print(i + " - " + metaData.getColumnName(i));
				System.out.print(" Type: " + 	metaData.getColumnTypeName(i));
				System.out.println("  Presision: " + 	metaData.getPrecision(i));
			}
		} catch (SQLException sqle) {
			throw new HCException("HREloader - Metadata error: " + sqle.getMessage());
		}
	}


/**
 * closeDatabase()
 * @throws HCException
 */
	public void closeDatabase() throws HCException {
		pointHREbase.closeSQLconnect();
	}

/**
 *
 * @throws HCException
 */
	public void closeHREtables() throws HCException {
		try {
	// Close all HREtables except exhibit table
			if (TMGglobal.T101 != null) TMGglobal.T101.close();
			TMGglobal.T104.close();
			TMGglobal.T131.close();
			TMGglobal.T170.close();
			TMGglobal.T401.close();
			TMGglobal.T402.close();
			TMGglobal.T403.close();
			TMGglobal.T405.close();
			TMGglobal.T450.close();
			TMGglobal.T451.close();
			TMGglobal.T460.close();
			TMGglobal.T461.close();
			if (TMGglobal.T463 != null) TMGglobal.T463.close();
			TMGglobal.T551.close();
			TMGglobal.T552.close();
			TMGglobal.T553.close();

		} catch (SQLException sqle) {
			throw new HCException("HREloader - closeHREtables() - SQL exception" + sqle.getMessage());
		}
	}

/**
 * void reloadHreTables()
 * @throws HCException
 */
	public void reloadHreTables() throws HCException {

		int nrOfTables = 21;
		int completed = 0;
		if (TMGglobal.DEBUG) System.out.println("Updated HRE tables:");
		reportProgress(completed, nrOfTables);
		
		TMGglobal.T102 = tableLoader("T102_PACKAGE_DEFN");
		completed++;
		reportProgress(completed, nrOfTables);
		
		TMGglobal.T104 = tableLoader("T104_SCHEMA_DEFN");
		completed++;
		reportProgress(completed, nrOfTables);

		TMGglobal.T131 = tableLoader("T131_USER");
		completed++;
		reportProgress(completed, nrOfTables);
		
		TMGglobal.T167 = tableLoader("T167_MEMO_SET");
		completed++;
		reportProgress(completed, nrOfTables);

		TMGglobal.T170 = tableLoader("T170_DATE");
		completed++;
		reportProgress(completed, nrOfTables);
		
		reportProgress(completed, nrOfTables);
		TMGglobal.T251 = tableLoader("T251_FLAG_DEFN");
		completed++;
		
		reportProgress(completed, nrOfTables);
		TMGglobal.T252 = tableLoader("T252_FLAG_VALU");
		completed++;

		TMGglobal.T401 = tableLoader("T401_PERS");
		completed++;
		reportProgress(completed, nrOfTables);

		TMGglobal.T402 = tableLoader("T402_PERS_NAME");
		completed++;
		reportProgress(completed, nrOfTables);

		TMGglobal.T403 = tableLoader("T403_PERS_NAME_ELEMNTS");
		completed++;
		reportProgress(completed, nrOfTables);
		
		reportProgress(completed, nrOfTables);
		TMGglobal.T404 = tableLoader("T404_PARTNER");
		completed++;

		TMGglobal.T405 = tableLoader("T405_PARENT_RELATION");
		completed++;
		reportProgress(completed, nrOfTables);

		TMGglobal.T450 = tableLoader("T450_EVNT");
		completed++;
		reportProgress(completed, nrOfTables);
		
		TMGglobal.T451 = tableLoader("T451_EVNT_ASSOC");
		completed++;
		reportProgress(completed, nrOfTables);

		TMGglobal.T460 = tableLoader("T460_EVNT_DEFN");
		completed++;
		reportProgress(completed, nrOfTables);
		
		TMGglobal.T461 = tableLoader("T461_EVNT_ROLE");
		completed++;
		reportProgress(completed, nrOfTables);
			
		TMGglobal.T551 = tableLoader("T551_LOCN");
		completed++;
		reportProgress(completed, nrOfTables);

		TMGglobal.T552 = tableLoader("T552_LOCN_NAME");
		completed++;
		reportProgress(completed, nrOfTables);
		
		TMGglobal.T553 = tableLoader("T553_LOCN_ELEMNTS");
		completed++;
		reportProgress(completed, nrOfTables);

		TMGglobal.T676 = tableLoader("T676_DIGT");
		completed++;
		reportProgress(completed, nrOfTables);
		
		TMGglobal.T677 = tableLoader("T677_DIGT_NAME");
		completed++;
		reportProgress(completed, nrOfTables);

/**
 * Indexing HRE tables
 */
		tmgHreConverter.setStatusMessage(" Start indexing HRE tables");
		String personNames = "T402_PERS_NAME";
		String personNameElements = "T403_PERS_NAME_ELEMNTS";
		String personEvent = "T450_EVNT";
		String eventAssoc = "T451_EVNT_ASSOC";
		String eventDefs = "T460_EVNT_DEFN";
		String eventRole = "T461_EVNT_ROLE";
		String locationNameElements = "T553_LOCN_ELEMNTS";
		String hdateTable = "T170_DATE";
		
		createIndexTable("T170_PID", hdateTable,"PID");
		tmgHreConverter.setStatusMessage(" Indexed - " + hdateTable);
		
		createIndexTable("T402_OWNRPID", personNames, "OWNER_RPID");
		createIndexTable("T402_PID", personNames, "PID");
		tmgHreConverter.setStatusMessage(" Indexed - " + personNames);
		
		createIndexTable("T403_OWNRPID", personNameElements, "OWNER_RPID");
		tmgHreConverter.setStatusMessage(" Indexed - " + personNameElements);
		
		createIndexTable("T450_EVENTPID", personEvent, "PID");
		tmgHreConverter.setStatusMessage(" Indexed - " + personEvent);
		
		createIndexTable("T451_EVENASSOC", eventAssoc, "EVNT_RPID");
		createIndexTable("T451_PERSASSOC", eventAssoc, "ASSOC_RPID");
		tmgHreConverter.setStatusMessage(" Indexed - " + eventAssoc);
		
		createIndexTable("T460_EVNTDEFN", eventDefs, "EVNT_TYPE");
		tmgHreConverter.setStatusMessage(" Indexed - " + eventDefs);
		
		createIndexTable("T461_EVNTROLE", eventRole, "EVNT_ROLE_NUM");
		tmgHreConverter.setStatusMessage(" Indexed - " + eventRole);
		
		createIndexTable("T553_OWNRPID", locationNameElements, "OWNER_RPID");
		tmgHreConverter.setStatusMessage(" Indexed - " + locationNameElements);

/**
 * Dump size of tables
 */
		tmgHreConverter.setStatusMessage(" Start print HRE table size");
		if (TMGglobal.HRETRACE)
			
		try {
			
			TMGglobal.T167.last();
			if (TMGglobal.DEBUG)
				System.out.println("New HRE - T167_MEMO_SET size: " + TMGglobal.T167.getRow());
			tmgHreConverter.setStatusMessage(" T167_MEMO_SET  size: " + TMGglobal.T167.getRow());
			
			TMGglobal.T170.last();
			if (TMGglobal.DEBUG)
				System.out.println("New HRE - T170_HDATES size: " + TMGglobal.T170.getRow());
			tmgHreConverter.setStatusMessage(" T170_HDATES  size: " + TMGglobal.T170.getRow());
			
			TMGglobal.T251.last();
			if (TMGglobal.DEBUG)
				System.out.println("New HRE - T251_FLAG_DEFN size: " + TMGglobal.T251.getRow());
			tmgHreConverter.setStatusMessage(" T251_FLAG_DEFN  size: " + TMGglobal.T251.getRow());
			
			TMGglobal.T252.last();
			if (TMGglobal.DEBUG)
				System.out.println("New HRE - T252_FLAG_VALU size: " + TMGglobal.T252.getRow());
			tmgHreConverter.setStatusMessage(" T252_FLAG_VALU  size: " + TMGglobal.T252.getRow());
			
			TMGglobal.T401.last();
			if (TMGglobal.DEBUG)
				System.out.println("New HRE - T401_PERS size: " + TMGglobal.T401.getRow());
			tmgHreConverter.setStatusMessage(" T401_PERS size: " + TMGglobal.T401.getRow());
			
			TMGglobal.T402.last();
			if (TMGglobal.DEBUG)
				System.out.println("New HRE - T402_PERS_NAME size: " + TMGglobal.T402.getRow());
			tmgHreConverter.setStatusMessage(" T402_PERS_NAME size: " + TMGglobal.T402.getRow());
			
			TMGglobal.T403.last();
			if (TMGglobal.DEBUG)
				System.out.println("New HRE - T403_PERS_NAME_ELEMNTS size: " + TMGglobal.T403.getRow());
			tmgHreConverter.setStatusMessage(" T403_PERS_NAME_ELEMNTS size: " + TMGglobal.T403.getRow());
			
			TMGglobal.T404.last();
			if (TMGglobal.DEBUG)
				System.out.println("New HRE - T404_PARTNER size: " + TMGglobal.T404.getRow());
			tmgHreConverter.setStatusMessage(" T404_PARTNER size: " + TMGglobal.T404.getRow());
			
			TMGglobal.T405.last();
			if (TMGglobal.DEBUG)
				System.out.println("New HRE - T405_PARENT_RELATION size: " + TMGglobal.T405.getRow());
			tmgHreConverter.setStatusMessage(" T405_PARENT_RELATION size: " + TMGglobal.T405.getRow());
			
			TMGglobal.T450.last();
			if (TMGglobal.DEBUG)
				System.out.println("New HRE - T450_EVNT size: " + TMGglobal.T450.getRow());
			tmgHreConverter.setStatusMessage(" T450_EVNT size: " + TMGglobal.T450.getRow());
			
			TMGglobal.T451.last();
			if (TMGglobal.DEBUG)
				System.out.println("New HRE - T451_EVNT_ASSOC size: " + TMGglobal.T451.getRow());
			tmgHreConverter.setStatusMessage(" T451_EVNT_ASSOC size: " + TMGglobal.T451.getRow());
			
			TMGglobal.T460.last();
			if (TMGglobal.DEBUG)
				System.out.println("New HRE - T460_EVNT_DEFS size: " + TMGglobal.T460.getRow());
			tmgHreConverter.setStatusMessage(" T460_EVNT_DEFS size: " + TMGglobal.T460.getRow());
			
			TMGglobal.T461.last();
			if (TMGglobal.DEBUG)
				System.out.println("New HRE - T461_EVENT_ROLE size: " + TMGglobal.T461.getRow());
			tmgHreConverter.setStatusMessage(" T461_EVENT_ROLE size: " + TMGglobal.T461.getRow());
			
			TMGglobal.T551.last();
			if (TMGglobal.DEBUG)
				System.out.println("New HRE - T551_LOCN size: " + TMGglobal.T551.getRow());
			tmgHreConverter.setStatusMessage(" T551_LOCN size: " + TMGglobal.T551.getRow());
			
			TMGglobal.T552.last();
			if (TMGglobal.DEBUG)
				System.out.println("New HRE - T552_LOCN_NAME size: " + TMGglobal.T552.getRow());
			tmgHreConverter.setStatusMessage(" T552_LOCN_NAME size: " + TMGglobal.T552.getRow());
			
			TMGglobal.T553.last();
			if (TMGglobal.DEBUG)
				System.out.println("New HRE - T553_LOCN_NAME_ELEMNTS size: " + TMGglobal.T553.getRow());
			tmgHreConverter.setStatusMessage(" T553_LOCN_NAME_ELEMNTS  size: " + TMGglobal.T553.getRow());
			
			TMGglobal.T676.last();
			if (TMGglobal.DEBUG)
				System.out.println("New HRE - T676_DIGT size: " + TMGglobal.T676.getRow());
			tmgHreConverter.setStatusMessage(" T676_DIGT  size: " + TMGglobal.T676.getRow());
			
			TMGglobal.T677.last();
			if (TMGglobal.DEBUG)
				System.out.println("New HRE - T677_DIGT_NAME size: " + TMGglobal.T677.getRow());
			tmgHreConverter.setStatusMessage(" T677_DIGT_NAME size: " + TMGglobal.T677.getRow());


		} catch (SQLException sqle) {
			throw new HCException("HREloader - reloadHreTables() - SQL exception" + sqle.getMessage());
		}
	}
/**
 * reportProgress(int completed, int nrOfTables )
 * @param completed
 * @param nrOfTables
 * @throws HCException
 */
	private void reportProgress(int completed, int nrOfTables ) throws HCException {
		// Report progress in %
		int progress = (int)Math.round(((double)completed / (double)nrOfTables) * 100);
		if (progress > 100) throw new HCException("Complete percentage error!");
		tmgHreConverter.setStatusProgress(progress);
	}
}
