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
 *******************************************************************************
 * NOTES for incomplete functionality:
 * NOTE08 need to check that Min# of Key_Assoc have been selected before saving
 ******************************************************************************/

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

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
	boolean locationChanged = false;
	TableModelListener locnListener;
	boolean dateOK = false;
	
	HG0547UpdateEvent pointUpdateEvent = this;
	
	public HG0547UpdateEvent(HBProjectOpenData pointOpenProject, int eventNumber, 
									int roleNumber, long eventPID, boolean addHdate, long locationPID) throws HBException {
		super(pointOpenProject, eventNumber, roleNumber, eventPID);
		this.locationNamePID = locationPID;
		int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		partnerNames = pointWhereWhenHandler.getPartnerNames();
		btn_Save.setText(HG0547Msgs.Text_70);	// Update
		setTitle(HG0547Msgs.Text_71 + eventName + HG0547Msgs.Text_51 + eventPersonName);	// Update the		// Event for
		//setTitle(HG0547Msgs.Text_71 + eventName + HG0547Msgs.Text_51 + partnerNames);
		if (HGlobal.DEBUG) System.out.println(" UpdateEvent: " + eventNumber + "/" + roleNumber); //$NON-NLS-1$ //$NON-NLS-2$
		
	// The following code updates the GUI from the selected event
		int eventGroup = pointPersonHandler.pointLibraryResultSet.getEventGroup(eventNumber, dataBaseIndex);
		if (eventGroup == 6) 
			btn_EventType.setEnabled(false);
		
	// Update event memo
	// Disable memoText listener first
		memoText.getDocument().removeDocumentListener(memoTextChange);
		String memoString = pointWhereWhenHandler.readFromGUIMemo(eventPID);
		memoText.append(memoString);
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
		locationNameStyles.setSelectedIndex(pointWhereWhenHandler.getDefaultLocationStyleIndex());
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

				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: accepting updates and leaving HG0547UpdateEvent");	//$NON-NLS-1$
				if (HGlobal.DEBUG) 
					System.out.println(" Update event save button: " + eventNumber + "/" + roleNumber); //$NON-NLS-1$ //$NON-NLS-2$
				
				try {					
				// Check if date or location or memo > 0	
					if (locationChanged || startDateOK || sortDateOK || memoEdited || changeEventType) {
					// Crate or update hdate	
						if (startDateOK) pointWhereWhenHandler.
								createEventDates(true, eventPID, "START_HDATE_RPID", startHREDate);	//$NON-NLS-1$
						if (sortDateOK) pointWhereWhenHandler.
								createEventDates(true, eventPID, "SORT_HDATE_RPID", sortHREDate);		//$NON-NLS-1$
					// update memo text if edited
						if (memoEdited)  pointWhereWhenHandler.updateFromGUIMemo(memoText.getText());
					// Update name element table T403
						if (locationChanged) {
							if (locationNamePID == null_RPID) 
								locationNamePID = pointWhereWhenHandler.createLocationRecord(eventPID);
							pointWhereWhenHandler.updateLocationElementData(locationNamePID);
						}
					// Update event type and role
						if (changeEventType) pointWhereWhenHandler.
								changeEventType(eventPID, selectedEventNum, selectedRoleNum);	
					} else System.out.println(" HG0547UpdateEvent - No updated data for event!");	//$NON-NLS-1$	
					
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
					if (HGlobal.DEBUG) hbe.printStackTrace();
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
					// reset screen size
						pack();
					} catch (HBException hbe) {
						hbe.printStackTrace();
						System.out.println(" HG0547UpdateEvent - Action loc style selection: " + hbe.getMessage());	//$NON-NLS-1$	
						}
					// Re-enable tableLocation listener
					tableLocation.getModel().addTableModelListener(locnListener);
				}
			}
		});
		
		// Listener for changes made in tableLocation
		locnListener = new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				TableModel model = (TableModel)e.getSource();
				if (tableLocation.getSelectedRow() > -1) {
					String nameElementData = (String) model.getValueAt(tableLocation.getSelectedRow(), 1);
					String element = (String) model.getValueAt(tableLocation.getSelectedRow(), 0);
					if (HGlobal.DEBUG) {
							System.out.println("HG0547UpdateEvent - location table changed: "+ tableLocation.getSelectedRow() //$NON-NLS-1$
									 + " Element: " + element + "/" + nameElementData); 	//$NON-NLS-1$ //$NON-NLS-2$
					}
					pointWhereWhenHandler.addToNameChangeList(tableLocation.getSelectedRow(), nameElementData);
					locationChanged = true;
					btn_Save.setEnabled(true);
				}
			}
		};
		tableLocation.getModel().addTableModelListener(locnListener);
		
	} // End HG0547UpdateEvent constructor
}
