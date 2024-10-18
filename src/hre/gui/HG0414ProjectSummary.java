package hre.gui;
/************************************************************************************
* Project Summary - Specification 04.14 GUI_ProjectSummary 2020-02-27
* v0.00.0006 2019-02-17 by R Thompson, revised 2019-07-18 by D Ferguson
* v0.00.0015 2019-12-07 changed code for HG0514 call (D Ferguson)
* v0.00.0016 2019-12-20 implemented alternative constructor (N.Tolleshaug)
* v0.00.0016 2019-12-20 implemented summary data from OpenProjectData (N.Tolleshaug)
* v0.00.0017 2020-01-14 removed dummy data, adjusted table size (D. Ferguson)
* v0.00.0019 2020-02-17 remove Reminder icon (meaningless) (D.Ferguson
* v0.00.0022 2020-07-27 add screenID for Reminder, Help xref (D Ferguson)
* v0.01.0023 2020-08-04 changed from JDialog to extend HG0450SuperDialog (D Ferguson)
* 			 2020-09-30 changed to MigLayout; fonts removed for JTattoo install (D Ferguson)
* v0.01.0025 2021-03-07 text converted to NLS (D Ferguson)
* 			 2021-04-03 changed so screen size adjusts to table/font size (D Ferguson)
* v0.01.0026 2021-05-07 make table expand if screen expanded (D Ferguson)
**************************************************************************************/

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import hre.bila.HB0711Logging;
import hre.nls.HG0414Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Project Summary
 * @author R Thompson
 * @version v0.01.0026
 * @since 2019-02-17
 */

public class HG0414ProjectSummary extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	private String screenID = "41400"; //$NON-NLS-1$
	private JPanel contents;

	private JTable table_Properties;

/**
 * Collect JTable for output by SuperDialog
 * JTable getDataTable()
 */
	@Override
	public JTable getDataTable() {
		return table_Properties;
	}

	/**
	 * Create the Dialog.
	 */
	public HG0414ProjectSummary(String[][] summaryData) {
		// Setup references for HG0450
		windowID = screenID;
		helpName = "summproject"; //$NON-NLS-1$

		if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: entering HG0414 Project Summary");} //$NON-NLS-1$
		setTitle(HG0414Msgs.Text_3);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		contents = new JPanel();
		contents.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[grow]", "[grow]20[]"));	 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.add(Box.createHorizontalGlue());
		// Add icons defined in HG0450
		toolBar.add(btn_Outputicon);
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north"); //$NON-NLS-1$

		JScrollPane scrollPane = new JScrollPane();
		contents.add(scrollPane, "cell 0 0, growx"); //$NON-NLS-1$

		JButton btn_Close = new JButton(HG0414Msgs.Text_9);
		contents.add(btn_Close, "cell 0 1, alignx right"); //$NON-NLS-1$

		// Setup a Project table as non-editable by the user and set TableModel
		table_Properties = new JTable() { private static final long serialVersionUID = 1L;
										@Override
										public boolean isCellEditable(int row, int column) {
														return false;}};
		table_Properties.setModel(new DefaultTableModel(summaryData,
				new String[] {	HG0414Msgs.Text_11, HG0414Msgs.Text_12	}
				));
		table_Properties.getColumnModel().getColumn(0).setPreferredWidth(130);
		table_Properties.getColumnModel().getColumn(0).setMinWidth(100);
		table_Properties.getColumnModel().getColumn(1).setPreferredWidth(300);
		table_Properties.getColumnModel().getColumn(1).setMinWidth(150);
		JTableHeader pHeader = table_Properties.getTableHeader();
		pHeader.setOpaque(false);
		table_Properties.setFillsViewportHeight(true);
		TableCellRenderer rendererFromHeader = table_Properties.getTableHeader().getDefaultRenderer();
		JLabel headerLabel = (JLabel) rendererFromHeader;
		headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
		ListSelectionModel cellSelectionModel = table_Properties.getSelectionModel();
		cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// Setup scrollpane at correct size
		scrollPane.setViewportView(table_Properties);
		int nRows = table_Properties.getRowCount();
		table_Properties.setPreferredScrollableViewportSize(new Dimension(430, nRows*table_Properties.getRowHeight()));

		pack();

/***
 * CREATE ACTION BUTTON LISTENERS
 **/
		btn_Close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: exiting HG0414 Project Summary");} //$NON-NLS-1$
				dispose();
			}
		});

		// Listener for clicking 'X' on screen
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
				btn_Close.doClick();
		    }
		});

	}	// End HG0414 constructor

}	// End HG0414ProjectSummary
