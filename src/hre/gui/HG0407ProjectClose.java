package hre.gui;
/***********************************************************************************************
 * Project Close - Specification 04.07 GUI_ProjectClose 2020-02-28
 * v0.00.0007 2019-02-25 by R Thompson, updated by D Ferguson
 * v0.00.0008 2019-07-21 Fix close/cancel code (D Ferguson)
 * v0.00.0014 2019-11-18 Setup server/project ArrayLists properly (D Ferguson)
 * 						   and changes for HB0711 static classes (D Ferguson)
 * v0.00.0015 2019-12-07 Updated open project table, implemented close (N. Tolleshaug)
 * v0.00.0019 2020-02-24 Removed project usage/change boxes (D. Ferguson)
 *            2020-03-01 Remove projects from Open projects as they are closed
 *            2020-03-01 Do not close screen until numOpenProjects=0
 * v0.00.0020 2020-03-18 Save project name in lastProjectClosed on close (D. Ferguson)
 * v0.00.0020 2020-03-20 Update list of projects after close project > 1 (N. Tolleshaug)
 *            2020-03-21 Reduced width of table to avoid horizontal scrollbar
 * v0.00.0021 2020-05-03 If closed project was the selectedProject and there are other projects
 *                       open, ensure first of them becomes selectedProject (D Ferguson)
 * v0.00.0022 2020-05-20 fix bug in 0021 change (D Ferguson)
 *            2020-05-22 on project close, close any associated non-modal JDialogs (D Ferguson)
 *            2020-05-23 Update handling of Cancel (N. Tolleshaug)
 *            2020-05-24 fix erroneous log messages (wrong HG04x ID) (D Ferguson)
 *            2020-07-09 execute project close automagically if only 1 project open (D Ferguson)
 *            2020-07-13 check if user REALLY wants to exit on last project close (D Ferguson)
 *            2020-07-10 execute close correctly based on codes set in Main (D Ferguson)
 *            2020-07-27 add screenID for Reminder, Help xref (D Ferguson)
 *            2020-08-02 remove all screens related to the closing project (D Ferguson)
 * v0.01.0023 2020-08-04 changed from JDialog to extend HG0450SuperDialog (D Ferguson)
 *            2020-09-11 removed Reminder icon from Toolbar (D Ferguson)
 *            2020-09-30 changed to MigLayout; fonts removed for JTattoo install (D Ferguson)
 * v0.01.0025 2020-11-23 fix reset of Active project on project close (D Ferguson)
 * 			  2021-01-15 honour 'Backup on close' Setting during project close (D Ferguson)
 * 			  2021-01-28 removed use of HGlobal.selectedProject (N. Tolleshaug)
 * 			  2021-03-09 JOptionPane messages moved to GUI (N. Tolleshaug)
 * 			  2021-04-11 NLS implemented (D Ferguson)
 * v0.01.0026 2021-09-15 Apply tag codes to screen control buttons (D Ferguson)
 * 			  2021-09-27 Removed hidden code related to server login (D Ferguson)
 * v0.01.0027 2022-02-26 Modified to use the NLS version of HGlobal (D Ferguson)
 * 			  2022-05-29 auto-select the project if only 1 project open (D Ferguson)
 * v0.03.0031 2024-10-01 Organize imports (D Ferguson)
 **********************************************************************************************
 * NOTES for incomplete functionality
 ************************************************
 * NOTE09 Code required for optional data validation
 **************************************************/

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
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
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBProjectHandler;
import hre.nls.HG0407Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Project Close
 * @author R Thompson
 * @version v0.03.0031
 * @since 2019-02-25
 */

public class HG0407ProjectClose extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	private String screenID = "40700"; //$NON-NLS-1$
	private JPanel contents;

	private JTable table_Projects;

/**
 * Collect JTable for output by SuperDialog
 * JTable getDataTable()
 */
	@Override
	public JTable getDataTable() {
		return table_Projects;
	}

/**
 * Create the Dialog.
 */
	public HG0407ProjectClose(HBProjectHandler pointProHand) {
		// Setup references for HG0450
		windowID = screenID;
		helpName = "closeproject"; //$NON-NLS-1$

		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0407 Project Close"); 		 //$NON-NLS-1$
		setResizable(false);
		setTitle(HG0407Msgs.Text_3);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		contents = new JPanel();
		contents.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]", "[]10[]20[]20[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
		toolBar.add(Box.createHorizontalGlue());
		// Add icons defined in HG0450
		toolBar.add(btn_Outputicon);
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north"); //$NON-NLS-1$

		JLabel lbl_Projects = new JLabel(HG0407Msgs.Text_8);
		contents.add(lbl_Projects, "cell 0 0"); //$NON-NLS-1$

		JScrollPane scrollPane = new JScrollPane();
		contents.add(scrollPane, "cell 0 1"); //$NON-NLS-1$

		JLabel lbl_SelectedProject = new JLabel(""); //$NON-NLS-1$
		lbl_SelectedProject.setHorizontalTextPosition(SwingConstants.LEFT);
		lbl_SelectedProject.setHorizontalAlignment(SwingConstants.LEFT);
		contents.add(lbl_SelectedProject, "cell 0 2"); //$NON-NLS-1$

		JButton btn_Summary = new JButton(HG0407Msgs.Text_13);
		btn_Summary.setEnabled(false);
		btn_Summary.setToolTipText(HG0407Msgs.Text_14);
		contents.add(btn_Summary, "cell 0 3"); //$NON-NLS-1$

		JButton btn_DataValidation = new JButton(HG0407Msgs.Text_16);
		btn_DataValidation.setToolTipText(HG0407Msgs.Text_17);
		btn_DataValidation.setEnabled(false);
		contents.add(btn_DataValidation, "cell 0 3, gapleft push"); //$NON-NLS-1$

		JButton btn_Backup = new JButton(HG0407Msgs.Text_19);
		btn_Backup.setEnabled(false);
		contents.add(btn_Backup, "cell 0 3, gapleft push"); //$NON-NLS-1$

		JButton btn_Cancel = new JButton(HG0407Msgs.Text_21);
		btn_Cancel.setToolTipText(HG0407Msgs.Text_22);
		contents.add(btn_Cancel, "cell 0 3, gapleft push, tag cancel"); //$NON-NLS-1$

		JButton btn_CloseProject = new JButton(HG0407Msgs.Text_24);
		btn_CloseProject.setEnabled(false);
		btn_CloseProject.setToolTipText(HG0407Msgs.Text_25);
		contents.add(btn_CloseProject, "cell 0 3, gapleft push, tag ok"); //$NON-NLS-1$

		// Setup a Project table as non-editable by the user and set TableModel
		table_Projects = new JTable() { private static final long serialVersionUID = 1L;
										@Override
										public boolean isCellEditable(int row, int column) {
											return false;}};
		table_Projects.setModel(new DefaultTableModel(
			pointProHand.getOpenProjectArray2D(),
			HGlobalCode.projectTableHeader()));
		table_Projects.getColumnModel().getColumn(0).setMinWidth(150);
		table_Projects.getColumnModel().getColumn(0).setPreferredWidth(200);
		table_Projects.getColumnModel().getColumn(1).setMinWidth(150);
		table_Projects.getColumnModel().getColumn(1).setPreferredWidth(200);
		table_Projects.getColumnModel().getColumn(2).setMinWidth(100);
		table_Projects.getColumnModel().getColumn(2).setPreferredWidth(130);
		table_Projects.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		// Set the ability to sort on columns and presort column 0
		table_Projects.setAutoCreateRowSorter(true);
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table_Projects.getModel());
		table_Projects.setRowSorter(sorter);
		List <RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
		sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(sortKeys);

		// Set tooltips, column widths and header format
		table_Projects.getTableHeader().setToolTipText(HG0407Msgs.Text_27);
		JTableHeader pHeader = table_Projects.getTableHeader();
		pHeader.setOpaque(false);
		TableCellRenderer rendererFromHeader = table_Projects.getTableHeader().getDefaultRenderer();
		JLabel headerLabel = (JLabel) rendererFromHeader;
		headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
		ListSelectionModel cellSelectionModel = table_Projects.getSelectionModel();
		cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		table_Projects.setPreferredScrollableViewportSize(new Dimension(530, 100));
		scrollPane.setViewportView(table_Projects);

		pack();

/**
* ACTION BUTTON LISTENERS
**/
		btn_Summary.addActionListener(new ActionListener() {
			//Displays properties of the Selected Project
			@Override
			public void actionPerformed(ActionEvent arg0) {
                int[] selectedRow = table_Projects.getSelectedRows();
                String [][] summaryData = null;
				for (int i = 0; i < selectedRow.length; i++) {
					String selectedProjectName = (String) table_Projects.getValueAt(selectedRow[i], 0);
					try {
						summaryData = pointProHand.getSummaryOpenProjectAction(selectedProjectName);
					} catch (HBException hbe) {
						JOptionPane.showMessageDialog(null, HG0407Msgs.Text_28
								+  hbe.getMessage(), HG0407Msgs.Text_29,JOptionPane.ERROR_MESSAGE);
					}
				}
				if (summaryData != null) {
					HG0414ProjectSummary summscreen = new HG0414ProjectSummary(summaryData);
					summscreen.setModalityType(ModalityType.APPLICATION_MODAL);
					Point xy = btn_Summary.getLocationOnScreen();          	  // Gets Summary button location on screen
					summscreen.setLocation(xy.x + 50, xy.y);     			  // Sets Summary screen top-left corner relative to that
					summscreen.setVisible(true);
				} else if (HGlobal.DEBUG) System.out.println("Summary data is null"); //$NON-NLS-1$
			}
		});

		btn_DataValidation.addActionListener(new ActionListener() {
			// NOTE09 To run the data validation module eventually
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(btn_DataValidation, HG0407Msgs.Text_31, HG0407Msgs.Text_32,JOptionPane.INFORMATION_MESSAGE);
			}
		});

		btn_Backup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

			// Initiate project Backup and close
				HG0405ProjectBackup backupWindow = new HG0405ProjectBackup(pointProHand);
				backupWindow.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xy = btn_Backup.getLocationOnScreen();		  //Get button location (top-left corner)
				backupWindow.setLocation(xy.x, xy.y);			 	  //Set location relative to Project Backup
				backupWindow.setVisible(true);
			// Close Project Close window
				dispose();
			}
		});

		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				HGlobal.closeType = "N";	 //$NON-NLS-1$
				if (HGlobal.showCancelmsg) {
					if (JOptionPane.showConfirmDialog(btn_Cancel, HG0407Msgs.Text_34, HG0407Msgs.Text_35,
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: cancelling HG0407 Project Close");	 //$NON-NLS-1$
							dispose();
						}	// yes option - return to main menu
				} else {
					if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: cancelling HG0407 Project Close");}	 //$NON-NLS-1$
					dispose();
				}
			}
		});

		// Listener for clicking 'X on screen - make same as Cancel button
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
		    	btn_Cancel.doClick();
		    }
		});

		btn_CloseProject.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				HGlobal.closeType = "N";	 //$NON-NLS-1$
                int[] selectedRow = table_Projects.getSelectedRows();
				for (int i = 0; i < selectedRow.length; i++) {
					String selectedProjectName = (String) table_Projects.getValueAt(selectedRow[i], 0);
					int errorCode = pointProHand.closeProjectAction(selectedProjectName);
					if (errorCode == 0) {
						if (HGlobal.DEBUG) userInfoCloseProject(0, selectedProjectName);

						// Display close confirmed message if showCancelmsg is true
		                if (HGlobal.showCancelmsg) JOptionPane.showMessageDialog(btn_CloseProject,
		                		HG0407Msgs.Text_39+ selectedProjectName+HG0407Msgs.Text_40, HG0407Msgs.Text_41,JOptionPane.INFORMATION_MESSAGE);

		                // Clean out Person 'Recently Used' list
		                HG0401HREMain.mainFrame.removeRecents();

		                // Selected project now closed, so disable Close Project button
		                btn_CloseProject.setEnabled(false);

						// As we close each project, check the Backup on Close setting - confirm backup
						// is still wanted and initiate a Backup of the project if answer = yes
						if (HGlobal.backupActivProject)
							{ int confirm = JOptionPane.showConfirmDialog (btn_CloseProject, HG0407Msgs.Text_42+selectedProjectName+"?", //$NON-NLS-1$
									HG0407Msgs.Text_44,JOptionPane.YES_NO_OPTION);
					    	  if(confirm == JOptionPane.YES_OPTION) {
								  String newBackupName = selectedProjectName + HG0407Msgs.Text_45 + pointProHand.currentTime("_yyyy-MM-dd_HH-mm-ss"); //$NON-NLS-1$
								  newBackupName = newBackupName.replace(":", "-");	// get rid of illegal colon character in time	 //$NON-NLS-1$ //$NON-NLS-2$
								  if (HGlobal.pathHREbackups.endsWith(File.separator))
										  newBackupName = HGlobal.pathHREbackups + newBackupName;
								  else newBackupName = HGlobal.pathHREbackups + File.separator + newBackupName;
								  errorCode = pointProHand.backupProjectAction(selectedProjectName, newBackupName);
								  if (errorCode == 0) userInfoCloseProject(errorCode, selectedProjectName);
								  else if (errorCode == 1) {
									  errorCode = 2;
									  userInfoCloseProject(errorCode, selectedProjectName);
								  }
					    	  	}
							}

						// Reset global last project closed
						HGlobal.lastProjectClosed = selectedProjectName;

						// Log action
						if (HGlobal.writeLogs)
								HB0711Logging.logWrite("Action: closing project " + selectedProjectName + " in HG0407ProjectClose");	//$NON-NLS-1$ //$NON-NLS-2$

						// Empty displayed table_Projects in this dialog
						DefaultTableModel model =  (DefaultTableModel)table_Projects.getModel();
						model.setRowCount(0);
						// and reload it for re-display
						table_Projects.setModel(new DefaultTableModel(
								pointProHand.getOpenProjectArray2D(),
								HGlobalCode.projectTableHeader()));
						table_Projects.getColumnModel().getColumn(0).setMinWidth(150);
						table_Projects.getColumnModel().getColumn(0).setPreferredWidth(200);
						table_Projects.getColumnModel().getColumn(1).setMinWidth(150);
						table_Projects.getColumnModel().getColumn(1).setPreferredWidth(200);
						table_Projects.getColumnModel().getColumn(2).setMinWidth(100);
						table_Projects.getColumnModel().getColumn(2).setPreferredWidth(130);
						table_Projects.setPreferredScrollableViewportSize(new Dimension(530, 100));
						// Reset selected project text
						lbl_SelectedProject.setVisible(false);
						if (HGlobal.numOpenProjects == 0) {
									HG0401HREMain.mainFrame.setStatusProject(HG0407Msgs.Text_51);
									dispose();
					     }
						if (HGlobal.numOpenProjects == 1)
							table_Projects.setRowSelectionInterval(0,0);	// automatically select 1st (only) row of table

					} else if (errorCode == 2) {
	                	userInfoCloseProject(errorCode, selectedProjectName);
	                }
				}
			}
		});

	//Project Table selection
		cellSelectionModel.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				String selectedProject = null;
				int[] selectedRow = table_Projects.getSelectedRows();
				for (int i = 0; i < selectedRow.length; i++) {

				//get the Project name irrespective of whether or not Server or Project Name was selected in the table
					selectedProject = (String) table_Projects.getValueAt(selectedRow[i], 0);	// Projectname in col 0

				//put the Project name into the 'ProjectName label'
					lbl_SelectedProject.setText(HG0407Msgs.Text_52+selectedProject);
					lbl_SelectedProject.setVisible(true);

				//enable the Summary and BACKUP TO (Browse) buttons
					btn_Summary.setEnabled(true);
					btn_DataValidation.setEnabled(true);
					btn_Backup.setEnabled(true);
					btn_CloseProject.setEnabled(true);
				}
			}
		});

/**
 * EXECUTION CODE
 *
 * HG0407ProjectClose may be invoked from 3 possible paths from HG0401HREMain:
 * 	 1. clicking 'X' -> ExitHRE -> menuProjClose (in which case closeType = "X")
 *   2. clicking ExitHRE -> menuProjClose (in which case closeType = "E")
 *   3. clicking menuProjClose (in which case closeType = "C")
 * If closeType = "X" and numOpenProjects = 1; this code will be running with no screen showing
 *    so we confirm the HRE Exit action (if cancel messages are to be shown) and close and exit
 **/
		if (HGlobal.closeType.equals("X") & HGlobal.numOpenProjects == 1)  //$NON-NLS-1$
				{   HGlobal.closeType = "N";						// reset the closeType just in case not an exit	 //$NON-NLS-1$
				    // Check the user REALLY wants to exit (if cancel msgs are shown)
					if (HGlobal.showCancelmsg) {
												if (JOptionPane.showConfirmDialog(null, HG0407Msgs.Text_55, HG0407Msgs.Text_56,
														JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
													{return;}		// return if answer is NO, else proceed with silent close and exit
											   }
					table_Projects.setRowSelectionInterval(0,0);	// automatically select 1st (only) row of table
					btn_CloseProject.doClick();						// execute the closeproject code
					return;											// and return
				}

	// If closeType = "C" and numOpenProjects = 1 then automatically select the only project showing
		if (HGlobal.closeType.equals("C") & HGlobal.numOpenProjects == 1)  //$NON-NLS-1$
					table_Projects.setRowSelectionInterval(0,0);	// automatically select 1st (only) row of table

	} // End HG0407 constructor

/**
 * GUI user messages
 * @param errorCode
 * @param projectName
 */
	private void userInfoCloseProject(int errorCode, String projectName) {
		String errorMess = ""; //$NON-NLS-1$
		String errorTitle = HG0407Msgs.Text_58;
		if (errorCode == 0) {
			errorMess = HG0407Msgs.Text_59;
			JOptionPane.showMessageDialog(null, errorMess + projectName, errorTitle, JOptionPane.INFORMATION_MESSAGE);
		}
		if (errorCode > 0) {
			if (errorCode == 1)  errorMess = HG0407Msgs.Text_60 + projectName + HG0407Msgs.Text_61;
			if (errorCode == 2)  errorMess = HG0407Msgs.Text_62 + projectName + HG0407Msgs.Text_63;
			JOptionPane.showMessageDialog(null, errorMess, errorTitle, JOptionPane.ERROR_MESSAGE);
		}
	}	// End userInfoCloseProject

}	//End HG0407ProjectClose