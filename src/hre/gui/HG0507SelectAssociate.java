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
 **************************************************************************************/

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import hre.bila.HBException;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.bila.HBWhereWhenHandler;
import hre.nls.HG05070Msgs;

/**
 * HG0507SelectAssociate
 * @author N Tolleshaug
 * @version v0.03.0031
 * @since 2024-04-05
 */
	
public class HG0507SelectAssociate extends HG0507SelectPerson {
	private static final long serialVersionUID = 1L;
	
	HBWhereWhenHandler pointWhereWhenHandler;
	
	Object[][] roleData;
	String[] assocRoleList;
	int[] assocRoleNumber;
	Object[] assocRelationData = null;
	String memoString;
	long assocTablePID;
	
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
	// Set titles for Associate Select
		selectTitle = HG05070Msgs.Text_150;	// Select New Associate
		newTitle = HG05070Msgs.Text_151;	// New Associate:
		addTitle = HG05070Msgs.Text_152;	// Add New Associate
		setTitle(selectTitle); // Set first title
		
		btn_SaveEvent.setEnabled(false);
		btn_SaveEvent.setVisible(false);
		btn_Save.setText(HG05070Msgs.Text_153);	// Save new Associate
		
	// Add code for select role and activate save button
		lbl_Relate.setText(HG05070Msgs.Text_154);	//   Set Associate type
		pointWhereWhenHandler = pointOpenProject.getWhereWhenHandler();
		assocRelationData = pointWhereWhenHandler.getAssocTableData(indexInAssocTable);
		if (assocRelationData != null)
			if(HGlobal.DEBUG) 
				System.out.println(" AssocData: " + assocRelationData[0] + "/" + assocRelationData[1] + "/" + assocRelationData[2]); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		roleData = pointWhereWhenHandler.getEventRoleData(eventNumber, "");		//$NON-NLS-1$
		
	// Update event memo
	// Disable memoText listener first
		memoText.getDocument().removeDocumentListener(memoTextChange);
		if (assocRelationData != null)
			memoString = pointPersonHandler.readSelectGUIMemo((long)assocRelationData[0],
							pointPersonHandler.eventAssocTable);
		else memoString = HG05070Msgs.Text_155;		//  No memo found
		memoText.append(memoString);
	// and enable it again
		memoText.getDocument().addDocumentListener(memoTextChange);

	// Set assoc name in window
		if (assocRelationData != null)
			lbl_ParentName.setText(HG05070Msgs.Text_156 + (String) assocRelationData[2]);		// Edit assoc: 
	// Collect assoc role data	
		assocRoleList = new String[roleData.length]; 
		assocRoleNumber = new int[roleData.length];
		for (int i= 0; i < roleData.length; i++) assocRoleList[i] = (String)roleData[i][0];
		for (int i= 0; i < roleData.length; i++) assocRoleNumber[i] = (Integer)roleData[i][1];
	    updateComboPanel(comboBox_Relationships, assocRoleList);
		comboBox_Relationships.setVisible(true);
			
	// Listener for Select Associate Save button
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {				
				try {
					int selectedAssociateRole = comboBox_Relationships.getSelectedIndex();
					int roleNumber = (int) roleData[selectedAssociateRole][1];
					if (addRelation) {
						assocTablePID = pointWhereWhenHandler.createAssocTableRow(roleNumber, personPID);
						pointPersonHandler.setRelationTablePID(assocTablePID);
						if (memoEdited) 
							pointPersonHandler.createSelectGUIMemo(memoText.getText(), 
									pointPersonHandler.eventAssocTable);
					} else {
						if (memoEdited) 
							pointPersonHandler.updateSelectGUIMemo(memoText.getText(), 
								(long)assocRelationData[0], pointPersonHandler.eventAssocTable);
						pointWhereWhenHandler.updateAssocTableRow((long)assocRelationData[0], roleNumber);
					}
					
					persRolePanel.setVisible(false);
					pointPersonHandler.resetPersonManager();
					dispose();
				
				} catch (HBException hbe) {
					System.out.println(" HG0507SelectAssociate error: " + hbe.getMessage());	//$NON-NLS-1$
					hbe.printStackTrace();
				} 								
			}
		});  
	} // End HG0507SelectAssociate constructor
	
/**
 * 	public void setAssocRoleEdit()
 */
	public void setAssocRoleEdit()	{
    	int assocRole = 0;
    	if (assocRelationData != null) assocRole = (int) assocRelationData[1];
    	int assocRoleindex = 0;
		btn_SaveEvent.setEnabled(false);
		btn_SaveEvent.setVisible(false);
		btn_Save.setEnabled(true);
		btn_Save.setVisible(true);
		btn_Save.setText(HG05070Msgs.Text_157);	// Update Associate
    	for (int i = 0; i < assocRoleNumber.length; i++) {
    		if (assocRoleNumber[i] == assocRole) assocRoleindex = i;	
    	}
    	comboBox_Relationships.setSelectedIndex(assocRoleindex);
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
        for (String item : items) model.addElement(item);
	// setting model with new data
	    jCombo.setModel(model);
	}
}
