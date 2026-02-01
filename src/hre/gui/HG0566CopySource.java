package hre.gui;
/**************************************************************************************
 * HG0566copySource extends HG0566EditSource
 * ***********************************************************************************
 * v0.04.0032 2025-12-01 Original draft (N Tolleshaug)
 *			  2025-12-03 Implemented copy and create new source copy (N Tolleshaug)
 *			  2025-12-05 Add warning that Source/source and Repo data not copied (D Ferguson)
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
 * Copy) Source
 * @author N. Tolleshaug
 * @version v0.04.0032
 * @since 2025-12-03
 */
public class HG0566CopySource extends HG0566EditSource {
	private static final long serialVersionUID = 1L;
	HBPersonHandler pointPersonHandler;
	int dataBaseIndex;

/**
 * HG0566CopySource(HBProjectOpenData pointOpenProject, long sourcePID, HG0565ManageSource pointManageSource)
 * @param pointOpenProject
 * @param sourcePID
 * @param pointManageSource
 */
	public HG0566CopySource(HBProjectOpenData pointOpenProject, long sourcePID, HG0565ManageSource pointManageSource) {
		super(pointOpenProject, sourcePID);
		this.pointOpenProject = pointOpenProject;
		pointPersonHandler = pointOpenProject.getPersonHandler();
		pointCitationSourceHandler = pointOpenProject.getCitationSourceHandler();
		pointRepositoryHandler = pointOpenProject.getRepositoryHandler();
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		this.sourceTablePID = sourcePID;
		setTitle(HG0566Msgs.Text_70);		// Copy Source
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
				HB0711Logging.logWrite("ERROR: in HG0566Copy loading Source Elements: " + hbe.getMessage()); //$NON-NLS-1$
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
				HB0711Logging.logWrite("ERROR: in HG0566Copy loading Source data: " + hbe.getMessage()); //$NON-NLS-1$
				HB0711Logging.printStackTraceToFile(hbe);
			}
 		}

 		// Get the Source Defn list (to be able to get the type's name by matching the PID)
		try {
			sorcDefnTable = pointCitationSourceHandler.getSourceDefnList(HGlobal.dataLanguage);
		} catch (HBException hbe) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in HG0566Copy loading Source Defns: " + hbe.getMessage()); //$NON-NLS-1$
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
				HB0711Logging.logWrite("ERROR: in HG0566Copy loading Source Defn templates: " + hbe.getMessage()); //$NON-NLS-1$
				HB0711Logging.printStackTraceToFile(hbe);
			}
		}

		// Get the Source Element data values belonging to this Source
		try {
			tableSourceElmntDataValues = pointCitationSourceHandler.getSourceElmntDataValues(sourcePID);
		} catch (HBException hbe) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in HG0566Copy loading Source Element data: " + hbe.getMessage()); //$NON-NLS-1$
				HB0711Logging.printStackTraceToFile(hbe);
			}
		}

		// Get any Source of Source citation data for this Source
		try {
			citnSrcSrcData = pointCitationSourceHandler.getCitationSourceData(sourcePID, "T736"); //$NON-NLS-1$
		} catch (HBException hbe) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in HG0566Copy loading Source of Source data: " + hbe.getMessage()); //$NON-NLS-1$
				HB0711Logging.printStackTraceToFile(hbe);
			}
		}

		// Get any Repository data for this Source
		// First get the PIDs of all Repos connected to this Source via the T740 link table
		try {
			repoLinkData = pointRepositoryHandler.getRepoLinkData(sourcePID);
		} catch (HBException hbe) {
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("ERROR: in HG0566Copy loading Repository link entries: " + hbe.getMessage()); //$NON-NLS-1$
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
						HB0711Logging.logWrite("ERROR: in HG0566Copy loading Repository link data: " + hbe.getMessage()); //$NON-NLS-1$
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

	// Mark the title/abbrev for copied source text
		titleText.setText(HG0566Msgs.Text_71 + titleText.getText());		// COPY -
		abbrevText.setText(HG0566Msgs.Text_71 + abbrevText.getText());		// COPY -

	// Listener for Save button copy source
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
					HB0711Logging.logWrite("Action: saving data in HG0566CopySource");	//$NON-NLS-1$
				// Collect the copied GUI data
				storeData();
				try {
					pointCitationSourceHandler.updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex); //$NON-NLS-1$
				// Create copied Source record
					sourceTablePID = pointCitationSourceHandler.createSourceRecord(sourceStoreData);
				// Add new element data records
					pointCitationSourceHandler.createSourceElementDataRecords(true);
				// Reset displayed source table
					pointManageSource.resetSourceTable(true);
				// End transaction
					pointCitationSourceHandler.updateTableData("COMMIT", dataBaseIndex);	//$NON-NLS-1$
				} catch (HBException hbe) {
				// Roll back transaction if error occurs
					try {
						pointCitationSourceHandler.updateTableData("ROLLBACK", dataBaseIndex); //$NON-NLS-1$
					} catch (HBException hber) {
						if (HGlobal.writeLogs) {
							HB0711Logging.logWrite("ERROR: in HG0566Copy copy Source rollback: " + hbe.getMessage()); //$NON-NLS-1$
							HB0711Logging.printStackTraceToFile(hbe);
						}
					}
					if (HGlobal.writeLogs)
						HB0711Logging.logWrite("ERROR: in HG0566Copy update Source: " + hbe.getMessage()); //$NON-NLS-1$
					hbe.printStackTrace();
					if (HGlobal.writeLogs)
						HB0711Logging.logWrite("Action: source copy rolled back in HG0566CopySource"); //$NON-NLS-1$
					JOptionPane.showMessageDialog(pointEditSource,
							HG0566Msgs.Text_72,		// Source copy failed
							HG0566Msgs.Text_73,		// Copy Source
							JOptionPane.ERROR_MESSAGE);
					// Exit this screen
					pointEditSource.dispose();
				}
				// Source copy completed
				if (HGlobal.writeLogs)
					HB0711Logging.logWrite("Action: source copy saved in HG0566CopySource"); //$NON-NLS-1$
				JOptionPane.showMessageDialog(pointEditSource,
						HG0566Msgs.Text_74		// Source copy completed. Note that all Repository and \n
						+ HG0566Msgs.Text_75,		// Source of Source entries have NOT been copied
						HG0566Msgs.Text_73,		// Copy Source
						JOptionPane.INFORMATION_MESSAGE);
				// Exit this screen
				pointEditSource.dispose();
			}
		});

	} // End HG0566CopySource constructor

}