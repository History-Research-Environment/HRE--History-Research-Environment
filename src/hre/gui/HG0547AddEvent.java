package hre.gui;
/*******************************************************************************
 * Add Event - Inherit from Specification 05.47 GUI_Add Event
 * v0.03.0032 2025-04-26 first draft (N.Tolleshaug)
 * 			  2025-04-27 Updated to create event record when initiated (N.Tolleshaug)
 *			  2025-05-08 Updated for save data for new event (N.Tolleshaug)
 *			  2025-05-09 - Reload associate and citation event add/edit(N.Tolleshaug)
 *			  2025-11-01 - Modified name for createLocationRecord (N.Tolleshaug)
 *******************************************************************************
 * NOTES for incomplete functionality:
 * NOTE08 need to check that Min# of Key_Assoc have been selected before saving
 ******************************************************************************/

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBProjectOpenData;
import hre.nls.HG0547Msgs;

/**
 * Update Event
 * @author N.Tolleshaug
 * @version v0.03.0032
 * @since 2025-04-26
 */

public class HG0547AddEvent extends HG0547EditEvent {
	private static final long serialVersionUID = 1L;
	private static long null_RPID = 1999999999999999L;
	long locationNamePID = null_RPID;
	boolean dateOK = false;
	boolean locationChanged = false;

	PropertyChangeListener propListener;
	HG0547AddEvent pointUpdateEvent = this;

	public HG0547AddEvent(HBProjectOpenData pointOpenProject, int eventNumber,
									int roleNumber, String sexCode) throws HBException {
		super(pointOpenProject, eventNumber, roleNumber, null_RPID, sexCode);

		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		changedLocationNameStyle = false;
		partnerNames = pointWhereWhenHandler.getPartnerNames();

		btn_Save.setText(HG0547Msgs.Text_27);	// Lagre
		setTitle(HG0547Msgs.Text_45 + eventName + HG0547Msgs.Text_51 + eventPersonName);	//  Add  // Update Event for

		if (HGlobal.DEBUG)
			System.out.println(" UpdateEvent: " + eventNumber + "/" + roleNumber); //$NON-NLS-1$ //$NON-NLS-2$

	// Get Name data 17.11.2024
		nameData = pointWhereWhenHandler.getNameData();

	// The following code updates the GUI from the selected event
		int eventGroup = pointPersonHandler.pointLibraryResultSet.getEventGroup(eventNumber, dataBaseIndex);
		if (eventGroup == 6)
			btn_EventType.setEnabled(false);

	// Enable event memo
		memoText.getDocument().addDocumentListener(memoTextChange);
		dateText.setText(" ");		//$NON-NLS-1$
		sortDateText.setText(" ");	//$NON-NLS-1$
		// Enable date text
		dateText.getDocument().addDocumentListener(dateTextChange);
		sortDateText.getDocument().addDocumentListener(sortTextChange);

	// Set defaul stored Style index
		pointWhereWhenHandler.setNameStyleIndex(0);

	// Update the location table

		pointWhereWhenHandler.updateManageLocationNameTable(locationNamePID, false);
		tableLocationData = pointWhereWhenHandler.getLocationNameTable();

		DefaultTableModel tableModel = (DefaultTableModel) tableLocation.getModel();
		tableModel.setNumRows(0);	// clear table
		tableModel.setDataVector(tableLocationData, tableBirthHeader);

	// reset table rows
		tableModel.setRowCount(tableLocationData.length);

	// Create new event
		eventPID = pointWhereWhenHandler.createNewEvent(selectedEventNum, selectedRoleNum);

	// reset screen size
		pack();

/**
 * Listener for Save button
 */
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// NOTE08 need code to check we have as many KEY_ASSOC roles selected as the Event requires
				if (HGlobal.writeLogs)
					HB0711Logging.logWrite("Action: accepting updates and leaving HG0547EditEvent");		//$NON-NLS-1$
				if (HGlobal.DEBUG)
					System.out.println(" ADD event save button: " + selectedEventNum + "/" + selectedRoleNum); //$NON-NLS-1$ //$NON-NLS-2$
				try {
				// Check if data updated
					if (locationElementUpdate || startDateOK || sortDateOK || memoEdited || changedLocationNameStyle) {

					// If eventGroup is Death or Burial, we need to set Living Flag = N
						if (eventGroup == 5 || eventGroup == 9)	{	// death, burial groups
							// Get all of the person's current flag data. Table columns we need are:
							// column 3 - FLAG_ID; 8 - FLAG_VALUES
							objAllFlagData = pointPersonHandler.getManagedFlagTable();
							// For flagID=2 (Living) we want to get the flag values
							String flagValues =" ";		//$NON-NLS-1$
							for (Object[] element : objAllFlagData) {
								if ((int) (element[3]) ==2)
									flagValues = (String) element[8];	// get the Flag values of Living flag
							}
							String[] values = flagValues.split(",");	//$NON-NLS-1$
							// Save the 2nd value (the 'N' value)
							pointPersonHandler.setPersonFlagSetting(2, values[1]);
						}

					//if update memo text
						if (memoEdited)
							pointWhereWhenHandler.createFromGUIMemo(memoText.getText());

					// Create a new set of HDATE records
						if (startDateOK)
							pointWhereWhenHandler.createEventDates(false, eventPID, "START_HDATE_RPID", startHREDate);  //$NON-NLS-1$

						if (sortDateOK)
							pointWhereWhenHandler.createEventDates(false, eventPID, "SORT_HDATE_RPID", sortHREDate);		//$NON-NLS-1$

					// Update name element table T403
						if (locationChanged) {
							if (locationNamePID == null_RPID)
								locationNamePID = pointWhereWhenHandler.createLocationAndUpdateEvent(eventPID);
							pointWhereWhenHandler.updateLocationElementData(locationNamePID);
						}

					// Update location name style
						if (changedLocationNameStyle) {
							selectedStyleIndex = locationNameStyles.getSelectedIndex();
							locationNamePID = pointWhereWhenHandler.getLocationNameRecordPID();
							pointWhereWhenHandler.updateStoredNameStyle(selectedStyleIndex, locationNamePID);
						}

					} else if (HGlobal.DEBUG)
								System.out.println(" HG0547EditEvent - No edited data for event!"); //$NON-NLS-1$

				// Reload Person windows
						pointOpenProject.reloadT401Persons();
						pointOpenProject.reloadT402Names();
						pointOpenProject.getPersonHandler().resetPersonSelect();
						pointOpenProject.getPersonHandler().resetPersonManager();
						pointEditEvent.dispose();


				} catch (HBException hbe) {
					System.out.println("HG0547AddEvent - Failed to add event: " + hbe.getMessage()); //$NON-NLS-1$
					try {
						pointWhereWhenHandler.deleteSingleEvent(eventPID);
					} catch (HBException e) {
						System.out.println("HG0547AddEvent - Delete events failed: " + hbe.getMessage()); //$NON-NLS-1$
						e.printStackTrace();
					}
					JOptionPane.showMessageDialog(btn_Save, HG0547Msgs.Text_39 + hbe.getMessage(),
							HG0547Msgs.Text_40, JOptionPane.ERROR_MESSAGE);
					if (HGlobal.DEBUG)
						hbe.printStackTrace();
				}
			}
		});

		// On selection of a locn style update the Location table
		locationNameStyles.addActionListener (new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (!(locationNameStyles.getSelectedIndex() == -1)) {
					int index = locationNameStyles.getSelectedIndex();
					DefaultTableModel locationTableModel = (DefaultTableModel) tableLocation.getModel();
				// Temporarily disable tablelocation listener while Style changes
					locationTableModel.removeTableModelListener(eventLocationListener);
					tableModel.setNumRows(0);	// clear table
					try {
						pointWhereWhenHandler.setNameStyleIndex(index);
						pointWhereWhenHandler.updateManageLocationNameTable(locationNamePID, false);
						tableLocationData = pointWhereWhenHandler.getLocationNameTable();
						tableModel.setDataVector(tableLocationData, tableBirthHeader);
					// reset table rows
						tableModel.setRowCount(tableLocationData.length);

					// Mark location name style change
						changedLocationNameStyle = true;
						btn_Save.setEnabled(true);
					// reset screen size
						pack();
					} catch (HBException hbe) {
						hbe.printStackTrace();
						System.out.println(" HG0547UpdateEvent - Action loc style selection: " + hbe.getMessage());	//$NON-NLS-1$
					}
					// Re-enable tableLocation listener
					locationTableModel.addTableModelListener(eventLocationListener);
				}
			}
		});


      // TableModelListener to detect location data changes
	    tableLocation.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent tme) {
                if (tme.getType() == TableModelEvent.UPDATE) {
                    int row = tme.getFirstRow();
                    if (row > -1) {
	                    String nameElementData = (String) tableLocation.getValueAt(row, 1);
						String element = (String) tableLocation.getValueAt(row, 0);
						if (HGlobal.DEBUG)
								System.out.println(" HG0547UpdateEvent - location table changed: " + row //$NON-NLS-1$
										 + " Element: " + element + "/" + nameElementData); 	//$NON-NLS-1$ //$NON-NLS-2$
						if (nameElementData != null) {
							pointWhereWhenHandler.addToNameChangeList(row, nameElementData);
							locationChanged = true;
							btn_Save.setEnabled(true);
						}
                    }
                }
            }
        });
	} // End HG0547UpdateEvent constructor

/**
 * setUpdateEventTitle(String eventPersonName)
 * @param eventPersonName
 */
	public void setUpdateEventTitle(String eventPersonName) {
		setTitle(HG0547Msgs.Text_45  + eventName + HG0547Msgs.Text_51 + eventPersonName);	//  Add   // Update Event for
	}

}	// End HG0547AddEvent
