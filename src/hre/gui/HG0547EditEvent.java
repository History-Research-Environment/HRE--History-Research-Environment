package hre.gui;
/*******************************************************************************
 * Edit Event - Specification 05.47 GUI_Edit Event
 * v0.01.0027 2022-04-07 first draft (D Ferguson)
 * v0.03.0030 2022-09-21 Revised screen format to radio-button sections (D Ferguson)
 * v0.03.0031 2024-01-26 Setup Citation table, Memo, Sentence areas properly (D Ferguson)
 * 			  2024-03-05 Add confirmation prompts to delete actions (D Ferguson)
 * 			  2024-03-08 Fix for enpty location name data - line 167 (N. Tolleshaug)
 * 			  2024-03-16 Add minimal PersonSelector for Add/Change Associates (D Ferguson)
 * 			  2024-03-22 New handling of date, location and memo (N. Tolleshaug)
 * 			  2024-04-04 Get full roleData and show correct role (D Ferguson)
 * 			  2024-04-05 Substituted titleType selector with Java code (N. Tolleshaug)
 * 			  2024-05-26 New handling event edit (N. Tolleshaug)
 * 			  2024-05-30 Fix save/Close buttons; add check for unsaved data (D Ferguson)
 * 			  2024-06-10 Remove Sentence text field; add Sentence button (D Ferguson)
 * 			  2024-06-24 Add Event Type button action code (D Ferguson)
 * 			  2024-07-14 New handling of event Hdates (N. Tolleshaug)
 * 			  2024-07-23 NLS conversion (D Ferguson)
 * 			  2024-07-24 Updated for use of HG0590EditDate (N Tolleshaug)
 * 			  2024-07-28 Updated for use of HG0547TypeEvent (N Tolleshaug)
 * 			  2024-08-29 After Event type change, force Save action (D Ferguson)
 * 			  2024-10-05 Removed console output (N Tolleshaug)
 * 			  2024-10-25 make date fields non-editable by keyboard (D Ferguson)
 * 			  2024-11-03 Removed SwingUtility for table cell focus (D Ferguson)
 * 			  2024-11-19 Updated location style handling (N. Tolleshaug)
 * 			  2024-12-08 Updated location name TAB handling (N. Tolleshaug, D Ferguson)
 ********************************************************************************
 * NOTES for incomplete functionality:
 * NOTE03 need to perform sentence editing
 * NOTE06 need code to handle Citations
 * NOTE07 needs code to handle adding/deleting media items
 * NOTE08 need to check that Min# of Key_Assoc have been selected before saving
 * NOTE09 add Flags support for Events
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EventObject;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.DefaultCaret;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.bila.HBWhereWhenHandler;
import hre.gui.HGlobalCode.JTableCellTabbing;
import hre.gui.HGlobalCode.focusPolicy;
import hre.nls.HG0547Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Edit Events
 * @author D Ferguson
 * @version v0.03.0031
 * @since 2022-04-07
 */

public class HG0547EditEvent extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;
	public HBWhereWhenHandler pointWhereWhenHandler;
	public HBPersonHandler pointPersonHandler;
	HG0547EditEvent pointEditEvent = this;
	long null_RPID  = 1999999999999999L;

	String eventName, roleName = " not set", eventPersonName;

	public String screenID = "54700";	//$NON-NLS-1$
	private String className;
	private JPanel contents;
	private int eventGroup = 0;
	private int eventRoleNum;
	private int eventNum;
	private int dataBaseIndex;

	// Icons for null media
	Icon peopleIcon = new ImageIcon(getClass().getResource("/hre/images/people-48.png")); //$NON-NLS-1$
    Icon bookIcon = new ImageIcon(getClass().getResource("/hre/images/book-48.png")); //$NON-NLS-1$
    Icon audioIcon = new ImageIcon(getClass().getResource("/hre/images/audio-48.png")); //$NON-NLS-1$
    Icon videoIcon = new ImageIcon(getClass().getResource("/hre/images/video-48.png")); //$NON-NLS-1$
    // For Text area font setting
    Font font = UIManager.getFont("TextArea.font");		//$NON-NLS-1$

	DocumentListener dateTextChange;	// accessed by update
	DocumentListener sortTextChange;	// accessed by update

    JButton btn_Save; 					// added for update
    boolean notAddAssoc = true;
    JTextArea memoText; 				// accessed by update
	DocumentListener memoTextChange;	// accessed by update
    boolean memoEdited = false; 		// Signals memo is edited
    boolean changedLocationNameStyle = false;   // Signals nane style changed
    int selectedStyleIndex;

    JLabel actualEvent;
    JTable tableLocation;
    String[] tableBirthHeader;
    JComboBox<String> locationNameStyles;
    TableModelListener eventLocationListener;
    JTextField dateText;
    JTextField sortDateText;
    JButton btn_EventType;
    boolean changeEventType = false;

    Object[][] tableLocationData;
    String [] nameData;
    Object[][] tableEventCiteData;
    public Object[][] tableAssocsData;
    String[] tableAssocsHeader;
    String[] tableCiteHeader;
   	Object[][] roleData;	// Contains Role Name, Role Number, Role Seq.
    boolean locationElementUpdate = false;
    public long locationNamePID = null_RPID;

    public int selectedEventNum;
    public int selectedRoleNum;
	private int rowClicked;

	HG0590EditDate editStartDate;
	long startMainYear = 0L;
	String startMainDetails = "";	//$NON-NLS-1$
	long startExtraYear = 0L;
	String startExtraDetails = "";	//$NON-NLS-1$
	String startSortCode = "";		//$NON-NLS-1$
	Object[] startHREDate;
	boolean startDateOK = false;
	long startHDatePID;
	long newEventPID;

	HG0590EditDate sortEndDate;
	long sortMainYear = 0L;
	String sortMainDetails = "";	//$NON-NLS-1$
	long sortExtraYear = 0L;
	String sortExtraDetails = "";	//$NON-NLS-1$
	String sortSortCode = "";		//$NON-NLS-1$
	Object[] sortHREDate;
	boolean sortDateOK = false;
	long sortHDatePID;
	String partnerNames = " - ";

/**
 * String getClassName()
 * @return className
 */
    public String getClassName() {
    	return className;
    }

/**
 * Create the dialog
 * @throws HBException
 */
	public HG0547EditEvent(HBProjectOpenData pointOpenProject, int eventNumber, int roleNumber, long eventPID) throws HBException {
		if (HGlobal.DEBUG)
			System.out.println(" EditEvent: " + eventNumber + "/" + roleNumber);	//$NON-NLS-1$ //$NON-NLS-2$

	// Set pointOpenproject in super - HG0450SuperDialog
		this.pointOpenProject = pointOpenProject;
		this.eventRoleNum = roleNumber;
		this.eventNum = eventNumber;
		// Setup references
		windowID = screenID;
		helpName = "editevent";	//$NON-NLS-1$
    	className = getClass().getSimpleName();
    	selectedEventNum = eventNumber;
    	selectedRoleNum = roleNumber;
    	this.setResizable(true);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs)
			HB0711Logging.logWrite("Action: entering HG0547EditEvent");	//$NON-NLS-1$

	    pointWhereWhenHandler = pointOpenProject.getWhereWhenHandler();
	    pointPersonHandler = pointOpenProject.getPersonHandler();
	    dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
	    tableAssocsHeader = pointPersonHandler.setTranslatedData("54700", "1", false); // Person, Role //$NON-NLS-1$ //$NON-NLS-2$
	    tableCiteHeader = pointPersonHandler.setTranslatedData("50500", "1", false); // Source#, Source, 1 2 D P M  //$NON-NLS-1$ //$NON-NLS-2$
	    eventGroup = pointPersonHandler.pointLibraryResultSet.getEventGroup(eventNum, dataBaseIndex);

/***********************************
 * Initiate load of Name/Style data
 ***********************************/
	 // Get Name style and start/end dates i exist for update
	    if (this instanceof HG0547UpdateEvent) {
	    	if (eventGroup == pointPersonHandler.marrGroup || eventGroup == pointPersonHandler.divorceGroup)
				roleName = pointWhereWhenHandler.getPartnerNames();
			else
				roleName = pointWhereWhenHandler.getEventRoleName(eventNumber, roleNumber);

			String selectString = pointPersonHandler.setSelectSQL("*", pointPersonHandler.eventTable,	//$NON-NLS-1$
					"PID = " + eventPID);																//$NON-NLS-1$
			ResultSet eventTable = pointPersonHandler.requestTableData(selectString, dataBaseIndex);
			try {
				eventTable.first();
				startHDatePID = eventTable.getLong("START_HDATE_RPID");		//$NON-NLS-1$
				sortHDatePID = eventTable.getLong("SORT_HDATE_RPID");		//$NON-NLS-1$
			} catch (SQLException sqle) {
				System.out.println(" SQL error personNameTable: " + sqle.getMessage());		//$NON-NLS-1$
				sqle.printStackTrace();
				throw new HBException(" SQL error personNameTable: " + sqle.getMessage());	//$NON-NLS-1$
			}
		// Collect start date Hdate
	    	startHREDate = pointPersonHandler.pointLibraryResultSet.
	    			dateIputHdate(startHDatePID, dataBaseIndex);
	    	if (startHREDate != null) {
				startMainYear = (long) startHREDate[0];
				startMainDetails = (String) startHREDate[1];
				startExtraYear = (long) startHREDate[2];
				startExtraDetails = (String) startHREDate[3];
				startSortCode = (String) startHREDate[4];
	    	}
	  // Collect end date Hdate
	    	sortHREDate = pointPersonHandler.pointLibraryResultSet.dateIputHdate(sortHDatePID, dataBaseIndex);
	    	if (sortHREDate != null) {
				sortMainYear = (long) sortHREDate[0];
				sortMainDetails = (String) sortHREDate[1];
				sortExtraYear = (long) sortHREDate[2];
				sortExtraDetails = (String) sortHREDate[3];
				sortSortCode = (String) sortHREDate[4];
	    	} else if (startHREDate != null){
				sortMainYear = (long) startHREDate[0];
				sortMainDetails = (String) startHREDate[1];
				sortExtraYear = (long) startHREDate[2];
				sortExtraDetails = (String) startHREDate[3];
				sortSortCode = (String) startHREDate[4];
	    	}
	     }
	    eventName = pointWhereWhenHandler.getEventName(eventNumber);

	    if (this instanceof HG0547PartnerEvent) {
	    	if (eventGroup == pointPersonHandler.marrGroup || eventGroup == pointPersonHandler.divorceGroup)
				roleName = pointWhereWhenHandler.getPartnerNames();
			else
				roleName = pointWhereWhenHandler.getEventRoleName(eventNumber, roleNumber);
	    	if (HGlobal.DEBUG)
	    		System.out.println(" Event " + eventName + " partners " + roleName);
	    }

	    try {
			eventPersonName = pointPersonHandler.getManagedPersonName();
			roleData = pointWhereWhenHandler.getEventRoleData(eventNumber, "");		//$NON-NLS-1$
		} catch (HBException hbe) {
			System.out.println(" HG0547EditEvent find eventname/rolename error"); 	//$NON-NLS-1$
			hbe.printStackTrace();
		}
	    setTitle(HG0547Msgs.Text_0 + eventName + HG0547Msgs.Text_1 + eventPersonName);	// Event for

	    // load ALL current Assocs and their current roles
		tableAssocsData = pointWhereWhenHandler.getAssociateTabl();

		// NOTE06 Following is dummy data for example purposes only - to be removed
	    Object[][] tableCiteData = {{"1", "Source A ", "3 ,3 ,2 ,1 ,1 "},	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    							{"17", "Source B", "3 ,3 ,0 ,1 ,2 "}};	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

/************************************
 * Setup main panel and its contents
 ***********************************/
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]10[]", "[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    	JToolBar toolBar = new JToolBar();
    	toolBar.setFloatable(false);
    	toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
    	toolBar.add(Box.createHorizontalGlue());
    	// Add HG0450 icons
		toolBar.add(btn_Remindericon);
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north");	//$NON-NLS-1$

/**************************************
 * Setup leftPanel and its contents
 **************************************/
		JPanel leftPanel = new JPanel();
		leftPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		contents.add(leftPanel, "cell 0 0, growx, aligny top");	//$NON-NLS-1$
		leftPanel.setLayout(new MigLayout("insets 5", "[]", "[]10[]10[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_EditType = new JLabel(HG0547Msgs.Text_2);		// Edit Item
		leftPanel.add(lbl_EditType, "cell 0 0, alignx center");	//$NON-NLS-1$

		JRadioButton radio_Event = new JRadioButton(HG0547Msgs.Text_3);	// Event
		radio_Event.setSelected(true);
		leftPanel.add(radio_Event, "cell 0 1, alignx left");	//$NON-NLS-1$

		JRadioButton radio_Flag = new JRadioButton(HG0547Msgs.Text_4);	// Flags
		leftPanel.add(radio_Flag, "cell 0 2, alignx left");		//$NON-NLS-1$

		JRadioButton radio_Media = new JRadioButton(HG0547Msgs.Text_5);	// Media
		leftPanel.add(radio_Media, "cell 0 3, alignx left");	//$NON-NLS-1$

		ButtonGroup radioGroup = new ButtonGroup();
		radioGroup.add(radio_Event);
		radioGroup.add(radio_Flag);
		radioGroup.add(radio_Media);

/***************************************************
 * Setup rightPanel and a CardLayout of Panels in it
 **************************************************/
		JPanel rightPanel = new JPanel(new CardLayout());
		rightPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		contents.add(rightPanel, "cell 1 0, grow");	//$NON-NLS-1$
		// Define cards of the CardLayout, each card-Panel with its own layout manager
		JPanel cardEvent = new JPanel();
		cardEvent.setLayout(new MigLayout("", "[grow]10[grow]", "[]10[grow]10[grow]5[grow]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		rightPanel.add(cardEvent, "EVENT");	//$NON-NLS-1$
		JPanel cardFlags = new JPanel();
		cardFlags.setLayout(new MigLayout("", "[]10[]", "[grow]"));		//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		rightPanel.add(cardFlags, "FLAGS");	//$NON-NLS-1$
		JPanel cardMedia = new JPanel();
		cardMedia.setLayout(new MigLayout("", "[]", "[]20[]"));			//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		rightPanel.add(cardMedia, "MEDIA");	//$NON-NLS-1$

/***************************************************
 * Setup cardPanel for Event
 **************************************************/
	// Event Name/date sub-panel
		JPanel topEvntPanel = new JPanel();
		topEvntPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		topEvntPanel.setLayout(new MigLayout("insets 5", "[]50[]10[]", "[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		actualEvent = new JLabel(eventName + " / " + roleName);	//$NON-NLS-1$
		actualEvent.setFont(actualEvent.getFont().deriveFont(actualEvent.getFont().getStyle() | Font.BOLD));
		topEvntPanel.add(actualEvent, "cell 0 0");	//$NON-NLS-1$

		btn_EventType = new JButton(HG0547Msgs.Text_6);	// Change Event Type
		btn_EventType.setFont(btn_EventType.getFont().deriveFont(btn_EventType.getFont().getStyle() | Font.BOLD));
		topEvntPanel.add(btn_EventType, "cell 0 1");	//$NON-NLS-1$

		JLabel lbl_Date = new JLabel(HG0547Msgs.Text_7);	// Date:
		topEvntPanel.add(lbl_Date, "cell 1 0, alignx right");	//$NON-NLS-1$

		dateText = new JTextField(" ");	//$NON-NLS-1$
		dateText.setColumns(18);
		dateText.setEditable(false);		// ensure field cannot be edited from keyboard
		dateText.setBackground(UIManager.getColor("TextField.background"));  //$NON-NLS-1$
		topEvntPanel.add(dateText, "cell 2 0");	//$NON-NLS-1$

		JLabel lbl_sortDate = new JLabel(HG0547Msgs.Text_8);	// Sort Date:
		topEvntPanel.add(lbl_sortDate, "cell 1 1");	//$NON-NLS-1$

		sortDateText = new JTextField(" ");	//$NON-NLS-1$
		sortDateText.setColumns(18);
		sortDateText.setEditable(false);		// ensure field cannot be edited from keyboard
		sortDateText.setBackground(UIManager.getColor("TextField.background"));  //$NON-NLS-1$
		topEvntPanel.add(sortDateText, "cell 2 1");	//$NON-NLS-1$

		cardEvent.add(topEvntPanel, "cell 0 0 2, aligny top");	//$NON-NLS-1$

	// Setup People/Roles sub-panel
		JPanel midLeftEvntPanel = new JPanel();
		midLeftEvntPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		midLeftEvntPanel.setLayout(new MigLayout("insets 5", "[][]", "[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_Assocs = new JLabel(HG0547Msgs.Text_9);	// Associates of this Event:
		lbl_Assocs.setFont(lbl_Assocs.getFont().deriveFont(lbl_Assocs.getFont().getStyle() | Font.BOLD));
		midLeftEvntPanel.add(lbl_Assocs, "cell 0 0");	//$NON-NLS-1$

		// Create scrollpane and table for the Person/Role data
		JTable tableAssocs = new JTable() {
			private static final long serialVersionUID = 1L;
				@Override
				public Dimension getPreferredScrollableViewportSize() {
				    return new Dimension(super.getPreferredSize().width,
					    		10 * super.getRowHeight());
				}
				@Override
				public boolean isCellEditable(int row, int col) {
					if (col == 1) return true;
					return false;
				}
		};
		tableAssocs.setModel(new DefaultTableModel (tableAssocsData, tableAssocsHeader));

		tableAssocs.getColumnModel().getColumn(0).setMinWidth(80);
		tableAssocs.getColumnModel().getColumn(0).setPreferredWidth(250);
		tableAssocs.getColumnModel().getColumn(1).setMinWidth(50);
		tableAssocs.getColumnModel().getColumn(1).setPreferredWidth(120);
		tableAssocs.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JTableHeader persHeader = tableAssocs.getTableHeader();
		persHeader.setOpaque(false);
		ListSelectionModel persSelectionModel = tableAssocs.getSelectionModel();
		persSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	// Setup scrollpane and add to Name panel
		tableAssocs.setFillsViewportHeight(true);
		JScrollPane persScrollPane = new JScrollPane(tableAssocs);
		persScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		persScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		// Setup tabbing within table against all rows, cols
		if (tableAssocs.getRowCount() > 0)
			JTableCellTabbing.setTabMapping(tableAssocs, 0, tableAssocs.getRowCount(), 0, 2);
		midLeftEvntPanel.add(persScrollPane, "cell 0 1 2");	//$NON-NLS-1$

		cardEvent.add(midLeftEvntPanel, "cell 0 1, aligny top");	//$NON-NLS-1$

	// Setup Location sub-panel
		JPanel midRightEvntPanel = new JPanel();
		midRightEvntPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		midRightEvntPanel.setLayout(new MigLayout("insets 5", "[]", "[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_Styles = new JLabel(HG0547Msgs.Text_10);	// Location Style:
		lbl_Styles.setFont(lbl_Styles.getFont().deriveFont(lbl_Styles.getFont().getStyle() | Font.BOLD));
		midRightEvntPanel.add(lbl_Styles, "cell 0 0, alignx left");	//$NON-NLS-1$
		DefaultComboBoxModel<String> comboLocnModel
									= new DefaultComboBoxModel<>(pointWhereWhenHandler.getLocationStyles());
		locationNameStyles = new JComboBox<>(comboLocnModel);
		locationNameStyles.setSelectedIndex(pointWhereWhenHandler.getDefaultLocationStyleIndex());
		midRightEvntPanel.add(locationNameStyles, "cell 0 0, gapx 10");	//$NON-NLS-1$

	// Create scrollpane and table for the Location data entry
		tableLocation = new JTable() {
			private static final long serialVersionUID = 1L;
				@Override
				public Dimension getPreferredScrollableViewportSize() {
					  return new Dimension(super.getPreferredSize().width,
					    		super.getRowCount() * super.getRowHeight());
					 }
				@Override
				public boolean isCellEditable(int row, int col) {
					if (col == 1) return true;
					return false;
				}
				@Override
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
				@Override
				public Component prepareRenderer(TableCellRenderer renderer, int row, int col)  {
					Component cell = super.prepareRenderer(renderer, row, col);
					// For the Selected cell we let the editor take over with no highlights
					if (col == 1) cell.setCursor(Cursor.getDefaultCursor());
					cell.setBackground((Color) UIManager.get("Table.background"));		//$NON-NLS-1$
					cell.setForeground((Color) UIManager.get("Table.foreground"));		//$NON-NLS-1$
					return cell;
				}
			};

		int defaultLocationNameStyle = pointWhereWhenHandler.getDefaultLocationStyleIndex();
		locationNameStyles.setSelectedIndex(defaultLocationNameStyle);
		tableLocationData = pointWhereWhenHandler.getLocationDataTable(defaultLocationNameStyle);
		tableBirthHeader = pointWhereWhenHandler.getEventLocationTableHeader();
		tableLocation.setModel(new DefaultTableModel(tableLocationData, tableBirthHeader));

	// Make table single-click editable
		((DefaultCellEditor) tableLocation.getDefaultEditor(Object.class)).setClickCountToStart(1);
		// and make loss of focus on a cell terminate the edit
		tableLocation.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);	//$NON-NLS-1$

		tableLocation.getColumnModel().getColumn(0).setMinWidth(80);
		tableLocation.getColumnModel().getColumn(0).setPreferredWidth(120);
		tableLocation.getColumnModel().getColumn(1).setMinWidth(100);
		tableLocation.getColumnModel().getColumn(1).setPreferredWidth(300);
		tableLocation.setAutoCreateColumnsFromModel(false);	// preserve column setup
		tableLocation.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JTableHeader nameHeader = tableLocation.getTableHeader();
		nameHeader.setOpaque(false);
		ListSelectionModel nameSelectionModel = tableLocation.getSelectionModel();
		nameSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	// Setup scrollpane and add to Name panel
		tableLocation.setFillsViewportHeight(true);
		JScrollPane nameScrollPane = new JScrollPane(tableLocation);
		nameScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		nameScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
	// Setup tabbing within table against all rows but only column 1
		if (tableLocation.getRowCount() > 0) {
			JTableCellTabbing.setTabMapping(tableLocation, 0, tableLocation.getRowCount(), 1, 1);
		}
		midRightEvntPanel.add(nameScrollPane, "cell 0 1");	//$NON-NLS-1$

		cardEvent.add(midRightEvntPanel, "cell 1 1, aligny top");	//$NON-NLS-1$

	// Setup Memo sub-panel
		JPanel botmLeftEvntPanel = new JPanel();
		botmLeftEvntPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		botmLeftEvntPanel.setLayout(new MigLayout("insets 5", "[grow]", "[]5[grow]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_Memo = new JLabel(HG0547Msgs.Text_11);	// Memo:
		lbl_Memo.setFont(lbl_Memo.getFont().deriveFont(lbl_Memo.getFont().getStyle() | Font.BOLD));
		botmLeftEvntPanel.add(lbl_Memo, "cell 0 0, align left");	//$NON-NLS-1$

		memoText = new JTextArea();
		memoText.setWrapStyleWord(true);
		memoText.setLineWrap(true);
		memoText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		memoText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)memoText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	    memoText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
	    memoText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
	    memoText.setBorder(new JTable().getBorder());		// match Table border
	// Setup scrollpane with textarea
		JScrollPane memoTextScroll = new JScrollPane(memoText);
		memoTextScroll.setMinimumSize(new Dimension(370, 60));
		memoTextScroll.getViewport().setOpaque(false);
		memoTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		memoText.setCaretPosition(0);	// set scrollbar to top
		botmLeftEvntPanel.add(memoTextScroll, "cell 0 1, alignx left, aligny top");	//$NON-NLS-1$

		cardEvent.add(botmLeftEvntPanel, "cell 0 2, aligny top");	//$NON-NLS-1$

	// Setup Citation sub-panel
		JPanel botmRightEvntPanel = new JPanel();
		botmRightEvntPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		botmRightEvntPanel.setLayout(new MigLayout("insets 5", "[][grow][80!]", "[]5[grow]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_Citation = new JLabel(HG0547Msgs.Text_12);	// Citation:
		lbl_Citation.setFont(lbl_Citation.getFont().deriveFont(lbl_Citation.getFont().getStyle() | Font.BOLD));
		botmRightEvntPanel.add(lbl_Citation, "cell 0 0, align left");	//$NON-NLS-1$

		JButton btn_AddL = new JButton("+"); //$NON-NLS-1$
		btn_AddL.setFont(new Font("Arial", Font.BOLD, 12)); //$NON-NLS-1$
		btn_AddL.setMaximumSize(new Dimension(24, 24));
		btn_AddL.setEnabled(true);
		botmRightEvntPanel.add(btn_AddL, "cell 1 0, alignx center, aligny top"); //$NON-NLS-1$

		JButton btn_DelL = new JButton("-"); //$NON-NLS-1$
		btn_DelL.setFont(new Font("Arial", Font.BOLD, 12));	//$NON-NLS-1$
		btn_DelL.setMaximumSize(new Dimension(24, 24));
		btn_DelL.setEnabled(false);
		botmRightEvntPanel.add(btn_DelL, "cell 1 0, aligny top"); //$NON-NLS-1$

		ImageIcon upArrow = new ImageIcon(getClass().getResource("/hre/images/arrow_up16.png")); //$NON-NLS-1$
		JButton btn_UpL = new JButton(upArrow);
		btn_UpL.setVerticalAlignment(SwingConstants.TOP);
		btn_UpL.setToolTipText(HG0547Msgs.Text_13);	// Moves Citation up the list
		btn_UpL.setMaximumSize(new Dimension(20, 20));
		btn_UpL.setEnabled(false);
		botmRightEvntPanel.add(btn_UpL, "cell 1 0, aligny top, gapx 10"); //$NON-NLS-1$

		ImageIcon downArrow = new ImageIcon(getClass().getResource("/hre/images/arrow_down16.png")); //$NON-NLS-1$
		JButton btn_DownL = new JButton(downArrow);
		btn_DownL.setVerticalAlignment(SwingConstants.TOP);
		btn_DownL.setToolTipText(HG0547Msgs.Text_14);	// Moves Citation down the list
		btn_DownL.setMaximumSize(new Dimension(20, 20));
		btn_DownL.setEnabled(false);
		botmRightEvntPanel.add(btn_DownL, "cell 1 0, aligny top"); //$NON-NLS-1$

		JLabel lbl_Surety = new JLabel(HG0547Msgs.Text_15);	// Surety
		botmRightEvntPanel.add(lbl_Surety, "cell 2 0");	//$NON-NLS-1$

		// Create scrollpane and table for the Citations
		JTable tableCite = new JTable() {
			private static final long serialVersionUID = 1L;
				@Override
				public Dimension getPreferredScrollableViewportSize() {
					// Force a 4-row table, even if empty
					int r = super.getRowCount();
					if (r < 4)	r = 4;
					return new Dimension(super.getPreferredSize().width,
					    		r * super.getRowHeight());
					 }
				@Override
				public boolean isCellEditable(int row, int col) {
						return false;
				}
			};
//		tableCiteData = pointxxxxxxxxxxx							// NOTE06 get citation data
		tableCite.setModel(new DefaultTableModel (tableCiteData, tableCiteHeader));
		tableCite.getColumnModel().getColumn(0).setMinWidth(30);
		tableCite.getColumnModel().getColumn(0).setPreferredWidth(50);
		tableCite.getColumnModel().getColumn(1).setMinWidth(100);
		tableCite.getColumnModel().getColumn(1).setPreferredWidth(270);
		tableCite.getColumnModel().getColumn(2).setMinWidth(80);
		tableCite.getColumnModel().getColumn(2).setPreferredWidth(100);
		tableCite.setAutoCreateColumnsFromModel(false);	// preserve column setup
		JTableHeader citeHeader = tableCite.getTableHeader();
		citeHeader.setOpaque(false);
		ListSelectionModel citeSelectionModel = tableCite.getSelectionModel();
		citeSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	// Setup scrollpane and add to Name panel
		tableCite.setFillsViewportHeight(true);
		JScrollPane citeScrollPane = new JScrollPane(tableCite);
		citeScrollPane.setFocusTraversalKeysEnabled(false);
		citeScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		citeScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	// Set Source#, Surety to be center-aligned
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		tableCite.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
		tableCite.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
	// Setup tabbing within table against all rows but only column 1, 2
		if (tableCite.getRowCount() > 0)
			JTableCellTabbing.setTabMapping(tableCite, 0, tableCite.getRowCount(), 1, 2);
	// Add to Event panel
		botmRightEvntPanel.add(citeScrollPane, "cell 0 1 3 1, alignx left, aligny top");	//$NON-NLS-1$

		cardEvent.add(botmRightEvntPanel, "cell 1 2 1 2, aligny top");	//$NON-NLS-1$

	// Add Sentence Editor button below Memo panel
		JButton btn_Sentence = new JButton(HG0547Msgs.Text_16);	// Sentence Editor
		btn_Sentence.setToolTipText(HG0547Msgs.Text_17);
		cardEvent.add(btn_Sentence, "cell 0 3, alignx center"); //$NON-NLS-1$

/***************************************
 * Setup cardFlags for Flags
 **************************************/
		JLabel lbl_Flag = new JLabel("Flags not yet implemented for Events");	//NOTE09	//$NON-NLS-1$
		cardFlags.add(lbl_Flag, "cell 1 0");				//$NON-NLS-1$

/***************************************
 * Setup cardMedia for Media
 **************************************/
		JButton btn_Text = new JButton(HG0547Msgs.Text_19);		// Add Image	//NOTE07
		cardMedia.add(btn_Text, "cell 0 0");				//$NON-NLS-1$

		JButton btn_Image = new JButton(HG0547Msgs.Text_20);		// Add Text	//NOTE07
		cardMedia.add(btn_Image, "cell 0 0, gapx 20");		//$NON-NLS-1$

		JButton btn_Audio = new JButton(HG0547Msgs.Text_21);		// Add Audio	//NOTE07
		cardMedia.add(btn_Audio, "cell 0 0, gapx 20");		//$NON-NLS-1$

		JButton btn_Video = new JButton(HG0547Msgs.Text_22);		// Add Video	//NOTE07
		cardMedia.add(btn_Video, "cell 0 0, gapx 20"); 		//$NON-NLS-1$

	 // Add dummy entries for Image/text/Audio/video until MediaHandler can deliver them (NOTE07)
    	JPanel imagePanel = new JPanel();
		imagePanel.setLayout(new MigLayout("insets 5", "[]", "[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		imagePanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        JLabel image = new JLabel(peopleIcon);
		imagePanel.add(image, "cell 0 0, alignx center");	//$NON-NLS-1$
		JTextArea txt_Image = new JTextArea(HG0547Msgs.Text_23);		//   No Image files present
		txt_Image.setLineWrap(true);
		txt_Image.setWrapStyleWord(true);
		txt_Image.setEditable(false);
		txt_Image.setSize(150,22);
   		imagePanel.add(txt_Image, "cell 0 1");		//$NON-NLS-1$
		cardMedia.add(imagePanel, "cell 0 1");		//$NON-NLS-1$

		// Add text icon with 'not present' msg
   		JPanel textPanel = new JPanel();
		textPanel.setLayout(new MigLayout("insets 5", "[]", "[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		textPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		JLabel lbl_Text = new JLabel(bookIcon);
		textPanel.add(lbl_Text, "cell 0 0, alignx center");	//$NON-NLS-1$
		JTextArea txt_Text = new JTextArea(HG0547Msgs.Text_24);		//   No Text files present
		txt_Text.setLineWrap(true);
		txt_Text.setWrapStyleWord(true);
		txt_Text.setEditable(false);
		txt_Text.setSize(150,22);
   		textPanel.add(txt_Text, "cell 0 1");		//$NON-NLS-1$
		cardMedia.add(textPanel, "cell 0 1");		//$NON-NLS-1$

   		JPanel audioPanel = new JPanel();
		audioPanel.setLayout(new MigLayout("insets 5", "[]", "[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		audioPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		JLabel lbl_Audio = new JLabel(audioIcon);
		audioPanel.add(lbl_Audio, "cell 0 0, alignx center");		//$NON-NLS-1$
		JTextArea txt_Audio = new JTextArea(HG0547Msgs.Text_25);		//   No Audio files present
		txt_Audio.setLineWrap(true);
		txt_Audio.setWrapStyleWord(true);
		txt_Audio.setEditable(false);
		txt_Audio.setSize(150,22);
		audioPanel.add(txt_Audio, "cell 0 1");		//$NON-NLS-1$
		cardMedia.add(audioPanel, "cell 0 1");		//$NON-NLS-1$

   		JPanel videoPanel = new JPanel();
		videoPanel.setLayout(new MigLayout("insets 5", "[]", "[]5[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		videoPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		JLabel lbl_Video = new JLabel(videoIcon);
		videoPanel.add(lbl_Video, "cell 0 0, alignx center");	//$NON-NLS-1$
		JTextArea txt_Video = new JTextArea(HG0547Msgs.Text_26);		//    No Video files present
		txt_Video.setLineWrap(true);
		txt_Video.setWrapStyleWord(true);
		txt_Video.setEditable(false);
		txt_Video.setSize(150,22);
		videoPanel.add(txt_Video, "cell 0 1");		//$NON-NLS-1$
		cardMedia.add(videoPanel, "cell 0 1");		//$NON-NLS-1$

/*******************************
 * Setup final control buttons
 ******************************/
		btn_Save = new JButton(HG0547Msgs.Text_27);	// Save
		btn_Save.setToolTipText(HG0547Msgs.Text_28);	// Save the edited Event data
		btn_Save.setEnabled(false);
		contents.add(btn_Save, "cell 1 1, align right, gapx 20, tag ok"); //$NON-NLS-1$

		JButton btn_Close = new JButton(HG0547Msgs.Text_29);	// Close
		btn_Close.setToolTipText(HG0547Msgs.Text_30);	// Close this screen
		contents.add(btn_Close, "cell 1 1, align right, gapx 20, tag cancel"); //$NON-NLS-1$

//*******************
// Setup Focus Policy
//*******************
        Vector<Component> focusOrder = new Vector<>();
        focusOrder.add(btn_EventType);
        focusOrder.add(dateText);
        focusOrder.add(sortDateText);
        focusOrder.add(tableAssocs);
        focusOrder.add(tableLocation);
        focusOrder.add(memoText);
        focusOrder.add(tableCite);
        contents.setFocusCycleRoot(true);
        contents.setFocusTraversalPolicy(new focusPolicy(focusOrder));
	// Set initial focus of screen
        btn_EventType.requestFocusInWindow();
    // Enable event selection only for update event or select partner event
        if (this instanceof HG0547UpdateEvent || this instanceof HG0547PartnerEvent) {
			btn_EventType.setEnabled(true);
		} else {
			btn_EventType.setEnabled(false);
		}
   // Event type and role need to be changed from partner table
        if (eventGroup == pointWhereWhenHandler.marrGroup || eventGroup == pointWhereWhenHandler.divorceGroup)
			btn_EventType.setEnabled(false);

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
			} // return to main menu
		});

	// Listener to start date to enable save button
		dateText.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e){
            	btn_Save.setEnabled(true);
          // Initiate edit date window and preload
            	if (editStartDate != null)
					editStartDate.convertFromHDate(startMainYear, startMainDetails, startExtraYear, startExtraDetails);
				else {
            		editStartDate = new HG0590EditDate(pointOpenDisplay, false, false, false, true);
            		editStartDate.dateType = 1;  // set start date
            		if (startHREDate != null)
						editStartDate.convertFromHDate(startMainYear, startMainDetails, startExtraYear, startExtraDetails);
            	}
           // 	Set position and set visible
    			editStartDate.setModalityType(ModalityType.APPLICATION_MODAL);
    			Point xyDate = dateText.getLocationOnScreen();
    			editStartDate.setLocation(xyDate.x, xyDate.y-50);
    			editStartDate.setVisible(true);
            }
        });

	// Listener to sort date to enable save button
		sortDateText.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
            	btn_Save.setEnabled(true);
                // Initiate edit date window and preload
            	if (sortEndDate != null)
					sortEndDate.convertFromHDate(sortMainYear, sortMainDetails, sortExtraYear, sortExtraDetails);
				else {
            		sortEndDate = new HG0590EditDate(pointOpenDisplay, false, false, false, true);
            		sortEndDate.dateType = 3;  // set end date
            		sortEndDate.convertFromHDate(sortMainYear, sortMainDetails, sortExtraYear, sortExtraDetails);
            	}

           // 	Set position and set visible
    			sortEndDate.setModalityType(ModalityType.APPLICATION_MODAL);
    			Point xyDate = sortDateText.getLocationOnScreen();
    			sortEndDate.setLocation(xyDate.x, xyDate.y-50);
    			sortEndDate.setVisible(true);
            }
        });

	// Listener for edits of memoText
		memoTextChange = new DocumentListener() {
	        @Override
	        public void removeUpdate(DocumentEvent e) {
	           	memoEdited = true;
            	btn_Save.setEnabled(true);
	        }
	        @Override
	        public void insertUpdate(DocumentEvent e) {
	           	memoEdited = true;
            	btn_Save.setEnabled(true);
	        }
	        @Override
	        public void changedUpdate(DocumentEvent e) {
	           	memoEdited = true;
            	btn_Save.setEnabled(true);
	        }
	    };
	    memoText.getDocument().addDocumentListener(memoTextChange);

	// Listeners for radio button group items, to show each rightPanel card
		ActionListener actionRadioEvent = new ActionListener() {
		    @Override
			public void actionPerformed(ActionEvent actionEvent) {
		    	  CardLayout cl = (CardLayout)(rightPanel.getLayout());
		    	  cl.show(rightPanel, "EVENT");	//$NON-NLS-1$
		      }
		    };
		ActionListener actionRadioFlag = new ActionListener() {
		    @Override
			public void actionPerformed(ActionEvent actionEvent) {
		    	  CardLayout cl = (CardLayout)(rightPanel.getLayout());
		    	  cl.show(rightPanel, "FLAGS");	//$NON-NLS-1$
		      }
		    };
		ActionListener actionRadioMedia = new ActionListener() {
		    @Override
			public void actionPerformed(ActionEvent actionEvent) {
		    	  CardLayout cl = (CardLayout)(rightPanel.getLayout());
		    	  cl.show(rightPanel, "MEDIA");	//$NON-NLS-1$
		      }
		    };
		// Link the listeners above to the radio buttons
		radio_Event.addActionListener(actionRadioEvent);
		radio_Flag.addActionListener(actionRadioFlag);
		radio_Media.addActionListener(actionRadioMedia);

		// Define ActionListeners for Associates table right-click popupMenus
		// For popupMenu item popChange - Edit the clicked associate
	    ActionListener popChange = new ActionListener() {
	        @Override
			public void actionPerformed(ActionEvent e) {
		// The right-clicked row is passed here in rowClicked
        		int rowInTable = tableAssocs.convertRowIndexToModel(rowClicked);
        		String personClicked = (String) tableAssocsData[rowInTable][0];
				try {
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				// Activate HG0507SelectPerson code with event number, to change person at this row of the Assoc table
					HG0507SelectPerson personSelectScreen = pointPersonHandler.activateAssociateEdit(pointOpenProject,
																				eventNumber, rowInTable);
					personSelectScreen.setTitle(HG0547Msgs.Text_31 + personClicked);	// Edit associate:
					personSelectScreen.setModalityType(ModalityType.APPLICATION_MODAL);
					Point xyShow = dateText.getLocationOnScreen();
					personSelectScreen.setLocation(xyShow.x, xyShow.y);
					personSelectScreen.setVisible(true);
				} catch (HBException hbe) {
					System.out.println(" HG0547EditEvent - associate: " + hbe.getMessage());	//$NON-NLS-1$
						hbe.printStackTrace();
				}
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				btn_Save.setEnabled(true);
			}
	      };

	// For popupMenu item popMenuDel - Delete the clicked Assoc
	    ActionListener popDel = new ActionListener() {
	        @Override
			public void actionPerformed(ActionEvent e) {
		// The right-clicked row is passed here in rowClicked
        		int rowInTable = tableAssocs.convertRowIndexToModel(rowClicked);
        		String personClicked = (String) tableAssocsData[rowInTable][0];
        		if (JOptionPane.showConfirmDialog(tableAssocs, HG0547Msgs.Text_32	// Are you sure you want to delete \n
						  + HG0547Msgs.Text_33 + personClicked +"?",		// associate (name)?	//$NON-NLS-1$
						   HG0547Msgs.Text_34,						// Delete Associate
						   JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
					return;
				}
				try {
					pointWhereWhenHandler.deleteAssocPerson(rowInTable);
					btn_Save.setEnabled(true);
			// Reset Edit Event
					pointWhereWhenHandler.resetUpdateEvent(pointOpenProject);
				} catch (HBException hbe) {
					System.out.println(" Delete assoc error: " + hbe.getMessage());	//$NON-NLS-1$
					hbe.printStackTrace();
				}
	        }
	      };

	// For popupMenu item popMenuAdd - general Add Assoc
	    ActionListener popAdd = new ActionListener() {
	        @Override
			public void actionPerformed(ActionEvent e) {
				try {
					notAddAssoc = false;
					btn_Save.doClick(); // Save before adding associates

					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				// Activate  HG0507SelectPerson code with event number, to load selected person to Assoc table
					HG0507SelectPerson personSelectScreen = pointPersonHandler.activateAssociateAdd(pointOpenProject, eventNumber);
					personSelectScreen.setModalityType(ModalityType.TOOLKIT_MODAL);
					Point xyShow = dateText.getLocationOnScreen();
					personSelectScreen.setLocation(xyShow.x-100, xyShow.y);
					personSelectScreen.setVisible(true);
				} catch (HBException hbe) {
					System.out.println(" HG0547EditEvent - Add Assoc popup: " + hbe.getMessage());	//$NON-NLS-1$
					hbe.printStackTrace();
				}
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				btn_Save.setEnabled(true);
	        }
	      };

	// Define Assocs popup menu items and actions
	    JMenuItem popMenuEdit = new JMenuItem(HG0547Msgs.Text_35);	// Change this Associate
	    popMenuEdit.addActionListener(popChange);
	    JMenuItem popMenuDel = new JMenuItem(HG0547Msgs.Text_36);	// Delete this Associate
	    popMenuDel.addActionListener(popDel);
	    JMenuItem popMenuAdd = new JMenuItem(HG0547Msgs.Text_37);		// Add new Associate
	    popMenuAdd.addActionListener(popAdd);
	    JMenuItem popMenuAddOnly = new JMenuItem(HG0547Msgs.Text_37);	// Add new Associate
	    popMenuAddOnly.addActionListener(popAdd);
	    // Define a right-click popup menu to use in tableAssocs
	    JPopupMenu popupMenuAssoc = new JPopupMenu();
	    popupMenuAssoc.add(popMenuEdit);
	    popupMenuAssoc.add(popMenuDel);
	    popupMenuAssoc.add(popMenuAdd);
	    // and a popup menu for anywhere in the table
	    JPopupMenu popupMenuOnlyAdd = new JPopupMenu();
	    popupMenuOnlyAdd.add(popMenuAddOnly);
	// Listener for Assocs table mouse click
	    tableAssocs.addMouseListener(new MouseAdapter() {
			@Override
            public void mousePressed(MouseEvent me) {
                if (me.getButton() == MouseEvent.BUTTON3) {
                // RIGHT-CLICK
                	rowClicked = tableAssocs.rowAtPoint(me.getPoint());
                	if (tableAssocs.rowAtPoint(me.getPoint()) < 0)
						popupMenuOnlyAdd.show(me.getComponent(), me.getX(), me.getY());
					else
						popupMenuAssoc.show(me.getComponent(), me.getX(), me.getY());
                }
            }
        });

	// Listener for Event Type button
		btn_EventType.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// pointEditEvent does not allow action if eventGroup=6 (marriage type)
	        	HG0547TypeEvent eventTypeScreen = new HG0547TypeEvent(pointOpenProject, eventGroup, eventNum,
	        															eventRoleNum, pointEditEvent);
	        	eventTypeScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xyShow = btn_EventType.getLocationOnScreen();
				eventTypeScreen.setLocation(xyShow.x, xyShow.y);
				eventTypeScreen.setVisible(true);
				// When exit HG0547, do Save so that incorrect roles not shown for other Associates
				btn_Save.doClick(); // update 25.11.2024
			}
		});

	// Listener for Add Location Citation button
		btn_AddL.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// NOTE06 need code here show an AddCitation screen ( defined) & turn on btn_Save
			}
		});

	// Listener for Delete Location Citation button
		btn_DelL.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// NOTE06 need code here to delete selected Name Citation & turn on btn_Save
			}
		});

	// Listener for move Location Citation up button
		btn_UpL.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// NOTE06 need code here to move selected Citation up in table & turn on btn_Save
			}
		});

	// Listener for move Location Citation down button
		btn_DownL.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// NOTE06 need code here to move selected Citation down in table & turn on btn_Save
			}
		});

	// Listener for Add Image button
		btn_Image.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// NOTE07 need code here to action media Save & turn on btn_Save
			}
		});

	// Listener for Add Text files button
		btn_Text.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// NOTE07 need code here to action media Save & turn on btn_Save
			}
		});

	// Listener for Add Audio files button
		btn_Audio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// NOTE07 need code here to action media Save & turn on btn_Save
			}
		});

	// Listener for Add Video files button
		btn_Video.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// NOTE07 need code here to action media Save & turn on btn_Save
			}
		});

	// Listener for Sentence Editor button
		btn_Sentence.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// NOTE03 need code here to allow sentence editing
				JOptionPane.showMessageDialog(btn_Sentence, "This function is not yet implemented");	//$NON-NLS-1$
			}
		});

	// Listener for changes made in event location
		if (!(this instanceof HG0547UpdateEvent)) {
			eventLocationListener = new TableModelListener() {
				@Override
	           public void tableChanged(TableModelEvent tme) {
	                if (tme.getType() == TableModelEvent.UPDATE) {
	                    int row = tme.getFirstRow();
	                    if (row > -1) {
		                    String nameElementData = (String) tableLocation.getValueAt(row, 1);
							if (HGlobal.DEBUG) {
									String element = (String) tableLocation.getValueAt(row, 0);
									System.out.println(" HG0547EditEvent - location table changed: " + row //$NON-NLS-1$
											 + " Element: " + element + "/" + nameElementData); 	//$NON-NLS-1$ //$NON-NLS-2$
							}
							if (nameElementData != null) {
								pointWhereWhenHandler.addToLocationChangeList(tableLocation.getSelectedRow(), nameElementData);
								locationElementUpdate = true;
								btn_Save.setEnabled(true);
							}
	                    }
	                }
	            }
			};
		}
		tableLocation.getModel().addTableModelListener(eventLocationListener);

	// On selection of a locn style update the Location table
		if (!(this instanceof HG0547UpdateEvent)) {
			locationNameStyles.addActionListener (new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					if (!(locationNameStyles.getSelectedIndex() == -1)) {
						DefaultTableModel tableModel = (DefaultTableModel) tableLocation.getModel();
						tableModel.setNumRows(0);	// clear table
						tableLocationData = pointWhereWhenHandler.getLocationDataTable(locationNameStyles.getSelectedIndex());
						tableModel.setDataVector(tableLocationData, tableBirthHeader);
					// reset table rows
						tableModel.setRowCount(tableLocationData.length);
						changedLocationNameStyle = true;
						btn_Save.setEnabled(true);
					// reset screen size
						pack();
					}
				}
			});
		}

	// Listener for Save button
		if (!(this instanceof HG0547PartnerEvent)) {
			if (!(this instanceof HG0547UpdateEvent)) {
				btn_Save.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						// NOTE08 need code to check we have as many KEY_ASSOC roles selected as the Event requires
						if (HGlobal.writeLogs)
							HB0711Logging.logWrite("Action: accepting updates and leaving HG0547EditEvent");		//$NON-NLS-1$
						if (HGlobal.DEBUG)
							System.out.println(" Edit event save button: " + selectedEventNum + "/" + selectedRoleNum); //$NON-NLS-1$ //$NON-NLS-2$
						try {
						// Check if data updated
							if (locationElementUpdate || startDateOK || sortDateOK || memoEdited || changedLocationNameStyle) {

							// Create new event
								newEventPID = pointWhereWhenHandler.createNewEvent(selectedEventNum, selectedRoleNum);

							//if update memo text
								if (memoEdited)
									pointWhereWhenHandler.createFromGUIMemo(memoText.getText());

							// Create a new set of HDATE records
								if (startDateOK)
									pointWhereWhenHandler.createEventDates(false, newEventPID, "START_HDATE_RPID", startHREDate);  //$NON-NLS-1$

								if (sortDateOK)
									pointWhereWhenHandler.createEventDates(false, newEventPID, "SORT_HDATE_RPID", sortHREDate);		//$NON-NLS-1$

							// Update location name style
								if (changedLocationNameStyle && locationElementUpdate) {
								//if (changedLocationNameStyle) {
									selectedStyleIndex = locationNameStyles.getSelectedIndex();
									locationNamePID = pointWhereWhenHandler.getLocationNameRecordPID();
									//System.out.println(" Edit event - location name PID: " + locationNamePID);
									pointWhereWhenHandler.updateStoredNameStyle(selectedStyleIndex, locationNamePID);
								}

							} else System.out.println(" HG0547EditEvent - No edited data for event!"); //$NON-NLS-1$

							if (notAddAssoc) {
						// Reload Person windows
								pointOpenProject.reloadT401Persons();
								pointOpenProject.reloadT402Names();
								pointOpenProject.getPersonHandler().resetPersonSelect();
								pointOpenProject.getPersonHandler().resetPersonManager();
								pointEditEvent.dispose();
							}

						} catch (HBException hbe) {
							System.out.println("HG0547EditEvent - Failed to edit event: " + hbe.getMessage());		//$NON-NLS-1$
							JOptionPane.showMessageDialog(btn_Save, HG0547Msgs.Text_39 + hbe.getMessage(),
									HG0547Msgs.Text_40, JOptionPane.ERROR_MESSAGE);
							if (HGlobal.DEBUG)
								hbe.printStackTrace();
						}
					}
				});
			}
		}

	// Listener for Close button
		btn_Close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
			// Test for unsaved changes
			if (btn_Save.isEnabled() || memoEdited) {
				if (JOptionPane.showConfirmDialog(btn_Save,
						HG0547Msgs.Text_41		//There are unsaved changes. \n
						+ HG0547Msgs.Text_42,	// Do you still wish to exit this screen?
						HG0547Msgs.Text_43,		// Edit Event
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							if (HGlobal.writeLogs)
								HB0711Logging.logWrite("Action: cancelling out of HG0547EditEvent"); //$NON-NLS-1$

						// Yes option: clear Reminder and exit
							if (reminderDisplay != null)
								reminderDisplay.dispose();
							dispose();
						}
				} else {
					if (HGlobal.writeLogs)
						HB0711Logging.logWrite("Action: exiting HG0547EditEvent"); //$NON-NLS-1$
					if (reminderDisplay != null)
						reminderDisplay.dispose();
					dispose();
				}
			}
		});

	}	// End HG0547EditEvent constructor

/**
 * public void updateEventType(int selectedEventType, int selectedRole)
 * @param selectedEventType
 * @param selectedRole
 * @throws HBException
 */
	public void updateEventType(int selectedEventType, int selectedRole) throws HBException {
		eventName = pointWhereWhenHandler.getEventName(selectedEventType);
		roleName = pointWhereWhenHandler.getEventRoleName(selectedEventType, selectedRole);
		actualEvent.setText(eventName + " / " + roleName);	//$NON-NLS-1$
		selectedEventNum = selectedEventType;
		selectedRoleNum = selectedRole;
		changeEventType = true;
		btn_Save.setEnabled(true);
	}
	@Override
	public void saveStartDate() {
		if (editStartDate != null) {
			startMainYear = editStartDate.returnYearToGUI();
			startMainDetails = editStartDate.returnDetailToGUI().trim();
			startExtraYear = editStartDate.returnYear2ToGUI();
			startExtraDetails = editStartDate.returnDetail2ToGUI().trim();
			startSortCode = editStartDate.returnSortString().trim();
			pointWhereWhenHandler.setUpDateTranslation();
			dateText.setText(" " + pointWhereWhenHandler.formatDateSelector(startMainYear, startMainDetails, 	//$NON-NLS-1$
					startExtraYear, startExtraDetails).trim());
		}
		if (startHREDate == null)
			startHREDate = new Object[5];
		startHREDate[0] = startMainYear;
		startHREDate[1] = startMainDetails;
		startHREDate[2] = startExtraYear;
		startHREDate[3] = startExtraDetails;
		startHREDate[4] = startSortCode;
		startDateOK = true;

		if (sortHREDate == null) {
			sortHREDate = new Object[5];
			sortHREDate[0] = startMainYear;
			sortHREDate[1] = startMainDetails;
			sortHREDate[2] = startExtraYear;
			sortHREDate[3] = startExtraDetails;
			sortHREDate[4] = startSortCode;

			sortMainYear = startMainYear;
			sortMainDetails = startMainDetails;
			sortExtraYear = startExtraYear;
			sortExtraDetails = startExtraDetails;
			sortSortCode = startSortCode;

			pointWhereWhenHandler.setUpDateTranslation();
			sortDateText.setText(" " + pointWhereWhenHandler.formatDateSelector(sortMainYear, sortMainDetails, 	//$NON-NLS-1$
					sortExtraYear, sortExtraDetails).trim());
			sortDateOK = true;
		}
	}

	@Override
	public void saveSortDate() {
		if (sortEndDate != null) {
			sortMainYear = sortEndDate.returnYearToGUI();
			sortMainDetails = sortEndDate.returnDetailToGUI().trim();
			sortExtraYear = sortEndDate.returnYear2ToGUI();
			sortExtraDetails = sortEndDate.returnDetail2ToGUI().trim();
			sortSortCode = sortEndDate.returnSortString().trim();
			pointWhereWhenHandler.setUpDateTranslation();
			sortDateText.setText(" " + pointWhereWhenHandler.formatDateSelector(sortMainYear, sortMainDetails, 	//$NON-NLS-1$
					sortExtraYear, sortExtraDetails).trim());
		}
		if (sortHREDate == null)
			sortHREDate = new Object[5];
		sortHREDate[0] = sortMainYear;
		sortHREDate[1] = sortMainDetails;
		sortHREDate[2] = sortExtraYear;
		sortHREDate[3] = sortExtraDetails;
		sortHREDate[4] = sortSortCode;
		sortDateOK = true;
	}

}  // End of HG0547EditEvent
