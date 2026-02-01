package hre.gui;
/**************************************************************************************
 * HG0566UpdateSource extends HG0566EditSource
 * ***********************************************************************************
 * v0.04.0032 2025-10-06 Original draft (N Tolleshaug)
 *			  2025-10-07 Updated save enable for edit action (N Tolleshaug)
 *			  2025-10-12 Partly updated for Add/Update source  (N. Tolleshaug)
 *			  2025-11-11 Include rep link data in repo data table (D Ferguson)
 *			  2025-11-16 Moved col head read to EditSource (N. Tolleshaug)
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
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.nls.HG0566Msgs;

/**
 * Update Source
 * @author N. Tolleshaug
 * @version v0.04.0032
 * @since 2025-10-06
 */
public class HG0566UpdateSource extends HG0566EditSource {
	private static final long serialVersionUID = 1L;
	HBPersonHandler pointPersonHandler;

/**
 * HG0566UpdateSource(HBProjectOpenData pointOpenProject, long sourcePID, HG0565ManageSource pointManageSource)
 * @param pointOpenProject
 * @param sourcePID
 * @param pointManageSource
 */
	public HG0566UpdateSource(HBProjectOpenData pointOpenProject, long sourcePID, HG0565ManageSource pointManageSource) {
		super(pointOpenProject, sourcePID);
		this.pointOpenProject = pointOpenProject;
		pointPersonHandler = pointOpenProject.getPersonHandler();
		pointCitationSourceHandler = pointOpenProject.getCitationSourceHandler();
		pointRepositoryHandler = pointOpenProject.getRepositoryHandler();
		this.sourceTablePID = sourcePID;
		setTitle(HG0566Msgs.Text_5);	// Edit Source
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
				HB0711Logging.logWrite("ERROR: in HG0566Upd loading Source Elements: " + hbe.getMessage()); //$NON-NLS-1$
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

        // Get the data for this Source for populating this screen
 		try {
 			sourceEditData = pointCitationSourceHandler.getSourceEditData(sourcePID);
 		} catch (HBException hbe) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in HG0566Upd loading Source data: " + hbe.getMessage()); //$NON-NLS-1$
				HB0711Logging.printStackTraceToFile(hbe);
			}
 		}

 		// Get the Source Defn list (to be able to get the type's name by matching the PID)
		try {
			sorcDefnTable = pointCitationSourceHandler.getSourceDefnList(HGlobal.dataLanguage);
		} catch (HBException hbe) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in HG0566Upd loading Source Defns: " + hbe.getMessage()); //$NON-NLS-1$
				HB0711Logging.printStackTraceToFile(hbe);
			}
		}

		// and sort it as we need it in sorted order for its combobox
		Arrays.sort(sorcDefnTable, (row1, row2) -> ((String) row1[0]).compareTo((String) row2[0]));

 		// Get the 3 underlying Source Defn templates using the sorcDefnPID value
		sourceDefnPID = (long)sourceEditData[11];
		try {
			sorcDefnTemplates = pointCitationSourceHandler.getSourceDefnTemplates(sourceDefnPID);
		} catch (HBException hbe) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in HG0566Upd loading Source Defn templates: " + hbe.getMessage()); //$NON-NLS-1$
				HB0711Logging.printStackTraceToFile(hbe);
			}
		}

		// Get the Source Element data values belonging to this Source
		try {
			tableSourceElmntDataValues = pointCitationSourceHandler.getSourceElmntDataValues(sourcePID);
		} catch (HBException hbe) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in HG0566Upd loading Source Element data: " + hbe.getMessage()); //$NON-NLS-1$
				HB0711Logging.printStackTraceToFile(hbe);
			}
		}

		// Get any Source of Source citation data for this Source
		try {
			citnSrcSrcData = pointCitationSourceHandler.getCitationSourceData(sourcePID, "T736"); //$NON-NLS-1$
		} catch (HBException hbe) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in HG0566Upd loading Source of Source data: " + hbe.getMessage()); //$NON-NLS-1$
				HB0711Logging.printStackTraceToFile(hbe);
			}
		}

		// Get any Repository data for this Source
		// First get the PIDs of all Repos connected to this Source via the T740 link table
		try {
			repoLinkData = pointRepositoryHandler.getRepoLinkData(sourcePID);
		} catch (HBException hbe) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in HG0566Upd loading Repository links: " + hbe.getMessage()); //$NON-NLS-1$
				HB0711Logging.printStackTraceToFile(hbe);
			}
		}
		// Then get the data for each of the Repos found in repoPIDs
		if (repoLinkData.length > 0) {
			tableRepoData = new Object[repoLinkData.length][5];
			for (int i=0; i < repoLinkData.length; i++) {
				repoEditData = new Object[3];
				try {
					repoEditData = pointRepositoryHandler.getRepositoryData((long) repoLinkData[i][0]);
				} catch (HBException hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0566Upd loading Repository link data: " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
				if (repoEditData != null) {
					// Save the data we need for the Repository table
					tableRepoData[i][0] = repoEditData[2];  // the repo ref#
					tableRepoData[i][1] = repoEditData[1];  // the repo abbrev
					tableRepoData[i][2] = repoLinkData[i][1];  // the repo link reference
					tableRepoData[i][3] = repoLinkData[i][2];  // the repo link Primary setting
					tableRepoData[i][4] = repoLinkData[i][0]; // Repository table PID
				}
			}
		}
		screenLayout();
		createActionListner();
		loadData();
		btn_Save.setEnabled(false);

	// Listener for Save button to update source
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
				if (HGlobal.writeLogs)
					HB0711Logging.logWrite("Action: saving data in HG0566UpdateSource");	//$NON-NLS-1$
				// Collect the GUI data
				storeData();
				try {
				// Update source record
					pointCitationSourceHandler.updateSourceRecord(sourceTablePID, sourceStoreData);
				// Update source element data
					pointCitationSourceHandler.updateSourceElementDataRecords(sourceTablePID);
				// Reset displayed source table
					pointManageSource.resetSourceTable(true);
				} catch (HBException hbe) {
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("ERROR: in HG0566Upd saving Source: " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
				}
				if (HGlobal.writeLogs)
					HB0711Logging.logWrite("Action: Data saved in HG0566UpdateSource");	//$NON-NLS-1$
				// Exit this screen
				pointEditSource.dispose();
			}
		});

	} // End HG0566UpdateSource constructor

}
