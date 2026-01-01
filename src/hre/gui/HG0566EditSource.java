package hre.gui;
/**************************************************************************************
 * HG0566EditSource extends HG0450SuperDialog
 * ***********************************************************************************
 * v0.04.0032 2025-02-07 Original draft (D Ferguson)
 *			  2025-02-10 Change textAreas to textPanes for Preview buttons (D Ferguson)
 * 			  2025-06-29 Correctly handle Reminder screen display/remove (D Ferguson)
 *			  2025-08-16 Get table header from T204 (D Ferguson)
 *			  2025-10-01 Load all Source data and populate General tab (D Ferguson)
 *			  2025-10-02 Handle user change of Source Type (D Ferguson)
 *			  2025-10-04 Revise sizes of text areas to handle large fonts (D Ferguson)
 *			  2025-10-05 Load Source of Source and Repository data tables (D Ferguson)
 *			  2025-10-07 Populate element table for Add Source (N Tolleshaug)
 *			  2025-10-12 Partly updated for Add/Update source  (N. Tolleshaug)
 *			  2025-10-16 Updated activation of save button (N. Tolleshaug)
 *			  2025-10-17 Activate edit by Single click and last edit update (N. Tolleshaug)
 *			  2025-10-24 Update add/delete citation for source (N. Tolleshaug)
*			  2025-10-25 Do not load 'special' Elements to Element/value table (D Ferguson)
*			  2025-10-29 Rename Referenced text to 'Source Reference text' (D Ferguson)
*			  2025-11-04 Implement source template Preview buttons (D Ferguson)
*			  2025-11-07 Implemented reset repository table (N. Tolleshaug)
*			  2025-11-10 Activate HG0571RepoLink with double click on repo.
*			  2025-11-11 Pass repo data (incl. link data)  to HG0571 (D Ferguson)
*			  2025-11-16 Updated call for HG0571RepoLink (N. Tolleshaug)
 * 			  2025-11-17 Change HGlobalCode routines to be in ReportHandler (D Ferguson)
 *			  2025-11-28 Implemented author, editor and compiler name edit (N. Tolleshaug)
 *			  2025-11-30 Implemented option for select or delete editorial names (N. Tolleshaug)
 *			  2025-12-03 Implemented copy and create new source copy (N Tolleshaug)
 *			  2025-12-05 Make author/editor/compiler look editable (D Ferguson)
 *			  2025-12-06 Added validateBeforeSave routine for all HG0566 code to use (D Ferguson)
 *			  2025-12-08 Fix incorrect template changed flag settings (D Ferguson)
 *			  2025-12-09 Add 'over-ridden' indicator if a local template in use (D Ferguson)
 *			  2025-12-09 Initial values sourceAuthorPID = null_RPID, sourceEditorPID = null_RPID,
 *            			   sourceCompilerPID = null_RPID (N Tolleshaug)
 *            2025-12-11 Ensure only updated templates saved in T736 (D Ferguson)
 *			  2025-12-13 Reinstate getting RepoColHeads from T204 (D Ferguson)
 *			  2025-12-20 NLS all code to this point (D Ferguson)
 *			  2025-12-22 Updated foot note template error handling (N. Tolleshaug)
 *			  2025-12-29 NLS update (D Ferguson)
 *
 ************************************************************************************/

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.DefaultCaret;

import hre.bila.HB0711Logging;
import hre.bila.HBCitationSourceHandler;
import hre.bila.HBException;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.bila.HBReportHandler;
import hre.bila.HBRepositoryHandler;
import hre.gui.HGlobalCode.JTableCellTabbing;
import hre.nls.HG0566Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Edit Source
 * @author D Ferguson
 * @version v0.04.0032
 * @since 2025-02-07
 */

public class HG0566EditSource extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;
	HBPersonHandler pointPersonHandler;
	HBCitationSourceHandler pointCitationSourceHandler;
	HBRepositoryHandler pointRepositoryHandler;
	HBReportHandler pointReportHandler;
	HG0507SelectPerson pointSelectPerson;

	long null_RPID  = 1999999999999999L;
	long proOffset  = 1000000000000000L;

	public static final String screenID = "56600"; //$NON-NLS-1$

	private JPanel contents;

	HG0566EditSource pointEditSource = this;
	String citeOwnerType = "T736"; // Cited owner type to identify source table name  //$NON-NLS-1$
	String footTextType = "";	//$NON-NLS-1$
	Font font;
	JLabel fullFootOverride, shortFootOverride, biblioOverride;
	JButton btn_Save, btn_Close, btn_repoDel, btn_repoAdd, btn_srcDel, btn_srcAdd, btn_biblioPreview,
			btn_shortPreview, btn_fullPreview;
	DefaultTableCellRenderer centerLabelRenderer;
	JComboBox <String> comboSourceTypes, comboFidelity;
	JTable tableRepo, tableSrcSrc, tableSrcElmntValues;
	JTextField abbrevText, authorName, editorName, compilerName;
	JTextArea titleText;
    JCheckBox activChkBox;

	// For Source Element handling
	String[] tableSrcElmntValueHeads = null;
	String[][] tableSrcElmntValueData;
	DefaultTableModel srcElmntValueModel = null;

	String[][] tableSrcElmntData;
    Map<String, String> codeToTextMap;
    Map<String, String> textToCodeMap;
	String[][] tableSourceElmntDataValues;
	List<String> uniqueElementNums, uniqueElementNames, uniqueElementValues;

	// For Source of Source handling
	String[] tableSrcSrcColHeads = null;
	Object[][] tableSrcSrcData = null;
	DefaultTableModel srcSrcTableModel = null;
	Object[][] citnSrcSrcData = null;
	Object objCiteDataToEdit[] = new Object[2]; // To hold data to pass to Citation editor

	// For Repository handling
	String[] tableRepoColHeads = null;
	Object[][] tableRepoData;
	DefaultTableModel repoTableModel = null;
	Object[][] repoLinkData = null;
	Object[] repoEditData;

	JTextArea remindText, memoText;
	JTextArea fullFootText, shortFootText, biblioText;

	private String[] fidelityOptions = {HG0566Msgs.Text_0,			// Original
										HG0566Msgs.Text_1,		// Photocopy
										HG0566Msgs.Text_2,		// Transcript
										HG0566Msgs.Text_3,			// Extract
										HG0566Msgs.Text_4 };	// Other/Unknown

	private String[] fidelityValues = {"A","B","C","D","E"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

	String authorsName = "", editorsName = "", compilersName = ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	// For source template parser input and Previews
	private String[] citationParts = {"","",""};	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	String previewText;
	JTextPane previewPane;
	JScrollPane previewPaneScroll;

	long sourceTablePID, sourceDefnPID, selectedRepositoryTablePID;
	long sourceAuthorPID = null_RPID, sourceEditorPID = null_RPID, sourceCompilerPID = null_RPID;
	long originalSourceDefnPID;

	String[] sorcDefnTemplates;
	String fullFootNumberedTemplate, fullFootNamedTemplate, shortFootNumberedTemplate, shortFootNamedTemplate,
			biblioNumberedTemplate, biblioNamedTemplate;
	Object[][] sorcDefnTable;		// contains sourceDefn Name, PID
	Object[] sourceEditData = null;
	Object[] sourceStoreData = null;

	DocumentListener titleTextChange, abbrevTextChange,
					 fullFootTextChange, shortFootTextChange, biblioTextChange,
					 memoTextChange, remindTextChange;
	boolean fullFootTextChanged = false, shortFootTextChanged = false,
			biblioTextChanged = false, memoTextChanged = false, remindTextChanged = false,
			nameElementDataChanged = false, authorNameChanged = false;
	String templateTestBrackets, templateTestCodes;

/**
 * HG0566EditSource constructor
 * @param pointOpenProject
 * @param sourceTablePID
 */
		public HG0566EditSource(HBProjectOpenData pointOpenProject, long sourcePID)  {
			this.pointOpenProject = pointOpenProject;
			pointPersonHandler = pointOpenProject.getPersonHandler();
			pointReportHandler = pointOpenProject.getReportHandler();
			pointRepositoryHandler = pointOpenProject.getRepositoryHandler();
			this.sourceTablePID = sourcePID;

			setTitle(HG0566Msgs.Text_5);	// Edit Source
			setResizable(false);
		// Setup references for HG0450
			windowID = screenID;
			helpName = "editsource";		 //$NON-NLS-1$

		// Setup close and logging actions
			setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0566EditSource");	//$NON-NLS-1$

		// For Text area font setting
		    font = UIManager.getFont("TextArea.font");		//$NON-NLS-1$

	   // Load table headings from T204
			tableSrcElmntValueHeads =
					pointPersonHandler.setTranslatedData("56600", "1", false);	// Source Element, Value //$NON-NLS-1$ //$NON-NLS-2$
			tableSrcSrcColHeads =
					pointPersonHandler.setTranslatedData("56600", "2", false); // ID, Sources (abbrev.)  //$NON-NLS-1$ //$NON-NLS-2$
			tableRepoColHeads =
					pointPersonHandler.setTranslatedData("56600", "3", false); // ID, Repository, Abbrev  //$NON-NLS-1$ //$NON-NLS-2$

	// Focus Policy still to be setup!

		}	// End HG0566EditSource constructor


/**
 * Setup the screen layouts
 * protected void screenLayout()
 */
	 @SuppressWarnings("serial")
	protected void screenLayout() {
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]", "[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	   	JToolBar toolBar = new JToolBar();
    	toolBar.setFloatable(false);
    	toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
    	toolBar.add(Box.createHorizontalGlue());
    	// Add the HG0450 icons
		toolBar.add(btn_Remindericon);
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north");	//$NON-NLS-1$

	// ****** Define the tabbed Pane structure - tabbedPane size effectively sets screen size
		JTabbedPane tabPane = new JTabbedPane(SwingConstants.TOP);
		tabPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
		tabPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		tabPane.setPreferredSize(new Dimension(750, 500));
		contents.add(tabPane, "cell 0 0");	//$NON-NLS-1$

	// ******Define main (General) panel for most data
        JPanel genPanel = new JPanel();
        tabPane.addTab(HG0566Msgs.Text_6, null, genPanel);	// General
        tabPane.setMnemonicAt(0, KeyEvent.VK_G);
        genPanel.setLayout(new MigLayout("insets 10", "[]10[]", "[]10[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        // Define sub-panel for Source title etc
		JPanel titlePanel = new JPanel();
		titlePanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		titlePanel.setLayout(new MigLayout("insets 10", "[]10[]", "[]5[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel title = new JLabel(HG0566Msgs.Text_7);	// Source Title
		titlePanel.add(title, "cell 0 0, alignx right");	//$NON-NLS-1$
		titleText = new JTextArea();
		titleText.setWrapStyleWord(true);
		titleText.setLineWrap(true);
		titleText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		titleText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)titleText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		titleText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		titleText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		titleText.setBorder(new JTable().getBorder());		// match Table border
		JScrollPane titleTextScroll = new JScrollPane(titleText);
		titleTextScroll.setPreferredSize(new Dimension(550, 200));
		titleTextScroll.getViewport().setOpaque(false);
		titleTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		titleText.setCaretPosition(0);	// set scrollbar to top
		titlePanel.add(titleTextScroll, "cell 1 0, alignx left");	//$NON-NLS-1$

		JLabel abbrev = new JLabel(HG0566Msgs.Text_8);	// Abbreviation
		titlePanel.add(abbrev, "cell 0 1, alignx right");	//$NON-NLS-1$
		abbrevText = new JTextField();
//		abbrevText.setColumns(60);
		abbrevText.setEditable(true);
		titlePanel.add(abbrevText, "cell 1 1, alignx left, growx");	//$NON-NLS-1$

		JLabel srcType = new JLabel(HG0566Msgs.Text_9);	// Source Type
		titlePanel.add(srcType, "cell 0 2, alignx right");	//$NON-NLS-1$
		DefaultComboBoxModel<String> comboTypeModel = new DefaultComboBoxModel<>();
		//JComboBox<String> comboSourceTypes = new JComboBox<>(comboTypeModel);
		comboSourceTypes = new JComboBox<>(comboTypeModel);
		titlePanel.add(comboSourceTypes, "cell 1 2, alignx left");	//$NON-NLS-1$

		genPanel.add(titlePanel, "cell 0 0 2, grow");	//$NON-NLS-1$

		// Define sub-panel for Source element table of values
		JPanel valuePanel = new JPanel();
		valuePanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		valuePanel.setLayout(new MigLayout("insets 10", "[]", "[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// Setup JTable to show (editable) Source elements
		tableSrcElmntValues = new JTable() {
			private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int row, int column) {
					if (column == 0) return false;
					return true;
			}};

	 // Setup tableSrcElmntValues, model and renderer
		srcElmntValueModel = new DefaultTableModel(tableSrcElmntValueData, tableSrcElmntValueHeads);
		tableSrcElmntValues.setModel(srcElmntValueModel);

	// Make table single-click editable
		((DefaultCellEditor) tableSrcElmntValues.getDefaultEditor(Object.class)).setClickCountToStart(1);
	// and make loss of focus on a cell terminate the edit
		tableSrcElmntValues.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);	//$NON-NLS-1$
		tableSrcElmntValues.getColumnModel().getColumn(0).setMinWidth(80);
		tableSrcElmntValues.getColumnModel().getColumn(0).setPreferredWidth(150);
		tableSrcElmntValues.getColumnModel().getColumn(1).setMinWidth(180);
		tableSrcElmntValues.getColumnModel().getColumn(1).setPreferredWidth(230);
		tableSrcElmntValues.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		centerLabelRenderer = new DefaultTableCellRenderer();
		centerLabelRenderer.setHorizontalAlignment(JLabel.CENTER);
	    // Set header format
		JTableHeader pHeader = tableSrcElmntValues.getTableHeader();
		pHeader.setOpaque(false);
		JLabel pheaderLabel = (JLabel) centerLabelRenderer;
		pheaderLabel.setHorizontalAlignment(SwingConstants.CENTER);
	// Set row selection action
		ListSelectionModel rowSelectionModel = tableSrcElmntValues.getSelectionModel();
		rowSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		// Show the table
		tableSrcElmntValues.setMaximumSize(new Dimension(32767, 32767));
		tableSrcElmntValues.setFillsViewportHeight(true);
		// Setup tabbing within table against all rows but only column 0-1
		if (tableSrcElmntValues.getRowCount() > 0)
					JTableCellTabbing.setTabMapping(tableSrcElmntValues, 0, tableSrcElmntValues.getRowCount(), 0, 1);
	// scrollPane contains the Source Element picklist
		JScrollPane scrollSrcElmntTable = new JScrollPane();
		scrollSrcElmntTable.setPreferredSize(new Dimension(400, 320));
		scrollSrcElmntTable.setViewportView(tableSrcElmntValues);
		valuePanel.add(scrollSrcElmntTable, "cell 0 0"); //$NON-NLS-1$

		genPanel.add(valuePanel, "cell 0 1, grow"); //$NON-NLS-1$

		// Define sub-panel for Source footnote/biblio templates
		JPanel templatePanel = new JPanel();
		templatePanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		templatePanel.setLayout(new MigLayout("insets 10", "[]", "5[]5[]10[]5[]10[]5[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel fullFoot = new JLabel(HG0566Msgs.Text_10);	// Full footnote
		templatePanel.add(fullFoot, "cell 0 0, alignx left");	//$NON-NLS-1$
		btn_fullPreview = new JButton(HG0566Msgs.Text_11);		// Preview
		templatePanel.add(btn_fullPreview, "cell 0 0, gapx 10, alignx left");	//$NON-NLS-1$
		fullFootOverride = new JLabel(HG0566Msgs.Text_12);	// (overridden)
		templatePanel.add(fullFootOverride, "cell 0 0, gapx 10, alignx left");	//$NON-NLS-1$
		fullFootOverride.setVisible(false); // turn off by default
		fullFootText = new JTextArea();
		fullFootText.setLineWrap(true);
		fullFootText.setWrapStyleWord(true);
		fullFootText.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		fullFootText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		fullFootText.setBorder(new JTable().getBorder());		// match Table border
		JScrollPane fullFootTextScroll = new JScrollPane(fullFootText);
		fullFootTextScroll.setPreferredSize(new Dimension(300, 120));
		fullFootTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		templatePanel.add(fullFootTextScroll, "cell 0 1, alignx left");	//$NON-NLS-1$

		JLabel shortFoot = new JLabel(HG0566Msgs.Text_13);	// Short footnote
		templatePanel.add(shortFoot, "cell 0 2, alignx left");	//$NON-NLS-1$
		btn_shortPreview = new JButton(HG0566Msgs.Text_11);		// Preview
		templatePanel.add(btn_shortPreview, "cell 0 2, gapx 10, alignx left");	//$NON-NLS-1$
		shortFootOverride = new JLabel(HG0566Msgs.Text_12);	// (overridden)
		templatePanel.add(shortFootOverride, "cell 0 2, gapx 10, alignx left");	//$NON-NLS-1$
		shortFootOverride.setVisible(false); // turn off by default
		shortFootText = new JTextArea();
		shortFootText.setLineWrap(true);
		shortFootText.setWrapStyleWord(true);
		shortFootText.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		shortFootText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		shortFootText.setBorder(new JTable().getBorder());		// match Table border
		JScrollPane shortFootTextScroll = new JScrollPane(shortFootText);
		shortFootTextScroll.setPreferredSize(new Dimension(300, 100));
		shortFootTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		templatePanel.add(shortFootTextScroll, "cell 0 3, alignx left");	//$NON-NLS-1$

		JLabel biblio = new JLabel(HG0566Msgs.Text_16);		// Bibliogrpahy
		templatePanel.add(biblio, "cell 0 4, alignx left");	//$NON-NLS-1$
		btn_biblioPreview = new JButton(HG0566Msgs.Text_11);		// Preview
		templatePanel.add(btn_biblioPreview, "cell 0 4, gapx 10, alignx left");	//$NON-NLS-1$
		biblioOverride = new JLabel(HG0566Msgs.Text_12);	// (overridden)
		templatePanel.add(biblioOverride, "cell 0 4, gapx 10, alignx left");	//$NON-NLS-1$
		biblioOverride.setVisible(false); // turn off by default
		biblioText = new JTextArea();
		biblioText.setLineWrap(true);
		biblioText.setWrapStyleWord(true);
		biblioText.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		biblioText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		biblioText.setBorder(new JTable().getBorder());		// match Table border
		JScrollPane biblioTextScroll = new JScrollPane(biblioText);
		biblioTextScroll.setPreferredSize(new Dimension(300, 120));
		biblioTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		templatePanel.add(biblioTextScroll, "cell 0 5, alignx left");	//$NON-NLS-1$

		genPanel.add(templatePanel, "cell 1 1, aligny top"); //$NON-NLS-1$

	// ******Define Additional tabbed panel for other data
        JPanel addPanel = new JPanel();
        tabPane.addTab(HG0566Msgs.Text_19, null, addPanel);		// Additional
        tabPane.setMnemonicAt(0, KeyEvent.VK_A);
        addPanel.setLayout(new MigLayout("insets 10", "[]10[]", "10[]10[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// Define sub-panel for Active, Fidelity data
        JPanel activPanel = new JPanel();
        activPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
        activPanel.setLayout(new MigLayout("insets 10", "[]10[]", "[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        JLabel active = new JLabel(HG0566Msgs.Text_20);	// Source is Active
        activPanel.add(active, "cell 0 0"); //$NON-NLS-1$
        activChkBox = new JCheckBox();
        activPanel.add(activChkBox, "cell 1 0"); //$NON-NLS-1$

        JLabel fidelity = new JLabel(HG0566Msgs.Text_21);		// Source Fidelity
        activPanel.add(fidelity, "cell 0 1"); //$NON-NLS-1$
		comboFidelity = new JComboBox<String>(fidelityOptions);
		for (int i=0; i < fidelityOptions.length; i++) {
				comboFidelity.setSelectedIndex(i);
		}

		comboFidelity.setSelectedIndex(-1);		// Set as no value for starters
	    activPanel.add(comboFidelity, "cell 1 1"); //$NON-NLS-1$

        addPanel.add(activPanel, "cell 0 0, aligny top, grow"); //$NON-NLS-1$

		// Define sub-panel for connected personID's
        JPanel idPanel = new JPanel();
        idPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
        idPanel.setLayout(new MigLayout("insets 10", "[]10[]", "[]10[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        JLabel author = new JLabel(HG0566Msgs.Text_22);	// Author of this Source
        idPanel.add(author, "cell 0 0, alignx right"); //$NON-NLS-1$
		authorName = new JTextField() {
		    @Override
		    protected void processKeyEvent(KeyEvent e) {
		        // block all editing but allow double-click to still update this field
		    }
		};
		authorName.setColumns(50);
		idPanel.add(authorName, "cell 1 0, alignx left");	//$NON-NLS-1$

        JLabel editor = new JLabel(HG0566Msgs.Text_23);	// Editor of this Source
        idPanel.add(editor, "cell 0 1, alignx right"); //$NON-NLS-1$
    	editorName = new JTextField() {
		    @Override
		    protected void processKeyEvent(KeyEvent e) {
		        // block all editing but allow double-click to still update this field
		    }
    	};
		editorName.setColumns(50);
		idPanel.add(editorName, "cell 1 1, alignx left");	//$NON-NLS-1$

        JLabel compiler = new JLabel(HG0566Msgs.Text_24);	// Compiler of this Source
        idPanel.add(compiler, "cell 0 2, alignx right"); //$NON-NLS-1$
    	compilerName = new JTextField() {
		    @Override
		    protected void processKeyEvent(KeyEvent e) {
		        // block all editing but allow double-click to still update this field
		    }
    	};
		compilerName.setColumns(50);
		idPanel.add(compilerName, "cell 1 2, alignx left");	//$NON-NLS-1$

        addPanel.add(idPanel, "cell 1 0, aligny top"); //$NON-NLS-1$

		// Define sub-panel for Source Memo/Source reference text (if any)
        JPanel memoPanel = new JPanel();
        memoPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),HG0566Msgs.Text_25)); // Source Reference text
        memoPanel.setLayout(new MigLayout("insets 10", "[]", "[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		memoText = new JTextArea();
		memoText.setWrapStyleWord(true);
		memoText.setLineWrap(true);
		memoText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		memoText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)memoText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		memoText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		memoText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		memoText.setBorder(new JTable().getBorder());		// match Table border
		JScrollPane memoTextScroll = new JScrollPane(memoText);
		memoTextScroll.setPreferredSize(new Dimension(650, 140));
		memoTextScroll.getViewport().setOpaque(false);
		memoTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		memoText.setCaretPosition(0);	// set scrollbar to top
		memoPanel.add(memoTextScroll, "cell 0 0, alignx left");	//$NON-NLS-1$

        addPanel.add(memoPanel, "cell 0 1 2"); //$NON-NLS-1$

		// Define sub-panel for Reminder text (if any)
        JPanel remPanel = new JPanel();
        remPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),HG0566Msgs.Text_26));	// Source Reminder
        remPanel.setLayout(new MigLayout("insets 10", "[]", "[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		remindText = new JTextArea();
		remindText.setWrapStyleWord(true);
		remindText.setLineWrap(true);
		remindText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		remindText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)remindText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		remindText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		remindText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		remindText.setBorder(new JTable().getBorder());		// match Table border
		JScrollPane remindTextScroll = new JScrollPane(remindText);
		remindTextScroll.setPreferredSize(new Dimension(650, 140));
		remindTextScroll.getViewport().setOpaque(false);
		remindTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		remindText.setCaretPosition(0);	// set scrollbar to top
		remPanel.add(remindTextScroll, "cell 0 0, alignx left,grow");	//$NON-NLS-1$

        addPanel.add(remPanel, "cell 0 2 2"); //$NON-NLS-1$

    	// ******Define Connections panel for repository, source of source
        JPanel connPanel = new JPanel();
        tabPane.addTab(HG0566Msgs.Text_27, null, connPanel);		// Connections
        tabPane.setMnemonicAt(0, KeyEvent.VK_C);
        connPanel.setLayout(new MigLayout("insets 10", "[]", "[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        // Define sub-panel for Source of Source
		JPanel srcsrcPanel = new JPanel();
		srcsrcPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		srcsrcPanel.setLayout(new MigLayout("insets 10", "[]", "[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	    JLabel srcsrc = new JLabel(HG0566Msgs.Text_28);		// Sources of this Source
	    srcsrcPanel.add(srcsrc, "cell 0 0, alignx left"); //$NON-NLS-1$
		btn_srcAdd = new JButton("+"); //$NON-NLS-1$
		btn_srcAdd.setFont(new Font("Arial", Font.BOLD, 12)); //$NON-NLS-1$
		btn_srcAdd.setMaximumSize(new Dimension(24, 24));
		btn_srcAdd.setEnabled(true);
		srcsrcPanel.add(btn_srcAdd, "cell 0 0, gapx 30, aligny top"); //$NON-NLS-1$
		btn_srcDel = new JButton("-"); //$NON-NLS-1$
		btn_srcDel.setFont(new Font("Arial", Font.BOLD, 12));	//$NON-NLS-1$
		btn_srcDel.setMaximumSize(new Dimension(24, 24));
		btn_srcDel.setEnabled(false);
		srcsrcPanel.add(btn_srcDel, "cell 0 0, aligny top"); //$NON-NLS-1$

		// Setup JTable to show (editable) Source of Sources data
		tableSrcSrc = new JTable() {
			private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
			}};
		// Setup Source of Source table, model and renderer
		srcSrcTableModel = new DefaultTableModel(tableSrcSrcData, tableSrcSrcColHeads) ;
        tableSrcSrc.setModel(srcSrcTableModel);
		tableSrcSrc.getColumnModel().getColumn(0).setMinWidth(40);
		tableSrcSrc.getColumnModel().getColumn(0).setPreferredWidth(50);
		tableSrcSrc.getColumnModel().getColumn(1).setMinWidth(150);
		tableSrcSrc.getColumnModel().getColumn(1).setPreferredWidth(645);
		tableSrcSrc.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableSrcSrc.getColumnModel().getColumn(0).setCellRenderer(centerLabelRenderer);
	    // Set header format
		JTableHeader sHeader = tableSrcSrc.getTableHeader();
		sHeader.setOpaque(false);
		JLabel sheaderLabel = (JLabel) centerLabelRenderer;
		sheaderLabel.setHorizontalAlignment(SwingConstants.CENTER);
		// Set row selection action
		ListSelectionModel rowSrcSelectionModel = tableSrcSrc.getSelectionModel();
		rowSrcSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		// Show the table
		tableSrcSrc.setMaximumSize(new Dimension(32767, 32767));
		tableSrcSrc.setFillsViewportHeight(true);
		// Setup tabbing within table against all rows but only column 0-1
		if (tableSrcSrc.getRowCount() > 0)
					JTableCellTabbing.setTabMapping(tableSrcSrc, 0, tableSrcSrc.getRowCount(), 0, 1);
		// scrollPane contains the Source of Source picklist
		JScrollPane scrollSrcTable = new JScrollPane();
		scrollSrcTable.setPreferredSize(new Dimension(700, 240));
		scrollSrcTable.setViewportView(tableSrcSrc);
		srcsrcPanel.add(scrollSrcTable, "cell 0 1, growx"); //$NON-NLS-1$

		connPanel.add(srcsrcPanel, "cell 0 0, growx");	//$NON-NLS-1$

		// Define sub-panel for Repositories
		JPanel repoPanel = new JPanel();
		repoPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		repoPanel.setLayout(new MigLayout("insets 10", "[]", "[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	    JLabel repos = new JLabel(HG0566Msgs.Text_29);		// Repositories
	    repoPanel.add(repos, "cell 0 0, alignx left"); //$NON-NLS-1$
		btn_repoAdd = new JButton("+"); //$NON-NLS-1$
		btn_repoAdd.setFont(new Font("Arial", Font.BOLD, 12)); //$NON-NLS-1$
		btn_repoAdd.setMaximumSize(new Dimension(24, 24));
		btn_repoAdd.setEnabled(true);
		repoPanel.add(btn_repoAdd, "cell 0 0, gapx 30, aligny top"); //$NON-NLS-1$
		btn_repoDel = new JButton("-"); //$NON-NLS-1$
		btn_repoDel.setFont(new Font("Arial", Font.BOLD, 12));	//$NON-NLS-1$
		btn_repoDel.setMaximumSize(new Dimension(24, 24));
		btn_repoDel.setEnabled(false);
		repoPanel.add(btn_repoDel, "cell 0 0, aligny top"); //$NON-NLS-1$

		// Setup JTable to show (editable) Repository data
		tableRepo = new JTable() {
			private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
			}};
	 	// Setup tableRepoData, model and renderer
		repoTableModel = new DefaultTableModel(tableRepoData, tableRepoColHeads) ;
        tableRepo.setModel(repoTableModel);
		tableRepo.getColumnModel().getColumn(0).setMinWidth(40);
		tableRepo.getColumnModel().getColumn(0).setPreferredWidth(50);
		tableRepo.getColumnModel().getColumn(1).setMinWidth(150);
		tableRepo.getColumnModel().getColumn(1).setPreferredWidth(450);
		tableRepo.getColumnModel().getColumn(2).setMinWidth(50);
		tableRepo.getColumnModel().getColumn(2).setPreferredWidth(195);
		tableRepo.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableRepo.getColumnModel().getColumn(0).setCellRenderer(centerLabelRenderer);
	    // Set header format
		JTableHeader rHeader = tableRepo.getTableHeader();
		rHeader.setOpaque(false);
		JLabel rheaderLabel = (JLabel) centerLabelRenderer;
		rheaderLabel.setHorizontalAlignment(SwingConstants.CENTER);
		// Set row selection action
		ListSelectionModel rowRepoSelectionModel = tableRepo.getSelectionModel();
		rowRepoSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		// Show the table
		tableRepo.setMaximumSize(new Dimension(32767, 32767));
		tableRepo.setFillsViewportHeight(true);
		// Setup tabbing within table against all rows but only column 0-1
		if (tableRepo.getRowCount() > 0)
					JTableCellTabbing.setTabMapping(tableRepo, 0, tableRepo.getRowCount(), 0, 1);
		JScrollPane scrollRepoTable = new JScrollPane();
		scrollRepoTable.setPreferredSize(new Dimension(700, 240));
		scrollRepoTable.setViewportView(tableRepo);
		repoPanel.add(scrollRepoTable, "cell 0 1, growx"); //$NON-NLS-1$

		connPanel.add(repoPanel, "cell 0 1, growx");	//$NON-NLS-1$

	// **** Define control buttons
		btn_Close = new JButton(HG0566Msgs.Text_30);		// Close
		btn_Close.setEnabled(true);
		contents.add(btn_Close, "cell 0 1, alignx right, gapx 10, tag cancel"); //$NON-NLS-1$
		btn_Save = new JButton(HG0566Msgs.Text_31);		// Save
		btn_Save.setEnabled(false);
		contents.add(btn_Save, "cell 0 1, alignx right, gapx 10, tag ok"); //$NON-NLS-1$

	// End of Screen Definitions
		pack();

	// Now define a JTextPane and scrollpane for use by Source Template Preview buttons.
		 previewPane = new JTextPane();
		previewPane.setEditable(false);
		previewPane.setContentType("text/html"); //$NON-NLS-1$
		previewPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		previewPane.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		previewPane.setBorder(new JTable().getBorder());		// match Table border
		previewPaneScroll = new JScrollPane(previewPane);
		previewPaneScroll.setPreferredSize(new Dimension(300, 120));
		previewPaneScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		// End of all Panel Definitions

	 }

/**
 * Load sourceEditData into the above dialog definitions
 * protected void loadData()
 * Format of sourceEditData[] is as follows:
			sourceEditData[0] = sourceTitle;			used
			sourceEditData[1] = sourceAbbrev;			used
			sourceEditData[2] = sourceRef;				use for reference number in table??
			sourceEditData[3] = sourceType;					ditto!
			sourceEditData[4] = sourceActiv;			used
			sourceEditData[5] = sourceFidelity;			used
			sourceEditData[6] = sourceFullFoot;			used
			sourceEditData[7] = sourceShortFoot;		used
			sourceEditData[8] = sourceBiblio;			used
			sourceEditData[9] = sourceText;				used
			sourceEditData[10] = sourceRemind;			used
			sourceEditData[11] = sourceDefnPID;			used to get SourceDefn templates and Defn Name
			sourceEditData[12] = sourceAuthorPID;		used
			sourceEditData[13] = sourceEditorPID;		used
			sourceEditData[14] = sourceCompilerPID;		used
 */
	 protected void loadData() {
	// Set the title and abbrev text
		titleText.setText((String)sourceEditData[0]);
		abbrevText.setText((String)sourceEditData[1]);

	// Load comboSourceTypes from the Source Defn table and set the index by matching PIDs
		// First save this sourceDefnPID for checking later if Defn changes
		originalSourceDefnPID = sourceDefnPID;
		long compare = sourceDefnPID;
		for (int j = 0; j < sorcDefnTable.length; j++) {
			comboSourceTypes.addItem((String) sorcDefnTable[j][0]);
			if (compare == (long)sorcDefnTable[j][1]) {
				comboSourceTypes.setSelectedIndex(j);
			}
		}

	// Set the active checkbox
		if ((boolean)sourceEditData[4] == true) activChkBox.setSelected(true);
		else activChkBox.setSelected(false);

	// Set the Fidelity combobox
		String fid = (String)sourceEditData[5];
		if (fid.equals("A")) comboFidelity.setSelectedIndex(0);				//$NON-NLS-1$
		else if (fid.equals("B")) comboFidelity.setSelectedIndex(1);		//$NON-NLS-1$
		else if (fid.equals("C")) comboFidelity.setSelectedIndex(2);		//$NON-NLS-1$
		else if (fid.equals("D")) comboFidelity.setSelectedIndex(3);		//$NON-NLS-1$
		else if (fid.equals("E")) comboFidelity.setSelectedIndex(4);		//$NON-NLS-1$

	// Set Source Memo and Reminder text
		memoText.setText((String)sourceEditData[9]);
		memoText.setCaretPosition(0);
		remindText.setText((String)sourceEditData[10]);
		remindText.setCaretPosition(0);

	// Get this Source's templates; use the 'createAndDisplayTextTemplates' routine, which
	// analyses them and the Source Defn templates, converts the Element [nnnnn] values
	// into Element Names using the Hashmap codeToTextMap, and loads the
	// result into the text areas for display.
		fullFootNumberedTemplate = (String)sourceEditData[6];		// get Source full footer
		shortFootNumberedTemplate = (String)sourceEditData[7];		// get Source short footer
		biblioNumberedTemplate = (String)sourceEditData[8];			// get Source bibliography
		createAndDisplayTextTemplates();

	// Create Lists of all UNIQUE Source Element Numbers and Source Element Names from the Source templates
		uniqueElementNums =
				extractUniqueElementNums(fullFootNumberedTemplate+shortFootNumberedTemplate+biblioNumberedTemplate);
		uniqueElementNames =
				extractUniqueElementNames(fullFootNamedTemplate+shortFootNamedTemplate+biblioNamedTemplate);

	// Match Element numbers from the above to create an Element Value list aligned with Element Names (if needed)
		if (pointEditSource instanceof HG0566UpdateSource)	loadElementValueTable(false);
		if (pointEditSource instanceof HG0566AddSource)	loadElementDefinitionTable();
		if (pointEditSource instanceof HG0566CopySource) loadElementValueTable(true);

	// Load Author, Editor, Compiler names (if present)
		sourceAuthorPID = (long) sourceEditData[12];
		sourceEditorPID = (long) sourceEditData[13];
		sourceCompilerPID = (long) sourceEditData[14];
		try {
			authorsName = pointCitationSourceHandler.getPersonName(sourceAuthorPID);
			editorsName = pointCitationSourceHandler.getPersonName(sourceEditorPID);
			compilersName = pointCitationSourceHandler.getPersonName(sourceCompilerPID);
		} catch (HBException hbe) {
			System.out.println( "HG0566EditSource - Name author, editor or compiler error: " + hbe.getMessage()); //$NON-NLS-1$
			hbe.printStackTrace();
		}
		if (authorsName.isEmpty()) authorName.setText(HG0566Msgs.Text_32);	//  Not recorded
		else authorName.setText(authorsName);
		if (editorsName.isEmpty()) editorName.setText(HG0566Msgs.Text_32);	//  Not recorded
		else editorName.setText(editorsName);
		if (compilersName.isEmpty()) compilerName.setText(HG0566Msgs.Text_32);	//  Not recorded
		else compilerName.setText(compilersName);

	// Load Source of Source data
		// The citnSrcSrcData Object contains a Source# and Source Abbrev to build into tableSrSrcData:
		if (citnSrcSrcData != null) {
			tableSrcSrcData = new Object[citnSrcSrcData.length][2];
			// Get the data and load into SrcSrc tableModel
			for (int i=0; i < citnSrcSrcData.length; i++) {
				tableSrcSrcData[i][0] = citnSrcSrcData[i][0];
				tableSrcSrcData[i][1] = citnSrcSrcData[i][1];
				srcSrcTableModel.addRow(tableSrcSrcData[i]);
			}
		}
	 } // End of dialog data population processes

 /**
  * Load dialog-updated data into sourceStoreData
  * protected void storeData()
  * Format of sourceStoreData[] is as follows:
 			sourceStoreData[0] = sourceTitle;			used
 			sourceStoreData[1] = sourceAbbrev;			used
 			sourceStoreData[2] = sourceRef;				use for reference number in table??
 			sourceStoreData[3] = sourceType;			ditto!
 			sourceStoreData[4] = sourceActiv;			used
 			sourceStoreData[5] = sourceFidelity;		used
 			sourceStoreData[6] = sourceFullFoot;		used
 			sourceStoreData[7] = sourceShortFoot;		used
 			sourceStoreData[8] = sourceBiblio;			used
 			sourceStoreData[9] = sourceText;			used
 			sourceStoreData[10] = sourceRemind;			used
 			sourceStoreData[11] = sourceDefnPID;		used to get SourceDefn templates and Defn Name
 			sourceStoreData[12] = sourceAuthorPID;		used
 			sourceStoreData[13] = sourceEditorPID;		used
 			sourceStoreData[14] = sourceCompilerPID;		used
  */
 	 protected void storeData() {
 		 	sourceStoreData = new Object[15];
 			sourceStoreData[0] = titleText.getText(); //   sourceTitle - used
 			sourceStoreData[1] = abbrevText.getText(); //sourceAbbrev -	used
 			sourceStoreData[2] = ""; // sourceRefNr - use for reference number in table??  //$NON-NLS-1$
 			sourceStoreData[3] = ""; // sourceType - not much for this here!				//$NON-NLS-1$
 			sourceStoreData[4] = activChkBox.isSelected(); // sourceActiv;			used
 			if (comboFidelity.getSelectedIndex() == -1)
 				sourceStoreData[5] = "";		//$NON-NLS-1$
 			else sourceStoreData[5] = fidelityValues[comboFidelity.getSelectedIndex()]; // Fidelity used
 			sourceStoreData[6] = fullFootNumberedTemplate;	// used
 			sourceStoreData[7] = shortFootNumberedTemplate;	// used
 			sourceStoreData[8] = biblioNumberedTemplate;	// used
 			sourceStoreData[9] = memoText.getText(); //sourceMemo used
 			sourceStoreData[10] = remindText.getText(); //sourceRemind used
 		// used to get SourceDefn templates and Defn Name
 			sourceStoreData[11] = (long)sorcDefnTable[comboSourceTypes.getSelectedIndex()][1];
 			sourceStoreData[12] = sourceAuthorPID;
 			sourceStoreData[13] = sourceEditorPID;
 			sourceStoreData[14] = sourceCompilerPID;
 	 }

/**
 * CREATE All ACTION LISTENERS
 * public void createActionListner()
 */
	public void createActionListner() {
		// Listener for clicking 'X' on screen
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
		    	btn_Close.doClick();
			}
		});

		// Listener for Close button
		btn_Close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				if (reminderDisplay != null) reminderDisplay.dispose();
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0566EditSource");	//$NON-NLS-1$
				dispose();
			}
		});

		// Listener for changes made in tableSrcElmntValues
		TableModelListener elmntListener = new TableModelListener() {
			@Override
            public void tableChanged(TableModelEvent tme) {
                if (tme.getType() == TableModelEvent.UPDATE) {
                    int row = tme.getFirstRow();
                    if (row > -1) {
                    		String nameElementName = (String) tableSrcElmntValues.getValueAt(row, 0);
                    		String nameElementData = (String) tableSrcElmntValues.getValueAt(row, 1);
							String elementNumber = tableSrcElmntValueData[row][2];
						if (nameElementData != null) {
							btn_Save.setEnabled(true);
							nameElementDataChanged = true;
							pointCitationSourceHandler.updateElementDataChangeList(nameElementName, elementNumber, nameElementData);
						}
                    }
				}
			}
		};
		tableSrcElmntValues.getModel().addTableModelListener(elmntListener);

		// Listener for edit of Title text
		titleTextChange = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				btn_Save.setEnabled(true);
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				btn_Save.setEnabled(true);
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				btn_Save.setEnabled(true);
			}
		};
		titleText.getDocument().addDocumentListener(titleTextChange);

		// Listener for edit of Abbreviation text
		abbrevTextChange = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				btn_Save.setEnabled(true);
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				btn_Save.setEnabled(true);
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				btn_Save.setEnabled(true);
			}
		};
		abbrevText.getDocument().addDocumentListener(abbrevTextChange);

		// Listener for edit of Fullfooter text
		fullFootTextChange = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				fullFootTextChanged = true;
				btn_Save.setEnabled(true);
				fullFootOverride.setVisible(true);
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				fullFootTextChanged = true;
				btn_Save.setEnabled(true);
				fullFootOverride.setVisible(true);
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				fullFootTextChanged = true;
				btn_Save.setEnabled(true);
				fullFootOverride.setVisible(true);
			}
		};
		fullFootText.getDocument().addDocumentListener(fullFootTextChange);

		// Listener for edit of Short footer text
		shortFootTextChange = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				shortFootTextChanged = true;
				btn_Save.setEnabled(true);
				shortFootOverride.setVisible(true);
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				shortFootTextChanged = true;
				btn_Save.setEnabled(true);
				shortFootOverride.setVisible(true);
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				shortFootTextChanged = true;
				btn_Save.setEnabled(true);
				shortFootOverride.setVisible(true);
			}
		};
		shortFootText.getDocument().addDocumentListener(shortFootTextChange);

		// Listener for edit of Bibliography text
		biblioTextChange = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				biblioTextChanged = true;
				btn_Save.setEnabled(true);
				biblioOverride.setVisible(true);
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				biblioTextChanged = true;
				btn_Save.setEnabled(true);
				biblioOverride.setVisible(true);
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				biblioTextChanged = true;
				btn_Save.setEnabled(true);
				biblioOverride.setVisible(true);
			}
		};
		biblioText.getDocument().addDocumentListener(biblioTextChange);

		// Listener for edit of Source Memo text
		memoTextChange = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				memoTextChanged = true;
				btn_Save.setEnabled(true);
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				memoTextChanged = true;
				btn_Save.setEnabled(true);
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				memoTextChanged = true;
				btn_Save.setEnabled(true);
			}
		};
		memoText.getDocument().addDocumentListener(memoTextChange);

		// Listener for edit of Reminder text
		remindTextChange = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				remindTextChanged = true;
				btn_Save.setEnabled(true);
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				remindTextChanged = true;
				btn_Save.setEnabled(true);
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				remindTextChanged = true;
				btn_Save.setEnabled(true);
			}
		};
		remindText.getDocument().addDocumentListener(remindTextChange);

	// Mouse click listener for Author name
		authorName.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                // If no name present go direct to selecting one
                	if (authorsName.isEmpty()) {
                		try {
							activatePersonSelect(1);
						} catch (HBException hbe) {
							System.out.println("Author name edit error: " + hbe.getMessage()); //$NON-NLS-1$
							hbe.printStackTrace();
						}
                    	btn_Save.setEnabled(true);
                	}
                // Else ask if user wants to Select another or Delete this one
                	else try {
                    	selectOption(HG0566Msgs.Text_35, 1);		// Author
					} catch (HBException hbe) {
						System.out.println("Author name edit error: " + hbe.getMessage()); //$NON-NLS-1$
						hbe.printStackTrace();
					}
                }
            }
        });

	// Mouse click listener for Editor name
		editorName.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                // If no name present go direct to selecting one
                	if (editorsName.isEmpty()) {
                		try {
							activatePersonSelect(2);
						} catch (HBException hbe) {
							System.out.println("Editor name edit error: " + hbe.getMessage()); //$NON-NLS-1$
							hbe.printStackTrace();
						}
                    	btn_Save.setEnabled(true);
                	}
                // Else ask if user wants to Select another or Delete this one
                	else try {
                    	selectOption(HG0566Msgs.Text_36, 2);		// Editor
					} catch (HBException hbe) {
						System.out.println("Editor name edit error: " + hbe.getMessage()); //$NON-NLS-1$
						hbe.printStackTrace();
					}
                }
            }
        });

	// Mouse click listener for Compilor name
		compilerName.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                // If no name present go direct to selecting one
                	if (compilersName.isEmpty()) {
                		try {
							activatePersonSelect(3);
						} catch (HBException hbe) {
							System.out.println("Compiler name edit error: " + hbe.getMessage()); //$NON-NLS-1$
							hbe.printStackTrace();
						}
                    	btn_Save.setEnabled(true);
                	}
                // Else ask if user wants to Select another or Delete this one
                	else try {
                    	selectOption(HG0566Msgs.Text_37, 3);		// Compiler
					} catch (HBException hbe) {
						System.out.println("Compiler name edit error: " + hbe.getMessage()); //$NON-NLS-1$
						hbe.printStackTrace();
					}
                }
            }
        });

	// Source Types combo-box listener
		comboSourceTypes.addActionListener (new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
			// Get the PID of the newly selected Source Defn (Type)
				int indx = comboSourceTypes.getSelectedIndex();
				sourceDefnPID = (long) sorcDefnTable[indx][1];
			// Get its 3 source templates
				sorcDefnTemplates = new String[3];
				try {
					sorcDefnTemplates = pointCitationSourceHandler.getSourceDefnTemplates(sourceDefnPID);
				} catch (HBException hbe) {
					System.out.println( "Error loading source defn templates: " + hbe.getMessage()); //$NON-NLS-1$
					hbe.printStackTrace();
				}
			// Reset the Source templates and use new Source Defn templates to rebuild the displayed templates;
			// BUT if user has re-selected the ORIGINAL sourceDefn re-use this Source's templates
				if (sourceDefnPID == originalSourceDefnPID) {
					fullFootNumberedTemplate = (String)sourceEditData[6];		// get Source full footer
					shortFootNumberedTemplate = (String)sourceEditData[7];		// get Source short footer
					biblioNumberedTemplate = (String)sourceEditData[8];			// get Source bibliography
				} else {
					fullFootNumberedTemplate = "";	//$NON-NLS-1$
					shortFootNumberedTemplate = "";	//$NON-NLS-1$
					biblioNumberedTemplate = "";	//$NON-NLS-1$
				}
				createAndDisplayTextTemplates();

			// Create Lists of all UNIQUE Source Element Numbers and Names from the revised Source templates
				uniqueElementNums =
						extractUniqueElementNums(fullFootNumberedTemplate+shortFootNumberedTemplate+biblioNumberedTemplate);
				uniqueElementNames =
						extractUniqueElementNames(fullFootNamedTemplate+shortFootNamedTemplate+biblioNamedTemplate);

			// Rebuild the Source Element Value table to show the values that might still apply
				if (pointEditSource instanceof HG0566UpdateSource)	loadElementValueTable(false);
				if (pointEditSource instanceof HG0566AddSource)	loadElementDefinitionTable();
				if (pointEditSource instanceof HG0566CopySource) loadElementValueTable(true);
				btn_Save.setEnabled(true);
			}
		});

		// Source Fidelity combo-box listener
		comboFidelity.addActionListener (new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				btn_Save.setEnabled(true);
			}
		});

		activChkBox.addActionListener (new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				btn_Save.setEnabled(true);
			}
		});

		// Listener for tableSrcElmntValues row selection
		tableSrcElmntValues.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent selectSrc) {
				if (!selectSrc.getValueIsAdjusting()) {
					if (tableSrcElmntValues.getSelectedRow() == -1) return;
					btn_Save.setEnabled(true);
				}
			}
        });

		// Listener for fullFootnote Preview button
		btn_fullPreview.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				previewText = pointReportHandler.parseFootnoteBiblio(fullFootNumberedTemplate,	// footnote template
										sourceTablePID,					// PID of this Source
										(String)sourceEditData[9],		// Source Memo text
										tableSourceElmntDataValues,		// source element numbers, values
										citationParts);					// dummy citation entries
				previewPane.setText(previewText);
				previewPane.setCaretPosition(0);    // show from the top
				JOptionPane.showMessageDialog(
					tableSrcElmntValues,		// position on screen
				    previewPaneScroll,
				    HG0566Msgs.Text_38,		// Full Footnote Preview
				    JOptionPane.PLAIN_MESSAGE
				);
			}
		});

		// Listener for shortFootnote Preview button
		btn_shortPreview.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				previewText = pointReportHandler.parseFootnoteBiblio(shortFootNumberedTemplate,	// footnote template
									sourceTablePID,					// PID of this Source
									(String)sourceEditData[9],		// Source Memo text
									tableSourceElmntDataValues,		// source element numbers, values
									citationParts);					// dummy citation entries
				previewPane.setText(previewText);
				previewPane.setCaretPosition(0);    // show from the top
				JOptionPane.showMessageDialog(
					tableSrcElmntValues,		// position on screen
				    previewPaneScroll,
				    HG0566Msgs.Text_39,		// Short Footnote Preview
				    JOptionPane.PLAIN_MESSAGE
				);
			}
		});

		// Listener for biblio Preview button
		btn_biblioPreview.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				previewText = pointReportHandler.parseFootnoteBiblio(biblioNumberedTemplate,	// biblio template
									sourceTablePID,					// PID of this Source
									(String)sourceEditData[9],		// Source Memo text
									tableSourceElmntDataValues,		// source element numbers, values
									citationParts);					// dummy citation entries
				previewPane.setText(previewText);
				previewPane.setCaretPosition(0);    // show from the top
				JOptionPane.showMessageDialog(
					tableSrcElmntValues,		// position on screen
				    previewPaneScroll,
				    HG0566Msgs.Text_40,		// Bibliography Preview
				    JOptionPane.PLAIN_MESSAGE
				);
			}
		});

		// Listener for tableSrcSrc row selection
		tableSrcSrc.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent selectSrcSrc) {
				if (!selectSrcSrc.getValueIsAdjusting()) {
					if (tableSrcSrc.getSelectedRow() == -1) return;
				// allow action on selected row
					btn_srcDel.setEnabled(true);
				}
			}
        });

		// Listener for tableRepo row selection
		tableRepo.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent selectRepo) {
				if (!selectRepo.getValueIsAdjusting()) {
					if (tableRepo.getSelectedRow() == -1) return;
				// allow action selected row
					btn_repoDel.setEnabled(true);
				// find repo clicked
					int selectedRowInTable = tableRepo.convertRowIndexToModel(tableRepo.getSelectedRow());
					selectedRepositoryTablePID = (long) repoLinkData[selectedRowInTable][0];
				}
			}
        });

		// Listener for Citation table mouse clicks
		tableSrcSrc.addMouseListener(new MouseAdapter() {
			@Override
	        public void mousePressed(MouseEvent me) {
				int keyAssocMin = 0;
	           	if (me.getClickCount() == 1 && tableSrcSrc.getSelectedRow() != -1) {
	           	// SINGLE-CLICK - turn on table controls
	           		btn_srcDel.setEnabled(true);
	           	}
        		// DOUBLE_CLICK - get Object to pass to  Citation Editor and do so
	           	if (me.getClickCount() == 2 && tableSrcSrc.getSelectedRow() != -1) {
	           		int atRow = tableSrcSrc.getSelectedRow();
	           		objCiteDataToEdit = citnSrcSrcData[atRow]; // select whole row
	        	// Display HG0555EditCitation with this data
					HG0555EditCitation citeScreen
								= new HG0555EditCitation(false, pointOpenProject, "T736", keyAssocMin, //$NON-NLS-1$
										(long) objCiteDataToEdit[3]);
					citeScreen.setModalityType(ModalityType.APPLICATION_MODAL);
					citeScreen.setModalityType(ModalityType.APPLICATION_MODAL);
					Point xySrcSrc = btn_srcAdd.getLocationOnScreen();
					citeScreen.setLocation(xySrcSrc.x, xySrcSrc.y + 30);
					citeScreen.setVisible(true);
	           	}
	        }
		});

		// Listener for Source of Source Add button - allow selection of a source
		btn_srcAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int keyAssocMin = 0;
				HG0555EditCitation citeScreen = null;
				pointCitationSourceHandler.setCitedTableData(citeOwnerType, sourceTablePID);
				citeScreen = new HG0555EditCitation(true, pointOpenProject, citeOwnerType, keyAssocMin);
				citeScreen.pointSelectedSource = pointEditSource;
				citeScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xySrcSrc = btn_srcAdd.getLocationOnScreen();
				citeScreen.setLocation(xySrcSrc.x, xySrcSrc.y + 30);
				citeScreen.setVisible(true);
				btn_Save.setEnabled(true);
			}
		});

		// Listener for Source of Source Delete button
		btn_srcDel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
           		try {
           			int clickedRow = tableSrcSrc.getSelectedRow();
					pointCitationSourceHandler.deleteCitationRecord((long)citnSrcSrcData[clickedRow][3]);
					resetCitationTable();
				} catch (HBException hbe) {
					System.out.println("HG0566EditSource delete citation error: " + hbe.getMessage());	//$NON-NLS-1$
					hbe.printStackTrace();
				}
			}
		});

		// Listener for RepositoryAdd button - allow selection of a Repository
		btn_repoAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				pointRepositoryHandler.setSelectedSourceTablePID(sourceTablePID);
				pointRepositoryHandler.pointEditSource = pointEditSource;
				HG0569ManageRepos repoScreen = new HG0569ManageRepos(pointOpenProject, true);
				repoScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xyRepo = btn_srcAdd.getLocationOnScreen();
				repoScreen.setLocation(xyRepo.x, xyRepo.y + 30);
				repoScreen.setVisible(true);
				btn_Save.setEnabled(true);
			}
		});

		// Listener for Repository Delete button
		btn_repoDel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					pointRepositoryHandler.deleteRespositoryLink(sourceTablePID, selectedRepositoryTablePID);
					resetRepositoryTable();
				} catch (HBException hbe) {
					System.out.println("Delete repository table error: " + hbe.getMessage()); //$NON-NLS-1$
					hbe.printStackTrace();
				}
			}
		});

	// Listener for table repo mouse clicks
		tableRepo.addMouseListener(new MouseAdapter() {
		@Override
        public void mousePressed(MouseEvent me) {
           	if (me.getClickCount() == 1 && tableRepo.getSelectedRow() != -1) {
           	// SINGLE-CLICK - turn on table controls
           		btn_srcDel.setEnabled(true);
           	}
    		// DOUBLE_CLICK - get Object to pass to  HG0571RepoLink Editor and do so
           	if (me.getClickCount() == 2 && tableRepo.getSelectedRow() != -1) {
           		int atRow = tableRepo.getSelectedRow();
           		// Display HG0571RepoLink with the repo data in atRow
           		pointRepositoryHandler.pointEditSource = pointEditSource;
           		HG0571RepoLink pointRepolink = new HG0571RepoLink(pointOpenProject, sourceTablePID, tableRepoData[atRow]);
           		pointRepolink.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xySrcSrc = btn_srcAdd.getLocationOnScreen();
				pointRepolink.setLocation(xySrcSrc.x, xySrcSrc.y + 30);
				pointRepolink.setVisible(true);
           	}
        }
	});
	}

/*
 * extractUniqueElementNums
 */
	public void createAndDisplayTextTemplates() {
		// Get this Source's templates; if empty, use the template from the SourceDefn templates,
		// then convert the Element [nnnnn] entries into Element Names using the Hashmap codeToTextMap
		// and load result into the text areas for display.
		// Start with the Full footer
		fullFootText.getDocument().removeDocumentListener(fullFootTextChange); // disable change listener
		if (fullFootNumberedTemplate.isEmpty()) {
				fullFootOverride.setVisible(false); // turn off (overriden)
				fullFootNumberedTemplate = sorcDefnTemplates[0];	// use Source Defn full footer
			}
		else {		// set flags to indicate this is a changed template as came from Source record
			fullFootOverride.setVisible(true);
			fullFootTextChanged = true;
			}
		fullFootNamedTemplate =
				pointReportHandler.convertNumsToNames(fullFootNumberedTemplate, codeToTextMap); // convert to Names
		fullFootText.setText(fullFootNamedTemplate.trim());
		fullFootText.setCaretPosition(0);
		fullFootText.getDocument().addDocumentListener(fullFootTextChange); // re-enable listener

		// then the Short footer
		shortFootText.getDocument().removeDocumentListener(shortFootTextChange); // disable change listener
		if (shortFootNumberedTemplate.isEmpty()) {
				shortFootOverride.setVisible(false); // turn off (overriden)
				shortFootNumberedTemplate = sorcDefnTemplates[1];	// use Source Defn short footer
			}
		else {		// set flags to indicate this is a changed template as came from Source record
			shortFootOverride.setVisible(true);
			shortFootTextChanged = true;
			}
		shortFootNamedTemplate =
				pointReportHandler.convertNumsToNames(shortFootNumberedTemplate, codeToTextMap); // convert to Names
		shortFootText.setText(shortFootNamedTemplate.trim());
		shortFootText.setCaretPosition(0);
		shortFootText.getDocument().addDocumentListener(shortFootTextChange); // re-enable listener

		// then the Bibliography template
		biblioText.getDocument().removeDocumentListener(biblioTextChange); // disable change listener
		if (biblioNumberedTemplate.isEmpty()) {
				biblioOverride.setVisible(false); // turn off (overriden)
				biblioNumberedTemplate = sorcDefnTemplates[2];	// use Source Defn bibliography
			}
		else {		// set flags to indicate this is a changed template as came from Source record
			biblioOverride.setVisible(true);
			biblioTextChanged = true;
			}
		biblioNamedTemplate =
				pointReportHandler.convertNumsToNames(biblioNumberedTemplate, codeToTextMap); // convert to Names
		biblioText.setText(biblioNamedTemplate.trim());
		biblioText.setCaretPosition(0);
		biblioText.getDocument().addDocumentListener(biblioTextChange); // re-enable listener
	}	// End createAndDisplayTextTemplates

/**
 * loadElementDefTable()
 */
	public void loadElementDefinitionTable() {
		uniqueElementValues = new ArrayList<String>();
		// Reload tableSrcElmntValueData
		srcElmntValueModel.setRowCount(0); 		// first clear all existing rows
		tableSrcElmntValueData = new String[uniqueElementNames.size()][3];
		for (int i=0; i < uniqueElementNames.size(); i++) {
			// Ignore elements with numbers in the 'specials' range as they will not have T734 values
			if (Integer.parseInt(uniqueElementNums.get(i)) < 40000) {
				tableSrcElmntValueData[i][0] = uniqueElementNames.get(i).trim();
				tableSrcElmntValueData[i][1] = "";		//$NON-NLS-1$
				tableSrcElmntValueData[i][2] = uniqueElementNums.get(i);
				srcElmntValueModel.addRow(tableSrcElmntValueData[i]);
			}
		}
	}

/*
 * loadElementValueTable
 */
	public void loadElementValueTable(boolean addNewElementRecords) {
		// Use the uniqueElementNums list to match Element numbers in the imported
		// tableSourceElmntDataValues to create an Element Value list aligned with Element Names,
		// then use that to load the table of Element names and values.
		// Any existing Element names/values that exist in the new Source Defn will be preserved.
		// But ignore Elements with numbers > 40000 (which were TMG Grp 28-32) as they do not have T734 values
		int numValues = tableSourceElmntDataValues.length;
		uniqueElementValues = new ArrayList<String>();
		boolean matched = false;
		for (int i=0; i < uniqueElementNums.size(); i++) {
			matched = false;
			for (int j=0; j < numValues; j++) {
				if (uniqueElementNums.get(i).equals(tableSourceElmntDataValues[j][0])) {
							uniqueElementValues.add(tableSourceElmntDataValues[j][1]);
							matched = true;
				}
			}
	// If no match for this Element Name, add a blank Value to the Value List
			if (matched == false) uniqueElementValues.add("");		//$NON-NLS-1$
		}

	// (Re)load tableSrcElmntValueData
		srcElmntValueModel.setRowCount(0); 		// first clear all existing rows
		tableSrcElmntValueData = new String[uniqueElementNames.size()][3];
		for (int i=0; i < uniqueElementNames.size(); i++) {
			// Ignore elements with numbers in the 'specials' range as they will not have T734 values
			if (Integer.parseInt(uniqueElementNums.get(i)) < 40000) {
				tableSrcElmntValueData[i][0] = uniqueElementNames.get(i).trim();
				tableSrcElmntValueData[i][1] = uniqueElementValues.get(i).trim();
				tableSrcElmntValueData[i][2] = uniqueElementNums.get(i);
				srcElmntValueModel.addRow(tableSrcElmntValueData[i]);

		// If copy source add changes to ElementDataChangeList to create source element copy
				if (addNewElementRecords)
					pointCitationSourceHandler.updateElementDataChangeList(
								tableSrcElmntValueData[i][0],
								tableSrcElmntValueData[i][2],
								tableSrcElmntValueData[i][1]);
			}
		}
	}	// End loadElementValueTable

/*
 * extractUniqueElementNums - extract a unique list of Source Element numbers from input String
 * @param - input
 * @return
 */
	public static List<String> extractUniqueElementNums(String input) {
		// Extract a unique list of Source Element numbers from input String (preserving the order)
		Set<String> uniqueEntries = new LinkedHashSet<>();
		Matcher matcher = Pattern.compile("\\[(\\d{5})\\]").matcher(input);	//$NON-NLS-1$
		while (matcher.find()) {
			uniqueEntries.add(matcher.group(1)); // strip brackets
		}
		return new ArrayList<>(uniqueEntries);
	}	// End extractUniqueElementNums

/*
 * extractUniqueElementNames - extract a unique list of Source Element names from input String
 * @param - input
 * @return
 */
	public static List<String> extractUniqueElementNames(String input) {
		// Extract a unique list of Source Element names from input String (preserving the order)
		Set<String> uniqueEntries = new LinkedHashSet<>();
		Matcher matcher = Pattern.compile("\\[(.*?)\\]").matcher(input); //$NON-NLS-1$
		while (matcher.find()) {
			String content = matcher.group(1);
			if (!content.contains(":")) {				//$NON-NLS-1$
				uniqueEntries.add(matcher.group()); // preserve brackets
			}
		}
		return new ArrayList<>(uniqueEntries);
	}	// End  extractUniqueElementNames

/**
 * resetCitationTable()
 * @throws HBException
 */
	public void resetCitationTable() throws HBException {
		citnSrcSrcData = pointCitationSourceHandler.getCitationSourceData(sourceTablePID, citeOwnerType);
	// Reload Source citation data
	// The citnSrcSrcData Object contains a Source# and Source Abbrev to build into tableSrSrcData:
		if (citnSrcSrcData != null) {
			tableSrcSrcData = new Object[citnSrcSrcData.length][2];
	// Get the data and load into SrcSrc tableModel
			for (int i=0; i < citnSrcSrcData.length; i++) {
				tableSrcSrcData[i][0] = citnSrcSrcData[i][0];
				tableSrcSrcData[i][1] = citnSrcSrcData[i][1];
				srcSrcTableModel.addRow(tableSrcSrcData[i]);
			}
		}
	// Setup Source of Source table, model and renderer
		srcSrcTableModel = new DefaultTableModel(tableSrcSrcData, tableSrcSrcColHeads) ;
        tableSrcSrc.setModel(srcSrcTableModel);
		tableSrcSrc.getColumnModel().getColumn(0).setMinWidth(40);
		tableSrcSrc.getColumnModel().getColumn(0).setPreferredWidth(50);
		tableSrcSrc.getColumnModel().getColumn(1).setMinWidth(150);
		tableSrcSrc.getColumnModel().getColumn(1).setPreferredWidth(545);
		tableSrcSrc.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}

/**
 * 	resetRepositoryTable()
 */
	public void resetRepositoryTable() {
	// First get the PIDs of all Repos connected to this Source via the T740 link table
		try {
			repoLinkData = pointRepositoryHandler.getRepoLinkData(sourceTablePID);
		} catch (HBException hbe) {
			System.out.println("Error loading repository PIDs: " + hbe.getMessage()); //$NON-NLS-1$
			hbe.printStackTrace();
		}
		// Then get the data for each of the Repos found in repoPIDs
		if (repoLinkData.length > 0) {
			tableRepoData = new Object[repoLinkData.length][5];
			for (int i=0; i < repoLinkData.length; i++) {
				repoEditData = new Object[3];
				try {
					repoEditData = pointRepositoryHandler.getRepositoryData((long) repoLinkData[i][0]);
				} catch (HBException hbe) {
					System.out.println( "Error loading repository data: " + hbe.getMessage()); //$NON-NLS-1$
					hbe.printStackTrace();
				}
				if (repoEditData != null) {
					// Save the data we need for the Repository table
					tableRepoData[i][0] = repoEditData[2];  // the repo ref#
					tableRepoData[i][1] = repoEditData[1];  // the repo abbrev
					tableRepoData[i][2] = repoLinkData[i][1];  // the repo link reference
					tableRepoData[i][3] = repoLinkData[i][2];  // the repo link Primary setting
					tableRepoData[i][4] = repoLinkData[i][0]; // Repository table PID
				}
			}
		}
		if(repoTableModel.getRowCount() > 0)  repoTableModel.setRowCount(0);
		repoTableModel.setDataVector(tableRepoData, tableRepoColHeads);
        tableRepo.setModel(repoTableModel);
		tableRepo.getColumnModel().getColumn(0).setMinWidth(40);
		tableRepo.getColumnModel().getColumn(0).setPreferredWidth(50);
		tableRepo.getColumnModel().getColumn(1).setMinWidth(150);
		tableRepo.getColumnModel().getColumn(1).setPreferredWidth(445);
		tableRepo.getColumnModel().getColumn(2).setMinWidth(50);
		tableRepo.getColumnModel().getColumn(2).setPreferredWidth(100);
		tableRepo.getColumnModel().getColumn(0).setCellRenderer(centerLabelRenderer);
	}		// End resetRepositoryTable

/**
 * activatePersonSelect()
 * @throws HBException
 */
	private void activatePersonSelect(int textFieldSelection) throws HBException {
		pointSelectPerson = pointCitationSourceHandler.activateSelectPerson(pointOpenProject, pointEditSource, textFieldSelection);
		pointSelectPerson.additionalPanel = false; // Turn off additional panels
		pointSelectPerson.setModalityType(ModalityType.APPLICATION_MODAL);
		Point xyShow = authorName.getLocationOnScreen();
		pointSelectPerson.setLocation(xyShow.x-300, xyShow.y-50);
		pointSelectPerson.setVisible(true);
	}

/**
 * public void resetNames(long personAuthorPID, long personEditorPID, long personCompilerPID)
 */
	public void resetNames(long personAuthorPID, long personEditorPID, long personCompilerPID) {
		sourceAuthorPID = personAuthorPID;
		sourceEditorPID = personEditorPID;
		sourceCompilerPID = personCompilerPID;
		try {
			authorsName = pointCitationSourceHandler.getPersonName(sourceAuthorPID);
			editorsName = pointCitationSourceHandler.getPersonName(sourceEditorPID);
			compilersName = pointCitationSourceHandler.getPersonName(sourceCompilerPID);
		} catch (HBException hbe) {
			System.out.println( "HG0566EditSource - Name auth, edit or compiler error: " + hbe.getMessage()); //$NON-NLS-1$
			hbe.printStackTrace();
		}
		if (authorsName.isEmpty()) authorName.setText(HG0566Msgs.Text_32);	// Not recorded
		else authorName.setText(authorsName);
		if (editorsName.isEmpty()) editorName.setText(HG0566Msgs.Text_32);	// Not recorded
		else editorName.setText(editorsName);
		if (compilersName.isEmpty()) compilerName.setText(HG0566Msgs.Text_32);	// Not recorded
		else compilerName.setText(compilersName);
	}

/**
 * private void selectOption( )
 * @param personTypeSelected
 * @param textFieldSelection
 * @throws HBException
 */
	private int selectOption(String personTypeSelected, int textFieldSelection) throws HBException {
		String[] options = { HG0566Msgs.Text_44, HG0566Msgs.Text_45, HG0566Msgs.Text_46 };	// Select, Delete, Cancel
		int choice = JOptionPane.showOptionDialog(
				pointEditSource, // Parent component (null for default frame)
				HG0566Msgs.Text_47, //Select another Person or Delete current entry?
				HG0566Msgs.Text_48 + personTypeSelected, // Edit
				0, 3, null, options, options[0] // Specifies the options
				);
		// Process the user's choice
		if (choice == JOptionPane.YES_OPTION) {			//Select option
			activatePersonSelect(textFieldSelection);
			btn_Save.setEnabled(true);
		}
		if (choice == JOptionPane.NO_OPTION) {		// Delete option
			int choice2 = JOptionPane.showConfirmDialog(pointEditSource,
					HG0566Msgs.Text_49,	// Are you sure you want to delete this entry?
					HG0566Msgs.Text_48 + personTypeSelected, // Edit
					JOptionPane.YES_NO_OPTION);
			if (choice2 ==JOptionPane.YES_OPTION) {
				pointCitationSourceHandler.updateTextFieldSelection(pointEditSource, textFieldSelection);
				pointCitationSourceHandler.updatePersonName(null_RPID);
				btn_Save.setEnabled(true);
				return choice;
			}
		}
		return choice;
	}

/**
 * private int validateBeforeSave( )
 * @throws HBException
 */
	public int validateBeforeSave() throws HBException {
		// Following checks are present to ensure a saved Source has a valid
		// Title and Abbreviation, and that all 3 source templates have matching [ ] brackets
		// as well as valid BOLD/ITAL/etc formatting codes.
		// If these tests OK, the Source Templates are then converted back to element number formats
		// to allow them to be saved correctly.
		// Code returns 99 if any error found or 0 if all tests passed successfully.

		// Test that Title/Abbrev are present
		if (titleText.getText().length() == 0 || abbrevText.getText().length() == 0) {
			JOptionPane.showMessageDialog(pointEditSource,
					HG0566Msgs.Text_51, // Both Title and Abbreviation must be entered \n before the Source can be saved
					HG0566Msgs.Text_52,		// Source Title
					JOptionPane.ERROR_MESSAGE);
			return 99;  // end Save, so user can fix this error
		}

	// If any source template has been changed, test it for valid bracket etc syntax
	// and then convert it back to Element Number format for saving.
		if (fullFootTextChanged) {
			// Test for unpaired brackets. Null returned data = good, otherwise show error
			templateTestBrackets = pointReportHandler.validateBrackets(fullFootText.getText());
			if (templateTestBrackets != null) {
				JOptionPane.showMessageDialog(pointEditSource,
						templateTestBrackets,
						HG0566Msgs.Text_53,		// Full footnote template error
						JOptionPane.ERROR_MESSAGE);
				return 99;  // end Save, so user can fix this error
			}
			// Test for unpaired formating codes. Null returned data = good, otherwise show error
			templateTestCodes = pointReportHandler.validateFormatCodes(fullFootText.getText());
			if (templateTestCodes != null) {
				JOptionPane.showMessageDialog(pointEditSource,
						templateTestCodes,
						HG0566Msgs.Text_53,	// Full footnote template error
						JOptionPane.ERROR_MESSAGE);
				return 99;  // end Save, so user can fix this error
			}
		// Convert source template element Names to element Numbers for saving
		// Null returned data means conversion has failed (converter has raised Exception)
			footTextType = HG0566Msgs.Text_82;		// in Full footnote
			String fullFootToSave = pointReportHandler.convertNamesToNums(fullFootText.getText(), textToCodeMap);
			if (fullFootToSave == null) return 99; // null data returned implies error, halt Save process
			// If all OK save validated template in data to be saved
			fullFootNumberedTemplate = fullFootToSave;
		}
		// but if fullFootText has NOT been changed, set the T736 template to be saved as blank
		else fullFootNumberedTemplate = "";	//$NON-NLS-1$

		if (shortFootTextChanged) {
			// Test for unpaired brackets. Null returned data = good, otherwise show error
			templateTestBrackets = pointReportHandler.validateBrackets(shortFootText.getText());
			if (templateTestBrackets != null) {
				JOptionPane.showMessageDialog(pointEditSource,
						templateTestBrackets,
						HG0566Msgs.Text_55,		// Short footnote template error
						JOptionPane.ERROR_MESSAGE);
				return 99;  // end Save, so user can fix this error
			}
			// Test for unpaired formating codes. Null returned data = good, otherwise show error
			templateTestCodes = pointReportHandler.validateFormatCodes(shortFootText.getText());
			if (templateTestCodes != null) {
				JOptionPane.showMessageDialog(pointEditSource,
						templateTestCodes,
						HG0566Msgs.Text_55,		// Short footnote template error
						JOptionPane.ERROR_MESSAGE);
				return 99;  // end Save, so user can fix this error
			}
		// Convert source template element Names to element Numbers for saving
		// Null returned data means conversion has failed (converter has raised Exception)
			footTextType = HG0566Msgs.Text_83;		// in Short footnote
			String shortFootToSave = pointReportHandler.convertNamesToNums(shortFootText.getText(), textToCodeMap);
			if (shortFootToSave == null) return 99; // null data returned implies error, halt Save process
			// If all OK save validated template in data to be saved
			shortFootNumberedTemplate = shortFootToSave;
		}
		// but if shortFootText has NOT been changed, set the T736 template to be saved as blank
		else shortFootNumberedTemplate = "";	//$NON-NLS-1$

		if (biblioTextChanged) {
			// Test for unpaired brackets. Null returned data = good, otherwise show error
			templateTestBrackets = pointReportHandler.validateBrackets(biblioText.getText());
			if (templateTestBrackets != null) {
				JOptionPane.showMessageDialog(pointEditSource,
						templateTestBrackets,
						HG0566Msgs.Text_57,		// Bibliography template error
						JOptionPane.ERROR_MESSAGE);
				return 99;  // end Save, so user can fix this error
			}
			// Test for unpaired formating codes. Null returned data = good, otherwise show error
			templateTestCodes = pointReportHandler.validateFormatCodes(biblioText.getText());
			if (templateTestCodes != null) {
				JOptionPane.showMessageDialog(pointEditSource,
						templateTestCodes,
						HG0566Msgs.Text_57,		// Bibliography template error
						JOptionPane.ERROR_MESSAGE);
				return 99;  // end Save, so user can fix this error
			}
		// Convert source template element Names to element Numbers for saving
		// Null returned data means conversion has failed (converter has raised Exception)
			footTextType = HG0566Msgs.Text_84;		// in Bibliography
			String biblioToSave = pointReportHandler.convertNamesToNums(biblioText.getText(), textToCodeMap);
			if (biblioToSave == null) return 99; // null data returned implies error, halt Save process
			// If all OK save validated template in data to be saved
			biblioNumberedTemplate = biblioToSave;
		}
		// but if biblioText has NOT been changed, set the T736 template to be saved as blank
		else biblioNumberedTemplate = "";	//$NON-NLS-1$

		// if we reach here all tests have been passed, so return 0
			return 0;
	}		// End validateBeforeSave

}  // End of HG0566EditSource
