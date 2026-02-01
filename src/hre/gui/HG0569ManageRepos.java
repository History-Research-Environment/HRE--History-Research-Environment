package hre.gui;
/**************************************************************************************
 * ManageRepositories - HG0569ManageRepos extends HG0450SuperDialog
 * ***********************************************************************************
 * v0.04.0032 2025-01-17 Original draft (D Ferguson)
 *			  2025-01-31 Add Delete button (D Ferguson)
 *			  2025-02-01 Invoke HG0570 from Add button (D Ferguson)
 * 			  2025-06-29 Correctly handle Reminder screen display/remove (D Ferguson)
 *			  2025-08-16 Get table header from T204 (D Ferguson)
 *			  2025-09-03 Load Repository list from database (D Ferguson)
 *			  2025-09-17 Add Repo count into table header, fix sort order (D Ferguson)
 *			  2025-11-01 Code for Add and Edit repositories (N.Tolleshaug)
 *     		  2025-11-08 Code for delete and check repositor is in use (N.Tolleshaug)
 *     		  2025-11-22 Fix Repo table display when reset; remove Save btn (D Ferguson)
 *     					 Make double-click on Repo go to Edit (D Ferguson)
 *     		  2025-12-05 Code for copy repositories (N.Tolleshaug)
 *			  2025-12-15 NLS all code into HG0570Msgs (D Ferguson)
 *			  2026-01-04 Log catch block errors (D Ferguson)
 *			  2026-01-27 Updated for more than one project open/close (N.Tolleshaug)
 ************************************************************************************/

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import hre.bila.HB0711Logging;
import hre.bila.HBCitationSourceHandler;
import hre.bila.HBException;
import hre.bila.HBProjectOpenData;
import hre.bila.HBRepositoryHandler;
import hre.gui.HGlobalCode.JTableCellTabbing;
import hre.nls.HG0570Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Manage Repositories
 * @author D Ferguson
 * @version v0.04.0032
 * @since 2025-01-17
 */
public class HG0569ManageRepos extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	HBCitationSourceHandler pointCitationSourceHandler;
	HBRepositoryHandler pointRepositoryHandler;
	HG0569ManageRepos pointManageRepos = this;

	public static final String screenID = "56900"; //$NON-NLS-1$
	long null_RPID  = 1999999999999999L;
	long proOffset  = 1000000000000000L;

	int selectedRowInTable;
	boolean select = false; // Select true - only select / select false reposmanage

	private JPanel contents;
	JTable tableRepo;

	String[] tableRepoColHeads = null;
	Object[][] tableRepoData;
	DefaultTableModel repoTableModel = null;
	DefaultTableCellRenderer centerLabelRenderer;

/**
 * Create the dialog
 */
	public HG0569ManageRepos(HBProjectOpenData pointOpenProject, boolean select)  {
		this.pointOpenProject = pointOpenProject;
		this.select = select;
		pointCitationSourceHandler = pointOpenProject.getCitationSourceHandler();
		pointRepositoryHandler = pointOpenProject.getRepositoryHandler();
		pointRepositoryHandler.pointManageRepos = this;

		setTitle(HG0570Msgs.Text_30);	// Manage Repositories
	// Setup references for HG0450
		windowID = screenID;
		helpName = "managerepository";		 //$NON-NLS-1$

	// Setup close and logging actions
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0570ManageRepos");	//$NON-NLS-1$

	// Collect static GUI text from T204 for Repository table
		tableRepoColHeads = pointCitationSourceHandler.setTranslatedData("56900", "1", false); // ID, Repository //$NON-NLS-1$ //$NON-NLS-2$

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
		actionPanel.setLayout(new MigLayout("insets 5", "[]", "[]10[]10[]10[][]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		JButton btn_Add = new JButton(HG0570Msgs.Text_31);		// Add
		btn_Add.setEnabled(true);
		actionPanel.add(btn_Add, "cell 0 0, alignx center, grow"); //$NON-NLS-1$
		JButton btn_Edit = new JButton(HG0570Msgs.Text_32);		// Edit
		btn_Edit.setEnabled(false);
		actionPanel.add(btn_Edit, "cell 0 1, alignx center, grow"); //$NON-NLS-1$
		JButton btn_Copy = new JButton(HG0570Msgs.Text_33);		// Copy
		btn_Copy.setEnabled(false);
		actionPanel.add(btn_Copy, "cell 0 2, alignx center, grow"); //$NON-NLS-1$
		JButton btn_Delete = new JButton(HG0570Msgs.Text_34);		// Delete
		btn_Delete.setEnabled(false);
		actionPanel.add(btn_Delete, "cell 0 3, alignx center, grow"); //$NON-NLS-1$
		JLabel find = new JLabel(HG0570Msgs.Text_35);		// Find:
		actionPanel.add(find, "cell 0 4, alignx center"); //$NON-NLS-1$
		JTextField findText = new JTextField();
		findText.setColumns(10);
		actionPanel.add(findText, "cell 0 5, alignx left"); //$NON-NLS-1$

		contents.add(actionPanel, "cell 0 0, aligny top"); //$NON-NLS-1$

	// Define panel for Repo list
		JPanel repoPanel = new JPanel();
		repoPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		repoPanel.setLayout(new MigLayout("insets 5", "[]", "[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	// Setup JTable to show Repo data
		tableRepo = new JTable() {
			private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int row, int col) {
					return false;
			}};
		tableRepo.setMaximumSize(new Dimension(32767, 32767));
		tableRepo.setFillsViewportHeight(true);
		// Setup tabbing within table against all rows but only column 0-1
		if (tableRepo.getRowCount() > 0)
					JTableCellTabbing.setTabMapping(tableRepo, 0, tableRepo.getRowCount(), 0, 1);
		// scrollPane contains the Repo picklist
		JScrollPane scrollTable = new JScrollPane();
		scrollTable.setPreferredSize(new Dimension(460, 400));
		repoPanel.add(scrollTable, "cell 0 0"); //$NON-NLS-1$
		contents.add(repoPanel, "cell 1 0, grow"); //$NON-NLS-1$

	// Define control buttons
		JButton Btn_Close = new JButton(HG0570Msgs.Text_12);		// Close
		Btn_Close.setEnabled(true);
		contents.add(Btn_Close, "cell 1 1, alignx right, gapx 10, tag cancel"); //$NON-NLS-1$
		JButton btn_Select = new JButton(HG0570Msgs.Text_37);		// Select
		btn_Select.setEnabled(false);
		contents.add(btn_Select, "cell 1 1, alignx right, gapx 10, tag ok"); //$NON-NLS-1$

	// End of Panel Definitions

 	// Get Repo data
		try {
			tableRepoData = pointRepositoryHandler.getRepoList(true);
		} catch (HBException hbe) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in HG0569 loading Repository list " + hbe.getMessage()); //$NON-NLS-1$
				HB0711Logging.printStackTraceToFile(hbe);
			}
		}
		if (tableRepoData.length == 0 ) {
			JOptionPane.showMessageDialog(scrollTable, HG0570Msgs.Text_39	// No data found in HRE database\n
													 + HG0570Msgs.Text_40,		// Repository Select error
													   HG0570Msgs.Text_41, 			// Repository Select
													   JOptionPane.ERROR_MESSAGE);
			dispose();
		}

		// Get the number of Elements in the table into a string (##)
		String count = " (" + String.format("%01d", tableRepoData.length) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		// Add this count into the table header string
		tableRepoColHeads[1] = tableRepoColHeads[1] + count;

		// Setup tablemodel, renderer and layout
		repoTableModel = new DefaultTableModel(tableRepoData, tableRepoColHeads) {
			private static final long serialVersionUID = 1L;
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public Class getColumnClass(int column) {
					return getValueAt(0, column).getClass();
			}
		};
        tableRepo.setModel(repoTableModel);
		tableRepo.getColumnModel().getColumn(0).setMinWidth(50);
		tableRepo.getColumnModel().getColumn(0).setPreferredWidth(50);
		tableRepo.getColumnModel().getColumn(1).setMinWidth(50);
		tableRepo.getColumnModel().getColumn(1).setPreferredWidth(380);
		tableRepo.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		centerLabelRenderer = new DefaultTableCellRenderer();
		centerLabelRenderer.setHorizontalAlignment(JLabel.CENTER);
		tableRepo.getColumnModel().getColumn(0).setCellRenderer(centerLabelRenderer);

		// Set the ability to sort on columns
		tableRepo.setAutoCreateRowSorter(true);
	    TableModel myModel = tableRepo.getModel();
	    TableRowSorter<TableModel> sorter = new TableRowSorter<>(myModel);
		List <RowSorter.SortKey> psortKeys1 = new ArrayList<>();

		// Presort on column 1
		psortKeys1.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
		sorter.setSortKeys(psortKeys1);
	    tableRepo.setRowSorter(sorter);

	    // Set tooltips and header format
		tableRepo.getTableHeader().setToolTipText(HG0570Msgs.Text_42);	// Click to sort; Click again to sort in reverse order
		JTableHeader pHeader = tableRepo.getTableHeader();
		pHeader.setOpaque(false);
		TableCellRenderer prendererFromHeader = tableRepo.getTableHeader().getDefaultRenderer();
		JLabel pheaderLabel = (JLabel) prendererFromHeader;
		pheaderLabel.setHorizontalAlignment(SwingConstants.CENTER);

		// Set row selection action
		ListSelectionModel rowSelectionModel = tableRepo.getSelectionModel();
		rowSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

		// Show the table
		scrollTable.setViewportView(tableRepo);
		tableRepo.requestFocus();

		// Focus Policy still to be setup!

		pack();


/*****************************
 * CREATE All ACTION LISTENERS
 *****************************/
		// Listener for clicking 'X' on screen
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
		    	Btn_Close.doClick();
			}
		});

		// Listener for Repo table mouse click selections
		tableRepo.addMouseListener(new MouseAdapter() {
			@Override
	        public void mousePressed(MouseEvent me) {
				if (tableRepo.getSelectedRow() != -1) {
				int clickedRow = tableRepo.getSelectedRow();
				selectedRowInTable = tableRepo.convertRowIndexToModel(clickedRow);
		           	if (me.getClickCount() == 1) {
		           	// SINGLE-CLICK - turn on table controls
						btn_Edit.setEnabled(true);
						btn_Copy.setEnabled(true);
						btn_Select.setEnabled(true);
						btn_Delete.setEnabled(true);
		           	}
	        		// DOUBLE_CLICK - make it an Edit
		           	if (me.getClickCount() == 2) {
		           		btn_Edit.doClick();
		           	}
				}
			}
		});

		// Listener for Add button
		btn_Add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				HG0570EditRepository addScreen;
				try {
					addScreen = pointRepositoryHandler.
												activateeAddRepository(pointOpenProject,
														findLargestSequenceNumber());
					addScreen.setModalityType(ModalityType.APPLICATION_MODAL);
					Point xyEdit = btn_Add.getLocationOnScreen();
					addScreen.setLocation(xyEdit.x, xyEdit.y + 30);
					addScreen.setVisible(true);
				} catch (HBException hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0569 adding a Repository " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
			}
		});

		// Listener for Edit button
		btn_Edit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				// Check if no row selected (like after a Delete!)
				if (tableRepo.getSelectedRow() == -1) return;
				HG0570EditRepository editScreen;
				try {
					editScreen = pointRepositoryHandler.
												activateEditRepository(pointOpenProject,
														(long) tableRepoData[selectedRowInTable][2]);
					editScreen.setModalityType(ModalityType.APPLICATION_MODAL);
					Point xyEdit = btn_Add.getLocationOnScreen();
					editScreen.setLocation(xyEdit.x, xyEdit.y + 30);
					editScreen.setVisible(true);
				} catch (HBException hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0569 editing a Repository " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
			}
		});

		// Listener for Copy button
		btn_Copy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				if (tableRepo.getSelectedRow() == -1) return;
				HG0570EditRepository copyScreen;
				try {
					copyScreen = pointRepositoryHandler.
												activateCopyRepository(pointOpenProject, findLargestSequenceNumber()
														,(long) tableRepoData[selectedRowInTable][2]);
					copyScreen.setModalityType(ModalityType.APPLICATION_MODAL);
					Point xyEdit = btn_Add.getLocationOnScreen();
					copyScreen.setLocation(xyEdit.x, xyEdit.y + 30);
					copyScreen.setVisible(true);
				} catch (HBException hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0569 copying a Repository " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
			}
		});

		// Listener for Repository Delete button
		btn_Delete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				try {
					pointRepositoryHandler.deleteRepository((long) tableRepoData[selectedRowInTable][2]);
					resetRepositoryTable();
					tableRepo.getSelectionModel().clearSelection(); // clear selectedRow setting
				} catch (HBException hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0569 deleting a Repository " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
					JOptionPane.showMessageDialog(pointManageRepos, HG0570Msgs.Text_43	// Cannot delete Repository\n
							 + hbe.getMessage(),
							   HG0570Msgs.Text_44, 			// Repository Delete
							   JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		// Listener for Select button
		btn_Select.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				// Check if no row selected (like after a Delete!)
				if (tableRepo.getSelectedRow() == -1) return;
				// pass the selected element back to the caller
				int selectedRowInTable = tableRepo.convertRowIndexToModel(tableRepo.getSelectedRow());
				// Transfer selected repos PID to Respository handler
				if (select) {
					try {
						pointRepositoryHandler.addNewRespositoryLink((long)tableRepoData[selectedRowInTable][2]);
					} catch (HBException hbe) {
						if (HGlobal.writeLogs) {
							HB0711Logging.logWrite("ERROR: in HG0569 selecting a Repository " + hbe.getMessage()); //$NON-NLS-1$
							HB0711Logging.printStackTraceToFile(hbe);
						}
					}
				}
				if (reminderDisplay != null) reminderDisplay.dispose();
				dispose();
			}
		});

		// Listener for Close button
		Btn_Close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				if (reminderDisplay != null) reminderDisplay.dispose();
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0569ManageRepos");	//$NON-NLS-1$
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
		            for (int row = 0; row <= tableRepo.getRowCount() - 1; row++) {
	            		String tableValue = (String) tableRepo.getValueAt(row, 1);
   	                    if (tableValue.toLowerCase().contains(text.toLowerCase())) {
   	                    	tableRepo.scrollRectToVisible(tableRepo.getCellRect(row, 1, true));
   	                    // set the 'found' row as selected
   	                    	tableRepo.changeSelection(row, 0, false, false);
   	                    	return;
   	                    }
	            }
	          }
		});

	}	// End HG0569ManageRepos constructor

/**
 * resetRepositoryTable();
 */
	public void resetRepositoryTable() {
		try {
			tableRepoData = pointRepositoryHandler.getRepoList(false);
		// Setup reposotory table, model and renderer
			repoTableModel.setDataVector(tableRepoData, tableRepoColHeads);
			tableRepo.setModel(repoTableModel);
			tableRepo.getColumnModel().getColumn(0).setMinWidth(50);
			tableRepo.getColumnModel().getColumn(0).setPreferredWidth(50);
			tableRepo.getColumnModel().getColumn(1).setMinWidth(50);
			tableRepo.getColumnModel().getColumn(1).setPreferredWidth(380);
			tableRepo.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			tableRepo.getColumnModel().getColumn(0).setCellRenderer(centerLabelRenderer);

			// Set the ability to sort on columns
			tableRepo.setAutoCreateRowSorter(true);
		    TableModel myModel = tableRepo.getModel();
		    TableRowSorter<TableModel> sorter = new TableRowSorter<>(myModel);
			List <RowSorter.SortKey> psortKeys1 = new ArrayList<>();

			// Presort on column 1
			psortKeys1.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
			sorter.setSortKeys(psortKeys1);
		    tableRepo.setRowSorter(sorter);

		} catch (HBException hbe) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in HG0569 at Repository list reload " + hbe.getMessage()); //$NON-NLS-1$
				HB0711Logging.printStackTraceToFile(hbe);
			}
		}
	}

/**
 * findLargestSequenceNumber()
 * @return
 */
	private int findLargestSequenceNumber() {
		int sequenceNumber = 0;
		for (int i = 0; i < tableRepoData.length; i++) {
			if ((int)tableRepoData[i][0] > sequenceNumber) sequenceNumber = (int)tableRepoData[i][0];
		}
		return sequenceNumber;
	}

}  // End of HG0569ManageRepos
