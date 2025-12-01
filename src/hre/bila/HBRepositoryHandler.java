package hre.bila;
/************************************************************************************************
 * Class  HBRespositoryHandler extends BusinessLayer
 * Processes data for Evidence/Source-Repository menus in User GUI
 * Receives requests from User GUI to action methods
 * Sends requests to database over Database Layer API
 ************************************************************************************************
 * v0.04.0032 2025-10-25 - First draft (D Ferguson)
 *			  2025-11-01 - Added code for add and edit repository records (N. Tolleshaug)
 *			  2025-11-02 - Added repo routines removed from HBCitationSourceHandler (D Ferguson)
 *			  2025-11-02 - Completed code for repositories (N.Tolleshaug)
 *			  2025-11-07 - Code for add/delete repositories for source (N.Tolleshaug)
 *			  2025-11-08 - Code for delete and check repository is in use (N.Tolleshaug)
 *			  2025-11-11 - Replaced getRepoPIDs with getRepoLinkData (D Ferguson)
 *            2025-11-16 - New handling of repos data and save data for repos link (N. Tolleshaug)
 *********************************************************************************************/

import java.sql.ResultSet;
import java.sql.SQLException;

import hre.gui.HG0566EditSource;
import hre.gui.HG0569ManageRepos;
import hre.gui.HG0570EditRepository;
import hre.tmgjava.HCException;

public class HBRepositoryHandler extends HBBusinessLayer {

	long null_RPID  = 1999999999999999L;
	long proOffset  = 1000000000000000L;

	HBProjectOpenData pointOpenProject;
	public HBNameStyleManager pointLocationStyleData;
	HG0570EditRepository pointEditRepository;
	HG0570EditRepository pointAddRepository;
	public HG0566EditSource pointEditSource;
	public HG0569ManageRepos pointManageRepos;
	HREmemo pointHREmemo;
	int dataBaseIndex;

	String selectString;
	String[] nameStyleData = {"","","",""};
	Object[] repositorySaveData;
	Object[][] tableRepoData = null;

// Repos SourceLink variables T740
	Object[][] repoLinkData = null;
	String reposReference = "";
	long linkedSourceTablePID, reposTablePID, selectedSourceTablePID;

	public int getDefaultStyleIndex() {
		return pointLocationStyleData.getDefaultStyleIndex();
	}

/**
 * Constructor HBRepositoryHandler
 */
	HBRepositoryHandler(HBProjectOpenData pointOpenProject) {
		super();
		this.pointOpenProject = pointOpenProject;
		pointDBlayer = pointOpenProject.pointProjectHandler.pointDBlayer;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		pointHREmemo = new HREmemo(pointDBlayer, dataBaseIndex);
		pointLocationStyleData = new HBNameStyleManager(pointDBlayer, dataBaseIndex, "P");
	}

/**
 * activateEditRespository(HBProjectOpenData pointOpenProject, long repositoryTablePID)
 * @param pointOpenProject
 * @param repositoryTablePID
 * @param addRespository
 * @return pointEditRepository
 * @throws HBException
 */
	public HG0570EditRepository activateEditRepository(HBProjectOpenData pointOpenProject,
														long repositoryTablePID) throws HBException {
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		pointLocationStyleData.updateStyleTable("P", nameStyles, nameElementsDefined);
		pointEditRepository = new HG0570EditRepository(pointOpenProject, true, repositoryTablePID);
		return  pointEditRepository;
	}

/**
 * activateeAddRespository(HBProjectOpenData pointOpenProject)
 * @param pointOpenProject
 * @return pointAddRepository
 * @throws HBException
 */
	public HG0570EditRepository activateeAddRepository(HBProjectOpenData pointOpenProject, int largestSeqNr) throws HBException {
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		pointLocationStyleData.updateStyleTable("P", nameStyles, nameElementsDefined);
		pointAddRepository = new HG0570EditRepository(pointOpenProject, false , null_RPID);
		pointAddRepository.updateAddSequenceNumber(largestSeqNr);
		return  pointAddRepository;
	}

/**
 * public void addRepositoryTable(long locationTabelPID, Object[] respositorySaveData)
 * @param locationTabelPID
 * @param respositorySaveData
 * @throws HBException
 */
	public void addRepositoryTable(long locationTabelPID, Object[] respositorySaveData) throws HBException {
		this.repositorySaveData = respositorySaveData;
		ResultSet repositoryTableRS;
		long nextReposityryTablePID = lastRowPID(repoTable, dataBaseIndex) + 1;
		selectString = setSelectSQL("*", repoTable, "PID = " + (nextReposityryTablePID-1));
		repositoryTableRS = requestTableData(selectString, dataBaseIndex);
		insertRowT739_REPO(nextReposityryTablePID, repositoryTableRS);
	}

/**
 * updateRespositoryTable(long repositoryTablePID, Object[] respositorySaveData)
 * @param respositoryTablePID
 * @param respositorySaveData
 * @throws HBException
 */
	public void updateRepositoryTable(long respositoryTablePID, Object[] repositorySaveData) throws HBException {
		ResultSet repositoryTableRS;
		this.repositorySaveData = repositorySaveData;
		selectString = setSelectSQL("*", repoTable, "PID =" + respositoryTablePID);
		try {
			repositoryTableRS = requestTableData(selectString, dataBaseIndex);
			if (isResultSetEmpty(repositoryTableRS))  return;
			repositoryTableRS.first();
			updateRepositoryT739_REPO(repositoryTableRS);
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" updateRepositoryTable error: " + sqle.getMessage());
		}
	}

/**
 * public void deleteRepository(long repositoryTablePID)
 * @param repositoryTablePID
 * @throws HBException
 */
	public void deleteRepository(long repositoryTablePID) throws HBException {
		ResultSet repositoryTableRS, sourecRespositoryLinkRS = null;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", sourceLinkTable, "REPO_RPID = " + repositoryTablePID);
		sourecRespositoryLinkRS = requestTableData(selectString, dataBaseIndex);
		try {
		// Check if repositiry is used in T740
			if (!isResultSetEmpty(sourecRespositoryLinkRS))
				throw new HBException(" Repository in use!");
			selectString = setSelectSQL("*", repoTable, "PID =" + repositoryTablePID);
			repositoryTableRS = requestTableData(selectString, dataBaseIndex);
		// Check if repository exist in repoTable
			if (isResultSetEmpty(repositoryTableRS))
				throw new HBException(" Repository - not found!");
			repositoryTableRS.first();
			repositoryTableRS.deleteRow();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" delete RepositoryTable error: " + sqle.getMessage());
		}
	}

/**
 * getLinkedRepository() throws HBException
 * @return
 * @throws HBException
 */
	public Object[] getLinkedRepository(long sourcePID) throws HBException {
		// Find a Primary Source/Repo Link (T740) for the given Source PID,
		// and return the linked REPO_PID and REPO_REF, of the last Primary link data.
		// T740_SORC_LINK table format is:
		//		PID BIGINT
		//		IS_PRIMARY BOOLEAN
		//		SORC_RPID BIGINT
		//		REPO_RPID BIGINT
		//		REPO_REF CHAR(25)

		// Initialise a null return Object
		Object[] repoLinkData = new Object[2];
		repoLinkData[0] = null_RPID;
		repoLinkData[1] = "";
		// Get the link table ResultSet
		ResultSet repoLinkRS;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", sourceLinkTable, "SORC_RPID=" + sourcePID);
		repoLinkRS = requestTableData(selectString, dataBaseIndex);
		try {
			if (isResultSetEmpty(repoLinkRS)) return repoLinkData;
			repoLinkRS.beforeFirst();
			while (repoLinkRS.next()) {
				if (repoLinkRS.getBoolean("IS_PRIMARY")) {
					repoLinkData[0] = repoLinkRS.getLong("REPO_RPID");
					repoLinkData[1] = repoLinkRS.getString("REPO_REF");
				}
			}
		} catch (SQLException sqle) {
			System.out.println(" getRepositoryLink() error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" getRepositoryLink() error: " + sqle.getMessage());
		}
		return repoLinkData;
	}		// End getLinkedRepository


/**
 * getRepositoryData() throws HBException
 * @return
 * @throws HBException
 */
	public Object[] getRepositoryData(long repoPID) throws HBException {
		// Get the Repository (T739) table for the given Repo PID,
		// and return all data from that reord.
		// T739_REPO table format is:
		//		PID BIGINT
		//		CL_COMMIT_RPID BIGINT
		//		REPO_MEMO_RPID BIGINT
		//		REPO_ADDR_RPID BIGINT
		//		REPO_ABBREV CHAR(50)
		//		REPO_REF SMALLINT
		//		REPO_NAME VARCHAR(500)

		Object[] repoData = new Object[6];
		long repositoryLocationPID;
		String[] locationNameStyle;
		ResultSet repoRS;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		locationNameStyle = getNameStyleOutputCodes("P", dataBaseIndex);
		selectString = setSelectSQL("*", repoTable, "PID=" + repoPID);
		repoRS = requestTableData(selectString, dataBaseIndex);
		try {
			repoRS.first();
			repoData[0] = repoRS.getString("REPO_NAME");
			repoData[1] = repoRS.getString("REPO_ABBREV");
			repoData[2] = repoRS.getInt("REPO_REF");
			repoData[3] = pointHREmemo.readMemo(repoRS.getLong("REPO_MEMO_RPID"));
			repositoryLocationPID = repoRS.getLong("REPO_ADDR_RPID");
			repoData[4]  = pointLibraryResultSet
						.selectLocationName(repositoryLocationPID, locationNameStyle, dataBaseIndex);
			repoData[5] = repoRS.getLong("REPO_ADDR_RPID");
		} catch (SQLException sqle) {
			System.out.println(" getRepositoryData() error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" getRepositoryData() error: " + sqle.getMessage());
		}
		return repoData;
	}		// End getRepositoryData

/**
 * Object[][] getRepoList() throws HBException
 * @return
 * @throws HBException
 */
	public Object[][] getRepoList(boolean keepRepoList) throws HBException {
		// Load all T739 REPO records
		int index = 0;
		ResultSet repoRS;

	// Load pre populated list if done before
		if (keepRepoList) if (tableRepoData != null) return tableRepoData;

	// Continue with loading repositorylist
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", repoTable, "");
		repoRS = requestTableData(selectString, dataBaseIndex);
		try {
			repoRS.last();
			tableRepoData = new Object[repoRS.getRow()][3];
			repoRS.beforeFirst();
			while (repoRS.next()) {
				tableRepoData[index][0] = repoRS.getInt("REPO_REF");
				tableRepoData[index][1] = repoRS.getString("REPO_ABBREV");
				tableRepoData[index][2] = repoRS.getLong("PID");
				index++;
			}
		} catch (SQLException sqle) {
			System.out.println(" getRepoList() error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" getRepoList() error:  " + sqle.getMessage());
		}
		return tableRepoData;
	}

/**
 * long[] getRepoLinkData() throws HBException
 * @return
 * @throws HBException
 */
	public Object[][] getRepoLinkData(long sourcePID) throws HBException {
		// Load all T740 SORC_LINK records for this SourcePID
		// and extract the related Repository PIDs, Reference and Primary
		int index = 0;
		ResultSet sorcLinkRS;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", sourceLinkTable, "SORC_RPID=" + sourcePID);
		sorcLinkRS = requestTableData(selectString, dataBaseIndex);
		try {
			sorcLinkRS.last();
			repoLinkData = new Object[sorcLinkRS.getRow()][3];
			sorcLinkRS.beforeFirst();
			while (sorcLinkRS.next()) {
				repoLinkData[index][0] = sorcLinkRS.getLong("REPO_RPID");
				repoLinkData[index][1] = sorcLinkRS.getString("REPO_REF").trim();
				repoLinkData[index][2] = sorcLinkRS.getBoolean("IS_PRIMARY");
				index++;
			}
		} catch (SQLException sqle) {
			System.out.println(" repoLinkData error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" repoLinkData error: " + sqle.getMessage());
		}
		return repoLinkData;
	}

/**
 * locationNameStyleData(long locationNamePID)
 * @param locationNamePID
 * @return
 * @throws HBException
 */
	public int locationNameStyleData(long locationNamePID) throws HBException {
		long nameStyleRPID = null_RPID;
		ResultSet locationNameResultSet, nameStyleTable ;
		int locationNameStyleIndex = 0, index = 0;
		selectString = setSelectSQL("*", locationNameTable, "PID = " + locationNamePID);
		locationNameResultSet = requestTableData(selectString, dataBaseIndex);
		try {
			locationNameResultSet.last();
			if (locationNameResultSet.getRow() == 0) {
				System.out.println(" ** Location Table RS empty!");
				return 0;
			}
			locationNameResultSet.first();
			nameStyleRPID = locationNameResultSet.getLong("NAME_STYLE_RPID");
			nameStyleTable = pointLibraryResultSet.getNameStylesTable(nameStyles, "P", dataBaseIndex);
			nameStyleTable.last();
			if (nameStyleTable.getRow() == 0) {
				System.out.println(" ** Name Style Table RS empty!");
				return 0;
			}
			nameStyleTable.beforeFirst();
			while (nameStyleTable.next()) {
				if (nameStyleRPID == nameStyleTable.getLong("PID")) {
					nameStyleData[0] = nameStyleTable.getString("NAME_STYLE").trim();
					nameStyleData[1] = nameStyleTable.getString("NAME_STYLE_DESC").trim();
					nameStyleData[2] = nameStyleTable.getString("ELEMNT_NAMES").trim();
					nameStyleData[3] = nameStyleTable.getString("ELEMNT_CODES").trim();
					locationNameStyleIndex = index;
				}
				index++;
			}
		} catch (SQLException hbe) {
			throw new HBException("ManagePersonNameData - updateStyleAndDates: " + hbe.getMessage());
		}
		return locationNameStyleIndex;
	}

/**
 * 	setSelectedReposPID(long reposTablePID)
 * @param reposTablePID
*/
	public void setSelectedSourceTablePID(long sourceTablePID) {
		this.selectedSourceTablePID = sourceTablePID;
	}

/**
 * public void addNewRespositoryLink(long repositoryTablePID)
 * @param repositoryTablePID
 * @throws HBException
 */
	public void addNewRespositoryLink(long repositoryTablePID) throws HBException {
		this.reposTablePID = repositoryTablePID;
		ResultSet sourecRespositoryLinkRS = null;
		long T740tablePID = null_RPID;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		T740tablePID = lastRowPID(sourceLinkTable, pointOpenProject) + 1;
		selectString = setSelectSQL("*", sourceLinkTable, " PID = " + (T740tablePID - 1));
		sourecRespositoryLinkRS = requestTableData(selectString, dataBaseIndex);
		linkedSourceTablePID = selectedSourceTablePID;
	// Add new source repository link
		addToSourLinkT740_SORC_LINK(T740tablePID, sourecRespositoryLinkRS);
	// Reset repository table
		pointEditSource.resetRepositoryTable();
	}
	
/**
 * public void updateRespositoryLink(long sourceTablePID, long repositoryTablePID, String linkreference)
 * @param sourceTablePID
 * @param repositoryTablePID
 * @param linkreference
 * @throws HBException
 */
	public void updateRespositoryLink(long sourceTablePID, long repositoryTablePID, 
			String linkreference, boolean primaryLink) throws HBException {
		ResultSet sourecRespositoryLinkRS = null;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", sourceLinkTable,
				" SORC_RPID = " + sourceTablePID + " AND REPO_RPID = " + repositoryTablePID);
		sourecRespositoryLinkRS = requestTableData(selectString, dataBaseIndex);
		try {
			sourecRespositoryLinkRS.first();
			sourecRespositoryLinkRS.updateString("REPO_REF", linkreference);
			sourecRespositoryLinkRS.updateBoolean("IS_PRIMARY", primaryLink);
			sourecRespositoryLinkRS.updateRow();
		// Reset repository table
			pointEditSource.resetRepositoryTable();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" updateRespositoryLink sql error: " + sqle.getMessage());
		}
	}

/**
 * public void deleteRespositoryLink(long sourceTablePID)
 * @param sourceTablePID
 * @throws HBException
 */
	public void deleteRespositoryLink(long sourceTablePID, long repositoryTablePID) throws HBException {
		ResultSet sourecRespositoryLinkRS = null;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		selectString = setSelectSQL("*", sourceLinkTable,
				" SORC_RPID = " + sourceTablePID + " AND REPO_RPID = " + repositoryTablePID);
		sourecRespositoryLinkRS = requestTableData(selectString, dataBaseIndex);
		try {
			sourecRespositoryLinkRS.first();
			sourecRespositoryLinkRS.deleteRow();
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new HBException(" deleteRespositoryLink sql error: " + sqle.getMessage());
		}
	}

/**
 * private void addToSourLinkT740_SORC_LINK(long T740tablePID, ResultSet hreTable)
 * @param T740tablePID
 * @param hreTable
 * T740_SORC_LINK
 * PID
 * IS_PRIMARY
 * SORC_RPID
 * REPO_RPID
 * REPO_REF
 */
	private void addToSourLinkT740_SORC_LINK(long T740tablePID, ResultSet hreTable) {
		try {
		// Move cursor to the insert row
			hreTable.moveToInsertRow();

		// Update new row in H2 database
			hreTable.updateLong("PID", T740tablePID);
			hreTable.updateBoolean("IS_PRIMARY", true);
			hreTable.updateLong("SORC_RPID", linkedSourceTablePID);
			hreTable.updateLong("REPO_RPID", reposTablePID);
			hreTable.updateString("REPO_REF", reposReference);

		//Insert row
			hreTable.insertRow();

		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}

/**
 * private void insertRowT739_REPO(int index_R_Table, long T739tablePID, ResultSet hreTable)
		   CREATE TABLE T739_REPO(
			PID BIGINT NOT NULL,
			CL_COMMIT_RPID BIGINT NOT NULL,
			REPO_MEMO_RPID BIGINT NOT NULL,
			REPO_ADDR_RPID BIGINT NOT NULL,
			REPO_ABBREV CHAR(50) NOT NULL,
			REPO_REF SMALLINT NOT NULL,
			REPO_NAME VARCHAR(500) NOT NULL
			);
 * @throws HCException
 */
	private void insertRowT739_REPO(long T739tablePID, ResultSet hreTable) throws HBException {
		try {
		    // moves cursor to the insert row
				hreTable.moveToInsertRow();
			// Update new row in H2 database
				hreTable.updateLong("PID", T739tablePID);
				hreTable.updateLong("CL_COMMIT_RPID", null_RPID);

				String memoText = (String)repositorySaveData[3];
			// Processing memo to T167_MEMO_SET
				if (memoText.length() == 0) hreTable.updateLong("REPO_MEMO_RPID", null_RPID);
				else hreTable.updateLong("REPO_MEMO_RPID", pointHREmemo.addMemoRecord(memoText));
				hreTable.updateLong("REPO_ADDR_RPID", (long)repositorySaveData[4]);

				hreTable.updateString("REPO_ABBREV", (String)repositorySaveData[1]);
				hreTable.updateInt("REPO_REF", (int)repositorySaveData[2]);
				hreTable.updateString("REPO_NAME", (String)repositorySaveData[0]);
			//Insert row
				hreTable.insertRow();

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			System.out.println("insertRowT739_REPO - error: " + sqle.getMessage());
			throw new HBException("insertRowT739_REPO - error: " + sqle.getMessage());
		}
	}


/**
 * private void updateRepositoryT739_REPO(long T739tablePID, ResultSet hreTable)
 * @param T739tablePID
 * @param hreTable
 * @throws HBException
 *  T739_REPO
 	PID
 	CL_COMMIT_RPID
 	REPO_MEMO_RPID
 	REPO_ADDR_RPID
 	REPO_ABBREV
 	REPO_REF
 	REPO_NAME
 */
	private void updateRepositoryT739_REPO(ResultSet hreTable) throws HBException {
		long memoTablePID;
		try {
			hreTable.updateString("REPO_NAME", (String)repositorySaveData[0]);
			hreTable.updateString("REPO_ABBREV", (String)repositorySaveData[1]);
			hreTable.updateInt("REPO_REF", (int)repositorySaveData[2]);
			String memoText = (String)repositorySaveData[3];
		// Update source memo text
			memoTablePID = hreTable.getLong("REPO_MEMO_RPID");
			if (memoTablePID == null_RPID)
				hreTable.updateLong("REPO_MEMO_RPID", pointHREmemo.addMemoRecord(memoText));
			else
				pointHREmemo.findT167_MEMOrecord(memoText, memoTablePID);
		// Update database
			hreTable.updateRow();
		} catch (SQLException sqle) {
			System.out.println(" getRepositoryData() error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" getRepositoryData() error: " + sqle.getMessage());
		}
	}
}		// End HBREpositoryHandler
