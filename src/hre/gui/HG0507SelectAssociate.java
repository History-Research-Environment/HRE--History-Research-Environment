package hre.gui;
/**************************************************************************************
 * HG0507SelectAssociate - extends HG0507SelectPerson
 * User GUI for add and update associate table in edit event
 * ************************************************************************************
 * v0.03.0031 2024-04-05 First version (N. Tolleshaug)
 * 			  2024-04-05 Handling of associate select (N. Tolleshaug)
 * 			  2024-05-09 Settin up selected associate edit (N. Tolleshaug)
 * 			  2024-05-20 Add and edit associate and memo (N. Tolleshaug)
 * 			  2024-07-31 Revised HG0507SelectAssociate buttons (N. Tolleshaug)
 * 			  2024-08-24 NLS conversion (D Ferguson)-
 * 			  2025-05-09 Reload associate and citation event add/edit(N.Tolleshaug)
 *       	  2026-01-01 Updated code for pointer to HBEventRoleManager (N. Tolleshaug)
 * v0.04.0032 2026-01-06 Log catch block and DEBUG msgs (D Ferguson)
 * v0.05.0033 2026-05-11 Handling duplicate associate roles (N. Tolleshaug)
 * 			  2026-05-15 Update NLS (D Ferguson)
 * v0.05.0034 2026-07-23 Remove SentenceEditor button for AddAssoc case (D Ferguson)
 * 			  2026-07-24 Setup rolename passed to EditSentence (D Ferguson)
 * 			  2026-07-25 Pass sexcodes to Sentence Editor (D Ferguson)
 * 			  2026-07-29 Remove use of 'sentenceRole' variable (D Ferguson)
 * 			  2026-07-31 Added eventTablePID = (long) assocRelationData[4]; (N. Tolleshaug)
 * 			  2026-08-05 ownerType = 3; Owner type associate for LOCAL(N. Tolleshaug)
 * 			  2026-08-16 Add Preferred name functions to persRolePanel (D Ferguson)
 * 			  2026-08-22 Make 1st Primary role the default in role list (D Ferguson)
 * 			  2026-08-28 Implemented Preferred name for associate (N. Tolleshaug)
 * 			  2026-09-03 Update NLS (D Ferguson)
 **************************************************************************************/

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import hre.bila.HB0711Logging;
import hre.bila.HBEventRoleManager;
import hre.bila.HBException;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.bila.HBWhereWhenHandler;
import hre.nls.HG05070Msgs;

/**
 * HG0507SelectAssociate
 * @author N Tolleshaug
 * @version v0.05.0034
 * @since 2024-04-05
 */

public class HG0507SelectAssociate extends HG0507SelectPerson {
	private static final long serialVersionUID = 1L;

	HBWhereWhenHandler pointWhereWhenHandler;
	HG0507SelectAssociate pointSelectAssociate = this;
	HBEventRoleManager pointEventRoleManager;

	Object[][] roleData;
	String[] assocRoleNames;
	int[] assocRoleNumbers;
	boolean[] assocRoleKey;
	Object[] assocRelationData = null;
	String memoString;
	long assocTablePID, assocPersonRPID;
	int selectedIndex = 0, rows = 0;

/**
 * HG0507SelectAssociate constructor
 * @param pointPersonHandler
 * @param pointOpenProject
 * @param eventNumber
 * @param indexInAssocTable
 * @throws HBException
 */
	public HG0507SelectAssociate(HBPersonHandler pointPersonHandler, HBProjectOpenData pointOpenProject,
									int eventNumber, int indexInAssocTable, boolean addAssoc) throws HBException {
		super(pointPersonHandler, pointOpenProject, addAssoc);
		this.addRelation = addAssoc;
		ownerType = 3; // Set owner type associate for LOCAL
		pointEventRoleManager = pointOpenProject.getEventRoleManager();
		pointEventRoleManager.setSelectedLanguage(HGlobal.dataLanguage);

	// Set titles for Associate Select
		selectTitle = HG05070Msgs.Text_150;	// Select New Associate
		newTitle = HG05070Msgs.Text_151;	// New Associate:
		addTitle = HG05070Msgs.Text_152;	// Add New Associate
		setTitle(selectTitle); // Set first title

		btn_SaveEvent.setEnabled(false);
		btn_SaveEvent.setVisible(false);
		btn_Save.setText(HG05070Msgs.Text_153);	// Save new Associate

		pointWhereWhenHandler = pointOpenProject.getWhereWhenHandler();

	// Get the selected Persons PID, Role#, Name and sexNum code
		assocRelationData = pointWhereWhenHandler.getAssocTableData(indexInAssocTable);
	/**
		assocRelationData  content:
				[0] = assocTablePID, [1] = eventRoleCode, [2] = personName.trim(),
			    [3] = sexNum, [4] = eventTablePID;
    */
	// Decode the sex number value to a String code (U/F/M)
		if (assocRelationData != null) {
			int sexNum = (int) assocRelationData[3];
			if (sexNum == 2) sexCode = "M";			//$NON-NLS-1$
			else if (sexNum == 1) sexCode = "F";	//$NON-NLS-1$
			else sexCode = "U";						//$NON-NLS-1$

		// Get the PID for the event table 	- T450_EVNT
			ownerTablePID = (long) assocRelationData[0];
			eventTablePID = (long) assocRelationData[4];

			if(HGlobal.DEBUG && HGlobal.writeLogs)
				HB0711Logging.logWrite("Status: in HG0507SelAssoc AssocData: " 		//$NON-NLS-1$
						+ assocRelationData[0] + "/" + assocRelationData[1] + "/"	//$NON-NLS-1$ //$NON-NLS-2$
						+ assocRelationData[2] +"/" + assocRelationData[3]) ;		//$NON-NLS-1$

			assocTablePID = (long) assocRelationData[0];
			citedTablePID = assocTablePID;

			try {
				String selectString = pointPersonHandler.setSelectSQL("*", pointPersonHandler.eventAssocTable,	//$NON-NLS-1$
					"PID = " + assocTablePID);																//$NON-NLS-1$
				ResultSet assocTableRS = pointPersonHandler.requestTableData(selectString, dataBaseIndex);
				personTablePID = pointOpenProject.getSelectedPersonPID();
				assocTableRS.first();
				assocPersonRPID = assocTableRS.getLong("ASSOC_RPID");		//$NON-NLS-1$
				objNameData1 = pointPersonHandler.preparePersonNameTable(assocPersonRPID, false);
				assocPrefNamePID = assocTableRS.getLong("PREF_NAME_RPID");	//$NON-NLS-1$
			} catch (SQLException sqle) {
				if (HGlobal.writeLogs) {
					HB0711Logging.logWrite("ERROR: in HG0507SelAssoc partner data load: " + sqle.getMessage()); //$NON-NLS-1$
					HB0711Logging.printStackTraceToFile(sqle);
				}
			}

		// Set up list of pref person names and select preferred
			JLabel lbl_prefName1 = new JLabel(HG05070Msgs.Text_155);		// Preferred Name:
			persRolePanel.add(lbl_prefName1, "cell 2 0, alignx right");	//$NON-NLS-1$
			selectedIndex = 0;
			rows = objNameData1.length;
			prefNameOptions1 = new String[rows + 1];
			prefNameOptions1[0] = defaultSetting;
			for (int i = 0; i < rows; i++) {
				prefNameOptions1[i + 1] = (String) objNameData1[i][1];
				//System.out.println(" Assoc compare: " + priPartnerPrefNamePID
				//	+ " & " + objNameData1[i][3]);
				if (assocPrefNamePID == (long) objNameData1[i][3])  selectedIndex = i + 1;
			}

			DefaultComboBoxModel<String> comboNameModel1
						= new DefaultComboBoxModel<>(prefNameOptions1);			// Load names
			combo_prefName1 = new JComboBox<>(comboNameModel1);
			combo_prefName1.setSelectedIndex(selectedIndex); // Set preferred name for event
			persRolePanel.add(combo_prefName1, "cell 2 0, alignx right");	//$NON-NLS-1$

		// Set up listener for preferred name
			activatePrefNameListener();
		}
	// Get the role data for this event type
		// roleData[i][0] = EVNT_ROLE_NAME		roleData[i][1] = EVNT_ROLE_NUM
		// roleData[i][2] = EVNT_ROLE_SEQ		roleData[i][3] = KEY_ASSOC - boolean
		roleData = pointEventRoleManager.getRolesDataForEvent(eventNumber, ""); //$NON-NLS-1$

	// Update event memo
	// Disable memoText listener first
		memoText.getDocument().removeDocumentListener(memoTextChange);
		if (assocRelationData != null)
			memoString = pointPersonHandler.readSelectGUIMemo((long)assocRelationData[0],
							pointPersonHandler.eventAssocTable);
		else memoString = "";		//  No memo found		//$NON-NLS-1$

		memoText.append(memoString);
	// and enable listener again
		memoText.getDocument().addDocumentListener(memoTextChange);

	// Set assoc name in window
		if (assocRelationData != null)
			lbl_PersonName.setText((String) assocRelationData[2]);	// Edit assoc:

	// Collect assoc role data
		assocRoleNames = new String[roleData.length];		// for role names
		assocRoleNumbers = new int[roleData.length];		// for role numbers
		assocRoleKey = new boolean[roleData.length];		// for key indicator
		for (int i= 0; i < roleData.length; i++)	{
			assocRoleNames[i] = (String)roleData[i][0];
			assocRoleNumbers[i] = (Integer)roleData[i][1];
			assocRoleKey[i] = (boolean)roleData[i][3];
		}
	// Load the combobox with rolenames
	    updateComboPanel(comboBox_Relationships, assocRoleNames);
	// Find first Key assoc in role list and make it the default displayed
		for (int i= 0; i < roleData.length; i++)	{
			if (assocRoleKey[i]) {
				comboBox_Relationships.setSelectedIndex(i);
				break;
			}
		}
		comboBox_Relationships.setVisible(true);

	// Remove the Sentence Editor button from screen for Add Assoc case
		if (addRelation) btn_Sentence.setVisible(false);

/********************
 * Action Listeners
 *******************/
	// Listener for Select Associate Save button
		btn_Save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dispose();
				try {
					int selectedAssociateRole = comboBox_Relationships.getSelectedIndex();
					int roleNumber = (int) roleData[selectedAssociateRole][1];
					if (addRelation) {
						assocTablePID = pointWhereWhenHandler.createAssocTableRow(roleNumber, personPID);
						pointPersonHandler.setAssociateTablePID(assocTablePID);
						if (memoEdited)
							pointPersonHandler.createSelectGUIMemo(memoText.getText(),
									pointPersonHandler.eventAssocTable);
					} else {
						if (memoEdited)
							pointPersonHandler.updateSelectGUIMemo(memoText.getText(),
								(long)assocRelationData[0], pointPersonHandler.eventAssocTable);
						pointWhereWhenHandler.updateAssocTableRow((long)assocRelationData[0], roleNumber);

						if (changedPrefName)
							pointWhereWhenHandler.updateAssocPrefName((long)assocRelationData[0], assocPrefNamePID);
					}

					if (pointEditEvent != null)
						pointEditEvent.resetAssociateTable(assocTablePID);
					else if (HGlobal.writeLogs)
						HB0711Logging.logWrite("Status: in HG0507SelAssoc pointEditEvent == null");  //$NON-NLS-1$

					persRolePanel.setVisible(false);
					persRolePanel.remove(pointSelectAssociate);

				} catch (HBException hbe) {
					if (hbe.getMessage().startsWith("###")) {		//$NON-NLS-1$
						try {
							if (HGlobal.writeLogs) {
								HB0711Logging.logWrite("WARNING Duplicate role for "+ pointPersonHandler.getPersonName(personPID)); //$NON-NLS-1$
								HB0711Logging.printStackTraceToFile(hbe);
							}
							JOptionPane.showMessageDialog(persRolePanel,
									HG05070Msgs.Text_158 + pointPersonHandler.getPersonName(personPID), // Role already exists for
									HG05070Msgs.Text_159,		// Duplicate role error
									JOptionPane.ERROR_MESSAGE);
						} catch (HBException hbee) {
							if (HGlobal.writeLogs) {
								HB0711Logging.logWrite("ERROR: in HG0507SelAssoc - pointPersonHandler.getPersonName " + hbee.getMessage()); //$NON-NLS-1$
								HB0711Logging.printStackTraceToFile(hbee);
							}
						}
					} else
						if (HGlobal.writeLogs) {
							HB0711Logging.logWrite("ERROR: in HG0507SelAssoc save: " + hbe.getMessage()); //$NON-NLS-1$
							HB0711Logging.printStackTraceToFile(hbe);
						}
				}
			}
		});
	} // End HG0507SelectAssociate constructor

/**
 * 	public void setAssocRoleEdit()
 */
	public void setAssocRoleEdit()	{
    	int assocRole = 0;
    	if (assocRelationData != null)
			assocRole = (int) assocRelationData[1];
    	int assocRoleindex = 0;
		btn_SaveEvent.setEnabled(false);
		btn_SaveEvent.setVisible(false);
		btn_Save.setEnabled(true);
		btn_Save.setVisible(true);
		btn_Save.setText(HG05070Msgs.Text_157);	// Update Associate
    	for (int i = 0; i < assocRoleNumbers.length; i++) {
    		if (assocRoleNumbers[i] == assocRole) assocRoleindex = i;
    	}
    	comboBox_Relationships.setSelectedIndex(assocRoleindex);
    	eventRoleNumber = assocRole;
	}

/**
 * updateComboPanel(JComboBox<String> jCombo, String[] items)
 * @param jCombo
 * @param items
 */
    public void updateComboPanel(JComboBox<String> jCombo, String[] items) {
    // getting exiting combo box model
        DefaultComboBoxModel<String> model =  (DefaultComboBoxModel<String>) jCombo.getModel();
    // removing old data
        model.removeAllElements();
        for (String item : items) {
			model.addElement(item);
		}
	// setting model with new data
	    jCombo.setModel(model);
	}

}
