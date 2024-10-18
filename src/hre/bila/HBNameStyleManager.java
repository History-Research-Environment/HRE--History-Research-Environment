package hre.bila;
/*************************************************************************************
 * Class HBNameStyleManager extends BusinessLayer
 * Processes data for Name Styles Person and Location
 * Receives requests from User GUI to action methods
 * Sends requests to database over Database Layer API
 * ***********************************************************************************
 * v0.01.0028 2022-09-25 - Added Name Style Management class (N. Tolleshaug)
 * 			  2022-10-07 - Added Update Name Style Element Description (N. Tolleshaug)
 * 			  2022-10-24 - Default Element Description = "null|" in seed v21i (N. Tolleshaug)
 * 			  2022-11-12 - Activated output style data from database (N. Tolleshaug)
 * 			  2022-11-15 - Other output styles imported from TMG (N. Tolleshaug)
 * 			  2022-11-16 - Can open both Person and Location Name Styles manager (N. Tolleshaug)
 * 			  2022-11-18 - New DDL 21a for T160/T162  (N. Tolleshaug)
 *  		  2022-11-24 - Activated setting of style type in database (N. Tolleshaug)
 *  		  2023-01-02 - Reset elements description lists (N. Tolleshaug)
 *  		  2023-01-17 - Compensate for 3 digits in location element code (N. Tolleshaug)
 *  		  2023-01-19 - Reset of Location table if update of location style (N. Tolleshaug)
 *  		  2023-01-25 - Implemented Convert TMG and new exception handling (N. Tolleshaug)
 *  		  2023-02-01 - Added code for add new person (N. Tolleshaug)
 *  		  2023-02-04 - Added code for truncation of new style name (N. Tolleshaug)
 * v0.03.0030 2021-10-06 - Truncate Output name style copy if > 30 chars (D Ferguson) 
 **********************************************************************************************
 */
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import javax.swing.DefaultListModel;

import hre.dbla.HDDatabaseLayer;
import hre.gui.HGlobal;

/**
 * NameStyleData handles Name Style Data management
 * @author Nils Tolleshaug
 * @version v0.03.0030
 * @since 2022-09-25
 */
public class HBNameStyleManager extends HBBusinessLayer {
	int dataBaseIndex = 0;
	String selectedStyle = "";
	String dataLanguage;
	String type;

	String nameStyleTableName, nameElementTableName;

	int nameStyleIndex = 0;
	int defaultStyleIndex = 0;
	long[] nameStylePID;

/**
 * Name style data from T160 table
 */
	String[] nameStyleNames;
	boolean[] isSystemNameStyle;
	boolean[] isDefaultNameStyle;
	public boolean[] isTmgNameStyle;
	String[] nameStyleText;
	String[] nameStyleCodeString;
	String[] nameStyleNameString;
/**
 * Temp processing data
 */
	String[] nameElementData;
	String[] nameElementDescription;
	String[] nameStyleCodes;
	String[] nameStyleDescript;

/**
 * 	Output style data from T162 table
 */
	String[] outputStyleNames;
	boolean[] isSystemOutputStyle;
	boolean[] isTmgOutputStyle;
	String[] outType;
	String[] outputStyleText;
	String[] outputStyleCodeString;
	String[] outputStyleNameString;
	// numberNS  of output D/R types
	int numberOfDtypes;
	int numberOfRtypes;

/**
 * Temp processing data
 */
	String[] outputElementData;
	String[] outputElementDescription;
	String[] outputStyleCodes;
	String[] outputStyleDescript;
/**
 * Administration
 */
	ResultSet nameStyleTable, nameStyleElements, outputStyleTable;
	HashMap<String,String> elementCodeMap = new HashMap<>();

/**
 * constructor NameStyleData
 * @param pointDBlayer
 * @param dataBaseIndex
 */
	public HBNameStyleManager(HDDatabaseLayer pointDBlayer, int dataBaseIndex, String type) {
		super();
		this.pointDBlayer = pointDBlayer;
		this.dataBaseIndex = dataBaseIndex;
		dataLanguage = HGlobal.dataLanguage;
		this.type = type;
	}

/**
 * updateLocationStyleTable(String langCode)
 * @param langCode
 * @return
 * @throws HBException
 * @throws SQLException
 */
	public int updateStyleTable(String nameType, String nameStyleTable, String nameElementTable) throws HBException {
		nameStyleTableName = nameStyleTable;
		nameElementTableName = nameElementTable;
		if (HGlobal.DEBUG)
			System.out.println(" Tables: " +  " Name Type: "  + nameType +  " data: "
					+ nameStyleTableName + " / " + nameElementTableName);
		setNameStyleTable(nameType);
		getAllNameStyleElements(nameType);
		setNameStyleData(0);
		return 0;
	}

/**
 * resetStyleTable(String nameType, int styleIndex)
 * @param nameType
 * @param styleIndex
 * @return
 * @throws HBException
 */
	public int resetStyleTable(String nameType, int styleIndex) throws HBException {
		setNameStyleTable(nameType);
		getAllNameStyleElements(nameType);
		setNameStyleData(styleIndex);
		return 0;
	}


/**
 * setNameStyleTable(String nameType)
 * @param nameType
 * @throws HBException
 */
	public void setNameStyleTable(String nameType) throws HBException {
		int index = 0;
		try {
			nameStyleTable = pointLibraryResultSet.getNameStylesTable(nameStyleTableName,
																	  nameType,
																	  dataBaseIndex);
			nameStyleTable.last();
			int rows = nameStyleTable.getRow();
			nameStyleNames = new String[rows];
			isSystemNameStyle = new boolean[rows];
			isDefaultNameStyle = new boolean[rows];
			isTmgNameStyle = new boolean[rows];
			nameStyleText = new String[rows];
			nameStyleCodeString = new String[rows];
			nameStyleNameString = new String[rows];
			nameStylePID = new long[rows];

			nameStyleTable.beforeFirst();
			while (nameStyleTable.next()) {
				nameStylePID[index] = nameStyleTable.getLong("PID");
				nameStyleNames[index] = nameStyleTable.getString("NAME_STYLE");
				isSystemNameStyle[index] = nameStyleTable.getBoolean("IS_SYSTEM");
				isDefaultNameStyle[index] = nameStyleTable.getBoolean("IS_DEFAULT");
				isTmgNameStyle[index] = nameStyleTable.getBoolean("IS_TMG");
				nameStyleText[index] = nameStyleTable.getString("NAME_STYLE_DESC");
				nameStyleCodeString[index] = nameStyleTable.getString("ELEMNT_CODES");
				nameStyleNameString[index] = nameStyleTable.getString("ELEMNT_NAMES");

				if (HGlobal.DEBUG)
					if (isTmgNameStyle[index])
						System.out.println(" Style descriptions " + nameStyleNameString[index]);
					else System.out.println(" Style descriptions - Null");

				if (isDefaultNameStyle[index]) {
					defaultStyleIndex = index;
					nameStyleIndex = index;
					selectedStyle = nameStyleNames[index];
				}

				selectedStyle = nameStyleNames[index];
				index++;
			}
		} catch (SQLException | HBException hbe) {
			if (HGlobal.DEBUG) System.out.println("HBToolHandler - updateStyleTable: " + hbe.getMessage());
			if (HGlobal.DEBUG) hbe.printStackTrace();
			throw new HBException("HBE01","HBE01 - Name Style Handling \n" + hbe.getMessage(),0);
		}
	}

/**
 * setOutputStyleTable(String nameType)
 * @param nameType
 * @throws HBException
 */
	public void setOutputStyleTable(String nameType) throws HBException {
		int index = 0;
		long ownerPID = nameStylePID[nameStyleIndex];
		try {
			outputStyleTable = pointLibraryResultSet.getOutputStylesTable(nameStylesOutput,
																		  nameType,
																		  ownerPID,
																		  dataBaseIndex);
			numberOfDtypes = 0;
			numberOfRtypes = 0;
			outputStyleTable.last();
			int rows = outputStyleTable.getRow();
			outputStyleNames = new String[rows];
			isSystemOutputStyle = new boolean[rows];
			isTmgOutputStyle = new boolean[rows];
			outType = new String[rows];
			outputStyleText = new String[rows];
			outputStyleCodeString = new String[rows];
			outputStyleNameString = new String[rows];
			outputStyleTable.beforeFirst();
			while (outputStyleTable.next()) {
				outputStyleNames[index] = outputStyleTable.getString("OUT_NAME_STYLE");
				isSystemOutputStyle[index] = outputStyleTable.getBoolean("IS_SYSTEM");
				isTmgOutputStyle[index] = outputStyleTable.getBoolean("IS_TMG");
				outType[index] = outputStyleTable.getString("OUT_TYPE");
				if (outType[index].equals("D")) numberOfDtypes++;
				if (outType[index].equals("R")) numberOfRtypes++;
				outputStyleText[index] = outputStyleTable.getString("OUT_NAME_STYLE_DESC");
				outputStyleCodeString[index] = outputStyleTable.getString("OUT_ELEMNT_CODES");
				if (outputStyleTable.getBoolean("IS_TMG"))
				  outputStyleNameString[index] = "";
				else outputStyleNameString[index] = "null|";

				if (HGlobal.DEBUG)
					if (isTmgOutputStyle[index])
						System.out.println(" Style descript:  " + index + " - " + outputStyleNameString[index]);
					else System.out.println(" Style descript: " + index + " -  null|");
				selectedStyle = outputStyleNames[index];
				index++;
			}
		} catch (SQLException | HBException hbe) {
			if (HGlobal.DEBUG)
				System.out.println("HBToolHandler - updateOutputStyleTable: " + hbe.getMessage());
			if (HGlobal.DEBUG)
				hbe.printStackTrace();
			throw new HBException("HBE02","HBE02 -  Name Style Handling \n" + hbe.getMessage(),0);
		}
	}


/**
 * Name styles status setting
 * @param styleIndex
 * @return boolean
 */
	
	public String[] getNameStyleElementCodes (int selectedIndex) {
		return  nameStyleCodeString[nameStyleIndex].split("\\|");
	}

	public long getNameStylePID(int styleIndex) {
		return nameStylePID[styleIndex];
	}

	public boolean getNameStyleIsSystem(int styleIndex) {
		return isSystemNameStyle[styleIndex];
	}

	public boolean getNameStyleIsDefault(int styleIndex) {
		return isDefaultNameStyle[styleIndex];
	}

	public void setNameStyleIsDefault(int styleIndex) {
		for (int i = 0; i < isDefaultNameStyle.length; i++)
			if (i == styleIndex) isDefaultNameStyle[i] = true;
			else isDefaultNameStyle[i] = false;
	}

	public void setOutStyleType(int styleIndex, String outStyleType) {
		outType[styleIndex] = outStyleType;
	}

	public boolean getNameStyleIsTMG(int styleIndex) {
		return isTmgNameStyle[styleIndex];
	}

	public boolean lastDtypeOutNS(int styleIndex, String nameType) {
		if (HGlobal.DEBUG)
			System.out.println(" Number of " +nameType + " D-types: " + numberOfDtypes);
		if (nameType.equals("N"))
			if (numberOfDtypes > 2) return false; else return true;
		else if (nameType.equals("P"))
			if (numberOfDtypes > 1) return false; else return true;
		else return true;
	}

	public boolean lastRtypeOutNS(int styleIndex, String nameType) {
		if (HGlobal.DEBUG)
			System.out.println(" Number of " +nameType + " R-types: " + numberOfRtypes);
		if (numberOfRtypes > 1) return false; else return true;
	}

/**
 * Output Name styles setting
 * @param styleIndex
 * @return boolean
 */

	public boolean getOutputStyleIsSystem(int styleIndex) {
		return isSystemOutputStyle[styleIndex];
	}

	public boolean getOutputStyleIsTMG(int styleIndex) {
		return isTmgOutputStyle[styleIndex];
	}

	public char getOutputStyleType(int styleIndex) {
		return outType[styleIndex].charAt(0);
	}

/**
 * getAllNameStyleElements()
 * @throws HBException
 */
	public void getAllNameStyleElements(String nameType) throws HBException {
		try {
			elementCodeMap = new HashMap<>();
			nameStyleElements = pointLibraryResultSet.getNameStyleElements(nameElementTableName, nameType, dataLanguage, dataBaseIndex);
			nameStyleElements.first();
			String elementData = nameStyleElements.getString("ELEMNT_CODES");
			String elementDescription = nameStyleElements.getString("ELEMNT_NAMES");
			nameElementData = elementData.split("\\|");
			nameElementDescription = elementDescription.split("\\|");
			for (int i = 0; i < nameElementDescription.length; i++) {
				//Set up index to name element description with key name element code
				if (HGlobal.DEBUG)
					System.out.println(" " + i + " element "
							   + nameElementData[i] + "/" + nameElementDescription[i]);
				elementCodeMap.put(nameElementData[i], nameElementDescription[i]);
			}
		} catch (SQLException sqle) {
			if (HGlobal.DEBUG) System.out.println("HBNameStyleManager - getAllNameStyleElements(): " + sqle.getMessage());
			if (HGlobal.DEBUG) sqle.printStackTrace();
			throw new HBException("HBE03","HBE03 - HBNameStyleManager - getAllNameStyleElements(): " + sqle.getMessage(),0);
		}
	}

/**
 * setNameStyleCodes(int styleIndex)
 * @param styleIndex
 */
	public void setNameStyleData(int styleIndex) {
		nameStyleIndex = styleIndex;
		nameStyleCodes = nameStyleCodeString[styleIndex].split("\\|");
		if (isTmgNameStyle[styleIndex])
			nameStyleDescript = nameStyleNameString[styleIndex].split("\\|");
		else {
			nameStyleDescript = new String[nameStyleCodes.length];
			for (int i = 0; i < nameStyleCodes.length; i++)
				nameStyleDescript[i] = elementCodeMap.get(nameStyleCodes[i]);
		}
	}

/**
 * setOuputStyleData(int styleIndex)
 * @param styleIndex
 */
	public void setOutputStyleData(int styleIndex) {
		String[] separatorCode;
		String separator, testSeparator;
		outputStyleCodes = outputStyleCodeString[styleIndex].trim().split("\\|");

		if (isTmgOutputStyle[styleIndex]) {
	    // TMG imported output style
			outputStyleDescript = new String[outputStyleCodes.length];
			for (int i = 0; i < outputStyleCodes.length; i++) {
				separator = "";
				if (outputStyleCodes[i].length() > 4) {
					separatorCode = outputStyleCodes[i].split("#");
					separator = separatorCode[1];
					testSeparator = separatorCode[0];
				} else testSeparator = outputStyleCodes[i];

		// Find the element names from style element names
				for (int j = 0; j < nameStyleDescript.length; j++)
					if (testSeparator.equals(nameStyleCodes[j])) {
						outputStyleDescript[i]	= nameStyleDescript[j] + separator;
					}
			}

		} else {

		// HRE generated output style
			outputStyleDescript = new String[outputStyleCodes.length];
			for (int i = 0; i < outputStyleCodes.length; i++) {
				if (outputStyleCodes[i].length() > 4) {
					separatorCode = outputStyleCodes[i].split("#");
					outputStyleDescript[i] = elementCodeMap.get(separatorCode[0]) + separatorCode[1];
				} else outputStyleDescript[i] = elementCodeMap.get(outputStyleCodes[i]);
			}
		}
	}

/**
 * getDefaultStyleIndex()
 * @return
 */
	public int getDefaultStyleIndex() {
		return defaultStyleIndex;
	}

/**
 * loadNameStyles(DefaultListModel<String> elements)
 * @param elements
 */
	public void getNameStyles(DefaultListModel<String> elements) {
		for (String nameStyleName : nameStyleNames) {
			elements.addElement(nameStyleName);
		}
	}

/**
 * getOuputStyles(DefaultListModel<String> elements)
 * @param elements
 */
	public void getOutputStyles(DefaultListModel<String> elements) {
		for (String outputStyleName : outputStyleNames) {
			elements.addElement(outputStyleName);
		}
	}

/**
 * loadNameStyleText()
 * @return
 */
	public String getNameStyleText(int styleIndex) {
		return nameStyleText[styleIndex];
	}

/**
 * getOutputStyleText(int styleIndex)
 * @param styleIndex
 * @return
 */
	public String getOutputStyleText(int styleIndex) {
		return outputStyleText[styleIndex];
	}
/**
 * getChosenElements(DefaultListModel<String> elements)
 * @param elements
 */
	public void getChosenElements(DefaultListModel<String> elements) {
		for (String element : nameStyleDescript)
			elements.addElement(element);
	}

/**
 * getOutputElements(DefaultListModel<String> elements)
 * @param elements
 */
	public void getOutputElements(DefaultListModel<String> elements) {
		for (String element : outputStyleDescript)
			elements.addElement(element);
	}

/**
 * getOutputCodes(DefaultListModel<String> elements)
 * @param elements
 */
	public void getOutputCodes(DefaultListModel<String> elements) {
		for (String outputStyleCode : outputStyleCodes)
			elements.addElement(outputStyleCode);
	}

/**
 *  Load list of ALL Name Style Elements and Element Codes from the T168 record
 * @param codes
 */
	   public void getChosenCodes(DefaultListModel<String> codes) {
		   for (String nameStyleCode : nameStyleCodes)
			codes.addElement(nameStyleCode);
	   }

/**
 * loadAllElements(DefaultListModel<String> elements)
 * @param elements
 */
	   public void getAllElements(DefaultListModel<String> elements) {
		   for (int i = 0; i < nameElementDescription.length; i++) {
			   elements.addElement(nameElementDescription[i]);

		// The T163 is set up with 3 digits (500) and not leading 0 as 0500
			   if (nameElementData[i].length() == 3) nameElementData[i] = "0" + nameElementData[i];
			   if (HGlobal.DEBUG)
				   System.out.println(" " + i + " element "
					   + nameElementData[i] + "/" + nameElementDescription[i]);
		   }
	   }

/**
 * Load list of ALL Name Style Elements and Element Codes  from the T168 record
 */
	   public void getAllCodes(DefaultListModel<String> codes) {
		   for (String element : nameElementData)
			codes.addElement(element);
	   }

/**
 * addNewTableRow(String[] styleData)
 * @param styleData
 * @throws HBException
 */
	   public void addNewTableRow(int styleIndex, boolean addHRE, String nameType, String newCodes) throws HBException {
		   String[] styleData = new String[4];
		   try {
		// Find last PID for table, create Style name
			   long lastPID = lastRowPID(nameStyles, dataBaseIndex);
			   nameStyleTable.absolute(styleIndex+1); // Start row at index = 1
			   if (HGlobal.DEBUG) System.out.println(" NAME_STYLE name length: "
					   + nameStyleTable.getString("NAME_STYLE").trim().length());

			   if (addHRE) styleData[0] = nameStyleTable.getString("NAME_STYLE").trim() + "-COPY";
			   else styleData[0] = "HRE " + nameStyleTable.getString("NAME_STYLE").trim();
		// Truncation of new style name
			   if (styleData[0].length() > 30) {
				   styleData[0] = styleData[0].substring(0,30);
			   }
		// Create Style Description
			   if (addHRE) styleData[1] = nameStyleTable.getString("NAME_STYLE_DESC");
			   else {
				   if (nameType.equals("N")) styleData[1] = "HRE person name style converted from TMG";
				   else if (nameType.equals("P")) styleData[1] = "HRE location name style converted from TMG";
				   else System.out.println("Illegal name type");
			   }
		// If this is a copy, then copy the ElementCodes, else use the newCodes parameter
			   if (addHRE) styleData[2] = nameStyleTable.getString("ELEMNT_CODES");
			   else styleData[2] = newCodes;
			   styleData[3] = "null|";
			   pointLibraryResultSet.addNameStyleToTable(nameStyleTable, lastPID + 1, nameType, styleData);

		// Copy out styles for the new Name Style
			   outputStyleTable.last();
			   int outStyleRows = outputStyleTable.getRow();
			   for (int outIndex = 0; outIndex < outStyleRows; outIndex++) {
				   addNewOutTableRow(outIndex, lastPID + 1, nameType);
			   }

		   } catch (HBException | SQLException hbe) {
			   if (HGlobal.DEBUG) System.out.println("HBNameStyleManager - addNewTableRow(): " + hbe.getMessage());
			   if (HGlobal.DEBUG) hbe.printStackTrace();
			   throw new HBException("HBE04", "Name Style Handling - addNewTableRow(\n" + hbe.getMessage(),0);
		   }
	   }
/**
 * updateNameStyleRPID(long oldRPID, long newRPID, String nameType)
 * @param oldRPID
 * @param newRPID
 * @param nameType
 * @throws HBException
 */
	   public int updateNameStyleRPID(int styleIndex, String nameType) throws HBException {
		   String nameTable = null, selectString;
		   long newRPID, oldRPID;
		   int substitutions = 0;
		   ResultSet nameTableResultSet;
	   // Select person or location name table
		   if (nameType.equals("N")) nameTable = personNameTable;
		   else if (nameType.equals("P")) nameTable = locationNameTable;
		   else System.out.println("Illegal nameType!");
		   try {
		// Find new and old RPID for substitute
			   newRPID = lastRowPID(nameStyles, dataBaseIndex);
			   nameStyleTable.absolute(styleIndex+1); // Start row at index = 1
			   oldRPID = nameStyleTable.getLong("PID");
		// Substitute RPID fir Style in name tables
			   selectString = setSelectSQL("*", nameTable,"");
			   nameTableResultSet = requestTableData(selectString, dataBaseIndex);
			   nameTableResultSet.beforeFirst();
			   while (nameTableResultSet.next()) {
					if (nameTableResultSet.getLong("NAME_STYLE_RPID") == oldRPID) {
						nameTableResultSet.updateLong("NAME_STYLE_RPID", newRPID);
						nameTableResultSet.updateRow();
						substitutions++;
					}
			   }
			   nameTableResultSet.close(); //Release resources
			   return substitutions;
			} catch (SQLException sqle) {
				sqle.printStackTrace();
				throw new HBException("HBE05","HBE05 - updateNameStyleRPID - " + sqle.getMessage(),0);
			}
	   }
/**
 * addNewOutTableRow(int styleIndex, long ownerPID, String nameType)
 * @param styleIndex
 * @param ownerPID
 * @param nameType
 * @throws HBException
 */
	   public void addNewOutTableRow(int styleIndex, long ownerPID, String nameType) throws HBException  {
		   addNewOutStyleRow(styleIndex, ownerPID, nameType,"");
	   }

/**
 * addNewOutTableRow(int styleIndex, String nameType)
 * @param styleIndex
 * @param nameType
 * @throws HBException
 */
	   public void addNewOutTableRow(int styleIndex, String nameType) throws HBException  {
		   long ownerPID = 0;
			try {
				outputStyleTable.absolute(styleIndex+1);
				ownerPID = outputStyleTable.getLong("OWNER_RECORD_RPID");
				addNewOutStyleRow(styleIndex, ownerPID, nameType, "-COPY");
			} catch (SQLException sqle) {
				System.out.println(" NAME_STYLE error - addNewOutTableRow \nType: " + nameType
						+ " - " + sqle.getMessage());
				throw new HBException("HBE06","Name Style Handling - addNewOutTableRow() \n" + sqle.getMessage(),0);
			}
	   }

/**
 * addNewOutStyleRow(int styleIndex, long ownerPID, String nameType)
 * @param styleIndex
 * @param ownerPID
 * @param nameType
 * @throws HBException
 */
	   public void addNewOutStyleRow(int styleIndex, long ownerPID, String nameType, String copy) throws HBException {
		   String[] styleData = new String[4];
		   try {
		// Find last PID for table
			   long lastPID = lastRowPID(nameStylesOutput, dataBaseIndex);
			   outputStyleTable.absolute(styleIndex+1); // Start row at index = 1
			   if (HGlobal.DEBUG)
				   System.out.println(" NAME_STYLE name length: "
						   		+ outputStyleTable.getString("OUT_NAME_STYLE").trim().length());
			   styleData[0] = outType[styleIndex];		  
			   styleData[1] = outputStyleTable.getString("OUT_NAME_STYLE").trim();
			   // Restrict length of new name to 30 chars (including "-COPY")
			   if (styleData[1].length() > 25) styleData[1] = styleData[1].substring(0,25); 
			   styleData[1] = styleData[1] + copy; 
			   styleData[2] = outputStyleTable.getString("OUT_NAME_STYLE_DESC").trim();
			   styleData[3] = outputStyleTable.getString("OUT_ELEMNT_CODES");

			   pointLibraryResultSet.addOutputStyleToTable(outputStyleTable, lastPID + 1, ownerPID, nameType, styleData);
		   } catch (HBException | SQLException hbe) {
			   if (HGlobal.DEBUG) System.out.println("HBNameStyleManager - addNewOutTableRow(): " + hbe.getMessage());
			   if (HGlobal.DEBUG) hbe.printStackTrace();
			   throw new HBException("HBE07","HBE07 - Name Style Handling - addNewOutTableRow() \n" + hbe.getMessage(),0);
		   }
	   }

/**
 * deleteTableRow(int styleIndex)
 * @param styleIndex
 * @throws HBException
 */
   public void deleteNameStyleTableRow(int styleIndex, String nameType) throws HBException {
	   int occursInNameList = 0;
	   ResultSet nameTableResultSet;
	   String tableName = null, selectString;
	   if (getNameStyleIsDefault(styleIndex))
		   throw new HBException("HBE08","HBE08 Default name style cannot be deleted!", 0);
	   try {
		   setNameStyleData(0);
		   nameStyleTable.absolute(styleIndex+1); // Start row at index = 1
		   long oldRPID = nameStyleTable.getLong("PID");

		   if (nameType.equals("N")) tableName = personNameTable;
		   else if (nameType.equals("P")) tableName = locationNameTable;
		   else System.out.println("Illegal nameType!");

	// Check the use of name style in name tables
		   selectString = setSelectSQL("*", tableName,"");
		   nameTableResultSet = requestTableData(selectString, dataBaseIndex);
		   nameTableResultSet.beforeFirst();
		   while (nameTableResultSet.next())
			   if (nameTableResultSet.getLong("NAME_STYLE_RPID") == oldRPID) occursInNameList++;
		   if (occursInNameList > 0) throw new HBException("HBE09", "HBE09 Name style used ", occursInNameList);

		   pointLibraryResultSet.deleteNameStyleRow(nameStyleTable, styleIndex);

	// Delete corresponding out styles owned by Name Style
		   outputStyleTable.last();
		   int outStyleRows = outputStyleTable.getRow();
		   for (int outIndex = 0; outIndex < outStyleRows; outIndex++)
			   pointLibraryResultSet.deleteNameStyleRow(outputStyleTable, outIndex);

		   nameTableResultSet.close(); //Release resources
		} catch (SQLException sqle) {
			if (HGlobal.DEBUG) System.out.println("HBNameStyleManager - deleteNameStyleRow(): " + sqle.getMessage());
			if (HGlobal.DEBUG) sqle.printStackTrace();
			throw new HBException("HBE10","Name Style Handling - deleteNameStyleRow()"  + sqle.getMessage(), 0);
		}
   }

/**
 * deleteOutputStyleRow(int styleIndex, String nameType)
 * @param styleIndex
 * @param nameType
 * @throws HBException
 */
   public void deleteOutputStyleTableRow(int styleIndex, String nameType) throws HBException {
	// Delete single output Style
	   pointLibraryResultSet.deleteNameStyleRow(outputStyleTable, styleIndex);
   }

/**
 * updateDefaultStyle()
 * @throws HBException
 */
   public void updateDefaultStyle() throws HBException {
	   int index = 0;
	   try {
		   nameStyleTable.beforeFirst();
		   while(nameStyleTable.next()) {
			   nameStyleTable.updateBoolean("IS_DEFAULT", isDefaultNameStyle[index]);
			   nameStyleTable.updateRow();
			   index++;
		   }
		   nameStyleTable.beforeFirst();
		} catch (SQLException sqle) {
			System.out.println(" DEFAULT update error index: " + index + "/" + sqle.getMessage());
			throw new HBException("HBE11","Name Style Handling - updateDefaultStyle()\n" + sqle.getMessage(),0);
		}
   }

/**
 * updateStyleName(int styleIndex, String newStyleName, String styleDescription)
 * @param styleIndex
 * @param newStyleName
 * @param styleDescription
 * @throws HBException
 */
   public void updateStyleName(int styleIndex, String newStyleName, String styleDescription) throws HBException {
	   String[] styleData = new String[4];
	   try {
		   nameStyleTable.absolute(styleIndex+1);
		   styleData[0] = newStyleName;
		   styleData[1] = styleDescription;
		   styleData[2] = nameStyleTable.getString("ELEMNT_NAMES");
		   styleData[3] = nameStyleTable.getString("ELEMNT_CODES");
		   pointLibraryResultSet.updateNameStyleTable(nameStyleTable, styleData);
	   } catch (SQLException | HBException hbe) {
		   if (HGlobal.DEBUG) System.out.println("HBNameStyleManager - updateStyleName(): " + hbe.getMessage());
		   if (HGlobal.DEBUG) hbe.printStackTrace();
		   throw new HBException("HBE12", "Name Style Handling - updateStyleName\n" + hbe.getMessage(),0);
	   } // Start row at index = 1
   }



   public void updateOutputStyleName(int styleIndex, String newStyleName, String styleDescription) throws HBException {
	   String[] styleData = new String[4];
	   try {
		   outputStyleTable.absolute(styleIndex+1);
		   long ownerPID = outputStyleTable.getLong("OWNER_RECORD_RPID");
	   		styleData[0] = outType[styleIndex];
	   		styleData[1] = newStyleName.trim();
			styleData[2] = styleDescription.trim();
			styleData[3] = outputStyleTable.getString("OUT_ELEMNT_CODES");
		    pointLibraryResultSet.updateOutputStyleTable(outputStyleTable, ownerPID, styleData);
	   } catch (SQLException | HBException hbe) {
		   if (HGlobal.DEBUG) System.out.println("HBNameStyleManager - updateStyleName(): " + hbe.getMessage());
		   if (HGlobal.DEBUG) hbe.printStackTrace();
		   throw new HBException("HBE13","Name Style Handling - updateOutputStyleName\n" + hbe.getMessage(),0);
	   } // Start row at index = 1
   }

/**
 * updateNameStyleData(int styleIndex, DefaultListModel<String> newCodes)
 * @param styleIndex
 * @param newCodes
 * @throws HBException
 */
   public void updateNameStyleData(int styleIndex,
		   					          DefaultListModel<String> newDescriot,
		   					          DefaultListModel<String> newCodes) throws HBException {

	   String[] styleData = new String[4];
	   try {
	   		nameStyleTable.absolute(styleIndex+1);
	   		styleData[0] = nameStyleTable.getString("NAME_STYLE").trim();
			styleData[1] = nameStyleTable.getString("NAME_STYLE_DESC").trim();
			String elementNames = nameStyleTable.getString("ELEMNT_NAMES");
			if (!nameStyleTable.getBoolean("IS_TMG")) styleData[2] = elementNames;
			else styleData[2] = generateElementString(newDescriot);
			styleData[3] = generateElementString(newCodes);
			pointLibraryResultSet.updateNameStyleTable(nameStyleTable, styleData);
		} catch (SQLException | HBException hbe) {
			if (HGlobal.DEBUG) System.out.println("HBNameStyleManager - updateNameStyleData(): " + hbe.getMessage());
			if (HGlobal.DEBUG) hbe.printStackTrace();
			throw new HBException("HBE14", "Name Style Handling - updateNameStyleData\n" + hbe.getMessage(),0);
		}
   }

/**
 * updateOutputStyleData(int styleIndex,  DefaultListModel<String> newCodes)
 * @param styleIndex
 * @param newCodes
 * @throws HBException
 */
  public void updateOutputStyleData(int styleIndex,  DefaultListModel<String> newCodes) throws HBException {

	   String[] styleData = new String[4];
	   long ownerPID;
	   try {
		    outputStyleTable.absolute(styleIndex+1);
		    ownerPID = outputStyleTable.getLong("OWNER_RECORD_RPID");
	   		styleData[0] = outType[styleIndex];
	   		styleData[1] = outputStyleTable.getString("OUT_NAME_STYLE").trim();
			styleData[2] = outputStyleTable.getString("OUT_NAME_STYLE_DESC").trim();
			styleData[3] = generateElementString(newCodes);
			pointLibraryResultSet.updateOutputStyleTable(outputStyleTable,  ownerPID, styleData);
		} catch (SQLException | HBException hbe) {
			if (HGlobal.DEBUG) System.out.println("HBNameStyleManager - updateOutputStyleData(): " + hbe.getMessage());
			if (HGlobal.DEBUG) hbe.printStackTrace();
			throw new HBException("HBE15","HBE15 - Name Style Handling - updateOutputStyleData\n" + hbe.getMessage(),0);
		}
   }

/**
 * updateAllElementTable
 * @param newDescript
 * @param newCodes
 * @throws HBException
 */
   public void updateAllElementTable(String nameType, DefaultListModel<String> newDescript,
		   								DefaultListModel<String> newCodes) throws HBException {

	   try {
			nameStyleElements = pointLibraryResultSet.getNameStyleElementTable(nameType, nameElementTableName, dataBaseIndex);
			nameStyleElements.last();
			if (HGlobal.DEBUG) {
				nameStyleElements.last();
				System.out.println(" Name element rows: " + nameStyleElements.getRow());
			}
			nameStyleElements.beforeFirst();
			while (nameStyleElements.next()) {
				if (nameStyleElements.getString("LANG_CODE").trim().equals(dataLanguage.trim())) {
					String dataString = generateElementString(newDescript);
					String codeString = generateElementString(newCodes);
					if (HGlobal.DEBUG) System.out.println(" updateAllElementTable - datalang: " + nameStyleElements.getString("LANG_CODE"));
					pointLibraryResultSet.updateElementList(nameStyleElements, dataString, codeString);
				} else {
					String newElementDescriptionString = "";
					String newElementCodeString = "";
					String nameString = nameStyleElements.getString("ELEMNT_NAMES");
					String codeString = nameStyleElements.getString("ELEMNT_CODES");
					String[] oldDescript = nameString.split("\\|");
					String[] oldCodes = codeString.split("\\|");
					for (int i = 0; i < oldCodes.length; i++)
						if (oldCodes[i].length() == 3) oldCodes[i] = "0" + oldCodes[i];
					int index = 0;
					for (int i = 0; i < newCodes.size(); i++) {
						String elelementCode = newCodes.get(i);
						String elementDescript = newDescript.get(i);
						if (index >= oldCodes.length) {
							newElementDescriptionString = newElementDescriptionString + elementDescript+ "(" + dataLanguage + ")" + "|";
							newElementCodeString = newElementCodeString + elelementCode + "|";
						} else
						if (elelementCode.equals(oldCodes[index])) {
							newElementDescriptionString = newElementDescriptionString + oldDescript[index] + "|";
							newElementCodeString = newElementCodeString + oldCodes[index] + "|";
							index++;
						} else {
							newElementDescriptionString = newElementDescriptionString + elementDescript+ "(" + dataLanguage + ")" + "|";
							newElementCodeString = newElementCodeString + elelementCode + "|";
						}
					}
					if (HGlobal.DEBUG)
						System.out.println(" New Descript: " + newElementDescriptionString);
					if (HGlobal.DEBUG)
						System.out.println(" New Codes: " + newElementCodeString);

					pointLibraryResultSet.updateElementList(nameStyleElements, newElementDescriptionString, newElementCodeString);
				}
			}
		} catch (HBException | SQLException hbe) {
			if (HGlobal.DEBUG) System.out.println("HBNameStyleManager - updateAllElementTable(): " + hbe.getMessage());
			if (HGlobal.DEBUG) hbe.printStackTrace();
			throw new HBException("HBE16","Name Style Handling - updateAllElementTable\n" + hbe.getMessage(),0);
		}
   }

/**
 * updateNameElementDescription(DefaultListModel<String> newDescript)
 * @param newDescript
 * @throws HBException
 */
   public void updateNameElementDescription(String nameType, DefaultListModel<String> newDescript) throws HBException {
		try {
			nameStyleElements = pointLibraryResultSet.getNameStyleElements(nameElementTableName, nameType, dataLanguage, dataBaseIndex);
			nameStyleElements.first();
			pointLibraryResultSet.updateElementName(nameStyleElements, generateElementString(newDescript));

		} catch (HBException | SQLException hbe) {
			if (HGlobal.DEBUG) System.out.println("HBNameStyleManager - updateNameElementDescription(): " + hbe.getMessage());
			if (HGlobal.DEBUG) hbe.printStackTrace();
			throw new HBException("HBE17","Name Style Handling - updateNameElementDescription\n" + hbe.getMessage(),0);
		}
   }

/**
 * generateElementCodeString(DefaultListModel<String> elements)
 * @param elements
 * @return
 */
	public String generateElementString(DefaultListModel<String> elements) {
		String codeString = "";
		for (int i = 0; i < elements.getSize(); i++)
			codeString = codeString + elements.get(i) + "|";
		return codeString;
	}

/*******************************************
 * Methods for add person name elements
 ************************************************
 */

/**
* getNameStyleList() for addperson
* @return String[]
*/
	public String[] getNameStyleList() {
		return nameStyleNames;
	}

/**
 * createPersonNameTable(int selectedStyleIndex)
 * @param selectedStyleIndex
 * @throws HBException
 */
	public String[][] createNameStyleTable(int selectedStyleIndex) {
		String codeString, nameString;
		String[] elementCodes, elementNames;
		String[][] nameStyleTable;
		if (isTmgNameStyle[selectedStyleIndex]) {
			nameString = nameStyleNameString[selectedStyleIndex];
			elementNames = nameString.split("\\|");
			codeString = nameStyleCodeString[selectedStyleIndex];
			elementCodes = codeString.split("\\|");
			nameStyleTable = new String[elementNames.length][3];
			for (int i = 0; i < elementNames.length; i++) {
				nameStyleTable[i][0] = elementNames[i];
				nameStyleTable[i][2] = elementCodes[i]; // Store code for future use/Not shown
			}
		} else {
			codeString = nameStyleCodeString[selectedStyleIndex];
			elementCodes = codeString.split("\\|");
			nameStyleTable = new String[elementCodes.length][3];
			for (int i = 0; i < elementCodes.length; i++) {
				nameStyleTable[i][0] = elementCodeMap.get(elementCodes[i]);
				nameStyleTable[i][2] = elementCodes[i]; // Store code for future use/Not shown
			}
		}
		return nameStyleTable;
	}
//****************************************************************************************

/**
 * updateNameElementCodes(String nameType long nameStylePID, String oldNameCode, String newNameCode)
 * @param nameType
 * @param nameStylePID
 * @param oldNameCode
 * @param newNameCode
 */
	public void updateNameElementCodes(String nameType, long nameStylePID, String oldNameCode, String newNameCode) {
		System.out.println(" Update codes Type: "+nameType+ " PID: " + nameStylePID + " Code " + oldNameCode + " to " + newNameCode);
		ResultSet nameRecords, nameElementRecord;
		long nameTablePID;
		int index = 0;
		String selectString = null;
		try {
			if (nameType.equals("N"))
				selectString = setSelectSQL("*", personNameTable, "NAME_STYLE_RPID = " + nameStylePID);
			else if (nameType.equals("P"))
					selectString = setSelectSQL("*", locationNameTable, "NAME_STYLE_RPID = " + nameStylePID);
			else System.out.println("updateNameElementCodes name type error? " + nameType);
			nameRecords = requestTableData(selectString, dataBaseIndex);
			nameRecords.last();
			//System.out.println(" Name rows: " + nameRecords.getRow());
			nameRecords.beforeFirst();
			while (nameRecords.next()) {
				nameTablePID = nameRecords.getLong("PID");
				if (nameType.equals("N"))
					selectString = setSelectSQL("*", personNamesTableElements, "OWNER_RPID = "
												+ nameTablePID + " AND ELEMNT_CODE = " + oldNameCode);
				else if (nameType.equals("P"))
					selectString = setSelectSQL("*", locationNameElementTable, "OWNER_RPID = "
												+ nameTablePID + " AND ELEMNT_CODE = " + oldNameCode);
				else System.out.println("updateNameElementCodes name type error? " + nameType);
				nameElementRecord = requestTableData(selectString, dataBaseIndex);
				if (!isResultSetEmpty(nameElementRecord)) {
					nameElementRecord.first();
					nameElementRecord.updateString("ELEMNT_CODE", newNameCode);
					nameElementRecord.updateRow();
					index++;
				}
			}
			System.out.println(" Substituted type: " + type + " - "+ index + " times!");
		} catch (HBException | SQLException hbe) {
			// TODO Auto-generated catch block
			hbe.printStackTrace();
		}
	}
} // End class HBNameStyleManager


