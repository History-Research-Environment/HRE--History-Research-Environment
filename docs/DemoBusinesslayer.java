/********************************************************************
 * Title: Proposal for RequestManager classes for Client
 * Project: HRE
 * Author: Nils Tolleshaug
 * Date: 6.8.2018
 ********************************************************************/

import java.io.InputStream;

/**
 * Demonstration class for testing of RequestManager
 * This class can be extended to include GUI for use
 * parameter input
 */

class DemoBusinesslayer {

	RequestManager pointManager;
	boolean localPC = true;

	// For record #5 table person return ancestors
	String ancestorObject = "ancestors/person#5";
	//update record #5 table person
	String personObject = "person#5";

	String sessionID = "XYZ......123";
	String newContent = "";
	String generations = "5";

/**
 * Constructor for DemoBusinesslayer()
 */
	public void DemoBusinesslayer() {

/**
 * Select Client or Bridge mode
 */
		if (localPC)
			pointManager = new LocalRequestManager();
		else
			pointManager = new RemoteRequestManager();

/**
 * Request data from HRE database
 */
		processJSONdata(pointManager.getAncestors(sessionID,ancestorObject,generations));

/**
 * Request update of person data in HRE database
 */
		String responseCode = pointManager.updateHREobject(sessionID,personObject,newContent);
	}

/**
 * Method for prosessing and presentation of JSON data in GUI
 */

	private void processJSONdata (InputStream inpStr) {

		// Do some processing of received JSON data.stream
	}

/**
 * Method for prosessing and presentation of JSON data in GUI
 */

	private void updataPersonData(String responseCode) {

		// Do some processing of received statusCode
	}

/**
 * Create intance of demoBusinesslayer
 */
	public static void main(String args[]) {
		DemoBusinesslayer testUrl= new DemoBusinesslayer();
   } // End Main
}


/********************************************************************
 * The following section contains class RemoteRequestManager
 * and class LocalRequestManager both subclasses of
 * class RequestManager
 *
 */

abstract class RequestManager {

/**
 * Examples of abstract methods for requesting data to be prensented in HRE GUI
 */

/**
 * Abstract methods for requesting ancestors from HRE database
 */
	public abstract InputStream getAncestors(String sessionID,String restObject,String nrofGen);

/**
 * Abstract methods for updating object in HRE database
 */
	public abstract String updateHREobject(String sessionID,String restObject,String newContent);

/**
 * A standard HTTP REST data request method
 * Sends HTTP REST datarequest over IP network and return JSON datastream
 */
	InputStream httpsRequest(String sessionID,String httpMethod,String restURL, String parameters) {
		InputStream instream = null; //  = HTTPS call
		return instream;
	}
/**
 * A standard HTTP REST data request method
 * Sends HTTP RESR datarequest over IP network
 * to update a collection of items in HRE database
 */

	String httpsRequestUpdate(String sessionID,String method,String restURL,String parameter) {
		String responceFromServer = null; //  = HTTPS call
		return responceFromServer;
	}
}


/********************************************************************
 * Used when HRE database is on HRE server and Client is on local PC
 * Handling HTTPS requests to Server Request Listener
 * Returns JSON coded datastream
 */

class RemoteRequestManager extends RequestManager {


	String hreURL = "https://historyresearchenvironment.org/";

/**
 * Method for sending HTTP REST request to server
 */
	public InputStream getAncestors(String sessionID,String restObject,String nrofGen) {
		String method = "GET";
		String restURL = hreURL + restObject;
		String parameter = "generations=" + nrofGen;
		InputStream inputStream = httpsRequest(sessionID,method,restURL,parameter);
		return inputStream;
	}

	public String updateHREobject(String sessionID,String restObject,String newContent) {

		String method = "PUT";
		String restURL = hreURL + restObject;
		String responceFromServer = httpsRequestUpdate(sessionID,method,restURL,newContent);
		return responceFromServer;
	}
}

/********************************************************************
 * Used when HRE database and Client is on local PC
 * Handling SQL requests to HRE database
 * Returns JSON coded datastream
 */

class LocalRequestManager extends RequestManager {

/**
 *	Request data from HRE database in the local PC mode
 *	Variable sessionID is not necessary,
 *  but needed for method to be compatible with abstract method in RequestManager.
 */
	public InputStream getAncestors(String sessionID,String restObject,String nrofGen) {
		InputStream inputStream = sqlHREdatabase(restObject,nrofGen);
		return inputStream;
	}

/**
 * Method to send SQL request to HRE database and return JSON datastream
 */

	private InputStream sqlHREdatabase(String restObject,String nrofGen) {
		// Prepare SQL to HRE database and write to OutputStream
		InputStream inputStream = null; // Send SQL to database and return JSON stream
		return inputStream;
	}

/**
 * Method to send SQL request to HRE database and return statuscode
 */
	public String updateHREobject(String sessionID,String restObject,String newContent) {
		// Prepare SQL update to HRE database and return response code
		// Request updata of HRE database object
		String responceFromServer = ""; // SQL request to HRE database
		return responceFromServer;
	}
}

//*******************************************************************

