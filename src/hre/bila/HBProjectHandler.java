package hre.bila;
/**************************************************************************************
 * Class ProjectHandler extends BusinessLayer
 * Processes data for Project Menu in User GUI
 * Receives requests from User GUI over Business Layer API
 * Sends requests over Database Layer API
 * v0.00.0016 2019-12-20 First version (N. Tolleshaug)
 * v0.00.0017 2020-01-11 modify array2D construction (D. Ferguson)
 * v0.00.0017 2020-01-23 modifed open, new and browse project (N. Tolleshaug)
 * v0.00.0017 2020-02-06 Added backup database, restore database (N. Tolleshaug)
 * v0.00.0017 2020-02-24 Corrected error opening empty database or no
 * 						 content in database tables (N. Tolleshaug)
 * v0.00.0018 2020-02-25 Delete trace file if exists (N. Tolleshaug)
 * v0.00.0019 2020-02-26 add lastBackup field to userProjects (D. Ferguson)
 * v0.00.0019 2020-03-12 Rename Project - open project removed (N. Tolleshaug)
 * v0.00.0019 2020-03-15 Copy AS - implemented without progress monitor
 * 						 see NOTE 02 (N. Tolleshaug)
 * v0.00.0022 2020-05-16 Modified getPassWord(String userID)
 * 						 removed call to setFocusProject() - (N. Tolleshaug)
 * v0.00.0022 2020-05-23 Action close project Updated call to close (N. Tolleshaug)
 * v0.01.0025 2020-10-22 Make isProjectNameUsed public for use in Restore (D Ferguson)
 * 			  2020-11-18 Use Main status bar for project status at open (N Tolleshaug)
 * 			  2021-01-21 Set pointer to selected project in openAction (N Tolleshaug)
 * 			  2021-01-28 Removed use of HGlobal.selectedProject (N. Tolleshaug)
 * 			  2021-02-04 Add restoreExternalAction for restore of zipped folders (D Ferguson)
 * 			  2021-03-09 JOptionPane messaged removed from Open project (N. Tolleshaug)
 * 			  2021-03-09 JOptionPane messaged removed from Browse open project (N. Tolleshaug)
 *			  2021-03-09 JOptionPane messaged removed from CopyProjectAction (N. Tolleshaug)
 * 			  2021-03-09 JOptionPane messaged removed from Close project (N. Tolleshaug)
 * 			  2021-03-09 JOptionPane messaged removed from Delete project (N. Tolleshaug)
 * 			  2021-03-10 JOptionPane messaged removed from Rename project (N. Tolleshaug)
 * 			  2021-03-15 JOptionPane messaged removed from Backup project (N. Tolleshaug)
 * 			  2021-03-16 JOptionPane messaged removed from Backup project (N. Tolleshaug)
 * 			  2021-04-01 Status message for project moved to Main (N. Tolleshaug)
 * 			  2021-04-12 Log message created for HBException handling (N. Tolleshaug)
 * v0.01.0026 2021-05-05 Fix error in Open Browse error exiting project (N. Tolleshaug)
 * 			  2021-09-29 Add check in deleteProjectAction to not delete remote DB (D Ferguson)
 * 			  2021-10-01 Handling of local/remote connect to database (N. Tolleshaug)
 * 			  2021-10-10 Update of users in database (N. Tolleshaug)
 * v0.01.0031 2023-11-22 Added code to modify EVNT_TYPE in T460 and T461 (N. Tolleshaug)
 * 			  2023-11-23 Added code add first person in empty project (N. Tolleshaug)
 * 			  2024-01-05 Added table colums adding in empty project (N. Tolleshaug)
 * 			  2024-10-11 Added initiateDateFormat(3); in add first person line 510 (N. Tolleshaug)
 * 			  2024-10-21 Added dateFormatSelect() to addFirstPerson() (N. Tolleshaug)
 * v0.01.0032 2024-12-22 Modified for B32 and v22c (N. Tolleshaug)
 * 			  2024-12-22 Updated for new project B32 (N. Tolleshaug)
 * 			  2025-03-22 Updated for create citation/source tables (N. Tolleshaug)
 *			  2025-03-24 Create boolean IS_OWNER in T131 (N. Tolleshaug)
 *			  2025-05-12 Complete IS_OWNER update code (D Ferguson)
 *			  2025-08-25 Update T168 table to add missing new fields (D Ferguson)
 *			  2025-09-18 Remove T168 updates and T73x additions (now in Seed)(D Ferguson)
 * ***************************************************************************************
 * NOTE 01 - Copy As action - Error from accessing a "No Content" database is not
 * 			 handled correct. The "No Content" database is not released/closed
 * 			 and the project cannot be released - needs HRE restart to be able to delete.
 * 			 HBE exception handling does not work as expected.
 * NOTE 02 - Now implemented Files.copy() as filecopy
 ****************************************************************************************/

import java.awt.Dialog.ModalExclusionType;
import java.awt.Point;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import hre.gui.HG0401HREMain;
import hre.gui.HG0402ProjectOpen;
import hre.gui.HG0405ProjectBackup;
import hre.gui.HG0410ProjectDelete;
import hre.gui.HG0450SuperDialog;
import hre.gui.HG0505AddPerson;
import hre.gui.HGlobal;
import hre.gui.HGlobalCode;

/**
 * Sub class of BusinessLayer handles project administration
 * @author Nils Tolleshaug
 * @since 2019-12-20
 */
public class HBProjectHandler extends HBBusinessLayer {

/**
 *  NOTE defaultDatabaseEngine must be set by user ???
 */
/** Popular SQL database engines and filetypes
 *   #1) Oracle RDBMS - filetype .dbf
 *   #2) IBM DB2
 *   #3) Microsoft SQL Server - filetype .mdf
 *   #4) Sybase - filetype .dbf and many others
 *   #5) SQLite - filetype sqlite
 *   #6) MySQL - filetypes .myd & .myi
 *   #7) H2 - filetype .db.mv
 */

/**
 * ProjectHandler constructor
 * @author Nils Tolleshaug
 * @version v0.01.0025
 * @since 2019-12-09
 */
	public HBProjectHandler() {
		super();
	}

/**
 * Set default database engine
 * @return defaultDatabaseEngine
 */
	@Override
	public String getDefaultDatabaseEngine() {
		return HGlobal.defDatabaseEngine;
	}

/**
 * Convert project list from ArrayList<String[]> to array2D[][]
 * @param arrayList to be processed
 * @return String[][] array2D
 */
	public String[][] getUserProjectArray2D(ArrayList<String []> arrayList) {
		String[][] array2D = new String[arrayList.size()][];
		for (int i = 0; i < array2D.length; i++) {
			String[] row = arrayList.get(i);
			array2D[i] = row;
		}
		return array2D;
	}

/**
 * Get String[][] from openProjects and populate JTable
 * @return String[][]
 */
	public String[][] getOpenProjectArray2D() {
		String[][] array2D = new String[HGlobal.openProjects.size()][];
		for (int i = 0; i < array2D.length; i++) {
			String[] row = HGlobal.openProjects.get(i).getProjectData();
			array2D[i] = new String[]  {" " + row[0],	//project name
										" " + row[1],	//file name
										" " + row[3]};	//server name
			if (array2D[i][2].trim().equals(HGlobal.thisComputer.trim())) {
				array2D[i][2] = HGlobalCode.getLocalText();
			}
		}
		return array2D;
	}
/**
 * Get String[][] from userProjects and populate JTable
 * @return String[][]
 */
	public String[][] getUserProjectArray2D() {
		String[][] array2D = new String[HGlobal.userProjects.size()][];
		for (int i = 0; i < array2D.length; i++) {
			String[] row = HGlobal.userProjects.get(i);
			array2D[i] = new String[]  {" " + row[0],	//project name
										" " + row[1],	//file name
										" " + row[3]};	//server name
			if (array2D[i][2].trim().equals(HGlobal.thisComputer.trim())) {
				array2D[i][2] = HGlobalCode.getLocalText();
			}
		}
		return array2D;
	}

/**
 * Check if project name is in available projects
 * @param name - project name
 * @param arrayList - list of userProjects
 * @return - found = true
 */
	public boolean isProjectNameUsed(String name, ArrayList<String []> arrayList) {
		for (String[] element : arrayList) {
			if (element[0].trim().equals(name.trim())) {
				return true;
			}
		}
		return false;
	}

/**
 * Check if project are already open
 * @param name - Nmae of Project
 * @param arrayList - list of openProjects
 * @return - if in use return true
 */
	private boolean isProjectNameOpen(String name) {
		ArrayList<HBProjectOpenData> arrayList = HGlobal.openProjects;
		for (HBProjectOpenData element : arrayList) {
			if (element.projectName.trim().equals(name.trim())) {
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
		if (i < HGlobal.userProjects.size()) {
			return HGlobal.userProjects.get(i);
		}
		throw new HBException("getUserProjectByIndex\nArrayList index out of range: " + i);
	}

/**
 * Return project String[] with name from userProjects ArrayList
 * @param name Name of project to return
 * @return String[] with project data
 * @throws HBException
 */
	public int getUserProjectByName(String name) throws HBException {
		for (int i = 0; i < HGlobal.userProjects.size(); i++) {
			if (HGlobal.userProjects.get(i)[0].trim().equals(name.trim())) {
				return i;
			}
		}
		throw new HBException("getUserProjectByName\nProject is not a known projects: " + name);
	}

/**
 * Return project String[] with name from openProjects ArrayList
 * @param name Name of project to return
 * @return String[] with project data
 * @throws HBException
 */
		public int getOpenProjectByName(String projectName) throws HBException {
			for (int i = 0; i < HGlobal.openProjects.size(); i++) {
				if (HGlobal.openProjects.get(i).getProjectData()[0].trim().
						equals(projectName.trim())) {
					return i;
				}
			}
			throw new HBException("getOpenProjectByName\n"
					+ "Open project not in list of open projects: " + projectName);
		}

/**
 * Collects open project summary data from openProjectData
 * @param projectName
 * @return
 * @throws HBException
 */
	public String[][] getSummaryOpenProjectAction(String projectName) throws HBException {
			int indexOpenProject = getOpenProjectByName(projectName);
			String [] project = HGlobal.openProjects.get(indexOpenProject).getProjectData();
			return HGlobal.openProjects.get(indexOpenProject).getSummaryData(project);
	}

/**
 * Collects open project summary data from userProjects
 * @param projectName name of the selected project
 * @return String [][] summary
 */
	public String[][] getSummaryUserProjectAction(String projectName) throws HBException {
		if (isProjectNameOpen(projectName)) {
			return getSummaryOpenProjectAction(projectName);
		}
		int indexUserProject = getUserProjectByName(projectName);
		String [] project = HGlobal.userProjects.get(indexUserProject);
		return HGlobalCode.getSummaryData(project);

	}
/**
 * Add project to project list
 * @param projectData String[] with project to be added
 */
	public void addProjectToList(String [] projectData) {
		HGlobal.userProjects.add(projectData);
	}

/**
 * Return number of projects in project list
 * @return number of elements in ArrayList userProjects
 */
	public int getSizeProjectList(String [] projectData) {
		return HGlobal.userProjects.size();
	}

/**
 * Find password for user in focus project data from T131_USERS in focus projects
 * @param userID
 * @return
*/
	public String getPassWord(String userID) throws HBException {
		//try {
			String  projectName = HG0401HREMain.mainFrame.getSelectedOpenProject().getProjectName();
			int openIndex = getOpenProjectByName(projectName);
			HBProjectOpenData focusProjectPointer = HGlobal.openProjects.get(openIndex);
			String	passWord = pointLibraryResultSet.findPassword(userID,
					focusProjectPointer.pointSchemaDefTable);
			return passWord;
	}

/**
 * Create a new project to be included in ArrayList userProjects.
 * Called from User GUI main menu New Project
 * @return true if userGUI shall dispose() window
*/
	public int newProjectAction(String[] newProjectArray) {
		String newProjectName = newProjectArray[0];
		try {
		// If project name already used ?
			if (isProjectNameUsed(newProjectName,HGlobal.userProjects)) {
				return 1;
			}

		//Copy seed to selected folder
			String chosenFileName = newProjectArray[1];
			String chosenFolder = newProjectArray[2];

		// File chooser strips off file type and must be added **** Temp solution
			pointLibraryBusiness.copyFile(HGlobal.seedProjectFile, chosenFolder + File.separator + chosenFileName + ".mv.db");

/**
 *  The following code with copy and progress monitor cannot be used since
 *  the copy process runs in a new Thread. Copy must be the last action to do
 *  if the code shall be used:
 *  pointLibDatabase.setupUpCopyFile(HGlobal.seedProjectFile,chosenFolder + File.separator + chosenFileName + ".mv.db");
 */
		// Find database loc for H2 connect
			String dataBaseFilePath = pointLibraryBusiness.getDatabaseFilePath(newProjectArray);

			if (HGlobal.DEBUG) {
				System.out.println("HBProjectHandler - New Project - DatabasePath: "
						+ dataBaseFilePath);
			}

			if (!pointLibraryResultSet.setDatabaseProjectName(newProjectName,dataBaseFilePath)) {

			// Delete copied database file
				pointLibraryBusiness.deleteFile(chosenFolder + File.separator + chosenFileName + ".mv.db");
				return 3;
			}
			// Update user table data for project
				pointLibraryResultSet.updateNewUserInProject(dataBaseFilePath, HGlobal.userCred);

			// Create database user
				HBToolHandler pointToolHandler = (HBToolHandler) HG0401HREMain.pointBusinessLayer[8];

			// Update user in database
				pointToolHandler.createNewDatabaseUser((String)HGlobal.userCred[0],
													   (String)HGlobal.userCred[2],
													   dataBaseFilePath);

			// Include new project name in user project list
				addProjectToList(newProjectArray);

			// Update userAUX
				HB0744UserAUX.writeUserAUXfile();

			// Open project
				int errorCode = openProjectLocal(newProjectName);
				if (errorCode == 0) {
					return 0;
				}
				return 2;

		} catch(HBException hbe) {
			if (HGlobal.DEBUG) {
				System.out.println("New Project creation failed: " + hbe.getMessage());
			}
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Error New Project: " + hbe.getMessage());
				HB0711Logging.printStackTraceToFile(hbe);
			}
			if (hbe.getMessage().startsWith("ERR2")) {
				return 5;
			}
			return 4;
		}
	}

/**
 * Actions for opening a local project from userProjects and include project into openProjects.
 * Called from User GUI Main menu and OpenProject
 * @param selectedProjectName selected project in file chooser
 * @return true if userGUI shall dispose() window
 */
	public int openProjectLocal(String selectedProjectName) {
		try {
			int selectIndex = getUserProjectByName(selectedProjectName);
			String [] projectSelected = getUserProjectByIndex(selectIndex);

			if (HGlobal.DEBUG) {
				System.out.println("HBProjectHandler openProjectLocal EDT: "
						+ SwingUtilities.isEventDispatchThread());
			}

		// Set user login for local connect to database
			String[] logonData = {projectSelected[5],"SA",""};
			return openProjectAction(false, selectedProjectName, logonData);
		} catch(HBException hbe) {
			if (HGlobal.DEBUG) {
				System.out.println("HBProjectHandler - openProjectAction - Open Project error: \n" + hbe.getMessage());
			}
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("HBProjectHandler - Error in openProjectAction");
				HB0711Logging.printStackTraceToFile(hbe);
			}
			return 2;
		}
	}

/**
 * Actions for opening a remote project from userProjects and include project into openProjects.
 * Called from User GUI Main menu and OpenProject
 * @param selectedProjectName selected project in file chooser
 * @return true if userGUI shall dispose() window
 */
	public int openProjectAction(boolean remote, String selectedProjectName, String[] loginData) {
		if (HGlobal.DEBUG) {
			System.out.println("Project name: "  + selectedProjectName);
		}

		try {
/**
 * Need check of valid project database name from selected project
 */
			int selectIndex = getUserProjectByName(selectedProjectName);
			String [] projectSelected = getUserProjectByIndex(selectIndex);

			if (HGlobal.DEBUG) {
				System.out.println("Check if open: " + projectSelected[0]);
			}

	    // If project already open - reject open.
			if (isProjectNameOpen(projectSelected[0])) {
				getOpenProjectByName(projectSelected[0]);
				mainFrame.setStatusAction(1);
				return 2;
			}
			// Project not open - continue
					int proIndex = HGlobal.userProjects.indexOf(projectSelected);
					if (HGlobal.DEBUG) {
						System.out.println("Project name: " + getUserProjectByIndex(proIndex)[0]);
					}
			// Open project
					HBProjectOpenData openProject = new HBProjectOpenData(this);
					
			// Set up all project manager for new project and initiate project
					openProject.openProject(remote, proIndex, loginData);

				// Add the open project to the list of open projects
					HGlobal.openProjects.add(openProject);

			// New code for recording the selectedProject and update pointer
					mainFrame.setSelectedOpenProject(HGlobalCode.pointOpenProjectByName(selectedProjectName));

			// Add to Number of open projects and set Status bar entries
					HGlobal.numOpenProjects++;
					if (remote) {
						mainFrame.setStatusAction(9, openProject.getProjectData()[3]);
					} else {
						mainFrame.setStatusAction(2);
					}

					mainFrame.setStatusProject(selectedProjectName);

					if (HGlobal.TIME) {
						HGlobalCode.timeReport("complete open project: " + openProject.getProjectName());
					}

			// Add new person if empty project
					openProject.getT401Persons().last();
					if (openProject.getT401Persons().getRow() == 0) {
						HG0401HREMain.mainFrame.setSelectedOpenProject(openProject);
						addFirstPerson();
					}
					else {
						openProject.resetOpenWindows(); // Reopen windows previously open before HRE close
					}
					return 0;

		} catch(HBException | SQLException hbe) {
			if (HGlobal.DEBUG) {
				System.out.println(" HBProjectHandler - openProjectLocal/Remote - Open project error: \n"
								+ hbe.getMessage());
			}
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("HBProjectHandler - Error in openProjecLocal/Remote");
				HB0711Logging.printStackTraceToFile(hbe);
			}
			return 1;
		}
	}

/**
 * addFirstPerson()  when project empty add first person and modify database
 * @throws HBException
 */
	private void addFirstPerson() throws HBException {
		int sexIndex = 0;
        HBProjectOpenData pointOpenProject = HG0401HREMain.mainFrame.getSelectedOpenProject();
        HBPersonHandler pointPersonHandler = pointOpenProject.getPersonHandler();

     // Modify columns T404_PARTNER
        int databaseIndex = pointOpenProject.getOpenDatabaseIndex();

	// Set IS_IMPORTED = FALSE in T126_PROJECTS
		updateTableData("UPDATE T126_PROJECTS SET IS_IMPORTED = FALSE WHERE PROJECT_CODE = 1", databaseIndex);

	// Set boolean IS_OWNER in T131
		updateTableInBase("T131_USER", "UPDATE", "SET IS_OWNER = TRUE WHERE PID = 1000000000000001", databaseIndex);

		initiateDateFormat(3); // set initial date format	use index = 0 to 9

		dateFormatSelect(); // set dateFormat index

     // Activate add new first person
        HG0505AddPerson addScreen = pointPersonHandler.activateAddPerson(pointOpenProject, sexIndex);
		addScreen.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		Point xymainPane = HG0401HREMain.mainPane.getLocationOnScreen();
		addScreen.setLocation(xymainPane.x + 50, xymainPane.y + 50);
		addScreen.setAlwaysOnTop(true);
		addScreen.setVisible(true);
	}


/**
 * openBrowseAction
 * Create a project entry from found database file and open it
 * @param newProjectArray
 * @return error code
 */
	public int openBrowseAction(String[] newProjectArray, HG0402ProjectOpen pointOpenBrowse) {

		String projectName = "";

		// Find HRE database file path
		String dataBaseFilePath = pointLibraryBusiness.getDatabaseFilePath(newProjectArray);

		try {
				projectName = pointLibraryResultSet.getDatabaseProjectName(dataBaseFilePath);

				if (HGlobal.DEBUG) {
					System.out.println("Database projectName: " + projectName);
				}
				newProjectArray[0] = projectName;

			// Is project name already used ?
				if (isProjectNameUsed(projectName, HGlobal.userProjects)) {
					pointOpenBrowse.setNewProjectName(projectName);
					return 1;
				}

		// If projectName equals "No name found" the project has no table with project name
			if (projectName.equals("No name found")) {
				return 3;
			}
			addProjectToList(newProjectArray);
			int errorCode = openProjectLocal(projectName);
			if (errorCode == 0) {

// Update userAUX and write log entry
				HB0744UserAUX.writeUserAUXfile();
				return 0;
			}
			return 2;

		} catch (HBException hbe) {
			if (HGlobal.DEBUG) {
				System.out.println("Error openBrowseAction database - Project name: " + hbe.getMessage());
			}
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Error openBrowseAction database");
				HB0711Logging.printStackTraceToFile(hbe);
			}
			return 4;
		}
	}

/**
 * Close an open project.
 * Remove project from ArrayList openProjects in HGlobal
 * and connectedDataBases in DatabaseLayer
 * @param selectedProjectName selected project in file chooser
 * @return true if userGUI shall dispose() window
 */
	public int closeProjectAction(String selectedProjectName) {
		if (HGlobal.DEBUG) {
			System.out.println("closeProjectAction() - Project: "  + selectedProjectName);
		}
		try {
			// Find openProject index
			int openProjectIndex = getOpenProjectByName(selectedProjectName);

			int databaseIndex = HGlobal.openProjects.get(openProjectIndex).getOpenDatabaseIndex();

			if (HGlobal.DEBUG) {
				System.out.println("Open/database index: "
						+ openProjectIndex + " / " + databaseIndex);
			}

		// Close database connection - also close of all open windows
			HGlobal.openProjects.get(openProjectIndex).closeDatabase(databaseIndex);

		// Remove open projects from ArrayList
			HGlobal.openProjects.remove(openProjectIndex);

		//Update closing time in userProjects
			int selectIndex = getUserProjectByName(selectedProjectName);
			HGlobal.userProjects.get(selectIndex)[4] = currentTime("yyyy-MM-dd / HH:mm:ss");

		//Update UserAUX for last closed time
			HB0744UserAUX.writeUserAUXfile();

		// Update Main Status bar Action item code 3
			mainFrame.setStatusAction(3, selectedProjectName);

		// Remove from Project menu Recent list (with Open icon) and add back in with HRE icon
			mainFrame.removeProjMenuItem(selectedProjectName);
			mainFrame.addProjMenuItem(selectedProjectName);

		// Update number of openProjects
			HGlobal.numOpenProjects--;

		// Now take the last entry of openProjects as the new active Project
		// as either it IS the Active project or it was the last most recent opened one
			if (HGlobal.numOpenProjects > 0) {
				String[] row = HGlobal.openProjects.get(HGlobal.openProjects.size() - 1).getProjectData();

				String newSelectedProject = row[0];
				mainFrame.setStatusProject(newSelectedProject);

				String newActiveProject = row[0];
				mainFrame.setStatusProject(newActiveProject);
				try {
	            	mainFrame.setSelectedOpenProject(HGlobalCode.pointOpenProjectByName(newActiveProject));
					} catch (HBException hbe) {
						System.out.println("Project Status: setSelectedOpenProject " +  newActiveProject);
						hbe.printStackTrace();
					}
				}

		// Now let HG0407ProjectClose or HG0405ProjectBackup set Active Project
		// to 'none' if there are no projects left open
			return 0;

		} catch(HBException hbe) {
			if (HGlobal.DEBUG) {
				System.out.println("closeProjectAction - Close Project error: \n" + hbe.getMessage());
			}
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("closeProjectAction - Close Project");
				HB0711Logging.printStackTraceToFile(hbe);
			}
			return 1;
		}
	}

/**
 * Do necessary operation when deleting a project.
 * Remove project from ArrayList userProjects in HGlobal if not open
 * @param selectedProjectName selected project in file chooser
 * @return true if userGUI shall dispose() window
 */
	public int deleteProjectAction(HG0410ProjectDelete pointProjectDelete, String selectedProjectName, String selectedServerName) {
		try {
			int selectIndex = getUserProjectByName(selectedProjectName);
			String [] projectSelected = getUserProjectByIndex(selectIndex);

			if (HGlobal.DEBUG) {
				System.out.println("Check if open before delete: " + projectSelected[0]);
			}

	    // If project open - reject delete.
			if (isProjectNameOpen(projectSelected[0])) {
				return 1;
			}

		// Delete database file if User confirms (but if remote project, don't even ask)
			String deleteFilePath = projectSelected[2] + File.separator + projectSelected[1] + ".mv.db";
			String deleteTracePath = projectSelected[2] + File.separator + projectSelected[1] + ".trace.db";

		// ONLY if the database's server is this PC, do we ask whether to delete (cannot delete a remote project)
			if (selectedServerName == HGlobal.thisComputer) {
				if (pointProjectDelete.keepDatabaseFile(deleteFilePath)) {
					if (HGlobal.DEBUG) {
						System.out.println("Selected database DELETED: " + deleteFilePath);
					}
					pointLibraryBusiness.deleteFile(deleteFilePath);
					pointLibraryBusiness.deleteFile(deleteTracePath);
				} else if (HGlobal.DEBUG) {
					System.out.println("Database REMAINS on PC: " + deleteFilePath);
				}
			}
	    // Remove from userProject list
	    	if (!HGlobal.userProjects.remove(projectSelected)) { // true if object in ArrayList and removed
				return 2;
			}
			if (HGlobal.DEBUG) {
				System.out.println("Project named: " + selectedProjectName + " deleted");
			}

			// Update userAUX and write log entry
			HB0744UserAUX.writeUserAUXfile();

   // If the last closed project is deleted, set last closed to "";
			if (HGlobal.lastProjectClosed.trim().equals(projectSelected[0].trim())) {
				if (HGlobal.DEBUG) {
					System.out.println("Last open project deleted: "
															+ HGlobal.lastProjectClosed);
				}
				HGlobal.lastProjectClosed = "";
			}

			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Action: delete HG0410 Deleted: " + selectedProjectName);
			}
			return 0;

		} catch(HBException hbe) {
			if (HGlobal.DEBUG) {
				System.out.println("Delete project failed: \n" + hbe.getMessage());
			}
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("deleteProjectAction - delete failed");
				HB0711Logging.printStackTraceToFile(hbe);
			}
			return 3;
		}
	}

/**
 * Backup of selected project to backup folder
 * @param selectedProjectName - project name selected
 * @param fileBackupPath - file path to backupfolder
 * @return - errorCode 0 (successful), 1 (project is open), 2 (backup failed)
 * @throws
 */
	public int backupProjectAction(String selectedProjectName,String fileBackupPath) {
		return backupProjectAction(null, selectedProjectName,fileBackupPath);
	}

	public int backupProjectAction(HG0450SuperDialog pointBackUp, String selectedProjectName,String fileBackupPath)  {
		int selectIndex;
		try {
		// Get project database file name
			selectIndex = getUserProjectByName(selectedProjectName);
			String [] projectSelected = getUserProjectByIndex(selectIndex);

		// If project open, then reject backup
			if (isProjectNameOpen(projectSelected[0])) {
				((HG0405ProjectBackup) pointBackUp).userInfoBackupProject(1, selectedProjectName, fileBackupPath);
		// and close project
				closeProjectAction(selectedProjectName);
			}

		// Get database file path
			String dataBaseFilePath = projectSelected[2] + File.separator + projectSelected[1] + ".mv.db";
			String databaseFileName = projectSelected[1] + ".mv.db";

			if (HGlobal.DEBUG) {
				System.out.println("Database file name: " + databaseFileName);
			}

		// Create zip file
			pointLibraryBusiness.createZipFile(dataBaseFilePath, fileBackupPath + ".hrez");

			if (HGlobal.DEBUG) {
				System.out.println("HBProjectHandler - backupProjectAction():"
						+ "\nFrom project: " + dataBaseFilePath
						+ "\nTo: " + fileBackupPath + ".hrez");
			}

		// Save the Backup date/time - use the filename timestamp not the time 'now', to avoid confusion
			projectSelected[6] = fileBackupPath.substring(fileBackupPath.length()-19);		// extract the date_time from the filename
			projectSelected[6] = projectSelected[6].replace("_", " / ");					// change the underscore to slash
			return 0;

		} catch (HBException hbe) {
			if (HGlobal.DEBUG) {
				System.out.println("Backup project failed: \n" + hbe.getMessage());
			}
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("backupProjectAction - backup failed");
				HB0711Logging.printStackTraceToFile(hbe);
			}
			return 2;
		}
	}

/**
 * Backup of selected External file folder to a zip file
 * @param backupFolderName - External file folder to backup
 * @param zipFileCreate - complete zip folder+file to be ouput
 * @throws HBException
 */
	public int backupExternalAction(String backupFolderName, String zipFileCreate)  {
		try {
		// Create the External files backed up into a zip file
			pointLibraryBusiness.zipFolder(backupFolderName, zipFileCreate);
			return 0;

		} catch (HBException hbe) {
			if (HGlobal.DEBUG) {
				System.out.println("External folder backup project failed: \n" + hbe.getMessage());
			}
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("externalBackupProjectAction - externalBackup failed");
				HB0711Logging.printStackTraceToFile(hbe);
			}
			return 1;

		}
	}

/**
 * Restore to new Project
 * @param newProjectArray
 * @param fromBackupFilePath
 * @return
 */
	public int restoreProjectActionNew(String [] newProjectArray,String fromBackupFilePath) {
		try {
			// Get database full path and filename
			String toDatabaseRestoreFilePath = newProjectArray[2] + File.separator + newProjectArray[1] + ".mv.db";

			if (HGlobal.DEBUG) {
				System.out.println("HBProjectHandler - restoreProjectAction():"
						+ "\nFrom backup: " + fromBackupFilePath
						+ "\nTo location: " + toDatabaseRestoreFilePath);
			}
			// Extract from backup zip file and restore
			pointLibraryBusiness.extractZip(fromBackupFilePath,toDatabaseRestoreFilePath);

			// Find database loc for H2 connect
			String dataBaseFilePath = pointLibraryBusiness.getDatabaseFilePath(newProjectArray);
			// Rename restored project to required name
			pointLibraryResultSet.setDatabaseProjectName(newProjectArray[0],dataBaseFilePath);

			// Update list of known projects
			addProjectToList(newProjectArray);

			// Update userAUX
			HB0744UserAUX.writeUserAUXfile();

			return 0;
		} catch (HBException hbe) {
			if (HGlobal.DEBUG) {
				System.out.println("Restore project new failed: \n" + hbe.getMessage());
			}
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("restoreProjectAction - restore new failed");
				HB0711Logging.printStackTraceToFile(hbe);
			}
			return 2;
		}
	}

/**
 * Restore database file for selected project
 * @param restoreProjectName
 * @param fromBackupFilePath
 * @return - 0 if window shall be disposed
 */

	public int restoreProjectActionSelect(String restoreProjectName,String fromBackupFilePath) {
		int selectIndex;
		try {
		// Get project database file name
			selectIndex = getUserProjectByName(restoreProjectName);
			String [] projectSelected = getUserProjectByIndex(selectIndex);

		// If project open - reject delete.
			if (isProjectNameOpen(projectSelected[0])) {
				return 1;
			}

		// Get database file path
			String toDatabaseRestoreFilePath = projectSelected[2] + File.separator + projectSelected[1] + ".mv.db";
			String restorePath = projectSelected[2];
			pointLibraryBusiness.extractZip(fromBackupFilePath,restorePath);

			if (HGlobal.DEBUG) {
				System.out.println("HBProjectHandler - restoreProjectAction():"
						+ "\nFrom backup: " + fromBackupFilePath
						+ "\nTo location: " + toDatabaseRestoreFilePath);
			}

			return 0;
		} catch (HBException hbe) {
			if (HGlobal.DEBUG) {
				System.out.println("Restore project failed: \n" + hbe.getMessage());
			}
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("restoreProjectAction - restore failed");
				HB0711Logging.printStackTraceToFile(hbe);
			}
			return 2;
		}
	}

/**
 * Restore zipped External Folder content
 * @param restoreExtName
 * @param toFolder
 * @return - 0 if window shall be disposed
 */
	public int restoreExternalAction(String restoreExtName,String toFolder) {
		try {
			// perform the unzip and restore of files and folders
			pointLibraryBusiness.extractZipFolder(restoreExtName, toFolder);

			if (HGlobal.DEBUG) {
				System.out.println("HBProjectHandler - restoreExternalAction():"
						+ "\nFrom zip file: " + restoreExtName
						+ "\nTo location: " + toFolder);
			}
			return 0;

		} catch (HBException hbe) {
			if (HGlobal.DEBUG) {
				System.out.println("Restore of external files failed: \n" + hbe.getMessage());
			}
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("externaRestoreProjectAction - restore failed");
				HB0711Logging.printStackTraceToFile(hbe);
			}
			return 1;
		}
	}

/**
 * Rename project in userProjects and in database
 * Called from User GUI Rename Project
 * @param selectedProjectArray
 * @param newProjectName
 * @return erroCode 0 (good) 1-3 (if errors)
 */
	public int renameProjectAction(String selectedProjectName, String newProjectName) {
		try {
			int index = getUserProjectByName(selectedProjectName);
			String[] selectedProjectArray = getUserProjectByIndex(index);
			if (HGlobal.DEBUG) {
				System.out.println("renameProjectAction - start\n"
					+ "New project name: " +  newProjectName);
			}

		// If project name already used ?
			if (isProjectNameUsed(newProjectName,HGlobal.userProjects)) {
				return 1;
			}

		// If project open - reject delete.
			if (isProjectNameOpen(selectedProjectArray[0])) {
				return 2 ;
			}

			if (HGlobal.DEBUG) {
				System.out.println("Folder: " + selectedProjectArray[2]
						+ "File name: " + selectedProjectArray[1]);
			}

		// Find database loc for H2 connect
			String dataBaseFilePath = pointLibraryBusiness.getDatabaseFilePath(selectedProjectArray);

			if (HGlobal.DEBUG) {
				System.out.println("Database file path: " + dataBaseFilePath);
			}

			if (pointLibraryResultSet.setDatabaseProjectName(newProjectName,dataBaseFilePath)) {

		// Rename project in list of userProjects
				selectedProjectArray[0] = newProjectName;

		// Update userAUX
				HB0744UserAUX.writeUserAUXfile();
				return 0; // removed openProjectAction(newProjectName);
			}
			return 3;

		} catch(HBException hbe) {
			if (HGlobal.DEBUG) {
				System.out.println("Project Rename failed: \n" + hbe.getMessage());
			}
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("renameProjectAction - rename failed");
				HB0711Logging.printStackTraceToFile(hbe);
			}
			return 3;
		}
	}

/**
 * Copy a database project file to another location
 * Called from
 * @param copyFrom
 * @param copyTo
 * @return - 0 if success and userGUI shall dispose() window
 */
	public int copyProjectAction(File copyFrom, File copyTo, String [] newProjectArray) {
		if (HGlobal.DEBUG) {
			System.out.println("copyProjectAction - start\n"
						+ "Copy from: " + copyFrom.getName()
						+ "\nCopy to: " + copyTo.getName()
						+ "\nNew project: " + newProjectArray[0] );
		}
		try {
		// If project name already used ?
			if (isProjectNameUsed(newProjectArray[0],HGlobal.userProjects)) {
				return 1;
			}

		// Check if copy to file exist
			if (copyTo.exists()) {
				return 2;
			}

		// Check if database file is in use??

		// Dump Project array
			if (HGlobal.DEBUG) {
				for (int i=0; i < 6;i++) {
					System.out.println(i + " - " + newProjectArray[i]);
				}
			}
/**
 * 	NOTE 02	-standard Files.copy(src,dst) implemented
 */
		// copy File without progress monitor
			pointLibraryBusiness.copyFile(copyFrom.getPath(),copyTo.getPath());

		// Update list of known projects
			addProjectToList(newProjectArray);

/**
 * NOTE 01 - Copy with progress monitor is removed - does not work as expected
 * Process Copy run as new Thread independent of main Thread
 * Process copy not completed when copyProjectAction continues.
 * Therefore we cannot modify database project name in T_131
 * Last action point in Copy Project AS need to be copy

			JOptionPane.showMessageDialog(null, "Created copy of project: " + newProjectArray[0]
					+ "\nProject name not updated in database"
					+ "\nUse Project Rename to update project name!", "Copy Project AS",
					JOptionPane.WARNING_MESSAGE);

			pointLibraryBusiness.setupUpCopyFile(copyFrom, copyTo);
*/
			String dataBaseFilePath = pointLibraryBusiness.getDatabaseFilePath(newProjectArray);

			if (pointLibraryResultSet.setDatabaseProjectName(newProjectArray[0],dataBaseFilePath)) {
				// Update userAUX
				HB0744UserAUX.writeUserAUXfile();
				return 0;
			}

			return 3;

		} catch (HBException hbe) {
			if (HGlobal.DEBUG) {
				System.out.println("ERROR copyProjectAction:\n" + hbe.getMessage());
			}
			if (hbe.getMessage().contains("IOException")) {
				if (HGlobal.writeLogs) {
					HB0711Logging.logWrite("copyProjectProjectAction - copy rejected");
					HB0711Logging.printStackTraceToFile(hbe);
				}
				return 3;
			}
			if (HGlobal.DEBUG) {
				System.out.println("copyProjectAction - ERROR\n"
						+ "Copy from: " + copyFrom.getName()
						+ "\nCopy to: " + copyTo.getName()
						+ "\nNew project: " + newProjectArray[0] );
			}
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("copyProjectProjectAction - copy failed");
				HB0711Logging.printStackTraceToFile(hbe);
			}
			return 4;
		}
	}
} // End ProjectHandler
