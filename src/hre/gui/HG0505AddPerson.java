package hre.gui;
/*******************************************************************************
 * Add Person - Specification 05.05 GUI_AddPerson
 * v0.01.0027 2022-03-18 Initial draft, updated 31/3 (D Ferguson)
 * 			  2022-04-16 Shrunk to just 3 panels (D Ferguson)
 * v0.01.0028 2022-01-31 Implemented Name/Locn Styles (N Tolleshaug)
 * v0.03.0030 2023-07-21 revised screen format (D Ferguson)
 * 		 	  2023-09-08 Implemented class AddPersonRecord (N Tolleshaug)
 * 		      2023-09-10 Add Reference/Media panel (D Ferguson)
 * 			  2023-09-16 Implemented edit name and edit editBaptDate last entry (N Tolleshaug)
 * 			  2023-09-18 Activated buttons for person and create all (N Tolleshaug)
 * 			  2023-09-21 GUI rewrite (D Ferguson)
 * v0.03.0031 2023-11-15 Added missing bapt/death/burial panels (D Ferguson)
 *			  2023-11-15 Update event switch (N Tolleshaug)
 *			  2023-11-16 Add missing ActionListeners (D Ferguson)
 *			  2023-11-19 Remove media section add Memo text entry areas (D Ferguson)
 *			  2023-11-27 Added code to copy date to sort date (N Tolleshaug)
 *			  2023-12-05 Added code for birth, bapt, death and burial data (N Tolleshaug)
 *			  2023-12-08 Prepared for add memo (N Tolleshaug)
 *			  2023-12-23 Revised Name panel layout; added Date listener (D Ferguson)
 * 			  2023-12-23 Updated for select Christ/Bapt (N. Tolleshaug)
 * 			  2023-12-24 Implemented add child (N Tolleshaug)
 * 			  2023-12-24 Update set sex at birth flag when save (N Tolleshaug)
 * 			  2024-01-03 Update of role list for baptism according to event (N Tolleshaug)
 * 			  2024-01-07 Add Partner panel for partner events (D Ferguson)
 * 			  2024-01-09 Update of partner event (N Tolleshaug)
 * 			  2024-01-11 Fix of event select for birth and bapt (N Tolleshaug)
 * 			  2024-01-23 Added Death/Burial Event type selection (D Ferguson)
 * 			  2024-01-28 Fix Citation panel area, other layout changes (D Ferguson)
 * 			  2024-02-01 Add Living=N setting to Death/Burial action (D Ferguson)
 * 			  2024-03-23 Update add person event creation (N Tolleshaug)
 * 			  2024-06-10 Remove Sentence text fields; add Sentence edit button (D Ferguson)
 * 			  2024-07-24 Updated for use of HG0590EditDate (N Tolleshaug)
 * 			  2024-08-17 Fix for add partner event from main add partner (N Tolleshaug)
 * 			  2024-08-25 NLS conversion (D Ferguson)
 * 			  2024-10-06 In Name panel, swap Flag/Citation areas (D Ferguson)
 * 			  2024-10-12 Change screen layouts for consistency with Event screen ( D Ferguson)
 * 			  2024-10-18 used 'if (HGlobal.DEBUG)' to removed console printout (N Tolleshaug)
 * 			  2024-10-22 only allow Save to proceed if Person has a Name (D Ferguson)
 * 			  2024-10-24 ignore null data entry in name, location fields (D Ferguson)
 * 			  2024-10-25 make date fields non-editable by keyboard (D Ferguson)
 * 			  2024-10-31 Reduce Memo/Citation area size and make consistent (D Ferguson)
 * 			  2024-11-03 Removed SwingUtility from table cell edit focus (D Ferguson)
 * 			  2024-12-02 Replace JOptionPane 'null' locations with 'contents' (D Ferguson)
 * 			  2024-12-09 Update TAB handling for �erson and location names (N Tolleshaug)
 * v0.04.0032 2024-12-26 Only turn Living flag to N if Death/Burial saved (D Ferguson)
 * 			  2024-12-31 Add citation table select/up/down code (D Ferguson)
 * 			  2025-02-16 Fixed error in birt event list - adding language (N Tolleshaug)
 * 			  2025-03-17 Adjust Citation table column sizes (D Ferguson)
 * 			  2025-03-24 Remove all Citation tables (D Ferguson)
 ********************************************************************************
 * NOTES on incomplete functionality:
 * NOTE02 need Sentence Editor function eventually
 ********************************************************************************/

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.EventObject;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.DefaultCaret;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.gui.HGlobalCode.JTableCellTabbing;
import hre.gui.HGlobalCode.focusPolicy;
import hre.nls.HG0505Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Add Person
 * @author D Ferguson
 * @version v0.04.0032
 * @since 2022-03-18
 */

public class HG0505AddPerson extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	private String screenID = "50500";		//$NON-NLS-1$
	long null_RPID  = 1999999999999999L;
	long proOffset = 1000000000000000L;
	private JPanel contents;
	JPanel panelNameRelate;

	long newPartnerPID = null_RPID;
	int selectedPartnerType = 1;
	HBPersonHandler pointPersonHandler;

	boolean createEvent = false;
	boolean resetPS = false;
	// Signals memo is edited
	boolean nameMemoEdited = false, birthMemoEdited = false, baptMemoEdited = false,
			deathMemoEdited = false, burialMemoEdited = false, partnerMemoEdited = false;
	// Signals date format OK
	boolean birthDateOK = false, baptDateOK = false,
			deathDateOK = false, burialDateOK = false, partnerDateOK = false;
	JButton btn_Save;

    static focusPolicy newPolicy;

    Object[][] tableNameData;
	Object[][] tableFlagData;
	Object[][] objAllFlagData;
	Object[][] objReqFlagData;
    Object[][] tableBirthData;
    Object[][] tableBaptData;
    Object[][] tableDeathData;
    Object[][] tableBurialData;
    Object[][] tablePartnerData;

    JTable tableFlags;
    DefaultTableModel flagModel;
    JRadioButton radio_Partner;
    String langCode = HGlobal.dataLanguage;

    boolean unrelated = true; // Unrelated person
    boolean[] updatedEvent = {false,false,false,false,false}; // birth, bapt, death, burial, partner event to be created
    int[] eventLocationUpdates = new int[5]; // Counts the location element updates
    int nameElementUpdates = 0; // Counts if name elements are edited for create person name

	static int birthEventNumber = 1002;	// birth event
	static int birthEventGroup = 4; // Birth / Christning / Baptism group
	String selectBirthRoles = " AND EVNT_ROLE_NUM BETWEEN 1 AND 99";		//$NON-NLS-1$
	String[] birthEventList = null;
	int[] birthEventType = null;
	String[] birthRoleList = null;
	int[] birthRoleType = null;

	static int baptEventNumber = 1012;	// baptism event
	static int baptEventGroup = 4; // Birth / Christning / Baptism group
	String selectBaptRoles = " AND EVNT_ROLE_NUM BETWEEN 1 AND 99";		//$NON-NLS-1$
	String[] baptEventList;
	int[] baptEventType = null;
	String[] baptRoleList = null;
	int[] baptRoleType = null;

	static int deathEventNumber = 1003;	// death event
	static int deathEventGroup = 5; // Death group
	String selectDeathRoles = " AND EVNT_ROLE_NUM BETWEEN 1 AND 99";	//$NON-NLS-1$
	String[] deathEventList;
	int[] deathEventType = null;
	String[] deathRoleList = null;
	int[] deathRoleType = null;

	static int burialEventNumber = 1006; // burial event
	static int burialEventGroup = 9; // Burial group
	String selectBurialRoles = " AND EVNT_ROLE_NUM BETWEEN 1 AND 99";	//$NON-NLS-1$
	String[] burialEventList;
	int[] burialEventType = null;
	String[] burialRoleList = null;
	int[] burialRoleType = null;

	static int partnerEventNumber = 1004; // marriage event
	static int partnerEventGroup = 6; // marriage/partner group
	String selectPartnerRoles = " AND EVNT_ROLE_NUM BETWEEN 1 AND 99";	//$NON-NLS-1$
	String[] partnerEventList;
	int[] partnerEventType = null;
	String[] partnerRoleList = null;
	int[] partnerRoleType = null;

	HG0590EditDate editBirthDate;
	long birthMainYear = 0L;
	String birthMainDetails = "";	//$NON-NLS-1$
	long birthExtraYear = 0L;
	String birthExtraDetails = "";	//$NON-NLS-1$
	String birthSortCode = "";		//$NON-NLS-1$
	Object[] birthHREDate;
	long birthHDatePID;

	HG0590EditDate editBirthSortDate;
	long birthSortMainYear = 0L;
	String birthSortMainDetails = "";	//$NON-NLS-1$
	long birthSortExtraYear = 0L;
	String birthSortExtraDetails = "";	//$NON-NLS-1$
	String birthSortSortCode = "";		//$NON-NLS-1$
	Object[] birthSortHREDate;
	boolean birthSortDateOK = false;
	long birthSortHDatePID;

	HG0590EditDate editBaptDate;
	long baptMainYear = 0L;
	String baptMainDetails = "";	//$NON-NLS-1$
	long baptExtraYear = 0L;
	String baptExtraDetails = "";	//$NON-NLS-1$
	String baptSortCode = "";		//$NON-NLS-1$
	Object[] baptHREDate;
	long baptHDatePID;

	HG0590EditDate editBaptSortDate;
	long baptSortMainYear = 0L;
	String baptSortMainDetails = "";	//$NON-NLS-1$
	long baptSortExtraYear = 0L;
	String baptSortExtraDetails = "";	//$NON-NLS-1$
	String baptSortSortCode = "";		//$NON-NLS-1$
	Object[] baptSortHREDate;
	boolean baptSortDateOK = false;
	long baptSortHDatePID;

	HG0590EditDate editDeathDate;
	long deathMainYear = 0L;
	String deathMainDetails = "";	//$NON-NLS-1$
	long deathExtraYear = 0L;
	String deathExtraDetails = "";	//$NON-NLS-1$
	String deathSortCode = "";		//$NON-NLS-1$
	Object[] deathHREDate;
	long deathHDatePID;

	HG0590EditDate editDeathSortDate;
	long deathSortMainYear = 0L;
	String deathSortMainDetails = "";	//$NON-NLS-1$
	long deathSortExtraYear = 0L;
	String deathSortExtraDetails = "";	//$NON-NLS-1$
	String deathSortSortCode = "";		//$NON-NLS-1$
	Object[] deathSortHREDate;
	boolean deathSortDateOK = false;
	long deathSortHDatePID;

	HG0590EditDate editBurialDate;
	long burialMainYear = 0L;
	String burialMainDetails = "";	//$NON-NLS-1$
	long burialExtraYear = 0L;
	String burialExtraDetails = "";	//$NON-NLS-1$
	String burialSortCode = "";		//$NON-NLS-1$
	Object[] burialHREDate;
	long burialHDatePID;

	HG0590EditDate editBurialSortDate;
	long burialSortMainYear = 0L;
	String burialSortMainDetails = "";	//$NON-NLS-1$
	long burialSortExtraYear = 0L;
	String burialSortExtraDetails = "";	//$NON-NLS-1$
	String burialSortSortCode = "";		//$NON-NLS-1$
	Object[] burialSortHREDate;
	boolean burialSortDateOK = false;
	long burialSortHDatePID;

	HG0590EditDate editPartnerDate;
	long partnerMainYear = 0L;
	String partnerMainDetails = "";	//$NON-NLS-1$
	long partnerExtraYear = 0L;
	String partnerExtraDetails = "";	//$NON-NLS-1$
	String partnerSortCode = "";		//$NON-NLS-1$
	Object[] partnerHREDate;
	long partnerHDatePID;

	HG0590EditDate editPartnerSortDate;
	long partnerSortMainYear = 0L;
	String partnerSortMainDetails = "";	//$NON-NLS-1$
	long partnerSortExtraYear = 0L;
	String partnerSortExtraDetails = "";	//$NON-NLS-1$
	String partnerSortSortCode = "";		//$NON-NLS-1$
	Object[] partnerSortHREDate;
	boolean partnerSortDateOK = false;
	long partnerSortHDatePID;

	// Global variables to be inherited
	JComboBox<String> birthRoles, baptRoles, deathRoles, burialRoles, comboPartRole1, comboPartRole2,
					  comboBirthType, comboBaptType, comboDeathType, comboBurialType, comboPartnerType;
	JTextField birthDateText, baptDateText, deathDateText, burialDateText, partnerDateText,
			   birthSortDateText, baptSortDateText, deathSortDateText, burialSortDateText, partnerSortDateText;
	JTextField refText;
	JTextArea memoNameText, memoBirthText, memoBaptText, memoDeathText, memoBurialText, memoPartnerText;
	JLabel lbl_ChosenPartnerEvent = new JLabel("");	//$NON-NLS-1$
	JLabel lbl_ChosenPartnerRole1 = new JLabel(""); //$NON-NLS-1$
	JLabel lbl_ChosenPartnerRole2 = new JLabel(""); //$NON-NLS-1$

/**
 * Create the dialog
 * @param pointOpenProject
 * @param pointStyleData
 * @param sexIndex 		is 0 for unrelated/partner, 1 for female (mother/dau/sister), 2 for male (father/brother/son)
 * @param relationship	is relationship of add Person to currently active ManagePerson
 * @param ofPerson		is Name of person currently displayed in ManagePerson
 * @throws HBException
 */
	public HG0505AddPerson(HBProjectOpenData pointOpenProject,
						   HBPersonHandler pointPersonHandler,
						   int sexIndex,
						   boolean unrelated,
						   String ofPerson) throws HBException {
		this.pointPersonHandler = pointPersonHandler;
		this.pointOpenProject  = pointOpenProject;
		this.unrelated = unrelated;

		if (unrelated) setTitle(HG0505Msgs.Text_0);	// Add a new Unrelated Person
		// Setup references for HG0450
		windowID = screenID;
		helpName = "addperson";		//$NON-NLS-1$

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0505AddPerson");		//$NON-NLS-1$

/***********************************
 * Setup main panel and its contents
 ***********************************/
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]", "[]5[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    	JToolBar toolBar = new JToolBar();
    	toolBar.setFocusTraversalKeysEnabled(false);
    	toolBar.setFloatable(false);
    	toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
    	Component horizontalGlue = Box.createHorizontalGlue();
    	horizontalGlue.setFocusTraversalKeysEnabled(false);
    	toolBar.add(horizontalGlue);
    	// Add HG0450 icons
		toolBar.add(btn_Remindericon);
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north");	//$NON-NLS-1$

/******************************************************************
 * Define Label & Tooltip text used repeatedly in following panels
 *****************************************************************/
		String eventText = HG0505Msgs.Text_1;				// Event Type:
		String roleText = HG0505Msgs.Text_2;				// Role:
		String eventDateText = HG0505Msgs.Text_3;			// Event Date:
		String sortDateText = HG0505Msgs.Text_4;			// Sort Date:
		String locStyleText = HG0505Msgs.Text_5;			// Location Style:
//		String citationText = HG0505Msgs.Text_6;			// Citations:			** no longer used **
//		String suretyText = HG0505Msgs.Text_7;				// Surety:			** no longer used **
		String memoText = HG0505Msgs.Text_8;				// Memo:
//		String sentenceText = HG0505Msgs.Text_9;			// Sentence:			** no longer used **
		String sentenceEditor = HG0505Msgs.Text_10;			// Sentence Editor
//		String citationUpText = HG0505Msgs.Text_11;			// Moves Citation up the list		** no longer used **
//		String citationDownText = HG0505Msgs.Text_12;		// Moves Citation down the list		** no longer used **
		String addPersonText = HG0505Msgs.Text_13;			// Add Person

/**************************************
 * Setup leftPanel and its contents
 **************************************/
		JPanel leftPanel = new JPanel();
		leftPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		contents.add(leftPanel, "cell 0 0, growx, aligny top");	//$NON-NLS-1$
		leftPanel.setLayout(new MigLayout("insets 5", "[]", "[]10[]10[]10[]10[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_AddType = new JLabel(HG0505Msgs.Text_14);		// Add ...
		leftPanel.add(lbl_AddType, "cell 0 0, alignx left");	//$NON-NLS-1$

		JRadioButton radio_Name = new JRadioButton(HG0505Msgs.Text_15);	// Name and Flags
		radio_Name.setSelected(true);
		leftPanel.add(radio_Name, "cell 0 1, alignx left");		//$NON-NLS-1$

		JRadioButton radio_Birth = new JRadioButton(HG0505Msgs.Text_16);		// Birth
		leftPanel.add(radio_Birth, "cell 0 2, alignx left");	//$NON-NLS-1$

		JRadioButton radio_Bapt = new JRadioButton(HG0505Msgs.Text_17);		// Baptism/Christening
		leftPanel.add(radio_Bapt, "cell 0 3, alignx left");		//$NON-NLS-1$

		JRadioButton radio_Death = new JRadioButton(HG0505Msgs.Text_18);		// Death
		leftPanel.add(radio_Death, "cell 0 4, alignx left");		//$NON-NLS-1$

		JRadioButton radio_Burial = new JRadioButton(HG0505Msgs.Text_19);		// Burial
		leftPanel.add(radio_Burial, "cell 0 5, alignx left");		//$NON-NLS-1$

		radio_Partner = new JRadioButton(HG0505Msgs.Text_20);			// Partner Event
		radio_Partner.setVisible(false);	// make this radio button part of the group, but invisible initially
		leftPanel.add(radio_Partner, "cell 0 6, alignx left, hidemode 3");		//$NON-NLS-1$

		ButtonGroup radioGroup = new ButtonGroup();
		radioGroup.add(radio_Name);
		radioGroup.add(radio_Birth);
		radioGroup.add(radio_Bapt);
		radioGroup.add(radio_Death);
		radioGroup.add(radio_Burial);
		radioGroup.add(radio_Partner);

/***************************************************
 * Setup rightPane and a CardLayout of Panels in it
 **************************************************/
		JPanel rightPanel = new JPanel(new CardLayout());
		rightPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		contents.add(rightPanel, "cell 1 0, grow");	//$NON-NLS-1$
		// Define cards of the CardLayout, each card-Panel with its own layout manager
		JPanel cardName = new JPanel();
		cardName.setLayout(new MigLayout("", "[grow, fill]", "[grow, fill]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		rightPanel.add(cardName, "NAME");	//$NON-NLS-1$
		JPanel cardBirth = new JPanel();
		cardBirth.setLayout(new MigLayout("", "[grow][grow]", "[grow][grow]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		rightPanel.add(cardBirth, "BIRTH");	//$NON-NLS-1$
		JPanel cardBapt = new JPanel();
		cardBapt.setLayout(new MigLayout("", "[grow][grow]", "[grow][grow]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		rightPanel.add(cardBapt, "BAPTISM");	//$NON-NLS-1$
		JPanel cardDeath = new JPanel();
		cardDeath.setLayout(new MigLayout("", "[grow][grow]", "[grow][grow]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		rightPanel.add(cardDeath, "DEATH");		//$NON-NLS-1$
		JPanel cardBurial = new JPanel();
		cardBurial.setLayout(new MigLayout("", "[grow][grow]", "[grow][grow]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		rightPanel.add(cardBurial, "BURIAL");	//$NON-NLS-1$
		JPanel cardPartner = new JPanel();
		cardPartner.setLayout(new MigLayout("", "[grow][grow]", "[grow][grow]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		rightPanel.add(cardPartner, "PARTNER");	//$NON-NLS-1$

/************************************************************
 * Setup cardName contents - panelName with Flags
 ***********************************************************/
	// Define Name panels
		JPanel panelNameCol0 = new JPanel();
		panelNameCol0.setLayout(new MigLayout("", "[]", "[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$)
		JPanel panelNameCol1 = new JPanel();
		panelNameCol1.setLayout(new MigLayout("", "[]", "[]10[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	// Create Name/Flags details sub-panel
	//************************************
		JPanel panelNameDetail = new JPanel();
		panelNameDetail.setFocusTraversalKeysEnabled(false);
		panelNameDetail.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		panelNameDetail.setLayout(new MigLayout("insets 5", "[]", "[]10[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_Name = new JLabel(HG0505Msgs.Text_21);		// Name:
		lbl_Name.setFocusTraversalKeysEnabled(false);
		lbl_Name.setFont(lbl_Name.getFont().deriveFont(lbl_Name.getFont().getStyle() | Font.BOLD));
		panelNameDetail.add(lbl_Name, "cell 0 0,alignx left");		//$NON-NLS-1$

		JLabel lbl_nStyles = new JLabel(HG0505Msgs.Text_22);	// Name Style:
		lbl_nStyles.setFocusTraversalKeysEnabled(false);
		panelNameDetail.add(lbl_nStyles, "cell 0 1, alignx left");		//$NON-NLS-1$
		// Get all NameStyles that can apply to thePerson's name
		DefaultComboBoxModel<String> comboNameModel = new DefaultComboBoxModel<>(
									pointPersonHandler.getNameStyles());
		JComboBox<String> persNameStyle = new JComboBox<>(comboNameModel);
		persNameStyle.setSelectedIndex(pointPersonHandler.getDefaultNameStyleIndex());
		panelNameDetail.add(persNameStyle, "cell 0 1, gapx 10");		//$NON-NLS-1$

	// Create scrollpane and table for the Name data
		JTable tableName = new JTable() {
			private static final long serialVersionUID = 1L;
				public Dimension getPreferredScrollableViewportSize() {
					  return new Dimension(super.getPreferredSize().width,
					    		super.getRowCount() * super.getRowHeight());
					 }
				public boolean isCellEditable(int row, int col) {
					if (col == 1) return true;
					return false;
				}
				public boolean editCellAt(int row, int col, EventObject e) {
					if (col == 0) return false;
				    boolean result = super.editCellAt(row, col, e);
				    final Component editor = getEditorComponent();
				    if (e != null && e instanceof MouseEvent) {
				    	((JTextField)editor).requestFocus();
		        		((JTextField)editor).getCaret().setVisible(true);
				    }
				    return result;
				}
				public Component prepareRenderer(TableCellRenderer renderer, int row, int col)  {
					Component cell = super.prepareRenderer(renderer, row, col);
					// For the Selected cell we let the editor take over with no highlights
					if (col == 1) cell.setCursor(Cursor.getDefaultCursor());
					cell.setBackground((Color) UIManager.get("Table.background"));		//$NON-NLS-1$
					cell.setForeground((Color) UIManager.get("Table.foreground"));		//$NON-NLS-1$
					return cell;
				}
			};

		int defaultPersonNameStyle = pointPersonHandler.getDefaultNameStyleIndex();
		persNameStyle.setSelectedIndex(defaultPersonNameStyle);
		tableNameData = pointPersonHandler.getNameDataTable(defaultPersonNameStyle);
		String[] tableNameHeader = pointPersonHandler.getNameTableHeader();
		tableName.setModel(new DefaultTableModel(tableNameData,tableNameHeader));

		// Make table single-click editable
		((DefaultCellEditor) tableName.getDefaultEditor(Object.class)).setClickCountToStart(1);
		// and make loss of focus on a cell terminate the edit
		tableName.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);	//$NON-NLS-1$

		tableName.getColumnModel().getColumn(0).setMinWidth(50);
		tableName.getColumnModel().getColumn(0).setPreferredWidth(120);
		tableName.getColumnModel().getColumn(1).setMinWidth(100);
		tableName.getColumnModel().getColumn(1).setPreferredWidth(200);
		tableName.setAutoCreateColumnsFromModel(false);	// preserve column setup
		JTableHeader nameHeader = tableName.getTableHeader();
		nameHeader.setOpaque(false);
		ListSelectionModel nameSelectionModel = tableName.getSelectionModel();
		nameSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

	// Setup scrollpane and add to Name panel
		tableName.setFillsViewportHeight(true);
		JScrollPane nameScrollPane = new JScrollPane(tableName);
		nameScrollPane.setFocusTraversalKeysEnabled(false);
		nameScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		nameScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
	// Setup tabbing within table against all rows but only column 1
		JTableCellTabbing.setTabMapping(tableName, 0, tableName.getRowCount(), 1, 1);
	// Add to panel
		panelNameDetail.add(nameScrollPane, "cell 0 2, aligny top");		//$NON-NLS-1$
	// sub-panel to NameCol0 panel
		panelNameCol0.add(panelNameDetail, "cell 0 0, aligny top");		//$NON-NLS-1$

	// Add Reference area to NameCol1 panel
	//*********************************
		JLabel lbl_Ref = new JLabel(HG0505Msgs.Text_23);		// Reference:
		lbl_Ref.setFont(lbl_Ref.getFont().deriveFont(lbl_Ref.getFont().getStyle() | Font.BOLD));
		panelNameCol1.add(lbl_Ref, "cell 0 0,alignx left");		//$NON-NLS-1$
		refText = new JTextField();
		refText.setColumns(24);
		panelNameCol1.add(refText, "cell 0 0,alignx left,gapx 10");		//$NON-NLS-1$

	// Create a Flag sub-Panel for flag data
	//**************************************
		JPanel panelFlags = new JPanel();
		panelFlags.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		panelFlags.setLayout(new MigLayout("insets 5", "[]", "[]3[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_Flags = new JLabel(HG0505Msgs.Text_24);		// Flags:
		lbl_Flags.setFont(lbl_Flags.getFont().deriveFont(lbl_Flags.getFont().getStyle() | Font.BOLD));
		panelFlags.add(lbl_Flags, "cell 0 0,alignx left");		//$NON-NLS-1$

		objAllFlagData = pointPersonHandler.getAddPersobFlagTable();
	// and sort on GUI-sequence (column 2)
		Arrays.sort(objAllFlagData, (o1, o2) -> Integer.compare((Integer) o1[2], (Integer) o2[2]));

	// Now count the number of Active Flags to be able to set size of objReqFlagData
		int activeFlags = 0;
		for (Object[] element : objAllFlagData) {
			if ((boolean) (element[1])) activeFlags++;
		}
	/**
	 * Now build only the Active Flag data into objReqFlagData.
	 * Required table columns are:
	 * From T251_FLAG_DEFN:					From T204_FLAG_TRAN:
	 	0 - IS_SYSTEM,						5 - LANG_CODE
	 	1 - ACTIVE							6 - FLAG_NAME
	 	2 - GUI_SEQ							7 - FLAG_VALUES
		3 - FLAG_IDENT						8 - FLAG_DESC
		4 - DEFAULT_INDEX ?

		Display columns 0, 1 in table, col 2 in textarea, keep col 3 to identify flag changes, col 4 to get values
	 */
		objReqFlagData = new Object[activeFlags][5];
		int reqRow = 0;
		int defaultIndex = 0;
		for (Object[] element : objAllFlagData) {
			if ((boolean) (element[1])) {
				objReqFlagData[reqRow][0] = element[6];	// Flag name
				String flagPossibles = (String) element[7];
				String[] values = flagPossibles.split(",");	//$NON-NLS-1$
				objReqFlagData[reqRow][1] = "   " + values[defaultIndex]; // Set default value		//$NON-NLS-1$
				objReqFlagData[reqRow][2] = defaultIndex;	// Flag index value
				objReqFlagData[reqRow][3] = element[3];		// Flag ID
				objReqFlagData[reqRow][4] = element[7];		// Flag values
				reqRow++;
			}
		}

		String[] tableFlagHeading = pointPersonHandler.setTranslatedData("50600", "5", false); //$NON-NLS-1$ //$NON-NLS-2$
		flagModel = new DefaultTableModel(objReqFlagData, tableFlagHeading);

	// Create table for the Flag data
		tableFlags = new JTable(flagModel) {
			private static final long serialVersionUID = 1L;
				public Dimension getPreferredScrollableViewportSize() {
					// Force a maximum of a 5-row table
					int r = super.getRowCount();
					if (r > 5) r = 5;
					  return new Dimension(super.getPreferredSize().width,
					    		r * super.getRowHeight());
				  }
				public boolean isCellEditable(int row, int col) {
					if (col == 1) return true;
					return false;
				}

				// Now change the Flag Settings to comboboxes of the Flag Values, so user can reset flags
				public TableCellEditor getCellEditor(int row, int col) {
	                if (col != 1)
						return super.getCellEditor(row, col);
					String flagPossibles = (String) objReqFlagData[row][4];	// all possible flag values
					String[] flagList = flagPossibles.split(","); 			//$NON-NLS-1$
					for (int i = 0; i < flagList.length; i++) flagList[i] = "   " + flagList[i];	//$NON-NLS-1$
					DefaultComboBoxModel<String> comboFlagModel = new DefaultComboBoxModel<>(flagList);
					JComboBox<String> comboBoxFlag = new JComboBox<>(comboFlagModel);
					comboBoxFlag.addActionListener (new ActionListener() {
						public void actionPerformed(ActionEvent event) {
							int flagIdent = (Integer) objReqFlagData[row][3];
							int flagSetIndex = comboBoxFlag.getSelectedIndex();
							objReqFlagData[row][2] = flagSetIndex;
							pointPersonHandler.addToPersonFlagChangeList(flagIdent, flagSetIndex);
						}
					});
					return new DefaultCellEditor(comboBoxFlag);
	            }
			};

		tableFlags.getColumnModel().getColumn(0).setMinWidth(100);
		tableFlags.getColumnModel().getColumn(0).setPreferredWidth(190);
		tableFlags.getColumnModel().getColumn(1).setMinWidth(50);
		tableFlags.getColumnModel().getColumn(1).setPreferredWidth(110);
		tableFlags.setAutoCreateColumnsFromModel(false);	// preserve column setup
		JTableHeader flagHeader = tableFlags.getTableHeader();
		flagHeader.setOpaque(false);
		ListSelectionModel flagSelectionModel = tableFlags.getSelectionModel();
		flagSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	// Setup scrollpane and add to contents
		JScrollPane flagScrollPane = new JScrollPane(tableFlags);
		flagScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		flagScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	// Setup tabbing within table against all rows but only column 1
		JTableCellTabbing.setTabMapping(tableFlags, 0, tableFlags.getRowCount(), 1, 1);
		// Add to Flag panel
		panelFlags.add(flagScrollPane, "cell 0 1");	//$NON-NLS-1$
		// Add flagPanel to namePanelCol1
		panelNameCol1.add(panelFlags, "cell 0 1, aligny top");	//$NON-NLS-1$

	// If related use the sexIndex to set Birth Sex flag
		if (!unrelated)
			for (int i = 0; i < tableFlags.getRowCount(); i++) {
				// First, find flagIdent = 1 (Birth sex)
				if ((Integer) objReqFlagData[i][3] == 1) {				// this ID is for Birth Sex
					String allValues = (String) objReqFlagData[i][4];	// get all birthSex values list
					String[] values = allValues.split(",");				//$NON-NLS-1$
					String newValue = "   " + values[sexIndex];		// extract the required value	//$NON-NLS-1$
					flagModel.setValueAt(newValue, i, 1);			// save it back into the flagTable
					pointPersonHandler.addToPersonFlagChangeList(1, sexIndex); // record flag changes for save
				}
			}

	// Add a (hidden) Relationship sub-panel for use by HG0505 extended code
	//**********************************************************************
		panelNameRelate = new JPanel();
		panelNameRelate.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		panelNameRelate.setLayout(new MigLayout("insets 5", "[]", "[]5[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	// Add sub-panel to panelNameCol0
		panelNameCol0.add(panelNameRelate, "cell 0 1, aligny top, grow, hidemode 3");		//$NON-NLS-1$
		panelNameRelate.setVisible(false);

	// Add a Memo sub-panel for Memo text area (scrollable)
	//**************************************************
		JPanel panelNameMemo = new JPanel();
		panelNameMemo.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		panelNameMemo.setLayout(new MigLayout("insets 5", "[]", "[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_MemoN = new JLabel(memoText);
		lbl_MemoN.setFont(lbl_MemoN.getFont().deriveFont(lbl_MemoN.getFont().getStyle() | Font.BOLD));
		panelNameMemo.add(lbl_MemoN, "cell 0 0,alignx left");		//$NON-NLS-1$

		memoNameText = new JTextArea();
		memoNameText.setWrapStyleWord(true);
		memoNameText.setLineWrap(true);
		memoNameText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		memoNameText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)memoNameText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	    Font font = UIManager.getFont("TextArea.font");		//$NON-NLS-1$
	    memoNameText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
	    memoNameText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
	    memoNameText.setBorder(new JTable().getBorder());		// match Table border
	// Setup scrollpane with textarea
		JScrollPane memoNameScroll = new JScrollPane(memoNameText);
		memoNameScroll.setMinimumSize(new Dimension(320, 110)); // set a starter size
		memoNameScroll.getViewport().setOpaque(false);
		memoNameScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		memoNameText.setCaretPosition(0);	// set scrollbar to top
		panelNameMemo.add(memoNameScroll, "cell 0 1, grow");		//$NON-NLS-1$
	// Add memo sub-panel to panelNameCol1
		panelNameCol1.add(panelNameMemo, "cell 0 2, grow, aligny top");		//$NON-NLS-1$

	// Add to Name card
		cardName.add(panelNameCol0, "cell 0 0, aligny top");	//$NON-NLS-1$
		cardName.add(panelNameCol1, "cell 1 0, aligny top");	//$NON-NLS-1$

/****************************************
 * Setup cardBirth contents - panelBirth
 ***************************************/
	// Define Birth panel
		JPanel panelBirth = new JPanel();
		panelBirth.setLayout(new MigLayout("insets 5", "[]10[grow]", "[]10[]10[grow]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	// Create birth details sub-panel to hold the detailed info
	//*********************************************************
		JPanel panelBirthDetail = new JPanel();
		panelBirthDetail.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		panelBirthDetail.setLayout(new MigLayout("insets 5", "[][]", "[]10[]10[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_EventTypeBi = new JLabel(eventText);
		lbl_EventTypeBi.setFont(lbl_EventTypeBi.getFont().deriveFont(lbl_EventTypeBi.getFont().getStyle() | Font.BOLD));
		panelBirthDetail.add(lbl_EventTypeBi, "cell 0 0,alignx left");		//$NON-NLS-1$

	// Set datalanguage for event  role manager
		pointPersonHandler.pointEventRoleManager.setSelectedLanguage(HGlobal.dataLanguage);
	// Collect event types/roles for Birth, etc
		birthEventList = pointPersonHandler.getEventTypeList(birthEventGroup);
		//System.out.println(" birthEventList: " + birthEventList.length);
		birthEventType = pointPersonHandler.getEventTypes();
		DefaultComboBoxModel<String> birthEvents = new DefaultComboBoxModel<String>(birthEventList);
		comboBirthType = new JComboBox<String>(birthEvents);
		// Set the initial combobox index = 0, but try to match to against Birth event number
		comboBirthType.setSelectedIndex(0);
		for (int i = 0; i < birthEventList.length; i++) {
			if (birthEventNumber == pointPersonHandler.getEventTypes()[i]) {
					comboBirthType.setSelectedIndex(i);
					break;
			}
		}
		panelBirthDetail.add(comboBirthType, "cell 1 0");		//$NON-NLS-1$

		JLabel lbl_RoleBi = new JLabel(roleText);
		panelBirthDetail.add(lbl_RoleBi, "cell 0 1, alignx left");		//$NON-NLS-1$

		birthRoleList = pointPersonHandler.getRolesForEvent(birthEventNumber, selectBirthRoles);
		birthRoleType = pointPersonHandler.getEventRoleTypes();
		DefaultComboBoxModel<String> comboRoles	= new DefaultComboBoxModel<String>(birthRoleList);
		birthRoles = new JComboBox<String>(comboRoles);
		panelBirthDetail.add(birthRoles, "cell 1 1, alignx left");		//$NON-NLS-1$

	// and dates
		JLabel lbl_EventDateBi = new JLabel(eventDateText);
		panelBirthDetail.add(lbl_EventDateBi, "cell 0 2, alignx left");		//$NON-NLS-1$
		birthDateText = new JTextField();
		birthDateText.setColumns(22);
		birthDateText.setEditable(false);		// ensure field cannot be edited from keyboard
		birthDateText.setBackground(UIManager.getColor("TextField.background"));  //$NON-NLS-1$
		panelBirthDetail.add(birthDateText, "cell 1 2, alignx left");		//$NON-NLS-1$

		JLabel lbl_SortDateBi = new JLabel(sortDateText);
		panelBirthDetail.add(lbl_SortDateBi, "cell 0 3, alignx left");		//$NON-NLS-1$
		birthSortDateText = new JTextField();
		birthSortDateText.setColumns(22);
		birthSortDateText.setEditable(false);		// ensure field cannot be edited from keyboard
		birthSortDateText.setBackground(UIManager.getColor("TextField.background"));  //$NON-NLS-1$
		panelBirthDetail.add(birthSortDateText, "cell 1 3, alignx left");		//$NON-NLS-1$

	// Add Birth details sub-panel to Birth panel
		panelBirth.add(panelBirthDetail, "cell 0 0, aligny top");	//$NON-NLS-1$

	// Create Birth location scrollpane/table in Location sub-panel
	//*************************************************************
		JPanel panelBirthLocn = new JPanel();
		panelBirthLocn.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		panelBirthLocn.setLayout(new MigLayout("insets 5", "[]", "[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// Location style
		JLabel lbl_LocStyleBi = new JLabel(locStyleText);
		lbl_LocStyleBi.setFont(lbl_LocStyleBi.getFont().deriveFont(lbl_LocStyleBi.getFont().getStyle() | Font.BOLD));
		panelBirthLocn.add(lbl_LocStyleBi, "cell 0 0, align left");		//$NON-NLS-1$
		DefaultComboBoxModel<String> comboLocnModel
				= new DefaultComboBoxModel<>(pointPersonHandler.getLocationStyles());
		JComboBox<String> birthLocnStyle = new JComboBox<String>(comboLocnModel);
		birthLocnStyle.setSelectedIndex(pointPersonHandler.getDefaultLocationStyleIndex());
		panelBirthLocn.add(birthLocnStyle, "cell 0 0, gapx 10");		//$NON-NLS-1$

		JTable tableBirthLocn = new JTable() {
		private static final long serialVersionUID = 1L;
				public Dimension getPreferredScrollableViewportSize() {
				    return new Dimension(super.getPreferredSize().width,
				    		super.getRowCount() * super.getRowHeight());
				  }
				public boolean isCellEditable(int row, int col) {
					if (col == 1) return true;
					return false;
				}
				public boolean editCellAt(int row, int col, EventObject e) {
					if (col == 0) return false;
				    boolean result = super.editCellAt(row, col, e);
				    final Component editor = getEditorComponent();
				    if (e != null && e instanceof MouseEvent) {
				        ((JTextField)editor).requestFocus();
				        ((JTextField)editor).getCaret().setVisible(true);
				    }
				    return result;
				}
				public Component prepareRenderer(TableCellRenderer renderer, int row, int col)  {
					Component cell = super.prepareRenderer(renderer, row, col);
					// For the Selected cell we let the editor take over with no highlights
					if (col == 1) cell.setCursor(Cursor.getDefaultCursor());
					cell.setBackground((Color) UIManager.get("Table.background"));		//$NON-NLS-1$
					cell.setForeground((Color) UIManager.get("Table.foreground"));		//$NON-NLS-1$
					return cell;
				}
		};

		int defaultLocationNameStyle = pointPersonHandler.getDefaultLocationStyleIndex();
		birthLocnStyle.setSelectedIndex(defaultLocationNameStyle);
		tableBirthData = pointPersonHandler.getLocationDataTable(defaultLocationNameStyle);
		String[] tableBirthHeader = pointPersonHandler.getLocationTableHeader();
		tableBirthLocn.setModel(new DefaultTableModel(tableBirthData,tableBirthHeader));

		// Make table single-click editable
		((DefaultCellEditor) tableBirthLocn.getDefaultEditor(Object.class)).setClickCountToStart(1);
		// and make loss of focus on a cell terminate the edit
		tableBirthLocn.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);	//$NON-NLS-1$

	    tableBirthLocn.getColumnModel().getColumn(0).setMinWidth(50);
		tableBirthLocn.getColumnModel().getColumn(0).setPreferredWidth(120);
		tableBirthLocn.getColumnModel().getColumn(1).setMinWidth(100);
		tableBirthLocn.getColumnModel().getColumn(1).setPreferredWidth(250);
		tableBirthLocn.setAutoCreateColumnsFromModel(false);	// preserve column setup
		JTableHeader birthHeader = tableBirthLocn.getTableHeader();
		birthHeader.setOpaque(false);
		ListSelectionModel birthSelectionModel = tableBirthLocn.getSelectionModel();
		birthSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

	// Setup scrollpane and add to Birth panel
		tableBirthLocn.setFillsViewportHeight(true);
		JScrollPane birthScrollPane = new JScrollPane(tableBirthLocn);
		birthScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		birthScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
	// Setup tabbing within table against all rows but only column 1
		JTableCellTabbing.setTabMapping(tableBirthLocn, 0, tableBirthLocn.getRowCount(), 1, 1);
	// Add locn scrollpane to Locn sub-panel
		panelBirthLocn.add(birthScrollPane, "cell 0 1, aligny top");		//$NON-NLS-1$
	// Add Locn sub-panel to Birth panel
		panelBirth.add(panelBirthLocn, "cell 1 0 1 2, aligny top");	//$NON-NLS-1$

	// Create Memo label and text area (scrollable) in a Memo sub-panel
	//***************************************************************
		JPanel panelBirthMemo = new JPanel();
		panelBirthMemo.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		panelBirthMemo.setLayout(new MigLayout("insets 5", "[grow]", "[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		JLabel lbl_MemoBi = new JLabel(memoText);
		lbl_MemoBi.setFont(lbl_MemoBi.getFont().deriveFont(lbl_MemoBi.getFont().getStyle() | Font.BOLD));
		panelBirthMemo.add(lbl_MemoBi, "cell 0 0,alignx left");		//$NON-NLS-1$
		memoBirthText = new JTextArea();
		memoBirthText.setWrapStyleWord(true);
		memoBirthText.setLineWrap(true);
		memoBirthText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		memoBirthText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)memoBirthText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	    memoBirthText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
	    memoBirthText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
	    memoBirthText.setBorder(new JTable().getBorder());		// match Table border
	// Setup scrollpane with textarea
		JScrollPane memoBirthScroll = new JScrollPane(memoBirthText);
		memoBirthScroll.setMinimumSize(new Dimension(300, 110)); // set a starter size
		memoBirthScroll.getViewport().setOpaque(false);
		memoBirthScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		memoBirthText.setCaretPosition(0);	// set scrollbar to top
	// Add scrollpane to Memo sub-panel
		panelBirthMemo.add(memoBirthScroll, "cell 0 1, aligny top");		//$NON-NLS-1$
	// Add Memo sub-panel to Birth panel
		panelBirth.add(panelBirthMemo, "cell 0 1");	//$NON-NLS-1$

	// Add a Sentence Edit button
		JButton btn_SentenceBirth = new JButton(sentenceEditor);
		panelBirth.add(btn_SentenceBirth, "cell 0 2, aligny center, alignx center");		//$NON-NLS-1$

	// Add Birth panel to cardBirth
		cardBirth.add(panelBirth, "cell 0 0, aligny top");		//$NON-NLS-1$

/****************************************
* Setup cardBapt contents - panelBapt
****************************************/
	// Define Bapt panel
		JPanel panelBapt = new JPanel();
		panelBapt.setLayout(new MigLayout("insets 5", "[]10[grow]", "[]10[]10[grow]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	// Create baptism details sub-panel to hold the detailed info
	//***********************************************************
		JPanel panelBaptDetail = new JPanel();
		panelBaptDetail.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		panelBaptDetail.setLayout(new MigLayout("insets 5", "[][]", "[]10[]10[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_EventTypeBa = new JLabel(eventText);
		lbl_EventTypeBa.setFont(lbl_EventTypeBa.getFont().deriveFont(lbl_EventTypeBa.getFont().getStyle() | Font.BOLD));
		panelBaptDetail.add(lbl_EventTypeBa, "cell 0 0,alignx left");		//$NON-NLS-1$

	// Collect event types for Baptism
		baptEventList = pointPersonHandler.getEventTypeList(baptEventGroup);
		baptEventType = pointPersonHandler.getEventTypes();

		DefaultComboBoxModel<String> baptEvents = new DefaultComboBoxModel<String>(baptEventList);
		comboBaptType = new JComboBox<String>(baptEvents);
		// Set the initial combobox index = 0, but try to match to against Baptism event number
		comboBaptType.setSelectedIndex(0);
		for (int i = 0; i < baptEventList.length; i++) {
			if (baptEventNumber == pointPersonHandler.getEventTypes()[i]) {
					comboBaptType.setSelectedIndex(i);
					break;
			}
		}
		panelBaptDetail.add(comboBaptType, "cell 1 0");		//$NON-NLS-1$

		JLabel lbl_RoleBa = new JLabel(roleText);
		panelBaptDetail.add(lbl_RoleBa, "cell 0 1, alignx left");		//$NON-NLS-1$

		baptRoleList = pointPersonHandler.getRolesForEvent(baptEventNumber, selectBaptRoles);
		baptRoleType = pointPersonHandler.getEventRoleTypes();
		DefaultComboBoxModel<String> comboBaptRoles = new DefaultComboBoxModel<String>(baptRoleList);
		baptRoles = new JComboBox<String>(comboBaptRoles);
		panelBaptDetail.add(baptRoles, "cell 1 1, alignx left");		//$NON-NLS-1$

	// and dates
		JLabel lbl_EventDateBa = new JLabel(eventDateText);
		panelBaptDetail.add(lbl_EventDateBa, "cell 0 2, alignx left");		//$NON-NLS-1$
		baptDateText = new JTextField();
		baptDateText.setColumns(22);
		baptDateText.setEditable(false);		// ensure field cannot be edited from keyboard
		baptDateText.setBackground(UIManager.getColor("TextField.background"));  //$NON-NLS-1$
		panelBaptDetail.add(baptDateText, "cell 1 2, alignx left");		//$NON-NLS-1$

		JLabel lbl_SortDateBa = new JLabel(sortDateText);
		panelBaptDetail.add(lbl_SortDateBa, "cell 0 3, alignx left");		//$NON-NLS-1$
		baptSortDateText = new JTextField();
		baptSortDateText.setColumns(22);
		baptSortDateText.setEditable(false);		// ensure field cannot be edited from keyboard
		baptSortDateText.setBackground(UIManager.getColor("TextField.background"));  //$NON-NLS-1$
		panelBaptDetail.add(baptSortDateText, "cell 1 3, alignx left");		//$NON-NLS-1$

	// Add Bapth details sub-panel to Bapt panel
		panelBapt.add(panelBaptDetail, "cell 0 0, aligny top");	//$NON-NLS-1$

	// Create Baptism location scrollpane/table in Location sub-panel
	//***************************************************************
		JPanel panelBaptLocn = new JPanel();
		panelBaptLocn.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		panelBaptLocn.setLayout(new MigLayout("insets 5", "[]", "[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_LocStyleBa = new JLabel(locStyleText);
		lbl_LocStyleBa.setFont(lbl_EventTypeBa.getFont().deriveFont(lbl_EventTypeBa.getFont().getStyle() | Font.BOLD));
		panelBaptLocn.add(lbl_LocStyleBa, "cell 0 0, align left");		//$NON-NLS-1$

		JComboBox<String> baptLocnStyle = new JComboBox<String>(comboLocnModel);
		baptLocnStyle.setSelectedIndex(pointPersonHandler.getDefaultLocationStyleIndex());
		panelBaptLocn.add(baptLocnStyle, "cell 0 0, gapx 10");		//$NON-NLS-1$

	// Create scrollpane and table for the Bapt location data
		JTable tableBaptLocn = new JTable() {
		private static final long serialVersionUID = 1L;
				public Dimension getPreferredScrollableViewportSize() {
				    return new Dimension(super.getPreferredSize().width,
				    		super.getRowCount() * super.getRowHeight());
				  }
				public boolean isCellEditable(int row, int col) {
					if (col == 1) return true;
					return false;
				}
				public boolean editCellAt(int row, int col, EventObject e) {
					if (col == 0) return false;
				    boolean result = super.editCellAt(row, col, e);
				    final Component editor = getEditorComponent();
				    if (e != null && e instanceof MouseEvent) {
				        ((JTextField)editor).requestFocus();
				        ((JTextField)editor).getCaret().setVisible(true);
				    }
				    return result;
				}
				public Component prepareRenderer(TableCellRenderer renderer, int row, int col)  {
					Component cell = super.prepareRenderer(renderer, row, col);
					// For the Selected cell we let the editor take over with no highlights
					if (col == 1) cell.setCursor(Cursor.getDefaultCursor());
					cell.setBackground((Color) UIManager.get("Table.background"));		//$NON-NLS-1$
					cell.setForeground((Color) UIManager.get("Table.foreground"));		//$NON-NLS-1$
					return cell;
				}
		};

		int defaultBaptLocationNameStyle = pointPersonHandler.getDefaultLocationStyleIndex();
		baptLocnStyle.setSelectedIndex(defaultBaptLocationNameStyle);
		tableBaptData = pointPersonHandler.getLocationDataTable(defaultLocationNameStyle);
		String[] tableBaptHeader = pointPersonHandler.getLocationTableHeader();
		tableBaptLocn.setModel(new DefaultTableModel(tableBaptData,tableBaptHeader));

		// Make table single-click editable
		((DefaultCellEditor) tableBaptLocn.getDefaultEditor(Object.class)).setClickCountToStart(1);
		// and make loss of focus on a cell terminate the edit
		tableBaptLocn.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);	//$NON-NLS-1$

	    tableBaptLocn.getColumnModel().getColumn(0).setMinWidth(50);
		tableBaptLocn.getColumnModel().getColumn(0).setPreferredWidth(120);
		tableBaptLocn.getColumnModel().getColumn(1).setMinWidth(100);
		tableBaptLocn.getColumnModel().getColumn(1).setPreferredWidth(250);
		tableBaptLocn.setAutoCreateColumnsFromModel(false);	// preserve column setup
		JTableHeader baptHeader = tableBaptLocn.getTableHeader();
		baptHeader.setOpaque(false);
		ListSelectionModel baptSelectionModel = tableBaptLocn.getSelectionModel();
		baptSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

	// Setup scrollpane and add to Bapt panel
		tableBaptLocn.setFillsViewportHeight(true);
		JScrollPane baptScrollPane = new JScrollPane(tableBaptLocn);
		baptScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		baptScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
	// Setup tabbing within table against all rows but only column 1
		JTableCellTabbing.setTabMapping(tableBaptLocn, 0, tableBaptLocn.getRowCount(), 1, 1);
	// Add locn scrollpane to Locn sub-panel
		panelBaptLocn.add(baptScrollPane, "cell 0 1, aligny top");		//$NON-NLS-1$
	// Add Location sub-panel to Bapt panel
		panelBapt.add(panelBaptLocn, "cell 1 0 1 2, aligny top");	//$NON-NLS-1$

	// Create Memo label and text area (scrollable) in a Memo sub-panel
	//***************************************************************
		JPanel panelBaptMemo = new JPanel();
		panelBaptMemo.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		panelBaptMemo.setLayout(new MigLayout("insets 5", "[grow]", "[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_MemoBa = new JLabel(memoText);
		lbl_MemoBa.setFont(lbl_MemoBa.getFont().deriveFont(lbl_MemoBa.getFont().getStyle() | Font.BOLD));
		panelBaptMemo.add(lbl_MemoBa, "cell 0 0,alignx left");		//$NON-NLS-1$
		memoBaptText = new JTextArea();
		memoBaptText.setWrapStyleWord(true);
		memoBaptText.setLineWrap(true);
		memoBaptText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		memoBaptText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)memoBaptText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	    memoBaptText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
	    memoBaptText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
	    memoBaptText.setBorder(new JTable().getBorder());		// match Table border
	// Setup scrollpane with textarea
		JScrollPane memoBaptScroll = new JScrollPane(memoBaptText);
		memoBaptScroll.setMinimumSize(new Dimension(300, 110)); // set a starter size
		memoBaptScroll.getViewport().setOpaque(false);
		memoBaptScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		memoBaptText.setCaretPosition(0);	// set scrollbar to top
	// Add scrollpane to Memo sub-panel
		panelBaptMemo.add(memoBaptScroll, "cell 0 1, aligny top");		//$NON-NLS-1$
	// Add Memo sub-panel to Bapt panel
		panelBapt.add(panelBaptMemo, "cell 0 1");	//$NON-NLS-1$

	// Add a Sentence Edit button
		JButton btn_SentenceBapt = new JButton(sentenceEditor);
		panelBapt.add(btn_SentenceBapt, "cell 0 2, align center");		//$NON-NLS-1$

	// Add Bapt panel to cardBapt
		cardBapt.add(panelBapt, "cell 0 0, aligny top");		//$NON-NLS-1$

/****************************************
* Setup cardDeath contents - panelDeath
***************************************/
	// Define Death panel
		JPanel panelDeath = new JPanel();
		panelDeath.setLayout(new MigLayout("insets 5", "[]10[grow]", "[]10[]10[grow]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	// Create Death details sub-panel to hold the detailed info
	//*********************************************************
		JPanel panelDeathDetail = new JPanel();
		panelDeathDetail.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		panelDeathDetail.setLayout(new MigLayout("insets 5", "[][]", "[]10[]10[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_EventTypeD = new JLabel(eventText);
		lbl_EventTypeD.setFont(lbl_EventTypeD.getFont().deriveFont(lbl_EventTypeD.getFont().getStyle() | Font.BOLD));
		panelDeathDetail.add(lbl_EventTypeD, "cell 0 0, alignx left");		//$NON-NLS-1$

	// Collect event types for Death
		deathEventList = pointPersonHandler.getEventTypeList(deathEventGroup);
		deathEventType = pointPersonHandler.getEventTypes();

		DefaultComboBoxModel<String> deathEvents = new DefaultComboBoxModel<String>(deathEventList);
		comboDeathType = new JComboBox<String>(deathEvents);
		// Set the initial combobox index = 0, but try to match to against Death event number
		comboDeathType.setSelectedIndex(0);
		for (int i = 0; i < deathEventList.length; i++) {
			if (deathEventNumber == pointPersonHandler.getEventTypes()[i]) {
					comboDeathType.setSelectedIndex(i);
					break;
			}
		}
		panelDeathDetail.add(comboDeathType, "cell 1 0");		//$NON-NLS-1$

		JLabel lbl_RoleD = new JLabel(roleText);
		panelDeathDetail.add(lbl_RoleD, "cell 0 1, alignx left");		//$NON-NLS-1$

	// Find death roles
		deathRoleList = pointPersonHandler.getRolesForEvent(deathEventNumber, selectDeathRoles);
		deathRoleType = pointPersonHandler.getEventRoleTypes();
		DefaultComboBoxModel<String> comboDeathRoles = new DefaultComboBoxModel<String>(deathRoleList);
		deathRoles = new JComboBox<String>(comboDeathRoles);
		panelDeathDetail.add(deathRoles, "cell 1 1, alignx left");		//$NON-NLS-1$

	// and dates
		JLabel lbl_EventDateD = new JLabel(eventDateText);
		panelDeathDetail.add(lbl_EventDateD, "cell 0 2, alignx left");		//$NON-NLS-1$
		deathDateText = new JTextField();
		deathDateText.setColumns(22);
		deathDateText.setEditable(false);		// ensure field cannot be edited from keyboard
		deathDateText.setBackground(UIManager.getColor("TextField.background"));  //$NON-NLS-1$
		panelDeathDetail.add(deathDateText, "cell 1 2, alignx left");		//$NON-NLS-1$

		JLabel lbl_SortDateD = new JLabel(sortDateText);
		panelDeathDetail.add(lbl_SortDateD, "cell 0 3, alignx left");		//$NON-NLS-1$
		deathSortDateText = new JTextField();
		deathSortDateText.setColumns(22);
		deathSortDateText.setEditable(false);		// ensure field cannot be edited from keyboard
		deathSortDateText.setBackground(UIManager.getColor("TextField.background"));  //$NON-NLS-1$
		panelDeathDetail.add(deathSortDateText, "cell 1 3, alignx left");		//$NON-NLS-1$

	// Add Death details sub-panel to Death panel
		panelDeath.add(panelDeathDetail, "cell 0 0, aligny top");	//$NON-NLS-1$

	// Create Death location scrollpane/table in Location sub-panel
	//*************************************************************
		JPanel panelDeathLocn = new JPanel();
		panelDeathLocn.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		panelDeathLocn.setLayout(new MigLayout("insets 5", "[]", "[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_LocStyleD = new JLabel(locStyleText);
		lbl_LocStyleD.setFont(lbl_LocStyleD.getFont().deriveFont(lbl_LocStyleD.getFont().getStyle() | Font.BOLD));
		panelDeathLocn.add(lbl_LocStyleD, "cell 0 0, alignx left");		//$NON-NLS-1$

		JComboBox<String> deathLocnStyle = new JComboBox<String>(comboLocnModel);
		deathLocnStyle.setSelectedIndex(pointPersonHandler.getDefaultLocationStyleIndex());
		panelDeathLocn.add(deathLocnStyle, "cell 0 0, gapx 10");		//$NON-NLS-1$

	// Create scrollpane and table for the Death location data
		JTable tableDeathLocn = new JTable() {
		private static final long serialVersionUID = 1L;
				public Dimension getPreferredScrollableViewportSize() {
				    return new Dimension(super.getPreferredSize().width,
				    		super.getRowCount() * super.getRowHeight());
				  }
				public boolean isCellEditable(int row, int col) {
					if (col == 1) return true;
					return false;
				}
				public boolean editCellAt(int row, int col, EventObject e) {
					if (col == 0) return false;
				    boolean result = super.editCellAt(row, col, e);
				    final Component editor = getEditorComponent();
				    if (e != null && e instanceof MouseEvent) {
				        ((JTextField)editor).requestFocus();
				        ((JTextField)editor).getCaret().setVisible(true);
				    }
				    return result;
				}
				public Component prepareRenderer(TableCellRenderer renderer, int row, int col)  {
					Component cell = super.prepareRenderer(renderer, row, col);
					// For the Selected cell we let the editor take over with no highlights
					if (col == 1) cell.setCursor(Cursor.getDefaultCursor());
					cell.setBackground((Color) UIManager.get("Table.background"));		//$NON-NLS-1$
					cell.setForeground((Color) UIManager.get("Table.foreground"));		//$NON-NLS-1$
					return cell;
				}
		};

		int defaultDeathLocationNameStyle = pointPersonHandler.getDefaultLocationStyleIndex();
		deathLocnStyle.setSelectedIndex(defaultDeathLocationNameStyle);
		tableDeathData = pointPersonHandler.getLocationDataTable(defaultLocationNameStyle);
		String[] tableDeathHeader = pointPersonHandler.getLocationTableHeader();
		tableDeathLocn.setModel(new DefaultTableModel(tableDeathData,tableDeathHeader));

		// Make table single-click editable
		((DefaultCellEditor) tableDeathLocn.getDefaultEditor(Object.class)).setClickCountToStart(1);
		// and make loss of focus on a cell terminate the edit
		tableDeathLocn.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);	//$NON-NLS-1$

	    tableDeathLocn.getColumnModel().getColumn(0).setMinWidth(50);
		tableDeathLocn.getColumnModel().getColumn(0).setPreferredWidth(120);
		tableDeathLocn.getColumnModel().getColumn(1).setMinWidth(100);
		tableDeathLocn.getColumnModel().getColumn(1).setPreferredWidth(250);
		tableDeathLocn.setAutoCreateColumnsFromModel(false);	// preserve column setup
		JTableHeader deathHeader = tableDeathLocn.getTableHeader();
		deathHeader.setOpaque(false);
		ListSelectionModel deathSelectionModel = tableDeathLocn.getSelectionModel();
		deathSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

	// Setup scrollpane and add to Death panel
		tableDeathLocn.setFillsViewportHeight(true);
		JScrollPane deathScrollPane = new JScrollPane(tableDeathLocn);
		deathScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		deathScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
	// Setup tabbing within table against all rows but only column 1
		JTableCellTabbing.setTabMapping(tableDeathLocn, 0, tableDeathLocn.getRowCount(), 1, 1);
	// Add locn scrollpane to Locn sub-panel
		panelDeathLocn.add(deathScrollPane, "cell 0 1, aligny top");		//$NON-NLS-1$
	// Add Locn sub-panel to Death panel
		panelDeath.add(panelDeathLocn, "cell 1 0 1 2, aligny top");	//$NON-NLS-1$

	// Create Memo label and text area (scrollable) in a Memo sub-panel
	//***************************************************************
		JPanel panelDeathMemo = new JPanel();
		panelDeathMemo.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		panelDeathMemo.setLayout(new MigLayout("insets 5", "[grow]", "[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		JLabel lbl_MemoD = new JLabel(memoText);
		lbl_MemoD.setFont(lbl_MemoD.getFont().deriveFont(lbl_MemoD.getFont().getStyle() | Font.BOLD));
		panelDeathMemo.add(lbl_MemoD, "cell 0 0, alignx left");		//$NON-NLS-1$
		memoDeathText = new JTextArea();
		memoDeathText.setWrapStyleWord(true);
		memoDeathText.setLineWrap(true);
		memoDeathText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		memoDeathText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)memoDeathText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	    memoDeathText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
	    memoDeathText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
	    memoDeathText.setBorder(new JTable().getBorder());		// match Table border
	// Setup scrollpane with textarea
		JScrollPane memoDeathScroll = new JScrollPane(memoDeathText);
		memoDeathScroll.setMinimumSize(new Dimension(300, 110)); // set a starter size
		memoDeathScroll.getViewport().setOpaque(false);
		memoDeathScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		memoDeathText.setCaretPosition(0);	// set scrollbar to top
	// Add scrollpane to Memo sub-panel
		panelDeathMemo.add(memoDeathScroll, "cell 0 1, aligny top");		//$NON-NLS-1$
	// Add Memo sub-panel to Death panel
		panelDeath.add(panelDeathMemo, "cell 0 1");	//$NON-NLS-1$

	// Add a Sentence Edit button
		JButton btn_SentenceDeath = new JButton(sentenceEditor);
		panelDeath.add(btn_SentenceDeath, "cell 0 2, align center");		//$NON-NLS-1$

	// Add Death panel to cardDeath
		cardDeath.add(panelDeath, "cell 0 0, aligny top");		//$NON-NLS-1$

/****************************************
* Setup cardBurial contents - panelBurial
****************************************/
	// Define Burial panel
		JPanel panelBurial = new JPanel();
		panelBurial.setLayout(new MigLayout("insets 5", "[]10[grow]", "[]10[]10[grow]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	// Create Burial details sub-panel to hold the detailed info
	//*********************************************************
		JPanel panelBurialDetail = new JPanel();
		panelBurialDetail.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		panelBurialDetail.setLayout(new MigLayout("insets 5", "[][]", "[]10[]10[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_EventTypeBu = new JLabel(eventText);
		lbl_EventTypeBu.setFont(lbl_EventTypeBu.getFont().deriveFont(lbl_EventTypeBu.getFont().getStyle() | Font.BOLD));
		panelBurialDetail.add(lbl_EventTypeBu, "cell 0 0, alignx left");		//$NON-NLS-1$

	// Collect event types for Burial
		burialEventList = pointPersonHandler.getEventTypeList(burialEventGroup);
		burialEventType = pointPersonHandler.getEventTypes();

		DefaultComboBoxModel<String> burialEvents = new DefaultComboBoxModel<String>(burialEventList);
		comboBurialType = new JComboBox<String>(burialEvents);
		// Set the initial combobox index = 0, but try to match to against Burial event number
		comboBurialType.setSelectedIndex(0);
		for (int i = 0; i < burialEventList.length; i++) {
			if (burialEventNumber == pointPersonHandler.getEventTypes()[i]) {
					comboBurialType.setSelectedIndex(i);
					break;
			}
		}
		panelBurialDetail.add(comboBurialType, "cell 1 0");		//$NON-NLS-1$

		JLabel lbl_RoleBu = new JLabel(roleText);
		panelBurialDetail.add(lbl_RoleBu, "cell 0 1, alignx left");		//$NON-NLS-1$

		burialRoleList = pointPersonHandler.getRolesForEvent(burialEventNumber, selectBurialRoles);
		burialRoleType = pointPersonHandler.getEventRoleTypes();
		DefaultComboBoxModel<String> comboBurialRoles = new DefaultComboBoxModel<String>(burialRoleList);
		burialRoles = new JComboBox<String>(comboBurialRoles);
		panelBurialDetail.add(burialRoles, "cell 1 1, alignx left");		//$NON-NLS-1$

	// and Dates
		JLabel lbl_EventDateBu = new JLabel(eventDateText);
		panelBurialDetail.add(lbl_EventDateBu, "cell 0 2, alignx left");		//$NON-NLS-1$
		burialDateText = new JTextField();
		burialDateText.setColumns(22);
		burialDateText.setEditable(false);		// ensure field cannot be edited from keyboard
		burialDateText.setBackground(UIManager.getColor("TextField.background"));  //$NON-NLS-1$
		panelBurialDetail.add(burialDateText, "cell 1 2, alignx left");		//$NON-NLS-1$

		JLabel lbl_SortDateBu = new JLabel(sortDateText);
		panelBurialDetail.add(lbl_SortDateBu, "cell 0 3, alignx left");		//$NON-NLS-1$
		burialSortDateText = new JTextField();
		burialSortDateText.setColumns(22);
		burialSortDateText.setEditable(false);		// ensure field cannot be edited from keyboard
		burialSortDateText.setBackground(UIManager.getColor("TextField.background"));  //$NON-NLS-1$
		panelBurialDetail.add(burialSortDateText, "cell 1 3, alignx left");		//$NON-NLS-1$

	// Add Burial details sub-panel to Burial panel
		panelBurial.add(panelBurialDetail, "cell 0 0, aligny top");	//$NON-NLS-1$

	// Create Burial location scrollpane/table in Location sub-panel
	//**************************************************************
		JPanel panelBurialLocn = new JPanel();
		panelBurialLocn.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		panelBurialLocn.setLayout(new MigLayout("insets 5", "[]", "[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	// Burial Location Style
		JLabel lbl_LocStyleBu = new JLabel(locStyleText);
		lbl_LocStyleBu.setFont(lbl_LocStyleBu.getFont().deriveFont(lbl_LocStyleBu.getFont().getStyle() | Font.BOLD));
		panelBurialLocn.add(lbl_LocStyleBu, "cell 0 0, alignx left");		//$NON-NLS-1$

		JComboBox<String> burialLocnStyle = new JComboBox<String>(comboLocnModel);
		burialLocnStyle.setSelectedIndex(pointPersonHandler.getDefaultLocationStyleIndex());
		panelBurialLocn.add(burialLocnStyle, "cell 0 0, gapx 10");		//$NON-NLS-1$

	// Create scrollpane and table for the Burial location data
		JTable tableBurialLocn = new JTable() {
		private static final long serialVersionUID = 1L;
				public Dimension getPreferredScrollableViewportSize() {
				    return new Dimension(super.getPreferredSize().width,
				    		super.getRowCount() * super.getRowHeight());
				  }
				public boolean isCellEditable(int row, int col) {
					if (col == 1) return true;
					return false;
				}
				public boolean editCellAt(int row, int col, EventObject e) {
					if (col == 0) return false;
				    boolean result = super.editCellAt(row, col, e);
				    final Component editor = getEditorComponent();
				    if (e != null && e instanceof MouseEvent) {
				        ((JTextField)editor).requestFocus();
				        ((JTextField)editor).getCaret().setVisible(true);
				    }
				    return result;
				}
				public Component prepareRenderer(TableCellRenderer renderer, int row, int col)  {
					Component cell = super.prepareRenderer(renderer, row, col);
					// For the Selected cell we let the editor take over with no highlights
					if (col == 1) cell.setCursor(Cursor.getDefaultCursor());
					cell.setBackground((Color) UIManager.get("Table.background"));		//$NON-NLS-1$
					cell.setForeground((Color) UIManager.get("Table.foreground"));		//$NON-NLS-1$
					return cell;
				}
		};

		int defaultBurialLocationNameStyle = pointPersonHandler.getDefaultLocationStyleIndex();
		burialLocnStyle.setSelectedIndex(defaultBurialLocationNameStyle);
		tableBurialData = pointPersonHandler.getLocationDataTable(defaultLocationNameStyle);
		String[] tableBurialHeader = pointPersonHandler.getLocationTableHeader();
		tableBurialLocn.setModel(new DefaultTableModel(tableBurialData,tableBurialHeader));

		// Make table single-click editable
		((DefaultCellEditor) tableBurialLocn.getDefaultEditor(Object.class)).setClickCountToStart(1);
		// and make loss of focus on a cell terminate the edit
		tableBurialLocn.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);	//$NON-NLS-1$

	    tableBurialLocn.getColumnModel().getColumn(0).setMinWidth(50);
		tableBurialLocn.getColumnModel().getColumn(0).setPreferredWidth(120);
		tableBurialLocn.getColumnModel().getColumn(1).setMinWidth(100);
		tableBurialLocn.getColumnModel().getColumn(1).setPreferredWidth(250);
		tableBurialLocn.setAutoCreateColumnsFromModel(false);	// preserve column setup
		JTableHeader burialHeader = tableBurialLocn.getTableHeader();
		burialHeader.setOpaque(false);
		ListSelectionModel burialSelectionModel = tableBurialLocn.getSelectionModel();
		burialSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

	// Setup scrollpane and add to Burial panel
		tableBurialLocn.setFillsViewportHeight(true);
		JScrollPane burialScrollPane = new JScrollPane(tableBurialLocn);
		burialScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		burialScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
	// Setup tabbing within table against all rows but only column 1
		JTableCellTabbing.setTabMapping(tableBurialLocn, 0, tableBurialLocn.getRowCount(), 1, 1);
	// Add locn scrollpane to Locn sub-panel
		panelBurialLocn.add(burialScrollPane, "cell 0 1, aligny top");		//$NON-NLS-1$
	// Add Locn sub-panel to Burial panel
		panelBurial.add(panelBurialLocn, "cell 1 0 1 2, aligny top");	//$NON-NLS-1$

	// Create Memo label and text area (scrollable) in a Memo sub-panel
	//***************************************************************
		JPanel panelBurialMemo = new JPanel();
		panelBurialMemo.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		panelBurialMemo.setLayout(new MigLayout("insets 5", "[grow]", "[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_MemoBu = new JLabel(memoText);
		lbl_MemoBu.setFont(lbl_MemoBu.getFont().deriveFont(lbl_MemoBu.getFont().getStyle() | Font.BOLD));
		panelBurialMemo.add(lbl_MemoBu, "cell 0 0, alignx left");		//$NON-NLS-1$
		memoBurialText = new JTextArea();
		memoBurialText.setWrapStyleWord(true);
		memoBurialText.setLineWrap(true);
		memoBurialText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		memoBurialText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)memoBurialText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	    memoBurialText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
	    memoBurialText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
	    memoBurialText.setBorder(new JTable().getBorder());		// match Table border
	// Setup scrollpane with textarea
		JScrollPane memoBurialScroll = new JScrollPane(memoBurialText);
		memoBurialScroll.setMinimumSize(new Dimension(300, 110)); // set a starter size
		memoBurialScroll.getViewport().setOpaque(false);
		memoBurialScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		memoBurialText.setCaretPosition(0);	// set scrollbar to top
	// Add scrollpane to Memo sub-panel
		panelBurialMemo.add(memoBurialScroll, "cell 0 1, aligny top");		//$NON-NLS-1$
	// Add Memo sub-panel to Burial panel
		panelBurial.add(panelBurialMemo, "cell 0 1");	//$NON-NLS-1$

	// Add a Sentence Edit button
		JButton btn_SentenceBurial = new JButton(sentenceEditor);
		panelBurial.add(btn_SentenceBurial, "cell 0 2, align center");		//$NON-NLS-1$

	// Add Burial panel to cardBurial
		cardBurial.add(panelBurial, "cell 0 0, aligny top");		//$NON-NLS-1$

/********************************************
* Setup cardPartner contents - panelPartner
********************************************/
	// Add Partner panel
		JPanel panelPartner = new JPanel();
		panelPartner.setLayout(new MigLayout("insets 5", "[]10[grow]", "[]10[]10[grow]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	// Create Partner details sub-panel to hold the detailed info
	//*********************************************************
		JPanel panelPartnerDetail = new JPanel();
		panelPartnerDetail.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		panelPartnerDetail.setLayout(new MigLayout("insets 5", "[][]", "[]12[]12[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	// Set JLabel for selected partner event type
		JLabel lbl_EventTypeP = new JLabel(eventText);
		lbl_EventTypeP.setFont(lbl_EventTypeP.getFont().deriveFont(lbl_EventTypeP.getFont().getStyle() | Font.BOLD));
		panelPartnerDetail.add(lbl_EventTypeP, "cell 0 0, alignx left");	//$NON-NLS-1$

		panelPartnerDetail.add(lbl_ChosenPartnerEvent, "cell 1 0");		//$NON-NLS-1$

	// Set JLabel for selected partner event role
		JLabel lbl_PartnerRole2 = new JLabel(HG0505Msgs.Text_25);		// New Partner's Role:
		panelPartnerDetail.add(lbl_PartnerRole2, "cell 0 1 2, alignx left");		//$NON-NLS-1$
		panelPartnerDetail.add(lbl_ChosenPartnerRole2, "cell 0 1 2, gapx 10");	//$NON-NLS-1$

	// and dates
		JLabel lbl_EventDateP = new JLabel(eventDateText);
		panelPartnerDetail.add(lbl_EventDateP, "cell 0 2, alignx left");		//$NON-NLS-1$
		partnerDateText = new JTextField();
		partnerDateText.setColumns(22);
		partnerDateText.setEditable(false);		// ensure field cannot be edited from keyboard
		partnerDateText.setBackground(UIManager.getColor("TextField.background"));  //$NON-NLS-1$
		panelPartnerDetail.add(partnerDateText, "cell 1 2, alignx left");		//$NON-NLS-1$

		JLabel lbl_SortDateP = new JLabel(sortDateText);
		panelPartnerDetail.add(lbl_SortDateP, "cell 0 3, alignx left");		//$NON-NLS-1$
		partnerSortDateText = new JTextField();
		partnerSortDateText.setColumns(22);
		partnerSortDateText.setEditable(false);		// ensure field cannot be edited from keyboard
		partnerSortDateText.setBackground(UIManager.getColor("TextField.background"));  //$NON-NLS-1$
		panelPartnerDetail.add(partnerSortDateText, "cell 1 3, alignx left");		//$NON-NLS-1$

	// Add Partner details sub-panel to Partner panel
		panelPartner.add(panelPartnerDetail, "cell 0 0, aligny top");	//$NON-NLS-1$

	// Create Partner event location scrollpane/table in Location sub-panel
	//*********************************************************************
		JPanel panelPartnerLocn = new JPanel();
		panelPartnerLocn.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		panelPartnerLocn.setLayout(new MigLayout("insets 5", "[]", "[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	//  Location style
		JLabel lbl_LocStyleP = new JLabel(locStyleText);
		lbl_LocStyleP.setFont(lbl_LocStyleP.getFont().deriveFont(lbl_LocStyleP.getFont().getStyle() | Font.BOLD));
		panelPartnerLocn.add(lbl_LocStyleP, "cell 0 0, alignx left");		//$NON-NLS-1$

		JComboBox<String> partnerLocnStyle = new JComboBox<String>(comboLocnModel);
		partnerLocnStyle.setSelectedIndex(pointPersonHandler.getDefaultLocationStyleIndex());
		panelPartnerLocn.add(partnerLocnStyle, "cell 0 0, gapx 10");		//$NON-NLS-1$

	// Create scrollpane and table for the Partner location data
		JTable tablePartnerLocn = new JTable() {
		private static final long serialVersionUID = 1L;
				public Dimension getPreferredScrollableViewportSize() {
				    return new Dimension(super.getPreferredSize().width,
				    		super.getRowCount() * super.getRowHeight());
				  }
				public boolean isCellEditable(int row, int col) {
					if (col == 1) return true;
					return false;
				}
				public boolean editCellAt(int row, int col, EventObject e) {
					if (col == 0) return false;
				    boolean result = super.editCellAt(row, col, e);
				    final Component editor = getEditorComponent();
				    if (e != null && e instanceof MouseEvent) {
				        ((JTextField)editor).requestFocus();
				        ((JTextField)editor).getCaret().setVisible(true);
				    }
				    return result;
				}
				public Component prepareRenderer(TableCellRenderer renderer, int row, int col)  {
					Component cell = super.prepareRenderer(renderer, row, col);
					// For the Selected cell we let the editor take over with no highlights
					if (col == 1) cell.setCursor(Cursor.getDefaultCursor());
					cell.setBackground((Color) UIManager.get("Table.background"));		//$NON-NLS-1$
					cell.setForeground((Color) UIManager.get("Table.foreground"));		//$NON-NLS-1$
					return cell;
				}
		};

		int defaultPartnerLocationNameStyle = pointPersonHandler.getDefaultLocationStyleIndex();
		partnerLocnStyle.setSelectedIndex(defaultPartnerLocationNameStyle);
		tablePartnerData = pointPersonHandler.getLocationDataTable(defaultLocationNameStyle);
		String[] tablePartnerHeader = pointPersonHandler.getLocationTableHeader();
		tablePartnerLocn.setModel(new DefaultTableModel(tablePartnerData,tablePartnerHeader));

		// Make table single-click editable
		((DefaultCellEditor) tablePartnerLocn.getDefaultEditor(Object.class)).setClickCountToStart(1);
		// and make loss of focus on a cell terminate the edit
		tablePartnerLocn.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);	//$NON-NLS-1$

	    tablePartnerLocn.getColumnModel().getColumn(0).setMinWidth(50);
		tablePartnerLocn.getColumnModel().getColumn(0).setPreferredWidth(120);
		tablePartnerLocn.getColumnModel().getColumn(1).setMinWidth(100);
		tablePartnerLocn.getColumnModel().getColumn(1).setPreferredWidth(250);
		tablePartnerLocn.setAutoCreateColumnsFromModel(false);	// preserve column setup
		JTableHeader partnerHeader = tablePartnerLocn.getTableHeader();
		partnerHeader.setOpaque(false);
		ListSelectionModel partnerSelectionModel = tablePartnerLocn.getSelectionModel();
		partnerSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

	// Setup scrollpane and add to Partner panel
		tablePartnerLocn.setFillsViewportHeight(true);
		JScrollPane partnerScrollPane = new JScrollPane(tablePartnerLocn);
		partnerScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		partnerScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
	// Setup tabbing within table against all rows but only column 1
		JTableCellTabbing.setTabMapping(tablePartnerLocn, 0, tablePartnerLocn.getRowCount(), 1, 1);
	// Add locn scrollpane to Locn sub-panel
		panelPartnerLocn.add(partnerScrollPane, "cell 0 1, aligny top");		//$NON-NLS-1$
	// Add Locn sub-panel to Partner panel
		panelPartner.add(panelPartnerLocn, "cell 1 0 1 2, aligny top");	//$NON-NLS-1$

	// Create Memo label and text area (scrollable) in a Memo sub-panel
	//***************************************************************
		JPanel panelPartnerMemo = new JPanel();
		panelPartnerMemo.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		panelPartnerMemo.setLayout(new MigLayout("insets 5", "[grow]", "[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_MemoP = new JLabel(memoText);
		lbl_MemoP.setFont(lbl_MemoP.getFont().deriveFont(lbl_MemoP.getFont().getStyle() | Font.BOLD));
		panelPartnerMemo.add(lbl_MemoP, "cell 0 0, alignx left");		//$NON-NLS-1$
		memoPartnerText = new JTextArea();
		memoPartnerText.setWrapStyleWord(true);
		memoPartnerText.setLineWrap(true);
		memoPartnerText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		memoPartnerText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)memoPartnerText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	    memoPartnerText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
	    memoPartnerText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
	    memoPartnerText.setBorder(new JTable().getBorder());		// match Table border
	// Setup scrollpane with textarea
		JScrollPane memoPartnerScroll = new JScrollPane(memoPartnerText);
		memoPartnerScroll.setMinimumSize(new Dimension(300, 110)); // set a starter size
		memoPartnerScroll.getViewport().setOpaque(false);
		memoPartnerScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		memoPartnerText.setCaretPosition(0);	// set scrollbar to top
	// Add scrollpane to Memo sub-panel
		panelPartnerMemo.add(memoPartnerScroll, "cell 0 1, aligny top");		//$NON-NLS-1$
	// Add Memo sub-panel to Partner panel
		panelPartner.add(panelPartnerMemo, "cell 0 1");	//$NON-NLS-1$

	// Add a Sentence Edit button
		JButton btn_SentencePartner = new JButton(sentenceEditor);
		panelPartner.add(btn_SentencePartner, "cell 0 2, align center");		//$NON-NLS-1$

	// Add Partner panel to cardPartner
		cardPartner.add(panelPartner, "cell 0 0, aligny top");		//$NON-NLS-1$

//*************************
// Add bottom control panel
//**************************
		JButton btn_Cancel = new JButton(HG0505Msgs.Text_26);		// Cancel
		contents.add(btn_Cancel, "cell 0 2 2, alignx right, gapx 20, tag cancel");		//$NON-NLS-1$

		btn_Save = new JButton(HG0505Msgs.Text_27);		// Save
		btn_Save.setEnabled(false);
		contents.add(btn_Save, "cell 0 2 2, alignx right, gapx 20, tag ok");	//$NON-NLS-1$

//*******************
// Setup Focus Policy			--- still needs work!
//*******************
        Vector<Component> focusOrder = new Vector<>();
        // Name panel
        focusOrder.add(persNameStyle);
        focusOrder.add(tableName);
        focusOrder.add(refText);
        focusOrder.add(tableFlags);
        focusOrder.add(memoNameText);
        // Birth panel
        focusOrder.add(comboBirthType);
        focusOrder.add(birthRoles);
        focusOrder.add(birthDateText);
        focusOrder.add(birthSortDateText);
        focusOrder.add(birthLocnStyle);
        focusOrder.add(tableBirthLocn);
        focusOrder.add(memoBirthText);
        //Bapt/Chr panel
        focusOrder.add(comboBaptType);
        focusOrder.add(baptRoles);
        focusOrder.add(baptDateText);
        focusOrder.add(baptSortDateText);
        focusOrder.add(baptLocnStyle);
        focusOrder.add(tableBaptLocn);
        focusOrder.add(memoBaptText);
        // Death panel
        focusOrder.add(comboDeathType);
        focusOrder.add(deathRoles);
        focusOrder.add(deathRoles);
        focusOrder.add(deathDateText);
        focusOrder.add(deathSortDateText);
        focusOrder.add(deathLocnStyle);
        focusOrder.add(tableDeathLocn);
        focusOrder.add(memoDeathText);
        // Burial panel
        focusOrder.add(comboBurialType);
        focusOrder.add(burialRoles);
        focusOrder.add(burialDateText);
        focusOrder.add(burialSortDateText);
        focusOrder.add(burialLocnStyle);
        focusOrder.add(tableBurialLocn);
        focusOrder.add(memoBurialText);
        // Partner panel
        focusOrder.add(partnerDateText);
        focusOrder.add(partnerSortDateText);
        focusOrder.add(partnerLocnStyle);
        focusOrder.add(tablePartnerLocn);
        focusOrder.add(memoPartnerText);

        contents.setFocusCycleRoot(true);
        contents.setFocusTraversalPolicy(new focusPolicy(focusOrder));
	// Set initial focus of screen
		persNameStyle.requestFocusInWindow();

		pack();

/******************
 * ACTION LISTENERS
 ******************/
		// Listener for clicking 'X' on screen
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e)  {
				btn_Cancel.doClick();
			}
		});

		// Listener for Cancel button
		btn_Cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			// Test for unsaved changes
			if (btn_Save.isEnabled()) {
				if (JOptionPane.showConfirmDialog(btn_Save,
						HG0505Msgs.Text_28			// There are unsaved changes. \n
						+ HG0505Msgs.Text_29,		// Do you still wish to exit this screen?
						addPersonText,
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: cancelling out of HG0505AddPerson"); //$NON-NLS-1$
						// Yes option: clear Reminder and exit without saving
							if (reminderDisplay != null) reminderDisplay.dispose();
							dispose();
						}
				}
			else {
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0505AddPerson"); //$NON-NLS-1$
				if (reminderDisplay != null) reminderDisplay.dispose();
				dispose();
				}
			}
		});

		//Listeners for radio button group items, to show each rightPanel card
		 ActionListener actionRadioName = new ActionListener() {
		      public void actionPerformed(ActionEvent actionEvent) {
		    	  CardLayout cl = (CardLayout)(rightPanel.getLayout());
		    	  cl.show(rightPanel, "NAME");	//$NON-NLS-1$
		      }
		    };

		 ActionListener actionRadioBirth = new ActionListener() {
		      public void actionPerformed(ActionEvent actionEvent) {
		    	  CardLayout cl = (CardLayout)(rightPanel.getLayout());
		    	  cl.show(rightPanel, "BIRTH");	//$NON-NLS-1$
		    	  if (radio_Birth.isSelected()) {
					  createEvent = true;
					  updatedEvent[0] = true;
		    	  }
		      }
		    };

		 ActionListener actionRadioBapt = new ActionListener() {
		      public void actionPerformed(ActionEvent actionEvent) {
		    	  CardLayout cl = (CardLayout)(rightPanel.getLayout());
		    	  cl.show(rightPanel, "BAPTISM");	//$NON-NLS-1$
		    	  if (radio_Bapt.isSelected()) {
		    		  createEvent = true;
		    		  updatedEvent[1] = true;
		    	  }
		      }
		    };

		 ActionListener actionRadioDeath = new ActionListener() {
		      public void actionPerformed(ActionEvent actionEvent) {
		    	  CardLayout cl = (CardLayout)(rightPanel.getLayout());
		    	  cl.show(rightPanel, "DEATH");	//$NON-NLS-1$
		    	  if (radio_Death.isSelected()) {
		    		  createEvent = true;
		    		  updatedEvent[2] = true;
		    	  }
		      }
		    };

		 ActionListener actionRadioBurial = new ActionListener() {
		      public void actionPerformed(ActionEvent actionEvent) {
		    	  CardLayout cl = (CardLayout)(rightPanel.getLayout());
		    	  cl.show(rightPanel, "BURIAL");	//$NON-NLS-1$
		    	  if (radio_Burial.isSelected()) {
		    		  createEvent = true;
		    		  updatedEvent[3] = true;
		    	  }
		      }
		    };

		 ActionListener actionRadioPartner = new ActionListener() {
		      public void actionPerformed(ActionEvent actionEvent) {
		    	  CardLayout cl = (CardLayout)(rightPanel.getLayout());
		    	  cl.show(rightPanel, "PARTNER");	//$NON-NLS-1$
		    	  if (radio_Partner.isSelected()) {
		    		  createEvent = true;
		    		  updatedEvent[4] = true;
		    	  }
		      }
		    };
		// Link the listeners above to the radio buttons
		radio_Name.addActionListener(actionRadioName);
		radio_Birth.addActionListener(actionRadioBirth);
		radio_Bapt.addActionListener(actionRadioBapt);
		radio_Death.addActionListener(actionRadioDeath);
		radio_Burial.addActionListener(actionRadioBurial);
		radio_Partner.addActionListener(actionRadioPartner);

	// On selection of a name style update the Name table
		persNameStyle.addActionListener (new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (!(persNameStyle.getSelectedIndex() == -1)) {
					DefaultTableModel tableModel = (DefaultTableModel) tableName.getModel();
					tableModel.setNumRows(0);	// clear table
					tableNameData = pointPersonHandler.getNameDataTable(persNameStyle.getSelectedIndex());
					tableModel.setDataVector(tableNameData, tableNameHeader);
				// reset table rows
					tableModel.setRowCount(tableNameData.length);
				// reset screen size
					pack();
				}
			}
		});

	// On selection of a birth locn style update the Location table
		birthLocnStyle.addActionListener (new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (!(birthLocnStyle.getSelectedIndex() == -1)) {
					DefaultTableModel tableModel = (DefaultTableModel) tableBirthLocn.getModel();
					tableModel.setNumRows(0);	// clear table
					tableBirthData = pointPersonHandler.getLocationDataTable(birthLocnStyle.getSelectedIndex());
					tableModel.setDataVector(tableBirthData, tableBirthHeader);
				// reset table rows
					tableModel.setRowCount(tableBirthData.length);
				// reset screen size
					pack();
				}
			}
		});

	// On selection of a bapt locn style update the Location table
		baptLocnStyle.addActionListener (new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (!(baptLocnStyle.getSelectedIndex() == -1)) {
					DefaultTableModel tableModel = (DefaultTableModel) tableBaptLocn.getModel();
					tableModel.setNumRows(0);	// clear table
					tableBaptData = pointPersonHandler.getLocationDataTable(baptLocnStyle.getSelectedIndex());
					tableModel.setDataVector(tableBaptData, tableBaptHeader);
				// reset table rows
					tableModel.setRowCount(tableBaptData.length);
				// reset screen size
					pack();
				}
			}
		});

	// On selection of a death locn style update the Location table
		deathLocnStyle.addActionListener (new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (!(deathLocnStyle.getSelectedIndex() == -1)) {
					DefaultTableModel tableModel = (DefaultTableModel) tableDeathLocn.getModel();
					tableModel.setNumRows(0);	// clear table
					tableDeathData = pointPersonHandler.getLocationDataTable(deathLocnStyle.getSelectedIndex());
					tableModel.setDataVector(tableDeathData, tableDeathHeader);
				// reset table rows
					tableModel.setRowCount(tableDeathData.length);
				// reset screen size
					pack();
				}
			}
		});

	// On selection of a burial locn style update the Location table
		burialLocnStyle.addActionListener (new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (!(burialLocnStyle.getSelectedIndex() == -1)) {
					DefaultTableModel tableModel = (DefaultTableModel) tableBurialLocn.getModel();
					tableModel.setNumRows(0);	// clear table
					tableBurialData = pointPersonHandler.getLocationDataTable(burialLocnStyle.getSelectedIndex());
					tableModel.setDataVector(tableBurialData, tableBurialHeader);
				// reset table rows
					tableModel.setRowCount(tableBurialData.length);
				// reset screen size
					pack();
				}
			}
		});

	// On selection of a partner locn style update the Location table
		partnerLocnStyle.addActionListener (new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (!(partnerLocnStyle.getSelectedIndex() == -1)) {
					DefaultTableModel tableModel = (DefaultTableModel) tablePartnerLocn.getModel();
					tableModel.setNumRows(0);	// clear table
					tablePartnerData = pointPersonHandler.getLocationDataTable(partnerLocnStyle.getSelectedIndex());
					tableModel.setDataVector(tablePartnerData, tablePartnerHeader);
				// reset table rows
					tableModel.setRowCount(tablePartnerData.length);
				// reset screen size
					pack();
				}
			}
		});

	// ComboBox listener for event type Birth and setting of role list
		comboBirthType.addActionListener (new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				int selectedBirthIndex = comboBirthType.getSelectedIndex();
				int selectedEventType = birthEventType[selectedBirthIndex];
				if (HGlobal.DEBUG)
					System.out.println("Birth event index/type: " + selectedBirthIndex + "/" + selectedEventType + " - /"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				try {
					birthRoleList = pointPersonHandler.getRolesForEvent(selectedEventType, selectBirthRoles);
					birthRoleType = pointPersonHandler.getEventRoleTypes();
					birthRoles.removeAllItems();
					for (int i = 0; i< birthRoleList.length; i++) {
						birthRoles.addItem(birthRoleList[i]);
					}
					birthRoles.setSelectedIndex(0);
				} catch (HBException hbe) {
					hbe.printStackTrace();
					System.out.println("Add Person role combobox reset error: " + hbe.getMessage());	//$NON-NLS-1$
				}
			}
		});

	// ComboBox listener for event type Bapt and setting of role list
		comboBaptType.addActionListener (new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				int selectedBaptIndex = comboBaptType.getSelectedIndex();
				int selectedEventType = baptEventType[selectedBaptIndex];
				if (HGlobal.DEBUG)
					System.out.println("Bapt event index/type: " + selectedBaptIndex + "/" + selectedEventType + " - /"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				try {
					baptRoleList = pointPersonHandler.getRolesForEvent(selectedEventType, selectBaptRoles);
					baptRoleType = pointPersonHandler.getEventRoleTypes();
					baptRoles.removeAllItems();
					for (int i = 0; i< baptRoleList.length; i++) {
						baptRoles.addItem(baptRoleList[i]);
					}
					baptRoles.setSelectedIndex(0);
				} catch (HBException hbe) {
					hbe.printStackTrace();
					System.out.println("Add Person bapt. role combobox listener error: " + hbe.getMessage());	//$NON-NLS-1$
					pointPersonHandler.errorJOptionMessage(addPersonText, HG0505Msgs.Text_30 	// Add Person bapt. role combobox listener error:
													+ hbe.getMessage());
				}
			}
		});

	// ComboBox listener for event type Death and setting of role list
		comboDeathType.addActionListener (new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				int selectedDeathIndex = comboDeathType.getSelectedIndex();
				int selectedEventType = deathEventType[selectedDeathIndex];
				if (HGlobal.DEBUG)
					System.out.println("Death event index/type: " + selectedDeathIndex + "/" + selectedEventType + " - /"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				try {
					deathRoleList = pointPersonHandler.getRolesForEvent(selectedEventType, selectDeathRoles);
					deathRoleType = pointPersonHandler.getEventRoleTypes();
					deathRoles.removeAllItems();
					for (int i = 0; i< deathRoleList.length; i++) {
						deathRoles.addItem(deathRoleList[i]);
					}
					deathRoles.setSelectedIndex(0);
				} catch (HBException hbe) {
					hbe.printStackTrace();
					System.out.println("Add Person death role combobox listener error: " + hbe.getMessage());	//$NON-NLS-1$
					pointPersonHandler.errorJOptionMessage(addPersonText, HG0505Msgs.Text_31 	// Add Person death role combobox listener error:
													+ hbe.getMessage());
				}
			}
		});

	// ComboBox listener for event type Burial and setting of role list
		comboBurialType.addActionListener (new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				int selectedBurialIndex = comboBurialType.getSelectedIndex();
				int selectedEventType = burialEventType[selectedBurialIndex];
				if (HGlobal.DEBUG)
					System.out.println(" Burial event index/type: " + selectedBurialIndex + "/" + selectedEventType + " - /"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				try {
					burialRoleList = pointPersonHandler.getRolesForEvent(selectedEventType, selectBurialRoles);
					burialRoleType = pointPersonHandler.getEventRoleTypes();
					burialRoles.removeAllItems();
					for (int i = 0; i< burialRoleList.length; i++) {
						burialRoles.addItem(burialRoleList[i]);
					}
					burialRoles.setSelectedIndex(0);
				} catch (HBException hbe) {
					hbe.printStackTrace();
					System.out.println("Add Person burial role combobox listener error: " + hbe.getMessage());	//$NON-NLS-1$
					pointPersonHandler.errorJOptionMessage(addPersonText,
							HG0505Msgs.Text_32 	+ hbe.getMessage());		// Add Person burial role combobox listener error:
				}
			}
		});

		// Listener to birth date to enable save button
		birthDateText.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
            	btn_Save.setEnabled(true);
          // Initiate edit date window and preload
            	if (editBirthDate != null)
            		editBirthDate.convertFromHDate(birthMainYear, birthMainDetails, birthExtraYear, birthExtraDetails);
            	else {
            		editBirthDate = new HG0590EditDate(pointOpenDisplay, false, false, false, true);
            		editBirthDate.dateType = 4;  // set start date
            		if (birthHREDate != null)
            			editBirthDate.convertFromHDate(birthMainYear, birthMainDetails, birthExtraYear, birthExtraDetails);
            	}
           // 	Set position and set visible
                Point xyDate = birthDateText.getLocationOnScreen();
                editBirthDate.setLocation(xyDate.x-50, xyDate.y-50);
                editBirthDate.setModalityType(ModalityType.TOOLKIT_MODAL);
                editBirthDate.setAlwaysOnTop(true);
                editBirthDate.setVisible(true);
            }
        });

		// Listener to birth sort date to enable save button
		birthSortDateText.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
            	btn_Save.setEnabled(true);
                // Initiate edit date window and preload
            	if (editBirthSortDate != null)
            		editBirthSortDate.convertFromHDate(birthSortMainYear, birthSortMainDetails, birthSortExtraYear, birthSortExtraDetails);
            	else {
            		editBirthSortDate = new HG0590EditDate(pointOpenDisplay, false, false, false, true);
            		editBirthSortDate.dateType = 5;  // set end date
            		//if (sortHREDate != null)
            			editBirthSortDate.convertFromHDate(birthSortMainYear, birthSortMainDetails, birthSortExtraYear, birthSortExtraDetails);
            	}
           // 	Set position and set visible
            	Point xyDate = birthSortDateText.getLocationOnScreen();
            	editBirthSortDate.setLocation(xyDate.x-50, xyDate.y-50);
            	editBirthSortDate.setModalityType(ModalityType.TOOLKIT_MODAL);
            	editBirthSortDate.setAlwaysOnTop(true);
            	editBirthSortDate.setVisible(true);
            }
        });

		// Listener to Start date to enable save button
		baptDateText.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
            	btn_Save.setEnabled(true);
          // Initiate edit date window and preload
            	if (editBaptDate != null)
            		editBaptDate.convertFromHDate(baptMainYear, baptMainDetails, baptExtraYear, baptExtraDetails);
            	else {
            		editBaptDate = new HG0590EditDate(pointOpenDisplay, false, false, false, true);
            		editBaptDate.dateType = 6;  // set start date
            		if (baptHREDate != null)
            			editBaptDate.convertFromHDate(baptMainYear, baptMainDetails, baptExtraYear, baptExtraDetails);
            	}
           // 	Set position and set visible
                Point xyDate = baptDateText.getLocationOnScreen();
                editBaptDate.setLocation(xyDate.x-50, xyDate.y-50);
                editBaptDate.setModalityType(ModalityType.TOOLKIT_MODAL);
                editBaptDate.setAlwaysOnTop(true);
                editBaptDate.setVisible(true);
            }
        });

		// Listener to sort date to enable save button
		baptSortDateText.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
            	btn_Save.setEnabled(true);
                // Initiate edit date window and preload
            	if (editBaptSortDate != null)
            		editBaptSortDate.convertFromHDate(baptSortMainYear, baptSortMainDetails, baptSortExtraYear, baptSortExtraDetails);
            	else {
            		editBaptSortDate = new HG0590EditDate(pointOpenDisplay, false, false, false, true);
            		editBaptSortDate.dateType = 7;  // set end date
            		//if (baptSortHREDate != null)
            			editBaptSortDate.convertFromHDate(baptSortMainYear, baptSortMainDetails, baptSortExtraYear, baptSortExtraDetails);
            	}
           // 	Set position and set visible
            	Point xyDate = baptSortDateText.getLocationOnScreen();
            	editBaptSortDate.setLocation(xyDate.x-50, xyDate.y-50);
            	editBaptSortDate.setModalityType(ModalityType.TOOLKIT_MODAL);
            	editBaptSortDate.setAlwaysOnTop(true);
            	editBaptSortDate.setVisible(true);
            }
        });

		// Listener to death date to enable save button
		deathDateText.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
            	btn_Save.setEnabled(true);
          // Initiate edit date window and preload
            	if (editDeathDate != null)
            		editDeathDate.convertFromHDate(deathMainYear, deathMainDetails, deathExtraYear, deathExtraDetails);
            	else {
            		editDeathDate = new HG0590EditDate(pointOpenDisplay, false, false, false, true);
            		editDeathDate.dateType = 8;  // set start date
            		if (deathHREDate != null)
            			editDeathDate.convertFromHDate(deathMainYear, deathMainDetails, deathExtraYear, deathExtraDetails);
            	}
           // 	Set position and set visible
                Point xyDate = deathDateText.getLocationOnScreen();
                editDeathDate.setLocation(xyDate.x-50, xyDate.y-50);
                editDeathDate.setModalityType(ModalityType.TOOLKIT_MODAL);
                editDeathDate.setAlwaysOnTop(true);
                editDeathDate.setVisible(true);
            }
        });

		// Listener to sort date to enable save button
		deathSortDateText.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
            	btn_Save.setEnabled(true);
                // Initiate edit date window and preload
            	if (editDeathSortDate != null)
            		editDeathSortDate.convertFromHDate(deathSortMainYear, deathSortMainDetails, deathSortExtraYear, deathSortExtraDetails);
            	else {
            		editDeathSortDate = new HG0590EditDate(pointOpenDisplay, false, false, false, true);
            		editDeathSortDate.dateType = 9;  // set end date
            		//if (burialSortHREDate != null)
            			editDeathSortDate.convertFromHDate(deathSortMainYear, deathSortMainDetails, deathSortExtraYear, deathSortExtraDetails);
            	}
           // 	Set position and set visible
            	Point xyDate = deathSortDateText.getLocationOnScreen();
            	editDeathSortDate.setLocation(xyDate.x-50, xyDate.y-50);
            	editDeathSortDate.setModalityType(ModalityType.TOOLKIT_MODAL);
            	editDeathSortDate.setAlwaysOnTop(true);
            	editDeathSortDate.setVisible(true);
            }
        });

		// Listener to burial date to enable save button
		burialDateText.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
            	btn_Save.setEnabled(true);
          // Initiate edit date window and preload
            	if (editBurialDate != null)
            		editBurialDate.convertFromHDate(burialMainYear, burialMainDetails, burialExtraYear, burialExtraDetails);
            	else {
            		editBurialDate = new HG0590EditDate(pointOpenDisplay, false, false, false, true);
            		editBurialDate.dateType = 10;  // set start date
            		if (burialHREDate != null)
            			editBurialDate.convertFromHDate(burialMainYear, burialMainDetails, burialExtraYear, burialExtraDetails);
            	}
           // 	Set position and set visible
                Point xyDate = burialDateText.getLocationOnScreen();
                editBurialDate.setLocation(xyDate.x-50, xyDate.y-50);
                editBurialDate.setModalityType(ModalityType.TOOLKIT_MODAL);
                editBurialDate.setAlwaysOnTop(true);
                editBurialDate.setVisible(true);
            }
        });

	// Listener to burial sort date to enable save button
		burialSortDateText.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
            	btn_Save.setEnabled(true);
                // Initiate edit date window and preload
            	if (editBurialSortDate != null)
            		editBurialSortDate.convertFromHDate(burialSortMainYear, burialSortMainDetails, burialSortExtraYear, burialSortExtraDetails);
            	else {
            		editBurialSortDate = new HG0590EditDate(pointOpenDisplay, false, false, false, true);
            		editBurialSortDate.dateType = 11;  // set end date
            		//if (burialHREDate != null)
            			editBurialSortDate.convertFromHDate(burialSortMainYear, burialSortMainDetails, burialSortExtraYear, burialSortExtraDetails);
            	}
           // 	Set position and set visible
            	Point xyDate = burialSortDateText.getLocationOnScreen();
            	editBurialSortDate.setLocation(xyDate.x-50, xyDate.y-50);
            	editBurialSortDate.setModalityType(ModalityType.TOOLKIT_MODAL);
            	editBurialSortDate.setAlwaysOnTop(true);
            	editBurialSortDate.setVisible(true);
            }
        });

		// Listener to partner date to enable save button
		partnerDateText.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
            	btn_Save.setEnabled(true);
          // Initiate edit date window and preload
            	if (editPartnerDate != null)
            		editPartnerDate.convertFromHDate(partnerMainYear, partnerMainDetails, partnerExtraYear, partnerExtraDetails);
            	else {
            		editPartnerDate = new HG0590EditDate(pointOpenDisplay, false, false, false, true);
            		editPartnerDate.dateType = 12;  // set start date
            		if (partnerHREDate != null)
            			editPartnerDate.convertFromHDate(partnerMainYear, partnerMainDetails, partnerExtraYear, partnerExtraDetails);
            	}
           // 	Set position and set visible
                Point xyDate = partnerDateText.getLocationOnScreen();
                editPartnerDate.setLocation(xyDate.x-50, xyDate.y-50);
                editPartnerDate.setModalityType(ModalityType.TOOLKIT_MODAL);
                editPartnerDate.setAlwaysOnTop(true);
                editPartnerDate.setVisible(true);
            }
        });

		// Listener to partner sort date to enable save button
		partnerSortDateText.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
            	btn_Save.setEnabled(true);
                // Initiate edit date window and preload
            	if (editPartnerSortDate != null)
            		editPartnerSortDate.convertFromHDate(partnerSortMainYear, partnerSortMainDetails, partnerSortExtraYear, partnerSortExtraDetails);
            	else {
            		editPartnerSortDate = new HG0590EditDate(pointOpenDisplay, false, false, false, true);
            		editPartnerSortDate.dateType = 13;  // set end date
            		//if (partnerSortHREDate != null)
            			editPartnerSortDate.convertFromHDate(partnerSortMainYear, partnerSortMainDetails, partnerSortExtraYear, partnerSortExtraDetails);
            	}
           // 	Set position and set visible
            	Point xyDate = partnerSortDateText.getLocationOnScreen();
            	editPartnerSortDate.setLocation(xyDate.x-50, xyDate.y-50);
            	editPartnerSortDate.setModalityType(ModalityType.TOOLKIT_MODAL);
            	editPartnerSortDate.setAlwaysOnTop(true);
            	editPartnerSortDate.setVisible(true);
            }
        });


	// Listener for editing birth, bapth, death and burial memo text
		memoBirthText.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
            	birthMemoEdited = true;
            	btn_Save.setEnabled(true);
            }
        });

		memoBaptText.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
            	baptMemoEdited = true;
            	btn_Save.setEnabled(true);
            }
        });

		memoDeathText.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
            	deathMemoEdited = true;
            	btn_Save.setEnabled(true);
            }
        });

		memoBurialText.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
            	burialMemoEdited = true;
            	btn_Save.setEnabled(true);
            }
        });

		memoPartnerText.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
            	partnerMemoEdited = true;
            	btn_Save.setEnabled(true);
            }
        });

	// Listener for changes made in tableName
		TableModelListener persListener = new TableModelListener() {
            public void tableChanged(TableModelEvent tme) {
                if (tme.getType() == TableModelEvent.UPDATE) {
                    int row = tme.getFirstRow();
                    if (row > -1) {
	                    String nameElementData = (String) tableName.getValueAt(row, 1);
						String element = (String) tableName.getValueAt(row, 0);
						if (HGlobal.DEBUG) {
							System.out.println("HG0505AddPerson - name table changed: " + row 	//$NON-NLS-1$
												+ " Element: " + element + " / " + nameElementData);			//$NON-NLS-1$	//$NON-NLS-2$
						}
						if (nameElementData != null) {
							pointPersonHandler.addToPersonNameChangeList(row, nameElementData);
							btn_Save.setEnabled(true);
							nameElementUpdates++;
						}
                    }
				}
			}
		};
		tableName.getModel().addTableModelListener(persListener);

		// Listener for changes made in person Birth location
		TableModelListener birthEventListener = new TableModelListener() {
			public void tableChanged(TableModelEvent tme) {
				if (tme.getType() == TableModelEvent.UPDATE) {
                    int row = tme.getFirstRow();
                    if (row > -1) {
						String nameElementData =  (String) tableBirthLocn.getValueAt(row, 1);
						String element = (String) tableBirthLocn.getValueAt(row, 0);
						if (HGlobal.DEBUG)
							System.out.println("HG0505AddPerson - birth locn table changed: " + row 	//$NON-NLS-1$
											+ " Element: " + element + " / " + nameElementData);			//$NON-NLS-1$
						if (nameElementData != null) {
							pointPersonHandler.addToLocationChangeList( 1, row, nameElementData);
							btn_Save.setEnabled(true);
							eventLocationUpdates[0]++;
						}
                    }
				}
			}
		};
		tableBirthLocn.getModel().addTableModelListener(birthEventListener);

	// Listener for changes made in person Bapt location
		TableModelListener baptEventListener = new TableModelListener() {
			public void tableChanged(TableModelEvent tme) {
				if (tme.getType() == TableModelEvent.UPDATE) {
                    int row = tme.getFirstRow();
                    if (row > -1) {
						String nameElementData =  (String) tableBaptLocn.getValueAt(row, 1);
						String element = (String) tableBaptLocn.getValueAt(row, 0);
						if (HGlobal.DEBUG)
							System.out.println("HG0505AddPerson - bapt locn table changed: " + row 	//$NON-NLS-1$
											+ " Element: " + element + " / " + nameElementData);			//$NON-NLS-1$/$NON-NLS-2$
						if (nameElementData != null) {
							pointPersonHandler.addToLocationChangeList(2, row, nameElementData);
							btn_Save.setEnabled(true);
							eventLocationUpdates[1]++;
						}
                    }
				}
			}
		};
		tableBaptLocn.getModel().addTableModelListener(baptEventListener);

		// Listener for changes made in person Death location
		TableModelListener deathEventListener = new TableModelListener() {
			public void tableChanged(TableModelEvent tme) {
				if (tme.getType() == TableModelEvent.UPDATE) {
                    int row = tme.getFirstRow();
                    if (row > -1) {
						String nameElementData =  (String) tableDeathLocn.getValueAt(row, 1);
						String element = (String) tableDeathLocn.getValueAt(row, 0);
						if (HGlobal.DEBUG)
							System.out.println("HG0505AddPerson - death locn table changed: " + row 	//$NON-NLS-1$
											+ " Element: " + element + " / " + nameElementData);			//$NON-NLS-1$/$NON-NLS-2$
						if (nameElementData != null) {
							pointPersonHandler.addToLocationChangeList(3, row, nameElementData);
							btn_Save.setEnabled(true);
							eventLocationUpdates[2]++;
						}
                    }
				}
			}
		};
		tableDeathLocn.getModel().addTableModelListener(deathEventListener);

		// Listener for changes made in person Burial location
		TableModelListener burialEventListener = new TableModelListener() {
			public void tableChanged(TableModelEvent tme) {
				if (tme.getType() == TableModelEvent.UPDATE) {
                    int row = tme.getFirstRow();
                    if (row > -1) {
						String nameElementData =  (String) tableBurialLocn.getValueAt(row, 1);
						String element = (String) tableBurialLocn.getValueAt(row, 0);
						if (HGlobal.DEBUG)
							System.out.println("HG0505AddPerson - burial locn table changed: " + row
											+ " Element: " + element + " / " + nameElementData);
						if (nameElementData != null) {
							pointPersonHandler.addToLocationChangeList(4, row, nameElementData);
							btn_Save.setEnabled(true);
							eventLocationUpdates[3]++;
						}
                    }
				}
			}
		};
		tableBurialLocn.getModel().addTableModelListener(burialEventListener);

		// Listener for changes made in person Partner location
		TableModelListener partnerEventListener = new TableModelListener() {
			public void tableChanged(TableModelEvent tme) {
				if (tme.getType() == TableModelEvent.UPDATE) {
                    int row = tme.getFirstRow();
                    if (row > -1) {
						String nameElementData =  (String) tablePartnerLocn.getValueAt(row, 1);
						String element = (String) tablePartnerLocn.getValueAt(row, 0);
						if (HGlobal.DEBUG)
							System.out.println("HG0505AddPerson - partner locn table changed: " + row
											+ " Element: " + element + " / " + nameElementData);
						if (nameElementData != null) {
							pointPersonHandler.addToLocationChangeList(5, row, nameElementData);
							btn_Save.setEnabled(true);
							eventLocationUpdates[4]++;
						}
                    }
				}
			}
		};
		tablePartnerLocn.getModel().addTableModelListener(partnerEventListener);

		// General Listener after edit of any text field
		DocumentListener textListen = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {updateFieldState();}
            public void removeUpdate(DocumentEvent e) {updateFieldState();}
            public void changedUpdate(DocumentEvent e) {updateFieldState();}
            protected void updateFieldState() {
            // do not turn on btn_Save here as memo-only updates do not cause event saves
            }
        };
        // Link to the text fields we listen to for changes
        memoNameText.getDocument().addDocumentListener(textListen);
        memoBirthText.getDocument().addDocumentListener(textListen);
        memoBaptText.getDocument().addDocumentListener(textListen);
        memoDeathText.getDocument().addDocumentListener(textListen);
        memoBurialText.getDocument().addDocumentListener(textListen);
        memoPartnerText.getDocument().addDocumentListener(textListen);

		// General Listener for Sentence Editor buttons
		ActionListener sentenceListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// NOTE02 need code here to allow sentence editing
				JOptionPane.showMessageDialog(contents, "This function is not yet implemented"); //$NON-NLS-1$
			}
		};
		btn_SentenceBirth.addActionListener(sentenceListener);
		btn_SentenceBapt.addActionListener(sentenceListener);
		btn_SentenceDeath.addActionListener(sentenceListener);
		btn_SentenceBurial.addActionListener(sentenceListener);
		btn_SentencePartner.addActionListener(sentenceListener);

	// Activate listener for Add New Person SAVE button ONLY
	if (unrelated)
		btn_Save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			// Check person has a name before allowing Save to proceed
				if (nameElementUpdates == 0) {
					addNameErrorMessage();
					return;
				}
			// Add the person to the database
				try {
				// If we are going to add Death or Burial events, set flag Living=N
					if (createEvent) {
						for ( int eventIndex = 0; eventIndex < updatedEvent.length; eventIndex++) {
							if (eventIndex == 2 && updatedEvent[eventIndex]) setLivingToN();	// Death
							if (eventIndex == 3 && updatedEvent[eventIndex]) setLivingToN();	// Burial
						}
					}
					if (HGlobal.DEBUG)
						for (Object[] element : objReqFlagData)
							System.out.println(" Flagid: " + element[3] + " / " + element[2]);	//$NON-NLS-1$ //$NON-NLS-2$
					pointPersonHandler.createAddPersonGUIMemo(memoNameText.getText());
					pointPersonHandler.addNewPerson(refText.getText());
				// Create the edited events
					createAllEvents(pointPersonHandler); // If events created
					if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: saved updates and leaving HG0505AddPerson-New");	//$NON-NLS-1$
				// Reload Result Set for person and names
					pointOpenProject.reloadT401Persons();
					pointOpenProject.reloadT402Names();
					pointOpenProject.getPersonHandler().resetPersonSelect();

				} catch (HBException hbe) {
					System.out.println("HG0505AddPerson - Failed to add person: " + hbe.getMessage());	//$NON-NLS-1$
					JOptionPane.showMessageDialog(btn_Save, HG0505Msgs.Text_34 + hbe.getMessage(),	// ERROR: failed to add person: \n
							addPersonText, JOptionPane.ERROR_MESSAGE);
					if (HGlobal.DEBUG) hbe.printStackTrace();
				}
				dispose();
			}
		});
	}	// End HG0505AddPerson constructor

/**
 * addNameErrorMessage - show prompt for a Name
 */
	public void addNameErrorMessage() {
		JOptionPane.showMessageDialog(btn_Save, HG0505Msgs.Text_35,		// Please add a Name for this Person
				HG0505Msgs.Text_13, JOptionPane.ERROR_MESSAGE);
	}

/**
 * setLivingToN - set flag Living=N if Death/Burial Events saved
 *
 */
	public void setLivingToN() {
  	  for (int i = 0; i < tableFlags.getRowCount(); i++) {
		  // First, find flagIdent = 2 (Living)
		  if ((Integer) objReqFlagData[i][3] == 2) {			// this ID is for Living
			  String allValues = (String) objReqFlagData[i][4];	// get all Living values list
			  String[] values = allValues.split(",");			//$NON-NLS-1$
			  String newValue = "   " + values[1];				// extract the N value	//$NON-NLS-1$
			  flagModel.setValueAt(newValue, i, 1);				// save it back into the flagTable
			  pointPersonHandler.addToPersonFlagChangeList(2, 1); // record flag changes for save
		  }
	  }
	}

/**
 * createAllEvents(HBPersonHandler pointPersonHandler)
 * @param pointPersonHandler
 * @throws HBException
 */
	public long createAllEvents(HBPersonHandler pointPersonHandler) throws HBException {
		/*Create events for birth, bapt, death, burial, partner
		  boolean memoEdited = true; // Signals memo is edited
		  boolean dateOK = true; // Signals date format OK */
		long newEventPID = null_RPID;
		if (createEvent) {
			for ( int eventIndex = 0; eventIndex < updatedEvent.length; eventIndex++) {
				if (eventIndex == 0 && updatedEvent[eventIndex]) {
					if (HGlobal.DEBUG)
						System.out.println(" Create birth event");		//$NON-NLS-1$
					pointPersonHandler.setEventRole(birthRoleType[birthRoles.getSelectedIndex()]);
					int selectedEventIndex = comboBirthType.getSelectedIndex();
					if (birthDateOK || birthSortDateOK ||eventLocationUpdates[eventIndex] > 0 || birthMemoEdited) {
						if (birthMemoEdited) pointPersonHandler.createAddPersonGUIMemo(memoBirthText.getText());
						newEventPID = pointPersonHandler.addPersonEvent(eventIndex, birthEventType[selectedEventIndex]);
						if (birthDateOK) pointPersonHandler.createPersonEventDates(false, newEventPID, "START_HDATE_RPID", birthHREDate); //$NON-NLS-1$
						if (birthSortDateOK) pointPersonHandler.createPersonEventDates(false, newEventPID, "SORT_HDATE_RPID", birthSortHREDate); //$NON-NLS-1$
					}

				} else if (eventIndex == 1 && updatedEvent[eventIndex]) {
					if (HGlobal.DEBUG)
						System.out.println(" Create baptism event");		//$NON-NLS-1$
					pointPersonHandler.setEventRole(baptRoleType[baptRoles.getSelectedIndex()]);
					int selectedEventIndex = comboBaptType.getSelectedIndex();
					if (baptDateOK || baptSortDateOK || eventLocationUpdates[eventIndex] > 0 || baptMemoEdited) {
						if (baptMemoEdited) pointPersonHandler.createAddPersonGUIMemo(memoBaptText.getText());
						newEventPID = pointPersonHandler.addPersonEvent(eventIndex, baptEventType[selectedEventIndex]);
						if (baptDateOK) pointPersonHandler.createPersonEventDates(false, newEventPID, "START_HDATE_RPID", baptHREDate); //$NON-NLS-1$
						if (baptSortDateOK) pointPersonHandler.createPersonEventDates(false, newEventPID, "SORT_HDATE_RPID", baptSortHREDate); //$NON-NLS-1$
					}

				} else if (eventIndex == 2 && updatedEvent[eventIndex]) {
					if (HGlobal.DEBUG)
						System.out.println(" Create death event");		//$NON-NLS-1$
					pointPersonHandler.setEventRole(deathRoleType[deathRoles.getSelectedIndex()]);
					int selectedEventIndex = comboDeathType.getSelectedIndex();
					if (deathDateOK || deathSortDateOK || eventLocationUpdates[eventIndex] > 0 || deathMemoEdited) {
						if (deathMemoEdited) 	pointPersonHandler.createAddPersonGUIMemo(memoDeathText.getText());
						newEventPID = pointPersonHandler.addPersonEvent(eventIndex, deathEventType[selectedEventIndex]);
						if (deathDateOK) pointPersonHandler.createPersonEventDates(false, newEventPID, "START_HDATE_RPID", deathHREDate); //$NON-NLS-1$
						if (deathSortDateOK) pointPersonHandler.createPersonEventDates(false, newEventPID, "SORT_HDATE_RPID", deathSortHREDate); //$NON-NLS-1$
					}

				} else if (eventIndex == 3 && updatedEvent[eventIndex]) {
					if (HGlobal.DEBUG)
						System.out.println(" Create burial event");		//$NON-NLS-1$
					pointPersonHandler.setEventRole(burialRoleType[burialRoles.getSelectedIndex()]);
					int selectedEventIndex = comboBurialType.getSelectedIndex();
					if (burialDateOK || burialSortDateOK || eventLocationUpdates[eventIndex] > 0 || burialMemoEdited) {
						if (burialMemoEdited) pointPersonHandler.createAddPersonGUIMemo(memoBurialText.getText());
						newEventPID = pointPersonHandler.addPersonEvent(eventIndex, burialEventType[selectedEventIndex]);
						if (burialDateOK) pointPersonHandler.createPersonEventDates(false, newEventPID, "START_HDATE_RPID", burialHREDate); //$NON-NLS-1$
						if (burialSortDateOK) pointPersonHandler.createPersonEventDates(false, newEventPID, "SORT_HDATE_RPID", burialSortHREDate); //$NON-NLS-1$
					}

				} else if (eventIndex == 4 && updatedEvent[eventIndex]) {
					if (HGlobal.DEBUG)
						System.out.println(" Create partner event");		//$NON-NLS-1$
					// For partner event role is not applicable since roles are recorded in partner table
					pointPersonHandler.setEventRole(0);
					if (partnerDateOK || partnerSortDateOK || eventLocationUpdates[eventIndex] > 0 || partnerMemoEdited) {
						if (partnerMemoEdited) pointPersonHandler.createAddPersonGUIMemo(memoPartnerText.getText());
						newEventPID = pointPersonHandler.addPersonEvent(eventIndex,
									selectedPartnerType);
						if (HGlobal.DEBUG)
							System.out.println(" Create partner event index/type: " + eventIndex + "/" + selectedPartnerType); //$NON-NLS-1$ //$NON-NLS-2$
						if (partnerDateOK) pointPersonHandler.createPersonEventDates(false, newEventPID, "START_HDATE_RPID", partnerHREDate); //$NON-NLS-1$
						if (partnerSortDateOK) pointPersonHandler.createPersonEventDates(false, newEventPID, "SORT_HDATE_RPID", partnerSortHREDate); //$NON-NLS-1$
						pointPersonHandler.updatePartnerEventLink(newPartnerPID, newEventPID);
					}
				}
			}
			if (HGlobal.DEBUG)
				System.out.println(" HBPersonHandler - createAllEvents - Reset Person Select!");		 //$NON-NLS-1$
		} else if (HGlobal.DEBUG) System.out.println(" No events edited and created.");	//$NON-NLS-1$
		return newEventPID;
	}

	public void saveBirthDate() {
		if (editBirthDate != null) {
			birthMainYear = editBirthDate.returnYearToGUI();
			birthMainDetails = editBirthDate.returnDetailToGUI().trim();
			birthExtraYear = editBirthDate.returnYear2ToGUI();
			birthExtraDetails = editBirthDate.returnDetail2ToGUI().trim();
			birthSortCode = editBirthDate.returnSortString().trim();
			pointPersonHandler.setUpDateTranslation();
			birthDateText.setText(" " + pointPersonHandler.formatDateSelector(birthMainYear, birthMainDetails, 	//$NON-NLS-1$
					birthExtraYear, birthExtraDetails).trim());
		}
		if (birthHREDate == null) birthHREDate = new Object[5];
		birthHREDate[0] = birthMainYear;
		birthHREDate[1] = birthMainDetails;
		birthHREDate[2] = birthExtraYear;
		birthHREDate[3] = birthExtraDetails;
		birthHREDate[4] = birthSortCode;
		birthDateOK = true;

		if (birthSortHREDate == null) {
			birthSortHREDate = new Object[5];
			birthSortHREDate[0] = birthMainYear;
			birthSortHREDate[1] = birthMainDetails;
			birthSortHREDate[2] = birthExtraYear;
			birthSortHREDate[3] = birthExtraDetails;
			birthSortHREDate[4] = birthSortCode;

			birthSortMainYear = birthMainYear;
			birthSortMainDetails = birthMainDetails;
			birthSortExtraYear = birthExtraYear;
			birthSortExtraDetails = birthExtraDetails;
			birthSortSortCode = birthSortCode;

			pointPersonHandler.setUpDateTranslation();
			birthSortDateText.setText(" " + pointPersonHandler.formatDateSelector(birthSortMainYear, birthSortMainDetails, 	//$NON-NLS-1$
					birthSortExtraYear, birthSortExtraDetails).trim());
			birthSortDateOK = true;
		}
	}

	public void saveBirthSortDate() {
		if (editBirthSortDate != null) {
			birthSortMainYear = editBirthSortDate.returnYearToGUI();
			birthSortMainDetails = editBirthSortDate.returnDetailToGUI().trim();
			birthSortExtraYear = editBirthSortDate.returnYear2ToGUI();
			birthSortExtraDetails = editBirthSortDate.returnDetail2ToGUI().trim();
			birthSortSortCode = editBirthSortDate.returnSortString().trim();

			pointPersonHandler.setUpDateTranslation();
			birthSortDateText.setText(" " + pointPersonHandler.formatDateSelector(birthSortMainYear, birthSortMainDetails, 	//$NON-NLS-1$
					birthSortExtraYear, birthSortExtraDetails).trim());
		}
		if (birthSortHREDate == null) birthSortHREDate = new Object[5];
		birthSortHREDate[0] = birthSortMainYear;
		birthSortHREDate[1] = birthSortMainDetails;
		birthSortHREDate[2] = birthSortExtraYear;
		birthSortHREDate[3] = birthSortExtraDetails;
		birthSortHREDate[4] = birthSortSortCode;
		birthSortDateOK = true;
	}

	public void saveBaptDate() {
		if (editBaptDate != null) {
			baptMainYear = editBaptDate.returnYearToGUI();
			baptMainDetails = editBaptDate.returnDetailToGUI().trim();
			baptExtraYear = editBaptDate.returnYear2ToGUI();
			baptExtraDetails = editBaptDate.returnDetail2ToGUI().trim();
			baptSortCode = editBaptDate.returnSortString().trim();
			pointPersonHandler.setUpDateTranslation();
			baptDateText.setText(" " + pointPersonHandler.formatDateSelector(baptMainYear, baptMainDetails, 	//$NON-NLS-1$
					baptExtraYear, baptExtraDetails).trim());
		}
		if (baptHREDate == null) baptHREDate = new Object[5];
		baptHREDate[0] = baptMainYear;
		baptHREDate[1] = baptMainDetails;
		baptHREDate[2] = baptExtraYear;
		baptHREDate[3] = baptExtraDetails;
		baptHREDate[4] = baptSortCode;
		baptDateOK = true;

		if (baptSortHREDate == null) {
			baptSortHREDate = new Object[5];
			baptSortHREDate[0] = baptMainYear;
			baptSortHREDate[1] = baptMainDetails;
			baptSortHREDate[2] = baptExtraYear;
			baptSortHREDate[3] = baptExtraDetails;
			baptSortHREDate[4] = baptSortCode;

			baptSortMainYear = baptMainYear;
			baptSortMainDetails = baptMainDetails;
			baptSortExtraYear = baptExtraYear;
			baptSortExtraDetails = baptExtraDetails;
			baptSortSortCode = baptSortCode;

			pointPersonHandler.setUpDateTranslation();
			baptDateText.setText(" " + pointPersonHandler.formatDateSelector(baptSortMainYear, baptSortMainDetails, 	//$NON-NLS-1$
					baptSortExtraYear, baptSortExtraDetails).trim());
			baptSortDateOK = true;
		}
	}

	public void saveBaptSortDate() {
		if (editBaptSortDate != null) {
			baptSortMainYear = editBaptSortDate.returnYearToGUI();
			baptSortMainDetails = editBaptSortDate.returnDetailToGUI().trim();
			baptSortExtraYear = editBaptSortDate.returnYear2ToGUI();
			baptSortExtraDetails = editBaptSortDate.returnDetail2ToGUI().trim();
			baptSortSortCode = editBaptSortDate.returnSortString().trim();

			pointPersonHandler.setUpDateTranslation();
			baptSortDateText.setText(" " + pointPersonHandler.formatDateSelector(baptSortMainYear, baptSortMainDetails, 	//$NON-NLS-1$
					baptSortExtraYear, baptSortExtraDetails).trim());
		}
		if (baptSortHREDate == null) baptSortHREDate = new Object[5];
		baptSortHREDate[0] = baptSortMainYear;
		baptSortHREDate[1] = baptSortMainDetails;
		baptSortHREDate[2] = baptSortExtraYear;
		baptSortHREDate[3] = baptSortExtraDetails;
		baptSortHREDate[4] = baptSortSortCode;
		baptSortDateOK = true;
	}

	public void saveDeathDate() {
		if (editDeathDate != null) {
			deathMainYear = editDeathDate.returnYearToGUI();
			deathMainDetails = editDeathDate.returnDetailToGUI().trim();
			deathExtraYear = editDeathDate.returnYear2ToGUI();
			deathExtraDetails = editDeathDate.returnDetail2ToGUI().trim();
			deathSortCode = editDeathDate.returnSortString().trim();
			pointPersonHandler.setUpDateTranslation();
			deathDateText.setText(" " + pointPersonHandler.formatDateSelector(deathMainYear, deathMainDetails, 	//$NON-NLS-1$
					deathExtraYear, deathExtraDetails).trim());
		}
		if (deathHREDate == null) deathHREDate = new Object[5];
		deathHREDate[0] = deathMainYear;
		deathHREDate[1] = deathMainDetails;
		deathHREDate[2] = deathExtraYear;
		deathHREDate[3] = deathExtraDetails;
		deathHREDate[4] = deathSortCode;
		deathDateOK = true;

		if (deathSortHREDate == null) {
			deathSortHREDate = new Object[5];
			deathSortHREDate[0] = deathMainYear;
			deathSortHREDate[1] = deathMainDetails;
			deathSortHREDate[2] = deathExtraYear;
			deathSortHREDate[3] = deathExtraDetails;
			deathSortHREDate[4] = deathSortCode;

			deathSortMainYear = deathMainYear;
			deathSortMainDetails = deathMainDetails;
			deathSortExtraYear = deathExtraYear;
			deathSortExtraDetails = deathExtraDetails;
			deathSortSortCode = deathSortCode;

			pointPersonHandler.setUpDateTranslation();
			deathSortDateText.setText(" " + pointPersonHandler.formatDateSelector(deathSortMainYear, deathSortMainDetails, 	//$NON-NLS-1$
					deathSortExtraYear, deathSortExtraDetails).trim());
			deathSortDateOK = true;
		}
	}

	public void saveDeathSortDate() {
		if (editBirthSortDate != null) {
			deathSortMainYear = editBirthSortDate.returnYearToGUI();
			deathSortMainDetails = editBirthSortDate.returnDetailToGUI().trim();
			deathSortExtraYear = editBirthSortDate.returnYear2ToGUI();
			deathSortExtraDetails = editBirthSortDate.returnDetail2ToGUI().trim();
			deathSortSortCode = editBirthSortDate.returnSortString().trim();

			pointPersonHandler.setUpDateTranslation();
			deathSortDateText.setText(" " + pointPersonHandler.formatDateSelector(deathSortMainYear, deathSortMainDetails, 	//$NON-NLS-1$
					deathSortExtraYear, deathSortExtraDetails).trim());
		}
		if (deathSortHREDate == null) deathSortHREDate = new Object[5];
		deathSortHREDate[0] = deathSortMainYear;
		deathSortHREDate[1] = deathSortMainDetails;
		deathSortHREDate[2] = deathSortExtraYear;
		deathSortHREDate[3] = deathSortExtraDetails;
		deathSortHREDate[4] = deathSortSortCode;
		deathSortDateOK = true;
	}

	public void saveBurialDate() {
		if (editBurialDate != null) {
			burialMainYear = editBurialDate.returnYearToGUI();
			burialMainDetails = editBurialDate.returnDetailToGUI().trim();
			burialExtraYear = editBurialDate.returnYear2ToGUI();
			burialExtraDetails = editBurialDate.returnDetail2ToGUI().trim();
			burialSortCode = editBurialDate.returnSortString().trim();
			pointPersonHandler.setUpDateTranslation();
			burialDateText.setText(" " + pointPersonHandler.formatDateSelector(burialMainYear, burialMainDetails, 	//$NON-NLS-1$
					burialExtraYear, burialExtraDetails).trim());
		}
		if (burialHREDate == null) burialHREDate = new Object[5];
		burialHREDate[0] = burialMainYear;
		burialHREDate[1] = burialMainDetails;
		burialHREDate[2] = burialExtraYear;
		burialHREDate[3] = burialExtraDetails;
		burialHREDate[4] = burialSortCode;
		burialDateOK = true;

		if (burialSortHREDate == null) {
			burialSortHREDate = new Object[5];
			burialSortHREDate[0] = burialMainYear;
			burialSortHREDate[1] = burialMainDetails;
			burialSortHREDate[2] = burialExtraYear;
			burialSortHREDate[3] = burialExtraDetails;
			burialSortHREDate[4] = burialSortCode;

			burialSortMainYear = burialMainYear;
			burialSortMainDetails = burialMainDetails;
			burialSortExtraYear = burialExtraYear;
			burialSortExtraDetails = burialExtraDetails;
			burialSortSortCode = burialSortCode;

			pointPersonHandler.setUpDateTranslation();
			burialSortDateText.setText(" " + pointPersonHandler.formatDateSelector(burialSortMainYear, burialSortMainDetails, 	//$NON-NLS-1$
					burialSortExtraYear, burialSortExtraDetails).trim());
			burialSortDateOK = true;
		}
	}

	public void saveBurialSortDate() {
		if (editBurialSortDate != null) {
			burialSortMainYear = editBurialSortDate.returnYearToGUI();
			burialSortMainDetails = editBurialSortDate.returnDetailToGUI().trim();
			burialSortExtraYear = editBurialSortDate.returnYear2ToGUI();
			burialSortExtraDetails = editBurialSortDate.returnDetail2ToGUI().trim();
			burialSortSortCode = editBurialSortDate.returnSortString().trim();

			pointPersonHandler.setUpDateTranslation();
			burialSortDateText.setText(" " + pointPersonHandler.formatDateSelector(burialSortMainYear, burialSortMainDetails, 	//$NON-NLS-1$
					burialSortExtraYear, burialSortExtraDetails).trim());
		}
		if (burialSortHREDate == null) burialSortHREDate = new Object[5];
		burialSortHREDate[0] = burialSortMainYear;
		burialSortHREDate[1] = burialSortMainDetails;
		burialSortHREDate[2] = burialSortExtraYear;
		burialSortHREDate[3] = burialSortExtraDetails;
		burialSortHREDate[4] = burialSortSortCode;
		burialSortDateOK = true;
	}

	public void savePartnerDate() {
		if (editPartnerDate != null) {
			partnerMainYear = editPartnerDate.returnYearToGUI();
			partnerMainDetails = editPartnerDate.returnDetailToGUI().trim();
			partnerExtraYear = editPartnerDate.returnYear2ToGUI();
			partnerExtraDetails = editPartnerDate.returnDetail2ToGUI().trim();
			partnerSortCode = editPartnerDate.returnSortString().trim();
			pointPersonHandler.setUpDateTranslation();
			partnerDateText.setText(" " + pointPersonHandler.formatDateSelector(partnerMainYear, partnerMainDetails, 	//$NON-NLS-1$
					partnerExtraYear, partnerExtraDetails).trim());
		}
		if (partnerHREDate == null) partnerHREDate = new Object[5];
		partnerHREDate[0] = partnerMainYear;
		partnerHREDate[1] = partnerMainDetails;
		partnerHREDate[2] = partnerExtraYear;
		partnerHREDate[3] = partnerExtraDetails;
		partnerHREDate[4] = partnerSortCode;
		partnerDateOK = true;

		if (partnerSortHREDate == null) {
			partnerSortHREDate = new Object[5];
			partnerSortHREDate[0] = partnerMainYear;
			partnerSortHREDate[1] = partnerMainDetails;
			partnerSortHREDate[2] = partnerExtraYear;
			partnerSortHREDate[3] = partnerExtraDetails;
			partnerSortHREDate[4] = partnerSortCode;

			partnerSortMainYear = partnerMainYear;
			partnerSortMainDetails = partnerMainDetails;
			partnerSortExtraYear = partnerExtraYear;
			partnerSortExtraDetails = partnerExtraDetails;
			partnerSortSortCode = partnerSortCode;

			pointPersonHandler.setUpDateTranslation();
			partnerSortDateText.setText(" " + pointPersonHandler.formatDateSelector(partnerSortMainYear, partnerSortMainDetails, 	//$NON-NLS-1$
					partnerSortExtraYear, partnerSortExtraDetails).trim());
			partnerSortDateOK = true;
		}
	}

	public void savePartnerSortDate() {
		if (editPartnerSortDate != null) {
			partnerSortMainYear = editPartnerSortDate.returnYearToGUI();
			partnerSortMainDetails = editPartnerSortDate.returnDetailToGUI().trim();
			partnerSortExtraYear = editPartnerSortDate.returnYear2ToGUI();
			partnerSortExtraDetails = editPartnerSortDate.returnDetail2ToGUI().trim();
			partnerSortSortCode = editPartnerSortDate.returnSortString().trim();

			pointPersonHandler.setUpDateTranslation();
			partnerSortDateText.setText(" " + pointPersonHandler.formatDateSelector(partnerSortMainYear, partnerSortMainDetails, 	//$NON-NLS-1$
					partnerSortExtraYear, partnerSortExtraDetails).trim());
		}
		if (partnerSortHREDate == null) partnerSortHREDate = new Object[5];
		partnerSortHREDate[0] = partnerSortMainYear;
		partnerSortHREDate[1] = partnerSortMainDetails;
		partnerSortHREDate[2] = partnerSortExtraYear;
		partnerSortHREDate[3] = partnerSortExtraDetails;
		partnerSortHREDate[4] = partnerSortSortCode;
		partnerSortDateOK = true;
	}

}  // End of HG0505AddPerson