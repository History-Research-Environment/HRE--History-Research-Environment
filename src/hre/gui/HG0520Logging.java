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
 * v0.05.0033 2026-03-27 Add ability to filter on 3 log entry types (D Ferguson)
 **************************************************************************************/

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
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
 * @version v0.05.0033
 * @since 2019-06-24
 */

public class HG0520Logging extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	private String screenID = "52000"; //$NON-NLS-1$
	private JPanel contents;
	private boolean noneSelected = true;		// for managing table_LogEntries list selections
	private ArrayList<Integer> rowsSelected = new ArrayList<>();

	private JTable table_LogEntries;
	private TableRowSorter<TableModel> logSorter;

	JCheckBox chk_Error, chk_Warning, chk_Message;


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

		JLabel lbl_Select = new JLabel(HG0520Msgs.Text_8);		// Select a log file:
		contents.add(lbl_Select, "cell 0 0"); //$NON-NLS-1$

		JButton btn_Browse = new JButton(HG0520Msgs.Text_10);	// Browse...
		btn_Browse.setToolTipText(HG0520Msgs.Text_11);
		contents.add(btn_Browse, "cell 1 0"); //$NON-NLS-1$

		chk_Error= new JCheckBox(HG0520Msgs.Text_5);		// Display only ERROR entries
		chk_Error.setHorizontalTextPosition(SwingConstants.LEFT);
		chk_Error.setVisible(false);
		contents.add(chk_Error, "cell 1 0, gapx 60"); //$NON-NLS-1$

		chk_Warning = new JCheckBox("WARNING ");	//$NON-NLS-1$
		chk_Warning.setHorizontalTextPosition(SwingConstants.LEFT);
		chk_Warning.setVisible(false);
		contents.add(chk_Warning, "cell 1 0, gapx 10"); //$NON-NLS-1$

		chk_Message= new JCheckBox("MESSAGE ");		//$NON-NLS-1$
		chk_Message.setHorizontalTextPosition(SwingConstants.LEFT);
		chk_Message.setVisible(false);
		contents.add(chk_Message, "cell 1 0, gapx 10"); //$NON-NLS-1$

		JLabel lbl_LogFilename = new JLabel(HG0520Msgs.Text_13);		// Log filename
		contents.add(lbl_LogFilename, "cell 0 2,alignx right"); //$NON-NLS-1$
		JLabel lbl_FilenameData = new JLabel(""); //$NON-NLS-1$
		contents.add(lbl_FilenameData, "cell 1 2"); //$NON-NLS-1$

		JScrollPane scrollPane = new JScrollPane();
		contents.add(scrollPane, "cell 0 3 2 1, grow, push"); //$NON-NLS-1$

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
		table_LogEntries = new JTable() {
		    @Override public boolean isCellEditable(int r, int c) { return false; }
		};
		table_LogEntries.setModel(new DefaultTableModel(
		    new String[][] {},
		    new String[] {HG0520Msgs.Text_32, HG0520Msgs.Text_33,
		                  HG0520Msgs.Text_34, HG0520Msgs.Text_35,
		                  HG0520Msgs.Text_36}
		));


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
				table_LogEntries.getColumnModel().getColumn(2).setMinWidth(75);				//server
				table_LogEntries.getColumnModel().getColumn(2).setPreferredWidth(100);
				table_LogEntries.getColumnModel().getColumn(2).setMaxWidth(200);
				table_LogEntries.getColumnModel().getColumn(3).setMinWidth(80);			//project name
				table_LogEntries.getColumnModel().getColumn(3).setPreferredWidth(100);
				table_LogEntries.getColumnModel().getColumn(4).setMinWidth(400);			//log entry
				table_LogEntries.getColumnModel().getColumn(4).setPreferredWidth(1000);
			}}

		scrollPane.setViewportView(table_LogEntries);
		new tableSizer();
		// Force viewport size
		table_LogEntries.setPreferredScrollableViewportSize(new Dimension(1000, 400));		// W x H
		pack();

/***
 * CREATE ACTION LISTENERS
 **/
		// Browse button - lets user choose a log file to be loaded into GUI table
		btn_Browse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// Disable and make Error, etc chkboxes invisible until new file opened
				chk_Error.setVisible(false);
				chk_Error.setSelected(false);
				chk_Warning.setVisible(false);
				chk_Warning.setSelected(false);
				chk_Message.setVisible(false);
				chk_Message.setSelected(false);
				btn_Browse.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				// Enable FileChooser for hrel files
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
							// Install sorter
							logSorter = new TableRowSorter<>(table_LogEntries.getModel());
							table_LogEntries.setRowSorter(logSorter);
							// Disable auto-resize
							table_LogEntries.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
							// Apply column widths
							new tableSizer();
							// Force viewport to use real table width
							table_LogEntries.setPreferredScrollableViewportSize(new Dimension(1000, 400));	//W x H
							// Repack window
							HG0520Logging.this.pack();
							// then enable function button and Error selection chkboxes
							btn_Output.setEnabled(true);
							chk_Error.setVisible(true);
							chk_Warning.setVisible(true);
							chk_Message.setVisible(true);

					}
			}
		});

		// Error/Warning/Message checkbox listners - when turned on select only those entries
		chk_Error.addActionListener(_ -> applyFilters());
		chk_Warning.addActionListener(_ -> applyFilters());
		chk_Message.addActionListener(_ -> applyFilters());

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
			    Point xy = table_LogEntries.getLocationOnScreen();      // Gets table area location on screen
			    outputWindow.setLocation(xy.x, xy.y);       	        // Sets screen top-left corner relative to that
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

/**
 * applyFilters - handle checkboxes to display only ERROR/WARNING/MESSAGE log entries
 */
	private void applyFilters() {
	    TableRowSorter<?> sorter = (TableRowSorter<?>) table_LogEntries.getRowSorter();
	    boolean showError   = chk_Error.isSelected();
	    boolean showWarning = chk_Warning.isSelected();
	    boolean showMessage = chk_Message.isSelected();

	    // If nothing selected, show all rows
	    if (!showError && !showWarning && !showMessage) {
	        sorter.setRowFilter(null);
	        return;
	    }
	    sorter.setRowFilter(new RowFilter<Object,Object>() {
	        @Override
	        public boolean include(Entry<?, ?> entry) {
	            String value = entry.getStringValue(4); 	// column 4 = log entry text
	            if (value == null) return false;
	            if (showError   && value.contains("ERROR"))   return true;	//$NON-NLS-1$
	            if (showWarning && value.contains("WARNING")) return true;	//$NON-NLS-1$
	            if (showMessage && value.contains("MESSAGE")) return true;	//$NON-NLS-1$
	            return false;
	        }
	    });
	}		// End applyFilters

}	// End HG0520Logging