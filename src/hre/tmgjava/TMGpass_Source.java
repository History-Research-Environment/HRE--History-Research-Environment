package hre.tmgjava;
/**
 * TMGpass_V22a_Source
 * Convert source data from TMG to HRE
 * @author NTo - Nils Tolleshaug
 * @since 2023-08-15
 * @see document
 */
public class TMGpass_Source {
	HREdatabaseHandler pointHREbase;
	TMGtableData  tmgAtable = null;
	TMGtableData  tmgMtable = null;
	TMGtableData  tmgRtable = null;
	TMGtableData  tmgStable = null;
	TMGtableData  tmgUtable = null;
	TMGtableData  tmgWtable = null;
	int currentRow = 0;
	// On/off dump of source
	boolean sourceDump = false;
	//boolean sourceDump = true;
	
/**
 * Constructor TMGpass_V22a_Source
 * @param pointHREbase
 */
	TMGpass_Source(HREdatabaseHandler pointHREbase) {
		this.pointHREbase = pointHREbase;
		tmgAtable = TMGglobal.tmg_A_table;
		tmgMtable = TMGglobal.tmg_M_table;
		tmgRtable = TMGglobal.tmg_R_table;
		tmgStable = TMGglobal.tmg_S_table;
		tmgUtable = TMGglobal.tmg_U_table;
		tmgWtable = TMGglobal.tmg_W_table;
	}
	
/**
 * 	testSourceTables()
 */
	public void testSourceTables() {
		if (sourceDump) System.out.println("\n**** Source table: *******************************************");
		int nrOftmgSRows = tmgStable.getNrOfRows();
		for (int indexS_PID = 0; indexS_PID < nrOftmgSRows; indexS_PID++) {
			currentRow = indexS_PID + 1;
			try {
				String sourceRefTable = tmgStable.getValueString(indexS_PID,"STYPE");
				if (sourceRefTable.equals("E")) {
					int majSource = tmgStable.getValueInt(indexS_PID,"MAJSOURCE");
					String sourceTitle = tmgMtable.findValueString(majSource,"TITLE");
					String sourceText = tmgMtable.findValueString(majSource,"TEXT");
					int sourceType = tmgMtable.findValueInt(majSource,"TYPE");
					String sourceName = tmgAtable.findValueString(sourceType,"NAME");
					//if (majSource == 81)
					if (sourceDump) System.out.println(" " + indexS_PID  + " Source E: " + sourceRefTable   + "/" + sourceType 
							+ "  Name: " + sourceName + "\n     Title: " + sourceTitle  
							+ "\n     Text: " + sourceText);
				}
			} catch (HCException hce) {
				System.out.println("Source error: " + hce.getMessage());
				hce.printStackTrace();
			}
			
		}
	}
	
	public void testReposTables() {
		if (sourceDump) System.out.println("\n**** Repos table: *******************************************");
		int nrOftmgRRows = tmgRtable.getNrOfRows();
		for (int indexR_PID = 0; indexR_PID < nrOftmgRRows; indexR_PID++) {
			try {
				String name = tmgRtable.getValueString(indexR_PID,"NAME").trim();	
				String abbrev = tmgRtable.getValueString(indexR_PID,"ABBREV").trim();
				if (sourceDump) System.out.println(" " + indexR_PID  + " Repos: " + name + " - " + abbrev);
			} catch (HCException hce) {
				System.out.println("Repos error: " + hce.getMessage());
				hce.printStackTrace();
			}
		}
	}
}
