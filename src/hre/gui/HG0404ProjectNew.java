package hre.gui;
/*********************************************************************************************
 * Project New - Specification 04.04 GUI_ProjectNew 2020-03-02
 * v0.00.0005 2019-02-27 by R Thompson, updated by D Ferguson
 * v0.00.0008 2019-07-21 fix close cancel code (D Ferguson)
 * v0.00.0014 2019-11-18 changes for HB0711 static classes (D Ferguson)
 * v0.00.0015 2019-12-07 changed code for HG0514 call (D Ferguson)
 * v0.00.0016 2019-12-12 added pointer to ProjectHandler in constructor (N. Tolleshaug)
 * v0.00.0018 2019-02-2010 added call to newProjectAction in ProjectHandler
 * v0.00.0019 2020-03-02 fixed missing logging records (D. Ferguson)
*             2020-03-06 made server related buttons not visible (temporary change)
*  v0.00.0020 2020-03-18 fix errors in DocumentListener code (D. Ferguson)
*  v0.00.0021 2020-04-11 add new projects at bottom of Project menu (D Ferguson)
*  v0.00.0022 2020-05-13 add wait cursor around FileChooser (can take time) (D Ferguson)
*             2020-07-27 add screenID for Reminder, Help xref (D Ferguson)
 * v0.01.0023 2020-08-04 changed from JDialog to extend HG0450SuperDialog (D Ferguson)
 *            2020-09-11 removed Reminder icon from Toolbar (D Ferguson)
 *            2020-09-30 changed to MigLayout; fonts removed for JTattoo (D Ferguson) 
 * v0.01.0025 2020-11-24 use location Setting for default HRE file folder (D Ferguson)
 * 			  2021-04-11 NLS implemented (D Ferguson)
 * v0.01.0026 2021-05-01 Error 5 added for 'Copy to' file already existing (N Tolleshaug)
 * 			  2021-09-17 Apply tag codes to screen control buttons (D Ferguson)
 *			  2021-09-29 Remove server login button, etc as cannot create new remote DB (D Ferguson)
 * v0.03.0031 2024-08-15 Make suucessful create msg always visible, on top (D Ferguson)
 ********************************************************************************************/

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

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import hre.bila.HB0711Logging;
import hre.bila.HBProjectHandler;
import hre.nls.HG0404Msgs;

import net.miginfocom.swing.MigLayout;

/**
 * Project New
 * @author R Thompson
 * @version v0.01.0026
 * @since 2019-07-21
 */

public class HG0404ProjectNew extends HG0450SuperDialog {	
	private static final long serialVersionUID = 001L;
			
	private String screenID = "40400";     //$NON-NLS-1$
	private JPanel contents;
	private JTextField txt_ProjectName;
	private String newProjectName = ""; //$NON-NLS-1$
	private JButton btn_Create;
	
/**
* Create the Dialog.
*/
	public HG0404ProjectNew(HBProjectHandler pointProHand) {
		// Setup references for HG0450
		windowID = screenID;
		helpName = "newproject"; //$NON-NLS-1$
		
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0404ProjectNew"); //$NON-NLS-1$
		setResizable(false);
		setTitle(HG0404Msgs.Text_4);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		contents = new JPanel();
		contents.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]20[][]", "[]10[]10[]10[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);;
		toolBar.add(Box.createHorizontalGlue());
		// Add icons defined in HG0450
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north");  //$NON-NLS-1$
		
		JLabel lbl_ProjectName = new JLabel(HG0404Msgs.Text_9);
		contents.add(lbl_ProjectName, "cell 0 0");  //$NON-NLS-1$
		
		txt_ProjectName = new JTextField();
		txt_ProjectName.setToolTipText(HG0404Msgs.Text_11);
		txt_ProjectName.setText(HG0404Msgs.Text_12);
		txt_ProjectName.setColumns(30);
		contents.add(txt_ProjectName, "cell 1 0"); //$NON-NLS-1$
		
		JLabel lbl_Location = new JLabel(HG0404Msgs.Text_24);
		contents.add(lbl_Location, "cell 0 1, alignx right"); //$NON-NLS-1$
		
		JButton btn_Browse = new JButton(HG0404Msgs.Text_26);
		btn_Browse.setToolTipText(HG0404Msgs.Text_27);
		btn_Browse.setEnabled(false);
		contents.add(btn_Browse, "cell 1 1, alignx left"); //$NON-NLS-1$				
		
		JLabel lblFolder = new JLabel(HG0404Msgs.Text_29);
		contents.add(lblFolder, "cell 0 2, alignx right"); //$NON-NLS-1$
		JLabel lbl_FolderNameData = new JLabel();
		lbl_FolderNameData.setMaximumSize(new Dimension(255, 23));
		contents.add(lbl_FolderNameData, "cell 1 2"); //$NON-NLS-1$
		
		JLabel lbl_Filename = new JLabel(HG0404Msgs.Text_32);
		contents.add(lbl_Filename, "cell 0 3, alignx right"); //$NON-NLS-1$
		JLabel lbl_FilenameData = new JLabel();
		lbl_FilenameData.setMaximumSize(new Dimension(255, 23));
		contents.add(lbl_FilenameData, "cell 1 3"); //$NON-NLS-1$
		
		JButton btn_Cancel = new JButton(HG0404Msgs.Text_38);
		btn_Cancel.setToolTipText(HG0404Msgs.Text_39);
		contents.add(btn_Cancel, "cell 1 4, align right, gapx 20, tag cancel"); //$NON-NLS-1$
		
		btn_Create = new JButton(HG0404Msgs.Text_35);
		btn_Create.setEnabled(false);
		btn_Create.setToolTipText(HG0404Msgs.Text_36);
		contents.add(btn_Create, "cell 1 4, align right, gapx 20, tag ok"); //$NON-NLS-1$
		
		pack();
		
/***
 * CREATE ACTION LISTENERS 
 **/
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
            	else {
            		  txt_ProjectName.setForeground(UIManager.getColor("TextField.selectionForeground")); //$NON-NLS-1$
            		  txt_ProjectName.setBackground(UIManager.getColor("TextField.selectionBackground")); //$NON-NLS-1$
            		  btn_Browse.setEnabled(true); 
            	}   
            } };
        txt_ProjectName.getDocument().addDocumentListener(docListen);
       
/**
 * Implementation of action Listeners
 */		
		btn_Browse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				btn_Browse.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				HG0577FileChooser chooseFile = new HG0577FileChooser(pointProHand.pointLibraryBusiness.
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
						HGlobal.chosenFilename = pointProHand.pointLibraryBusiness.
							modifyFileNameChosen(HGlobal.chosenFilename, HGlobal.defDatabaseEngine);
						if (HGlobal.DEBUG) System.out.println("HG0404ProjectNew - ch Filename: " +  HGlobal.chosenFilename); //$NON-NLS-1$
						HGlobal.pathProjectFolder = HGlobal.chosenFolder;
						lbl_FilenameData.setText(HGlobal.chosenFilename);    // set filename
						lbl_FolderNameData.setText(HGlobal.chosenFolder);    // set path
						btn_Create.setEnabled(true); 
					}
			}
		});	
		
		btn_Create.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {	
			// List of 'Create' actions performed here -
			//   Copy Seed file to wherever the user has said he wants to put his project 
			//	(error possibility here if HRE cannot find the Seed file)
			//   Rename copied Seed file to whatever filename the User chose
			//   Insert chosen Project Name into new file 
			//   Add entry for this project to Known Projects table (make 'last used' field = now)
			//   Write out UserAUX to ensure this new info is not lost
                String[] newProjectArray = {
                		newProjectName,
                		HGlobal.chosenFilename,
                		HGlobal.chosenFolder,
                		HGlobal.thisComputer,
                		pointProHand.currentTime("yyyy-MM-dd / HH:mm:ss"), //$NON-NLS-1$
                		pointProHand.getDefaultDatabaseEngine(),
                		HG0404Msgs.Text_50};	// for LastBackup date
                
                // If no error, issue Succesful msg and dispose
                int errorCode = pointProHand.newProjectAction(newProjectArray);
                if (errorCode == 0) {
                	if (HGlobal.showCancelmsg) userInfoNewProject(errorCode, newProjectName);
                	
                	// add name to Project menu project list
                	pointProHand.mainFrame.resetProjMenuItem(newProjectName.trim());
                	if (HGlobal.writeLogs) 
                		HB0711Logging.logWrite("Action: new Project "+newProjectName+" created by HG0404ProjectNew"); //$NON-NLS-1$ //$NON-NLS-2$
                	dispose();
                // else just issue error msg
                } else if (errorCode > 0) 
                	userInfoNewProject(errorCode, newProjectName);
			}
		});	
		
		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (HGlobal.showCancelmsg)		// only show Cancel message if setting true
					{if (JOptionPane.showConfirmDialog(btn_Cancel, HG0404Msgs.Text_53, HG0404Msgs.Text_54,
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						    if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: cancelling HG0404ProjectNew"); //$NON-NLS-1$
							dispose();	}	// yes option - return to main menu
					}
				else {if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: cancelling HG0404 Project New"); //$NON-NLS-1$
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

	}	// End HG0404ProjectNew constructor
	
/**
 * GUI user translated messages
 * @param errorCode
 * @param projectName
 */
	private void userInfoNewProject(int errorCode, String projectName) {	
		String errorMess = " "; //$NON-NLS-1$
		if (errorCode == 0) { 
			// Setup as a dialog to enable always on top of the 'Add Person' screen that is created
			JOptionPane optionPane = new JOptionPane(HG0404Msgs.Text_60 + projectName);  // message
			JDialog dialog = optionPane.createDialog(HG0404Msgs.Text_61);		// title
			Point pt = new Point(btn_Create.getLocation()); 	// location for msg
			SwingUtilities.convertPointToScreen(pt, btn_Create);
			dialog.setLocation(pt);
			dialog.setAlwaysOnTop(true);
			dialog.setVisible(true);
		}		
		if (errorCode > 0) { 
			if (errorCode == 1)  errorMess = HG0404Msgs.Text_62 + projectName + HG0404Msgs.Text_63;
			if (errorCode == 2)  errorMess = HG0404Msgs.Text_64 + projectName + HG0404Msgs.Text_65;
			if (errorCode == 3)  errorMess = HG0404Msgs.Text_66 + projectName + HG0404Msgs.Text_67;
			if (errorCode == 4)  errorMess = HG0404Msgs.Text_68;
			if (errorCode == 5)  errorMess = HG0404Msgs.Text_70;
			JOptionPane.showMessageDialog(null, errorMess, HG0404Msgs.Text_69, JOptionPane.ERROR_MESSAGE);
		}
	}	// End userInfoNewProject
	
}	// End HG0404ProjectNew
