package hre.gui;
/**********************************************************************************
 * Project Status - Specification 06.20 GUI_DB Maintenance
 * v0.00.0025 2021-02-12 initial draft (D Ferguson)
 * 			  2021-03-07 Add confirmation of action messages (D Ferguson)
 * v0.01.0026 2021-09-15 Apply tag codes to screen control buttons (D Ferguson)
 * v0.01.0027 2021-11-30 Check for no projects open condition (D Ferguson)
 * v0.04.0032 2025-05-20 Perform NLS update (D Ferguson)
 **********************************************************************************/

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBProjectOpenData;
import hre.nls.HG0620Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Project DB Maintenance
 * @author D Ferguson
 * @version v0.04.0032
 * @since 2021-02-12
 */

public class HG0620DBMaintenance extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	private String screenID = "62000"; //$NON-NLS-1$
	private static JPanel contents;
	private static String selected = ""; //$NON-NLS-1$

/**
 * Create the Dialog.
 */
	public HG0620DBMaintenance(HBProjectOpenData pointOpenProject) {

		// Setup references for HG0450
		windowID = screenID;
		helpName = "dbmaintenance"; //$NON-NLS-1$

		setResizable(false);
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/hre/images/HRE-32.png"))); //$NON-NLS-1$
		setTitle(HG0620Msgs.Text_4);		// Database Maintenance
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[]", "[]10[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0620DBMaintenance"); //$NON-NLS-1$

		// Button group for radio buttons
		String reset = HG0620Msgs.Text_9;		// Perform reset of all screen configuration data
		String validate = HG0620Msgs.Text_10;	// Perform validation of all data in project database

		JRadioButton buttonA = new JRadioButton(reset);
		JRadioButton buttonB = new JRadioButton(validate);
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(buttonA);
		buttonGroup.add(buttonB);
		contents.add(buttonA, "wrap"); //$NON-NLS-1$
		contents.add(buttonB, "wrap"); //$NON-NLS-1$

		JButton btn_Cancel = new JButton(HG0620Msgs.Text_13);	// Cancel
		contents.add(btn_Cancel, "cell 0 2, align right, gapx 15, tag cancel"); //$NON-NLS-1$

		JButton btn_Execute = new JButton(HG0620Msgs.Text_15);	// Execute
		contents.add(btn_Execute, "cell 0 2, align right, gapx 15, tag ok"); //$NON-NLS-1$

		pack();

/**
* Create Action Listeners
**/
		// Listener for clicking 'X' on screen
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting out of HG0620DBMaintenance"); //$NON-NLS-1$
				dispose();
		    }
		});

		// Listener for Cancel button
		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: cancelling out of HG0620DBMaintenance"); //$NON-NLS-1$
				dispose();
			}
		});

		// Listener for Execute button
		btn_Execute.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (HGlobal.numOpenProjects == 0) {
					JOptionPane.showMessageDialog(btn_Execute, HG0620Msgs.Text_19,	// There are no projects open to \nperform Maintenance on.
							HG0620Msgs.Text_20, JOptionPane.ERROR_MESSAGE);			// Maintenance
				} else {
			        	if (selected.equals(reset)) {
			        		if (HGlobal.writeLogs)
			        			HB0711Logging.logWrite("Action: performed screen config reset in HG0620DBMaintenance"); //$NON-NLS-1$
			        		try {
			        			if (!pointOpenProject.pointT302_GUI_CONFIG.isClosed()) {
									pointOpenProject.pointGuiData.deleteRecords(pointOpenProject.pointT302_GUI_CONFIG);
									pointOpenProject.pointGuiData.createT302data(pointOpenProject.pointT302_GUI_CONFIG,true);
									int dbIndex = pointOpenProject.getOpenDatabaseIndex();
									pointOpenProject.loadT302data(dbIndex);
									JOptionPane.showMessageDialog(btn_Execute, HG0620Msgs.Text_22, // Performed reset of screen configuration data
																			   HG0620Msgs.Text_20, // Maintenance
																			   JOptionPane.INFORMATION_MESSAGE);
			        			}
			        		} catch (HBException | HeadlessException | SQLException hbe) {
								System.out.println("HG0620DBMaint - reset ERROR T302! " + hbe.getMessage()); //$NON-NLS-1$
								hbe.printStackTrace();
							}

			        	}
						if (selected.equals(validate)) {
							JOptionPane.showMessageDialog(btn_Execute, HG0620Msgs.Text_26, // Validate action not yet available
																	   HG0620Msgs.Text_20, // Maintenance
																	   JOptionPane.INFORMATION_MESSAGE);
			        	}
				}
				dispose();
			}
		});

		// Listener for buttonA (Reset)
		buttonA.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent ae) {
	        	selected = ae.getActionCommand();
	        }
	    });

		// Listener for buttonB (Validate)
		buttonB.addActionListener(new ActionListener() {
	        @Override
	        public void actionPerformed(ActionEvent ae) {
	        	selected = ae.getActionCommand();
	        }
	    });

	}	// End HG0620 constructor

}	// End HG0620DBMaintenance
