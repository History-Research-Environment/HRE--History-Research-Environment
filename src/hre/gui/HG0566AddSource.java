package hre.gui;
/**************************************************************************************
 * HG0566AddSource extends HG0566EditSource
 * ***********************************************************************************
 * v0.04.0032 2025-10-06 Original draft (N Tolleshaug)
 *			  2025-10-07 Populate element table for Add Source (N Tolleshaug)
 *			  2025-10-07 Updated save enable for edit action (N Tolleshaug)
 *			  2025-10-12 - Partly updated for Add/Update source  (N. Tolleshaug)
 *			  2025-10-16 - Updated warning when save (N. Tolleshaug)
 *************************************************************************************
 * Notes for incomplete code still requiring attention
 * NOTE01 allow saving of the Source's data
 * NOTE02 load Author/Editoer/Compiler data
 * NOTE06 handle add/delete of source of source and repositories
 * NOTE07 make Preview buttons work on footnotes/biblio data
 *
 ************************************************************************************/
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
/**************************************************************************************
 * HG0566AddSource extends HG0566EditSource
 * ***********************************************************************************
 * v0.04.0032 2025-10-06 Original draft (N Tolleshaug)
 *
 *************************************************************************************
 * Notes for incomplete code still requiring attention
 * NOTE01 allow saving of the Source's data
 * NOTE02 load Author/Editoer/Compiler data
 * NOTE06 handle add/delete of source of source and repositories
 * NOTE07 make Preview buttons work on footnotes/biblio data
 *
 ************************************************************************************/
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JOptionPane;

import hre.bila.HB0711Logging;
import hre.bila.HBException;
import hre.bila.HBProjectOpenData;
/**
 * Add Source
 * @author N. Tolleshaug
 * @version v0.04.0032
 * @since 2025-10-06
 */
public class HG0566AddSource extends HG0566EditSource {
	private static final long serialVersionUID = 1L;
	private static long sourcePID;

	public HG0566AddSource(HBProjectOpenData pointOpenProject, HG0565ManageSource pointManageSource) {
		super(pointOpenProject, sourcePID);
		this.pointOpenProject = pointOpenProject;
		pointPersonHandler = pointOpenProject.getPersonHandler();
		pointCitationSourceHandler = pointOpenProject.getCitationSourceHandler();
		setTitle("Add Source");
		setResizable(false);
	// Setup references for HG0450
		windowID = screenID;
		helpName = "editsource";
		
/**************************************************
// Start loading all data required for this screen
 **************************************************/
	    // Collect static GUI text from T204 for all tables
		tableSrcElmntValueHeads =
				pointPersonHandler.setTranslatedData("56600", "1", false);	// Source Element, Value //$NON-NLS-1$ //$NON-NLS-2$
		tableSrcSrcColHeads =
				pointPersonHandler.setTranslatedData("56600", "2", false); // ID, Sources (abbrev.)  //$NON-NLS-1$ //$NON-NLS-2$
		tableRepoColHeads =
				pointPersonHandler.setTranslatedData("56600", "3", false); // ID, Repository  //$NON-NLS-1$ //$NON-NLS-2$

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
		try {
			sorcDefnTemplates = pointCitationSourceHandler.getSourceDefnTemplates(sourceDefnPID);
		} catch (HBException hbe) {
			System.out.println( " Error loading source defn templates: " + hbe.getMessage());
			hbe.printStackTrace();
		}
		
/*********************************'  
 * Code to be used later
 **********************************
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
			repoPIDs = pointCitationSourceHandler.getRepoPIDs(sourcePID);
		} catch (HBException hbe) {
			System.out.println( " Error loading repository PIDs: " + hbe.getMessage());
			hbe.printStackTrace();
		}
		// Then get the data for each of the Repos found in repoPIDs
		if (repoPIDs.length > 0) {
			tableRepoData = new Object[repoPIDs.length][2];
			for (int i=0; i < repoPIDs.length; i++) {
				repoEditData = new Object[3];
				try {
					repoEditData = pointCitationSourceHandler.getRepoEditData(repoPIDs[i]);
				} catch (HBException hbe) {
				System.out.println( " Error loading repository data: " + hbe.getMessage());
				hbe.printStackTrace();
				}
				if (repoEditData != null) {
					// Save the data we need for the Repository table
					tableRepoData[i][0] = repoEditData[0];  // the repo ref#
					tableRepoData[i][1] = repoEditData[1];  // the repo abbrev
				}
			}
		}
*/
		
	// Set up data and screen
		screenLayout();
		createActionListner();
		btn_Save.setEnabled(false);
		activChkBox.setSelected(true);
		
		for (int j = 0; j < sorcDefnTable.length; j++) 
			comboSourceTypes.addItem((String) sorcDefnTable[j][0]);
		
		// Listener for Save button - adding new source
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: saving new source in HG0566AddSource");	//$NON-NLS-1$
				//System.out.println(" Save - Add new source!" );
				storeData(); // Collect the GUI data
				try {
					if (titleTextChanged && abbrevTextChanged) {
						pointCitationSourceHandler.createSourceRecord(sourceStoreData);	
		// Add new element data records
						pointCitationSourceHandler.createSourceElementDataRecords(true);
		// Reset source table
						pointManageSource.resetSourceTable();
					 } else {
						//System.out.println(" Edit title and abbrev first! ");
						String message = " Cannot add source! \nSource must have Title and Abbbrev set! ";
						JOptionPane.showMessageDialog(pointEditSource, message,	
								"Add Source warning!", JOptionPane.INFORMATION_MESSAGE);
					 }
				} catch (HBException hbe) {
					System.out.println(" Create Source Error: " + hbe.getMessage());
					hbe.printStackTrace();
				}
				// NOTE01 save any changed data - use the xxxEdited booleans to check text edits

				// When saving source templates use the convertNamesToNums routine with the textToCodeMap hashmap
				// to check all Element names exist and can be converted back to Element numbers, and do the
				// conversion back to the numbered version for saving.
				// It throws error msg and returns null, so if null returned, do not save!
				// Example usage using fullFoot text, with before/after console routines:
				//    System.out.println("Input="+fullFootText.getText());
				//    String fullFootToSave = HGlobalCode.convertNamesToNums(fullFootText.getText(), textToCodeMap);
				//    System.out.println("Output="+fullFootToSave);
				// then check for null response and do NOT dispose! (leave user to fix it and try again!)
				pointEditSource.dispose();

			}
		});
	}
}
