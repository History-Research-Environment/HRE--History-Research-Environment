package hre.tmgjava;
/******************************************************************************
 * Uses library com.linuxense.javadbf
 * Java library for reading and writing Xbase (dBase/DBF) files
 * https://github.com/albfernandez/javadbf
 * albfernandez/javadbf is licensed under the
 * GNU Lesser General Public License v3.0
 * Written by Alberto Fern�ndez
 * *******************************************************************************
 * Process Event tables in HRE
 * v0.00.0018 2020-04-25 - First version (N. Tolleshaug)
 * v0.00.0021 2020-04-30 - Added update of temp fields in T404
 * 						 - Born and Death Place added (N. Tolleshaug)
 * v0.00.0021 2020-05-01 - Implemented update of T404_BIO_NAMES (N. Tolleshaug)
 * v0.00.0022 2020-05-27 - Preliminary updated for database version 18b
 * v0.00.0022 2020-06-05 - databaseVersion = "v18c 2020-06-01"
 * 			  2020-06-30 - databaseVersion = "v19a 2020-06-21"
 * 			  2020-07-10 - Handles both event nr x and 1000 + x ???
 * 			  2020-07-30 - Updated for DDL v20a 2020-07-26
 * v0.00.0025 2020-10-25 - Birth and death place to HRE event tables (N. Tolleshaug)
 * 			  2020-11-15 - Removed DDL19a (N. Tolleshaug)
 * 			  2020-12-05 - Using sort data from tmg if edate = 0 (N. Tolleshaug)
 * 			  2020-12-29 - Added new columns to LIFE_FORMS and EVENT (N. Tolleshaug)
 * 			  2020-12-29 - Changed to T552_LOCATION_NAMES for placedata (N. Tolleshaug)
 * v0.00.0026 2021-05-20 - Corrected pass 2 events (N. Tolleshaug)
 * 			  2021-07-09 - Removed "T552_LOCATION_NAMES","LOCATION_PLACE_NAME","VARCHAR(300)"
 * v0.00.0027 2021-11-03 - Corrected PID error in T552_LOCATION_NAMES (N. Tolleshaug)
 * 			  2022-01-27 - Fix for error in TMG Sample_UK - table ..G.dbf (N. Tolleshaug)
 * 			  2022-02-27 - Implemented handling event tags, witness and roles(N. Tolleshaug)
 * v0.00.0028 2022-12-03 - Incomplete mod. for EVENTNUM to adopt to Sample-UK(N. Tolleshaug)
 * 			  2023-02-27 - Location processing in pass 2 - event in pass 3(N. Tolleshaug)
 * 			  2023-04-08 - Changed from ge-GE to de-DE for german language (N. Tolleshaug)
 * v0.01.0029 2023-05-01 - Implemented v22a (N. Tolleshaug)
 * v0.03.0030 2023-06-21 - Corrected primary setting associate v22a (N. Tolleshaug)
 * 			  2023-06-26 - Primary setting associate from G.PER1 and G.PER2 (N. Tolleshaug)
 * 			  2023-07-06 - Removed hreTable.updateLong("EVNT_PERS_RPID".... (N. Tolleshaug)
 * 			  2023-07-10 - Removed LAST_PARTNER / TMG SPOULAST processing (N. Tolleshaug)
 * 			  2023-08-12 - Updated for Sample UK processing with enum < 1000 (N. Tolleshaug)
 * 			  2023-08-13 - Test for numeric Role Number due to database error in
 * 						   Leininger-2022 project and whar2015 2023-08-13 11-57-42
 * 			  2023-08-15 - Test for sort date when TMG date missing (N. Tolleshaug)
 * 			  2023-08-16 - Test for TMG sortdate = 0  (N. Tolleshaug)
 * 			  2023-09-21 - Updated T460 EVNT_TYPE processing in seed  (N. Tolleshaug)
 * v0.03.0031 2023-10-20 - Updated for v22b database
 * 			  2023-11-02 - Updated for extraction of partner roles from E.dbf
 * 			  2023-11-11 - Updated for etype < 1000 in new TMG project
 * 			  2024-01-26 - Set EVNT_KEY_ASSOC_MIN=2 for divorceGroup (D Ferguson)
 * 			  2024-02-15 - Exclude only self assocs (N. Tolleshaug)
 * 			  2024-02-15 - Fix 31.14: T460 IS_SYSTEM = FALSE for user-added events (D Ferguson)
 * 			  2024-02-15 - Added message when error in E.dbf (N. Tolleshaug)
 * 			  2024-03-01 - Added error in E.dbf counting ex. "princ"(N. Tolleshaug)
 * 			  2024-03-03 - If no ENGLISHUK role translation copy the ENGLISH rolename (N. Tolleshaug)
 * 			  2024-03-10 - Rewrite of event tag processing for T460 (N. Tolleshaug)
 * 			  2024-03-11 - Updated for import of en-UK orinated projects (N. Tolleshaug)
 * 			  2024-03-11 - Fix 31.02: etypenr for user def events > 2000 (N. Tolleshaug)
 * 			  2024-03-13 - Fix 31.02: reinstalled Ferguson code for event numbers (N. Tolleshaug)
 * 			  2024-03-14 - Handle incomplete PROPERTIES language translations  (N. Tolleshaug)
 * 			  2024-04-26 - Dropped to exclude associates when processing E.dbf  (N. Tolleshaug)
 *  		  2024-08-06 - Added alterColumnInTable("T404_PARTNER","IMP_TMG","BOOLEAN");
 * 			  2024-08-06 - Added alterColumnInTable("T450_EVNT","IMP_TMG","BOOLEAN");
 * 			  2024-08-11 - Updated processing E.dbf partner roles (N. Tolleshaug)
 * 		  	  2024-09-04 - Removed alterColumnInTable("T404_PARTNER","IMP_TMG","BOOLEAN");
 *  		  2024-11-11 - Included PER1 == 0 and PER2 != as primary assoc (N. Tolleshaug)
 *   		  2024-11-23 - Added PER1 == 0 and PER2 != 0 role prosessing (N. Tolleshaug)
 * v0.03.0032 2025-01-30 - Added import of event reminder text (N. Tolleshaug)
 * 			  2025-01-31 - Removed console print messages (N. Tolleshaug)
 * 			  2025-02-11 - Added code to cross ref T450 PID with RECNO (N. Tolleshaug)
 * 			  2025-06-29 - Added TMG sentence addToT168_SENTENCE_SET (N. Tolleshaug)
 * 			  2025-07-01 - Added processing of [C= ,0,110] data for roles (N. Tolleshaug)
 * 			  2025-07-07 - Upated splitting of sentencess for roles (D Ferguson)
 * 			  2025-08-02 - Extract Primary roles; remove adding 'Subject' role (D Ferguson)
 * **************************************************************************************/

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.linuxense.javadbf.DBFRow;

import hre.bila.HBException;
/**
 * TMGpass_V22a_Events
 * Convert event data from TMG to HRE
 * @author NTo - Nils Tolleshaug
 * @since 2020-03-05
 * @see document
 */
class TMGpass_Events  {
	HREdatabaseHandler pointHREbase;
	TMGHREconverter tmgHreConverter;

	long proOffset = 1000000000000000L;
	long null_RPID  = 1999999999999999L;

	// Control of trace date for event defs table gnereation
	boolean trace = TMGglobal.DEBUG;

	private static TMGtableData  tmgEtable = null;
	private static TMGtableData  tmgGtable = null;
	private static TMGtableData  tmgTtable = null;
	private static String eventDefnTable = "T460_EVNT_DEFN";
	private static String eventRoleTable = "T461_EVNT_ROLE";
	private static int marrGroup = 6, divorceGroup = 7, birthGroup = 4;

	ResultSet tableT168;
	ResultSet tableT170;
	ResultSet tableT404;
	ResultSet tableT405;
	ResultSet tableT450;
	ResultSet tableT451;
	ResultSet tableT460;
	ResultSet tableT461;
	ResultSet hreTable;

	int missingNrOfG_PID = 0;
	int rowsInT402 = 0;
	int eventType = 0;
	int personNr = 0;

	//Statistics
	int noMemo = 0;
	int smallMemo = 0;
	int largeMemo = 0;
	int nrOfEvents = 0;

/**
 * Role parameters init roleSex, minRoleAge, maxRoleAge, default Primary roles
 */
	static final String roleSex = " ";
	static final int minRoleAge = 0;
	static final int maxRoleAge = 110;
	int defPrimary1 = 1;
	int defPrimary2 = 1;

/**
 * Map for retrieving event admin group.
 */
	private HashMap<Integer,Integer> eventAdminTMGmap = new HashMap<>();

/**
 * Record born place and death place PID
 */
	private HashMap<Long,Long> personBornPlaceIndex = new HashMap<>();
	private HashMap<Long,Long> personDeathPlaceIndex = new HashMap<>();

/**
 * Hashmap for storing partner roles extracted from E.dbf witness, The map uses the gnum primary index
 * and is the two dimensional array holding pri_role and sec_role for partner relation
 */
	HashMap<Long,int[]> roleEvents = new HashMap<>();
/**
 * HashMap for birth events
 */
	HashMap<Long,Long> birthEvents = new HashMap<>();

	private long locationTablePID = proOffset + 1;
	private long eventDefnPID = proOffset;
	private long eventRolePID = proOffset;
	private long eventSentencPID = proOffset;

	public int getAdminGroup(int eventTypeNr) {
		if (eventAdminTMGmap.containsKey(eventTypeNr))
		return eventAdminTMGmap.get(eventTypeNr);
		System.out.println(" Get admin error: " + eventTypeNr);
		return 99;
	}

/**
 * Constructor 	TMGPassEvent(TMGDatabaseHREhandler pointHREbase)
 * @param pointHREbase
 */
	public TMGpass_Events(HREdatabaseHandler pointHREbase) {
		this.pointHREbase = pointHREbase;
		tmgEtable = TMGglobal.tmg_E_table;
		tmgGtable = TMGglobal.tmg_G_table;
		tmgTtable = TMGglobal.tmg_T_table;
		tableT168 = TMGglobal.T168;
		try {
			eventDefnPID = pointHREbase.lastRowPID(eventDefnTable);
			eventRolePID = pointHREbase.lastRowPID(eventRoleTable);
			if (TMGglobal.DEBUG) System.out.println(" Last PID in Table: " + eventDefnTable + " -  " + eventDefnPID);
			if (TMGglobal.DEBUG) System.out.println(" Last PID in Table: " + eventRoleTable + " -  " + eventRolePID);
		} catch (HBException hbe) {
			System.out.println("Table " + eventDefnTable + "/" + eventRoleTable + " empty");
			eventDefnPID  = proOffset;
			eventRolePID = proOffset;
			hbe.printStackTrace();
		}
	}

/**
 * addEventsToHRE(TMGHREconverter tmgHreConverter)
 * @param tmgHreConverter
 */
	public void addEventTable(TMGHREconverter tmgHreConverter) throws HCException {
		this.tmgHreConverter = tmgHreConverter;
		int currentRow = 0;
		int primaryIndex = 0;
		int tmgPlaceNr;
		int nrOftmgGRows = tmgGtable.getNrOfRows();

		if (TMGglobal.DEBUG)
			System.out.println(" tmg _G.dbf Table size: " + nrOftmgGRows + " rows");
		if (TMGglobal.DUMP) System.out.println("\nTest Event processing initiated");

		int progress;
		tmgHreConverter.setStatusProgress(0);

/**
 *  Reset reloaded ResultSet HRE tables
 */
		tableT170 = TMGglobal.T170;
		tableT404 = TMGglobal.T404;
		tableT450 = TMGglobal.T450;

/**
 * Start convert Event
 */
		for (int index_G_Table = 0; index_G_Table < nrOftmgGRows; index_G_Table++) {
			currentRow = index_G_Table + 1;

			int styleId = 0;

			// Report progress in %
			progress = (int)Math.round(((double)currentRow / (double)nrOftmgGRows) * 100);
			tmgHreConverter.setStatusProgress(progress);

			if (TMGglobal.DEBUG)
				System.out.println(" Line: " + index_G_Table + "  Progress %: " + progress);

			if (tmgGtable.getValueInt(index_G_Table,"RECNO") > 0 ) {

			// DATASET CHECK
				if (TMGglobal.dataSetID == tmgGtable.getValueInt(index_G_Table,"DSID"))	{

				// Find Place PID
					tmgPlaceNr = tmgGtable.getValueInt(index_G_Table,"PLACENUM");
					if (tmgPlaceNr > 1)
						locationTablePID = tmgPlaceNr + proOffset;
					else locationTablePID = null_RPID;

				// Updated for en-UK initiated projects with event type < 1000
					int etypeNumber = tmgGtable.getValueInt(index_G_Table,"ETYPE");
					int origtype = tmgTtable.findValueInt(etypeNumber,"ORIGETYPE");

				// Set etype to 2XXX for user-added events - Fix 31.02
					if (origtype != 0) etypeNumber = origtype + 1000;
					else etypeNumber = etypeNumber + 1000;

				// Record the place RPID to set in T401
					long personRPID;
					if (etypeNumber == 1002) { // Born event
						personRPID = tmgGtable.getValueInt(index_G_Table,"PER1") + proOffset;
						personBornPlaceIndex.put(personRPID, locationTablePID);
					}

					if (etypeNumber == 1003) { // Death event
						personRPID = tmgGtable.getValueInt(index_G_Table,"PER1") + proOffset;
						personDeathPlaceIndex.put(personRPID, locationTablePID);
					}

					if (TMGglobal.DEBUG)
							System.out.println("** TMG name_P.dbf - location: " + tmgPlaceNr
									+ " / RECNO: " + tmgGtable.getValueInt(index_G_Table,"RECNO")
								+ " / STYLEID: " + styleId);

					// ADD EVENTS with new LOCATION
						addToT450_EVNT(index_G_Table, etypeNumber, locationTablePID, tableT450);

					// Set up partner table
						int eventTypeNr = tmgGtable.getValueInt(index_G_Table, "ETYPE");
						int adminGroup = getAdminGroup(eventTypeNr);

				   // Only record marriages and divorces
						if (adminGroup == marrGroup || adminGroup == divorceGroup) {
							primaryIndex++;
							addToT404_PARTNER(primaryIndex, index_G_Table, tableT404);
						}

				} else System.out.println("Dataset DSID not processed tmgGtable: "
						+ tmgGtable.getValueInt(index_G_Table,"DSID"));
			} else {
				missingNrOfG_PID++;
				System.out.println(" Not found PID - TMG name_G.dbf - indexPID: "
						+ index_G_Table + " / " + tmgGtable.getValueInt(index_G_Table,"PER_NO"));
			}
		}

	// Update place born/death RPID in T401
		long personPID = 0;
		try {
			ResultSet persont401 = tmgHreConverter.hreLoader.tableLoader("T401_PERS");
			if (TMGglobal.DEBUG) {
				persont401.last();
				System.out.println(" Number of persons to update: " + persont401.getRow());
			}
			persont401.beforeFirst();
			while (persont401.next()) {
				personPID = persont401.getLong("PID");
				if (personBornPlaceIndex.containsKey(personPID))
					persont401.updateLong("BIRTH_PLACE_RPID", personBornPlaceIndex.get(personPID));
				if (personDeathPlaceIndex.containsKey(personPID))
					persont401.updateLong("DEATH_PLACE_RPID", personDeathPlaceIndex.get(personPID));
				persont401.updateRow();
			}
			persont401 = null;

		} catch (SQLException sqle) {
			System.out.println(" Error update born/death person: " + personPID);
			sqle.printStackTrace();
		}
		if (TMGglobal.DUMP) System.out.println("Test Event processing ended!\n");
	}	// End AddEventTable

/**
 * addToT450_EVNT(int rowPID, int etypeNumber, long locationTablePID, ResultSet hreTable)
 * @param rowPID
 * @param etypeNumber
 * @param locationTablePID
 * @param hreTable
 * @throws HCException
 */
	public void addToT450_EVNT(int rowPID, int etypeNumber, long locationTablePID, ResultSet hreTable) throws HCException {
		String tmgDate, tmgSort, roleNumber = "";
		long sortDate = null_RPID, startDate = null_RPID, indexPID;
		int recNr;
		if (TMGglobal.DEBUG)
			System.out.println("** addTo T450_EVENTS row: " + rowPID);

		recNr = tmgGtable.getValueInt(rowPID,"RECNO");
		indexPID = proOffset + recNr;

		// index for event PID
		tmgHreConverter.pointSourcePass.eventIndexPID.put(recNr, indexPID );

		try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();
		// Update new row in H2 database
			hreTable.updateLong("PID", indexPID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateBoolean("HAS_CITATIONS", false);
			hreTable.updateBoolean("OWNS_EVENTS", false);
			hreTable.updateInt("VISIBLE_ID", tmgGtable.getValueInt(rowPID,"REF_ID"));
			hreTable.updateString("SURETY", tmgGtable.getValueString(rowPID,"ENSURE"));
			hreTable.updateInt("EVNT_TYPE", etypeNumber);
			hreTable.updateLong("BEST_IMAGE_RPID", null_RPID);
			hreTable.updateLong("EVNT_OWNER_RPID", null_RPID);
			hreTable.updateLong("EVNT_LOCN_RPID", locationTablePID);

			//***** HDATE processing
			tmgDate = tmgGtable.getValueString(rowPID,"EDATE");
			tmgSort = tmgGtable.getValueString(rowPID,"SRTDATE");
			if (tmgSort.length() == 20 && !tmgSort.startsWith("0")) {
				System.out.println(" WARNING - addToT450_EVENTS - TMG srtdate used from ..G.dbf row: " + recNr
							+ " short date format: " + tmgSort + " tmgdate length: " + tmgSort.length());
				tmgSort = tmgSort + "0";
			}
			if (tmgSort.length() == 0) sortDate = null_RPID;
			else sortDate = HREhdate.addToT170_22a_HDATES(tableT170, tmgSort);

			if (tmgDate.length() == 0 || tmgDate.startsWith("1000000000"))
				startDate = null_RPID;
			else startDate = HREhdate.addToT170_22a_HDATES(tableT170, tmgDate);

			hreTable.updateLong("SORT_HDATE_RPID", sortDate);
			hreTable.updateLong("START_HDATE_RPID", startDate);
			hreTable.updateLong("END_HDATE_RPID", startDate);

			hreTable.updateLong("THEME_RPID", null_RPID);

			String efoot = HREmemo.returnStringContent(tmgGtable.getValueString(rowPID,"EFOOT"));

		// Processing memo to T167_MEMO_SET
			if (efoot.length() == 0) hreTable.updateLong("MEMO_RPID", null_RPID);
			else hreTable.updateLong("MEMO_RPID",
					tmgHreConverter.pointHREmemo.addToT167_22c_MEMO(efoot));

		// Find the role number from E.dbf table "ROLE"
			int per1 = tmgGtable.getValueInt(rowPID,"PER1");
			int per2 = tmgGtable.getValueInt(rowPID,"PER2");

		// check if PER1 == 0 then set PER2 as PRIM_ASSOC_RPID
			if (per1 != 0) {
				hreTable.updateLong("PRIM_ASSOC_RPID", proOffset + per1);
				roleNumber = tmgEtable.findVectorString(recNr, 0, "ROLE"); // 22.11.2024
				//roleNumber = tmgEtable.findValueString(recNr,"ROLE");
			} else {
				hreTable.updateLong("PRIM_ASSOC_RPID", proOffset + per2);

		// Find the per2 role on TMG E.table - 22.11.2024
				Vector<DBFRow> vectorTableE = tmgEtable.findVectorRows(recNr);
				if (vectorTableE.size() > 1) {
					//System.out.println(" Per2:  " + per2 + " size: " + vectorTableE.size());
					for (int i = 0; i < vectorTableE.size() ; i++) {
						String role = tmgEtable.findVectorString(recNr, i, "ROLE");
						int eper = tmgEtable.findVectorInt(recNr, i, "EPER");
						//int gnum = tmgEtable.findVectorInt(recNr, i, "GNUM");
						//System.out.println(" Eper : " + eper + " Gnum: " + gnum + " Role:" + role);
						if (per2 == eper) roleNumber = role;
					}
				} else roleNumber = tmgEtable.findVectorString(recNr, 0, "ROLE");
			}

			if (HREhdate.isInteger(roleNumber))
				hreTable.updateInt("PRIM_ASSOC_ROLE_NUM", Integer.parseInt(roleNumber));
			else hreTable.updateInt("PRIM_ASSOC_ROLE_NUM", 00001);

			hreTable.updateInt("PRIM_ASSOC_BASE_TYPE",0);
			hreTable.updateLong("PRIM_ASSOC_SENTENCE_RPID", null_RPID);

		// Insert row
			hreTable.insertRow();
		} catch (SQLException sqle) {
			System.out.println("TMGpass_Events - addTo table T450_EVNT - error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HCException("TMGpass_Events - addTo table T450_EVNT - error: " + sqle.getMessage());
		}
	}

/**
 * addToT404_PARTNER(int primaryIndex, ResultSet hreTable)
 * @param primaryIndex
 * @param hreTable
 * @throws HCException
 */
    protected void addToT404_PARTNER(int primaryIndex, int indexG_PID, ResultSet hreTable) throws HCException {
		try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();
		// update rows
			long indexPID = primaryIndex + proOffset;
			hreTable.updateLong("PID", indexPID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);

		// When TRUE, then this Record is the owner of some records in T735 CITN,
			hreTable.updateBoolean("HAS_CITATIONS",false);
			//hreTable.updateBoolean("IMP_TMG", true); // Mark partner as imported from TMG

		// This is the PID of a T401 record which defines one partner in the relationship
			if (tmgGtable.getValueInt(indexG_PID,"PER1") != 0)
				hreTable.updateLong("PRI_PARTNER_RPID", tmgGtable.getValueInt(indexG_PID,"PER1") + proOffset);
			else hreTable.updateLong("PRI_PARTNER_RPID", null_RPID);
		// This is the PID of a T401 record which defines another partner in the relationship
			if (tmgGtable.getValueInt(indexG_PID,"PER2") != 0)
				hreTable.updateLong("SEC_PARTNER_RPID", tmgGtable.getValueInt(indexG_PID,"PER2") + proOffset);
			else hreTable.updateLong("SEC_PARTNER_RPID", null_RPID);
			long startDate;
			String eventdate = tmgGtable.getValueString(indexG_PID,"EDATE");
			if (eventdate.length() == 0 || eventdate.startsWith("1000000000")) {
		// User TMG G table SRTDATE as START_HDATE_RPID
				String sortDate = tmgGtable.getValueString(indexG_PID,"SRTDATE");
				if (sortDate.length() != 0 || sortDate.startsWith("10000000003"))
					startDate = HREhdate.addToT170_22a_HDATES(tableT170, sortDate);
				else startDate = null_RPID;
			} else
				startDate = HREhdate.addToT170_22a_HDATES(tableT170, eventdate);

			hreTable.updateLong("START_HDATE_RPID", startDate);

		// TMG G table have only start date for event
			hreTable.updateLong("END_HDATE_RPID", null_RPID);

		// A number defining the relationship type, as per the codes defined in the ‘Data Transfer from TMG $/F Records’ section of this document
			int eventTypeNumber = tmgGtable.getValueInt(indexG_PID,"ETYPE");

		// Updated for en-UK initiated projects with event type < 1000
			int origtype = tmgTtable.findValueInt(eventTypeNumber ,"ORIGETYPE");

		// Set etype to 2XXX - Fix 31.02 - user defined match preloaded event types
			if (origtype != 0) eventTypeNumber = origtype + 1000;
			else eventTypeNumber = eventTypeNumber + 1000;
			if (eventTypeNumber < 1000) eventTypeNumber = origtype + 1000;

			hreTable.updateInt("PARTNER_TYPE", 	eventTypeNumber);

		// Record the partner event creating the partnership
			hreTable.updateLong("EVNT_RPID", proOffset + tmgGtable.getValueInt(indexG_PID,"RECNO"));

			hreTable.updateInt("PRI_ROLE",0);
			hreTable.updateInt("SEC_ROLE",0);

		// The PID of a T167_MEMO_SET that identifies an applicable Memo
			String efoot = HREmemo.returnStringContent(tmgGtable.getValueString(indexG_PID,"EFOOT"));

		// Processing memo to T167_MEMO_SET
			if (efoot.length() == 0) hreTable.updateLong("MEMO_RPID", null_RPID);
			else hreTable.updateLong("MEMO_RPID",
					tmgHreConverter.pointHREmemo.addToT167_22c_MEMO(efoot));

		// Surety in TMG also recorded for other partner relationship
			hreTable.updateString("SURETY", tmgGtable.getValueString(indexG_PID,"ENSURE"));

		// Insert row
			hreTable.insertRow();

		} catch (SQLException sqle) {
			System.out.println("TMGpass_Events - addTo table addToT404_PARTNER - error: "
					+ sqle.getMessage());
			sqle.printStackTrace();
			throw new HCException("TMGpass_Events - addTo table addToT404_PARTNER - error: "
					+ sqle.getMessage());
		}
    }

/**
 * addEventTagTables(TMGHREconverter tmgHreConverter)
 * @param tmgHreConverter
 */
	public void addEventTagTables(TMGHREconverter tmgHreConverter) throws HCException {
		this.tmgHreConverter = tmgHreConverter;
		HashMap<Integer, Object[]> roleParamMap;
		int currentRow = 0;
		int progress;
		int nrOftmgTRows = tmgTtable.getNrOfRows();
		if (TMGglobal.DUMP) System.out.println("\nEvent processing initiated");

/**
 *  Reset reloaded ResultSet HRE tables
 */
		tableT460 = TMGglobal.T460;
		tableT461 = TMGglobal.T461;

/**
 * Start convert Event
 */
		tmgHreConverter.setStatusProgress(0);
		for (int indexT_PID = 0; indexT_PID < nrOftmgTRows; indexT_PID++) {
			currentRow = indexT_PID + 1;

			// Report progress in %
			progress = (int)Math.round(((double)currentRow / (double)nrOftmgTRows) * 100);
			tmgHreConverter.setStatusProgress(progress);

			if (TMGglobal.DEBUG)
				System.out.println(" Line: " + indexT_PID + "  Progress %: " + progress);

		// DATASET CHECK
			if(TMGglobal.dataSetID == tmgTtable.getValueInt(indexT_PID,"DSID"))	{
				String tsentence = HREmemo.returnStringContent(tmgTtable.getValueString(indexT_PID,"TSENTENCE"));
				String properties = tmgTtable.getValueString(indexT_PID,"PROPERTIES").trim();
				String reminder = HREmemo.returnStringContent(tmgTtable.getValueString(indexT_PID,"REMINDERS").trim());
				String etypeName =  tmgTtable.getValueString(indexT_PID,"ETYPENAME");
				String abbrev = tmgTtable.getValueString(indexT_PID,"ABBREV");
				String pasttense =  tmgTtable.getValueString(indexT_PID,"PASTTENSE");
				boolean isLdsOnly = tmgTtable.getValueBoolean(indexT_PID,"LDSONLY");

				int adminGroup = tmgTtable.getValueInt(indexT_PID,"ADMIN");
				int eventTypeNr = tmgTtable.getValueInt(indexT_PID,"ETYPENUM");
				int origeType = tmgTtable.getValueInt(indexT_PID,"ORIGETYPE");

			// Set up cross reference HashMap for admin group
				eventAdminTMGmap.put(eventTypeNr, adminGroup);

			// Set etype to 2XXX for user-added events - Fix 31.02
				if (origeType != 0) eventTypeNr = origeType + 1000;
				else eventTypeNr = eventTypeNr + 1000;

			// extract or update event tags - first, do system-defined
				if (origeType > 0) { // Originally used code if (origeType > 0) test origeType > 1000
					//System.out.println(" Event hint: " + getReminderText("no-NB", reminder));
					if (!isLdsOnly)	updateT460_EVNT_DEFS(eventTypeNr, origeType, etypeName, reminder);
					else if (TMGglobal.DEBUG)
						System.out.println(" Event name LDS excluded: " + eventTypeNr + " / " + etypeName);
			// else do user-added (origetype = 0)
				} else  {
					String reminderText = getReminderText("en-US", reminder);
	    			if (trace)
	    				System.out.println(" Default Language: " + "en-US / "
		    				+ eventTypeNr + " / " + etypeName + " / " + abbrev + " / " + pasttense);
					addToT460_EVNT_DEFS(indexT_PID, "en-US", etypeName, eventTypeNr, abbrev, pasttense, reminderText, tableT460);
					extractLangTags(indexT_PID, eventTypeNr, properties, reminder);
				 }

			// Get role sentences
				roleParamMap = extractRoleSentence(eventTypeNr, etypeName, tsentence);
			// Extract roles and get Primary roles
				extractRoles(eventTypeNr, tsentence, roleParamMap, properties, tableT461);
			}
		}
	}

/**
 * private void extractRoles( int eventType, String roleString, HashMap<Integer,Object[]> roleParamMap, ResultSet hreTable)
 * @param eventType
 * @param roleString
 * @param roleParamMap
 * @param hreTable
 * @throws HCException
 */
	private void extractRoles( int eventType,
								String roleString,
								HashMap<Integer,Object[]> roleParamMap,
								String properties,
								ResultSet hreTable) throws HCException {
		String roleName;
		String usRolename = "";
		int roleCode;

		Object[] roleParams =  new Object[3];
		roleParams[0] = roleSex;
		roleParams[1] = minRoleAge;
		roleParams[2] = maxRoleAge;

		// Extract the Primary role settings (if any) out of the properties text
		defPrimary1 = 1;
		defPrimary2 = 1;
	    String [] tagLang = properties.split("\\r\\n");
	    for (String element : tagLang) {
	    	// Get the role #s of the Principal roles
	    	if (element.startsWith("DefPrincipal1="))
	    		defPrimary1 = Integer.valueOf(element.substring(14));
	    	if (element.startsWith("DefPrincipal2="))
	    		defPrimary2 = Integer.valueOf(element.substring(14));
	    }

		String languageTMG = tmgHreConverter.languageTMG();
		//System.out.println(" Event: " + eventType + " Map size: " + roleParamMap.size());
		boolean foundENGUK = false;
		roleString = roleString.replace("[LABELS:]"," ");
		int pos = roleString.indexOf("[:LABELS]");

		if (pos > 0 ) {
			roleString = roleString.substring(1, pos);
		    String[] roleType = roleString.split("RL=");
			for (int i = 1; i < roleType.length; i++) {
				String roleNumber = roleType[i].substring(0,5);
				foundENGUK = false;
		// Handling special case where roleNumber == "\0000" ?
				if (roleNumber.startsWith("\\")) roleCode = 0;
				else roleCode  = Integer.parseInt(roleNumber);

		// Get role param sex and age
				if (roleParamMap.containsKey(roleCode))
						roleParams = roleParamMap.get(roleCode);

/*				System.out.println(" Role setting for event: " + eventType
						+ " - Role: " + roleCode + " - " + roleParams[0] + "/" + roleParams[1] + "/" + roleParams[2]);
*/
		// Now split the role tranlation string
				String [] translate = roleType[i].split("L=");
				for (int j=1; j < translate.length; j++) {
					translate[j] = translate[j].replace('[', ' ');
					translate[j] = translate[j].trim();
					String[] role = translate[j].split("]");
					String lang_code = getLangCode(role[0]);
					if (lang_code.equals("en-GB")) foundENGUK = true;
					if (j == 1) usRolename = role[1];
					if (role[1] != null) roleName = role[1]; else roleName = " ";
					roleName = roleName.trim();
					eventRolePID++;
					addToT461_EVNT_ROLE(eventRolePID, eventType, roleCode , i, roleName, roleParams, lang_code, hreTable);
				}
		// Case if no ENGLISHUK tranlation copy the ENGLISHUS rolename
				if (!foundENGUK && languageTMG.equals("en-GB")) {
					eventRolePID++;
					addToT461_EVNT_ROLE(eventRolePID, eventType, roleCode , i, usRolename, roleParams, "en-GB", hreTable);
				}
			}
		}
	}

/**
 * private HashMap<Integer, Object[]> extractRoleSentence(int eventTypeNr, String etypeName, String roleString)
 * @param eventTypeNr
 * @param etypeName
 * @param roleString
 * @return
 * @throws HCException
 */
	private HashMap<Integer, Object[]> extractRoleSentence(int eventTypeNr, String etypeName, String roleString) throws HCException {
		HashMap<Integer, Object[]> roleParamMap = new HashMap<Integer, Object[]>();
		String[] roleParamSet;
		String roleSentenceString, language, stringLine ;
		String roleTextNumber = "", roleParam;
		String roleTMGsentence = "";
		String[] roleSentence;
		boolean printSentenceControl = false;
		int roleNumber = 0;
		int posLabel = roleString.indexOf("[:LABELS]"); // result -1 of not found
		if (roleString.length() == 0) return roleParamMap;
		if (posLabel < 0) posLabel = 0;
		roleSentenceString = roleString.substring(posLabel, roleString.length());
		roleSentenceString = roleSentenceString.replace("[:LABELS]","");
		String[] sentenceLaguages = roleSentenceString.split("\\[L=");
		if (printSentenceControl)
				System.out.println(" ROLES for EVENT nr: " + eventTypeNr + " name: " + etypeName);
		if (sentenceLaguages.length > 0)
		for (int i = 1; i < sentenceLaguages.length; i++) {
			stringLine = sentenceLaguages[i];
				if (stringLine.length() > 20) {
					roleSentence = stringLine.split("\\[R=");
					language = roleSentence[0].substring(0, roleSentence[0].length() - 1);
					if (printSentenceControl)
							System.out.println(" Language: " + i + " - " + language + " Code: " + getLangCode(language));
					for (int j = 1; j < roleSentence.length; j++) {
						roleTextNumber = " ";
						Object[] roleParamArray = new Object[3];
						if (roleSentence[j].length() > 2) {
							roleTextNumber = roleSentence[j].substring(0,5);
							if (HREhdate.isInteger(roleTextNumber)) {
								roleNumber = Integer.parseInt(roleTextNumber);
								if (printSentenceControl) System.out.print(" Role: " + roleTextNumber);
								roleTMGsentence = roleSentence[j].substring(6, roleSentence[j].length());

/* Examples: 			 [C=F,12 ,110][RP:00005] became [R:00003]'s adoptive mother <at [L2]> <in [L3]> <[D]>. <[M]>
				  		 [C= ,10 ,110]At [R:00003]'s birth, <[D]> <[L]> [W] assisted as a midwife. <[M]>
*/
								if (roleTMGsentence.startsWith("[C=")) {
									roleParamArray[0] = roleSex;
									roleParamArray[1] = minRoleAge;
									roleParamArray[2] = maxRoleAge;

									int rightBracketPos = roleTMGsentence.indexOf(']', 0);
									roleParam = roleTMGsentence.substring(3, rightBracketPos);
									roleParamSet = roleParam.split(",");
							//Test for roleParamSet[x] with only "   " space
									if (roleParamSet[1].trim().length() == 0) roleParamSet[1] = "0";
									if (roleParamSet[2].trim().length() == 0) roleParamSet[2] = "110";
									roleParamArray[0] = roleParamSet[0];
									roleParamArray[1] = Integer.parseInt(roleParamSet[1].trim());
									roleParamArray[2] = Integer.parseInt(roleParamSet[2].trim());
/*
									System.out.println(" Event: " + etypeName + " EvNr: " + eventTypeNr + " Role: " + roleNumber
															+ " RoleParamSet: /"
															+ roleParamArray[0] + ","
															+ roleParamArray[1] + ","
															+ roleParamArray[2] + "/");  */

									roleTMGsentence = roleTMGsentence.substring(rightBracketPos + 1, roleTMGsentence.length());
									roleParamMap.put(roleNumber, roleParamArray);
								}
								if (printSentenceControl) System.out.println("  Sentence: " + roleTMGsentence);

								eventSentencPID = eventSentencPID + 1;
								addToT168_SENTENCE_SET(tableT168, eventSentencPID, getLangCode(language), eventTypeNr,
										roleNumber, roleTMGsentence);
							} else System.out.println(" ** ROLE NUMBER NOT FOUND for: " + language + " Event: " + etypeName
									+ " Evnr: " + eventTypeNr + " Role sentence: [R=" + roleSentence[j]);

						} else System.out.print(" ** MISSING ROLE NUMBER for: " + language + " Event: " + etypeName
								+ " Evnr: " + eventTypeNr + " Role sentence: [R=" + roleSentence[j]);
					}
				} else System.out.print(" ** INCOMPLETE SENTENCE for Event: " + etypeName
							+ " Evnr: " + eventTypeNr + " Sentence: " + stringLine);
		}
		return roleParamMap;
	}

/**
 * extractLangTags(int indexT_PID, String properties, String reminder)
 * @param properties
 * @throws HCException
 * l_Englishuk=Birth - this is a Properties example structure (Birth tag)
	a_Englishuk=b.
	p_Englishuk=birth
	l_English=Birth
	a_English=b.
	p_English=born
	DefPrincipal1=00003
	DefPrincipal2=00003
	DefWitness=00002
	DefStyle=0
 */
    private void extractLangTags(int indexT_PID, int etypeNumber, String properties, String reminder) throws HCException {
		String langCode = " ~", tagName = " ~",abbrev = " ~", tagSentense = " ~";
		String UStagName = " ~", USabbrev = " ~", UStagSentense = " ~";
		int index = 0;
		String reminderText;
		boolean foundEngUK = false;
		boolean founddata = false;
		boolean createRecord = false;
		//System.out.println(" Reminder text: " + langCode + " - Text: " + reminderText);
	    String [] tagLang = properties.split("\\r\\n");
	    for (String element : tagLang) {
	    	// Handle the language etc data in Properties
	    	if (!element.startsWith("Def") && element.trim().length() > 0) {
		    	if (trace) System.out.println(" - " + index  + " line: " + element);
		    	index++;
		    	String[] content = element.split("=");
		    	if (content.length == 2) {
			    	if (langCode.equals("en-GB")) foundEngUK = true;
			    	if (content[0].startsWith("l_")) {
			    		if (createRecord && !langCode.equals("en-US")) {
			    			if (trace) System.out.println(" Found Language: " + langCode + " / "
				    				+ etypeNumber + " / " + tagName + " / " + abbrev + " / " + tagSentense);
				    		reminderText = getReminderText(langCode, reminder);
				    		//System.out.println(" Reminder text: " + langCode + " - Text: " + reminderText);
				    		addToT460_EVNT_DEFS(indexT_PID, langCode, tagName, etypeNumber,
				    					abbrev, tagSentense, reminderText , tableT460);
				    		createRecord = false;
			    		}
			    		tagName = " ~";
			    		founddata = true;
			    		langCode = getTagLangCode(content[0].substring(2));
			    		tagName = content[1];
			    		if (langCode.equals("en-US")) UStagName = content[1];
			    		createRecord = true;
			    	}
			    	else if (content[0].startsWith("a_")) {
			    		abbrev = " ~";
			    		founddata = true;
			    		abbrev = content[1];
			    		if (langCode.equals("en-US")) USabbrev = content[1];
			    		createRecord = true;
			    	}
			    	else if (content[0].startsWith("p_")) {
			    		tagSentense = " ~";
			    		founddata = true;
			    		tagSentense = content[1];
			    		if (langCode.equals("en-US")) UStagSentense = content[1];
			    		createRecord = true;
			    	}
		    	}
	// Handle last language in the list
	    	} else 	if (createRecord && !langCode.equals("en-US")) {
    			if (trace) System.out.println(" Found last Language: " + langCode + " / "
	    				+ etypeNumber + " / " + tagName + " / " + abbrev + " / " + tagSentense);
	    		reminderText = getReminderText(langCode, reminder);
	    		addToT460_EVNT_DEFS(indexT_PID, langCode, tagName, etypeNumber,
	    					abbrev, tagSentense, reminderText , tableT460);
	    		createRecord = false;
    		}
	    } // End for loop

	 // Final language translation	if en-GB translaion is missing use the en-US setting
	    if (!foundEngUK && founddata) {
	    	langCode = "en-GB";
	    	if (trace) System.out.println(" ** Added Fallback en-GB Language: " + langCode
    				+ " / " + etypeNumber + " / " + tagName + " / " + abbrev + " / " + tagSentense);
	    	reminderText = getReminderText(langCode, reminder);
	    	addToT460_EVNT_DEFS(indexT_PID, langCode, UStagName, etypeNumber, USabbrev, UStagSentense, reminderText , tableT460);
	    }
	    if (trace) System.out.println(" **** End T.dbf record");
    }

/**
 * String getReminderText(String langText, String reminder)
 * @param langText
 * @param reminder - example follows
 * [L=ENGLISHUK]Intended for adopted children.  Enter the child or children as Principals.
   [L=ENGLISH]Intended for adopted children.  Enter the child or children as Principals.
   [L=DUTCH]Bedoeld voor geadopteerde kinderen.  Voer het kind of de kinderen in als Principals
   [L=NORWEGIAN]Hendelsen brukes for adopterte barn. Legg inn barnet/barna som Hovedpersoner.
   [L=NORWEGIA2]Hendinga blir brukt for adopterte barn. Legg inn barnet/barna som Hovudpersonar.
   [L=GERMAN]Vorgesehen für adoptierte Kinder.
    Geben Sie das Kind oder die Kinder als Hauptpersonen ein.
 */
	public String getReminderText(String langText, String reminder) {
		String text = ""; String langCode = null;
		String[] reminderText = reminder.split("L=");
		//System.out.println(" Number of language codes: " + reminderText.length + " - "  + reminder);
		for (int i = 1; i < reminderText.length; i++) {
			langCode = getLangCode(reminderText[i].substring(0, reminderText[i].indexOf(']')));
			//System.out.println("Line " + i + " Lang code: " + langCode + " Text: " + reminderText[i]);
			if (langCode.equals(langText)) {
				text = reminderText[i].substring(reminderText[i].indexOf(']') + 1);
				text = text.replace('[', ' ').trim();
				//System.out.println(" Code = " + langCode + " / Reminder: " + text + "\n");
				return text;
			}
		}
		return  " ";
	}

/**
 *
 * @param hreTable
 * @param primaryPID
 * @param lang_code
 * @param etypeNumber
 * @param roleNumber
 * @param TMGsentence
 * @throws HCException
 * 	 T168_SENTENCE_SET format
	 PID
	 BIGINT
	 CL_COMMIT_RPID
	 BIGINT
	 IS_LONG
	 SHORT_SENT
	 LONG_SENT
	 LANG_CODE
	 EVNT_TYPE
	 EVNT_ROLE_NUM
 */

	protected void addToT168_SENTENCE_SET(ResultSet hreTable, long primaryPID, String lang_code, int etypeNumber,
											int roleNumber, String TMGsentence) throws HCException {
		try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();
		// update
			hreTable.updateLong("PID", primaryPID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateString("LANG_CODE", lang_code);
			hreTable.updateInt("EVNT_TYPE", etypeNumber);
			hreTable.updateInt("EVNT_ROLE_NUM", roleNumber);
			if (TMGsentence.length() <= 500) {
				hreTable.updateBoolean("IS_LONG", false);
				hreTable.updateString("SHORT_SENT", TMGsentence);
			} else {
				hreTable.updateBoolean("IS_LONG", true);
				hreTable.updateClob("LONG_SENT", pointHREbase.createNClob(TMGsentence));
			}

		//Insert row
			hreTable.insertRow();

		} catch (SQLException sqle) {
			System.out.println("TMGPass_events - addToT168_SENTENCE_SET - error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HCException("TMGPass_events - addToT168_SENTENCE_SET - error: " + sqle.getMessage());
		}
	}
/**
 * updateT460_EVNT_DEFS(int etypeNumber, int origeType, String etypeName)
 * @param etypeNumber
 * @param origeType
 * @param etypeName
 * @throws SQLException
 * @throws HCException
 */
	protected void updateT460_EVNT_DEFS(int etypeNumber, int origeType, String etypeName, String reminder) throws  HCException {
		boolean isActive = true;
		String eventName, language, hintText;
		ResultSet hreTable;
		String selectString = pointHREbase.setSelectSQL("*", eventDefnTable, "EVNT_TYPE = " + etypeNumber);
		hreTable = pointHREbase.requestTabledata(eventDefnTable, selectString);
		try {
			hreTable.beforeFirst();
			while (hreTable.next()) {
				hintText = " No hint!";
				if (hreTable.getString("LANG_CODE").contains("en-US")) {
					hreTable.updateString("EVNT_NAME", etypeName);
					isActive = hreTable.getBoolean("IS_ACTIVE");
				}
				hreTable.updateBoolean("IS_ACTIVE", isActive);
				eventName = hreTable.getString("EVNT_NAME").trim();
				language = hreTable.getString("LANG_CODE");

				hintText = getReminderText(language, reminder);
			// Adopt to Sample UK where event type Nr < 1000
				if (etypeNumber < 1000)
					hreTable.updateInt("EVNT_TYPE", origeType + 1000);
				else hreTable.updateInt("EVNT_TYPE", etypeNumber);
				hreTable.updateString("EVNT_HINT", hintText);
				hreTable.updateRow();

				if (TMGglobal.DEBUG)
					System.out.println(" updateT460_EVNT_DEFS: "
									+ " OrigeType: " + origeType
									+ " langCode: " + language
									+ " EventType: " + etypeNumber
									+ " EtypeName. " +  eventName
									+ " Hint: " + hintText);
			}
		} catch (SQLException sqle) {
			if (TMGglobal.DEBUG)
				System.out.println(" TMGpass_Events updateT460_EVNT_DEFS error: " + sqle.getMessage());
			throw new HCException(" TMGpass_Events updateT460_EVNT_DEFS error: " + sqle.getMessage());
		}

	}

/**
 * addToT460_EVNT_DEFS (int primaryIndex, string lang_code, string etypename, string abbrev, string pasttense, string reminder, ResultSet hreTable)
 * @param primaryIndex
 * @param lang_code
 * @param etypeName
 * @param abbrev
 * @param pasttense
 * @param reminder
 * @param hreTable
 * @throws HCException
 */
	protected void addToT460_EVNT_DEFS(int primaryIndex, String lang_code,
										String etypeName, int etypeNumber, String abbrev, String pasttense,
										String reminder, ResultSet hreTable) throws HCException {
		if (TMGglobal.DEBUG)
				System.out.println("** addTo T460_EVENT_DEFN PID: " + (primaryIndex + proOffset + 1));

	// Abbrev too long:
		if (abbrev.length() > 10) {
			System.out.println(" Long T460_EVNT_DEFN - EVNT_ABBREV: " + abbrev + " - Length: "
					+ abbrev.length() + "/10");
			abbrev = abbrev.substring(0,10);
		}
		if (reminder.length() > 2000) {
			System.out.println(" Long T460_EVNT_DEFN - EVNT_HINT - Length: "
					+ reminder.length() + "/2000");
			reminder = reminder.substring(0,2000);
		}

		try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();
		// update row
			eventDefnPID++;
			hreTable.updateLong("PID", eventDefnPID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateBoolean("IS_SYSTEM", false);		// Fix 31.14: IS_SYSTEM=FALSE for user-added event
			hreTable.updateBoolean("IS_ACTIVE", tmgTtable.getValueBoolean(primaryIndex,"ACTIVE"));
			hreTable.updateInt("EVNT_TYPE", etypeNumber);
			int eventGroup = tmgTtable.getValueInt(primaryIndex,"ADMIN");
			hreTable.updateInt("EVNT_GROUP", eventGroup);
			if (eventGroup == marrGroup || eventGroup == divorceGroup) hreTable.updateInt("EVNT_KEY_ASSOC_MIN", 2);
			else hreTable.updateInt("EVNT_KEY_ASSOC_MIN", 1);
			hreTable.updateInt("MIN_YEAR", tmgTtable.getValueInt(primaryIndex,"MINYEAR"));
			hreTable.updateInt("MAX_YEAR", tmgTtable.getValueInt(primaryIndex,"MAXYEAR"));
			hreTable.updateString("GEDCOM", tmgTtable.getValueString(primaryIndex,"GEDCOM_TAG"));
			hreTable.updateString("LANG_CODE", lang_code);
			hreTable.updateString("EVNT_NAME", etypeName);
			hreTable.updateString("EVNT_ABBREV", abbrev);
			hreTable.updateString("EVNT_PAST", pasttense);
			hreTable.updateString("EVNT_HINT", reminder);
		//Insert row
			hreTable.insertRow();

		} catch (SQLException sqle) {
			System.out.println("TMGpass_Events - addTo table T460_EVNT_DEFN - error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HCException("TMGpass_Events - addTo table T460_EVNT_DEFN - error: " + sqle.getMessage());
		}
	}

/**
 * addToT461_EVENT_ROLE_NAME
 * @param rowPID
 * @param hreTable
 * @throws HCException
 */
	protected void addToT461_EVNT_ROLE(long primaryPID,
											int eventTypeNr,
											int eventRoleNr,
											int eventRoleSequence,
											String eventRoleName,
											Object[] roleData,
											String lang_code,
											ResultSet hreTable) throws HCException {
		if (TMGglobal.DEBUG)
			System.out.println("** addTo T461_EVNT_ROLE PID: " + primaryPID);

		if (eventRoleName.length() > 40) {
			System.out.println(" Long T461_EVNT_ROLE - EVNT_ROLE_NAME - Length: "
								+ eventRoleName.length() + "/40");
			eventRoleName = eventRoleName.substring(0,40);
		}
		try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();
		// update
			hreTable.updateLong("PID", primaryPID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateBoolean("IS_SYSTEM", true);
			hreTable.updateString("LANG_CODE", lang_code);
			hreTable.updateInt("EVNT_TYPE", eventTypeNr);
			hreTable.updateInt("EVNT_ROLE_NUM", eventRoleNr);
			hreTable.updateInt("EVNT_ROLE_SEQ", eventRoleSequence); // ************Need update
			hreTable.updateString("EVNT_ROLE_NAME", eventRoleName);
			hreTable.updateString("EVNT_ROLE_SEX", (String) roleData[0]);
			hreTable.updateInt("EVNT_ROLE_MINAGE", (Integer) roleData[1]);
			hreTable.updateInt("EVNT_ROLE_MAXAGE", (Integer) roleData[2]);
			hreTable.updateLong("ROLE_SENTENCE_RPID", null_RPID);
			if (defPrimary1 == eventRoleNr || defPrimary2 == eventRoleNr)
								hreTable.updateBoolean("KEY_ASSOC", true);
			else hreTable.updateBoolean("KEY_ASSOC", false);
		//Insert row
			hreTable.insertRow();

		} catch (SQLException sqle) {
			System.out.println("Not able to update table - T461_EVNT_ROLE" + " Event role name: " + eventRoleName);
			sqle.printStackTrace();
			throw new HCException("TMGpass_Events - addTo table T461_EVNT_ROLE - error: "
					+ sqle.getMessage());
		}
	}

/**
 * addEventsAssocTable(TMGHREconverter tmgHreConverter)
 * @param tmgHreConverter
 */
	public void addEventsAssocTable(TMGHREconverter tmgHreConverter) throws HCException {
		int currentRow = 0, progress;
		long eventAssocPID = proOffset;
		//boolean noRole = false;
		int excludedWitness = 0, addedAssoc = 0, partnerEvent = 0, eventsBirths = 0, primaryRoleFound = 0, edbferror = 0;
		int per2assocs = 0;
		int nrOftmgERows = tmgEtable.getNrOfRows();

		if (TMGglobal.DEBUG)
			System.out.println(" tmg _E.dbf Table size: " + nrOftmgERows + " rows");

		if (TMGglobal.DUMP) System.out.println("\nT451_EVENT_ASSOC  processing initiated");

/**
 *  Reset reloaded ResultSet HRE tables
 */
		tableT451 = TMGglobal.T451;

		tmgHreConverter.setStatusProgress(0);
		for (int indexE_ROW = 0; indexE_ROW < nrOftmgERows; indexE_ROW++) {
			currentRow = indexE_ROW + 1;

		// Report progress in %
			progress = (int)Math.round(((double)currentRow / (double)nrOftmgERows) * 100);
			tmgHreConverter.setStatusProgress(progress);

			if (TMGglobal.DEBUG)
				System.out.println(" Line: " + indexE_ROW + "  Progress %: " + progress);

		// DATASET CHECK
			if(TMGglobal.dataSetID == tmgEtable.getValueInt(indexE_ROW,"DSID"))	{
		// Data from TMG E.dbf
				int gnumIndex = tmgEtable.getValueInt(indexE_ROW,"GNUM");
				int eper = tmgEtable.getValueInt(indexE_ROW,"EPER");
				boolean primary = tmgEtable.getValueBoolean(indexE_ROW,"PRIMARY");
				String role = HREmemo.returnStringContent(tmgEtable.getValueString(indexE_ROW,"ROLE"));

			// Fix 31.15: check role will convert to numeric; if not, fix it
			// role in E.dbf is often "Princ" or something else erroneous
				if(!StringUtils.isNumeric(role))	{
					if (TMGglobal.TRACE) System.out.println(" Role error in E.dbf row: " + indexE_ROW + " - rolevalue: " + role);
					edbferror++;
					if (role.equals("Princ")) role = "00001";
					else role = "00002";
				}

		// Data from TMG G.dbf
				int per1 = tmgGtable.findValueInt(gnumIndex,"PER1");
				int per2 = tmgGtable.findValueInt(gnumIndex,"PER2");
				int etype = tmgGtable.findValueInt(gnumIndex,"ETYPE");

		// Data from TMG T.dbf
				int admin = tmgTtable.findValueInt(etype,"ADMIN");

				String sentence = HREmemo.returnStringContent(tmgEtable.getValueString(indexE_ROW,"WSENTENCE"));
				int namerec = tmgEtable.getValueInt(indexE_ROW,"NAMEREC");
				String witmemo = HREmemo.returnStringContent(tmgEtable.getValueString(indexE_ROW,"WITMEMO"));
				int seq = tmgEtable.getValueInt(indexE_ROW,"SEQUENCE");

		// Updated for en-UK initiated projects with event type < 1000
		// Set etype to 2XXX - Fix 31.02 - user defined match preloaded event types
				int origtype = tmgTtable.findValueInt(etype,"ORIGETYPE");
				if (origtype != 0) etype = origtype + 1000;
				else etype = etype + 1000;
				if (etype < 1000) etype = origtype + 1000;

		// Extract the birth event PID for a person
				if (eper == per1 && per2 == 0)
					if (admin == birthGroup && etype == 1002) {
						birthEvents.put(eper + proOffset, gnumIndex +  proOffset);
						eventsBirths++;
						if (TMGglobal.TRACE)
							System.out.println(" Birth event person: " + eper + " / event: " + gnumIndex);
					}

		// Extract the partner roles from witness table e.dbf
				int[] roles;
				if (per1 != 0 && per2 != 0)
					if (admin == marrGroup || admin == divorceGroup) {
						if (roleEvents.containsKey(gnumIndex + proOffset)) {
							roles = roleEvents.get(gnumIndex + proOffset);
							if (eper == per1) roles[0] = Integer.parseInt(role);
							else if (eper == per2) roles[1] = Integer.parseInt(role);
							else if (primary) {
								primaryRoleFound++;
								//noRole = true;
								if (TMGglobal.TRACE) System.out.println(" Not found role: " + eper);
							}
						} else {
							roles = new int[2];
							if (eper == per1) roles[0] = Integer.parseInt(role);
							else if (eper == per2) roles[1] = Integer.parseInt(role);
							else if (primary) {
								primaryRoleFound++;
								//noRole = true;
								if (TMGglobal.TRACE) System.out.println(" Not found role index: " + eper);
							}
							roleEvents.put(gnumIndex + proOffset, roles);
							partnerEvent++;
						}
					}

					 if (eper != per1 && per1 != 0 && per2 != 0) // Test 11.11.2024 exclude only PER2 set
					 //if (eper != per1 && per2 != 0) // The master B31
						if (!(admin == marrGroup || admin == divorceGroup)) {
							if (TMGglobal.TRACE)
								System.out.println(" KEY ASSOC PER2: "
								+ " E.dbf: " + indexE_ROW
								+ " E-EPER: " + eper		// E Person
								+ " E-GNUM: " + gnumIndex // G Event nr
								+ " E-Role: " + role
								+ " E-PRIMARY: " + primary
								+ " Sequen: " + seq
								+ " - G.dbf:"
								+ " G-PER1: " + per1
								+ " G-PER2: " + per2
								+ " G-Evtyp: " + etype
								+ " G-Admin: " + admin);

						per2assocs++;
						addedAssoc++;
						eventAssocPID++;
						addToT451_EVNT_ASSOC(indexE_ROW, eventAssocPID, tableT451);
					}

			// Exclude self associate if per1 same as person and per2 not set and not primary
				if (primary || eper == per1) { // Exclude primary or principal PER1 recordings
					excludedWitness++;
					if (TMGglobal.TRACE)
						//if (admin != 6 && per1 != 0 && per2 != 0)
							System.out.println(" Excluded from T451 " + " Event: " + gnumIndex
									+ " E-EPER: " + eper
									+ " G-PER1: " + per1
									+ " G-PER2: " + per2
									+ " PRIMARY: " + primary
									+ " Event: " + etype
									+ " Admin: " + admin
									+ " Role: " + role
									+ " Sentence: " + sentence
									+ " Namerec: " + namerec
									+ " Witmemo: " + witmemo
									+ " Sequen: " + seq);
				} else {
						addedAssoc++;
						eventAssocPID++;
						addToT451_EVNT_ASSOC(indexE_ROW, eventAssocPID, tableT451);
				}
			}
		}

		//if (TMGglobal.TRACE)
			System.out.println(" *** E.dbf witness data extract statistics *****"
								+ "\n * Total E.dbf rows: " + nrOftmgERows
							    + "\n * PER2 assocs: " + per2assocs
								+ "\n * Assocs added: " + addedAssoc
								+ "\n * Assocs excluded: " + excludedWitness
								+ "\n * Detected Partner events: " + partnerEvent
								+ "\n * Detected birth events: " + eventsBirths
								+ "\n * E.dbf err report: " + edbferror
								+ "\n * Partner primary role: " + primaryRoleFound
								+ "\n *** End list of witness statistics!");
	}

/**
 * void addToT451_EVNT_ASSOC (int primaryIndex, ResultSet hreTable)
 * @param primaryIndex
 * @param hreTable
 * @throws HCException
 */
    protected void addToT451_EVNT_ASSOC(int primaryIndex, long eventAssocPID, ResultSet hreTable) throws HCException {
    	boolean principal;
    	if (TMGglobal.DEBUG)
    		System.out.println("** addTo T451_EVNT_ASSOC PID: " + (primaryIndex + proOffset + 1));

    	// Witness Sentence not used - TMG sentence format
    	String withSentence = " Witness Sentence";
    	withSentence = HREmemo.returnStringContent(tmgEtable.getValueString(primaryIndex,"WSENTENCE"));
    	//statMemoRecord(withSentence, 2000);
		if (withSentence.length() > 2000) {
			System.out.println(" Long T451_EVNT_ASSOC - LOCAL_SENTENCE Length: " + withSentence.length() + "/2000");
			withSentence = withSentence.substring(0, 2000);
		}

		try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();
		// update rows
			hreTable.updateLong("PID", eventAssocPID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateBoolean("HAS_CITATIONS", false);
			hreTable.updateInt("ASSOC_BASE_TYPE",0);
			hreTable.updateLong("ASSOC_RPID", proOffset + tmgEtable.getValueInt(primaryIndex,"EPER"));
			hreTable.updateLong("EVNT_RPID", proOffset + tmgEtable.getValueInt(primaryIndex,"GNUM"));

		// Check for primary persons in associate table
			int gnumIndex = tmgEtable.getValueInt(primaryIndex,"GNUM");
			int per1 = tmgGtable.findValueInt(gnumIndex,"PER1");
			int per2 = tmgGtable.findValueInt(gnumIndex,"PER2");
			int eper = tmgEtable.getValueInt(primaryIndex,"EPER");

			if (eper == per1 || eper == per2) principal = true; else principal = false;
			hreTable.updateBoolean("KEY_ASSOC", principal);

			String roleNumber = tmgEtable.getValueString(primaryIndex,"ROLE");

		// Test for error in Leininger-2022 project and whar2015 2023-08-13 11-57-42
			if (HREhdate.isInteger(roleNumber))
				hreTable.updateInt("ROLE_NUM", Integer.parseInt(roleNumber));
			else hreTable.updateInt("ROLE_NUM", 00001);

			hreTable.updateInt("SEQUENCE",tmgEtable.getValueInt(primaryIndex,"SEQUENCE"));

			String memo = HREmemo.returnStringContent(tmgEtable.getValueString(primaryIndex,"WITMEMO").trim());

		// Processing memo to T167_MEMO_SET
			if (memo.length() == 0) hreTable.updateLong("MEMO_RPID", null_RPID);
			else hreTable.updateLong("MEMO_RPID",
					tmgHreConverter.pointHREmemo.addToT167_22c_MEMO(memo));

			hreTable.updateLong("ASSOC_SENTENCE_RPID", null_RPID);
		//Insert row
			hreTable.insertRow();

		} catch (SQLException sqle) {
			System.out.println("TMGpass_Events - addTo table T451_EVNT_ASSOC - error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HCException("TMGpass_Events - addTo table T451_EVNT_ASSOC - error: " + sqle.getMessage());
		}
    }

 /**
  * void updatePartnerRoles()
  * @throws HCException
  */
    public void updatePartnerRoles() throws HCException {
    	int missingPartRole = 0;
    	tableT404 = tmgHreConverter.hreLoader.tableLoader("T404_PARTNER");
    	try {
			tableT404.beforeFirst();
	    	while (tableT404.next()) {
	    		long parnerEventPID = tableT404.getLong("EVNT_RPID");
	    		int[] partner = roleEvents.get(parnerEventPID);
	    		if (partner != null) {
		    		tableT404.updateInt("PRI_ROLE", partner[0]);
		    		tableT404.updateInt("SEC_ROLE", partner[1]);
		    		tableT404.updateRow();
	    		} else {
	    			missingPartRole++;
	    		}
	    	}
	    	if (TMGglobal.TRACE)
	    		if (missingPartRole > 0)System.out.println(" Missing partner roles: " + missingPartRole);
		} catch (SQLException sqle) {
			System.out.println(" TMGpass_Events Update updatePartnerRoles(): " + sqle.getMessage());
			sqle.printStackTrace();
		}
    }

/**
 * void updateBirthEvents()
 * @throws HCException
 */
    public void updateBirthEvents() throws HCException {
    	int missingBirthEvent = 0;
    	tableT405 = tmgHreConverter.hreLoader.tableLoader("T405_PARENT_RELATION");
    	try {
			tableT405.beforeFirst();
	    	while (tableT405.next()) {
	    		long childRPID = tableT405.getLong("PERSON_RPID");
	    		if (birthEvents.get(childRPID) != null) {
		    		tableT405.updateLong("EVNT_RPID", birthEvents.get(childRPID));
		    		tableT405.updateRow();
	    		} else missingBirthEvent++;
	    	}
	    	if (TMGglobal.TRACE)
	    		if (missingBirthEvent > 0) System.out.println(" Missing birth events: " + missingBirthEvent);
		} catch (SQLException sqle) {
			System.out.println(" Update updateBirthEvents(): " + sqle.getMessage());
			sqle.printStackTrace();
		}
    }

/**
 * memoData()
 */
    public void memoData() {
    	System.out.println(" No memo/empty: " + noMemo);
    	System.out.println(" Small memo < 300: " + smallMemo);
    	System.out.println(" Too Large > max: " + largeMemo);
    	System.out.println(" Total nr. events: " + nrOfEvents);
    }

/**
 * getTagLangCode(String language)
 * @param language
 * @return
 */
	private String getTagLangCode(String language) {
		String langName = language.toUpperCase();
		return getLangCode(langName);
	}

/**
 * getLangCode(String language)
 * @param language
 * @return
 */
	private String getLangCode(String language) {
		String code = "en-US";
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

} // End classes
