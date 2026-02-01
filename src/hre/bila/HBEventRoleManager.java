package hre.bila;
/************************************************************************************************
 * Class HBEventRoleManager extends BusinessLayer
 * Processes data for Event Definition and Roles in User GUI
 * Receives requests from User GUI to action methods
 * Sends requests to database over Database Layer API
 ************************************************************************************************
 * v0.03.0032 2025-01-11 Rearranged processing of event and roles (N. Tolleshaug)
 *			  2025-01-17 Implement add or edit event type (N. Tolleshaug)
 *			  2025-01-22 Fixed initiate error with databaseindex = 0 (N. Tolleshaug)
 *			  2025-01-30 Added processing of event hints (N. Tolleshaug)
 *			  2025-02-03 Corrected group number for census (N. Tolleshaug)
 *			  2025-02-24 Modified listEventRoles and dataEventRoles for RS empty (N. Tolleshaug)
 *			  2025-07-02 Added set language to HBEventRoleManager (N. Tolleshaug)
 *			  2025-07-31 Add eventRoleSeq and Key lists to be returned (D Ferguson)
 *			  2025-08-07 Rename various routines for greater clarity (D Ferguson)
 *			  2025-08-12 Fix T461 update creating duplicate records (D Ferguson)
 *			  2025-08-13 Add routine to update 2 Role records for move up/down actions (D Ferguson)
 *			  2025-08-14 Fix missing langcode when searching T461 RS (D Ferguson)
 *			  2026-01-01 Added code in HBEventRoleManager for select event name (N. Tolleshaug)
 *			  2026-01-07 Partially implemented add, edit and copy eventtype (N. Tolleshaug)
 *			  2026-01-13 Handling SENTENCE_SET_RPID from T461 role table (N. Tolleshaug)
 *            2026-01-20 Handling abbrev and past sentence fields (N. Tolleshaug)
 *            2026-01-28 Updated for add and copy sentence handling (N. Tolleshaug)
 *            2026-01-30 Test for suplicate names and delete roles(s) (N. Tolleshaug)
 ************************************************************************************************/

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;

//import javax.swing.JRadioButton;

//import hre.dbla.HDDatabaseLayer;
import hre.gui.HG0507LocationSelect;
import hre.gui.HG0551DefineEvent;
import hre.gui.HG0552ManageEvent;
import hre.gui.HGlobal;
import hre.nls.HG0552Msgs;
import hre.tmgjava.HCException;
/**
 * Class HBEventRoleManager
 * @author Nils Tolleshaug
 * @version v0.01.0032
 * @since 2025-01-02
 */
public class HBEventRoleManager extends HBBusinessLayer {

	long null_RPID  = 1999999999999999L;
	long proOffset  = 1000000000000000L;	
	int dataBaseIndex = -1;

	HG0551DefineEvent pointDefineEvent;
	HBProjectOpenData pointOpenProject;
	public HG0507LocationSelect locationScreen = null;
	public EditEventRecord pointEditEventRecord = null;
	HG0552ManageEvent pointManageEvent = null;
	public ManageLocationNameData pointManageLocationData;

	String[] eventTypeList;
	int[] eventTypeNumbers;
	String[] eventRoleList;
	int[] eventRoleType;
	int[] eventRoleSeq;
	long[] eventRoleSentencePID;
	boolean[] eventRoleKey;
	boolean selectedKeyRole;
	Object[][] eventRoleData;
	HashMap<Integer,String> typeNameList;

	Object[] eventTypeTransfer; //= new Object[10];
	Object[] eventRoleTransfer; //= //new Object[10];

	String[] eventGroupNames = new String[14];
	int[] eventGroupNumbers = new int[14];

	String selectString, lang_code = HGlobal.dataLanguage;
	String eventTypeName = " Event";
	String eventRoleName = " Role";
	int selectedEventType;

	ResultSet T460_Event_Defs;
	ResultSet T461_Event_Role;
	ResultSet T460_Events;
	long nextT460PID, nextT461PID, roleSentencePID;
	boolean isActive = false, roleKey = false;

	String abbrev, pasttense, eventHint, gedComTag, roleSex, roleSentece ;
	int newEventNumber, roleNumber, minAssoc, maxYear, minYear, roleMinAge, roleMaxAge, eventGroup, eventRoleSequence;

	public void setSelectedLanguage(String selectedLanguage) {
		lang_code = selectedLanguage;
		System.out.println(" Selected lang code: " + lang_code);
	}
	
	public String getEventName(int eventTypeNumber) {
		return typeNameList.get(eventTypeNumber);
	}
	
	public String[] getEventTypeList(int eventGroup) throws HBException {
		listEventTypes(eventGroup);
		return eventTypeList;
	}

	public int[] getEventTypes() {
		return eventTypeNumbers;
	}
	public String[] getEventRoleNames(int selectedEventType, String selectRoles) throws HBException {
		return getRolesForEvent(selectedEventType, selectRoles);
	}
	
	public String[] getRolesForEvent(int eventType, String selectRoles) throws HBException {
		listEventRoles(eventType, selectRoles);
		return eventRoleList;
	} 

	public int[] getEventRoleNumbers() {
		return eventRoleType;
	}

	public int[] getEventRoleSeq() {
		return eventRoleSeq;
	}

	public boolean[] getEventRoleKey() {
		return eventRoleKey;
	}
	
	public long[] getEventRoleSentencePID() {
		return eventRoleSentencePID;
	}

	public Object[][] getRolesDataForEvent(int eventType, String selectRoles) throws HBException {
		dataEventRoles(eventType);
		return eventRoleData;
	}

	public int getNewEventNumber() {
		return newEventNumber;
	}

	public Object[] getEventTypeTransfer(int eventTypeNumber) throws HBException {
		collectEventTypeData(eventTypeNumber, T460_Event_Defs);
		eventTypeTransfer = new Object[10];
		eventTypeTransfer[0] = eventTypeName;
		eventTypeTransfer[1] = newEventNumber;
		eventTypeTransfer[2] = eventGroup;
		eventTypeTransfer[3] = gedComTag;
		eventTypeTransfer[4] = minYear;
		eventTypeTransfer[5] = maxYear;
		eventTypeTransfer[6] = abbrev;
		eventTypeTransfer[7] = pasttense;
		eventTypeTransfer[8] = eventHint;
		eventTypeTransfer[9] = minAssoc;
		return eventTypeTransfer;
	}

/**
 * public Object[] getEventRoleTransfer(int eventType, int roleNumber)
 * @param eventType
 * @param roleNumber
 * @return
 * @throws HBException 
 */
	public Object[] getEventRoleTransfer(int eventType, int roleNumber) throws HBException {
		collectEventRoleData(eventType, roleNumber, T461_Event_Role);
		eventRoleTransfer = new Object[8];
		eventRoleTransfer[0] = roleNumber;
		eventRoleTransfer[1] = eventRoleSequence;
		eventRoleTransfer[2] = eventRoleName;
		eventRoleTransfer[3] = roleSex;
		eventRoleTransfer[4] = roleMinAge;
		eventRoleTransfer[5] = roleMaxAge;
		eventRoleTransfer[6] = roleSentencePID;
		eventRoleTransfer[7] = roleKey;
		return eventRoleTransfer;
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
		eventGroupNumbers[0] = 4;
		eventGroupNames[1] = HG0552Msgs.Text_18;		// Marriage
		eventGroupNumbers[1] = 6;
		eventGroupNames[2] = HG0552Msgs.Text_19;		// Death
		eventGroupNumbers[2] = 5;
		eventGroupNames[3] = HG0552Msgs.Text_20;		// Burial
		eventGroupNumbers[3] = 9;
		eventGroupNames[4] = HG0552Msgs.Text_21;		// History
		eventGroupNumbers[4] = 8;
		eventGroupNames[5] = HG0552Msgs.Text_22;		// Address
		eventGroupNumbers[5] = 10;


	// User defined event group names
		eventGroupNames[6] = HG0552Msgs.Text_26;		// Census
		eventGroupNumbers[6] = 11;
		eventGroupNames[7] = HG0552Msgs.Text_27;		// Travel
		eventGroupNumbers[7] = 14;
		eventGroupNames[8] = HG0552Msgs.Text_28;		// Military
		eventGroupNumbers[8] = 15;
		eventGroupNames[9] = HG0552Msgs.Text_29;		// Your Event Group 1
		eventGroupNumbers[9] = 16;
		eventGroupNames[10] = HG0552Msgs.Text_30;		// Your Event Group 2
		eventGroupNumbers[10] = 17;
		eventGroupNames[11] = HG0552Msgs.Text_31;		// Your Event Group 3
		eventGroupNumbers[11] = 18;
		eventGroupNames[12] = HG0552Msgs.Text_32;		// Your Event Group 4
		eventGroupNumbers[12] = 19;
		eventGroupNames[13] = HG0552Msgs.Text_34;		// Other
		eventGroupNumbers[13] = 99;
	}

	public String[] geteventGroupNames() {
		return eventGroupNames;
	}

	public int[] geteventGroupNumbers() {
		return eventGroupNumbers;
	}

/**
 * Constructor HBEventRoleManager
 */
	public HBEventRoleManager(HBProjectOpenData pointOpenProject) {
		this.pointOpenProject = pointOpenProject;
		if (pointOpenProject != null) {
			pointDBlayer = pointOpenProject.getPointDBlayer();
			dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		} else System.out.println(" Error! HBEventRoleManager -- pointOpenProject == null");
		setEventGroups();
	}

/**
 * activateDefineNewEvent() add new event
 * @param pointOpenProject
 * @return
 */
	public HG0551DefineEvent activateAddEventType() {
		int eventGroup = 0;
		try {
			listEventTypes(eventGroup);
			pointDefineEvent = new HG0551DefineEvent(this, pointOpenProject);
			return pointDefineEvent;
		} catch (HBException hbe) {
			System.out.println(" HBEventRoleManager - activateDefineEvent error: " + hbe.getMessage());
			hbe.printStackTrace();
		}
		return null;
	}

/**
 * activateDefineNewEvent() - Edit
 * @param pointOpenProject
 * @return
 */
	public HG0551DefineEvent activateEditEventType(int selectedEventType) {
		int eventGroup = 0;
		try {
			this.selectedEventType = selectedEventType;
			listEventTypes(eventGroup);
			pointDefineEvent = new HG0551DefineEvent(this, pointOpenProject, selectedEventType);
			return pointDefineEvent;
		} catch (HBException hbe) {
			System.out.println(" HBEventRoleManager - activateEditEvent error: " + hbe.getMessage());
			hbe.printStackTrace();
		}
		return null;
	}
	
/**
 * activateDefineNewEvent()- copy
 * @param pointOpenProject
 * @return
 */
	public HG0551DefineEvent activateCopyEventType(int selectedEventType) {
		int eventGroup = 0;
		try {
			this.selectedEventType = selectedEventType;
			listEventTypes(eventGroup);
			pointDefineEvent = new HG0551DefineEvent(this, pointOpenProject, "dummy", selectedEventType);
			return pointDefineEvent;
		} catch (HBException hbe) {
			System.out.println(" HBEventRoleManager - activateCopyEvent error: " + hbe.getMessage());
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
		typeNameList = new HashMap<Integer,String>(); 
		ResultSet eventListRS = pointLibraryResultSet.getEventTypeList(eventGroup, lang_code, dataBaseIndex);
		try {
			eventListRS.last();
			int nrOfRows = eventListRS.getRow();
			if (HGlobal.DEBUG)
				System.out.println(" Event #types: " + nrOfRows);

			eventTypeList = new String[nrOfRows];
			eventTypeNumbers = new int[nrOfRows];
			int index = 0;
			eventListRS.beforeFirst();
			while (eventListRS.next()) {
				String eventTypeName = eventListRS.getString("EVNT_NAME").trim();
				int eventTypeNr = eventListRS.getInt("EVNT_TYPE");
				if (HGlobal.DEBUG) 
					System.out.println(" Event type: " + index + " - " + eventTypeName + "/" + eventTypeNr);
				
				eventTypeList[index] = eventListRS.getString("EVNT_NAME").trim();
				eventTypeNumbers[index] = eventListRS.getInt("EVNT_TYPE");
				typeNameList.put(eventTypeNr, eventTypeName);
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
						int tempInt = eventTypeNumbers[i];
						eventTypeNumbers[i] = eventTypeNumbers[j];
						eventTypeNumbers[j] = tempInt;
					}
				}
			}
		} catch (SQLException sqle) {
			System.out.println(" HBEventRoleManager listEventTypes: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" HBEventRoleManager listEventTypes: " + sqle.getMessage());
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
			if (isResultSetEmpty(eventRoles)) {
				nrOfRows = 0;
			} else {
				eventRoles.last();
				nrOfRows = eventRoles.getRow();
			}
			if (nrOfRows == 0) {
					if (HGlobal.DEBUG)
							System.out.println(" No roles found for eventtype: " + eventType + " Select: " + selectRoles);
				// Setup null values to avoid empty role list being returned
					eventRoleList = new String[1];
					eventRoleType = new int[1];
					eventRoleSeq = new int[1];
					eventRoleKey = new boolean[1];
					eventRoleSentencePID = new long[1];
					eventRoleList[0] = "Dummy";
					eventRoleType[0] = 1;
					eventRoleSeq[0] = 0;
					eventRoleSentencePID[0] = null_RPID;
					eventRoleKey[0] = false;
					return;
			}
			eventRoleKey = new boolean[nrOfRows];
			eventRoleList = new String[nrOfRows];
			eventRoleType = new int[nrOfRows];
			eventRoleSeq = new int[nrOfRows];
			eventRoleSentencePID = new long[nrOfRows];
			int index = 0;
			eventRoles.beforeFirst();
			while (eventRoles.next()) {
				eventRoleList[index] = eventRoles.getString("EVNT_ROLE_NAME").trim();
				eventRoleType[index] = eventRoles.getInt("EVNT_ROLE_NUM");
				eventRoleSeq[index] = eventRoles.getInt("EVNT_ROLE_SEQ");
				eventRoleKey[index] = eventRoles.getBoolean("KEY_ASSOC");
				eventRoleSentencePID[index] = eventRoles.getLong("ROLE_SENTENCE_RPID");
				index++;
			}
		} catch (SQLException sqle) {
			System.out.println(" HBEventRoleManager listEventRoles: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" HBEventRoleManager listEventRoles: " + sqle.getMessage());
		}
	}
	
/**
 * public String getEventRoleSentence(long eventRoleSentencePID)	
 * @param eventRoleSentencePID
 * @return
 * @throws HBException
 */
	public String getEventRoleSentence(long eventRoleSentencePID) throws HBException {
		ResultSet eventSentenceRS;
		String eventRoleSentence = "NOSENTENCE";
		if (eventRoleSentencePID == null_RPID) return eventRoleSentence;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", sentenceSet, "PID = " + eventRoleSentencePID);
		eventSentenceRS = requestTableData(selectString, dataBaseIndex);
		try {
			if (isResultSetEmpty(eventSentenceRS)) return eventRoleSentence;
			eventSentenceRS.first();
			eventRoleSentence = eventSentenceRS.getString("SHORT_SENT").trim();
			return eventRoleSentence;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" HBEventRoleManager - getEvenrRoleSentence " + sqle.getMessage());
		}
	}
	
/**
 * public void updateEventRoleSentence(long eventRoleSentencePID, String roleSentence) 	
 * @param eventRoleSentencePID
 * @param roleSentence
 * @throws HBException
 */
	public void updateEventRoleSentence(long eventRoleSentencePID, String roleSentence) throws HBException {
		ResultSet eventSentenceRS;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", sentenceSet, "PID = " + eventRoleSentencePID);
		eventSentenceRS = requestTableData(selectString, dataBaseIndex);
		try {
			if (isResultSetEmpty(eventSentenceRS)) 
				throw new HBException("HBEventRoleManager - getEvenrRoleSentence - missing row in table");	
			eventSentenceRS.first();
			eventSentenceRS.updateString("SHORT_SENT", roleSentence);
			eventSentenceRS.updateRow();
			eventSentenceRS.close();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" HBEventRoleManager - getEvenrRoleSentence " + sqle.getMessage());
		}
	}
	
/**
 * public long addEventRoleSentence(String roleSentence, int etypeNumber, int roleNumber)	
 * @param roleSentence
 * @param etypeNumber
 * @param roleNumber
 * @return
 * @throws HBException
 */
	public long addEventRoleSentence(String roleSentence, int etypeNumber, int roleNumber) throws HBException {
		ResultSet eventSentenceRS;
		long nextSentenceSetPID;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		nextSentenceSetPID = lastRowPID(sentenceSet, pointOpenProject) + 1;
		selectString = setSelectSQL("*", sentenceSet, "PID = " + (nextSentenceSetPID - 1));
		eventSentenceRS = requestTableData(selectString, dataBaseIndex);
		try {
			if (isResultSetEmpty(eventSentenceRS)) 
				throw new HBException("HBEventRoleManager -addEventRoleSentence error!");
			addToT168_SENTENCE_SET(eventSentenceRS, nextSentenceSetPID, lang_code, etypeNumber,
													roleNumber, roleSentence);
			return nextSentenceSetPID;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" HBEventRoleManager - getEvenrRoleSentence " + sqle.getMessage());
		}
	}
	
/**
 *
 * @param hreTable
 * @param primaryPID
 * @param lang_code
 * @param etypeNumber
 * @param roleNumber
 * @param TMGsentence
 * @throws HCException
 * 	 T168_SENTENCE_SET format
	 PID
	 BIGINT
	 CL_COMMIT_RPID
	 BIGINT
	 IS_LONG
	 SHORT_SENT
	 LONG_SENT
	 LANG_CODE
	 EVNT_TYPE
	 EVNT_ROLE_NUM
 */

	protected void addToT168_SENTENCE_SET(ResultSet hreTable, long primaryPID, String lang_code, int etypeNumber,
											int roleNumber, String TMGsentence) throws HBException {
		try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();
		// update
			hreTable.updateLong("PID", primaryPID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateString("LANG_CODE", lang_code);
			hreTable.updateInt("EVNT_TYPE", etypeNumber);
			hreTable.updateInt("EVNT_ROLE_NUM", roleNumber);
			if (TMGsentence.length() <= 500) {
				hreTable.updateBoolean("IS_LONG", false);
				hreTable.updateString("SHORT_SENT", TMGsentence);
			} else {
				hreTable.updateBoolean("IS_LONG", true);
				//hreTable.updateClob("LONG_SENT", pointHREbase.createNClob(TMGsentence));
			}
		//Insert row
			hreTable.insertRow();

		} catch (SQLException sqle) {
			System.out.println("TMGPass_events - addToT168_SENTENCE_SET - error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException("TMGPass_events - addToT168_SENTENCE_SET - error: " + sqle.getMessage());
		}
	}

/**
 * dataEventRoles(int eventType)
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
					eventRoleData = new Object[1][4];
					eventRoleData[0][0] = "Subject";
					eventRoleData[0][1] = 1;
					eventRoleData[0][2] = 1;
					eventRoleData[0][3] = true;
					return;
			}
			// Otherwise build the Role data - Name, Number, Sequence
			eventRoleData = new Object[nrOfRows][4];
			int index = 0;
			eventRoles.beforeFirst();
			while (eventRoles.next()) {
				eventRoleData[index][0] = eventRoles.getString("EVNT_ROLE_NAME").trim();
				eventRoleData[index][1] = (int)eventRoles.getInt("EVNT_ROLE_NUM");
				eventRoleData[index][2] = (int)eventRoles.getInt("EVNT_ROLE_SEQ");
				eventRoleData[index][3] = (boolean)eventRoles.getBoolean("KEY_ASSOC");
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
	public void prepareResultSet() throws HBException {
		selectString = setSelectSQL("*", eventDefnTable, "");
		T460_Event_Defs = requestTableData(selectString, dataBaseIndex);
		selectString = setSelectSQL("*", eventRoleTable, "");
		T461_Event_Role = requestTableData(selectString, dataBaseIndex);
	}
	
/**
 * setCopyEventNumber()
 * @throws HBException
 */
	public int setNewEventNumber(boolean addEventType, boolean copyEventType) throws HBException {
		if (addEventType || copyEventType)
		  newEventNumber = findNewLargestNumber(T460_Event_Defs, "EVNT_TYPE") + 1;
		else newEventNumber = selectedEventType;
		return newEventNumber;
	}
	
/**
 * public void copyEventRolesTableRows()	
 * @throws HBException
 */
	public void copyEventRolesTableRows() throws HBException {
		selectString = setSelectSQL("*", eventRoleTable, "EVNT_TYPE = " + selectedEventType 
				+ " AND LANG_CODE = '" + lang_code + "'");
		copyEventRoleAction(false);
	}
	
/**
 * public void copyOneRoleTableRow(int copiedRuleNumber, int newRoleNumber)	
 * @param copiedRuleNumber
 * @param newRoleNumber
 * @throws HBException
 */
	public void copyOneRoleTableRow(String newEventRoleNmae,int copiedRuleNumber, int newRoleNumber, int roleSequence) throws HBException {
		selectString = setSelectSQL("*", eventRoleTable, "EVNT_TYPE = " + selectedEventType 
				+ " AND EVNT_ROLE_NUM = " + copiedRuleNumber + " AND LANG_CODE = '" + lang_code + "'");
		eventRoleName = newEventRoleNmae;
		roleNumber = newRoleNumber;
		eventRoleSequence = roleSequence;
		copyEventRoleAction(true);
	}

/**
 * public void copyEventRoleAction(boolean onlyOne, int newRoleNumber)
 * @param onlyOne
 * @param newRoleNumber
 * @throws HBException
 */
	public void copyEventRoleAction(boolean onlyOne) throws HBException {
		ResultSet T461_Event_Role_Copy;
		long nextT461PID;
		String roleSentence;
		nextT461PID = lastRowPID(eventRoleTable, pointOpenProject) + 1;
		T461_Event_Role_Copy = requestTableData(selectString, dataBaseIndex);
		try {
			T461_Event_Role_Copy.last();
			System.out.println(" Number of roles copeied: " + T461_Event_Role_Copy.getRow());
			T461_Event_Role_Copy.beforeFirst();
			while (T461_Event_Role_Copy.next()) {
				lang_code = T461_Event_Role_Copy.getString("LANG_CODE");
		// Set up new rolenumber, sequence or roleName case one or many
				if (!onlyOne) {
					roleNumber = T461_Event_Role_Copy.getInt("EVNT_ROLE_NUM");
					eventRoleSequence = T461_Event_Role_Copy.getInt("EVNT_ROLE_SEQ");
					eventRoleName = T461_Event_Role_Copy.getString("EVNT_ROLE_NAME").trim();
				}
				roleSex = T461_Event_Role_Copy.getString("EVNT_ROLE_SEX").trim();
				roleMinAge = T461_Event_Role_Copy.getInt("EVNT_ROLE_MINAGE");
				roleMaxAge = T461_Event_Role_Copy.getInt("EVNT_ROLE_MAXAGE");
				roleSentencePID = T461_Event_Role_Copy.getLong("ROLE_SENTENCE_RPID");
			// Create a copy of the role sentence if sentence exist for copied role
				if (roleSentencePID != null_RPID) {
					roleSentence = getEventRoleSentence(roleSentencePID);
					roleSentencePID = addEventRoleSentence(roleSentence, newEventNumber, roleNumber);
				}
				selectedKeyRole =  T461_Event_Role_Copy.getBoolean("KEY_ASSOC"); 
				addToT461_EVNT_ROLE(nextT461PID , T461_Event_Role_Copy);
				nextT461PID = nextT461PID + 1;
			}
			T461_Event_Role_Copy.close();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" HBEventRoleManager - copyEventRoles() err: " + sqle.getMessage());
		}
	}

/**
 * public void addEventType(String newEventName, int selectedgroupNumber, Object[] eventTypeData) 
 * @throws HBException
 */
	public void addEventType(String newEventName, int selectedgroupNumber, Object[] eventTypeData) throws HBException {
		isActive = false;
		eventGroup = selectedgroupNumber;
		eventTypeName = newEventName;
		newEventNumber = (int) eventTypeData[1];
		gedComTag = (String) eventTypeData[3];
		minYear = (int) eventTypeData[4];
		maxYear =  (int) eventTypeData[5];
		abbrev = (String) eventTypeData[6]; 
		pasttense = (String) eventTypeData[7]; 
		eventHint = (String) eventTypeData[8];
		minAssoc = (int) eventTypeData[9];
		nextT460PID = lastRowPID(eventDefnTable, pointOpenProject) + 1;
		addToT460_EVNT_DEFS(nextT460PID, T460_Event_Defs);
	}

/**
 * findNewLargestNumber(ResultSet hreTable, String field)
 * @param hreTable
 * @param field
 * @return
 * @throws HBException 
*/
	private int findNewLargestNumber(ResultSet hreTable, String field) throws HBException {
		return findNewLargestNumber(hreTable, field, null);
	}
	
	private int findNewLargestNumber(ResultSet hreTable, String field, String event) throws HBException {
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
			return newIndex;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" HBEventRoleManager findNewLargestNumber error: " + sqle.getMessage());
		}
	}
 

/**
 * public void addEventRole(int eventTypeNumber, Object[] eventRoleData) 	
 * @param eventTypeNumber
 * @param eventRoleData
 * @throws HBException
 */
	public void addEventRole(int eventTypeNumber, Object[] eventRoleData) throws HBException {
	
		newEventNumber = eventTypeNumber;
		roleNumber = (int) eventRoleData[0];
		eventRoleSequence = (int) eventRoleData[1];
		eventRoleName = (String) eventRoleData[2];
		roleSex = (String) eventRoleData[3];
		roleMinAge = (int) eventRoleData[4];
		roleMaxAge = (int) eventRoleData[5];
		roleSentencePID = (long) eventRoleData[6];
		selectedKeyRole = (boolean) eventRoleData[7]; 
		nextT461PID = lastRowPID(eventRoleTable, pointOpenProject) + 1;
		addToT461_EVNT_ROLE(nextT461PID, T461_Event_Role);
	}


/**
 * editEventType(String newEventName)
 * @param newEventName
 * @throws HBException
 */
	public void editEventType(String newEventName, int eventTypeNumber,
								int selectedGroupNumber, Object[] eventTypeData) throws HBException {
		isActive = false;
		newEventNumber = selectedEventType;
		eventTypeName = newEventName;
		eventGroup = selectedGroupNumber;
		gedComTag = (String) eventTypeData[3];
		minYear = (int) eventTypeData[4];
		maxYear =  (int) eventTypeData[5];
		abbrev = (String) eventTypeData[6]; 
		pasttense = (String) eventTypeData[7]; 
		eventHint = (String) eventTypeData[8];
		minAssoc = (int) eventTypeData[9];
		updateT460_EVNT_DEFS(eventTypeNumber, T460_Event_Defs);
	}

/**
 * editEventRole(int eventTypeNumber, Obj eventRoleData)
 * @param eventTypeNumber
 * @param eventRoleData
 * @throws HBException
 */
	public void editEventRole(int eventTypeNumber, Object[] eventRoleData) throws HBException {
		roleNumber = (int) eventRoleData[0];
		eventRoleSequence = (int) eventRoleData[1];
		eventRoleName = (String) eventRoleData[2];
		roleSex = (String) eventRoleData[3];
		roleMinAge = (int) eventRoleData[4];
		roleMaxAge = (int) eventRoleData[5];
		roleSentencePID = (long) eventRoleData[6];	
		selectedKeyRole = (boolean) eventRoleData[7]; 	
		System.out.println(" editEventRole " + eventTypeNumber + "/" + roleNumber + "/" + lang_code);
		updateT461_EVNT_ROLE(eventTypeNumber, roleNumber, T461_Event_Role);
	}

/**
 * updateEventRoleSequences
 * @param eventTypeNumber
 * @param roleNumber1
 * @param seqNumber1
 * @param roleNumber2
 * @param seqNumber2
 * @throws HBException
 */
	public void updateEventRoleSequences(int eventTypeNumber, int roleNumber1, int seqNumber1,
											int roleNumber2, int seqNumber2) throws HBException {
		// To update just the GUI Seq numbers of 2 T461 records after a role is moved up/down in its table
		updateSeqInT461_EVNT_ROLE(eventTypeNumber, roleNumber1, seqNumber1, T461_Event_Role);
		updateSeqInT461_EVNT_ROLE(eventTypeNumber, roleNumber2, seqNumber2, T461_Event_Role);
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
			hreTable.updateInt("EVNT_KEY_ASSOC_MIN", minAssoc);
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
 * addToT461_EVENT_ROLE
 * @param primaryPID
 * @param hreTable
 * @throws HBException
 * 		roleMinAge = (int) eventRoleData[4];
		roleMaxAge = (int) eventRoleData[5];
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
		// update with new roleName and default other data
			hreTable.updateLong("PID", primaryPID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateBoolean("IS_SYSTEM", false);
			hreTable.updateString("LANG_CODE", lang_code);
			hreTable.updateInt("EVNT_TYPE", newEventNumber);
			hreTable.updateInt("EVNT_ROLE_NUM", roleNumber);
			hreTable.updateInt("EVNT_ROLE_SEQ", eventRoleSequence);
			hreTable.updateString("EVNT_ROLE_NAME", eventRoleName);
			hreTable.updateString("EVNT_ROLE_SEX", "U");
			hreTable.updateInt("EVNT_ROLE_MINAGE", roleMinAge);
			hreTable.updateInt("EVNT_ROLE_MAXAGE", roleMaxAge);
			hreTable.updateLong("ROLE_SENTENCE_RPID", roleSentencePID);
			hreTable.updateBoolean("KEY_ASSOC", selectedKeyRole);  // New in v22c - 12.12.2024
		//Insert row
			hreTable.insertRow();
			System.out.println(" addToT461_EVNT_ROLE " + newEventNumber + "/" + roleNumber + "/" + eventRoleSequence
					+ " lang=" + lang_code);
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
					hreTable.updateInt("EVNT_KEY_ASSOC_MIN", minAssoc);
					hreTable.updateString("GEDCOM", gedComTag);
					hreTable.updateInt("MIN_YEAR", minYear);
					hreTable.updateInt("MAX_YEAR", maxYear);
					hreTable.updateString("EVNT_ABBREV", abbrev);
					hreTable.updateString("EVNT_PAST", pasttense);
					hreTable.updateString("EVNT_HINT", eventHint);
					hreTable.updateRow();
					break;
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException("HBEventRoleManager - updateT460_EVNT_DEFS error:" + sqle.getMessage());
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
					minAssoc = hreTable.getInt("EVNT_KEY_ASSOC_MIN");
					gedComTag = hreTable.getString("GEDCOM").trim();
					maxYear = hreTable.getInt("MAX_YEAR");
					minYear = hreTable.getInt("MIN_YEAR");
					abbrev = hreTable.getString("EVNT_ABBREV").trim(); //abbrev not available in Define Event GUI
					pasttense = hreTable.getString("EVNT_PAST").trim(); //pasttense available in Define Event GUI
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
 * protected void updateT461_EVNT_ROLE(int eventNumber, int roleNumber, ResultSet hreTable)
 * @param eventNumber
 * @param roleNumber
 * @param hreTable
 * @throws HBException
 * 		roleNumber = (int) eventRoleData[0];
		eventRoleSequence = (int) eventRoleData[1];
		eventRoleName = (String) eventRoleData[2];
		roleSex = (String) eventRoleData[3];
		roleMinAge = (int) eventRoleData[4];
		roleMaxAge = (int) eventRoleData[5];
 */
	protected void updateT461_EVNT_ROLE(int eventTypeNumber, int roleNumber, ResultSet hreTable) throws HBException {
		
		try {
			hreTable.beforeFirst();
			while (hreTable.next()) {
				if (hreTable.getInt("EVNT_TYPE") == eventTypeNumber
						&& hreTable.getInt("EVNT_ROLE_NUM") == roleNumber
							&& hreTable.getString("LANG_CODE").trim().equals(lang_code)) {
				// No update for LANG_CODE (as that would cause a SQL duplicate record!)
				// No update needed for ROLE_NUM either!
					hreTable.updateInt("EVNT_ROLE_SEQ", eventRoleSequence);
					hreTable.updateString("EVNT_ROLE_NAME", eventRoleName.trim());
					hreTable.updateString("EVNT_ROLE_SEX", roleSex);
					hreTable.updateInt("EVNT_ROLE_MINAGE", roleMinAge);
					hreTable.updateInt("EVNT_ROLE_MAXAGE", roleMaxAge);
					hreTable.updateLong("ROLE_SENTENCE_RPID", roleSentencePID); 
					hreTable.updateBoolean("KEY_ASSOC", selectedKeyRole);
					hreTable.updateRow();
				//  System.out.println(" updateT461_EVNT_ROLE " + eventTypeNumber + "/" + roleNumber
				//			+ " lang=" + lang_code + " tabllang=" + hreTable.getString("LANG_CODE"));
					break;
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException("HBEventRoleManager - updateT461_EVNT_ROLE error:" + sqle.getMessage());
		}
	}

/**
 * protected void updateSeqInT461_EVNT_ROLE
 * @param eventNumber
 * @param roleNumber
 * @param seqNumber
 * @param hreTable
 * @throws HBException
 */
	protected void updateSeqInT461_EVNT_ROLE(int eventNumber, int roleNumber, int seqNumber, ResultSet hreTable) throws HBException {
		try {
			hreTable.beforeFirst();
			while (hreTable.next()) {
				if (hreTable.getInt("EVNT_TYPE") == eventNumber
						&& hreTable.getInt("EVNT_ROLE_NUM") == roleNumber
							&& hreTable.getString("LANG_CODE").equals(lang_code)) {
				// Only update the Seq number field
					hreTable.updateInt("EVNT_ROLE_SEQ", seqNumber);
					//System.out.println(" updateSeqInT461: role#=" + roleNumber+" Seq#=" + seqNumber 
					//		+ " lang=" + lang_code + " tabllang=" + hreTable.getString("LANG_CODE"));
					hreTable.updateRow();
					break;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

/**
 * private void collectEventRoleData(int eventNumber, int roleNumber, ResultSet hreTable)
 * @param eventNumber
 * @param roleNumber
 * @param hreTable
 * @throws HBException 
 */
	private void collectEventRoleData(int eventNumber, int roleNumber, ResultSet hreTable) throws HBException {
		try {
			hreTable.beforeFirst();
			while (hreTable.next()) {
				if (hreTable.getInt("EVNT_TYPE") == eventNumber
						&& hreTable.getInt("EVNT_ROLE_NUM") == roleNumber
						&& hreTable.getString("LANG_CODE").equals(lang_code)) {
					eventRoleSequence = hreTable.getInt("EVNT_ROLE_SEQ");
					eventRoleName = hreTable.getString("EVNT_ROLE_NAME").trim();
					roleSex = hreTable.getString("EVNT_ROLE_SEX").trim();
					roleMinAge = hreTable.getInt("EVNT_ROLE_MINAGE");
					roleMaxAge = hreTable.getInt("EVNT_ROLE_MAXAGE");
					roleSentencePID = hreTable.getLong("ROLE_SENTENCE_RPID");
					roleKey = hreTable.getBoolean("KEY_ASSOC");
					//System.out.println( " CollectRoleData: " 
					//			+ eventNumber + "/" + roleNumber
					//			+ "  lang=" + lang_code+" tabllang=" + hreTable.getString("LANG_CODE"));
					break;
				}
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException("HBEventRoleManager - collectEventRoleData error:" + sqle.getMessage());
		}
	}

/**
 * public int deleteEventType(int eventTypeNumber) throws HBException
 * @param eventTypeNumber
 * @return
 * @throws HBException
 */
	public int deleteEventType(int eventTypeNumber) throws HBException {
		long roleSentencePID;
		ResultSet T460_EventDefsRS, T450_EventsRS, T461_EventRoleRS;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		//System.out.println(" Event Type number: " + eventTypeNumber);
		selectString = setSelectSQL("*", eventTable, "EVNT_TYPE = " + eventTypeNumber);
		T450_EventsRS = requestTableData(selectString, dataBaseIndex);
	// Start transaction
		updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
		try {
			if (!isResultSetEmpty(T450_EventsRS)) {
				//System.out.println(" -> Event type " + eventTypeNumber + " in use! ");
			// End transaction
				updateTableData("COMMIT", dataBaseIndex);
				return 1;
			}
			selectString = setSelectSQL("*", eventDefnTable, "EVNT_TYPE = " + eventTypeNumber + " AND LANG_CODE = '" + lang_code + "'");
			T460_EventDefsRS = requestTableData(selectString, dataBaseIndex);
			selectString = setSelectSQL("*", eventRoleTable, "EVNT_TYPE = " + eventTypeNumber);
			T461_EventRoleRS = requestTableData(selectString, dataBaseIndex);

			if (isResultSetEmpty(T461_EventRoleRS)) {
				T460_EventDefsRS.first();
				T460_EventDefsRS.deleteRow();
			// End transaction
				updateTableData("COMMIT", dataBaseIndex);
				return 0;
			}
			T461_EventRoleRS.beforeFirst();
			while (T461_EventRoleRS.next()) {
				roleSentencePID = T461_EventRoleRS.getLong("ROLE_SENTENCE_RPID");
				T461_EventRoleRS.deleteRow();
				if (roleSentencePID != null_RPID) deleteSentenseSet(roleSentencePID);
			}
			T460_EventDefsRS.first();
			T460_EventDefsRS.deleteRow();
			T460_EventDefsRS.close();
			T450_EventsRS.close(); 
			T461_EventRoleRS.close();
		// End transaction
			updateTableData("COMMIT", dataBaseIndex);
			return 0;
		} catch (SQLException sqle) {
		// Roll back transaction
			updateTableData("ROLLBACK", dataBaseIndex);
			sqle.printStackTrace();
			throw new HBException(" HBEventRoleManager - deleteEventType error: " + sqle.getMessage());
		}
	}
	
	
/**
 * public int deleteEventRoles(int eventTypeNumber)	
 * @param eventTypeNumber
 * @return
 * @throws HBException
 */
	public int deleteEventRoles(int eventTypeNumber) throws HBException {
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", eventRoleTable, 
				"EVNT_TYPE = " + eventTypeNumber );
		return deleteRoleAction();
	}
		
	public int deleteEventRole(int eventTypeNumber, int eventRoleNumber) throws HBException {
		ResultSet T450_EventsRS, T461_EventRoleRS;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", eventTable, 
				"EVNT_TYPE = " + eventTypeNumber + " AND PRIM_ASSOC_ROLE_NUM = " + eventRoleNumber);
		T450_EventsRS = requestTableData(selectString, dataBaseIndex);

		try {
			if (!isResultSetEmpty(T450_EventsRS)) {
				System.out.println(" -> Event type and role " + eventTypeNumber + "/" 
						+ eventRoleNumber + " in use! ");
			// End transaction
				updateTableData("COMMIT", dataBaseIndex);
				return 1;
			}
			
		// Check number of roles in table
			selectString = setSelectSQL("*", eventRoleTable, 
					"EVNT_TYPE = " + eventTypeNumber);	
			T461_EventRoleRS =  requestTableData(selectString, dataBaseIndex);
			T461_EventRoleRS.last();
			if (T461_EventRoleRS.getRow() < 2) {
				System.out.println(" -> Event type and role " + eventTypeNumber + "/" 
						+ eventRoleNumber + " not delete last role! ");
				return 2;
			}
			T461_EventRoleRS.close();
			
		// Select role to delete	
			selectString = setSelectSQL("*", eventRoleTable, 
					"EVNT_TYPE = " + eventTypeNumber + " AND EVNT_ROLE_NUM = " + eventRoleNumber);
			return deleteRoleAction();
			
		} catch (SQLException | HBException hbe) {
			hbe.printStackTrace();
			return 3;
		}
	}
	
/**
 * private int deleteRoleAction()		
 * @return
 * @throws HBException
 */
	private int deleteRoleAction() throws HBException {
		ResultSet T461_EventRoleRS;
		T461_EventRoleRS = requestTableData(selectString, dataBaseIndex);
		
	// Start transaction
		updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
		try {
			if (isResultSetEmpty(T461_EventRoleRS)) return 3;
			T461_EventRoleRS.beforeFirst();
			while (T461_EventRoleRS.next()) {
				roleSentencePID = T461_EventRoleRS.getLong("ROLE_SENTENCE_RPID");
				if (roleSentencePID != null_RPID) deleteSentenseSet(roleSentencePID);
				T461_EventRoleRS.deleteRow();
			}
			T461_EventRoleRS.close();
		// End transaction
			updateTableData("COMMIT", dataBaseIndex);
			return 0;
		} catch (SQLException sqle) {
		// Roll back transaction
			updateTableData("ROLLBACK", dataBaseIndex);
			sqle.printStackTrace();
			throw new HBException(" HBEventRoleManager - deleteEventRoles: " + sqle.getMessage());
		}
	}
	
/**
 * public void deleteSentenseSet(long sentenceSetPID)
 * @param sentenceSetPID
 * @throws HBException
 */
	public void deleteSentenseSet(long sentenceSetPID) throws HBException {
		ResultSet eventSentenceRS;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", sentenceSet, "PID = " + sentenceSetPID);
		eventSentenceRS = requestTableData(selectString, dataBaseIndex);
		try {
			if (isResultSetEmpty(eventSentenceRS)) 
				throw new HBException("HBEventRoleManager - deleteEventRoleSentence error!");
			eventSentenceRS.first();
			eventSentenceRS.deleteRow();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" HBEventRoleManager - deleteEventRoleSentence " + sqle.getMessage());
		}
	}
	
/**
 * public boolean testForDuplicateRoleName(String newEventRoleName, int eventTypeNumber)	
 * @param newEventRoleName
 * @param eventTypeNumber
 * @return true if duplicate names
 * @throws HBException
 */
	public boolean testForDuplicateRoleName(String newEventRoleName, int eventTypeNumber) throws HBException {
		String roleName;
		ResultSet T461_EventRoleRS;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", eventRoleTable, "EVNT_TYPE = " + eventTypeNumber);
		T461_EventRoleRS = requestTableData(selectString, dataBaseIndex);
		try {
			if (isResultSetEmpty(T461_EventRoleRS)) 
				throw new HBException(" Eventtype: " + eventTypeNumber + " not found!");
			T461_EventRoleRS.beforeFirst();
			while (T461_EventRoleRS.next()) {
				roleName = T461_EventRoleRS.getString("EVNT_ROLE_NAME").trim();
				if (roleName.equals(newEventRoleName)) return true;
			}
			T461_EventRoleRS.close();
			return false;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" HBEventRoleManager - testEventRolesNames: " + sqle.getMessage());
		}
	}
	
/**	
 * public boolean testForDuplicateEventTypeName(String newEventTypeName, int eventTypeNumber) 
 * @param newEventTypeName
 * @param eventTypeNumber
 * @return
 * @throws HBException
 */
	public boolean testForDuplicateEventTypeName(String newEventTypeName) throws HBException {
		String eventTypeName;
		ResultSet T460_EventDefRS;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", eventDefnTable, "");
		T460_EventDefRS = requestTableData(selectString, dataBaseIndex);
		try {
			if (isResultSetEmpty(T460_EventDefRS) )
				throw new HBException(" Eventtype (T460_EVNT_DEFN) table empty!");
			T460_EventDefRS.beforeFirst();
			while (T460_EventDefRS.next()) {
				eventTypeName = T460_EventDefRS.getString("EVNT_NAME").trim();
				if (eventTypeName.equals(newEventTypeName)) return true;
			}
			T460_EventDefRS.close();
			return false;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" HBEventRoleManager - testEventTypeNames: " + sqle.getMessage());
		}
	}
} // End HBEventRoleManager
