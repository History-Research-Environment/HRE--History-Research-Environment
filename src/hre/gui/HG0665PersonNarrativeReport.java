package hre.gui;
/************************************************************************************************
 * Class  HG0665PersonNarrativeReport
 * Processes data for narrative report
 * Receives througt interface TransferSelectedPerson the selected person
 ************************************************************************************************
 * v0.05.0034 2026-10-10 Implemented preliminary narrative report (N. Tolleshaug)
 * 			  2026-10-13 Added control buttons;made panel size variable (D Ferguson)
 * 			  2026-10-15 Updated [R:NNNNNN] roles for partners (N. Tolleshaug)
 * 			  2026-10-18 Improve GUI startup layout (D Ferguson)
 ************************************************************************************************/

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.text.DefaultCaret;

import hre.bila.HB0711Logging;
import hre.bila.HBEventRoleManager;
import hre.bila.HBException;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.bila.HBReportHandler;
import net.miginfocom.swing.MigLayout;

public class HG0665PersonNarrativeReport extends HG0450SuperDialog implements TransferSelectedPerson {
	private static final long serialVersionUID = 1L;
	public static final String screenID = "66500";	//$NON-NLS-1$

	long null_RPID  = 1999999999999999L;
	long proOffset  = 1000000000000000L;

	//private static final int nameRelatedEventGroup = 1;
	protected HBPersonHandler pointPersonHandler;
	protected HBReportHandler pointReportHandler;
	protected HBEventRoleManager pointEventRoleManager;
	HG0507SelectPerson personSelectScreen = null;
	HBProjectOpenData pointOpenProject;
	HG0665PersonNarrativeReport pointPersonNarrativeReport = this;
	protected JDialog relateFrame = this;

	private JPanel contents;
    JPanel reportPanel;
	private JTextArea reportTextArea;
	int dataBaseIndex;
	String languageCode = HGlobal.dataLanguage;

/**
 * HG0665PersonNarrativeReport
 * @param pointOpenProject
 * @throws HBException
 */
	public HG0665PersonNarrativeReport(HBProjectOpenData pointOpenProject) throws HBException {
		// Setup all references for HG0665
		windowID = screenID;
		helpName = "narrativereport";	//$NON-NLS-1$
		this.pointOpenProject = pointOpenProject;
		pointPersonHandler = pointOpenProject.getPersonHandler();
		setTitle("Individual Narrative report");

		// For Text area font setting
	    Font font = UIManager.getFont("TextArea.font");		//$NON-NLS-1$

/***********************************
 * Setup main panel and its contents
 ***********************************/
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[grow]", "[grow]10[]"));
    	JToolBar toolBar = new JToolBar();
    	toolBar.setFloatable(false);
    	toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
    	toolBar.add(Box.createHorizontalGlue());

    // Add our menu icons and HG0451 icons
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north");	//$NON-NLS-1$

	// Build a repprt preview panel/textarea
		reportPanel = new JPanel();
		reportPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		reportPanel.setLayout(new MigLayout("insets 10", "[grow]", "[grow, fill]"));
		contents.add(reportPanel, "cell 0 0, grow, push, hidemode 3");

		reportTextArea = new JTextArea(10, 40);	// force a row size so that adding a text block still computes properly
		reportTextArea.setWrapStyleWord(true);
		reportTextArea.setLineWrap(true);
		reportTextArea.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		reportTextArea.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)reportTextArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	    reportTextArea.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
	    reportTextArea.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
	    reportTextArea.setBorder(new JTable().getBorder());		// match Table border
	// Setup scrollpane with textarea and starter size
		JScrollPane reportTextAreaScroll = new JScrollPane(reportTextArea);
		reportTextAreaScroll.setMinimumSize(new Dimension(250, 300));
		reportTextAreaScroll.getViewport().setOpaque(false);
		reportTextAreaScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);  // Vert scroll if needed
		reportTextAreaScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		reportTextArea.setCaretPosition(0);	// set scrollbar to top
		reportPanel.add(reportTextAreaScroll, "cell 0 0, grow, push");
		reportPanel.setVisible(false);

	// Add control buttons
    	JButton btn_Select = new JButton("Select a Person");
    	contents.add(btn_Select, "cell 0 1, alignx right, gapx 100");		//$NON-NLS-1$
    	JButton btn_Close = new JButton("Close");
    	contents.add(btn_Close, "cell 0 1, alignx right, gapx 20");		//$NON-NLS-1$

	// Display the screen
		pack();

/******************
 * ACTION LISTENERS
 ******************/
		// Listener for clicking 'X' on screen
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
		    	btn_Close.doClick();
		    }
		});

	   	// listener for Close button
		btn_Close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
		    	if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG065PersonNarrativeReport"); //$NON-NLS-1$
		    	dispose();
			}
    	});

		// listener for Select Person button
		btn_Select.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				activatePersonSelect();
				reportPanel.setVisible(true);
				pack();
			}
		});

}	// End HG0665 constructor

/**
 * private void activatePersonSelect()
 */
	private void activatePersonSelect() {
		try {
			personSelectScreen = new HG0507SelectPerson(pointPersonHandler, pointOpenProject, false);
			personSelectScreen.additionalPanel = false; // Setting in Selectperson
			personSelectScreen.pointSelectInterface = pointPersonNarrativeReport;	// setting in selectPerson
			personSelectScreen.setModalityType(ModalityType.APPLICATION_MODAL);
			Point xyShow = contents.getLocationOnScreen();
			personSelectScreen.setLocation(xyShow.x, xyShow.y);
			personSelectScreen.setVisible(true);
		} catch (HBException hbe) {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in HG0665 findselect narrative person " + hbe.getMessage());	//$NON-NLS-1$
				HB0711Logging.printStackTraceToFile(hbe);
			}
		}
	}

	/**
	 * public void updatePersonPointer(long personPID)
	 */
		@Override
		public void updatePersonPointer(long personPID)  {
			ArrayList<Object[]> narrativeReportList = new ArrayList<Object[]>();
			ResultSet eventTableRS, partnerTableRS, nameTableRS;
			int index, narrativeReportLines, eventType, eventGroup, eventRole;
			int ownerType = 0; // owner type:  1 - name, 2 - event, 3 - associate, 4 - partner
			long personTablePID = personPID, priPartnerPID, secPartnerPID, eventTablePID = null_RPID, ownerTablePID,
					nameTablePID = null_RPID, sentenceTablePID;
			boolean nameSentence = false;

			Object[] reportEventData;
			String[] sortStrings;

			String personName, selectString, narrativeSentence, sentenceFormat;
			pointEventRoleManager = pointOpenProject.getEventRoleManager();
			pointReportHandler = pointOpenProject.getReportHandler();
			pointEventRoleManager.setSelectedLanguage(HGlobal.dataLanguage);
			dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();

		// Process single events
			selectString = pointReportHandler.setSelectSQL("*", pointReportHandler.eventTable,
											"PRIM_ASSOC_RPID = " + personTablePID);
			try {
				personName = pointOpenProject.getPersonHandler().getPersonName(personTablePID);
				setTitle("Narrative report for " + personName );
				reportTextArea.append("Narrative report for " + personName + "\n \n");
				eventTableRS = pointReportHandler.requestTableData(selectString, dataBaseIndex);
				eventTableRS.beforeFirst();
				while (eventTableRS.next()) {
					reportEventData = new Object[7];
					//eventTablePID = eventTableRS.getLong("PID"); // eventTablePID
					reportEventData[0] = eventTableRS.getLong("PID"); // eventTablePID
					eventType = eventTableRS.getInt("EVNT_TYPE");
					reportEventData[1] = eventTableRS.getInt("EVNT_TYPE"); // eventType
					reportEventData[2] = eventTableRS.getInt("PRIM_ASSOC_ROLE_NUM"); // eventRole
					reportEventData[3] = eventTableRS.getLong("EVNT_OWNER_RPID"); // ownerTablePID
					reportEventData[4] = 2; // ownerType = event
					reportEventData[5] = personTablePID;
					reportEventData[6] = pointReportHandler.pointLibraryResultSet.
							exstractSortString(eventTableRS.getLong("SORT_HDATE_RPID"), dataBaseIndex);
					eventGroup = pointPersonHandler.pointLibraryResultSet.getEventGroup(eventType, dataBaseIndex);
				// Skip partner events
					if (eventGroup != pointPersonHandler.marrGroup && eventGroup != pointPersonHandler.divorceGroup)
										narrativeReportList.add(reportEventData);
				}
				eventTableRS.close();

			// Process partner events
				selectString = pointPersonHandler.setSelectSQL("*", pointPersonHandler.personPartnerTable,
						"PRI_PARTNER_RPID = " + personTablePID + " OR SEC_PARTNER_RPID = " + personTablePID);
				partnerTableRS = pointPersonHandler.requestTableData(selectString, dataBaseIndex);
				partnerTableRS.beforeFirst();
				while (partnerTableRS.next()) {
					reportEventData = new Object[7];
					eventTablePID = partnerTableRS.getLong("EVNT_RPID");
					reportEventData[0] = partnerTableRS.getLong("EVNT_RPID"); // eventTablePID
					reportEventData[1] = partnerTableRS.getInt("PARTNER_TYPE"); // eventType
					reportEventData[3] = partnerTableRS.getLong("EVNT_RPID"); // ownerTablePID
					reportEventData[4] = 4; // ownerType = partner
					priPartnerPID = partnerTableRS.getLong("PRI_PARTNER_RPID");
					if (priPartnerPID == personPID) {
						reportEventData[5] = partnerTableRS.getLong("PRI_PARTNER_RPID");
						reportEventData[2] = partnerTableRS.getInt("PRI_ROLE"); // eventRole
					} else {
						reportEventData[5] = partnerTableRS.getLong("SEC_PARTNER_RPID");
						reportEventData[2] = partnerTableRS.getInt("SEC_ROLE"); // eventRole
					}

				// Select event data
					selectString = pointReportHandler.setSelectSQL("*", pointReportHandler.eventTable,
							"PID = " + eventTablePID);
					eventTableRS = pointReportHandler.requestTableData(selectString, dataBaseIndex);
					eventTableRS.first();
					//reportEventData[3] = eventTableRS.getLong("EVNT_OWNER_RPID"); // ownerTablePID
					reportEventData[6] = pointReportHandler.pointLibraryResultSet.
							exstractSortString(eventTableRS.getLong("SORT_HDATE_RPID"), dataBaseIndex);
					narrativeReportList.add(reportEventData);
				}
				partnerTableRS.close();
				eventTableRS.close();

			// Process name events
				selectString = pointReportHandler.setSelectSQL("*", pointReportHandler.personNameTable,
						"OWNER_RPID = " + personTablePID);
				nameTableRS = pointReportHandler.requestTableData(selectString, dataBaseIndex);
				nameTableRS.beforeFirst();
				while (nameTableRS.next()) {
					reportEventData = new Object[7];
				// Exclude primary name
					if (!nameTableRS.getBoolean("NAME_PRIMARY")) {
						reportEventData[0] = nameTableRS.getLong("PID"); // eventTablePID
						reportEventData[1] = nameTableRS.getInt("NAME_EVNT_TYPE"); // eventType
						reportEventData[2] = 1;
						reportEventData[3] = nameTableRS.getLong("OWNER_RPID"); // ownerTablePID
						reportEventData[4] = 1; // ownerType = name
						reportEventData[5] = personTablePID;
						reportEventData[6] = pointReportHandler.pointLibraryResultSet.
								exstractSortString(nameTableRS.getLong("SORT_HDATE_RPID"), dataBaseIndex);
						narrativeReportList.add(reportEventData);
					}
				}

			// Collect sort dates
				narrativeReportLines = narrativeReportList.size();
				sortStrings = new String[narrativeReportLines];
				for (int i = 0; i < narrativeReportLines; i++) {
					sortStrings[i] = (String) narrativeReportList.get(i)[6];
				}

			// Setup sort index
				int [] rowsSorted = pointReportHandler.sortDate(sortStrings);

				for (int i = 0; i < narrativeReportLines; i++) {
					index = rowsSorted[i];
					eventTablePID = (long) narrativeReportList.get(index)[0];
					eventType = (Integer) narrativeReportList.get(index)[1];
					eventRole = (Integer) narrativeReportList.get(index)[2];
					//ownerTablePID = (long) narrativeReportList.get(index)[3];
					ownerTablePID = eventTablePID;
					ownerType = (int) narrativeReportList.get(index)[4];
					personTablePID = (long) narrativeReportList.get(index)[5];
					if (ownerType == 1) {
						nameSentence = true;
						nameTablePID = eventTablePID;
					} else nameSentence = false;

			// Set up sentence processing for events or names
					if (nameSentence) pointReportHandler.createReportNameData(personTablePID, nameTablePID);
					else pointReportHandler.createReportEventData(personTablePID, eventTablePID, ownerTablePID, ownerType);

			//	owner types:  1 - name, 2 - event, 3 - associate, 4 - partner
					if (nameSentence) {
						sentenceTablePID = pointReportHandler.pointLibraryResultSet.
							selectSentenceSetPID(null_RPID, 0, eventType, 1, languageCode, dataBaseIndex);
				// if no local name sentence
						if (sentenceTablePID == null_RPID)
							sentenceTablePID = pointReportHandler.selectGlobalSentencePID(eventType, eventRole, languageCode);
						else if (HGlobal.DEBUG)
							System.out.println(" Local name sentence PID: " + sentenceTablePID);

					} else {
						sentenceTablePID = pointReportHandler.pointLibraryResultSet.
							selectSentenceSetPID(ownerTablePID, ownerType, eventType, eventRole, languageCode, dataBaseIndex);
				// if no local event sentence
						if (sentenceTablePID == null_RPID)
							sentenceTablePID = pointReportHandler.selectGlobalSentencePID(eventType, eventRole, languageCode);
						else if (HGlobal.DEBUG)
							System.out.println(" Local event sentence PID: " + sentenceTablePID);
					}
				// if not global for language, Select en-US fall back sentence
					if (sentenceTablePID == null_RPID)
						sentenceTablePID = pointReportHandler.selectGlobalSentencePID(eventType, eventRole, "en-US");

					sentenceFormat = pointReportHandler.pointLibraryResultSet.getSentenceSetString(sentenceTablePID, dataBaseIndex);
					//System.out.println(" Sentence Format: " + " Lang: " + languageCode + "  Sent: " + sentenceFormat);
					narrativeSentence = pointReportHandler.sentenceParser(sentenceFormat).trim();
					//System.out.println(" " + personName + " Event: " + eventType + " Sentence: " + narrativeSentence);

				// Inne i en l�kke eller metode:
					reportTextArea.append(narrativeSentence.trim());
					reportTextArea.append("\n \n");		// temporarily force new line per sevent sentence
				}
				reportTextArea.setCaretPosition(0);	// set scrollbar to top
			} catch (HBException hbe) {
				// TODO Auto-generated catch block
				hbe.printStackTrace();
			} catch (SQLException sqle) {
				// TODO Auto-generated catch block
				sqle.printStackTrace();
			}

		}
} // End HG0665PersonNarrativeReport
