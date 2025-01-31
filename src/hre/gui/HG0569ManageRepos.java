package hre.gui;
/**************************************************************************************
 * ManageRepositories -
 * ***********************************************************************************
 * v0.04.0032 2025-01-17 Original draft (D Ferguson)
 *
 *************************************************************************************
 * Notes for incomplete code still requiring attention
 * NOTE01 implement Add button
 * NOTE02 implement Edit button
 * NOTE03 implement Copy button
 *
 ************************************************************************************/

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import hre.bila.HB0711Logging;
import hre.gui.HGlobalCode.JTableCellTabbing;
import net.miginfocom.swing.MigLayout;

/**
 * Manage Repositories
 * @author D Ferguson
 * @version v0.04.0032
 * @since 2025-01-17
 */

public class HG0569ManageRepos extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	public static final String screenID = "56900"; //$NON-NLS-1$

	private JPanel contents;

	private String[] tableRepoColHeads = null;
	private Object[][] tableRepoData;
	DefaultTableModel srcTableModel = null;

/**
 * Create the dialog
 */
	public HG0569ManageRepos()  {
		setTitle("Manage Repositories");
	// Setup references for HG0450
		windowID = screenID;
		helpName = "managerepository";		 //$NON-NLS-1$

	// Collect static GUI text from T204 for Repository table (T204 data still to be preloaded)
//		String[] tableRepoColHeads = pointPersonHandler.setTranslatedData(screenID, "1", false);		//$NON-NLS-1$

		// Setup close and logging actions
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0569ManageRepos");	//$NON-NLS-1$

	// Setup dialog
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
		JButton btn_Add = new JButton("Add");		// Add
		btn_Add.setEnabled(true);
		actionPanel.add(btn_Add, "cell 0 0, alignx center, grow"); //$NON-NLS-1$
		JButton btn_Edit = new JButton("Edit");		// Edit
		btn_Edit.setEnabled(false);
		actionPanel.add(btn_Edit, "cell 0 1, alignx center, grow"); //$NON-NLS-1$
		JButton btn_Copy = new JButton("Copy");		// Copy
		btn_Copy.setEnabled(false);
		actionPanel.add(btn_Copy, "cell 0 2, alignx center, grow"); //$NON-NLS-1$
		JLabel find = new JLabel("Find:");		// Find:
		actionPanel.add(find, "cell 0 3, alignx center"); //$NON-NLS-1$
		JTextField findText = new JTextField();
		findText.setColumns(10);
		actionPanel.add(findText, "cell 0 4, alignx left"); //$NON-NLS-1$

		contents.add(actionPanel, "cell 0 0, aligny top"); //$NON-NLS-1$

	// Define panel for Repo list
		JPanel repoPanel = new JPanel();
		repoPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		repoPanel.setLayout(new MigLayout("insets 5", "[]", "[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	// Setup JTable to show Repo data
		JTable tableRepo = new JTable() {
			private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
			}};
		tableRepo.setMaximumSize(new Dimension(32767, 32767));
		tableRepo.setFillsViewportHeight(true);
		// Setup tabbing within table against all rows but only column 0-2
		if (tableRepo.getRowCount() > 0)
					JTableCellTabbing.setTabMapping(tableRepo, 0, tableRepo.getRowCount(), 0, 2);
		// scrollPane contains the Repo picklist
		JScrollPane scrollTable = new JScrollPane();
		scrollTable.setPreferredSize(new Dimension(305, 380));
		repoPanel.add(scrollTable, "cell 0 0"); //$NON-NLS-1$
		contents.add(repoPanel, "cell 1 0, grow"); //$NON-NLS-1$

	// Define control buttons
		JButton btn_Cancel = new JButton("Cancel");		// Cancel
		btn_Cancel.setEnabled(true);
		contents.add(btn_Cancel, "cell 1 1, alignx right, gapx 10, tag cancel"); //$NON-NLS-1$
		JButton btn_Select = new JButton("Select");		// Select
		btn_Select.setEnabled(false);
		contents.add(btn_Select, "cell 1 1, alignx right, gapx 10, tag ok"); //$NON-NLS-1$

	// End of Panel Definitions

 	// Get Repo data
		// load some dummy data for test & display - to be removed
		tableRepoColHeads = new String[] {"RepoID", "Repository" };
		tableRepoData = new Object[][] {{1, "Repository A "},	{2, "Repository B"}};
//		tableRepoData = pointPersonHandler.xxxxxxxxxxxxxxxxxxxx		<<< load Repository data
		if (tableRepoData == null ) {
			JOptionPane.showMessageDialog(scrollTable, "No data found in HRE database\n"	// No data found in HRE database\n
													 + "Repository Select error",		// Repository Select error
													   "Repository Select", 			// Repository Select
													   JOptionPane.ERROR_MESSAGE);
			dispose();
		}

	 	// Setup tablePersData, model and renderer
		srcTableModel = new DefaultTableModel(
    		tableRepoData, tableRepoColHeads) {
				private static final long serialVersionUID = 1L;
				@SuppressWarnings({ "unchecked", "rawtypes" })
				@Override
				public Class getColumnClass(int column) {
						return getValueAt(0, column).getClass();
			}
		};
        tableRepo.setModel(srcTableModel);
		tableRepo.getColumnModel().getColumn(0).setMinWidth(50);
		tableRepo.getColumnModel().getColumn(0).setPreferredWidth(50);
		tableRepo.getColumnModel().getColumn(1).setMinWidth(50);
		tableRepo.getColumnModel().getColumn(1).setPreferredWidth(250);
		tableRepo.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		DefaultTableCellRenderer centerLabelRenderer = new DefaultTableCellRenderer();
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
		tableRepo.getTableHeader().setToolTipText("Click to sort; Click again to sort in reverse order");	// Click to sort; Click again to sort in reverse order
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
		    	btn_Cancel.doClick();
			}
		});

		// Listener for tableRepo row selection
		tableRepo.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent selectSrc) {
				if (!selectSrc.getValueIsAdjusting()) {
					if (tableRepo.getSelectedRow() == -1) return;
				// find repo clicked
					int clickedRow = tableRepo.getSelectedRow();
					int selectedRowInTable = tableRepo.convertRowIndexToModel(clickedRow);

				// Do something with the repo so Select button can use it

					btn_Edit.setEnabled(true);
					btn_Copy.setEnabled(true);
					btn_Select.setEnabled(true);
				}
			}
        });

		// Listener for Add button
		btn_Add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {

			}
		});

		// Listener for Edit button
		btn_Edit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				// NOTE02 does nothing yet
			}
		});

		// Listener for Copy button
		btn_Copy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				// NOTE03 does nothing yet
			}
		});

		// Listener for Select button
		btn_Select.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {

			}
		});

		// Listener for Cancel button
		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
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

}  // End of HG0569ManageRepos
