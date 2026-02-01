package hre.gui;
/******************************************************************************************
 * Project Admin code - Specification 04.03 GUI_ProjectAdmin 2020-03-03
 * v0.00.0005 2019-02-10 by R Thompson, updated by D Ferguson
 * v0.00.0008 2019-07-21 fix close exit code (D Ferguson)
 * v0.00.0014 2019-11-18 changes for userProjects being ArrayList (D Ferguson)
 * 						 and changes for HB0711 static classes (D Ferguson)
 * v0.00.0015 2019-12-07 changed code for HG0514 call (D Ferguson)
 * v0.00.0018 2020-02-16 implemented calls to HBToolHandler (N. Tolleshaug)
 *            2020-02-23 corrected double action on select user (N. Tolleshaug)
 * v0.00.0019 2020-03-07 added base login code; changed screen layout (D.Ferguson)
 *                       made server related buttons not visible (temporary change)
 * 						 emptied table_Users on project selection
 *                       added prompts for User Add/Change information
 * v0.00.0019 2020-03-12 added call to handler update user data. (N. Tolleshaug)
 * 						 corrected adding correct row in table. (N. Tolleshaug)
 * v0.00.0022 2020-07-27 add screenID for Reminder, Help xref (D Ferguson)
 * v0.01.0023 2020-08-04 changed from JDialog to extend HG0450SuperDialog (D Ferguson)
 *            2020-09-11 removed Reminder icon from Toolbar (D Ferguson)
 *            2020-09-30 changed to MigLayout; fonts removed for JTattoo install (D Ferguson)
 * v0.01.0025 2020-11-24 use location Setting for default HRE file folder (D Ferguson)
 * 			  2021-01-28 removed use of HGlobal.selectedProject (N. Tolleshaug)
 * 			  2021-03-22 implemented JOptionPane from Tools (N. Tolleshaug)
 * 			  2021-03-23 fixed database open/close database index error (N. Tolleshaug)
 * v0.01.0026 2021-05-05 fix NPE on Browse; fix long file/foldername lbl issue (D Ferguson)
 * 			  2021-06-24 revise user add/delete processes (D Ferguson)
 * 			  2021-06-30 Edit/add/delete handling rewritten (N. Tolleshaug)
 * 			  2021-07-01 Update if cells changed (N. Tolleshaug)
 * v0.01.0027 2021-10-10 Update password in T131 (D Ferguson)
 * 			  2021-10-10 New password dialog without showing password (N. Tolleshaug)
 *			  2022-02-26 Modified to use the NLS version of HGlobal (D Ferguson)
 * v0.01.0028 2023-03-06 Converted to NLS (D Ferguson)
 * v0.03.0031 2024-10-01 Revise complex IFs (D Ferguson)
 * 			  2024-11-29 Replace JoptionPane 'null' locations with 'contents' (D Ferguson)
 * 			  2024-11-30 Fix crash if cancel out of Browse action (D Ferguson)
 * v0.04.0032 2025-03-26 Add display of IS_OWNER setting (D Ferguson)
 * 			  2025-03-27 Reject duplicate and illegal LogonID entries (D Ferguson)
 * 			  2025-05-08 Complete IS_OWNER update code (D Ferguson)
 * 			  2026-01-02 Logged catch block and DEBUG actions (D Ferguson)
 ******************************************************************************************/

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
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
import javax.swing.JPasswordField;
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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBToolHandler;
import hre.dbla.HDException;
import hre.nls.HG0403Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Project Admin
 * @author R Thompson
 * @version v0.04.0032
 * @since 2019-02-10
 */

public class HG0403ProjectUserAdmin extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	private String screenID = "40300"; //$NON-NLS-1$
	private final JPanel contents;
	private JTable table_Users;
	private int tableUserRowSelected = -1;	// not selected
	private boolean unsavedChanges = false;
	private boolean cellEdited = false;
	private String passWord = null;
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
 * Constructor for Create the Dialog.
 */
	public HG0403ProjectUserAdmin(HBToolHandler pointToolHand) {
		// Setup references for HG0450
		windowID = screenID;
		helpName = "admin"; //$NON-NLS-1$

		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0403 Project Admin"); //$NON-NLS-1$
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setTitle(HG0403Msgs.Text_3);		// Project User Administration

		contents = new JPanel();
		contents.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]20[]", "[]10[]10[]10[]10[]10[]10[]20[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.add(Box.createHorizontalGlue());

		// Add icons defined in HG0450
		toolBar.add(btn_Outputicon);
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north"); //$NON-NLS-1$

		JLabel lbl_Projects = new JLabel(HG0403Msgs.Text_8);		// Known Projects
		lbl_Projects.setFont(lbl_Projects.getFont().deriveFont(lbl_Projects.getFont().getStyle() | Font.BOLD));
		contents.add(lbl_Projects, "cell 0 0 2"); 	//$NON-NLS-1$

		JScrollPane projectScrollPane = new JScrollPane();
		contents.add(projectScrollPane, "cell 0 1 3"); 	//$NON-NLS-1$

		JLabel lbl_ProjectSearch = new JLabel(HG0403Msgs.Text_11);		// Project Search
		contents.add(lbl_ProjectSearch, "cell 0 2"); 	//$NON-NLS-1$

		JButton btn_Browse = new JButton(HG0403Msgs.Text_13);	// Browse...
		btn_Browse.setToolTipText(HG0403Msgs.Text_14);			// Browse the file explorer for projects
		contents.add(btn_Browse, "cell 1 2"); 	//$NON-NLS-1$

		JLabel lbl_Filename = new JLabel(HG0403Msgs.Text_16);	// Filename:
		contents.add(lbl_Filename, "cell 0 3, alignx right"); 	//$NON-NLS-1$
		JLabel lbl_FilenameData = new JLabel(""); 				//$NON-NLS-1$
		lbl_FilenameData.setMaximumSize(new Dimension(300, 23));
		contents.add(lbl_FilenameData, "cell 1 3"); 			//$NON-NLS-1$

		JLabel lbl_FolderName = new JLabel(HG0403Msgs.Text_20);	// Folder Name:
		contents.add(lbl_FolderName, "cell 0 4, alignx right"); //$NON-NLS-1$
		JLabel lbl_FolderNameData = new JLabel(""); 			//$NON-NLS-1$
		lbl_FolderNameData.setMaximumSize(new Dimension(400, 23));
		contents.add(lbl_FolderNameData, "cell 1 4"); 			//$NON-NLS-1$

		JLabel lbl_Users = new JLabel(HG0403Msgs.Text_24);		// Defined Users
		lbl_Users.setFont(lbl_Users.getFont().deriveFont(lbl_Users.getFont().getStyle() | Font.BOLD));
		contents.add(lbl_Users, "cell 0 5"); 					//$NON-NLS-1$

		JButton btn_AddUser = new JButton(HG0403Msgs.Text_26);	//   Add
		btn_AddUser.setToolTipText(HG0403Msgs.Text_27);			// Opens prompts to add a new User
		btn_AddUser.setEnabled(false);
		contents.add(btn_AddUser, "cell 1 5, alignx right"); 	//$NON-NLS-1$

		JButton btn_DeleteUser = new JButton(HG0403Msgs.Text_29);	//  Delete
		btn_DeleteUser.setToolTipText(HG0403Msgs.Text_30);		// Deletes the currently selected User from the User List
		btn_DeleteUser.setEnabled(false);
		contents.add(btn_DeleteUser, "cell 1 5, gapx 20"); 		//$NON-NLS-1$

		JButton btn_PassWord = new JButton(HG0403Msgs.Text_32);	// Set password
		btn_PassWord.setToolTipText(HG0403Msgs.Text_33);		// Sets a new password for this user
		btn_PassWord.setEnabled(false);
		contents.add(btn_PassWord, "cell 1 5, gapx 20"); 		//$NON-NLS-1$

		JScrollPane userScrollPane = new JScrollPane();
		contents.add(userScrollPane, "cell 0 6 2, growx"); 		//$NON-NLS-1$

		JButton btn_Cancel = new JButton(HG0403Msgs.Text_36);	// Cancel
		btn_Cancel.setToolTipText(HG0403Msgs.Text_37);			// Cancels any changes and closes the window
		contents.add(btn_Cancel, "cell 1 7, align right, gapx 20, tag cancel"); //$NON-NLS-1$

		JButton btn_Done = new JButton(HG0403Msgs.Text_39);		// Done
		btn_Done.setToolTipText(HG0403Msgs.Text_40);			// Closes the window
		contents.add(btn_Done, "cell 1 7, align right, gapx 20, tag ok"); //$NON-NLS-1$

	// Setup Users table as editable by the user and set Model
		table_Users = new JTable() {
			private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int row, int col) {
					if (col == 0) return false;
					return true;
				}
				@Override
				public Class<?> getColumnClass(int col) {
				    Class<?> classType = String.class;
				    if (col == 3) classType = Boolean.class;
				    return classType;
				}
			};
		table_Users.setModel(new DefaultTableModel(
								new String[][] {  },
								new String[] {HG0403Msgs.Text_42, HG0403Msgs.Text_43, 		// Logon ID, User's Name
											  HG0403Msgs.Text_44, HG0403Msgs.Text_50}));			// Email, Is Owner?
		table_Users.getColumnModel().getColumn(0).setMinWidth(80);
		table_Users.getColumnModel().getColumn(0).setPreferredWidth(100);
		table_Users.getColumnModel().getColumn(1).setMinWidth(80);
		table_Users.getColumnModel().getColumn(1).setPreferredWidth(100);
		table_Users.getColumnModel().getColumn(2).setMinWidth(100);
		table_Users.getColumnModel().getColumn(2).setPreferredWidth(120);
		table_Users.getColumnModel().getColumn(3).setMinWidth(30);
		table_Users.getColumnModel().getColumn(3).setPreferredWidth(30);
	// Set the ability to sort on columns and presort column 0
		table_Users.setAutoCreateRowSorter(true);
		TableRowSorter<TableModel> userSorter = new TableRowSorter<TableModel>(table_Users.getModel());
		table_Users.setRowSorter(userSorter);
		List <RowSorter.SortKey> userSortKeys = new ArrayList<RowSorter.SortKey>();
		userSortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		userSorter.setSortKeys(userSortKeys);

	// Set tooltips and header format
		table_Users.getTableHeader().setToolTipText(HG0403Msgs.Text_45);	// Click to sort; Click again to sort in reverse order
		JTableHeader userHeader = table_Users.getTableHeader();
		userHeader.setOpaque(false);
		TableCellRenderer userHeaderRenderer = table_Users.getTableHeader().getDefaultRenderer();
		JLabel userHeaderLabel = (JLabel) userHeaderRenderer;
		userHeaderLabel.setHorizontalAlignment(SwingConstants.LEFT);
		ListSelectionModel userListModel = table_Users.getSelectionModel();
		userListModel.setValueIsAdjusting(true);
		userListModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table_Users.setPreferredScrollableViewportSize(new Dimension(530, 100));
		userScrollPane.setViewportView(table_Users);

	// Setup a Project table as non-editable by the user and set List Model
		table_Projects = new JTable() { private static final long serialVersionUID = 1L;
										@Override
										public boolean isCellEditable(int row, int column) {
											return false;}};
		table_Projects.setModel(new DefaultTableModel(
									pointToolHand.getKnownProjectArray2D(),
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
		TableRowSorter<TableModel> projectSorter = new TableRowSorter<TableModel>(table_Projects.getModel());
		table_Projects.setRowSorter(projectSorter);
		List <RowSorter.SortKey> projSortKeys = new ArrayList<RowSorter.SortKey>();
		projSortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		projectSorter.setSortKeys(projSortKeys);

	// Set tooltips and header format
		table_Projects.getTableHeader().setToolTipText(HG0403Msgs.Text_45);	// Click to sort; Click again to sort in reverse order
		JTableHeader projHeader = table_Projects.getTableHeader();
		projHeader.setOpaque(false);
		TableCellRenderer projHeaderRenderer = table_Projects.getTableHeader().getDefaultRenderer();
		JLabel projHeaderLabel = (JLabel) projHeaderRenderer;
		projHeaderLabel.setHorizontalAlignment(SwingConstants.LEFT);
		ListSelectionModel projectListModel = table_Projects.getSelectionModel();
		projectListModel.setValueIsAdjusting(true);
		projectListModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table_Projects.setPreferredScrollableViewportSize(new Dimension(530, 100));
		projectScrollPane.setViewportView(table_Projects);

		pack();

/******************
 * CREATE LISTENERS
 ******************/
	// Listener for file Browse action
		btn_Browse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				btn_Browse.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				HG0577FileChooser chooseFile = new HG0577FileChooser("Open",			 //$NON-NLS-1$
																	 HG0403Msgs.Text_48, // HRE Project files (*.db)
																	 "db", 				//$NON-NLS-1$
																	  "", 				//$NON-NLS-1$
																	 HGlobal.pathHRElocation, 1);
				chooseFile.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xy = btn_Browse.getLocationOnScreen();
				chooseFile.setLocation(xy.x, xy.y);
				chooseFile.setVisible(true);
				btn_Browse.setCursor(Cursor.getDefaultCursor());
			// Get file and folder-names returned from file chooser
				HGlobal.projectFolder = HGlobal.chosenFolder;
				HGlobal.projectFilename = HGlobal.chosenFilename;
				HGlobal.projectFilename = HGlobal.projectFilename.replace(".mv.db", " "); //$NON-NLS-1$ //$NON-NLS-2$
				HGlobal.projectFilename = HGlobal.projectFilename.trim();
				lbl_FilenameData.setText(HGlobal.projectFilename);   	// set filename
				lbl_FolderNameData.setText(HGlobal.projectFolder); 		// set path
				if (HGlobal.projectFilename.equals("")) return;	//$NON-NLS-1$	// return if cancelled out of folderchooser

				try {
				// Check if editor active and stop editing
					if (table_Users.getCellEditor() != null) table_Users.getCellEditor().stopCellEditing();
				// Save user data from edited row
					if (cellEdited) saveUsedData(pointToolHand,tableUserRowSelected);
					cellEdited = false;
				// Close any DB that are open; open this one
					pointToolHand.closeDatabaseConnection();
					pointToolHand.openDatabase(HGlobal.projectFolder, HGlobal.projectFilename);
				// Populate user table from project data
					DefaultTableModel model = (DefaultTableModel) table_Users.getModel();
					model.setDataVector(pointToolHand.presentTableUsersT131(),
							new String[] {HG0403Msgs.Text_42,HG0403Msgs.Text_43, HG0403Msgs.Text_44});	// Logon ID, User's Name, E-mail
				// set button visibility
					btn_AddUser.setEnabled(true);
					btn_DeleteUser.setEnabled(true);
				} catch (HBException | HDException hbe) {
					JOptionPane.showMessageDialog(contents, "User table loading error:\n" //$NON-NLS-1$
							+  hbe.getMessage(),
							"Administration error",JOptionPane.ERROR_MESSAGE); 		//$NON-NLS-1$
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0403 loading User table " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
			}
		});

		// Listener for clicking 'X on screen - make same as Close button
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
		    	btn_Cancel.doClick();
		    }
		});

		// Listener for Cancel button - check unsavedChanges first
		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// Ensure any edits in progress are completed
				if (null != table_Users.getCellEditor()) {
					table_Users.getCellEditor().stopCellEditing();
					unsavedChanges = true;
					}
				if (unsavedChanges) {
					if (JOptionPane.showConfirmDialog(btn_Cancel, HG0403Msgs.Text_59, 	// You have unsaved changes.\nDo you want to exit without saving?
														HG0403Msgs.Text_60,				// User Administration
														JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
						return;
					if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: closing HG0403ProjectAdmin without saving"); //$NON-NLS-1$
					// Close any open DBs first
					try {
						 pointToolHand.closeDatabaseConnection();
					 } catch (HBException hbe) {
							JOptionPane.showMessageDialog(contents, "Database close error \n"  //$NON-NLS-1$
									+  hbe.getMessage(),
									"Administration error",JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
							if (HGlobal.writeLogs) {
								HB0711Logging.logWrite("ERROR: in HG0403 closing database " + hbe.getMessage()); //$NON-NLS-1$
								HB0711Logging.printStackTraceToFile(hbe);
							}
					 }
					dispose();
					}
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: cancelling HG0403ProjectAdmin"); //$NON-NLS-1$
				dispose();
			}
		});

		// Done button listener
		btn_Done.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: closing HG0403 Project Admin"); //$NON-NLS-1$
				try {
				// Check if editor active and stop editing
					if (table_Users.getCellEditor() != null) table_Users.getCellEditor().stopCellEditing();
				// Save user data from edited row
					if (cellEdited) saveUsedData(pointToolHand,tableUserRowSelected);
					cellEdited = false;
				// Close database
					pointToolHand.closeDatabaseConnection();
				} catch (HBException hbe) {
					JOptionPane.showMessageDialog(contents, "Database close error \n"  //$NON-NLS-1$
							+  hbe.getMessage(),
							"Administration error",JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0403 closing database " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
				dispose();
			}
		});

		// Perform selection in Project Table and load User table
		projectListModel.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent eventp) {
				if (!eventp.getValueIsAdjusting()) {
					int selectedPRow = table_Projects.getSelectedRow();
					String selectedProject = (String) table_Projects.getValueAt(selectedPRow, 0); 	// Project name in col 0
					if (HGlobal.DEBUG && HGlobal.writeLogs)
						HB0711Logging.logWrite("Action: in HG0403 - selected project: " + selectedProject); //$NON-NLS-1$
				// Proceed with this selection
					int selectedProjRow = table_Projects.getSelectedRow();
					// If selected row server name not 'Local' (column 2) it is remote, so do not allow
					if ((String) table_Projects.getValueAt(selectedProjRow, 2) != HGlobalCode.getLocalText()) {
						// Show error msg for Remote projects
						lbl_FilenameData.setText(HG0403Msgs.Text_69);	// Cannot perform changes on a Remote project
						lbl_FilenameData.setForeground(Color.RED);
						lbl_FolderNameData.setVisible(false);
						// Empty visible table (in case it shows a previous selection)
						((DefaultTableModel)table_Users.getModel()).setNumRows(0);
						// Disable buttons
						btn_PassWord.setEnabled(false);
						btn_DeleteUser.setEnabled(false);
						btn_AddUser.setEnabled(false);
						return;
					}
					lbl_FilenameData.setForeground(Color.BLACK);
					lbl_FolderNameData.setVisible(true);
					String [] projectData = null;
					try {
						projectData = pointToolHand.getUserProjectByIndex(pointToolHand.getUserProjectByName(selectedProject));
						lbl_FilenameData.setText(projectData[1]);
						HGlobal.projectFilename = projectData[1];
						lbl_FolderNameData.setText(projectData[2]);
						HGlobal.projectFolder = projectData[2];
					// Check if editor active and stop editing
						if (table_Users.getCellEditor() != null) table_Users.getCellEditor().stopCellEditing();
					// Save userdata from edited row
						if (cellEdited) saveUsedData(pointToolHand,tableUserRowSelected);
						cellEdited = false;
					// Close any DB that's open; open this one
						pointToolHand.closeDatabaseConnection();
						pointToolHand.openDatabase(HGlobal.projectFolder, HGlobal.projectFilename);
					// Populate user table from project data
						DefaultTableModel model = (DefaultTableModel) table_Users.getModel();
						model.setDataVector(pointToolHand.presentTableUsersT131(),			// Logon ID, User's Name, Email, Is Owner?
								new String[] {HG0403Msgs.Text_42,HG0403Msgs.Text_43, HG0403Msgs.Text_44, HG0403Msgs.Text_50});
					// Reset column sizes
						table_Users.getColumnModel().getColumn(0).setMinWidth(80);
						table_Users.getColumnModel().getColumn(0).setPreferredWidth(100);
						table_Users.getColumnModel().getColumn(1).setMinWidth(80);
						table_Users.getColumnModel().getColumn(1).setPreferredWidth(100);
						table_Users.getColumnModel().getColumn(2).setMinWidth(100);
						table_Users.getColumnModel().getColumn(2).setPreferredWidth(120);
						table_Users.getColumnModel().getColumn(3).setMinWidth(30);
						table_Users.getColumnModel().getColumn(3).setPreferredWidth(30);
					// set button visibility
						btn_AddUser.setEnabled(true);
						btn_DeleteUser.setEnabled(true);
						btn_PassWord.setEnabled(true);
					} catch (HBException | HDException hbe) {
						JOptionPane.showMessageDialog(contents, "User table load error \n"  //$NON-NLS-1$
								+  hbe.getMessage(),
								"Administration error",JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
						if (HGlobal.writeLogs) {
							HB0711Logging.logWrite("ERROR: in HG0403 loading User table " + hbe.getMessage()); //$NON-NLS-1$
							HB0711Logging.printStackTraceToFile(hbe);
						}
					}
				}
			}
		});

	// Table change listener
		table_Users.getModel().addTableModelListener(new TableModelListener() {
            @Override
			public void tableChanged(TableModelEvent e) {
				if (HGlobal.DEBUG && HGlobal.writeLogs)
					HB0711Logging.logWrite("Action: in HG0403 Change Type: " + e.getType() + " Col: "  //$NON-NLS-1$ //$NON-NLS-2$
            				+ e.getColumn() + " Row: " + e.getFirstRow()); //$NON-NLS-1$
             // Test if IS_OWNER checkbox column has changed.
            	if (e.getColumn() == 3) {
            		table_Users.setRowSelectionInterval(e.getFirstRow(), e.getFirstRow()); // Force this row as selected one
            		tableUserRowSelected = e.getFirstRow();
            		cellEdited = true;
            	}
             // Single Cell edited and row updated in T131
                if (e.getType() == 0 && e.getColumn() != -1)
                	cellEdited = true;
            	if (e.getColumn() == 2) {
    				if (HGlobal.DEBUG && HGlobal.writeLogs)
    					HB0711Logging.logWrite("Action: in HG0403 E-mail changed: " + table_Users.getValueAt(e.getFirstRow(), 2));  //$NON-NLS-1$
					String email = (String) table_Users.getValueAt(e.getFirstRow(), 2);
					if (HGlobalCode.isEmailValid(email)) {     	//email address is valid
						if (HGlobal.DEBUG && HGlobal.writeLogs)
							HB0711Logging.logWrite("Result: in HG0403 valid email: " + email); //$NON-NLS-1$
					} else {
						while (!HGlobalCode.isEmailValid(email)) {
							email = JOptionPane.showInputDialog(btn_AddUser,
								HG0403Msgs.Text_80 + "\n" + HG0403Msgs.Text_82,	//$NON-NLS-1$  // Invalid email address, Enter valid email address:
								email);
						}
						table_Users.setValueAt(email,e.getFirstRow(), 2);
					}
                }
            }
        });

	// User Table selection
		userListModel.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent eventu) {
				if (!eventu.getValueIsAdjusting()) {
			// Store row to update
					int updateRow = tableUserRowSelected;
					tableUserRowSelected = table_Users.getSelectedRow();
					if (tableUserRowSelected < 0) return;	//exit if listener call caused by emptying table_User
					if (HGlobal.DEBUG && HGlobal.writeLogs)
						HB0711Logging.logWrite("Action: in HG0403 Selected user: " //$NON-NLS-1$
								+ tableUserRowSelected + " / " + table_Users.getValueAt(tableUserRowSelected, 0)); //$NON-NLS-1$
				// Check if editor active and stop editing
					if (table_Users.getCellEditor() != null) table_Users.getCellEditor().stopCellEditing();
	                String userId = (String) table_Users.getModel().getValueAt(tableUserRowSelected, 0);
	            //Not allowed to update user SA
	                if (userId.equals("SA")) return; //$NON-NLS-1$
	             // Save user data when selecting new
					if (cellEdited) saveUsedData(pointToolHand,updateRow);
					cellEdited = false;
				}
			}
		});

	// Listener for User Add button
		btn_AddUser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: adding User in HG0403ProjectAdmin"); //$NON-NLS-1$
				DefaultTableModel userModel = (DefaultTableModel) table_Users.getModel();
			// Create Strings of new user info (Logon ID, User Name, password, email, let Owner default to false)
				String[] newUserInfo = addUserInfo(btn_AddUser);
				if (newUserInfo != null) {
					try {
					// Add new entry to database User table T131 and screen's TableModel
						if (pointToolHand.addToUserTableAction (newUserInfo)) {
							userModel.addRow( new Object[] { newUserInfo[0], newUserInfo[1], newUserInfo[3]});
					// Now highlight the added row as selected
							int size = table_Users.getRowCount();
							table_Users.setRowSelectionInterval(size-1, size-1);
						}	// do not dispose (as user may want to do more actions)

					} catch (HBException hbe) {
						JOptionPane.showMessageDialog(contents, "User table adderror \n"  //$NON-NLS-1$
								+  hbe.getMessage(),
								"Administration error",JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
						if (HGlobal.writeLogs) {
							HB0711Logging.logWrite("ERROR: in HG0403 adding to User table " + hbe.getMessage()); //$NON-NLS-1$
							HB0711Logging.printStackTraceToFile(hbe);
						}
					}
				} else JOptionPane.showMessageDialog(contents, HG0403Msgs.Text_90		// Add user error.\n
													+ HG0403Msgs.Text_91,			// No data entered.
													HG0403Msgs.Text_92, JOptionPane.ERROR_MESSAGE);	// Admin Tools
			}
		});

	// Listener for Delete User button
		btn_DeleteUser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: deleting User in HG0403ProjectAdmin"); //$NON-NLS-1$
				DefaultTableModel userModel = (DefaultTableModel) table_Users.getModel();
				try {
			// Action the delete in database User table T131 and in TableModel
					if (tableUserRowSelected != -1) {
						if (pointToolHand.deleteUserTableAction(tableUserRowSelected,
								(String)table_Users.getModel().getValueAt(tableUserRowSelected, 0))) {
							userModel.removeRow(tableUserRowSelected);
						}
					} else JOptionPane.showMessageDialog(contents, HG0403Msgs.Text_94,		// Select user first
														HG0403Msgs.Text_92,JOptionPane.ERROR_MESSAGE);  // Admin Tools

				} catch (HBException hbe) {
					JOptionPane.showMessageDialog(contents, "User table deletion error \n"  //$NON-NLS-1$
							+  hbe.getMessage(),
							"Administration error",JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0403 deleting from User table " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
			}
		});

	// Listener for password button
		btn_PassWord.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (tableUserRowSelected != -1) {
					passWord = setNewPassWord(btn_PassWord);
					if (passWord != null) if (passWord.length() < 8) passWord = null;
					saveUsedData(pointToolHand, tableUserRowSelected);
				} else JOptionPane.showMessageDialog(contents, HG0403Msgs.Text_94,		// Select user first
													HG0403Msgs.Text_92,JOptionPane.ERROR_MESSAGE);  // Admin Tools
			}
		});

	}	//End HG0403 constructor

/**
 * private String setNewPassWord(JButton button)
 * @param button
 * @return password or null if no change
 */
	private String setNewPassWord(JButton button) {
		passWord = " "; //$NON-NLS-1$
		String passMessage = HG0403Msgs.Text_101 	// Password must be at least 8 characters, including at least one \n
							+ HG0403Msgs.Text_102;	// Capital letter, Lower-case letter, Number and Special character
		JPasswordField passWordField = new JPasswordField("--------",15); //$NON-NLS-1$
		while (!HGlobalCode.isPswdGood(passWord)) {
			if (passWord.length() > 0) JOptionPane.showMessageDialog(button, passMessage);
			int action = JOptionPane.showConfirmDialog(button,passWordField, HG0403Msgs.Text_104,	// Enter Password
														JOptionPane.OK_CANCEL_OPTION);
			passWord = new String(passWordField.getPassword());
			if (HGlobal.DEBUG && HGlobal.writeLogs)
				HB0711Logging.logWrite("Action: in HG0403 code: " + action + " Password: " +  passWord ); //$NON-NLS-1$ //$NON-NLS-2$
		    if (action == 2 || passWord.length() == 0) return null;
		}
		JOptionPane.showMessageDialog(button, HG0403Msgs.Text_107);	// New password is set
		return passWord;
	}

/**
 * addUserInfo(JButton btn_AddUser)
 * @param btn_AddUser
 * @return String[] chgUserInfo
 */
	private String[] addUserInfo(JButton btn_AddUser) {
		String[] chgUserInfo = new String[4];
		// Get new Logon ID
		chgUserInfo[0] = JOptionPane.showInputDialog(btn_AddUser,
							HG0403Msgs.Text_108, chgUserInfo[0]);		// Enter new Logon ID:
		if (chgUserInfo[0] == null) return null; 		// user must have cancelled
		if (chgUserInfo[0].length() == 0) return null;	// user pressed OK
		// Test if LogonID contains blanks - will create illegal SQL is it does
		if (chgUserInfo[0].contains(" ")) {		//$NON-NLS-1$
			JOptionPane.showMessageDialog(btn_AddUser,
					HG0403Msgs.Text_51,		// Logon ID must not contain blanks
					chgUserInfo[1],
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		// Test if new LogonId equals existing one - not allowed
		for(int i = 0; i < table_Users.getRowCount(); i++){
			if(chgUserInfo[0].equals(table_Users.getModel().getValueAt(i, 0).toString().trim())) {
				JOptionPane.showMessageDialog(btn_AddUser,
						HG0403Msgs.Text_52 + chgUserInfo[0] + HG0403Msgs.Text_53,		// Logon ID  // already in use
						chgUserInfo[1],
						JOptionPane.ERROR_MESSAGE);
				return null;
			}
		}
		// Get new User anme
		chgUserInfo[1] = JOptionPane.showInputDialog(btn_AddUser,
							HG0403Msgs.Text_109 + chgUserInfo[0] +"\n"+ //$NON-NLS-1$	// Logon ID is:
							HG0403Msgs.Text_111, chgUserInfo[1]);						// Now enter new User's Name:
		if (chgUserInfo[1] == null) return null; 		// user must have cancelled
		if (chgUserInfo[1].length() == 0) return null;  // user pressed OK
		// Get email address
		String email = JOptionPane.showInputDialog(btn_AddUser,
							HG0403Msgs.Text_109 + chgUserInfo[0] +"\n"	 //$NON-NLS-1$	// Logon ID is:
							+ HG0403Msgs.Text_114 + chgUserInfo[1] +"\n" //$NON-NLS-1$	// User's name is:
							+ HG0403Msgs.Text_116);										// Now enter their email address:
		if (email == null) return null;	// user must have cancelled
		if (email.length() == 0) return null;	// user must have clicked OK
		if (HGlobalCode.isEmailValid(email)) {     	//email address is valid
			chgUserInfo[3] = email;
		} else {
			while (!HGlobalCode.isEmailValid(email)) {
				email = JOptionPane.showInputDialog(btn_AddUser,
							HG0403Msgs.Text_117 + "\n" 	//$NON-NLS-1$		// Invalid email address
							+ HG0403Msgs.Text_119,							// Enter valid email address:
							HG0403Msgs.Text_120, JOptionPane.ERROR_MESSAGE);	// Enter Email Address
			}
			chgUserInfo[3] = email;
		}
		// Enter password
		passWord = setNewPassWord(btn_AddUser);
		if (passWord == null) {
			JOptionPane.showMessageDialog(btn_AddUser,
					HG0403Msgs.Text_121 + chgUserInfo[0], 				// Default password set for
					HG0403Msgs.Text_32, JOptionPane.ERROR_MESSAGE);		// Set Password
			chgUserInfo[2] = "Hre_2021"; //$NON-NLS-1$
		} else chgUserInfo[2] = passWord;
		// Don't prompt for an IS_OWNER setting - let it be set to false
		return chgUserInfo;
	}

/**
 * saveUsedData(HBToolHandler pointToolHand)
 * @param pointToolHand
 */
	public void saveUsedData(HBToolHandler pointToolHand, int tableUserRowSelected) {
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: saving User info in HG0403ProjectAdmin"); //$NON-NLS-1$
		Object chgUserInfo[] =  {null, null, null, null, false};
    // Check if editor active and there is an edit in progress
		if (table_Users.getCellEditor() != null) table_Users.getCellEditor().stopCellEditing();
	// Check if row selected
		if (tableUserRowSelected >= 0) {
		// get selected user's current info
			chgUserInfo[0] = (String) table_Users.getValueAt(tableUserRowSelected, 0);	// logonid
			chgUserInfo[1] = (String) table_Users.getValueAt(tableUserRowSelected, 1);	// username
			chgUserInfo[2] = passWord;
			chgUserInfo[3] = (String) table_Users.getValueAt(tableUserRowSelected, 2);	// email
			chgUserInfo[4] = (boolean) table_Users.getValueAt(tableUserRowSelected, 3);	// IS_OWNER setting
			try {
			// action the changed user data
				if (pointToolHand.changeUserTableAction(chgUserInfo,tableUserRowSelected)) {
					if (HGlobal.DEBUG && HGlobal.writeLogs)
						HB0711Logging.logWrite("Action: in HG0403 Modified User T131 table: " + chgUserInfo[0]); //$NON-NLS-1$
				} // do not dispose (as user may want to do more actions)
			} catch (HBException hbe) {
				JOptionPane.showMessageDialog(contents, "User table update error \n"  //$NON-NLS-1$
						+  hbe.getMessage(),
						"Administration error",JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
				if (HGlobal.writeLogs) {
					HB0711Logging.logWrite("ERROR: in HG0403 updating User table " + hbe.getMessage()); //$NON-NLS-1$
					HB0711Logging.printStackTraceToFile(hbe);
				}
			}
		}
	}

}	//End HG0403ProjectUserAdmin