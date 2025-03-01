package hre.gui;
/***********************************************************************************
* Administer Person Name Styles - Specification 05.24 GUI_NameStyleEdit
************************************************************************************
* v0.01.0026 2022-07-15 updated dummy data to align with DDL v22 preload (D Ferguson)
* v0.01.0027 2022-08-24 Changed to extend from HG0524ManageNameStyles (D Ferguson)
* 			 2022-09-07 Activated of name style data from database (N. Tolleshaug)
*			 2022-09-08 Implemented all Save functions (N. Tolleshaug)
*			 2022-10-07 Added Update Name Style Element Description (N. Tolleshaug)
*			 2022-11-13 Add new Save listener for Output (incomplete) (D Ferguson)
*            2023-01-02 Reset elements description lists when save style (N. Tolleshaug)
*            2023-01-23 Add Convert button D Ferguson)
*            2023-01-25 Implemented Convert TMG and new exception handling (N. Tolleshaug)
* 			 2023-01-31 NLS converted (D Ferguson)
* v0.01.0029 2023-04-14 Convert function modified (D Ferguson)
* 			 2023-04-19 All NLS entries moved to HG0524Msgs (D Ferguson)
* v0.04.0032 2025-02-23 Dispose of Reminder on Close (D Ferguson)
*
***********************************************************************************/

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBNameStyleManager;
import hre.bila.HBProjectOpenData;
import hre.nls.HG0524Msgs;

/**
 * Admin Person Name Styles
 * @author D Ferguson
 * @version v0.04.0032
 * @param <NameStyleData>
 * @since 2021-04-25
 */

public class HG0525ManagePersonNameStyles extends HG0524ManageNameStyles {

	private static final long serialVersionUID = 001L;
	public static final String screenID = "52500"; //$NON-NLS-1$

	long null_RPID  = 1999999999999999L;
	HBNameStyleManager pointClass;

	HG0527ConvertNameStyle convertStyle;
/**
 * Finish tailoring the Dialog
 */
	public HG0525ManagePersonNameStyles(HBProjectOpenData pointOpenProject, HBNameStyleManager pointStyleHandler) {
		super(pointOpenProject, pointStyleHandler);

		// Setup references for HG0450, HG0524
		windowID = screenID;
		helpName = "personnamestyles"; //$NON-NLS-1$
		String nameType = "N"; //$NON-NLS-1$

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0525ManagePersonNameStyles"); //$NON-NLS-1$
		setResizable(true);
		setTitle(HG0524Msgs.Text_160);	// Administer Person Name Styles
		setNameType(nameType);

/**
 * Load data into Lists in HG0524
 **/
        // Load list of ALL Name Style Elements from T163
		pointStyleHandler.getAllElements(allElementsModel);
		pointStyleHandler.getAllCodes(allCodesModel);

		// This data loaded from list of all name style T160 records
		pointStyleHandler.getNameStyles(namestyleModel);

		list_Styles.setSelectedIndex(pointStyleHandler.getDefaultStyleIndex());	// set default as selected

		// Display list for Name Style from a T160 record
    	chosenElementsModel.clear();
    	chosenCodesModel.clear();
    	pointStyleHandler.getChosenElements(chosenElementsModel);
    	pointStyleHandler.getChosenCodes(chosenCodesModel);

		// Display list for Name Style from a T162 record
       	chosenOutElementsModel.clear();
    	chosenOutCodesModel.clear();

		// Load Name Style description from T160 contents
    	txtpnDesc.setText(pointStyleHandler.getNameStyleText(0));

       	btn_Save.setEnabled(false);
    	btn_OutSave.setEnabled(false);
    	pack();

/**
 * SETUP LISTENERS SPECIFIC TO THIS METHOD
 */
		// Listener for Close button
		btn_Close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// Close reminder if showing
				if (reminderDisplay != null) reminderDisplay.dispose();
				// If NOT in Save mode, just Close the screen
				if (!btn_Save.isEnabled() & !btn_OutSave.isEnabled()) {
					if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0525ManagePersonNameStyles"); //$NON-NLS-1$
					dispose();
				}
				// Else confirm exit without Save
				else {
					if (JOptionPane.showConfirmDialog(btn_Close,
													HG0524Msgs.Text_162 + 	// There are unsaved Name Style changes.
													HG0524Msgs.Text_163,		// Do you still wish to exit this screen?
													HG0524Msgs.Text_160,		// Administer Person Name Styles
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: cancelling HG0525ManagePersonNameStyles"); //$NON-NLS-1$
							dispose();
						}	// yes option - exit
					}
				}
		});

		// Listener for btn_Save (save all Style changes to database)
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (list_Styles.getSelectedIndex() != -1) {
					if (HGlobal.DEBUG) {
						dumpElementCodes(chosenElementsModel);
						dumpElementCodes(chosenCodesModel);
					}

					try {
			   // Update Default Style Setting in T162
						pointStyleHandler.updateDefaultStyle();
						String styleName = list_Styles.getSelectedValue();
						pointStyleHandler.updateNameStyleData(list_Styles.getSelectedIndex(),
													         chosenElementsModel,
													         chosenCodesModel);
					// Update name and description
						pointStyleHandler.updateStyleName(list_Styles.getSelectedIndex(), styleName, txtpnDesc.getText());
					// If TMG style the allElementsModel is cleared
						if (!pointStyleHandler.isTmgNameStyle[list_Styles.getSelectedIndex()]) {
							pointStyleHandler.updateNameElementDescription(nameType, allElementsModel);
							pointStyleHandler.updateAllElementTable(nameType, allElementsModel, allCodesModel);
						}
			 // Reset elements lists when changing HRE name element description
						resetElementLists();
					} catch (HBException hbe) {
						System.out.println("Save all name style changes error: " + hbe.getMessage()); //$NON-NLS-1$
						errorMessage("HBE00","Save all name style changes error: \n" + hbe.getMessage(), 0, nameType); //$NON-NLS-1$ //$NON-NLS-2$
						hbe.printStackTrace();
					}
				}
				btn_Save.setEnabled(false);
			}
		});

		// Listener for btn_OutSave (save all Output Style changes to database)
		btn_OutSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (list_OutStyles.getSelectedIndex() != -1) {
					if (HGlobal.DEBUG) {
						dumpElementCodes(chosenOutElementsModel);
						dumpElementCodes(chosenOutCodesModel);
					}
					String styleName = list_OutStyles.getSelectedValue();
			// Update output code String
					try {
						pointStyleHandler.updateOutputStyleData(list_OutStyles.getSelectedIndex(), chosenOutCodesModel);
						pointStyleHandler.updateOutputStyleName(list_OutStyles.getSelectedIndex(),
															styleName, txtpnOutDesc.getText());
			// Reset output elements lists when changing element list
						pointStyleHandler.setOutputStyleTable(nameType);
						resetElementLists();
					} catch (HBException hbe) {
						System.out.println(" HG0525ManagePersonNameStyles - Save output style error: " + hbe.getMessage()); //$NON-NLS-1$
						hbe.printStackTrace();
					}
				}
				btn_OutSave.setEnabled(false);
			}
		});

		// Listener for Convert button - call HG0527 and exit this screen
		btn_Convert.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (list_Styles.getSelectedIndex() != -1) {
					// Get name of TMG Name Style
					String tmgStyle = list_Styles.getSelectedValue();
					// Invoke Convert routine
					Point xy = btn_Convert.getLocationOnScreen();
					convertStyle = new HG0527ConvertNameStyle(pointOpenProject, pointStyleHandler, tmgStyle, styleIndex, nameType);
					convertStyle.setLocation(xy.x-100, xy.y);
					convertStyle.setAlwaysOnTop(true);
					// and dispose of one-self
					dispose();
				}
			}
		});

	}	// End HG0525ManagePersonNameStyles constructor
}