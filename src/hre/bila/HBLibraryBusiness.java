package hre.bila;
/*****************************************************************************
 * Process SQL requests to HRE database
 * Library for for HBProjectHandler
 *****************************************************************************
 * v0.00.0017 2020-01-27 First version (N. Tolleshaug)
 * v0.00.0017 2020-02-06 Corrected error opening empty database or no
 * 						   content in database tables (N. Tolleshaug)
 * v0.00.0018 2020-03-17 Corrected error New Project in updateUserInTable
 * v0.00.0023 2020-09-13 Cleanup of pointers to HBBusinessLayer
 * v0.00.0025 2021-01-22 Add creating zip file from External folder (D. Ferguson)
 * 			  2021-02-04 Add unzipping Ext. file folder to target location (D Ferguson)
 *            2021-04-15 Added QwikSort for HDATE sort. (N.Tolleshaug)
 * v0.01.0026 2021-05-16 Included both DDL20a and DDL21a (N. Tolleshaug)
 * 			  2021-05-27 Modified getDatabaseFilePath (N. Tolleshaug)
 * v0.01.0030 2023-08-24 HDate formated input from GUI (N. Tolleshaug)
 * 			  2023-08-25 Updated date pattern parcer (N. Tolleshaug)
 * v0.01.0031 2023-12-08 class HREmemo extends HBBusinessLayer (N. Tolleshaug)
 *****************************************************************************
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import hre.dbla.HDDatabaseLayer;
import hre.dbla.HDException;
import hre.gui.HGlobal;
import hre.tmgjava.HCException;

/**
 * class HBLibraryBusiness
 * @author NTo
 * @since 2019-12-20
 */
public class HBLibraryBusiness {

	HBBusinessLayer pointBusinessLayer = null;
	HBLibraryResultSet pointLibraryResultSet;
	public HdateSort sorter;

	long null_RPID  = 1999999999999999L;
	long proOffset  = 1000000000000000L;

	long [] baseTypePID = new long[8];
	long [] subTypePID = new long[4];
	long baseTypeRPID = null_RPID;
	long subTypeRPID = null_RPID;

//	public static HG058705ActionProgress pointProgressDisplay;

/**
 *
 * @param array
 * @return
 */
	public int[] sort(String[] array) {
		return sorter.sort(array);
	}

/**
 * Constructor HBLibraryBusiness(HBBusinessLayer pointBusinessLayer)
 * @param pointBusunessLayer
 */
	public HBLibraryBusiness(HBBusinessLayer pointBusinessLayer) {
		this.pointBusinessLayer = pointBusinessLayer;
		this.pointLibraryResultSet = pointBusinessLayer.pointLibraryResultSet;
		sorter = new HdateSort();
	}

/**
 * findSubTypePID()
 * @return
 * @throws HBException
 */
	public void findBaseSubTypePID(int select, int dataBaseIndex) throws HBException {
		boolean dump = false;
		//boolean dump = true;
		int subType, baseType;
		String selectSQL = pointBusinessLayer.setSelectSQL("*", pointBusinessLayer.entityTypeDefinition, "");
	// Find the slected project database index
		try {
			ResultSet researchTypeRPID = pointBusinessLayer.requestTableData(selectSQL, dataBaseIndex);
			researchTypeRPID.beforeFirst();
			if (dump) {
				System.out.println("************************");
			}
			while (researchTypeRPID.next()) {
				baseType = researchTypeRPID.getInt("BASE_TYPE");
				subType = researchTypeRPID.getInt("SUB_TYPE");
				if (subType == 0) {
					baseTypePID[baseType] = researchTypeRPID.getLong("PID");
					if (dump) {
						System.out.println(" BaseType: " + baseType + " PID: " + baseTypePID[baseType]
								+ " - " + researchTypeRPID.getString("FIRST_LEVEL_DEFN_TRAN").split("\\|")[0]);
					}
				}
			}
			researchTypeRPID.beforeFirst();
			if (dump) {
				System.out.println("************************");
			}
			while (researchTypeRPID.next()) {
				baseType = researchTypeRPID.getInt("BASE_TYPE");
				subType = researchTypeRPID.getInt("SUB_TYPE");
				if (baseType == select && subType != 0) {
					if (subType > 9) {
						subType = subType - baseType*10;
					}
					subTypePID[subType] = researchTypeRPID.getLong("PID");
					if (dump) {
						System.out.println(" SubType: " + subType + " PID: " + subTypePID[subType]
									+ " - " + researchTypeRPID.getString("SECND_LEVEL_DEFN_TRAN").split("\\|")[0]);
					}
				}
			}

		} catch (SQLException sqle) {
			System.out.println(" HBMediaHandler - findBaseSubTypePID " + sqle.getMessage());
			sqle.printStackTrace();
		}
	}

/**
 * findGreatestVisibleId(ResultSet personTable)
 * @param personTable
 * @return
 * @throws HBException
 */
	public static int findGreatestVisibleId(ResultSet hreTable) throws HBException {
		int gretestVisibleId = 0, visibleId = 0;
		// process location data
		try {
			hreTable.beforeFirst();
			while (hreTable.next()) {
				visibleId = (int) hreTable.getLong("VISIBLE_ID");
				if (visibleId > gretestVisibleId) {
					gretestVisibleId = visibleId;
				}
			}
			return gretestVisibleId;
		} catch (SQLException hbe) {
			throw new HBException(" findGreatestVisibleId: " + hbe.getMessage());
		}
	}

/**
 * Only used for v21a - DEPRECIATED
 * setUpStyleString(String [] personData, int[] dataStyle)
 * @param personData
 * @param dataStyle
 * @return styleString
 */
	public String setUpStyleString(String [] personData, int[] dataStyle) {
		String place = " ";
		boolean first = false;
		if (personData != null) {
			for ( int i = 1; i < dataStyle.length; i++) {
				if (personData[dataStyle[i]].length() > 0) {
					if (first) {
						place = place + ", ";
					}
					first = true;
					place = place + personData[dataStyle[i]];
				}
			}
		}
		return place;
	}

/**
 * Modify filename to match SQL connect to database
 * @param chosenName
 * @param databaseEngine
 * @return
 */
		public String modifyFileNameChosen(String chosenName, String databaseEngine) {
		String modifiedName = chosenName;
		if (databaseEngine.equals("H2")) {
			modifiedName = modifiedName.replace(".mv.db"," ");
			modifiedName = modifiedName.trim();
		} else if (databaseEngine.equals("MySQL")) {
			modifiedName = modifiedName.replace(".mv.db"," ");
			modifiedName = modifiedName.trim();
			modifiedName = "MySQL filename modification: " + chosenName;
		} else 	{ // HRE database
			modifiedName = modifiedName.replace(".mv.db"," ");
			modifiedName = modifiedName.trim();
			modifiedName = "Unknown filename modification: " + chosenName;
		}
		return modifiedName;
	}

/**
 * Set up parameters for file chooser
 * @param startFolder
 * @param databaseEngine
 * @return String[] parameters
 */

	public String[] setUpFileRestoreChooser(String startFolder,String databaseEngine) {
		return setUpFileChooser(startFolder,"HRE Project backup files (*.hrez)", "hrez", "1",databaseEngine);
	}
	public String[] setUpFolderBackupChooser(String startFolder,String databaseEngine) {
		return setUpFileChooser(startFolder,"HRE Project backup folder (*.hrez)", "hrez", "0",databaseEngine);
	}
	public String[] setUpFolderChooser(String startFolder,String databaseEngine) {
		return setUpFileChooser(startFolder,"HRE Project Restore folder (*.db)", "db", "0",databaseEngine);
	}
	public String[] setUpFileOpenChooser(String startFolder,String databaseEngine) {
		return setUpFileChooser(startFolder,"HRE Project files (*.db)", "db", "1",databaseEngine);
	}

	private static String[] setUpFileChooser(String startFolder,String fileType, String fileExtension, String mode, String databaseEngine) {
		String[] fileChooserParams = new String[6];
		if (databaseEngine.equals("H2")) {
			String[] h2Params = {"Select",fileType,fileExtension,"",startFolder,mode};
			fileChooserParams = h2Params;
		} else if (databaseEngine.equals("MySQL")) {
			String[] mySQLparams = {"","","","",null,"1"};
			fileChooserParams = mySQLparams;
		} else 	{
			String[] hreParams = {"","","","","","1"};
			fileChooserParams = hreParams;
		}
		return fileChooserParams;
	}

/**
 * Get database file path from project array
 * @param projectData - array with new project data
 * @return - database file path
 */
	public String getDatabaseFilePath(String [] projectData) {
		return getDatabaseFilePath(projectData[2], projectData[1]);
	}

	public String getDatabaseFilePath(String folderPath, String fileName) {
		return  folderPath + File.separator + fileName;
	}

/**
 * Copy file to new location
 * Source file not deleted
 * Example:
 * Files.copy(Paths.get("C:/Users/Nils/HRE/HRE Seed DB v16d.mv.db"),
 *		Paths.get("C:/Users/Nils/Documents/Utvikling/HRE-project/Alfatest-HRE/Databases/Seed-v16d.mv.db"));
 */
	public void copyFile(String sourceFilePath,String copyFilePath) throws HBException  {
	        try {
				Files.copy(Paths.get(sourceFilePath),
	    				Paths.get(copyFilePath));
	        } catch (NoSuchFileException nsfe) {
	        	if (HGlobal.DEBUG) {
					System.out.println("copyFile - No such file: " + nsfe.getMessage());
				}
				throw new HBException("ERR1-No such file: \n" + nsfe.getMessage());
	        } catch (FileAlreadyExistsException fae) {
	        	if (HGlobal.DEBUG) {
					System.out.println("copyFile - File already exists " + fae.getMessage());
				}
				throw new HBException("ERR2-File already exist: \n" + fae.getMessage());
	        } catch (IOException ioe) {
	        	if (HGlobal.DEBUG) {
					System.out.println("copyFile - Copy file IOException: " + ioe.getMessage());
				}
				throw new HBException("ERR3-Copy file IOException: \n" + ioe.getMessage());
			}
		}

/**
 * Delete file in folder
 * @param sourceFilePath
 * @throws HBException
 */
	public void deleteFile(String sourceFilePath) throws HBException {
        try {
        // delete also trace file is exists
        	if (sourceFilePath.endsWith("trace.db")) {
				Files.deleteIfExists(Paths.get(sourceFilePath));
			} else {
				Files.delete(Paths.get(sourceFilePath));
			}
        } catch (NoSuchFileException nsfe) {
        	if (HGlobal.DEBUG) {
				System.out.println("No such file: \n" + nsfe.getMessage());
			}
			throw new HBException("Cannot delete non existing file \n" + nsfe.getMessage());
        } catch (IOException ioe) {
        	if (HGlobal.DEBUG) {
				System.out.println("Delete file IOException: \n" + ioe.getMessage());
			}
			throw new HBException("Delete file IOException: \n" + ioe.getMessage());
		}
	}


/**
 * Only for reference - 2020-03-15 N. Tolleshaug
 * Start CopyFile as new Thread with update to
 * @param sourceFile
 * @param destinationFile
 * @throws HBException

	public void setupUpCopyFile (File sourceFile, File destinationFile) throws HBException {
		pointProgressDisplay = new HG058705ActionProgress();
		pointProgressDisplay.setModalityType(ModalityType.APPLICATION_MODAL);

	// Position set from parent window and stored in HGlobal.ButtonXY
		Point xy = TMGGlobal.ButtonXY;   // Get button location
		pointProgressDisplay.setLocation(xy.x+100, xy.y-200);

	// Start new Thread with Progress Monitor
		Thread monitor = new Thread(pointProgressDisplay);
		monitor.start();

	// Start copy process as HBLibraryBusiness
		HBCopyFileProcess task = new HBCopyFileProcess(sourceFile, destinationFile,this);
        task.addPropertyChangeListener(pointProgressDisplay);
        task.execute();
	}

	public void stopProcess() {
		pointProgressDisplay.dispose();
	}
*/

/**
 * Start CopyFile as new Thread with update to
 * @param sourceFile
 * @param destinationFile
 * @throws HBException

	public void setupUpCopyFile (File sourceFile, File destinationFile) throws HBException {
		pointProgressDisplay.setVisible(true);
		copyFile(sourceFile,destinationFile,pointProgressDisplay);
	}
*/
/**
 * Copy file with progress monitor update
 * @param sourceFile
 * @param destinationFile
 * @throws HBException

	public void copyFile(File sourceFile, File destinationFile,HG058705ActionProgress pointProgressDisplay) throws HBException {
		copyFile(sourceFile.getPath(), destinationFile.getPath(),pointProgressDisplay);
	}
*/
/**
 * Copy file with progress indicator
 * @param sourcePath
 * @param destinationPath
 * @throws HBException


	public static void copyFile(String sourcePath, String destinationPath,
								HG058705ActionProgress pointMonitorProcess) throws HBException {
		File src = new File(sourcePath);
		File dst = new File(destinationPath);
		long timeElapsed = 0;
		long expectedBytes = 0;
		try {
			TimeUnit.MILLISECONDS.sleep(10);
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dst);
		// Transfer bytes from in to out
			expectedBytes = src.length(); // This is the number of bytes we expected to copy..
			pointMonitorProcess.setFilelength("" + expectedBytes);
			pointMonitorProcess.setContextOfAction("Copy From file: \n" + sourcePath + "\nTo file:\n" + destinationPath);
			long totalBytesCopied = 0; // This will track the total number of bytes we've copied
			byte[] buf = new byte[1024];
			int len = 0;
			long startTime = System.nanoTime();
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
				totalBytesCopied += len;
				int progress = (int)Math.round(((double)totalBytesCopied / (double)expectedBytes) * 100);
				pointMonitorProcess.setProgress(progress);
				timeElapsed = System.nanoTime() - startTime;
				if (HGlobal.DEBUG) System.out.println("Execution time in milliseconds: " + timeElapsed / 1000000);
				if (HGlobal.DEBUG) TimeUnit.MILLISECONDS.sleep(1);
			}
			timeElapsed = System.nanoTime() - startTime;
			if (HGlobal.DEBUG) System.out.println("Execution time in milliseconds: " + timeElapsed / 1000000);
			in.close();
			out.close();
			pointMonitorProcess.progressClose();
			JOptionPane.showMessageDialog(null,"Copy File Process Completed\nFile:  " + src.getName() +
					"\nFile size: " + expectedBytes / 1000 + " Kb"
					+ "\nTime used: " + timeElapsed / 1000000 + " ms",
					"Copy Project As ",JOptionPane.INFORMATION_MESSAGE);
		} catch(IOException iox) {
			if (HGlobal.DEBUG) System.out.println("HBLibraryDatabase - IOException\n" + iox.getMessage());
			throw new HBException("HBLibraryDatabase - IOException\n" + iox.getMessage());
			//iox.printStackTrace();
		} catch (InterruptedException inte) {
			if (HGlobal.DEBUG) System.out.println("CopyFile - InterruptedException: \n"+ inte.getMessage());
			inte.printStackTrace();
		}
	}
	*/
/**
 * Create zip file
 * @param fileSource
 * @param zipFile
 */
	public void createZipFile(String filSource, String zipFile) throws HBException {
		try {
			FileOutputStream fos = new FileOutputStream(zipFile);
			ZipOutputStream zos = new ZipOutputStream(fos);
			File zipFrom = new File(filSource);
			addToZipFile(zipFrom , zos);
			zos.close();
			fos.close();
		} catch (FileNotFoundException fnfe) {
			throw new HBException("Add to zip file not found error:\n" + fnfe.getMessage());
		} catch (IOException ioe) {
			throw new HBException("Add to zip file error:\n" + ioe.getMessage());
		}
	}

/**
 * Add file to zip file
 * @param sourceFile - database file to be zipped
 * @param zos - ZipOutputStream
 * @throws FileNotFoundException
 * @throws IOException
 */
	public static void addToZipFile(File sourceFile, ZipOutputStream zos) throws HBException {

		if (HGlobal.DEBUG) {
			System.out.println("Writing '" + sourceFile.getName() + "' to zip file");
		}

		FileInputStream fis;
		try {
			fis = new FileInputStream(sourceFile.getCanonicalFile());

			ZipEntry zipEntry = new ZipEntry(sourceFile.getName());
			zos.putNextEntry(zipEntry);

			byte[] bytes = new byte[1024];
			long transferred = 0;
			int length;
			while ((length = fis.read(bytes)) >= 0) {
				zos.write(bytes, 0, length);
				transferred = transferred + length;
				if (HGlobal.DEBUG) {
					System.out.println("Number of bytes: " + transferred);
				}
			}
			zos.closeEntry();
			fis.close();
		} catch (IOException ioe) {
			throw new HBException("Add to zip file error:\n" + ioe.getMessage());
		}
	}

/**
 * Zip a folder's contents (including sub-folders) into a zip file
 * @param folder - full path of folder to be zipped
 * @param outputZip - ZipOutputStream file and path
 * @throws IOException
 */
	public void zipFolder (String folder, String outputZip) throws HBException {
		  try (
			  FileOutputStream fos = new FileOutputStream(outputZip);
		      ZipOutputStream zos = new ZipOutputStream(fos)) {
		      Path sourcePath = Paths.get(folder);
		      // using WalkFileTree to traverse directory
		      Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
		    	   @Override
		    	   public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
		    		   zos.putNextEntry(new ZipEntry(sourcePath.relativize(dir).toString() + File.separator));
		    		   zos.closeEntry();
		    		   return FileVisitResult.CONTINUE;
		    	   }
		    	   @Override
		    	   public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
		    		   zos.putNextEntry(new ZipEntry(sourcePath.relativize(file).toString()));
		    		   Files.copy(file, zos);
		    		   zos.closeEntry();
		    		   return FileVisitResult.CONTINUE;
		    	   }
		      });
		    } catch (IOException zoe) {
		    	throw new HBException("External folder backup to zip file error: \n" + zoe.getMessage());
		    }
		}

/**
 * Extract file from .zip
 * @param sourceZipFile - source zip file
 * @param restorePath - restore to path
 * @throws HBException
 */
	public void extractZip(String sourceZipFile, String restorePath) throws HBException {
		try {
			ZipFile zipFile = new ZipFile(sourceZipFile);
			Enumeration<?> enu = zipFile.entries();
			while (enu.hasMoreElements()) {
				ZipEntry zipEntry = (ZipEntry) enu.nextElement();

				String name = zipEntry.getName();
				long size = zipEntry.getSize();
				long compressedSize = zipEntry.getCompressedSize();
				if (HGlobal.DEBUG) {
					System.out.printf("Extracted file: %-20s | size: %6d | compressed size: %6d\n",
							name, size, compressedSize);
				}

				// If parameter restorePath is a complete filepath+name ending in '.mv.db'
				// then use it. Else construct it from restorePath + originally zipped filename
				String restoretoPathFileName = "";
				if (restorePath.endsWith(".mv.db")) {
					restoretoPathFileName = restorePath;
				} else {
					restoretoPathFileName = restorePath + File.separator + name;
				}
				File file = new File(restoretoPathFileName);

				InputStream is = zipFile.getInputStream(zipEntry);
				FileOutputStream fos = new FileOutputStream(file);
				long transferred = 0;
				byte[] bytes = new byte[1024];
				int length;
				while ((length = is.read(bytes)) >= 0) {
					fos.write(bytes, 0, length);
					transferred = transferred + length;
				}
				if (HGlobal.DEBUG) {
					System.out.println("Unzipped number of bytes: " + transferred);
				}
				is.close();
				fos.close();
			}
			zipFile.close();
		} catch (IOException ioe) {
			throw new HBException("Extract from zip file error:\n" + ioe.getMessage());
		}
	}

/**
 * UnZip a folder's contents (including sub-folders) from a zip file
 * @param fromZip - String of path of file to be unzipped
 * @param toFolder - String of path of folder to be unzipped into
 * @throws HBException
 */
	public void extractZipFolder(String fromZip, String toFolder) throws HBException {
		Path source = Paths.get(fromZip);
		Path target = Paths.get(toFolder);
	    try (
	    	ZipInputStream zis = new ZipInputStream(new FileInputStream(source.toFile()))) {
	        // list files in zip
	        ZipEntry zipEntry = zis.getNextEntry();

	        while (zipEntry != null) {
	        	// Ignore entries that are just '/'
				String fileName = zipEntry.getName();
		        if (!fileName.equals(File.separator)) {
		                boolean isDirectory = false;
		                // Case 1 - some zips store files and folders separately
		                // e.g data/
		                //     data/folder/
		                //     data/folder/file.txt
		                // Also zipEntry file separator may not be this OS's separator, so test for both
		                if (zipEntry.getName().endsWith("/") || zipEntry.getName().endsWith("\\")) {
							isDirectory = true;
						}
		                // Now test for 'zip slip'
		                Path newPath = zipSlipProtect(zipEntry, target);
		                // IF OK, carry on
		                if (isDirectory) {
							Files.createDirectories(newPath);
						} else {
		                			// Case 2 - some zips store file path only, so create parent directories
		                			// e.g data/folder/file.txt
		                			if (newPath.getParent() != null) {
		                					if (Files.notExists(newPath.getParent())) {
		                						Files.createDirectories(newPath.getParent());
		                					}
		                			}
		                			// Finally, copy file
		                			Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
		                		}
                	}
                zipEntry = zis.getNextEntry();
	        }
	        zis.closeEntry();
	    	} catch (IOException ioe) {
	        	throw new HBException("Extract from zip folder error:\n" + ioe.getMessage());
				}
	    }
	    // Routine to protect from zip slip attack
	    public static Path zipSlipProtect(ZipEntry zipEntry, Path targetDir) throws HBException {
	        // test zip slip vulnerability
	        // Path targetDirResolved = targetDir.resolve("../../" + zipEntry.getName());
	        Path targetDirResolved = targetDir.resolve(zipEntry.getName());
	        // make sure normalized file still has targetDir as its prefix, else throws exception
	        Path normalizePath = targetDirResolved.normalize();
	        if (!normalizePath.startsWith(targetDir)) {
	            throw new HBException("Extract from zip folder; bad zip entry:\n" + zipEntry.getName());
	        }
	        return normalizePath;
	    }
} // End HBLibraryBusiness

class HdateSort {

    String[] names;
    int length;
    int[] index;

/**
* Test of sorter - not normally used
* testSort()
*/
    public void testSort() {
        String words[] = {
        		"1000000000000007", 										// irregular
        		"50000001199225511111133000000000000000000500000000000", 	// exact date 1925-11-13
        		"50000001199225511111133000000000000000000510000000000",	// exact date 1925-11-13 with ?
        		"50000001177664400000000000000000000000000100000000000", 	// before 1764
        		"50000001188995500551111000000000000000000600000000000", 	// after 1883
        		"50000001188883300000000000000000000000000200000000000", 	// say 1884
        		"50000001188227700000000000000000000000000400000000000",	// circa 1827
        		"50000001188355000000000000000000000000000700000000000",	// between 1835 and 1850
        		"50000001188355000000000000000000000000000800000000000",	// either 1835 or 1850
        		"50000001188355000000000000000000000000000900000000000"		// from 1835 to 1850
        		};

        int[] number = {1,2,3,4,5,6,7,8,9,10};

        // Display input order
        System.out.println("Input:");
        int nr = 0;
        for (String i : words) {
            System.out.print(number[nr] + "-"  +  i);
            nr++;
            System.out.println();
        }

        // Sort
        //sorter.sort(words);
        sort(words);

        //Display output order
        System.out.println("Output:");
        nr = 0;
        for (String j : words) {
            System.out.print(index[nr] + "-" + j);
            nr++;
            System.out.println();
        }
        System.out.println();
    }

/**
* public int[] sort(String[] array)
* @param array
* @return
*/
	public int[] sort(String[] array) {
	    if (array == null || array.length == 0) {
	        return null;
	    }
	    this.index = new int[array.length];
	    for (int i= 0; i < array.length; i++) {
			this.index[i] = i;
		}
	    this.names = array;
	    this.length = array.length;
	    //this.index = index;
	    quickSort(0, length - 1);
	    return index;
	}
/**
* quickSort(int lowerIndex, int higherIndex)
* @param lowerIndex
* @param higherIndex
*/
	private void quickSort(int lowerIndex, int higherIndex) {
	    int i = lowerIndex;
	    int j = higherIndex;
	    String pivot = this.names[lowerIndex + (higherIndex - lowerIndex) / 2];

	    while (i <= j) {
	        while (this.names[i].compareToIgnoreCase(pivot) < 0) {
	            i++;
	        }

	        while (this.names[j].compareToIgnoreCase(pivot) > 0) {
	            j--;
	        }

	        if (i <= j) {
	            exchangeNames(i, j);
	            i++;
	            j--;
	        }
	    }

	//call quickSort recursively
	    if (lowerIndex < j) {
	        quickSort(lowerIndex, j);
	    }
	    if (i < higherIndex) {
	        quickSort(i, higherIndex);
	    }
}

/**
* exchangeNames(int i, int j)
* Swap in tables arrays
* @param i
* @param j
*/
	private void exchangeNames(int i, int j) {
	    String temp = this.names[i];
	    int tempIn = this.index[i];
	    this.names[i] = this.names[j];
	    this.index[i] = this.index[j];
	    this.names[j] = temp;
	    this.index[j] = tempIn;
	}
} // HdateSort

/**
 * Converter from TMG date to HRE HDate
 * v0.00.0030 2023-04-24 - Implemented according to T170
 * @author NTo
 * @Since  2023-04-23
 */

class HdateInput {

	static long main_years = 0, extra_years = 0;
	static String main_details = "", extra_details = "", sort_date_code = "";

	static int nrOldStyles = 0;
	static int nrIrrDates = 0;
	static int nrQestionMarks = 0;

	static long proOffset = 1000000000000000L;
	static long null_RPID  = 1999999999999999L;

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

/**
 * updateT170_HDATES(ResultSet hreTable, String hdate)
 * @param hreTable
 * @param tmgdate
 * @return
 * @throws SQLException
 * @throws HCException
 */

	public static int updateT170_HDATES(ResultSet hreTable, Object[] inputHdate) throws HBException {
		main_years = (long) inputHdate[0];
		main_details = (String) inputHdate[1];
		extra_years = (long) inputHdate[2];
		extra_details = (String) inputHdate[3];
		sort_date_code = (String) inputHdate[4];
		return updateT170_HDATES(hreTable, "");
	}
	public static int updateT170_HDATES(ResultSet hreTable, String tmgdate) throws HBException {

		if (HGlobal.DEBUG) {
			System.out.println(" ** updateT170_HDATES PID: " + " TMG Date: "
						+ tmgdate + " Length: " + tmgdate.length());
		}
		try {

		// Update T170 record
			hreTable.updateLong("MAIN_HDATE_YEARS", getMainYears());
			hreTable.updateString("MAIN_HDATE_DETAILS", getMainDetails());
			hreTable.updateLong("LOCALE_DST_RPID", null_RPID);
			hreTable.updateLong("HDATE_YEARS", getExtraYears());
			hreTable.updateString("HDATE_DETAILS", getExtraDetails());
			hreTable.updateString("SORT_HDATE_CODE", getSortDateCode());

		//Update row
			hreTable.updateRow();
			return 0;

		} catch (SQLException sqle) {
			if (HGlobal.DEBUG) {
				System.out.println("Error updateT170_HDATES - T170_DATES");
			}
			sqle.printStackTrace();
			throw new HBException("HBLibraryBusiness - updateT170_HDATES" + " - error: " + sqle.getMessage());
		}
	}

/**
 * addT170_HDATES(ResultSet hreTable, String hdate)
 * @param hreTable
 * @param tmgdate
 * @return
 * @throws SQLException
 * @throws HCException
 */
	public static int addT170_HDATES(ResultSet hreTable, long newHdatePID, Object[] inputHdate) throws HBException {
		main_years = (long) inputHdate[0];
		main_details = (String) inputHdate[1];
		extra_years = (long) inputHdate[2];
		extra_details = (String) inputHdate[3];
		sort_date_code = (String) inputHdate[4];
		return addT170_HDATES(hreTable, newHdatePID, "");
	}

	public static int addT170_HDATES(ResultSet hreTable, long newHdatePID, String tmgdate) throws HBException {

		if (HGlobal.DEBUG) {
			System.out.println(" ** addT170_HDATES PID: " + " TMG Date: "
						+ tmgdate + " Length: " + tmgdate.length() + " PID: " + newHdatePID);
		}
		try {

		// Set up T170 record
		// moves cursor to the insert row
			hreTable.moveToInsertRow();
		// Insert data
			hreTable.updateLong("PID", newHdatePID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			hreTable.updateBoolean("IS_SYSTEM", false);
			hreTable.updateLong("DISPLAY_DATE_RPID", newHdatePID);
			hreTable.updateLong("SORT_DATE_RPID", newHdatePID);
			hreTable.updateLong("REFERENCE_HDATE_RPID", null_RPID);
			hreTable.updateLong("MAIN_LOCALE_DST_RPID", null_RPID);
			//*******************************************************
			hreTable.updateLong("MAIN_HDATE_YEARS", getMainYears());
			hreTable.updateString("MAIN_HDATE_DETAILS", getMainDetails());
			hreTable.updateLong("LOCALE_DST_RPID", null_RPID);
			hreTable.updateLong("HDATE_YEARS", getExtraYears());
			hreTable.updateString("HDATE_DETAILS", getExtraDetails());
	//****************************************************
			hreTable.updateBoolean("HAS_CITATIONS", false);
			hreTable.updateLong("MEMO_RPID", null_RPID);
			hreTable.updateString("SURETY","---------");
	//*******************************************************
			hreTable.updateLong("SORT_LOCALE_DST_RPID", null_RPID);
			hreTable.updateString("SORT_HDATE_CODE", getSortDateCode());

			//Insert row
			hreTable.insertRow();
			return 0;

		} catch (SQLException sqle) {
			if (HGlobal.DEBUG) {
				System.out.println("Error addT170_HDATES - T170_DATES");
			}
			sqle.printStackTrace();
			throw new HBException("HBLibraryBusiness - addT170_HDATES" + " - error: " + sqle.getMessage());
		}
	}
}

/**
 * Processing of HRE memo recording
 * v0.00.0030 2023-04-24 -
 * @author NTo
 * @Since  2023-04-23
 */
class HREmemo extends HBBusinessLayer {

	static long proOffset = 1000000000000000L;
	static long null_RPID  = 1999999999999999L;
	long nextHREMemoPID;

	ResultSet hreMemoResultSet;
	int dataBaseIndex;
	String selectString;

/**
 * HREmemo(HDDatabaseLayer pointDBlayer, int dataBaseIndex)
 * @param pointDBlayer
 * @param dataBaseIndex
 */
	public HREmemo(HDDatabaseLayer pointDBlayer, int dataBaseIndex) {
		this.pointDBlayer = pointDBlayer;
		this.dataBaseIndex = dataBaseIndex;
	}

/**
 * long addRecordToT167_MEMO(String memoElement)
 * @param ownerRPID
 * @param memoElement
 * @return
 * @throws HBException
 * @throws SQLException
 */
	public long addMemoRecord(String memoElement) throws HBException {
		nextHREMemoPID = lastRowPID(memoSet, dataBaseIndex) + 1;
		selectString = setSelectSQL("*", memoSet, " PID = " + (nextHREMemoPID-1));
		hreMemoResultSet = requestTableData(selectString, dataBaseIndex);
		try {
			addToT167_MEMO_SET(nextHREMemoPID, memoElement, hreMemoResultSet);
			hreMemoResultSet.close();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" HREmemo - addToT167_MEMO: " + sqle.getMessage());
		}
		return nextHREMemoPID;
	}

/**
 * addToT167_MEMO_SET
 * @param rowPID
 * @param hreTable
 * @throws SQLException
 * @throws HCException
 * H2 metadata for:
 * T167_MEMO_SET (
	PID BIGINT NOT NULL,
	CL_COMMIT_RPID BIGINT NOT NULL,
	OWNER_RPID BIGINT NOT NULL,
	IS_LONG BOOLEAN NOT NULL,
	SHORT_MEMO VARCHAR(500),
	LONG_MEMO CLOB(30K));
 */
	public void addToT167_MEMO_SET(long primaryPID,
									String memoElement,
									ResultSet hreTable) throws SQLException {
		final int memoLimit = 500;
		final int maxLimit = 30000;
		String shortMemo;

		// moves cursor to the insert row
			hreTable.moveToInsertRow();
			hreTable.updateLong("PID", primaryPID);
			hreTable.updateLong("CL_COMMIT_RPID", null_RPID);
			if (memoElement.length() <= memoLimit) {
				hreTable.updateBoolean("IS_LONG", false);
				hreTable.updateString("SHORT_MEMO", memoElement);
			} else {
				hreTable.updateBoolean("IS_LONG", true);
				shortMemo = memoElement.substring(0, memoLimit);
				hreTable.updateString("SHORT_MEMO", shortMemo);
				if (memoElement.length() > maxLimit) {
					System.out.println(" Memo element > maxLimit - truncated - length: " + memoElement.length());
					memoElement = memoElement.substring(0, maxLimit);
				}
				hreTable.updateClob("LONG_MEMO", createNClob(memoElement));
			}
		//Insert row
			hreTable.insertRow();
			hreTable.close();
	}

/**
 * findT167_MEMOrecord(String memoElement, long memoTablePID)
 * @param memoElement
 * @param memoTablePID
 * @throws HBException
*/
	public void findT167_MEMOrecord(String memoElement, long memoTablePID) throws HBException {
		selectString = setSelectSQL("*", memoSet, " PID = " + memoTablePID);
		hreMemoResultSet = requestTableData(selectString, dataBaseIndex);
		try {
			hreMemoResultSet.first();
			updateT167_MEMO_SET(memoElement, hreMemoResultSet);
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" HREmemo - updateToT167_MEMO: " + sqle.getMessage());
		}
	}

/**
 * updateT167_MEMO_SET(String memoElement, ResultSet hreTable)
 * @param memoElement
 * @param hreTable
 * @throws SQLException
*/
	private void updateT167_MEMO_SET(String memoElement, ResultSet hreTable) throws SQLException {
			final int memoLimit = 500;
			final int maxLimit = 30000;
			String shortMemo;
			if (memoElement.length() <= memoLimit) {
				hreTable.updateBoolean("IS_LONG", false);
				hreTable.updateString("SHORT_MEMO", memoElement);
			} else {
				hreTable.updateBoolean("IS_LONG", true);
				shortMemo = memoElement.substring(0, memoLimit);
				hreTable.updateString("SHORT_MEMO", shortMemo);
				if (memoElement.length() > maxLimit) {
					System.out.println(" Memo element > maxLimit - truncated - length: " + memoElement.length());
					memoElement = memoElement.substring(0, maxLimit);
				}
				hreTable.updateClob("LONG_MEMO", createNClob(memoElement));
			}
			hreTable.updateRow();
			hreTable.close();
	}

/**
 * createNClob(String clobContent)
 * @param clobContent
 * @return Clob
 */
	public Clob createNClob(String clobContent) {
		Clob myClob = null;
	    try {
	    	myClob = createNClob();
			int nrOfChar = myClob.setString(1, clobContent);
			if (HGlobal.DEBUG) {
				System.out.println("Charac in Clob: " + nrOfChar);
			}
	        return myClob;
	    } catch (HDException hre) {
			if (HGlobal.DEBUG) {
				System.out.println("Write Clob error: " + hre.getMessage());
			}
			hre.printStackTrace();
	    } catch (SQLException sqle) {
	    	if (HGlobal.DEBUG) {
				System.out.println("Write Clob error: " + sqle.getMessage());
			}
			sqle.printStackTrace();
		}
	    return myClob;
	}

/**
 * createClob()
 * @return Clob object
 * @throws HCException
 */
	public Clob createNClob() throws HDException {
		Clob myClob = null;
		try {
			myClob = pointDBlayer.getConnection(dataBaseIndex).createNClob();
			return myClob;
		} catch (SQLException sqle) {
			throw new HDException("Request Connection SQL error: \n" + sqle.getMessage());
			//e.printStackTrace();
		}
	}

/**
 * readMemo(long memoTablePID) throws HBException
 * @param memoTablePID
 * @return
 * @throws HBException
 */
	public String readMemo(long memoTablePID) throws HBException {
		String memoText = "No Text";
		selectString = setSelectSQL("*", memoSet, " PID = " + memoTablePID);
		hreMemoResultSet = requestTableData(selectString, dataBaseIndex);
		try {
			hreMemoResultSet.first();
			if (hreMemoResultSet.getBoolean("IS_LONG")) {
				Clob clobMemo = hreMemoResultSet.getClob("LONG_MEMO");
		         Reader readClob = clobMemo.getCharacterStream();
		         StringBuffer buffer = new StringBuffer();
		         int ch;
		         while ((ch = readClob.read())!=-1) {
					buffer.append(""+(char)ch);
				}
		         memoText = buffer.toString();
			} else {
				memoText = hreMemoResultSet.getString("SHORT_MEMO");
			}
			if (HGlobal.DEBUG) {
				System.out.println(" HREmemo - Memo: " + memoText);
			}
		} catch (SQLException | IOException sqle) {
			System.out.println(" HREmemo - readMemo: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" HREmemo - readMemo: " + sqle.getMessage());
		}
		return memoText;
	}

/*
	public String readT167_MEMOclob(long memoTablePID) throws HBException {
		selectString = setSelectSQL("*", memoSet, " PID = " + memoTablePID);
		hreMemoResultSet = requestTableData(selectString, dataBaseIndex);
		try {
			hreMemoResultSet.first();
			updateT167_MEMO_SET(memoElement, hreMemoResultSet);
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" HREmemo - updateToT167_MEMO: " + sqle.getMessage());
		}
	} */
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
											ResultSet hreTable) throws HBException {
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
			if (HGlobal.DEBUG) {
				System.out.println("HREmemo - addToT204_DATA_TRAN - error: ");
			}
			sqle.printStackTrace();
			throw new HBException("HREmemo - T204_DATA_TRAN - error: "
					+ sqle.getMessage());
		}
	}

/**
 * returnStringContent(String content)
 * @param content
 * @return

	public static String returnStringContent(String content) {
		if (content == null) return "";
		if (content.length() == 64) {
		   if (content.trim().length() == 0) return "";
		}
		return content;
	}
*/
/**
 * getLangCode(String language)
 * @param language
 * @return

	public static String getLangCode(String language) {
		String code = "--";
		if (language.equals("AFRIKAANS")) code = "af-AF";
		if (language.equals("ENGLISH")) code = "en-US";
		if (language.equals("ENGLISHUK")) code = "en-GB";
		if (language.equals("DANISH")) code = "da-DA";
		if (language.equals("DUTCH")) code = "nl-NL";
		if (language.equals("FRENCH")) code = "fr-FR";
		if (language.equals("GERMAN")) code = "de-DE";
		if (language.equals("ITALIAN")) code = "it-IT";
		if (language.equals("NORWEGIAN")) code = "no-NB";
		if (language.equals("NORWEGIA2")) code = "no-NN";
		return code;
	}
*/
}



