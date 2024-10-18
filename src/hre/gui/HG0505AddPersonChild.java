package hre.gui;
/*******************************************************************************
 * Add Person Child - Extends GUI_AddPerson
 * *****************************************************************************
 * v0.03.0031 2023-11-15 Prepared for add child (N Tolleshaug)
 * 			  2023-12-24 Implemented add child (N Tolleshaug)
 * 			  2024-08-25 NLS conversion (D Ferguson)
 * 			  2024-10-12 Modify for HG0505AddPerson layout changes (D Ferguson)
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
 * class HG0505AddPersonChild extends HG0505AddPerson
 */
public class HG0505AddPersonChild extends HG0505AddPerson {
	private static final long serialVersionUID = 1L;

	// Following String[] loaded from T460_EVENT_DEFN where EVNT_GROUP=2 (father) or 3 (mother)
	String[] childRelationType;
	final static int fatherEventGroup = 2;
	final static int motherEventGroup = 3;

/**
 * @param pointOpenProject
 * @param pointPersonHandler
 * @param sexIndex
 * @param ofPerson
 * @throws HBException
 */
	public HG0505AddPersonChild(HBProjectOpenData pointOpenProject, HBPersonHandler pointPersonHandler,
			int sexIndex, String ofPerson) throws HBException {
		super(pointOpenProject, pointPersonHandler, sexIndex, false, ofPerson);

	// Setup screen title
		if (sexIndex == 1) {
			setTitle(HG0505Msgs.Text_40 + ofPerson);	// Add a Daughter of
		}
		else if (sexIndex == 2) {
			setTitle(HG0505Msgs.Text_41 + ofPerson);	// Add a Son of
		}
		else setTitle(HG0505Msgs.Text_42 + ofPerson);	// Add a Child of

		int parentSex = pointPersonHandler.getPersonSex(pointOpenProject.getSelectedPersonPID());
		if (parentSex == 1) childRelationType = pointPersonHandler.getRolesForParent(motherEventGroup);
		else if (parentSex == 2) childRelationType = pointPersonHandler.getRolesForParent(fatherEventGroup);
		else {
			pointPersonHandler.errorJOptionMessage(HG0505Msgs.Text_43, HG0505Msgs.Text_44);	// Add Child	//   Set selected parent sex first
			if (HGlobal.DEBUG)
				System.out.println(" Sex for parent not known - sexindex: " + parentSex);	//$NON-NLS-1$
			dispose();
			return;
		}

	// Modify Name screen, Relate sub-panel, for Child
		JLabel lbl_parType = new JLabel(HG0505Msgs.Text_45);	// Child relationship:
		lbl_parType.setFocusTraversalKeysEnabled(false);
		panelNameRelate.add(lbl_parType, "cell 0 0");		//$NON-NLS-1$

		DefaultComboBoxModel<String> comboTypeModel = new DefaultComboBoxModel<>(childRelationType);
		JComboBox<String> childType = new JComboBox<>(comboTypeModel);
		childType.setSelectedIndex(1); 		// set to child-bio
		panelNameRelate.add(childType, "cell 0 0, gapx 10");	//$NON-NLS-1$
		panelNameRelate.setVisible(true);
	// repack screen
		pack();

/******************
 * ACTION LISTENERS
 ******************/
// Listener for Save button add Child
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// Perform DB updates for all changes not yet saved
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: accepting updates and leaving HG0505AddPersonChild");	//$NON-NLS-1$
				try {
				// Code for check update of database
					if (HGlobal.DEBUG)
						for (Object[] element : objReqFlagData)
							System.out.println(" Flagid: " + element[3] + " / " + element[2]);	//$NON-NLS-1$ //$NON-NLS-2$
				// Check if person is given name
					if (nameElementUpdates > 0) {
						pointPersonHandler.createAddPersonGUIMemo(memoNameText.getText());
						pointPersonHandler.addNewPerson(refText.getText());
						pointPersonHandler.setParentRole(childType.getSelectedIndex());
					} else createEvent = false;
					createAllEvents(pointPersonHandler);
					if (nameElementUpdates > 0) pointPersonHandler.addNewChild();

				// reload preloaded result set for T401 and Person Select
					pointOpenProject.reloadT401Persons();
					pointOpenProject.reloadT402Names();
					pointPersonHandler.resetPersonSelect();
					pointPersonHandler.resetPersonManager();

				} catch (HBException hbe) {
					System.out.println(" HG0505AddPerson - Add child error: " + hbe.getMessage());	//$NON-NLS-1$
					if (HGlobal.DEBUG) hbe.printStackTrace();
				}
				dispose();
			}
		});
	} // End constructor

} // End class HG0505AddPersonChild