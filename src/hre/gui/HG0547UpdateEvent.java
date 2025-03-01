package hre.gui;
/*******************************************************************************
 * Update Event - Inherit from Specification 05.47 GUI_Edit Event
 * v0.03.0031 2024-02-27 first draft (N.Tolleshaug)
 *			  2024-03-22 New handling of date, location and memo (N. Tolleshaug)
 *		      2024-05-26 New handling event update (N. Tolleshaug)
 * 			  2024-07-23 NLS conversion (D Ferguson)
 * 			  2024-07-24 Updated for use of HG0590EditDate (N Tolleshaug)
 * 			  2024-07-28 Updated for use of G0547TypeEvent (N Tolleshaug)
 * 			  2024-08-04 Updated create new location if not exist (N Tolleshaug)
 * 			  2024-11-17 Updated location style hendling (N. Tolleshaug)
 * 			  2024-11-18 Implemented location style update (N. Tolleshaug)
 * 			  2024-11-19 Updated location style handling (N. Tolleshaug)
 * 			  2024-11-23 Updated event update for witnessed events (N. Tolleshaug)
 * 			  2024-11-28 Updated event location style handling (N. Tolleshaug)
 * 			  2024-12-01 Updated location name TAB handling (N. Tolleshaug)
 * 			  2024-12-05 Final update location name TAB handling (N. Tolleshaug)
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
 * @version v0.03.0031
 * @since 2024-02-26
 */

public class HG0547UpdateEvent extends HG0547EditEvent {
	private static final long serialVersionUID = 1L;
	long null_RPID  = 1999999999999999L;
	long locationNamePID;
	boolean dateOK = false;
	boolean locationChanged = false;

	PropertyChangeListener propListener;
	HG0547UpdateEvent pointUpdateEvent = this;

	public HG0547UpdateEvent(HBProjectOpenData pointOpenProject, int eventNumber,
									int roleNumber, long eventPID, boolean addHdate, long locNamePID) throws HBException {
		super(pointOpenProject, eventNumber, roleNumber, eventPID);
		this.locationNamePID = locNamePID;
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		changedLocationNameStyle = false;
		partnerNames = pointWhereWhenHandler.getPartnerNames();

		btn_Save.setText(HG0547Msgs.Text_70);	// Update
		setTitle(HG0547Msgs.Text_71 + eventName + HG0547Msgs.Text_51 + eventPersonName);	// Update Event for

		if (HGlobal.DEBUG)
			System.out.println(" UpdateEvent: " + eventNumber + "/" + roleNumber); //$NON-NLS-1$ //$NON-NLS-2$

	// Get Name data 17.11.2024
		nameData = pointWhereWhenHandler.getNameData();

	// The following code updates the GUI from the selected event
		int eventGroup = pointPersonHandler.pointLibraryResultSet.getEventGroup(eventNumber, dataBaseIndex);
		if (eventGroup == 6)
			btn_EventType.setEnabled(false);

	// Update event memo
	// Disable memoText listener first
		memoText.getDocument().removeDocumentListener(memoTextChange);
		String memoString = pointWhereWhenHandler.readFromGUIMemo(eventPID);
		memoText.append(memoString);
		memoText.setCaretPosition(0);	// set scrollbar to top
	// and enable it again
		memoText.getDocument().addDocumentListener(memoTextChange);

	// Update content in date fields
		// Disable dateText listeners first
		dateText.getDocument().removeDocumentListener(dateTextChange);
		sortDateText.getDocument().removeDocumentListener(sortTextChange);
		// get the dates
		String [] eventDates = pointWhereWhenHandler.getDates();
		dateText.setText(" " + eventDates[0]);		//$NON-NLS-1$
		sortDateText.setText(" " + eventDates[1]);	//$NON-NLS-1$
		// and enable them again
		dateText.getDocument().addDocumentListener(dateTextChange);
		sortDateText.getDocument().addDocumentListener(sortTextChange);

	// Update the location table
	// Match the currently stored Style against the combo-box entries
	// to be able to show the correct current values
		int ii = 0;
		for (ii = 0; ii < locationNameStyles.getItemCount(); ii++) {
			locationNameStyles.setSelectedIndex(ii);
			if (nameData[0].trim().equals(locationNameStyles.getSelectedItem().toString().trim())) break;
		}

	// Make sure we haven't got to loop end with no match
		if (ii == locationNameStyles.getItemCount()) ii = 0;

	// Get data for the Location elements and values
		pointWhereWhenHandler.setNameStyleIndex(ii);

		pointWhereWhenHandler.updateManageLocationNameTable(locationNamePID);
		tableLocationData = pointWhereWhenHandler.getLocationNameTable();

		DefaultTableModel tableModel = (DefaultTableModel) tableLocation.getModel();
		tableModel.setNumRows(0);	// clear table
		tableModel.setDataVector(tableLocationData, tableBirthHeader);
	// reset table rows
		tableModel.setRowCount(tableLocationData.length);
	// reset screen size
		pack();

/**
 *  Action listener for Update date and sort
 */
	// Listener for Save button
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				// NOTE08 need code to check we have as many KEY_ASSOC roles selected as the Event requires

				if (HGlobal.writeLogs)
					HB0711Logging.logWrite("Action: accepting updates and leaving HG0547UpdateEvent");	//$NON-NLS-1$

				if (HGlobal.DEBUG)
					System.out.println(" Update event save button: " + eventNumber + "/" + roleNumber); //$NON-NLS-1$ //$NON-NLS-2$

				try {
				// Check if changes in GUI
					if (locationChanged || changedLocationNameStyle || startDateOK || sortDateOK
										|| memoEdited || changeEventType) {
					// Crate or update hdate
						if (startDateOK)
							pointWhereWhenHandler.
									createEventDates(true, eventPID, "START_HDATE_RPID", startHREDate);	//$NON-NLS-1$

						if (sortDateOK)
							pointWhereWhenHandler.
									createEventDates(true, eventPID, "SORT_HDATE_RPID", sortHREDate);		//$NON-NLS-1$

					// update memo text if edited
						if (memoEdited)
							pointWhereWhenHandler.updateFromGUIMemo(memoText.getText());

					// Update name element table T403
						if (locationChanged) {
							if (locationNamePID == null_RPID)
								locationNamePID = pointWhereWhenHandler.createLocationRecord(eventPID);
							pointWhereWhenHandler.updateLocationElementData(locationNamePID);
						}
					// Update event type and role
						if (changeEventType)
							pointWhereWhenHandler.
									changeEventType(eventPID, selectedEventNum, selectedRoleNum);
					// Update location name style
						if (changedLocationNameStyle) {
							selectedStyleIndex = locationNameStyles.getSelectedIndex();
							pointWhereWhenHandler.updateStoredNameStyle(selectedStyleIndex, locationNamePID);
						}
					} else
						System.out.println(" HG0547UpdateEvent - No updated data for event!");	//$NON-NLS-1$


				// Reload Person windows
					pointOpenProject.reloadT401Persons();
					pointOpenProject.reloadT402Names();
					pointOpenProject.getPersonHandler().resetPersonSelect();
					pointOpenProject.getPersonHandler().resetPersonManager();
					pointWhereWhenHandler.resetLocationSelect(pointOpenProject);

				// Close update event
					dispose();

				} catch (HBException hbe) {
					System.out.println("HG0547UpdateEvent - failed to update event: " + hbe.getMessage());	//$NON-NLS-1$
					JOptionPane.showMessageDialog(btn_Save, HG0547Msgs.Text_72 + hbe.getMessage(),
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
						pointWhereWhenHandler.updateManageLocationNameTable(locationNamePID);
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
		setTitle(HG0547Msgs.Text_71 + eventName + HG0547Msgs.Text_51 + eventPersonName);	// Update Event for
	}

}	// End HG0547UpdateEvent
