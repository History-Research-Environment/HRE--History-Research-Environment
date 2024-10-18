package hre.tmgjava;
/**********************************************************************************
 * Uses library com.linuxense.javadbf
 * Java library for reading and writing Xbase (dBase/DBF) files
 * https://github.com/albfernandez/javadbf
 * albfernandez/javadbf is licensed under the
 * GNU Lesser General Public License v3.0
 * Written by Alberto Fernï¿½ndez
 ***********************************************************************************
 * Process Exhibit tables in HRE
 * *********************************************************************************
 * v0.00.0027 2022-01-20 -
 * 			  2022-01-26 - Implemented Exhibits for PERSONS (N. Tolleshaug)
 * 			  2022-01-27 - Statistics for all Exhibits  (N. Tolleshaug)
 * 			  2022-01-29 - Updating "BEST_IMAGE_RPID from T676 in T401 (N. Tolleshaug)
 * 			  2022-05-26 - Rearranged updated "BEST_IMAGE_RPID (N. Tolleshaug)
 * 			  2022-05-27 - Read I.dbf row by row (N. Tolleshaug)
 * 			  2022-06-14 - Modified counting of Exhibits (N. Tolleshaug)
 * 			  2022-08-06 - Modified return in do loop (H. Leininger)
 * 			  2022-08-08 - New processing of exhibit type (H. Leininger)
 * v0.00.0028 2023-02-15 - Implemented create new thumbs for exhibits (N. Tolleshaug)
 * 			  2023-02-19 - Implemented handle new exhibit processing (N. Tolleshaug)
 * 			  2023-02-24 - T169 implemented for setting exhibit types  (N. Tolleshaug)
 * 			  2023-03-01 - Implemented TEXT for exhibit types  (N. Tolleshaug)
 * 			  2023-03-02 - New text for exhibits like .pdf .odt  (N. Tolleshaug)
 * 		      2023-03-10 - Import Caption from I.dbf  (N. Tolleshaug)
 * v0.01.0029 2023-05-01 - Implemented v22a (N. Tolleshaug)
 * v0.01.0030 2023-08-26 - Line 610 - Exception replace IOException (N. Tolleshaug)
 * 			  2023-08-28 - Added HB0711Logging.logWrite("ERROR Creating Thumb Image (D.Ferguson)
 * 			  2023-08-28 - Removed if (EXHCHECK) control error (D.Ferguson)
 *  		  2023-08-31 - Improved (EXHCHECK) control error (D.Ferguson)
 *  		  2023-09-05 - Attempt to handle PNG files in create thumb (N. Tolleshaug)
 *  		  2023-09-05 - Eliminated debug error line 47X in console out (N. Tolleshaug)
 *  		  2023-09-06 - Modified image exception handling (N. Tolleshaug)
 **********************************************************************************/
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.sql.rowset.serial.SerialBlob;

import com.linuxense.javadbf.DBFRow;

import hre.bila.HB0711Logging;

/**
 * class TMGpass_V22a_Exhibits extends TMGpass_Exhibits
 * @author NTo
 * @since 2022.1.20
 */
class TMGpass_Exhibits {
	HREdatabaseHandler pointHREbase;

	long proOffset = 1000000000000000L;
	long null_RPID  = 1999999999999999L;
	boolean EXHCHECK = false;

	int tmgPersonExh = 0;
	int tmgEventExh = 0;
	int tmgLocationExh = 0;
	int tmgSourceExh = 0;
	int tmgCitationExh = 0;
	int tmgReposExh = 0;
	int hreRecordsT676 = 0;
	int tmgImageExhibits = 0;
	int tmgTextExhibits = 0;
	int errorExhibits = 0;
	int createdExhibits = 0;

	private static TMGtableData  tmgItable = null;

	ResultSet tableT676, tableT677;
	DBFRow tmgIrow;

	long [] baseTypePID = new long[8];
	long [] subTypePID = new long[4];
	long baseTypeExhibitRPID = null_RPID;
	long subTypeExhibitRPID = null_RPID;

	int missingNrOf_PID = 0;
	int rowsInT402 = 0;
	int eventType = 0;
	int personNr = 0;

	// Linked record to dump from Exhibit
	//int exhibitLinkedRecord = 143;
	int exhibitLinkedRecord = 0;

	String rlType;
	int rlNum;
	int idExhibit;
	String filePath = " -- ";
	String newExhibitFolderPath;
	String captionText;

	// Set the number of lines printed
	int maxNrPrint = 20;
	// HashMap for best image PID in T401
	HashMap<Long,Long> bestImagePersonMap;
	HashMap<Long,Long> bestImageEventMap;
	HashMap<Long,Long> bestImageLocationMap;

/**
 * Constructor 	TMGPassEvent(TMGDatabaseHREhandler pointHREbase)
 * @param pointHREbase
 */
	public TMGpass_Exhibits(HREdatabaseHandler pointHREbase) {
		this.pointHREbase = pointHREbase;
		tmgItable = TMGglobal.tmg_I_table;
		tableT676 = TMGglobal.T676;
		tableT677 = TMGglobal.T677;
		EXHCHECK = TMGglobal.EXHCHECK;
		this.newExhibitFolderPath = TMGglobal.newExhibitFolderPath;
		if (EXHCHECK) System.out.println(" TMGpass_V22a_Exhibits release: " + TMGglobal.releaseDate);
		findBaseSubTypePID(4);
	}

/**
 * returnByteContent(byte[] content)
 * @param content
 * @return
 */
	private byte[] returnByteContent(byte[] content) {
		if (content.length == 64) {
		    if (content[0] == 0 && content[63] == 0) {
		    	return null;
		    }
		}
		return content;
	}

/**
 * returnStringContent(String content)
 * @param content
 * @return
 */
	private String returnStringContent(String content) {
		if (content.length() == 64) {
		   if (content.trim().length() == 0) return "";
		}
		return content;
	}

/**
 * findSubTypePID()
 * @return
 */
	private void findBaseSubTypePID(int select) {
		//boolean dumpExhibitTypes = true;
		boolean dumpExhibitTypes = false;
		//boolean dump = true;
		int subType, baseType;
		try {
			ResultSet researchTypeRPID = pointHREbase.requestSQLdata("T169_ENTY_TYPE_DEFN");
			researchTypeRPID.beforeFirst();
			if (dumpExhibitTypes) System.out.println("************************");
			while (researchTypeRPID.next()) {
				baseType = researchTypeRPID.getInt("BASE_TYPE");
				subType = researchTypeRPID.getInt("SUB_TYPE");
				if (subType == 0) {
					baseTypePID[baseType] = researchTypeRPID.getLong("PID");
					if (dumpExhibitTypes) System.out.println(" BaseType: " + baseType + " PID: " + baseTypePID[baseType]
							+ " - " + researchTypeRPID.getString("FIRST_LEVEL_DEFN_TRAN").split("\\|")[0]);
				}
			}
			researchTypeRPID.beforeFirst();
			if (dumpExhibitTypes) System.out.println("************************");
			while (researchTypeRPID.next()) {
				baseType = researchTypeRPID.getInt("BASE_TYPE");
				subType = researchTypeRPID.getInt("SUB_TYPE");
				if (baseType == select && subType != 0) {
					if (subType > 9) subType = subType - baseType*10;
					subTypePID[subType] = researchTypeRPID.getLong("PID");
					if (dumpExhibitTypes) System.out.println(" SubType: " + subType + " PID: " + subTypePID[subType]
								+ " - " + researchTypeRPID.getString("SECND_LEVEL_DEFN_TRAN").split("\\|")[0]);
				}
			}

		} catch (HCException | SQLException ex) {
			System.out.println(" TMGpass_V22a_Exhibits - " + ex.getMessage());
			ex.printStackTrace();
		}
	}

/**
 * addExhibitsToHRE(TMGHREconverter tmgHreConverter)
 * @param tmgHreConverter
 */
	public void addExhibitsToHRE(TMGHREconverter tmgHreConverter) throws HCException {

		int progress;
		int idExhibit;
		int processedRow = 0;
		int nrOftmgIRows = tmgItable.getTableRows();

	// New HashMap
		bestImagePersonMap = new HashMap<>();
		bestImageEventMap = new HashMap<>();
		bestImageLocationMap = new HashMap<>();

		if (TMGglobal.DEBUG)
			System.out.println(" tmgITable size: " + nrOftmgIRows + " rows");

		if (TMGglobal.DUMP) System.out.println("\nTest Exhibit processing initiated");

		tmgHreConverter.setStatusProgress(0);
		for (int index_I_table = 0; index_I_table < nrOftmgIRows; index_I_table++) {
			processedRow = index_I_table + 1;

		// Read next
			tmgIrow = TMGglobal.tmg_I_table.TMGreadTableRow();
			//if (tmgIrow == null) return; // Nils Tolleshaug Original
			//if (tmgIrow == null) break; // Helmut Leininger 6.8.2022
			if (tmgIrow == null) {
				if (EXHCHECK) System.out.println(" WARNING - addExhibitsToHRE - row: " + index_I_table + " no content - tmgIrow == null!");
				continue; // Nils Tolleshaug 8.8.2022
			}

		// Report progress in %
			progress = (int)Math.round(((double)processedRow / (double)nrOftmgIRows) * 100);
			tmgHreConverter.setStatusProgress(progress);

			subTypeExhibitRPID = null_RPID;

			if (TMGglobal.DEBUG)
				System.out.println(" Progress: " + index_I_table + "  %: " + progress);

			if (TMGglobal.dataSetID == tmgIrow.getInt("DSID"))	{

				rlType = tmgIrow.getString("RLTYPE").trim();
				rlNum = tmgIrow.getInt("RLNUM");
				idExhibit = tmgIrow.getInt("IDEXHIBIT");

				if (tmgIrow.getInt("ID_PERSON") != 0) {
					tmgPersonExh++;
					baseTypeExhibitRPID = baseTypePID[0];
					if (!bestImagePersonMap.containsKey(tmgIrow.getInt("ID_PERSON") + proOffset))
						bestImagePersonMap.put(tmgIrow.getInt("ID_PERSON") + proOffset, idExhibit + proOffset);
					if (tmgIrow.getBoolean("PRIMARY"))
							bestImagePersonMap.put(tmgIrow.getInt("ID_PERSON") + proOffset, idExhibit + proOffset);

				} else if (tmgIrow.getInt("ID_EVENT") != 0) {
					tmgEventExh++;
					baseTypeExhibitRPID = baseTypePID[6];
					bestImageEventMap.put(tmgIrow.getInt("ID_EVENT") + proOffset, idExhibit + proOffset);

				} else if (tmgIrow.getInt("ID_PLACE") != 0) {
					tmgLocationExh++;
					baseTypeExhibitRPID = baseTypePID[2];
					bestImageLocationMap.put(tmgIrow.getInt("ID_PLACE") + proOffset, idExhibit + proOffset);
					//System.out.println(" Location: " + tmgIrow.getInt("ID_PLACE") + "/" + idExhibit);

				} else if (tmgIrow.getInt("ID_SOURCE") != 0) {
					tmgSourceExh++;
					baseTypeExhibitRPID = null_RPID;

				} else if (tmgIrow.getInt("ID_CIT") != 0) {
					tmgCitationExh++;
					baseTypeExhibitRPID = null_RPID;

				} else if (tmgIrow.getInt("ID_REPOS") != 0) {
					tmgReposExh++;
					baseTypeExhibitRPID= null_RPID;

				} else System.out.println(" Not found Exhibit RLTYPE's : " + index_I_table + " / "
							+ rlType + " / " + rlNum);

				if (TMGglobal.DEBUG) System.out.println(index_I_table  + " Prop: "
						+ tmgIrow.getString("PROPERTY"));

				if (exhibitLinkedRecord > 0)
					if (rlNum == exhibitLinkedRecord)
						dumpExhibitRecordFromTMG(tmgIrow);
				
				idExhibit = tmgIrow.getInt("IDEXHIBIT");
				long ownerRPID = idExhibit + proOffset;
				
			// Adding to T676 and T677
					addToT676_DIGT(index_I_table, idExhibit, tableT676);
					addToT677_DIGT_NAME(index_I_table, ownerRPID, idExhibit, tableT677);

			// Count processed records
				hreRecordsT676++;

			} else if (TMGglobal.DEBUG) System.out.println(" Dataset DSID in ...I.dbf " 
					+ " DSID: " + tmgIrow.getInt("DSID") + " not processed!");
		}

// Print statistics at console
		if (EXHCHECK) {
			System.out.println("\n Statistics TMG Exhibits for: "
					+ TMGHREconverter.tmgProjectName + "_I.dbf:");
			System.out.println(" Person   PID's: " + tmgPersonExh);
			System.out.println(" Events   PID's: " + tmgEventExh);
			System.out.println(" Places   PID's: " + tmgLocationExh);
			System.out.println(" Sources  PID's: " + tmgSourceExh);
			System.out.println(" Citation PID's: " + tmgCitationExh);
			System.out.println(" Reposit  PID's: " + tmgReposExh);
			System.out.println(" T676 PID's nr: " + hreRecordsT676);	
			System.out.println(" Created exhibits:  " + createdExhibits);
			System.out.println(" TextEx   PID's: " + tmgTextExhibits);
			System.out.println(" ImageEx   PID's: " + tmgImageExhibits);
			System.out.println(" *** Exhibit err's: " + errorExhibits);
			System.out.println(" Other dataset PID's: " + missingNrOf_PID);
		}

// Statistics for converter Window
		tmgHreConverter.setStatusMessage(" - Person # exhibits: " + tmgPersonExh);
		tmgHreConverter.setStatusMessage(" - Events # exhibits: " + tmgEventExh);
		tmgHreConverter.setStatusMessage(" - Places # exhibits: " + tmgLocationExh);
		tmgHreConverter.setStatusMessage(" - Source # exhibits: " + tmgSourceExh);
		tmgHreConverter.setStatusMessage(" - Citation # exhibits: " + tmgCitationExh);
		tmgHreConverter.setStatusMessage(" - Repository # exhibits: " + tmgReposExh);
		tmgHreConverter.setStatusMessage(" - Total # Text exhibits: " + tmgTextExhibits);
		tmgHreConverter.setStatusMessage(" - Total # Image exhibits: " + tmgImageExhibits);
		tmgHreConverter.setStatusMessage(" - Total # Created exhibits: " + createdExhibits);
		tmgHreConverter.setStatusMessage(" - Total # Exhibits processed: " + hreRecordsT676);
		tmgHreConverter.setStatusMessage(" *** Total # Error exhibits: " + errorExhibits);
		if (errorExhibits > 0)
			tmgHreConverter.setStatusMessage(" *** Exhibit errors - create Thumb image!");
		if (TMGglobal.DEBUG)
			System.out.println(" Exhibit processing ended!\n");

        if (tmgItable != null) {
            tmgItable.closeTMGtable();
            tmgItable = null;
        }
	} // end EXHIBIT


/**
 * addToT676_DIGT(int rowPID, ResultSet hreTable)
 * @param idExhibit
 * @param hreTable
 * @throws HCException
 */
	private void addToT676_DIGT(int rowIdbfTable, int idExhibit, ResultSet hreTable) throws HCException {
		String filePath, fileType;
		if (TMGglobal.DEBUG) {
			System.out.println(" ** addTo T676_DIGT row: " + rowIdbfTable);
			System.out.println("Path: " + tmgIrow.getString("PROPERTY"));
		}
		byte[] thumbCont = returnByteContent(tmgIrow.getBytes("THUMB"));
		byte[] textCont = returnByteContent(tmgIrow.getString("TEXT").getBytes());
		int rlnum = tmgIrow.getInt("RLNUM");
		long ownerRPID = rlnum + proOffset;
		
		filePath = checkExhibitContent(idExhibit, rlnum, textCont, thumbCont);
		if (filePath.length() > 10) fileType = filePath.substring(filePath.length()-4);
		else fileType = ".xxx";

		if (EXHCHECK) 
			System.out.println(" Exhibit nr: " + idExhibit + " Filetype: " + fileType);	
		
	// Create text exhibit
		//if (textCont == null && thumbCont != null) textCont = createTextCont(idExhibit);
		
		if (textCont == null || textCont.length < 5) 
				if (fileType.equals(".txt")) textCont = createTextCont(idExhibit);

	// Try to recreate thumb
		//if (thumbCont == null && textCont == null) thumbCont = createImageThumb(idExhibit);
		if (thumbCont == null || thumbCont.length < 5) thumbCont = createImageThumb(idExhibit);

		try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();
		// Update new row in H2 database
			hreTable.updateLong("PID", proOffset + idExhibit);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateBoolean("HAS_CITATIONS", false);
			hreTable.updateLong("VISIBLE_ID", tmgIrow.getInt("IDREF"));
			hreTable.updateLong("OWNER_RPID", ownerRPID);
			hreTable.updateLong("ENTITY_TYPE_RPID", baseTypeExhibitRPID);
			hreTable.updateLong("SUB_TYPE_RPID", subTypeExhibitRPID);
			hreTable.updateString("SURETY", "3");
			hreTable.updateLong("BEST_NAME_RPID", null_RPID);
/**
 * Need update of IS_INTERNAL and IS_BINARY			
 */
			if (thumbCont != null)
				hreTable.updateBoolean("IS_INTERNAL", true);
			else hreTable.updateBoolean("IS_INTERNAL", false);
//****************************
			if (thumbCont != null)
				hreTable.updateBoolean("IS_BINARY", true);
			else hreTable.updateBoolean("IS_BINARY", false);

			if (thumbCont != null)
				hreTable.updateBlob("BIN_CONTENT", new SerialBlob(thumbCont));
			else hreTable.updateBlob("BIN_CONTENT", new SerialBlob(new byte[0]));

			if (textCont != null)
				hreTable.updateObject("CHAR_CONTENT", textCont); //  byte[]
			else hreTable.updateObject("CHAR_CONTENT", new byte[0]);

			hreTable.updateString("FILE_PATH", filePath);		
			hreTable.updateString("FILE_EXTN", fileType);
			hreTable.updateString("AUDIO_ENCODING","MP2");
			hreTable.updateString("VIDEO_ENCODING","MP4");
			hreTable.updateString("TEXT_ENCODING","Plain-txt");
			//hreTable.updateLong("ENCODING_RPID", null_RPID);
			hreTable.updateInt("IMAGE_WIDTH",100);
			hreTable.updateInt("IMAGE_HEIGHT",100);

		//Insert row
			hreTable.insertRow();
		} catch (SQLException sqle) {
			if (TMGglobal.DEBUG) System.out.println("Not able to update table - T676_DIGT");
			sqle.printStackTrace();
			throw new HCException("TMGpass_V22a_Exhibits - addToT676_DIGT - error: " + sqle.getMessage());
		}
	}
	
/**
 * addToT677_DIGT_NAME(int rowIdbfTable, ResultSet hreTable)	
 * @param rowIdbfTable
 * @param hreTable
 * @throws HCException
 */
	private void addToT677_DIGT_NAME(int rowIdbfTable, long ownerRPID, int idExhibit, ResultSet hreTable) throws HCException {

		String reference = tmgIrow.getString("PROPERTY");
	// reference too long:
		if (reference.length() > 30) {	
			if (TMGglobal.DEBUG) System.out.println(" Long T677_DIGT_NAME - REFERENCE - Length: " 
					+ reference.length() + "/30");
			reference = reference.substring(0,30);	
		} 
	// no caption in TMG
		if (captionText == null) captionText = "";
		
		String description = tmgIrow.getString("XNAME");
		try {
		// moves cursor to the insert row
			hreTable.moveToInsertRow();
		// Update new row in H2 database
			hreTable.updateLong("PID", proOffset + idExhibit);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateBoolean("HAS_CITATIONS", false);
			hreTable.updateLong("OWNER_RPID", ownerRPID);
			hreTable.updateLong("START_HDATE_RPID", null_RPID);
			hreTable.updateLong("END_HDATE_RPID", null_RPID);	
			hreTable.updateLong("THEME_RPID", null_RPID);
			hreTable.updateLong("MEMO_RPID", null_RPID);
			hreTable.updateString("SURETY", "3");
			hreTable.updateString("LANG_CODE", "");
			hreTable.updateString("REFERENCE", reference);	
			hreTable.updateString("DESCRIPTION", description); // Temp - substituted by DESCRIP_RPID
			hreTable.updateString("CAPTION", captionText); // Temp - substituted by CAPTION_RPID
			hreTable.updateLong("DESCRIP_RPID", null_RPID);
			hreTable.updateLong("CAPTION_RPID", null_RPID);
		//Insert row
			hreTable.insertRow();
		} catch (SQLException sqle) {
			if (TMGglobal.DEBUG) 
				System.out.println("TMGpass_V22a_Exhibits - addToT677_DIGT_NAME - error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HCException("TMGpass_V22a_Exhibits - addToT677_DIGT_NAME - error: " + sqle.getMessage());
		}
	}

/**
 * createTextCont(int idExhibit)
 * @param idExhibit
 * @return byte[] with content
 */
	private byte[] createTextCont(int idExhibit) {
		byte[] textCont = null;
		String filePath = returnStringContent(tmgIrow.getString("TFILENAME"));
		if (filePath.length() > 0) {
			if (TMGglobal.CONVERT_EXH_PATH) filePath = "" + convertFilePath(filePath);
			textCont = createExhibitText(idExhibit, filePath);
			if (textCont != null) {
				createdExhibits++;
				if (TMGglobal.DEBUG)
					System.out.println(" Created Text nr: " + idExhibit + " - "
						+ "length: " + textCont.length + " / " + new String(textCont)
						+ " path: " + filePath + " length: " + filePath.length());
			}
		}
		return textCont;
	}

/**
 * createImageThumb(int idExhibit)
 * @param idExhibit
 * @return byte[] with content
 */
	private byte[] createImageThumb(int idExhibit) {
		byte[] thumbCont = null;
		String filePath = returnStringContent(tmgIrow.getString("IFILENAME"));
		if (filePath.length() > 0) {
			if (TMGglobal.CONVERT_EXH_PATH) filePath = "" + convertFilePath(filePath);
			thumbCont = createNewImageThumb(idExhibit, filePath);
			if (thumbCont != null) {
				createdExhibits++;	
				if (TMGglobal.DEBUG)
					System.out.println(" Created Thumb nr:  " + idExhibit + " size: " + thumbCont.length
						+ " path: " + filePath + " length: " + filePath.length());
			}
		}
		return thumbCont;
	}

/**
 * bestImageProcessing()
 * @throws HCException
 */
	public void bestImageProcessing() throws HCException {
		String tableName;
	// Update BRST_IMAGEP_ID in T401.452 and 551
		tableName = "T401_PERS";
		ResultSet personTable401 = pointHREbase.requestSQLdata(tableName);
		if (bestImagePersonMap.size() > 0 ) updateBestImageRPID(personTable401, bestImagePersonMap);
		tableName = "T450_EVNT";
		ResultSet eventTable452 = pointHREbase.requestSQLdata(tableName);
		if (bestImageEventMap.size() > 0 ) updateBestImageRPID(eventTable452,bestImageEventMap);
		tableName = "T551_LOCN";
		ResultSet locationTable551 = pointHREbase.requestSQLdata(tableName);
		if (bestImageLocationMap.size() > 0 ) updateBestImageRPID(locationTable551,bestImageLocationMap);
	}

/**
 * updateBestImageRPID(ResultSet personTable401)
 * @param personTable401
 * @throws HCException
 */
	private void updateBestImageRPID(ResultSet hreTable,
								     HashMap<Long,Long> bestImageMap)
								    		 throws HCException {
		try {
			hreTable.beforeFirst();
			while (hreTable.next()) {
				long tablePID = hreTable.getLong("PID");
				if (bestImageMap.containsKey(tablePID)) {
					if (TMGglobal.DEBUG)
						System.out.println(" Found PID registered: " + tablePID);
					hreTable.updateLong("BEST_IMAGE_RPID", bestImageMap.get(tablePID));
					hreTable.updateRow();
				}
			}
			hreTable.close();
		} catch (SQLException sqle) {
			System.out.println("updateBestImageRPID - error:" + sqle.getMessage());
			throw new HCException("updateBestImageRPID - error:" + sqle.getMessage() + "\n");
		}
	}

/**
 * checkImageFromByte(int exhibit, int rlnum, byte[] textData, byte[] imageData)
 * @param exhibit
 * @param rlnum
 * @param textData
 * @param imageData
 * @return
 */
	private String checkExhibitContent(int exhibit, int rlnum, byte[] textData, byte[] imageData) {
		String filePath = "- null";
		try {
			if (textData != null) {
				tmgTextExhibits++;
				subTypeExhibitRPID = subTypePID[2];
				filePath = returnStringContent(tmgIrow.getString("TFILENAME"));
				captionText = returnStringContent(tmgIrow.getString("CAPTION"));
/*				if (EXHCHECK) System.out.println(" Exhibit Text nr: " + exhibit + " - "
						+ " RLNUM: " + rlnum + " length: "
							+ textData.length + " / " + new String(textData)); */
			}
			if (imageData != null) {
				tmgImageExhibits++;
				subTypeExhibitRPID = subTypePID[1];
				filePath = returnStringContent(tmgIrow.getString("IFILENAME"));
				captionText = returnStringContent(tmgIrow.getString("CAPTION"));
			}
			//else if (EXHCHECK) 
			//	if (textData == null) System.out.println(" checkExhibitContent - Exhibit Image/Text nr: " + exhibit + " = null");
			
			if (captionText.length() > 150) {
				if (EXHCHECK) System.out.println(" checkExhibitContent - Long caption Text nr: " 
						+ exhibit + " length: " + captionText.length() + " / " + captionText);
				captionText = captionText.substring(0,150);
			}
			//if (EXHCHECK) System.out.println(" Caption Text nr: " + exhibit + " / " + captionText);
		} catch (NullPointerException ime) {
			System.out.println(" NullPointerException - checkExhibitContent - exhibit nr: " + exhibit 
					+ " Path: " + filePath);
		}
		return filePath;
	}

/**
 * createNewThumb(String filePath)
 * @param filePath
 * @return byte[] image
 */
/*  From TMG IFILENAME create its thumbnail using:
	BufferedImage thumbnail = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	Graphics2D g = thumbnail.createGraphics();
	g.drawImage(originalImage, 0, 0, WIDTH, HEIGHT, null);
	g.dispose();

	*** or if you want to get a bit more sophisticated use bilinear interpolation (little bit slower):
	g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	*** or even bicubic interpolation (little bit slower):
	g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 */
	private byte[] createNewImageThumb(int exhibitIndex, String filePath) {
		int WIDTH = 160, HEIGHT  = 200;
	    File imageFile = new File(filePath);
	    Image originalImage = null;
		try {
			BufferedImage thumbnail = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
			originalImage = ImageIO.read(imageFile);
			Graphics2D g = thumbnail.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.drawImage(originalImage, 0, 0, WIDTH, HEIGHT, null);
			g.dispose();
			ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
			ImageIO.write(thumbnail, "jpg", byteOutStream);
			return byteOutStream.toByteArray();
		} catch (Exception all) {
			errorExhibits++;
			if (EXHCHECK) {
				if (all instanceof IOException)
					System.out.println(" IOExcption - createNewImageThumb exhibit nr: " 
							+ exhibitIndex + " Message: " + all.getMessage() + "\n Path: " + filePath);
				else if (all instanceof IllegalArgumentException)
					System.out.println(" IllegalArgumentException - createNewImageThumb exhibit nr: " 
							+ exhibitIndex + " Message: " + all.getMessage() + "\n Path: " + filePath);
				else  all.printStackTrace();
			}
			HB0711Logging.logWrite("ERROR Creating Thumb Image at I.dbf index: " + exhibitIndex + " Path: " +filePath + ": " + all.getMessage());
			return null;
		} 
	}

/**
 * createExhibitText(int exhibitIndex, String filePath)
 * @param exhibitIndex
 * @param filePath
 * @return
 */
	private byte[] createExhibitText(int exhibitIndex, String filePath) {
		String textData = "";
		try {
	        File textFile = new File(filePath);
	        Scanner fileReader = new Scanner(textFile);
	        while (fileReader.hasNextLine())
	        	textData = textData + fileReader.nextLine();
		    fileReader.close();
			if (!filePath.endsWith(".txt")) {
				if (EXHCHECK)
					System.out.println(" No plain text in file: " + filePath);
				textData = "Filepath: " + filePath;
			}
			return textData.getBytes();
	     } catch (FileNotFoundException fnfe) {
				errorExhibits++;
				if (EXHCHECK) System.out.println(" TMGpass_V22a_Exhibits - newText - IOE error: "
						+ exhibitIndex + " - " + filePath);
				return null;
	     }
	}

/**
 * This is a method to 
 * convertFilePath(String filePath)
 * @param filePath
 * @return new filepath
 */
	private String convertFilePath(String filePath) {
		//if (filePath.startsWith("E:\\Genealogy") || filePath.startsWith("e:\\Genealogy")) {
	    if (filePath.startsWith("c:\\Users\\don")) {
			String path = filePath.replace('\\', '|');
			String[] filePathElements = path.split("\\|");
			filePath =  newExhibitFolderPath
					+ filePathElements[filePathElements.length-2].trim() + "\\"
					+ filePathElements[filePathElements.length-1].trim();
		}
		//System.out.println(" Convert path nr:  " + idExhibit 
		//		+ " path: " + filePath + " length: " + filePath.length());
		return filePath;
	}

/**
 * dumpExhibitRecordFromTMG(DBFRow tmgIrow)
 * @param personNr
 */
	private void dumpExhibitRecordFromTMG(DBFRow tmgIrow) {
		System.out.println(" IDEXHIBIT: " + tmgIrow.getInt("IDEXHIBIT"));
		System.out.println(" RLNUM: " + tmgIrow.getInt("RLNUM"));
		System.out.println(" XNAME: " + tmgIrow.getString("XNAME"));
		System.out.println(" DESCRIPT: " + tmgIrow.getString("DESCRIPT").trim());
		System.out.println(" PRIMARY: " + tmgIrow.getBoolean("PRIMARY"));
		System.out.println(" IFILENAME: " + tmgIrow.getString("IFILENAME"));
		System.out.println(" PROPERTY: " + tmgIrow.getString("PROPERTY"));
		String textString = tmgIrow.getString("TEXT");
		System.out.println(" TEXTEXHIBIT: \n" + textString);
		System.out.println();
	}

} // ********* End class
