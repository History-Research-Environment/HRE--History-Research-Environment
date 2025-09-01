package hre.tmgjava;
/******************************************************************************
 * Uses library com.linuxense.javadbf
 * Java library for reading and writing Xbase (dBase/DBF) files
 * https://github.com/albfernandez/javadbf
 * albfernandez/javadbf is licensed under the
 * GNU Lesser General Public License v3.0
 * Written by Alberto Fern�ndez
 * *******************************************************************************
 * Process Citation/Source tables in HRE
 * *******************************************************************************
 * v0.01.0032  2025-02-11 - Added code for citation/source import (N. Tolleshaug)
 * 			   2025-02-11 - Updated code for citation/source tables (N. Tolleshaug)
 * 			   2025-02-14 - Updated code name/location tables import (N. Tolleshaug)
 * 			   2025-03-19 - Numerical values for ACCURACY use TINYINT (N. Tolleshaug)
 * 			   2025-05-17 - Added import of "F" partner citations (N. Tolleshaug)
 * 			   2025-07-12 - Added remaining source_def/elemnt, repo and link tables (N. Tolleshaug)
 * 			   2025-07-21 - Added code for extracting source element name data (N. Tolleshaug)
 * 			   2025-07-21 - Only import ruleset 3 - Mills & custom source types (N. Tolleshaug)
 *  		   2025-07-22 - Imported project ruleset from pjc file (N. Tolleshaug)
 *  		   2025-07-24 - Tested indexing source type between A and Mtables (N. Tolleshaug)
 *  		   2025-08-22 - Corrected import of RuleSet 1,2,3 Source types (D Ferguson)
 *  		   2025-08-23 - Fixed A/M table matching for all Ruleset types (D Ferguson)
 *  		   2025-08-24 - Make Source element names upperCase for comparisons (D Ferguson)
 *  		   2025-08-24 - Use TMGlanguage for all SORC_LANG settings (D Ferguson)
 *  		   2025-08-26 - Fix elementInfoExtract to extract data correctly (D Ferguson)
 *  		   2025-08-31 - Create T734 records with full element ID info (D Ferguson)
 **********************************************************************************
 * Accuracy numerical definitions
 * 		3 = an original source, close in time to the event
 * 		2 = a reliable secondary source
 * 		1 = a less reliable secondary source or an assumption based on other facts in a source
 * 		0 = a guess
 *  	- = -1 the source does not support the information cited or this information has been disproved.
 * 	space = -2 no accuracy recorded
 * 	empty = -3 No data available
 * *******************************************************************************
 * For Fidelity, TMG set it as
 * 1 = Other, 2 = Original, 3 = Photocopy, 4 = Transcript, 5 = Extract :
 * TMG = 1 - > HRE 'E'
 * TMG = 2 - > HRE 'A'
 * TMG = 3 - > HRE 'B'
 * TMG = 4 - > HRE 'C'
 * TMG = 5 - > HRE 'D'
 *********************************************************************************/

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TMGpass_Source
 * Convert source data from TMG to HRE
 * @author NTo - Nils Tolleshaug
 * @since 2023-08-15
 * @see document
 */
public class TMGpass_Source {
	long proOffset = 1000000000000000L;
	long null_RPID  = 1999999999999999L;

	TMGHREconverter tmgHreConverter;
	HREdatabaseHandler pointHREbase;
	TMGtableData  tmgAtable = null;
	TMGtableData  tmgMtable = null;
	TMGtableData  tmgRtable = null;
	TMGtableData  tmgStable = null;
	TMGtableData  tmgUtable = null;
	TMGtableData  tmgWtable = null;
	int currentRow = 0;
	int errorCount = 0;
	final static int majorTestSource = 2;
	// On/off dump of source
	boolean sourceDump = false;
	//boolean sourceDump = true;

	long citedRecordRPID, citationTablePID;
	String citedTableName, sourceTitle, abbrevTitle, ctnDetail, ctnMemo;

	String sourceTypeName, sourceRefTable, sourceMemo, subSource, sourceText, sourceRemind;
	String numberedFullFoot, numberedShortFoot, numberedBiblio;
	int sourceNumber, majSource, sourceType, transToType, custType, sourceRefId;

// Source
	String elementSourceInfo, ruleSetName;

// Source def variables
	int sourceDefType;

// Source element variables
	String elementName, elementNumber;
	int elementRecno;

// Repos variables
	String reposName, reposAbbrev;
	int reposRefnr = 0, reposLocationTable = 0;

// Link table variables
	String linkRefrence;
	String ruleSet;
	int sourceTableID, reposTableID, pjcRuleSetNumber;
	boolean primaryRespository;

// Counters for citation table types
	int names, relationships, sources, events, places, citations;

	 ResultSet tableT734_SORC_ELMNT_DATA, tableT735_CITN, tableT736_SORC, tableT737_SORC_DEFN, tableT738_SORC_ELMNT,
	 		 tableT739_REPO, tableT740_SORC_LINK;

// Index for TMG to HRE PID for events
	public HashMap<Integer,Long> eventIndexPID = new HashMap<Integer, Long>();

// Hashmap to look up elementNumber from U.dbf
	public HashMap<String,Integer> sorcElmntNameIndex = new HashMap<String,Integer>();

// Hashmap for source def table PID
	public HashMap<Integer,Long> sorcDefinPIDindex = new HashMap<Integer,Long>();

/**
 * Constructor TMGpass_V22c_Source
 * @param pointHREbase
 */
	TMGpass_Source(HREdatabaseHandler pointHREbase, String ruleSet) {
		this.pointHREbase = pointHREbase;
		this.ruleSet = ruleSet;
		tableT734_SORC_ELMNT_DATA = TMGglobal.T734;
		tableT735_CITN = TMGglobal.T735;
		tableT736_SORC = TMGglobal.T736;
		tableT737_SORC_DEFN = TMGglobal.T737;
		tableT738_SORC_ELMNT = TMGglobal.T738;
		tableT739_REPO = TMGglobal.T739;
		tableT740_SORC_LINK = TMGglobal.T740;
	}

	public void initSourceTables() {
		tmgAtable = TMGglobal.tmg_A_table;
		tmgMtable = TMGglobal.tmg_M_table;
		tmgRtable = TMGglobal.tmg_R_table;
		tmgStable = TMGglobal.tmg_S_table;
		tmgUtable = TMGglobal.tmg_U_table;
		tmgWtable = TMGglobal.tmg_W_table;
	}

/**
 * public void addToCitationTable()
 * Citation record types
 * N=name,
 * F=relationship,
 * M=source,
 * E=event,
 * P=Place,
 * C=Citation.
 */
	public void addToCitationTable(TMGHREconverter tmgHreConverter) {
		this.tmgHreConverter = tmgHreConverter;
		if (sourceDump) System.out.println("\n**** Citation table: *******************************************");
		int nrOftmgSRows, referedRecord;
		nrOftmgSRows = tmgStable.getNrOfRows();
	// Start processing
		for (int index_S_Table = 0; index_S_Table < nrOftmgSRows; index_S_Table++) {
			currentRow = index_S_Table + 1;
			try {
				sourceNumber = tmgStable.getValueInt(index_S_Table,"RECNO");
				referedRecord = tmgStable.getValueInt(index_S_Table,"REFREC");

				majSource = tmgStable.getValueInt(index_S_Table,"MAJSOURCE");
				sourceRefTable = tmgStable.getValueString(index_S_Table,"STYPE");
				sourceTypeName = tmgAtable.findValueString(sourceType,"NAME");	// never used
				sourceMemo = tmgStable.getValueString(index_S_Table,"CITMEMO");
				subSource = tmgStable.getValueString(index_S_Table,"SUBSOURCE");

				citationTablePID = sourceNumber + proOffset;
				citedTableName = "----";

				// Test printout citations
				if (majSource == majorTestSource)
					if (sourceDump) System.out.println(" " + currentRow  + " Source ref/type: " + sourceRefTable
							+ "/" + sourceType + "  Title: " + abbrevTitle
							+ "\n    SourceTitle: " + sourceTitle  + "  Text: " + sourceText
							+ "\n    SourceType: " + sourceTypeName + "  SubSource: " + subSource);

		/**
		 * Citation record type (N=name, F=relationship, M=source, E=event, P=Place, C=Citation	)
		 */
				if (sourceRefTable.equals("N")) {
					citedRecordRPID = referedRecord + proOffset;
					citedTableName = "T402";
					insertRowT735_CITN( index_S_Table, citationTablePID, tableT735_CITN);
					names++;
					continue;
				}
				if (sourceRefTable.equals("F")) {
					citedRecordRPID = referedRecord + proOffset;
					citedTableName = "T405";
					insertRowT735_CITN( index_S_Table, citationTablePID, tableT735_CITN);
					relationships++;
					continue;
				}
				if (sourceRefTable.equals("M")) {
					sources++;
					continue;
				}
				if (sourceRefTable.equals("E")) {
					citedRecordRPID = eventIndexPID.get(referedRecord);
					citedTableName = "T450";
					insertRowT735_CITN( index_S_Table, citationTablePID, tableT735_CITN);
					events++;
					continue;
				}
				if (sourceRefTable.equals("P")) {
					citedRecordRPID = referedRecord + proOffset;
					citedTableName = "T552";
					citationTablePID = sourceNumber + proOffset;
					insertRowT735_CITN( index_S_Table, citationTablePID, tableT735_CITN);
					places++;
					continue;
				}
				if (sourceRefTable.equals("C")) {
					citations++;
					continue;
				}
				System.out.println(" " + currentRow + " not found type: " + sourceRefTable);

			} catch (HCException hce) {
				System.out.println("addToCitationTable error: " + hce.getMessage());
				hce.printStackTrace();
			}
			if (sourceDump)  System.out.println( "PID: " + citationTablePID + " Event "
						+ citedTableName + " Cited PID: " + citedRecordRPID);
		}
	}

/**
 * void addToSourceTable()
 */
	public void addToSourceTable(TMGHREconverter tmgHreConverter) {
		this.tmgHreConverter = tmgHreConverter;
	// Based on data in the M.dbf records, extract M.info data into T734 SORC_DATA records
	// and create the T736 SORC records
		long sourceTablePID, T734tablePID = proOffset;
		List<String> elementNameList = null, elementNumberList = null;
		String allTemplates, sorcFullFoot ="", sorcShortFoot = "", sorcBiblio = "";
		String[] T737Templates;
		String[][] splitInfo;
		if (sourceDump)
			System.out.println("\n**** T736_SORC table: ****");
		int nrOftmgRRows = tmgMtable.getNrOfRows();

		for (int index_M_Table = 0; index_M_Table < nrOftmgRRows; index_M_Table++) {
			try {
				sourceTablePID = tmgMtable.getValueInt(index_M_Table, "MAJNUM") + proOffset;
				sourceRefId = tmgMtable.getValueInt(index_M_Table, "REF_ID");
				sourceTitle = tmgMtable.getValueString(index_M_Table, "TITLE");
				abbrevTitle = tmgMtable.getValueString(index_M_Table, "ABBREV");
				sourceText = tmgMtable.getValueString(index_M_Table,"TEXT");
			// GET THE sourceType VALUE SET CORRECTLY!
			// For PJC Ruleset = 1 (Mills) or 3 (Custom), the M.custtype matches A.sourtype where A.ruleset
			// matches that RuleSet number.  A T737 SORC_DEFN record will have been created for each A record.
			// Thus each M.custtype will now match a T737 SORC_DEFN's SORC_DEF_TYPE value.
			//
			// For PJC Ruleset = 2 (Lackey) source types a double type-matching process is required.
			// The T737 creation step will have created a T737 record for each of the 14 Lackey A.dbf records.
			// For the match process, if a Source M.type matches A.sourtype where A.ruleset=1 then the A record's
			// A.trans_to field will match a T737 SORC_DEFN record's SORC_DEF_TYPE (in the range of 1-14),
			// so that the A.trans_to value is the 'real' sourceType of a T737 SORC_DEFN.
				if (pjcRuleSetNumber == 1 || pjcRuleSetNumber == 3)
											sourceType = tmgMtable.getValueInt(index_M_Table, "CUSTTYPE");
				else {		// for Lackey type source defns, get M.type
					sourceType = tmgMtable.getValueInt(index_M_Table, "TYPE");
					// Match it to an A.sourtype, and get the A.trans_to
					for (int index_A = 0; index_A < tmgAtable.getNrOfRows(); index_A++) {
						if (sourceType == tmgAtable.getValueInt(index_A, "SOURTYPE")) {
							transToType = tmgAtable.getValueInt(index_A, "TRANS_TO");
							break;
						}
					}
					// and reset sourtype to trans_to (one of the Lackey types (1-14))
					sourceType = transToType;
				}

			// Pass M.info to elementInfoExtract routine to extract source text data into splitInfo.
			// splitInfo[][] will contain rows of source element data + group# of that data in text format
				elementSourceInfo = HREmemo.returnStringContent(tmgMtable.getValueString(index_M_Table,"INFO"));
				splitInfo = elementInfoExtract(elementSourceInfo);

			// Get all 3 source templates from T737 SORC_DEFN for this source type
				T737Templates = getT737SourceTemplates(sourceType);

			// Now if M templates exist, use them, otherwise fall back to the base T737 templates and
			// construct a single string (allTemplates) of all 3 full/short footnotes and bibliography.
				sorcFullFoot = HREmemo.returnStringContent(tmgMtable.getValueString(index_M_Table,"FFORM"));
				if (sorcFullFoot.isEmpty()) allTemplates = T737Templates[0];
				else allTemplates = sorcFullFoot;

				sorcShortFoot = HREmemo.returnStringContent(tmgMtable.getValueString(index_M_Table,"SFORM"));
				if (sorcShortFoot.isEmpty()) allTemplates = allTemplates + T737Templates[1];
				else allTemplates = allTemplates + sorcShortFoot;

				sorcBiblio = HREmemo.returnStringContent(tmgMtable.getValueString(index_M_Table,"BFORM"));
				if (sorcBiblio.isEmpty()) allTemplates = allTemplates + T737Templates[2];
				else allTemplates = allTemplates + sorcBiblio;

				if (sourceDump) System.out.println("Source#=" + index_M_Table + "  allTemplates=" + allTemplates);

			// Now extract out of allTemplates a List of all unique [SOURCE ELEMENTS] that appear.
				elementNameList = extractElementNames(allTemplates);

			// Lookup T738 Source Elements for records with matching element names and get
			// their IDs into a new elementNumberList
				elementNumberList = new ArrayList<String>();
				for (int i = 0; i < elementNameList.size(); i++) {
					elementNumberList.add(getT738Number(elementNameList.get(i)));
				}

			// For each piece of source data in splitinfo[j][0] find the elementName/number it is for.
			// To get the right element, the group# in splitInfo[j][1] will match the 1st 2 digits
			// of the ID# from the T738 record, now held in elementNumberList (Note, there can only be
			// one record that satisfies this match as TMG only allows 1 item from each group).
			// When found, place the element Number back into splitInfo[j][1]
			if (splitInfo.length > 0) {
				for (int j = 0; j < splitInfo.length; j++) {
					for (int k = 0; k < elementNumberList.size(); k++) {
					// Extract the group# (1st 2 digits) of an elemenNumberList entry
						String group = elementNumberList.get(k).substring(0, 2);
					// if it matches the splitInfo group#, update splitInfo with the full 5-digit element#
						if (splitInfo[j][1].equals(group)) {
							splitInfo[j][1] = elementNumberList.get(k);
							break;
						}
					}
					// Create a T734 SORC DATA record holding:
					// - PID of this Source #
					// - the element ID# from splitInfo[][1] (as matched above) that defines the T738 SORC ELMNT
					// - the source's data from splitInfo[][0] (as extracted from M.info)
					T734tablePID = T734tablePID + 1;
					insertRowT734_SORC_DATA(T734tablePID, sourceTablePID,
							splitInfo[j][1], splitInfo[j][0], tableT734_SORC_ELMNT_DATA);
				}
			}

			// For each of the 3 M table source templates, convert their element [NAME] entries
			// to [number] entries (if they have any template data to convert)
			numberedFullFoot = templateNameToNumber(sorcFullFoot, elementNameList, elementNumberList);
			numberedShortFoot = templateNameToNumber(sorcShortFoot, elementNameList, elementNumberList);
			numberedBiblio = templateNameToNumber(sorcBiblio, elementNameList, elementNumberList);


			// Finally, create new T736 SORC record for this M record
			insertRowT736_SORC(index_M_Table, sourceTablePID, tableT736_SORC);
			} catch (HCException hce) {
				System.out.println("addToT736Source error: " + hce.getMessage());
				hce.printStackTrace();
			}

			// Now go back to the 3 source templates recorded in the base T737 SORC DEFN records
			// and convert these templates from [NAME] entries to [number] entries



		}
	}

/**
 * addToSorceDefTable(TMGHREconverter tmgHreConverter)
 * @param tmgHreConverter
 * Field Name Type Width Description
		* ? RULESET N 1 1=Mills Categories, 2=Lackey Categories, 3=Custom Categories
		- DSID I 4 Data Set ID
		* ? SOURTYPE I 4 Type within this ruleset.
		* ? TRANS_TO I 4 Most like which source type within Mills’ Categories?
		* NAME C 66 Name of this source type
		- FOOT M 4 Standard full footnote template
		- SHORT M 4 Standard short footnote template
		- BIB M 4 Standard bibliographic template
		* CUSTFOOT M 4 Custom full footnote template
		* CUSTSHORT M 4 Custom short footnote template
		* CUSTBIB M 4 Custom bibliographic template
		- SAMEAS I 4 This source type is the same as another in the Mills Categories.
		- Relates to a.sourtype where ruleset=1.
		- SAMEASMSG M 4 The message displayed to the user when this source type is selected.
		- PRIMARY L 1 Is this the primary custom source type (ruleset=3).
		* REMINDERS M 4 Hint Memo  (new in v7.01)
 */
	public void addToSorceDefTable(TMGHREconverter tmgHreConverter) {
		this.tmgHreConverter = tmgHreConverter;
	// Based on pjc RuleSet #, build the T737 SORC_DEFN table
		long sourceDefTablePID = proOffset;
		int ruleSetFound;
		int nrOftmgRows = tmgAtable.getNrOfRows();
		if (sourceDump)
			System.out.println(" Start  T737_SORC_DEFN table: *******");
	// Get PJC Ruleset number: 1=Mills Categories, 2=Lackey Categories, 3=Custom Categories
		pjcRuleSetNumber = Integer.parseInt(ruleSet.substring(ruleSet.length()-1, ruleSet.length()));
		if (pjcRuleSetNumber == 1) ruleSetName = "Mills";
		else if (pjcRuleSetNumber == 2) ruleSetName = "Lackey";
		else if (pjcRuleSetNumber == 3) ruleSetName = "Custom ";
		else ruleSetName = "Not known";
		System.out.println(" Project Ruleset: " + pjcRuleSetNumber + " - " + ruleSetName + " Categories");
		tmgHreConverter.setStatusMessage(" Source Ruleset: " + pjcRuleSetNumber
				+ " - " + ruleSetName + " Categories");

		for (int index_A_Table = 0; index_A_Table < nrOftmgRows; index_A_Table++) {
			try {
				ruleSetFound = tmgAtable.getValueInt(index_A_Table, "RULESET");
				// Import RuleSet 1 (Mills) OR RuleSet 2 (Lackey) OR Ruleset 3 (Custom) source types.
				// For each record in A.dbf where A.ruleset = the PJC ruleSetNumber, create a T737 record
				if (ruleSetFound == pjcRuleSetNumber) {
					sourceDefTablePID = sourceDefTablePID + 1;
					sourceDefType = tmgAtable.getValueInt(index_A_Table, "SOURTYPE");
					custType = tmgAtable.getValueInt(index_A_Table, "SOURTYPE");
					sorcDefinPIDindex.put(custType, sourceDefTablePID);
				// Add new source def record for the A record at entry index_A_table to T737 ResultSet
					insertRowT737_SORC_DEFN(index_A_Table, sourceDefTablePID, tableT737_SORC_DEFN);
				}
			} catch (HCException hce) {
				System.out.println("addToSorceDefTable error: " + hce.getMessage());
				hce.printStackTrace();
			}
		}
		//System.out.println(" End  T737_SORC_DEFN table: *******");
	}

/**
 * addToSourceElementTable(TMGHREconverter tmgHreConverter)
 * @param tmgHreConverter
 * Field Name Type Width Description
		RECNO I 4 PRIMARY KEY.  ID Number of the Source Element.
		DSID I 4 Data Set ID
		ELEMENT C 30 Source Element name
		GROUPNUM N 3 Source Element administrative group:
 */
	public void addToSorceElementTable(TMGHREconverter tmgHreConverter) {
		this.tmgHreConverter = tmgHreConverter;
		long sourceElementTablePID = proOffset;
		int elementTmgGroup, prevRecno = 0, elementRecno = 0;
		if (sourceDump)
			System.out.println(" ****  T738_SORC_ELMNT table: *******");
		int nrOftmgURows = tmgUtable.getNrOfRows();
		for (int index_U_Table = 0; index_U_Table < nrOftmgURows; index_U_Table++) {
			try {
				//sourceElementTablePID = sourceElementTablePID + 1;
		// Special construction to compensate for non consecutive reposRecno - Ferguson project !!!
				prevRecno = elementRecno;
				elementRecno = tmgUtable.getValueInt(index_U_Table, "RECNO");
				elementName = tmgUtable.getValueString(index_U_Table, "ELEMENT").trim();
				if (elementRecno <= prevRecno) {
					System.out.println(" WARNING - T738_SORC_ELMNT: " + elementName + " - RECNO: " + elementRecno + " == prev: " + prevRecno + " Ignored!");
					continue;
				}
		// *********************************************************************
				sourceElementTablePID = proOffset + elementRecno;
				elementTmgGroup = tmgUtable.getValueInt(index_U_Table, "GROUPNUM");
				if (sorcElmntNameIndex.containsKey(elementName))
					System.out.println(" Duplicate element name: " + elementName);
				else
					sorcElmntNameIndex.put(elementName, elementTmgGroup);
			// Calculation of elementNum as CHAR(5)
				elementNumber = String.format("%05d", (elementTmgGroup*1000 + elementRecno));

		// Add new source element record
				insertRowT738_SORC_ELMNT(index_U_Table, sourceElementTablePID, tableT738_SORC_ELMNT);

			} catch (HCException hce) {
				System.out.println("ddToSorceElementTable error: " + hce.getMessage());
				hce.printStackTrace();
			}
		}
	}

/**
 * addToReposTable(TMGHREconverter tmgHreConverter)
 * @param tmgHreConverter
 */
	public void addToReposTable(TMGHREconverter tmgHreConverter) {
		this.tmgHreConverter = tmgHreConverter;
		long sourceReposTablePID = proOffset;
		int reposRecno = 0, prevRecno;
		if (sourceDump)
			System.out.println(" *** Start  T739_REPO processing");
		int nrOftmgRows = tmgRtable.getNrOfRows();
		for (int index_R_Table = 0; index_R_Table < nrOftmgRows; index_R_Table++) {
			try {
		// Special construction to compensate for non consecutive reposRecno - Ferguson project !!!
				prevRecno = reposRecno;
				reposRecno = tmgRtable.getValueInt(index_R_Table, "RECNO");
				if (reposRecno < prevRecno) {
					System.out.println(" WARNING - T739_REPO - RECNO: " + reposRecno + " -> " + (prevRecno + 1) + " Abbrev: " + reposAbbrev + " /Name: " + reposName);
					reposRecno = prevRecno + 1;
				}
		// *********************************************************************
				sourceReposTablePID = reposRecno + proOffset;
				reposName = HREmemo.returnStringContent(tmgRtable.getValueString(index_R_Table,"NAME"));
				reposAbbrev = tmgRtable.getValueString(index_R_Table, "ABBREV");
				reposRefnr = tmgRtable.getValueInt(index_R_Table, "REF_ID");
				reposLocationTable = tmgRtable.getValueInt(index_R_Table, "ADDRESS");

		// Add new source repos record
				insertRowT739_REPO(index_R_Table, sourceReposTablePID, tableT739_REPO);
				//System.out.println(" Repos: " + reposRecno + " - " + reposAbbrev + " /Name: " + reposName);
			} catch (HCException hce) {
				System.out.println("addToReposTable error: " + hce.getMessage());
				hce.printStackTrace();
			}
		}
		//System.out.println(" *** End T739_REPO");
	}

/**
 * addToSorceLinkTable(TMGHREconverter tmgHreConverter)
 * @param tmgHreConverter
 * Links between sources and respositories.
 *
	Field Name Type Width Description
		MNUMBER I 4 ID Number of the linked Source.  Related to m.majnum.
		RNUMBER I 4 ID Number of the linked Repository.  Related to r.recno.
		REFERENCE C 25 Repository/Source reference value
		PRIMARY L 1 Is this the primary repository for this source?
 */
	public void addToSorceLinkTable(TMGHREconverter tmgHreConverter) {
		this.tmgHreConverter = tmgHreConverter;
		long sourceLinkTablePID = proOffset;
		if (sourceDump)
			System.out.println(" *** Start  tableT740_SORC_LINK processing");
		int nrOftmgWRows = tmgWtable.getNrOfRows();
		for (int index_W_Table = 0; index_W_Table < nrOftmgWRows; index_W_Table++) {
			try {
				sourceLinkTablePID = sourceLinkTablePID + 1;
				sourceTableID = tmgWtable.getValueInt(index_W_Table, "MNUMBER");
				reposTableID = tmgWtable.getValueInt(index_W_Table, "RNUMBER");
				linkRefrence = tmgWtable.getValueString(index_W_Table, "REFERENCE");
				primaryRespository = tmgWtable.getValueBoolean(index_W_Table, "PRIMARY");

		// Add new source link record
				insertRowT740_SORC_LINK(index_W_Table, sourceLinkTablePID, tableT740_SORC_LINK);
			} catch (HCException hce) {
				System.out.println("addToSorceLinkTable error: " + hce.getMessage());
				hce.printStackTrace();
			}
		}
	}

/**
 * private void insertRowT734_SORC_DATA(long T734tablePID, long T736tablePID, String elementNum,
										String elementData, ResultSet hreTable) throws HCException {
	createTableInBase("T734_SORC_DATA","PID BIGINT NOT NULL,"
			  + "SORC_OWNER_RPID BIGINT NOT NULL,"
			  + "SORC_ELMNT_LANG CHAR(5) NOT NULL,"
			  + "SORC_ELMNT_NUM CHAR(5) NOT NULL,"
			  + "SORC_ELMNT_DATA VARCHAR(400) NOT NULL");
*/
	private void insertRowT734_SORC_DATA(long T734tablePID, long T736tablePID, String elementNum,
										String elementData, ResultSet hreTable) throws HCException {
		try {
		    // moves cursor to the insert row
				hreTable.moveToInsertRow();
			// Update row
				hreTable.updateLong("PID", T734tablePID);
				hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
				hreTable.updateLong("SORC_OWNER_RPID", T736tablePID);
				hreTable.updateString("SORC_ELMNT_LANG", tmgHreConverter.languageTMG());
				hreTable.updateString("SORC_ELMNT_NUM", elementNum );
				hreTable.updateString("SORC_ELMNT_DATA", elementData);
			//Insert row
				hreTable.insertRow();
		} catch (SQLException sqle) {
			System.out.println("insertRowT734_SORC_DATA - error: " + sqle.getMessage());
			sqle.printStackTrace();
			errorCount++;
			throw new HCException("insertRowT734_SORC_DATA  - error: " + sqle.getMessage());
		}
	}

/**
 * private void insertRowT735_CITN(long indexPID, ResultSet hreTable)
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
 * @throws HCException
 */
	private void insertRowT735_CITN(int index_S_Table, long T735tablePID, ResultSet hreTable) throws HCException {
		try {
		    // moves cursor to the insert row
				hreTable.moveToInsertRow();
			// Update new row in H2 database
				hreTable.updateLong("PID", T735tablePID);
				hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
				hreTable.updateLong("CITED_RPID", citedRecordRPID);
				hreTable.updateString("OWNER_TYPE", citedTableName );
				hreTable.updateLong("SORC_RPID", majSource + proOffset);
				hreTable.updateLong("ASSESSOR_RPID", proOffset + 1L);

			//Create a T167_MEMO_SET record with citation detials
				ctnDetail = HREmemo.returnStringContent(tmgStable.getValueString(index_S_Table,"SUBSOURCE"));
			// Processing detial to T167_MEMO_SET
				if (ctnDetail.length() == 0) hreTable.updateLong("CITN_DETAIL_RPID", null_RPID);
				else hreTable.updateLong("CITN_DETAIL_RPID",
						tmgHreConverter.pointHREmemo.addToT167_22c_MEMO(ctnDetail));

			//Create a T167_MEMO_SET record with the citation memo
				ctnMemo = HREmemo.returnStringContent(tmgStable.getValueString(index_S_Table,"CITMEMO"));
			// Processing memo to T167_MEMO_SET
				if (ctnMemo.length() == 0) hreTable.updateLong("CITN_MEMO_RPID", null_RPID);
				else hreTable.updateLong("CITN_MEMO_RPID",
						tmgHreConverter.pointHREmemo.addToT167_22c_MEMO(ctnMemo));

				hreTable.updateString("CITN_REF", tmgStable.getValueString(index_S_Table,"CITREF"));
				hreTable.updateInt("CITN_GUI_SEQ", tmgStable.getValueInt(index_S_Table,"SEQUENCE"));
				hreTable.updateInt("CITN_ACC_NAME1", accConvert(tmgStable.getValueString(index_S_Table,"SNSURE")));
				hreTable.updateInt("CITN_ACC_NAME2",accConvert(tmgStable.getValueString(index_S_Table,"SSSURE")));
				hreTable.updateInt("CITN_ACC_DATE", accConvert(tmgStable.getValueString(index_S_Table,"SDSURE")));
				hreTable.updateInt("CITN_ACC_LOCN", accConvert(tmgStable.getValueString(index_S_Table,"SPSURE")));
				hreTable.updateInt("CITN_ACC_MEMO", accConvert(tmgStable.getValueString(index_S_Table,"SFSURE")));
			// Test
				//hreTable.updateInt("CITN_ACC_P2NAME", -1);

			//Insert row
				hreTable.insertRow();
		} catch (SQLException sqle) {
			System.out.println("insertRowT735_CITN - error: " + sqle.getMessage());
			sqle.printStackTrace();
			errorCount++;
			throw new HCException("insertRowT735_CITN  - error: " + sqle.getMessage());
		}
	}

/**
 * accConvert(String acc)
 * @param acc
 * @return
 */
	private int accConvert(String acc) {
		if (acc.equals("3")) return 3;
		if (acc.equals("2")) return 2;
		if (acc.equals("1")) return 1;
		if (acc.equals("0")) return 0;
		if (acc.equals("-")) return -1;
		if (acc.equals(" ")) return -2;
		if (acc.isEmpty()) return -3;
		return -4;
	}

/**
 * private void insertRowT736_SORC(int index_M_Table, long sourceTablePID, ResultSet hreTable)
 * @param indexPID
 * @param hreTable
 * CREATE TABLE T736_SORC(
				PID BIGINT NOT NULL,
				CL_COMMIT_RPID BIGINT NOT NULL,
				IS_ACTIVE BOOLEAN NOT NULL,
				SORC_DEF_RPID BIGINT NOT NULL,
				SORC_REF SMALLINT NOT NULL,
				SORC_TYPE SMALLINT NOT NULL,
				SORC_FIDELITY CHAR(1) NOT NULL,
				SORC_TEXT_RPID BIGINT NOT NULL,
				SORC_AUTHOR_RPID BIGINT NOT NULL,
				SORC_EDITOR_RPID BIGINT NOT NULL,
				SORC_COMPILER_RPID BIGINT NOT NULL,
				SORC_ABBREV CHAR(50) NOT NULL,
				SORC_TITLE VARCHAR(600) NOT NULL,
				SORC_FULLFORM VARCHAR(500) NOT NULL,
				SORC_SHORTFORM VARCHAR(500) NOT NULL,
				SORC_BIBLIOFORM VARCHAR(500) NOT NULL,
				SORC_REMIND_RPID BIGINT NOT NULL
				);
 * @throws HCException
 */
	private void insertRowT736_SORC(int index_M_Table, long sourceTablePID, ResultSet hreTable) throws HCException {
		// Move cursor to the insert row
			try {
				hreTable.moveToInsertRow();
				hreTable.updateLong("PID", sourceTablePID);
				hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
				hreTable.updateBoolean("IS_ACTIVE", tmgMtable.getValueBoolean(index_M_Table,"MACTIVE"));

			// M table to T737 table match based on sourceType
				if (sorcDefinPIDindex.containsKey(sourceType))
					hreTable.updateLong("SORC_DEF_RPID", sorcDefinPIDindex.get(sourceType));
				else {
					hreTable.updateLong("SORC_DEF_RPID", null_RPID);
					System.out.println(ruleSetName + " Source type mismatch between M/T737 tables - Type: " + sourceType);
				}
				hreTable.updateInt("SORC_REF", sourceRefId);
				hreTable.updateInt("SORC_TYPE", sourceType);

			// Source fidelity
				int sourceFidleity = tmgMtable.getValueInt(index_M_Table,"FIDELITY");
				if (sourceFidleity == 1) hreTable.updateString("SORC_FIDELITY","E");
				else if (sourceFidleity == 2) hreTable.updateString("SORC_FIDELITY","A");
				else if (sourceFidleity == 3) hreTable.updateString("SORC_FIDELITY","B");
				else if (sourceFidleity == 4) hreTable.updateString("SORC_FIDELITY","C");
				else if (sourceFidleity == 5) hreTable.updateString("SORC_FIDELITY","D");
				else if (sourceFidleity < 1 || sourceFidleity > 5) {
						System.out.println(" Fidelity: " + sourceFidleity + " not found");
						hreTable.updateString("SORC_FIDELITY","A");
					}

			// Create a T167_MEMO_SET record with the citation memo
				sourceMemo = HREmemo.returnStringContent(tmgMtable.getValueString(index_M_Table,"TEXT"));
			// Processing memo to T167_MEMO_SET
				if (sourceMemo.length() == 0) hreTable.updateLong("SORC_TEXT_RPID", null_RPID);
				else hreTable.updateLong("SORC_TEXT_RPID",
						tmgHreConverter.pointHREmemo.addToT167_22c_MEMO(sourceMemo));

			// Check the following RPID value towards PID for T401
				hreTable.updateLong("SORC_AUTHOR_RPID", proOffset + tmgMtable.getValueInt(index_M_Table,"SPERNO"));
				hreTable.updateLong("SORC_EDITOR_RPID", proOffset + tmgMtable.getValueInt(index_M_Table,"EDITORID"));
				hreTable.updateLong("SORC_COMPILER_RPID", proOffset + tmgMtable.getValueInt(index_M_Table,"COMPILERID"));
			// End
				hreTable.updateString("SORC_ABBREV",tmgMtable.getValueString(index_M_Table,"ABBREV"));
				hreTable.updateString("SORC_TITLE",tmgMtable.getValueString(index_M_Table,"TITLE"));
				hreTable.updateString("SORC_FULLFORM", numberedFullFoot);
				hreTable.updateString("SORC_SHORTFORM", numberedShortFoot);
				hreTable.updateString("SORC_BIBLIOFORM", numberedBiblio);
			// Create a T167_MEMO_SET record with the reminder
				sourceRemind = HREmemo.returnStringContent(tmgMtable.getValueString(index_M_Table,"TEXT"));
			// Processing memo to T167_MEMO_SET
				if (sourceRemind.length() == 0) hreTable.updateLong("SORC_REMIND_RPID", null_RPID);
				else hreTable.updateLong("SORC_REMIND_RPID",
						tmgHreConverter.pointHREmemo.addToT167_22c_MEMO(sourceRemind));
			// Insert row
				hreTable.insertRow();
			} catch (SQLException sqle) {
				System.out.println("insertRowT736_SORC - error: " + sqle.getMessage());
				sqle.printStackTrace();
				throw new HCException("insertRowT736_SORC - error: " + sqle.getMessage());
			}
		}

/**
 *  private void insertRowT737_SORC_DEFN(int index_A_Table, long T737tablePID, ResultSet hreTable)
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
 * @throws HCException
 */
	private void insertRowT737_SORC_DEFN(int index_A_Table, long T737tablePID, ResultSet hreTable) throws HCException {
		String reminderNote;
		try {
		    // moves cursor to the insert row//************************************check atable stuff
				hreTable.moveToInsertRow();
			// Update new row in H2 database
				hreTable.updateLong("PID", T737tablePID);
				hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
				hreTable.updateInt("SORC_DEFN_TYPE", sourceDefType);
				hreTable.updateString("SORC_DEFN_NAME", tmgAtable.getValueString(index_A_Table, "NAME"));
				hreTable.updateString("SORC_LANG", tmgHreConverter.languageTMG());
				hreTable.updateString("SORC_DEFN_FULLFOOT",
						HREmemo.returnStringContent(tmgAtable.getValueString(index_A_Table,"CUSTFOOT")));
				hreTable.updateString("SORC_DEFN_SHORTFOOT",
						HREmemo.returnStringContent(tmgAtable.getValueString(index_A_Table,"CUSTSHORT")));
				hreTable.updateString("SORC_DEFN_BIBLIO",
						HREmemo.returnStringContent(tmgAtable.getValueString(index_A_Table,"CUSTBIB")));

				reminderNote = HREmemo.returnStringContent(tmgAtable.getValueString(index_A_Table,"REMINDERS"));
			// Processing memo to T167_MEMO_SET
				if (reminderNote.length() == 0) hreTable.updateLong("SORC_DEFN_REMIND_RPID", null_RPID);
				else hreTable.updateLong("SORC_DEFN_REMIND_RPID",
						tmgHreConverter.pointHREmemo.addToT167_22c_MEMO(reminderNote));

			//Insert row
				hreTable.insertRow();

		} catch (SQLException sqle) {
			System.out.println("insertRowT737_SORC_DEFN - error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HCException("insertRowT737_SORC_DEFN - error: " + sqle.getMessage());
		}
	}

/**
 * private void insertRowT738_SORC_ELMNT(int index_U_Table, long T738tablePID, ResultSet hreTable)
		   CREATE TABLE T738_SORC_ELMNT(
			PID BIGINT NOT NULL,
			CL_COMMIT_RPID BIGINT NOT NULL,
			SORC_ELMNT_NUM CHAR(5) NOT NULL,
			SORC_ELMNT_LANG CHAR(5) NOT NULL,
			SORC_ELMNT_NAME CHAR(30) NOT NULL
			);
		String elementName, elementNumber;
 * @throws HCException
 */
	private void insertRowT738_SORC_ELMNT(int index_U_Table, long T738tablePID, ResultSet hreTable) throws HCException {
		try {
		    // moves cursor to the insert row
				hreTable.moveToInsertRow();
			// Update new row in H2 database
				hreTable.updateLong("PID", T738tablePID);
				hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
				hreTable.updateString("SORC_ELMNT_NUM", elementNumber);
				hreTable.updateString("SORC_LANG", tmgHreConverter.languageTMG());
				hreTable.updateString("SORC_ELMNT_NAME", elementName );
			//Insert row
				hreTable.insertRow();

		} catch (SQLException sqle) {
			System.out.println("insertRowT738_SORC_ELMNT - error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HCException("insertRowT738_SORC_ELMNT - error: " + sqle.getMessage());
		}
	}

/**
 * private void insertRowT739_REPO(int index_R_Table, long T739tablePID, ResultSet hreTable)
		   CREATE TABLE T739_REPO(
			PID BIGINT NOT NULL,
			CL_COMMIT_RPID BIGINT NOT NULL,
			REPO_MEMO_RPID BIGINT NOT NULL,
			REPO_ADDR_RPID BIGINT NOT NULL,
			REPO_ABBREV CHAR(50) NOT NULL,
			REPO_REF SMALLINT NOT NULL,
			REPO_NAME VARCHAR(500) NOT NULL
			);
 * @throws HCException
 */
	private void insertRowT739_REPO(int index_R_Table, long T739tablePID, ResultSet hreTable) throws HCException {
		String reposNote = "";
		try {
		    // moves cursor to the insert row
				hreTable.moveToInsertRow();
			// Update new row in H2 database
				hreTable.updateLong("PID", T739tablePID);
				hreTable.updateLong("CL_COMMIT_RPID", null_RPID);

				reposNote = HREmemo.returnStringContent(tmgRtable.getValueString(index_R_Table,"RNOTE"));
			// Processing memo to T167_MEMO_SET
				if (reposNote.length() == 0) hreTable.updateLong("REPO_MEMO_RPID", null_RPID);
				else hreTable.updateLong("REPO_MEMO_RPID",
						tmgHreConverter.pointHREmemo.addToT167_22c_MEMO(reposNote));

			// Note Ref to T341 not established ***********************
				hreTable.updateLong("REPO_ADDR_RPID", proOffset + reposLocationTable); // *** Location PID

				hreTable.updateString("REPO_ABBREV", reposAbbrev);
				hreTable.updateInt("REPO_REF", reposRefnr);
				hreTable.updateString("REPO_NAME", reposName);
			//Insert row
				hreTable.insertRow();

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			System.out.println("insertRowT739_REPO - error: " + sqle.getMessage());
			throw new HCException("insertRowT739_REPO - error: " + sqle.getMessage());
		}
	}

/**
 * private void insertRowT740_SORC_LINK(int index_W_Table, long T740tablePID, ResultSet hreTable)													throws HCExcept
		   CREATE TABLE T740_SORC_LINK(
			PID BIGINT NOT NULL,
			IS_PRIMARY BOOLEAN NOT NULL,
			SORC_RPID BIGINT NOT NULL,
			REPO_RPID BIGINT NOT NULL,
			REPO_REF CHAR(25) NOT NULL
			);
 */
	private void insertRowT740_SORC_LINK(int index_W_Table, long T740tablePID, ResultSet hreTable)
													throws HCException {
		try {
		    // moves cursor to the insert row
				hreTable.moveToInsertRow();
			// Update new row in H2 database
				hreTable.updateLong("PID", T740tablePID);
				hreTable.updateBoolean("IS_PRIMARY", primaryRespository);
				hreTable.updateLong("SORC_RPID", proOffset + sourceTableID);
				hreTable.updateLong("REPO_RPID", proOffset + reposTableID);
				hreTable.updateString("REPO_REF", linkRefrence);
			//Insert row
				hreTable.insertRow();

		} catch (SQLException sqle) {
			System.out.println("insertRowT740_SORC_LINK - error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HCException("insertRowT740_SORC_LINK - error: " + sqle.getMessage());
		}
	}

/**
 * private String getT737SourceTemplates(int dataBaseIndex)
 * @return
 * @throws HCException
 * @throws SQLException
 */
	private String[] getT737SourceTemplates(int sourceType) throws HCException  {
		// Get all source template data from T737 SORC_DEFN
		String[] templates = new String[3];
		ResultSet hreTable;
		String selectString = pointHREbase.setSelectSQL("*", "T737_SORC_DEFN", "SORC_DEFN_TYPE = " + sourceType);
		hreTable = pointHREbase.requestTabledata("T737_SORC_DEFN", selectString);
		try {
			hreTable.beforeFirst();
			while (hreTable.next()) {
					templates[0] = hreTable.getString("SORC_DEFN_FULLFOOT");
					templates[1] = hreTable.getString("SORC_DEFN_SHORTFOOT");
					templates[2] = hreTable.getString("SORC_DEFN_BIBLIO");
			}
			return templates;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HCException("get T737 data error: " + sqle.getMessage());
		}
	}

/**
 * getT738Number(String elementName)
 * @return
 * @throws HCException
 * @throws SQLException
 */
	private String getT738Number(String elementName) throws HCException  {
		// Get the T738 element number of the parameter elementName
		String number = "";
		ResultSet hreTable;
		String selectString = pointHREbase.setSelectSQL("*", "T738_SORC_ELMNT",
												"SORC_ELMNT_NAME = " + "'"+elementName + "'");
		hreTable = pointHREbase.requestTabledata("T738_SORC_ELMNT", selectString);
		try {
			hreTable.beforeFirst();
			while (hreTable.next()) {
					number = hreTable.getString("SORC_ELMNT_NUM");
			}
			return number;

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HCException("get T738 lookup error: " + sqle.getMessage());
		}
	}

/**
 * elementInfoExtract(String elementSourceInfo)
 * @param elementSourceInfo
 * @return String[][]
 */
	private String[][] elementInfoExtract(String elementSourceInfo) {
		// Extract out of the M.info string all text sitting between
		// the $!& markers and save in the List<String> texts.
		// We also identify the position (group) of each text entry in List(int> positions.
		// Combine these into Object[][] return
		Pattern pattern = Pattern.compile("\\$!&");
        Matcher matcher = pattern.matcher(elementSourceInfo);
        List<String> texts = new ArrayList<>();
        List<Integer> positions = new ArrayList<>();
        int delimiterCount = 0;

        while (matcher.find()) {
            delimiterCount++;
            int start = matcher.end();
         // Look ahead to find next delimiter or end of string
            int nextDelimiterStart = elementSourceInfo.indexOf("$!&", start);
            int end = (nextDelimiterStart != -1) ? nextDelimiterStart : elementSourceInfo.length();
            String between = elementSourceInfo.substring(start, end);
            if (!between.isEmpty()) {
                texts.add(between);
                positions.add(delimiterCount + 1); // +1 to shift position as 1st entry is #2
            }
        }
     // Build String[][] result
        String[][] result = new String[texts.size()][2];
        for (int i = 0; i < texts.size(); i++) {
            result[i][0] = texts.get(i);       // source Text
            result[i][1] = String.format("%02d", positions.get(i));   // Position, converted to 2-digit text
        }
        // Display results
//       for (String[] row : result)
//            System.out.println("Text: " + row[0] + ", Position: " + row[1]);
        return result;
	}

/**
 * extractElementNames(String sourceTemplate)
 * @param sourceTemplate
 * @return
 */
	private List<String> extractElementNames(String sourceTemplate) {
	// Extract all [xxx] strings from the parameter contaning all source templates.
	// Ensure all text is upper-case with no duplicated [xxx] in the final List
		return Pattern
	            .compile("\\[[^\\]]+\\]")       // match “[text]”
	            .matcher(sourceTemplate)
	            .results()                      // stream of all matches
	            .map(MatchResult::group)        // extract “[text]”
	            .filter(s -> !s.contains(":"))  // drop any “[...:]”
	            .map(s -> s.toUpperCase())    	// uppercase brackets + content
	            .distinct()                     // remove duplicates, keep first, retain order
	            .toList();                      // collect into immutable List

	}

/**
 * templateNameToNumber
 * @param template
 * @param nameList
 * @param numberList
 * @return
 */
	private String templateNameToNumber(String template, List<String> nameList, List<String> numberList) {
		// Build lookup map: "[text here]" → "12345"
		Map<String, String> replacementMap = new HashMap<>();
		for (int i = 0; i < nameList.size(); i++) {
			replacementMap.put(nameList.get(i), numberList.get(i));
		}

		// Regex to find all bracketed entries
		Pattern pattern = Pattern.compile("\\[[^\\]]+\\]");
		Matcher matcher = pattern.matcher(template);

		StringBuffer result = new StringBuffer();
		while (matcher.find()) {
			String match = matcher.group(); // e.g. "[this is an example]"
			String replacement = replacementMap.get(match);
			if (replacement != null) {
				matcher.appendReplacement(result, "[" + Matcher.quoteReplacement(replacement) + "]");
			} else {
				matcher.appendReplacement(result, Matcher.quoteReplacement(match));
			}
		}
		matcher.appendTail(result);
		return result.toString();
	}

/**
 * 	void testReposTables()
 */
	public void testReposTables() {
		if (sourceDump) System.out.println("\n**** Repos table: *******************************************");
		int nrOftmgRRows = tmgRtable.getNrOfRows();
		for (int index_R_Table = 0; index_R_Table < nrOftmgRRows; index_R_Table++) {
			try {
				String name = tmgRtable.getValueString(index_R_Table,"NAME").trim();
				String abbrev = tmgRtable.getValueString(index_R_Table,"ABBREV").trim();
				if (sourceDump) System.out.println(" " + index_R_Table  + " Repos: " + name + " - " + abbrev);
			} catch (HCException hce) {
				System.out.println("Repos error: " + hce.getMessage());
				hce.printStackTrace();
			}
		}
	}
/**
 * public void citationStat()
 */
	public void citationStat() {
		// int names, relationships, sources, events, places, citations;
		System.out.println(" Sources/Citations stats: ");
		System.out.println(" Names: " + names);
		System.out.println(" Relations: " + relationships);
		System.out.println(" Sources: " + sources);
		System.out.println(" Events: " + events);
		System.out.println(" Places: " + places);
		System.out.println(" Citations: " + citations );
		System.out.println(" S table rows: " + currentRow + "\n");
	}
}
