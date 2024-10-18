package hre.tmgjava;
/**********************************************************************
 * Uses library com.linuxense.javadbf
 * Java library for reading and writing Xbase (dBase/DBF) files
 * https://github.com/albfernandez/javadbf
 * albfernandez/javadbf is licensed under the
 * GNU Lesser General Public License v3.0
 * Written by Alberto Fern�ndez
 *************************************************************************************
 * Update BIO tables in HRE
 * v0.00.0018 2020-03-05 - First version (N. Tolleshaug)
 * v0.00.0019 2020-03-15 - Updated with progress monitor
 * v0.00.0021 2020-04-01 - Upgraded to HRE database v17b9 2020-03-04
 * v0.00.0021 2020-04-09 - Added check for DSID when converting
 * v0.00.0022 2020-05-14 - Updated person conversion T401_BIO
 * 			  2020-05-27 - Preliminary updated for version 18b
 * 			  2020-06-13 - databaseVersion = "v18c 2020-06-01"
 * 			  2020-06-30 - databaseVersion = "v19a 2020-06-21"
 * 			  2020-06-30 - HDate implemented according to T751
 * 			  2020-07-01 - Test tmgDate.startsWith("100000000")
 * 			  2020-07-30 - Updated for DDL v20a 2020-07-26
 * v0.01.0025 2020-11-13 - Primary name or BEST_NAME_RPID implemented. (N. Tolleshaug)
 *			  2020-11-15 - Removed DDL19a (N. Tolleshaug)
 *			  2020-11-18 - Implemented name display options DISP/SORT (N. Tolleshaug)
 *			  2020-11-25 - Implemented fix for Father or Mother = 0 (N. Tolleshaug)
 *			  2020-12-04 - Fix for HDATE  T401_LIFE_FORMS (N. Tolleshaug)
 * v0.01.0026 2021-07-09 - Removed "T402_PERSON_NAMES","DISPLAY_SUR_FIRST","VARCHAR(100)"
 *         				 - Removed"T402_PERSON_NAMES","DISPLAY_GIV_FIRST","VARCHAR(100)");
 * v0.01.0028 2022-10-13 - Implemented T160 import from TMG  (N. Tolleshaug)
 * 			  2022-11-10 - Implemented IS_TMG boolean for TMG import (N. Tolleshaug)
 * 			  2022-11-15 - Other output styles imported from TMG (N. Tolleshaug)
 * 			  2022-11-18 - New T160/T162 imported from TMG (N. Tolleshaug)
 * 			  2022-12-05 - TMG styles with DSID = 0 set IS_SYSTEM = TRUE
 * 			  2022-01-10 - Error in HRE codes when converted from TMG (N. Tolleshaug)
 * 			  2022-01-20 - Corrected location code converted from TMG (N. Tolleshaug)
 * v0.01.0029 2023-05-01 - Implemented v22a (N. Tolleshaug)
 * v0.01.0030 2023-07-06 - Removed dump of flags (N. Tolleshaug)
 * 			  2023-08-08 - Implemented TMG translation of flag data to T204 (N. Tolleshaug)
 * 			  2023-08-14 - Implemented fix for multiple datasets  (N. Tolleshaug)
 * 			  2023-08-15 - Fix if project contains TMG default flags dupl. (N. Tolleshaug)
 * v0.01.0031 2024-09-29 - Updated with this version in Build31 and Master 31 (N. Tolleshaug)
 ***************************************************************************************
 * NOTE 01 - 	Insert updates only the database
 * 				To see the inserted rows in ResultSet
 * 				the program must do a new SELECT
 * NOTE 02 - 	HDATE missing, need to be implemented
 *********************************************************************
 */

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;

import hre.bila.HBException;

/**
 * class TMGpass_V22a_Support
 * @author NTo
 * @since 2022-10-02
 */

public class TMGpass_Support {

	HREdatabaseHandler pointHREbase;
	NameStyleStat pointNameStyleStat;

	HashMap<Integer, NameStyleStat> nameStyleMap = new HashMap<>();

	TMGtableData  tmgSTtable = null;
	TMGtableData  tmgCtable = null;
	ResultSet tableT160 = null, tableT162 = null, tableT204 = null, tableT251 = null;
	int index = 0;
	int flagIdent = 0;
	String[] flagFields;
	String translatedFlag = "T204_FLAG_TRAN";
	boolean updateFlags = TMGglobal.CONVERT_ALL_FLAG;
	
// Temp solution to identify preloaded flag definitions
	String[] flagFieldsT251 = {"SEX","LIVING","BIRTHORDER","MULTIBIRTH","ADOPTED","ANCE_INT","DESC_INT"};
	int[] defaultIndexList = {0,0,0,0,0,0,0};
	
// To find the flagIdent	
	HashMap<String,Integer> tmgFlagFieldIndex = new HashMap<String,Integer>();
	
// Index to find row for specific flag field
	HashMap<String,Integer> flagIdentIndex = new HashMap<String,Integer>();
	
// To find the PID's for T251
	HashMap<Integer,Long> flagDefsT251PID = new HashMap<Integer,Long>();
	
// This is not used
	HashMap<Integer,Integer> defaultindex = new HashMap<Integer,Integer>();	
	
// For Translated flag data from PROPERTY	
	HashMap<String,String[]> languagTransIndex = new HashMap<String,String[]>();
	
// Name Code jndex	
	HashMap<String,String> nameCodes;

	long proOffset = 1000000000000000L;
	long null_RPID  = 1999999999999999L;

// Max number printed
	//int maxNrPrint = 300;
	int maxNrPrint = 20;
	String personStyleCodes = "1100|2000|3000|5000|5200|5700|3700|5500|3300|";
	String placeStyleCodes = "0500|1100|3000|3100|3400|3500|3900|0100|4100|4300|";
	String styleCodes;
	boolean isSystem = false;

	String[] standardNameList,
	 		 userNameStyle,
	 		 outputNameStyle,
	 		 outputCodeStyle;

	String displayStyle, outputStyleString, outputSurnameFirstString, outputGivenFirstString;
	String styleName, outStyleNameType, styleGroup, outType;

	long nameStylePID, pidTableT162;

/**
 * getPersonStyleCodes(int index)
 * @param index
 * @return
 */
	public String getPersonStyleCodes(int index) {
		String [] styleCodes = personStyleCodes.split("\\|");
		return styleCodes[index];
	}

/**
 * getPlaceStyleCodes(int index)
 * @param index
 * @return
 */
	public String getPlaceStyleCodes(int index) {
		String [] styleCodes = placeStyleCodes.split("\\|");
		return styleCodes[index];
	}

/**
 * getNameStyleHashMap()
 * @return
 */
	public HashMap<Integer, NameStyleStat> getNameStyleHashMap() {
		return nameStyleMap;
	}
	
	public HashMap<String,Integer> getTmgFlagFieldIndex() {
		return tmgFlagFieldIndex;
	}
	
	public HashMap<String,Integer> getFlagIdentIndex() {
		return flagIdentIndex;
	}
	
	public HashMap<Integer,Long>  getFlagDefsT251PID() {
		return flagDefsT251PID;
	}

	public HashMap<Integer,Integer> getDefaultIndex() {
		return  defaultindex;
	}

/**
 * addUsedStyle(int styleId)
 * @param styleId
 */
	public void addUsedStyle(int styleId) {
		if (nameStyleMap.containsKey(styleId))
				nameStyleMap.get(styleId).addUsedStyles();
		else System.out.println(" WARNING - TMGpass_V22a_Support - addUsedStyle: no styleID: "
				+ styleId + " in name style ..st.dbf table!");
	}

/**
 * getNameStylePID(int styleId)
 * @param styleId
 * @return
 */
	public long getNameStylePID(int styleId) {
		if (nameStyleMap.containsKey(styleId))
			return nameStyleMap.get(styleId).getNameStylePID();
		else {
			System.out.println(" WARNING - TMGpass_V22a_Support - getNameStylePID: no styleID: "
					+ styleId + " in name style ..st.dbf table!");
			return null_RPID;
		}
	}
	
	public String getFlagFieldString() {
		String flagFieldString = "";
		for (int i = 0; i < flagFields.length; i++)
			flagFieldString = flagFieldString + flagFields[i] + "|";
		return flagFieldString;
	}
	
	public String[] getFlagFields() {
		return flagFields;
	}

/**
 * TMGPassBIO_Names(TMGDatabaseHREhandler pointHREbase)
 * @param pointHREbase
 */
	public TMGpass_Support(HREdatabaseHandler pointHREbase) {

		this.pointHREbase = pointHREbase;
		tmgSTtable = TMGglobal.tmg_ST_table;
		tmgCtable = TMGglobal.tmg_C_table;
		tableT160 = TMGglobal.T160;
		tableT162 = TMGglobal.T162;
		tableT204 = TMGglobal.T204; // T204_FLAG_TRAN
		tableT251 = TMGglobal.T251;
	}

	public void addNameStyleToHRE(TMGHREconverter tmgHreConverter) throws HCException {
		int currentRow = 0;
		try {
		// Find last PID
			tableT160.last();
			tableT162.last();
			pidTableT162 = tableT162.getLong("PID");

			if (TMGglobal.DEBUG)
				System.out.println("T160 Table ResultSet size before insert: " + tableT160.getRow());

			if (TMGglobal.DEBUG) System.out.println("T160 tmgSTable end PID: " + (proOffset + tmgSTtable.getNrOfRows()));

			int nrOftmgSTRows = tmgSTtable.getNrOfRows();

			if (TMGglobal.DEBUG)
				System.out.println(" tmg _ST.dbf Table size: " + nrOftmgSTRows + " rows");

			int progress;
			tmgHreConverter.setStatusProgress(0);

			if (TMGglobal.DEBUG)
				System.out.println("*** Updating HRE T160/165 Name Style");

		// Insert new records - number starts from  = 1
			for (int index = 0; index < nrOftmgSTRows; index++) {

				currentRow = index + 1;

		// Report progress in %
				progress = (int)Math.round(((double)currentRow / (double)nrOftmgSTRows) * 100);
				tmgHreConverter.setStatusProgress(progress);

				if (TMGglobal.DEBUG)
					System.out.println("** TMG name_ST.dbf - indexPID: " + index
						+ " / STYLE: " + tmgSTtable.getValueInt(index,"STYLEID"));

				int styleID = tmgSTtable.getValueInt(index,"STYLEID");
				if (styleID > 0 ) {
					styleGroup = tmgSTtable.getValueString(index,"GROUP");
					styleName = tmgSTtable.getValueString(index,"STYLENAME");

			// Set Style codes for N and P
					if (styleGroup.equals("N"))  styleCodes = personStyleCodes;
					else if (styleGroup.equals("P"))  styleCodes = placeStyleCodes;
					else System.out.println("addNameStyleToHRE - StyleGroup not found!");

/**
 * Some Name Styles have a strange Style Name indication errors in TMG Style manager
 */
					if (styleName.length() > 26) {
						if (TMGglobal.DEBUG)
							System.out.println(" *** Test TMG long style name: " + styleName );
						String styleTypeName = "";
						if (styleGroup.equals("N")) styleTypeName = "name";
						else if (styleGroup.equals("P")) styleTypeName = "place";
						else System.out.println(" Not found style type: " + styleGroup);
						styleName = "TMG " +  styleTypeName + " style nr: " + index;
					} else styleName = "TMG-" + styleName;
/**
 * 	NAME_STYLE	processing		Testing DSID in TMG ST.dbf
 */

					if (TMGglobal.dataSetID == tmgSTtable.getValueInt(index, "DSID")) { // DSID = 1

						nameCodes = new HashMap<String,String>();

					// For TMG user made name styles set IS_SYSTEM = FALSE
						isSystem = false;

				// Handle 	TMG ST_DISPLAY style
						displayStyle = tmgSTtable.getValueString(index,"ST_DISPLAY");
						userNameStyle = displayStyle.split(",");
						String[] styleCodeArray = styleCodes.split("\\|");
						for (int i=0; i < userNameStyle.length; i++) {
							nameCodes.put(userNameStyle[i].trim(),styleCodeArray[i].trim());
							if (TMGglobal.DEBUG)
								System.out.println("Map: " + userNameStyle[i].trim() + " = " + styleCodeArray[i].trim());
						}
						String nameStyleList = createStyleString(userNameStyle);

						long nameStylePID = tableT160.getLong("PID") + index + 1;
						addToT160_NAME_STYLE(nameStylePID, tableT160, styleGroup, styleName, "Imported " + styleGroup + "-style from TMG",
														styleCodes, nameStyleList);

			// Counting use of styles
						pointNameStyleStat = new NameStyleStat(nameStylePID, styleGroup, styleName);
						nameStyleMap.put(styleID, pointNameStyleStat);

			// generate output style in T162
						pidTableT162++;
						if (styleGroup.equals("N")) outType = "R"; else outType = "D";
						outputStyleString = tmgSTtable.getValueString(index,"ST_OUTPUT");
						genOutputStyle(index, pidTableT162, nameStylePID, outputStyleString, "ST_OUTPUT");
						if (TMGglobal.DEBUG)
							System.out.println(" TMG DSID 1 - ST_OUTPUT " + styleGroup + " = " + outputStyleString);
						if (styleGroup.equals("N")) {

							pidTableT162++;
							outType = "D";
							outStyleNameType = "GVNAMEDISP";
							outputSurnameFirstString = tmgSTtable.getValueString(index,outStyleNameType);
							genOutputStyle(index, pidTableT162, nameStylePID, outputSurnameFirstString, outStyleNameType);
							if (TMGglobal.DEBUG)
								System.out.println(" TMG DSID 1 - " +  outStyleNameType + " Group: " + styleGroup
										+ " = " + outputSurnameFirstString);

							pidTableT162++;
							outType = "D";
							outStyleNameType = "SRNAMEDISP";
							outputGivenFirstString = tmgSTtable.getValueString(index,outStyleNameType);
							genOutputStyle(index, pidTableT162, nameStylePID, outputGivenFirstString, outStyleNameType);
							if (TMGglobal.DEBUG)
								System.out.println(" TMG DSID 1 - " +  outStyleNameType + " Group: " + styleGroup
										+ " = " + outputGivenFirstString);
						}

					} else { // DSID = 0
						// For TMG generic name styles set IS_SYSTEM = TRUE
						isSystem = true;
						nameCodes = new HashMap<>();
						displayStyle = tmgSTtable.getValueString(index,"ST_DISPLAY");
						standardNameList = displayStyle.split(",");

					// Set up code map
						String[] styleCodeArray = styleCodes.split("\\|");
						for (int i=0; i < standardNameList.length; i++) {
							nameCodes.put(standardNameList[i].trim(),styleCodeArray[i].trim());
							if (TMGglobal.DEBUG)
								System.out.println("Map: " + standardNameList[i].trim() + " = " + styleCodeArray[i].trim());
						}

						String standardNamesList = createStyleString(standardNameList);

						long nameStylePID = tableT160.getLong("PID") + index + 1;
						addToT160_NAME_STYLE(nameStylePID, tableT160, styleGroup, styleName,  "Imported " + styleGroup + "-style from TMG",
														styleCodes, standardNamesList);

			// Counting use of styles
						pointNameStyleStat = new NameStyleStat(nameStylePID, styleGroup, styleName);
						nameStyleMap.put(styleID, pointNameStyleStat);

			// generate output style in T162

						pidTableT162++;
						if (styleGroup.equals("N")) outType = "R"; else outType = "D";
						outputStyleString = tmgSTtable.getValueString(index,"ST_OUTPUT");
						genOutputStyle(index, pidTableT162, nameStylePID, outputStyleString, "ST_OUTPUT");
						if (TMGglobal.DEBUG)
							System.out.println(" TMG TMG DSID 0 - ST_OUTPUT " + styleGroup + " = " + outputStyleString);

						if (styleGroup.equals("N")) {

							pidTableT162++;
							outType = "D";
							outStyleNameType = "GVNAMEDISP";
							outputSurnameFirstString = tmgSTtable.getValueString(index,outStyleNameType);
							genOutputStyle(index, pidTableT162, nameStylePID, outputSurnameFirstString, outStyleNameType);
							if (TMGglobal.DEBUG)
								System.out.println(" TMG DSID 0 - " +  outStyleNameType + " Group: " + styleGroup
										+ " = " + outputSurnameFirstString);

							pidTableT162++;
							outType = "D";
							outStyleNameType = "SRNAMEDISP";
							outputGivenFirstString = tmgSTtable.getValueString(index,outStyleNameType);
							genOutputStyle(index, pidTableT162, nameStylePID, outputGivenFirstString, outStyleNameType);
							if (TMGglobal.DEBUG)
								System.out.println(" TMG DSID 0 - " +  outStyleNameType + " Group: " + styleGroup
										+ " = " + outputGivenFirstString);
						}
					}
				}
			}

		} catch (SQLException sqle) {
			System.out.println("SQLxception - addNameStyle: " + sqle.getMessage()
				+ "\nTMG name_ST.dbf - index: " + currentRow);
			throw new HCException("SQLxception - addNameStyle: " + sqle.getMessage()
			+ "\nTMG name_ST.dbf - index: " + currentRow + "\n" + sqle.getMessage());
		} catch (HCException hre) {
			System.out.println("HRException - addNameStyle: " + hre.getMessage()
				+ "\nTMG name_ST.dbf - index: " + currentRow);
			throw new HCException("HRException - addNameStyle: " + hre.getMessage()
			+ "\nTMG name_ST.dbf - index: " + currentRow + "\n" + hre.getMessage());
		}
	}

/**
 * genOutputStyle(
 * @param index
 * @param pidTableT162
 * @param nameStylePID
 * @param outputStyleString
 * @param tmgOutStyle
 * @throws HCException
 */
	private void genOutputStyle(int index, long pidTableT162,
								long nameStylePID, String outputStyleString,
								String tmgOutStyle) throws HCException  {

		// Extract style names from style string
				boolean debug = TMGglobal.DEBUG;
				String separatorString = "#,", addSeparator = "";

				String[] outputNameStyle = findNamesInStyle(outputStyleString);
				String[] outputCodeStyle = new String[outputNameStyle.length];
				if (debug) System.out.print(" Element names: ");
				for (int i = 0; i < outputNameStyle.length; i++) {
					addSeparator = "";
					String elementName = outputNameStyle[i].trim();
					if (debug) System.out.print(elementName + "|");
					if (elementName.endsWith(",")) {
						elementName = elementName.substring(0,elementName.length()-1);
						addSeparator = separatorString;
					}
					if (nameCodes.containsKey(elementName))
						outputCodeStyle[i] = nameCodes.get(elementName) + addSeparator;
					else {
						outputCodeStyle[i] = "0000";
						System.out.println(" WARNING - TMGpass_V22a_Support - missing element code for: " + outputNameStyle[i]);
					}
				}
				if (debug) System.out.println();

		// Create output Style data name/codes
				String elementCodes = createStyleString(outputCodeStyle);
				if (TMGglobal.DEBUG)
					System.out.println(" TMG-Style: "+ styleName + " -  OUTPUT codes: " + elementCodes);

				addToT162_NAME_STYLE_OUTPUT(pidTableT162,
												tableT162,
												nameStylePID,
												styleGroup,
												"TMG style " + tmgOutStyle,
												"TMG output " + styleGroup + " - " + tmgOutStyle,
												elementCodes);
	}

/**
 * addToT160_PERSON_NAME_STYLE(int rowPID, ResultSet hreTable)
 * @param rowPID
 * @param hreTable
 * @throws HCException
 */
	private void addToT160_NAME_STYLE(long tablePID,
									  ResultSet hreTable,
									  String nameType,
									  String styleName,
									  String description,
									  String codes,
									  String names) throws HCException {

		if (TMGglobal.DEBUG)  System.out.println("Start - addToT160_NAME_STYLE row: " + tablePID);

		try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();
		// Update new row in H2 database
			hreTable.updateLong("PID", tablePID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateBoolean("IS_SYSTEM", isSystem);
			hreTable.updateBoolean("IS_DEFAULT", false);
			hreTable.updateBoolean("IS_TMG", true);
			hreTable.updateString("NAME_TYPE", nameType);
			hreTable.updateString("NAME_STYLE", styleName);
			hreTable.updateString("NAME_STYLE_DESC", description);
			hreTable.updateString("ELEMNT_CODES", codes);
			hreTable.updateString("ELEMNT_NAMES", names);

		//Insert row in database
			hreTable.insertRow();

			if (TMGglobal.DEBUG)  System.out.println("End - addToT160_PERSON_NAME_STYLE row: " + tablePID);
		} catch (SQLException sqle) {
			if (TMGglobal.DEBUG) System.out.println(" update table - T160_PERSON_NAME_STYLE row: " + tablePID);
			sqle.printStackTrace();
			throw new HCException("TMGPass_Names_Styles - addToT160_PERSON_NAME_STYLE: " + sqle.getMessage());
		}
	}

/**
 *  T162_NAME_STYLE_OUTPUT
 *  PID
 *  CL_COMMIT_RPID
 *  IS_DEFAULT
 *  OWNER_RECORD_RPID
 *  OUT_TYPE
 *  NAME_TYPE
 *  OUT_NAME_STYLE
 *  OUT_NAME_STYLE_DESC
 *  OUT_ELEMNT_CODES
 */

/**
 * addToT162_NAME_STYLE_OUTPUT
 * @param tablePID
 * @param hreTable
 * @param ownerRPID
 * @param nameType
 * @param styleName
 * @param description
 * @param codes
 * @throws HCException
 */
	private void addToT162_NAME_STYLE_OUTPUT(long tablePID,
			 						ResultSet hreTable,
			 						long ownerRPID,
			 						String nameType,
			 						String styleName,
			 						String description,
			 						String codes) throws HCException {

		if (TMGglobal.DEBUG)  System.out.println("Start - addToT16X_NAME_STYLE row: " + tablePID);

		try {
			// moves cursor to the insert row
			hreTable.moveToInsertRow();
			// Update new row in H2 database
			hreTable.updateLong("PID", tablePID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateBoolean("IS_SYSTEM", isSystem);
			hreTable.updateBoolean("IS_TMG", true);
			hreTable.updateLong("OWNER_RECORD_RPID", ownerRPID);
			hreTable.updateString("OUT_TYPE", outType);
			hreTable.updateString("NAME_TYPE", nameType);
			hreTable.updateString("OUT_NAME_STYLE", styleName);
			hreTable.updateString("OUT_NAME_STYLE_DESC", description);
			hreTable.updateString("OUT_ELEMNT_CODES", codes);

		//Insert row in database
			hreTable.insertRow();

			if (TMGglobal.DEBUG)  System.out.println("End - addto - T162_NAME_STYLE_OUTPUT row: " + tablePID);
		} catch (SQLException sqle) {
			if (TMGglobal.DEBUG) System.out.println(" update table - - T162_NAME_STYLE_OUTPUT row: " + tablePID);
			sqle.printStackTrace();
			throw new HCException("TMGPass_Names_Styles - addToT162_NAME_STYLE_OUTPUT - : " + sqle.getMessage());
		}
	}

/**
 * createStyleString(String styleName, String[] styledata)
 * @param styleName
 * @param outpuStyleArray
 * @return
 */
   private String createStyleString(String[] outpuStyleArray) {
	   String style = "";
		for (String element : outpuStyleArray)
			style = style + element.trim() + "|";
		return style;
   }

/**
 * findNamesInStyle(String tmgStyle)
 * @param tmgStyle
 * @return
 */
	private String[] findNamesInStyle(String tmgStyle) {
		String newName = null;

		Vector<String> styleNames = new Vector<>();
		for ( int i= 0; i < tmgStyle.length(); i++) {
			Character newChar = tmgStyle.charAt(i);
			if (newChar.equals('[')) newName = "";
			if (!newChar.equals(']')) {
				if (!newChar.equals('['))
					newName = newName + newChar;
			}
			if (newChar.equals(']') && i < tmgStyle.length()-1) {
				Character nextChar = tmgStyle.charAt(i + 1);
				if (nextChar.equals(',')) styleNames.add(newName + nextChar);
				else styleNames.add(newName);
			} else if (newChar.equals(']')) styleNames.add(newName);
		}
		return styleNames.toArray(new String[0]);
	}

/**
 * printResSet(ResultSet resultSet)
 * @param resultSet
 * @throws SQLException
 */
   public static void printResSet(ResultSet resultSet) throws SQLException{
	   System.out.println("*** Print ResultSet");
	   //Ensure we start with first row
	      resultSet.beforeFirst();
	      while(resultSet.next()){
	         //Retrieve by column name
	         long id  = resultSet.getInt("PID");
	         //int age = resultSet.getInt("age");
	         //String first = resultSet.getString("first");
	         //String last = resultSet.getString("last");

	         //Display values
	         System.out.print("PID: " + id);
	         //System.out.print(", Age: " + age);
	         //System.out.print(", First: " + first);
	         //System.out.println(", Last: " + last);
	     }
	     System.out.println();
	}//end printRs()
   
/***************************************************************************************************
 * HRE table Convert flags from TMG Flags 
 ****************************************************************************************************/
   
/**
 * updateFlagFromTMG(TMGHREconverter tmgHreConverter)   
 * @param tmgHreConverter
 * @throws HCException
 */
   public void updateFlagFromTMG(TMGHREconverter tmgHreConverter) throws HCException {
	    
		int currentRow = 0;
		String propertyTrans = "";
		try {
		// Find last PID
			tableT251.last();
		
			if (TMGglobal.DEBUG)
				System.out.println(" T251 Table ResultSet size before insert: " + tableT251.getRow());
	
			if (TMGglobal.DEBUG) 
				System.out.println(" T251 tmgCable end PID: " + (proOffset + tmgCtable.getNrOfRows()));	
			
		// Update ACTIVE and GUI seq in preloaded table			
			int nrOftmgCrows = tmgCtable.getNrOfRows();
			int index251 = 0;	
			tableT251.beforeFirst();
			while (tableT251.next()) {
				String flagField = flagFieldsT251[index251];
				flagIdent = tableT251.getInt("FLAG_IDENT");
				if (!flagIdentIndex.containsKey(flagField))
						flagIdentIndex.put(flagField, flagIdent);
				if (TMGglobal.DEBUG) System.out.println("Flag field: " + flagField + "/" + flagIdent);
				int defaultIndex = defaultIndexList[index251];
				defaultindex.put(flagIdent, defaultIndex);		
				for (index = 0; index < nrOftmgCrows; index++) {
					String tmgFlagField = tmgCtable.getValueString(index,"FLAGFIELD");
					int DSID = tmgCtable.getValueInt(index,"DSID");
					if (flagField.equals(tmgFlagField) && DSID == 0) { 
					//if (flagField.equals(tmgFlagField)) { 
						propertyTrans = tmgCtable.getValueString(index,"PROPERTY");
						tableT251.updateBoolean("ACTIVE", tmgCtable.getValueBoolean(index,"ACTIVE"));
						tableT251.updateInt("GUI_SEQ", tmgCtable.getValueInt(index,"SEQUENCE"));		
						tableT251.updateInt("DEFAULT_INDEX", defaultIndex);							
						tableT251.updateRow();		
						//continue;
						break;
					}
				}
			// update T204_FLAG_TRAN for language flag translations		
				processPropertyTransTMG(propertyTrans);
				updateT204_FLAG_TRAN(flagIdent, updateFlags);
				if (TMGglobal.DEBUG) 
					 System.out.println("Updated index: " + index251 + "/" + flagIdent + "\n" + propertyTrans);
				index251++;		
			}
			
			flagFields = new String[nrOftmgCrows];
			if (TMGglobal.DEBUG)
				System.out.println(" tmg _C.dbf Table size: " + nrOftmgCrows + " rows");
	
			int progress;
			tmgHreConverter.setStatusProgress(0);
	
			if (TMGglobal.DEBUG)
				System.out.println("*** Updating HRE T204/251 Flags");
			
			long lastT251PID = pointHREbase.lastRowPID("T251_FLAG_DEFN");
			long lastT204PID = pointHREbase.lastRowPID("T204_FLAG_TRAN");
	
		// Insert new records - number starts from  = 1
			for (index = 0; index < nrOftmgCrows; index++) {
				currentRow = index + 1;

		// Report progress in %
				progress = (int)Math.round(((double)currentRow / (double)nrOftmgCrows) * 100);
				tmgHreConverter.setStatusProgress(progress);
				
		// Set up list of Flag Fields in C.dbf	
				String flagField = tmgCtable.getValueString(index,"FLAGFIELD");
				int tmgFlagIdent = tmgCtable.getValueInt(index,"FLAGID");
				flagFields[index] = flagField;
				
		// Set up index to find row for specific flag field
				tmgFlagFieldIndex.put(flagField, tmgFlagIdent);
							
				if (TMGglobal.DEBUG)
					System.out.println(" TMG flag id: " + tmgFlagIdent 
						+ " - Active = " + tmgCtable.getValueBoolean(index, "ACTIVE")
						+ " - Label: " + tmgCtable.getValueString(index,"FLAGLABEL")   
						+ " - Field: " + tmgCtable.getValueString(index,"FLAGFIELD"));
				
				if (tmgCtable.getValueInt(index, "DSID") == 1) {
					
					//System.out.println(" TMG-Label: " + tmgCtable.getValueString(index,"FLAGLABEL") + " Field: "  
					//					+ tmgCtable.getValueString(index,"FLAGFIELD"));
					
					long table251PID = ++lastT251PID;
					
			// If project contain duplicates of the TMG default flags
					if (!flagIdentIndex.containsKey(flagField))
						flagIdentIndex.put(flagField, ++flagIdent);
					
			// Set up index table to find RPID from flagId
					flagDefsT251PID.put(tmgFlagIdent, table251PID);
			
					int defaultIndex = 0;
					defaultindex.put(tmgFlagIdent, defaultIndex);
					
			// PID for person in T169
					long baseTypeRPID = 1000000000000001L;  // Base Type Person		
					
			// Add T251_FLAG_DEFN row		
					addToT251_FLAG_DEFN(table251PID, flagIdent ,baseTypeRPID, defaultIndex, tableT251);
					
			// Add T204_FLAG_TRAN row
					long table204PID = ++lastT204PID;
					addToT204_FLAG_TRAN(table204PID, flagIdent, tmgHreConverter.languageTMG(), tableT204 );
				}			
			}		
			
		} catch (SQLException | HBException sqle) {
			System.out.println("SQLxception - updateFlagFromTMG: " + sqle.getMessage()
			+ "\nTMG name_C.dbf - index: " + currentRow);
		throw new HCException("SQLxception - updateFlagFromTMG: " + sqle.getMessage()
					+ "\nTMG name_ST.dbf - index: " + currentRow + "\n" + sqle.getMessage());
		}	
   }
   
/**
 * processPropertyTransTMG(String flagProperty)
 * @param flagProperty
 * 
	n_Norwegian=ADOPTERT
	v_Norwegian=?,N,J
	d_Norwegian=  ?=ukjent|N=ikke adoptert|J=adoptert
  */
   private void processPropertyTransTMG(String flagProperty) {
	   int index = 0;
	   String flagName = "", flagDescr = "", flagValues = "", language = "";
	   String[] propertyCont;
	   String[] flagTransData;
	   languagTransIndex = new HashMap<String,String[]>();
	   if (TMGglobal.DEBUG) 
		   System.out.println("Property:\n" + flagProperty);
	   String[] propertyLines = flagProperty.split("\n");
	   for (int i = 0; i < propertyLines.length; i++) {
		   propertyLines[i] = propertyLines[i].replace("\n", "").replace("\r","").trim();
		   if (propertyLines[i].startsWith("n_")) {
			   propertyCont = propertyLines[i].split("=");
			   language = propertyCont[0].replace("n_", "").trim();
			   flagName = propertyCont[1];	
			   index++;
		   } else if (propertyLines[i].startsWith("d_")) {
			   propertyCont = propertyLines[i].split("=");
			   flagDescr = propertyLines[i].replace(propertyCont[0], "").replaceFirst("=", "");
			   index++;
		   } else if (propertyLines[i].startsWith("v_")) {
			   propertyCont = propertyLines[i].split("=");
			   flagValues = propertyCont[1];
			   index++;
		   }
		   if (index == 3) {
			   String langCode = getTagLangCode(language);
			   flagTransData = new String[3];
			   		   
			   flagTransData[0] = flagName;
			   flagTransData[1] = flagValues;
			   flagTransData[2] = flagDescr.replace("|", "\n");		   
			   index = 0;
			   languagTransIndex.put(langCode, flagTransData);
			   if (TMGglobal.DEBUG) 
				   System.out.println("Data translation: " + langCode 
						   + "/" + flagName + "/" + flagValues + "/" + flagDescr);
		   }
	   }
   }
   
/**
 * getLangCode(String language)
 * @param language
 * @return
 */
   	private String getTagLangCode(String language) {
   		String code = "en-US";
   		if (language.contains("Afrikaans")) code = "af-ZA";
   		if (language.contains("English")) code = "en-US";
   		if (language.contains("Englishuk")) code = "en-GB";
   		if (language.contains("Danish")) code = "da-DK";
   		if (language.contains("Dutch")) code = "nl-NL";
   		if (language.contains("French")) code = "fr-FR";
   		if (language.contains("German")) code = "de-DE";
   		if (language.contains("Italian")) code = "it-IT";
   		if (language.contains("Norwegian")) code = "no-NB";
   		if (language.contains("Norwegia2")) code = "no-NN";
   		return code;
   	}
   	
 /**
  * updateT204_FLAG_TRAN(int flagIdent)  	
  * @param flagIdent
  * @throws HCException
  */
   	private void updateT204_FLAG_TRAN(int flagIdent, boolean useImport) throws HCException {
		String selectString = pointHREbase.setSelectSQL("*", translatedFlag, "FLAG_IDENT = " + flagIdent);	
		ResultSet flagTranslationSet = pointHREbase.requestTabledata(translatedFlag, selectString);
		try {
			flagTranslationSet.beforeFirst();
			while (flagTranslationSet.next())  {
				String lanCode = flagTranslationSet.getString("LANG_CODE");
				if (TMGglobal.DEBUG) 
					System.out.println( " Lang code: " + lanCode);
				if (languagTransIndex.containsKey(lanCode)) {
					String [] flagData = languagTransIndex.get(lanCode);
					if (TMGglobal.DEBUG) {
						System.out.println(" Pre: " + flagTranslationSet.getString("FLAG_NAME").trim() 
								+ "/" + flagTranslationSet.getString("FLAG_VALUES"));
						System.out.println(" Impoted name: " + flagData[0] + "/Value: " + flagData[1]);	
					}
					flagTranslationSet.updateString("FLAG_VALUES", 
							convertFlagValues(flagTranslationSet.getString("FLAG_VALUES"), flagData[1]));
					if (useImport) flagTranslationSet.updateString("FLAG_NAME", flagData[0]);
					if (useImport) flagTranslationSet.updateString("FLAG_VALUES", flagData[1]);
					if (useImport) flagTranslationSet.updateString("FLAG_DESC", flagData[2]);
					flagTranslationSet.updateRow();
				}
			}
		} catch (SQLException sqle) {
			System.out.println(" TMGpass_V22a_Support - updateT204_FLAG_TRAN" + sqle.getMessage());
			throw new HCException(" TMGpass_V22a_Support - updateT204_FLAG_TRAN" + sqle.getMessage());
		}
   	}
   	
/**
 * convertFlagValues(String preValues, String newValues)   	
 * @param preValues
 * @param newValues
 * @return
 */
   	private String convertFlagValues(String preValues, String newValues) {
   		int equal = 0;
   		String outString;
   		char first;
   		String[] preList = preValues.split(",");
   		String[] newList = newValues.split(",");
   		String[] outList = new String[newList.length];
   		if (preList.length == newList.length) {
   			for (int i = 0; i < preList.length; i++)
   				if (preList[i].charAt(0) == newList[i].charAt(0)) equal++;;
   		}
   		if (equal == preList.length) return preValues;
   		else {
   			for (int i = 0; i < newList.length; i++) {
   				first = newList[i].charAt(0);
   				for (int j = 0; j < preList.length; j++)
   					if (preList[j].charAt(0) == first) outList[i] = preList[j];				
   			}
   			outString = outList[0];
   			for (int k = 1; k < outList.length; k++) outString = outString + "," + outList[k];
   			return outString; 			
   		}
   	}
   
/**
 * addToT251_FLAG_DEFN(long tablePID, ResultSet hreTable)
 * @param tablePID
 * @param hreTable
 * @throws HCException
 */
   private void addToT251_FLAG_DEFN(long tablePID, int flagIdent, long baseTypeRPID, 
		   					int defaultIndex, ResultSet hreTable) throws HCException {	
	   try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();
		// Update new row in H2 database
			hreTable.updateLong("PID", tablePID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateBoolean("IS_SYSTEM", false);
			hreTable.updateBoolean("INHERIT",false);
			hreTable.updateBoolean("ACTIVE", tmgCtable.getValueBoolean(index,"ACTIVE"));
			hreTable.updateLong("BASE_TYPE_RPID", baseTypeRPID);
			hreTable.updateInt("GUI_SEQ", tmgCtable.getValueInt(index,"SEQUENCE"));
			hreTable.updateInt("FLAG_IDENT", flagIdent);
			hreTable.updateInt("DEFAULT_INDEX", defaultIndex);

		//Insert row in database
			hreTable.insertRow();
			
		} catch (SQLException sqle) {
			if (TMGglobal.DEBUG) System.out.println(" update table - T251_FLAG_DEFN row: " + tablePID);
			sqle.printStackTrace();
			throw new HCException("TMGPass_Names_Styles - addToT251_FLAG_DEFN - : " + sqle.getMessage());
		}	
   }
   
   
/**
 * addToT204_FLAG_TRAN(long tablePID, String flagLang, ResultSet hreTable)   
 * @param tablePID
 * @param flagLang
 * @param hreTable
 * @throws HCException
 */
   private void addToT204_FLAG_TRAN(long tablePID, int flagIdent, String flagLang, ResultSet hreTable) throws HCException {
	   try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();
		// Update new row in H2 database
			hreTable.updateLong("PID", tablePID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateBoolean("IS_SYSTEM", false);
			hreTable.updateInt("FLAG_IDENT", flagIdent);
			hreTable.updateString("LANG_CODE",flagLang);
			hreTable.updateString("FLAG_NAME", tmgCtable.getValueString(index,"FLAGLABEL"));
			hreTable.updateString("FLAG_VALUES", tmgCtable.getValueString(index,"FLAGVALUE"));	
			String decript = HREmemo.returnStringContent(tmgCtable.getValueString(index,"DESCRIPT"));
			if (decript.length() > 2000) {	
				System.out.println(" *** Long T204_FLAG_TRAN - DESCRIPT - Length: " 
						+ decript.length() + "/2000");
				decript = decript.substring(0,2000);	
			} 
			hreTable.updateString("FLAG_DESC", decript);		
		//Insert row in database
			hreTable.insertRow();				
		} catch (SQLException sqle) {
			if (TMGglobal.DEBUG) System.out.println(" update table - T204_FLAG_TRAN row: " + tablePID);
			sqle.printStackTrace();
			throw new HCException("TMGPass_Names_Styles - T204_FLAG_TRAN - : " + sqle.getMessage());
		}
   }
} // End TMGpass_Support

class NameStyleStat {

	long null_RPID  = 1999999999999999L;
	long nameStylePID = null_RPID;
	String styleName;
	String nameType;
	int nameStyleUse;
	public NameStyleStat(long nameStylePID, String nameType, String styleName) {
		this.nameStylePID = nameStylePID;
		this.nameType = nameType;
		this.styleName = styleName;
	}

	public long getNameStylePID() {
		return nameStylePID;
	}

	public String getNameType() {
		return nameType;
	}

	public String getStyleName() {
		return styleName;
	}

	public int getNameStyleUse() {
		return nameStyleUse;
	}

	public void addUsedStyles() {
		nameStyleUse++;
	}
} // End nameStyleStat

