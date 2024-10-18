package hre.dbla;
/*************************************************************************************
 * Class HDGetIPAddress find the network IP address of the PC
 * Returns the true IP address of the running PC, by matching
 * the first 3 IP address sub-groups against its Gateway address.
 * ***********************************************************************************
 * v0.01.0027 2021-10-30 First version (Don Ferguson)
 * 			  2021-10-31 Modified for use with HDServerH2tcp (N. Tolleshaug)
 * 			  2021-11-06 Fixed code to also work in Mac/Linux (D Ferguson)
 * v0.03.0031 2024-10-13 Replace deprecated Runtime.exec stmt with ProcessBuilder (D Ferguson)
 ************************************************************************************/
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.TreeSet;

import hre.gui.HGlobal;
/**
 * HDGetIPAddress find the network IP address of the PC
 * @author Don Ferguson
 * @version v0.03.0031
 * @since 2021-10-30
 */
public class HDGetIPAddress {

/**
 * String getNetworkIP()
 * @return PC network IP
 */
    public String getNetworkIP() {
        String yourIp = getYourIp(getDefaultGateWayAddress());
        if (HGlobal.DEBUG )
        	System.out.println("IPAdress: " + yourIp);
		return yourIp;
    }

/**
 * String getYourIp(String defaultAddress)
 * @param defaultAddress
 * @return ip address which you need
 */
    private String getYourIp(String defaultAddress) {
    	// Find the position of the last . in the IP address
    	int posn = defaultAddress.lastIndexOf(".");
    	// and take that many chars as the first 3 groups of the address
        String temp = defaultAddress.substring(0, posn);
        String ipToForward = "";
        TreeSet<String> ipAddrs = getIpAddressList();
        for (String tempIp : ipAddrs) {
            if (tempIp.contains(temp)) {
                ipToForward = tempIp;
                break;
            }
        }
        return ipToForward;
    }	// End getYourIp

/**
 * TreeSet<String> getIpAddressList()
 * @return the ipaddress list
 */
    private TreeSet<String> getIpAddressList() {
        TreeSet<String> ipAddrs = new TreeSet<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
            // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    ipAddrs.add(addr.getHostAddress());
                }  // 2nd while
            }  // 1st while
        } catch (SocketException soe) {
            System.out.println("ERROR HDGetIPAddress - getIpAddressList() Socket exception: \n"
            		+ soe.getMessage());
            soe.printStackTrace();
        }
        return ipAddrs;
    }	// End getIpAddressList


/**
 * String getDefaultGateWayAddress()
 * @return default gateway address in java
 */
    private String getDefaultGateWayAddress() {
    	// Initialise 'gateway' so as to avoid an Exception in method getYourIP
        String gateway = "...";
        try {
       	 	ProcessBuilder pb = new ProcessBuilder("netstat -rn".split(" "));
       	 	Process p;
        	p = pb.start();
            BufferedReader output = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = output.readLine();
            // The Default Gateway IP Address is found in
            // (iMac) the 2nd field of the line that starts with 'default'
            // (Linux) the 2nd field of the line that starts with '0.0.0.0'
            // (Windows) the 3rd field of the line that starts with '0.0.0.0'
            while (line != null) {
                if (line.trim().startsWith("0.0.0.0") || line.trim().startsWith("default"))
                    break;
                line = output.readLine();
            }
            if (line == null) // gateway not found - return
                return gateway;
             StringTokenizer st = new StringTokenizer(line);
             st.nextToken();	// 1st token
             if (HGlobal.osType.contains("win")) {
            	 st.nextToken();	// 2nd token
             	 gateway = st.nextToken();	// 3rd token used if Windows
             	}
             else gateway = st.nextToken();	// 2nd token used if not Windows

        } catch (IOException ioe) {
            System.out.println("ERROR HDGetIPAddress - getDefaultGateWayAddress() IO exception: \n"
            		+ ioe.getMessage());
            ioe.printStackTrace();
        }
        return gateway;
    }	// End getDefaultGatewayAddress
}