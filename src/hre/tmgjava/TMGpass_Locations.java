package hre.tmgjava;

/******************************************************************************
 * Uses library com.linuxense.javadbf
 * Java library for reading and writing Xbase (dBase/DBF) files
 * https://github.com/albfernandez/javadbf
 * albfernandez/javadbf is licensed under the
 * GNU Lesser General Public License v3.0
 * Written by Alberto Fern�ndez
 **********************************************************************************************
 * Process Location tables in HRE
 * **********************************************************************************************
 * v0.00.0028 2023-02-26 - Separate pass for Location processing(N. Tolleshaug)
 * 			  2023-02-27 - Location processing in pass 2 - event in pass 3(N. Tolleshaug)
 * v0.01.0029 2023-05-01 - Implemented v22a (N. Tolleshaug)
 * v0.03.0030 2023-08-16 - Test for length in line 303 and 309 changed to != (N. Tolleshaug)
 *			  2023-08-29 - Import year < 1000 in start/end year (N. Tolleshaug)
 * v0.03.0031 2023-08-16 - Corrected class name in printout (N. Tolleshaug)
 * v0.04.0032 2026-01-16 - Log ccatch blocks (D Ferguson)
 *********************************************************************************************/

import java.sql.ResultSet;
import java.sql.SQLException;

import hre.bila.HB0711Logging;
import hre.gui.HGlobal;
/**
 * class TMGpass_Locations
 * Convert location data from TMG to HRE
 * @author NTo - Nils Tolleshaug
 * @since 2020-03-05
 * @see document
 */
class TMGpass_Locations {
	HREdatabaseHandler pointHREbase;
	TMGpass_Support pointSupportPass;
	TMGHREconverter tmgHreConverter;

	long proOffset = 1000000000000000L;
	long null_RPID  = 1999999999999999L;

	private static TMGtableData  tmgPtable = null;
	private static TMGtableData  tmgPDtable = null;
	private static TMGtableData  tmgPPTtable = null;
	private static TMGtableData  tmgPPVtable = null;

	ResultSet tableT170;
	ResultSet tableT551;
	ResultSet tableT552;
	ResultSet tableT553;


	int missingNrOfG_PID = 0;
	int rowsInT402 = 0;
	int eventType = 0;
	int personNr = 0;

	private String eventName;

	private long locationTablePID = proOffset + 1;

	// Set the number of lines printed
	int maxNrPrint = 20;


/**
 * Constructor 	TMGPassEvent(TMGDatabaseHREhandler pointHREbase)
 * @param pointHREbase
 */
	public TMGpass_Locations(HREdatabaseHandler pointHREbase) {
		this.pointHREbase = pointHREbase;
		tmgPtable = TMGglobal.tmg_P_table;
		tmgPDtable = TMGglobal.tmg_PD_table;
		tmgPPTtable = TMGglobal.tmg_PPT_table;
		tmgPPVtable = TMGglobal.tmg_PPV_table;
		tableT170 = TMGglobal.T170;
	}

/**
 * addEventsToHRE(TMGHREconverter tmgHreConverter)
 * @param tmgHreConverter
 */
	public void addLocationsToHRE(TMGHREconverter tmgHreConverter) throws HCException {
		this.pointSupportPass = tmgHreConverter.pointSupportPass;
		this.tmgHreConverter = tmgHreConverter;
		int currentRow = 0;
		int placeNr;
		String[] placeData;
		long locationNameElementPID = proOffset;
		int nrOftmgPRows = tmgPtable.getNrOfRows();

		if (TMGglobal.DEBUG)
			System.out.println(" tmg _P.dbf Table size: " + nrOftmgPRows + " rows");

		if (TMGglobal.DUMP) System.out.println("\nTest Event processing initiated");

		int progress;
		tmgHreConverter.setStatusProgress(0);

/**
 *  Reset reloaded ResultSet HRE tables
 */
		tableT551 = TMGglobal.T551;
		tableT552 = TMGglobal.T552;
		tableT553 = TMGglobal.T553;
		tableT170 = TMGglobal.T170;

/**
 * Start convert Event
 */
		for (int indexP_PID = 0; indexP_PID < nrOftmgPRows; indexP_PID++) {
			currentRow = indexP_PID + 1;

			int styleId = 0;

			// Report progress in %
			progress = (int)Math.round(((double)currentRow / (double)nrOftmgPRows) * 100);
			tmgHreConverter.setStatusProgress(progress);

			if (TMGglobal.DEBUG)
				System.out.println(" Line: " + indexP_PID + "  Progress %: " + progress);

			if (tmgPtable.getValueInt(indexP_PID,"RECNO") > 0 ) {

			// DATASET CHECK
				if(TMGglobal.dataSetID == tmgPtable.getValueInt(indexP_PID,"DSID"))	{

					placeNr = tmgPtable.getValueInt(indexP_PID,"RECNO");
					locationTablePID = placeNr + proOffset;

				// Update counter of Name styles
					if (placeNr != 0) {
						styleId = tmgPtable.findValueInt(placeNr,"STYLEID");
						pointSupportPass.addUsedStyle(styleId);
					} else System.out.println(" WARNING - addLocationsToHRE" +
								"- STYLEID' not found for placeNr: " + placeNr);

					if (TMGglobal.DEBUG)
						System.out.println("** TMG name_P.dbf - location: " + placeNr
								+ " / RECNO: " + tmgPtable.getValueInt(indexP_PID,"RECNO")
							+ " / STYLEID: " + styleId);

				// Find Place data
						placeData = findEventPlace(indexP_PID, placeNr);

				// ADD LOCATIONS
						addToT551_LOCATIONS(indexP_PID, locationTablePID, placeNr, tableT551);

				// ADD LOCATION NAMES
						addToT552_LOCATION_NAMES(indexP_PID,locationTablePID, placeNr, tableT552);

				// ADD T553 Name element for location
						for (int i = 0; i < placeData.length ; i++) {
							if (placeData[i].length() > 0) {
								locationNameElementPID++;
								int encodingType = i;
								addToT553_LOCATION_NAME_ELEMENTS(locationTablePID,
															locationNameElementPID,
															encodingType,
															tableT553,
															placeData[i]);
						}
					}

				} else System.out.println(" ..P.dbf index: " + indexP_PID + " DSID not processed: "
						+ tmgPtable.getValueInt(indexP_PID,"DSID"));
			} else {
				missingNrOfG_PID++;
				System.out.println(" Not found PID - TMG name_G.dbf - indexPID: "
						+ indexP_PID + " / " + tmgPtable.getValueInt(indexP_PID,"PER_NO"));
			}
		}

		if (TMGglobal.DUMP) System.out.println("Test Event processing ended!\n");

	}

/**
 * findEventPlace(int indexG_PID)
 * @param indexG_PID
 * @return
 * @throws HCException
 */
	private String[] findEventPlace(int indexP_PID, int placeNr) throws HCException {

		int placeNrInx = 0;
		int placePartInx = 0;
		int vectorSize = 0;
		int placePartTypeInx = 0;
		String placeName;
		String placePartType = "-";
		String printReport = "";

		String[] placeData = new String[16];
		for (int i = 0; i < placeData.length ; i++ ) placeData[i] = "";

/**
 * RECNO value 0 and 1 is not in tmgPPVtable
 * 0 - Person Source
 * 1 - Event memo only ???
 */
		if (placeNr > 1) {
			vectorSize = tmgPPVtable.getVectorSize(placeNr);
			if (vectorSize < 1)
				System.out.println(" TMGpass_Location - PPV Place vectorSize < 1 : " + placeNr + "/" + vectorSize);
			printReport =  "" + (indexP_PID+1) + " Pers: " + personNr + " - " + eventName + " at ";
			for (int i = 0; i < vectorSize; i++ ) {
				placeNrInx = tmgPPVtable.findVectorInt(placeNr, i,"UID");
				placePartInx = tmgPPVtable.findVectorInt(placeNr, i,"ID");
				placePartTypeInx = tmgPPVtable.findVectorInt(placeNr, i,"TYPE");
				placeName = tmgPDtable.findValueString(placeNrInx,"VALUE");
				placePartType = tmgPPTtable.findValueString(placePartInx,"SHORTVALUE");

				placeData[placePartTypeInx-1] = placeName;

				printReport = printReport + "(" + placePartTypeInx + ") " + placePartType + " : "
						+ placeData[placePartTypeInx-1] + " / ";
			}

			if (TMGglobal.DUMP)
				if (indexP_PID < maxNrPrint)
					System.out.println(printReport);
		} else if (TMGglobal.DEBUG) System.out.println("PPV Place nr < 1 : " + placeNr + "" + vectorSize);
		return placeData;
	}



/**
 * T551_LOCATIONS
 * PID CL_COMMIT_RPID IS_SYSTEM HAS_CITATIONS HAS_LINKS VISIBLE_ID SUB_TYPE_CLASS
 * SUB_TYPE_RPID SURETY BEST_NAME_RPID BEST_IMAGE_RPID START_HDATE_RPID END_HDATE_RPID
 */

/**
 * addToT551_LOCATIONS(long locationPID, ResultSet hreTable)
 * @param locationTablePID
 * @param hreTable
 * @throws HCException
 */
	public void addToT551_LOCATIONS(int indexP_PID, long locationTablePID, int tmgPlaceNr, ResultSet hreTable) throws HCException {

		if (TMGglobal.DEBUG)
			System.out.println("** addTo T552_T551_LOCATIONS PID: " + locationTablePID);

		try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();

		// Update new row in H2 database
			hreTable.updateLong("PID", locationTablePID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateBoolean("HAS_CITATIONS", false);
			hreTable.updateLong("VISIBLE_ID", tmgPlaceNr);
			hreTable.updateLong("ENTITY_TYPE_RPID", null_RPID);
			hreTable.updateString("SURETY", "3");
			hreTable.updateLong("BEST_NAME_RPID", locationTablePID);
			hreTable.updateLong("BEST_IMAGE_RPID", null_RPID);

			//Insert row
			hreTable.insertRow();

		} catch (SQLException sqle) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in TMGpassLocations SQL exception updating T551_LOCATIONS: " + sqle.getMessage());
				HB0711Logging.printStackTraceToFile(sqle);
			}
			throw new HCException("TMGPass_LOCATIONS - addToT551_LOCATIONS - error: " + sqle.getMessage());
		}
	}

/**
 * addToT552_LOCATION_NAMES(int rowPID,ResultSet hreTable)
 * @param rowPID
 * @param hreTable
 * @throws HCException
 */
	public void addToT552_LOCATION_NAMES(int indexP_PID, long locationNamePID, int placeNr, ResultSet hreTable) throws HCException {
		int styleId = 0;
		String tmgDate;
		if (TMGglobal.DEBUG)
			System.out.println("** addTo T552_LOCATION_NAMES PID: " + locationNamePID);

	// Update Style ID to find NAME_STYLE_RPID
		if (placeNr != 0) styleId = tmgPtable.findValueInt(placeNr,"STYLEID");
		else System.out.println(" WARNING - addToT552_LOCATION_NAMES" +
				"- STYLEID' not found for placeNr: " + placeNr);

		try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();

		// Update new row in H2 database
			hreTable.updateLong("PID", locationNamePID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateBoolean("HAS_CITATIONS", false);
			hreTable.updateLong("OWNER_RPID", locationNamePID);
		// Set name style RPID
			hreTable.updateLong("NAME_STYLE_RPID",
					pointSupportPass.getNameStylePID(styleId));

		// Processing HDATE
			String startYear = tmgPtable.getValueString(indexP_PID,"STARTYEAR");

			if (TMGglobal.DEBUG)
				System.out.println(" Start Year: /" + startYear + "/" + startYear.length());

			if (HREhdate.isInteger(startYear)) {
				if (startYear.length() == 0)
					tmgDate = "1" + "0000" + "0000030000000000";
				else tmgDate = "1" + HREhdate.prepare4digit(Integer.parseInt(startYear)) + "0000030000000000";
			} else tmgDate = "1" + "0000" + "0000030000000000";

			hreTable.updateLong("START_HDATE_RPID", HREhdate.addToT170_22a_HDATES(tableT170, tmgDate));

			String endYear = tmgPtable.getValueString(indexP_PID,"ENDYEAR");
			if (HREhdate.isInteger(endYear)) {
				//if (endYear.length() != 4 || endYear.length() == 1 || endYear.length() == 0)
				if (endYear.length() == 0)
					tmgDate = "1" + "0000" + "0000030000000000";
				else tmgDate = "1" + HREhdate.prepare4digit(Integer.parseInt(endYear)) + "0000030000000000";
			} else tmgDate = "1" + "0000" + "0000030000000000";

			hreTable.updateLong("END_HDATE_RPID", HREhdate.addToT170_22a_HDATES(tableT170, tmgDate));

			hreTable.updateLong("THEME_RPID", null_RPID);
			hreTable.updateLong("MEMO_RPID", null_RPID);

		// Processing memo to T167_MEMO_SET
			String comment = HREmemo.returnStringContent(tmgPtable.getValueString(indexP_PID,"COMMENT"));
			if (comment.length() == 0) hreTable.updateLong("MEMO_RPID", null_RPID);
			else hreTable.updateLong("MEMO_RPID",
					tmgHreConverter.pointHREmemo.addToT167_22c_MEMO(comment));
			hreTable.updateString("SURETY", "3"); // Need update 12.12.2024 ******************
		//Insert row
			hreTable.insertRow();

		} catch (SQLException sqle) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in TMGpassLocations SQL exception updating T552_LOCATION_NAMES: "
										+ sqle.getMessage());
				HB0711Logging.printStackTraceToFile(sqle);
			}
			throw new HCException("TMGPass_LOCATIONS - addTo table T552_LOCATION_NAMES - error: " + sqle.getMessage());
		}
	}

/**
 * addToT553_LOCATION_NAME
 * @param rowPID
 * @param hreTable
 * @throws HCException
 * H2 metadata for: T403_PERSON_NAME_ELEMENTS
1 - PID Type: BIGINT  Presision: 19
2 - CL_COMMIT_RPID Type: BIGINT  Presision: 19
3 - IS_SYSTEM Type: BOOLEAN  Presision: 1
4 - OWNER_RPID Type: BIGINT  Presision: 19
5 - LIST_ENCODING_TYPE Type: BIGINT  Presision: 19
6 - LANG_CODE Type: VARCHAR  Presision: 5
7 - SHORT_NAME_DATA Type: VARCHAR  Presision: 300
8 - LONG_NAME_DATA Type: CLOB  Presision: 30000
 */
	public void addToT553_LOCATION_NAME_ELEMENTS(long ownerPID,
												 long primaryPID,
											     int encodingType,
											     ResultSet hreTable,
											     String nameElement)
			throws HCException {
		if (TMGglobal.DEBUG)
			System.out.println("** addTo T553_LOCATION_NAME_ELEMENTS PID: " + primaryPID);
		try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();
		// -----------------
			hreTable.updateLong("PID", primaryPID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateLong("OWNER_RPID", ownerPID);
			hreTable.updateString("LANG_CODE", "");
			hreTable.updateString("ELEMNT_CODE", pointSupportPass.getPlaceStyleCodes(encodingType));
			hreTable.updateString("NAME_DATA", nameElement);

		//Insert row
			hreTable.insertRow();

		} catch (SQLException sqle) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in TMGpassLocations SQL exception updating T553_LOCATION_NAME_ELEMENTS: "
									+ sqle.getMessage());
				HB0711Logging.printStackTraceToFile(sqle);
			}
			throw new HCException("TMGPass_LOCATIONS - addTo table T553_LOCATION_NAME_ELEMENTS - error: "
					+ sqle.getMessage());
		}
	}

/**
 * String getReminderText(String langText, String reminder)
 * @param langText
 * @param reminder
 */
	public String getReminderText(String langText, String reminder) {
		String text = ""; String langCode = null;
		String[] reminderText = reminder.split("L=");
		for (int i = 1; i < reminderText.length; i++) {
			langCode = getLangCode(reminderText[i].substring(0,reminderText[i].indexOf(']')));
			//System.out.println("Line " + i + " Code: " + langCode + " Text: " + reminderText[i]);
			if (langCode.equals(langText)) {
				text = reminderText[i].substring(reminderText[i].indexOf(']') + 1);
				text = text.replace('[', ' ').trim();
				//System.out.println(" Code = " + langCode + " / Reminder: " + text + "\n");
				return text;
			}
		}
		return  "No reminder txt!";
	}

/**
 * getLangCode(String language)
 * @param language
 * @return
*/
	private String getLangCode(String language) {
		String code = "--";
		if (language.equals("AFRIKAANS")) code = "af-ZA";
		if (language.equals("ENGLISH")) code = "en-US";
		if (language.equals("ENGLISHUK")) code = "en-GB";
		if (language.equals("DANISH")) code = "da-DK";
		if (language.equals("DUTCH")) code = "nl-NL";
		if (language.equals("FRENCH")) code = "fr-FR";
		if (language.equals("GERMAN")) code = "de-DE";
		if (language.equals("ITALIAN")) code = "it-IT";
		if (language.equals("NORWEGIAN")) code = "no-NB";
		if (language.equals("NORWEGIA2")) code = "no-NN";
		return code;
	}
} // End class
