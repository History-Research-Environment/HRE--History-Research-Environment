package hre.pf4j;
/*********************************************************************************
 * class FirstMainMenuExtension implements MainMenuExtensionPoint
 * *******************************************************************************
 * v0.03.0032 2025-01-04 - Upgraded for Build 32 and database v22c (N. Tolleshaug)
 **********************************************************************************/
import java.awt.Dialog.ModalityType;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.pf4j.Extension;
import hre.pf4j.ext.ReportMenuExtensionPoint;

import hre.bila.HBException;

import hre.bila.HBLibraryBusiness;
import hre.bila.HBLibraryResultSet;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.gui.HG0401HREMain;
import hre.nls.ReportPluginMenu;

@Extension(ordinal = 1)
public class PersonReportMenuExtension  implements ReportMenuExtensionPoint, ActionListener {

/**
 * Setting of database version	
 */
	public final static String buildDBversion = "v22c";
/**
 * Parameters	
 */
	String dBbuild = ""; // HRE db version
	boolean DEBUG = false;

	JDesktopPane mainPane;
	HG0401HREMain mainFrame;
	//HBBusinessLayer[] pointBusinessLayer;
	HBPersonHandler pointPersonHandler;
	//HBBusinessLayer pointBusinessLayer;
	HBLibraryResultSet pointLibraryResultSet; 
	HBLibraryBusiness pointLibraryBusiness;
	HBProjectOpenData selectedProject;
	
	JMenuItem plugInMenuItemA, plugInMenuItemB;	
	Object[][] eventsTable;	
	long proOffset = 1000000000000000L;
	long null_RPID  = 1999999999999999L;
	long selectedPersonPID = 0L;
	int personIndex;
	String personName;
	String reptLanguage = "en-US";
	
	// Setting of style for summary events
	int[] placeStyle = {0,1,2,3};
	int dataBaseIndex;
	
	
/**
 * setMainPane(JDesktopPane mainPane)
 */
	public void setMainPane(JDesktopPane mainPane) {
		this.mainPane = mainPane;
	}	
	
/**
 * setNationalLanguage(String nls)	
 * @param nationalLanguage National Language select string
 */
	public void setNationalLanguage(String nationalLanguage) {
		setGUIlanguage(nationalLanguage);
		if (DEBUG) System.out.println(" Plugin national language: " + nationalLanguage); //$NON-NLS-1$
	}
	
	
/**
 * 	String className()
 */
	public String className() {
		return getClass().getName();
	}
	
/**
 * 	
 * @param dbaseVersion
 */
	public void setDDLversion(String dbaseVersion) {
		dBbuild = dbaseVersion;
		if (DEBUG) System.out.println(" PersonReportMenuExtension - Report database version: " + dBbuild);
	}
	
/**
 * setDebug(boolean debug)	
 * @param debug
 */
	public void setDEBUG(boolean debug) {
		DEBUG = debug;
		if (DEBUG) System.out.println(" Debug report plugin: " + DEBUG);
	}
	
/**
 * setMainPointer(HG0401HREMain mainFrame)
 */
	@Override
	public void setMainPointer(HG0401HREMain mainFrame) {
		this.mainFrame = mainFrame;		
	}
	
/**
 * void buildMenuBar(JMenuBar menuBar)
 */
	@Override
    public void buildMenu(JMenu reportMenu) {
        JMenu menu = new JMenu(ReportPluginMenu.getString("Text_2") + " ++"); 
        plugInMenuItemA = new JMenuItem(ReportPluginMenu.getString("Text_3")); 
        plugInMenuItemA.addActionListener(this);
        menu.add(plugInMenuItemA);
        plugInMenuItemB = new JMenuItem(ReportPluginMenu.getString("Text_4")); 
        plugInMenuItemB.addActionListener(this);
        menu.add(plugInMenuItemB);
        reportMenu.add(menu);        
    }

/**
 * void actionPerformed(ActionEvent actionEvent)
 */
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
		if (actionEvent.getSource() == plugInMenuItemA) {
			if (!dBbuild.contains(buildDBversion)) {
				//JOptionPane.showMessageDialog(null,ReportPluginMenu.getString("Text_8"),ReportPluginMenu.getString("Text_9"),			 //$NON-NLS-1$ //$NON-NLS-2$
				JOptionPane.showMessageDialog(null," Report plugin not implemented for: " + buildDBversion,ReportPluginMenu.getString("Text_9"),			 //$NON-NLS-1$ //$NON-NLS-2$
						JOptionPane.INFORMATION_MESSAGE);
					return;
			}
			String windowTitle = ReportPluginMenu.getString("Text_5"); //$NON-NLS-1$
			//System.out.println(windowTitle);
			JDialog dialogWindow = createPluginWindow(windowTitle,100);
			JTextArea textArea = new JTextArea();
			JScrollPane scrollPane = new JScrollPane(textArea);
			dialogWindow.add(scrollPane);
			selectedProject = mainFrame.getSelectedOpenProject();
			pointPersonHandler = selectedProject.getPersonHandler();	
			if (pointPersonHandler == null) System.out.println(" Error: pointPersonHandler == null ");
			pointLibraryResultSet =  pointPersonHandler.pointLibraryResultSet;
			pointLibraryBusiness = pointPersonHandler.pointLibraryBusiness;	
			dataBaseIndex = selectedProject.getOpenDatabaseIndex();
			selectedPersonPID = selectedProject.getSelectedPersonPID();
			personIndex = selectedProject.getSelectedPersonIndex();
			personName = selectedProject.getSelectedPersonName();
			reptLanguage = selectedProject.getReportLanguage();
			
			

		// Set national language according to report language
			if (reptLanguage.equals("en-US")) setNationalLanguage("en");
			if (reptLanguage.equals("no-NB")) setNationalLanguage("no");
			if (reptLanguage.equals("de-DE")) setNationalLanguage("de");
			if (reptLanguage.equals("fr-FR")) setNationalLanguage("fr");
			
/**
 *  Warning
 */
			if (selectedPersonPID == null_RPID || personName == null) {
				JOptionPane.showMessageDialog(null,ReportPluginMenu.getString("Text_8"),ReportPluginMenu.getString("Text_9"),			 //$NON-NLS-1$ //$NON-NLS-2$
					JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			
			if (DEBUG) System.out.println(" Plugin selected person OK! ");
					
			textArea.append(ReportPluginMenu.getString("Text_10") + personIndex + ")" + personName + ":\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			textArea.append("  ---------------------------" + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			String text = "  "; //$NON-NLS-1$
			try {
				int nrRows = eventList(selectedPersonPID);
				for (int i = 0; i < nrRows; i++) {
					String age = "" +  eventsTable[i][4]; //$NON-NLS-1$
					text = text + ("" + eventsTable[i][0]).trim(); //$NON-NLS-1$
					text = text + ReportPluginMenu.getString("Text_18") + ("" + eventsTable[i][2]).trim(); //$NON-NLS-1$
					String eventPlace = ("" + eventsTable[i][3]).trim(); //$NON-NLS-1$
					if (eventPlace.length() > 0)
						text = text + ReportPluginMenu.getString("Text_21") + eventsTable[i][3]; //$NON-NLS-1$
					if (!age.trim().equals("0")) text = text + ReportPluginMenu.getString("Text_23") + eventsTable[i][4]; //$NON-NLS-1$ //$NON-NLS-2$
					text = text + ".  "; //$NON-NLS-1$
				}
							
				StringBuilder stringBuilder = new StringBuilder(text);
				int lineLength = 100;

				int i = 0;
				while (i + lineLength < stringBuilder.length() && (i = stringBuilder.lastIndexOf(" ", i + lineLength)) != -1) { //$NON-NLS-1$
				    stringBuilder.replace(i, i + 1, "\n  "); //$NON-NLS-1$
				}
				textArea.append(stringBuilder.toString());
			} catch (HBException hbe) {
				System.out.println("  PersonReportMenuExtension error: " + hbe.getMessage());
				hbe.printStackTrace();
			}
			
			dialogWindow.setVisible(true);
		}
		
		if (actionEvent.getSource() == plugInMenuItemB) {
			String windowTitle = ReportPluginMenu.getString("Text_27"); //$NON-NLS-1$
			JDialog dialogWindow = createPluginWindow(windowTitle,50);
			JTextArea textArea = new JTextArea();
			textArea.append(ReportPluginMenu.getString("Text_28") + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			dialogWindow.add(textArea);
			dialogWindow.setVisible(true);
		}
    	
    }
    
	 /**
	  * JDialog createPluginWindow(String windowTitle)   
	  * @param windowTitle
	  * @return
	  */
	private JDialog createPluginWindow(String windowTitle, int vert) {
	// Create and show a dialog with the menu bar.
		if (DEBUG) System.out.println(" Plugin create plugin window OK! ");
		JDialog dialog = new JDialog();
		JMenuBar dialogMenu = new JMenuBar();
		dialog.setTitle(windowTitle);
		dialog.setSize(700,300);
		dialog.setJMenuBar(dialogMenu);
	    dialog.setModalityType(ModalityType.APPLICATION_MODAL);
		Point xymainPane = mainPane.getLocationOnScreen();     		
		dialog.setLocation(xymainPane.x + 20, xymainPane.y + vert);  				
		return dialog;
	}
		
/**
 * int eventList(long selectPersonPID)	
 * @param selectPersonPID
 * @return
 */
	private int eventList(long selectPersonPID) throws HBException {
				 
		String eventTable = "T450_EVNT"; //, eventAssocTable = "T451_EVNT_ASSOC";
		ResultSet eventSelected; //, eventAssocSelected;
		String selectString = null, eventPlaceName = " - "; 
		int nrRows = 0, row = 0;
		//long eventPID;
		Object[][] eventsData = null;
		String[] sortStrings;
		ArrayList<String[]> eventList = new ArrayList<String[]>();
		String[] events;
		String placeTableCodes = "500|1200|2900|3000|3300|3400|";
		String[] elemntCodes = placeTableCodes.split("\\|");
		
		//pointBusinessLayer[1].dateFormatSelect();
		//pointBusinessLayer[1].setUpDateTranslation();
		pointPersonHandler.dateFormatSelect();
		pointPersonHandler.setUpDateTranslation();
	
		try {		

			if (DEBUG) System.out.println(" Plugin start event processing! ");
			//selectString = pointBusinessLayer[1].setSelectSQL("*", eventTable, "PRIM_ASSOC_RPID = " + selectPersonPID);	
			selectString = pointPersonHandler.setSelectSQL("*", eventTable, "PRIM_ASSOC_RPID = " + selectPersonPID);	
			//eventSelected = pointBusinessLayer[1].requestTableData(selectString, dataBaseIndex);	
			eventSelected = pointPersonHandler.requestTableData(selectString, dataBaseIndex);
			eventSelected.beforeFirst();			
			sortStrings = new String[nrRows];
			long eventHDatePID;
			eventSelected.beforeFirst();
			while (eventSelected.next()) {		
				
				events = new String[6];
				long locationName_RPID = eventSelected.getLong("EVNT_LOCN_RPID"); //$NON-NLS-1$
				int eventNumber = eventSelected.getInt("EVNT_TYPE");
				events[0] = " " + pointLibraryResultSet.getEventName(eventNumber, reptLanguage, dataBaseIndex).trim();
				eventPlaceName = pointLibraryResultSet.selectLocationName(locationName_RPID,
															elemntCodes,
															dataBaseIndex);
				
				eventHDatePID = eventSelected.getLong("START_HDATE_RPID"); //$NON-NLS-1$
				events[1] = ReportPluginMenu.getString("Text_47");	 //$NON-NLS-1$
				events[2] = pointLibraryResultSet.exstractDate(eventHDatePID,dataBaseIndex);
				events[3] = eventPlaceName;	
				events[4] = pointLibraryResultSet.calculateAge(eventHDatePID,selectPersonPID, dataBaseIndex );
				events[5] = pointLibraryResultSet.exstractSortString(eventHDatePID, dataBaseIndex);
				eventList.add(events);
				row++;
				if (DEBUG) System.out.println(" Plugin event " + events[0] + " - number! " + row);
			}		
			
			nrRows = row;
			eventsData = new Object[nrRows][6];
			sortStrings = new String[nrRows];
				
			for (int i = 0; i < nrRows; i++) {
				eventsData[i][0] = " " + eventList.get(i)[0];
				eventsData[i][1] = " " + eventList.get(i)[1];
				eventsData[i][2] = " " + eventList.get(i)[2];
				eventsData[i][3] = " " + eventList.get(i)[3];
				eventsData[i][4] = " " + eventList.get(i)[4];
				sortStrings[i] = eventList.get(i)[5];
			}
			
		// Sort the events according to HDATE SORT
			if (row > 0) {
				int[] sortIndex = pointLibraryBusiness.sort(sortStrings);
				//eventsTable = pointBusinessLayer[1].sortTable(eventsData, sortIndex); 
				eventsTable = pointPersonHandler.sortTable(eventsData, sortIndex); 
			}
			return nrRows;		
			
		} catch (SQLException sqle ) {
			throw new HBException("SQL exception: " + sqle.getMessage()  //$NON-NLS-1$
						+ "\nSQL string: " + selectString); //$NON-NLS-1$
		} catch (HBException hbe) {
			System.out.println("  PersonReportMenuExtension error: " + hbe.getMessage());
			throw new HBException("  PersonReportMenuExtension error: " + hbe.getMessage()); //$NON-NLS-1$
		}
	}	
		
/**
 * void setGUIlanguage(String nationalLanguage)		
 * @param nationalLanguage national language select string
 */
	public void setGUIlanguage(String nationalLanguage) {
		// Some possible Native language strings are the following. Only those
		// marked have NLS tables built, so if we find anything else,
		// we have to change back to English.
		//		"da" 	 Danish
		//		"de"	 German - NLS tables exist
		//		"el"	 Greek
		//		"en"	 English - NLS tables exist
		//		"es"	 Spanish
		//		"fr"	 French - NLS tables exist
		//		"it"	 Italian
		//		"nl"	 Dutch
		//		"no"	 Norwegian - NLS tables exist
		//		"pt" 	 Portugese
		
	// So test the nativeLanguage string - if not valid, set it back to 'en'
		if (	!"en".equals(nationalLanguage) 		
			&& 	!"fr".equals(nationalLanguage) 	
			&&	!"de".equals(nationalLanguage) 	
			&& 	!"no".equals(nationalLanguage) 	
			) nationalLanguage = "en";
				
	// Invoke language switch for Java-managed text items
		new ReportPluginMenu(nationalLanguage);
	}  // End setGUIlanguage		
} // End PersonReportMenuExtension
