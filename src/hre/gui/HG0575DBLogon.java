package hre.gui;
/*************************************************************************************
 * Database Logon  - Specification 05.75 GUI_DatabaseLogon
 * v0.00.0007 2019-02-17 by R Thompson, updated 2019-07-19 by D Ferguson
 * v0.00.0008 2019-07-20 add listener for clicking 'X' on frame (D Ferguson)
 * 					 	 change name from ServerLogInOff to ServerLogOnOff
 * v0.00.0014 2019-11-18 changes for HB0711 static classes (D Ferguson)
 * v0.00.0015 2019-12-07 changed code for HG0514 call (D Ferguson)
 * v0.00.0017 2020-01-20 modified screen, added 3 logon attempts trap (D. Ferguson)
 * v0.00.0022 2020-07-27 add screenID for Reminder, Help xref (D Ferguson)
 * v0.01.0023 2020-08-04 changed from JDialog to extend HG0450SuperDialog (D Ferguson)
 *            2020-09-11 removed Reminder icon from Toolbar (D Ferguson)
 *            2020-09-30 changed to MigLayout; fonts removed for JTattoo install (D Ferguson)
 * v0.01.0026 2021-09-15 Apply tag codes to screen control buttons (D Ferguson)
 * v0.01.0027 2021-09-27 Revised screen for DB login instead of server (D Ferguson)
 * 			  2021-09-29 renamed to HG0575DBLogon and modified (N Tolleshaug)
 * 			  2021-10-28 get server IP/port from AUX server entry (D Ferguson)
 * v0.01.0030 2023-06-21 set an errorCode for login cancel to avoid open error (D Ferguson)
 *************************************************************************************/

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import hre.bila.HB0711Logging;
import hre.bila.HBProjectHandler;
import hre.nls.HG0575Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Database Logon (was Server logon/off)
 * @author R Thompson
 * @version v0.01.0030
 * @since 2019-02-17
 */

public class HG0575DBLogon extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	private String screenID = "57500"; //$NON-NLS-1$
	private JPanel contents;
	private JTextField txt_UserID;
	private JTextField txt_Password;
	private JTextField txt_ServerID;
    String serverName, serverIP, serverPort, dbType;
	boolean logonOK = false;
	private int attemptLogin = 3;
	private int errorCode;

/**
 * Launch the window.
 */
	public HG0575DBLogon(HBProjectHandler pointProHand, String[] projectsData) {
		// Setup references for HG0450
		windowID = screenID;
		helpName = "dblogon"; //$NON-NLS-1$

		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0575DBLogon"); //$NON-NLS-1$

		// projectsData is the project row from HGlobal.userProjects
		serverName = projectsData[3];
		dbType = projectsData[5];
		boolean found = false;

		// Lookup Server table to get ServerIP and Port
		for (String[] element : HGlobal.userServers) {
			if (element[0].trim().equals(serverName.trim())) {
				serverIP = element[1];
				serverPort = element[2];
				found = true;
			}
		}
		if (!found) {
			System.out.println(" ERROR - HG0575DBLogon - No server found: " + serverName  //$NON-NLS-1$
					+ " - Selected first entry in server list");	 //$NON-NLS-1$
			if (HGlobal.writeLogs) HB0711Logging.logWrite("Error: No server found: " + serverName); //$NON-NLS-1$
			serverIP = HGlobal.userServers.get(0)[1];
			serverPort = HGlobal.userServers.get(0)[2];
		}

		buildWindow(pointProHand, projectsData);
	}	// End HG0575DBLogon constructor

	public int getErrorCode() {
		return errorCode;
	}

/**
 * Build contents of the Dialog and execute it
 */
	protected void buildWindow(HBProjectHandler pointProHand, String[] projectsData) {
		setTitle(HG0575Msgs.Text_6 + projectsData[0] + HG0575Msgs.Text_7 + projectsData[3]);	// Logon to   // at server
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		contents = new JPanel();
		contents.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[][grow]", "[]10[]10[]10[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.add(Box.createHorizontalGlue());
		// Add icons defined in HG0450
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north"); //$NON-NLS-1$

		JLabel lbl_Server = new JLabel();
		lbl_Server.setText(HG0575Msgs.Text_12);					// Remote Connection Data:
		contents.add(lbl_Server, "cell 0 0, alignx right");	    //$NON-NLS-1$

		txt_ServerID = new JTextField(dbType + "/" + serverIP + ":" + serverPort); //$NON-NLS-1$ //$NON-NLS-2$
		txt_ServerID.setColumns(25);
		contents.add(txt_ServerID, "cell 1 0, growx");			 //$NON-NLS-1$

		JLabel lbl_User = new JLabel(HG0575Msgs.Text_17); // Database Logon ID:
		contents.add(lbl_User, "cell 0 1, alignx right"); //$NON-NLS-1$

		txt_UserID = new JTextField(""); //$NON-NLS-1$
		txt_UserID.setColumns(25);
		if (HGlobal.remoteID.length() == 0) txt_UserID.setText(HGlobal.userID);
		else txt_UserID.setText(HGlobal.remoteID);
		contents.add(txt_UserID, "cell 1 1, growx");			 //$NON-NLS-1$

		JLabel lbl_Password = new JLabel(HG0575Msgs.Text_21); // Database password:
		contents.add(lbl_Password, "cell 0 2, alignx right"); //$NON-NLS-1$

		//txt_Password = new JTextField("");
		txt_Password = new JPasswordField(""); //$NON-NLS-1$
		txt_Password.setColumns(25);
		txt_Password.setText(HGlobal.passWord);
		contents.add(txt_Password, "cell 1 2, growx");	 //$NON-NLS-1$

		JLabel msgAttempts = new JLabel(""); //$NON-NLS-1$
		contents.add(msgAttempts, "cell 1 3"); //$NON-NLS-1$
		if (attemptLogin < 3)
			msgAttempts.setText(HG0575Msgs.Text_27 + attemptLogin);	// Attempts remaining:

		JButton btn_Cancel = new JButton(HG0575Msgs.Text_28);	// Cancel
		btn_Cancel.setToolTipText(HG0575Msgs.Text_29);			// Cancels any action and closes
		contents.add(btn_Cancel, "cell 1 4, align right, gapx 20, tag cancel"); //$NON-NLS-1$

		JButton btn_DBLogon = new JButton(HG0575Msgs.Text_31);	// Logon
		btn_DBLogon.setToolTipText(HG0575Msgs.Text_32);			// Logon to the selected remote database
		contents.add(btn_DBLogon, "cell 1 4, align right, gapx 20, tag ok"); //$NON-NLS-1$

		pack();

/***
 * CREATE ACTION BUTTON LISTENER
 **/
		//buttons
		btn_DBLogon.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//int errorCode = pointProHand.openProjectAction(projectsData[0]);
				String[] loginData = {txt_ServerID.getText(),txt_UserID.getText(),txt_Password.getText()};

				if (txt_UserID.getText().equals("SA")) { //$NON-NLS-1$
					JOptionPane.showMessageDialog(btn_DBLogon, HG0575Msgs.Text_35, 		// Illegal user SA
										HG0575Msgs.Text_36, JOptionPane.ERROR_MESSAGE);	// Logon Message
					dispose();
				}
				HGlobal.remoteID = txt_UserID.getText();
				HGlobal.passWord = txt_Password.getText();
				errorCode = pointProHand.openProjectAction(true, projectsData[0], loginData);
				if (HGlobal.DEBUG) System.out.println("Remote Logon errorcode = " + errorCode + "  Server: " + projectsData[3]); //$NON-NLS-1$ //$NON-NLS-2$
				if (errorCode == 0) logonOK = true;
				if (logonOK) {  // lookup userServers table and set logged in flag to true for this server
					if (HGlobal.writeLogs)
						HB0711Logging.logWrite("Action: logged into server "+ txt_ServerID.getText() + "; exiting HG0575DBLogon"); //$NON-NLS-1$ //$NON-NLS-2$
					dispose();
				} else {attemptLogin = attemptLogin - 1;}
				msgAttempts.setText(attemptLogin + HG0575Msgs.Text_41 );	// Logon sttempts remaining
				if (attemptLogin == 0) {
					JOptionPane.showMessageDialog(btn_DBLogon, HG0575Msgs.Text_42, 		// Logon Failed
										HG0575Msgs.Text_36, JOptionPane.ERROR_MESSAGE);	// Logon Message
					if (HGlobal.writeLogs)
						HB0711Logging.logWrite("Action: logon to server " + txt_ServerID.getText() + " failed; exiting HG0575DBLogon"); //$NON-NLS-1$ //$NON-NLS-2$
					dispose();
				}
			}
		});

		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				errorCode = 9; // for cancelled out of Remote login
				if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: cancelling HG0575DBLogon");} //$NON-NLS-1$
				dispose();
			}
		});

		// Listener for clicking 'X' on screen - make same as Cancel button
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
		    	btn_Cancel.doClick();
		    }
		});

	}	//End buildWindow

}	// End HG0575DBLogon
