package hre.gui;
/**************************************************************************************
 * ManageSource -
 * ***********************************************************************************
 * v0.04.0032 2025-01-12 Original draft (D Ferguson)
 * 			  2025-01-17 Setup Add button to drive HG0567ManageSourceType (D Ferguson)
 *			  2025-01-31 Add Delete button (D Ferguson)
 *			  2025-02-04 Add Show Inactive button (D Ferguson)
 *			  2025-02-14 Activated Source list table (N. Tolleshaug)
 *			  2025-02-18 Add Save button (D Ferguson)
 *			  2025-03-01 Make Select button dispose this screen (D Ferguson)
 *			  2025-03-22 Handling add citation if source table empty (N. Tolleshaug)
 *			  2025-05-26 Adjust miglayout settings (D Ferguson)
 * 			  2025-06-29 Correctly handle Reminder screen display/remove (D Ferguson)
 *			  2025-08-16 Get table header from T204 (D Ferguson)
 *			  2025-09-16 Add Source count into table header (D Ferguson)
 *			  2025-09-26 Load Source templates and convert to Element Names (D Ferguson)
 *			  2025-09-27 Invoke HG0566 to perform Source edits (D Ferguson)
 *			  2025-10-07 Initiation Add and Edit source buttons (N. Tolleshaug)
 * 			  2025-11-17 Change HGlobalCode routines to be in ReportHandler (D Ferguson)
 *			  2025-11-24 Fix crash when action taken with nothing seelcted (D Ferguson)
 *						 Do Edit if Source table double-clicked; remove Save btn (D Ferguson)
 *
 *************************************************************************************
 * Notes for incomplete code still requiring attention
 * NOTE03 implement Copy button
 * NOTE06 implement Show Inactive button
 ************************************************************************************/

import java.awt.Component;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JButton;
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
import hre.bila.HBReportHandler;
import hre.gui.HGlobalCode.JTableCellTabbing;
import net.miginfocom.swing.MigLayout;

/**
 * Manage Source
 * @author D Ferguson
 * @version v0.04.0032
 * @since 2025-01-12
 */

public class HG0565ManageSource extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	public static final String screenID = "56500"; //$NON-NLS-1$
	public HBCitationSourceHandler pointCitationSourceHandler;
	public HG0555EditCitation pointEditCitation;
	HG0565ManageSource pointThis = this;
	HBPersonHandler pointPersonHandler;
	HBReportHandler pointReportHandler;

	private JPanel contents;

	String[] tableSourceColHeads = null;
	Object[][] tableSourceData;
	DefaultTableModel srcTableModel = null;
	JTable tableSource;

	JTextArea fullFootText, shortFootText, biblioText;
	String templateText = "";	//$NON-NLS-1$
	String[] sorcDefnTemplates;
	String[][] tableSrcElmntData;
	long selectedSourcePID, selectedSorcDefnPID;
	int clickedRow, selectedRowInTable;

/**
 * Create the dialog
 */
	public HG0565ManageSource(HBProjectOpenData pointOpenProject, HG0555EditCitation pointEditCitation)  {
		this.pointOpenProject = pointOpenProject;
		this.pointEditCitation = pointEditCitation;
		pointPersonHandler = pointOpenProject.getPersonHandler();
		pointReportHandler = pointOpenProject.getReportHandler();

		setTitle("Manage Sources");
	// Setup references for HG0450
		windowID = screenID;
		helpName = "managesource";		 //$NON-NLS-1$
		pointCitationSourceHandler = pointOpenProject.getCitationSourceHandler();

	// Setup close and logging actions
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0565ManageSource");	//$NON-NLS-1$

	// Collect static GUI text from T204 for Source table
		tableSourceColHeads = pointPersonHandler.setTranslatedData("56500", "1", false); // ID, Source, Cited  //$NON-NLS-1$ //$NON-NLS-2$

	// Load the Source Element list (names/ID#s) as we need it for Source template conversion & checking
		try {
			tableSrcElmntData = pointCitationSourceHandler.getSourceElmntList(HGlobal.dataLanguage);
		} catch (HBException hbe) {
			System.out.println( " Error loading Source Element list: " + hbe.getMessage());
			hbe.printStackTrace();
		}

	    // Then construct a lookup fo: "12345" → "[element text]" conversion
        Map<String, String> codeToTextMap = new HashMap<>();
        for (String[] row : tableSrcElmntData) {
            if (row.length >= 2)
                codeToTextMap.put(row[1], row[0].trim());
        }

	// For Text area font setting
	    Font font = UIManager.getFont("TextArea.font");		//$NON-NLS-1$

	// Setup dialog
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 5", "[]10[]10[]", "[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

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
		actionPanel.setLayout(new MigLayout("insets 5", "[]", "[]10[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		JButton btn_Add = new JButton("Add");		// Add
		btn_Add.setEnabled(true);
		actionPanel.add(btn_Add, "cell 0 0, alignx center, grow"); //$NON-NLS-1$
		JButton btn_Edit = new JButton("Edit");		// Edit
		btn_Edit.setEnabled(false);
		actionPanel.add(btn_Edit, "cell 0 1, alignx center, grow"); //$NON-NLS-1$
		JButton btn_Copy = new JButton("Copy");		// Copy
		btn_Copy.setEnabled(false);
		actionPanel.add(btn_Copy, "cell 0 2, alignx center, grow"); //$NON-NLS-1$
		JButton btn_Delete = new JButton("Delete");		// Delete
		btn_Delete.setEnabled(false);
		actionPanel.add(btn_Delete, "cell 0 3, alignx center, grow"); //$NON-NLS-1$
		JButton btn_Inactive = new JButton("Show Inactive");		// Show Inactive
		btn_Inactive.setEnabled(true);
		actionPanel.add(btn_Inactive, "cell 0 4, alignx center, grow"); //$NON-NLS-1$
		JLabel find = new JLabel("Find:");		// Find:
		actionPanel.add(find, "cell 0 5, alignx center"); //$NON-NLS-1$
		JTextField findText = new JTextField();
		findText.setColumns(10);
		actionPanel.add(findText, "cell 0 6, alignx left"); //$NON-NLS-1$

		contents.add(actionPanel, "cell 0 0, aligny top"); //$NON-NLS-1$

	// Define panel for Source list
		JPanel sourcePanel = new JPanel();
		sourcePanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		sourcePanel.setLayout(new MigLayout("insets 10", "[]", "[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	// Setup JTable to show Source data list
		tableSource = new JTable() {
			private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
			}};
	 	// Get Source data list. Table Data consists of:
		// Source ref#, Source Abbrev, #times Cited  (these 3 items displayed)
		// and SourcePID, Fidelity, Source Defn PID, Footnote text, Shortnote text, Biblio text
		try {
			tableSourceData = pointCitationSourceHandler.getSourceList();
		} catch (HBException hbe) {
			System.out.println( " Error loading source list: " + hbe.getMessage());
			hbe.printStackTrace();
		}

		if (tableSourceData == null ) {
			JOptionPane.showMessageDialog(tableSource, "No sources found in HRE database\n"	// No data found in HRE database\n
													 + "Source Select error",		// Source Select error
													   "Source Select", 			// Source Select
													   JOptionPane.ERROR_MESSAGE);
		}

	// Get the number of items in the table into a string (##)
		String count = " (" + String.format("%02d", tableSourceData.length) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	// Add this count into the table header string
		tableSourceColHeads[1] = tableSourceColHeads[1] + count;


	 // Setup tableSource, model and renderer
		if (tableSourceData != null) {
			srcTableModel = new DefaultTableModel(
	    		tableSourceData, tableSourceColHeads) {
					private static final long serialVersionUID = 1L;
					@SuppressWarnings({ "unchecked", "rawtypes" })
					@Override
					public Class getColumnClass(int column) {
							return getValueAt(0, column).getClass();
				}
			};
	        tableSource.setModel(srcTableModel);
			tableSource.getColumnModel().getColumn(0).setMinWidth(50);
			tableSource.getColumnModel().getColumn(0).setPreferredWidth(50);
			tableSource.getColumnModel().getColumn(1).setMinWidth(100);
			tableSource.getColumnModel().getColumn(1).setPreferredWidth(270);
			tableSource.getColumnModel().getColumn(2).setMinWidth(50);
			tableSource.getColumnModel().getColumn(2).setPreferredWidth(50);
			//tableSource.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			DefaultTableCellRenderer centerLabelRenderer = new DefaultTableCellRenderer();
			centerLabelRenderer.setHorizontalAlignment(JLabel.CENTER);
			tableSource.getColumnModel().getColumn(0).setCellRenderer(centerLabelRenderer);
			tableSource.getColumnModel().getColumn(2).setCellRenderer(centerLabelRenderer);
			// Set the ability to sort on columns
			tableSource.setAutoCreateRowSorter(true);
		    TableModel myModel = tableSource.getModel();
		    TableRowSorter<TableModel> sorter = new TableRowSorter<>(myModel);
			List <RowSorter.SortKey> psortKeys1 = new ArrayList<>();
			// Presort on column 1
			psortKeys1.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
			sorter.setSortKeys(psortKeys1);
		    tableSource.setRowSorter(sorter);
		    // Set tooltips and header format
			tableSource.getTableHeader().setToolTipText("Click to sort; Click again to sort in reverse order");	// Click to sort; Click again to sort in reverse order
			JTableHeader pHeader = tableSource.getTableHeader();
			pHeader.setOpaque(false);
			TableCellRenderer prendererFromHeader = tableSource.getTableHeader().getDefaultRenderer();
			JLabel pheaderLabel = (JLabel) prendererFromHeader;
			pheaderLabel.setHorizontalAlignment(SwingConstants.CENTER);
			// Set row selection action
			ListSelectionModel rowSelectionModel = tableSource.getSelectionModel();
			rowSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			tableSource.setMaximumSize(new Dimension(32767, 32767));
			tableSource.setFillsViewportHeight(true);
		}
		// Setup tabbing within table against all rows, column 0-2
		if (tableSource.getRowCount() > 0)
					JTableCellTabbing.setTabMapping(tableSource, 0, tableSource.getRowCount(), 0, 2);
		// scrollPane contains the Source picklist
		JScrollPane scrollTable = new JScrollPane();
		scrollTable.setPreferredSize(new Dimension(390, 380));
		scrollTable.setViewportView(tableSource);
		sourcePanel.add(scrollTable, "cell 0 0"); //$NON-NLS-1$
		contents.add(sourcePanel, "cell 1 0, grow"); //$NON-NLS-1$

	// Define panel for Source templates
		JPanel templatePanel = new JPanel();
		templatePanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		templatePanel.setLayout(new MigLayout("insets 5", "[]", "5[]5[]10[]5[]10[]5[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel fullFoot = new JLabel("Full footnote");
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
		fullFootTextScroll.setPreferredSize(new Dimension(240, 100));
		fullFootTextScroll.getViewport().setOpaque(false);
		fullFootTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		fullFootText.setCaretPosition(0);	// set scrollbar to top
		templatePanel.add(fullFootTextScroll, "cell 0 1, alignx left");	//$NON-NLS-1$

		JLabel shortFoot = new JLabel("Short footnote");
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
		shortFootTextScroll.setPreferredSize(new Dimension(240, 100));
		shortFootTextScroll.getViewport().setOpaque(false);
		shortFootTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		shortFootText.setCaretPosition(0);	// set scrollbar to top
		templatePanel.add(shortFootTextScroll, "cell 0 3, alignx left");	//$NON-NLS-1$

		JLabel biblio = new JLabel("Bibliography");
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
		biblioTextScroll.setPreferredSize(new Dimension(240, 100));
		biblioTextScroll.getViewport().setOpaque(false);
		biblioTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		biblioText.setCaretPosition(0);	// set scrollbar to top
		templatePanel.add(biblioTextScroll, "cell 0 5, alignx left");	//$NON-NLS-1$

		contents.add(templatePanel, "cell 2 0, aligny top"); //$NON-NLS-1$

	// Define control buttons
		JButton btn_Close = new JButton("Close");		// Close
		btn_Close.setEnabled(true);
		contents.add(btn_Close, "cell 1 1 2, alignx right, gapx 10, tag cancel"); //$NON-NLS-1$
		JButton btn_Select = new JButton("Select");		// Select
		btn_Select.setEnabled(false);
		contents.add(btn_Select, "cell 1 1 2, alignx right, gapx 10, tag yes"); //$NON-NLS-1$

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
		    	btn_Close.doClick();
			}
		});

		// Listener for Close button
		btn_Close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
			// Dispose EditCitation, Reminder if not null
				if (!(pointEditCitation == null)) pointEditCitation.dispose();
				if (reminderDisplay != null) reminderDisplay.dispose();
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0565ManageSource");	//$NON-NLS-1$
				dispose();
			}
		});

		// Listener for Source table mouse click - load Source templates and selected Source's PID; do Edit if double-click
		tableSource.addMouseListener(new MouseAdapter() {
			@Override
	        public void mousePressed(MouseEvent me) {
				if (tableSource.getSelectedRow() != -1) {
				// find source that was clicked
					clickedRow = tableSource.getSelectedRow();
					selectedRowInTable = tableSource.convertRowIndexToModel(clickedRow);
					selectedSourcePID = (long)tableSourceData[selectedRowInTable][3];
				// Get the underlying Source Defn templates using the sorcDefnPID value
					selectedSorcDefnPID = (long)tableSourceData[selectedRowInTable][5];
					try {
						sorcDefnTemplates = pointCitationSourceHandler
											.getSourceDefnTemplates(selectedSorcDefnPID);
					} catch (HBException hbe) {
						System.out.println( " Error loading source templates: " + hbe.getMessage());
						hbe.printStackTrace();
					}
				// Try the Source's template; if empty, get the template from the Source Defn templates,
				// then convert the Element [nnnnn] entries into Element Names using the Hashmap codeToTextMap
				// and load result into the text areas for display.
				// Start with the Full footer
					templateText = (String)tableSourceData[selectedRowInTable][6];		// Source full footer
					if (templateText.isEmpty()) templateText = sorcDefnTemplates[0];	// Source Defn full footer
					fullFootText.setText(pointReportHandler.convertNumsToNames(templateText, codeToTextMap));
				// then the Short footer
					templateText = (String)tableSourceData[selectedRowInTable][7];		// Source short footer
					if (templateText.isEmpty()) templateText = sorcDefnTemplates[1];	// Source Defn short footer
					shortFootText.setText(pointReportHandler.convertNumsToNames(templateText, codeToTextMap));
				// then the Bibliography template
					templateText = (String)tableSourceData[selectedRowInTable][8];		// Source bibliography
					if (templateText.isEmpty()) templateText = sorcDefnTemplates[2];	// Source Defn bibliography
					biblioText.setText(pointReportHandler.convertNumsToNames(templateText, codeToTextMap));
				// Set control buttons
					btn_Edit.setEnabled(true);
					btn_Copy.setEnabled(true);
					if (tableSourceData != null) btn_Select.setEnabled(true);
					btn_Delete.setEnabled(true);
				}
        	// If DOUBLE_CLICK - make it an Edit
	           	if (me.getClickCount() == 2) {
	           		btn_Edit.doClick();
	           	}
			}
		});

		// Listener for Add button - invoke HG0566AddSource to a new source
		btn_Add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				HG0566AddSource sourceAddScreen = new HG0566AddSource(pointOpenProject, pointThis);
				sourceAddScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xySourceT = btn_Add.getLocationOnScreen();
				sourceAddScreen.setLocation(xySourceT.x, xySourceT.y + 30);
				sourceAddScreen.setVisible(true);
			}
		});

		// Listener for Edit(update) button to Edit this Source
		btn_Edit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				// Check if no row selected (like after a Delete!)
				if (tableSource.getSelectedRow() == -1) return;
				HG0566UpdateSource updateScreen = new HG0566UpdateSource(pointOpenProject, selectedSourcePID, pointThis);
				updateScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xyEdit = btn_Add.getLocationOnScreen();
				updateScreen.setLocation(xyEdit.x, xyEdit.y + 30);
				updateScreen.setVisible(true);
			}
		});

		// Listener for Copy button
		btn_Copy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				// NOTE03 does nothing yet

			}
		});

		// Listener for Delete button
		btn_Delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				// Check if no row selected (like after another Delete!)
				if (tableSource.getSelectedRow() == -1) return;
				try {
					selectedRowInTable = tableSource.convertRowIndexToModel(tableSource.getSelectedRow());
					int numberCiteded = (int) tableSourceData[selectedRowInTable][2];
			// DO NOT DELETE ANY SOURCE THAT IS IN USE!
					if (numberCiteded  == 0) {
						pointCitationSourceHandler.deleteSourceRecord(selectedSourcePID);
						resetSourceTable();
						tableSource.getSelectionModel().clearSelection(); // clear selectedRow setting
						// Disable buttons (as nothing is selected at this stage)
						btn_Edit.setEnabled(false);
						btn_Copy.setEnabled(false);
						btn_Delete.setEnabled(false);
					} else {
						String message = " Cannot delete as \nSource: " + tableSourceData[selectedRowInTable][1]
								+ " is used " + numberCiteded + " times.";
						JOptionPane.showMessageDialog(contents, message,
								"Delete Source", JOptionPane.INFORMATION_MESSAGE);
					}
				} catch (HBException hbe) {
					System.out.println(" Delete Source error: " + hbe.getMessage());
					hbe.printStackTrace();
				}
			}
		});

		// Listener for Inactive button
		btn_Inactive.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				// NOTE06 at this point, reload the Source table with all T736 records where IS_ACTIVE=no
			}
		});

		// Listener for Select button
		btn_Select.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				// Check if no row selected (like after a Delete!)
				if (tableSource.getSelectedRow() == -1) return;
				Object objSourceDataToEdit[] = new Object[2]; // to hold data to pass to Citation editor
				int clickedRow = tableSource.getSelectedRow();
				int selectedRowInTable = tableSource.convertRowIndexToModel(clickedRow);
           		objSourceDataToEdit = tableSourceData[selectedRowInTable]; // select whole row
           		if (pointEditCitation != null)
           			pointEditCitation.setSourceSelectedData(objSourceDataToEdit);
				if (reminderDisplay != null) reminderDisplay.dispose();
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
		            for (int row = 0; row <= tableSource.getRowCount() - 1; row++) {
	            		String tableValue = (String) tableSource.getValueAt(row, 1);
   	                    if (tableValue.toLowerCase().contains(text.toLowerCase())) {
   	                    	tableSource.scrollRectToVisible(tableSource.getCellRect(row, 1, true));
   	                    // set the 'found' row as selected
   	                    	tableSource.changeSelection(row, 0, false, false);
   	                    	return;
   	                    }
	            }
	          }
		});

	}	// End HG0565ManageSource constructor

/**
 * private void resetSourceTable() throws HBException
 * @throws HBException
 */
	public void resetSourceTable() throws HBException {
		tableSourceData = pointCitationSourceHandler.getSourceList();
		srcTableModel.setDataVector(tableSourceData, tableSourceColHeads);
        tableSource.setModel(srcTableModel);
		tableSource.getColumnModel().getColumn(0).setMinWidth(50);
		tableSource.getColumnModel().getColumn(0).setPreferredWidth(50);
		tableSource.getColumnModel().getColumn(1).setMinWidth(100);
		tableSource.getColumnModel().getColumn(1).setPreferredWidth(270);
		tableSource.getColumnModel().getColumn(2).setMinWidth(50);
		tableSource.getColumnModel().getColumn(2).setPreferredWidth(50);
		//tableSource.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		DefaultTableCellRenderer centerLabelRenderer = new DefaultTableCellRenderer();
		centerLabelRenderer.setHorizontalAlignment(JLabel.CENTER);
		tableSource.getColumnModel().getColumn(0).setCellRenderer(centerLabelRenderer);
		tableSource.getColumnModel().getColumn(2).setCellRenderer(centerLabelRenderer);
		// Set the ability to sort on columns
		tableSource.setAutoCreateRowSorter(true);
	    TableModel myModel = tableSource.getModel();
	    TableRowSorter<TableModel> sorter = new TableRowSorter<>(myModel);
		List <RowSorter.SortKey> psortKeys1 = new ArrayList<>();
		// Presort on column 1
		psortKeys1.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
		sorter.setSortKeys(psortKeys1);
	    tableSource.setRowSorter(sorter);
	    // Set tooltips and header format
		tableSource.getTableHeader().setToolTipText("Click to sort; Click again to sort in reverse order");	// Click to sort; Click again to sort in reverse order
		JTableHeader pHeader = tableSource.getTableHeader();
		pHeader.setOpaque(false);
		TableCellRenderer prendererFromHeader = tableSource.getTableHeader().getDefaultRenderer();
		JLabel pheaderLabel = (JLabel) prendererFromHeader;
		pheaderLabel.setHorizontalAlignment(SwingConstants.CENTER);
		// Set row selection action
		ListSelectionModel rowSelectionModel = tableSource.getSelectionModel();
		rowSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		tableSource.setMaximumSize(new Dimension(32767, 32767));
		tableSource.setFillsViewportHeight(true);
	}

}  // End of HG0565ManageSource
