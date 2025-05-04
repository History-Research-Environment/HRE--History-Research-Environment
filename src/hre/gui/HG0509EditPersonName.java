package hre.gui;
/*******************************************************************************
 * Edit Person Name
 * v0.03.0031 2024-06-14 Original draft - split from ManagePersonName (D Ferguson)
 * 			  2024-06-27 Activated edit name (N Tolleshaug)
 * 			  2024-08-16 NLS conversion (D Ferguson)
 * 			  2024-10-05 Updated for reset PM/PS (N Tolleshaug)
 * 			  2024-11-29 Fixed reset PM/PS (N Tolleshaug)
 * 			  2024-12-08 Updated name styles and event type handling (N Tolleshaug)
 ******************************************************************************
 * Notes on functions not yet enabled
 * NOTE02 load/edit/save/move of Citation data
 *****************************************************************************/

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.WindowConstants;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.bila.HBWhereWhenHandler;
import hre.nls.HG0509Msgs;

/**
 * Edit PersonName
 * @author D Ferguson
 * @version v0.03.0031
 * @since 2024-06-14
 */

public class HG0509EditPersonName extends HG0509ManagePersonName {
	private static final long serialVersionUID = 001L;

	HBWhereWhenHandler pointWhereWhenHandler;

/**
 * Add PersonName
 * @author N Tolleshaug
 * @version v0.03.0031
 * @since 2024-06-14
 */
	public HG0509EditPersonName(HBPersonHandler pointPersonHandler,
			 						HBProjectOpenData pointOpenProject,
			 						long personNameTablePID) throws HBException {
		super(pointPersonHandler, pointOpenProject, personNameTablePID);
		pointWhereWhenHandler = pointOpenProject.getWhereWhenHandler();
		if (HGlobal.DEBUG)
			System.out.println("HG0509EditPersonName initated");	//$NON-NLS-1$

    	this.setResizable(true);
    // Setup screenID and helpName for 50600 as Help for this dialog is part of
    // managePerson Help and F1 has to select that (also this screen does not save to T302)
    	String screenID = "50600";			//$NON-NLS-1$
    	windowID = screenID;
		helpName = "manageperson";	//$NON-NLS-1$

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: entering HG0509EditPersonName");}	//$NON-NLS-1$
	    setTitle(HG0509Msgs.Text_15);		// Edit Person Name

	 // Get Name style and start/end dates
	    nameData = pointPersonHandler.getNameData();

		// Test the person data against the Name style used - if there are elements
		// not showing due to the Name Style ignoring them, show the 'Show Hidden' button
		if (pointPersonHandler.detectHiddenElements()) {
 						btn_Hidden.setVisible(true);
 						btn_Hidden.setEnabled(true);
  		} else {
			btn_Hidden.setVisible(false);
		}

	// Update event memo
	// Disable memoText listener first
		memoNameText.getDocument().removeDocumentListener(textListen);
		String memoString = pointPersonHandler.readSelectGUIMemo(personNameTablePID, pointPersonHandler.personNameTable);
		memoNameText.append(memoString);
	// and enable it again
		memoNameText.getDocument().addDocumentListener(textListen);

/********************
 * ACTION LISTENERS
 *******************/
		// Listener for Save button
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
			// Action each save function depending on what has been changed
			try {
				if (styleChanged) {
					// Save the new name style in DB
						int selectIndex = comboStyles.getSelectedIndex();
						pointPersonHandler.updateStoredNameStyle(selectIndex, personNameTablePID);
					// Update all names table in manage person
						pointPersonHandler.updateAllNameTable();
						pointPersonHandler.managePersonScreen.resetAllNametable(pointOpenProject);

					// Save new name style on screen
						lbl_Style.setText(HG0509Msgs.Text_13);		// Saved Name Style:
						lbl_CurrStyle.setText(String.valueOf(comboStyles.getSelectedItem()));
						styleChanged = false;
				}

				if (startDateOK)
					pointPersonHandler.createNameDates(true, personNameTablePID, "START_HDATE_RPID", startHREDate);	//$NON-NLS-1$

				if (endDateOK)
					pointPersonHandler.createNameDates(true, personNameTablePID, "END_HDATE_RPID", endHREDate);			//$NON-NLS-1$

				if (nameChanged || eventTypeChanged || memoChanged || citationOrderChanged) {
					if (nameChanged || eventTypeChanged) {
						// Update best name in T401
							if (setPrimary) {
								pointPersonHandler.setNameEventType(nameEventType);
								pointPersonHandler.updatePersonBestName(personNameTablePID);
								if (btn_Primary.isSelected()) {
									btn_Primary.setEnabled(false);
								}
							// ResultSet T401 must be reloaded after update of table
								pointOpenProject.reloadT401Persons();
								pointOpenProject.reloadT402Names();
								setPrimary = false;
							}
						// Update name element table T403
							pointPersonHandler.updateElementData(nameEventType);

						// Update all names table in manage person
							pointPersonHandler.updateAllNameTable();
							pointPersonHandler.managePersonScreen.resetAllNametable(pointOpenProject);
							nameChanged = false;
							eventTypeChanged = false;
					}

					if (memoChanged) {
						pointPersonHandler.updateSelectGUIMemo(memoNameText.getText(),
																personNameTablePID,
																pointPersonHandler.personNameTable);
						memoChanged = false;
					}

					// Redo citation sequence, but only if more than 1 citation left
					if (citationOrderChanged && objNameCiteData.length > 1) {
						pointCitationSourceHandler.updateCiteGUIseq(personNameTablePID, "T402", objNameCiteData);	//$NON-NLS-1$
						citationOrderChanged = false;
					}
				}

			// Reset personSelector and PersonManager(if running)
				pointOpenProject.reloadT401Persons();
				pointOpenProject.reloadT402Names();
				pointPersonHandler.resetPersonManager();
				pointPersonHandler.resetPersonSelect();

			} catch (HBException hbe) {
				System.out.println("HG0509EditPersonName - Save error: " + hbe.getMessage());	//$NON-NLS-1$
				hbe.printStackTrace();
				errorJOptionMessage("HG0509EditPersonName", HG0509Msgs.Text_26 + hbe.getMessage());	//$NON-NLS-1$  // Save Error:
			}
			btn_Save.setEnabled(false);
			}
		});

	}	// End HG0509EditPersonName constructor

}  // End of HG0509EditPersonName