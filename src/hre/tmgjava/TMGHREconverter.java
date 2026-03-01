package hre.tmgjava;
/******************************************************************************************
 * Uses library com.linuxense.javadbf
 * Java library for reading and writing Xbase (dBase/DBF) files
 * https://github.com/albfernandez/javadbf
 * albfernandez/javadbf is licensed under the
 * GNU Lesser General Public License v3.0
 * Written by Alberto Fern�ndez
 * *****************************************************************************************
 * TMGHREconverter initiate the conversion process
 * v0.00.0018 2020-03-05 - First version (N. Tolleshaug)
 * v0.00.0019 2020-03-15 - Updated with progress monitor
 * v0.00.0019 2020-03-16 - Improved progress monitor with SwingWorker
 * v0.00.0019 2020-03-18 - Implemented for HRE database v17b4 2020-02-11
 * v0.00.0019 2020-03-20 - Read the .pjc file from TMG and check version
 * v0.00.0021 2020-04-08 - Open TMG folder and choose .pjc file
 * v0.00.0021 2020-04-10 - Import from HRE without new seed copy
 * v0.00.0021 2020-04-28 - Exception delete handling database in use
 * v0.00.0021 2020-05-01 - Included monitoring of TMG table load
 * v0.00.0021 2020-05-01 - Implemented update of T404_BIO_NAMES (N. Tolleshaug)
 * v0.00.0021 2020-05-07 - Updated process monitor status messages (N. Tolleshaug)
 * v0.00.0022 2020-06-13 - databaseVersion = "v18c 2020-06-01" (N. Tolleshaug)
 * 			  2020-06-13 - Checks version in Seed database (N. Tolleshaug)
 * 			  2020-06-30 - Implemented test of HDate from TMG date string
 * 			  2020-07-30 - Updated for DDL v20a 2020-07-26
 * v0.00.0025 2020-10-25 - Birth and death place to HRE event tables (N. Tolleshaug)
 * 			  2020-11-15 - Removed DDL19a (N. Tolleshaug)
 * 			  2020-12-29 - Changed to T552_LOCATION_NAMES for placedata (N. Tolleshaug)
 * v0.00.0027 2022-01-24 - Implemented Exhibit processing (N. Tolleshaug)
 * 			  2022-02-27 - Implemented handling event tags, witness and roles(N. Tolleshaug)
 * 			  2022-05-17 - Implemented progress monitor for load TMG tables(N. Tolleshaug)
 *  		  2022-05-24 - Release all TMG tables rsouces after exhibit load (N. Tolleshaug)
 *  		  2022-05-26 - Rearranged updated "BEST_IMAGE_RPID (N. Tolleshaug)
 *  		  2022-05-24 - Forced close of TMG data after person data processing (N. Tolleshaug)
 *  		  2022-08-23 - Included DDL 21g as new seed for DDL21 (N. Tolleshaug)
 *  		  2023-01-15 - Included DDL 21k as new seed for DDL21 (N. Tolleshaug)
 *  		  2023-02-13 - Modified table name T751 to T170 DDL21 (N. Tolleshaug)
 *  		  2023-02-15 - Implemented create new thumbs for exhibits (N. Tolleshaug)
 *  		  2023-02-28 - New pass 2 for location, events in pass 3 (N. Tolleshaug)
 *  		  2023-04-01 - Cleaned database version testing (N. Tolleshaug)
 * v0.02.0029 2023-05-01 - Implemented v22a (N. Tolleshaug)
 * v0.03.0030 2023-05-01 - Get TMG language fixed for v22a (N. Tolleshaug)
 * v0.03.0031 2023-10-20 - Updated for v22b database
 * 			  2023-10-20 - Loading Source/Citation TMG tables
 * 			  2023-10-20 - Removed all v22a handling
 * 			  2023-11-03 - Updated for new associate event processing
 * 			  2024-10-20 - Removed finally console and status printout (N. Tolleshaug)
 *			  2024-11-25 - Convert "\\" usage to File.separator (D Ferguson)
 *			  2024-12-12 - Updated for seed v22c database (N. Tolleshaug)
 *v0.04.0032  2025-02-11 - Code for initiate citation/source import (N. Tolleshaug)
 *			  2025-06-30 - Added lines for build number (N. Tolleshaug)
 *			  2025-07-12 - Added remaining source_def/elemnt, repo and link tables (N. Tolleshaug)
 *			  2025-07-21 - Changed sequenece of source table processing (N. Tolleshaug)
 *			  2025-07-22 - Imported project ruleset from pjc file (N. Tolleshaug)
 *			  2025-09-01 - Add new source table process (updateT737Templates) (D Ferguson)
 *			  2025-09-19 - Change TMGLanguage code to find most common language (D Ferguson)
 * 			  2026-01-15 - Log catch block msgs (D Ferguson)
 * 			  2026-02-08 - Added TMG table dump to user HRE folder (N. Tolleshaug)
 *v0.05.0033  2026-02-16 - Correct default language setting if no language found (D Ferguson)
 * 			  2026-02-16 - Added if (TMGglobal.DEBUG) in stopTCPSever()
 			  2026-02-28 - Fixed default language setting if no language found (D Ferguson)
 *******************************************************************************************/

import java.awt.Dialog.ModalityType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
//import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.gui.HGlobal;

/**
 * class TMGconverter
 * Initiate the TMG to HRE conversion process
 * @author NTo - Nils Tolleshaug
 * @since 2020-03-05
 * @see document
 */
public class TMGHREconverter extends SwingWorker<String, String> {

	private TMGloader tmgLoader;
	protected HREloader_V22c hreLoader;
	protected HREmemo pointHREmemo;
	protected TMGpass_Support pointSupportPass;
	protected TMGpass_Source pointSourcePass;
	public TMGpass_Persons  pointPersonPass;
	private TMGpass_Locations  pointLocationPass;
	private TMGpass_Events  pointEventPass;
	private TMGpass_Exhibits pointExhibitPass;
	protected TMGHREprogressMonitor processMonitor;
	private static HREserverH2tcp tcpServ;
	private String statusMessage = "ended";
	private int totalNumberPasses = 5;
	private int completedNumberPasses = 0;
	long oneMilliSec = 1000L;
	long startTime;
	long timeElapsed;

	private static String tmgStartFolder;
	private static String tmghreDataBase;
	private static String seedBase;

	public static String tmgProjectName;
	private static String tmgFolderPath;

	private static String tmgVersion;
	private static String tmgNativeLang;

	private String ruleSet;

/**
 * Set Dump of TMG table content based on selected table name
 * @throws HCException
*/
	private void dumpTMGtable() throws HCException {
			String reportDump;
			// reportDump = TMGtypes.DATA_SET;
	    	// reportDump = TMGtypes.PERSON;
	    	// reportDump = TMGtypes.DATA_SET;
	    	// reportDump = TMGtypes.EVENT;
			// reportDump = TMGtypes.EXHIBIT;
	    	// reportDump = TMGtypes.NAME;
	    	// reportDump = TMGtypes.PLACE;
	    	//reportDump =  TMGtypes.PLACE_DICTIONARY;
	    	// reportDump =  TMGtypes.PLACE_PART_TYPE;
	    	// reportDump =  TMGtypes.PLACE_PART_VALUE;
			   reportDump = TMGtypes.STYLE;
	    	// reportDump = TMGtypes.TAG_TYPE;

		//Load tables if needed
	    	tmgLoader.testTmgTables(reportDump);
	    	closeAllTMGtables("DUMP test");
	    	System.out.println();
	}

/**
 * languageTMG() - returns the found native language
 * @return native language
 */
	public String languageTMG() {
		return tmgNativeLang;
	}

/**
 * constructors TMGHREconverter(TMGHREprogressMonitor pointMonitor, String dataBasePath)
 * @param pointMonitor
 * @param dataBasePath
 */
	public TMGHREconverter(TMGHREprogressMonitor pointMonitor, String dataBasePath) {
		this.processMonitor = pointMonitor;
		tmghreDataBase = dataBasePath;
		tmgStartFolder = TMGglobal.tmgStartFolder;
		seedBase = TMGglobal.seedBase;
	}

/**
 * TMGHREconverter(String databasePath)
 * @param databasePath
 * @throws HCException
 */
	public TMGHREconverter(String databasePath) throws HCException {
		tmghreDataBase = databasePath;
		tmgStartFolder = TMGglobal.tmgStartFolder;
		seedBase = TMGglobal.seedBase22c;
	// Copy seed database
		try {
			updateSeedDatabase(seedBase);
		} catch (HCException hre) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in TMGHREconverter copying Seed database: " + hre.getMessage());
				HB0711Logging.printStackTraceToFile(hre);
			}
			throw new HCException("\"copySeedDatabase error: " + hre.getMessage());
		}
	}

/**
 * Do on background
 */
	@Override
	protected String doInBackground() throws Exception {
		startConvert();
		return "ended";
	}

/**
 * 	Executed in event dispatching thread
 */
	@Override
	public void done() {
		if (TMGglobal.DEBUG) System.out.println("TMGHREconverter - " + statusMessage);
		processMonitor.endConvert(statusMessage);
	}

/**
 * set status in monitor
 * @param percent
 */
	public void setStatusProgress(int percent) {
		setProgress(percent);
	}

	public void setStatusMessage(String message) {
		processMonitor.setContextOfAction(message);
	}

/**
 * 	startConvert()
 */
	private void startConvert() {

		try {
			processMonitor.setContextOfAction(" Processing with build: " + TMGglobal.buildNo);
			processMonitor.setContextOfAction(" Processing with rel: " + TMGglobal.releaseDate);
			processMonitor.setContextOfAction(" Database version: " + TMGglobal.databaseVersion);
			if (TMGglobal.DEBUG) System.out.println(" *** Choose TMG folder");
			chooseTMGfolder();
			if (TMGglobal.DEBUG)
				System.out.println(" *** TMG Tables - PJC file path: "
					+ TMGglobal.chosenFolder + " / Filename: " + TMGglobal.chosenFilename);

	// Read TMG - PJC file data
			tmgProjectName = TMGglobal.chosenFilename.replace("__.pjc", " ");
			tmgProjectName = tmgProjectName.replace("__.PJC", " ");
			tmgProjectName = tmgProjectName.toLowerCase().trim();

	// Special processing of Tolleshaug projects
			if (tmgProjectName.contains("tolleshaug")) {
				processMonitor.setContextOfAction(" For Tolleshaug convert all surnames to lowercase!");
				TMGglobal.LOWER_CASE = true;
			}

			tmgFolderPath = TMGglobal.chosenFolder;
			String plcFilePath = tmgFolderPath + File.separator + tmgProjectName + "__" + ".pjc";
			processMonitor.setContextOfAction(" PJC at: " + plcFilePath);

			String [] pjcData = readPJCfile(plcFilePath);

			processMonitor.setStatus(completedNumberPasses, totalNumberPasses);

			processMonitor.setContextOfAction(" Project name: " + TMGglobal.chosenFilename);

	// Write PJC data to minitor
			for (String element : pjcData) {
				processMonitor.setContextOfAction(" " + element);
			}

			tmgVersion = pjcData[2].substring(11,13);

			if (TMGglobal.DEBUG) System.out.println("TMG version collected from PJC file: "
					+ tmgVersion);

		// Check TMG database version
			if (!tmgVersion.equals("10") && !tmgVersion.equals("11")) throw new HCException(" Old TMG database version - not accepted");
			processMonitor.setContextOfAction(" Database PJC version accepted ");


	if (TMGglobal.TRACE)
			System.out.println(" *** TMG to HRE converter startup"
				+ "\n *** TMG PJC file path: " + plcFilePath
				+ "\n *** HREbase folder path: " + tmghreDataBase
				+ "\n *** Program version: " + TMGglobal.buildNo
				+ "\n *** Program rel. date: " + TMGglobal.releaseDate
				+ "\n *** HRE DB version: " + TMGglobal.databaseVersion
				+ "\n");

	// Generate TMG tables
			startTime = System.currentTimeMillis();
			processMonitor.setContextOfAction("\n Start converting TMG to HRE");
			generateTMGtables();

	// Locate TMG native language
			tmgNativeLang = locateTMGlanguage();
			processMonitor.setContextOfAction(" *** TMG native language: " + tmgNativeLang);
			System.out.println(" TMG language: " + tmgNativeLang);

	// Generate HRE Tables;
			processMonitor.setContextOfAction(" Loading HRE tables ");
			generateHREtables();

    // Check DDL version
			String databaseBuild = hreLoader.getDatabaseVersion();
			processMonitor.setContextOfAction(" *** Uses HRE database build: " + databaseBuild);

	// Check database version
			if (TMGglobal.DEBUG)
				System.out.println(" DB version: " + databaseBuild + "/" + TMGglobal.databaseBuild);

			if (!databaseBuild.contains(TMGglobal.databaseBuild))
				throw new HCException(" Mismatch in HRE database build\n"
						+ "expected: " + TMGglobal.databaseBuild);

			timeReport("loading HRE tables");

	// Dump of TMG table content based on selected table name
			if (TMGglobal.DUMP) dumpTMGtable();

	// Set up memo handler
			pointHREmemo = new HREmemo(hreLoader.getDataBasePointer());
			pointSourcePass = new TMGpass_Source(hreLoader.getDataBasePointer(), ruleSet);

	// SUPPORT pass
			passSupportData();

	// PERSON passes
			passPersonData();

	// LOCATION passes
			passLocationData();

	// EVENT passes
			passEventData();

	// SOURCE passes
			passSourceData();

	// Close all HRE tables except Exhibit Table
			hreLoader.closeHREtables();
			processMonitor.setContextOfAction(" HRE tables closed");

    // Exhibit passes
			passExhibitData();

	// Coversion finished
			resetHREtables();

	//Close database
			timeReport("before close of database");
			hreLoader.closeDatabase();
			processMonitor.setContextOfAction(" HRE database closed");
			processMonitor.setContextOfAction(" Questions Marks = " + HREhdate.getQuestionMarks());
			processMonitor.setContextOfAction(" Old Style Dates = " + HREhdate.getOldStyleDates());
			processMonitor.setContextOfAction(" Irregular Dates = " + HREhdate.getNrIrrDates());
			if (TMGglobal.TRHDATE) HREhdate.printSpecialDates();

			timeReport("finishing data processing");

			if (TMGglobal.DEBUG) {
				tcpServ = new HREserverH2tcp(); //create a new server object
				tcpServ.tcpServer(); //starts the tcp server
			}

	    	// Release all TMG tables
			closeAllTMGtables("All");

			reporNameStyleUse();

		} catch (HCException hce) {
			processMonitor.setContextOfAction(" ERROR - HRE conversion terminated!");
			if (TMGglobal.DEBUG) System.out.println("Converter TMG error\n" + hce.getMessage());
			JOptionPane.showMessageDialog(null,	"TMG Convert error:"
					+ "\nError: " + hce.getMessage(),"TMG to HRE",JOptionPane.INFORMATION_MESSAGE);
			if (TMGglobal.TRACE) hce.printStackTrace();
		// Log Stack Trace
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in TMGHREconverter conversion: " + hce.getMessage());
				HB0711Logging.printStackTraceToFile(hce);
			}
		} catch (Exception exc) {
			processMonitor.setContextOfAction(" PROGRAM EXCEPTION - HRE conversion terminated!");
			JOptionPane.showMessageDialog(null,	"TMG-HRE Converter error detected.\nSee log for more info."
					+ "\nException message: " + exc.getMessage(),"TMG to HRE",JOptionPane.INFORMATION_MESSAGE);
			// Log Stack Trace
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in TMGHREconverter conversion: " + exc.getMessage());
				HB0711Logging.printStackTraceToFile(exc);
			}
		}  finally {
			System.out.println(" TMG to HRE conversion completed!");
	    }
	}

/**
 * Release all TMG tables
 * void closeAllTMGtables()
 */
	private void closeAllTMGtables(String message) {
		if (TMGglobal.tmg_$_table != null) TMGglobal.tmg_$_table.closeTMGtable();
		if (TMGglobal.tmg_D_table != null) TMGglobal.tmg_D_table.closeTMGtable();
		if (TMGglobal.tmg_E_table != null) TMGglobal.tmg_E_table.closeTMGtable();
		if (TMGglobal.tmg_F_table != null) TMGglobal.tmg_F_table.closeTMGtable();
		if (TMGglobal.tmg_G_table != null) TMGglobal.tmg_G_table.closeTMGtable();
		if (TMGglobal.tmg_I_table != null) TMGglobal.tmg_I_table.closeTMGtable();
		if (TMGglobal.tmg_N_table != null) TMGglobal.tmg_N_table.closeTMGtable();
		if (TMGglobal.tmg_NPV_table != null) TMGglobal.tmg_NPV_table.closeTMGtable();
		if (TMGglobal.tmg_NPT_table != null) TMGglobal.tmg_NPT_table.closeTMGtable();
		if (TMGglobal.tmg_ND_table != null) TMGglobal.tmg_ND_table.closeTMGtable();
		if (TMGglobal.tmg_P_table != null) TMGglobal.tmg_P_table.closeTMGtable();
		if (TMGglobal.tmg_PD_table != null) TMGglobal.tmg_PD_table.closeTMGtable();
		if (TMGglobal.tmg_PPT_table != null) TMGglobal.tmg_PPT_table.closeTMGtable();
		if (TMGglobal.tmg_PPV_table != null) TMGglobal.tmg_PPV_table.closeTMGtable();
		if (TMGglobal.tmg_ST_table != null) TMGglobal.tmg_ST_table.closeTMGtable();
		if (TMGglobal.tmg_T_table != null) TMGglobal.tmg_T_table.closeTMGtable();
		TMGglobal.tmg_$_table = null;
		TMGglobal.tmg_E_table = null;
		TMGglobal.tmg_F_table = null;
		TMGglobal.tmg_G_table = null;
		TMGglobal.tmg_I_table = null;
		TMGglobal.tmg_N_table = null;
		TMGglobal.tmg_NPV_table = null;
		TMGglobal.tmg_NPT_table = null;
		TMGglobal.tmg_ND_table = null;
		TMGglobal.tmg_P_table = null;
		TMGglobal.tmg_PD_table = null;
		TMGglobal.tmg_PPT_table = null;
		TMGglobal.tmg_PPV_table = null;
		TMGglobal.tmg_ST_table = null;
		TMGglobal.tmg_T_table = null;
		if (TMGglobal.DEBUG) System.out.println(" TMG " + message + " tables closed");
		processMonitor.setContextOfAction("TMG " + message + " tables closed");
	}

/**
 * stopTCPServer()
 */
	public void stopTCPServer() {
		if (TMGglobal.DEBUG)
			if (tcpServ.server != null) tcpServ.stopTCPserver();
	}

/**
 * 	reporNameStyleUse()
 */
	private void reporNameStyleUse() {
		String styleReport;
		if (pointSupportPass == null) return;
		Set<Integer> styleKeys = pointSupportPass.getNameStyleHashMap().keySet();

	// Sort keys ascending
		styleKeys = new TreeSet<>(styleKeys);
		Integer[] keyList =  styleKeys.toArray(new Integer[0]);

	// Report statistics
		if (TMGglobal.DUMP) System.out.println("\n Use of Person and Location Name Styles:");
		processMonitor.setContextOfAction("*** Person and Location Name Style report:");
		for (Integer element : keyList) {
			styleReport = " StyleID: " + element +
					" Name: " + pointSupportPass.getNameStyleHashMap().get(element).getNameType() +
					" - " + pointSupportPass.getNameStyleHashMap().get(element).getStyleName() + " " +
					" Use: " + pointSupportPass.getNameStyleHashMap().get(element).getNameStyleUse();
			if (TMGglobal.DUMP) System.out.println(styleReport);
			processMonitor.setContextOfAction(styleReport);
		}
	}

/**
 * timeReport(String eventMessage)
 * @param eventMessage
 */
	private void timeReport(String eventMessage) {
		timeElapsed = System.currentTimeMillis() - startTime;
		processMonitor.setTransferTime((double)timeElapsed / oneMilliSec);
		processMonitor.setContextOfAction(" Used " + eventMessage + ": " + (double)timeElapsed / oneMilliSec + " sec");
	}

/**
 * 	chooseTMGfolder()
 */
	public void chooseTMGfolder() {
		TMGfolderChooser chooseFile = new TMGfolderChooser("Select", "TMG files (*.pjc)", "pjc", null, tmgStartFolder, 1);
		chooseFile.setModalityType(ModalityType.APPLICATION_MODAL);
		chooseFile.setLocation(600, 200);  // Sets chooser screen top-left corner relative to that
		chooseFile.setVisible(true);
	}

/**
 * readPJCfile(String fileName)
 * @param fileName
 * @throws HCException
 * *******************Eaxmple PJC content***********************************
 * Name =Nils Tolleshaug
 * MappingCountry=Norway
 * PjcVersion=11
 * RestoredByVersion=v 9.05.0000
 * CreateDate=20040113
 * CreateTime=07:47:26 PM
 * LastIndexed=03/08/2020
 * LastVFI=03/08/2020
 * LastOptimized=03/08/2020
 *********************************************************/
	@SuppressWarnings("resource")
	public String[] readPJCfile(String filePath) throws HCException {
		  String [] data = new String[10];
		  data[0] = "Researcher's Name not found";
		  data[1] = "Mapping Country not found";
		  data[2] = "PJC Version not found";
		  data[3] = "Restored By not found";
		  data[4] = "Create Date not found";
		  data[5] = "Create Time not found";
		  data[6] = "Last Indexed not found";
		  data[7] = "Last VFI not found";
		  data[8] = "Last Optimized not found";
		  data[9] = "Source ruleset not found";

		  File file = new File(filePath);
		  BufferedReader buffer;
		  try {
				buffer = new BufferedReader(new FileReader(file));
				String string;
				while ((string = buffer.readLine()) != null)  {
					if (string.startsWith("Name")) data[0] = string;
					if (string.startsWith("MappingCountr")) data[1] = string;
					if (string.startsWith("PjcVersion")) data[2] = string;
					if (string.startsWith("RestoredByVersion")) data[3] = string;
					if (string.startsWith("CreateDate")) data[4] = string;
					if (string.startsWith("CreateTime")) data[5] = string;
					if (string.startsWith("LastIndexed")) data[6] = string;
					if (string.startsWith("LastVFI")) data[7] = string;
					if (string.startsWith("LastOptimized")) data[8] = string;
					if (string.startsWith("SourceRule")) data[9] = string;
					ruleSet = data[9];

				}
				return data;
		  } catch (FileNotFoundException fnfe) {
			  if (TMGglobal.DEBUG) System.out.println("File not found\n" + fnfe.getMessage());
			  if (TMGglobal.TRACE) fnfe.printStackTrace();
				if (HGlobal.writeLogs)
					HB0711Logging.logWrite("ERROR: in TMGHREconverter file not found: " + fnfe.getMessage());
			  throw new HCException("File not found\n" + fnfe.getMessage());
		  } catch (IOException ioe) {
			  if (TMGglobal.DEBUG) System.out.println("File read error\n" + ioe.getMessage());
			  if (TMGglobal.TRACE) ioe.printStackTrace();
				if (HGlobal.writeLogs)
					HB0711Logging.logWrite("ERROR: in TMGHREconverter IO exception: " + ioe.getMessage());
			  throw new HCException("File read error\n" + ioe.getMessage());
		  }
	}

/**
 * locateTMGlanguage()
 * @return
 */
	private String locateTMGlanguage() {
		String wsentance = "", language = "";
		List<String> possibles = new ArrayList<>();
		try {
			tmgLoader.loadTmgEventTables(false);
			TMGtableData tmgEtable = TMGglobal.tmg_E_table;
			int nrOftmg$Rows = tmgEtable.getNrOfRows();
			// Extract [LANGUAGE= settings in E.dbf into List of possible languages
			for (int index = 0; index < nrOftmg$Rows; index++) {
				wsentance = HREmemo.returnStringContent(tmgEtable.getValueString(index,"WSENTENCE"));
				if (wsentance.trim().length() > 20) {
					int endPos = wsentance.indexOf("][");
					if (endPos > 0)
						possibles.add(wsentance.substring(3,endPos));
				}
			}
			// Now find the most common entry in the list of possibles
			String mostCommon = possibles.stream()
	                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
	                .entrySet().stream().max((o1, o2) -> o1.getValue().compareTo(o2.getValue()))
	                .map(Map.Entry::getKey).orElse(null);
			if (mostCommon == null) {
				mostCommon = "ENGLISH";
				System.out.println(" TMG project Language not found; set to ENGLISH");
				if (HGlobal.writeLogs)
					HB0711Logging.logWrite("Action: in TMGHREconverter language defaulted to ENGLISH");
			}
			else {
				System.out.println(" TMG project language found: " + mostCommon);
				if (HGlobal.writeLogs)
					HB0711Logging.logWrite("Action: in TMGHREconverter language found as "+ mostCommon);
			}
			language = HREmemo.getLangCode(mostCommon).trim();
			TMGglobal.tmg_E_table.closeTMGtable();
		} catch (HCException hce) {
			if (TMGglobal.TRACE) hce.printStackTrace();
			if (HGlobal.writeLogs)
				HB0711Logging.logWrite("ERROR: in TMGHREconverter locating TMG language: " + hce.getMessage());
		}
		return language;
	}

/**
 * generateTMGtables()
 * @throws HCException
 */
	private void generateTMGtables() throws HCException {
		setStatusMessage(" Loading TMG dataset table");
		tmgLoader = new TMGloader(this, tmgFolderPath, tmgProjectName);
		processMonitor.setProgress(0);
	}

/**
 * generateHREtables()
 * @throws HCException
 */
	private void generateHREtables() throws HCException {
		hreLoader = new HREloader_V22c(tmghreDataBase,this);
		processMonitor.setProgress(0);
	}
/**
 * resetHREtables()
 * @throws HCException
 */
	private void resetHREtables() throws HCException {
		processMonitor.setContextOfAction(" Start reload HRE tables");
		hreLoader.reloadHreTables();
	}

	private void passSupportData() throws HCException {
		setStatusMessage("*Support pass - HRE name table processing");
		setStatusMessage(" Loading TMG style tables");
		tmgLoader.loadTmgSupportTables();
		timeReport("load TMG style tables");
		HREdatabaseHandler pointDB = hreLoader.getDataBasePointer();

		pointSupportPass = new TMGpass_Support(pointDB);

		setStatusMessage(" Updating HRE T16X_NAME_STYLES");
		pointSupportPass.addNameStyleToHRE(this);
		timeReport("updated T160_NAME_STYLES");

		setStatusMessage(" Updating HRE T251_FLAG_DEFN");
		pointSupportPass.updateFlagFromTMG(this);
		timeReport("updated T251_FLAG_DEFN");
	}

/**
 * Pass person data to HRE database
 * @throws HCException
 */
	private void passPersonData() throws HCException {
		setStatusMessage("** 1st - pass - HRE name table processing");
		setStatusMessage(" Loading TMG name tables");
		tmgLoader.loadTmgNameTables();
		timeReport("load TMG Name tables");
		HREdatabaseHandler pointDB = hreLoader.getDataBasePointer();

		pointPersonPass = new TMGpass_Persons(pointDB);

		try {

		// HRE T401_PERSONS
				setStatusMessage(" Updating HRE T401_PERS");
				pointPersonPass.addPersonsToHRE(this);
				timeReport("updating HRE T401_PERS");

		// HRE T402_PERSON_NAMES
				setStatusMessage(" Updating HRE T402_PERS_NAME");
				pointPersonPass.addNamesToHRE(this);
				timeReport("updating HRE T402_PERS_NAME");

		// HRE T405_PERSON_BITHS
				setStatusMessage(" Updating T405_PARENT_RELATION");
				pointPersonPass.addParentRelationToHRE(this);
				timeReport("updating HRE T405_PARENT_RELATION");

		// Test name parts
			if (TMGglobal.DUMP) pointPersonPass.displayNamesParts();

/**
 * Release Table objects for TMG tables
 */
			TMGglobal.tmg_$_table.closeTMGtable();
            TMGglobal.tmg_F_table.closeTMGtable();
			TMGglobal.tmg_NPV_table.closeTMGtable();
			TMGglobal.tmg_NPT_table.closeTMGtable();
			TMGglobal.tmg_ND_table.closeTMGtable();

			TMGglobal.tmg_$_table = null;
			TMGglobal.tmg_F_table = null;
			TMGglobal.tmg_G_table = null;
			TMGglobal.tmg_I_table = null;
	// N table is needed in Event processing - not now
			TMGglobal.tmg_N_table = null;
			TMGglobal.tmg_NPV_table = null;
			TMGglobal.tmg_NPT_table = null;
			TMGglobal.tmg_ND_table = null;
			TMGglobal.tmg_P_table = null;
			TMGglobal.tmg_PD_table = null;
			TMGglobal.tmg_PPT_table = null;
			TMGglobal.tmg_PPV_table = null;
			TMGglobal.tmg_T_table = null;

			completedNumberPasses++;
			processMonitor.setStatus(completedNumberPasses,totalNumberPasses);

		} catch (HCException hce) {
			System.out.println("passPersonData error: " + hce.getMessage());
			statusMessage = "ERROR";
			JOptionPane.showMessageDialog(null, "PassPersonData error: \n"
					+  hce.getMessage(), "Project HRE pass",JOptionPane.ERROR_MESSAGE);
		// Log Stack trace
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in TMGHREconverter passPersonData: " + hce.getMessage());
				HB0711Logging.printStackTraceToFile(hce);
			}
		}
	}

	private void passLocationData() throws HCException {
		setStatusMessage("** 2nd - pass - HRE place tables processing");
		setStatusMessage(" Loading TMG place tables");
		processMonitor.setProgress(0);
		tmgLoader.loadTmgPlaceTables();
		timeReport("load TMG Place tables");
/**
 *  Processing Places fromTMG
*/
		setStatusMessage(" Processing  Places from TMG");
		HREdatabaseHandler pointDB = hreLoader.getDataBasePointer();

		pointLocationPass = new TMGpass_Locations(pointDB);

		try {
			pointLocationPass.addLocationsToHRE(this);
			setStatusMessage(" Completed T551, T552 and T553 LOCATIONS");
			timeReport("HRE place data processing");
			completedNumberPasses++;
			processMonitor.setStatus(completedNumberPasses, totalNumberPasses);

			closeAllTMGtables("Place");

		} catch (HCException hce) {
			System.out.println(" ERROR PassPlaceData: " + hce.getMessage());
			JOptionPane.showMessageDialog(null, "ERROR PassLocationtData: \n"
					+  hce.getMessage(), "Place pass",JOptionPane.ERROR_MESSAGE);
		// Log Stack Trace
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in TMGHREconverter passLocationData: " + hce.getMessage());
				HB0711Logging.printStackTraceToFile(hce);
			}
		}

	}

/**
 * passEventData to HRE	database
 * @throws HCException
 */
	private void passEventData() throws HCException {
		setStatusMessage("** 3rd - pass - HRE event tables processing");
		setStatusMessage(" Loading TMG event tables");
		processMonitor.setProgress(0);
		tmgLoader.loadTmgEventTables(true);
		timeReport("load TMG Event tables");

/**
 *  Processing Events fromTMG
*/
		setStatusMessage(" Processing Events from TMG");
		HREdatabaseHandler pointDB = hreLoader.getDataBasePointer();

		pointEventPass = new TMGpass_Events(pointDB);

		try {

			pointEventPass.addEventTagTables(this);
			setStatusMessage(" Completed EVENT TAG table");

			pointEventPass.addEventTable(this);
			setStatusMessage(" Completed EVENT table");

			pointEventPass.addEventsAssocTable(this);
			setStatusMessage(" Completed table EVENT_ASSOC");

			pointEventPass.updatePartnerRoles();
			setStatusMessage(" Updated table PARTNER WITH ASSOC ROLES");

			pointEventPass.updateBirthEvents();
			setStatusMessage(" Updated table PARENT_RELATION");
			processMonitor.setStatus(completedNumberPasses, totalNumberPasses);

/**
 * Release Table objects for TMG tables
*/
			closeAllTMGtables("Event");

			timeReport("HRE event data processing");
			completedNumberPasses++;
			processMonitor.setStatus(completedNumberPasses,totalNumberPasses);

		} catch (HCException hce) {
			System.out.println(" ERROR PassEventData: " + hce.getMessage());
			JOptionPane.showMessageDialog(null, "ERROR PassEventData: \n"
					+  hce.getMessage(), "Event pass",JOptionPane.ERROR_MESSAGE);
			// Log Stack Trace
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in TMGHREconverter passEventData: " + hce.getMessage());
				HB0711Logging.printStackTraceToFile(hce);
			}
		}
	}

/**
 * passSourceData()
 * @throws HCException
 */
	private void passSourceData() throws HCException {
		setStatusMessage("** 4th - pass - HRE source table processing");
		setStatusMessage(" Loading TMG source tables");
		tmgLoader.loadTmgSourceTables();
		timeReport("load TMG Source tables");

	// Copy TMG source tables
		pointSourcePass.initSourceTables(); // from TMG A, M, R, S, U, W tables
	// Import Source tables
		pointSourcePass.addToSourceElementTable(this);	// Process T738 SORC_ELEMENT
		pointSourcePass.addToSourceDefnTable(this); 	// Build T737 SORC_DEFN
		pointSourcePass.addToSourceTable(this);			// Build T736 SORC, T734 SORC_DATA
		pointSourcePass.updateT737Templates(this);		// Update T737 source template formats
		pointSourcePass.addToCitationTable(this);		// Build T735 CITN
		pointSourcePass.addToReposTable(this);			// Build T739 REPO
		pointSourcePass.addToSorceLinkTable(this);		// Build T740 SORC_LINK

		pointSourcePass.testReposTables();
		pointSourcePass.citationStat();
		completedNumberPasses++;
		processMonitor.setStatus(completedNumberPasses,totalNumberPasses);
	}

/**
 * passEventData to HRE	database
 * @throws HCException
 */
	private void passExhibitData() throws HCException {
		setStatusMessage("** 5th - pass - HRE exhibit table processing");
		setStatusMessage(" Loading TMG exhibit table");
		processMonitor.setProgress(0);
		tmgLoader.loadTmgExhibitTables();
		timeReport("load TMG Exhibit tables");
/**
 *  Processing Exhibits fromTMG
*/
		setStatusMessage(" Processing Exhibits from TMG");
		HREdatabaseHandler pointDB = hreLoader.getDataBasePointer();

		pointExhibitPass = new TMGpass_Exhibits(pointDB);

		try {
			pointExhibitPass.addExhibitsToHRE(this);
			closeAllTMGtables("Exhibits");
			pointExhibitPass.bestImageProcessing();
			timeReport("HRE Exhibit data processing");
			completedNumberPasses++;
			processMonitor.setStatus(completedNumberPasses,totalNumberPasses);

		// Close T402_PERSON_NAME table
			TMGglobal.T676.close();

		} catch (HCException | SQLException hce) {
			System.out.println("Pass Exhibit Data error: " + hce.getMessage());
			statusMessage = "ERROR";
			JOptionPane.showMessageDialog(null, "Pass Exhibit Data error: \n"
					+  hce.getMessage(), "Event pass",JOptionPane.ERROR_MESSAGE);
			// Log Stack Trace
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in TMGHREconverter passExhibitData: " + hce.getMessage());
				HB0711Logging.printStackTraceToFile(hce);
			}
		}
}

/**
 * errorDataBaseVersion(String passName)
 * @param passName

	private void errorDataBaseVersion(String passName) {
		setStatusMessage(" -> Pass? " + passName + " DB not accepted: " + TMGglobal.databaseVersion);
	}
*/
/**
 * updateSeedDatabase()
 * @throws HCException
 */
	private void updateSeedDatabase(String seedBase) throws HCException {
		String copyTo = tmghreDataBase + ".mv.db";
		if (TMGglobal.DEBUG) System.out.println("updateSeedDatabase - copy seed to: " + copyTo);
	// Delete earlier updated database
		deleteFile(copyTo);
	// Copy seed database
		copyFile(seedBase, copyTo);
	}

/**
 * Copy file to new location
 * Source file not deleted
 * Example:
 * Files.copy(Paths.get("C:/Users/nils/HRE/HRE Seed DB v16d.mv.db"),
 *		Paths.get("C:/Users/nils/Documents/Utvikling/HRE-project/Alfatest-HRE/Databases/Seed-v16d.mv.db"));
 * @throws HCException
 */
	private void copyFile(String sourceFilePath,String copyFilePath) throws HCException  {
        try {
			Files.copy(Paths.get(sourceFilePath),
    				Paths.get(copyFilePath));
        } catch (NoSuchFileException nsfe) {
        	if (TMGglobal.DEBUG) System.out.println("No such file: \n" + nsfe.getMessage());
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in TMGHREconverter no file exists: " + nsfe.getMessage());
				HB0711Logging.printStackTraceToFile(nsfe);
			}
			throw new HCException("No such file: \n" + nsfe.getMessage());
        } catch (FileAlreadyExistsException fae) {
        	if (TMGglobal.DEBUG) System.out.println("File already exist: \n" + fae.getMessage());
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in TMGHREconverter file already exists: " + fae.getMessage());
				HB0711Logging.printStackTraceToFile(fae);
			}
			throw new HCException("File already exist: \n" + fae.getMessage());
        } catch (IOException ioe) {
        	if (TMGglobal.DEBUG) System.out.println("Copy file IOException: \n" + ioe.getMessage());
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in TMGHREconverter IOexception on copy file: " + ioe.getMessage());
				HB0711Logging.printStackTraceToFile(ioe);
			}
			throw new HCException("Copy file IOException: \n" + ioe.getMessage());
		}
	}

/**
 * Delete file in folder
 * @param deleteFilePath
 * @throws HBException
 */
	public void deleteFile(String deleteFilePath) throws HCException {
        try {
        // also delete trace file if it exists
        	if (deleteFilePath.endsWith("trace.db")) Files.deleteIfExists(Paths.get(deleteFilePath));
        	else Files.deleteIfExists(Paths.get(deleteFilePath));

        } catch (NoSuchFileException nsfe) {
        	if (TMGglobal.DEBUG) System.out.println("No such file: \n" + nsfe.getMessage());
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in TMGHREconverter no file exists: " + nsfe.getMessage());
				HB0711Logging.printStackTraceToFile(nsfe);
			}
			throw new HCException("Cannot delete non existing file: \n" + nsfe.getMessage());
        } catch (IOException ioe) {
        	if (TMGglobal.DEBUG) System.out.println("Delete file IOException: \n" + ioe.getMessage());
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in TMGHREconverter IOexception on delete file: " + ioe.getMessage());
				HB0711Logging.printStackTraceToFile(ioe);
			}
			throw new HCException("Delete file IOException: \n" + ioe.getMessage());
		}
	}

/**
 * main(String args[])
 * @param args
 */
	public static void main(String args[]) {
		String tmghreDataBase = "";
		try {
		// Send console output to file.
			if (TMGglobal.TRACE) redirectOutput(TMGglobal.logFile);

		// Test HDATE convert
			//if (TMGglobal.TRACE) testHdateConvert();

		// Dump TMG table to HRE folder
			if (TMGglobal.TMGFILEDUMP) {
				new TMGfileDump(true);
				return;
			}

		// Select database
			if (!TMGglobal.databaseVersion.startsWith("v22c")) throw new HCException(" MAIN - HRE database version not accepted");
			tmghreDataBase = TMGglobal.tmghreBase22c;

			TMGHREprogressMonitor conv = new TMGHREprogressMonitor();
			conv.startMonitor();
			conv.startConversion(tmghreDataBase);
			new TMGHREconverter(tmghreDataBase);

		} catch (HCException | IOException hce) {
			JOptionPane.showMessageDialog(null, "TMG converter Main error: \n"
					+  hce.getMessage(), "TMG converter",JOptionPane.ERROR_MESSAGE);
			// Log Stack Trace
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in TMGHREconverter Main: " + hce.getMessage());
				HB0711Logging.printStackTraceToFile(hce);
			}
			if (TMGglobal.TRACE) hce.printStackTrace();
		}
	}

/**
 * redirectOutput(String fileName)
 * @param fileName
 * @throws IOException
 */
   public static void redirectOutput(String fileName) throws IOException {
	   //Instantiating the File class
	      File file = new File(fileName);
	   //Instantiating the PrintStream class
	      PrintStream stream = new PrintStream(file);
	      System.out.println("Now "+ file.getAbsolutePath()
	      		+ " receive TMG converter console output");
	      System.setOut(stream);
	      System.setErr(stream);
	   //Printing values to file
	      System.out.println("TMG-HRE Console output:");
	}

}
