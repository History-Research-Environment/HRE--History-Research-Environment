package hre.gui;
/*******************************************************************************
 * Manage Event - Specification 05.52 GUI_Event Edit
 * v0.01.0027 2022-04-07 first draft (D Ferguson)
 *			  2022-04-16 Revised layout for less ambiguity (D Ferguson)
 *			  2022-04-20 Added calls to HG0551DefineEvent (D Ferguson)
 * v0.01.0028 2022-10-16 Adjust screen literals (D Ferguson)
 * v0.01.0029 2023-04-26 Adjust Event group radio buttons (D Ferguson)
 * v0.03.0031 2024-01-23 Implement setting/finding disabled events (D Ferguson)
 * 			  2024-01-26 Make it a 4-column layout (screen too high)(D Ferguson)
 * 			  2024-01-27 Add a Quick event find field (D Ferguson)
 * 			  2024-02-29 Reset event and role lists; fix radio-buttons (D Ferguson)
 * 			  2024-03-02 Fix crash if new event selected after role select (D Ferguson)
 * 			  2024-03-02 Included en-US roles in addition to en-GB in role select (D Ferguson)
 * 			  2024-03-22 Second radiobutton group for user added event sets (D Ferguson)
 * 			  2024-03-22 New handling of date, location and memo (N. Tolleshaug)
 * 			  2024-04-01 Fix errors in handling control buttons (D Ferguson)
 * 			  2024-05-26 New handling event edit (N. Tolleshaug)
 * 			  2024-06-10 Rewrite test of partner event (N. Tolleshaug)
 * 			  2024-06-14 Remove Name radio-button selection (D Ferguson)
 * 			  2024-06-16 Place Partner user msg properly (D Ferguson)
 * 			  2024-07-21 NLS conversion (D Ferguson)
 * 			  2024-12-01 Replace JOptionPane 'null' locations with 'contents' (D Ferguson)
 * v0.04.0032 2025-01-11 Rearranged processing of event and roles (N. Tolleshaug)
 * 			  2025-01-17 Implement add or edit event type (N. Tolleshaug)
 * 			  2025-01-20 Added Event deletion confirmation msgs (D Ferguson)
 * 			  2025-01-26 Fixed error in delete event type handling (N. Tolleshaug)
 * 			  2025-04-27 Changed btn_Select.addActionListener to use activateAddEvent (N. Tolleshaug)
 * 			  2025-05-22 Removed unused selectedRole/Event fields (D Ferguson)
 * 			  2025-06-29 Correctly handle Reminder screen display/remove (D Ferguson)
 * 			  2025-07-03 Correctly handle partner events when invoked from menu (D Ferguson)
 * 			  2025-07-16 Load HG0551 with corrected position and modality (D Ferguson)
 * 			  2026-01-01 Updated code for pointer to HBEventRoleManager (N. Tolleshaug)
 * 			  2026-01-13 Implemented add, edit and copy eventtype (N. Tolleshaug)
 * 			  2026-01-22 Implemented reset event list after DefineEvent action (N. Tolleshaug)
 * 			  2026-01-30 Make event double-click invoke Edit automatically (D Ferguson)
 * 			  2026-01-31 Reload Event List for correct event group after changes (D Ferguson)
 * 			  2026-02-02 Log all catch block msgs and do NLS update (D Ferguson)
 ********************************************************************************
 * NOTES for incomplete functionality:
 * NOTE05 code needed for Grouped Events, Enable/Disable Events
 ********************************************************************************/

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import hre.bila.HB0711Logging;
import hre.bila.HBEventRoleManager;
import hre.bila.HBException;
import hre.bila.HBProjectOpenData;
import hre.bila.HBWhereWhenHandler;
import hre.nls.HG0552Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Manage Events
 * @author D Ferguson
 * @version v0.04.0032
 * @since 2022-04-07
 */

/**
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

public class HG0552ManageEvent extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;
	private static final int marriageRelatedEventGroup = 6;

	public String screenID = "55200";	//$NON-NLS-1$
	private String className;
	private JPanel contents;

	long null_RPID  = 1999999999999999L;

	HBEventRoleManager pointEventRoleManager;

	// Lists for holding event/role data
	private JList<String> eventList;
	private JList<String> roleList;
	String[] eventTypeList;
	int[] eventTypeNumber;
	String[] eventRoleList;
	Object[][] eventRoleData;

    private int indexSelectedEvent;
    private int selectedEventType;
    private int selectedRoleType;
    private int indexSelectedRole;
    private int selectedPartnerTableRow;
    private int eventGroup = 0;
    private boolean roleListenOn = true;
    private boolean partnerEventDetected = false;
    DefaultListModel<String> eventListmodel;
    DefaultListModel<String> roleListmodel;
    int[] marrTypes;

	JButton btn_Add;
	JButton btn_AddSet;
	JButton btn_Edit;
	JButton btn_Delete;
	JButton btn_Copy;
	JButton btn_Disable;

/**
 * String getClassName()
 * @return className
 */
    public String getClassName() {
    	return className;
    }

    public void setPartnerTableSelectedRow(int selectedPartnerTableIndex) {
    	selectedPartnerTableRow = selectedPartnerTableIndex;
    }

/**
 * Create the dialog
 * @throws HBException
 */
	public HG0552ManageEvent(HBProjectOpenData pointOpenProject, String forPerson) throws HBException {
		this.pointOpenProject = pointOpenProject;

	// Setup references
		windowID = screenID;
		helpName = "manageevent";	//$NON-NLS-1$
    	className = getClass().getSimpleName();
    	this.setResizable(true);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: entering HG0552ManageEvent");}	//$NON-NLS-1$
	    setTitle(HG0552Msgs.Text_0);	// Manage Events

	    pointEventRoleManager = pointOpenProject.getEventRoleManager();
	    pointEventRoleManager.setSelectedLanguage(HGlobal.dataLanguage);

	 // Get all partner event type codes
    	pointEventRoleManager.getEventTypeList(marriageRelatedEventGroup);
    	marrTypes = pointEventRoleManager.getEventTypes();

	// Setup initial All Events event/role lists
	    DefaultListModel<String> eventListmodel = new DefaultListModel<String>();
	    eventList = new JList<String>(eventListmodel);
	    resetEventList(eventGroup);
	    DefaultListModel<String> roleListmodel = new DefaultListModel<String>();
	    roleList = new JList<String>(roleListmodel);

/***********************************
 * Setup main panel and its contents
 ***********************************/
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]10[]10[grow]10[grow]", "[grow]5[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    	JToolBar toolBar = new JToolBar();
    	toolBar.setFloatable(false);
    	toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
    	JLabel toolBarHint = new JLabel(HG0552Msgs.Text_1); //    Select an Event for use by selecting the Event and Role
    	toolBar.add(toolBarHint);
    	toolBar.add(Box.createHorizontalGlue());
    	// Add HG0450 icons
		toolBar.add(btn_Remindericon);
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north");	//$NON-NLS-1$

/****************************************************
 * Setup left (control button) Panel and its contents
 ***************************************************/
		JPanel leftPanel = new JPanel();
		leftPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		contents.add(leftPanel, "cell 0 0, growx, aligny top");	//$NON-NLS-1$
		leftPanel.setLayout(new MigLayout("insets 5", "[]", "[]5[]5[]5[]5[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_EventEdit = new JLabel(HG0552Msgs.Text_2);		// Control Buttons
		leftPanel.add(lbl_EventEdit, "cell 0 0, alignx center");	//$NON-NLS-1$

		btn_Add = new JButton(HG0552Msgs.Text_3);					// Define New Event
		btn_Add.setToolTipText(HG0552Msgs.Text_4);					// Add a New Event
		leftPanel.add(btn_Add, "cell 0 1, growx, alignx center"); 	//$NON-NLS-1$

		btn_AddSet = new JButton(HG0552Msgs.Text_5);				// Define Event Set
		btn_AddSet.setToolTipText(HG0552Msgs.Text_6);				// Add a new Event Set
		leftPanel.add(btn_AddSet, "cell 0 2, growx, alignx center"); //$NON-NLS-1$

		btn_Edit = new JButton(HG0552Msgs.Text_7);					// Edit Event
		btn_Edit.setToolTipText(HG0552Msgs.Text_8);					// Edit the Chosen Event
		btn_Edit.setEnabled(false);
		leftPanel.add(btn_Edit, "cell 0 3, growx, alignx center"); //$NON-NLS-1$

		btn_Delete = new JButton(HG0552Msgs.Text_9);				// Delete Event
		btn_Delete.setToolTipText(HG0552Msgs.Text_10);				// Delete the Chosen Event
		btn_Delete.setEnabled(false);
		leftPanel.add(btn_Delete, "cell 0 4, growx, alignx center"); //$NON-NLS-1$

		btn_Copy = new JButton(HG0552Msgs.Text_11);					// Copy Event
		btn_Copy.setToolTipText(HG0552Msgs.Text_12);				// Copy the Chosen Event
		btn_Copy.setEnabled(false);
		leftPanel.add(btn_Copy, "cell 0 5, growx, alignx center"); //$NON-NLS-1$

		btn_Disable = new JButton(HG0552Msgs.Text_13);				// Disable Event
		btn_Disable.setToolTipText(HG0552Msgs.Text_14);				// Disable or Re-enable the chosen Event
		btn_Disable.setEnabled(false);
		leftPanel.add(btn_Disable, "cell 0 6, growx, alignx center"); //$NON-NLS-1$

/**************************************************
 * Setup 2nd column Panel and radio-button contents
 *************************************************/
		JPanel secondPanel = new JPanel();
		secondPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		contents.add(secondPanel, "cell 1 0, aligny top");	//$NON-NLS-1$
		secondPanel.setLayout(new MigLayout("insets 5", "[]", "[]5[]5[]5[]5[]5[]5[]5[]5[]5[]5[]5[]10[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_EventGrps = new JLabel(HG0552Msgs.Text_15);		// Select Event Group
		secondPanel.add(lbl_EventGrps, "cell 0 0, alignx center");	//$NON-NLS-1$

		// Define radio-buttons for the std ex-TMG Event groups +Other + Disabled
		// Also a button to switch to the alternate (user-defined) groups of Events
		JRadioButton radio_All = new JRadioButton(HG0552Msgs.Text_16);			// All Events
		radio_All.setSelected(true);
		secondPanel.add(radio_All, "cell 0 1, alignx left, hidemode 3");		//$NON-NLS-1$
		JRadioButton radio_Birth = new JRadioButton(HG0552Msgs.Text_17);		// Birth
		secondPanel.add(radio_Birth, "cell 0 2, alignx left, hidemode 3");		//$NON-NLS-1$
		JRadioButton radio_Marriage = new JRadioButton(HG0552Msgs.Text_18);		// Marriage
		secondPanel.add(radio_Marriage, "cell 0 3, alignx left, hidemode 3");	//$NON-NLS-1$
		JRadioButton radio_Death = new JRadioButton(HG0552Msgs.Text_19);		// Death
		secondPanel.add(radio_Death, "cell 0 4, alignx left, hidemode 3");		//$NON-NLS-1$
		JRadioButton radio_Burial = new JRadioButton(HG0552Msgs.Text_20);		// Burial
		secondPanel.add(radio_Burial, "cell 0 5, alignx left, hidemode 3");		//$NON-NLS-1$
		JRadioButton radio_History = new JRadioButton(HG0552Msgs.Text_21);		// History
		secondPanel.add(radio_History, "cell 0 6, alignx left, hidemode 3");	//$NON-NLS-1$
		JRadioButton radio_Address = new JRadioButton(HG0552Msgs.Text_22);		// Address
		secondPanel.add(radio_Address, "cell 0 7, alignx left, hidemode 3");	//$NON-NLS-1$
		JRadioButton radio_User = new JRadioButton(HG0552Msgs.Text_23);			// Your Event Groups
		secondPanel.add(radio_User, "cell 0 8, alignx left, hidemode 3");		//$NON-NLS-1$
		JRadioButton radio_Other = new JRadioButton(HG0552Msgs.Text_24);		// Other Events
		secondPanel.add(radio_Other, "cell 0 9, alignx left, hidemode 3");		//$NON-NLS-1$
		JRadioButton radio_InActive = new JRadioButton(HG0552Msgs.Text_25);		// Disabled Events
		secondPanel.add(radio_InActive, "cell 0 10, alignx left, hidemode 3");	//$NON-NLS-1$

		ButtonGroup radioGroup = new ButtonGroup();
		radioGroup.add(radio_All);
		radioGroup.add(radio_Birth);
		radioGroup.add(radio_Marriage);
		radioGroup.add(radio_Death);
		radioGroup.add(radio_Burial);
		radioGroup.add(radio_History);
		radioGroup.add(radio_Address);
		radioGroup.add(radio_User);
		radioGroup.add(radio_Other);
		radioGroup.add(radio_InActive);

		// Define radio-buttons for the User-defined event sets +Other + Disabled
		// Also a button to switch back to the std ex-TMG set
		JRadioButton radio_User11 = new JRadioButton(HG0552Msgs.Text_26);		// Census
		secondPanel.add(radio_User11, "cell 0 1, alignx left, hidemode 3");		//$NON-NLS-1$
		JRadioButton radio_User14 = new JRadioButton(HG0552Msgs.Text_27);		// Travel
		secondPanel.add(radio_User14, "cell 0 2, alignx left, hidemode 3");		//$NON-NLS-1$
		JRadioButton radio_User15 = new JRadioButton(HG0552Msgs.Text_28);		// Military
		secondPanel.add(radio_User15, "cell 0 3, alignx left, hidemode 3");		//$NON-NLS-1$
		JRadioButton radio_User16 = new JRadioButton(HG0552Msgs.Text_29);		// Your Event Group 1
		secondPanel.add(radio_User16, "cell 0 4, alignx left, hidemode 3");		//$NON-NLS-1$
		JRadioButton radio_User17 = new JRadioButton(HG0552Msgs.Text_30);		// Your Event Group 2
		secondPanel.add(radio_User17, "cell 0 5, alignx left, hidemode 3");		//$NON-NLS-1$
		JRadioButton radio_User18 = new JRadioButton(HG0552Msgs.Text_31);		// Your Event Group 3
		secondPanel.add(radio_User18, "cell 0 6, alignx left, hidemode 3");		//$NON-NLS-1$
		JRadioButton radio_User19 = new JRadioButton(HG0552Msgs.Text_32);		// Your Event Group 4
		secondPanel.add(radio_User19, "cell 0 7, alignx left, hidemode 3");		//$NON-NLS-1$
		JRadioButton radio_Std = new JRadioButton(HG0552Msgs.Text_33);			// Standard Events
		secondPanel.add(radio_Std, "cell 0 8, alignx left, hidemode 3");		//$NON-NLS-1$
		JRadioButton radio_Other2 = new JRadioButton(HG0552Msgs.Text_34);		// Other Events
		secondPanel.add(radio_Other2, "cell 0 9, alignx left, hidemode 3");		//$NON-NLS-1$
		JRadioButton radio_InActive2 = new JRadioButton(HG0552Msgs.Text_25);	// Disabled Events
		secondPanel.add(radio_InActive2, "cell 0 10, alignx left, hidemode 3");	//$NON-NLS-1$

		// Disable and hide this set of radio-buttons for now
		radio_User11.setVisible(false);
		radio_User14.setVisible(false);
		radio_User15.setVisible(false);
		radio_User16.setVisible(false);
		radio_User17.setVisible(false);
		radio_User18.setVisible(false);
		radio_User19.setVisible(false);
		radio_Std.setVisible(false);
		radio_Other2.setVisible(false);
		radio_InActive2.setVisible(false);
		radio_User11.setEnabled(false);
		radio_User14.setEnabled(false);
		radio_User15.setEnabled(false);
		radio_User16.setEnabled(false);
		radio_User17.setEnabled(false);
		radio_User18.setEnabled(false);
		radio_User19.setEnabled(false);
		radio_Std.setEnabled(false);
		radio_Other2.setEnabled(false);
		radio_InActive2.setEnabled(false);

		ButtonGroup radioGroup2 = new ButtonGroup();
		radioGroup2.add(radio_User11);
		radioGroup2.add(radio_User14);
		radioGroup2.add(radio_User15);
		radioGroup2.add(radio_User16);
		radioGroup2.add(radio_User17);
		radioGroup2.add(radio_User18);
		radioGroup2.add(radio_User19);
		radioGroup2.add(radio_Std);
		radioGroup2.add(radio_Other2);
		radioGroup2.add(radio_InActive2);

		JLabel lbl_Find = new JLabel(HG0552Msgs.Text_36);		// Find Event
		secondPanel.add(lbl_Find, "cell 0 11, alignx center"); //$NON-NLS-1$

		JTextField textToFind = new JTextField();
		secondPanel.add(textToFind, "cell 0 12, growx"); //$NON-NLS-1$

/**************************************
 * Setup Event Panel and its contents
 **************************************/
		JPanel eventPanel = new JPanel();
		eventPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		contents.add(eventPanel, "cell 2 0 1 2, aligny top");	//$NON-NLS-1$
		eventPanel.setLayout(new MigLayout("insets 5", "[]", "[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lblEventList = new JLabel(HG0552Msgs.Text_37);		// Events
		eventPanel.add(lblEventList, "cell 0 0, alignx center");	//$NON-NLS-1$

	// Load the Event list
		eventList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
	    eventList.setLayoutOrientation(JList.VERTICAL);
	// But don't add the list to the scrollpane yet!
	    JScrollPane eventScrollPane = new JScrollPane();
		eventScrollPane.setMinimumSize(new Dimension(180, 300));
		eventPanel.add(eventScrollPane, "cell 0 1");	//$NON-NLS-1$

/************************************
 * Setup Role Panel and its contents
 ***********************************/
		JPanel rolePanel = new JPanel();
		rolePanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		contents.add(rolePanel, "cell 3 0 1 2, aligny top");	//$NON-NLS-1$
		rolePanel.setLayout(new MigLayout("insets 5", "[]", "[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lblRoles = new JLabel(HG0552Msgs.Text_38);	// Roles of the Event
		rolePanel.add(lblRoles, "cell 0 0, alignx center");	//$NON-NLS-1$

		// Load the Role list
		roleList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
	    roleList.setLayoutOrientation(JList.VERTICAL);
	    // But don't add the list to the scrollpane yet!
		JScrollPane roleScrollPane = new JScrollPane();
		roleScrollPane.setMinimumSize(new Dimension(180, 300));
		rolePanel.add(roleScrollPane, "cell 0 1");	//$NON-NLS-1$

/****************************************************
 * Setup control buttons at bottom and display screen
 ****************************************************/
		JButton btn_Select = new JButton(HG0552Msgs.Text_39);	// Select
		btn_Select.setToolTipText(HG0552Msgs.Text_40);			// Select the chosen Event for use
		btn_Select.setEnabled(false);
		contents.add(btn_Select, "cell 3 2, align right, gapx 20, tag ok"); //$NON-NLS-1$

		JButton btn_Cancel = new JButton(HG0552Msgs.Text_41);	// Cancel
		btn_Cancel.setToolTipText(HG0552Msgs.Text_42);			// Close and Exit
		contents.add(btn_Cancel, "cell 3 2, align right, gapx 20, tag cancel"); //$NON-NLS-1$

	// Size the screen - pass #1
		pack();
	// Get the height of the secondPanel, less heading row and insets
		int height = secondPanel.getHeight() - lblEventList.getHeight() - 20;
	// Use it to resize the Event and Role scrollpanes
		eventScrollPane.setPreferredSize(new Dimension(180, height));
		roleScrollPane.setPreferredSize(new Dimension(180, height));
	// Load the lists into their correctly sized scrollpanes
		eventScrollPane.getViewport().setView(eventList);
		roleScrollPane.getViewport().setView(roleList);
	// Size the screen - pass #2, and display it
		pack();

/******************
 * ACTION LISTENERS
 ******************/
		// Listener for clicking 'X' on screen
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
	    		 btn_Cancel.doClick();
			}
		});

		// Listener for Cancel button
		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (reminderDisplay != null) reminderDisplay.dispose();
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: cancelling out of HG0552ManageEvent"); //$NON-NLS-1$
				dispose();
			}
		});

	// Listeners for std radio-button group items, to show event and rolePanel data
		// Load ALL events to the Events list
		ActionListener actionRadioAll = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int eventGroup = 0;
				btn_Disable.setText(HG0552Msgs.Text_13);	// Disable Event
				btn_Select.setEnabled(false);
				try {
					resetEventList(eventGroup);
				} catch (HBException hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0552 loading all event list " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
			}
		};

		 // Load events for Birth group (T460_EVNT_GROUP=4)
		 ActionListener actionRadioBirth = new ActionListener() {
			 public void actionPerformed(ActionEvent actionEvent) {
				 btn_Disable.setText(HG0552Msgs.Text_13);	// Disable Event
				 btn_Select.setEnabled(false);
				 eventGroup = 4;
				 try {
					 resetEventList(eventGroup);
				 } catch (HBException hbe) {
						if (HGlobal.writeLogs) {
							HB0711Logging.logWrite("ERROR: in HG0552 loading birth event list " + hbe.getMessage()); //$NON-NLS-1$
							HB0711Logging.printStackTraceToFile(hbe);
						}
				 }
			 }
		 };

		 // Load events for Marriage+divorce groups (T460_EVNT_GROUP=6 & 7)
		 ActionListener actionRadioMarriage = new ActionListener() {
			 public void actionPerformed(ActionEvent actionEvent) {
				 btn_Disable.setText(HG0552Msgs.Text_13);	// Disable Event
				 btn_Select.setEnabled(false);
				 eventGroup = 6;
				 try {
					 resetEventList(eventGroup);
				 } catch (HBException hbe) {
						if (HGlobal.writeLogs) {
							HB0711Logging.logWrite("ERROR: in HG0552 loading marriage event list " + hbe.getMessage()); //$NON-NLS-1$
							HB0711Logging.printStackTraceToFile(hbe);
						}
				 }
			 }
		 };

		// Load events for Death group (T460_EVNT_GROUP=5)
		 ActionListener actionRadioDeath = new ActionListener() {
			 public void actionPerformed(ActionEvent actionEvent) {
				 btn_Disable.setText(HG0552Msgs.Text_13);	// Disable Event
				 btn_Select.setEnabled(false);
				 eventGroup = 5;
				 try {
					 resetEventList(eventGroup);
				 } catch (HBException hbe) {
						if (HGlobal.writeLogs) {
							HB0711Logging.logWrite("ERROR: in HG0552 loading death event list " + hbe.getMessage()); //$NON-NLS-1$
							HB0711Logging.printStackTraceToFile(hbe);
						}
				 }
			 }
		 };

		 // Load events for Burial group (T460_EVNT_GROUP=9)
		 ActionListener actionRadioBurial = new ActionListener() {
			 public void actionPerformed(ActionEvent actionEvent) {
				 btn_Disable.setText(HG0552Msgs.Text_13);	// Disable Event
				 btn_Select.setEnabled(false);
				 eventGroup = 9;
				 try {
					 resetEventList(eventGroup);
				 } catch (HBException hbe) {
						if (HGlobal.writeLogs) {
							HB0711Logging.logWrite("ERROR: in HG0552 loading burial event list " + hbe.getMessage()); //$NON-NLS-1$
							HB0711Logging.printStackTraceToFile(hbe);
						}
				 }
			 }
		 };

		 // Load events for History group (T460_EVNT_GROUP=8)
		 ActionListener actionRadioHistory = new ActionListener() {
			 public void actionPerformed(ActionEvent actionEvent) {
				 btn_Disable.setText(HG0552Msgs.Text_13);	// Disable Event
				 btn_Select.setEnabled(false);
				 eventGroup = 8;
				 try {
					 resetEventList(eventGroup);
				 } catch (HBException hbe) {
						if (HGlobal.writeLogs) {
							HB0711Logging.logWrite("ERROR: in HG0552 loading history event list " + hbe.getMessage()); //$NON-NLS-1$
							HB0711Logging.printStackTraceToFile(hbe);
						}
				 }
			 }
		 };

		 // Load events for Address group (T460_EVNT_GROUP=10)
		 ActionListener actionRadioAddress = new ActionListener() {
			 public void actionPerformed(ActionEvent actionEvent) {
				 btn_Disable.setText(HG0552Msgs.Text_13);	// Disable Event
				 btn_Select.setEnabled(false);
				 eventGroup = 10;
				 try {
					 resetEventList(eventGroup);
				 } catch (HBException hbe) {
						if (HGlobal.writeLogs) {
							HB0711Logging.logWrite("ERROR: in HG0552 loading address event list " + hbe.getMessage()); //$NON-NLS-1$
							HB0711Logging.printStackTraceToFile(hbe);
						}
				 }
			 }
		 };

		 // Switch to User-defined radio-buttons
		 ActionListener actionRadioUser = new ActionListener() {
			 public void actionPerformed(ActionEvent actionEvent) {
			 // Disable and hide THIS set of buttons
				 radio_All.setVisible(false);
				 radio_Birth.setVisible(false);
				 radio_Death.setVisible(false);
				 radio_Burial.setVisible(false);
				 radio_Marriage.setVisible(false);
				 radio_History.setVisible(false);
				 radio_Address.setVisible(false);
				 radio_User.setVisible(false);
				 radio_Other.setVisible(false);
				 radio_InActive.setVisible(false);
				 radio_All.setEnabled(false);
				 radio_Birth.setEnabled(false);
				 radio_Death.setEnabled(false);
				 radio_Burial.setEnabled(false);
				 radio_Marriage.setEnabled(false);
				 radio_History.setEnabled(false);
				 radio_Address.setEnabled(false);
				 radio_User.setEnabled(false);
				 radio_Other.setEnabled(false);
				 radio_InActive.setEnabled(false);
			// Enable and show the other set
				 radio_User11.setVisible(true);
				 radio_User14.setVisible(true);
				 radio_User15.setVisible(true);
				 radio_User16.setVisible(true);
				 radio_User17.setVisible(true);
				 radio_User18.setVisible(true);
				 radio_User19.setVisible(true);
				 radio_Std.setVisible(true);
				 radio_Other2.setVisible(true);
				 radio_InActive2.setVisible(true);
				 radio_User11.setEnabled(true);
				 radio_User14.setEnabled(true);
				 radio_User15.setEnabled(true);
				 radio_User16.setEnabled(true);
				 radio_User17.setEnabled(true);
				 radio_User18.setEnabled(true);
				 radio_User19.setEnabled(true);
				 radio_Std.setEnabled(true);
				 radio_Other2.setEnabled(true);
				 radio_InActive2.setEnabled(true);

				 clearBothLists();
				 radio_User11.setSelected(true);
				 radio_User11.doClick();
				 pack();
			 }
		 };

		 // Load events for Misc group (T460_EVNT_GROUP=99)
		 ActionListener actionRadioOther = new ActionListener() {
			 public void actionPerformed(ActionEvent actionEvent) {
				 btn_Disable.setText(HG0552Msgs.Text_13);	// Disable Event
				 btn_Select.setEnabled(false);
				 eventGroup = 99;
				 try {
					 resetEventList(eventGroup);
				 } catch (HBException hbe) {
						if (HGlobal.writeLogs) {
							HB0711Logging.logWrite("ERROR: in HG0552 loading misc. event list " + hbe.getMessage()); //$NON-NLS-1$
							HB0711Logging.printStackTraceToFile(hbe);
						}
				 }
			 }
		 };

		 // Load disabled events (T460_IS_ACTIVE=FALSE)
		 ActionListener actionInActive = new ActionListener() {
			 public void actionPerformed(ActionEvent actionEvent) {
				 btn_Disable.setText(HG0552Msgs.Text_51);	// Enable Event
				 btn_Select.setEnabled(false);
				 clearBothLists();

				 // NOTE04 - need code to get disabled event list loaded

			 }
		 };
		// Link the listeners above to the radio buttons
		radio_All.addActionListener(actionRadioAll);
		radio_Birth.addActionListener(actionRadioBirth);
		radio_Death.addActionListener(actionRadioDeath);
		radio_Burial.addActionListener(actionRadioBurial);
		radio_Marriage.addActionListener(actionRadioMarriage);
		radio_History.addActionListener(actionRadioHistory);
		radio_Address.addActionListener(actionRadioAddress);
		radio_User.addActionListener(actionRadioUser);
		radio_Other.addActionListener(actionRadioOther);
		radio_InActive.addActionListener(actionInActive);

	// Listeners for User radio-button group items, to show event and rolePanel data
		// Load User11 events to the Events list (T460_EVNT_GROUP=11)
		ActionListener actionRadioUser11 = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int eventGroup = 11;
				btn_Disable.setText(HG0552Msgs.Text_13);	// Disable Event
				btn_Select.setEnabled(false);
				try {
					resetEventList(eventGroup);
				} catch (HBException hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0552 loading user11 event list " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
			}
		};

		// Load User14 events to the Events list (T460_EVNT_GROUP=14)
		ActionListener actionRadioUser14 = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				btn_Disable.setText(HG0552Msgs.Text_13);	// Disable Event
				btn_Select.setEnabled(false);
				eventGroup = 14;
				try {
					resetEventList(eventGroup);
				} catch (HBException hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0552 loading user14 event list " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
			}
		};

		// Load User15 events to the Events list (T460_EVNT_GROUP=15)
		ActionListener actionRadioUser15 = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				btn_Disable.setText(HG0552Msgs.Text_13);	// Disable Event
				btn_Select.setEnabled(false);
				eventGroup = 15;
				try {
					resetEventList(eventGroup);
				} catch (HBException hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0552 loading user15 event list " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
			}
		};

		// Load User16 events to the Events list (T460_EVNT_GROUP=16)
		ActionListener actionRadioUser16 = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				btn_Disable.setText(HG0552Msgs.Text_13);	// Disable Event
				btn_Select.setEnabled(false);
				eventGroup = 16;
				try {
					resetEventList(eventGroup);
				} catch (HBException hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0552 loading user16 event list " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
			}
		};

		// Load User17 events to the Events list (T460_EVNT_GROUP=17)
		ActionListener actionRadioUser17 = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				btn_Disable.setText(HG0552Msgs.Text_13);	// Disable Event
				btn_Select.setEnabled(false);
				eventGroup = 17;
				try {
					resetEventList(eventGroup);
				} catch (HBException hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0552 loading user17 event list " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
			}
		};

		// Load User18 events to the Events list (T460_EVNT_GROUP=18)
		ActionListener actionRadioUser18 = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				btn_Disable.setText(HG0552Msgs.Text_13);	// Disable Event
				btn_Select.setEnabled(false);
				eventGroup = 18;
				try {
					resetEventList(eventGroup);
				} catch (HBException hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0552 loading user18 event list " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
			}
		};

		// Load User19 events to the Events list (T460_EVNT_GROUP=19)
		ActionListener actionRadioUser19 = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				btn_Disable.setText(HG0552Msgs.Text_13);	// Disable Event
				btn_Select.setEnabled(false);
				eventGroup = 19;
				try {
					resetEventList(eventGroup);
				} catch (HBException hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0552 loading user19 event list " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
			}
		};

		 // Switch to Standard radio-buttons
		ActionListener actionRadioStd = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
			// Disable and hide THIS set of buttons
				radio_User11.setVisible(false);
				radio_User14.setVisible(false);
				radio_User15.setVisible(false);
				radio_User16.setVisible(false);
				radio_User17.setVisible(false);
				radio_User18.setVisible(false);
				radio_User19.setVisible(false);
				radio_Std.setVisible(false);
				radio_Other2.setVisible(false);
				radio_InActive2.setVisible(false);
				radio_User11.setEnabled(false);
				radio_User14.setEnabled(false);
				radio_User15.setEnabled(false);
				radio_User16.setEnabled(false);
				radio_User17.setEnabled(false);
				radio_User18.setEnabled(false);
				radio_User19.setEnabled(false);
				radio_Std.setEnabled(false);
				radio_Other2.setEnabled(false);
				radio_InActive2.setEnabled(false);
			// Enable and show the std set of buttons
				radio_All.setVisible(true);
				radio_Birth.setVisible(true);
				radio_Death.setVisible(true);
				radio_Burial.setVisible(true);
				radio_Marriage.setVisible(true);
				radio_History.setVisible(true);
				radio_Address.setVisible(true);
				radio_User.setVisible(true);
				radio_Other.setVisible(true);
				radio_InActive.setVisible(true);
				radio_All.setEnabled(true);
				radio_Birth.setEnabled(true);
				radio_Death.setEnabled(true);
				radio_Burial.setEnabled(true);
				radio_Marriage.setEnabled(true);
				radio_History.setEnabled(true);
				radio_Address.setEnabled(true);
				radio_User.setEnabled(true);
				radio_Other.setEnabled(true);
				radio_InActive.setEnabled(true);

				clearBothLists();
				radio_All.setSelected(true);
				radio_All.doClick();
				pack();
			}
		};

		 // Load events for Misc group (T460_EVNT_GROUP=99)
		ActionListener actionRadioOther2 = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				btn_Disable.setText(HG0552Msgs.Text_13);	// Disable Event
				btn_Select.setEnabled(false);
				eventGroup = 99;
				try {
					resetEventList(eventGroup);
				} catch (HBException hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0552 loading misc. event list " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
			}
		};

		// Load disabled events (T460_IS_ACTIVE=FALSE)
		ActionListener actionInActive2 = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				btn_Disable.setText(HG0552Msgs.Text_51);	// Enable Event
				btn_Select.setEnabled(false);
				clearBothLists();

				// NOTE05 - need code to get disabled event list loaded

			}
		};
		// Link the listeners above to the radio buttons
		radio_User11.addActionListener(actionRadioUser11);
		radio_User14.addActionListener(actionRadioUser14);
		radio_User15.addActionListener(actionRadioUser15);
		radio_User16.addActionListener(actionRadioUser16);
		radio_User17.addActionListener(actionRadioUser17);
		radio_User18.addActionListener(actionRadioUser18);
		radio_User19.addActionListener(actionRadioUser19);
		radio_Std.addActionListener(actionRadioStd);
		radio_Other2.addActionListener(actionRadioOther2);
		radio_InActive2.addActionListener(actionInActive2);

		// Listener to action selection of row in Event list
		ListSelectionListener eventListener = new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent selectEvent) {
				if (!eventList.getValueIsAdjusting()) {
					// Save the eventList index and selected value for use
					indexSelectedEvent = eventList.getSelectedIndex();
					if (indexSelectedEvent < 0) return;
					int[] eventTypes = pointEventRoleManager.getEventTypes();
					selectedEventType = eventTypes[indexSelectedEvent];
					// Load the rolelist for this event
					try {
						resetRoleList(selectedEventType);
					}  catch (HBException hbe) {
						if (HGlobal.writeLogs) {
							HB0711Logging.logWrite("ERROR: in HG0552 loading role list " + hbe.getMessage()); //$NON-NLS-1$
							HB0711Logging.printStackTraceToFile(hbe);
						}
					}
					btn_Select.setEnabled(false);
				// Enable all the relevant action buttons
					btn_Edit.setEnabled(true);
					btn_Delete.setEnabled(true);
					btn_Copy.setEnabled(true);
					btn_Disable.setEnabled(true);
				// Disable irrelevant buttons
					btn_Add.setEnabled(false);
					btn_AddSet.setEnabled(false);
				}
			}
		};
		eventList.addListSelectionListener(eventListener);

		// Listener for double-click on Event in list
		eventList.addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent me) {
		    	// double-click?
		        if (me.getClickCount() == 2) {
		            int index = eventList.locationToIndex(me.getPoint());
		            if (index >= 0) btn_Edit.doClick();
		        }
		    }
		});

		// Listener to action selection of row in Roles list
	    ListSelectionListener roleListener = new ListSelectionListener() {
	    	public void valueChanged(ListSelectionEvent selectRole) {
	    		if (roleListenOn) {
	    			if (!roleList.getValueIsAdjusting()) {
	    				// If a Partner event/role selected, direct user to Partners area
	    				try {
							if (testForPartnerEvent(selectedEventType)) {
								JOptionPane.showMessageDialog(radio_Address,
															HG0552Msgs.Text_61,	// Proceed by selecting 'Add new partner' \nfrom within the Partner table
															HG0552Msgs.Text_62, // Add Partner Event
															JOptionPane.INFORMATION_MESSAGE);
								if (reminderDisplay != null) reminderDisplay.dispose();
								if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0552ManageEvent to partner event"); //$NON-NLS-1$
								dispose();
							}
						} catch (HBException hbe) {
							if (HGlobal.writeLogs) {
								HB0711Logging.logWrite("ERROR: in HG0552 selecting role " + hbe.getMessage()); //$NON-NLS-1$
								HB0711Logging.printStackTraceToFile(hbe);
							}
						}
	    			// Otherwise, save the roleList index and selected value for use
	    				indexSelectedRole = roleList.getSelectedIndex();
	    				int[] eventRoleTypes = pointEventRoleManager.getEventRoleNumbers();
	    				selectedRoleType = eventRoleTypes[indexSelectedRole];
	    			// Enable the Select action
	    				btn_Select.setEnabled(true);
	    			// Disable all action buttons (no longer needed)
	    				btn_Edit.setEnabled(false);
	    				btn_Delete.setEnabled(false);
	    				btn_Copy.setEnabled(false);
	    				btn_Disable.setEnabled(false);
	    			}
	    		}
	    	}
	    };
		roleList.addListSelectionListener(roleListener);

		// Listener for Add new Event Type button
		btn_Add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
			// load HG0551DefineEvent screen (no Event Name/role) to create new Event type
				pointEventRoleManager.setSelectedLanguage(HGlobal.dataLanguage);
				HG0551DefineEvent newEventType = pointEventRoleManager.activateAddEventType();
				newEventType.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xymainPane = lbl_EventGrps.getLocationOnScreen();
				newEventType.setLocation(xymainPane.x-100, xymainPane.y);
				if (reminderDisplay != null) reminderDisplay.dispose();
				newEventType.setVisible(true);
				try {
					resetEventList(eventGroup);
				} catch (HBException hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0552 adding event " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
			}
		});

		// Listener for Add Event Set button
		btn_AddSet.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

			// NOTE05 need code here to show screen for adding an Event composed of Events

			}
		});

		// Listener for Edit Event Type button
		btn_Edit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
			// load HG0551DefineEvent screen (passing Event Name/role) to edit Event type
				pointEventRoleManager.setSelectedLanguage(HGlobal.dataLanguage);
				HG0551DefineEvent editEventType = pointEventRoleManager.activateEditEventType(selectedEventType);
				editEventType.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xymainPane = lbl_EventGrps.getLocationOnScreen();
				editEventType.setLocation(xymainPane.x-100, xymainPane.y);
				if (reminderDisplay != null) reminderDisplay.dispose();
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0552ManageEvent to edit Event"); //$NON-NLS-1$
				editEventType.setVisible(true);
				try {
					resetEventList(eventGroup);
				} catch (HBException hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0552 editing event " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
			}
		});

		// Listener for Copy button
		btn_Copy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
		   // load HG0551DefineEvent screen (passing Event Name/role) to copy Event type
				pointEventRoleManager.setSelectedLanguage(HGlobal.dataLanguage);
				HG0551DefineEvent editEventType = pointEventRoleManager.activateCopyEventType(selectedEventType);
				editEventType.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xymainPane = lbl_EventGrps.getLocationOnScreen();
				editEventType.setLocation(xymainPane.x-100, xymainPane.y);
				if (reminderDisplay != null) reminderDisplay.dispose();
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0552ManageEvent to edit Event"); //$NON-NLS-1$
				editEventType.setVisible(true);
				try {
					resetEventList(eventGroup);
				} catch (HBException hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0552 copying event " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
			}
		});

		// Listener for Delete button
		btn_Delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String eventTypeName = pointEventRoleManager.getEventName(selectedEventType);
				try {
					int errorCode = pointEventRoleManager.deleteEventType(selectedEventType);
					if (errorCode == 0)
						JOptionPane.showMessageDialog(btn_Delete,
													HG0552Msgs.Text_66 + eventTypeName	// Event type '
													+ HG0552Msgs.Text_67,		// ' deleted
													HG0552Msgs.Text_65, 		// Delete Event
													JOptionPane.INFORMATION_MESSAGE);
					if (errorCode == 1)
						JOptionPane.showMessageDialog(btn_Delete,
													HG0552Msgs.Text_66 + eventTypeName 	// Event type '
													+ HG0552Msgs.Text_68,	// 'is in use - cannot be deleted
													HG0552Msgs.Text_65, 	// Delete Event
													JOptionPane.ERROR_MESSAGE);
					resetEventList(eventGroup);
				} catch (HBException hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0552 deleting event " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
			}
		});

		// Listener for Disable (or Enable) button
		btn_Disable.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				// NOTE05 need code here to reverse T460 IS_ACTIVE true/false setting

				if (reminderDisplay != null) reminderDisplay.dispose();
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0552ManageEvent after Disable"); //$NON-NLS-1$
				dispose();
			}
		});

		// Listener for an entry in TextField textToFind (Event Search)
		textToFind.getDocument().addDocumentListener(new DocumentListener() {
	          @Override
	          public void insertUpdate(DocumentEvent e) {
	        	  eventList.clearSelection();
	              findTheText();
	          }
	          @Override
	          public void removeUpdate(DocumentEvent e) {
	        	  eventList.clearSelection();
	        	  findTheText();
	          }
	          @Override
	          public void changedUpdate(DocumentEvent e) {
	        	  eventList.clearSelection();
	        	  findTheText();
	          }
	          private void findTheText() {
	        	  String text = textToFind.getText();
	        	  if (text.length() == 0) return;		// return if text is now null
	      	      int textIndex = eventList.getNextMatch(text, 0, javax.swing.text.Position.Bias.Forward);
	    	      if (textIndex < 0) return;			// return if index has no matches
	    	      eventList.ensureIndexIsVisible(textIndex);		// make sure selected item visible in scrollpane
	    	      eventList.setSelectedIndex(textIndex);	 		// highlight it
	          }
		});

		// Listener for Select button - pass the selected Event & Role and exit
		btn_Select.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				HG0547EditEvent editEventScreen = null;
				HBWhereWhenHandler pointHBWhereWhenHandler = pointOpenProject.getWhereWhenHandler();
				long eventPersonPID = null_RPID;
				if (partnerEventDetected)
				// Add partner event to existing partner relation
					editEventScreen = pointHBWhereWhenHandler.activateAddPartnerEvent(pointOpenProject,
											selectedEventType, selectedRoleType, eventPersonPID, selectedPartnerTableRow);
				else
				// Add any other event with undefined sexCode "U"
					editEventScreen = pointHBWhereWhenHandler.activateAddEvent(pointOpenProject,
											selectedEventType, selectedRoleType, eventPersonPID, "U"); //$NON-NLS-1$

				editEventScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xyShow = secondPanel.getLocationOnScreen();
				editEventScreen.setLocation(xyShow.x, xyShow.y);
				if (reminderDisplay != null) reminderDisplay.dispose();
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0552ManageEvent to edit Event"); //$NON-NLS-1$
				dispose();
				editEventScreen.setVisible(true);
			}
		});

	}	// End HG0552ManageEvent constructor

/**
 * resetEventList(int eventGroup)- reload Event List
 * @param eventGroup
 * @throws HBException
 */
    public void resetEventList(int eventGroup) throws HBException {
   	// Clean out existing event/role lists first
    	clearBothLists();
    // then reload event list
    	eventListmodel = (DefaultListModel<String>) eventList.getModel();
    	String[] newEventList = pointEventRoleManager.getEventTypeList(eventGroup);
    	for (int i = 0; i < newEventList.length; i++)  eventListmodel.addElement(newEventList[i]);
    	pack();
    }

/**
 * resetRoleList(int selectedEvent) - reload Role List
 * @param selectedEvent
 * @throws HBException
 */
    public void resetRoleList(int selectedEvent) throws HBException {
    	roleListmodel = (DefaultListModel<String>) roleList.getModel();
    	String[] newRoleList = pointEventRoleManager.getRolesForEvent(selectedEvent, "");	//$NON-NLS-1$

    // Turn off RoleListener and clear role list
    	roleListenOn = false;
    	if(roleListmodel.size() > 0) {
    		roleListmodel.removeAllElements();
    		roleListmodel.clear();
    		roleList.removeAll();
    	}
    // reload role list
    	if (newRoleList.length > 0)
    		for (int i = 0; i < newRoleList.length; i++)  roleListmodel.addElement(newRoleList[i]);
    	else JOptionPane.showMessageDialog(contents,
    			HG0552Msgs.Text_63,	// No Event Roles found. Are you using the correct \n HRE Data Presentation Language setting?
    			HG0552Msgs.Text_64, // Event Roles
    			JOptionPane.ERROR_MESSAGE);
    // turn on rolelistener again
    	roleListenOn = true;
    }

/**
 * clearBothLists() - clear Event and Role Lists
 */
    public void clearBothLists() {
   	// Turn off RoleListener and clear role list
      	roleListenOn = false;
        if (roleListmodel != null && roleListmodel.size() > 0) {
            roleListmodel.removeAllElements();
            roleListmodel.clear();
            roleList.removeAll();
        }
        roleListenOn = true;
     // Clean out existing event list
        if (eventListmodel != null && eventListmodel.size() > 0) {
            eventListmodel.removeAllElements();
            eventListmodel.clear();
            eventList.removeAll();
        // and enable/disable controls
            btn_Add.setEnabled(true);
            btn_AddSet.setEnabled(true);
            btn_Edit.setEnabled(false);
            btn_Delete.setEnabled(false);
            btn_Copy.setEnabled(false);
            btn_Disable.setEnabled(false);
        }
    }

/**
 * private boolean testForPartnerEvent(int selectedEventType)
 * @param selectedEventType
 * @return
 */
    private boolean testForPartnerEvent(int selectedEventType) throws HBException {
		for (int i = 0; i < marrTypes.length; i++)
			if (marrTypes[i] == selectedEventType) return true;
    	return false;
    }

}  // End of HG0552ManageEvent
