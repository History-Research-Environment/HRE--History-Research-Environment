package hre.tmgjava;
/****************************************************************************************
 * TMG to HRE converter PUBLIC VARIABLES
 * Global data
 ****************************************************************************************
 * v0.00.0017 2020-01-23 - included path to Seed Database
 * v0.00.0017 2020-01-23 - included path to last selected folder
 * v0.00.0017 2020-01-29 - included variable defDatabaseEngine = "H2";
 * v0.00.0021 2020-04-09 - included variable dataSetID for DSID test
 * v0.00.0022 2020-06-05 - Select database version implemented
 *			  2020-06-13 - databaseVersion = "v18c 2020-06-01" (N. Tolleshaug)
 * 			  2020-06-30 - Substitute \\ with / (N. Tolleshaug)
 * 			  2020-07-28 - Updated for DDL v20a 2020-07-26
 * v0.00.0025 2020-11-22 - File separator updated for file path strings (N. Tolleshaug)
 * v0.00.0027 2022-08-22 - included DDL v21g as seed for v21c (N. Tolleshaug)
 * v0.00.0028 2023-04-01 - Cleaned database version testing (N. Tolleshaug)
 * v0.00.0030 2023-08-15 - Included log file path for user/HRE (N. Tolleshaug)
 * 			  2023-08-31 - Added boolean FLAGCHECK control flag error (N. Tolleshaug)
 * v0.00.0031 2023-08-15 - Build 31 (N. Tolleshaug)
 * v0.00.0032 2024-12-21 - Build 32 (N. Tolleshaug)
 * 			  2024-12-22 - Updated for v22c database
 * 			  2025-02-11 - Added parameteres for initiate citation/source import (N. Tolleshaug)
 *****************************************************************************************/

import java.sql.ResultSet;

public class TMGglobal {

	public static String buildNo = "22.12.2024";				// Set Build number
	public static String releaseDate = "22 Des. 2024";		// and release date

	//public static String databaseVersion = "v22c 2024-10-20";	// Database version DDL
	public static String databaseVersion = "v22c 2024-12-19";	// Database version DDL
	
	//public static String databaseBuild = "v22c 2024-11-01"; // Updated T20X tables
	//public static String databaseBuild = "v22c 2024-11-01"; // Updated for v22c / v22c 2024-11-01
	public static String databaseBuild = "v22c 2024-12-19"; // Updated for v22c / SQL: DDL v22c 2024-12-19

	public static int dataSetID = 1; // Selected DATASET number from TMG folder

	public static boolean DEBUG = false;	 // DEBUG variable - to be set in App settings
	//public static boolean DEBUG = true;	 // DEBUG variable - to be set in App settings
	public static boolean TRACE = false;	 // Set up stack trace for debug to external file
	//public static boolean TRACE = true;	 // Set up stack trace for debug to external file
	
	// Switches for process monitor Output	
	//public static boolean TMGTRACE = false; // Controls TMG table load messages
	public static boolean TMGTRACE = true; // Controls TMG table load messages
	//public static boolean HRETRACE = false; // Controls HRE table load and index messages
	public static boolean HRETRACE = true; // Controls HRE table load and index messages
	
	public static boolean DUMP = false;		 // Dump meta data to System.out.println
	//public static boolean DUMP = true;     // Dump meta data to System.out.println
	public static boolean TRHDATE = false; // Dump Special HDATES to System.out.println
	//public static boolean TRHDATE = true;    // Dump Special HDATES to System.out.println
	public static boolean CONVERT_ALL_FLAG = false; // Set convert all flag data from C.dbf - PROPERTY

// Controls of name processing Upper/Lower case and display/sort string
	public static boolean LOWER_CASE = true; // Convert surname from  _ND.dbf to lower case
	//public static boolean LOWER_CASE = false; // Convert surname from  _ND.dbf to lower case

	//public static boolean UPDATE_SORT_DISP = true; // Update preloaded DISP or SORT
	public static boolean UPDATE_SORT_DISP = false; // Update preloaded DISP or SORT

// Control of exhibit processing
	public static boolean FLAGCHECK = false; // Statistics from flag processing
	//public static boolean FLAGCHECK = true; // Statistics from exhibit processing
	public static boolean EXHCHECK = false; // Statistics from exhibit processing
	//public static boolean EXHCHECK = true;    //Statistics from exhibit processing
	
	//public static boolean CONVERT_EXH_PATH = false; // Set new folder path for exhibits
	public static boolean CONVERT_EXH_PATH = true; // Set new folder path for exhibits
	
// ---------------------------------------------

	public static String systemLanguage;					// running OS System language code (e.g.'en-AU')
	public static String nativeLanguage;					// user's language (as may not = systemLanguage)(e.g.'en')
	public static String thisComputer;						// computer HRE is running on
	public static String defDatabaseEngine = "H2";	        // standard database engine

	public static String passWord = "abc123";
	public static String userName = "";						// User name as known by the OS

	// New exhibit path for test NTo
	public static String newExhibitFolderPath = "C:\\Users\\nils\\Documents\\The Master Genealogist v9\\Exhibits\\Ferguson\\";

	// path to HRE Help sub-folders	(Production)
	public static String helpPath = "file:\\C:\\Program Files\\HRE\\Help\\";

	// path to seed database
	public static String seedProjectFile = "C:\\Program Files\\HRE\\HRE Seed database.mv.db";

	// Path to last selected folder
	public static String pathProjectFolder = null;
	
// ****** Log file for for console output from converter enabled by TRACE
// ******-- Specific for user NTo - need to general for all Users ---********
	public static String logFile = "C:\\Users\\nils\\HRE\\NTo-B31-Log.txt";
// **************************************************************************
	public static String tmgStartFolder = "C:\\Users\\nils\\Documents\\The Master Genealogist v9\\Projects\\";
// DDL 22c **************************************************************************
	public static String tmghreBase22c = "C:/Users/nils/HRE/Project/HRE-TMG-v22c";
//****************************************************************************************
	public static String seedBase = "C:\\Users\\nils\\HRE\\HRE Seed database.mv.db";
// DDL 22c ************************** 	
	public static String seedBase22c = "C:\\Users\\nils\\HRE\\HRE Seed database v22c.mv.db";

/**
 * Table objects for TMG tables
 */
	public static TMGtableData  tmg_$_table = null;
	public static TMGtableData  tmg_A_table = null;
	public static TMGtableData  tmg_C_table = null;
	public static TMGtableData  tmg_D_table = null;
	public static TMGtableData  tmg_E_table = null;
	public static TMGtableData  tmg_F_table = null;
	public static TMGtableData  tmg_G_table = null;
	public static TMGtableData  tmg_I_table = null;
	public static TMGtableData  tmg_M_table = null;
	public static TMGtableData  tmg_N_table = null;
	public static TMGtableData  tmg_NPV_table = null;
	public static TMGtableData  tmg_NPT_table = null;
	public static TMGtableData  tmg_ND_table = null;
	public static TMGtableData  tmg_P_table = null;
	public static TMGtableData  tmg_PD_table = null;
	public static TMGtableData  tmg_PPT_table = null;
	public static TMGtableData  tmg_PPV_table = null;
	public static TMGtableData  tmg_R_table = null;
	public static TMGtableData  tmg_S_table = null;
	public static TMGtableData  tmg_ST_table = null;
	public static TMGtableData  tmg_T_table = null;
	public static TMGtableData  tmg_U_table = null;
	public static TMGtableData  tmg_W_table = null;



/**
 * ResultSet objects for HRE database
 */
	public static ResultSet T101 = null;
	public static ResultSet T102 = null;
	public static ResultSet T104 = null;
	public static ResultSet T131 = null;
	public static ResultSet T160 = null;
	public static ResultSet T162 = null;
	public static ResultSet T167 = null;
	public static ResultSet T170 = null;
	public static ResultSet T204 = null; // T204_FLAG_TRAN
	public static ResultSet T251 = null;
	public static ResultSet T252 = null;
	public static ResultSet T401 = null;
	public static ResultSet T402 = null;
	public static ResultSet T403 = null;
	public static ResultSet T404 = null;
	public static ResultSet T405 = null;
	public static ResultSet T450 = null;
	public static ResultSet T451 = null;
	public static ResultSet T452 = null;
	public static ResultSet T453 = null;
	public static ResultSet T460 = null;
	public static ResultSet T461 = null;
	public static ResultSet T463 = null;
	public static ResultSet T551 = null;
	public static ResultSet T552 = null;
	public static ResultSet T553 = null;
	public static ResultSet T676 = null;
	public static ResultSet T677 = null;
	public static ResultSet T735 = null;
	public static ResultSet T736 = null;

	public static int numOpenProjects = 0;					// count of number of currently open projects

	public static String selectedProject = "";				// for saving current project amongst Delete, Backup, Rename, SelectProj, Viewpoints
	public static String chosenFolder, chosenFilename;		// for saving the folder/filename selected in 0577FileChooser for use in New, Open, Delete, CopyAs, Restore
	public static String copyfromFolder, copyfromFilename,  // for from folder/filenames selected in 0577FileChooser for CopyAs
						 copytoFolder, copytoFilename,		// for to folder/filenames selected in 0577FileChooser for CopyAs
						 zipFromFolder, zipFromFilename,
						 projectFolder, projectFilename;  // holds from/to folder/filenames for backup zip files
	public static String fromExtFolder, fromExtFile; 		// holds file/foldernames for external (zipped) file to be restored
															// structure is: Projectname, Filename, Folder, Server, Lastclosed, DBtype
	//***************************************************************************************************************************************

	// Following item is the default GUI font
	public static String dfltGUIfont = "Arial";


	//*****************************************************************************************************************************************

}