package hre.gui;
/*************************************************************************************
 * Project Restore - Specification 04.06 GUI_ProjectRestore 2020-03-02
 * v0.00.0007 2019-03-05 by R Thompson, updated by D Ferguson
 * v0.00.0008 2019-07-21 fix close cancel code (D Ferguson)
 * v0.00.0014 2019-11-18 changes for userProjects being ArrayList (D Ferguson)
 * 						 and changes for HB0711 static classes (D Ferguson)
 * v0.00.0015 2019-12-07 changed code for HG0514 call (D Ferguson)
 * v0.00.0017 2020-01-14 updated call to HG0414 (D. Ferguson)
 * v0.00.0018 2020-02-12 implemented call to projectRestoreAction (N. Tolleshaug)
 * v0.00.0020 2020-03-18 fix errors in DocumentListener code (D. Ferguson)
 * v0.00.0022 2020-06-28 standardise file separators in filepaths (D Ferguson)
 *            2020-07-27 add screenID for Reminder, Help xref (D Ferguson)
 * v0.01.0023 2020-08-04 changed from JDialog to extend HG0450SuperDialog (D Ferguson)
 *            2020-09-11 removed Reminder icon from Toolbar (D Ferguson)
 *            2020-09-30 changed to MigLayout; fonts removed for JTattoo (D Ferguson)
 * v0.01.0024 2020-10-07 revised to clarify naming rules on restore (D Ferguson)
 * v0.01.0025 2020-10-22 restrict naming- project name must be unique (D Ferguson)
 * 			  2020-11-24 use location Setting for backup/restore to folders (D Ferguson)
 * 			  2021-01-27 remove use of HGlobal.selectedProject (D Ferguson)
 * 			  2021-02-04 Add code for External file restore; remove HG0406A (D Ferguson)
 * 			  2021-03-22 Implemented JOptionPane messages from ProjectHandler(N.Tolleshaug)
 * 			  2021-04-15 converted to NLS (D Ferguson)
 * v0.01.0026 2021-05-06 set max size for file/foldername label content (D Ferguson)
 * 			  2021-09-17 Apply tag codes to screen control buttons (D Ferguson)
 * v0.03.0031 2024-10-01 Organize imports (D Ferguson)
 ************************************************************************************/

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import hre.bila.HB0711Logging;
import hre.bila.HBProjectHandler;
import hre.nls.HG0406Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Project Restore
 * @author R Thompson
 * @version v0.03.0031
 * @since 2019-03-05
 */

public class HG0406ProjectRestore extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	private String screenID = "40600"; //$NON-NLS-1$
	private JPanel contents;
	private JTextField txt_ProjectNameNewData, txt_FileNameToData;
	private String newRestoreProjectName = ""; //$NON-NLS-1$
	private String newRestoreFileName = ""; //$NON-NLS-1$
	private String selectedProject = ""; //$NON-NLS-1$

	/**
	 * Create the Dialog.
	 */
	public HG0406ProjectRestore(HBProjectHandler pointProHand) {
		// Setup references for HG0450
		windowID = screenID;
		helpName = "restoreproject"; //$NON-NLS-1$

		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0406 Project Restore"); //$NON-NLS-1$

		// Cleanout the global restore variables before we start
		HGlobal.copyfromFilename = ""; //$NON-NLS-1$
		HGlobal.copyfromFolder = ""; //$NON-NLS-1$
		HGlobal.copytoFilename = "";	 //$NON-NLS-1$
		HGlobal.copytoFolder = ""; //$NON-NLS-1$
		HGlobal.zipFromFilename = "";	 //$NON-NLS-1$
		HGlobal.zipFromFolder = "";		 //$NON-NLS-1$

		setResizable(false);
		setTitle(HG0406Msgs.Text_12);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		contents = new JPanel();
		contents.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]20[400!]", //$NON-NLS-1$ //$NON-NLS-2$
				"[]10[]10[]10[]10[]10[]10[]10[]10[]10[]10[]10[]10[]")); //$NON-NLS-1$

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
		toolBar.add(Box.createHorizontalGlue());
		// Add icon defined in HG0450
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north"); //$NON-NLS-1$

		JLabel lbl_From = new JLabel(HG0406Msgs.Text_17);
		lbl_From.setFont(lbl_From.getFont().deriveFont(lbl_From.getFont().getStyle() | Font.BOLD));
		contents.add(lbl_From, "cell 0 0 2 1"); //$NON-NLS-1$

		JButton btn_BrowseBackup = new JButton(HG0406Msgs.Text_19);
		btn_BrowseBackup.setToolTipText(HG0406Msgs.Text_20);
		btn_BrowseBackup.setEnabled(true);
		contents.add(btn_BrowseBackup, "cell 0 1, alignx right"); //$NON-NLS-1$

		JLabel lbl_FolderFrom = new JLabel(HG0406Msgs.Text_22);
		contents.add(lbl_FolderFrom, "cell 0 2,alignx right"); //$NON-NLS-1$
		JLabel lbl_FolderFromData = new JLabel();
		lbl_FolderFromData.setMaximumSize(new Dimension(400, 23));
		contents.add(lbl_FolderFromData, "cell 1 2"); //$NON-NLS-1$

		JLabel lbl_FilenameFrom = new JLabel(HG0406Msgs.Text_25);
		contents.add(lbl_FilenameFrom, "cell 0 3,alignx right"); //$NON-NLS-1$
		JLabel lbl_FilenameFromData = new JLabel();
		lbl_FilenameFromData.setMaximumSize(new Dimension(400, 23));
		contents.add(lbl_FilenameFromData, "cell 1 3"); //$NON-NLS-1$

		JLabel lbl_To = new JLabel(HG0406Msgs.Text_28);
		lbl_To.setFont(lbl_To.getFont().deriveFont(lbl_To.getFont().getStyle() | Font.BOLD));
		contents.add(lbl_To, "cell 0 4 2 1"); //$NON-NLS-1$

		JTextArea txt_Restore = new JTextArea();
		txt_Restore.setOpaque(false);
		txt_Restore.setWrapStyleWord(true);
		txt_Restore.setLineWrap(true);
		txt_Restore.setText(HG0406Msgs.Text_30
				+ HG0406Msgs.Text_31
				+ HG0406Msgs.Text_32);
		txt_Restore.setEditable(false);
		contents.add(txt_Restore, "cell 0 5 2, growx, growy"); //$NON-NLS-1$

		JButton btn_BrowseTo = new JButton(HG0406Msgs.Text_34);
		btn_BrowseTo.setEnabled(false);
		btn_BrowseTo.setToolTipText(HG0406Msgs.Text_35);
		contents.add(btn_BrowseTo, "cell 0 6 1, alignx right"); //$NON-NLS-1$

		JLabel lbl_FolderTo = new JLabel(HG0406Msgs.Text_37);
		contents.add(lbl_FolderTo, "cell 0 7,alignx right"); //$NON-NLS-1$
		JLabel lbl_FolderToData = new JLabel();
		contents.add(lbl_FolderToData, "cell 1 7"); //$NON-NLS-1$

		JLabel lbl_FilenameTo = new JLabel(HG0406Msgs.Text_40);
		contents.add(lbl_FilenameTo, "cell 0 8,alignx right"); //$NON-NLS-1$
		txt_FileNameToData = new JTextField(HG0406Msgs.Text_42);
		contents.add(txt_FileNameToData, "cell 1 8, growx"); //$NON-NLS-1$

		JLabel lbl_ProjectNameNew = new JLabel(HG0406Msgs.Text_44);
		contents.add(lbl_ProjectNameNew, "cell 0 9,alignx right"); //$NON-NLS-1$
		txt_ProjectNameNewData = new JTextField(HG0406Msgs.Text_46);
		contents.add(txt_ProjectNameNewData, "cell 1 9, growx"); //$NON-NLS-1$

		JButton btn_Cancel = new JButton(HG0406Msgs.Text_48);
		btn_Cancel.setToolTipText(HG0406Msgs.Text_49);
		contents.add(btn_Cancel, "cell 1 10, align right, gapx 20, tag cancel"); //$NON-NLS-1$

		JButton btn_Restore = new JButton(HG0406Msgs.Text_51);
		btn_Restore.setEnabled(false);
		btn_Restore.setToolTipText(HG0406Msgs.Text_52);
		contents.add(btn_Restore, "cell 1 10, align right, gapx 20, tag ok"); //$NON-NLS-1$

		JSeparator separator2 = new JSeparator();
		separator2.setBackground(UIManager.getColor("windowBorder")); //$NON-NLS-1$
		separator2.setOpaque(true);
		separator2.setPreferredSize(new Dimension(0, 3));
		contents.add(separator2, "cell 0 11 2, growx"); //$NON-NLS-1$

		// Add controls for restore of External files
		JLabel lbl_ExtFiles = new JLabel(HG0406Msgs.Text_56);
		lbl_ExtFiles.setFont(lbl_ExtFiles.getFont().deriveFont(lbl_ExtFiles.getFont().getStyle() | Font.BOLD));
		contents.add(lbl_ExtFiles, "cell 0 12 2 1"); //$NON-NLS-1$

		JButton btn_ExtFromBrowse = new JButton(HG0406Msgs.Text_58);
		btn_ExtFromBrowse.setToolTipText(HG0406Msgs.Text_59);
		contents.add(btn_ExtFromBrowse, "cell 0 13, alignx right"); //$NON-NLS-1$

		JLabel lbl_ExtFromFilename = new JLabel(HG0406Msgs.Text_61);
		contents.add(lbl_ExtFromFilename, "cell 0 14, alignx right"); //$NON-NLS-1$
		JLabel lbl_ExtFromFilenameData = new JLabel();
		lbl_ExtFromFilenameData.setMaximumSize(new Dimension(400, 23));
		contents.add(lbl_ExtFromFilenameData, "cell 1 14");   //$NON-NLS-1$

		JLabel lbl_ExtFromFolder = new JLabel(HG0406Msgs.Text_64);
		contents.add(lbl_ExtFromFolder, "cell 0 15, alignx right"); //$NON-NLS-1$
		JLabel lbl_ExtFromFolderData = new JLabel();
		lbl_ExtFromFolderData.setMaximumSize(new Dimension(400, 23));
		contents.add(lbl_ExtFromFolderData, "cell 1 15");		 //$NON-NLS-1$

		JButton btn_ExtToBrowse = new JButton(HG0406Msgs.Text_67);
		btn_ExtToBrowse.setToolTipText(HG0406Msgs.Text_68);
		contents.add(btn_ExtToBrowse, "cell 0 16, alignx right"); //$NON-NLS-1$

		JLabel lbl_ExtToFolder = new JLabel(HG0406Msgs.Text_70);
		contents.add(lbl_ExtToFolder, "cell 0 17, alignx right"); //$NON-NLS-1$
		JLabel lbl_ExtToFolderData = new JLabel();
		lbl_ExtToFolderData.setMaximumSize(new Dimension(400, 23));
		contents.add(lbl_ExtToFolderData, "cell 1 17"); //$NON-NLS-1$

		JButton btn_ExtCancel = new JButton(HG0406Msgs.Text_73);
		btn_ExtCancel.setToolTipText(HG0406Msgs.Text_74);
		contents.add(btn_ExtCancel, "cell 1 18, align right, gapx 20, tag cancel"); //$NON-NLS-1$

		JButton btn_ExtRestore = new JButton(HG0406Msgs.Text_76);
		btn_ExtRestore.setEnabled(false);
		btn_ExtRestore.setToolTipText(HG0406Msgs.Text_77);
		contents.add(btn_ExtRestore, "cell 1 18, align right, gapx 20, tag ok");	//$NON-NLS-1$

		pack();

/***
 * CREATE ACTION LISTENERS
 **/
		//buttons
		btn_BrowseBackup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				btn_BrowseBackup.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				HG0577FileChooser chooseFile =
						new HG0577FileChooser(pointProHand.pointLibraryBusiness.
								setUpFileRestoreChooser(HGlobal.pathHREbackups,HGlobal.defDatabaseEngine));
				chooseFile.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xy = btn_BrowseTo.getLocationOnScreen();      // Gets browse button location on screen
				chooseFile.setLocation(xy.x+100, xy.y-100);     	// Sets screen top-left corner relative to that
				chooseFile.setVisible(true);
				btn_BrowseBackup.setCursor(Cursor.getDefaultCursor());
				lbl_FilenameFromData.setText(HGlobal.chosenFilename);
				lbl_FolderFromData.setText(HGlobal.chosenFolder);
				HGlobal.zipFromFolder = HGlobal.chosenFolder;
				// Get file and folder-names returned from file chooser
				HGlobal.zipFromFilename = HGlobal.chosenFilename;
				HGlobal.zipFromFilename = HGlobal.zipFromFilename.trim();
				//Backup filenames are built from Project name + date + time,
				// so extract the project name from the Backup filename
				String [] nameElements = HGlobal.zipFromFilename.split("_"); //$NON-NLS-1$
				selectedProject = nameElements[0];
				btn_BrowseTo.setEnabled(true);
			}
		});

		btn_BrowseTo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				btn_BrowseTo.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				HG0577FileChooser chooseFile =
						new HG0577FileChooser(pointProHand.pointLibraryBusiness.
								setUpFolderChooser(HGlobal.pathHRElocation,HGlobal.defDatabaseEngine));
				chooseFile.setModalityType(ModalityType.APPLICATION_MODAL);
				btn_BrowseTo.setCursor(Cursor.getDefaultCursor());
				Point xy = btn_BrowseTo.getLocationOnScreen();       // Gets chooser button location on screen
				chooseFile.setLocation(xy.x+100, xy.y-100);          // Sets screen top-left corner relative to that
				chooseFile.setVisible(true);

				// Process folder-name and filename returned from file chooser (the ''chosen' fields)
				if (HGlobal.chosenFilename == "" ) {   }		         			// do nothing if no filename setup //$NON-NLS-1$
					else {String lower = HGlobal.chosenFilename.toLowerCase();  	// make filename all lower case
						int test = lower.lastIndexOf(".db");												// test if it ends in .db //$NON-NLS-1$
						if (test == -1)  { HGlobal.chosenFilename = HGlobal.chosenFilename + ".mv.db"; }	// if not, add .mv.db //$NON-NLS-1$
						//lbl_FilenameToData.setText(HGlobal.chosenFilename);    // set filename
						lbl_FolderToData.setText(HGlobal.chosenFolder);  // set path
						HGlobal.copytoFolder = HGlobal.chosenFolder;
						HGlobal.copytoFilename = HGlobal.chosenFilename;
						String zipName = ""; //$NON-NLS-1$
						try {
							ZipFile zipFile = new ZipFile(HGlobal.zipFromFolder + File.separator + HGlobal.zipFromFilename);
							Enumeration<?> enu = zipFile.entries();
							while (enu.hasMoreElements()) {
								ZipEntry zipEntry = (ZipEntry) enu.nextElement();
								zipName = zipEntry.getName();
							}
							zipFile.close();
							zipName = zipName.replace(".mv.db"," "); //$NON-NLS-1$ //$NON-NLS-2$
							zipName = zipName.trim();
							txt_FileNameToData.setText(zipName);
							HGlobal.copytoFilename = zipName;
						} catch (IOException e) {
							System.out.printf("Restore new project error - zip file: " + zipName); //$NON-NLS-1$
							e.printStackTrace();
						}
					}
			}
		});

		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				btn_Cancel.setCursor(Cursor.getDefaultCursor());
				if (HGlobal.showCancelmsg)
					{if (JOptionPane.showConfirmDialog(btn_Cancel, HG0406Msgs.Text_87, HG0406Msgs.Text_88,
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: cancelling HG0406 Project Restore");} //$NON-NLS-1$
							dispose();	}	// yes option - return to main menu
					}
				else {if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: cancelling HG0406 Project Restore");} //$NON-NLS-1$
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

		btn_Restore.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// Setup warning messages, etc
				String fromRestoreFilePath = HGlobal.zipFromFolder + File.separator + HGlobal.zipFromFilename;
				String warningText1 = HG0406Msgs.Text_91
						+ HG0406Msgs.Text_92
						+ HG0406Msgs.Text_93 + newRestoreProjectName + HG0406Msgs.Text_94
						+ HG0406Msgs.Text_95;
				String warningText2 = HG0406Msgs.Text_96;
				String warningText3 = HG0406Msgs.Text_97;
				String warningText4 = HG0406Msgs.Text_98;

				String [] nameElements = HGlobal.zipFromFilename.split("_"); //$NON-NLS-1$

				if (HGlobal.DEBUG) System.out.println("Backup project name: " + nameElements[0] + //$NON-NLS-1$
						" / Selected project: " + selectedProject); //$NON-NLS-1$

				// Now check if we are trying to restore over same file location, name and project name
				// as an existing project - issue warning if so, but user can proceed.
				// If they decide to stop, reset ALL restore parameters so they must start again
            	if (nameElements[0].equals(selectedProject)
            			& isFileLocationSame(HGlobal.copytoFilename, HGlobal.copytoFolder) ) {
					if (JOptionPane.showConfirmDialog(null, warningText1, HG0406Msgs.Text_102,
    						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
        						JOptionPane.showMessageDialog(btn_Cancel, warningText2,
        													HG0406Msgs.Text_103,JOptionPane.WARNING_MESSAGE);
        						txt_FileNameToData.setText(HG0406Msgs.Text_104);
        						txt_ProjectNameNewData.setText(HG0406Msgs.Text_105);
        						btn_Restore.setEnabled(false);
								}
					else {
   					 	if (HGlobal.DEBUG) System.out.println(HG0406Msgs.Text_106 + selectedProject);
   					 	// Restore over existing project
   						int errorCode = pointProHand.restoreProjectActionSelect(selectedProject,fromRestoreFilePath);
	   					 if (errorCode == 0) {
	   						 userInfoRestoreProject(errorCode, selectedProject, fromRestoreFilePath);
	   						 dispose();
	   					 } else if (errorCode == 1) {
	   						 userInfoRestoreProject(errorCode, selectedProject, fromRestoreFilePath);
	   					 } else if (errorCode == 2) {
	   						 userInfoRestoreProject(errorCode, selectedProject, fromRestoreFilePath);
	   					 }
					}
    			}
            	else {
					// We still may have a problem, in that we cannot allow the same filename/location
					// even if the project name was different as that means 2 projects inside 1 file.
					if (isFileLocationSame(HGlobal.copytoFilename, HGlobal.copytoFolder)) {
						JOptionPane.showMessageDialog(btn_Cancel, warningText3 + warningText2,
											HG0406Msgs.Text_107,JOptionPane.WARNING_MESSAGE);
						txt_FileNameToData.setText(HG0406Msgs.Text_108);
						txt_ProjectNameNewData.setText(HG0406Msgs.Text_109);
						btn_Restore.setEnabled(false);
						}
						else {
							// Now we have a new project to create for a new file/location combination,
							// BUT we can't allow Project name reuse even if file/location is different
							// as then we can't distinguish projects by name.
							String newProjectName = txt_ProjectNameNewData.getText();
							// test for project name in use....
							if (pointProHand.isProjectNameUsed(newProjectName,HGlobal.userProjects))  {
								JOptionPane.showMessageDialog(btn_Cancel, warningText4 + warningText2,
										HG0406Msgs.Text_110,JOptionPane.WARNING_MESSAGE);
										txt_ProjectNameNewData.setText(HG0406Msgs.Text_111);
										btn_Restore.setEnabled(false);
								}
								else {
									if (HGlobal.DEBUG) System.out.println("Restore to new folder: " + newProjectName); //$NON-NLS-1$
									// Finally we're good to do the restore...
									String[] newProjectArray = {
											newProjectName,
											HGlobal.copytoFilename,
											HGlobal.copytoFolder,
											HGlobal.thisComputer,
											pointProHand.currentTime("yyyy-MM-dd / HH:mm:ss"), //$NON-NLS-1$
											pointProHand.getDefaultDatabaseEngine(),
											HG0406Msgs.Text_114};
									int errorCode =  pointProHand.restoreProjectActionNew(newProjectArray,fromRestoreFilePath);
					   				if (errorCode == 0) {
					   					userInfoRestoreProjectNew(errorCode, newProjectArray[0], fromRestoreFilePath);
										if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: performed Project File Restore in HG0406");	 //$NON-NLS-1$
										// Reset screen fields for another restore if user wants to do it
										lbl_FilenameFromData.setText(null);
										lbl_FolderFromData.setText(null);
										lbl_FolderToData.setText(null);
										txt_FileNameToData.setText(HG0406Msgs.Text_116);
										txt_ProjectNameNewData.setText(HG0406Msgs.Text_117);
										btn_Restore.setEnabled(false);
										// Check what user now wants to do
										if (JOptionPane.showConfirmDialog(btn_Restore,
												HG0406Msgs.Text_118
												+ HG0406Msgs.Text_119
												+ HG0406Msgs.Text_120, HG0406Msgs.Text_121,
												JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
													dispose();		// Yes option - return to main menu
												}

				   					 } else if (errorCode == 1) {
				   						 userInfoRestoreProjectNew(errorCode, newProjectArray[0], fromRestoreFilePath);
				   					 }
								}
						}
            	}
			}
		});

		// Listeners for entry of new Project 'Restore To' File Name
		// Clean out file prompt when field gets focus
		txt_FileNameToData.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent arg0) {
				txt_FileNameToData.setText("");  //$NON-NLS-1$
				txt_FileNameToData.setBackground(Color.YELLOW);
				}
			});
		DocumentListener fileListen = new DocumentListener() {
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
            	newRestoreFileName = txt_FileNameToData.getText().trim();		// remove any leading/trailing blanks and remove silly characters
            	newRestoreFileName = newRestoreFileName.replaceAll("[\\/|\\\\|\\*|\\:|\\;|\\^|\\||\"|\'|\\<|\\>|\\{|\\}|\\?|\\%|,]",""); //$NON-NLS-1$ //$NON-NLS-2$
            	if (newRestoreFileName.length() == 0 ) {
            		txt_FileNameToData.setBackground(Color.YELLOW);
            		}
            		else {txt_FileNameToData.setForeground(UIManager.getColor("TextField.selectionForeground")); //$NON-NLS-1$
            			  txt_FileNameToData.setBackground(UIManager.getColor("TextField.selectionBackground")); //$NON-NLS-1$
            			  HGlobal.copytoFilename = newRestoreFileName;
	        			  }
            }
        };
        txt_FileNameToData.getDocument().addDocumentListener(fileListen);

		// Listeners for entry of new 'Restore To' Project Name
		// Clean out project prompt when field gets focus
		txt_ProjectNameNewData.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent arg0) {
				txt_ProjectNameNewData.setText("");  //$NON-NLS-1$
				txt_ProjectNameNewData.setBackground(Color.YELLOW);
				}
			});
		DocumentListener projListen = new DocumentListener() {
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
            	newRestoreProjectName = txt_ProjectNameNewData.getText().trim();		// remove any leading/trailing blanks and remove silly characters
            	newRestoreProjectName = newRestoreProjectName.replaceAll("[\\/|\\\\|\\*|\\:|\\;|\\^|\\||\"|\'|\\<|\\>|\\{|\\}|\\?|\\%|,]",""); //$NON-NLS-1$ //$NON-NLS-2$
            	if (newRestoreProjectName.length() == 0 ) {
            		txt_ProjectNameNewData.setBackground(Color.YELLOW);
            		btn_Restore.setEnabled(false);
            		}
            	else {txt_ProjectNameNewData.setForeground(UIManager.getColor("TextField.selectionForeground")); //$NON-NLS-1$
            		  txt_ProjectNameNewData.setBackground(UIManager.getColor("TextField.selectionBackground")); //$NON-NLS-1$
            		  selectedProject = newRestoreProjectName;
        			  btn_Restore.setEnabled(true);
        			  }
            }
        };
        txt_ProjectNameNewData.getDocument().addDocumentListener(projListen);

		// Listeners from here on are for External File Restore activities

        // Listener for finding External Zip file to restore
		btn_ExtFromBrowse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				btn_ExtFromBrowse.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				HG0577FileChooser chooseFile=new HG0577FileChooser("Open", HG0406Msgs.Text_133, "zip", null, HGlobal.pathExtbackups, 1); //$NON-NLS-1$ //$NON-NLS-2$
				chooseFile.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xy = btn_ExtFromBrowse.getLocationOnScreen();       // Gets chooser button location on screen
				chooseFile.setLocation(xy.x, xy.y);     		       // Sets screen top-left corner relative to that
				chooseFile.setVisible(true);
				btn_ExtFromBrowse.setCursor(Cursor.getDefaultCursor());
				// Get returned folder and filename values and display them
				lbl_ExtFromFolderData.setText(HGlobal.fromExtFolder);
				lbl_ExtFromFilenameData.setText(HGlobal.fromExtFile);
			}
		});

		// Listener for finding Ext file's restore location
		btn_ExtToBrowse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				btn_ExtToBrowse.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				HG0577FileChooser chooseFile=new HG0577FileChooser("Select", HG0406Msgs.Text_136, " ", null, HGlobal.pathExtbackups, 2); //$NON-NLS-1$ //$NON-NLS-2$
				chooseFile.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xy = btn_ExtToBrowse.getLocationOnScreen();
				chooseFile.setLocation(xy.x, xy.y);
				chooseFile.setVisible(true);
				btn_ExtToBrowse.setCursor(Cursor.getDefaultCursor());
				// Get returned folder value and display
				lbl_ExtToFolderData.setText(HGlobal.chosenFolder);
				btn_ExtRestore.setEnabled(true);
			}
		});

		// Listener for clicking Cancel in External section - same as Cancel button
		btn_ExtCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				HGlobal.fromExtFolder = "";		// on Cancel, clean out relevant fields //$NON-NLS-1$
				HGlobal.fromExtFile = ""; 		//$NON-NLS-1$
		    	btn_Cancel.doClick();
		    }
		});

		// Listener for Restore process for External zip file
		btn_ExtRestore.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// perform restore
				String zipFileToRestore = HGlobal.fromExtFolder + File.separator + HGlobal.fromExtFile;
				HG0401HREMain.mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				int errorCode = pointProHand.restoreExternalAction(zipFileToRestore, HGlobal.chosenFolder);
				 if (errorCode == 0) {
					 userInfoRestoreExternal(errorCode, zipFileToRestore, HGlobal.chosenFolder);
					 dispose();
				 } else if (errorCode == 2) {
					 userInfoRestoreExternal(errorCode, zipFileToRestore, HGlobal.chosenFolder);
					 dispose();
				 }

				HG0401HREMain.mainFrame.setCursor(Cursor.getDefaultCursor());
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: performed External File Restore in HG0406");			 //$NON-NLS-1$
			}
		});

	}	// End of HG0406 constructor

/**
 * Check if file location, name match a userProjects entry
 * @param fileName - filename
 * @param folderName - file location
 * @return - found = true
 */
	private boolean isFileLocationSame(String fileName, String folderName) {
		for (int i = 0; i < HGlobal.userProjects.size(); i++) {
			if (HGlobal.userProjects.get(i)[1].equals(fileName)
					& HGlobal.userProjects.get(i)[2].equals(folderName) ) return true;
		}
		return false;
	}	// End isFileNameUsed

/**
 * GUI user messages
 * @param errorCode
 * @param restoreName (project or external)
 * @param folder (from file path or to Folder)
 */
	private void userInfoRestoreProjectNew(int errorCode, String restoreProjectName, String fromBackupFilePath) {
		String errorMessage = ""; //$NON-NLS-1$
		String errorTitle = HG0406Msgs.Text_142;
		if (errorCode == 0) {
			errorMessage = HG0406Msgs.Text_143
					+ HG0406Msgs.Text_144 + restoreProjectName
					+ HG0406Msgs.Text_145 + fromBackupFilePath;
			JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.INFORMATION_MESSAGE);
		}
		if (errorCode > 0) {
			if (errorCode == 1)  errorMessage =  HG0406Msgs.Text_146
									+ restoreProjectName
									+ HG0406Msgs.Text_147;
			if (errorCode == 2)  errorMessage = HG0406Msgs.Text_148 + restoreProjectName + HG0406Msgs.Text_149;
			JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);
		}
	}	// End userInfoRestoreProjectNew

	private void userInfoRestoreProject(int errorCode, String restoreProjectName, String fromBackupFilePath) {
		String errorMessage = ""; //$NON-NLS-1$
		String errorTitle = HG0406Msgs.Text_151;
		if (errorCode == 0) {
			errorMessage = HG0406Msgs.Text_152
					+ HG0406Msgs.Text_153 + restoreProjectName
					+ HG0406Msgs.Text_154 + fromBackupFilePath;
			JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.INFORMATION_MESSAGE);
		}
		if (errorCode > 0) {
			if (errorCode == 1)  errorMessage =  HG0406Msgs.Text_155
									+ restoreProjectName
									+ HG0406Msgs.Text_156;
			if (errorCode == 2)  errorMessage = HG0406Msgs.Text_157 + restoreProjectName + HG0406Msgs.Text_158;
			JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);
		}
	}	// End userInfoRestoreProject

	private void userInfoRestoreExternal(int errorCode, String restoreExtName, String toFolder) {
		String errorMessage = ""; //$NON-NLS-1$
		String errorTitle = HG0406Msgs.Text_160;
		if (errorCode == 0) {
			errorMessage = HG0406Msgs.Text_161
					+ HG0406Msgs.Text_162 + restoreExtName
					+ HG0406Msgs.Text_163 + toFolder;
			JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.INFORMATION_MESSAGE);
		}
		if (errorCode == 1) {
			errorMessage = HG0406Msgs.Text_164 + restoreExtName + HG0406Msgs.Text_165 + toFolder;
			JOptionPane.showMessageDialog(null, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);
		}
	}	// End userInfoRestoreExternal

}	// End of HG0406
