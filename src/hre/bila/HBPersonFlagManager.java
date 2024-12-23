package hre.bila;
/****************************************************************************
 * Class for handling of personflags
 *
 ***************************************************************************
 * v0.01.0030 2023-06-03 - Activated flag processing (N. Tolleshaug)
 *
 ****************************************************************************/
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import hre.dbla.HDDatabaseLayer;
import hre.gui.HGlobal;


/**
 * class ManagePersonFlags extends HBBusinessLayer
 * @author NTo
 * @version v0.01.0030
 * @since 2023.05.30
 */
public class HBPersonFlagManager extends HBBusinessLayer {
	int dataBaseIndex;
	Object[][] tableFlagData;
	String[] tableFlagDescript;
	ResultSet personFlagDefinition, personFlagTranslation;
	HashMap<Integer,Long> flagDefinMapPID;

/**
 * getPersonFlagTable()
 * @return Object[][]
 */
	public Object[][] getPersonFlagTable() {
		return tableFlagData;
	}

/**
 * getManageFlagDescript()
 * @return String[]
 */
	public String[] getManageFlagDescript() {
		return tableFlagDescript;

	}

	public String returnFlagLangName(String languageCode) throws HBException {
		return getFlagLangCode(languageCode);
	}

/**
 * updateFlagDescription()
 * @throws HBException
 */
	public void updateFlagDescription() throws HBException {
		if (HGlobal.DEBUG) {
			System.out.println(" Update flag data: ");
			for (int i = 0; i < tableFlagData.length; i++) {
				System.out.println(" " + i + " - "
					+ (String)tableFlagData[i][6] + " / "
					+ (int)tableFlagData[i][3] + " / "
					+ (String)tableFlagData[i][7] + " / "
					+ (String)tableFlagData[i][8]);
			}
		}
		updateFlagTableData();
	}

/**
 * ManagePersonFlag constructor
 * @param pointDBlayer
 * @param dataBaseIndex
 * @param pointOpenProject
 * @throws HBException
 */
	public HBPersonFlagManager(HDDatabaseLayer pointDBlayer, int dataBaseIndex) throws HBException {
		super();
		this.pointDBlayer = pointDBlayer;
		this.dataBaseIndex = dataBaseIndex;
		processFlagTable();
	}

/**
 * processFlagTable()
 * @throws HBException
 * From T251_FLAG_DEFN:
 	0 - IS_SYSTEM
	1 - ACTIVE
	2 - GUI_SEQ
	3 - FLAG_IDENT
	4 - DEFAULT_INDEX ?

	From T204_FLAG_TRAN:
	5 - LANG_CODE
	6 - FLAG_NAME
	7 - FLAG_VALUES
	8 - FLAG_DESC
 */
	public void processFlagTable() throws HBException {
		String langCode = HGlobal.dataLanguage, selectString;
		int index = 0, flagIdent, flagRows = 0;
		//ResultSet personFlagDefinition, personFlagTranslation;
		flagDefinMapPID = new HashMap<>() ;
		selectString = setSelectSQL("*", flagDefinition,"");
		personFlagDefinition = requestTableData(selectString, dataBaseIndex);
		try {
			personFlagDefinition.last();
			flagRows = personFlagDefinition.getRow();
			tableFlagData = new Object[flagRows][9];
			tableFlagDescript = new String[flagRows];
			personFlagDefinition.beforeFirst();
			while (personFlagDefinition.next()) {
				flagIdent = personFlagDefinition.getInt("FLAG_IDENT");
				flagDefinMapPID.put(flagIdent, personFlagDefinition.getLong("PID"));
			// Get translated flag data
				selectString = setSelectSQL("*", translatedFlag, "(LANG_CODE = '" + langCode + "'"
												 + " OR FLAG_IDENT > 7)"
												 + " AND FLAG_IDENT = " + flagIdent);
				personFlagTranslation = requestTableData(selectString, dataBaseIndex);
				personFlagTranslation.first();

				if (HGlobal.DEBUG) {
					String flagValue = personFlagTranslation.getString("FLAG_VALUES");
					String flagDescript = personFlagTranslation.getString("FLAG_DESC");
					String flagName = personFlagTranslation.getString("FLAG_NAME");
					System.out.print( "Index: " + index + " / " + flagIdent);
					System.out.print( " Flag name: " + flagName);
					System.out.print( " /value: " + flagValue);
					System.out.println( " /descript: " + flagDescript);
				}

				tableFlagData[index][0] = personFlagDefinition.getBoolean("IS_SYSTEM");
				tableFlagData[index][1] = personFlagDefinition.getBoolean("ACTIVE");
				tableFlagData[index][2] = personFlagDefinition.getInt("GUI_SEQ");
				tableFlagData[index][3] = flagIdent;
				tableFlagData[index][4] = personFlagDefinition.getInt("DEFAULT_INDEX");
				tableFlagData[index][5] = getFlagLangCode(personFlagTranslation.getString("LANG_CODE"));
				tableFlagData[index][6] = personFlagTranslation.getString("FLAG_NAME").trim();
				tableFlagData[index][7] = personFlagTranslation.getString("FLAG_VALUES").trim();
				tableFlagData[index][8] = personFlagTranslation.getString("FLAG_DESC").trim();
				index++;
			}
		} catch (SQLException sqle) {
			System.out.println(" HBPersonHnadler - perocessFlagTable(): " + sqle.getMessage());
			throw new HBException(" HBPersonHnadler - perocessFlagTable(): " + sqle.getMessage());
		}
	}

/**
 * addFlagDescriptTrans(Object[] addFlagDescriptData)
 * @param flagIdent
 * @param guiSeq
 * @param flagLang
 * @throws HBException
 */
	public void addFlagDescriptTrans(Object[] addFlagDescriptData) throws HBException {
		ResultSet flagDefinitionSet, flagTranslationSet;
		int defaultIndex = 0, flagIdent = 0, guiSeq = 0;
		long lastFlagDefPID = lastRowPID(flagDefinition, dataBaseIndex);
		String selectString = setSelectSQL("*", flagDefinition,"");
		try {
			flagDefinitionSet = requestTableData(selectString, dataBaseIndex);
		// Find largest value for FLAG_IDENT and GUI_SEQ
			flagDefinitionSet.beforeFirst();
			while (flagDefinitionSet.next()) {
				if (flagDefinitionSet.getInt("GUI_SEQ") > guiSeq) {
					guiSeq = flagDefinitionSet.getInt("GUI_SEQ");
				}
				if (flagDefinitionSet.getInt("FLAG_IDENT") > flagIdent) {
					flagIdent = flagDefinitionSet.getInt("FLAG_IDENT");
				}
			}

		// add to T251_FLAG_DEFN
		// moves cursor to the insert row
			flagDefinitionSet.moveToInsertRow();
		// Update new row in H2 database
			flagDefinitionSet.updateLong("PID", lastFlagDefPID + 1);
			flagDefinitionSet.updateLong("CL_COMMIT_RPID", null_RPID);
			flagDefinitionSet.updateBoolean("IS_SYSTEM",  (boolean) addFlagDescriptData[1]);
			flagDefinitionSet.updateBoolean("INHERIT",false);
			flagDefinitionSet.updateBoolean("ACTIVE", (boolean) addFlagDescriptData[3]);
			flagDefinitionSet.updateLong("BASE_TYPE_RPID", proOffset + 1);
			flagDefinitionSet.updateInt("GUI_SEQ", guiSeq + 1);
			flagDefinitionSet.updateInt("FLAG_IDENT", flagIdent + 1);
			flagDefinitionSet.updateInt("DEFAULT_INDEX", defaultIndex);

		//Insert row in database
			flagDefinitionSet.insertRow();

	// Add to T204_FLAG_TRAN
			long lastFlagTranPID = lastRowPID(translatedFlag, dataBaseIndex);
			selectString = setSelectSQL("*", translatedFlag,"");
			flagTranslationSet = requestTableData(selectString, dataBaseIndex);
		// moves cursor to the insert row
			flagTranslationSet.moveToInsertRow();
		// Update new row in H2 database
			flagTranslationSet.updateLong("PID", lastFlagTranPID + 1);
			flagTranslationSet.updateLong("CL_COMMIT_RPID", null_RPID);
			flagTranslationSet.updateBoolean("IS_SYSTEM", (boolean) addFlagDescriptData[1]);
			flagTranslationSet.updateInt("FLAG_IDENT", flagIdent + 1);

			flagTranslationSet.updateString("LANG_CODE", HGlobal.dataLanguage);
			//flagTranslationSet.updateString("LANG_CODE", (String) addFlagDescriptData[2]);

			flagTranslationSet.updateString("FLAG_NAME", (String) addFlagDescriptData[0]);
			flagTranslationSet.updateString("FLAG_VALUES", (String) addFlagDescriptData[4]);
			flagTranslationSet.updateString("FLAG_DESC", (String) addFlagDescriptData[5]);
		//Insert row in database
			flagTranslationSet.insertRow();
		} catch (SQLException sqle) {
			if (HGlobal.DEBUG) {
				System.out.println("HBPersonHandler - addFlagDescriptTrans(): " + sqle.getMessage());
			}
			sqle.printStackTrace();
			throw new HBException("HBPersonHandler - addFlagDescriptTrans(): " + sqle.getMessage());
		}
	}

/**
 * public void deleteFlagDescriprion(int flagIdent)
 * @param flagIdent
 * @throws HBException
 */
	public void deleteFlagDescriprion(int flagIdent) throws HBException {
		ResultSet flagDefinitionSet, flagTranslationSet;
		String selectString = "";
		try {
		// Delete description
			selectString = setSelectSQL("*", flagDefinition, "FLAG_IDENT = " + flagIdent);
			flagDefinitionSet = requestTableData(selectString, dataBaseIndex);
			flagDefinitionSet.first();
			flagDefinitionSet.deleteRow();

		// Delete translation
			selectString = setSelectSQL("*", translatedFlag, "FLAG_IDENT = " + flagIdent);
			flagTranslationSet = requestTableData(selectString, dataBaseIndex);
			flagTranslationSet.first();
			flagTranslationSet.deleteRow();
		} catch (SQLException sqle) {
			if (HGlobal.DEBUG) {
				System.out.println("HBPersonHandler - deleteFlagDescription(): " + sqle.getMessage());
			}
			//sqle.printStackTrace();
			throw new HBException("HBPersonHandler - deleteFlagDescription(): " + sqle.getMessage());
		}
	}

/**
 * updateFlagTableData()
 * @param flagIdent
 * @throws HBException
 *  From T251_FLAG_DEFN:
 	0 - IS_SYSTEM
	1 - ACTIVE
	2 - GUI_SEQ
	3 - FLAG_IDENT
	4 - DEFAULT_INDEX ?

	From T204_FLAG_TRAN:
	5 - LANG_CODE
	6 - FLAG_NAME
	7 - FLAG_VALUES
	8 - FLAG_DESC
 */

	public void updateFlagTableData() throws HBException {
		int index = 0, defaultIndex = 0;
		String langCode = HGlobal.dataLanguage, selectString;
		ResultSet flagDefinitionSet, flagTranslationSet;
		selectString = setSelectSQL("*", flagDefinition, "");
		try {
			flagDefinitionSet = requestTableData(selectString, dataBaseIndex);
			flagDefinitionSet.beforeFirst();
			while (flagDefinitionSet.next()) {
				int flagIdent = (int) tableFlagData[index][3];
				flagDefinitionSet.updateBoolean("IS_SYSTEM", (boolean) tableFlagData[index][0]);
				flagDefinitionSet.updateBoolean("ACTIVE", (boolean) tableFlagData[index][1]);
				flagDefinitionSet.updateLong("BASE_TYPE_RPID", proOffset + 1);
				flagDefinitionSet.updateInt("GUI_SEQ", (int) tableFlagData[index][2]);
				flagDefinitionSet.updateInt("FLAG_IDENT", (int) tableFlagData[index][3]);
				flagDefinitionSet.updateInt("DEFAULT_INDEX", defaultIndex);
				flagDefinitionSet.updateRow();

				selectString = setSelectSQL("*", translatedFlag, "(LANG_CODE = '" + langCode + "'"
						 + " OR FLAG_IDENT > 7)"
						 + " AND FLAG_IDENT = " + flagIdent);

				flagTranslationSet = requestTableData(selectString, dataBaseIndex);
				flagTranslationSet.first();
				// Update language ???
				//flagTranslationSet.updateString("LANG_CODE", (String) tableFlagData[index][5]);
				flagTranslationSet.updateInt("FLAG_IDENT", flagIdent);
				flagTranslationSet.updateString("FLAG_NAME", (String) tableFlagData[index][6]);
				flagTranslationSet.updateString("FLAG_VALUES", (String) tableFlagData[index][7]);
				flagTranslationSet.updateString("FLAG_DESC", (String) tableFlagData[index][8]);
				flagTranslationSet.updateRow();
				index++;
			}
		} catch (SQLException sqle) {
			if (HGlobal.DEBUG) {
				System.out.println("HBPersonHandler - updateFlagTableData(): " + sqle.getMessage());
			}
			sqle.printStackTrace();
			throw new HBException("HBPersonHandler - aupdateFlagTableData(): " + sqle.getMessage());
		}
	}

/**
 * getFlagLangCode(String languageCode)
 * @param languageCode
 * @return
 * @throws HBException
 */
	private String getFlagLangCode(String languageCode) throws HBException {
		int index = 0;
		String language = "English";
		String dataLangCode = HGlobal.dataLanguage;
		String selectString = setSelectSQL("*", translatedLang, "LANG_CODE = '" + languageCode + "'");
		ResultSet languageTranslation = requestTableData(selectString, dataBaseIndex);
		try {
			languageTranslation.first();
			String  translationsString = languageTranslation.getString("ALL_TRAN_LIST");
			String[] tranlationCodes = translationsString.split("\\|");
			for (int i = 0; i < tranlationCodes.length; i++) {
				if (dataLangCode.equals(tranlationCodes[index])) {
					language = tranlationCodes[index + 1];
					break;
				}
				index = index + 2;
			}
			return language;
		} catch (SQLException sqle) {
			System.out.println(" ManagePersonFlag - getFlagLangCode(): " + sqle.getMessage());
			throw new HBException(" ManagePersonFlag - getFlagLangCode(): " + sqle.getMessage());
		}
	}
} // End PersonFlagManager


