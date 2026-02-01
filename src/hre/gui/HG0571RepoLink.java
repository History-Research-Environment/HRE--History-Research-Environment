package hre.gui;
/*******************************************************************************
 * Edit Repository Link table
 * v0.04.0032 2025-11-10 First draft (D Ferguson)
 *			  2025-11-11 Accept repo data from caller (D Ferguson)
 *		      2025-11-16 Updated handling of data and save ref. for reposlink (N. Tolleshaug)
 *			  2025-11-28 NLS all code (uses HG0570 NLS tables (D Ferguson)
 *			  2026-01-04 Log catch block errors (D Ferguson)
 ********************************************************************************/

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import hre.bila.HB0711Logging;
import hre.bila.HBCitationSourceHandler;
import hre.bila.HBException;
import hre.bila.HBProjectOpenData;
import hre.bila.HBRepositoryHandler;
import hre.nls.HG0570Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Edit Repository Link
 * @author D Ferguson
 * @version v0.04.0032
 * @since 2025-11-10
 */

public class HG0571RepoLink extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	HBCitationSourceHandler pointCitationSourceHandler;
	HBRepositoryHandler pointRepositoryHandler;

	public String screenID = "57100";	//$NON-NLS-1$
	long null_RPID  = 1999999999999999L;
	long proOffset  = 1000000000000000L;

	private JPanel contents;
	long sourceTablePID;
	boolean primaryLink;

	Object[] repoData = new Object[4]; // repo#, repo abbrev, repo ref, repo primary
    JTextField repoAbbrev, repoRef;
    DocumentListener abbrevTextChange;

    // For Text area font setting
    Font font = UIManager.getFont("TextArea.font");		//$NON-NLS-1$

/**
 * Create the dialog
 */
	public HG0571RepoLink(HBProjectOpenData pointOpenProject, long sourceTablePID, Object[] repoData)  {
		this.pointOpenProject = pointOpenProject;
		this.sourceTablePID = sourceTablePID;
		pointCitationSourceHandler = pointOpenProject.getCitationSourceHandler();
		pointRepositoryHandler = pointOpenProject.getRepositoryHandler();
		this.repoData = repoData;

	// Setup references
		windowID = screenID;
		helpName = "editrepository";	// (use same help) //$NON-NLS-1$

    	setResizable(false);
    	setTitle(HG0570Msgs.Text_22); // Edit Repository Link data

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0571RepoLink");	//$NON-NLS-1$

/************************************
 * Setup main panel and its contents
 ***********************************/
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]", "[]10[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    	JToolBar toolBar = new JToolBar();
    	toolBar.setFloatable(false);
    	toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
    	toolBar.add(Box.createHorizontalGlue());
    	// Add HG0450 icons
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north");	//$NON-NLS-1$

		JLabel reponum = new JLabel(HG0570Msgs.Text_23);	// ID:
		contents.add(reponum, "cell 0 0");	//$NON-NLS-1$
		if (repoData[0] == null) repoRef = new JTextField();
		else repoRef = new JTextField(String.valueOf((int)repoData[0]));
		repoRef.setHorizontalAlignment(JTextField.CENTER);
		repoRef.setColumns(3);
		repoRef.setEditable(false);
		contents.add(repoRef, "cell 0 0, alignx left, gap 10");	//$NON-NLS-1$

		JLabel name = new JLabel(HG0570Msgs.Text_24);	// Repository:
		contents.add(name, "cell 0 0, gapx 20");	//$NON-NLS-1$
		repoAbbrev = new JTextField((String)repoData[1]);
		repoAbbrev.setColumns(35);
		repoAbbrev.setEditable(false);
		contents.add(repoAbbrev, "cell 0 0, alignx left, gapx 10");	//$NON-NLS-1$

		JLabel ref = new JLabel(HG0570Msgs.Text_25);	// Reference:
		contents.add(ref, "cell 0 1");	//$NON-NLS-1$
		JTextField reference = new JTextField(((String)repoData[2]).trim());
		reference.setColumns(30);
		reference.setEditable(true);
		contents.add(reference, "cell 0 1, alignx left, gapx 10");	//$NON-NLS-1$

		JCheckBox primarySetting = new JCheckBox(HG0570Msgs.Text_26);	// Primary
		if ((boolean)repoData[3] ) primarySetting.setSelected(true);
		primarySetting.setHorizontalTextPosition(SwingConstants.LEADING);
		contents.add(primarySetting, "cell 0 1, alignx left, gapx 30");	//$NON-NLS-1$

		JButton btn_Save = new JButton(HG0570Msgs.Text_10);	// Save
		btn_Save.setToolTipText(HG0570Msgs.Text_21);	// Save the edited Repository Link data
		btn_Save.setEnabled(false);
		contents.add(btn_Save, "cell 0 2, align right, gapx 20, tag ok"); //$NON-NLS-1$

		JButton btn_Close = new JButton(HG0570Msgs.Text_12);	// Close
		btn_Close.setToolTipText(HG0570Msgs.Text_13);	// Close this screen
		contents.add(btn_Close, "cell 0 2, align right, gapx 20, tag cancel"); //$NON-NLS-1$

	// Display the screen
		pack();

/******************
 * ACTION LISTENERS
 ******************/
	// Listener for clicking 'X' on screen
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
	    		 btn_Close.doClick();
			} // return to main menu
		});


	// Listener for edit of reference text
		abbrevTextChange = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				btn_Save.setEnabled(true);
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				btn_Save.setEnabled(true);
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				btn_Save.setEnabled(true);
			}
		};

		reference.getDocument().addDocumentListener(abbrevTextChange);

	// Listener for primary setting
		primarySetting.addItemListener(new ItemListener() {
		    @Override
		    public void itemStateChanged(ItemEvent e) {
		        if(e.getStateChange() == ItemEvent.SELECTED) {//checkbox has been selected
		        	primaryLink = true; // set primary true
		        } else primaryLink = false; // set primary as false when deselected
		        btn_Save.setEnabled(true);
		    }
		});

	// Listener for Save button
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				primaryLink = primarySetting.isSelected();
				try {
					pointRepositoryHandler.updateRespositoryLink(sourceTablePID,
							(long) repoData[4], reference.getText().trim(), primaryLink);
					dispose();
				} catch (HBException hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0571 updating Repository link data " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: saved and leaving HG0571RepoLink");	//$NON-NLS-1$
			}
		});

	// Listener for Close button
		btn_Close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
			// Test for unsaved changes
			if (btn_Save.isEnabled()) {
				if (JOptionPane.showConfirmDialog(btn_Save,
						HG0570Msgs.Text_17		// There are unsaved changes. \n
						+ HG0570Msgs.Text_18,	// Do you still wish to exit this screen?
						HG0570Msgs.Text_20,		// Edit Repository Link
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: cancelling out of HG0571RepoLink"); //$NON-NLS-1$
						// Yes option: exit
							dispose();
						}
				} else {
					if (HGlobal.writeLogs) 	HB0711Logging.logWrite("Action: exiting HG0571RepolInk"); //$NON-NLS-1$
					dispose();
				}
			}
		});

	}	// End HG0571RepoLink constructor"

}  // End of HG0571RepoLink
