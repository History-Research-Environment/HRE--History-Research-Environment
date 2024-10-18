package hre.bila;
/*******************************************************************************************
 * v0.00.0023 2020-08-21 - Test write T302_GUI_CONFIG data to HRE DDL v20a (N.Tolleshaug)
 * 			  2020-08-26 - Class inheritance included HRE DDL v20a (N.Tolleshaug)
 * 			  2020-08-31 - moved to BusinessLayer and renamed (N.Tolleshaug)
 * 			  2020-09-01 - Implemented read/write reminder to database(N Tolleshaug)
 * 			  2020-09-03 - Removed debug printouts (N Tolleshaug)
 * 			  2020-09-08 - Exception handling implemented (N Tolleshaug)
 * 			  2020-09-09 - Implemented handling of GUI position (N Tolleshaug)
 * 			  2020-09-09 - Implemented handling of GUI frame size (N Tolleshaug)
 * 			  2020-09-25 - GUI index stored in HashMap when reading T302 (N Tolleshaug)
 * 			  2020-10-03 - Error fixed init HRE with T302 table empty (N. Tolleshaug)
 * v0.00.0025 2021-01-20 - Reset person ID and multiple PersonViewPoint (N. Tolleshaug)
 * 			  2021-02-07 - Implemented column control HG0581ConfigureTable (N. Tolleshaug)
 * 			  2021-02-22 - Modification in T302 create and update (N. Tolleshaug).
 *            2021-03-06 - Implemented handling of 5 Location VP (N. Tolleshaug)
 * v0.00.0027 2022-01-17 - Renamed methods, indexes and PID's (N. Tolleshaug)
 * v0.00.0028 2022-12-16 - Store name style index in T302 (N. Tolleshaug)
 * 			  2023-03-09 - Reset T302 if error open project (N. Tolleshaug)
 ******************************************************************************************/
import java.awt.Dimension;
import java.awt.Point;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import hre.gui.HGlobal;


/**
 * class HBStoreGuiConfig()
 * @author NTo
 * @since 2020-09-01
 */
public class HBStoreGuiConfig {
	String ver = "2022-12-16";

 // Temporary constants for testing

	long null_RPID = 1999999999999999L;
	long proOffset = 1000000000000000L;
	long user_RPID = 1000000000000001L;

 // Array with GUI_ID values for Project Menu item dialog windows

	String[] guiProjectVal = {"40200","40300","40400","40500","40600","40700","40800"
				,"40900","41000","41400","41700"};


 // Array with GUI_ID values for project controlled windows
    String[] guiIDvalues = {"50100","50500","50600","50700","50750", "50800",
    						"51200","52000","52500","52600",
    						"53000","53001","53002","53003","53004",
    						"53030","53031","53032","53033","53034",
    						"53060","53061","53062","53063","53064",
    						"54000","56000","56030","57500","66000","70000"};

    int nrOfScreens = guiIDvalues.length; // Number of project windows

 // Pointers to guiConfigObjects
    //public ScreenConfig[] screenConfigs = new ScreenConfig[nrOfScreens];
    public ScreenConfig[] screenConfigDataObj;

 // HashMap for indexes in in T302
    HashMap<String, Integer> guiIndexMap = new HashMap<>();

/**
 *  resetScreenConfig()
 */
    public void resetScreenConfig() {
    	screenConfigDataObj = new ScreenConfig[nrOfScreens];
    }

 /**
 *  Constructor HBStoreGuiConfig()
 *  Only for testing of screenConfigs data
 */
	public HBStoreGuiConfig() {
		if (HGlobal.DEBUG) {
			System.out.println("*** HBStoreGuiConfig initiated - ver: " + ver);
		}
		screenConfigDataObj = new ScreenConfig[nrOfScreens];
	}

/**
 * Find GUI Store index
 * @param screenID
 * @return
*/
	public int getStoreIndex(String screenID) {
		if (guiIndexMap.get(screenID) != null) {
			return guiIndexMap.get(screenID);
		} else {
			System.out.println(" HBStoreGuiConfig - getStoreIndex - Key: /" + screenID + "/ not found in HashMap");
		}
		return -1;
	}

/**
 * getOpenStatus for GUI screen
 * @param screenID
 * @param t302Data
 * @return
 */
	public boolean getOpenStatus(String screenID, ResultSet t302Data) throws HBException {
		try {
			if (!t302Data.absolute(getStoreIndex(screenID) + 1)) {
				throw new HBException("HBStoreGuiConfig - getOpenStatus - absolute error: "
						+ getStoreIndex(screenID) + 1);
			}
			return t302Data.getBoolean("IS_OPEN");
		} catch (SQLException sqle) {
			System.out.println("HBStoreGuiConfig - getOpenStatus: " + sqle.getMessage());
			throw new HBException("No data found in ResultSet\n" + sqle.getMessage());
		}
	}

/**
 * setOpenStatus for GUI screen
 * @param screenID
 * @param t302Data
 * @param open
 * @throws HBException
 */
	public void setOpenStatus(String screenID, ResultSet t302Data, boolean open) throws HBException {
		try {
			if (!t302Data.absolute(getStoreIndex(screenID) + 1)) {
				throw new HBException("HBStoreGuiConfig - setOpenStatus - absolute error: "
						+ getStoreIndex(screenID) + 1);
			}
			t302Data.updateBoolean("IS_OPEN",open);
		 //Update row in database
            t302Data.updateRow(); // updates the row in the data source
			if (HGlobal.DEBUG) {
				System.out.println("HBStoreGuiConfig - setOpenStatus: " + screenID + " Open: " + open);
			}
		} catch (SQLException sqle) {
			System.out.println("HBStoreGuiConfig - setOpenStatus: " + sqle.getMessage());
			sqle.printStackTrace();
		}
	}

/**
 * Update T302_GUI_CONFIG table
 * @param t302Data
 * @throws HBException
 */
	public void updateT302table(ResultSet t302Data) throws HBException {
		for (int i = 0; i < nrOfScreens; i++) {
			updateObject(t302Data, i);
		}
	}

/**
 * Collection of Get and Get methods that operate on GUI screen data objects
 * @param screenId to dentify the GUI object
 * @return
 */
	public Point getFramePosition(String screenId) {
		Point posis = ((DisplayScreenData)screenConfigDataObj[getStoreIndex(screenId)]).getPosis();
		return posis;
	}

	public void setFramePosition(String screenId, Point posis) {
		((DisplayScreenData)screenConfigDataObj[getStoreIndex(screenId)]).setPosis(posis);
	}

	public Dimension getFrameSize(String screenId) {
		return screenConfigDataObj[getStoreIndex(screenId)].getSize();
	}

	public void setFrameSize(String screenId, Dimension dim) {
			screenConfigDataObj[getStoreIndex(screenId)].setSize(dim);
		}

	public Object[][]  getTableControl(String screenId) {
		return screenConfigDataObj[getStoreIndex(screenId)].getTableControl();
	}

	public void setTableControl(String screenId, Object [][] tableControl) {
		screenConfigDataObj[getStoreIndex(screenId)].setTableControl(tableControl);
	}

	public void cloneTableControl(String screenId, Object [][] tableControl) {
		screenConfigDataObj[getStoreIndex(screenId)].cloneTableControl(tableControl);
	}

	public void setClassName(String screenId, String name) {
		screenConfigDataObj[getStoreIndex(screenId)].setClassName(name);
	}

	public String getClassName(String screenId) {
		return screenConfigDataObj[getStoreIndex(screenId)].getClassName();
	}

	public String getDispRemindText(String screenId) {
		return ((DisplayScreenData) screenConfigDataObj[getStoreIndex(screenId)]).getReminder();
	}

	public void setDispRemindText(String screenId, String text) {
		((DisplayScreenData) screenConfigDataObj[getStoreIndex(screenId)]).setReminder(text);
	}

	public void setVisibleIDX(String screenId, int idx) {
		((DisplayScreenData) screenConfigDataObj[getStoreIndex(screenId)]).setVisibleIDX(idx);
	}

	public int getVisibleIDX(String screenId) {
		return ((DisplayScreenData) screenConfigDataObj[getStoreIndex(screenId)]).getVisibleIDX();
	}

	public void setTableViewPointPID(String screenId, long PID) {
		((DisplayScreenData) screenConfigDataObj[getStoreIndex(screenId)]).setViewPointPID(PID);
	}

	public long getTableViewPointPID(String screenId) {
		return ((DisplayScreenData) screenConfigDataObj[getStoreIndex(screenId)]).getViewPointPID();
	}

	public int getNameDisplayIndex(String screenId) {
		int styleIndex = ((DisplayScreenData) screenConfigDataObj[getStoreIndex(screenId)]).getDisplayIndex();
		if (HGlobal.DEBUG) {
			System.out.println("**Restore name style index: " + styleIndex);
		}
		return styleIndex;
	}

	public void setNameDisplayIndex(String screenId, int styleIndex) {
		if (HGlobal.DEBUG) {
			System.out.println("##Setting name style index: " + styleIndex);
		}
		((DisplayScreenData) screenConfigDataObj[getStoreIndex(screenId)]).setDisplayIndex(styleIndex);
	}


/**
 *  createT302data()
 *  Generate records in table T302_GUI_CONFIG or read table
 * @throws HBException
 */
    public int createT302data(ResultSet t302Data, boolean reload) throws HBException {
    	if (HGlobal.DEBUG) {
			System.out.println("\nStart - CreateT302data - Gui Object");
		}
    	int size = 0;
    	try {
            t302Data.last();
            size = t302Data.getRow();
            if (HGlobal.DEBUG) {
				System.out.println("Number of rows/screens: " + size + "/" + nrOfScreens);
			}
            long lStartTime = System.nanoTime();
            if (size > 0 && size != nrOfScreens) {
            	deleteRecords(t302Data);
            	size = 0;
            }

            if (reload) {
				size = 0;
			}
        // Create records if T302 table empty
            if (size == 0) {

        	// Create T302_GUI_CONFIG table
	            int rows = 0;
	            for(int i=0; i < nrOfScreens; i++) {
	            	rows = i + 1;

	            // Create gui config objects
	            	if (guiIDvalues[i].startsWith("70")) {
						screenConfigDataObj[i] = new InputScreenData();
					} else {
						screenConfigDataObj[i] = new DisplayScreenData();
					}
	            // Set GUI_ID in guiConfig Object
	                screenConfigDataObj[i].setGuiId(guiIDvalues[i]);

	        	// moves cursor to the insert row
	                t302Data.moveToInsertRow();
	    		// Update new row in H2 database
	                t302Data.updateLong("PID", proOffset + i);
	                t302Data.updateLong("CL_COMMIT_RPID", null_RPID);
	                t302Data.updateLong("USER_RPID", user_RPID);
	                t302Data.updateString("GUI_ID", guiIDvalues[i]);
	                t302Data.updateBoolean("IS_OPEN",false);
	                t302Data.updateObject("CONFIGURATION", screenConfigDataObj[i]);
	        	//Insert row in database
	    			t302Data.insertRow();
	            }
	            long lEndTime = System.nanoTime();
	            long output = lEndTime - lStartTime;
	            if (HGlobal.DEBUG) {
					System.out.println("createT302data : Number of rows written: " + rows);
				}
	            if (HGlobal.DEBUG) {
					System.out.println("createT302data : Time in milliseconds: " + output / 1000000);
				}
	            return size;
            } else {
            	readObjects(t302Data);
            	if (HGlobal.DEBUG) {
					System.out.println("Config data read from table!!");
				}
            }
        } catch (SQLException sqle)   {
        	if (sqle.getMessage().startsWith("Unique")) {
				System.out.println("Table aready created with data!!");
        	} else {
				System.out.println("Create records - SQL error: \n" + sqle.getMessage());
			}
        	System.out.println("Create Object - Terminated ");
        	throw new HBException("\nCreate records - SQL error: " + sqle.getMessage());
        }
		return size;
    }

/**
 * updateObject(int row)
 * @param row
 * @throws HBException
 */
    public void updateObject(ResultSet t302Data, int row) throws HBException {
    	//if (HGlobal.DEBUG) System.out.println("\nUpdate Object Start");
        try {
        	long lStartTime = System.nanoTime();
    	// moves cursor to the insert row
            //t302Data.absolute(row + 1 );
			if (!t302Data.absolute(row + 1)) {
				throw new HBException("HBStoreGuiConfig  - updateObject - absolute error: "
						+ t302Data.absolute(row + 1));
			}
            long pid = t302Data.getLong("PID");
            screenConfigDataObj[row].setGuiId(guiIDvalues[row]);

		// Update new row in H2 database
            t302Data.updateLong("PID", pid);
            t302Data.updateLong("CL_COMMIT_RPID", null_RPID);
            t302Data.updateLong("USER_RPID", user_RPID);
            t302Data.updateString("GUI_ID", guiIDvalues[row]);
            t302Data.updateObject("CONFIGURATION", screenConfigDataObj[row]);
        // Status updated separately
            //t302Data.updateBoolean("IS_OPEN",false);
    	//Update row in database
            t302Data.updateRow(); // updates the row in the data source

            long lEndTime = System.nanoTime();
            long output = lEndTime - lStartTime;
            if (HGlobal.DEBUG) {
				System.out.println("GUI config(" + row + ") updated"
						+ ": Time in milliseconds: " + output / 1000000);
			}
        } catch (SQLException sqle)   {
        	System.out.println("Update records - SQL error: " + sqle.getMessage());
        	throw new HBException("\nUpdate records - SQL error: " + sqle.getMessage());
        	//sqle.printStackTrace();
        }
    }

/**
 * readObjects
 * @param t302Data
 * @throws HBException
 */
    public void readObjects(ResultSet t302Data) throws HBException {
    	int index = 0;

    	if (HGlobal.DEBUG) {
			System.out.println("\nRead Config Object Start");
		}
    	try {

    	// Reset arrays for screenDataPonters and guiID
    		t302Data.last();
    		nrOfScreens = t302Data.getRow();
			if (HGlobal.DEBUG) {
				System.out.println("Number of GUI screens = " + nrOfScreens);
			}
			guiIDvalues = new String[nrOfScreens];
			screenConfigDataObj = new ScreenConfig[nrOfScreens];

    		t302Data.beforeFirst();
            while(t302Data.next())    {
            	String guiID = t302Data.getString("GUI_ID");
            	guiIDvalues[index] = guiID;

           // Select type of object to be restored
                if (guiID.startsWith("70")) {
                	screenConfigDataObj[index] = (InputScreenData) t302Data.getObject("CONFIGURATION");
                } else {
                	screenConfigDataObj[index] = (DisplayScreenData) t302Data.getObject("CONFIGURATION");
                }
                guiIndexMap.put(guiID,index);
                index++;
            }
        } catch (SQLException sqle)   {
        	System.out.println("Read Data - SQL error: " + sqle.getMessage());
        	throw new HBException("\nRead Data - SQL error: " + sqle.getMessage());
        }
    }

/**
 * Delete all records in table T302
 * @param t302Data
 * @throws HBException
 */
    public void deleteRecords(ResultSet t302Data) throws HBException {
    	if (HGlobal.DEBUG) {
			System.out.println("\nDelete Config Object Start");
		}

    	try {
    		t302Data.beforeFirst();
            while(t302Data.next()) {
            		t302Data.deleteRow();
            }
            t302Data.last();
            int size = t302Data.getRow();
            if (HGlobal.DEBUG) {
				System.out.println("HBStoreGuiConfig - Deleted rows/screens: " + size + "/" + nrOfScreens);
			}
            //if (HGlobal.DEBUG)
	            	System.out.println(" HBStoreGuiConfig - DeleteRecords - Table T302 cleared! ");
    	} catch (SQLException sqle) {
        	//System.out.println("HBStoreGuiConfig - DeleteRecords - SQL error: " + sqle.getMessage());
        	throw new HBException("HBStoreGuiConfig\nDeleteRecords - SQL error: " + sqle.getMessage());
        }
    }

} // End HDStoreObject

/**
 * class DisplayScreenData
 * @author NTo
 * @since 2020-09-09
 */
class DisplayScreenData extends ScreenConfig {

	private static final long serialVersionUID = 1L;
	private long null_RPID = 1999999999999999L;

	private String reminderText = null;
	private int visiblePersonIDX = 1;
	private long viewPointPID = null_RPID;
	private int selectNameDisplayIndex = 0;

    public DisplayScreenData() {
    	super();
    }
    public String getReminder() {
        return reminderText;
    }
    public void setReminder(String txt) {
        reminderText = txt;
    }

    public int getVisibleIDX() {
    	return visiblePersonIDX;
    }

    public void setVisibleIDX(int visibleID) {
    	visiblePersonIDX = visibleID;
    }

    public long getViewPointPID() {
    	return viewPointPID;
    }

    public void setViewPointPID(long PID) {
    	viewPointPID = PID;
    }

    public int getDisplayIndex() {
    	return selectNameDisplayIndex;
    }

    public void setDisplayIndex(int styleIndex) {
    	selectNameDisplayIndex = styleIndex;
    }

}// End class DisplayScreenData

/**
 * class InputScreenData
 * @author NTo
 * @since 2020-09-09
 */
class InputScreenData extends ScreenConfig  {

	private static final long serialVersionUID = 1L;

	String [] reminderText = {"Edit reminder text - 1",
							  "Edit reminder text - 2",
							  "Edit reminder text - 3" };

	public InputScreenData() {
		super();
	}

	public String getReminder(int i) {
		return reminderText[i];
	}

	public void setReminder(String txt, int i) {
		reminderText[i] = txt;
	}

} // End class InputScreenData

/**
 * class ScreenConfig
 * @author NTo
 * @since 2020-09-09
 */
class ScreenConfig implements Serializable {
	private static final long serialVersionUID = 1L;
	private String guiID = "";
	private String className = "";

	private Dimension size = null;
	private Point posis = null;
	private Object [][] tableControlStore = null;

	public void cloneTableControl(Object[][] tableControl) {
		tableControlStore = tableControl.clone();
	}

	public ScreenConfig() {
		super();
	}

	public String getGuiId() {
		return guiID;
	}

	public void setGuiId(String guiId) {
		this.guiID = guiId;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public Dimension getSize() {
		if (HGlobal.DEBUG) {
			if (size != null) {
				System.out.println("ScreenConfig - Get Size: " + size.toString());
			} else {
				System.out.println("Get Size == null");
			}
		}
		return size;
	}

	public void setSize(Dimension dim) {
		size = dim;
		if (HGlobal.DEBUG) {
			if (size != null) {
				System.out.println("ScreenConfig - Set Size " + size.toString());
			} else {
				System.out.println("Set Size == null");
			}
		}
	}

	public Object [][] getTableControl() {
		if (HGlobal.DEBUG) {
			if (tableControlStore != null) {
				printTableControl("Get",tableControlStore);
			} else {
				System.out.println("Get Table Control == null");
			}
		}
		return tableControlStore;
	}

	public void setTableControl(Object [][] tableControl) {
		tableControlStore = tableControl;
		if (HGlobal.DEBUG) {
			if (tableControlStore != null) {
				printTableControl("Set",tableControl);
			} else {
				System.out.println("Set Table Control == null");
			}
		}
	}

	public Point getPosis() {
		if (HGlobal.DEBUG) {
			if (posis != null) {
				System.out.println("ScreenConfig - Get Posis: " + posis.toString());
			} else {
				System.out.println("Get Posis == null");
			}
		}
		return posis;
	}

	public void setPosis(Point pos) {
		posis = pos;
		if (HGlobal.DEBUG) {
			if (posis != null) {
				System.out.println("ScreenConfig - Set Posis: " + posis.toString());
			} else {
				System.out.println("Set Posis == null");
			}
		}
	}

 /**
  * Test table Control
  */
	    private void printTableControl(String type, Object [][] tableControl) {
			System.out.println("Screen Config " + type + " Control Table rows");
			for (int i = 0; i < tableControl.length; i++ ) {
				System.out.println(i + " - " + tableControl[i][0] + " / " +  tableControl[i][1]);
			}
	    }

} // End ScreenConfig
