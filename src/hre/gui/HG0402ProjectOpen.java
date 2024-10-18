package hre.gui;
/********************************************************************************************
 * Project Open code - Specification 04.02 GUI_ProjectOpen 2020-02-27
 * v0.00.0005 2019-02-22 by R Thompson, updated by D Ferguson
 * v0.00.0008 2019-07-21 fix close code (D Ferguson)
 * v0.00.0014 2019-11-18 changes for userProjects being ArrayList (D Ferguson)
 * 						 and changes for HB0711 static classes (D Ferguson)
 * v0.00.0015 2019-12-07 changed code for HG0514 Help call (D Ferguson)
 * v0.00.0016 2019-12-12 added pointer to ProjectHandler in constructor (N Tolleshaug)
 * v0.00.0016 2019-12-20 added call to openProjectAction in ProjectHandler
 * v0.00.0017 2020-01-07 modify window elements - simplify server login (D Ferguson)
 *            2020-01-15 change format of displayed known projects; use ProjHandler code
 * v0.00.0018 2020-01-23 modified openProjectAction to include Browse (N Tolleshaug)
 * v0.00.0019 2020-03-04 made server related buttons not visible (temporary change) (DF)
 * v0.00.0021 2020-04-11 add found projects at bottom of Project menu (D Ferguson)
 *            2020-05-03 add wait cursor display for long-running open actions (D Ferguson)
 * v0.00.0022 2020-07-27 add screenID for Reminder, Help xref (D Ferguson)
 * v0.01.0023 2020-08-04 changed from JDialog to extend HG0450SuperDialog (N. Tolleshaug)
 *            2020-09-11 removed Reminder icon from Toolbar (D Ferguson)
 *            2020-09-30 changed to MigLayout; fonts removed for JTattoo install (D Ferguson) 
 * v0.01.0025 2020-11-24 use location Setting for default HRE file folder (D Ferguson)
 * 			  2021-01-28 removed use of HGlobal.selectedProject (N. Tolleshaug)
 * 			  2021-03-09 JOptionPane messages moved to GUI (N. Tolleshaug)
 * 			  2021-04-11 NLS implemented (D Ferguson)
 * v0.01.0026 2021-05-05 Fix error in Open Browse error exiting project (N. Tolleshaug)
 * 			  2021-09-17 Apply tag codes to screen control buttons (D Ferguson)
 * 			  2021-09-27 Removed hidden code related to server login (D Ferguson)
 * v0.01.0027 2021-10-01 Handling of local/remote connect to database (N. Tolleshaug)
 *			  2022-02-26 Modified to use the NLS version of HGlobal (D Ferguson)
 * v0.03.0030 2022-09-22 If attempt to open already open project, exit after 1st msg (D Ferguson)
 * v0.03.0031 2024-02-18 Clear Person Recents list when opening project (D Ferguson)
 ********************************************************************************************/

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Component;
import java.awt.Cursor;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JToolBar;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SortOrder;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBProjectHandler;
import hre.nls.HG0402Msgs;

/**
 * Project Open
 * @author R Thompson
 * @version v0.03.0031
 * @since 2019-02-22
 */
	
public class HG0402ProjectOpen extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;  
	
	HG0402ProjectOpen pointThis = this;
	
	private String screenID = "40200"; //$NON-NLS-1$
 	private JPanel contents;
	private boolean newProject = false;
	private String selectedProject = ""; //$NON-NLS-1$
	private String newProjectName = ""; //$NON-NLS-1$

	private JTable table_Projects;
	
/**
 * Collect JTable for output by SuperDialog
 * JTable getDataTable()
 */
	@Override
	public JTable getDataTable() {
		return table_Projects;		
	}
	
	public void setNewProjectName(String name) {
		newProjectName = name;
	}
	
/**
 * Create the Dialog.
 */
	public HG0402ProjectOpen(HBProjectHandler pointProHand) { 
		// Setup references for HG0450
		windowID = screenID;
		helpName = "openproject"; //$NON-NLS-1$
		
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0402ProjectOpen");	 //$NON-NLS-1$
		setResizable(false);
		setTitle(HG0402Msgs.Text_4);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		contents = new JPanel();
		contents.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]20[]", "[]10[]10[]10[]10[]10[]10[]10[]10[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);;
		toolBar.add(Box.createHorizontalGlue());
		// Add icons defined in HG0450
		toolBar.add(btn_Outputicon);			
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north"); //$NON-NLS-1$
		
		JLabel lbl_Project = new JLabel(HG0402Msgs.Text_9);
		lbl_Project.setFont(lbl_Project.getFont().deriveFont(lbl_Project.getFont().getStyle() | Font.BOLD));
		contents.add(lbl_Project, "cell 0 0 2"); //$NON-NLS-1$
		
		JScrollPane scrollPane = new JScrollPane();
		contents.add(scrollPane, "cell 0 1 2"); //$NON-NLS-1$
		
		JLabel lbl_Search = new JLabel(HG0402Msgs.Text_12);
		lbl_Search.setFont(lbl_Search.getFont().deriveFont(lbl_Search.getFont().getStyle() | Font.BOLD));
		contents.add(lbl_Search, "cell 0 2 2 1"); //$NON-NLS-1$
		
		JLabel lbl_LocalProject = new JLabel(HG0402Msgs.Text_14);
		contents.add(lbl_LocalProject, "split 2,cell 0 3 2 1"); //$NON-NLS-1$
		
		JButton btn_LocalBrowse = new JButton(HG0402Msgs.Text_16);
		btn_LocalBrowse.setToolTipText(HG0402Msgs.Text_17);
		contents.add(btn_LocalBrowse, "cell 1 3"); //$NON-NLS-1$
		
		JSeparator separator = new JSeparator();
		separator.setOpaque(true);
		separator.setForeground(UIManager.getColor("scrollbar")); //$NON-NLS-1$
		separator.setPreferredSize(new Dimension(0, 3));
		contents.add(separator, "cell 0 4 2, growx"); //$NON-NLS-1$
		
		JLabel lbl_Selected = new JLabel(HG0402Msgs.Text_26);
		lbl_Selected.setFont(lbl_Selected.getFont().deriveFont(lbl_Selected.getFont().getStyle() | Font.BOLD));
		contents.add(lbl_Selected, "cell 0 5 2");	 //$NON-NLS-1$
		
		JLabel lbl_Location = new JLabel(HG0402Msgs.Text_28);
		contents.add(lbl_Location, "cell 0 6,alignx right");	 //$NON-NLS-1$
		JLabel lbl_LocationData = new JLabel();
		contents.add(lbl_LocationData, "cell 1 6");	 	//$NON-NLS-1$	
		
		JLabel lbl_ProjectName = new JLabel(HG0402Msgs.Text_31);
		contents.add(lbl_ProjectName, "cell 0 7,alignx right"); //$NON-NLS-1$
		JLabel lbl_ProjectNameData = new JLabel();
		contents.add(lbl_ProjectNameData, "cell 1 7"); 	//$NON-NLS-1$
		
		JLabel lbl_ProjectFile = new JLabel(HG0402Msgs.Text_34);
		contents.add(lbl_ProjectFile, "cell 0 8,alignx right"); //$NON-NLS-1$
		JLabel lbl_ProjectFileData = new JLabel();
		contents.add(lbl_ProjectFileData, "cell 1 8"); //$NON-NLS-1$
		
		JButton btn_Summary = new JButton(HG0402Msgs.Text_37);
		btn_Summary.setEnabled(false);
		btn_Summary.setToolTipText(HG0402Msgs.Text_38);
		contents.add(btn_Summary, "cell 0 9"); 		//$NON-NLS-1$
		
		JButton btn_Cancel = new JButton(HG0402Msgs.Text_40);
		btn_Cancel.setToolTipText(HG0402Msgs.Text_41);
		contents.add(btn_Cancel, "cell 1 9, align right, gapx 20, tag cancel"); //$NON-NLS-1$
		
		JButton btn_Open = new JButton(HG0402Msgs.Text_43);
		btn_Open.setEnabled(false);
		btn_Open.setToolTipText(HG0402Msgs.Text_44);
		contents.add(btn_Open, "cell 1 9, align right, gapx 20, tag ok"); //$NON-NLS-1$
		
		// Setup a Project table as non-editable by the user, set TableModel, and load data
		table_Projects = new JTable() { private static final long serialVersionUID = 1L;
										@Override
										public boolean isCellEditable(int row, int column) {return false;}
										};
		table_Projects.setModel(new DefaultTableModel(
								pointProHand.getUserProjectArray2D(), 
								HGlobalCode.projectTableHeader()));
		// Set the ability to sort on columns and presort column 0 (Project Name)
		table_Projects.setAutoCreateRowSorter(true);
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table_Projects.getModel());
		table_Projects.setRowSorter(sorter);
		List <RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
		sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		sorter.setSortKeys(sortKeys); 
		
		// Set tooltips, column widths and formats
		table_Projects.getTableHeader().setToolTipText(HG0402Msgs.Text_46);
		table_Projects.getColumnModel().getColumn(0).setMinWidth(150);
		table_Projects.getColumnModel().getColumn(0).setPreferredWidth(200);
		table_Projects.getColumnModel().getColumn(1).setMinWidth(150);
		table_Projects.getColumnModel().getColumn(1).setPreferredWidth(200);
		table_Projects.getColumnModel().getColumn(2).setMinWidth(100);
		table_Projects.getColumnModel().getColumn(2).setPreferredWidth(130);
		table_Projects.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JTableHeader pHeader = table_Projects.getTableHeader();	
		pHeader.setOpaque(false);
		TableCellRenderer rendererFromHeader = table_Projects.getTableHeader().getDefaultRenderer();
		JLabel headerLabel = (JLabel) rendererFromHeader;
		headerLabel.setHorizontalAlignment(SwingConstants.LEFT);;
		ListSelectionModel cellSelectionModel = table_Projects.getSelectionModel();
		cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	
		table_Projects.setPreferredScrollableViewportSize(new Dimension(530, 100));
		scrollPane.setViewportView(table_Projects);
		
		pack();
		
/***
 * CREATE ACTION LISTENERS 
 **/
		// Action code Button Browse		
		btn_LocalBrowse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				btn_LocalBrowse.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				// parameters setup as   HG0577FileChooser("Select", "HRE files (*.db)", "hre", null, null, 1);
 				HG0577FileChooser chooseFile = 
						new HG0577FileChooser(pointProHand.pointLibraryBusiness.
								setUpFileOpenChooser(HGlobal.pathHRElocation,HGlobal.defDatabaseEngine));	
				
				chooseFile.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xy = btn_LocalBrowse.getLocationOnScreen();      // Gets browse button location on screen
				chooseFile.setLocation(xy.x, xy.y);     		       // Sets chooser screen top-left corner relative to that				
				chooseFile.setVisible(true);
				btn_LocalBrowse.setCursor(Cursor.getDefaultCursor());
				if (HGlobal.chosenFilename == "" ) {   }		       // do nothing if no filename selected //$NON-NLS-1$
				else {  
					HGlobal.chosenFilename = pointProHand.pointLibraryBusiness.
							modifyFileNameChosen(HGlobal.chosenFilename, HGlobal.defDatabaseEngine);					
					HGlobal.pathProjectFolder = HGlobal.chosenFolder;					
					lbl_ProjectFileData.setText(HGlobal.chosenFilename); 
					lbl_LocationData.setText(HG0402Msgs.Text_48); 
					lbl_ProjectNameData.setText(null);
					btn_Open.setEnabled(true); 
			// Extract the Project name from the database file
			// Then add all data to openProjects table and userProjects table
			// Write out UserAUX so this new info is not lost
			// and proceed to Open actions				
					selectedProject = HGlobal.chosenFilename; //$NON-NLS-1$
					newProject = true;	
				}	
			}
		});	
		
		btn_Summary.addActionListener(new ActionListener() {
			//Displays properties of the Selected Project
			@Override
			public void actionPerformed(ActionEvent arg0) {
                int[] selectedRow = table_Projects.getSelectedRows();
                String [][] summaryData = null;
				for (int i = 0; i < selectedRow.length; i++) {
					String selectedProjectName = (String) table_Projects.getValueAt(selectedRow[i], 0);
					try {
						summaryData = pointProHand.getSummaryUserProjectAction(selectedProjectName);
					} catch (HBException hbe) {
						JOptionPane.showMessageDialog(null, HG0402Msgs.Text_51 							// Open project summar error
								+  hbe.getMessage(), HG0402Msgs.Text_52,JOptionPane.ERROR_MESSAGE);		// Project Open
					}
				}
				if (summaryData != null) {
					HG0414ProjectSummary summscreen = new HG0414ProjectSummary(summaryData); 
					summscreen.setModalityType(ModalityType.APPLICATION_MODAL);
					Point xy = btn_Summary.getLocationOnScreen();          	  // Gets Summary button location on screen
					summscreen.setLocation(xy.x + 50, xy.y);     			  // Sets screen top-left corner relative to that				
					summscreen.setVisible(true);
				} else if (HGlobal.DEBUG) System.out.println("Summary data is null"); //$NON-NLS-1$
			}
		});	
		
		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (HGlobal.showCancelmsg)								
					{if (JOptionPane.showConfirmDialog(btn_Cancel, HG0402Msgs.Text_54, HG0402Msgs.Text_55,	// Cancel project open process?  // Project Open
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: cancelling HG0402ProjectOpen");} //$NON-NLS-1$
							dispose();	}		// yes option - return to main menu
					}
				else {if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: cancelling HG0402ProjectOpen");} //$NON-NLS-1$
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
		
		btn_Open.addActionListener (new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {		
				btn_Open.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			// If new project, setup Known project entry
				if (newProject) {
					newProject = false;
	                String[] newProjectArray = {
	                		selectedProject,
	                		HGlobal.chosenFilename,
	                		HGlobal.chosenFolder,
	                		HGlobal.thisComputer,
	                		HG0402Msgs.Text_59,			// no time available for LastClosed, as project is new and now open
	                		pointProHand.getDefaultDatabaseEngine(),
	                		HG0402Msgs.Text_60};		// for last backup entry
	            // And attempt to open the newly found file
					int errorCode = pointProHand.openBrowseAction(newProjectArray, pointThis);
					btn_Open.setCursor(Cursor.getDefaultCursor());
				// If all OK, set opened project icon in project menu, clear Recents list and exit	
					if (errorCode == 0) {						
							pointProHand.mainFrame.resetProjMenuItem(selectedProject);
							pointProHand.mainFrame.removeRecents();
							if (HGlobal.writeLogs) 
									HB0711Logging.logWrite("Action: opened project: " + selectedProject + " from HG00402ProjectOpen"); //$NON-NLS-1$ //$NON-NLS-2$
							if (HGlobal.DEBUG) userInfoBrowseProject(errorCode, selectedProject);
							dispose();	
					} else if (errorCode == 1) {
						userInfoBrowseProject(errorCode, newProjectName);
	                } else if (errorCode == 2) {
	                	userInfoBrowseProject(errorCode, selectedProject);
	                } else if (errorCode == 3) {
	                	userInfoBrowseProject(errorCode, selectedProject);
	                } else if (errorCode == 4) {
	                	userInfoBrowseProject(errorCode, selectedProject);
	                }
					
			// Not new project, so open from userProject list		
				} else {
					String serverName = "";	 //$NON-NLS-1$
					String[] projectsData = null;
					int[] selectedRow = table_Projects.getSelectedRows();
					for (int i = 0; i < selectedRow.length; i++) {
						String selectedProjectName = (String) table_Projects.getValueAt(selectedRow[i], 0);
						try {
							int index = pointProHand.getUserProjectByName(selectedProjectName);
						// Get row of HGlobal.userProjects table
							projectsData = pointProHand.getUserProjectByIndex(index);
							serverName = projectsData[3];
						} catch (HBException hbe) {
							if (HGlobal.DEBUG) System.out.println("HG0402ProjectOpen error : " + projectsData[1]);	 //$NON-NLS-1$
							hbe.printStackTrace();
						}
						
						if (HGlobal.thisComputer.equals(serverName)) {		// For Local project
							int errorCode = pointProHand.openProjectLocal(selectedProjectName);
							btn_Open.setCursor(Cursor.getDefaultCursor());
						// If all OK, reset opened project icon in project menu, clear Recents list and exit		
							if (errorCode == 0) {
								pointProHand.mainFrame.removeProjMenuItem(selectedProjectName);
								pointProHand.mainFrame.resetProjMenuItem(selectedProjectName);	
								pointProHand.mainFrame.removeRecents();	
								if (HGlobal.writeLogs)
									HB0711Logging.logWrite("Action: opened known project: " + selectedProjectName + " from HG00402ProjectOpen"); //$NON-NLS-1$ //$NON-NLS-2$
								if (HGlobal.DEBUG) userInfoOpenProject(errorCode, selectedProjectName);
								dispose();
							} else if (errorCode == 1) {
								userInfoOpenProject(errorCode, selectedProjectName);
			                } else if (errorCode == 2) {
			                	userInfoOpenProject(errorCode, selectedProjectName);
			                }
						} else {			// For project on remote server
							if (HGlobal.DEBUG) System.out.println("Remote server name: " + serverName);	 //$NON-NLS-1$
							HG0575DBLogon poinLogonScreen = new HG0575DBLogon(pointProHand, projectsData);	
							poinLogonScreen.setModalityType(ModalityType.APPLICATION_MODAL);
							Point xymainPane = HG0401HREMain.mainPane.getLocationOnScreen();      
							poinLogonScreen.setLocation(xymainPane.x + 100, xymainPane.y + 100);
							poinLogonScreen.setVisible(true);
							dispose();
						}
					}
				}
			}
		});			
		
/**
 * 	Project Table selection
 *  any selection from this table replaces any pre-selection
 */
		cellSelectionModel.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {	
				int[] selectedRow = table_Projects.getSelectedRows();
				for (int i = 0; i < selectedRow.length; i++) {
					//get the Project name irrespective of what field was selected in the table
					selectedProject = (String) table_Projects.getValueAt(selectedRow[i], 0);	// Project always in col 0					
					//put the Project name, Filename, Local/remote into their label places
					lbl_ProjectNameData.setText(selectedProject);
					lbl_ProjectNameData.setVisible(true);
					lbl_ProjectFileData.setText((String)table_Projects.getValueAt(selectedRow[i], 1));
					lbl_ProjectFileData.setVisible(true);
					lbl_LocationData.setText((String)table_Projects.getValueAt(selectedRow[i], 2));
					lbl_LocationData.setVisible(true);
					btn_Summary.setEnabled(true);;
					btn_Open.setEnabled(true);
				}
			}
		});

		// If detect double-click on table_Projects row -> then open this project
		table_Projects.addMouseListener(new MouseAdapter() {
		    @Override
			public void mousePressed(MouseEvent mouseEvent) {
		    	String projectToOpen = null;
		    	table_Projects =(JTable) mouseEvent.getSource();
		        Point point = mouseEvent.getPoint();
		        int row = table_Projects.rowAtPoint(point);
		        projectToOpen = (String) table_Projects.getValueAt(row, 0);	// Project always in col 0		
		        if (mouseEvent.getClickCount() == 2 && table_Projects.getSelectedRow() != -1) {
		        	selectedProject = projectToOpen;
		        	btn_Open.doClick(); 
		        }
		    }
		});
	} // End Constructor HG0402ProjectOpen
	
/**
 * GUI user messages
 * @param errorCode
 * @param projectName
 */
	private void userInfoOpenProject(int errorCode, String projectName) {	
		String errorMess = ""; 	//$NON-NLS-1$
		String errorTitle = HG0402Msgs.Text_66;		// Open Project
		if (errorCode == 0) { 
			errorMess = HG0402Msgs.Text_67;			// Opened project: 
			JOptionPane.showMessageDialog(null, errorMess  + projectName, errorTitle,
												 JOptionPane.INFORMATION_MESSAGE);
		}		
		if (errorCode > 0) { 
			if (errorCode == 1)  errorMess = HG0402Msgs.Text_70 + projectName + HG0402Msgs.Text_71;		// Open project: ......... failed!
			if (errorCode == 2)  errorMess = HG0402Msgs.Text_68 + projectName + HG0402Msgs.Text_69;		// Open project rejected ..... already open	
			JOptionPane.showMessageDialog(null, errorMess, errorTitle, JOptionPane.ERROR_MESSAGE);
			if (errorCode == 2) dispose();	// exit immediately if project already open
		}
	}	// End userInfoOpenProject
	
	private void userInfoBrowseProject(int errorCode, String projectName) {	
		String errorMess = ""; 	//$NON-NLS-1$
		String errorTitle = HG0402Msgs.Text_73;		// Browse Open Project
		if (errorCode == 0) { 
			errorMess = HG0402Msgs.Text_74;			// Opened new project: 
			JOptionPane.showMessageDialog(null, errorMess + projectName, errorTitle,
												 JOptionPane.INFORMATION_MESSAGE);
		}		
		if (errorCode > 0) { 
			if (errorCode == 1)  errorMess = HG0402Msgs.Text_75 + projectName + HG0402Msgs.Text_76;		// New project name .... in use
			if (errorCode == 2)  errorMess = HG0402Msgs.Text_77 + projectName;							// Not able to open project 
			if (errorCode == 3)  errorMess = HG0402Msgs.Text_78 + projectName + HG0402Msgs.Text_79;		// No project name ..... in database file
			if (errorCode == 4)  errorMess = HG0402Msgs.Text_80 + projectName;							// Open project browse failed
			JOptionPane.showMessageDialog(null, errorMess , errorTitle, JOptionPane.ERROR_MESSAGE);
		}
	}	// End userInfoBrowseProject

}	//End of HG0402ProjectOpen