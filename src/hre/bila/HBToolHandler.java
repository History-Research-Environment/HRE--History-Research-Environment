package hre.bila;
/*************************************************************************************
 * Class HBToolHandler extends BusinessLayer
 * Processes data for Tools Menu in User GUI
 * Receives requests from User GUI to action methods
 * Sends requests to database over Database Layer API
 * HBToolHandler for processing of menuToolActions
 * ***********************************************************************************
 * v0.00.0016 2019-12-13 - First version (N. Tolleshaug)
 * v0.00.0017 2020-01-30 - mainFrame.resetGUILanguage() link changed (N. Tolleshaug)
 *            2020-02-16 - implemented Project User Administration (N. Tolleshaug)
  v0.00.0019 2020-03-12 - added handler update user data in project. (N. Tolleshaug)
  * 						corrected update user in table (N. Tolleshaug)
 * v0.00.0019 2020-03-19 - Removed setting of HDGlobal.DEBUG - Only HGlobal.DEBUG used
 * v0.00.0025 2021-03-22 - Removed JOptionPane from Tools with HBExceptions(N. Tolleshaug)
 * 			  2021-03-23 - fixed database open/close database index error (N. Tolleshaug)
 * v0.01.0026 2021-05-16 - Added copied file delete for Errorcode 3 (N. Tolleshaug)
 * 			  2021-05-16 - Added handling of TCP server on/off (N. Tolleshaug)
 * 			  2021-09-09 - Implemented SSL for TCP server   (N. Tolleshaug)
 * 			  2021-10-10 - Update password in T131 (N. Tolleshaug)
 * 			  2023-09-03 Implemented monitor flags and redirect console output (N.Tolleshaug)
 * v0.03.0030 2023-10-02 If import fails wrong file deleted or useProject error (D Ferguson)
 ************************************************************************************/
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JTable;

import hre.dbla.HDException;
import hre.gui.HGlobal;
import hre.gui.HGlobalCode;
import hre.tmgjava.TMGHREconverter;
import hre.tmgjava.TMGHREprogressMonitor;
import hre.tmgjava.TMGglobal;

/**
  * HBToolHandler for Tool menu
  * @author Nils Tolleshaug
  * @version v0.03.0030
  * @since 2019-12-20
  */
public class HBToolHandler extends HBBusinessLayer {

	public JTable userTable;
    public JTable projectTable;

    protected ResultSet pointT131usersResultSet;
    protected ResultSet pointT101schemaResultSet;

    String databaseEngine = "H2";
    int currentDatabaseIndex = -1;

    private String defaultDatabaseEngine = HGlobal.defDatabaseEngine;
/**
 * HBToolHandler constructor
 */
	public HBToolHandler() {
		super();
	}

/**
 * 	Method links resetGUILanguage() from HG05011AppSettings to method in HG0401HREMain
 */
	public void resetGUILanguage() {
		if (HGlobal.DEBUG) System.out.println("GUI language reset!");
		mainFrame.resetGUILanguage();
	}

/**
 * Control of DEBUG variable in BusinessLayer and DatabaseLayer
 * @param on - debug setting
 */
	public void setDebugOption(boolean on) {
		if (on) {
			HGlobal.DEBUG = true;
        	if (HGlobal.DEBUG) System.out.println("Console output - DEBUG = ON");
		} else {
        	if (HGlobal.DEBUG) System.out.println("Console output - DEBUG = OFF");
			HGlobal.DEBUG = false;
		}
	}

/**
 * Get default database engine
 * @return defaultDatabaseEngine
 */
	@Override
	public String getDefaultDatabaseEngine() {
		return defaultDatabaseEngine;
	}

/**
 * Add project to project list
 * @param projectData String[] with project to be added
 */
	public void addProjectToList(String [] projectData) {
		HGlobal.userProjects.add(projectData);
	}

/**
 * String startTCPserver()
 */
	public String startTCPserver() {
		String serverStatus = " Status unknown!";
		try {
			if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: TCP server started:  ");
		// Get port number from first entry in the HGlobal.userServers list
			String serverPort = HGlobal.userServers.get(0)[2];
		// Initiate server that returns the server connect data
			serverStatus =  pointDBlayer.startServer(serverPort);
		} catch (HDException hde) {
			if (HGlobal.DEBUG) System.out.println(" Start server error: " + hde.getMessage() );
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Error Start TCP server:  " + hde.getMessage());
				HB0711Logging.printStackTraceToFile(hde);
			}
			return "ERROR Start TCP server - " + hde.getMessage();
		}
		return serverStatus;
	}

/**
 * boolean stopTCPserver()
 */
	public boolean stopTCPserver() {
		if (HGlobal.numOpenProjects == 0) {
			pointDBlayer.stopServer();
			if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: TCP server stopped:  ");
			return true;
		} else {
			return false;
		}
	}

/**
 * Closes database connection
 * @throws HBException
 */
	public void closeDatabaseConnection() throws HBException {
		try {
			if (!(currentDatabaseIndex < 0)) {
				pointDBlayer.closeDatabaseConnection(currentDatabaseIndex);
				if (pointT131usersResultSet != null) pointT131usersResultSet.close();

				if (HGlobal.DEBUG)
					System.out.println("HBToolHandler Closed database index nr: " + currentDatabaseIndex);
				currentDatabaseIndex = -1;
			}
		} catch (HDException | SQLException hde) {
			if (HGlobal.DEBUG) System.out.println("HBToolHandler - close database: \n" + hde.getMessage());
			throw new HBException("HBToolHandler close database error:\n"
					+  hde.getMessage());
		}
	}

/**
 * Check if project name is in available projects
 * @param name - project name
 * @param arrayList - list of userProjects
 * @return - found = true
 */
	private boolean isProjectNameUsed(String name, ArrayList<String []> arrayList) {
		for (String[] element : arrayList) {
			if (element[0].equals(name)) {
				return true;
			}
		}
		return false;
	}

/**
 * Get String[] from user project index i
 * @param i project index to return
 * @return String[] with project data
 * @throws HBException
 */
	public String[] getUserProjectByIndex(int i) throws HBException {
		if (i < HGlobal.userProjects.size())
			return HGlobal.userProjects.get(i);
		else throw new HBException("ArrayList index out of range: " + i);
	}

/**
 * Return project String[] with name from userProjects ArrayList
 * @param name Name of project to return
 * @return String[] with project data
 * @throws HBException
 */
	public int getUserProjectByName(String name) throws HBException {
		for (int i = 0; i < HGlobal.userProjects.size(); i++) {
			if (HGlobal.userProjects.get(i)[0].equals(name)) {
				return i;
			}
		}
		throw new HBException("Project not in list of user projects: " + name);
	}

/**
 * Get String[][] from userProjects and populate JTable
 * @return String[][]
 */
	public String[][] getKnownProjectArray2D() {
		String[][] array2D = new String[HGlobal.userProjects.size()][];
		for (int i = 0; i < array2D.length; i++) {
			String[] row = HGlobal.userProjects.get(i);
			array2D[i] = new String[]  {row[0],	//project name
										row[1],	//file name
										row[3]	//server name
										};
			if (array2D[i][2].equals(HGlobal.thisComputer))
					array2D[i][2] = HGlobalCode.getLocalText();
		}
		return array2D;
	}

/**
 * Open database file for user admin
 * @param projectFolder
 * @param projectFile
 * @throws HDException
 */
	public void openDatabase(String projectFolder, String projectFile) throws HDException {
		String dataBaseFilePath = pointLibraryBusiness.
				getDatabaseFilePath(projectFolder, projectFile);
	// Connect to new database and receive Index in ArrayList connectedDataBases
		String[] logonData = {databaseEngine,"SA",""};
		currentDatabaseIndex = pointDBlayer.connectSQLdatabase(dataBaseFilePath, logonData);
        if (HGlobal.DEBUG)
        	System.out.println("HBToolHandler - open database nr: " + currentDatabaseIndex);
	}

/**
 * opens database T131 table and present data from database T131
 * @param projectData
 * @throws HBException
 */
	public String [][] presentTableUsersT131() throws HBException {
				int dataBaseIndex = currentDatabaseIndex;
		        if (HGlobal.DEBUG)
		        	System.out.println("HBToolHandler - read from database nr: " + dataBaseIndex);
		 // SELECT action to SQL base
		        String selectSQL = setSelectSQL("*","T131_USER","");
				pointT131usersResultSet = requestTableData(selectSQL, dataBaseIndex);
				userTable = makeTableFromRS(pointT131usersResultSet);
				if (HGlobal.DEBUG) dumpResultSetData(pointT131usersResultSet);
				return setUpUserTable(pointT131usersResultSet);
	}

/**
 *
 * @param table131ResultSet
 * @return
 * @throws HBException
 */
   private String [][] setUpUserTable(ResultSet table131ResultSet) throws HBException {
	   if (HGlobal.DEBUG) System.out.println("Set up user Table : ");
	   if (table131ResultSet == null) throw new HBException("User table T131 not available\n");
	   try {
		  // Find number of rows in ResultSet
		   	table131ResultSet.last();
		   	int size = table131ResultSet.getRow();
		  // Initiate table array
		   	String [][] userTable = new String[size][3];
	   		int index = 0;
	   		table131ResultSet.beforeFirst();
		   	while (table131ResultSet.next()) {
		   		userTable[index][0] = table131ResultSet.getString("LOGON_NAME");
		   		userTable[index][1] = table131ResultSet.getString("USER_NAME");
		   		userTable[index][2] = table131ResultSet.getString("EMAIL");
		   		index++;
		   	}
		   	if (HGlobal.DEBUG) System.out.println();
		   	return userTable;
	   	} catch(SQLException exc) {
	   		if (HGlobal.DEBUG) System.out.println("HBToolHandler - Set up user table error:: \n"
	   				+ exc.getMessage());
	   		throw new HBException("HBToolHandler - Set up user table error: \n" + exc.getMessage());
	   	}
   }

/**
 * changeUserTableAction
 * @param userCred
 * @return
 * @throws HBException
 */
	public boolean changeUserTableAction(String[] userCred, int selectedRow) throws HBException {
		try {
		// Position to current row in ResultSet
			if (!pointT131usersResultSet.absolute(selectedRow + 1))
				throw new HBException("HBToolHandler - changeUserTableAction - absolute error: "
						+ (selectedRow + 1));
		 // Update selected row
			pointLibraryResultSet.updateUserInTable(pointT131usersResultSet, userCred);
		// Update password for database user
			return updateDatabaseUserPassWord(userCred[0], userCred[2]);
		} catch (SQLException sqle) {
			if (HGlobal.DEBUG) System.out.println("HBToolHandler - updateUserTable: \n" + sqle.getMessage());
			throw new HBException("HBToolHandler - updateUserTable: \n" + sqle.getMessage());
		}
	}

/**
 * addToUserTableAction(String[] userCred)
 * @param userCred
 * @return
 */
   public boolean addToUserTableAction(String[] userCred) throws HBException {
	   pointLibraryResultSet.addUserToTable(pointT131usersResultSet, userCred);
       String selectSQL = setSelectSQL("*","T131_USER","");
	   pointT131usersResultSet = requestTableData(selectSQL, currentDatabaseIndex);
	// create user for H2 database
	   return createNewDatabaseUser(userCred[0], userCred[2]);
   }

 /**
  * deleteUserTableAction (int selectedUser)
  * @param selectedUser
  * @return
  * @throws HBException
  */
   public boolean deleteUserTableAction (int selectedUser, String userId) throws HBException {
	   try {
		   pointLibraryResultSet.deleteUserInTable(pointT131usersResultSet,selectedUser);
	       String selectSQL = setSelectSQL("*","T131_USER","");
		   pointT131usersResultSet = requestTableData(selectSQL, currentDatabaseIndex);
		   return deleteDatabaseUser(userId);
		} catch (HBException hbe) {
			if (HGlobal.DEBUG) System.out.println("HBToolHandler - delete user error: \n" + hbe.getMessage());
			return false;
		}
   }

/**
 * boolean createNewUser(String userid, String password)
 * @param userid
 * @param password
 * @return
 * @throws HBException
 */
   public boolean createNewDatabaseUser(String userid, String password, String dataBaseFilePath) throws HBException {
		//  Connect database
	   int dataBaseIndex = -1;
	   String dataBaseEngine = getDefaultDatabaseEngine();
	   try {
	//  Connect database
			String[] logonData = {dataBaseEngine,"SA",""};
			dataBaseIndex = pointDBlayer.connectSQLdatabase(dataBaseFilePath, logonData);
			createNewDatabaseUser(userid, password, dataBaseIndex);
	 // Close database
			closeDatabase(dataBaseIndex);
			return true;
		} catch (HDException hde) {
	// Close database
			closeDatabase(dataBaseIndex);
			throw new HBException("HBToolHandler createNewDatabaseUser error: " + hde.getMessage());
		}
   }

   public boolean createNewDatabaseUser(String userid, String password) throws HBException {
	   return createNewDatabaseUser(userid, password, currentDatabaseIndex);
   }

   public boolean createNewDatabaseUser(String userid, String password, int dataBaseIndex) throws HBException {
		String sqlRequest = "CREATE USER " + userid + " PASSWORD '" + password + "';"
				+ " GRANT ALTER ANY SCHEMA TO " + userid + ";";
		if (!userid.equals("SA"))
			try {
				return pointDBlayer.updateTableData(sqlRequest, dataBaseIndex);
			} catch (HDException hde) {
				closeDatabase(dataBaseIndex);
				throw new HBException("" + hde.getMessage());
			}  else throw new HBException("Cannot create new SA user! \n");
   }

/**
 * deleteDatabaseUser(String userid)
 * @param userid
 * @return
 * @throws HBException
 */
   public boolean deleteDatabaseUser(String userid) throws HBException {
		String sqlRequest = "DROP USER IF EXISTS " + userid + ";";
		if (!userid.equals("SA"))
			try {
				return pointDBlayer.updateTableData(sqlRequest, currentDatabaseIndex);
			} catch (HDException hde) {
				throw new HBException("" + hde.getMessage());
				//hde.printStackTrace();
			} else throw new HBException("User SA cannot be changed! \n");
   }

/**
* deleteDatabaseUser(String userid)
* @param userid
* @return
* @throws HBException
*/
  public boolean updateDatabaseUserPassWord(String userid, String passWord) throws HBException {
		String sqlRequest = "ALTER USER " + userid + " SET PASSWORD '" + passWord + "';";
		if (!userid.equals("SA")) {
			if  (passWord != null)
				try {
					return pointDBlayer.updateTableData(sqlRequest, currentDatabaseIndex);
				} catch (HDException hde) {
					throw new HBException("Update user database password error: \n" + hde.getMessage());
				} else return false;
		} else throw new HBException("User SA cannot be changed! \n");
  }
  	
/**
 * Import data from TMG and create a new project with imported data from TMG
 * New project to be included in ArrayList userProjects.
 * Called from User GUI Tools menu TMG Import Project
 *
 * boolean importTmgAction()
 * @return
 * @throws HBException
 */
	public int importTmgToHreAction(String[] newProjectArray,
				boolean[] monitorFlags, String logFile) throws HBException {
		String newProjectName = newProjectArray[0];

	// Set file path components
		String chosenFileName = newProjectArray[1];
		String chosenFolder = newProjectArray[2];
		try {
		// set monitor flags
			setMonitorFlags(monitorFlags, logFile);

		// If project name already used ?
			if (isProjectNameUsed(newProjectName, HGlobal.userProjects)) {
				return 1;
			}

		// File chooser strips off file type and must be added **** Temp solution
			pointLibraryBusiness.copyFile(HGlobal.seedProjectFile,
						chosenFolder + File.separator + chosenFileName + ".mv.db");	

		// Find database loc for H2 connect
			String dataBaseFilePath = pointLibraryBusiness.getDatabaseFilePath(newProjectArray);
			if (HGlobal.DEBUG)
				System.out.println("HBToolHandler - New Project - DatabasePath: "
					+ dataBaseFilePath
					+ "\nSeed database file: " + HGlobal.seedProjectFile);
			if (pointLibraryResultSet.setDatabaseProjectName(newProjectName, dataBaseFilePath)) {

		// Update user table data for project
				HGlobal.userCred[2] = "Hre_2021"; // Set initial password for user!

		// update T131 table with new user and new database user
				pointLibraryResultSet.updateNewUserInProject(dataBaseFilePath, HGlobal.userCred);

		// Include new project data in HGlobal.userProjects list
				addProjectToList(newProjectArray);
				if (HGlobal.DEBUG)
					System.out.println("HBToolHandler Prepared copy of new TMG project " + newProjectName);

		// Update userAUX with new project data
				HB0744UserAUX.writeUserAUXfile();

		// start conversion
				TMGHREprogressMonitor conv = new TMGHREprogressMonitor();
				conv.startMonitor();
				if (HGlobal.DEBUG)
					System.out.println("HBToolHandler importTmgToHreAction - Convert TMG to folder/file: "
							+ chosenFolder + "/" + chosenFileName);
				conv.startConversion(chosenFolder + File.separator + chosenFileName);
				return 0;
			} 
			else {
		// Delete created database file
				pointLibraryBusiness.deleteFile(chosenFolder + File.separator + chosenFileName + ".mv.db");
			// Remove last project from HGlobal.userProjects list
				HGlobal.userProjects.remove(HGlobal.userProjects.size()-1);
			// Update userAUX file with revised data
				HB0744UserAUX.writeUserAUXfile();
				return 2;
			}

		} catch(HBException hbe) {
			if (HGlobal.DEBUG)
				System.out.println("HBToolHandler - New Project creation failed: \n "
						+ hbe.getMessage());
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Error importTmgToHreAction:  " + hbe.getMessage());
				HB0711Logging.printStackTraceToFile(hbe);
			}
			// Do NOT delete any files here - file creation has failed so there is nothing to delete
			return 3;
		}		
   }

/**
 * setMonitorFlags(boolean[] monitorFlags)
 * @param monitorFlags
 * @throws HBException
 * @throws IOException
 */
	private void setMonitorFlags(boolean[] monitorFlags, String logFile) throws HBException  {
		TMGglobal.TRACE = monitorFlags[0];
		TMGglobal.TMGTRACE = monitorFlags[1];
		TMGglobal.HRETRACE = monitorFlags[2];
		TMGglobal.FLAGCHECK = monitorFlags[3];
		TMGglobal.EXHCHECK = monitorFlags[4];
		TMGglobal.DEBUG = monitorFlags[5];
		if (TMGglobal.TRACE)
			try {
				TMGHREconverter.redirectOutput(logFile);
			} catch (IOException ioe) {
				System.out.println("Tools - setMonitorFlags - redirevt output error" + ioe.getMessage());
				ioe.printStackTrace();
			}
	}
	
} // END HBToolHandler