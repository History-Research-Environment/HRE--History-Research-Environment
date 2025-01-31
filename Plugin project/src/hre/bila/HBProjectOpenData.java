package hre.bila;
import java.util.HashMap;
/**
 * Dummy class HBProjectOpenData
 * @author NTo
 *
 */
public class HBProjectOpenData {
/**
 * Hash Map for indexing person Table PID with visible index
 */
	public HashMap<Integer,Long> visibleIdIndex;
	HBPersonHandler pointPersonHandler;
	
/**
 * Return open project data
 * @return String[] with selected open project data
 */
	public String[] getProjectData() {
		return null;
	}
	
/**
 * Return open database index
 * @return int open database index
 */
	public int getOpenDatabaseIndex() {
		return 0;
	}
	
/**
 * Return selected person visible index
 * @return person visible index
 */
	public int getSelectedPersonIndex() {
		return 0;
	}
	
/**
 * getSelectedPersonPID()
 * @return
 */
    public long getSelectedPersonPID() {
    	return 0L;
    }
    
    public HBPersonHandler getPersonHandler() {
    	return pointPersonHandler;
    }
	
/**
 * Return selected person name
 * @return String person name
 */
	public String getSelectedPersonName() {
		return null;
	}
/*
	public ResultSet getT175Hdata() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public ResultSet getT204Hdata(String reptLanguage) {
		// TODO Auto-generated method stub
		return null;
	}
*/	
	public String getReportLanguage() {
		// TODO Auto-generated method stub
		return null;
}
}
