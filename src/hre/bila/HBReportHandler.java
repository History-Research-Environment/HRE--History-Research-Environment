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
 * 			  2026-04-20 - Added code for clear rellation/update person relation (N. Tolleshaug)
 *********************************************************************************************/

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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
	RelationCalculation pointRelationCalculation;
	public ReportEventTMG pointReportEventTMG;
	int dataBaseIndex = -1;

	String[] locationNameCodeArray, personNameCodeArray;
	String personNameCodes = "1100|2000|3000|5000|5200|5700|3700|5500|3300|";
	String personNameFields ="Title|Prefix|GivenName|PreSurname|Surname|Suffix|OtherName|SortSurname|SortGiven|";
	String locationNameCodes = "0500|1100|3000|3100|3400|3500|3900|0100|4100|4300|";
	String locationNameFields = "Addressee|Detail|City|County|State|Country|Postal|Phone|LatLong|Temple|";

	//boolean EditEventCase = true;  //Switch on local eventEdit data or repor data
	boolean EditEventCase = false;

	public void turnOnReportHandling() {
		EditEventCase = false;
	}

	public void turnOnEditEvent() {
		EditEventCase = true;
	}

	public ReportEventTMG createReportEventData(long eventTablePID) {
		try {
		// Set up the element code arrays
			personNameCodeArray = personNameCodes.split("\\|");
			locationNameCodeArray = locationNameCodes.split("\\|");
			pointReportEventTMG = new ReportEventTMG(pointOpenProject, eventTablePID);
		// Set report handler mode
			turnOnReportHandling();
			return pointReportEventTMG;
		} catch (HBException hbe) {
			System.out.println(" HBReportHandler - ReportEventData. " + hbe.getMessage());
			hbe.printStackTrace();
			return null;
		}
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
									  String[][] tableSourceElmntDataValues, String[] citationParts) {

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
  				for (int j = 0; j < tableSourceElmntDataValues.length; j++) {
  					if (elmntNumber.equals(tableSourceElmntDataValues[j][0])) {
  						tokensPh3[i] = tableSourceElmntDataValues[j][1];
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
 * Activivate relation Calculate
 * @param focusPersonPID
 * @throws HBException
 */
	public void activateRelationCalc(long focusPersonPID) throws HBException {

		if (pointRelationCalculation == null)
					pointRelationCalculation = new	RelationCalculation(pointOpenProject);
		pointRelationCalculation.recalcRelationsT401(focusPersonPID);
	}

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
    public String sentenceParser(HG0547EditEvent pointEditEvent, String inputSentence) throws HBException {
       // input = "[Per] ble f�dt <[1990]><[Oslo]>. <Disse var ogs� med: [Kari og Knut].> <[Hjemmef�dsel]>";

        // Define the Regex pattern to identify content within square brackets
    	this.pointEditEvent = pointEditEvent;
    	String reportSentence = inputSentence, replaceString;
    	String variableData, match;
        String regexStandard = "\\[(.*?)\\]";
        String regexRoles = "\\[R:\\d{5}\\]";

        Pattern patternVariables = Pattern.compile(regexStandard);
        Pattern patternRoles = Pattern.compile(regexRoles);
        Matcher matcherVariables = patternVariables.matcher(inputSentence);
        Matcher matcherRoles = patternRoles.matcher(inputSentence);
        HashMap<String, String> sentenceElements = new HashMap<>();
        int count = 0;
        while (matcherVariables.find()) {
        // matcher.group(1) extracts the content inside the []
        	variableData = returnSentenceVariableData(matcherVariables.group(1));
        	sentenceElements.put(matcherVariables.group(0).trim(), variableData);
            count++;
        }
        if (HGlobal.DEBUG)
        	System.out.println(" Number of variables: " + count);
        count = 0;
        matcherRoles = patternRoles.matcher(inputSentence);
        while (matcherRoles.find()) {
         // matcher.group(1) extracts the content inside the []
        	match = matcherRoles.group();
        	//System.out.println(" Match: " + match);
        	match = match.replace("[","");
        	match = match.replace("]","");
        	variableData = returnSentenceVariableData(match);
        	sentenceElements.put(matcherRoles.group(0).trim(), variableData);
            count++;
        }

        if (HGlobal.DEBUG)
        	System.out.println(" Number of roles: " + count);

        matcherVariables = patternVariables .matcher(inputSentence);
        while (matcherVariables.find()) {
        	String sentenceMatch = matcherVariables.group(0).trim();
        	//System.out.println(" Variable replace: " + sentenceMatch+ "/"
        	//		+ sentenceElements.get(sentenceMatch));
        	replaceString = sentenceElements.get(sentenceMatch);
        	if (replaceString == null) replaceString = "";
        	reportSentence = reportSentence.replace(sentenceMatch, replaceString);
        }
        matcherRoles = patternRoles .matcher(inputSentence);
        while (matcherRoles.find()) {
        	String sentenceMatch = matcherRoles.group(0).trim();
        	//System.out.println(" Role replace: " + sentenceMatch+ "/"
        	//		+ sentenceElements.get(sentenceMatch));

        	replaceString = sentenceElements.get(sentenceMatch);
        	if (replaceString == null) replaceString = "";
        	reportSentence = reportSentence.replace(sentenceMatch, replaceString);
        }

        reportSentence = reportSentence.replace("<","");
        reportSentence = reportSentence.replace(">","");
        reportSentence = reportSentence.replace("(en-US)","");

      // Output results
        if (HGlobal.DEBUG) {
        	sentenceElements.forEach((key, value) -> System.out.print(key + ":" + value + ","));
        	System.out.println();
        }
        //if (!EditEventCase) System.out.println(" Report entence build: " + reportSentence);
        return reportSentence;
    }

/*    At the adoption of [R:00003], <[D]>, <[L]> [RP:00008] was listed as the natural child of [R:00004] and [R:00005]. <[M]>
 *    [RP:00010] was [R:00003]'s birth father, as noted in the adoption proceedings <at [L2]> <in [L3]> <[D]>. <[M]>
 */

/**
 * private String returnSentenceVariableData(String sentenceVariable)
 * @param sentenceVariable
 * @return
 * @throws HBException
 */
    private String returnSentenceVariableData(String sentenceVariable) throws HBException {
    	if (EditEventCase)
    		return returnSentenceVariable(sentenceVariable);
		return pointReportEventTMG.returnSentenceVariable(sentenceVariable);
    }

/*    At the adoption of [R:00003], <[D]>, <[L]> [RP:00008] was listed as the natural child of [R:00004] and [R:00005]. <[M]>
 *    [RP:00010] was [R:00003]'s birth father, as noted in the adoption proceedings <at [L2]> <in [L3]> <[D]>. <[M]>
 */

    /**
     * private String returnSentenceVariable(String sentenceVariable)
     * @param sentenceVariable
     * @return
     * @throws HBException
     */
    		 public String returnSentenceVariable(String sentenceVariable) {
    			int rows = 0;
    			String roleNumber;
    			try {
    			// Proceess R - role variables
    		    	if (sentenceVariable.startsWith("R")) {
    		    		if (rows == 0)
    		    			rows = pointReportEventTMG.inittAssociatePersonsLists();

    		    		String[] roleData = sentenceVariable.split(":");
    		    		if (roleData.length > 1) {
    		    			roleNumber = roleData[1];
    		    			//System.out.println(" Role number: " + roleNumber);
    		    			if (pointReportEventTMG.isParnerEvent()) {
    		    				if (roleNumber.equals("00004")) return pointReportEventTMG.findPersonName(pointReportEventTMG.priPartnerPID);
    		    				if (roleNumber.equals("00003")) return pointReportEventTMG.findPersonName(pointReportEventTMG.secPartnerPID);
    		    				//if (roleNumber.equals("00004")) return findPersonName(secPartnerPID);
    		    			} else if (roleNumber.equals("00001") || roleNumber.equals("00003"))
    		    						return pointReportEventTMG.getPersonName();
    		    					else {
    		    			// Select the associate with the rolenumber
	    		    					if (rows == 0)
	    		    						rows = pointReportEventTMG.inittAssociatePersonsLists();
	    		    					return pointReportEventTMG.findPersonName(pointReportEventTMG.findAssociatePersonRole(roleNumber));
    		    					}
    		    		}
    		    	}

    		    // Process Px
    		    	if (sentenceVariable.startsWith("P")) {

    		    		if (sentenceVariable.equals("P") || sentenceVariable.equals("P+"))
    		    				return pointReportEventTMG.getPersonName();

    		    		if (sentenceVariable.equals("PO")) return pointReportEventTMG.getPersonOptional();

    		    		switch (sentenceVariable) {
    		    			case "PG":  return pointReportEventTMG.getPersonNameElement(3);
    		    			case "PF": return pointReportEventTMG.getPersonNameElement(3);
    		    			case "PL":  return pointReportEventTMG.getPersonNameElement(5);
    		    			case "PGS": return pointReportEventTMG.getPersonNameElement(3);
    		    			case "PFS":  return pointReportEventTMG.getPersonNameElement(3);
    		    			case "PLS":  return pointReportEventTMG.getPersonNameElement(5);
    		    			default: return "";
    		    		}
    		    	}

    		    // Process date
    		    	if (sentenceVariable.startsWith("D")) {
    		    		pointReportEventTMG.getEventDate();
    		    		//return  eventDate;
    		    	}

    		    // process Lx variables
    		    	if (sentenceVariable.startsWith("L")) {
    				 	if (sentenceVariable.equals("L")) return pointReportEventTMG.getLocationName();
    		    		switch (sentenceVariable) {
    		    			case "L1":  return pointReportEventTMG.getLocationNameElement(1);
    		    			case "L2":  return pointReportEventTMG.getLocationNameElement(2);
    		    			case "L3":  return pointReportEventTMG.getLocationNameElement(3);
    		    			case "L4":  return pointReportEventTMG.getLocationNameElement(4);
    		    			case "L5":  return pointReportEventTMG.getLocationNameElement(5);
    		    			case "L6":  return pointReportEventTMG.getLocationNameElement(6);
    		    			case "L7":  return pointReportEventTMG.getLocationNameElement(7);
    		    			case "L8":  return pointReportEventTMG.getLocationNameElement(8);
    		    			case "L9":  return pointReportEventTMG.getLocationNameElement(9);
    		    			case "L10":  return pointReportEventTMG.getLocationNameElement(10);
    		    			default: return "";
    		    		}
    		    	 }
    			    // process Sx variables
    		    	if (sentenceVariable.startsWith("S")) {
    		    		switch (sentenceVariable) {
    		    			case "S": return  pointReportEventTMG.getPersonName();
    		    			case "S+": return pointReportEventTMG.getPersonName();
    		    			case "SS": return pointReportEventTMG.getPersonName();
    		    			case "SG": return pointReportEventTMG.getPersonName();
    		    			case "SF": return pointReportEventTMG.getPersonName();
    		    			case "SL": return pointReportEventTMG.getPersonName();
    		    			case "SA": return "";
    		    			case "SE": return "";
    		    			case "SP": return "He/She";
    		    			case "SPP": return "";
    		    			case "SM": return "";
    		    			case "SGS": return "";
    		    			case "SFS": return "";
    		    			case "SLS": return "";
    		    			default: return "";
    		    		}
    		    	}

    		    // Process witness
    		    	 if (sentenceVariable.startsWith("W")) {
    		    		 rows = pointReportEventTMG.inittAssociatePersonsLists();
    		    		 if (rows < 1) return "";
    		    		 if (sentenceVariable.equals("W"))
    		    			 return pointReportEventTMG.findAssociatePersonName(1);
    					 if (sentenceVariable.equals("WO") && rows > 0)
    		    			 return pointReportEventTMG.findAssociatePersonName(1);
    					 if (sentenceVariable.equals("WO") && rows > 1)
    		    			 return pointReportEventTMG.findAssociatePersonName(2);
    					 if (sentenceVariable.equals("WM")) {
    		        			 return "Witness memo";
    		    		 }
    		    	 }

    		    // Process memo
    		    	 if (sentenceVariable.startsWith("M")) {
    						return pointReportEventTMG.getEventMemo();
    		    	 }
    		    	 return "";

    			} catch (HBException hbe) {
    				System.out.println(" ERROR in returnSentenceVariable: " + hbe.getMessage());
    				hbe.printStackTrace();
    			}
    			return "";
    	    }

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
	PRIM_ASSOC_SENTENCE_
*/

	public class ReportEventTMG extends HBBusinessLayer {
		HBProjectOpenData pointOpenProject;
		HBPersonHandler pointPersonHandler;
		HBWhereWhenHandler pointWhereWhenHandler;
		HBLibraryResultSet pointLibraryResultSet;
		//HREmemo pointHREmemo;
		long eventTablePID, memoRPID;
		String selectString, eventDate, locationName, eventPersonName, eventPersonNamePri, eventPersonNameSec ;
		ResultSet eventTableRS, personTableRS, assocTableRS, partnerTableRS;
		long personTablePID, nameStylePID, eventBestPersonNamePID, startHdate, eventLocationPID, priPartnerPID, secPartnerPID;
		int dataBaseIndex, viibleIdent, eventType, eventGroup;
		HashMap<String,String> personNameElements, locationNameElements;
		long[] associatePID;
		int[] associateRoles;
		String[] partnerNames, nameStyleCodes;
		boolean partnerEvent = false;
		int rows = 0;

		ReportEventTMG(HBProjectOpenData pointOpenProject, long eventTablePID) throws HBException {
			this.eventTablePID = eventTablePID;
			dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
			pointHREmemo = pointOpenProject.getHREmemo();
			pointPersonHandler = pointOpenProject.getPersonHandler();
			pointWhereWhenHandler = pointOpenProject.getWhereWhenHandler();
			pointLibraryResultSet = pointPersonHandler.pointLibraryResultSet;
			this.pointDBlayer = pointPersonHandler.pointDBlayer;
		// Look up data from eventable
			selectString = setSelectSQL("*", eventTable,"PID = " + eventTablePID);
			eventTableRS = requestTableData(selectString, dataBaseIndex);
			try {
				eventTableRS.first();
				eventType = eventTableRS.getInt("EVNT_TYPE");
				personTablePID = eventTableRS.getLong("PRIM_ASSOC_RPID");
				startHdate = eventTableRS.getLong("START_HDATE_RPID");
				eventLocationPID = eventTableRS.getLong("EVNT_LOCN_RPID");
				memoRPID = eventTableRS.getLong("MEMO_RPID");
		// look up data from persontable
				selectString = setSelectSQL("*", personTable, "PID = " + personTablePID);
				personTableRS = requestTableData(selectString, dataBaseIndex);
				personTableRS.first();
				eventBestPersonNamePID = personTableRS.getLong("BEST_NAME_RPID");
				viibleIdent = personTableRS.getInt("VISIBLE_ID");
				nameStyleCodes = getOuputReportStyleCodes(eventBestPersonNamePID);
		// Collect date and event group
				eventDate = pointPersonHandler.pointLibraryResultSet.exstractDate(startHdate, dataBaseIndex);
				eventGroup = pointPersonHandler.pointLibraryResultSet.getEventGroup(eventType, dataBaseIndex);
		    	if (eventGroup == pointPersonHandler.marrGroup || eventGroup == pointPersonHandler.divorceGroup) {
	    // Get and extract the partner names and roles
		    		partnerEvent = true;
					selectString = setSelectSQL("*", personPartnerTable, "EVNT_RPID = " + eventTablePID);
					partnerTableRS = requestTableData(selectString, dataBaseIndex);
					partnerTableRS.first();
					priPartnerPID = partnerTableRS.getLong("PRI_PARTNER_RPID");
					secPartnerPID = partnerTableRS.getLong("SEC_PARTNER_RPID");
					//priRoleNum = partnerTableRS.getInt("PRI_ROLE");
					//secRoleNum = partnerTableRS.getInt("SEC_ROLE");
					//eventType = partnerTableRS.getInt("PARTNER_TYPE");
					partnerTableRS.close();
    	    	} else {
    				eventPersonNamePri = eventPersonName;
    				//roleNamePri = pointWhereWhenHandler.getEventRoleName(eventType, roleNumber);
    			}

			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw new HBException("ReportEventData error: " + sqle.getMessage());
			}

			//System.out.println("ReportEventData -->");
/*
			for (Map.Entry<String, String> entry : locationNameElements.entrySet()) {
			    System.out.println(entry.getKey() + ": " + entry.getValue());
			}

			System.out.println();

			for (Map.Entry<String, String> entry : personNameElements.entrySet()) {
			    System.out.println(entry.getKey() + ": " + entry.getValue());
			}

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
		 public String returnSentenceVariable(String sentenceVariable) {
			int rows = 0;
			String roleNumber;
			try {
			// Proceess R - role variables
		    	if (sentenceVariable.startsWith("R")) {
		    		if (rows == 0)
		    			rows = collectAssociatePersons(eventTablePID);
		    		if (personNameElements == null)
		    			personNameElements =  pointPersonHandler.pointLibraryResultSet.
		    				selectPersonNameElements(eventBestPersonNamePID, dataBaseIndex);
		    		String[] roleData = sentenceVariable.split(":");
		    		if (roleData.length > 1) {
		    			roleNumber = roleData[1];
		    			//System.out.println(" Role number: " + roleNumber);
		    			if (partnerEvent) {
		    				if (roleNumber.equals("00004")) return findPersonName(priPartnerPID);
		    				if (roleNumber.equals("00003")) return findPersonName(secPartnerPID);
		    				//if (roleNumber.equals("00004")) return findPersonName(secPartnerPID);
		    			} else if (roleNumber.equals("00001") || roleNumber.equals("00003"))
		    					return getPersonName(personNameElements, viibleIdent);
		    				else {
		    			// Select the associate with the rolenumber
		    					if (rows == 0)
		    						rows = collectAssociatePersons(eventTablePID);
		    					int roleInt = Integer.parseInt(roleNumber);
		    					for (int index = 0; index < associatePID.length; index++)
		    						if (roleInt == associateRoles[index])
		    							return findPersonName(associatePID[index]);
		    				return "";
		    			}
		    		}
		    	}

		    // Process Px
		    	if (sentenceVariable.startsWith("P")) {
		    		if (personNameElements == null)
		    			personNameElements =  pointPersonHandler.pointLibraryResultSet.
		    				selectPersonNameElements(eventBestPersonNamePID, dataBaseIndex);
		    		if (sentenceVariable.equals("P") || sentenceVariable.equals("P+"))
		    				return getPersonName(personNameElements, viibleIdent);

		    		if (sentenceVariable.equals("PO")) {
		    			if (partnerEvent) {
		    				if (personTablePID == secPartnerPID)
		    					return findPersonName(priPartnerPID);
							return findPersonName(secPartnerPID);
		    			}
						rows = collectAssociatePersons(eventTablePID);
						if (rows < 1) return "";
						return findPersonName(associatePID[0]);
		    		}

		    		switch (sentenceVariable) {
		    			case "PG":  return personNameElements.get(personNameCodeArray[2]);
		    			case "PF": return personNameElements.get(personNameCodeArray[2]);
		    			case "PL":  return personNameElements.get(personNameCodeArray[4]);
		    			case "PGS": return personNameElements.get(personNameCodeArray[2]);
		    			case "PFS":  return personNameElements.get(personNameCodeArray[2]);
		    			case "PLS":  return personNameElements.get(personNameCodeArray[4]);
		    			default: return "";
		    		}
		    	}

		    // Process date
		    	if (sentenceVariable.startsWith("D")) {
		    		return  eventDate;
		    	}

		    // process Lx variables
		    	if (sentenceVariable.startsWith("L")) {
					if (locationNameElements == null)
						locationNameElements = pointPersonHandler.pointLibraryResultSet.
						   selectLocationNameElements(eventLocationPID, dataBaseIndex);
				 	if (sentenceVariable.equals("L")) return getLocationName();
		    		switch (sentenceVariable) {
		    			case "L1":  return locationNameElements.get(locationNameCodeArray[0]);
		    			case "L2":  return locationNameElements.get(locationNameCodeArray[1]);
		    			case "L3":  return locationNameElements.get(locationNameCodeArray[2]);
		    			case "L4":  return locationNameElements.get(locationNameCodeArray[3]);
		    			case "L5":  return locationNameElements.get(locationNameCodeArray[4]);
		    			case "L6":  return locationNameElements.get(locationNameCodeArray[5]);
		    			default: return "";
		    		}
		    	 }
			    // process Sx variables
		    	if (sentenceVariable.startsWith("S")) {
		    		switch (sentenceVariable) {
		    			case "S": return getPersonName(personNameElements, viibleIdent);
		    			case "S+": return getPersonName(personNameElements, viibleIdent);
		    			case "SS": return getPersonName(personNameElements, viibleIdent);
		    			case "SG": return getPersonName(personNameElements, viibleIdent);
		    			case "SF": return getPersonName(personNameElements, viibleIdent);
		    			case "SL": return getPersonName(personNameElements, viibleIdent);
		    			case "SA": return "";
		    			case "SE": return "";
		    			case "SP": return "He/She";
		    			case "SPP": return "";
		    			case "SM": return "";
		    			case "SGS": return "";
		    			case "SFS": return "";
		    			case "SLS": return "";
		    			default: return "";
		    		}
		    	}

		    // Process witness
		    	 if (sentenceVariable.startsWith("W")) {
		    		 if (rows == 0)
		    			 rows = collectAssociatePersons(eventTablePID);
		    		 if (rows < 1) return "";
		    		 if (sentenceVariable.equals("W"))
		    			 return findPersonName(associatePID[0]);
					 if (sentenceVariable.equals("WO") && rows > 0)
		    			 return findPersonName(associatePID[0]);
					 if (sentenceVariable.equals("WO") && rows > 1)
		    			 return findPersonName(associatePID[1]);
					 if (sentenceVariable.equals("WM")) {
		        			 return "Witness memo";
		    		 }
		    	 }

		    // Process memo
		    	 if (sentenceVariable.startsWith("M")) {
						return pointHREmemo.readMemo(memoRPID);
		    	 }
		    	 return "";

			} catch (HBException hbe) {
				hbe.printStackTrace();
			}
			return "";
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
				System.out.println(" HBReportHandler - getOuputStyleCodes: " + sqle.getMessage());
				sqle.printStackTrace();
				throw new HBException(" HBReportHandler - getOuputStyleCodes: " + sqle.getMessage());
			}

		}

/**
 * Returns partner Optional (PO) name or name first of person in associate list if not partener event
 * @return person name with visible id.
 * @throws HBException
 */
	public String getPersonOptional() throws HBException {
			if (partnerEvent) {
				if (personTablePID == secPartnerPID)
					return findPersonName(priPartnerPID);
				return findPersonName(secPartnerPID);
			}
			rows = collectAssociatePersons(eventTablePID);
			if (rows < 1) return "";
			return findPersonName(associatePID[0]);
		}

/**
 * Returns the name of the associate person with index number in Associate list
 * @param index - The number of the associate list startinng with first = 1
 * @return
 * @throws HBException
 */
		public String findAssociatePersonName(int index) throws HBException {
			index = index -1;
			if (index < 0 || index > rows) return "";
			return findPersonName(associatePID[index]);
		}

		private String findPersonName(long personTablePID) throws HBException {
			HashMap<String,String> personNameElements = null;
			int viibleIdent;
    		if (personNameElements == null)
    			personNameElements =  pointPersonHandler.pointLibraryResultSet.
    				selectPersonNameElements(eventBestPersonNamePID, dataBaseIndex);
			selectString = setSelectSQL("*", personTable, "PID = " + personTablePID);
			personTableRS = requestTableData(selectString, dataBaseIndex);
			try {
				personTableRS.first();
				eventBestPersonNamePID = personTableRS.getLong("BEST_NAME_RPID");
				viibleIdent = personTableRS.getInt("VISIBLE_ID");
				personNameElements =  pointLibraryResultSet.
						selectPersonNameElements(eventBestPersonNamePID, dataBaseIndex);
				return getPersonName(personNameElements, viibleIdent);
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw new HBException(" ReportEventData - collectAssocatePersons error: " + sqle.getMessage());
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
    				selectPersonNameElements(eventBestPersonNamePID, dataBaseIndex);
			return getPersonName(personNameElements, viibleIdent);
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
				if (roleInt == associateRoles[i])
					return associatePID[i];
		return 0L;
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
				assocTableRS.beforeFirst();
				while (assocTableRS.next()) {
					associatePID[index] = assocTableRS.getLong("ASSOC_RPID");
					associateRoles[index] = assocTableRS.getInt("ROLE_NUM");
					//System.out.println(" Assocs: " + associatePID[index] + "/" + associateRoles[index]);
					index++;
				}
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw new HBException(" ReportEventData - collectAssocatePersons error: " + sqle.getMessage());
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

	} // End ReportEventData
} // End Class HBReportHandler

class RelationCalculation extends HBBusinessLayer {
/************************************************************************************************
 * Class  RelationCalculation
 * Processes data for recalcukation of relationship code pairs
 * As created by ChatGPT to Create relationship codes
 * Sends requests to database over Database Layer API
 ************************************************************************************************
 * v0.05.0033 2026-04-10 - First draft (D Ferguson)
 * 			  2026-04-12 - Second draft (D Ferguson)
 * 			  2026-04-13 - Getting cousins almost right (D Ferguson)
 * 			  2026-04-14 - Remove invalid codes for no blood relationships (D Ferguson)
 * 			  2026-04-16 - Handle aunts/cousins greater than 4 generations (D Ferguson)
 * 			  2026-04-18 - Use ChatGPT code to fix all problems (D Ferguson)
 * 			  2026-04-19 - Rvised the code to java standard (N. Tolleshaug)
 ************************************************************************************************/

    protected ResultSet pointT401_PERSONS, selectedPersonRS;
    private  HBProjectOpenData pointOpenProject;
    private HBPersonHandler pointPersonHandler;
    private int dataBaseIndex;
    static HashMap<Long,Object[]> parentMap = new HashMap<Long,Object[]>();

/**
 * Constructor RelationCalculation(HBProjectOpenData pointOpenProject)
 * @param pointOpenProject
 */
	  public RelationCalculation(HBProjectOpenData pointOpenProject)  {
			  this.pointOpenProject = pointOpenProject;
			  long personTablePID;
			  dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
			  pointDBlayer = pointOpenProject.getPointDBlayer();
			  pointPersonHandler = pointOpenProject.getPersonHandler();
			  pointT401_PERSONS =  pointOpenProject.getT401Persons();
			  try {
				  pointT401_PERSONS.beforeFirst();
				  while (pointT401_PERSONS.next()) {
					  Object[] parents = new Object[2];
					  parents[0] = pointT401_PERSONS.getLong("SPERM_PROVIDER_RPID");
					  parents[1] = pointT401_PERSONS.getLong("EGG_PROVIDER_RPID");
					  personTablePID = pointT401_PERSONS.getLong("PID");
					  parentMap.put(personTablePID, parents);
				  }
			} catch (SQLException sqle) {
				System.out.println(" ERROR in  RelationCalculation: " + sqle.getMessage());
				sqle.printStackTrace();
			}
	  }
	  
/**
 * Updaye relation for a single person in T401	  
 * @param focusPersonPID - PID for project focus person
 * @param personTablePID - PID to the person to update
 * @throws HBException
 */
	  public void updatePersonRelations(long focusPersonPID, long personTablePID) throws HBException {
		  ResultSet selectedPersonRS;
		  Relationship relation;
		  String selecStringSQL;
		  int T401_RELATE1, T401_RELATE2, T401_RELATE3, T401_RELATE4;
			if (personTablePID != null_RPID) {
				selecStringSQL = setSelectSQL("*", personTable,"PID = " + personTablePID);
				try {
					selectedPersonRS= requestTableData(selecStringSQL, dataBaseIndex);
					selectedPersonRS.last();
					if (selectedPersonRS.getRow() == 0) 
						throw new HBException(" updatePersonRelations - No data found");
					
					relation = findRelationships(focusPersonPID, personTablePID );
				// following code to update relations
					T401_RELATE1 = relation.primary.x;
					T401_RELATE2 = relation.primary.y;
					T401_RELATE3 = relation.secondary.x;
					T401_RELATE4 = relation.secondary.y;
					
					selectedPersonRS.first();
					selectedPersonRS.updateInt("RELATE1", T401_RELATE1);
					selectedPersonRS.updateInt("RELATE2", T401_RELATE2);
					selectedPersonRS.updateInt("RELATE3", T401_RELATE3);
					selectedPersonRS.updateInt("RELATE4", T401_RELATE4);
					selectedPersonRS.updateRow();
				} catch (SQLException sqle) {
					System.out.println(" ERROR in  updatePersonRelations: " + sqle.getMessage());
					sqle.printStackTrace();
					throw new HBException("ERROR in  updatePersonRelations: " + sqle.getMessage());
				}
			} else throw new HBException(" updatePersonRelations - No data found");
	  }

/**
 * Recalculate Relationships ans save in all T401 records
 * public void recalcRelationsT401(long focusPersonPID)
 * @param focusPersonPID
 * @throws HBException
 */
	  public void recalcRelationsT401(long focusPersonPID) throws HBException {
		  long personTablePID;
		  Relationship relation;
		  int numberOfPerson = 0, errorIndex = 0;
		  int T401_RELATE1, T401_RELATE2, T401_RELATE3, T401_RELATE4;
		  int RELATE1, RELATE2;
		  boolean consoleT401 = false; // Controls testout of T401
		  pointT401_PERSONS =  pointOpenProject.getT401Persons();
		  int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		  if (consoleT401) System.out.println(" Start test relations!");
		// Start transaction
		  updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
		  try {
			  pointT401_PERSONS.beforeFirst();
			  while (pointT401_PERSONS.next()) {
				  personTablePID = pointT401_PERSONS.getLong("PID");
				  RELATE1 = pointT401_PERSONS.getInt("RELATE1");
				  RELATE2 = pointT401_PERSONS.getInt("RELATE2");
				  relation = findRelationships(focusPersonPID, personTablePID );
				// following code to update relations
				  T401_RELATE1 = relation.primary.x;
				  T401_RELATE2 = relation.primary.y;
				  T401_RELATE3 = relation.secondary.x;
				  T401_RELATE4 = relation.secondary.y;
				  if (consoleT401) 
					  if (RELATE1 != T401_RELATE1 || RELATE2 != T401_RELATE2) {
						  errorIndex++;
						  System.out.println(" " + errorIndex + " - " + pointPersonHandler.getPersonName(personTablePID)
					  			+ " - PID: "+ personTablePID + " TMG: (" + RELATE1 + "," + RELATE2 + ") HRE: "
							    + "(" + T401_RELATE1 + " , " + T401_RELATE2 + ")");
					  }
				  
				  pointT401_PERSONS.updateInt("RELATE1", T401_RELATE1);
				  pointT401_PERSONS.updateInt("RELATE2", T401_RELATE2);
				  pointT401_PERSONS.updateInt("RELATE3", T401_RELATE3);
				  pointT401_PERSONS.updateInt("RELATE4", T401_RELATE4);
				  pointT401_PERSONS.updateRow();
				  numberOfPerson++;
			  }
			  if (consoleT401) System.out.println( " Deviations: " + errorIndex + " Total tested: " + numberOfPerson);
		// End transaction
			  updateTableData("COMMIT", dataBaseIndex);
		  } catch (SQLException sqle) {
		// Roll back transaction
				updateTableData("ROLLBACK", dataBaseIndex);
				if (HGlobal.writeLogs) {
					HB0711Logging.logWrite("ERROR: in recalcRelationsT401 " + sqle.getMessage());	//$NON-NLS-1$
					HB0711Logging.printStackTraceToFile(sqle);
				}
		  }
	  }
	  
/**
 * Clear the relation varibales in T401	  
 * @throws HBException
 */
	  public void clearRelationDataInT401() throws HBException {	 
		  pointT401_PERSONS =  pointOpenProject.getT401Persons();
		  int dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		// Start transaction
		  updateTableData("SET AUTOCOMMIT OFF;", dataBaseIndex);
		  try {
			  pointT401_PERSONS.beforeFirst();
			  while (pointT401_PERSONS.next()) {			  
				  pointT401_PERSONS.updateInt("RELATE1", 0);
				  pointT401_PERSONS.updateInt("RELATE2", 0);
				  pointT401_PERSONS.updateInt("RELATE3", 0);
				  pointT401_PERSONS.updateInt("RELATE4", 0);
				  pointT401_PERSONS.updateRow();
			  }
			 
		// End transaction
			  updateTableData("COMMIT", dataBaseIndex);
		  } catch (SQLException sqle) {
		// Roll back transaction
				updateTableData("ROLLBACK", dataBaseIndex);
				if (HGlobal.writeLogs) {
					HB0711Logging.logWrite("ERROR: in recalcRelationsT401 " + sqle.getMessage());	//$NON-NLS-1$
					HB0711Logging.printStackTraceToFile(sqle);
				}
		  }
	  
	  }

/**
 * Return parents of a given PID
 * public static Object[] getParentsPID(long personTablePID)
 * @param personTablePID
 * @return
 */
    public static Object[] getParentsPID(long personTablePID) {
    	if (parentMap.containsKey(personTablePID))
    		return parentMap.get(personTablePID);
		return null;
    }

/**
 * Class Relationship (x,y) pair
 */
    public static class Relationship {
        public CodePair primary;
        public CodePair secondary;
        public Relationship(CodePair p1, CodePair p2) {
            this.primary = p1;
            this.secondary = p2;
        }
    }
    
/**
 * public static class CodePair to store code pairs
 */
    public static class CodePair {
        public int x, y;
        public CodePair(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

/**
 * Build ancestor map: PID generations up from startPID
 *           depth 0 = self, 1 = parent, 2 = grandparent, ...
 * public static Map<Long, Integer> buildAncestorMap(long startPID)
 * @param startPID
 * @return
 */
    @SuppressWarnings("unused")
	private static Map<Long, List<Integer>> buildAncestorMap(
            long pid,
            long NULL_RPID,
            int maxDepth ) {

        Map<Long, List<Integer>> map = new HashMap<>();
        Queue<long[]> q = new LinkedList<>();
        q.add(new long[]{pid, 0});

        while (!q.isEmpty()) {
            long[] cur = q.poll();
            long id = cur[0];
            int depth = (int) cur[1];
            if (depth > maxDepth) continue;
            if (id == NULL_RPID) continue;

            map.computeIfAbsent(id, k -> new ArrayList<>()).add(depth);
            Object[] parents = getParentsPID(id);
            if (parents == null) continue;
            for (Object p : parents) {
                if (p == null) continue;
                long parent = (Long) p;
                if (parent == NULL_RPID) continue;
                q.add(new long[]{parent, depth + 1});
            }
        }
        return map;
    }

/**
 * Find Relationship pairs between focus and target
 * @param focusPID
 * @param targetPID
 * @return
 */
    public static Relationship findRelationships(long focusPID, long targetPID) {

        final long NULL_RPID = 1999999999999999L;
     // Assumed that no project will be deeper than 50 generations
        final int MAX_DEPTH = 50;

        if (focusPID == targetPID) 
        	return new Relationship(new CodePair(0,0), new CodePair(0,0));

        Map<Long, List<Integer>> focusMap =
                buildAncestorMap(focusPID, NULL_RPID, MAX_DEPTH);
        Map<Long, List<Integer>> targetMap =
                buildAncestorMap(targetPID, NULL_RPID, MAX_DEPTH);

/*
 * STEP 1: find BEST common ancestor (single winner only)
 */
        Long bestAncestor = null;
        int bestScore = Integer.MAX_VALUE;
        int bestDF = -1;
        int bestDT = -1;

        for (Long id : focusMap.keySet()) {
            if (!targetMap.containsKey(id)) continue;

            for (int dF : focusMap.get(id)) {
                for (int dT : targetMap.get(id)) {
                	int min = Math.min(dF, dT);
                	int max = Math.max(dF, dT);
                	// strong priority order
                	int score =
                	        (min * 1000) +     // degree (most important)
                	        (max * 10) +       // removal
                	        (dF + dT);         // tie-break
                    if (score < bestScore) {
                        bestScore = score;
                        bestAncestor = id;
                        bestDF = dF;
                        bestDT = dT;
                    }
                }
            }
        }

/*
 * STEP 2: no relationship found
 */
        if (bestAncestor == null) 
        	return new Relationship(new CodePair(0,0), new CodePair(0,0));
        
/*
 * STEP 3: derive EXACT ONE relationship
 */
        CodePair primary;
/*
 * SELF
 */ 
        if (bestDF == 0 && bestDT == 0)
        	primary = new CodePair(0, 0);

/*
 * LINEAL
 */
        else if (bestDT == 0)
        	primary = new CodePair(0, bestDF);
        else if (bestDF == 0)
        	primary = new CodePair(-bestDT, 0);

/*
 * SIBLINGS
 */
        else if (bestDF == 1 && bestDT == 1)
        	primary = new CodePair(-1, -1);

/*
 * COLLATERAL
 */
        else if ((bestDF == 1 && bestDT == 2) || (bestDF == 2 && bestDT == 1)) {
/*
 * CRITICAL RULE:
 * DO NOT interpret DF/DT as direction.
 * ONLY interpret which side is closer to LCA.
 */
        	if (bestDF < bestDT) {
        		// focus is closer to LCA → focus is younger branch
        		primary = new CodePair(-2, -1); // niece/nephew
        	} else {
        		// target is closer → focus is older branch
        		primary = new CodePair(-1, -2); // uncle/aunt
        	}
        }

/*
 * COUSINS
 */
        else {
        	int min = Math.min(bestDF, bestDT);
        	int max = Math.max(bestDF, bestDT);
        	int cousin = min - 1;
        	int removed = max - min;
        	primary = new CodePair(
        			-(cousin + 1),
        			-(cousin + 1 + removed));
        }
        return new Relationship(primary, new CodePair(0,0));
    }
} // End class RelationCalculation
