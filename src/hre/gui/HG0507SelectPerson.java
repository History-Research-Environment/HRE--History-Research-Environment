package hre.gui;
/**************************************************************************************
 * SelectPerson - Original Specs 05.07 GUI_EntitySelect 2019-09-16
 * ***********************************************************************************
 * v0.03.0031 2024-03-10 Cut-down JDialog version of PersonSelect (D Ferguson)
 * 			  2024-03-12 Add parameter to set max.# of Assocs that can be selected (D Ferguson)
 * 			  2024-03-15 Fixed over-write of normal PersonSelect screen config (D Ferguson)
 * 			  2024-03-16 Added GUI Title parameter (D Ferguson)
 * 			  2024-03-29 Add 2nd screen layout (relationship/memo/cite) (D Ferguson)
 *			  2024-03-30 Remove ability to select more than 1 person (D Ferguson)
 *			  2024-04-02 Fix method of selecting row in person table (D Ferguson)
 *			  2024-05-08 Setting up edit select person (N. Tolleshaug)
 *			  2024-07-31 Revised HG0507SelectPerson buttons (N. Tolleshaug)
 *			  2024-11-05 Fix filter crashing issue (D Ferguson)
 * v0.04.0032 2024-12-31 Add citation select/up/down code (D Ferguson)
 * 			  2025-03-17 Adjust Citation table column sizes (D Ferguson)
 * 			  2025-04-24 Add citation add/delete/move code (D Ferguson)
 * 			  2025-04-27 Add citation panel for Partners (D Ferguson)
 * 			  2025-05-09 Reload associate and citation event add/edit(N.Tolleshaug)
 * 			  2025-05-25 Adjust structure of call to HG0555 (D Ferguson)
 *************************************************************************************
 * Notes for incomplete code still requiring attention
 * NOTE03 need to recognise the current setting of the person name style (fails somehow)
 * NOTE04 need to pass in the initial person (focusPersIDX)	as a new parameter
 * NOTE06 need to get memo data and enable edit/save
 ************************************************************************************/

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.ScrollPaneConstants;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.DefaultCaret;

import hre.bila.HB0711Logging;
import hre.bila.HBCitationSourceHandler;
import hre.bila.HBException;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.gui.HGlobalCode.JTableCellTabbing;
import hre.nls.HG05070Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Select Person
 * @author D Ferguson
 * @version v0.04.0032
 * @since 2024-03-10
 */

public class HG0507SelectPerson extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	public static final String screenID = "50700"; //$NON-NLS-1$

	HG0507SelectPerson thisSelectPerson = this;
	public HG0547EditEvent pointEditEvent;
    public HBPersonHandler pointPersonHandler;
    HG0507PersonSelect personSelect;
	HBProjectOpenData pointOpenProject;
	HG0507SelectPerson pointSelectPerson = this;
	public HBCitationSourceHandler pointCitationSourceHandler;
	HG0507SelectPerson personFrame = this;

	public String selectTitle, newTitle, addTitle;
	private JPanel contents;
	JPanel persRolePanel;
	JPanel findPanel;
	JPanel personPanel;
	JPanel control1Panel;
	JPanel memoPanel;
	JPanel citePanel;
	JPanel control2Panel;
	boolean addRelation;
	DefaultTableModel citeModel;

	private String selectString = ""; //$NON-NLS-1$
	private JCheckBox chkbox_Filter;
	private int focusPersIDX = 1;	// default start person	// see NOTE04
	private int foundRow;
	private int clickedRow, selectedRowInTable;
	public long personPID;
	private String idText, allColumnsText1, allColumnsText2;
	String[] tablePersColHeads = null;

    JScrollPane scrollTable;
	JComboBox<String> comboBox_Subset;

    JTextArea memoText; 				// accessed by update
	DocumentListener memoTextChange;	// accessed by update
    boolean memoEdited = false; 		// Signals memo is edited
    boolean citationOrderChanged = false;	// Signals citation added or order changed

    // Added for partner select
	JComboBox<String> comboPartRole1, comboPartRole2;
	int[] partnerEventType = null;
	String[] partnerRoleList = null;
	int[] partnerRoleType = null;

	String[] partnerEventList;
	String newSelectedName;
	JLabel lbl_nRole1, lbl_nRole2, lbl_ParentName;

	public JButton btn_SaveEvent;
	public JButton btn_Save;

	private Object[][] tablePersData;
	private JTable tablePersons;
	DefaultTableModel myTableModel = null;

	Object[][] objCiteData;
	Object objCiteDataToEdit[] = new Object[2]; // to hold data to pass to Citation editor
	Object objTempCiteData[] = new Object[2];   // to temporarily hold a row of data when moving rows around
	String[] tableCiteHeader;
	String citeTableName = "";

	JLabel lbl_Relate;
	JComboBox<String> comboBox_Relationships;
	int[] eventRoleTypes;

    // For Text area font setting
    Font font = UIManager.getFont("TextArea.font");		//$NON-NLS-1$

/**
 * Create the dialog with access to the Open Project
 * @param pointPersonHandler
 * @param pointOpenProject
 * @param titleType for use in setting dialog title
 * @throws HBException
 */
	public HG0507SelectPerson(HBPersonHandler pointPersonHandler,
								 HBProjectOpenData pointOpenProject, boolean addRela) throws HBException {

		this.pointOpenProject = pointOpenProject;
		this.pointPersonHandler = pointPersonHandler;
		pointCitationSourceHandler = pointOpenProject.getCitationSourceHandler();
		this.addRelation = addRela;
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0507SelectPerson");	//$NON-NLS-1$

		// Setup references for HG0450
		windowID = screenID;
		helpName = "selectperson";		 //$NON-NLS-1$
		lbl_nRole2 = new JLabel(HG05070Msgs.Text_130);	// New Selected?

		// Set focus person that will be first in table
		focusPersIDX = pointOpenProject.getSelectedPersonIndex();  // see NOTE04 - not active yet

		// Collect static GUI text from T204 for filter drop-down
		String[] translatedTexts = pointPersonHandler.setTranslatedData(screenID, "0", false);		//$NON-NLS-1$
		idText = translatedTexts[0];
		allColumnsText1 = translatedTexts[1];
		allColumnsText2 = translatedTexts[2];

		// Collect static GUI text from T204 for Person table
		String[] tableHeaders = pointPersonHandler.setTranslatedData(screenID, "1", false);		//$NON-NLS-1$

		// Collect static GUI text from T204 for Citation table
		tableCiteHeader = pointPersonHandler.setTranslatedData("50500", "1", false); // Source#, Source, 1 2 D P M  //$NON-NLS-1$ //$NON-NLS-2$

		// Setup final table column headers - just use ID, Name, Birth data, Death date
		tablePersColHeads = new String[4];
		tablePersColHeads[0] = (String) tableHeaders[0];
		tablePersColHeads[1] = (String) tableHeaders[1];
		tablePersColHeads[2] = (String) tableHeaders[2];
		tablePersColHeads[3] = (String) tableHeaders[4];

		// Setup close action
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		// Setup dialog
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 5", "[]10[]", "[][][][]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	// Define panel for Find/Filter controls
		findPanel = new JPanel();
		findPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		findPanel.setLayout(new MigLayout("insets 5", "[]10[]", "[]5[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		JLabel lbl_Search = new JLabel(HG05070Msgs.Text_23);		// Find within Name:
		findPanel.add(lbl_Search, "cell 0 0, alignx right"); //$NON-NLS-1$

		JTextField searchField = new JTextField();
		searchField.setColumns(30);
		searchField.setToolTipText(HG05070Msgs.Text_25);			// Text to be searched for
		findPanel.add(searchField, "cell 1 0"); //$NON-NLS-1$

		JButton searchPrevious = new JButton(HG05070Msgs.Text_19);	// Find Previous
		searchPrevious.setToolTipText(HG05070Msgs.Text_20);			// Search for previous text position
		searchPrevious.setEnabled(false);
		findPanel.add(searchPrevious, "cell 1 1"); //$NON-NLS-1$

		JButton searchNext = new JButton(HG05070Msgs.Text_27);		// Find Next
		searchNext.setToolTipText(HG05070Msgs.Text_28);				// Search for next text position
		searchNext.setEnabled(false);
		findPanel.add(searchNext, "cell 1 1, gapx 10"); //$NON-NLS-1$

		JCheckBox chkbox_ignoreDiacriticFind = new JCheckBox(HG05070Msgs.Text_21);	// Ignore Diacritics
		chkbox_ignoreDiacriticFind.setHorizontalTextPosition(SwingConstants.LEADING);
		chkbox_ignoreDiacriticFind.setHorizontalAlignment(SwingConstants.LEFT);
		chkbox_ignoreDiacriticFind.setToolTipText(HG05070Msgs.Text_22);				// If ticked, diacritics are ignored during Find
		findPanel.add(chkbox_ignoreDiacriticFind, "cell 1 1, gapx 10"); //$NON-NLS-1$

		JLabel lbl_TxtFilter = new JLabel(HG05070Msgs.Text_33);		// Text to filter for:
		findPanel.add(lbl_TxtFilter, "cell 0 2, alignx right"); //$NON-NLS-1$

		JTextField filterTextField = new JTextField();
		filterTextField.setColumns(10);
		filterTextField.setToolTipText(HG05070Msgs.Text_35);		// Name of stored filter
		findPanel.add(filterTextField, "flowx,cell 1 2"); //$NON-NLS-1$

		chkbox_Filter = new JCheckBox(HG05070Msgs.Text_37);		// Filter
		chkbox_Filter.setHorizontalTextPosition(SwingConstants.LEADING);
		chkbox_Filter.setHorizontalAlignment(SwingConstants.LEFT);
		chkbox_Filter.setToolTipText(HG05070Msgs.Text_38);		// On if a filter in use
		findPanel.add(chkbox_Filter, "cell 1 2, gapx 20"); //$NON-NLS-1$

		JLabel lbl_Subsets = new JLabel(HG05070Msgs.Text_40);	// Select:
		lbl_Subsets.setToolTipText(HG05070Msgs.Text_41);		// Select filter or Subset name
		findPanel.add(lbl_Subsets, "cell 1 2, gapx 20"); //$NON-NLS-1$

		comboBox_Subset = new JComboBox<>();
		comboBox_Subset.setToolTipText(HG05070Msgs.Text_43);	// List of saved filter and subset names

		for (int i = 1; i < tablePersColHeads.length; i++) {
			comboBox_Subset.addItem(tablePersColHeads[i]);
		}
		comboBox_Subset.addItem(idText);					// ID
		comboBox_Subset.addItem(allColumnsText1);			// All Columns
		findPanel.add(comboBox_Subset, "cell 1 2"); //$NON-NLS-1$

		contents.add(findPanel, "cell 0 0, grow, hidemode 3"); //$NON-NLS-1$

	// Define panel for Person list
		personPanel = new JPanel();
		personPanel.setLayout(new MigLayout("insets 0", "[]", "[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		// scrollPane contains the Person Select picklist
		scrollTable = new JScrollPane();
		scrollTable.setPreferredSize(new Dimension(570, 400));

		// Setup JTable to show person data
		tablePersons = new JTable() {
			private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
			}};
		tablePersons.setMaximumSize(new Dimension(32767, 32767));
		tablePersons.setFillsViewportHeight(true);
		// Setup tabbing within table against all rows but only column 1-3
		if (tablePersons.getRowCount() > 0)
					JTableCellTabbing.setTabMapping(tablePersons, 0, tablePersons.getRowCount(), 1, 3);
		personPanel.add(scrollTable, "cell 0 0"); //$NON-NLS-1$
		contents.add(personPanel, "cell 0 1, grow, hidemode 3"); //$NON-NLS-1$

	// Define panel for first control button set
		control1Panel = new JPanel();
		control1Panel.setLayout(new MigLayout("insets 5", "[]", "[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		JButton btn_Cancel1 = new JButton(HG05070Msgs.Text_131);		// Cancel
		btn_Cancel1.setEnabled(true);
		control1Panel.add(btn_Cancel1, "cell 0 0, alignx right, gapx 10, tag cancel"); //$NON-NLS-1$

		JButton btn_Select = new JButton(HG05070Msgs.Text_132);		// Select
		btn_Select.setEnabled(false);
		control1Panel.add(btn_Select, "cell 0 0, alignx right, gapx 10, tag ok"); //$NON-NLS-1$
		contents.add(control1Panel, "cell 0 3, align right, hidemode 3"); //$NON-NLS-1$

	// Define Alternate panels for use after Select button clicked
	// Define panel for Person name and role
		persRolePanel = new JPanel();
		persRolePanel.setVisible(false);
		persRolePanel.setLayout(new MigLayout("insets 5", "[]50[]", "[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		JLabel lbl_Parent = new JLabel();
		lbl_Parent.setText(newTitle); // Set new title for window
		lbl_Parent.setFont(lbl_Parent.getFont().deriveFont(lbl_Parent.getFont().getStyle() | Font.BOLD));
		persRolePanel.add(lbl_Parent, "cell 0 0, alignx left"); //$NON-NLS-1$

		lbl_Relate = new JLabel();
		lbl_Relate.setFont(lbl_Relate.getFont().deriveFont(lbl_Relate.getFont().getStyle() | Font.BOLD));
		persRolePanel.add(lbl_Relate, "cell 1 0, alignx left,"); //$NON-NLS-1$

		lbl_ParentName = new JLabel(HG05070Msgs.Text_133);	// Parent dummy
		persRolePanel.add(lbl_ParentName, "cell 0 1, alignx left"); //$NON-NLS-1$

		comboBox_Relationships = new JComboBox<>();
		persRolePanel.add(comboBox_Relationships, "cell 1 1, alignx left"); //$NON-NLS-1$
		contents.add(persRolePanel, "cell 0 0, grow, hidemode 3"); //$NON-NLS-1$

	// Define panel for Memo
		memoPanel = new JPanel();
		memoPanel.setVisible(false);
		memoPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		memoPanel.setLayout(new MigLayout("insets 5", "[]", "[]5[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		JLabel lbl_Memo = new JLabel(HG05070Msgs.Text_134);		// Memo:
		lbl_Memo.setFont(lbl_Memo.getFont().deriveFont(lbl_Memo.getFont().getStyle() | Font.BOLD));
		memoPanel.add(lbl_Memo, "cell 0 0, alignx left"); //$NON-NLS-1$

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
		memoTextScroll.setMinimumSize(new Dimension(570, 100));
		memoTextScroll.getViewport().setOpaque(false);
		memoTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		memoText.setCaretPosition(0);	// set scrollbar to top
		memoPanel.add(memoTextScroll, "cell 0 1, aligny top"); //$NON-NLS-1$
		contents.add(memoPanel, "cell 0 1, grow, hidemode 3"); //$NON-NLS-1$

	// Define panel for Citations (for Parents only)
		citePanel = new JPanel();
		citePanel.setVisible(false);
		citePanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		citePanel.setLayout(new MigLayout("insets 5", "[][grow][80!]", "[]5[grow]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		JLabel lbl_Citation = new JLabel(HG05070Msgs.Text_135);		// Citations:
		lbl_Citation.setFont(lbl_Citation.getFont().deriveFont(lbl_Citation.getFont().getStyle() | Font.BOLD));
		citePanel.add(lbl_Citation, "cell 0 0, alignx left"); //$NON-NLS-1$

		JButton btn_Add = new JButton("+"); //$NON-NLS-1$
		btn_Add.setFont(new Font("Arial", Font.BOLD, 12)); //$NON-NLS-1$
		btn_Add.setMaximumSize(new Dimension(24, 24));
		btn_Add.setEnabled(true);
		citePanel.add(btn_Add, "cell 1 0, alignx center, aligny top"); //$NON-NLS-1$

		JButton btn_Del = new JButton("-"); //$NON-NLS-1$
		btn_Del.setFont(new Font("Arial", Font.BOLD, 12));	//$NON-NLS-1$
		btn_Del.setMaximumSize(new Dimension(24, 24));
		btn_Del.setEnabled(false);
		citePanel.add(btn_Del, "cell 1 0, aligny top"); //$NON-NLS-1$

		ImageIcon upArrow = new ImageIcon(getClass().getResource("/hre/images/arrow_up16.png")); //$NON-NLS-1$
		JButton btn_Up = new JButton(upArrow);
		btn_Up.setVerticalAlignment(SwingConstants.TOP);
		btn_Up.setToolTipText(HG05070Msgs.Text_136);	// Moves Citation up the list
		btn_Up.setMaximumSize(new Dimension(20, 20));
		btn_Up.setEnabled(false);
		citePanel.add(btn_Up, "cell 1 0, aligny top, gapx 10"); //$NON-NLS-1$

		ImageIcon downArrow = new ImageIcon(getClass().getResource("/hre/images/arrow_down16.png")); //$NON-NLS-1$
		JButton btn_Down = new JButton(downArrow);
		btn_Down.setVerticalAlignment(SwingConstants.TOP);
		btn_Down.setToolTipText(HG05070Msgs.Text_137);	// Moves Citation down the list
		btn_Down.setMaximumSize(new Dimension(20, 20));
		btn_Down.setEnabled(false);
		citePanel.add(btn_Down, "cell 1 0, aligny top"); //$NON-NLS-1$

		JLabel lbl_Surety = new JLabel(HG05070Msgs.Text_138);	// Surety
		citePanel.add(lbl_Surety, "cell 2 0");		//$NON-NLS-1$

		// Create scrollpane and table for the Citations; HG0507SelectParent/Partner will load the citation data
		citeModel = new DefaultTableModel(objCiteData, tableCiteHeader);
		JTable tableCite = new JTable(citeModel) {
			private static final long serialVersionUID = 1L;
				@Override
				public Dimension getPreferredScrollableViewportSize() {
					// Force a 4-row table, even if empty
					int r = super.getRowCount();
					if (r < 4) r = 4;
					  return new Dimension(super.getPreferredSize().width,
					    		r * super.getRowHeight());
					 }
				@Override
				public boolean isCellEditable(int row, int col) {
						return false;
				}
			};
		tableCite.getColumnModel().getColumn(0).setMinWidth(30);
		tableCite.getColumnModel().getColumn(0).setPreferredWidth(70);
		tableCite.getColumnModel().getColumn(1).setMinWidth(100);
		tableCite.getColumnModel().getColumn(1).setPreferredWidth(420);
		tableCite.getColumnModel().getColumn(2).setMinWidth(80);
		tableCite.getColumnModel().getColumn(2).setPreferredWidth(80);
		tableCite.setAutoCreateColumnsFromModel(false);	// preserve column setup
		JTableHeader citeHeader = tableCite.getTableHeader();
		citeHeader.setOpaque(false);
		ListSelectionModel citeSelectionModel = tableCite.getSelectionModel();
		citeSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	// Setup scrollpane
		tableCite.setFillsViewportHeight(true);
		JScrollPane citeScrollPane = new JScrollPane(tableCite);
		citeScrollPane.setFocusTraversalKeysEnabled(false);
		citeScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		citeScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
	// Set Source#, Surety to be center-aligned
		DefaultTableCellRenderer centerLabelRenderer = new DefaultTableCellRenderer();
		centerLabelRenderer.setHorizontalAlignment(JLabel.CENTER);
		tableCite.getColumnModel().getColumn(0).setCellRenderer(centerLabelRenderer);
		tableCite.getColumnModel().getColumn(2).setCellRenderer(centerLabelRenderer);
	// Setup tabbing within table against all rows but only column 1, 2
		if (tableCite.getRowCount() > 0)
					JTableCellTabbing.setTabMapping(tableCite, 0, tableCite.getRowCount(), 1, 2);
		// Add to citePanel
		citePanel.add(citeScrollPane, "cell 0 1 3 1, alignx left, aligny top");	//$NON-NLS-1$
		contents.add(citePanel, "cell 0 2, grow, hidemode 3"); //$NON-NLS-1$

	// Define panel for second control button set
		control2Panel = new JPanel();
		control2Panel.setVisible(false);
		control2Panel.setLayout(new MigLayout("insets 10", "[]", "[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		btn_SaveEvent = new JButton(HG05070Msgs.Text_139);		// Partner & Event
		btn_SaveEvent.setEnabled(false);
		if (addRelation)
			control2Panel.add(btn_SaveEvent, "cell 0 0, align right, gapx 10, tag ok"); //$NON-NLS-1$
		btn_Save = new JButton(HG05070Msgs.Text_140);		// Save
			control2Panel.add(btn_Save, "cell 0 0, align right, gapx 10, tag ok");	//$NON-NLS-1$
		JButton btn_Cancel2 = new JButton(HG05070Msgs.Text_141);	// Cancel
		btn_Cancel2.setEnabled(true);
		control2Panel.add(btn_Cancel2, "cell 0 0, align right, gapx 10, tag cancel"); //$NON-NLS-1$
		contents.add(control2Panel, "cell 0 3, align right, hidemode 3"); //$NON-NLS-1$
	// End of Panel Definitions

 	// Set project Name Display Index
 		int nameDisplayIndex = pointOpenProject.getNameDisplayIndex();
 		pointPersonHandler.setNameDisplayIndex(nameDisplayIndex);

 	// turn off progress bar
 		pointPersonHandler.enableUpdateMonitor(false);

 	// Get person data
 		pointPersonHandler.initiatePersonDataMin(tablePersColHeads.length, pointOpenProject);
		tablePersData = pointPersonHandler.createSelectPersonList(tablePersColHeads.length, pointOpenProject);

 	// Setup tablePersData, model and renderer
		if (tablePersData == null ) {
			JOptionPane.showMessageDialog(scrollTable, HG05070Msgs.Text_109		// No data found in HRE database\n
													 + HG05070Msgs.Text_110,	// Person Select error
													   HG05070Msgs.Text_111, 	// Person Select
													   JOptionPane.ERROR_MESSAGE);
			dispose();
		}

		myTableModel = new DefaultTableModel(
    		tablePersData, tablePersColHeads) {
				private static final long serialVersionUID = 1L;
				@SuppressWarnings({ "unchecked", "rawtypes" })
				@Override
				public Class getColumnClass(int column) {
						return getValueAt(0, column).getClass();
			}
		};
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment( SwingConstants.CENTER );
        tablePersons.setDefaultRenderer(Integer.class, centerRenderer);
        tablePersons.setModel(myTableModel);
		tablePersons.getColumnModel().getColumn(0).setMinWidth(50);
		tablePersons.getColumnModel().getColumn(0).setPreferredWidth(50);
		tablePersons.getColumnModel().getColumn(1).setMinWidth(50);
		tablePersons.getColumnModel().getColumn(1).setPreferredWidth(250);
		tablePersons.getColumnModel().getColumn(2).setMinWidth(90);
		tablePersons.getColumnModel().getColumn(2).setPreferredWidth(120);
		tablePersons.getColumnModel().getColumn(3).setMinWidth(90);
		tablePersons.getColumnModel().getColumn(3).setPreferredWidth(120);
		tablePersons.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		// Set the ability to sort on columns
		tablePersons.setAutoCreateRowSorter(true);
	    TableModel myModel = tablePersons.getModel();
	    TableRowSorter<TableModel> sorter = new TableRowSorter<>(myModel);
		List <RowSorter.SortKey> psortKeys1 = new ArrayList<>();

		// Presort on column 1
		psortKeys1.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
		sorter.setSortKeys(psortKeys1);
	    tablePersons.setRowSorter(sorter);

	    // Set tooltips and header format
		tablePersons.getTableHeader().setToolTipText(HG05070Msgs.Text_80);	// Click to sort; Click again to sort in reverse order
		JTableHeader pHeader = tablePersons.getTableHeader();
		pHeader.setOpaque(false);
		TableCellRenderer prendererFromHeader = tablePersons.getTableHeader().getDefaultRenderer();
		JLabel pheaderLabel = (JLabel) prendererFromHeader;
		pheaderLabel.setHorizontalAlignment(SwingConstants.CENTER);

		// Set row selection action - allow multiple selections
		ListSelectionModel rowSelectionModel = tablePersons.getSelectionModel();
		rowSelectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// Show the table
		scrollTable.setViewportView(tablePersons);

		// Now set the sorted table view to start at the required focus person
		tablePersons.setRowSelectionAllowed(true);
		for(int i = 0; i < tablePersons.getRowCount(); i++){
			if(tablePersons.getValueAt(i, 0).equals(focusPersIDX)) {		// NOTE04
		        	tablePersons.changeSelection(i, 0, true, true);
		        	break;
		        }
		}
		tablePersons.requestFocus();

		// Set the dialog visible with normal cursor
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

		// Focus Policy still to be setup!
		pack();

/*****************************
 * CREATE All ACTION LISTENERS
 *****************************/
		// Listener for clicking 'X' on screen
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
		    	btn_Cancel1.doClick();
			}
		});

		// btn_Cancel1 listener
		btn_Cancel1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: cancelling out of HG0507SelectPerson 1st phase"); //$NON-NLS-1$
		    	dispose();
			}
		});
		// btn_Cancel2 listener
		btn_Cancel2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: cancelling out of HG0507SelectPerson 2nd phase"); //$NON-NLS-1$
		    	dispose();
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

		// Find within Name field listener
	    searchField.addActionListener(new ActionListener() {
	        @Override
			public void actionPerformed(ActionEvent e) {
	        // First, find how many rows are visible in current scrollpane's size, and half it
	        	JViewport viewport = scrollTable.getViewport();
	            Dimension extentSize = viewport.getExtentSize();
	            int halfVisibleRows = (extentSize.height/tablePersons.getRowHeight())/2;
		        Rectangle viewRect = viewport.getViewRect();
	        // Now find the first table row where beginning of Name matches search value
	            String searchValue = searchField.getText();
	         // If Ignore Diacritics checked, remove diacritics from searchValue
	            if (chkbox_ignoreDiacriticFind.isSelected())
	            	searchValue = Normalizer.normalize(searchValue, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", ""); //$NON-NLS-1$ //$NON-NLS-2$
	            for (int row = 0; row <= tablePersons.getRowCount() - 1; row++) {
	            		String tableValue = (String) tablePersons.getValueAt(row, 1);
	            		// If Ignore Diacritics checked, remove diacritics from tableValue
			            if (chkbox_ignoreDiacriticFind.isSelected())
			            	tableValue = Normalizer.normalize(tableValue, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", ""); //$NON-NLS-1$ //$NON-NLS-2$
   	                    if (tableValue.toLowerCase().contains(searchValue.toLowerCase())) {
   	                    // set the found row to the middle of the scrollpane; adjusted for scroll-up or down
   	                    	int first = tablePersons.rowAtPoint(new Point(0, viewRect.y));
   	                    	if (first > row) halfVisibleRows = -halfVisibleRows;
   	                    	tablePersons.scrollRectToVisible(tablePersons.getCellRect(row + halfVisibleRows, 1, true));
   	                    // set the 'found' row as selected, clear any other selection, save it
   	                    	tablePersons.changeSelection(row, 0, false, false);
   	                   		foundRow = row;
   	                   		searchNext.setEnabled(true);
   	                    	return;
   	                    }
	            }
	            JOptionPane.showMessageDialog(searchField, HG05070Msgs.Text_84 		// Cannot find
	            							+ searchValue + HG05070Msgs.Text_85,	//  in Names
	            											HG05070Msgs.Text_86,	// Search Result
	            											JOptionPane.INFORMATION_MESSAGE);
	        }
	    });

		// Find Next button listener
	    searchNext.addActionListener(new ActionListener() {
	        @Override
			public void actionPerformed(ActionEvent e) {
	        // First, find how many rows are visible in current scrollpane's size, and half it
	        	JViewport viewport = scrollTable.getViewport();
	            Dimension extentSize = viewport.getExtentSize();
	            int halfVisibleRows = (extentSize.height/tablePersons.getRowHeight())/2;
		        Rectangle viewRect = viewport.getViewRect();
	        // Now find first table row after last found row which matches search value
	            String searchValue = searchField.getText();
	        // If Ignore Diacritics checked, remove diacritics from searchValue
	            if (chkbox_ignoreDiacriticFind.isSelected())
	            	searchValue = Normalizer.normalize(searchValue, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", ""); //$NON-NLS-1$ //$NON-NLS-2$
	            foundRow = foundRow + 1;
	            for (int row = foundRow; row <= tablePersons.getRowCount() - 1; row++) {
	            		String tableValue = (String) tablePersons.getValueAt(row, 1);
	            		// If Ignore Diacritics checked, remove diacritics from tableValue
			            if (chkbox_ignoreDiacriticFind.isSelected())
			            	tableValue = Normalizer.normalize(tableValue, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", ""); //$NON-NLS-1$ //$NON-NLS-2$
   	                    if (tableValue.toLowerCase().contains(searchValue.toLowerCase())) {
   	                    // set the found row to the middle of the scrollpane; adjusted for scroll-up or down
   	                    	int first = tablePersons.rowAtPoint(new Point(0, viewRect.y));
   	                    	if (first > row) halfVisibleRows = -halfVisibleRows;
   	                    	tablePersons.scrollRectToVisible(tablePersons.getCellRect(row + halfVisibleRows, 1, true));
   	                    // set the 'found' row as selected, clear any other selection, save it
   	                    	tablePersons.changeSelection(row, 0, false, false);
   	                    	foundRow = row;
   	                    	searchPrevious.setEnabled(true);
   	                    	return;
   	                    }
	            }
   	            JOptionPane.showMessageDialog(searchNext, HG05070Msgs.Text_87 		// No more occurrences of
   	            							+ searchValue + HG05070Msgs.Text_88, 	//  in Names
   	            											HG05070Msgs.Text_89,	// Search Result
   	            											JOptionPane.INFORMATION_MESSAGE);
	        }
	    });

		// Find Previous button listener
	    searchPrevious.addActionListener(new ActionListener() {
	        @Override
			public void actionPerformed(ActionEvent e) {
	        // First, find how many rows are visible in current scrollpane's size, and half it
	        	JViewport viewport = scrollTable.getViewport();
	            Dimension extentSize = viewport.getExtentSize();
	            int halfVisibleRows = (extentSize.height/tablePersons.getRowHeight())/2;
		        Rectangle viewRect = viewport.getViewRect();
	        // Now find first table row after last found row which matches search value
	            String searchValue = searchField.getText();
	        // If Ignore Diacritics checked, remove diacritics from searchValue
	            if (chkbox_ignoreDiacriticFind.isSelected())
	            	searchValue = Normalizer.normalize(searchValue, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", ""); //$NON-NLS-1$ //$NON-NLS-2$
	            foundRow = foundRow - 2;
	            for (int row = foundRow; row >= 0; row--) {
	            		String tableValue = (String) tablePersons.getValueAt(row, 1);
	            		// If Ignore Diacritics checked, remove diacritics from tableValue
			            if (chkbox_ignoreDiacriticFind.isSelected())
			            	tableValue = Normalizer.normalize(tableValue, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", ""); //$NON-NLS-1$ //$NON-NLS-2$
   	                    if (tableValue.toLowerCase().contains(searchValue.toLowerCase())) {
   	                    // set the found row to the middle of the scrollpane; adjusted for scroll-up or down
   	                    	int first = tablePersons.rowAtPoint(new Point(0, viewRect.y));
   	                    	if (first > row) halfVisibleRows = -halfVisibleRows;
   	                    	tablePersons.scrollRectToVisible(tablePersons.getCellRect(row + halfVisibleRows, 1, true));
   	                    // set the 'found' row as selected, clear any other selection, save it
   	                    	tablePersons.changeSelection(row, 0, false, false);
   	                    	foundRow = row;
   	                    	return;
   	                    }
	            }
   	            JOptionPane.showMessageDialog(searchNext, HG05070Msgs.Text_87 		// No more occurrences of
   	            							+ searchValue + HG05070Msgs.Text_88, 	//  in Names
   	            											HG05070Msgs.Text_89,	// Search Result
   	            											JOptionPane.INFORMATION_MESSAGE);
	        }
	    });

	// Filter Text field listener
		filterTextField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// no action here
			}
		});

	// Filter checkbox listener
		chkbox_Filter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			// Reset filter for whole table when unselected, sorted as before
				if (!chkbox_Filter.isSelected()) {
						setTableFilter(allColumnsText2, ""); //$NON-NLS-1$  // All Columns, allColumnsText2
						comboBox_Subset.setSelectedIndex(0);
						tablePersons.setRowSorter(sorter);
				}
			}
		});

		// On selection of a Subset perform appropriate action
		comboBox_Subset.addActionListener (new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (chkbox_Filter.isSelected()) {
					selectString = comboBox_Subset.getSelectedItem().toString();
					setTableFilter(selectString, filterTextField.getText());
				}
			}
		});

		// Listener for tablePersons row selection of a relation to be added
		tablePersons.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent selectPer) {
				if (!selectPer.getValueIsAdjusting()) {
				// Return if filter location triggers select in table
					if (tablePersons.getSelectedRow() == -1) return;
				// Otherwise, find personPID clicked
					clickedRow = tablePersons.getSelectedRow();
					selectedRowInTable = tablePersons.convertRowIndexToModel(clickedRow);
					personPID = pointPersonHandler.getPersonTablePID(selectedRowInTable);
				// Get the person's name
					lbl_ParentName.setText((String) tablePersData[selectedRowInTable][1]);
					newSelectedName = (String) tablePersData[selectedRowInTable][1];
					lbl_nRole2.setText("" + newSelectedName);		//$NON-NLS-1$
					btn_Select.setEnabled(true);
				}
			}
        });

		// Listener for Select button
		btn_Select.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
			// Hide the Find/Filter & Person panels
				findPanel.setVisible(false);
				personPanel.setVisible(false);
				control1Panel.setVisible(false);

			// Display the Relationship/memo/Citation panels instead
				persRolePanel.setVisible(true);
				memoPanel.setVisible(true);
				// but don't show citation panel for associates
				if (thisSelectPerson instanceof HG0507SelectParent) citePanel.setVisible(true);
				if (thisSelectPerson instanceof HG0507SelectPartner) citePanel.setVisible(true);
				control2Panel.setVisible(true);

			// Change the title from Select to Add
				setTitle(addTitle);
				pack();
			}
		});

		// Listener for Citation table mouse clicks
		tableCite.addMouseListener(new MouseAdapter() {
			@Override
	        public void mousePressed(MouseEvent me) {
	           	if (me.getClickCount() == 1 && tableCite.getSelectedRow() != -1) {
	           	// SINGLE-CLICK - turn on table controls
	        		btn_Del.setEnabled(true);
	        		btn_Up.setEnabled(true);
	        		btn_Down.setEnabled(true);
	           	}
        		// DOUBLE_CLICK - get Object to pass to  Citation Editor and do so
	           	if (me.getClickCount() == 2 && tableCite.getSelectedRow() != -1) {
	           		int atRow = tableCite.getSelectedRow();
	           		objCiteDataToEdit = objCiteData[atRow]; // select whole row
		        	// Display HG0555EditCitation with this data - keyAssocMin = 1 by default
						HG0555EditCitation citeScreen
										= new HG0555EditCitation(false, pointOpenProject, citeTableName, 1, (long)objCiteDataToEdit[3]);
						citeScreen.pointSelectPerson = pointSelectPerson;
						citeScreen.setModalityType(ModalityType.APPLICATION_MODAL);
						Point xyCite = lbl_Relate.getLocationOnScreen();
						citeScreen.setLocation(xyCite.x, xyCite.y + 30);
						citeScreen.setVisible(true);
						btn_Save.setEnabled(true);
	           	}
	        }
	    });

		// Listener for Add Citation button
		btn_Add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				pointCitationSourceHandler.setCitedTableData(citeTableName, personPID);
				// NB keyAssocMin = 1 by default as follows
				HG0555EditCitation citeScreen = new HG0555EditCitation(true, pointOpenProject, citeTableName, 1);
				citeScreen.pointSelectPerson = pointSelectPerson;
				citeScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xyCite = lbl_Relate.getLocationOnScreen();
				citeScreen.setLocation(xyCite.x, xyCite.y + 30);
				citeScreen.setVisible(true);
				btn_Save.setEnabled(true);
				citationOrderChanged = true;
			}
		});

		// Listener for Delete Citation button
		btn_Del.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
           		int atRow = tableCite.getSelectedRow();
           		objCiteDataToEdit = objCiteData[atRow]; // select whole row
           		try {
					pointCitationSourceHandler.deleteCitationRecord((long) objCiteDataToEdit[3]);
					citeModel.removeRow(atRow);
					pack();
				} catch (HBException hbe) {
					System.out.println("HG0507SelectPerson delete citation error: " + hbe.getMessage());	//$NON-NLS-1$
					hbe.printStackTrace();
				}
			}
		});

		// Listener for move Citation up button
		btn_Up.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int selectedRow = tableCite.getSelectedRow();
				// only allow move up if not at top of table
				if (selectedRow >= 1) {
				// Switch rows in citeModel
					citeModel.moveRow(selectedRow, selectedRow, selectedRow-1);
				// Switch rows in underlying objNameCiteData
					objTempCiteData = objCiteData[selectedRow-1];
					objCiteData[selectedRow-1] = objCiteData[selectedRow];
					objCiteData[selectedRow] = objTempCiteData;
				//  Reset visible selected row
				    tableCite.setRowSelectionInterval(selectedRow-1, selectedRow-1);
				    btn_Save.setEnabled(true);
					citationOrderChanged = true;
				}
			}
		});

		// Listener for move Citation down button
		btn_Down.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int tableSize = tableCite.getRowCount();
				int selectedRow = tableCite.getSelectedRow();
				// only allow move down if not at end of table
				if (selectedRow < tableSize-1) {
				// Switch rows in tableModel
					citeModel.moveRow(selectedRow, selectedRow, selectedRow+1);
				// Switch rows in underlying objNameCiteData
					objTempCiteData = objCiteData[selectedRow];
					objCiteData[selectedRow] = objCiteData[selectedRow+1];
					objCiteData[selectedRow+1] = objTempCiteData;
				//  Reset visible selected row
					tableCite.setRowSelectionInterval(selectedRow+1, selectedRow+1);
					btn_Save.setEnabled(true);
					citationOrderChanged = true;
				}
			}
		});

	}	// End HG0507SelectPerson constructor

/**
 * resetCitationTable
 * @aram tableName
 * @throws HBException
 */
	public void resetCitationTable(String tableName) throws HBException {
		objCiteData = pointCitationSourceHandler.getCitationSourceData(personPID, tableName);
		Arrays.sort(objCiteData, (o1, o2) -> Integer.compare((Integer) o1[4], (Integer) o2[4]));
		citeModel.setDataVector(objCiteData, tableCiteHeader);
	}

	public void setEditMode() {
	// Hide the Find/Filter & Person panels
		findPanel.setVisible(false);
		personPanel.setVisible(false);
		control1Panel.setVisible(false);
		if (thisSelectPerson instanceof HG0507SelectParent)
			if (!addRelation) addTitle = HG05070Msgs.Text_142;		// Edit Parent
		if (thisSelectPerson instanceof HG0507SelectPartner)
			if (!addRelation) addTitle = HG05070Msgs.Text_143;		// Edit Partner
		if (thisSelectPerson instanceof HG0507SelectAssociate)
			if (!addRelation) addTitle = HG05070Msgs.Text_144;		// Edit Associate

	// Display the Relationship/memo/Citation panels instead
		persRolePanel.setVisible(true);
		memoPanel.setVisible(true);
	// but only show citation panel for Parents/Partners
		if (thisSelectPerson instanceof HG0507SelectParent) citePanel.setVisible(true);
		if (thisSelectPerson instanceof HG0507SelectPartner) citePanel.setVisible(true);
		control2Panel.setVisible(true);
		btn_SaveEvent.setEnabled(true);
		btn_SaveEvent.setVisible(true);
		btn_SaveEvent.setText(HG05070Msgs.Text_145);	// Edit partner/event
		btn_Save.setEnabled(true);
		btn_Save.setVisible(true);
		btn_Save.setText(HG05070Msgs.Text_146);		// Update Partner
	// Change the title from Select to Add
		setTitle(addTitle);
		pack();
	}

/**
 * Action setup and/or change of filter settings
 * @param selectString (table column name) and filterText
 */
	private void setTableFilter(String selectString, String filterText) {
		// Test filter for special characters and insert backslash (escape) if needed
		// Note that as backslash is also special, we need to double each use of it!
		filterText = filterText.replaceAll("\\?", "\\\\?");		//$NON-NLS-1$	//$NON-NLS-2$
		filterText = filterText.replaceAll("\\(", "\\\\(");		//$NON-NLS-1$	//$NON-NLS-2$
		filterText = filterText.replaceAll("\\)", "\\\\)");		//$NON-NLS-1$	//$NON-NLS-2$
		filterText = filterText.replaceAll("\\*", "\\\\*");		//$NON-NLS-1$	//$NON-NLS-2$
		// Setup sorter
		TableModel myModel = tablePersons.getModel();
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(myModel);
		try {
			if (selectString.trim().equals(allColumnsText2.trim())) {				// All Columns
				// For whole table
				sorter.setRowFilter(RowFilter.regexFilter("(?iu)" + filterText));	// case-insensitive unicode filter //$NON-NLS-1$
			} else for (int i = 0; i < tablePersColHeads.length; i++) {
					if (selectString.trim().equals(tablePersColHeads[i].trim())) {
						// For only one column
						sorter.setRowFilter(RowFilter.regexFilter("(?iu)" + filterText, i));	// case-insensitive unicode filter //$NON-NLS-1$
						break;
					}
			}
		} catch (PatternSyntaxException pse) {
			JOptionPane.showMessageDialog(chkbox_Filter, HG05070Msgs.Text_121 						// Cannot use
												+ filterText + HG05070Msgs.Text_122,				// as a filter
												HG05070Msgs.Text_123, JOptionPane.ERROR_MESSAGE);	// Filter Text Error
		}
	    tablePersons.setRowSorter(sorter);
	    // Set scroll bar to top of filter results
	    scrollTable.getViewport().setViewPosition(new Point(0,0));
	}	// End setTableFilter

}  // End of HG0507SelectPerson
