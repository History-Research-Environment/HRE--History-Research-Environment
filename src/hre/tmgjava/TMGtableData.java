package hre.tmgjava;


import java.util.ArrayList;
/***************************************************************************************
 * Uses library com.linuxense.javadbf
 * Java library for reading and writing Xbase (dBase/DBF) files
 * https://github.com/albfernandez/javadbf
 * albfernandez/javadbf is licensed under the
 * GNU Lesser General Public License v3.0
 * Written by Alberto Fern�ndez
 * ************************************************************************************
 * TMGtableData
 * v0.00.0018 2020-03-05 - First version (N. Tolleshaug)
 * v0.00.0018 2020-03-25 - Changed to ArrayList for getMethods (N. Tolleshaug)
 * 						   HashMap used for findMethods (N. Tolleshaug)
 * v0.00.0018 2020-03-26 - Exception handling in get and find methods
 * v0.00.0027 2022-01-29 - Added methods getValueBoolean(N. Tolleshaug)
 * 			  2022-05-16 - Progress bar implemented for TMG file load (N. Tolleshaug)
 * ************************************************************************************
 * NOTE 1
 * The HashMap for access to PID_Number handles only the first occurrence of the ID_Numner
 * Introduced a Vector for multiple occurrence of PID's in the table
 *****************************************************************************************/
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFRow;

import hre.bila.HB0711Logging;

/**
 * class TMGtableData contains the data from a TMG table
 * ArrayList with index referring to each row in the TMG database - used by getMethods
 * HashMap for accessing a row from the PID of the TMG table - used by findfMethods
 * Methods for access to TMG data field.
 * @author NTo
 * @since 2020-02-05
 */
public class TMGtableData {

	private HashMap<Integer, DBFRow> PIDmap = new HashMap<>();
	private HashMap<Integer,Vector<DBFRow>> multiPidMap = new HashMap<>();
	private ArrayList<DBFRow> PIDlist = new ArrayList<>();
	private int nrOFRows,rowMultiRows;
	DBFReader dbfReader;
	String keyColumnName;

	TMGHREconverter pointConvert;

/**
 * Constructor convert TMG table
 * Set up HashMap for storing reference to rows in TMG table - Retrieve row by HashMap index
 * add row to ArrayList to retrieve DBFrow object by numerical index.
 * @param dbfReader - reader of TMG database file
 */

	public TMGtableData(DBFReader dbfReader,String keyColumnName, TMGHREconverter pointConvert) {
		this.dbfReader = dbfReader;
		this.keyColumnName = keyColumnName;
		this.pointConvert = pointConvert;
	}

/**
 * closeTMGtable()
 */
	public void closeTMGtable() {
		try {
			dbfReader.close();
		 } catch (Exception ex) {
			System.out.println("TMG close exception " + ex.getMessage());
		}
	}

/**
 * reportProgress(int completed, int nrOfTables )
 * @param completed
 * @param nrOfTables
 */
	private void reportProgress(int completed, int nrOfTables ) {
		// Report progress in %
		int progress = (int)Math.round(((double)completed / (double)nrOfTables) * 100);
		pointConvert.setStatusProgress(progress);
	}

/**
 * TMGtableSingleData(DBFReader dbfReader,String headerPID)
 * @param dbfReader
 * @param keyColumnName
 * @throws HCException
 */
	public void TMGtableSingleData(DBFReader dbfReader) throws HCException {
		TMGtableSingleData(dbfReader, true);
	}

	public void TMGtableSingleData(DBFReader dbfReader, boolean dsid) throws HCException {

		DBFRow row;
		int rowIndex = 0;
		int numberRows = dbfReader.getRecordCount();
		try {
			while ((row = dbfReader.nextRow()) != null) {
				if (dsid) {
					if (row.getInt("DSID") == TMGglobal.dataSetID || row.getInt("DSID") == 0) {
						PIDlist.add(row);
						int mapKey = row.getInt(keyColumnName);
						if (!PIDmap.containsKey(mapKey))
							PIDmap.put(mapKey, row);
						rowIndex++;
					}
				} else {
					PIDlist.add(row);
					int mapKey = row.getInt(keyColumnName);
					if (!PIDmap.containsKey(mapKey))
						PIDmap.put(mapKey,row);
					rowIndex++;
				}
				reportProgress(rowIndex, numberRows );
			}
			nrOFRows = rowIndex;
		} catch (DBFException dbfe) {
			System.out.println("TMGtableData/TMGtableSingleData/DBFException: " + dbfe.getMessage());
			dbfe.printStackTrace();
			throw new HCException("TMGtableData/TMGtableSingleData/DBFException: " + dbfe.getMessage());
		}
	}

/**
 * Set up HashMap for storing reference to rows in TMG table - Retrieve row by HashMap index
 * add row to ArrayList to retrieve DBFrow object by numerical index.
 * Multiple PID indexes stored in a Vector list
 * @param dbfReader
 * @param keyColumnName
 * @throws HCException
 */
	public void TMGtableMultiData(DBFReader dbfReader) throws HCException {
		DBFRow row;
		Vector<DBFRow> pidMultiSet;
		int rowIndex = 0;
		int rowMultiIndex = 0;
		int numberRows = dbfReader.getRecordCount();
		try {
			while ((row = dbfReader.nextRow()) != null) {
				if (row.getInt("DSID") == TMGglobal.dataSetID || row.getInt("DSID") == 0) {
					PIDlist.add(row);
				// Set up multiple entries in HashMap
					int mapKey = row.getInt(keyColumnName);
					if (multiPidMap.containsKey(mapKey)) {
						multiPidMap.get(mapKey).add(row);
					} else {
						pidMultiSet = new Vector<>(6);
						pidMultiSet.add(row);
						multiPidMap.put(mapKey,pidMultiSet);
						rowMultiIndex++;
					}
					rowIndex++;
				}
				reportProgress(rowIndex, numberRows );
			}
			rowMultiRows = rowMultiIndex;
			nrOFRows = rowIndex;
		} catch (DBFException dbfe) {
			System.out.println("TMGtableData/TMGtableMultiData/DBFException: " + dbfe.getMessage());
			dbfe.printStackTrace();
			throw new HCException("TMGtableData/TMGtableMultiData/DBFException: " + dbfe.getMessage());
		}
	}

/**
 * getTableRow()
 * @return
 * @throws HCException
 */
	public DBFRow TMGreadTableRow() throws HCException {
		DBFRow row;
		try {
			row = dbfReader.nextRow();
			return row;
		} catch (DBFException dbfe) {
			System.out.println("TMGtableData/readDBFrow/DBFException: " + dbfe.getMessage());
			dbfe.printStackTrace();
			throw new HCException("TMGtableData/readDBFrow/DBFException: " + dbfe.getMessage());
		}
	}


/**
 *
 * @param dbfReader
 * @throws HCException
 */
	public void writeDbfObjects(DBFReader dbfReader) throws HCException {
		Object[] rowObjects;
		try {
			while ((rowObjects = dbfReader.nextRecord()) != null) {

				for (Object rowObject : rowObjects) {
					System.out.print(rowObject + " / ");
				}
				System.out.println();
			}
		} catch (DBFException dbfe) {
			System.out.println("TMGtableData/writeDbfObjects/DBFException: " + dbfe.getMessage());
			dbfe.printStackTrace();
			throw new HCException("TMGtableData/writeDbfObjects/DBFException: " + dbfe.getMessage());
		}
	}

/**
 * getByteContent - returns the byte content of MEMO field
 * @param listIndex
 * @param fieldName
 * @return byte[]
 * @throws HCException
 */
	public byte[] getByteContent(int listIndex, String fieldName) throws HCException {
		try {
			if (PIDlist.get(listIndex) == null) {
				if (TMGglobal.DEBUG)
					System.out.println("TMGtableData/Arraylist Error Key: "
							+ listIndex + " - FieldName: " + fieldName);
				throw new HCException("TMGtableData/Arraylist get Bytes Error Key: "
							+ listIndex + " - FieldName: " + fieldName);
			}
			//System.out.println("\ngetByteContent: " + listIndex + " / " + fieldName);
			return PIDlist.get(listIndex).getBytes(fieldName);
		} catch (DBFException dbfe) {
			System.out.println("TMGtableData/getByteContent/DBFException: " + dbfe.getMessage());
			dbfe.printStackTrace();
			throw new HCException("TMGtableData/getByteContent/DBFException: " + dbfe.getMessage());
		}
	}

/**
 * getValueString - get data from rows indexed by ArrayList and Field Name in row
 * @param PID
 * @param fieldName
 * @return
 * @throws HCException
 */
	public String getValueString(int listIndex, String fieldName) throws HCException {
		try {
			if (PIDlist.get(listIndex) == null) {
				if (TMGglobal.DEBUG)
					System.out.println("TMGtableData\nArraylist Error Key: "
							+ listIndex + " - FieldName: " + fieldName);
				throw new HCException("TMGtableData\nArraylist Error Key: "
							+ listIndex + " - FieldName: " + fieldName);
			}
			return PIDlist.get(listIndex).getString(fieldName);
		} catch (DBFException dbfe) {
			System.out.println("TMGtableData/getValueString/DBFException: " + dbfe.getMessage());
			dbfe.printStackTrace();
			throw new HCException("TMGtableData/getValueString/DBFException: " + dbfe.getMessage());
		}
	}


/**
 * findValueString - find data from rows referred to by HashMap index PID number
 * @param PID
 * @param fieldName
 * @return
 * @throws HCException
 */
	public String findValueString(int PID, String fieldName) throws HCException {
		try {
			if (PIDmap.get(PID) == null) {
				if (TMGglobal.DEBUG)
					System.out.println("TMGtableData/findValueString/HashMap Error Key: " + PID + " - FieldName: " + fieldName);
				throw new HCException("TMGtableData/findValueString/HashMap Error Key: " + PID + " - FieldName: " + fieldName);
			}
			return PIDmap.get(PID).getString(fieldName);
		} catch (DBFException dbfe) {
			System.out.println("TMGtableData/findValueString/DBFException: " + dbfe.getMessage());
			dbfe.printStackTrace();
			throw new HCException("TMGtableData/findValueString/DBFException: " + dbfe.getMessage());
		}
	}

/**
 * findVectorString(int PID,int vectorInx, String fieldName)
 * @param PID
 * @param vectorInx
 * @param fieldName
 * @return String from field name
 * @throws HCException
 */
	public String findVectorString(int PID, int vectorIndex, String fieldName) throws HCException {
		try {
			if (multiPidMap.get(PID) == null) {
				if (TMGglobal.DEBUG)
					System.out.println("TMGtableData/findVectorString/HashMap Multi Error Key: " + PID + " - FieldName: " + fieldName);
				throw new HCException("TMGtableData/findVectorString/HashMap Multi Errror Key: " + PID + " - FieldName: " + fieldName);
			}
			if (vectorIndex < 0 || vectorIndex >= multiPidMap.get(PID).size()) {
				if (TMGglobal.DEBUG)
					System.out.println("TMGtableData/findVectorString/HashMap Multi Error Key: " + PID + " - FieldName: " + fieldName);
				throw new HCException("TMGtableData/findVectorString/HashMap Multi Errror Key: " + PID + "Vector idex: " + vectorIndex + " - FieldName: " + fieldName);
			}
			return multiPidMap.get(PID).get(vectorIndex).getString(fieldName);
		} catch (DBFException dbfe) {
			System.out.println("TMGtableData/findVectorString/DBFException: " + dbfe.getMessage());
			dbfe.printStackTrace();
			throw new HCException("TMGtableData/findVectorString/DBFException: " + dbfe.getMessage());
		}
	}

/**
 * existVector(int PID)
 * @param PID
 * @return
 * @throws HCException
 */
	public boolean existVector(int PID) throws HCException {
		try {
			if (multiPidMap.get(PID) == null) return false; else return true;
		} catch (DBFException dbfe) {
			System.out.println("TMGtableData/existVector/DBFException: " + dbfe.getMessage());
			dbfe.printStackTrace();
			throw new HCException("TMGtableData/existVector/DBFException: " + dbfe.getMessage());
		}
	}

/**
 * existInMap(int PID)
 * @param PID
 * @return
 * @throws HCException
 */
	public boolean existInMap(int PID) throws HCException {
		try {
			if (PIDmap.get(PID) == null) return false; else return true;
		} catch (DBFException dbfe) {
			System.out.println("TMGtableData/existInMap/DBFException: " + dbfe.getMessage());
			dbfe.printStackTrace();
			throw new HCException("TMGtableData/existInMap/DBFException: " + dbfe.getMessage());
		}
	}

/**
 * getVectorSize(int PID)
 * @param PID
 * @return int - size of Vector
 */
	public int getVectorSize(int PID) throws HCException {
		try {
			if (existVector(PID)) return multiPidMap.get(PID).size();
			else {
				if (TMGglobal.DEBUG) System.out.println("TMGtableData - HashMap vector size = 0 for PID: " + PID);
				HB0711Logging.logWrite("TMG to HRE - TMGtableData - Name or place elements missing for PID: " + PID);
				return 0;
			}
		} catch (DBFException dbfe) {
			System.out.println("TMGtableData/getVectorSize/DBFException: " + dbfe.getMessage());
			dbfe.printStackTrace();
			throw new HCException("TMGtableData/getVectorSize/DBFException: " + dbfe.getMessage());
		}
	}

/**
 * findValueObject - find data from rows referred to by HashMap index PID number
 * @param PID
 * @param fieldName
 * @return
 * @throws HCException
 */

	public Object findValueObject(int PID,String fieldName) throws HCException {
		try {
			if (PIDmap.get(PID) == null) {
				if (TMGglobal.DEBUG)
					System.out.println("TMGtableData/findValueObject/HashMap Error Key: " + PID
							+ " - FieldName: " + fieldName);
				throw new HCException("TMGtableData/findValueObject/HashMap Error Key: " + PID
						+ " - FieldName: " + fieldName);
			}
			return PIDmap.get(PID).getObject(fieldName);
		} catch (DBFException dbfe) {
			System.out.println("TMGtableData/findValueObject/DBFException: " + dbfe.getMessage());
			dbfe.printStackTrace();
			throw new HCException("TMGtableData/findValueObject/DBFException: " + dbfe.getMessage());
		}
	}

/**
 * getValueInt	- get data from rows indexed by ArrayList and Field Name in row
 * @param PID
 * @param fieldName
 * @return
 * @throws HCException
 */
	public int getValueInt(int listIndex, String fieldName) throws HCException {
		try {
			if (PIDlist.get(listIndex) == null) {
				if (TMGglobal.DEBUG)
					System.out.println("TMGtableData/getValueInt/HashMap Error Key: "
							+ listIndex + " - FieldName: " + fieldName);
				throw new HCException("TMGtableData/getValueInt/HashMap Error Key: "
							+ listIndex + " - FieldName: " + fieldName);
			}
			return PIDlist.get(listIndex).getInt(fieldName);
		} catch (DBFException dbfe) {
			System.out.println("TMGtableData/getValueInt/DBFException line: " + listIndex + " / " + dbfe.getMessage());
			dbfe.printStackTrace();
			throw new HCException("TMGtableData/getValueInt/DBFException line: " + listIndex + " / " + dbfe.getMessage());
		}
	}

/**
 * findValueInt	- find data from rows referred to by HashMap index PID number
 * @param PID
 * @param fieldName
 * @return
 * @throws HCException
 */
	public int findValueInt(int PID, String fieldName) throws HCException {
		try {
			if (PIDmap.get(PID) == null) {
				if (TMGglobal.DEBUG)
					System.out.println("TMGtableData/findValueInt/HashMap Error Key: "
							+ PID + " - FieldName: " + fieldName);
				throw new HCException("TMGtableData/findValueInt/HashMap Error Key: "
							+ PID + " - FieldName: " + fieldName);
			}
			return PIDmap.get(PID).getInt(fieldName);
		} catch (DBFException dbfe) {
			System.out.println("TMGtableData/findValueInt/DBFException line: " + PID + " / " + dbfe.getMessage());
			dbfe.printStackTrace();
			throw new HCException("TMGtableData/findValueInt/DBFException line: " + PID + " / " + dbfe.getMessage());
		}
		//return 0;
	}

/**
 * findVectorInt(int PID, int vectorIndex, String fieldName)
 * @param PID
 * @param fieldName
 * @return integer from field in DBFRow
 * @throws HCException
 */
	public int findVectorInt(int PID, int vectorIndex, String fieldName) throws HCException {
		try {
			if (multiPidMap.get(PID) == null) {
				if (TMGglobal.DEBUG)
					System.out.println("TMGtableData/findVectorInt/HashMap Multi Error Key: " + PID + " - FieldName: " + fieldName);
				throw new HCException("TMGtableData/findVectorInt/HashMap Multi Errror Key: " + PID + " - FieldName: " + fieldName);
			}
			if (vectorIndex < 0 || vectorIndex >= multiPidMap.get(PID).size()) {
				if (TMGglobal.DEBUG)
					System.out.println("TMGtableData/findVectorInt/HashMap Multi Error Key: " + PID + " - FieldName: " + fieldName);
				throw new HCException("TMGtableData/findVectorInt/HashMap Multi Errror Key: " + PID + "Vector idex: " + vectorIndex + " - FieldName: " + fieldName);
			}
			return multiPidMap.get(PID).get(vectorIndex).getInt(fieldName);
		} catch (DBFException dbfe) {
			System.out.println("TMGtableData/findVectorInt/DBFException: " + dbfe.getMessage());
			dbfe.printStackTrace();
			throw new HCException("TMGtableData/findVectorInt/DBFException: " + dbfe.getMessage());
		}
	}

/**
 * getValueBoolean(int listIndex,String fieldName)
 * @param listIndex
 * @param fieldName
 * @return
 * @throws HCException
 */
	public boolean getValueBoolean(int listIndex,String fieldName) throws HCException {
		try {
			if (PIDlist.get(listIndex) == null) {
				if (TMGglobal.DEBUG)
					System.out.println("TMGtableData/getValueBoolean/HashMap Error Key: "
							+ listIndex + " - FieldName: " + fieldName);
				throw new HCException("TMGtableData/getValueBoolean/HashMap Error Key: "
							+ listIndex + " - FieldName: " + fieldName);
			}
			return PIDlist.get(listIndex).getBoolean(fieldName);
		} catch (DBFException dbfe) {
			System.out.println("TMGtableData/getValueBoolean/DBFException: " + dbfe.getMessage());
			dbfe.printStackTrace();
			throw new HCException("TMGtableData/getValueBoolean/DBFException: " + dbfe.getMessage());
		}
	}
	
/**
 * findValueBoolean(int PID, String fieldName)	
 * @param PID
 * @param fieldName
 * @return
 * @throws HCException
 */
	public Boolean findValueBoolean(int PID, String fieldName) throws HCException {
		try {
			if (PIDmap.get(PID) == null) {
				if (TMGglobal.DEBUG)
					System.out.println("TMGtableData/findValueInt/HashMap Error Key: "
							+ PID + " - FieldName: " + fieldName);
				throw new HCException("TMGtableData/findValueInt/HashMap Error Key: "
							+ PID + " - FieldName: " + fieldName);
			}
			return PIDmap.get(PID).getBoolean(fieldName);
		} catch (DBFException dbfe) {
			System.out.println("TMGtableData/findValueInt/DBFException line: " + PID + " / " + dbfe.getMessage());
			dbfe.printStackTrace();
			throw new HCException("TMGtableData/findValueInt/DBFException line: " + PID + " / " + dbfe.getMessage());
		}
	}
/**
 * findVectorBoolean(int PID, int vectorIndex, String fieldName)
 * @param PID
 * @param vectorIndex
 * @param fieldName
 * @return
 * @throws HCException
 */
	public boolean findVectorBoolean(int PID, int vectorIndex, String fieldName) throws HCException {
		try {
			if (multiPidMap.get(PID) == null) {
				if (TMGglobal.DEBUG)
					System.out.println("TMGtableData/findVectorBoolean/HashMap Multi Error Key: " + PID + " - FieldName: " + fieldName);
				throw new HCException("TMGtableData/findVectorBoolean/HashMap Multi Errror Key: " + PID + " - FieldName: " + fieldName);
			}
			if (vectorIndex < 0 || vectorIndex >= multiPidMap.get(PID).size()) {
				if (TMGglobal.DEBUG)
					System.out.println("TMGtableData/findVectorBoolean/HashMap Multi Error Key: " + PID + " - FieldName: " + fieldName);
				throw new HCException("TMGtableData/findVectorBoolean/HashMap Multi Errror Key: " + PID + "Vector idex: " + vectorIndex + " - FieldName: " + fieldName);
			}
			return multiPidMap.get(PID).get(vectorIndex).getBoolean(fieldName);
		} catch (DBFException dbfe) {
			System.out.println("TMGtableData/findVectorBoolean/DBFException: " + dbfe.getMessage());
			dbfe.printStackTrace();
			throw new HCException("TMGtableData/findVectorBoolean/DBFException: " + dbfe.getMessage());
		}
	}

/**
 * getValueDate	- get data from rows indexed by ArrayList and Field Name in row
 * @param PID
 * @param fieldName
 * @return
 * @throws HCException
 */
	public  Date getValueDate(int listIndex,String fieldName) throws HCException {
		try {
			return PIDlist.get(listIndex).getDate(fieldName);
		} catch (DBFException dbfe) {
			System.out.println("TMGtableData/getValueDate/DBFException: " + dbfe.getMessage());
			dbfe.printStackTrace();
			throw new HCException("TMGtableData/getValueDate/DBFException: " + dbfe.getMessage());
		}
	}

/**
 * findValueDate - find data from rows referred to by HashMap index PID number
 * @param PID
 * @param fieldName
 * @return
 * @throws HCException
 */
	public  Date findValueDate(int PID,String fieldName) throws HCException {
		try {
			return PIDlist.get(PID).getDate(fieldName);
		} catch (DBFException dbfe) {
			System.out.println("TMGtableData/findValueDate/DBFException: " + dbfe.getMessage());
			dbfe.printStackTrace();
			throw new HCException("TMGtableData/findValueDate/DBFException: " + dbfe.getMessage());
		}
	}
/**
 *
 * @param PID
 * @param fieldName
 * @return
 * @throws HCException
 */
	public Vector<DBFRow> findVectorRows(int PID) throws HCException {
		try {
			return multiPidMap.get(PID);
		} catch (DBFException dbfe) {
			System.out.println("TMGtableData/findValueDate/DBFException: " + dbfe.getMessage());
			dbfe.printStackTrace();
			throw new HCException("TMGtableData/findValueDate/DBFException: " + dbfe.getMessage());
		}
	}

/**
 * getNrOfRows()
 * @return
 */
	public int getNrOfRows() {
		return  nrOFRows;
	}

/**
 * int getMultiRowNr()
 * @return
 */
	public int getMultiRowNr() {
		return rowMultiRows;
	}

	public int getTableRows() {
		return  dbfReader.getRecordCount();
	}
} // End of Class


