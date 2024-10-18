package hre.gui;
/***************************************************************************************
 * Logging GUI - Specification 05.20 GUI_Logging 2019-04-09
 * v0.00.0007 2019-06-24 by D Ferguson, updated 2019-07-18
 * v0.00.0014 2019-11-18 changes for HB0711 static classes (D Ferguson)
 * v0.00.0015 2019-12-07 changed code for HG0514 call (D Ferguson)
 * v0.00.0022 2020-07-27 add screenID for Reminder, Help xref (D Ferguson)
 * v0.01.0023 2020-08-04 changed from JDialog to extend HG0450SuperDialog (D Ferguson)
 *            2020-09-11 removed Reminder icon from Toolbar (D Ferguson)
 *            2020-09-30 changed to MigLayout; fonts removed for JTattoo install (D Ferguson)
 * v0.01.0025 2021-01-08 correct header names; correct btn_clear handling (D Ferguson)
 * 			  2021-04-05 NLS; allow resizing to handle a log file with long log entries (D Ferguson)
 * v0.01.0026 2021-05-05 stop NPE caused by null or corrupt data (D Ferguson)
 **************************************************************************************/

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
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
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import hre.bila.HB0711Logging;
import hre.nls.HG0520Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Logging GUI
 * @author D Ferguson
 * @version v0.01.0026
 * @since 2019-06-24
 */

public class HG0520Logging extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	private String screenID = "52000"; //$NON-NLS-1$
	private JPanel contents;
	private boolean noneSelected = true;		// for managing table_LogEntries list selections
	private ArrayList<Integer> rowsSelected = new ArrayList<>();

	private JTable table_LogEntries;

/**
 * Collect JTable for output by SuperDialog
 * JTable getDataTable()
 */
	@Override
	public JTable getDataTable() {
		return table_LogEntries;
	}

	/**
	 * Create the Dialog.
	 */
	public HG0520Logging() {
		// Setup references for HG0450
		windowID = screenID;
		helpName = "logging"; //$NON-NLS-1$

		if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: entering HG0520 Logging");}	 //$NON-NLS-1$
		setTitle(HG0520Msgs.Text_3);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		contents = new JPanel();
		contents.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]20[grow]", "[]10[]10[]20[]10[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
	    toolBar.add(Box.createHorizontalGlue());
		// Add icon defined in HG0450
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north"); //$NON-NLS-1$

		JLabel lbl_Select = new JLabel(HG0520Msgs.Text_8);
		contents.add(lbl_Select, "cell 0 0"); //$NON-NLS-1$

		JButton btn_Browse = new JButton(HG0520Msgs.Text_10);
		btn_Browse.setToolTipText(HG0520Msgs.Text_11);
		contents.add(btn_Browse, "cell 1 0"); //$NON-NLS-1$

		JLabel lbl_LogFilename = new JLabel(HG0520Msgs.Text_13);
		contents.add(lbl_LogFilename, "cell 0 2,alignx right"); //$NON-NLS-1$
		JLabel lbl_FilenameData = new JLabel(""); //$NON-NLS-1$
		contents.add(lbl_FilenameData, "cell 1 2"); //$NON-NLS-1$

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(new Rectangle(20, 100, 0, 0));
		contents.add(scrollPane, "cell 0 3 2 1,grow, pushy"); //$NON-NLS-1$

		JButton btn_Clear = new JButton(HG0520Msgs.Text_18);
		btn_Clear.setToolTipText(HG0520Msgs.Text_19);
		btn_Clear.setEnabled(false);
		contents.add(btn_Clear, "cell 0 4 2 1"); //$NON-NLS-1$

		JButton btn_Output = new JButton(HG0520Msgs.Text_21);
		btn_Output.setToolTipText(HG0520Msgs.Text_22);
		btn_Output.setEnabled(false);
		contents.add(btn_Output, "cell 1 4, gapx 20"); //$NON-NLS-1$

		JButton btn_Close = new JButton(HG0520Msgs.Text_24);
		btn_Close.setToolTipText(HG0520Msgs.Text_25);
		contents.add(btn_Close, "cell 1 5, alignx right"); //$NON-NLS-1$

		// Setup table_LogEntries, List of Headings to be re-used
		List<String> headings = new ArrayList<>();
			headings.add(HG0520Msgs.Text_27);
			headings.add(HG0520Msgs.Text_28);
			headings.add(HG0520Msgs.Text_29);
			headings.add(HG0520Msgs.Text_30);
			headings.add(HG0520Msgs.Text_31);

		// Setup a table to hold all Log entries as non-editable by the user and set initial TableModel
		table_LogEntries = new JTable() { private static final long serialVersionUID = 1L;
										 @Override
										public boolean isCellEditable(int row, int column) {
											return false;}};
		table_LogEntries.setBackground(new Color(255, 255, 255));
		table_LogEntries.setPreferredScrollableViewportSize(new Dimension(750, 400));
		table_LogEntries.setModel(new DefaultTableModel(
			new String[][] {    },
			new String[] {HG0520Msgs.Text_32, HG0520Msgs.Text_33, HG0520Msgs.Text_34, HG0520Msgs.Text_35, HG0520Msgs.Text_36}
				));
		// Set the ability to sort on columns
		table_LogEntries.setAutoCreateRowSorter(true);
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(table_LogEntries.getModel());
		table_LogEntries.setRowSorter(sorter);
		List <RowSorter.SortKey> sortKeys = new ArrayList<>();
		sorter.setSortKeys(sortKeys);
		// Set tooltips, column widths and header format
		table_LogEntries.getTableHeader().setToolTipText(HG0520Msgs.Text_37);
		JTableHeader pHeader = table_LogEntries.getTableHeader();
		pHeader.setOpaque(false);
		TableCellRenderer rendererFromHeader = table_LogEntries.getTableHeader().getDefaultRenderer();
		JLabel headerLabel = (JLabel) rendererFromHeader;
		headerLabel.setHorizontalAlignment(SwingConstants.LEFT);

		// Implement selection listener on table_LogEntries
	    class SelectionHandler implements ListSelectionListener {
	        @Override
			public void valueChanged(ListSelectionEvent e) {
	        	btn_Clear.setEnabled(true);									// enable selection reset once a selection is made
	            ListSelectionModel lsm = (ListSelectionModel)e.getSource();
	            if (lsm.isSelectionEmpty()) {noneSelected = true;}
	            	 else {	noneSelected = false;
	            	 		int minIndex = lsm.getMinSelectionIndex();		//reset all indexes each time through here
	                    	int maxIndex = lsm.getMaxSelectionIndex();
	                    	int listPosn = -1;
	                    	rowsSelected.clear();
	                    	for (int i = minIndex; i <= maxIndex; i++) 		// build the selections into the rowsSelected arraylist
	                    		{if (lsm.isSelectedIndex(i)) {	listPosn = listPosn + 1;
	                    										rowsSelected.add(listPosn,i);
	                    									}
	                    		}
	            	 	}
	        }
		}
		// And invoke it against the table
		ListSelectionModel listSelectionModel;
		listSelectionModel = table_LogEntries.getSelectionModel();
        listSelectionModel.addListSelectionListener(new SelectionHandler());
        table_LogEntries.setSelectionModel(listSelectionModel);
        table_LogEntries.setFillsViewportHeight(true);
		// Setup a class to set table_LogEntries column widths, as we need to repeat this action
		class tableSizer {
			private tableSizer() {
				table_LogEntries.getColumnModel().getColumn(0).setMinWidth(75);				//date
				table_LogEntries.getColumnModel().getColumn(0).setPreferredWidth(100);
				table_LogEntries.getColumnModel().getColumn(0).setMaxWidth(120);
				table_LogEntries.getColumnModel().getColumn(1).setMinWidth(100);			//time
				table_LogEntries.getColumnModel().getColumn(1).setMaxWidth(120);
				table_LogEntries.getColumnModel().getColumn(1).setPreferredWidth(100);
				table_LogEntries.getColumnModel().getColumn(2).setMinWidth(75);				//PC
				table_LogEntries.getColumnModel().getColumn(2).setPreferredWidth(100);
				table_LogEntries.getColumnModel().getColumn(2).setMaxWidth(200);
				table_LogEntries.getColumnModel().getColumn(3).setMinWidth(100);			//project name
				table_LogEntries.getColumnModel().getColumn(3).setPreferredWidth(150);
				table_LogEntries.getColumnModel().getColumn(4).setMinWidth(300);			//log entry
				table_LogEntries.getColumnModel().getColumn(4).setPreferredWidth(500);
			}}

		new tableSizer();
		scrollPane.setViewportView(table_LogEntries);

		pack();

/***
 * CREATE ACTION LISTENERS
 **/
		// Browse button - lets user choose a log file to be loaded into GUI table
		btn_Browse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				btn_Browse.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				HG0577FileChooser chooseFile=new HG0577FileChooser("Open", "HRE Log files (*.hrel)", "hrel", null, null, 1);	 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				chooseFile.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xy = btn_Browse.getLocationOnScreen();         // Gets Browse button location on screen
				chooseFile.setLocation(xy.x, xy.y);     		     // Sets screen top-left corner relative to that
				chooseFile.setVisible(true);
				btn_Browse.setCursor(Cursor.getDefaultCursor());
				// Process file and folder-names returned from file chooser (the ''chosen' fields)
				if (HGlobal.chosenFilename == "" ) {   }		         		 // do nothing if no filename setup //$NON-NLS-1$
					else {	lbl_FilenameData.setText(HGlobal.chosenFilename);    // set filename
							// call the log reader - log file will be placed into the List HB0711Logging.allLogEntries
							if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: reading Logfile '"+HGlobal.chosenFilename+"'");} //$NON-NLS-1$ //$NON-NLS-2$
							HB0711Logging.logRead(HGlobal.chosenFilename);
							// Test the data is not null before attempting to use it (else get NPE)
							if (HB0711Logging.allLogEntries == null) return;
							// Now take the allLogEntries data and load it into the GUI's JTable
							DefaultTableModel tableModel = new DefaultTableModel(HB0711Logging.allLogEntries.toArray(new Object[][] {}), headings.toArray() );
							table_LogEntries.setModel(tableModel);
							table_LogEntries.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
							new tableSizer();
							// then enable function button
							btn_Output.setEnabled(true); }
			}
		});

		//Output button - takes selected table entries (or all table) and sends to Output function
		btn_Output.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int numRows = table_LogEntries.getRowCount();
				// check whether user made a row selection or not
	            if (noneSelected) {
	            	  				// do nothing as all of table_LogEntries is to be output
	            	} else {for (int i = numRows-1; i >=0; i--) 			// work backwards thru table_LogEntries
	            				{if (rowsSelected.contains(i)) { } 	        // keep selected rows, else remove the row
	            					else ((DefaultTableModel)table_LogEntries.getModel()).removeRow(i);}
	            			btn_Clear.setEnabled(true);
	            			}
	            HG0531Output outputWindow=new HG0531Output(getTitle(), table_LogEntries);
			    outputWindow.setModalityType(ModalityType.APPLICATION_MODAL);
			    Point xy = btn_Output.getLocationOnScreen();             	// Gets Output button location on screen
			    outputWindow.setLocation(xy.x, xy.y);       	        	// Sets screen top-left corner relative to that
		        outputWindow.setVisible(true);
			}
		});

		// Clear button - clears selections and reloads logEntries into GUI table
		btn_Clear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// When table selections are cleared, reload table_LogEntries from allLogEntries
				DefaultTableModel tableModel = new DefaultTableModel(HB0711Logging.allLogEntries.toArray(new Object[][] {}), headings.toArray() );
				table_LogEntries.setModel(tableModel);
				table_LogEntries.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				new tableSizer();
				btn_Clear.setEnabled(false);		// turn off button once it is used
			}
		});

		// Close button
		btn_Close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (JOptionPane.showConfirmDialog(btn_Close, HG0520Msgs.Text_44, HG0520Msgs.Text_45,
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: closing HG0520 Logging");} //$NON-NLS-1$
							dispose();	}						// yes option - close and return to main menu
			}
		});

		// Listener for clicking 'X' on screen - make same as Close button
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
		    	btn_Close.doClick();
		    }
		});
	}	// End HG0520Logging constructor

}	// End HG0520Logging