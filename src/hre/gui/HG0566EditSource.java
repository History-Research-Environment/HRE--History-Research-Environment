package hre.gui;
/**************************************************************************************
 * EditSource -
 * ***********************************************************************************
 * v0.04.0032 2025-02-07 Original draft (D Ferguson)
 *			  2025-02-10 Change textAreas to textPanes; add Preview buttons (D Ferguson)
 * 			  2025-06-29 Correctly handle Reminder screen display/remove (D Ferguson)
 *************************************************************************************
 * Notes for incomplete code still requiring attention
 * NOTE01 allow editing/saving of the Source's data
 * NOTE02 need to load combobox of all Source types for selection and handle changes
 * NOTE06 handle add/delete of source of source and repositories
 * NOTE07 make Preview buttons work
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.text.DefaultCaret;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
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

	public static final String screenID = "56600"; //$NON-NLS-1$

	private JPanel contents;

	private String[] tableSrcElmntColHeads = null;
	private Object[][] tableSrcElmntData;
	DefaultTableModel srcElmntTableModel = null;
	private int rowClicked;

	private String[] tableSrcSrcColHeads = null;
	private Object[][] tableSrcSrcData;
	DefaultTableModel srcSrcTableModel = null;

	private String[] tableRepoColHeads = null;
	private Object[][] tableRepoData;
	DefaultTableModel repoTableModel = null;

	JTextArea remindText, referText;
	JTextPane fullFootText, shortFootText, biblioText;

	private String[] fidelityOptions = {"Original",
										"Photocopy",
										"Transcript",
										"Extract",
										"Other/Unknown" };

/**
 * Create the dialog
 */
	public HG0566EditSource(HBProjectOpenData pointOpenProject)  {
		this.pointOpenProject = pointOpenProject;
		setTitle("Source Definition");
		setResizable(false);
	// Setup references for HG0450
		windowID = screenID;
		helpName = "editsource";		 //$NON-NLS-1$

	// Collect static GUI text from T204 for all tables (T204 data still to be preloaded)
//		String[] tableSrcElmntColHeads = pointPersonHandler.setTranslatedData(screenID, "1", false);		//$NON-NLS-1$
//		String[] tableSrcSrcColHeads = pointPersonHandler.setTranslatedData(screenID, "1", false);		//$NON-NLS-1$
//		String[] tableRepoColHeads = pointPersonHandler.setTranslatedData(screenID, "1", false);		//$NON-NLS-1$

		// Setup close and logging actions
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0566EditSource");	//$NON-NLS-1$

	// For Text area font setting
	    Font font = UIManager.getFont("TextArea.font");		//$NON-NLS-1$

	// Setup dialog
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
		contents.add(tabPane, "cell 0 0");

	// ******Define main (General) panel for most data
        JPanel genPanel = new JPanel();
        tabPane.addTab("General", null, genPanel);
        tabPane.setMnemonicAt(0, KeyEvent.VK_G);
        genPanel.setLayout(new MigLayout("insets 10", "[]10[]", "[]10[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        // Define sub-panel for Source title etc
		JPanel titlePanel = new JPanel();
		titlePanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		titlePanel.setLayout(new MigLayout("insets 10", "[]10[]", "[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel title = new JLabel("Title");
		titlePanel.add(title, "cell 0 0, alignx right");
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
		titleTextScroll.setMinimumSize(new Dimension(550, 30));
		titleTextScroll.getViewport().setOpaque(false);
		titleTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		titleText.setCaretPosition(0);	// set scrollbar to top
		titlePanel.add(titleTextScroll, "cell 1 0, alignx left");	//$NON-NLS-1$

		JLabel abbrev = new JLabel("Abbrev.");
		titlePanel.add(abbrev, "cell 0 1, alignx right");
		JTextField srcAbbrev = new JTextField();
		srcAbbrev.setColumns(50);
		srcAbbrev.setEditable(true);
		titlePanel.add(srcAbbrev, "cell 1 1, alignx left");	//$NON-NLS-1$

		genPanel.add(titlePanel, "cell 0  0 2, grow");

		// Define sub-panel for Source Type and Source element list table
		JPanel sourcePanel = new JPanel();
		sourcePanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		sourcePanel.setLayout(new MigLayout("insets 10", "[]", "[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel srcType = new JLabel("Source Type:");
		sourcePanel.add(srcType, "cell 0 0, alignx left");
		DefaultComboBoxModel<String> comboTypeModel = new DefaultComboBoxModel<>();			// NOTE02 - load list of SourceTypes here
//											= new DefaultComboBoxModel<>(pointxxxxx.get all source types());
		 JComboBox<String> comboSourceTypes = new JComboBox<>(comboTypeModel);
		sourcePanel.add(comboSourceTypes, "cell 0 0, alignx left, gapx 10");	//$NON-NLS-1$

		// Setup JTable to show (editable) Source elements
		JTable tableSrcElmnt = new JTable() {
			private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int row, int column) {
					if (column == 0) return false;
					return true;
			}};
		// Get Source data
		// load some dummy data for test & display - to be removed
		tableSrcElmntColHeads = new String[] {"Source Element", "Value" };
		tableSrcElmntData = new Object[][] {{"[TITLE]", "blah blah blah "},
										  {"[AUTHOR]", "John Smith"}  };

		//		tableSrcElmntData = pointzzzzzHandler.xxxxxxxxxxxxxxxxxxxx		<<< load Source Element data
		if (tableSrcElmntData == null ) {
			JOptionPane.showMessageDialog(tableSrcElmnt, "No data found in HRE database\n"	// No data found in HRE database\n
													 + "Source load error",		// Source load error
													   "Edit Source", 			// Edit Source
													   JOptionPane.ERROR_MESSAGE);
			dispose();
		}
	 	// Setup tableSrcElmntData, model and renderer
		srcElmntTableModel = new DefaultTableModel(tableSrcElmntData, tableSrcElmntColHeads) ;
        tableSrcElmnt.setModel(srcElmntTableModel);
		tableSrcElmnt.getColumnModel().getColumn(0).setMinWidth(80);
		tableSrcElmnt.getColumnModel().getColumn(0).setPreferredWidth(140);
		tableSrcElmnt.getColumnModel().getColumn(1).setMinWidth(150);
		tableSrcElmnt.getColumnModel().getColumn(1).setPreferredWidth(230);
		tableSrcElmnt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		DefaultTableCellRenderer centerLabelRenderer = new DefaultTableCellRenderer();
		centerLabelRenderer.setHorizontalAlignment(JLabel.CENTER);
		tableSrcElmnt.getColumnModel().getColumn(0).setCellRenderer(centerLabelRenderer);
	    // Set header format
		JTableHeader pHeader = tableSrcElmnt.getTableHeader();
		pHeader.setOpaque(false);
		JLabel pheaderLabel = (JLabel) centerLabelRenderer;
		pheaderLabel.setHorizontalAlignment(SwingConstants.CENTER);
		// Set row selection action
		ListSelectionModel rowSelectionModel = tableSrcElmnt.getSelectionModel();
		rowSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		// Show the table
		tableSrcElmnt.setMaximumSize(new Dimension(32767, 32767));
		tableSrcElmnt.setFillsViewportHeight(true);
		// Setup tabbing within table against all rows but only column 0-1
		if (tableSrcElmnt.getRowCount() > 0)
					JTableCellTabbing.setTabMapping(tableSrcElmnt, 0, tableSrcElmnt.getRowCount(), 0, 1);
		// scrollPane contains the Source Element picklist
		JScrollPane scrollSrcElmntTable = new JScrollPane();
		scrollSrcElmntTable.setPreferredSize(new Dimension(375, 250));
		scrollSrcElmntTable.setViewportView(tableSrcElmnt);
		sourcePanel.add(scrollSrcElmntTable, "cell 0 1"); //$NON-NLS-1$

		genPanel.add(sourcePanel, "cell 0 1, grow"); //$NON-NLS-1$

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
		fullFootText.setPreferredSize(new Dimension(200, 75));
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
		shortFootText.setPreferredSize(new Dimension(200, 60));
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
		biblioText.setPreferredSize(new Dimension(200, 60));
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
        activChkBox.setSelected(true);		// default setting
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
		tableSrcSrcColHeads = new String[] {"ID", "Source" };
		tableSrcSrcData = new Object[][] {{"11", "source eleven"},
										  {"27", "source twenty-seven"}  };

		//		tableSrcSrcData = pointzzzzzHandler.xxxxxxxxxxxxxxxxxxxx		<<< load Source of Source data
		if (tableSrcSrcData == null ) {
			JOptionPane.showMessageDialog(tableSrcElmnt, "No data found in HRE database\n"	// No data found in HRE database\n
													 + "Source of Sources load error",		// Source of Sources load error
													   "Edit Source", 			// Edit Source
													   JOptionPane.ERROR_MESSAGE);
			dispose();
		}
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

		genPanel.add(sourcePanel, "cell 0 1, grow"); //$NON-NLS-1$

		connPanel.add(srcsrcPanel, "cell 0 0");

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
		tableRepoColHeads = new String[] {"ID", "Repository" };
		tableRepoData = new Object[][] {{"23", "Repository 23"},
										  {"55", "Repository 55"}  };

		//		tableReposData = pointzzzzzHandler.xxxxxxxxxxxxxxxxxxxx		<<< load Repository data
		if (tableRepoData == null ) {
			JOptionPane.showMessageDialog(tableSrcElmnt, "No data found in HRE database\n"	// No data found in HRE database\n
													 + "Repository data load error",		// Repository data load error
													   "Edit Source", 			// Edit Source
													   JOptionPane.ERROR_MESSAGE);
			dispose();
		}
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

		connPanel.add(repoPanel, "cell 0 1");

	// **** Define control buttons
		JButton btn_Cancel = new JButton("Cancel");		// Cancel
		btn_Cancel.setEnabled(true);
		contents.add(btn_Cancel, "cell 0 1, alignx right, gapx 10, tag cancel"); //$NON-NLS-1$
		JButton btn_Save = new JButton("Save");		// Save
		btn_Save.setEnabled(false);
		contents.add(btn_Save, "cell 0 1, alignx right, gapx 10, tag ok"); //$NON-NLS-1$

	// End of Panel Definitions

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

				// NOTE01 perform save actions on all data

			}
		});

		// Source Types combo-box listener
		comboSourceTypes.addActionListener (new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {

				// NOTE02 - for the new selected SourceType, perform following actions:
				// 1. set the combobox to show the selected SourceType
				// 2. for that SourceType, load into tableSrcElmntData, column 0, all that Type's Source Elements
				// 3. for all Source Elements whose name matches the 'new' elements, also transfer the Value of the Element into col 1
				// 4. re-display the results in tableSrcElmnt

			}
		});

		// Listener for tableSrcElmnt row selection
		tableSrcElmnt.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent selectSrc) {
				if (!selectSrc.getValueIsAdjusting()) {
					if (tableSrcElmnt.getSelectedRow() == -1) return;
				// find source clicked
//					int clickedRow = tableSrcElmnt.getSelectedRow();
//					int selectedRowInTable = tableSrcElmnt.convertRowIndexToModel(clickedRow);

				// allow editing of selected row

					btn_Save.setEnabled(true);

				}
			}
        });

	// Define ActionListeners for Source Element table right-click popupMenus
	// For popupMenu item popMenuDel - Delete the clicked Source Element
	    ActionListener popDel = new ActionListener() {
	        @Override
			public void actionPerformed(ActionEvent e) {
		// The right-clicked row is passed here in rowClicked
        		int rowInTable = tableSrcElmnt.convertRowIndexToModel(rowClicked);
        		String elmntClicked = (String) tableSrcElmntData[rowInTable][0];
        		if (JOptionPane.showConfirmDialog(tableSrcElmnt, "Are you sure you want to delete \n"	// Are you sure you want to delete \n
						  + "Source Element " + elmntClicked +"?",		// Source Element (name)?	//$NON-NLS-1$
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
					Point xyElmnt = srcAbbrev.getLocationOnScreen();
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
	    tableSrcElmnt.addMouseListener(new MouseAdapter() {
			@Override
            public void mousePressed(MouseEvent me) {
                if (me.getButton() == MouseEvent.BUTTON3) {
                // RIGHT-CLICK
                	rowClicked = tableSrcElmnt.rowAtPoint(me.getPoint());
                	if (tableSrcElmnt.rowAtPoint(me.getPoint()) < 0)
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
				// NOTE06 need code here to delete selected source of source & turn on btn_Save
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
				// NOTE06 need code here to delete selected repository & turn on btn_Save
			}
		});


	}	// End HG0566EditSource constructor

}  // End of HG0566EditSource
