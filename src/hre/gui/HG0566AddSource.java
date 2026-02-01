package hre.gui;
/**************************************************************************************
 * HG0566AddSource extends HG0566EditSource
 * ***********************************************************************************
 * v0.04.0032 2025-10-06 Original draft (N Tolleshaug)
 *			  2025-10-07 Populate element table for Add Source (N Tolleshaug)
 *			  2025-10-07 Updated save enable for edit action (N Tolleshaug)
 *			  2025-10-12 Partly updated for Add/Update source  (N. Tolleshaug)
 *			  2025-10-16 Updated warning when save (N. Tolleshaug)
 *			  2025-11-16 Moved col head read to EditSource (N. Tolleshaug)
 *			  2025-12-01 Changed test for Name/Abbrev both entered (D Ferguson)
 *			  2025-12-06 Uses validateBeforeSave routine to do that! (D Ferguson)
 *			  2025-12-20 NLS all code to this point (D Ferguson)
 *			  2025-12-22 Updated foot note error handling (N. Tolleshaug)
 *			  2025-12-29 NLS update (D Ferguson)
 *			  2026-01-07 Log catch block msgs (D Ferguson)
 ************************************************************************************/

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JOptionPane;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBProjectOpenData;
import hre.nls.HG0566Msgs;

/**
 * Add Source
 * @author N. Tolleshaug
 * @version v0.04.0032
 * @since 2025-10-06
 */
public class HG0566AddSource extends HG0566EditSource {
	private static final long serialVersionUID = 1L;
	private static long sourcePID;

/**
 * HG0566AddSource(HBProjectOpenData pointOpenProject, HG0565ManageSource pointManageSource)
 * @param pointOpenProject
 * @param pointManageSource
 */
	public HG0566AddSource(HBProjectOpenData pointOpenProject, HG0565ManageSource pointManageSource) {
		super(pointOpenProject, sourcePID);
		this.pointOpenProject = pointOpenProject;
		pointPersonHandler = pointOpenProject.getPersonHandler();
		pointCitationSourceHandler = pointOpenProject.getCitationSourceHandler();
		setTitle(HG0566Msgs.Text_60);		// Add Source
		setResizable(false);
	// Setup references for HG0450
		windowID = screenID;
		helpName = "editsource";	//$NON-NLS-1$

	// Start loading data required for this screen
		// Load the Source Element list (names/ID#s) as we need it for Source template conversion & checking
		try {
			tableSrcElmntData = pointCitationSourceHandler.getSourceElmntList(HGlobal.dataLanguage);
		} catch (HBException hbe) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in HG0566Add loading Source Elements: " + hbe.getMessage()); //$NON-NLS-1$
				HB0711Logging.printStackTraceToFile(hbe);
			}
		}
	    // Then construct a lookup for "12345" → "[element text]" conversion
		codeToTextMap = new HashMap<>();
        for (String[] row : tableSrcElmntData) {
            if (row.length >= 2)
                codeToTextMap.put(row[1], row[0].trim());
        }
        // Then construct a lookup for "[text here]" → "12345" conversion back
        textToCodeMap = new HashMap<>();
        for (String[] row : tableSrcElmntData) {
            if (row.length >= 2) {
                String trimmedText = row[0].trim();
                textToCodeMap.put(trimmedText, row[1]);
            }
        }

 		// Get the Source Defn list (to be able to get the type's name by matching the PID)
		try {
			sorcDefnTable = pointCitationSourceHandler.getSourceDefnList(HGlobal.dataLanguage);
		} catch (HBException hbe) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in HG0566Add loading Source Defns: " + hbe.getMessage()); //$NON-NLS-1$
				HB0711Logging.printStackTraceToFile(hbe);
			}
		}
		// and sort it as we need it in sorted order for its combobox
		Arrays.sort(sorcDefnTable, (row1, row2) -> ((String) row1[0]).compareTo((String) row2[0]));

 		// Get the 3 underlying Source Defn templates using the sorcDefnPID value
		try {
			sorcDefnTemplates = pointCitationSourceHandler.getSourceDefnTemplates(sourceDefnPID);
		} catch (HBException hbe) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in HG0566Add loading Source Defn templates: " + hbe.getMessage()); //$NON-NLS-1$
				HB0711Logging.printStackTraceToFile(hbe);
			}
		}

	// Set up data and screen
		screenLayout();
		createActionListner();
		btn_Save.setEnabled(false);
		activChkBox.setSelected(true);

		for (int j = 0; j < sorcDefnTable.length; j++)
			comboSourceTypes.addItem((String) sorcDefnTable[j][0]);

		// Remove Preview buttons as cannot preview templates for an unsaved Source
		btn_fullPreview.setVisible(false);
		btn_shortPreview.setVisible(false);
		btn_biblioPreview.setVisible(false);

/***********************
 * SAVE ACTION LISTENER
 ***********************/
		// Listener for Save button - adding new source
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
			// Execute validation checks on the Title/Abbrev being present and source templates being valid format
				int check;
				try {
					check = validateBeforeSave();
				} catch (HBException hbe) {
			        JOptionPane.showMessageDialog(btn_Save,
			        		HG0566Msgs.Text_80			// Unknown Source Element Name \n
			        		+ hbe.getMessage()			// (shows the name)
			        		+ footTextType,				// in Full/Short/Bibliography
			        		HG0566Msgs.Text_81,			// Source Element Name error
			                JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (check == 99) return;	// error flagged, so exit this Save

			// Validation tests passed OK, so start the Save process
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: saving new source in HG0566AddSource");	//$NON-NLS-1$
				// Collect the GUI data
				storeData();
				try {
				// Add new Source
					pointCitationSourceHandler.createSourceRecord(sourceStoreData);
				// Add new element data records
					pointCitationSourceHandler.createSourceElementDataRecords(true);
				// Reset displayed source table
					pointManageSource.resetSourceTable(true);
				} catch (HBException hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0566Add saving Source: " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
			// Exit this screen
				pointEditSource.dispose();
			}
		});
	}
}
