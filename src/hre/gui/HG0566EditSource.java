package hre.gui;
/**************************************************************************************
 * EditSource -
 * ***********************************************************************************
 * v0.04.0032 2025-02-07 Original draft (D Ferguson)
 *			  2025-02-10 Change textAreas to textPanes for Preview buttons (D Ferguson)
 * 			  2025-06-29 Correctly handle Reminder screen display/remove (D Ferguson)
 *			  2025-08-16 Get table header from T204 (D Ferguson)
 *			  2025-09-29 Load all Source data and populate much of screen (D Ferguson)
 *			  2025-10-01 Fix error in Element Values table load (D Ferguson)
 *************************************************************************************
 * Notes for incomplete code still requiring attention
 * NOTE01 allow saving of the Source's data
 * NOTE02 need to handle change of Source Types - working on it
 * NOTE06 handle load/add/delete of source of source and repositories
 * NOTE07 make Preview buttons work on footnotes/biblio data
 *
 ************************************************************************************/

import java.awt.Component;
import java.awt.Cursor;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.DefaultCaret;

import hre.bila.HB0711Logging;
import hre.bila.HBCitationSourceHandler;
import hre.bila.HBException;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.gui.HGlobalCode.JTableCellTabbing;
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
	public HBCitationSourceHandler pointCitationSourceHandler;

	public static final String screenID = "56600"; //$NON-NLS-1$

	private JPanel contents;

	String[] tableSrcElmntValueHeads = null;
	String[][] tableSrcElmntValueData;
	DefaultTableModel srcElmntValueModel = null;
	private int rowClicked;

	String[] tableSrcSrcColHeads = null;
	Object[][] tableSrcSrcData;
	DefaultTableModel srcSrcTableModel = null;

	String[] tableRepoColHeads = null;
	Object[][] tableRepoData;
	DefaultTableModel repoTableModel = null;

	JTextArea remindText, referText;
	JTextPane fullFootText, shortFootText, biblioText;

	private String[] fidelityOptions = {"Original",
										"Photocopy",
										"Transcript",
										"Extract",
										"Other/Unknown" };
	String[][] tableSrcElmntData;
	String[][] tableSourceElmntDataValues;
	List<String> uniqueElementNums, uniqueElementNames, uniqueElementValues;
	long sourcePID, sourceDefnPID;
	String[] sorcDefnTemplates;
	String fullFootNumberedTemplate, fullFootNamedTemplate, shortFootNumberedTemplate, shortFootNamedTemplate,
			biblioNumberedTemplate, biblioNamedTemplate;
	Object[][] sorcDefnTable;
	Object[] sourceEditData = null;

	DocumentListener titleTextChange, abbrevTextChange, fullFootTextChange, shortFootTextChange, biblioTextChange,
					 referTextChange, remindTextChange;
	boolean titleTextChanged = false, abbrevTextChanged = false, fullFootTextChanged = false, shortFootTextChanged = false,
			biblioTextChanged = false, referTextChanged = false, remindTextChanged = false;

/**
 * Create the dialog
 */
	public HG0566EditSource(HBProjectOpenData pointOpenProject, long sourcePID)  {
		this.pointOpenProject = pointOpenProject;
		pointPersonHandler = pointOpenProject.getPersonHandler();
		pointCitationSourceHandler = pointOpenProject.getCitationSourceHandler();
		this.sourcePID = sourcePID;

		setTitle("Edit Source");
		setResizable(false);
	// Setup references for HG0450
		windowID = screenID;
		helpName = "editsource";		 //$NON-NLS-1$

	// Setup close and logging actions
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0566EditSource");	//$NON-NLS-1$

	// For Text area font setting
	    Font font = UIManager.getFont("TextArea.font");		//$NON-NLS-1$

/**************************************************
// Start loading all data required for this screen
 **************************************************/
	    // Collect static GUI text from T204 for all tables
		tableSrcElmntValueHeads =
				pointPersonHandler.setTranslatedData("56600", "1", false);	// Source Element, Value //$NON-NLS-1$ //$NON-NLS-2$
		tableSrcSrcColHeads =
				pointPersonHandler.setTranslatedData("56600", "2", false); // ID, Sources (abbrev.)  //$NON-NLS-1$ //$NON-NLS-2$
		tableRepoColHeads =
				pointPersonHandler.setTranslatedData("56600", "3", false); // ID, Repository  //$NON-NLS-1$ //$NON-NLS-2$

		// Load the Source Element list (names/ID#s) as we need it for Source template conversion & checking
		try {
			tableSrcElmntData = pointCitationSourceHandler.getSourceElmntList(HGlobal.dataLanguage);
		} catch (HBException hbe) {
			System.out.println( " Error loading Source Element list: " + hbe.getMessage());
			hbe.printStackTrace();
		}
	    // Then construct a lookup for "12345" → "[element text]" conversion
        Map<String, String> codeToTextMap = new HashMap<>();
        for (String[] row : tableSrcElmntData) {
            if (row.length >= 2)
                codeToTextMap.put(row[1], row[0].trim());
        }
        // Then construct a lookup for "[text here]" → "12345" conversion back
        Map<String, String> textToCodeMap = new HashMap<>();
        for (String[] row : tableSrcElmntData) {
            if (row.length >= 2) {
                String trimmedText = row[0].trim();
                textToCodeMap.put(trimmedText, row[1]);
            }
        }

        // Get the data for this Source for populating this screen
 		try {
 			sourceEditData = pointCitationSourceHandler.getSourceEditData(sourcePID);
 		} catch (HBException hbe) {
 			System.out.println( " Error loading Source data: " + hbe.getMessage());
 			hbe.printStackTrace();
 		}

 		// Get the Source Defn list (to be able to get the type's name by matching the PID)
		try {
			sorcDefnTable = pointCitationSourceHandler.getSourceDefnList();
		} catch (HBException hbe) {
			System.out.println( " Error loading source defn list: " + hbe.getMessage());
			hbe.printStackTrace();
		}
		// and sort it as we need it in sorted order for its combobox
		Arrays.sort(sorcDefnTable, (row1, row2) -> ((String) row1[0]).compareTo((String) row2[0]));

 		// Get the underlying Source Defn templates using the sorcDefnPID value
		sourceDefnPID = (long)sourceEditData[11];
		try {
			sorcDefnTemplates = pointCitationSourceHandler.getSourceDefnTemplates(sourceDefnPID);
		} catch (HBException hbe) {
			System.out.println( " Error loading source defn templates: " + hbe.getMessage());
			hbe.printStackTrace();
		}

		// Get the Source Element data values currently saved for this Source
		try {
			tableSourceElmntDataValues = pointCitationSourceHandler.getSourceElmntDataValues(sourcePID);
		} catch (HBException hbe) {
			System.out.println( " Error loading source element values: " + hbe.getMessage());
			hbe.printStackTrace();
		}

/****************************
// Setup the screen layouts
 ****************************/
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
		tabPane.setPreferredSize(new Dimension(640, 440));
		contents.add(tabPane, "cell 0 0");	//$NON-NLS-1$

	// ******Define main (General) panel for most data
        JPanel genPanel = new JPanel();
        tabPane.addTab("General", null, genPanel);
        tabPane.setMnemonicAt(0, KeyEvent.VK_G);
        genPanel.setLayout(new MigLayout("insets 10", "[]10[]", "[]10[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        // Define sub-panel for Source title etc
		JPanel titlePanel = new JPanel();
		titlePanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		titlePanel.setLayout(new MigLayout("insets 10", "[]10[]", "[]5[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel title = new JLabel("Source Title");
		titlePanel.add(title, "cell 0 0, alignx right");	//$NON-NLS-1$
		JTextArea titleText = new JTextArea();
		titleText.setWrapStyleWord(true);
		titleText.setLineWrap(true);
		titleText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		titleText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)titleText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		titleText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		titleText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		titleText.setBorder(new JTable().getBorder());		// match Table border
		JScrollPane titleTextScroll = new JScrollPane(titleText);
		titleTextScroll.setMinimumSize(new Dimension(550, 50));
		titleTextScroll.getViewport().setOpaque(false);
		titleTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		titleText.setCaretPosition(0);	// set scrollbar to top
		titlePanel.add(titleTextScroll, "cell 1 0, alignx left");	//$NON-NLS-1$

		JLabel abbrev = new JLabel("Abbreviation");
		titlePanel.add(abbrev, "cell 0 1, alignx right");	//$NON-NLS-1$
		JTextField abbrevText = new JTextField();
		abbrevText.setColumns(60);
		abbrevText.setEditable(true);
		titlePanel.add(abbrevText, "cell 1 1, alignx left");	//$NON-NLS-1$

		JLabel srcType = new JLabel("Source Type");
		titlePanel.add(srcType, "cell 0 2, alignx right");	//$NON-NLS-1$
		DefaultComboBoxModel<String> comboTypeModel = new DefaultComboBoxModel<>();			// NOTE02 - load list of SourceTypes here
//											= new DefaultComboBoxModel<>(pointxxxxx.get all source types());
		 JComboBox<String> comboSourceTypes = new JComboBox<>(comboTypeModel);
		titlePanel.add(comboSourceTypes, "cell 1 2, alignx left");	//$NON-NLS-1$

		genPanel.add(titlePanel, "cell 0 0 2, grow");	//$NON-NLS-1$

		// Define sub-panel for Source element table of values
		JPanel valuePanel = new JPanel();
		valuePanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		valuePanel.setLayout(new MigLayout("insets 10", "[]", "[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// Setup JTable to show (editable) Source elements
		JTable tableSrcElmntValues = new JTable() {
			private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int row, int column) {
					if (column == 0) return false;
					return true;
			}};

	 	// Setup tableSrcElmntValues, model and renderer
		srcElmntValueModel = new DefaultTableModel(tableSrcElmntValueData, tableSrcElmntValueHeads);
        tableSrcElmntValues.setModel(srcElmntValueModel);
		tableSrcElmntValues.getColumnModel().getColumn(0).setMinWidth(80);
		tableSrcElmntValues.getColumnModel().getColumn(0).setPreferredWidth(120);
		tableSrcElmntValues.getColumnModel().getColumn(1).setMinWidth(150);
		tableSrcElmntValues.getColumnModel().getColumn(1).setPreferredWidth(200);
		tableSrcElmntValues.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		DefaultTableCellRenderer centerLabelRenderer = new DefaultTableCellRenderer();
		centerLabelRenderer.setHorizontalAlignment(JLabel.CENTER);
//		tableSrcElmntValues.getColumnModel().getColumn(0).setCellRenderer(centerLabelRenderer);
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
		scrollSrcElmntTable.setPreferredSize(new Dimension(325, 270));
		scrollSrcElmntTable.setViewportView(tableSrcElmntValues);
		valuePanel.add(scrollSrcElmntTable, "cell 0 0"); //$NON-NLS-1$

		genPanel.add(valuePanel, "cell 0 1, grow"); //$NON-NLS-1$

		// Define sub-panel for Source footnote/biblio templates
		JPanel templatePanel = new JPanel();
		templatePanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		templatePanel.setLayout(new MigLayout("insets 10", "[]", "5[]5[]10[]5[]10[]5[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel fullFoot = new JLabel("Full footnote");
		templatePanel.add(fullFoot, "cell 0 0, alignx left");	//$NON-NLS-1$
		JButton btn_fullPreview = new JButton("Preview");
		templatePanel.add(btn_fullPreview, "cell 0 0, gapx 20, alignx left");	//$NON-NLS-1$
		fullFootText = new JTextPane();
		fullFootText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		fullFootText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		fullFootText.setBorder(new JTable().getBorder());		// match Table border
		fullFootText.setPreferredSize(new Dimension(260, 75));
		JScrollPane fullFootTextScroll = new JScrollPane(fullFootText);
		fullFootTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		templatePanel.add(fullFootTextScroll, "cell 0 1, alignx left");	//$NON-NLS-1$

		JLabel shortFoot = new JLabel("Short footnote");
		templatePanel.add(shortFoot, "cell 0 2, alignx left");	//$NON-NLS-1$
		JButton btn_shortPreview = new JButton("Preview");
		templatePanel.add(btn_shortPreview, "cell 0 2, gapx 20, alignx left");	//$NON-NLS-1$
		shortFootText = new JTextPane();
		shortFootText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		shortFootText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		shortFootText.setBorder(new JTable().getBorder());		// match Table border
		shortFootText.setPreferredSize(new Dimension(260, 60));
		JScrollPane shortFootTextScroll = new JScrollPane(shortFootText);
		shortFootTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		templatePanel.add(shortFootTextScroll, "cell 0 3, alignx left");	//$NON-NLS-1$

		JLabel biblio = new JLabel("Bibliography");
		templatePanel.add(biblio, "cell 0 4, alignx left");	//$NON-NLS-1$
		JButton btn_biblioPreview = new JButton("Preview");
		templatePanel.add(btn_biblioPreview, "cell 0 4, gapx 20, alignx left");	//$NON-NLS-1$
		biblioText = new JTextPane();
		biblioText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		biblioText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		biblioText.setBorder(new JTable().getBorder());		// match Table border
		biblioText.setPreferredSize(new Dimension(260, 60));
		JScrollPane biblioTextScroll = new JScrollPane(biblioText);
		biblioTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		templatePanel.add(biblioTextScroll, "cell 0 5, alignx left");	//$NON-NLS-1$

		genPanel.add(templatePanel, "cell 1 1, aligny top"); //$NON-NLS-1$

	// ******Define Additional tabbed panel for other data
        JPanel addPanel = new JPanel();
        tabPane.addTab("Additional", null, addPanel);
        tabPane.setMnemonicAt(0, KeyEvent.VK_A);
        addPanel.setLayout(new MigLayout("insets 10", "[]10[]", "10[]10[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// Define sub-panel for Active, Fidelity data
        JPanel activPanel = new JPanel();
        activPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
        activPanel.setLayout(new MigLayout("insets 10", "[]10[]", "[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        JLabel active = new JLabel("Source is Active");
        activPanel.add(active, "cell 0 0"); //$NON-NLS-1$
        JCheckBox activChkBox = new JCheckBox();
        activPanel.add(activChkBox, "cell 1 0"); //$NON-NLS-1$

        JLabel fidelity = new JLabel("Source Fidelity");
        activPanel.add(fidelity, "cell 0 1"); //$NON-NLS-1$
		JComboBox<String> comboFidelity = new JComboBox<String>(fidelityOptions);
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

        JLabel author = new JLabel("Author of this Source");
        idPanel.add(author, "cell 0 0, alignx right"); //$NON-NLS-1$
		JTextField authorName = new JTextField();
		authorName.setColumns(50);
		idPanel.add(authorName, "cell 1 0, alignx left");	//$NON-NLS-1$

        JLabel editor = new JLabel("Editor of this Source");
        idPanel.add(editor, "cell 0 1, alignx right"); //$NON-NLS-1$
    	JTextField editorName = new JTextField();
		editorName.setColumns(50);
		idPanel.add(editorName, "cell 1 1, alignx left");	//$NON-NLS-1$

        JLabel compiler = new JLabel("Compiler of this Source");
        idPanel.add(compiler, "cell 0 2, alignx right"); //$NON-NLS-1$
    	JTextField compilerName = new JTextField();
		compilerName.setColumns(50);
		idPanel.add(compilerName, "cell 1 2, alignx left");	//$NON-NLS-1$

        addPanel.add(idPanel, "cell 1 0, aligny top"); //$NON-NLS-1$

		// Define sub-panel for Referenced text (if any)
        JPanel refPanel = new JPanel();
        refPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Referenced Text"));
        refPanel.setLayout(new MigLayout("insets 10", "[]", "[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		referText = new JTextArea();
		referText.setWrapStyleWord(true);
		referText.setLineWrap(true);
		referText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		referText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)referText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		referText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		referText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		referText.setBorder(new JTable().getBorder());		// match Table border
		JScrollPane referTextScroll = new JScrollPane(referText);
		referTextScroll.setMinimumSize(new Dimension(600, 75));
		referTextScroll.getViewport().setOpaque(false);
		referTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		referText.setCaretPosition(0);	// set scrollbar to top
		refPanel.add(referTextScroll, "cell 0 0, alignx left");	//$NON-NLS-1$

        addPanel.add(refPanel, "cell 0 1 2"); //$NON-NLS-1$

		// Define sub-panel for Reminder text (if any)
        JPanel remPanel = new JPanel();
        remPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Reminder"));
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
		remindTextScroll.setMinimumSize(new Dimension(600, 75));
		remindTextScroll.getViewport().setOpaque(false);
		remindTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		remindText.setCaretPosition(0);	// set scrollbar to top
		remPanel.add(remindTextScroll, "cell 0 0, alignx left,grow");	//$NON-NLS-1$

        addPanel.add(remPanel, "cell 0 2 2"); //$NON-NLS-1$

    	// ******Define Connections panel for repository, source of source
        JPanel connPanel = new JPanel();
        tabPane.addTab("Connections", null, connPanel);
        tabPane.setMnemonicAt(0, KeyEvent.VK_C);
        connPanel.setLayout(new MigLayout("insets 10", "[]", "[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        // Define sub-panel for Source of Source
		JPanel srcsrcPanel = new JPanel();
		srcsrcPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		srcsrcPanel.setLayout(new MigLayout("insets 10", "[]", "[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	    JLabel srcsrc = new JLabel("Sources of this Source");
	    srcsrcPanel.add(srcsrc, "cell 0 0, alignx left"); //$NON-NLS-1$
		JButton btn_srcAdd = new JButton("+"); //$NON-NLS-1$
		btn_srcAdd.setFont(new Font("Arial", Font.BOLD, 12)); //$NON-NLS-1$
		btn_srcAdd.setMaximumSize(new Dimension(24, 24));
		btn_srcAdd.setEnabled(true);
		srcsrcPanel.add(btn_srcAdd, "cell 0 0, gapx 30, aligny top"); //$NON-NLS-1$
		JButton btn_srcDel = new JButton("-"); //$NON-NLS-1$
		btn_srcDel.setFont(new Font("Arial", Font.BOLD, 12));	//$NON-NLS-1$
		btn_srcDel.setMaximumSize(new Dimension(24, 24));
		btn_srcDel.setEnabled(false);
		srcsrcPanel.add(btn_srcDel, "cell 0 0, aligny top"); //$NON-NLS-1$

		// Setup JTable to show (editable) Source of Sources data
		JTable tableSrcSrc = new JTable() {
			private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
			}};

		// Get Source of Source data
		// load some dummy data for test & display - to be removed
		tableSrcSrcData = new Object[][] {{"11", "dummy entry for now"} };

		//		tableSrcSrcData = pointzzzzzHandler.xxxxxxxxxxxxxxxxxxxx		<<< load Source of Source data

	 	// Setup tableSrcSrcData, model and renderer
		srcSrcTableModel = new DefaultTableModel(tableSrcSrcData, tableSrcSrcColHeads) ;
        tableSrcSrc.setModel(srcSrcTableModel);
		tableSrcSrc.getColumnModel().getColumn(0).setMinWidth(40);
		tableSrcSrc.getColumnModel().getColumn(0).setPreferredWidth(50);
		tableSrcSrc.getColumnModel().getColumn(1).setMinWidth(150);
		tableSrcSrc.getColumnModel().getColumn(1).setPreferredWidth(480);
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
		scrollSrcTable.setPreferredSize(new Dimension(540, 250));
		scrollSrcTable.setViewportView(tableSrcSrc);
		srcsrcPanel.add(scrollSrcTable, "cell 0 1"); //$NON-NLS-1$

		connPanel.add(srcsrcPanel, "cell 0 0");	//$NON-NLS-1$

		// Define sub-panel for Repositories
		JPanel repoPanel = new JPanel();
		repoPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		repoPanel.setLayout(new MigLayout("insets 10", "[]", "[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	    JLabel repos = new JLabel("Repositories");
	    repoPanel.add(repos, "cell 0 0, alignx left"); //$NON-NLS-1$
		JButton btn_repoAdd = new JButton("+"); //$NON-NLS-1$
		btn_repoAdd.setFont(new Font("Arial", Font.BOLD, 12)); //$NON-NLS-1$
		btn_repoAdd.setMaximumSize(new Dimension(24, 24));
		btn_repoAdd.setEnabled(true);
		repoPanel.add(btn_repoAdd, "cell 0 0, gapx 30, aligny top"); //$NON-NLS-1$
		JButton btn_repoDel = new JButton("-"); //$NON-NLS-1$
		btn_repoDel.setFont(new Font("Arial", Font.BOLD, 12));	//$NON-NLS-1$
		btn_repoDel.setMaximumSize(new Dimension(24, 24));
		btn_repoDel.setEnabled(false);
		repoPanel.add(btn_repoDel, "cell 0 0, aligny top"); //$NON-NLS-1$

		// Setup JTable to show (editable) Repository data
		JTable tableRepo = new JTable() {
			private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int row, int column) {
					return true;
			}};

		// Get Repository data
		// load some dummy data for test & display - to be removed
		tableRepoData = new Object[][] {{"23", "dummy entry for now"}};

		//		tableReposData = pointzzzzzHandler.xxxxxxxxxxxxxxxxxxxx		<<< load Repository data

	 	// Setup tableRepoData, model and renderer
		repoTableModel = new DefaultTableModel(tableRepoData, tableRepoColHeads) ;
        tableRepo.setModel(repoTableModel);
		tableRepo.getColumnModel().getColumn(0).setMinWidth(40);
		tableRepo.getColumnModel().getColumn(0).setPreferredWidth(50);
		tableRepo.getColumnModel().getColumn(1).setMinWidth(150);
		tableRepo.getColumnModel().getColumn(1).setPreferredWidth(480);
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
		// scrollPane contains the Source of Source picklist
		JScrollPane scrollRepoTable = new JScrollPane();
		scrollRepoTable.setPreferredSize(new Dimension(540, 250));
		scrollRepoTable.setViewportView(tableRepo);
		repoPanel.add(scrollRepoTable, "cell 0 1"); //$NON-NLS-1$

		connPanel.add(repoPanel, "cell 0 1");	//$NON-NLS-1$

	// **** Define control buttons
		JButton btn_Cancel = new JButton("Cancel");		// Cancel
		btn_Cancel.setEnabled(true);
		contents.add(btn_Cancel, "cell 0 1, alignx right, gapx 10, tag cancel"); //$NON-NLS-1$
		JButton btn_Save = new JButton("Save");		// Save
		btn_Save.setEnabled(false);
		contents.add(btn_Save, "cell 0 1, alignx right, gapx 10, tag ok"); //$NON-NLS-1$

	// End of Panel Definitions

/********************************************************
// Load sourceEditData into the above dialog definitions
******************************************************** */
	       /* Format of sourceEditData[] is as follows
				sourceEditData[0] = sourceTitle;			used
				sourceEditData[1] = sourceAbbrev;			used
				sourceEditData[2] = sourceRef;					not much value to this code!
				sourceEditData[3] = sourceType;					ditto!
				sourceEditData[4] = sourceActiv;			used
				sourceEditData[5] = sourceFidelity;			used
				sourceEditData[6] = sourceFullFoot;			used
				sourceEditData[7] = sourceShortFoot;		used
				sourceEditData[8] = sourceBiblio;			used
				sourceEditData[9] = sourceText;				used
				sourceEditData[10] = sourceRemind;			used
				sourceEditData[11] = sourceDefnPID;			used to get SourceDefn templates and Defn Name
				sourceEditData[12] = sourceAuthorPID;
				sourceEditData[13] = sourceEditorPID;
				sourceEditData[14] = sourceCompilerPID;
			*/

	// Set the title and abbrev text
		titleText.setText((String)sourceEditData[0]);
		abbrevText.setText((String)sourceEditData[1]);

	// Load comboSourceTypes from the Source Defn table and set the index by matching PIDs
		for (int j=0; j < sorcDefnTable.length; j++) {
			comboSourceTypes.addItem((String) sorcDefnTable[j][0]);
			if (sourceDefnPID == (long)sorcDefnTable[j][1]) comboSourceTypes.setSelectedIndex(j);
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

	// Set Source reference and Reminder text
		referText.setText((String)sourceEditData[9]);
		referText.setCaretPosition(0);
		remindText.setText((String)sourceEditData[10]);
		remindText.setCaretPosition(0);

	// Get this Source's templates; if empty, use the template from the SourceDefn templates,
	// then convert the Element [nnnnn] entries into Element Names using the Hashmap codeToTextMap
	// and load result into the text areas for display.
	// Start with the Full footer
		fullFootNumberedTemplate = (String)sourceEditData[6];					// get Source full footer
		if (fullFootNumberedTemplate.isEmpty())
							fullFootNumberedTemplate = sorcDefnTemplates[0];	// Source Defn full footer
		fullFootNamedTemplate = HGlobalCode.convertNumsToNames(fullFootNumberedTemplate, codeToTextMap); // convert to names
		fullFootText.setText(fullFootNamedTemplate);
		fullFootText.setCaretPosition(0);
	// then the Short footer
		shortFootNumberedTemplate = (String)sourceEditData[7];							// get Source short footer
		if (shortFootNumberedTemplate.isEmpty())
									shortFootNumberedTemplate = sorcDefnTemplates[1];	// Source Defn short footer
		shortFootNamedTemplate = HGlobalCode.convertNumsToNames(shortFootNumberedTemplate, codeToTextMap); // convert to names
		shortFootText.setText(shortFootNamedTemplate);
		shortFootText.setCaretPosition(0);
	// then the Bibliography template
		biblioNumberedTemplate = (String)sourceEditData[8];							// get Source bibliography
		if (biblioNumberedTemplate.isEmpty())
									biblioNumberedTemplate = sorcDefnTemplates[2];	// Source Defn bibliography
		biblioNamedTemplate = HGlobalCode.convertNumsToNames(biblioNumberedTemplate, codeToTextMap); // convert to names
		biblioText.setText(biblioNamedTemplate);
		biblioText.setCaretPosition(0);

	// Create Lists of all UNIQUE Source Element Numbers and Source Element Names from the Source templates
		uniqueElementNums =
				extractUniqueElementNums(fullFootNumberedTemplate+shortFootNumberedTemplate+biblioNumberedTemplate);
		uniqueElementNames =
				extractUniqueElementNames(fullFootNamedTemplate+shortFootNamedTemplate+biblioNamedTemplate);

	// Match Element numbers to create an Element Value list aligned with Element Names
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
			if (matched == false) uniqueElementValues.add("");
		}
		// (Re)load tableSrcElmntValueData
		srcElmntValueModel.setRowCount(0); 			// first clear all existing rows
		tableSrcElmntValueData = new String[uniqueElementNames.size()][2];
		for (int i=0; i < uniqueElementNames.size(); i++) {
			tableSrcElmntValueData[i][0] = uniqueElementNames.get(i);
			tableSrcElmntValueData[i][1] = uniqueElementValues.get(i);
			srcElmntValueModel.addRow(tableSrcElmntValueData[i]);
		}




	// End of dialog data population process

	// Focus Policy still to be setup!

		pack();

/*****************************
 * CREATE All ACTION LISTENERS
 *****************************/
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
			public void actionPerformed(ActionEvent actEvent) {
				if (reminderDisplay != null) reminderDisplay.dispose();
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0566EditSource");	//$NON-NLS-1$
				dispose();
			}
		});

		// Listener for Save button
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: saving data in HG0566EditSource");	//$NON-NLS-1$

				// NOTE01 save any changed data - use the xxxEdited booleans to check text edits

				// When saving source templates use the convertNamesToNums routine with the textToCodeMap hashmap
				// to check all Element names exist and can be converted back to Element numbers, and do the
				// conversion back to the numbered version for saving.
				// It throws error msg and returns null, so if null returned, do not save!
				// Example usage using fullFoot text, with before/after console routines:
				//    System.out.println("Input="+fullFootText.getText());
				//    String fullFootToSave = HGlobalCode.convertNamesToNums(fullFootText.getText(), textToCodeMap);
				//    System.out.println("Output="+fullFootToSave);
				// then check for null response and do NOT dispose! (leave user to fix it and try again!)

			}
		});

		// Listener for edit of Title text
		titleTextChange = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				titleTextChanged = true;
				btn_Save.setEnabled(true);
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				titleTextChanged = true;
				btn_Save.setEnabled(true);
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				titleTextChanged = true;
				btn_Save.setEnabled(true);
			}
		};
		titleText.getDocument().addDocumentListener(titleTextChange);

		// Listener for edit of Abbreviation text
		titleTextChange = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				titleTextChanged = true;
				btn_Save.setEnabled(true);
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				titleTextChanged = true;
				btn_Save.setEnabled(true);
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				titleTextChanged = true;
				btn_Save.setEnabled(true);
			}
		};
		titleText.getDocument().addDocumentListener(titleTextChange);

		// Listener for edit of Abbreviation text
		abbrevTextChange = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				abbrevTextChanged = true;
				btn_Save.setEnabled(true);
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				abbrevTextChanged = true;
				btn_Save.setEnabled(true);
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				abbrevTextChanged = true;
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
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				fullFootTextChanged = true;
				btn_Save.setEnabled(true);
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				fullFootTextChanged = true;
				btn_Save.setEnabled(true);
			}
		};
		fullFootText.getDocument().addDocumentListener(fullFootTextChange);

		// Listener for edit of Short footer text
		shortFootTextChange = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				shortFootTextChanged = true;
				btn_Save.setEnabled(true);
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				shortFootTextChanged = true;
				btn_Save.setEnabled(true);
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				shortFootTextChanged = true;
				btn_Save.setEnabled(true);
			}
		};
		shortFootText.getDocument().addDocumentListener(shortFootTextChange);

		// Listener for edit of Bibliography text
		biblioTextChange = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				biblioTextChanged = true;
				btn_Save.setEnabled(true);
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				biblioTextChanged = true;
				btn_Save.setEnabled(true);
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				biblioTextChanged = true;
				btn_Save.setEnabled(true);
			}
		};
		biblioText.getDocument().addDocumentListener(biblioTextChange);

		// Listener for edit of Reference text
		referTextChange = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				referTextChanged = true;
				btn_Save.setEnabled(true);
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				referTextChanged = true;
				btn_Save.setEnabled(true);
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				referTextChanged = true;
				btn_Save.setEnabled(true);
			}
		};
		referText.getDocument().addDocumentListener(referTextChange);

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

		// Source Types combo-box listener
		comboSourceTypes.addActionListener (new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {

			// NOTE02 - for the new selected SourceType, perform following actions:
			// 1. set the combobox to show the selected SourceType
			// 2. for that SourceType, get its 3 source templates into sorcDefnTemplates
			// 3. then re-load the foot/biblio templates into their textPanes, using the Source ones (if they
			//		exist), defaulting to the SorcDefnTemplates
			// 4. re-analyze these templates to extract to source element numbers they use
			// 5. rebuild the source element value table to show the values that apply to this source

			}
		});

		// Source Fidelity combo-box listener
		comboFidelity.addActionListener (new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {

			// NOTE01 - based on index selected, convert to value in the A-E range for saving

			}
		});

		// Listener for tableSrcElmntValues row selection
		tableSrcElmntValues.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent selectSrc) {
				if (!selectSrc.getValueIsAdjusting()) {
					if (tableSrcElmntValues.getSelectedRow() == -1) return;
				// find source clicked
//					int clickedRow = tableSrcElmnt.getSelectedRow();
//					int selectedRowInTable = tableSrcElmnt.convertRowIndexToModel(clickedRow);

				// allow editing of selected row

					btn_Save.setEnabled(true);

				}
			}
        });

	// Define ActionListeners for Source Element values table right-click popupMenus
	// For popupMenu item popMenuDel - Delete the clicked Source Element
	    ActionListener popDel = new ActionListener() {
	        @Override
			public void actionPerformed(ActionEvent e) {
		// The right-clicked row is passed here in rowClicked
        		int rowInTable = tableSrcElmntValues.convertRowIndexToModel(rowClicked);
        		String elmntClicked = (String) tableSrcElmntData[rowInTable][0];
        		if (JOptionPane.showConfirmDialog(tableSrcElmntValues, "Are you sure you want to delete \n"	// Are you sure you want to delete \n
						  + "Source Element " + elmntClicked +"?",		// Source Element (name)?	//$NON-NLS-1$ //$NON-NLS-2$
						   "Delete Element",						// Delete Element
						   JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
					return;
				}
//				try {
//					pointxxxxxx.deleteSrcElement(rowInTable);
//					btn_Save.setEnabled(true);
        		// Reset
//					pointxxxxxx.resetSrcElement(pointOpenProject);
//				} catch (HBException hbe) {
//					hbe.printStackTrace();
//				}
	        }
	      };

	// For popupMenu item popMenuAdd - general Add Source Element
	    ActionListener popAdd = new ActionListener() {
	        @Override
			public void actionPerformed(ActionEvent e) {
				try {
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				// Show HG0564ManageSrcElmnt code to allow selection of new Element into the Source Element table
					HG0564ManageSrcElmnt elmntSelectScreen = new HG0564ManageSrcElmnt(pointOpenProject);
					elmntSelectScreen.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
					Point xyElmnt = abbrevText.getLocationOnScreen();
					elmntSelectScreen.setLocation(xyElmnt.x+100, xyElmnt.y);
					elmntSelectScreen.setVisible(true);
				} catch (HBException hbe) {
					hbe.printStackTrace();
				}
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				btn_Save.setEnabled(true);
	        }
	      };

	// Define Source Element popup menu items and actions
	    JMenuItem popMenuDel = new JMenuItem("Delete this Source Element");	// Delete this Source Element
	    popMenuDel.addActionListener(popDel);
	    JMenuItem popMenuAdd = new JMenuItem("Add new Source Element");		// Add new Source Element
	    popMenuAdd.addActionListener(popAdd);
	    JMenuItem popMenuAddOnly = new JMenuItem("Add new Source Element");	// Add new Source Element
	    popMenuAddOnly.addActionListener(popAdd);
	    // Define a right-click popup menu to use in tableSrcElmnt
	    JPopupMenu popupMenuElmnt = new JPopupMenu();
	    popupMenuElmnt.add(popMenuDel);
	    popupMenuElmnt.add(popMenuAdd);
	    // and a popup menu for anywhere in the table
	    JPopupMenu popupMenuOnlyAdd = new JPopupMenu();
	    popupMenuOnlyAdd.add(popMenuAddOnly);
	// Listener for Source Element table mouse click
	    tableSrcElmntValues.addMouseListener(new MouseAdapter() {
			@Override
            public void mousePressed(MouseEvent me) {
                if (me.getButton() == MouseEvent.BUTTON3) {
                // RIGHT-CLICK
                	rowClicked = tableSrcElmntValues.rowAtPoint(me.getPoint());
                	if (tableSrcElmntValues.rowAtPoint(me.getPoint()) < 0)
						popupMenuOnlyAdd.show(me.getComponent(), me.getX(), me.getY());
					else
						popupMenuElmnt.show(me.getComponent(), me.getX(), me.getY());
                }
            }
        });

		// Listener for fullFootnote Preview button
		btn_fullPreview.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				// NOTE07 invoke code to convert full footnote text to parsed and formatted version
			}
		});

		// Listener for shortFootnote Preview button
		btn_shortPreview.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				// NOTE07 invoke code to convert short footnote text to parsed and formatted version
			}
		});

		// Listener for biblio Preview button
		btn_biblioPreview.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				// NOTE07 invoke code to convert bibliography text to parsed and formatted version
			}
		});

		// Listener for tableSrcSrc row selection
		tableSrcSrc.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent selectSrcSrc) {
				if (!selectSrcSrc.getValueIsAdjusting()) {
					if (tableSrcSrc.getSelectedRow() == -1) return;
				// allow action on selected row
					btn_srcDel.setEnabled(true);
				// find source-source clicked
//					int clickedRow = tableSrcSrc.getSelectedRow();
//					int selectedRowInTable = tableSrcSrc.convertRowIndexToModel(clickedRow);

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
//					int clickedRow = tableRepo.getSelectedRow();
//					int selectedRowInTable = tableRepo.convertRowIndexToModel(clickedRow);

				}
			}
        });

		// Listener for Source of Source Add button - allow selection of a source
		btn_srcAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				HG0565ManageSource selectSrcScreen = new HG0565ManageSource(pointOpenProject,null);
				selectSrcScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xySrcSrc = btn_srcAdd.getLocationOnScreen();
				selectSrcScreen.setLocation(xySrcSrc.x, xySrcSrc.y + 30);
				selectSrcScreen.setVisible(true);
				btn_Save.setEnabled(true);
				}
		});

		// Listener for Source of Source Delete button
		btn_srcDel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// NOTE06 need code here to delete selected source ONLY IF NOT IN USE ANYWHERE!
			}
		});

		// Listener for RepositoryAdd button - allow selection of a Repository
		btn_repoAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				HG0569ManageRepos repoScreen = new HG0569ManageRepos(pointOpenProject);
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
				// NOTE06 need code here to delete selected repository ONLY IF NOT IN USE ANYWHERE!
			}
		});


	}	// End HG0566EditSource constructor

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
			if (!content.contains(":")) {
				uniqueEntries.add(matcher.group()); // preserve brackets
			}
		}
		return new ArrayList<>(uniqueEntries);
	}	// End  extractUniqueElementNames

}  // End of HG0566EditSource
