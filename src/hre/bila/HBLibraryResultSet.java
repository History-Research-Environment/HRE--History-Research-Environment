package hre.bila;
/******************************************************************************************
 * Convert ResultSet to User GUI components
 * Library for HBProjectHandler
 * v0.00.0016 2019-12-13 - First version (N. Tolleshaug)
 * v0.00.0017 2020-02-17 - Implemented handling project of name
 * 							 and add/delete T131 users (N. Tolleshaug)
 * v0.00.0019 2020-02-12 - addUserToTable error corrected (N. Tolleshaug)
 * 						   updateUserIntable error fixed (N. Tolleshaug)
 * v0.00.0023 2020-08-12 - Implemented collect T175 label data from T204
 * v0.01.0026 2021-05-16 - Implemented for DDL21a with modification (N. Tolleshaug)
 * 			  2021-06-04 - Removed empty line with no placedata
 *            2021-07-05 - Pre-loading ResultSet for location name element (N. Tolleshaug)
 *            2021-08-08 - Modified for new seed database - HRE Seed database v21ac.mv.db (NTo)
 *            2021-10-10 - Keep T131 password if password == null (N. Tolleshaug)
 * v0.01.0027 2021-11-09 - Indexed T_553 loction name elements (N. Tolleshaug)
 * 			  2021-11-10 - Indexed T_403 person name elements (N. Tolleshaug)
 * 			  2021-11-10 - Removed HashMap for name and place element table (N. Tolleshaug)
 * 			  2021-11-10 - Removed HashMap for HDATE table (N. Tolleshaug)
 * 			  2022-02-05 - Test setup for T204_LABEL_TRANS_NEW and Norwegian (N. Tolleshaug)
 * 			  2022-02-25 - initiate personPID map from visibleID (N. Tolleshaug)
 * 			  2022-08-07 - New methods for Name Style Handling(N. Tolleshaug)
 * 			  2022-10-07 - Update Name Style Element Description (N. Tolleshaug)
 * v0.01.0028 2022-10-14 - Update Name Style Location (N. Tolleshaug)
 * 			  2022-11-10 - Update for comma handling in person name output (N. Tolleshaug)
 * 			  2022-11-18 - Update for comma handling in location name output (N. Tolleshaug)
 * 			  2023-01-19 - New method for select location name elements (N. Tolleshaug)
 * 			  2023-03-21 - Corrected error in selectPersonName - last element (N. Tolleshaug)
 * v0.03.0030 2023-06-03 - Updated for database v22a (N. Tolleshaug)
 * 			  2023-06-06 - Line 1239 Removed "en-" for role name - TMG error (N. Tolleshaug)
 * 			  2023-06-06 - Removed "en-" for event name (N. Tolleshaug)
 * v0.03.0031 2023-11-16 - Updated with methods for get roles and get parent role (N. Tolleshaug)
 * 			  2023-11-16 - Corrected last row PID and first row PID (N. Tolleshaug)
 * 			  2023-12-08 - Updated parent role list (N. Tolleshaug)
 * 			  2024-02-20 - Updated read result set eventtype (N. Tolleshaug)
 * 			  2024-03-11 - Line 1359 - getEventName Updated for fall back to en-US (N. Tolleshaug)
 * 			  2024-02-30 - Updated read result set eventtype for both parents (N. Tolleshaug)
 * 			  2024-03-31 - Updated getEventTypeList for event group selection (N. Tolleshaug)
 * 			  2024-10-19 - getPersonBirthEventPID() returns null_RPID if no birth event(N. Tolleshaug)
 * 			  2024-10-21 - getEventGroup() checks only em-US for eventGroup
 * 			  2024-11-11 - exstractPersonName - updated with "No name found"
 * *****************************************************************************************
 * NOTE 01 - Update of table T104 - last PID for T131 is not implemented
 * NOTE 02 - Commit table update not implemented
 * NOTE 03 - updateUserIntable need to have position set in calling method
 *******************************************************************************************/

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.util.HashMap;
import java.util.Vector;
//import java.util.logging.Level;
//import java.util.logging.Logger;

import javax.swing.table.DefaultTableModel;

import hre.dbla.HDException;
import hre.gui.HGlobal;

/**
 * HBLibraryResultSet contains ResultSet processing methods
 * @author Nils Tolleshaug
 * @version v0.03.0031
 * @since 2019-12-09
 */
public class HBLibraryResultSet {

	static boolean DEBUG = true;
	HBBusinessLayer pointBusinessLayer = null;

    long proOffset  = 1000000000000000L;
    long null_RPID  = 1999999999999999L;
/**
 * HBResultSetLibrary Constructor
 */
	public HBLibraryResultSet(HBBusinessLayer pointBusinessLayer) {
		this.pointBusinessLayer = pointBusinessLayer;
		if (HGlobal.DEBUG) {
			System.out.println("HBLibraryResultSet initiated");
		}
	}

/**
 * getNameStylesNames(int dataBaseIndex)
 * @param dataBaseIndex
 * @return
 * @throws HBException
 */

	public ResultSet getNameStylesTable(String tableName, String nameType, int dataBaseIndex) throws HBException {
		String selectString;
		ResultSet nameStyleTable;
		selectString = pointBusinessLayer.
				setSelectSQL("*", tableName,"NAME_TYPE = '" + nameType + "'");
		nameStyleTable = pointBusinessLayer.requestTableData(selectString, dataBaseIndex);
		return nameStyleTable;
	}


	public ResultSet getOutputStylesTable(String tableName, String nameType, long ownerPID, int dataBaseIndex) throws HBException {
		String selectString;
		ResultSet nameStyleTable;
		selectString = pointBusinessLayer.
				setSelectSQL("*", tableName,"NAME_TYPE = '" + nameType + "' AND OWNER_RECORD_RPID = " + ownerPID);
		nameStyleTable = pointBusinessLayer.requestTableData(selectString, dataBaseIndex);
		return nameStyleTable;
	}


	public ResultSet getNameStyleElementTable(String nameType, String tableName, int dataBaseIndex) throws HBException {
		String selectString;
		ResultSet nameStyleElements;
		selectString = pointBusinessLayer.
				setSelectSQL("*", tableName, "NAME_TYPE = '" + nameType + "'");
		nameStyleElements = pointBusinessLayer.requestTableData(selectString, dataBaseIndex);
		return nameStyleElements;
	}


	public ResultSet getNameStyleElements(String tableName, String nameType, String langCode, int dataBaseIndex) throws HBException {
		String selectString;
		ResultSet nameStyleElements;
		selectString = pointBusinessLayer.
				setSelectSQL("*", tableName, "LANG_CODE = '" + langCode + "' AND NAME_TYPE = '" + nameType + "'");
		nameStyleElements = pointBusinessLayer.requestTableData(selectString, dataBaseIndex);
		return nameStyleElements;
	}

/**
 * getNameStyleOutput(String tableName, int dataBaseIndex)
 * @param tableName
 * @param dataBaseIndex
 * @return
 * @throws HBException
 */

	public String[] getNameStyleOutput(String tableName, long ownerRPID, String nameType, int selectNameStyleIndex, int dataBaseIndex) throws HBException {
		String selectString = pointBusinessLayer.

				setSelectSQL("*", tableName,"OWNER_RECORD_RPID = '" + ownerRPID
											+ "'AND NAME_TYPE = '" + nameType
											+ "' AND OUT_TYPE = 'D'");
		ResultSet nameStyleOutputCodes;
		try {
			nameStyleOutputCodes = pointBusinessLayer.requestTableData(selectString, dataBaseIndex);
		// Check if the stores absolute selectNameStyleIndex in absolute exceeds result set size
			if (nameStyleOutputCodes.absolute(selectNameStyleIndex + 1)) {
				nameStyleOutputCodes.absolute(selectNameStyleIndex + 1);
			} else {
				nameStyleOutputCodes.absolute(1);
			}

			String styleCodes = nameStyleOutputCodes.getString("OUT_ELEMNT_CODES");
			if (HGlobal.DEBUG) {
				System.out.println(" Name Style Codes: " + nameType + " / " + selectNameStyleIndex + " / " + styleCodes);
			}
			return styleCodes.split("\\|");
		} catch (SQLException sqle) {
			throw new HBException("LibraryResultSet - getNameStyleOutput: " + sqle.getMessage());
		}
	}

/**
 * Add new user to  T160
 * @param pointResultSet
 * @param userData
 * @throws HBException
 * T160_NAME_STYLE
�		PID
�		CL_COMMIT_RPID
�		IS_SYSTEM
�		IS_DEFAULT
�		IS_TMG
�		NAME_TYPE
�		NAME_STYLE
		NAME_STYLE_DESC
		ELEMNT_NAMES
�		ELEMNT_CODES
*/

	public void addNameStyleToTable(ResultSet pointResultSet, long lastPID, String nameType, String [] userData) throws HBException {

		long startPID  = 1000000000000001L;
		long valuePID;
		try {
			if (HGlobal.DEBUG) {
				System.out.println("ResultSet - addNameStyleToTable: ");
			}

		// Increment PID for next row i table or start with empty database
			//pointResultSet.last();
			if (HGlobal.DEBUG) {
				System.out.println("Last row in ResultSet: " + lastPID);
			}
			if (pointResultSet.getRow() == 0) {
				valuePID = startPID;
			} else {
				valuePID = lastPID;
			}

			pointResultSet.moveToInsertRow(); // moves cursor to the insert row

		// Update new row in ResultSet
		    pointResultSet.updateLong("PID", valuePID);
			pointResultSet.updateLong("CL_COMMIT_RPID", null_RPID);
			pointResultSet.updateBoolean("IS_SYSTEM", false);
			pointResultSet.updateBoolean("IS_DEFAULT", false);
			pointResultSet.updateBoolean("IS_TMG", false);
			pointResultSet.updateString("NAME_TYPE", nameType);
			pointResultSet.updateString("NAME_STYLE", userData[0]);
			pointResultSet.updateString("NAME_STYLE_DESC", userData[1]);
			pointResultSet.updateString("ELEMNT_CODES", userData[2]);
			pointResultSet.updateString("ELEMNT_NAMES", userData[3]);
			pointResultSet.insertRow();
			pointResultSet.beforeFirst();

		} catch (SQLException sqle) {
			if (HGlobal.DEBUG) {
				System.out.println("Not able to update for: " + userData[0]);
			}
			sqle.printStackTrace();
			throw new HBException("HBLibraryResultSet - addNameStyleToTable: " + sqle.getMessage());
		}
	}

/**
 *
 * @param pointResultSet
 * @param lastPID
 * @param nameType
 * @param userData
 * @throws HBException
 * T162_NAME_STYLE_OUTPUT
�		PID
�		CL_COMMIT_RPID
�		IS_SYSTEM
�		IS_TMG
�		OWNER_RECORD_RPID
		OUT_TYPE
�		NAME_TYPE
�		OUT_NAME_STYLE
�		OUT_NAME_STYLE_DESC
�		OUT_ELEMNT_CODES
 */

	public void addOutputStyleToTable(ResultSet pointResultSet, long lastPID,
												long ownerRPID, String nameType,
												String [] userData) throws HBException {
		long startPID  = 1000000000000001L;
		long valuePID = lastPID +1;
		try {
			if (HGlobal.DEBUG) {
				System.out.println("ResultSet - addNameStyleToTable: ");
			}

		// Increment PID for next row i table or start with empty database
			//pointResultSet.last();
			if (HGlobal.DEBUG) {
				System.out.println("Last row in ResultSet: " + lastPID);
			}
			if (pointResultSet.getRow() == 0) {
				valuePID = startPID;
			} else {
				valuePID = lastPID;
			}

			pointResultSet.moveToInsertRow(); // moves cursor to the insert row

		// Update new row in ResultSet
		    pointResultSet.updateLong("PID", valuePID);
			pointResultSet.updateLong("CL_COMMIT_RPID", null_RPID);
			pointResultSet.updateBoolean("IS_SYSTEM", false);
			pointResultSet.updateBoolean("IS_TMG", false);
			pointResultSet.updateLong("OWNER_RECORD_RPID", ownerRPID);
			pointResultSet.updateString("OUT_TYPE", userData[0]);
			pointResultSet.updateString("NAME_TYPE", nameType);
			pointResultSet.updateString("OUT_NAME_STYLE", userData[1]);
			pointResultSet.updateString("OUT_NAME_STYLE_DESC", userData[2]);
			pointResultSet.updateString("OUT_ELEMNT_CODES", userData[3]);
			pointResultSet.insertRow();
			pointResultSet.beforeFirst();

		} catch (SQLException sqle) {
			if (HGlobal.DEBUG) {
				System.out.println("Not able to update for: " + userData[0]);
			}
			sqle.printStackTrace();
			throw new HBException("HBLibraryResultSet - addNameStyleToTable: " + sqle.getMessage());
		}

	}
/**
 * Update user to  T160
 * @param pointResultSet
 * @param userData
 * @throws HBException
 *  T160_NAME_STYLE
�	PID
�	CL_COMMIT_RPID
�	IS_SYSTEM
�	IS_DEFAULT
�	IS_TMG
�	NAME_TYPE
�	NAME_STYLE
�	NAME_STYLE_DESC
�	ELEMNT_NAMES
�	ELEMNT_CODES
*/

	public void updateNameStyleTable(ResultSet pointResultSet, String [] userData) throws HBException {
		try {

		// Update new row in ResultSet
			pointResultSet.updateString("NAME_STYLE", userData[0]);
			pointResultSet.updateString("NAME_STYLE_DESC", userData[1]);
			pointResultSet.updateString("ELEMNT_NAMES", userData[2]);
			pointResultSet.updateString("ELEMNT_CODES", userData[3]);
			pointResultSet.updateRow();
			pointResultSet.beforeFirst();

		} catch (SQLException sqle) {
			if (HGlobal.DEBUG) {
				System.out.println("Not able to update for: " + userData[0]);
			}
			sqle.printStackTrace();
			throw new HBException("HBLibraryResultSet - updateNameStyleTable: " + sqle.getMessage());
		}
	}

/**
 *
 * @param pointResultSet
 * @param userData
 * @throws HBException
  T163_PERS_NAME_ELEMNTS
 	PID
 	CL_COMMIT_RPID
 	IS_SYSTEM
 	LANG_CODE
 	DATA_STRING
 	DATA_CODES
 */

	public void updateElementName(ResultSet pointResultSet, String dataString) throws HBException {
		try {

		// Update data string in ResultSet
			pointResultSet.updateString("ELEMNT_NAMES", dataString);
			pointResultSet.updateRow();
		} catch (SQLException sqle) {
			if (HGlobal.DEBUG) {
				System.out.println("HBLibraryResultSet - updateElementName: " + dataString);
			}
			sqle.printStackTrace();
			throw new HBException("HBLibraryResultSet - updateElementName: " + sqle.getMessage());
		}
	}


	public void updateElementList(ResultSet pointResultSet, String nameString, String codeString) throws HBException {
		// Update data string in ResultSet
		try {
			pointResultSet.updateString("ELEMNT_NAMES", nameString);
			pointResultSet.updateString("ELEMNT_CODES", codeString);
			pointResultSet.updateRow();
		} catch (SQLException sqle) {
			if (HGlobal.DEBUG) {
				System.out.println("HBLibraryResultSet - updateElementList: " + nameString);
			}
			sqle.printStackTrace();
			throw new HBException("HBLibraryResultSet - updateElementList: " + sqle.getMessage());

		}
	}


/**
 *
 * @param pointResultSet
 * @param userData
 * @throws HBException
 * T162_NAME_STYLE_OUTPUT
�		PID
�		CL_COMMIT_RPID
	�	IS_SYSTEM
	�	IS_TMG
�	  	OWNER_RECORD_RPID
�		OUT_TYPE
�		NAME_TYPE
�		OUT_NAME_STYLE
�		OUT_NAME_STYLE_DESC
	�	OUT_ELEMNT_CODES
 */

	public void updateOutputStyleTable(ResultSet pointResultSet, long ownerPID,  String [] userData) throws HBException {
		try {
			// Update new row in ResultSet
			pointResultSet.updateLong("OWNER_RECORD_RPID", ownerPID);
			pointResultSet.updateString("OUT_TYPE", userData[0]);
			pointResultSet.updateString("OUT_NAME_STYLE", userData[1]);
			pointResultSet.updateString("OUT_NAME_STYLE_DESC", userData[2]);
			pointResultSet.updateString("OUT_ELEMNT_CODES", userData[3]);
			pointResultSet.updateRow();
			pointResultSet.beforeFirst();
		} catch (SQLException sqle) {
			if (HGlobal.DEBUG) {
				System.out.println("Not able to update for: " + userData[0]);
			}
			sqle.printStackTrace();
			throw new HBException("HBLibraryResultSet - updateNameStyleTable: " + sqle.getMessage());
		}
	}

/**
 * deleteUserTable	removes last entry in user table T160/165
 * @param pointResultSet
 * @throws HBException
 */

	public void deleteNameStyleRow(ResultSet pointResultSet, int selectIndex) throws HBException {
		try {
			if (HGlobal.DEBUG) {
				System.out.println("ResultSet - Delete: " + selectIndex + " in Table T160/T162");
			}
		//Absolute starts with row 1,2... and index starts with 0
			if (!pointResultSet.absolute(selectIndex + 1)) {
				throw new HBException("HBLibraryResultSet_v21c - delete Name Styel - absolute error: "
						+ (selectIndex + 1));
			}
			pointResultSet.deleteRow();
			pointResultSet.updateRow();

		} catch (SQLException sqle) {
			if (HGlobal.DEBUG) {
				System.out.println("Not able to delete row in T160/T165");
			}
			throw new HBException("HBLibraryResultSet - Delete user error: " + sqle.getMessage());
		}
	}

/**
 * Method to be modified according to name style implementation
 * NOT used in business layer!!!
 * selectPersNameElements(long namePID, int dataBaseIndex)
 * @param namePID
 * @param dataBaseIndex
 * @return String[] nameElements
 * @throws HBException
 */

	public String[] selectPersNameElements(long namePID,
								int dataBaseIndex)
								throws HBException {
		String[] nameElements = new String[16];
		String selectString;
		ResultSet nameElementTable;
		for (int i = 0; i < 16; i++) {
			nameElements[i] = "";
		}
		try {
			selectString = pointBusinessLayer.
				setSelectSQL("*", pointBusinessLayer.personNamesTableElements, "OWNER_RPID = " + namePID);
			nameElementTable = pointBusinessLayer.requestTableData(selectString, dataBaseIndex);
			nameElementTable.beforeFirst();
			while (nameElementTable.next()) {
				int nameElementType = (int) (nameElementTable.getLong("LIST_ENCODING_TYPE") - proOffset);
				nameElements[nameElementType] = nameElementTable.getString("NAME_DATA");
			}
			return nameElements;
		} catch (SQLException sqle) {
			System.out.println("Get Person name element DDLv21c error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException("Get Person name element data DDLv21c error: " + sqle.getMessage());
		}
	}

/**
 * selectPersonName(long namePID,
 * @param int dataBaseIndex,
 * @param String[] personStyle)
 * return person name
 */

	public String selectPersonName(long namePID,
									int dataBaseIndex,
									String[] personStyle) throws HBException {
		String personName = "";
		String selectString;
		ResultSet nameElementTable;
		try {
			selectString = pointBusinessLayer.
				setSelectSQL("*", pointBusinessLayer.personNamesTableElements,"OWNER_RPID = " + namePID);
			nameElementTable = pointBusinessLayer.requestTableData(selectString, dataBaseIndex);
			boolean first = true;
			for (int i = 0; i < personStyle.length; i++) {
				String personNameCode = personStyle[i].trim();
				String[] commaData = null;
				String commaChar = " ";

		// if comma after name element data is marked in name element code
				if (personNameCode.length() > 4) {
					commaData = personNameCode.split("#");
					commaChar = commaData[1] + " ";
					personNameCode = commaData[0];
				}

		// Select the name element data corresponding the name element code
				nameElementTable.beforeFirst();
				while (nameElementTable.next()) {
					String elementCode = nameElementTable.getString("ELEMNT_CODE").trim();
					String elementName = nameElementTable.getString("NAME_DATA").trim();
					if (personNameCode.trim().equals(elementCode)) {
						if (first) {
							personName = elementName + commaChar;
							first = false;
						//} else if (nameElementTable.isLast())
						} else if (i == personStyle.length) {
							personName = personName  + elementName; // ***** Note
						} else {
							personName = personName  + elementName + commaChar;
						}
					}
				}
			}
			return personName;
		} catch (SQLException sqle) {
			System.out.println("Get Person name element DDLv21c error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException("Get Person name element data DDLv21c error: " + sqle.getMessage());
		}
	}


/**
 * selecLocationNameElements(long ownerRPID, int dataBaseIndex)
 * @param ownerRPID
 * @param dataBaseIndex
 * @return String[][]
 * @throws HBException
 */


	public String[] selectLocationNameElements(long ownerRPID, String[] elemntCodes, int dataBaseIndex) throws HBException {
		String selectString;
		ResultSet placeElementTable;
		String[] placeElements = new String[elemntCodes.length];

		// Initiate location data array
		for (int i = 0; i < placeElements.length; i++) {
			placeElements[i] = "";
		}
		try {
			selectString = pointBusinessLayer.
				setSelectSQL("*", pointBusinessLayer.locationNameElementTable, "OWNER_RPID = " + ownerRPID);
			placeElementTable = pointBusinessLayer.requestTableData(selectString, dataBaseIndex);

		// Check if ResultSet has no rows and return
			placeElementTable.last();
			int row = placeElementTable.getRow();

			if (row == 0) {
				placeElements[0] = "----";
				if (HGlobal.DEBUG) {
					System.out.println("Rows == 0");
				}
				return placeElements;
			}

		// If ResultSet has rows - collect place data from ResultSet

			for (int i = 0; i < placeElements.length; i++) {
				placeElementTable.beforeFirst();
				while (placeElementTable.next()) {
					if (placeElementTable.getString("ELEMNT_CODE").trim().equals(elemntCodes[i].trim())) {
						placeElements[i] = placeElementTable.getString("NAME_DATA");
						break;
					}
				}
			}
			return placeElements;
		} catch (SQLException sqle) {
			System.out.println("Get Place data DDLv21c error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException("Get Place data DDLv21c error: " + sqle.getMessage());
		}
	}

/**
 * public selectLocationNameElements(long ownerRPID, int dataBaseIndex)
 * @param ownerRPID
 * @param dataBaseIndex
 * @return
 * @throws HBException
 */

	public HashMap<String,String> selectLocationNameElements(long ownerRPID, int dataBaseIndex) throws HBException {
		String selectString;
		ResultSet placeElementTable;
		HashMap<String,String> elementCodemap = new HashMap<>();
		try {
			selectString = pointBusinessLayer.
				setSelectSQL("*", pointBusinessLayer.locationNameElementTable, "OWNER_RPID = " + ownerRPID);
			placeElementTable = pointBusinessLayer.requestTableData(selectString, dataBaseIndex);

		// Check if ResultSet has no rows and return
			placeElementTable.last();
			if (placeElementTable.getRow() == 0) {
				if (HGlobal.DEBUG) {
					System.out.println("Rows == 0");
				}
				return elementCodemap;
			}

		// If ResultSet has rows - collect place data from ResultSet
			placeElementTable.beforeFirst();
			while (placeElementTable.next()) {
				String elementCode = placeElementTable.getString("ELEMNT_CODE").trim();
				String elementDescription = placeElementTable.getString("NAME_DATA").trim();
				elementCodemap.put(elementCode, elementDescription);
			}

			return elementCodemap;
		} catch (SQLException sqle) {
			System.out.println("Get Place data DDLv21c error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException("Get Place data DDLv21c error: " + sqle.getMessage());
		}
	}

/**
 * selectLocationName(long ownerRPID, String[] locationNameStyle, int dataBaseIndex)
 * @param ownerRPID
 * @param String[] locationNameStyle
 * @param dataBaseIndex
 * @return String
 * @throws HBException
 */

	public String selectLocationName(long ownerRPID, String[] locationNameStyle, int dataBaseIndex) throws HBException {
		String locationName = "";
		String selectString;
		ResultSet placeElementTable;

		try {
			selectString = pointBusinessLayer.
				setSelectSQL("*", pointBusinessLayer.locationNameElementTable,"OWNER_RPID = " + ownerRPID);
			placeElementTable = pointBusinessLayer.requestTableData(selectString, dataBaseIndex);
		// Check if ResultSet has no rows and return
			placeElementTable.last();
			int row = placeElementTable.getRow();

		// If ResultSet has rows - collect place data from ResultSet
			if (HGlobal.DEBUG) {
				if (row == 0) {
					System.out.println("Rows == 0");
				}
			}

			boolean first = true;
			for (String element : locationNameStyle) {
				String personNameCode = element.trim();
				String[] commaData = null;
				String commaChar = " ";
				if (row == 0) {
					break;
				}

			// if comma after name element data is marked in name element code
				if (personNameCode.length() > 4) {
					commaData = personNameCode.split("#");
					commaChar = commaData[1] + " ";
					personNameCode = commaData[0];
				}
				placeElementTable.beforeFirst();
				while (placeElementTable.next()) {
					String elementCode = placeElementTable.getString("ELEMNT_CODE").trim();
					String elementName = placeElementTable.getString("NAME_DATA").trim();
					if (personNameCode.trim().equals(elementCode)) {
						if (first) {
							locationName = " " + elementName + commaChar;
							first = false;
						} else if (placeElementTable.isLast()) {
							locationName = locationName  + elementName;
						} else {
							locationName = locationName  + elementName + commaChar;
						}
					}
				}
			}
			return locationName;
		} catch (SQLException sqle) {
			System.out.println("Get Place data DDLv21c error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException("Get Place data DDLv21c error: " + sqle.getMessage());
		}
	}


/**
 * Get database version from Table T10X_SCHEMA_DEFNS
 * @param dbIndex
 * @return
 */

	public String getDatabaseVersion(ResultSet pointT10X_SCHEMA_DEFNS, int dataBaseIndex) throws HBException {

		if (HGlobal.DEBUG) {
			System.out.println("getDatabaseVersion - dbindex: " + dataBaseIndex);
		}
        try {

			pointT10X_SCHEMA_DEFNS.first();
			if (HGlobal.DEBUG) {
				System.out.println("Found databaseVersion - VERSION_NAME: " +
							pointT10X_SCHEMA_DEFNS.getNString("VERSION_NAME"));
			}
			return pointT10X_SCHEMA_DEFNS.getNString("VERSION_NAME");

		} catch (SQLException sqle) {
			if (HGlobal.DEBUG) {
				System.out.println("getDatabaseVersion - SQLException: " + sqle.getMessage());
			}
			throw new HBException ("getDatabaseVersion - SQLException: " + sqle.getMessage());
		}
	}

/**
 * Get project name in table T126_PROJECTS from HRE database
 * Only written for one single project in a HRE Database
 * @param newProjectName - new project name in HRE database
 * @param dataBaseLoc - Path to the HRE database in the file system
 * @return  true for success
 * @throws HBException
 */

	public String getDatabaseProjectName(String dataBaseLoc) throws HBException {
		int dataBaseIndex = -1;
		String selectSQL;
		String projectName;
		String databaseEngine = pointBusinessLayer.getDefaultDatabaseEngine();
		ResultSet projectTable;
		try {
			// Open database
			String[] logonData = {databaseEngine,"SA",""};
			dataBaseIndex = pointBusinessLayer.pointDBlayer.connectSQLdatabase(dataBaseLoc, logonData);

		// DATA from T126_PROJECTS
			selectSQL = pointBusinessLayer.setSelectSQL("*","T126_PROJECTS","");
			projectTable =  pointBusinessLayer.requestTableData(selectSQL, dataBaseIndex );
			projectTable.first();
			if (HGlobal.DEBUG) {
				System.out.println("SQL Project Name: " + projectTable.getNString("PROJECT_NAME"));
			}

			projectName = projectTable.getNString("PROJECT_NAME");
			projectTable.updateRow();
			projectTable.close();

			//Close database
			pointBusinessLayer.closeDatabase(dataBaseIndex);
			return projectName;

		} catch(HDException hde) {
			if (HGlobal.DEBUG) {
				System.out.println("setDatabaseProjectName - HDException: \n"
						+ hde.getMessage());
			}

			projectTable= null;
			pointBusinessLayer.closeDatabase(dataBaseIndex);
			throw new HBException("getDatabaseProjectName - HDException: \n" + hde.getMessage());
		} catch (SQLException sqle) {
			if (HGlobal.DEBUG) {
				System.out.println("getDatabaseProjectName - SQLException: \n"
						+ sqle.getMessage());
			}

			projectTable= null;
			pointBusinessLayer.closeDatabase(dataBaseIndex);
			throw new HBException("setDatabaseProjectName - SQLException: \n" + sqle.getMessage());
		}
	}

/**
 * Change project name in table T126_PROJECTS from HRE database
 * Only written for one single project in a HRE Database
 * @param newProjectName - new project name in HRE database
 * @param dataBaseLoc - Path to the HRE database in the file system
 * @return  true for success
 * @throws HBException
 */

	public boolean setDatabaseProjectName(String newProjectName, String dataBaseLoc) throws HBException {
		int dataBaseIndex = -1;
		String selectSQL;
		String databaseEngine = pointBusinessLayer.getDefaultDatabaseEngine();
		ResultSet projectTable;
		try {
		// Open database
			String[] logonData = {databaseEngine,"SA",""};
			dataBaseIndex = pointBusinessLayer.pointDBlayer.connectSQLdatabase(dataBaseLoc, logonData);

		// DATA from T126_PROJECTS
			selectSQL = pointBusinessLayer.setSelectSQL("*","T126_PROJECTS","");
			projectTable =  pointBusinessLayer.requestTableData(selectSQL, dataBaseIndex );
			projectTable.first();
			if (HGlobal.DEBUG) {
				System.out.println("SQL Project Name: " + projectTable.getNString("PROJECT_NAME"));
			}
			projectTable.updateLong("PID",proOffset + 1);
			projectTable.updateLong("CL_COMMIT_RPID",null_RPID);
			projectTable.updateTime("COMMENCED",new Time(1000000L));
			projectTable.updateInt("PROJECT_CODE",1);
			projectTable.updateLong("PROJECT_OFFSET",proOffset);
			projectTable.updateString("PROJECT_NAME", newProjectName);
			projectTable.updateRow();
			projectTable.close();

			//Close database
			pointBusinessLayer.closeDatabase(dataBaseIndex);
			return true;

		} catch(HDException hde) {
			if (HGlobal.DEBUG) {
				System.out.println("setDatabaseProjectName - HDException: \n"
						+ hde.getMessage());
			}

			projectTable= null;
			pointBusinessLayer.closeDatabase(dataBaseIndex);
			throw new HBException("setDatabaseProjectName - HDException: \n" + hde.getMessage());
		} catch (SQLException sqle) {
			if (HGlobal.DEBUG) {
				System.out.println("setDatabaseProjectName - SQLException: \n"
						+ sqle.getMessage());
			}

			projectTable= null;
			pointBusinessLayer.closeDatabase(dataBaseIndex);
			throw new HBException("setDatabaseProjectName - SQLException: \n" + sqle.getMessage());
		}
	}


/**
 * exstractPersonName(long personPID)
 * @param personPID
 * @return
 * @throws HBException
 * @throws SQLException
 */

	protected String exstractPersonName(long personPID, String[] personStyle, int dataBaseIndex)  throws HBException  {
		String selectSQL = null, personName = "";
		long personNamePID;
		ResultSet nameSelected;
		int visibleID = 0;
		try {

		// PER1 can be 0 in TMG event table G.dbf
			if (personPID != proOffset) {
				if (personPID != null_RPID) {
					selectSQL = pointBusinessLayer.setSelectSQL("*", pointBusinessLayer.personTable,"PID = " + personPID);
					nameSelected = pointBusinessLayer.requestTableData(selectSQL, dataBaseIndex);
					nameSelected.last();
					if (nameSelected.getRow() == 0) return " --- ";
					
					nameSelected.first();
					personNamePID = nameSelected.getLong("BEST_NAME_RPID");
					visibleID = nameSelected.getInt("VISIBLE_ID");
					selectSQL = pointBusinessLayer.setSelectSQL("*", pointBusinessLayer.personNameTable, "PID = " + personNamePID);
					nameSelected = pointBusinessLayer.requestTableData(selectSQL, dataBaseIndex);
					nameSelected.first();
					personName = selectPersonName(personNamePID, dataBaseIndex, personStyle);
/**
 *  WARNING Name element not always == 1 - must be selected
 */
					personName = personName + " ("+ visibleID + ")";
				} else personName  = " No name pointer ";		
			} else personName  = " No name found";
			return " " + personName;
		} catch (SQLException sqle) {
			throw new HBException("SQL exception: " + sqle.getMessage() + "\nPersonPID: " + personPID);
		}
	}

/**
 * String exstractSortString(long hdatePID, int dataBaseIndex) throws HBException
 * @param hdatePID
 * @param dataBaseIndex
 * @return
 * @throws HBException
 */

	public String exstractSortString(long hdatePID, int dataBaseIndex) throws HBException {
		String selectString = null, sortString = "";
		ResultSet hdateSelected;
		if (hdatePID == null_RPID) {
			return sortString;
		} else {
			try {
				selectString = pointBusinessLayer.setSelectSQL("*", pointBusinessLayer.dateTable,"PID = " + hdatePID);
				hdateSelected = pointBusinessLayer.requestTableData(selectString, dataBaseIndex);
				if (pointBusinessLayer.isResultSetEmpty(hdateSelected)) {
					System.out.println( " Return sort string (exstractSortString) error for HDate PID: " + hdatePID);
					return sortString;
				}
				hdateSelected.first();
				return hdateSelected.getString("SORT_HDATE_CODE");
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw new HBException("SQL exception: " + sqle.getMessage() + "\nSQL string: " + selectString);
			}
		}
	}

/**
 * Exstract date from HDATA in T170
 * @param hdatePID
 * @return
 * @throws HBException
 */

	public String exstractDate(long hdatePID, int dataBaseIndex) throws HBException {
		String dateString = "", selectString = null;
		ResultSet hdateSelected;
		if (hdatePID == null_RPID) {
			return dateString;
		} else {
			try {
			selectString = pointBusinessLayer.setSelectSQL("*", pointBusinessLayer.dateTable, "PID = " + hdatePID);
			hdateSelected = pointBusinessLayer.requestTableData(selectString, dataBaseIndex);
			if (pointBusinessLayer.isResultSetEmpty(hdateSelected )) {
				System.out.println( " Not found Hdate (exstractDate) error for HDate PID: " + hdatePID);
				return dateString;
			}
			hdateSelected.first();
			dateString = pointBusinessLayer.formatDateSelector(hdateSelected.getLong("MAIN_HDATE_YEARS"),
					hdateSelected.getString("MAIN_HDATE_DETAILS"),
					hdateSelected.getLong("HDATE_YEARS"),
					hdateSelected.getString("HDATE_DETAILS"));
				return dateString;
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw new HBException("SQL exception: " + sqle.getMessage() + "\nSQL string: " + selectString);
			}
		}

	}

/**
 * dateIputHdate(long hdatePID, int dataBaseIndex)
 * @param hdatePID
 * @param dataBaseIndex
 * @return
 * @throws HBException
 */
	public Object[] dateIputHdate(long hdatePID, int dataBaseIndex) throws HBException {
		String selectString = null;
		Object[] dateHREDate = new Object[5];
		ResultSet hdateSelected;
		if (hdatePID == null_RPID) {
			return null;
		} else {
			try {
				selectString = pointBusinessLayer.setSelectSQL("*", pointBusinessLayer.dateTable, "PID = " + hdatePID);
				hdateSelected = pointBusinessLayer.requestTableData(selectString, dataBaseIndex);
				if (pointBusinessLayer.isResultSetEmpty(hdateSelected )) {
					System.out.println( " Convert to date (exstractDate) error for HDate PID: " + hdatePID);
					return null;
				}
				hdateSelected.first();
				dateHREDate[0] = hdateSelected.getLong("MAIN_HDATE_YEARS");
				dateHREDate[1] = hdateSelected.getString("MAIN_HDATE_DETAILS");
				dateHREDate[2] = hdateSelected.getLong("HDATE_YEARS");
				dateHREDate[3] = hdateSelected.getString("HDATE_DETAILS");
				dateHREDate[4] = hdateSelected.getString("SORT_HDATE_CODE");
				return dateHREDate;
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw new HBException("SQL exception: " + sqle.getMessage() + "\nSQL string: " + selectString);
			}
		}

	}


/**
 * updateNewUserInProject (String dataBasePath, String[] userData)
 * @param dataBaseLoc
 * @throws HDException
 * @throws HBException
 */

	public void updateNewUserInProject (String dataBasePath, String[] userData) throws HBException {
		int dataBaseIndex = -1;
		String dataBaseEngine = pointBusinessLayer.getDefaultDatabaseEngine();

		try {
		//  SELECT action to SQL base
			String[] logonData = {dataBaseEngine,"SA",""};
			dataBaseIndex = pointBusinessLayer.pointDBlayer.connectSQLdatabase(dataBasePath, logonData);
	        String selectSQL = pointBusinessLayer.setSelectSQL("*","T131_USER","");
	        ResultSet pointT131usersResultSet = pointBusinessLayer.requestTableData(selectSQL,dataBaseIndex);

			// Position to first row in ResultSet
			pointT131usersResultSet.absolute(1);
			if (HGlobal.DEBUG)
			 {
				System.out.println("updateUserInTable user creds: "
					+ userData[0]	+ "/"
							+ userData[1]	+ "/"
									+ userData[2]	+ "/"
											+ userData[3]);
		// Create database user
			//((HBToolHandler)pointBusinessLayer).
			//		createNewDatabaseUser(userData[0], userData[2], dataBaseIndex);
			}

		// Create entry in T131
			updateUserInTable(pointT131usersResultSet, userData);

		//Close database
			pointBusinessLayer.closeDatabase(dataBaseIndex);
		} catch (HDException hde) {
			throw new HBException("updateUserInTable error: " + hde.getMessage());

		} catch (SQLException sqle) {
			throw new HBException("updateUserInTable error: " + sqle.getMessage());
		}
	}

/**
 *
 * @param tableIdent
 * @param langCode
 * @param dataBaseIndex
 * @return
 * @throws HBException
 */

	protected String[] getTranslatedData(Boolean abbrev, String screenID, String tableIdent, String langCode, int dataBaseIndex) throws HBException {
		String langCodeParam = "'" + langCode + "'"; // Setting up
		String tableIdentParam = "'" + tableIdent + "'";
		String screenIdParam = "'" + screenID + "'";
		String data;
		String selectSQL = pointBusinessLayer.setSelectSQL("*", pointBusinessLayer.translatedData,
				"GUI_ID = " + screenIdParam +
				" AND TABL_ID = " + tableIdentParam +
				" AND LANG_CODE = " + langCodeParam);
		try {
			ResultSet translatedData = pointBusinessLayer.requestTableData(selectSQL, dataBaseIndex);
			translatedData.first();
			if (abbrev) {
				data = translatedData.getString("ABBR");
			} else {
				data = translatedData.getString("DATA");
			}
			return data.split("\\|");
		} catch (HBException | SQLException hbe) {
			if (HGlobal.DEBUG) {
				System.out.println(" HBLibraryResultSet_v22a - getTranslatedData error: " + hbe.getMessage());
			}
			throw new HBException(" HBLibraryResultSet_v22a  - getTranslatedData error: " + hbe.getMessage());
		}
	}


/**
 * Read ResultSet and return data for project IDX field  PROJECT  - test purpose
 * @param IDX String IDX Project number
 * @param resSet - ResultSet to search
 * @return String
 * @throws HBException
 */
    public String readH2project(String IDX, ResultSet resSet) throws HBException {
		String ID = "";
		String surName, givName, projectLoc = "";
		try {
			 resSet.beforeFirst(); // Reset to first index
	    	while (resSet.next()) {
	    		ID = resSet.getString("ID");
	    		if (IDX.equals(ID)) {
	    			projectLoc = resSet.getString("PROJECT");
	        		surName = resSet.getString("SUR_NAME");
	        		givName = resSet.getString("GIV_NAME");
	        		projectLoc = resSet.getString("PROJECT");
	        		if (HGlobal.DEBUG) {
						System.out.println(ID + " - "
								+ surName + " "
								+ givName + " /  "
								+ projectLoc );
					}
	    		}
	    	}

	    	if (HGlobal.DEBUG) {
				System.out.println("readH2project: " + ID + " Loc: " + projectLoc);
			}
	    	return projectLoc;
	   	} catch(SQLException exc) {
	   		throw new HBException("readH2project - SQL error: \n" + exc.getMessage());
	   	}
    }

/**
 * Search in ResultSet for password from table user data
 * @param userIDname user ID for search
 * @param resSet ResultSet to be searched
 * @return passWord for user ID
 * @throws HBException
 */

	public String findPassword(String userIDname, ResultSet resSet) throws HBException {
		   if (HGlobal.DEBUG) {
			System.out.println("Find password: ");
		}
		   if (resSet == null) {
			throw new HBException("User table T131 not available\n");
		}
		   	String userName = "";
		   	String passWord = "";
		   	try {
			   	resSet.beforeFirst();
			   	while (resSet.next()) {
			   		userName = resSet.getString("LOGON_NAME");
			   		if (userName.equals(userIDname)) {
			   			passWord = resSet.getString("PASSWORD");
			   			if (HGlobal.DEBUG) {
							System.out.println("User: " + userName + "\n" + "Pass: " + passWord);
						}
			   		}
			   	}
			   	return passWord;
		   	} catch(SQLException exc) {
		   		throw new HBException("Find password - SQL error: \n" + exc.getMessage());
		   	}
	   }


/**
 * BuildTableModel convert ResultSet to DefaultTableModel
 * @param resSet ResultSet data to be converted
 * @return DefaultTableModel instance
 * @throws HBException
 */


	public DefaultTableModel buildTableModel(ResultSet resSet) throws HBException {

	    try {
			ResultSetMetaData metaData = resSet.getMetaData();

		    // names of columns
		    Vector<String> columnNames = new Vector<>();
		    int columnCount = metaData.getColumnCount();
		    for (int column = 1; column <= columnCount; column++) {
		        columnNames.add(metaData.getColumnName(column));
		    }
		    resSet.beforeFirst();
		    // data of the table
		    Vector<Vector<Object>> data = new Vector<>();
		    while (resSet.next()) {
		        Vector<Object> vector = new Vector<>();
		        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
		            vector.add(resSet.getObject(columnIndex));
		        }
		        data.add(vector);
		    }

		    return new DefaultTableModel(data, columnNames);
	   	} catch(SQLException exc) {
	   		throw new HBException("buildTableModel - SQL error: \n" + exc.getMessage());
	   	}

	}

/**
 * Update T131_USERS with user name etc
 * @param pointResultSet
 * @throws HBException
 */

	protected void updateUserInTable(ResultSet pointResultSet, String [] userData) throws HBException {
		String logonID = "", userName, passWord;
		try {

			if (HGlobal.DEBUG) {
				System.out.println("ResultSet - updateUserInTable Table T131");
			}

		// NOTE 03 - Position in ResultSet is set in calling method: ex. sqlTable.absolute(1);
			logonID = pointResultSet.getNString("LOGON_NAME");
			userName = pointResultSet.getNString("USER_NAME");
			passWord = pointResultSet.getNString("PASSWORD");
			if (HGlobal.DEBUG) {
				System.out.println("ResultSet - content UserInTable - row: "
						+ pointResultSet.getRow() + " - " + logonID + "/" + userName);
			}

			pointResultSet.updateString("LOGON_NAME", userData[0]);
			pointResultSet.updateString("USER_NAME", userData[1]);

		// Update user password if set
			if (userData[2] != null) {
				pointResultSet.updateString("PASSWORD", userData[2]);
			} else {
				pointResultSet.updateString("PASSWORD", passWord);
			}

	    // PASSWORD kept and not updated
			pointResultSet.updateString("EMAIL", userData[3]);
			pointResultSet.updateRow();

		} catch (SQLException sqle) {
			if (HGlobal.DEBUG) {
				System.out.println("Not able to update T131 for: " + logonID);
			}
			throw new HBException("HBLibraryResultSet updateUserInTable error: " + sqle.getMessage());
		}
	}

/**
 * Add new user to  T131_USERS with user name etc
 * @param pointResultSet
 * @param userData
 * @throws HBException
*/

	public void addUserToTable(ResultSet pointResultSet, String [] userData) throws HBException {

		long startPID  = 1000000000000001L;
		long valuePID;
		try {
			if (HGlobal.DEBUG) {
				System.out.println("ResultSet - addUserToTable: " + "Table T131");
			}

		// Increment PID for next row i table or start with empty database
			pointResultSet.last();
			if (HGlobal.DEBUG) {
				System.out.println("Last row in ResultSet: " + pointResultSet.getRow());
			}
			if (pointResultSet.getRow() == 0) {
				valuePID = startPID;
			} else {
				valuePID = pointResultSet.getLong("PID");
				valuePID++;
			}

			pointResultSet.moveToInsertRow(); // moves cursor to the insert row

		// Update new row in ResultSet
		    pointResultSet.updateLong("PID", valuePID);
			pointResultSet.updateLong("CL_COMMIT_RPID", null_RPID);
			pointResultSet.updateString("LOGON_NAME", userData[0]);
			pointResultSet.updateString("USER_NAME", userData[1]);
			pointResultSet.updateString("PASSWORD", userData[2]);
			pointResultSet.updateString("EMAIL", userData[3]);
			pointResultSet.updateLong("USER_GROUP_RPID",null_RPID);
			pointResultSet.insertRow();
			pointResultSet.beforeFirst();

		} catch (SQLException sqle) {
			if (HGlobal.DEBUG) {
				System.out.println("Not able to update T131 for: " + userData[0]);
			}
			sqle.printStackTrace();
			throw new HBException("HBLibraryResultSet add user error: " + sqle.getMessage());
		}
	}

/**
 * deleteUserTable	removes last entry in user table T131
 * @param pointResultSet
 * @throws HBException
 */

	public void deleteUserInTable(ResultSet pointResultSet, int selectIndex) throws HBException {
		try {
			if (HGlobal.DEBUG) {
				System.out.println("ResultSet - Mod: " + "Table T131");
			}
		//Absolute starts with row 1,2... and index starts with 0
			if (!pointResultSet.absolute(selectIndex + 1)) {
				throw new HBException("HBLibraryResultSet_v21a - deleteUserInTable - absolute error: "
						+ (selectIndex + 1));
			}
/**
 * Result Set operation need to reorganize PID values
 * and update of T104 with last PID for table
 */
			pointResultSet.deleteRow();
			pointResultSet.updateRow();

		} catch (SQLException sqle) {
			if (HGlobal.DEBUG) {
				System.out.println("Not able to delete row in T131");
			}
			throw new HBException("HBLibraryResultSet - Delete user error: " + sqle.getMessage());
		}
	}

/**
 * Calculate Age at event
 * @param eventHDatePID
 * @param selectPersonPID
 * @return String age
 * @throws HBException
 */

	public String calculateAge(long eventHDatePID,long selectPersonPID, int dataBaseIndex) throws HBException {
		ResultSet dataSelected;
		long eventYear = 0, birthYear;
		String detailEvent = null, detailBirth, selectString = null;
		int[] eventDateDec = null, birthDateDec = null;
		long birhHDatePID;
		int age = 0;
		char qualifier;
		char ca = ' ';
		try {

	// Find event year
			if (eventHDatePID != null_RPID ) {
				selectString = pointBusinessLayer.setSelectSQL("*", pointBusinessLayer.dateTable, "PID = " + eventHDatePID);
				dataSelected = pointBusinessLayer.requestTableData(selectString, dataBaseIndex);
				if (pointBusinessLayer.isResultSetEmpty(dataSelected)) {
					System.out.println( " Return date string (calculateAge) error for HDate PID: " +  eventHDatePID);
					return "";
				}
				dataSelected.first();
				eventYear = dataSelected.getLong("MAIN_HDATE_YEARS");
				detailEvent = dataSelected.getString("MAIN_HDATE_DETAILS");
				qualifier = detailEvent.charAt(16);
				if (qualifier != 'X') {
					ca = '~';
				}
				eventDateDec = pointBusinessLayer.numericalDate(eventYear, detailEvent);
			} else {
				return "";
			}

	// Find birth year
			if (selectPersonPID != null_RPID ) {
				selectString = pointBusinessLayer.setSelectSQL("*", pointBusinessLayer.personTable, "PID = " + selectPersonPID);
				dataSelected = pointBusinessLayer.requestTableData(selectString, dataBaseIndex);
				dataSelected.first();
				birhHDatePID = dataSelected.getLong(pointBusinessLayer.personHDateBirthField);
				if (birhHDatePID != null_RPID ) {
					selectString = pointBusinessLayer.setSelectSQL("*", pointBusinessLayer.dateTable, "PID = " + birhHDatePID);
					dataSelected = pointBusinessLayer.requestTableData(selectString, dataBaseIndex);
					if (pointBusinessLayer.isResultSetEmpty(dataSelected)) {
						System.out.println( " Return date string (calculateAge) error for HDate PID: " +  eventHDatePID);
						return "";
					}
					dataSelected.first();
					birthYear = dataSelected.getLong("MAIN_HDATE_YEARS");
					detailBirth = dataSelected.getString("MAIN_HDATE_DETAILS");
					qualifier = detailBirth.charAt(16);
					if (qualifier != 'X') {
						ca = '~';
					}
					birthDateDec = pointBusinessLayer.numericalDate(birthYear, detailBirth);
				} else {
					return "";
				}
			} else {
				return "";
			}

	 // Compare month and day
			age = eventDateDec[0] - birthDateDec[0];
			if (age != 0) {
				if (eventDateDec[1] < birthDateDec[1]) {
					age--;
				}
				if (eventDateDec[1] == birthDateDec[1]) {
					if (eventDateDec[2] < birthDateDec[2]) {
						age--;
					}
				}
			} else {
				ca = ' ';
			}
			return "" + ca + age;
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException("SQL exception: " + sqle.getMessage() + "\nSQL string: " + selectString);
		} catch (NumberFormatException nfe) {
			if (HGlobal.DEBUG) {
				System.out.println(" In Library ResultSer calculateAge: \nNumberformatException date value: "
									+ eventYear + "/" + detailEvent.trim()
									+ " - Event Hdate PID =  " + eventHDatePID
									+ " Person PID = " + selectPersonPID);
			}
			return "";
		}
	}


/**
 * indexingPersonMapPID(ResultSet pointT401) set up HashMap for  personPID
 * @param pointT401
 * @return
 * @throws HBException
 * @throws SQLException
 */

	public HashMap<Integer,Long> indexingPersonMapPID(ResultSet pointT401) throws HBException  {
		long PID = 0;
		int visibleId = 0;
		HashMap<Integer,Long> hashCodePersonPID = new HashMap<>();
		try {
			if (HGlobal.DEBUG) {
				pointT401.last();
				int rows = pointT401.getRow();
				System.out.println("Name lement rows: " + rows);
			}
			pointT401.beforeFirst();
			while (pointT401.next() ) {
				PID = pointT401.getLong("PID");
				visibleId  = pointT401.getInt("VISIBLE_ID");
				hashCodePersonPID.put(visibleId , PID);
			}
		} catch (SQLException sqle) {
			throw new HBException("Error indexingPersonMapPID " + sqle.getMessage());
			//sqle.printStackTrace();
		}
		return hashCodePersonPID;
	}


/**
 *
 * @param eventNumber
 * @param selectRoles example " AND EVNT_ROLE_NUM IN (1,2,3,10)")
 * @param dataBaseIndex
 * @return
 * @throws HBException
 */
	public ResultSet getRoleNameList(int eventNumber, String selectRoles, int dataBaseIndex) throws HBException {
		String langCode = HGlobal.dataLanguage;
		return getRoleNameList(eventNumber, selectRoles, langCode, dataBaseIndex);

	}
	public ResultSet getRoleNameList(int eventNumber, String selectRoles, String langCode, int dataBaseIndex) throws HBException {
		ResultSet roleNameList;
		//String langCode = HGlobal.dataLanguage;
		String selectString = 	pointBusinessLayer.setSelectSQL("*",
				pointBusinessLayer.eventRoleTable,
				"EVNT_TYPE = " + eventNumber + " AND LANG_CODE = '" + langCode + "'" + selectRoles);
		roleNameList = pointBusinessLayer.requestTableData(selectString, dataBaseIndex);
		return roleNameList;
	}

/**
 * getParentRoleList(int eventGroup, int dataBaseIndex)
 * @param eventGroup
 * @param dataBaseIndex
 * @return
 * @throws HBException
 */
	public ResultSet getEventTypeList( int eventGroup, int dataBaseIndex) throws HBException {
		ResultSet eventTypeList;
		String langCode = HGlobal.dataLanguage;
		String selectString = null;
	// Select events in eventGroup #
		if (eventGroup > 0) {
			selectString = 	pointBusinessLayer.setSelectSQL("*",
				pointBusinessLayer.eventDefnTable,
				"EVNT_GROUP = " + eventGroup + " AND LANG_CODE = '" + langCode + "'");
		} else if (eventGroup == 0) {
			selectString = pointBusinessLayer.setSelectSQL("*",
				pointBusinessLayer.eventDefnTable,
				"EVNT_GROUP NOT IN(1,2,3,12) AND LANG_CODE = '" + langCode + "'");
		} else if (eventGroup == -1) {
			selectString = pointBusinessLayer.setSelectSQL("*",
				pointBusinessLayer.eventDefnTable,
				"EVNT_GROUP IN(2,3) AND LANG_CODE = '" + langCode + "'");
		} else if (eventGroup == -2) {
			selectString = pointBusinessLayer.setSelectSQL("*",
				pointBusinessLayer.eventDefnTable,
				"EVNT_GROUP IN(6,7) AND LANG_CODE = '" + langCode + "'");
		} else {
			System.out.println(" getEventTypeList - event group error: " + eventGroup);
		}
		eventTypeList = pointBusinessLayer.requestTableData(selectString, dataBaseIndex);
		return eventTypeList;
	}

/**
 * public int getEventGroup(int eventType, int dataBaseIndex)
 * @param eventType
 * @param dataBaseIndex
 * @return
 * @throws HBException
 */
	public int getEventGroup(int eventType, int dataBaseIndex) throws HBException {
		ResultSet eventTypeList;
		String langCode = "en-US"; // en-US has entries for all eventtypes
		String selectString = null;
		selectString = 	pointBusinessLayer.setSelectSQL("*",
				pointBusinessLayer.eventDefnTable,
				"EVNT_TYPE = " + eventType + " AND LANG_CODE = '" + langCode + "'");
		eventTypeList = pointBusinessLayer.requestTableData(selectString, dataBaseIndex);
		try {
			eventTypeList.last();
			if (eventTypeList.getRow() == 0) return 99;	
			eventTypeList.first();
			return eventTypeList.getInt("EVNT_GROUP");
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" HBLibraryResultSet - getEventGroup error\n" + sqle.getMessage());
		}
	}

/**
 * getPersonBirthEventPID(long selectedPersonPID,  int dataBaseIndex)
 * @param selectedPersonPID
 * @param dataBaseIndex
 * @return
 * @throws HBException
 */
	public long getPersonBirthEventPID(long selectedPersonPID,  int dataBaseIndex) throws HBException {
		ResultSet eventRecord;
		String selectString = 	pointBusinessLayer.setSelectSQL("*",
				pointBusinessLayer.eventTable,
	// Select select birth event
				"PRIM_ASSOC_RPID = " + selectedPersonPID + " AND EVNT_TYPE = 1002");
		eventRecord = pointBusinessLayer.requestTableData(selectString, dataBaseIndex);
		try {
			if (pointBusinessLayer.isResultSetEmpty(eventRecord)) return null_RPID;
			eventRecord.first();
			return eventRecord.getLong("PID");
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" HBLibraryResultSet - getPersonBirthEventPID\n"
			+ " Missing birth event for selected person: \n" + sqle.getMessage());
		}
	}
/**
 * getRoleName
 * @param eventRoleCode
 * @param eventNumber
 * @param langCode
 * @param dataBaseIndex
 * @return
 * @throws HBException
 */

	public String getRoleName(int eventRoleCode,
			  int eventNumber,
			  String langCode,
			  int dataBaseIndex) throws HBException {
		String selectString = "";
		ResultSet eventRoleSet;
		try {
			selectString = pointBusinessLayer.setSelectSQL("EVNT_ROLE_NAME",
					pointBusinessLayer.eventRoleTable,
						"EVNT_TYPE = " + eventNumber + " AND LANG_CODE = '" + langCode + "'"
								+ " AND EVNT_ROLE_NUM = '" + eventRoleCode + "'" );
			eventRoleSet = pointBusinessLayer.requestTableData(selectString, dataBaseIndex);
			eventRoleSet.first();
			if (eventRoleSet.getRow() > 0 ) {
				return eventRoleSet.getString("EVNT_ROLE_NAME");
			} else {
				selectString = pointBusinessLayer.setSelectSQL("EVNT_ROLE_NAME",
						pointBusinessLayer.eventRoleTable,
						"EVNT_TYPE = " + eventNumber + " AND LANG_CODE = 'en-US'"
								+ " AND EVNT_ROLE_NUM  = '" + eventRoleCode + "'" );
				eventRoleSet = pointBusinessLayer.requestTableData(selectString, dataBaseIndex);
				eventRoleSet.last();
				if (eventRoleSet.getRow() == 0) {
					return " Not Found";
				}
				eventRoleSet.first();
				return eventRoleSet.getString("EVNT_ROLE_NAME");
			}
		} catch (SQLException | HBException sqle) {
			throw new HBException("SQL exception: " + sqle.getMessage()
						+ "\nSQL string: " + selectString);
		}
}

/**
 * getEventName(int eventNumber, String langCode, int dataBaseIndex)
 * @param eventNumber
 * @param langCode
 * @param dataBaseIndex
 * @return translated event name
 * @throws HBException
 */
	public String getEventName(int eventNumber, String langCode, int dataBaseIndex) throws HBException {
		ResultSet eventTagSet;
		String selectString = pointBusinessLayer.setSelectSQL("EVNT_NAME", pointBusinessLayer.eventDefnTable,
					"EVNT_TYPE = " + eventNumber + " AND LANG_CODE = '" + langCode + "'");
		eventTagSet = pointBusinessLayer.requestTableData(selectString, dataBaseIndex);
		try {
			eventTagSet.last();
			if (eventTagSet.getRow() > 0 ) {
				eventTagSet.first();
				return eventTagSet.getString("EVNT_NAME");
			} else {

	// fall back to en-US translation
				selectString = pointBusinessLayer.setSelectSQL("EVNT_NAME", pointBusinessLayer.eventDefnTable,
						"EVNT_TYPE = " + eventNumber + " AND LANG_CODE = 'en-US'");
				eventTagSet = pointBusinessLayer.requestTableData(selectString, dataBaseIndex);
				eventTagSet.last();
				if (eventTagSet.getRow() > 0 ) {
					eventTagSet.first();
					return "*" + eventTagSet.getString("EVNT_NAME");
				} else {
					System.out.println(" Missing fall back to en-US event nr: " + eventNumber + "/" + langCode);
					throw new HBException(" Missing fall back event name for en-US event nr: " + eventNumber + "/" + langCode);
				}
			}
		} catch (SQLException sqle) {
			throw new HBException("SQL exception: " + sqle.getMessage()
			+ "\nSQL string: " + selectString);
		}
	}

/**
 * getLanguageCodes(int dataBaseIndex)
 * @param dataBaseIndex
 * @return
 * @throws HBException
 */

	public String[] getLanguageCodes(String guiLang, int dataBaseIndex) throws HBException {
		String[] langCodes = new String[4];
		String selectString = pointBusinessLayer.
				setSelectSQL("*", pointBusinessLayer.languageUses,"LANG_CODE = '" + guiLang + "'");
		ResultSet langSettings = pointBusinessLayer.requestTableData(selectString, dataBaseIndex);
		try {
			langSettings.first();
			langCodes[0] = langSettings.getString("GUI_LANG_CODE");
			langCodes[1] = langSettings.getString("DATA1_LANG_CODE");
			langCodes[2] = langSettings.getString("DATA2_LANG_CODE");
			langCodes[3] = langSettings.getString("REPORT_LANG_CODE");
		} catch (SQLException sqle) {
			throw new HBException("getLanguageCodes - SQL error: " + sqle.getMessage());
		}
		return langCodes;
	}

/**
 * setLanguageCodes(String[] langCodes, int dataBaseIndex)
 * @param langCodes
 * @param dataBaseIndex
 * @throws HBException
 */

	public void setLanguageCodes(String guiLang, String[] langCodes, int dataBaseIndex) throws HBException {
		String selectString = pointBusinessLayer.
				setSelectSQL("*", pointBusinessLayer.languageUses,"LANG_CODE = '" + guiLang + "'");
		ResultSet langSettings = pointBusinessLayer.requestTableData(selectString, dataBaseIndex);
		try {
			langSettings.first();
			langSettings.updateString("GUI_LANG_CODE", langCodes[0]);
			langSettings.updateString("DATA1_LANG_CODE", langCodes[1]);
			langSettings.updateString("DATA2_LANG_CODE", langCodes[2]);
			langSettings.updateString("REPORT_LANG_CODE", langCodes[3]);
			langSettings.updateRow();

		} catch (SQLException sqle) {
			throw new HBException("setLanguageCodes - SQL error: " + sqle.getMessage());
		}
	}

/**
 * dump ResultSet data - write content of ResultSet to Java Console
 * @param ResSet input ResultSet
 * @throws HBException
 */

	public void dumpResultSetData(ResultSet resSet) throws HBException {
		try {
			System.out.println("ResultSet Metadata:");
			ResultSetMetaData rsmd = resSet.getMetaData();
			int columnCount = rsmd.getColumnCount();

			// The column count starts from 1
			System.out.println("Col Names: ");
			for (int i = 1; i <= columnCount; i++ ) {
				String name = rsmd.getColumnName(i);
				System.out.print(" / " + name);
			}
			System.out.println();
			System.out.println("Data content: ");
			resSet.beforeFirst();
	        while (resSet.next()) {
	            Object [] rowData = new Object[columnCount];
	            for (int i = 0; i < rowData.length; ++i) {
	                rowData[i] = resSet.getObject(i+1);
	                System.out.print(" / " + rowData[i]);
	            }
	            System.out.println();
	        }
		} catch(SQLException exc) {
			throw new HBException("SQL ResultSet Error: \n" + exc.getMessage());
		}
	}

/*
	public boolean setDatabaseProjectName(String dataBaseLoc) throws HBException {
		// TODO Auto-generated method stub
		return false;
	}


	public String[] getPlaceData(ResultSet eventTable) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}


	public HashMap<String, String[]> initiateT175data(ResultSet dataT175, ResultSet dataExstractT204) throws HBException {
		// TODO Auto-generated method stub
		return null;
	}


	public ResultSet getT204Resultset(String language, HBBusinessLayer pointBusinessLayer, int dataBaseIndex)
			throws HBException {
		// TODO Auto-generated method stub
		return null;
	}


	protected String getRoleName(String eventRoleCode, int eventNumber, String langCode, int dataBaseIndex)
			throws HBException {
		// TODO Auto-generated method stub
		return null;
	}

*/

} // End class HBResultSetLibrary
