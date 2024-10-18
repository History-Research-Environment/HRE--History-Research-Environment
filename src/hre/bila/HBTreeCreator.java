package hre.bila;
/* HBTreeCreator
 * ************************************************************************************
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/****************************************************************************************
 * Based on Oracle example provided by tutorial reader Olivier Berlanger.
 * Original A 1.4 example that uses the following files:
 *    GenealogyModel.java, Person.java, GenealogyTree.java
 ****************************************************************************************
 * v0.01.0023 2020-08-20 Built from GenealogyXXX components of pres package (D Ferguson)
 * 			  2020-08-20 Modified to extend HBBusinessLayer (N. Tolleshaug)
 * 			  2020-08-21 Completed connection to HG0507PersonSelect (N. Tolleshaug)
 *    		  2020-08-22 Moved getOpenProjectByName to bila package
 *  					   and renamed to pointOpenProjectByName. (N. Tolleshaug)
 *  		  2020-08-23 Allow passed parameters select focus person (D Ferguson)
 *  		  2020-08-23 Modified to fireTreeStructureChanged in Tree (N. Tolleshaug)
 *  		  2020-08-25 implement expand/collapse commands (D Ferguson)
 * 			  2020-08-27 Last Partner data returned to PersonSelect (N. Tolleshaug)
 * 			  2020-08-29 All Partners returned to PersonSelect (N. Tolleshaug)
 * v0.01.0025 2020-11-13 Primary name or BEST_NAME_RPID implemented. (N. Tolleshaug)
 * 			  2020-11-18 Implemented name display options DISP/SORT (N. Tolleshaug)
 * 			  2020-12-02 Exception handling improved (N. Tolleshaug)
 * 			  2021-01-22 Right click on tree name initiate pVP (N. Tolleshaug)
 * 			  2021-04-10 Fixed right click when no selection made (N. Tolleshaug)
 * v0.01.0026 2021-05-03 Implemented handling of DDL 21a (N. Tolleshaug)
 * 			  2021-05-16 Included both DDL20a and DDL21a (N. Tolleshaug)
 * 			  2021-06-10 New search for partners (N. Tolleshaug)
 * 			  2021-06-14 Improved HDATE processing (N. Tolleshaug)
 * 			  2021-09-18 Corrected error in partner select (N. Tolleshaug)
 * v0.01.0027 2021-11-10 Removed HashMap lookup for name and place element table (N. Tolleshaug)
 * 			  2021-11-10 Removed HashMap lookup for HDATE table (N. Tolleshaug)
 * 			  2022-03-12 Changed mouse right-click to double-click to open VP (D Ferguson)
 * 			  2022-10-01 - Changed date translation to v21h tables (N. Tolleshaug)
 * v0.01.0028 2023-01-27 - Dates are not collected from T401 not T402 (N. Tolleshaug)
 * v0.01.0030 2023-07-01 - Merged with Person Select table processing (N. Tolleshaug)
 * 			  2023-08-05 - Enable update status bar - on/off (N. Tolleshaug)
 ****************************************************************************************
 * NOTES ON INCOMPLETE FUNCTIONALITY
 ****************************************************************************************
 * NOTE-1 The use of Visible-ID to select person HBTreeCreator - getFocusPerson 
 * may not be correct 
 ***************************************************************************************/

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import hre.gui.HG0507PersonSelect;
import hre.gui.HGlobal;
import hre.gui.HGlobalCode;

/**
 * Class HBTreeCreator
 * @author NTo
 * @since 2020-03-30
 */
public class HBTreeCreator extends HBBusinessLayer {
	HG0507PersonSelect pointPersonSelect;
	private HBViewPointHandler pointViewPointHandler;
	HBProjectOpenData pointOpenProject;
	String dBbuild = HGlobal.databaseVersion;
	String ownerRecordField;
	String bestNameField;
	boolean updateMonitor = true;

	GenealogyTree tree;
	private int personID;
	private int dataBaseIndex;

	HashMap<Long, GenealogyPerson> personBranchIndex = new HashMap<>();

	// HashMap for person names index PID
			HashMap<Long,GenealogyData> personDataIndex;

    ResultSet personTable,
    			nameTable,
    			hdateData = null,
    			hdateT175table = null,
    			hdateT204data = null,
    			hdataTable = null;

    long null_RPID  = 1999999999999999L;
    long proOffset = 1000000000000000L;
    int initPersonNr;


/**
 * Constructor
 * @param openProject (for personTable, nameTable, hdateTable)
 * @param personID	for person to be focus of returned tree
 * @throws HBException
 */
    public HBTreeCreator(HBProjectOpenData pointOpenProject
    						, int personID
    						, int nameDisplayIndex
    						, HG0507PersonSelect pointPersonSelect) throws HBException {
    	super();
    	this.pointPersonSelect = pointPersonSelect;
    	this.personID = personID;
    	this.pointOpenProject = pointOpenProject;
    	if (pointPersonSelect == null) updateMonitor = false;

    // Set up pointer to DBlayer for BusinessLayer
    	this.pointDBlayer = pointOpenProject.pointProjectHandler.pointDBlayer;

    	pointViewPointHandler = pointOpenProject.getViewPointHandler();

    	// Set name display index in this instance of BusinessLayer
    	setNameDisplayIndex(nameDisplayIndex);

    	dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();

        personTable = pointOpenProject.getT401Persons();
        nameTable = pointOpenProject.getT402Names();
		selectDataBase(dBbuild);

    }	// End HBTree constructor

    private void selectDataBase(String dBversion) {
    	if (dBversion.startsWith("v22b")) {
    		ownerRecordField = "OWNER_RPID";
    		bestNameField = "BEST_NAME_RPID";
    	} else System.out.println("Tree Creator - DB build not found - " + dBversion);
    }

/**
 * initateTree()
 * @return
 */
    public int initateTree() {
    	if (HGlobal.TIME) HGlobalCode.timeReport("start processing family tree");
		try {

	    // Set up date translation
			dateFormatSelect();
			setUpDateTranslation();

        //Construct the tree, focused on personID parameter.
			tree = new GenealogyTree(getGenealogyGraph(personID, dataBaseIndex));
			if (HGlobal.TIME)
				HGlobalCode.timeReport("End processing family tree");

		} catch (HBException hbe) {
			if (HGlobal.DEBUG)  System.out.println("Not able to create tree\n" + hbe.getMessage());
			if (HGlobal.DEBUG)  hbe.printStackTrace();
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("HBCreateTree - Create Tree Error:  " + hbe.getMessage());
				HB0711Logging.printStackTraceToFile(hbe);
			}
			return 1;
		} catch (Exception exc) {
			if (HGlobal.DEBUG)  System.out.println("HBCreateTree - Unexpected Exception:\n  " + exc.getMessage());
			if (HGlobal.DEBUG)  exc.printStackTrace();
			if (HGlobal.writeLogs) {
				HB0711Logging.logWrite("HBCreateTree - Unexpected Exception:  " + exc.getMessage());
				HB0711Logging.printStackTraceToFile(exc);
			}
		}

/**
 * Listener for mouse double click
 */
        tree.addMouseListener(new MouseAdapter() {
        	@Override
            public void mousePressed(MouseEvent me) {
        		if (me.getClickCount() == 2 && tree.getSelectPerson() != null) {
        			GenealogyPerson selectedPerson = tree.getSelectPerson();
        			String name = selectedPerson.getName();
        			long personTablePID = selectedPerson.getPersonTablePID();
        			if (HGlobal.DEBUG)  System.out.println("Selected: "  + name + " Index: " + personTablePID);

        			if (pointPersonSelect != null) pointViewPointHandler.initiatePersonVP(pointOpenProject, personTablePID);
        			else if (HGlobal.DEBUG) System.out.println("Pointer to HBViewpointHandler not set!!");
        		}
        	}
        });
		return 0;
    }	// End HBTree constructor

/**
 * GenealogyTree getTree()
 * @return
 */
    public GenealogyTree getTree() {
    	return tree;
    }

/**
 * setFamView
 * @param ancest
 */
    public void setFamView(boolean ancest) {
			tree.showAncestor(ancest);
	}

/**
 * expandAll()
 */
    public void expandAll() {
    	 int row = 0;
    	 while (row < tree.getRowCount()) {
    	    tree.expandRow(row);
    	    row++;
    	 }
	}

/**
 * collapseAll() - collapse all but last node
 */
    public void collapseAll() {
    	int row = tree.getRowCount() - 1;
        while (row >= 1) {
          tree.collapseRow(row);
          row--;
        }
	}

/**
 * expandOne()
 */
    public void expandOne() {
    	int count = tree.getRowCount();
    	// Work backwards through tree and expand each row
   	 	for (int row = count; row >= 1; row--) {
   	 		tree.expandRow(row);
   	 	}
	}

/**
 * collapseOne()
 */
    public void collapseOne() {
    	int pathCount;
    	int row = 1;
    	int hiPC = 2;
    	int count = tree.getRowCount() - 1;

    	// Run through tree and get highest pathcount
    	while (row < count) {
      	 	pathCount = tree.getPathForRow(row).getPathCount();
      	 	if (pathCount > hiPC) hiPC = pathCount;
    	    row++;
    	 }
    	// Go back through tree and collapse hiPC row owners
    	for (row = count; row >= 1; row--) {
      	 	pathCount = tree.getPathForRow(row).getPathCount();
      	 	if (pathCount == hiPC - 1) tree.collapseRow(row);
   	 	}
	}

/**
 * findPartners()
 * Modified for return of partner - 2020-08-28 NTo
 */
	public Vector<GenealogyPerson> findPartners() {
		GenealogyPerson pointSelected = tree.selectedPerson();
		if (tree.getPersonSelected()) {
			if (pointSelected.getPartners().size() != 0 ) {
				return pointSelected.getPartners();
			} else {
				partnerMessage("ERR1");
				return null;
			}
		} else {
			partnerMessage("ERR2");
			return null;
		}
	}

/**
 * Show Partner list
 * @param message
 */
	private void partnerMessage(String message) {
		pointPersonSelect.userInfoTreeCreator(2, message);
	}

/**
 *  Constructs Genealogy graph used by the model, focused on passed parameter
 * @throws HBException
 */
    public GenealogyPerson getGenealogyGraph(int focusPersID, int dataBaseIndex) throws HBException {
		long personPID = 0;
		long hdateRPID;

		HBPersonHandler  pointPersonHandler =  pointOpenProject.getPersonHandler();

		// HashMap for person names index PID
		personDataIndex = new HashMap<>();

		if (HGlobal.DEBUG)
			System.out.println("Ancestor HRE T401 and T404 to Tree ");

		try {

			if (HGlobal.DEBUG) {
				personTable.last();
				int nrOfRows = personTable.getRow();
				System.out.println("HBTreeCreator - Person Table size: " + nrOfRows);
				nameTable.last();
				nrOfRows = nameTable.getRow();
				System.out.println("HBTreeCreator - Name Table size: " + nrOfRows);
			}

		// take each value from the HRE table separately
			String personName = null;
			String bornDate = null;
			String deathDate = null;
			long name_RPID;
		//Set up Person data index
			personTable.beforeFirst();
			while (personTable.next()) {
				if (updateMonitor) pointPersonHandler.setStatusPercent(pointPersonHandler.countStatusBar++);
				personPID = personTable.getLong("PID");
				PersonSelectData personSelectData = pointPersonHandler.personDataIndex.get(personPID);
				int visbleId = personSelectData.getVisibleId();

			// Find best name
				name_RPID = personTable.getLong(bestNameField);
				if (name_RPID != null_RPID) {
					personName = personSelectData.getName();
	    		} else System.out.println(" ERROR - HBTreeCreator - getGenealogyGraph() - PID: " + name_RPID);

				if (HGlobal.DEBUG) System.out.println("Tree Creator Person name: " + personName);

			// Extract HDATA START from T170_DATE
				int bornDateNumber = 0;
				hdateRPID = personTable.getLong(personHDateBirthField);

				if (hdateRPID == null_RPID) bornDate = "";
				else {
					bornDateNumber = calculateDateNumber(hdateRPID);
					bornDate = personSelectData.getBornDate();
				}

				deathDate = personSelectData.getDeathDate();

				GenealogyData genData = new GenealogyData(personName, bornDate, deathDate);
				genData.setBornNumber(bornDateNumber);
				genData.setUserGeneratedIndex(visbleId);

				long person_RPID = personTable.getLong(bestNameField);
				if (!personDataIndex.containsKey(person_RPID))
					personDataIndex.put(person_RPID,genData);
			}

		// Generate all GenealogyPerson's
			GenealogyPerson personBranch;
			personTable.beforeFirst();
			while (personTable.next()) {		// set progress bar in Location Select

				if (updateMonitor) pointPersonHandler.setStatusPercent(pointPersonHandler.countStatusBar++);
				int userGenIndex = (int) personTable.getLong("VISIBLE_ID");
				personPID = personTable.getLong("PID");
				long bestNamePID = personTable.getLong(bestNameField);

			// Collect the person id String
				String personString = personDataIndex.get(bestNamePID).getPersonString();

			// Set sort number for children
				int bornDateNumber = personDataIndex.get(bestNamePID).getBornNumber();

			// Set up GenealogyPerson objects
				personBranch = new GenealogyPerson(personString, userGenIndex);
				personBranch.setPersonTableID(personPID);
				personBranch.setBornNumber(bornDateNumber);
				personBranchIndex.put(personPID,personBranch);
			}

		// Set up families
			personTable.beforeFirst();
			while (personTable.next()) {
				if (updateMonitor) pointPersonHandler.setStatusPercent(pointPersonHandler.countStatusBar++);
				long fatherPID = personTable.getLong(personFatherField);
				GenealogyPerson father = personBranchIndex.get(fatherPID);
				long motherPID = personTable.getLong(personMotherField);
				GenealogyPerson mother = personBranchIndex.get(motherPID);
				personPID = personTable.getLong("PID");
				GenealogyPerson person = personBranchIndex.get(personPID);

				if (father != null ) {
					father.addChildren(person);
					person.father = father;
				} else {
					person.father = new GenealogyPerson("No recorded father",0);
					person.father.addChildren(person);
				}
				if (mother != null) {
					mother.addChildren(person);
					person.mother = mother;
				} else {
					person.mother = new GenealogyPerson("No recorded mother",0);
					person.mother.addChildren(person);
				}

			// Generate partner list for Person in Tree
				Vector<Long> partners = null;
				GenealogyPerson partner = null;
				if (pointPersonSelect != null) {
					if (pointPersonSelect.pointPersonHandler.getPartnerList(personPID) != null) {
						partners = pointPersonSelect.pointPersonHandler.getPartnerList(personPID);
				        Iterator<Long> iter = partners.iterator();
				        while (iter.hasNext()) {
				          long parnerPID = iter.next();
				          partner = personBranchIndex.get(parnerPID);
							if (!person.partners.contains(partner))
								person.partners.add(partner);
				        }
					}
				}
			}

			return getFocusPerson(focusPersID);

		} catch (SQLException sqle) {
			if (HGlobal.DEBUG) System.out.println("Genealogy Tree - SQL Exception: " + sqle.getMessage());
			if (HGlobal.DEBUG)
				sqle.printStackTrace();
			throw new HBException("Create Tree error - PersID: " + focusPersID + "\n" +sqle.getMessage());
		}
    }	// End getGenealogyGraph

/**
 * GenealogyPerson getFocusPerson(int focusPersID)
 * @param focusPersID
 * @return
 * @throws HBException
 */
    private GenealogyPerson getFocusPerson(int focusPersID) throws HBException {
		// Find requested focus person in personTable
    	boolean personFound = false;
    	long focusPID;
    	dateFormatSelect();
    	try {
	    	personTable.beforeFirst();
			while (personTable.next()) {
				int personID = (int) personTable.getLong("VISIBLE_ID");
				if (personID == focusPersID) {
					personFound = true;
					break;
				}
			}

			if (personFound) {
				if (HGlobal.DEBUG)
					System.out.println("HBTreeCreator - getFocusPerson - Start person "
							+ focusPersID + " found!");
				focusPID = personTable.getLong("PID");
			} else {
				if (HGlobal.DEBUG)
					System.out.println("HBTreeCreator - getFocusPerson - Start person "
						+ focusPersID + " not found in project");
				if (!personTable.absolute(focusPersID)) {
					if (HGlobal.DEBUG) 
						System.out.println(" ERROR - HBTreeCreator - getFocusPerson - absolute error: " + focusPersID);
					//throw new HBException("HBTreeCreator - getFocusPerson - absolute error: " + focusPersID);
		/******* Eliminate last PID which is deleted *****************
		 * Eliminate last PID which is deleted 
		 ***********************************************************'*/
					personTable.absolute(1);
				}
				focusPID = personTable.getLong("PID");
			}
			return personBranchIndex.get(focusPID);

		} catch (SQLException sqle) {
			if (HGlobal.DEBUG) System.out.println("getFocusPerson - SQL Exception: " + sqle.getMessage());
			if (HGlobal.DEBUG)
				sqle.printStackTrace();
			throw new HBException("getFocusPerson - PersID: " + focusPersID + "\n" + sqle.getMessage());
		}
    }

/**
 * setFocusPerson(int focusPersID)
 * @param focusPersID
 * @throws HBException
 */
    public void setFocusPerson(int focusPersID) throws HBException {
    	dateFormatSelect();
    	//Set focus person in tree
    	GenealogyPerson focusPerson = getFocusPerson(focusPersID);
    	tree.setFocusPerson(focusPerson);
    }


/**
 * int calculateDateNumber(long hdatePID)
 * @param hdatePID
 * @return int calculated number
 * @throws SQLException
 * @throws HBException
 */
    private int calculateDateNumber(long hdatePID) throws SQLException, HBException {
		String selectString =
				setSelectSQL("*",dateTable,"PID = " + hdatePID);
		ResultSet hdateTable = requestTableData(selectString, dataBaseIndex);
		hdateTable.next();
    	return calculateDateNumber(hdateTable.getLong("MAIN_HDATE_YEARS"),
					hdateTable.getString("MAIN_HDATE_DETAILS"));
    }
/**
 * int calculateDateNumber(long years, String mainDetails)
 * @param years
 * @param mainDetails
 * @return
 */
	private int calculateDateNumber(long years, String mainDetails) {
		String outString = "" + years;
		// Check for irregular date first char = 0
		if (mainDetails.startsWith("I")) return 0;
		try {
			if (mainDetails.length() > 0) {
				if (!mainDetails.substring(3,7).contains("%"))
							outString = outString + mainDetails.substring(3,7);
			}
			return Integer.parseInt(outString);
		} catch (StringIndexOutOfBoundsException sobe) {
			System.out.println("HBTreeCreator - calculateDateNumber error: " + mainDetails);
			return 0;
		}
	}	// End calculateDateNumber

/**
 * GenealogyData (String personName, String bornDate, String deathDate)
 * @author NTo
 * @since 2020-04-05
 */
    class GenealogyData {
    	String personName;
    	String bornDate;
    	String deathDate;
    	int userVisibleId = 0;
    	int birthNumber = 0;

/**
 * Constructor GenealogyData
 * @param personName
 * @param bornDate
 * @param deathDate
 */
    	public GenealogyData(String personName, String bornDate, String deathDate) {
    		this.personName = personName;
    		this.bornDate = bornDate;
    		this.deathDate = deathDate;
    	}

 /**
  * long getBornNumber()
  * @return birthNumber
  */
    	public int getBornNumber() {
    		return birthNumber;
    	}

/**
 * setBornNumber(long dataNumber)
 * @param dataNumber
 */
    	public void setBornNumber(int dateNumber) {
    		birthNumber = dateNumber;
    	}

/**
 * setUserGeneratedIndex(int index)
 * @param index
 */
    	public void setUserGeneratedIndex(int index) {
    		userVisibleId = index;
    	}

/**
 * String getPersonString()
 * @return
 */
    	public String getPersonString() {
    		if (bornDate.trim().length() > 0 ) bornDate = " b. " + bornDate.trim();
			if (deathDate.trim().length() > 0 ) deathDate = " - d. " + deathDate.trim();
			String personString = personName + " (" + userVisibleId + ")" + bornDate + deathDate;
    		return personString;
    	}
    }	// End class GenealogyData

/**
 * GenealogyPerson
 * @author NTo
 * @since 2020-04-05
 */
	public class GenealogyPerson {
		public int visibleIndex = 0;
	    public GenealogyPerson father;
	    public GenealogyPerson mother;
	    public Vector<GenealogyPerson> children;
	    public Vector<GenealogyPerson> partners;
	    private String name;
	    private int birthNumber = 0;
	    private long personTablePID;

	    public GenealogyPerson(String name, int visibleIndex) {
	        this.name = name;
	        this.visibleIndex = visibleIndex;
	        mother = father = null;
	        children = new Vector<>();
	        partners = new Vector<>();
    }	// End class GenealogyPerson

/**
 * void addChildren(GenealogyPerson child) Add child and Sorts birth order
 * @param child
 */
	    public void addChildren(GenealogyPerson child) {

	    	int birthNumber = child.getBornNumber();
	    	int i = 0;
	    	if (birthNumber > 0) {
		    	if (children.size() > 1) {
		    		int limit = children.size();
		    		boolean notFound = true;
		    		for (i = 1; i < limit; i++) {
		    			if (birthNumber < children.get(i-1).getBornNumber()) {
		    				children.add(i-1,child);
		    				i = limit;
		    				notFound = false;
		    			} else if (birthNumber > children.get(i-1).getBornNumber()
	    					&& birthNumber < children.get(i).getBornNumber()) {
	    						children.add(i,child);
	    						i = limit;
	    						notFound = false;
	    					}
		    		}
		    		if (notFound) children.add(child);
		    	} else if (children.size() == 1) {
		    		if (birthNumber < children.get(0).getBornNumber()) children.add(0,child);
		    			else children.add(child);
		    	} else if (children.size() == 0) children.add(child);
	    	} else children.add(0,child);
	    } 	// End addChildren

	    public void setPersonTableID(long personPID) {
	    	personTablePID = personPID;
	    }

/**
 * setBornNumber(int dataNumber)
 * @param dataNumber
 */
	    public void setBornNumber(int dataNumber) {
	    	birthNumber = dataNumber;
	    }
/**
 * get methods
 */
	    public int getVisibleIndex() {
	    	return visibleIndex;
	    }

	    public long getPersonTablePID() {
	    	return personTablePID;
	    }

	    public int getBornNumber() {
	    	return birthNumber;
	    }
	    @Override
		public String toString() {
	    	return name;
	    }
	    public String getName() {
	    	return name;
	    }
	    public GenealogyPerson getFather() {
	    	return father;
	    }
	    public GenealogyPerson getMother() {
	    	return mother;
	    }
	    public Vector<GenealogyPerson> getPartners() {
	    	return partners;
	    }
	    public int getChildCount() {
	    	return children.size();
	    }
	    public GenealogyPerson getChildAt(int i) {
	        return children.elementAt(i);
	    }
	    public int getIndexOfChild(GenealogyPerson kid) {
	        return children.indexOf(kid);
	    }
	}	// End class GenealogyPerson

/**
 * GenealogyTree
 * @author NTo
 * @since 2020-04-05
 */
	public class GenealogyTree extends JTree implements TreeSelectionListener {
		private static final long serialVersionUID = 1L;

		GenealogyModel model;
		GenealogyPerson selectedPers;
		boolean personSelected = false;

	    public GenealogyTree(GenealogyPerson graphNode) {
	        super(new GenealogyModel(graphNode));
	        getSelectionModel().setSelectionMode(
	                TreeSelectionModel.SINGLE_TREE_SELECTION);
	        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
	        Icon personIcon = null;
	        renderer.setLeafIcon(personIcon);
	        renderer.setClosedIcon(personIcon);
	        renderer.setOpenIcon(personIcon);
	        setCellRenderer(renderer);
	        addTreeSelectionListener(this);
	        model = (GenealogyModel)getModel();
	    }

	    public boolean getPersonSelected() {
	    	return personSelected;
	    }

	    @Override
		public void valueChanged(TreeSelectionEvent arg0) {
	        GenealogyPerson selPers = getSelectPerson();
	        selectedPers = selPers;
	        personSelected = true;
		}

/**
 * void showAncestor(boolean bool)
 * Get the selected item in the tree, and call showAncestor with this
 * item on the model.
 * @param bool
 */
	    public void showAncestor(boolean bool) {
	        Object newRoot = null;
	        TreePath path = getSelectionModel().getSelectionPath();
	        if (path != null) {
	            newRoot = path.getLastPathComponent();
	        }
	        ((GenealogyModel)getModel()).showAncestor(bool, newRoot);
	    }

/**
 * Test change of Focus person
 * Change focus person
 * @param bool
 */
	    public void setFocusPerson(GenealogyPerson selPers) {
	    	Object newRoot = selPers;
	    	personSelected = false;
	    	((GenealogyModel)getModel()).showAncestor(true, newRoot);
	    }

/**
 * Get the selected item in the tree..
 */
	    public GenealogyPerson getSelectPerson() {
	        Object selPers = null;
	        TreePath path = getSelectionModel().getSelectionPath();
	        if (path != null) {
	            selPers = path.getLastPathComponent();
	            return (GenealogyPerson) selPers;
	        } else return model.getRootPerson();
	    }

/**
 * selectedPerson()
 * @return
 */
	    public  GenealogyPerson selectedPerson() {
	    	return selectedPers;
	    }
	}	// End class GenealogyTree

/**
 * class GenealogyModel implements TreeModel
 * @author NTo
 * @since 2020-03-30
 */
	public class GenealogyModel implements TreeModel {
	    private boolean showAncestors;
	    private Vector<TreeModelListener> treeModelListeners =
	        new Vector<>();
	    private GenealogyPerson rootPerson;

/**
 * GenealogyModel(GenealogyPerson root)
 * @param root
 */
	    public GenealogyModel(GenealogyPerson root) {
	        showAncestors = true;
	        rootPerson = root;
	    }

/**
 * GenealogyPerson getRootPerson()
 * @return
 */
	    public GenealogyPerson getRootPerson() {
	    	return rootPerson;
	    }

/**
 * Used to toggle between show ancestors/show descendant and
 * to change the root of the tree.
 */
	    public void showAncestor(boolean b, Object newRoot) {
	        showAncestors = b;
	        GenealogyPerson oldRoot = rootPerson;
	        if (newRoot != null) {
	           rootPerson = (GenealogyPerson)newRoot;
	        }
	        fireTreeStructureChanged(oldRoot);
	    }
//////////////// Fire events //////////////////////////////////////////////
/**
 * The only event raised by this model is TreeStructureChanged with the
 * root as path, i.e. the whole tree has changed.
 */
	    protected void fireTreeStructureChanged(GenealogyPerson oldRoot) {
	        TreeModelEvent e = new TreeModelEvent(this,
	                                              new Object[] {oldRoot});
	        for (TreeModelListener tml : treeModelListeners) {
	            tml.treeStructureChanged(e);
	        }
	    }
//////////////// TreeModel interface implementation ///////////////////////
/**
 * Adds a listener for the TreeModelEvent posted after the tree changes
 */
	    @Override
		public void addTreeModelListener(TreeModelListener l) {
	        treeModelListeners.addElement(l);
	    }

/**
 * Returns the child of parent at index index in the parent's child array
 */
	    @Override
		public Object getChild(Object parent, int index) {
	        GenealogyPerson p = (GenealogyPerson)parent;
	        if (showAncestors) {
	            if ((index > 0) && (p.getFather() != null)) {
	                return p.getMother();
	            }
	            return p.getFather();
	        }
	        return p.getChildAt(index);
	    }

/**
 * Returns the number of children of parent
 */
	    @Override
		public int getChildCount(Object parent) {
	        GenealogyPerson p = (GenealogyPerson)parent;
	        if (showAncestors) {
	            int count = 0;
	            if (p.getFather() != null) {
	                count++;
	            }
	            if (p.getMother() != null) {
	                count++;
	            }
	            return count;
	        }
	        return p.getChildCount();
	    }

/**
 * Returns the index of child in parent.
 */
	    @Override
		public int getIndexOfChild(Object parent, Object child) {
	        GenealogyPerson p = (GenealogyPerson)parent;
	        if (showAncestors) {
	            int count = 0;
	            GenealogyPerson father = p.getFather();
	            if (father != null) {
	                count++;
	                if (father == child) {
	                    return 0;
	                }
	            }
	            if (p.getMother() != child) {
	                return count;
	            }
	            return -1;
	        }
	        return p.getIndexOfChild((GenealogyPerson)child);
	    }
/**
 * Returns the root of the tree
 */
	    @Override
		public Object getRoot() {
	        return rootPerson;
	    }
/**
 * Returns true if node is a leaf
 */
	    @Override
		public boolean isLeaf(Object node) {
	        GenealogyPerson p = (GenealogyPerson)node;
	        if (showAncestors) {
	            return ((p.getFather() == null)
	                 && (p.getMother() == null));
	        }
	        return p.getChildCount() == 0;
	    }
/**
 * Removes a listener previously added with addTreeModelListener().
 */
	    @Override
		public void removeTreeModelListener(TreeModelListener l) {
	        treeModelListeners.removeElement(l);
	    }

/**
 * Messaged when the user has altered the value for the item
 * identified by path to newValue.  Not used by this model.
 */
	    @Override
		public void valueForPathChanged(TreePath path, Object newValue) {
	        System.out.println("*** valueForPathChanged : " + path + " --> " + newValue);
	    }
	} // End GenealogyModel
}	// End HBTreeCreator
