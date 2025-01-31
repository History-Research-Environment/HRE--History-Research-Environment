package hre.bila;
/**
 * Dummy class HBBusinessLayer
 */
import java.sql.ResultSet;

public class HBBusinessLayer {
	
/**
 * 	Pointer to HBLibraryResultSet
 */
	public HBLibraryResultSet pointLibraryResultSet;
	
/**
 * Pointer to HBLibraryBusiness
 */
	public HBLibraryBusiness pointLibraryBusiness;
	
/**
 * HDATE for person birth
 */
	public String personHDateBirth;
	
/**
 * Person table field
 */
	public String personTableField;
	
/**
 * Generate SQL SELECT string for database call
 * @param fieldSelector database table filed slector string
 * @param tableName database table name
 * @param searchConditions search conditions for table SELECT
 * @return SQL String for database select call
 */
	public String setSelectSQL(String fieldSelector, 
							   String tableName, 
							   String searchConditions) {
		return "";
	}
	
/**
 * Call to database with SQL request String.
 * @param sqlRequestString - String produced by setSelectSQL
 * @param databaseIndex for open database
 * @return ResultSet from database with the requested data 
 */
	public ResultSet requestTableData(String sqlRequestString, int databaseIndex) {
		return null;
	}
	
/**
 * Rearrange the event data according to the sequence in int[] sort
 * @param eventsData with the original non sorted event data
 * @param sort int[] with sort sequence if eventdata
 * @return Object[][] with sorted eventdata
 */
	public Object[][] sortTable(Object[][] eventsData, int[] sort) {
		return null;
	}
	
/**
 * Set up date translation	
 * @param hdateT175table
 * @param hdateT204data
 */
	//public void setUpT175data(ResultSet hdateT175table, ResultSet hdateT204data) {}
/**
 * 	
 * @param openProject
 * @param language
 */
	//public void setUpT175data(HBProjectOpenData openProject, String language) {}
/**
 * 
 */
	public void setUpDateTranslation() {}
/**
 * 
 */
	public void dateFormatSelect() {}
} // End Class

	 