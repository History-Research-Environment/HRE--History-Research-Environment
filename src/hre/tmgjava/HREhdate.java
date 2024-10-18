package hre.tmgjava;
/****************************************************************************************
 * TMG to HRE converter HDATE
 ****************************************************************************************
 * v0.00.0025 2020-11-22 - File separator updated for file path strings (N. Tolleshaug)
 * v0.00.0026 2021-05-20 - Test printout of special dates (N. Tolleshaug)
 * v0.00.0027 2022-01-27 - Test on number of chars i hdate from TMG (N. Tolleshaug)
 * v0.00.0030 2023-08-08 - Test long irregular date,  number of chars from TMG (N. Tolleshaug)
 * 			  2023-08-13 - Test for numeric value for Integer.parseInt (N. Tolleshaug)
 * 			  2023-08-15 - If TMG data length is 20 add one "0" (N. Tolleshaug)
 * 			  2024-07-16 - Updated TMG to Hdate according to HRE (N. Tolleshaug)
 *****************************************************************************************/
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

/**
 * Converter from TMG date to HRE HDate
 * v0.00.0022 2020-06-30 - HDate implemented according to T751
 * @author NTo
 * @Since 2020-06-30
 */
public class HREhdate {
	static long this_project_unset_rpid = 1999999999999999999L;
	static long main_years = 0, extra_years = 0;
	static String main_details = "", extra_details = "";
	static String sort_date_code = "";
	static int nrOldStyles = 0;
	static int nrIrrDates = 0;
	static int nrQestionMarks = 0;
	private static Vector<String> oldStyles = new Vector<>();
	private static Vector<String> irrDates = new Vector<>();
	private static Vector<String> qestionMarks = new Vector<>();

	static long proOffset = 1000000000000000L;
	static long null_RPID  = 1999999999999999L;

	// Holds the PID value for T170
	static long hdatePID  = proOffset;

/**
 * Constructor
 */
	public HREhdate() {
		System.out.println("Start HDATE");
	}

/**
 * addToT751_HDATE
 * @param rowPID
 * @param hreTable
 * @throws HCException
 */
	public static long addToT170_22a_HDATES(ResultSet hreTable, String hdate) throws HCException {
		return addToT170_21c_HDATES(hreTable, hdate);
	}
	
/**
 * addToT170_HDATES(ResultSet hreTable, String hdate)
 * @param hreTable
 * @param hdate
 * @return
 * @throws HCException
 */
	public static long addToT170_21c_HDATES(ResultSet hreTable, String hdate) throws HCException {

	// Increase PID for each record
		hdatePID++;
		long recordPID = hdatePID;
		if (TMGglobal.DEBUG)
			System.out.println("** addToT170_21c_HDATES PID: " + recordPID + " TMG Date: "
						+ hdate + " Length: " + hdate.length());

	// Test number of chars in hdate from TMG and exclude illegal dates
		if (hdate.length() != 21 && !hdate.startsWith("0")) {
			System.out.println(" WARNING - HREhdate - addToT170_22a TMG date/sort: "
					+ hdate.length() + " hdatePID: " + hdatePID + " TMGdate: " + hdate);
			if (hdate.length() == 20) hdate = hdate + "0";
	 		else throw new HCException("Illegal number of chars in TMG date/sort size: " + hdate.length() +
					" TMGdate: " + hdate); 
		}

	// Convert from TMG to HDate variables
		tmgToT170hdate(hdate);

		// Set up T751 record
		try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();
		// Insert data
			hreTable.updateLong("PID", recordPID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateBoolean("IS_SYSTEM", false);
			hreTable.updateLong("DISPLAY_DATE_RPID", recordPID);
			hreTable.updateLong("SORT_DATE_RPID", recordPID);
			hreTable.updateLong("REFERENCE_HDATE_RPID", null_RPID);
			hreTable.updateLong("MAIN_LOCALE_DST_RPID", null_RPID);
			//*******************************************************
			hreTable.updateLong("MAIN_HDATE_YEARS", getMainYears());
			hreTable.updateString("MAIN_HDATE_DETAILS", getMainDetails());
			//*****************************************************
			//hreTable.updateLong("EXTRA_LOCALE_DST_RPID", startPID);
			hreTable.updateLong("LOCALE_DST_RPID", null_RPID);
			hreTable.updateLong("HDATE_YEARS", getExtraYears());
			hreTable.updateString("HDATE_DETAILS", getExtraDetails());
	//****************************************************
			hreTable.updateBoolean("HAS_CITATIONS", false);
			hreTable.updateLong("MEMO_RPID", null_RPID);
			hreTable.updateString("SURETY","---------");
	//*******************************************************
			hreTable.updateLong("SORT_LOCALE_DST_RPID", null_RPID);
			hreTable.updateString("SORT_HDATE_CODE",getSortDateCode());

		//Insert row
			hreTable.insertRow();
			return recordPID;

		} catch (SQLException sqle) {
			if (TMGglobal.DEBUG) System.out.println("Not able to update table - T170_DATES");
			sqle.printStackTrace();
			throw new HCException("TMGPass_Names V21a - addTo table T170_DATES" + " - error: " + sqle.getMessage());
		}
	}

/**
 * Get methods for HRE HDate
 */
	static public long getMainYears() {
		return main_years;
	}

	static public long getExtraYears() {
		return extra_years;
	}

	static public String getMainDetails() {
		return main_details;
	}

	static public String getExtraDetails() {
		return extra_details;
	}

	static public String getSortDateCode() {
		return sort_date_code;
	}

	static public int getNrIrrDates() {
		//return nrIrrDates;
		return irrDates.size();
	}

	static public int getOldStyleDates() {
		//return nrOldStyles;
		return oldStyles.size();
	}

	static public int getQuestionMarks() {
		//return nrQestionMarks;
		return qestionMarks.size();
	}

	static public void printSpecialDates() {
		System.out.println();
		System.out.println(" Question marks: " + getQuestionMarks() + " / " + nrQestionMarks);
		System.out.println(" OLD Style date: " + getOldStyleDates() + " / " + nrOldStyles);
		System.out.println(" Irregular date: " + getNrIrrDates() + " / " + nrIrrDates);
		System.out.println();
	}
/**
 * START--- TMG Date to HRE Hdate Conversion method
 * Called first to set up HDate variables
 * @param tmg_date
 */
	static public void tmgToT170hdate(String tmg_date) {

		String main_days, main_months, main_hours, main_minutes, main_seconds,
			main_milliseconds,main_offset_units, main_offset_value;

		String extra_days = "", extra_months = "", extra_hours = "", extra_minutes = "", extra_seconds = "",
			extra_milliseconds = "",extra_offset_units = "", extra_offset_value = "";

		// DEFAULT SETTINGS
		String sort_date_start = "11";
		String year_header = "550000";
		String start_details = "";
		int dates_required = 1;
		char sort_qualifier_code = '0';
	// Origin data
		char date_origin = 'N';
		String calendar_type = "CE";
		char question_mark_detail = 'N';
		char question_mark_sort = '0';

	// Classify the the type of date entry
		char first_char = tmg_date.charAt(0);
	// has leading "0"
		if (first_char == '0') {
	// irregular date
			if(tmg_date.length() == 1) {
				// irregular date with no text desription
				// EXTRA CODE NEEDED HERE ///
				System.out.println("Irregular date no content = " + tmg_date);
			} else {
				nrIrrDates++;
				if (!irrDates.contains(tmg_date)) {
					irrDates.add(tmg_date);
					if(TMGglobal.TRHDATE) System.out.println("Irrnr: " + nrIrrDates + " New irregular: " + tmg_date);
				}
				String irregular_text = tmg_date.substring(1);
				main_years = 0;
				main_details =	"I";
				extra_years =	0;
		
			// Irregular date can be up o 29 chars
				if (irregular_text.length() > 24) {
					System.out.println(" @@ Long Irregular date (max 24) , text = " + irregular_text + " / length: "
							+ irregular_text.length());
					irregular_text = irregular_text.substring(0,24);
				}
			// Temp solution - Irregular text in extra_details
				extra_details =	irregular_text;
				sort_date_code = "3";
	
				if (TMGglobal.DEBUG) System.out.println("@@ Irregular date value = " + irregular_text);
			}
		} else {
	// output regular date and return

			char old_style = tmg_date.charAt(9);
	// has old style
			if (old_style == '1') {
				nrOldStyles++;
				if (!oldStyles.contains(tmg_date)) {
					oldStyles.add(tmg_date);
					if (TMGglobal.TRHDATE) System.out.println("OS nr: " + nrOldStyles + " New old style: " + tmg_date);
				}
				calendar_type = "CO";
				if (TMGglobal.DEBUG) System.out.println("@@ TMG old style date  = " + tmg_date);
			} else calendar_type = "CE";

			start_details = "" + date_origin + calendar_type;

		// question mark
			if (tmg_date.charAt(20) =='1') {
				nrQestionMarks++;
				if (!qestionMarks.contains(tmg_date)) {
					qestionMarks.add(tmg_date);
					if(TMGglobal.TRHDATE) System.out.println("Qm nr: " + nrQestionMarks + " New questionm: " + tmg_date);
				}
				if (TMGglobal.DEBUG) System.out.println("@@ TMG question mark date  = " + tmg_date);
				question_mark_detail = 'Y';
				question_mark_sort = '1';
			}

		// Main Date Data
			main_years = Long. parseLong(tmg_date.substring( 1, 5));
			//if (main_years == 0) main_years = years_not_set;
			main_months = tmg_date.substring( 5, 7);
			if (main_months.equals("00")) main_months = "%%";
			main_days = tmg_date.substring( 7, 9);
			if (main_days.equals("00")) main_days = "%%";
			main_hours = "%%";
			main_minutes = "%%";
			main_seconds = "%%";
			main_milliseconds = "%%%";
			main_offset_units = "__";
			main_offset_value = "000";

			//char sort_code = '0';
			char qualifier_code = '%';

		// TMG qualifier settings
			char qualifier = tmg_date.charAt(10);
			switch(qualifier) {

				case '0':
					qualifier_code = 'B';	// ELEMENT_CODE "Before"
					sort_qualifier_code = '1';
					break;

				case '1':
					qualifier_code = 'S';	// ELEMENT_CODE "Say"
					sort_qualifier_code = '2';
					break;

				case '2':
					qualifier_code = 'C';	// ELEMENT_CODE "Circa"
					sort_qualifier_code = '3';
					break;

				case '3':
					qualifier_code = 'X';	// ELEMENT_CODE "Exact"
					sort_qualifier_code = '4';
					break;

				case '4':
					qualifier_code = 'A';	// ELEMENT_CODE "After"
					sort_qualifier_code = '5';
					break;

				case '5':
					qualifier_code = 'W';	// ELEMENT_CODE "Between / And"
					sort_qualifier_code = '6';
					dates_required = 2;

					break;

				case '6':
					qualifier_code = 'O';	//  ELEMENT_CODE "Either / Or"
					sort_qualifier_code = '7';
					dates_required = 2;
					break;

				case '7':
					qualifier_code = 'F';	// ELEMENT_CODE "From / To"
					sort_qualifier_code = '8';
					dates_required = 2;
					break;

				default: System.out.println("Unknown Main Qualifier = " + tmg_date);
			}

			start_details = "" + date_origin + calendar_type;
			main_details = start_details + main_months + main_days + main_hours;
			main_details = main_details	+ main_minutes + main_seconds + main_milliseconds;
			main_details = main_details	+ qualifier_code + question_mark_detail
							+ main_offset_units + main_offset_value;
/**
 * 	Check for 2 time point cases
 *	single date point case - set extra_years and extra_details as null
 *	extra date exists
 */
			if (dates_required == 2) {
				extra_years = Long. parseLong(tmg_date.substring( 11, 15));
				//if (extra_years == 0) extra_years = years_not_set;
				extra_months = tmg_date.substring( 15, 17);
				if(extra_months.equals("00")) extra_months = "%%";
				extra_days = tmg_date.substring( 17, 19);
				if (extra_days.equals("00")) extra_days = "%%";
				extra_hours = "%%";
				extra_minutes = "%%";
				extra_seconds = "%%";
				extra_milliseconds = "%%%";
				extra_offset_units = "__";
				extra_offset_value = "000";
				extra_details = start_details + extra_months + extra_days + extra_hours;
				extra_details = extra_details + extra_minutes + extra_seconds + extra_milliseconds;
				extra_details = extra_details + qualifier_code + question_mark_detail
								+ extra_offset_units + extra_offset_value;

			} else {
				extra_years = 0;
				extra_details = "0";
			}

/**
 * Prepare 	sort_date_code
 */
			// now start the SORT_DATE_CODE
				sort_date_code = sort_date_start + year_header;
			// Add date repeated
			for(int m = 1; m <= 8; m++) {
				char data_char = tmg_date.charAt(m);
				sort_date_code = sort_date_code + data_char;
				if (dates_required == 1) {
					// repeat main year, month, day data
					sort_date_code = sort_date_code + data_char;
				} else {
					// use extra year, months, years data
					sort_date_code = sort_date_code + tmg_date.charAt(m + 10);
				}
			}

			// allow for addition of hours(2), minutes(2), seconds(2), milliseconds(3)
			sort_date_code = sort_date_code + "0000" + "0000" + "0000" + "000000";

			// now include qualifier values
			sort_date_code = sort_date_code + sort_qualifier_code + question_mark_sort;
			sort_date_code = sort_date_code + "000000000000";
		}
	}
/**
 * isInteger(String numericValue) 	
 * @param numericValue
 * @return
 */
    public static boolean isInteger(String numericValue) {
        if (numericValue == null) return false;
        try {
            Integer.parseInt(numericValue);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public static String prepare4digit(int value) {
    	String tmgDate = "";
    	if (value < 9999)
	        if (value > 1000) 
	        	tmgDate = "" + value;
	        else if (value > 100) 
	        	tmgDate = tmgDate + "0" + value;
	        else if (value > 10) 
	        	tmgDate = tmgDate + "00" + value;
	        else tmgDate = tmgDate + "000" + value;
        return tmgDate;
    }

/**
 * dumpHdateStrings()
 */
	static public void dumpHdateStrings() {
		System.out.println("@@ MAIN_years = " + getMainYears());
		System.out.println("@@ MAIN_details = " + getMainDetails()
				+ " / " + main_details.length());
		System.out.println("@@ EXTRA_years = " + getExtraYears());
		System.out.println("@@ EXTRA_details = " + getExtraDetails()
				+ " / " + extra_details.length());
		System.out.println("@@ SORT_DATE_CODE = " + getSortDateCode() +
				" / " + sort_date_code.length());
	}

}
