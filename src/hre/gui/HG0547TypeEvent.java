package hre.gui;
/**********************************************************************************
 * Project Status - Specification 05.47 GUI_Edit Event
 * v0.03.0031 2024-06-24 initial draft (D Ferguson)
 * 			  2024-07-23 NLS conversion (D Ferguson)
 * 			  2024-07-28 Add role lists/controls; load event/role list (D Ferguson)
 * 			  2024-07-28 Updated for update of event/role in HG0547EditEvent (N Tolleshaug)
 * 			  2024-08-04 Updated screen prompts, labels, list selection (D Ferguson)
 * 			  2024-08-06 Updated NLS for new screen scripts (D Ferguson)
 * 			  2024-08-20 Allow Role change without Event change (D Ferguson)
 **********************************************************************************/

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import hre.bila.HBWhereWhenHandler;
import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBProjectOpenData;
import hre.nls.HG0547Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Event Type change
 * @author D Ferguson
 * @version v0.03.0031
 * @since 2024-06-24
 */

public class HG0547TypeEvent extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;
	HBWhereWhenHandler pointWhereWhenHandler;
	HG0547EditEvent pointEditEvent;
	private String screenID = "54700";	//$NON-NLS-1$
	private static JPanel contents;

	String currentEventName = "";	//$NON-NLS-1$
	String selectedEvent = "";		//$NON-NLS-1$
	String currentRoleName = "";	//$NON-NLS-1$
	int indexSelectedEvent;
	int indexSelectedRole;
	int[] eventTypesNumber;
	int[] roleTypesNumber;
	int selectedEventType;
	int selectedRole = 0;
	int eventGroup = 0;
	String[] grpEventList;
	String[] currentRoleList;
	JList<String> roleList;
	ListSelectionListener roleListener;
	
	DefaultListModel<String> roleListmodel;
	
/**
 * Create the Dialog.
 */
	public HG0547TypeEvent(HBProjectOpenData pointOpenProject, int eventGroup, int eventNumber, int eventRole, HG0547EditEvent pointEditEvent) {
		this.pointEditEvent = pointEditEvent;
		this.eventGroup = eventGroup;
		// Setup references for HG0450
		windowID = screenID;
		helpName = "editevent";		//$NON-NLS-1$
		pointWhereWhenHandler = pointOpenProject.getWhereWhenHandler();
		setResizable(false);
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/hre/images/HRE-32.png")));	//$NON-NLS-1$
		setTitle(HG0547Msgs.Text_60);	//   Change Event Type
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0547TypeEvent"); //$NON-NLS-1$

		// Get the List of Events for event group of this Event and also its Roles
		try {
	    	grpEventList = pointWhereWhenHandler.getEventTypeList(eventGroup);
	    	eventTypesNumber = pointWhereWhenHandler.getEventTypes();
			currentEventName = pointWhereWhenHandler.getEventName(eventNumber);
			currentRoleList = pointWhereWhenHandler.getEventRoleList(eventNumber, ""); //$NON-NLS-1$
			roleTypesNumber = pointWhereWhenHandler.getEventRoleTypes();
			currentRoleName = pointWhereWhenHandler.getEventRoleName(eventNumber, eventRole); 
			selectedEventType = eventNumber;
			selectedRole = eventRole;
		} catch (HBException hbe) {
			System.out.println("HG0547TypeEvent event/role load error: " + hbe.getMessage());	//$NON-NLS-1$
			hbe.printStackTrace();
		}		

		// Build screen
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]10[]10[]", "[][]10[]10[]10[]"));	//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_Prompt = new JLabel(HG0547Msgs.Text_61);	// Choose a new Event Type, then a new Role.
		contents.add(lbl_Prompt, "cell 0 0, alignx left");			//$NON-NLS-1$
		JLabel lbl_Prompt2 = new JLabel(HG0547Msgs.Text_64);	// NOTE: Associates of this Event may also need Role changes
		contents.add(lbl_Prompt2, "cell 0 1 3, alignx left");			//$NON-NLS-1$

		JLabel lbl_Event = new JLabel(HG0547Msgs.Text_65);	// Current Event:
		contents.add(lbl_Event, "cell 0 2, alignx left");			//$NON-NLS-1$
		
	    DefaultListModel<String> eventListmodel = new DefaultListModel<String>();
	    JList<String> eventList = new JList<String>(eventListmodel);
	  	for (int i = 0; i < grpEventList.length; i++)  
	  			eventListmodel.addElement(grpEventList[i]);	    
		eventList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
	    eventList.setLayoutOrientation(JList.VERTICAL);    
	    JScrollPane eventScrollPane = new JScrollPane();
		eventScrollPane.setPreferredSize(new Dimension(180, 300));
		eventScrollPane.getViewport().setView(eventList);		
		contents.add(eventScrollPane, "cell 0 3");	//$NON-NLS-1$
		  
    // Find current Event in event list and mark it as selected (for now)
	    int eventIndex = eventList.getNextMatch(currentEventName, 0, javax.swing.text.Position.Bias.Forward);
	    if (eventIndex >= 0) {			
	    	eventList.ensureIndexIsVisible(eventIndex);		// make sure selected item visible in scrollpane
	    	eventList.setSelectedIndex(eventIndex);	 		// highlight it         
	    }
	   
		JLabel lbl_Role = new JLabel(HG0547Msgs.Text_66);	// Current Role:
		contents.add(lbl_Role, "cell 1 2, alignx left");			//$NON-NLS-1$
		
	    roleListmodel = new DefaultListModel<String>();
	    roleList = new JList<String>(roleListmodel);
	  	for (int i = 0; i < currentRoleList.length; i++)  
	  			roleListmodel.addElement(currentRoleList[i]);		    
		roleList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
	    roleList.setLayoutOrientation(JList.VERTICAL);    
	    JScrollPane roleScrollPane = new JScrollPane();
		roleScrollPane.setPreferredSize(new Dimension(180, 300));
		roleScrollPane.getViewport().setView(roleList);
		contents.add(roleScrollPane, "cell 1 3");	//$NON-NLS-1$	
   	
    // Find current role in role list and mark it as selected (for now)
	    int roleIndex = roleList.getNextMatch(currentRoleName, 0, javax.swing.text.Position.Bias.Forward);
	    if (roleIndex >= 0) {			
	    	roleList.ensureIndexIsVisible(roleIndex);		// make sure selected item visible in scrollpane
	    	roleList.setSelectedIndex(roleIndex);	 		// highlight it         
	    }
	    
	    JButton btn_Cancel = new JButton(HG0547Msgs.Text_62);	// Cancel
		contents.add(btn_Cancel, "cell 0 4 3, alignx right, gapx 15, tag cancel"); //$NON-NLS-1$
		
		JButton btn_Change = new JButton(HG0547Msgs.Text_63);	// Change
		contents.add(btn_Change, "cell 0 4 3, alignx right, gapx 15, hidemode 3, tag ok"); //$NON-NLS-1$
		btn_Change.setVisible(false);
		btn_Change.setEnabled(false);
		
		pack();

/**
* Create Action Listeners
**/
	// Listener for clicking 'X' on screen
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
				btn_Cancel.doClick();
		    }
		});

	// Listener for Cancel button
		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: cancelling out of HG0547TypeEvent");	//$NON-NLS-1$
				dispose();
			}
		});

	// Listener to action selection of row in Event list
		eventList.addListSelectionListener (new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent selectEvent) {		        
				if (!eventList.getValueIsAdjusting()) {
					// Save the eventList index and selected value
					try {
						indexSelectedEvent = eventList.getSelectedIndex();
						if (indexSelectedEvent < 0) return;
						selectedEvent = eventList.getSelectedValue().toString ();
						eventTypesNumber = pointWhereWhenHandler.getEventTypes();
						selectedEventType = eventTypesNumber[indexSelectedEvent];		
						currentRoleList = pointWhereWhenHandler.getEventRoleList(selectedEventType, ""); //$NON-NLS-1$
						roleTypesNumber = pointWhereWhenHandler.getEventRoleTypes();
						roleListmodel.clear();
						for (int i = 0; i < currentRoleList.length; i++)  
				  			roleListmodel.addElement(currentRoleList[i]);
						// Change button, labels
						btn_Change.setVisible(true);
						lbl_Event.setText(HG0547Msgs.Text_67);	// New Event type:
						lbl_Role.setText(HG0547Msgs.Text_68);	// Choose a new Role:
					    
					} catch (HBException hbe) {
						System.out.println(" HG0547TypeEvent - eventList.addListSelectionListener: " + hbe.getMessage());  //$NON-NLS-1$
						hbe.printStackTrace();
					}
				}
			}
		});

	// Listener to action selection of row in Role list
		roleList.addListSelectionListener (new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent selectEvent) {	
				if (!roleList.getValueIsAdjusting()) {
					indexSelectedRole = roleList.getSelectedIndex();
					if (indexSelectedRole < 0) return;
					selectedRole = roleTypesNumber[indexSelectedRole];
					btn_Change.setVisible(true);
					btn_Change.setEnabled(true);
					lbl_Role.setText(HG0547Msgs.Text_69);	// New Role:
					}
			}
		});
		
		// Listener for Change button
		btn_Change.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {			
				try {
					pointEditEvent.updateEventType(selectedEventType, selectedRole);
					dispose();
				} catch (HBException hbe) {
					System.out.println(" HG0547TypeEvent - btn_Change.addActionListener: " + hbe.getMessage());  //$NON-NLS-1$
					hbe.printStackTrace();
				}
			}
		});
	}	// End HG0547TypeEvent constructor
	
}	// End HG0547TypeEvent