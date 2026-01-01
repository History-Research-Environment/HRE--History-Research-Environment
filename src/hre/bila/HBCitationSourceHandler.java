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
 *			  2025-09-02 - Add routine to return SourcElmnt table (D Ferguson)
 * 			  2025-09-03 - Add routine to return Repository table (D Ferguson)
 * 			  2025-09-04 - Add routine to return Source Defn table (D Ferguson)
 * 			  2025-09-23 - Add routine to return Source Defn templates (D Ferguson)
 * 			  2025-09-26 - Add routine to return Source Defn Edit data (D Ferguson)
 * 			  2025-09-28 - Add routine to return Source Edit data (D Ferguson)
 * 			  2025-09-29 - Add routine to return Source Elmnt values (T734) (D Ferguson)
 * 			  2025-10-03 - Update handling add citation if source table emplty (N. Tolleshaug)
 * 			  2025-10-04 - Methods for Add source definition and add element  (N. Tolleshaug)
 * 			  2025-10-05 - Add getRepoPIDs routine (D Ferguson)
 * 			  2025-10-07 - Add sourcePID to data returned by getCitationEditData (D Ferguson)
 * 						 - Add routine to return just Source Templates (D Ferguson)
 * 			  2025-10-12 - Partly updated for Add/Update source  (N. Tolleshaug)
 * 			  2025-10-16 - Updated Exception handling (N. Tolleshaug)
 * 			  2025-10-17 - Handling edit and add new source elements (N. Tolleshaug)
 * 			  2025-10-20 - New project create first new source (N. Tolleshaug)
 * 			  2025-10-24 - Update add/delete citation for source (N. Tolleshaug)
 * 			  2025-11-02 - Move Repo routines to HBRepositoryHandler (D Ferguson)
 * 			  2025-11-09 - Update delete source also source/repos links (N. Tolleshaug)
 * 			  2025-11-28 - Implemented author, editor and compiler name edit (N. Tolleshaug)
 *            2025-11-30 - Implemented option for select or delete editorial names (N. Tolleshaug)
 *            2025-12-01 - Implemented show inactive sources (N. Tolleshaug)
 *            2025-12-02 - Fix for show inactive sources empty (N. Tolleshaug)
 *            2025-12-03 - Implemented copy and create new source copy (N Tolleshaug)
 *            2025-12-07 - Implemented add new source element (N Tolleshaug)
 *            2025-12-09 - Initial values sourceAuthorPID = null_RPID, sourceEditorPID = null_RPID,
 *            			   sourceCompilerPID = null_RPID (N Tolleshaug)
 *            2025-12-11 - Implemented translate source element name (N Tolleshaug)
 *            2025-12-14 - Modify getSourceDefnList to load by language (D Ferguson)
 *			  2025-12-14 - Implemented edit source element name (N Tolleshaug)
 *		      2025-12-22 - Add, edit and copy source type implemented (N. Tolleshaug)
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
import java.util.HashMap;
import java.util.Map;

import hre.gui.HG0507SelectPerson;
import hre.gui.HG0564ManageSrcElmnt;
import hre.gui.HG0566EditSource;
import hre.gui.HG0567ManageSourceType;
import hre.gui.HG0568EditSourceType;
import hre.gui.HGlobal;

public class HBCitationSourceHandler extends HBBusinessLayer {

	long null_RPID  = 1999999999999999L;
	long proOffset  = 1000000000000000L;

	HBWhereWhenHandler pointWhereWhenHandler;
	HBProjectOpenData pointOpenProject;
	HREmemo pointHREmemo;
	HG0507SelectPerson pointSelectPerson;
	HG0566EditSource pointEditSource;
	HG0568EditSourceType pointEditSourceType;
	public HG0567ManageSourceType pointManageSourceType;

	int dataBaseIndex;

	String selectString;

	Object[][] tableCiteData = null;
	Object[][] tableSourceData =  null;
	String[][] tableSourceElmntData = null;
	String[][] tableSourceElmntDataValues = null;
	Object[][] tableSourceDefnData = null;
	String[] sourceDefnTemplates = {" - "," - "," - "};
	String[] accuracy = {"","","","",""};

	public final static int elementIDmin = 35000;
	public final static int elementIDmax = 40000;
	
// Cite variables
	long citedRecordRPID, citationTablePID, nextCitationPID;
	public String citedTableName;
	String ctnDetail;
	String ctnMemo;

// Source T736 variables
	long sourceTablePID, sourceDefinTablePID, nextSourceTablePID, sourceDefnPID,
		  sourceAuthorPID = null_RPID, sourceEditorPID = null_RPID, sourceCompilerPID = null_RPID,
		  reminderTablePID, referenceTablePID;
	
	String sourceTitle = "New source", sourceAbbrev = "New source", sourceTypeName, 
			sourceFidelity = "", sourceReference = "",
			sourceMemo, sourceReminder = "", subSource, sourceText, 
			sourceFullFoot = "", sourceShortFoot = "", sourceBiblio = "";
	
// Source definition T737 varibales
	String sourceDefinName = "Default Source Definition", sourceDefFullFoot = "", sourceDefShortFoot = "", sourceDefBiblio = "",
			sourceDefReminder = "", sourceDefLanguage = "en-US";
	
	int sourceNumber, majSource, sourceRefId, sourceDefType;
	int textFieldSelection;
	boolean sourceActive;

// Sourcce element T738 data
	String sourceElementNumber, sourceElementData;
	long nextSourceElementDataPID;
	long nextSourceElementTablePID;

// Data for source element definition
	String sourceElementIdent, sourceElementLanguage, sourceElementName;

	// Records the name element changes from HG0566EditSource
	HashMap<String,String> elementNameDataChanges = new HashMap<String,String>();

/**
 * public String[] getAccuracyData()
 * @return
 */
	public String[] getAccuracyData() {
		return accuracy;
	}

/**
 * public String getPersonName(long personTablePID)
 * @param personTablePID
 * @return
 * @throws HBException
 */
	public String getPersonName(long personTablePID) throws HBException {
		String[] personNameStyle = {"","",""};
		String personName = "";
		if (personTablePID > proOffset && personTablePID != null_RPID) {
				personNameStyle =  getNameStyleOutputCodes(nameStylesOutput, "N", dataBaseIndex);
				personName = pointLibraryResultSet.exstractPersonName(personTablePID, personNameStyle, dataBaseIndex);
		}
		return personName;
	}

/**
 * public void activateSelectPerson(HBProjectOpenData pointOpenProject)
 * @param pointOpenProject
 * @throws HBException
 */
	public HG0507SelectPerson activateSelectPerson(HBProjectOpenData pointOpenProject,
										HG0566EditSource pointEditSource,
										int textFieldSelection) throws HBException {
		this.textFieldSelection = textFieldSelection;
		this.pointEditSource = pointEditSource;
		HBPersonHandler pointPersonHandler = pointOpenProject.getPersonHandler();
		pointSelectPerson = new HG0507SelectPerson(pointPersonHandler, pointOpenProject, false);
		return pointSelectPerson;
	}

/**
 * updateAutorsName(long personTablePID)
 * @param personTablePID
 */
	public void updatePersonName(long personTablePID) {
		if (textFieldSelection == 1) sourceAuthorPID = personTablePID;
		else if (textFieldSelection == 2) sourceEditorPID = personTablePID;
		else if (textFieldSelection == 3) sourceCompilerPID = personTablePID;

		pointEditSource.resetNames(sourceAuthorPID, sourceEditorPID, sourceCompilerPID);
	}

	public void updateTextFieldSelection(HG0566EditSource pointEditSource, int textFieldSelection) {
		this.pointEditSource = pointEditSource;
		this.textFieldSelection = textFieldSelection;
	}

/**
 * Constructor HBCitationSourceHandler
 */
	HBCitationSourceHandler(HBProjectOpenData pointOpenProject) {
		super();
		this.pointOpenProject = pointOpenProject;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		pointHREmemo = new HREmemo(pointDBlayer, dataBaseIndex);
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
		pointHREmemo = new HREmemo(pointDBlayer, dataBaseIndex);
		nextCitationPID = lastRowPID(citationTable, pointOpenProject) + 1;
		selectString = setSelectSQL("*", citationTable, " PID = " + (nextCitationPID - 1));
		eventCitationRS = requestTableData(selectString, dataBaseIndex);
		addToCitationT735_CITN(nextCitationPID, eventCitationRS);
		try {
			eventCitationRS.close();
		} catch (SQLException sqle) {
			System.out.println(" createCitationRecord close error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException("createCitationRecord close error: " + sqle.getMessage());
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
			throw new HBException(" deleteCitationRecord() error: " + sqle.getMessage());
		}
	}

/**
 * public long createSourceRecord(long sourceDefinTablePID) throws HBException
 * @return
 * @throws HBException
 */
	public long createSourceRecord(Object[] sourceStoreData) throws HBException {
// Transfer data from Gui to handler
		sourceTitle = (String) sourceStoreData[0] ;				//	used
		sourceAbbrev		= (String) sourceStoreData[1] ;	//	used
		//sourceRef			= (String) sourceStoreData[2] ;		// Reused		//	not much for this here!
		//sourceType		= sourceStoreData[3] ;				//	ditto!
		sourceActive		= (boolean) sourceStoreData[4] ;	//	used
		sourceFidelity		= (String) sourceStoreData[5] ;		//	used
		sourceFullFoot		= (String) sourceStoreData[6];   	//	used
		sourceShortFoot		= (String) sourceStoreData[7] ;		//  used
		sourceBiblio		= (String) sourceStoreData[8] ;		//  used
		sourceReference		= (String) sourceStoreData[9];		//  used
		sourceReminder		= (String) sourceStoreData[10] ; 	//  used
		sourceDefnPID		= (long) sourceStoreData[11] ;		//  used to get SourceDefn templates and Defn Name
		sourceAuthorPID		= (long) sourceStoreData[12] ;      //  used
		sourceEditorPID		= (long) sourceStoreData[13] ;		//  used
		sourceCompilerPID	= (long) sourceStoreData[14] ;		//  used

		ResultSet sourceTableRS;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		try {
			pointHREmemo = new HREmemo(pointDBlayer, dataBaseIndex);
			nextSourceTablePID = lastRowPID(sourceTable, pointOpenProject) + 1;
			selectString = setSelectSQL("*", sourceTable, " PID = " + (nextSourceTablePID - 1));
			sourceTableRS = requestTableData(selectString, dataBaseIndex);
			addToSourceT736_SORC(nextSourceTablePID, sourceTableRS);
			sourceTableRS.close();
		} catch (SQLException sqle) {
			System.out.println(" createSourceRecord close error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException("  createSourceRecord close error: " + sqle.getMessage());
		}
		return nextSourceTablePID;
	}

/**
 * public long createSourceRecord(long sourceDefinTablePID) throws HBException
 * @return
 * @throws HBException
 */
	public long updateSourceRecord(long sourcePID, Object[] sourceStoreData) throws HBException {
		this.sourceTablePID = sourcePID;
// Transfer data from Gui to handler
		sourceTitle = (String) sourceStoreData[0] ;				//	used
		sourceAbbrev		= (String) sourceStoreData[1] ;	//	used
		//sourceRef			= sourceStoreData[2] ;				//	not much for this here!
		//sourceType		= sourceStoreData[3] ;				//	ditto!
		sourceActive		= (boolean) sourceStoreData[4] ;	//	used
		sourceFidelity		= (String) sourceStoreData[5] ;		//	used
		sourceFullFoot		= (String) sourceStoreData[6];   	//	used
		sourceShortFoot		= (String) sourceStoreData[7] ;		//  used
		sourceBiblio		= (String) sourceStoreData[8] ;		//  used
		sourceReference		= (String) sourceStoreData[9];		//  used
		sourceReminder		= (String) sourceStoreData[10] ; 	//  used
		sourceDefnPID		= (long) sourceStoreData[11] ;		//  used to get SourceDefn templates and Defn Name
		sourceAuthorPID		= (long) sourceStoreData[12] ;
		sourceEditorPID		= (long) sourceStoreData[13] ;
		sourceCompilerPID	= (long) sourceStoreData[14] ;

		ResultSet sourceTableRS;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", sourceTable, " PID = " + sourceTablePID);
		sourceTableRS = requestTableData(selectString, dataBaseIndex);
		saveSourceDataT736_SORC(sourceTableRS);
		try {
			sourceTableRS.close();
		} catch (SQLException sqle) {
			System.out.println(" updateSourceRecord: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" updateSourceRecord: " + sqle.getMessage());
		}
		return nextCitationPID;
	}

/**
 * public void deleteSourceRecord(long sourceTablePID) throws HBException {
 * @param sourceTablePID
 * @throws HBException
 * NOTE:
 * 1 - Delete only if source not used by citations
 * 2 - Delete memoes if used
 * 3 - Delete source citations
 */
	public void deleteSourceRecord(long sourceTablePID) throws HBException {
		ResultSet sourceTabelRS, sourceElementDataRS, sourecRespositoryLinkRS;
		this.sourceTablePID = sourceTablePID;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", sourceTable, " PID = " + sourceTablePID);
		sourceTabelRS = requestTableData(selectString, dataBaseIndex);
		selectString = setSelectSQL("*", sourceDataTable, " SORC_OWNER_RPID = " + sourceTablePID);
		sourceElementDataRS = requestTableData(selectString, dataBaseIndex);
		selectString = setSelectSQL("*", sourceLinkTable, " SORC_RPID = " + sourceTablePID);
		sourecRespositoryLinkRS = requestTableData(selectString, dataBaseIndex);
	// Start transaction
		updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
		try {
	// Delete source
			sourceTabelRS.first();
			sourceTabelRS.deleteRow();
	// Delete source element data
			if (!isResultSetEmpty(sourceElementDataRS)) {
				sourceElementDataRS.beforeFirst();
				while (sourceElementDataRS.next()) sourceElementDataRS.deleteRow();
			}
	// Delete source/repos links
			if (!isResultSetEmpty(sourecRespositoryLinkRS)) {
				sourecRespositoryLinkRS.beforeFirst();
				while (sourecRespositoryLinkRS.next()) sourecRespositoryLinkRS.deleteRow();
			}
	// End transaction
			updateTableData("COMMIT", dataBaseIndex);
		} catch (SQLException sqle) {
	// Role back transaction
			updateTableData("ROLLBACK", dataBaseIndex);
			sqle.printStackTrace();
			throw new HBException(" deleteCitationRecord() error:  " + sqle.getMessage());
		}
	}

/**
 * createSourceDefRecord(Object[] sourceDefData) throws HBException
 * @param sourceDefData
 * @throws HBException
 */
	public long createSourceDefRecord(Object[] sourceDefData) throws HBException {
		long nextT737tablePID = null_RPID;
		ResultSet sourceDefinTableRS = null;	
		sourceDefinName = (String) sourceDefData[0];
		sourceDefLanguage = (String) sourceDefData[2];
		sourceDefFullFoot = (String) sourceDefData[3];
		sourceDefShortFoot = (String) sourceDefData[4];
		sourceDefBiblio = (String) sourceDefData[5];
		sourceDefReminder = (String) sourceDefData[6];
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		try {
			pointHREmemo = new HREmemo(pointDBlayer, dataBaseIndex);
			nextT737tablePID = lastRowPID(sourceDefnTable, pointOpenProject) + 1;
			selectString = setSelectSQL("*", sourceDefnTable, "");
			sourceDefinTableRS = requestTableData(selectString, dataBaseIndex);
			addToSourDefT737_SORC_DEFN(nextT737tablePID, sourceDefinTableRS);
			sourceDefinTableRS.close();
		} catch (SQLException sqle) {
			System.out.println(" createSourceDefRecord close error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException("  createSourceDefRecord close error: " + sqle.getMessage());
		}
		pointManageSourceType.resetSorceDefinTable();
		return nextT737tablePID;
		
	}
	
/**
 * createSourceDefRecord(Object[] sourceDefData) throws HBException
 * @param sourceDefData
 * @throws HBException
 */
	public void updateSourceDefRecord(long T737tablePID, Object[] sourceDefData) throws HBException {
		ResultSet sourceDefinTableRS = null;	
		sourceDefinName = (String) sourceDefData[0];
		//sourceDefType = (int) Integer.parseInt((String)sourceDefData[1]);
		//sourceDefLanguage = (String) sourceDefData[2];
		sourceDefFullFoot = (String) sourceDefData[3];
		sourceDefShortFoot = (String) sourceDefData[4];
		sourceDefBiblio = (String) sourceDefData[5];
		sourceDefReminder = (String) sourceDefData[6];
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		//System.out.println(" Source def PID: " + T737tablePID);
		try {
			pointHREmemo = new HREmemo(pointDBlayer, dataBaseIndex);
			selectString = setSelectSQL("*", sourceDefnTable, "PID = " + T737tablePID);
			sourceDefinTableRS = requestTableData(selectString, dataBaseIndex);
			sourceDefinTableRS.first();
			updateSourDefT737_SORC_DEFN(sourceDefinTableRS);
			sourceDefinTableRS.close();
		} catch (SQLException sqle) {
			System.out.println(" updateSourceDefRecord error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException("  updateSourceDefRecord error: " + sqle.getMessage());
		}
		pointManageSourceType.resetSorceDefinTable();		
	}	
/**
 * public void deleteSourceDefinition(long T737tablePID)	
 * @param T737tablePID
 * @throws HBException
 */
	public void deleteSourceDefinition(long T737tablePID) throws HBException {
		ResultSet sourceTableRS = null, sourceDefinTableRS = null;
		selectString = setSelectSQL("*", sourceTable, "SORC_DEF_RPID = " + T737tablePID);
		sourceTableRS = requestTableData(selectString, dataBaseIndex);
	// Start transaction
		updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
		try {
			if (!isResultSetEmpty(sourceTableRS)) {
				sourceTableRS.last();
				throw new HBException("#" +  sourceTableRS.getRow());
			}
	// Continue with delete		
			selectString = setSelectSQL("*", sourceDefnTable, "PID = " + T737tablePID);
			sourceDefinTableRS = requestTableData(selectString, dataBaseIndex);
			sourceDefinTableRS.first();
			sourceDefinTableRS.deleteRow();
			sourceDefinTableRS.close();
		// End transaction
			 updateTableData("COMMIT", dataBaseIndex);
			pointManageSourceType.resetSorceDefinTable();
		} catch (SQLException sqle) {
	   // Roll back transaction
			updateTableData("ROLLBACK", dataBaseIndex);
			System.out.println(" deleteSourceDefinition error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" deleteSourceDefinition error: " + sqle.getMessage());
		}
	}	

/**
 * public void createNewSourceElementType(String sourceElementName)
 * @param sourceElementName
 * @throws HBException
 * @throws
 * @throws
 */
	public void createNewSourceElementType(String sourceElementNa, String sourceElementLang,
						HG0564ManageSrcElmnt pointManageSourceElement) throws HBException  {
		this.sourceElementName = sourceElementNa;
		this.sourceElementLanguage = sourceElementLang;
		ResultSet sourceElementTableRS;
		long nextSourceElementTablePID;
		int lastSourceElementNr = elementIDmin, sourceElementNr;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
	// Start test
		this.sourceElementName = "[" + sourceElementName + "]";

	// Check if sourceElementName is in use
		selectString = setSelectSQL("*", sourceElmntTable,
					" SORC_ELMNT_LANG = '" + sourceElementLanguage
					+ "' AND SORC_ELMNT_NAME = " + "'" + sourceElementName + "'");
		sourceElementTableRS = requestTableData(selectString, dataBaseIndex);
		try {
			if (!isResultSetEmpty(sourceElementTableRS)) {
				//System.out.println(" Source element name in use: " + sourceElementName);
				throw new HBException("* Source element name in use!");
			}

	// Find next sourceElementIdent number
			selectString = setSelectSQL("*", sourceElmntTable, "");
			sourceElementTableRS = requestTableData(selectString, dataBaseIndex);
			sourceElementTableRS.beforeFirst();
			while (sourceElementTableRS.next()) {
				sourceElementNr = Integer.parseInt(sourceElementTableRS.getString("SORC_ELMNT_NUM"));
				if (lastSourceElementNr < sourceElementNr
						&& sourceElementNr > elementIDmin && sourceElementNr < elementIDmax) lastSourceElementNr = sourceElementNr;
			}
			sourceElementIdent = "" + (lastSourceElementNr + 1);
			//System.out.println(" Source element ident: " + sourceElementIdent);

		} catch (SQLException hbe) {
			hbe.printStackTrace();
			throw new HBException(" HBCitationSourceHandler - createNewSourceElementType: " + hbe.getMessage());
		}
		nextSourceElementTablePID = lastRowPID(sourceElmntTable, pointOpenProject) + 1;
		selectString = setSelectSQL("*", sourceElmntTable, " PID = " + (nextSourceElementTablePID - 1));
		sourceElementTableRS = requestTableData(selectString, dataBaseIndex);
		addToSourceElementT738_SORC_ELMNT(nextSourceElementTablePID, sourceElementTableRS);
	// reset source element table
		pointManageSourceElement.resetElementList();

	}

/**
 * public boolean testSourceElementTranslation (String elementID,
												String sourceElementLang)
 * @param elementID
 * @param sourceElementLang
 * @return
 * @throws HBException
 */
	public boolean testSourceElementTranslationExist (String elementID,
												String elementLang) throws HBException {
		ResultSet sourceElementTableRS = null;
		selectString = setSelectSQL("*", sourceElmntTable,
				" SORC_ELMNT_LANG = '" + elementLang
				+ "' AND SORC_ELMNT_NUM = " + "'" +  elementID + "'");

		sourceElementTableRS = requestTableData(selectString, dataBaseIndex);
		try {
			if (isResultSetEmpty(sourceElementTableRS)) return true;
		} catch (SQLException sql) {
			sql.printStackTrace();
			throw new HBException(" HBCitationSourceHandler - createNewSourceElementType: " + sql.getMessage());
		}
		return false;
	}

/**
 * public void updateSourceElementTranslation
 * @param elementName
 * @param elementID
 * @param sourceElementLang
 * @throws HBException
 */
	public void updateSourceElementTranslation (String elementName,
												String elementID,
												String sourceElementLang,
												HG0564ManageSrcElmnt pointManageSourceElement) throws HBException {
		this.sourceElementLanguage = sourceElementLang;
		this.sourceElementName = elementName;
		this.sourceElementIdent = elementID;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		ResultSet sourceElementTableRS = null;
		selectString = setSelectSQL("*", sourceElmntTable, "");
		sourceElementTableRS = requestTableData(selectString, dataBaseIndex);
		nextSourceElementTablePID = lastRowPID(sourceElmntTable, pointOpenProject) + 1;
		addToSourceElementT738_SORC_ELMNT(nextSourceElementTablePID,sourceElementTableRS);
	// reset source element table
		pointManageSourceElement.resetElementList();
	}

/**
 * public void updateSourceElementData(
 * @param elementName
 * @param elementID
 * @param sourceElementLang
 * @param pointManageSourceElement
 * @throws HBException
 */
	public void updateSourceElementData(String elementName,
										 String elementID,
										 String sourceElementLang,
										 HG0564ManageSrcElmnt pointManageSourceElement) throws HBException {

			this.sourceElementLanguage = sourceElementLang;
			this.sourceElementName = elementName;
			this.sourceElementIdent = elementID;
			dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
			ResultSet sourceElementTableRS = null;

			//System.out.println(" Update lang: " + sourceElementLanguage + " source element name/id: "
			//		+ sourceElementName + " / " + sourceElementIdent);

			selectString = setSelectSQL("*", sourceElmntTable,
					" SORC_ELMNT_LANG = '" + sourceElementLanguage
					+ "' AND SORC_ELMNT_NUM = " + "'" +  sourceElementIdent + "'");

			sourceElementTableRS = requestTableData(selectString, dataBaseIndex);
			try {
				if (isResultSetEmpty(sourceElementTableRS)) System.out.println(" No data in ResultSet!");
				else {
					sourceElementTableRS.first();
					updateSourceElementT738_SORC_ELMNT(sourceElementTableRS);
				}
			} catch (SQLException | HBException hbe) {
				hbe.printStackTrace();
				throw new HBException(" updateSourceElementData error: " + hbe.getMessage());
			}
		// reset source element table
			pointManageSourceElement.resetElementList();
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
			throw new HBException(" getCitationSourceData() error: " + sqle.getMessage());
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
	public Object[][] getSourceList(boolean showAllSorces, boolean copyList) throws HBException {
		// Load all T736 SORC records
		ResultSet eventCitationRS, eventSourcRS;
		String sourceAbbrev = "";
		int citedNumber = 0, index = 0;
		long sourcePID = null_RPID;
		long sourceDefnPID = null_RPID;
		String sourceFID = "E";
		String sourceFullFoot = "", sourceShortFoot = "", sourceBiblio ="";

	// Load pre populated list if done before
		if (tableSourceData != null && copyList) return tableSourceData;

	// Continue with loading source list
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		pointHREmemo = new HREmemo(pointDBlayer, dataBaseIndex);

		if (showAllSorces)
			selectString = setSelectSQL("*", sourceTable, "");
		else
		selectString = setSelectSQL("*", sourceTable, "IS_ACTIVE = FALSE");

		eventSourcRS = requestTableData(selectString, dataBaseIndex);
		try {
			if (isResultSetEmpty(eventSourcRS)) {
				tableSourceData = new Object[1][9];
				//tableSourceData[0][0] = eventSourcRS.getInt("SORC_REF");
				tableSourceData[0][0] = "";
				tableSourceData[0][1] = sourceAbbrev;
				//tableSourceData[0][2] = citedNumber;
				tableSourceData[0][2] = "";
				tableSourceData[0][3] = sourcePID;
				tableSourceData[0][4] = sourceFID; // Source Fidelity
				tableSourceData[0][5] = sourceDefnPID;
				tableSourceData[0][6] = sourceFullFoot;
				tableSourceData[0][7] = sourceShortFoot;
				tableSourceData[0][8] = sourceBiblio;
				return tableSourceData;
			}
		// Continue
			eventSourcRS.last();
			tableSourceData = new Object[eventSourcRS.getRow()][9];
			eventSourcRS.beforeFirst();
			while (eventSourcRS.next()) {
				sourceAbbrev = eventSourcRS.getString("SORC_ABBREV").trim();
				sourcePID = eventSourcRS.getLong("PID");
				selectString = setSelectSQL("*", citationTable, "SORC_RPID = " + sourcePID);
				eventCitationRS = requestTableData(selectString, dataBaseIndex);
				eventCitationRS.last();
				citedNumber = eventCitationRS.getRow();
				sourceFID = eventSourcRS.getString("SORC_FIDELITY");
				sourceDefnPID = eventSourcRS.getLong("SORC_DEF_RPID");
				sourceFullFoot = eventSourcRS.getString("SORC_FULLFORM").trim();
				sourceShortFoot = eventSourcRS.getString("SORC_SHORTFORM").trim();
				sourceBiblio = eventSourcRS.getString("SORC_BIBLIOFORM").trim();
				tableSourceData[index][0] = eventSourcRS.getInt("SORC_REF");
				tableSourceData[index][1] = sourceAbbrev;
				tableSourceData[index][2] = citedNumber;
				tableSourceData[index][3] = sourcePID;
				tableSourceData[index][4] = sourceFID; // Source Fidelity
				tableSourceData[index][5] = sourceDefnPID;
				tableSourceData[index][6] = sourceFullFoot;
				tableSourceData[index][7] = sourceShortFoot;
				tableSourceData[index][8] = sourceBiblio;
				index++;
			}
		} catch (SQLException sqle) {
			System.out.println(" getSourceList() error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" getSourceList() error: " + sqle.getMessage());
		}
		return tableSourceData;
	}

/**
 * Object[] getSourceTemplates() throws HBException
 * @return
 * @throws HBException
 * @throws SQLException
 */
	public Object[] getSourceTemplates(long sourcePID) throws HBException {
		// Get the 3 T736 SORC footnote/biblio templates and SORC_DEFN PID for the requested Source
		ResultSet sourceTemplateRS;
		Object[] sourceTemplates = new Object[4];
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", sourceTable, "PID = " + sourcePID);
		sourceTemplateRS = requestTableData(selectString, dataBaseIndex);
		try {
			if (isResultSetEmpty(sourceTemplateRS)) return sourceDefnTemplates;
			sourceTemplateRS.first();
			sourceDefnTemplates[0] = sourceTemplateRS.getString("SORC_FULLFORM").trim();
			sourceDefnTemplates[1] = sourceTemplateRS.getString("SORC_SHORTFORM").trim();
			sourceDefnTemplates[2] = sourceTemplateRS.getString("SORC_BIBLIOFORM").trim();
			sourceDefnTemplates[3] = sourceTemplateRS.getString("SORC_DEF_RPID");
		} catch (SQLException sqle) {
			System.out.println(" getSourceTemplates error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" getSourceTemplates error: " + sqle.getMessage());
		}
		return sourceTemplates;
	}

/**
 * public Object[] getSourceEditData(long sourcePID)
 * @param sourcePID
 * @return
 * @throws HBException
 */
	public Object[] getSourceEditData(long sourcePID) throws HBException {
		ResultSet sourceRS;
		String sourceFidelity, sourceAbbrev, sourceTitle,
							sourceFullFoot, sourceShortFoot, sourceBiblio, sourceRemind, sourceText;
		int sourceRef, sourceType;
		long sourceDefnPID, sourceTextPID,
		//sourceAuthorPID, sourceEditorPID, sourceCompilerPID,
		sourceRemindPID;
		boolean sourceActiv;
		Object[] sourceEditData = new Object[15];
		//pointHREmemo = new HREmemo(pointDBlayer, dataBaseIndex);
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		pointHREmemo = new HREmemo(pointDBlayer, dataBaseIndex);
		selectString = setSelectSQL("*", sourceTable, " PID = " + sourcePID);
		sourceRS = requestTableData(selectString, dataBaseIndex);
		try {
			sourceRS.first();
			sourceActiv = sourceRS.getBoolean("IS_ACTIVE");
			sourceDefnPID = sourceRS.getLong("SORC_DEF_RPID");
			sourceRef = sourceRS.getInt("SORC_REF");
			sourceType = sourceRS.getInt("SORC_TYPE");
			sourceFidelity = sourceRS.getString("SORC_FIDELITY");
			sourceTextPID = sourceRS.getLong("SORC_TEXT_RPID");
			if (sourceTextPID == null_RPID) sourceText = "";
			else sourceText = pointHREmemo.readMemo(sourceTextPID);
			sourceAuthorPID = sourceRS.getLong("SORC_AUTHOR_RPID");
			sourceEditorPID = sourceRS.getLong("SORC_EDITOR_RPID");
			sourceCompilerPID = sourceRS.getLong("SORC_COMPILER_RPID");
			sourceAbbrev = sourceRS.getString("SORC_ABBREV").trim();
			sourceTitle = sourceRS.getString("SORC_TITLE").trim();
			sourceFullFoot = sourceRS.getString("SORC_FULLFORM").trim();
			sourceShortFoot = sourceRS.getString("SORC_SHORTFORM").trim();
			sourceBiblio = sourceRS.getString("SORC_BIBLIOFORM").trim();
			sourceRemindPID = sourceRS.getLong("SORC_REMIND_RPID");
			if (sourceRemindPID == null_RPID) sourceRemind = "";
			else sourceRemind = pointHREmemo.readMemo(sourceRemindPID);
			sourceDefnPID = sourceRS.getLong("SORC_DEF_RPID");

			sourceEditData[0] = sourceTitle;
			sourceEditData[1] = sourceAbbrev;
			sourceEditData[2] = sourceRef;
			sourceEditData[3] = sourceType;
			sourceEditData[4] = sourceActiv;
			sourceEditData[5] = sourceFidelity;
			sourceEditData[6] = sourceFullFoot;
			sourceEditData[7] = sourceShortFoot;
			sourceEditData[8] = sourceBiblio;
			sourceEditData[9] = sourceText;
			sourceEditData[10] = sourceRemind;
			sourceEditData[11] = sourceDefnPID;
			sourceEditData[12] = sourceAuthorPID;
			sourceEditData[13] = sourceEditorPID;
			sourceEditData[14] = sourceCompilerPID;
		} catch (SQLException sqle) {
			System.out.println(" getSourceData() error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" getSourceData() error: " + sqle.getMessage());
		}
		return sourceEditData;
	}

/**
 * getSourceElmntList() throws HBException
 * @return
 * @throws HBException
 */
	public String[][] getSourceElmntList(String language) throws HBException {
		// Load all T738 SORC_ELMNT records for requested language
		int index = 0;
		ResultSet sourceElmntRS;

	// Load source element list for requested language
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", sourceElmntTable, "SORC_ELMNT_LANG =" + "'" + language + "'");
		sourceElmntRS = requestTableData(selectString, dataBaseIndex);
		try {
			sourceElmntRS.last();
			tableSourceElmntData = new String[sourceElmntRS.getRow()][2];
			sourceElmntRS.beforeFirst();
			while (sourceElmntRS.next()) {
				tableSourceElmntData[index][0] = sourceElmntRS.getString("SORC_ELMNT_NAME").trim();
				tableSourceElmntData[index][1] = sourceElmntRS.getString("SORC_ELMNT_NUM").trim();
				index++;
			}
		} catch (SQLException sqle) {
			System.out.println(" getSourceElmntList() error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" getSourceElmntList() error: " + sqle.getMessage());
		}
		return tableSourceElmntData;
	}

/**
 * getSourceElmntDataValues() throws HBException
 * @return
 * @throws HBException
 */
	public String[][] getSourceElmntDataValues(long sourceOwner) throws HBException {
		// Load all T734 SORC_DATA records for a particular Source
		int index = 0;
		ResultSet elmntValuRS;

	// Load source data table for requested source
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", sourceDataTable, "SORC_OWNER_RPID=" + sourceOwner);
		elmntValuRS = requestTableData(selectString, dataBaseIndex);
		try {
			elmntValuRS.last();
			tableSourceElmntDataValues= new String[elmntValuRS.getRow()][2];
			elmntValuRS.beforeFirst();
			while (elmntValuRS.next()) {
				tableSourceElmntDataValues[index][0] = elmntValuRS.getString("SORC_ELMNT_NUM");
				tableSourceElmntDataValues[index][1] = elmntValuRS.getString("SORC_ELMNT_DATA");
				index++;
			}
		} catch (SQLException sqle) {
			System.out.println(" getSourceElmntValue() error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" getSourceElmntValue() error: " + sqle.getMessage());
		}
		return tableSourceElmntDataValues;
	}

/**
 * String[][] getSourceDefnList()
 * @param language
 * @return
 * @throws HBException
 */
	public Object[][] getSourceDefnList(String language) throws HBException {
	// Load all T737 SORC_DEFN records for this language
		int index = 0;
		ResultSet sourceDefnRS;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		pointHREmemo = new HREmemo(pointDBlayer, dataBaseIndex);
		selectString = setSelectSQL("*", sourceDefnTable, "SORC_DEFN_LANG = '" + language + "'");
		sourceDefnRS = requestTableData(selectString, dataBaseIndex);
		try {
		// Check RS - if empty, repeat the load for en-US as default alnguage
			if (isResultSetEmpty(sourceDefnRS)) {
				language = "en-US";
				selectString = setSelectSQL("*", sourceDefnTable, "SORC_DEFN_LANG = '" + language + "'");
				sourceDefnRS = requestTableData(selectString, dataBaseIndex);
			}
		// If still empty, create default source def and continue
			if (isResultSetEmpty(sourceDefnRS)) {
				sourceDefFullFoot = "[CD]"; sourceDefShortFoot = "[CD]"; sourceDefBiblio = "[CD]";
				addToSourDefT737_SORC_DEFN(proOffset + 1000, sourceDefnRS);	// use high PID# for safety!
				selectString = setSelectSQL("*", sourceDefnTable, "");
				sourceDefnRS = requestTableData(selectString, dataBaseIndex);
			}
			sourceDefnRS.last();
			tableSourceDefnData = new Object[sourceDefnRS.getRow()][2];
			sourceDefnRS.beforeFirst();
			while (sourceDefnRS.next()) {
				tableSourceDefnData[index][0] = sourceDefnRS.getString("SORC_DEFN_NAME").trim();
				tableSourceDefnData[index][1] = sourceDefnRS.getLong("PID");
				index++;
			}
		} catch (SQLException sqle) {
			System.out.println(" getSourceDefnList() error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" getSourceDefnList() error: " + sqle.getMessage());
		}
		return tableSourceDefnData;
	}

/**
 * String[] getSourceDefnTemplates() throws HBException
 * @return
 * @throws HBException
 * @throws SQLException
 */
	public String[] getSourceDefnTemplates(long defnPID) throws HBException {
		// Get the 3 T737 SORC_DEFN footnote/biblio templates for the requested Source Defn
		ResultSet sourceDefnRS;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", sourceDefnTable, "PID = " + defnPID);
		sourceDefnRS = requestTableData(selectString, dataBaseIndex);
		try {
			if (isResultSetEmpty(sourceDefnRS)) return sourceDefnTemplates;
			sourceDefnRS.first();
			sourceDefnTemplates[0] = sourceDefnRS.getString("SORC_DEFN_FULLFOOT");
			sourceDefnTemplates[1] = sourceDefnRS.getString("SORC_DEFN_SHORTFOOT");
			sourceDefnTemplates[2] = sourceDefnRS.getString("SORC_DEFN_BIBLIO");
		} catch (SQLException sqle) {
			System.out.println(" getSourceDefnTemplate error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" getSourceDefnTemplate error: " + sqle.getMessage());
		}
		return sourceDefnTemplates;
	}

/**
 * public Object[] getSourceDefnEditData(long sourceDefnPID)
 * @param sourceDefnPID
 * @return
 * @throws HBException
 */
	public Object[] getSourceDefnEditData(long sourceDefnPID) throws HBException {
		ResultSet sourceDefnRS;
		String sourceDefnName, sourceDefnLang, sourceDefnFullFoot, sourceDefnShortFoot, sourceDefnBiblio, sourceDefnRemind;
		int sourceDefnType;
		long sourceDefnRemindPID;
		Object[] sourceDefnEditData = new Object[7];
		pointHREmemo = new HREmemo(pointDBlayer, dataBaseIndex);
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", sourceDefnTable, " PID = " + sourceDefnPID);
		sourceDefnRS = requestTableData(selectString, dataBaseIndex);
		try {
			sourceDefnRS.first();
			sourceDefnName = sourceDefnRS.getString("SORC_DEFN_NAME").trim();
			sourceDefnType = sourceDefnRS.getInt("SORC_DEFN_TYPE");
			sourceDefnLang = sourceDefnRS.getString("SORC_DEFN_LANG").trim();
			sourceDefnFullFoot = sourceDefnRS.getString("SORC_DEFN_FULLFOOT").trim();
			sourceDefnShortFoot = sourceDefnRS.getString("SORC_DEFN_SHORTFOOT").trim();
			sourceDefnBiblio = sourceDefnRS.getString("SORC_DEFN_BIBLIO").trim();
			sourceDefnRemindPID = sourceDefnRS.getLong("SORC_DEFN_REMIND_RPID");
			if (sourceDefnRemindPID == null_RPID) sourceDefnRemind = "";
			else sourceDefnRemind = pointHREmemo.readMemo(sourceDefnRemindPID);

			sourceDefnEditData[0] = sourceDefnName;
			sourceDefnEditData[1] = sourceDefnType;
			sourceDefnEditData[2] = sourceDefnLang;
			sourceDefnEditData[3] = sourceDefnFullFoot;
			sourceDefnEditData[4] = sourceDefnShortFoot;
			sourceDefnEditData[5] = sourceDefnBiblio;
			sourceDefnEditData[6] = sourceDefnRemind;

		} catch (SQLException sqle) {
			System.out.println(" getSourceDefnData() error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" getSourceDefnData() error:  " + sqle.getMessage());
		}
		return sourceDefnEditData;
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

		Object[] citationEditData = new Object[9];
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
			citationEditData[8] = sourcePID;

		} catch (SQLException sqle) {
			System.out.println(" getCitationEditData() error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" getCitationEditData() error:  " + sqle.getMessage());
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
			throw new HBException(" saveCitedData error: " + sqle.getMessage());
		}
	}

/**
 *  T734_SORC_DATA
 PID
 CL_COMMIT_RPID
 SORC_OWNER_RPID
 SORC_ELMNT_NUM
 SORC_ELMNT_DATA
 * @throws HBException
 */
	private void addToSourceElementDataT734_SORC_DATA(long T738tablePID, ResultSet hreTable) throws HBException {
		try {
		// Move cursor to the insert row
			hreTable.moveToInsertRow();

		// Update new row in H2 database
			hreTable.updateLong("PID", T738tablePID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateLong("SORC_OWNER_RPID", sourceTablePID);
			hreTable.updateString("SORC_ELMNT_NUM", sourceElementNumber);
			hreTable.updateString("SORC_ELMNT_DATA", sourceElementData);

		//Insert row
			hreTable.insertRow();

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" addToSourceElementDataT734_SORC_DATA: " + sqle.getMessage());
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
 * @throws HBException
 */
	private void addToCitationT735_CITN(long T735tablePID, ResultSet hreTable) throws HBException {
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
			throw new HBException(" addToCitationT735_CITN " + sqle.getMessage());
		}
	}

/**
 * public void updateElementDataChangeList(String elementName, String elementNumber, String elementData)
 * Update the elementNameDataChanges HashMap with edited element data.
 * If key (elementNumber exist in HashMap the olde entry is overwritten
 * @param selectedIndex
 * @param nameData
 */
	public void updateElementDataChangeList(String elementName, String elementNumber, String elementData) {
		if (HGlobal.DEBUG)
			System.out.println(" addToElementDataChangeList(): " +  elementName + "/" + elementNumber + "/" + elementData);
		elementNameDataChanges.put(elementNumber, elementData);
	}

/**
 * public void createSourceElementDataRecords(long sourcePID)
 * @param sourceTablePID
 * @throws HBException
 */
	public void createSourceElementDataRecords(boolean add) throws HBException {
		ResultSet sourceElementDataRS;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		nextSourceElementDataPID = lastRowPID(sourceDataTable, pointOpenProject) + 1;
		selectString = setSelectSQL("*", sourceDataTable, " PID = " + (nextSourceElementDataPID - 1));
		if (add) sourceTablePID = nextSourceTablePID; // Why ?
		sourceElementDataRS = requestTableData(selectString, dataBaseIndex);
	// Loop HashMap
		for (Map.Entry<String, String> entry : elementNameDataChanges.entrySet()) {
			sourceElementNumber = entry.getKey();
			sourceElementData = entry.getValue();
			addToSourceElementDataT734_SORC_DATA(nextSourceElementDataPID, sourceElementDataRS);
			nextSourceElementDataPID++;
            //System.out.println("Element Data: " + sourceElementNumber + ", Data: " + sourceElementData);
		}
	}

/**
 * public void updateSourceElementDataRecords(long sourceTablePID)
 * @param sourceTablePID
 * @throws HBException
 */
	public void updateSourceElementDataRecords(long sourceTablePID) throws HBException {
		ResultSet sourceElementDataRS;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		//pointHREmemo = new HREmemo(pointDBlayer, dataBaseIndex);
		selectString = setSelectSQL("*", sourceDataTable, " SORC_OWNER_RPID = " + sourceTablePID);
		sourceElementDataRS = requestTableData(selectString, dataBaseIndex);
		try {
			sourceElementDataRS.beforeFirst();
			while (sourceElementDataRS.next()) {
				sourceElementNumber = sourceElementDataRS.getString("SORC_ELMNT_NUM");
				if (elementNameDataChanges.containsKey(sourceElementNumber)) {
					sourceElementData = elementNameDataChanges.get(sourceElementNumber);
					elementNameDataChanges.remove(sourceElementNumber);
					sourceElementDataRS.updateString("SORC_ELMNT_DATA", sourceElementData);
					sourceElementDataRS.updateRow();
				}
			}
		// If new elementa are edite - create new elements
			this.sourceTablePID = sourceTablePID;
			if (elementNameDataChanges.size() > 0) createSourceElementDataRecords(false);

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" updateSourceElementDataRecords error: " + sqle.getMessage());
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
 * @throws HBException
 */
	private void addToSourceT736_SORC(long T736tablePID, ResultSet hreTable) throws HBException {
		int sourceRefNr = 0;
		try {
			hreTable.beforeFirst();
			while (hreTable.next()) {
				if (hreTable.getInt("SORC_REF") > sourceRefNr) {
					sourceRefNr = hreTable.getInt("SORC_REF");
				}
			}

		// Move cursor to the insert row
				hreTable.moveToInsertRow();
		// Update new row in H2 database
				hreTable.updateLong("PID", T736tablePID);
				hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
				hreTable.updateBoolean("IS_ACTIVE", sourceActive);
				hreTable.updateLong("SORC_DEF_RPID", sourceDefnPID); // Added for new seed
				hreTable.updateInt("SORC_REF", sourceRefNr + 1);
				hreTable.updateInt("SORC_TYPE",1);
				hreTable.updateString("SORC_FIDELITY", sourceFidelity);
			// Create source reference text
				hreTable.updateLong("SORC_TEXT_RPID", pointHREmemo.addMemoRecord(sourceReference));
				hreTable.updateLong("SORC_AUTHOR_RPID", sourceAuthorPID);
				hreTable.updateLong("SORC_EDITOR_RPID", sourceEditorPID);
				hreTable.updateLong("SORC_COMPILER_RPID", sourceCompilerPID);
				hreTable.updateString("SORC_ABBREV", sourceAbbrev);
				hreTable.updateString("SORC_TITLE", sourceTitle);
				hreTable.updateString("SORC_FULLFORM", sourceFullFoot);
				hreTable.updateString("SORC_SHORTFORM", sourceShortFoot);
				hreTable.updateString("SORC_BIBLIOFORM", sourceBiblio);
			// Create reminder memo text
				hreTable.updateLong("SORC_REMIND_RPID", pointHREmemo.addMemoRecord(sourceReminder));
			//Insert row
				hreTable.insertRow();

		} catch (SQLException sqle) {
			System.out.println(" addToSourceT736_SORC error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" addToSourceT736_SORC error: " + sqle.getMessage());
		}
	}
/**
 * private void saveSourceDataT736_SORC(long T736tablePID, ResultSet hreTable)
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
 * @throws HBException
 */
	private void saveSourceDataT736_SORC(ResultSet hreTable) throws HBException {
		try {
			hreTable.first();
		// Update row in H2 database
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateBoolean("IS_ACTIVE", sourceActive);
			hreTable.updateLong("SORC_DEF_RPID", sourceDefnPID); // Added for new seed
			hreTable.updateInt("SORC_TYPE",1);
			hreTable.updateString("SORC_FIDELITY",sourceFidelity);
		// Update source reference text
			referenceTablePID = hreTable.getLong("SORC_TEXT_RPID");
			if (referenceTablePID == null_RPID)
				hreTable.updateLong("SORC_TEXT_RPID", pointHREmemo.addMemoRecord(sourceReference));
			else
				pointHREmemo.findT167_MEMOrecord(sourceReference, referenceTablePID);
			hreTable.updateLong("SORC_AUTHOR_RPID", sourceAuthorPID);
			hreTable.updateLong("SORC_EDITOR_RPID", sourceEditorPID);
			hreTable.updateLong("SORC_COMPILER_RPID", sourceCompilerPID);
			hreTable.updateString("SORC_ABBREV", sourceAbbrev);
			hreTable.updateString("SORC_TITLE", sourceTitle);
			hreTable.updateString("SORC_FULLFORM", sourceFullFoot);
			hreTable.updateString("SORC_SHORTFORM", sourceShortFoot);
			hreTable.updateString("SORC_BIBLIOFORM", sourceBiblio);
		// Update or create reminder memo
			reminderTablePID = hreTable.getLong("SORC_REMIND_RPID");
			if (reminderTablePID == null_RPID)
				hreTable.updateLong("SORC_REMIND_RPID", pointHREmemo.addMemoRecord(sourceReminder));
			else
				pointHREmemo.findT167_MEMOrecord(sourceReminder, reminderTablePID);
		// Update row in H2 database
			hreTable.updateRow();

		} catch (SQLException sqle) {
			System.out.println(" saveSourceDataT736_SORC error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" saveSourceDataT736_SORC: " + sqle.getMessage());
		}
	}

/**
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
 * @throws HBException
 */
	private void addToSourDefT737_SORC_DEFN(long T737tablePID, ResultSet hreTable) throws HBException {

		try {
			
			hreTable.beforeFirst();
			while (hreTable.next()) {
				if (hreTable.getInt("SORC_DEFN_TYPE") > sourceDefType) {
					sourceDefType = hreTable.getInt("SORC_DEFN_TYPE");
				}
			}
			sourceDefType++; // next not used number
		// Move cursor to the insert row
			hreTable.moveToInsertRow();

		// Update new row in H2 database
			hreTable.updateLong("PID", T737tablePID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateInt("SORC_DEFN_TYPE", sourceDefType);
			hreTable.updateString("SORC_DEFN_NAME", sourceDefinName);
			hreTable.updateString("SORC_DEFN_LANG", sourceDefLanguage);
			hreTable.updateString("SORC_DEFN_FULLFOOT", sourceDefFullFoot);
			hreTable.updateString("SORC_DEFN_SHORTFOOT", sourceDefShortFoot);
			hreTable.updateString("SORC_DEFN_BIBLIO", sourceDefBiblio);
		// Update source memo text
			hreTable.updateLong("SORC_DEFN_REMIND_RPID", pointHREmemo.addMemoRecord(sourceDefReminder));
		//Insert row
			hreTable.insertRow();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" HBCitationSourceHandler - addToSourDefT737_SORC_DEFN: " 
									+ sqle.getMessage());
		}
	}
/**
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
 * @throws HBException
 */
	private void updateSourDefT737_SORC_DEFN(ResultSet hreTable) throws HBException {

		try {
		// Update new row in H2 database
			//hreTable.updateLong("PID", T737tablePID);
			//hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			//hreTable.updateInt("SORC_DEFN_TYPE", sourceDefType);
			hreTable.updateString("SORC_DEFN_NAME", sourceDefinName);
			//hreTable.updateString("SORC_DEFN_LANG", sourceDefLanguage);
			hreTable.updateString("SORC_DEFN_FULLFOOT", sourceDefFullFoot);
			hreTable.updateString("SORC_DEFN_SHORTFOOT", sourceDefShortFoot);
			hreTable.updateString("SORC_DEFN_BIBLIO", sourceDefBiblio);
		// update reminder memo text
			long memoTablePID = hreTable.getLong("SORC_DEFN_REMIND_RPID");
			if (memoTablePID == null_RPID)
				if (sourceDefReminder.length() > 0) 
					hreTable.updateLong("SORC_DEFN_REMIND_RPID", pointHREmemo.addMemoRecord(sourceDefReminder)); 
				else hreTable.updateLong("SORC_DEFN_REMIND_RPID", null_RPID);
			else
				pointHREmemo.findT167_MEMOrecord(sourceDefReminder, memoTablePID);
		//Insert row
			hreTable.updateRow();

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" HBCitationSourceHandler - updateSourDefT737_SORC_DEFN: " 
									+ sqle.getMessage());
		}
	}
	

	/**
	 * 	public void addToSourceElementT738_SORC_ELMNT(long T738tablePID, ResultSet hreTable)
	 *  T738_SORC_ELMNT
		 PID
		 CL_COMMIT_RPID
		 SORC_ELMNT_NUM
		 SORC_ELMNT_LANG
		 SORC_ELMNT_NAME
	 * @throws SQLException
	 */
		public void addToSourceElementT738_SORC_ELMNT(long T738tablePID, ResultSet hreTable) throws HBException {
			try {
			// Move cursor to the insert row
				hreTable.moveToInsertRow();
			//Update
				hreTable.updateLong("PID", T738tablePID);
				hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
				hreTable.updateString("SORC_ELMNT_NUM", sourceElementIdent);
				hreTable.updateString("SORC_ELMNT_LANG", sourceElementLanguage);
				hreTable.updateString("SORC_ELMNT_NAME", sourceElementName);
			//Insert row
				hreTable.insertRow();
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw new HBException(" addToSourceElementT738_SORC_ELMNT " + sqle.getMessage());
			}
		}

/**
 * 	public void updateSourceElementT738_SORC_ELMNT(long T738tablePID, ResultSet hreTable)
 *  T738_SORC_ELMNT
	 PID
	 CL_COMMIT_RPID
	 SORC_ELMNT_NUM
	 SORC_ELMNT_LANG
	 SORC_ELMNT_NAME
 * @throws SQLException
 */
	public void updateSourceElementT738_SORC_ELMNT(ResultSet hreTable) throws HBException {
		try {
		//Update
			//hreTable.updateLong("PID", T738tablePID);
			//hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateString("SORC_ELMNT_NUM", sourceElementIdent);
			hreTable.updateString("SORC_ELMNT_LANG", sourceElementLanguage);
			hreTable.updateString("SORC_ELMNT_NAME", sourceElementName);
		//Insert row
			hreTable.updateRow();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" updateSourceElementT738_SORC_ELMNT " + sqle.getMessage());
		}
	}

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
			throw new HBException(" updateCiteGUIseq - " + sqle.getMessage());
		}
	}

}
