package hre.gui;
/*******************************************************************************
 * Define Event - Specification 05.51 GUI_Define_Event
 * v0.01.0027 2022-04-20 first draft (D Ferguson)
 * v0.03.0031 2023-12-12 revise screen literals (D Ferguson)
 * 			  2024-01-23 add setting for Min. no. of Key Assocs. (D Ferguson)
 * 			  2024-01-31 turn on control buttons correctly (D Ferguson)
 * v0.04.0032 2025-01-13 Add actions for Add Role button (D Ferguson)
 * 		      2025-01-17 Implement add or edit event type (N. Tolleshaug)
 * 			  2025-01-20 Add move role up/down code; add Hint panel (D Ferguson)
 * 			  2025-01-30 Added processing of event hints (N. Tolleshaug)
 * 			  2025-02-03 Added NLS enabled error message (N. Tolleshaug)
 * 			  2025-02-22 Change role table col 0 to checkbox (D Ferguson)
 * 			  2025-06-01 Add test for Add vs. Edit of existing event (D Ferguson)
 * 			  2025-06-29 Correctly handle Reminder screen display/remove (D Ferguson)
 * 			  2025-07-17 Load sentences with correct rolenames converted (D Ferguson)
 * 			  2025-07-22 Load eventRoleSeq, KeyRole and minAssoc values (D Ferguson)
 * 			  2025-07-28 Enable btn_Save correctly when edits done (D Ferguson)
 * 						 Check for unsaved changes at Close or Language/Role change (D Ferguson)
 * 	 		  2025-08-04 Fix role data (sex, ages) incorrect load (D Ferguson)
 *			  2025-08-07 Rename various routines, Objects for greater clarity (D Ferguson)
 *						 Ensure numeric textfields only accept numbers (D Ferguson)
 *			  2025-08-08 Fix code to move roles up/down in table (D Ferguson)
 *			  2025-08-09 Fix Add Role code so correct new T461 created (D Ferguson)
 *			  2025-08-11 Fix handling of GEDCOM tagtype selection/saving (D Ferguson)
 *			  2025-08-13 Add update of both Role records after move up/down actions (D Ferguson)
 *			  2025-08-14 Fix role display not in role Seq ( D Ferguson)
 *			  2025-08-16 Use T204 record for table header (D Ferguson)
 *			  2025-12-17 NLS all code up to this point (D Ferguson)
 *
 ********************************************************************************
 * NOTES for incomplete functionality:
 * NOTE03 code in Save function for saving changed sentences
 * NOTE05 code needed to action Copy, Delete control buttons
 *
 * Note also that the TMG Event tag fields for Past Tense, Abbreviation and type
 * are ignored for now and no place has been set on this screen yet.
 ********************************************************************************/

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DocumentFilter;
import javax.swing.text.NumberFormatter;

import hre.bila.HB0711Logging;
import hre.bila.HBEventRoleManager;
import hre.bila.HBException;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.bila.HBWhereWhenHandler;
import hre.nls.HG0551Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Define Events
 * @author D Ferguson
 * @version v0.04.0032
 * @since 2022-04-20
 */
public class HG0551DefineEvent extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;
	HBEventRoleManager pointEventRoleManager;
	HBWhereWhenHandler pointWhereWhenHandler;
	HBPersonHandler pointPersonHandler;
	HG0547EditEvent pointEditEvent;
	int dataBaseIndex;
	int eventNumber;

	public String screenID = "55100";	//$NON-NLS-1$
	private String className;
	private JPanel contents;

	DefaultListModel<String> roleListmodel;
	JTable tableRole;

    private String selectedEventName, selectedRoleName, selectedLangCode, newEventName;
    int selectedEventNumber, selectedRoleNumber, selectedRoleSeq, selectedRowInRoleTable, selectedGroup, languageIndexDefault;
    boolean selectedKeyRole;
    boolean startAtSentenceTab = false;

    ItemListener comboLangListener;
    private int prevSelectedLangIndex, newSelectedLangIndex, prevSelectedRowInRoleTable;
    boolean notSavingRole = false;
    boolean roleChanged = false;
    boolean eventChanged = false;

	JFormattedTextField text_minYear, text_maxYear, text_minAssoc, text_minAge, text_maxAge;
	JTextField text_EventName, text_gedTag;
	JTextArea text_MaleSent, text_FemaleSent, hintText;

	DocumentListener roleTextListen, eventNameTextListen, eventTextListen, gedTextListen;
	JComboBox<String> comboGroups, comboSex, comboLanguage;
	ActionListener comboSexListener;

	String abbrev, pasttense, eventHint, gedComTag, roleSex, roleSentence;
	int newEventNumber, roleNumber, minAssoc, maxYear, minYear, roleMinAge, roleMaxAge, eventGroup, eventRoleSequence;

	JRadioButton radio_Tag, radio_Even;

	String[] sexOptions = {HG0551Msgs.Text_0, HG0551Msgs.Text_1, HG0551Msgs.Text_2};	// Any, Male, Female
	String[] sexValues = {"U","M","F"};	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	// For Text area font setting
    Font font = UIManager.getFont("TextArea.font");		//$NON-NLS-1$

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
	// Lists for holding event/role data
    String[] tableRoleHeader;
	Object[][] tableRoleData;
    String[] eventGroups;
    int[] eventGroupNumbers;
    HashMap<String, Integer> groupIndexMap = new  HashMap<String,Integer>();
    Object[] selectedRoleData = new Object[4];

	String[] eventNamesInGroup;
	int[] eventNumbersInGroup;
	String[] eventRoleNames;
	int[] eventRoleNumbers;
	int[] eventRoleSeq;
	boolean[] eventRoleKey;
	DefaultTableModel roleModel;

	Object[] eventTypeTransfer = new Object[10]; // Array used to transfer date to HBEventRoleManager
	Object[] eventRoleTransfer = new Object[10]; // Array used to transfer role to HBEventRoleManager
	Object objTempRoleData[] = new Object[4];   // to temporarily hold a row of roles when moving rows up/down

	int origGUISeq, newGUISeq; // for use when moving roles up/down

    JButton btn_Save;

/**
 * String getClassName()
 * @return className
 */
    public String getClassName() {
    	return className;
    }

/**
 * HG0551DefineEvent
 * @param pointEventRoleManager
 * @param pointOpenProject
 * @param addNewEventType (yes = add a new event)
 * @throws HBException
 * @wbp.parser.constructor
 */
    public HG0551DefineEvent(HBEventRoleManager pointEventRoleManager, HBProjectOpenData pointOpenProject,
    									boolean addNewEventType) throws HBException {
    	selectedEventName = "";		//$NON-NLS-1$
    	this.pointOpenProject = pointOpenProject;
    	this.pointEventRoleManager = pointEventRoleManager;
    	pointPersonHandler = pointOpenProject.getPersonHandler();
		pointWhereWhenHandler = pointOpenProject.getWhereWhenHandler();
    	constructDefineEvent(addNewEventType, true);
        dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
    }

/**
 * HG0551DefineEvent
 * @param pointEventRoleManager
 * @param pointOpenProject
 * @param addNewEventType (no = edit of selectedEventNumber)
 * @param selectedEventNumber
 * @throws HBException
 */
	public HG0551DefineEvent(HBEventRoleManager pointEventRoleManager, HBProjectOpenData pointOpenProject,
					boolean addNewEventType, int selectedEventNumber) throws HBException {
		this.selectedEventNumber = selectedEventNumber;
		this.pointOpenProject = pointOpenProject;
		this.pointEventRoleManager = pointEventRoleManager;
		pointPersonHandler = pointOpenProject.getPersonHandler();
		pointWhereWhenHandler = pointOpenProject.getWhereWhenHandler();
		constructDefineEvent(addNewEventType, true);
        dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
	}

/**
 * constructDefineEvent() - Construct the dialog screen
 * @param addNewEventType - boolean, true for add event, false for edit event
 * @param initiate - boolean, true for 1st entry only; false if reloading for language change
 * @throws HBException
 */
	private void constructDefineEvent(boolean addNewEventType, boolean initiate) throws HBException	{
		windowID = screenID;
		helpName = "defineevent";	//$NON-NLS-1$
    	className = getClass().getSimpleName();
    	this.setResizable(false);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: entering HG0551DefineEvent");}	//$NON-NLS-1$

	// Reset all general variables
		abbrev = "";		//$NON-NLS-1$
		pasttense = "";		//$NON-NLS-1$
		eventHint = ""; 	//$NON-NLS-1$
		gedComTag = "";		//$NON-NLS-1$
		roleSex = "U";		//$NON-NLS-1$
		minAssoc = 1;
		maxYear = 0;
		minYear = 0;
		roleMinAge = 0;
		roleMaxAge = 0;
		eventGroup = 0;
		eventRoleSequence = 1;

	// Define formatter for formatted textField inputs
	    NumberFormat format = NumberFormat.getInstance();
	    NumberFormatter formatter = new NumberFormatter(format);
	    formatter.setValueClass(Integer.class);
	    format.setGroupingUsed(false); // no commas allowed
	    formatter.setMinimum(1);
	    formatter.setMaximum(9999);

	// Set up event groups map
		eventGroups = pointEventRoleManager.geteventGroupNames(); // like Birth, arriage, Death, Burial, etc
		eventGroupNumbers = pointEventRoleManager.geteventGroupNumbers(); // their group numbers
		for (int i = 0; i < eventGroups.length; i++)
							groupIndexMap.put(eventGroups[i], eventGroupNumbers[i]);

		if(!addNewEventType) {
			eventNamesInGroup = pointEventRoleManager.getEventTypeList(0); // List of Event Names in group
			eventNumbersInGroup = pointEventRoleManager.getEventTypes();  // their event numbers
		// Get the roleNames, roleNumbers, roleSeq and whether role is Prim (Key)
			eventRoleNames = pointEventRoleManager.getEventRoleNames(selectedEventNumber, "");	//$NON-NLS-1$
			eventRoleNumbers = pointEventRoleManager.getEventRoleNumbers();
			eventRoleSeq = pointEventRoleManager.getEventRoleSeq();
			eventRoleKey = pointEventRoleManager.getEventRoleKey();
		// Find event type name for selected event
			for (int i = 0; i < eventNumbersInGroup.length; i++)
				if (eventNumbersInGroup[i] == selectedEventNumber) selectedEventName = eventNamesInGroup[i];
		// Populate role table for selected event (although we only display 1st 2 values)
			tableRoleData = new Object[eventRoleNames.length][4];
			for (int i = 0; i < eventRoleNames.length; i++)  {
				tableRoleData[i][0] = eventRoleKey[i];
				tableRoleData[i][1] = eventRoleNames[i].trim();
				tableRoleData[i][2] = eventRoleNumbers[i];
				tableRoleData[i][3] = eventRoleSeq[i];
			}
		// and sort on its seq#
			Arrays.sort(tableRoleData, (o1, o2) -> Integer.compare((Integer) o1[3], (Integer) o2[3]));
		} else tableRoleData = new Object[1][4];

		if (addNewEventType) {
			selectedEventName = HG0551Msgs.Text_3;	// New
			setTitle(HG0551Msgs.Text_4 + selectedEventName + HG0551Msgs.Text_5); // Define .... Event
		}
		else {
			for (int i = 0; i < eventNumbersInGroup.length; i++)
				if (selectedEventNumber == eventNumbersInGroup[i])
					selectedEventName = eventNamesInGroup[i];
			setTitle(HG0551Msgs.Text_6 + selectedEventName + HG0551Msgs.Text_7);  // Update ... Event
		}

	// Set up data in HBEventRoleManager
		pointEventRoleManager.prepareNewEventType(addNewEventType);

/***********************************
 * Setup main panel and top row
 ***********************************/
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]10[]", "[]10[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    	JToolBar toolBar = new JToolBar();
    	toolBar.setFloatable(false);
    	toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
    	toolBar.add(Box.createHorizontalGlue());
    // Add HG0450 icons
		toolBar.add(btn_Remindericon);
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north");	//$NON-NLS-1$

		JLabel lbl_EventTypeName = new JLabel(selectedEventName);
		lbl_EventTypeName.setFont(lbl_EventTypeName.getFont().deriveFont(lbl_EventTypeName.getFont().getStyle() | Font.BOLD));
		contents.add(lbl_EventTypeName, "cell 0 0");	//$NON-NLS-1$

		JLabel lbl_Language = new JLabel(HG0551Msgs.Text_8);	// Language:
		contents.add(lbl_Language, "cell 1 0, alignx left, gap 100");		//$NON-NLS-1$
		if (initiate) {
			comboLanguage = new JComboBox<String>();
			// Load the data language names and set to current Global dataLanguage
			for (int i=0; i < HG0501AppSettings.dataReptLanguages.length; i++)
					comboLanguage.addItem(HG0501AppSettings.dataReptLanguages[i]);
			if (HGlobal.numOpenProjects > 0)
				for (int i = 0; i < HG0501AppSettings.dataReptLangCodes.length; i++)
					if (HGlobal.dataLanguage.equals(HG0501AppSettings.dataReptLangCodes[i])) {
						comboLanguage.setSelectedIndex(i);
						selectedLangCode = 	HG0501AppSettings.dataReptLangCodes[i];
						languageIndexDefault = i;
						pointEventRoleManager.setSelectedLanguage(selectedLangCode);
						prevSelectedLangIndex = comboLanguage.getSelectedIndex();
					}
		}
		contents.add(comboLanguage, "cell 1 0");	//$NON-NLS-1$

/***********************************
 * Define the tabbed Pane structure
 **********************************/
		JTabbedPane tabPane = new JTabbedPane(JTabbedPane.TOP);
		tabPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
		tabPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		contents.add(tabPane, "cell 0 1 2"); //$NON-NLS-1$

/***********************************************
 * Setup General Settings Panel and its contents
 ***********************************************/
		JPanel settingPanel = new JPanel();
	    tabPane.addTab(HG0551Msgs.Text_9, null, settingPanel);	// General Event Settings
		settingPanel.setLayout(new MigLayout("insets 5", "[]50[]", "10[]30[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	// Setup sub-panel for Event Name, etc
		JPanel namePanel = new JPanel();
		namePanel.setBorder(BorderFactory.createTitledBorder (HG0551Msgs.Text_10));	// Event Name and Group
		namePanel.setLayout(new MigLayout("insets 5", "[]10[]", "[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_EventName = new JLabel(HG0551Msgs.Text_11);		// Event Name:
		namePanel.add(lbl_EventName, "cell 0 0, alignx right");	//$NON-NLS-1$

		text_EventName = new JTextField();
		text_EventName.setPreferredSize(new Dimension(150, 20));
		text_EventName.setText(selectedEventName);
		namePanel.add(text_EventName, "cell 1 0, alignx left, aligny top");	//$NON-NLS-1$

		JLabel lbl_EventGrp = new JLabel(HG0551Msgs.Text_12);		// Event Group:
		namePanel.add(lbl_EventGrp, "cell 0 1, alignx right");	//$NON-NLS-1$

		comboGroups = new JComboBox<String>();
		for (int i = 0; i < eventGroups.length; i++) comboGroups.addItem(eventGroups[i]);
		namePanel.add(comboGroups, "cell 1 1");	//$NON-NLS-1$
		settingPanel.add(namePanel, "cell 0 0");	//$NON-NLS-1$

	// Setup sub-panel for year validity
		JPanel validPanel = new JPanel();
		validPanel.setBorder(BorderFactory.createTitledBorder (HG0551Msgs.Text_13));	// Event Validity - Associates and Years
		validPanel.setLayout(new MigLayout("insets 5", "[]10[]", "[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_MinKeyAssoc = new JLabel(HG0551Msgs.Text_14);		// Minimum Primary Associates:
		validPanel.add(lbl_MinKeyAssoc, "cell 0 0, alignx right");	//$NON-NLS-1$

		text_minAssoc = new JFormattedTextField(formatter);
		text_minAssoc.setHorizontalAlignment(SwingConstants.CENTER);
		text_minAssoc.setPreferredSize(new Dimension(40, 20));
		validPanel.add(text_minAssoc, "cell 1 0, alignx left");	//$NON-NLS-1$

		JLabel lbl_minYear = new JLabel(HG0551Msgs.Text_15);		// Minimum Valid Year:
		validPanel.add(lbl_minYear, "cell 0 1, alignx right");	//$NON-NLS-1$

		text_minYear = new JFormattedTextField(formatter);
		text_minYear.setHorizontalAlignment(SwingConstants.CENTER);
		text_minYear.setPreferredSize(new Dimension(40, 20));
		validPanel.add(text_minYear, "cell 1 1, alignx left");	//$NON-NLS-1$

		JLabel lbl_maxYear = new JLabel(HG0551Msgs.Text_16);		// Maximum Valid Year:
		validPanel.add(lbl_maxYear, "cell 0 2, alignx right");	//$NON-NLS-1$

		text_maxYear = new JFormattedTextField(formatter);
		text_maxYear.setHorizontalAlignment(SwingConstants.CENTER);
		text_maxYear.setPreferredSize(new Dimension(40, 20));
		validPanel.add(text_maxYear, "cell 1 2, alignx left");	//$NON-NLS-1$
		settingPanel.add(validPanel, "cell 0 1, growx");	//$NON-NLS-1$

	// Setup sub-panel for GEDCOM info
		JPanel gedPanel = new JPanel();
		gedPanel.setBorder(BorderFactory.createTitledBorder (HG0551Msgs.Text_17));		// Event GEDCOM Settings
		gedPanel.setLayout(new MigLayout("insets 5", "[]10[]", "[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_gedcom = new JLabel(HG0551Msgs.Text_18);		// Export to GEDCOM as:
		gedPanel.add(lbl_gedcom, "cell 0 0, alignx right");	//$NON-NLS-1$

		radio_Tag = new JRadioButton(HG0551Msgs.Text_19);		// Tag:
		radio_Tag.setSelected(true);
		gedPanel.add(radio_Tag, "cell 1 0, alignx left");	//$NON-NLS-1$

		text_gedTag = new JTextField();		// Filter restricts this field to 6-char, upper-case
		((AbstractDocument) text_gedTag.getDocument()).setDocumentFilter(new UppercaseLengthFilter(6));
		text_gedTag.setPreferredSize(new Dimension(100, 20));
		gedPanel.add(text_gedTag, "cell 1 0");	//$NON-NLS-1$

		radio_Even = new JRadioButton();
		radio_Even.setText("1 EVEN, 2 TYPE " + text_EventName.getText().toUpperCase());	//$NON-NLS-1$
		gedPanel.add(radio_Even, "cell 1 1, alignx left, grow");		//$NON-NLS-1$
		settingPanel.add(gedPanel, "cell 1 0");	//$NON-NLS-1$

		ButtonGroup radioGed = new ButtonGroup();
		radioGed.add(radio_Tag);
		radioGed.add(radio_Even);

/************************************************
 * Setup Role/Sentence Panel and 3 content panels
 ************************************************/
		JPanel roleSentPanel = new JPanel();
	    tabPane.addTab(HG0551Msgs.Text_20, null, roleSentPanel);	// Roles and Sentences
		roleSentPanel.setLayout(new MigLayout("insets 5", "[]10[]", "[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// Setup sub-panel for role table
		JPanel rolePanel = new JPanel();
		rolePanel.setBorder(BorderFactory.createTitledBorder (HG0551Msgs.Text_21));	// Roles of this Event
		rolePanel.setLayout(new MigLayout("insets 5", "[]10[]", "[]50[]10[]10[]10[]30[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JButton btn_Add = new JButton(HG0551Msgs.Text_22);		// Add New Role
		btn_Add.setEnabled(true);
		rolePanel.add(btn_Add, "cell 0 2, alignx left, growx");	//$NON-NLS-1$

		JButton btn_Delete = new JButton(HG0551Msgs.Text_23);	// Delete Role
		btn_Delete.setEnabled(true);
		rolePanel.add(btn_Delete, "cell 0 3, alignx left, growx");	//$NON-NLS-1$

		JButton btn_Copy = new JButton(HG0551Msgs.Text_24);		// Copy Role
		btn_Copy.setEnabled(true);
		rolePanel.add(btn_Copy, "cell 0 4, alignx left, growx");	//$NON-NLS-1$

		JButton btn_MoveUp = new JButton(HG0551Msgs.Text_25);		// Move Up
		btn_MoveUp.setEnabled(true);
		rolePanel.add(btn_MoveUp, "cell 0 5, alignx left, growx");		//$NON-NLS-1$

		JButton btn_MoveDown = new JButton(HG0551Msgs.Text_26);		// Move Down
		btn_MoveDown.setEnabled(true);
		rolePanel.add(btn_MoveDown, "cell 0 6, alignx left, aligny top, growx");		//$NON-NLS-1$

	// Get the table header data from T204
		tableRoleHeader = pointPersonHandler.setTranslatedData("55100", "1", false); // Primary, Role //$NON-NLS-1$ //$NON-NLS-2$
	// Create scrollpane and table for the Rolenames with Primary yes/no checkbox in column 0
	// and roleName in col 1. roleName cannot be editable or sentences will fail!
		roleModel = new DefaultTableModel(tableRoleData, tableRoleHeader);
		tableRole = new JTable(roleModel) {
			private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int row, int col) {
					if (col == 1) return false;	// Name not editable!
					return true;
				}
				public Class<?> getColumnClass(int col) {
				    if (col == 0) return Boolean.class;
				    return String.class;
				}
		};
		// Define table
		tableRole.getColumnModel().getColumn(0).setMinWidth(50);
		tableRole.getColumnModel().getColumn(0).setPreferredWidth(50);
		tableRole.getColumnModel().getColumn(1).setMinWidth(80);
		tableRole.getColumnModel().getColumn(1).setPreferredWidth(180);
		tableRole.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

	// Set header and selection mode
		JTableHeader roleHeader = tableRole.getTableHeader();
		roleHeader.setOpaque(false);
		ListSelectionModel roleSelectionModel = tableRole.getSelectionModel();
		roleSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

	// Setup scrollpane and add to Role panel
		tableRole.setFillsViewportHeight(true);
		JScrollPane roleScrollPane = new JScrollPane(tableRole);
		tableRole.setPreferredScrollableViewportSize(new Dimension(240, 300));

		rolePanel.add(roleScrollPane, "cell 1 0 1 7, growx");	//$NON-NLS-1$
		roleSentPanel.add(rolePanel, "cell 0 0 1 2, growx");	//$NON-NLS-1$

	// Setup sub-panel for sentences
		JPanel sentPanel = new JPanel();
		sentPanel.setBorder(BorderFactory.createTitledBorder (HG0551Msgs.Text_27));	// Sentences for the Selected Role
		sentPanel.setLayout(new MigLayout("insets 5", "[]", "[]5[]10[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_MaleSent = new JLabel(HG0551Msgs.Text_28);	// Male Sentence Structure:
		sentPanel.add(lbl_MaleSent, "cell 0 0, alignx left");	//$NON-NLS-1$

		text_MaleSent = new JTextArea();
		text_MaleSent.setWrapStyleWord(true);
		text_MaleSent.setLineWrap(true);
		((DefaultCaret)text_MaleSent.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		text_MaleSent.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		text_MaleSent.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		text_MaleSent.setBorder(new JTable().getBorder());		// match Table border
		// Setup scrollpane with textarea
		JScrollPane text_MaleSentScroll = new JScrollPane(text_MaleSent);
		text_MaleSentScroll.setMinimumSize(new Dimension(300, 70));
		text_MaleSentScroll.getViewport().setOpaque(false);
		text_MaleSentScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		text_MaleSent.setCaretPosition(0);	// set scrollbar to top
		sentPanel.add(text_MaleSentScroll, "cell 0 1, alignx left, aligny top");	//$NON-NLS-1$

		JLabel lbl_FemaleSent = new JLabel(HG0551Msgs.Text_29);	// Female Sentence Structure (if different):
		sentPanel.add(lbl_FemaleSent, "cell 0 2, alignx left");	//$NON-NLS-1$

		text_FemaleSent = new JTextArea();
		text_FemaleSent.setWrapStyleWord(true);
		text_FemaleSent.setLineWrap(true);
		((DefaultCaret)text_FemaleSent.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		text_FemaleSent.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		text_FemaleSent.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		text_FemaleSent.setBorder(new JTable().getBorder());		// match Table border
		// Setup scrollpane with textarea
		JScrollPane text_FemaleSentScroll = new JScrollPane(text_FemaleSent);
		text_FemaleSentScroll.setMinimumSize(new Dimension(300, 70));
		text_FemaleSentScroll.getViewport().setOpaque(false);
		text_FemaleSentScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		text_FemaleSent.setCaretPosition(0);	// set scrollbar to top
		sentPanel.add(text_FemaleSentScroll, "cell 0 3, alignx left, aligny top");	//$NON-NLS-1$

		roleSentPanel.add(sentPanel, "cell 1 0");	//$NON-NLS-1$

	// Setup sub-panel for role settings
		JPanel validityPanel = new JPanel();
		validityPanel.setBorder(BorderFactory.createTitledBorder (HG0551Msgs.Text_30));	// Selected Role is valid for:
		validityPanel.setLayout(new MigLayout("insets 5", "[]5[]", "[]10[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_Sex = new JLabel(HG0551Msgs.Text_31);		// Birth Sex:
		validityPanel.add(lbl_Sex, "cell 0 0, alignx right");	//$NON-NLS-1$

		comboSex = new JComboBox<String>();
		comboSex.addItem(sexOptions[0]);
		comboSex.addItem(sexOptions[1]);
		comboSex.addItem(sexOptions[2]);
		validityPanel.add(comboSex, "cell 1 0, alignx left");	//$NON-NLS-1$

		JLabel lbl_minAge = new JLabel(HG0551Msgs.Text_32);		// Minimum Age:
		validityPanel.add(lbl_minAge, "cell 0 1, alignx right");	//$NON-NLS-1$

		text_minAge = new JFormattedTextField(formatter);
		text_minAge.setHorizontalAlignment(SwingConstants.CENTER);
		text_minAge.setPreferredSize(new Dimension(30, 20));
		validityPanel.add(text_minAge, "cell 1 1, alignx left");	//$NON-NLS-1$

		JLabel lbl_maxAge = new JLabel(HG0551Msgs.Text_33);		// Maximum Age:
		validityPanel.add(lbl_maxAge, "cell 0 2, alignx right");	//$NON-NLS-1$

		text_maxAge = new JFormattedTextField(formatter);
		text_maxAge.setHorizontalAlignment(SwingConstants.CENTER);
		text_maxAge.setPreferredSize(new Dimension(30, 20));
		validityPanel.add(text_maxAge, "cell 1 2, alignx left");	//$NON-NLS-1$

		roleSentPanel.add(validityPanel, "cell 1 1");	//$NON-NLS-1$

/***********************************************
 * Setup Hint Panel and its contents
 ***********************************************/
		JPanel hintPanel = new JPanel();
	    tabPane.addTab(HG0551Msgs.Text_34, null, hintPanel);		// Event Hints
		hintPanel.setLayout(new MigLayout("insets 10", "[]", "[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	// Setup sub-panel for hints text area
		JPanel hint2Panel = new JPanel();
		hint2Panel.setBorder(BorderFactory.createTitledBorder (HG0551Msgs.Text_35));	// Your Hints for Usage of this Event
		hint2Panel.setLayout(new MigLayout("", "[]", "[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		hintText = new JTextArea();
		hintText.setWrapStyleWord(true);
		hintText.setLineWrap(true);
		hintText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		hintText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)hintText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		hintText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		hintText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		hintText.setBorder(new JTable().getBorder());		// match Table border
		JScrollPane hintTextScroll = new JScrollPane(hintText);
		hintTextScroll.setPreferredSize(new Dimension(625, 300));
		hintTextScroll.getViewport().setOpaque(false);
		hintTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		hintText.setCaretPosition(0);	// set scrollbar to top
		hint2Panel.add(hintTextScroll, "cell 0 0");	//$NON-NLS-1$

		hintPanel.add(hint2Panel, "cell 0 0");	//$NON-NLS-1$

/*********************************
 * Setup control buttons at bottom
 *********************************/
		btn_Save = new JButton(HG0551Msgs.Text_36);		// Save
		btn_Save.setToolTipText(HG0551Msgs.Text_37);	// Save the edited Event type
		contents.add(btn_Save, "cell 1 2, align right, gapx 20, tag ok"); //$NON-NLS-1$

		JButton btn_Close = new JButton(HG0551Msgs.Text_38);	// Close
		btn_Close.setToolTipText(HG0551Msgs.Text_39);			// Close and Exit
		contents.add(btn_Close, "cell 1 2, align right, gapx 20, tag cancel"); //$NON-NLS-1$

/***************************************************
 * Load data if this is EXISTING event being edited
 ***************************************************/
	// If Editing Existing Event, load Event data into GUI from database
		if (!addNewEventType) {
			try {
				getEventTypeData(pointEventRoleManager.getEventTypeTransfer(selectedEventNumber), addNewEventType);
			} catch (HBException hbe) {
				// Disable any save action
				btn_Save.setEnabled(false);
				// If error, issue msg and exit
				if (hbe.getMessage().startsWith("ERR1")) {		//$NON-NLS-1$
					JOptionPane.showMessageDialog(comboLanguage,
							HG0551Msgs.Text_40 						// Event does not exist in the
							+ comboLanguage.getSelectedItem()
							+ HG0551Msgs.Text_41,					// language
							HG0551Msgs.Text_42,						// No Event
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
		// If Event data load was OK, load first role's data and sentences
			// Load sex/age role data
			selectedRowInRoleTable = 0;
     		int eventRoleNum = (int) tableRoleData[selectedRowInRoleTable][2];
    		getEventRoleData(pointEventRoleManager.getEventRoleTransfer(selectedEventNumber,
    														eventRoleNum), addNewEventType);
			// Load sentences, convert roleNumbers to roleNames and load into the 2 text areas
			convertSentRoleNumToNames(eventRoleNum);
			// Set 1st row of role table as selected
			tableRole.setRowSelectionInterval(0, 0);
			tableRole.scrollRectToVisible(tableRole.getCellRect(0, 0, true));
		}

	// Display the screen
		pack();

	// Check if this is a GUI reload that should start with
	// the Role/Sentece tab showing
		if (startAtSentenceTab) {
			tabPane.setSelectedIndex(1);
		    tabPane.requestFocusInWindow();
			startAtSentenceTab = false;
		}

	// Disable all change flags, as role data load will have turned them on
		eventChanged = false;
		roleChanged = false;
		btn_Save.setEnabled(false);

/******************
 * ACTION LISTENERS
 ******************/
		// Listener for clicking 'X' on screen
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e) {
	    		 btn_Close.doClick(); // returns to main menu
		    }
		});

		// Listener for Close button
		btn_Close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// Check for unsaved changes before exit
				if (btn_Save.isEnabled())
					if (JOptionPane.showConfirmDialog (btn_Save,
							HG0551Msgs.Text_43		// There are unsaved changes. \n
							+ HG0551Msgs.Text_44,	// Do you still wish to exit?
							HG0551Msgs.Text_45,		// Define Event
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						// YES option
						if (HGlobal.writeLogs)
							HB0711Logging.logWrite("Action: closing without saving from HG0551DefineEvent"); //$NON-NLS-1$
						// Clear Reminder if present
						if (reminderDisplay != null) reminderDisplay.dispose();
						dispose();
						}
						else; // NO option - do nothing
				else { // btn_Save not enabled - exit
					if (HGlobal.writeLogs)
						HB0711Logging.logWrite("Action: closing HG0551DefineEvent"); //$NON-NLS-1$
					// Clear Reminder if present
					if (reminderDisplay != null) reminderDisplay.dispose();
					dispose();
				}
			}
		});

		// Lstener for selection of the Role/Sentence tabbed pane
		// Required so that tableRole gets focus, otherwise the up/down
		// buttons don't work until the table is manually selected.
		tabPane.addChangeListener(e -> {		// using unnamed lambda
			if (tabPane.getSelectedIndex() == 1) tableRole.requestFocusInWindow();
		});

		// Listner for switching of primary Role flag on/off
		roleModel.addTableModelListener(new TableModelListener() {
			@Override
		    public void tableChanged(TableModelEvent e) {
		        if (e.getType() == TableModelEvent.UPDATE) {
		            if (e.getColumn() == 0) { // Check for change in column 0
		            	roleChanged = true;
						btn_Save.setEnabled(true);
		            }
		        }
		    }
		});

		// Language combobox - listener for item change
		comboLangListener = new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					// Check for unsaved changes before changing languages
					if (btn_Save.isEnabled())
						if (JOptionPane.showConfirmDialog (btn_Save,
								HG0551Msgs.Text_43		// There are unsaved changes. \n
								+ HG0551Msgs.Text_47,	// Do you still wish to change language?
								HG0551Msgs.Text_45,		// Define Event
								JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
							// NO option - temp remove the listener
							comboLanguage.removeItemListener(comboLangListener);
							// Change the language back to what it was
							comboLanguage.setSelectedIndex(prevSelectedLangIndex);
							// Re-instate the listener and do nothing else
							comboLanguage.addItemListener(comboLangListener);
							return;
						}
					// YES option - set the selected languge
					if (comboLanguage.getSelectedIndex() != -1) {
						try {
							// Get and save the newly selected index
							newSelectedLangIndex = comboLanguage.getSelectedIndex();
							prevSelectedLangIndex = newSelectedLangIndex;
							selectedLangCode = 	HG0501AppSettings.dataReptLangCodes[newSelectedLangIndex];
							pointEventRoleManager.setSelectedLanguage(selectedLangCode);
							constructDefineEvent(addNewEventType, false);
						} catch (HBException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		};
		comboLanguage.addItemListener(comboLangListener);

		// Sex combobox listener
		comboSexListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				roleChanged = true;
				btn_Save.setEnabled(true);
			}
		};
		comboSex.addActionListener(comboSexListener);

		// Group combobox listener
		comboGroups.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				eventChanged = true;
				btn_Save.setEnabled(true);
			}
		});

		// Listener for Add role button
		btn_Add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// If a role change has not been saved, issue warning
				if (roleChanged) {
					JOptionPane.showMessageDialog(comboLanguage,
	    					HG0551Msgs.Text_49 +	// There is unsaved Event data. Please save \n
	    					HG0551Msgs.Text_50,		// all changes before adding a new Role
	    					HG0551Msgs.Text_51,		// Save Event Data
	    					JOptionPane.WARNING_MESSAGE);
					return;
				}
				// Prompt for new Role name
				selectedRoleName = JOptionPane.showInputDialog(HG0551Msgs.Text_52); 	// Enter new Role name:
				// Create new roleSeq, 1 > highest existing roleSeq
				int newRoleSeq = Arrays.stream(eventRoleSeq).max().getAsInt() + 1;
				// Add the new role entry into T461 table
				if (selectedRoleName != null) {
					try {
						pointEventRoleManager.addNewEventRole(selectedRoleName.trim(), newRoleSeq);
				// and rebuild the screen, showing role/sentence tab at start
						startAtSentenceTab = true;
						constructDefineEvent(addNewEventType, false);
					} catch (HBException e) {
						e.printStackTrace();
					}
					// Select last (new) role and auto-scroll to show it
					selectedRowInRoleTable = tableRole.convertRowIndexToView(roleModel.getRowCount() - 1);
					tableRole.setRowSelectionInterval(selectedRowInRoleTable, selectedRowInRoleTable);
					tableRole.scrollRectToVisible(new Rectangle(tableRole.getCellRect(selectedRowInRoleTable, 0, true)));
					// Load this role's data
		     		int eventRoleNum = (int) tableRoleData[selectedRowInRoleTable][2];
		    		getEventRoleData(pointEventRoleManager.getEventRoleTransfer(selectedEventNumber,
		    														eventRoleNum), addNewEventType);
		    		// Reload sentences, convert roleNumbers to roleNames and load into the 2 text areas
					convertSentRoleNumToNames(eventRoleNum);
					roleChanged = false;
					btn_Save.setEnabled(false);
				}
				return;	// if no name entered
			}
		});

		// Listener for Copy role button
		btn_Copy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// NOTE05 need code here to copy then Edit
			}
		});

		// Listener for Delete role button
		btn_Delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// NOTE05 need code here to delete the Role but ONLY IF NOT IN USE IN PROJECT!
			}
		});

		// Listener for Move role Up button
		btn_MoveUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				selectedRowInRoleTable = tableRole.getSelectedRow();
				origGUISeq = (int) tableRoleData[selectedRowInRoleTable][3];
				// only allow move up if not at top of table
				if (selectedRowInRoleTable >= 1) {
				// Save GUI seq settings
					origGUISeq = (int) tableRoleData[selectedRowInRoleTable][3];
					newGUISeq = (int) tableRoleData[selectedRowInRoleTable - 1][3];
				// Switch rows in roleModel
					roleModel.moveRow(selectedRowInRoleTable, selectedRowInRoleTable, selectedRowInRoleTable-1);
				// Switch rows in underlying tableRoleData
					objTempRoleData = tableRoleData[selectedRowInRoleTable-1];
					tableRoleData[selectedRowInRoleTable-1] = tableRoleData[selectedRowInRoleTable];
					tableRoleData[selectedRowInRoleTable] = objTempRoleData;
				// Reset GUI Seq settings in the moved rows
					tableRoleData[selectedRowInRoleTable - 1][3] = newGUISeq;
					tableRoleData[selectedRowInRoleTable][3] = origGUISeq;
				//  Reset visible selected row and row pointers
				    tableRole.setRowSelectionInterval(selectedRowInRoleTable - 1, selectedRowInRoleTable - 1);
					prevSelectedRowInRoleTable = selectedRowInRoleTable + 1;
					selectedRowInRoleTable = tableRole.getSelectedRow();
					// Update both role record's Sequence numbers in T461 records -
					// pass across the event# and each role#+seq# pair
					try {
						pointEventRoleManager.updateEventRoleSequences(selectedEventNumber,
								(int) tableRoleData[selectedRowInRoleTable][2],
								(int) tableRoleData[selectedRowInRoleTable][3],
								(int) tableRoleData[prevSelectedRowInRoleTable][2],
								(int) tableRoleData[prevSelectedRowInRoleTable][3] );
					} catch (HBException e) {
						e.printStackTrace();
					}
				// Reload sex/age data for the new selection
		     		int eventRoleNum = (int) tableRoleData[selectedRowInRoleTable][2];
		    		getEventRoleData(pointEventRoleManager.getEventRoleTransfer(selectedEventNumber,
		    														eventRoleNum), addNewEventType);
				// Reload sentences, convert roleNumbers to roleNames and load into the 2 text areas
					convertSentRoleNumToNames(eventRoleNum);
					roleChanged = false;	// as we've done the T461 save already
			   	// and if there are no Event changes pending, we can turn off btn_Save
		    			if (!eventChanged) btn_Save.setEnabled(false);
				}
			}
		});

		// Listener for Move role Down button
		btn_MoveDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int tableSize = tableRole.getRowCount();
				selectedRowInRoleTable = tableRole.getSelectedRow();
				// only allow move down if not at end of table
				if (selectedRowInRoleTable < tableSize-1) {
				// Save GUI seq settings
					origGUISeq = (int) tableRoleData[selectedRowInRoleTable][3];
					newGUISeq = (int) tableRoleData[selectedRowInRoleTable + 1][3];
				// Switch rows in roleModel
					roleModel.moveRow(selectedRowInRoleTable, selectedRowInRoleTable, selectedRowInRoleTable+1);
				// Switch rows in underlying objEventCiteData
					objTempRoleData = tableRoleData[selectedRowInRoleTable];
					tableRoleData[selectedRowInRoleTable] = tableRoleData[selectedRowInRoleTable+1];
					tableRoleData[selectedRowInRoleTable+1] = objTempRoleData;
				// Reset GUI Seq settings in the moved rows
					tableRoleData[selectedRowInRoleTable + 1][3] = newGUISeq;
					tableRoleData[selectedRowInRoleTable][3] = origGUISeq;
				//  Reset visible selected row and row pointers
					tableRole.setRowSelectionInterval(selectedRowInRoleTable + 1, selectedRowInRoleTable + 1);
					prevSelectedRowInRoleTable = selectedRowInRoleTable - 1;
					selectedRowInRoleTable = tableRole.getSelectedRow();
				// Update both role record's Sequence numbers in T461 records -
				// pass across the event# and each role#+seq# pair
					try {
						pointEventRoleManager.updateEventRoleSequences(selectedEventNumber,
								(int) tableRoleData[selectedRowInRoleTable][2],
								(int) tableRoleData[selectedRowInRoleTable][3],
								(int) tableRoleData[prevSelectedRowInRoleTable][2],
								(int) tableRoleData[prevSelectedRowInRoleTable][3] );
					} catch (HBException e) {
						e.printStackTrace();
					}
				// Reload sex/age data for the new selection
		     		int eventRoleNum = (int) tableRoleData[selectedRowInRoleTable][2];
		    		getEventRoleData(pointEventRoleManager.getEventRoleTransfer(selectedEventNumber,
		    														eventRoleNum), addNewEventType);
				// Reload sentences, convert roleNumbers to roleNames and load into the 2 text areas
					convertSentRoleNumToNames(eventRoleNum);
					roleChanged = false;	// as we've done the T461 save already
		   		// and if there are no Event changes pending, we can turn off btn_Save
	    			if (!eventChanged) btn_Save.setEnabled(false);
				}
			}
		});

		// Listener for Role table selection by mouse click or use of up/down keys
		tableRole.getSelectionModel().addListSelectionListener(e -> {
		    // Ignore extra events (e.g. while adjusting the selection)
		    if (!e.getValueIsAdjusting() && tableRole.getSelectedRow() != -1) {
		    	// If notSavingRole flag is on, return immediately
		    	if (notSavingRole) {
		    		notSavingRole = false;
		    		return;
		    	}
		    	selectedRowInRoleTable = tableRole.getSelectedRow();
		    	// Check if roleChanged true - means unsaved changes
           		if (roleChanged) {
	           		if (JOptionPane.showConfirmDialog (btn_Save,
							HG0551Msgs.Text_43		// There are unsaved changes. \n
							+ HG0551Msgs.Text_54,	// Do you still wish to change Role?
							HG0551Msgs.Text_55,		// Role Change
							JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
							// NO option - reset back to role we left
		           			notSavingRole = true;
							// Change the selected role back to what it was - this will
		           			//trigger this listener again, but notSavingRole flag setting
		           			// will mean listener is bypassed
							tableRole.setRowSelectionInterval(prevSelectedRowInRoleTable, prevSelectedRowInRoleTable);
							return;
	           			}
           			}
           		//  roleChanged not true OR YES, we wish to change role, so make the change
           		prevSelectedRowInRoleTable = selectedRowInRoleTable;
           		int eventRoleNum = (int) tableRoleData[selectedRowInRoleTable][2];
        		getEventRoleData(pointEventRoleManager.getEventRoleTransfer(selectedEventNumber,
        														eventRoleNum), addNewEventType);
    			// and load sentence, convert roleNumbers to roleNames and load into the 2 text areas
    			convertSentRoleNumToNames(eventRoleNum);
    			// Turn off roleChanged flag
    			roleChanged = false;
    			// and if there are no Event changes, we can turn off btn_Save
    			if (!eventChanged) btn_Save.setEnabled(false);
		    }
		});

		// Listener any role textarea edit
		roleTextListen = new DocumentListener() {
	        @Override
	        public void insertUpdate(DocumentEvent e) {updateFieldState();}
	        @Override
	        public void removeUpdate(DocumentEvent e) {updateFieldState();}
	        @Override
	        public void changedUpdate(DocumentEvent e) {updateFieldState();}
	        protected void updateFieldState() {
				roleChanged = true;
				btn_Save.setEnabled(true);
	        }
	    };
	    text_MaleSent.getDocument().addDocumentListener(roleTextListen);
	    text_FemaleSent.getDocument().addDocumentListener(roleTextListen);
	    text_minAge.getDocument().addDocumentListener(roleTextListen);
	    text_maxAge.getDocument().addDocumentListener(roleTextListen);

	    // Listener for event name textField edit
		eventNameTextListen = new DocumentListener() {
	        @Override
	        public void insertUpdate(DocumentEvent e) {updateFieldState();}
	        @Override
	        public void removeUpdate(DocumentEvent e) {updateFieldState();}
	        @Override
	        public void changedUpdate(DocumentEvent e) {updateFieldState();}
	        protected void updateFieldState() {
	        	// Switch the gedcom setting if Event Name edited
				radio_Even.setSelected(true);
				// And trigger the ActionListener programmatically
				for (ActionListener al : radio_Even.getActionListeners()) {
				    al.actionPerformed(new ActionEvent(radio_Even, ActionEvent.ACTION_PERFORMED, "manual")); //$NON-NLS-1$
				}
				text_gedTag.setText("");	//$NON-NLS-1$
	        	eventChanged = true;
	        	btn_Save.setEnabled(true);
	        }
	    };
	    text_EventName.getDocument().addDocumentListener(eventNameTextListen);

	    // Listener for other event textField edits
		eventTextListen = new DocumentListener() {
	        @Override
	        public void insertUpdate(DocumentEvent e) {updateFieldState();}
	        @Override
	        public void removeUpdate(DocumentEvent e) {updateFieldState();}
	        @Override
	        public void changedUpdate(DocumentEvent e) {updateFieldState();}
	        protected void updateFieldState() {
	        	eventChanged = true;
	        	btn_Save.setEnabled(true);
	        }
	    };
	    text_minYear.getDocument().addDocumentListener(eventTextListen);
	    text_maxYear.getDocument().addDocumentListener(eventTextListen);
	    text_minAssoc.getDocument().addDocumentListener(eventTextListen);
	    hintText.getDocument().addDocumentListener(roleTextListen);

	    // Listener for radioTag selection
		ActionListener actionRadioTag = new ActionListener() {
		     public void actionPerformed(ActionEvent actionEvent) {
		    	 if (radio_Tag.isSelected()) {
		    		 eventChanged = true;
		    		 btn_Save.setEnabled(true);
		    	 }
		     }
		};
		radio_Tag.addActionListener(actionRadioTag);

		ActionListener actionRadioEven = new ActionListener() {
		     public void actionPerformed(ActionEvent e) {
		    	 if (radio_Even.isSelected()) {
		    		 radio_Even.setText("1 EVEN, 2 TYPE " + text_EventName.getText().toUpperCase());	//$NON-NLS-1$
		    	 	eventChanged = true;
		    	 	btn_Save.setEnabled(true);
		    	 }
		     }
		};
		radio_Even.addActionListener(actionRadioEven);

	// Listener for Save button - add new event type - save the data and exit
		if (addNewEventType)
			btn_Save.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					newEventName = text_EventName.getText();
					selectedGroup = groupIndexMap.get(comboGroups.getSelectedItem());
					try {
					// Tranfer data to EventRoleManager
						pointEventRoleManager.saveEventTypeData(saveEventTypeData());
						pointEventRoleManager.addNewEventType(newEventName, selectedGroup);
					} catch (HBException e) {
						e.printStackTrace();
					}
					if (reminderDisplay != null) reminderDisplay.dispose();
					if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: saved new event " + //$NON-NLS-1$
											newEventName + " in HG0551DefineEvent"); //$NON-NLS-1$
					dispose();
			}
		});

	// Listener for Save button - edit event type -save role and/or event data and exit if event saved
		if (!addNewEventType)
			btn_Save.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
				// Check if role data has changed - if so, save it
					if (roleChanged) {
						try {
							int atRow = tableRole.getSelectedRow();
							if (atRow != -1) {
				           		selectedRoleData = tableRoleData[atRow]; // select whole row
				           		selectedRoleName = ((String) selectedRoleData[1]).trim();
				           		selectedRoleNumber = (int) selectedRoleData[2];
				           		selectedKeyRole = (boolean) roleModel.getValueAt(atRow, 0);
				           		selectedRoleSeq = (int) selectedRoleData[3];
								pointEventRoleManager.editEventRole(selectedEventNumber, saveEventRoleData());
							}
						} catch (HBException e) {
							e.printStackTrace();
						}
						roleChanged = false;
						if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: saved role " + //$NON-NLS-1$
												selectedRoleName + " in HG0551DefineEvent"); //$NON-NLS-1$
						// If only role needed saving, turn off btn_Save
						if (!eventChanged) btn_Save.setEnabled(false);
					}
				// Now check if event changed and needs saving
					if (eventChanged) {
						try {
							newEventName = text_EventName.getText();
							selectedGroup = groupIndexMap.get(comboGroups.getSelectedItem());
							pointEventRoleManager.saveEventTypeData(saveEventTypeData());
							pointEventRoleManager.editEventType(newEventName, selectedEventNumber,
																selectedGroup, eventTypeTransfer);
						} catch (HBException e) {
							e.printStackTrace();
						}
						if (reminderDisplay != null) reminderDisplay.dispose();
						if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: saved event " + //$NON-NLS-1$
												newEventName + " in HG0551DefineEvent"); //$NON-NLS-1$
						dispose();
					}
				}
		});

	}	// End HG0551DefineEvent constructor

/**
 * protected void saveEventTypeData()
 */
	protected Object[] saveEventTypeData() {
		eventTypeTransfer[0] = selectedEventName.trim(); //eventTypeName;
		eventTypeTransfer[1] = newEventNumber; //newEventNumber;
		eventTypeTransfer[2] = selectedGroup; //eventGroup;
		if (radio_Tag.isSelected())
			gedComTag = text_gedTag.getText().trim();
		else gedComTag = "";		//$NON-NLS-1$
		eventTypeTransfer[3] = gedComTag;
		if (text_minYear.getText().isEmpty()) minYear = 0;
			else minYear = Integer.parseInt(text_minYear.getText());
		eventTypeTransfer[4] = minYear;
		if (text_maxYear.getText().isEmpty()) maxYear = 0;
			else maxYear = Integer.parseInt(text_maxYear.getText());
		eventTypeTransfer[5] = maxYear;
		eventTypeTransfer[6] = ""; //abbrev;	//$NON-NLS-1$
		eventTypeTransfer[7] = ""; //pasttense;	//$NON-NLS-1$
		eventHint = hintText.getText();
		eventTypeTransfer[8] = eventHint ; //reminder;
		if (text_minAssoc.getText().isEmpty()) minAssoc = 1;
		eventTypeTransfer[9] = minAssoc;
		return eventTypeTransfer;
	}

/**
 * protected void saveEventRoleData()
 */
	protected Object[] saveEventRoleData() {
		eventRoleTransfer[0] = selectedRoleNumber;
		eventRoleTransfer[1] = selectedRoleSeq;
		eventRoleTransfer[2] = selectedRoleName;
		eventRoleTransfer[3] = sexValues[comboSex.getSelectedIndex()];
		if (text_minAge.getText().isEmpty()) roleMinAge = 0;
			else roleMinAge = Integer.parseInt(text_minAge.getText());
		eventRoleTransfer[4] = roleMinAge;
		if (text_maxAge.getText().isEmpty()) roleMaxAge = 0;
			else roleMaxAge = Integer.parseInt(text_maxAge.getText());
		eventRoleTransfer[5] = roleMaxAge;
		eventRoleTransfer[6] = null;		// NOTE03 sentence PID!
		eventRoleTransfer[7] = selectedKeyRole;
		return eventRoleTransfer;
	}

/**
 * getEventTypeData(Object[] eventTypeData, boolean addNewEventType)
 * @param eventTypeTransfer
 * @param addNewEventType
 */
	private void getEventTypeData(Object[] eventTypeTransfer, boolean addNewEventType) {
		if (addNewEventType) {
			abbrev = "abr.";		//$NON-NLS-1$
			pasttense = " ";		//$NON-NLS-1$
			text_gedTag.setText("");	//$NON-NLS-1$
			text_minAssoc.setText("1");		//$NON-NLS-1$
			text_minYear.setText("0");		//$NON-NLS-1$
			text_maxYear.setText("3000");	//$NON-NLS-1$
		} else {
			for (int i = 0; i <  eventGroupNumbers.length; i++)
				if ((int)eventTypeTransfer[2] == eventGroupNumbers[i])
					comboGroups.setSelectedIndex(i);
			if (eventTypeTransfer[3] == null || eventTypeTransfer[3] == "") {				//$NON-NLS-1$
				radio_Even.setText("1 EVEN, 2 TYPE " + text_EventName.getText().toUpperCase());	//$NON-NLS-1$
				radio_Even.setSelected(true);
				text_gedTag.setText("");			//$NON-NLS-1$
			}
			else text_gedTag.setText((String) eventTypeTransfer[3]);
			text_minAssoc.setText("" + eventTypeTransfer[9]);	//$NON-NLS-1$
			text_minYear.setText("" + eventTypeTransfer[4]);	//$NON-NLS-1$
			text_maxYear.setText("" + eventTypeTransfer[5]);	//$NON-NLS-1$
			hintText.append("" + eventTypeTransfer[8]);			//$NON-NLS-1$
		}
	}

/**
 * protected void getEventRoleData(Object[] eventRoleData, boolean addNewEventType)
 * @param eventTypeTransfer
 * @param addNewEventType
 */
	private void getEventRoleData(Object[] eventRoleTransfer, boolean addNewEventType) {
		if (addNewEventType) {
			String tempSentence = "";	//$NON-NLS-1$
			if (text_FemaleSent.getText().isEmpty()) tempSentence = text_MaleSent.getText();
			else tempSentence = text_MaleSent.getText() + "$!&" + text_FemaleSent.getText(); //$NON-NLS-1$
			// Now convert any role names to role numbers for saving
			roleSentence = convertSentRoleNamesToNums(tempSentence);
			roleSex = "U";					//$NON-NLS-1$
			text_minAge.setText("0");		//$NON-NLS-1$
			text_maxAge.setText("100");		//$NON-NLS-1$
		} else {
			comboSex.setSelectedIndex(0);
			roleSex = sexValues[0];
			if (eventRoleTransfer[3] != null)
				for (int i = 0; i < 3; i++)
					if (eventRoleTransfer[3].equals(sexValues[i])) {
						comboSex.setSelectedIndex(i);
						roleSex = sexValues[i];
					}
			text_minAge.setText("" + eventRoleTransfer[4]);	//$NON-NLS-1$
			text_maxAge.setText("" + eventRoleTransfer[5]);	//$NON-NLS-1$
		}
	}

/**
 * lass UppercaseLengthFilter - restricts GEDCOM input to 6-chars, forces upper-case
 */
   static class UppercaseLengthFilter extends DocumentFilter {
        private final int maxLength;
        public UppercaseLengthFilter(int maxLength) {
            this.maxLength = maxLength;
        }
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string == null) return;
            string = string.toUpperCase();
            int newLength = fb.getDocument().getLength() + string.length();
            if (newLength <= maxLength) {
                super.insertString(fb, offset, string, attr);
            } else {
                string = string.substring(0, maxLength - fb.getDocument().getLength());
                super.insertString(fb, offset, string, attr);
            }
        }
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text == null) return;
            text = text.toUpperCase();
            int currentLength = fb.getDocument().getLength();
            int newLength = currentLength - length + text.length();
            if (newLength <= maxLength) {
                super.replace(fb, offset, length, text, attrs);
            } else {
                text = text.substring(0, maxLength - (currentLength - length));
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }		// End UppercaseLengthFilter

/**
 * convertSentRoleNumToNames - load sentence and convert role numbers to role names
 */
	public void convertSentRoleNumToNames(int roleNum) {
		// Setup fields for this routine to use
		String workSentence = "";		//$NON-NLS-1$
        String replacement = "";		//$NON-NLS-1$
        String[] sexSentences;
        // Clean out textareas
        text_MaleSent.setText("");		//$NON-NLS-1$
        text_FemaleSent.setText("");	//$NON-NLS-1$

		// Load the sentence for this event#, role#, langcode
		try {
			roleSentence = pointWhereWhenHandler.pointLibraryResultSet.selectSentenceString(selectedEventNumber,
	                													roleNum, selectedLangCode, dataBaseIndex);
		} catch (HBException | NullPointerException ex) {
				// Handle new HRE project which has no sentences
				 roleSentence = ""; //$NON-NLS-1$
				 return;
		}

		// If roleSentence has error flag, or is default en-US language, set sentence msg and return
		if (roleSentence.equals("NOSENTENCE") || roleSentence.startsWith("(en-US)")) {		//$NON-NLS-1$ //$NON-NLS-2$
			text_MaleSent.append(HG0551Msgs.Text_61);	// No sentences exist in this language for this Role
			return;
		}

		// Check if the roleSentence contains any '[R' patterns that need converting
		if (roleSentence.contains("[R")) {		//$NON-NLS-1$
			// If so, setup a regex to find role reference patterns
			Pattern pattern = Pattern.compile("(\\[R[a-zA-Z0-9]{0,4}:)(\\d{5})(])");		//$NON-NLS-1$
	        Matcher matcher = pattern.matcher(roleSentence);
	        StringBuffer result = new StringBuffer();
	        // Search for the [R: references and extract the integer role number
	        while (matcher.find()) {
	            String prefix = matcher.group(1); // e.g., "[RX:"
	            int index = Integer.parseInt(matcher.group(2)); // e.g., 00012
	            String suffix = matcher.group(3); // e.g., "]"
	            // then use 'index' to find correct roleName
	    		for (int i=0; i < eventRoleNames.length; i++) {
	    			if (index == eventRoleNumbers[i])
	    					replacement = eventRoleNames[i];
	    		}
	   		// insert the role name and look for another
	            matcher.appendReplacement(result, Matcher.quoteReplacement(prefix + replacement + suffix));
	            replacement = "";			//$NON-NLS-1$
	        }
	        matcher.appendTail(result);
	        // Save the formatted sentence text
	        workSentence = result.toString();
		}
		else workSentence = roleSentence;

		// Check for male/female sentences needing to be split at the $!& marker
		// and place results in correct text area
		if (workSentence.contains("$!&")) {		//$NON-NLS-1$
			sexSentences = workSentence.split("\\$!&");	// NOTE need escape for $!  //$NON-NLS-1$
			text_MaleSent.append(sexSentences[0]);
			text_FemaleSent.append(sexSentences[1]);
			}
		// No male/female split so just load whole sentence
		else text_MaleSent.append(workSentence);
	}		// End convertSentRoleNumToNames


/**
 * convertSentRoleNamesToNums - load sentence Text and convert role Names to their numbers for saving
 * @param editedSentence
 * @return converted sentence
 */
	public String convertSentRoleNamesToNums(String sentence) {
		// Incoming sentence string is assumed to be the text from the maleSentence area
		// plus the $!& separater plus the femaleSentence. Process it all as one string
        String replacement = "";		//$NON-NLS-1$

		// Look for role references (like [RF:father] etc). If there are none,
		// return sentence unchanged
		if (!sentence.contains("[R")) return sentence;		//$NON-NLS-1$

		// Setup a regex to find role reference patterns
		Pattern pattern = Pattern.compile("(\\[R[a-zA-Z0-9]{0,4}:)([^]]+)(])"); 		//$NON-NLS-1$
        Matcher matcher = pattern.matcher(sentence);
        StringBuffer result = new StringBuffer();

        // Search for the [R: references and extract the rolename string
        while (matcher.find()) {
            String prefix = matcher.group(1); // e.g., "[RX:"
            String value = matcher.group(2); // e.g., father
            String suffix = matcher.group(3); // e.g., "]"
            // then use 'value' to find correct rolenumber for that name, converted to a 5-char string
    		for (int i=0; i < eventRoleNames.length; i++) {
    			if (value.equals(eventRoleNames[i]))
    					replacement = String.format("%05d", eventRoleNumbers[i]);		//$NON-NLS-1$
    		}
    		// Check if no match found; if so throw error msg and return null as error flag
    		if (replacement.isEmpty()) {
    			JOptionPane.showMessageDialog(comboLanguage,
    					HG0551Msgs.Text_62 						// The role
    					+ value
    					+ HG0551Msgs.Text_63 					// does not exist in
    					+ comboLanguage.getSelectedItem() +
    					HG0551Msgs.Text_64,						// for this event. Please revise your edit.
    					HG0551Msgs.Text_65, 					// Role not matched
    					JOptionPane.WARNING_MESSAGE);
    			btn_Save.setEnabled(false);
    			return null;
    		}
    		// Otherwies insert the role name and look for another
            matcher.appendReplacement(result, Matcher.quoteReplacement(prefix + replacement + suffix));
            replacement = "";			//$NON-NLS-1$
        }
        matcher.appendTail(result);
        // Return the reformatted sentence text
		return result.toString();

	}		// End convertSentRoleNamesToNums

}  // End of HG0551DefineEvent
