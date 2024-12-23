package hre.bila;
/**************************************************************************************
 * Class HBMediaHandler extends BusinessLayer
 * Processes data for Media in User GUI
 * Receives requests from User GUI over Business Layer API
 * Sends requests over Database Layer API
 * **************************************************************************************
 * v0.01.0027 2022-03-25 - Processing thumb imaged for exhibit table (N. Tolleshaug)
 * 			  2022-03-28 - Corrected minor error - rest image table (N. Tolleshaug)
 *            2022-08-02 - Implemented HG0508ManageLocation + HG0506ManagePerson
 *            2022-08-02 - Select person, event and location images form T676
 * v0.01.0028 2023-02-22 - All exhibit image processing now in HBMediaHandler (N. Tolleshaug)
 * 			  2023-02-24 - T169 implemented for exhibit types in HBMediaHandler (N. Tolleshaug)
 * 			  2023-03-02 - exhibit text presented as String (N. Tolleshaug)
 * 			  2023-03-03 - exhibit text length > 100 truncated to length = 100 (N. Tolleshaug)
 * 			  2023-03-10 - Add Caption handling into media images (N. Tolleshaug)
 * ******************************************************************************************
 * Note content from T169 controlling exhibits
 * ******************************************************************************************
	 BaseType: 0 PID: 1000000000000001 - Person
	 BaseType: 1 PID: 1000000000000002 - Life Form
	 BaseType: 2 PID: 1000000000000003 - Location
	 BaseType: 3 PID: 1000000000000004 - Thing
	 BaseType: 4 PID: 1000000000000005 - Digital
	 BaseType: 5 PID: 1000000000000006 - Assembly
	 BaseType: 6 PID: 1000000000000007 - Event
	 BaseType: 7 PID: 1000000000000008 - Evidence
	***************************************************************************************
	 SubType: 1 PID: 1000000000000009 - image
	 SubType: 0 PID: 1000000000000010 - audio
	 SubType: 3 PID: 1000000000000011 - video
	 SubType: 2 PID: 1000000000000012 - text
 ****************************************************************************************/
import java.awt.Image;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import hre.gui.HGlobal;

/**
 * class HBMediaHandler
 * @author Nils Tolleshaug
 * @version v0.00.0027
 * @since 2022-03-25
 */
public class HBMediaHandler extends HBBusinessLayer {
	String dBbuild = HGlobal.databaseVersion;
	String ownerRecordField;
	String bestNameField;

	long proOffset = 1000000000000000L;
	long null_RPID  = 1999999999999999L;
	String personName = " Person name";
	int dataBaseIndex = 0;
	int nrRows = 0, childRow = 0, row = 0, grandRow = 0, nameRows = 0, eventRows = 0, asciateRows = 0;

	ArrayList<String> listOfTexts;
	ArrayList<ImageIcon> listOfImages;
	ArrayList<String> listOfImagesCaptions;

	JLabel pictLabel;
	ImageIcon image;

	long [] baseTypePID = new long[8];
	long [] subTypePID = new long[4];
	long baseTypeExhibitRPID = null_RPID;
	long subTypeExhibitRPID = null_RPID;

/**
 * HBMediaManager constructor
 */
	public HBMediaHandler(HBProjectOpenData pointOpenProject) {
		super();
		selectDataBase(dBbuild);
		if (HGlobal.DEBUG) {
			System.out.println("Media Handler initiated!!");
		}
	}

/**
 * void selectDataBase(String dBversion)
 * @param dBversion
 */
    private void selectDataBase(String dBversion) {
    	if (dBversion.startsWith("v22c")) {
    		ownerRecordField = "OWNER_RPID";
    		bestNameField = "BEST_NAME_RPID";
    	} else {
			System.out.println("HBMediaHandler - selected DataBase not found - " + dBversion);
		}
    }

/**
 * findSubTypePID()
 * @return
 * @throws HBException
 */
	private void findBaseSubTypePID(int select, int dataBaseIndex) throws HBException {
		boolean dump = false;
		//boolean dump = true;
		int subType, baseType;
		String selectSQL = setSelectSQL("*", entityTypeDefinition, "");
	// Find the slected project database index
		try {
			ResultSet researchTypeRPID = requestTableData(selectSQL, dataBaseIndex);
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
 * getAllExhibitImage(long ownerTablePID, int imageType, int dataBaseIndex)
 * @param ownerTablePID
 * @param imageType  1 for person, 2 for event, 3 for location
 * @param dataBaseIndex
 * @return
 * @throws HBException
 *
 */
	protected int getAllExhibitImage(long ownerTablePID, int imageType, int dataBaseIndex) throws HBException {
		long exhibitT676PID = proOffset, bestImagePID;
		ResultSet allExhibitsSelected, ownerSelected;
		String allImageSQLString, bestImageOwnerSQLString, ownerTable = null;

	// Set up T169 media parameters
		findBaseSubTypePID(4, dataBaseIndex);

		listOfImages = new ArrayList<>();
		listOfImagesCaptions = new ArrayList<>();
		listOfTexts = new ArrayList<>();
		allImageSQLString = setSelectSQL("*", digtalExhibitTable, "OWNER_RPID = " + ownerTablePID
											+ " AND ENTITY_TYPE_RPID = " + baseTypePID[imageType]);

		if (imageType == 0) {
			ownerTable = personTable;
		} else if (imageType == 6) {
			ownerTable = eventTable;
		} else if (imageType == 2) {
			ownerTable = locationTable;
		} else {
			System.out.println(" getAllExhibitImage - Unknown image type: " + imageType);
		}
		bestImageOwnerSQLString = setSelectSQL("*", ownerTable, "PID = " + ownerTablePID);

		try {
			ownerSelected = requestTableData(bestImageOwnerSQLString, dataBaseIndex);
			ownerSelected.first();
			bestImagePID = ownerSelected.getLong("BEST_IMAGE_RPID");

			allExhibitsSelected = requestTableData(allImageSQLString, dataBaseIndex);

		// Test if the selecte imageType has images in T676_DIGT
			allExhibitsSelected.last();
			if (allExhibitsSelected.getRow() == 0) {
				image = null;
				return 1;
			}
			if (HGlobal.DEBUG) {
				System.out.println(" Number of images for person: " + allExhibitsSelected.getRow());
			}

	// Extract all images for person / event / location
			allExhibitsSelected.beforeFirst();
			image = null;
			while (allExhibitsSelected.next()) {
				exhibitT676PID = allExhibitsSelected.getLong("PID");
				long exhibitType = allExhibitsSelected.getLong("SUB_TYPE_RPID");
				if (HGlobal.DEBUG) {
					System.out.println(" Image PID: " + exhibitT676PID + " Ent sub type: " + exhibitType);
				}
				if (exhibitType == subTypePID[1]) {
					if (HGlobal.DEBUG) {
						System.out.println(" Image PID: " + exhibitT676PID);
					}
					ImageIcon newImage = getImageFromBlob(allExhibitsSelected.getBlob("BIN_CONTENT"));
					String selectSQL = setSelectSQL("CAPTION", digtalNameTable, "OWNER_RPID = " + exhibitT676PID);
					ResultSet nameExhibitsSelected = requestTableData(selectSQL, dataBaseIndex);
					nameExhibitsSelected.first();
					String imageCaption = nameExhibitsSelected.getString("CAPTION");
					if (exhibitT676PID == bestImagePID) {
						image = newImage;
					}
					if (newImage != null) {
						listOfImages.add(newImage);
						listOfImagesCaptions.add(imageCaption);
					}
					else {
						if (imageType == 0) {
							listOfImages.add(new ImageIcon(getClass().getResource("/hre/images/missing-person-100.png")));
						} else if (imageType == 6) {
							listOfImages.add(new ImageIcon(getClass().getResource("/hre/images/missing-event-100.png")));
						} else if (imageType == 2) {
							listOfImages.add(new ImageIcon(getClass().getResource("/hre/images/missing-locn-100.png")));
						} else {
							System.out.println(" getAllExhibitImage - Unknown image type: " + imageType);
						}
					}
				} else if (exhibitType == subTypePID[2]) {
					if (HGlobal.DEBUG) {
						System.out.println(" Text Exhibit: "+  exhibitT676PID + " File: "
								+ allExhibitsSelected.getString("FILE_PATH"));
					}
					Clob textCont = allExhibitsSelected.getClob("CHAR_CONTENT");
					String textContent = textCont.getSubString(1,(int) textCont.length());
					if (textContent.length() > 100) {
						textContent = textContent.substring(0,100) + "......";
						if (HGlobal.DEBUG) {
							System.out.println(" Text Content truncated:\n " + textContent);
						}
					}
					if (HGlobal.DEBUG) {
						System.out.println(" Text Content:\n" + textContent);
					}
					listOfTexts.add(textContent);
				}
			}
			return 0;
		} catch (SQLException hbe ) {
			hbe.printStackTrace();
			throw new HBException(" HBMediaHandler - ExhibitImage create - error " + hbe.getMessage());
		}
	}

/**
 * ImageIcon getImageFromBlob(Blob imageData)
 * @param imageData
 * @return
 */
	private ImageIcon getImageFromBlob(Object imageData) {
		byte[] imageBytes = null;
		try {
			imageBytes = ((Blob) imageData).getBytes(0,(int)((Blob) imageData).length());
			if (HGlobal.DEBUG) {
				System.out.println(" Image bytes length: " + imageBytes.length);
			}
			if (imageBytes.length > 0) {
				return new ImageIcon(imageBytes);
			} else {
				return null;
			}
		} catch (NullPointerException | SQLException ime) {
			System.out.println(" HB MediaHandler - NullPointerException bytes length: " + imageBytes.length);
			return null;
		}
	}

/**
 * getExhibitImage()
 * @return
 */
	public ImageIcon getExhibitImage() {
		return image;
	}

/**
 * ArrayList<ImageIcon> getImageList()
 * @return
 */
	public ArrayList<ImageIcon> getImageList() {
		return listOfImages;
	}

/**
 * ArrayList<String> getImageCaptionList()
 * @return istOfImagesCaptions
 */
	public ArrayList<String> getImageCaptionList() {
		return listOfImagesCaptions;
	}

/**
 * ArrayList<String> getTextList()
 * @return
 */
	public ArrayList<String> getTextList() {
		return listOfTexts;
	}

/**
 * int getNumberOfTexts()
 * @return
 */
	public int getNumberOfTexts() {
		if (listOfTexts == null) {
			return 0;
		} else {
			return listOfTexts.size();
		}
	}

/**
 * int getNumberOfImages()
 * @return
 */
	public int getNumberOfImages() {
		if (listOfImages == null) {
			return 0;
		} else {
			return listOfImages.size();
		}
	}

/**
 * Scale an ImageIcon to required size
 * @param maxHeight - max height of rescaled image
 * @param maxWidth - max width of rescaled image
 * @param imageToScale - image to be rescaled
 * @return newImage - rescaled image
 */
    public ImageIcon scaleImage(int maxHeight, int maxWidth, ImageIcon imageToScale) {
    	ImageIcon newImage;
       	int newHeight, newWidth = 0;
    	int currentHeight = imageToScale.getIconHeight();
    	int currentWidth = imageToScale.getIconWidth();
    	// Calculate new height and width
    	if((float)currentHeight/(float)currentWidth > (float)maxHeight/(float)maxWidth)  {
            newHeight = maxHeight;
            newWidth = (int)(((float)currentWidth/(float)currentHeight)*newHeight);
        	}
        else {
            newWidth = maxWidth;
            newHeight = (int)(((float)currentHeight/(float)currentWidth)*newWidth);
        	}
    	// Re-scale image and return
 		Image scaledImage = imageToScale.getImage().getScaledInstance(newWidth, newHeight, java.awt.Image.SCALE_SMOOTH);
		newImage=new ImageIcon(scaledImage);
    	return newImage;
       }	// End scaleImage

}
