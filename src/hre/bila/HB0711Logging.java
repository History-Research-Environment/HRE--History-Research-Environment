package hre.bila;
/********************************************************************************
 * Logging - Specification 07.11 BR_Logging of 2019-05-10
 * v0.00.0007 2019-05-17 by D Ferguson
 * v0.00.0011 2019-09-07 add 'Manage' function to delete old log files (D Ferguson)
 * v0.00.0014 2019-11-18 changes to implement static classes (D Ferguson)
 * v0.00.0022 2020-06-28 standardise file separators in filepaths (D Ferguson)
 * v0.00.0026 2021-01-08 fix file separator error (D Ferguson)
 * v0.01.0025 2021-01-28 removed use of HGlobal.selectedProject (N. Tolleshaug)
 * 			  2021-01-28 Modification needed for logging HRE start (N. Tolleshaug)
 * 			  2021-01-29 fix another file separator error (D Ferguson)
 * 			  2021-03-23 move error msgs to HGlobal routine (N. Tolleshaug)
 * 			  2021-04-05 add method to print Java stack trace to log (N. Tolleshaug)
 * v0.03.0031 2023-12-28 changed WildcardFileFilter to match commons-io-2.15.1 version (D Ferguson)
 * v0.05.0033 2026-02-22 Get Project name correctly for the log record (D Ferguson)
 *******************************************************************************
* For documentation on the OpenCSV functions used here, see:
* https://sourceforge.net/projects/opencsv/
* http://opencsv.sourceforge.net/apidocs/com/opencsv/CSVWriterBuilder.html
* http://opencsv.sourceforge.net/apidocs/com/opencsv/CSVReaderBuilder.html
********************************************************************************/

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;

import hre.gui.HG0401HREMain;
import hre.gui.HGlobal;
import hre.gui.HGlobalCode;

/**
 * Log file read, write, manage
 * @author D Ferguson
 * @version v0.05.0033
 * @since 2019-05-17
 */

public class HB0711Logging {

	private static String logfileName;			// for path + filename of this Log file
	public static List<String[]> allLogEntries;	// for all log records from a Read operation
	private static String noName = " ------- ";

/**
 * START of logWrite - create the log file if it doesn't exist
 * @param actionEntry is the Log entry to be written
 * @throws HBException
 */
	public static void logWrite(String actionEntry) {

		// First, build the Log File name = User Home directory + /HRE/ + Username + date + " log.hrel"
		logfileName = System.getProperty("user.home") + File.separator + "HRE" + File.separator + HGlobal.userID + " " + LocalDate.now() + " log.hrel";
		// We know the HRE directory exists in the User's home directory, as the UserAUX module is
		// called before this module is called, so it must have been created.
		// However, there may or may not be a log file with today's date, as this depends
		// on whether this is the first use of HRE for today, or not.
		File logFile = new File(logfileName);
		HGlobal.currentLogFile = logfileName;
		// Build the Log file header record and new log record to be added to log file
		String[] headerRecord = { "Date","Time","Server","Project","Log Entry" };
		String nowDate = LocalDate.now().toString();
		String nowTime = LocalTime.now().toString();

	// Select open project in the list and prepare output
		String projectName = noName;

		if (HG0401HREMain.mainFrame != null)
			projectName = HG0401HREMain.mainFrame.getStatusProject();
		else projectName = noName;

		if (HGlobal.DEBUG)
			System.out.println("Logged project: " + projectName);

		String[] logRecord = { nowDate, nowTime,  HGlobal.thisComputer, projectName, actionEntry} ;

    // Now create an Arraylist and load it with 1 or both Strings
		List<String[]> allLogRecords = new ArrayList<>();
		if (!logFile.exists())
			allLogRecords.add(headerRecord);	// if no log file exists, write header record first
		allLogRecords.add(logRecord);

		try (
			FileWriter fileWriter = new FileWriter(logfileName, true);		//true=append
			 CSVWriter writer = new CSVWriter(fileWriter, ICSVWriter.DEFAULT_SEPARATOR,
		     ICSVWriter.NO_QUOTE_CHARACTER, ICSVWriter.NO_ESCAPE_CHARACTER,
		     ICSVWriter.DEFAULT_LINE_END)) {
			 writer.writeAll(allLogRecords);

		} catch (Exception wre) {
	   	    HGlobalCode.logErrorMessage(1, wre.getMessage());		// Write error
	   	}
	} // End of logWrite

/**
 * START of logRead
 * @param actionEntry is the filename to be opened and read
 */
	public static void logRead(String actionEntry) {

		// Make full file path/name and check it exists
		logfileName = System.getProperty("user.home") + File.separator + "HRE" + File.separator + actionEntry;
		File logFile = new File(logfileName);
		if (!logFile.exists()) {			// if log file does not exist, write error message and exit
			HGlobalCode.logErrorMessage(2, "");
			return;
			}
		try { CSVParser csvParser = new CSVParserBuilder().withSeparator(',').build();
			  CSVReader reader = new CSVReaderBuilder(new FileReader(logfileName)).withCSVParser(csvParser).withSkipLines(1).build();
			  allLogEntries = reader.readAll();
			  reader.close();
	   	}  catch (Exception ex) {
	   		HGlobalCode.logErrorMessage(3, ex.getMessage());	// Read error
	   		}
	}	// End of logRead

/**
 * START of logManage - remove old Log files (leave 3)
 */
	public static void logManage() {
		// Set directory to where the logfiles should be
		File directory = new File(System.getProperty("user.home") + File.separator + "HRE" + File.separator);
		// get all logfile names
		FileFilter fileFilter = WildcardFileFilter.builder().setWildcards("*log.hrel").get();
		File[] logfiles = directory.listFiles(fileFilter);
		// sort them into descending order (oldest first)
		Arrays.sort(logfiles, NameFileComparator.NAME_REVERSE);
		// if 3 or fewer files exist, we're done, else delete the rest
		if (logfiles.length > 3) {
			for ( int i = 3; i < logfiles.length; i++ ) {
				logfiles[i].delete();
			}
		}
     }	// End of logManage

/**
 * printStackTraceToFile - add Java trace to HRE Log file in HRE log format
 */
	public static void printStackTraceToFile(Exception exc) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		exc.printStackTrace(printWriter);
		// Modify each line of the Java trace to match HRE Log file
		// format by replacing each line starting "(tab)at " with ",,,,at "
		String csvFormat = stringWriter.toString().replace("	at ", ",,,,at ");
		logWrite("Exception: " + csvFormat);
	}	// End of printStackTraceToFile

} // End HB0711Logging