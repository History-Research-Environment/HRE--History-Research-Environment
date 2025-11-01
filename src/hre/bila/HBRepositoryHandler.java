package hre.bila;

import java.sql.ResultSet;
import java.sql.SQLException;

/************************************************************************************************
 * Class  HBRespositoryHandler extends BusinessLayer
 * Processes data for Evidence/Source-Repository menus in User GUI
 * Receives requests from User GUI to action methods
 * Sends requests to database over Database Layer API
 ************************************************************************************************
 * v0.04.0032 2025-10-25 - First draft (D Ferguson)
 *
 *********************************************************************************************/

public class HBRepositoryHandler extends HBBusinessLayer {

	long null_RPID  = 1999999999999999L;
	long proOffset  = 1000000000000000L;

	HBProjectOpenData pointOpenProject;
	int dataBaseIndex;

	String selectString;
	String sourceLinkTable = "T740_SORC_LINK";
	String repoTable = "T739_REPO";
	
// Repos SourceLink variables T740
		String reposReference;
		long linkedSourceTablePID, reposTablePID;

/**
 * Constructor HBRepositoryHandler
 */
	HBRepositoryHandler(HBProjectOpenData pointOpenProject) {
		super();
		this.pointOpenProject = pointOpenProject;
		dataBaseIndex = pointOpenProject.getOpenDatabaseIndex();
		pointHREmemo = new HREmemo(pointDBlayer, dataBaseIndex);
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

		Object[] repoData = new Object[5];
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
			//System.out.println(" Repos pysical address: " + repoData[4]);
		} catch (SQLException sqle) {
			System.out.println(" getRepositoryData() error: " + sqle.getMessage());
			sqle.printStackTrace();
			throw new HBException(" getRepositoryData() error: " + sqle.getMessage());
		}
		return repoData;

	}		// End getRepositoryData
	
/**
 * 	setSelectedReposPID(long reposTablePID)
 * @param reposTablePID
 */
	public void setSelectedReposPID(long reposTablePID) {
		this.reposTablePID = reposTablePID;
	}

/**
 * public void addNewRespositoryLink()
 */

	public void addNewRespositoryLink() {
		ResultSet sourecRespositoryLinkRS = null;
		long T740tablePID = null_RPID;
		// Code to add new source repository link
		addToSourLinkT740_SORC_LINK(T740tablePID, sourecRespositoryLinkRS);
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

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}		// End HBREpositoryHandler
