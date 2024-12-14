package hre.tmgjava;
import java.io.File;
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

import hre.dbla.HDException;

/**
 * class HREserverH2tcp
 * @author NTo
 *
 */
public class HREserverH2tcp {

    Server server; //the server's instance variable
	private static final String SERVER_PORT = "9092"; //fixed port the server is listening to

/**
 * Constructor HREserverH2tcp()
 */
	public HREserverH2tcp() {
/**
 * Set up path to keyStore and trustStore which basically contains a self signed ssl certificate
 * Your keystore contains 1 entry
 * hre-sslcert, 8. sep. 2021, PrivateKeyEntry,
 * Certificate fingerprint (SHA-256): ED:52:84:F7:22:E4:D1:74:63:EC:F7:23:8A:20:57:DC:99:0C:30:59:B8:A7:CB:C9:90:4D:56:D6:D5:90:6C:BD
 */
		String pathUserHRE = System.getProperty("user.home") + File.separator + "HRE" + File.separator;
		System.setProperty("javax.net.ssl.keyStore", pathUserHRE + "KeyStore" + File.separator + "serverKeystore.jks");
		System.setProperty("javax.net.ssl.keyStorePassword", "servStore");
		System.setProperty("javax.net.ssl.keyPassword", "servStore");
		System.setProperty("javax.net.ssl.trustStore", pathUserHRE + "KeyStore" + File.separator + "serverKeystore.jks");
		System.setProperty("javax.net.ssl.trustStorePassword","servStore");
		System.setProperty("javax.net.ssl.keyStoreType", "JKS");
		System.setProperty("javax.net.ssl.trustStoreType", "JKS");
	}

/**
 * void tcpServer()
 * method responsible to create the tcp server
 * @throws HCException
 */
    public void tcpServer() throws HCException {
    	//catches any server related errors, if the connection is broken etc.
        try {
 /**
  * server uses the IP and port defined earlier,
  * allows other computers in the LAN to connect and
  * implements the secure socket layer (SSL) feature
  */
        	String[] paraString = new String[] {"-tcpPort" , SERVER_PORT , "-tcpAllowOthers" , "-tcpSSL" };

        	server = Server.createTcpServer(paraString).start();
            System.out.println(" " + server.getStatus()); //prints out the server's status

            JOptionPane.showMessageDialog(null,server.getStatus()); //prints out the server's status on the option pane as well

        } catch(Exception ex){
            System.out.println(" HREtcpH2Server - Error with Server: \n " + ex.getMessage());
            if (TMGglobal.DEBUG) ex.printStackTrace();
            throw new HCException(" HDSeverH2tcp\nSQL error: " + ex.getMessage());
        }
    }

/**
 *  Stop Server
 */
    public void stopTCPserver() {
    	server.stop();
    	System.out.println(" HREtcpH2Server stopped!");
    }

/**
 * Remote H2 connection
 * @param urlH2loc
 * @return
 * @throws HDException
 */
    public Connection remoteConnect(String urlH2loc, String iPA_port) throws HDException {

	    Connection connt = null;        //connection variable
	// String which will hold important messages
        String outputConn = null;

/** Metadata variable which include methods such as the following:
 * 1) Database Product Name
 * 2) Database Product Version
 * 3) URL where the database files are located (in TCP mode)
 */
        DatabaseMetaData dbmd;

        try {
        //Driver's name
            Class.forName("org.h2.Driver");

      // SSL does work,  CERT stores implemented

/** The String URL is pertained of the following:
 *  1) jdbc which java implements so that it can take advantage of the SQL features
 *  2) Which Database Engine will be used
 *  3) URL where the files will be stored (as this is a TCP connection)
 *  4) Schema: businessApp
 *  5) Auto server is true means that other computers can connect with the same database at any time
 *  6) DB_CLOSE_DELAY=1 for update of database before close
 *  7) Port number of the server is also defined
 */
            String url = "jdbc:h2:ssl://"  + iPA_port + "/" + urlH2loc
            		+ ";IFEXISTS=TRUE;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=1";

	        if (TMGglobal.DEBUG) System.out.println(" H2 connect to URL: " + url); //prints out the url the database files are located as well as the h2 features used (SSL)
	        connt = DriverManager.getConnection(url, "sa", ""); //Driver Manager defines the username & password of the database

	    //set AutoCommit to false to control commit actions manually
	        connt.setAutoCommit(false);

	    //outputs H2 version and the URL of the database files which H2 is reading from, for confirmation
	        dbmd = connt.getMetaData(); //get MetaData to confirm connection

	        outputConn = "Connected to "+ dbmd.getDatabaseProductName() + " " +
	                   dbmd.getDatabaseProductVersion() + "  Catalog: " + connt.getCatalog()
	                   + "\n URL: " + dbmd.getURL();

	     //outputs the message on the system (NetBeans compiler)
	        System.out.println(" " + outputConn);

	     //In case there is an error for creating the class for the Driver to be used
	    } catch (ClassNotFoundException clnf){
	        System.out.println(" Error creating class: " + clnf.getMessage());
	        clnf.printStackTrace();
	        throw new HDException("HDSeverH2tcp - Error creating class: " + clnf.getMessage());
	      //Any error associated with the Database Engine
	    } catch(SQLException sqle){
	        System.out.println(" HDSeverH2tcp\n SQL error: " + sqle.getMessage());
	        JOptionPane.showMessageDialog(null," HDSeverH2tcp\n SQL error: " + sqle.getMessage());
	        sqle.printStackTrace();
	        throw new HDException("HDSeverH2tcp\nSQL error: " + sqle.getMessage());
	    }
	    return connt;
	}
}