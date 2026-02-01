package hre.gui;
/*******************************************************************************
 * Add Person Parent - Extends GUI_AddPerson
 * *****************************************************************************
 * v0.03.0031 2023-11-15 Prepared for add parent (N Tolleshaug)
 * 			  2023-12-08 Prepared for add event and memo (N Tolleshaug)
 * 			  2024-08-25 NLS conversion (D Ferguson)
 * 			  2024-10-12 Modify for HG0505AddPerson layout changes (D Ferguson)
 * 			  2024-10-22 only allow Save to proceed if Person has a Name (D Ferguson)
 * v0.04.0032 2024-12-26 Only turn Living flag to N if Death/Burial saved ( D Ferguson)
 * 			  2026-01-06 Log all catch block and DEBUG msgs (D Ferguson)
 *******************************************************************************/

import java.awt.Font;
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
 * class HG0505AddPersonParent extends HG0505AddPerson
 * @author D. Ferguson
 * @version v0.03.0032
 * @since 2023-11-15
 */

public class HG0505AddPersonParent extends HG0505AddPerson {
	private static final long serialVersionUID = 1L;

	// Following String[] loaded from T460_EVENT_DEFN where EVNT_GROUP=2 (father) or 3 (mother)
	String[] parentType;
	final static int fatherEventGroup = 2;
	final static int motherEventGroup = 3;

/**
 * @param pointOpenProject
 * @param pointPersonHandler
 * @param sexIndex
 * @param ofPerson
 * @throws HBException
 */
	public HG0505AddPersonParent(HBProjectOpenData pointOpenProject,
								 HBPersonHandler pointPersonHandler,
								 int sexIndex, String ofPerson) throws HBException {
		super(pointOpenProject, pointPersonHandler, sexIndex, false,  ofPerson);

		// Now set GUI Title and get appropriate parent roles
		if (sexIndex == 1) {
			setTitle(HG0505Msgs.Text_50 + ofPerson);	// Add a Mother of
			parentType = pointPersonHandler.getRolesForParent(motherEventGroup);
		}
		else if (sexIndex == 2) {
			setTitle(HG0505Msgs.Text_51 + ofPerson);	// Add a Father of
			parentType = pointPersonHandler.getRolesForParent(fatherEventGroup);
		}
		else setTitle(HG0505Msgs.Text_52 + ofPerson);	// Add a Parent of

		JLabel lbl_parType = new JLabel(HG0505Msgs.Text_53);	// Parent Relationship:
		lbl_parType.setFont(lbl_parType.getFont().deriveFont(lbl_parType.getFont().getStyle() | Font.BOLD));
		lbl_parType.setFocusTraversalKeysEnabled(false);
		panelNameRelate.add(lbl_parType, "cell 0 0");	//$NON-NLS-1$
		DefaultComboBoxModel<String> comboNameModel = new DefaultComboBoxModel<>(parentType);
		JComboBox<String> parentType = new JComboBox<>(comboNameModel);
		parentType.setSelectedIndex(1); 		// set to parent-bio
		panelNameRelate.add(parentType, "cell 0 0, gapx 10");	//$NON-NLS-1$
		panelNameRelate.setVisible(true);
	// repack screen
		pack();

	// Listener for Save button add Parent
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
					if (HGlobal.DEBUG && HGlobal.writeLogs)
						for (int i = 0; i < objReqFlagData.length; i++)
							HB0711Logging.logWrite("Status: in HG0530AddParent Flagid: " + objReqFlagData[i][3] + " / " + objReqFlagData[i][2]); //$NON-NLS-1$ //$NON-NLS-2$
				// Do updates
					pointPersonHandler.createAddPersonGUIMemo(memoNameText.getText());
					pointPersonHandler.addNewPerson(refText.getText());
					pointPersonHandler.setParentRole(parentType.getSelectedIndex());
					createAllEvents(pointPersonHandler);
					pointPersonHandler.addNewParent();
				// reload preloaded result set for T401 and Person Select
					pointOpenProject.reloadT401Persons();
					pointOpenProject.reloadT402Names();
					pointPersonHandler.resetPersonSelect();
					pointPersonHandler.resetPersonManager();

				} catch (HBException hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0530AddParent Save error " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
				if (HGlobal.writeLogs)
					HB0711Logging.logWrite("Action: attempted Save and leaving HG0505AddPersonParent");	//$NON-NLS-1$
				dispose();
			}
		});
	}
}