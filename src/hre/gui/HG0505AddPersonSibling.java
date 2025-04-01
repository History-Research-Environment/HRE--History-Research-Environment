package hre.gui;
/*******************************************************************************
 * Add Person Sibling - Extends GUI_AddPerson
 * *****************************************************************************
 * v0.03.0031 2023-11-15 Prepared for add Sibling (N Tolleshaug)
 * 			  2023-12-30 Implemented add sibling (N Tolleshaug)
 * 			  2024-08-25 NLS conversion (D Ferguson)
 * 			  2024-10-12 Modify for HG0505AddPerson layout changes (D Ferguson)
 * 			  2024-10-22 only allow Save to proceed if Person has a Name (D Ferguson)
 * v0.04.0032 2024-12-26 Only turn Living flag to N if Death/Burial saved ( D Ferguson)
* 			  2025-03-24 Modify panelNameRelate layout (D Ferguson)
 ******************************************************************************/

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.nls.HG0505Msgs;

/**
 * class HG0505AddPersonSibling extends HG0505AddPerson
 */
public class HG0505AddPersonSibling extends HG0505AddPerson {
	private static final long serialVersionUID = 1L;

	String[] siblingTypes =  {HG0505Msgs.Text_70,		//  Full sibling
							  HG0505Msgs.Text_71,		//  Half-sibling (biological father)
							  HG0505Msgs.Text_72};		//  Half-sibling (biological mother)

/**
 * @param pointOpenProject
 * @param pointPersonHandler
 * @param sexIndex
 * @param ofPerson
 * @throws HBException
 */
	public HG0505AddPersonSibling(HBProjectOpenData pointOpenProject, HBPersonHandler pointPersonHandler,
			int sexIndex, String ofPerson) throws HBException {
		super(pointOpenProject, pointPersonHandler, sexIndex, false, ofPerson);

	// Set screen title
		if (sexIndex == 1) setTitle(HG0505Msgs.Text_73 + ofPerson);	// Add a Sister of
			else if (sexIndex == 2) setTitle(HG0505Msgs.Text_74 + ofPerson);	// Add a Brother of
				else setTitle(HG0505Msgs.Text_75 + ofPerson);	// Add a Sibling of

	// Modify Name screen for Siblings
		JLabel lbl_parType = new JLabel(HG0505Msgs.Text_76);	// Choose Full or Half-sibling:
		lbl_parType.setFocusTraversalKeysEnabled(false);
		panelNameRelate.add(lbl_parType, "cell 0 0");		//$NON-NLS-1$

		DefaultComboBoxModel<String> comboTypeModel = new DefaultComboBoxModel<>(siblingTypes);
		JComboBox<String> siblingType = new JComboBox<>(comboTypeModel);
		panelNameRelate.add(siblingType, "cell 0 0, gapx 10");	//$NON-NLS-1$
		panelNameRelate.setVisible(true);
	// repack screen
		pack();

/******************
 * ACTION LISTENERS
 ******************/
	// Listener for Save button add Sibling
		btn_Save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			// Check person has a name before allowing Save to proceed
				if (nameElementUpdates == 0) {
					addNameErrorMessage();
					return;
				}
			// Add the person to the database
				try {
				// If we are going to add Death or Burial events, set flag Living=N
					if (createEvent) {
						for ( int eventIndex = 0; eventIndex < updatedEvent.length; eventIndex++) {
							if (eventIndex == 2 && updatedEvent[eventIndex]) setLivingToN();	// Death
							if (eventIndex == 3 && updatedEvent[eventIndex]) setLivingToN();	// Burial
						}
					}
					if (HGlobal.DEBUG)
						for (int i = 0; i < objReqFlagData.length; i++)
							System.out.println(" Flagid: " + objReqFlagData[i][3] + " / " + objReqFlagData[i][2]); //$NON-NLS-1$ //$NON-NLS-2$
				// Do updates
					pointPersonHandler.createAddPersonGUIMemo(memoNameText.getText());
					pointPersonHandler.addNewPerson(refText.getText());
					createAllEvents(pointPersonHandler);
					pointPersonHandler.addNewSibling(siblingType.getSelectedIndex());
					if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: saved updates and leaving HG0505AddPersonSibling");		//$NON-NLS-1$

				// reload preloaded result set for T401 and Person Select
					pointOpenProject.reloadT401Persons();
					pointOpenProject.reloadT402Names();
					pointPersonHandler.resetPersonSelect();
					pointPersonHandler.resetPersonManager();

				} catch (HBException hbe) {
					System.out.println(" HG0505AddPersonSibling - Add sibling error: " + hbe.getMessage());	//$NON-NLS-1$
					if (HGlobal.DEBUG) hbe.printStackTrace();
				}
				dispose();
			}
		});
	}
}