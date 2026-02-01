package hre.gui;
/**************************************************************************************
 * Person Select - Specification 05.07 GUI_EntitySelect 2019-09-16
 * v0.00.0012 2019-09-16 by D Ferguson
 * v0.00.0014 2019-11-18 changes for HB0711 static classes (D Ferguson)
 * v0.00.0015 2019-12-07 changed code for HG0514 call (D Ferguson)
 * v0.00.0021 2020-04-13 Added new version of HG0507EntitySelect (N. Tolleshaug)
 *            2020-04-14 Partial implement HG0507EntitySelect (N. Tolleshaug)
 *            2020-04-17 converted to MigLayout (D Ferguson)
 * v0.00.0022 2020-05-20 Error correction - modification (N. Tolleshaug)
 * 	          2020-05-21 Made tableData local to this class (N. Tolleshaug)
 *            2020-05-22 put project name in frame title (D Ferguson)
 * v0.00.0022 2020-05-24 Person selected transferred to HBProjectOpenData instance
 * 						   to corresponding project open data (N. Tolleshaug)
 * v0.00.0022 2020-07-07 increase column DoB/DoD from 100 to 120 (N. Tolleshaug)
 *            2020-07-19 change initial screen size (D Ferguson)
 *            2020-07-27 add screenID for Reminder, Help xref (D Ferguson)
 *            2020-07-31 changed to JFrame (D Ferguson)
 * v0.01.0023 2020-08-16 renamed to HG0507PersonSelect; added splitPane for Tree (D Ferguson)
 *            2020-08-17 removed Tools, View functions; added Search on Name (D Ferguson)
 *            2020-08-18 fixed error in sort sequence after filter removed (D Ferguson)
 *            2020-08-21 implementation of connection to HBTreeCreator (N. Tolleshaug)
 *            2020-08-22 Moved getOpenProjectByName to bila package
 *  					   and renamed to pointOpenProjectByName (N. Tolleshaug)
 *  		  2020-08-23 Person selected in picklist opens Tree at that person (D Ferguson)
 *  		  2020-08-23 Modified to fireTreeStructureChanged in Tree (N. Tolleshaug)
 *  		  2020-08-24 Set picklist and tree to open at initial focus-person
 *  					 	and implement Expand/Collapse All buttons (D Ferguson)
 *  		  2020-08-27 Partner information message popup (N. Tolleshaug)
 *  		  2020-08-28 Added listener for mouse double-click and right-click (D Ferguson)
 *  		  2020-08-29 List of partners message popup (N. Tolleshaug)
 *  		  2020-09-04 HG0540Reminder store implemented (N. Tolleshaug)
 *  		  2020-09-09 Close HG0540Reminder window implemented (N. Tolleshaug)
 *  		  2020-09-09 Read/Store frame position and size from T302 (N. Tolleshaug)
 *  		  2020-09-10 Code to bring PersonSelect to front in line 510 (N. Tolleshaug)
 *  		  2020-09-30 fonts removed for JTattoo install (D Ferguson)
 *  		  2020-10-03 PersonSelect extends HG0451SuperIntFrame (N. Tolleshaug)
 * v0.01.0025 2020-11-01 Minor modification to improve code (N. Tolleshaug)
 *   		  2020-11-03 Fix table name to match Super code (D Ferguson)
 *   		  2020-11-03 Corrected output table print error (N. Tolleshaug)
 *   		  2020-11-18 Implemented name display options DISP/SORT (N. Tolleshaug)
 *   		  2020-11-21 Name display and tree reset for new DISP/SORT (N. Tolleshaug)
 * 			  2020-11-22 Framework for processing data personView (N. Tolleshaug)
 * 			  2020-12-08 Person double-click opens Person Viewpoint for theme (D Ferguson)
 * 			  2021-01-20 Reset person ID and multiple PersonViewPoint (N. Tolleshaug)
 * 			  2021-01-22 Right click on tree name initiate pVP(N. Tolleshaug)
 * 			  2021-02-07 Implemented column control HG0581ConfigureTable (N. Tolleshaug)
 * 			  2021-03-07 Make found 'Search' row marked as Selected (D Ferguson)
 * v0.01.0026 2021-09-19 Add 'Find Next' button to repeat Search (D Ferguson)
 * v0.01.0027 2021-11-29 Run picklist build on own thread plus progress bar (D Ferguson)
 * 			  2021-12-02 Run HBTreeCreator on the same thread as picklist (N. Tolleshaug)
 * 			  2022-01-17 Set last selected person always on table top (N. Tolleshaug)
 * 			  2022-02-27 NLS converted (D Ferguson)
 * 			  2022-02-27 NLS updated for table headers (N. Tolleshaug)
 * 			  2022-03-07 Updated to work with HGloDataMsgs (D Ferguson)
 *            2022-03-12 Change right-click in table to show popupMenu (D Ferguson)
 *            2022-05-16 Corrected conflict with Find when selecting row (N. Tolleshaug)
 *            2022-05-30 Add Find Prev and Ignore Diacritics in Find (D Ferguson)
 * v0.01.0028 2022-12-12 Add button to switch Name Display choices (D Ferguson)
 * 			  2022-12-16 Store name style index in T302 (N. Tolleshaug)
 * 			  2022-12-19 NLS update by (D Ferguson).
 * 			  2023-01-08 Preliminary fix of partner #17 list problem in Sample UK (N. Tolleshaug)
 * 			  2023-01-15 Translated texts collected from T204 (N. Tolleshaug)
 * 			  2023-01-16 Corrected ID - noNB error filter from T204 (N. Tolleshaug)
 * 			  2023-01-29 Added catch block for PSE on filter (D Ferguson)
 * v0.03.0030 2023-07-01 Revision of Person Select table processing (N. Tolleshaug)
 * 			  2023-08-05 Enable update status bar - on/off (N. Tolleshaug)
 * v0.03.0031 2024-03-24 Removed right click error line 957 (N. Tolleshaug)
 * 			  2024-10-29 Modified public static final String screenID = "50700" (N. Tolleshaug)
 * 			  2024-12-02 Replace JoptionPane 'null' locations with 'contents' (D Ferguson)
 * v0.04.0032 2026-01-06 Log catch block and DEBUG msgs (D Ferguson)
 ***************************************************************************************
 * NOTES for incomplete functionality
 * NOTE02 No code for importing saved filters
 ***************************************************************************************/

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.regex.PatternSyntaxException;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.bila.HBTreeCreator;
import hre.bila.HBTreeCreator.GenealogyPerson;
import hre.bila.HBTreeCreator.GenealogyTree;
import hre.bila.HBViewPointHandler;
import hre.nls.HG05070Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Person Select
 * @author D Ferguson
 * @version v0.03.0031
 * @since 2019-09-16
 */

public class HG0507PersonSelect extends HG0451SuperIntFrame implements ActionListener {
	private static final long serialVersionUID = 001L;

	public static final String screenID = "50700"; //$NON-NLS-1$
	final static int maxPersonVPIs = 5;
	private JPanel contents;
	private String selectString = ""; //$NON-NLS-1$
	private JCheckBox chkbox_Filter;
	JComboBox<String> comboBox_Subset;

	private int focusPersIDX = 1;	// default start person
	private int foundRow;
	private int selectedRow;
	private long tablePersonPID;
	private boolean findActivated = false;
	private ActionListener comboViewChange = null;

    private static String SHOW_ANCESTOR_CMD = "showAncestor"; //$NON-NLS-1$
    private static String SHOW_PARTNER_CMD = "showPartner"; //$NON-NLS-1$
    private static String SHOW_DESCEND_CMD = "showDescendant";  //$NON-NLS-1$
    private static String EXPAND_ALL_CMD = "expandAll"; //$NON-NLS-1$
    private static String COLLAPSE_ALL_CMD = "collapseAll"; //$NON-NLS-1$
    private static String EXPAND_CMD = "expand"; //$NON-NLS-1$
    private static String COLLAPSE_CMD = "collapse"; //$NON-NLS-1$

    private String idText, allColumnsText1, allColumnsText2;

    public HBPersonHandler pointPersonHandler;
    private HBViewPointHandler pointViewPointHandler;
    HG0507PersonSelect personSelect = this;
	HBProjectOpenData pointOpenProject;
	JInternalFrame personFrame = this;
    JScrollPane scrollTable, scrollTree;

    HBTreeCreator pointTree;
	GenealogyTree tree;

	String[] tableColHeads = null;
	private Object [][] tableControlData;
	private Object[][] tableData;

	// If true, use display-type Name Styles; else use standard (translated) Texts
	boolean selectStyleNamesControl = true;
	String[] nameTexts = {HG05070Msgs.Text_11, HG05070Msgs.Text_10};	// Birth-name + Surname OR Surname, Birth-name
	String[] nameViews;

	private JTable table_Entity;
	DefaultTableModel myTableModel = null;

/**
 * Collect JTable for output
 * JTable getDataTable()
 */
	@Override
	public JTable getDataTable() {
		return table_Entity;
	}

/**
 * Create the dialog with access to the Open Project
 * @throws HBException
 */
	public HG0507PersonSelect(HBPersonHandler pointPersonHandler,
							  HBProjectOpenData pointOpenProject,
							  Object [][] tableControl,
							  String position) throws HBException {
		super(pointPersonHandler,"Person Select",true,true,true,true,tableControl);	//$NON-NLS-1$
		this.pointOpenProject = pointOpenProject;
		this.pointPersonHandler = pointPersonHandler;

		pointViewPointHandler = pointOpenProject.getViewPointHandler();

		// Setup references for HG0451
		windowID = screenID;
		helpName = "selectperson";		 //$NON-NLS-1$

		// Set focus person that will be on top in table
		focusPersIDX = pointOpenProject.getSelectedPersonIndex();

		// Test translated data from T204
		if (HGlobal.DEBUG && HGlobal.writeLogs)
			HB0711Logging.logWrite("Status: in HG0507PerSelect Translated texts: 0-" 					//$NON-NLS-1$
					+ Arrays.toString(pointPersonHandler.setTranslatedData(screenID, "0", false)));		//$NON-NLS-1$

		// Collect static gui texts from T204
		String[] translatedTexts = pointPersonHandler.setTranslatedData(screenID, "0", false);			//$NON-NLS-1$
		idText = " " + translatedTexts[0]; 				//$NON-NLS-1$
		allColumnsText1 = " " + translatedTexts[1];		//$NON-NLS-1$
		allColumnsText2 = translatedTexts[2];

		// Setup column headings and data
		tableControlData = pointOpenProject.pointGuiData.getTableControl(screenID);
		tableColHeads = setColumnHeaders(tableControlData);
		String projectName = pointOpenProject.getProjectName();

		// Setup close and logging actions
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs)
			HB0711Logging.logWrite("Action: entering HG0507PersonSelect");	//$NON-NLS-1$

		if (HGlobal.TIME)
			HGlobalCode.timeReport("start of HG0507PerSel on thread "  				//$NON-NLS-1$
									+ Thread.currentThread().getName());

		// Create and Show Progress Bar
		JFrame progFrame = new JFrame(HG05070Msgs.Text_16);
		progFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/hre/images/HRE-32.png"))); //$NON-NLS-1$
		progFrame.setSize(400,50);
		progFrame.setLocation(HGlobal.mainX + 350, HGlobal.mainY + 80);
		JProgressBar progBar = new JProgressBar(SwingConstants.HORIZONTAL, 0, 100);
		progBar.setValue(0);
		progBar.setStringPainted(true);
		progBar.setIndeterminate(false);
		progFrame.setAlwaysOnTop(true);
		progFrame.add(progBar);

		// Set pointer to progbar in Handler
		pointPersonHandler.setPointProgBar(progBar);
		if (HGlobal.TIME)
			HGlobalCode.timeReport("show HG0507PerSel progress bar on thread "  //$NON-NLS-1$
									+ Thread.currentThread().getName());
		progFrame.setVisible(true);

		// Setup panel
		setTitle(HG05070Msgs.Text_18 + projectName);
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[][][grow]", "[]10[]10[]10[grow]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// Define toolBar in NORTH dock area
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.add(Box.createHorizontalGlue());
		toolBar.add(btn_Configicon);
		toolBar.add(btn_Outputicon);
		toolBar.add(btn_Remindericon);
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north"); //$NON-NLS-1$

	// Define screen controls in CENTRAL area, located by cell notation (col, row, width, height)
	// Row 0
		JLabel lbl_Search = new JLabel(HG05070Msgs.Text_23);			// Find within Name:
		contents.add(lbl_Search, "cell 0 0, alignx right"); //$NON-NLS-1$

		JTextField searchField = new JTextField();
		searchField.setColumns(30);
		searchField.setToolTipText(HG05070Msgs.Text_25);
		contents.add(searchField, "cell 1 0"); //$NON-NLS-1$

		JButton searchPrevious = new JButton(HG05070Msgs.Text_19);			// Find Previous
		searchPrevious.setToolTipText(HG05070Msgs.Text_20);
		searchPrevious.setEnabled(false);
		contents.add(searchPrevious, "cell 1 0, gapx 20"); //$NON-NLS-1$

		JButton searchNext = new JButton(HG05070Msgs.Text_27);				// Find Next
		searchNext.setToolTipText(HG05070Msgs.Text_28);
		searchNext.setEnabled(false);
		contents.add(searchNext, "cell 1 0, gapx 10"); //$NON-NLS-1$

		JCheckBox chkbox_ignoreDiacriticFind = new JCheckBox(HG05070Msgs.Text_21);	// Ignore Diacritics
		chkbox_ignoreDiacriticFind.setHorizontalTextPosition(SwingConstants.LEADING);
		chkbox_ignoreDiacriticFind.setHorizontalAlignment(SwingConstants.LEFT);
		chkbox_ignoreDiacriticFind.setToolTipText(HG05070Msgs.Text_22);
		contents.add(chkbox_ignoreDiacriticFind, "cell 2 0"); //$NON-NLS-1$

	// Row 1
		JLabel lbl_TxtFilter = new JLabel(HG05070Msgs.Text_33);			// Text to filter for:
		contents.add(lbl_TxtFilter, "cell 0 1, alignx right"); //$NON-NLS-1$

		JTextField filterTextField = new JTextField();
		filterTextField.setColumns(15);
		filterTextField.setToolTipText(HG05070Msgs.Text_35);
		contents.add(filterTextField, "flowx,cell 1 1"); //$NON-NLS-1$

		chkbox_Filter = new JCheckBox(HG05070Msgs.Text_37);	// Filter
		chkbox_Filter.setHorizontalTextPosition(SwingConstants.LEADING);
		chkbox_Filter.setHorizontalAlignment(SwingConstants.LEFT);
		chkbox_Filter.setToolTipText(HG05070Msgs.Text_38);
		contents.add(chkbox_Filter, "cell 1 1,gapx 20"); //$NON-NLS-1$

		JLabel lbl_Subsets = new JLabel(HG05070Msgs.Text_40);			// Select:
		lbl_Subsets.setToolTipText(HG05070Msgs.Text_41);
		contents.add(lbl_Subsets, "cell 1 1, gapx 20"); //$NON-NLS-1$

		comboBox_Subset = new JComboBox<>();
		comboBox_Subset.setPreferredSize(new Dimension(170, 20));
		comboBox_Subset.setToolTipText(HG05070Msgs.Text_43);	// List of saved filter and subset names

		for (int i = 1; i < tableColHeads.length; i++) {
			comboBox_Subset.addItem(" " + tableColHeads[i]);		//$NON-NLS-1$
		}
		comboBox_Subset.addItem(" " + idText);					// ID 				//$NON-NLS-1$
		comboBox_Subset.addItem(" " + allColumnsText1);			// All Columns		//$NON-NLS-1$
		contents.add(comboBox_Subset, "cell 1 1"); //$NON-NLS-1$

	// Row 2
		JLabel lbl_View = new JLabel(HG05070Msgs.Text_47);		// Display Name As:
		contents.add(lbl_View, "cell 0 2, alignx right"); //$NON-NLS-1$

		JButton btn_DisplayType = new JButton();
		// Set name select types in Combobox; select control type and button text
		if (pointOpenProject.getSelectStyleNamesControl()) {
			selectStyleNamesControl = false;
			btn_DisplayType.setText(HG05070Msgs.Text_50);			// Use Simple Formats
			nameViews = pointPersonHandler.getDefaultOutNameStyles("N", pointOpenProject);	//$NON-NLS-1$
		} else {
			selectStyleNamesControl = true;
			btn_DisplayType.setText(HG05070Msgs.Text_51);				// Use Name Styles
			nameViews = nameTexts;
		}
		DefaultComboBoxModel<String> comboNameTypes = new DefaultComboBoxModel<>(nameViews);
		JComboBox<String> comboBox_View = new JComboBox<>(comboNameTypes);
		comboBox_View.setPreferredSize(new Dimension(300, 20));
		comboBox_View.setToolTipText(HG05070Msgs.Text_49);
		// If selected index > number of select items in JComboBox - reset name display index
		if (pointOpenProject.getNameDisplayIndex() >= nameViews.length) {
			pointOpenProject.setNameDisplayIndex(0);
		}
		comboBox_View.setSelectedIndex(pointOpenProject.getNameDisplayIndex());
		contents.add(comboBox_View, "cell 1 2");	 		//$NON-NLS-1$
		contents.add(btn_DisplayType, "cell 1 2, gapx 20");	//$NON-NLS-1$

		JButton btn_Swap = new JButton(HG05070Msgs.Text_30);			// << Swap Panels >>
		btn_Swap.setToolTipText(HG05070Msgs.Text_31);
		contents.add(btn_Swap, "cell 2 2"); //$NON-NLS-1$

	// Row 3- create 2 Panes to embed in a JSplitPane
		// Left scrollTable pane contains the Person Select picklist
		// Right treePane contains Ascendent/Descendant tree
		// Panels can be swapped by use of the 'Swap' button
		// 1. define left scrollTable for picklist table
		scrollTable = new JScrollPane();
		scrollTable.setPreferredSize(new Dimension(650, 400));

		// 2. define right treePane for tree info
		JPanel treePane = new JPanel();
		treePane.setLayout(new MigLayout("insets 2", "[grow]", "[25:]10[]10[]10[grow]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		JLabel treeTitle = new JLabel(HG05070Msgs.Text_54);
		treeTitle.setBackground(UIManager.getColor("TableHeader.background")); //$NON-NLS-1$
		treeTitle.setBorder(UIManager.getBorder("ScrollPane.border")); //$NON-NLS-1$
		treeTitle.setOpaque(false);
		treeTitle.setHorizontalAlignment(SwingConstants.CENTER);

		treePane.add(treeTitle, "cell 0 0, grow"); //$NON-NLS-1$

		JRadioButton showDescendant = new JRadioButton(HG05070Msgs.Text_58);
		JRadioButton showAncestor = new JRadioButton(HG05070Msgs.Text_59, true);
        showDescendant.addActionListener(this);
        showAncestor.addActionListener(this);
        showDescendant.setActionCommand(SHOW_DESCEND_CMD);
        showAncestor.setActionCommand(SHOW_ANCESTOR_CMD);
	    ButtonGroup bGroup = new ButtonGroup();
	    bGroup.add(showDescendant);
	    bGroup.add(showAncestor);
	    treePane.add(showDescendant, "cell 0 1, align center"); //$NON-NLS-1$
	    treePane.add(showAncestor, "cell 0 1, gapx 20"); //$NON-NLS-1$

	    JButton treePartner = new JButton(HG05070Msgs.Text_62);
	    treePartner.addActionListener(this);
	    treePartner.setActionCommand(SHOW_PARTNER_CMD);
		treePane.add(treePartner, "cell 0 1, gapx 20"); //$NON-NLS-1$

	    JButton expandAll = new JButton(HG05070Msgs.Text_64);
	    expandAll.addActionListener(this);
	    expandAll.setActionCommand(EXPAND_ALL_CMD);
		treePane.add(expandAll, "cell 0 2, align center"); //$NON-NLS-1$
	    JButton expandOne = new JButton(HG05070Msgs.Text_66);
	    expandOne.addActionListener(this);
	    expandOne.setActionCommand(EXPAND_CMD);
		treePane.add(expandOne, "cell 0 2, gapx 10");	 //$NON-NLS-1$
	    JButton collapseOne = new JButton(HG05070Msgs.Text_68);
	    collapseOne.addActionListener(this);
	    collapseOne.setActionCommand(COLLAPSE_CMD);
		treePane.add(collapseOne, "cell 0 2, gapx 10"); //$NON-NLS-1$
		JButton collapseAll = new JButton(HG05070Msgs.Text_70);
	    collapseAll.addActionListener(this);
	    collapseAll.setActionCommand(COLLAPSE_ALL_CMD);
		treePane.add(collapseAll, "cell 0 2, gapx 10"); //$NON-NLS-1$

		// Setup JTable to show data
		table_Entity = new JTable() {
			private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
			}};
		table_Entity.setMaximumSize(new Dimension(32767, 32767));
		table_Entity.setFillsViewportHeight(true);

	    // Start SwingWorker to build Person Picklist as a background task on worker thread
		SwingWorker<Void, Integer> buildPerList = new SwingWorker<>() {
	         @Override
	         protected Void doInBackground() throws Exception {
	        	 int errorCode;
	     		 if (HGlobal.TIME) {
					HGlobalCode.timeReport("start of HG0507PerSel HBTreeCreator on background thread "  //$NON-NLS-1$
							 + Thread.currentThread().getName());
				}

	     	// Set project Name Display Index
	     		 int nameDisplayIndex = pointOpenProject.getNameDisplayIndex();
	     		 pointPersonHandler.setNameDisplayIndex(nameDisplayIndex);
	     		if (HGlobal.DEBUG && HGlobal.writeLogs)
	    			HB0711Logging.logWrite("Status: in HG0507PerSelect Name display index = " + nameDisplayIndex);	//$NON-NLS-1$

	     	// Initiate partners for HBTreeCreator
	     		pointPersonHandler.enableUpdateMonitor(true);
	     		pointPersonHandler.processPartnerList(pointOpenProject);
	     		pointPersonHandler.initiatePersonData(tableColHeads.length, pointOpenProject);

			// Use HBTreeCreator object if pointer stored in HBOpenProjectData or need to reload tree
				 if (pointOpenProject.pointTree == null || pointOpenProject.getReloadPersonSelectData()) {
			// Create tree for focus person
					pointTree = new HBTreeCreator(pointOpenProject, focusPersIDX, nameDisplayIndex, personSelect);
					errorCode = pointTree.initateTree();
					pointOpenProject.pointTree = pointTree;
				 } else {
					 pointTree = pointOpenProject.pointTree;
					 errorCode = pointTree.initateTree();
				 }
				 if (errorCode > 0) {
					 userInfoTreeCreator(errorCode,HG05070Msgs.Text_73);
					 dispose();
				 }
	     		 if (HGlobal.TIME)
					HGlobalCode.timeReport("Start of HG0507PerSel tabledata on thread "  //$NON-NLS-1$
							 + Thread.currentThread().getName());
	    		 tableData = pointPersonHandler.createPersonTable(tableColHeads.length,
	    				 											tableControlData,
	    				 											pointOpenProject);
				 if (tableData == null ) {
					userInfoConvertData(1);
					progFrame.dispose();
					errorCloseAction();
				 }
	     		 if (HGlobal.TIME)
					HGlobalCode.timeReport("End of HG0507PerSel background on thread " 		//$NON-NLS-1$
									+ Thread.currentThread().getName());
		         return null;
	         }

		// Takes SwingWorker publish %s and updates Progress Bar
	         @Override
	         protected void process(List<Integer> chunks) {
	             progBar.setValue(chunks.get(chunks.size()-1));
	         }

	    // All code to be executed AFTER background thread is done
	         @Override
	         protected void done() {
	        	 if (HGlobal.TIME)
					HGlobalCode.timeReport("start of HG0507PerSel 'done' component on thread "  //$NON-NLS-1$
							 + Thread.currentThread().getName());
	        	 	publish(100);	// Set Progress bar to 100%
					tree = pointTree.getTree();
			        scrollTree = new JScrollPane(tree);
			        scrollTree.setPreferredSize(new Dimension(450, 400));
					treePane.add(scrollTree, "cell 0 3, grow"); //$NON-NLS-1$

		    // Build split-pane and add to contents panel - set divider control buttons at top
					UIManager.put("SplitPane.centerOneTouchButtons", false);		 //$NON-NLS-1$
					JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollTable, treePane);
					splitPane.setOneTouchExpandable(true);
					splitPane.setResizeWeight(1.0);		//Screen growth goes all to left panel
					splitPane.setDividerSize(10);
					contents.add(splitPane, "cell 0 3 4 1, grow"); //$NON-NLS-1$

		     // Setup table model and renderer
				// No table data ??
					if (tableData == null)
						return;

					myTableModel = new DefaultTableModel(
		        		tableData, tableColHeads) {
							private static final long serialVersionUID = 1L;
							@SuppressWarnings({ "unchecked", "rawtypes" })
							@Override
							public Class getColumnClass(int column) {
									return getValueAt(0, column).getClass();
						}
					};

		        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		        centerRenderer.setHorizontalAlignment( SwingConstants.CENTER );
		        table_Entity.setDefaultRenderer(Integer.class, centerRenderer);
		        table_Entity.setModel(myTableModel);
				table_Entity.getColumnModel().getColumn(0).setMinWidth(50);
				table_Entity.getColumnModel().getColumn(0).setPreferredWidth(50);
				table_Entity.getColumnModel().getColumn(1).setMinWidth(50);
				table_Entity.getColumnModel().getColumn(1).setPreferredWidth(250);

		        int col = 2;
		        for (int i = 2; i < tableControlData.length; i++) {
		        	if ((boolean)tableControlData[i][1]) {
		        		// Select col within table
		        		if ( i == 2 || i == 4) {
		        			table_Entity.getColumnModel().getColumn(col).setMinWidth(90);
		        			table_Entity.getColumnModel().getColumn(col).setPreferredWidth(120);
		        		} else {
		        			table_Entity.getColumnModel().getColumn(col).setMinWidth(50);
		        			table_Entity.getColumnModel().getColumn(col).setPreferredWidth(250);
		        		}
		        		col++;
		        	}
		        }

				table_Entity.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

				// Set the ability to sort on columns
				table_Entity.setAutoCreateRowSorter(true);
			    TableModel myModel = table_Entity.getModel();
			    TableRowSorter<TableModel> sorter = new TableRowSorter<>(myModel);
				List <RowSorter.SortKey> psortKeys1 = new ArrayList<>();

				// Presort on column 1
				psortKeys1.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
				sorter.setSortKeys(psortKeys1);
			    table_Entity.setRowSorter(sorter);

			    // Set tooltips and header format
				table_Entity.getTableHeader().setToolTipText(HG05070Msgs.Text_80);
				JTableHeader pHeader = table_Entity.getTableHeader();
				pHeader.setOpaque(false);
				TableCellRenderer prendererFromHeader = table_Entity.getTableHeader().getDefaultRenderer();
				JLabel pheaderLabel = (JLabel) prendererFromHeader;
				pheaderLabel.setHorizontalAlignment(SwingConstants.CENTER);

				// Set row selection action
				ListSelectionModel pcellSelectionModel = table_Entity.getSelectionModel();
				pcellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

				// Show the table
				scrollTable.setViewportView(table_Entity);

				// Now set the sorted table view to start at the required focus person
				table_Entity.setRowSelectionAllowed(true);
				for(int i = 0; i < table_Entity.getRowCount(); i++){
					if(table_Entity.getValueAt(i, 0).equals(focusPersIDX)){
				        	table_Entity.changeSelection(i, 0, true, true);
				        	break;
				        }
				}
				table_Entity.requestFocus();

				// Do update of T302
				pointPersonHandler.finalActionT302(pointOpenProject);

				// Set the frame visible
				personFrame.pack();
				HG0401HREMain.mainPane.add(personFrame);
				personFrame.setVisible(true);
				// Set frame position relative to other frames
				if (position == "F") personFrame.toFront();	//$NON-NLS-1$
				else personFrame.toBack();

				// Remove the Progress Bar panel
			    progFrame.dispose();
			    if (HGlobal.TIME)
					HGlobalCode.timeReport("Completed HG0507PerSel on thread " 		//$NON-NLS-1$
							+ Thread.currentThread().getName());

			/*****************************
			 * CREATE All ACTION LISTENERS
			 *****************************/
				// Listener for clicking 'X' on screen
			     personFrame.addInternalFrameListener(new InternalFrameAdapter() {
			    	 @Override
					public void internalFrameClosing(InternalFrameEvent e)  {
						if (HGlobal.writeLogs)
							HB0711Logging.logWrite("Action: exiting HG00507PersonSelect"); //$NON-NLS-1$

					// close reminder display
						if (reminderDisplay != null) reminderDisplay.dispose();

				    // Set frame size in GUI data
						Dimension frameSize = getSize();
						pointOpenProject.setSizeScreen(screenID,frameSize);

					// Set Table Control
						pointOpenProject.setTableControl(screenID, tableControl);

					// Set position	in GUI data
						Point position = getLocation();
						pointOpenProject.setPositionScreen(screenID,position);

					// Set class name in GUI configuration data
						pointOpenProject.setClassName(screenID,"HG0507PersonSelect"); //$NON-NLS-1$

					// Store name style setting in T302
						pointOpenProject.storeNameStyleIndex(screenID);

					// Mark the screen as closed in T302
						pointOpenProject.closeStatusScreen(screenID);
						dispose();
					} // return to main menu
				});

				// Find within Name field listener
			    searchField.addActionListener(new ActionListener() {
			        @Override
					public void actionPerformed(ActionEvent e) {
			        // First, find how many rows are visible in current scrollpane's size, and half it
			        	JViewport viewport = scrollTable.getViewport();
			            Dimension extentSize = viewport.getExtentSize();
			            int halfVisibleRows = (extentSize.height/table_Entity.getRowHeight())/2;
				        Rectangle viewRect = viewport.getViewRect();
			        // Now find the first table row where beginning of Name matches search value
			            String searchValue = searchField.getText();
			         // If Ignore Diacritics checked, remove diacritics from searchValue
			            if (chkbox_ignoreDiacriticFind.isSelected())
							searchValue = Normalizer.normalize(searchValue, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", ""); //$NON-NLS-1$ //$NON-NLS-2$
			            findActivated = true;
			            for (int row = 0; row <= table_Entity.getRowCount() - 1; row++) {
			            		String tableValue = (String) table_Entity.getValueAt(row, 1);
			            		// If Ignore Diacritics checked, remove diacritics from tableValue
					            if (chkbox_ignoreDiacriticFind.isSelected())
									tableValue = Normalizer.normalize(tableValue, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", ""); //$NON-NLS-1$ //$NON-NLS-2$

		   	                    if (tableValue.toLowerCase().contains(searchValue.toLowerCase())) {
		   	                    // set the found row to the middle of the scrollpane; adjusted for scroll-up or down
		   	                    	int first = table_Entity.rowAtPoint(new Point(0, viewRect.y));
		   	                    	if (first > row) {
										halfVisibleRows = -halfVisibleRows;
									}
		   	                    	table_Entity.scrollRectToVisible(table_Entity.getCellRect(row + halfVisibleRows, 1, true));
		   	                    // set the 'found' row as selected, clear any other selection, save it
		   	                    	table_Entity.changeSelection(row, 0, false, false);
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
			            int halfVisibleRows = (extentSize.height/table_Entity.getRowHeight())/2;
				        Rectangle viewRect = viewport.getViewRect();
			        // Now find first table row after last found row which matches search value
			            String searchValue = searchField.getText();
			        // If Ignore Diacritics checked, remove diacritics from searchValue
			            if (chkbox_ignoreDiacriticFind.isSelected())
							searchValue = Normalizer.normalize(searchValue, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", ""); //$NON-NLS-1$ //$NON-NLS-2$
			            findActivated = true;
			            foundRow = foundRow + 1;
			            for (int row = foundRow; row <= table_Entity.getRowCount() - 1; row++) {
			            		String tableValue = (String) table_Entity.getValueAt(row, 1);
			            		// If Ignore Diacritics checked, remove diacritics from tableValue
					            if (chkbox_ignoreDiacriticFind.isSelected())
									tableValue = Normalizer.normalize(tableValue, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", ""); //$NON-NLS-1$ //$NON-NLS-2$
		   	                    if (tableValue.toLowerCase().contains(searchValue.toLowerCase())) {
		   	                    // set the found row to the middle of the scrollpane; adjusted for scroll-up or down
		   	                    	int first = table_Entity.rowAtPoint(new Point(0, viewRect.y));
		   	                    	if (first > row)
										halfVisibleRows = -halfVisibleRows;
		   	                    	table_Entity.scrollRectToVisible(table_Entity.getCellRect(row + halfVisibleRows, 1, true));
		   	                    // set the 'found' row as selected, clear any other selection, save it
		   	                    	table_Entity.changeSelection(row, 0, false, false);
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
			            int halfVisibleRows = (extentSize.height/table_Entity.getRowHeight())/2;
				        Rectangle viewRect = viewport.getViewRect();
			        // Now find first table row after last found row which matches search value
			            String searchValue = searchField.getText();
			        // If Ignore Diacritics checked, remove diacritics from searchValue
			            if (chkbox_ignoreDiacriticFind.isSelected())
							searchValue = Normalizer.normalize(searchValue, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", ""); //$NON-NLS-1$ //$NON-NLS-2$
			            findActivated = true;
			            foundRow = foundRow - 2;
			            for (int row = foundRow; row >= 0; row--) {
			            		String tableValue = (String) table_Entity.getValueAt(row, 1);
			            		// If Ignore Diacritics checked, remove diacritics from tableValue
					            if (chkbox_ignoreDiacriticFind.isSelected())
									tableValue = Normalizer.normalize(tableValue, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", ""); //$NON-NLS-1$ //$NON-NLS-2$

		   	                    if (tableValue.toLowerCase().contains(searchValue.toLowerCase())) {
		   	                    // set the found row to the middle of the scrollpane; adjusted for scroll-up or down
		   	                    	int first = table_Entity.rowAtPoint(new Point(0, viewRect.y));
		   	                    	if (first > row)
										halfVisibleRows = -halfVisibleRows;
		   	                    	table_Entity.scrollRectToVisible(table_Entity.getCellRect(row + halfVisibleRows, 1, true));
		   	                    // set the 'found' row as selected, clear any other selection, save it
		   	                    	table_Entity.changeSelection(row, 0, false, false);
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
								setTableFilter(allColumnsText2, ""); //$NON-NLS-1$  // All Columns allColumnsText2
								comboBox_Subset.setSelectedIndex(0);
								table_Entity.setRowSorter(sorter);
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

				// On selection of a Name View, perform appropriate action
				comboViewChange= new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent event) {
						int selectIndex = comboBox_View.getSelectedIndex();

					// Set index in Business Layer
						pointPersonHandler.setNameDisplayIndex(selectIndex);

					// Set selected person style index in open project
						pointOpenProject.setNameDisplayIndex(selectIndex);
						pointOpenProject.storeNameStyleIndex(screenID);

					// Force regenerate of table and tree with new name style
						pointOpenProject.pointTree = null;
						pointOpenProject.personSelectData = null;

					// Initiate new PersonSelect window in Front
						pointPersonHandler.initiatePersonSelect(pointOpenProject, "F");	//$NON-NLS-1$
						dispose();
					}
				};
				comboBox_View.addActionListener(comboViewChange);

				// Listener for Name Display Type button
				btn_DisplayType.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
					// Disable the combobox listener while we change its content
						comboBox_View.removeActionListener(comboViewChange);
					// If Name Styles not already selected, then this button click must be to turn them on,
					// so load combo-box with display-type Name Styles and vice-versa
						if (selectStyleNamesControl) {
							try {
								pointOpenProject.setSelectStyleNamesControl(true);
								nameViews = pointPersonHandler.getDefaultOutNameStyles("N", pointOpenProject);	//$NON-NLS-1$
								DefaultComboBoxModel<String> comboNameTypes = new DefaultComboBoxModel<>(nameViews);
								comboBox_View.setModel(comboNameTypes);
					// If selected index > number of select items in JComboBox - reset name display index
								if (pointOpenProject.getNameDisplayIndex() >= nameViews.length) {
									pointOpenProject.setNameDisplayIndex(0);
								}
								comboBox_View.setSelectedIndex(pointOpenProject.getNameDisplayIndex());
					// reset for next button click
								btn_DisplayType.setText(HG05070Msgs.Text_50);		// Use Simple Formats
								selectStyleNamesControl = false;
							} catch (HBException hbe) {
								if (HGlobal.writeLogs) {
									HB0711Logging.logWrite("ERROR: in HG0507PerSelect load name styles: " + hbe.getMessage()); //$NON-NLS-1$
									HB0711Logging.printStackTraceToFile(hbe);
								}
							}
					// else use the translated terms for Birth-name + Surname; Surname, Birth-name and reset button
						} else {
							pointOpenProject.setSelectStyleNamesControl(false);
							nameViews = nameTexts;
							DefaultComboBoxModel<String> comboNameTypes = new DefaultComboBoxModel<>(nameViews);
							comboBox_View.setModel(comboNameTypes);
					// Set selected #1 of name display index > 1
							if (pointOpenProject.getNameDisplayIndex() > 1) {
								comboBox_View.setSelectedIndex(1);
							} else
								comboBox_View.setSelectedIndex(pointOpenProject.getNameDisplayIndex());

					// reset for next button click
							btn_DisplayType.setText(HG05070Msgs.Text_51);		// Use Name Styles
							selectStyleNamesControl = true;
						}
					// Store new setting in T302
						pointOpenProject.storeNameStyleIndex(screenID);
						// Re-enable the combobox listener
						comboBox_View.addActionListener(comboViewChange);
					 }
				  });

				// Listener to action selection of row in Person table
				pcellSelectionModel.addListSelectionListener(new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent event) {
						long personTablePID;
						int personVPindex = 0;
							if (!event.getValueIsAdjusting()) {
								int viewRow = table_Entity.getSelectedRow();
								int selectedRowInTable = table_Entity.convertRowIndexToModel(viewRow);
						 // Return if filter location triggers select in table
								if (selectedRowInTable == -1)
									return;

								personTablePID = pointPersonHandler.getPersonTablePID(selectedRowInTable);
								try {
									if (!findActivated) {
								// Find next open eventVP
										personVPindex = pointViewPointHandler.findClosedVP("5300", pointOpenProject);  //$NON-NLS-1$
										String personVPident = pointViewPointHandler.getPersonScreenID(personVPindex);
										if (HGlobal.DEBUG && HGlobal.writeLogs)
											HB0711Logging.logWrite("Status: in HG0507PerSelect row: " + selectedRowInTable //$NON-NLS-1$
													+ " personVPindex: " + personVPindex	//$NON-NLS-1$
													+ " personVPident: " + personVPident	//$NON-NLS-1$
													+ " PersonPID: " + personTablePID);		//$NON-NLS-1$
								// Set PID for event from event list
										if (personVPindex >= 0) {
											pointOpenProject.pointGuiData.setTableViewPointPID(personVPident, personTablePID);
										} else {
											userInfoInitVP(1);
											if (HGlobal.DEBUG && HGlobal.writeLogs)
												HB0711Logging.logWrite("Status: in HG0507PerSelect valueChanged - personVPindex: "	//$NON-NLS-1$
														+ personVPindex);
										}
									} else
										findActivated = false;

								// Set visible IDX for Person Select
									pointOpenProject.pointGuiData.setVisibleIDX(screenID, selectedRowInTable + 1);
									if (selectedRowInTable < 0)
										return;			// exit if listener call was caused by emptying table_User

									int indexPerson = (Integer) myTableModel.getValueAt(selectedRowInTable, 0);
									String selectedPersonPID = (String) myTableModel.getValueAt(selectedRowInTable, 1);
									pointPersonHandler.setSelectedPerson(projectName, selectedRowInTable, indexPerson, selectedPersonPID);
								// Reset Tree in scrollTree pane to the selected person
									showAncestor.setSelected(true);
									pointTree.setFocusPerson(indexPerson);
								} catch (HBException hbe) {
									if (HGlobal.writeLogs) {
										HB0711Logging.logWrite("ERROR: in HG0507PerSelect selecting person: " + hbe.getMessage()); //$NON-NLS-1$
										HB0711Logging.printStackTraceToFile(hbe);
									}
									JOptionPane.showMessageDialog(contents, HG05070Msgs.Text_93 + hbe.getMessage(),
											HG05070Msgs.Text_94,JOptionPane.INFORMATION_MESSAGE);
								}
								scrollTree.setViewportView(tree);
							}
					}
				});

				// Define ActionListeners for each entry of the following popupMenu
				// For popupMenu item popMenu1 - show Person VP
			    ActionListener popAL1 = new ActionListener() {
			        @Override
					public void actionPerformed(ActionEvent e) {
			            int errorCode = 0;
				    // Display Person Viewpoint for the selected Location
				    // The right-clicked row is passed here in selectedRow
	            		int rowInTable = table_Entity.convertRowIndexToModel(selectedRow);
						tablePersonPID = pointPersonHandler.getPersonTablePID(rowInTable);
					// Open Viewpoint for selected person
						errorCode = pointViewPointHandler.initiatePersonVP(pointOpenProject, tablePersonPID);
						if (errorCode > 0)
							userInfoInitVP(errorCode);
			        }
			      };
				// For popupMenu item popMenu2 - show Manage Person screen
			    ActionListener popAL2 = new ActionListener() {
			        @Override
					public void actionPerformed(ActionEvent e) {
				    // Show Manage Person screen
				    // The right-clicked row is passed here in selectedRow
	            		int rowInTable = table_Entity.convertRowIndexToModel(selectedRow);
						tablePersonPID = pointPersonHandler.getPersonTablePID(rowInTable);
						pointOpenProject.pointGuiData.setTableViewPointPID("50600", tablePersonPID); //$NON-NLS-1$
						int errorCode = pointPersonHandler.initiateManagePerson(pointOpenProject, tablePersonPID, "50600" ); //$NON-NLS-1$
						if (errorCode > 1)
							userInfoInitVP(errorCode);
			        }
			      };
				// Define a right-click popup menu to use below
			    JPopupMenu popupMenu = new JPopupMenu();
			    JMenuItem popMenu1 = new JMenuItem(HG05070Msgs.Text_91);	// Show person Viewpoint
			    popMenu1.addActionListener(popAL1);
			    popupMenu.add(popMenu1);
			    JMenuItem popMenu2 = new JMenuItem(HG05070Msgs.Text_92);	// Show Manage Person screen
			    popMenu2.addActionListener(popAL2);
			    popupMenu.add(popMenu2);

				// Listener for pick list mouse right-click and double click
				table_Entity.addMouseListener(new MouseAdapter() {
		            int errorCode = 0;
					@Override
		            public void mousePressed(MouseEvent me) {
						// if double-click
		            	if (me.getClickCount() == 2 && table_Entity.getSelectedRow() != -1) {
		            		selectedRow = table_Entity.getSelectedRow();
		            		int rowInTable = table_Entity.convertRowIndexToModel(selectedRow);
							tablePersonPID = pointPersonHandler.getPersonTablePID(rowInTable);
						// Open Viewpoint for selected person
							errorCode = pointViewPointHandler.initiatePersonVP(pointOpenProject, tablePersonPID);
							if (errorCode > 0)
								userInfoInitVP(errorCode);
		                }
		                // if right-click
		                if (me.getButton() == MouseEvent.BUTTON3) {
		                	selectedRow = table_Entity.rowAtPoint(me.getPoint());
		                // Avoid right click outside table
		                	if (selectedRow < 0)
								return;

		                	table_Entity.addRowSelectionInterval(selectedRow, selectedRow);
		                	int rowInTable = table_Entity.convertRowIndexToModel(selectedRow);
		                // exit if listener call was caused by emptying table_Entity
		                	if (rowInTable < 0)
								return;

						// Show popup menu of all possible actions
		                	popupMenu.show(me.getComponent(), me.getX(), me.getY());
		                }
		            }
		        });

				// Listener to action Swap button to exchange splitPane's contents
				btn_Swap.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
					// Get initial state of things
						int location = splitPane.getDividerLocation();
						Component r = splitPane.getRightComponent();
					    Component l = splitPane.getLeftComponent();
					// Remove the current components
					    splitPane.setLeftComponent(null);
					    splitPane.setRightComponent(null);
					// Add them back in, swapped
					    splitPane.setLeftComponent(r);
					    splitPane.setRightComponent(l);
					// Reset the divider
					    splitPane.setDividerLocation(splitPane.getWidth() - location - splitPane.getDividerSize());
					}
				});

	        }	// End SwingWorker done component
	    };   // End of SwingWorker method

		// Execute the background thread to build the Person Picklist and (when
		// finished) build the rest of the GUI
	    buildPerList.execute();

	}	// End HG0507PersonSelect constructor

/**
 * errorCloseAction()
 */
	private void errorCloseAction() {
	// close reminder display
		if (reminderDisplay != null)
			reminderDisplay.dispose();
    // Set frame size	in GUI data
		Dimension frameSize = getSize();
		pointOpenProject.setSizeScreen(screenID,frameSize);
	// Set Table Control
		//pointOpenProject.setTableControl(screenID, tableControl);
	// Set position	in GUI data
		Point position = getLocation();
		pointOpenProject.setPositionScreen(screenID,position);
	// Set class name in GUI configuration data
		pointOpenProject.setClassName(screenID,"HG0507PersonSelect"); //$NON-NLS-1$
	// Mark the screen as closed in T302
		pointOpenProject.closeStatusScreen(screenID);
		dispose();
	}	// End errorCloseAction

/**
 * Set up column headings
 * @param tableControl
 * @return
 */
	private String[] setColumnHeaders(Object[][] tableControl) {
		int rows = 2;
		for (int i = 2; i < 6; i++) {
			if (((boolean)tableControl[i][1])) {
				rows++;
			}
		}
		String [] tableHeads = new String[rows];
		tableHeads[0] = (String) tableControl[0][0];
		tableHeads[1] = (String) tableControl[1][0];
		int index = 2;
		for (int i = 2; i < 6; i++) {
			if ((boolean) tableControl[i][1]) {
				tableHeads[index] = (String) tableControl[i][0];
				index++;
			}
		}
		return tableHeads;
	}	// End setColumnHeaders

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
		TableModel myModel = table_Entity.getModel();
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(myModel);
		try {
			if (selectString.trim().equals(allColumnsText2.trim())) {				// All Columns
				// For whole table
				sorter.setRowFilter(RowFilter.regexFilter("(?iu)" + filterText));	// case-insensitive unicode filter //$NON-NLS-1$
			} else { // All Columns
				for (int i = 0; i < tableColHeads.length; i++) {
									if (selectString.trim().equals(tableColHeads[i].trim())) {
										// For only one column
										sorter.setRowFilter(RowFilter.regexFilter("(?iu)" + filterText, i));	// case-insensitive unicode filter //$NON-NLS-1$
										break;
									}
							}
			}
		} catch (PatternSyntaxException pse) {
			JOptionPane.showMessageDialog(chkbox_Filter, HG05070Msgs.Text_121 						// Cannot use
												+ filterText + HG05070Msgs.Text_122,				// as a filter
												HG05070Msgs.Text_123, JOptionPane.ERROR_MESSAGE);	// Filter Text Error
		}
	    table_Entity.setRowSorter(sorter);
	    // Set scroll bar to top of filter results
	    scrollTable.getViewport().setViewPosition(new Point(0,0));
	}	// End setTableFilter

/**
 * Listeners for all Tree Display buttons
 */
	@Override
	public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand() == SHOW_ANCESTOR_CMD)
        	pointTree.setFamView(true);
        if (ae.getActionCommand() == SHOW_DESCEND_CMD)
        	pointTree.setFamView(false);
        if (ae.getActionCommand() == SHOW_PARTNER_CMD)
        	displayPartner();
        if (ae.getActionCommand() == EXPAND_ALL_CMD)
        	pointTree.expandAll();
        if (ae.getActionCommand() == COLLAPSE_ALL_CMD)
        	pointTree.collapseAll();
        if (ae.getActionCommand() == EXPAND_CMD)
        	pointTree.expandOne();
        if (ae.getActionCommand() == COLLAPSE_CMD)
        	pointTree.collapseOne();
	}	// End actionPerformed

/**
 * 	Show info message with partner data
 */
	private void displayPartner() {
		String names = HG05070Msgs.Text_98;
		Vector<hre.bila.HBTreeCreator.GenealogyPerson> partners = pointTree.findPartners();
		if (partners != null) {
			for (GenealogyPerson partner : partners)
			 {
				if (partner != null)
					names = names + partner.getName() + "\n"; //$NON-NLS-1$
				else
					names = names + " No partner name \n";   //$NON-NLS-1$
			}
			JOptionPane.showMessageDialog(personFrame, " " + names, HG05070Msgs.Text_101, //$NON-NLS-1$
						 JOptionPane.PLAIN_MESSAGE);
		}
	}	// End displayPartner

/**
 * GUI user messages
 * @param errorCode
 * @param message
 */
	public void userInfoTreeCreator(int errorCode, String message) {
		if (errorCode == 1)
			JOptionPane.showMessageDialog(contents, HG05070Msgs.Text_105 + HG05070Msgs.Text_106,
										HG05070Msgs.Text_107, JOptionPane.ERROR_MESSAGE);
	// Show partner list error message
		if (errorCode == 2) {
			String errorText = HG05070Msgs.Text_102;	// no error
			if (message.equals("ERR1"))		//$NON-NLS-1$
				errorText = HG05070Msgs.Text_103;	// No partner recorded
			if (message.equals("ERR2"))		//$NON-NLS-1$
				errorText = HG05070Msgs.Text_104;	// Select a person in the tree first
			JOptionPane.showMessageDialog(tree, errorText, HG05070Msgs.Text_108, JOptionPane.INFORMATION_MESSAGE);  // Partner/Spouse:
		}
	}	//End userInfoTreeCreator

	private void userInfoConvertData(int errorCode) {
		if (errorCode == 1)
			JOptionPane.showMessageDialog(contents, HG05070Msgs.Text_109 + HG05070Msgs.Text_110,
										HG05070Msgs.Text_111, JOptionPane.ERROR_MESSAGE);
	}	// End userInfoConvertData

	private void userInfoInitVP(int errorCode) {
		if (errorCode == 1) {
			JOptionPane.showMessageDialog(contents, HG05070Msgs.Text_112 + maxPersonVPIs,
										HG05070Msgs.Text_113, JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (errorCode == 2) {
			JOptionPane.showMessageDialog(contents, HG05070Msgs.Text_114,
										HG05070Msgs.Text_115, JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (errorCode == 3) {
			JOptionPane.showMessageDialog(contents, HG05070Msgs.Text_116
												+ HG05070Msgs.Text_117
												+ HG05070Msgs.Text_118
												+ HG05070Msgs.Text_119,
										HG05070Msgs.Text_120, JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (HGlobal.DEBUG && HGlobal.writeLogs)
			HB0711Logging.logWrite("ERROR: in HG0507PerSelect unidentified errorcode: " + errorCode);	//$NON-NLS-1$
	}	// End userInfoInitVP

}  // End of HG0507PersonSelect
