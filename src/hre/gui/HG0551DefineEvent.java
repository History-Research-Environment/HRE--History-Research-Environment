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
 ********************************************************************************
 * NOTES for incomplete functionality:
 * NOTE01 code needed to load all roles and sentences for Event
 * NOTE04 code needed to save all data
 * NOTE05 code needed to action all control buttons
 *
 * Note also that the TMG Event tag fields for Past Tense, Abbreviation and type
 * are ignored for now and no place has been set on this screen yet.
 ********************************************************************************/

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.DefaultCaret;

import hre.bila.HB0711Logging;
import hre.bila.HBEventRoleManager;
import hre.bila.HBException;
import hre.bila.HBProjectOpenData;
import net.miginfocom.swing.MigLayout;

/**
 * Define Events
 * @author D Ferguson
 * @version v0.04.0032
 * @since 2022-04-20
 */

public class HG0551DefineEvent extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	public String screenID = "55100";	//$NON-NLS-1$
	private String className;
	private JPanel contents;
	HBEventRoleManager pointEventRoleManager;
	DefaultListModel<String> roleListmodel;
	JTable tableRole;
	String[] tableRoleHeader = new String[] {"Key?", "Role"};

    private String selectedEventName, selectedRoleName, selectedLangCode, newEventName;
    int selectedEventType, selectedRoleNumber, selectedGroup;
    
    int teller = 0;

	String[] eventTypeList;
	int[] eventTypeNumbers;
	String[] eventRoleList;
	int[] eventRoleNumbers;

	Object[] eventTypeTransfer = new Object[10]; // Array used to transfer date to HBEventRoleManager
	Object[] eventRoleTransfer = new Object[10]; // Array used to transfer date to HBEventRoleManager
	Object objTempRoleData[] = new Object[2];   // to temporarily hold a row of roles when moving rows around

	JTextField text_minYear, text_maxYear, text_minAssoc, text_gedTag, text_minAge, text_maxAge;
	JTextArea text_MaleSent, text_FemaleSent, hintText;
	JComboBox<String> comboGroups, comboSex, comboLanguage;

	String abbrev, pasttense, eventHint, gedComTag, roleSex, roleSentence;
	int newEventNumber, roleNumber, maxYear, minYear, roleMinAge, roleMaxAge, eventGroup, eventRoleSequence;

	String yes = " Yes";  // Tranlated to used datalanguage
	String no = " No"; // Tranlated to used datalanguage
	String[] sexOptions = {"Any","Male","Female"};
	String[] sexValues = {"U","M","F"};

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
	Object[][] tableRoleData;
    String[] eventGroups;
    int[] eventGroupIndex;
    HashMap<String, Integer> groupIndexMap = new  HashMap<String,Integer>();
    Object[] selectedRoleData = new Object[2];

/**
 * String getClassName()
 * @return className
 */
    public String getClassName() {
    	return className;
    }

/**
 *
 * @param pointOpenProject
 * @throws HBException
 * @wbp.parser.constructor
 */
    public HG0551DefineEvent(HBEventRoleManager pointEventRoleManager, HBProjectOpenData pointOpenProject,
    									boolean addNewEventType) throws HBException {
    	selectedEventName = "";
    	this.pointEventRoleManager = pointEventRoleManager;
    	constructDefineEvent(addNewEventType, true);
    }
/**
 *
 * @param pointOpenProject
 * @param selectedEventName
 * @param selectedRoleName
 * @throws HBException
 */
	public HG0551DefineEvent(HBEventRoleManager pointEventRoleManager, HBProjectOpenData pointOpenProject,
					boolean addNewEventType, int selectedEventType) throws HBException {
		this.selectedEventType = selectedEventType;
		this.pointEventRoleManager = pointEventRoleManager;
		constructDefineEvent(addNewEventType, true);
	}

/**
 * 	constructDefineEvent();
 * @throws HBException
 */
	private void constructDefineEvent(boolean addNewEventType, boolean initiate) throws HBException	{
		windowID = screenID;
		helpName = "defineevent";	//$NON-NLS-1$
    	className = getClass().getSimpleName();
    	this.setResizable(false);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: entering HG0551DefineEvent");}	//$NON-NLS-1$
				
	// Set up event groups map index
		eventGroups = pointEventRoleManager.geteventGroupNames();
		eventGroupIndex = pointEventRoleManager.geteventGroupNumbers();
		for (int i = 0; i < eventGroups.length; i++)  groupIndexMap.put(eventGroups[i], eventGroupIndex[i]);

		if(!addNewEventType) {
			eventTypeList = pointEventRoleManager.getEventTypeList(0);
			eventTypeNumbers = pointEventRoleManager.getEventTypes();
			eventRoleList = pointEventRoleManager.getEventRoleList(selectedEventType, "");
			eventRoleNumbers = pointEventRoleManager.getEventRoleTypes();
		// Find event type name for selected event
			for (int i = 0; i < eventTypeNumbers.length; i++)
				if (eventTypeNumbers[i] == selectedEventType) selectedEventName = eventTypeList[i];
		// Populate role list for selected event
			tableRoleData = new Object[eventRoleList.length][3];
			for (int i = 0; i < eventRoleList.length; i++)  {
				tableRoleData[i][0] = yes;
				tableRoleData[i][1] = " " + eventRoleList[i].trim();
				tableRoleData[i][2] = eventRoleNumbers[i];
			}
		} else tableRoleData = new Object[1][3]; 

		if (addNewEventType) {
			selectedEventName = "New";
			setTitle("Define "+ selectedEventName + " Event");
		}
		else {
			for (int i = 0; i < eventTypeNumbers.length; i++)
				if (selectedEventType == eventTypeNumbers[i])
					selectedEventName = eventTypeList[i];
			setTitle("Update "+ selectedEventName + " Event");
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

		JLabel lbl_Language = new JLabel("Language: ");
		contents.add(lbl_Language, "cell 1 0, alignx center");		//$NON-NLS-1$
		if (initiate) {
			comboLanguage = new JComboBox<String>(); 
			
			for (int i=0; i < HG0501AppSettings.dataReptLanguages.length; i++)
					comboLanguage.addItem(HG0501AppSettings.dataReptLanguages[i]);
		
			if (HGlobal.numOpenProjects > 0) 
				for (int i = 0; i < HG0501AppSettings.dataReptLangCodes.length; i++) 
					if (HGlobal.dataLanguage.equals(HG0501AppSettings.dataReptLangCodes[i])) {
						comboLanguage.setSelectedIndex(i);
						selectedLangCode = 	HG0501AppSettings.dataReptLangCodes[i];	
						pointEventRoleManager.setSelectedLanguage(selectedLangCode);
						//System.out.println(" Combo language selected: " + selectedLangCode);
					}
		}
				
		contents.add(comboLanguage, "cell 1 0");	//$NON-NLS-1$

/*****************************************************
 * Define the tabbed Pane structure - sets screen size
 *****************************************************/
		JTabbedPane tabPane = new JTabbedPane(JTabbedPane.TOP);
		tabPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
		tabPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		tabPane.setPreferredSize(new Dimension(670, 300));
		contents.add(tabPane, "cell 0 1 2"); //$NON-NLS-1$

/***********************************************
 * Setup General Settings Panel and its contents
 ***********************************************/
		JPanel settingPanel = new JPanel();
	    tabPane.addTab(" General Event Settings ", null, settingPanel);
		settingPanel.setLayout(new MigLayout("insets 5", "[]50[]", "10[]30[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	// Setup sub-panel for Event Name, etc
		JPanel namePanel = new JPanel();
		namePanel.setBorder(BorderFactory.createTitledBorder ("Event Name and Group"));
		namePanel.setLayout(new MigLayout("insets 5", "[]10[]", "[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_EventName = new JLabel("Event Name:");
		namePanel.add(lbl_EventName, "cell 0 0, alignx right");	//$NON-NLS-1$

		JTextField text_EventName = new JTextField();
		text_EventName.setPreferredSize(new Dimension(150, 20));
		text_EventName.setText(selectedEventName);
		namePanel.add(text_EventName, "cell 1 0, alignx left, aligny top");	//$NON-NLS-1$

		JLabel lbl_EventGrp = new JLabel("Event Group:");
		namePanel.add(lbl_EventGrp, "cell 0 1, alignx right");	//$NON-NLS-1$

		comboGroups = new JComboBox<String>();
		for (int i = 0; i < eventGroups.length; i++) comboGroups.addItem(eventGroups[i]);
		namePanel.add(comboGroups, "cell 1 1");	//$NON-NLS-1$
		settingPanel.add(namePanel, "cell 0 0");	//$NON-NLS-1$

	// Setup sub-panel for year validity
		JPanel validPanel = new JPanel();
		validPanel.setBorder(BorderFactory.createTitledBorder ("Event Validity - Associates and Years"));
		validPanel.setLayout(new MigLayout("insets 5", "[]10[]", "[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_MinKeyAssoc = new JLabel("Minimum Key Associates:");
		validPanel.add(lbl_MinKeyAssoc, "cell 0 0, alignx right");	//$NON-NLS-1$

		text_minAssoc = new JTextField("1");	//$NON-NLS-1$
		text_minAssoc.setPreferredSize(new Dimension(40, 20));
		validPanel.add(text_minAssoc, "cell 1 0, alignx left");	//$NON-NLS-1$

		JLabel lbl_minYear = new JLabel("Minimum Valid Year:");
		validPanel.add(lbl_minYear, "cell 0 1, alignx right");	//$NON-NLS-1$

		text_minYear = new JTextField("0");	//$NON-NLS-1$
		text_minYear.setPreferredSize(new Dimension(40, 20));
		validPanel.add(text_minYear, "cell 1 1, alignx left");	//$NON-NLS-1$

		JLabel lbl_maxYear = new JLabel("Maximum Valid Year:");
		validPanel.add(lbl_maxYear, "cell 0 2, alignx right");	//$NON-NLS-1$

		text_maxYear = new JTextField("3000");	//$NON-NLS-1$
		text_maxYear.setPreferredSize(new Dimension(40, 20));
		validPanel.add(text_maxYear, "cell 1 2, alignx left");	//$NON-NLS-1$
		settingPanel.add(validPanel, "cell 0 1, growx");	//$NON-NLS-1$

	// Setup sub-panel for GEDCOM info
		JPanel gedPanel = new JPanel();
		gedPanel.setBorder(BorderFactory.createTitledBorder ("Event GEDCOM Settings"));
		gedPanel.setLayout(new MigLayout("insets 5", "[]10[]", "[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_gedcom = new JLabel("Export to GEDCOM as:");
		gedPanel.add(lbl_gedcom, "cell 0 0, alignx right");	//$NON-NLS-1$

		JRadioButton radio_Tag = new JRadioButton("Tag:");
		radio_Tag.setSelected(true);
		gedPanel.add(radio_Tag, "cell 1 0, alignx left");	//$NON-NLS-1$

		text_gedTag = new JTextField();
		text_gedTag.setPreferredSize(new Dimension(100, 20));
		gedPanel.add(text_gedTag, "cell 1 0");	//$NON-NLS-1$

		JRadioButton radio_Even = new JRadioButton("1 EVEN, 2 TYPE " + selectedEventName.toUpperCase());	//$NON-NLS-1$
		gedPanel.add(radio_Even, "cell 1 1, alignx left");		//$NON-NLS-1$
		settingPanel.add(gedPanel, "cell 1 0");	//$NON-NLS-1$

		ButtonGroup radioGed = new ButtonGroup();
		radioGed.add(radio_Tag);
		radioGed.add(radio_Even);

/************************************************
 * Setup Role/Sentence Panel and 3 content panels
 ************************************************/
		JPanel roleSentPanel = new JPanel();
	    tabPane.addTab(" Roles and Sentences ", null, roleSentPanel);
		roleSentPanel.setLayout(new MigLayout("insets 5", "[]10[]", "[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// Setup sub-panel for role table
		JPanel rolePanel = new JPanel();
		rolePanel.setBorder(BorderFactory.createTitledBorder ("Roles of this Event"));
		rolePanel.setLayout(new MigLayout("insets 5", "[]10[]", "[]50[]10[]10[]10[]30[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JButton btn_Add = new JButton("Add New Role");
		rolePanel.add(btn_Add, "cell 0 2, alignx left, growx");	//$NON-NLS-1$

		JButton btn_Delete = new JButton("Delete Role");
		btn_Delete.setEnabled(false);
		rolePanel.add(btn_Delete, "cell 0 3, alignx left, growx");		//$NON-NLS-1$

		JButton btn_Copy = new JButton("Copy Role");
		btn_Copy.setEnabled(false);
		rolePanel.add(btn_Copy, "cell 0 4, alignx left, growx");		//$NON-NLS-1$

		JButton btn_MoveUp = new JButton("Move Up");
		btn_MoveUp.setEnabled(false);
		rolePanel.add(btn_MoveUp, "cell 0 5, alignx left, growx");		//$NON-NLS-1$

		JButton btn_MoveDown = new JButton("Move Down");
		btn_MoveDown.setEnabled(false);
		rolePanel.add(btn_MoveDown, "cell 0 6, alignx left, aligny top, growx");		//$NON-NLS-1$

	// Create scrollpane and table for the Roles
		DefaultTableModel roleModel = new DefaultTableModel(tableRoleData,tableRoleHeader);
		tableRole = new JTable(roleModel) {
			private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int row, int col) {
						return true;
			}};
	// Set a centred cell renderer for Key column
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		// Define table
		tableRole.getColumnModel().getColumn(0).setMinWidth(25);
		tableRole.getColumnModel().getColumn(0).setPreferredWidth(50);
		tableRole.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
		tableRole.getColumnModel().getColumn(1).setMinWidth(80);
		tableRole.getColumnModel().getColumn(1).setPreferredWidth(150);
		tableRole.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	// Set header and selection mode
		JTableHeader roleHeader = tableRole.getTableHeader();
		roleHeader.setOpaque(false);
		ListSelectionModel roleSelectionModel = tableRole.getSelectionModel();
		roleSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
	// Setup scrollpane and add to Role panel
		tableRole.setFillsViewportHeight(true);
		JScrollPane roleScrollPane = new JScrollPane(tableRole);
		tableRole.setPreferredScrollableViewportSize(new Dimension(200, 300));

		rolePanel.add(roleScrollPane, "cell 1 0 1 7");	//$NON-NLS-1$
		roleSentPanel.add(rolePanel, "cell 0 0 1 2");	//$NON-NLS-1$

		// Setup sub-panel for sentences
		JPanel sentPanel = new JPanel();
		sentPanel.setBorder(BorderFactory.createTitledBorder ("Sentences for the Selected Role"));
		sentPanel.setLayout(new MigLayout("insets 5", "[]", "[]5[]10[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_MaleSent = new JLabel("Male Sentence Structure:");
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

		JLabel lbl_FemaleSent = new JLabel("Female Sentence Structure (if different):");
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
		validityPanel.setBorder(BorderFactory.createTitledBorder ("Selected Role is valid for:"));
		validityPanel.setLayout(new MigLayout("insets 5", "[]5[]", "[]10[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_Sex = new JLabel("Birth Sex:");
		validityPanel.add(lbl_Sex, "cell 0 0, alignx right");	//$NON-NLS-1$

		comboSex = new JComboBox<String>();
		comboSex.addItem(sexOptions[0]);
		comboSex.addItem(sexOptions[1]);
		comboSex.addItem(sexOptions[2]);
		validityPanel.add(comboSex, "cell 1 0, alignx left");	//$NON-NLS-1$

		JLabel lbl_minAge = new JLabel("Minimum Age:");
		validityPanel.add(lbl_minAge, "cell 0 1, alignx right");	//$NON-NLS-1$

		text_minAge = new JTextField();
		text_minAge.setPreferredSize(new Dimension(30, 20));
		validityPanel.add(text_minAge, "cell 1 1, alignx left");	//$NON-NLS-1$

		JLabel lbl_maxAge = new JLabel("Maximum Age:");
		validityPanel.add(lbl_maxAge, "cell 0 2, alignx right");	//$NON-NLS-1$

		text_maxAge = new JTextField();
		text_maxAge.setPreferredSize(new Dimension(30, 20));
		validityPanel.add(text_maxAge, "cell 1 2, alignx left");	//$NON-NLS-1$

		roleSentPanel.add(validityPanel, "cell 1 1");	//$NON-NLS-1$

/***********************************************
 * Setup Hint Panel and its contents
 ***********************************************/
		JPanel hintPanel = new JPanel();
	    tabPane.addTab(" Event Hints ", null, hintPanel);
		hintPanel.setLayout(new MigLayout("insets 10", "[]", "[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	// Setup sub-panel for hints text area
		JPanel hint2Panel = new JPanel();
		hint2Panel.setBorder(BorderFactory.createTitledBorder ("Your Hints for Usage of this Event"));
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
		hintTextScroll.setPreferredSize(new Dimension(615, 300));
		hintTextScroll.getViewport().setOpaque(false);
		hintTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		hintText.setCaretPosition(0);	// set scrollbar to top
		hint2Panel.add(hintTextScroll, "cell 0 0");	//$NON-NLS-1$

		hintPanel.add(hint2Panel, "cell 0 0, align center");	//$NON-NLS-1$

/****************************************************
 * Setup control buttons at bottom and display screen
 ****************************************************/
		JButton btn_Save = new JButton("Save");
		btn_Save.setToolTipText("Save the edited Event type");
		if (addNewEventType)
			btn_Save.setEnabled(false);
		else btn_Save.setEnabled(true); // Enable update of event type name
		contents.add(btn_Save, "cell 1 2, align right, gapx 20, tag ok"); //$NON-NLS-1$

		JButton btn_Cancel = new JButton("Cancel");
		btn_Cancel.setToolTipText("Cancel changes and exit");
		contents.add(btn_Cancel, "cell 1 2, align right, gapx 20, tag cancel"); //$NON-NLS-1$
		
	// Set up data for gui	from database or default data
		//System.out.println( " Set up Define event! " + selectedEventType + " LangCode: " + selectedLangCode);
		updateEventTypeData(pointEventRoleManager.getEventTypeTransfer(selectedEventType),addNewEventType );
		
		selectedRoleNumber = 1; //Initial select row 1
		tableRole.setRowSelectionInterval(0, 0);
		updateEventRoleData(pointEventRoleManager.getEventRoleTransfer(selectedEventType, selectedRoleNumber), addNewEventType);
	
	// Display the screen
		pack();

/******************
 * ACTION LISTENERS
 ******************/
		// Listener for clicking 'X' on screen
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
	    		 btn_Cancel.doClick();
			} // return to main menu
		});
		
   // Language checkbox listener
		comboLanguage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			// Set selected languge
				if (comboLanguage.getSelectedIndex() != -1) {
					try {
						int selectedIndex = comboLanguage.getSelectedIndex();
						selectedLangCode = 	HG0501AppSettings.dataReptLangCodes[selectedIndex];
						//System.out.println(" New Language code: " + selectedLangCode + " / " + teller++);	
						pointEventRoleManager.setSelectedLanguage(selectedLangCode);
						constructDefineEvent(addNewEventType, false);
					} catch (HBException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});

		// Listener for Add role button
		btn_Add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//int atRow = 0;
				selectedRoleName = JOptionPane.showInputDialog("Enter new Role name").trim();
				if (selectedRoleName != null) {
					//System.out.println(" New Role for event: " + selectedRole);
					try {
						selectedEventType = pointEventRoleManager.getEventType();
						pointEventRoleManager.saveEventRoleData(saveEventRoleData());
						pointEventRoleManager.addNewEventRole(selectedRoleName.trim());
					// Reset table tableRoleData
						String[] newRoleList = pointEventRoleManager.getEventRoleList(selectedEventType, "");
						int[] roleNumbers = pointEventRoleManager.getEventRoleTypes();
						tableRoleData = new Object[newRoleList.length][3];
						for (int i = 0; i < newRoleList.length; i++)  {
							tableRoleData[i][0] = yes;
							tableRoleData[i][1] = " " + newRoleList[i].trim();
							tableRoleData[i][2] = roleNumbers[i];
						}
						DefaultTableModel persModel = (DefaultTableModel) tableRole.getModel();
						persModel.setDataVector(tableRoleData,tableRoleHeader);
						pack();
					} catch (HBException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// do s[]omething with newRole string
					btn_Save.setEnabled(true);

				} else System.out.println(" Type in new role name:");
				return;
			}
		});

		// Listener for Copy button
		btn_Copy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// NOTE05 need code here to copy then Edit
			}
		});

		// Listener for Delete button
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
				int selectedRow = tableRole.getSelectedRow();
				// only allow move up if not at top of table
				if (selectedRow >= 1) {
				// Switch rows in citeModel
					roleModel.moveRow(selectedRow, selectedRow, selectedRow-1);
				// Switch rows in underlying tableRoleData
					objTempRoleData = tableRoleData[selectedRow-1];
					tableRoleData[selectedRow-1] = tableRoleData[selectedRow];
					tableRoleData[selectedRow] = objTempRoleData;
				//  Reset visible selected row
				    tableRole.setRowSelectionInterval(selectedRow - 1, selectedRow - 1);
				    btn_Save.setEnabled(true);
				}

			}
		});

		// Listener for Move role Down button
		btn_MoveDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int tableSize = tableRole.getRowCount();
				int selectedRow = tableRole.getSelectedRow();
				// only allow move down if not at end of table
				if (selectedRow < tableSize-1) {
				// Switch rows in tableModel
					roleModel.moveRow(selectedRow, selectedRow, selectedRow+1);
				// Switch rows in underlying objEventCiteData
					objTempRoleData = tableRoleData[selectedRow];
					tableRoleData[selectedRow] = tableRoleData[selectedRow+1];
					tableRoleData[selectedRow+1] = objTempRoleData;
				//  Reset visible selected row
					tableRole.setRowSelectionInterval(selectedRow + 1, selectedRow + 1);
					btn_Save.setEnabled(true);
				}
			}
		});

	// Listener for Role table mouse clicks
	tableRole.addMouseListener(new MouseAdapter() {
		@Override
        public void mousePressed(MouseEvent me) {
           	if (me.getClickCount() == 1 && tableRole.getSelectedRow() != -1) {
           	// SINGLE-CLICK - turn on table controls
           		btn_Copy.setEnabled(true);
           		btn_Add.setEnabled(true);
           		btn_Delete.setEnabled(true);
        		btn_MoveUp.setEnabled(true);
        		btn_MoveDown.setEnabled(true);
        		
        	// Update role data in GUI
        		int atRow = tableRole.getSelectedRow();
        		selectedRoleData = tableRoleData[atRow]; // select whole row
        		selectedRoleNumber = (int) selectedRoleData[2];
        		System.out.println(" Single click role: " + selectedRoleNumber + "/" + selectedRoleData[1]);
        		updateEventRoleData(pointEventRoleManager.getEventRoleTransfer(selectedEventType, 
        														selectedRoleNumber), addNewEventType);
           	} 
        }
    });

		ActionListener actionRadioTag = new ActionListener() {
		     public void actionPerformed(ActionEvent actionEvent) {
		    	// save the gedcom setting as needed
		     }
		};
		ActionListener actionRadioEven = new ActionListener() {
		     public void actionPerformed(ActionEvent actionEvent) {
		    	// save the gedcom setting as needed
		     }
		};

	// Link the listeners above to the radio buttons
		radio_Tag.addActionListener(actionRadioTag);
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// NOTE04 save the edited event info
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: saved new event type from HG0551DefineEvent"); //$NON-NLS-1$
				dispose();

			}
		});

	// Listener for Save button - edit event type -save the data and exit
		if (!addNewEventType)
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int atRow;
				try {
					newEventName = text_EventName.getText();
					selectedGroup = groupIndexMap.get(comboGroups.getSelectedItem());
					pointEventRoleManager.saveEventTypeData(saveEventTypeData());
					pointEventRoleManager.editEventType(newEventName, selectedEventType, selectedGroup, eventTypeTransfer);
	           // Save data for selceted roel		
					atRow = tableRole.getSelectedRow();
					if (atRow != -1) {
		           		selectedRoleData = tableRoleData[atRow]; // select whole row
		           		//System.out.println(" Save role: " + atRow + "-" + selectedRoleData[0] + " / " + selectedRoleData[1] );
		           		
		           		selectedRoleName = ((String) selectedRoleData[1]).trim();
		           		selectedRoleNumber = (int) selectedRoleData[2];
		
						pointEventRoleManager.editEventRole(selectedEventType, selectedRoleNumber, saveEventRoleData());
					} else System.out.println( " No role selection!" + atRow);

				} catch (HBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// NOTE04 save the edited event info
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: saved edit event type from HG0551DefineEvent"); //$NON-NLS-1$
				dispose();
			}
		});

		// Listener for Cancel button
		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: cancelling out of HG0551DefineEvent"); //$NON-NLS-1$
				dispose();
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
		gedComTag = text_gedTag.getText();
		eventTypeTransfer[3] = gedComTag.trim();
		minYear = Integer.parseInt(text_minYear.getText());
		eventTypeTransfer[4] = minYear;
		maxYear = Integer.parseInt(text_maxYear.getText());
		eventTypeTransfer[5] = maxYear;
		eventTypeTransfer[6] = ""; //abbrev;
		eventTypeTransfer[7] = ""; //pasttense;
		eventHint = hintText.getText();
		eventTypeTransfer[8] = eventHint ; //reminder;	
		return eventTypeTransfer;
	}
	
/**
 * protected void saveEventRoleData()
 */
	protected Object[] saveEventRoleData() {
		eventRoleTransfer[0] = selectedRoleNumber; 
		eventRoleTransfer[1] = newEventNumber; //newEventNumber;
		eventRoleTransfer[2] = selectedRoleName; //
		eventRoleTransfer[3] = sexValues[comboSex.getSelectedIndex()]; 
		roleMinAge = Integer.parseInt(text_minAge.getText());
		eventRoleTransfer[4] = roleMinAge;
		roleMaxAge = Integer.parseInt(text_maxAge.getText());
		eventRoleTransfer[5] = roleMaxAge;
		eventRoleTransfer[6] = null; 
		eventRoleTransfer[7] = false; 
		eventRoleSequence = 1;
		return eventRoleTransfer;
	}

	/**
	 * updateEventTypeData(Object[] eventTypeData, boolean addNewEventType)
	 * @param eventTypeTransfer
	 * @param addNewEventType
	 */

	private void updateEventTypeData(Object[] eventTypeTransfer, boolean addNewEventType) {
		if (addNewEventType) {
			abbrev = "abr.";
			pasttense = " sentece";
			text_gedTag.setText("GEDCOM");
			text_minYear.setText("0");
			text_maxYear.setText("2000");
		} else {
			for (int i = 0; i <  eventGroupIndex.length; i++)
				if ((int)eventTypeTransfer[2] == eventGroupIndex[i])
					comboGroups.setSelectedIndex(i);
			text_gedTag.setText((String) eventTypeTransfer[3]);
			text_minYear.setText("" + eventTypeTransfer[4]);
			text_maxYear.setText("" + eventTypeTransfer[5]);
			hintText.append("" + eventTypeTransfer[8]);
		}
	}

/**
 * protected void updateEventRoleData(Object[] eventRoleData, boolean addNewEventType)
 * @param eventTypeTransfer
 * @param addNewEventType
 */
	private void updateEventRoleData(Object[] eventRoleTransfer, boolean addNewEventType) {
		if (addNewEventType) {
			roleSentence = " sentence";
			roleSex = "U";
			text_minAge.setText("0");
			text_maxAge.setText("100");
		} else {
			comboSex.setSelectedIndex(0);
			roleSex = sexValues[0];
			if (eventRoleTransfer[3] != null) 
				for (int i = 0; i < 3; i++)
					if (eventRoleTransfer[3].equals(sexValues[i])) {
						comboSex.setSelectedIndex(i);
						roleSex = sexValues[i];
					}
			text_minAge.setText("" + eventRoleTransfer[4]);
			text_maxAge.setText("" + eventRoleTransfer[5]);		
		}
	}

/**
 * protected void resetRoleTableData() throws HBException
 * @throws HBException
 */
	protected void resetRoleTableData() throws HBException {
		String[] newRoleList = pointEventRoleManager.getEventRoleList(selectedEventType, "");
		int[] eventRoles = pointEventRoleManager.getEventRoleTypes();
		// Populate role list for selected event
		tableRoleData = new Object[newRoleList.length][3];
		for (int i = 0; i < newRoleList.length; i++)  {
			tableRoleData[i][0] = yes;
			tableRoleData[i][1] = " " + newRoleList[i].trim();
			tableRoleData[i][2] = eventRoles[i];
		}

		DefaultTableModel persModel = (DefaultTableModel) tableRole.getModel();
		persModel.setDataVector(tableRoleData,tableRoleHeader);
		pack();
	}

}  // End of HG0551DefineEvent
