package hre.bila;
/**
 * HBLibraryResultSet_v21a
 * @author NTo
 *
 */
public class HBLibraryResultSet {

	
	public HBLibraryResultSet(HBBusinessLayer pointBusinessLayer) {}
	
/**
 * return place data from result set
 * @param locationNameSelected ResultSet for Lacation names 
 * @return String[] with location name data
 

	public String[] getPlaceData(ResultSet locationNameSelected) throws SQLException {
		return null;
	}
*/	
/**
 * Select HDATE string from HDATE table
 * @param eventHDatePID PID in HDATE table
 * @param dataBaseIndex open databaseIndex
 * @return HDATE String
 * @throws HBException when ERROR
 */
	public String exstractDate(long eventHDatePID, int dataBaseIndex) throws HBException {
		return null;
	}
	
/**
 * Select HDATE string for event
 * @param eventHDatePID PID for event HDATE
 * @param dataBaseIndex open database index
 * @return HDATE string
 * @throws HBException when ERROR
 */
	public String exstractSortString(long eventHDatePID, int dataBaseIndex) throws HBException{
		return null;
	}
	
/**
 * Select place data elements
 * @param locationName_RPID long 
 * @param dataBaseIndex open database index
 * @return String[] with place dataelements
 * @throws HBException when ERROR
 */
	public String[] selectLocationNameElements(long locationName_RPID, int dataBaseIndex) throws HBException {
		return null;
	}
	
/**
 * Calculates the actual age when the vent occur.
 * @param eventHDatePID HDATE for actual event
 * @param selectPersonPID PID for person
 * @param databaseIndex index to open database
 * @return String with actual age depending of event date
 */
	public String calculateAge(long eventHDatePID, long selectPersonPID, int databaseIndex) {
		return "";
	}

/**
 * Get the language translated event name.
 * @param int eventNumber - event number type
 * @param String langCode - the report language code
 * @param databaseIndex index to open database
 * @return String with the translated event name
 */
	public String getEventName(int eventNumber, String langCode, int dataBaseIndex) throws HBException {
		return "";
	}
	
/**
 * selectLocationName(long locationName_RPID, String[] elemntCodes, int dataBaseIndex )	
 * @param locationName_RPID
 * @param elemntCodes
 * @param dataBaseIndex
 * @return
 */
	public String selectLocationName(long locationName_RPID, String[] elemntCodes, int dataBaseIndex ) {return "-";}
	
/**
 * Get the language translated role name 
 * @param String eventRoleCode - role String ex "00001"
 * @param int eventNumber - event number type
 * @param String langCode - the report language code
 * @param databaseIndex index to open database
 * @return String with the translated role name
 */

	public String getRoleName(String eventRoleCode, 
			  int eventNumber, 
			  String langCode, 
			  int dataBaseIndex) throws HBException { return "";}


} // End class
