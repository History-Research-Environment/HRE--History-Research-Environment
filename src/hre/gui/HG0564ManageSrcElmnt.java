package hre.gui;
/**************************************************************************************
 * ManageSourceElements
 * ***********************************************************************************
 * v0.04.0032 2025-02-07 Original draft (D Ferguson)
 * 			  2025-06-29 Correctly handle Reminder screen display/remove (D Ferguson)
 *			  2025-08-16 Get table header from T204 (D Ferguson)
 *			  2025-09-02 Load Source Elements from database (D Ferguson)
 *			  2025-09-16 Add Find function and Element count in table header (D Ferguson)
 *			  2025-09-20 Add language chooser (D Ferguson)
 *			  2025-12-07 Implemented add new source element (N Tolleshaug)
 *			  2025-12-08 Make new element all CAPS; remove Delete button (D Ferguson)
 *			  2025-12-11 Implemented translate source element name (N Tolleshaug)
 *			  2025-12-14 Implemented edit source element name (N Tolleshaug)
 *			  2025-12-15 Fully remove Save button (D Ferguson)
 *			  2025-12-18 Removed Select button; made new Name checks consistent (D Ferguson)
 *			  2025-12-19 Change Translate button to ask for new language (D Ferguson)
 *			  2025-12-20 NLS all code to this point (D Ferguson)
 *			  2026-01-06 Log catch block msgs (D Ferguson)
 *************************************************************************************
 * Notes for code still outstanding
 * NOTE03 implement a Delete button (complex but may be required)
 *
 ************************************************************************************/

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
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

import hre.bila.HB0711Logging;
import hre.bila.HBCitationSourceHandler;
import hre.bila.HBException;
import hre.bila.HBProjectOpenData;
import hre.gui.HGlobalCode.JTableCellTabbing;
import hre.nls.HG0564Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Manage Source Elements
 * @author D Ferguson
 * @version v0.04.0032
 * @since 2025-02-07
 */

public class HG0564ManageSrcElmnt extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	public HBCitationSourceHandler pointCitationSourceHandler;
	public static final String screenID = "56400"; //$NON-NLS-1$
	HG0564ManageSrcElmnt pointManageSrcElmnt = this;

	private JPanel contents;
	JTable tableSrcElmnt;

	String[] tableSrcElmntColHeads = null;
	String[][] tableSrcElmntData = null;
	String count = "";	//$NON-NLS-1$
	DefaultTableModel srcElmntTableModel = null;
	int selectedRowInTable;
	String sourceElementIdent;
	String sourceElementName;

	JComboBox<String> comboLanguage;
	Object[] languages;
    ItemListener comboLangListener;
	String selectedLangCode = HGlobal.dataLanguage;
	String selectedLanguage;
	int languageIndexDefault, prevSelectedLangIndex, newSelectedLangIndex, elementCount;

	String newElementName = null, editElementName = null, editElementNum = null;

/**
 * Create the dialog
 */
	public HG0564ManageSrcElmnt(HBProjectOpenData pointOpenProject) throws HBException  {
		this.pointOpenProject = pointOpenProject;
		//pointPersonHandler = pointOpenProject.getPersonHandler();
		pointCitationSourceHandler = pointOpenProject.getCitationSourceHandler();

		setTitle(HG0564Msgs.Text_0);	// Manage Source Elements
	// Setup references for HG0450
		windowID = screenID;
		helpName = "managesourceelement";		 //$NON-NLS-1$

	// Setup close and logging actions
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0564ManageSrcElmnt");	//$NON-NLS-1$

	// Collect static GUI text from T204 for Source Element table
		tableSrcElmntColHeads = pointCitationSourceHandler.setTranslatedData("56400", "1", false);	// Source Elements //$NON-NLS-1$ //$NON-NLS-2$

	// Setup dialog
		setResizable(false);
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 5", "[]10[]", "[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

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
		actionPanel.setLayout(new MigLayout("insets 10", "[]", "[]5[]20[]10[]10[]5[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_Language = new JLabel(HG0564Msgs.Text_1);		// Language:
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

		JButton btn_Add = new JButton(HG0564Msgs.Text_2);		// Add
		btn_Add.setEnabled(true);
		actionPanel.add(btn_Add, "cell 0 2, alignx center, grow"); //$NON-NLS-1$
		JButton btn_Translate = new JButton(HG0564Msgs.Text_3);		// Translation
		btn_Translate.setEnabled(false);
		actionPanel.add(btn_Translate, "cell 0 3, alignx center, grow"); //$NON-NLS-1$
		JLabel lbl_Find = new JLabel(HG0564Msgs.Text_4);		// Find:
		actionPanel.add(lbl_Find, "cell 0 4, alignx center"); //$NON-NLS-1$
		JTextField findText = new JTextField();
		findText.setColumns(10);
		actionPanel.add(findText, "cell 0 5, alignx left"); //$NON-NLS-1$

		contents.add(actionPanel, "cell 0 0, aligny top"); //$NON-NLS-1$

	// Define panel for Source Element list
		JPanel SrcElmntPanel = new JPanel();
		SrcElmntPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		SrcElmntPanel.setLayout(new MigLayout("insets 10", "[]", "[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	// Setup JTable to show Source Element data
		tableSrcElmnt = new JTable() {
			private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int row, int column) {
					return true;
			}};
	 	// Get Source Element data for current HRE dataLanguage
			try {
				tableSrcElmntData = pointCitationSourceHandler.getSourceElmntList(HGlobal.dataLanguage);
			} catch (HBException hbe) {
				if (HGlobal.writeLogs) {
					HB0711Logging.logWrite("ERROR: in HG0564 loading Source Elements: " + hbe.getMessage()); //$NON-NLS-1$
					HB0711Logging.printStackTraceToFile(hbe);
				}
			}
		if (tableSrcElmntData.length == 0 ) {
			JOptionPane.showMessageDialog(tableSrcElmnt, HG0564Msgs.Text_5	// No data found in the HRE database\n
													 + HG0564Msgs.Text_6,		// for thecurrent Data Language
													   HG0564Msgs.Text_7, 						// Source Elements
													   JOptionPane.ERROR_MESSAGE);
			dispose();
		}

		// Get the number of Elements in the table into a string (##)
		count = " (" + String.format("%01d", tableSrcElmntData.length) + ")";	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		// Add this count into the table header string
		tableSrcElmntColHeads[0] = tableSrcElmntColHeads[0] + count;

	 	// Setup tablemodel amd table layout
		// NB: table is defined as 2D and holds Element Name, Number, but
		// we are only using a single column for the Source Element's name
		srcElmntTableModel = new DefaultTableModel(tableSrcElmntData, tableSrcElmntColHeads);
        tableSrcElmnt.setModel(srcElmntTableModel);
		tableSrcElmnt.getColumnModel().getColumn(0).setMinWidth(100);
		tableSrcElmnt.getColumnModel().getColumn(0).setPreferredWidth(240);
		tableSrcElmnt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		// Set the ability to sort on columns
		tableSrcElmnt.setAutoCreateRowSorter(true);
	    TableRowSorter<TableModel> sorter = new TableRowSorter<>(srcElmntTableModel);
		List <RowSorter.SortKey> psortKeys1 = new ArrayList<>();
		// Presort on column 0
		psortKeys1.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(psortKeys1);
	    tableSrcElmnt.setRowSorter(sorter);
	    // Set tooltips and header format
		tableSrcElmnt.getTableHeader().setToolTipText(HG0564Msgs.Text_8);	// Click to sort; Click again to sort in reverse order
		JTableHeader pHeader = tableSrcElmnt.getTableHeader();
		pHeader.setOpaque(false);
		TableCellRenderer prendererFromHeader = tableSrcElmnt.getTableHeader().getDefaultRenderer();
		JLabel pheaderLabel = (JLabel) prendererFromHeader;
		pheaderLabel.setHorizontalAlignment(SwingConstants.CENTER);

		// Set row selection action
		ListSelectionModel rowSelectionModel = tableSrcElmnt.getSelectionModel();
		rowSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		tableSrcElmnt.setMaximumSize(new Dimension(32767, 32767));
		tableSrcElmnt.setFillsViewportHeight(true);
		// Setup tabbing within table against all rows but only column 0
		if (tableSrcElmnt.getRowCount() > 0)
					JTableCellTabbing.setTabMapping(tableSrcElmnt, 0, tableSrcElmnt.getRowCount(), 0, 1);
		// scrollPane contains the Source Type picklist
		JScrollPane scrollTable = new JScrollPane();
		scrollTable.setPreferredSize(new Dimension(260, 300));
		// Show the table
		scrollTable.setViewportView(tableSrcElmnt);
		tableSrcElmnt.requestFocus();
		SrcElmntPanel.add(scrollTable, "cell 0 0"); //$NON-NLS-1$

		contents.add(SrcElmntPanel, "cell 1 0, grow"); //$NON-NLS-1$

	// Define control buttons
		JButton btn_Close = new JButton(HG0564Msgs.Text_9);		// Close
		btn_Close.setEnabled(true);
		contents.add(btn_Close, "cell 1 1, alignx right, gapx 10, tag cancel"); //$NON-NLS-1$

	// End of Panel Definitions
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

		// Listener for tableSrcElmnt row selection
		tableSrcElmnt.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent selectSrc) {
				if (!selectSrc.getValueIsAdjusting()) {
					if (tableSrcElmnt.getSelectedRow() == -1) return;
					selectedRowInTable = tableSrcElmnt.convertRowIndexToModel(tableSrcElmnt.getSelectedRow());
					sourceElementName = tableSrcElmntData[selectedRowInTable][0];
					sourceElementIdent = tableSrcElmntData[selectedRowInTable][1];
					btn_Translate.setEnabled(true);
				}
			}
        });

// Listener for element table double-click selection
		tableSrcElmnt.addMouseListener(new MouseAdapter() {
			@Override
	        public void mousePressed(MouseEvent me) {
				if (tableSrcElmnt.getSelectedRow() != -1) {
				selectedRowInTable = tableSrcElmnt.convertRowIndexToModel(tableSrcElmnt.getSelectedRow());
	        		// DOUBLE_CLICK - allow user to edit the Element Name
		           	if (me.getClickCount() == 2) {
		           	// Strip the brackets off the element name ready for user edit
		           		String elmntName = tableSrcElmntData[selectedRowInTable][0];
		           		String noBracketName = elmntName.substring(1, elmntName.length() - 1);
		           	// Get user edited name
		           		editElementName = JOptionPane.showInputDialog(pointManageSrcElmnt,
								HG0564Msgs.Text_10,	// Enter new Source Element name
								noBracketName );
		                // Check the return value
		                if (editElementName == null) {
		             // User clicked "Cancel" or closed the dialog
		                    return;
		                }
						if (editElementName == null || editElementName.isEmpty()) {
		             // User clicked "OK" with no text entered
		                    JOptionPane.showMessageDialog(pointManageSrcElmnt, HG0564Msgs.Text_11);	// No data entered
		                    return;
		                }
						// User clicked "OK" with input
						   		editElementName = "[" + editElementName.trim().toUpperCase() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
						   		editElementNum = tableSrcElmntData[selectedRowInTable][1];
						   		// Check length of edited name
						   		if (editElementName.length() > 40) {
						   			JOptionPane.showMessageDialog(pointManageSrcElmnt,
											editElementName + HG0564Msgs.Text_12,	//  is too long
											HG0564Msgs.Text_13,				// Element Name error
											JOptionPane.ERROR_MESSAGE);
						   			return;
						   		}
						   // if OK, save the edited name
						   		try {
									pointCitationSourceHandler.updateSourceElementData (editElementName,
											editElementNum,
											selectedLangCode,
											pointManageSrcElmnt);
								} catch (HBException hbe) {
									if (HGlobal.writeLogs) {
										HB0711Logging.logWrite("ERROR: in HG0564 saving Source Element: " + hbe.getMessage()); //$NON-NLS-1$
										HB0711Logging.printStackTraceToFile(hbe);
									}
								}
		           	}
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
					resetElementList();
				}
			}
		};
		comboLanguage.addItemListener(comboLangListener);

		// Listener for Add button
		btn_Add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actEvent) {
				newElementName = JOptionPane.showInputDialog(pointManageSrcElmnt,
										HG0564Msgs.Text_10);		// Enter new Source Element name
				if (newElementName == null || newElementName.isEmpty()) {
		        // User clicked "OK" with no text entered
		                    JOptionPane.showMessageDialog(pointManageSrcElmnt, HG0564Msgs.Text_11);	// No data entered
		                    return;
		                }
		   		// Check length of name
		   		if (newElementName.length() > 38) {
		   			JOptionPane.showMessageDialog(pointManageSrcElmnt,
		   					newElementName + HG0564Msgs.Text_12,	//  is too long
							HG0564Msgs.Text_13,				// Element Name error
							JOptionPane.ERROR_MESSAGE);
		   			return;
		   		}

				if (newElementName != null) {
					newElementName = newElementName.trim().toUpperCase();
					try {
						pointCitationSourceHandler.
						createNewSourceElementType(newElementName, selectedLangCode, pointManageSrcElmnt);
					} catch (HBException hbe) {
						if (hbe.getMessage().startsWith("*"))	//$NON-NLS-1$
							JOptionPane.showMessageDialog(pointManageSrcElmnt,
									newElementName + HG0564Msgs.Text_18,	// already exists
									HG0564Msgs.Text_19,			// Duplicate Element Name
									JOptionPane.ERROR_MESSAGE);
						else {
							if (HGlobal.writeLogs) {
								HB0711Logging.logWrite("ERROR: in HG0564 adding Source Element: " + hbe.getMessage()); //$NON-NLS-1$
								HB0711Logging.printStackTraceToFile(hbe);
							}
						}
					}
				}
			}
		});

		// Listener for Add Translation button
		btn_Translate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actEvent) {
				// Create tanslation of selected element in another data language
				// Ask for new language to be selected
		        // Show langauges[] as a list in JoptionPane for user selection
		        selectedLanguage = (String)JOptionPane.showInputDialog(
			        	pointManageSrcElmnt,
			            HG0564Msgs.Text_20,
			            HG0564Msgs.Text_21,
			            JOptionPane.PLAIN_MESSAGE,
			            null,
			            languages,
			            languages[comboLanguage.getSelectedIndex()]		// current language setting
			        );
				// Check result - end dialog is user Canceled
		        if (selectedLanguage == null) return;
		        // Lookup langauge and get its index in the language list
				for (int i=0; i < languageCount; i++) {
					if (selectedLanguage == HG0501AppSettings.dataReptLanguages[i])
						newSelectedLangIndex = i;
				}
				// Get the element code
				selectedLangCode = 	HG0501AppSettings.dataReptLangCodes[newSelectedLangIndex];
				// Check if translation already exists - if not, ask for translated name
				try {
					if (pointCitationSourceHandler.
							testSourceElementTranslationExist (sourceElementIdent, selectedLangCode)) {
						newElementName = JOptionPane.showInputDialog(pointManageSrcElmnt,
								HG0564Msgs.Text_22	// Enter Element name in
								+ selectedLanguage);
						if (newElementName != null) {
							newElementName = newElementName.trim().toUpperCase();
							newElementName = "[" + newElementName + "]"; //$NON-NLS-1$ //$NON-NLS-2$
							pointCitationSourceHandler.updateSourceElementTranslation(newElementName,
									sourceElementIdent, selectedLangCode, pointManageSrcElmnt);
						}

					} else 	JOptionPane.showMessageDialog(pointManageSrcElmnt,
							HG0564Msgs.Text_23 +						// A translation of
							sourceElementName + HG0564Msgs.Text_24	//\n already exists in
							+ selectedLanguage,
							HG0564Msgs.Text_25,		// Existing Element Name
							JOptionPane.ERROR_MESSAGE);

				} catch (HBException hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0564 adding Element in new language: " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
			}
		});

		// Listener for Close button
		btn_Close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				if (reminderDisplay != null) reminderDisplay.dispose();
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0564ManageSrcElmnt");	//$NON-NLS-1$
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
		            for (int row = 0; row <= tableSrcElmnt.getRowCount() - 1; row++) {
	            		String tableValue = (String) tableSrcElmnt.getValueAt(row, 0);
   	                    if (tableValue.toLowerCase().contains(text.toLowerCase())) {
   	                    	tableSrcElmnt.scrollRectToVisible(tableSrcElmnt.getCellRect(row, 0, true));
   	                    // set the 'found' row as selected
   	                    	tableSrcElmnt.changeSelection(row, 0, false, false);
   	                    	return;
   	                    }
	            }
	          }
		});

	}	// End HG0564ManageSrcElmnt constructor

/**
 * 	public void resetEelementList()
 */
	public void resetElementList() {
		try {
			srcElmntTableModel.setRowCount(0);
		// Get Element list for new language and reload table
			//String[][] tableSrcElmntData = null;
			tableSrcElmntData = pointCitationSourceHandler.getSourceElmntList(selectedLangCode);
		// Revise current element count
			count = " (" + String.format("%01d", tableSrcElmntData.length) + ")";	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		// Change table header for new count and reset tablemodel
			int indexOfBracket = tableSrcElmntColHeads[0].indexOf("(");		//$NON-NLS-1$
			tableSrcElmntColHeads[0] = tableSrcElmntColHeads[0].substring(0, indexOfBracket) + count;
			srcElmntTableModel.setDataVector(tableSrcElmntData, tableSrcElmntColHeads);
			tableSrcElmnt.getColumnModel().getColumn(0).setMinWidth(100);
			tableSrcElmnt.getColumnModel().getColumn(0).setPreferredWidth(240);
		    TableRowSorter<TableModel> sorter = new TableRowSorter<>(srcElmntTableModel);
			List <RowSorter.SortKey> psortKeys1 = new ArrayList<>();
			psortKeys1.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
			sorter.setSortKeys(psortKeys1);
		    tableSrcElmnt.setRowSorter(sorter);
		} catch (HBException hbe) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in HG0564 resetting Element list: " + hbe.getMessage()); //$NON-NLS-1$
				HB0711Logging.printStackTraceToFile(hbe);
			}
		}
	}		// End resetElementList

}  // End of HG0564ManageSrcElmnt
