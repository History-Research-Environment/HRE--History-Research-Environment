package hre.tmgjava;
/***************************************************************************************
 * Uses library com.linuxense.javadbf
 * Java library for reading and writing Xbase (dBase/DBF) files
 * https://github.com/albfernandez/javadbf
 * albfernandez/javadbf is licensed under the
 * GNU Lesser General Public License v3.0
 * Written by Alberto Fern�ndez
 * **************************************************************************************
 * TMGfileDump write TMG tables to a CSV file for EXEL
 * v0.00.0032 2026-02-04 - First version (N. Tolleshaug)
 * 			  2026-02-07 - Updated csv export (N. Tolleshaug)
 * 			  2026-02-08 - Added TMG file selection(N. Tolleshaug)
 * 			  2026-02-12 - Added SEP=^  as TMG csv separator(N. Tolleshaug)
 ****************************************************************************************/

import java.awt.Dialog.ModalityType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.linuxense.javadbf.DBFDataType;
import com.linuxense.javadbf.DBFReader;

import hre.nls.HG0620Msgs;
import net.miginfocom.swing.MigLayout;

public class TMGfileDump {
	PrintWriter writer;
	DBFReader DBFreader;
	FileWriter fileWriter;
	FileInputStream inputStream;
	private static String tmgStartFolder;
    String TMGpath = "C:\\Users\\Nils\\Documents\\The Master Genealogist v9\\Projects\\";
    String dbfFile, csvFile , dbMemoFile;
    String csvFilePath, dbfFileName, dbfFilePath;
    DBFDataType[] dbfTypeName;
    String[] dbfFieldName;
    boolean memoNeeded = false;
    
/**
 * public TMGfileDump()  constructor  
 */
    public TMGfileDump(boolean callConstructor) {
    	if (callConstructor) {
			int  errorCode = selectTMGfile();
			TMGfileConvert_csv(dbfFilePath, csvFilePath);
			if (errorCode == 0)
			JOptionPane.showMessageDialog(null, "Finished TMG table export to CSV \n"
					+ dbfFilePath, // Finished TMG table export to CSV \n
				   "Maintenance", // Maintenance
				   JOptionPane.INFORMATION_MESSAGE);
			else System.out.println("Dump TMG file table failed!");
    	}
    }
    
/**
 * public void TMGfileDump_csv(String tmgdDBFile, String csvOUTFile)    
 * @param tmgdDBFile
 * @param csvOUTFile
 */
	public void TMGfileConvert_csv(String tmgdDBFile, String csvOUTFile) {

		dbfFile = tmgdDBFile;
		dbMemoFile = tmgdDBFile.replace(".dbf",".fpt");
		csvFile = csvOUTFile;
		
		System.out.println(" TMGfileDump - TMG file: " + dbfFile);
		System.out.println(" TMGfileDump - TMG memo: " + dbMemoFile);
		System.out.println(" TMGfileDump - EXEL csv: " + csvFile);
		
        System.out.println(" Start Conversion!");
    	try {
			inputStream = new FileInputStream(dbfFile);
			fileWriter = new FileWriter(csvFile);
			writer = new PrintWriter(fileWriter);
			DBFreader = new DBFReader(inputStream);
			
        // Write Header
			int numberOfFields = DBFreader.getFieldCount();
			dbfTypeName = new DBFDataType[numberOfFields];
			dbfFieldName = new String[numberOfFields];
			writer.println("SEP=^");
            for (int i = 0; i < numberOfFields; i++) {
            	dbfTypeName[i] = DBFreader.getField(i).getType();
            	dbfFieldName[i] = DBFreader.getField(i).getName();
            	if (dbfTypeName[i] == DBFDataType.MEMO) memoNeeded = true;
            	writer.print(DBFreader.getField(i).getName() + (i == numberOfFields - 1 ? "" : "^"));
            	//if (TMGglobal.DEBUG)
            		System.out.println(" Data Col nr: " + i + " - " + DBFreader.getField(i).getName()
            			+ "/" + DBFreader.getField(i).getType());
            }
            writer.println();
       // if memo file present activate memo file
            if (memoNeeded) DBFreader.setMemoFile(new File(dbMemoFile),true);

        // Write Rows
            int rowNr = 0;
            Object[] rowObjects;
            while ((rowObjects = DBFreader.nextRecord()) != null) {
            	rowNr++;
                for (int i = 0; i < rowObjects.length; i++) {
                	if (dbfTypeName[i] == DBFDataType.MEMO) {
                		System.out.println(" " + rowNr + " Memo: " + i + " / " + rowObjects[i]);
                		if (!dbfFieldName[i].equals("IMAGE") && !dbfFieldName[i].equals("THUMB"))
                			rowObjects[i] = ((String) rowObjects[i]).replace('"','#').trim();
                		writer.print("\"" + rowObjects[i] + "\"" + (i == rowObjects.length - 1 ? "" : "^"));
                	} else if (dbfTypeName[i] == DBFDataType.CHARACTER) {
                		writer.print("\"" + (String)rowObjects[i] + "\"" + (i == rowObjects.length - 1 ? "" : "^"));
                	} else if (dbfTypeName[i] == DBFDataType.NUMERIC) {
                		writer.print(rowObjects[i] + (i == rowObjects.length - 1 ? "" : "^"));
                	} else if (dbfTypeName[i] == DBFDataType.LONG) {	
                		writer.print(rowObjects[i] + (i == rowObjects.length - 1 ? "" : "^"));
                	} else if (dbfTypeName[i] == DBFDataType.LOGICAL) {
                		//System.out.println(" " + rowNr + " PRIMARY: " + i + " / " + rowObjects[i]);
                		if (rowObjects[i] == null) {
                			rowObjects[i] = "  ";
                			writer.print((String)rowObjects[i] + (i == rowObjects.length - 1 ? "" : "^"));
                		} else writer.print((boolean)rowObjects[i] + (i == rowObjects.length - 1 ? "" : "^"));
                	} else {
                		writer.print(rowObjects[i] + (i == rowObjects.length - 1 ? "" : "^"));
                	}
                }
                writer.println();
            }
            writer.close();
            DBFreader.close();
            System.out.println(" Conversion complete!");
		} catch (IOException ioe) {
			System.out.println(" Conversion TMGfileDump error: " + ioe.getMessage());
			ioe.printStackTrace();
		}
    }
	
/**
 * public int selectTMGfile()	
 * @return
 */
	public int selectTMGfile() {
		String csvFileFolder = System.getProperty("user.home") + File.separator	//$NON-NLS-1$
				+ "HRE" + File.separator;	//$NON-NLS-1$
		int selection;
		String selectedItem, tmgTableName, tmgFileLetter;
		String[] itemElements, tmgFileNames = TMGtypes.TMGfiles;; 
		tmgStartFolder = TMGglobal.tmgStartFolder;
		JComboBox<String> comboTMGtables = new JComboBox<String>(tmgFileNames);
	// Build a JPanel to hold everything
		String instructions = HG0620Msgs.Text_27	// Select the required table from the list below
				+ "and then select the TMG project (PJC) file"; // and then select the TMG project (PJC) file
		String title = "Select Table to extract from TMG";	// Select Table to extract from TMG
		JPanel msgPanel = new JPanel();
		msgPanel.setLayout(new MigLayout("insets 5", "[]", "[]10[][]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		JLabel label = new JLabel(instructions);
		msgPanel.add(label, "cell 0 0");			//$NON-NLS-1$
		msgPanel.add(comboTMGtables, "cell 0 1");		//$NON-NLS-1$
	// Show the JOptionPane msg
		selection = JOptionPane.showConfirmDialog(null, msgPanel, title, JOptionPane.OK_CANCEL_OPTION);
	// If Cancel, exit now
		if (selection == 2) return 2;
		else System.out.println(" Action copy to HRE folder: " + csvFileFolder);
		selectedItem = (String) comboTMGtables.getSelectedItem();
		itemElements = selectedItem.split("=");
		tmgTableName = itemElements[0].trim();
		tmgFileLetter = itemElements[1].trim();
	// Select TMG file to dump from file choooser
		chooseTMGfolder();
		dbfFileName = TMGglobal.chosenFilename.substring(0, TMGglobal.chosenFilename.length()-5);
		dbfFilePath = TMGglobal.chosenFolder + "\\" + dbfFileName + tmgFileLetter + ".dbf";
		csvFilePath = csvFileFolder + "TMG_" + dbfFileName + tmgTableName + ".csv";
		return 0;
	}
	
/**
 *  private void chooseTMGfolder()
 */
	private void chooseTMGfolder() {
		TMGfolderChooser chooseFile = new TMGfolderChooser("Select", "TMG files (*.pjc)", "pjc", null, tmgStartFolder, 1);
		chooseFile.setModalityType(ModalityType.APPLICATION_MODAL);
		chooseFile.setLocation(600, 200);  // Sets chooser screen top-left corner relative to that
		chooseFile.setVisible(true);
	}
}
