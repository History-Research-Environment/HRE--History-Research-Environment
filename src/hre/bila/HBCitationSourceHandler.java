package hre.bila;
/************************************************************************************************
 * Class  HBCitationSourceHandler extends BusinessLayer
 * Processes data for Citation Source Menu in User GUI
 * Receives requests from User GUI to action methods
 * Sends requests to database over Database Layer API
 ************************************************************************************************
 * v0.04.0032 2025-02-12 - First version (N. Tolleshaug)
 * 			  2025-02-13 - Modified source number (N. Tolleshaug)
 * 			  2025-02-17 - Created source list only first time (N. Tolleshaug)
 * 			  2025-02-21 - Populate citation data and update (N. Tolleshaug)
 * 			  2025-02-24 - Added database index update (N. Tolleshaug)
 * 			  2025-02-25 - Added add new citation (N. Tolleshaug)
 * 			  2025-03-02 - Added update of fidelity source citation edit (N. Tolleshaug)
 *			  2025-03-19 - Numerical values for ACCURACY use TINYINT (N. Tolleshaug)
 *			  2025-03-20 - Added findAssessorName() (N. Tolleshaug)
 *			  2025-03-22 - Handling add citation if source table emplty (N. Tolleshaug)
 *			  2025-03-22 - Removed saveCitedData PID console print (N. Tolleshaug)
 *			  2025-04-21 - Add code to pass CITN_GUI_SEQ and handle resetting it (D Ferguson)
 *			  2025-05-26 - Add SORC_FIDELITY into tableSourceData (D Ferguson)
 *			  2025-05-29 - Add assessor data into citationEditData (D Ferguson)
 *			  2025-06-15 - For new project with empty source table add default Source (N. Tolleshaug)
 * *******************************************************************************************
 * Accuracy numerical definitions
 * 		3 = an original source, close in time to the event
 * 		2 = a reliable secondary source
 * 		1 = a less reliable secondary source or an assumption based on other facts in a source
 * 		0 = a guess
 *  	- = -1 the source does not support the information cited or this information has been disproved.
 * 	space = -2 no accuracy recorded
 * 	empty = -3 No data available
 * ********************************************************************************************
 * For Fidelity, TMG set it as
 * 1 = Other, 2 = Original, 3 = Photocopy, 4 = Transcript, 5 = Extract :
 * TMG = 1 - > HRE 'E'
 * TMG = 2 - > HRE 'A'
 * TMG = 3 - > HRE 'B'
 * TMG = 4 - > HRE 'C'
 * TMG = 5 - > HRE 'D'
 *********************************************************************************************/

import java.sql.ResultSet;
import java.sql.SQLException;

import hre.gui.HGlobal;

public class HBCitationSourceHandler extends HBBusinessLayer {

	long null_RPID  = 1999999999999999L;
	long proOffset  = 1000000000000000L;

	HBWhereWhenHandler pointWhereWhenHandler;
	HBProjectOpenData pointOpenProject;
	int dataBaseIndex;

	String selectString;

	Object[][] tableCiteData = null;
	Object[][] tableSourceData =  null;
	String[] accuracy = {"","","","",""};

	long citedRecordRPID, citationTablePID, nextCitationPID, sourceTablePID;
	String citedTableName, sourceTitle, abbrevTitle, ctnDetail, ctnMemo;
	String sourceTypeName, sourceRefTable, sourceMemo, subSource, sourceText, sourceRemind;
	int sourceNumber, majSource, sourceType, sourceRefId;

	public String[] getAccuracyData() {
		return accuracy;
	}

/**
 * Constructor HBCitationSourceHandler
 */
	HBCitationSourceHandler(HBProjectOpenData pointOpenProject) {
		super();
		this.pointOpenProject = pointOpenProject;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
	}

/**
 *
 * @param citedTableName
 * @param citedRecordRPID
 * @throws HBException
 */
	public void setCitedTableData(String citedTableName, long citedRecordRPID) {
		this.citedTableName = citedTableName;
		this.citedRecordRPID = citedRecordRPID;
		if (HGlobal.DEBUG)
			System.out.println(" setCitedTableData table/recordPID: " + citedTableName + "/" + citedRecordRPID);
	}

/**
 * public Object getAssessorData()
 * @return
 * @throws HBException
 */
	public Object getAssessorData() throws HBException {
		return pointLibraryResultSet.getProjectOwner(dataBaseIndex);
	}

/**
 * long createCitationRecord() throws HBException
 * @return
 * @throws HBException
 */
	public long createCitationRecord(long sourceTablePID) throws HBException {
		this.sourceTablePID = sourceTablePID;
		ResultSet eventCitationRS;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		nextCitationPID = lastRowPID(citationTable, pointOpenProject) + 1;
		selectString = setSelectSQL("*", citationTable, " PID = " + (nextCitationPID - 1));
		eventCitationRS = requestTableData(selectString, dataBaseIndex);
		addToCitationT735_CITN(nextCitationPID, eventCitationRS);
		try {
			eventCitationRS.close();
		} catch (SQLException sqle) {
			System.out.println(" createCitationRecord: " + sqle.getMessage());
			sqle.printStackTrace();
		}
		return nextCitationPID;

	}
/**
 * public void deleteCitationRecord(long sourceTablePID) throws HBException {
 * @param sourceTablePID
 * @throws HBException
 */
	public void deleteCitationRecord(long citationTablePID) throws HBException {
		ResultSet eventCitationRS;
		this.citationTablePID = citationTablePID;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", citationTable, " PID = " + citationTablePID);
		eventCitationRS = requestTableData(selectString, dataBaseIndex);
		try {
			eventCitationRS.first();
			eventCitationRS.deleteRow();
		} catch (SQLException sqle) {
			System.out.println(" deleteCitationRecord() error: " + sqle.getMessage());
			sqle.printStackTrace();
		}
	}

/**
 * public Object[][] getCitationSourceData(long eventPID, String tableName)
 * @param eventPID
 * @param tableName
 * @return
 * @throws HBException
 */
	public Object[][] getCitationSourceData(long eventPID, String tableName) throws HBException {
		ResultSet eventCitationRS, eventSourcRS;
		int index = 0;
		long sourcePID;
		char name1Acc, name2Acc, dateAcc, locAcc, memoAcc;
		String sourceTitle;
		Object[][] citationTableData = null;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", citationTable, "CITED_RPID = " + eventPID + " AND OWNER_TYPE = '" + tableName + "'");
		eventCitationRS = requestTableData(selectString, dataBaseIndex);
		try {
			eventCitationRS.last();
			citationTableData = new Object[eventCitationRS.getRow()][5];
			eventCitationRS.beforeFirst();
			while (eventCitationRS.next()) {
				name1Acc = displayAcc(eventCitationRS.getInt("CITN_ACC_NAME1"));
				name2Acc = displayAcc(eventCitationRS.getInt("CITN_ACC_NAME2"));
				dateAcc = displayAcc(eventCitationRS.getInt("CITN_ACC_DATE"));
				locAcc = displayAcc(eventCitationRS.getInt("CITN_ACC_LOCN"));
				memoAcc = displayAcc(eventCitationRS.getInt("CITN_ACC_MEMO"));
				citationTableData[index][2] = name1Acc + " "+  name2Acc + "  " + dateAcc + "  " + locAcc + "  " + memoAcc;
				citationTableData[index][3] = eventCitationRS.getLong("PID");
				citationTableData[index][4] = eventCitationRS.getInt("CITN_GUI_SEQ");
			// Find source title
				sourcePID = eventCitationRS.getLong("SORC_RPID");
				selectString = setSelectSQL("*", sourceTable, "PID = " + sourcePID);
				eventSourcRS = requestTableData(selectString, dataBaseIndex);
				eventSourcRS.first();
			// Source number
				citationTableData[index][0] = eventSourcRS.getInt("SORC_REF");
			// Source title
				sourceTitle = eventSourcRS.getString("SORC_ABBREV").trim();
				citationTableData[index][1] = " " + sourceTitle;
		/*      System.out.println(citationTableData[index][0] + " "
									+ citationTableData[index][1] + " "
									+ citationTableData[index][2]);			*/
				index++;

			}
		} catch (SQLException sqle) {
			System.out.println(" getCitationSourceData() error: " + sqle.getMessage());
			sqle.printStackTrace();
		}
		return citationTableData;
	}

/**
 * private char displayAcc(int accValue)
 * @param accValue
 * @return
 */
	private char displayAcc(int accValue) {
		String accChar;
		if (accValue >= 0) {
			accChar = "" + accValue;
			return accChar.charAt(0);
		}
		if (accValue == -1) return '-';
		if (accValue == -2) return ' ';
		if (accValue == -3) return ' ';
		return 'e';
	}

/**
 * Object[][] getSourceList() throws HBException
 * @return
 * @throws HBException
 */
	public Object[][] getSourceList() throws HBException {
		ResultSet eventCitationRS, eventSourcRS;
		String sourceTitle = "Edit New Source";
		int citedNumber = 0, index = 0;
		long sourcePID = null_RPID;
		String sourceFID = "E";

	// Load pre populated list if done before
		if (tableSourceData != null) return tableSourceData;
		
	// Continue with loading source list
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", sourceTable, "");
		eventSourcRS = requestTableData(selectString, dataBaseIndex);
		try {
		// Return null to HG0555EditCitation
			if (isResultSetEmpty(eventSourcRS)) {
				addToSourceT736_SORC(proOffset + 1, eventSourcRS);
				selectString = setSelectSQL("*", sourceTable, "");
				eventSourcRS = requestTableData(selectString, dataBaseIndex);
				//return null;
			}
		// Continue
			eventSourcRS.last();
			tableSourceData = new Object[eventSourcRS.getRow()][5];
			eventSourcRS.beforeFirst();
			while (eventSourcRS.next()) {
				sourceTitle = eventSourcRS.getString("SORC_ABBREV").trim();
				sourcePID = eventSourcRS.getLong("PID");
				selectString = setSelectSQL("*", citationTable, "SORC_RPID = " + sourcePID);
				eventCitationRS = requestTableData(selectString, dataBaseIndex);
				eventCitationRS.last();
				citedNumber = eventCitationRS.getRow();
				sourceFID = eventSourcRS.getString("SORC_FIDELITY");
				tableSourceData[index][0] = eventSourcRS.getInt("SORC_REF");
				tableSourceData[index][1] = sourceTitle;
				tableSourceData[index][2] = citedNumber;
				tableSourceData[index][3] = sourcePID;
				tableSourceData[index][4] = sourceFID;
				index++;
			}
		} catch (SQLException sqle) {
			System.out.println(" getSourceList() error: " + sqle.getMessage());
			sqle.printStackTrace();
		}
		return tableSourceData;
	}

/**
 * public Object[] getCitationEditData(long citationTablePID)
 * @param citationTablePID
 * @return
 * @throws HBException
 */
	public Object[] getCitationEditData(long citationTablePID) throws HBException {
		ResultSet eventCitationRS, eventSourcRS;
		long sourcePID, assessorRPID;
		String sourceTitle, citationRefer,citationDetail, citationMemo = "", sourceFidelity = "";
		int sourceREF = 0;

		Object[] citationEditData = new Object[8];
		pointHREmemo = new HREmemo(pointDBlayer, dataBaseIndex);
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", citationTable, " PID = " + citationTablePID);
		eventCitationRS = requestTableData(selectString, dataBaseIndex);
		try {
			if (isResultSetEmpty(eventCitationRS)) return citationEditData;
			eventCitationRS.first();
			accuracy[0] =  "" + displayAcc(eventCitationRS.getInt("CITN_ACC_NAME1"));
			accuracy[1] =  "" + displayAcc(eventCitationRS.getInt("CITN_ACC_NAME2"));
			accuracy[2] =  "" + displayAcc(eventCitationRS.getInt("CITN_ACC_DATE"));
			accuracy[3] =  "" + displayAcc(eventCitationRS.getInt("CITN_ACC_LOCN"));
			accuracy[4] =  "" + displayAcc(eventCitationRS.getInt("CITN_ACC_MEMO"));
			citationRefer = eventCitationRS.getString("CITN_REF").trim();

			sourcePID = eventCitationRS.getLong("SORC_RPID");
			selectString = setSelectSQL("*", sourceTable, "PID = " + sourcePID);
			eventSourcRS = requestTableData(selectString, dataBaseIndex);
			eventSourcRS.first();
			sourceREF = eventSourcRS.getInt("SORC_REF");
			sourceTitle = eventSourcRS.getString("SORC_ABBREV").trim();
			sourceFidelity = eventSourcRS.getString("SORC_FIDELITY").trim();
			citationDetail = pointHREmemo.readMemo(eventCitationRS.getLong("CITN_DETAIL_RPID"));
			citationMemo = pointHREmemo.readMemo(eventCitationRS.getLong("CITN_MEMO_RPID"));
			assessorRPID = eventCitationRS.getLong("ASSESSOR_RPID");

			citationEditData[0] = citationRefer;
			citationEditData[1] = sourceREF;
			citationEditData[2] = sourceTitle;
			citationEditData[3] = citationDetail;
			citationEditData[4] = citationMemo;
			citationEditData[5] = pointLibraryResultSet.getUserName(assessorRPID, dataBaseIndex);
			citationEditData[6] = assessorRPID;
			citationEditData[7] = sourceFidelity;

		} catch (SQLException sqle) {
			System.out.println(" getCitationEditData() error: " + sqle.getMessage());
			sqle.printStackTrace();
		}
		return citationEditData;
	}

/**
 * saveCitedData(long citationTablePID, Object[] saveCitedData, String[] accuracy)
 * @param citationTablePID
 * @param saveCitedData
 * @param accuracy
 * @throws HBException
 */
	public void saveCitedData(long citationTablePID, Object[] saveCitedData, int[] accuracySave) throws HBException{
		//System.out.println(" saveCitedData PID: " + citationTablePID + "-" + saveCitedData[0] + "/" + accuracySave[0]);
		ResultSet eventCitationRS, eventSourcRS;
		long sourcePID, memoTablePID;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		pointHREmemo = new HREmemo(pointDBlayer, dataBaseIndex);
		selectString = setSelectSQL("*", citationTable, " PID = " + citationTablePID);
		eventCitationRS = requestTableData(selectString, dataBaseIndex);
		try {
			eventCitationRS.first();
			eventCitationRS.updateInt("CITN_ACC_NAME1", accuracySave[0]);
			eventCitationRS.updateInt("CITN_ACC_NAME2", accuracySave[1]);
			eventCitationRS.updateInt("CITN_ACC_DATE", accuracySave[2]);
			eventCitationRS.updateInt("CITN_ACC_LOCN", accuracySave[3]);
			eventCitationRS.updateInt("CITN_ACC_MEMO", accuracySave[4]);
			eventCitationRS.updateString("CITN_REF", (String)saveCitedData[0]);
			eventCitationRS.updateLong("ASSESSOR_RPID", (long)saveCitedData[5]);
			sourcePID = eventCitationRS.getLong("SORC_RPID");
			selectString = setSelectSQL("*", sourceTable, "PID = " + sourcePID);
			eventSourcRS = requestTableData(selectString, dataBaseIndex);
			eventSourcRS.first();
			eventSourcRS.updateInt("SORC_REF", (int)saveCitedData[1]);
			eventSourcRS.updateString("SORC_ABBREV", (String) saveCitedData[2]);

	// Update or create detail text
			memoTablePID = eventCitationRS.getLong("CITN_DETAIL_RPID");
			if (memoTablePID == null_RPID)
				eventCitationRS.updateLong("CITN_DETAIL_RPID", pointHREmemo.addMemoRecord((String)saveCitedData[3]));
			else
				pointHREmemo.findT167_MEMOrecord((String)saveCitedData[3], memoTablePID);
	// Update or create memo text
			memoTablePID = eventCitationRS.getLong("CITN_MEMO_RPID");
			if (memoTablePID == null_RPID)
				eventCitationRS.updateLong("CITN_MEMO_RPID", pointHREmemo.addMemoRecord((String)saveCitedData[4]));
			else
				pointHREmemo.findT167_MEMOrecord((String)saveCitedData[4], memoTablePID);
	// Update tables
			eventCitationRS.updateRow();
			eventSourcRS.updateRow();

		} catch (SQLException sqle) {
			System.out.println(" saveCitedData() error: " + sqle.getMessage());
			sqle.printStackTrace();
		}
	}

/**
 * addToCitationT735_CITN(long indexPID, ResultSet hreTable)
 * @param indexPID
 * @param hreTable
 * CREATE TABLE T735_CITN(
				PID BIGINT NOT NULL,
				CL_COMMIT_RPID BIGINT NOT NULL,
				CITED_RPID BIGINT NOT NULL,
				OWNER_TYPE CHAR(4) NOT NULL,
				SORC_RPID BIGINT NOT NULL,
				ASSESSOR_RPID BIGINT NOT NULL,
				CITN_DETAIL_RPID BIGINT NOT NULL,
				CITN_MEMO_RPID BIGINT NOT NULL,
				CITN_REF CHAR(30) NOT NULL,
				CITN_GUI_SEQ SMALLINT NOT NULL,
				CITN_ACC_NAME1 TINYINT NOT NULL,
				CITN_ACC_NAME2 TINYINT NOT NULL,
				CITN_ACC_DATE TINYINT NOT NULL,
				CITN_ACC_LOCN TINYINT NOT NULL,
				CITN_ACC_MEMO TINYINT NOT NULL
 */
	private void addToCitationT735_CITN(long T735tablePID, ResultSet hreTable) {
		try {
			int guiSeq = 0;
			// Find largest current CITN_GUI_SEQ value
			hreTable.beforeFirst();
			while (hreTable.next()) {
				if (hreTable.getInt("CITN_GUI_SEQ") > guiSeq) {
					guiSeq = hreTable.getInt("CITN_GUI_SEQ");
				}
			}
		    // Move cursor to the insert row
				hreTable.moveToInsertRow();
			// Update new row in H2 database
				hreTable.updateLong("PID", T735tablePID);
				hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
				hreTable.updateLong("CITED_RPID", citedRecordRPID);
				hreTable.updateString("OWNER_TYPE", citedTableName);
				hreTable.updateLong("SORC_RPID", sourceTablePID);
				hreTable.updateLong("ASSESSOR_RPID", null_RPID);

			// Processing detial to T167_MEMO_SET
				hreTable.updateLong("CITN_DETAIL_RPID", null_RPID);

			// Processing memo to T167_MEMO_SET
				hreTable.updateLong("CITN_MEMO_RPID", null_RPID);

				hreTable.updateString("CITN_REF", "");
				hreTable.updateInt("CITN_GUI_SEQ", guiSeq + 1);
				hreTable.updateInt("CITN_ACC_NAME1",-3);
				hreTable.updateInt("CITN_ACC_NAME2",-3);
				hreTable.updateInt("CITN_ACC_DATE", -3);
				hreTable.updateInt("CITN_ACC_LOCN", -3);
				hreTable.updateInt("CITN_ACC_MEMO", -3);
			//Insert row
				hreTable.insertRow();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}
	
	
/**
 * private void addToSourceT736_SORC(long T736tablePID, ResultSet hreTable)	
 * @param T736tablePID
 * @param hreTable
 *  * CREATE TABLE T736_SORC( 
	PID BIGINT NOT NULL,
	CL_COMMIT_RPID BIGINT NOT NULL,
	IS_ACTIVE BOOLEAN NOT NULL,
	SORC_REF SMALLINT NOT NULL,
	SORC_TYPE SMALLINT NOT NULL,
	SORC_FIDELITY CHAR(1) NOT NULL,
	SORC_TEXT_RPID BIGINT NOT NULL,
	SORC_AUTHOR_RPID BIGINT NOT NULL,
	SORC_EDITOR_RPID BIGINT NOT NULL,
	SORC_COMPILER_RPID BIGINT NOT NULL,
	SORC_ABBREV CHAR(50) NOT NULL,
	SORC_TITLE VARCHAR(400) NOT NULL,
	SORC_FULLFORM VARCHAR(500) NOT NULL,
	SORC_SHORTFORM VARCHAR(500) NOT NULL,
	SORC_BIBLIOFORM VARCHAR(500) NOT NULL,
	SORC_REMIND_RPID BIGINT NOT NULL
 */
	private void addToSourceT736_SORC(long T736tablePID, ResultSet hreTable) {
		int sourceReference = 0;
		String sourceTitle = "Default Source", sourceAbbrev = "Default Source";
		try {
			hreTable.beforeFirst();
			while (hreTable.next()) {
				if (hreTable.getInt("SORC_REF") > sourceReference) {
					sourceReference = hreTable.getInt("SORC_REF");
				}
			}
			
		// Move cursor to the insert row
				hreTable.moveToInsertRow();
		// Update new row in H2 database
				hreTable.updateLong("PID", T736tablePID);
				hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
				hreTable.updateBoolean("IS_ACTIVE", true);
				hreTable.updateInt("SORC_REF", sourceReference + 1);
				hreTable.updateInt("SORC_TYPE",1);
				hreTable.updateString("SORC_FIDELITY","C");			
				hreTable.updateLong("SORC_TEXT_RPID",null_RPID);
				hreTable.updateLong("SORC_AUTHOR_RPID",null_RPID);
				hreTable.updateLong("SORC_EDITOR_RPID",null_RPID);
				hreTable.updateLong("SORC_COMPILER_RPID",null_RPID);
				hreTable.updateString("SORC_ABBREV", sourceAbbrev);
				hreTable.updateString("SORC_TITLE", sourceTitle);
				hreTable.updateString("SORC_FULLFORM", sourceTitle);
				hreTable.updateString("SORC_SHORTFORM", sourceTitle);
				hreTable.updateString("SORC_BIBLIOFORM", sourceTitle);		
				hreTable.updateLong("SORC_REMIND_RPID",null_RPID);
			//Insert row
				hreTable.insertRow();
				
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

/*
CREATE TABLE T737_SORC_DEFN( 
	PID BIGINT NOT NULL,
	CL_COMMIT_RPID BIGINT NOT NULL,
	SORC_DEFN_TYPE SMALLINT NOT NULL,
	SORC_DEFN_NAME CHAR(66) NOT NULL,
	SORC_LANG CHAR(5) NOT NULL,
	SORC_DEFN_FULLFOOT VARCHAR(500) NOT NULL,
	SORC_DEFN_SHORTFOOT VARCHAR(500) NOT NULL,
	SORC_DEFN_BIBLIO VARCHAR(500) NOT NULL,
	SORC_DEFN_REMIND_RPID BIGINT NOT NULL
	);
ALTER TABLE T737_SORC_DEFN ADD PRIMARY KEY (PID);

CREATE TABLE T738_SORC_ELMNT( 
	PID BIGINT NOT NULL,
	CL_COMMIT_RPID BIGINT NOT NULL,
	SORC_ELMNT_NUM CHAR(5) NOT NULL,
	SORC_ELMNT_LANG CHAR(5) NOT NULL,
	SORC_ELMNT_NAME CHAR(30) NOT NULL
	);
ALTER TABLE T738_SORC_ELMNT ADD PRIMARY KEY (PID);
 */

/**
 * updateCiteGUIseq()
 * @param eventPID
 * @param tableName
 * @param objCiteData
 * @throws HBException
 */
	public void updateCiteGUIseq(long eventPID, String tableName, Object objCiteData[][]) throws HBException {
		int sourceNum = 0;
		int rows = objCiteData.length;
		ResultSet eventCitationRS, sourceRS;
		long sourcePID;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", citationTable, "CITED_RPID = " + eventPID + " AND OWNER_TYPE = '" + tableName + "'");
		eventCitationRS = requestTableData(selectString, dataBaseIndex);
		try {
			eventCitationRS.beforeFirst();
			while (eventCitationRS.next()) {
				// First, get the source record for this citation
				sourcePID = eventCitationRS.getLong("SORC_RPID");
				selectString = setSelectSQL("*", sourceTable, "PID = " + sourcePID);
				sourceRS = requestTableData(selectString, dataBaseIndex);
				sourceRS.first();
				// Get its Source number
				sourceNum = sourceRS.getInt("SORC_REF");
				// Match this SourceNum field with a citeData source number field,
				// and set its GUI_SEQ to match the row value in objCiteData
				for (int i=0; i < rows; i++) {
					if (sourceNum == (int)objCiteData[i][0]) {
						eventCitationRS.updateInt("CITN_GUI_SEQ", i);
						eventCitationRS.updateRow();
						break;
					}
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException("HBCitationSourceHandler - " + sqle.getMessage());
		}
	}

}
