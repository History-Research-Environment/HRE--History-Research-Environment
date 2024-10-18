package hre.gui;
/**************************************************************************************
 * Project CopyAs - Specification 04.08 GUI_ProjectCopyAs 2020-02-29
 * v0.00.0001 2019-02-12 by R Thompson, updated by D Ferguson
 * v0.00.0008 2019-07-22 fix close/cancel code (D Ferguson)
 * v0.00.0014 2019-11-18 changes for HB0711 static classes (D Ferguson)
 * v0.00.0015 2019-12-07 changed code for HG0514 call (D Ferguson)
 * v0.00.0017 2020-01-14 updated call to HG0414 (D. Ferguson)
 * v0.00.0018 2020-02-15 added code to perform copy (N. Tolleshaug)
 * v0.00.0019 2020-02-29 log copy action; add server login call (D. Ferguson)
 *                       made server related buttons not visible (temporary change)
 * v0.00.0020 2020-03-18 fix errors in DocumentListener code (D. Ferguson)
 * v0.00.0022 2020-07-27 add screenID for Reminder, Help xref (D Ferguson)
 * v0.01.0023 2020-08-04 changed from JDialog to extend HG0450SuperDialog (D Ferguson)
 *            2020-09-11 removed Reminder icon from Toolbar (D Ferguson)
 *            2020-09-30 changed to MigLayout; fonts removed for JTattoo (D Ferguson) 
 * v0.01.0025 2020-11-24 use location Setting for default HRE file folder (D Ferguson)
 * 			  2021-01-28 removed use of HGlobal.selectedProject (N. Tolleshaug)
 * 			  2021-03-09 All JOptionPane messages moved to GUI (N. Tolleshaug)
 * 			  2021-04-18 add known projects table - allow copy from there (D Ferguson)
 * 			  2021-04-18 converted to NLS (D Ferguson)
 * v0.01.0026 2021-05-06 set max size for file/foldername label content (D Ferguson) 
 * 			  2021-09-17 Apply tag codes to screen control buttons (D Ferguson)
 * 			  2021-09-27 Removed code for server login; disallowed remote copy (D Ferguson)
 * v0.01.0027 2022-02-26 Modified to use the NLS version of HGlobal (D Ferguson)
 * v0.03.0031 2023-11-16 Slight screen layout revision (D Ferguson)
 *************************************************************************************/

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import hre.bila.HB0711Logging;
import hre.bila.HBProjectHandler;
import hre.nls.HG0408Msgs;

/**
 * Project CopyAs
 * @author R Thompson
 * @version v0.03.0031
 * @since 2019-02-12
 */

public class HG0408ProjectCopyAs extends HG0450SuperDialog {	
	private static final long serialVersionUID = 001L;
	
	private String screenID = "40800"; //$NON-NLS-1$
	private JPanel contents;
	private JTextField txt_toProjectName;
	private String copytoProjectName = ""; //$NON-NLS-1$
	private JTable table_Projects;
	private String selectedProject = ""; //$NON-NLS-1$
	private String selectedFile = ""; //$NON-NLS-1$
	private String projectData[];
	
/**
 * Create the Dialog.
 */
	public HG0408ProjectCopyAs(HBProjectHandler pointProHand) {
		// Setup references for HG0450
		windowID = screenID;
		helpName = "copyproject"; //$NON-NLS-1$
		
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0408ProjectCopy"); //$NON-NLS-1$
		
		// Cleanout the global copy variables before we start
		HGlobal.copyfromFilename = ""; //$NON-NLS-1$
		HGlobal.copytoFilename = ""; //$NON-NLS-1$
		HGlobal.copyfromFolder = ""; //$NON-NLS-1$
		HGlobal.copytoFolder = ""; //$NON-NLS-1$
		
		//Build the screen	
		setResizable(false);
		setTitle(HG0408Msgs.Text_10);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		contents = new JPanel();
		contents.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]20[][]", "[]10[]10[]10[]10[]10[]10[]10[]10[]10[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);;
		toolBar.add(Box.createHorizontalGlue());
		// Add icons defined in HG0450
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north"); //$NON-NLS-1$
		
		JLabel lbl_From = new JLabel(HG0408Msgs.Text_26);	//Copy From
		lbl_From.setFont(lbl_From.getFont().deriveFont(lbl_From.getFont().getStyle() | Font.BOLD));
		contents.add(lbl_From, "cell 0 0,alignx left"); //$NON-NLS-1$
		
		JScrollPane scrollPane = new JScrollPane();
		contents.add(scrollPane, "cell 0 1 3"); //$NON-NLS-1$				
		
		JButton btn_Browse1 = new JButton(HG0408Msgs.Text_28);	// Browse...
		btn_Browse1.setToolTipText(HG0408Msgs.Text_29);
		contents.add(btn_Browse1, "cell 0 2, alignx right");	 //$NON-NLS-1$
		
		JLabel lbl_Filename = new JLabel(HG0408Msgs.Text_31);	// Filename
		contents.add(lbl_Filename, "cell 0 3, alignx right");	 //$NON-NLS-1$
		JLabel lbl_fromFilenameData = new JLabel(""); //$NON-NLS-1$
		lbl_fromFilenameData.setMaximumSize(new Dimension(400, 23));
		contents.add(lbl_fromFilenameData, "cell 1 3 2");	 //$NON-NLS-1$
		
		JLabel lbl_Folder = new JLabel(HG0408Msgs.Text_35);		// Folder		
		contents.add(lbl_Folder, "cell 0 4, alignx right"); //$NON-NLS-1$
		JLabel lbl_fromFolderNameData = new JLabel(""); //$NON-NLS-1$
		lbl_fromFolderNameData.setMaximumSize(new Dimension(400, 23));
		contents.add(lbl_fromFolderNameData, "cell 1 4 2");	 //$NON-NLS-1$
		
		JSeparator separator = new JSeparator();
		separator.setOpaque(true);
		separator.setForeground(UIManager.getColor("scrollbar")); //$NON-NLS-1$
		separator.setPreferredSize(new Dimension(0, 3));
		contents.add(separator, "cell 0 5 3, grow"); //$NON-NLS-1$
		
		JLabel lbl_To = new JLabel(HG0408Msgs.Text_41);	// Copy To
		lbl_To.setFont(lbl_To.getFont().deriveFont(lbl_To.getFont().getStyle() | Font.BOLD));
		contents.add(lbl_To, "cell 0 6,alignx right"); //$NON-NLS-1$
		
		txt_toProjectName = new JTextField();
		txt_toProjectName.setBackground(UIManager.getColor("TextField.selectionBackground")); //$NON-NLS-1$
		txt_toProjectName.setForeground(UIManager.getColor("TextField.selectionForeground")); //$NON-NLS-1$
		txt_toProjectName.setText(HG0408Msgs.Text_45);	// --- enter new Project Name ---
		txt_toProjectName.setToolTipText(HG0408Msgs.Text_46);	// Project name to use for copied project
		txt_toProjectName.setColumns(35);
		contents.add(txt_toProjectName, "cell 1 6 2 1"); //$NON-NLS-1$
		
		JButton btn_Browse2 = new JButton(HG0408Msgs.Text_48);	// Browse...
		btn_Browse2.setEnabled(false);
		btn_Browse2.setToolTipText(HG0408Msgs.Text_49);		// Browse the file explorer for copy to location
		contents.add(btn_Browse2, "cell 0 7, alignx right"); //$NON-NLS-1$
		
		JLabel lbl_NewFolder = new JLabel(HG0408Msgs.Text_51);	// New Folder
		contents.add(lbl_NewFolder, "cell 0 8,alignx right"); //$NON-NLS-1$
		JLabel lbl_toFolderNameData = new JLabel();
		lbl_toFolderNameData.setMaximumSize(new Dimension(400, 23));
		contents.add(lbl_toFolderNameData, "cell 1 8 2"); //$NON-NLS-1$
		
		JLabel lbl_NewFilename = new JLabel(HG0408Msgs.Text_54);	// New Filename
		contents.add(lbl_NewFilename, "cell 0 9,alignx right"); //$NON-NLS-1$
		JLabel lbl_toFilenameData = new JLabel();
		lbl_toFilenameData.setMaximumSize(new Dimension(400, 23));
		contents.add(lbl_toFilenameData, "cell 1 9 2"); //$NON-NLS-1$
		
		JButton btn_Cancel = new JButton(HG0408Msgs.Text_57);	// Cancel
		btn_Cancel.setToolTipText(HG0408Msgs.Text_58);
		contents.add(btn_Cancel, "cell 2 10, align right, gapx 20, tag cancel"); //$NON-NLS-1$
		
		JButton btn_Copy = new JButton(HG0408Msgs.Text_60);		// Copy
		btn_Copy.setToolTipText(HG0408Msgs.Text_61);
		btn_Copy.setEnabled(false);
		contents.add(btn_Copy, "cell 2 10, align right, gapx 20, tag ok"); //$NON-NLS-1$
		
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
		table_Projects.getTableHeader().setToolTipText(HG0408Msgs.Text_63);
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
		// Listener for driving FileChooser for 'Copy from' location
		btn_Browse1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				btn_Browse1.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				HG0577FileChooser chooseFile = 
						new HG0577FileChooser(pointProHand.pointLibraryBusiness.
								setUpFileOpenChooser(HGlobal.pathHRElocation,HGlobal.defDatabaseEngine));
				
				chooseFile.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xy = btn_Browse1.getLocationOnScreen();      // Gets browse button location on screen
				chooseFile.setLocation(xy.x, xy.y);     		   // Sets screen top-left corner relative to that				
				chooseFile.setVisible(true);
				btn_Browse1.setCursor(Cursor.getDefaultCursor());
				// Get file and folder-names returned from file chooser 
				HGlobal.copyfromFilename = HGlobal.chosenFilename;
				HGlobal.copyfromFilename = HGlobal.copyfromFilename.trim();
				
				HGlobal.copyfromFolder = HGlobal.chosenFolder;
				lbl_fromFilenameData.setText(HGlobal.copyfromFilename);     // set filename
				lbl_fromFolderNameData.setText(HGlobal.copyfromFolder);     // set path
				HGlobal.pathProjectFolder = HGlobal.chosenFolder; 			// set last selected folder
				if (HGlobal.copyfromFilename == "" ) {   }		            // do nothing if no filename setup //$NON-NLS-1$
					else btn_Browse2.setEnabled(true);	               
			}
		});			
		
		// Clean out project prompt when Textfield gets focus
		txt_toProjectName.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent arg0) {
				btn_Copy.setEnabled(false);
				txt_toProjectName.setText("");  //$NON-NLS-1$
				txt_toProjectName.setBackground(Color.YELLOW);
				}
		});
				
		// Listener for entry of new 'Copy To' Project Name		
		DocumentListener docListen = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateFieldState();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
            	updateFieldState();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateFieldState();
            }
            protected void updateFieldState() {
            	copytoProjectName = txt_toProjectName.getText().trim();		// remove leading/trailing blanks and cleanout silly characters   
            	copytoProjectName = copytoProjectName.replaceAll("[\\/|\\\\|\\*|\\:|\\;|\\^|\\||\"|\'|\\<|\\>|\\{|\\}|\\?|\\%|,]",""); //$NON-NLS-1$ //$NON-NLS-2$
            	if (copytoProjectName.length() == 0) {
            		btn_Copy.setEnabled(false);
            		txt_toProjectName.setBackground(Color.YELLOW);
            		}
            		else {txt_toProjectName.setBackground(UIManager.getColor("TextField.selectionBackground")); //$NON-NLS-1$
            			  txt_toProjectName.setForeground(UIManager.getColor("TextField.selectionForeground")); //$NON-NLS-1$
            			  if (HGlobal.copytoFilename != "" ) btn_Copy.setEnabled(true); 	                    				   //$NON-NLS-1$
            			  	else btn_Copy.setEnabled(false);
            		}
            	}
        };
        txt_toProjectName.getDocument().addDocumentListener(docListen);	
        
        // Listener for driving FileChooser for 'Copy to' location
		btn_Browse2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				btn_Browse2.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				// Use the saved from Folder path as suggested to Folder
				HG0577FileChooser chooseFile = 
						new HG0577FileChooser(pointProHand.pointLibraryBusiness.
								setUpFileOpenChooser(HGlobal.pathProjectFolder,HGlobal.defDatabaseEngine));	
				
				chooseFile.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xy = btn_Browse2.getLocationOnScreen();      // Gets Browse2 button location on screen
				chooseFile.setLocation(xy.x, xy.y);     		   // Sets screen top-left corner relative to that				
				chooseFile.setVisible(true);
				btn_Browse2.setCursor(Cursor.getDefaultCursor());
				// Process file and folder-names returned from file chooser (the 'chosen' fields)
				if (HGlobal.chosenFilename == "" ) {   }  // do nothing if no filename setup //$NON-NLS-1$
						else {	HGlobal.copytoFilename = HGlobal.chosenFilename.toLowerCase(); 	 //make all lower case
								int test = HGlobal.copytoFilename.lastIndexOf(".db");			                        // test if filename ends in .db //$NON-NLS-1$
								if (test == -1)  { HGlobal.chosenFilename = HGlobal.chosenFilename + ".mv.db";  }		// if not, add .mv.db //$NON-NLS-1$
								HGlobal.copytoFilename = HGlobal.chosenFilename;
								HGlobal.copytoFolder = HGlobal.chosenFolder;
								lbl_toFilenameData.setText(HGlobal.copytoFilename);   	  // set filename
								lbl_toFolderNameData.setText(HGlobal.copytoFolder);  	  // set path
								HGlobal.pathProjectFolder = HGlobal.chosenFolder; // set last selected folder
								btn_Copy.setEnabled(true); 
															
								if (HGlobal.copyfromFilename.equals(HGlobal.copytoFilename)
									&& HGlobal.copyfromFolder.equals(HGlobal.copytoFolder) )  	// reject to destination if it matches from location
									 { 	JOptionPane.showMessageDialog(btn_Cancel, HG0408Msgs.Text_77, HG0408Msgs.Text_78,JOptionPane.ERROR_MESSAGE);
										HGlobal.copytoFilename = ""; //$NON-NLS-1$
										HGlobal.copytoFolder = ""; //$NON-NLS-1$
										lbl_toFilenameData.setText(HGlobal.copytoFilename);   		 // reset filename
										lbl_toFolderNameData.setText(HGlobal.copytoFolder);   		 // reset path
										btn_Copy.setEnabled(false);
									 } 
						  }  
			}	
		});		                
        
		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (HGlobal.showCancelmsg)								
					{if (JOptionPane.showConfirmDialog(btn_Cancel, HG0408Msgs.Text_81, HG0408Msgs.Text_82,
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							if (HGlobal.writeLogs) HB0711Logging.logWrite("Action:cancelling HG0408ProjectCopy"); //$NON-NLS-1$
							dispose();	}	// yes option - return to main menu
					}
				else {if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: cancelling HG0408ProjectCopy"); //$NON-NLS-1$
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

		//	Project Table selection
		cellSelectionModel.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent arg0) {	
				int[] selectedRow = table_Projects.getSelectedRows();
				for (int i = 0; i < selectedRow.length; i++) {
				// If selected row server name is not ' Local' (column 2) it is remote, so do not allow
					if ((String) table_Projects.getValueAt(selectedRow[i], 2) != HGlobalCode.getLocalText()) {
						lbl_fromFilenameData.setText(HG0408Msgs.Text_83);	// Cannot Copy Remote Proj
						lbl_fromFilenameData.setForeground(Color.RED);
						lbl_fromFilenameData.setVisible(true);
						lbl_fromFolderNameData.setVisible(false);
						btn_Browse2.setVisible(false);
						txt_toProjectName.setVisible(false);
						return;
						}
					else {	// re-enable other data fields
						lbl_fromFilenameData.setForeground(Color.BLACK);
						lbl_fromFolderNameData.setVisible(true);
						btn_Browse2.setVisible(true);
						txt_toProjectName.setVisible(true);
					}
				// If Local file, get the Project name (column 0) 
					selectedProject = ((String) table_Projects.getValueAt(selectedRow[i], 0)).trim();	
				// Put the Filename, Foldername into their label places and setup copyfrom parameters
					selectedFile = ((String)table_Projects.getValueAt(selectedRow[i], 1)).trim();
					lbl_fromFilenameData.setText(selectedFile);
					lbl_fromFilenameData.setVisible(true);
					HGlobal.copyfromFilename = selectedFile;
					int test = HGlobal.copyfromFilename.lastIndexOf(".db");	// test if filename ends in .db //$NON-NLS-1$
					if (test == -1)  { 
						HGlobal.copyfromFilename = HGlobal.copyfromFilename + ".mv.db";  // if not, add .mv.db //$NON-NLS-1$
					}	
					// find Folder name in the userProjects data
					for (int j = 0; j < HGlobal.userProjects.size(); j++) {
						projectData = HGlobal.userProjects.get(j);
						if(selectedProject.equals(projectData[0])) {
							lbl_fromFolderNameData.setText(projectData[2]);
							lbl_fromFolderNameData.setVisible(true);
							HGlobal.copyfromFolder = projectData[2];
							btn_Browse2.setEnabled(true);	
							break;
						}
					}	
				}
			}
		});
				
		// Listener for driving the Copy function
		btn_Copy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// New project for Copy Project AS
				String dataBaseName = HGlobal.chosenFilename.replace(".mv.db"," "); //$NON-NLS-1$ //$NON-NLS-2$
				dataBaseName = dataBaseName.trim();
                String[] newProjectArray = {
                		txt_toProjectName.getText(),
                		dataBaseName,
                		HGlobal.copytoFolder,
                		HGlobal.thisComputer,
                		pointProHand.currentTime("yyyy-MM-dd / HH:mm:ss"), //$NON-NLS-1$
                		pointProHand.getDefaultDatabaseEngine(),
                		HG0408Msgs.Text_90};

				File copyFrom = new File(HGlobal.copyfromFolder + File.separator + HGlobal.copyfromFilename);
				File copyTo = new File(HGlobal.copytoFolder + File.separator + HGlobal.copytoFilename);
                int errorCode = pointProHand.copyProjectAction(copyFrom, copyTo, newProjectArray);
				if (errorCode == 0) {
                	if (HGlobal.writeLogs) 
                		HB0711Logging.logWrite("Action: copied " + HGlobal.copyfromFilename +		//$NON-NLS-1$ 
                				" to " + HGlobal.copytoFilename + " by HG0408ProjectCopy"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                	userInfoCopyProjectAs(errorCode, newProjectArray[0]);
                	dispose();
                } else if (errorCode == 1) {
                	userInfoCopyProjectAs(errorCode, newProjectArray[0]);
                } else if (errorCode == 2) {
                	userInfoCopyProjectAs(errorCode, HGlobal.copytoFilename);
                } else if (errorCode == 3) {
                	userInfoCopyProjectAs(errorCode, newProjectArray[0]);
                } else if (errorCode == 4) {
                	userInfoCopyProjectAs(errorCode, newProjectArray[0]);
                }				
			}
		});		

	}	// End of HG0408ProjectCopyAs constructor
	
/**
 * GUI user translated messages
 * @param errorCode
 */
	private void userInfoCopyProjectAs(int errorCode, String name) {	
		String errorMess = ""; //$NON-NLS-1$
		String errorTitle = HG0408Msgs.Text_95;
		if (errorCode == 0) { 
			errorMess = HG0408Msgs.Text_96;
			JOptionPane.showMessageDialog(null, errorMess + name, errorTitle, JOptionPane.INFORMATION_MESSAGE);
		}		
		if (errorCode > 0) { 
			if (errorCode == 1)  errorMess = HG0408Msgs.Text_97 + HG0408Msgs.Text_98 + name + HG0408Msgs.Text_99;
			if (errorCode == 2)  errorMess = HG0408Msgs.Text_100 + HG0408Msgs.Text_101 + name + HG0408Msgs.Text_102;
			if (errorCode == 3)  errorMess = HG0408Msgs.Text_103 + name +HG0408Msgs.Text_104
					+ HG0408Msgs.Text_105;
			if (errorCode == 4)  errorMess = HG0408Msgs.Text_106 + name + HG0408Msgs.Text_107;
			JOptionPane.showMessageDialog(null, errorMess, errorTitle, JOptionPane.ERROR_MESSAGE);
		}
	}	// End userInfoCopyProjectAs
	
}	// End of HG0408ProjectCopyAs