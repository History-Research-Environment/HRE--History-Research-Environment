package hre.tmgjava;
/***************************************************************************************
 * Uses library com.linuxense.javadbf
 * Java library for reading and writing Xbase (dBase/DBF) files
 * https://github.com/albfernandez/javadbf
 * albfernandez/javadbf is licensed under the
 * GNU Lesser General Public License v3.0
 * Written by Alberto Fern�ndez
 * **************************************************************************************
 * TMGloader loads the TMG tables and create TMGtabledata object for each table
 * v0.00.0018 2020-03-05 - First version (N. Tolleshaug)
 * v0.00.0021 2020-04-08 - Open TMG folder and choose .pcj file
 * 			  2020-05-01 - Included monitoring of TMG table load
 * v0.02.0022 2020-06-30 - Substitute \\ with / (N. Tolleshaug)
 * v0.02.0025 2020-11-15 - Removed DDL19a (N. Tolleshaug)
 * 			  2020-11-22 - File separator updated for file path strings (N. Tolleshaug)
 * v0.03.0027 2022-02-27 - Implemented handling event tags, witness and roles(N. Tolleshaug)
 * 			  2022-05-17 - Implemented progress monitor for load TMG tables(N. Tolleshaug)
 * 			  2022-07-24 - For Exhibit processing removed TMGtableSingleData(tmgReader)
 * v0.03.0031 2023-10-20 - Updated for v22b database
 * 			  2024-11-23 - Changed to multi table processing for E.dbf (N. Tolleshaug)
 *			  2024-11-25 - Convert "\\" usage to File.separator (D Ferguson)
 * v0.04.0032 2025-07-21 - Added tables for import of source A,M,S,R,U,W tables (N. Tolleshaug)
 *			  2026-01-14 - Log catch block msgs (D Ferguson)
 * v0.05.0032 2026-02-24 - Added HB0711Logging.logWrite("Action: in TMGloader loading XXXX tables");
 *****************************************************************************************/

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Vector;

import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFRow;

import hre.bila.HB0711Logging;
import hre.gui.HGlobal;

/**
 * class TMGloader
 * @author NTo
 * @since 2020-03-05
 */

public class TMGloader {

	String tmgBaseFolder;
	String tmgFileName;
	TMGHREconverter pointConvert;
	DBFReader tmgReader;
/**
 * TMGloader
 * @param tmgBaseFolder
 * @throws HCException
 */
	public TMGloader(TMGHREconverter pointConvert, String tmgBaseFolder, String tmgFileName) throws HCException  {
		this.tmgBaseFolder = tmgBaseFolder;
		this.tmgFileName = tmgFileName;
		this.pointConvert = pointConvert;

		// DATASET TABLE
		tmgReader = tableLoader(tmgFileName + "_" + TMGtypes.DATA_SET + ".dbf");
		setMemoFile(tmgReader,tmgFileName + "_" + TMGtypes.DATA_SET + ".fpt");
		TMGglobal.tmg_D_table = new TMGtableData(tmgReader,"DSID", pointConvert);
		TMGglobal.tmg_D_table.TMGtableSingleData(tmgReader);
		statusUpdate(TMGtypes.DATA_SET, TMGglobal.tmg_D_table);
	}

/**
 * reportProgress(int completed, int nrOfTables )
 * @param completed
 * @param nrOfTables
 */
	private void reportProgress(int completed, int nrOfTables ) {
		// Report progress in %
		int progress = (int)Math.round(((double)completed / (double)nrOfTables) * 100);
		pointConvert.setStatusProgress(progress);
	}

/**
 * resetProgress()
 */
	private void resetProgress() {
		reportProgress(0,1);
	}

/**
 * loadFileTMG(String fileType)
 * @param fileType
 */
	private void loadFileTMG(String fileType) {
		if (TMGglobal.TMGTRACE) {
			String statusText = " Loading TMG: "
					+ tmgFileName + "_" + fileType + ".dbf";
			pointConvert.processMonitor.setContextOfAction(statusText);
		}
}

/**
 * statusUpdate(String fileType, TMGtableData tablePointer)
 * @param fileName
 * @param tablePointer
 */
	private void statusUpdate(String fileType, TMGtableData tablePointer) {

		String statusText = " - file data: " + fileType + ".dbf";
		if (tablePointer.getNrOfRows() > 0 ) statusText = statusText
				+ " rows: " + tablePointer.getNrOfRows();
		if (tablePointer.getMultiRowNr() > 0 ) statusText = statusText
				+ " - mrows:  " + tablePointer.getMultiRowNr();
		if (TMGglobal.DEBUG) System.out.println(statusText);

	// Update monitor
		pointConvert.processMonitor.setContextOfAction(statusText);
		pointConvert.timeElapsed = System.currentTimeMillis() - pointConvert.startTime;
		pointConvert.processMonitor.setTransferTime((double)pointConvert.timeElapsed / pointConvert.oneMilliSec);
	}

/**
 * loadTmgSupportTables()
 * @throws HCException
 */
	public void loadTmgSupportTables() throws HCException {
		if (HGlobal.writeLogs)
			HB0711Logging.logWrite("Action: in TMGloader loading Support tables");
	//STYLE TYPE TABLE
		loadFileTMG(TMGtypes.STYLE);
		tmgReader = tableLoader(tmgFileName + "_" + TMGtypes.STYLE + ".dbf");
		setMemoFile(tmgReader,tmgFileName + "_" + TMGtypes.STYLE + ".fpt");
		TMGglobal.tmg_ST_table = new TMGtableData(tmgReader,"STYLEID", pointConvert);
		TMGglobal.tmg_ST_table.TMGtableSingleData(tmgReader);
		if (TMGglobal.DUMP) statusUpdate(TMGtypes.STYLE , TMGglobal.tmg_ST_table);
		resetProgress();

	//STYLE TYPE TABLE
		loadFileTMG(TMGtypes.FLAG);
		tmgReader = tableLoader(tmgFileName + "_" + TMGtypes.FLAG + ".dbf");
		setMemoFile(tmgReader,tmgFileName + "_" + TMGtypes.FLAG + ".fpt");
		TMGglobal.tmg_C_table = new TMGtableData(tmgReader,"FLAGID", pointConvert);
		TMGglobal.tmg_C_table.TMGtableSingleData(tmgReader);
		if (TMGglobal.DUMP) statusUpdate(TMGtypes.STYLE , TMGglobal.tmg_C_table);
		resetProgress();
	}

/**
 * loadTmgNameTables()
 * @throws HCException
 */
	public void loadTmgNameTables() throws HCException {
		if (HGlobal.writeLogs)
			HB0711Logging.logWrite("Action: in TMGloader loading Name tables");

	//DBFReader tmgReader;
		resetProgress();

	// PERSON TABLE
		loadFileTMG(TMGtypes.PERSON);
		tmgReader = tableLoader(tmgFileName + "_" + TMGtypes.PERSON + ".dbf");
		TMGglobal.tmg_$_table = new TMGtableData(tmgReader,"PER_NO", pointConvert);
		TMGglobal.tmg_$_table.TMGtableSingleData(tmgReader);
		if (TMGglobal.DUMP) statusUpdate(TMGtypes.PERSON, TMGglobal.tmg_$_table);
		resetProgress();

	//NAME TABLE
		loadFileTMG(TMGtypes.NAME);
		tmgReader = tableLoader(tmgFileName + "_" + TMGtypes.NAME + ".dbf");
		setMemoFile(tmgReader,tmgFileName + "_" + TMGtypes.NAME + ".fpt");
		TMGglobal.tmg_N_table = new TMGtableData(tmgReader,"NPER", pointConvert);
		TMGglobal.tmg_N_table.TMGtableMultiData(tmgReader);
		if (TMGglobal.DUMP) statusUpdate(TMGtypes.NAME, TMGglobal.tmg_N_table);
		resetProgress();

	// PARENT CHILD RELATION TABLE
		loadFileTMG(TMGtypes.PARENT_CHILD_RELATIONSHIP);
		tmgReader = tableLoader(tmgFileName + "_" + TMGtypes.PARENT_CHILD_RELATIONSHIP + ".dbf");
		TMGglobal.tmg_F_table = new TMGtableData(tmgReader,"RECNO", pointConvert);
		TMGglobal.tmg_F_table.TMGtableSingleData(tmgReader);
		if (TMGglobal.DUMP) statusUpdate(TMGtypes.PARENT_CHILD_RELATIONSHIP, TMGglobal.tmg_F_table);
		resetProgress();

	//NAME PART VALUE
		loadFileTMG(TMGtypes.NAME_PART_VALUE);
		tmgReader = tableLoader(tmgFileName + "_" + TMGtypes.NAME_PART_VALUE + ".dbf");
		TMGglobal.tmg_NPV_table = new TMGtableData(tmgReader,"RECNO", pointConvert);
		TMGglobal.tmg_NPV_table.TMGtableMultiData(tmgReader);
		if (TMGglobal.DUMP) statusUpdate(TMGtypes.NAME_PART_VALUE, TMGglobal.tmg_NPV_table);
		resetProgress();

	//NAME PART TYPE
		loadFileTMG(TMGtypes.NAME_PART_TYPE);
		tmgReader = tableLoader(tmgFileName + "_" + TMGtypes.NAME_PART_TYPE + ".dbf");
		TMGglobal.tmg_NPT_table = new TMGtableData(tmgReader,"ID", pointConvert);
		TMGglobal.tmg_NPT_table.TMGtableSingleData(tmgReader);
		if (TMGglobal.DUMP) statusUpdate(TMGtypes.NAME_PART_TYPE, TMGglobal.tmg_NPT_table);
		resetProgress();

	//NAME PART DICTIONARY
		loadFileTMG(TMGtypes.NAME_DICTIONARY);
		tmgReader = tableLoader(tmgFileName + "_" + TMGtypes.NAME_DICTIONARY + ".dbf");
		setMemoFile(tmgReader,tmgFileName + "_" + TMGtypes.NAME_DICTIONARY + ".fpt");
		TMGglobal.tmg_ND_table = new TMGtableData(tmgReader,"UID", pointConvert);
		TMGglobal.tmg_ND_table.TMGtableSingleData(tmgReader,false);
		if (TMGglobal.DUMP) statusUpdate(TMGtypes.NAME_DICTIONARY, TMGglobal.tmg_ND_table);
		resetProgress();

	//TAG TYPE TABLE
		loadFileTMG(TMGtypes.TAG_TYPE);
		tmgReader = tableLoader(tmgFileName + "_" + TMGtypes.TAG_TYPE + ".dbf");
		setMemoFile(tmgReader,tmgFileName + "_" + TMGtypes.TAG_TYPE + ".fpt");
		TMGglobal.tmg_T_table = new TMGtableData(tmgReader,"ETYPENUM", pointConvert);
		TMGglobal.tmg_T_table.TMGtableSingleData(tmgReader);
		if (TMGglobal.DUMP) statusUpdate(TMGtypes.TAG_TYPE , TMGglobal.tmg_T_table);
		resetProgress();
	} // Name loading

	public void loadTmgPlaceTables() throws HCException {
		if (HGlobal.writeLogs)
			HB0711Logging.logWrite("Action: in TMGloader loading Place tables");
		resetProgress();
	//PLACE FILE
		loadFileTMG(TMGtypes.PLACE);
		tmgReader = tableLoader(tmgFileName + "_" + TMGtypes.PLACE + ".dbf");
		setMemoFile(tmgReader,tmgFileName + "_" + TMGtypes.PLACE + ".fpt");
		TMGglobal.tmg_P_table = new TMGtableData(tmgReader,"RECNO", pointConvert);
		TMGglobal.tmg_P_table.TMGtableSingleData(tmgReader);
		if (TMGglobal.DUMP) statusUpdate(TMGtypes.PLACE, TMGglobal.tmg_P_table);
		resetProgress();

	//PLACE DICTIONARY
		loadFileTMG(TMGtypes.PLACE_DICTIONARY);
		tmgReader = tableLoader(tmgFileName + "_" + TMGtypes.PLACE_DICTIONARY + ".dbf");
		setMemoFile(tmgReader,tmgFileName + "_" + TMGtypes.PLACE_DICTIONARY + ".fpt");
		TMGglobal.tmg_PD_table = new TMGtableData(tmgReader,"UID", pointConvert);
		TMGglobal.tmg_PD_table.TMGtableSingleData(tmgReader,false);
		if (TMGglobal.DUMP) statusUpdate(TMGtypes.PLACE_DICTIONARY, TMGglobal.tmg_PD_table);
		resetProgress();

	//PLACE	PART TYPE
		loadFileTMG(TMGtypes.PLACE_PART_TYPE);
		tmgReader = tableLoader(tmgFileName + "_" + TMGtypes.PLACE_PART_TYPE + ".dbf");
		TMGglobal.tmg_PPT_table = new TMGtableData(tmgReader,"ID", pointConvert);
		TMGglobal.tmg_PPT_table.TMGtableSingleData(tmgReader);
		if (TMGglobal.DUMP) statusUpdate(TMGtypes.PLACE_PART_TYPE, TMGglobal.tmg_PPT_table);
		resetProgress();

	//PLACE	PART VALUE
		loadFileTMG(TMGtypes.PLACE_PART_VALUE);
		tmgReader = tableLoader(tmgFileName + "_" + TMGtypes.PLACE_PART_VALUE + ".dbf");
		TMGglobal.tmg_PPV_table = new TMGtableData(tmgReader,"RECNO", pointConvert);
		TMGglobal.tmg_PPV_table.TMGtableMultiData(tmgReader);
		if (TMGglobal.DUMP) statusUpdate(TMGtypes.PLACE_PART_VALUE, TMGglobal.tmg_PPV_table);
		resetProgress();
	}

/**
 *
 * @param all - load all tables or only E table
 * @throws HCException
 */
	public void loadTmgEventTables(boolean all) throws HCException {
		
		if (HGlobal.writeLogs)
			HB0711Logging.logWrite("Action: in TMGloader loading Event tables");
		
		resetProgress();

	// WITNESS TABLE
		loadFileTMG(TMGtypes.EVENT_WITNESS);
		tmgReader = tableLoader(tmgFileName + "_" + TMGtypes.EVENT_WITNESS + ".dbf");
		setMemoFile(tmgReader,tmgFileName + "_" + TMGtypes.EVENT_WITNESS + ".fpt");
		TMGglobal.tmg_E_table = new TMGtableData(tmgReader,"GNUM", pointConvert);
		TMGglobal.tmg_E_table.TMGtableMultiData(tmgReader); // New 22.11.2024
		if (TMGglobal.DUMP) statusUpdate(TMGtypes.EVENT_WITNESS, TMGglobal.tmg_E_table);
		resetProgress();

	// EVENT TABLE
		if (all) {
			loadFileTMG(TMGtypes.EVENT);
			tmgReader = tableLoader(tmgFileName + "_" + TMGtypes.EVENT + ".dbf");
			setMemoFile(tmgReader,tmgFileName + "_" + TMGtypes.EVENT + ".fpt");
			TMGglobal.tmg_G_table = new TMGtableData(tmgReader,"RECNO", pointConvert);
			TMGglobal.tmg_G_table.TMGtableSingleData(tmgReader);
			if (TMGglobal.DUMP) statusUpdate(TMGtypes.EVENT, TMGglobal.tmg_G_table);
			resetProgress();

		//TAG TYPE TABLE
			loadFileTMG(TMGtypes.TAG_TYPE);
			tmgReader = tableLoader(tmgFileName + "_" + TMGtypes.TAG_TYPE + ".dbf");
			setMemoFile(tmgReader,tmgFileName + "_" + TMGtypes.TAG_TYPE + ".fpt");
			TMGglobal.tmg_T_table = new TMGtableData(tmgReader,"ETYPENUM", pointConvert);
			TMGglobal.tmg_T_table.TMGtableSingleData(tmgReader);
			if (TMGglobal.DUMP) statusUpdate(TMGtypes.TAG_TYPE , TMGglobal.tmg_T_table);
			resetProgress();
		}
	}

	public void loadTmgSourceTables() throws HCException {
		if (HGlobal.writeLogs)
			HB0711Logging.logWrite("Action: in TMGloader loading Source tables");
	// Source type
		loadFileTMG(TMGtypes.SOURCE_TYPE);
		tmgReader = tableLoader(tmgFileName + "_" + TMGtypes.SOURCE_TYPE + ".dbf");
		setMemoFile(tmgReader,tmgFileName + "_" + TMGtypes.SOURCE_TYPE + ".fpt");
		TMGglobal.tmg_A_table = new TMGtableData(tmgReader,"SOURTYPE", pointConvert);
		TMGglobal.tmg_A_table.TMGtableSingleData(tmgReader);
		if (TMGglobal.DUMP) statusUpdate(TMGtypes.SOURCE_TYPE, TMGglobal.tmg_A_table);
		resetProgress();

	// Source file
		loadFileTMG(TMGtypes.SOURCE);
		tmgReader = tableLoader(tmgFileName + "_" + TMGtypes.SOURCE + ".dbf");
		setMemoFile(tmgReader,tmgFileName + "_" + TMGtypes.SOURCE + ".fpt");
		TMGglobal.tmg_M_table = new TMGtableData(tmgReader,"MAJNUM", pointConvert);
		TMGglobal.tmg_M_table.TMGtableSingleData(tmgReader);
		if (TMGglobal.DUMP) statusUpdate(TMGtypes.SOURCE, TMGglobal.tmg_M_table);
		resetProgress();

	// Repository file
		loadFileTMG(TMGtypes.REPOSITORY);
		tmgReader = tableLoader(tmgFileName + "_" + TMGtypes.REPOSITORY + ".dbf");
		setMemoFile(tmgReader,tmgFileName + "_" + TMGtypes.REPOSITORY + ".fpt");
		TMGglobal.tmg_R_table = new TMGtableData(tmgReader,"RECNO", pointConvert);
		TMGglobal.tmg_R_table.TMGtableSingleData(tmgReader);
		if (TMGglobal.DUMP) statusUpdate(TMGtypes.REPOSITORY, TMGglobal.tmg_R_table);
		resetProgress();

	// Source Citations file
		loadFileTMG(TMGtypes.SOURCE_CITATION);
		tmgReader = tableLoader(tmgFileName + "_" + TMGtypes.SOURCE_CITATION + ".dbf");
		setMemoFile(tmgReader,tmgFileName + "_" + TMGtypes.SOURCE_CITATION + ".fpt");
		TMGglobal.tmg_S_table = new TMGtableData(tmgReader,"RECNO", pointConvert);
		TMGglobal.tmg_S_table.TMGtableSingleData(tmgReader);
		if (TMGglobal.DUMP) statusUpdate(TMGtypes.SOURCE_CITATION, TMGglobal.tmg_S_table);
		resetProgress();

	// Source Element file
		loadFileTMG(TMGtypes.SOURCE_ELEMENT);
		tmgReader = tableLoader(tmgFileName + "_" + TMGtypes.SOURCE_ELEMENT + ".dbf");
		TMGglobal.tmg_U_table = new TMGtableData(tmgReader,"RECNO", pointConvert);
		TMGglobal.tmg_U_table.TMGtableSingleData(tmgReader);
		if (TMGglobal.DUMP) statusUpdate(TMGtypes.SOURCE_ELEMENT, TMGglobal.tmg_U_table);
		resetProgress();

		// Source Citations file
		loadFileTMG(TMGtypes.REPOSITORY_LINK);
		tmgReader = tableLoader(tmgFileName + "_" + TMGtypes.REPOSITORY_LINK + ".dbf");
		TMGglobal.tmg_W_table = new TMGtableData(tmgReader,"RNUMBER", pointConvert);
		TMGglobal.tmg_W_table.TMGtableSingleData(tmgReader);
		if (TMGglobal.DUMP) statusUpdate(TMGtypes.REPOSITORY_LINK, TMGglobal.tmg_W_table);
		resetProgress();
	}

/**
 * Test load exhibit table
 * @throws HCException
 */
	public void loadTmgExhibitTables() throws HCException {
	//DBFReader tmgReader;
		if (TMGglobal.DUMP) System.out.println(" Loading Exhibit Tables");
		if (HGlobal.writeLogs)
			HB0711Logging.logWrite("Action: in TMGloader loading Exhibit tables");
		resetProgress();
	//EXHIBIT FILE
		loadFileTMG(TMGtypes.EXHIBIT);
		tmgReader = tableLoader(tmgFileName + "_" + TMGtypes.EXHIBIT + ".dbf");
		setMemoFile(tmgReader,tmgFileName + "_" + TMGtypes.EXHIBIT + ".fpt");
		TMGglobal.tmg_I_table = new TMGtableData(tmgReader,"IDEXHIBIT", pointConvert);

/**
 * 	For I.dbf the reading with TMGtableSingleData is not used
 * 	Reads now one row at a time
 * Saves memory if PC has only 8G memory
 */
		if (TMGglobal.DUMP) statusUpdate(TMGtypes.EXHIBIT, TMGglobal.tmg_I_table);
	}

/**
 * testTmgTables(String reportDump)
 * @param reportDump
 * @throws HCException
 */
	public void testTmgTables(String reportDump) throws HCException {

		if (TMGglobal.DEBUG) testTMGaccess();
		try {
	    	 switch (reportDump) {
		    	 case TMGtypes.PERSON : {
		    		 String [] dataSets = {"PER_NO","FATHER","MOTHER","SPOULAST"};
		    		 dumpTMGsingleTable("DATASET FILE",TMGglobal.tmg_$_table,10,dataSets);
		    	 	}
		    	 	break;
		    	 case TMGtypes.DATA_SET  : {
		    		 String [] dataSets = {"DSID","DSNAME","DSLOCATION","DSTYPE"};
		    		 dumpTMGsingleTable("DATASET FILE",TMGglobal.tmg_D_table,2,dataSets);
		    	 	}
		    	 	break;
		    	 case TMGtypes.EVENT : {
		    		 	String [] events = {"RECNO","ETYPE","PER1","PER2","EFOOT"};
						dumpTMGsingleTable("EVENT FILE",TMGglobal.tmg_G_table,20,events);
		    	 	}
		    	 	break;
		    	 case TMGtypes.EXHIBIT : {
		    		 	String [] exhibits = {"IDEXHIBIT","XNAME","TEXT","IMAGE","DESCRIPT","THUMB"};
						dumpTMGsingleTable("EXHIBIT FILE",TMGglobal.tmg_I_table,100,exhibits);
		    	 	}
		    	 	break;
		    	 case TMGtypes.NAME  : {
		    		 	String [] names = {"RECNO","NPER","ALTYPE","SRNAMESORT","NNOTE"};
						dumpTMGsingleTable("NAME FILE",TMGglobal.tmg_N_table,20,names);
		    	 	}
		    	 	break;
		    	 case TMGtypes.PLACE : {
		    		 	String [] eventsPlaceFile = {"RECNO","STYLEID","STARTYEAR","COMMENT","SHORTPLACE"};
						dumpTMGsingleTable("PLACE FILE",TMGglobal.tmg_P_table,10,eventsPlaceFile);
		    	 	}
		    	 	break;
		    	 case TMGtypes.PLACE_DICTIONARY : {
		    		 	String [] eventsPlaceDic = {"UID","VALUE","SDX","TT"};
						dumpTMGsingleTable("PLACE DICTIONARY",TMGglobal.tmg_PD_table,110,eventsPlaceDic);
		    	 	}
		    	 	break;
				case TMGtypes.PLACE_PART_TYPE : {
						String [] eventsPlacePartType = {"ID","TYPE","VALUE","SHORTVALUE","DSID"};
						dumpTMGsingleTable("PLACE PART TYPE",TMGglobal.tmg_PPT_table,20,eventsPlacePartType);
					}
					break;
				case TMGtypes.PLACE_PART_VALUE : {
						String [] styleNameValue = {"RECNO","UID","TYPE","ID","DSID"};
						dumpTMGmultiTable("PLACE PART VALUE",TMGglobal.tmg_PPV_table,20,styleNameValue);
					}
					break;
				case TMGtypes.STYLE : {
						loadTmgSupportTables(); // Load TMG table
						String [] eventType = {"STYLEID","ST_DISPLAY","ST_OUTPUT","GROUP","STYLENAME"};
						dumpTMGsingleTable("STYLE TYPE FILE",TMGglobal.tmg_ST_table,20,eventType);
						if (TMGglobal.tmg_ST_table != null) TMGglobal.tmg_ST_table.closeTMGtable();
						TMGglobal.tmg_ST_table = null;
					}
					break;
				case TMGtypes.TAG_TYPE : {
						String [] eventType = {"ETYPENUM","ETYPENAME","ABBREV","PASTTENSE","PROPERTIES"};
						dumpTMGsingleTable("TAG TYPE FILE",TMGglobal.tmg_T_table,20,eventType);
					}
				default:  if (TMGglobal.DUMP) System.out.println("TMG table not selected ");
			}
		} catch (HCException hde) {
			if (HGlobal.writeLogs)
				HB0711Logging.logWrite("ERROR: in TMGloader "); //$NON-NLS-1$
			throw new HCException("TMGloader error: "+ hde.getMessage());
		}
	}

/**
 * tableLoader
 * @param fileName
 * @return
 * @throws HCException
 */
	private DBFReader tableLoader(String fileName) throws HCException  {
		String dbfFile = tmgBaseFolder + File.separator + fileName;
		DBFReader tmgTable = null;

		try {
			tmgTable = new DBFReader(new FileInputStream(dbfFile));
		} catch (FileNotFoundException fnfe) {
			if (HGlobal.writeLogs)
				HB0711Logging.logWrite("ERROR: in TMGloader " + fnfe.getMessage()); //$NON-NLS-1$
			throw new HCException("TMGloader error: "+ fnfe.getMessage());

		}
		int numberOfFields = tmgTable.getFieldCount();
		if (TMGglobal.DEBUG)
			System.out.println("Number of Fields in: " + fileName + " - NrFields: " + numberOfFields);
		return tmgTable;
	}

/**
 * Open fpt file from read memory fields
 * @param tmgPDreader
 * @param fileName
 */
	private void setMemoFile(DBFReader tmgPDreader, String fileName) {
		try {
		String fptFile = tmgBaseFolder + File.separator + fileName;
		tmgPDreader.setMemoFile(new File(fptFile),true);
		if (TMGglobal.DEBUG)
			System.out.println("Memory FTP file opened: " + fptFile);
		} catch (DBFException dbfe) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in TMGloader open FTP file " + dbfe.getMessage());
				HB0711Logging.printStackTraceToFile(dbfe);
			}
		}
	}

	private void testTMGaccess() throws HCException {
		System.out.println();
		String fieldValue;
		fieldValue = TMGglobal.tmg_$_table.getValueString(1,"PBIRTH");
		System.out.println("TMG table field: " + fieldValue);
		fieldValue = TMGglobal.tmg_N_table.getValueString(1,"SRNAMESORT");
		System.out.println("TMG table field: " + fieldValue);
	}

/**
 *
 * @param tableName
 * @param tmgTable
 * @param maxNoDump
 * @param fields
 */
	private void dumpTMGsingleTable(String tableName, TMGtableData tmgTable, int maxNoDump, String [] fields) throws HCException {
		// Must be in DEBUG mode to have entered this code
		int nrOftmgRows = tmgTable.getNrOfRows();
		if (maxNoDump > nrOftmgRows) maxNoDump = nrOftmgRows;
		System.out.println("\nTMGloader: " + tableName +  " rows: " + nrOftmgRows);
		for (int i = 0; i < maxNoDump; i++ ) {
			System.out.println("Row - " + (i+1) + ": ");
			for (String field : fields) {
				if (field.equals("IMAGE") || field.equals("THUMB") ) {
					System.out.print(field + " = " + tmgTable.getByteContent(i,field).length + " & ");
				} else
					System.out.print("## Text: ");
					System.out.print(field + " = " + tmgTable.getValueString(i,field) + "\n");
			}
		}
	}

// NB: original code for above was as follows - edited as it uses filenames unique to NTo!
/*
	private void dumpTMGsingleTable(String tableName, TMGtableData tmgTable, int maxNoDump, String [] fields) throws HCException {
		int nrOftmgRows = tmgTable.getNrOfRows();
		if (maxNoDump > nrOftmgRows) maxNoDump = nrOftmgRows;
		System.out.println("\nTMGloader: " + tableName +  " rows: " + nrOftmgRows);
		for (int i = 0; i < maxNoDump; i++ ) {
			System.out.println("Row - " + (i+1) + ": ");
			for (String field : fields) {
				if (field.equals("IMAGE") || field.equals("THUMB") ) {
					System.out.print(field + " = " + tmgTable.getByteContent(i,field).length + " & ");
					byte[] image = tmgTable.getByteContent(i,field);
					String fileName = "";
					if (field.equals("IMAGE")) fileName = "exhibit_image" + i + ".jpg";
					if (field.equals("THUMB")) fileName = "thumb_image" + i + ".jpg";
					File outputFile = new File("C:\\Users\\Nils\\HRE\\Exhibits\\" + fileName);
					try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
					    outputStream.write(image);
					} catch (FileNotFoundException | IOException e) {
						e.printStackTrace();
					}
				} else
					System.out.print("## Text: ");
					System.out.print(field + " = " + tmgTable.getValueString(i,field) + "\n");
			}
			System.out.println();
		}
	}
 */

	private void dumpTMGmultiTable(String tableName, TMGtableData tmgMultiTable, int maxNoDump, String [] fields) throws HCException {
		// Must be in DEBUG mode to have entered this code
		int nrOfMultitmgRows = tmgMultiTable.getMultiRowNr();
		if (maxNoDump > nrOfMultitmgRows) maxNoDump = nrOfMultitmgRows;
		System.out.println("\nTMGloader: " + tableName +  " Multi rows: " + nrOfMultitmgRows);
		for (int i = 0; i < maxNoDump; i++ ) {
			Vector<DBFRow> placeVector = tmgMultiTable.findVectorRows(i);
			//Test
			if (placeVector == null) System.out.println("Nr: " + i + " - Vector = null");
			else {
				System.out.println("Nr: " + i + " - Vector - sice: " + placeVector.size());
				for (int j = 0; j < placeVector.size();j++ ) {
					System.out.print("Element:  " + j + " - ");
					DBFRow rowObj = placeVector.get(j);
					for (String field : fields) {
						System.out.print(field + "=" + rowObj.getString(field) + "  ");
					}
				System.out.println();
				}
			}
		}
	}
}
