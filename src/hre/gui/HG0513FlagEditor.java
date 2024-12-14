package hre.gui;
/*******************************************************************************
 * Flag Editor (New or Existing)
 * v0.03.0030 2023-06-07 First draft (D Ferguson)
 *			  2023-07-28 Add update/save code (N. Tolleshaug)
 *			  2023-08-01 Set update marker in HG0512FlagManager (N. Tolleshaug)
 *			  2023-08-05 Set new validity checks on flag values (D Ferguson)
 *			  2023-10-11 Convert to NLS (D Ferguson)
 * v0.03.0031 2024-10-01 Clean whitespaces (D Ferguson)
 *			  2024-12-01 Replace JoptionPane 'null' locations with 'contents' (D Ferguson)
 *******************************************************************************/

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultCaret;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.nls.HG0513Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Edit or Add Flag
 * @author D Ferguson
 * @version v0.03.0031
 * @since 2023-06-07
 */

public class HG0513FlagEditor extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	private JPanel contents;
	JButton btn_Save;
	Object objFlagDataToEdit[];
	boolean madeChanges = false;
	boolean addFlag = false; 		// Enables test if this is an Add Flag case
	String[] oldValuesList;		 	// Enable save of the original Flag Values list
	char question = '?'; // These 2 fields for handling ?->Unknown flag change
	String unknown = HG0513Msgs.Text_0;		// Unknown

	HG0512FlagManager pointFlagManager;

/**
 * Create the dialog
 * @throws HBException
 */
	public HG0513FlagEditor(HG0512FlagManager pointFlagManager, boolean addFlag, Object objFlagDataToEdit[]) {
		this.pointFlagManager = pointFlagManager;
		this.objFlagDataToEdit = objFlagDataToEdit;
		this.addFlag = addFlag;

    // Setup screenID and helpName for 51200 as Help for this dialog is part of
    // FlagManager Help and F1 has to select that (also this screen does not save to T302)
    	String screenID = "51200";		//$NON-NLS-1$
    	windowID = screenID;
		helpName = "flagmanager";	//$NON-NLS-1$

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: entering HG0513FlagEditor");}	//$NON-NLS-1$
	    setTitle(HG0513Msgs.Text_1);		// Edit Flag Detail

/***********************************
 * Setup main panel and its contents
 ***********************************/
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]10[]", "[]10[]10[]10[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_Name = new JLabel(HG0513Msgs.Text_2);		// Flag Name
		contents.add(lbl_Name, "cell 0 0, alignx right");	//$NON-NLS-1$

		JTextField textFlagName = new JTextField();
		textFlagName.setColumns(30);
		contents.add(textFlagName, "cell 1 0");	//$NON-NLS-1$

		JLabel lbl_Lang = new JLabel(HG0513Msgs.Text_3);		// Flag Language
		contents.add(lbl_Lang, "cell 0 1, alignx right");	//$NON-NLS-1$

		JLabel lbl_Language = new JLabel();
		contents.add(lbl_Language, "cell 1 1, alignx left");	//$NON-NLS-1$

		JLabel lbl_Active = new JLabel(HG0513Msgs.Text_4);		// Active Flag?
		contents.add(lbl_Active, "cell 0 2, alignx right");	//$NON-NLS-1$

		JCheckBox chkbox_Active = new JCheckBox();
		chkbox_Active.setToolTipText(HG0513Msgs.Text_5);	// On if a Flag is in use
		contents.add(chkbox_Active, "cell 1 2");	//$NON-NLS-1$

		JLabel lbl_System = new JLabel(HG0513Msgs.Text_6);		// SystemFlag?
		contents.add(lbl_System, "cell 1 2, gapx 30");	//$NON-NLS-1$

		JCheckBox chkbox_System = new JCheckBox();
		chkbox_System.setEnabled(false);
		chkbox_System.setToolTipText(HG0513Msgs.Text_7);	// On if a Flag is System defined
		contents.add(chkbox_System, "cell 1 2, gapx 10");	//$NON-NLS-1$

		JLabel lbl_Values = new JLabel(HG0513Msgs.Text_8);	// 	All Flag Values
		contents.add(lbl_Values, "cell 0 3, alignx right");	//$NON-NLS-1$

		JTextField textFlagValues = new JTextField();
		textFlagValues.setColumns(50);
		contents.add(textFlagValues, "cell 1 3");	//$NON-NLS-1$

		JLabel lbl_Desc = new JLabel(HG0513Msgs.Text_9);	// Flag Description
		contents.add(lbl_Desc, "cell 0 4, alignx right");	//$NON-NLS-1$

		JTextArea textFlagDesc = new JTextArea(10,50);
		textFlagDesc.setLineWrap(true);
		textFlagDesc.setWrapStyleWord(true);
		((DefaultCaret)textFlagDesc.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	    Font font = UIManager.getFont("TextArea.font");	//$NON-NLS-1$
	    textFlagDesc.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
	    textFlagDesc.setBorder(new JTextField().getBorder());
		contents.add(textFlagDesc, "cell 1 4, growx");	//$NON-NLS-1$

		JButton btn_Cancel = new JButton(HG0513Msgs.Text_10);	// Cancel
		btn_Cancel.setEnabled(true);
		contents.add(btn_Cancel, "cell 1 5, align right, gapx 20, tag cancel");	//$NON-NLS-1$

		btn_Save = new JButton(HG0513Msgs.Text_11);		// Save
		btn_Save.setEnabled(false);
		contents.add(btn_Save, "cell 1 5, align right, gapx 20, tag ok");	//$NON-NLS-1$

	// Now load flagDataToEdit data into the above fields
	// flagDataToEdit entries are: 0 Flag name; 1 System?; 2 Lang Code; 3 Active?
	//							   4 Flag values; 5 Flag description; 6 Flag ID
		textFlagName.setText((String) objFlagDataToEdit[0]);
		chkbox_System.setSelected((boolean) objFlagDataToEdit[1]);

	// Initialize the Language, checkbox, Values, Desc displayed
		lbl_Language.setText((String) objFlagDataToEdit[2]);

		chkbox_Active.setSelected((boolean) objFlagDataToEdit[3]);
		textFlagValues.setText((String) objFlagDataToEdit[4]);
		textFlagDesc.setText((String) objFlagDataToEdit[5]);

	// Check if this is Add a Flag case
	// if not, save the original Flag values
		if (!addFlag)
			oldValuesList = ((String) objFlagDataToEdit[4]).split("\\,");		//$NON-NLS-1$

	// Show screen
        pack();

/*******************
 * ACTION LISTENERS
 ******************/
		// Listener for clicking 'X on screen - make same as Cancel button
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
		    	btn_Cancel.doClick();
		    }
		});

		// Listener for Cancel button
		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// If NOT in Save mode, just Close the screen
				if (!btn_Save.isEnabled() ) {
					if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0513FlagEditor");	//$NON-NLS-1$
					// Set cancel marker so HG0512 does not save anything
					pointFlagManager.setCancelMarker(true);
					dispose();
				}
				// Else confirm exit without Save
				else if (JOptionPane.showConfirmDialog(btn_Cancel, HG0513Msgs.Text_12			// There are unsaved changes. \n
																  + HG0513Msgs.Text_13,	// Do you wish to exit without Saving?
																   HG0513Msgs.Text_14,							// Flag Editor
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					    if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0513FlagEditor");	//$NON-NLS-1$
					    // Set cancel marker so HG0512 does not save anything
					    pointFlagManager.setCancelMarker(true);
						dispose();	}	// yes option - exit
					}

		});

		// Listener for Save button
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// Save values back into objFlagDataToEdit object to capture all changes
				// (this excludes Language, System? & flagID that cannot be changed)
				try {
	         		objFlagDataToEdit[0] = textFlagName.getText();
	           		objFlagDataToEdit[3] = chkbox_Active.isSelected();
	           		objFlagDataToEdit[4] = textFlagValues.getText();
	           		objFlagDataToEdit[5] = textFlagDesc.getText();
	           		// and return to HG0512 for it to reload its flagData and table model
					pointFlagManager.resetFlagDescription(false);
					dispose();
				} catch (HBException hbe) {
					btn_Save.setEnabled(false);
					if(HGlobal.DEBUG) System.out.println("Reset Flag Error: " + hbe.getMessage());	//$NON-NLS-1$
       				JOptionPane.showMessageDialog(contents, HG0513Msgs.Text_15 + hbe.getMessage(),	// Error:
       											HG0513Msgs.Text_14, JOptionPane.ERROR_MESSAGE);		// Flag Editor
				}
			}
		});

		// General Listener to turn on btn_Save for edit of any text field
		DocumentListener textListen = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {updateFieldState();}
            @Override
            public void removeUpdate(DocumentEvent e) {updateFieldState();}
            @Override
            public void changedUpdate(DocumentEvent e) {updateFieldState();}
            protected void updateFieldState() {
            	// Save button only valid if both FlagName and FlagValue are non-empty and valid;
            	// so first, turn it off in case the validity checks fail.
            	btn_Save.setEnabled(false);
            	if(textFlagName.getText().length() > 2 && textFlagValues.getText().length() > 2) {
            		try {
            			// Apply all validity checks
						if (checkFlagValueChanges(textFlagValues.getText()))
							btn_Save.setEnabled(true);
					} catch (HBException hbe) {
	       				JOptionPane.showMessageDialog(btn_Save,	HG0513Msgs.Text_15 + hbe.getMessage(),	// Error:
   													HG0513Msgs.Text_14, JOptionPane.ERROR_MESSAGE);	// Flag Editor
					}
            	}
            }
        };
        // Link to the text fields we listen to for changes
        textFlagName.getDocument().addDocumentListener(textListen);
        textFlagValues.getDocument().addDocumentListener(textListen);
        textFlagDesc.getDocument().addDocumentListener(textListen);

        // Listener for change in checkboxActive
        chkbox_Active.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
            	// Save button only valid if both FlagName and FlagValue are non-empty and valid;
            	// so first, turn it off in case the validity checks fail.
            	btn_Save.setEnabled(false);
            	if(textFlagName.getText().length() > 3 && textFlagValues.getText().length() > 2) {
            		try {
            			// Apply all validity checks
						if (checkFlagValueChanges(textFlagValues.getText()))
							btn_Save.setEnabled(true);
					} catch (HBException hbe) {
	       				JOptionPane.showMessageDialog(btn_Save,	HG0513Msgs.Text_15 + hbe.getMessage(),	// Error:
   													HG0513Msgs.Text_14, JOptionPane.ERROR_MESSAGE);	// Flag Editor
					}
            	}
            }
          });

	}	// End HG0513FlagEditor constructor

/**
 * checkFlagValueChanges(String newValues)
 * @param newValues
 * @return true if all tests passed
 * @throws HBException
 */
	private boolean checkFlagValueChanges(String newValues) throws HBException {
		// Break the new Values into comma-separated items
		String[] newValuesList = newValues.split("\\,");		//$NON-NLS-1$

		// For addFlag case, wait until at least 3 letters typed
		if (addFlag && newValuesList.length < 2) return false;

		// Check for delimiter ',' and last char no ','
		if (newValues.contains(",") && newValues.charAt(newValues.length()-1) != ',') {	//$NON-NLS-1$

		// New Values must have at least 2 entries, comma-separated
			if (newValuesList.length < 2)
				throw new HBException(HG0513Msgs.Text_21);	// Flag Value list must have at least 2 entries separated by ','

		// All new Values must be between 1 and 10 char long
			for (int i = 0; i < newValuesList.length; i++)
				if (newValuesList[i].length() == 0 || newValuesList[i].length() > 10)
					throw new HBException(HG0513Msgs.Text_22);	// Flag Value element too short or too long
			}

		// If NOT a new Flag, check the old vs. new Values
		// newValuesList can have added items, but cannot have fewer
		// First letter of newValues items must match first letter of oldValues items,
		// except that "?" may be changed to "Unknown" (and its language equivalents)
			if (!addFlag) {
				// Test new #values >= old #values
				if (oldValuesList.length > newValuesList.length)
					throw new HBException(HG0513Msgs.Text_23);	// you are not allowed to delete Flag Values
				// Test new-values 1st letter match old-values 1st letter
				for (int i = 0; i < oldValuesList.length; i++)
					if ((oldValuesList[i].charAt(0) != newValuesList[i].charAt(0))) {
						// Test "?" changed to 1st letter of "Unknown"
						if (oldValuesList[i].charAt(0) == question && newValuesList[i].charAt(0) == unknown.charAt(0))
							return true;
						throw new HBException(HG0513Msgs.Text_24);	// changed Flag Values must start with the same letter as before
					}
			}
		return true;
	}	// End of checkFlagValueChanges

}  // End of HG0513FlagEditor