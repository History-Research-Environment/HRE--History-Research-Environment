package hre.gui;
/********************************************************************************************
 * GUI JinternalFrame Super class code
 * v0.01.0023 2020-08-05 Common Super class for all JinternalFrames (D Ferguson)
 *            2020-08-10 updated to integrate HG0530Viewxxxx code (N Tolleshaug, D Ferguson)
 *            2020-09-01 Implemented read/write reminder to database (N Tolleshaug)
 * 			  2020-09-15 Corrected comments and removed dead code (D Ferguson)
 * 			  2020-09-17 Make reminder always onTop and only 1 visible (D Ferguson)
 * 			  2020-10-03 Errors corrected position + size setting (N. Tolleshaug)
 * v0.01.0025 2020-10-21 Place Output, Reminder icon actions inside frame (D Ferguson)
 * 			  2020-11-03 Corrected output table print error (N. Tolleshaug)
 * v0.03.0031 2024-08-15 NLS conversion (D Ferguson)
 * v0.04.0032 2026-01-03 Logged Reminder stare msgs (D Ferguson)
 *******************************************************************************************/

import java.awt.Dialog.ModalExclusionType;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import hre.bila.HB0614Help;
import hre.bila.HB0711Logging;
import hre.bila.HBBusinessLayer;
import hre.bila.HBProjectOpenData;
import hre.nls.HG0450Msgs;

/**
 * HG0451SuperIntFrame
 * @author D Ferguson
 * @version v0.01.0032
 * @since 2020-08-05
 */

public class HG0451SuperIntFrame extends JInternalFrame {
	private static final long serialVersionUID = 1L;

	JButton btn_Configicon;
	JButton btn_Outputicon;
	JButton btn_Remindericon;
	JButton btn_Helpicon;

	private JTable tableToDisplay;	// for Output
	protected String windowID;		// for Reminder, Help functions
	String helpName;				// for Help function

	HBBusinessLayer pointBusinessLayer;
	HG0451SuperIntFrame pointOpenDisplay;
	public HG0540Reminder reminderDisplay = null;

	// Stores standard frame height
	public double frameHeight;

	private Object [][] tableControl = null;

/**
 * Dummy Method getDataTable() - override in PersonSelect and LocationSelect
 * @return
 */
	JTable getDataTable() {
		System.out.println("Output table error: No table available");	//$NON-NLS-1$
		return null;
	}

/**
 * HG0451SuperIntFrame constructors
 */
	public HG0451SuperIntFrame(HBBusinessLayer pointBusinessLayer,
								String name, boolean b1, boolean b2, boolean b3, boolean b4,
								Object [][] tableControl ) {
		super(name,b1,b2,b3,b4);
		this.pointBusinessLayer = pointBusinessLayer;
		initiateSuper(tableControl);
	}

	public HG0451SuperIntFrame(HBBusinessLayer pointBusinessLayer,
										String name,
										boolean b1, boolean b2, boolean b3, boolean b4 ) {
        super(name,b1,b2,b3,b4);
        initiateSuper(tableControl);

	}

	public HG0451SuperIntFrame(String name,
							boolean b1, boolean b2, boolean b3, boolean b4 ) {
        super(name,b1,b2,b3,b4);
        initiateSuper(tableControl);
	}

	public String getScreenID() {
		return windowID;
	}


	private void initiateSuper(Object [][] tableControl) {
		pointOpenDisplay = this;

		setFrameIcon(new ImageIcon(getClass().getResource("/hre/images/HRE-16.png")));	//$NON-NLS-1$

		btn_Configicon = new JButton("");	//$NON-NLS-1$
		btn_Configicon.setToolTipText(HG0450Msgs.Text_0);		// Opens the Screen configuration function
		btn_Configicon.setMinimumSize(new Dimension(24, 24));
		btn_Configicon.setMaximumSize(new Dimension(24, 24));
		btn_Configicon.setIcon(new ImageIcon(getClass().getResource("/hre/images/spanner_BW_24.png")));	//$NON-NLS-1$

		btn_Outputicon = new JButton("");	//$NON-NLS-1$
		btn_Outputicon.setToolTipText(HG0450Msgs.Text_1);		// Opens the Output function
		btn_Outputicon.setMinimumSize(new Dimension(24, 24));
		btn_Outputicon.setMaximumSize(new Dimension(24, 24));
		btn_Outputicon.setIcon(new ImageIcon(getClass().getResource("/hre/images/output_BW_24.png")));	//$NON-NLS-1$

		btn_Remindericon = new JButton("");	//$NON-NLS-1$
		btn_Remindericon.setToolTipText(HG0450Msgs.Text_2);		// Opens the Reminder text
		btn_Remindericon.setMinimumSize(new Dimension(24, 24));
		btn_Remindericon.setMaximumSize(new Dimension(24, 24));
		btn_Remindericon.setIcon(new ImageIcon(getClass().getResource("/hre/images/reminder_BW_24.png")));	//$NON-NLS-1$

		btn_Helpicon = new JButton("");	//$NON-NLS-1$
		btn_Helpicon.setToolTipText(HG0450Msgs.Text_3);		// Opens Help for this function
		btn_Helpicon.setMinimumSize(new Dimension(24, 24));
		btn_Helpicon.setMaximumSize(new Dimension(24, 24));
		btn_Helpicon.setIcon(new ImageIcon(getClass().getResource("/hre/images/help_BW_24.png")));	//$NON-NLS-1$

	// Listener for Config icon action
		btn_Configicon.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				HG0581ConfigureTable configWindow = new HG0581ConfigureTable(windowID, tableControl, getOpenProject());
			    configWindow.setModalityType(ModalityType.APPLICATION_MODAL);
			    Point xy = btn_Configicon.getLocationOnScreen();          	// Gets Config button location on screen
			    configWindow.setLocation(xy.x-350, xy.y-30);      	 		// Sets screen top-left corner relative to that
		        configWindow.setVisible(true);
			}
		});

	// Listener for Output icon action
		btn_Outputicon.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tableToDisplay = getDataTable();
				HG0531Output outputWindow = new HG0531Output(getTitle(), tableToDisplay);
			    outputWindow.setModalityType(ModalityType.APPLICATION_MODAL);
			    Point xy = btn_Outputicon.getLocationOnScreen();          	// Gets Output button location on screen
			    outputWindow.setLocation(xy.x-320, xy.y-30);       			// Sets screen top-left corner relative to that
		        outputWindow.setVisible(true);
			}
		});

		// Listener for Reminder icon action
		btn_Remindericon.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (reminderDisplay == null) {
					reminderDisplay = new HG0540Reminder(windowID, pointOpenDisplay);
					reminderDisplay.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
					Point xy = btn_Remindericon.getLocationOnScreen();        	 // Gets Reminder button location on screen
					reminderDisplay.setLocation(xy.x-360, xy.y-30);              // Sets screen top-left corner relative to that
					reminderDisplay.setAlwaysOnTop(true);
					reminderDisplay.setVisible(true);
				} else reminderDisplay.toFront();
			}
		});

		// Listener for Help icon action (must use mainWindowInstance as this is a JIF, not a Window)
		btn_Helpicon.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				HB0614Help.contextHelp(HGlobal.mainWindowInstance, btn_Helpicon, windowID + helpName);
			}
		});

		// Binding to allow F1 to be recognised
	    InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
	    ActionMap actionMap = rootPane.getActionMap();
	    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), 0);
	    actionMap.put(0, new AbstractAction() {
	    	private static final long serialVersionUID = 1L;
	        @Override
			public void actionPerformed(ActionEvent arg0) {
	            btn_Helpicon.doClick();
	         	}
	    });

	}	// End HG0451SuperIntFrame Initiator

/**
 * HBProjectOpenData getOpenProject()
 * @return
 */
	private HBProjectOpenData getOpenProject() {
		HBProjectOpenData point = HG0401HREMain.mainFrame.getSelectedOpenProject() ;
		return point;
	}

/**
 * Store reminder text
 * @param text
 */
	public void storeReminderText(String text) {
		if (HGlobal.writeLogs)
			HB0711Logging.logWrite("Action: in HG0451 Storing Reminder text for \nWindow ID: "	//$NON-NLS-1$
											+ windowID + "\n" + text);	//$NON-NLS-1$
		getOpenProject().pointGuiData.setDispRemindText(windowID,text) ;
	}	// End storeReminderTetxt

/**
 * Read reminder text
 * @return reminder text
 */
	public String readReminderText() {
		String reminder = getOpenProject().pointGuiData.getDispRemindText(windowID);
		if (HGlobal.writeLogs)
			HB0711Logging.logWrite("Action: in HG0451 Reading Reminder text for \nWindow ID: "	//$NON-NLS-1$
											+ windowID + "\n" + reminder);	//$NON-NLS-1$
		return reminder;
	}	// End readReminderTetxt

}	// End HG0451SuperIntFrame
