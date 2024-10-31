package hre.gui;
/*******************************************************************************
 * Add Person Partner - Extends GUI_AddPerson
 * *****************************************************************************
 * v0.03.0031 2023-11-15 Prepared for add partner (N Tolleshaug)
 * 			  2023-12-16 Add checkbox for add partner event (D Ferguson)
 * 			  2024-01-01 Set visible combo boxes for partner table input (N Tolleshaug)
 * 			  2024-01-09 Update of partner event (N Tolleshaug)
 * 			  2024-01-13 Update of partner event roles setting (N Tolleshaug)
 * 			  2024-01-14 Stop Save process if no Sex has been set (D Ferguson)
 * 			  2024-01-23 Make Partner Event radio-button always visible (D Ferguson)
 * 			  2024-01-27 Adjust screen layout (D Ferguson)
 * 			  2024-08-15 Add prompt for partner event data if not done (D Ferguson)
 *  		  2024-08-17 Fix for add partner event from main add partner (N Tolleshaug)
 * 			  2024-08-25 NLS conversion (D Ferguson)
 * 			  2024-10-12 Modify for HG0505AddPerson layout changes (D Ferguson)
 * 			  2024-10-22 only allow Save to proceed if Person has a Name (D Ferguson)
 ******************************************************************************/

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.nls.HG0505Msgs;

/**
 * class HG0505AddPersonParent extends HG0505AddPerson
 */
public class HG0505AddPersonPartner extends HG0505AddPerson {
	private static final long serialVersionUID = 1L;

	private ActionListener comboRole1Change = null;
	private ActionListener comboRole2Change = null;

	String addPartnerText = HG0505Msgs.Text_60;	// Add Partner
	int selectedPartTypeIndex, partRole1, partRole2;

/**
 * @param pointOpenProject
 * @param pointPersonHandler
 * @param sexIndex
 * @param ofPerson
 * @throws HBException
 */
	public HG0505AddPersonPartner(HBProjectOpenData pointOpenProject,
								 HBPersonHandler pointPersonHandler,
								 int sexIndex, String ofPerson) throws HBException {
		super(pointOpenProject, pointPersonHandler, sexIndex, false, ofPerson);

	// Set Title
		setTitle( HG0505Msgs.Text_61 + ofPerson);	// Add a Partner of

	// Following string[] loaded from T460_EVENT_DEFN where EVNT_GROUP=6
		partnerEventList = pointPersonHandler.getPartnerEventList(partnerEventGroup);
		partnerEventType = pointPersonHandler.getPartnerEventTypes();

	// Modify Name screen for Partner
		JLabel lbl_parType = new JLabel( HG0505Msgs.Text_62);		// Event Type:
		lbl_parType.setFont(lbl_parType.getFont().deriveFont(lbl_parType.getFont().getStyle() | Font.BOLD));
		lbl_parType.setVisible(true);
		panelNameRelate.add(lbl_parType, "cell 0 0");	//$NON-NLS-1$

		comboPartnerType = new JComboBox<>(partnerEventList);
	// Set the initial combobox index = 0, but try to match to against Marriage event number
		comboPartnerType.setSelectedIndex(0);
		for (int i = 0; i < partnerEventList.length; i++) {
			if (partnerEventNumber == pointPersonHandler.getPartnerEventTypes()[i]) {
					comboPartnerType.setSelectedIndex(i);
					break;
			}
		}
		panelNameRelate.add(comboPartnerType, "cell 0 0, gapx 10");	//$NON-NLS-1$
	// and also copy it to the Partner event panel
		lbl_ChosenPartnerEvent.setText(comboPartnerType.getSelectedItem().toString());

	// Load the role lists for chosen Event type
		partnerRoleList = pointPersonHandler.getRolesForEvent(partnerEventNumber, selectPartnerRoles);
		partnerRoleType = pointPersonHandler.getEventRoleTypes();

		JLabel lbl_nRole1 = new JLabel( HG0505Msgs.Text_63 + ofPerson);	// Role of
		panelNameRelate.add(lbl_nRole1, "cell 0 1");		//$NON-NLS-1$
		comboPartRole1 = new JComboBox<>(partnerRoleList);
		panelNameRelate.add(comboPartRole1, "cell 0 1, gapx 10");		//$NON-NLS-1$

		JLabel lbl_nRole2 = new JLabel( HG0505Msgs.Text_64);	// Role of New Partner
		panelNameRelate.add(lbl_nRole2, "cell 1 1");		//$NON-NLS-1$
		comboPartRole2 = new JComboBox<>(partnerRoleList);
		panelNameRelate.add(comboPartRole2, "cell 1 1, gapx 10");		//$NON-NLS-1$
		panelNameRelate.setVisible(true);

	// and copy Partner role to the Partner event panel
		lbl_ChosenPartnerRole2.setText(comboPartRole2.getSelectedItem().toString());

	// Add Partner radio-buuton to panel list to invoke Partner panel
		radio_Partner.setVisible(true);
	// repack the screen
		pack();

/******************
 * ACTION LISTENERS
 ******************/
		// Listener to copy partner date to sort date if sort date empty
		partnerDateText.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
				if (partnerSortDateText.getText().length() < 4)
					partnerSortDateText.setText(partnerDateText.getText());
				btn_Save.setEnabled(true);
            }
        });

		// Listener for selection within partner1 role combobox
		comboRole1Change = new ActionListener () {
	        public void actionPerformed(ActionEvent e) {
	        	lbl_ChosenPartnerRole1.setText(comboPartRole1.getSelectedItem().toString());
	        }
	    };
		comboPartRole1.addActionListener(comboRole1Change);

		// Listener for selection within partner2 role combobox
		comboRole2Change = new ActionListener () {
	        public void actionPerformed(ActionEvent e) {
	        	lbl_ChosenPartnerRole2.setText(comboPartRole2.getSelectedItem().toString());
	        }
	    };
		comboPartRole2.addActionListener(comboRole2Change);

		// ComboBox listener for partner event type and setting of partner role lists
		comboPartnerType.addActionListener (new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				selectedPartTypeIndex = comboPartnerType.getSelectedIndex();
				selectedPartnerType = partnerEventType[selectedPartTypeIndex];
				if (HGlobal.DEBUG)
					System.out.print(" Partner event index/type: " + selectedPartTypeIndex + "/" + selectedPartnerType + " - /");  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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

					comboPartRole1.setSelectedIndex(0);
					comboPartRole2.setSelectedIndex(0);
					lbl_ChosenPartnerEvent.setText(comboPartnerType.getSelectedItem().toString());

				} catch (HBException hbe) {
					hbe.printStackTrace();
					System.out.println(" Add Person Partner role reset error: " + hbe.getMessage());	//$NON-NLS-1$
				}
			}
		});

		// Listener for Save button add Partner
		btn_Save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// Check person has a name before allowing Save to proceed
				if (nameElementUpdates == 0) {
					addNameErrorMessage();
					return;
				}
				// if no sex has been set, throw warning msg and halt Save operation
				for (int i = 0; i < tableFlags.getRowCount(); i++) {
					// First, find flagIdent = 1 (Birth sex)
					if ((Integer) objReqFlagData[i][3] == 1) {				// this ID is for Birth Sex
						String allValues = (String) objReqFlagData[i][4];	// get all birthSex values list
						String[] values = allValues.split(",");				//$NON-NLS-1$
						String defaultSexFlag = ((String)values[0].trim());	// find the default sex flag
						String currentSexFlag = ((String) flagModel.getValueAt(i, 1)).trim(); // find current setting
						if (currentSexFlag.equals(defaultSexFlag)) {
							JOptionPane.showMessageDialog(btn_Save,  HG0505Msgs.Text_65,	// Please set the Birth Sex before Saving
									addPartnerText, JOptionPane.WARNING_MESSAGE);
							return;
						}
					}
				}

				// If partner event data has not been set, issue prompt for user to do so
				if (!updatedEvent[4]) {
					if (JOptionPane.showConfirmDialog(btn_Save,
							 HG0505Msgs.Text_66,	// No event data entered. \nAdd Event data for this Partner?
							addPartnerText,
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							// Yes option: show Partner event panel and let user add event data
								radio_Partner.setSelected(true);
								radio_Partner.doClick();
								return;
							}
				}

				// Perform DB updates for all changes not yet saved
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: accepting updates and leaving HG0505AddPersonPartner");	//$NON-NLS-1$
				try {
				// Do updates
					pointPersonHandler.createAddPersonGUIMemo(memoNameText.getText());
					pointPersonHandler.addNewPerson(refText.getText());
					selectedPartTypeIndex = comboPartnerType.getSelectedIndex();
					selectedPartnerType = pointPersonHandler.getPartnerEventTypes()[selectedPartTypeIndex];
					partRole1 = partnerRoleType[comboPartRole1.getSelectedIndex()];
					partRole2 = partnerRoleType[comboPartRole2.getSelectedIndex()];
					newPartnerPID = pointPersonHandler.addNewPartner(selectedPartnerType, partRole1, partRole2);
					createAllEvents(pointPersonHandler);

				// reload preloaded result set for T401 and Person Select
					pointOpenProject.reloadT401Persons();
					pointOpenProject.reloadT402Names();
					pointPersonHandler.resetPersonSelect();
					pointPersonHandler.resetPersonManager();

				} catch (HBException hbe) {
					System.out.println(" HG0505AddPersonPartner - Add partner error: " + hbe.getMessage());	//$NON-NLS-1$
					if (HGlobal.DEBUG) hbe.printStackTrace();
				}
				dispose();
			}
		});
	}
}
