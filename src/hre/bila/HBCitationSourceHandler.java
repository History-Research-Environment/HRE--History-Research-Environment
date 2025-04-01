package hre.bila;
/************************************************************************************************
 * Class  HBCitationSourceHandler extends BusinessLayer
 * Processes data for Citation Source Menu in User GUI
 * Receives requests from User GUI to action methods
 * Sends requests to database over Database Layer API
 ************************************************************************************************
 * v0.00.0032 2025-02-12 - First version (N. Tolleshaug)
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
 ***********************************************************************************************/
import java.sql.ResultSet;
import java.sql.SQLException;

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

	public String[] getAccoracyData() {
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
	}

/**
 * public String findAssessorName()
 * @return
 * @throws HBException
 */
	public String findAssessorName() throws HBException {
		return pointLibraryResultSet.findActiveUser(dataBaseIndex);
	}

/**
 * long createCitationRecord() throws HBException
 * @return
 * @throws HBException
 */
	public long createCitationRecord(long sourceTablePID) throws HBException {
		this.sourceTablePID = sourceTablePID;
		ResultSet eventCitiationRS;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		nextCitationPID = lastRowPID(citationTable, pointOpenProject) + 1;
		selectString = setSelectSQL("*", citationTable, " PID = " + (nextCitationPID - 1));
		eventCitiationRS = requestTableData(selectString, dataBaseIndex);
		addToCitationT735_CITN(nextCitationPID, eventCitiationRS);
		try {
			eventCitiationRS.close();
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
		ResultSet eventCitiationRS;
		this.citationTablePID = citationTablePID;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", citationTable, " PID = " + citationTablePID);
		eventCitiationRS = requestTableData(selectString, dataBaseIndex);
		try {
			eventCitiationRS.first();
			eventCitiationRS.deleteRow();
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
		ResultSet eventCitiationRS, eventSourcRS;
		int index = 0;
		long sourcePID;
		char name1Acc, name2Acc, dateAcc, locAcc, memoAcc;
		String sourceTitle;
		Object[][] citationTableData = null;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", citationTable, "CITED_RPID = " + eventPID + " AND OWNER_TYPE = '" + tableName + "'");
		eventCitiationRS = requestTableData(selectString, dataBaseIndex);
		try {
			eventCitiationRS.last();
			citationTableData = new Object[eventCitiationRS.getRow()][4];
			eventCitiationRS.beforeFirst();
			while (eventCitiationRS.next()) {
				name1Acc = displayAcc(eventCitiationRS.getInt("CITN_ACC_NAME1"));
				name2Acc = displayAcc(eventCitiationRS.getInt("CITN_ACC_NAME2"));
				dateAcc = displayAcc(eventCitiationRS.getInt("CITN_ACC_DATE"));
				locAcc = displayAcc(eventCitiationRS.getInt("CITN_ACC_LOCN"));
				memoAcc = displayAcc(eventCitiationRS.getInt("CITN_ACC_MEMO"));
				citationTableData[index][2] = name1Acc + " "+  name2Acc + "  " + dateAcc + "  " + locAcc + "  " + memoAcc;
				citationTableData[index][3] = eventCitiationRS.getLong("PID");
			// Find source title
				sourcePID = eventCitiationRS.getLong("SORC_RPID");
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
		ResultSet eventCitiationRS, eventSourcRS;
		String sourceTitle = "Edit New Source";
		int citedNumber = 0, index = 0;
		long sourcePID = null_RPID;

	// Load pre populated list if done before
		if (tableSourceData != null) return tableSourceData;

	// Continue with loading source list
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", sourceTable, "");
		eventSourcRS = requestTableData(selectString, dataBaseIndex);
		try {
		// Return null to HG0555EditCitation
			if (isResultSetEmpty(eventSourcRS)) return null;
		// Continue
			eventSourcRS.last();
			tableSourceData = new Object[eventSourcRS.getRow()][4];
			eventSourcRS.beforeFirst();
			while (eventSourcRS.next()) {
				sourceTitle = eventSourcRS.getString("SORC_ABBREV").trim();
				sourcePID = eventSourcRS.getLong("PID");
				selectString = setSelectSQL("*", citationTable, "SORC_RPID = " + sourcePID);
				eventCitiationRS = requestTableData(selectString, dataBaseIndex);
				eventCitiationRS.last();
				citedNumber = eventCitiationRS.getRow();
				tableSourceData[index][0] = eventSourcRS.getInt("SORC_REF");
				tableSourceData[index][1] = sourceTitle;
				tableSourceData[index][2] = citedNumber;
				tableSourceData[index][3] = sourcePID;
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
		ResultSet eventCitiationRS, eventSourcRS;
		long sourcePID, assessorRPID;
		String sourceTitle, citationRefer,citationDetail, citationMemo = "", sourceFidelity = "";
		int sourceREF = 0;
		//accuracy = new String[5]; -

		Object[] citationEditData = new Object[7];
		pointHREmemo = new HREmemo(pointDBlayer, dataBaseIndex);
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", citationTable, " PID = " + citationTablePID);
		eventCitiationRS = requestTableData(selectString, dataBaseIndex);
		try {
			if (isResultSetEmpty(eventCitiationRS)) return citationEditData;
			eventCitiationRS.first();
			accuracy[0] =  "" + displayAcc(eventCitiationRS.getInt("CITN_ACC_NAME1"));
			accuracy[1] =  "" + displayAcc(eventCitiationRS.getInt("CITN_ACC_NAME2"));
			accuracy[2] =  "" + displayAcc(eventCitiationRS.getInt("CITN_ACC_DATE"));
			accuracy[3] =  "" + displayAcc(eventCitiationRS.getInt("CITN_ACC_LOCN"));
			accuracy[4] =  "" + displayAcc(eventCitiationRS.getInt("CITN_ACC_MEMO"));
			citationRefer = eventCitiationRS.getString("CITN_REF").trim();

			sourcePID = eventCitiationRS.getLong("SORC_RPID");
			selectString = setSelectSQL("*", sourceTable, "PID = " + sourcePID);
			eventSourcRS = requestTableData(selectString, dataBaseIndex);
			eventSourcRS.first();
			sourceREF = eventSourcRS.getInt("SORC_REF");
			sourceTitle = eventSourcRS.getString("SORC_ABBREV").trim();
			sourceFidelity = eventSourcRS.getString("SORC_FIDELITY").trim();
			citationDetail = pointHREmemo.readMemo(eventCitiationRS.getLong("CITN_DETAIL_RPID"));
			citationMemo = pointHREmemo.readMemo(eventCitiationRS.getLong("CITN_MEMO_RPID"));
			assessorRPID = eventCitiationRS.getLong("ASSESSOR_RPID");
/*
			System.out.println(" getCitationEditData ref: " + citationRefer
										+ " \n Title: " + sourceTitle
										+ " \n Detail: " + citationDetail
										+ " \n Fidelity: " + sourceFidelity
										+ " \n Memo: " + citationMemo);
			System.out.println(" Accuracy:");
			System.out.println(" 1 2 D P M ");
			for (int i = 0; i < accuracy.length; i ++)
				System.out.print(" " + accuracy[i]);
			System.out.println();
*/
			citationEditData[0] = citationRefer;
			citationEditData[1] = sourceREF;
			citationEditData[2] = sourceTitle;
			citationEditData[3] = citationDetail;
			citationEditData[4] = citationMemo;
			citationEditData[5] = assessorRPID;
			citationEditData[6] = sourceFidelity;

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
		ResultSet eventCitiationRS, eventSourcRS;
		long sourcePID, memoTablePID;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		//accuracy = new String[5];
		pointHREmemo = new HREmemo(pointDBlayer, dataBaseIndex);
		selectString = setSelectSQL("*", citationTable, " PID = " + citationTablePID);
		eventCitiationRS = requestTableData(selectString, dataBaseIndex);
		try {
			eventCitiationRS.first();
			eventCitiationRS.updateInt("CITN_ACC_NAME1", accuracySave[0]);
			eventCitiationRS.updateInt("CITN_ACC_NAME2", accuracySave[1]);
			eventCitiationRS.updateInt("CITN_ACC_DATE", accuracySave[2]);
			eventCitiationRS.updateInt("CITN_ACC_LOCN", accuracySave[3]);
			eventCitiationRS.updateInt("CITN_ACC_MEMO", accuracySave[4]);
			eventCitiationRS.updateString("CITN_REF", (String)saveCitedData[0]);

			sourcePID = eventCitiationRS.getLong("SORC_RPID");
			selectString = setSelectSQL("*", sourceTable, "PID = " + sourcePID);
			eventSourcRS = requestTableData(selectString, dataBaseIndex);
			eventSourcRS.first();
			eventSourcRS.updateInt("SORC_REF", (int)saveCitedData[1]);
			eventSourcRS.updateString("SORC_ABBREV", (String) saveCitedData[2]);

			//eventSourcRS.updateString("SORC_FIDELITY", (String) saveCitedData[6]);
	// Update or create detail text
			memoTablePID = eventCitiationRS.getLong("CITN_DETAIL_RPID");
			if (memoTablePID == null_RPID)
				eventCitiationRS.updateLong("CITN_DETAIL_RPID", pointHREmemo.addMemoRecord((String)saveCitedData[3]));
			else
				pointHREmemo.findT167_MEMOrecord((String)saveCitedData[3], memoTablePID);
	// Update or crete memo text
			memoTablePID = eventCitiationRS.getLong("CITN_MEMO_RPID");
			if (memoTablePID == null_RPID)
				eventCitiationRS.updateLong("CITN_MEMO_RPID", pointHREmemo.addMemoRecord((String)saveCitedData[4]));
			else
				pointHREmemo.findT167_MEMOrecord((String)saveCitedData[4], memoTablePID);
	// Update tables
			eventCitiationRS.updateRow();
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
				CITN_ACC_NAME CHAR(1) NOT NULL,
				CITN_ACC_DATE CHAR(1) NOT NULL,
				CITN_ACC_LOCN CHAR(1) NOT NULL,
				CITN_ACC_MEMO CHAR(1) NOT NULL
 */
	private void addToCitationT735_CITN(long T735tablePID, ResultSet hreTable) {
		try {
		    // moves cursor to the insert row
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
				hreTable.updateInt("CITN_GUI_SEQ", 1);
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

	//sourceFidelity = (String) citationEditData[6];
/*
	public String fidelityConvert(String fidelityText) {
		String fidelityCode;
		if (fidelityText.equals("Original")) fidelityCode = "A";
		else if (fidelityText.equals("Copy")) fidelityCode = "B";
		else if (fidelityText.equals("Transcript")) fidelityCode = "C";
		else if (fidelityText.equals("Extract")) fidelityCode = "D";
		else if (fidelityText.equals("Other")) fidelityCode = "E";
		else fidelityCode = "O";
		return fidelityCode;
	}	*/
}
