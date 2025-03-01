package hre.bila;
/************************************************************************************************
 * Class HBEventRoleManager extends BusinessLayer
 * Processes data for Event Definition and Roles in User GUI
 * Receives requests from User GUI to action methods
 * Sends requests to database over Database Layer API
 ************************************************************************************************
 * v0.03.0032 2025-01-11 Rearranged processing of event and roles (N. Tolleshaug)
 *			  2025-01-17 Imlement add or edit event type (N. Tolleshaug)
 *			  2025-01-22 Fixed initiate error with databaseindex = 0 (N. Tolleshaug)
 *			  2025-01-30 Added processing of event hints (N. Tolleshaug)
 *			  2025-02-03 Corrected group number for census (N. Tolleshaug)
 *			  2025-02-24 Modified listEventRoles and dataEventRoles for RS empty (N. Tolleshaug)
 ************************************************************************************************/
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

//import javax.swing.JRadioButton;

//import hre.dbla.HDDatabaseLayer;
import hre.gui.HG0507LocationSelect;
import hre.gui.HG0551DefineEvent;
import hre.gui.HG0552ManageEvent;
import hre.gui.HGlobal;
import hre.nls.HG0552Msgs;

/**
 * Class HBEventRoleManager
 * @author Nils Tolleshaug
 * @version v0.01.0032
 * @since 2025-01-02
 */
public class HBEventRoleManager extends HBBusinessLayer {
	
	long null_RPID  = 1999999999999999L;
	long proOffset  = 1000000000000000L;
	private static int marrGroup = 6, divorceGroup = 7;
	int dataBaseIndex;
	
	HG0551DefineEvent pointDefineEvent;
	HBProjectOpenData pointOpenProject;
	public HG0507LocationSelect locationScreen = null;
	public EditEventRecord pointEditEventRecord = null;
	HG0552ManageEvent pointManageEvent = null;
	public ManageLocationNameData pointManageLocationData;
	
	String[] eventTypeList;
	int[] eventTypeNumber;
	String[] eventRoleList;
	int[] eventRoleType;
	Object[][] eventRoleData;
	
	Object[] eventTypeTransfer = new Object[10];
	Object[] eventRoleTransfer = new Object[10];
	
	String[] eventGroupNames = new String[14];
	int[] eventGroupNumber = new int[14];
	
	String selectString, lang_code;
	String eventTypeName = " Event";
	String eventRoleName = " Role"; 
	int selectedEventType;
	
	
	ResultSet T460_Event_Defs;
	ResultSet T461_Event_Role;
	ResultSet T460_Events;
	long newT460PID, newT461PID, roleSentencePID;
	boolean isActive = false, keyRole = false;
	
	String abbrev, pasttense, eventHint, gedComTag, roleSex;
	int newEventNumber, roleNumber, maxYear, minYear, roleMinAge, roleMaxAge, eventGroup, eventRoleSequence;
	
	public void setSelectedLanguage(String selectedLanguage) {
		//System.out.println(" Set selected lang code: " + selectedLanguage);
		lang_code = selectedLanguage;
	}
	
	public String[] getEventTypeList(int eventGroup) throws HBException {
		listEventTypes(eventGroup);
		return eventTypeList;
	}
	
	public int[] getEventTypes() {
		return eventTypeNumber;
	}
	
	public String[] getEventRoleList(int selectedEventType, String selectRoles) throws HBException {
		return getRolesForEvent(selectedEventType, selectRoles);
	}
	
	public String[] getRolesForEvent(int eventType, String selectRoles) throws HBException {
		listEventRoles(eventType, selectRoles);
		return eventRoleList;
	}
	
	public int[] getEventRoleTypes() {
		return eventRoleType;
	}	

	public Object[][] getRolesDataForEvent(int eventType, String selectRoles) throws HBException {
		dataEventRoles(eventType);
		return eventRoleData;
	}
	
	public int getEventType() {
		return newEventNumber;
	}
	
	public Object[] getEventTypeTransfer(int eventTypeNumber) throws HBException {		
		collectEventTypeData(eventTypeNumber, T460_Event_Defs);	
		eventTypeTransfer[0] = eventTypeName;
		eventTypeTransfer[1] = newEventNumber;
		eventTypeTransfer[2] = eventGroup;
		eventTypeTransfer[3] = gedComTag;
		eventTypeTransfer[4] = minYear;
		eventTypeTransfer[5] = maxYear;
		eventTypeTransfer[6] = abbrev;
		eventTypeTransfer[7] = pasttense;
		eventTypeTransfer[8] = eventHint;
		return eventTypeTransfer;
	}
	
/**
 * public Object[] getEventRoleTransfer(int eventType, int roleNumber)	
 * @param eventType
 * @param roleNumber
 * @return
 */	
	public Object[] getEventRoleTransfer(int eventType, int roleNumber) {
		collectEventRoleData(eventType, roleNumber, T461_Event_Role);	
		eventRoleTransfer[0] = roleNumber;
		eventRoleTransfer[1] = eventRoleSequence;
		eventRoleTransfer[2] = eventRoleName;
		eventRoleTransfer[3] = roleSex;
		eventRoleTransfer[4] = roleMinAge;
		eventRoleTransfer[5] = roleMaxAge;
		eventRoleTransfer[6] = roleSentencePID;
		eventRoleTransfer[7] = keyRole;
		return eventRoleTransfer;
	}
	
/**
 * public void saveEventTypeData(Object[] eventTypeTransfer)
 * @param eventTypeTransfer
 */
	
	public void saveEventTypeData(Object[] eventTypeTransfer) {
		//eventTypeName = (String) eventTypeData[0];
		//newEventNumber = (int) eventTypeData[1];
		eventGroup = (int) eventTypeTransfer[2];
		gedComTag = (String) eventTypeTransfer[3];
		minYear = (int) eventTypeTransfer[4];
		maxYear = (int) eventTypeTransfer[5];
		abbrev = (String) eventTypeTransfer[6];
		pasttense = (String) eventTypeTransfer[7];
		eventHint = (String) eventTypeTransfer[8];
	}
	
/**
 * public void saveEventRoleData(Object[] eventRoleTransfer)	
 * @param eventRoleTransfer
 */
	public void saveEventRoleData(Object[] eventRoleTransfer) {
		roleNumber = (int) eventRoleTransfer[0];
		eventRoleSequence = (int) eventRoleTransfer[1];
		eventRoleName = (String) eventRoleTransfer[2];
		roleSex = (String) eventRoleTransfer[3];
		roleMinAge = (int) eventRoleTransfer[4];
		roleMaxAge = (int) eventRoleTransfer[5];
		if (eventRoleTransfer[6] != null) 
		roleSentencePID = (long) eventRoleTransfer[6];
		keyRole = (boolean) eventRoleTransfer[7];
	}

/**
 * private void setEventGroups()
 * NOTE the standard TMG event group numbers are:
 *	1 - Name related
 *	2 - Father-relationship pseudo events
 *	3 - Mother-relationship pseudo events
 *	4 - Birth related (6 entries + 2 LDS + user adds)
 *	5 - Death (1 entry + users adds)
 *	6 - Marriage related (6 entries + 1 LDS + user adds)
 *	7 - Divorce related (2 entries + user adds)
 *	8 - History
 *	9 - Burial (2 entries + user adds)
 *	10 - Address
 *	12- Parent-relationship pseudo events
 *	99 - Other
 * The HRE Event Preload adds these new numbers (and some group-99 entries are moved to):
 *   11 = Census related
 *   14 = Emigration related
 *   15 = Military related
 *   (plus 12 standard TMG events are allocated to more relevant groups),
 *   and numbers 16-20 are defined as usable for 5 more 'user-defined' groups
 */
	private void setEventGroups() {	
		eventGroupNames[0] = HG0552Msgs.Text_17;		// Birth
		eventGroupNumber[0] = 4;
		eventGroupNames[1] = HG0552Msgs.Text_18;		// Marriage
		eventGroupNumber[1] = 6;
		eventGroupNames[2] = HG0552Msgs.Text_19;		// Death
		eventGroupNumber[2] = 5;
		eventGroupNames[3] = HG0552Msgs.Text_20;		// Burial
		eventGroupNumber[3] = 9;
		eventGroupNames[4] = HG0552Msgs.Text_21;		// History
		eventGroupNumber[4] = 8;
		eventGroupNames[5] = HG0552Msgs.Text_22;		// Address
		eventGroupNumber[5] = 10;

	
	// User defined event group names
		eventGroupNames[6] = HG0552Msgs.Text_26;		// Census
		eventGroupNumber[6] = 11;
		eventGroupNames[7] = HG0552Msgs.Text_27;		// Travel
		eventGroupNumber[7] = 14;
		eventGroupNames[8] = HG0552Msgs.Text_28;		// Military
		eventGroupNumber[8] = 15;
		eventGroupNames[9] = HG0552Msgs.Text_29;		// Your Event Group 1
		eventGroupNumber[9] = 16;
		eventGroupNames[10] = HG0552Msgs.Text_30;		// Your Event Group 2
		eventGroupNumber[10] = 17;
		eventGroupNames[11] = HG0552Msgs.Text_31;		// Your Event Group 3
		eventGroupNumber[11] = 18;
		eventGroupNames[12] = HG0552Msgs.Text_32;		// Your Event Group 4
		eventGroupNumber[12] = 19;
		eventGroupNames[13] = HG0552Msgs.Text_34;		// Other
		eventGroupNumber[13] = 99;
	}
	
	public String[] geteventGroupNames() {
		return eventGroupNames;
	}
	
	public int[] geteventGroupNumbers() {
		return eventGroupNumber;
	}	
	
/**
 * Constructor HBEventRoleManager
 */
	public HBEventRoleManager(HBProjectOpenData pointOpenProject) {
		this.pointOpenProject = pointOpenProject;
		if (pointOpenProject != null) {
			pointDBlayer = pointOpenProject.pointProjectHandler.pointDBlayer;
			dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		} else System.out.println(" Error! HBEventRoleManager -- pointOpenProject == null");
		setEventGroups();
	}	
		
/**
 * activateDefineNewEvent()
 * @param pointOpenProject
 * @return
 */
	public HG0551DefineEvent activateDefineNewEventType() {
		int eventGroup = 0;
		try {
			lang_code = HGlobal.dataLanguage;
			listEventTypes(eventGroup);
			pointDefineEvent = new HG0551DefineEvent(this, pointOpenProject, true);
			return pointDefineEvent;
		} catch (HBException hbe) {
			System.out.println(" HBEventRoleManager - activateDefineEvent error: " + hbe.getMessage());
			hbe.printStackTrace();
		} 
		return null;
	}
	
/**
 * activateDefineNewEvent()
 * @param pointOpenProject
 * @return
 */
	public HG0551DefineEvent activateEditEventType(int selectedEventType) {
		int eventGroup = 0;
		try {
			lang_code = HGlobal.dataLanguage;
			this.selectedEventType = selectedEventType;
			listEventTypes(eventGroup);
			pointDefineEvent = new HG0551DefineEvent(this, pointOpenProject, false, selectedEventType);
			return pointDefineEvent;
		} catch (HBException hbe) {
			System.out.println(" HBEventRoleManager - activateDefineEvent error: " + hbe.getMessage());
			hbe.printStackTrace();
		} 
		return null;
	}
	
/**
 * listEventTypes(int eventGroup)
 * @param eventGroup
 * @throws HBException
 */
	private void listEventTypes(int eventGroup) throws HBException {
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		ResultSet eventListRS = pointLibraryResultSet.getEventTypeList(eventGroup, lang_code, dataBaseIndex);
		try {
			eventListRS.last();
			int nrOfRows = eventListRS.getRow();
			if (HGlobal.DEBUG) 
				System.out.println(" Event #types: " + nrOfRows);
			
			eventTypeList = new String[nrOfRows];
			eventTypeNumber = new int[nrOfRows];
			int index = 0;
			eventListRS.beforeFirst();
			while (eventListRS.next()) {
				String eventTypeName = eventListRS.getString("EVNT_NAME").trim();
				int eventTypeNr = eventListRS.getInt("EVNT_TYPE");
				if (HGlobal.DEBUG) {
					System.out.println(" Event type: " + index + " - " + eventTypeName + "/" + eventTypeNr);
				}
				eventTypeList[index] = eventListRS.getString("EVNT_NAME").trim();
				eventTypeNumber[index] = eventListRS.getInt("EVNT_TYPE");
				index++;
			}
		// Now sort the eventTypeList alpabetically, keeping eventTypeNumber in sync.
			index = eventTypeList.length;
			for(int i = 0; i <index-1; i++) {
				for (int j = i+1; j < eventTypeList.length; j++) {
					// compares each elements of the array to all the remaining elements
					if(eventTypeList[i].compareTo(eventTypeList[j]) > 0) {
						// swap eventTypeList array elements
						String tempStr = eventTypeList[i];
						eventTypeList[i] = eventTypeList[j];
						eventTypeList[j] = tempStr;
						// swap eventTypeNumber array to match
						int tempInt = eventTypeNumber[i];
						eventTypeNumber[i] = eventTypeNumber[j];
						eventTypeNumber[j] = tempInt;
					}
				}
			}
		} catch (SQLException sqle) {
			System.out.println(" HBEventRoleManager updateEventTypes: " + sqle.getMessage());
			sqle.printStackTrace();
		}
	}

/**
 * listEventRoles(int eventType, String selectRoles)
 * @param eventType
 * @throws HBException
 */
	private void listEventRoles(int eventType, String selectRoles) throws HBException {
		int nrOfRows;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		ResultSet eventRoles = pointLibraryResultSet.getRoleNameList(eventType, selectRoles, lang_code, dataBaseIndex);
		try {
			if (isResultSetEmpty(eventRoles))
				nrOfRows = 0;
			else {
				eventRoles.last();
				nrOfRows = eventRoles.getRow();
			}
			if (nrOfRows == 0) {
					if (HGlobal.DEBUG) {
						System.out.println(" No roles found for eventtype: " + eventType + " Select: " + selectRoles);
					}
					eventRoleList = new String[1];
					eventRoleType = new int[1];
					eventRoleList[0] = "Principal";
					eventRoleType[0] = 1;
					return;
			}
			eventRoleList = new String[nrOfRows];
			eventRoleType = new int[nrOfRows];
			int index = 0;
			eventRoles.beforeFirst();
			while (eventRoles.next()) {
				eventRoleList[index] = eventRoles.getString("EVNT_ROLE_NAME").trim();
				eventRoleType[index] = eventRoles.getInt("EVNT_ROLE_NUM");
				index++;
			}
		} catch (SQLException sqle) {
			System.out.println(" HBEventRoleManager listEventRoles: " + sqle.getMessage());
			throw new HBException(" HBEventRoleManager listEventRoles: " + sqle.getMessage());
		}
	}
	
/**
 * dataEventRoles(int eventType, String selectRoles)
 * @param eventType
 * @return eventRoleData
 * @throws HBException
 */
	private void dataEventRoles(int eventType) throws HBException {
		int nrOfRows;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		ResultSet eventRoles = pointLibraryResultSet.getRoleNameList(eventType, "", dataBaseIndex);
		try {
			if (isResultSetEmpty(eventRoles))
				nrOfRows = 0;
			else {
				eventRoles.last();
				nrOfRows = eventRoles.getRow();
			}
			// If no roles for the event, construct a dummy return object
			if (nrOfRows == 0) {
					if (HGlobal.DEBUG) {
						System.out.println(" No roles found for Event type: " + eventType);
					}
					eventRoleData = new Object[1][3];
					eventRoleData[0][0] = "Principal";
					eventRoleData[0][1] = 1;
					eventRoleData[0][2] = 1;
					return;
			}
			// Otherwise build the Role data - Name, Number, Sequence
			eventRoleData = new Object[nrOfRows][3];
			int index = 0;
			eventRoles.beforeFirst();
			while (eventRoles.next()) {
				eventRoleData[index][0] = eventRoles.getString("EVNT_ROLE_NAME").trim();
				eventRoleData[index][1] = (int)eventRoles.getInt("EVNT_ROLE_NUM");
				eventRoleData[index][2] = (int)eventRoles.getInt("EVNT_ROLE_SEQ");
				index++;
			}
			// Sort the role data on the sequence field (col 2) before returning it
			Arrays.sort(eventRoleData, (o1, o2) -> Integer.compare((Integer) o1[2], (Integer) o2[2]));
		} catch (SQLException sqle) {
			System.out.println(" HBEventRoleManager dataEventRoles: " + sqle.getMessage());
			throw new HBException(" HBEventRoleManager dataEventRoles: " + sqle.getMessage());
		}
	}
	
/**
 * public void prepareNewEventType() throws HBException	
 * @throws HBException
 */
	public void prepareNewEventType(boolean addNewEventType) throws HBException {	
		//lang_code = HGlobal.dataLanguage;
		selectString = setSelectSQL("*", eventDefnTable, "");
		T460_Event_Defs = requestTableData(selectString, dataBaseIndex);
		selectString = setSelectSQL("*", eventRoleTable, "");
		T461_Event_Role = requestTableData(selectString, dataBaseIndex);
		newT460PID = lastRowPID(eventDefnTable, pointOpenProject) + 1;
		newT461PID = lastRowPID(eventRoleTable, pointOpenProject) + 1;
		if (addNewEventType)
		  newEventNumber = findNewLargestNumber(T460_Event_Defs, "EVNT_TYPE");
		else newEventNumber = selectedEventType;
	}
	
/**
 * 	addNewEventType()
 * @throws HBException 
 */
	public void addNewEventType(String newEventName, int selectedgroupNumber) throws HBException {	
		isActive = false;
		eventGroup = selectedgroupNumber;
		eventTypeName = newEventName;
		abbrev = " abr. ";
		eventRoleSequence = 1;
		pasttense = " Sentence";
		eventHint = "Type in event hint text"; 
		addToT460_EVNT_DEFS(newT460PID, T460_Event_Defs); 
	}

/**
 * findNewLargestNumber(ResultSet hreTable, String field)
 * @param hreTable
 * @param field
 * @return
 */
	private int findNewLargestNumber(ResultSet hreTable, String field) {
		return findNewLargestNumber(hreTable, field, null);
		
	}
	private int findNewLargestNumber(ResultSet hreTable, String field, String event) {
		int newIndex = 0, foundIndex, identType;
		try {
			hreTable.beforeFirst();
			while (hreTable.next()) {
				if (event == null) {
					foundIndex = hreTable.getInt(field);
					if (foundIndex > newIndex ) newIndex  = foundIndex;
				} else {
					identType = hreTable.getInt(event);
					if (newEventNumber == identType) {
						foundIndex = hreTable.getInt(field);
						if (foundIndex > newIndex ) newIndex  = foundIndex;
					}
				}
			}
			return newIndex + 1 ;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;	
	}
	
/**
 * public void addNewEventRole(String newRoleName) throws HBException	
 * @param newRoleName
 * @throws HBException
 * @throws  
 */
	public void addNewEventRole(String newRoleName) throws HBException {
		//lang_code = HGlobal.dataLanguage;
		eventRoleName = newRoleName;
		selectString = setSelectSQL("*", eventRoleTable, "");
		T461_Event_Role = requestTableData(selectString, dataBaseIndex);
		try {
			if (!isResultSetEmpty(T461_Event_Role))
				roleNumber = findNewLargestNumber(T461_Event_Role, "EVNT_ROLE_NUM","EVNT_TYPE");
			else roleNumber = 1;
			newT461PID = lastRowPID(eventRoleTable, pointOpenProject) + 1;
			addToT461_EVNT_ROLE(newT461PID, T461_Event_Role);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
/**
 * editEventType(String newEventName)	
 * @param newEventName
 * @throws HBException
 */
	public void editEventType(String newEventName, int eventTypeNumber, 
								int selectedGroupNumber, Object[] eventTypeData) throws HBException {
		isActive = false;
		abbrev = " abr. ";
		minYear = (int) eventTypeData[4];
		maxYear =  (int) eventTypeData[5];
		eventGroup = selectedGroupNumber;
		eventTypeName = newEventName;
		newEventNumber = selectedEventType;
		eventRoleSequence = 1;
		//pasttense = " Sentence";
		//reminder = "Reminder"; 
		gedComTag = (String) eventTypeData[3];
		updateT460_EVNT_DEFS(eventTypeNumber, T460_Event_Defs);
	}
	
	public void editEventRole(int eventTypeNumber, int eventRoleNumber, Object[] eventRoleData) throws HBException {	
		roleNumber = (int) eventRoleData[0]; 
		newEventNumber = (int) eventRoleData[1]; //newEventNumber;
		eventRoleName = (String) eventRoleData[2]; //
		roleSex = (String) eventRoleData[3];	
		roleMinAge = (int) eventRoleData[4];	
		roleMaxAge = (int) eventRoleData[5];
		// eventRoleData[6]; 
		//eventRoleData[7] = false; 
		eventRoleSequence = 1;
		//System.out.println(" editEventRole " + eventTypeNumber + "/" + eventRoleNumber);
		updateT461_EVNT_ROLE(eventTypeNumber, eventRoleNumber, T461_Event_Role);
	}
	
	
/**
 * protected void addToT460_EVNT_DEFS(long eventDefnPID, ResultSet hreTable) throws HBException
 * @param eventDefnPID
 * @param hreTable
 * @throws HBException
 */
	protected void addToT460_EVNT_DEFS(long eventDefnPID, ResultSet hreTable) throws HBException {
		if (HGlobal.DEBUG)
				System.out.println("** addTo T460_EVENT_DEFN PID: " + eventDefnPID);

	// Abbrev too long:
		if (abbrev.length() > 10) {
			System.out.println(" Long T460_EVNT_DEFN - EVNT_ABBREV: " + abbrev + " - Length: "
					+ abbrev.length() + "/10");
			abbrev = abbrev.substring(0,10);
		}
		if (eventHint.length() > 2000) {
			System.out.println(" Long T460_EVNT_DEFN - EVNT_HINT - Length: "
					+ eventHint.length() + "/2000");
			eventHint = eventHint.substring(0,2000);
		}

		try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();
		// update row
			hreTable.updateLong("PID", eventDefnPID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateBoolean("IS_SYSTEM", false);		
			hreTable.updateBoolean("IS_ACTIVE", isActive);
			hreTable.updateInt("EVNT_TYPE", newEventNumber);
			hreTable.updateInt("EVNT_GROUP", eventGroup);
			if (eventGroup == marrGroup || eventGroup == divorceGroup) hreTable.updateInt("EVNT_KEY_ASSOC_MIN", 2);
			else hreTable.updateInt("EVNT_KEY_ASSOC_MIN", 1);
			hreTable.updateInt("MIN_YEAR", minYear);
			hreTable.updateInt("MAX_YEAR", maxYear);
			hreTable.updateString("GEDCOM", gedComTag);
			hreTable.updateString("LANG_CODE", lang_code);
			hreTable.updateString("EVNT_NAME", eventTypeName);
			hreTable.updateString("EVNT_ABBREV", abbrev);
			hreTable.updateString("EVNT_PAST", pasttense);
			hreTable.updateString("EVNT_HINT", eventHint);
		//Insert row
			hreTable.insertRow();
			hreTable.close();

		} catch (SQLException sqle) {
			System.out.println(" HBEventRoleManager - addTo table T460_EVNT_DEFN - error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" HBEventRoleManager - addTo table T460_EVNT_DEFN - error: " + sqle.getMessage());
		}
	}
	
			
/**
 * addToT461_EVENT_ROLE_NAME
 * @param primaryPID
 * @param hreTable
 * @throws HCException
 */
	protected void addToT461_EVNT_ROLE(long primaryPID, ResultSet hreTable) throws HBException {
		if (HGlobal.DEBUG)
			System.out.println("** addTo T461_EVNT_ROLE PID: " + primaryPID);

		if (eventRoleName.length() > 40) {
			System.out.println(" Long T461_EVNT_ROLE - EVNT_ROLE_NAME - Length: "
								+ eventRoleName.length() + "/40");
			eventRoleName = eventRoleName.substring(0,40);
		}
		try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();
		// update
			hreTable.updateLong("PID", primaryPID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateBoolean("IS_SYSTEM", false);
			hreTable.updateString("LANG_CODE", lang_code);
			hreTable.updateInt("EVNT_TYPE", newEventNumber);
			hreTable.updateInt("EVNT_ROLE_NUM", roleNumber);
			hreTable.updateInt("EVNT_ROLE_SEQ", eventRoleSequence); // ************Need update
			hreTable.updateString("EVNT_ROLE_NAME", eventRoleName);
			hreTable.updateString("EVNT_ROLE_SEX", roleSex);
			hreTable.updateInt("EVNT_ROLE_MINAGE", roleMinAge);
			hreTable.updateInt("EVNT_ROLE_MAXAGE", roleMaxAge); 
			hreTable.updateLong("ROLE_SENTENCE_RPID", null_RPID);
			hreTable.updateBoolean("KEY_ASSOC", keyRole);  // New in v22c - 12.12.2024 *****************
		//Insert row
			hreTable.insertRow();

		} catch (SQLException sqle) {
			System.out.println("Not able to update table - T461_EVNT_ROLE" + " Event role name: " + eventRoleName);
			sqle.printStackTrace();
			throw new HBException(" HBEventRoleManager - addTo table T461_EVNT_ROLE - error: "
					+ sqle.getMessage());
		}
	}

/**
 * protected void updateT460_EVNT_DEFS(int eventNumber, ResultSet hreTable) throws HBException
 * @param eventNumber
 * @param hreTable
 * @throws HBException
 */
	protected void updateT460_EVNT_DEFS(int eventNumber, ResultSet hreTable) throws HBException {
		try {
			hreTable.beforeFirst();
			while (hreTable.next()) {
				if (hreTable.getInt("EVNT_TYPE") == eventNumber 
						&& hreTable.getString("LANG_CODE").trim().equals(lang_code)){ 
					hreTable.updateString("EVNT_NAME", eventTypeName);
					hreTable.updateInt("EVNT_GROUP", eventGroup);
					hreTable.updateString("GEDCOM", gedComTag);
					hreTable.updateInt("MIN_YEAR", minYear);
					hreTable.updateInt("MAX_YEAR", maxYear);
					hreTable.updateString("EVNT_HINT", eventHint);
					hreTable.updateRow();
					break;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
/**
 * private void collectEventTypeData(int eventNumber, ResultSet hreTable)
 * @param eventNumber
 * @param hreTable
 * @throws HBException 
 */
	private void collectEventTypeData(int eventNumber, ResultSet hreTable) throws HBException {
		try {
			hreTable.beforeFirst();
			while (hreTable.next()) {
				if (hreTable.getInt("EVNT_TYPE") == eventNumber 
						&& hreTable.getString("LANG_CODE").trim().equals(lang_code)) {	
					eventTypeName = hreTable.getString("EVNT_NAME").trim();
					newEventNumber = hreTable.getInt("EVNT_TYPE");
					eventGroup = hreTable.getInt("EVNT_GROUP");
					gedComTag = hreTable.getString("GEDCOM").trim();
					maxYear = hreTable.getInt("MAX_YEAR");
					minYear = hreTable.getInt("MIN_YEAR");
					abbrev = hreTable.getString("EVNT_ABBREV").trim();
					pasttense = hreTable.getString("EVNT_PAST").trim();
					eventHint = hreTable.getString("EVNT_HINT").trim();	
					//System.out.println(" CollectEventTypes: Event Hints: " + eventHint);
					return;
				} 
			}
			throw new HBException("ERR1 - Eventype have no translation for " + lang_code);
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" SQL error! " + sqle.getMessage());
		}
	}

/**
 * protected void editT461_EVNT_ROLE(int eventNumber, int roleNumber, ResultSet hreTable) 
 * @param eventNumber
 * @param roleNumber
 * @param hreTable
 * @throws HBException
 */
	protected void updateT461_EVNT_ROLE(int eventNumber, int roleNumber, ResultSet hreTable) throws HBException {
		try {
			hreTable.beforeFirst();
			while (hreTable.next()) {
				if (hreTable.getInt("EVNT_TYPE") == eventNumber && hreTable.getInt("EVNT_ROLE_NUM") == roleNumber) {
					hreTable.updateString("LANG_CODE", lang_code);
					hreTable.updateInt("EVNT_ROLE_NUM", roleNumber);
					hreTable.updateInt("EVNT_ROLE_SEQ", eventRoleSequence); // ************Need update
					hreTable.updateString("EVNT_ROLE_NAME", eventRoleName.trim());
					hreTable.updateString("EVNT_ROLE_SEX", roleSex);
					hreTable.updateInt("EVNT_ROLE_MINAGE", roleMinAge);
					hreTable.updateInt("EVNT_ROLE_MAXAGE", roleMaxAge);
					hreTable.updateLong("ROLE_SENTENCE_RPID", null_RPID);
					hreTable.updateBoolean("KEY_ASSOC", false);
					hreTable.updateRow();
					//System.out.println(" updateT461_EVNT_ROLE " + eventNumber + "/" + roleNumber);
					break;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

/**
 * private void collectEventRoleData(int eventNumber, int roleNumber, ResultSet hreTable)
 * @param eventNumber
 * @param roleNumber
 * @param hreTable
 */
	private void collectEventRoleData(int eventNumber, int roleNumber, ResultSet hreTable) {
		try {
			hreTable.beforeFirst();
			while (hreTable.next()) {
				if (hreTable.getInt("EVNT_TYPE") == eventNumber && hreTable.getInt("EVNT_ROLE_NUM") == roleNumber) {	
					eventRoleSequence = hreTable.getInt("EVNT_ROLE_SEQ");
					eventRoleName = hreTable.getString("EVNT_ROLE_NAME").trim();
					roleSex = hreTable.getString("EVNT_ROLE_SEX").trim();
					roleMinAge = hreTable.getInt("EVNT_ROLE_MINAGE");
					roleMaxAge = hreTable.getInt("EVNT_ROLE_MAXAGE");
					roleSentencePID = hreTable.getLong("ROLE_SENTENCE_RPID");
					keyRole = hreTable.getBoolean("KEY_ASSOC");	
					break;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

/**
 * public int deleteEventType(int eventTypeNumber) throws HBException
 * @param eventTypeNumber
 * @return
 * @throws HBException
 */
	public int deleteEventType(int eventTypeNumber) throws HBException {
		//lang_code = HGlobal.dataLanguage;
		selectString = setSelectSQL("*", eventTable, "EVNT_TYPE = " + eventTypeNumber);
		T460_Events = requestTableData(selectString, dataBaseIndex);
		try {
			if (!isResultSetEmpty(T460_Events)) {
				System.out.println(" -> Event type " + eventTypeNumber + " in use! ");
				return 1;
			}
			selectString = setSelectSQL("*", eventDefnTable, "EVNT_TYPE = " + eventTypeNumber + " AND LANG_CODE = '" + lang_code + "'");
			T460_Event_Defs = requestTableData(selectString, dataBaseIndex);
			selectString = setSelectSQL("*", eventRoleTable, "EVNT_TYPE = " + eventTypeNumber + " AND LANG_CODE = '" + lang_code + "'");
			T461_Event_Role = requestTableData(selectString, dataBaseIndex);
			//T461_Event_Role.last();
			if (isResultSetEmpty(T461_Event_Role)) {
				T460_Event_Defs.first();
				T460_Event_Defs.deleteRow();
				return 0;
			} else { 
				T461_Event_Role.beforeFirst();
				while (T461_Event_Role.next()) T461_Event_Role.deleteRow();
				T460_Event_Defs.first();
				T460_Event_Defs.deleteRow();
				return 0;
			}		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
} // End HBEventRoleManager

	



