package hre.gui;
/*****************************************************************************************************
 * TMG Project Import - Specification 04.17 GUI_TMGImport
 * v0.00.0020 2020-04-08 Project New code reused for TMG import to HRE (N. Tolleshaug)
 * v0.00.0022 2020-05-14 Removed server code; added guidance tips to GUI; new Help entry (D Ferguson)
 *            2020-07-10 adjusted layout. tooltips; guidance text (D Ferguson)
 *            2020-07-24 renamed from HG0411 to HG0417 (D Ferguson)
 *            2020-07-27 add screenID for Help xref (D Ferguson)
 * v0.01.0023 2020-08-04 changed from JDialog to extend HG0450SuperDialog (D Ferguson)
 *            2020-09-11 removed Reminder icon from Toolbar (D Ferguson)
 *            2020-09-24 converted to MigLayout and add Instructions (D Ferguson)
 *            2020-09-30 fonts removed for JTattoo install (D Ferguson)
 * v0.01.0025 2020-11-24 use location Setting for default HRE file folder (D Ferguson)
 * 			  2021-03-22 implemented JOptionPane from Tools (N. Tolleshaug)
 * v0.01.0026 2021-05-01 allow return to set Project name after setting File/Folder (D Ferguson)
 * 			  2021-09-06 remove File/Folder fields and put in btn_Browse field (D Ferguson)
 * v0.03.0030 2023-08-28 fix long filepath's distorting screen layout (D Ferguson)
 * 			  2023-09-03 add hidden panel for import debug switches (D Ferguson)
 * 			  2023-09-03 Option message create project removed for DEBUG = false (N.Tolleshaug)
 * 			  2023-09-03 Implemented monitor flags and redirect console output (N.Tolleshaug)
 * 			  2023-09-08 Changed to DEBUG flag for console output (N.Tolleshaug)
 * 		 	  2023-09-28 Implement NLS of new debug area (D Ferguson)
 * v0.03.0031 2024-11-30 Replace JoptionPane 'null' locations with 'contents' (D Ferguson)
 ***************************************************************************************************/

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBToolHandler;
import hre.nls.HG0417Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * TMGProject Import
 * @author R Thompson originally
 * @version v0.03.0031
 * @since 2019-07-21
 */

public class HG0417TMGProjectImport extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	private String screenID = "41700"; //$NON-NLS-1$
	private JPanel contents;
	private JTextField txt_ProjectName;
	private String newProjectName = ""; //$NON-NLS-1$
	private boolean newFilenameSet = false;

	JCheckBox trace, tmgTrace, hreTrace, flagCheck, exhCheck, debug;

/**
 * Create the Dialog.
 */
	public HG0417TMGProjectImport(HBToolHandler pointToolHand) {
		// Setup references for HG0450
		windowID = screenID;
		helpName = "tmgimport"; //$NON-NLS-1$

		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0417TMGProjectImport"); //$NON-NLS-1$
		setResizable(false);
		setTitle(HG0417Msgs.Text_4);	// Import a TMG project to HRE
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		contents = new JPanel();
		contents.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "10[]10[]10", "20[]20[]20[]20[]30[]10[]"));	 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
		JButton btn_TMGicon = new JButton(""); //$NON-NLS-1$
		btn_TMGicon.setIcon(new ImageIcon(getClass().getResource("/hre/images/tmg_64.png"))); //$NON-NLS-1$
		toolBar.add(btn_TMGicon);
		Component rigidArea = Box.createRigidArea(new Dimension(30,64));
		rigidArea.setMaximumSize(new Dimension(30, 64));
		toolBar.add(rigidArea);
		JButton btn_Arrowicon = new JButton(""); //$NON-NLS-1$
		btn_Arrowicon.setIcon(new ImageIcon(getClass().getResource("/hre/images/rightarrow-32.png"))); //$NON-NLS-1$
		toolBar.add(btn_Arrowicon);
		toolBar.add(Box.createRigidArea(new Dimension(30,64)));
		JButton btn_HREicon = new JButton(""); //$NON-NLS-1$
		btn_HREicon.setIcon(new ImageIcon(getClass().getResource("/hre/images/HRE-64.png"))); //$NON-NLS-1$
		toolBar.add(btn_HREicon);
		toolBar.add(Box.createHorizontalGlue());
		// Over-ride Help icon defined in HG0450
		btn_Helpicon.setMaximumSize(new Dimension(32, 32));
		btn_Helpicon.setIcon(new ImageIcon(getClass().getResource("/hre/images/help_BW_32.png"))); //$NON-NLS-1$
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north"); //$NON-NLS-1$

		JTextArea txt_Instruct = new JTextArea(HG0417Msgs.Text_16 +		// TMG import is a 3-step process:
												HG0417Msgs.Text_17 +	// 1. define the name of your new HRE Project;
												HG0417Msgs.Text_18 +	// 2. choose where to place the HRE Database file and give the file a name;
												HG0417Msgs.Text_19);	// 3. locate your TMG project's PJC file and start the import process.
		txt_Instruct.setOpaque(false);
		contents.add(txt_Instruct, "cell 0 0 2"); //$NON-NLS-1$

		JLabel lbl_ProjectName = new JLabel(HG0417Msgs.Text_21);	// Define the Project Name
		contents.add(lbl_ProjectName, "cell 0 1");		 //$NON-NLS-1$

		txt_ProjectName = new JTextField();
		txt_ProjectName.setHorizontalAlignment(SwingConstants.LEFT);
		txt_ProjectName.setToolTipText(HG0417Msgs.Text_23);	// Enter the new HRE Project name
		txt_ProjectName.setText(HG0417Msgs.Text_24);		// Step 1: enter the new HRE project's name
		txt_ProjectName.setColumns(30);
		contents.add(txt_ProjectName, "cell 1 1, growx"); 	//$NON-NLS-1$

		JLabel lbl_Location = new JLabel(HG0417Msgs.Text_26);	// Define the file Location
		contents.add(lbl_Location, "cell 0 2, alignx right"); 	//$NON-NLS-1$

		JButton btn_Browse = new JButton(HG0417Msgs.Text_28);	// Step 2: set the name/location of the new HRE database
		btn_Browse.setHorizontalAlignment(SwingConstants.LEFT);
		btn_Browse.setToolTipText(HG0417Msgs.Text_29);			// Select location and enter new HRE database name
		btn_Browse.setVisible(true);
		btn_Browse.setEnabled(false);
		contents.add(btn_Browse, "cell 1 2, growx"); //$NON-NLS-1$

		JButton btn_Create = new JButton(HG0417Msgs.Text_42);	// Step 3: select TMG project's PJC file for import
		btn_Create.setHorizontalAlignment(SwingConstants.LEFT);
		btn_Create.setEnabled(false);
		btn_Create.setToolTipText(HG0417Msgs.Text_43);			// Convert to the new HRE project database
		contents.add(btn_Create, "cell 1 3, growx"); 			//$NON-NLS-1$

		// Setup hidden panel to display switch controls for Import
		JPanel switchPanel= new JPanel();
		switchPanel.setVisible(false);
		switchPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.RAISED,
				new Color(255, 255, 255), new Color(160, 160, 160)), HG0417Msgs.Text_70,	// Controls for monitoring TMG Import
				TitledBorder.LEFT, TitledBorder.TOP, null, new Color(0, 0, 0)));
		contents.add(switchPanel, "cell 0 4 2, growx, hidemode 3"); 	//$NON-NLS-1$
		switchPanel.setLayout(new MigLayout("insets 10", "[]20[]20[]", "[]10[]10[]10[]"));		//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		trace = new JCheckBox(HG0417Msgs.Text_71);		// Console to file
		trace.setToolTipText(HG0417Msgs.Text_72);		// To write Java stack trace to an external file
		switchPanel.add(trace, "cell 0 0"); 	//$NON-NLS-1$
		tmgTrace = new JCheckBox(HG0417Msgs.Text_73);	// TMG Monitor
		tmgTrace.setToolTipText(HG0417Msgs.Text_74);	// To write TMG table load messages
		switchPanel.add(tmgTrace, "cell 1 0"); 	//$NON-NLS-1$
		hreTrace = new JCheckBox(HG0417Msgs.Text_75);	// HRE Monitor
		hreTrace.setToolTipText(HG0417Msgs.Text_76);	// To write HRE table load and index messages
		switchPanel.add(hreTrace, "cell 2 0"); 	//$NON-NLS-1$

		flagCheck = new JCheckBox(HG0417Msgs.Text_77);	// 	Flag Check
		flagCheck.setToolTipText(HG0417Msgs.Text_78);	// To write Flag import processing and errors
		switchPanel.add(flagCheck, "cell 0 1"); 	//$NON-NLS-1$
		exhCheck = new JCheckBox(HG0417Msgs.Text_79);	// Exhibit Check
		exhCheck.setToolTipText(HG0417Msgs.Text_80);	// To write statistics from exhibit processing
		switchPanel.add(exhCheck, "cell 1 1"); 	//$NON-NLS-1$
		debug = new JCheckBox(HG0417Msgs.Text_81);		// Import Debug
		debug.setToolTipText(HG0417Msgs.Text_82);  		// To write Debug data to System.out.println
		switchPanel.add(debug, "cell 2 1"); 	//$NON-NLS-1$

		JTextField consoleLog = new JTextField();
		switchPanel.add(consoleLog, "cell 0 2 3 1,growx"); 	//$NON-NLS-1$
		// If Windows OS, set a filepath for the console log
		String currentUsersHomeDir = System.getProperty("user.home");					//$NON-NLS-1$
		if (HGlobal.osType.contains("win")) consoleLog.setText(currentUsersHomeDir		//$NON-NLS-1$
					+ "\\HRE\\ConsoleB30Log.txt"); 										//$NON-NLS-1$
			else consoleLog.setText(HG0417Msgs.Text_83);		// Enter name of file to receive console log

		JButton btn_Cancel = new JButton(HG0417Msgs.Text_39);	// Cancel
		btn_Cancel.setToolTipText(HG0417Msgs.Text_40);			// Cancel the import process and return to the main menu
		contents.add(btn_Cancel, "cell 1 5, alignx right"); 	//$NON-NLS-1$

		// Make switchPanel visible if we're in DEBUG mode
		if (HGlobal.DEBUG) switchPanel.setVisible(true);

		pack();

		// Now ensure the button holding the new folder/file location does not expand past current size
		btn_Browse.setMaximumSize(new Dimension(btn_Browse.getWidth(), btn_Browse.getHeight()));

/*****************************
 * CREATE ACTION LISTENERS
 *****************************/
	// Clean out project prompt when Textfield gets focus
		txt_ProjectName.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent arg0) {
				txt_ProjectName.setText("");  //$NON-NLS-1$
				txt_ProjectName.setBackground(Color.YELLOW);}
			});

	// Listener for entry of new Project Name
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
            	newProjectName = txt_ProjectName.getText().trim();    // remove any leading/trailing blanks then cleanout silly characters
            	newProjectName = newProjectName.replaceAll("[\\/|\\\\|\\*|\\:|\\;|\\^|\\||\"|\'|\\<|\\>|\\{|\\}|\\?|\\%|,]",""); //$NON-NLS-1$ //$NON-NLS-2$
            	if (newProjectName.length() == 0 ) {
            		btn_Browse.setEnabled(false);
            		btn_Create.setEnabled(false);
            		txt_ProjectName.setBackground(Color.YELLOW);
            		}
            	else {btn_Browse.setEnabled(true);
            		  txt_ProjectName.setBackground(UIManager.getColor("TextField.selectionBackground"));  //$NON-NLS-1$
            		  txt_ProjectName.setForeground(UIManager.getColor("TextField.selectionForeground"));  //$NON-NLS-1$
            		  if (newFilenameSet) btn_Create.setEnabled(true);		// if File/Folder already setup, allow Create
            		 }
            	}
            };

     // Set up document listener
        txt_ProjectName.getDocument().addDocumentListener(docListen);

    // Listener for file browse button
		btn_Browse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				btn_Browse.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				HG0577FileChooser chooseFile = new HG0577FileChooser(pointToolHand.pointLibraryBusiness.
						setUpFileOpenChooser(HGlobal.pathHRElocation,HGlobal.defDatabaseEngine));

				chooseFile.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xy = btn_Browse.getLocationOnScreen();       // Gets chooser button location on screen
				chooseFile.setLocation(xy.x, xy.y);     		   // Sets screen top-left corner relative to that
				chooseFile.setVisible(true);
				btn_Browse.setCursor(Cursor.getDefaultCursor());
				// Process file and folder-names returned from file chooser (the 'chosen' fields)
				if (HGlobal.chosenFilename == "" ) {   }	       // do nothing if no filename setup //$NON-NLS-1$
					else {
						// Modify file name to match database engine specification
						HGlobal.chosenFilename = pointToolHand.pointLibraryBusiness.
							modifyFileNameChosen(HGlobal.chosenFilename, HGlobal.defDatabaseEngine);
						if (HGlobal.DEBUG)
							System.out.println("HG0417TMGProjectImport chosen Filename: " +  HGlobal.chosenFilename); //$NON-NLS-1$
						HGlobal.pathProjectFolder = HGlobal.chosenFolder;
						btn_Browse.setText(HGlobal.chosenFolder+File.separator+HGlobal.chosenFilename);  // show results
						newFilenameSet = true;								 // flag as setup correctly
						btn_Create.setEnabled(true);
					}
			}
		});

		// Listener for Create button
		btn_Create.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
			// List of 'Create' actions performed by importTmgToHreAction:
			//   copy Seed file to wherever the user has said he wants to put his project
			//	     (error possibility here if HRE cannot find the Seed file),
			//   rename copied Seed file to whatever filename the User chose,
			//   insert chosen Project Name into new file,
			//   update HGlobal.userProjects with this new ino,
			//   update UserAUX to ensure this new info is not lost.
                String[] newProjectArray = {
                		newProjectName,
                		HGlobal.chosenFilename,
                		HGlobal.chosenFolder,
                		HGlobal.thisComputer,
                		pointToolHand.currentTime("yyyy-MM-dd / HH:mm:ss"), //$NON-NLS-1$
                		pointToolHand.getDefaultDatabaseEngine(),
                		HG0417Msgs.Text_53};	// for LastBackup date
            // Perform TMG import process - dispose window if operation OK
                int errorCode = 0;
				try {
					errorCode = pointToolHand.importTmgToHreAction(newProjectArray,
							collectMonitorFlags(), consoleLog.getText());
				} catch (HBException hbe) {
					if (HGlobal.DEBUG)System.out.println("ERROR: importing TMG to HRE " + hbe.getMessage());	//$NON-NLS-1$
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: importing TMG to HRE " + hbe.getMessage());	//$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
					errorCode = 3;
				}
                if (errorCode == 0) {
                	userInfoImportTMGProject(errorCode, newProjectName);
                	if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: HRE Project "+newProjectName+" created by HG0417TMG import to HRE"); //$NON-NLS-1$ //$NON-NLS-2$
                	dispose();
                } else if (errorCode == 1) {
                	userInfoImportTMGProject(errorCode, newProjectName);
                } else if (errorCode == 2) {
                	userInfoImportTMGProject(errorCode, newProjectName);
                } else if (errorCode == 3) {
                	userInfoImportTMGProject(errorCode, newProjectName);
                }
			}
		});

/**
 * Cancel listener
 */
		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (HGlobal.showCancelmsg) {				// only show Cancel message if setting true
					if (JOptionPane.showConfirmDialog(btn_Cancel,
													  HG0417Msgs.Text_56, 	// Cancel TMG import process?
													  HG0417Msgs.Text_57,	// Project Create
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						    if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: cancelling HG0417TMGProjectImport"); //$NON-NLS-1$
							// close reminder display
							if (reminderDisplay != null) reminderDisplay.dispose();
						    dispose();
						} // yes option - return to main menu
				} else {
					if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: cancelling HG0417TMGProjectImport"); //$NON-NLS-1$
				// close reminder display
					if (reminderDisplay != null) reminderDisplay.dispose();
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
	}	// End HG0417 constructor

/**
 * GUI user messages for TMG import
 * @param errorCode
 * @param projectName
 */
	private void userInfoImportTMGProject(int errorCode, String projectName) {
		String errorMess = ""; 	//$NON-NLS-1$
		String errorTitle = HG0417Msgs.Text_61;	// Import TMG Project
		if (errorCode == 0) {
			errorMess = HG0417Msgs.Text_62 + newProjectName + HG0417Msgs.Text_63;	// Project ...... created for TMG import
			if (HGlobal.DEBUG)
				JOptionPane.showMessageDialog(contents, errorMess  + projectName, errorTitle,
											 JOptionPane.INFORMATION_MESSAGE);
			else System.out.println(" INFORMATION: " + errorTitle + " - " + errorMess);		//$NON-NLS-1$ //$NON-NLS-2$
			if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: " + errorTitle + " - " + errorMess);   //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (errorCode > 0) {
			if (errorCode == 1)  errorMess = HG0417Msgs.Text_64 + projectName;	// Project name in use
			if (errorCode == 2)  errorMess = HG0417Msgs.Text_65 + projectName;	// Failed, not able to set new project name:
			if (errorCode == 3)  errorMess = HG0417Msgs.Text_66 + projectName;	// New project creation failed:
			JOptionPane.showMessageDialog(contents, errorMess, errorTitle, JOptionPane.ERROR_MESSAGE);
			if (HGlobal.writeLogs) HB0711Logging.logWrite("ERROR: " + errorMess); //$NON-NLS-1$
		}
	}	// End userInfoOpenProject

/**
 * collectMonitorFlags()
 * @return boolean[]
 */
	private boolean[] collectMonitorFlags() {
		boolean[] monitorSetting = new boolean[6];
		if (trace.isSelected()) monitorSetting[0] = true; else monitorSetting[0] = false;
		if (tmgTrace.isSelected()) monitorSetting[1] = true; else monitorSetting[1] = false;
		if (hreTrace.isSelected()) monitorSetting[2] = true; else monitorSetting[2] = false;
		if (flagCheck.isSelected()) monitorSetting[3] = true; else monitorSetting[3] = false;
		if (exhCheck.isSelected()) monitorSetting[4] = true; else monitorSetting[4] = false;
		if (debug.isSelected()) monitorSetting[5] = true; else monitorSetting[5] = false;
		return monitorSetting;
	}

}	// End HG0417TMGProjectImport