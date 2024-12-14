package hre.bila;
/********************************************************************************
 * Class ViewpointHandler extends BusinessLayer
 * Processes data for Viewpoint Menu in User GUI
 * Receives requests from User GUI
 * Sends requests over Database Layer API
 * *******************************************************************************
 * TEST version for import of witness table E.dbf - 2024-9-03
 * *******************************************************************************
 * v0.00.0016 2019-12-20 - First version (N. Tolleshaug)
 * v0.00.0021 2020-04-12 - Moved ResultSets to HBOpenProjectData (N. Tolleshaug)
 * v0.00.0021 2020-04-15 - Corrected ERROR message open project  (N. Tolleshaug)
 * v0.00.0021 2020-04-26 - Uses selected project for tables  (N. Tolleshaug)
 * v0.00.0022 2020-07-08 - hre.pres classes now handled by HBTestPresPersonHandler(NTo)
 * 						 - HBViewpointHandler can now be used for Viewpoint menu
 * v0.00.0023 2020-09-24 - Implemented 3 x Viewpoints reopen screen (N. Tolleshaug)
 * 			  2020-10-03 - Errors corrected position + size setting (N. Tolleshaug)
 * 			  2020-10-04 - Reset VP counters when opening project (N. Tolleshaug)
 * v0.00.0025 2020-11-22 - Framework for processing  data personView (N. Tolleshaug)
 * 			  2020-11-25 - Implemented fix for Father or Mother = 0 (N. Tolleshaug)
 * 			  2020-11-30 - Implemented fix date format + seed error (N. Tolleshaug)
 * 			  2020-12-02 - Exception handling improved (N. Tolleshaug)
 * 			  2020-12-05 - Processing  data personView ex. SEX (N. Tolleshaug)
 * 			  2020-12-05 - Fixed father and mother childs/grandchilds (N. Tolleshaug)
 * 			  2020-12-08 - Added SEX, Partner, Number, visibleID (N. Tolleshaug)
 * 			  2020-12-16 - Stagger initial locations of Viiewpoints (D Ferguson)
 * 			  2020-12-29 - Added new columns to LIFE_FORMS and EVENT (N. Tolleshaug)
 * 			  2020-12-29 - Changed to T552_LOCATION_NAMES for placedata (N. Tolleshaug)
 * 		      2021-01-02 - Correct initial position ViewPoints empty database (N. Tolleshaug)
 * 			  2021-01-20 - Reset person ID and multiple PersonViewPoint (N. Tolleshaug)
 *  		  2021-02-27 - Implemented list of events table in Location VP (N. Tolleshaug)
 *  		  2021-03-06 - Implemented handling of 5 Location VP (N. Tolleshaug)
 *  		  2021-04-15 - Sorting of event from HDATE sort field (N. Tolleshaug)
 * v0.01.0026 2021-05-16 - Included both DDL20a and DDL21a (N. Tolleshaug)
 * 			  2021-05-16 - Fixed error in DDL21a locationVp handling (N. Tolleshaug)
 * v0.01.0027 2021-11-10 - Indexed HDATE Element name tables - faster (N. Tolleshaug)
 * 			  2021-12-02 - Dirty Trick - Wait for location data filled with data (N. Tolleshaug)
 * 			  2021-12-02 - Corrected error in VP Person (N. Tolleshaug)
 * 			  2021-12-11 - Included DDL20a, DDL21a and DDL21c(N. Tolleshaug)
 * 			  2021-12-15 - New processing of DDL21c location ViewPoint (N. Tolleshaug)
 * 			  2022-01-01 - Handling of Images from T676 in Person ViewPoint (N. Tolleshaug)
 * 			  2022-01-29 - Handling of Best Images from T676 in T401 (N. Tolleshaug)
 * 			  2022-01-30 - Handling no image in T676 - error message (N. Tolleshaug)
 * 			  2022-03-01 - PS-VP includes event name and role translated (N. Tolleshaug)
 * 			  2022-03-03 - PS-VP populated for Associates and translated (N. Tolleshaug)
 * 			  2022-03-03 - PS-VP populated for person table and translated (N. Tolleshaug)
 * 			  2022-03-08 - LS-VP handling new start PID after T302 reset (N. Tolleshaug)
 * 			  2022-04-04 - Person-VP event list also include PER2 events (N. Tolleshaug)
 * 			  2022-04-13 - Implemented ViewEventData and populate EventVP (N. Tolleshaug)
 * 			  2022-04-20 - Implemented control of 5 x EventVP (N. Tolleshaug)
 * 			  2022-04-22 - Corrected error in image VP handling (N. Tolleshaug)
 * 			  2022-04-25 - Associate person PID method implemented (N. Tolleshaug)
 * 			  2022-05-11 - Handling of duplicate VP PID implemented (N. Tolleshaug)
 * 			  2022-05-21 - fixed error first location PID np place data (N. Tolleshaug)
 * 			  2022-06-08 - new implementation of exhibits (N. Tolleshaug)
 * 			  2022-06-10 - implement person/locnVP handling like Event (D Ferguson)
 *		      2022-10-01 - Changed date translation to v21h tables (N. Tolleshaug)
 * v0.01.0028 2022-12-05 - Reset of VP data pointers when open project (N. Tolleshaug)
 * 			  2023-01-26 - Setting NS for person name in personVP (N. Tolleshaug)
 * 			  2023-01-29 - Setting NS for location name in locationVP (N. Tolleshaug)
 * 			  2023-02-22 - Moved all exhibit image processing to HBMediaHandler (N. Tolleshaug)
 * 			  2023-04-10 - Support for 3 open projects (N. Tolleshaug)
 * v0.03.0030 2023-06-28 - Person VP updated processing of event table (N. Tolleshaug)
 * 			  2023-06-29 - Use of HDATE sort for sorting event table (N. Tolleshaug)
 * 			  2023-06-29 - Activated witness on/off for person event table (N. Tolleshaug)
 * 			  2023-07-14 - Activated all partners in Gen(0) table (N. Tolleshaug)
 * 			  2023-08-13 - Fix for sorter problem when no events (N. Tolleshaug)
 * 			  2023-08-29 - Fix for sex = null for parents/grandparents (N. Tolleshaug)
 * 			  2023-09-13 - Moved flagFieldsT401 flag Names to HBBusinessLayer (N. Tolleshaug)
 * 			  2023-09-18 - Handle flagindex = -1 and set F-ERR for flag value (N. Tolleshaug)
 * 			  2023-09-19 - Also for TMG flags flagindex = -1 and set F-ERR  (N. Tolleshaug)
 * 			  2023-09-20 - Fix for missing translated parent role in T460 (N. Tolleshaug)
 * v0.03.0031 2023-11-06 - Updated Person VP for DDL 22b (N. Tolleshaug)
 * 			  2023-11-22 - Swapped date and role in Person VP eventlist (N. Tolleshaug)
 * 			  2024-01-14 - Updated processing of eventlist without associate (N. Tolleshaug)
 * 			  2024-02-07 - Updated processing of associate list with partner (N. Tolleshaug)
 * 			  2024-08-10 - Updated processing Event VP person name
 * 			  2024-09-04 - PersonVP, eventVP and locationVP with key assoc (N. Tolleshaug)
 * 			  2024-09-22 - Set for eventVP partner event correct type and name (N. Tolleshaug)
 *			  2024-10-21 - In addPartnerAssocToAssoc() removed self assoc (N. Tolleshaug)
 *			  2024-10-24 - Added more tests for self assocs Person VP (N. Tolleshaug)
 *			  2024-11-05 - Change literal " for" in Event info to ":" (D Ferguson)
 *			  2024-12-06 - line 3689 - if (isResultSetEmpty(partnerRelationRS)) (N. Tolleshaug);
 *			  2024-12-10 - Updated name table for Person VP (N. Tolleshaug)
 *********************************************************************************
 * NOTE 1 - Not implemented handling of missing birth date line 1023
 * NOTE 2 - line 2996 findLocationWithImage() always return false???
 *********************************************************************************/

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import hre.dbla.HDDatabaseLayer;
import hre.gui.HG0401HREMain;
import hre.gui.HG0451SuperIntFrame;
import hre.gui.HG0530ViewEvent;
import hre.gui.HG0530ViewLocation;
import hre.gui.HG0530ViewPeople;
import hre.gui.HGlobal;

/**
 * class ViewpointHandler
 * @author Nils Tolleshaug
 * @version v0.03.0031
 * @since 2019-12-20
 */
public class HBViewPointHandler extends HBBusinessLayer {

	String dBbuild = HGlobal.databaseVersion;

	final static int maxPersonVPIs = HGlobal.maxPersonVPIs;
	final static int maxLocationVPIs = HGlobal.maxLocationVPIs;
	final static int maxEventVPIs = HGlobal.maxEventVPIs;
	final static int maxVPtotal = HGlobal.maxVPtotal;

	public ViewPeopleData[] pointPersonData = new ViewPeopleData[maxPersonVPIs];
	public ViewLocationData[] pointLocationData = new ViewLocationData[maxLocationVPIs];
	public ViewEventData[] pointEventData = new ViewEventData[maxEventVPIs];

    protected ResultSet resultSetT401_PERS;
    protected ResultSet pointT302_GUI_CONFIG;

    long proOffset = 1000000000000000L;
    long null_RPID  = 1999999999999999L;

	int dataBaseIndex;
	String selectSQL;

	boolean initiatedPersonWindows = false;
	boolean initiatedLocationWindows = false;
	boolean initiatedEventWindows = false;

	private String[] locationNameStyle;

/**
 * HBViewpointHandler() constructor
 */
	public HBViewPointHandler(HBProjectOpenData pointOpenProject) {
		super();
	}

/**
 * resetVPdata() - Rest pointers to VP data
 * NB! Cannot handle multiple open projects
 */
	public void resetVPdata() {
		for (int i = 0; i < pointPersonData.length; i++) {
			pointPersonData[i] = null;
		}
		for (int i = 0; i < pointLocationData.length; i++) {
			pointLocationData[i] = null;
		}
		for (int i = 0; i < pointEventData.length; i++) {
			pointEventData[i] = null;
		}
	}

/**
 * get Person data
 * @param personVPnr
 * @return
 * @throws HBException
 */
	public void setWitnessState(int personVPnr, boolean state) throws HBException {
		pointPersonData[personVPnr].setWinessState(state);
	}

	public int getNumberOfEvents(int personVPnr) {
		return pointPersonData[personVPnr].getNumberOfEvents();
	}

	public int getNumberOfAsociates(int personVPnr) {
		return pointPersonData[personVPnr].getNumberOfAsociates();
	}

	public int getNumberOfNames(int personVPnr) {
		return pointPersonData[personVPnr].getNumberOfNames();
	}

	public Object[][] getParentData(int personVPnr) {
		return pointPersonData[personVPnr].getParentTable();
	}

	public String getPersonSex(int personVPnr) {
		return pointPersonData[personVPnr].getSekfSex();
	}

	public String getPersonName(int personVPnr) {
		return pointPersonData[personVPnr].getPersonName();
	}

	public String getPersonReference(int personVPnr) {
		return pointPersonData[personVPnr].getPersonReference();
	}

	public Object[][] getEventData(int personVPnr) {
		return pointPersonData[personVPnr].getEventTable();
	}

	public Object[][] getRelativesGP2Data(int personVPnr) {
		return pointPersonData[personVPnr].getRelativesGP2Table();
	}

	public Object[][] getRelativesGP1Data(int personVPnr) {
		return pointPersonData[personVPnr].getRelativesGP1Table();
	}

	public Object[][] getRelativesGS0Data(int personVPnr) {
		return pointPersonData[personVPnr].getRelativesGS0Table();
	}

	public Object[][] getRelativesGC1Data(int personVPnr) {
		return pointPersonData[personVPnr].getRelativesGC1Table();
	}

	public Object[][] getRelativesGC2Data(int personVPnr) {
		return pointPersonData[personVPnr].getRelativesGC2Table();
	}

	public Object[][] getAssociateData(int personVPnr) {
		return pointPersonData[personVPnr].getAssociateTable();
	}

	public Object[][] getNameData(int personVPnr) {
		return pointPersonData[personVPnr].getNameTable();
	}

	public Object[][] getFlagData(int personVPnr) {
		return pointPersonData[personVPnr].getFlagTable();
	}

	public int getPersonNumberOfImages(int personVPnr) {
		return pointPersonData[personVPnr].pointMediaHandler.getNumberOfImages();
	}

	public ImageIcon getPersonThumbImage(int personVPnr) {
		return pointPersonData[personVPnr].pointMediaHandler.getExhibitImage();
	}

	public ArrayList<ImageIcon> getPersonImageList(int personVPnr) {
		return pointPersonData[personVPnr].pointMediaHandler.getImageList();
	}

/**
 * getPersonEventPID(int personVPnr, int tableRow)
 * @param personVPnr
 * @param tableRow
 * @return PID for event
 */
	public long getPersonEventPID(int personVPnr, int tableRow) {
		return pointPersonData[personVPnr].getEventPID(tableRow);
	}

/**
 * getPersonAssociatePID(int personVPnr, int tableRow)
 * @param personVPnr
 * @param tableRow
 * @return personPID
 */
	public long getPersonAssociatePID(int personVPnr, int tableRow) {
		return pointPersonData[personVPnr].getPersonPID(tableRow);
	}

/**
 * Location data
 * @param locationVPnr
 * @return
 */

	public void setLocationWitnessState(int locationVPnr, boolean state) throws HBException {
		pointLocationData[locationVPnr].setWinessState(state);
	}

	public String getLocationName(int locationVPnr) {
		return pointLocationData[locationVPnr].getPlaceName();
	}

	public int getNumberEvents(int locationVPnr) {
		return pointLocationData[locationVPnr].getNumberEvents();
	}

	public int getNumberPersons(int locationVPnr) {
		return pointLocationData[locationVPnr].getNumberPersons();
	}

	public Object[][] getEventTable(int locationVPnr) {
		return pointLocationData[locationVPnr].getEventTable();
	}

	public Object[][] getPersonTable(int locationVPnr) {
		return pointLocationData[locationVPnr].getPersonTable();
	}

	public int getLocationNumberOfImages(int locationVPnr) {
		return pointLocationData[locationVPnr].pointMediaHandler.getNumberOfImages();
	}

	public ArrayList<ImageIcon> getLocationImageList(int locationVPnr) {
		return pointLocationData[locationVPnr].pointMediaHandler.getImageList();
	}

/**
 * getLocationEventPID(int locationVPnr, int tableRow)
 * @param locationVPnr
 * @param tableRow
 * @return eventTablePID from eventList
 */
	public long getLocationEventPID(int locationVPnr, int tableRow) {
		return pointLocationData[locationVPnr].getEventPID(tableRow);
	}

/**
 * getLocationAssociatePID(int locationVPnr, int tableRow)
 * @param locationVPnr
 * @param tableRow
 * @return
 */

	public long getLocationAssociatePID(int locationVPnr, int tableRow) {
		return pointLocationData[locationVPnr].getPersonPID(tableRow);
	}

/**
 * Handling Event data
 * @param eventVPnr
 * @return
 */

	public void setEventWitnessState(int eventVPnr, boolean state) throws HBException {
		pointEventData[eventVPnr].setWinessState(state);
	}

	public Object[][] getAssociateEventTable(int eventVPnr) {
		return pointEventData[eventVPnr].getAssociateTable();
	}

	public String getEventIdentInfo(int eventVPnr) {
		return pointEventData[eventVPnr].getIdentInfo();
	}

	public String getEventDateInfo(int eventVPnr) {
		return pointEventData[eventVPnr].getDateInfo();
	}

	public String getEventPlaceInfo(int eventVPnr) {
		return pointEventData[eventVPnr].getPlaceInfo();
	}

	public int getEventAssociateNumber(int eventVPnr) {
		return pointEventData[eventVPnr].getNumberOfAsociates();
	}

	public int getEventNumberOfImages(int eventVPnr) {
		return pointEventData[eventVPnr].pointMediaHandler.getNumberOfImages();
	}

	public ArrayList<ImageIcon> getEventImageList(int eventVPnr) {
		return pointEventData[eventVPnr].pointMediaHandler.getImageList();
	}

	public ArrayList<String> gettextImageList(int eventVPnr) {
		return pointEventData[eventVPnr].pointMediaHandler.getTextList();
	}

	public int getEventNumberOfTexts(int eventVPnr) {
		return pointEventData[eventVPnr].pointMediaHandler.getNumberOfTexts();
	}


/**
 * getEventAssociatePID(int eventVPnr, int tableRow)
 * @param eventVPnr
 * @param tableRow
 * @return
 */
	public long getEventAssociatePID(int eventVPnr, int tableRow) {
		return pointEventData[eventVPnr].getPersonPID(tableRow);
	}

/**
 * findClosedVP(String scrrenIDmatch, HBProjectOpenData pointOpenProject )
 * @param scrrenIDmatch
 * @param pointOpenProject
 * @return
 * @throws HBException
 */
	public int findClosedVP(String scrrenIDmatch, HBProjectOpenData pointOpenProject ) throws HBException {
		int indexVP = 0;
		pointT302_GUI_CONFIG = pointOpenProject.pointT302_GUI_CONFIG;
		try {
			pointT302_GUI_CONFIG.beforeFirst();
			while (pointT302_GUI_CONFIG.next()) {
				String ScreenId = pointT302_GUI_CONFIG.getString("GUI_ID");
				boolean open = pointT302_GUI_CONFIG.getBoolean("IS_OPEN");
				if (ScreenId.startsWith(scrrenIDmatch)) {
					if (!open) {
						if (HGlobal.DEBUG) {
							System.out.println(" HBViewpointHandler - findClosedVP: " + ScreenId
								+ " indexVP: " + indexVP);
						}
						return indexVP;
					}
					indexVP++;
				}
			}
		} catch (SQLException sqle) {
			throw new HBException("HBViewpointHandler - findClosedVP error: \n" + sqle.getMessage());
		}
		return -1;
	}

/**
 * fintOpenVP(String scrrenIDmatch, HBProjectOpenData pointOpenProject)
 * @param scrrenIDmatch
 * @param pointOpenProject
 * @return
 * @throws HBException
 */
	public int findOpenVP(String scrrenIDmatch, HBProjectOpenData pointOpenProject) throws HBException {
		int indexVP = 0;
		pointT302_GUI_CONFIG = pointOpenProject.pointT302_GUI_CONFIG;
		try {
			pointT302_GUI_CONFIG.beforeFirst();
			while (pointT302_GUI_CONFIG.next()) {
				String ScreenId = pointT302_GUI_CONFIG.getString("GUI_ID");
				boolean open = pointT302_GUI_CONFIG.getBoolean("IS_OPEN");
				if (ScreenId.startsWith(scrrenIDmatch)) {
					if (open) {
						if (HGlobal.DEBUG) {
							System.out.println(" HBViewpointHandler - findClosedVP: " + ScreenId
								+ " indexVP: " + indexVP);
						}
						return indexVP;
					}
				}
				indexVP++;
			}
		} catch (SQLException sqle) {
			throw new HBException("HBViewpointHandler - findOpenVP error: \n" + sqle.getMessage());
		}
		return -1;
	}

/**
 * testOpenVP(String screenIDcode, HBProjectOpenData pointOpenProject)
 * @param screenIDcode
 * @param pointOpenProject
 * @return true if window open else false
 * @throws HBException
 */
	public boolean testOpenVP(String screenIDcode, HBProjectOpenData pointOpenProject) throws HBException {
		pointT302_GUI_CONFIG = pointOpenProject.pointT302_GUI_CONFIG;
		try {
			pointT302_GUI_CONFIG.beforeFirst();
			while (pointT302_GUI_CONFIG.next()) {
				String screenID = pointT302_GUI_CONFIG.getString("GUI_ID");
				boolean open = pointT302_GUI_CONFIG.getBoolean("IS_OPEN");
				if (screenID.equals(screenIDcode)) {
					if (open) {
						if (HGlobal.DEBUG) {
							System.out.println("HBViewpointHandler - foundOpenVP: " + screenID);
						}
						return true;
					}
				}
			}
			return false;
		} catch (SQLException sqle) {
			throw new HBException("HBViewpointHandler - findClosedVP error: \n" + sqle.getMessage());
		}
	}
/**
 * Set ScreenID for PersonVP
 * @param personVPnr
 * @return
 */
	public String getPersonScreenID(int personVPnr) {
	    String screenID = null;
    	if (personVPnr < maxPersonVPIs) {
    		if (personVPnr == 0) {
				screenID = "53000";
			}
	    	if (personVPnr == 1) {
				screenID = "53001";
			}
	    	if (personVPnr == 2) {
				screenID = "53002";
			}
	    	if (personVPnr == 3) {
				screenID = "53003";
			}
	    	if (personVPnr == 4) {
				screenID = "53004";
			}
	    	return screenID;
    	}
		if (HGlobal.DEBUG) {
			System.out.println("HBViewpointHandler - getPersonScreenID ERROR personVPnr: "
					+ personVPnr);
		}
		return "";
	}

/**
 * Set ScreenID for Location VP
 * @param personVPindex
 * @return
 */
	public String getLocationScreenID(int locationVPnr) {
	    String screenID = null;
    	if (locationVPnr < maxLocationVPIs) {
    		if (locationVPnr == 0) {
				screenID = "53030";
			}
	    	if (locationVPnr == 1) {
				screenID = "53031";
			}
	    	if (locationVPnr == 2) {
				screenID = "53032";
			}
	    	if (locationVPnr == 3) {
				screenID = "53033";
			}
	    	if (locationVPnr == 4) {
				screenID = "53034";
			}
	    	return screenID;
    	}
		if (HGlobal.DEBUG) {
			System.out.println("HBViewpointHandler - getPersonScreenID ERROR personVPnr: "
					+ locationVPnr);
		}
		return "";
	}

/**
 * Set ScreenID for EventVP
 * @param personVPindex
 * @return
 */
	public String getEventScreenID(int eventVPnr) {
	    String screenID = null;
    	if (eventVPnr < maxEventVPIs) {
    		if (eventVPnr == 0) {
				screenID = "53060";
			}
	    	if (eventVPnr == 1) {
				screenID = "53061";
			}
	    	if (eventVPnr == 2) {
				screenID = "53062";
			}
	    	if (eventVPnr == 3) {
				screenID = "53063";
			}
	    	if (eventVPnr == 4) {
				screenID = "53064";
			}
	    	return screenID;
    	}
		if (HGlobal.DEBUG) {
			System.out.println("HBViewpointHandler - getEventScreenID ERROR personVPnr: "
					+ eventVPnr);
		}
		return "";
	}

/**
 * closeVPscreen(String screenID, HBProjectOpenData pointOpenProject)
 * @param screenID
 * @param pointOpenProject
 * @throws HBException
 */
	public void closeVPscreens(HBProjectOpenData pointOpenProject,
			String searchID, int maxNrVPs) throws HBException {
		HG0451SuperIntFrame pointVPscreen;
		for(int i = 0; i < maxNrVPs; i++) {
			int index = findOpenVP(searchID, pointOpenProject);

		// if no more open VPs
			if (index == -1) {
				break;
			}
		    pointVPscreen = (HG0451SuperIntFrame) pointOpenProject.getWindowPointer(index);
		    String screenID = pointVPscreen.getScreenID();

		// Close and Dispose open VPs
			if (pointVPscreen != null) {
				pointOpenProject.closeStatusScreen(screenID);

				// close reminder display
				if (pointVPscreen.reminderDisplay != null) {
					pointVPscreen.reminderDisplay.dispose();
				}

		    // Set frame size in GUI data
				Dimension frameSize = pointVPscreen.getSize();
				pointOpenProject.setSizeScreen(screenID,frameSize);

			// Set position	in GUI data
				Point position = pointVPscreen.getLocation();
				pointOpenProject.setPositionScreen(screenID,position);

			// Close window
				pointVPscreen.dispose();
			}
		}
	}

/**
 * closePersonVPs()
 * @param maxNrVPs
 * @return
 */
	public int closePersonVPs(HBProjectOpenData pointOpenProject) {
		try {
			closeVPscreens(pointOpenProject, "5300", maxPersonVPIs);
			return 0;
		} catch (HBException hbe) {
			if (HGlobal.DEBUG) {
				System.out.println("closePersonVPs() ERROR " + hbe.getMessage());
			}
			return 2;
		}
	}

/**
 * menuActionPersonVP(boolean single) Open ViewPerson(s) for the selected project
 * @param single- true: only one VP - false: all VP's
 * @return error codes for message from main
 */
	public int menuActionPersonVP(HBProjectOpenData pointOpenProject, boolean single) {
		int errorCode = 0;
		try {

			int personVPindex = 0;
			// Reopen only closed windows
			for (int vpNr = 0; vpNr < maxPersonVPIs; vpNr++) {
				personVPindex = findClosedVP("5300", pointOpenProject);  //$NON-NLS-1$
				if (personVPindex >= 0 ) {
					String windowId = getPersonScreenID(personVPindex);
					long personTablePID = pointOpenProject.pointGuiData.getTableViewPointPID(windowId);
					if (personTablePID != null_RPID) {

						errorCode = initiateViewPeople(pointOpenProject, personVPindex, personTablePID, windowId);
						initiatedPersonWindows = true;
						if (HGlobal.DEBUG)
						 {
							System.out.println("Main PersVP open: "  //$NON-NLS-1$
									+ pointOpenProject.getProjectName() + " VPn: " + personVPindex  //$NON-NLS-1$
										+ " ID: " + windowId);	 //$NON-NLS-1$
						}

						if (single) {
							return errorCode;
						}
					} else {
						if (vpNr == 0 && !initiatedPersonWindows) {

							// Find the PID for first person in table
							long firstPersonPID = firstRowPID(personTable, pointOpenProject);
							if (HGlobal.DEBUG) {
								System.out.println(" Initiate first person VP - PID: " + firstPersonPID);
							}
							errorCode = initiateViewPeople(pointOpenProject, 0, firstPersonPID,"53000"); //$NON-NLS-1$

							// Set table PID for first person
							pointOpenProject.pointGuiData.setTableViewPointPID("53000",firstPersonPID);	//$NON-NLS-1$
							initiatedPersonWindows = true;
						}
					}
				}
			}
		// If all opened
			return 0 ;
		} catch (HBException hbe) {
			if (HGlobal.DEBUG)
			 {
				System.out.println("Main View People error:\n" + hbe.getMessage()); //$NON-NLS-1$
			}
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Main View People error: " + hbe.getMessage());
				HB0711Logging.printStackTraceToFile(hbe);
			}
			return 1;
		}
	}

/**
 * Initiate personVP window with selected focus person
 * @param focusPerson
 * @return
 */
    public int initiatePersonVP(HBProjectOpenData pointOpenProject, long tablePersonPID) {
    	int errorCode = 0;
		try {

			int personVPindex = findClosedVP("5300", pointOpenProject); //$NON-NLS-1$
			if (personVPindex >= 0 ) {
				String screenId = getPersonScreenID(personVPindex);
				pointOpenProject.pointGuiData.setTableViewPointPID(screenId, tablePersonPID);
				errorCode = initiateViewPeople(pointOpenProject,
																  personVPindex,
																  tablePersonPID,
																  screenId );
				if (errorCode > 0) {
					return errorCode;
				}
			} else {
				errorCode = 1;
				if (HGlobal.DEBUG)
				 {
					System.out.println(" \"Max number of open person VPs! vpnr: "
							+ personVPindex + " Error: " + errorCode);  //$NON-NLS-1$
				}
			}
			return errorCode;
    	} catch (HBException e) {
    		System.out.println("HG0507PersonSelect - Exception: " + tablePersonPID); //$NON-NLS-1$
			e.printStackTrace();
		}
		return errorCode;
    }  // End initiatePersonVP

/**
 * Set up HG0530ViewPeople from PersonSelect
 * @param pointOpenProject
 * @param indexVisibleId
 * @return HG0530ViewPeople pointer
 */
	public int initiateViewPeople(HBProjectOpenData pointOpenProject
							,int personVPix
							,long personTablePID
							,String screenIDent) {

		int personVPindex = personVPix;
		String screenID = screenIDent;

		if (HGlobal.DEBUG) {
			System.out.println("HBViewPointHandler initiateViewPeople - Person table PID: " + personTablePID);
		}

		int errorCode = 0;
		HG0530ViewPeople viewPeopleScreen = null;

		int personVPnr = pointOpenProject.getPersonVP();
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		this.resultSetT401_PERS = pointOpenProject.getT401Persons();

		try {

			if (personVPnr < maxPersonVPIs) {

			// Check and handle duplicate PID
				for (int i = 0; i <  maxPersonVPIs; i++) {
					if (pointPersonData[i] != null) {
						long personPID = pointPersonData[i].getPersonPID();
						if (personTablePID == personPID) {
							if (HGlobal.DEBUG) {
								System.out.println("Person VP for PID exist: " + personTablePID);
							}
					// Test if open
								String screenIDtest = pointPersonData[i].getScreenID();
								if (testOpenVP(screenIDtest, pointOpenProject)) {
									if (HGlobal.DEBUG) {
										System.out.println("HBViewPointHandler - Person VP to front VPnr: "
												+ personVPindex + " ScreenID: " + screenIDtest
												+ " personVP-PID: " + personTablePID);
									}
									pointOpenProject.toFrontOpenScreen(screenIDtest);
									return errorCode;

								} else {
									personVPindex = i;
									screenID = screenIDtest;
									if (HGlobal.DEBUG) {
										System.out.println("HBViewPointHandler - Person VP reopen VPnr: "
												+ personVPindex + " ScreenID: " + screenIDtest
												+ " personVP-PID: " + personTablePID);
									}
								}
						}
					}
				}

			// Initiate ViewPeopleData
					pointPersonData[personVPindex] = new ViewPeopleData(pointDBlayer,
													dataBaseIndex, pointOpenProject);

			// Move nameDisplayIndex from HBOpenProjectData to ViewPeopleData
					pointPersonData[personVPindex].setNameDisplayIndex(pointOpenProject.getNameDisplayIndex());

			// Load data from database(dataBaseIndex) into ViewPeopleData
					errorCode = pointPersonData[personVPindex].updatePersonVPtable(personTablePID, screenID);
					if (errorCode > 0) {
						if (errorCode == 3) {
							System.out.println("PersonVP Error Exhibt image: " + " PID=" + personTablePID);
						}
					}

			// Select date format
					dateFormatSelect();
					if (HGlobal.DEBUG) {
						System.out.println("Dateformat: " + dateFormatIndex + " / " + HGlobal.dateFormat);
						System.out.println("ViewPeopleData person table PID: " + personTablePID + "  initiated vp: " + personVPindex);
					}
					HG0401HREMain.mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					  viewPeopleScreen = new HG0530ViewPeople(
							  		  pointOpenProject.getViewPointHandler(),
									  pointOpenProject,
									  personVPindex,
									  screenID);

			// Set size for frame
					  Dimension frameSize = pointOpenProject.pointGuiData.getFrameSize(viewPeopleScreen.screenID);
					  if (frameSize != null) {
						viewPeopleScreen.setSize(frameSize);
					}

			// Set location for frame
					  Point location = pointOpenProject.pointGuiData.getFramePosition(viewPeopleScreen.screenID);
					  if (location == null) {

			// Sets screen top-left corner relative to parent window
						  viewPeopleScreen.setLocation((personVPindex+1)*50, (personVPindex+1)*50);
						  if (HGlobal.DEBUG) {
							System.out.println("HBViewPointHandler - initiateViewPeople - initiate location: " + personVPindex);
						}
					  } else {
						  viewPeopleScreen.setLocation(location);
						  if (HGlobal.DEBUG) {
							System.out.println("HBViewPointHandler - initiateViewPeople - Reset location: " + personVPindex);
						}
					  }

			// Set visible ID in GUI config
					pointOpenProject.pointGuiData.setTableViewPointPID(screenID, personTablePID);

			  		viewPeopleScreen.setVisible(true);
			  		viewPeopleScreen.toFront();

			  		HG0401HREMain.mainFrame.setCursor(Cursor.getDefaultCursor());

		// Mark the window as open in the T302 table
			  		pointOpenProject.pointGuiData.setOpenStatus(viewPeopleScreen.screenID,
			  				pointOpenProject.pointT302_GUI_CONFIG,true);

		// Set class name in GUI config data
			  		pointOpenProject.setClassName(viewPeopleScreen.screenID,viewPeopleScreen.getClassName());

		// register the open screen on HBOpenprojectData
			  		pointOpenProject.registerOpenScreen(viewPeopleScreen);

			  		if (HGlobal.DEBUG) {
						System.out.println("Initiate: " + viewPeopleScreen.getClassName() + " personVP() nr: "
			  											+ personVPindex);
					}

		// Count number of open personVP
			  		if (personVPnr < maxPersonVPIs) {
						pointOpenProject.countUpPersonVP();
					}

	    // return errorCode to HG0507PersonSelect/Main
			  		//System.out.println("HBViewPointHandler - initiateViewPeople - errorCode: " + errorCode);
		  		return errorCode;
			}
			if (HGlobal.DEBUG) {
				System.out.println("initiateViewPeople - Max number of ViewPoints! vpnr: " + personVPindex);
			}
			errorCode = 1;
			return errorCode;
		} catch (HBException hbe) {
			if (HGlobal.DEBUG) {
				System.out.println(" HBViewPointHandler - initiateViewPeople error: \n" + hbe.getMessage());
			}
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("HBViewPointHandler - initiateViewPeople error: " + hbe.getMessage());
				HB0711Logging.printStackTraceToFile(hbe);
			}
			errorCode = 2;
		}
		return errorCode;
	}

/**
 * closeLocationVPs()
 * @param maxNrVPs
 * @return
 */
	public int closeLocationVPs(HBProjectOpenData pointOpenProject) {
		try {
			closeVPscreens(pointOpenProject, "5303", maxLocationVPIs);
			return 0;
		} catch (HBException hbe) {
			if (HGlobal.DEBUG) {
				System.out.println("closeLocationVPs() ERROR" + hbe.getMessage());
			}
			return 2;
		}
	}

/**
 * menuActionLocationVP(boolean single) Open ViewLocation(s) for the selected project
 * @param single- true: only one VP - false: all VP's
 * @return error codes for message from main
 */
	public int menuActionLocationVP(HBProjectOpenData pointOpenProject, boolean single) {
		int locationVPindex = 0;
		int errorCode = 0;
		try {
			int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
			locationNameStyle =  getNameStyleOutputCodes("P", dataBaseIndex);

			for (int vpIndex = 0; vpIndex < maxLocationVPIs; vpIndex++) {
				locationVPindex = findClosedVP("5303", pointOpenProject);  //$NON-NLS-1$
				if (HGlobal.DEBUG) {
					System.out.println("Main LocationVP - Test: " + locationVPindex);
				}
				if (locationVPindex >= 0 ) {
					String windowId = getLocationScreenID(locationVPindex);
					long locationTablePID = pointOpenProject.pointGuiData.getTableViewPointPID(windowId);
					if (locationTablePID != null_RPID) {
						errorCode = initiateViewLocation(pointOpenProject, locationTablePID, locationVPindex, windowId);
						initiatedLocationWindows = true;
						if (HGlobal.DEBUG)
						 {
							System.out.println("Main LocationVP open: "  //$NON-NLS-1$
									+ pointOpenProject.getProjectName() + " VPindex: " + locationVPindex  //$NON-NLS-1$
										+ " ID: " + windowId);	 //$NON-NLS-1$
						}
						if (single) {
							return errorCode;
						}
					} else {
						if (vpIndex == 0 && !initiatedLocationWindows) {

							// First the PID for first location in table
							long firstLocationPID = firstRowPID(locationNameTable, pointOpenProject);

							if (HGlobal.DEBUG) {
								System.out.println(" Initiate first location PID: " + firstLocationPID);
							}

							if (pointLibraryResultSet.
									//selectLocationNameElements(firstLocationPID,locationNameStyle, dataBaseIndex) == null)
									selectLocationName(firstLocationPID,locationNameStyle, dataBaseIndex).length() == 0) {
								firstLocationPID = firstLocationPID + 1;
							}

							errorCode = initiateViewLocation(pointOpenProject, firstLocationPID , 0, "53030"); //$NON-NLS-1$

							// Set table PID for first location
							pointOpenProject.pointGuiData.setTableViewPointPID("53030", firstLocationPID); //$NON-NLS-1$
							initiateViewLocation(pointOpenProject, firstLocationPID, 0, windowId);
							initiatedLocationWindows = true;
						}
					}
				}
			}
		// If all opened
			return 0;
		} catch (HBException hbe) {
			if (HGlobal.DEBUG)
			 {
				System.out.println("Main View Location error:\n" + hbe.getMessage()); //$NON-NLS-1$
			}
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Main View Location error: " + hbe.getMessage());
				HB0711Logging.printStackTraceToFile(hbe);
			}
			return 1;
		}
	}

/**
 * Initiate locationVP window with selected focus location
 * @param focusLocation
 * @return
  */
    public int initiateLocationVP(HBProjectOpenData pointOpenProject, long locationTablePID) {
    	int errorCode = 0;
		try {
			int locationVPindex = findClosedVP("5303", pointOpenProject); //$NON-NLS-1$
			if (locationVPindex >= 0 ) {

				// Set up Location selected
				String screenId = getLocationScreenID(locationVPindex);
				pointOpenProject.pointGuiData.setTableViewPointPID(screenId, locationTablePID);

				errorCode = initiateViewLocation(pointOpenProject,
						locationTablePID,
						locationVPindex,
						screenId);

				if (errorCode > 0) {
					return errorCode;
				}
			} else {
				if (HGlobal.DEBUG)
				 {
					System.out.println("Max number of open location VPs! vpnr: " + locationVPindex);  //$NON-NLS-1$
				}
				errorCode = 1;
			}
			return errorCode;
    	} catch (HBException hbe) {
			hbe.printStackTrace();
		}
		return errorCode;
    }  //End initiateLocationVP

/**
 * Init method used by Location Select
 * @param pointOpenProject
 * @param locationDataPoint
 * @param locationVPindex
 * @param screenID
 * @return HG0530ViewLocation pointer
 */
	public int initiateViewLocation(HBProjectOpenData pointOpenProject,
			long locationTablePID,
			int locationVPix,
			String screenIDent) {

		int errorCode = 0;
		int locationVPindex = locationVPix;
		String screenID = screenIDent;

		HG0530ViewLocation viewLocationScreen = null;
		HG0401HREMain.mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		int locationVPnr = pointOpenProject.getLocationVP();

		try {
			if (locationVPnr >= maxLocationVPIs) {
				if (HGlobal.DEBUG) {
					System.out.println("Max number of ViewPoints! vpnr: " + locationVPindex);
				}
				return 1;
			}
			// Check and handle duplicate PID
				for (int i = 0; i <  maxLocationVPIs; i++) {
					if (pointLocationData[i] != null) {
						long locationPID = pointLocationData[i].getLocationPID();
						if (locationTablePID == locationPID) {
								//System.out.println(" Person VP for PID exist: " + personTablePID);
					// Test if open
								String screenIDtest = pointLocationData[i].getScreenID();
								if (testOpenVP(screenIDtest, pointOpenProject)) {
									if (HGlobal.DEBUG) {
										System.out.println(" Location VP for PID to front indexVP: "
												+ locationVPindex + " ScreenID: " + screenIDtest
												+ " locationVP-PID: " + locationTablePID);
									}
										pointOpenProject.toFrontOpenScreen(screenIDtest);
									return errorCode;

								} else {
									locationVPindex = i;
									screenID = screenIDtest;
									if (HGlobal.DEBUG) {
										System.out.println(" Location VP for PID reopen indexVP: "
												+ locationVPindex + " ScreenID: " + screenIDtest
												+ " locationVP-PID: " + locationTablePID);
									}
								}
						}
					}
				}

				 pointLocationData[locationVPindex] = new ViewLocationData(pointDBlayer,
						 								dataBaseIndex, pointOpenProject);

				 if (dBbuild.startsWith("v22b")) {

			// Move nameDisplayIndex from HBOpenProjectData to ViewPeopleData
							 pointLocationData[locationVPindex].setNameDisplayIndex(pointOpenProject.getNameDisplayIndex());
							 errorCode = pointLocationData[locationVPindex].updateLocationVPtable(locationTablePID, screenID);
				} else {
					System.out.println("HBViewPointHandler selected DataBase not found - " + dBbuild);
				}

				 viewLocationScreen = new HG0530ViewLocation(
						 pointOpenProject.getViewPointHandler(),
								  pointOpenProject,
								  locationVPindex,
								  screenID);

				// Set size for frame
				  Dimension frameSize = pointOpenProject.pointGuiData.getFrameSize(viewLocationScreen.screenID);
				  if (frameSize != null) {
					viewLocationScreen.setSize(frameSize);
				}

				// Set location for frame
				  Point location = pointOpenProject.pointGuiData.getFramePosition(viewLocationScreen.screenID);
				  if (location == null) {

				// Sets screen top-left corner
					  viewLocationScreen.setLocation((locationVPindex + 1)*50, (locationVPindex + 1)*50);
					  if (HGlobal.DEBUG) {
						System.out.println("initiateViewLocation - initiate location: " + locationVPindex);
					}
				  } else {

					  viewLocationScreen.setLocation(location);
					  if (HGlobal.DEBUG) {
						System.out.println("initiateViewLocation - Reset location: " + locationVPindex);
					}
				  }

				// Set visible ID in GUI config
				  pointOpenProject.pointGuiData.setTableViewPointPID(screenID, locationTablePID);

				  viewLocationScreen.setVisible(true);
				  viewLocationScreen.toFront();

				  HG0401HREMain.mainFrame.setCursor(Cursor.getDefaultCursor());

			// Mark the window as open in the T302 table
				  pointOpenProject.pointGuiData.setOpenStatus(viewLocationScreen.screenID,
						  pointOpenProject.pointT302_GUI_CONFIG,true);

			// Set class name in GUI config data
				  pointOpenProject.setClassName(viewLocationScreen.screenID,viewLocationScreen.getClassName());

			// register the open screen on HBOpenprojectData
				  pointOpenProject.registerOpenScreen(viewLocationScreen);

			// Count number of open personVP
			  	if (locationVPnr < maxLocationVPIs) {
					pointOpenProject.countUpLocationVP();
				}

			   if (HGlobal.DEBUG) {
				System.out.println("Initiate LocationVP() nr: " + locationVPnr);
			}

			//return errorcode;
				return 0;

		} catch (HBException hbe) {
			if (HGlobal.DEBUG) {
				System.out.println("Error - Initiate LocationVP() nr: "
						+ locationVPnr + "\n" + hbe.getMessage());
			}
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Error - Initiate LocationVP(): " + hbe.getMessage());
				HB0711Logging.printStackTraceToFile(hbe);
			}
			return 2;
		}
	}

/**
 * closeEventVPs()
 * @param maxNrVPs
 * @return
 */
	public int closeEventVPs(HBProjectOpenData pointOpenProject) {
		try {
			closeVPscreens(pointOpenProject, "5306", maxEventVPIs);
			return 0;
		} catch (HBException hbe) {
			if (HGlobal.DEBUG) {
				System.out.println("closeEventVPs() ERROR" + hbe.getMessage());
			}
			return 2;
		}
	}

/**
 * menuActionEventVP(boolean single) Open ViewEvent(s) for the selected project
 * @param single- true: only one VP - false: all VP's
 * @return error codes for message from main
 */
	public int menuActionEventVP(HBProjectOpenData pointOpenProject, boolean single) {
		int errorCode = 0;
		try {
			int eventVPindex = 0;

			// Reopen only closed windows
			for (int vpNr = 0; vpNr < maxEventVPIs; vpNr++) {
				eventVPindex = findClosedVP("5306", pointOpenProject);  //$NON-NLS-1$
				if (eventVPindex >= 0 ) {
					String windowId = getEventScreenID(eventVPindex);
					long eventTablePID = pointOpenProject.pointGuiData.getTableViewPointPID(windowId);
					if (eventTablePID != null_RPID) {
						errorCode = initiateViewEvent(pointOpenProject, eventTablePID, eventVPindex, windowId);
						initiatedEventWindows = true;
						if (HGlobal.DEBUG)
						 {
							System.out.println("Main EventVP open: "  //$NON-NLS-1$
									+ pointOpenProject.getProjectName() + " VPn: " + eventVPindex  //$NON-NLS-1$
										+ " ID: " + windowId);	 //$NON-NLS-1$
						}

						if (single) {
							return errorCode;
						}
					} else {
						if (vpNr == 0 && !initiatedEventWindows) {

							// Find the PID for first event in table
							long firstEventPID = firstRowPID(eventTable, pointOpenProject);
							if (HGlobal.DEBUG) {
								System.out.println(" Initiate first event VP - PID: " + firstEventPID);
							}
							errorCode = initiateViewEvent(pointOpenProject, firstEventPID , 0, "53060"); //$NON-NLS-1$

							// Set table PID for first person
							pointOpenProject.pointGuiData.setTableViewPointPID("53060",firstEventPID);	//$NON-NLS-1$
							initiatedEventWindows = true;
						}
					}
				}
			}
		// If all opened
			return 0;
		} catch (HBException hbe) {
			if (HGlobal.DEBUG)
			 {
				System.out.println("Main View Event error:\n" + hbe.getMessage()); //$NON-NLS-1$
			}
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Main View Event error: " + hbe.getMessage());
				HB0711Logging.printStackTraceToFile(hbe);
			}
			return 1;
		}
	}


/**
 * Initiate EventVP window with selected focus
 * @param focusEvent PID
 * @return
 */
    public int initiateEventVP(HBProjectOpenData pointOpenProject, long tableEventPID) {
    	int errorCode = 0;
		try {
			int eventVPindex = findClosedVP("5306", pointOpenProject); //$NON-NLS-1$
			if (eventVPindex >= 0 ) {
				String screenEventId = getEventScreenID(eventVPindex);
				pointOpenProject.pointGuiData.setTableViewPointPID(screenEventId, tableEventPID);
				if (HGlobal.DEBUG)
				 {
					System.out.println("HG0530ViewPeople - Initiate Event VP nr: " 		//$NON-NLS-1$
								+ eventVPindex + " ScreenID: " + screenEventId + " EventPID: " + tableEventPID); //$NON-NLS-1$ //$NON-NLS-2$
				}
				errorCode = initiateViewEvent(pointOpenProject,
											  tableEventPID,
											  eventVPindex,
											  screenEventId );

				if (errorCode > 0) {
					return errorCode;
				}
			} else {
				errorCode = 1;
				if (HGlobal.DEBUG)
				 {
					System.out.println("Illegal eventVPs! vpnr: " + eventVPindex);  //$NON-NLS-1$
				}
			}
			return errorCode;
    	} catch (HBException hbe) {
    		System.out.println("HG0530ViewEvent - Exception: " + tableEventPID); //$NON-NLS-1$
			hbe.printStackTrace();
		}
		return errorCode;
    }  // End initiateEventVP

/**
 * Set up HG0530ViewEvent on main screen
 * @param pointOpenProject
 * @return
 * @throws HBException
 */
	public int initiateViewEvent(HBProjectOpenData pointOpenProject,
												long eventTablePID,
												int eventVPindex,
												String screenID)  {

		int errorCode = 0;
		int eventVPnr = pointOpenProject.getEventVP();
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		HG0530ViewEvent viewEventScreen = null;
		try {

			if (eventVPnr < maxEventVPIs) {
			// Check and handle duplicate PID
				for (int i = 0; i <  maxEventVPIs; i++) {
					if (pointEventData[i] != null) {
						long locationPID = pointEventData[i].getEventPID();
						if (eventTablePID == locationPID) {

					// Test if open
								String screenIDtest = pointEventData[i].getScreenID();
								if (testOpenVP(screenIDtest, pointOpenProject)) {
									if (HGlobal.DEBUG) {
										System.out.println(" Event VP for PID set to front indexVP: "
												+ eventVPindex + " ScreenID: " + screenIDtest
												+ " eventVP-PID: " + eventTablePID);
									}
										pointOpenProject.toFrontOpenScreen(screenIDtest);
									return errorCode;

								}
								eventVPindex = i;
								screenID = screenIDtest;
								if (HGlobal.DEBUG) {
									System.out.println(" Event VP for PID reopen indexVP: "
											+ eventVPindex + " ScreenID: " + screenIDtest
											+ " eventVP-PID: " + eventTablePID);
								}
						}
					}
				}

				pointEventData[eventVPindex] = new ViewEventData(pointDBlayer, dataBaseIndex, pointOpenProject);
			  	if (HGlobal.DEBUG) {
					System.out.println("initiateViewEvent - eventVP() nr: "
  						+ eventVPindex + " ScreenID: " + screenID + " EventPID: " + eventTablePID);
				}

			// Create eventVPData
			  	if (dBbuild.startsWith("v22b")) {

			// Move nameDisplayIndex from HBOpenProjectData to ViewPeopleData
					pointEventData[eventVPindex].setNameDisplayIndex(pointOpenProject.getNameDisplayIndex());
			    	pointEventData[eventVPindex].updateEventVPtable(eventTablePID, screenID);

				} else {
					System.out.println("HBViewPointHandler selected DataBase not found - " + dBbuild);
				}

			//Initiate 	HG0530ViewEvent
				HG0401HREMain.mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				  viewEventScreen = new HG0530ViewEvent(
						  pointOpenProject.getViewPointHandler(),
						  			pointOpenProject,
						  			eventVPindex,
						  			screenID);

		// Set size for frame
				  Dimension frameSize = pointOpenProject.pointGuiData.getFrameSize(viewEventScreen.screenID);
				  if (frameSize != null) {
					viewEventScreen.setSize(frameSize);
				}

		// Set location for frame
				  Point location = pointOpenProject.pointGuiData.getFramePosition(viewEventScreen.screenID);

				  if (location == null) {

		// Sets screen top-left corner relative to parent window
					  viewEventScreen.setLocation((eventVPindex+1)*50, (eventVPindex+1)*50);

					  if (HGlobal.DEBUG) {
						System.out.println("initiateViewPeople - initiate location: " + eventVPindex);
					}
				  } else {
					  viewEventScreen.setLocation(location);
					  if (HGlobal.DEBUG) {
						System.out.println("initiateViewPeople - Reset location: " + eventVPindex);
					}
				  }

				  viewEventScreen.setVisible(true);
				  viewEventScreen.toFront();

				  HG0401HREMain.mainFrame.setCursor(Cursor.getDefaultCursor());

	     // Mark the window as open in the T302 table
				  pointOpenProject.pointGuiData.setOpenStatus(viewEventScreen.screenID,
					  pointOpenProject.pointT302_GUI_CONFIG,true);

		// Set class name in GUI config data
				  pointOpenProject.setClassName(viewEventScreen.screenID,"HG0530ViewEvent");

	    // register the open screen on HBOpenprojectData

				  pointOpenProject.registerOpenScreen(viewEventScreen);


		// Count number of open eventVP
			  	  if (eventVPnr < maxEventVPIs) {
					pointOpenProject.countUpEventVP();
				}

				  if (HGlobal.DEBUG) {
					System.out.println("Initiate EventVP() nr: " + eventVPnr);
				}

			} else {
				errorCode = 1;
				if (HGlobal.DEBUG) {
					System.out.println("Max number of ViewPoints! vpnr: " + eventVPindex);
				}
			}
			return errorCode;
		} catch (HBException hbe) {
			if (HGlobal.DEBUG) {
				System.out.println("Error initiateViewEvent: " + hbe.getMessage());
			}
			errorCode = 2;
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("Error initiateViewEvent: " + hbe.getMessage());
				HB0711Logging.printStackTraceToFile(hbe);
			}
		}
		return errorCode;
	}
} // End HBViewpointHandler

/**
 * class ViewPeopleData
 * @author NTo
 * @version v0.00.0025
 * @since 2020-11-16
 */
class ViewPeopleData extends HBBusinessLayer {

	String dBbuild = HGlobal.databaseVersion;
	protected HBMediaHandler pointMediaHandler = null;
	HBProjectOpenData pointOpenProject;

	long proOffset = 1000000000000000L;
	long null_RPID  = 1999999999999999L;

	// Show witness in event list
	boolean includeWitnessEvents = true;

/**
 * List over already included events
 */
		private Vector<Long> eventDuplicateList;

	String langCode;

	// exhibit type index
	int personImage = 0;

	String personName = " Person name";
	String reference;
	long personTablePID;
	long selectedPersonPID; // Stores the selected person PID
	String screenID;
	int dataBaseIndex = 0;
	int nrRows = 0, childRow = 0, row = 0, grandRow = 0, nameRows = 0, eventRows = 0, asociateRows = 0;
	ArrayList<String[]> grandChildList;
	ArrayList<ImageIcon> listOfImages;
	ArrayList<Object[]> asociateList;

// HashMap index for person PID
	HashMap<Integer,Long> personPIDhash;
	ResultSet personSelected;

	private boolean openVPstate = false;

// HashMap index for eventPID's
	HashMap<Integer,Long> eventPIDhash;
	String[] sortStrings;
	JLabel pictLabel;
	ImageIcon image;

// Name styles
		private String[] personStyle; // = {"1100","5000","3000","5200","5700"};
		private String[] locationNameStyle;

		String[] grandParents, parents, person, childType, grandChildType;

	String selfSex; // To be returned for person sex

	Object [][] partnerTable;

	Object [][] parentTable = {
			    {" Not set ", " No recorded father", "  ? "},
				{" Not set ", " No recorded mother", "  ? "}};

	Object [][] eventsTable = {{null, null, null, null, null}};

	Object [][] relativesGP2Table = {
				{null, " Grand Father", null, null, null},
				{null, " Grand Mother", null, null, null},
				{null, " Grand Father", null, null, null},
				{null, " Grand Mother", null, null, null}};

	Object [][] relativesGP1Table = {
				{null, " Father", null, null, null},
				{null, " Mother", null, null, null}};

	Object [][] relativesGS0Table = {
				{null, " Self", null, null, null}};

	//			{" - ", " Partner", " - ", " - ", " - "}};

	Object [][] relativesGC1Table = {
				{null, " Child" , null, null, null},
				{null, " Child", null, null, null},
				{null, " Child", null, null, null},
				{null, " Child", null, null, null}};

	Object [][] relativesGC2Table = {
				{null, " Grandchild", null, null, null},
				{null, " Grandchild", null, null, null},
				{null, " Grandchild", null, null, null},
				{null, " Grandchild", null, null, null},
				{null, " Grandchild", null, null, null},
				{null, " Grandchild", null, null, null},
				{null, " Grandchild", null, null, null},
				{null, " Grandchild", null, null, null}};

	Object [][] associateTable = {
				{" Witness", null, null, null},
				{" Best Man", null, null, null},
				{" Witness", null, null, null},
				{" Witness", null, null, null}};

	Object [][] nameTable = {
				{" Primary", " name", null},
				{null, null, null},
				{null, null, null},
				{null, null, null}};

	Object [][] personFlagTable;


	public ViewPeopleData(HDDatabaseLayer pointDBlayer, int dataBaseIndex, HBProjectOpenData pointOpenProject) {
		super();
		this.pointDBlayer = pointDBlayer;
		this.dataBaseIndex = dataBaseIndex;
		this.pointOpenProject = pointOpenProject;
		dateFormatSelect();
		pointMediaHandler = pointOpenProject.getMediaHandler();

		if (HGlobal.DEBUG) {
			System.out.println("Dateformat: "
					+ dateFormatIndex + " / " + HGlobal.dateFormat);
		}
	}

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

	private void setGenerationType() {
		String screenId = "53000";
		String[] temp;
		grandParents = setTranslatedData(screenId, "401", false);
		parents = setTranslatedData(screenId, "402", false);
		person = setTranslatedData(screenId, "403", false);
		temp = setTranslatedData(screenId, "404", false);
		childType = new String[temp.length];
		childType[0] = temp[2];
		childType[1] = temp[1];
		childType[2] = temp[0];

		temp = setTranslatedData(screenId, "405", false);
		grandChildType = new String[temp.length];
		grandChildType[0] = temp[2];
		grandChildType[1] = temp[1];
		grandChildType[2] = temp[0];

	}

/**
 * Get methods to collect data for personVP
 * @return
 */
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

	public Object[][] getEventTable() {
		return eventsTable;
	}

	public Object[][] getRelativesGP2Table() {
		return relativesGP2Table;
	}

	public Object[][] getRelativesGP1Table() {
		return relativesGP1Table;
	}

	public Object[][] getRelativesGS0Table() {
		return relativesGS0Table;
	}

	public Object[][] getRelativesGC1Table() {
		return relativesGC1Table;
	}

	public Object[][] getRelativesGC2Table() {
		return relativesGC2Table;
	}

	public Object[][] getAssociateTable() {
		return associateTable;
	}

	public Object[][] getNameTable() {
		return nameTable;
	}

	public Object[][] getFlagTable() {
		return personFlagTable;
	}

/**
 * Get event PID's and person PID's
 * @param tableRow
 * @return
 */
	public long getEventPID(int tableRow) {
		return eventPIDhash.get(tableRow);
	}

	public long getPersonPID(int tableRow) {
		return personPIDhash.get(tableRow);
	}

	public String getPersonReference() {
		return reference;
	}

	public String getSekfSex() {
		return selfSex;
	}

	public long getPersonPID() {
		return personTablePID;
	}

	public String getScreenID() {
		return screenID;
	}

	public String getPersonName() {
		return personName;
	}

	public Object[][] getParentTable() {
		return parentTable;
	}

	public int getNumberOfEvents() {
		return eventRows;
	}

	public int getNumberOfAsociates() {
		return asociateRows;
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

	public int updatePersonVPtable(long personTablePID, String screenID) throws HBException {

		int errorCode = 0;
		this.personTablePID = personTablePID;
		this.screenID = screenID;
		langCode = HGlobal.dataLanguage;
		String 	grandFatherMother = "",
				grandFatherFather = "",
			   	grandMotherMother = "",
			   	grandMotherFather = "";

		String 	fatherName = "",
				motherName = "";
//			   	partnerName = "";

		String	femaleSex = "?", maleSex = "?";

		String 	personLifeSpan = null,
				//partnerLifeSpan = null,
				fatherLifeSpan = null,
			   	motherLifeSpan = null,
			   	fatherFatherLifeSpan = null,
			   	fatherMotherLifeSpan = null,
			   	motherFatherLifeSpan = null,
			   	motherMotherLifeSpan = null;

		String 	fatherPartnerName = null,
			   	motherPartnerName = null,
			   	fatherFatherPartnerName = null,
			   	fatherMotherPartnerName = null,
			   	motherFatherPartnerName = null,
			   	motherMotherPartnerName = null;

		String selectString = null;

	// Initiate language translation for date
		setUpDateTranslation();
		dateFormatSelect();

		setGenerationType();

		locationNameStyle =  getNameStyleOutputCodes("P", dataBaseIndex);

	// Initiate index HashMap for eventPID's
		eventPIDhash = new HashMap<>();

		long birthFatherPID, birthMotherPID, fatherPID, motherPID, partnerPID, personPID;
		ResultSet nameSelected, childsSelected; // Local ResultSet
		long birthDatePID, deathDatePID, bestNameRPID, nameStyleRPID;

		try {
			selectString = setSelectSQL("*", personTable, "PID = " + personTablePID);
			personSelected = requestTableData(selectString, dataBaseIndex);
			personSelected.first();
			birthDatePID = personSelected.getLong(personHDateBirthField);
			deathDatePID = personSelected.getLong(personHdateDeathField);
			personLifeSpan = exstractLifeSpan(birthDatePID, deathDatePID);
			selfSex = getPersonFlagSetting(personSelected.getInt("BIRTH_SEX"), 1);
			reference = personSelected.getString("REFERENCE");
	// Find person name style
			bestNameRPID = personSelected.getLong(bestNameField);
			selectString = setSelectSQL("*", personNameTable, "PID = " + bestNameRPID);
			nameSelected =  requestTableData(selectString, dataBaseIndex);
			nameSelected.first();
			nameStyleRPID = nameSelected.getLong("NAME_STYLE_RPID");
			personStyle =  getNameStyleOutputCodes(nameStylesOutput, nameStyleRPID, "N", dataBaseIndex);
	// Extract person name
			personName = pointLibraryResultSet.exstractPersonName(personTablePID, personStyle, dataBaseIndex);

	   // Person  NAME_STYLE_RPID
			birthFatherPID = personSelected.getLong(personFatherField);
			if (birthFatherPID != null_RPID) {
				fatherName = pointLibraryResultSet.exstractPersonName(birthFatherPID,personStyle, dataBaseIndex);
			} else {
				fatherName = " --- ";
			}
			birthMotherPID = personSelected.getLong(personMotherField);
			if (birthMotherPID != null_RPID) {
				motherName = pointLibraryResultSet.exstractPersonName(birthMotherPID, personStyle, dataBaseIndex);
			} else {
				motherName = " --- ";
			}

		// initiate partner list
			partnerPID = findLastPartner(personTablePID);
			if (partnerPID != null_RPID) {
				preparePartnerTable(personTablePID);
			}

	    //Father
			if (birthFatherPID != null_RPID) {
				selectString = setSelectSQL("*", personTable,"PID = " + birthFatherPID);
				personSelected = requestTableData(selectString, dataBaseIndex);
				personSelected.first();
				fatherPID = personSelected.getLong(personFatherField);
				maleSex = getPersonFlagSetting(personSelected.getInt("BIRTH_SEX"), 1);
				grandFatherFather = pointLibraryResultSet.exstractPersonName(fatherPID, personStyle, dataBaseIndex);
				motherPID = personSelected.getLong(personMotherField);
				grandFatherMother = pointLibraryResultSet.exstractPersonName(motherPID, personStyle, dataBaseIndex);
				birthDatePID = personSelected.getLong(personHDateBirthField);
				deathDatePID = personSelected.getLong(personHdateDeathField);
				fatherLifeSpan = exstractLifeSpan(birthDatePID,deathDatePID);
				partnerPID = findLastPartner(birthFatherPID);
				fatherPartnerName = pointLibraryResultSet.exstractPersonName(partnerPID, personStyle, dataBaseIndex);

		// Grand Father partners
				if (fatherPID != null_RPID) {
					selectString = setSelectSQL("*", personTable,"PID = " + fatherPID);
					personSelected = requestTableData(selectString, dataBaseIndex);
					personSelected.first();
					birthDatePID = personSelected.getLong(personHDateBirthField);
					deathDatePID = personSelected.getLong(personHdateDeathField);
					maleSex = getPersonFlagSetting(personSelected.getInt("BIRTH_SEX"), 1);
					fatherFatherLifeSpan = exstractLifeSpan(birthDatePID,deathDatePID);
					partnerPID = findLastPartner(fatherPID);
					fatherFatherPartnerName = pointLibraryResultSet.exstractPersonName(partnerPID, personStyle, dataBaseIndex);
				}

				if (motherPID != null_RPID) {
					selectString = setSelectSQL("*", personTable,"PID = " + motherPID);
					personSelected = requestTableData(selectString, dataBaseIndex);
					personSelected.first();
					birthDatePID = personSelected.getLong(personHDateBirthField);
					deathDatePID = personSelected.getLong(personHdateDeathField);
					femaleSex = getPersonFlagSetting(personSelected.getInt("BIRTH_SEX"), 1);
					fatherMotherLifeSpan = exstractLifeSpan(birthDatePID,deathDatePID);
					partnerPID = findLastPartner(motherPID);
					fatherMotherPartnerName = pointLibraryResultSet.exstractPersonName(partnerPID, personStyle, dataBaseIndex);
				}
			}

		// Mother
			if (birthMotherPID != null_RPID) {
				selectString = setSelectSQL("*", personTable,"PID = " + birthMotherPID);
				personSelected = requestTableData(selectString, dataBaseIndex);
				personSelected.first();
				femaleSex = getPersonFlagSetting(personSelected.getInt("BIRTH_SEX"), 1);
				fatherPID = personSelected.getLong(personFatherField);
				grandMotherFather = pointLibraryResultSet.exstractPersonName(fatherPID, personStyle, dataBaseIndex);
				motherPID = personSelected.getLong(personMotherField);
				grandMotherMother  = pointLibraryResultSet.exstractPersonName(motherPID, personStyle, dataBaseIndex);
				birthDatePID = personSelected.getLong(personHDateBirthField);
				deathDatePID = personSelected.getLong(personHdateDeathField);
				motherLifeSpan = exstractLifeSpan(birthDatePID,deathDatePID);
				partnerPID = findLastPartner(birthMotherPID);
				motherPartnerName = pointLibraryResultSet.exstractPersonName(partnerPID, personStyle, dataBaseIndex);

			//Grand	mother partners and life span
				if (fatherPID != null_RPID) {
					selectString = setSelectSQL("*", personTable, "PID = " + fatherPID);
					personSelected = requestTableData(selectString,dataBaseIndex);
					personSelected.first();
					birthDatePID = personSelected.getLong(personHDateBirthField);
					deathDatePID = personSelected.getLong(personHdateDeathField);
					maleSex = getPersonFlagSetting(personSelected.getInt("BIRTH_SEX"), 1);
					motherFatherLifeSpan = exstractLifeSpan(birthDatePID,deathDatePID);
					partnerPID = findLastPartner(fatherPID);
					motherFatherPartnerName = pointLibraryResultSet.exstractPersonName(partnerPID, personStyle, dataBaseIndex);
				}

				if (motherPID != null_RPID) {
					selectString = setSelectSQL("*", personTable, "PID = " + motherPID);
					personSelected = requestTableData(selectString, dataBaseIndex);
					personSelected.first();
					birthDatePID = personSelected.getLong(personHDateBirthField);
					deathDatePID = personSelected.getLong(personHdateDeathField);
					femaleSex = getPersonFlagSetting(personSelected.getInt("BIRTH_SEX"), 1);
					motherMotherLifeSpan = exstractLifeSpan(birthDatePID,deathDatePID);
					partnerPID = findLastPartner(motherPID);
					motherMotherPartnerName = pointLibraryResultSet.exstractPersonName(partnerPID, personStyle, dataBaseIndex);
				}
			}

			// Set up parent table
			int parentCode = 0;
			updateParentTable(personTablePID);
			if (HGlobal.DEBUG) {
				System.out.println(" Parent table parentCode: " + parentCode);
			}
			if (parentCode > 0)	{
				parentTable[0][1] = fatherName;
				if (fatherName.trim().equals("---")) {
					parentTable[0][1] = "  No father recorded";
					parentTable[0][2] = "  Not valid";
				}

				parentTable[1][1] = motherName;
				if (motherName.trim().equals("---")) {
					parentTable[1][1] = "  No mother recorded";
					parentTable[1][2] = "  Not valid";
				}
			}

		// Father parents
			relativesGP2Table[0][0] = grandFatherFather;
			relativesGP2Table[0][1] = " " + grandParents[0];
			relativesGP2Table[0][2] = " " + maleSex;
			relativesGP2Table[0][3] = fatherFatherLifeSpan;
			relativesGP2Table[0][4] = fatherFatherPartnerName;

			relativesGP2Table[1][0] = grandFatherMother;
			relativesGP2Table[1][1] = " " + grandParents[1];
			relativesGP2Table[1][2] = " " + femaleSex;
			relativesGP2Table[1][3] = fatherMotherLifeSpan;
			relativesGP2Table[1][4] = fatherMotherPartnerName;

		//Mother parents
			relativesGP2Table[2][0] = grandMotherFather;
			relativesGP2Table[2][1] = " " + grandParents[2];
			relativesGP2Table[2][2] = " " + maleSex;
			relativesGP2Table[2][3] = motherFatherLifeSpan;
			relativesGP2Table[2][4] = motherFatherPartnerName;

			relativesGP2Table[3][0] = grandMotherMother;
			relativesGP2Table[3][1] = " " + grandParents[3];
			relativesGP2Table[3][2] = " " + femaleSex;
			relativesGP2Table[3][3] = motherMotherLifeSpan;
			relativesGP2Table[3][4] = motherMotherPartnerName;

		//Parents
			relativesGP1Table[0][0] = fatherName;
			relativesGP1Table[0][1] = " " + parents[0];
			relativesGP1Table[0][2] = " " + maleSex;
			relativesGP1Table[0][3] = fatherLifeSpan;
			relativesGP1Table[0][4] = fatherPartnerName;

			relativesGP1Table[1][0] = motherName;
			relativesGP1Table[1][1] = " " + parents[1];
			relativesGP1Table[1][2] = " " + femaleSex;
			relativesGP1Table[1][3] = motherLifeSpan;
			relativesGP1Table[1][4] = motherPartnerName;

		// Self
			relativesGS0Table[0][0] = personName;
			relativesGS0Table[0][1] = "  " + person[0];
			relativesGS0Table[0][2] = selfSex ;
			relativesGS0Table[0][3] = personLifeSpan;

		// Children
			selectString = setSelectSQL("*", personTable,
					"SPERM_PROVIDER_RPID = " + personTablePID
					+ " OR " + "EGG_PROVIDER_RPID = " + personTablePID);

			childsSelected = requestTableData(selectString, dataBaseIndex);
			childsSelected.last();
			nrRows = childsSelected.getRow();
			relativesGC1Table = new Object[nrRows][5];
			grandChildList = new ArrayList<>();
			childsSelected.beforeFirst();
			childRow = 0;
			while (childsSelected.next()) {
				personPID = childsSelected.getLong("PID");
				relativesGC1Table[childRow][0] = pointLibraryResultSet.exstractPersonName(personPID, personStyle, dataBaseIndex);
				//relativesGC1Table[childRow][1] = " Child";

				relativesGC1Table[childRow][1] = " " + childType[childsSelected.getInt("BIRTH_SEX")];
				relativesGC1Table[childRow][2] = getPersonFlagSetting(childsSelected.getInt("BIRTH_SEX"), 1);
				birthDatePID = childsSelected.getLong(personHDateBirthField);
				deathDatePID = childsSelected.getLong(personHdateDeathField);
				relativesGC1Table[childRow][3] = exstractLifeSpan(birthDatePID,deathDatePID);
				partnerPID = findLastPartner(personPID);
				relativesGC1Table[childRow][4] = pointLibraryResultSet.exstractPersonName(partnerPID, personStyle, dataBaseIndex);
			 // Set up list with grand child's
			    exstractGrandChilds(personPID);
			    childRow++;
			}

	    // Grand Children
			relativesGC2Table = new String[grandChildList.size()][5];
			for (int i = 0; i < grandChildList.size(); i++) {
				String[] grandChilds = grandChildList.get(i);
				relativesGC2Table[i][0] = grandChilds[0];
				relativesGC2Table[i][1] = " " + grandChilds[1];
				relativesGC2Table[i][2] = grandChilds[2];
				relativesGC2Table[i][3] = grandChilds[3];
				relativesGC2Table[i][4] = grandChilds[4];
			}
			
		// Se up nametable	
			preparePersonNameTable(personTablePID);

		// set up all images
			int imageErrorCode = pointMediaHandler.getAllExhibitImage(personTablePID, personImage, dataBaseIndex);
			if (imageErrorCode > 1) {
				String personName = pointLibraryResultSet.exstractPersonName(personTablePID, personStyle, dataBaseIndex);
					System.out.println(" PersonVP Image error for name: "
				+ personName + " HRE PID: " + personTablePID);
			}

		// Set up sorted event list with age
			prepareEventTable(personTablePID);

		// Set up eventWitness list
			prepareAssociateTable(personTablePID);

		// Update flag table
			updatePersonFlagList(personTablePID);

		} catch (SQLException sqle) {
			throw new HBException(" updatePersonVPtable - SQL exception: " + sqle.getMessage()
				+ "\nSQL string: " + selectString);
		}
		return errorCode;
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
		try {
		// Find best name for selected person
			selectString = setSelectSQL("*", personTable, " PID = " + selectPersonPID);
			personTableRS = requestTableData(selectString, dataBaseIndex);
			personTableRS.first();
			
		// Set up table
			selectString = setSelectSQL("*", personNameTable, ownerRecordField + " = " + selectPersonPID);
			personNameTableRS = requestTableData(selectString, dataBaseIndex);
			personNameTableRS.last();
			nrRows = personNameTableRS.getRow();
			nameTable = new Object[nrRows][3];
			personNameTableRS.beforeFirst();
			int row = 0;
			while (personNameTableRS.next()) {
				if (dBbuild.startsWith("v22b")) {
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
				} else {
					System.out.println("HBPersonHandler selected DataBase not found - " + dBbuild);
				}
				long hdatePID = personNameTableRS.getLong("START_HDATE_RPID");
				nameTable[row][2] = pointLibraryResultSet.exstractDate(hdatePID, dataBaseIndex);
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
 */
	private int updateParentTable(long selectPersonPID) throws HBException, SQLException {
		int errorCode = 0;
		int tableSize, row;
		String 	parentName = "",
				parentRole = "",
				parentSurety = "   ?";
		int parentType;
		long parentPID;
		String langCode = HGlobal.dataLanguage;
		String birthSelect = setSelectSQL("*", personParentTable, "PERSON_RPID = " + selectPersonPID);
		ResultSet parentsSelected = requestTableData(birthSelect, dataBaseIndex);
		parentsSelected.last();
		tableSize = parentsSelected.getRow();
		if (tableSize <= 0) {
			return 1;
		}
		parentTable = new Object[tableSize][3];
		row = 0;
		parentsSelected.beforeFirst();
		while (parentsSelected.next()) {
			parentType = parentsSelected.getInt("PARENT_TYPE");
			if (HGlobal.DEBUG) {
				System.out.println(" Parent type: " + parentType);
			}
			parentSurety = parentsSelected.getString("SURETY");
			if (parentSurety.trim().length() == 0) {
				parentSurety = "   Not set";
			}
			parentPID = parentsSelected.getLong("PARENT_RPID");
			parentName =  pointLibraryResultSet.exstractPersonName(parentPID, personStyle, dataBaseIndex);
			String typeSelect = setSelectSQL("*", eventDefnTable, "EVNT_TYPE = " + parentType
					+ " AND LANG_CODE = '" + langCode + "'");
			ResultSet typesSelected = requestTableData(typeSelect, dataBaseIndex);
			if (typesSelected.last()) {
				typesSelected.first();
				parentRole = " " + typesSelected.getString("EVNT_NAME");
				parentTable[row][0] = " " + parentRole.trim();
			} else {
				if (parentType == 1079) {
					parentRole = " Father (en-US)";
				} else if (parentType == 1090) {
					parentRole = " Mother (en-US)";
				} else {
					parentRole = "  Not found  ";
				}
				parentTable[row][0] = parentRole.trim();
			}
			parentTable[row][1] = " " + parentName;
			parentTable[row][2] = "   " + parentSurety;
			row++;
			if (HGlobal.DEBUG) {
				System.out.println(" Parent Role: "
					+ parentRole + " Name: " + parentName + " Surety: " + parentSurety);
			}
		}
		return errorCode;
	}

/**
 *
 * @return
 * @throws HBException
 */
	protected long findLastPartner(long personPID) throws HBException {
		int index = 0;
		String  selectString = setSelectSQL("*", personPartnerTable,
				"PRI_PARTNER_RPID = " + personPID + " OR SEC_PARTNER_RPID = " + personPID);
		ResultSet partnerList = requestTableData(selectString, dataBaseIndex);
		long priPartner = null_RPID, secPartner = null_RPID;
		try {
			partnerList.last();
			nrRows = partnerList.getRow();
			//System.out.println(" Number of partners: " + nrRows);
			if (nrRows == 0) {
				return null_RPID;
			}
			Object[][] partners = new Object[nrRows][3];
			sortStrings = new String[nrRows];
			partnerList.beforeFirst();
			//System.out.println( " Last partn pers: " + personPID);
			while (partnerList.next()) {
					partners[index][0] = partnerList.getLong("PRI_PARTNER_RPID");
					partners[index][1] = partnerList.getLong("SEC_PARTNER_RPID");
					long sortHDatePID = partnerList.getLong("START_HDATE_RPID");
					int partnerType = partnerList.getInt("PARTNER_TYPE");
					partners[index][2] = pointLibraryResultSet.getEventName(partnerType,
							langCode, dataBaseIndex).trim();
					//System.out.println(" Partner Type; " + partners[index][2]);
					sortStrings[index] = pointLibraryResultSet.exstractSortString(sortHDatePID, dataBaseIndex);
					//System.out.println(" Sort: " + sortStrings[index]);
					//System.out.println( " Relations  prio: " + partners[index][0] + " seco: "
					//		+ partners[index][1]);
					index++;
			}
		// Create the sort indexes
			int [] rowsSorted = pointLibraryBusiness.sorter.sort(sortStrings);
			//for (int i = 0; i<rowsSorted.length; i++)
			//	System.out.print(" Sort: " + rowsSorted[i] + " / ");
			//System.out.println();
		// Now the sorted list of parner in decending sequence
			partnerTable = sortTable(partners, rowsSorted);
			priPartner = (long) partnerTable[nrRows-1][0];
			secPartner = (long) partnerTable[nrRows-1][1];
			if (personPID == priPartner) {
				return secPartner;
			}
			if (personPID == secPartner) {
				return priPartner;
			}
		} catch (SQLException sqle) {
			System.out.println( " HBViewPointHandler - findPartne: " + sqle.getMessage());
			throw new HBException(" HBViewPointHandler - findPartne: " + sqle.getMessage());
		}
		return null_RPID;
	}

/**
 * preparePartnerTable(long personPID)
 * @param personPID
 * @throws HBException
 */
	private void preparePartnerTable(long personPID) throws HBException {
		String selectString;
		ResultSet partnerSelected;
		long partnerPID = null_RPID, priPartner, secPartner;
		int nrOfPartners  = partnerTable.length;
		relativesGS0Table = new Object[nrOfPartners + 1][4];
		for (int i = 0; i < nrOfPartners; i++) {
			priPartner = (long) partnerTable[i][0];
			secPartner = (long) partnerTable[i][1];
			if (personPID == priPartner) {
				partnerPID = secPartner;
			}
			if (personPID == secPartner) {
				partnerPID = priPartner;
			}
			try {
				relativesGS0Table[i+1][0]  = pointLibraryResultSet.exstractPersonName(partnerPID, personStyle, dataBaseIndex);
				selectString = setSelectSQL("*", personTable,"PID = " + partnerPID);
				//System.out.println("Partner PID: " +  partnerPID);
				partnerSelected = requestTableData(selectString,dataBaseIndex);
				partnerSelected.first();
				long birthDatePID = partnerSelected.getLong(personHDateBirthField);
				long deathDatePID = partnerSelected.getLong(personHdateDeathField);
				relativesGS0Table[i+1][1] = " " + partnerTable[i][2];
				relativesGS0Table[i+1][2] = getPersonFlagSetting(partnerSelected.getInt("BIRTH_SEX"), 1);
				relativesGS0Table[i+1][3] = exstractLifeSpan(birthDatePID, deathDatePID);
			} catch (SQLException sqle) {
				System.out.println(" ViewPeopleData - preparePartnerTable: " + sqle.getMessage());
				throw new HBException(" ViewPeopleData - preparePartnerTable: " + sqle.getMessage());
			}
		}
	}

/**
 * prepareEventTable()
 * @return
 * @throws HBException
 */

	private void prepareEventTable() throws HBException {
		prepareEventTable(personTablePID);
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
		eventDuplicateList = new Vector<>(100,10);
		int rowCount = 0;

	// Add partner events
		rowCount = rowCount + addPartnersToEvents(selectPersonPID, eventList);
	// Add events to table
		rowCount = rowCount + addEventToEvents(selectPersonPID, eventList);
	// Include witness events
		if (includeWitnessEvents)
		 {
			rowCount = rowCount + addWitnesToEvents(selectPersonPID, eventList);
//	Include list of child's
			//if (includeCildrenEvents) rowCount = rowCount + addChildsToTable(selectPersonPID, eventList);
		}

		nrRows = rowCount;
	// Set number of events for GUI
		eventRows = rowCount;
		eventsData = new Object[nrRows][5];
		sortStrings = new String[nrRows];
		long[] eventsPIDs = new long[nrRows];

		for (int i = 0; i < nrRows; i++) {
			eventsData[i][0] = " " + eventList.get(i)[0];
			eventsData[i][1] = " " + eventList.get(i)[1];
			eventsData[i][2] = " " + eventList.get(i)[2];
			eventsData[i][3] = " " + eventList.get(i)[3];
			eventsData[i][4] = " " + eventList.get(i)[4];

			sortStrings[i] = "" + eventList.get(i)[5];
			eventsPIDs[i] = (long) eventList.get(i)[6];
		}
	// Sort the events according to HDATE SORT

		// Setup sort index
		int [] rowsSorted = pointLibraryBusiness.sorter.sort(sortStrings);

	// Sort the events according to HDATE SORT

		if (rowCount > 0)
		 {
			eventsTable = sortTable(eventsData, rowsSorted);
	// Test 2023.12.21
		//else eventsTable[0][5] = "";
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
 * addPartnersToEvents(long selectedPersonPID, ArrayList<String[]> eventList)
 * @pa ram selectedPersonPID
 * @param eventList
 * @return
 * @throws HBException
 */
	private int addPartnersToEvents(long selectedPersonPID, ArrayList<Object[]> eventList) throws HBException {
		String selectString = null, image;
		String langCode = HGlobal.dataLanguage;
		long locationName_RPID, eventHDatePID, bestImageRPID, sortHDatePID, selectedEvent;
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

			// No partner event recorded EVNT_RPID = null_RPID
				if (selectedEvent == null_RPID) {
					return index;
				}

				partnerType = personPartnerSelected.getInt("PARTNER_TYPE");
				if (selectedPersonPID == personPartnerSelected.getLong("PRI_PARTNER_RPID")) {
					eventRole = personPartnerSelected.getInt("PRI_ROLE");
				} else if (selectedPersonPID == personPartnerSelected.getLong("SEC_PARTNER_RPID")) {
					eventRole = personPartnerSelected.getInt("SEC_ROLE");
				} else {
					System.out.println(" Not found: " + selectedPersonPID);
				}
				selectString = setSelectSQL("*", eventTable, "PID = " + selectedEvent);
				partnerEventSelected = requestTableData(selectString, dataBaseIndex);
				int eventCount = 0;
			// Test for no partner event recorded
				partnerEventSelected.last();
				if (partnerEventSelected.getRow() == 0) {
					return index;
				}

				partnerEventSelected.beforeFirst();
				while (partnerEventSelected.next()) {
					events = new Object[7];
					eventDuplicateList.add(partnerEventSelected.getLong("PID"));
					eventNumber = partnerEventSelected.getInt("EVNT_TYPE");
					locationName_RPID = partnerEventSelected.getLong("EVNT_LOCN_RPID");
					bestImageRPID = partnerEventSelected.getLong("BEST_IMAGE_RPID");
					if (bestImageRPID != null_RPID) {
						image = "*";
					} else {
						image = "";
					}
					eventHDatePID = partnerEventSelected.getLong("START_HDATE_RPID");
					sortHDatePID = partnerEventSelected.getLong("SORT_HDATE_RPID");

					events[0] = pointLibraryResultSet.getEventName(partnerType,
													langCode, dataBaseIndex).trim() + image;
					events[1] = pointLibraryResultSet.getRoleName(eventRole,
							eventNumber, langCode, dataBaseIndex).trim();
					events[2] = pointLibraryResultSet.exstractDate(eventHDatePID,
													dataBaseIndex).trim();
					events[3] = pointLibraryResultSet.selectLocationName(locationName_RPID,
													locationNameStyle, dataBaseIndex).trim();
					events[4] = "" + pointLibraryResultSet.calculateAge(eventHDatePID,
													selectedPersonPID, dataBaseIndex);
					events[5] = pointLibraryResultSet.exstractSortString(sortHDatePID, dataBaseIndex);
				// record event PID table indexed by table position.
					events[6] = partnerEventSelected.getLong("PID");
					eventList.add(events);
					index++;
					eventCount++;

					if (HGlobal.DEBUG) {
						dumpEvents("Partner",events);
					}
				}
				if (eventCount > 1) {
					System.out.println(" Number of added partner events: " + eventCount);
				}
			}
			return index;
		} catch (SQLException sqle) {
			throw new HBException(" addPartnersToTable - SQL exception: " + sqle.getMessage()
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

	// Add the partner relations as assocs to the list
		try {
			selectString = setSelectSQL("*", personPartnerTable, "PRI_PARTNER_RPID = " + selectedPersonPID
								+ " OR SEC_PARTNER_RPID = " + selectedPersonPID);
			personPartnerSelected = requestTableData(selectString, dataBaseIndex);
			personPartnerSelected.beforeFirst();
			while (personPartnerSelected.next()) {

			// Find the PID for the event
				selectedEventPID = personPartnerSelected.getLong("EVNT_RPID");

			// No partner event recorded EVNT_RPID = null_RPID
				if (selectedEventPID == null_RPID) {
					return index;
				}

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
				int partnerCount = 0;

			// Test for no partner event recorded
				partnerEventSelected.last();
				if (partnerEventSelected.getRow() == 0) {
					return index;
				}

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
					Object[] asociate = new Object[5];
					asociate[0] = " " + personName.trim();
					asociate[1] = " " + eventRoleName.trim();
					asociate[2] = " " + eventName.trim();
					asociate[3] = " " + eventDate.trim();
					asociate[4] = partnerPersonPID;
					assocList.add(asociate);
					index++;

			// Add the assocs to the partner event to assoc list
					index = index + addPartnerAssocToAssoc(partnerPersonPID, selectedEventPID, eventName, eventNumber, eventDate);

					if (HGlobal.DEBUG) {
						dumpEvents("Associate partner", asociate);
					}
				}
				if (partnerCount > 1) {
					System.out.println(" Number of added partner events: " + partnerCount);
				}
			}
			return index;
		} catch (SQLException sqle) {
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
				//if (focusPersonPID == assocPersonPID) continue; // Self assoc detected
				assocRoleCode = eventAssocSelected.getInt("ROLE_NUM");
				eventRole = pointLibraryResultSet.getRoleName(assocRoleCode,
			  			eventType,
			  			langCode,
			  			dataBaseIndex);
				personName = pointLibraryResultSet.exstractPersonName(assocPersonPID, personStyle, dataBaseIndex);

				if (HGlobal.DEBUG) {
					System.out.println(" Associate nr: " + witness + " Event PID: " + eventTablePID + " Type: " + eventType
						+ " Event: " + eventName.trim() + " Name: " + personName + " Role: " + eventRole);
				}

				Object[] asociates = new Object[5];
				asociates[0] = " " + personName.trim();
				asociates[1] = " " + eventRole.trim();
				asociates[2] = " " + eventName.trim();
				asociates[3] = " " + eventDate.trim();
				asociates[4] = assocPersonPID;
				asociateList.add(asociates);
				witness++;
			}
			return witness;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" HBPersonHandler - addPartnerAssocToAssoc \nerror: " + sqle.getMessage());
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

				if (HGlobal.DEBUG) {
					System.out.println(" Key Associate nr: " + witness + " Event PID: " + eventTablePID + " Type: " + eventType
						+ " Event: " + eventName.trim() + " Name: " + personName + " Role: " + eventRoleName);
				}

				Object[] asociates = new Object[5];
				asociates[0] = " " + personName.trim();
				asociates[1] = " " + eventRoleName.trim();
				asociates[2] = " " + eventName.trim();
				asociates[3] = " " + eventDate.trim();
				asociates[4] = assocPersonPID;
				asociateList.add(asociates);
				witness++;
			}

			return witness;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" HBPersonHandler - addPartnerAssocToAssoc \nerror: " + sqle.getMessage());
		}
	}
/**
 * addEventToEvents(long selectPersonPID, ArrayList<Object[]> eventList)
 * @param selectPersonPID
 * @param eventList
 * @return
 * @throws HBException
 */
	private int addEventToEvents(long selectPersonPID, ArrayList<Object[]> eventList) throws HBException {
		String langCode = HGlobal.dataLanguage;
		String selectString = "", image = "";
		Object[] events = null;
		ResultSet eventSelected;
		int index = 0;
		long eventHDatePID, sortHDatePID, bestImageRPID, locationName_RPID = null_RPID;
		int eventRole = 0, eventNumber;
		try {
			locationName_RPID = null_RPID;
			selectString = setSelectSQL("*", eventTable, "PRIM_ASSOC_RPID = " + selectPersonPID);
			eventSelected = requestTableData(selectString, dataBaseIndex);
			eventSelected.beforeFirst();
			int eventCount = 0;
			while (eventSelected.next()) {
				long eventPID = eventSelected.getLong("PID");
				if (!eventDuplicateList.contains(eventPID)) {
					eventDuplicateList.add(eventPID);
					//System.out.println(" Event list: " + selectPersonPID + "/" + eventPID + "/" + eventRole);
					events = new Object[7];
					bestImageRPID = eventSelected.getLong("BEST_IMAGE_RPID");
					if (bestImageRPID != null_RPID) {
						image = "*";
					} else {
						image = "";
					}
					locationName_RPID = eventSelected.getLong("EVNT_LOCN_RPID");
					eventNumber = eventSelected.getInt("EVNT_TYPE");
					eventRole = eventSelected.getInt("PRIM_ASSOC_ROLE_NUM");
					eventHDatePID = eventSelected.getLong("START_HDATE_RPID");
					sortHDatePID = eventSelected.getLong("SORT_HDATE_RPID");
					events[0] = pointLibraryResultSet.getEventName(eventNumber,
											langCode, dataBaseIndex).trim() + image;
					events[1] = pointLibraryResultSet.getRoleName(eventRole,
											eventNumber, langCode, dataBaseIndex).trim();
					events[2] = pointLibraryResultSet.exstractDate(eventHDatePID, dataBaseIndex).trim();
					events[3] = pointLibraryResultSet.selectLocationName(locationName_RPID,
											locationNameStyle, dataBaseIndex).trim();
					events[4] = "" + pointLibraryResultSet.calculateAge(eventHDatePID,
											selectPersonPID, dataBaseIndex);
					events[5] = pointLibraryResultSet.exstractSortString(sortHDatePID, dataBaseIndex);
				// record event PID table indexed by table position.
					events[6] = eventSelected.getLong("PID");
					eventList.add(events);
					eventCount++;
					index++;
					if (HGlobal.DEBUG) {
						dumpEvents("Event",events);
					}
				}
			}

			if (HGlobal.DEBUG) {
				if (eventCount > 1) {
					System.out.println(" Number of added events: " + eventCount);
				}
			}
			return index;
		} catch (SQLException sqle) {
			throw new HBException(" addEventToTable - SQL exception: " + sqle.getMessage()
						+ "\nSQL string: " + selectString);
		}
	}

/**
 * addWitnesEventToEvents(long selectPersonPID, ArrayList<String[]> eventList)
 * @param selectPersonPID
 * @param eventList
 * @return
 * @throws HBException
 */
	private int addWitnesToEvents(long selectPersonPID, ArrayList<Object[]> eventList) throws HBException {
		String langCode = HGlobal.dataLanguage;
		String selectString = "", image;
		Object[] events;
		int index = 0;
		ResultSet eventAssocSelected, eventSelected;
		long eventHDatePID, sortHDatePID, eventPID, bestImageRPID, locationName_RPID = null_RPID;
		int eventRole, eventNumber;
		try {
			locationName_RPID = null_RPID;
			selectString = setSelectSQL("*", eventAssocTable, "ASSOC_RPID = " + selectPersonPID);
			eventAssocSelected = requestTableData(selectString, dataBaseIndex);
			eventAssocSelected.beforeFirst();
			while (eventAssocSelected.next()) {
				eventRole = eventAssocSelected.getInt("ROLE_NUM");
				eventPID = eventAssocSelected.getLong("EVNT_RPID");
				//System.out.println(" Witness list: " + selectPersonPID + "/" + eventPID + "/" + eventRole);
				selectString = setSelectSQL("*", eventTable, "PID = " + eventPID);
				eventSelected = requestTableData(selectString, dataBaseIndex);
				eventSelected.beforeFirst();
				int eventCount = 0;
				events = new String[7];
				while (eventSelected.next()) {
					if (!eventDuplicateList.contains(eventSelected.getLong("PID"))) {
						events = new Object[7];
						locationName_RPID = eventSelected.getLong("EVNT_LOCN_RPID");
						bestImageRPID = eventSelected.getLong("BEST_IMAGE_RPID");
						if (bestImageRPID != null_RPID) {
							image = "*";
						} else {
							image = "";
						}
						eventNumber = eventSelected.getInt("EVNT_TYPE");
						eventHDatePID = eventSelected.getLong("START_HDATE_RPID");
						sortHDatePID = eventSelected.getLong("SORT_HDATE_RPID");
						events[0] = pointLibraryResultSet.getEventName(eventNumber,
												langCode, dataBaseIndex).trim() + image;
						events[1] = pointLibraryResultSet.getRoleName(eventRole,
								eventNumber, langCode, dataBaseIndex).trim();
						events[2] = pointLibraryResultSet.exstractDate(eventHDatePID, dataBaseIndex).trim();

						events[3] = pointLibraryResultSet.selectLocationName(locationName_RPID,
												locationNameStyle, dataBaseIndex).trim();
						events[4] = "" + pointLibraryResultSet.calculateAge(eventHDatePID,
												selectPersonPID, dataBaseIndex);
						events[5] = pointLibraryResultSet.exstractSortString(sortHDatePID, dataBaseIndex);
						// record event PID table indexed by table position.
						events[6] = eventSelected.getLong("PID");
						eventCount++;
						index++;
						eventList.add(events);
						if (HGlobal.DEBUG) {
							dumpEvents("Witness", events);
						}
					}
				}
				if (eventCount > 1) {
					System.out.println("Multiple witness events :" + eventCount);
				}
			}
			return index;
		} catch (SQLException sqle) {
			throw new HBException(" addWitnesEventToTable - SQL exception: " + sqle.getMessage()
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
 * prepareAssociateTable(long selectPersonPID)
 * @param selectedPersonPID
 * @throws HBException
 */
	private void prepareAssociateTable(long selectedPersonPID) throws HBException {
		String selectString = null;
		ResultSet eventSelected, personAssociateSelected, eventAssocSelected;
		String langCode = HGlobal.dataLanguage;
		String personName = "", eventRole = "", eventName = "", eventDate = "--";
		int assocs = 0, eventNumber = 0, eventRoleCode, selfAssocCount = 0;
		long eventTablePID, eventPID, assocPersonPID;
		asociateList = new ArrayList<>();
		personPIDhash = new HashMap<>();

	// Add partners to Assocs
		if (HGlobal.DEBUG)
			System.out.println(" Imported project: " + pointOpenProject.getImportedProject());

		asociateRows = addPartnersToAssocs(selectedPersonPID, asociateList);
		//System.out.println(" addPartnersToAssocs - partner assocs found: " + asociateRows);
		asociateRows = asociateRows + addEventToAssociates(selectedPersonPID, asociateList); // Def
		//System.out.println(" addEventToAssociates- event assocs found: " + asociateRows);

		try {
			selectString = setSelectSQL("*", eventAssocTable, "ASSOC_RPID = " + selectedPersonPID);
			personAssociateSelected = requestTableData(selectString, dataBaseIndex);
			personAssociateSelected.beforeFirst();
			while (personAssociateSelected.next()) {

		// Get associate persons with the events in list
				eventTablePID = personAssociateSelected.getLong("EVNT_RPID");
		// Add key associate
				asociateRows = asociateRows + addKeyAssocToAssoc(selectedPersonPID, eventTablePID);
				selectString = setSelectSQL("*", eventAssocTable, "EVNT_RPID = " + eventTablePID);
				eventAssocSelected = requestTableData(selectString, dataBaseIndex);
				eventAssocSelected.beforeFirst();
				while (eventAssocSelected.next()) {
					eventPID = eventAssocSelected.getLong("EVNT_RPID");
					assocPersonPID = eventAssocSelected.getLong("ASSOC_RPID");
					eventRoleCode = eventAssocSelected.getInt("ROLE_NUM");

					if (assocPersonPID != selectedPersonPID) {
				// Get event data
						selectString = setSelectSQL("*", eventTable, "PID = " + eventPID);
						eventSelected = requestTableData(selectString, dataBaseIndex);
						//eventSelected.beforeFirst();
						eventSelected.first();
						long eventHDatePID = eventSelected.getLong("START_HDATE_RPID");
						eventDate = pointLibraryResultSet.exstractDate(eventHDatePID, dataBaseIndex);
						eventNumber = eventSelected.getInt("EVNT_TYPE");
						eventName = pointLibraryResultSet.getEventName(eventNumber, langCode, dataBaseIndex);
						eventRole = pointLibraryResultSet.getRoleName(eventRoleCode,
							  			eventNumber,
							  			langCode,
							  			dataBaseIndex);
						personName = pointLibraryResultSet.exstractPersonName(assocPersonPID, personStyle, dataBaseIndex);
					//System.out.println(" Associate: " + assocs + " Event PID: " + eventPID + " Type: " + eventNumber
					//					+ " Event: " + eventName.trim() + " Name: " + personName + " Role: " + eventRole);
						Object[] asociate = new Object[5];
						asociate[0] = " " + personName.trim();
						asociate[1] = " " + eventRole.trim();
						asociate[2] = " " + eventName.trim();
						asociate[3] = " " + eventDate.trim();
						asociate[4] = assocPersonPID;
						asociateList.add(asociate);
						assocs++;

					} else {
						selfAssocCount++;
						if (HGlobal.DEBUG) {
							System.out.println(" Self assoc: " + selfAssocCount
														  + " Event PID: " + eventPID
														  + " RoleCode: " + eventRoleCode
														  + " Assoc per: " + assocPersonPID);
						}
					}
				}
			}
			if (HGlobal.DEBUG) {
				if (selfAssocCount > 0) {
					System.out.println(" Number of self assocs: " + selfAssocCount);
				}
			}

		// Set number of row in asscociate table
			asociateRows = asociateRows + assocs;
			associateTable = new Object[asociateRows][4];

		// Set up sort and list PID
			long [] personPID = new long[asociateRows];
			for (int i = 0; i < asociateRows; i++) {
				associateTable[i][0] = asociateList.get(i)[0];
				associateTable[i][1] = asociateList.get(i)[1];
				associateTable[i][2] = asociateList.get(i)[2];
				associateTable[i][3] = asociateList.get(i)[3];
				personPID[i] = (long) asociateList.get(i)[4];
			}

		// Setup the HashMap for row to personPID
			for (int row = 0; row < personPID.length; row++) {
				personPIDhash.put(row, personPID[row]);
			}

		} catch (SQLException sqle) {
			throw new HBException(" prepareAssociateTable - SQL exception: " + sqle.getMessage()
						+ "\nSQL string: " + selectString);
		}
	}

/**
 * addEventToAssociates(long selectedPersonPID, ArrayList<Object[]> assocList)
 * @param selectedPersonPID
 * @throws HBException
 */
	private int addEventToAssociates(long selectedPersonPID, ArrayList<Object[]> asociateList) throws HBException {
		String selectString = null;
		ResultSet personEventSelected, eventAssocSelected;
		String langCode = HGlobal.dataLanguage;
		String personName = "", eventRole = "", eventName = "", eventDate = "--";
		int witness = 0, eventType = 0, eventRoleCode, eventGroup;
		long eventTablePID, eventHDatePID, assocPersonPID;
		if (HGlobal.DEBUG) {
			System.out.println(" Imported project: " + pointOpenProject.getImportedProject());
		}
		try {

		// Get person events from T450
			selectString = setSelectSQL("*", eventTable, "PRIM_ASSOC_RPID = " + selectedPersonPID);
			personEventSelected = requestTableData(selectString, dataBaseIndex);
			personEventSelected.beforeFirst();
			while (personEventSelected.next()) {
				//****************
				//if (personEventSelected.getBoolean("IMP_TMG")) continue; // If imported from TMG
				//***********************'
				eventTablePID = personEventSelected.getLong("PID");
				eventHDatePID = personEventSelected.getLong("START_HDATE_RPID");
				eventDate = pointLibraryResultSet.exstractDate(eventHDatePID, dataBaseIndex);
				eventType = personEventSelected.getInt("EVNT_TYPE");
				eventGroup = pointLibraryResultSet.getEventGroup(eventType, dataBaseIndex);
				if (eventGroup == marrGroup || eventGroup == divorceGroup) {
					continue;
				}
				eventName = pointLibraryResultSet.getEventName(eventType, langCode, dataBaseIndex);

			// Get associate persons with the events in list
				selectString = setSelectSQL("*", eventAssocTable, "EVNT_RPID = " + eventTablePID);
				eventAssocSelected = requestTableData(selectString, dataBaseIndex);
				eventAssocSelected.beforeFirst();
				while (eventAssocSelected.next()) {
					assocPersonPID = eventAssocSelected.getLong("ASSOC_RPID");
					eventRoleCode = eventAssocSelected.getInt("ROLE_NUM");
					eventRole = pointLibraryResultSet.getRoleName(eventRoleCode,
				  			eventType,
				  			langCode,
				  			dataBaseIndex);
					personName = pointLibraryResultSet.exstractPersonName(assocPersonPID, personStyle, dataBaseIndex);

					if (HGlobal.DEBUG)
					 {
						System.out.println(" Person VP - Associate: " + witness + " Event PID: " + eventTablePID + " Type: " + eventType
							+ " Event: " + eventName.trim() + " Name: " + personName + " Role: " + eventRole);
				// The focus person can also be a witness to another event
					}

					Object[] asociates = new Object[5];
					asociates[0] = " " + personName.trim();
					asociates[1] = " " + eventRole.trim();
					asociates[2] = " " + eventName.trim();
					asociates[3] = " " + eventDate.trim();
					asociates[4] = assocPersonPID;
					asociateList.add(asociates);
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
 * exstractLifeSpan(long hdatePID)
 * @param hdatePID
 * @return
 * @throws HBException
 */
	private String exstractLifeSpan(long birthPID, long deathPID) throws HBException {
		String lifSpanString, selectString = null;
		ResultSet hdateSelected;
		try {
		// Birth Year
			if (birthPID != null_RPID ) {
				selectString = setSelectSQL("*", dateTable, "PID = " + birthPID);
				hdateSelected = requestTableData(selectString, dataBaseIndex);
				hdateSelected.first();
				if (hdateSelected.getString("MAIN_HDATE_DETAILS").startsWith("I")) {
					lifSpanString = "<?>";
				} else {
					lifSpanString = hdateSelected.getString("MAIN_HDATE_YEARS");
				}
			} else {
				lifSpanString = "";
			}

		// Death Year
			if (deathPID != null_RPID ) {
				selectString = setSelectSQL("*", dateTable, "PID = " + deathPID);
				hdateSelected = requestTableData(selectString, dataBaseIndex);
				hdateSelected.first();
				if	(hdateSelected.getString("MAIN_HDATE_DETAILS").startsWith("I")) {
					lifSpanString = lifSpanString + " - " + "<?>";
				} else {
					lifSpanString = lifSpanString + " - "
							+ hdateSelected.getString("MAIN_HDATE_YEARS");
				}
			} else {
				lifSpanString = lifSpanString + " - ";
			}
			return lifSpanString;
		} catch (SQLException sqle) {
			throw new HBException(" exstractLifeSpan - SQL exception: " + sqle.getMessage() + "\nSQL string: " + selectString);
		}
	}


/**
 * Exstract grandchildren
 * @param personPID
 * @throws HBException
 */
	private void exstractGrandChilds(long personPID) throws HBException {
		ResultSet grandsSelected;
		String selectString;
		long partnerPID, selectedPID, birthDatePID, deathDatePID;
		String[] grandChild;
		selectString = setSelectSQL("*", personTable,
									"SPERM_PROVIDER_RPID = " + personPID
									+ " OR " + "EGG_PROVIDER_RPID = " + personPID);

		grandsSelected = requestTableData(selectString, dataBaseIndex);
		try {
			grandsSelected.beforeFirst();
			while (grandsSelected.next()) {
				grandChild = new String[5];
				selectedPID = grandsSelected.getLong("PID");
				grandChild[0] = pointLibraryResultSet.exstractPersonName(selectedPID, personStyle, dataBaseIndex);
				birthDatePID = grandsSelected.getLong(personHDateBirthField);
				deathDatePID = grandsSelected.getLong(personHdateDeathField);
				grandChild[1] = " " + grandChildType[grandsSelected.getInt("BIRTH_SEX")];
				grandChild[2] = getPersonFlagSetting(grandsSelected.getInt("BIRTH_SEX"), 1);
				grandChild[3] = exstractLifeSpan(birthDatePID,deathDatePID);
				partnerPID = findLastPartner(selectedPID);
				grandChild[4] = pointLibraryResultSet.exstractPersonName(partnerPID, personStyle, dataBaseIndex);
				grandChildList.add(grandChild);
			}
		} catch (SQLException sqle) {
			throw new HBException(" exstractGrandChilds - SQL exception: " + sqle.getMessage() + " SQL string: " + selectString);
		}
	}

/**
 * String getPersonFlagSetting(int flagvalueSetting, int flagIdent)
 * @param flagvalueSetting
 * @param flagIdent
 * @return
 * @throws HBException
 */
	protected String getPersonFlagSetting(int flagvalueSetting, int flagIdent) throws HBException {
		String languageCode = HGlobal.dataLanguage, flagValues;
		String[] values;
		String selectString = setSelectSQL("*", translatedFlag, "FLAG_IDENT = " + flagIdent
				+ " AND LANG_CODE = '" + languageCode + "'");
		ResultSet flagDefinition = requestTableData(selectString, dataBaseIndex);
		try {
			flagDefinition.last();
			//System.out.print( "Flag defni rows: " + flagDefinition.getRow() + " Lang: " + languageCode);
			flagDefinition.first();
			flagValues = flagDefinition.getString("FLAG_VALUES");
			values = flagValues.split(",");
			//System.out.println(" Values: " + flagValues + " Setting: " + flagvalueSetting
			//		+ " Flag return: " + values[flagvalueSetting]);
			return values[flagvalueSetting];
		} catch (SQLException sqle) {
			System.out.println(" HBViewPointHandler - getPersonFlagSetting: " + sqle.getMessage());
			sqle.printStackTrace();
		}
		return "";
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
	public void updatePersonFlagList(long selectedPersonPID) throws HBException {
		int flagRows = 0, index = 0, flagIdent;
		String langCode = HGlobal.dataLanguage; //"en-GB";
		ResultSet personFlagDefinition, personFlagTranslation;
		String selectString;
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
		String[] flagValueOptions = flagValues.split(",");
		try {
			if (flagIdent < 8) {
				selectSQL = setSelectSQL("*", personTable, "PID = " + selectedPersonPID);
				ResultSet personData = requestTableData(selectSQL, dataBaseIndex);
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
				ResultSet flagData = requestTableData(selectSQL, dataBaseIndex);
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


} // End Class

/**
 * class ViewLocationData
 * @author NTo
 * @version v0.00.0025
 * @since 2020-11-16
 */
class ViewLocationData extends HBBusinessLayer {

	protected HBMediaHandler pointMediaHandler = null;
	long proOffset = 1000000000000000L;
	long null_RPID  = 1999999999999999L;
	String locationName = " Location";
	int dataBaseIndex = 0;
	int eventRows = 0, associateRows = 0;
	String placeName;
	boolean includeWitnessEvents = true;

	// Type of exhibit
	int locationImage = 2;

	long locationTablePID;
	String screenID;

	Object [][] personTable;
	Object [][] eventsTable;

// HashMap index for eventPID's
	HashMap<Integer, Long> eventPIDs;
	HashMap<Integer, Long> personPIDs;

	ArrayList<Object[]> eventList;
	HashMap<Long, Object[]> usedEventPID;

// String array for HDATE sort string
	String[] sortStrings;
	String[] sortPIDs;

// Setting of style for summary events
	private String[] locationNameStyle;
	private String[] personNameStyle; // = {"1100","5000","3000","5200","5700"};

	ArrayList<ImageIcon> listOfImages;
	JLabel pictLabel;
	ImageIcon image;

/**
 * Constructor	ViewLocationData(
 * @param pointDBlayer
 * @param dataBaseIndex
 * @param hdataLabels
 */
	public ViewLocationData(HDDatabaseLayer pointDBlayer, int dataBaseIndex, HBProjectOpenData pointOpenProject) {
		super();
		this.pointDBlayer = pointDBlayer;
		this.dataBaseIndex = dataBaseIndex;
		dateFormatSelect();
		pointMediaHandler = pointOpenProject.getMediaHandler();
	}

	public void setWinessState(boolean state) throws HBException {
        if (HGlobal.DEBUG) {
			if (state) {
				System.out.println(" Witness ON");
			} else {
				System.out.println(" Witness OFF");
			}
		}
        includeWitnessEvents = state;
     // Create new event table
        resetEventTable();
	}

	public long getLocationPID() {
		return locationTablePID;
	}

	public String getScreenID() {
		return screenID;
	}

/**
 * ArrayList<ImageIcon> getImageList()
 * @return
 */
	public ArrayList<ImageIcon> getImageList() {
		return listOfImages;
	}

/**
 * int getNumberOfImages()
 * @return
 */
	public int getNumberOfImages() {
		if (listOfImages == null) {
			return 0;
		}
		return listOfImages.size();
	}

/**
 * Return data for tables
 * @return
 */
	public String getPlaceName() {
		return placeName;
	}

	public int getNumberEvents() {
		return eventRows;
	}

	public int getNumberPersons() {
		return associateRows;
	}

	public Object[][] getPersonTable() {
		return personTable;
	}

	public Object[][] getEventTable() {
		return eventsTable;
	}

/**
 * Collect PID's from person and events
 * @param tableRow
 * @return
 */
	public long getEventPID(int tableRow) {
		return eventPIDs.get(tableRow);
	}

	public long getPersonPID(int tableRow) {
		return personPIDs.get(tableRow);
	}

/**
 * DDL V22a VP location processing
 * updateLocationVPtable_v22(long locationTablePID, String screenID)
 * @param locationTablePID
 * @param screenID
 * @throws HBException
 */
	public int updateLocationVPtable(long locationTablePID, String screenID) throws HBException {
		int errorCode = 0;
		this.locationTablePID = locationTablePID;
		this.screenID = screenID;
		String selectString = null;
		long bestNameRPID, nameStyleRPID;
		ResultSet locationNameSelected, locationSelected;
	// Initiate index HashMap for eventPID's
		eventPIDs = new HashMap<>();

	// Initiate language translation for date
		setUpDateTranslation();
		dateFormatSelect();
		personNameStyle =  getNameStyleOutputCodes(nameStylesOutput, "N", dataBaseIndex);

		try {
			if (locationTablePID != null_RPID) {

		// Select location BEST_NAME_RPID from T551
			    selectString = setSelectSQL("*", locationTable, "PID = " + locationTablePID);
			    locationSelected =  requestTableData(selectString, dataBaseIndex);
			    locationSelected.first();
				bestNameRPID = locationSelected.getLong("BEST_NAME_RPID");

		// Select location NAME_STYLE_RPID from T552
				selectString = setSelectSQL("*", locationNameTable, "PID = " + bestNameRPID);
				locationNameSelected =  requestTableData(selectString, dataBaseIndex);
				locationNameSelected.first();
				nameStyleRPID = locationNameSelected.getLong("NAME_STYLE_RPID");
				locationNameSelected.close();
				locationSelected.close();

		// Select location style
				locationNameStyle =  getNameStyleOutputCodes(nameStylesOutput,
										nameStyleRPID, "P", dataBaseIndex);
				placeName = pointLibraryResultSet
						.selectLocationName(locationTablePID, locationNameStyle, dataBaseIndex);

    		} else {
				System.out.println("HBViewPointHandler - initiateHRElocation21a"
											+ " - locationNamePID == null: " + locationTablePID);
			}

		// Start preparing event table
			eventList = new ArrayList<>();
			usedEventPID = new HashMap<>();

		// Include withnessed events
			if(includeWitnessEvents) {
				eventRows = prepareEventWithTable(locationTablePID);
			} else {
				eventRows = 0;
			}

		// Set add location event table
			prepareEventTable(locationTablePID);

		// set up associate list
			prepareAssocTable(locationTablePID);

		// set up pictures for location
			errorCode = pointMediaHandler.getAllExhibitImage(locationTablePID, locationImage, dataBaseIndex);

			if (errorCode > 1) {
				System.out.println(" ViewLocationData - exhibittable, Image error PID: " + locationTablePID);
			}

		} catch (SQLException sqle) {
			throw new HBException(" ViewLocationData - updateLocationVPtable - SQL exception: " + sqle.getMessage()
				+ "\nSQL string: " + selectString);
		}
		return errorCode;
	}

/**
 * prepareEventTable()
 * @throws HBException
 */
	private void resetEventTable() throws HBException {
		eventList = new ArrayList<>();
		usedEventPID = new HashMap<>();

	// Include withnessed events
		if(includeWitnessEvents) {
			eventRows = prepareEventWithTable(locationTablePID);
		} else {
			eventRows = 0;
		}

	// Set up event table
		prepareEventTable(locationTablePID);
	}

/**
 * prepareEventTable(long locationTablePID)
 * @param locationTablePID
 * @throws HBException
 */
	private int prepareEventWithTable(long locationTablePID) throws HBException {
		String selectString;
		int row = 0;
		ResultSet eventsSelected;
		String langCode = HGlobal.dataLanguage;
			selectString = setSelectSQL("*", eventTable, "EVNT_LOCN_RPID = "
					+ locationTablePID);
	// Define object arrays
		Object[] events;
		try {
			eventsSelected = requestTableData(selectString, dataBaseIndex);
			eventsSelected.last();
			if (eventsSelected.getRow() > 0) {
				if (HGlobal.DEBUG) {
					System.out.println("Location VP - Selected Place:  "
						+ placeName + " / Event rows: " + eventRows);
				}

				String imageMark = "";
				eventsSelected.beforeFirst();
				row = 0;
				while (eventsSelected.next()) {
					if (findLocationWithImage()) {
						imageMark = " *";
					} else {
						imageMark = "";
					}
					int eventNumber = eventsSelected.getInt("EVNT_TYPE");

				// Get first associate person
					selectString = setSelectSQL("*", eventAssocTable, "EVNT_RPID = "
												+ eventsSelected.getLong("PID"));
					ResultSet eventAssocPerson = requestTableData(selectString, dataBaseIndex);
					eventAssocPerson.beforeFirst();
					while (eventAssocPerson.next()) {
						if (eventAssocPerson.getBoolean("KEY_ASSOC")) {
							events = new Object[5];
						// Populate table
							events[0] = " " + pointLibraryResultSet
												.getEventName(eventNumber, langCode, dataBaseIndex).trim() + imageMark ;
							events[1] = " " + pointLibraryResultSet
												.exstractDate(eventsSelected.getLong("START_HDATE_RPID"), dataBaseIndex).trim();
							events[2] = " " + pointLibraryResultSet
												.exstractPersonName(eventAssocPerson.getLong("ASSOC_RPID"), personNameStyle, dataBaseIndex).trim();
							events[3] = pointLibraryResultSet.exstractSortString(eventsSelected.getLong("SORT_HDATE_RPID"), dataBaseIndex);
						// record event PID table indexed by table position.
							events[4] = eventsSelected.getLong("PID");
							eventList.add(events);
							usedEventPID.put(eventsSelected.getLong("PID"), events);
							row++;
						}
					}
				}

				if (HGlobal.DEBUG) {
					System.out.println(" Witness events: " + row);
				}
			} else
				if (HGlobal.DEBUG) {
					System.out.println(" Found no events for locationTablePID: " + locationTablePID);
				}
			return row;
		} catch (SQLException sqle) {
			throw new HBException(" ViewLocationData - updateLocationVPtable - SQL exception: " + sqle.getMessage()
				+ "\nSQL string: " + selectString);
		}
	}

/**
 * prepareEventTable(long locationTablePID)
 * @param locationTablePID
 * @throws HBException
 */
	private void prepareEventTable(long locationTablePID) throws HBException {
		String selectString;
		int nrRows;
		ResultSet eventsSelected;
		String langCode = HGlobal.dataLanguage;
			selectString = setSelectSQL("*", eventTable, "EVNT_LOCN_RPID = "
					+ locationTablePID);
	// Define object arrays
		Object[][] eventsData;
		Object[] events;
		try {
			eventsSelected = requestTableData(selectString, dataBaseIndex);
			eventsSelected.last();
			if (eventsSelected.getRow() > 0) {
				if (HGlobal.DEBUG) {
					System.out.println("Location VP - Selected Place:  "
						+ placeName + " / Event rows: " + eventRows);
				}

				String imageMark = "";
				eventsSelected.beforeFirst();
				int row = 0;
				while (eventsSelected.next()) {
					if (findLocationWithImage()) {
						imageMark = " *";
					} else {
						imageMark = "";
					}
					if (!usedEventPID.containsKey(eventsSelected.getLong("PID"))) {
						int eventNumber = eventsSelected.getInt("EVNT_TYPE");
						events = new Object[5];
					// Populate table
						events[0] = " " + pointLibraryResultSet
											.getEventName(eventNumber, langCode, dataBaseIndex).trim() + imageMark ;
						events[1] = " " + pointLibraryResultSet
											.exstractDate(eventsSelected.getLong("START_HDATE_RPID"), dataBaseIndex).trim();
						events[2] = " " + pointLibraryResultSet
											.exstractPersonName(eventsSelected.getLong("PRIM_ASSOC_RPID"), personNameStyle, dataBaseIndex).trim();
						events[3] = pointLibraryResultSet.exstractSortString(eventsSelected.getLong("SORT_HDATE_RPID"), dataBaseIndex);
					// record event PID table indexed by table position.
						events[4] = eventsSelected.getLong("PID");
						eventList.add(events);
						row++;
					} else
						if (HGlobal.DEBUG) {
							System.out.println(" DUPLICATE: " + eventsSelected.getInt("EVNT_TYPE"));
						}
				}

				if (HGlobal.DEBUG) {
					System.out.println(" Standard events: " + row);
				}
				eventRows = eventRows + row;
				nrRows = eventRows;
				eventsData = new Object[nrRows][5];
				sortStrings = new String[nrRows];
				long [] eventsPID = new long[nrRows];

				for (int i = 0; i < nrRows; i++) {
					eventsData[i][0] = " " + eventList.get(i)[0];
					eventsData[i][1] = " " + eventList.get(i)[1];
					eventsData[i][2] = " " + eventList.get(i)[2];
					sortStrings[i] = "" + eventList.get(i)[3];
					eventsPID[i] = (long) eventList.get(i)[4];
					//System.out.println(i + " " + eventList.get(i)[0] + " / " + nrRows);
				}

			// Setup sort index
				int [] rowsSorted = pointLibraryBusiness.sorter.sort(sortStrings);

			// Sort the events according to HDATE SORT
				if (rowsSorted != null) {
					eventsTable = sortTable(eventsData, rowsSorted);
				}

			// Setup the HashMap for row to eventPID
				for (int i = 0; i < eventRows; i++) {
					eventPIDs.put(i, eventsPID[rowsSorted[i]]);
				}

			} else if (HGlobal.DEBUG) {
				System.out.println(" Found no events for locationTablePID: " + locationTablePID);
			}

		} catch (SQLException sqle) {
			throw new HBException(" ViewLocationData - updateLocationVPtable - SQL exception: " + sqle.getMessage()
				+ "\nSQL string: " + selectString);
		}
	}

/**
 * findLocationWithImage()
 * @return
 */
	private boolean findLocationWithImage() {
		return false;
	}

/**
 * void personList(long locationTablePID)
 * Set data for person table
 * @param selectPersonPID
 * @throws HBException
 */
	private void prepareAssocTable(long locationTablePID) throws HBException {

		String selectString = null;
		ResultSet eventSelected, eventAssciateSeleted;
		String langCode = HGlobal.dataLanguage;
		ArrayList<Object[]> associateList = new ArrayList<>();
		String personName = "", eventRole = "", eventName = "", eventDate = "--";
		int eventRoleCode, eventType, associates = 0;
		long eventPID, eventHDatePID, personAssocPID;
		personPIDs = new HashMap<>();
		try {
			selectString = setSelectSQL("*", eventTable, "EVNT_LOCN_RPID = " + locationTablePID);
			eventSelected = requestTableData(selectString, dataBaseIndex);
			eventSelected.last();
			eventSelected.beforeFirst();
			while (eventSelected.next()) {
				eventPID = eventSelected.getLong("PID");
			// Add key assocs
				associateRows = associateRows + addKeyAssocToAssoc(eventPID, associateList);

				eventType = eventSelected.getInt("EVNT_TYPE");
				eventName = " " + pointLibraryResultSet.getEventName(eventType, langCode, dataBaseIndex);
				eventHDatePID = eventSelected.getLong("START_HDATE_RPID");
				eventDate = pointLibraryResultSet.exstractDate(eventHDatePID,dataBaseIndex);

				selectString = setSelectSQL("*", eventAssocTable, "EVNT_RPID = " + eventPID);
				eventAssciateSeleted = requestTableData(selectString, dataBaseIndex);
				eventAssciateSeleted.beforeFirst();
				while (eventAssciateSeleted.next()) {
					personAssocPID = eventAssciateSeleted.getLong("ASSOC_RPID");
					eventRoleCode = eventAssciateSeleted.getInt("ROLE_NUM");
					eventRole = " " + pointLibraryResultSet.getRoleName(eventRoleCode,
							  eventType,
							  langCode,
							  dataBaseIndex);
					personName = pointLibraryResultSet.exstractPersonName(personAssocPID, personNameStyle, dataBaseIndex);

					Object[] associate = new Object[5];
					associate[0] = personName.trim();
					associate[1] = eventRole.trim();
					associate[2] = eventName.trim();
					associate[3] = eventDate;
					associate[4] = personAssocPID;
					associateList.add(associate);
					associates++;
				}
			}

	// Set number of row in person table
			associateRows = associateRows + associates;

	// Set up sort and list PID
			long [] personPID = new long[associateRows];

			personTable = new Object[associateRows][4];
			for (int i = 0; i < associateRows; i++) {
				personTable[i][0] =  " " + associateList.get(i)[0];
				personTable[i][1] =  " " + associateList.get(i)[1];
				personTable[i][2] =  " " + associateList.get(i)[2];
				personTable[i][3] = associateList.get(i)[3];
				personPID[i] = (long) associateList.get(i)[4];
			}

	  // Setup the HashMap for row to personPID
			for (int i = 0; i < personPID.length; i++) {
				personPIDs.put(i,personPID[i]);
			}

		} catch (SQLException sqle) {
			throw new HBException(" prepareAssocTable - SQL exception: " + sqle.getMessage()
						+ "\nSQL string: " + selectString);
		}
	}

/**
 * addKeyAssocToAssoc(long eventTablePID)
 * @param eventTablePID
 * @return
 * @throws HBException
 */
	private int addKeyAssocToAssoc(long eventTablePID, ArrayList<Object[]> associateList) throws HBException {
		String selectString = null;
		ResultSet keyAssocSelected, partnerRelationRS;
		String langCode = HGlobal.dataLanguage;
		String personName = "", eventRoleName = "", eventDate = "", eventName = "";
		int witness = 0, assocRoleCode, eventType, eventGroup, priRole = 0, secRole = 0, keyAssocs = 1;
		long  assocPersonPID, eventHDatePID, priPartnerPID = null_RPID, secPartnerPID = null_RPID;
		selectString = setSelectSQL("*", eventTable, "PID = " + eventTablePID);
		try {
			keyAssocSelected = requestTableData(selectString, dataBaseIndex);
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
				if (isResultSetEmpty(partnerRelationRS)) return witness;
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
				eventRoleName = pointLibraryResultSet.getRoleName(assocRoleCode,
			  			eventType,
			  			langCode,
			  			dataBaseIndex);
				personName = pointLibraryResultSet.exstractPersonName(assocPersonPID, personNameStyle, dataBaseIndex);

				if (HGlobal.DEBUG) {
					System.out.println(" Key Associate nr: " + witness + " Event PID: " + eventTablePID + " Type: " + eventType
						+ " Event: " + eventName.trim() + " Name: " + personName + " Role: " + eventRoleName);
				}

				Object[] associates = new Object[5];
				associates[0] = " " + personName.trim();
				associates[1] = " " + eventRoleName.trim();
				associates[2] = " " + eventName.trim();
				associates[3] = " " + eventDate.trim();
				associates[4] = assocPersonPID;
				associateList.add(associates);
				witness++;
			}

			return witness;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" HBPersonHandler - addPartnerAssocToAssoc \nerror: " + sqle.getMessage());
		}
	}


} // End Class ViewLocationData

/**
 * class ViewEventData
 * @author NTo
 * @version v0.00.0025
 * @since 2020-11-16
 */
class ViewEventData extends HBBusinessLayer {

    long proOffset = 1000000000000000L;
    long null_RPID  = 1999999999999999L;
	int dataBaseIndex;
	protected HBMediaHandler pointMediaHandler = null;
	HBProjectOpenData pointOpenProject;

	long eventTablePID;
	String screenID;
	// Show witness in event list
	boolean includeWitnessEvents = true;
	//boolean includeWitnessEvents = false;

// Name styles for location ans persons
	private String[] personNameStyle; // = {"1100","5000","3000","5200","5700"};
	private String[] locationNameStyle;

	Object [][] associateTable = {
			{null," Witness", null, null},
			{null," Best Man", null, null},
			{null," Witness", null, null},
			{null," Witness", null, null}};

	String eventInfo = "",
			eventPlace = "",
			eventDate = "--";

	String langCode = HGlobal.dataLanguage;

	// Type of exhibit
	int eventImage = 6;

	int eventNumber;
	int numberOfAssociates = 0;
	ArrayList<Object[]> personList = new ArrayList<>();
	ArrayList<ImageIcon> listOfImages;
	HashMap<Integer,Long> personPIDs;
	JLabel pictLabel;
	ImageIcon image;

	public ViewEventData(HDDatabaseLayer pointDBlayer, int dataBaseIndex, HBProjectOpenData pointOpenProject) {
		super();
		this.dataBaseIndex = dataBaseIndex;
		this.pointDBlayer = pointDBlayer;
		this.pointOpenProject = pointOpenProject;
		dateFormatSelect();
		pointMediaHandler = pointOpenProject.getMediaHandler();
	}

	public long getEventPID() {
		return eventTablePID;
	}

	public String getScreenID() {
		return screenID;
	}

/**
 * get date for HG0530ViewEvent
 * @return value
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
        prepareAssociateTable();
	}

	public Object[][] getAssociateTable() {
		return associateTable;
	}

	public String getIdentInfo() {
		return eventInfo;
	}

	public String getDateInfo() {
		return eventDate;
	}

	public String getPlaceInfo() {
		return eventPlace;
	}

	public int getNumberOfAsociates() {
		return numberOfAssociates;
	}

	public long getPersonPID(int tableRow) {
		return personPIDs.get(tableRow);
	}



/**
 * updateEventVPtable DDLv21c
 * @param locationTablePID
 * @throws HBException
 */
	public void updateEventVPtable(long eventTablePID, String screenID) throws HBException {
		this.eventTablePID = eventTablePID;
		this.screenID = screenID;
		int errorCode, eventGroup;
		String selectString, personName, eventName;
		ResultSet eventSelected, partnerTableRS;
		long personTablePID = 0, locationNameRPID, eventHDatePID, priPartnerPID, secPartnerPID;

	// Initiate language translation for date
		setUpDateTranslation();
		dateFormatSelect();
		personNameStyle =  getNameStyleOutputCodes(nameStylesOutput, "N", dataBaseIndex);
		locationNameStyle =  getNameStyleOutputCodes(nameStylesOutput, "P", dataBaseIndex);
		try {
		// Find the event data
			selectString = setSelectSQL("*", eventTable, "PID = " + eventTablePID);
			eventSelected = requestTableData(selectString, dataBaseIndex);
			eventSelected.first();
			eventNumber = eventSelected.getInt("EVNT_TYPE");

		// if partner event process partner person and roles
			eventGroup = pointLibraryResultSet.getEventGroup(eventNumber, dataBaseIndex);
			if (eventGroup == marrGroup || eventGroup == divorceGroup) {
				//personTablePID = pointOpenProject.getSelectedPersonPID(); // Selected person
				selectString = setSelectSQL("*", personPartnerTable, "EVNT_RPID = " + eventTablePID);
				partnerTableRS = requestTableData(selectString, dataBaseIndex);
				partnerTableRS.first();
				priPartnerPID = partnerTableRS.getLong("PRI_PARTNER_RPID");
				secPartnerPID = partnerTableRS.getLong("SEC_PARTNER_RPID");
				eventNumber = partnerTableRS.getInt("PARTNER_TYPE");
				partnerTableRS.close();
				personName = pointLibraryResultSet.exstractPersonName(priPartnerPID, personNameStyle, dataBaseIndex)
						+ " & " + pointLibraryResultSet.exstractPersonName(secPartnerPID, personNameStyle, dataBaseIndex);
			} else {
			// find the primary assoc person for event
				personTablePID = eventSelected.getLong("PRIM_ASSOC_RPID"); // Find person releted to event
				personName = pointLibraryResultSet.exstractPersonName(personTablePID, personNameStyle, dataBaseIndex);
			}
			eventName = " " + pointLibraryResultSet.getEventName(eventNumber, langCode, dataBaseIndex).trim();
			eventInfo = " " + eventName + ":" + personName;

		// Find location for event
			locationNameRPID = eventSelected.getLong("EVNT_LOCN_RPID");
			eventPlace = pointLibraryResultSet.selectLocationName(locationNameRPID,
																	locationNameStyle, dataBaseIndex);
			eventHDatePID = eventSelected.getLong("START_HDATE_RPID");
			eventDate = " " + pointLibraryResultSet.exstractDate(eventHDatePID, dataBaseIndex).trim();
			//System.out.println(" Event data - PID: " + eventTablePID + " Person: " + personName);

	// Set up associate table
			prepareAssociateTable(eventTablePID);

	// Set up images
			errorCode = pointMediaHandler.getAllExhibitImage(eventTablePID, eventImage, dataBaseIndex);
			if (errorCode > 1) {
				System.out.println(" HBViewPointHandler - ViewEventData - exhibittable, Image error PID: " + eventTablePID);
			}

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException("HBViewPointHandler - updateEventVPtable_v22 error: " + sqle.getMessage());
		}
	}

/**
 * private void prepareAssociateTable()
 * @throws HBException
 */
	private void prepareAssociateTable() throws HBException {
		prepareAssociateTable(eventTablePID);
	}

/**
 * private void prepareAssociateTable(long eventTablePID)
 * @param selectedPersonPID
 * @throws HBException
 */
	private void prepareAssociateTable(long eventTablePID) throws HBException {
		String selectString = null;
		ResultSet eventSelected, eventAssocSelected;
		String langCode = HGlobal.dataLanguage;
		ArrayList<Object[]> associateList = new ArrayList<>();
		String personName = "", eventRole = "", eventName = "", eventDate = "--";
		int assocs = 0, eventNumber = 0, eventRoleCode;
		long eventPID, assocPersonPID, eventHDatePID;
		personPIDs = new HashMap<>();
		try {

		// Add key assoc to list
			numberOfAssociates = addKeyAssocToAssoc(eventTablePID, associateList);

		// Select the associte person for the event
			selectString = setSelectSQL("*", eventAssocTable, "EVNT_RPID = " + eventTablePID);
			eventAssocSelected = requestTableData(selectString, dataBaseIndex);
			eventAssocSelected.beforeFirst();
			while (eventAssocSelected.next()) {
				eventPID = eventAssocSelected.getLong("EVNT_RPID");
				assocPersonPID = eventAssocSelected.getLong("ASSOC_RPID");
				eventRoleCode = eventAssocSelected.getInt("ROLE_NUM");
				if (includeWitnessEvents) {
		// Get event data
					selectString = setSelectSQL("*", eventTable, "PID = " + eventPID);
					eventSelected = requestTableData(selectString, dataBaseIndex);
					eventSelected.first();
					eventHDatePID = eventSelected.getLong("START_HDATE_RPID");
					eventDate = pointLibraryResultSet.exstractDate(eventHDatePID, dataBaseIndex);
					eventNumber = eventSelected.getInt("EVNT_TYPE");
					eventName = pointLibraryResultSet.getEventName(eventNumber, langCode, dataBaseIndex);
					eventRole = pointLibraryResultSet.getRoleName(eventRoleCode,
						  			eventNumber,
						  			langCode,
						  			dataBaseIndex);
					personName = pointLibraryResultSet.exstractPersonName(assocPersonPID, personNameStyle, dataBaseIndex);
					if (HGlobal.DEBUG) {
						System.out.println(" Event Associate: " + assocs + " Event PID: " + eventPID + " Type: " + eventNumber
										+ " Event: " + eventName.trim() + " Name: " + personName + " Role: " + eventRole);
					}
					Object[] associates = new Object[5];
					associates[0] = personName.trim();
					associates[1] = eventRole.trim();
					associates[2] = eventName.trim();
					associates[3] = eventDate.trim();
					associates[4] = assocPersonPID;
					associateList.add(associates);
					assocs++;
				}
			}

		// Set number of row in ascociate table
			numberOfAssociates = numberOfAssociates + assocs;
			associateTable = new Object[numberOfAssociates][4];
			// Set up sort and list PID
			long[] personPID = new long[numberOfAssociates];
			for (int i = 0; i < numberOfAssociates; i++) {
				associateTable[i][0] = " " + associateList.get(i)[0];
				associateTable[i][1] = " " +  associateList.get(i)[1];
				associateTable[i][2] = " " +  associateList.get(i)[2];
				associateTable[i][3] = " " +  associateList.get(i)[3];
				personPID[i] = (long) associateList.get(i)[4];
			}

		// Setup the HashMap for row to personPID
			for (int row = 0; row < personPID.length; row++) {
				personPIDs.put(row, personPID[row]);
			}

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" prepareAssociateTable - SQL exception: " + sqle.getMessage()
						+ "\nSQL string: " + selectString);
		}
	}

/**
 * addKeyAssocToAssoc(long eventTablePID)
 * @param eventTablePID
 * @return
 * @throws HBException
 */
	private int addKeyAssocToAssoc(long eventTablePID, ArrayList<Object[]> associateList) throws HBException {
		String selectString = null;
		ResultSet keyAssocSelected, partnerRelationRS;
		String langCode = HGlobal.dataLanguage;
		String personName = "", eventRoleName = "", eventDate = "", eventName = "";
		int keyAssociates = 0, assocRoleCode, eventType, eventGroup, priRole = 0, secRole = 0, keyAssocs = 1;
		long  assocPersonPID, eventHDatePID, priPartnerPID = null_RPID, secPartnerPID = null_RPID;
		selectString = setSelectSQL("*", eventTable, "PID = " + eventTablePID);
		try {
			keyAssocSelected = requestTableData(selectString, dataBaseIndex);
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
				eventRoleName = pointLibraryResultSet.getRoleName(assocRoleCode,
			  			eventType,
			  			langCode,
			  			dataBaseIndex);
				personName = pointLibraryResultSet.exstractPersonName(assocPersonPID, personNameStyle, dataBaseIndex);

				if (HGlobal.DEBUG) {
					System.out.println(" Key Associate nr: " + keyAssociates + " Event PID: " + eventTablePID + " Type: " + eventType
						+ " Event: " + eventName.trim() + " Name: " + personName + " Role: " + eventRoleName);
				}

				Object[] associates = new Object[5];
				associates[0] = " " + personName.trim();
				associates[1] = " " + eventRoleName.trim();
				associates[2] = " " + eventName.trim();
				associates[3] = " " + eventDate.trim();
				associates[4] = assocPersonPID;
				associateList.add(associates);
				keyAssociates++;
			}

			return keyAssociates;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" HBPersonHandler - addPartnerAssocToAssoc \nerror: " + sqle.getMessage());
		}
	}

/**
 * ArrayList<ImageIcon> getImageList()
 * @return
 */
	public ArrayList<ImageIcon> getImageList() {
		return listOfImages;
	}

/**
 * int getNumberOfImages()
 * @return
 */
	public int getNumberOfImages() {
		if (listOfImages == null) {
			return 0;
		}
		return listOfImages.size();
	}

} // End Class ViewEventData
