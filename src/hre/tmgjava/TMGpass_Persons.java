package hre.tmgjava;
/**********************************************************************
 * Uses library com.linuxense.javadbf
 * Java library for reading and writing Xbase (dBase/DBF) files
 * https://github.com/albfernandez/javadbf
 * albfernandez/javadbf is licensed under the
 * GNU Lesser General Public License v3.0
 * Written by Alberto Fern�ndez
 *************************************************************************************
 * Update BIO tables in HRE
 * v0.00.0018 2020-03-05 - First version (N. Tolleshaug)
 * v0.00.0019 2020-03-15 - Updated with progress monitor
 * v0.00.0021 2020-04-01 - Upgraded to HRE database v17b9 2020-03-04
 * v0.00.0021 2020-04-09 - Added check for DSID when converting
 * v0.00.0022 2020-05-14 - Updated person conversion T401_BIO
 * 			  2020-05-27 - Preliminary updated for version 18b
 * 			  2020-06-13 - databaseVersion = "v18c 2020-06-01"
 * 			  2020-06-30 - databaseVersion = "v19a 2020-06-21"
 * 			  2020-06-30 - HDate implemented according to T751
 * 			  2020-07-01 - Test tmgDate.startsWith("100000000")
 * 			  2020-07-30 - Updated for DDL v20a 2020-07-26
 * v0.01.0025 2020-11-13 - Primary name or BEST_NAME_RPID implemented. (N. Tolleshaug)
 *			  2020-11-15 - Removed DDL19a (N. Tolleshaug)
 *			  2020-11-18 - Implemented name display options DISP/SORT (N. Tolleshaug)
 *			  2020-11-25 - Implemented fix for Father or Mother = 0 (N. Tolleshaug)
 *			  2020-12-04 - Fix for HDATE error T401_LIFE_FORMS (N. Tolleshaug)
 * v0.01.0026 2021-07-09 - Removed "T402_PERSON_NAMES","DISPLAY_SUR_FIRST","VARCHAR(100)"
 *         				 - Removed"T402_PERSON_NAMES","DISPLAY_GIV_FIRST","VARCHAR(100)");
 * v0.02.0028 2023-01-06 - Changed name date in T402 from PBIRTH to NDATE
 * v0.02.0029 2023-05-01 - Implemented v22a (N. Tolleshaug)
 * v0.03.0030 2023-07-08 - Implemented Reference in T401 (N. Tolleshaug)
 * 			  2023-07-10 - Removed LAST_PARTNER / TMG SPOULAST (N. Tolleshaug)
 * 			  2023-08-12 - Updated for Sample UK processing with enum < 1000 (N. Tolleshaug)
 * 			  2023-08-13 - Test for flagOptions.length() processing flag values (N. Tolleshaug)
 * 			  2023-08-23 - Fix for processing TMG default flag values (N. Tolleshaug)
 * 			  2023-08-31 - Error report TMG flag error (N. Tolleshaug)
 * v0.03.0031 2023-10-20 - Updated for v22b database. (N. Tolleshaug)
 * 			  2023-11-24 - line 653 fix for Parent Role into T405. (N. Tolleshaug)
 * 			  2024-03-16 - line 654 Corrected for parent type import to T405. (N. Tolleshaug)
 * 			  2024-06-17 - Added import of NAME_EVNT_TYPE to T402_PERS_NAME
 * v0.04.0032 2024-12-22 - Updated for v22c database (N. Tolleshaug)
 * 			  2025-05-18 - Changed for T405 to PID = offset + s.recno (N. Tolleshaug)
 * 			  2026-01-17 - Log all catch blocks (D Ferguson)
 * 			  2026-01-22 - Import TMG Relationship fields (D Ferguson)
 ****************************************************************************************/

import java.sql.ResultSet;
import java.sql.SQLException;

import hre.bila.HB0711Logging;
import hre.gui.HGlobal;


/**
 * class TMGpass_Persons
 * @author NTo
 * @since 2020-03-15
 */

public class TMGpass_Persons {
	private TMGpass_Support pointSupportPass;
	private TMGHREconverter tmgHreConverter;

	TMGtableData  tmg$table = null;
	TMGtableData  tmgCtable = null;
	TMGtableData  tmgFtable = null;
	TMGtableData  tmgNtable = null;
	TMGtableData  tmgNDtable = null;
	TMGtableData  tmgNPTtable = null;
	TMGtableData  tmgNPVtable = null;
	TMGtableData  tmgTtable = null;

	ResultSet T104 = null;
	ResultSet T204 = null;
	ResultSet T131 = null;
	ResultSet T170 = null;
	ResultSet T252 = null;
	ResultSet T401 = null;
	ResultSet T402 = null;
	ResultSet T403 = null;
	ResultSet T405 = null;

	long proOffset = 1000000000000000L;
	long null_RPID  = 1999999999999999L;
	long flagTablePID = proOffset + 1;
	String flagFieldString;
	String[] flagFields;
	int[] flagIndexValues;
	int nrDefaultFlag = 0;
	int nrTotalFlag = 0;
	int errorFlag = 0; // count number of flag errors
	// Max number printed
	int maxNrPrint = 20;

/**
 * TMGPassBIO_NamePersons(TMGDatabaseHREhandler pointHREbase)
 * @param pointHREbase
 */
	public TMGpass_Persons(HREdatabaseHandler pointHREbase) {

		tmg$table = TMGglobal.tmg_$_table;
		tmgCtable = TMGglobal.tmg_C_table;
		tmgFtable = TMGglobal.tmg_F_table;
		tmgNtable = TMGglobal.tmg_N_table;
		tmgNDtable = TMGglobal.tmg_ND_table;
		tmgNPTtable = TMGglobal.tmg_NPT_table;
		tmgNPVtable = TMGglobal.tmg_NPV_table;
		tmgTtable = TMGglobal.tmg_T_table;

		T104 = TMGglobal.T104;
		T204 = TMGglobal.T204;
		T131 = TMGglobal.T131;
		T252 = TMGglobal.T252;
		T401 = TMGglobal.T401;
		T402 = TMGglobal.T402;
		T403 = TMGglobal.T403;
		T405 = TMGglobal.T405;
		T170 = TMGglobal.T170;
	}

/**
 * Constructor addPersonsToHRE()
 * @throws HCException
 */

	public void addPersonsToHRE(TMGHREconverter tmgHreConverter) throws HCException {
		this.pointSupportPass = tmgHreConverter.pointSupportPass;
		int currentRow = 0;
		int flagIndex = -1;
		long flagDefsRPID;
		try {
		// Find last PID
			T401.last();
			if (TMGglobal.DEBUG)
				System.out.println("T401 Table ResultSet size before insert: " + T401.getRow());

			if (TMGglobal.DEBUG) System.out.println("SQL PERSON tmg$Table end PID: " + (proOffset + tmg$table.getNrOfRows()));
			if (TMGglobal.DEBUG) System.out.println("SQL NAME tmgNTable end PID: " + (proOffset + tmgNtable.getNrOfRows()));

			flagFields = tmgHreConverter.pointSupportPass.getFlagFields();

			flagIndexValues = new int[flagFields.length];

			int nrOftmg$Rows = tmg$table.getNrOfRows();
			if (TMGglobal.DEBUG)
				System.out.println(" tmg _$.dbf Table size: " + nrOftmg$Rows + " rows");

			int missingNrOf$PID = 0;
			int progress;
			tmgHreConverter.setStatusProgress(0);

			if (TMGglobal.DEBUG)
				System.out.println("*** Updating HRE T401_LIFE_FORMS");

		// Insert new records - number starts from indexPID = 1
			for (int index$PID = 0; index$PID < nrOftmg$Rows; index$PID++) {
				currentRow = index$PID + 1;

		// Report progress in %
				progress = (int)Math.round(((double)currentRow / (double)nrOftmg$Rows) * 100);
				tmgHreConverter.setStatusProgress(progress);

				if (TMGglobal.DEBUG)
					System.out.println("** TMG name_$.dbf - indexPID: " + index$PID
						+ " / PER_NO: " + tmg$table.getValueInt(index$PID,"PER_NO"));

				if (tmg$table.getValueInt(index$PID,"PER_NO") > 0 ) {

				// Process T401_PERSONS
					if (tmg$table.getValueInt(index$PID,"DSID") == 0)
						System.out.println(tmg$table.getValueInt(index$PID,"PER_NO") + " Dataset ID: " + tmg$table.getValueInt(index$PID,"DSID"));

					if (TMGglobal.dataSetID == tmg$table.getValueInt(index$PID,"DSID")) {

					// Processing flags
						String flagSetting = "", flagOptions = "";
						int ctableRow;
						for (int i= 0; i < flagFields.length; i++) {

							int flagIdent251 = tmgHreConverter.pointSupportPass.getFlagIdentIndex().get(flagFields[i]);

							flagSetting = tmg$table.getValueString(index$PID, flagFields[i]).trim();
							ctableRow = tmgHreConverter.pointSupportPass.getTmgFlagFieldIndex().get(flagFields[i]);

					// Find the flag index for a flag
							flagIndex = -1;
							flagOptions = tmgCtable.findValueString(ctableRow, "FLAGVALUE");
							String[] optionArray = flagOptions.split(",");
							for (int j = 0; j < optionArray.length; j++) {
								if (optionArray[j].equals(flagSetting)) {
									flagIndex = j;
									break;
								}
							}

						// Set the correct value into T401 flag fields
							if (flagIdent251 < 8) flagIndexValues[flagIdent251-1] = flagIndex;
							if (flagIndex == -1) {
								errorFlag++;
								if (TMGglobal.FLAGCHECK)
									System.out.println(" T401 flags processing error: " + flagFields[i] + " - "
											+ (flagIdent251) + " /Options: " + flagOptions
											+ " /Setting: " + flagSetting + " /Index: " + flagIndex);
							}

						// Record user flag settings
							int defaultFlagIndex = 0;
							int tmgFlagIdent = tmgCtable.findValueInt(ctableRow, "FLAGID");

						// Only record Flag setting != default
							if (flagFields[i].startsWith("FLAG")) {
								nrTotalFlag++;
								if (TMGglobal.DEBUG) System.out.println(" FlagField: "+ flagFields[i]
										+ " Value: " + flagSetting + " Options: " + flagOptions);
								flagDefsRPID = tmgHreConverter.pointSupportPass.getFlagDefsT251PID().get(tmgFlagIdent);

						// Get the default index from T251
								if (tmgHreConverter.pointSupportPass.getDefaultIndex().containsKey(tmgFlagIdent))
									defaultFlagIndex = tmgHreConverter.pointSupportPass.getDefaultIndex().get(tmgFlagIdent);
								else System.out.println(" No default index in T251 for flagId: " + tmgFlagIdent );

						// Create flag value row in T252
								if (flagOptions.length() > 0)
									if (flagSetting.equals(flagOptions.charAt(defaultFlagIndex) + "")) nrDefaultFlag++;
									else addToT252_FLAG_VALU(index$PID, flagDefsRPID, flagIdent251, flagIndex, T252);
							}
						}

					// Now its time to create person in T401 with flag values
						addToT401_PERSONS(index$PID, flagIndexValues, T401);

					} else System.out.println("Not processed dataset tmg$table: "
							+ tmg$table.getValueInt(index$PID,"DSID"));

				} else {
					missingNrOf$PID++;
					System.out.println(" Not found PID - TMG name_$.dbf - indexPID: "
							+ index$PID + " / " + tmg$table.getValueInt(index$PID,"PER_NO"));
				}
			} // end loop insert T401

			if (TMGglobal.DEBUG) {
				int percent = 100-(int)Math.round(((double)nrDefaultFlag / (double)nrTotalFlag) * 100);
				System.out.print(" Number of persons: " + currentRow + ",");
				System.out.println(" Flag stat T252 rows: " + (nrTotalFlag-nrDefaultFlag) + " - " + percent + "%,"
								+ " - Default/Total: " + nrDefaultFlag + "/" + nrTotalFlag);
			}

			tmgHreConverter.setStatusMessage(" *** Total # Flag errors: " + errorFlag);

			if (TMGglobal.DEBUG)
					System.out.println("Number of PER_NO's not used: "+ missingNrOf$PID);


		} catch (SQLException | HCException sqle) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in TMGpassPersons addPerson TMG $.dbf - indexPID: "
										+ currentRow + sqle.getMessage());
				HB0711Logging.printStackTraceToFile(sqle);
			}
			throw new HCException("SQLxception - addPerson: " + sqle.getMessage()
			+ "\nTMG name_$.dbf - indexPID: " + currentRow + "\n" + sqle.getMessage());
		}
	}

/**
 *
 * @param tmgHreConverter
 * @throws HCException
 */
	public void addNamesToHRE(TMGHREconverter tmgHreConverter) throws HCException {
		int currentRow = 0;
		long nameElementPID = proOffset;
		try {
		// Find start PID
			T402.last();
			if (TMGglobal.DEBUG)
				System.out.println(" T402 Table size: " + T402.getRow());
			if (TMGglobal.DEBUG) System.out.println("SQL NAME tmgNTable end PID: " + (proOffset + tmgNtable.getNrOfRows()));

		// Insert new records - number starts from indexPID = 1
			int nrOftmgNRows = tmgNtable.getNrOfRows();

			if (TMGglobal.DEBUG)
				System.out.println(" tmg _N.dbf Table size: " + nrOftmgNRows + " rows");

			int missingNrOfNPID = 0;
			int progress;
			tmgHreConverter.setStatusProgress(0);

		// New pass for T402_PERSON_NAMES
			if (TMGglobal.DEBUG)
				System.out.println("*** Updating HRE T402_PERSON_NAMES");

			tmgHreConverter.setStatusProgress(0);

			for (int indexNPID = 0; indexNPID < nrOftmgNRows; indexNPID++) {
				currentRow = indexNPID + 1;
				progress = (int)Math.round(((double)currentRow / (double)nrOftmgNRows) * 100);
				tmgHreConverter.setStatusProgress(progress);

				if (TMGglobal.DEBUG)
					System.out.println("** TMG name_N.dbf - indexPID: " + indexNPID
						+ " / NPER: " + tmgNtable.getValueInt(indexNPID,"NPER")
						+ " / STYLEID: " + tmgNtable.getValueInt(indexNPID,"STYLEID"));

			// Update counter of Name styles
				int styleIndex = tmgNtable.getValueInt(indexNPID,"STYLEID");
				pointSupportPass.addUsedStyle(styleIndex);

				if (tmgNtable.getValueInt(indexNPID,"NPER") > 0 )  {

					String[] nameValues = findNameValue(indexNPID);

			// T402_LIFE_PERSON_NAMES
					if(TMGglobal.dataSetID == tmgNtable.getValueInt(indexNPID,"DSID"))	{

							addToT402_PERS_NAME(indexNPID,T402);

				// Create T403 Name element for each name
						for (int i = 0; i < nameValues.length ; i++) {
							if (nameValues[i].length() > 0) {
							nameElementPID++;
							int encodingType = i;
							addToT403_PERS_NAME_ELEMNTS(indexNPID,
															nameElementPID,
															encodingType,
															T403,
															nameValues[i]);
							}
						}

					} else System.out.println("Not processed dataset tmgNtable: "
						+ tmgNtable.getValueInt(indexNPID,"DSID"));

				} else {
						missingNrOfNPID++;
						System.out.println("  Not found PID - TMG name_N.dbf - indexPID: "
								+ indexNPID + " / " + tmgNtable.getValueInt(indexNPID,"NPER"));
				}
			} // end T402

			if (TMGglobal.DEBUG)
				System.out.println("Number of NPER's not used: "+ missingNrOfNPID);

		} catch (SQLException | HCException sqle) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in TMGpassPersons addNames TMG N.dbf - indexPID: "
										+ currentRow + sqle.getMessage());
				HB0711Logging.printStackTraceToFile(sqle);
			}
			throw new HCException("SQLxception - addPerson: " + sqle.getMessage()
			+ "\nTMG name_N.dbf - indexPID: " + currentRow + "\n" + sqle.getMessage());
		}
	}

	public void addParentRelationToHRE(TMGHREconverter tmgHreConverter) throws HCException {
		this.tmgHreConverter = tmgHreConverter;
		int currentRow = 0;
		try {
		// Find last PID
			T405.last();
			if (TMGglobal.DEBUG)
				System.out.println("T401 Table ResultSet size before insert: " + T405.getRow());

			if (TMGglobal.DEBUG) System.out.println("SQL PERSON tmgFTable end PID: " + (proOffset + tmgFtable.getNrOfRows()));
			if (TMGglobal.DEBUG) System.out.println("SQL NAME tmgFTable end PID: " + (proOffset + tmgFtable.getNrOfRows()));

			int nrOftmgFRows = tmgFtable.getNrOfRows();
			if (TMGglobal.DEBUG) System.out.println(" tmg _F.dbf Table size: " + nrOftmgFRows + " rows");

			int progress;
			tmgHreConverter.setStatusProgress(0);

		// Insert new records - number starts from indexPID = 1
			for (int indexFPID = 0; indexFPID < nrOftmgFRows; indexFPID++) {
				currentRow = indexFPID + 1;

				if(TMGglobal.dataSetID == tmgFtable.getValueInt(indexFPID,"DSID"))	{
		// Report progress in %
				progress = (int)Math.round(((double)currentRow / (double)nrOftmgFRows) * 100);
				tmgHreConverter.setStatusProgress(progress);

				addToT405_PARENT_RELATION(indexFPID, T405);

				} else System.out.println("Not processed dataset tmgFtable: "
						+ tmgFtable.getValueInt(indexFPID,"DSID"));

			}

		} catch (SQLException | HCException sqle) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in TMGpassPersons addParent TMG F.dbf - indexPID: "
										+ currentRow + sqle.getMessage());
				HB0711Logging.printStackTraceToFile(sqle);
			}
			throw new HCException("SQLxception - addParentToHRE: " + sqle.getMessage()
			+ "\nTMG name_F.dbf - indexPID: " + currentRow + "\n" + sqle.getMessage());
		}
	}

/**
 * addToT401_BIOS(long rowPID, ResultSet HREtable)
 * @param rowPID
 * @param hreTable
 * @throws HCException
 */
	private void addToT401_PERSONS(int rowPID, int[] flagIndexValues, ResultSet hreTable) throws HCException {
		String tmgDate;
		if (TMGglobal.DEBUG)  System.out.println("Start - addTo T402_LIFE_FORMS row: " + rowPID);

		try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();
		// Update new row in H2 database
			hreTable.updateLong("PID", proOffset + tmg$table.getValueInt(rowPID,"PER_NO"));
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateBoolean("HAS_CITATIONS", false);
			hreTable.updateBoolean("HAS_OTHER_PARENTS", false); // ************** to be updated
			hreTable.updateLong("VISIBLE_ID", tmg$table.getValueInt(rowPID,"REF_ID"));
			hreTable.updateLong("ENTITY_TYPE_RPID", null_RPID);
		// ***************************************************
			int father = tmg$table.getValueInt(rowPID,"FATHER");
			if (father != 0)
				hreTable.updateLong("SPERM_PROVIDER_RPID", proOffset + father);
			else hreTable.updateLong("SPERM_PROVIDER_RPID",null_RPID);

			int mother = tmg$table.getValueInt(rowPID,"MOTHER");
			if (mother != 0)
				hreTable.updateLong("EGG_PROVIDER_RPID", proOffset + mother);
			else hreTable.updateLong("EGG_PROVIDER_RPID", null_RPID);
		//************************************************************
			tmgDate = tmg$table.getValueString(rowPID,"PBIRTH");
			if (tmgDate.length() == 0 || tmgDate.startsWith("100000000")) hreTable.updateLong("BIRTH_HDATE_RPID",null_RPID);
			else {
				hreTable.updateLong("BIRTH_HDATE_RPID", HREhdate.addToT170_22a_HDATES(T170, tmgDate));
			}
			hreTable.updateLong("BIRTH_PLACE_RPID", null_RPID); // *********** To be updated
			tmgDate = tmg$table.getValueString(rowPID,"PDEATH");
			if (tmgDate.length() == 0 || tmgDate.startsWith("100000000")) hreTable.updateLong("DEATH_HDATE_RPID", null_RPID);
			else {
				hreTable.updateLong("DEATH_HDATE_RPID", HREhdate.addToT170_22a_HDATES(T170, tmgDate));
			}
			hreTable.updateLong("DEATH_PLACE_RPID", null_RPID); // **** To be updated
		//***************************************************************'
			int perNoID = tmg$table.getValueInt(rowPID,"PER_NO");
			if (tmgNtable.existVector(perNoID)) {
				for ( int i = 0; i < tmgNtable.getVectorSize(perNoID); i++)
					if (tmgNtable.findVectorBoolean(perNoID, i, "PRIMARY")) {
						hreTable.updateLong("BEST_NAME_RPID", proOffset + tmgNtable.findVectorInt(perNoID,i,"RECNO"));
						if (TMGglobal.DEBUG) System.out.println(rowPID + " BEST_NAME_RPID: " + i
								+ " / " + tmgNtable.findVectorInt(perNoID,i,"RECNO"));
					}
			} else {
				System.out.println("Not existing vector index : " + rowPID);
				hreTable.updateLong("BEST_NAME_RPID", null_RPID);
			}

			hreTable.updateLong("BEST_IMAGE_RPID", null_RPID); // Updated in TMGpass_Exhibits

			hreTable.updateInt("PARTNER_COUNT", 0); // **** To be updated
			hreTable.updateLong("DNA_RPID", null_RPID); //PID to T407, not created
			hreTable.updateLong("LAST_EDIT_HDATE_RPID", null_RPID);
			hreTable.updateLong("MEMO_RPID", null_RPID);
			hreTable.updateString("SURETY", " Not set");

			hreTable.updateInt("BIRTH_SEX", flagIndexValues[0]);
			hreTable.updateInt("LIVING", flagIndexValues[1]);
			hreTable.updateInt("BIRTH_ORDER", flagIndexValues[2]);
			hreTable.updateInt("MULTI_BIRTH", flagIndexValues[3]);
			hreTable.updateInt("ADOPTED", flagIndexValues[4]);
			hreTable.updateInt("ANCESTOR_INT", flagIndexValues[5]);
			hreTable.updateInt("DESCENDANT_INT", flagIndexValues[6]);
			hreTable.updateString("REFERENCE", tmg$table.getValueString(rowPID,"REFERENCE"));
		// Relationship fields added in v22c
			hreTable.updateInt("RELATE1", tmg$table.getValueInt(rowPID,"RELATE"));
			hreTable.updateInt("RELATE2", tmg$table.getValueInt(rowPID,"RELATEFO"));
			hreTable.updateInt("RELATE3",0);
			hreTable.updateInt("RELATE4",0);

		//Insert row in database
			hreTable.insertRow();
		} catch (SQLException sqle) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in TMGpassPersons add to T401 row: " + rowPID + sqle.getMessage());
				HB0711Logging.printStackTraceToFile(sqle);
			}
			throw new HCException("TMGPass_Persons - addTo T401_PERSON - error: " + sqle.getMessage());
		}
	}

/**
 * addToT402_PERS_NAME(long rowPID,ResultSet hreTable)
 * @param rowPID
 * @param hreTable
 * @throws HCException
 */
	public void addToT402_PERS_NAME(int rowPID,ResultSet hreTable) throws HCException {
		String tmgDate;
		String nullDate = "100000000030000000000";

		if (TMGglobal.DEBUG)
			System.out.println("** addTo T402_PERSON_NAMES row: " + rowPID);

		int styleId = tmgNtable.getValueInt(rowPID,"STYLEID");
		try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();

			int etypeNumber = tmgNtable.getValueInt(rowPID,"ALTYPE");
			int origtype = tmgTtable.findValueInt(etypeNumber,"ORIGETYPE");

		// Set etype to 2XXX - Fix 31.02 - user defined match preloaded event types
			if (origtype != 0) etypeNumber = origtype + 1000;
			else etypeNumber = etypeNumber + 1000;

		// Update new row in database
			hreTable.updateLong("PID", proOffset + tmgNtable.getValueInt(rowPID,"RECNO"));
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateBoolean("HAS_CITATIONS", false);
			hreTable.updateBoolean("NAME_PRIMARY", tmgNtable.getValueBoolean(rowPID,"PRIMARY"));
			hreTable.updateInt("NAME_EVNT_TYPE", etypeNumber);
			hreTable.updateLong("OWNER_RPID", proOffset + tmgNtable.getValueInt(rowPID,"NPER"));

		// Set name style RPID
			hreTable.updateLong("NAME_STYLE_RPID",
					pointSupportPass.getNameStylePID(styleId));

		// Use "NDATE" as date for name - 2023-1-04 Changes made in Person Manager
			tmgDate = tmgNtable.getValueString(rowPID,"NDATE");

			if (tmgDate.length() == 0 || tmgDate.equals(nullDate)) hreTable.updateLong("START_HDATE_RPID", null_RPID);
			else hreTable.updateLong("START_HDATE_RPID", HREhdate.addToT170_22a_HDATES(T170, tmgDate));

			tmgDate = tmgNtable.getValueString(rowPID,"PDEATH");
			if (tmgDate.length() == 0 || tmgDate.equals(nullDate)) hreTable.updateLong("END_HDATE_RPID", null_RPID);
			else hreTable.updateLong("END_HDATE_RPID", HREhdate.addToT170_22a_HDATES(T170, tmgDate));

			hreTable.updateLong("THEME_RPID", null_RPID);
			hreTable.updateLong("MEMO_RPID", null_RPID);
			hreTable.updateString("SURETY", "3");
		//Insert row
			hreTable.insertRow();

		} catch (SQLException | HCException sqle) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in TMGpassPersons add to T402: " + sqle.getMessage());
				HB0711Logging.printStackTraceToFile(sqle);
			}
			throw new HCException("TMGPass_Persons - addTo table T402_PERSON_NAMES - error: "
					+ sqle.getMessage());
		}
	}

/**
 * addToT403_PERS_NAME_ELEMNTS
 * @param rowPID
 * @param primaryPID
 * @param encodingType
 * @param hreTable
 * @param nameElement
 * @throws HCException
 */
	public void addToT403_PERS_NAME_ELEMNTS(int rowPID,
												long primaryPID,
											    int encodingType,
											    ResultSet hreTable,
											    String nameElement)
			throws HCException {
		if (TMGglobal.DEBUG)
			System.out.println("** addTo T403_PERS_NAME_ELEMNTS row: " + primaryPID);
		try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();
			hreTable.updateLong("PID", primaryPID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateLong("OWNER_RPID", proOffset + tmgNtable.getValueInt(rowPID,"RECNO"));
			hreTable.updateString("LANG_CODE", ""); // Not needed ********************
			hreTable.updateString("ELEMNT_CODE", pointSupportPass.getPersonStyleCodes(encodingType));
			hreTable.updateString("NAME_DATA", nameElement);
		//Insert row
			hreTable.insertRow();

		} catch (SQLException sqle) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in TMGpassPersons add to T403: " + sqle.getMessage());
				HB0711Logging.printStackTraceToFile(sqle);
			}
			throw new HCException("TMGPass_Persons - addTo table T403_PERS_NAME_ELEMNTS - error: "
					+ sqle.getMessage());
		}
	}

/**
 * addToT252_FLAG_VALU(long tablePID, ResultSet hreTable)
 * @param tablePID
 * @param hreTable
 * @throws HCException
 */
   private void addToT252_FLAG_VALU(int rowPID, long flagDefsRPID, int flagId,
		   			int flagIndex, ResultSet hreTable) throws HCException {
	   	try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();
		// Update new row in H2 database
			hreTable.updateLong("PID", flagTablePID++);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateLong("FLAG_DEFN_RPID", flagDefsRPID);
			hreTable.updateLong("ENTITY_RPID", proOffset + tmg$table.getValueInt(rowPID,"PER_NO"));
			hreTable.updateInt("FLAG_IDENT", flagId);
			hreTable.updateInt("FLAG_INDEX", flagIndex); // New name for falg value
		//Insert row in database
			hreTable.insertRow();

		} catch (SQLException sqle) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in TMGpassPersons add to T252 row: " + flagTablePID + sqle.getMessage());
				HB0711Logging.printStackTraceToFile(sqle);
			}
			throw new HCException("TMGPass_Person - addToT252_FLAG_VALU - : " + sqle.getMessage());
		}
   }


/**
 * addToT405_PARENT_RELATION(int rowPID, ResultSet hreTable)
 * @param rowPID
 * @param hreTable
 * @throws HCException
 */
	private void addToT405_PARENT_RELATION(int rowPID, ResultSet hreTable) throws HCException {
		if (TMGglobal.DEBUG)  System.out.println("Start - addToT405_PARENT_RELATION row: " + rowPID);
		try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();

		// Modification 18.5.2025 to match S citation "F" records
			hreTable.updateLong("PID", proOffset + tmgFtable.getValueInt(rowPID,"RECNO"));
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateBoolean("HAS_CITATIONS", false);
			hreTable.updateLong("PERSON_RPID",
					proOffset + tmgFtable.getValueInt(rowPID,"CHILD")); // Child PID);
			hreTable.updateLong("PARENT_RPID",
					proOffset + tmgFtable.getValueInt(rowPID,"PARENT")); // Parent PID);
			hreTable.updateLong("START_HDATE_RPID", null_RPID); // No dates available for import from TMG
			hreTable.updateLong("END_HDATE_RPID", null_RPID); // No dates available for import from TMG

		// Set etype to 2XXX - Fix 31.02 - user defined match preloaded event types
			int parentType = tmgFtable.getValueInt(rowPID,"PTYPE");
			int origeType = tmgTtable.findValueInt(parentType,"ORIGETYPE");
			if (origeType != 0) parentType = origeType + 1000;
			else parentType = parentType + 1000; // Only if user defined parent type
			hreTable.updateInt("PARENT_TYPE", parentType);

		// Collect PNOTE
			String pnote = HREmemo.
					returnStringContent(tmgFtable.getValueString(rowPID,"PNOTE"));

		// Processing memo to T167_MEMO_SET
			if (pnote.length() == 0) hreTable.updateLong("MEMO_RPID", null_RPID);
			else hreTable.updateLong("MEMO_RPID",
					tmgHreConverter.pointHREmemo.addToT167_22c_MEMO(pnote));

			hreTable.updateString("SURETY", tmgFtable.getValueString(rowPID,"FSURE"));
			hreTable.updateLong("EVNT_RPID", null_RPID);

		//Insert row
			hreTable.insertRow();

		} catch (SQLException sqle) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in TMGpassPersons add to T405: " + sqle.getMessage());
				HB0711Logging.printStackTraceToFile(sqle);
			}
			throw new HCException("TMGPass_Persons - addTo table addToT405_PARENT_RELATION - error: "
					+ sqle.getMessage());
		}
	}

/**
 * displayNamesParts()
 * @throws HCException
 */
	public void displayNamesParts() throws HCException {
		System.out.println("\nTest Name Part Value started!");
		int nrOfVectorRows = tmgNtable.getMultiRowNr();

		for (int indexNPID = 0; indexNPID < nrOfVectorRows; indexNPID++) {
			testNameParts(indexNPID);
		}
		System.out.println("Test Name Part Value ended!\n");
	}

/**
 * testNameParts(int indexNPID)
 * @param indexG_PID
 * @param T452
 * @throws HCException
 */
	private void testNameParts(int indexNPID) throws HCException {

		int namePartValueInx, nameVectorSize = 0;
		boolean primary;
		String printPerson, printName = "";

		if (tmgNtable.existVector(indexNPID)) {
			nameVectorSize = tmgNtable.getVectorSize(indexNPID);
			printPerson = indexNPID + " Person : "
					+ tmgNtable.findVectorString(indexNPID, 0,"SRNAMEDISP")
					+ " names " + nameVectorSize ;
			if (indexNPID < maxNrPrint) System.out.println(printPerson);
			if (nameVectorSize == 1) {
				namePartValueInx = tmgNtable.findVectorInt(indexNPID,0,"RECNO");
				primary = tmgNtable.findVectorBoolean(indexNPID,0,"PRIMARY");
				printName = " --- Nr: " + "1 - prim: " + primary + " / ";
				printName = printName + exstractNameVaues(namePartValueInx, indexNPID);
				if (indexNPID < maxNrPrint) System.out.println(printName);
			} else for (int i = 0; i < nameVectorSize; i++) {
				namePartValueInx = tmgNtable.findVectorInt(indexNPID,i,"RECNO");
				primary = tmgNtable.findVectorBoolean(indexNPID,i,"PRIMARY");
				printName = " --- Nr: " + (i+1) + " - prim: " + primary + " / ";
				printName = printName + exstractNameVaues(namePartValueInx, indexNPID);
				if (indexNPID < maxNrPrint) System.out.println(printName);
			}
		}
	}

/**
 * exstractNameVaues(int namePartValueInx, int indexNPID)
 * @param namePartValueInx
 * @param indexNPID
 * @throws HCException
 */
	private String exstractNameVaues( int namePartValueInx, int indexNPID) throws HCException {
		int vectorPartSize, nameNrInx, namePartInx;
		String personNameValue, namePartType, nameParts = "" ;
		vectorPartSize = tmgNPVtable.getVectorSize(namePartValueInx);
		for (int i = 0; i < vectorPartSize; i++ ) {
			nameNrInx = tmgNPVtable.findVectorInt(namePartValueInx, i,"UID");
			namePartInx = tmgNPVtable.findVectorInt(namePartValueInx, i,"TYPE");
			personNameValue = tmgNDtable.findValueString(nameNrInx,"VALUE");
			namePartType = tmgNPTtable.findValueString(namePartInx,"VALUE");
			nameParts = nameParts + "(" + namePartInx + ")" + namePartType
					+ " : " + personNameValue + " / ";
		}
		return nameParts;
	}

/**
 * findNameValue(int namePartValueInx)
 * @param namePartValueInx
 * @return
 * @throws HCException
 */
	private String[] findNameValue(int indexNPID) throws HCException {
		int vectorPartSize, nameNrInx, namePartInx, namePartValueInx;
		String nameParts = "", namePartType, nameString = "";
		String[] personNameValue = new String[16];
		for (int i = 0; i < personNameValue.length ; i++ ) personNameValue[i] = "";
		try {
			namePartValueInx = tmgNtable.getValueInt(indexNPID,"RECNO");
			vectorPartSize = tmgNPVtable.getVectorSize(namePartValueInx);
			for (int i = 0; i < vectorPartSize; i++ ) {
				nameNrInx = tmgNPVtable.findVectorInt(namePartValueInx, i,"UID");
				namePartInx = tmgNPVtable.findVectorInt(namePartValueInx, i,"TYPE");
				namePartType = tmgNPTtable.findValueString(namePartInx,"VALUE");
				nameString = tmgNDtable.findValueString(nameNrInx,"VALUE").trim();

			// Convert surname to lowercas
				if (TMGglobal.LOWER_CASE)
					if (namePartInx == 5 || namePartInx == 8) {
						String nameLowerCase = "";
						if (TMGglobal.DEBUG) System.out.print(indexNPID + " - " + namePartInx + " name: " + nameString);
						if (Character.isUpperCase(nameString.charAt(0))
								&& Character.isUpperCase(nameString.charAt(nameString.length()-1))) {
							nameLowerCase = nameString.toLowerCase();
							nameLowerCase = Character.toUpperCase(nameLowerCase.charAt(0))
									+ nameLowerCase.substring(1);
							if (TMGglobal.DEBUG) System.out.print(" to lower: " + nameLowerCase);
							nameString = nameLowerCase;
						}
						if (TMGglobal.DEBUG) System.out.println();
					}
				personNameValue[namePartInx-1] = nameString;

				nameParts = nameParts + "(" + namePartInx + ")" + namePartType
						+ " : " + personNameValue[namePartInx-1] + " / ";
			}
		} catch (HCException hce) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in TMGpassPersons find Name value: " + hce.getMessage());
				HB0711Logging.printStackTraceToFile(hce);
			}
			throw new HCException("findNameValue error: " + hce.getMessage());
		}
		if (TMGglobal.DUMP)
			if (indexNPID < maxNrPrint)
				System.out.println("Name parts for: " + indexNPID + " / " + nameParts);
		return personNameValue;

	}

/**
 * printResSet(ResultSet resultSet)
 * @param resultSet
 * @throws SQLException
 */
   public static void printResSet(ResultSet resultSet) throws SQLException{
	   System.out.println("*** Print ResultSet");
	   //Ensure we start with first row
	      resultSet.beforeFirst();
	      while(resultSet.next()){
	         //Retrieve by column name
	         long id  = resultSet.getInt("PID");
	     //Display values
	         System.out.print("PID: " + id);
	     }
	     System.out.println();
	 } //end printResSet()
}
