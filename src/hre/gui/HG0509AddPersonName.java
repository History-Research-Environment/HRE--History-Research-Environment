package hre.gui;
/*******************************************************************************
 * Add Person Name
 * v0.03.0031 2024-06-14 Original draft - split from ManagePersonName (N Tolleshaug)
 * 			  2024-06-27 Activated add name (N Tolleshaug)
 * 			  2024-06-30 Reset Person manager (N Tolleshaug)
 * 			  2024-08-16 NLS conversion (D Ferguson)
 * 			  2024-10-13 Edited and removed additinal{} (N Tolleshaug)
 ******************************************************************************
 * Notes on functions not yet enabled
 * NOTE02 load/edit/save/move of Citation data
 *****************************************************************************/

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.WindowConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.nls.HG0509Msgs;

/**
 * Add PersonName
 * @author N Tolleshaug
 * @version v0.03.0031
 * @since 2024-06-14
 */

public class HG0509AddPersonName extends HG0509ManagePersonName {
	private static final long serialVersionUID = 1L;
	long personTablePID;
	HBPersonHandler pointPersonHandler;
	public HG0509AddPersonName(HBPersonHandler pointPersonHandler,
								HBProjectOpenData pointOpenProject,
								long personTablePID) throws HBException {
		super(pointPersonHandler, pointOpenProject, personTablePID);
		this.personTablePID = personTablePID;
		this.pointPersonHandler = pointPersonHandler;
    	this.setResizable(true);

    // Setup screenID and helpName for 50600 as Help for this dialog is part of
    // managePerson Help and F1 has to select that (also this screen does not save to T302)
    	String screenID = "50600";			//$NON-NLS-1$
    	windowID = screenID;
		helpName = "manageperson";	//$NON-NLS-1$

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: entering HG0509AddPersonName");}	//$NON-NLS-1$
	    setTitle(HG0509Msgs.Text_31);		// Add a new Person Name

	    clearPersonTableData();

/********************
 * ACTION LISTENERS
 *******************/
		// Listener for Save button
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
			// Action each save function depending on what has been changed
				long personNameTablePID = 0;
				try {
					personNameTablePID = pointPersonHandler.createPersonNameRecord(personTablePID, nameEventType);
					if (styleChanged) {
						// Save the new name style in DB
							int selectIndex = comboStyles.getSelectedIndex();
								pointPersonHandler.updateStoredNameStyle(selectIndex, personNameTablePID); // New code??
							// Update all names table in manage person
								pointPersonHandler.updateAllNameTable(); // New code??
								pointPersonHandler.managePersonScreen.resetAllNametable(pointOpenProject);

						// Save new name style on screen
							lbl_Style.setText(HG0509Msgs.Text_13);		// Saved Name Style:
							lbl_CurrStyle.setText(String.valueOf(comboStyles.getSelectedItem()));
							styleChanged = false;
					}

					if (startDateOK)					 
						pointPersonHandler.createNameDates(false, personNameTablePID, "START_HDATE_RPID", startHREDate);   //$NON-NLS-1$
					
					if (endDateOK)	 
						pointPersonHandler.createNameDates(false, personNameTablePID, "END_HDATE_RPID", endHREDate);			//$NON-NLS-1$
					
					if (nameChanged) {
							new TableModelEvent(tablePerson.getModel());
					// Update best name in T401
						if (setPrimary) {
							pointPersonHandler.updatePersonBestName(personNameTablePID);
							if (btn_Primary.isSelected()) {
								btn_Primary.setEnabled(false);
							}
						// ResultSet T401 must be reloaded after update of table
							pointOpenProject.reloadT401Persons();
							setPrimary = false;
						}

					// Update all names table in manage person
						pointPersonHandler.updateAllNameTable();
						pointPersonHandler.managePersonScreen.resetAllNametable(pointOpenProject);
					// Reset personSelector (if running)
						pointOpenProject.reloadT401Persons();
						pointOpenProject.reloadT402Names();
						pointOpenProject.getPersonHandler().resetPersonManager();
						pointOpenProject.getPersonHandler().resetPersonSelect();
						nameChanged = false;
					}

					if (citationChanged) {
						// Save the new citation in the DB - NOTE02
						citationChanged = false;
					}

					if (memoChanged) {
						pointPersonHandler.updateSelectGUIMemo(memoNameText.getText(),
								personNameTablePID,
								pointPersonHandler.personNameTable);
						memoChanged = false;
					}
				// Reset Person manager
					pointOpenProject.reloadT402Names();
					pointPersonHandler.resetPersonManager();

				} catch (HBException hbe) {
					System.out.println("HG0509AddPersonName - Save error: " + hbe.getMessage());	//$NON-NLS-1$
					hbe.printStackTrace();
					errorJOptionMessage("HG0509AddPersonName", HG0509Msgs.Text_32 + hbe.getMessage());	//$NON-NLS-1$	// Save Error:
				}

				btn_Save.setEnabled(false);
			}
		});
	}	// End HG0509AddPersonName constructor

/**
 * clearPersonTableData() - clear name data in JTable tablePerson for add new name
 */
	private void clearPersonTableData() {
		for (int i = 0; i < tablePersData.length; i++) 
			tablePersData[i][1] = "";	//$NON-NLS-1$
		
		DefaultTableModel persModel = (DefaultTableModel) tablePerson.getModel();
		persModel.setDataVector(tablePersData, persHeaderData);
		pack();
	}
}	// End HG0509AddPersonName
