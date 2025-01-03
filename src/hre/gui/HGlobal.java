package hre.gui;
/************************************************************************
 * PUBLIC VARIABLES MODULE
 * Split off from HG0401HREMain 2019-05-17
 **
 * (NB: for the purists who don't believe Java has 'global' variables,
 * we really don't care what you call these, but they look and act
 * like global variables to us)
 * v0.00.0018 2020-01-23 included paths to Seed Database and last selected folder
 *			  2020-01-29 included variable defDatabaseEngine = "H2";
 * v0.00.0020 2020-03-17 moved userCred to UserAUX area;
 *                       added Regex validation routines in Common Code.
 * 			 2021-01-28 removed use of HGlobal.selectedProject (N. Tolleshaug)
 * v0.01.0026 2021-05-16 Included both DDL20a and DDL21a (N. Tolleshaug)
 * 			  2021-06-16 Modified Accents entries (D Ferguson)
 * 			  2021-08-07 Added fontName setting (D Ferguson)
 * v0.01.0029 2023-05-02 Common Code split off into HGlobalCode (D Ferguson)
 * v0.03.0031 2024-02-15 New variable: boolean importedData = true/false (N. Tolleshaug)
 * 			  2024-04-14 Removed variable importedData added 2024-02-15 (N. Tolleshaug)
 * 			  2024-10-12 Added boolean reloadPS user option (D Ferguson)
 * 			  2024-11-15 Added boolean married name prompt option (D Ferguson)
 * v0.04.0032 2024-12-22 Updated for Build 32 and database v22c (N. Tolleshaug)
 ************************************************************************/

import java.awt.Color;
import java.awt.Window;
import java.util.ArrayList;

import hre.bila.HBProjectOpenData;

/**
 * Common data for HRE (hre.bila, hre.gui and hre.dbla)
 * @author Don Ferguson
 * @since 2020-01-23 - HRE Build 18
 * @version build 0.04.3225.0101
 */
public class HGlobal {
	public static String buildNo = "0.04.3225.0101";		// Set Build number as V.vv.BBYY.MMDD //$NON-NLS-1$
	public static String releaseDate = "1 Jan 2025";		// and release date to match //$NON-NLS-1$

	public static String databaseVersion = "v22c";		    // Set Database DDL version //$NON-NLS-1$

	public static boolean DEBUG = false;				    // DEBUG variable - set in App settings, not saved
	public static boolean TIME = false;				        // TIME variable - set in App settings; enables timing msgs to console
	public static boolean pluginLog = false;				// Log output from PluginManager

	public final static int maxPersonVPIs = 5;				// Set all maximum VP counts
	public final static int maxLocationVPIs = 5;
	public final static int maxEventVPIs = 5;
	public final static int maxVPtotal = 15;

	public static String osType;							// OS Name from System.getProperties (lower-cased)
	public static String systemLanguage;					// running OS System language code (e.g.'en-AU')
	public static String nativeLanguage;					// user's language (as may not = systemLanguage)(e.g.'en')
	public static String dataLanguage = "en-US";			// language for use for data //$NON-NLS-1$
	public static String data2Language = "en-US";			// alt. language for use for data //$NON-NLS-1$
	public static String reptLanguage = "en-US";			// language for use for reports //$NON-NLS-1$
	public static String thisComputer;						// computer HRE is running on
	public static String defDatabaseEngine = "H2";	        // standard database engine //$NON-NLS-1$
	public static String userID = "";						// UserID as known by the OS, copied to userCred[0] //$NON-NLS-1$

	public static String passWord = "";						// Store used password remote login //$NON-NLS-1$
	public static String remoteID = "";						// Store used remote user login  //$NON-NLS-1$

    public static Window mainWindowInstance;				// to save MainMenu instance so Viewpoint JIFs can still access Help
	public static Color returnColor;						// for interface to the ColorChooser

	// path to HRE Help sub-folders	(Production)
	public static String helpPath = "file:\\C:\\Program Files\\HRE\\app\\Help\\";			// default Windows location //$NON-NLS-1$
	public static String helpPathMac = "file:///Applications/HRE.app/Contents/app/Help/";	// MacOS location //$NON-NLS-1$
	public static String helpPathUX = "file:///opt/hre/lib/app/Help/";						// Linux location //$NON-NLS-1$

	// path for KeyStore folder (Production)
	public static String keyStorePath = "C:\\Program Files\\HRE\\app\\KeyStore\\"; 	 		// default Windows location //$NON-NLS-1$
	public static String keyStorePathMac = "/Applications/HRE.app/Contents/app/KeyStore/"; 	// MacOS location //$NON-NLS-1$
	public static String keyStorePathUX = "/opt/hre/lib/app/KeyStore/";					 	// Linux location //$NON-NLS-1$

	// location of seed database (Production)
	public static String seedProjectFile = "C:\\Program Files\\HRE\\app\\HRE Seed database.mv.db";			// default Windows location //$NON-NLS-1$
	public static String seedProjectMac = "/Applications/HRE.app/Contents/app/HRE Seed database.mv.db";		// MacOS location //$NON-NLS-1$
	public static String seedProjectUX = "/opt/hre/lib/app/HRE Seed database.mv.db";						// Linux location //$NON-NLS-1$

	// location of Sample database (Production)
	public static String sampleProjectFile = "C:\\Program Files\\HRE\\app\\HRE Sample database.mv.db";		// default Windows location //$NON-NLS-1$
	public static String sampleProjectMac = "/Applications/HRE.app/Contents/app/HRE Sample database.mv.db";	// MacOS location //$NON-NLS-1$
	public static String sampleProjectUX = "/opt/hre/lib/app/HRE Sample database.mv.db";					// Linux location //$NON-NLS-1$

	public static String currentLogFile = "";				// Current log file as setup by HB0711Logging
	public static int numOpenProjects = 0;					// count of number of currently open projects (max allowed = 3)
	public static boolean statusWindowOpen = false;			// to track if the Project Status window is already open (allow only 1)
	public static String closeType = "N";					// to track how close is being done (from screen X or ExitHRE or Close Project) (N = not set) //$NON-NLS-1$

	public static String chosenFolder, chosenFilename;		// for saving the folder/filename selected in 0577FileChooser for use in New, Open, Delete, CopyAs, Restore
	public static String copyfromFolder, copyfromFilename,  // for from folder/filenames selected in 0577FileChooser for CopyAs
						 copytoFolder, copytoFilename,		// for to folder/filenames selected in 0577FileChooser for CopyAs
						 zipFromFolder, zipFromFilename,
						 projectFolder, projectFilename;    // holds from/to folder/filenames for backup zip files
	public static String fromExtFolder, fromExtFile; 		// holds file/foldernames for external (zipped) file to be restored

	public static String currentServer = "";				// for defining current project's server //$NON-NLS-1$
	public static String selectedServer = "";				// for defining server selected for login //$NON-NLS-1$

	// A list of open Projects (a subset of userProjects)
	// Structure is same as userProjects: Projectname, Filename, Folder, Servername, Lastclosed, DBtype, LastBackup
	public static ArrayList<HBProjectOpenData> openProjects = new ArrayList<>();

//*********************************************************************************************************************
// Following Definitions are for the UserAUX file content
// NB: startup userServers & userProjects values are set by HB0744UserAUX

	public static boolean tcpServerOn = false;				// for tracking Server mode (on/off)

	//Following items for LAF settings
	public static int lafNumber = 0;						// for storing LAF name index value
	public static int lafThemeNumber = 0;					// for storing LAF Theme index value
	public static String lafTheme = "Default";				// for storing LAF Theme name //$NON-NLS-1$
	public static String fontName = "Dialog";				// for storing Font name; default=JTattoo's choice //$NON-NLS-1$

	//Following items for user credentials
	public static String[] userCred = {"","","",""};		// for storing userID, name, password, email address //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	// Following items are User application settings
	public static boolean showWelcome = true;				// whether HRE Welcome screen shown at startup (default true)
	public static boolean showCancelmsg = true;				// whether Cancel messages are shown to user (default true)
	public static boolean openLastProject = false;			// whether Last used project opened at HRE startup (default false)
	public static boolean backupActivProject = false;		// whether to auto-backup Active Project at close (default false)
	public static boolean pluginEnabled = true;				// whether plugins are loaded at open project(default true)
	public static boolean reloadPS = true;					// whether to always reload Person Selector (default true)
	public static boolean promptMarrName = false;			// whether to prompt to enter a Married Name (default false)
	public static boolean writeLogs = true;					// whether external Log files are written (default true)

	// Following items relate to the window bounds for positioning mainMenu at startup
	public static int mainX = 0;							// main menu x point - if 0, is centred at default startup
	public static int mainY = 0;							// main menu y point - if 0, is centred at default startup
	public static int mainW = 1200;							// main menu Width - initial size used in HG0401HREMain
	public static int mainH = 700;							// main menu Height - initial size used in HG0401HREMain

	// Following are default HRE file locations
	public static String pathHRElocation = "";				// default HRE database location //$NON-NLS-1$
	public static String pathHREreports = "";				// default HRE report output location //$NON-NLS-1$
	public static String pathHREbackups = "";				// default HRE backups location //$NON-NLS-1$
	public static String pathExtbackups = "";				// default HRE external backup file location //$NON-NLS-1$

	// Following are miscellaneous other items
	public static String pathProjectFolder = null;			// Path to last selected folder
	public static String lastProjectClosed ="";				// name of last project closed - for opening at HRE restart //$NON-NLS-1$

	// Following items are user settings
	public static String dateFormat = "dd Mmm yyyy";		// default date display format //$NON-NLS-1$
	public static String timeFormat = "HH.mm.ss.msec";		// default time display format (24Hr with msecs) //$NON-NLS-1$
	public static String lifespan = "110";					// default assumed max. lifespan //$NON-NLS-1$

	// Following are the Accent tables and Mode setting
	public static ArrayList<String> accentNames = new ArrayList<>(); 			// for user-supplied Accent names
	public static ArrayList<String> accentRules = new ArrayList<>(); 			// for user-supplied Accent rules
	public static ArrayList<Color> accentFillColors = new ArrayList<>();		// for Accent background color
	public static ArrayList<Color> accentFontColors = new ArrayList<>();		// for Accent font (foreground) color
	public static ArrayList<Color> accentBorderColors = new ArrayList<>();		// for Accent border color
	public static boolean accentOn = true;										// for Accent mode (on/off)

	// Following are arrays for Servers and Projects known to this User
	// Structure is Servername, TCP/IP address, Port number
	public static ArrayList<String[]> userServers = new ArrayList<>();

	// list of Projects known to this User (initially HRE Sample)
	// Structure is: Projectname, Filename, Folder, Servername, Lastclosed, DBtype, LastBackup
	public static ArrayList<String[]> userProjects = new ArrayList<>();

// End of UserAUX file content
//*****************************************************************************************************************

}