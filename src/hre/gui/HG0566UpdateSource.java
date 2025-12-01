package hre.gui;
/**************************************************************************************
 * HG0566UpdateSource extends HG0566EditSource
 * ***********************************************************************************
 * v0.04.0032 2025-10-06 Original draft (N Tolleshaug)
 *			  2025-10-07 Updated save enable for edit action (N Tolleshaug)
 *			  2025-10-12 Partly updated for Add/Update source  (N. Tolleshaug)
 *			  2025-11-11 Include rep link data in repo data table (D Ferguson)
 *			  2025-11-16 Moved col head read to EditSource (N. Tolleshaug)
 *************************************************************************************
 * Notes for incomplete code still requiring attention
 * NOTE01 allow saving of the Source's data
 * NOTE02 load Author/Editor/Compiler data
 * NOTE06 handle add/delete of source of source and repositories
 *
 ************************************************************************************/

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;

/**
 * Update(Edit) Source
 * @author N. Tolleshaug
 * @version v0.04.0032
 * @since 2025-10-06
 */
public class HG0566UpdateSource extends HG0566EditSource {
	private static final long serialVersionUID = 1L;
	HBPersonHandler pointPersonHandler;
	public HG0566UpdateSource(HBProjectOpenData pointOpenProject, long sourcePID, HG0565ManageSource pointManageSource) {
		super(pointOpenProject, sourcePID);
		this.pointOpenProject = pointOpenProject;
		pointPersonHandler = pointOpenProject.getPersonHandler();
		pointCitationSourceHandler = pointOpenProject.getCitationSourceHandler();
		pointRepositoryHandler = pointOpenProject.getRepositoryHandler();
		this.sourceTablePID = sourcePID;
		setTitle("Edit Source");
		setResizable(false);
	// Setup references for HG0450
		windowID = screenID;
		helpName = "editsource";

	// Start loading data required for this screen
		// Load the Source Element list (names/ID#s) as we need it for Source template conversion & checking
		try {
			tableSrcElmntData = pointCitationSourceHandler.getSourceElmntList(HGlobal.dataLanguage);
		} catch (HBException hbe) {
			System.out.println( " Error loading Source Element list: " + hbe.getMessage());
			hbe.printStackTrace();
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
 			System.out.println( " Error loading Source data: " + hbe.getMessage());
 			hbe.printStackTrace();
 		}

 		// Get the Source Defn list (to be able to get the type's name by matching the PID)
		try {
			sorcDefnTable = pointCitationSourceHandler.getSourceDefnList();
		} catch (HBException hbe) {
			System.out.println( " Error loading source defn list: " + hbe.getMessage());
			hbe.printStackTrace();
		}

		// and sort it as we need it in sorted order for its combobox
		Arrays.sort(sorcDefnTable, (row1, row2) -> ((String) row1[0]).compareTo((String) row2[0]));

 		// Get the 3 underlying Source Defn templates using the sorcDefnPID value
		sourceDefnPID = (long)sourceEditData[11];

		try {
			sorcDefnTemplates = pointCitationSourceHandler.getSourceDefnTemplates(sourceDefnPID);
		} catch (HBException hbe) {
			System.out.println( " Error loading source defn templates: " + hbe.getMessage());
			hbe.printStackTrace();
		}

		// Get the Source Element data values belonging to this Source
		try {
			tableSourceElmntDataValues = pointCitationSourceHandler.getSourceElmntDataValues(sourcePID);
		} catch (HBException hbe) {
			System.out.println( " Error loading source element values: " + hbe.getMessage());
			hbe.printStackTrace();
		}

		// Get any Source of Source citation data for this Source
		try {
			citnSrcSrcData = pointCitationSourceHandler.getCitationSourceData(sourcePID, "T736"); //$NON-NLS-1
		} catch (HBException hbe) {
			System.out.println( " Error loading source of source data: " + hbe.getMessage());
			hbe.printStackTrace();
		}

		// Get any Repository data for this Source
		// First get the PIDs of all Repos connected to this Source via the T740 link table
		try {
			repoLinkData = pointRepositoryHandler.getRepoLinkData(sourcePID);
		} catch (HBException hbe) {
			System.out.println( " Error loading repository link data: " + hbe.getMessage());
			hbe.printStackTrace();
		}
		// Then get the data for each of the Repos found in repoPIDs
		if (repoLinkData.length > 0) {
			tableRepoData = new Object[repoLinkData.length][5];
			for (int i=0; i < repoLinkData.length; i++) {
				repoEditData = new Object[3];
				try {
					repoEditData = pointRepositoryHandler.getRepositoryData((long) repoLinkData[i][0]);
				} catch (HBException hbe) {
				System.out.println( " Error loading repository link data: " + hbe.getMessage());
				hbe.printStackTrace();
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

		// Listener for Save button edit or update source
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: saving data in HG0566UpdateSource");	//$NON-NLS-1$
				//System.out.println(" Save - Updated source data!" );
				storeData(); // Collect the GUI data
				try {
			// Update source record
					pointCitationSourceHandler.updateSourceRecord(sourceTablePID, sourceStoreData);
			// Udate source element data
					pointCitationSourceHandler.updateSourceElementDataRecords(sourceTablePID);
			// Reset source table
					pointManageSource.resetSourceTable();
				} catch (HBException hbe) {
					System.out.println(" Update Source Error: " + hbe.getMessage());
					hbe.printStackTrace();
				}

				// NOTE01 save any changed data - use the xxxEdited booleans to check text edits

				// When saving source templates use the convertNamesToNums routine with the textToCodeMap hashmap
				// to check all Element names exist and can be converted back to Element numbers, and do the
				// conversion back to the numbered version for saving.
				// It throws error msg and returns null, so if null returned, do not save!
				// Example usage using fullFoot text, with before/after console routines:
				//    System.out.println("Input="+fullFootText.getText());
				//    String fullFootToSave = pointReportHandler.convertNamesToNums(fullFootText.getText(), textToCodeMap);
				//    System.out.println("Output="+fullFootToSave);
				// then check for null response and do NOT dispose! (leave user to fix it and try again!)
				pointEditSource.dispose();
			}
		});

	} // End HG0566UpdateSource constructor

}
