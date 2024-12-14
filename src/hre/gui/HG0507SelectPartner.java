package hre.gui;
/**************************************************************************************
 * HG0507SelectPartner - Original Specs 05.07 GUI_EntitySelect 2019-09-16
 * User GUI for add/edit partner table
 * ***********************************************************************************
 * v0.03.0031 2024-04-05 First version (N. Tolleshaug)
 * 			  2024-04-05 Handling of partner select (N. Tolleshaug)
 * 			  2024-04-05 Extended dialog for partner add and edit (N. Tolleshaug)
 * 			  2024-05-08 Setting up edit selected partner (N. Tolleshaug)
 *            2024-05-20 Add and edit partner and memo (N. Tolleshaug)
 *            2024-06-09 Change Save options to be with/without event (D Ferguson)
 *            2024-07-31 Revised HG0507SelectPartner buttons (N. Tolleshaug)
* 			  2024-08-24 NLS conversion (D Ferguson)-
 *************************************************************************************/

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import hre.bila.HBException;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.bila.HBWhereWhenHandler;
import hre.nls.HG05070Msgs;

/**
 * HG0507SelectPartner
 * @author N Tolleshaug
 * @version v0.03.0031
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
	Object[] partnerRelationData = null;
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
	// Set titles for Partner Select
		selectTitle = HG05070Msgs.Text_170;	// Select New Partner
		newTitle = HG05070Msgs.Text_171;	// New Partner:
		addTitle = HG05070Msgs.Text_172;	// Add New Partner
		setTitle(addTitle); // Set first title

		btn_Save.setText(HG05070Msgs.Text_173);	// Add partner
		btn_SaveEvent.setEnabled(true);

		pointHBWhereWhenHandler = pointOpenProject.getWhereWhenHandler();
		partnerRelationData = pointPersonHandler.getPartnerTableData(selectedRowInTable);

	// Update event memo
	// Disable memoText listener first
		memoText.getDocument().removeDocumentListener(memoTextChange);
		if (partnerRelationData != null) {
			memoString = pointPersonHandler.readSelectGUIMemo((long)partnerRelationData[0],
															pointPersonHandler.personPartnerTable);
		}
		else {
			memoString = HG05070Msgs.Text_155;		//  No memo found
		}
		memoText.append(memoString);
	// and enable listener again
		memoText.getDocument().addDocumentListener(memoTextChange);

	// Add code for select partner role and activate save button
		lbl_Relate.setText(HG05070Msgs.Text_174);		//  Set Event Type
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

		if (partnerRelationData != null) {
			lbl_nRole1 = new JLabel("" + partnerRelationData[4]);	//$NON-NLS-1$
		} else { 
			lbl_nRole1 = new JLabel("");	//$NON-NLS-1$
		}
		lbl_nRole1.setText("" + pointPersonHandler.getManagedPersonName());	//$NON-NLS-1$

		persRolePanel.add(lbl_nRole1, "cell 0 5");		//$NON-NLS-1$
		comboPartRole1 = new JComboBox<>(partnerRoleList);
		persRolePanel.add(comboPartRole1, "cell 0 5, gapx 10");		//$NON-NLS-1$
		persRolePanel.add(lbl_nRole2, "cell 1 5");		//$NON-NLS-1$
		comboPartRole2 = new JComboBox<>(partnerRoleList);
		persRolePanel.add(comboPartRole2, "cell 1 5, gapx 10");		//$NON-NLS-1$

	// Modify the Save buttons in the control panel
		btn_SaveEvent.setText(HG05070Msgs.Text_175);		// Add Partner & Event
		control2Panel.add(btn_Save, "cell 0 0, align left, gapx 10, tag ok");	//$NON-NLS-1$
		lbl_ParentName.setVisible(false);

/********************
 * Action Listeners
 *******************/
	// Listener for selection within partner1 role combobox
		comboRole1Change = new ActionListener () {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	        	priRole = comboPartRole1.getSelectedIndex();
	        	if (HGlobal.DEBUG)
				 {
					System.out.println("Pri partner role nr: " + partnerRoleType[priRole] + " Role: " + comboPartRole1.getSelectedItem().toString()); //$NON-NLS-1$ //$NON-NLS-2$
				}
	        }
	    };
		comboPartRole1.addActionListener(comboRole1Change);

	// Listener for selection within partner2 role combobox
		comboRole2Change = new ActionListener () {
	        @Override
	        public void actionPerformed(ActionEvent e) {
	        	secRole = comboPartRole2.getSelectedIndex();
	        	if (HGlobal.DEBUG)
				 {
					System.out.println("Sec partner role nr: " + partnerRoleType[secRole] + " Role: " + comboPartRole2.getSelectedItem().toString()); //$NON-NLS-1$ //$NON-NLS-2$
				}
	        }
	    };
		comboPartRole2.addActionListener(comboRole2Change);

		// ComboBox listener for partner event type and setting of partner role lists
		comboBox_Relationships.addActionListener (new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				selectedPartTypeIndex = comboBox_Relationships.getSelectedIndex();
				selectedPartnerType = partnerTypeNumbers[selectedPartTypeIndex];
				if (HGlobal.DEBUG)
				 {
					System.out.println("Partner event index/type: " + selectedPartTypeIndex + "/" + selectedPartnerType + " - /");  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
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
					hbe.printStackTrace();
					System.out.println(" Add Person Partner role reset error: " + hbe.getMessage());	//$NON-NLS-1$
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
						if (memoEdited) {
							pointPersonHandler.updateSelectGUIMemo(memoText.getText(),
									(long)partnerRelationData[0], pointPersonHandler.personPartnerTable);
						}
					// update partner table
						pointPersonHandler.updatePartner((long)partnerRelationData[0],
												selectedPartnerType, partnerRoleType[priRole], partnerRoleType[secRole]);
					}

					pointOpenProject.reloadT401Persons();
					pointOpenProject.reloadT402Names();
					pointOpenProject.getPersonHandler().resetPersonSelect();
					pointOpenProject.getPersonHandler().resetPersonManager();
					dispose();

				} catch (HBException hbe) {
					System.out.println(" HG0507SelectPartner error: " + hbe.getMessage());	//$NON-NLS-1$
					hbe.printStackTrace();
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
						if (memoEdited) {
							pointPersonHandler.createSelectGUIMemo(memoText.getText(),pointPersonHandler.personPartnerTable);
						}

					} else {
						if (memoEdited) {
							pointPersonHandler.updateSelectGUIMemo(memoText.getText(),
									(long)partnerRelationData[0], pointPersonHandler.personPartnerTable);
						}
					// update partner table
						pointPersonHandler.updatePartner((long)partnerRelationData[0],
												selectedPartnerType, partnerRoleType[priRole], partnerRoleType[secRole]);
					}

				} catch (HBException hbe) {
					System.out.println(" HG0507SelectPartner error: " + hbe.getMessage());	//$NON-NLS-1$
					hbe.printStackTrace();
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
    	int partnerRoleNumber = (int) partnerRelationData[1];
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
    		if (partnerTypeNumbers[i] == partnerRoleNumber) {
				partnerRoleindex = i;
			}
    	}
    	comboBox_Relationships.setSelectedIndex(partnerRoleindex);

    	for (int i = 0; i < partnerRoleType.length; i++) {
    		if (partnerRoleType[i] == priPartnerRole) {
				priPartnerRoleIndex = i;
			}
    	}
    	comboPartRole1.setSelectedIndex(priPartnerRoleIndex);

    	for (int i = 0; i < partnerRoleType.length; i++) {
    		if (partnerRoleType[i] == secPartnerRole) {
				secPartnerRoleIndex = i;
			}
    	}
    	comboPartRole2.setSelectedIndex(secPartnerRoleIndex);
	}
}		// End HG0507SelectPartner
