package hre.gui;
/**************************************************************************************
 * Flag Manager - Specification 05.10-05.13 GUI_Flagxxxxxx
 * v0.01.0025 2021-02-14 Initial draft (D Ferguson)
 * v0.01.0026 2021-09-16 Apply tag codes to screen control buttons (D Ferguson)
 * v0.03.0030 2023-06-01 Add code to display Flag settings (N. Tolleshaug)
 * 			  2023-06-03 Revise layout to add Flag mgmt buttons (D Ferguson)
 *			  2023-07-28 Add update/save code (N. Tolleshaug)
 *			  2023-08-01 Set update/cancel marker code (N. Tolleshaug)
 *			  2023-08-19 Add confirmation of Flag deletion (D Ferguson)
 *			  2023-09-08 Changed to HBPersonFlagManager (N. Tolleshaug)
 *			  2023-10-11 Convert to NLS (D Ferguson)
 *******************************************************************************/

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBPersonFlagManager;
import hre.bila.HBProjectOpenData;
import hre.nls.HG0512Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Flag Manager
 * @author D Ferguson
 * @version v0.03.0030
 * @since 2021-02-14
 */
public class HG0512FlagManager extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	public HBPersonFlagManager pointManagePersonFlag;
	public String screenID = "51200";	//$NON-NLS-1$
	private JPanel contents;

	private JCheckBox chkbox_Filter;
	private String selectString = "";	//$NON-NLS-1$
	JComboBox<String> comboBox_Subset;
	JButton btn_Save;
	boolean cancel = false;

	private boolean madeChanges = false;

	private Object[][] objAllFlagData;
	private Object[][] objReqFlagData;
	private String[] tableColHeads;
	private JTable tableFlags;
	Object objFlagDataToEdit[] = new Object[7]; // to hold data to pass to HG0513FlagEditor
	Object objTempFlagData[] = new Object[7];   // to temporarily hold a row of data when moving rows around

/**
 * Collect JTable for output
 * JTable getDataTable()
 */
	@Override
	public JTable getDataTable() {
		return tableFlags;
	}

	public void setCancelMarker(Boolean cancelState) {
		cancel = cancelState;
	}

/**
 * Create the dialog
 */
	public HG0512FlagManager(HBProjectOpenData pointOpenProject, HBPersonFlagManager pointManagePersonFlag)  {
		this.pointManagePersonFlag = pointManagePersonFlag;

	// Setup references for HG0450
		windowID = screenID;
		helpName = "flagmanager";	//$NON-NLS-1$

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0512FlagManager");	//$NON-NLS-1$

		setTitle(HG0512Msgs.Text_0);	// Flag Manager
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]10[grow]", "[]10[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
	// Get the Table Headings, which are: Flag Name, System?, Language, Active?, All Possible Settings
		tableColHeads = pointManagePersonFlag.setTranslatedData(screenID, "1", false); //$NON-NLS-1$ 
		
	// Define toolBar in NORTH dock area
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.add(Box.createHorizontalGlue());
		toolBar.add(btn_Outputicon);
		toolBar.add(btn_Remindericon);
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north");	//$NON-NLS-1$

	// Row 0 central area
		JLabel lbl_TxtFilter = new JLabel(HG0512Msgs.Text_1);	// Text to filter for: 
		contents.add(lbl_TxtFilter, "cell 1 0, align left");	//$NON-NLS-1$

		JTextField textField = new JTextField();
		textField.setColumns(10);
		textField.setToolTipText(HG0512Msgs.Text_2);	// Name of stored filter
		contents.add(textField, "flowx,cell 1 0");		//$NON-NLS-1$

		chkbox_Filter = new JCheckBox(HG0512Msgs.Text_3);	// Filter    
		chkbox_Filter.setHorizontalTextPosition(SwingConstants.LEADING);
		chkbox_Filter.setHorizontalAlignment(SwingConstants.LEFT);
		chkbox_Filter.setToolTipText(HG0512Msgs.Text_4);	// On if a filter is in use
		contents.add(chkbox_Filter, "cell 1 0, gapx 15");	//$NON-NLS-1$

		JLabel lbl_Subsets = new JLabel(HG0512Msgs.Text_5);	// Select: 
		lbl_Subsets.setToolTipText(HG0512Msgs.Text_6);	// Select filter or Subset name
		contents.add(lbl_Subsets, "cell 1 0, gapx 15");	//$NON-NLS-1$

		comboBox_Subset = new JComboBox<>();
		comboBox_Subset.setPreferredSize(new Dimension(120, 20));
		comboBox_Subset.setToolTipText(HG0512Msgs.Text_7);	// List of saved filter and subset names
		comboBox_Subset.addItem(HG0512Msgs.Text_8);	// All Columns
		contents.add(comboBox_Subset, "cell 1 0");	//$NON-NLS-1$
		for (String tableColHead : tableColHeads) {
			comboBox_Subset.addItem(tableColHead);
			}

	// Col 0, Row 1 - controls for Flag mgmt
		ImageIcon upArrow = new ImageIcon(getClass().getResource("/hre/images/arrow_up16.png")); //$NON-NLS-1$
		JButton btn_Up = new JButton(HG0512Msgs.Text_9, upArrow);		// Move
		btn_Up.setVerticalTextPosition(SwingConstants.BOTTOM);
		btn_Up.setHorizontalTextPosition(SwingConstants.CENTER);
		btn_Up.setEnabled(false);
		contents.add(btn_Up, "flowy,cell 0 1,alignx center,aligny top, gapy 25"); //$NON-NLS-1$

		JButton btn_Add = new JButton(HG0512Msgs.Text_10);		// Add a new Flag
		btn_Add.setHorizontalAlignment(SwingConstants.CENTER);
		btn_Add.setEnabled(true);
		contents.add(btn_Add, "flowy,cell 0 1,growx"); //$NON-NLS-1$

		JButton btn_Delete = new JButton(HG0512Msgs.Text_11);	// Delete this Flag
		btn_Delete.setHorizontalAlignment(SwingConstants.CENTER);
		btn_Delete.setEnabled(false);
		contents.add(btn_Delete, "flowy,cell 0 1,growx"); //$NON-NLS-1$

		ImageIcon downArrow = new ImageIcon(getClass().getResource("/hre/images/arrow_down16.png")); //$NON-NLS-1$
		JButton btn_Down = new JButton(HG0512Msgs.Text_9, downArrow);		// Move
		btn_Down.setVerticalTextPosition(SwingConstants.TOP);
		btn_Down.setHorizontalTextPosition(SwingConstants.CENTER);
		btn_Down.setEnabled(false);
		contents.add(btn_Down, "cell 0 1,alignx center"); //$NON-NLS-1$

	// Col 1, Row 1 - scrollTable for Flag list table
		JScrollPane scrollTable = new JScrollPane();
		scrollTable.setViewportView(tableFlags);
		contents.add(scrollTable, "cell 1 1 2, grow");	//$NON-NLS-1$

	// Row 2
		JButton btn_Exit = new JButton(HG0512Msgs.Text_13);	// Exit
		contents.add(btn_Exit, "cell 1 2, align right, gapx 20, tag cancel");	//$NON-NLS-1$
		btn_Save = new JButton(HG0512Msgs.Text_14);	// Save
		btn_Save.setEnabled(false);
		contents.add(btn_Save, "cell 1 2, align right, gapx 20, tag ok");	//$NON-NLS-1$

/**
 * Setup Flag Table
 **/
	// Get all Flag data - table columns are:
		// 0 - IS_SYSTEM?; 1 - ACTIVE?; 2 - GUI_SEQ; 3 - FLAG_ID; 4 - DEFAULT_INDEX;
	    // 5 - LANG_CODE; 6 - FLAG_NAME; 7 - FLAG_VALUES; 8 - FLAG_DESC
		objAllFlagData = pointManagePersonFlag.getPersonFlagTable();
		// and sort on GUI-sequence (column 2)
		Arrays.sort(objAllFlagData, (o1, o2) -> Integer.compare((Integer) o1[2], (Integer) o2[2]));

	// Now build only the required Flag data into tableReqFlagData. Required table columns are:
		// 0 - Flag name; 1 - System?; 2 - Language; 3 - Active?; 4 - Flag Values; 5- Flag desc.; 6 - FlagID
		// Only cols 0-4 will be displayed, but col 5, 6 needed for editing
		objReqFlagData = new Object[objAllFlagData.length][7];
		for (int i = 0; i < objAllFlagData.length; i++) {
				objReqFlagData[i][0] = objAllFlagData[i][6];	// Flag name
				objReqFlagData[i][1] = objAllFlagData[i][0];	// System? or Custom
				objReqFlagData[i][2] = objAllFlagData[i][5];	// Language
				objReqFlagData[i][3] = objAllFlagData[i][1];	// Active?
				objReqFlagData[i][4] = objAllFlagData[i][7];	// Flag values
				objReqFlagData[i][5] = objAllFlagData[i][8];	// Flag description
				objReqFlagData[i][6] = objAllFlagData[i][3];	// Flag ID
		}

	// Define displayable flag table
		DefaultTableModel flagModel = new DefaultTableModel(objReqFlagData, tableColHeads);
		tableFlags = new JTable(flagModel) {
			private static final long serialVersionUID = 1L;
			@Override
			public Dimension getPreferredScrollableViewportSize() {
			    return new Dimension(super.getPreferredSize().width,
			    		super.getPreferredScrollableViewportSize().height);
			  }
			// Make all columns non-editable
			@Override
			public boolean isCellEditable(int row, int col) {
				return false;
			}
			// Make col 1 & 3 (System? & Active?) Boolean class
			@Override
			public Class<?> getColumnClass(int col) {
			     if (col == 1 || col ==3) return Boolean.class;
			        else return String.class;
			}
		};

	// Load data table and header for Flag list
		tableFlags.setFillsViewportHeight(true);
		tableFlags.getColumnModel().getColumn(0).setMinWidth(50);
		tableFlags.getColumnModel().getColumn(0).setPreferredWidth(180);	// flag name
		tableFlags.getColumnModel().getColumn(1).setMinWidth(50);
		tableFlags.getColumnModel().getColumn(1).setPreferredWidth(60);		// flag type checkbox
		tableFlags.getColumnModel().getColumn(2).setMinWidth(50);
		tableFlags.getColumnModel().getColumn(2).setPreferredWidth(80);		// flag language
		tableFlags.getColumnModel().getColumn(3).setMinWidth(50);
		tableFlags.getColumnModel().getColumn(3).setPreferredWidth(60);		// flag active checkbox
		tableFlags.getColumnModel().getColumn(4).setMinWidth(50);
		tableFlags.getColumnModel().getColumn(4).setPreferredWidth(350);	// flag all values
		tableFlags.getColumnModel().getColumn(4).setMaxWidth(600);
		tableFlags.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	// Center column 2 data
	    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tableFlags.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
	// Set the ability to restore after filter
	    TableModel myModel = tableFlags.getModel();
	    TableRowSorter<TableModel> sorter = new TableRowSorter<>(myModel);
	// Set header format
		JTableHeader pHeader = tableFlags.getTableHeader();
		pHeader.setOpaque(false);
		TableCellRenderer prendererFromHeader = tableFlags.getTableHeader().getDefaultRenderer();
		JLabel pheaderLabel = (JLabel) prendererFromHeader;
		pheaderLabel.setHorizontalAlignment(SwingConstants.CENTER);
		ListSelectionModel pcellSelectionModel = tableFlags.getSelectionModel();
		pcellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		scrollTable.setViewportView(tableFlags);

	  // Build GUI
		pack();

/**
 * CREATE ACTION BUTTON LISTENERS
 **/
	// Listener for clicking 'X on screen
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
		    	btn_Exit.doClick();
		    }
		});

	// Listener for Exit button
		btn_Exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (madeChanges) {
					if (JOptionPane.showConfirmDialog(btn_Save,
						HG0512Msgs.Text_15		// There are unsaved Flag changes. \n
						+ HG0512Msgs.Text_16,	// Do you still wish to exit this screen?
						HG0512Msgs.Text_17,		// Administer Person Flags
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0512FlagManager"); //$NON-NLS-1$
						// Yes option: Save GUI config data and exit
							setGuiConfigData(pointOpenProject,screenID);
							dispose();
						}
				}
				else {
					if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0512FlagManager"); //$NON-NLS-1$
					// Save GUI config data and exit
					setGuiConfigData(pointOpenProject,screenID);
					dispose();
				}
			}
		});

	// Listener for Save button
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (madeChanges) {
					try {
					//Transfer data to ManagePersonFlag and update table.
						for (int i = 0; i < objAllFlagData.length; i++) {
							objAllFlagData[i][6] = objReqFlagData[i][0];	// Flag name
							objAllFlagData[i][0] = objReqFlagData[i][1];	// System? or Custom
							objAllFlagData[i][5] = objReqFlagData[i][2];	// Language
							objAllFlagData[i][1] = objReqFlagData[i][3];	// Active?
							objAllFlagData[i][7] = objReqFlagData[i][4];	// Flag values
							objAllFlagData[i][8] = objReqFlagData[i][5];	// Flag description
							objAllFlagData[i][3] = objReqFlagData[i][6];	// Flag ID
							objAllFlagData[i][2] = i; 						// Set new GUI_SEQ
						}
						pointManagePersonFlag.updateFlagDescription();
						resetFlagDescription(false);
						setGuiConfigData(pointOpenProject, screenID);
						if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: saving updates and exiting HG0512FlagManager"); //$NON-NLS-1$
						dispose();
					} catch (HBException hbe) {
						System.out.println("HBPersonHandler - updateFlagDescription(): " + hbe.getMessage());	//$NON-NLS-1$
						hbe.printStackTrace();
					// Log Stack Trace
						HB0711Logging.logWrite("ERROR Flag updating:  " + hbe.getMessage());	//$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
			}
		});

	// Filter checkbox listener
		chkbox_Filter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			// Reset filter for whole table when unselected, sorted as before
				if (!chkbox_Filter.isSelected()) {
						setTableFilter(selectString, textField.getText());
						comboBox_Subset.setSelectedIndex(0);
						tableFlags.setRowSorter(sorter);
				}
			}
		});

	// On selection of a Subset perform appropriate filter action
		comboBox_Subset.addActionListener (new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (chkbox_Filter.isSelected()) {
					selectString = comboBox_Subset.getSelectedItem().toString();
					setTableFilter(selectString, textField.getText());
				}
			}
		});

	// Listener for Flag table mouse clicks
		tableFlags.addMouseListener(new MouseAdapter() {
			@Override
	        public void mousePressed(MouseEvent me) {
	           	if (me.getClickCount() == 1 && tableFlags.getSelectedRow() != -1) {
	           	// SINGLE-CLICK - turn on table controls
	        		btn_Delete.setEnabled(true);
	        		btn_Up.setEnabled(true);
	        		btn_Down.setEnabled(true);
	           	}

        		// DOUBLE_CLICK - get Object to pass to HG0513FlagEditor and do so
	           	if (me.getClickCount() == 2 && tableFlags.getSelectedRow() != -1) {
	           		int atRow = tableFlags.getSelectedRow();
	           		objFlagDataToEdit = objReqFlagData[atRow]; // select whole row

	        	// Display FlagEditor
	        		showHG0513FlagEdit(atRow, objFlagDataToEdit, false);
	           	}
	        }
	    });

	// Listener for btn_Add a Flag
		btn_Add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
			// Pass across an empty flag data Object to HG0513
				try {
	           		objFlagDataToEdit[0] = "";	//$NON-NLS-1$	// Flag name
	           		objFlagDataToEdit[1] = ((boolean) false);	// System? (must be false for an added flag)
					objFlagDataToEdit[2] = pointManagePersonFlag.returnFlagLangName(HGlobal.dataLanguage);  // Set LangCode = current data Language
	           		objFlagDataToEdit[3] = ((boolean) true);	// Active? (assume true for a new flag)
	           		objFlagDataToEdit[4] = "";	//$NON-NLS-1$	// Flag values
	           		objFlagDataToEdit[5] = "";	//$NON-NLS-1$	// Flag description
	           		objFlagDataToEdit[6] = 0;					// FlagID (dummy value)

				// Assume show at row -1 of flag table
	           		showHG0513FlagEdit(-1, objFlagDataToEdit, true);
	           	// do not continue, as we have cancelled out of HG0513
	           		if (cancel) return;

	           	// Now test new Flag Name - if it matches an existing Flag Name, add '1' to the name
	    			for (Object[] element : objReqFlagData) {
	    				if (objFlagDataToEdit[0].equals(element[0]))
	    					objFlagDataToEdit[0] = objFlagDataToEdit[0] + "1";	//$NON-NLS-1$
	    			}
	    		// and save it
					pointManagePersonFlag.addFlagDescriptTrans(objFlagDataToEdit);
         		    resetFlagDescription(true);
		            madeChanges = false;
		            btn_Save.setEnabled(false);
				} catch (HBException hbe) {
					System.out.println("HBPersonHandler - add Flag: " + hbe.getMessage());	//$NON-NLS-1$
					hbe.printStackTrace();
				// Log Stack Trace
					HB0711Logging.logWrite("ERROR Add Flag handling:  " + hbe.getMessage());	//$NON-NLS-1$
					HB0711Logging.printStackTraceToFile(hbe);
				}
			}
		});

	// Listener for btn_Delete of a Flag
		btn_Delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int delRow = tableFlags.getSelectedRow();
				if (delRow != -1) {
	               int flagIdent = (int) objReqFlagData[delRow][6];
	               try {
	            	// check if System flag
	            	   if ((boolean) objReqFlagData[delRow][1]) {
	           				JOptionPane.showMessageDialog(null,	HG0512Msgs.Text_18,		// You cannot delete a System-defined flag
	           						HG0512Msgs.Text_0, JOptionPane.ERROR_MESSAGE);		// Flag Manager
	           				return;
	            	   }
	            	// confirm deletion
	            	   if (JOptionPane.showConfirmDialog(btn_Save,
								HG0512Msgs.Text_20,		// Are you sure you want to delete this Flag? 
								HG0512Msgs.Text_17,		// Administer Person Flags
								JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
									pointManagePersonFlag.deleteFlagDescriprion(flagIdent);
				            		resetFlagDescription(true);
								}
	            	   		else return;
			           madeChanges = false;
				       btn_Save.setEnabled(false);
	               } catch (HBException hbe) {
						System.out.println(" HBPersonHandler - delete Flag: " + hbe.getMessage());	//$NON-NLS-1$
						hbe.printStackTrace();
					// Log Stack Trace
						HB0711Logging.logWrite("ERROR Delete Flag handling:  " + hbe.getMessage());	//$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
	               }
				}
			}
		});

	// Listener for btn_Up (move a Flag up in the GUI Sequence)
		btn_Up.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int selectedRow = tableFlags.getSelectedRow();
				// only allow move up if not at top of table
				if (selectedRow >= 1) {
				// Switch rows in tableModel
					flagModel.moveRow(selectedRow, selectedRow, selectedRow-1);
				// Switch rows in underlying objReqFlagData
					objTempFlagData = objReqFlagData[selectedRow-1];
					objReqFlagData[selectedRow-1] = objReqFlagData[selectedRow];
					objReqFlagData[selectedRow] = objTempFlagData;
				//  Reset visible selected row
				    tableFlags.setRowSelectionInterval(selectedRow-1, selectedRow-1);
					madeChanges = true;
					btn_Save.setEnabled(true);
				}
			}
		});

	// Listener for btn_Down (move a Flag down in the GUI Sequence)
		btn_Down.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int tableSize = tableFlags.getRowCount();
				int selectedRow = tableFlags.getSelectedRow();
				// only allow move down if not at end of table
				if (selectedRow < tableSize-1) {
				// Switch rows in tableModel
					flagModel.moveRow(selectedRow, selectedRow, selectedRow+1);
				// Switch rows in underlying objReqFlagData
					objTempFlagData = objReqFlagData[selectedRow];
					objReqFlagData[selectedRow] = objReqFlagData[selectedRow+1];
					objReqFlagData[selectedRow+1] = objTempFlagData;
				//  Reset visible selected row
					tableFlags.setRowSelectionInterval(selectedRow+1, selectedRow+1);
					madeChanges = true;
					btn_Save.setEnabled(true);
				}
			}
		});

	}	// End HG0512FlagManager constructor

/**
 * resetFlagDescription()
 * @throws HBException
 */
	public void resetFlagDescription(boolean updateRowTable) throws HBException {
		pointManagePersonFlag.processFlagTable();
		objAllFlagData = pointManagePersonFlag.getPersonFlagTable();
		if (updateRowTable) {
			objReqFlagData = new Object[objAllFlagData.length][7];
			for (int i = 0; i < objAllFlagData.length; i++) {
				objReqFlagData[i][0] = objAllFlagData[i][6];	// Flag name
				objReqFlagData[i][1] = objAllFlagData[i][0];	// System? or Custom
				objReqFlagData[i][2] = objAllFlagData[i][5];	// Language
				objReqFlagData[i][3] = objAllFlagData[i][1];	// Active?
				objReqFlagData[i][4] = objAllFlagData[i][7];	// Flag values
				objReqFlagData[i][5] = objAllFlagData[i][8];	// Flag description
				objReqFlagData[i][6] = objAllFlagData[i][3];	// Flag ID
			}
		}

		DefaultTableModel eventModel = (DefaultTableModel) tableFlags.getModel();
		eventModel.setDataVector(objReqFlagData, tableColHeads);

	// Load data table and header for Flag list
		tableFlags.setFillsViewportHeight(true);
		tableFlags.getColumnModel().getColumn(0).setMinWidth(50);
		tableFlags.getColumnModel().getColumn(0).setPreferredWidth(180);		// flag name
		tableFlags.getColumnModel().getColumn(1).setMinWidth(50);
		tableFlags.getColumnModel().getColumn(1).setPreferredWidth(60);			// flag type checkbox
		tableFlags.getColumnModel().getColumn(2).setMinWidth(50);
		tableFlags.getColumnModel().getColumn(2).setPreferredWidth(80);			// flag language
		tableFlags.getColumnModel().getColumn(3).setMinWidth(50);
		tableFlags.getColumnModel().getColumn(3).setPreferredWidth(60);			// flag active checkbox
		tableFlags.getColumnModel().getColumn(4).setMinWidth(50);
		tableFlags.getColumnModel().getColumn(4).setPreferredWidth(350);		// flag all values
		tableFlags.getColumnModel().getColumn(4).setMaxWidth(600);
		tableFlags.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		madeChanges = true;
		btn_Save.setEnabled(true);
	}

/**
 * Action setup and/or change of filter settings
 * @param selectString
 * @param filterText
 */
	private void setTableFilter(String selectString, String filterText) {
		TableModel myModel = tableFlags.getModel();
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(myModel);
		if (selectString.equals(HG0512Msgs.Text_8)) {		// All Columns
		// Whole table!
			sorter.setRowFilter(RowFilter.regexFilter("(?i)" + filterText));	// use case insensitive form  //$NON-NLS-1$
		} else for (int i = 0; i < tableColHeads.length; i++) {
			if (selectString.equals(tableColHeads[i])) {
				// Only one column
				sorter.setRowFilter(RowFilter.regexFilter("(?i)" + filterText,i));	// use case insensitive form  //$NON-NLS-1$
			}
		}
	    tableFlags.setRowSorter(sorter);
	}	// End setTableFilter

/**
 * Set GUI window configuration when closing window
 * @param pointOpenProject
 * @param screenID
 */
	private void setGuiConfigData(HBProjectOpenData pointOpenProject, String screenID) {
		// close reminder display
		if (reminderDisplay != null) reminderDisplay.dispose();
	    // Set frame size	in GUI data
			Dimension frameSize = getSize();
			pointOpenProject.setSizeScreen(screenID,frameSize);
		// Set position	in GUI data
			Point position = getLocation();
			pointOpenProject.setPositionScreen(screenID,position);
		// Set class name in GUI configuration data
			pointOpenProject.setClassName(screenID, "HG0512FlagManager");	//$NON-NLS-1$
		// Mark the screen as closed in T302
			pointOpenProject.closeStatusScreen(screenID);
	}	// End setGuiConfigData

/**
 * showHG0513FlagEdit - complete action to show HG0513 screen
 * @param row - where to put HG0513
 * @param objFlagDataToEdit - data to display for editing
 */
    private void showHG0513FlagEdit(int row, Object[] objFlagDataToEdit, boolean addFlag) {
	// Mark the row selected, at col 0
		tableFlags.changeSelection(row, 0, false, false );
	// Work out where to show HG0513 screen
        Rectangle r = tableFlags.getCellRect(row, 0, true);
        Point p = new Point(r.x, r.y + r.height);  // lower left of cell
        SwingUtilities.convertPointToScreen(p, tableFlags);
    // Show HG0513
        setCancelMarker(false);
        HG0513FlagEditor editScreen = new HG0513FlagEditor(this, addFlag, objFlagDataToEdit);
        editScreen.setModalityType(ModalityType.APPLICATION_MODAL);
        editScreen.setLocation(p.x, p.y);
        editScreen.setVisible(true);
    }	// End showHG0513FlagEdit

}  // End of HG0512FlagManager