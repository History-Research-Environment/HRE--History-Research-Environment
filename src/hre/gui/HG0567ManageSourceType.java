package hre.gui;
/**************************************************************************************
 * ManageSourceType -
 * ***********************************************************************************
 * v0.04.0032 2025-01-17 Original draft (D Ferguson)
 *			  2025-01-31 Add Delete button, implement Add button (D Ferguson)
 *			  2025-05-26 Adjust miglayout settings (D Ferguson)
 *			  2025-08-16 Get table header from T204 (D Ferguson)
 *			  2025-09-04 Load Source Type list from database (D Ferguson)
 *			  2025-09-16 Add Source type count into table header (D Ferguson)
 *			  2025-09-25 Load Source templates and convert to Element Names (D Ferguson)
 *			  2025-09-27 Invoke HG0568 to perform Source Type edits (D Ferguson)
 * 			  2025-11-17 Change HGlobalCode routines to be in ReportHandler (D Ferguson)
 *			  2025-12-14 Load T737 records for current language (D Ferguson)
 *			  2025-12-22 Add, edit and copy source type implemented (N. Tolleshaug)
 *			  2025-12-28 Removed Save button; make table double-click do an Edit (D Ferguson)
 *						 Add language selection combo-box (D Ferguson)
 *			  2025-12-29 NLS all code to this point (D Ferguson)
 *			  2025-12-30 Remove Select button (redundant) (D Ferguson)
 *			  2025-12-31 Updated language setting for add source definition (N. Tolleshaug)
 *			  2026-01-06 Log catch block msgs (D Ferguson)
 ************************************************************************************/

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
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
import hre.bila.HBReportHandler;
import hre.gui.HGlobalCode.JTableCellTabbing;
import hre.nls.HG0567Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Manage Source Types
 * @author D Ferguson
 * @version v0.04.0032
 * @since 2025-01-17
 */

public class HG0567ManageSourceType extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;
	HBPersonHandler pointPersonHandler;
	HBReportHandler pointReportHandler;
	HG0567ManageSourceType pointManageSourceType = this;
	HG0568EditSourceType editSourcetypeScreen;
	public HBCitationSourceHandler pointCitationSourceHandler;

	public static final String screenID = "56700"; //$NON-NLS-1$

	private JPanel contents;
	JTable tableSourceType;

	JComboBox<String> comboLanguage;
	Object[] languages;
    ItemListener comboLangListener;
	String selectedLangCode = HGlobal.dataLanguage;
	String selectedLanguage;
	int languageIndexDefault, prevSelectedLangIndex, newSelectedLangIndex, elementCount;

	String[] tableSourceTypeColHeads = null;
	String tableSourceTypeHead;
	Object[][] tableSourceTypeData;
	DefaultTableModel srcTableModel = null;

	JTextArea fullFootText, shortFootText, biblioText;
	String[] sorcDefnTemplates;
	String[][] tableSrcElmntData;
	long selectedSourceDefnPID;
	long null_RPID  = 1999999999999999L;

/**
 * Create the dialog
 */
	public HG0567ManageSourceType(HBProjectOpenData pointOpenProject)  {
		this.pointOpenProject = pointOpenProject;
		pointPersonHandler = pointOpenProject.getPersonHandler();
		pointCitationSourceHandler = pointOpenProject.getCitationSourceHandler();
		pointReportHandler = pointOpenProject.getReportHandler();

	// NOTE this can be called directly from 'Evidence' on mainMenu , when title should be 'Manage Source types'
	// OR from HG0565ManageSource, when the title should be 'Select Source Type'
		setTitle(HG0567Msgs.Text_0);		// Manage Source Types
	// Setup references for HG0450
		windowID = screenID;
		helpName = "managesourcetype";		 //$NON-NLS-1$

		// Setup close and logging actions
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0567ManageSourceType");	//$NON-NLS-1$

	// Collect static GUI text from T204 for Source type table
		tableSourceTypeColHeads = pointPersonHandler.setTranslatedData("56700", "1", false); // Source Type	//$NON-NLS-1$ //$NON-NLS-2$

	// Load the Source Element list (names/ID#s) as we need it for Source template conversion & checking
		try {
			tableSrcElmntData = pointCitationSourceHandler.getSourceElmntList(HGlobal.dataLanguage);
		} catch (HBException hbe) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in HG0567 loading Source Elements: " + hbe.getMessage()); //$NON-NLS-1$
				HB0711Logging.printStackTraceToFile(hbe);
			}
		}
	    // Then construct a lookup for "12345" → "[element text]" conversion
        Map<String, String> codeToTextMap = new HashMap<>();
        for (String[] row : tableSrcElmntData) {
            if (row.length >= 2)
                codeToTextMap.put(row[1], row[0].trim());
        }

	// For Text area font setting
	    Font font = UIManager.getFont("TextArea.font");		//$NON-NLS-1$

	// Setup dialog
	    setResizable(false);
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]10[]10[]", "[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	   	JToolBar toolBar = new JToolBar();
    	toolBar.setFloatable(false);
    	toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
    	toolBar.add(Box.createHorizontalGlue());
    	// Add the HG0450 icons
		toolBar.add(btn_Remindericon);
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north");	//$NON-NLS-1$

	// Define panel for Action buttons
		JPanel actionPanel = new JPanel();
		actionPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		actionPanel.setLayout(new MigLayout("insets 5", "[]", "[]5[]20[]10[]10[]10[]10[][]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		JLabel lbl_Language = new JLabel(HG0567Msgs.Text_1);		// Language:
		actionPanel.add(lbl_Language, "cell 0 0, alignx center"); //$NON-NLS-1$

		// Load the data language names and set to current Global dataLanguage
		int languageCount = HG0501AppSettings.dataReptLanguages.length;
		comboLanguage = new JComboBox<String>();
		languages = new Object[languageCount];
		for (int i=0; i < languageCount; i++) {
				comboLanguage.addItem(HG0501AppSettings.dataReptLanguages[i]);
				languages[i] = HG0501AppSettings.dataReptLanguages[i];
		}
		if (HGlobal.numOpenProjects > 0)
			for (int i = 0; i < languageCount; i++)
				if (HGlobal.dataLanguage.equals(HG0501AppSettings.dataReptLangCodes[i])) {
					comboLanguage.setSelectedIndex(i);
					selectedLangCode = 	HG0501AppSettings.dataReptLangCodes[i];
					languageIndexDefault = i;
					prevSelectedLangIndex = comboLanguage.getSelectedIndex();
				}
		actionPanel.add(comboLanguage, "cell 0 1, alignx left, grow"); //$NON-NLS-1$

		JButton btn_Add = new JButton(HG0567Msgs.Text_2);		// Add
		btn_Add.setEnabled(true);
		actionPanel.add(btn_Add, "cell 0 2, alignx center, grow"); //$NON-NLS-1$
		JButton btn_Edit = new JButton(HG0567Msgs.Text_3);		// Edit
		btn_Edit.setEnabled(false);
		actionPanel.add(btn_Edit, "cell 0 3, alignx center, grow"); //$NON-NLS-1$
		JButton btn_Copy = new JButton(HG0567Msgs.Text_4);		// Copy
		btn_Copy.setEnabled(false);
		actionPanel.add(btn_Copy, "cell 0 4, alignx center, grow"); //$NON-NLS-1$
		JButton btn_Delete = new JButton(HG0567Msgs.Text_5);		// Delete
		btn_Delete.setEnabled(false);
		actionPanel.add(btn_Delete, "cell 0 5, alignx center, grow"); //$NON-NLS-1$
		JLabel find = new JLabel(HG0567Msgs.Text_6);		// Find:
		actionPanel.add(find, "cell 0 6, alignx center"); //$NON-NLS-1$
		JTextField findText = new JTextField();
		findText.setColumns(10);
		actionPanel.add(findText, "cell 0 7, alignx left"); //$NON-NLS-1$

		contents.add(actionPanel, "cell 0 0, aligny top"); //$NON-NLS-1$

	// Define panel for Source Type list
		JPanel sourceTypePanel = new JPanel();
		sourceTypePanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		sourceTypePanel.setLayout(new MigLayout("insets 5", "[]", "[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	// Setup JTable to show Source Type data
		tableSourceType = new JTable() {
			private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
			}};
		tableSourceType.setMaximumSize(new Dimension(32767, 32767));
		tableSourceType.setFillsViewportHeight(true);
	// Setup tabbing within table against all rows but only column 0
		if (tableSourceType.getRowCount() > 0)
					JTableCellTabbing.setTabMapping(tableSourceType, 0, tableSourceType.getRowCount(), 0, 0);
		// scrollPane contains the Source Type picklist
		JScrollPane scrollTable = new JScrollPane();
		scrollTable.setPreferredSize(new Dimension(370, 380));
		sourceTypePanel.add(scrollTable, "cell 0 0"); //$NON-NLS-1$
		contents.add(sourceTypePanel, "cell 1 0, grow"); //$NON-NLS-1$

	// Define panel for Source Types templates
		JPanel templatePanel = new JPanel();
		templatePanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		templatePanel.setLayout(new MigLayout("insets 5", "[]", "5[]5[]10[]5[]10[]5[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel fullFoot = new JLabel(HG0567Msgs.Text_7);		// Full footnote
		templatePanel.add(fullFoot, "cell 0 0, alignx left");	//$NON-NLS-1$
		fullFootText = new JTextArea();
		fullFootText.setWrapStyleWord(true);
		fullFootText.setLineWrap(true);
		fullFootText.setEditable(false);
		fullFootText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		fullFootText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)fullFootText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		fullFootText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		fullFootText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		fullFootText.setBorder(new JTable().getBorder());		// match Table border
		JScrollPane fullFootTextScroll = new JScrollPane(fullFootText);
		fullFootTextScroll.setMinimumSize(new Dimension(240, 100));
		fullFootTextScroll.getViewport().setOpaque(false);
		fullFootTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		fullFootText.setCaretPosition(0);	// set scrollbar to top
		templatePanel.add(fullFootTextScroll, "cell 0 1, alignx left");	//$NON-NLS-1$

		JLabel shortFoot = new JLabel(HG0567Msgs.Text_8);		// Short footnote
		templatePanel.add(shortFoot, "cell 0 2, alignx left");	//$NON-NLS-1$
		shortFootText = new JTextArea();
		shortFootText.setWrapStyleWord(true);
		shortFootText.setLineWrap(true);
		shortFootText.setEditable(false);
		shortFootText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		shortFootText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)shortFootText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		shortFootText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		shortFootText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		shortFootText.setBorder(new JTable().getBorder());		// match Table border
		JScrollPane shortFootTextScroll = new JScrollPane(shortFootText);
		shortFootTextScroll.setMinimumSize(new Dimension(240, 100));
		shortFootTextScroll.getViewport().setOpaque(false);
		shortFootTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		shortFootText.setCaretPosition(0);	// set scrollbar to top
		templatePanel.add(shortFootTextScroll, "cell 0 3, alignx left");	//$NON-NLS-1$

		JLabel biblio = new JLabel(HG0567Msgs.Text_9);		// Bibliography
		templatePanel.add(biblio, "cell 0 4, alignx left");	//$NON-NLS-1$
		biblioText = new JTextArea();
		biblioText.setWrapStyleWord(true);
		biblioText.setLineWrap(true);
		biblioText.setEditable(false);
		biblioText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		biblioText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)biblioText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		biblioText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		biblioText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		biblioText.setBorder(new JTable().getBorder());		// match Table border
		JScrollPane biblioTextScroll = new JScrollPane(biblioText);
		biblioTextScroll.setMinimumSize(new Dimension(240, 100));
		biblioTextScroll.getViewport().setOpaque(false);
		biblioTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		biblioText.setCaretPosition(0);	// set scrollbar to top
		templatePanel.add(biblioTextScroll, "cell 0 5, alignx left");	//$NON-NLS-1$

		contents.add(templatePanel, "cell 2 0, aligny top"); //$NON-NLS-1$

	// Define control buttons
		JButton btn_Cancel = new JButton(HG0567Msgs.Text_10);		// Cancel
		btn_Cancel.setEnabled(true);
		contents.add(btn_Cancel, "cell 1 1 2, alignx right, gapx 10, tag cancel"); //$NON-NLS-1$

	// End of Panel Definitions

 	// Get Source Defn data for the current Data language.
		// If only en-US records exist, that is all that will be returned.
		// tableSourceTypeData contains SourceDefnName, PID (only display 1st col)
		try {
			tableSourceTypeData = pointCitationSourceHandler.getSourceDefnList(HGlobal.dataLanguage);
		} catch (HBException hbe) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in HG0567 loading Source Defns: " + hbe.getMessage()); //$NON-NLS-1$
				HB0711Logging.printStackTraceToFile(hbe);
			}
		}
		if (tableSourceTypeData.length == 0 ) {
			JOptionPane.showMessageDialog(scrollTable,
							HG0567Msgs.Text_12,		// No data found in HRE database
							HG0567Msgs.Text_13, 		// Source Definition select error
							JOptionPane.ERROR_MESSAGE);
			dispose();
		}

		// Get the number of entriets in the table into a string (##)
		String count = " (" + String.format("%01d", tableSourceTypeData.length) + ")";	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		// Add this count into the table header string
		tableSourceTypeHead = tableSourceTypeColHeads[0];
		tableSourceTypeColHeads[0] = tableSourceTypeHead + count;

	 	// Setup tablemodel and layout
		srcTableModel = new DefaultTableModel(tableSourceTypeData, tableSourceTypeColHeads);
        tableSourceType.setModel(srcTableModel);
		tableSourceType.getColumnModel().getColumn(0).setMinWidth(50);
		tableSourceType.getColumnModel().getColumn(0).setPreferredWidth(350);
		tableSourceType.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

	// Set the ability to sort on columns
		tableSourceType.setAutoCreateRowSorter(true);
	    TableModel myModel = tableSourceType.getModel();
	    TableRowSorter<TableModel> sorter = new TableRowSorter<>(myModel);
		List <RowSorter.SortKey> psortKeys1 = new ArrayList<>();

	// Presort on column 0
		psortKeys1.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(psortKeys1);
	    tableSourceType.setRowSorter(sorter);

	// Set tooltips and header format
		tableSourceType.getTableHeader().setToolTipText(HG0567Msgs.Text_14);	// Click to sort; Click again to sort in reverse order
		JTableHeader pHeader = tableSourceType.getTableHeader();
		pHeader.setOpaque(false);
		TableCellRenderer prendererFromHeader = tableSourceType.getTableHeader().getDefaultRenderer();
		JLabel pheaderLabel = (JLabel) prendererFromHeader;
		pheaderLabel.setHorizontalAlignment(SwingConstants.CENTER);

	// Set row selection action
		ListSelectionModel rowSelectionModel = tableSourceType.getSelectionModel();
		rowSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

	// Show the table
		scrollTable.setViewportView(tableSourceType);
		tableSourceType.requestFocus();

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

		// Listener for tableSourceType row selection
		tableSourceType.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent selectSrc) {
				if (!selectSrc.getValueIsAdjusting()) {
					if (tableSourceType.getSelectedRow() == -1) return;
				// find source clicked
					int clickedRow = tableSourceType.getSelectedRow();
					int selectedRowInTable = tableSourceType.convertRowIndexToModel(clickedRow);
				// Get the Source Defn templates using the selected sorcDefnPID value
					selectedSourceDefnPID = (long)tableSourceTypeData[selectedRowInTable][1];
					try {
						sorcDefnTemplates = pointCitationSourceHandler
											.getSourceDefnTemplates(selectedSourceDefnPID);
					} catch (HBException hbe) {
						if (HGlobal.writeLogs) {
							HB0711Logging.logWrite("ERROR: in HG0567 loading source templates: " + hbe.getMessage()); //$NON-NLS-1$
							HB0711Logging.printStackTraceToFile(hbe);
						}
					}
				// Get the source template from the Source Defn templates, then convert
				// the Element [nnnnn] entries into Element Names via the hashmap codeToTextMap
				// and load the result into the text areas for display.
				// Start with the Full footer
					fullFootText.setText(pointReportHandler.convertNumsToNames(sorcDefnTemplates[0], codeToTextMap));
				// then the Short footer
					shortFootText.setText(pointReportHandler.convertNumsToNames(sorcDefnTemplates[1], codeToTextMap));
				// then the Bibliography template
					biblioText.setText(pointReportHandler.convertNumsToNames(sorcDefnTemplates[2], codeToTextMap));
				// Set control buttons
					btn_Edit.setEnabled(true);
					btn_Copy.setEnabled(true);
					btn_Delete.setEnabled(true);
				}
			}
        });

		// Listener for tableSourceType mouse double-click
		tableSourceType.addMouseListener(new MouseAdapter() {
			@Override
	        public void mousePressed(MouseEvent me) {
				if (tableSourceType.getSelectedRow() != -1) {
	        		// DOUBLE_CLICK - make it an Edit
		           	if (me.getClickCount() == 2) btn_Edit.doClick();
				}
			}
		});

		// Language combobox - listener for item change
		comboLangListener = new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
				// Get and save the newly selected index
					newSelectedLangIndex = comboLanguage.getSelectedIndex();
					prevSelectedLangIndex = newSelectedLangIndex;
					selectedLangCode = 	HG0501AppSettings.dataReptLangCodes[newSelectedLangIndex];
					try {
						resetSorceDefinTable();
					} catch (HBException hbe) {
						if (HGlobal.writeLogs) {
							HB0711Logging.logWrite("ERROR: in HG0567 resetting language: " + hbe.getMessage()); //$NON-NLS-1$
							HB0711Logging.printStackTraceToFile(hbe);
						}
					}
				}
			}
		};
		comboLanguage.addItemListener(comboLangListener);

		// Listener for Add button
		btn_Add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				editSourcetypeScreen = new HG0568EditSourceType(pointOpenProject, false, false, null_RPID); //<<TEMP NULLRPID!
				editSourcetypeScreen.sourceDefinLanguage = selectedLangCode;
				pointCitationSourceHandler.pointManageSourceType = pointManageSourceType;
				editSourcetypeScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xyType = btn_Add.getLocationOnScreen();
				editSourcetypeScreen.setLocation(xyType.x, xyType.y + 30);
				editSourcetypeScreen.setVisible(true);
			}
		});

		// Listener for Edit button
		btn_Edit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				if (tableSourceType.getSelectedRow() == -1) return;
				editSourcetypeScreen = new HG0568EditSourceType(pointOpenProject, true, false, selectedSourceDefnPID);
				pointCitationSourceHandler.pointManageSourceType = pointManageSourceType;
				editSourcetypeScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xyEdit = btn_Add.getLocationOnScreen();
				editSourcetypeScreen.setLocation(xyEdit.x, xyEdit.y + 30);
				editSourcetypeScreen.setVisible(true);
			}
		});

		// Listener for Copy button
		btn_Copy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				if (tableSourceType.getSelectedRow() == -1) return;
				editSourcetypeScreen = new HG0568EditSourceType(pointOpenProject, false, true, selectedSourceDefnPID);
				pointCitationSourceHandler.pointManageSourceType = pointManageSourceType;
				editSourcetypeScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xyEdit = btn_Add.getLocationOnScreen();
				editSourcetypeScreen.setLocation(xyEdit.x, xyEdit.y + 30);
				editSourcetypeScreen.setVisible(true);
			}

		});

		// Listener for Delete button
		btn_Delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				if (tableSourceType.getSelectedRow() == -1) return;
				try {
					pointCitationSourceHandler.pointManageSourceType = pointManageSourceType;
					pointCitationSourceHandler.deleteSourceDefinition(selectedSourceDefnPID);
				} catch (HBException hbe) {
					if (hbe.getMessage().startsWith("#")) {		//$NON-NLS-1$
				        JOptionPane.showMessageDialog(pointManageSourceType,
				        		HG0567Msgs.Text_15		// Source Definition is used
				        		+ hbe.getMessage()
				        		+ HG0567Msgs.Text_16,		//  times. \nCannot be deleted
				                HG0567Msgs.Text_17,		// Source Definition delete error
				                JOptionPane.ERROR_MESSAGE);
					} else {
						if (HGlobal.writeLogs) {
							HB0711Logging.logWrite("ERROR: in HG0567 on Source Defn delete: " + hbe.getMessage()); //$NON-NLS-1$
							HB0711Logging.printStackTraceToFile(hbe);
						}
					}
				}
			}
		});

		// Listener for Cancel button
		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				if (reminderDisplay != null) reminderDisplay.dispose();
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0567ManageSourceType");	//$NON-NLS-1$
				dispose();
			}
		});

		// Listener for an entry in findText
		findText.getDocument().addDocumentListener(new DocumentListener() {
	          @Override
	          public void insertUpdate(DocumentEvent e) {
	              findTheText();
	          }
	          @Override
	          public void removeUpdate(DocumentEvent e) {
	        	  findTheText();
	          }
	          @Override
	          public void changedUpdate(DocumentEvent e) {
	        	  findTheText();
	          }
	          private void findTheText() {
	        	  String text = findText.getText();
		            for (int row = 0; row <= tableSourceType.getRowCount() - 1; row++) {
	            		String tableValue = (String) tableSourceType.getValueAt(row, 0);
   	                    if (tableValue.toLowerCase().contains(text.toLowerCase())) {
   	                    	tableSourceType.scrollRectToVisible(tableSourceType.getCellRect(row, 0, true));
   	                    // set the 'found' row as selected
   	                    	tableSourceType.changeSelection(row, 0, false, false);
   	                    	return;
   	                    }
	            }
	          }
		});

	}	// End HG0567ManageSourceType constructor

/**
 * public void resetSorceDefinTable()
 * @throws HBException
 */
	public void resetSorceDefinTable() throws HBException {
		tableSourceTypeData = pointCitationSourceHandler.getSourceDefnList(selectedLangCode);
		if(srcTableModel.getRowCount() > 0)  srcTableModel.setRowCount(0);
	// Get the number of entries in the table into a string (##)
		String count = " (" + String.format("%01d", tableSourceTypeData.length) + ")";	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	// Add this count into the table header string
		tableSourceTypeColHeads[0] = tableSourceTypeHead + count;

	 // Setup tablemodel and layout
		srcTableModel.setDataVector(tableSourceTypeData, tableSourceTypeColHeads);
        tableSourceType.setModel(srcTableModel);
		tableSourceType.getColumnModel().getColumn(0).setMinWidth(50);
		tableSourceType.getColumnModel().getColumn(0).setPreferredWidth(350);
		tableSourceType.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

	// Set the ability to sort on columns
		tableSourceType.setAutoCreateRowSorter(true);
	    TableModel myModel = tableSourceType.getModel();
	    TableRowSorter<TableModel> sorter = new TableRowSorter<>(myModel);
		List <RowSorter.SortKey> psortKeys1 = new ArrayList<>();

	// Presort on column 0
		psortKeys1.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(psortKeys1);
	    tableSourceType.setRowSorter(sorter);
	}

}  // End of HG0567ManageSourceType
