package hre.gui;
/********************************************************************************************************
 * Project Backup - Specification 04.05 GUI_ProjectBackup 2020-03-02
 * v0.00.0007 2019-02-10 by R Thompson, revised by D Ferguson
 * v0.00.0008 2019-07-21 fix close cancel code (D Ferguson)
 * v0.00.0014 2019-11-18 setup server/project ArrayLists properly (D Ferguson)
 * 						 and changes for HB0711 static classes (D Ferguson)
 * v0.00.0015 2019-12-07 changed code for HG0514 call (D Ferguson)
 * v0.00.0017 2020-01-14 updated call to HG0414 (D. Ferguson)
 * v0.00.0018 2020-01-29 implemented call to ProjectHander (N. Tolleshaug)
 *            2020-02-18 add backupProjectAction
 * v0.00.0019 2020-02-24 removed project usage/change boxes (D. Ferguson)
 * v0.00.0022 2020-06-28 standardise file separators in filepaths (D Ferguson)
 *            2020-07-27 add screenID for Help xref (D Ferguson)
 * v0.01.0023 2020-08-04 changed from JDialog to extend HG0450SuperDialog (D Ferguson)
 *            2020-09-11 removed Reminder icon from Toolbar (D Ferguson)
 *            2020-09-30 changed to MigLayout; fonts removed for JTattoo (D Ferguson)
 * v0.01.0025 2020-11-24 use location Setting for backup folder (D Ferguson)
 * 			  2021-01-19 fix backups going to wrong place if external files chosen (D Ferguson)
 * 			  2021-01-22 fix error that external files not backed up at all (D Ferguson)
 * 			  2021-03-15 Implemented JOptionPane messages from ProjectHandler (N.Tolleshaug)
 * 			  2021-04-15 converted to NLS (D Ferguson)
 * v0.01.0026 2021-05-05 Trim filename and restrict size of folder/filename lbl fields (D Ferguson)
 * 			  2021-09-17 Apply tag codes to screen control buttons (D Ferguson)
 * v0.01.0027 2022-02-26 Modified to use the NLS version of HGlobal (D Ferguson)
 * 			  2022-06-16 Add progressbar (indeterminate) during backup action (D Ferguson)
 * v0.03.0031 2023-11-17 Adjust error action if file open b4 backup - allow Cancel (D Ferguson)
 * 			  2024-11-29 Replace JoptionPane 'null' locations with 'contents' (D Ferguson)
 *********************************************************************************************************/

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
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
import hre.bila.HBProjectOpenData;
import hre.nls.HG0405Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Project Backup
 * @author R Thompson
 * @version v0.03.0031
 * @since 2019-02-10
 */

public class HG0405ProjectBackup extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	private String screenID = "40500"; //$NON-NLS-1$
	private HG0405ProjectBackup pointProjectBackup = this;
	private JPanel contents;
	private String selectedProjectName;
	private String backupFilename;
	private String backupFolder;
	private String backupExtFolder;
	private String backupExtZipFile;
	private String timeStamp;
	private JTable table_Projects;
	private JProgressBar progBar;

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
	public HG0405ProjectBackup(HBProjectHandler pointProHand) {
		// Setup references for HG0450
		windowID = screenID;
		helpName = "backupproject"; //$NON-NLS-1$

		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0405 Project Backup");  	 //$NON-NLS-1$
		setResizable(false);
		setTitle(HG0405Msgs.Text_3);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		contents = new JPanel();
		contents.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]20[250][]", "[]10[]10[]10[]10[]10[]10[]10[]10[]10[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// Toolbar setup
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
		toolBar.add(Box.createHorizontalGlue());
		// Add icon defined in HG0450
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north"); //$NON-NLS-1$

		// Add other components
		JLabel lbl_From = new JLabel(HG0405Msgs.Text_8);
		lbl_From.setFont(lbl_From.getFont().deriveFont(lbl_From.getFont().getStyle() | Font.BOLD));
		contents.add(lbl_From, "cell 0 0 2 1"); //$NON-NLS-1$

		JLabel lbl_OpenProjects = new JLabel(HG0405Msgs.Text_10);
		lbl_OpenProjects.setToolTipText(HG0405Msgs.Text_11);
		contents.add(lbl_OpenProjects, "cell 0 1 2"); //$NON-NLS-1$

		JScrollPane scrollPane = new JScrollPane();
		contents.add(scrollPane, "cell 0 2 3"); //$NON-NLS-1$

		JLabel lbl_Project = new JLabel(HG0405Msgs.Text_14);
		contents.add(lbl_Project, "cell 0 3,alignx right");		 //$NON-NLS-1$
		JLabel lbl_ProjectName = new JLabel();
		lbl_ProjectName.setVisible(false);
		contents.add(lbl_ProjectName, "cell 1 3"); //$NON-NLS-1$

		JButton btn_Summary = new JButton(HG0405Msgs.Text_17);
		btn_Summary.setToolTipText(HG0405Msgs.Text_18);
		btn_Summary.setEnabled(false);				//Enable after project selection
		contents.add(btn_Summary, "cell 2 3,alignx right");	 //$NON-NLS-1$

		JSeparator separator = new JSeparator();
		separator.setOpaque(true);
		separator.setForeground(UIManager.getColor("scrollbar")); //$NON-NLS-1$
		separator.setPreferredSize(new Dimension(0, 3));
		contents.add(separator, "cell 0 4 3, growx");		 //$NON-NLS-1$

		JLabel lbl_To = new JLabel(HG0405Msgs.Text_22);
		lbl_To.setFont(lbl_To.getFont().deriveFont(lbl_To.getFont().getStyle() | Font.BOLD));
		contents.add(lbl_To, "cell 0 5"); //$NON-NLS-1$

		JButton btn_Browse_Project = new JButton(HG0405Msgs.Text_24);
		btn_Browse_Project.setToolTipText(HG0405Msgs.Text_25);
		btn_Browse_Project.setEnabled(false);					//Enable after project selection
		contents.add(btn_Browse_Project, "cell 1 5"); //$NON-NLS-1$

		JLabel lbl_FolderName = new JLabel(HG0405Msgs.Text_27);
		contents.add(lbl_FolderName, "cell 0 6,alignx right"); //$NON-NLS-1$
		JLabel lbl_FolderNameData = new JLabel(""); //$NON-NLS-1$
		lbl_FolderNameData.setMaximumSize(new Dimension(450, 23));
		contents.add(lbl_FolderNameData, "cell 1 6 2 1"); //$NON-NLS-1$

		JLabel lbl_Filename = new JLabel(HG0405Msgs.Text_31);
		contents.add(lbl_Filename, "cell 0 7,alignx right"); //$NON-NLS-1$
		JLabel lbl_FilenameData = new JLabel(""); //$NON-NLS-1$
		lbl_FilenameData.setMaximumSize(new Dimension(450, 23));
		contents.add(lbl_FilenameData, "cell 1 7 2 1");		 //$NON-NLS-1$

		JCheckBox chckbx_ExtFolders = new JCheckBox(HG0405Msgs.Text_35);
		chckbx_ExtFolders.setToolTipText(HG0405Msgs.Text_36);
		chckbx_ExtFolders.setEnabled(false);				//Enable after backup location set
		contents.add(chckbx_ExtFolders, "cell 0 8 2 1");	//$NON-NLS-1$

		progBar = new JProgressBar(SwingConstants.HORIZONTAL, 0, 100);
		progBar.setVisible(false);
		contents.add(progBar, "cell 0 9 3 1, growx, hidemode 2");	 //$NON-NLS-1$

		JButton btn_Cancel = new JButton(HG0405Msgs.Text_38);
		btn_Cancel.setToolTipText(HG0405Msgs.Text_39);
		contents.add(btn_Cancel, "cell 2 10, align right, gapx 20, tag cancel"); //$NON-NLS-1$

		JButton btn_Backup = new JButton(HG0405Msgs.Text_41);
		btn_Backup.setToolTipText(HG0405Msgs.Text_42);
		btn_Backup.setEnabled(false);						//Enable after backup location set
		contents.add(btn_Backup, "cell 2 10, align right, gapx 20, tag ok"); //$NON-NLS-1$

		// Setup a Project table as non-editable by the user and set TableModel
		table_Projects = new JTable() { private static final long serialVersionUID = 1L;
										@Override
										public boolean isCellEditable(int row, int column) {
											return false;}};
		table_Projects.setModel(new DefaultTableModel(
				pointProHand.getUserProjectArray2D(),
				HGlobalCode.projectTableHeader()));

		// Set the column widths
		table_Projects.getColumnModel().getColumn(0).setMinWidth(150);
		table_Projects.getColumnModel().getColumn(0).setPreferredWidth(200);
		table_Projects.getColumnModel().getColumn(1).setMinWidth(150);
		table_Projects.getColumnModel().getColumn(1).setPreferredWidth(200);
		table_Projects.getColumnModel().getColumn(2).setMinWidth(100);
		table_Projects.getColumnModel().getColumn(2).setPreferredWidth(130);
		table_Projects.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		// Set the ability to sort on columns and presort column 0
		table_Projects.setAutoCreateRowSorter(true);
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(table_Projects.getModel());
		table_Projects.setRowSorter(sorter);
		List <RowSorter.SortKey> sortKeys = new ArrayList<>();
		sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(sortKeys);
		// Set tooltips and header format, renderer
		table_Projects.getTableHeader().setToolTipText(HG0405Msgs.Text_44);
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

/********************************************************
 * DEFINE HG0405A INNER CLASS FOR HANDLING EXTERNAL FILES
 ********************************************************/
		// Includes this JTable to handle External File folder names
		// to be populated by 0405A
		Object[][] rowFolders = {   	};											// setup empty table
		Object[] colName = { HG0405Msgs.Text_45 };									// with a column title
		DefaultTableModel extmodel = new DefaultTableModel(rowFolders, colName);
		JTable table_ExtFolders = new JTable();
		table_ExtFolders.setModel(extmodel);
		table_ExtFolders.getColumnModel().getColumn(0).setPreferredWidth(15);

		class HG0405ABackupExt extends JDialog {
			private static final long serialVersionUID = 001L;
			private JPanel contentsA;

			private HG0405ABackupExt() {
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0405A Project Backup"); //$NON-NLS-1$
				setResizable(false);
				setTitle(HG0405Msgs.Text_47);
				setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/hre/images/HRE-32.png"))); //$NON-NLS-1$
				setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				contentsA = new JPanel();
				contentsA.setBorder(new EmptyBorder(5, 5, 5, 5));
				setContentPane(contentsA);
				contentsA.setLayout(new MigLayout("insets 10", "[]10[]", "[]10[]10[]10[]10[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

				//Build 0405A screen
				JLabel lbl_ExtFilesA = new JLabel(HG0405Msgs.Text_52);
				lbl_ExtFilesA.setFont(lbl_ExtFilesA.getFont().deriveFont(lbl_ExtFilesA.getFont().getStyle() | Font.BOLD));
				contentsA.add(lbl_ExtFilesA, "cell 0 0 2"); //$NON-NLS-1$

				JLabel lbl_FromA = new JLabel(HG0405Msgs.Text_54);
				lbl_FromA.setFont(lbl_FromA.getFont().deriveFont(lbl_FromA.getFont().getStyle() | Font.BOLD));
				contentsA.add(lbl_FromA, "cell 0 1, alignx right"); //$NON-NLS-1$

				JButton btn_BrowseA = new JButton(HG0405Msgs.Text_56);
				btn_BrowseA.setToolTipText(HG0405Msgs.Text_57);
				contentsA.add(btn_BrowseA, "cell 1 1"); //$NON-NLS-1$

				JScrollPane scrollPane = new JScrollPane();
				contentsA.add(scrollPane, "cell 0 2 2"); //$NON-NLS-1$

				JLabel lbl_ToA = new JLabel(HG0405Msgs.Text_60);
				lbl_ToA.setFont(lbl_ToA.getFont().deriveFont(lbl_ToA.getFont().getStyle() | Font.BOLD));
				contentsA.add(lbl_ToA, "cell 0 3, alignx right"); //$NON-NLS-1$

				JButton btn_Browse_ExtA = new JButton(HG0405Msgs.Text_62);
				btn_Browse_ExtA.setToolTipText(HG0405Msgs.Text_63);
				btn_Browse_ExtA.setEnabled(false);		//Enable after folder(s) to backup are selected
				contentsA.add(btn_Browse_ExtA, "cell 1 3"); //$NON-NLS-1$

				JLabel lbl_FolderNameA = new JLabel(HG0405Msgs.Text_65);
				contentsA.add(lbl_FolderNameA, "cell 0 4, alignx right"); //$NON-NLS-1$
				JLabel lbl_FolderNameDataA = new JLabel(""); 		//$NON-NLS-1$
				contentsA.add(lbl_FolderNameDataA, "cell 1 4");		//$NON-NLS-1$

				JButton btn_CancelA = new JButton(HG0405Msgs.Text_69);
				btn_CancelA.setToolTipText(HG0405Msgs.Text_70);
				contentsA.add(btn_CancelA, "cell 1 5, alignx right"); //$NON-NLS-1$

				JButton btn_AcceptA = new JButton(HG0405Msgs.Text_72);
				btn_AcceptA.setEnabled(false);
				btn_AcceptA.setToolTipText(HG0405Msgs.Text_73);
				contentsA.add(btn_AcceptA, "cell 1 5, gapx 20");	 //$NON-NLS-1$

				// If the default ext. backup location is defined in Settings, then accept it
				if (HGlobal.pathExtbackups != null && !HGlobal.pathExtbackups.trim().isEmpty()) {
						backupExtFolder = HGlobal.pathExtbackups;
						lbl_FolderNameDataA.setText(HGlobal.pathExtbackups);
						btn_AcceptA.setEnabled(true);
				}

				// Complete definition of table_ExtFolders
				// Set the ability to auto-sort on column 0
				table_ExtFolders.setAutoCreateRowSorter(true);
				TableRowSorter<TableModel> sorter = new TableRowSorter<>(table_ExtFolders.getModel());
				table_ExtFolders.setRowSorter(sorter);
				List <RowSorter.SortKey> sortKeys = new ArrayList<>();
				sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
				sorter.setSortKeys(sortKeys);
				// Set tooltips, header format and renderer
				table_ExtFolders.getTableHeader().setToolTipText(HG0405Msgs.Text_75);
				JTableHeader pHeader = table_ExtFolders.getTableHeader();
				pHeader.setOpaque(false);
				TableCellRenderer rendererFromHeader = table_ExtFolders.getTableHeader().getDefaultRenderer();
				JLabel headerLabel = (JLabel) rendererFromHeader;
				headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
				ListSelectionModel cellSelectionModel = table_ExtFolders.getSelectionModel();
				cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

				table_ExtFolders.setPreferredScrollableViewportSize(new Dimension(400, 150));
				scrollPane.setViewportView(table_ExtFolders);

				pack();

	/******************************
 	* CREATE 0405A ACTION LISTENERS
 	******************************/
				// Browse - for folders to backup
				btn_BrowseA.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						// Start looking at the defined HGlobal Setting for HRE project files
						HG0577FileChooser chooseFile = new HG0577FileChooser("Select",  //$NON-NLS-1$
								HG0405Msgs.Text_77, "*.*", null, HGlobal.pathHRElocation, 2);	// mode=2=Directories Only //$NON-NLS-1$
						chooseFile.setModalityType(ModalityType.APPLICATION_MODAL);
						Point xy = btn_BrowseA.getLocationOnScreen();           // Gets Browse button location on screen
						chooseFile.setLocation(xy.x, xy.y);     		        // Sets screen top-left corner relative to that
						chooseFile.setVisible(true);
						//Get returned folder name and add to table_ExtFolders
						Object[] row = { HGlobal.chosenFolder };
						extmodel.addRow(row);
						btn_Browse_ExtA.setEnabled(true);
					}
				});

				// Browse - for place and file to backup External folders to
				btn_Browse_ExtA.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						// Start looking at the defined HGlobal Setting for Ext file backups
						HG0577FileChooser chooseExt = new HG0577FileChooser("Select",  			//$NON-NLS-1$
								HG0405Msgs.Text_80, "*.zip*", null, HGlobal.pathExtbackups, 2);	//$NON-NLS-1$
						chooseExt.setModalityType(ModalityType.APPLICATION_MODAL);
						Point xy = btn_Browse_ExtA.getLocationOnScreen();
						chooseExt.setLocation(xy.x, xy.y);
						chooseExt.setVisible(true);
						// Get returned folder to use for External File backup
						backupExtFolder = HGlobal.chosenFolder;
						lbl_FolderNameDataA.setText(backupExtFolder);
						btn_AcceptA.setEnabled(true);
					}
				});

				// Listener for clicking 'X on 0405A screen - make same as CancelA button
				addWindowListener(new WindowAdapter() {
				    @Override
					public void windowClosing(WindowEvent e)  {
				    	btn_CancelA.doClick();
				    }
				});
				btn_CancelA.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						((DefaultTableModel)table_ExtFolders.getModel()).setRowCount(0); // If Cancel, empty table_ExtFolders
						JOptionPane.showMessageDialog(btn_CancelA, HG0405Msgs.Text_82, HG0405Msgs.Text_83, JOptionPane.WARNING_MESSAGE);
						chckbx_ExtFolders.setSelected(false);
						if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: cancelling HG0405A Project Backup");} //$NON-NLS-1$
						dispose();			// return to Backup screen
					}
				});

				// Listener for Accept button
				btn_AcceptA.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent arg0) {
						int numRows = table_ExtFolders.getRowCount();		// count number of folders in table; change checkbox text
						chckbx_ExtFolders.setText(HG0405Msgs.Text_85 + String.valueOf(numRows));
						if (HGlobal.writeLogs) {
							HB0711Logging.logWrite("Action: returning from HG0405A Project Backup"); //$NON-NLS-1$
						}
		                dispose();		// return to Backup screen
					}
				});

			}
		}	// End of HG0405A class for external folder handling

/********************************
 * CREATE HG0405 ACTION LISTENERS
 *******************************/
		// Listener for project selection in Project Table
		cellSelectionModel.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				int[] selectedRow = table_Projects.getSelectedRows();
				for (int element : selectedRow) {
					//get the Project name irrespective of whether or not Server or Project Name was selected in the table
					selectedProjectName = (String) table_Projects.getValueAt(element, 0);	// Projectname in col 0

					//put the Project name into the 'ProjectName label'
					lbl_ProjectName.setText(selectedProjectName);
					lbl_ProjectName.setVisible(true);

					// If the default backup location is defined in Settings, then use it
					if (HGlobal.pathHREbackups != null && !HGlobal.pathHREbackups.trim().isEmpty()) {
							lbl_FolderNameData.setText(HGlobal.pathHREbackups);
							btn_Backup.setEnabled(true);
							chckbx_ExtFolders.setEnabled(true);
							backupFolder = HGlobal.pathHREbackups;
						}
					// use the project name to construct a suggested backup filename with timestamp (zipped .hrez file)
					timeStamp = pointProHand.currentTime("_yyyy-MM-dd_HH-mm-ss"); //$NON-NLS-1$
					timeStamp = timeStamp.replace(":", "-");	// get rid of illegal colon character in time	 //$NON-NLS-1$ //$NON-NLS-2$
					backupFilename = selectedProjectName + HG0405Msgs.Text_90 + timeStamp;
					lbl_FilenameData.setText(backupFilename.trim());
					//enable the Summary and BACKUP TO (Browse) buttons
					btn_Summary.setEnabled(true);
					btn_Browse_Project.setEnabled(true);
				}
			}
		});

		// listener for Project Summary button
		btn_Summary.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String [][] summaryData = null;
				try {
					summaryData = pointProHand.getSummaryUserProjectAction(selectedProjectName);
				} catch (HBException hbe) {
					JOptionPane.showMessageDialog(contents, "Backup project summary error\n"  //$NON-NLS-1$
							+  hbe.getMessage(), "Project Backup",JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
				}
				if (summaryData != null) {
					HG0414ProjectSummary summscreen = new HG0414ProjectSummary(summaryData);
					summscreen.setModalityType(ModalityType.APPLICATION_MODAL);
					Point xy = btn_Summary.getLocationOnScreen();    // Gets Summary button location on screen
					summscreen.setLocation(xy.x + 50, xy.y);     	 // Sets screen top-left corner relative to that
					summscreen.setVisible(true);
				} else if (HGlobal.DEBUG) System.out.println("Summary data is null"); //$NON-NLS-1$
			}
		});

		// Listener for Browse To - find location where project backup will be put
		btn_Browse_Project.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// Use the filename based on Project name as default backup filename - user can over-ride
				//HG0577 parameters are HG0577FileChooser("Save", "HRE Project files (*.hrez)", "hrez", backupFilename, HGlobal.pathHREbackups, 1);
				btn_Browse_Project.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				// Start looking at the defined HGlobal Setting for HRE backup files
				HG0577FileChooser chooseFile =
						new HG0577FileChooser(pointProHand.pointLibraryBusiness.
								setUpFolderBackupChooser(HGlobal.pathHREbackups, HGlobal.defDatabaseEngine));
				chooseFile.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xy = btn_Browse_Project.getLocationOnScreen();  // Gets chooser button location on screen
				chooseFile.setLocation(xy.x, xy.y);     		     // Sets screen top-left corner relative to that
				chooseFile.setVisible(true);
				btn_Browse_Project.setCursor(Cursor.getDefaultCursor());
				// Process file and folder-names returned from file chooser (the 'chosen' fields)
				if (HGlobal.chosenFilename == "" ) {   }		       // do nothing if no filename setup //$NON-NLS-1$
					else {	String lower = HGlobal.chosenFilename.toLowerCase();  	//make filename all lower case
							int test = lower.lastIndexOf(".hrez");					// test if it ends in .hrez //$NON-NLS-1$
							if (test == -1)  {
								HGlobal.chosenFilename = HGlobal.chosenFilename + ".hrez";  // if not, add .hrez //$NON-NLS-1$
							}
							HGlobal.pathProjectFolder = HGlobal.chosenFolder;		// Save last used (preferred) folder path
							backupFolder = HGlobal.chosenFolder;					// save chosen folder for backup action
							lbl_FolderNameData.setText(HGlobal.chosenFolder);    	// display path
							chckbx_ExtFolders.setEnabled(true);
							btn_Backup.setEnabled(true);
						 }
			}
		});

		// Listener for 'External file selected' CheckBox
	    // When selected - invokes the HG0405A Class to collect the folder names to be backed up
		// External folders are returned in table_ExtFolders
		chckbx_ExtFolders.addActionListener(new ActionListener() {
		      @Override
		      public void actionPerformed(ActionEvent e) {
		        if (chckbx_ExtFolders.isSelected()) {
		        	HG0405ABackupExt extFolders=new HG0405ABackupExt();
					extFolders.setModalityType(ModalityType.APPLICATION_MODAL);
					Point xy = lbl_Project.getLocationOnScreen();     // Gets Project label location on screen
					extFolders.setLocation(xy.x, xy.y);     		  // Sets screen top-left corner relative to that
					extFolders.setVisible(true);
			      }
		        	else {chckbx_ExtFolders.setText(HG0405Msgs.Text_97); }  //reset text if user changes mind and unselects ext backup
		      }
		    });

		// Listener for backup button - backs up HRE project DB AND External files (if chosen)
		btn_Backup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// Check if file is open - allow user to cancel out of backup
				for (HBProjectOpenData element : HGlobal.openProjects) {
					if (element.getProjectData()[0].trim()
											.equals(selectedProjectName.trim())) {
						String msg =  HG0405Msgs.Text_117 	// Cannot backup an open project (newline) Project '
										+ selectedProjectName
										+ HG0405Msgs.Text_118;	// ' will be closed
						int reply = JOptionPane.showConfirmDialog(contents, msg, HG0405Msgs.Text_111,
																	JOptionPane.OK_CANCEL_OPTION);
						// If user hits Cancel or 'X' on JoptionPane then stop; else must be OK, so proceed
						if (reply == JOptionPane.CANCEL_OPTION || reply == JOptionPane.CLOSED_OPTION) return;
					}
				}

				 // Proceed to Backup the chosen HRE project database
				 String fileBackupPath;
				 if (backupFolder.endsWith(File.separator))
					 fileBackupPath = backupFolder + backupFilename.trim();
				 else fileBackupPath = backupFolder + File.separator + backupFilename.trim();

				 // Setup progressBar - visible, indeterminate, no values
				 progBar.setVisible(true);
				 progBar.setStringPainted(true);
				 progBar.setString("");	//$NON-NLS-1$
				 progBar.setIndeterminate(true);

			    // SwingWorker to do backup as a background task
				SwingWorker<Void, Void> doBackup = new SwingWorker<Void, Void>() {
			         @Override
			         protected Void doInBackground() throws Exception {
				 		 // do the backup
						 int errorCode = pointProHand.backupProjectAction(pointProjectBackup,selectedProjectName, fileBackupPath);
						 // hide progress bar when completed
						 progBar.setVisible(false);

						 if (errorCode == 0) {
							 userInfoBackupProject(errorCode, selectedProjectName, fileBackupPath);
						 } else if (errorCode == 2) {
							 userInfoBackupProject(errorCode, selectedProjectName, fileBackupPath);
							 dispose();
						 }
						 // Check if external files to be backed up too
						 if (chckbx_ExtFolders.isSelected() ) {
							 // Loop through the table of Ext folders the user wants to backup,
							 // and for each table entry, extract the Folder Name - then create
							 // a unique Backup (zip) filename with time stamp (reuse above) for each entry
							 // and then call the routine that creates zipped copies of folders.
							 for (int i = 0; i < table_ExtFolders.getRowCount(); i++) {
								 File backupOf = new File((String) table_ExtFolders.getValueAt(i,0));
								 String backupOfName = ((String) table_ExtFolders.getValueAt(i,0));
								 //Create Backup zip file name
								 backupExtZipFile = HG0405Msgs.Text_98 + backupOf.getName().toString() + timeStamp + ".zip"; //$NON-NLS-1$
								 // Add chosen folder name to it
								 if (backupExtFolder.endsWith(File.separator))
									 	 backupExtFolder = backupExtFolder + backupExtZipFile;
								 	else backupExtFolder = backupExtFolder + File.separator + backupExtZipFile;
								 // Show the progress bar again
								 progBar.setVisible(true);
								 // Initiate the Ext file backup to this location
								 errorCode = pointProHand.backupExternalAction(backupOfName, backupExtFolder);
								// hide the progress bar again
								 progBar.setVisible(false);

								 if (errorCode == 0) {
									 userInfoBackupExternal(errorCode, backupOfName, backupExtZipFile);
								 	 dispose();
								 } else if (errorCode == 1)
									userInfoBackupExternal(errorCode, backupOfName, backupExtZipFile);
				             }
						 } else dispose();

						 return null;
			         }		// end of doinBackground

				};		// End of SwingWorker doBackup

				// Execute the Swingworker backup task
			    doBackup.execute();
			}
		});

		// Listener for Cancel button
		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
			if (HGlobal.showCancelmsg)			// only show Cancel message if setting true
					{if (JOptionPane.showConfirmDialog(btn_Cancel, HG0405Msgs.Text_100, HG0405Msgs.Text_101,
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: cancelling HG0405 Project Backup");} //$NON-NLS-1$
								dispose();	}	// yes option - return to main menu
					}
				else {if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: cancelling HG0405 Project Backup");} //$NON-NLS-1$
					  	dispose();
					 }
			}
		});

		// Listener for clicking 'X' on screen - make same as Cancel button
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
		    	btn_Cancel.doClick();
		    }
		});

	}	// End HG0405 constructor

/**
 * GUI user messages invoked from HBProjectHandler
 * @param errorCode
 * @param selectedProjectName
 * @param filePath - either for zipfile or project file
 */
	private void userInfoBackupExternal(int errorCode, String backupFolderName, String zipFileCreate) {
		String errorMessage = ""; //$NON-NLS-1$
		String errorTitle = HG0405Msgs.Text_105;	// External Folder Backup
		if (errorCode == 0) {
			errorMessage = HG0405Msgs.Text_106 		// Completed Backup of folder:
								+ backupFolderName
								+ HG0405Msgs.Text_107 // To file:
								+ zipFileCreate;
			JOptionPane.showMessageDialog(contents, errorMessage, errorTitle, JOptionPane.INFORMATION_MESSAGE);
		}
		if (errorCode == 1) {
			errorMessage = HG0405Msgs.Text_108 			// External folder backup failed
								+ backupFolderName
								+ HG0405Msgs.Text_109 	// To file:
								+ zipFileCreate;
			JOptionPane.showMessageDialog(contents, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);
		}
	}	// End userInfoBackupExternal

	public void userInfoBackupProject(int errorCode, String selectedProjectName, String fileBackupPath) {
		String errorMessage = ""; //$NON-NLS-1$
		String errorTitle = HG0405Msgs.Text_111;	// Project Backup
		if (errorCode == 0) {
			errorMessage = HG0405Msgs.Text_112 			// Completed Backup of project
								+ selectedProjectName
								+ HG0405Msgs.Text_113 	// To file
								+ fileBackupPath;
			JOptionPane.showMessageDialog(contents, errorMessage, errorTitle, JOptionPane.INFORMATION_MESSAGE);
			HG0401HREMain.mainFrame.setStatusAction(HG0405Msgs.Text_114 + selectedProjectName + HG0405Msgs.Text_115);
			if (HGlobal.numOpenProjects == 0)
				HG0401HREMain.mainFrame.setStatusProject(HG0405Msgs.Text_116);	// none
			}

		// errorcode == 1 situation not applicable as check for backup of
		// an open project now handled at start of btn_Backup ActionListener.

		if (errorCode == 2) {
			errorMessage = HG0405Msgs.Text_119 	// Backup failed for project
											+ selectedProjectName;
			JOptionPane.showMessageDialog(contents, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);
			}

	}	// End userInfoBackupProject

}	// End of HG0405ProjectBackup
