package hre.gui;
/****************************************************************************************
 * Configure Table - Specification 05.81 Configure Columns 2018-09-22
 * v0.01.0025 2020-11-27 first draft (D Ferguson)
 * 			  2021-02-07 Implemented column control HG0581ConfigureTable (N. Tolleshaug)
 * 			  2021-02-09 HG0581ConfigureTable edit and class problem fixed (N. Tolleshaug)
 * v0.01.0026 2021-09-15 Apply tag codes to screen control buttons (D Ferguson)
 * v0.01.0027 2022-02-28 Converted to NLS (D Ferguson)
 * 			  2022-03-07 Modified to use HGloDataMsgs (D Ferguson) (now removed)
 * v0.01.0028 2023-01-08 Translated table headers collected from T204 (N. Tolleshaug) 
 * 			  2023-01-15 Translated texts collected from T204 (N. Tolleshaug) 
 * 			  2023-01-28 Remove ability to edit column names (D Ferguson)
 ***************************************************************************************/

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.nls.HG0581Msgs;

import net.miginfocom.swing.MigLayout;

/**
 * Configure Table
 * @author D Ferguson
 * @version v0.01.0028
 * @since 2020-11-27
 */

public class HG0581ConfigureTable extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	private String screenID = "58100"; //$NON-NLS-1$
	private JPanel contents;
	private JTable tableColumns;
	public String callerScreenID;
	private String staticText;

/**
 * Create the dialog.
 * @tablecallerID	the String identifying the calling screen
 * @tableControl	an Object containing the column heading names and status
 * @throws HBException
 */
	public HG0581ConfigureTable(String tablecallerID, Object [][] tableControl, HBProjectOpenData pointOpenProject)  {

		// Setup references for HG0450
		windowID = screenID;
		helpName = "configuretable";	 //$NON-NLS-1$
		callerScreenID = tablecallerID;
		
		//HBPersonHandler pointPersonHandler = (HBPersonHandler)HG0401HREMain.pointBusinessLayer[1];
		HBPersonHandler pointPersonHandler = pointOpenProject.getPersonHandler();
		
		if (HGlobal.DEBUG) 
			System.out.println("Table control: 0-" 														//$NON-NLS-1$
					+ Arrays.toString(pointPersonHandler.setTranslatedData(screenID, "0", false)));		//$NON-NLS-1$
			
		// Collect static text from T204 	
		staticText = pointPersonHandler.setTranslatedData(screenID, "0", false)[0];		//$NON-NLS-1$

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: entering HG0581 Configure Table");} //$NON-NLS-1$
	    setTitle(HG0581Msgs.Text_3);		// Configure Table Columns

		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[grow]", "[][]10[grow]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_Guide = new JLabel(HG0581Msgs.Text_7);		// Select the columns in the displayed table
//		JLabel lbl_Guide2 = new JLabel(HG0581Msgs.Text_8);		// You may also edit the column names
				
		contents.add(lbl_Guide, "cell 0 0"); //$NON-NLS-1$
//		contents.add(lbl_Guide2, "cell 0 1"); //$NON-NLS-1$

		JScrollPane tableScrollPane = new JScrollPane();
		contents.add(tableScrollPane, "cell 0 2, grow"); //$NON-NLS-1$

		JButton btn_Cancel = new JButton(HG0581Msgs.Text_12);		// Cancel
		contents.add(btn_Cancel, "cell 0 3, align right, gapx 20, tag cancel"); //$NON-NLS-1$

		JButton btn_Accept = new JButton(HG0581Msgs.Text_14);		// Accept
		contents.add(btn_Accept, "cell 0 3, align right, gapx 20, tag ok");	 //$NON-NLS-1$

		// Setup renderer
	    DefaultTableCellRenderer stringRenderer = new DefaultTableCellRenderer();
	    stringRenderer.setHorizontalAlignment( SwingConstants.CENTER );

		//Define tableColumns layout
		tableColumns = new JTable() {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int column) {
			     switch (column) {
		         case 0:
		        	 return false;
		         case 1:
		        	 if (row >= 2) return true; else return false;
		         default:
		             return false;
		      }
			}
			@Override
			public Class<?> getColumnClass(int column) {
		        if (column == 1) return Boolean.class;
		        else return String.class;
			}
			@Override
			public TableCellRenderer getCellRenderer(int row, int column) {
		        if ((row == 0 || row == 1) && column == 1) return stringRenderer;
		        	else return super.getCellRenderer(row, column);
		    }
		};
		
		if (HGlobal.DEBUG) 
			System.out.println("Table control: 1-" 													//$NON-NLS-1$
					+ Arrays.toString(pointPersonHandler.setTranslatedData(screenID, "1", false))); //$NON-NLS-1$
		
		tableControl[0][1]= staticText;			// staticText from T204
		tableControl[1][1]= staticText;			// staticText from T204
		tableColumns.setModel(new DefaultTableModel(
				tableControl,
				pointPersonHandler.setTranslatedData(screenID, "1", false)		//$NON-NLS-1$
				));

		tableScrollPane.setViewportView(tableColumns);
		tableColumns.setFillsViewportHeight(true);
		tableColumns.getColumnModel().getColumn(0).setMinWidth(200);
		tableColumns.getColumnModel().getColumn(0).setPreferredWidth(200);
		tableColumns.getColumnModel().getColumn(1).setMinWidth(70);
		tableColumns.getColumnModel().getColumn(1).setPreferredWidth(70);
		tableColumns.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableColumns.setPreferredScrollableViewportSize(new Dimension(270, 350));

	    // Set header format
		TableCellRenderer rendererFromHeader = tableColumns.getTableHeader().getDefaultRenderer();
		JLabel headerLabel = (JLabel) rendererFromHeader;
		headerLabel.setHorizontalAlignment(SwingConstants.LEFT);

		// Set selection model
		ListSelectionModel selectionModel = tableColumns.getSelectionModel();
		selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		pack();

/***
 * CREATE ACTION LISTENERS
 **/
		// Listener for Accept button
		btn_Accept.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(null != tableColumns.getCellEditor()) {
				    // there is an edit in progress
					tableColumns.getCellEditor().stopCellEditing();
				}
				for (int i = 0; i < tableControl.length; i++ ) {
					tableControl[i][0] = tableColumns.getModel().getValueAt(i,0);
					tableControl[i][1] = tableColumns.getModel().getValueAt(i,1);
				}

				if (callerScreenID.equals("50700")) { //$NON-NLS-1$
		// Reload personSelector data, in Front
					pointOpenProject.setReloadPersonSelectData(true);
					pointOpenProject.getPersonHandler().initiatePersonSelect(pointOpenProject, "F");	//$NON-NLS-1$
				}

				if (callerScreenID.equals("50750")) { //$NON-NLS-1$
		// Reload location select data
					pointOpenProject.reloadLocationSelectData = true;
					pointOpenProject.getWhereWhenHandler().initiateLocationSelect(pointOpenProject);
				}

				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: accepting changes and leaving HG0581ConfigureTable"); //$NON-NLS-1$
				dispose();
			}
		});

		// Listener for Cancel button
		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: cancelling HG0581ConfigureTable"); //$NON-NLS-1$
				dispose();
			}
		});

		// Listener for clicking 'X' on screen
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
				if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: exiting HG0581ConfigureTable");} //$NON-NLS-1$
				dispose();	}		// return to main menu
		});

	}	// End HG0581ConfigureTable constructor

}  // End of HG0581ConfigureTable
