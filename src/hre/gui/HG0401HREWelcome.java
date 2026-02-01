package hre.gui;
/************************************************************************************
 * HRE Welcome screen - Specification 04.01 GUI_UserInterface 2020-03
 * v0.00.0007 2019-01-31 by D Ferguson
 * v0.00.0020 2020-03-17 converted Splash to Welcome screen (D Ferguson)
 *            2020-03-22 re-implemented NLS
 * v0.00.0022 2020-06-28 standardise file separators in filepaths (D Ferguson)
 * v0.01.0023 2020-09-30 changed to MigLayout; fonts removed for JTattoo install (D Ferguson)
 * v0.01.0025 2021-02-05 revised NLS target property names (D Ferguson)
 * 			  2021-02-17 fix missing File.separator and adjust screen layout (D Ferguson)
 * v0.01.0026 2021-05-16 make screen into 4 parts with project folder choice (D Ferguson)
 * v0.03.0030 2023-07-14 switch to generic logo name (D Ferguson)
 * 			  2023-09-18 resize logo to shrink screen depth (D Ferguson)
 * v0.03.0031 2024-10-01 organize imports (D Ferguson)
 * v0.04.0032 2026-01-02 Logged catch block actions (D Ferguson)
 ************************************************************************************/

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import hre.bila.HB0711Logging;
import hre.bila.HB0744UserAUX;
import hre.nls.HG0401WMsgs;
import net.miginfocom.swing.MigLayout;

/**
 * HRE Welcome screen
 * @author D Ferguson
 * @version v0.03.0032
 * @since 2019-01-31
 */

public class HG0401HREWelcome extends JDialog {

	private static final long serialVersionUID = 001L;

	private JTextField txtNameEntry;
	private JTextField txtEmailEntry;
	private boolean nameValid = false;
	private boolean emailValid = false;
	private boolean notNewUser = false;
	private boolean clearedNameOnce = false;
	private boolean removedNameOnce = false;
	private boolean clearedEmailOnce = false;
	private boolean removedEmailOnce = false;
	// Set start folder for Filechooser for HRE projects
	String startFolder = System.getProperty("user.home") + File.separator + "hre" + File.separator;	//$NON-NLS-1$ //$NON-NLS-2$

	public HG0401HREWelcome() {
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setTitle(HG0401WMsgs.Text_0);
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/hre/images/HRE-32.png")));  //$NON-NLS-1$
		JPanel contents = new JPanel();
		contents.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[250!]25[270!]", "[]10![]10![]10![]10[]10[]10[]10[]10[]20[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	// Fixed Section 0 contents
		JLabel lblNewLabel = new JLabel("");  //$NON-NLS-1$
		// re-scale logo to 3/4 size (width, height) to not take too much space
		lblNewLabel.setIcon(new ImageIcon(new ImageIcon(getClass().getResource("/hre/images/HRE-logo.png")).getImage().getScaledInstance(136, 156, Image.SCALE_SMOOTH)));
		contents.add(lblNewLabel, "cell 0 0 1 3, top, center"); //$NON-NLS-1$

		JTextArea txtHREname = new JTextArea();
		txtHREname.setOpaque(false);
		txtHREname.setEditable(false);
		txtHREname.setFont(new Font("Arial", Font.BOLD, 20)); //$NON-NLS-1$
		txtHREname.setText(HG0401WMsgs.Text_4);
		contents.add(txtHREname, "cell 1 0, center"); //$NON-NLS-1$

		JTextArea txtVer = new JTextArea();
		txtVer.setOpaque(false);
		txtVer.setEditable(false);
		txtVer.setFont(new Font("Arial", Font.PLAIN, 15)); //$NON-NLS-1$
		txtVer.setText((HG0401WMsgs.Text_5) + HGlobal.buildNo);
		contents.add(txtVer, "cell 1 1, center"); //$NON-NLS-1$

		JTextArea txtDate = new JTextArea();
		txtDate.setOpaque(false);
		txtDate.setEditable(false);
		txtDate.setFont(new Font("Arial", Font.PLAIN, 15)); //$NON-NLS-1$
		txtDate.setText((HG0401WMsgs.Text_6) + HGlobal.releaseDate);
		contents.add(txtDate, "cell 1 2, center"); //$NON-NLS-1$

	// Variable section 1 contents
		JTextArea txtNewUser = new JTextArea();
		txtNewUser.setOpaque(false);
		txtNewUser.setWrapStyleWord(true);
		txtNewUser.setLineWrap(true);
		txtNewUser.setText(HG0401WMsgs.Text_9);
		txtNewUser.setEditable(false);
		contents.add(txtNewUser, "cell 0 3 2, growx, growy, hidemode 2"); //$NON-NLS-1$

		txtNameEntry = new JTextField();
		txtNameEntry.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		txtNameEntry.setToolTipText(HG0401WMsgs.Text_2);
		txtNameEntry.setText(HG0401WMsgs.Text_11);
		txtNameEntry.setColumns(41);
		contents.add(txtNameEntry, "cell 0 4 2, hidemode 2"); //$NON-NLS-1$

		txtEmailEntry = new JTextField();
		txtEmailEntry.setText(HG0401WMsgs.Text_12);
		txtEmailEntry.setToolTipText(HG0401WMsgs.Text_1);
		txtEmailEntry.setColumns(41);
		contents.add(txtEmailEntry, "cell 0 5 2, hidemode 2");	//$NON-NLS-1$

	// Variable Section 2 contents
		JTextArea txtProjFolder = new JTextArea();
		txtProjFolder.setOpaque(false);
		txtProjFolder.setWrapStyleWord(true);
		txtProjFolder.setLineWrap(true);
		txtProjFolder.setText(HG0401WMsgs.Text_25 + HG0401WMsgs.Text_26);
		txtProjFolder.setEditable(false);
		contents.add(txtProjFolder, "cell 0 6 2, growx, growy, hidemode 2"); //$NON-NLS-1$
		txtProjFolder.setVisible(false);

		JFileChooser chooseFolder = new JFileChooser(startFolder);
		// Setup FileChooser in minimal (fast start) mode
        chooseFolder.putClientProperty("FileChooser.useShellFolder", Boolean.FALSE);  //$NON-NLS-1$
        chooseFolder.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooseFolder.setApproveButtonText(HG0401WMsgs.Text_27);	 // Select
		contents.add(chooseFolder, "cell 0 7 2, hidemode 2");	//$NON-NLS-1$
		chooseFolder.setVisible(false);

	// Variable section 3 contents
		JEditorPane infoPane = new JEditorPane();
		infoPane.setForeground(Color.BLACK);
		infoPane.setBackground(Color.WHITE);
		infoPane.setEditable(false);
		contents.add(infoPane, "cell 0 8 2, growx, growy, hidemode 2"); //$NON-NLS-1$
		// pickup Welcome text from html file in Help folder
	    String file = HGlobal.helpPath + HGlobal.nativeLanguage + File.separator + "Welcome.html";    //$NON-NLS-1$
		try {
			 infoPane.setPage(file);
	      } catch (IOException e) {
	    	  infoPane.setContentType("text/html"); //$NON-NLS-1$
	    	  infoPane.setText("<html>Page Welcome.html not found.</html>"); //$NON-NLS-1$
				if (HGlobal.writeLogs)
					HB0711Logging.logWrite("ERROR: in HG0401W Welcome text not found " + e.getMessage()); //$NON-NLS-1$
	      }
		infoPane.setVisible(false);

	// Fixed section 4 contents
		JCheckBox chkbxDoNotShow = new JCheckBox(HG0401WMsgs.Text_8);
		contents.add(chkbxDoNotShow, "cell 0 9, hidemode 3"); //$NON-NLS-1$
		chkbxDoNotShow.setVisible(false);

		JButton btnSave = new JButton(HG0401WMsgs.Text_7);
		btnSave.setEnabled(false);
		contents.add(btnSave, "cell 1 9, alignx right"); //$NON-NLS-1$

	// Setup screen layout if NOT New User (Section 0+3+4 only)
	// if userCred has been setup, this is not a new user, so remove the New User prompts
	// Otherwise screen will start with Section 1 showing
		if (HGlobal.userCred[1] != "" & HGlobal.userCred[3] != "") { //$NON-NLS-1$ //$NON-NLS-2$
			// Hide section 1
			txtNewUser.setVisible(false);
			txtNameEntry.setVisible(false);
			txtEmailEntry.setVisible(false);
			// Hide section 2
			txtProjFolder.setVisible(false);
			chooseFolder.setVisible(false);
			// Show section 3
			infoPane.setVisible(true);
			// Tailor section 4
			chkbxDoNotShow.setVisible(true);
			nameValid = true;
			emailValid = true;
			notNewUser = true;
			btnSave.setText(HG0401WMsgs.Text_28);  // Close
			btnSave.setEnabled(true);
		}

		pack();

/*******************
 * ACTION LISTENERS
 ******************/
		// Listener for 'do not show again' CheckBox
		chkbxDoNotShow.addActionListener(new ActionListener() {
		      @Override
			public void actionPerformed(ActionEvent e) {
		        if (chkbxDoNotShow.isSelected()) HGlobal.showWelcome = false;
		        	else HGlobal.showWelcome = true;
	       }
		});

		// Clean out name prompt when it gets focus, but only first time
		txtNameEntry.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent arg0) {
				if (clearedNameOnce)  {    } 					// do nothing if we've been here before
					else {clearedNameOnce = true;
						  txtNameEntry.setText(""); //$NON-NLS-1$
						  txtNameEntry.setBackground(Color.YELLOW);
						 }
				}
		});

		// Listener for entry of user Name
		DocumentListener nameListen = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
            	updateNameState();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
            	if (removedNameOnce)  updateNameState();
					else removedNameOnce = true;
            	}
            @Override
            public void changedUpdate(DocumentEvent e) {
            }
            protected void updateNameState() {
            	HGlobal.userCred[1] = txtNameEntry.getText().trim();
            	if (((String) HGlobal.userCred[1]).length() == 0 ) {
            			nameValid = false;
            			txtNameEntry.setBackground(Color.YELLOW);
            			}
            		else {nameValid = true;				// might be valid - subject to final test at Close
            			  txtNameEntry.setForeground(UIManager.getColor("TextField.selectionForeground")); //$NON-NLS-1$
            			  txtNameEntry.setBackground(UIManager.getColor("TextField.selectionBackground")); //$NON-NLS-1$
            			 }
            	if (nameValid & emailValid) btnSave.setEnabled(true);
            	}
		};
        txtNameEntry.getDocument().addDocumentListener(nameListen);

		// Clean out email prompt when it gets focus, but only first time
		txtEmailEntry.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent arg0) {
				if (clearedEmailOnce)  {    } 					// do nothing if we've been here before
					else   {clearedEmailOnce = true;
							txtEmailEntry.setText(""); 	 //$NON-NLS-1$
							txtEmailEntry.setBackground(Color.YELLOW);
							}
				}
		});

		// Listener for entry of email address
		DocumentListener emailListen = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateEmailState();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
				if (removedEmailOnce)  {updateEmailState();}
					else removedEmailOnce = true;
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
            }
            protected void updateEmailState() {
            	HGlobal.userCred[3] = txtEmailEntry.getText().trim();
            	if (((String) HGlobal.userCred[3]).length() == 0 ) {
            			emailValid = false;
            			txtEmailEntry.setBackground(Color.YELLOW);
            			}
            		else {emailValid = true;				// might be valid - subject to final test at Close
            			  txtEmailEntry.setForeground(UIManager.getColor("TextField.selectionForeground")); //$NON-NLS-1$
            			  txtEmailEntry.setBackground(UIManager.getColor("TextField.selectionBackground")); //$NON-NLS-1$
            			 }
            	if (nameValid & emailValid) btnSave.setEnabled(true);
            	}
		};
        txtEmailEntry.getDocument().addDocumentListener(emailListen);

		// FileChooser Listener - find a folder
		chooseFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent fc) {
				if (fc.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {
				// Use the selected folder as the global setting for HRE projects
						String projectFolder = chooseFolder.getSelectedFile().getPath();
						if (!projectFolder.endsWith(File.separator)) projectFolder = projectFolder + File.separator;
		                HGlobal.pathHRElocation = projectFolder;
		        // Now copy the HRE Sample database to this location
		        // Note: cannot use a bila routine as HB layer may not be set up yet
		                try {
		                Files.copy(Paths.get(HGlobal.sampleProjectFile),
		                		   Paths.get(HGlobal.pathHRElocation + "HRE Sample database.mv.db"), StandardCopyOption.REPLACE_EXISTING); //$NON-NLS-1$
		                } catch (NoSuchFileException nsfe) {
		    				if (HGlobal.writeLogs)
		    					HB0711Logging.logWrite("ERROR: in HG0401W Sample database not found " + nsfe.getMessage()); //$NON-NLS-1$
		    	        	JOptionPane.showMessageDialog(null, HG0401WMsgs.Text_29
									+  nsfe.getMessage(), HG0401WMsgs.Text_30, JOptionPane.ERROR_MESSAGE);
		    	        } catch (IOException ioe) {
		    				if (HGlobal.writeLogs)
		    					HB0711Logging.logWrite("ERROR: in HG0401W Sample database IO error " + ioe.getMessage()); //$NON-NLS-1$
		    	        	JOptionPane.showMessageDialog(null, HG0401WMsgs.Text_31
									+  ioe.getMessage(), HG0401WMsgs.Text_30, JOptionPane.ERROR_MESSAGE);
		    			}
		        // Now create an entry for the Sample file in UserProjects
			    		String[] sample = {"HRE Sample project", 		// project		//$NON-NLS-1$
			    							"HRE Sample database", 		// file			//$NON-NLS-1$
			    							HGlobal.pathHRElocation,	// folder
			    							HGlobal.thisComputer, 		// server
			    							"2026-01-01 / 12:00:00",	// lastclosed	//$NON-NLS-1$
			    							HGlobal.defDatabaseEngine,	// DB type
			    							HG0401WMsgs.Text_32};		// last backup (unknown)
			    		HGlobal.userProjects.add(sample);
			    // and update the UserAUX file
			    		HB0744UserAUX.writeUserAUXfile();
		        // Now hide screen Section 2
	  					txtProjFolder.setVisible(false);
    					chooseFolder.setVisible(false);
		        // Display screen Section 3 and revise Section 4
	   					chkbxDoNotShow.setVisible(true);
    					infoPane.setVisible(true);
            			notNewUser = true;
            			btnSave.setText(HG0401WMsgs.Text_28);  // Close
            			btnSave.setVisible(true);
 //           			pack();
		            }
				if (fc.getActionCommand().equals(JFileChooser.CANCEL_SELECTION)) {
		               // do nothing
		            }
			}
		});

		// LISTENER FOR CLICKING 'X" ON SCREEN
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
	        if (nameValid & emailValid) btnSave.doClick();
		    }
		});

		// Save or Close button - exit if notNewUser state
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (notNewUser) dispose();
				nameValid = false;				// assume the worst first
				emailValid = false;
            	if (!HGlobalCode.isNameValid((String) HGlobal.userCred[1])) {
            		txtNameEntry.setBackground(Color.YELLOW);
            		txtNameEntry.setForeground(Color.BLACK);
            		JOptionPane.showMessageDialog(txtNameEntry , HG0401WMsgs.Text_21,
					HG0401WMsgs.Text_22, JOptionPane.ERROR_MESSAGE);
            		}
					else {nameValid = true;
						  txtNameEntry.setBackground(Color.WHITE);
						  txtNameEntry.setForeground(Color.BLACK);
						  }

            	if (!HGlobalCode.isEmailValid((String) HGlobal.userCred[3])) {
            		txtEmailEntry.setBackground(Color.YELLOW);
            		txtEmailEntry.setForeground(Color.BLACK);
            		JOptionPane.showMessageDialog(txtEmailEntry , HG0401WMsgs.Text_23,
					HG0401WMsgs.Text_24, JOptionPane.ERROR_MESSAGE);
            		}
					else {emailValid = true;
						  txtEmailEntry.setBackground(Color.WHITE);
						  txtEmailEntry.setForeground(Color.BLACK);
						  }
            	if (emailValid & nameValid) {
            		// Save new user values
            			HB0744UserAUX.writeUserAUXfile();
            		// Section 1 completed so hide its components
            			txtNewUser.setVisible(false);
    					txtNameEntry.setVisible(false);
    					txtEmailEntry.setVisible(false);
            		// Reset screen to show Section 2
    					txtProjFolder.setVisible(true);
    					chooseFolder.setVisible(true);
            		// Reset screen display
    					btnSave.setVisible(false);
            			pack();
            			}
			}
		});

	}	// End HG0401HREWelcome constructor

}	// End HG0401HREWelcome
