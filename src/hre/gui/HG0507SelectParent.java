package hre.gui;
/**************************************************************************************
 * HG0507SelectParent - Original Specs 05.07 GUI_EntitySelect 2019-09-16
 * ***********************************************************************************
 * v0.03.0031 2024-04-05 Extended dialog for parent add and edit (N. Tolleshaug)
 * 			  2024-05-08 Setting up edit selected parent (N. Tolleshaug)
 * 			  2024-05-20 Add and edit parent and memo (N. Tolleshaug)
 * 			  2024-07-31 Revised  HG0507SelectParent buttons (N. Tolleshaug)
 * 			  2024-08-24 NLS conversion (D Ferguson)
 * 			  2024-10-03 Added reset T401/402 and PS/PM reset (N. Tolleshaug)
 *************************************************************************************
 * NOTES on missing functionality
 * 		Need check that we're not adding a parent to itself
 * 		Need to match roles to sex of selected person (so don't add male as a female role)
 *      (use pointPersonHandler.getPersonSex(personPID) to get sexIndex of chosen parent)
 * ***********************************************************************************/
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**************************************************************************************
 * HG0507SelectParent - extends HG0507SelectPerson
 * User GUI for add/edit  parent table in edit event
 * ************************************************************************************
 * v0.03.0031 2024-04-05 First version (N. Tolleshaug)
 * 			  2024-04-05 Handling of parent select (N. Tolleshaug)
  **************************************************************************************
 * NOTE - Code activate for parent select
 **************************************************************************************/
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import hre.bila.HBException;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.nls.HG05070Msgs;

/**
 * HG0507SelectParent
 * @author N Tolleshaug
 * @version v0.03.0031
 * @since 2024-04-05
 */
public class HG0507SelectParent extends HG0507SelectPerson {
	private static final long serialVersionUID = 1L;

	String[] parentRoleList;
	int[] parentRoleType;
	Object[] parentRelationData = null;
	int selectedRowInTable;
	final static int parentEventGroup = -1;
	String memoString;
	boolean addRelation;

/**
 * HG0507SelectParent constructor
 * @param pointPersonHandler
 * @param pointOpenProject
 * @throws HBException
 */
	public HG0507SelectParent(HBPersonHandler pointPersonHandler,
			HBProjectOpenData pointOpenProject, int selectedRowInTable, boolean addParent) throws HBException {
		super(pointPersonHandler, pointOpenProject, addParent);
		this.selectedRowInTable = selectedRowInTable;
		this.addRelation = addParent;
	// Set titles for Parent Select
		selectTitle = HG05070Msgs.Text_160;	// Select New Parent
		newTitle = HG05070Msgs.Text_161;	// New Parent:
		addTitle = HG05070Msgs.Text_162;	// Add New Parent
		setTitle(selectTitle); // Set first title
	// Setup of parentRelationData:
	//	0 = ParentPID, 1 = parent Name, 2 = parentRole, 3 = surety, 4 = parentRPID
		parentRelationData = pointPersonHandler.getParentTableData(selectedRowInTable);

		btn_SaveEvent.setEnabled(false);
		btn_SaveEvent.setVisible(false);
		btn_Save.setText(HG05070Msgs.Text_163);	// Save new Parent

	// Add code for select parent role and activate save button
		lbl_Relate.setText(HG05070Msgs.Text_164);	//  Set Parent type

	// Set parent name in window
		if (parentRelationData != null) {
			lbl_ParentName.setText(HG05070Msgs.Text_165 + (String) parentRelationData[1]);	// Edit parent:
		} 

		parentRoleList = pointPersonHandler.getRolesForParent(parentEventGroup);
		parentRoleType = pointPersonHandler.getRolesTypeParent();
	    updateComboPanel(comboBox_Relationships, parentRoleList);

	// Update event memo
	// Disable memoText listener first
		memoText.getDocument().removeDocumentListener(memoTextChange);
		if (parentRelationData != null) {
			memoString = pointPersonHandler.readSelectGUIMemo((long)parentRelationData[0],
							pointPersonHandler.personParentTable);
		} else {
			memoString = HG05070Msgs.Text_155;		//  No memo found
		}
		memoText.append(memoString);
	// and enable it again
		memoText.getDocument().addDocumentListener(memoTextChange);

	// Set correct parent role in combo when edit
	    if (!addRelation) {
	    	int parentRoleNumber = (int) parentRelationData[2];
	    	int parentRoleindex = 0;
	    	for (int i = 0; i < parentRoleType.length; i++) {
	    		if (parentRoleType[i] == parentRoleNumber) {
					parentRoleindex = i;
				}
	    	}
	    	comboBox_Relationships.setSelectedIndex(parentRoleindex);
	    } else {
			comboBox_Relationships.setSelectedIndex(1);
		}
		comboBox_Relationships.setVisible(true);

	// Listener for Select Parent Save button
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					int selectedParentRole = comboBox_Relationships.getSelectedIndex();
					pointPersonHandler.setParentRole(selectedParentRole);
					if (addRelation) {
						pointPersonHandler.addNewParent(personPID);
						if (memoEdited) 
							pointPersonHandler.createSelectGUIMemo(memoText.getText(),
									pointPersonHandler.personParentTable);		
					} else {
						if (memoEdited) 
							pointPersonHandler.updateSelectGUIMemo(memoText.getText(),
								(long)parentRelationData[0], pointPersonHandler.personParentTable);
						
						pointPersonHandler.setParentRole(selectedParentRole);
						pointPersonHandler.updateParentRelationTableRow((long)parentRelationData[0]);
					}
					
					pointOpenProject.reloadT401Persons();
					pointOpenProject.reloadT402Names();
					pointOpenProject.getPersonHandler().resetPersonSelect();
					pointOpenProject.getPersonHandler().resetPersonManager();
					dispose();
				} catch (HBException hbe) {
					System.out.println(" HG0507SelectParent error: " + hbe.getMessage());	//$NON-NLS-1$
					hbe.printStackTrace();
				}
			}
		});
	} // end constructor

/**
 * 	public void setParentRoleEdit()
 */
	public void setEditParentRole()	{
    	int parentRoleNumber = 0;
    	if (parentRelationData != null) {
			parentRoleNumber = (int) parentRelationData[2];
		}
    	int parentRoleindex = 0;
		btn_SaveEvent.setEnabled(false);
		btn_SaveEvent.setVisible(false);
		btn_Save.setEnabled(true);
		btn_Save.setVisible(true);
		btn_Save.setText(HG05070Msgs.Text_166);	// Update Parent
    	for (int i = 0; i < parentRoleType.length; i++) {
    		if (parentRoleType[i] == parentRoleNumber) {
				parentRoleindex = i;
			}
    	}
    	comboBox_Relationships.setSelectedIndex(parentRoleindex);
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
