package hre.gui;
/**************************************************************************************
 * HG0507SelectPartner - extends HG0507SelectPerson for add/edit partner table
 * ***********************************************************************************
 * v0.03.0031 2024-04-05 First version (N. Tolleshaug)
 * 			  2024-04-05 Handling of partner select (N. Tolleshaug)
 * 			  2024-04-05 Extended dialog for partner add and edit (N. Tolleshaug)
 * 			  2024-05-08 Setting up edit selected partner (N. Tolleshaug)
 *            2024-05-20 Add and edit partner and memo (N. Tolleshaug)
 *            2024-06-09 Change Save options to be with/without event (D Ferguson)
 *            2024-07-31 Revised HG0507SelectPartner buttons (N. Tolleshaug)
 * 			  2024-08-24 NLS conversion (D Ferguson)-
 * v0.04.0032 2025-04-27 Add handling of citations (D Ferguson)
 * 			  2025-06-05 Address minor layout errors (D Ferguson)
 * 			  2026-01-06 Log all catch block and DEBUG msgs (D Ferguson)
 * v0.05.0034 2026-07-23 Remove Sentence Editor button from screen for AddPartner (D Ferguson)
 * 			  2026-07-24 Pass correct rolename to EditSentence (D Ferguson)
 * 			  2026-07-25 Pass sexcodes to Sentence Editor (D Ferguson)
 * 			  2026-07-27 Added code to to initiate data for sentence edit (N. Tolleshaug)
 * 			  2026-07-29 Remove use of 'sentenceRole' variables (D Ferguson)
 *************************************************************************************/

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.bila.HBWhereWhenHandler;
import hre.nls.HG05070Msgs;

/**
 * HG0507SelectPartner
 * @author N Tolleshaug
 * @version v0.05.0034
 * @since 2024-04-05
 */

public class HG0507SelectPartner extends HG0507SelectPerson {
	private static final long serialVersionUID = 1L;

	long null_RPID  = 1999999999999999L;
	protected HBWhereWhenHandler pointHBWhereWhenHandler;
	HG0507SelectPartner pointSelectPartner = this;
	boolean addRelation;
	String[] partnerTypeList;
	int [] partnerTypeNumbers;
	Object[] partnerRelationData;
	final static int partnerEventGroup = -2; // Include both group 6 and 7

	private ActionListener comboRole1Change = null;
	private ActionListener comboRole2Change = null;
	int selectedPartTypeIndex, selectedPartnerType, partRole1, partRole2;
	long createdPartnerTablePID;

	static int partnerEventNumber = 1004; // marriage event
	String selectPartnerRoles = " AND EVNT_ROLE_NUM BETWEEN 1 AND 99";	//$NON-NLS-1$
	int priRole, secRole;
	String memoString;

/**
 * HG0507SelectPartner constructor
 * @param pointPersonHandler
 * @param pointOpenProject
 * @param titleType
 * @throws HBException
 */
	public HG0507SelectPartner(HBPersonHandler pointPersonHandler,
										HBProjectOpenData pointOpenProject,
										 int selectedRowInTable, boolean addPartner) throws HBException {
		super(pointPersonHandler, pointOpenProject, addPartner);
		this.addRelation = addPartner;
		citeTableName = "T404";		//$NON-NLS-1$

	// Set titles for Partner Select
		selectTitle = HG05070Msgs.Text_170;	// Select New Partner
		newTitle = HG05070Msgs.Text_171;	// New Partner:
		addTitle = HG05070Msgs.Text_172;	// Add New Partner
		setTitle(addTitle); // Set first title

		btn_Save.setText(HG05070Msgs.Text_173);	// Add partner
		btn_SaveEvent.setEnabled(true);

		pointHBWhereWhenHandler = pointOpenProject.getWhereWhenHandler();
		
/* partnerRelationData content
  			      [0] = personPID; [1] = eventype; [2] = prirole; [3] = secrole;
				  [4] = priname; [5] = secname, [6] = sex# code of person, [7] = sex# of partner
				  [8] = partner event table PID
*/		
		partnerRelationData = pointPersonHandler.getPartnerTableData(selectedRowInTable);
		
	// Get the  data for this partnerPID
		if (partnerRelationData != null) {
			//System.out.println(" Select partner - event PID: " + partnerRelationData[8]);	
		// Set event and role for sentence editor
			eventTypeNumber = (int) partnerRelationData[1];
			eventRoleNumber = (int) partnerRelationData[2];
			eventTablePID = (long) partnerRelationData[8]; // Settig value in SelectPerson
	
		// Decode the sex number values to a String code (U/F/M)
			int sexNum = (int) partnerRelationData[6];
			if (sexNum == 2) sexCode = "M";			//$NON-NLS-1$
			else if (sexNum == 1) sexCode = "F";	//$NON-NLS-1$
			else sexCode = "U";						//$NON-NLS-1$
			sexNum = (int) partnerRelationData[7];
			if (sexNum == 2) sexCode2 = "M";			//$NON-NLS-1$
			else if (sexNum == 1) sexCode2 = "F";	//$NON-NLS-1$
			else sexCode2 = "U";						//$NON-NLS-1$
	
		// Update event memo
		// Disable memoText listener first
			memoText.getDocument().removeDocumentListener(memoTextChange);
			if (partnerRelationData != null)
				memoString = pointPersonHandler.readSelectGUIMemo((long)partnerRelationData[0],
																pointPersonHandler.personPartnerTable);
			else memoString = "";		//  No memo found	//$NON-NLS-1$
			memoText.append(memoString);
		// and enable listener again
			memoText.getDocument().addDocumentListener(memoTextChange);
	
		// Get the citation data for this partnerPID
		//if (partnerRelationData != null) {
			personPID = (long)partnerRelationData[0];
			objCiteData = pointCitationSourceHandler.getCitationSourceData(personPID, citeTableName); //for T404
			// and sort it on GUI sequence
			Arrays.sort(objCiteData, (o1, o2) -> Integer.compare((Integer) o1[4], (Integer) o2[4]));
			// and ensure it is displayed
			resetCitationTable(citeTableName);
		}

	// Get the partner details
		partnerTypeList = pointPersonHandler.getPartnerEventList(partnerEventGroup);
		partnerTypeNumbers = pointPersonHandler.getPartnerEventTypes();

	// Set initially marriage - 1004
		selectedPartnerType = partnerEventNumber;
	    updateComboPanel(comboBox_Relationships, partnerTypeList);
		comboBox_Relationships.setVisible(true);
	// Set default to marriage
		comboBox_Relationships.setSelectedIndex(1);

	// Load the role lists for chosen Event type
		partnerRoleList = pointPersonHandler.getRolesForEvent(partnerEventNumber, selectPartnerRoles);
		partnerRoleType = pointPersonHandler.getEventRoleTypes();

		if (partnerRelationData != null)
			lbl_nRole1 = new JLabel("" + partnerRelationData[4]);	//$NON-NLS-1$
		else
			lbl_nRole1 = new JLabel("");	//$NON-NLS-1$

		lbl_nRole1.setText("" + pointPersonHandler.getManagedPersonName());	//$NON-NLS-1$

	// Tailor the persRolePanel for partners
		lbl_Parent.setText(HG05070Msgs.Text_145);		//  Edit partner/event
		lbl_Relate.setText(HG05070Msgs.Text_174);		//  Set Event Type
		persRolePanel.add(lbl_nRole1, "cell 0 2");		//$NON-NLS-1$
		comboPartRole1 = new JComboBox<>(partnerRoleList);
		persRolePanel.add(comboPartRole1, "cell 0 2, gapx 10");		//$NON-NLS-1$
		persRolePanel.add(lbl_nRole2, "cell 1 2");		//$NON-NLS-1$
		comboPartRole2 = new JComboBox<>(partnerRoleList);
		persRolePanel.add(comboPartRole2, "cell 1 2, gapx 10");		//$NON-NLS-1$

	// Modify the Save buttons in the control panel
		btn_SaveEvent.setText(HG05070Msgs.Text_175);		// Add Partner & Event
		control2Panel.add(btn_Save, "cell 0 0, align left, gapx 10, tag ok");	//$NON-NLS-1$
		lbl_ParentName.setVisible(false);

	// Remove the Sentence Editor button from screen for Add Partner case
		if (addRelation) btn_Sentence.setVisible(false);

/********************
 * Action Listeners
 *******************/
	// Listener for selection within partner1 role combobox
		comboRole1Change = new ActionListener () {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	        	priRole = comboPartRole1.getSelectedIndex();
	    		if (HGlobal.DEBUG && HGlobal.writeLogs)
	    			HB0711Logging.logWrite("Status: in HG0507SelPartner Pri partner role nr: " + partnerRoleType[priRole] //$NON-NLS-1$
	    					+ " Role: " + comboPartRole1.getSelectedItem().toString()); //$NON-NLS-1$
	        }
	    };
		comboPartRole1.addActionListener(comboRole1Change);

	// Listener for selection within partner2 role combobox
		comboRole2Change = new ActionListener () {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	        	secRole = comboPartRole2.getSelectedIndex();
	    		if (HGlobal.DEBUG && HGlobal.writeLogs)
	    			HB0711Logging.logWrite("Status: in HG0507SelPartner Sec partner role nr: " + partnerRoleType[secRole] //$NON-NLS-1$
	    					+ " Role: " + comboPartRole2.getSelectedItem().toString()); //$NON-NLS-1$
	        }
	    };
		comboPartRole2.addActionListener(comboRole2Change);

		// ComboBox listener for partner event type and setting of partner role lists
		comboBox_Relationships.addActionListener (new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				selectedPartTypeIndex = comboBox_Relationships.getSelectedIndex();
				selectedPartnerType = partnerTypeNumbers[selectedPartTypeIndex];
	    		if (HGlobal.DEBUG && HGlobal.writeLogs)
	    			HB0711Logging.logWrite("Status: in HG0507SelPartner event index/type: "  //$NON-NLS-1$
	    					+ selectedPartTypeIndex + "/" + selectedPartnerType + " - /");  //$NON-NLS-1$ //$NON-NLS-2$
				try {
					partnerRoleList = pointPersonHandler.getRolesForEvent(selectedPartnerType, selectPartnerRoles);
					partnerRoleType = pointPersonHandler.getEventRoleTypes();
				// Disable the combobox listeners while we change their content
					comboPartRole1.removeActionListener(comboRole1Change);
					comboPartRole2.removeActionListener(comboRole2Change);
				// update Role comboboxes contents
					comboPartRole1.removeAllItems();
					comboPartRole2.removeAllItems();
					for (String element : partnerRoleList) {
						comboPartRole1.addItem(element);
						comboPartRole2.addItem(element);
					}
				// re-instate the combobox listeners
					comboPartRole1.addActionListener(comboRole1Change);
					comboPartRole2.addActionListener(comboRole2Change);
				// set start entry
					comboPartRole1.setSelectedIndex(0);
					comboPartRole2.setSelectedIndex(0);

				} catch (HBException hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0507SelPartner role reset: " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
			}
		});

		// Listener for 'Add partner with Event'
		btn_SaveEvent.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				HG0547EditEvent editPartnerEventScreen;
				long createdPartnerTablePID;
				try {
					pointPersonHandler.setNewPartnerPID(personPID);
					if (addRelation) {
						createdPartnerTablePID = pointPersonHandler.addNewPartner(selectedPartnerType, partnerRoleType[priRole], partnerRoleType[secRole]);
						if (memoEdited) {
							pointPersonHandler.createSelectGUIMemo(memoText.getText(),pointPersonHandler.personPartnerTable);
						}
					// Add new partner event
						editPartnerEventScreen = pointHBWhereWhenHandler.activateAddPartnerEvent(pointOpenProject,
																	selectedPartnerType, 0, createdPartnerTablePID, 0);
						editPartnerEventScreen.setModalityType(ModalityType.APPLICATION_MODAL);
						Point xyShow = pointSelectPartner.getLocationOnScreen();
						editPartnerEventScreen.setLocation(xyShow.x, xyShow.y);
						editPartnerEventScreen.setVisible(true);
					} else {
						if (memoEdited)
							pointPersonHandler.updateSelectGUIMemo(memoText.getText(),
									(long)partnerRelationData[0], pointPersonHandler.personPartnerTable);

						// update partner table memo
						pointPersonHandler.updatePartner((long)partnerRelationData[0],
												selectedPartnerType, partnerRoleType[priRole], partnerRoleType[secRole]);
					}

					// Redo citation sequence, but only if more than 1 citation left
					if (citationOrderChanged && objCiteData.length > 1) {
						pointCitationSourceHandler.updateCiteGUIseq(personPID, citeTableName, objCiteData);
						citationOrderChanged = false;
					}

					pointOpenProject.reloadT401Persons();
					pointOpenProject.reloadT402Names();
					pointOpenProject.getPersonHandler().resetPersonSelect();
					pointOpenProject.getPersonHandler().resetPersonManager();
					dispose();

				} catch (HBException hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0507SelPartner event save: " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
			}
		});

		// Listener for 'Add Partner, no Event'
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					pointPersonHandler.setNewPartnerPID(personPID);
					if (addRelation) {
						createdPartnerTablePID = pointPersonHandler.addNewPartner(selectedPartnerType, partnerRoleType[priRole], partnerRoleType[secRole]);
						if (memoEdited)
							pointPersonHandler.createSelectGUIMemo(memoText.getText(),pointPersonHandler.personPartnerTable);
					} else {
						if (memoEdited)
							pointPersonHandler.updateSelectGUIMemo(memoText.getText(),
									(long)partnerRelationData[0], pointPersonHandler.personPartnerTable);
						// update partner table
						pointPersonHandler.updatePartner((long)partnerRelationData[0],
												selectedPartnerType, partnerRoleType[priRole], partnerRoleType[secRole]);
					}

				} catch (HBException hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0507SelPartner save: " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
				pointOpenProject.reloadT401Persons();
				pointOpenProject.reloadT402Names();
				pointOpenProject.getPersonHandler().resetPersonSelect();
				pointOpenProject.getPersonHandler().resetPersonManager();
				dispose();
			}
		});
	}		// End HG0507SelectPartner constructor

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

/**
 * 	public void setEditPartnerRole()
 */
	public void setEditPartnerRole()	{
    	int partnerEventNumber = (int) partnerRelationData[1];
    	int priPartnerRole = (int) partnerRelationData[2];
    	int secPartnerRole = (int) partnerRelationData[3];
    	int partnerRoleindex = 0;
    	int priPartnerRoleIndex = 0;
    	int secPartnerRoleIndex = 0;

		btn_SaveEvent.setEnabled(false);
		btn_SaveEvent.setVisible(false);
		btn_Save.setText(HG05070Msgs.Text_176);		// Update

    // Set edit label for partner2
    	lbl_nRole2.setText("" + partnerRelationData[5]);	//$NON-NLS-1$
    	for (int i = 0; i < partnerTypeNumbers.length; i++) {
    		if (partnerTypeNumbers[i] == partnerEventNumber) {
				partnerRoleindex = i;
			}
    	}
    	comboBox_Relationships.setSelectedIndex(partnerRoleindex);

    	for (int i = 0; i < partnerRoleType.length; i++) {
    		if (partnerRoleType[i] == priPartnerRole)
				priPartnerRoleIndex = i;
    	}
    	comboPartRole1.setSelectedIndex(priPartnerRoleIndex);

    	for (int i = 0; i < partnerRoleType.length; i++) {
    		if (partnerRoleType[i] == secPartnerRole)
				secPartnerRoleIndex = i;
    	}
    	comboPartRole2.setSelectedIndex(secPartnerRoleIndex);
	}
}		// End HG0507SelectPartner
