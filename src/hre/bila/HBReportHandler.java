package hre.bila;

/************************************************************************************************
 * Class  HBReportHandler extends BusinessLayer
 * Processes data for report/citation/source output handling
 * Receives requests from User GUI to action methods
 * Sends requests to database over Database Layer API
 ************************************************************************************************
 * v0.04.0032 2025-11-03 - First draft (D Ferguson)
 *			  2025-11-03 - Routine for parsing Source templates for Citations (D Ferguson)
 *			  2025-11-14 - Add ex-HGlobalcode Source Element name/number conversion (D Ferguson)
 *			  2025-12-22 - convertNamesToNums modified to return HBException(N. Tolleshaug)
 *			  2026-01-27 - Line 43 - pointHREmemo = pointOpenProject.getHREmemo();(N. Tolleshaug)
 *			  2026-02-21 - Added preliminary methods for sentence preload (N. Tolleshaug)
 * v0.05.0033 2026-03-08 - Added class ReportEventData extends HBBusinessLayer (N. Tolleshaug)
 * 			  2026-03-17 - Changed class name from ReportEventData to ReportEventTMG (N. Tolleshaug)
 * 			  2026-03-23 - Handle escape char in parseFootnoteBiblio (D Ferguson)
 * 			  2026-03-27 - Handle escape char in validateBrackets & convertNamesToNums (D Ferguson)
 * 			  2026-03-28 - Updated sentence varible ouput from database (N. Tolleshaug)
 * 			  2026-04-05 - Updated sentence varible ouput from database (N. Tolleshaug)
 * 			  2026-04-12 - Updated sentence variable processing and Javadoc output (N. Tolleshaug)
 * 			  2026-04-17 - Improved T401 recalculation and table update (N. Tolleshaug)
 * 			  2026-04-18 - Relationship code calculation completed (D Ferguson/N. Tolleshaug)
 * 			  2026-04-20 - Added code for clear relation and focus person (N. Tolleshaug)
 * 			  2026-05-01 - Updated findRelationships to return 2 relate code pairs (D Ferguson)
 * 			  2026-05-03 - Completed code for clear all T401 relation and focus person (N. Tolleshaug)
 * 			  2026-06-04 - Improving sentence parser (N. Tolleshaug)
 * 			  2026-06-12 - Added findLocalSentenceSetPID(long eventTablePID) (N. Tolleshaug)
 * 			  2026-06-27 - Added handling of P2 for associates (N. Tolleshaug)
 * v0.05.0034 2026-03-08 - Removed V22c reference to T450 ASSOC_SENTENCE field (N. Tolleshaug)
 * 			  2026-07-04 - Change tableSourceElmntDataValues to Object for new T734 fields (D Ferguson)
 * 			  2026-07-25 - Refactor code change (N. Tolleshaug)
 * 			  2026-07-28 - Improved handling of partner events (N. Tolleshaug)
 * 			  2026-07-30 - Added ? + sentence variable (?P) for not found (N. Tolleshaug)
 * 			  2026-08-03 - Added name sentence variable output (N. Tolleshaug)
 * 			  2026=08-06 - Split off RelationCalc into HBRelationHandler (D Ferguson)
 * 			  2026=08-08 - Added handling of "S" sentence variables for associates (N. Tolleshaug)
 * 			  2026-08-30 - Prepare preferred name presentation(N. Tolleshaug)
 * 			  2026-09-07 - Add handling of TMG's [:TAB:] (D Ferguson)
 * 			  2026-09-07 - Improved exception handling and error reports (N. Tolleshaug)
 *			  2026-09-08 - Fixed error in findSubjectNameTablePID (N. Tolleshaug)
 *********************************************************************************************/

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hre.gui.HG0547EditEvent;
import hre.gui.HGlobal;
import hre.nls.HGlobalMsgs;

public class HBReportHandler extends HBBusinessLayer {

	long null_RPID  = 1999999999999999L;
	long proOffset  = 1000000000000000L;

	HBProjectOpenData pointOpenProject;
	HBRepositoryHandler pointRepositoryHandler;
	HBNameStyleManager pointLocationStyleData;
	HBReportHandler pointReportHandler = this;

	HREmemo pointHREmemo;
	public ReportEventTMG pointReportEventTMG;
	public ReportNameTMG pointReportNameTMG;
	int dataBaseIndex = -1;

	String[] locationNameCodeArray, personNameCodeArray;
	String personNameCodes = "1100|2000|3000|5000|5200|5700|3700|5500|3300|";
	String personNameFields ="Title|Prefix|GivenName|PreSurname|Surname|Suffix|OtherName|SortSurname|SortGiven|";
	String locationNameCodes = "0500|1100|3000|3100|3400|3500|3900|0100|4100|4300|";
	String locationNameFields = "Addressee|Detail|City|County|State|Country|Postal|Phone|LatLong|Temple|";

/**
 * public ReportEventTMG createReportEventData(long eventTablePID)
 * @param eventTablePID
 * @return
 */
	public ReportEventTMG createReportEventData(long eventTablePID, long ownerTablePID, int ownerType) {
		try {
		// Set up the element code arrays
			if (eventTablePID == null_RPID || eventTablePID == 0) {
				System.out.println(" reateReportEventData ERROR - EventTable PID == null_RPID/0");
				return null;
			}
			personNameCodeArray = personNameCodes.split("\\|");
			locationNameCodeArray = locationNameCodes.split("\\|");
			pointReportEventTMG = new ReportEventTMG(pointOpenProject, eventTablePID, ownerTablePID, ownerType);
			pointReportNameTMG = null;
			return pointReportEventTMG;
		} catch (HBException hbe) {
			System.out.println(" HBReportHandler - ReportEventData - " + hbe.getMessage());
			return null;
		}
	}

/**
 * public ReportNameTMG createReportNameData(long nameTablePID)
 * @param nameTablePID
 * @return
 */
	public ReportNameTMG createReportNameData(long nameTablePID) {
		try {
			pointReportNameTMG = new ReportNameTMG(pointOpenProject, nameTablePID);
			pointReportEventTMG = null;
		} catch (HBException hbe) {
			System.out.println(" HBReportHandler - ReportNameData. " + hbe.getMessage());
			hbe.printStackTrace();
		}
		return null;
	}

/**
 * Constructor HBReportHandler
 */
	HBReportHandler(HBProjectOpenData pointOpenProject) {
		super();
		this.pointOpenProject = pointOpenProject;
		pointDBlayer = pointOpenProject.getPointDBlayer();
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		pointHREmemo = pointOpenProject.getHREmemo();
		pointRepositoryHandler = pointOpenProject.getRepositoryHandler();
	}

/**
 * parseFootnoteBiblio - converts footnote/bibliography templates to HTML display form
 * @param inputText	- sourcetemplate text to be converted
 * @param sourcePID - PID of the source concerned
 * @param sourceMemo - memo of the Source concerned
 * @param citationParts - array of the 3 citation fields (empty when parsing a Sourcetemplate)
 * @return output - text in HTML format with formatting applied
 */
	public String parseFootnoteBiblio(String inputText, long sourcePID, String sourceMemo,
									  Object[][] objectSourceElmntDataValues, String[] citationParts) {

		// Break out the citationParts parameter to its items
		String citationRefer = citationParts[0];
		String citationDetail = citationParts[1];
		String citationMemo = citationParts[2];

		// Define token workareas (150 entries should be enough)
		String[] tokensPh2 = new String[150];
		String[] tokensPh3 = new String[150];
		// and return string
		String output = "";		//$NON-NLS-1$
		// and token counters
		int tokenNumPh2 = 0;
		int tokenNumPh3 = 0;
		// and work string
		String workText = ""; //$NON-NLS-1$
		// and memo-splitting string arrays
		Boolean srcMemoNotProcessed = true, cdNotProcessed = true, cmNotProcessed = true, repoNotProcessed = true;
		String[] sourceMemoParts = null;
		String[] citationDetailParts = null;
		String[] citationMemoParts = null;
		String[] repositoryMemoParts = null;
		// and repository data parts
		Object[] repoLinkData;
		Object[] repoData;
		long repoPID;
		String repositoryName = "", repositoryReference = "", repositoryAddress = "";

		// If no valid input, return nothing
		if (inputText == null || inputText.isEmpty() || sourcePID == null_RPID) {
			output = "";	//$NON-NLS-1$
			return output;
		}

	/****************************
	 * PHASE 1 - clean the input
	 ***************************/
		// Both TMG/HRE templates and HTML use < > markers for different purposes, so we need to replace all
		// such markers in the input string to enable use of HTML formatting codes.
		// We have chosen to replace the template < and > markers with {{ and }} - we do this now.
		String markers = inputText.replace("<", "{{").replace(">", "}}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		// The format of the input string can include TMG formatting values which should never be
		// part of citations. These are [SCAP:] [INDEX:] [SIZE:] and need to be removed.
		// Next, all the other [xxx:] and [:xxx} codes need to be converted to their
		// HTML equivalents.
		// To do this we use an iterative regex.
		Map<String, String> repl = new HashMap<>();
		repl.put("[BOLD:]", "<b>"); 		//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[:BOLD]", "</b>"); 		//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[ITAL:]", "<i>"); 		//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[:ITAL]", "</i>"); 		//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[UND:]", "<u>"); 			//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[:UND]", "</u>"); 		//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[SUP:]", "<sup>"); 		//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[:SUP]", "</sup>"); 		//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[SUB:]", "<sub>"); 		//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[:SUB]", "</sub>"); 		//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[HID:]", "<!"); 			//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[:HID]", "->"); 			//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[CAP:]", "<p style=\"text-transform: uppercase;\">"); //$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[:CAP]", "</p>"); 		//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[HTML:]", ""); 			//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[:HTML]", ""); 			//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[WEB:]", ""); 			//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[:WEB]", ""); 			//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[EMAIL:]", "<a href=mailto:"); //$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[:EMAIL]", "></a>"); 		//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[INDEX:]", ""); 			//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[:INDEX]", ""); 			//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[SIZE:]", ""); 			//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[:SIZE]", ""); 			//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[SCAP:]", ""); 			//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[:SCAP]", ""); 			//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[:TAB:]", "&emsp"); 			//$NON-NLS-1$ //$NON-NLS-2$  // NB: this enters 4 spaces (std tab width)
		Pattern pattern1 = Pattern.compile
			("\\[BOLD:\\]|\\[:BOLD\\]|\\[ITAL:\\]|\\[:ITAL\\]|\\[UND:\\]|\\[:UND\\]|\\[SUP:\\]|\\[:SUP\\]|\\[SUB:\\]|\\[:SUB\\]|\\[HID:\\]|\\[:HID\\]|\\[CAP:\\]|\\[:CAP\\]|\\[HTML:\\]|\\[:HTML\\]|\\[WEB:\\]|\\[:WEB\\]|\\[EMAIL:\\]|\\[:EMAIL\\]|\\[INDEX:\\]|\\[:INDEX\\]|\\[SIZE:\\]|\\[:SIZE\\]|\\[SCAP:\\]|\\[:SCAP\\]"); //$NON-NLS-1$
		Matcher matcher1 = pattern1.matcher(markers);
		StringBuffer buffer1 = new StringBuffer();
		while(matcher1.find())
		    matcher1.appendReplacement(buffer1, repl.get(matcher1.group()));
		matcher1.appendTail(buffer1);
		workText = buffer1.toString();

		//System.out.println("End Phase 1: " + workText);

	/******************************
	 * PHASE 2 - tokenize the input
	 ******************************/
		// In Phase 2 we want to break the revised input string into an array of 'tokens',
		// each containing just 1 component of the input string.
		// So we setup a regex to break the text string into 'tokens' in tokensPh2[].
        // This regex will extract each string enclosed in [ ] OR {{ }} OR plain text OR \x sequences
        // note that {{ }} strings will enclose more [ ] strings - process them in Phase 3.
		String regex2 =
		        "\\\\."                 // \X  → escaped sequence as its own token
		      + "|\\[[^\\\\\\]]*\\]"    // [ ... ] but stop before '\' or ']'
		      + "|\\{\\{[^\\\\}]*\\}\\}"// {{ ... }} but stop before '\' or '}'
		      + "|[^\\\\\\[\\]\\{\\}]+";// plain text, no '\', '[', ']', '{', '}'
		Pattern pattern2 = Pattern.compile(regex2);
        Matcher matcher2 = pattern2.matcher(workText);

        while (matcher2.find()) {
            tokensPh2[tokenNumPh2] = matcher2.group();
            tokenNumPh2++ ;
        }

        // Display tokensPh2 for checking
  		//System.out.println("End Phase 2:");
  		//for (int i = 0; i < tokenNumPh2; i++) {
  		//	System.out.println("tokensPh2[" + i + "] = " + tokensPh2[i]);
  		//}

	/**********************************************************
	 * PHASE 3 - remove empty tokens and split apart < > tokens
	 **********************************************************/
        // This phase transfers all tokens to tokensPh3, ignoring null/empty tokens and tokens wrapped in {{ }} (for now).
        for (int i=0; i < tokenNumPh2; i++) {
        	if (tokensPh2[i].trim().length() > 0 && tokensPh2[i] != null && !tokensPh2[i].startsWith("{{")) { //$NON-NLS-1$
        		tokensPh3[tokenNumPh3] = tokensPh2[i];
        		tokenNumPh3++;
        	}
            // Now break apart all {{ }} entries in tokensPh2 to expose [  ] entries they
        	// will contain and load them into tokensPh3 as separate tokens
        	if (tokensPh2[i].startsWith("{{")) { //$NON-NLS-1$
        		// Remove the {{ }} characters from beginning/end of this token
    			workText = tokensPh2[i].substring(1, tokensPh2[i].length() - 1);
    			// add the {{ into tokensPh3
    			tokensPh3[tokenNumPh3] = "{{"; //$NON-NLS-1$
    			tokenNumPh3++;
    			// Break apart the {{ }} contents using the previous regex pattern (now
    			// applied to workText) and add these into tokensPh3.
    			Matcher matcher3 = pattern2.matcher(workText);
                while (matcher3.find()) {
                	tokensPh3[tokenNumPh3] = matcher3.group();
                    tokenNumPh3++ ;
                }
    			// add the ending }}
    			tokensPh3[tokenNumPh3] ="}}"; //$NON-NLS-1$
    			tokenNumPh3++;
        	}
        }

        // Display tokensPh3 for checking
  		//System.out.println("End Phase 3:");
  		//for (int i = 0; i < tokenNumPh3; i++) {
  		//	System.out.println("tokensPh3[" + i + "] = " + tokensPh3[i]);
  		//}

	/***************************************************
	 * PHASE 4 - substitute [  ] fields with real values
	 ***************************************************/
  		// At this stage substitute the [nnnnn] tokens with their real values
  		// which are available in tableSourceElmntDataValues[number][value].
 		for (int i = 0; i < tokenNumPh3; i++) {
 			// Find [nnnnn] tokens and extract the 'nnnnn'
  			if (tokensPh3[i].startsWith("[") && tokensPh3[i].length() == 7) {		//$NON-NLS-1$
  				String elmntNumber = tokensPh3[i].substring(1, 6);
  				// Match the 'nnnnn' with a tableSourceElmntDataValues entry to get its value
  				for (int j = 0; j < objectSourceElmntDataValues.length; j++) {
  					if (elmntNumber.equals(objectSourceElmntDataValues[j][0])) {
  						tokensPh3[i] = (String)objectSourceElmntDataValues[j][1];
  						break;
  					}
  				}
  			}
  		}
 		// Now consider whether we still have [nnnnn] tokens left to be handled.
 		// These can be:
 		//	[REPOSITORY xxx ] entries in the 40000-400002 number range
 		//  [COMMMENTS} = number 51000, ALL data from Source MEMO (TEXT field)
 		//  [M] or [Mn] = numbers 50000-500009, data from Source MEMO
 		//  [CD} or [CDn] = number 60000-60009, data from Citation DETAIL
 		//  [CREF} = number 61000, data from Citation Reference
 		//  [CM] or [CMn] = number 70000-70009, data from Citation MEMO
 		//  [RM] or [RMn] = number 80000-80009, data from Repository MEMO
 		// NOTE: [COMMENTS] is the same thing as [M], i.e. the whole of the Source Memo (aka Source Text)
 		// NOTE: in TMG [CD1] is the same as [CD] i.e., the 'parts' counter starts at 1, not 0.
 		// This means the 'parts' number needs to be corrected when processing [XXx] tokens.

 		// On first pass, look for any CDn, Mn. RMn, CMn tokens that may
 		// require breaking apart their respective memo strings at the || marker
 		// For repositories, this requires getting the source/repo link data to find the repository.
		for (int i = 0; i < tokenNumPh3; i++) {
			// For Source Memo
			if (tokensPh3[i].startsWith("[500") && srcMemoNotProcessed) {	//$NON-NLS-1$
				sourceMemoParts = sourceMemo.split("\\|\\|");				//$NON-NLS-1$
				srcMemoNotProcessed = false;
			}
			// For Citation Detail
			if (tokensPh3[i].startsWith("[600") && cdNotProcessed)	{	//$NON-NLS-1$
				citationDetailParts = citationDetail.split("\\|\\|");	//$NON-NLS-1$
				cdNotProcessed = false;
			}
			// For Citation Memo
			if (tokensPh3[i].startsWith("[700") && cmNotProcessed)	{	//$NON-NLS-1$
				citationMemoParts = citationMemo.split("\\|\\|");		//$NON-NLS-1$
				cmNotProcessed = false;
			}
			// For repository data including its memo
			if ((tokensPh3[i].startsWith("[400") || 			//$NON-NLS-1$
					tokensPh3[i].startsWith("[800]"))			//$NON-NLS-1$
						&& repoNotProcessed) {
				repoNotProcessed = false;
				try {
				// Get the source/repo link data (and repo ref)
					repoLinkData = pointRepositoryHandler.getLinkedRepository(sourcePID);
					repoPID = (long) repoLinkData[0];
					repositoryReference = (String) repoLinkData[1];
				// If a valid repoPID, get the nominated repository data
					if (repoPID != null_RPID) {
						repoData = pointRepositoryHandler.getRepositoryData(repoPID);
						repositoryName = (String) repoData[0];
						String repoMemo = (String) repoData[3];
						repositoryMemoParts = repoMemo.split("\\|\\|");		//$NON-NLS-1$
						repositoryAddress = (String) repoData[4];
					}
					else {
						repositoryName = "";	//$NON-NLS-1$
						String repoMemo = " ";	//$NON-NLS-1$    // dummy repoMemo
						repositoryMemoParts = repoMemo.split("\\|\\|");		//$NON-NLS-1$
						repositoryAddress = "";	//$NON-NLS-1$
					}
				} catch (HBException hre) {
					System.out.println(" HG0555EditCitation get repo data error: " + hre.getMessage()); //$NON-NLS-1$
					hre.printStackTrace();
				}
			}
		}

		// Now run through the tokens again and complete all substitutions
		int tokenNum = 0;
		for (int i = 0; i < tokenNumPh3; i++) {
			if (tokensPh3[i].equals("[40000]")) tokensPh3[i] = repositoryName;			//$NON-NLS-1$
			if (tokensPh3[i].equals("[40001]")) tokensPh3[i] = repositoryAddress; 		 //$NON-NLS-1$
			if (tokensPh3[i].equals("[40002]")) tokensPh3[i] = repositoryReference;		//$NON-NLS-1$
			if (tokensPh3[i].equals("[51000]")) tokensPh3[i] = sourceMemo;				//$NON-NLS-1$
			if (tokensPh3[i].equals("[61000]")) tokensPh3[i] = citationRefer;			//$NON-NLS-1$
			if (tokensPh3[i].startsWith("[600")) {			//$NON-NLS-1$
				// Convert the Element number to an integer in range 0-8 (as TMG uses 1-9)
				tokenNum = Integer.parseInt(tokensPh3[i].substring(1, 6)) - 60000;
				if (tokenNum > 0) tokenNum= tokenNum - 1;
				// and test that the split text has that number of parts
				if (citationDetailParts.length <= tokenNum) break;
				// then get the applicable part of the split-apart Citation Detail string
				tokensPh3[i] = citationDetailParts[tokenNum];
			}
			// Repeat above code for 50000 entries (Source Memo)
			if (tokensPh3[i].startsWith("[500")) {			//$NON-NLS-1$
				tokenNum = Integer.parseInt(tokensPh3[i].substring(1, 6)) - 50000;
				if (tokenNum > 0) tokenNum= tokenNum - 1;
				if (sourceMemoParts.length <= tokenNum) break;
				tokensPh3[i] = sourceMemoParts[tokenNum];
			}
			// Repeat above code for 70000 entries (Citation Memo)
			if (tokensPh3[i].startsWith("[700")) {			//$NON-NLS-1$
				tokenNum = Integer.parseInt(tokensPh3[i].substring(1, 6)) - 70000;
				if (tokenNum > 0) tokenNum= tokenNum - 1;
				if (citationMemoParts.length <= tokenNum) break;
				tokensPh3[i] = citationMemoParts[tokenNum];
			}
			// Repeat above code for 80000 entries (Repo Memo)
			if (tokensPh3[i].startsWith("[800")) {			//$NON-NLS-1$
				tokenNum = Integer.parseInt(tokensPh3[i].substring(1, 6)) - 80000;
				if (tokenNum > 0) tokenNum= tokenNum - 1;
				if (repositoryMemoParts.length <= tokenNum) break;
				tokensPh3[i] = repositoryMemoParts[tokenNum];
			}
		}

        // Display tokensPh3 for checking
  		//System.out.println("End Phase 4:");
  		//for (int i = 0; i < tokenNumPh3; i++) {
  		//	System.out.println("tokensPh3[" + i + "] = " + tokensPh3[i]);
  		//}

	/***************************************************
	 * PHASE 5 - process within the {{ }} markers
	 ***************************************************/
   		// Now check the substitutions of tokens enclosed in the {{ }} markers.
		// If there are unsubstituted tokens then the complete content
		// of these markers needs to be removed from the output.
    	// First, look for a start marker
    	int startMarker = 0;
    	int stopMarker = 0;
		for (int i = 0; i < tokenNumPh3; i++) {
			if (tokensPh3[i].startsWith("{{")) { //$NON-NLS-1$
				startMarker = i;
				// Once "{{" is found, look for a "}}"
				for (int j = i; j < tokenNumPh3; j++) {
					if (tokensPh3[j].startsWith("}}")) { //$NON-NLS-1$
						stopMarker = j;
						break;
					}
				}
				// Now check within the range of the start/stopMarkers for a  [  ] token.
				// If one exists, it hasn't been substituted, so delete the whole {{ }} range.
				for (int k = startMarker+1; k < stopMarker; k++) {
					if (tokensPh3[k].startsWith("[")) { //$NON-NLS-1$
						for (int x = startMarker; x < stopMarker+1; x++) {
							tokensPh3[x] = ""; //$NON-NLS-1$
						}
					}
				}
			}
		}

        // Display tokensPh3 for checking
  		//System.out.println("End Phase 5:");
  		//for (int i = 0; i < tokenNumPh3; i++) {
  		//	System.out.println("tokensPh3[" + i + "] = " + tokensPh3[i]);
  		//}

	/***************************************************
	 * PHASE 6 - look for brackets containing nothing
	 ***************************************************/
	// Now check for brackets containing no good info and clean out the invalid info
	// First, look for a start (
		boolean bracketsFound = false;
  	   	startMarker = 0;
    	stopMarker = 0;
		for (int i = 0; i < tokenNumPh3; i++) {
			if (tokensPh3[i].trim().equals("(")) { 	//$NON-NLS-1$
				bracketsFound = true;
				startMarker = i;
				// Once "(" is found, look for a ")"
				for (int j = i; j < tokenNumPh3; j++) {
					if (tokensPh3[j].trim().startsWith(")")) { 	//$NON-NLS-1$
						stopMarker = j;
						break;
					}
				}
				// Now check within the range of the start/stopMarkers for a  [  ] token, followed
				// by punctuation and blank them both out
				for (int k = startMarker+1; k < stopMarker; k++) {
					if (tokensPh3[k].trim().startsWith("[")  && (tokensPh3[k+1].trim().equals(":") ||	//$NON-NLS-1$ //$NON-NLS-2$
						tokensPh3[k+1].trim().equals(";") || tokensPh3[k+1].trim().equals(",") ||		//$NON-NLS-1$ //$NON-NLS-2$
						tokensPh3[k+1].trim().equals("/") ) ) { 										//$NON-NLS-1$
							tokensPh3[k] = ""; 		//$NON-NLS-1$
							tokensPh3[k+1] = "";	//$NON-NLS-1$
						}
					// or an unsubstituted token before the last bracket
					if (tokensPh3[k].trim().startsWith("[") && tokensPh3[k+1].trim().startsWith(")") )	//$NON-NLS-1$ //$NON-NLS-2$
						tokensPh3[k] = "";		//$NON-NLS-1$
					// or just a spare unsubstituted token
					if (tokensPh3[k].trim().startsWith("[") )		//$NON-NLS-1$
						tokensPh3[k] = "";		//$NON-NLS-1$
				}
			}
		}
	// Now if we are in brackets mode, pass through again and if all tokens between
    // brackets are empty, remove the brackets
		if (bracketsFound ) {
			boolean allBlank = true;
			for (int k = startMarker+1; k < stopMarker; k++) {
				if (!tokensPh3[k].trim().equals("")) allBlank = false;		//$NON-NLS-1$
			}
			if (allBlank) {		// remove the brackets
				tokensPh3[startMarker] = "";	//$NON-NLS-1$
				tokensPh3[stopMarker] = tokensPh3[stopMarker].substring(1);
			}
		}

        // Display tokensPh3 for checking
  		//System.out.println("End Phase 6:");
  		//for (int i = 0; i < tokenNumPh3; i++) {
  		//	System.out.println("tokensPh3[" + i + "] = " + "/" + tokensPh3[i] +"/") ;
  		//}

	/****************************************************
	 * PHASE 7 - build final output string
	 ****************************************************/
        // Build the tokens into our workText string, ignoring {{, }} and blank ones
  		// Also ignore unsubstituted/dead tokens and any punctuation after them.
		// Also strip out escape characters but leave the escape target
		workText = ""; //$NON-NLS-1$
        for (int i=0; i < tokenNumPh3; i++) {
        	if (tokensPh3[i].trim().startsWith("[")) {						//$NON-NLS-1$
        		tokensPh3[i] = "";											//$NON-NLS-1$
        		// check first letter  of token after a dead one - if its punctuation, delete it as well
        		if (i < tokenNumPh3) {
        			if (tokensPh3[i].length() > 0) {	// check it isn't blank
        				char c = tokensPh3[i+1].charAt(0);
        				if (!Character.isLetterOrDigit(c)) tokensPh3[i+1] = "";	//$NON-NLS-1$
        			}
        		}
        	}
        	// Remove dead brackets
        	if (tokensPh3[i].contains("{{")) tokensPh3[i] = "";				//$NON-NLS-1$ //$NON-NLS-2$
           	if (tokensPh3[i].contains("}}")) tokensPh3[i] = "";				//$NON-NLS-1$ //$NON-NLS-2$
        	// Remove escape characters
        	if (tokensPh3[i].startsWith("\\")) tokensPh3[i] = tokensPh3[i].substring(1);
        	// Add token to final string
        	if (!tokensPh3[i].equals("")) workText = workText + tokensPh3[i] ; //$NON-NLS-1$
        }

        // Now clean the output of double blanks, blanks before/after punctuation etc
        output = workText;
	    Map<String, String> clean = new LinkedHashMap<>();
		clean.put("  ", " ");		// change double blank to blank //$NON-NLS-1$ //$NON-NLS-2$
		clean.put("( ", "(");		// remove blank after ( 		//$NON-NLS-1$ //$NON-NLS-2$
		clean.put(" )", ")");		// remove blank before ) 		//$NON-NLS-1$ //$NON-NLS-2$
		clean.put(" :", ":");		// remove blank before : 		//$NON-NLS-1$ //$NON-NLS-2$
		clean.put(" ;", ";");		// remove blank before ; 		//$NON-NLS-1$ //$NON-NLS-2$
		clean.put(" ,", ",");		// remove blank before , 		//$NON-NLS-1$ //$NON-NLS-2$
		clean.put(" .", ".");		// remove blank before . 		//$NON-NLS-1$ //$NON-NLS-2$
		clean.put(",.", ".");		// change .. to .		 		//$NON-NLS-1$ //$NON-NLS-2$
		clean.put("..", ".");		// change .. to .		 		//$NON-NLS-1$ //$NON-NLS-2$
		clean.put(". .", ".");		// change . . to .		 		//$NON-NLS-1$ //$NON-NLS-2$
		clean.put(", .", ".");		// change , . to .		 		//$NON-NLS-1$ //$NON-NLS-2$
		clean.put("; .", ".");		// change ; . to .		 		//$NON-NLS-1$ //$NON-NLS-2$
		clean.put(".,", "");		// change ., to nothing		 		//$NON-NLS-1$ //$NON-NLS-2$
		for (Map.Entry<String, String> entry : clean.entrySet()) {
		    output = output.replaceAll(Pattern.quote(entry.getKey()), entry.getValue());
		}
        // and return it
		return output;

	}		// end of parseFootnoteBiblio

/******************************************************************************************
 * ROUTINES FOR SOURCE ELEMNT NUMBER->NAME AND NAME->NUMBER CONVERSION in SOURCE TEMPLATES
 *****************************************************************************************/
/**
 * convertNumsToNames - convert SourceElement numbers to Element names in template parameter
 * @param template
 * @param hashmap
 * @return converted template
 */
	public String convertNumsToNames(String template, Map<String, String> codeToTextMap) {
		// Setup a regex to find [nnnnn] entries in template
		Pattern pattern = Pattern.compile("\\[(\\d{5})\\]");		//$NON-NLS-1$
        Matcher matcher = pattern.matcher(template);
        StringBuffer result = new StringBuffer();
        // Convert the 5-digit strings to Source Element strings via the hashmap
        while (matcher.find()) {
            String code = matcher.group(1); // extract just the 5-digit string
            String replacement = (String) codeToTextMap.get(code);
            if (replacement != null) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            } else {
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group()));
            }
        }
        matcher.appendTail(result);
        return result.toString();
	}		// End convertNumsToNames

/**
 * convertNamesToNums - convert Source Element names back to Element numbers in template parameter
 * @param template
 * @param hashmap
 * @return converted template
 * @throws HBException
 */
	public String convertNamesToNums(String template, Map<String, String> textToCodeMap) throws HBException {
	// Setup a regex to find [Element names] entries in template ignoring the effect of escape chars
	// creating problems, like \[[AUTHOR]\] does
		Pattern pattern = Pattern.compile("(?<!\\\\)\\[[^\\]]+\\]");
		Matcher matcher = pattern.matcher(template);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String original = matcher.group(); 	// e.g. "[element-name]"
            if (original.contains(":")) {		 // Skip colon-containing entries like [IAL:]  //$NON-NLS-1$
                matcher.appendReplacement(result, Matcher.quoteReplacement(original));
                continue;
            }
            // Convert the Source Element names to 5-digit strings via the hashmap
            String code = textToCodeMap.get(original.trim());
            // Throw an error if no match - user used an element name that doesn't exist
            if (code == null) {
/*                JOptionPane.showMessageDialog(null,
                    HGlobalMsgs.Text_0 + original,			// Unknown Source Element name:
                    HGlobalMsgs.Text_1,						// Source Element name error
                    JOptionPane.ERROR_MESSAGE);
                return null; // halt processing */
                throw new HBException(original);
            }
            // otherwise keep going
            matcher.appendReplacement(result, "[" + Matcher.quoteReplacement(code) + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        matcher.appendTail(result);
        return result.toString();
	}		// End convertNamesToNums

/******************************************************************************************
 * ROUTINES FOR VALIDATING [ ] BRACKETS AND FORMAT CODES IN SENTENCES AND SOURCE TEMPLATES
 *****************************************************************************************/
/**
 * validateBrackets
 * @param input string
 * @return error string
 */
	public String validateBrackets(String input) {
		// Before we start, eliminate any escape character and its target from input
		String cleanInput = input.replaceAll("\\\\.", "  ");
		// Test String input for matching [ ] brackets.
		// Returns null string if all OK, otherwise returns error msg
	    int openIndex = -1;
	    for (int i = 0; i < cleanInput.length(); i++) {
	        char c = cleanInput.charAt(i);
	        if (c == '[') {
	            if (openIndex != -1) {
	            	// Unmatched opening bracket after position openIndex
	            	return HGlobalMsgs.Text_2 + openIndex;		// Unmatched opening bracket after position
	            }
	            openIndex = i;
	        } else if (c == ']') {
	            if (openIndex == -1) {
	            	// Unmatched closing bracket before position i
	            	return HGlobalMsgs.Text_3 + i;				// Unmatched closing bracket before position
	            }
	            openIndex = -1; // matched
	        }
	    }
	    if (openIndex != -1) {
	    	// Unmatched opening bracket after position openIndex
	    	return HGlobalMsgs.Text_4 + openIndex;				// nmatched opening bracket after position
	    }
		return null;		// all OK
	}		// End validateBrackets

/**
 * validateFormatCodes
 * @param input string
 * @return error string
 */
	public String validateFormatCodes(String input) {
		// Test String input for matching [xxx:}  and [:xxx] formatting codes.
		// Returns null string if all OK,
		// otherwise returns error msgg of bad tag and position
		Pattern bracketPattern = Pattern.compile("\\[(.*?)\\]");	//$NON-NLS-1$
		Matcher matcher = bracketPattern.matcher(input);
		Map<String, Integer> openColonTags = new LinkedHashMap<>();
		while (matcher.find()) {
			String content = matcher.group(1);
			int position = matcher.start();
			if (content.endsWith(":")) {		//$NON-NLS-1$
				String tag = content.substring(0, content.length() - 1);
				if (openColonTags.containsKey(tag))
					return (HGlobalMsgs.Text_5 + tag + HGlobalMsgs.Text_6 + position);
						// Duplicate opening tag [		] at position
				openColonTags.put(tag, position);
			} else if (content.startsWith(":")) {		//$NON-NLS-1$
				String tag = content.substring(1);
				if (!openColonTags.containsKey(tag))
					return (HGlobalMsgs.Text_7 + tag + HGlobalMsgs.Text_8 + position);
						// Unmatched closing tag [:		] at position
				openColonTags.remove(tag);
			}
		}
		if (!openColonTags.isEmpty()) {
			Map.Entry<String, Integer> first = openColonTags.entrySet().iterator().next();
			return (HGlobalMsgs.Text_9 + first.getKey() + HGlobalMsgs.Text_10 + first.getValue());
				// Unmatched opening tag [				:] at position
		}
		return null;		// All good
	}    // End validate FormatCodes

/**
 * String runTMGparcer(String roleSentence)
 * @param roleSentence
 * @return
 */
	public String runTMGparcer(String roleSentence) {
		return "";
		//return runTMGparcerTest(roleSentence):
	}

/**
 * public String runTMGparcer(String roleSentence)
 * @param roleSentence
 * @return

	public String runTMGparcerTest(String roleSentence) {
		Map<String,String> argsMap = new HashMap<>();
		TMG_Parser parser = new TMG_Parser(pointReportHandler);
		System.out.println(" Input sentence: " + roleSentence);
		String actualRaw = null;
		try {
			actualRaw = parser.parseTemplate(roleSentence, argsMap);
		} catch (HBException hbe) {
			System.out.println(" runTMGparcer error: " + hbe.getMessage());
			hbe.printStackTrace();
		}
		System.out.println(" Raw output: " + actualRaw);
		return actualRaw;
	}
*/
	String sentencePreview = "";
	HG0547EditEvent pointEditEvent;

/**
 * public void sentenceParser(String input)
 * @param input
 * @throws HBException
 */
    public String sentenceParser(String inputSentence) throws HBException {
/*   [P] was married <to [PO]> <[D]> <[L]>
 *   (en-US)[R:Barn] was born <at [L2]> <in [L]> <[D]>. <[M]>
 *   At the adoption of [R:00003], <[D]>, <[L]> [RP:00008] was listed as the natural child of [R:00004] and [R:00005]. <[M]>
 *    [RP:00010] was [R:00003]'s birth father, as noted in the adoption proceedings <at [L2]> <in [L3]> <[D]>. <[M]>
 */
    	//this.pointEditEvent = pointEditEvent;
    	String reportSentence = inputSentence, replaceString;
    	String variableData = "", sentenceMatch, sentenceVariable;
        Pattern patternVariables, patternRoles, patternOptions, patternAlter;
        Matcher matcherVariables, matcherRoles, matcherOption, matcherAlter;

    // Define the Regex pattern to identify content within the input string

    	String regexOption = "<\\w+\\s+\\[(.*?)\\]>";  // <at [L2]> or <in [L]>
    	String regexAlter = "<\\[(.*?)\\]>";  // <[L2]> or <[L]>
        String regexStandard = "\\[(.*?)\\]"; // [P] was married <to [PO]> <[D]> <[L]>
        String regexRoles = "\\[R:\\d{5}\\]"; // (en-US)[R:Barn] was born <at [L2]> <in [L]> <[D]>. <[M]>
        if (HGlobal.DEBUG) System.out.println(" Input sentence: " + inputSentence);

        try {
	        int count;
	        patternVariables = Pattern.compile(regexStandard);
	        patternRoles = Pattern.compile(regexRoles);
	        patternOptions = Pattern.compile(regexOption);
	        patternAlter = Pattern.compile(regexAlter);

	        matcherOption = patternOptions.matcher(inputSentence);

	        count = 0;
	        while (matcherOption.find()) {
	        	sentenceMatch = matcherOption.group(0);
	        	sentenceVariable = matcherOption.group(1);
	        	variableData = returnSentenceVariableData(sentenceVariable);
	        	if (HGlobal.DEBUG)
	        		System.out.println(" Options found match group: " + matcherOption.group()
	        			+ " - Group 0: " + matcherOption.group(0) + " / "
	        			+ " - Group 1: " + matcherOption.group(1) + " = "
	        			+ variableData);
	        	if (variableData == null) variableData = "";
	        	if (variableData.length() > 0) {
	        		replaceString = sentenceMatch.replace("["+ sentenceVariable + "]", variableData);
	        		reportSentence = reportSentence.replace(sentenceMatch, replaceString);
	        	} else reportSentence = reportSentence.replace(sentenceMatch, "");
	        	count++;
	        }
	        if (HGlobal.DEBUG) System.out.println(" Number of options: " + count);

	        count = 0;
	        matcherAlter = patternAlter .matcher(reportSentence);
	        while (matcherAlter.find()) {
	        	sentenceMatch = matcherAlter.group(0);
	        	sentenceVariable = matcherAlter.group(1);
	        	variableData = returnSentenceVariableData(sentenceVariable);
	        	if (HGlobal.DEBUG)
	        		System.out.println(" Alter found match group: " + matcherAlter.group()
	        			+ " - Group 0: " + matcherAlter.group(0) + " / "
	        			+ " - Group 1: " + matcherAlter.group(1) + " = "
	        			+ variableData);
	        	if (variableData == null) variableData = "";
	        	if (variableData.length() > 0)
	        		reportSentence = reportSentence.replace(sentenceMatch, variableData);
	        	count++;
	        }
	        if (HGlobal.DEBUG) System.out.println(" Number of alters: " + count);

	        count = 0;
	        matcherRoles = patternRoles .matcher(reportSentence);
	        while (matcherRoles.find()) {
	         //matcher.group() extracts the matched role
	        	sentenceMatch = matcherRoles.group();
	        	sentenceVariable = sentenceMatch.replace("[","");
	        	sentenceVariable = sentenceVariable.replace("]","");
	        	if (HGlobal.DEBUG)
	        		System.out.println(" Roles found match: " + sentenceMatch
								 + " - Variable: " + sentenceVariable + " = "
								 + returnSentenceVariableData(sentenceVariable));

	        	variableData = returnSentenceVariableData(sentenceVariable);
	        	if (variableData == null) variableData = "";
	        	reportSentence = reportSentence.replace(sentenceMatch, variableData);
	        	count++;
	        }
	        if (HGlobal.DEBUG) System.out.println(" Number of roles: " + count);

	        count = 0;
	        matcherVariables = patternVariables.matcher(reportSentence);
	        while (matcherVariables.find()) {
	        	sentenceMatch = matcherVariables.group(0);
	        	sentenceVariable = matcherVariables.group(1);
	        	if (HGlobal.DEBUG)
	        		System.out.println(" Variable replace: " + sentenceMatch
	        			+ " - Variable: " + sentenceVariable + " = "
	        			+ returnSentenceVariableData(sentenceVariable));

	        	variableData = returnSentenceVariableData(sentenceVariable);
	        	if (variableData == null) variableData = "";
	        	reportSentence = reportSentence.replace(sentenceMatch, variableData);
	        	count++;
	        }
	        if (HGlobal.DEBUG) System.out.println(" Number of patterns: " + count);

	        reportSentence = reportSentence.replace("<",""); // Temp removal of misplaced
	        reportSentence = reportSentence.replace(">",""); // Temp removal of misplaced
	        reportSentence = reportSentence.replace("(en-US)","");

	        if (HGlobal.DEBUG) System.out.println(" Report sentence build: " + reportSentence);
	        return reportSentence;

		} catch (Exception exe) {
			System.out.println(" HBReportHandler - Sentence: " + inputSentence 
								+ " Parser error: " + exe.getMessage());
			if (HGlobal.writeLogs) HB0711Logging.logWrite(" HBReportHandler - Sentence: " + inputSentence 
								+ " Parser error: " + exe.getMessage());
			//exe.printStackTrace();
			throw new HBException(" HBReportHandler - Sentence: " + inputSentence 
								+ " Parser error: " + exe.getMessage());
		}
    }

/**
 * public String returnSentenceVariableData(String sentenceVariable)
 * @return
 * @throws HBException
 */
    public String returnSentenceVariableData(String sentenceVariable) throws HBException {
    	if (pointReportNameTMG != null) return pointReportNameTMG.returnSentenceVariable(sentenceVariable);
    	if (pointReportEventTMG != null) return pointReportEventTMG.returnSentenceVariable(sentenceVariable);
		return "";
    }

 /**
  * findLocalSentenceSETPID()
  * @return
  * @throws HBException
  
 	public long findLocalSentenceSetPID(long eventTablePID) throws HBException {
 		ResultSet sentenceSetRS;
 		String selectString = setSelectSQL("*", eventTable, "PID = " + eventTablePID);
 		sentenceSetRS = requestTableData(selectString, dataBaseIndex);
 		try {
 			sentenceSetRS.first();
 			return null_RPID;  //
 		} catch (SQLException sqle) {
 			System.out.println(" HBReportHandler - indLocalSentenceSetPID: " + sqle.getMessage());
 			throw new HBException("HBReportHandler - indLocalSentenceSetPID: \" + sqle.getMessage()");
 		}
 	}
*/
    
/**
	T450_EVNT
	PID
	CL_COMMIT_RPID
	HAS_CITATIONS
	OWNS_EVENTS
	VISIBLE_ID
	SURETY
	EVNT_TYPE
	PRIM_ASSOC_BASE_TYPE
	PRIM_ASSOC_RPID
	PRIM_ASSOC_ROLE_NUM
	BEST_IMAGE_RPID
	EVNT_OWNER_RPID
	EVNT_LOCN_RPID
	SORT_HDATE_RPID
	START_HDATE_RPID
	END_HDATE_RPID
	THEME_RPID
	MEMO_RPID
*/

	public class ReportEventTMG extends HBBusinessLayer {
		HBProjectOpenData pointOpenProject;
		HBPersonHandler pointPersonHandler;
		HBWhereWhenHandler pointWhereWhenHandler;
		HBLibraryResultSet pointLibraryResultSet;
		HREmemo pointHREmemo;
		long eventTablePID, memoRPID;
		String selectString, eventDate, locationName, eventPersonName;
		ResultSet eventTableRS, personTableRS, assocTableRS, partnerTableRS;
		long personTablePID = null_RPID, ownerTablePID = null_RPID, nameStylePID = null_RPID, bestPersonNamePID = null_RPID, startHdate,
			eventLocationPID, priPartnerPID = null_RPID, secPartnerPID = null_RPID;
		long assocPersonPrefRPID, eventPersonPrefNamePID = null_RPID, priPartnerPrefNamePID = null_RPID,
			 secPartnerPrefNamePID = null_RPID;
		int dataBaseIndex, visbleIdent, eventType, eventGroup, birthSex, subjectBirthSex, ownerType;
		HashMap<String,String> personNameElements, subjectNameElements, locationNameElements;
		long[] associatePID;
		int[] associateRoles;
		int[] primaryNumbers;
		long[] associatePrefNameRPID;
		String[] partnerNames, nameStyleCodes;
		boolean partnerEvent = false;
		int rows = 0;

		ReportEventTMG(HBProjectOpenData pointOpenProject, long eventTablePID, long ownerTablePID, int ownerType) throws HBException {
			this.eventTablePID = eventTablePID;
			this.ownerTablePID = ownerTablePID;
			this.ownerType = ownerType; // owner type:  1 - name, 2 - event, 3 - associate, 4 - partner
			dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
			pointHREmemo = pointOpenProject.getHREmemo();
			pointPersonHandler = pointOpenProject.getPersonHandler();
			this.pointDBlayer = pointPersonHandler.pointDBlayer;
			pointWhereWhenHandler = pointOpenProject.getWhereWhenHandler();
			pointLibraryResultSet = pointPersonHandler.pointLibraryResultSet;
			personTablePID = pointOpenProject.getSelectedPersonPID();

		// Look up data from eventable
			selectString = setSelectSQL("*", eventTable,"PID = " + eventTablePID);
			eventTableRS = requestTableData(selectString, dataBaseIndex);
			try {
				if (isResultSetEmpty(eventTableRS)) {
					System.out.println(" ReportEventTMG - Event RS empty PID: " + eventTablePID);
					throw new HBException(" ReportEventTMG - Event RS empty PID: " + eventTablePID);
				}
				eventTableRS.first();
				eventType = eventTableRS.getInt("EVNT_TYPE");
			// PRIM_ASSOC_RPID cannot be used since for partner events alway fixed
			// personTablePID = eventTableRS.getLong("PRIM_ASSOC_RPID");
				startHdate = eventTableRS.getLong("START_HDATE_RPID");
				eventLocationPID = eventTableRS.getLong("EVNT_LOCN_RPID");
				eventPersonPrefNamePID =  eventTableRS.getLong("PREF_NAME_RPID");
				memoRPID = eventTableRS.getLong("MEMO_RPID");
				eventGroup = pointPersonHandler.pointLibraryResultSet.getEventGroup(eventType, dataBaseIndex);
				eventDate = pointPersonHandler.pointLibraryResultSet.exstractDate(startHdate, dataBaseIndex);

		    	if (eventGroup == pointPersonHandler.marrGroup || eventGroup == pointPersonHandler.divorceGroup) {
	    // Get and extract the partner names and roles
		    		partnerEvent = true;
					selectString = setSelectSQL("*", personPartnerTable, "EVNT_RPID = " + eventTablePID);
					partnerTableRS = requestTableData(selectString, dataBaseIndex);
					partnerTableRS.first();
					priPartnerPID = partnerTableRS.getLong("PRI_PARTNER_RPID");
					priPartnerPrefNamePID = partnerTableRS.getLong("PRPRI_NAME_RPID");
					if (personTablePID == priPartnerPID) {
						secPartnerPID = partnerTableRS.getLong("SEC_PARTNER_RPID");
						secPartnerPrefNamePID = partnerTableRS.getLong("PRSEC_NAME_RPID");
					} else {
						secPartnerPID = partnerTableRS.getLong("PRI_PARTNER_RPID");
						priPartnerPID = partnerTableRS.getLong("SEC_PARTNER_RPID");
						priPartnerPrefNamePID = partnerTableRS.getLong("PRSEC_NAME_RPID");
						personTablePID = priPartnerPID;
					}
					partnerTableRS.close();
		    	}

		// look up data from persontable
				selectString = setSelectSQL("*", personTable, "PID = " + personTablePID);
				personTableRS = requestTableData(selectString, dataBaseIndex);
				personTableRS.first();
				bestPersonNamePID = personTableRS.getLong("BEST_NAME_RPID");
				visbleIdent = personTableRS.getInt("VISIBLE_ID");
				birthSex = personTableRS.getInt("BIRTH_SEX");
				nameStyleCodes = getOuputReportStyleCodes(bestPersonNamePID);

				if (eventPersonPrefNamePID == null_RPID)  eventPersonPrefNamePID = bestPersonNamePID;
				if (priPartnerPrefNamePID == null_RPID) priPartnerPrefNamePID = bestPersonNamePID;
				if (secPartnerPrefNamePID == null_RPID) secPartnerPrefNamePID = bestPersonNamePID;
				//System.out.println(" Event person best nane PID: " + eventPersonPrefNamePID);

			} catch (SQLException sqle) {
				System.out.println(" ReportEventTMG personTablePID: " + personTablePID);
				System.out.println( " Pri PID: " + priPartnerPID + " Sec PID: " + secPartnerPID);
				System.out.println(" Event Person name: " + getPersonName());
				System.out.println(" Event date: " + eventDate);
				System.out.println(" Event location: " + getLocationName());
				sqle.printStackTrace();
				throw new HBException("ReportEventTMG error: " + sqle.getMessage());
			}
/*
			System.out.println(" ReportEventData pers PID: " + personTablePID);
			System.out.println( " Pri PID: " + priPartnerPID + " Sec PID: " + secPartnerPID);
			System.out.println(" Event Person name: " + getPersonName());
			System.out.println(" Event date: " + eventDate);
			System.out.println(" Event location: " + getLocationName());
			System.out.println(" -->End - ReportEventData"); */
		}

/**
 * private String returnSentenceVariable(String sentenceVariable)
 * @param sentenceVariable
 * @return
 * @throws HBException
 */
		 public String returnSentenceVariable(String sentenceVariable) throws HBException {
			int rows = 0;
			String roleNumber = "00000";
			//long personTablePID;
			try {
/* Proceess R - role variables
 *    At the adoption of [R:00003], <[D]>, <[L]> [RP:00008] was listed as the natural child of [R:00004] and [R:00005]. <[M]>
 *    [RP:00010] was [R:00003]'s birth father, as noted in the adoption proceedings <at [L2]> <in [L3]> <[D]>. <[M]>
 */
		    	if (sentenceVariable.startsWith("R") || sentenceVariable.startsWith("RP") ) {
		    		if (rows == 0)
		    			rows = collectAssociatePersons(eventTablePID);
		    		if (personNameElements == null)
		    			personNameElements =  pointPersonHandler.pointLibraryResultSet.
		    				selectPersonNameElements(eventPersonPrefNamePID, dataBaseIndex);
		    		String[] roleData = sentenceVariable.split(":");
		    		if (roleData.length <= 1) return "?" + sentenceVariable;
					roleNumber = roleData[1];
					if (partnerEvent) {
						if (roleNumber.equals("00020")) return findPersonName(secPartnerPID, secPartnerPrefNamePID);
						if (roleNumber.equals("00004")) return findPersonName(priPartnerPID, priPartnerPrefNamePID);
						if (roleNumber.equals("00003")) return findPersonName(secPartnerPID, secPartnerPrefNamePID);
						return findPersonName(findAssociatePersonRole(roleNumber), null_RPID);
					}
					if (roleNumber.equals("00001") || roleNumber.equals("00003"))
						return getPersonName(personNameElements, visbleIdent);
	// Temp added to test long sentence				
					if (roleNumber.equals("00002")) {
						String withessList = "";
						if (rows > 0) {
							for (int i = 0; i < associatePID.length; i++) {
								//System.out.println(" Nr: " + i + " witnes: " + associatePID[i]);
								if (i < associatePID.length -1)
								withessList = withessList 
										+ findAssociatePersonName(i + 1) + ", ";
								else 	withessList = withessList 
										+ findAssociatePersonName(i + 1);
							}
							return withessList;
						} else  return "";
					}
					
					return findPersonName(findAssociatePersonRole(roleNumber), assocPersonPrefRPID);
		    	}


		    /* Process Px
		    	Summarising Px in sentence reference terms:

		    	    [P1] -- produces the name of the person entered in the P1 slot in the event tag
		    	    [P2] -- produces the name of the person entered in the P2 slot in the event tag
		    	    [P] --  produces the name of the subject of the sentence (who will be a P1 or a P2 person
		    	    [PO] -- produces the name of the other principal (if there is one),
		    	    	    the one who is not the subject.

            */
		    	if (sentenceVariable.startsWith("P")) {
		    		if (personNameElements == null)
		    			personNameElements =  pointPersonHandler.pointLibraryResultSet.
		    				selectPersonNameElements(eventPersonPrefNamePID, dataBaseIndex);

		    		if (sentenceVariable.equals("P") || sentenceVariable.equals("P+")
		    										 || sentenceVariable.equals("P1"))
		    				return getPersonName(personNameElements, visbleIdent);

		    		if (sentenceVariable.equals("PP"))
		    			if (birthSex == 1) return "He";
		    			else if (birthSex == 2) return "She";
		    			else if (birthSex == 3) return "Hen";

		    		if (sentenceVariable.equals("PO") || sentenceVariable.equals("P2")
		    										  || sentenceVariable.equals("POS"))
		    			return getPersonOptional();

		    		switch (sentenceVariable) {
		    			case "PG":  return personNameElements.get(personNameCodeArray[2]);
		    			case "PF": return personNameElements.get(personNameCodeArray[2]);
		    			case "PL":  return personNameElements.get(personNameCodeArray[4]);
		    			case "PGS": return personNameElements.get(personNameCodeArray[2]);
		    			case "PFS":  return personNameElements.get(personNameCodeArray[2]);
		    			case "PLS":  return personNameElements.get(personNameCodeArray[4]);
		    			//default: return "?" + sentenceVariable;
		    		}

			    	/**
			    	 * [PAR] The parents of the Current Principal
					 * [PARO]The parents of the Other Principal
					 * [PAR1] The parents of Principal #1
					 * [PAR2] The parents of Principal #2
			    	 */

			    	if (sentenceVariable.startsWith("PA")) {
			    			String prefix;
			    			if (birthSex == 1) prefix = " son of ";
			    			else if (birthSex == 2) prefix = " dauther of ";
			    			else prefix = " parents ";
			    			switch (sentenceVariable) {
			    			case "PAR":
			    				if (partnerEvent)
			    					return prefix + findPersonName(findFatherForPerson(priPartnerPID), null_RPID);
			    				 else return prefix + findPersonName(findFatherForPerson(personTablePID), null_RPID);
			    			case "PARO":
			    				if (partnerEvent)
			    					return prefix + findPersonName(findFatherForPerson(secPartnerPID), null_RPID);
			    				 else return prefix + findPersonName(findFatherForPerson(personTablePID), null_RPID);
			    			case "PAR1": return "?" + ownerType + sentenceVariable;
			    			case "PAR2": return "?" + ownerType + sentenceVariable;
			    			default: return "??" + sentenceVariable;
			    		}
			    	}
		    	}

		    // Process date
		    	if (sentenceVariable.startsWith("D")) {
		    		return  eventDate;
		    	}

// process Lx variables
/*
 * Level 1  [LA], [L1] or [ADDRESSEE]
 * Level 2  [LD], [L2] or [DETAIL]  also [DETAIL1], [DETAIL2], ... [DETAIL9] alternatively, [LD1], [LD2], ... [LD9]
 * Level 3  [LCI], [L3] or [CITY]
 * Level 4  [LCN], [L4] or [COUNTY]
 * Level 5  [LS], [L5] or [STATE]
 * Level 6  [LCR], [L6] or [COUNTRY]
 * Level 7  [LZ], [L7] or [ZIP]
 * Level 8  [LP], [L8] or[PHONE]
 * Level 9  [LL], [L9] or [LATLONG]
 * Level 10[LT], [L10] or [TEMPLE]
 */
		    	
		    	if (sentenceVariable.startsWith("L")) {
					if (locationNameElements == null)
						locationNameElements = pointPersonHandler.pointLibraryResultSet.
						   selectLocationNameElements(eventLocationPID, dataBaseIndex);
				 	if (sentenceVariable.equals("L")) return getLocationName();
		    		switch (sentenceVariable) {
		    			case "L1":  return locationNameElements.get(locationNameCodeArray[0]);
		    			case "LA":  return locationNameElements.get(locationNameCodeArray[0]);
		    			
		    			case "L2":  return locationNameElements.get(locationNameCodeArray[1]);
		    			case "LD":  return locationNameElements.get(locationNameCodeArray[1]);
		    			
		    			case "L3":  return locationNameElements.get(locationNameCodeArray[2]);
		    			case "LCI":  return locationNameElements.get(locationNameCodeArray[2]);
		    			
		    			case "L4":  return locationNameElements.get(locationNameCodeArray[3]);
		    			case "LCN":  return locationNameElements.get(locationNameCodeArray[3]);
		    			
		    			case "L5":  return locationNameElements.get(locationNameCodeArray[4]);
		    			case "LS":  return locationNameElements.get(locationNameCodeArray[4]);
		    			
		    			case "L6":  return locationNameElements.get(locationNameCodeArray[5]);
		    			case "LZ":  return locationNameElements.get(locationNameCodeArray[5]);
		    			default: return "?" + sentenceVariable;
		    		}
		    	 }

			// process Sx variables Subject variables
		    // Need update *******************************************' NTo 7-8-2026
		    	if (sentenceVariable.startsWith("S")) {
		    		//System.out.println(" S-variable OwnerType: " + ownerType + " OwnerPID:" + ownerTablePID);
		    		if ( ownerType != 3) return "?" + ownerType + "/" +sentenceVariable;
					long bestSubjectPersonName = findSubjectNameTablePID(ownerTablePID);
					if (subjectNameElements == null)
						subjectNameElements =  pointPersonHandler.pointLibraryResultSet.
							selectPersonNameElements(bestSubjectPersonName, dataBaseIndex);

		    		switch (sentenceVariable) {
		    			case "S": return getPersonName(subjectNameElements, visbleIdent);
		    			case "S+": return getPersonName(subjectNameElements, visbleIdent);
		    			case "SS": return subjectNameElements.get(personNameCodeArray[2]);
		    			case "SG": return subjectNameElements.get(personNameCodeArray[2]);
		    			case "SF": return subjectNameElements.get(personNameCodeArray[2]);
		    			case "SL": return subjectNameElements.get(personNameCodeArray[4]);
		    			case "SA": return "?Age";
		    			case "SE": return "?ExAge";
		    			case "SP": if (subjectBirthSex == 1) return "She";
						if (subjectBirthSex == 2) return "He";
		    			case "SPP": if (subjectBirthSex == 1) return "Her";
						if (subjectBirthSex == 2) return "His";
		    			case "SM": if (subjectBirthSex == 1) return "Her";
						if (subjectBirthSex == 2) return "Him";
		    			case "SGS": return subjectNameElements.get(personNameCodeArray[2]) + "'s";
		    			case "SFS": return subjectNameElements.get(personNameCodeArray[2]) + "'s";
		    			case "SLS": return subjectNameElements.get(personNameCodeArray[4]) + "'s";
		    			default: return "?" + sentenceVariable;
		    		}
		    	}

		    // Process witness
		    	 if (sentenceVariable.startsWith("W")) {
		    		 if (rows == 0)
		    			 rows = collectAssociatePersons(eventTablePID);
		    		 if (rows < 1) return "?" + sentenceVariable;
		    		 if (sentenceVariable.equals("W"))
		    			 return findPersonName(associatePID[0], associatePrefNameRPID[0]);
					 if (sentenceVariable.equals("WO") && rows > 1)
		    			 return findPersonName(associatePID[1], associatePrefNameRPID[1]);
					 if (sentenceVariable.equals("WO") && rows > 2)
		    			 return findPersonName(associatePID[2], associatePrefNameRPID[2]);
					 if (sentenceVariable.equals("WM")) return "WM memo";
					 return "?" + sentenceVariable;

		    	 }

		    // Process memo
		    	 if (sentenceVariable.startsWith("M"))
						return pointHREmemo.readMemo(memoRPID);

		    // create new line or [:CR:]
		    	 if (sentenceVariable.startsWith(":CR:"))
		    		 return "\n";

		    // handle [:TAB:] - treat as 4 spaces (width of std TAB)
		    	 if (sentenceVariable.startsWith(":TAB:"))
		    		 return "    ";


		    	 return "?" + sentenceVariable;

			} catch (HBException hbe) {
				System.out.println(" ReportEventTMG - Sentence variable: " + sentenceVariable + " error: " + hbe.getMessage());
				hbe.printStackTrace();
				throw new HBException(" ReportEventTMG");
			}
	    }

/**
 * private String[] getOuputStyleCodes(long nameStyleOutputPID)
 * @param nameStyleOutputPID
 * @return
 * @throws HBException
 */
		private String[] getOuputReportStyleCodes(long bestPersonNamePID) throws HBException {
			ResultSet bestPersonNameRS,nameStyleOutputRS;
			String codeString = "No String";
			long nameStyleOutputPID;
			String[] outputDataCodes = null;
			selectString = setSelectSQL("*", personNameTable,"PID = " + bestPersonNamePID);
			bestPersonNameRS = requestTableData(selectString, dataBaseIndex);
			try {
				bestPersonNameRS.first();
				nameStyleOutputPID = bestPersonNameRS.getLong("NAME_STYLE_RPID");
				nameStyleOutputRS = pointLibraryResultSet.getOutputStylesTable(nameStylesOutput,
						"N", nameStyleOutputPID, dataBaseIndex);
				if (isResultSetEmpty(nameStyleOutputRS)) return outputDataCodes;

				nameStyleOutputRS.beforeFirst();
				while (nameStyleOutputRS.next()) {
					if (nameStyleOutputRS.getString("OUT_TYPE").equals("R")) {
							codeString = nameStyleOutputRS.getString("OUT_ELEMNT_CODES");
							outputDataCodes = codeString.split("\\|");
							break;
					}
				}
				//System.out.println(" Output Style Codes: " + codeString);
				return outputDataCodes;
			} catch (SQLException sqle) {
				System.out.println(" ReportEventTMG - getOuputStyleCodes: " + sqle.getMessage());
				sqle.printStackTrace();
				throw new HBException(" ReportEventTMG - getOuputStyleCodes: " + sqle.getMessage());
			}
		}


		private long findMotherForPerson(long personTablePID) throws HBException {
			return findValueForPerson("EGG_PROVIDER_RPID", personTablePID);
		}

		private long findFatherForPerson(long personTablePID) throws HBException {
			return findValueForPerson("SPERM_PROVIDER_RPID", personTablePID);
		}

		private long findBestNameForPerson(long personTablePID) throws HBException {
			return findValueForPerson("BEST_NAME_RPID", personTablePID);
		}

		private long findValueForPerson(String relation, long personTablePID ) throws HBException {
			ResultSet personTableRS;
			selectString = setSelectSQL(relation, personTable, "PID = " + personTablePID);
			personTableRS = requestTableData(selectString, dataBaseIndex);
			try {
				if (isResultSetEmpty(personTableRS)) return null_RPID;
				personTableRS.first();
				return personTableRS.getLong(relation);
			} catch (SQLException sqle) {
				System.out.println(" ReportEventTMG - findParentForPerson - " + sqle.getMessage());
				sqle.printStackTrace();
				throw new HBException(" ReportEventTMG - findValueForPerson error: " + sqle.getMessage());
			}
		}

/**
 * private long findSubjectNameRablePID(long assocTablePID)
 * @param assocTablePID
 * @return
 * @throws HBException
 */
	private long findSubjectNameTablePID(long assocTablePID) throws HBException {
		ResultSet personNameRS, assocTableRS;
		long assocPersonTablePID, assocPersonPefNamePID;
		selectString = setSelectSQL("ASSOC_RPID,PREF_NAME_RPID", eventAssocTable,"PID = " + assocTablePID);
		assocTableRS = requestTableData(selectString, dataBaseIndex);
		try {
			assocTableRS.first();
			assocPersonTablePID = assocTableRS.getLong("ASSOC_RPID");
			assocPersonPefNamePID = assocTableRS.getLong("PREF_NAME_RPID");
			selectString = setSelectSQL("BEST_NAME_RPID,BIRTH_SEX", personTable,"PID = " + assocPersonTablePID);
			personNameRS = requestTableData(selectString, dataBaseIndex);
			personNameRS.first();
			subjectBirthSex = personNameRS.getInt("BIRTH_SEX");
			if (assocPersonPefNamePID == null_RPID) return personNameRS.getLong("BEST_NAME_RPID");
			return assocPersonPefNamePID;
		} catch (SQLException sqle) {
			System.out.println(" ReportEventTMG - findSubjectNameTablePID error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" ReportEventTMG - findSubjectNameTablePID error: " + sqle.getMessage());
		}
	}

/**
 * Returns partner Optional (PO) name or name first of person in associate list if not partener event
 * @return person name with visible id.
 * @throws HBException
 */
	public String getPersonOptional() throws HBException {
			if (partnerEvent)
				return findPersonName(secPartnerPID, null_RPID);
			if (rows == 0) rows = collectAssociatePersons(eventTablePID);
			if (rows < 1) return "";
		// Find the primary P2 name
			for (int i = 0; i < primaryNumbers.length; i++)
				if (primaryNumbers[i] == 2) return findPersonName(associatePID[i], associatePrefNameRPID[i]);
			return findPersonName(associatePID[0], associatePrefNameRPID[0]);
		}

/**
 * Returns the name of the associate person with index number in Associate list
 * @param index - The number of the associate list startinng with first = 1
 * @return
 * @throws HBException
 */
		public String findAssociatePersonName(int index) throws HBException {
			index = index -1;
			if (rows == 0) rows = collectAssociatePersons(eventTablePID);
			if (index < 0 || index > rows) return "";
			return findPersonName(associatePID[index], associatePrefNameRPID[index]);
		}

		private String findPersonName(long personTablePID, long prefNamePID) throws HBException {
			HashMap<String,String> personNameElements = null;
			long bestPersonNamePID;
			int viibleIdent;
			selectString = setSelectSQL("*", personTable, "PID = " + personTablePID);
			personTableRS = requestTableData(selectString, dataBaseIndex);
			try {
				if (isResultSetEmpty(personTableRS)) {
					//System.out.println(" ReportEventData - findPersonName error personNamePID: " + personTablePID);
					return " Not found!";
				}
				personTableRS.first();
				viibleIdent = personTableRS.getInt("VISIBLE_ID");
				if (prefNamePID == null_RPID)
					bestPersonNamePID = personTableRS.getLong("BEST_NAME_RPID");
				else bestPersonNamePID = prefNamePID;
				
				personNameElements =  pointLibraryResultSet.
						selectPersonNameElements(bestPersonNamePID, dataBaseIndex);
				return getPersonName(personNameElements, viibleIdent);
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw new HBException(" ReportEventTMG - findPersonName error: " + sqle.getMessage());
			}
		}

/**
 * Check if event is a partner event?
 * @return tur if event is a partner vent.
 */
		public boolean isParnerEvent() {
			return partnerEvent;
		}
/**
 * Returns the individual person name fields as Given name, surname according to the index patameter
 * @param index - The following index values is used for personNameFields:
 * Title = 1, Prefix = 2, GivenName = 3, PreSurname = 4, Surname =5, Suffix = 6, OtherName = 7, SortSurname = 8, SortGiven = 9,
 * @return A String with the requested name field
 */
		public String getPersonNameElement(int index) {
			index = index -1;
			if (index < 0 || index > 9 ) return "";
			return personNameElements.get(personNameCodeArray[index]);
		}

/**
 * Create and returns the person name according to the recorded namestyle
 * @return String with person name and (visible id)
 * @throws HBException
 */
		public String getPersonName() throws HBException {
    		if (personNameElements == null)
    			personNameElements =  pointPersonHandler.pointLibraryResultSet.
    				selectPersonNameElements(eventPersonPrefNamePID, dataBaseIndex);
			return getPersonName(personNameElements, visbleIdent);
		}

		private String getPersonName(HashMap<String,String> personNameElements, int viibleIdent) {
			String nameElement, nameCode, personName = "";
			boolean first = true, comma = false;
			for (int i = 0; i < nameStyleCodes.length; i++) {
				nameCode = nameStyleCodes[i];
				if (nameCode.contains("#")) {
					comma = true;
					nameCode = nameCode.substring(0,4);
					//System.out.println(" New name code: " + nameCode);
				}
				nameElement = personNameElements.get(nameCode);
				if (comma) nameElement = nameElement + ",";
				if (nameElement != null) {
					if (first)
						personName =  nameElement;
					else personName = personName + " " + nameElement;
					if (first) first = false;
				}
				comma = false;
			}
			return personName + "(" + viibleIdent + ")";
		}

/**
 * Return the location name element (locationNameFields) according to index value
 * @param index - The following index values is used for locationNameFields:
 * Addressee = 1, Detail = 2, City = 3, County = 4, State = 5, Country = 6, Postal = 7, Phone = 8
 * LatLong = 9, Temple = 10;
 * @return String with locationNameField
 */
		public String getLocationNameElement(int index) {
			index = index -1;
			if (index < 0 || index > 10 ) return "";
			return locationNameElements.get(locationNameCodeArray[index]);
		}

/**
 * Returns the location name according to location name style
 * @return String with location name.
 * @throws HBException
 */
		public  String getLocationName() throws HBException {
			String nameElement, locationName = "";
			if (locationNameElements == null)
				locationNameElements = pointLibraryResultSet.
				   selectLocationNameElements(eventLocationPID, dataBaseIndex);
			boolean first = true;
			for (int i = 0; i < locationNameCodeArray.length; i++) {
				nameElement = locationNameElements.get(locationNameCodeArray[i]);
				if (nameElement != null) {
					if (first)
						locationName = locationName + nameElement;
					else locationName = locationName + ", " + nameElement;
					if (first) first = false;
				}
			}
			return locationName;
		}

/**
 * Return the name of the associate in the associate list with role == roleNumner
 * @param roleNumber String with role number in the form 0000N
 * @return String with person name and (visible id)
 * @throws HBException
 */

	public long findAssociatePersonRole(String roleNumber) throws HBException {
			if (rows == 0)
				rows = pointReportEventTMG.collectAssociatePersons(eventTablePID);
			int roleInt = Integer.parseInt(roleNumber);
			for (int i = 0; i < associatePID.length; i++)
				if (roleInt == associateRoles[i]) {
					assocPersonPrefRPID = associatePrefNameRPID[i];
					return associatePID[i];
				}
		return null_RPID;
	}

/**
 * Initiate the list of associate persons for the event
 * @return integer with number of persons in list
 * @throws HBException
 */
		public int inittAssociatePersonsLists() throws HBException {
			return collectAssociatePersons(eventTablePID);
		}

/**
 * private int collectAssociatePersons(long eventTablePID)
 * @param eventTablePID
 * @return
 * @throws HBException
 */
		private int collectAssociatePersons(long eventTablePID) throws HBException {
			int nrOfRows = 0, index = 0;
			ResultSet assocTableRS;
			selectString = setSelectSQL("*", eventAssocTable,"EVNT_RPID = " + eventTablePID);
			assocTableRS = requestTableData(selectString, dataBaseIndex);
			try {
				assocTableRS.last();
				nrOfRows = assocTableRS.getRow();
				associatePID = new long[nrOfRows];
				associateRoles = new int[nrOfRows];
				primaryNumbers = new int[nrOfRows];
				associatePrefNameRPID = new long[nrOfRows];
				assocTableRS.beforeFirst();
				while (assocTableRS.next()) {
					associatePID[index] = assocTableRS.getLong("ASSOC_RPID");
					associateRoles[index] = assocTableRS.getInt("ROLE_NUM");
					primaryNumbers[index] = assocTableRS.getInt("PRIMARY_NUM");
					associatePrefNameRPID[index] = assocTableRS.getLong("PREF_NAME_RPID");
					//System.out.println(" Assocs: " + associatePID[index] + "/" + associateRoles[index]
					//		+ "/" + primaryNumbers[index]);
					index++;
				}
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw new HBException(" ReportEventTMG - collectAssocatePersons error: " + sqle.getMessage());
			}
			return nrOfRows;

		}

/**
 * Get date for the event
 * @return date in the user defined date format
 * @throws
 */
		public String getEventDate()  {
			try {
				return pointPersonHandler.pointLibraryResultSet.exstractDate(startHdate, dataBaseIndex);
			} catch (HBException hbe) {
				System.out.println(" ERROR: GetEventDate: " + hbe.getMessage());
				hbe.printStackTrace();
			}
			return "";
		}
/**
 * Return event memo for sentence building
 * @return String with memo text
 * @throws HBException
 */
		public String getEventMemo() throws HBException {
			return pointHREmemo.readMemo(memoRPID);
		}
		
	} // End ReportEventTMG

/**
 * class ReportNameTMG
 */
	public class ReportNameTMG extends HBBusinessLayer {
		HBProjectOpenData pointOpenProject;
		HBPersonHandler pointPersonHandler;
		HBWhereWhenHandler pointWhereWhenHandler;
		HBLibraryResultSet pointLibraryResultSet;
		String[] nameStyleCodes;
		ResultSet personNameTableRS, personTableRS;
		String selectString, eventDate;
		long selectedPersonPID, personNamedPID, eventBestPersonNamePID, nameTablePID, startHdate, memoRPID;
		HashMap<String,String> personNameElements, personNamedElements;
		int dataBaseIndex, visbleIdent, eventType, eventGroup, birthSex;

/**
 * ReportNameTMG(HBProjectOpenData pointOpenProject, long nameTablePID)
 * @param pointOpenProject
 * @param nameTablePID
 * @throws HBException
 */
		ReportNameTMG(HBProjectOpenData pointOpenProject, long nameTablePID) throws HBException {
			this.nameTablePID = nameTablePID;
			selectedPersonPID = pointOpenProject.getSelectedPersonPID();
			dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
			pointHREmemo = pointOpenProject.getHREmemo();
			pointPersonHandler = pointOpenProject.getPersonHandler();
			this.pointDBlayer = pointPersonHandler.pointDBlayer;
			pointWhereWhenHandler = pointOpenProject.getWhereWhenHandler();
			pointLibraryResultSet = pointPersonHandler.pointLibraryResultSet;
			nameStyleCodes = getOuputReportStyleCodes(nameTablePID);

			if (HGlobal.DEBUG) System.out.println(" ReportNameTMG  Name table PID: " +  nameTablePID);
		// Look up data from nametable
			selectString = setSelectSQL("*", personNameTable, "PID = " + nameTablePID);
			personNameTableRS = requestTableData(selectString, dataBaseIndex);
			try {
				personNameTableRS.first();
				personNamedPID = personNameTableRS.getLong("OWNER_RPID");
				startHdate = personNameTableRS.getLong("START_HDATE_RPID");
				memoRPID = personNameTableRS.getLong("MEMO_RPID");
				eventDate = pointPersonHandler.pointLibraryResultSet.exstractDate(startHdate, dataBaseIndex);

			// look up data from persontable
				selectString = setSelectSQL("*", personTable, "PID = " + selectedPersonPID);
				personTableRS = requestTableData(selectString, dataBaseIndex);
				personTableRS.first();
				eventBestPersonNamePID = personTableRS.getLong("BEST_NAME_RPID");
				visbleIdent = personTableRS.getInt("VISIBLE_ID");
				birthSex = personTableRS.getInt("BIRTH_SEX");
			} catch (SQLException sqle) {
				System.out.println(" ReportEventTMG personNameTablePID: " + nameTablePID);
				System.out.println(" Person name: " + getPersonName());
				System.out.println(" Event date: " + eventDate);
				sqle.printStackTrace();
				throw new HBException(" HBReportHandler - ReportNameTMG error: " + sqle.getMessage());
			}
		}

/**
 * private String[] getOuputStyleCodes(long nameStyleOutputPID)
 * @param nameStyleOutputPID
 * @return
 * @throws HBException
 */
		private String[] getOuputReportStyleCodes(long bestPersonNamePID) throws HBException {
			ResultSet bestPersonNameRS,nameStyleOutputRS;
			String codeString = "No String";
			long nameStyleOutputPID;
			String[] outputDataCodes = null;
			selectString = setSelectSQL("*", personNameTable,"PID = " + bestPersonNamePID);
			bestPersonNameRS = requestTableData(selectString, dataBaseIndex);
			try {
				bestPersonNameRS.first();
				nameStyleOutputPID = bestPersonNameRS.getLong("NAME_STYLE_RPID");
				nameStyleOutputRS = pointLibraryResultSet.getOutputStylesTable(nameStylesOutput,
						"N", nameStyleOutputPID, dataBaseIndex);
				if (isResultSetEmpty(nameStyleOutputRS)) return outputDataCodes;

				nameStyleOutputRS.beforeFirst();
				while (nameStyleOutputRS.next()) {
					if (nameStyleOutputRS.getString("OUT_TYPE").equals("R")) {
							codeString = nameStyleOutputRS.getString("OUT_ELEMNT_CODES");
							outputDataCodes = codeString.split("\\|");
							break;
					}
				}
				//System.out.println(" Output Style Codes: " + codeString);
				return outputDataCodes;
			} catch (SQLException sqle) {
				System.out.println(" ReportNameTMG - getOuputStyleCodes: " + sqle.getMessage());
				sqle.printStackTrace();
				throw new HBException(" ReportNameTMG - getOuputStyleCodes: " + sqle.getMessage());
			}
		}

/**
 * Create and returns the person name according to the recorded namestyle
 * @return String with person name and (visible id)
 * @throws HBException
 */
		public String getPersonName() throws HBException {
    		if (personNameElements == null)
    			personNameElements =  pointPersonHandler.pointLibraryResultSet.
    				selectPersonNameElements(nameTablePID, dataBaseIndex);
			return getPersonName(personNameElements, visbleIdent);
		}

		private String getPersonName(HashMap<String,String> personNameElements, int viibleIdent) {
			String nameElement, nameCode, personName = "";
			boolean first = true, comma = false;
			for (int i = 0; i < nameStyleCodes.length; i++) {
				nameCode = nameStyleCodes[i];
				if (nameCode.contains("#")) {
					comma = true;
					nameCode = nameCode.substring(0,4);
					//System.out.println(" New name code: " + nameCode);
				}
				nameElement = personNameElements.get(nameCode);
				if (comma) nameElement = nameElement + ",";
				if (nameElement != null) {
					if (first)
						personName =  nameElement;
					else personName = personName + " " + nameElement;
					if (first) first = false;
				}
				comma = false;
			}

			if (viibleIdent > 0)
				return personName + "(" + viibleIdent + ")";
			return personName;
		}

/**
 * returnSentenceVariable(String sentenceVariable)
 * @param sentenceVariable
 * @return
 * @throws HBException 
 */
		public String returnSentenceVariable(String sentenceVariable) throws HBException {
			if (HGlobal.DEBUG) System.out.println(" Not found sentence variable: " + sentenceVariable);
			try {
			   	if (sentenceVariable.startsWith("P")) {
		    		if (personNameElements == null)
		    			personNameElements =  pointPersonHandler.pointLibraryResultSet.
		    				selectPersonNameElements(selectedPersonPID, dataBaseIndex);

		    		if (sentenceVariable.equals("P") || sentenceVariable.equals("P+")
		    										 || sentenceVariable.equals("P1"))
		    				return getPersonName(personNameElements, visbleIdent);

		    		if (sentenceVariable.equals("PP"))
		    			if (birthSex == 2) return "His";
		    			else if (birthSex == 1) return "Hers";
		    			else if (birthSex == 3) return "Hen's";
		    			else return "?" + sentenceVariable;

			   	}

			   	if (sentenceVariable.startsWith("N")) {
			    		if (personNamedElements == null)
			    			personNamedElements =  pointPersonHandler.pointLibraryResultSet.
			    				selectPersonNameElements(nameTablePID, dataBaseIndex);

			    		if (sentenceVariable.equals("N") || sentenceVariable.equals("P+")
			    										 || sentenceVariable.equals("P1"))
			    				return getPersonName(personNamedElements, 0);
			   	}
			    // Process date
		    	if (sentenceVariable.startsWith("D")) {
		    		if (eventDate.length() > 0) return  eventDate;
					return "";
		    	}

			    // Process memo
		    	 if (sentenceVariable.startsWith("M"))
						return pointHREmemo.readMemo(memoRPID);
		    	 
		    	 return "?" + sentenceVariable;

			} catch (HBException hbe) {
				System.out.println(" ReportNameTMG - Sentence variable: " + sentenceVariable 
						+ "  error: " + hbe.getMessage());
				hbe.printStackTrace();
				throw new HBException(" ReportNameTMG - error: " + hbe.getMessage());
			}
		}
	} // End class ReportNameTMG
} // End Class HBReportHandler

