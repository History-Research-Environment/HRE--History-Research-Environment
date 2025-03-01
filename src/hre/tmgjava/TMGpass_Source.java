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
 ***********************************************************************************/
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * TMGpass_V22a_Source
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
	int sourceNumber, majSource, sourceType, sourceRefId;
	
// Counters for citation table types
	int names, relationships, sources, events, places, citations; 
	
	 
	 ResultSet tableT735_CITN, tableT736_SORC;
	 
// Index for TMG to HRE PID for events
	public HashMap<Integer,Long> eventIndexPID = new HashMap<Integer, Long>();
	
/**
 * Constructor TMGpass_V22a_Source
 * @param pointHREbase
 */
	TMGpass_Source(HREdatabaseHandler pointHREbase) {
		this.pointHREbase = pointHREbase;
		tableT735_CITN = TMGglobal.T735;
		tableT736_SORC = TMGglobal.T736;
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
				sourceTypeName = tmgAtable.findValueString(sourceType,"NAME");
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
					addToCitationT735_CITN( index_S_Table, citationTablePID, tableT735_CITN);
					names++;
					continue;
				} else
				if (sourceRefTable.equals("F")) {
					relationships++;
					continue;
				} else
				if (sourceRefTable.equals("M")) {
					sources++;
					continue;
				} else
				if (sourceRefTable.equals("E")) {
					citedRecordRPID = eventIndexPID.get(referedRecord);
					citedTableName = "T450";
					addToCitationT735_CITN( index_S_Table, citationTablePID, tableT735_CITN);	
					events++;
					continue;
				} else
				if (sourceRefTable.equals("P")) {
					citedRecordRPID = referedRecord + proOffset;
					citedTableName = "T552";
					citationTablePID = sourceNumber + proOffset;
					addToCitationT735_CITN( index_S_Table, citationTablePID, tableT735_CITN);
					places++;
					continue;
				} else
				if (sourceRefTable.equals("C")) {
					citations++;
					continue;
				} else System.out.println(" " + currentRow + " not found type: " + sourceRefTable);
				
			} catch (HCException hce) {
				System.out.println("Source error: " + hce.getMessage());
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
		long sourceTablePID;
		if (sourceDump) System.out.println("\n**** Source table: *******************************************");
		int nrOftmgRRows = tmgMtable.getNrOfRows();
		for (int index_M_Table = 0; index_M_Table < nrOftmgRRows; index_M_Table++) {
			try {
				sourceTablePID = tmgMtable.getValueInt(index_M_Table, "MAJNUM") + proOffset;
				sourceRefId = tmgMtable.getValueInt(index_M_Table, "REF_ID");
				sourceTitle = tmgMtable.getValueString(index_M_Table, "TITLE");			
				abbrevTitle = tmgMtable.getValueString(index_M_Table, "ABBREV");
				sourceType = tmgMtable.getValueInt(index_M_Table, "TYPE");
				sourceText = tmgMtable.getValueString(index_M_Table,"TEXT");
			// Add new source record	
				addToSourceT736_SORC(index_M_Table, sourceTablePID, tableT736_SORC);
			} catch (HCException hce) {
				System.out.println("Source error: " + hce.getMessage());
				hce.printStackTrace();
			}
		}
	}
	
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
	private void addToCitationT735_CITN(int index_S_Table, long T735tablePID, ResultSet hreTable) {	
		try {				
		    // moves cursor to the insert row
				hreTable.moveToInsertRow();
			// Update new row in H2 database
				hreTable.updateLong("PID", T735tablePID);
				hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
				hreTable.updateLong("CITED_RPID", citedRecordRPID);
				hreTable.updateString("OWNER_TYPE", citedTableName );	
				hreTable.updateLong("SORC_RPID", majSource + proOffset);
				hreTable.updateLong("ASSESSOR_RPID", null_RPID);
				
			//Create a T167_MEMO_SET record with citiation detials
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
				hreTable.updateString("CITN_ACC_NAME",tmgStable.getValueString(index_S_Table,"SNSURE"));
				hreTable.updateString("CITN_ACC_DATE",tmgStable.getValueString(index_S_Table,"SDSURE"));
				hreTable.updateString("CITN_ACC_LOCN",tmgStable.getValueString(index_S_Table,"SPSURE"));
				hreTable.updateString("CITN_ACC_MEMO",tmgStable.getValueString(index_S_Table,"SFSURE"));
			//Insert row
				hreTable.insertRow();
		} catch (SQLException sqle) {
			
			sqle.printStackTrace();
			errorCount++;
		} catch (HCException hce) {

			hce.printStackTrace();
		}	
	}
/**
 * 		
 * @param indexPID
 * @param hreTable
 * CREATE TABLE T736_SORC( 
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
				);
 */
	private void addToSourceT736_SORC(int index_M_Table, long sourceTablePID, ResultSet hreTable) {
	    // moves cursor to the insert row
			try {
				hreTable.moveToInsertRow();
				hreTable.updateLong("PID", sourceTablePID);
				hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
				hreTable.updateBoolean("IS_ACTIVE", tmgMtable.getValueBoolean(index_M_Table,"MACTIVE"));
				hreTable.updateInt("SORC_REF", sourceRefId);
				hreTable.updateInt("SORC_TYPE", sourceType);
			// Source fidelity
				int sourceFidleity = tmgMtable.getValueInt(index_M_Table,"FIDELITY");
				if (sourceFidleity == 1) hreTable.updateString("SORC_FIDELITY","E");
				else if (sourceFidleity == 2) hreTable.updateString("SORC_FIDELITY","A");
				else if (sourceFidleity == 3) hreTable.updateString("SORC_FIDELITY","B");
				else if (sourceFidleity == 4) hreTable.updateString("SORC_FIDELITY","C");
				else if (sourceFidleity == 5) hreTable.updateString("SORC_FIDELITY","D");
				else if (sourceFidleity < 1 || sourceFidleity > 5)
					System.out.println(" Fidelity: " + sourceFidleity + " not found");
				
				//Create a T167_MEMO_SET record with the citation memo
				sourceMemo = HREmemo.returnStringContent(tmgMtable.getValueString(index_M_Table,"TEXT"));
			// Processing memo to T167_MEMO_SET
				if (sourceMemo.length() == 0) hreTable.updateLong("SORC_TEXT_RPID", null_RPID);
				else hreTable.updateLong("SORC_TEXT_RPID",
						tmgHreConverter.pointHREmemo.addToT167_22c_MEMO(sourceMemo));
				
		//Check the following RPID value towards PID for T401 
				hreTable.updateLong("SORC_AUTHOR_RPID", proOffset + tmgMtable.getValueInt(index_M_Table,"SPERNO"));				
				hreTable.updateLong("SORC_EDITOR_RPID", proOffset + tmgMtable.getValueInt(index_M_Table,"EDITORID"));		
				hreTable.updateLong("SORC_COMPILER_RPID", proOffset + tmgMtable.getValueInt(index_M_Table,"COMPILERID"));
		// End		
				hreTable.updateString("SORC_ABBREV",tmgMtable.getValueString(index_M_Table,"ABBREV"));  
				hreTable.updateString("SORC_TITLE",tmgMtable.getValueString(index_M_Table,"TITLE"));	
				hreTable.updateString("SORC_FULLFORM",
						HREmemo.returnStringContent(tmgMtable.getValueString(index_M_Table,"FFORM")));
				hreTable.updateString("SORC_SHORTFORM",
						HREmemo.returnStringContent(tmgMtable.getValueString(index_M_Table,"SFORM")));
				hreTable.updateString("SORC_BIBLIOFORM",
						HREmemo.returnStringContent(tmgMtable.getValueString(index_M_Table,"BFORM")));	
			//Create a T167_MEMO_SET record with the remider
				sourceRemind = HREmemo.returnStringContent(tmgMtable.getValueString(index_M_Table,"TEXT"));
			// Processing memo to T167_MEMO_SET
				if (sourceRemind.length() == 0) hreTable.updateLong("SORC_REMIND_RPID", null_RPID);
				else hreTable.updateLong("SORC_REMIND_RPID",
						tmgHreConverter.pointHREmemo.addToT167_22c_MEMO(sourceRemind));
			//Insert row
				hreTable.insertRow();		
			} catch (SQLException sqle) {
				// TODO Auto-generated catch block
				sqle.printStackTrace();
			} catch (HCException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
/**
 * public void citationStat()
 */
	public void citationStat() {
		// int names, relationships, sources, events, places, citations;
		System.out.println(" Sources/Citiations stats: "); 
		System.out.println(" Names: " + names);
		System.out.println(" Relations: " + relationships);
		System.out.println(" Sources: " + sources);
		System.out.println(" Events: " + events);
		System.out.println(" Places: " + places);
		System.out.println(" Citations: " + citations );
		System.out.println(" S table rows: " + currentRow + "\n");
	}
}
