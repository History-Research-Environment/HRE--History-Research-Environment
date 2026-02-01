package hre.bila;
/************************************************************************************************
  * Class PersonHandler extends BusinessLayer
  * Processes data for Person Menu in User GUI
  * Receives requests from User GUI to action methods
  * Sends requests to database over Database Layer API
  ************************************************************************************************
  * v0.00.0016 2019-12-20 - First version (N. Tolleshaug)
  * v0.01.0025 2020-10-25 - Birth and death place data from event tables (N. Tolleshaug)
  * 		   2020-11-01 - Storing of data tables in HBOpenProject (N. Tolleshaug)
  * 		   2020-11-13 - Primary name or BEST_NAME_RPID implemented. (N. Tolleshaug)
  * 		   2020-11-18 - Implemented name display options DISP/SORT (N. Tolleshaug)
  *   		   2020-11-21 - Name display and tree reset for new DISP/SORT (N. Tolleshaug)
  *   		   2020-12-02 - Exception handling improved (N. Tolleshaug)
  * 		   2020-12-29 - Added new columns to LIFE_FORMS and EVENT (N. Tolleshaug)
  * 		   2020-12-29 - Changed to T552_LOCATION_NAMES for placedata (N. Tolleshaug)
  * 		   2021-01-02 - Correct initial position empty database (N. Tolleshaug)
  * 		   2021-01-28 - removed use of HGlobal.selectedProject (N. Tolleshaug)
  * 		   2021-02-07 - Implemented column control HG0581ConfigureTable (N. Tolleshaug)
  * v0.01.0026 2021-05-03 - Implemented handling of DDL 21a (N. Tolleshaug)
  * 		   2021-05-16 - Included both DDL20a and DDL21a (N. Tolleshaug)
  * 		   2021-06-10 - New search for partners (N. Tolleshaug)
  * 		   2021-06-11 - Search for nr 1004, 1026, 1027, 1028, 1029, 1520, 1521 (N. Tolleshaug)
  * 		   2021-06-14 - Improved HDATE processing (N. Tolleshaug)
  * 		   2021-07-07 - Pre-loading and indexing of name element tables (N. Tolleshaug)
  * 		   2021-07-08 - Birth and death place from indexed ResulttSet tables (N. Tolleshaug)
  * 		   2021-07-09 - Event Location data from indexed ResulttSet tables (N. Tolleshaug)
  * v0.01.0027 2021-11-09 - Indexed T_553 loction name elements (N. Tolleshaug)
  *            2021-12-11 - Included DDL20a, DDL21a and DDL21c(N. Tolleshaug)
  *            2022-01-01 - Progress Bar update for PersonSelect (N. Tolleshaug)
  *            2022-01-17 - Set last selected person visible index (N. Tolleshaug)
  * 		   2022-02-26 - Modified to use the NLS version of HGlobal (D Ferguson)
  * 		   2022-04-28 - Updated parent table i Person manager (N. Tolleshaug)
  * v0.01.0028 2022-09-07 - Activated HG0525ManagePersonNameStyles (N. Tolleshaug)
  *	 		   2022-10-01 - Changed date translation to v21h tables (N. Tolleshaug)
  *			   2022-10-25 - Name style description default "null|" in v21i database (N. Tolleshaug)
  *			   2022-12-07 - Update of single parent recorded - error fix  (N. Tolleshaug)
  *			   2022-12-08 - Update of name manager HIDDEN elements  (N. Tolleshaug)
  *			   2022-12-16 - Store name style index in T302 (N. Tolleshaug)
  * 		   2023-01-06 - Rewrite of person data processing (N. Tolleshaug)
  * 		   2023-01-16 - Added space in manage name elements (N. Tolleshaug)
  * 		   2023-01-26 - Final update of T302 to Done after processing (N. Tolleshaug)
  * 		   2023-02-07 - Manage person Names - Added VP name style and dates (N. Tolleshaug)
  * 		   2023-02-08 - Implemented show and store person name style (N. Tolleshaug)
  * 		   2023-02-22 - Moved all exhibit image processing to HBMediaHandler (N. Tolleshaug)
  * 		   2023-03-13 - Primary name update handling (N. Tolleshaug)
  * 		   2023-03-19 - Handling of name element data update in T403 (N. Tolleshaug)
  * 		   2023-03-20 - Person Select update if Person Name update (N. Tolleshaug)
  * 		   2023-04-10 - Support for 3 open projects (N. Tolleshaug)
  * v0.03.0030 2023-06-03 - Activated flag processing (N. Tolleshaug)
  * 		   2023-06-26 - Major revision to remove no used code (N. Tolleshaug)
  * 		   2023-06-27 - Updated code for event and associate table (N. Tolleshaug)
  * 		   2023-06-28 - Control of witness events in Person Manager table (N. Tolleshaug)
  * 		   2023-06-29 - Use of HDATE sort for sorting event table (N. Tolleshaug)
  * 		   2023-07-01 - Revision of Person Select table processing (N. Tolleshaug)
  * 		   2023-07-01 - list of children in Person Manager event processing (N. Tolleshaug)
  * 		   2023-07-05 - list of children with parent role (N. Tolleshaug)
  * 		   2023-07-07 - Present number of marriages and children (N. Tolleshaug)
  * 		   2023-07-16 - Language translation for Flag Manager (N. Tolleshaug)
  * 		   2023-07-22 - Update flag value for Person Manager (N. Tolleshaug)
  * 		   2023-08-01 - Person Name Manager updated to V22a (N. Tolleshaug)
  * 		   2023-08-05 - Enable update status bar - on/off (N. Tolleshaug)
  * 		   2023-08-13 - Fix event table problem when no events (N. Tolleshaug)
  * 		   2023-09-08 - Moved class ManagePersonFlag to HBPersonFlagManager (N. Tolleshaug)
  * 		   2023-09-13 - Moved flagFieldsT401 flag Names to HBBusinessLayer (N. Tolleshaug)
  * 		   2023-09-16 - For add and delete person added COMMIT/ROLLBACK (N. Tolleshaug)
  * 		   2023-09-18 - Handle flagindex = -1 and set F-ERR for flag value (N. Tolleshaug)
  * 		   2023-09-19 - Also for TMG flags flagindex = -1 and set F-ERR (N. Tolleshaug)
  * 		   2023-09-20 - Fix for missing translated parent role in T460 (N. Tolleshaug)
  * v0.03.0031 2023-10-22 - Updated for v22b (N. Tolleshaug)
  * 		   2023-11-19 - Updated for add person and add parent (N. Tolleshaug)
  * 		   2023-11-21 - Added delete person and Fixed error in add parent (N. Tolleshaug)
  * 		   2023-11-22 - Updated for new HRE project add person/parent (N. Tolleshaug)
  * 		   2023-12-07 - Updated for detailed main menu add person (N. Tolleshaug)
  * 		   2023-12-08 - Updated for memo handling name ande event (N. Tolleshaug)
  * 		   2023-12-10 - Updated for setting selected person in Person manager (N. Tolleshaug)
  * 		   2023-12-23 - Updated for select Christ/Bapt (N. Tolleshaug)
  * 		   2023-12-24 - Implemented add child (N Tolleshaug)
  * 		   2023-12-30 - ERROR fix Activate Person Manager after delete(N Tolleshaug)
  * 		   2023-12-30 - Implemented add sibling (N Tolleshaug)
  * 		   2024-01-03 - Update of role list for baptism according to event (N Tolleshaug)
  * 		   2024-01-09 - Update of partner event (N Tolleshaug)
  * 		   2024-02-07 - Updated processing of associate list with partner (N. Tolleshaug)
  * 		   2024-02-24 - Updated processing person mamager events with PID ref (N. Tolleshaug)
  *			   2024-03-16 - Added routines for use by PersonSelectMin (D Ferguson)
  *			   2024-04-01 - Added method for delete partner in PersonManager (N. Tolleshaug)
  *			   2024-04-01 - Added activation of select parent, assocoate and partner (N. Tolleshaug)
  *			   2024-04-07 - updateElementData() for editing of name date elements (N. Tolleshaug)
  *			   2024-04-14 - Updated processing of associate table processing (N. Tolleshaug)
  *			   2024-04-17 - Renamed associate variables (N. Tolleshaug)
  *			   2024-05-20 - Implemented add, delete and edit parent and partner (N. Tolleshaug)
  *			   2024-05-20 - Updated addToNameChangeList removed test (N. Tolleshaug)
  *			   2024-05-26 - New add/edit event handling (N. Tolleshaug)
  *			   2024-06-21 - Implemented name handling for Person Manager
  *			   2024-06-22 - Modified activatePersonNameEdit to find personMamePID
  *			   2024-06-27 - Implemented activatePersonNameEdit to change nameType
  *			   2024-07-24 - Updated for use of HG0590EditDate (N Tolleshaug)
  *			   2024-08-17 - Fix for add partner event from main add partner (N Tolleshaug)
  *			   2024-08-20 - Fix for add update partner role (N Tolleshaug)
  *			   2024-09-04 - Updated PersonManager associate table (N. Tolleshaug)
  *			   2024-09-11 - Fixed en-UK problem translation in updateEventRoles.
  *			   2024-10-03 - Fixed error edit partner when no partner available  (N. Tolleshaug)
  *			   2024-10-05 - Modified reset PS and initiate PS  (N. Tolleshaug)
  *			   2024-10-10 - Use getChangedDateFormat() if date format changed (N. Tolleshaug)
  *			   2024-10-12 - HGlobal.reloadPS control in resetPersonSelect() (N. Tolleshaug)
  *			   2024-10-13 - Removed if (HGlobal.DEBUG) {} to if (HGlobal.DEBUG) (N. Tolleshaug)
  *			   2024-10-19 - Removed resetPersonManager() in deletePartnerEvent (N. Tolleshaug)
  *			   2024-10-19 - Added child with no birth in PersonManager event table (N. Tolleshaug)
  *			   2024-10-21 - In addPartnerAssocToAssoc() removed self assoc (N. Tolleshaug)
  *			   2024-10-24 - Added more tests for self assocs Person Manager (N. Tolleshaug)
  *			   2024-10-29 - Added close window call to HBOpenProjectData ResetPS/PM (N. Tolleshaug)
  *			   2024-11-08 - More changes to remove duplicate assocs Person Manager (N. Tolleshaug)
  *			   2024-11-10 - Final changes to remove duplicate assocs Person Manager (N. Tolleshaug)
  *			   2024-11-19 - Modified person name element table update (N. Tolleshaug)
  *			   2024-12-08 - Updated name styles and event type handling (N Tolleshaug)
  *			   2024-12-11 - Fix for Person Select reset PS off (N Tolleshaug)
  * v0.04.0032 2024-12-22 - Updated for new project B32 (N. Tolleshaug)
  * 		   2024-12-23 - Fixed update of name memo for add person (N. Tolleshaug)
  * 		   2025-01-11 - Rearranged processing of event and roles (N. Tolleshaug)
  * 		   2025-01-22 - Fixed error in event and roles processing(N. Tolleshaug)
  * 		   2025-04-17 - Only count children if bio relationship (Issue 31.67) (D Ferguson)
  * 		   2025-04-26 - If parent not known pass '---' parent-name in parentTable (D Ferguson)
  *            2025-05-09 - Reload associate and citation event add/edit(N.Tolleshaug)
  *            2026-01-27 - line 234 - pointEventRoleManager = 
  *            				pointOpenProject.getEventRoleManager(); (N.Tolleshaug)
  *            
  *********************************************************************************************
  * 	Interpretation of partnerRelationData
  *			 	0 = partnerTablePID, 1 = partneType, 2 = priPartRole, 3 = secPartRole
  *								4 = selectedPerson, 5 = partner
  **********************************************************************************************
  *		 Interpretation of parentRelationData:
  *		 		0 = ParentPID, 1 = parent Name, 2 = paentRole, 3 = surety, 4 = parentRPID
  ***************************************************************************************************/

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import hre.dbla.HDDatabaseLayer;
import hre.gui.HG0401HREMain;
import hre.gui.HG0505AddPerson;
import hre.gui.HG0505AddPersonChild;
import hre.gui.HG0505AddPersonParent;
import hre.gui.HG0505AddPersonPartner;
import hre.gui.HG0505AddPersonSibling;
import hre.gui.HG0506ManagePerson;
import hre.gui.HG0507PersonSelect;
import hre.gui.HG0507SelectAssociate;
import hre.gui.HG0507SelectParent;
import hre.gui.HG0507SelectPartner;
import hre.gui.HG0507SelectPerson;
import hre.gui.HG0509AddPersonName;
import hre.gui.HG0509EditPersonName;
import hre.gui.HG0509ManagePersonName;
import hre.gui.HG0512FlagManager;
import hre.gui.HG0525ManagePersonNameStyles;
import hre.gui.HG0547EditEvent;
import hre.gui.HGlobal;
import hre.gui.HGlobalCode;
import hre.tmgjava.HCException;

/**
  * Class HBPersonHandler
  * @author Nils Tolleshaug
  * @version v0.04.0032
  * @since 2019-12-20
  */
public class HBPersonHandler extends HBBusinessLayer {

	String dBbuild = HGlobal.databaseVersion;

	HashMap<Long,PersonSelectData> personDataIndex;
	HashMap<Long,Vector<Long>> partnerMap;
	ArrayList<PersonSelectData> personSelectData;

	protected ResultSet resultSetT401_PERSON;
    protected ResultSet hdateT175table, hdateT204data;
    protected ResultSet pointT302_GUI_CONFIG;

	long null_RPID  = 1999999999999999L;
	long proOffset  = 1000000000000000L;

	int dateFormatIndex = 0;
	boolean updateMonitor = true;

    public ManagePersonData pointManagePersonData;
    public HBPersonFlagManager pointManagePersonFlag;
    public HBEventRoleManager pointEventRoleManager;
	public AddPersonRecord pointAddPersonRecord;
	private HBProjectOpenData pointSelectedProject;
	public HBNameStyleManager pointStyleData;
	public ManagePersonNameData pointManagePersonNameData;
	public HG0507PersonSelect personSelectScreen = null;
	//public HG0507SelectPerson pointSelectAssociate = null;
	public HG0506ManagePerson managePersonScreen = null;
	private HBProjectOpenData pointOpenProject;


	int nrOfRows = 0;
	int countStatusBar = 0;
	int maxRows = 0;
	int numberofProcesses = 6;
	ResultSet personTableData;

	JProgressBar progBar;

// Name Styles defines the name element to be included in the name string
	private String[] locationNameStyle;
	private String[] personNameStyle; // HRE Codes - 1100|5000|3000|5200|5700

// Control of colums
	Object [][] tableControl;

// Update table of names from LIFE_FORM_NAMES
	String personName = null;

/**
 * Constructor HBPersonHandler()
 */
	public HBPersonHandler(HBProjectOpenData pointOpenProject) {
		super();
		this.pointOpenProject = pointOpenProject;
		if (pointOpenProject != null) {
			pointEventRoleManager = pointOpenProject.getEventRoleManager(); // Updated 28.1.2026 NTo
		} else System.out.println(" HBPersonHandler() - pointOpenProject == null!");
		if (HGlobal.DEBUG)
			System.out.println("Person Handler initiated!");
		
	}

/**
 * errorMessage(String errorMess)
 * @param errorMess
 */
	public void errorJOptionMessage(String title, String errorMess) {
		JOptionPane.showMessageDialog(null, errorMess, title, JOptionPane.ERROR_MESSAGE);
	}

/**
 * setStatusPercent(int countStatusBar)
 * @param countStatusBar
 */
	public void setStatusPercent(int countStatusBar) {
		try {
			if (updateMonitor) {
				progBar.setValue(calcPercent(countStatusBar, maxRows));
			}
		} catch (HBException hbe) {
			hbe.printStackTrace();
		}
	}

	public void enableUpdateMonitor(boolean state) {
		updateMonitor = state;
	}

/**
 * API Methods for Person Manager
 * @returns Object[][] table data or image
 */
	public String getManagedPersonName() {
		return pointManagePersonData.getPersonName();
	}

	public Object[] getParentTableData(int indexInTable) {
		return pointManagePersonData.getParentTableData(indexInTable);
	}

	public Object[] getPartnerTableData(int indexInTable) {
		return pointManagePersonData.getPartnerTableData(indexInTable);
	}

	public String getPersonReference() {
		return pointManagePersonData.getPersonReference();
	}

	public long getEventPID(int tableRow) {
		if (pointManagePersonData.eventPIDhash.containsKey(tableRow)) {
			return pointManagePersonData.eventPIDhash.get(tableRow);
		}
		return null_RPID;
	}


	public String[] getManagedStyleNameAndDates() {
		return pointManagePersonData.getNameData();
	}

	public Object[][] getManagedEventTable() {
		return pointManagePersonData.getEventTable();
	}

	public Object[][] getManagedAssociateTable() {
		return pointManagePersonData.getAssociateTable();
	}

	public Object[][] getManagedPartnerTable() {
		return pointManagePersonData.getPartnerTable();
	}

	public Object[][] getManagedNameTable() {
		return pointManagePersonData.getNameTable();
	}

	public Object[][] getManagedFlagTable() {
		return pointManagePersonData.getFlagTable();
	}

	public void setPersonFlagSetting(int flagId, String newValue) throws HBException {
		pointManagePersonData.setPersonFlagSetting(flagId, newValue);
	}

	public void setWitnessState(boolean state) throws HBException {
		pointManagePersonData.setWinessState(state);
	}

	public void setChildrenState(boolean state) throws HBException {
		pointManagePersonData.setChildrenState(state);
	}

	public int getNumberOfCildren() {
		return pointManagePersonData.getNumberOfCildren();
	}

	public int getNumberOfMarriages() {
		return pointManagePersonData.getNumberOfMarriages();
	}

	public Object[][] getManagedParentTable() {
		return pointManagePersonData.getParentTable();
	}

	public ImageIcon getManagedExhibitImage() {
		return pointManagePersonData.getExhibitImage();
	}

	public ArrayList<ImageIcon> getManagedImageList() {
		return pointManagePersonData.getImageList();
	}

	public void updateAllNameTable() throws HBException {
		pointManagePersonData.updateAllNameTable();
	}


/**
 * API Methods for Person Name Manager
 * @returns Object[][] table data or image
 */

	public boolean detectHiddenElements() {
		return pointManagePersonNameData.detectHiddenElements();
	}

	public String[] getAvailableStyles() {
		return pointManagePersonNameData.getAvailableStyles();
	}

	public void setNameStyleIndex(int index) {
		pointManagePersonNameData.setNameStyleIndex(index);
	}

	public Object[][] getPersonNameTable() {
		return pointManagePersonNameData.getNameElementTable();
	}

	public int getDefaultIndex() {
		return pointManagePersonNameData.getDefaultStyleIndex();
	}

	public String[] getPersonTableHeader() {
		return pointManagePersonNameData.getTableHeader();
	}

	public String[] getNameData() {
		return pointManagePersonNameData.getNameData();
	}

	public boolean getPrimaryName() {
		return pointManagePersonNameData.getPrimaryName();
	}

	public void setNameEventType(int nameEventType) {
		pointManagePersonNameData.setNameEventType(nameEventType);
	}

	public int updateManagePersonNameTable(long personTablePID) throws HBException {
		return pointManagePersonNameData.updateManagePersonNameTable(personTablePID);
	}

	public int updateManagePersonNameTable() throws HBException {
		return pointManagePersonNameData.updateManagePersonNameTable();
	}

	public void updateStoredNameStyle(int selectIndex, long personNamePID) throws HBException {
		pointManagePersonNameData.updateStoredNameStyle(selectIndex, personNamePID);
	}

	public void updatePersonBestName(long personNameTablePID) throws HBException {
		pointManagePersonNameData.updatePersonBestName(personNameTablePID);
	}

	public long createPersonNameRecord(long personTablePID, int nmaeType) throws HBException {
		return pointManagePersonNameData.createPersonNameRecord(personTablePID, nmaeType);
	}

	public void addToNameChangeList(int selectedIndex, String nameData) {
		pointManagePersonNameData.addToNameChangeList(selectedIndex, nameData);
	}

	public void updateElementData(int nameType) throws HBException {
		pointManagePersonNameData.updateElementData(nameType);
	}

	public void createNameDates(boolean update, long nameTablePID, String dateField, Object[] nameDateData) throws HBException {
		pointManagePersonNameData.createNameDates(update, nameTablePID, dateField, nameDateData);
	}

/**
 * 	API's for AddPersonRecord
 *  Adding a person to HRE
 */
	public int getDefaultNameStyleIndex() {
		return pointAddPersonRecord.getDefaultNameStyleIndex();
	}

	public int getDefaultLocationStyleIndex() {
		return pointAddPersonRecord.getDefaultLocationStyleIndex();
	}

	public String[] getNameStyles() {
		return pointAddPersonRecord.getNameStyles();
	}

	public String[] getLocationStyles() {
		return pointAddPersonRecord.getLocationStyles();
	}

	public Object[][] getNameDataTable(int styleIndex) {
		return pointAddPersonRecord.getNameDataTable(styleIndex);
	}

	public String[] getNameTableHeader() {
		return pointAddPersonRecord.nameHeaderData;
	}

	public Object[][] getLocationDataTable(int styleIndex) {
		return pointAddPersonRecord.getLocationDataTable(styleIndex);
	}

	public String[] getLocationTableHeader() {
		return pointAddPersonRecord.locationHeaderData;
	}

	public Object[][] getAddPersobFlagTable() {
		return pointAddPersonRecord.getPersonFlagTable();
	}

	public void addToPersonNameChangeList(int selectedIndex, String nameData) {
		pointAddPersonRecord.addToNameChangeList(selectedIndex, nameData);
	}

	public void addToPersonFlagChangeList(int selectedIndex, int flagIdent) {
		pointAddPersonRecord.addToFlagChangeList(selectedIndex, flagIdent);
	}

	public void addToLocationChangeList(int eventIndex, int selectedIndex, String nameData) {
		pointAddPersonRecord.addToLocationChangeList(eventIndex, selectedIndex, nameData);
	}

	public void addNewPerson(String refText) throws HBException {
		pointAddPersonRecord.createPersonRecord(refText);
	}

	public void createPersonEventDates(boolean update, long nameTablePID, String dateField, Object[] nameDateData) throws HBException {
		pointAddPersonRecord.createPersonEventDates(update, nameTablePID, dateField, nameDateData);
	}

	public long addPersonEvent(int indexEventType, int eventType) throws HBException {
		return pointAddPersonRecord.addPersonEvent(indexEventType, eventType);
	}

	public void addNewParent() throws HBException {
		pointAddPersonRecord.addParentRelation();
	}

	public void addNewParent(long newParentPID) throws HBException {
		pointAddPersonRecord.addParentRelation(newParentPID);
	}

	public void updatePartnerEventLink(long newPartnerPID, long newEventPID) throws HBException {
		pointAddPersonRecord.updatePartnerEventLink(newPartnerPID, newEventPID);
	}

	public void updateParentRelationTableRow(long parentTablePID) throws HBException {
		pointAddPersonRecord.updateParentRelationTableRow(parentTablePID);
	// Restart Person manager after update
	//	resetPersonManager();
	}

	public void addNewChild() throws HBException {
		pointAddPersonRecord.updateChildRelation();
	}

	public void addNewSibling(int siblingIndex) throws HBException {
		pointAddPersonRecord.updateSiblingRelation(siblingIndex);
	}

	public long addNewPartner(int selectedPartnerType, int partRole1, int partRole2) throws HBException {

		long createdPartnerRelation = pointAddPersonRecord.addPartnerRelation(selectedPartnerType, partRole1, partRole2);
		return createdPartnerRelation;
	}

	public void updatePartner(long partnerTablePID, int selectedPartnerType, int partRole1, int partRole2) throws HBException {
		pointAddPersonRecord.updatePartnerRelation(partnerTablePID, selectedPartnerType, partRole1, partRole2);
	// Reset Person manager
		resetPersonManager();
	}

	public void setGreatestVisibleId(int visibleId) {
		pointAddPersonRecord.nextVisibleId = visibleId;
	}

	public void setAssociateTablePID(long assocTablePID) {
		pointAddPersonRecord.selectedAssociatePID = assocTablePID;
	}

	public void createAddPersonGUIMemo(String memodata) throws HBException {
		pointAddPersonRecord.createAddPersonGUIMemo(memodata);
	}

	public void createSelectGUIMemo(String memodata, String tableName) throws HBException {
		pointAddPersonRecord.createSelectGUIMemo(memodata, tableName);
	}

	public String readSelectGUIMemo(long tablePID, String tableName) throws HBException {
		return pointAddPersonRecord.readSelectGUIMemo(tablePID, tableName);
	}

	public void updateSelectGUIMemo(String memodata, long tablePID, String tableName) throws HBException {
		pointAddPersonRecord.updateSelectGUIMemo(memodata, tablePID, tableName);
	}

	public String[] getRolesForEvent(int eventType, String selectRoles) throws HBException {
		return pointAddPersonRecord.getRolesForEvent(eventType, selectRoles);
	}

	public int[] getEventRoleTypes() {
		return pointAddPersonRecord.getEventRoleTypes();
	}

	public String[] getPartnerEventList(int eventGroup) throws HBException {
		return pointAddPersonRecord.getPartnerEventList(eventGroup);
	}

	public int[] getPartnerEventTypes() {
		return pointAddPersonRecord.getPartnerEventTypes();
	}

	public String[] getRolesForParent(int eventGroup) throws HBException {
		return pointAddPersonRecord.getRolesForParent(eventGroup);
	}

	public int[] getRolesTypeParent() {
		return pointAddPersonRecord.getRolesTypeParent();
	}

	public void setEventRole(int eventRoleIndex) {
		pointAddPersonRecord.setEventRole(eventRoleIndex);
	}

	public void setParentRole(int parentRoleIndex) throws HBException {
		pointAddPersonRecord.setParentRole(parentRoleIndex);
	}

	public void setNewPartnerPID(long newPartnerPID) throws HBException {
		if (HGlobal.DEBUG)
			System.out.println(" HBPersonHandler setPartner PID:  " + newPartnerPID);
		pointAddPersonRecord.setNewPartnerPID(newPartnerPID);
	}

/**
 * API for Event Role manager
 */
	public String[] getEventTypeList(int eventGroup) throws HBException {
		pointEventRoleManager = pointOpenProject.getEventRoleManager();
		return pointEventRoleManager.getEventTypeList(eventGroup);
	}

	public int[] getEventTypes() {
		return pointEventRoleManager.getEventTypes();
	}
/**
 * Set pointer for program bar
 * @param progBar
 */
	public void setPointProgBar(JProgressBar progBar) {
		this.progBar = progBar;
	}

/**
 * int getPersonSex(long personPID)
 * @param personPID
 * @return
 * @throws HBException
 */
	public int getPersonSex(long personPID) throws HBException {
		String selectString;
		ResultSet personTableRS;
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", personTable,"PID = " + personPID);
		personTableRS = requestTableData(selectString, dataBaseIndex);
		try {
			personTableRS.first();
			return personTableRS.getInt("BIRTH_SEX");
		} catch (SQLException sqle) {
			System.out.println(" HBPersonHandler - getPersonSex: " + sqle.getMessage());
			sqle.printStackTrace();
		}
		return 0;
	}

/**
 * get person table PID for selected person
 * @param tableIndex
 * @return
 */
	public long getPersonTablePID(int tableIndex) {
		if (personSelectData == null) {
			return null_RPID ;
		}
		return personSelectData.get(tableIndex).getPersonTablePID();
	}

/**
 * intitiateFlagManager(HBProjectOpenData pointOpenProject)
 * @param pointOpenProject
 * @return HG0512FlagManager
 * @throws HBException
 */
	public HG0512FlagManager intitiateFlagManager(HBProjectOpenData pointOpenProject) throws HBException {
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		HG0512FlagManager flagScreen;
		String screenID;

		pointManagePersonFlag = new HBPersonFlagManager(pointDBlayer, dataBaseIndex);
		flagScreen = new HG0512FlagManager(pointOpenProject, pointManagePersonFlag);
	// Set size for frame
		screenID = flagScreen.screenID;
		Dimension frameSize = pointOpenProject.pointGuiData.getFrameSize(screenID);
		if (frameSize != null) {
			flagScreen.setSize(frameSize);
		}

// Set location for frame
	  Point location = pointOpenProject.pointGuiData.getFramePosition(screenID);
	  if (location == null) {
	// Sets screen top-left corner relative to parent window
			Point xymainPane = HG0401HREMain.mainPane.getLocationOnScreen();
			flagScreen.setLocation(xymainPane.x + 50, xymainPane.y + 50);
		  } else {
		flagScreen.setLocation(location.x,location.y);
	}
		  return flagScreen;
	}

/**
 * getPartnerList(Long PersonPID)
 * @param PersonPID
 * @return Vector with list
 */
	public Vector<Long> getPartnerList(Long personPID) {
		if (partnerMap.containsKey(personPID)) {
			return partnerMap.get(personPID);
		}
		return null;
	}

/**
 * setSelectedPerson
 * @param projectName
 * @param selectedRowInTable
 * @param userID
 * @param selectedUser
 */
	public void setSelectedPerson(String projectName, int selectedRowInTable, int userID, String selectedUser) {
		long personTablePID = getPersonTablePID(selectedRowInTable);
		pointOpenProject.storeSelectedPersonData(selectedRowInTable, userID,
										personTablePID, selectedUser);
	}

/**
 * getDefaultOutNameStyles(String nameType)
 * @param nameType
 * @return
 * @throws HBException
 */
	public String[] getDefaultOutNameStyles(String nameType, HBProjectOpenData pointOpenProject) throws HBException {
		int dataBaseIndex =  pointOpenProject.getOpenDatabaseIndex();
		int selectNameDisplayIndex = pointOpenProject.getNameDisplayIndex();
		String[] outputStyleNames = null;
		ResultSet outputStyleTable;
		int index = 0;
		long ownerPID = getDefaultNameStyle(nameType, dataBaseIndex);
		try {
			outputStyleTable = pointLibraryResultSet.getOutputStylesTable(nameStylesOutput,
																		  nameType,
																		  ownerPID,
																		  dataBaseIndex);
			outputStyleTable.last();
/**
 * 	WARNING ******* Expect only one R styles
 */
			int rows = outputStyleTable.getRow() - 1;
		// If larger number of name styles selected - reset selectNameDisplayIndex
			if (selectNameDisplayIndex > rows) {
				pointOpenProject.setNameDisplayIndex(0);
			}

			outputStyleNames = new String[rows];
			outputStyleTable.beforeFirst();
			while (outputStyleTable.next()) {
				if (outputStyleTable.getString("OUT_TYPE").equals("D")) {
					outputStyleNames[index] = outputStyleTable.getString("OUT_NAME_STYLE");
					index++;
				}
			}
			return outputStyleNames;
		} catch (SQLException | HBException hbe) {
			if (HGlobal.DEBUG) {
				System.out.println("HBToolHandler - updateOutputStyleTable: " + hbe.getMessage());
				hbe.printStackTrace();
			}

		}
		return outputStyleNames;
	}


/**
 * Set up HG0507PersonSelect on main screen
 * @param pointOpenProject
 * @param position (of this frame relative to others)
 * @return
 */
	public HG0507PersonSelect initiatePersonSelect(HBProjectOpenData pointOpenProject, String position) {
		int index;
		String screenID = HG0507PersonSelect.screenID;

	// Remove People select if already there
		index = pointOpenProject.pointGuiData.getStoreIndex(screenID);
		personSelectScreen = (HG0507PersonSelect) pointOpenProject.getWindowPointer(index);
		if  (personSelectScreen != null) {
			pointOpenProject.clearWindowPointer(index); // Temp fix for duplicate PS
			personSelectScreen.dispose();
			personSelectScreen = null;
			if (HGlobal.DEBUG)
				System.out.println(" initiatePersonSelect - Clear and Dispose screenID: "
												+ screenID + " StoreIndex: " + index);

		} // else System.out.println(" initiatePersonSelect - Create screenID: "
			//	+ screenID + " StoreIndex: " + index);

		dateFormatSelect();

		try {
			  HG0401HREMain.mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		// Get Table Control array	for HG0507PersonSelect
			  Object [][] tableControlData = pointOpenProject.pointGuiData.getTableControl(screenID);

		// Initiate tableControl
			  if (tableControlData == null) {
				  pointOpenProject.pointGuiData.cloneTableControl(screenID,
				  							tableControlInitPerson(screenID));

				  tableControlData = pointOpenProject.pointGuiData.getTableControl(screenID);
			  } else {
		// Reset language translation
					Object[][] headersTranslated = tableControlInitPerson(screenID);
					for (int i = 0; i < headersTranslated.length; i++) {
						tableControlData[i][0] = headersTranslated[i][0];
					}
			  }

		// Set last selected person visible index
			  pointOpenProject.setSelectedPersonIndex(pointOpenProject.pointGuiData.getVisibleIDX(screenID));

	    // Restore name style index setting
			  pointOpenProject.restoreNameStyleIndex(screenID);

			  HG0401HREMain.mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			  personSelectScreen = new HG0507PersonSelect(this,
					  									pointOpenProject,
					  									tableControlData,
					  									position);

		// Reset window pointer
			  pointOpenProject.setWindowPointer(index, personSelectScreen);

		// Set size for frame
			  Dimension frameSize = pointOpenProject.pointGuiData.getFrameSize(screenID);
			  if (frameSize != null)
				personSelectScreen.setPreferredSize(frameSize);


		// Set location for frame
			  Point location = pointOpenProject.pointGuiData.getFramePosition(screenID);
			  if (location == null) {
				  // Sets screen top-left corner relative to parent window
					 personSelectScreen.setLocation(50, 50);
			  } else {
				personSelectScreen.setLocation(location.x,location.y);
			}

			  personSelectScreen.setVisible(true);
			  HG0401HREMain.mainFrame.setCursor(Cursor.getDefaultCursor());
			  return personSelectScreen;

		} catch (HBException hbe) {
			if (HGlobal.DEBUG)
				System.out.println("Error initiatePersonSelect: " + hbe.getMessage());


	// Temp error message waiting for NLS implementation
			JOptionPane.showMessageDialog(null,	"Initiate Person select error:"
					+ "\nError: " + hbe.getMessage(),"Person Select",JOptionPane.INFORMATION_MESSAGE);
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Error initiatePersonSelect:  " + hbe.getMessage());
				HB0711Logging.printStackTraceToFile(hbe);
			}
			return personSelectScreen;
		}
	}

/**
 * finalActionT302(HBProjectOpenData pointOpenProject)
 * @param pointOpenProject
 */
	public void finalActionT302(HBProjectOpenData pointOpenProject) {

	// Mark the window as open in the T302 table
		String screenID = HG0507PersonSelect.screenID;
		  try {
				pointOpenProject.pointGuiData.setOpenStatus(screenID,
					pointOpenProject.pointT302_GUI_CONFIG,true);

			// Set class Name in GUI config data
				  pointOpenProject.setClassName(screenID,"HG0507PersonSelect");

			// register the open screen on HBOpenprojectData
				  pointOpenProject.registerOpenScreen(personSelectScreen);

			} catch (HBException hbe) {
				if (HGlobal.DEBUG)
					System.out.println("HBPersonHandler - finalActionT302: " + hbe.getMessage());

					hbe.printStackTrace();
			}
	}

/**
 * 	resetPersonSelect() - Restart Person Select if changes in table
 */
	public void resetPersonSelect() {
		String screenID = HG0507PersonSelect.screenID;
		int index = pointOpenProject.pointGuiData.getStoreIndex(screenID);
		personSelectScreen = (HG0507PersonSelect) pointOpenProject.getWindowPointer(index);
		if (personSelectScreen != null) {
			if (HGlobal.DEBUG)
				System.out.println(" HBPersonHandler - Reset Person Select!");
			if (HGlobal.reloadPS) {
				pointOpenProject.removeOpenScreen(screenID);
				personSelectScreen.dispose();
			}

		// Force regenerate of table and tree with new name style
			pointSelectedProject.pointTree = null;
			pointSelectedProject.personSelectData = null;
		// Reload person table list for PersonSelect
			pointSelectedProject.setReloadPersonSelectData(true);

		// Reload person table list also for PersonSelectMin
			pointSelectedProject.setReloadSelectPersonList(true);

		// If reload PS set by user
			if (HGlobal.reloadPS) {
			// Initiate new PersonSelect window, but open Behind all others
				initiatePersonSelect(pointSelectedProject, "B");
			}
		}
	}

/**
 * resetPersonmanager()
 */
	public void resetPersonManager() {
		long personSelectedPID;
		//String screenID = "50600";
		String screenID = HG0506ManagePerson.screenID;

	// Remove Manage person GUI
		if (managePersonScreen != null) {
			managePersonScreen.dispose();
			pointOpenProject.removeOpenScreen(screenID);
			personSelectedPID = pointOpenProject.getSelectedPersonPID();
			initiateManagePerson(pointOpenProject, personSelectedPID, screenID);
		}
	}

/**
 * Routine to supply Table Control info for PersonSelector Configuration
 * as used by HG0507PersonSelect through HBPersonHandler
 * tableControlInitPerson(String screenID)
 * @param screenID
 * @return String[] tableControlInitPerson
 */
	private Object[][] tableControlInitPerson(String screenID) {
		String [] tableHeaders = setTranslatedData(screenID, "1", false);
		if (HGlobal.DEBUG)
			System.out.println("Person Table Headers: " + Arrays.toString(tableHeaders));

		Object [][] tableControlInitPerson  = new Object[tableHeaders.length][2];
		tableControlInitPerson[0][0] = " " + tableHeaders[0];
		tableControlInitPerson[0][1] = Boolean.FALSE;
		tableControlInitPerson[1][0] = " " + tableHeaders[1];
		tableControlInitPerson[1][1] = Boolean.FALSE;
		for (int i = 2 ; i < tableHeaders.length; i++) {
			tableControlInitPerson[i][0] = " " + tableHeaders[i];
			tableControlInitPerson[i][1] = Boolean.TRUE;
		}
		return tableControlInitPerson;
	}

/**
 * createPersonTable(int nrOfColumns, Obj tableControl, HBProjectOpenData openProject)
 * @param nrOfColumns
 * @param tableControl
 * @param openProject
 * @return
 */
	public Object[][] createPersonTable(int nrOfColumns,
										   Object [][] tableControl,
										   HBProjectOpenData openProject) {
		this.tableControl = tableControl;
		if (HGlobal.TIME)
			HGlobalCode.timeReport("start building person table");

		try {

		// If first call of person select reload data
			if (openProject.personSelectData == null) {
				openProject.setReloadPersonSelectData(true);
			}

		// Reload set true
			if (openProject.getReloadPersonSelectData() || getChangedDateFormat()) {
				openProject.personSelectData = extractPersonData(personTableData, nrOfRows, nrOfColumns, openProject);

		// Person Select data loaded
				openProject.setReloadPersonSelectData(false);
				if (HGlobal.DEBUG)
					System.out.println("Open Project: " + openProject.getProjectName());

			} else if (updateMonitor) {
				progBar.setValue(100);
			}

			if (HGlobal.TIME)
				HGlobalCode.timeReport("end building person table");


			return openProject.personSelectData;
		} catch (SQLException | HBException exc) {
			if (HGlobal.DEBUG) {
				System.out.println("No data found in HRE database: " + exc.getMessage());
				exc.printStackTrace();
			}
			return null;
		} catch (Exception exc)	{
			if (HGlobal.DEBUG) {
				System.out.println("HBPersonHandler - unexpected Exception!\n Message: "
												+ exc.getMessage());
				exc.printStackTrace();
			}
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("HBPersonHandler - unexpected Exception:  " + exc.getMessage());
				HB0711Logging.printStackTraceToFile(exc);
			}
			JOptionPane.showMessageDialog(null,	"HBPersonHandler - unexpected Exception!\n Set DEBUG ON and"
					+ "\n See stack trace for more info!"
					+ "\nException message: " + exc.getMessage(),"Person Data",JOptionPane.INFORMATION_MESSAGE);
			return null;
		}
	}

/**
 * createSelectPersonList(int nrOfColumns, HBProjectOpenData openProject)
 *  Following routines are Mimimal (-Min) versions of the routines of the same name
 *  They are used by HG0507PersonSelectMin to quickly create people lists to allow
 *  selection of people for use as new parents or new Associates
 * @param nrOfColumns
 * @param openProject
 * @return
 */
	public Object[][] createSelectPersonList(int nrOfColumns, HBProjectOpenData openProject) {
		try {
		// If first call, reload select person list
			if (openProject.personSelectPersonList == null) {
				openProject.setReloadSelectPersonList(true);
			}
		// If Reload set true
			if (openProject.getReloadSelectPersonList() || getChangedDateFormat()) {
				openProject.personSelectPersonList = extractSelectPersonList(personTableData, nrOfRows, nrOfColumns, openProject);
			// Select person data loaded, set reload false
				openProject.setReloadSelectPersonList(false);
			}
			if (HGlobal.TIME) {
				HGlobalCode.timeReport("End processing person select person list");
			}
			return openProject.personSelectPersonList;

		} catch (SQLException | HBException exc) {
			if (HGlobal.DEBUG) {
				System.out.println("No data found in HRE database: " + exc.getMessage());
				exc.printStackTrace();
			}
			return null;
		} catch (Exception exc)	{
			if (HGlobal.DEBUG) {
				System.out.println("HBPersonHandler - unexpected Exception\n Message: "
												+ exc.getMessage());
				exc.printStackTrace();
			}
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("HBPersonHandler - unexpected Exception:  " + exc.getMessage());
				HB0711Logging.printStackTraceToFile(exc);
			}
			return null;
		}
	}

/**
 * initiatePersonDataMin
 * @param nrOfColumns
 * @param pointOpenProject
 * @throws HBException
 */
	public void initiatePersonDataMin(int nrOfColumns,
									  HBProjectOpenData pointOpenProject)
									  throws HBException  {
	// Reset HashMap for name data
		personDataIndex = new HashMap<>();

		if (HGlobal.TIME) {
			HGlobalCode.timeReport("Start processing person data (min)");
		}

	// Selected open project in the list
		pointSelectedProject = pointOpenProject;
		try {
			personTableData = pointOpenProject.getT401Persons();
			setUpDateTranslation();
			dateFormatSelect();
			int openDatabaseIndex = pointOpenProject.getOpenDatabaseIndex();
			personNameStyle = getNameStyleOutputCodes(nameStylesOutput, "N", openDatabaseIndex);
			createPersonDataList(personTableData, pointOpenProject);
			personTableData.last();
			nrOfRows = personTableData.getRow();
		// Check if nrOfRows > 0 ?
			if (nrOfRows == 0) {
				throw new HBException("No event data received from database");
			}

		} catch (SQLException | HBException exc) {
			throw new HBException("initiatePersonDataMin - SQL Exception: " + exc.getMessage());
		}
	}

/**
 * extractPersonDataMin(ResultSet hreTable)
 * @param personTable
 * @param nrOfRows
 * @param nrOfColumns
 * @param openProject
 * @throws SQLException
 * @throws HBException
 */
	private Object[][] extractSelectPersonList(ResultSet personTable,
										int nrOfRows,
										int nrOfColumns,
										HBProjectOpenData openProject ) throws SQLException, HBException {

		Object[][] personDataTable = new Object[nrOfRows][nrOfColumns];
		int row = 0;
		personSelectData = new ArrayList<>();

	// take each value from the HRE table separately
		personTable.beforeFirst();
	   	while (personTable.next()) {

    // Process table data
	   		long personPID = personTable.getLong("PID");
	   		long personTablePID = personTable.getLong("PID");
	   		PersonSelectData genData = personDataIndex.get(personPID);
	   		if (genData != null) {
	   		// Set personTablePID
	   			genData.setPersonTablePID(personTablePID);
	        // Set up an array list of person select data
	   			personSelectData.add(genData);
	   		// Initiate table
	   			personDataTable[row][0] = genData.getVisibleId();
	   			personDataTable[row][1] = genData.getName();
		   		personDataTable[row][2] = genData.getBornDate();
		   		personDataTable[row][3] = genData.getDeathDate();
	   		}
			row++;
		}
		return personDataTable;
	}

/**
 * initiatePersonData
 * @param nrOfColumns
 * @param pointOpenProject
 * @return Object[][]
 * @throws HBException
 */
	public void initiatePersonData(int nrOfColumns,
									  HBProjectOpenData pointOpenProject)
									  throws HBException  {
		Object[][] tableData = null;

	// Reset HashMap for name data
		personDataIndex = new HashMap<>();

		if (HGlobal.TIME) {
			HGlobalCode.timeReport("initiate processing person data");
		}

	// Selected open project in the list
		pointSelectedProject = pointOpenProject;
		if (HGlobal.DEBUG)
			System.out.println("Selected Project: " + pointSelectedProject.getProjectName());


		try {
			personTableData = pointOpenProject.getT401Persons();
			setUpDateTranslation();
			dateFormatSelect();
			int openDatabaseIndex = pointOpenProject.getOpenDatabaseIndex();
			personNameStyle =  getNameStyleOutputCodes(nameStylesOutput, "N", openDatabaseIndex);

		// processEventsPlace(openProject) modified to only partner
			createPersonDataList(personTableData, pointOpenProject);

		// Test time consumption calling last() in ResultSet
			//HGlobal.timeReport("Time set before person table last()");
			personTableData.last();
			nrOfRows = personTableData.getRow();

		// Check if nrOfRows > 0 ?
			if (nrOfRows <= 0) {
				if (HGlobal.DEBUG)
					System.out.println("No data in person table ResultSet! ");

				throw new HBException("No event data received from database!");
			}
			if (HGlobal.DEBUG)
				System.out.println("Number of rows/cols: " + nrOfRows
						+ "/" + nrOfColumns);

		   	if (HGlobal.DEBUG)
				writeDataTable(tableData);


		   	if (HGlobal.TIME)
				HGlobalCode.timeReport("end processing person data");


		} catch (SQLException | HBException exc) {
			throw new HBException("initiatePersonData - SQL Exception: " + exc.getMessage());
		}
	}


/**
 * createPersonDataList(ResultSet personTable, HBProjectOpenData openProject)
 * @param personTable
 * @param openProject
 * @throws SQLException
 * @throws HBException
 */
	private void createPersonDataList(ResultSet personTable, HBProjectOpenData openProject) throws SQLException, HBException {

		long hdateRPID;
		String bornDate = "", deathDate = "";
		int openDatabaseIndex = openProject.getOpenDatabaseIndex();

		locationNameStyle =  getNameStyleOutputCodes("P", openDatabaseIndex);

		if (HGlobal.DEBUG) {
			System.out.println("Dateformat: " + dateFormatIndex + " / " + HGlobal.dateFormat);
			System.out.println( "Date format index: " + dateFormatIndex);
		}

	// process nameTable data
		personTable.beforeFirst();

	// Collect name, death and birth data from T402_LIFE_FORM_NAMES into GenealogyData objects
		while (personTable.next()) {

	// set progress bar in Location Select
			if (updateMonitor) {
				progBar.setValue(calcPercent(countStatusBar, maxRows));
			}

	// Count rows for percentage status bar
		   	countStatusBar++;

			String personName = "";
			long name_RPID = personTable.getLong(bestNameField);

			if (name_RPID != null_RPID) {
				personName = pointLibraryResultSet.selectPersonName(name_RPID,
															openDatabaseIndex, personNameStyle);

    		} else {
				System.out.println(" ERROR HBPersonHandler - createPersonDataList - name_RPID: " + name_RPID);
			}

			if (HGlobal.DEBUG)
				System.out.println(name_RPID + " Person name : " + personName);


		// Extract HDATA START from T751
			hdateRPID = personTable.getLong(personHDateBirthField);
			bornDate = pointLibraryResultSet.exstractDate(hdateRPID, openDatabaseIndex).trim();

		//Extract HDATA END from T751
			hdateRPID = personTable.getLong(personHdateDeathField);
			deathDate = pointLibraryResultSet.exstractDate(hdateRPID, openDatabaseIndex).trim();

/*
 * 	Note T551_LOCN and  T552_LOCN_NAME have identical PID.
 * 	If each location have more than one location name this must
 * 	include also a T551_LOCN lookup to find BEST_NAME_RPID
 *  and use this locationNamePID in pointLibraryResultSet.selectLocationName(...........
 */

			long birthPlacePID = personTable.getLong(personLocnBirthField);
			long deathPlacePID = personTable.getLong(personLocnDeathField);
			String bornPlace = pointLibraryResultSet.selectLocationName(birthPlacePID,
									locationNameStyle, openDatabaseIndex);

			//System.out.println(" Born Place" + bornPlace + " / PID: " + birthPlacePID);

			String deathPlace = pointLibraryResultSet.selectLocationName(deathPlacePID,
					locationNameStyle, openDatabaseIndex);

			long personPID = personTable.getLong("PID");

			PersonSelectData genData = new PersonSelectData(personName, bornDate, bornPlace, deathDate, deathPlace);
			int visibleId = (int) personTable.getLong("VISIBLE_ID");
			genData.setVisibleId(visibleId);

			if (!personDataIndex.containsKey(personPID)) {
				personDataIndex.put(personPID, genData);
			}
		}
	}

/**
 * processPartnerList(HBProjectOpenData pointOpenProject)
 * @param openProject
 * @throws HBException
 */
	public void processPartnerList(HBProjectOpenData pointOpenProject) throws HBException {

		ResultSet partnerTableSelection;
		partnerMap = new HashMap<>();
		pointSelectedProject = pointOpenProject;
		int openDatabaseIndex = pointOpenProject.getOpenDatabaseIndex();

		try {

		// Reset Count rows
			countStatusBar = 0;
		// Find number of rows in person table
			long nrOfRowsInTable = 0;
			nrOfRowsInTable = numberOfTableRows(personTable, openDatabaseIndex);

	// Estimate number of processed rows for PersonSelect progressbar
			maxRows = (int) (nrOfRowsInTable * numberofProcesses);

			if (HGlobal.TIME)
				HGlobalCode.timeReport("start processing partners");


			String marriSelect = setSelectSQL("*", personPartnerTable, "");
			partnerTableSelection = requestTableData(marriSelect, openDatabaseIndex);

			if (HGlobal.DEBUG) {
				partnerTableSelection.last();
				int marriedRows = partnerTableSelection.getRow();
				System.out.println(" Death events: " + marriedRows);
			}

		// Processing marriages places and set up partner index
			partnerTableSelection.beforeFirst();
			while (partnerTableSelection.next()) {

		// set progress bar in Location Select
				if (updateMonitor) {
					progBar.setValue(calcPercent(countStatusBar, maxRows));
				}

		// Count rows for percentage status bar
			   	countStatusBar++;

				long priPartnerPID = partnerTableSelection.getLong("PRI_PARTNER_RPID");
				long secPartnerPID = partnerTableSelection.getLong("SEC_PARTNER_RPID");

		// New processing of partners
				Vector<Long> partners;
				if (priPartnerPID != null_RPID) {
					if (!partnerMap.containsKey(priPartnerPID)) {
						partners = new Vector<>();
						partners.add(secPartnerPID);
						partnerMap.put(priPartnerPID,partners);
					} else {
						partners = partnerMap.get(priPartnerPID);
						if (!partners.contains(secPartnerPID)) {
							partners.add(secPartnerPID);
						}
					}
				}

				if (secPartnerPID != null_RPID) {
					if (!partnerMap.containsKey(secPartnerPID)) {
						partners = new Vector<>();
						partners.add(priPartnerPID);
						partnerMap.put(secPartnerPID, partners);
					} else {
						partners = partnerMap.get(secPartnerPID);
						if (!partners.contains(priPartnerPID)) {
							partners.add(priPartnerPID);
						}
					}
				}
			}

			if (HGlobal.TIME)
				HGlobalCode.timeReport("end processing partners");


		} catch (HBException hbe) {
			throw new HBException("processEventsPlace PersonSelect data error: "
					+ hbe.getMessage());
		} catch (SQLException sqle) {
			throw new HBException("processEventsPlace PersonSelect data error: "
					+ sqle.getMessage());
		}
	}

/**
 * extractPersonData(ResultSet hreTable)
 * @param personTable
 * @param nrOfRows
 * @param nrOfColumns
 * @param openProject
 * @throws SQLException
 * @throws HBException
 */
	private Object[][] extractPersonData(ResultSet personTable,
										int nrOfRows,
										int nrOfColumns,
										HBProjectOpenData openProject ) throws SQLException, HBException {

		Object[][] personDataTable = new Object[nrOfRows][nrOfColumns];
		int row = 0;

		personSelectData = new ArrayList<>();

	// take each value from the HRE table separately
		personTable.beforeFirst();
	   	while (personTable.next()) {

	// set progress bar in Location Select
	   		if (updateMonitor) {
				progBar.setValue(calcPercent(countStatusBar, maxRows));
			}

	// Count rows for percentage status bar
		   	countStatusBar++;

    // Process table data
	   		long personPID = personTable.getLong("PID");
	   		long bestImagePID = personTable.getLong(bestImage);
	   		long personTablePID = personTable.getLong("PID");
	   		if (HGlobal.DEBUG)
				System.out.println(row + " - Person PID: " + personPID);

	   		PersonSelectData genData = personDataIndex.get(personPID);
	   		int index = 2;
	   		if (genData != null) {

	   		// Set personTablePID
	   			genData.setPersonTablePID(personTablePID);

	        // Set up an array list of person select data
	   			personSelectData.add(genData);

	   		// Initiate table
	   			//personDataTable[row][0] = (int) personTable.getLong(visibleId);
	   			personDataTable[row][0] = genData.getVisibleId();

	   			if (bestImagePID != null_RPID) {
					personDataTable[row][1] = genData.getName() + " *";
				} else {
					personDataTable[row][1] = genData.getName();
				}

		   		if ((boolean) (tableControl[2][1])) {
		   			personDataTable[row][index] = genData.getBornDate();
		   			index++;
		   		}
		   		if ((boolean) (tableControl[3][1])) {
		   			personDataTable[row][index] = genData.getBornPlace();
		   			index++;
		   		}
		   		if ((boolean) (tableControl[4][1])) {
		   			personDataTable[row][index] = genData.getDeathDate();
		   			index++;
		   		}
		   		if ((boolean) (tableControl[5][1])) {
		   			personDataTable[row][index] = genData.getDeathPlace();
		   		}

	   		} else {
	   			personDataTable[row][0] = 99999;
		   		personDataTable[row][1] = " ** No name found **";
		   		personDataTable[row][2] = " ";
		   		personDataTable[row][3] = " ";
		   		personDataTable[row][4] = " ";
		   		personDataTable[row][5] = " ";
		   		System.out.println("Extracted PersonData - gendata == null: " + row);
	   		}
			row++;
		}
		   	if (HGlobal.DEBUG)
				System.out.println("Extracted PersonData - row: " + row);


		// Set final value for progress bar
		   	if (updateMonitor) {
				progBar.setValue(100);
			}

		return personDataTable;
	}

/**
 * writeDataTable(Object[][] data)
 * @param data
 */
	private void writeDataTable(Object[][] data) {
		System.out.println(" *** writeDataTable() selected project: " + pointSelectedProject.getProjectName());
		if (data != null) {
			for (int i = 0; i < 5; i++) {
				System.out.print("Nr: " + i + " - " );
				for (int j = 0; j < 6; j++) {
					System.out.print(data[i][j] + " / ");
				}
				System.out.println();
			 }
		} else {
			System.out.println("convertHREdata SQL Exception - no data in ResultSet!" );
		}
	}

/**
 * activateManagePeople called from main
 * @param pointOpenProject
 * @return int errorCode
 */
	public int activateManagePerson(HBProjectOpenData pointOpenProject) {
		if (HGlobal.DEBUG)
			System.out.println(" Main menu initiate ");

		int errorCode = 0;

		try {
			String windowId = "50600";
			long personTablePID = pointOpenProject.pointGuiData.getTableViewPointPID(windowId);

			if (personTablePID != null_RPID ) {
				errorCode = initiateManagePerson(pointOpenProject, personTablePID, windowId);
				if (HGlobal.DEBUG) {
					System.out.println(" Main activateManagePerson - Valid: "  //$NON-NLS-1$
							+ pointOpenProject.getProjectName()
								+ " ID: " + windowId + " Person PID: " + personTablePID);	 //$NON-NLS-1$
				}
				return errorCode;
			}
			if (HGlobal.DEBUG) {
				System.out.println(" Main activateManagePerson - First: "  //$NON-NLS-1$
						+ pointOpenProject.getProjectName()
						+ " ID: " + windowId + " Person PID: " + personTablePID);
			}
// Find the PID for first person in table
			long firstPersonPID = firstRowPID(personTable, pointOpenProject);
			errorCode = initiateManagePerson(pointOpenProject, firstPersonPID, windowId);

// Set table PID for first person
			pointOpenProject.pointGuiData.setTableViewPointPID(windowId,firstPersonPID);

		} catch (HBException hbe) {
			if (HGlobal.DEBUG)
				System.out.println("Main Manage Person error:\n" + hbe.getMessage()); //$NON-NLS-1$

		// Temp error message waiting for NLS implementation
			JOptionPane.showMessageDialog(null,	"Main Manage Person error:\n"
					+ hbe.getMessage(),"Person VP",JOptionPane.INFORMATION_MESSAGE);
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Main Manage Person error: " + hbe.getMessage());
				HB0711Logging.printStackTraceToFile(hbe);
			}
			return 1;
		}
		return 0;
	}

/**
 * initiateManagePerson(HBProjectOpenData pointOpenProject
							,long personTablePID
							,String screenId)
 * @param pointOpenProject
 * @param personTablePID
 * @param screenId
 * @return int errorcode
 */
	public int initiateManagePerson(HBProjectOpenData pointOpenProject
							,long personTablePID
							,String screenID) {

		int errorCode;

		setUpDateTranslation();
		dateFormatSelect();

		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		this.resultSetT401_PERSON = pointOpenProject.getT401Persons();

		if (HGlobal.DEBUG)
			System.out.println(" InitiateManagePerson - Person table PID: " + personTablePID);


	// find pointer to PersonManager for each project
		managePersonScreen = pointOpenProject.getManagePersonPointer();

	// Close and Dispose HG0506ManagePerson window if already created
		if (managePersonScreen != null) {
			if (HGlobal.DEBUG)
				System.out.println( "Person Manager exists - PID: " + personTablePID);


			pointOpenProject.closeStatusScreen(screenID);

		// close reminder display
			if (managePersonScreen.reminderDisplay != null) {
				managePersonScreen.reminderDisplay.dispose();
			}

	    // Set frame size in GUI data
			Dimension frameSize = managePersonScreen.getSize();
			pointOpenProject.setSizeScreen(screenID,frameSize);

		// Set position	in GUI data
			Point position = managePersonScreen.getLocation();
			pointOpenProject.setPositionScreen(screenID,position);

		// Set class name in GUI configuration data
			pointOpenProject.setClassName(screenID,"HG0506ManagePerson"); //$NON-NLS-1$

		// Mark the screen as closed in T302 and remove open screeen
			pointOpenProject.closeStatusScreen(screenID);

		// Close screen
			managePersonScreen.dispose();

		} else
			if (HGlobal.DEBUG)
				System.out.println( "Person Manager closed - PID: " + personTablePID);


		try {

		// Initiate Person Manage Data
			pointManagePersonData = new ManagePersonData(pointDBlayer, dataBaseIndex, pointOpenProject);

		// Move nameDisplayIndex from HBProjectOpendata to ManagePersonData
			pointManagePersonData.setNameDisplayIndex(pointOpenProject.getNameDisplayIndex());

		// Load data from database(dataBaseIndex) into ManagePersonData
			errorCode = pointManagePersonData.updateManagePersonTable(personTablePID);

		// Select date format
			dateFormatSelect();
			if (HGlobal.DEBUG)
				System.out.println(" Dateformat: " + dateFormatIndex + " / " + HGlobal.dateFormat);


			HG0401HREMain.mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			managePersonScreen = new HG0506ManagePerson(this, pointOpenProject, screenID, personTablePID);

		// store pointer to PersonManager for each project
			pointOpenProject.setManagePersonPointer(managePersonScreen);

		// Set size for frame
			Dimension frameSize = pointOpenProject.pointGuiData.getFrameSize(HG0506ManagePerson.screenID);
			if (frameSize != null) {
				managePersonScreen.setSize(frameSize);
			}

		// Set location for frame
			Point location = pointOpenProject.pointGuiData.getFramePosition(HG0506ManagePerson.screenID);
			if (location == null) {
		// Sets screen top-left corner relative to parent window
				managePersonScreen.setLocation(0, 0);
				  if (HGlobal.DEBUG)
					System.out.println(" initiateManagePerson - initiate location: " + HG0506ManagePerson.screenID);

			} else {
				 managePersonScreen.setLocation(location);
				 if (HGlobal.DEBUG)
					System.out.println(" initiateManagePerson - reset location: " + HG0506ManagePerson.screenID);
			}


		// Set visible PID in GUI config
			pointOpenProject.pointGuiData.setTableViewPointPID(screenID, personTablePID);

		  	managePersonScreen.toFront();
		  	HG0401HREMain.mainFrame.setCursor(Cursor.getDefaultCursor());

	// Mark the window as open in the T302 table
		  	pointOpenProject.pointGuiData.setOpenStatus(HG0506ManagePerson.screenID,
		  				pointOpenProject.pointT302_GUI_CONFIG,true);

	// Set class name in GUI config data
		  	pointOpenProject.setClassName(HG0506ManagePerson.screenID, managePersonScreen.getClassName());

	// register the open screen on HBOpenprojectData

		  	pointOpenProject.registerOpenScreen(managePersonScreen);

	// Set selected person from Person Manager
		  	pointOpenProject.setSelectedPersonPID(personTablePID);

		  	if (HGlobal.DEBUG)
				System.out.println(" initiateManagePerson: " + managePersonScreen.getClassName() + " Errorcode: " + errorCode);


		  	return errorCode;

		} catch (HBException hbe) {
			if (HGlobal.DEBUG)
				System.out.println(" Initiate ManagePerson error: \n" + hbe.getMessage());


		// Temp error message waiting for NLS implementation
			JOptionPane.showMessageDialog(null,	" Initiate ManagePerson error:\n"
					+ hbe.getMessage(),"Manage Person",JOptionPane.INFORMATION_MESSAGE);
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite(" Initiate ManagePerson error: " + hbe.getMessage());
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
	public HG0525ManagePersonNameStyles activatePersonNameStyle(HBProjectOpenData pointOpenProject) {
		HG0525ManagePersonNameStyles personStyleScreen;
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		pointStyleData =  new HBNameStyleManager(pointDBlayer, dataBaseIndex, "Person ");
		try {
			pointStyleData.updateStyleTable("N", nameStyles,  nameElementsDefined);
			personStyleScreen = new HG0525ManagePersonNameStyles(pointOpenProject, pointStyleData);
			return personStyleScreen;
		} catch (HBException hbe) {
			System.out.println("HBPersonHandler - activatePersonNameStyles: " + hbe.getMessage());
			hbe.printStackTrace();
		}
		return null;
	}

/**
 * activateAssociateAdd(HBProjectOpenData pointOpenProject)
 * @param pointOpenProject
 * @return HG0507PersonSelectMin
 * @throws HBException
 */
	public HG0507SelectPerson activateAssociateAdd(HBProjectOpenData pointOpenProject, HG0547EditEvent pointerEditEvent, 
			int eventNumber) throws HBException {
		HG0507SelectPerson pointSelectAssociate = null;
		try {
			pointAddPersonRecord = new AddPersonRecord(pointDBlayer, pointOpenProject);
			pointSelectAssociate = new HG0507SelectAssociate(this, pointOpenProject, eventNumber, -1, true);
			if (pointerEditEvent == null) 
				System.out.println("HBPersonHandler - activateAssociateAdd - pointEdotevent = null");
			pointSelectAssociate.pointEditEvent = pointerEditEvent;
			return pointSelectAssociate;
		} catch (HBException hbe) {
			System.out.println("HBPersonHandler - activateAssociateAdd: " + hbe.getMessage());
			hbe.printStackTrace();
			throw new HBException(" Associate add error!" + hbe.getMessage());
		}
	}

/**
 * activateAssociateAdd(HBProjectOpenData pointOpenProject)
 * @param pointOpenProject
 * @return HG0507PersonSelectMin
 * @throws HBException
 */
	public HG0507SelectPerson activateAssociateEdit(HBProjectOpenData pointOpenProject, HG0547EditEvent pointerEditEvent,
								int eventNumber, int rowInTable) throws HBException {
		HG0507SelectPerson pointSelectAssociate = null;
		try {
			pointAddPersonRecord = new AddPersonRecord(pointDBlayer, pointOpenProject);
			pointSelectAssociate = new HG0507SelectAssociate(this, pointOpenProject, eventNumber, rowInTable, false);
			if (pointerEditEvent == null) 
				System.out.println("HBPersonHandler - activateAssociateAdd - pointEdotevent = null");
			pointSelectAssociate.pointEditEvent = pointerEditEvent;
			pointSelectAssociate.setEditMode();
			((HG0507SelectAssociate) pointSelectAssociate).setAssocRoleEdit();
			return pointSelectAssociate;
		} catch (HBException hbe) {
			System.out.println("HBPersonHandler - activateAssociateEdit: " + hbe.getMessage());
			hbe.printStackTrace();
			throw new HBException(" Associate edit error!");
		}
	}

/**
 * activatePartnerAdd(HBProjectOpenData pointOpenProject)
 * @param pointOpenProject
 * @return HG0507PersonSelectMin
 * @throws HBException
 */
	public HG0507SelectPerson activatePartnerAdd(HBProjectOpenData pointOpenProject) throws HBException {
		HG0507SelectPerson pointSelectPartner = null;
		try {
			pointAddPersonRecord = new AddPersonRecord(pointDBlayer, pointOpenProject);
			//nameSelectedPerson = getManagedPersonName();
			pointSelectPartner = new HG0507SelectPartner(this, pointOpenProject, -1, true);
			return pointSelectPartner;
		} catch (HBException hbe) {
			System.out.println("HBPersonHandler - activatePartnerSelec: " + hbe.getMessage());
			hbe.printStackTrace();
			throw new HBException(" Partner select error!");
		}
	}


/**
 * activatePartnerEdit(HBProjectOpenData pointOpenProject, int rowInTable)
 * @param pointOpenProject
 * @return HG0507PersonSelectMin
 * @throws HBException
 */
	public HG0507SelectPerson activatePartnerEdit(HBProjectOpenData pointOpenProject, int rowInTable) throws HBException {
		HG0507SelectPerson pointSelectParent = null;
		try {
			pointAddPersonRecord = new AddPersonRecord(pointDBlayer, pointOpenProject);
			pointSelectParent = new HG0507SelectPartner(this, pointOpenProject, rowInTable, false);
			pointSelectParent.setEditMode();
			((HG0507SelectPartner) pointSelectParent).setEditPartnerRole();
			return pointSelectParent;
		} catch (HBException hbe) {
			System.out.println("HBPersonHandler - activatePartnerEdit: " + hbe.getMessage());
			hbe.printStackTrace();
			throw new HBException(" Partner select error!");
		}
	}

/**
 * activateParentEdit(HBProjectOpenData pointOpenProject)
 * @param pointOpenProject
 * @return HG0507PersonSelectMin
 * @throws HBException
 */
	public HG0507SelectPerson activateParentEdit(HBProjectOpenData pointOpenProject,
						int selectedRowInTable) throws HBException {
		HG0507SelectPerson pointSelectParent = null;
		try {
			pointAddPersonRecord = new AddPersonRecord(pointDBlayer, pointOpenProject);
		// No parent relation data available
			if (getParentTableData(selectedRowInTable) == null)
				throw new HBException(" No Parent available for edit!");
			pointSelectParent = new HG0507SelectParent(this, pointOpenProject, selectedRowInTable, false);
		// Set edit mode for HG0507SelectPerson
			pointSelectParent.setEditMode();
		// Set parent role in combo according to database
			((HG0507SelectParent) pointSelectParent).setEditParentRole();
			return pointSelectParent;
		} catch (HBException hbe) {
		// Attempt to edit parent relation without table recording
			if (hbe.getMessage().contains("No Parent")) throw new HBException(hbe.getMessage());
		// Errror detected
			System.out.println("HBPersonHandler - activateParentEdit: " + hbe.getMessage());
			hbe.printStackTrace();
			throw new HBException(" HBPersonHandler - activateParentEdit - Parent select error!\n" + hbe.getMessage());
		}
	}
/**
 * activateParentAdd(HBProjectOpenData pointOpenProject)
 * @param pointOpenProject
 * @return HG0507PersonSelectMin
 * @throws HBException
 */
	public HG0507SelectPerson activateParentAdd(HBProjectOpenData pointOpenProject) throws HBException {
		HG0507SelectPerson pointSelectParent = null;
		try {
			pointAddPersonRecord = new AddPersonRecord(pointDBlayer, pointOpenProject);
			pointSelectParent = new HG0507SelectParent(this, pointOpenProject, -1, true);
			return pointSelectParent;
		} catch (HBException hbe) {
			System.out.println("HBPersonHandler - activateParentSelect: " + hbe.getMessage());
			hbe.printStackTrace();
			throw new HBException(" Parent select error!");
		}
	}

/**
 * activatePersonNameAdd(HBProjectOpenData pointOpenProject)
 * @param pointOpenProject
 * @return
 * @throws HBException
 */
	public  HG0509ManagePersonName activatePersonNameAdd(HBProjectOpenData pointOpenProject, long personTablePID) throws HBException {
		HG0509ManagePersonName pointManagePersonNameScreen = null;
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();

		// Load data from database(dataBaseIndex) into ManagePersonNameData
		pointManagePersonNameData = new ManagePersonNameData(pointDBlayer, pointOpenProject);
		pointManagePersonNameData.setNameStyleIndex(getDefaultIndex());
		pointAddPersonRecord = new AddPersonRecord(pointDBlayer, pointOpenProject);
		pointOpenProject.getWhereWhenHandler().pointEditEventRecord =
				new EditEventRecord(pointDBlayer, dataBaseIndex, pointOpenProject, null_RPID);
		try {
	 //   Initiate add person name
			pointManagePersonNameScreen = new HG0509AddPersonName(this, pointOpenProject, personTablePID);
			return pointManagePersonNameScreen;
		} catch (HBException hbe) {
			System.out.println("HBPersonHandler - activatePersonNameAdd: " + hbe.getMessage());
			hbe.printStackTrace();
			throw new HBException("HBPersonHandler - activatePersonNameAdd: " + hbe.getMessage());
		}
	}

/**
 * activatePersonNameEdit(HBProjectOpenData pointOpenProject, long personTablePID, int nameIndex)
 * @param pointOpenProject
 * @return HG0507PersonSelectMin
 * @throws HBException
 */
	public  HG0509ManagePersonName activatePersonNameEdit(HBProjectOpenData pointOpenProject,
													int nameTableIndex, boolean nameTable) throws HBException {
		long personNamePID = null_RPID;

		HG0509ManagePersonName pointManagePersonNameScreen = null;
		//int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();

	// Load data from database(dataBaseIndex) into ManagePersonNameData
		pointManagePersonNameData = new ManagePersonNameData(pointDBlayer, pointOpenProject);
		pointManagePersonNameData.setNameStyleIndex(getDefaultIndex());
		pointAddPersonRecord = new AddPersonRecord(pointDBlayer, pointOpenProject);

	// If name table or event table
		if (nameTable) {
			personNamePID = pointManagePersonData.getPersonNameTablePID(nameTableIndex);
		} else {
			personNamePID = pointManagePersonData.getEventPID(nameTableIndex);
		}

		if (HGlobal.DEBUG)
			System.out.println(" activatePersonNameEdit: " + personNamePID);


		pointManagePersonNameData.updateManagePersonNameTable(personNamePID);
		pointManagePersonNameScreen = new HG0509EditPersonName(this, pointOpenProject, personNamePID);
		return pointManagePersonNameScreen;
	}

/**
 * HG0505AddPerson activateAddPerson(HBProjectOpenData pointOpenProject, int sexIndex)
 * @param pointOpenProject
 * @param sexIndex
 * @return
 */
	public HG0505AddPerson activateAddPerson(HBProjectOpenData pointOpenProject, int sexIndex) {
		HG0505AddPerson pointAddPerson = null;
		String namePerson = " No person";
		dateFormatSelect();
		try {
			pointAddPersonRecord = new AddPersonRecord(pointDBlayer, pointOpenProject);
			pointAddPerson = new HG0505AddPerson(pointOpenProject, this, sexIndex, true, namePerson);
			return pointAddPerson;
		} catch (HBException hbe) {
			System.out.println("HBPersonHandler - activateAddPerson: " + hbe.getMessage());
			hbe.printStackTrace();
		}
		return null;
	}

/**
 * HG0505AddPerson activateAddPersonChild(HBProjectOpenData pointOpenProject, int sexIndex)
 * @param pointOpenProject
 * @param sexIndex
 * @return
 */
	public HG0505AddPerson activateAddPersonChild(HBProjectOpenData pointOpenProject, int sexIndex) {
		HG0505AddPerson pointAddPerson = null;
		String namePerson = " No person";
		try {
			pointAddPersonRecord = new AddPersonRecord(pointDBlayer, pointOpenProject);
			namePerson = getManagedPersonName();
			pointAddPerson = new HG0505AddPersonChild(pointOpenProject, this, sexIndex, namePerson);
			return pointAddPerson;
		} catch (HBException hbe) {
			System.out.println("HBPersonHandler - activateAddPersonChild: " + hbe.getMessage());
			hbe.printStackTrace();
		}
		return null;
	}

/**
 * HG0505AddPerson activateAddPersonParent(HBProjectOpenData pointOpenProject, int sexIndex)
 * @param pointOpenProject
 * @param sexIndex
 * @return
 */
	public HG0505AddPerson activateAddPersonParent(HBProjectOpenData pointOpenProject, int sexIndex) {
		HG0505AddPerson pointAddPerson = null;
		String namePerson = " No person";
		try {
			pointAddPersonRecord = new AddPersonRecord(pointDBlayer, pointOpenProject);
			namePerson = getManagedPersonName();
			pointAddPerson = new HG0505AddPersonParent(pointOpenProject, this, sexIndex, namePerson);
			return pointAddPerson;
		} catch (HBException hbe) {
			System.out.println("HBPersonHandler - activateAddPersonChild: " + hbe.getMessage());
			hbe.printStackTrace();
		}
		return null;
	}



/**
 * HG0505AddPerson activateAddPersonPartner(HBProjectOpenData pointOpenProject, int sexIndex)
 * @param pointOpenProject
 * @param sexIndex
 * @return
 */
	public HG0505AddPerson activateAddPersonPartner(HBProjectOpenData pointOpenProject, int sexIndex) {
		HG0505AddPerson pointAddPerson = null;
		String namePerson = " No person";
		try {
			pointAddPersonRecord = new AddPersonRecord(pointDBlayer, pointOpenProject);
			namePerson = getManagedPersonName();
			pointAddPerson = new HG0505AddPersonPartner(pointOpenProject, this, sexIndex, namePerson);
			return pointAddPerson;
		} catch (HBException hbe) {
			System.out.println("HBPersonHandler - activateAddPersonChild: " + hbe.getMessage());
			hbe.printStackTrace();
		}
		return null;
	}

/**
 * HG0505AddPerson activateAddPersonSibling(HBProjectOpenData pointOpenProject, int sexIndex)
 * @param pointOpenProject
 * @param sexIndex
 * @return
 */
	public HG0505AddPerson activateAddPersonSibling(HBProjectOpenData pointOpenProject, int sexIndex) {
		HG0505AddPerson pointAddPerson = null;
		String namePerson = " No person";
		try {
			pointAddPersonRecord = new AddPersonRecord(pointDBlayer, pointOpenProject);
			namePerson = getManagedPersonName();
			pointAddPerson = new HG0505AddPersonSibling(pointOpenProject,
								this, sexIndex, namePerson);
			return pointAddPerson;
		} catch (HBException hbe) {
			System.out.println("HBPersonHandler - activateAddPerson: " + hbe.getMessage());
			hbe.printStackTrace();
		}
		return null;
	}



/**
 * deletePersonInTable(long personPID, HBProjectOpenData pointOpenProject)
 * @param personPID
 * @param pointOpenProject
 * @throws HBException
 */
	public void deletePersonInTable(long personPID, HBProjectOpenData pointOpenProject) throws HBException {
		if (HGlobal.DEBUG)
			System.out.println(" HBPersonHandler - delete person: " + personPID);

		ResultSet personTableRS, personNameTableRS, personElementNameRS, parentTableRS, partnerTableRS;
		long bestNameRPID, nameOwner;
		String selectString;
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		try {
		// Check if last person in table
			personTableRS = pointOpenProject.getT401Persons();
			personTableRS.last();
			if (personTableRS.getRow() == 1) {
				throw new HBException("Not possible to delete last person!");
			}
		// Start transaction
			updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
			selectString = setSelectSQL("*", personTable,"PID = " + personPID);
			personTableRS = requestTableData(selectString, dataBaseIndex);
			personTableRS.first();
			bestNameRPID = personTableRS.getLong("BEST_NAME_RPID");
			selectString = setSelectSQL("*", personNameTable, "PID = " + bestNameRPID);
			personNameTableRS = requestTableData(selectString, dataBaseIndex);
			personNameTableRS.first();
			nameOwner = personNameTableRS.getLong("PID");
			selectString = setSelectSQL("*", personNamesTableElements,"OWNER_RPID= " + nameOwner);
			personElementNameRS = requestTableData(selectString, dataBaseIndex);
			personElementNameRS.beforeFirst();
			if (HGlobal.DEBUG)
				System.out.println(" Delete person PID: " + personTableRS.getLong("PID"));

			personTableRS.deleteRow();
			if (HGlobal.DEBUG)
				System.out.println(" Delete person name PID: " + personNameTableRS.getLong("PID"));

			personNameTableRS.deleteRow();
			while (personElementNameRS.next()) {
				if (HGlobal.DEBUG)
					System.out.println(" Delete name element PID: " + personElementNameRS.getLong("PID"));
				personElementNameRS.deleteRow();
			}

			personTableRS.close();
			personNameTableRS.close();
			personElementNameRS.close();

		// Reset father bio and mother bio
			selectString = setSelectSQL("*", personTable,"SPERM_PROVIDER_RPID  = " + personPID
					+ " OR EGG_PROVIDER_RPID  = " + personPID);
			personTableRS = requestTableData(selectString, dataBaseIndex);
			personTableRS.beforeFirst();
			while (personTableRS.next()) {
				long spermProvider = personTableRS.getLong(personFatherField);
				long eggProvider = personTableRS.getLong(personMotherField);
				if (spermProvider == personPID) {
					personTableRS.updateLong(personFatherField, null_RPID);
				}
				if (eggProvider == personPID) {
					personTableRS.updateLong(personMotherField, null_RPID);
				}
				personTableRS.updateRow();
			}
			personTableRS.close();

		// Remove both parent and child relations
			selectString = setSelectSQL("*", personParentTable,
					"PARENT_RPID  = " + personPID + " OR PERSON_RPID = " + personPID);
			parentTableRS = requestTableData(selectString, dataBaseIndex);
			parentTableRS.beforeFirst();
			while (parentTableRS.next()) {
				parentTableRS.deleteRow();
			}
			parentTableRS.close();

		// Remove partner relations
			selectString = setSelectSQL("*", personPartnerTable,
					"PRI_PARTNER_RPID  = " + personPID + " OR SEC_PARTNER_RPID = " + personPID);
			partnerTableRS = requestTableData(selectString, dataBaseIndex);
			partnerTableRS.beforeFirst();
			while (partnerTableRS.next()) {
				long partnerEventPID = partnerTableRS.getLong("EVNT_RPID");
				deletePartnerEvent(partnerEventPID);
				partnerTableRS.deleteRow();
			}
			partnerTableRS.close();

		// Delete all events for person
			deleteEventForPerson(personPID);

		// Now change the focus person PID because old focus is deleted
			long firstPersonPID = firstRowPID(personTable, pointOpenProject);
	        pointOpenProject.setSelectedPersonPID(firstPersonPID);

	        String windowId = "50600";
			pointOpenProject.pointGuiData.setTableViewPointPID(windowId, firstPersonPID);

			// End transaction
			updateTableData("COMMIT", dataBaseIndex);

		// Reload Result Set for person and names
			pointOpenProject.reloadT401Persons();
			pointOpenProject.reloadT402Names();

		// Reset Person Select
			resetPersonSelect();

		} catch (SQLException sqle) {
			updateTableData("ROLLBACK", dataBaseIndex);
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Error delete person - ROLLBACK:  " + sqle.getMessage());
				HB0711Logging.printStackTraceToFile(sqle);
			}
			throw new HBException("\n ERROR deletePersonInTable: " + sqle.getMessage());
		}
	}

/**
 * deleteParent(long personPID, int  dataBaseIndex)
 * @param personNamePID
 * @param dataBaseIndex
 * @throws HBException
 */
	public void deleteParent(HBProjectOpenData pointOpenProject, int  selectedRowParentTable) throws HBException {
		String selectString;
		ResultSet personTableRS, parentTableRS;
		Object[]  parentRelationData = null;
		long personPID;
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();

	// Find parent in parent table
		//long personPID = pointManagePersonData.getParentTablePID(selectedRowParentTable);
		parentRelationData = pointManagePersonData.getParentTableData(selectedRowParentTable);
		personPID = (long) parentRelationData[4];

	// Start transaction Reset father bio and mother bio
		updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
		selectString = setSelectSQL("*", personTable, "SPERM_PROVIDER_RPID  = " + personPID
									+ " OR EGG_PROVIDER_RPID  = " + personPID);
		personTableRS = requestTableData(selectString, dataBaseIndex);
		try {
			personTableRS.beforeFirst();
			while (personTableRS.next()) {
				long spermProvider = personTableRS.getLong(personFatherField);
				long eggProvider = personTableRS.getLong(personMotherField);
				if (spermProvider == personPID) {
					personTableRS.updateLong(personFatherField, null_RPID);
				}
				if (eggProvider == personPID) {
					personTableRS.updateLong(personMotherField, null_RPID);
				}
				personTableRS.updateRow();
			}
			personTableRS.close();

		// Remove both parent and child relations
			selectString = setSelectSQL("*", personParentTable,
					"PARENT_RPID  = " + personPID + " OR PERSON_RPID = " + personPID);
			parentTableRS = requestTableData(selectString, dataBaseIndex);
			parentTableRS.beforeFirst();
			while (parentTableRS.next()) {
				parentTableRS.deleteRow();
			}
			parentTableRS.close();

		// End transaction
			updateTableData("COMMIT", dataBaseIndex);

		// Reload Result Set for person and names
			pointOpenProject.reloadT401Persons();
			pointOpenProject.reloadT402Names();

		// Reset Person Select and person manager
			resetPersonSelect();
			resetPersonManager();

		} catch (SQLException sqle) {
		// Roll back transaction
			updateTableData("ROLLBACK", dataBaseIndex);
			System.out.println(" HBPersonHandler - deleteParent error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" HBPersonHandler - deleteParent: " + sqle.getMessage());
		}
	}

/**
 * deletePartner(HBProjectOpenData pointOpenProject, int  selectedRowParentTable)
 * @param pointOpenProject
 * @param selectedRowParentTable
 * @throws HBException
 */
	public void deletePartner(HBProjectOpenData pointOpenProject, int  selectedRowParentTable) throws HBException {
		ResultSet parnerTableRS;
		long partnerTablePID = pointManagePersonData.getPartnerPID(selectedRowParentTable);
		if (HGlobal.DEBUG)
			System.out.println(" HBPersonHandler - deletePartnerPID: " + partnerTablePID);

		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
	// Start transaction Reset father bio and mother bio
		updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
		String selectString = setSelectSQL("*", personPartnerTable, "PID = " + partnerTablePID);
		parnerTableRS = requestTableData(selectString, dataBaseIndex);
		try {
			parnerTableRS.first();
			parnerTableRS.deleteRow();
			parnerTableRS.close();
		// End transaction
			updateTableData("COMMIT", dataBaseIndex);
		// Reset Person Select and person manager
			resetPersonSelect();
			resetPersonManager();
		} catch (SQLException sqle) {
			// Roll back transaction
			updateTableData("ROLLBACK", dataBaseIndex);
			if (HGlobal.DEBUG)
				System.out.println(" Deleted partner with PID: " + partnerTablePID);

			sqle.printStackTrace();
			throw new HBException(" HBPersonHandler - deletePartner : " + sqle.getMessage());
		}
	}
/**
 * deleteEventForPerson(long assocPersonRPID)
 * @param assocPersonRPID
 * @throws HBException
 */
	private void deleteEventForPerson(long assocPersonRPID) throws HBException {
		// Delete existing events for person number
		ResultSet eventTableRS;
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
	// Start transaction
		updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
		String selectString = setSelectSQL("*", eventTable, "PRIM_ASSOC_RPID = " + assocPersonRPID);
		try {
			eventTableRS = requestTableData(selectString, dataBaseIndex);
			if (isResultSetEmpty(eventTableRS)) {
				return;
			}
			eventTableRS.beforeFirst();
			while (eventTableRS.next() ) {
				eventTableRS.deleteRow();
				if (HGlobal.DEBUG)
					System.out.println(" Deleted event and assoc for PID: " + assocPersonRPID);

			}
			eventTableRS.close();
		// End transaction
			updateTableData("COMMIT", dataBaseIndex);

		} catch (SQLException sqle) {
		// Roll back transaction
			updateTableData("ROLLBACK", dataBaseIndex);
			throw new HBException(" HBPersonHandler - deleteEventForPerson : " + sqle.getMessage());
		}
	}

/**
 * deleteNameForPerson(long personNameRPID)
 * @param personNameRPID
 * @throws HBException
 */
	public void deleteNameForPerson(int indexInTable) throws HBException {
		String selectString;
		ResultSet nameTableRS, nameTableElementRS;
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		long startNamePID, endNamePID;
		long personNamePID = pointManagePersonData.getPersonNameTablePID(indexInTable);

	// Start transaction
		updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
		selectString = setSelectSQL("*", personNameTable, "PID = " + personNamePID);
		nameTableRS = requestTableData(selectString, dataBaseIndex);
		try {
			nameTableRS.first();
			startNamePID = nameTableRS.getLong("START_HDATE_RPID");
			endNamePID = nameTableRS.getLong("END_HDATE_RPID");
			if (startNamePID != null_RPID) {
				deleteHdate(startNamePID);
			}
			if (endNamePID != null_RPID) {
				deleteHdate(endNamePID);
			}
			if (nameTableRS.getBoolean("NAME_PRIMARY")) {
				errorJOptionMessage("Delete person name", " Cannot delete primary person name!");
				nameTableRS.close();
				updateTableData("COMMIT", dataBaseIndex);
				return;
			}
			selectString = setSelectSQL("*", personNamesTableElements, "OWNER_RPID = " + personNamePID);
			nameTableElementRS = requestTableData(selectString, dataBaseIndex);
			nameTableElementRS.beforeFirst();
			while (nameTableElementRS.next() ) {

				nameTableElementRS.deleteRow();
				if (HGlobal.DEBUG)
					System.out.println(" Deleted person name element PID: " + nameTableElementRS);

			}

			nameTableElementRS.close();
			nameTableRS.deleteRow();
			nameTableRS.close();

			// End transaction
			updateTableData("COMMIT", dataBaseIndex);

		// Reset Person Select and person manager
			resetPersonManager();

		} catch (SQLException sqle) {
		// Roll back transaction
			updateTableData("ROLLBACK", dataBaseIndex);
			sqle.printStackTrace();
			throw new HBException(" HBPersonHandler - deleteNameForPerson : " + sqle.getMessage());
		}
	}

/**
 * deletePartnerEvent(long selectedEventRPID)
 * @param selectedEventRPID
 * @throws HBException
 */
	public void deletePartnerEvent(long selectedEventRPID) throws HBException {
		// Delete event PID
		ResultSet eventTableRS, partnertableRS;
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
	// Start transaction
		updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
		String selectString = setSelectSQL("*", eventTable, "PID = " + selectedEventRPID);
		try {
			eventTableRS = requestTableData(selectString, dataBaseIndex);
			if (isResultSetEmpty(eventTableRS)) {
				return;
			}
			eventTableRS.beforeFirst();
			while (eventTableRS.next() ) {
				eventTableRS.deleteRow();
				if (HGlobal.DEBUG)
					System.out.println(" Deleted partner event PID: " + selectedEventRPID);

			}
			eventTableRS.close();
		// Set partner event pointer to null_RPID
			selectString = setSelectSQL("*", personPartnerTable, "EVNT_RPID = " + selectedEventRPID);
			partnertableRS = requestTableData(selectString, dataBaseIndex);
			partnertableRS.first();
			partnertableRS.updateLong("EVNT_RPID", null_RPID);
			partnertableRS.updateRow();
			partnertableRS.close();

		// End transaction
			updateTableData("COMMIT", dataBaseIndex);

		} catch (SQLException sqle) {
		// Roll back transaction
			updateTableData("ROLLBACK", dataBaseIndex);
			sqle.printStackTrace();
			throw new HBException(" HBPersonHandler - deletePartnerEvent : " + sqle.getMessage());
		}
	}

/**
 * deleteHdate(long hdatePID)
 * @param hdatePID
 * @throws HBException
 */
	public void deleteHdate(long hdatePID) throws HBException {
		// Delete event PID
		ResultSet hdateTableRS;
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		String selectString = setSelectSQL("*", dateTable, "PID = " + hdatePID);
		try {
			hdateTableRS = requestTableData(selectString, dataBaseIndex);
			hdateTableRS.first();
			hdateTableRS.deleteRow();
			hdateTableRS.close();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" HBPersonHandler - deleteHdate : " + sqle.getMessage());
		}
	}

} // End class HBPersonHandler

/**
 * PersonSelectData (String personName, String bornDate, String deathDate)
 * @author NTo
 * @since 2020-04-28
 */
class PersonSelectData {
	String personName;
	String bornDate, deathDate;
	String bornPlace, deathPlace;
	long personTablePID;
	int visibleId;

/**
 * Constructor GenealogyData
 * @param personName
 * @param bornDate
 * @param deathDate
 */
    	public PersonSelectData(String personName, String bornDate, String bornPlace,
    			String deathDate, String deathPlace) {
    		this.personName = personName;
    		this.bornDate = bornDate;
    		this.bornPlace = bornPlace;
    		this.deathDate = deathDate;
    		this.deathPlace = deathPlace;
    	}

 /**
  * GET methods
  * @return
  */
    	public void setVisibleId(int value) {
    		visibleId = value;
    	}

    	public int getVisibleId() {
    		return visibleId;
    	}

    	public String getName() {
    		return " " + personName;
    	}

    	public String getBornDate() {
    		return " " + bornDate;
    	}

    	public String getBornPlace() {
    		return bornPlace;
    	}

       	public String getDeathDate() {
    		return " " + deathDate;
    	}

       	public String getDeathPlace() {
    		return deathPlace;
    	}

       	public void setPersonTablePID(long personTablePID) {
       		this.personTablePID = personTablePID;
       	}

       	public long getPersonTablePID() {
       		return personTablePID;
       	}

} // End PersonSelectData

/**
 * class ManagePersonData
 * @author NTo
 * @version v0.00.0025
 * @since 2020-11-16
 */
class ManagePersonData extends HBBusinessLayer {

	String dBbuild = HGlobal.databaseVersion;

	// Show witness in event list
	boolean includeWitnessEvents = true;
	boolean includeCildrenEvents = true;

	protected HBMediaHandler pointMediaHandler = null;
	protected HBProjectOpenData pointOpenProject;

	HashMap<Integer, Long> flagDefinMapPID;
	HashMap<Integer, Long> eventPIDhash;
	HashMap<Integer, Long> personNamePIDhash;
	HashMap<Integer, Object[]> parentDataHash;
	HashMap<Integer, Object[]>partnerDataHash;
/**
 * List over already included events
 */
	private Vector<Long> eventDuplicateList;

	long proOffset = 1000000000000000L;
	long null_RPID  = 1999999999999999L;

	private String[] locationNameStyle;

	int personImage = 0;
	long selectPersonPID, personNamePID;
	long nameStyleRPID;
	long selectedPersonPID; // Stores the selected person PID

	String personName = " Person name";
	int dataBaseIndex = 0;
	int nrRows = 0, row = 0, nameRows = 0, eventRows = 0, partnerRows = 0,
		associateRows = 0, children = 0, marriages = 0;
	String reference;
	ArrayList<String[]> grandChildList;
	ArrayList<ImageIcon> listOfImages;
	ArrayList<Object[]> associateList;

	private boolean openVPstate = false;

	String[] sortStrings;
	JLabel pictLabel;
	ImageIcon image;

// Setting of style for summary events
	String[] styleNameAndDates;
	private String[] personStyle;

	Object [][] parentTable = {
		    {" Father ", null, "  - - - - -"},
			{" Mother ", null, "  - - - - -"}};

	Object [][] eventsTable;
	Object [][] partnerTable;
	Object [][] associateTable;
	Object [][] nameTable;
	Object [][] personFlagTable;
	String[] tableFlagDescript;

	public ManagePersonData(HDDatabaseLayer pointDBlayer, int dataBaseIndex, HBProjectOpenData pointOpenProject) {
		super();
		this.pointDBlayer = pointDBlayer;
		this.dataBaseIndex = dataBaseIndex;
		this.pointOpenProject = pointOpenProject;
		dateFormatSelect();
		setUpDateTranslation();
		pointMediaHandler = pointOpenProject.getMediaHandler();

		if (HGlobal.DEBUG)
			System.out.println("Dateformat: "
					+ dateFormatIndex + " / " + HGlobal.dateFormat);

	}

/**
 * setWinessState(boolean state)
 * @param state
 * @throws HBException
 */
	public void setWinessState(boolean state) throws HBException {
        if (HGlobal.DEBUG) {
			if (state) {
				System.out.println(" Witness ON");
			} else {
				System.out.println(" Witness OFF");
			}
		}
        includeWitnessEvents = state;
        prepareEventTable();
	}

/**
 * setChildrenState(boolean state)
 * @param state
 * @throws HBException
 */
	public void setChildrenState(boolean state) throws HBException {
		if (HGlobal.DEBUG) {
			if (state) { System.out.println(" Children ON");}
        	else {System.out.println(" Children OFF");}
		}
		includeCildrenEvents = state;
		prepareEventTable();
	}

	public ImageIcon getExhibitImage() {
		return image;
	}

	public ArrayList<ImageIcon> getImageList() {
		return listOfImages;
	}

	public int getNumberOfImages() {
		if (listOfImages == null) {
			return 0;
		}
		return listOfImages.size();
	}

	public String[] getNameData()  {
		return styleNameAndDates;
	}

	public Object[][] getEventTable() {
		return eventsTable;
	}

	public Object[][] getAssociateTable() {
		return associateTable;
	}

	public Object[][] getPartnerTable() {
		return partnerTable;
	}

	public Object[][] getNameTable() {
		return nameTable;
	}

	public Object[][] getFlagTable() {
		return personFlagTable;
	}

	public String[] getPersonFlagDescript() {
		return tableFlagDescript;
	}

	public String getPersonName() {
		return personName;
	}

	public String getPersonReference() {
		return reference;
	}

	public Object[][] getParentTable() {
		return parentTable;
	}

	public long getEventPID(int tableRow) {
		return eventPIDhash.get(tableRow);
	}

	public long getPartnerPID(int tableRow) {
		return (long) partnerDataHash.get(tableRow)[0];
	}

	public Object[] getPartnerTableData(int indexInTable) {
		return partnerDataHash.get(indexInTable);
	}

	public long getParentTablePID(int indexInTable) {
		return (long) parentDataHash.get(indexInTable)[0];
	}

	public long getPersonNameTablePID(int indexInTable) {
		return personNamePIDhash.get(indexInTable);
	}

	public Object[] getParentTableData(int indexInTable) {
		return parentDataHash.get(indexInTable);
	}

	public int getNumberOfEvents() {
		return eventRows;
	}

	public int getNumberOfCildren() {
		return children;
	}

	public int getNumberOfMarriages() {
		return marriages;
	}

	public int getNumberOfAsociates() {
		return associateRows;
	}

	public int getNumberOfNames() {
		return nameRows;
	}

	public void setOpenVPstate(boolean state) {
		openVPstate = state;
	}

	public boolean getOpenVPstate() {
		return openVPstate;
	}



/**
 * updatePersonTable(long selectPersonPID)
 * @param personTablePID
 * @throws HBException
 * @throws SQLException
 */

	public int updateManagePersonTable(long selectPersonPID) throws HBException {
		this.selectPersonPID = selectPersonPID;
		int errorCode = 0;
		String 	fatherName = "",
				motherName = "";
		String selectString = null;
		long personNamePID;
		long birthFatherPID, birthMotherPID;
		ResultSet personSelected;
		setUpDateTranslation();
		locationNameStyle =  getNameStyleOutputCodes("P", dataBaseIndex);
		try {
			selectString = setSelectSQL("*", personTable, "PID = " + selectPersonPID);
			personSelected = requestTableData(selectString, dataBaseIndex);
			personSelected.first();
			personNamePID = personSelected.getLong(bestNameField);
			reference = personSelected.getString("REFERENCE");

		// Find style name and start and end data
			styleNameAndDates = updateStyleAndDates(personNamePID);
			personStyle =  getNameStyleOutputCodes(nameStylesOutput, nameStyleRPID, "N", dataBaseIndex);
			personName = pointLibraryResultSet.exstractPersonName(selectPersonPID, personStyle, dataBaseIndex);

	   // Person
			birthFatherPID = personSelected.getLong(personFatherField);
			if (birthFatherPID != null_RPID) {
				fatherName = pointLibraryResultSet.exstractPersonName(birthFatherPID,personStyle, dataBaseIndex);
			} else {
				fatherName = "---";
			}
			birthMotherPID = personSelected.getLong(personMotherField);
			if (birthMotherPID != null_RPID) {
				motherName = pointLibraryResultSet.exstractPersonName(birthMotherPID, personStyle, dataBaseIndex);
			} else {
				motherName = "---";
			}

		// Set up parent table
			int parentCode = prepareParentTable(selectPersonPID);
			if (HGlobal.DEBUG)
				System.out.println(" Parent table parentCode: " + parentCode  + "  Surety Father: " + parentTable[0][2]);
			if (parentCode > 0)	{
				parentTable[0][1] = fatherName;
				parentTable[1][1] = motherName;
			}

		// Count marriages
			marriages = countMarriages(selectPersonPID);

		// Set up all name table
			preparePersonNameTable(selectPersonPID);

		// Set flag list
			getPersonFlagList(selectPersonPID);

		// set up pictures
			if (HGlobal.DEBUG)
				System.out.println(" Person Selected: " + selectPersonPID + " DBindex: " + dataBaseIndex);

			errorCode = pointMediaHandler.getAllExhibitImage(selectPersonPID, personImage, dataBaseIndex);

			if (errorCode > 1) {
				System.out.println(" HBPersonHandler - exhibittable, Image error PID: " + selectPersonPID);
			}

		// Set up sorted event list with age
			eventRows = prepareEventTable(selectPersonPID);

		//	Prepare partner table
			partnerRows = preparePartnerTable(selectPersonPID);

		// Set up event Associate  list
			prepareAssociateTable(selectPersonPID);

		} catch (SQLException sqle) {
			throw new HBException("SQL exception: " + sqle.getMessage()
				+ "\nSQL string: " + selectString);
		}
		return errorCode;
	}

/**
 * countMarriages(long selectedPersonPID)
 * @param selectedPersonPID
 * @return
 * @throws HBException
 */
	private int countMarriages(long selectedPersonPID) throws HBException {
		int index = 0;
		Vector<Long> partnerPerson = new Vector<>();
		long partnerPID = null_RPID;
		String selectString = setSelectSQL("*", personPartnerTable, " PRI_PARTNER_RPID = " + selectedPersonPID
															+ " OR SEC_PARTNER_RPID = " + selectedPersonPID);
		ResultSet partnersSelected = requestTableData(selectString, dataBaseIndex);
		try {
			partnersSelected.last();
			partnersSelected.beforeFirst();
			while (partnersSelected.next()) {
				long priPartner = partnersSelected.getLong("PRI_PARTNER_RPID");
				long secPartner = partnersSelected.getLong("SEC_PARTNER_RPID");
				if (selectedPersonPID == priPartner) {
					partnerPID = secPartner;
				}
				if (selectedPersonPID == secPartner) {
					partnerPID = priPartner;
				}
				if (!partnerPerson.contains(partnerPID)) {
					partnerPerson.add(partnerPID);
					index++;
				}
			}
			return index;
		} catch (SQLException sqle) {
			System.out.println(" Error countMarriages(): " + sqle.getMessage());
			throw new HBException(" HBPersonHandler countMarriages() error " + sqle.getMessage());
		}
	}

/**
 * updateAllNameTable()  update the all name table
 * @throws HBException
 */
	public void updateAllNameTable() throws HBException {
		String selectString = setSelectSQL("*", personTable, " PID = " + selectPersonPID);
		ResultSet personSelected = requestTableData(selectString, dataBaseIndex);
		try {
			personSelected.first();
			personNamePID = personSelected.getLong(bestNameField);
			styleNameAndDates = updateStyleAndDates(personNamePID);
			preparePersonNameTable(selectPersonPID);
			personStyle =  getNameStyleOutputCodes(nameStylesOutput, nameStyleRPID, "N", dataBaseIndex);
			personName = pointLibraryResultSet.exstractPersonName(selectPersonPID, personStyle, dataBaseIndex);
		} catch (SQLException sqle) {
			throw new HBException(" Manage person data - updateAllNameTable: " + sqle.getMessage());
		}
	}

/**
 * allNameTable(long selectPersonPID)	Name table initia
 * @param selectPersonPID
 * @throws HBException
 */
	private void preparePersonNameTable(long selectPersonPID) throws HBException {
		String selectString;
		boolean primaryName;
		ResultSet personNameTableRS, personTableRS;
		long name_RPID = null_RPID, nameStylePID = null_RPID;
		int nameType;
		String langCode = HGlobal.dataLanguage;
		personNamePIDhash = new HashMap<>();
		try {
		// Find best name for selected person
			selectString = setSelectSQL("*", personTable, " PID = " + selectPersonPID);
			personTableRS = requestTableData(selectString, dataBaseIndex);
			personTableRS.first();
			personNamePID = personTableRS.getLong(bestNameField);
		// Set up table
			selectString = setSelectSQL("*", personNameTable, ownerRecordField + " = " + selectPersonPID);
			personNameTableRS = requestTableData(selectString, dataBaseIndex);
			personNameTableRS.last();
			nrRows = personNameTableRS.getRow();
			nameTable = new Object[nrRows][3];
			personNameTableRS.beforeFirst();
			int row = 0;
			while (personNameTableRS.next()) {

				name_RPID = personNameTableRS.getLong("PID");
				nameStylePID = personNameTableRS.getLong("NAME_STYLE_RPID");
				nameType = personNameTableRS.getInt("NAME_EVNT_TYPE");
				nameTable[row][0] = " " + pointLibraryResultSet.getEventName(nameType, langCode, dataBaseIndex).trim();

			// Set type of name
				primaryName = personNameTableRS.getBoolean("NAME_PRIMARY");
			// set up personal name style
				personStyle =  getNameStyleOutputCodes(nameStylesOutput, nameStylePID, "N", dataBaseIndex);
			// Find name according to name style
				nameTable[row][1] = " " + pointLibraryResultSet.selectPersonName(name_RPID,dataBaseIndex, personStyle);
			// Mark primary
				if (primaryName) {
					nameTable[row][1] = nameTable[row][1] + "(Primary)";
				}

				long hdatePID = personNameTableRS.getLong("START_HDATE_RPID");
				nameTable[row][2] = pointLibraryResultSet.exstractDate(hdatePID, dataBaseIndex);
				personNamePIDhash.put(row, name_RPID);
				row++;
			}
			nameRows = row;
		} catch (SQLException sqle) {
			throw new HBException("HBPersonHandler - allNameTable: " + sqle.getMessage());
		}
	}

/**
 * parentTable(long selectPersonPID)
 * @param selectPersonPID
 * @return errorCode
 * @throws HBException
 * @throws SQLException
 * 	Interpretation of parentRelationData:
		 			0 = ParentPID,
					1 = parent Name,
					2 = paentRole,
					3 = surety
					4 = parentRPID
 */
	private int prepareParentTable(long selectPersonPID) throws HBException, SQLException {
		int errorCode = 0;
		int tableSize, row;
		String 	parentName = "",
				parentRole = "",
				parentSurety = "   - - - - -";
		int parentType;
		long parentRPID, parentTablePID;
		ResultSet parentsSelectedRS;
		Object[] parentRelationData;
		parentDataHash = new HashMap<>();
		String langCode = HGlobal.dataLanguage;
		String birthSelect = setSelectSQL("*", personParentTable, "PERSON_RPID = " + selectPersonPID);
		parentsSelectedRS = requestTableData(birthSelect, dataBaseIndex);
		parentsSelectedRS.last();
		tableSize = parentsSelectedRS.getRow();
		if (tableSize <= 0) {
			return 1;
		}
		parentTable = new Object[tableSize][3];
		row = 0;
		parentsSelectedRS.beforeFirst();
		while (parentsSelectedRS.next()) {

			parentRelationData = new Object[5];
			parentTablePID = parentsSelectedRS.getLong("PID");
			parentType = parentsSelectedRS.getInt("PARENT_TYPE");
			parentSurety = parentsSelectedRS.getString("SURETY");
			if (parentSurety.trim().length() == 0) {
				parentSurety = " - - - - -";
			}

			parentRPID = parentsSelectedRS.getLong("PARENT_RPID");
			parentName =  pointLibraryResultSet.exstractPersonName(parentRPID, personStyle, dataBaseIndex);
			String typeSelect = setSelectSQL("*", eventDefnTable, "EVNT_TYPE = " + parentType
					+ " AND LANG_CODE = '" + langCode + "'");
			if (HGlobal.DEBUG)
				System.out.println(" Select SQL: " + typeSelect);

			ResultSet typesSelected = requestTableData(typeSelect, dataBaseIndex);
			typesSelected.last();
			int foundRows = typesSelected.getRow();
			if (typesSelected.last()) {
				typesSelected.first();
				parentRole = " " + typesSelected.getString("EVNT_NAME").trim();
				parentTable[row][0] = " " + parentRole;
			} else {
				if (parentType == 1079) {
					parentRole = " Father (en-US)";
				} else if (parentType == 1090) {
					parentRole = " Mother (en-US)";
				} else {
					parentRole = "  Not found  ";
				}
				parentTable[row][0] = parentRole;
			}
		// Set up parentRelationData for GUI use
			parentRelationData[0] = parentTablePID;
			parentRelationData[1] = parentName;
			parentRelationData[2] = parentType;
			parentRelationData[3] = parentSurety;
			parentRelationData[4] = parentRPID;

			parentTable[row][1] = " " + parentName;
			parentTable[row][2] = "   " + parentSurety;
			if (HGlobal.DEBUG)
				System.out.println(" HBPersonHandler - parentTable rows: " + foundRows + " Parent type: "
								+ parentType + " Role: " + parentRole + " Name: "
								+ parentName + " Surety: " + parentSurety);


			parentDataHash.put(row, parentRelationData);
			row++;
		}
		parentsSelectedRS.close();
		return errorCode;
	}

/**
 * preparePartnerTable(long selectPersonPID)
 * @param selectPersonPID
 * @return
 * @throws HBException
 * Setting of partnerRelationData
		Interpretation 	0 = partnerTablePID, 1 = partneType, 2 = priPartRole, 3 = secPartRole
					    4 = selectedPerson, 5 = partner name

 */
	private int preparePartnerTable(long selectedPersonPID) throws HBException {
		ArrayList<Object[]> partnerList = new ArrayList<>();
		partnerDataHash = new HashMap<>();
		Object[][] partnerData = null;
		String selectString = null;
		String langCode = HGlobal.dataLanguage;
		long  partnerTablePID = null_RPID, personPartnerPID = 0, eventHDatePID = null_RPID,
								sortHDatePID = null_RPID, selectedPartnerEvent =  null_RPID;
		int partnerRole = 1 , partnerType, priPartnerRole = 0, secPartnerRole = 0;
		ResultSet personPartnerSelectedRS, partnerEventSelectedRS = null;
		Object[] partners = null;
		Object[] partnerEditData = null;
		int rowCount = 0;

		try {
			selectString = setSelectSQL("*", personPartnerTable, "PRI_PARTNER_RPID = " + selectedPersonPID
								+ " OR SEC_PARTNER_RPID = " + selectedPersonPID);
			personPartnerSelectedRS = requestTableData(selectString, dataBaseIndex);
			personPartnerSelectedRS.beforeFirst();
			while (personPartnerSelectedRS.next()) {
			// Find the PID for the event
				partnerTablePID = personPartnerSelectedRS.getLong("PID");
				selectedPartnerEvent = personPartnerSelectedRS.getLong("EVNT_RPID");
				partnerType = personPartnerSelectedRS.getInt("PARTNER_TYPE");

				if (selectPersonPID == personPartnerSelectedRS.getLong("PRI_PARTNER_RPID")) {
					personPartnerPID = personPartnerSelectedRS.getLong("SEC_PARTNER_RPID");
					partnerRole = personPartnerSelectedRS.getInt("SEC_ROLE");
					priPartnerRole = personPartnerSelectedRS.getInt("PRI_ROLE");
					secPartnerRole = personPartnerSelectedRS.getInt("SEC_ROLE");
				} else if (selectPersonPID == personPartnerSelectedRS.getLong("SEC_PARTNER_RPID")) {
					personPartnerPID = personPartnerSelectedRS.getLong("PRI_PARTNER_RPID");
					partnerRole = personPartnerSelectedRS.getInt("PRI_ROLE");
					priPartnerRole = personPartnerSelectedRS.getInt("SEC_ROLE");
					secPartnerRole = personPartnerSelectedRS.getInt("PRI_ROLE");
				} else {
					System.out.println(" Not found: " + selectPersonPID);
				}

				int partnerEventCount = 0;
				eventHDatePID = null_RPID;
				sortHDatePID = null_RPID;
				if (selectedPartnerEvent != null_RPID) {
					selectString = setSelectSQL("*", eventTable, "PID = " + selectedPartnerEvent);
					partnerEventSelectedRS = requestTableData(selectString, dataBaseIndex);
					partnerEventSelectedRS.beforeFirst();
				// Find the partner event date from partner event
					while (partnerEventSelectedRS.next()) {
						eventHDatePID = partnerEventSelectedRS.getLong("START_HDATE_RPID");
						sortHDatePID = partnerEventSelectedRS.getLong("SORT_HDATE_RPID");
						partnerEventCount++;
					}
				}
				partnerEditData = new Object[6];
				partners = new Object[6];
				partners[0] = pointLibraryResultSet.exstractPersonName(personPartnerPID, personStyle,
													dataBaseIndex).trim();
				partners[1] = pointLibraryResultSet.getRoleName(partnerRole,
									partnerType, langCode, dataBaseIndex).trim();
				partners[2] = pointLibraryResultSet.getEventName(partnerType,
									langCode, dataBaseIndex).trim();

				if (eventHDatePID != null_RPID) {
					partners[3] = pointLibraryResultSet.exstractDate(eventHDatePID, dataBaseIndex).trim();
				} else {
					partners[3] = " -- ";
				}

				if (sortHDatePID != null_RPID) {
					partners[4] = pointLibraryResultSet.exstractSortString(sortHDatePID, dataBaseIndex);
				} else {
					partners[4] = "";
				}

		// Store date for setting edit partner
				partnerEditData[0] = partnerTablePID;
				partnerEditData[1] = partnerType;
				partnerEditData[2] = priPartnerRole;
				partnerEditData[3] = secPartnerRole;
				partnerEditData[4] = personName;
				partnerEditData[5] = partners[0];
			// Store partnerEditData in partnerList
				partners[5] = partnerEditData;
				partnerList.add(partners);
				rowCount++;

				if (HGlobal.DEBUG)
					dumpEvents("Partner", partners);

				if (partnerEventCount > 1)
					System.out.println(" Many partner events: " + partnerEventCount);

			}

			if (personPartnerSelectedRS != null)
				personPartnerSelectedRS.close();

			if (partnerEventSelectedRS != null)
				partnerEventSelectedRS.close();


			nrRows = rowCount;
			partnerData = new Object[nrRows][6];
			sortStrings = new String[nrRows];
			Object[] partnerRowLists = new Object[nrRows];

			for (int i = 0; i < nrRows; i++) {
				partnerData[i][0] = " " + partnerList.get(i)[0];
				partnerData[i][1] = " " + partnerList.get(i)[1];
				partnerData[i][2] = " " + partnerList.get(i)[2];
				partnerData[i][3] = " " + partnerList.get(i)[3];
				sortStrings[i] = (String) partnerList.get(i)[4];
				partnerRowLists[i] = partnerList.get(i)[5];
			}

		// Sort the events according to HDATE SORT
			int [] rowsSorted = pointLibraryBusiness.sorter.sort(sortStrings); // Setup sort index
			if (rowCount > 0) {
				partnerTable = sortTable(partnerData, rowsSorted);
			}

		// Setup the HashMap for row to partnerPID
			if (rowsSorted != null) {
				for (int i = 0; i < rowsSorted.length; i++) {
					partnerDataHash.put(i, (Object[]) partnerRowLists[rowsSorted[i]]);
					//System.out.println(" partnerDataHash: " + partnerDataHash.get(i)[0] + " / " + rowsSorted[i]);
				}
			}

			return nrRows;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException("SQL exception: " + sqle.getMessage() + "\nSQL string: " + selectString);
		}
	}

/**
 * prepareEventTable()
 * @return
 * @throws HBException
 */
	private int prepareEventTable() throws HBException {
		return prepareEventTable(selectPersonPID);
	}

/**
 * prepareEventTable(long selectPersonPID)
 * @param selectPersonPID
 * @return
 * @throws HBException
 */
	private int prepareEventTable(long selectPersonPID) throws HBException {
		Object[][] eventsData = null;
		ArrayList<Object[]> eventList = new ArrayList<>();

		// Initiate index HashMap for eventPID's
		eventPIDhash = new HashMap<>();
		eventDuplicateList = new Vector<>(100,10);
		int rowCount = 0;
	// Add names to event table
		rowCount = rowCount + addNamesToEvents(selectPersonPID, eventList);
	// Add partner events
		rowCount = rowCount + addPartnersToEvents(selectPersonPID, eventList);
	// Add events to table
		rowCount = rowCount + addEventToEvents(selectPersonPID, eventList);
	// Include witness events
		if (includeWitnessEvents) {
			rowCount = rowCount + addWitnesToEvents(selectPersonPID, eventList);
		}
	//	Include list of child's
		if (includeCildrenEvents) {
			rowCount = rowCount + addChildsToEvents(selectPersonPID, eventList);
		}

		nrRows = rowCount;
		eventsData = new Object[nrRows][6];
		sortStrings = new String[nrRows];
		long[] eventsPIDs = new long[nrRows];

		for (int i = 0; i < nrRows; i++) {
			eventsData[i][0] = " " + eventList.get(i)[0];
			eventsData[i][1] = " " + eventList.get(i)[1];
			eventsData[i][2] = " " + eventList.get(i)[2];
			eventsData[i][3] = " " + eventList.get(i)[3];
			eventsData[i][4] = " " + eventList.get(i)[4];
			eventsData[i][5] = eventList.get(i)[5];
			sortStrings[i] = (String) eventList.get(i)[6];
			eventsPIDs[i] = (long) eventList.get(i)[7];
		}
	// Sort the events according to HDATE SORT

		// Setup sort index
		int [] rowsSorted = pointLibraryBusiness.sorter.sort(sortStrings);

		if (rowCount > 0) {
			eventsTable = sortTable(eventsData, rowsSorted);
		}

		// Setup the HashMap for row to eventPID
		if (rowsSorted != null) {
			for (int i = 0; i < rowsSorted.length; i++) {
				eventPIDhash.put(i, eventsPIDs[rowsSorted[i]]);
			}
		}
		return nrRows;
	}

/**
 * addEventToEvents(long selectPersonPID, ArrayList<String[]> eventList)
 * @param selectPersonPID
 * @param eventList
 * @return
 * @throws HBException
 */
	private int addEventToEvents(long selectPersonPID, ArrayList<Object[]> eventList) throws HBException {
		String langCode = HGlobal.dataLanguage;
		String selectString = "";
		Object[] events = null;
		ResultSet eventSelected;
		int index = 0;
		long eventPID, eventHDatePID, sortHDatePID, locationName_RPID = null_RPID;
		int eventRole = 0, eventNumber;
		try {
			locationName_RPID = null_RPID;
			selectString = setSelectSQL("*", eventTable, "PRIM_ASSOC_RPID = " + selectPersonPID);
			eventSelected = requestTableData(selectString, dataBaseIndex);
			eventSelected.beforeFirst();
			int eventCount = 0;
			while (eventSelected.next()) {
				eventPID = eventSelected.getLong("PID");

		//Test for partner event duplicates - 07-11-2024
				if (eventDuplicateList.contains(eventPID)) continue;

				events = new Object[8];
				locationName_RPID = eventSelected.getLong("EVNT_LOCN_RPID");
				eventNumber = eventSelected.getInt("EVNT_TYPE");
				eventRole = eventSelected.getInt("PRIM_ASSOC_ROLE_NUM");
				eventHDatePID = eventSelected.getLong("START_HDATE_RPID");
				sortHDatePID = eventSelected.getLong("SORT_HDATE_RPID");

				//System.out.println(" Event event list: " + selectPersonPID + "/" + eventPID + "/"
				//		+ eventNumber + "/" + eventRole + "/" + locationName_RPID);

				events[0] = pointLibraryResultSet.getEventName(eventNumber, langCode, dataBaseIndex).trim();
				events[1] = pointLibraryResultSet.exstractDate(eventHDatePID, dataBaseIndex).trim();
				events[2] = pointLibraryResultSet.getRoleName(eventRole,
										eventNumber, langCode, dataBaseIndex).trim();
				events[3] = pointLibraryResultSet.selectLocationName(locationName_RPID,
										locationNameStyle, dataBaseIndex).trim();
				events[4] = "" + pointLibraryResultSet.calculateAge(eventHDatePID,
										selectPersonPID, dataBaseIndex);
		// Avoid negative age
				String date = (String) events[1];
				if (date.length() == 0 )
					events[4] = "";

				events[5] = "K";
				events[6] = pointLibraryResultSet.exstractSortString(sortHDatePID, dataBaseIndex);
				events[7] = eventPID;

				eventList.add(events);
				eventCount++;
				index++;
				if (HGlobal.DEBUG)
					dumpEvents("Event",events);

				//}
			}

			if (HGlobal.DEBUG) {
				if (eventCount > 1) {
					System.out.println(" Number of added events: " + eventCount);
				}
			}
			return index;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException("SQL exception: " + sqle.getMessage()
						+ "\nSQL string: " + selectString);
		}
	}

/**
 * addWitnesToEvents(long selectPersonPID, ArrayList<String[]> eventList)
 * @param selectPersonPID
 * @param eventList
 * @return
 * @throws HBException
 */
	private int addWitnesToEvents(long selectPersonPID, ArrayList<Object[]> eventList) throws HBException {
		String langCode = HGlobal.dataLanguage;
		String selectString = "";
		Object[] events;
		int index = 0;
		ResultSet eventAssocSelected, eventSelected;
		long eventHDatePID, sortHDatePID, eventPID, eventRPID, locationName_RPID = null_RPID;
		int eventRole, eventNumber;
		try {
			locationName_RPID = null_RPID;
			selectString = setSelectSQL("*", eventAssocTable, "ASSOC_RPID = " + selectPersonPID);
			eventAssocSelected = requestTableData(selectString, dataBaseIndex);
			eventAssocSelected.beforeFirst();
			while (eventAssocSelected.next()) {
				//events = new String[7];
				eventRole = eventAssocSelected.getInt("ROLE_NUM");
				eventRPID = eventAssocSelected.getLong("EVNT_RPID");
				//System.out.println(" Witness list: " + selectPersonPID + "/" + eventRPID + "/" + eventRole);
				selectString = setSelectSQL("*", eventTable, "PID = " + eventRPID);
				eventSelected = requestTableData(selectString, dataBaseIndex);
				eventSelected.beforeFirst();
				int eventCount = 0;
				events = new String[7];
				while (eventSelected.next()) {
					eventPID = eventSelected.getLong("PID");
				// Test duplicate 6.11.2024
					if (eventDuplicateList.contains(eventPID)) continue;
					eventDuplicateList.add(eventPID);
					events = new Object[8];
					locationName_RPID = eventSelected.getLong("EVNT_LOCN_RPID");
					eventNumber = eventSelected.getInt("EVNT_TYPE");
					eventHDatePID = eventSelected.getLong("START_HDATE_RPID");
					sortHDatePID = eventSelected.getLong("SORT_HDATE_RPID");
					events[0] = pointLibraryResultSet.getEventName(eventNumber,
											langCode, dataBaseIndex).trim();
					events[1] = pointLibraryResultSet.exstractDate(eventHDatePID, dataBaseIndex).trim();
					events[2] = pointLibraryResultSet.getRoleName(eventRole,
											eventNumber, langCode, dataBaseIndex).trim();
					events[3] = pointLibraryResultSet.selectLocationName(locationName_RPID,
											locationNameStyle, dataBaseIndex).trim();
					events[4] = "" + pointLibraryResultSet.calculateAge(eventHDatePID,
											selectPersonPID, dataBaseIndex);
					events[5] = "W";
					events[6] = pointLibraryResultSet.exstractSortString(sortHDatePID, dataBaseIndex);
					events[7] = eventRPID;
					eventCount++;
					index++;
					eventList.add(events);

					if (HGlobal.DEBUG)
						dumpEvents("Witness",events);


				}
				if (eventCount > 1) {
					System.out.println("Multiple witness events :" + eventCount);
				}
			}
			return index;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException("SQL exception: " + sqle.getMessage()
						+ "\nSQL string: " + selectString);
		}
	}

/**
 * addNamesToEvents(long selectPersonPID, ArrayList<Object[]> eventList)
 * @param selectPersonPID
 * @param eventList
 * @return
 * @throws HBException
 */
	private int addNamesToEvents(long selectPersonPID, ArrayList<Object[]> eventList) throws HBException {
		String langCode = HGlobal.dataLanguage;
		String selectString = "";
		Object[] events;
		String[] personNameStyle;
		int index = 0;
		ResultSet personNameSelected;
		long nameHDatePID, personNamePID, nameStyleRPID;
		int nameType;
		try {
			selectString = setSelectSQL("*", personNameTable, "OWNER_RPID = " + selectPersonPID);
			personNameSelected = requestTableData(selectString, dataBaseIndex);
			personNameSelected.beforeFirst();
			while (personNameSelected.next()) {
				if (!personNameSelected.getBoolean("NAME_PRIMARY")) {
					nameType = personNameSelected.getInt("NAME_EVNT_TYPE");
					nameStyleRPID = personNameSelected.getLong("NAME_STYLE_RPID");
					//System.out.println(" Name list: " + selectPersonPID + "/" + nameType + "/" + nameStyleRPID);
					personNameStyle =  getNameStyleOutputCodes(nameStylesOutput, nameStyleRPID, "N", dataBaseIndex);
					//events = new String[7];
					personNamePID = personNameSelected.getLong("PID");
					events = new Object[8];
					nameHDatePID = personNameSelected.getLong("START_HDATE_RPID");
					events[0] = pointLibraryResultSet.getEventName(nameType,langCode, dataBaseIndex).trim();
					events[1] = pointLibraryResultSet.exstractDate(nameHDatePID, dataBaseIndex).trim();
					events[2] = " ---- ";
					events[3] = pointLibraryResultSet.selectPersonName(personNamePID, dataBaseIndex, personNameStyle).trim();
					events[4] = "" + pointLibraryResultSet.calculateAge(nameHDatePID, selectPersonPID, dataBaseIndex);
					events[5] = "N";
					events[6] = pointLibraryResultSet.exstractSortString(nameHDatePID, dataBaseIndex);
					events[7] = personNamePID;
					index++;
					eventList.add(events);
				}
				//if (HGlobal.DEBUG) dumpEvents("Name events",events);

			}
			return index;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException("SQL exception: " + sqle.getMessage()
						+ "\nSQL string: " + selectString);
		}
	}


/**
 * addPartnersToEvents(long selectedPersonPID, ArrayList<String[]> eventList)
 * @param selectedPersonPID
 * @param eventList
 * @return
 * @throws HBException
 */
	private int addPartnersToEvents(long selectedPersonPID, ArrayList<Object[]> eventList) throws HBException {
		String selectString = null;
		String langCode = HGlobal.dataLanguage;
		long locationName_RPID, eventHDatePID, sortHDatePID, selectedEvent, eventPID;
		int eventRole = 1 , eventNumber, partnerType;
		ResultSet personPartnerSelected, partnerEventSelected;
		Object[] events = null;
		int index = 0;
		try {
			selectString = setSelectSQL("*", personPartnerTable, "PRI_PARTNER_RPID = " + selectedPersonPID
								+ " OR SEC_PARTNER_RPID = " + selectedPersonPID);
			personPartnerSelected = requestTableData(selectString, dataBaseIndex);
			personPartnerSelected.beforeFirst();
			while (personPartnerSelected.next()) {
			// Find the PID for the event
				selectedEvent = personPartnerSelected.getLong("EVNT_RPID");
				partnerType = personPartnerSelected.getInt("PARTNER_TYPE");
				if (selectPersonPID == personPartnerSelected.getLong("PRI_PARTNER_RPID")) {
					eventRole = personPartnerSelected.getInt("PRI_ROLE");
				} else if (selectPersonPID == personPartnerSelected.getLong("SEC_PARTNER_RPID")) {
					eventRole = personPartnerSelected.getInt("SEC_ROLE");
				} else {
					System.out.println(" Not found: " + selectPersonPID);
				}
				selectString = setSelectSQL("*", eventTable, "PID = " + selectedEvent);
				partnerEventSelected = requestTableData(selectString, dataBaseIndex);
				partnerEventSelected.beforeFirst();
				int eventCount = 0;
				while (partnerEventSelected.next()) {


					eventPID = partnerEventSelected.getLong("PID");
				// Chech Partner event duplicate - 7.11.2024
					//if (eventDuplicateList.contains(eventPID)) continue;
					eventDuplicateList.add(eventPID);

					events = new Object[8];
					eventNumber = partnerEventSelected.getInt("EVNT_TYPE");
					locationName_RPID = partnerEventSelected.getLong("EVNT_LOCN_RPID");
					eventHDatePID = partnerEventSelected.getLong("START_HDATE_RPID");
					sortHDatePID = partnerEventSelected.getLong("SORT_HDATE_RPID");

					//System.out.println(" Event partner list: " + selectPersonPID + "/" + eventNumber + "/"
					//		+ eventRole + "/" + locationName_RPID);

					events[0] = pointLibraryResultSet.getEventName(partnerType,
													langCode, dataBaseIndex).trim();
					events[1] = pointLibraryResultSet.exstractDate(eventHDatePID,
													dataBaseIndex).trim();
					events[2] = pointLibraryResultSet.getRoleName(eventRole,
													eventNumber, langCode, dataBaseIndex).trim();
					events[3] = pointLibraryResultSet.selectLocationName(locationName_RPID,
													locationNameStyle, dataBaseIndex).trim();
					events[4] = "" + pointLibraryResultSet.calculateAge(eventHDatePID,
													selectPersonPID, dataBaseIndex);
					events[5] = "K"; // set as key event
					events[6] = pointLibraryResultSet.exstractSortString(sortHDatePID, dataBaseIndex);
					events[7] = eventPID;
					eventList.add(events);
					index++;
					eventCount++;
					if (HGlobal.DEBUG)
						dumpEvents("Partner",events);
				}
				if (eventCount > 1) {
					System.out.println(" Number of added partner events: " + eventCount);
				}
			}
			return index;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException("SQL exception: " + sqle.getMessage()
						+ "\nSQL string: " + selectString);
		}
	}

/**
 * addPartnersToAssocs(long selectedPersonPID, ArrayList<Object[]> assocList)
 * @param selectedPersonPID
 * @param eventList
 * @return
 * @throws HBException
 */
	private int addPartnersToAssocs(long selectedPersonPID, ArrayList<Object[]> assocList) throws HBException {
		String selectString = null;
		String langCode = HGlobal.dataLanguage;
		int eventRole = 1 , eventNumber, partnerType;
		String personName = "", eventRoleName = "", eventName = "", eventDate = "--";
		long partnerPersonPID = null_RPID, selectedEventPID, eventHDatePID;
		ResultSet personPartnerSelected, partnerEventSelected;
		this.selectedPersonPID = selectedPersonPID;
		int index = 0;
		int partnerEventCount = 0;
		try {
			selectString = setSelectSQL("*", personPartnerTable, "PRI_PARTNER_RPID = " + selectedPersonPID
								+ " OR SEC_PARTNER_RPID = " + selectedPersonPID);
			personPartnerSelected = requestTableData(selectString, dataBaseIndex);
			personPartnerSelected.beforeFirst();
			while (personPartnerSelected.next()) {

			// Find the PID for the event
				selectedEventPID = personPartnerSelected.getLong("EVNT_RPID");

			// No partner event recorded EVNT_RPID = null_RPID
				if (selectedEventPID == null_RPID)
					return index;

				partnerType = personPartnerSelected.getInt("PARTNER_TYPE");
				if (selectedPersonPID == personPartnerSelected.getLong("PRI_PARTNER_RPID")) {
					eventRole = personPartnerSelected.getInt("SEC_ROLE");
					partnerPersonPID = personPartnerSelected.getLong("SEC_PARTNER_RPID");
				} else if (selectedPersonPID == personPartnerSelected.getLong("SEC_PARTNER_RPID")) {
					eventRole = personPartnerSelected.getInt("PRI_ROLE");
					partnerPersonPID = personPartnerSelected.getLong("PRI_PARTNER_RPID");
				} else {
					System.out.println(" Not found: " + selectedPersonPID);
				}

			// Duplicated self assoc fix - 24.10.2024
				if (selectedPersonPID == partnerPersonPID) continue; // Self assoc detected

		    // Selected partner vent
				selectString = setSelectSQL("*", eventTable, "PID = " + selectedEventPID);
				partnerEventSelected = requestTableData(selectString, dataBaseIndex);
				partnerEventCount = 0;

			// Test for no partner event recorded
				if (isResultSetEmpty(partnerEventSelected)) return index;

				partnerEventSelected.beforeFirst();
				while (partnerEventSelected.next()) {
					eventNumber = partnerEventSelected.getInt("EVNT_TYPE");
					eventHDatePID = partnerEventSelected.getLong("START_HDATE_RPID");
					personName = pointLibraryResultSet.exstractPersonName(partnerPersonPID, personStyle, dataBaseIndex);
					eventRoleName = pointLibraryResultSet.getRoleName(eventRole,
														eventNumber, langCode, dataBaseIndex);
					eventName = pointLibraryResultSet.getEventName(partnerType,
														langCode, dataBaseIndex);
					eventDate = pointLibraryResultSet.exstractDate(eventHDatePID, dataBaseIndex);
					if (HGlobal.DEBUG)
						System.out.println(" addPartnersToAssocs: " + personName + " / " + eventName);

					Object[] asociate = new Object[5];
					asociate[0] = " " + personName.trim();
					asociate[1] = " " + eventRoleName.trim();
					asociate[2] = " " + eventName.trim();
					asociate[3] = " " + eventDate.trim();
					asociate[4] = partnerPersonPID;
					assocList.add(asociate);
					index++;
					partnerEventCount++;
					if (HGlobal.DEBUG)
						dumpEvents("Associate partner", asociate);

				//  Must exclude self assocs *****************************
					index = index + addPartnerAssocToAssoc(partnerPersonPID, selectedEventPID,
								eventName, eventNumber, eventDate);
				}
				if (partnerEventCount > 1)
					System.out.println(" Number of added partner events: " + partnerEventCount);

			}
			return index;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" addPartnersToTable - SQL exception: " + sqle.getMessage()
						+ "\nSQL string: " + selectString);
		}
	}

/**
 * private int addPartnerAssocToAssoc()
 * @param focusPersonPID
 * @param eventTablePID
 * @param eventName
 * @param eventType
 * @param eventDate
 * @return
 * @throws HBException
 */
	private int addPartnerAssocToAssoc(long focusPersonPID, long eventTablePID, String eventName, int eventType, String eventDate) throws HBException {
		String selectString = null;
		ResultSet eventAssocSelected;
		String langCode = HGlobal.dataLanguage;
		String personName = "", eventRole = "";
		int witness = 0, assocRoleCode;
		long  assocPersonPID;
		selectString = setSelectSQL("*", eventAssocTable, "EVNT_RPID = " + eventTablePID);
		try {
			eventAssocSelected = requestTableData(selectString, dataBaseIndex);
			while (eventAssocSelected.next()) {
				assocPersonPID = eventAssocSelected.getLong("ASSOC_RPID");

		// Duplicated self assoc fix - 24.10.2024
				if (focusPersonPID == assocPersonPID || selectedPersonPID == assocPersonPID) continue;

				assocRoleCode = eventAssocSelected.getInt("ROLE_NUM");
				eventRole = pointLibraryResultSet.getRoleName(assocRoleCode,
			  			eventType,
			  			langCode,
			  			dataBaseIndex);
				personName = pointLibraryResultSet.exstractPersonName(assocPersonPID, personStyle, dataBaseIndex).trim();

				if (HGlobal.DEBUG)
					System.out.println(" Associate nr: " + witness + " Event PID: " + eventTablePID + " Type: " + eventType
						+ " Event: " + eventName.trim() + " Name: " + personName + " Role: " + eventRole);

			// The focus person can also be a witness to another event
				Object[] associates = new String[4];
				associates[0] = " " + personName.trim();
				associates[1] = " " + eventRole.trim();
				associates[2] = " " + eventName.trim();
				associates[3] = " " + eventDate.trim();
				associateList.add(associates);
				witness++;
			}
			return witness;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" HBPersonHandler - addPartnerAssocToAssoc \nerror: " + sqle.getMessage());
		}
	}

/**
 * private int addChildsToEvents(long selectedPersonPID, ArrayList<String[]> eventList)
 * @param selectedPersonPID
 * @param eventList
 * @return number of rows in event list
 * @throws HBException
 */
	private int addChildsToEvents(long selectedPersonPID, ArrayList<Object[]> eventList) throws HBException {
		String selectString = null;
		long personPID, eventRPID, eventHDatePID, sortHDatePID, eventPID;
		int eventRole , eventNumber, parentType;
		Object[] events = null;
		ResultSet personChildSelected, personBirthEvent;
		String langCode = HGlobal.dataLanguage;
		selectString = setSelectSQL("*", personParentTable, "PARENT_RPID = " + selectedPersonPID);
		int index = 0;
		try {
			personChildSelected = requestTableData(selectString, dataBaseIndex);
			personChildSelected.beforeFirst();
			while (personChildSelected.next()) {
				personPID = personChildSelected.getLong("PERSON_RPID");
				parentType = personChildSelected.getInt("PARENT_TYPE");
				eventRPID = personChildSelected.getLong("EVNT_RPID");
				selectString = setSelectSQL("*", eventTable, "PID = " + eventRPID);
				personBirthEvent = requestTableData(selectString, dataBaseIndex);

				int eventCount = 0;
				if (!isResultSetEmpty(personBirthEvent)) {
			// Birth event recorded for parent relation
					personBirthEvent.beforeFirst();
					while (personBirthEvent.next()) {

						eventPID = personBirthEvent.getLong("PID");
						if (eventDuplicateList.contains(eventPID)) continue;
						eventDuplicateList.add(eventPID);

						events = new Object[8];
						eventRole = personBirthEvent.getInt("PRIM_ASSOC_ROLE_NUM");
						eventNumber = personBirthEvent.getInt("EVNT_TYPE");
						eventHDatePID = personBirthEvent.getLong("START_HDATE_RPID");
						sortHDatePID = personBirthEvent.getLong("SORT_HDATE_RPID");

						events[0] = pointLibraryResultSet.getEventName(parentType,
														langCode, dataBaseIndex).trim();
						events[1] = pointLibraryResultSet.exstractDate(eventHDatePID,
														dataBaseIndex).trim();
						events[2] = pointLibraryResultSet.getRoleName(eventRole,
														eventNumber, langCode, dataBaseIndex).trim();
						events[3] = pointLibraryResultSet.exstractPersonName(personPID,
														personStyle, dataBaseIndex);
						events[4] = "" + pointLibraryResultSet.calculateAge(eventHDatePID,
														selectPersonPID, dataBaseIndex);
						events[5] = "C"; // set as child event
						events[6] = pointLibraryResultSet.exstractSortString(sortHDatePID, dataBaseIndex);
						events[7] = eventPID;
						eventCount++;
						eventList.add(events);
						// Only add to child count if a bio relationship
						if (parentType == 1079 || parentType == 1090) index++;
						if (HGlobal.DEBUG)
							dumpEvents("Child",events);

					}

				} else {
			// No birth event recorded for child in parent relation
					events = new Object[8];
					events[0] = pointLibraryResultSet.getEventName(parentType,
											langCode, dataBaseIndex).trim();
					events[1] = "";
					events[2] = "";
					events[3] = pointLibraryResultSet.exstractPersonName(personPID,
							personStyle, dataBaseIndex);
					events[4] = "";
					events[5] = "C"; // set as child event
					events[6] = "";
					events[7] = null_RPID;
					eventCount++;
					eventList.add(events);
					// Only add to child count if a bio relationship
					if (parentType == 1079 || parentType == 1090) index++;
				}
				if (eventCount > 1)
					System.out.println(" Number of added child birth events: " + eventCount);
			}
			children = index;
			return index;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException("SQL exception: " + sqle.getMessage()
						+ "\nSQL string: " + selectString);
		}
	}

/**
 * dumpEvents(String[] events)
 * @param events
 */
	private void dumpEvents(String eventType, Object[] events) {
		System.out.print(" Event - " + eventType + " - ");
		for (Object event : events) {
			System.out.print(event + " / ");
		}
		System.out.println();
	}

/**
 * addEventToAssociates(long selectedPersonPID, ArrayList<Object[]> assocList)
 * @param selectedPersonPID
 * @throws HBException
 */
	private int addEventToAssociates(long selectedPersonPID, ArrayList<Object[]> assocList) throws HBException {
		String selectString = null;
		ResultSet personEventSelected, eventAssocSelected;
		String langCode = HGlobal.dataLanguage;
		String personName = "", eventRole = "", eventName = "", eventDate = "--";
		int witness = 0, eventType = 0, eventRoleCode, eventGroup;
		long eventTablePID, eventHDatePID, assocPersonPID;
		Vector<Long> assocPersonDuplicatePIDList;
		try {
		// Get person events from T450
			selectString = setSelectSQL("*", eventTable, "PRIM_ASSOC_RPID = " + selectedPersonPID);
			personEventSelected = requestTableData(selectString, dataBaseIndex);
			personEventSelected.beforeFirst();
			while (personEventSelected.next()) {

				eventTablePID = personEventSelected.getLong("PID");
				eventHDatePID = personEventSelected.getLong("START_HDATE_RPID");
				eventDate = pointLibraryResultSet.exstractDate(eventHDatePID, dataBaseIndex);
				eventType = personEventSelected.getInt("EVNT_TYPE");
				eventGroup = pointLibraryResultSet.getEventGroup(eventType, dataBaseIndex);

			// Married and divorce group already processed
				if (eventGroup == marrGroup || eventGroup == divorceGroup) continue;
				eventName = pointLibraryResultSet.getEventName(eventType, langCode, dataBaseIndex);
			// Get associate persons with the events in list
				selectString = setSelectSQL("*", eventAssocTable, "EVNT_RPID = " + eventTablePID);
				eventAssocSelected = requestTableData(selectString, dataBaseIndex);
				assocPersonDuplicatePIDList = new Vector<>(100,10);
				eventAssocSelected.beforeFirst();
				while (eventAssocSelected.next()) {
					assocPersonPID = eventAssocSelected.getLong("ASSOC_RPID");

				// Test for duplicate persons - 10-11-2024
					if (assocPersonDuplicatePIDList.contains(assocPersonPID)) continue;
					assocPersonDuplicatePIDList.add(assocPersonPID);

					eventRoleCode = eventAssocSelected.getInt("ROLE_NUM");
					eventRole = pointLibraryResultSet.getRoleName(eventRoleCode,
				  			eventType,
				  			langCode,
				  			dataBaseIndex);
					personName = pointLibraryResultSet.exstractPersonName(assocPersonPID, personStyle, dataBaseIndex);

					if (HGlobal.DEBUG)
						System.out.println(" addEventToAssociates: " + witness + " Event PID: " + eventTablePID
								+ " Group: "+ eventGroup + " Type: " + eventType + " Eventname: "
								+ eventName.trim() + " Person: " + personName + " Role: " + eventRole);

					Object[] associates = new String[4];
					associates[0] = " " + personName.trim();
					associates[1] = " " + eventRole.trim();
					associates[2] = " " + eventName.trim();
					associates[3] = " " + eventDate.trim();
					associateList.add(associates);
					witness++;
				}
			}
			return witness;

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException("SQL exception: " + sqle.getMessage()
						+ "\nSQL string: " + selectString);
		}
	}

/**
 * prepareAssociateTable(long selectPersonPID)
 * @param selectedPersonPID
 * @throws HBException
*/
	private void prepareAssociateTable(long selectedPersonPID) throws HBException {
		String selectString = null;
		ResultSet eventSelected, personAssociateSelected, eventAssocSelected;
		String langCode = HGlobal.dataLanguage;
		associateList = new ArrayList<>();
		String personName = "", eventRole = "", eventName = "", eventDate = "--";
		int witness = 0, eventNumber = 0, eventRoleCode, selfAssocCount = 0;
		//int beforeAssocs;
		long eventTablePID, eventPID, assocPersonPID, eventHDatePID;

	// Test for duplicate assoc person - 9-11-2024
		Vector<Long> assocEventDuplicatePIDList;
		Vector<Long> assocPersonDuplicatePIDList;

		associateRows = addPartnersToAssocs(selectedPersonPID, associateList); //
		//System.out.println(" After addPartnersToAssocs - assocs found: " + associateRows);
	// addEventToAssociates add duplicates 9.11.2024
		//beforeAssocs = associateRows;
		associateRows = associateRows + addEventToAssociates(selectedPersonPID, associateList); // Def
		//System.out.println(" After addEventToAssociates - assocs found: " + (associateRows - beforeAssocs));
		assocEventDuplicatePIDList = new Vector<>(100,10);
		try {
			selectString = setSelectSQL("*", eventAssocTable, "ASSOC_RPID = " + selectedPersonPID);
			personAssociateSelected = requestTableData(selectString, dataBaseIndex);
			personAssociateSelected.beforeFirst();
			while (personAssociateSelected.next()) {

		// Get associate persons with the events in list
				eventTablePID = personAssociateSelected.getLong("EVNT_RPID");

		// Test for duplicate events - 9-11-2024
				if (assocEventDuplicatePIDList.contains(eventTablePID)) continue;
				assocEventDuplicatePIDList.add(eventTablePID);

			// Add key associate
				associateRows = associateRows + addKeyAssocToAssoc(selectedPersonPID, eventTablePID);
				//System.out.println(" After addKeyAssocToAssocs - assocs found: " + associateRows);
				selectString = setSelectSQL("*", eventAssocTable, "EVNT_RPID = " + eventTablePID);
				eventAssocSelected = requestTableData(selectString, dataBaseIndex);
				eventAssocSelected.beforeFirst();

				assocPersonDuplicatePIDList = new Vector<>(100,10);

				while (eventAssocSelected.next()) {
					eventPID = eventAssocSelected.getLong("EVNT_RPID");
					assocPersonPID = eventAssocSelected.getLong("ASSOC_RPID");

				// Test for duplicate persons - 9-11-2024
					if (assocPersonDuplicatePIDList.contains(assocPersonPID)) continue;
					assocPersonDuplicatePIDList.add(assocPersonPID);

					eventRoleCode = eventAssocSelected.getInt("ROLE_NUM");

				// The focus person can also be a witness to another event
					if (assocPersonPID != selectedPersonPID) {

				// Get event data
						selectString = setSelectSQL("*", eventTable, "PID = " + eventPID);
						eventSelected = requestTableData(selectString, dataBaseIndex);
						eventSelected.beforeFirst();
						while (eventSelected.next()) {

							eventHDatePID = eventSelected.getLong("START_HDATE_RPID");
							eventDate = pointLibraryResultSet.exstractDate(eventHDatePID, dataBaseIndex);
							eventNumber = eventSelected.getInt("EVNT_TYPE");
							eventName = pointLibraryResultSet.getEventName(eventNumber, langCode, dataBaseIndex).trim();
							eventRole = pointLibraryResultSet.getRoleName(eventRoleCode,
								  			eventNumber,
								  			langCode,
								  			dataBaseIndex);
							personName = pointLibraryResultSet.exstractPersonName(assocPersonPID, personStyle, dataBaseIndex);
							if (HGlobal.DEBUG)
								System.out.println(" prepareAssociateTable: " + witness + " Event PID: " + eventPID + " Type: " + eventNumber
													+ " Event: " + eventName + " Name: " + personName + " Role: " + eventRole);

							Object[] associates = new String[4];
							associates[0] = " " + personName.trim();
							associates[1] = " " + eventRole.trim();
							associates[2] = " " + eventName.trim();
							associates[3] = " " + eventDate.trim();
							associateList.add(associates);
							witness++;
						}
					} else {
						selfAssocCount++;
						if (HGlobal.DEBUG)
							System.out.println(" HBPersonHandler - Self assoc: " + selfAssocCount
														  + " Event PID: " + eventPID
														  + " RoleCode: " + eventRoleCode
														  + " Assoc per: " + assocPersonPID);

					}
				}
			}
			if (HGlobal.DEBUG)
				if (selfAssocCount > 0)
					System.out.println(" Number of self assocs: " + selfAssocCount);



		// Set number of row in asscociate table
			associateRows = associateRows + witness;

			associateTable = new Object[associateRows][4];
			for (int i = 0; i < associateRows; i++) {
				associateTable[i][0] = associateList.get(i)[0];
				associateTable[i][1] = associateList.get(i)[1];
				associateTable[i][2] = associateList.get(i)[2];
				associateTable[i][3] = associateList.get(i)[3];
			}
		// print out number of asscoated for person
			if (HGlobal.DEBUG)
				System.out.println(" Person Manager associate table rows: " + associateRows);

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException("SQL exception: " + sqle.getMessage()
						+ "\nSQL string: " + selectString);
		}
	}

/**
 * addKeyAssocToAssoc(long eventTablePID)
 * @param eventTablePID
 * @return
 * @throws HBException
 */
	private int addKeyAssocToAssoc(long focusPersonPID, long eventTablePID) throws HBException {
		String selectString = null;
		ResultSet keyAssocSelected, partnerRelationRS;
		String langCode = HGlobal.dataLanguage;
		String personName = "", eventRoleName = "", eventDate = "", eventName = "";
		int witness = 0, assocRoleCode, eventType, eventGroup, priRole = 0, secRole = 0, keyAssocs = 1;
		long  assocPersonPID, eventHDatePID, priPartnerPID = null_RPID, secPartnerPID = null_RPID;
		selectString = setSelectSQL("*", eventTable, "PID = " + eventTablePID);
		try {
			keyAssocSelected = requestTableData(selectString, dataBaseIndex);
			if (isResultSetEmpty(keyAssocSelected)) return 0;
			keyAssocSelected.first();
			eventType = keyAssocSelected.getInt("EVNT_TYPE");
			assocPersonPID = keyAssocSelected.getLong("PRIM_ASSOC_RPID");

			assocRoleCode = keyAssocSelected.getInt("PRIM_ASSOC_ROLE_NUM");
			eventHDatePID = keyAssocSelected.getLong("START_HDATE_RPID");
			eventDate = pointLibraryResultSet.exstractDate(eventHDatePID, dataBaseIndex);
			eventName = pointLibraryResultSet.getEventName(eventType,
															langCode, dataBaseIndex);

		// if partner event process partner person and roles
			eventGroup = pointLibraryResultSet.getEventGroup(eventType, dataBaseIndex);
			if (eventGroup == marrGroup || eventGroup == divorceGroup) {
				selectString = setSelectSQL("*", personPartnerTable, "EVNT_RPID = " + eventTablePID);
				partnerRelationRS = requestTableData(selectString, dataBaseIndex);
				partnerRelationRS.first();
				priRole = partnerRelationRS.getInt("PRI_ROLE");
				secRole = partnerRelationRS.getInt("SEC_ROLE");
				priPartnerPID = partnerRelationRS.getLong("PRI_PARTNER_RPID");
				secPartnerPID = partnerRelationRS.getLong("SEC_PARTNER_RPID");
				keyAssocs = 2;
			}

			for (int i = 0; i < keyAssocs; i++) {
				if (keyAssocs == 2) {
					if (i == 0) {
						assocPersonPID = priPartnerPID;
						assocRoleCode = priRole;
					}
					if (i == 1)	{
						assocPersonPID = secPartnerPID;
						assocRoleCode = secRole;
					}
				}

			// Detect self assoc - fix 24.10.2024
				if (focusPersonPID == assocPersonPID) return 0; // Self assoc detected

				eventRoleName = pointLibraryResultSet.getRoleName(assocRoleCode,
			  			eventType,
			  			langCode,
			  			dataBaseIndex);
				personName = pointLibraryResultSet.exstractPersonName(assocPersonPID, personStyle, dataBaseIndex);

				if (HGlobal.DEBUG)
					System.out.println(" addKeyAssocToAssoc: " + witness + " Event PID: " + eventTablePID
							+ " Event group: " + eventGroup + " Eventype: " + eventType + " Eventname: "
							+ eventName.trim() + " Prim Assoc PID: " + assocPersonPID + " Person name: " + personName.trim() + " Role: " + eventRoleName);


				Object[] asociates = new Object[5];
				asociates[0] = " " + personName.trim();
				asociates[1] = " " + eventRoleName.trim();
				asociates[2] = " " + eventName.trim();
				asociates[3] = " " + eventDate.trim();
				asociates[4] = assocPersonPID;
				associateList.add(asociates);
				witness++;
			}

			return witness;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" HBPersonHandler - addKeyAssocToAssoc \nerror: " + sqle.getMessage());
		}
	}
/**
 * personFlagList(long selectedPersonPID)
 * @param selectedPersonPID
 * @throws HBException
 *  From T251_FLAG_DEFN:
 	0 - IS_SYSTEM
 	1 - ACTIVE
	2 - GUI_SEQ
	3 - FLAG_IDENT
	4 - DEFAULT_INDEX

	From T252_FLAG_VALU
	5 - "Flag setting"

	From T204_FLAG_TRAN:
	6 - LANG_CODE
	7 - FLAG_NAME
	8 - FLAG_VALUES
	9 - FLAG_DESC
 */
	public void getPersonFlagList(long selectedPersonPID) throws HBException {
		int flagRows = 0, index = 0, flagIdent;
		String langCode = HGlobal.dataLanguage; //"en-GB";
		ResultSet personFlagDefinition, personFlagTranslation;
		String selectString;
		flagDefinMapPID = new HashMap<>() ;
		selectString = setSelectSQL("*", flagDefinition, "");
		personFlagDefinition = requestTableData(selectString, dataBaseIndex);
		try {
		// Count number of flags
			personFlagDefinition.last();
			flagRows = personFlagDefinition.getRow();
		// Create arrays
			personFlagTable = new Object[flagRows][10];
			//tableFlagDescript = new String[flagRows];
			personFlagDefinition.beforeFirst();
			while (personFlagDefinition.next()) {
				flagIdent = personFlagDefinition.getInt("FLAG_IDENT");
				flagDefinMapPID.put(flagIdent, personFlagDefinition.getLong("PID"));
				selectString = setSelectSQL("*", translatedFlag, "(LANG_CODE = '" + langCode + "'"
																		+ " OR FLAG_IDENT > 7)"
																		+ " AND FLAG_IDENT = " + flagIdent);
				personFlagTranslation = requestTableData(selectString, dataBaseIndex);
				personFlagTranslation.first();
				personFlagTable[index][0] = personFlagDefinition.getBoolean("IS_SYSTEM");
				personFlagTable[index][1] = personFlagDefinition.getBoolean("ACTIVE");
				personFlagTable[index][2] = personFlagDefinition.getInt("GUI_SEQ");
				personFlagTable[index][3] = flagIdent;
				personFlagTable[index][4] = personFlagDefinition.getInt("DEFAULT_INDEX");
				String flagValues = personFlagTranslation.getString("FLAG_VALUES");
				personFlagTable[index][5] = getPersonFlagSetting(selectedPersonPID, flagIdent, flagValues);
				personFlagTable[index][6] = personFlagTranslation.getString("LANG_CODE");
				personFlagTable[index][7] = personFlagTranslation.getString("FLAG_NAME").trim();
				personFlagTable[index][8] = flagValues;
				personFlagTable[index][9] = personFlagTranslation.getString("FLAG_DESC");
				if (HGlobal.DEBUG) {
					String flagValue = personFlagTranslation.getString("FLAG_VALUES");
					String flagDescript = personFlagTranslation.getString("FLAG_DESC");
					String flagName = personFlagTranslation.getString("FLAG_NAME");
					System.out.print( "Index: " + index + " / " + flagIdent);
					System.out.print( " Flag name: " + flagName.trim());
					System.out.print( " /value: " + flagValue);
					System.out.println( " /descript: " + flagDescript);
				}
				index++;
			}
		} catch (SQLException sqle) {
			System.out.println(" HBPersonHandler - getPersonFlagList ERROR: " + sqle.getMessage());
			sqle.printStackTrace();
		}
	}

/**
 * getFlagSetting(long selectedPersonPID, int flagIdent, String flagValues)
 * @param selectedPersonPID
 * @param flagIdent
 * @param flagValues
 * @return
 * @throws HBException
 */
	private String getPersonFlagSetting(long selectedPersonPID, int flagIdent, String flagValues) throws HBException {
		String selectSQL,  setting;
		int flagValue = 0;
		ResultSet flagData, personData;
		String[] flagValueOptions = flagValues.split(",");
		try {
			if (flagIdent < 8) {
				selectSQL = setSelectSQL("*", personTable, "PID = " + selectedPersonPID);
				personData = requestTableData(selectSQL, dataBaseIndex);
				personData.first();
				flagValue = personData.getInt(flagFieldsT401[flagIdent-1]);
				if (flagValue != -1 && flagValue < flagValueOptions.length) {
					setting = flagValueOptions[flagValue];
				} else {
					setting = " F-ERR";
				}
			} else {
				selectSQL = setSelectSQL("*", flagSettingValues,
											"ENTITY_RPID = " + selectedPersonPID +
											" AND FLAG_IDENT = " + flagIdent);
				flagData = requestTableData(selectSQL, dataBaseIndex);
				flagData.last();
				if (flagData.getRow() > 0) {
					flagData.first();
					flagValue = flagData.getInt("FLAG_INDEX");
					if (flagValue != -1 && flagValue < flagValueOptions.length) {
						setting = flagValueOptions[flagValue];
					}
					else {
						setting = " F-ERR";
						//System.out.println(" Flag setting: " + setting +  " Value: " + flagValue + " Options: "  + flagValues);
					}
				} else {
					setting = flagValueOptions[0];
				}
			}
			if (HGlobal.DEBUG) {
				System.out.println(" Flag ident: " + flagIdent
							 + " Setting: " + setting
							 + " Value: " + flagValue
							 + " Options: " + flagValues);
			}
			return setting;
		} catch (SQLException sqle) {
			System.out.println(" HBPersonHandler - getFlagSetting ERROR: " + sqle.getMessage());
			sqle.printStackTrace();
		}
		return "";
	}

/**
 * setPersonFlagSetting(int flagIdent, String newValue)
 * @param flagIdent
 * @param newValue
 * @throws HBException
 */
	public void setPersonFlagSetting(int flagIdent, String newValue) throws HBException {
		String selectSQL, flagValues = null;
		ResultSet flagData, personData;
		try {
			int flagIndex = getIntFlagValue(newValue, flagIdent);
			if (flagIdent < 8) {
				selectSQL = setSelectSQL("*", personTable, "PID = " + selectPersonPID);
				personData = requestTableData(selectSQL, dataBaseIndex);
				personData.first();
				personData.updateInt(flagFieldsT401[flagIdent-1], flagIndex);
				personData.updateRow();
				if (HGlobal.DEBUG)
					System.out.println(" 2-TMG flag person updated: " + selectPersonPID);

			} else {
				selectSQL = setSelectSQL("*", flagSettingValues,
											"ENTITY_RPID = " + selectPersonPID +
											" AND FLAG_IDENT = " + flagIdent);
				flagData = requestTableData(selectSQL, dataBaseIndex);
				flagData.last();
				if (flagIndex != 0 && flagData.getRow() > 0) {
					flagData.first();
					flagData.updateInt("FLAG_INDEX",flagIndex);
					flagData.updateRow();
					if (HGlobal.DEBUG)
						System.out.println(" 2-Row updated: " + newValue + " Options: "  + flagValues);

				} else if (flagIndex == 0 && flagData.getRow() > 0) {
					flagData.first();
					flagData.deleteRow();
					flagData.updateRow();
					if (HGlobal.DEBUG)
						System.out.println(" 2-Default / row deleted: " + newValue + " Options: "  + flagValues);

				} else if (flagIndex > 0 && flagData.getRow() == 0) {
				// moves cursor to the insert row
					flagData.moveToInsertRow();
				// Update new row in H2 database
					flagData.updateLong("PID", lastRowPID(flagSettingValues, dataBaseIndex) + 1);
					flagData.updateLong("CL_COMMIT_RPID", null_RPID);
					flagData.updateLong("FLAG_DEFN_RPID", flagDefinMapPID.get(flagIdent)); // Get PID fir flag defn table
					flagData.updateLong("ENTITY_RPID", selectPersonPID);
					flagData.updateInt("FLAG_IDENT", flagIdent);
					flagData.updateInt("FLAG_INDEX", flagIndex); // New name for falg value
				//Insert row in database
					flagData.insertRow();
					if (HGlobal.DEBUG)
						System.out.println(" 2-Row created: " + newValue + " Options: "  + flagValues);

				} if (flagIndex == 0 && flagData.getRow() == 0) {
					if (HGlobal.DEBUG)
						System.out.println(" 2-Default / no action: : " + newValue + " Options: "  + flagValues);

				}
				if (HGlobal.DEBUG)
					System.out.println(" 3-User flag update: " + flagIndex +  " Setting: " + newValue + " Options: "  + flagValues);

			}
		} catch (SQLException sqle) {
			System.out.println(" HBPersonHandler - setPersonFlagSetting ERROR: " + sqle.getMessage());
			throw new HBException(" HBPersonHandler - setPersonFlagSetting ERROR: " + sqle.getMessage());
		}
	}

/**
 * getFlagIntValue(String newValue, int flagIdent)
 * @param newValue
 * @param flagIdent
 * @return
 * @throws HBException
 */
	private int getIntFlagValue(String newValue, int flagIdent) throws HBException {
		String selectSQL, flagValues = null;
		ResultSet flagDefinition;
		String langCode = HGlobal.dataLanguage;
		if (flagIdent < 8) {
			selectSQL = setSelectSQL("FLAG_VALUES", translatedFlag, "LANG_CODE = '" + langCode
						+ "' AND FLAG_IDENT = " + flagIdent);
		} else {
			selectSQL = setSelectSQL("FLAG_VALUES", translatedFlag, "FLAG_IDENT = " + flagIdent);
		}
		try {
			flagDefinition = requestTableData(selectSQL, dataBaseIndex);
			flagDefinition.first();
			flagValues = flagDefinition.getString("FLAG_VALUES");
			if (HGlobal.DEBUG)
				System.out.println(" HBPersonHandler - getFlagIntValue - flagValues: " + flagValues);

			String[] flagValueOptions = flagValues.split(",");
			for (int i = 0; i < flagValueOptions.length; i++) {
				if (flagValueOptions[i].equals(newValue)) {
					return i;
				}
			}
		} catch (SQLException sqle) {
			System.out.println(" HBPersonHandler - getFlagIntValue ERROR: " + sqle.getMessage());
			throw new HBException(" HBPersonHandler - getFlagIntValue ERROR: " + sqle.getMessage());
		}
		System.out.println(" HBPersonHandler - getFlagIntValue - value not found return 0 - ERROR!: "
					+ newValue + "/" + flagValues);
		return 0;
	}

/**
 * updateStyleAndDates(long personNamePID)
 * @param personNamePID
 * @throws HBException
 */
	private String[] updateStyleAndDates(long personNamePID) throws HBException {
		String nameType = "N";
		long startRPID, endRPID;
		String selectString = setSelectSQL("*", personNameTable,"PID = " + personNamePID);
		ResultSet personNameTable = requestTableData(selectString, dataBaseIndex);
		ResultSet nameStyleTable = pointLibraryResultSet.getNameStyleElementTable(nameType, nameStyles, dataBaseIndex);
		String [] nameStyleAndDates = new String[3];
		nameStyleAndDates[0] = " ";
		nameStyleAndDates[1] = " ";
		nameStyleAndDates[2] = " ";
		try {
			personNameTable.first();
			nameStyleRPID = personNameTable.getLong("NAME_STYLE_RPID");
			nameStyleTable.beforeFirst();
			while (nameStyleTable.next()) {
				if (nameStyleRPID == nameStyleTable.getLong("PID")) {
					nameStyleAndDates[0] = nameStyleTable.getString("NAME_STYLE");
				}
			}
			nameStyleTable.beforeFirst();
			startRPID = personNameTable.getLong("START_HDATE_RPID");
			endRPID = personNameTable.getLong("END_HDATE_RPID");

			if (startRPID != null_RPID) {
				nameStyleAndDates[1] = pointLibraryResultSet.exstractDate(startRPID, dataBaseIndex);
			}
			if (endRPID != null_RPID) {
				nameStyleAndDates[2] = pointLibraryResultSet.exstractDate(endRPID, dataBaseIndex);
			}
			return nameStyleAndDates;
		} catch (SQLException hbe) {
			throw new HBException("ManagePersonNameData - updateStyleAndDates: " + hbe.getMessage());
		}
	}
} // End Class ManagePersonData

/**
 * class ManagePersonNameData extends HBBusinessLayer
 * @author NTo
 * @version v0.01.0027
 * @since 2022-08-02
 */
class ManagePersonNameData extends HBBusinessLayer {
	int dataBaseIndex;
	HBProjectOpenData pointOpenProject;
	long proOffset = 1000000000000000L;
	long null_RPID  = 1999999999999999L;
	protected HBMediaHandler pointMediaHandler = null;
	HBNameStyleManager pointPersonNameStyleData;

	Object[][] nameElementTable;
	Object[][] hiddenElementTable;
	String[] persHeaderData;

	long personNamePID = null_RPID; // Stores the current personName PID
	long baseTypeRPID = null_RPID;
	String[] locnHeaderData;

	ResultSet personNameTableResultSet;
	ResultSet personNameElementTableResultSet;
	long newPersonTablePID = null_RPID;
    long newPersonNameTablePID = null_RPID;
    long newNamesTableElementsPID = null_RPID;
    long nameStyleRPID = proOffset + 1;
	boolean namePrimary = true; // Setting primary if first nameTableRecord
	private int nameEventType = 1037; // Temp setting name variant - overwritten if changed

	int nameStyleIndex = 0;
	String selectedStyle = "";
	int defaultStyleIndex = 0;

	String[] nameStyleAndDates = new String[4];
	boolean primaryName = false;
	long[] nameStylePID;

	boolean allElements = true;
	boolean hiddenElements = false;
	int numberHidden = 0;

	String[] nameStyleNames;
	String[] nameStyleText;
	String[] nameStyleCodeString;
	String[] nameStyleDescriptionString;
	boolean[] isDefaultNameStyle;
	boolean[] isTmgNameStyle;
	//----------------------------
	String[] nameStyleElementCodes;
	String[][] personNameElementsData;

	ResultSet nameStyleTable;
	ResultSet nameElementRS;
	HashMap<String,String> elementCodeMap = new HashMap<>();
	HashMap<String,String> tmgCodeMap = new HashMap<>();
	// Records the name element changes from HG0509ManagePersonName
	HashMap<String,String> personNameChanges;

	String startTMGdate = "", endTMGdate = "";
	long startNameHdatePID = null_RPID, endNameHdatePID = null_RPID;

/**
 * GUI methods
 */

	public int getDefaultStyleIndex() {
		return defaultStyleIndex;
	}

	public void setNameStyleIndex(int index) {
		nameStyleIndex = index;
	}

	public String[] getAvailableStyles() {
		return nameStyleNames;
		//return pointPersonNameStyleData.getNameStyleList();
	}

	public Object[][] getNameElementTable() {
		return nameElementTable;
	}

	public String[] getTableHeader() {
		return persHeaderData;
	}

	public String[] getNameData()  {
		return nameStyleAndDates;
	}

	public boolean getPrimaryName() {
		return primaryName;
	}

	public void setNameEventType(int nameEventType) {
		this.nameEventType = nameEventType;
	}

/**
 * ManagePersonNameData(HDDatabaseLayer pointDBlayer, int dataBaseIndex)
 * @param pointDBlayer
 * @param dataBaseIndex
 * @throws HBException
 */
	public ManagePersonNameData(HDDatabaseLayer pointDBlayer, HBProjectOpenData pointOpenProject) throws HBException {
		super();
		this.pointDBlayer = pointDBlayer;
		this.pointOpenProject = pointOpenProject;
		this.dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
      	setUpDateTranslation();
      	dateFormatSelect();

    // Set up person name styles and location name styles
		pointPersonNameStyleData =  new HBNameStyleManager(pointDBlayer, dataBaseIndex, "Person ");
		pointPersonNameStyleData.updateStyleTable("N", nameStyles, nameElementsDefined);

		setNameStyleTable();
		getAllNameStyleElements(HGlobal.dataLanguage);

		persHeaderData = setTranslatedData("50600","101", false);
		personNameChanges = new HashMap<>();
	}

/**
 * createPersonNameRecord(long pesonTablePID)
 * @return nextPersonNameTablePID
 * @throws HBException
 */
	long createPersonNameRecord(long personTablePID, int nameType) throws HBException {
		this.nameEventType = nameType;
		String selectString;
		namePrimary = false;
		newPersonTablePID = personTablePID;
		newPersonNameTablePID = lastRowPID(personNameTable, dataBaseIndex) + 1;
		personNameTableResultSet = pointOpenProject.getT402Names();
		pointLibraryBusiness.findBaseSubTypePID(0, dataBaseIndex);
		baseTypeRPID = pointLibraryBusiness.baseTypePID[0];
		nameStyleRPID = pointPersonNameStyleData.
				getNameStylePID(pointPersonNameStyleData.getDefaultStyleIndex());
		try {
	// Start transaction handling
			updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
	// Data for T403
			newNamesTableElementsPID = lastRowPID(personNamesTableElements, dataBaseIndex) + 1;
			selectString = setSelectSQL("*", personNamesTableElements,"");
			personNameElementTableResultSet = requestTableData(selectString, dataBaseIndex);
			addToT402_PERS_NAME(personNameTableResultSet);

	 // Iterating HashMap ersonNameChanges for all entries
	        long elementNamePID = newNamesTableElementsPID;
			for (HashMap.Entry<String, String> mapset : personNameChanges.entrySet()) {
				if (HGlobal.DEBUG)
					System.out.println( " Name elements code: " + mapset.getKey() + " = " + mapset.getValue());

	            if (mapset.getValue().trim().length() > 0) {
	            	addToT403_PERS_NAME_ELEMNTS(personNameElementTableResultSet, elementNamePID,
											mapset.getKey(),
											mapset.getValue());
	            	elementNamePID++;
	            }
	        }

		// End transaction and COMMIT
			updateTableData("COMMIT", dataBaseIndex);

	// Close all used result sets
			//personNameTableResultSet.close();
			personNameElementTableResultSet.close();
			return newPersonNameTablePID;

		} catch (HBException hbe) {
			System.out.println(" HBPersonHandler - createPersonNameRecord - HBEexception: " + hbe.getMessage());
			updateTableData("ROLLBACK", dataBaseIndex);
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Error addPerson :  " + hbe.getMessage());
				HB0711Logging.printStackTraceToFile(hbe);
			}
			throw new HBException(" HBPersonHandler - createPersonNameRecord - HBEexception: " + hbe.getMessage());

		} catch (SQLException sqle) {
			System.out.println(" HBPersonHandler - createPersonNameRecord ROLLBACK: \n" + sqle.getMessage());
			updateTableData("ROLLBACK", dataBaseIndex);
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Error createPersonNameRecord - ROLLBACK:  " + sqle.getMessage());
				HB0711Logging.printStackTraceToFile(sqle);
			}
			throw new HBException(" HBPersonHandler - createPersonNameRecord ROLLBACK: \n " + sqle.getMessage());
		}
	}

/**
 * setNameStyleTable()
 */
	public void setNameStyleTable()   {
		int index = 0;
		try {
			nameStyleTable = pointLibraryResultSet.getNameStylesTable(nameStyles, "N" ,dataBaseIndex);
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
					nameStyleIndex = index;
					selectedStyle = nameStyleNames[index];
				}

				if (HGlobal.DEBUG) {
					System.out.println(" Style descriptions " + nameStyleNames[index]
							+ " = " + nameStyleDescriptionString[index]);
				}
				index++;
			}
		} catch (SQLException | HBException hbe) {
			if (HGlobal.DEBUG) {
				System.out.println("HBPesrsonHandler - updateLocationStyleTable: " + hbe.getMessage());
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
			nameStyleElements = pointLibraryResultSet.getNameStyleElements(nameElementsDefined, "N", dataLanguage, dataBaseIndex);
			nameStyleElements.first();
			String elementData = nameStyleElements.getString("ELEMNT_CODES");
			String elementDescription = nameStyleElements.getString("ELEMNT_NAMES");
			String[] nameElementData = elementData.split("\\|");
			String[] nameStyleElementDescription = elementDescription.split("\\|");
			for (int i = 0; i < nameStyleElementDescription.length; i++) {
				//Set up index to name element description with key name element code
				if (HGlobal.DEBUG) {
					System.out.println(" " + i + " element "
							   + nameElementData[i] + "/" + nameStyleElementDescription[i]);
				}
				elementCodeMap.put(nameElementData[i], nameStyleElementDescription[i]);
			}
		} catch (SQLException sqle) {
			if (HGlobal.DEBUG) {
				System.out.println("HBPesrsonHandler - getAllNameStyleElements(): " + sqle.getMessage());
				sqle.printStackTrace();
			}
			throw new HBException("HBPesrsonHandler - getAllNameStyleElements(): " + sqle.getMessage());
		}
	}

/**
 * updateManagePersonNameTable() called from Person Name Manager Show hidden
 * @return
 * @throws HBException
 */
	public int updateManagePersonNameTable() throws HBException  {
		hiddenElements = true;
		return updateManagePersonNameTable(personNamePID);
	}

/**
 * updateManagePersonNameTable
 * @param personNamePID
 * @return
 * @throws HBException
 */
	public int updateManagePersonNameTable(long persNamePID) throws HBException  {
		this.personNamePID = persNamePID;
		int errorCode = 0;
		String[] personNameStyleDestriptions;
    	nameStyleElementCodes = nameStyleCodeString[nameStyleIndex].split("\\|");
      	personNameElementsData = updateRecordedNameData(personNamePID);

    // extract name style and dates
      	exstractStyleAndDates(personNamePID);

		if (isTmgNameStyle[nameStyleIndex]) {
		      	personNameStyleDestriptions = nameStyleDescriptionString[nameStyleIndex].split("\\|");
		    // Set up list of descriptions for TMG US standard Name style - second style in list
		      	String [] standardUSstyleDestriptions = nameStyleDescriptionString[1].split("\\|");
		      	for (int i = 0; i < personNameStyleDestriptions.length; i++) {
					tmgCodeMap.put(nameStyleElementCodes[i], standardUSstyleDestriptions[i]);
				}
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
		//System.out.println(" Hidden #: " + numberHidden);
		return hidden;
	}

/**
 * updateRecordedNameData
 * @param personNamePID
 * @return
 * @throws HBException
 */
	private String[][] updateRecordedNameData(long personNamePID) throws HBException {

		String selectString;
		int rowIndex = 0;
		try {
			selectString = setSelectSQL("*", personNamesTableElements,"OWNER_RPID = " + personNamePID);
			nameElementRS = requestTableData(selectString, dataBaseIndex);
			nameElementRS.last();
			int rows = nameElementRS.getRow();
			String[][] tableData = new String[rows][2];
			nameElementRS.beforeFirst();
			while (nameElementRS.next()) {
				tableData[rowIndex][0] = nameElementRS.getString("ELEMNT_CODE").trim();
				tableData[rowIndex][1] = nameElementRS.getString("NAME_DATA").trim();
				rowIndex++;
			}
			return tableData;
		} catch (HBException | SQLException hbe) {
			errorMessage("Name Mnanagement \n" + hbe.getMessage());
			throw new HBException("Name Mnanagement error\n" + hbe.getMessage());
		}
	}

/**
 * updatePersonBestName(long personNameTablePID)
 * @param personNameTablePID
 * @throws HBException
 */
	public void updatePersonBestName(long personNameTablePID) throws HBException {
		ResultSet personTableNameSet, personTableSet;
		String selectString;
		long ownerRPID;
	// Start transaction
		updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
		try {
			selectString = setSelectSQL("*", personNameTable, "PID = " + personNameTablePID);
			personTableNameSet = requestTableData(selectString, dataBaseIndex);
			personTableNameSet.first();
			ownerRPID = personTableNameSet.getLong("OWNER_RPID");

		// Set the person table	BEST_NAME_RPID
			selectString = setSelectSQL("*", personTable, "PID = " + ownerRPID);
			personTableSet = requestTableData(selectString, dataBaseIndex);
			personTableSet.first();
			personTableSet.updateLong("BEST_NAME_RPID", personNameTablePID);
			personTableSet.updateRow();
			personTableSet.close();

		// Update NAME_PRIMARY	in name table T402
			selectString = setSelectSQL("*", personNameTable, "OWNER_RPID = " + ownerRPID);
			personTableNameSet = requestTableData(selectString, dataBaseIndex);
			personTableNameSet.beforeFirst();
			while (personTableNameSet.next()) {
				if (personTableNameSet.getLong("PID") == personNameTablePID) {
					personTableNameSet.updateBoolean("NAME_PRIMARY",true);
					personTableNameSet.updateInt("NAME_EVNT_TYPE", nameEventType); // Mod 6.12.2024
				} else {
					personTableNameSet.updateBoolean("NAME_PRIMARY",false);
				}

				personTableNameSet.updateRow();
			}
			personTableNameSet.close();
		// End transaction
			updateTableData("COMMIT", dataBaseIndex);
		} catch (SQLException sqle) {
		// Role back transaction
			updateTableData("ROLLBACK", dataBaseIndex);
			throw new HBException("ManagePersonNameData - updatePersonBestName error: " + sqle.getMessage());
		}
	}

/**
 * updateStyleAndDates(long personNamePID)
 * @param personNamePID
 * @throws HBException
 * @throws
 * @throws
 */
	private void exstractStyleAndDates(long personNamePID) throws HBException {
		long nameStyleRPID, startRPID, endRPID, ownerRPID, primaryNameRPID;
		String selectString;
		ResultSet  personNameTableRS, personTableRS;
		selectString = setSelectSQL("*", personNameTable,"PID = " + personNamePID);
		personNameTableRS = requestTableData(selectString, dataBaseIndex);
		nameStyleAndDates[0] = " No selection!";
		nameStyleAndDates[1] = " ";
		nameStyleAndDates[2] = " ";
		nameStyleAndDates[3] = "1037";
		if (HGlobal.DEBUG)
			System.out.println(" updateStyleAndDates - Person Name PID: " + personNamePID);

	// Continue with edit
		try {
	// Test for add person name
			if (personNamePID == null_RPID || isResultSetEmpty(personNameTableRS)) return;
			personNameTableRS.first();
			nameStyleRPID = personNameTableRS.getLong("NAME_STYLE_RPID");
			nameStyleTable.beforeFirst();
			while (nameStyleTable.next()) {
				if (nameStyleRPID == nameStyleTable.getLong("PID")) {
					nameStyleAndDates[0] = nameStyleTable.getString("NAME_STYLE").trim();
				}
			}
			//System.out.println(" Person name style PID: " + nameStyleRPID + "/" + nameStyleAndDates[0] + "/");
			nameStyleTable.beforeFirst();
			startRPID = personNameTableRS.getLong("START_HDATE_RPID");
			endRPID = personNameTableRS.getLong("END_HDATE_RPID");
			nameStyleAndDates[3] = "" + personNameTableRS.getInt("NAME_EVNT_TYPE");

			if (startRPID != null_RPID) {
				nameStyleAndDates[1] = pointLibraryResultSet.exstractDate(startRPID, dataBaseIndex);
			}
			if (endRPID != null_RPID) {
				nameStyleAndDates[2] = pointLibraryResultSet.exstractDate(endRPID, dataBaseIndex);
			}
			ownerRPID = personNameTableRS.getLong("OWNER_RPID");
			selectString = setSelectSQL("*", personTable, "PID = " + ownerRPID);
			personTableRS = requestTableData(selectString, dataBaseIndex);
			personTableRS.first();
			primaryNameRPID = personTableRS.getLong("BEST_NAME_RPID");
			if (primaryNameRPID == personNamePID) {
				primaryName = true;
			} else {
				primaryName = false;
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException("ManagePersonNameData - updateStyleAndDates: " + sqle.getMessage());
		}
	}

/**
 * updateStoredNameStyle(int selectIndex, long personNamePID)
 * @param selectIndex
 * @param personNamePID
 * @throws HBException
 */
	public void updateStoredNameStyle(int selectIndex, long personNamePID) throws HBException {
		long selectedNameStylePID = nameStylePID[selectIndex];
		String selectString = setSelectSQL("*", personNameTable,"PID = " + personNamePID);
		ResultSet personNameTable = requestTableData(selectString, dataBaseIndex);
		try {
			personNameTable.first();
			personNameTable.updateLong("NAME_STYLE_RPID",selectedNameStylePID);
			personNameTable.updateRow();
			personNameTable.close();
		} catch (SQLException hbe) {
			throw new HBException("HBPersonHandler - updateStoredNameStyle: " + hbe.getMessage());
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
						nameElementTable[index][0] = " " + tmgCodeMap.get(element[0]);
					} else {
						nameElementTable[index][0] = " " + elementCodeMap.get(element[0]);
					}

					nameElementTable[index][1] = element[1];
					index++;
				}
			}
		} else {
			if (!isTmgNameStyle[nameStyleIndex]) {
				nameElementTable = new Object[nameStyleElementCodes.length][2];
				for (int i = 0; i < nameStyleElementCodes.length; i++) {
					nameElementTable[i][0] = " " + elementCodeMap.get(nameStyleElementCodes[i]);
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
		if (HGlobal.DEBUG)
			System.out.println(" addToPersonNameChangList(): " +  nameElementCode + "/" + nameData + "/");
		personNameChanges.put(nameElementCode, nameData);
	}

/**
 * updateElementData(int nameType)
 * @throws HBException
 */
	public void updateElementData(int nameType) throws HBException {
		String elementCode, selectString;
		boolean updated = false;
		String personNameData = null;
		selectString = setSelectSQL("*", personNameTable, "PID = " + personNamePID);
		ResultSet nameTableRSet = requestTableData(selectString, dataBaseIndex);
		try {

			updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
			nameTableRSet.first();
			nameTableRSet.updateInt("NAME_EVNT_TYPE", nameType);
			nameTableRSet.updateRow();

			if (personNameChanges.size() == 0) return;

		// Continue to update name elements
			selectString = setSelectSQL("*", personNamesTableElements,"OWNER_RPID = " + personNamePID);
			nameElementRS = requestTableData(selectString, dataBaseIndex);
			for (String styleElementCode : nameStyleElementCodes) {
				updated = false;
				//System.out.println(" StyleElementCode: /" + styleElementCode + "/");
				if (personNameChanges.containsKey(styleElementCode)) {
					personNameData = personNameChanges.get(styleElementCode).trim();
					nameElementRS.beforeFirst();
					while (nameElementRS.next()) {
						elementCode = nameElementRS.getString("ELEMNT_CODE").trim();
						if (styleElementCode.equals(elementCode)) {
							//System.out.println(" *updatePersonElementData(): " + elementCode + "/" + personNameData + "/");
							if (personNameData.length() == 0) {
								nameElementRS.deleteRow();
							} else {
								nameElementRS.updateString("NAME_DATA", personNameData); // Remove element if set to length = 0	.get(elementCode));
								nameElementRS.updateRow();
							}
							updated = true;
						}
					}
					if (updated || personNameData.length() == 0) continue;
					//System.out.println(" *addingPersonElementData(): " + styleElementCode + "/" + personNameData);
				// find lst row in table
					long newPID = lastRowPID(personNamesTableElements, dataBaseIndex) + 1;
				// add new row to name element table
					nameElementRS.moveToInsertRow();
					nameElementRS.updateLong("PID", newPID);
					nameElementRS.updateLong("CL_COMMIT_RPID", null_RPID);
					nameElementRS.updateLong("OWNER_RPID", personNamePID);
					nameElementRS.updateString("ELEMNT_CODE", styleElementCode);
					nameElementRS.updateString("LANG_CODE", " -?-"); // To be removed - only marking
					nameElementRS.updateString("NAME_DATA", personNameData);
				//Insert row
					nameElementRS.insertRow();
				}
			}
			personNameChanges = new HashMap<>();
		// End transaction
			 updateTableData("COMMIT", dataBaseIndex);
		} catch (SQLException | HBException sqle) {
		// Roll back transaction
			updateTableData("ROLLBACK", dataBaseIndex);
			System.out.println("HBPersonHandler - updateElementData(): " + sqle.getMessage());
			sqle.printStackTrace();
		}
	}

/**
 * createNameDates(boolean update, long nameTablePID)
 * @param update
 * @param nameTablePID
 * @throws HBException
 */
	public void createNameDates(boolean update, long nameTablePID, String dateField, Object[] nameDateData) throws HBException {
		ResultSet personNameTableRS;
		String selectString;
		long nameHdatePID;
		//System.out.println(" createNameDates: " + update + "/" + dateField + "/" + nameDateData[0]);
		try {
			selectString = setSelectSQL("*", personNameTable, " PID = " + nameTablePID);
			personNameTableRS = requestTableData(selectString, dataBaseIndex);
			personNameTableRS.first();
			nameHdatePID = personNameTableRS.getLong(dateField);
			if (update) {
		// Convert to Hdate format and update record
				if (nameHdatePID == null_RPID) {
					nameHdatePID = addDateRecord(nameDateData);
				}
				else {
					updateDateRecord(nameDateData, nameHdatePID);
// Convert to Hdate format and add new record
				}
			} else {
				nameHdatePID = addDateRecord(nameDateData);
			}

		// Update the event record hdate
			personNameTableRS.first();
			personNameTableRS.updateLong(dateField, nameHdatePID);
			personNameTableRS.updateRow();
			personNameTableRS.close();
		} catch (SQLException sqle) {
			System.out.println(" HBPersonHnadler - createNmaeDates(): " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" HBPersonHnadler - createNmaeDates(): " + sqle.getMessage());
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
		if (HGlobal.DEBUG)
			System.out.println(" HBPersonHandler - updateDateRecord: " + nameDateData[0]);


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
		if (HGlobal.DEBUG)
			System.out.println(" HBPersonHandler - addDateRecord: " + nameDateData[0]);

		nextHdatePID = lastRowPID(dateTable, dataBaseIndex) + 1;
		selectString = setSelectSQL("*", dateTable, "");
		hreDateResultSet = requestTableData(selectString, dataBaseIndex);
		HdateInput.addT170_HDATES(hreDateResultSet, nextHdatePID, nameDateData);
		return nextHdatePID;
	}

/**
 * addToT402_PERS_NAME(ResultSet hreTable)
 * @param hreTable
 * @throws HBException
 * @throws SQLException
 */
	public void addToT402_PERS_NAME(ResultSet hreTable) throws SQLException {
		if (HGlobal.DEBUG)
			System.out.println("** addTo T402_PERSON_NAMES PID: " + newPersonNameTablePID);

	// moves cursor to the insert row
		hreTable.moveToInsertRow();

	// Update new row in database
		hreTable.updateLong("PID", newPersonNameTablePID);
		hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
		hreTable.updateBoolean("HAS_CITATIONS", false);
		hreTable.updateBoolean("NAME_PRIMARY", namePrimary);
		hreTable.updateInt("NAME_EVNT_TYPE", nameEventType);
		hreTable.updateLong("OWNER_RPID", newPersonTablePID);

	// Set name style RPID
		hreTable.updateLong("NAME_STYLE_RPID", nameStyleRPID);
		hreTable.updateLong("START_HDATE_RPID", null_RPID);
		hreTable.updateLong("END_HDATE_RPID", null_RPID);
		hreTable.updateLong("THEME_RPID", null_RPID);
		hreTable.updateLong("MEMO_RPID", null_RPID);
		hreTable.updateString("SURETY", "3");
	//Insert row
		hreTable.insertRow();
	}

/**
 * addToT403_PERS_NAME_ELEMNTS
 * @param hreTable
 * @param elementNamePID
 * @param elementCode
 * @param nameElement
 * @throws SQLException
 */
	public void addToT403_PERS_NAME_ELEMNTS(ResultSet hreTable,
											long elementNamePID,
											String elementCode,
											String nameElement) throws SQLException {
		if (HGlobal.DEBUG)
			System.out.println("** addTo T403_PERS_NAME_ELEMNTS row: " + elementNamePID);

	// moves cursor to the insert row
		hreTable.moveToInsertRow();
	// -----------------
		hreTable.updateLong("PID", elementNamePID);
		hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
		hreTable.updateLong("OWNER_RPID", newPersonNameTablePID);
		hreTable.updateString("LANG_CODE", ""); // Not needed ********************
		hreTable.updateString("ELEMNT_CODE", elementCode);
		hreTable.updateString("NAME_DATA", nameElement);
	//Insert row
		hreTable.insertRow();

	}

/**
 * errorMessage(String errorMess)
 * @param errorMess
 */
	private void errorMessage(String errorMess) {
		JOptionPane.showMessageDialog(null, errorMess, "Name Manangement", JOptionPane.ERROR_MESSAGE);
	}
} // End class ManagePersonData

/**
 * class AddPerson extends HBBusinessLayer
 * Adding a person to T401 etc
 * @author NTo
 * @version v0.01.0030
 * @since 2023.09.07
 */
class AddPersonRecord extends HBBusinessLayer {
	HBNameStyleManager pointPersonNameStyleData;
	HBNameStyleManager pointLocationStyleData;
	HBPersonFlagManager pointFlagManager;
	HBProjectOpenData pointOpenProject;
	HBPersonHandler pointPersonHandler;

	long null_RPID  = 1999999999999999L;
	long proOffset  = 1000000000000000L;
	boolean birthDateRecorded = false;
	int hdateAddIndex = 1;
	boolean namePrimary = true; // Setting primary if first nameTableRecord
	int nameEventType = 1037; // Temp setting name variant - overwritten if changed

	int dataBaseIndex;
	String[] nameHeaderData;
	String[] locationHeaderData;
    Object[][] tableNameData;
    Object[][] tableNameCiteData;
    Object[][] tableBirthData;
    Object[][] tableBirthCiteData;
    Object [][] tableFlagData;

  // Add T401 global date
    ResultSet personTableResultSet;
    long selectedPersonPID = null_RPID;
    long firstPersonTablePID = null_RPID;
    long newPersonTablePID = null_RPID;
    long newParentRelationPID = null_RPID;
    long newPartnerPID = null_RPID;
    long selectedAssociatePID = null_RPID;
    long spermProviderPID = null_RPID , eggProviderPID = null_RPID;
    long newHREMemoPID = null_RPID;
    long newEventPID = null_RPID;
    long newPartnerEventPID = null_RPID;

    long baseTypeRPID = null_RPID;
    long birthHdatePID = null_RPID, deathHdatePID = null_RPID, nextHdatePID = null_RPID;
    long sortHdatePID = null_RPID;
    long birthPlaceRPID = null_RPID, deathPlaceRPID = null_RPID;
    long memoElementPID = null_RPID;
    int[] flagIndexValues = {0,0,0,0,0,0,0};

    String startTMGdate = "", sortTMGdate = "";

    String reference = "REF";
    String memoElement = "";
    String [] eventRoleList;
    int [] eventRoleType;
    int eventRole = 0;
    int nextVisibleId;

    String [] parentRoleList, eventTypeList, partnerEventList ;
    int [] parentRoleType, eventTypeNumber, partnerEventType;
    int parentRole = 0;

// For Flag T252_FLAG_VALU values
    long newPersonFlagPID;

 // Add T402 global date
    ResultSet personNameTableResultSet;
    long newPersonNameTablePID;
    long nameStyleRPID = proOffset + 1;

 // Add T403 global date
    ResultSet personNameElementTableResultSet;
    long newNamesTableElementsPID;

    int selectedNameStyle;
	String[] tableFlagDescript;
	ResultSet personFlagDefinition, personFlagTranslation;
	ResultSet relationTable;

	// Records the name and birth table element changes from HG0505AddPerson
	HashMap<String,String> personNameChanges = new HashMap<>();
	HashMap<Integer,Integer> personFlagChanges = new HashMap<>();
	HashMap<String,String> personBirthChanges = new HashMap<>();
	HashMap<String,String> personBapthChanges = new HashMap<>();
	HashMap<String,String> personDeathChanges = new HashMap<>();
	HashMap<String,String> personBurialChanges = new HashMap<>();
	HashMap<String,String> personPartnerChanges = new HashMap<>();

/**
 * GUI methods for AddPersonRecord
 */

	public int getDefaultNameStyleIndex() {
		return pointPersonNameStyleData.getDefaultStyleIndex();
	}

	public int getDefaultLocationStyleIndex() {
		return pointLocationStyleData.getDefaultStyleIndex();
	}

	public String[] getNameStyles() {
		return pointPersonNameStyleData.getNameStyleList();
	}

	public String[] getLocationStyles() {
		return pointLocationStyleData.getNameStyleList();
	}

	public Object[][] getNameDataTable(int styleIndex) {
		selectedNameStyle = styleIndex;
		return pointPersonNameStyleData.createNameStyleTable(styleIndex);
	}

	public String[] getNameTableHeader() {
		return nameHeaderData;
	}

	public Object[][] getLocationDataTable(int styleIndex) {
		return pointLocationStyleData.createNameStyleTable(styleIndex);
	}

	public String[] getLocationTableHeader() {
		return locationHeaderData;
	}

	public Object[][] getPersonFlagTable() {
		return pointFlagManager.getPersonFlagTable();
	}

	public String[] getRolesForEvent(int eventType, String selectRoles) throws HBException {
		updateEventRoles(eventType, selectRoles);
		return eventRoleList;
	}

	public int[] getEventRoleTypes() {
		return eventRoleType;
	}

	public String[] getRolesForParent(int eventGroup) throws HBException {
		updateParentRoles(eventGroup);
		return parentRoleList;
	}

	public int[] getRolesTypeParent() {
		return parentRoleType;
	}

	public String[] getPartnerEventList(int eventGroup) throws HBException {
		updatePartnerTypes(eventGroup);
		return partnerEventList;
	}

	public int[] getPartnerEventTypes() {
		return partnerEventType;
	}

	public void setEventRole(int eventRole) {
		this.eventRole = eventRole;
	}

	public void setParentRole(int parentRoleIndex) throws HBException {
		parentRole = parentRoleType[parentRoleIndex];
	}

	public void setNewPartnerPID(long newPartnerPID) {
		newPersonTablePID = newPartnerPID;
	}

/**
 * createNameDates(boolean update, long nameTablePID)
 * @param update
 * @param nameTablePID
 * @throws HBException
 */
	public void createPersonEventDates(boolean update, long eventTablePID, String dateField, Object[] eventDateData) throws HBException {
		ResultSet eventTableRS;
		String selectString;
		long eventHdatePID;
		int eventType;
		if (HGlobal.DEBUG)
			System.out.println(" HBPersonHandler - createPersonEventDates: " + update + "/" + dateField + "/" + eventDateData[0]);

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
			System.out.println(" HBPersonHandler - createPersonEventDates(): " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" HBWhereWhenHandler - createPersonEventDates(): " + sqle.getMessage());
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
		//long selectedPersonPID = pointOpenProject.getSelectedPersonPID();
		long selectedPersonPID = newPersonTablePID;

		//System.out.println(" HBPersonHandler updatePersonData selected person: " + selectedPersonPID);
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
				personTableRS.updateLong(personLocnBirthField, birthPlaceRPID);
				//System.out.println(" Update person birth Hdate: " + nextHdatePID + " Birth: " + birthPlaceRPID);
			}
			if (updateDeath) {
				personTableRS.updateLong(personHdateDeathField, nextHdatePID);
				personTableRS.updateLong(personLocnDeathField, deathPlaceRPID);
				//System.out.println(" Update person death Hdate: " + nextHdatePID + " Death: " + birthPlaceRPID);
			}
			personTableRS.updateRow();
			personTableRS.close();

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" HBPersonHandler - updatePersonData: " + sqle.getMessage());
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
		if (HGlobal.DEBUG)
			System.out.println(" HBPersonHandler - updateDateRecord: " + nameDateData[0]);


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
		if (HGlobal.DEBUG)
			System.out.println(" HBPersonHandler - addDateRecord: " + nameDateData[0]);

		nextHdatePID = lastRowPID(dateTable, dataBaseIndex) + 1;
		selectString = setSelectSQL("*", dateTable, "");
		hreDateResultSet = requestTableData(selectString, dataBaseIndex);
		HdateInput.addT170_HDATES(hreDateResultSet, nextHdatePID, nameDateData);
		return nextHdatePID;
	}


/**
 * updatePartnerRelation(int selectedPartnerType, int priPartnerRole, int secPartnerRole)
 * @param selectedPartnerType
 * @param priPartnerRole
 * @param secPartnerRole
 * @throws HBException
 */
	public long addPartnerRelation(int selectedPartnerType, int priPartnerRole, int secPartnerRole) throws HBException {
		String selectString;
		ResultSet partnerTableRS = null;
		if (HGlobal.DEBUG)
			System.out.println(" Partner relation update: " + selectedPartnerType
								+ "/" + priPartnerRole + "/" + secPartnerRole);

		try {
			updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
			selectedPersonPID = pointOpenProject.getSelectedPersonPID();
			newPartnerPID = lastRowPID(personPartnerTable, dataBaseIndex) + 1;
		// set relationPID
			selectedAssociatePID = newPartnerPID;
			selectString = setSelectSQL("*", personPartnerTable, "");
			partnerTableRS = requestTableData(selectString, dataBaseIndex);
			addToT404_PARTNER(partnerTableRS, selectedPartnerType,
									priPartnerRole, secPartnerRole);
			// End transaction
			updateTableData("COMMIT", dataBaseIndex);
			return newPartnerPID;

		} catch (HBException hbe) {
			updateTableData("ROLLBACK", dataBaseIndex);
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Error updatePartners - ROLLBACK:  " + hbe.getMessage());
				HB0711Logging.printStackTraceToFile(hbe);
			}
			throw new HBException(" \nERROR updatePartners: " + hbe.getMessage());
		}
	}

/**
 * updatePartnerRelation(int selectedPartnerType, int priPartnerRole, int secPartnerRole)
 * @param selectedPartnerType
 * @param priPartnerRole
 * @param secPartnerRole
 * @throws HBException
 */
	public void updatePartnerRelation(long partnerTablePID, int selectedPartnerType, int priPartnerRole, int secPartnerRole) throws HBException {
		String selectString;
		ResultSet partnerTableRS = null;
		int priPartRole = 0, secPartRole = 0;

		try {
		// Start transaction
			updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
			selectString = setSelectSQL("*", personPartnerTable, "PID = " + partnerTablePID);
			partnerTableRS = requestTableData(selectString, dataBaseIndex);
			partnerTableRS.first();

			if (partnerTableRS.getLong("SEC_PARTNER_RPID") == pointOpenProject.getSelectedPersonPID()) {
				priPartRole = secPartnerRole;
				secPartRole = priPartnerRole;
			} else {
				priPartRole = priPartnerRole;
				secPartRole = secPartnerRole;
			}

			if (HGlobal.DEBUG)
				System.out.println(" Partner relation update person: " + pointOpenProject.getSelectedPersonPID() + "/" + selectedPartnerType
									+ "/" + priPartRole + "/" + secPartRole);


			updateT404_PARTNER(partnerTableRS, selectedPartnerType,
					priPartRole, secPartRole);
		// End transaction
			updateTableData("COMMIT", dataBaseIndex);

		} catch (HBException | SQLException hbe) {
			updateTableData("ROLLBACK", dataBaseIndex);
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Error updatePartnerRelation - ROLLBACK:  " + hbe.getMessage());
				HB0711Logging.printStackTraceToFile(hbe);
			}
			throw new HBException(" \nERROR updatePartnerRelation: " + hbe.getMessage());
		}
	}
/**
 * addParentRelation(long newParentPID)
 * @param newParentPID
 * @throws HBException
 */
	public void addParentRelation(long newParentPID) throws HBException  {
		newPersonTablePID = newParentPID;
		try {
			// Start transaction
			updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);

		// Update T401_PERS
			boolean bioParent = updateParentBio(false);
			if (HGlobal.DEBUG) {
				if (bioParent) {
					System.out.println(" Bioparent updated!");
				} else {
					System.out.println(" Other parent updated!");
				}
			}

		// Update T405_PARENT_RELATION
			addParentRelationTableRow();

		// End transaction
			updateTableData("COMMIT", dataBaseIndex);
		} catch (HBException hbe) {
			// Roll back transaction
			updateTableData("ROLLBACK", dataBaseIndex);
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Error addParentRelation - ROLLBACK:  " + hbe.getMessage());
				HB0711Logging.printStackTraceToFile(hbe);
			}
			throw new HBException(" \nERROR addParentRelation: " + hbe.getMessage());
		}
	}

/**
 * updateParentRelation()
 * @throws HBException
 */
	public void addParentRelation() throws HBException {
		try {
			updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);

		// Update T401_PERS
			boolean bioParent = updateParentBio(false);
			if (HGlobal.DEBUG) {
				if (bioParent) {
					System.out.println(" Bioparent updated!");
				} else {
					System.out.println(" Other parent updated!");
				}
			}

		// Update T405_PARENT_RELATION
			addParentRelationTableRow();

		// End transaction
			updateTableData("COMMIT", dataBaseIndex);

		} catch (HBException hbe) {
			updateTableData("ROLLBACK", dataBaseIndex);
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Error updateParentRelation - ROLLBACK:  " + hbe.getMessage());
				HB0711Logging.printStackTraceToFile(hbe);
			}
			throw new HBException(" \nERROR updateParentRelation: " + hbe.getMessage());
		}
	}

/**
 * updateChildRelation()
 * @throws HBException
 */
	public void updateChildRelation() throws HBException {
		try {
			updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);

		// Update T401_PERS
			boolean bioParent = updateParentBio(true);
		if (HGlobal.DEBUG) {
			if (bioParent) {
				System.out.println(" Biochild updated!");
			} else {
				System.out.println(" Other child updated!");
			}
		}

		// Update T405_PARENT_RELATION
			updateChildRelationTable();

		// End transaction
			updateTableData("COMMIT", dataBaseIndex);

		} catch (HBException hbe) {
			updateTableData("ROLLBACK", dataBaseIndex);
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Error updateChildRelation - ROLLBACK:  " + hbe.getMessage());
				HB0711Logging.printStackTraceToFile(hbe);
			}
			throw new HBException(" \nERROR updateChildRelation: " + hbe.getMessage());
		}
	}

/**
 * updateChildRelation()
 * @throws HBException
 */
	public void updateSiblingRelation(int siblingIndex) throws HBException {
		String selectString;
		ResultSet focusSibling;
		try {
			updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
			selectedPersonPID = pointOpenProject.getSelectedPersonPID();
			selectString = setSelectSQL("*", personTable,"PID = " + selectedPersonPID);
			focusSibling = requestTableData(selectString, dataBaseIndex);
			focusSibling.first();
			spermProviderPID = focusSibling.getLong(personFatherField);
			eggProviderPID = focusSibling.getLong(personMotherField);

		// Update T401_PERS
			updateParentSibling(siblingIndex, spermProviderPID, eggProviderPID);

			if (HGlobal.DEBUG) {
				if (siblingIndex == 0) {
					System.out.println(" Bio Sibling updated!");
				} else {
					System.out.println(" Other Sibling updated!");
				}
			}

		// Update T405_PARENT_RELATION
			updateSiblingRelationTable(siblingIndex);

		// End transaction
			updateTableData("COMMIT", dataBaseIndex);

		} catch (HBException hbe) {
			updateTableData("ROLLBACK", dataBaseIndex);
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Error updateChildRelation - ROLLBACK:  " + hbe.getMessage());
				HB0711Logging.printStackTraceToFile(hbe);
			}
			throw new HBException(" \nERROR updateChildRelation: " + hbe.getMessage());
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" \nERROR updateChildRelation: " + sqle.getMessage());
		}
	}

/**
 * Constructor class AddPersonRecord
 */
	public AddPersonRecord(HDDatabaseLayer pointDBlayer,
				HBProjectOpenData pointOpenProject) throws HBException {
		super();
		this.pointDBlayer = pointDBlayer;
		this.pointOpenProject = pointOpenProject;
		this.dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		pointPersonHandler = pointOpenProject.getPersonHandler();

      	setUpDateTranslation();
      	dateFormatSelect();

	// Set up person name styles and location name styles
		pointPersonNameStyleData =  new HBNameStyleManager(pointDBlayer, dataBaseIndex, "Person ");
		pointPersonNameStyleData.updateStyleTable("N", nameStyles, nameElementsDefined);
		pointLocationStyleData =  new HBNameStyleManager(pointDBlayer, dataBaseIndex, "Location ");
		pointLocationStyleData.updateStyleTable("P", nameStyles, nameElementsDefined);
		nameHeaderData = setTranslatedData("50600","101", false);
		locationHeaderData = setTranslatedData("50800","1", false);
/**
 * processFlagTable()
 * @throws HBException
 * From T251_FLAG_DEFN:
 	0 - IS_SYSTEM
	1 - ACTIVE
	2 - GUI_SEQ
	3 - FLAG_IDENT
	4 - DEFAULT_INDEX ?

	From T204_FLAG_TRAN:
	5 - LANG_CODE
	6 - FLAG_NAME
	7 - FLAG_VALUES
	8 - FLAG_DESC
 */
		pointFlagManager = new HBPersonFlagManager(pointDBlayer, dataBaseIndex);
		pointFlagManager.processFlagTable();
	}

/**
 * addToNameChangeList(integer selectedIndex, String nameData)
 * @param selectedIndex
 * @param nameData
 */
	public void addToNameChangeList(int selectedIndex, String nameData) {
		// ** Note: if test edited out for remove space in element table? **
		//if (nameData.length() > 0) {
			String nameElementCode = pointPersonNameStyleData.getNameStyleElementCodes(selectedNameStyle)[selectedIndex];
			if (HGlobal.DEBUG)
				System.out.println(" AddPersonRecord - addToNameChangList(): " +  nameElementCode + " / " + nameData);

			personNameChanges.put(nameElementCode, nameData);
		//}
	}

/**
 * addToFlagChangeList(integer selectedIndex, String nameData)
 * @param selectedIndex
 * @param nameData
 */
	public void addToFlagChangeList(int flagIdent, int selectedIndex) {
		if (HGlobal.DEBUG)
			System.out.println(" AddPersonRecord - addToFlagChangeList(): " +  flagIdent + " setIndex: " + selectedIndex );

		personFlagChanges.put(flagIdent, selectedIndex);
	}

/**
 * void addToLocationChangeList(int eventIndex, int selectedIndex, String nameData)
 * @param eventIndex  1 = birth, 2 = bapth, 3 = death, 4 = burial
 * @param selectedIndex
 * @param nameData
 */
	public void addToLocationChangeList(int eventIndex, int selectedIndex, String nameData) {
		// ** Note: if test edited out for remove space in element table? **
		//if (nameData.length() > 0) {
			String nameElementCode = pointLocationStyleData.getNameStyleElementCodes(selectedNameStyle)[selectedIndex];
			if (HGlobal.DEBUG)
				System.out.println(" AddPersonRecord - addToLocationChangeList(): " +  nameElementCode + " / " + nameData);

			if (eventIndex == 1) {
				personBirthChanges.put(nameElementCode, nameData);
			} else if (eventIndex == 2) {
				personBapthChanges.put(nameElementCode, nameData);
			} else if (eventIndex == 3) {
				personDeathChanges.put(nameElementCode, nameData);
			} else if (eventIndex == 4) {
				personBurialChanges.put(nameElementCode, nameData);
			} else if (eventIndex == 5) {
				personPartnerChanges.put(nameElementCode, nameData);
			} else {
				System.out.println(" addToLocationChangeList error: " + eventIndex);
				errorMessage(" Addlocation to change list", " addToLocationChangeList error: " + eventIndex);
			}
		//}
	}

/**
 * updateFromGUIMemo(String memoElement) for add person
 * @param memoElement
 * @param memoOwnerTable
 * @param nextOwnerPID
 * @throws HBException
 */
	public void createAddPersonGUIMemo(String memoElement) throws HBException {
		this.memoElement = memoElement;

	//	No memo element if textlength is zero
		if (memoElement.length() == 0) {
			return;
		}
		try {
			updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
			HREmemo pointHREmemo = new HREmemo(pointDBlayer, dataBaseIndex);
			newHREMemoPID = pointHREmemo.addMemoRecord(memoElement);

		// Now commit all updated for memo
			updateTableData("COMMIT", dataBaseIndex);
		} catch (HBException  hbe) {
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
 * createSelectGUIMemo(String memoElement, String tableName) for select parent, partner and associate
 * @param memoElement
 * @param memoOwnerTable
 * @param nextOwnerPID
 * @throws HBException
 */
	public void createSelectGUIMemo(String memoElement, String tableName) throws HBException {
		this.memoElement = memoElement;
		if (HGlobal.DEBUG)
			System.out.println(" createSelectGUIMemo: " + memoElement + " / " + tableName + " /selectedRela: " + selectedAssociatePID);

	//	No memo element if textlength is zero
		if (memoElement.length() == 0) {
			return;
		}
		try {
			updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
			HREmemo pointHREmemo = new HREmemo(pointDBlayer, dataBaseIndex);
			newHREMemoPID = pointHREmemo.addMemoRecord(memoElement);
			String selectString = setSelectSQL("*", tableName, " PID = " + selectedAssociatePID);
			relationTable = requestTableData(selectString, dataBaseIndex);
			relationTable.first();
			relationTable.updateLong("MEMO_RPID", newHREMemoPID);
			relationTable.updateRow();
			relationTable.close();
		// Now commit all updated for memo
			updateTableData("COMMIT", dataBaseIndex);
		} catch (HBException | SQLException  hbe) {
			updateTableData("ROLLBACK", dataBaseIndex);
			System.out.println(" HBPersonHandler - createSelectGUIMemo error: " + hbe.getMessage());
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Error createSelectGUIMemo: :  " + hbe.getMessage());
				HB0711Logging.printStackTraceToFile(hbe);
			}
			hbe.printStackTrace();
			throw new HBException(" HBPersonHandler - createSelectGUIMemo error: " + hbe.getMessage());
		}
	}

/**
 * String readFromGUIMemo(long memoElementPID)
 * @param memoElementPID
 * @return
 * @throws HBException
 */

	public String readSelectGUIMemo(long tablePID, String tableName) throws HBException {
		if (HGlobal.DEBUG)
			System.out.println(" readSelectGUIMemo: " + tablePID + " / " + tableName);

		String selectString = setSelectSQL("*", tableName, " PID = " + tablePID);
		relationTable = requestTableData(selectString, dataBaseIndex);
		try {
			relationTable.first();
			memoElementPID = relationTable.getLong("MEMO_RPID");
			if (memoElementPID == null_RPID) {
				return " No memo found";
			}
			if (HGlobal.DEBUG)
				System.out.println(" MemoPID: " + memoElementPID);

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Error readSelectGUIMemo:  " + sqle.getMessage());
				HB0711Logging.printStackTraceToFile(sqle);
			}
			throw new HBException(" HBWhereWhenHandler - readSelectGUIMemo error: " + sqle.getMessage());
		}
		HREmemo pointHREmemo = new HREmemo(pointDBlayer, dataBaseIndex);
		return pointHREmemo.readMemo(memoElementPID);
	}

/**
 * updateFromGUIMemo(String memoElement, long memoPID)
 * @param memoElement
 * @param memoPID
 * @throws HBException
 */
	public void updateSelectGUIMemo(String memoElement, long tablePID, String tableName) throws HBException {
		if (HGlobal.DEBUG)
			System.out.println(" updateFromGUIMemo: " + memoElement + " / " + tableName + " / " + tablePID);

		String selectString = setSelectSQL("*", tableName, " PID = " + tablePID);
		HREmemo pointHREmemo = new HREmemo(pointDBlayer, dataBaseIndex);
		try {
			relationTable = requestTableData(selectString, dataBaseIndex);
			relationTable.first();
			memoElementPID = relationTable.getLong("MEMO_RPID");
		// Start transaction
			updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
			if (memoElementPID != null_RPID) {
				pointHREmemo.findT167_MEMOrecord(memoElement, memoElementPID);
			} else {
			// Set existing PID for parent relation
				selectedAssociatePID = relationTable.getLong("PID");
				createSelectGUIMemo(memoElement, tableName);
			}
			relationTable.close();
			updateTableData("COMMIT", dataBaseIndex);
		} catch (SQLException sqle) {
			System.out.println(" HBWhereWhenHandler  - updateSelectGUIMemo ROLLBACK: \n" + sqle.getMessage());
			updateTableData("ROLLBACK", dataBaseIndex);
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Error updateSelectGUIMemo: :  " + sqle.getMessage());
				HB0711Logging.printStackTraceToFile(sqle);
			}
			System.out.println(" HBWhereWhenHandler - updateSelectGUIMemo: " + sqle.getMessage());
			sqle.printStackTrace();
		}
	}

/**
 * updatePartnerEventLink(long newPartnerPID, long newEventPID)
 * @param newPartnerPID
 * @param newEventPID
 * @throws HBException
 */
	public void updatePartnerEventLink(long newPartnerPID, long newEventPID) throws HBException {
		String selectString;
		ResultSet parentRelation;
		try {
			updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
			selectString = setSelectSQL("*", personPartnerTable,"PID = " + newPartnerPID);
			parentRelation = requestTableData(selectString, dataBaseIndex);
			parentRelation.first();
			parentRelation.updateLong("EVNT_RPID",newEventPID);
			parentRelation.updateRow();
			updateTableData("COMMIT", dataBaseIndex);
		} catch (SQLException hbe) {
			updateTableData("ROLLBACK", dataBaseIndex);
			System.out.println(" HBPersonHandler - updatePartnerEventLink error: " + hbe.getMessage());
			hbe.printStackTrace();
			throw new HBException(" HBPersonHandler - updatePartnerEventLink error: " + hbe.getMessage());
		}
	}

/**
 * public void addPersonEvent(int indexEventType, int eventType)
 * @throws HBException
 */
	public long addPersonEvent(int indexEventType, int eventType) throws HBException {
		try {

			if (HGlobal.DEBUG)
				System.out.println(" Add Person Event  index/EventType: " + indexEventType + "/" + eventType);

		// Initiate event create
			CreateEventRecord pointCreateEventRecord = new CreateEventRecord(pointDBlayer, dataBaseIndex, pointOpenProject);
			pointCreateEventRecord.initiateAddEventsToPerson(newPersonTablePID);
			if (indexEventType == 0) {
				newEventPID = pointCreateEventRecord.createEventRecord(eventType, eventRole, personBirthChanges,
								nextHdatePID, sortHdatePID, newHREMemoPID);
				birthPlaceRPID = pointCreateEventRecord.getLocationRecordPID();
			} else if (indexEventType == 1) {
				if (HGlobal.DEBUG)
					System.out.println(" Bapth Event  index/EventType: " + indexEventType + "/" + eventType);

				newEventPID = pointCreateEventRecord.createEventRecord(eventType, eventRole, personBapthChanges,
								nextHdatePID, sortHdatePID, newHREMemoPID);
			} else if (indexEventType == 2) {
				newEventPID = pointCreateEventRecord.createEventRecord(eventType, eventRole, personDeathChanges,
								nextHdatePID, sortHdatePID, newHREMemoPID);
				deathPlaceRPID = pointCreateEventRecord.getLocationRecordPID();
			} else if (indexEventType == 3) {
				newEventPID = pointCreateEventRecord.createEventRecord(eventType, eventRole, personBurialChanges,
								nextHdatePID, sortHdatePID, newHREMemoPID);
			} else if (indexEventType == 4) {
				if (HGlobal.DEBUG)
					System.out.println(" Partner Event  index/EventType: " + indexEventType + "/" + eventType);

			// Event partner record cannot point to an assoc person. Partner table eventRPID points to event.
				pointCreateEventRecord.assocPersonRPID = null_RPID;
				newEventPID = pointCreateEventRecord.createEventRecord(eventType, eventRole, personPartnerChanges,
								nextHdatePID, sortHdatePID, newHREMemoPID);
			} else {
				System.out.println(" Error index/EventType: " + indexEventType + "/" + eventType);
			}

			//updateTableData("COMMIT", dataBaseIndex);
			return newEventPID;
		} catch (HBException hbe) {
			//updateTableData("ROLLBACK", dataBaseIndex);
			System.out.println(" HBPersonHandler - addPersonEvent error: " + hbe.getMessage());
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Error addPersonEvent: :  " + hbe.getMessage());
				HB0711Logging.printStackTraceToFile(hbe);
			}
			throw new HBException(" HBPersonHandler - addPersonEvent error: " + hbe.getMessage());
		}
	}

/**
 * boolean updateParentBio(boolean child)
 * @param child - true if update child / false if update parent
 * @return bioparent = true
 * @throws HBException
 */
	private boolean updateParentBio(boolean child) throws HBException {
		String selectString, parentDatabaseField;
		ResultSet focusPerson;
		selectedPersonPID = pointOpenProject.getSelectedPersonPID();
		if (HGlobal.DEBUG)
			System.out.println(" Focus person: " + selectedPersonPID
					+ " Update Parent role: " + parentRole);

	// Bio Father
		if (parentRole == 1079) {
			parentDatabaseField = personFatherField;
		} else if (parentRole == 1090) {
			parentDatabaseField = personMotherField;
		} else {
			return false;
		}

		if (child) {
			selectString = setSelectSQL("*", personTable,"PID = " + newPersonTablePID);
		} else {
			selectString = setSelectSQL("*", personTable,"PID = " + selectedPersonPID);
		}

		focusPerson = requestTableData(selectString, dataBaseIndex);
		try {
			focusPerson.last();
			if (focusPerson.getRow() == 0) {
				throw new HBException(" updateParentTables\nFocus person not found");
			}
			focusPerson.first();

			if (child) {
				focusPerson.updateLong(parentDatabaseField, selectedPersonPID);
			} else {
				focusPerson.updateLong(parentDatabaseField, newPersonTablePID);
			}

			focusPerson.updateRow();
			focusPerson.close();
			//pointOpenProject.reloadT401Persons();
			return true;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" HBPersonHandler - updateParentBio()\n " + sqle.getMessage());
		}
	}

/**
 * updateParentSibling(int siblingIndex, long spermProvider, long eggProvider)
 * @param siblingIndex
 * @param spermProvider
 * @param eggProvider
 * @throws HBException
 */
	private void updateParentSibling(int siblingIndex, long spermProvider, long eggProvider) throws HBException {
		ResultSet siblingSet;
		String selectString = setSelectSQL("*", personTable,"PID = " + newPersonTablePID);
		siblingSet = requestTableData(selectString, dataBaseIndex);
		try {
			siblingSet.first();
			if (siblingIndex == 0) {
				siblingSet.updateLong(personFatherField, spermProvider);
				siblingSet.updateLong(personMotherField, eggProvider);
			} else if (siblingIndex == 1) {
				siblingSet.updateLong(personFatherField, spermProvider);

			} else if (siblingIndex == 2) {
				siblingSet.updateLong(personMotherField, eggProvider);
			}
			siblingSet.updateRow();
			siblingSet.close();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException( "HBPersonHandler - updateParentSibling: " + sqle.getMessage());
		}
	}

/**
 * private void updateParentRelation()
 * @param parentRole
 * @throws HBException
 */
	public void addParentRelationTableRow() throws HBException {
		String selectString;
		ResultSet parentRelationTable;
		long birthEventPID = 0;

	// Find the birth event for child
		selectedPersonPID = pointOpenProject.getSelectedPersonPID();
		birthEventPID = pointLibraryResultSet.getPersonBirthEventPID(selectedPersonPID, dataBaseIndex);
		newParentRelationPID = lastRowPID(personParentTable, dataBaseIndex) + 1;
	// Set relation PID for parent, partner and associate
		selectedAssociatePID = newParentRelationPID;

		selectString = setSelectSQL("*", personParentTable, "");
		parentRelationTable = requestTableData(selectString, dataBaseIndex);
		addToT405_PARENT_RELATION(parentRelationTable, birthEventPID);
	}

/**
 * public void updateParentRelationTableRow(long parentTablePID)
 * @param parentRole
 * @throws HBException
 */
	public void updateParentRelationTableRow(long parentTablePID)  throws HBException {
		String selectString;
		ResultSet parentRelationTable;
		try {
	// Start transaction
			updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
			selectString = setSelectSQL("*", personParentTable, "PID = " + parentTablePID);
			parentRelationTable = requestTableData(selectString, dataBaseIndex);
			updateT405_PARENT_RELATION(parentRelationTable, parentRole);
	// End transaction
			updateTableData("COMMIT", dataBaseIndex);
		} catch (HBException hbe) {
			updateTableData("ROLLBACK", dataBaseIndex);
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Error updateParentRelationTableRow - ROLLBACK:  " + hbe.getMessage());
				HB0711Logging.printStackTraceToFile(hbe);
			}
			throw new HBException(" \nERROR updateParentRelationTableRow: " + hbe.getMessage());
		}
	}

/**
 * private void updateChildRelationTable()
 * @param parentRole
 * @throws HBException
 */
	public void updateChildRelationTable()  throws HBException {
		String selectString;
		ResultSet parentRelationTable;
		newParentRelationPID = lastRowPID(personParentTable, dataBaseIndex) + 1;
		selectString = setSelectSQL("*", personParentTable, "");
		parentRelationTable = requestTableData(selectString, dataBaseIndex);
		addToT405_CHILD_RELATION(parentRelationTable, newEventPID);
		try {
			parentRelationTable.close();
		} catch (SQLException sqle) {
			throw new HBException(" HBPersonHandler - updateChildRelationTable()\n "
					+ sqle.getMessage());
		}
	}

/**
 * private void updateParentRelation()
 * @param parentRole
 * @throws HBException
 */
	public void updateSiblingRelationTable(int siblingIndex)  throws HBException {
		String selectString;
		ResultSet parentRelationTable;
		newParentRelationPID = lastRowPID(personParentTable, dataBaseIndex) + 1;
		selectString = setSelectSQL("*", personParentTable, "");
		parentRelationTable = requestTableData(selectString, dataBaseIndex);
		if (siblingIndex == 0) {
			parentRole = 1079;
			addToT405_SIBLING_RELATION(parentRelationTable, spermProviderPID, newEventPID);
			parentRole = 1090;
			newParentRelationPID++; // Add one to PID because updates not committed
			addToT405_SIBLING_RELATION(parentRelationTable, eggProviderPID, newEventPID);
		} else if (siblingIndex == 1) {
			parentRole = 1079;
			addToT405_SIBLING_RELATION(parentRelationTable, spermProviderPID, newEventPID);
		} else if (siblingIndex == 2) {
			parentRole = 1090;
			addToT405_SIBLING_RELATION(parentRelationTable, eggProviderPID, newEventPID);
		}
		try {
			parentRelationTable.close();
		} catch (SQLException sqle) {
			throw new HBException(" HBPersonHandler - updateSiblingRelationTable()\n "
					+ sqle.getMessage());
		}
	}

/**
 * createPersonRecord(String refText)
 * @param refText
 * @return
 * @throws HBException
 */

	long createPersonRecord(String refText) throws HBException {
		String selectString;
		this.reference = refText;

		newPersonTablePID = lastRowPID(personTable, dataBaseIndex) + 1;
		firstPersonTablePID = firstRowPID(personTable, dataBaseIndex);
		newPersonNameTablePID = lastRowPID(personNameTable, dataBaseIndex) + 1;
		newPersonFlagPID = lastRowPID(flagSettingValues, dataBaseIndex) + 1;
		personTableResultSet = pointOpenProject.getT401Persons();
		personNameTableResultSet = pointOpenProject.getT402Names();
		nextVisibleId = HBLibraryBusiness.findGreatestVisibleId(personTableResultSet) + 1;
		pointLibraryBusiness.findBaseSubTypePID(0, dataBaseIndex);
		baseTypeRPID = pointLibraryBusiness.baseTypePID[0];
		nameStyleRPID = pointPersonNameStyleData.
				getNameStylePID(pointPersonNameStyleData.getDefaultStyleIndex());
		try {
	// Start transaction handling
			updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);

	// Data for T403
			newNamesTableElementsPID = lastRowPID(personNamesTableElements, dataBaseIndex) + 1;
			selectString = setSelectSQL("*", personNamesTableElements,"");
			personNameElementTableResultSet = requestTableData(selectString, dataBaseIndex);

			addToT401_PERSONS(personTableResultSet);

	// Update flag vlaues in T401
			updateFlagValues();

			addToT402_PERS_NAME(personNameTableResultSet);

	 // Iterating HashMap ersonNameChanges for all entries
	        long elementNamePID = newNamesTableElementsPID;
			for (HashMap.Entry<String, String> mapset : personNameChanges.entrySet()) {
				if (HGlobal.DEBUG)
					System.out.println( " Name elements code: " + mapset.getKey() + " = " + mapset.getValue());

	            if (mapset.getValue().trim().length() > 0) {
	            	addToT403_PERS_NAME_ELEMNTS(personNameElementTableResultSet, elementNamePID,
											mapset.getKey(),
											mapset.getValue());
	            	elementNamePID++;
	            }
	        }

	// End transaction and COMMIT
			updateTableData("COMMIT", dataBaseIndex);

	// Close all used result sets
			personNameElementTableResultSet.close();

			return newPersonTablePID;

		} catch (HBException hbe) {
			System.out.println(" HBPersonHandler - addPerson - HBEexception: " + hbe.getMessage());
			updateTableData("ROLLBACK", dataBaseIndex);
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Error addPerson :  " + hbe.getMessage());
				HB0711Logging.printStackTraceToFile(hbe);
			}
			throw new HBException(" HBPersonHandler - addPerson - HBEexception: " + hbe.getMessage());

		} catch (SQLException sqle) {
			System.out.println(" HBPersonHandler - addPerson ROLLBACK: \n" + sqle.getMessage());
			updateTableData("ROLLBACK", dataBaseIndex);
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Error addPerson - ROLLBACK:  " + sqle.getMessage());
				HB0711Logging.printStackTraceToFile(sqle);
			}
			throw new HBException(" HBPersonHandler - addPerson ROLLBACK: \n " + sqle.getMessage());
		}
	}

/**
 * updateEventRoles(int eventType, String selectRoles)
 * @param eventType
 * @throws HBException
 */
	private void updateEventRoles(int eventType, String selectRoles) throws HBException {
		String langCode = HGlobal.dataLanguage;
		ResultSet eventRoles;
		int nrOfRows;
		if (selectRoles.length() == 0) {
			selectRoles = " AND EVNT_ROLE NUM IN (1,2)";
		} else {
			selectRoles = " " + selectRoles;
		}
		eventRoles = pointLibraryResultSet.getRoleNameList(eventType, selectRoles, dataBaseIndex);
		try {
			eventRoles.last();
			nrOfRows = eventRoles.getRow();
			if (nrOfRows == 0) {
				langCode = "en-US";
				eventRoles = pointLibraryResultSet.getRoleNameList(eventType, selectRoles, langCode, dataBaseIndex);
				eventRoles.last();
				nrOfRows = eventRoles.getRow();
				System.out.println(" Eventtype " + eventType + " no role translation for " + HGlobal.dataLanguage
						+ " now using en-US as fallback!");
			}

			if (nrOfRows == 0) {
				throw new HBException(" Eventtype " + eventType + " no role translation availble");
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
			System.out.println(" HBPersonHandler updateEventRole list: " + sqle.getMessage());
			throw new HBException(" HBPersonHandler updateEventRole list: " + sqle.getMessage());
		}
	}

/**
 * updateParentRoles(int eventGroup)
 * @param eventGroup
 * @throws HBException
 */
	private void updateParentRoles(int eventGroup) throws HBException {
		ResultSet parentRoles = pointLibraryResultSet.getEventTypeList(eventGroup, dataBaseIndex);
		try {
			parentRoles.last();
			int nrOfRows = parentRoles.getRow();
			if (HGlobal.DEBUG)
				System.out.println(" Parrent #roles: " + nrOfRows);

			parentRoleList = new String[nrOfRows];
			parentRoleType = new int[nrOfRows];
			int index = 0;
			parentRoles.beforeFirst();
			while (parentRoles.next()) {
				String parentRoleName = parentRoles.getString("EVNT_NAME").trim();
				int parentRoleNr = parentRoles.getInt("EVNT_TYPE");
				if (HGlobal.DEBUG)
					System.out.println(" Partner type: " + index + " - " + parentRoleName + "/" + parentRoleNr);

				parentRoleList[index] = parentRoles.getString("EVNT_NAME").trim();
				parentRoleType[index] = parentRoles.getInt("EVNT_TYPE");
				index++;
			}
		} catch (SQLException sqle) {
			System.out.println(" HBPersonHandler updateParentRoles: " + sqle.getMessage());
			sqle.printStackTrace();
		}
	}

/**
 * updatePartnerTypes(int eventGroup)
 * @param eventGroup
 * @throws HBException
 */
	private void updatePartnerTypes(int eventGroup) throws HBException {
		ResultSet partnerRoles = pointLibraryResultSet.getEventTypeList(eventGroup, dataBaseIndex);
		try {
			partnerRoles.last();
			int nrOfRows = partnerRoles.getRow();
			if (HGlobal.DEBUG)
				System.out.println(" Partner #roles: " + nrOfRows);

			partnerEventList = new String[nrOfRows];
			partnerEventType = new int[nrOfRows];
			int index = 0;
			partnerRoles.beforeFirst();
			while (partnerRoles.next()) {
				String partnerTypeName = partnerRoles.getString("EVNT_NAME").trim();
				int partnerTypeNr = partnerRoles.getInt("EVNT_TYPE");
				if (HGlobal.DEBUG)
					System.out.println(" Partner type: " + index + " - " + partnerTypeName + "/" + partnerTypeNr);

				partnerEventList[index] = partnerRoles.getString("EVNT_NAME").trim();
				partnerEventType[index] = partnerRoles.getInt("EVNT_TYPE");
				index++;
			}
		} catch (SQLException sqle) {
			System.out.println(" HBPersonHandler updatePartnerTypes: " + sqle.getMessage());
			sqle.printStackTrace();
		}
	}

/**
 * updateFlagValues()
 * @throws HBException
 * @throws SQLException
 */
	public void updateFlagValues() throws HBException, SQLException {
		for (HashMap.Entry<Integer, Integer> mapset : personFlagChanges.entrySet()) {
			if (HGlobal.DEBUG)
				System.out.println( " New Flag ident: " + mapset.getKey() + " = " + mapset.getValue());

            setPersonFlagSetting(mapset.getKey(), mapset.getValue());
		}
	}

/**
 * setPersonFlagSetting(int flagIdent, String newValue)
 * @param flagIdent
 * @param newValue
 * @throws HBException
 * @throws SQLException
 */
	private void setPersonFlagSetting(int flagIdent, int flagIndex) throws SQLException, HBException {
		String selectSQL, flagValues = null;
		ResultSet flagData, personData;
		if (flagIdent < 8) {
			selectSQL = setSelectSQL("*", personTable, "PID = " + newPersonTablePID);
			personData = requestTableData(selectSQL, dataBaseIndex);
			personData.first();
			personData.updateInt(flagFieldsT401[flagIdent-1], flagIndex);
			personData.updateRow();
			if (HGlobal.DEBUG)
				System.out.println(" 2-TMG flag person updated: " + newPersonTablePID);

		} else {
			selectSQL = setSelectSQL("*", flagSettingValues,"");
			flagData = requestTableData(selectSQL, dataBaseIndex);
			flagData.last();

		// moves cursor to the insert row
			flagData.moveToInsertRow();
		// Update new row in H2 database
			flagData.updateLong("PID", lastRowPID(flagSettingValues, dataBaseIndex) + 1);
			flagData.updateLong("CL_COMMIT_RPID", null_RPID);
			flagData.updateLong("FLAG_DEFN_RPID", pointFlagManager.flagDefinMapPID.get(flagIdent)); // Get PID fir flag defn table
			flagData.updateLong("ENTITY_RPID", newPersonTablePID);
			flagData.updateInt("FLAG_IDENT", flagIdent);
			flagData.updateInt("FLAG_INDEX", flagIndex); // New name for flag value
		//Insert row in database
			flagData.insertRow();
			if (HGlobal.DEBUG)
				System.out.println(" New Flag Row created: " + flagIndex + " Options: "  + flagValues);

		}
		if (HGlobal.DEBUG)
			System.out.println(" User flag update: " + flagIndex  + " Options: "  + flagValues);

	}

/**
 * addToT401_PERSONS(ResultSet hreTable)
 * @param hreTable
 * @throws HBException
 * @throws SQLException
 */
	private void addToT401_PERSONS(ResultSet hreTable) throws SQLException {
		if (HGlobal.DEBUG)
			System.out.println(" Start - addTo T401_PERS PID: " + newPersonTablePID);

	// moves cursor to the insert row
		hreTable.moveToInsertRow();
	// Update new row in H2 database
		hreTable.updateLong("PID", newPersonTablePID);
		hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
		hreTable.updateBoolean("HAS_CITATIONS", false);
		hreTable.updateBoolean("HAS_OTHER_PARENTS", false); // ************** to be updated
		hreTable.updateLong("VISIBLE_ID", nextVisibleId);
		hreTable.updateLong("ENTITY_TYPE_RPID", baseTypeRPID);
	// ***************************************************
		hreTable.updateLong(personFatherField, null_RPID); // ***  To be updated
		hreTable.updateLong(personMotherField, null_RPID); // ***  To be updated

	// Birth and death place/death updated after event created
		hreTable.updateLong("BIRTH_HDATE_RPID", null_RPID);
		hreTable.updateLong("BIRTH_PLACE_RPID", null_RPID);
		hreTable.updateLong("DEATH_HDATE_RPID", null_RPID);
		hreTable.updateLong("DEATH_PLACE_RPID", null_RPID);
	//***************************************************************'
		hreTable.updateLong("BEST_NAME_RPID", newPersonNameTablePID);
		hreTable.updateLong("BEST_IMAGE_RPID", null_RPID); // Updated in TMGpass_V22a_Exhibits
		hreTable.updateInt("PARTNER_COUNT", 0); // **** To be updated
		hreTable.updateLong("DNA_RPID", null_RPID); //PID to T407, not created
		hreTable.updateLong("LAST_EDIT_HDATE_RPID", null_RPID);
		hreTable.updateLong("MEMO_RPID", null_RPID);
		hreTable.updateString("SURETY", " Not set");

		hreTable.updateInt("BIRTH_SEX", flagIndexValues[0]);
		hreTable.updateInt("LIVING", flagIndexValues[1]);
		hreTable.updateInt("BIRTH_ORDER", flagIndexValues[2]);
		hreTable.updateInt("MULTI_BIRTH", flagIndexValues[3]);
		hreTable.updateInt("ADOPTED", flagIndexValues[4]);
		hreTable.updateInt("ANCESTOR_INT", flagIndexValues[5]);
		hreTable.updateInt("DESCENDANT_INT", flagIndexValues[6]);
		hreTable.updateString("REFERENCE", reference);

	// Fields added in v22c
		hreTable.updateInt("RELATE1",0); // Added V22c
		hreTable.updateInt("RELATE2",0); // Added V22c
		hreTable.updateInt("RELATE3",0); // Added V22c
		hreTable.updateInt("RELATE4",0); // Added V22c

	//Insert row in database
		hreTable.insertRow();
	}

/**
 * addToT402_PERS_NAME(ResultSet hreTable)
 * @param hreTable
 * @throws HBException
 * @throws SQLException
 */
	public void addToT402_PERS_NAME(ResultSet hreTable) throws SQLException {
		if (HGlobal.DEBUG)
			System.out.println("** addTo T402_PERSON_NAMES PID: " + newPersonNameTablePID);

	// moves cursor to the insert row
		hreTable.moveToInsertRow();

	// Update new row in database
		hreTable.updateLong("PID", newPersonNameTablePID);
		hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
		hreTable.updateBoolean("HAS_CITATIONS", false);
		hreTable.updateBoolean("NAME_PRIMARY", namePrimary);
		hreTable.updateInt("NAME_EVNT_TYPE", nameEventType);
		hreTable.updateLong("OWNER_RPID", newPersonTablePID);

	// Set name style RPID
		hreTable.updateLong("NAME_STYLE_RPID", nameStyleRPID);
		hreTable.updateLong("START_HDATE_RPID", null_RPID);
		hreTable.updateLong("END_HDATE_RPID", null_RPID);
		hreTable.updateLong("THEME_RPID", null_RPID);
		hreTable.updateLong("MEMO_RPID", newHREMemoPID);
		hreTable.updateString("SURETY", "3");
	//Insert row
		hreTable.insertRow();
	}

/**
 * addToT403_PERS_NAME_ELEMNTS
 * @param hreTable
 * @param elementNamePID
 * @param elementCode
 * @param nameElement
 * @throws SQLException
 */
	public void addToT403_PERS_NAME_ELEMNTS(ResultSet hreTable,
											long elementNamePID,
											String elementCode,
											String nameElement) throws SQLException {
		if (HGlobal.DEBUG)
			System.out.println("** addTo T403_PERS_NAME_ELEMNTS row: " + elementNamePID);

	// moves cursor to the insert row
		hreTable.moveToInsertRow();
	// -----------------
		hreTable.updateLong("PID", elementNamePID);
		hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
		hreTable.updateLong("OWNER_RPID", newPersonNameTablePID);
		hreTable.updateString("LANG_CODE", ""); // Not needed ********************
		hreTable.updateString("ELEMNT_CODE", elementCode);
		hreTable.updateString("NAME_DATA", nameElement);
	//Insert row
		hreTable.insertRow();

	}

/**
 * addToT405_PARENT_RELATION(int rowPID, ResultSet hreTable)
 * @param rowPID
 * @param hreTable
 * @throws HCException
 */
	private void addToT405_PARENT_RELATION(ResultSet hreTable, long birthEventPID) throws HBException {
		if (HGlobal.DEBUG)
			System.out.println("Start - addToT405_PARENT_RELATION PID: "
								+ newParentRelationPID);

		try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();
			hreTable.updateLong("PID", newParentRelationPID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateBoolean("HAS_CITATIONS", false);
			hreTable.updateLong("PERSON_RPID", selectedPersonPID);
			hreTable.updateLong("PARENT_RPID", newPersonTablePID);
			hreTable.updateLong("START_HDATE_RPID", null_RPID); // No dates available for import from TMG
			hreTable.updateLong("END_HDATE_RPID", null_RPID); // No dates available for import from TMG
			hreTable.updateInt("PARENT_TYPE", parentRole);

		// Processing memo to T167_MEMO_SET
			hreTable.updateLong("MEMO_RPID", null_RPID);
			hreTable.updateString("SURETY", "1");
			hreTable.updateLong("EVNT_RPID", birthEventPID); // PID for child birth event

		//Insert row
			hreTable.insertRow();
			hreTable.close();

		} catch (SQLException sqle) {
			if (HGlobal.DEBUG)
				System.out.println("HBPersonHandler - addTo table addToT405_PARENT_RELATION - error: "
						+ sqle.getMessage());

			sqle.printStackTrace();
			throw new HBException("HBPersonHandler - addTo table addToT405_PARENT_RELATION - error: "
					+ sqle.getMessage());
		}
	}

/**
 * void updateT405_PARENT_RELATION(ResultSet hreTable, int parentRole)
 * @param hreTable
 * @param parentRole
 * @throws HBException
 */
	private void updateT405_PARENT_RELATION(ResultSet hreTable, int parentRole) throws HBException {
		if (HGlobal.DEBUG)
			System.out.println("Start - addToT405_PARENT_RELATION PID: "
								+ newParentRelationPID);

		try {
			hreTable.first();
			hreTable.updateInt("PARENT_TYPE", parentRole);
			hreTable.updateString("SURETY", "1");
		//Update row
			hreTable.updateRow();
			hreTable.close();

		} catch (SQLException sqle) {
			if (HGlobal.DEBUG)
				System.out.println("HBPersonHandler - addTo table addToT405_PARENT_RELATION - error: "
						+ sqle.getMessage());

			sqle.printStackTrace();
			throw new HBException("HBPersonHandler - addTo table addToT405_PARENT_RELATION - error: "
					+ sqle.getMessage());
		}
	}
/**
 * addToT405_CHILD_RELATION(int rowPID, ResultSet hreTable)
 * @param rowPID
 * @param hreTable
 * @throws HCException
 */
	private void addToT405_CHILD_RELATION(ResultSet hreTable, long birthEventPID) throws HBException {
		if (HGlobal.DEBUG)
			System.out.println("Start - addToT405_CHILD_RELATION PID: "
								+ newParentRelationPID);

		try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();
			hreTable.updateLong("PID", newParentRelationPID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateBoolean("HAS_CITATIONS", false);

			//hreTable.updateLong("PERSON_RPID", selectedPerson);
			//hreTable.updateLong("PARENT_RPID", nextPersonTablePID);

			hreTable.updateLong("PERSON_RPID", newPersonTablePID);
			hreTable.updateLong("PARENT_RPID", selectedPersonPID);
			hreTable.updateLong("START_HDATE_RPID", null_RPID); // No dates available for import from TMG
			hreTable.updateLong("END_HDATE_RPID", null_RPID); // No dates available for import from TMG
			hreTable.updateInt("PARENT_TYPE", parentRole);

		// Processing memo to T167_MEMO_SET
			hreTable.updateLong("MEMO_RPID", null_RPID);
			hreTable.updateString("SURETY", "1");
			hreTable.updateLong("EVNT_RPID", birthEventPID);

		//Insert row
			hreTable.insertRow();

		} catch (SQLException sqle) {
			if (HGlobal.DEBUG)
				System.out.println("HBPersonHandler - addTo table addToT405_CHILD_RELATION - error: "
						+ sqle.getMessage());

			sqle.printStackTrace();
			throw new HBException("HBPersonHandler - addTo table addToT405_CHILD_RELATION - error: "
					+ sqle.getMessage());
		}
	}

/**
 * addToT405_CHILD_RELATION(int rowPID, ResultSet hreTable)
 * @param rowPID
 * @param hreTable
 * @throws HCException
 */
	private void addToT405_SIBLING_RELATION(ResultSet hreTable, long parentPID, long birthEventPID) throws HBException {
		if (HGlobal.DEBUG)
			System.out.println("Start - addToT405_CHILD_RELATION PID: "
								+ newParentRelationPID);

		try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();
			hreTable.updateLong("PID", newParentRelationPID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateBoolean("HAS_CITATIONS", false);

			hreTable.updateLong("PERSON_RPID", newPersonTablePID);
			hreTable.updateLong("PARENT_RPID", parentPID);
			hreTable.updateLong("START_HDATE_RPID", null_RPID); // No dates available for import from TMG
			hreTable.updateLong("END_HDATE_RPID", null_RPID); // No dates available for import from TMG
			hreTable.updateInt("PARENT_TYPE", parentRole);

		// Processing memo to T167_MEMO_SET
			hreTable.updateLong("MEMO_RPID", null_RPID);
			hreTable.updateString("SURETY", "1");
			hreTable.updateLong("EVNT_RPID", birthEventPID);

		//Insert row
			hreTable.insertRow();

		} catch (SQLException sqle) {
			if (HGlobal.DEBUG)
				System.out.println("HBPersonHandler - addTo table addToT405_CHILD_RELATION - error: "
						+ sqle.getMessage());

			sqle.printStackTrace();
			throw new HBException("HBPersonHandler - addTo table addToT405_CHILD_RELATION - error: "
					+ sqle.getMessage());
		}
	}

/**
 * addToT404_PARTNER(int primaryIndex, ResultSet hreTable)
 * @param primaryIndex
 * @param hreTable
 * @throws HCException
 */
    protected void addToT404_PARTNER(ResultSet hreTable,
    									int partnerEventType,
    									int priPartnerRole,
    									int secPartnerRole ) throws HBException {
		try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();
		// update rows

			hreTable.updateLong("PID", newPartnerPID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);

		//When TRUE, then this Record is the owner of some records in T735 CITN,
			hreTable.updateBoolean("HAS_CITATIONS",false);

			//hreTable.updateBoolean("IMP_TMG", false); // Mark partner as imported from TMG

		//This is the PID of a T401 record which defines one partner in the relationship
			hreTable.updateLong("PRI_PARTNER_RPID", selectedPersonPID);

		//This is the PID of a T401 record which defines another partner in the relationship
			hreTable.updateLong("SEC_PARTNER_RPID", newPersonTablePID);

		// Updated if partner event generated
			hreTable.updateLong("START_HDATE_RPID", null_RPID);
			hreTable.updateLong("END_HDATE_RPID", null_RPID);

		// PARTNER_TYPE defining the partner type
			hreTable.updateInt("PARTNER_TYPE", 	partnerEventType);

		// Updated if event creating the partnership
			hreTable.updateLong("EVNT_RPID", null_RPID);

		// Set from addPartner window
			hreTable.updateInt("PRI_ROLE",priPartnerRole);
			hreTable.updateInt("SEC_ROLE",secPartnerRole);
			hreTable.updateLong("MEMO_RPID", newHREMemoPID);

		// Surety in TMG also recorded for other partner relationship
			hreTable.updateString("SURETY", "1");

		//Insert row
			hreTable.insertRow();
			hreTable.close();

		} catch (SQLException sqle) {
			System.out.println("HREPersonHandler - addTo table addToT404_PARTNER - error: "
					+ sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException("HREPersonHandler - addTo table addToT404_PARTNER - error: "
					+ sqle.getMessage());
		}
    }
/**
 * addToT404_PARTNER(int primaryIndex, ResultSet hreTable)
 * @param primaryIndex
 * @param hreTable
 * @throws HCException
 */
    protected void updateT404_PARTNER(ResultSet hreTable,
    									int partnerEventType,
    									int priPartnerRole,
    									int secPartnerRole ) throws HBException {
		try {
		// moves cursor to first row
			hreTable.first();
		// update row

		// PARTNER_TYPE defining the partner type
			hreTable.updateInt("PARTNER_TYPE", 	partnerEventType);

		// Set from addPartner window
			hreTable.updateInt("PRI_ROLE",priPartnerRole);
			hreTable.updateInt("SEC_ROLE",secPartnerRole);
			hreTable.updateLong("MEMO_RPID", newHREMemoPID);

		// Surety in TMG also recorded for other partner relationship
			hreTable.updateString("SURETY", "1");

		//Insert row
			hreTable.updateRow();
			hreTable.close();

		} catch (SQLException sqle) {
			System.out.println("HREPersonHandler - updateT404_PARTNER - error: "
					+ sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException("HREPersonHandler - updateT404_PARTNER - error: "
					+ sqle.getMessage());
		}
    }
/**
 * errorMessage(String errorMess)
 * @param errorMess
 */
	public void errorMessage(String title, String errorMess) {
		JOptionPane.showMessageDialog(null, errorMess, title, JOptionPane.ERROR_MESSAGE);
	}
}