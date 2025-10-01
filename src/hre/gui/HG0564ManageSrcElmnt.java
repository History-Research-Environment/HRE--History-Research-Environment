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
 *************************************************************************************
 * Notes for incomplete code still requiring attention
 * NOTE02 implement Add button
 * NOTE03 implement Delete button
 *
 ************************************************************************************/

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.gui.HGlobalCode.JTableCellTabbing;
import net.miginfocom.swing.MigLayout;

/**
 * Manage Source Elements
 * @author D Ferguson
 * @version v0.04.0032
 * @since 2025-02-07
 */

public class HG0564ManageSrcElmnt extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	HBPersonHandler pointPersonHandler;
	public HBCitationSourceHandler pointCitationSourceHandler;
	public static final String screenID = "56400"; //$NON-NLS-1$

	private JPanel contents;

	String[] tableSrcElmntColHeads = null;
	String[][] tableSrcElmntData = null;
	String count = "";	//$NON-NLS-1$
	DefaultTableModel srcElmntTableModel = null;

	JComboBox<String> comboLanguage;
    ItemListener comboLangListener;
	String selectedLangCode;
	int languageIndexDefault, prevSelectedLangIndex, newSelectedLangIndex, elementCount;

	String newElementName = null;

/**
 * Create the dialog
 * NOTE: this can be called directly from mainMenu 'Evidence' OR from HG0566EditSource
 */
	public HG0564ManageSrcElmnt(HBProjectOpenData pointOpenProject) throws HBException  {
		this.pointOpenProject = pointOpenProject;
		pointPersonHandler = pointOpenProject.getPersonHandler();
		pointCitationSourceHandler = pointOpenProject.getCitationSourceHandler();

		setTitle("Source Elements");
	// Setup references for HG0450
		windowID = screenID;
		helpName = "managesourceelement";		 //$NON-NLS-1$

	// Setup close and logging actions
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0564ManageSrcElmnt");	//$NON-NLS-1$

	// Collect static GUI text from T204 for Source Element table
		tableSrcElmntColHeads = pointPersonHandler.setTranslatedData("56400", "1", false);	// Source Elements //$NON-NLS-1$ //$NON-NLS-2$

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

		JLabel lbl_Language = new JLabel("Language:");		// Language:
		actionPanel.add(lbl_Language, "cell 0 0, alignx center"); //$NON-NLS-1$
		// Load the data language names and set to current Global dataLanguage
		comboLanguage = new JComboBox<String>();
		for (int i=0; i < HG0501AppSettings.dataReptLanguages.length; i++)
				comboLanguage.addItem(HG0501AppSettings.dataReptLanguages[i]);
		if (HGlobal.numOpenProjects > 0)
			for (int i = 0; i < HG0501AppSettings.dataReptLangCodes.length; i++)
				if (HGlobal.dataLanguage.equals(HG0501AppSettings.dataReptLangCodes[i])) {
					comboLanguage.setSelectedIndex(i);
					selectedLangCode = 	HG0501AppSettings.dataReptLangCodes[i];
					languageIndexDefault = i;
					prevSelectedLangIndex = comboLanguage.getSelectedIndex();
				}
		actionPanel.add(comboLanguage, "cell 0 1, alignx left, grow"); //$NON-NLS-1$

		JButton btn_Add = new JButton("Add");		// Add
		btn_Add.setEnabled(true);
		actionPanel.add(btn_Add, "cell 0 2, alignx center, grow"); //$NON-NLS-1$
		JButton btn_Delete = new JButton("Delete");		// Delete
		btn_Delete.setEnabled(false);
		actionPanel.add(btn_Delete, "cell 0 3, alignx center, grow"); //$NON-NLS-1$
		JLabel lbl_Find = new JLabel("Find:");		// Find:
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
		JTable tableSrcElmnt = new JTable() {
			private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int row, int column) {
					return true;
			}};
	 	// Get Source Element data for current HRE dataLanguage
			try {
				tableSrcElmntData = pointCitationSourceHandler.getSourceElmntList(HGlobal.dataLanguage);
			} catch (HBException hbe) {
				System.out.println( " Error loading Source Element list: " + hbe.getMessage());
				hbe.printStackTrace();
			}
		if (tableSrcElmntData.length == 0 ) {
			JOptionPane.showMessageDialog(tableSrcElmnt, "No data found in the HRE database\n"	// No data found in the HRE database\n
													 + "for the current Data Language.",		// for thecurrent Data Language
													   "Source Elements", 			// Source Elements
													   JOptionPane.ERROR_MESSAGE);
			dispose();
		}

		// Get the number of Elements in the table into a string (##)
		count = " (" + String.format("%02d", tableSrcElmntData.length) + ")";	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
		tableSrcElmnt.getTableHeader().setToolTipText("Click to sort; Click again to sort in reverse order");	// Click to sort; Click again to sort in reverse order
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
		JButton btn_Cancel = new JButton("Cancel");		// Cancel
		btn_Cancel.setEnabled(true);
		contents.add(btn_Cancel, "cell 1 1, alignx right, gapx 10, tag cancel"); //$NON-NLS-1$
		JButton btn_Save = new JButton("Save");		// Save
		btn_Save.setEnabled(false);
		contents.add(btn_Save, "cell 1 1, alignx right, gapx 10, tag yes"); //$NON-NLS-1$
		JButton btn_Select = new JButton("Select");		// Select
		btn_Select.setEnabled(false);
		contents.add(btn_Select, "cell 1 1, alignx right, gapx 10, tag ok"); //$NON-NLS-1$

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

		// Listener for tableSrcElmnt row selection
		tableSrcElmnt.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent selectSrc) {
				if (!selectSrc.getValueIsAdjusting()) {
					if (tableSrcElmnt.getSelectedRow() == -1) return;
				// find source clicked
//					int clickedRow = tableSrcElmnt.getSelectedRow();
//					int selectedRowInTable = tableSrcElmnt.convertRowIndexToModel(clickedRow);

				// Do something with the value so Save button can save it

					btn_Select.setEnabled(true);
					btn_Delete.setEnabled(true);
				}
			}
        });

		// Language combobox - listener for item change
		comboLangListener = new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					// Check for unsaved changes before changing languages
					if (btn_Save.isEnabled())
						if (JOptionPane.showConfirmDialog (btn_Save,
								"There are unsaved changes. \n"
								+ "Do you still wish to change language?",
								"Define Event",
								JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
							// NO option - temp remove the listener
							comboLanguage.removeItemListener(comboLangListener);
							// Change the language back to what it was
							comboLanguage.setSelectedIndex(prevSelectedLangIndex);
							// Re-instate the listener and do nothing else
							comboLanguage.addItemListener(comboLangListener);
							return;
						}
					// YES option - set the selected languge
					if (comboLanguage.getSelectedIndex() != -1) {
						try {
							// Get and save the newly selected index
							newSelectedLangIndex = comboLanguage.getSelectedIndex();
							prevSelectedLangIndex = newSelectedLangIndex;
							selectedLangCode = 	HG0501AppSettings.dataReptLangCodes[newSelectedLangIndex];
							// Empty Element table contents
							srcElmntTableModel.setRowCount(0);
							// Get Element list for new language and reload table
							String[][] tableSrcElmntData = null;
							tableSrcElmntData = pointCitationSourceHandler.getSourceElmntList(selectedLangCode);
							// Revise current element count
							count = " (" + String.format("%02d", tableSrcElmntData.length) + ")";	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
						} catch (HBException e1) {
							e1.printStackTrace();
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
				newElementName = JOptionPane.showInputDialog("Enter new Source Element name");
				if (newElementName != null) {
					newElementName = newElementName.trim();
					// save the new value in T738 but make sure is surrounded by [  ]

				}
			}
		});

		// Listener for Delete button
		btn_Delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				// NOTE02 not implemented yet - delete the Elemnt ONLY if not in use anywhere in project!
				// also confirm deletion before proceeding

			}
		});

		// Listener for Save button
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {

				// update edited element name in database

				if (reminderDisplay != null) reminderDisplay.dispose();
			}
		});

		// Listener for Select button
		btn_Select.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {

				// pass the selected element back to the caller

				if (reminderDisplay != null) reminderDisplay.dispose();
				dispose();
			}
		});

		// Listener for Cancel button
		btn_Cancel.addActionListener(new ActionListener() {
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

}  // End of HG0564ManageSrcElmnt
