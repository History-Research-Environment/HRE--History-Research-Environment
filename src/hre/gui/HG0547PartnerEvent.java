package hre.gui;
/*******************************************************************************
 * Partner Event - Inherit from Specification 05.47 GUI_Edit Event
 * v0.03.0031 2024-02-27 first draft (N.Tolleshaug)
 *			  2024-03-22 New handling of date, location and memo (N. Tolleshaug)
 *		      2024-05-26 New handling event partner (N. Tolleshaug)
 *			  2024-07-14 New handling of Hdate (N. Tolleshaug)
 * 			  2024-07-23 NLS conversion (D Ferguson)
 * 			  2024-07-24 Updated for use of HG0590EditDate (N Tolleshaug)
 * 			  2024-10-05 Updated save partner (N Tolleshaug)
 * 			  2024-10-06 Removed reset PS for partner event (N Tolleshaug)
 *******************************************************************************
 * NOTES for incomplete functionality:
 * NOTE08 need to check that Min# of Key_Assoc have been selected before saving
 ******************************************************************************/

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBProjectOpenData;
import hre.nls.HG0547Msgs;

/**
 * Partner Event
 * @author N.Tolleshaug
 * @version v0.03.0031
 * @since 2024-05-26
 */
public class HG0547PartnerEvent extends HG0547EditEvent {

	private static final long serialVersionUID = 1L;
	static long null_RPID  = 1999999999999999L;
	String[] partnerTypeList;
	int [] partnerTypeNumbers;
	Object[] partnerRelationData = null;
	final static int partnerEventGroup = -2; // Include both group 6 and 7
	static long eventPID = null_RPID;
	long  partnerTablePID = null_RPID;
	int selectedEventNum = 1004, selectedRoleNum = 1;

	public HG0547PartnerEvent(HBProjectOpenData pointOpenProject, int selectedRowInTable, int eventNumber,
														int roleNumber , long createdPartnerTablePID) throws HBException {
		super(pointOpenProject, eventNumber, roleNumber, eventPID);
		this.partnerTablePID = createdPartnerTablePID;

		partnerRelationData = pointPersonHandler.getPartnerTableData(selectedRowInTable);
		if (partnerRelationData != null) {
			try {
				eventName = pointWhereWhenHandler.getEventName((int)partnerRelationData[1]);
			// If partner table row exist
				partnerTablePID = (long)partnerRelationData[0];
				selectedEventNum = (int)partnerRelationData[1];
			// If partner event is created
				if (createdPartnerTablePID != null_RPID) {
					partnerTablePID = createdPartnerTablePID;
				}
			} catch (HBException hbe) {
				System.out.println(" HG0547PartnerEvent event name error : " + eventName);	//$NON-NLS-1$
				hbe.printStackTrace();
			}
		}

		setTitle(HG0547Msgs.Text_50 + eventName + HG0547Msgs.Text_51 + eventPersonName);	// Create   //  event for

	// Partner role recorded in partner table!
		selectedRoleNum = 1;

	// Listener for Save button
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				if (HGlobal.writeLogs)
					HB0711Logging.logWrite("Action: accepting updates and leaving HG0547PartnerEvent");	//$NON-NLS-1$
				
				long newEventRecordPID;
				try {
				// Check if data updated
					if (locationElementUpdate > 0 || startDateOK ||sortDateOK || memoEdited) {
				//if update memo text
						if (memoEdited) 
							pointWhereWhenHandler.createFromGUIMemo(memoText.getText());
						
				// Create new event
						newEventRecordPID = pointWhereWhenHandler.createNewEvent(selectedEventNum, selectedRoleNum);
				// Create a new set of HDATE records
						if (startDateOK)
							pointWhereWhenHandler.createEventDates(true, newEventRecordPID, "START_HDATE_RPID", 
									startHREDate);	//$NON-NLS-1$			
						if (sortDateOK)
							pointWhereWhenHandler.createEventDates(true, newEventRecordPID, "SORT_HDATE_RPID", 
									sortHREDate);	//$NON-NLS-1$
						
				// Update partner table
						pointWhereWhenHandler.updateEventPartnerTable(partnerTablePID, newEventRecordPID);

					} else System.out.println(" HG0547PartnerEvent - No edited data for event!");	//$NON-NLS-1$
					

				// Reload GUI Managers
					// Reload Person windows
					pointOpenProject.reloadT401Persons();
					pointOpenProject.reloadT402Names();
					pointOpenProject.getPersonHandler().resetPersonManager();
					//pointOpenProject.getPersonHandler().resetPersonSelect(); // 6.10.2024 No reset PS
					dispose();

				} catch (HBException hbe) {
					System.out.println("HG0547PartnerEvent - Failed to add partner event: " + hbe.getMessage());	//$NON-NLS-1$
					JOptionPane.showMessageDialog(btn_Save, HG0547Msgs.Text_52 + hbe.getMessage(),	// Failed to add Partner event \n
							HG0547Msgs.Text_40, JOptionPane.ERROR_MESSAGE);							// Event Save Error
					if (HGlobal.DEBUG) {
						hbe.printStackTrace();
					}
				}
			}
		});
	}

}
