package hre.bila;
/************************************************************************************
 * Receives requests from User GUI over Business Layer API
 * Super class Business Layer processes data from database to be presented in User GUI
 * All HB.....Handlers have BusinessLayer as superclass.
 * Include all common methods for communication with DatabaseLayer
 * Sends requests over Database Layer API
 * **********************************************************************************
 * v0.00.0016 2019-12-20 - First version (N. Tolleshaug)
 * v0.00.0019 2020-03-19 - Removed updateTableData - handled with update ResultSet
 * v0.00.0022 2020-05-16 - Deleted setFocusProject(int openProjectIndex) and
 * 						 - Deleted focus project pointers (N. Tolleshaug)
 * 			  2020-07-10 - HDATE display included in HBBusinessLayer (N. Tolleshaug)
 * 			  2020-07-14 - between HDATEs display changed for some date formats (D Ferguson)
 * v0.00.0023 2020-08-12 - Implemented HDATE with data from T175 (N. Tolleshaug)
 * v0.00.0023 2020-08-17 - Corrected additional space in month/year display (N. Tolleshaug)
 *   		  2020-08-22 - Moved getOpenProjectByName to BILA
 *  					   and named pointOpenProjectByName. (N. Tolleshaug)
 * v0.00.0025 2020-11-18 - Implemented name display options DISP/SORT (N. Tolleshaug)
 * v0.00.0025 2021-01-04 - Removed not used code (N. Tolleshaug)
 * 			  2021-01-31 - Removed pointOpenProjectByName(String projectName) (N. Tolleshaug)
 * v0.01.0026 2021-05-16 - Included both DDL20a and DDL21a (N. Tolleshaug)
 * v0.01.0027 2021-12-11 - Included DDL20a, DDL21a and DDL21c (N. Tolleshaug)
 * 			  2022-01-02 - New method for table size numberOfTableRows() (N. Tolleshaug)
 * 			  2022-09-05 - New tables names for name styles T16X (N. Tolleshaug)
 * v0.01.0028 2023-01-05 - Added new database field names (N. Tolleshaug)
 * 			  2023-01-26 - Add method for select output style codes (N. Tolleshaug)
 * v0.01.0030 2023-06-03 - Activated flag processing (N. Tolleshaug)
 * v0.01.0031 2024-01-06 - Implemented alterColumnInTable for new projects (N. Tolleshaug)
 * 			  2024-10-10 - Modified change date format for all handlers (N. Tolleshaug)
 * v0.03.0032 2025-02-12 - Added code for HBCitationSourceHandler (N. Tolleshaug)
 * 			  2025-03-20 - Added userTable name (N. Tolleshaug)
 * 			  2025-07-02 - Added sentece set Table name (N. Tolleshaug)
 ******************************************************************************************/

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JTable;

import hre.dbla.HDDatabaseLayer;
import hre.dbla.HDException;
import hre.gui.HG0401HREMain;
import hre.gui.HGlobal;
import hre.tmgjava.HCException;


/**
 * Business Layer (bila) process requests from user GUI and connect to DatabaseLayer
 * @author Nils Tolleshaug
 * @version v0.00.0016
 * @since 2019-12-09
 */
public class HBBusinessLayer  {

	String dBversion = HGlobal.databaseVersion;
    long proOffset = 1000000000000000L;
    long null_RPID  = 1999999999999999L;

	public String languageUses, translatedLang, schemaDefined, projectTable, userTable, translatedData, translatedFlag, 
				  dateTable, memoSet, sentenceTable;
	
	public String nameStyles, nameStylesOutput, nameElementsDefined, locationNameStyles,
				  locationNameElements, entityTypeDefinition;
	
	public String flagDefn, flagValue, flagDefinition, flagSettingValues;
	
	public String personTable, personNameTable, personNamesTableElements, personBirthTable,
				  personParentTable, personPartnerTable;
	
	public String locationTable, locationNameTable, locationNameElementTable, eventTable, eventTagTable,
				  eventAssocTable, eventDefnTable, eventRoleTable;
	
	public String citationTable, sourceTable, digtalExhibitTable, digtalNameTable;
	
	// Fields
	public String visibleId, bestImage, bestNameField, personFatherField, personMotherField,
				  personHDateBirthField, personHdateDeathField, personLocnBirthField,
				  personLocnDeathField, ownerRecordField, ownerStyleField;

	int birthEventType, deathEventType;
	public int marrGroup = 6, divorceGroup = 7, birthGroup = 4;
	
	private String [] dateFormats = {"dd.mm.yyyy",
			"dd-mm-yyyy",
			"dd/mm/yyyy",
			"dd Mmm yyyy",
			"dd MMM yyyy",
			"yyyy.mm.dd",
			"yyyy-mm-dd",
			"Mmm dd, yyyy",
			"MMM dd, yyyy",
			"mm/dd/yyyy"};
/**
 * Needed for identifying the flag headers in T401
 */
	public String[] flagFieldsT401 = {"BIRTH_SEX","LIVING","BIRTH_ORDER","MULTI_BIRTH","ADOPTED","ANCESTOR_INT","DESCENDANT_INT"};

	// Integer array with table indexes showing first table group name
	int[] tableGroupIndex = new int[10];

	public HDDatabaseLayer pointDBlayer;
	public JDesktopPane mainPanel;
	public HG0401HREMain mainFrame;
	public HBLibraryResultSet pointLibraryResultSet;
	public HBLibraryBusiness pointLibraryBusiness;
	public HREmemo pointHREmemo;

    int dateFormatIndex = 0; 
    boolean changedDateFormat = true;

	// Date translation String[]'s
	String[] monthNamesAbbrev;
	String[] qualifierAbbrev;

/**
 * Project data
 */
	public int selectNameDisplayIndex = 0; // Default given + surname

/**
 * BusinessLayer constructor
 * @author Nils Tolleshaug
 * @version v0.00.0016
 * @since 2019-12-09
 */
	public HBBusinessLayer() {
/**
 * Set up pointers to library class objects according to database version
 */
		if (dBversion.contains("v22c")) {
/**
 * DDL22c
 */
		// Table names
			pointLibraryResultSet = new HBLibraryResultSet(this);

		// System tables
			schemaDefined = "T104_SCHEMA_DEFN";
			projectTable = "T126_PROJECTS";
			userTable = "T131_USER";
			sentenceTable = "T168_SENTENCE_SET";

		// Style tables
			nameStyles = "T160_NAME_STYLE";
			nameStylesOutput = "T162_NAME_STYLE_OUTPUT";
			nameElementsDefined = "T163_NAME_ELEMNT_DEFN";

		// Memo/entity
			memoSet = "T167_MEMO_SET";
			entityTypeDefinition = "T169_ENTY_TYPE_DEFN";

		// Date tables
			dateTable = "T170_DATE";

		// language tables
			languageUses = "T200_LANG_USES";
			translatedLang = "T201_LANG_TRAN";
			translatedData = "T204_DATA_TRAN";
			translatedFlag = "T204_FLAG_TRAN";

			flagDefinition = "T251_FLAG_DEFN";
			flagSettingValues = "T252_FLAG_VALU";

		//Person tables
			personTable = "T401_PERS";
			personNameTable = "T402_PERS_NAME";
			personNamesTableElements = "T403_PERS_NAME_ELEMNTS";
			personPartnerTable = "T404_PARTNER";
			personParentTable = "T405_PARENT_RELATION";

		//Event tables
			eventTable = "T450_EVNT";
			eventAssocTable = "T451_EVNT_ASSOC";
			eventDefnTable = "T460_EVNT_DEFN";
			eventRoleTable = "T461_EVNT_ROLE";

		//Location tables
			locationTable = "T551_LOCN";
			locationNameTable = "T552_LOCN_NAME";
			locationNameElementTable = "T553_LOCN_ELEMNTS";

		//Exhibit table
			digtalExhibitTable = "T676_DIGT";
			digtalNameTable = "T677_DIGT_NAME";
			
		// Citation source tables
			 citationTable = "T735_CITN";
			 sourceTable = "T736_SORC";
			 
		// Field names
			visibleId = "VISIBLE_ID";
			ownerRecordField = "OWNER_RPID";
			ownerStyleField = "OWNER_RECORD_RPID";
			bestImage = "BEST_IMAGE_RPID";
    		bestNameField = "BEST_NAME_RPID";
    		personFatherField = "SPERM_PROVIDER_RPID";
    		personMotherField = "EGG_PROVIDER_RPID";
			personHDateBirthField = "BIRTH_HDATE_RPID";
			personHdateDeathField = "DEATH_HDATE_RPID";
			personLocnBirthField = "BIRTH_PLACE_RPID";
			personLocnDeathField = "DEATH_PLACE_RPID";

		// Crital event Types
			birthEventType = 1002;
			deathEventType = 1003;

		} else {
			System.out.println("HBBusinessLayer - Database build version not found");
		}
		pointLibraryBusiness = new HBLibraryBusiness(this);
	}

/**
 * T101_TABLE_DEFNS
 * T104_SCHEMA_DEFNS
 * T126_PROJECTS
 * T131_USERS
 * T175_HDATE_SUPPORT_DEFNS
 * T201_LANGUAGE_USES
 * T204_LABEL_TRANS
 * T302_GUI_CONFIG
 * T401_PERSONS
 * T402_PERSON_NAMES
 * T403_PERSON_NAME_ELEMENTS
 * T405_PERSON_BIRTHS
 * T452_EVENTS
 * T453_EVENT_WITNESS
 * T460_EVENT_TAGS
 * T461_EVENT_ROLES_NAMES
 * T551_LOCATIONS
 * T552_LOCATION_NAMES
 * T553_LOCATION_NAME_ELEMENTS
 * T676_DIGITALS
 * T751_HDATES
*/
	public void initiateTableNames(ArrayList<String> databaseTables) {
		String tableName;
		//boolean TABLES = HGlobal.DEBUG;
		boolean TABLES = false;
		//TABLES = true;
		tableGroupIndex[1] = 0;
		for ( int i = 0; i < databaseTables.size(); i++) {
			tableName = databaseTables.get(i);
			if (tableName.startsWith("T1")) {
				if (TABLES) {
					System.out.println(i + " TG1 - " + tableName);
				}
				tableName = databaseTables.get(i);

			} else
			if (tableName.startsWith("T2")) {
				if (TABLES) {
					System.out.println(i + " TG2 - " + tableName);
				}
				tableName = databaseTables.get(i);
				if (tableGroupIndex[2] == 0) {
					tableGroupIndex[2] = i;
				}
			} else
			if (tableName.startsWith("T3")) {
				if (TABLES) {
					System.out.println(i + " TG3 - " + tableName);
				}
				tableName = databaseTables.get(i);
				if (tableGroupIndex[3] == 0) {
					tableGroupIndex[3] = i;
				}
			} else
			if (tableName.startsWith("T4")) {
				if (TABLES) {
					System.out.println(i + " TG4 - " + tableName);
				}
				tableName = databaseTables.get(i);
				if (tableGroupIndex[4] == 0) {
					tableGroupIndex[4] = i;
				}
			} else
			if (tableName.startsWith("T5")) {
				if (TABLES) {
					System.out.println(i + " TG55 - " + tableName);
				}
				tableName = databaseTables.get(i);
				if (tableGroupIndex[5] == 0) {
					tableGroupIndex[5] = i;
				}
			} else
			if (tableName.startsWith("T6")) {
				if (TABLES) {
					System.out.println(i + " TG6 - " + tableName);
				}
				tableName = databaseTables.get(i);
				if (tableGroupIndex[6] == 0) {
					tableGroupIndex[6] = i;
				}
			} else
			if (tableName.startsWith("T7")) {
				if (TABLES) System.out.println(i + " TG7 - " + tableName);
				tableName = databaseTables.get(i);
				if (tableGroupIndex[7] == 0) tableGroupIndex[7] = i;
			}  else {
				System.out.println(" Table name group not found: " + tableName);
			}
		}

		if (TABLES) {
			System.out.println("T401_PERS: " + databaseTables.get(tableGroupIndex[4] + 0));
			System.out.println("T552_LOCN_NAMES: " + databaseTables.get(tableGroupIndex[5] + 1));
			System.out.println("T676_DIGI: " + databaseTables.get(tableGroupIndex[6] + 3));
		}
	}

/**
 *
 * @param count
 * @param rows
 * @return
 * @throws HBException
 */
	public int calcPercent(int count, int rows ) throws HBException {
		try {
			if (rows == 0) {
				throw new HBException(" HBBusinessLayer calcPercent - Attempt to Divide by null error! ");
			}
			int percent = count * 100 / rows;
			if (percent > 100) {
				return 100;
			} else {
				return percent;
			}
		} catch (Exception hbe) {
			throw new HBException(" Divide by null error: " + hbe.getMessage());
		}
	}

/**
 * lastRowPID(String tableName, HBProjectOpenData pointOpenProject)
 * @param tableName
 * @param pointOpenProject
 * @return
 * @throws HBException
 */
	public long lastRowPID(String tableName, HBProjectOpenData pointOpenProject) throws HBException {
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		return lastRowPID(tableName, dataBaseIndex);
	}

/**
 *
 * @param tableName
 * @param dataBaseIndex
 * @return
 * @throws HBException
 */
	public long lastRowPID(String tableName, int dataBaseIndex) throws HBException {
		ResultSet PID;
		String selectSQL = "SELECT PID FROM " + tableName + " ORDER BY PID DESC LIMIT 1";
		PID = requestTableData(selectSQL, dataBaseIndex);
		try {
	        if (PID.first()) {
				return PID.getLong("PID");
			} else {
				return proOffset;
			}
		} catch (SQLException sqle) {
			throw new HBException(" HBBusinessLayer - lastRowPID error: " + sqle.getMessage());
		}
	}

/**
 * firstRowPID(String tableName, HBProjectOpenData pointOpenProject)
 * @param tableName
 * @param pointOpenProject
 * @return
 * @throws HBException
 */
	public long firstRowPID(String tableName, HBProjectOpenData pointOpenProject) throws HBException {
		 int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		 return firstRowPID(tableName, dataBaseIndex);
	}

	public long firstRowPID(String tableName, int dataBaseIndex) throws HBException {
		ResultSet PID;
		String selectSQL = "SELECT * FROM " + tableName + " LIMIT 1";
		PID = requestTableData(selectSQL, dataBaseIndex);
		try {
	        if (PID.first()) {
				return PID.getLong("PID");
			} else {
				return proOffset;
			}
		} catch (SQLException sqle) {
			throw new HBException(" HBBusinessLayer - firstRowPID error: " + sqle.getMessage());
		}
	}
/**
 * isMyResultSetEmpty(ResultSet rSet)	Is result set empty
 * @param rs
 * @return
 * @throws SQLException
 */
	public boolean isResultSetEmpty(ResultSet rSet) throws SQLException {
		if (rSet == null) return true; else
	    return (!rSet.isBeforeFirst() && rSet.getRow() == 0);
	}

/**
 * setNameDisplayIndex(int select)
 * @param select
 */
	public void setNameDisplayIndex(int select) {
    	selectNameDisplayIndex = select;
    }
/**
 * getNameDisplayIndex()
 * @return

    public int getNameDisplayIndex() {
    	return selectNameDisplayIndex;
    }
*/
/**
 * Set default database engine
 * @return defaultDatabaseEngine
 */
	public String getDefaultDatabaseEngine() {
		return HGlobal.defDatabaseEngine;
	}

/**
 * long ntOfTableRows(String tableName, int dbIndex)
 * SELECT COUNT(*) FROM table name [WHERE clause]
 * @param tableName
 * @param dbIndex
 * @return
 * @throws HBException
 */
	public long numberOfTableRows(String tableName, int dbIndex) throws HBException {
		String selectSQL = setSelectSQL("SELECT COUNT(*)",tableName,"");
		try {
			return pointDBlayer.requestTableRows(selectSQL,dbIndex);
		} catch (HDException hde) {
			throw new HBException(" Nr of rows not found!\n" + hde.getMessage());
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
		if (whereCond.length() > 0) {
			sqlRequestString = sqlRequestString + " WHERE " + whereCond;
		}
		if (HGlobal.DEBUG) {
			System.out.println("Request SQL String: \n" + sqlRequestString);
		}
		return sqlRequestString;
	}

/**
 * Request table data according to SQL search
 * @param selectSQL - SQL request string
 * @return ResultSet from SQL database
 */
	public ResultSet requestTableData(String selectSQL, int dbIndex ) throws HBException {
		try {
			return pointDBlayer.requestTableData(selectSQL, dbIndex);
		} catch(HDException hde) {
			if (HGlobal.DEBUG) {
				System.out.println("Request table data error: " + hde.getMessage());
			}
			throw new HBException("Request table data error: \n" + hde.getMessage());
		}
	}

/**
 * Add Column in H2 Base
 * @param tableName
 * @param newColName
 * @param varType
 * @throws HCException
 */
	public void alterColumnInTable(String tableName, String colName, String varType, int dbIndex) throws HBException {
		String sqlRequest = "ALTER TABLE " + tableName + " ADD " + colName + " " + varType;
    	updateTableData(sqlRequest, dbIndex);
	}
	
/**
 * updateTableInBase(String tableName, String sqlCommand, String condition, int dbIndex) 	
 * @param tableName
 * @param sqlCommand
 * @param condition
 * @param dbIndex
 * @throws HBException
 */
	public void updateTableInBase(String tableName, String sqlCommand, String condition, int dbIndex) throws HBException {
		String sqlRequest;
		if (condition.length() > 0)
			sqlRequest = sqlCommand + " " + tableName + " " + condition;
		else sqlRequest = sqlCommand + " " + tableName;
    	updateTableData(sqlRequest, dbIndex);
	}
/**
 * public boolean updateTableData(String selectSQL, int dbIndex)
 * @param selectSQL
 * @param dbIndex
 * @return
 * @throws HBException
 */
	public boolean updateTableData(String selectSQL, int dbIndex) throws HBException {
		try {
			return pointDBlayer.updateTableData(selectSQL, dbIndex);
		} catch(HDException hde) {
			if (HGlobal.DEBUG) {
				System.out.println("Update table data error: " + hde.getMessage());
			}
			throw new HBException("Update table data error: \n" + hde.getMessage());
		}
	}

	
/**
 *
 * @param selectSQL
 * @param dbIndex
 * @return
 * @throws HBException
 */
	public boolean createTableInBase(String tableName, String columnStructure, int dbIndex) throws HBException {
		try {
			return pointDBlayer.createTableInBase(tableName, columnStructure, dbIndex);
		} catch(HDException hde) {
			if (HGlobal.DEBUG) {
				System.out.println("Update table data error: " + hde.getMessage());
			}
			throw new HBException("Update table data error: \n" + hde.getMessage());
		}
	}
/**
 * Close Database in DBLayer
 * @param dataBaseIndex - index open databases
 */
	void closeDatabase(int dataBaseIndex) throws HBException {
		try {
			if (HGlobal.DEBUG) {
				System.out.println("HBLibraryBusiness - closeDatabase index: " + dataBaseIndex);
			}
			pointDBlayer.closeDatabaseConnection(dataBaseIndex);
		} catch (HDException hde) {
			if (HGlobal.DEBUG) {
				System.out.println("closeDatabase - HDException: \n" + hde.getMessage());
			}
			throw new HBException ("Close database failed - index: " + dataBaseIndex + "\n" + hde.getMessage());
			//hde.printStackTrace();
		}
	}

/**
 * Print content of ResultSet on Java Console - calls method in HBResultSetLibrary
 * @param pointResultSet The ResultSet content to by printed
 */
	public void dumpResultSetData(ResultSet pointResultSet) throws HBException {
		try {
			pointLibraryResultSet.dumpResultSetData(pointResultSet);
		} catch(HBException exc) {
			throw new HBException("BusinessLayer-convertRStoXML : \n" + exc.getMessage());
		}
	}

/**
 * Convert ResultSet from database to new JTable - calls method in HBResultSetLibrary
 * @param ResSet ResultSet to be converted to Jtable
 * @return - JTable input to User GUI
 * @throws HBException
 */
	public JTable makeTableFromRS(ResultSet ResSet) throws HBException {
			JTable userTable = new JTable(pointLibraryResultSet.buildTableModel(ResSet));
//		    JTableHeader tHeader = userTable.getTableHeader();
		    return userTable;
	}

/**
 * Remove JInternalFrame from main window
 * @param intFrame JInternalFrame to be removed
 */
		public void removeInternalFrameFromWindow(JInternalFrame intFrame) {
			intFrame.setVisible(false);
			mainPanel.remove(intFrame);
			intFrame = null;
		}

/**
 * Sort Object Table rows according to Array sortNumbers
 * sortTable(Object[][] tableData, int[] sortNumbers, int nrRows)
 * @param tableData
 * @param sortNumbers
 * @return
 */
	public Object[][] sortTable(Object[][] tableData, int[] sortNumbers) {
		int nrRows = tableData.length;
		int nrColumns = tableData[0].length;
		Object[][] sortedTable = new Object[nrRows][nrColumns];
		for (int i = 0; i < nrRows; i++) {
			for (int j = 0; j < nrColumns; j++) {
				sortedTable[i][j] = tableData[sortNumbers[i]][j];
			}
		}
		return sortedTable;
	}


//------

/**
 * formatDateSelector set up display date according to qualifier and formats
 * @param mainYears
 * @param mainDetails
 * @param extraYears
 * @param extraDetails
 * @return String displayDate
 */
  	public String formatDateSelector(long mainYears, String mainDetails,
  									 long extraYears, String extraDetails) {
  		String displayDate = "";
  		//System.out.println(" Date Format Index: " + dateFormatIndex);
  	// Weed out Irregular first
  		if (mainDetails.startsWith("I")) {
			return displayDate = " IR " + extraDetails;
		}
  	// First check HG590 wasn't cancelled out of
  		if (mainDetails.length() < 17) {
			return displayDate;
		}
  		char qualifier = mainDetails.charAt(16);
  	// Ensure years aren't negative (as BC qualifier is for presenting that)
  		mainYears = Math.abs(mainYears);
  		extraYears = Math.abs(extraYears);
  	// HRE qualifier handling -
		switch(qualifier) {
			case 'X':				// exact date (qualifier is null)
				displayDate = qualifierAbbrev[0] + " " + dateFormat(dateFormatIndex, mainYears, mainDetails);
				break;
			case 'C':				// circa (qualifier = 'c.')
				displayDate = qualifierAbbrev[1] + " " + dateFormat(dateFormatIndex, mainYears, mainDetails);
				break;
			case 'S':				// say (qualifier = 'say')
				displayDate = qualifierAbbrev[2] + " " + dateFormat(dateFormatIndex,mainYears, mainDetails);
				break;
			case 'E':				// estimated (qualifier = 'est')
				displayDate = qualifierAbbrev[3] + " " + dateFormat(dateFormatIndex,mainYears, mainDetails);
				break;
			case 'B':				// before (qualifier = 'bef')
				displayDate = qualifierAbbrev[4] + " " + dateFormat(dateFormatIndex,mainYears, mainDetails);
				break;
			case 'A':				// after (qualifier = 'aft')
				displayDate = qualifierAbbrev[5] + " " + dateFormat(dateFormatIndex,mainYears, mainDetails);
				break;
			case 'W':				// between/and (qualifiers = 'btwn' and '&')
				if (dateFormatIndex <= 4) {	// for formats "dd.mm.yyyy", "dd-mm-yyyy","dd/mm/yyyy" "dd Mmm yyyy","dd MMM yyyy"
						if (mainYears == extraYears)
						 {
							mainYears = 0;		// remove leading years
						}
				}
				String andString = " & ";
				if (mainYears == 0 && dateFormatIndex > 2) {
					andString = "& ";
				}
				displayDate = qualifierAbbrev[6] + " " + dateFormat(dateFormatIndex, mainYears, mainDetails)
								+ andString + dateFormat(dateFormatIndex, extraYears, extraDetails);
				break;
			case 'O':				// either/or (qualifiers = nothing and 'or')
				displayDate = " " + dateFormat(dateFormatIndex, mainYears, mainDetails)
							+ " " + qualifierAbbrev[7] + " " + dateFormat(dateFormatIndex, extraYears, extraDetails);
				break;
			case 'F':				// from/to (qualifiers = 'from', 'to')
				displayDate = qualifierAbbrev[8] + " " + dateFormat(dateFormatIndex, mainYears, mainDetails)
							+ " " + qualifierAbbrev[9] + " " + dateFormat(dateFormatIndex, extraYears, extraDetails);
				break;
			default: System.out.println("Unknown Date Qualifier = " + mainDetails);
		}
		// Check question mark and Old Style / BC / MYA
		if (mainDetails.charAt(17) == 'Y') {
			displayDate = displayDate + '?';
		}
		if (mainDetails.startsWith("NCO")) {
			displayDate = displayDate + " OS";
		}
		if (mainDetails.startsWith("NBC")) {
			displayDate = displayDate + " BC";
		}
		if (mainDetails.startsWith("NYA")) {
			displayDate = displayDate + " YA";
		}
  		return displayDate;
  	}

/**
 * dateFormat
 * @param dateFormatIndex
 * @param mainYear
 * @param mainDetails
 * @return
 */
  	
  	
  	private String dateFormat(int dateFormatIndex, long mainYear, String mainDetails) {
  		
  		char separator = '.';
  		String displayDate = "", dayMonth = "", mainYears = "", month = "", day = "";
  	// Test for problems
  		//if (mainDetails.length() == 1 || mainDetails == null) return " Error details - MainYear = " + mainYear;
  		if (mainDetails.length() == 1 || mainDetails == null) {
			return "";
		}
  	// extract month
  		dayMonth = mainDetails.substring(3,7);
  	// TMG does not use CE = 0000
  		if (mainYear == 0 && dayMonth.equals("%%%%")) {
			return "";
		}
  	//Do not show 0 if main years == 0
  		if (mainYear == 0) {
			mainYears = "";
		} else {
			mainYears = "" + String.format("%04d", mainYear);
		}
  		month = dayMonth.substring(0,2);
  		day = dayMonth.substring(2,4);

  	// Select date format
  		if (dateFormatIndex < 3) {
  	//Format "dd.mm.yyyy", "dd-mm-yyyy","dd/mm/yyyy"
  			if (dateFormatIndex == 0) {
				separator = '.';
			}
  			if (dateFormatIndex == 1) {
				separator = '-';
			}
  			if (dateFormatIndex == 2) {
				separator = '/';
			}
  	// No day set separator
  			if (day.startsWith("%")) {
				day = "";
			} else {
				day = day + separator;
			}
  	// No month set ?
  			if (!dayMonth.startsWith("%")) {
				displayDate = displayDate + day + month + separator + mainYears;
			} else {
				displayDate = displayDate + mainYears;
			}
  			return displayDate;

  		} else if (dateFormatIndex == 3 || dateFormatIndex == 4) {
  	//Format "dd Mmm yyyy","dd MMM yyyy"
  			if (!dayMonth.startsWith("%")) {
  				if (day.startsWith("%")) {
					day = "";
				} else {
					day = day + " ";
				}
  				if (dateFormatIndex == 3) {
					month = monthName(month, false);
				}
  				if (dateFormatIndex == 4) {
					month = monthName(month, true);
				}
  				displayDate = day + month + " " + mainYears;
  			} else {
				displayDate = mainYears;
			}
  			return displayDate;

  		} else if (dateFormatIndex == 5 || dateFormatIndex == 6) {
  	//Format "yyyy.mm.dd","yyyy-mm-dd"
  			if (dateFormatIndex == 5) {
				separator = '.';
			}
  			if (dateFormatIndex == 6) {
				separator = '-';
			}
  	// No month set ?
  			if (!dayMonth.startsWith("%")) {
  				if (day.startsWith("%")) {
					day = "";
				} else {
					day = separator + day;
				}
  				displayDate = mainYears + separator + month + day;
  			} else {
				displayDate = mainYears;
			}
  			return displayDate;

  		} else if (dateFormatIndex == 7 || dateFormatIndex == 8) {
  	//Format "Mmm dd, yyyy","MMM dd, yyyy"
  			if (!dayMonth.startsWith("%")) {
  				if (dateFormatIndex == 7) {
					month = monthName(month, false);
				}
  				if (dateFormatIndex == 8) {
					month = monthName(month, true);
				}
  				if (day.startsWith("%")) {
					day = "";
				} else {
					day = " " + day;
				}
  				displayDate = month +  day + ", " + mainYears;
  			} else {
				displayDate = mainYears;
			}
  			 return displayDate;

  		} else if (dateFormatIndex == 9) {
  	//format "mm/dd/yyyy";
  			if (!dayMonth.startsWith("%")) {
  				if (day.startsWith("%")) {
					day = "  ";
				}
  				displayDate = month + "/" + day + "/" + mainYears;

  			} else {
				displayDate = "" + mainYears;
			}
  			return displayDate;
  		}
  		return "No date recorded";
  	}

/**
 * monthName
 * @param month
 * @param upperCase
 * @return monthName
 */
	private String monthName(String month, boolean upperCase) {
		String monthName = "";
		if (month.startsWith("0")) {
			month = month.substring(1);
		}
		try {
			int monthNumber = Integer.parseInt(month);
			if (monthNumber > 0 &&  monthNumber < 13) {
				monthName = monthNamesAbbrev[Integer.parseInt(month)-1];
			} else {
				return monthName;
			}
		} catch(NumberFormatException nfe) {
			System.out.println("NumberFormatException month: " + month + " err: " + nfe.getMessage());
		}
		if (upperCase) {
			return monthName.toUpperCase();
		} else {
			return monthName;
		}
	}

/**
 * Return numerical value of date
 * @param mainYear
 * @param mainDetails
 * @return
 */
	public int[] numericalDate(long mainYear, String mainDetails) throws NumberFormatException{
		int[] date = new int[3];
		date[0] = Integer.parseInt("" + mainYear);
		String dayMonth = mainDetails.substring(3,7);
		if (dayMonth.equals("%%%%")) {
			date[1] = 0;
			date[2] = 0;
		}
		else {
			date[1] = Integer.parseInt(dayMonth.substring(0,2));
			if (dayMonth.substring(2,4).equals("%%")) {
				date[2] = 0;
			} else {
				date[2] = Integer.parseInt(dayMonth.substring(2,4));
			}
		}
		return date;
 }

/**
 * Select HDATE display format
 */
	public void newDateFormatSelected() {
		int newDateFormat = dateFormatSelector();
	// Mark dataformat change to reload data
		if (dateFormatIndex == newDateFormat) {
			changedDateFormat = false;
		} else {
			changedDateFormat = true;
		}
		dateFormatIndex = newDateFormat;
	}
	
/**
 * getChangedDateFormat()
 * @return
 */
	public boolean getChangedDateFormat() {
		return changedDateFormat;
	}
	
/**
 * public void dateFormatSelect() for all HDATE display in handlers
 */
	public void dateFormatSelect() {
		for (int i = 0; i < dateFormats.length; i++) {
			if (dateFormats[i].trim().equals(HGlobal.dateFormat.trim())) {
				dateFormatIndex = i;
				//System.out.println("dateFormatSelect() - FormatIndex = " + dateFormatIndex);
				return;
			}
		}
		System.out.println("dateFormatSelect() - Format " +  HGlobal.dateFormat + " not found!!");
	}
	
/**
 * Date format selector
 * @return date format index
 */
	protected int dateFormatSelector() {
		for (int i = 0; i < dateFormats.length; i++) {
			if (dateFormats[i].trim().equals(HGlobal.dateFormat.trim())) {
				return i;
			}
		}
		System.out.println("dateFormatSelector() - Format string not found!!");
        return 0;
	}
	
/**
 * initiateDateFormat(int index)	
 */
	public void initiateDateFormat(int index) {
		HGlobal.dateFormat = dateFormats[index];
	}

/**
 * setUpDateTranslation()
 */
	public void setUpDateTranslation() {
		monthNamesAbbrev = setTranslatedData("510", true);
		qualifierAbbrev = setTranslatedData("560", true);
		//System.out.println(" Length: " + qualifierAbbrev.length + " Index = 1 " + qualifierAbbrev[1]);
	}

/**
 * getDefaultNameStyle(String nameType, int dataBaseIndex)
 * @param nameStyleCodeTable
 * @return
 * @throws HBException
 */
	protected long getDefaultNameStyle(String nameType, int dataBaseIndex) throws HBException {
		String selectString  = setSelectSQL("*", nameStyles,"IS_DEFAULT = TRUE AND NAME_TYPE = '" + nameType + "'");
		try {
			ResultSet defaultNameStyle = requestTableData(selectString, dataBaseIndex);
			defaultNameStyle.first();
			return defaultNameStyle.getLong("PID");
		} catch (SQLException sqle) {
			System.out.println(" HBBusinessLayer - getDefaultNameStyle - Name Type: " + nameType + " - " + sqle.getMessage());
		// No default name style? select System Name Style
			return proOffset+1;
		}
	}

/**
 * getNameStyleOutputCodes(String nameType, int dataBaseIndex)
 * @param nameType
 * @param dataBaseIndex
 * @return
 * @throws HBException
 */
	public String[] getNameStyleOutputCodes(String nameType, int dataBaseIndex) throws HBException {
		return getNameStyleOutputCodes(nameStylesOutput, nameType, dataBaseIndex);
	}

/**
 * getNameStyleOutputCodes(String nameStyleOuputTable, String nameType, int dataBaseIndex)
 * @param nameStyleOuputTable
 * @param nameType
 * @param dataBaseIndex
 * @return
 * @throws HBException
 */
	public String[] getNameStyleOutputCodes(String nameStyleOuputTable,
											String nameType,
											int dataBaseIndex) throws HBException {
	// For Person - Match nameStyle index = 0 -> 1 and index = 1 -> 0 with T162 table !
		long ownerRPID = getDefaultNameStyle(nameType, dataBaseIndex);
		return getNameStyleOutputCodes(nameStyleOuputTable, ownerRPID, nameType, dataBaseIndex);
	}

/**
 * getNameStyleOutputCodes(String nameStyleOuputTable,
											long ownerRPID,
											String nameType,
											int dataBaseIndex)
 * @param nameStyleOuputTable
 * @param ownerRPID
 * @param nameType
 * @param dataBaseIndex
 * @return String[] output codes
 * @throws HBException
 */
	public String[] getNameStyleOutputCodes(String nameStyleOuputTable,
											long ownerRPID,
											String nameType,
											int dataBaseIndex) throws HBException {

		int index = selectNameDisplayIndex;
		if (nameType.equals("P")) {
			index = 0;
		} else if (nameType.equals("N")) {
			index = selectNameDisplayIndex;
		} else {
			System.out.println(" getNameStyleOutputCodes - Unknown nameType: " + nameType );
		}
		if (HGlobal.DEBUG) {
			System.out.println(" Default Name Style Type: " + nameType + " owner: " + ownerRPID
				+ " NSindex: " + index);
		}

		return pointLibraryResultSet.getNameStyleOutput(nameStylesOutput, ownerRPID, nameType , index, dataBaseIndex);
	}

/**
 * Date format selector
 * @return date format index
 
	private int dateFormatSelector() {
		for (int i = 0; i < dateFormat.length; i++) {
			if (dateFormat[i].trim().equals(HGlobal.dateFormat.trim())) {
				return i;
			}
		}
		System.out.println("dateFormatSelector() - Format string not found!!");
        return 0;
	}
*/	
/**
 * initiateDateFormat(int index)	

	public void initiateDateFormat(int index) {
		HGlobal.dateFormat = dateFormat[index];
	}
 */
/**
 * setTranslatedData(String tableIdent, boolean abbrev)
 * @param tableIdent
 * @param abbrev
 * @return String[]
 */
	public String[] setTranslatedData(String tableIdent, boolean abbrev) {
		return setTranslatedData("00000", tableIdent, abbrev);
	}

	public String[] setTranslatedData(String screenID, String tableIdent, boolean abbrev) {
		int dataBaseIndex = HG0401HREMain.mainFrame.getSelectedOpenProject().getOpenDatabaseIndex();
		if (HGlobal.DEBUG) {
			System.out.println(" Translated database screen: " + screenID  + " / " + dataBaseIndex + " / " + HGlobal.dataLanguage);
		}
		try {
			String[] translation = pointLibraryResultSet.
					getTranslatedData(abbrev, screenID, tableIdent, HGlobal.dataLanguage, dataBaseIndex);
			if (HGlobal.DEBUG) {
					System.out.println("Translations: ");
					for (String element : translation) {
						System.out.print(element + "/");
					}
					System.out.println();
			}
			return translation;
		} catch (HBException hbe) {
			System.out.println(" HBBusinessLayer - setTranslatedData() error: " + hbe.getMessage());
			hbe.printStackTrace();
		}
		return null;
	}

/**
 * Get current system time format yyyy-MM-dd / HH:mm:ss
 * @return String time
 */
	  public String currentTime(String dataTimeFormat) {
		  DateTimeFormatter dtf = DateTimeFormatter.ofPattern(dataTimeFormat);
		  //DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd / HH:mm:ss");
		   LocalDateTime now = LocalDateTime.now();
		   //System.out.println(dtf.format(now));
		   return dtf.format(now);
	  }
} // End of class Businesslayer
