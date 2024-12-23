package hre.tmgjava;
/****************************************************************************************
 * TMG to HRE MEMO handler
 ****************************************************************************************
 * v0.00.0029 2023-04-23 - Firt version (N. Tolleshaug)
 * v0.01.0029 2023-05-01 - Implemented v22a (N. Tolleshaug)
 * v0.01.0030 2023-08-15 - Test for long CLOB element (N. Tolleshaug)
 * 		      2023-09-06 - if (language.equals("DUTCH")) code = "nl-NL"; (N. Tolleshaug)
 *****************************************************************************************/
import java.sql.ResultSet;
import java.sql.SQLException;

import hre.bila.HBException;
/**
 * Converter from TMG date to HRE HDate
 * v0.00.0030 2023-04-24 - Implemented according to T167
 * @author NTo
 * @Since  2023-04-23
 */
public class HREmemo {

	static long proOffset = 1000000000000000L;
	static long null_RPID  = 1999999999999999L;
	static long memoPID  = proOffset;
	HREdatabaseHandler pointHREbase;
	//ResultSet hreTable;
	String memoTable = "T167_MEMO_SET";

	public HREmemo(HREdatabaseHandler pointHREbase) {
		this.pointHREbase = pointHREbase;
		//hreTable = TMGglobal.T167;
		try {
			memoPID = pointHREbase.lastRowPID(memoTable);
			if (TMGglobal.DEBUG) 
				System.out.println(" Last PID in Table " + memoPID);
		} catch (HBException hbe) {
			System.out.println("Table " + memoTable + " empty");
			memoPID  = proOffset;
			hbe.printStackTrace();
		}
	}

	public long addToT167_22c_MEMO(String memoElement) throws HCException {
		memoPID++;
		addToT167_MEMO_SET(memoPID, memoElement, TMGglobal.T167);
		return memoPID;
	}

/**
 * addToT167_MEMO_SET
 * @param rowPID
 * @param hreTable
 * @throws HCException
 * H2 metadata for:
 * T167_MEMO_SET (
	PID BIGINT NOT NULL,
	CL_COMMIT_RPID BIGINT NOT NULL,
	IS_LONG BOOLEAN NOT NULL,
	SHORT_MEMO VARCHAR(500),
	LONG_MEMO CLOB(30K));
 */
	public void addToT167_MEMO_SET(long primaryPID,
									String memoElement,
									ResultSet hreTable) throws HCException {
		String shortMemo;
		try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();
			hreTable.updateLong("PID", primaryPID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			if (memoElement.length() <= 500) {
				hreTable.updateBoolean("IS_LONG", false);
				hreTable.updateString("SHORT_MEMO", memoElement);
			} else {
				hreTable.updateBoolean("IS_LONG", true);
				shortMemo = memoElement.substring(0,500);
				hreTable.updateString("SHORT_MEMO", shortMemo);
				if (memoElement.length() > 30000) {
					System.out.println(" Memo element > 30000 - truncated - length: " + memoElement.length());
					memoElement = memoElement.substring(0,30000);
				}
				hreTable.updateClob("LONG_MEMO", pointHREbase.createNClob(memoElement));
			}
		//Insert row
			hreTable.insertRow();

		} catch (SQLException sqle) {
			if (TMGglobal.DEBUG)
				System.out.println("HREmemo - addToT167_MEMO_SET - error: ");
			sqle.printStackTrace();
			throw new HCException("HREmemo - addToT167_MEMO_SET - error: "
					+ sqle.getMessage());
		}
	}

/**
 * CREATE TABLE T204_DATA_TRAN(
	PID BIGINT NOT NULL,
	CL_COMMIT_RPID BIGINT NOT NULL,
	IS_SYSTEM BOOLEAN NOT NULL,
	GUI_ID CHAR(5) NOT NULL,
	TABL_ID SMALLINT NOT NULL,
	LANG_CODE CHAR(5) NOT NULL,
	DATA VARCHAR(400),
	ABBR VARCHAR(200)
	);
 * @throws HCException
 */
	public static void addToT204_DATA_TRAN(long tablePID,
											int tableID,
											String dataString,
											String abbrString,
											ResultSet hreTable) throws HCException {
		try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();
		// Update new row in H2 database
			hreTable.updateLong("PID", tablePID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateBoolean("IS_SYSTEM", false);
			hreTable.updateString("GUI_ID", "00000");
			hreTable.updateInt("TABL_ID", tableID);
			hreTable.updateString("LANG_CODE", "en-Us");
			hreTable.updateString("DATA", dataString);
			hreTable.updateString("ABBR", abbrString);
		//Insert row in database
			hreTable.insertRow();
		} catch (SQLException sqle) {
			if (TMGglobal.DEBUG)
				System.out.println("HREmemo - addToT204_DATA_TRAN - error: ");
			sqle.printStackTrace();
			throw new HCException("HREmemo - T204_DATA_TRAN - error: "
					+ sqle.getMessage());
		}
	}

/**
 * returnStringContent(String content)
 * @param content
 * @return
 */
	public static String returnStringContent(String content) {
		if (content == null) return "";
		if (content.length() == 64) {
		   if (content.trim().length() == 0) return "";
		}
		return content;
	}

/**
 * getLangCode(String language)
 * @param language
 * @return
 */
	public static String getLangCode(String language) {
		String code = "--";
		if (language.equals("AFRIKAANS")) code = "af-ZA";
		if (language.equals("ENGLISH")) code = "en-US";
		if (language.equals("ENGLISHUK")) code = "en-GB";
		if (language.equals("DANISH")) code = "da-DK";
		if (language.equals("DUTCH")) code = "nl-NL";
		if (language.equals("FRENCH")) code = "fr-FR";
		if (language.equals("GERMAN")) code = "de-DE";
		if (language.equals("ITALIAN")) code = "it-IT";
		if (language.equals("NORWEGIAN")) code = "no-NB";
		if (language.equals("NORWEGIA2")) code = "no-NN";
		return code;
	}
}


