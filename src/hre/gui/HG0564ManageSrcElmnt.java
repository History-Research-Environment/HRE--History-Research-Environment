package hre.gui;
/**************************************************************************************
 * ManageSourceElements
 * ***********************************************************************************
 * v0.04.0032 2025-02-07 Original draft (D Ferguson)
 * 			  2025-06-29 Correctly handle Reminder screen display/remove (D Ferguson)
 *
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
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
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

	public static final String screenID = "56400"; //$NON-NLS-1$

	private JPanel contents;

	private String[] tableSrcElmntColHeads = null;
	private Object[][] tableSrcElmntData;
	DefaultTableModel srcElmntTableModel = null;

	String newElementName = null;
/**
 * Create the dialog
 */
	public HG0564ManageSrcElmnt(HBProjectOpenData pointOpenProject) throws HBException  {
		this.pointOpenProject = pointOpenProject;

	// NOTE this can be called directly from 'Evidence' on mainMenu OR from HG0566EditSource
		setTitle("Source Elements");
	// Setup references for HG0450
		windowID = screenID;
		helpName = "managesourceelement";		 //$NON-NLS-1$

	// Collect static GUI text from T204 for Source type table (T204 data still to be preloaded)
//		String[] tableSrcElmntColHeads = pointPersonHandler.setTranslatedData(screenID, "1", false);		//$NON-NLS-1$

		// Setup close and logging actions
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0564ManageSrcElmnt");	//$NON-NLS-1$

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
		actionPanel.setLayout(new MigLayout("insets 10", "[]", "[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		JButton btn_Add = new JButton("Add");		// Add
		btn_Add.setEnabled(true);
		actionPanel.add(btn_Add, "cell 0 0, alignx center, grow"); //$NON-NLS-1$
		JButton btn_Delete = new JButton("Delete");		// Delete
		btn_Delete.setEnabled(false);
		actionPanel.add(btn_Delete, "cell 0 1, alignx center, grow"); //$NON-NLS-1$

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
					return false;
			}};
	 	// Get Source Type data
		// load some dummy data for test & display - to be removed
		tableSrcElmntColHeads = new String[] {"Source Elements" };			// T204 data eventually
		tableSrcElmntData = new Object[][] {{"[AUTHOR]"}, {"[AGENCY]"}};
//			tableSrcElmntData = pointzzzzzzz.xxxxxxxxxxxxxxxxxxxx		<<< load Source Elements from T738
		if (tableSrcElmntData == null ) {
			JOptionPane.showMessageDialog(tableSrcElmnt, "No data found in HRE database\n"	// No data found in HRE database\n
													 + "Source Element load error",		// Source Element load error
													   "Source Elements", 			// Source Elements
													   JOptionPane.ERROR_MESSAGE);
			dispose();
		}
	 	// Setup tablePersData, model and renderer
		srcElmntTableModel = new DefaultTableModel(
    		tableSrcElmntData, tableSrcElmntColHeads) {
				private static final long serialVersionUID = 1L;
				@SuppressWarnings({ "unchecked", "rawtypes" })
				@Override
				public Class getColumnClass(int column) {
						return getValueAt(0, column).getClass();
			}
		};
        tableSrcElmnt.setModel(srcElmntTableModel);
		tableSrcElmnt.getColumnModel().getColumn(0).setMinWidth(50);
		tableSrcElmnt.getColumnModel().getColumn(0).setPreferredWidth(150);
		tableSrcElmnt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		// Set the ability to sort on columns
		tableSrcElmnt.setAutoCreateRowSorter(true);
	    TableModel myModel = tableSrcElmnt.getModel();
	    TableRowSorter<TableModel> sorter = new TableRowSorter<>(myModel);
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
		scrollTable.setPreferredSize(new Dimension(160, 250));
		// Show the table
		scrollTable.setViewportView(tableSrcElmnt);
		tableSrcElmnt.requestFocus();
		SrcElmntPanel.add(scrollTable, "cell 0 0"); //$NON-NLS-1$

		contents.add(SrcElmntPanel, "cell 1 0, grow"); //$NON-NLS-1$

	// Define control buttons
		JButton btn_Cancel = new JButton("Cancel");		// Cancel
		btn_Cancel.setEnabled(true);
		contents.add(btn_Cancel, "cell 1 1, alignx right, gapx 10, tag cancel"); //$NON-NLS-1$
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

				// Do something with the source type value so Select button can use it,

					btn_Select.setEnabled(true);
					btn_Delete.setEnabled(true);
				}
			}
        });

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

	}	// End HG0564ManageSrcElmnt constructor

}  // End of HG0564ManageSrcElmnt
