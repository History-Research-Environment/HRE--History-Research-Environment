package hre.bila;
/*****************************************************************************************
  * Class HBWhereWhenHandler extends BusinessLayer
  * Processes data for Where When Menu in User GUI
  * Receives requests from User GUI to action methods
  * Sends requests to database over Database Layer API
  ****************************************************************************************
  * v0.00.0016 2019-12-20 - First version (N. Tolleshaug)
  * v0.00.0023 2020-09-13 -	Initiate open window for HG0507LocationSelect (N. Tolleshaug)
  * 		   2020-09-14 -	Initiate registration of open window (N. Tolleshaug)
  * v0.00.0025 2020-11-02 - Implemented location table with place data
  * 					  - and list of event ID for each location (N. Tolleshaug)
  * 		   2020-12-02 - Exception handling improved
  *   		   2020-12-29 - Changed to T552_LOCATION_NAMES for location data (N. Tolleshaug)
  * 		   2021-01-02 - Correct initial position empty database (N. Tolleshaug)
  * v0.01.0025 2021-01-28 - removed use of HGlobal.selectedProject (N. Tolleshaug)
  * 		   2021-02-08 - Implemented column control HG0581ConfigureTable (N. Tolleshaug)
  *  		   2021-02-27 - Implemented list of events table in Location VP (N. Tolleshaug)
  *            2021-03-06 - Implemented handling of 5 Location VP (N. Tolleshaug)
  * v0.01.0026 2021-05-16 - Included both DDL20a and DDL21a (N. Tolleshaug)
  * 		   2021-06-04 - Removed empty line with no placedata (N. Tolleshaug)
  * 		   2021-07-05 - Preloading ResultSet for location name element (N. Tolleshaug)
  * v0.01.0027 2021-11-09 - Indexed T_553 loction name elements (N. Tolleshaug)
  *			   2021-12-02 - Method getLocationEvents returns null if no data (N. Tolleshaug)
  *			   2021-12-11 - Included DDL20a, DDL21a and DDL21c(N. Tolleshaug)
  *			   2021-12-15 - New processing of DDL21c location ViewPoint (N. Tolleshaug)
  *			   2022-01-01 - Progress Bar update for LocationSelect (N. Tolleshaug)
  * 		   2022-02-26 - Modified to use the NLS version of HGlobal (D Ferguson)
  * 		   2022-08-02 - Handling of HG0508ManageLocation (N. Tolleshaug)
  * 		   2022-08-03 - Change location name style (N. Tolleshaug)
  * 	   	   2022-08-20 - Fix open Manage Location from main new database (N. Tolleshaug)
  * 		   2022-08-28 - Location name style handling (N. Tolleshaug)
  *v0.01.0028  2022-09-07 - Activated HG0526ManageLocationNameStyles (N. Tolleshaug)
  *  		   2022-10-01 - Changed date translation to v21h tables (N. Tolleshaug)
  *  		   2022-10-13 - Modified T160, T162 and T163 (N. Tolleshaug)
  *   		   2022-10-25 - Name style description default "null|" in v21i database (N. Tolleshaug)
  *   	       2022-12-10 - Location Name style controls table setup (N. Tolleshaug)
  *   		   2023-01-19 - New processing of Location Select table data (N. Tolleshaug)
  *   		   2023-01-26 - Final update of T302 to Done after processing (N. Tolleshaug)
  *   		   2023-01-26 - Added name elements for Location in LS table (N. Tolleshaug)
  *   		   2023-02-08 - Implemented show and store location name style (N. Tolleshaug)
  *   		   2023-02-22 - Moved all exhibit image processing to HBMediaHandler (N. Tolleshaug)
  *   		   2023-02-28 - ID is now Visible ID from Location table (N. Tolleshaug)
  *   		   2023-03-20 - Location name element T553 update table (N. Tolleshaug)
  *   		   2023-04-10 - Support for 3 open projects (N. Tolleshaug)
  *v0.03.0030  2023-08-19 - Updated to V22a for Location Name Manager (N. Tolleshaug)
  *			   2023-08-24 - Updated for start/end date in location Manager (N. Tolleshaug)
  *			   2023-09-16 - Added class AddEventRecord for creating events (N. Tolleshaug)
  *v0.03.0031  2023-11-11 - Updated to V22b for add person (N. Tolleshaug)
  *			   2023-11-21 - Updated for add person and add parent (N. Tolleshaug)
  *			   2024-01-06 - Updated for add sibling and add partner (N. Tolleshaug)
  *			   2024-03-01 - Add alphabetic sort of Event Lists by event name (D Ferguson)
  *			   2024-03-01 - Updated for handling no role for event (N. Tolleshaug)
  *			   2024-03-08 - Fix for enpty location name data - line 1651 (N. Tolleshaug)
  *			   2024-03-21 - New handling of date, location and memo (N. Tolleshaug)
  *			   2024-03-22 - Fix of updateElementData() - update owner (N. Tolleshaug)
  *			   2024-03-22 - New handling of date, location and memo (N. Tolleshaug)
  *			   2024-03-25 - Fix for dbindex problem new project (N. Tolleshaug)
  *			   2024-04-04 - Add collection of roleName, Num, Seq to roleData (D Ferguson)
  *			   2024-05-20 - Implemented add, delete and edit assocites (N. Tolleshaug)
  *			   2024-05-26 - New add/edit event handling (N. Tolleshaug)
  *			   2024-07-14 - New handling og event Hdates (N. Tolleshaug)
  *			   2024-07-24 - Updated for use of HG0590EditDate (N Tolleshaug)
  *			   2024-07-28 - Updated for use of G0547TypeEvent (N Tolleshaug)
  *			   2024-08-04 - Updated event create new location if not exist (N Tolleshaug)
  *			   2024-08-17 - Fix for add partner event from main add partner (N Tolleshaug)
  *			   2024-10-05 - Fix for add partner event - double PS (N Tolleshaug)
  *			   2024-10-19 - Added updateParentTableEvnt() to update parent relation (N Tolleshaug)
  *			   2024-11-08 - Remove duplicate assocs in Edit Event (N. Tolleshaug)
  *			   2024-11-19 - Updated location style handling (N. Tolleshaug)
  *			   2024-11-19 - Updated location name element handling (N. Tolleshaug)
  *			   2024-11-20 - Updated findLocationNameTablePID to return null_RPID (N. Tolleshaug)
  *			   2024-11-23 - Updated event update for witnessed events (N. Tolleshaug)
  *			   2024-11-28 - Updated event location style handling (N. Tolleshaug)
  * v0.03.0032 2025-01-11 - Rearranged processing of event and roles (N. Tolleshaug)
  * 		   2025-01-30 - Updated setting og language code for role/event (N. Tolleshaug)
  * 		   2025-02-18 - Fixed warning unused code (N. Tolleshaug)
  * 		   2025-04-27 - Added activation code for activateAddEvent (N. Tolleshaug)
  * 		   2025-05-08 - Updated for save data for new event (N.Tolleshaug)
  * 		   2025-05-09 - Reload associate and citation event add/edit(N.Tolleshaug)
  * 		   2025-05-11 - Update event close remove only added assocs and citations (N. Tolleshaug)
  *			   2025-05-24 - Change activateUpdateEvent for EditEvent key person display (D Ferguson)
  *****************************************************************************************/

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import hre.dbla.HDDatabaseLayer;
import hre.gui.HG0401HREMain;
import hre.gui.HG0507LocationSelect;
import hre.gui.HG0508ManageLocation;
import hre.gui.HG0526ManageLocationNameStyles;
import hre.gui.HG0547AddEvent;
import hre.gui.HG0547EditEvent;
import hre.gui.HG0547PartnerEvent;
import hre.gui.HG0547UpdateEvent;
import hre.gui.HG0551DefineEvent;
import hre.gui.HG0552ManageEvent;
import hre.gui.HGlobal;
import hre.tmgjava.HCException;

/**
  * WhereWhenHandler constructor
  * @author Nils Tolleshaug
  * @version v0.04.0032
  * @since 2019-12-20
  */

public class HBWhereWhenHandler extends HBBusinessLayer {

	public ManageLocationNameData pointManageLocationData;
	private HBProjectOpenData pointOpenProject;
	public HBNameStyleManager pointNameStyleManager;
	public HG0507LocationSelect locationScreen = null;
	private HBPersonHandler pointPersonMannager;
	public HBEventRoleManager pointEventRoleManager;
	public EditEventRecord pointEditEventRecord = null;
	public CreateEventRecord pointCreateEventRecord;
	HG0552ManageEvent pointManageEvent = null;
	HG0547EditEvent pointEditEvent = null;
	HG0551DefineEvent pointDefineEvent = null;

	ArrayList<LocationEventList> locationEventList;
	ArrayList<LocationTableData> locationDataList;

	HashMap<Integer,LocationEventList> hashCodeMap20;
	HashMap<Long,LocationEventList> locationPIDhashMap;
	JProgressBar progBar;

	long null_RPID  = 1999999999999999L;
	long proOffset = 1000000000000000L;
	int dateFormatIndex = 0;
	int dataBaseIndex;
	String langCode;

/**
 * Location data table for LocationSelect
 */
	Object[][] locationData = null;
	private String[] locationNameStyle;
	private String[] personNameStyle;
	boolean locationStyleChanged = true;
	boolean addHdate = false;

// The element codes for the location Select table
	String locationTableCodes;

	protected ResultSet eventsT452data;
	protected ResultSet locationNamesT552data, locationsT551data;
	protected ResultSet hdateT175table;
	protected ResultSet hdateT204data;

	int selectedEventTableRow;

/**
 * 	Constructor HBWhereWhenHandler
 * @throws HBException
 */
	public HBWhereWhenHandler(HBProjectOpenData pointOpenProject) {
		super();
		this.pointOpenProject = pointOpenProject;
		if (pointOpenProject != null) {
			pointEventRoleManager = new HBEventRoleManager(pointOpenProject);
			dataBaseIndex =  pointOpenProject.getOpenDatabaseIndex();
		} else {
			System.out.println(" HBWhereWhenHandler() - pointOpenProject == null!");
		}

		if (HGlobal.DEBUG) {
			System.out.println("HBWhereWhenHandler() - initiated");
		}
	}

/**
 * setlocationStyleChanged(boolean state)
 * @param state
*/
	public void setlocationStyleChanged(boolean state) {
		locationStyleChanged = state;
	}

/**
 * getEventName(int eventNumber)
 * @param eventNumber
 * @return
 * @throws HBException
 */
	public String getEventName(int eventNumber) throws HBException {
		langCode = HGlobal.dataLanguage;
		dataBaseIndex =  pointOpenProject.getOpenDatabaseIndex();
		return pointLibraryResultSet.getEventName(eventNumber, langCode, dataBaseIndex).trim();
	}

/**
 * getEventRoleName(int eventNumber, int roleNumber)
 * @param eventNumber
 * @param roleNumber
 * @return
 * @throws HBException
 */
	public String getEventRoleName(int eventNumber, int roleNumber) throws HBException {
		langCode = HGlobal.dataLanguage;
		dataBaseIndex =  pointOpenProject.getOpenDatabaseIndex();
		return pointLibraryResultSet.getRoleName(roleNumber, eventNumber, langCode, dataBaseIndex).trim();
	}

/**
 * API methods for CreateEventRecord
 * @throws HBException
 */
	public long createLocationRecord(long eventTablePID) throws HBException {
		return pointCreateEventRecord.createLocationRecord(eventTablePID);
	}

/**
 * API methods fir Event and Role manager
 */
	public String[] getEventTypeList(int eventGroup) throws HBException {
		pointEventRoleManager.setSelectedLanguage(HGlobal.dataLanguage);
		return pointEventRoleManager.getEventTypeList(eventGroup);
	}

	public int[] getEventTypes() {
		return pointEventRoleManager.getEventTypes();
	}

	public String[] getEventRoleList(int eventType, String selectRoles) throws HBException {
		return pointEventRoleManager.getRolesForEvent(eventType, selectRoles);
	}

	public Object[][] getEventRoleData(int eventType, String selectRoles) throws HBException {
		return pointEventRoleManager.getRolesDataForEvent(eventType, selectRoles);
	}

	public int[] getEventRoleTypes() {
		return pointEventRoleManager.getEventRoleTypes();
	}

/**
 * API Methods for Edit Event Manager
 * @throws HBException
 */
	public int getDefaultLocationStyleIndex() {
		return pointEditEventRecord.getDefaultLocationStyleIndex();
	}

	public long getLocationNameRecordPID() {
		return pointEditEventRecord.getLocationNameRecordPID();
	}

	public Object[] getAssocTableData(int indexInTable) {
		return pointEditEventRecord.getAssocTableData(indexInTable);
	}

	public String getPartnerNames() {
		return pointEditEventRecord.getPartnerNames();
	}

	public Object[][] getLocationDataTable(int styleIndex) {
		return pointEditEventRecord.getLocationDataTable(styleIndex);
	}

	public String[] getEventLocationTableHeader() {
		return pointEditEventRecord.getLocationTableHeader();
	}

	public String[] getLocationStyles() {
		return pointEditEventRecord.getLocationStyles();
	}

	public void addToLocationChangeList(int selectedIndex, String nameData) {
		pointEditEventRecord.addToLocationChangeList(selectedIndex, nameData);
	}

	public void createEventDates(boolean update, long eventPID, String fieldName, Object[] sortHREDate) throws HBException {
		pointEditEventRecord.createEventDates(update, eventPID, fieldName, sortHREDate);
	}

	public String[] getDates() {
		return pointEditEventRecord.getDates();
	}

	public Object[][] getAssociateTable() {
		return pointEditEventRecord.getAssociateTable();
	}

	public void prepareAssociateTable(long eventPID) throws HBException {
		pointEditEventRecord.prepareAssociateTable(eventPID);
	}

	public long createNewEvent(int selectedEventType, int selectedRoleType) throws HBException {
		return pointEditEventRecord.createNewEvent(selectedEventType, selectedRoleType);
	}

	public void changeEventType(long eventTablePID, int selectedEventNum, int selectedRoleNum) throws HBException {
		pointEditEventRecord.changeEventType(eventTablePID, selectedEventNum, selectedRoleNum);
	}

	public void updateEventPartnerTable(long partnerTablePID, long newEventRecordPID) throws HBException {
		pointEditEventRecord.updateEventPartnerTable(partnerTablePID, newEventRecordPID);
	}

	public void createFromGUIMemo(String memodata) throws HBException {
		pointEditEventRecord.createFromGUIMemo(memodata);
	}

	public String readFromGUIMemo(long memoElementPID) throws HBException {
		return pointEditEventRecord.readFromGUIMemo(memoElementPID);
	}

	public void updateFromGUIMemo(String memodata) throws HBException {
		pointEditEventRecord.updateFromGUIMemo(memodata);
	}

	public long createAssocTableRow(int roleNumber, long personPID) throws HBException {
		return pointEditEventRecord.createAssocTableRow(roleNumber, personPID);
	}

	public long updateAssocTableRow(long assocTablePID, int roleNumber) throws HBException {
		return pointEditEventRecord.updateAssocTableRow(assocTablePID, roleNumber);
	}

/**
 * API Methods for Manage Location Manager
 */
	public boolean detectHiddenElements() {
		return pointManageLocationData.detectHiddenElements();
	}

	public String[] getAvailableStyles() {
		return pointManageLocationData.getAvailableStyles();
	}

	public void setNameStyleIndex(int index) {
		pointManageLocationData.setNameStyleIndex(index);
	}

	public int getDefaultIndex() {
		return pointManageLocationData.getDefaultIndex();
	}

	public Object[][] getLocationNameTable() {
		return pointManageLocationData.getNameElementTable();
	}

	public String[] getLocationTableHeader() {
		return pointManageLocationData.getTableHeader();
	}

	public String[] getNameData() {
		return pointManageLocationData.getNameData();
	}

	public int updateManageLocationNameTable(long locationTablePID) throws HBException {
		return pointManageLocationData.updateManageLocationNameTable(locationTablePID);
	}

	public int updateManageLocationNameTable() throws HBException {
		return pointManageLocationData.updateManageLocationNameTable();
	}

	public void updateStoredNameStyle(int selectIndex, long personNamePID) throws HBException {
		pointManageLocationData.updateStoredNameStyle(selectIndex, personNamePID);
	}

	public void addToNameChangeList(int selectedIndex, String nameData) {
		pointManageLocationData.addToNameChangeList(selectedIndex, nameData);
	}

	public void updateLocationElementData(long locationNamePID) throws HBException {
		pointManageLocationData.updateLocationElementData(locationNamePID);
	}

	public void createLocationNameDates(boolean update, long locationNamePID, String dateFieldName, Object[] locationNameData) throws HBException {
		pointManageLocationData.createLocationNameDates(update, locationNamePID, dateFieldName, locationNameData);
	}

/**
 * Set pointer for program bar
 * @param progBar
 */
	public void setPointProgBar(JProgressBar progBar) {
		this.progBar = progBar;
	}

/**
 * Only for DDL v20b
 * LocationEventList getLocationEvents(int placeIndex)
 * @param placeIndex
 * @return
 */
	public LocationEventList getLocationEvents(int placeIndex) {
		try {
			return locationEventList.get(placeIndex-1);
		} catch (IndexOutOfBoundsException iobe) {
			return null;
		}
	}

/**
 * getLocationTablePID
 * @param placeIndex
 * @return
 */
	public long getLocationTablePID(int placeIndex) {
		try {
				// Mod 28.2.2023 - NTo
				return locationDataList.get(placeIndex).getLocationTablePID();
		} catch (IndexOutOfBoundsException iobe) {
			return null_RPID;
		}
	}

/**
 * ArrayList<LocationEventList> ifExistLocationEventList()
 * @return
 */
	public ArrayList<LocationEventList> ifExistLocationEventList() {
		return locationEventList;
	}

/**
 * Initiate Location Select
 * @param pointOpenProject
 * @return
 */
	public HG0507LocationSelect initiateLocationSelect(HBProjectOpenData pointOpenProject) {

		String screenID = HG0507LocationSelect.screenID;
		int index = pointOpenProject.pointGuiData.getStoreIndex(screenID);

	// Remove Location select if already there
		locationScreen = (HG0507LocationSelect) pointOpenProject.getWindowPointer(index);
		if  (locationScreen != null) {
			locationScreen.dispose();
		}
		dateFormatSelect();

		try {
			 HG0401HREMain.mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// Get Table Control array	for HG0507LocationSelect
			  Object [][] tableControlData = pointOpenProject.pointGuiData.getTableControl(screenID);

		// Initiate tableControl and reset if location name style changed
			  if (tableControlData == null || locationStyleChanged) {
				  pointOpenProject.pointGuiData.cloneTableControl(screenID, tableControlInitLocation(pointOpenProject));
				  tableControlData = pointOpenProject.pointGuiData.getTableControl(screenID);
			  } else {

	    // Reset language translation
				Object[][] headersTranslated = tableControlInitLocation(pointOpenProject);
				for (int i = 0; i < headersTranslated.length; i++) {
					tableControlData[i][0] = headersTranslated[i][0];
				}
			  }

			  if (HGlobal.DEBUG) {
				System.out.println(" HBWhereWhenHandler initiateLocationSelect EDT: "
						  + SwingUtilities.isEventDispatchThread());
			}

			  locationScreen = new HG0507LocationSelect(this,
														pointOpenProject,
														tableControlData);

		// Set size for frame
			  Dimension frameSize = pointOpenProject.pointGuiData.getFrameSize(screenID);
			  if (frameSize != null) {
				locationScreen.setPreferredSize(frameSize);
			}

		// Set location for frame
			  Point location = pointOpenProject.pointGuiData.getFramePosition(screenID);
			  if (location == null) {

		// Sets screen top-left corner relative to parent window
					  locationScreen.setLocation(50, 50);
			  } else {
				locationScreen.setLocation(location.x,location.y);
			}

			  locationScreen.setVisible(true);
			  locationScreen.toFront();

			  HG0401HREMain.mainFrame.setCursor(Cursor.getDefaultCursor());

			 return locationScreen;

		} catch (HBException hbe) {
			if (HGlobal.DEBUG) {
				System.out.println("HBWhereWhenHandler - initiateLocationSelect: " + hbe.getMessage());
			}
				hbe.printStackTrace();
			return locationScreen;
		}
	}

/**
 * finalActionT302(HBProjectOpenData pointOpenProject)
 * @param pointOpenProject
 */
	public void finalActionT302(HBProjectOpenData pointOpenProject) {
	// Mark the window as open in the T302 table
		 String screenID = HG0507LocationSelect.screenID;
		  try {
				pointOpenProject.pointGuiData.setOpenStatus(screenID,
					pointOpenProject.pointT302_GUI_CONFIG,true);

			// Set class Name in GUI config data
				  pointOpenProject.setClassName(screenID,"HG0507LocationSelect");

			// register the open screen on HBOpenprojectData
				  pointOpenProject.registerOpenScreen(locationScreen);

			} catch (HBException hbe) {
				if (HGlobal.DEBUG) {
					System.out.println("HBWhereWhenHandler - finalActionLS: " + hbe.getMessage());
				}
					hbe.printStackTrace();
			}
	}

/**
 * resetLocationSelect() - Reset Location Select
 */
	public void resetLocationSelect(HBProjectOpenData pointOpenProject) {
		pointOpenProject.reloadLocationSelectData = true;
		if (locationScreen != null) {
			initiateLocationSelect(pointOpenProject);
		}
	}

/**
 * public void deleteAssocPerson(int assocTableRow)
 * @param assocTableRow
 * @throws HBException
 */
	public void deleteAssocPerson(int assocTableRow) throws HBException {
		long assocTablePID = pointEditEventRecord.getAssoctablePID(assocTableRow);
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		String selectString = setSelectSQL("*", eventAssocTable, "PID = " + assocTablePID);
		try {
			ResultSet assocTableRS = requestTableData(selectString, dataBaseIndex);
			if (isResultSetEmpty(assocTableRS)) {
				return;
			}
			assocTableRS.beforeFirst();
			while (assocTableRS.next() ) {
				assocTableRS.deleteRow();
				if (HGlobal.DEBUG) {
					System.out.println(" Deleted assoc PID: " + assocTablePID);
				}
			}
			assocTableRS.close();

		} catch (SQLException sqle) {
			throw new HBException(" HBWhereWhenHandler - deleteAssocPerson : " + sqle.getMessage());
		}
	}

/**
 * public void deleteAssocPerson(int assocTableRow)
 * @param assocTableRow
 * @throws HBException
 */
	public void deleteAssocEvents(long eventTablePID) throws HBException {

		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		String selectString = setSelectSQL("*", eventAssocTable, "EVNT_RPID = " + eventTablePID);
		try {
			ResultSet assocTableRS = requestTableData(selectString, dataBaseIndex);
			if (isResultSetEmpty(assocTableRS)) {
				return;
			}
			assocTableRS.beforeFirst();
			while (assocTableRS.next() ) {
				assocTableRS.deleteRow();
				if (HGlobal.DEBUG) {
					System.out.println(" Deleted assoc PID: " + eventTablePID);
				}
			}
			assocTableRS.close();

		} catch (SQLException sqle) {
			throw new HBException(" HBWhereWhenHandler - deleteAssocEvents : " + sqle.getMessage());
		}
	}

/**
 * public void deleteCationEvents(long eventTablePID) throws HBException
 * @param eventTablePID
 * @throws HBException
 */
	public void deleteCitationEvents(long eventTablePID, String tableName) throws HBException {

		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		String selectString = setSelectSQL("*", citationTable, "CITED_RPID = " + eventTablePID
											+ " AND OWNER_TYPE = '" + tableName + "'");
		try {
			ResultSet citationTableRS = requestTableData(selectString, dataBaseIndex);
			if (isResultSetEmpty(citationTableRS)) {
				return;
			}
			citationTableRS.beforeFirst();
			while (citationTableRS.next() ) {
				citationTableRS.deleteRow();
				if (HGlobal.DEBUG) {
					System.out.println(" Deleted citation PID: " + eventTablePID);
				}
			}
			citationTableRS.close();

		} catch (SQLException sqle) {
			throw new HBException(" HBWhereWhenHandler - deleteCitationEvents : " + sqle.getMessage());
		}
	}

/**
 * tableControlInitLocation(HBProjectOpenData pointOpenProject)
 * @param pointOpenProject
 * @return
 * @throws HBException
 */
	private Object [][] tableControlInitLocation(HBProjectOpenData pointOpenProject) throws HBException {
		HashMap<String,String> elementCodeMap = new HashMap<>();
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		Object [][] tableControl = null;
		/**
		 * First HRE default name style does not contain Temple - choose TMG style
		 */
		try {
		// Collect location style coded and set style codes for location table
			ResultSet nameStyleTable = pointLibraryResultSet.getNameStylesTable(nameStyles, "P", dataBaseIndex);
			nameStyleTable.beforeFirst();
			while (nameStyleTable.next()) {
				String nameStyleType = "IS_DEFAULT";
				if (nameStyleTable.getBoolean(nameStyleType)) {
					String[] locationStyleNames = null;
					String elementCodes = nameStyleTable.getString("ELEMNT_CODES");
					String[] locationStyleCodes = elementCodes.split("\\|");
					locationNameStyle = elementCodes.split("\\|");
					if (nameStyleTable.getBoolean("IS_TMG")) {
						String elementNames = nameStyleTable.getString("ELEMNT_NAMES");
						locationStyleNames = elementNames.split("\\|");
					} else {
						initiateElementCodeMap(pointOpenProject, elementCodeMap);
					}

				// Set up table control array
					int tableSize = locationStyleCodes.length + 2;
					tableControl = new Object[tableSize ][2];
					tableControl[0][0] = " ID";
					tableControl[0][1] = false;
					tableControl[1][0] = " Location";
					tableControl[1][1] = false;
					for (int i = 2; i < tableControl.length; i++) {
						if (nameStyleTable.getBoolean("IS_TMG")) {
							tableControl[i][0] = " " + locationStyleNames[i-2];
						} else {
							tableControl[i][0] = " " + elementCodeMap.get(locationStyleCodes[i-2]);
						}
						tableControl[i][1] = true;
					}
				}
			}
			return tableControl;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException("HBWhereWhenHandler - tableControlInitLocation: " + sqle.getMessage());
		}
	}

/**
 * void initiateElementCodeMap(HBProjectOpenData pointOpenProject, HashMap<String,String> elementCodeMap)
 * @param pointOpenProject
 * @param elementCodeMap
 * @throws HBException
 */
	private void initiateElementCodeMap(HBProjectOpenData pointOpenProject, HashMap<String,String> elementCodeMap) throws HBException {
		String dataLanguage = HGlobal.dataLanguage; //
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		try {
		// Collect all location element descriptions
			ResultSet nameStyleDescriptions = pointLibraryResultSet.getNameStyleElements(nameElementsDefined, "P", dataLanguage, dataBaseIndex);
			nameStyleDescriptions.first();

			String elementHREcodes = nameStyleDescriptions.getString("ELEMNT_CODES");
			String elementHREdescriptions = nameStyleDescriptions.getString("ELEMNT_NAMES");
			String [] codes = elementHREcodes.split("\\|");
			String [] names = elementHREdescriptions.split("\\|");
			for (int i = 0; i < codes.length; i++) {
				if (codes[i].length() == 3) {
					codes[i] = "0" + codes[i];
				}
				elementCodeMap.put(codes[i], names[i]);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException("HBWhereWhenHandler - initiateElementCodeMap: " + sqle.getMessage());
		}
	}

/**
 * convertHRELocationData(int nrOfColumns, HBProjectOpenData openProject)
 * @param nrOfColumns
 * @param pointOpenProject
 * @return
 */
	public Object[][] convertHRLocationData(int nrOfColumns, Object [][] tableControl,
											HBProjectOpenData pointOpenProject) throws HBException {
		int nrOfRows = 0;

	// If first call of location select, reload data
		if (pointOpenProject.locationSelectData == null) {
			pointOpenProject.reloadLocationSelectData = true;
		}
		try {
		// If data exist copy data from project open
			if (pointOpenProject.reloadLocationSelectData || locationStyleChanged) {

				nrOfRows = generateHRElocationTable(pointOpenProject, nrOfColumns, tableControl);
				if (HGlobal.DEBUG)
					System.out.println(" HBWhereWhenHandler - HRElocation22c location table initiated ");

				pointOpenProject.locationSelectData = locationData;
				pointOpenProject.reloadLocationSelectData = false;

			// Reset location style data reset
				locationStyleChanged = false;

			// Test list of eventID's
				if (HGlobal.DEBUG) {
					writeDataTable(nrOfRows, nrOfColumns, locationData);
					dumpLocationEvents();
				}
			} else {
				progBar.setValue(100);
			}
			return pointOpenProject.locationSelectData;
			//return locationData;
		} catch (HBException hbe) {
			if (HGlobal.DEBUG) {
				System.out.println("convertHREdata - SQL Exception: " + hbe.getMessage());
			}
			if (HGlobal.DEBUG) {
				hbe.printStackTrace();
			}
			return null;
		} catch (Exception exc)	{
			if (HGlobal.DEBUG) {
				System.out.println("HBWhereWhenHandler - unecpected Exception!\n Message: "
						+ exc.getMessage());
			}
			if (HGlobal.DEBUG) {
				exc.printStackTrace();
			}
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Error: HBWhereWhenHandler - Unexpected Exception:  " + exc.getMessage());
				HB0711Logging.printStackTraceToFile(exc);
			}
			JOptionPane.showMessageDialog(null,	"HBWhereWhenHandler - unecpected Exception!\n Set DEBUG ON and"
											+ "\n See stack trace for more info!"
											+ "\nException message: " + exc.getMessage(),"TMG to HRE",JOptionPane.INFORMATION_MESSAGE);
			return null;
		}
	}

/**
 * void generateHRElocationTable for DDL21c
 * @param pointOpenProject
 * @param nrOfColumns
 * @param tableControl
 * @throws HBException
 */
	public int generateHRElocationTable(HBProjectOpenData pointOpenProject,
										 int nrOfColumns,
										 Object [][] tableControl)
										 throws HBException {

		String[] placeNamesElements = null;

		int countRow = 0;
		int nrOfRows;
		int maxNumberOfRows;
		locationDataList = new ArrayList<>();
		String locationNameSelect = setSelectSQL("*",locationNameTable,"");
		String locationSelect = setSelectSQL("*", locationTable,"");

		try {
			int openDatabaseIndex = pointOpenProject.getOpenDatabaseIndex();

	 // Request location name table data
			locationNamesT552data = requestTableData(locationNameSelect, openDatabaseIndex);
			locationsT551data = requestTableData(locationSelect, openDatabaseIndex);

			maxNumberOfRows = (int) numberOfTableRows(locationNameTable, openDatabaseIndex);

			if (HGlobal.DEBUG) {
				System.out.println(" Number of rows in location T551 table: " + maxNumberOfRows);
			}
			maxNumberOfRows = ((int) numberOfTableRows(locationNameTable, openDatabaseIndex)) * 2;

			locationNamesT552data.beforeFirst();
			locationsT551data.beforeFirst();
		   	while (locationNamesT552data.next()) {
		   		locationsT551data.next();
		   		long locationBestImage = locationsT551data.getLong(bestImage);
		   		int visibleId = locationsT551data.getInt("VISIBLE_ID");
				long locationTablePID = locationsT551data.getLong("PID");

		// set progress bar in Location Select
				progBar.setValue(calcPercent(countRow, maxNumberOfRows));

		// Count rows for percentage status bar
			   	countRow++;

    			if (locationTablePID != null_RPID) {
    				HashMap<String,String> locationElementCodemap = pointLibraryResultSet.
    								selectLocationNameElements(locationTablePID, openDatabaseIndex);
    				if (locationElementCodemap.size() == 0) {
						placeNamesElements = null;
					} else {
    					placeNamesElements = new String[locationNameStyle.length];
    	    			for (int i = 0; i < locationNameStyle.length; i++)  {
    	    				if (locationElementCodemap.containsKey(locationNameStyle[i])) {
								placeNamesElements[i] = locationElementCodemap.get(locationNameStyle[i]);
							} else {
								placeNamesElements[i] = "";
							}
        				}
    				}

	    		} else {
					System.out.println("HBWhereWhenHandler - generateHRElocationTable - locationTablePID == null: "
	    					+ locationTablePID);
				}

    			if (placeNamesElements != null) {
    				LocationTableData pointLocationData = new LocationTableData(locationTablePID, visibleId,
    																	 placeNamesElements,
    																	 locationBestImage);
    				locationDataList.add(pointLocationData);
    			}
		   	}

		// Set up location data table
		   	nrOfRows = locationDataList.size();
		   	if (nrOfRows <= 0) {
		   		locationData = null;
		   		return 0;
		   	}
			locationData = new Object[nrOfRows][nrOfColumns];
			if (HGlobal.DEBUG ) {
				System.out.println(" Location Data row/col: " + nrOfRows + "/" + nrOfColumns);
			}

		   	for (int i = 0; i < nrOfRows; i++) {

		// set progress bar in Location Select
				progBar.setValue(calcPercent(countRow, maxNumberOfRows));

		// Count rows for percentage status bar
			   	countRow++;

		// Get place name elements
		   		char imageMark = ' ';
			   	String[] eventPlace = locationDataList.get(i).getPlaceData();
		   		long bestImageRPID = locationDataList.get(i).getBestImagePID();
		   		int visibleId = locationDataList.get(i).getVisibleId();
		   		if (bestImageRPID != null_RPID) {
					imageMark = '*';
				}

		// Create row in location Table
				generateLocationDataRow(i, visibleId, eventPlace, imageMark, tableControl);
		   	}

		//  Set final value for progress bar
		   	progBar.setValue(100);

		   	return nrOfRows;

		} catch (HBException | SQLException hbe) {
			if (HGlobal.DEBUG) {
				System.out.println("convertHREdata - SQL Exception: " + hbe.getMessage());
				hbe.printStackTrace();
			}
			throw new HBException("WhereWhenHandler - generateHRElocationTable error!\n" + hbe.getMessage());
		}
	}

/**
 * Set Hash code for place data
 * @param eventPlace
 * @return
 */
	private int setPlaceHashCode(String[] eventPlace) {
		String place = "";
		for (String element : eventPlace) {
			place = place + element.trim();
		}
		return  place.hashCode();
	}

/**
 * Get eventlist for place
 * @param eventPlace
 * @return
*/
	public LocationEventList getEventList(String[] eventPlace) {
		int hashCode = setPlaceHashCode(eventPlace);
		return hashCodeMap20.get(hashCode);
	}

/**
 * Demo of identify event list for place
 * @param placeIndex
 */
	public void showEventList(int placeIndex) {
		String[] eventPlace = locationEventList.get(placeIndex-1).getPlaceData();
		LocationEventList locationDataPoint = getEventList(eventPlace);
		if (HGlobal.DEBUG) {
			System.out.print("Location: " + placeIndex + " - ");
		}
		dumpPlaceEvents(locationDataPoint);
		if (HGlobal.DEBUG) {
			System.out.println();
		}
	}

/**
 * Iterator<EventData> listOfEvents(long PID)
 * @param PID
 * @return
 */
	public Iterator<EventData> listOfEvents(long PID) {
		int index = (int) (PID - proOffset);
		return locationEventList.get(index).getEventListID();
	}

/**
 * setLocationData(int row, String[] eventPlace, Object [][] tableControl)
 * @param row
 * @param eventPlace
 * @param tableControl
 */
	public void generateLocationDataRow(int row, int visibleId, String[] eventPlace, char imageMark, Object [][] tableControl) {
		int countNameElements = 0;
		locationData[row][0] = visibleId;
		locationData[row][1] = " " + imageMark;
		for (int i = 0; i < eventPlace.length; i++) {
			if (eventPlace[i].trim().length() > 0) {
				if ((countNameElements >= 3) && (((String) locationData[row][1]).length() >= 4)) {
					locationData[row][1] = locationData[row][1] + eventPlace[i] ;
					break;
				}
				locationData[row][1] = locationData[row][1] + eventPlace[i] + ", ";
				if (eventPlace[i].equals("----")) {
					locationData[row][1] = " No location name";
					eventPlace[i] = "";
				}
				countNameElements++;
			}
		}
   		int index = 2;
   		for (int i = 2; i < tableControl.length; i++) {
	   		if ((boolean) tableControl[i][1]) {
	   			locationData[row][index] = " " + eventPlace[i-2];
	   			index++;
	   		}
   		}
	}

/**
 * writeDataTable(Object[][] locationData)
 */
	private void writeDataTable(int nrOfRows, int nrOfColumns, Object[][] locationData) {
		System.out.println(" *** writeDataTable() ***");
		if (locationData != null) {
			for (int i = 0; i < nrOfRows; i++) {
				System.out.print("" + i + " -");
				for (int j = 0; j < nrOfColumns; j++) {
					System.out.print(" / " + locationData[i][j]);
				}
				System.out.println();
			 }
			System.out.println(" *** " + nrOfRows + " rows written ***");
		} else {
			System.out.println("convertHREdata SQL Exception - no data in location table!! " );
		}
	}

/**
 * private void dumpLocationEvents()
 */
	private void dumpLocationEvents() {
		int row = 1;
		if (locationEventList != null) {
		for (LocationEventList locationDataPoint : locationEventList) {
				System.out.print(" Location: " + row + " - ");
				dumpPlaceEvents(locationDataPoint);
				System.out.println();
				row++;
			}
		} else {
			System.out.println("dumpLocationEvents, locationEventList == null");
		}
	}

/**
 * Dump event data for location
 * @param LocationEventList)locationDataPoint
 */
	public void dumpPlaceEvents(LocationEventList locationDataPoint) {
		String[] locationData = locationDataPoint.getPlaceData();
		Iterator<EventData> itrEventID = locationDataPoint.getEventListID();
		for (String element : locationData) {
			if (element.length() > 0) {
				System.out.print(element + " / ");
			}
		}
		System.out.println();
		while(itrEventID.hasNext()) {
			EventData eventData = itrEventID.next();
			System.out.println("             " + eventData.getEventID() + "-" + eventData.getEventTyoe() + "-"
						+ eventData.getEventName());
		}
	}

/**
 * activateUpdateEvent(HBProjectOpenData pointOpenProject, int tableRow, boolean updateEvent)
 * @param pointOpenProject
 * @param tableRow
 * @param updateEvent
 * @return
 */
	public HG0547EditEvent activateUpdateEvent(HBProjectOpenData pointOpenProject, int tableRow, boolean updateEvent) {
		ResultSet selectedEventTable, partnerTableRS;
		long priPartnerPID, secPartnerPID, primAssocPID = null_RPID, locationTablePID, locationNamePID, eventTablePID, hdateDate, hdateSort;
		int priRoleNum, secRoleNum;
		String selectString, personPartners = " - ", primAssocName;
		int eventGroup, eventNumber = 0, roleNumber = 0;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectedEventTableRow = tableRow;
		pointPersonMannager = pointOpenProject.getPersonHandler();

	// Mod 2.12.2024
		if (updateEvent)
			eventTablePID = pointPersonMannager.getEventPID(tableRow);
		else
			eventTablePID = pointEditEventRecord.eventTablePID;

		dateFormatSelect();
		try {
			personNameStyle =  getNameStyleOutputCodes(nameStylesOutput, "N", dataBaseIndex);
			selectString = setSelectSQL("*", eventTable, "PID = " + eventTablePID);
			selectedEventTable = requestTableData(selectString, dataBaseIndex);
		// Collect the event record
			selectedEventTable.first();
			eventNumber = selectedEventTable.getInt("EVNT_TYPE");
			eventGroup = pointLibraryResultSet.getEventGroup(eventNumber, dataBaseIndex);

			if (eventGroup == marrGroup || eventGroup == divorceGroup) {
				selectString = setSelectSQL("*", personPartnerTable, "EVNT_RPID = " + eventTablePID);
				partnerTableRS = requestTableData(selectString, dataBaseIndex);
				partnerTableRS.first();
				priPartnerPID = partnerTableRS.getLong("PRI_PARTNER_RPID");
				secPartnerPID = partnerTableRS.getLong("SEC_PARTNER_RPID");
				priRoleNum = partnerTableRS.getInt("PRI_ROLE");
				secRoleNum = partnerTableRS.getInt("SEC_ROLE");
				eventNumber = partnerTableRS.getInt("PARTNER_TYPE");
				partnerTableRS.close();
			// Build list of both names, both roles separated by / so HG0547EditEvent can split them
				personPartners = pointLibraryResultSet.exstractPersonName(priPartnerPID, personNameStyle, dataBaseIndex)
						+ "/" + pointLibraryResultSet.exstractPersonName(secPartnerPID, personNameStyle, dataBaseIndex)
						+ "/" + getEventRoleName(eventNumber, priRoleNum)
						+ "/" + getEventRoleName(eventNumber, secRoleNum);
			} else {
				eventNumber = selectedEventTable.getInt("EVNT_TYPE");
				roleNumber = selectedEventTable.getInt("PRIM_ASSOC_ROLE_NUM");
				primAssocPID = selectedEventTable.getLong("PRIM_ASSOC_RPID");
			}
			hdateDate = selectedEventTable.getLong("START_HDATE_RPID");
			hdateSort = selectedEventTable.getLong("SORT_HDATE_RPID");

		// Location processing moddified 17.11.2024
			locationTablePID = selectedEventTable.getLong("EVNT_LOCN_RPID");
			pointManageLocationData = new ManageLocationNameData(pointDBlayer, dataBaseIndex, pointOpenProject);

		// NOTE null_RPID if no location name data
			locationNamePID = pointManageLocationData.findLocationNameTablePID(locationTablePID);
			int error = pointManageLocationData.updateManageLocationNameTable(locationNamePID);

		// Temp error report
			if (error > 0)
				System.out.println(" HBWhereWhenHandler - activateUpdateEvent error: " + error );

			pointCreateEventRecord = new CreateEventRecord(pointDBlayer, dataBaseIndex, pointOpenProject);
			pointEditEventRecord = new EditEventRecord(pointDBlayer, dataBaseIndex, pointOpenProject, eventTablePID, primAssocPID);
			pointEditEventRecord.setDatesForEdit(hdateDate, hdateSort);
			pointEditEventRecord.setPartnerNames(personPartners);
		// Check if date and sort exist
			if (hdateDate == null_RPID || hdateSort == null_RPID) {
				addHdate = true;
			} else {
				addHdate = false;
			}

	// Start update event
			pointEditEvent = new HG0547UpdateEvent(pointOpenProject, eventNumber, roleNumber,
					eventTablePID, addHdate, locationNamePID);

	// If witnessed event set primary assoc name for an Update event
			if (primAssocPID != null_RPID) {
				primAssocName = pointLibraryResultSet.exstractPersonName(primAssocPID, personNameStyle, dataBaseIndex);
				((HG0547UpdateEvent) pointEditEvent).setUpdateEventOwnerData(primAssocName);
			}

			return pointEditEvent;
		} catch (HBException | SQLException hbe) {
			System.out.println(" HBWhereWhenHandler - activateEditEvent error: " + hbe.getMessage());
			hbe.printStackTrace();
		}
		return null;
	}

/**
 * activateEditEvent(HBProjectOpenData pointOpenProject, String eventName,
 * 						String roleName, String eventPerson)
 * @param pointOpenProject
 * @param eventName
 * @param roleName
 * @param eventPerson
 * @return
 */
	public HG0547EditEvent activateEditEvent(HBProjectOpenData pointOpenProject,
											int eventNumber, int roleNumber, long eventPersonPID, int tableRow) {
		long eventPID;
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		dateFormatSelect();
		try {
			pointPersonMannager = pointOpenProject.getPersonHandler();
			eventPID = pointPersonMannager.getEventPID(tableRow);
			pointManageLocationData = new ManageLocationNameData(pointDBlayer, dataBaseIndex, pointOpenProject);
			pointEditEventRecord = new EditEventRecord(pointDBlayer, dataBaseIndex, pointOpenProject, eventPID);
			pointEditEvent = new HG0547EditEvent(pointOpenProject, eventNumber, roleNumber, eventPID);
			return pointEditEvent;
		} catch (HBException hbe) {
			System.out.println("HBWhereWhenHandler - activateEditEvent error: " + hbe.getMessage());
			hbe.printStackTrace();
		}
		return null;
	}

/**
 * public HG0547EditEvent activateAddEvent(HBProjectOpenData pointOpenProject,
											int eventNumber, int roleNumber, long eventPersonPID)
 * @param pointOpenProject
 * @param eventNumber
 * @param roleNumber
 * @param eventPersonPID
 * @return
 */
	public HG0547EditEvent activateAddEvent(HBProjectOpenData pointOpenProject,
											int eventNumber, int roleNumber, long eventPersonPID) {
		long eventPID = 0;
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		dateFormatSelect();
		try {
			pointPersonMannager = pointOpenProject.getPersonHandler();
			//eventPID = pointPersonMannager.getEventPID(tableRow);
			pointManageLocationData = new ManageLocationNameData(pointDBlayer, dataBaseIndex, pointOpenProject);
			pointEditEventRecord = new EditEventRecord(pointDBlayer, dataBaseIndex, pointOpenProject, eventPID);
			pointEditEvent = new HG0547AddEvent(pointOpenProject, eventNumber, roleNumber);
			return pointEditEvent;
		} catch (HBException hbe) {
			System.out.println("HBWhereWhenHandler - activateEditEvent error: " + hbe.getMessage());
			hbe.printStackTrace();
		}
		return null;
	}

/**
 * HG0552ManageEvent activateAddSelectedEvent(HBProjectOpenData pointOpenProject)
 * @param pointOpenProject
 * @return
*/
	public HG0552ManageEvent activateAddSelectedEvent(HBProjectOpenData pointOpenProject, int selectedPartnerTableRow) {
		String namePerson = " No person";
		long eventPID = null_RPID;
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		try {
			pointManageLocationData = new ManageLocationNameData(pointDBlayer, dataBaseIndex, pointOpenProject);
			pointEditEventRecord = new EditEventRecord(pointDBlayer, dataBaseIndex, pointOpenProject, eventPID);
			pointCreateEventRecord = new CreateEventRecord(pointDBlayer, dataBaseIndex, pointOpenProject);
			pointManageEvent = new HG0552ManageEvent(pointOpenProject, namePerson);
			pointManageEvent.setPartnerTableSelectedRow(selectedPartnerTableRow);
			return pointManageEvent;
		} catch (HBException hbe) {
			System.out.println("HBWhereWhenHandler - activateAddSelectedEvent error: " + hbe.getMessage());
			hbe.printStackTrace();
		}
		return null;
	}

/**
 * HG0547EditEvent activateAddFixedEvent
 * @param pointOpenProject
 * @param eventNumber
 * @param roleNumber
 * @param eventPersonPID
 * @return
 */
	public HG0547EditEvent activateAddFixedEvent(HBProjectOpenData pointOpenProject,
								int eventNumber, int roleNumber, long eventPersonPID) {

		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		pointPersonMannager = pointOpenProject.getPersonHandler();
		long eventPID = null_RPID;
		try {
			pointManageLocationData = new ManageLocationNameData(pointDBlayer, dataBaseIndex, pointOpenProject);
			pointEditEventRecord = new EditEventRecord(pointDBlayer, dataBaseIndex, pointOpenProject, eventPID);
			pointCreateEventRecord = new CreateEventRecord(pointDBlayer, dataBaseIndex, pointOpenProject);
			pointEditEvent = new HG0547AddEvent(pointOpenProject, eventNumber, roleNumber);
			return pointEditEvent;
		} catch (HBException hbe) {
			System.out.println(" HBWhereWhenHandler - activateAddDefinedEvent error: " + hbe.getMessage());
			hbe.printStackTrace();
		}
		return null;
	}

/**
 * HG0547EditEvent activateAddPartnerEvent
 * @param pointOpenProject
 * @param eventName
 * @param roleName
 * @param eventPerson
 * @return
 */
	public HG0547EditEvent activateAddPartnerEvent(HBProjectOpenData pointOpenProject,
								int eventNumber, int roleNumber, long partnerTablePID, int partnerTableRow) {
		ResultSet partnerTableRS;
		long priPartnerPID, secPartnerPID;
		int eventType = eventNumber;
		String partnerNames;
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		pointPersonMannager = pointOpenProject.getPersonHandler();
		String selectString = setSelectSQL("*", personPartnerTable, "PID = " + partnerTablePID);
		try {
			partnerTableRS = requestTableData(selectString, dataBaseIndex);
			partnerTableRS.first();
			if (partnerTableRS.getLong("EVNT_RPID") != null_RPID) {
				JOptionPane.showMessageDialog(null,	" Partner event already exist!","Add partner event",
						JOptionPane.INFORMATION_MESSAGE);
				return null;
			}
			priPartnerPID = partnerTableRS.getLong("PRI_PARTNER_RPID");
			secPartnerPID = partnerTableRS.getLong("SEC_PARTNER_RPID");
			eventType = partnerTableRS.getInt("PARTNER_TYPE");
			partnerTableRS.close();
			personNameStyle =  getNameStyleOutputCodes(nameStylesOutput, "N", dataBaseIndex);
			partnerNames = pointLibraryResultSet.exstractPersonName(priPartnerPID, personNameStyle, dataBaseIndex)
					+ " & " + pointLibraryResultSet.exstractPersonName(secPartnerPID, personNameStyle, dataBaseIndex);

			pointManageLocationData = new ManageLocationNameData(pointDBlayer, dataBaseIndex, pointOpenProject);
			pointEditEventRecord = new EditEventRecord(pointDBlayer, dataBaseIndex, pointOpenProject, null_RPID);
			pointEditEventRecord.setPartnerNames(partnerNames);
			pointEditEvent = new HG0547PartnerEvent(pointOpenProject, partnerTableRow, eventType,
													roleNumber, partnerTablePID);
			return pointEditEvent;
		} catch (HBException | SQLException hbe) {
			System.out.println(" HBWhereWhenHandler - activateAddPartnerEvent: " + hbe.getMessage());
			hbe.printStackTrace();
		}
		return null;
	}

/**
 * public void deleteSingleEvent(long eventTablePID)
 * @param assocPersonRPID
 * @throws HBException
 */
	public void deleteSingleEvent(long eventTablePID) throws HBException {
		// Delete event with dates
		ResultSet eventTableRS;
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		String selectString = setSelectSQL("*", eventTable, "PID = " + eventTablePID);
	// Start transaction
		updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
		try {
			eventTableRS = requestTableData(selectString, dataBaseIndex);
			if (isResultSetEmpty(eventTableRS)) {
				return;
			}
			eventTableRS.beforeFirst();
			while (eventTableRS.next() ) {
				long ownerPersonPID = eventTableRS.getLong("PRIM_ASSOC_RPID");
				int eventType = eventTableRS.getInt("EVNT_TYPE");
				if (eventType == birthEventType) {
					removePersonData(ownerPersonPID, true, false);
				}
				if (eventType == deathEventType) {
					removePersonData(ownerPersonPID, false, true);
				}
				eventTableRS.deleteRow();
				if (HGlobal.DEBUG) {
					System.out.println(" HBWhereWhenHandler - deleted event PID: " + eventTablePID);
				}
			}
			eventTableRS.close();

		// Delete the assoc persons for this event
			deleteAssocEvents(eventTablePID);

		// Delete the citations for this event
			deleteCitationEvents(eventTablePID, "T450");

		// End transaction
			updateTableData("COMMIT", dataBaseIndex);

		// Reload Result Set for person and names
			pointOpenProject.reloadT401Persons();
			pointOpenProject.reloadT402Names();

		// Reset Person Select
			pointOpenProject.getPersonHandler().resetPersonSelect();

		} catch (SQLException | HBException sqle) {
			// Role back transaction
			updateTableData("ROLLBACK", dataBaseIndex);
			throw new HBException(" HBWhereWhenHandler - deleteSingleEvent: " + sqle.getMessage());
		}
	}

/**
 * 	public void deleteAssociateCitation(long eventTablePID,
							Vector<Long> associatesAddedList,
							Vector<Long> citationAddedList) throws HBException
 * @param eventTablePID
 * @param associatesAddedList
 * @param citationAddedList
 * @throws HBException
 */
	public void deleteAssociateCitation(long eventTablePID,
							Vector<Long> associatesAddedList,
							Vector<Long> citationAddedList) throws HBException {
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		// Start transaction
		updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
		try {
		// Delete the assoc persons for this event
			if (associatesAddedList.size() > 0) deleteAddedAssocs(eventTablePID, associatesAddedList);

		// Delete the citations for this event
			if (citationAddedList.size() > 0) deleteAddedCitations(eventTablePID, "T450", citationAddedList);

		// End transaction
			updateTableData("COMMIT", dataBaseIndex);

		// Reload Result Set for person and names
			pointOpenProject.reloadT401Persons();
			pointOpenProject.reloadT402Names();

		// Reset Person Select
			pointOpenProject.getPersonHandler().resetPersonSelect();

		} catch (HBException sqle) {
			// Role back transaction
			updateTableData("ROLLBACK", dataBaseIndex);
			throw new HBException(" HBWhereWhenHandler - deleteAssocCitation: " + sqle.getMessage());
		}
	}

	private void deleteAddedAssocs(long eventTablePID, Vector<Long> associatesAddedList) throws HBException {
		ResultSet assocTableRS;
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		String selectString = setSelectSQL("*", eventAssocTable, "EVNT_RPID = " + eventTablePID);
		try {
			assocTableRS = requestTableData(selectString, dataBaseIndex);
			if (isResultSetEmpty(assocTableRS)) {
				return;
			}
			assocTableRS.beforeFirst();
			while (assocTableRS.next() ) {
				for (int j = 0; j < associatesAddedList.size(); j++ ) {
					long addedAssociatePID = associatesAddedList.get(j);
					if (assocTableRS.getLong("PID") == associatesAddedList.get(j)) {
						if (HGlobal.DEBUG)
							System.out.print("Associate PID:  " + addedAssociatePID);
						assocTableRS.deleteRow();
					}
				}
			}
			assocTableRS.close();
		} catch (SQLException sqle) {
			throw new HBException(" HBWhereWhenHandler - deleteAddedAssocs : " + sqle.getMessage());
		}
	}

	private void deleteAddedCitations(long eventTablePID, String tableName, Vector<Long> citationsAddedList) throws HBException {
		ResultSet citationTableRS;
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		String selectString = setSelectSQL("*", citationTable, "CITED_RPID = " + eventTablePID
				+ " AND OWNER_TYPE = '" + tableName + "'");
		try {
			citationTableRS = requestTableData(selectString, dataBaseIndex);
			if (isResultSetEmpty(citationTableRS)) {
				return;
			}
			citationTableRS.beforeFirst();
			while (citationTableRS.next() ) {
				for (int j = 0; j < citationsAddedList.size(); j++ ) {
					long addedCitationPID = citationsAddedList.get(j);
					if (citationTableRS.getLong("PID") == citationsAddedList.get(j)) {
						if (HGlobal.DEBUG)
							System.out.print("Associate PID:  " + addedCitationPID);
						citationTableRS.deleteRow();
					}
				}
			}
			citationTableRS.close();
		} catch (SQLException sqle) {
			throw new HBException(" HBWhereWhenHandler - deleteAddedCitations : " + sqle.getMessage());
		}
	}

/**
 * removePersonData(long personPID, boolean deleteBirth, boolean deleteDeath)
 * @param personPID
 * @param deleteBirth
 * @param deleteDeath
 * @throws HBException
 * @throws SQLException
 */
	public void removePersonData(long personPID, boolean deleteBirth, boolean deleteDeath)
									throws SQLException, HBException {
		String selectString;
		ResultSet personTableRS;
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", personTable, "PID = " + personPID);
		personTableRS = requestTableData(selectString, dataBaseIndex);
		if (isResultSetEmpty(personTableRS)) {
			return;
		}
	// Continue remove data
		personTableRS.first();
		if (deleteBirth) {
			deleteHdate(personTableRS.getLong(personHDateBirthField));
			personTableRS.updateLong(personHDateBirthField, null_RPID);
			personTableRS.updateLong(personLocnBirthField, null_RPID);
		}
		if (deleteDeath) {
			deleteHdate(personTableRS.getLong(personHdateDeathField));
			personTableRS.updateLong(personHdateDeathField, null_RPID);
			personTableRS.updateLong(personLocnDeathField, null_RPID);
		}
		personTableRS.updateRow();
		personTableRS.close();
	}

/**
 * deleteHdate(long hdatePID)
 * @param hdatePID
 * @throws HBException
 */
	public void deleteHdate(long hdatePID) throws HBException {
		String selectString;
		ResultSet hdateTableRS;
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", dateTable, "PID = " + hdatePID);
		hdateTableRS = requestTableData(selectString, dataBaseIndex);
		try {
			if (isResultSetEmpty(hdateTableRS)) {
				return;
			}
		// Continue delete Hdate
			hdateTableRS.first();
			hdateTableRS.deleteRow();
			hdateTableRS.close();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" HBWhereWhenHandler - deleteHdate: " + sqle.getMessage());
		}
	}

/**
 * activateManageLocation called from main
 * @param pointOpenProject
 * @return int errorCode
 */
	public int activateManageLocation(HBProjectOpenData pointOpenProject) {
		if (HGlobal.DEBUG) {
			System.out.println(" Main menu initiate - activateManageLocation");
		}
		int errorCode = 0;
		try {
			String windowId = "50800";
			long locationTablePID = pointOpenProject.pointGuiData.getTableViewPointPID(windowId);
			if (locationTablePID != null_RPID) {
				errorCode = initiateManageLocation(pointOpenProject, locationTablePID, windowId);
				if (HGlobal.DEBUG)
				 {
					System.out.println(" Main activateManageLocation: "  //$NON-NLS-1$
							+ pointOpenProject.getProjectName()
								+ " ID: " + windowId);	 //$NON-NLS-1$
				}
				return errorCode;
			}
			// Find the PID for first location in table
					long firstLocationPID = firstRowPID(locationTable, pointOpenProject);
					int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();

			// Test if first location PID have location data

					if (locationNameStyle == null)
					 {
						tableControlInitLocation(pointOpenProject); // Initiate locationNameStyle
					}

					String [] locationNameElements = pointLibraryResultSet.
							selectLocationNameElements(firstLocationPID, locationNameStyle, dataBaseIndex);

					if (locationNameElements == null) {
						firstLocationPID = firstLocationPID + 1;
					}

					if (HGlobal.DEBUG) {
						System.out.println(" Main activateManageLocation - next location PID: " + firstLocationPID + " have data!");
					}

					errorCode = initiateManageLocation(pointOpenProject, firstLocationPID, windowId);

			// Set table PID for first location
					pointOpenProject.pointGuiData.setTableViewPointPID(windowId,firstLocationPID);

		} catch (HBException hbe) {
			if (HGlobal.DEBUG)
			 {
				System.out.println("Main Manage Location error:\n" + hbe.getMessage()); //$NON-NLS-1$
			}
		// Temp error message waiting for NLS implementation
			JOptionPane.showMessageDialog(null,	"Main Manage Location error:\n"
					+ hbe.getMessage(),"Manage Location",JOptionPane.INFORMATION_MESSAGE);
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Error: Manage Location error: " + hbe.getMessage());
				HB0711Logging.printStackTraceToFile(hbe);
			}
			return 1;
		}
		return 0;
	}

/**
 * initiateManageLocation(HBProjectOpenData pointOpenProject,long locationTablePID ,String screenId)
 * @param pointOpenProject
 * @param locationTablePID
 * @param screenId
 * @return int errorCode
 */
	public int initiateManageLocation(HBProjectOpenData pointOpenProject
							,long locationTablePID
							,String screenID) {

		//String screenID = HG0508ManageLocation.screenID;
		int errorCode;
		HG0508ManageLocation manageLocationScreen;
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();

		if (HGlobal.DEBUG)
			System.out.println(" InitiateManageLocation - Location table PID: " + locationTablePID);

	// find pointer to Location Manager for each project
		manageLocationScreen = pointOpenProject.getManageLocationPointer();

	// Close and Dispose HG0508ManageLocation window if already created
		if (manageLocationScreen != null) {
			pointOpenProject.closeStatusScreen(screenID);

	    // Set frame size in GUI data
			Dimension frameSize = manageLocationScreen.getSize();
			pointOpenProject.setSizeScreen(screenID,frameSize);

		// Set position	in GUI data
			Point position = manageLocationScreen.getLocation();
			pointOpenProject.setPositionScreen(screenID,position);

		// Set class name in GUI configuration data
			pointOpenProject.setClassName(screenID,"HG0508ManageLocation"); //$NON-NLS-1$

		// Mark the screen as closed in T302 and remove open screeen
			pointOpenProject.closeStatusScreen(screenID);

		// Close screen
			manageLocationScreen.dispose();

		} else if (HGlobal.DEBUG) {
			System.out.println( "Location Manager exists - PID: " + locationTablePID);
		}

		try {
	    // Set up date translation
			dateFormatSelect();
			setUpDateTranslation();

		// Load data from database(dataBaseIndex) for location data
			pointManageLocationData = new ManageLocationNameData(pointDBlayer, dataBaseIndex, pointOpenProject);
			pointManageLocationData.setNameStyleIndex(getDefaultIndex());

		// Mod 17.11.2024
			long locationNamePID = pointManageLocationData.findLocationNameTablePID(locationTablePID);
			//errorCode = pointManageLocationData.updateManageLocationNameTable(locationTablePID);
			errorCode = pointManageLocationData.updateManageLocationNameTable(locationNamePID);
			errorCode = pointManageLocationData.initiateImageExhibits(locationTablePID);

		// Select date format
			dateFormatSelect();

			if (HGlobal.DEBUG) {
				System.out.println(" Dateformat: " + dateFormatIndex + " / " + HGlobal.dateFormat);
			}

			HG0401HREMain.mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			manageLocationScreen = new HG0508ManageLocation(this, pointOpenProject, screenID, locationTablePID);

		// store pointer to LocationManager for each project
			pointOpenProject.setManageLocationPointer(manageLocationScreen);

		// Set size for frame
			//Dimension frameSize = pointOpenProject.pointGuiData.getFrameSize(manageLocationScreen.screenID);
			Dimension frameSize = pointOpenProject.pointGuiData.getFrameSize(screenID);
			if (frameSize != null) {
				manageLocationScreen.setSize(frameSize);
			}

		// Set location for frame
			//Point location = pointOpenProject.pointGuiData.getFramePosition(manageLocationScreen.screenID);
			Point location = pointOpenProject.pointGuiData.getFramePosition(screenID);
			if (location == null) {
		// Sets screen top-left corner relative to parent window
				manageLocationScreen.setLocation(50,50);
				  if (HGlobal.DEBUG) {
					//System.out.println(" initiateManageLocation - initiate location: " + manageLocationScreen.screenID);
				  	  System.out.println(" initiateManageLocation - initiate location: " + screenID);
				}
			} else {
				 manageLocationScreen.setLocation(location);
				 if (HGlobal.DEBUG) {
					//System.out.println(" initiateManageLocation - reset location: " + manageLocationScreen.screenID);
				    System.out.println(" initiateManageLocation - reset location: " + screenID);
				}
			}

		// Set visible PID in GUI config
			pointOpenProject.pointGuiData.setTableViewPointPID(screenID, locationTablePID);

		  	manageLocationScreen.toFront();
		  	HG0401HREMain.mainFrame.setCursor(Cursor.getDefaultCursor());

	// Mark the window as open in the T302 table
		  	pointOpenProject.pointGuiData.setOpenStatus(screenID,
		  				pointOpenProject.pointT302_GUI_CONFIG,true);

	// Set class name in GUI config data
		  	//pointOpenProject.setClassName(manageLocationScreen.screenID, manageLocationScreen.getClassName());
			pointOpenProject.setClassName(screenID, manageLocationScreen.getClassName());

	// register the open screen on HBOpenprojectData
		  	pointOpenProject.registerOpenScreen(manageLocationScreen);
		  		if (HGlobal.DEBUG) {
					System.out.println(" initiateManageLocation: " + manageLocationScreen.getClassName() + " Errorcode: " + errorCode);
				}

		  	return errorCode;

		} catch (HBException hbe) {
			if (HGlobal.DEBUG) {
				System.out.println(" Initiate ManagePerson error: \n" + hbe.getMessage());
			}

		// Temp error message waiting for NLS implementation
			JOptionPane.showMessageDialog(null,	" Initiate ManagePerson error:\n"
					+ hbe.getMessage(),"Manage Person",JOptionPane.INFORMATION_MESSAGE);
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Error: Initiate ManagePerson error: " + hbe.getMessage());
				HB0711Logging.printStackTraceToFile(hbe);
			}
			return 2;
		}
	}

/**
 * activateLocationNameStyel(HBProjectOpenData pointOpenProject)
 * @param pointOpenProject
 * @return
 */
	public HG0526ManageLocationNameStyles  activateLocationNameStyle(HBProjectOpenData pointOpenProject) {
		HG0526ManageLocationNameStyles locnStyleScreen;
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		pointNameStyleManager =  new HBNameStyleManager(pointDBlayer, dataBaseIndex, "Location ");
		try {
			pointNameStyleManager.updateStyleTable("P", nameStyles, nameElementsDefined);
			locnStyleScreen = new HG0526ManageLocationNameStyles(pointOpenProject, pointNameStyleManager);
			return locnStyleScreen;
		} catch (HBException hbe) {
			System.out.println("HBWhereWhen - activateLocationNameStyle: " + hbe.getMessage());
			hbe.printStackTrace();
		}
		return null;
	}

/**
 * createEventManager(HBProjectOpenData pointOpenProject)
 * @param pointOpenProject
 * @return
 * @throws HBException
 */
	public HG0552ManageEvent createEventManager(HBProjectOpenData pointOpenProject) throws HBException {
		//String eventName = null, roleName = null, eventPerson = null;
		//HBEventRoleManager pointEventRoleManager = null;
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		CreateEventRecord  pointEventManager = new CreateEventRecord(pointDBlayer, dataBaseIndex,
																pointOpenProject);
		pointEventManager.editEventData();
		HG0552ManageEvent addEventScreen = new HG0552ManageEvent(pointOpenProject, "");
		return addEventScreen;
	}

/**
 * editEventManager(HBProjectOpenData pointOpenProject)
 * @param pointOpenProject
 * @return
 * @throws HBException
 */
	public HG0547EditEvent editEventManager(HBProjectOpenData pointOpenProject, int tableRow) throws HBException {
		int eventNumber = 1001, roleNumber = 1;
		pointPersonMannager = pointOpenProject.getPersonHandler();
		long eventPID = pointPersonMannager.getEventPID(tableRow);

		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		CreateEventRecord  pointEventManager = new CreateEventRecord(pointDBlayer, dataBaseIndex,
																pointOpenProject);
		pointEventManager.editEventData();
		HG0547EditEvent editEventScreen = new HG0547EditEvent(pointOpenProject, eventNumber, roleNumber,
												eventPID);
		return editEventScreen;
	}

/**
 * class LocationEventList only for DDLv20b and v21a
  * @author Nils Tolleshaug
  * @version v0.00.0025
  * @since 2020-11-01
  */
	 public class LocationEventList {

		ArrayList<EventData> eventDatalist;
		String[] placeData;
		long locationTablePID;

/**
 * Constructor GenealogyData
 */
		public LocationEventList(String[] placeData, long locationTablePID) {
			this.placeData = placeData;
			this.locationTablePID = locationTablePID;
			eventDatalist = new ArrayList<>() ;
		}

 /**
  * SET methods
  */
		public void addEventToList(EventData eventData) {
			eventDatalist.add(eventData);
		}

 /**
  * GET methods
  * @return
  */
		public Iterator<EventData> getEventListID() {
			return eventDatalist.iterator();
		}

		public int getEventRows() {
			return eventDatalist.size();
		}

	   	public String[] getPlaceData() {
			return placeData;
		}

	   	public long getLocationTablePID() {
	   		return locationTablePID;
	   	}

	} // End LocationEventList

/**
  * class EventData Only for DDLv20b and v21a
  * @author NTo
  * @version v0.00.0025
  * @since 2020-11-05
  */
	 class EventData {
		long eventPID;
		long primPersionPID;
		long secPersionPID;
		long eventDate;
		private String eventType;
		private String eventName;

/**
 * EventData(long eventPID, String[] placeData)
 * @param eventPID
 * @param PlaceData
 */
		public EventData(long eventPID) {
			this.eventPID = eventPID;
		}

 /**
  * GET methods
  * @return
  */
		public void setEventName(String evName) {
			eventName = evName;
	   	}

	   	public long getEventID() {
	   		return eventPID;
	   	}

	   	public String getEventTyoe() {
	   		return eventType;
	   	}

	   	public String getEventName() {
	   		return eventName;
	   	}

	   	public long getEventDate() {
	   		return eventDate;
	   	}

	   	public long getPrimPersonPID() {
	   		return primPersionPID;
	   	}

	   	public long getSecPersonPID() {
	   		return secPersionPID;
	   	}
	 } // End EventData

 /**
  * class EventData - Only for DDLv21c
  * @author NTo
  * @version v0.00.0027
  * @since 2022-01-05
  */
	class LocationTableData {
		long locationTablePID;
		int visibleId;
		long bestImageRPID;
		String[] nameElements;

		public LocationTableData(long locationTablePID, int visibleId, String[] nameElements, long bestImageRPID) {
			this.locationTablePID = locationTablePID;
			this.nameElements = nameElements;
			this.visibleId = visibleId;
			this.bestImageRPID = bestImageRPID;
		}

	   	public String[] getPlaceData() {
			return nameElements;
	   	}

	   	public long getLocationTablePID() {
	   		return locationTablePID;
	   	}

	   	public int getVisibleId() {
	   		return visibleId;
	   	}

	   	public long getBestImagePID() {
	   		return bestImageRPID;
	   	}
	}

} // End Class HBWhereWhenHandler


/**
 * class ManageLocationNameData
 * @author NTo
 * @version v0.01.0027
 * @since 2022-08-02
 */
class ManageLocationNameData extends HBBusinessLayer {
	int dataBaseIndex;

	long proOffset = 1000000000000000L;
	long null_RPID  = 1999999999999999L;
	protected HBMediaHandler pointMediaHandler = null;

	Object[][] nameElementTable;
	String[] locationHeaderData;
	long locationNamePID = null_RPID; // Stores the current location Name PID
	String[] locnHeaderData;

	boolean allElements = true;
	boolean hiddenElements = false;
	int numberHidden = 0;
	// Type of exhibit
	int locationImage = 2;

	int nameStyleIndex = 0;
	String selectedStyle = "";
	int defaultStyleIndex = 0;

	//String[] nameStyleAndDates = new String[3];
	String[] nameStyleAndDates = {"","",""};
	long[] nameStylePID;
	ResultSet locationNameResultSet;

	String[] nameStyleNames;
	String[] nameStyleText;
	String[] nameStyleCodeString;
	String[] nameStyleDescriptionString;
	boolean[] isDefaultNameStyle;
	boolean[] isTmgNameStyle;
	//-------------------------------
	String[] nameElementData;
	String[] nameElementDescription;

	String[] nameStyleElementCodes;
	String[][] personNameElementsData;

	ResultSet nameStyleTable;
	ResultSet nameElementRSet;
	HashMap<String,String> elementCodeMap = new HashMap<>();
	HashMap<String,String> tmgCodeMap = new HashMap<>();
	HashMap<String,String> locationNameChanges;

/**
 * GUI methods for interface to manager
 */
	public int getDefaultIndex() {
		return defaultStyleIndex;
	}

	public void setNameStyleIndex(int index) {
		nameStyleIndex = index;
	}

	public String[] getAvailableStyles() {
		return nameStyleNames;
	}

	public Object[][] getNameElementTable() {
		return nameElementTable;
	}

	public String[] getTableHeader() {
		return locationHeaderData;
	}

	public String[] getNameData()  {
		return nameStyleAndDates;
	}

/**
 * Constructor 	ManageLocationData
 * @param pointDBlayer
 * @param dataBaseIndex
 * @param hdataLabels
 * @throws HBException
 */
	public ManageLocationNameData(HDDatabaseLayer pointDBlayer, int dataBaseIndex,
							HBProjectOpenData pointOpenProject) throws HBException {
		super();
		this.pointDBlayer = pointDBlayer;
		this.dataBaseIndex = dataBaseIndex;
      	setUpDateTranslation();
      	dateFormatSelect();
		setNameStyleTable();
		getAllNameStyleElements(HGlobal.dataLanguage);
		locationHeaderData = setTranslatedData("50800","1", false);
		locationNameChanges = new HashMap<String,String>();
		pointMediaHandler = pointOpenProject.getMediaHandler();
	}

/**
 * initiateImageExhibits(long selectLocationPID)
 * @param selectLocationPID
 * @return
 */
	public int initiateImageExhibits(long selectLocationPID) {
		int errorCode = 0;
		try {
			errorCode = pointMediaHandler.getAllExhibitImage(selectLocationPID,
															locationImage, dataBaseIndex);
			if (errorCode > 1) {
				System.out.println(" HBWhereWhenHandler - initiateImageExhibits, Image error PID: " + selectLocationPID);
			}
			return errorCode;
		} catch (HBException hbe) {
			System.out.println(" HBWhereWhenHandler - exhibittable, Image error PID: "
						+ selectLocationPID + "" + hbe.getMessage());
			hbe.printStackTrace();
			return errorCode;
		}
	}

/**
 * setNameStyleTable()
 */
	public void setNameStyleTable()   {
		int index = 0;
		try {
			nameStyleTable = pointLibraryResultSet.getNameStylesTable(nameStyles, "P",dataBaseIndex);
			nameStyleTable.last();
			int rows = nameStyleTable.getRow();
			nameStylePID = new long[rows];
			isDefaultNameStyle = new boolean[rows];
			isTmgNameStyle = new boolean[rows];
			nameStyleNames = new String[rows];
			nameStyleText = new String[rows];
			nameStyleCodeString = new String[rows];
			nameStyleDescriptionString = new String[rows];

			nameStyleTable.beforeFirst();
			while (nameStyleTable.next()) {
				nameStylePID[index] = nameStyleTable.getLong("PID");
				isDefaultNameStyle[index] = nameStyleTable.getBoolean("IS_DEFAULT");
				isTmgNameStyle[index] = nameStyleTable.getBoolean("IS_TMG");
				nameStyleNames[index] = nameStyleTable.getString("NAME_STYLE");
				nameStyleText[index] = nameStyleTable.getString("NAME_STYLE_DESC");
				nameStyleCodeString[index] = nameStyleTable.getString("ELEMNT_CODES");
				nameStyleDescriptionString[index] = nameStyleTable.getString("ELEMNT_NAMES");

				if (isDefaultNameStyle[index]) {
					defaultStyleIndex = index;
					selectedStyle = nameStyleNames[index];
				}

				if (HGlobal.DEBUG) {
					System.out.println(" Style descriptions " +  nameStyleNames[index]
							+ " = " + nameStyleDescriptionString[index]);
				}
				index++;
			}
		} catch (SQLException | HBException hbe) {
			if (HGlobal.DEBUG) {
				System.out.println("HBWhereWhenHandler - updateLocationStyleTable: " + hbe.getMessage());
			}
			if (HGlobal.DEBUG) {
				hbe.printStackTrace();
			}
			errorMessage("Name Mnangement \n" + hbe.getMessage());
		}
	}

/**
 * getAllNameStyleElements()
 * @throws HBException
 */
	public void getAllNameStyleElements(String dataLanguage) throws HBException {
		ResultSet nameStyleElements;
		try {
			nameStyleElements = pointLibraryResultSet.getNameStyleElements(nameElementsDefined, "P", dataLanguage, dataBaseIndex);
			nameStyleElements.first();
			String elementCodes = nameStyleElements.getString("ELEMNT_CODES");
			String elementDescription = nameStyleElements.getString("ELEMNT_NAMES");
			nameElementData = elementCodes.split("\\|");
			nameElementDescription = elementDescription.split("\\|");
			for (int i = 0; i < nameElementDescription.length; i++) {
				//Set up index to name element description with key name element code
				if (HGlobal.DEBUG) {
					System.out.println(" " + i + " element "
							   + nameElementData[i] + "/" + nameElementDescription[i]);
				}
				elementCodeMap.put(nameElementData[i],nameElementDescription[i]);
			}
		} catch (SQLException sqle) {
			if (HGlobal.DEBUG) {
				System.out.println("HBWhereWhenHandler - getAllNameStyleElements(): " + sqle.getMessage());
			}
			if (HGlobal.DEBUG) {
				sqle.printStackTrace();
			}
			throw new HBException("HBWhereWhenHandler - getAllNameStyleElements(): " + sqle.getMessage());
		}
	}

/**
 * findLocationNameTablePID(long locationTablePID)
 * @param locationTablePID
 * @return
 * @throws HBException
 */
	public long findLocationNameTablePID(long locationTablePID) throws HBException  {
		long locationNamePID;
		String selectString = setSelectSQL("*", locationNameTable, "OWNER_RPID = " + locationTablePID);
		ResultSet locationNameResultSet = requestTableData(selectString, dataBaseIndex);
		try {
			if (isResultSetEmpty(locationNameResultSet)) return null_RPID;
			locationNameResultSet.first();
			locationNamePID = locationNameResultSet.getLong("PID");
			return locationNamePID;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" HBWhereWhenHandler - findLocationNameTablePID: " + sqle.getMessage());
		}
	}

/**
 * updateManagePersonNameTable() called from Location Name Manager Show hidden
 * @return
 * @throws HBException
 * @throws
 */
	public int updateManageLocationNameTable() throws HBException  {
		hiddenElements = true;
		return updateManageLocationNameTable(locationNamePID);
	}
/**
 * updateManagePersonNameTable
 * @param locationNamePID
 * @return
 * @throws HBException
 */
	public int updateManageLocationNameTable(long locNamePID) throws HBException  {
		this.locationNamePID = locNamePID;
		int errorCode = 0;
		String[] personNameStyleDestriptions;
    	nameStyleElementCodes = nameStyleCodeString[nameStyleIndex].split("\\|");
      	personNameElementsData = updateRecordedNameData(locationNamePID);

      	exstractStyleAndDates(locationNamePID);

		if (isTmgNameStyle[nameStyleIndex]) {
		      	personNameStyleDestriptions = nameStyleDescriptionString[nameStyleIndex].split("\\|");
		    // Set up list of descriptions for TMG US standard Name style - second style in list
		      	String [] standardUSstyleDestriptions = nameStyleDescriptionString[1].split("\\|");
		      	for (int i = 0; i < personNameStyleDestriptions.length; i++)
					tmgCodeMap.put(nameStyleElementCodes[i], standardUSstyleDestriptions[i]);

		} else {
			personNameStyleDestriptions = new String[personNameElementsData.length];
			for (int i = 0; i < personNameElementsData.length; i++) {
				personNameStyleDestriptions[i] = elementCodeMap.get(personNameElementsData[i][0]);
			}
		}
      	updateNameElementGUItable(hiddenElements, personNameStyleDestriptions, nameStyleElementCodes);
		return errorCode;
	}

/**
 * detectHiddenElements()
 * @return
 */
	protected boolean detectHiddenElements() {
		numberHidden = 0;
		hiddenElements = false; // Reset flag for show hidden elements
		boolean hidden = false;
		for (String[] element : personNameElementsData) {
			for (String nameStyleElementCode : nameStyleElementCodes) {
				if (element[0].equals(nameStyleElementCode)) {
					//System.out.println(" Hidden: " + personNameElementsData[i][0] + "/" + nameStyleElementCodes[j]);
					hidden = false;
					break;
				}
				hidden = true;
			}
			if (hidden) {
				numberHidden++;
			}
		}
		return hidden;
	}

/**
 * exstractStyleAndDates(long locationNamePID)
 * @param locationNamePID
 * @throws HBException
 */
	private void exstractStyleAndDates(long locationNamePID) throws HBException {
		long nameStyleRPID = null_RPID, startRPID, endRPID;
		//System.out.println(" exstractStyleAndDates location name PID: " + locationNamePID);
		String selectString = setSelectSQL("*", locationNameTable, "PID = " + locationNamePID);
		locationNameResultSet = requestTableData(selectString, dataBaseIndex);
		nameStyleAndDates[0] = "  No selection!"; // Name style
		nameStyleAndDates[1] = "  "; // Start HDate
		nameStyleAndDates[2] = "  "; // End Hdate
		try {
			locationNameResultSet.last();
			//System.out.println(" Location name table rows: " + locationNameResultSet.getRow());
			if (locationNameResultSet.getRow() == 0) return;
			locationNameResultSet.first();
			nameStyleRPID = locationNameResultSet.getLong("NAME_STYLE_RPID");
			nameStyleTable.last();
			//System.out.println(" Name style table rows: " + nameStyleTable.getRow());
			nameStyleTable.beforeFirst();
			while (nameStyleTable.next()) {
				if (nameStyleRPID == nameStyleTable.getLong("PID")) {
					nameStyleAndDates[0] = nameStyleTable.getString("NAME_STYLE").trim();
				}
			}
			//System.out.println(" Location name style PID: " + nameStyleRPID + "/" + nameStyleAndDates[0] + "/");
			startRPID = locationNameResultSet.getLong("START_HDATE_RPID");
			endRPID = locationNameResultSet.getLong("END_HDATE_RPID");

			if (startRPID != null_RPID) {
				nameStyleAndDates[1] = pointLibraryResultSet.exstractDate(startRPID, dataBaseIndex);
			}
			if (endRPID != null_RPID) {
				nameStyleAndDates[2] = pointLibraryResultSet.exstractDate(endRPID, dataBaseIndex);
			}

		} catch (SQLException hbe) {
			throw new HBException("ManagePersonNameData - updateStyleAndDates: " + hbe.getMessage());
		}
	}

/**
 * createNameDates(boolean update, long nameTablePID)
 * @param update
 * @param nameTablePID
 * @throws HBException
 */
	public void createLocationNameDates(boolean update, long locationNameTablePID, String dateField, Object[] locationDateData) throws HBException {
		ResultSet locationNameTableRS;
		String selectString;
		long locationNameHdatePID;
		//System.out.println(" createNameDates: " + update + "/" + dateField + "/" + nameDateData[0]);
		try {
			selectString = setSelectSQL("*", locationNameTable, " PID = " + locationNameTablePID);
			locationNameTableRS = requestTableData(selectString, dataBaseIndex);
			locationNameTableRS.first();
			locationNameHdatePID = locationNameTableRS.getLong(dateField);
			if (update) {
		// Convert to Hdate format and update record
				if (locationNameHdatePID == null_RPID) {
					locationNameHdatePID = addDateRecord(locationDateData);
				}
				else {
					updateDateRecord(locationDateData, locationNameHdatePID);
		// Convert to Hdate format and add new record
				}
			} else {
				locationNameHdatePID = addDateRecord(locationDateData);
			}

		// Update the event record hdate
			locationNameTableRS.first();
			locationNameTableRS.updateLong(dateField, locationNameHdatePID);
			locationNameTableRS.updateRow();
			locationNameTableRS.close();
		} catch (SQLException sqle) {
			System.out.println(" HBWhereWhenHandler - createLocationNameDates(): " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" BWhereWhenHandler - createLocationNameDates(): " + sqle.getMessage());
		}
	}

/**
 * updateDateRecord(Object[] nameDateData, long nameHdatePID)
 * @param indexEventType
 * @param newTMGdate
 * @throws HBException
 */
	private void updateDateRecord(Object[] nameDateData, long nameHdatePID) throws HBException {
		String selectString;
		ResultSet hreDateResultSet;
		if (HGlobal.DEBUG) {
			System.out.println(" HBPersonHandler - updateDateRecord: " + nameDateData[0]);
		}

		selectString = setSelectSQL("*", dateTable, "PID = " + nameHdatePID);
		hreDateResultSet = requestTableData(selectString, dataBaseIndex);
		try {
			hreDateResultSet.first();
		} catch (SQLException sqle) {
			System.out.println(" HBPersonHandler - updateDateRecord: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException("  HBPersonHandler - updateDateRecord: " + sqle.getMessage());
		}
		HdateInput.updateT170_HDATES(hreDateResultSet, nameDateData);
	}

/**
 * addDateRecord(Object[] nameDateData)
 * @param indexEventType
 * @param newTMGdate
 * @throws HBException
 */
	private long addDateRecord(Object[] nameDateData) throws HBException {
		String selectString;
		long nextHdatePID;
		ResultSet hreDateResultSet;
		if (HGlobal.DEBUG) {
			System.out.println(" HBPersonHandler - addDateRecord: " + nameDateData[0]);
		}
		nextHdatePID = lastRowPID(dateTable, dataBaseIndex) + 1;
		selectString = setSelectSQL("*", dateTable, "");
		hreDateResultSet = requestTableData(selectString, dataBaseIndex);
		HdateInput.addT170_HDATES(hreDateResultSet, nextHdatePID, nameDateData);
		return nextHdatePID;
	}

/**
 * updateStoredNameStyle(int selectIndex, long personNamePID)
 * @param selectIndex
 * @param locationNamePID
 * @throws HBException
 */
	public void updateStoredNameStyle(int selectIndex, long locationNamePID) throws HBException {
		long selectedNameStylePID = nameStylePID[selectIndex];
		String selectString = setSelectSQL("*", locationNameTable, "PID = " + locationNamePID);
		ResultSet personNameTable = requestTableData(selectString, dataBaseIndex);
		try {
			personNameTable.first();
			personNameTable.updateLong("NAME_STYLE_RPID", selectedNameStylePID);
			personNameTable.updateRow();
			personNameTable.close();
		} catch (SQLException hbe) {
			throw new HBException("HBPersonHandler - updateStoredNameStyle: " + hbe.getMessage());
		}
	}

/**
 * updateRecordedNameData
 * @param locationNamePID
 * @return
 * @throws HBException
 */
	private String[][] updateRecordedNameData(long locationNamePID) throws HBException {

		String selectString;
		//ResultSet nameElementTable;
		int rowIndex = 0;
		try {
			selectString = setSelectSQL("*", locationNameElementTable,"OWNER_RPID = " + locationNamePID);
			nameElementRSet = requestTableData(selectString, dataBaseIndex);
			nameElementRSet.last();
			int rows = nameElementRSet.getRow();
			String[][] tableData = new String[rows][2];
			nameElementRSet.beforeFirst();
			while (nameElementRSet.next()) {
				tableData[rowIndex][0] = nameElementRSet.getString("ELEMNT_CODE").trim();
				tableData[rowIndex][1] = nameElementRSet.getString("NAME_DATA").trim();
				rowIndex++;
			}

			return tableData;
		} catch (HBException | SQLException hbe) {
			errorMessage("Name Mnanagement \n" + hbe.getMessage());
			throw new HBException("Name Mnanagement error\n" + hbe.getMessage());
		}
	}

/**
 * updateNameElementGUItable
 * @param showHiddenElements
 * @param nameStyleElementsDestriptions
 * @param nameStyleElementCodes
 */
	private void updateNameElementGUItable(boolean showHiddenElements,
											String[] nameStyleElementsDestriptions,
										    String[] nameStyleElementCodes) {

		if (showHiddenElements) {
			boolean hidden = false;
			int index = 0;
			detectHiddenElements();
			nameElementTable = new Object[numberHidden][2];
			for (String[] element : personNameElementsData) {
				for (String nameStyleElementCode : nameStyleElementCodes) {
					if (element[0].equals(nameStyleElementCode)) {
						//System.out.println(" Hidden: " + personNameElementsData[i][0] + "/" + nameStyleElementCodes[j]);
						hidden = false;
						break;
					}
					hidden = true;
				}
				if (hidden) {
					if (isTmgNameStyle[nameStyleIndex]) {
						nameElementTable[index][0] = tmgCodeMap.get(element[0]);
					} else {
						nameElementTable[index][0] = elementCodeMap.get(element[0]);
					}

					nameElementTable[index][1] = element[1];
					index++;
				}
			}
		} else {
			if (!isTmgNameStyle[nameStyleIndex]) {
				nameElementTable = new Object[nameStyleElementCodes.length][2];
				for (int i = 0; i < nameStyleElementCodes.length; i++) {
					nameElementTable[i][0] = elementCodeMap.get(nameStyleElementCodes[i]);
					for (String[] element : personNameElementsData) {
						if (element[0].equals(nameStyleElementCodes[i])) {
							nameElementTable[i][1] = element[1];
						}
					}
				}
			} else {
				nameElementTable = new Object[nameStyleElementCodes.length][2];
				for (int i = 0; i < nameStyleElementCodes.length; i++) {
					nameElementTable[i][0] = nameStyleElementsDestriptions[i];
					for (String[] element : personNameElementsData) {
						if (element[0].equals(nameStyleElementCodes[i])) {
							nameElementTable[i][1] = element[1];
						}
					}
				}
			}
		}
	}

/**
 * addToNameChangList(int selectedIndex, String nameData)
 * @param selectedIndex
 * @param nameData
 */
	public void addToNameChangeList(int selectedIndex, String nameData) {
		String nameElementCode = nameStyleElementCodes[selectedIndex];
		//System.out.println(" addToLocationNameChangList(): " +  nameElementCode + "/" + nameData);
		locationNameChanges.put(nameElementCode, nameData);
	}

/**
 * updateElementData()
 * @throws HBException
 */
	public void updateLocationElementData(long locationNamePID) throws HBException {
		String elementCode,selectString;
		long ownerRPID = 0;
		String locationNameData = "";
		boolean updated;

		if (locationNameChanges.size() == 0) return;
	// Start transaction
		updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
		try {
			selectString = setSelectSQL("*", locationNameElementTable, "OWNER_RPID = " + locationNamePID);
			nameElementRSet = requestTableData(selectString, dataBaseIndex);
			ownerRPID = locationNamePID;
			for (String styleElementCode : nameStyleElementCodes) {
				updated = false;
				if (locationNameChanges.containsKey(styleElementCode)) {
					//System.out.println(" updateLocationElementData: " + styleElementCode + "/" + locationNameChanges.get(styleElementCode));
					locationNameData = locationNameChanges.get(styleElementCode).trim();
					nameElementRSet.beforeFirst();
					while (nameElementRSet.next()) {
						elementCode = nameElementRSet.getString("ELEMNT_CODE").trim();
						if (styleElementCode.equals(elementCode)) {
						// Remove element if set to length = 0
							if (locationNameData.length() == 0) {
								nameElementRSet.deleteRow();
							} else {
								nameElementRSet.updateString("NAME_DATA", locationNameData);
								nameElementRSet.updateRow();
							}
							updated = true;
						}
					}
					if (updated || locationNameData.length() == 0) continue;

					long newPID = lastRowPID(locationNameElementTable, dataBaseIndex) + 1;
				// moves cursor to the insert row
					nameElementRSet.moveToInsertRow();
					nameElementRSet.updateLong("PID", newPID);
					nameElementRSet.updateLong("CL_COMMIT_RPID", null_RPID);
					nameElementRSet.updateLong("OWNER_RPID", ownerRPID);
					nameElementRSet.updateString("ELEMNT_CODE", styleElementCode);
					nameElementRSet.updateString("LANG_CODE", " -?-"); // To be removed - only marking
					nameElementRSet.updateString("NAME_DATA", locationNameData );
					nameElementRSet.insertRow();
				}
			}
			locationNameChanges = new HashMap<>();
	// End transaction
			 updateTableData("COMMIT", dataBaseIndex);
		} catch (SQLException sqle) {
	// Roll back transaction
			updateTableData("ROLLBACK", dataBaseIndex);
			System.out.println("HBWhereWhenHandler - updateLocationElementData(): " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException("HBWhereWhenHandler - updateLocationElementData(): " + sqle.getMessage());
		}
	}

/**
 * errorMessage(String errorMess)
 * @param errorMess
 */
	private void errorMessage(String errorMess) {
		JOptionPane.showMessageDialog(null, errorMess, "Name Manangement", JOptionPane.ERROR_MESSAGE);
	}
} // End class ManageLocationData

/**
 * class AddEventRecordextends HBBusinessLayer
 * Adding a person to T450_EVNT etc
 * @author NTo
 * @version v0.01.0030
 * @since 2023.09.14
 */
 class CreateEventRecord extends HBBusinessLayer {
	HBNameStyleManager pointLocationStyleData;
	int  dataBaseIndex;
	HBProjectOpenData pointOpenProject;
	int nextEventVisibleId, nextLocationVisibleId;
	long nextEventRecordPID, nextEventAssocPID, assocPersonRPID, nextLocationRecordPID,
		nextLocationNameRecordPID, nextLocNameElementRecordPID, newHREMemoPID, nameStyleRPID;

	ResultSet eventTableRS, locationTableRS, locationNameTableRS, locationNameElementTableRS;

	long null_RPID  = 1999999999999999L;
	long proOffset  = 1000000000000000L;

    long nextHdatePID, sortHdatePID, memoRPID;
    int eventType;
    int roleNumber;
    String memoString = " Memo string content";
    String selectString;

    HashMap<String,String>  locationNameChanges = new HashMap<>();

/**
 * Constructor AddEventRecord
 * @param dataBaseIndex
 * @throws HBException
 */
	public CreateEventRecord(HDDatabaseLayer pointDBlayer, int dataBaseIndex,
							HBProjectOpenData pointOpenProject) throws HBException {
		super();
		//this.pointDBlayer = pointDBlayer;
		//this.dataBaseIndex = dataBaseIndex;
		this.pointOpenProject = pointOpenProject;

		if (pointOpenProject != null) {
			this.pointDBlayer = pointOpenProject.pointProjectHandler.pointDBlayer;
			this.dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		} else System.out.println(" Error! CreateEventRecord -- pointOpenProject == null");

		pointLocationStyleData =  new HBNameStyleManager(pointDBlayer, dataBaseIndex, "Location ");
		pointLocationStyleData.updateStyleTable("P", nameStyles, nameElementsDefined);

      	setUpDateTranslation();
      	dateFormatSelect();
	}

/**
 * addBirthEventToPerson()
 */
	public void initiateAddEventsToPerson(long personRPID ) throws HBException {
		this.assocPersonRPID = personRPID;
	}

	public long editEventData() {
		System.out.println("editEventData() activated");
		return 0;
	}

	public long getLocationRecordPID() {
		return nextLocationRecordPID;
	}

	public long getLocationNameRecordPID() {
		return nextLocationNameRecordPID;
	}

/**
 * createEventRecord(int eventType, int eventRole,
			HashMap<String,String> personEventChanges, long eventDate, long sortDate, long newHREMemoPID)
 * @throws HBException
 * @throws HCException
 */
	public long createEventRecord(int eventType, int eventRole,
			HashMap<String,String> personEventChanges, long eventDate, long sortDate, long newHREMemoPID) throws HBException {

		this.eventType = eventType;
		this.roleNumber = eventRole;
		this.nextHdatePID = eventDate;
		this.sortHdatePID = sortDate;
		this.newHREMemoPID = newHREMemoPID;
		this.locationNameChanges = personEventChanges;
		nextEventRecordPID = lastRowPID(eventTable, dataBaseIndex) + 1;
		nextEventAssocPID = lastRowPID(eventAssocTable, dataBaseIndex) + 1;
		nextLocationRecordPID = lastRowPID(locationTable, dataBaseIndex) + 1;
		nextLocationNameRecordPID = lastRowPID(locationNameTable, dataBaseIndex) + 1;
		nextLocNameElementRecordPID = lastRowPID(locationNameElementTable, dataBaseIndex) + 1;
		nameStyleRPID = pointLocationStyleData.
				getNameStylePID(pointLocationStyleData.getDefaultStyleIndex());
	// Start transaction handling
		updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
		try {
			selectString = setSelectSQL("*", eventTable, "");
			eventTableRS = requestTableData(selectString, dataBaseIndex);

		// Set up data data for eventTable
			nextEventVisibleId = HBLibraryBusiness.findGreatestVisibleId(eventTableRS) + 1;

			memoRPID = addToT167_Memo(memoString);
			addToT450_EVNT(eventTableRS);

		// Data for locationTable
			selectString = setSelectSQL("*", locationTable, "");
			locationTableRS = requestTableData(selectString, dataBaseIndex);
			nextLocationVisibleId = HBLibraryBusiness.findGreatestVisibleId(locationTableRS) + 1;
			addToT551_LOCATIONS(locationTableRS);

		// Data for locationNameTable
			selectString = setSelectSQL("*", locationNameTable, "");
			locationNameTableRS = requestTableData(selectString, dataBaseIndex);
			addToT552_LOCATION_NAMES(locationNameTableRS);

		// Data for locationNameElementTable
			selectString = setSelectSQL("*", locationNameElementTable, "");
			locationNameElementTableRS = requestTableData(selectString, dataBaseIndex);

		// Iterating HashMap locationNameChanges for all entries
	        long elementNamePID = nextLocNameElementRecordPID;
			for (HashMap.Entry<String, String> mapset : locationNameChanges.entrySet()) {
				if (HGlobal.DEBUG)
					System.out.println( " Name elements code: " + mapset.getKey()
																+ " = " + mapset.getValue());
				if (mapset.getValue().length() != 0)
					addToT553_LOCATION_NAME_ELEMENTS(locationNameElementTableRS, elementNamePID,
												mapset.getKey(),
												mapset.getValue());
	            elementNamePID++;
	        }

			// Update person table data
			if (eventType == birthEventType) {
				updatePersonData( true, false);
			}
			if (eventType == deathEventType) {
				updatePersonData(false, true);
			}

			eventTableRS.close();
			locationTableRS.close();
			locationNameTableRS.close();
			locationNameElementTableRS.close();
		// End transaction and COMMIT
			updateTableData("COMMIT", dataBaseIndex);
			return nextEventRecordPID;

		} catch (SQLException sqle) {
			if (HGlobal.DEBUG) {
				System.out.println(" Error HBWhereWhenHandler - addEvent/Location ROLLBACK: " + sqle.getMessage());
			}
			updateTableData("ROLLBACK", dataBaseIndex);
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Error addEvent/Location ROLLBACK:  " + sqle.getMessage());
				HB0711Logging.printStackTraceToFile(sqle);
			}
			throw new HBException(" Error HBWhereWhenHandler - addEvent/Location : " + sqle.getMessage());
		}
	}

/**
 * public void createLocationRecord()
 * @throws HBException
 */
	public long createLocationRecord(long eventTablePID) throws HBException {

		nextLocationRecordPID = lastRowPID(locationTable, dataBaseIndex) + 1;
		nextLocationNameRecordPID = lastRowPID(locationNameTable, dataBaseIndex) + 1;
		nextLocNameElementRecordPID = lastRowPID(locationNameElementTable, dataBaseIndex) + 1;

		// Start transaction handling
		updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
		try {
		// Data for locationTable
			selectString = setSelectSQL("*", locationTable, "");
			locationTableRS = requestTableData(selectString, dataBaseIndex);
			nextLocationVisibleId = HBLibraryBusiness.findGreatestVisibleId(locationTableRS) + 1;
			addToT551_LOCATIONS(locationTableRS);

		// Data for locationNameTable
			selectString = setSelectSQL("*", locationNameTable, "");
			locationNameTableRS = requestTableData(selectString, dataBaseIndex);
			addToT552_LOCATION_NAMES(locationNameTableRS);

		// Data for locationNameElementTable
			selectString = setSelectSQL("*", locationNameElementTable, "");
			locationNameElementTableRS = requestTableData(selectString, dataBaseIndex);

		// Iterating HashMap locationNameChanges for all entries
	        long elementNamePID = nextLocNameElementRecordPID;
			for (HashMap.Entry<String, String> mapset : locationNameChanges.entrySet()) {
				if (HGlobal.DEBUG) {
					System.out.println( " Name elements code: " + mapset.getKey()
																+ " = " + mapset.getValue());
				}
				if (mapset.getValue().length() != 0) {
					addToT553_LOCATION_NAME_ELEMENTS(locationNameElementTableRS, elementNamePID,
												mapset.getKey(),
												mapset.getValue());
				}
	            elementNamePID++;
	        }

		// Update T450 event with new location
			selectString = setSelectSQL("*", eventTable, "PID = " + eventTablePID);
			eventTableRS = requestTableData(selectString, dataBaseIndex);
			eventTableRS.first();
			eventTableRS.updateLong("EVNT_LOCN_RPID", nextLocationRecordPID);
			eventTableRS.updateRow();

			// Update person table data
			if (eventType == birthEventType) {
				updatePersonData( true, false);
			}
			if (eventType == deathEventType) {
				updatePersonData(false, true);
			}

			eventTableRS.close();
			locationTableRS.close();
			locationNameTableRS.close();
			locationNameElementTableRS.close();

		// End transaction and COMMIT
			updateTableData("COMMIT", dataBaseIndex);
			return nextLocationRecordPID;
		} catch (SQLException sqle) {
			if (HGlobal.DEBUG) {
				System.out.println(" Error HBWhereWhenHandler - addEvent/Location ROLLBACK: " + sqle.getMessage());
			}
			updateTableData("ROLLBACK", dataBaseIndex);
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Error addEvent/Location ROLLBACK:  " + sqle.getMessage());
				HB0711Logging.printStackTraceToFile(sqle);
			}
			throw new HBException(" Error HBWhereWhenHandler - addEvent/Location : " + sqle.getMessage());
		}
	}

/**
 * updatePersonData(boolean updateBirth, boolean updateDeath)
 * @param updateBirth
 * @param updateDeath
 * @throws HBException
 */
	public void updatePersonData(boolean updateBirth, boolean updateDeath) throws HBException {
		String selectString;
		ResultSet personTableRS;
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", personTable, "PID = " + assocPersonRPID);

		try {
			personTableRS = requestTableData(selectString, dataBaseIndex);
			if (isResultSetEmpty(personTableRS)) {
				return;
			}

		// Continue remove data
			personTableRS.first();
			if (updateBirth) {
				personTableRS.updateLong(personLocnBirthField, nextLocationRecordPID);
			}
			if (updateDeath) {
				personTableRS.updateLong(personLocnDeathField, nextLocationRecordPID);
			}
			personTableRS.updateRow();
			personTableRS.close();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" HBWhereWhenHandler - removePersonHdates: " + sqle.getMessage());
		}
	}

/**
 * addToT450_EVNT(int rowPID,ResultSet hreTable)
 * @param rowPID
 * @param hreTable
 * @throws HCException
 * @throws SQLException
 */
	public void addToT450_EVNT(ResultSet hreTable) throws SQLException {
		long endDate = null_RPID; // Dummy variable
		if (HGlobal.DEBUG) {
			System.out.println("** addTo T450_EVENTS row: " + nextEventRecordPID + " EVtype: " + eventType);
		}

	// moves cursor to the insert row
		hreTable.moveToInsertRow();
	// Update new row in H2 database
		hreTable.updateLong("PID", nextEventRecordPID);
		hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
		hreTable.updateBoolean("HAS_CITATIONS", false);
		//hreTable.updateBoolean("IMP_TMG", false); // Mark partner as imported from TMG
	// Has lower event group
		hreTable.updateBoolean("OWNS_EVENTS",false);
		hreTable.updateInt("VISIBLE_ID", nextEventVisibleId);
		hreTable.updateString("SURETY", " -- ");

		hreTable.updateInt("EVNT_TYPE", eventType);
		hreTable.updateInt("PRIM_ASSOC_BASE_TYPE",0);
		hreTable.updateLong("PRIM_ASSOC_RPID", assocPersonRPID);
		hreTable.updateInt("PRIM_ASSOC_ROLE_NUM", roleNumber);

		hreTable.updateLong("BEST_IMAGE_RPID", null_RPID);
	// Future upper event group
		hreTable.updateLong("EVNT_OWNER_RPID", null_RPID);
		hreTable.updateLong("EVNT_LOCN_RPID", nextLocationRecordPID);

		//***** HDATE processing
		hreTable.updateLong("SORT_HDATE_RPID", sortHdatePID);
		hreTable.updateLong("START_HDATE_RPID", nextHdatePID);
		hreTable.updateLong("END_HDATE_RPID", endDate);
		hreTable.updateLong("THEME_RPID", null_RPID);

	// Point to memo in T167_MEMO_SET
		hreTable.updateLong("MEMO_RPID", newHREMemoPID);
	// Assoc sentence
		hreTable.updateLong("PRIM_ASSOC_SENTENCE_RPID", null_RPID);

	//Insert row
		hreTable.insertRow();
	}

/**
 * addToT551_LOCATIONS(ResultSet hreTable)
 * @param locationTablePID
 * @param hreTable
 * @throws SQLException
 */
	public void addToT551_LOCATIONS(ResultSet hreTable) throws SQLException {

		if (HGlobal.DEBUG) {
			System.out.println(" ** addTo T551_LOCATIONS PID: " + nextLocationRecordPID);
		}

	// moves cursor to the insert row
		hreTable.moveToInsertRow();
	// Update new row in H2 database
		hreTable.updateLong("PID", nextLocationRecordPID);
		hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
		hreTable.updateBoolean("HAS_CITATIONS", false);
		hreTable.updateLong("VISIBLE_ID", nextLocationVisibleId);
		hreTable.updateLong("ENTITY_TYPE_RPID", null_RPID);
		hreTable.updateString("SURETY", "3");
		hreTable.updateLong("BEST_NAME_RPID", nextLocationNameRecordPID);
		hreTable.updateLong("BEST_IMAGE_RPID", null_RPID);
		//Insert row
		hreTable.insertRow();
	}

/**
 * addToT552_LOCATION_NAMES(int rowPID,ResultSet hreTable)
 * @param rowPID
 * @param hreTable
 * @throws SQLException
 */
	public void addToT552_LOCATION_NAMES(ResultSet hreTable) throws SQLException {
		if (HGlobal.DEBUG) {
			System.out.println("** addTo T552_LOCATION_NAMES PID: " + nextLocationNameRecordPID);
		}

	// moves cursor to the insert row
		hreTable.moveToInsertRow();

	// Update new row in H2 database
		hreTable.updateLong("PID", nextLocationNameRecordPID);
		hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
		hreTable.updateBoolean("HAS_CITATIONS", false);
		hreTable.updateLong("OWNER_RPID", nextLocationRecordPID);
		// Set name style RPID
		hreTable.updateLong("NAME_STYLE_RPID", nameStyleRPID);

	// Processing HDATE
		hreTable.updateLong("START_HDATE_RPID", null_RPID);		// Tp be updated
		hreTable.updateLong("END_HDATE_RPID", null_RPID); // Tp be updated

		hreTable.updateLong("THEME_RPID", null_RPID);
		hreTable.updateLong("MEMO_RPID", null_RPID);

	// Processing memo to T167_MEMO_SET
		String comment ="";
		if (comment.length() == 0) {
			hreTable.updateLong("MEMO_RPID", null_RPID);
		} else {
			hreTable.updateLong("MEMO_RPID", addToT167_Memo(comment));
		}
		hreTable.updateString("SURETY", " -- ");
	//Insert row
		hreTable.insertRow();
	}

/**
 * addToT403_PERSON_NAME_ELEMENTS
 * @param rowPID
 * @param hreTable
 * @throws SQLException
 * H2 metadata for: T403_PERSON_NAME_ELEMENTS
1 - PID Type: BIGINT  Presision: 19
2 - CL_COMMIT_RPID Type: BIGINT  Presision: 19
3 - IS_SYSTEM Type: BOOLEAN  Presision: 1
4 - OWNER_RPID Type: BIGINT  Presision: 19
5 - LIST_ENCODING_TYPE Type: BIGINT  Presision: 19
6 - LANG_CODE Type: VARCHAR  Presision: 5
7 - SHORT_NAME_DATA Type: VARCHAR  Presision: 300
8 - LONG_NAME_DATA Type: CLOB  Presision: 30000
 */
	public void addToT553_LOCATION_NAME_ELEMENTS(ResultSet hreTable,
												long elementNamePID,
												String elementCode,
												String nameElement) throws SQLException {
		if (HGlobal.DEBUG) {
			System.out.println("** addTo T553_LOCATION_NAME_ELEMENTS PID: " + elementNamePID);
		}

	// moves cursor to the insert row
		hreTable.moveToInsertRow();
	// -----------------
		hreTable.updateLong("PID", elementNamePID);
		hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
		hreTable.updateLong("OWNER_RPID", nextLocationNameRecordPID);
		hreTable.updateString("LANG_CODE", " ");
		hreTable.updateString("ELEMNT_CODE", elementCode);		// Tobe updated
		hreTable.updateString("NAME_DATA", nameElement);

	//Insert row
		hreTable.insertRow();
	}

/**
 * addToT167_MEMO(String comment)
 * @param comment
 * @return
 */
	private long addToT167_Memo(String comment) {
		long memoRPID = null_RPID;
		return memoRPID;
	}

} // End class CreateEventRecord

class EditEventRecord extends HBBusinessLayer {

	long null_RPID  = 1999999999999999L;
	long proOffset = 1000000000000000L;

	HBNameStyleManager pointLocationStyleData;
	CreateEventRecord pointCreateEventRecord = null;
	HBProjectOpenData pointOpenProject;
	int  dataBaseIndex;

    long eventHdatePID = null_RPID, deathHdatePID = null_RPID, nextHdatePID = null_RPID,
    		nextHREMemoPID = null_RPID, sortHdatePID = null_RPID;
    long birthPlaceRPID = null_RPID, deathPlaceRPID = null_RPID;

    String[] eventDates = new String[2];
    String startTMGdate = "", sortTMGdate = "";
    String personPartnerNames;
	String[] eventTypeList;
	int[] eventTypeNumber;
	String[] eventRoleList;
	Object[][] eventRoleData;
	int[] eventRoleType;
	int selectedNameStyle;
// Memo handling
	String memoElement = "";
	long memoElementPID = null_RPID;
	ResultSet eventResultSet;
// Assoc handling
	long assocPersonPID;
	long assocEventPID;
	long primAssocPID;
	long locationNamePID = null_RPID;

	HashMap<Integer, Object[]> assocDataHash;
	ArrayList<Object[]> asociateList;
	Object [][] associateTable;
	String [] personStyle;
	long eventTablePID;

	String[] locationHeaderData;
	HashMap<String,String> eventLocationChanges = new HashMap<String,String>();

	public int getDefaultLocationStyleIndex() {
		return pointLocationStyleData.getDefaultStyleIndex();
	}

	public long getLocationNameRecordPID() {
		return locationNamePID;
	}

	public String[] getDates() {
		return eventDates;
	}

	public Object[][] getLocationDataTable(int styleIndex) {
		selectedNameStyle = styleIndex;
		return pointLocationStyleData.createNameStyleTable(styleIndex);
	}

	public String[] getLocationTableHeader() {
		return locationHeaderData;
	}

	public String[] getLocationStyles() {
		return pointLocationStyleData.getNameStyleList();
	}

	public Object[][] getAssociateTable() {
		return associateTable;
	}

	public String getPartnerNames() {
		return personPartnerNames;
	}

	public void setPartnerNames(String personPartnerNames) {
		this.personPartnerNames = personPartnerNames;
	}

/**
 * Constructor EditEventRecord(HDDatabaseLayer pointDBlayer, int dataBaseIndex,HBProjectOpenData pointOpenProject)
 * @param pointDBlayer
 * @param dataBaseIndex
 * @param pointOpenProject
 * @throws HBException
 */
	public EditEventRecord(HDDatabaseLayer pointDBlayer, int dataBaseIndex, HBProjectOpenData pointOpenProject,
			long eventTablePID, long primAssocPID) throws HBException {
			super();
		this.primAssocPID = primAssocPID;
		this.pointDBlayer = pointDBlayer;
		this.dataBaseIndex = dataBaseIndex;
		this.pointOpenProject = pointOpenProject;
		this.eventTablePID = eventTablePID;
		createEditEventRecord();
	}

	public EditEventRecord(HDDatabaseLayer pointDBlayer, int dataBaseIndex, HBProjectOpenData pointOpenProject,
			long eventTablePID) throws HBException {
		super();
		this.pointDBlayer = pointDBlayer;
		this.dataBaseIndex = dataBaseIndex;
		this.pointOpenProject = pointOpenProject;
		this.eventTablePID = eventTablePID;
		createEditEventRecord();
	}

/**
 * @throcreateEditEventRecord()ws HBException
 */
	private void createEditEventRecord() throws HBException {

		pointLocationStyleData =  new HBNameStyleManager(pointDBlayer, dataBaseIndex, "Location ");
		pointLocationStyleData.updateStyleTable("P", nameStyles, nameElementsDefined);
		locationHeaderData = setTranslatedData("50800","1", false);
		long nameStyleRPID = proOffset + 1;  // Set preligm to first N person name style

		personStyle =  getNameStyleOutputCodes(nameStylesOutput, nameStyleRPID, "N", dataBaseIndex);

		setUpDateTranslation();
		dateFormatSelect();

		//Load assoc table
		prepareAssociateTable(eventTablePID);

	}

/**
 * public void changeEventType(long eventTablePID, int selectedEventNum, int selectedRoleNum)
 * @param eventTablePID
 * @param selectedEventNum
 * @param selectedRoleNum
 * @throws HBException
 */
	public void changeEventType(long eventTablePID, int selectedEventNum, int selectedRoleNum) throws HBException {
		//System.out.println(" HBWhereWhenHandler - changeEventType: " + eventTablePID);
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		String selectString = setSelectSQL("*", eventTable, "PID = " + eventTablePID);
	//Start transaction
		updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
		try {
			eventResultSet = requestTableData(selectString, dataBaseIndex);
			eventResultSet.first();
			eventResultSet.updateInt("EVNT_TYPE", selectedEventNum );
			eventResultSet.updateInt("PRIM_ASSOC_ROLE_NUM", selectedRoleNum);
			eventResultSet.updateRow();
		// Now commit all updated for event
			updateTableData("COMMIT", dataBaseIndex);
		} catch (SQLException sqle) {
			updateTableData("ROLLBACK", dataBaseIndex);
			sqle.printStackTrace();
			throw new HBException(" HBWhereWhenHandler + readFromGUIMemo error: " + sqle.getMessage());
		}
	}

/**
 * void addToLocationChangeList(int selectedIndex, String nameData)
 * @param selectedIndex
 * @param nameData
 */
	public void addToLocationChangeList(int selectedIndex, String nameData) {
		// ** Note: if test edited out for remove space in element table? **
		//if (nameData != null) {
		//if (nameData.length() > 0) {
			String nameElementCode = pointLocationStyleData.getNameStyleElementCodes(selectedNameStyle)[selectedIndex];
			if (HGlobal.DEBUG)
				System.out.println(" Edit EventRecord - addToLocationChangeList(): " +  nameElementCode + " / " + nameData);
			eventLocationChanges.put(nameElementCode, nameData);
		//}
	}

/**
 * setDatesForEdit(long hdateDate, long hdateSort)
 * @param hdateDate
 * @param hdateSort
 * @throws HBException
 */
	public void setDatesForEdit(long hdateDate, long hdateSort) throws HBException {
		eventHdatePID = hdateDate;
		sortHdatePID = hdateSort;
		eventDates[0] = pointLibraryResultSet.exstractDate(hdateDate, dataBaseIndex).trim();
		eventDates[1] = pointLibraryResultSet.exstractDate(hdateSort, dataBaseIndex).trim();
		if (HGlobal.DEBUG) {
			System.out.println(" Date/sort: " + eventDates[0] + "/" + eventDates[1]);
		}
	}

/**
 * String readFromGUIMemo(long memoElementPID)
 * @param memoElementPID
 * @return
 * @throws HBException
 */
	public String readFromGUIMemo(long eventPID) throws HBException {

		String selectString = setSelectSQL("*", eventTable, " PID = " + eventPID);
		eventResultSet = requestTableData(selectString, dataBaseIndex);
		try {
			eventResultSet.first();
			memoElementPID = eventResultSet.getLong("MEMO_RPID");
			if (memoElementPID == null_RPID) {
				return " No memo found";
			}
			if (HGlobal.DEBUG) {
				System.out.println(" MemoPID: " + memoElementPID);
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" HBWhereWhenHandler + readFromGUIMemo error: " + sqle.getMessage());
		}
		HREmemo pointHREmemo = new HREmemo(pointDBlayer, dataBaseIndex);
		return pointHREmemo.readMemo(memoElementPID);
	}

/**
 * updateFromGUIMemo(String memoElement)
 * @param memoElement
 * @param memoOwnerTable
 * @param nextOwnerPID
 * @throws HBException
 */
	public void createFromGUIMemo(String memoElement) throws HBException {
		this.memoElement = memoElement;

	//	No memo element if textlength is zero
		if (memoElement.length() == 0) {
			return;
		}

		try {
			updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
			HREmemo pointHREmemo = new HREmemo(pointDBlayer, dataBaseIndex);
			nextHREMemoPID = pointHREmemo.addMemoRecord(memoElement);

		// Now commit all updated for memo
			updateTableData("COMMIT", dataBaseIndex);
		} catch (HBException hbe) {
			updateTableData("ROLLBACK", dataBaseIndex);
			System.out.println(" HBPersonHandler - updateFromGuiMemo error: " + hbe.getMessage());
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Error addPersonEvent: :  " + hbe.getMessage());
				HB0711Logging.printStackTraceToFile(hbe);
			}
			throw new HBException(" HBPersonHandler - updateFromGuiMemo error: " + hbe.getMessage());
		}
	}

/**
 * updateFromGUIMemo(String memoElement, long memoPID)
 * @param memoElement
 * @param memoPID
 * @throws HBException
 */
	public void updateFromGUIMemo(String memoElement) throws HBException {
		HREmemo pointHREmemo = new HREmemo(pointDBlayer, dataBaseIndex);
		if (memoElementPID != null_RPID) {
			pointHREmemo.findT167_MEMOrecord(memoElement, memoElementPID);
		} else {
			updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
			createFromGUIMemo(memoElement);
			try {
				eventResultSet.updateLong("MEMO_RPID", nextHREMemoPID);
				eventResultSet.updateRow();
				updateTableData("COMMIT", dataBaseIndex);
			} catch (SQLException sqle) {
				System.out.println(" HBWhereWhenHandler  - updateFromGUIMemo ROLLBACK: \n" + sqle.getMessage());
				updateTableData("ROLLBACK", dataBaseIndex);
				System.out.println(" HBWhereWhenHandler - updateFromGUIMemo: " + sqle.getMessage());
				sqle.printStackTrace();
			}
		}
	}

/**
 * createNameDates(boolean update, long nameTablePID)
 * @param update
 * @param nameTablePID
 * @throws HBException
 */
	public void createEventDates(boolean update, long eventTablePID, String dateField, Object[] eventDateData) throws HBException {
		ResultSet eventTableRS;
		String selectString;
		long eventHdatePID;
		int eventType;
		if (HGlobal.DEBUG) {
			System.out.println(" HBWhereWhenHandler - createEventDates: " + update + "/" + dateField + "/" + eventDateData[0]);
		}
		try {
			selectString = setSelectSQL("*", eventTable, " PID = " + eventTablePID);
			eventTableRS = requestTableData(selectString, dataBaseIndex);
			eventTableRS.first();
			eventHdatePID = eventTableRS.getLong(dateField);
			eventType = eventTableRS.getInt("EVNT_TYPE");

			if (update) {
		// Convert to Hdate format and update record
				if (eventHdatePID == null_RPID) {
					eventHdatePID = addDateRecord(eventDateData);
					//nextHdatePID = eventHdatePID;
				} else {
					updateDateRecord(eventDateData, eventHdatePID);
				}

		// Convert to Hdate format and add new record
			} else {
				eventHdatePID = addDateRecord(eventDateData);
			}

			nextHdatePID = eventHdatePID;

		// Update the event record hdate
			eventTableRS.first();
			eventTableRS.updateLong(dateField, eventHdatePID);
			eventTableRS.updateRow();
			eventTableRS.close();

		// Update person table T401
			if (eventType == 1002) {
				updatePersonData(true, false);
			}
			if (eventType == 1003) {
				updatePersonData(false, true);
			}

		} catch (SQLException sqle) {
			System.out.println(" HBWhereWhenHandler - createEventDates(): " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" HBWhereWhenHandler - createEventDates(): " + sqle.getMessage());
		}
	}

/**
 * addDateRecord(Object[] nameDateData)
 * @param indexEventType
 * @param newTMGdate
 * @throws HBException
 */
	private long addDateRecord(Object[] nameDateData) throws HBException {
		String selectString;
		long nextHdatePID;
		ResultSet hreDateResultSet;
		if (HGlobal.DEBUG) {
			System.out.println(" HBWhereWhenHandler - addDateRecord: " + nameDateData[0]);
		}
		nextHdatePID = lastRowPID(dateTable, dataBaseIndex) + 1;
		selectString = setSelectSQL("*", dateTable, "");
		hreDateResultSet = requestTableData(selectString, dataBaseIndex);
		HdateInput.addT170_HDATES(hreDateResultSet, nextHdatePID, nameDateData);
		return nextHdatePID;
	}

/**
 * updateDateRecord(Object[] nameDateData, long nameHdatePID)
 * @param indexEventType
 * @param newTMGdate
 * @throws HBException
 */
	private void updateDateRecord(Object[] nameDateData, long nameHdatePID) throws HBException {
		String selectString;
		ResultSet hreDateResultSet;
		if (HGlobal.DEBUG) {
			System.out.println(" HBWhereWhenHandler - updateDateRecord: " + nameDateData[0]);
		}

		selectString = setSelectSQL("*", dateTable, "PID = " + nameHdatePID);
		hreDateResultSet = requestTableData(selectString, dataBaseIndex);
		try {
			hreDateResultSet.first();
		} catch (SQLException sqle) {
			System.out.println(" HBWhereWhenHandler - updateDateRecord: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException("  HBWhereWhenHandler - updateDateRecord: " + sqle.getMessage());
		}
		HdateInput.updateT170_HDATES(hreDateResultSet, nameDateData);
	}

/**
 * updatePersonData(boolean updateBirth, boolean updateDeath)
 * @param updateBirth
 * @param updateDeath
 * @throws HBException
 */
	public void updatePersonData(boolean updateBirth, boolean updateDeath) throws HBException {
		String selectString;
		ResultSet personTableRS;
		long selectedPersonPID = pointOpenProject.getSelectedPersonPID();

		//System.out.println(" HBWhereWhenHandler - updatePersonData selected person: " + selectedPersonPID);
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", personTable, "PID = " + selectedPersonPID);
		try {
			personTableRS = requestTableData(selectString, dataBaseIndex);
			if (isResultSetEmpty(personTableRS)) {
				return;
			}

		// Continue update person T401 data
			personTableRS.first();
			if (updateBirth) {
				personTableRS.updateLong(personHDateBirthField, nextHdatePID);
				//personTableRS.updateLong(personLocnBirthField, birthPlaceRPID);
				//System.out.println(" Update person birth Hdate: " + nextHdatePID + " Birth: " + birthPlaceRPID);
			}
			if (updateDeath) {
				personTableRS.updateLong(personHdateDeathField, nextHdatePID);
				//personTableRS.updateLong(personLocnDeathField, deathPlaceRPID);
				//System.out.println(" Update person death Hdate: " + nextHdatePID + " Death: " + birthPlaceRPID);
			}
			personTableRS.updateRow();
			personTableRS.close();

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" HBWhereWhenHandler - updatePersonData: " + sqle.getMessage());
		}
	}

/**
 * public long getAssoctablePID(int tableIndex)
 * @param tableIndex
 * @return
 */
	public long getAssoctablePID(int tableIndex) {
		return (long) assocDataHash.get(tableIndex)[0];
	}

/**
 * public Object[] getAssociteTableData(int indexInTable)
 * @param indexInTable
 * @return
 */
	public Object[] getAssocTableData(int indexInTable) {
		return assocDataHash.get(indexInTable);
	}

/**
 * prepareAssociateTable(long selectPersonPID)
 * @param selectedEventPID
 * @throws HBException
 */
	void prepareAssociateTable(long selectedEventPID) throws HBException {
		String selectString = null;
		int asociateRows = 0;
		ResultSet eventSelected, eventAssocSelected;
		String langCode = HGlobal.dataLanguage;
		Object[] assocRelationData;

		assocDataHash = new HashMap<>();
		asociateList = new ArrayList<>();
		String personName = "", eventRole = "";
		int assocRows = 0, eventNumber = 0, eventRoleCode, selfAssocCount = 0;
		long eventPID, assocPersonPID, assocTablePID, selectedPersonPID;

		/**
		 * Test for duplicate assoc person - 5-11-2024
		 */
		Vector<Long> assocPersonDuplicateList = new Vector<>(100,10);

		selectedPersonPID = pointOpenProject.getSelectedPersonPID();
		try {

		// Get associate persons with the events in list
			selectString = setSelectSQL("*", eventAssocTable, "EVNT_RPID = " + selectedEventPID);
			eventAssocSelected = requestTableData(selectString, dataBaseIndex);
			eventAssocSelected.beforeFirst();
			while (eventAssocSelected.next()) {

				assocTablePID = eventAssocSelected.getLong("PID");
				eventPID = eventAssocSelected.getLong("EVNT_RPID");
				assocPersonPID = eventAssocSelected.getLong("ASSOC_RPID");
				eventRoleCode = eventAssocSelected.getInt("ROLE_NUM");

			// Test for duplicate assoc person - 5-11-2024
				if (assocPersonDuplicateList.contains(assocPersonPID)) continue;
				assocPersonDuplicateList.add(assocPersonPID);

			// The focus person can also be a witness to another event
			// if edit vitnessed event check primary assoc from event table
				if (primAssocPID != null_RPID) selectedPersonPID = primAssocPID;

				if (assocPersonPID != selectedPersonPID) {
			// Get event data
					selectString = setSelectSQL("*", eventTable, "PID = " + eventPID);
					eventSelected = requestTableData(selectString, dataBaseIndex);
					eventSelected.beforeFirst();
					while (eventSelected.next()) {
				/**
				 * assocRelationData interpretation
				 * 0 = assocTablePID, 1 = assocRole, 2 = assocName
				 */
						assocRelationData = new Object[3];
						eventNumber = eventSelected.getInt("EVNT_TYPE");
						eventRole = pointLibraryResultSet.getRoleName(eventRoleCode,
							  			eventNumber,
							  			langCode,
							  			dataBaseIndex);
						personName = pointLibraryResultSet.exstractPersonName(assocPersonPID, personStyle, dataBaseIndex);
						//System.out.println(" Associate: " + assocTablePID + " Event PID: " + eventPID + " Type: " + eventNumber
						//					+ " Event: " + eventName + " Name: " + personName + " Role: " + eventRole);

						Object[] asociates = new String[2];
						asociates[0] = " " + personName.trim();
						asociates[1] = " " + eventRole.trim();
						asociateList.add(asociates);

					// **********
						assocRelationData[0] = assocTablePID;
						assocRelationData[1] = eventRoleCode;
						assocRelationData[2] = personName.trim();
						assocDataHash.put(assocRows, assocRelationData);
					//************
						assocRows++;
					}
				} else {
					selfAssocCount++;
					if (HGlobal.DEBUG) {
						System.out.println(" HBWhereWhenHandler - Self assoc: " + selfAssocCount
													  + " Event PID: " + eventPID
													  + " RoleCode: " + eventRoleCode
													  + " Assoc per: " + assocPersonPID);
					}
				}
			}

			if (HGlobal.DEBUG) {
				if (selfAssocCount > 0) {
					System.out.println(" Number of self assocs: " + selfAssocCount);
				}
			}

		// Set number of row in asscociate table
			asociateRows = asociateRows + assocRows;
			associateTable = new Object[asociateRows][2];
			for (int i = 0; i < asociateRows; i++) {
				associateTable[i][0] = asociateList.get(i)[0];
				associateTable[i][1] = asociateList.get(i)[1];
			}
		} catch (SQLException sqle) {
			throw new HBException("SQL exception: " + sqle.getMessage()
						+ "\nSQL string: " + selectString);
		}
	}

/**
 * createAssocTableRow()
 * @throws HBException
 */
	public long createAssocTableRow(int roleNumber, long personPID) throws HBException {
		ResultSet assocTableRS = null;
		String selectString;
		long nextAssocTablePID = lastRowPID(eventAssocTable, dataBaseIndex) + 1;
		selectString = setSelectSQL("*", eventAssocTable, "");
		assocTableRS = requestTableData(selectString, dataBaseIndex);
		assocPersonPID = personPID;
		assocEventPID = eventTablePID;
		addToT451_EVNT_ASSOC(assocTableRS, nextAssocTablePID, roleNumber);

	// Reset Update event
		//pointOpenProject.getWhereWhenHandler().resetUpdateEvent(pointOpenProject);
		return nextAssocTablePID;
	}

/**
 * private void addToT451_EVNT_ASSOC(ResultSet hreTable, long nextAssocTablePID, int roleNumber)
 * @param hreTable
 * @param nextAssocTablePID
 * @param roleNumber
 * @throws HBException
 */
	private void addToT451_EVNT_ASSOC(ResultSet hreTable, long nextAssocTablePID, int roleNumber) throws HBException {
		if (HGlobal.DEBUG) {
			System.out.println("Start - addToT451_EVNT_ASSOC PID: "
								+ nextAssocTablePID);
		}
		// Start transaction
		   updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
		try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();
			hreTable.updateLong("PID", nextAssocTablePID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateBoolean("HAS_CITATIONS", false);
			hreTable.updateInt("ASSOC_BASE_TYPE", 1);
			hreTable.updateLong("ASSOC_RPID", assocPersonPID);
			hreTable.updateLong("EVNT_RPID", assocEventPID);
			hreTable.updateBoolean("KEY_ASSOC", false);
			hreTable.updateInt("ROLE_NUM", roleNumber);
			hreTable.updateInt("SEQUENCE", 1);
			hreTable.updateLong("MEMO_RPID", null_RPID);
			hreTable.updateLong("ASSOC_SENTENCE_RPID", null_RPID);
		//Insert row
			hreTable.insertRow();
		// End transaction
			   updateTableData("COMMIT", dataBaseIndex);
		} catch (SQLException sqle) {
			// Roll back transaction
			   updateTableData("ROLLBACK", dataBaseIndex);
			if (HGlobal.DEBUG) {
				System.out.println("HBPersonHandler - addToT451_EVNT_ASSOC - error: "
						+ sqle.getMessage());
			}
			sqle.printStackTrace();
			throw new HBException("HBPersonHandler - addToT451_EVNT_ASSOC - error: "
					+ sqle.getMessage());
		}
	}

/**
 * public long updateAssocTableRow(long assocTablePID, int roleNumber, long personPID) throws HBException
 * @param assocTablePID
 * @param roleNumber
 * @param personNamePID
 * @return
 * @throws HBException
 */
	public long updateAssocTableRow(long assocTablePID, int roleNumber) throws HBException {
		ResultSet assocTableRS = null;
		String selectString;
		selectString = setSelectSQL("*", eventAssocTable, "PID = " + assocTablePID);
		assocTableRS = requestTableData(selectString, dataBaseIndex);
		updateT451_EVNT_ASSOC(assocTableRS, roleNumber, 1);

	// Reset Update event
		//pointOpenProject.getWhereWhenHandler().resetUpdateEvent(pointOpenProject);
		return assocTablePID;
	}

/**
 * addToT405_PARENT_RELATION(ResultSet hreTable)
 * @param rowPID
 * @param hreTable
 * @throws HCException
 */
	private void updateT451_EVNT_ASSOC(ResultSet hreTable, int roleNumber, int sequence) throws HBException {
		if (HGlobal.DEBUG) {
			System.out.println("Start - updateT451_EVNT_ASSOC Role: " + roleNumber);
		}
		// Start transaction
		   updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
		try {
		// moves cursor to the insert row
			hreTable.first();

			hreTable.updateInt("ROLE_NUM", roleNumber);
			hreTable.updateInt("SEQUENCE", sequence);

		//Insert row
			hreTable.updateRow();

		// End transaction
		   updateTableData("COMMIT", dataBaseIndex);

		} catch (SQLException sqle) {
			// Roll back transaction
			   updateTableData("ROLLBACK", dataBaseIndex);
			if (HGlobal.DEBUG) {
				System.out.println("HBPersonHandler - updateT451_EVNT_ASSOC - error: "
						+ sqle.getMessage());
			}
			sqle.printStackTrace();
			throw new HBException("HBPersonHandler - updateT451_EVNT_ASSOC - error: "
					+ sqle.getMessage());
		}
	}

/**
 * createNewEvent()
 * @throws HBException
 */
	public long createNewEvent(int selectedEventType, int selectedRoleType) throws HBException {
		//long newEventPID;
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		pointCreateEventRecord = new CreateEventRecord(pointDBlayer, dataBaseIndex, pointOpenProject);
	// Temp solution that need to be upgradeg for marriage group = 6
		if (selectedEventType != 1004) {
			pointCreateEventRecord.assocPersonRPID = pointOpenProject.getSelectedPersonPID();
		} else {
			pointCreateEventRecord.assocPersonRPID = null_RPID;
		}
		//newEventPID = pointCreateEventRecord.createEventRecord(selectedEventType, selectedRoleType, eventLocationChanges,
		//										  eventHdatePID, sortHdatePID, nextHREMemoPID);

		eventTablePID = pointCreateEventRecord.createEventRecord(selectedEventType, selectedRoleType, eventLocationChanges,
				  eventHdatePID, sortHdatePID, nextHREMemoPID);

		locationNamePID = pointCreateEventRecord.getLocationNameRecordPID();

		if (selectedEventType == birthEventType)
			updateParentTableEvnt(pointOpenProject.getSelectedPersonPID(), eventTablePID );

	// Reload Result Set for person and names
		pointOpenProject.reloadT401Persons();
		pointOpenProject.reloadT402Names();

		return eventTablePID;
	}

/**
 * updateParentTableEvnt(long electedPersonPID,long newEventPID )
 * @param electedPersonPID
 * @param newEventPID
 * @throws HBException
 */
	private void updateParentTableEvnt(long selectedPersonPID,long newEventPID ) throws HBException {
		ResultSet parentTableRS = null;
		String selectString;
		long eventRPID;
		selectString = setSelectSQL("*", personParentTable, "PERSON_RPID = " + selectedPersonPID);
		parentTableRS = requestTableData(selectString, dataBaseIndex);
	//Start transaction
		updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
		try {
			parentTableRS.beforeFirst();
			while (parentTableRS.next()) {
				eventRPID = parentTableRS.getLong("EVNT_RPID");
				if (eventRPID == null_RPID)
					parentTableRS.updateLong("EVNT_RPID", newEventPID);
				parentTableRS.updateRow();
			}
	//End transaction
			updateTableData("COMMIT", dataBaseIndex);
		} catch (SQLException sqle) {
	//Roll back transaction
			updateTableData("ROLLBACK", dataBaseIndex);
			System.out.println(" updateParentTableEvntTable error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" updateParentTableEvnt error: " + sqle.getMessage());
		}
	}

/**
 * public void updateEventPartnerTable(long partnerTablePID, long newEventRecordPID)
 * @param partnerTablePID
 * @param newEventRecordPID
 * @throws HBException
 */
	public void updateEventPartnerTable(long partnerTablePID, long newEventRecordPID) throws HBException {
		ResultSet partnerTableRS = null;
		String selectString;
		selectString = setSelectSQL("*", personPartnerTable, "PID = " + partnerTablePID);
		partnerTableRS = requestTableData(selectString, dataBaseIndex);
	//Start transaction
		updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
		try {
			partnerTableRS.first();
			partnerTableRS.updateLong("EVNT_RPID", newEventRecordPID);
			partnerTableRS.updateRow();
	//End transaction
			updateTableData("COMMIT", dataBaseIndex);
		} catch (SQLException sqle) {
	//Roll back transaction
			updateTableData("ROLLBACK", dataBaseIndex);
			System.out.println(" updateEventPartnerTable error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" updateEventPartnerTable error: " + sqle.getMessage());
		}
	}
} // End class EditEventRecord