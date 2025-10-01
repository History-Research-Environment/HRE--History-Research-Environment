package hre.gui;
/************************************************************************
 * COMMON CODE MODULE
 * Split off from HGlobal 2023-05-02
 ************************************************************************
 * v0.00.0029 2023-05-01 - Common static methods for HRE (D. Ferguson)
 * v0.04.0032 2025-09-26 - Add Source Element convertNumsToNames (D Ferguson)
 *************************************************************************/
import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import hre.bila.HBException;
import hre.bila.HBProjectOpenData;
import hre.nls.HGlobalMsgs;
/**
 * Common methods for HRE (hre.bila and hre.gui)
 * @author Don Ferguson
 * @since 2023-05-02
 * @version build 0.01.2923.0501
 */
public class HGlobalCode {

/*************************************
 * ROUTINES FOR TABLE HEADERS
 ************************************/
/**
 * Supply Table Header for Project tables in HG040x
 * @return String[] projectTableHeader
 */
	public static String[] projectTableHeader() {
		String[] projectTableHdr =
			{HGlobalMsgs.Text_52,	// Project Name
			HGlobalMsgs.Text_53,	// Filename
			HGlobalMsgs.Text_54};	// Local/Remote Server
		return projectTableHdr;
	}

/**
 * String getLocalText()
 * @return String "Local"
 */
	public static String getLocalText() {
		return HGlobalMsgs.Text_55;
	}

/**
 * Supply Table Header for Remote Projects table in HG0418
 * @return String[] rmtprojTableHeader
 */
	public static String[] rmtprojTableHeader() {
		String[] rmtprojTableHdr =
			{HGlobalMsgs.Text_56,		// Project Name
			HGlobalMsgs.Text_57,		// Filename
			HGlobalMsgs.Text_58};		// Server
		return rmtprojTableHdr;
	}

/********************************
 * UNIVERSAL VALIDATION METHODS
 *******************************/
/**
 * Validates name string, returns true or false
 * nameRegex expression from https://owasp.org/www-community/OWASP_Validation_Regex_Repository as of 17 Mar 2020
 * @param name to check (must be a-z, A_Z '.' '-' ' ')
 */
	public static boolean isNameValid(String name) {
		String nameRegex = "^[a-zA-Z .-]+$"; //$NON-NLS-1$
		Pattern pat = Pattern.compile(nameRegex);
		return pat.matcher(name).matches();
	}  	// End isNameValid

/**
 * Validates email address string, returns true or false
 * emailRegex expression from https://owasp.org/www-community/OWASP_Validation_Regex_Repository as of 5 Mar 2020
 * @param email to check
 */
	public static boolean isEmailValid(String email) {
		if (email == null) return false;
		if(email.length() == 0) return false;
		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" + "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"; //$NON-NLS-1$ //$NON-NLS-2$
		Pattern pat = Pattern.compile(emailRegex);
		return pat.matcher(email).matches();
	}  	// End isEmailValid

/**
 * Validates IP address string, returns true or false
 * addrIPRegex expression from https://regexlib.com/ as of 30 Sep 2021
 * @param addrIP to check
 */
	public static boolean isIPValid(String addrIP) {
		String addrIPRegex =
			"^(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])$"; //$NON-NLS-1$
		Pattern pat = Pattern.compile(addrIPRegex);
		return pat.matcher(addrIP).matches();
	}  	// End isIPValid

/**
 * Validates 'strong password' string, returns true or false
 * Strong password (Baggaley) expression from https://regexlib.com/ as of 20 Oct 2021
 * Input must contain 1 Caps, 1 lowercase, 1 number, 1 special char, at least 8 chars
 * @param passWord to check
 */
	public static boolean isPswdGood(String passWord) {
		String pswdRegex =	"((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[\\W_]).{8,})"; //$NON-NLS-1$
		Pattern pat = Pattern.compile(pswdRegex);
		return pat.matcher(passWord).matches();
	}  	// End isPswdGood

/**********************
 * COMMON METHODS
 *********************/
/**
 * Return by name project pointer from openProjects ArrayList
 * @param ProjectName Name of project to return
 * @return String[] with project data
 * @throws HBException
 */
	public static HBProjectOpenData pointOpenProjectByName(String projectName) throws HBException {
		for (HBProjectOpenData element : HGlobal.openProjects) {
			if (element.getProjectData()[0].trim().equals(projectName.trim())) {
				if (HGlobal.DEBUG) System.out.println("Found open project: " + element.getProjectData()[0]); //$NON-NLS-1$
				return element;
			}
		}
		if (HGlobal.DEBUG) System.out.println("Not found: " + projectName); //$NON-NLS-1$
		throw new HBException("Open project not in list of open projects: " + projectName); //$NON-NLS-1$
	}

/**
 * Collect summary data for open selected project
 * @param String [] project
 * @return String[][] summaryData
 */
	public static String[][] getSummaryData(String[] project) {
		return getSummaryData(project, HGlobalMsgs.Text_67);	// Closed project
	}

/**
 * Collect summary data for open selected project
 * @param String [] project
 * @param String databaseVersion
 * @return String[][] summaryData
 */
	public static String[][] getSummaryData(String[] project, String databaseVersion) {
		String [][] summary = new String[][] {
			{HGlobalMsgs.Text_68, " " + currentTime("yyyy-MM-dd / HH:mm:ss")},	// Time Now + time //$NON-NLS-1$ //$NON-NLS-2$
			{HGlobalMsgs.Text_71, " " + project[0]},							// Project Name //$NON-NLS-1$
			{HGlobalMsgs.Text_73, " " + project[1]},							// File Name //$NON-NLS-1$
			{HGlobalMsgs.Text_75, " " + project[2]},							// Folder Name //$NON-NLS-1$
			{HGlobalMsgs.Text_77, " " + project[3]},							// Server Name //$NON-NLS-1$
			{HGlobalMsgs.Text_79, " " + project[4]},							// Last Closed //$NON-NLS-1$
			{HGlobalMsgs.Text_81, " " + project[6]},							// Last Backup //$NON-NLS-1$
			{HGlobalMsgs.Text_83, " " + project[5]},							// Database Type //$NON-NLS-1$
			{HGlobalMsgs.Text_85, " " + databaseVersion}						// Database Ver: //$NON-NLS-1$
		};
		return summary;
	}

/**
 * Get current system time format yyyy-MM-dd / HH:mm:ss
 * @return String time
 */
	  public static String currentTime(String dataTimeFormat) {
		  DateTimeFormatter dtf = DateTimeFormatter.ofPattern(dataTimeFormat);
		   LocalDateTime now = LocalDateTime.now();
		   return dtf.format(now);
	  }

/**
 * Measure time consumption	when opening project
 */
	private static long oneMilliSec = 1000L;
	private static long startTime,timeElapsed;
	public static void recordStart() {
		startTime = System.currentTimeMillis();
	}
	public static void timeReport(String eventMessage) {
		timeElapsed = System.currentTimeMillis() - startTime;
		System.out.println(" Used to " + eventMessage 				  //$NON-NLS-1$
				+ ": " + (double)timeElapsed / oneMilliSec + " sec"); //$NON-NLS-1$ //$NON-NLS-2$
	}

/*************************************
 * MESSAGE ROUTINES FOR STARTUP CODE
 ************************************/
/**
 * error message from HB0711Logging
 * @param errorCode
 * @param errorMessage with the reported error
 */
	public static void logErrorMessage(int errorCode, String errorMessage) {
		String errorType = ""; //$NON-NLS-1$
		if(errorCode == 1) errorType = HGlobalMsgs.Text_91 ;		// User Log file write error: \n
		if(errorCode == 2) errorType = HGlobalMsgs.Text_92 ;		// Log file requested does not exist
		if(errorCode == 3) errorType = HGlobalMsgs.Text_93 ;		// User Log file read error: \n
	    JOptionPane.showMessageDialog(null, errorType + errorMessage, HGlobalMsgs.Text_94,		// Log Error Message
	    			JOptionPane.ERROR_MESSAGE);
	}	// End logErrorMessage

/**
 * error message from HB0744UserAUX
 * @param errorCode
 * @param errorMessage
 */
	public static void loguserAuxMessage(int errorCode, String errorMessage) {
		String errorType = ""; //$NON-NLS-1$
		if(errorCode == 1) errorType = HGlobalMsgs.Text_96 ;		// User AUXfile write error: \n
		if(errorCode == 2) errorType = HGlobalMsgs.Text_97 ;		// Invalid User Auxiliary file - file deleted
		if(errorCode == 3) errorType = HGlobalMsgs.Text_98 ;		// User Auxiliary file UserID invalid - file deleted
		if(errorCode == 4) errorType = HGlobalMsgs.Text_99;			// User AUX file read error: \n
	    JOptionPane.showMessageDialog(null, errorType + errorMessage, HGlobalMsgs.Text_100,		// UserAUX Error Message
	    			JOptionPane.ERROR_MESSAGE);
	}	// End loguserAuxMessage

/**
 * TCPIP Server message from HA0001HREstart OR HG0501AppSettings
 * @param msgCode
 * @param txtMessage is server info (IP addr:port)
 */
	public static void logTCPIPMessage(int msgCode, String msgInfo) {
		String message = ""; //$NON-NLS-1$
		if(msgCode == 1) message = HGlobalMsgs.Text_102;		// HRE TCP server started on
		if(msgCode == 2) message = HGlobalMsgs.Text_103;		// HRE TCP server mode stopped
	    JOptionPane.showMessageDialog(null, message + msgInfo, HGlobalMsgs.Text_104,		// HRE Server Status
	    			JOptionPane.INFORMATION_MESSAGE);
	}	// End logTCPIPMessage

/*************************************
 * ROUTINES FOR TABLE CELL TABBING
 ************************************/
/**
 * JTable Cell Tabbing - Set Action Map for tab, shift-tab and Enter for a JTable
 * @param theTable - JTable this applies to
 * @param startRow - valid start row for tabbing [range 0 - (numRows-1) ]
 * @param numRows -  Number of rows this applies to
 * @param startCol - valid start col for tabbing [range 0 - (numCols-1) ]
 * @param numCols -  Number of columns this applies to
 */
	public static class JTableCellTabbing {
		public static void setTabMapping(final JTable theTable, final int startRow, final int numRows, final int startCol, final int numCols) {
			// Calculate last row and column for tabbing
		    final int endRow = startRow + (numRows - 1);
		    final int endCol = startCol + (numCols - 1);
		    // Check for valid range
		    if ((startRow > endRow) || (startCol > endCol)) {
		        throw new IllegalArgumentException("Table Size incorrectly set for JTableCellTabbing");	//$NON-NLS-1$
		    }
		    // Get Input and Action Map to set tabbing order on the JTable
		    InputMap im = theTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		    ActionMap am = theTable.getActionMap();
		    // Code for Enter key
		    KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		    am.put(im.get(enterKey), new AbstractAction() {
		        @Override
		        public void actionPerformed(ActionEvent e) {
		        	int row = theTable.getSelectedRow();
		            int col = theTable.getSelectedColumn();
		            // Next row
		            row++;
		            // If at end, clear action on last row and move to next focus component
		            if (row > endRow ) {
		            	theTable.getCellEditor(endRow, col).stopCellEditing();
		            	theTable.getSelectionModel().removeSelectionInterval(endRow, endRow);
		            	KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
		            }
		            // Else change cell selection
		            	else {
		            		theTable.changeSelection(row, col, false, false);
		            		theTable.editCellAt(row, col);
		            		}
		        }
		    });
		    // Code for TAB key
		    KeyStroke tabKey = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
		    am.put(im.get(tabKey), new AbstractAction() {
		        @Override
		        public void actionPerformed(ActionEvent e) {
		            int row = theTable.getSelectedRow();
		            int col = theTable.getSelectedColumn();
		            col++;
		            // Move to next row and left column
		            if (col > endCol) {
		                col = startCol;
		                row++;
		            }
		            // If at end, clear action on last row and move to next focus component
		            if (row > endRow ) {
		            	theTable.getCellEditor(endRow, col).stopCellEditing();
		            	theTable.getSelectionModel().removeSelectionInterval(endRow, endRow);
		            	KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
		            }
		            // Else change cell selection
		            	else {
		            		theTable.changeSelection(row, col, false, false);
		            		theTable.editCellAt(row, col);
		            		}
		        }
		    });
		    // Code for SHIFT_TAB (tab backwards)
		    KeyStroke shiftTab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, java.awt.event.InputEvent.SHIFT_DOWN_MASK);
		    am.put(im.get(shiftTab), new AbstractAction() {
		        @Override
		        public void actionPerformed(ActionEvent e) {
		            int row = theTable.getSelectedRow();
		            int col = theTable.getSelectedColumn();
		            col--;
		            // Move to top cell
		            if (col < startCol) {
		                col = endCol;
		                row--;
		            }
		            // If at start, clear action on row and move to previous focus component
		            if (row < startRow ) {
		            	theTable.getCellEditor(startRow, startCol).stopCellEditing();
		            	theTable.getSelectionModel().removeSelectionInterval(startRow, startRow);
		            	KeyboardFocusManager.getCurrentKeyboardFocusManager().focusPreviousComponent();
		            }
		            // Else change cell selection
		            	else {
		            		theTable.changeSelection(row, col, false, false);
		            		theTable.editCellAt(row, col);
		            	}
		        }
		    });
		}
	}	// End JTableCellTabbing

/**
 * Internal class focusPolicy extends FocusTraversalPolicy
 * @author Don Ferguson
 * @since 2023-05-02
 * @version build 0.01.2923.0501
 */
    public static class focusPolicy extends FocusTraversalPolicy {
        Vector<Component> focusOrder;
        public focusPolicy(Vector<Component> focusOrder) {
            this.focusOrder = new Vector<>(focusOrder.size());
            this.focusOrder.addAll(focusOrder);
        }
        @Override
		public Component getComponentAfter(Container focusCycleRoot, Component aComponent)  {
            int idx = (focusOrder.indexOf(aComponent) + 1) % focusOrder.size();
            return focusOrder.get(idx);
        }
        @Override
		public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
            int idx = focusOrder.indexOf(aComponent) - 1;
            if (idx < 0) idx = focusOrder.size() - 1;
            return focusOrder.get(idx);
        }
        @Override
		public Component getDefaultComponent(Container focusCycleRoot) {
            return focusOrder.get(0);
        }
        @Override
		public Component getLastComponent(Container focusCycleRoot) {
            return focusOrder.lastElement();
        }
        @Override
		public Component getFirstComponent(Container focusCycleRoot) {
            return focusOrder.get(0);
        }
    }		// End focusPolicy


/******************************************************************************************
 * ROUTINES FOR SOURCE ELEMNT NUMBER->NAME AND NAME->NUMBER CONVERSION in SOURCE TEMPLATES
 *****************************************************************************************/
/**
 * convertNumsToNames - convert SourceElement numbers to Element names in template parameter
 * @param template
 * @param hashmap
 * @return converted template
 */
	public static String convertNumsToNames(String template, Map<String, String> codeToTextMap) {
		// Setup a regex to find [nnnnn] entries in template
		Pattern pattern = Pattern.compile("\\[(\\d{5})\\]");
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
 */
	public static String convertNamesToNums(String template, Map<String, String> textToCodeMap) {
	// Setup a regex to find [Element names] entries in template
		Pattern pattern = Pattern.compile("\\[[^\\]]+\\]");
		Matcher matcher = pattern.matcher(template);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String original = matcher.group(); 	// e.g. "[element-name]"
            if (original.contains(":")) {		 // Skip colon-containing entries like [IAL:]
                matcher.appendReplacement(result, Matcher.quoteReplacement(original));
                continue;
            }
            // Convert the Source Element names to 5-digit strings via the hashmap
            String code = textToCodeMap.get(original.trim());
            // Throw an error if no match - user used an element name that doesn't exist
            if (code == null) {
                JOptionPane.showMessageDialog(null,
                    "Unknown Source Element name: " + original,
                    "Source Element name error",
                    JOptionPane.ERROR_MESSAGE);
                return null; // halt processing
            }
            // otherwise keep going
            matcher.appendReplacement(result, "[" + Matcher.quoteReplacement(code) + "]");
        }
        matcher.appendTail(result);
        return result.toString();
	}		// End convertNamesToNums

}		// End HGlobalCode