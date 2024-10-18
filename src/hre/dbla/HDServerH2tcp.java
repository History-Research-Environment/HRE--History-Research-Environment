package hre.dbla;
import java.sql.Connection;
/*************************************************************************
* Database remote server and connect
* ************************************************************************
* NOTE only preliminary implementation
* For operation on HRE database on remote server it must be possible
* for multiple users to open HRE databases. This is not foreseen in this
* Database Layer design. Need further design work in project.
***************************************************************************
* v0.00.0026 2021-05-22 - First version (N. Tolleshaug)
*            2021-05-22 - connect to fixed IP and port (N. Tolleshaug)
* 			 2021-05-27 - Implemented server in app config  (N. Tolleshaug)
* 			 2021-09-09 - Implemented SSL for TCP server   (N. Tolleshaug)
****************************************************************************/
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import org.h2.tools.Server; //imports the server utility

import hre.gui.HGlobal;

/***
 * class HDEtcpH2server methods for connect
 * and start of remote tcp server
 * @author Nils Tolleshaug
 * @version 2021-05-22
 */
public class HDServerH2tcp {

    Server server; //the server's instance variable
    String serverStatus  = "";
    String url;
    String outputConn = null; // String which will hold important messages
    String connMessage;

	private static String SERVER_PORT = "9092"; //Default port number

/**
 * 	HDServerH2tcp() constructor
 */
	public HDServerH2tcp(String portNumber) {
		if (HGlobal.DEBUG)
			System.out.println(" HDSeverH2tcp class initiated!");
/**
 * Set port number for the first server in HGlobal server list
 */
		SERVER_PORT = portNumber;
/**
 * Set up path to keyStore and trustStore which basically contains a self signed ssl certificate
 * Your keystore contains 1 entry
 * hre-sslcert, 8. sep. 2021, PrivateKeyEntry,
 * Certificate fingerprint (SHA-256): ED:52:84:F7:22:E4:D1:74:63:EC:F7:23:8A:20:57:DC:99:0C:30:59:B8:A7:CB:C9:90:4D:56:D6:D5:90:6C:BD
 */

		System.setProperty("javax.net.ssl.keyStore", HGlobal.keyStorePath + "serverKeystore.jks");
		System.setProperty("javax.net.ssl.keyStorePassword", "servStore");
		System.setProperty("javax.net.ssl.keyPassword", "servStore");
		System.setProperty("javax.net.ssl.trustStore", HGlobal.keyStorePath + "serverKeystore.jks");
		System.setProperty("javax.net.ssl.trustStorePassword","servStore");
		System.setProperty("javax.net.ssl.keyStoreType", "JKS");
		System.setProperty("javax.net.ssl.trustStoreType", "JKS");

/**
 * Identify the IP address for the server in case PC has a multiple-host setup
 */
		String ipAdr = new HDGetIPAddress().getNetworkIP();
/**
 * Set correct IP address for first server in list
 */
		HGlobal.userServers.get(0)[1] = ipAdr;
		System.setProperty("h2.bindAddress",ipAdr);
	}

/**
 * method responsible to create the tcp server
 * @throws HDException
 */
    public String startTCPserver() throws HDException {

     //catches any server related errors, if the connection is broken etc.
        try {

      //Server uses the IP and port defined earlier, allows other computers to connect
        	String[] params = new String[] { "-tcpPort" , SERVER_PORT , "-tcpAllowOthers" , "-tcpSSL" };

        	server = Server.createTcpServer(params).start();
        	//serverStatus = server.getStatus();
        	serverStatus = server.getURL();

        	if (HGlobal.DEBUG) {
        		System.out.println(" " + serverStatus); //prints out the server's status
            	JOptionPane.showMessageDialog(null," " + serverStatus);  //Server's status on the JOptionPane
        	}
            return serverStatus;
        } catch(Exception ex) {
        	if (HGlobal.DEBUG) {
        		System.out.println("HDServerH2tcp - Error creating TCP server: " + ex.getMessage());
        		ex.printStackTrace();
        	}
	        throw new HDException(" HDServerH2tcp SQL error: \n " + ex.getMessage());
        }
    }

/**
 * Stop Server
 * @throws
 */
    public void stopTCPserver() {
    	if (server != null) {
    		server.stop();
    		server.shutdown();
    		server = null;
    		System.out.println(" H2 TCP server STOPPED!");
    	} else System.out.println(" Server not initiated!");
    }

/**
 * String getConnectStatus()
 * @return connect status
 */
    public String getConnectStatus() {
    	return connMessage;
    }

/**
 * Remote H2 connection
 * @param urlH2loc
 * @return
 * @throws HDException
 */
    public Connection remoteConnect(String urlH2loc, String[] logonData) throws HDException {

	    Connection connt = null;  //connection variable

	// Set up login data
        String ipA_Port = logonData[0];
        String userID = logonData[1];
        String passWord = logonData[2];

/** Metadata variable which include methods such as the following:
 * 1) Database Product Name
 * 2) Database Product Version
 * 3) URL where the database files are located (in TCP mode)
 */
        DatabaseMetaData dbmd;

        try {
        //Driver's name
            Class.forName("org.h2.Driver");

/** The String URL is pertained of the following:
 *  1) jdbc which java implements so that it can take advantage of the SQL features
 *  2) Which Database Engine will be used
 *  3) URL where the files will be stored (as this is a TCP connection)
 *  4) Schema: businessApp
 *  5) Auto server is true means that other computers can connect with the same database at any time
 *  6) DB_CLOSE_DELAY=1 for update of database before close
 *  7) Port number of the server is also defined
 */

       // SSL setup,  need to have a valid SSL CERT

            url = "jdbc:h2:ssl://"  + ipA_Port + "/" + urlH2loc
            		//+ ";IFEXISTS=TRUE;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=1";
            		+ ";IFEXISTS=TRUE;AUTO_SERVER=TRUE";

	        if (HGlobal.DEBUG)
	        	System.out.println(" H2 connect to URL: " + url); //prints out the url the database files are located as well as the h2 features used (SSL)
	        connt = DriverManager.getConnection(url, userID, passWord); //Driver Manager defines the username & password of the database

	  //set AutoCommit to false to control commit actions manually
	        connt.setAutoCommit(false);

	  //outputs H2 version and the URL of the database files which H2 is reading from, for confirmation
	        dbmd = connt.getMetaData(); //get MetaData to confirm connection

	        outputConn = "Remote connect to "+ dbmd.getDatabaseProductName() + " " +
	                   dbmd.getDatabaseProductVersion() + " Catalog: " + connt.getCatalog()
	                   + "\n URL: " + dbmd.getURL();

	  //outputs the message on the system
	        //if (HGlobal.DEBUG)
	        	System.out.println(" " + outputConn);

	  //outputs the message to Main
	        connMessage = " Remote connected to: " + connt.getCatalog();
	        if (HGlobal.DEBUG) JOptionPane.showMessageDialog(null, connMessage);

	 //In case there is an error for creating the class for the Driver to be used
	    } catch (ClassNotFoundException clnf){
	    	if (HGlobal.DEBUG) System.out.println(" HDSeverH2tcp - Driver class error: " + clnf.getMessage());
	    	if (HGlobal.DEBUG) clnf.printStackTrace();
	        throw new HDException("HDSeverH2tcp - Driver class error: " + clnf.getMessage());
	 //Any error associated with the Database Engine
	    } catch(SQLException sqle){
	    	if (HGlobal.DEBUG) System.out.println(" HDSeverH2tcp SQL error: \n " + sqle.getMessage());
	        JOptionPane.showMessageDialog(null," Server error!\n Connect to server failed: \n "
	        		+ sqle.getMessage());
	        if (HGlobal.DEBUG) sqle.printStackTrace();
	        throw new HDException(" Server error!\n Connect to server failed: \n " + sqle.getMessage());
	    }
	    return connt;
	}
} // End class HDServerH2tcp

