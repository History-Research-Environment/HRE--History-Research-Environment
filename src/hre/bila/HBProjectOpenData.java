package hre.bila;
/*************************************************************************************
 * Store all project data for an open project
 *************************************************************************************
 * v0.00.0016 2019-12-13 - First version (N. Tolleshaug)
 * v0.00.0018 2020-02-24 - Corrected error opening empty database or no
 * 						   content in database tables (N. Tolleshaug)
 * v0.00.0019 2020-02-26 - add lastBackup field to userProjects (D. Ferguson)
 * v0.00.0019 2020-03-19 - Added temporary call to Person Table
 * 							Present person data in window line 108,109 and 110
 *							intFrame = presentPersonTable(dataBaseIndex);
 *							pointProHand.addInternaFrameToWindow(intFrame);
 * v0.00.0019 2020-03-20 - Moved temporary call to Person Table over to viewPoint
 * v0.00.0021 2020-04-12 - Initiate ResultSets for HRE tables (N. Tolleshaug)
 * v0.00.0021 2020-04-15 - Error fix: close ResultSets for HRE tables (N. Tolleshaug)
 * v0.00.0021 2020-04-27 - Moved OK message to after load  HRE tables (N. Tolleshaug)
 * v0.00.0022 2020-05-23 - Updated open database and debug printout (N. Tolleshaug)
 * v0.00.0022 2020-05-23 - Cleaned up code for get database version (N. Tolleshaug)
 * v0.00.0022 2020-05-24 - Person selected transferred to HBProjectOpenData instance
 * 						   to corresponding project open data (N. Tolleshaug)
 * 			  2020-06-13 - Loading of T751_HDATES implemented (DDL v18c)
 * 			  2020-07-30 - Implemented loading of DDL v20a
 * 			  2020-08-01 - HGlobal.databaseversion set from HBProject open
 * v0.01.0023 2020-08-12 - Loads T175 data and collects T204 data for T175
 * 			  2020-09-01 - Implemented read/write reminder to database (N Tolleshaug)
 * 			  2020-09-08 - Improved update GUI config update in database (N Tolleshaug)
 * 			  2020-09-09 - Read/Store frame position and size from T302 (N Tolleshaug)
 *  		  2020-09-10 - Moved initiatePersonSelect to HBPersonHandler (N. Tolleshaug)
 *  		  2020-09-24 - Implemented 3x Viewpoints reopen screen (N. Tolleshaug)
 *  		  2020-10-03 - Errors corrected position + size setting (N. Tolleshaug)
 *  		  2020-10-03 - Error fixed init HRE with T302 table empty (N. Tolleshaug)
 *            2020-10-03 - Close project implementing HG0507xxxxxxSelect
 *            			    with JInternalFrame (N. Tolleshaug)
 *            2020-10-04 - Reset VP counters when opening project (N. Tolleshaug)
 * v0.01.0025 2020-11-01 - Storing of people/location tables for reload (N. Tolleshaug)
 * 			  2020-11-07 - Make database Open info msg a Debug option (D Ferguson)
 * 			  2021-01-20 - Reset person ID and multiple PersonViewPoint (N. Tolleshaug)
 * 			  2021-01-28 - removed use of HGlobal.selectedProject (N. Tolleshaug)
 * 			  2021-02-07 - Implemented column control HG0581ConfigureTable (N. Tolleshaug)
 *            2021-03-06 - Implemented handling of 5 Location VP (N. Tolleshaug)
 * v0.01.0026 2021-05-16 - Included both DDL20a and DDL21a (N. Tolleshaug)
 * 			  2021-05-27 - Opening of remote connection ip/port in dbtype (N. Tolleshaug)
 * 			  2021-06-22 - Preload of ResultSet pointT553_LOCNAME (N. Tolleshaug)
 * 			  2021-07-07 - Pre-loading and indexing of name element tables (N. Tolleshaug)
 * 			  2021-10-01 - Handling of local/remote connect to database (N. Tolleshaug)
 * v0.01.0027 2021-11-10 - Removed HashMap for name and place element table (N. Tolleshaug)
 * 			  2021-11-10 - Removed HashMap for HDATE table (N. Tolleshaug)
 *  		  2021-12-11 - Included DDL20a, DDL21a and DDL21c (N. Tolleshaug)
 *  		  2022-01-17 - Reset last selected person IDX (N. Tolleshaug)
 *  		  2022-03-12 - Implemented T201 for storing language setting (N. Tolleshaug)
 *  		  2022-02-25 - Initiate personPID map from visibleID (N. Tolleshaug)
 * 			  2022-08-02 - Implemented handling of HG0508ManageLocation (N. Tolleshaug)
 * 			  2022-08-23 - Accepts DDL v21g as valid database (N. Tolleshaug)
 * v0.01.0028 2022-10-01 - Changed date translation to v22h tables (N. Tolleshaug)
 * 			  2022-12-16 - Store name style index in T302 (N. Tolleshaug)
 * 			  2023-03-11 - Reset T302 if corrupt when reopen windows (N. Tolleshaug)
 * 			  2023-03-20 - Reset ResultSet for T401 if updated table (N. Tolleshaug)
 * 			  2023-04-01 - Cleaned database version testing (N. Tolleshaug)
 * 			  2023-04-10 - Activate person, VP and location manager for each project (N. Tolleshaug)
 * v0.03.0030 2023-06-03 - Activated database v22a (N. Tolleshaug)
 * 			  2023-09-04 - Add Dutch to getGuiLanguage code (D Ferguson)
 * v0.03.0031 2024-03-17 - Modified code for person pick list Person Select min (D Ferguson)
 * 			  2024-04-14 - Added processing of boolean "IS_IMPORTED" from T126 (N. Tolleshaug)
 * 			  2024-08-18 - Added clear window pointer (N. Tolleshaug)
 * 			  2024-10-05 - Added set window pointer (N. Tolleshaug)
 ********************************************************************************************/

import java.awt.Container;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
//import java.util.HashMap;
import java.util.HashMap;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
//************************************
import javax.swing.JOptionPane;
//************************************
import javax.swing.JTable;
import javax.swing.JTree;

import hre.dbla.HDException;
import hre.gui.HG0401HREMain;
import hre.gui.HG0450SuperDialog;
import hre.gui.HG0451SuperIntFrame;
import hre.gui.HG0452SuperFrame;
import hre.gui.HG0506ManagePerson;
import hre.gui.HG0507LocationSelect;
import hre.gui.HG0507PersonSelect;
import hre.gui.HG0508ManageLocation;
import hre.gui.HGlobal;
import hre.gui.HGlobalCode;
import hre.tmgjava.TMGglobal;

/**
 * Stores all open project data for an HRE project
 * @author Nils Tolleshaug
 * @version v0.03.0030
 * @since 2019-12-14
 */
public class HBProjectOpenData {

	public HBProjectHandler pointProjectHandler;
	private HBViewPointHandler pointViewPointHandler;
	private HBPersonHandler pointPersonHandler;
	private HBWhereWhenHandler pointWhereWhenHandler;
	private HBMediaHandler pointMediaHandler;

    long proOffset = 1000000000000000L;
    long null_RPID  = 1999999999999999L;

    protected String projectName;
    protected String projectDatabaseName;
    protected boolean importedProject = false;
	private String fileName;
    private String [] projectData ;
    private String [] languageCodes; // Stores the language settings
    private String guiLanguage = "en-US";

    // Keeps track of the open projects and databases
    private int openProjectIndex;
    private int dataBaseIndex;
	private String selectSQL;

    private String dataBaseFilePath;
    private String databaseDDLversion;

    // Person Detail selected person data
    private int selectedRow, selectedVisibleIDX = 1;
    private String selectedPersonName;
    private long selectedPersonPID = null_RPID;
    // Stores the selected name display index for Person Select
    private int selectNameDisplayIndex = 0;
	// If true, use display-type Name Styles; else use standard (translated) Texts
	boolean selectStyleNamesControl = false;
    private HashMap<Integer,Long> personMapPID;

 /**
 * Pointers to Viewpoints containers
 */
    public JTable family;
    public JTree ancestors;
    public JTable userTable;
    public JTable projectTable;
    private JInternalFrame intFrame;

  // Pointers to config data
    public HBStoreGuiConfig pointGuiData;
    private Container[] guiScreenPointers;
    private HG0506ManagePerson managePersonScreen = null;
    private HG0508ManageLocation manageLocationScreen = null;
    private int maxNrOpenScreens = HGlobal.maxVPtotal;
    private int nrOpen = 0;

  // Number of VP's in project
	private int personVPnr;
	final static int maxPersonVPIs = 5;
	private int locationVPnr;
	final static int maxLocationVPIs = 5;
	private int eventVPnr;
	final static int maxEventVPIs = 5;

// Array list for all table names in database
	private ArrayList<String> databaseTables = new ArrayList<>();

 // ResultSet pointers
	public ResultSet projectTableRS;
    public ResultSet pointSchemaDefTable;
    protected ResultSet pointT401_PERSONS;
    protected ResultSet pointT402_PERSON_NAMES;
    public ResultSet pointT302_GUI_CONFIG;

 // Tables for PersonSelect data (normal and minimim)
    private boolean reloadPersonSelectData = true;
    private boolean reloadSelectPersonList = true;
    public Object[][] personSelectData = null;
    public Object[][] personSelectPersonList = null;

 // Table for LocationSelect data
    public boolean reloadLocationSelectData = true;
    public Object[][] locationSelectData = null;

 // Pointer to Genealogy tree
    public HBTreeCreator pointTree = null;

/**
  * return a pointer to GUI window
  * @param index
  * @return
  */
    public Container getWindowPointer(int index) {
    	return guiScreenPointers[index];
	}

    public void clearWindowPointer(int index) {
    	guiScreenPointers[index] = null;
    }
    
    public void setWindowPointer(int index, Container windowPointer) {
    	guiScreenPointers[index] = windowPointer;
    }

 /**
  * HG0506ManagePerson getManagePersonPointer()
  * @return
  */
    public HG0506ManagePerson getManagePersonPointer() {
    	return managePersonScreen;
    }

 /**
  * setManagePersonPointer(HG0506ManagePerson managePersonScreen)
  * @param managePersonScreen
  */
    public void setManagePersonPointer(HG0506ManagePerson managePersonScreen) {
    	this.managePersonScreen = managePersonScreen;
    }

/**
 * HG0506ManagePerson getManagePersonPointer()
 * @return
 */
   public HG0508ManageLocation getManageLocationPointer() {
   	return manageLocationScreen;
    }

 /**
  * setManageLocationPointer(HG0508ManageLocation manageLocationScreen)
  * @param manageLocationScreen
  */
    public void setManageLocationPointer(HG0508ManageLocation manageLocationScreen) {
    	this.manageLocationScreen = manageLocationScreen;
    }

 /**
  * String getReportLanguage()
  * @return
  */
    public String getReportLanguage() {
    	return languageCodes[3];
    }

/**
 * setSelectedLanguage(String [] langCodes)
 * @param langCodes
 */
    public void setSelectedLanguage(String [] langCodes) {
    	try {
    		guiLanguage = getGuiLanguage(HGlobal.nativeLanguage);
    		pointProjectHandler.pointLibraryResultSet.setLanguageCodes(guiLanguage,langCodes, dataBaseIndex);
    		languageCodes = langCodes;
    	} catch (HBException hbe) {
    		hbe.printStackTrace();
    	}
    }

/**
 * getLanguageCodes()
 * @throws HBException
 */
    private void getLanguageCodes(int dataBaseIndex) throws HBException  {
    	guiLanguage = getGuiLanguage(HGlobal.nativeLanguage);
    	String[] codes = pointProjectHandler.pointLibraryResultSet.getLanguageCodes(guiLanguage, dataBaseIndex);
    	//System.out.println(" GUI lang code: " + guiLanguage);
    	languageCodes = codes;
    	HGlobal.dataLanguage = codes[1];
    	HGlobal.data2Language = codes[2];
    	HGlobal.reptLanguage = codes[3];

    }

	public String getGuiLanguage(String guiCode) throws HBException {
		if (guiCode.equals("en")) {
			return "en-US";
		} else if (guiCode.equals("gb")) {
			return "en-GB";
		} else if (guiCode.equals("de")) {
			return "de-DE";
		} else if (guiCode.equals("fr")) {
			return "fr-FR";
		} else if (guiCode.equals("es")) {
			return "es-ES";
		} else if (guiCode.equals("it")) {
			return "it-IT";
		} else if (guiCode.equals("nl")) {
			return "nl-NL";
		} else if (guiCode.equals("no")) {
			return "no-NB";
		} else {
			System.out.println(" getGuiLanguage: " + guiCode + " not found");
			return "en-US";
		}
	}

/**
 * getProjectName()
 * @return
 */
	public String getProjectName() {
		return projectName;
	}

	public boolean getImportedProject() {
		return importedProject;
	}

/**
 * getDDLversion()
 * @return
 */
	public String getDDLversion() {
		return databaseDDLversion;
	}

/**
 * ResultSet getT401Persons()
 * @return
 */
	public ResultSet getT401Persons() {
		return pointT401_PERSONS;
	}

/**
 * reloadT401Persons()
 */
	public void reloadT401Persons() {
		try {
			selectSQL = pointProjectHandler.setSelectSQL("*", pointProjectHandler.personTable,"");
			pointT401_PERSONS = pointProjectHandler.requestTableData(selectSQL, dataBaseIndex);
			personMapPID =  pointProjectHandler.pointLibraryResultSet
								.indexingPersonMapPID(pointT401_PERSONS);
		} catch (HBException hbe) {
			System.out.println(" HBProjectOpenData - reloadT401Persons() error: " + hbe.getMessage());
			hbe.printStackTrace();
		}
	}
/**
 * ResultSet getT402LifeFormNames()
 * @return
 */
	public ResultSet getT402Names() {
		return pointT402_PERSON_NAMES;
	}

	public void reloadT402Names() {
		try {
			selectSQL = pointProjectHandler.setSelectSQL("*", pointProjectHandler.personNameTable,"");
			pointT402_PERSON_NAMES = pointProjectHandler.requestTableData(selectSQL, dataBaseIndex);
		} catch (HBException hbe) {
			System.out.println(" HBProjectOpenData - reloadT401Persons() error: " + hbe.getMessage());
			hbe.printStackTrace();
		}
	}

/**
 * getOpenDatabaseIndex()
 * @return
 */
    public int getOpenDatabaseIndex() {
    	return dataBaseIndex;
    }

/**
 * Get String[] projectData
 * @return array projectdata
 */
    public String[] getProjectData() {
    	return projectData;
    }

 /**
  * String getSelectedPersonName()
  * @return
  */
    public String getSelectedPersonName() {
    	return selectedPersonName;
    }

/**
 * getSelectedPersonPID()
 * @return
 */
    public long getSelectedPersonPID() {
    	return selectedPersonPID;
    }

/**
 * getPersonPIDfromVisID(int visibleID) - find PID from visibleID
 * @param visibleID
 * @return
 */
    public long getPersonPIDfromVisID(int visibleID) {
    	if (personMapPID.get(visibleID) != null) {
			return personMapPID.get(visibleID);
		} else {
			return null_RPID;
		}
    }

/**
 * int getSelectedPersonIndex()
 * @return
 */
    public int getSelectedPersonIndex() {
    	return selectedVisibleIDX;
    }

/**
 * getNameDisplayIndex()
 * @return
 */
    public int getNameDisplayIndex() {
    	return selectNameDisplayIndex;
    }

/**
 * setNameDisplayIndex(int index)
 * @param index
 */
    public void setNameDisplayIndex(int index) {
    	selectNameDisplayIndex = index;
    }

/**
 * setselectStyleNames(boolean setStyleType)
 * @param setStyleType
 */
    public void setSelectStyleNamesControl(boolean setStyleType) {
    	selectStyleNamesControl = setStyleType;
    }

/**
 * getselectStyleNames()
 * @return
 */
    public boolean getSelectStyleNamesControl() {
    	return selectStyleNamesControl;
    }

 /**
  * setSelectedPersonIndex(int selectedIDX)
  * @param selectedIDX
  */
    public void setSelectedPersonIndex(int selectedIDX) {
    	selectedVisibleIDX = selectedIDX;
    }

    public void setSelectedPersonPID(long personPID) {
    	selectedPersonPID = personPID;
    }

 /**
  * getReloadPersonSelectData()
  * @return
  */
    public boolean getReloadPersonSelectData() {
    	return reloadPersonSelectData;
    }

 /**
  * setReloadPersonSelectData(boolean state)
  * @param state
  */
    public void setReloadPersonSelectData(boolean state) {
    	reloadPersonSelectData = state;
    }

/**
 * getReloadPersonSelectDataMin()
 * @return
 */
   public boolean getReloadSelectPersonList() {
   	return reloadSelectPersonList;
   }

/**
 * setReloadPersonSelectDataMin(boolean state)
 * @param state
 */
   public void setReloadSelectPersonList(boolean state) {
   	reloadSelectPersonList = state;
   }

/**
 * 	Reset viewpoint counter
 */
	public void resetVPCounters() {

		personVPnr = 0;
		locationVPnr = 0;
		eventVPnr = 0;
	}

	public void countUpPersonVP() {
		if (getPersonVP() < (maxPersonVPIs-1)) {
			personVPnr++;
		}
		if (HGlobal.DEBUG) {
			System.out.println("HBProjectOpenData - countUpPersonVP() nr: " + personVPnr);
		}
	}

	public void countDownPersonVP() {
		personVPnr--;
		if (HGlobal.DEBUG) {
			System.out.println("HBProjectOpenData - countDownPersonVP() nr: " + personVPnr);
		}
	}

	public int getPersonVP() {
		if (HGlobal.DEBUG) {
			System.out.println("HBProjectOpenData - getPersonVP() nr: " + personVPnr);
		}
		return personVPnr;
	}

	public void countUpLocationVP() {
		if (getLocationVP() < (maxLocationVPIs-1)) {
			locationVPnr++;
		}
		if (HGlobal.DEBUG) {
			System.out.println("HBProjectOpenData - countUpLocationVP() nr: " + locationVPnr);
		}
	}

	public void countDownLocationVP() {
		locationVPnr--;
		if (HGlobal.DEBUG) {
			System.out.println("HBProjectOpenData - countDownLocationVP() nr: " + locationVPnr);
		}
	}

	public int getLocationVP() {
		if (HGlobal.DEBUG) {
			System.out.println("HBProjectOpenData - getLocationVP() nr: " + locationVPnr);
		}
		return locationVPnr;
	}

	public void countUpEventVP() {
		if (getEventVP() < (maxEventVPIs-1)) {
			eventVPnr++;
		}
		if (HGlobal.DEBUG) {
			System.out.println("HBProjectOpenData - countUpEventVP() nr: " + eventVPnr);
		}
	}

	public void countDownEventVP() {
		eventVPnr--;
		if (HGlobal.DEBUG) {
			System.out.println("HBProjectOpenData - countDownEventVP() nr: " + eventVPnr);
		}
	}

	public int getEventVP() {
		if (HGlobal.DEBUG) {
			System.out.println("HBProjectOpenData - getEventVP() nr: " + eventVPnr);
		}
		return eventVPnr;
	}

/**
 * Constructor establish connection to database
 * @param pointProjectHandler
 * @throws HBException
 */
	public HBProjectOpenData(HBProjectHandler pointProjectHandler) throws HBException {
		this.pointProjectHandler = pointProjectHandler;
	// Set up HBPersonHandler
		pointPersonHandler = new HBPersonHandler(this);
		pointPersonHandler.pointDBlayer = pointProjectHandler.pointDBlayer;
	// Set uo HBViewPointHandler
		pointViewPointHandler = new HBViewPointHandler(this);
		pointViewPointHandler.pointDBlayer = pointProjectHandler.pointDBlayer;
	// Set up HBWhereWhenHandler
		pointWhereWhenHandler = new HBWhereWhenHandler(this);
		pointWhereWhenHandler.pointDBlayer = pointProjectHandler.pointDBlayer;
	// Set up MediaHandler
		pointMediaHandler = new HBMediaHandler(this);
		pointMediaHandler.pointDBlayer = pointProjectHandler.pointDBlayer;
	}

/**
 * getViewPointHandler() - get pointer to HBViewPointHandler()
 * @return
 */
	public HBViewPointHandler getViewPointHandler() {
		return pointViewPointHandler;
	}

/**
 * getPersonHandler() - get pointer to HBPersonHandler
 * @return
 */
	public HBPersonHandler getPersonHandler() {
		return pointPersonHandler;
	}

/**
 * getWhereWhenHandler() - get pointer to HBWhereWhenHandler
 * @return
 */
	public HBWhereWhenHandler getWhereWhenHandler() {
		return pointWhereWhenHandler;
	}

/**
 * getMediaHandler() - get pointer to HBMediaHandler
 * @return
 */
	public HBMediaHandler getMediaHandler() {
		return pointMediaHandler;
	}

/**
 * openProject(boolean remote, int proIndex, String[] logonData)
 * @param remote
 * @param proIndex
 * @param logonData
 * @return
 * @throws HBException
 */
	public int openProject(boolean remote, int proIndex, String[] logonData) throws HBException   {

		// Reset pointers to VP data - only one project at a time
			getViewPointHandler().resetVPdata();

		try {
			this.projectData = pointProjectHandler.getUserProjectByIndex(proIndex);

			dataBaseFilePath = pointProjectHandler.pointLibraryBusiness.getDatabaseFilePath(projectData);
	        if (HGlobal.DEBUG) {
				System.out.println("HBOpenProjectData - Connect SQL Database path: " + dataBaseFilePath);
			}

	    // Connect to database and return database open index
	        dataBaseIndex = pointProjectHandler.pointDBlayer.connectSQLdatabase(remote, dataBaseFilePath, logonData);

	    // If remote set status for tcp connection
	        if (remote) {
	        	String connectStatus = pointProjectHandler.pointDBlayer.getConnectStatus();

	    // Set status in main window
	        	HG0401HREMain.mainFrame.setStatusAction(connectStatus);
	        }

	        if (HGlobal.DEBUG) {
				listOpenprojects();
			}

	        createTableList(dataBaseIndex);
	        pointProjectHandler.initiateTableNames(databaseTables);

		    projectName = projectData[0];
		    fileName = projectData[1];

		    if (HGlobal.DEBUG) {
				System.out.println("HBOpenProjectData nr: " + openProjectIndex
						+ " Name: " + projectName
						+ " File: " + fileName
						+ " Database Index: " + dataBaseIndex);
			}

	    // Find project database version
		    if (pointProjectHandler.pointDBlayer.ifTableExist(pointProjectHandler.schemaDefined, dataBaseIndex)) {

		// Set up ResultSet T101_SCHEMA_DEFNS
				selectSQL = pointProjectHandler.setSelectSQL("*", pointProjectHandler.schemaDefined,"");
				pointSchemaDefTable = pointProjectHandler.requestTableData(selectSQL, dataBaseIndex);

		//  Get database version
				databaseDDLversion = pointProjectHandler.pointLibraryResultSet.
						getDatabaseVersion(pointSchemaDefTable, dataBaseIndex);

			// Set up ResultSet T126_PROJECTS
				selectSQL = pointProjectHandler.setSelectSQL("*", pointProjectHandler.projectTable,"");
				projectTableRS = pointProjectHandler.requestTableData(selectSQL, dataBaseIndex);
				try {
					projectTableRS.first();
					projectDatabaseName = projectTableRS.getString("PROJECT_NAME");
					importedProject = projectTableRS.getBoolean("IS_IMPORTED");
					projectTableRS.close();
				} catch (SQLException sqle) {
					if (HGlobal.DEBUG) {
						System.out.println(" table T126_PROJECTS - IS_IMPORTED not found!");
					}
					importedProject = true;
				}

		    } else {
		    	System.out.println("Table T104_SCHEMA_DEFN not found!");
		    	return 1;
		    }

		    if (HGlobal.DEBUG) {
				System.out.println(" Opened DB created from: "
		    		+ databaseDDLversion + " / HRE DB implement: " + HGlobal.databaseVersion);
			}

/**
 * Give WARNING if generated HRE does not match database
 */
		    if (!databaseDDLversion.contains(HGlobal.databaseVersion)) {
				System.out.println(" WARNING: database build: " + databaseDDLversion
						+ " using HRE for version DDL" + HGlobal.databaseVersion );
			}

/**
 * 		Generate ResultSet for project tables
 * 		Select database version to process
 */
		    if (databaseDDLversion.contains("v22c")) {
				if (HGlobal.DEBUG) 
					System.out.println("Database DDL build: " + databaseDDLversion);			
				generateHRETables22(dataBaseIndex);
				getLanguageCodes(dataBaseIndex);
				personMapPID =  pointProjectHandler.pointLibraryResultSet
								.indexingPersonMapPID(pointT401_PERSONS);
			} else {
				throw new HBException(" HBProjectOpenData - HRE database version not accepted" +
					" found: " + databaseDDLversion);
			}

		// Inform user of database version if in DEBUG mode
		    if (HGlobal.DEBUG) {
		    	JOptionPane.showMessageDialog(null, "HRE Open Project Data:"
					+ "\nProject name: " + projectName
					+ "\nDB version: " +  HGlobal.databaseVersion
					+ "\nDB build: " +  databaseDDLversion,
					"Open Project", JOptionPane.INFORMATION_MESSAGE);
		    }

		// Reset Viewpoint counters
			resetVPCounters();
			return 0;

		} catch (HDException hde) {
			if (HGlobal.DEBUG) {
				System.out.println("ProjectOpenData - HDE error: \n" + hde.getMessage());
			}
			closeDatabase(dataBaseIndex);
			throw new HBException("ProjectOpenData - initiate error: \n" + hde.getMessage());
		}

	} // open Project

/**
 * Reset data for Person Select personDataLoaded = false;
 * Set also personDataLoaded = false when switching to another open project
 * @throws HBException
 */

	public void resetOpenWindows() throws HBException {

		try {
		// Open GUI windows if open when project close
			for (int i = 0; i < pointGuiData.guiIDvalues.length; i++) {

				String screenID = pointGuiData.guiIDvalues[i]; // *** screenID for window

				if (HGlobal.DEBUG) {
					System.out.println("Check Screen index: " + i + " ID: " + screenID);
				}
				int index = pointGuiData.getStoreIndex(screenID);
				String className = pointGuiData.screenConfigDataObj[index].getClassName();

				if (pointGuiData.getOpenStatus(screenID,pointT302_GUI_CONFIG)) {

					if (HGlobal.DEBUG) {
						System.out.println("Reset open windows: "
							+ getProjectName() + " VPn: " + personVPnr);
					}

					if (className != null) {

						if (HGlobal.DEBUG) {
							System.out.println(" resetOpenWindows(): " + className + " ID: "
								+ screenID + " Index: " + index);
						}

						if (className.equals("HG0507PersonSelect")) {
							getPersonHandler().initiatePersonSelect(this, "B");
							if (HGlobal.DEBUG) {
								System.out.println("Opened ClassName: " + className);
							}

						} else if (className.equals("HG0506ManagePerson")) {
							getPersonHandler().activateManagePerson(this);

							if (HGlobal.DEBUG) {
								System.out.println(" Opened ClassName: " + className + " Index: " + index);
							}

						} else if (className.equals("HG0507LocationSelect")) {
							getWhereWhenHandler().initiateLocationSelect(this);

							if (HGlobal.DEBUG) {
								System.out.println(" Opened ClassName: " + className + " Index: " + index);
							}

						} else if (className.equals("HG0508ManageLocation")) {
							getWhereWhenHandler().activateManageLocation(this);

							if (HGlobal.DEBUG) {
								System.out.println(" Opened ClassName: " + className + " Index: " + index);
							}

						} else if (className.equals("HG0530ViewPeople")) {

					// Select person from GUI config
							long personTablePID = ((DisplayScreenData) pointGuiData.screenConfigDataObj[index])
									.getViewPointPID();

					// Select initiate Person VP from GUI config
							int errorCode = getViewPointHandler().initiateViewPeople(this, getPersonVP(), personTablePID, screenID);

					// Error code 3 if image open error	- continue with
							if (errorCode > 0 && errorCode < 3) {
								throw new HBException("HG0530ViewPeople reopen error!");
							}

							if (HGlobal.DEBUG) {
								System.out.println("Opened ClassName: "
									+ className + " ScreenId: " + screenID + " Visible Id: " + personTablePID
									+ " VPnr: " + getPersonVP());
							}

						} else if (className.equals("HG0530ViewLocation")) {

							int focusLocation = (int) ((DisplayScreenData) pointGuiData.screenConfigDataObj[index])
													.getViewPointPID();
							long locationPID = pointGuiData.getTableViewPointPID(screenID);
							int errorCode = getViewPointHandler().initiateViewLocation(this,locationPID, getLocationVP(), screenID);
							if (errorCode > 0) {
								throw new HBException("HG0530ViewLocation reopen error!");
							}

							if (HGlobal.DEBUG) {
								System.out.println("Opened ClassName: " + className
									+ " ScreenId: " + screenID + " Location index: " + focusLocation
									+ " VPnr: " + getLocationVP());
							}

						} else if (className.equals("HG0530ViewEvent")) {

							long eventPID = pointGuiData.getTableViewPointPID(screenID);
							int errorCode = getViewPointHandler().initiateViewEvent(this,eventPID,getEventVP(),screenID);
							if (errorCode > 0) {
								throw new HBException("HG0530ViewEvent reopen error!");
							}

							if (HGlobal.DEBUG) {
								System.out.println("Opened ClassName: " + className
										+ " ScreenId: " + screenID + " Location index: " + eventPID
										+ " VPnr: " + getEventVP());
							}

						} else {
							System.out.println(" resetOpenWindows - ERROR Not found class index: " + i + " className: " + className);
							throw new HBException("resetOpenWindows ERROR Not found class index: " + i + " className: " + className);
						}

					} else {
						System.out.println("ERROR class name == null : " + i + " screen ID: " + screenID);
					}
				}
			}

		} catch (HBException exc) {
			if (HGlobal.DEBUG) {
				System.out.println("OpenProjectData - initiate error: \n" + exc.getMessage());
			}
			resetT302(dataBaseIndex);
			return;
		}
	}

/**
 * 	resetT302()
 */
	private void resetT302(int dbIndex) {
		try {
			if (!pointT302_GUI_CONFIG.isClosed()) {
				pointGuiData.deleteRecords(pointT302_GUI_CONFIG);
				pointGuiData.createT302data(pointT302_GUI_CONFIG,true);
				loadT302data(dbIndex);
				JOptionPane.showMessageDialog(null, "Error open windows\nReset of screen configuration data",
						"Maintenance", JOptionPane.INFORMATION_MESSAGE);
			}
		} catch (HeadlessException | SQLException | HBException hre) {
			System.out.println(" HBProjectOpenData - resetT302():\n " + hre.getMessage());
			hre.printStackTrace();
		}
	}
/**
 * Register the screens opened
 * @param screenPoint
 * @throws HBException
*/
	 public void registerOpenScreen(Container screenPoint) throws HBException {
		 
		 if (nrOpen < maxNrOpenScreens) {
			 String windowID = " -- ";
			 if (screenPoint instanceof JInternalFrame) {
				windowID = ((HG0451SuperIntFrame) screenPoint).getScreenID();
			}
	     	 if (screenPoint instanceof JDialog) {
				windowID = ((HG0450SuperDialog) screenPoint).getScreenID();
			}
	     	 if (screenPoint instanceof JFrame) {
				windowID = ((HG0452SuperFrame) screenPoint).getScreenID();
			}

			 int index = pointGuiData.getStoreIndex(windowID);

			 guiScreenPointers[index] = screenPoint;
			 nrOpen++;
			 
			 if (HGlobal.DEBUG) 
				System.out.println("HBProjectOpenData - Registered Screen ID: " + windowID + " Open windows: " + nrOpen);
			

		 } else {
			 if (screenPoint instanceof JInternalFrame) {
				((JInternalFrame) screenPoint).dispose();
			}
		     if (screenPoint instanceof JDialog) {
				((JDialog) screenPoint).dispose();
			}
		     if (screenPoint instanceof JFrame) {
				((JFrame) screenPoint).dispose();
			}
		     if (HGlobal.DEBUG) {
				System.out.println("Max nr of windows exceeded! nropen: " + nrOpen);
			}
		     throw new HBException("Max nr of windows exceeded! : " + nrOpen);
		 }
	 }

 /**
  * Set close status for screen and remove screen
  * @param screenID
  * @throws HBException
  */
 	public void closeStatusScreen(String screenID) {

 	// Set status closed for VP
 		try {
			pointGuiData.setOpenStatus(screenID, pointT302_GUI_CONFIG,false);
		} catch (HBException hbe) {
			System.out.println("HBProjectOpenData - closeStatusScreen error: " + hbe.getMessage());
			hbe.printStackTrace();
		}

 	// Remove open screens
 		removeOpenScreen(screenID);
 	}

 	public void setPositionScreen(String screenID,Point position) {
 		pointGuiData.setFramePosition(screenID, position);
 	}
 	public void setSizeScreen(String screenID,Dimension frameSize) {
 		pointGuiData.setFrameSize(screenID, frameSize);
 	}
 	public void setTableControl(String screenID, Object [][] tableControl) {
 		pointGuiData.setTableControl(screenID, tableControl);
 	}
 	public void setClassName(String screenID,String className) {
 		pointGuiData.setClassName(screenID, className);
 	}

 /**
  * removeOpenScreen(String screenID)
  * @param screenID
  */
 	public void removeOpenScreen(String screenID) {
 		String windowID = "";
 		 for (int i = 0; i < pointGuiData.guiIDvalues.length; i++) {
 			 if (guiScreenPointers[i] != null) {
	 			 if (guiScreenPointers[i] instanceof JInternalFrame) {
					windowID = ((HG0451SuperIntFrame) guiScreenPointers[i]).getScreenID();
				} else if (guiScreenPointers[i] instanceof JDialog) {
					windowID = ((HG0450SuperDialog) guiScreenPointers[i]).getScreenID();
				} else if (guiScreenPointers[i] instanceof JFrame) {
					windowID = ((HG0452SuperFrame) guiScreenPointers[i]).getScreenID();
				}

				 if (windowID.equals(screenID)) {
					guiScreenPointers[i] = null;
					nrOpen--;
				if (HGlobal.DEBUG) 
					System.out.println("HBProjectOpenData - Window removed: " + screenID + " NrOpen: " + nrOpen);
				
				 }
 			 }
 	     }
 	}

 /**
  * toFrontOpenScreen(String screenID)
  * @param screenID
  */
 	public void toFrontOpenScreen(String screenID) {
 		String windowID = "";
 		 for (int i = 0; i < pointGuiData.guiIDvalues.length; i++) {
 			 if (guiScreenPointers[i] != null) {
	 			 if (guiScreenPointers[i] instanceof JInternalFrame)  {
	 					 windowID = ((HG0451SuperIntFrame) guiScreenPointers[i]).getScreenID();
	 					if (windowID.equals(screenID)) {
							((HG0451SuperIntFrame)guiScreenPointers[i]).toFront();
						}
	 			 } else if (guiScreenPointers[i] instanceof JDialog)  {
	 		     		windowID = ((HG0450SuperDialog) guiScreenPointers[i]).getScreenID();
	 					if (windowID.equals(screenID)) {
							((HG0450SuperDialog)guiScreenPointers[i]).toFront();
						}
	 			 } else if (guiScreenPointers[i] instanceof JFrame)  {
	 		     		windowID = ((HG0452SuperFrame) guiScreenPointers[i]).getScreenID();
	 					if (windowID.equals(screenID)) {
							((HG0452SuperFrame)guiScreenPointers[i]).toFront();
						}
	 			 } else {
					//if (HGlobal.DEBUG)
					 System.out.println("HBProjectOpenData - toFrontOpenScreen: " + screenID + " not found");
				}

 			 }
 	     }
 	}

/**
 * storeNameStyleIndex(String screenID)  if (selectStyleNamesControl) set index + 100
 * @param screenID
 */
  public void storeNameStyleIndex(String screenID) {
	  int nameStyleIndexCombined = getNameDisplayIndex();
	  if (selectStyleNamesControl) {
		nameStyleIndexCombined = nameStyleIndexCombined + 100;
	}
	  pointGuiData.setNameDisplayIndex(screenID, nameStyleIndexCombined);
  }

/**
 * restoreNameStyleIndex(String screenID) restore name display index and selectStyleNamesControl
 * @param screenID
 */
  public void restoreNameStyleIndex(String screenID) {
	  if (pointGuiData.getNameDisplayIndex(screenID) >= 100) {
		  selectStyleNamesControl = true;
		  setNameDisplayIndex(pointGuiData.getNameDisplayIndex(screenID) - 100);
	  } else {
		  selectStyleNamesControl = false;
		  setNameDisplayIndex(pointGuiData.getNameDisplayIndex(screenID));
	  }
  }

/**
 * dumpConnectedDatabases()
 */
	private void listOpenprojects() {
		System.out.println("Existing OpenProjects nr:");
		for (int i = 0; i < HGlobal.openProjects.size(); i++) {
			int databaseIndex = HGlobal.openProjects.get(i).dataBaseIndex;
			String projectName = HGlobal.openProjects.get(i).projectName;
			String databasePath = pointProjectHandler.pointDBlayer.getDatabasePath(databaseIndex);
			System.out.println(i + " - " + projectName
					+ " Baseindex: " + databaseIndex + " Path: " + databasePath);
		}
	}

/**
 * storeSelectedPersonData
 * @param row
 * @param name
 */
	public void storeSelectedPersonData(int row, int userIDX, long personTablePID, String name) {
	    selectedRow = row;
	    selectedVisibleIDX = userIDX;
	    selectedPersonName = name;
	    selectedPersonPID = personTablePID;
	    if (HGlobal.DEBUG) {
			System.out.println("OpenProject: " + projectName + " - table row: " + selectedRow + " - "
	    		+ "User ID: " + selectedVisibleIDX + " PID: " + personTablePID + " Name: " + selectedPersonName);
		}
	}

/**
 * Collect summary data for open selected project
 * @return String[][] summary
 */
	public String[][] getSummaryData(String[] projectData) {
		return HGlobalCode.getSummaryData(projectData, databaseDDLversion);
	}

/**
 * ProjectOpen Data - generateBiosTables for open project
 * @param dbIndex
 * @return
 * @throws HBException
 */
	private void generateHRETables22(int dbIndex) throws HBException {
		if (HGlobal.DEBUG) {
			System.out.println("HBProjectOpenData - generateBiosTables - db-index: " +  dbIndex);
		}

		selectSQL = pointProjectHandler.setSelectSQL("*","T401_PERS","");
		pointT401_PERSONS = pointProjectHandler.requestTableData(selectSQL, dbIndex);

		selectSQL = pointProjectHandler.setSelectSQL("*","T402_PERS_NAME","");
		pointT402_PERSON_NAMES = pointProjectHandler.requestTableData(selectSQL, dbIndex);

	// Create or Open T302_GUI_CONFIG table
		selectSQL = pointProjectHandler.setSelectSQL("*","T302_GUI_CONFIG","");
		pointT302_GUI_CONFIG = pointProjectHandler.requestTableData(selectSQL, dbIndex);

    // Set up instance of HBStoreGuiConfig for open project
		pointGuiData = new HBStoreGuiConfig();
		guiScreenPointers = new Container[pointGuiData.guiIDvalues.length];

	// Reset global GUI config stored objects
		if (pointGuiData.createT302data(pointT302_GUI_CONFIG,false) == 0) {

	// Reload T302_GUI_CONFIG table
			loadT302data(dbIndex);
		}
	}
/**
 * generateT302data
 * @param dbIndex
 * @throws HBException
 */
	public void loadT302data(int dbIndex)  throws HBException {
			try {
				selectSQL = pointProjectHandler.setSelectSQL("*","T302_GUI_CONFIG","");
				pointT302_GUI_CONFIG = pointProjectHandler.requestTableData(selectSQL, dbIndex);
				pointT302_GUI_CONFIG.last();
				int size = pointT302_GUI_CONFIG.getRow();

		// Read the table to initiate HashMap for GUI index
				pointGuiData.readObjects(pointT302_GUI_CONFIG);
				if (HGlobal.DEBUG) {
					System.out.println(
							"HBProjectOpenData - T302_GUI_CONFIG size: " + size);
				}
			} catch (SQLException sqle) {
				if (HGlobal.DEBUG) {
					System.out.println(
							"ERROR - HBProjectOpenData - Reload ResultSet T302_GUI_CONFIG\n"
									+ sqle.getMessage());
				}
				sqle.printStackTrace();
			}
		if (HGlobal.DEBUG) {
			System.out.println("HBProjectOpenData - Reload ResultSet completed");
		}
	}


/**
 * closeDatabase(int databaseIndex)
 * @param databaseIndex
 * @throws HBException
 */
	public void closeDatabase(int databaseIndex) throws HBException {
		if (HGlobal.DEBUG) {
			System.out.println("Close ResultSet and database index: " + databaseIndex);
		}
		try {
			if (pointSchemaDefTable != null) {
				pointSchemaDefTable.close();
			} else {
				throw new HBException("OpenProjectData - pointT101_SCHEMA_DEFNS not open");
			}
			if (pointT401_PERSONS != null) {
				pointT401_PERSONS.close();
			} else {
				throw new HBException("OpenProjectData - pointT401_BIOS not open");
			}
			if (pointT402_PERSON_NAMES != null) {
				pointT402_PERSON_NAMES.close();
			} else {
				throw new HBException("OpenProjectData - pointT404_BIO_NAMES not open");
			}

		// Set frame size open screen in GUI data for open screens and dispose
			for (int i = 0; i < pointGuiData.guiIDvalues.length; i++) {

				if (guiScreenPointers[i] != null) {
					if (guiScreenPointers[i] instanceof HG0452SuperFrame) {
						HG0452SuperFrame frameScreenPoint = (HG0452SuperFrame) guiScreenPointers[i];
						if (HGlobal.DEBUG) {
							System.out.println("Close of HG0451SuperFrame: " + frameScreenPoint.getScreenID());
						}
						Dimension frameSize = frameScreenPoint.getSize();
						pointGuiData.setFrameSize(frameScreenPoint.getScreenID(),frameSize);
						Point position = frameScreenPoint.getLocation();
						pointGuiData.setFramePosition(frameScreenPoint.getScreenID(), position);
						frameScreenPoint.dispose();
					}

					if (guiScreenPointers[i] instanceof HG0451SuperIntFrame) {

						if (guiScreenPointers[i] instanceof HG0507PersonSelect
								|| guiScreenPointers[i] instanceof HG0507LocationSelect
								|| guiScreenPointers[i] instanceof HG0506ManagePerson
								|| guiScreenPointers[i] instanceof HG0508ManageLocation) {

							HG0451SuperIntFrame frameScreenPoint = (HG0451SuperIntFrame) guiScreenPointers[i];
							if (HGlobal.DEBUG) {
								System.out.println("Close of HG0507Per/Loc-Select: " + frameScreenPoint.getScreenID());
							}
							Dimension frameSize = frameScreenPoint.getSize();
							pointGuiData.setFrameSize(frameScreenPoint.getScreenID(),frameSize);
							Point position = frameScreenPoint.getLocation();
							pointGuiData.setFramePosition(frameScreenPoint.getScreenID(), position);
						// Set last selected visible index
							pointGuiData.setVisibleIDX(frameScreenPoint.getScreenID(), selectedVisibleIDX);

						// Set name style index
							storeNameStyleIndex(frameScreenPoint.getScreenID());
							//pointGuiData.setNameDisplayIndex(frameScreenPoint.getScreenID(),selectNameDisplayIndex);
							frameScreenPoint.dispose();

						} else {

							HG0451SuperIntFrame frameIntPoint = (HG0451SuperIntFrame) guiScreenPointers[i];
							if (HGlobal.DEBUG) {
								System.out.println("Close of HG0451SuperFrame: " + frameIntPoint.getScreenID());
							}

						// Limit screen hight to initial dimension
							Dimension frameSize = frameIntPoint.getSize();
/**
 *  Setting of original height for windows closed
 */
							double height = frameIntPoint.frameHeight;
							double with = frameSize.getWidth();
							Dimension newSize = new Dimension();
							newSize.setSize(with, height);
							pointGuiData.setFrameSize(frameIntPoint.getScreenID(),newSize);
/**
 * Removed 2022 - 4 - 21 to restore original height
 */
							//pointGuiData.setFrameSize(frameIntPoint.getScreenID(),frameSize);

							Point position = frameIntPoint.getLocation();
							pointGuiData.setFramePosition(frameIntPoint.getScreenID(), position);
							frameIntPoint.dispose();
						}
					}

					if (guiScreenPointers[i] instanceof HG0450SuperDialog) {
						HG0450SuperDialog dialogWindowPoint = (HG0450SuperDialog)guiScreenPointers[i];
						if (HGlobal.DEBUG) {
							System.out.println("Close of HG0450SuperDialog: " + dialogWindowPoint.getScreenID());
						}
						Dimension frameSize = dialogWindowPoint.getSize();
						pointGuiData.setFrameSize(dialogWindowPoint.getScreenID(),frameSize);
						Point position = dialogWindowPoint.getLocation();
						pointGuiData.setFramePosition(dialogWindowPoint.getScreenID(), position);
						dialogWindowPoint.dispose();
					}
				}
			}

		// Reset number of VP! in project
			resetVPCounters();

		// Update GUI configuration data in T302_GUI_CONFIG
			selectSQL = pointProjectHandler.setSelectSQL("*","T302_GUI_CONFIG","");
			pointT302_GUI_CONFIG = pointProjectHandler.requestTableData(selectSQL, databaseIndex);
			pointGuiData.updateT302table(pointT302_GUI_CONFIG);
			pointT302_GUI_CONFIG.close();
			pointProjectHandler.pointDBlayer.closeDatabaseConnection(dataBaseIndex);

		} catch (HDException hde) {
			throw new HBException("OpenProjectData - database close error: \n" + hde.getMessage());
		} catch (SQLException sqle) {
			//sqle.printStackTrace();
			throw new HBException("OpenProjectData -  ResultSet close error: \n" + sqle.getMessage());
		}
	}
/**
 * createTableList(int dbIndex)
 * @param dbIndex
 * @throws HDException
 */
	private void createTableList(int dbIndex) throws HDException {
		ResultSet HRE_Tables = pointProjectHandler.pointDBlayer.requestTableNames(dbIndex);
		try {
			HRE_Tables.beforeFirst();
			int nrOfTables = 0;
			while (HRE_Tables.next()) {
				nrOfTables++;
				String tableName = HRE_Tables.getString("TABLE_NAME");
				if (TMGglobal.DUMP) {
					System.out.println(" " + nrOfTables + " name: " + tableName);
				}
				databaseTables.add(tableName);
			}
			if (HGlobal.DEBUG) {
				System.out.println("Tables in database: " + nrOfTables);
			}

		} catch (SQLException sqle) {
			System.out.println("Delete CL table error: " + sqle.getMessage());
			sqle.printStackTrace();
		}
	}
/**
 * Remove InternalFrame from main window generated from this project
 */
	public void removeMainWindowInternalFrame() {
		if (intFrame != null) {
			pointProjectHandler.removeInternalFrameFromWindow(intFrame);
		}
	}
} // End HBProjectOpenData


