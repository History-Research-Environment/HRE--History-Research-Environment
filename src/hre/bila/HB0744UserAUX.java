package hre.bila;
/*************************************************************************************************************
 * UserAUX file - Specification 07.44 BR_UserAUX of 2019-04-27
 * v0.00.0007 2019-05-17 by D Ferguson
 * v0.00.0008 2019-07-19 fix bug in inconsistent use of Read/Open (D Ferguson)
 * v0.00.0009 2019-07-26 fix error on writing log before HRE folder exists (D Ferguson)
 * v0.00.0010 2019-08-25 add code to save/restore mainMenu bounds (D. Ferguson)
 * 					 add GUI font as an AppSetting variable, default Arial
 * v0.00.0011 2019-09-01 ignore NullPointerExceptions on Read to allow adding items to file (D. Ferguson)
 * v0.00.0013 2019-11-08 add saving of nativeLanguage variable
 * v0.00.0014 2019-11-18 setup server/project ArrayLists properly (D Ferguson)
 * 						 and changes for HB0711/0744 static classes (D Ferguson)
 * v0.00.0017 2020-01-07 Removed Seed data, change Sample; added Build# to AUX file (D. Ferguson)
 *            2020-01-14 add 'open last used project at startup' setting
 *            2020-01-18 add node validation tests to readUserAUXfile method
 *            2020-01-20 add logged in status to userServer fields
 * v0.00.0017 2020-01-23 Added DEBUG message and TRACE option in Exception handling (N. Tolleshaug)
 *                       More info to programmer if faults in AUX handling
 * v0.00.0018 2020-01-30 Corrected reading of native language from AUX
 * v0.00.0019 2020-02-26 add LastBackup field to userProjects table (D. Ferguson)
 *            2020-03-01 add lastProjectFolder to saved fields in UserAUX file
 * v0.00.0020 2020-03-19 change userName to userID, save userName, userEmail, lastProjectClosed (D. Ferguson)
 * v0.00.0022 2020-06-28 standardise file separators in filepaths (D Ferguson)
 *            2020-07-05 add save/restore of date/time format options (D Ferguson)
 *            2020-08-02 remove showVPproject setting; write out OS info (D Ferguson)
 *            2020-09-20 remove font, table color settings; save/read JTattoo LAF settings instead (D Ferguson)
 * v0.01.0025 2020-11-09 add storing of HRE file locations values (D Ferguson)
 * 			  2021-01-06 save DB version; add storing of 'Backup Active Proj on close' setting (D Ferguson)
 * v0.01.0026 2021-05-16 remove creation of Sample file entry in UserAUX (D Ferguson)
 * 			  2021-06-01 add saving of Server mode setting (D Ferguson)
 * 			  2021-06-20 implement read/write of Accent settings (D Ferguson)
 * 			  2021-08-07 implement read/write of Font setting (D Ferguson)
 * 			  2021-08-21 add save/restore of pluginEnabled setting (D Ferguson)
 * v0.01.0027 2021-11-01 ensure corect TCPIP address stored for this PC (D Ferguson)
 * 			  2021-11-15 add HGlobal.TIME setting in Preferences (D Ferguson)
 * 			  2022-03-21 add support of border cell colors (D Ferguson)
 * v0.03.0031 2023-12-17 add Lifespan setting (D Ferguson)
 *			  2024-10-12 add save/restore of 'PS reload' User option (D Ferguson)
 ************************************************************************************************************
 * Structure of HB0744UserAUX consists of 3 methods, namely:
 * 	(i)  initUserAUXfile: if the /username/HRE/ folder doesn't exist, this
 * 		 creates it, then sets initial variables in HGlobal and writes out
 *		 the userAUX file (or reads it if it already exists)
 *  (ii) writeUserAUXfile: writes out the file from existing HGlobal settings
 * (iii) readUserAUXfile: reads the file and sets HGlobal variables
 *******************************************************************************/

import java.awt.Color;
import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import hre.dbla.HDGetIPAddress;
import hre.gui.HGlobal;
import hre.gui.HGlobalCode;

/**
 * UserAUX file handling
 * @author D Ferguson
 * @version v0.03.0031
 * @since 2019-05-17
 */

public class HB0744UserAUX {

	private static String userDir;				// for path of User home directory + HRE
	private static String auxName;				// for path + filename of this UserAUX file
	private static String ipAddress;			// for TCPIP address of this PC

/**
 * START of initUserAUXfile	- create file if not present, otherwise read it
 **/
	public static void initUserAUXfile() {
		// First, get the User's home directory
		userDir = System.getProperty("user.home");
		userDir = userDir + File.separator +"HRE" +File.separator; 	// Add "HRE" to make folder name
		HGlobal.pathProjectFolder = userDir;			// ensure preferred folder not null
		auxName = userDir + HGlobal.userID + ".hreu"; 	// build UserAUX path+filename

		// Initialise Server data for this PC, this IP addr, port=9092
		ipAddress = new HDGetIPAddress().getNetworkIP();
		String[] server = {HGlobal.thisComputer, ipAddress, "9092"};

		// Now check if the HRE directory exists in the User's home directory
		File folder = new File(userDir);
		if (!folder.exists()) { folder.mkdir(); }		// if the folder does not exist, then create it

		// Now check if UserAUX file exists (as might not be present even if folder existed)
		File auxFile = new File(auxName);

		// If no UserAUX file exists, enter initial data into HGlobal arrays
		if (!auxFile.exists())
			{
				// Initialize LAF details to the HRE default settings
				HGlobal.lafNumber = 5;
				HGlobal.lafThemeNumber = 10;
				HGlobal.lafTheme = "Blue-Medium-Font";

				// Initialise Server data for this PC
    			HGlobal.userServers.add(server);

	    		// and write out a new UserAUX file
	    		writeUserAUXfile();
			}
			// But if it exists, read all of it and populate HGlobal
			else {
				readUserAUXfile("ALL");
				// Reset 1st AUX Server entry to ensure we trap current PC status
				HGlobal.userServers.set(0, server);
			}
	}	// End initUserAUXfile


/**
 * START of writeUserAUXfile - write AUX file based on HGlobal settings
 **/
	public static void writeUserAUXfile() {
    	 try {
	         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	         Document doc = dBuilder.newDocument();

	         // UserAUX XML file build
	         Element useraux = doc.createElement("userauxfile");
	         doc.appendChild(useraux);
	         // create OS element
	         Element os = doc.createElement("os");
	         os.appendChild(doc.createTextNode(HGlobal.osType));			//set this PC's OS type (lower cased)
	         useraux.appendChild(os);
	         // create Build No. element
	         Element buildno = doc.createElement("buildno");
	         buildno.appendChild(doc.createTextNode(HGlobal.buildNo));		//set HRE Build number
	         useraux.appendChild(buildno);
	         // create DB Version element
	         Element dbversion = doc.createElement("database");
	         dbversion.appendChild(doc.createTextNode(HGlobal.databaseVersion));		//set Database Build DDL Version
	         useraux.appendChild(dbversion);
	         // create Server mode element
	         Element servermode = doc.createElement("servermode");
	         if (HGlobal.tcpServerOn) servermode.appendChild(doc.createTextNode("true"));	//set servermode true/false
				else servermode.appendChild(doc.createTextNode("false"));
	         useraux.appendChild(servermode);
	             // create User section
		         Element user = doc.createElement("user");
		         useraux.appendChild(user);
		           	// create UserID element
		         	Element userID = doc.createElement("userid");
		         	userID.appendChild(doc.createTextNode(HGlobal.userID));		//set User to this userID from the OS
		         	user.appendChild(userID);
		           	// create UserName element
		         	Element userName = doc.createElement("username");
		         	userName.appendChild(doc.createTextNode(HGlobal.userCred[1]));		//set userName as gathered by Welcome screen
		         	user.appendChild(userName);
		           	// create UserEmail element
		         	Element userEmail = doc.createElement("useremail");
		         	userEmail.appendChild(doc.createTextNode(HGlobal.userCred[3]));		//set user's email address as gathered by Welcome screen
		         	user.appendChild(userEmail);

		         	// create preferences elements
		         	Element preferences = doc.createElement("preferences");
		         	user.appendChild(preferences);
		         		// create GUI Language element
	         			Element guiLang = doc.createElement("guilang");
	         			guiLang.appendChild(doc.createTextNode(HGlobal.nativeLanguage));
	         			preferences.appendChild(guiLang);
		         		// show welcome screen at startup setting
	         			Element showwelcome = doc.createElement("showwelcome");
	         			if (HGlobal.showWelcome) {showwelcome.appendChild(doc.createTextNode("true"));}
	         				else {showwelcome.appendChild(doc.createTextNode("false"));}
	         			preferences.appendChild(showwelcome);
	         			// show cancel msgs setting
		         		Element showcancelmsg = doc.createElement("showcancelmsg");
		         		if (HGlobal.showCancelmsg) {showcancelmsg.appendChild(doc.createTextNode("true"));}
		         			else {showcancelmsg.appendChild(doc.createTextNode("false"));}
		         		preferences.appendChild(showcancelmsg);
		         		// open last used project setting
		         		Element openlastproject = doc.createElement("openlastproject");
		         		if (HGlobal.openLastProject) {openlastproject.appendChild(doc.createTextNode("true"));}
		         			else {openlastproject.appendChild(doc.createTextNode("false"));}
		         		preferences.appendChild(openlastproject);
		         		// backup active project setting
		         		Element bkupactivproject = doc.createElement("bkupactivproject");
		         		if (HGlobal.backupActivProject) {bkupactivproject.appendChild(doc.createTextNode("true"));}
		         			else {bkupactivproject.appendChild(doc.createTextNode("false"));}
		         		preferences.appendChild(bkupactivproject);
		         		// enable plugins setting
		         		Element pluginEnabled = doc.createElement("pluginEnabled");
		         		if (HGlobal.pluginEnabled) {pluginEnabled.appendChild(doc.createTextNode("true"));}
		         			else {pluginEnabled.appendChild(doc.createTextNode("false"));}
		         		preferences.appendChild(pluginEnabled);
		         		// enable reload PS setting
		         		Element reloadPS = doc.createElement("reloadPS");
		         		if (HGlobal.reloadPS) {reloadPS.appendChild(doc.createTextNode("true"));}
		         			else {reloadPS.appendChild(doc.createTextNode("false"));}
		         		preferences.appendChild(reloadPS);
		         		// write logs setting
		         		Element writelogs = doc.createElement("writelogs");
		         		if (HGlobal.writeLogs) {writelogs.appendChild(doc.createTextNode("true"));}
		         			else {writelogs.appendChild(doc.createTextNode("false"));}
		         		preferences.appendChild(writelogs);
		         		// timing msgs setting
		         		Element timing = doc.createElement("timing");
		         		if (HGlobal.TIME) {timing.appendChild(doc.createTextNode("true"));}
		         			else {timing.appendChild(doc.createTextNode("false"));}
		         		preferences.appendChild(timing);

		           	// create visibility elements
		         	Element visible = doc.createElement("visible");
		         	user.appendChild(visible);
		         		// create LAF element
		         		Element lafnumber = doc.createElement("lafnumber");
		         		lafnumber.appendChild(doc.createTextNode(Integer.toString(HGlobal.lafNumber)));
	         			visible.appendChild(lafnumber);
	         			// create LAF theme number
		         		Element lafthemenumber = doc.createElement("lafthemenumber");
		         		lafthemenumber.appendChild(doc.createTextNode(Integer.toString(HGlobal.lafThemeNumber)));
	         			visible.appendChild(lafthemenumber);
	         			// create LAF theme name
		         		Element laftheme = doc.createElement("laftheme");
		         		laftheme.appendChild(doc.createTextNode(HGlobal.lafTheme));
	         			visible.appendChild(laftheme);
	         			// create Font name
		         		Element fontname = doc.createElement("fontname");
		         		fontname.appendChild(doc.createTextNode(HGlobal.fontName));
	         			visible.appendChild(fontname);
	         			// create date format element
		         		Element dateformat = doc.createElement("dateformat");
		         		dateformat.appendChild(doc.createTextNode(HGlobal.dateFormat));
	         			visible.appendChild(dateformat);
		         		// create time format element
		         		Element timeformat = doc.createElement("timeformat");
		         		timeformat.appendChild(doc.createTextNode(HGlobal.timeFormat));
	         			visible.appendChild(timeformat);
	         			// create lifespan element
		         		Element lifespan = doc.createElement("lifespan");
		         		lifespan.appendChild(doc.createTextNode(HGlobal.lifespan));
	         			visible.appendChild(lifespan);

		         	// create file location elements
		         	Element files = doc.createElement("files");
		         	user.appendChild(files);
		         		// create default HRE file location element
	         			Element locHRE = doc.createElement("hrefiles");
	         			locHRE.appendChild(doc.createTextNode(HGlobal.pathHRElocation));
	         			files.appendChild(locHRE);
	         			// create default HRE report files location element
	         			Element repHRE = doc.createElement("hrereports");
	         			repHRE.appendChild(doc.createTextNode(HGlobal.pathHREreports));
	         			files.appendChild(repHRE);
	         			// create default HRE backup files location element
	         			Element bakHRE = doc.createElement("hrebackups");
	         			bakHRE.appendChild(doc.createTextNode(HGlobal.pathHREbackups));
	         			files.appendChild(bakHRE);
	         			// create default HRE external backup files location element
	         			Element extHRE = doc.createElement("hreextback");
	         			extHRE.appendChild(doc.createTextNode(HGlobal.pathExtbackups));
	         			files.appendChild(extHRE);

		         	// create miscellaneous elements
		         	Element misc = doc.createElement("misc");
		         	user.appendChild(misc);
		         		// create last used folder element
	         			Element lastused = doc.createElement("lastusedfolder");
	         			lastused.appendChild(doc.createTextNode(HGlobal.pathProjectFolder));
	         			misc.appendChild(lastused);
		         		// create last Project Closed element
	         			Element lastProjClosed = doc.createElement("lastprojectclosed");
	         			lastProjClosed.appendChild(doc.createTextNode(HGlobal.lastProjectClosed));
	         			misc.appendChild(lastProjClosed);

		         	// create screen location setting elements
		         	Element locations = doc.createElement("locations");
		         	user.appendChild(locations);
		         		// create main menu position, size elements (x, y, height, width)
		         		Element mainmenux = doc.createElement("mainmenux");
		         		mainmenux.appendChild(doc.createTextNode(Integer.toString(HGlobal.mainX)));
		         		locations.appendChild(mainmenux);
		         		Element mainmenuy = doc.createElement("mainmenuy");
		         		mainmenuy.appendChild(doc.createTextNode(Integer.toString(HGlobal.mainY)));
		         		locations.appendChild(mainmenuy);
		         		Element mainmenuh = doc.createElement("mainmenuh");
		         		mainmenuh.appendChild(doc.createTextNode(Integer.toString(HGlobal.mainH)));
		         		locations.appendChild(mainmenuh);
		         		Element mainmenuw = doc.createElement("mainmenuw");
		         		mainmenuw.appendChild(doc.createTextNode(Integer.toString(HGlobal.mainW)));
		         		locations.appendChild(mainmenuw);

			        // create accent elements - mode first, then all accent settings
			        Element accents = doc.createElement("accents");
			        user.appendChild(accents);
			        	Element accentmode = doc.createElement("accentmode");
			        	if (HGlobal.accentOn) accentmode.appendChild(doc.createTextNode("on"));	//set accentmode on/off
							else accentmode.appendChild(doc.createTextNode("off"));
			        	accents.appendChild(accentmode);
		         		// add the contents of HGlobal.accentNames into accents section, plus fill, font, border colors
		         		int a = HGlobal.accentNames.size();				// a = number of Accent entries
		         		for (int i = 0; i < a; i++)
		         		{Element accent = doc.createElement("accent");
		         			accents.appendChild(accent);
		         			Attr acctattr = doc.createAttribute("name");
		         			String accentOut = HGlobal.accentNames.get(i);		//get an accent string from accentNames
		         			acctattr.setValue(accentOut);
		         			accent.setAttributeNode(acctattr);
		         				// add accent rule
		         				Element accentrule = doc.createElement("rule");
		         				accentrule.appendChild(doc.createTextNode(HGlobal.accentRules.get(i)));
		         				accent.appendChild(accentrule);
		         				// add accent fill color
		         				Element fillcolor = doc.createElement("fillcolor");
		         				String rgbfill = Integer.toHexString((HGlobal.accentFillColors.get(i)).getRGB());	// converts to 8-char hex
				         		fillcolor.appendChild(doc.createTextNode(rgbfill.substring(2, rgbfill.length())));	// just take last 6 characters
		         				accent.appendChild(fillcolor);
		         				// add font color
		         				Element fontcolor = doc.createElement("fontcolor");
		         				String rgbfont = Integer.toHexString((HGlobal.accentFontColors.get(i)).getRGB());	// converts to 8-char hex
				         		fontcolor.appendChild(doc.createTextNode(rgbfont.substring(2, rgbfont.length())));	// just take last 6 characters
		         				accent.appendChild(fontcolor);
		         				// add border color
		         				Element bordercolor = doc.createElement("bordercolor");
		         				String rgbbrdr = Integer.toHexString((HGlobal.accentBorderColors.get(i)).getRGB());	 // converts to 8-char hex
				         		bordercolor.appendChild(doc.createTextNode(rgbbrdr.substring(2, rgbbrdr.length()))); // just take last 6 characters
		         				accent.appendChild(bordercolor);
		         		}

		         	// create Servers list section
		         	Element servers = doc.createElement("servers");
		         	user.appendChild(servers);
		         		// add the contents of HGlobal.userServers as server section
		         		int s = HGlobal.userServers.size();				// s = number of server entries
		         		for (int i = 0; i < s; i++)
		         		{	Element server = doc.createElement("server");
			         		servers.appendChild(server);
			         		Attr servattr = doc.createAttribute("name");
		         			String servOut[] = HGlobal.userServers.get(i);		//get a server string from userServers
			                servattr.setValue(servOut[0]);
			                server.setAttributeNode(servattr);
			                	// add TCP/IP address
			                	Element ipaddr = doc.createElement("ipaddr");
			                	ipaddr.appendChild(doc.createTextNode(servOut[1]));
			                	server.appendChild(ipaddr);
			                	// add Port number
			                	Element portnum = doc.createElement("port");
			                	portnum.appendChild(doc.createTextNode(servOut[2]));
			                	server.appendChild(portnum);
		         		}

		         // create Project list section
		         Element projects = doc.createElement("projects");
		         useraux.appendChild(projects);
		         		// add the contents of HGlobal.userProjects into projects section
		         		int p = HGlobal.userProjects.size();				// p = number of project entries
		         		for (int i = 0; i < p; i++)
		         		{	Element project = doc.createElement("project");
		         			projects.appendChild(project);
		         			Attr projattr = doc.createAttribute("name");
		         			String projOut[] = HGlobal.userProjects.get(i);		//get a project string from userProjects
		         			projattr.setValue(projOut[0]);
		         			project.setAttributeNode(projattr);
		         				// add project filename
		         				Element filename = doc.createElement("filename");
		         				filename.appendChild(doc.createTextNode(projOut[1]));
		         				project.appendChild(filename);
		         				// add project foldername
		         				Element foldername = doc.createElement("foldername");
		         				foldername.appendChild(doc.createTextNode(projOut[2]));
		         				project.appendChild(foldername);
		         				// add project servername
		         				Element servername = doc.createElement("servername");
		         				servername.appendChild(doc.createTextNode(projOut[3]));
		         				project.appendChild(servername);
		         				// add project last closed date
		         				Element lastclosed = doc.createElement("lastclosed");
		         				lastclosed.appendChild(doc.createTextNode(projOut[4]));
		         				project.appendChild(lastclosed);
		         				// add database type
		         				Element dbtype = doc.createElement("dbtype");
		         				dbtype.appendChild(doc.createTextNode(projOut[5]));
		         				project.appendChild(dbtype);
		         				// add last backup
		         				Element lastback = doc.createElement("lastbackup");
		         				lastback.appendChild(doc.createTextNode(projOut[6]));
		         				project.appendChild(lastback);
		         		}

	         // Write out the content into the UserAUX file in XML format, indented, spaced 3
	         TransformerFactory transformerFactory = TransformerFactory.newInstance();
	         Transformer transformer = transformerFactory.newTransformer();
	         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	         transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
	         DOMSource source = new DOMSource(doc);
	         StreamResult result = new StreamResult(new File(auxName));
	         transformer.transform(source, result);
	         if (HGlobal.writeLogs)
	        	 HB0711Logging.logWrite("Action: writing UserAux file Build " + HGlobal.buildNo);

         } catch (Exception ex) {
        	 if (HGlobal.writeLogs)
        			HB0711Logging.logWrite("Error: User Auxiliary file Build " + HGlobal.buildNo + " write error in HB0744 "+ex.getMessage());
			 if (HGlobal.DEBUG)
				 System.out.println("ERROR: AUX file write: \n" + ex.toString());
        	 HGlobalCode.loguserAuxMessage(1,ex.getMessage());
			 ex.printStackTrace();
         }
	         if (HGlobal.writeLogs)
	        	 HB0711Logging.logWrite("Action: exiting HB0744 UserAux");
	}	// End writeUserAUXfile


/**
 * START of readUserAUXfile	- read AUX file and set HGlobal
 * 	@param callType - if ALL, process whole file
 *                    if OPT then exclude the User, Misc, Location, Server, Project entries
 **/
	public static void readUserAUXfile(String callType) {
		try {
	        File inputFile = new File(auxName);
	        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	        Document doc = dBuilder.parse(inputFile);
	        doc.getDocumentElement().normalize();

	        // Check this is possibly a UserAUX file
	        if (doc.getElementsByTagName("userauxfile").getLength() == 0 || doc.getElementsByTagName("userid").getLength() == 0) {
	         	HGlobalCode.loguserAuxMessage(2,"");
	         	inputFile.delete();				// delete the invalid file
	         	if (HGlobal.writeLogs)
		         		HB0711Logging.logWrite("Action: deleted invalid UserAUX file in HB0744; exited HRE");
	         	System.exit(0);					// kill HRE
	         }

	        // Check this userAUX file matches this User
	        if (doc.getElementsByTagName("userid").item(0).getTextContent().equals(HGlobal.userID)) {   }
	         	else {
	         		HGlobalCode.loguserAuxMessage(3,"");
	         		inputFile.delete();				// delete the invalid file
		         	if (HGlobal.writeLogs)
		         		HB0711Logging.logWrite("Action: deleted invalid UserID in UserAUX file in HB0744; exited HRE");
		         	System.exit(0);					// kill HRE
	         	}

	        // Get the servermode setting, if present and set HGlobal
	        if (doc.getElementsByTagName("servermode").getLength() > 0) {
		        if (doc.getElementsByTagName("servermode").item(0).getTextContent().equals("true")) {
		        		HGlobal.tcpServerOn=true;
		        	}
		        	else HGlobal.tcpServerOn=false;
	        }

	        // now get the Preference values and store them - if value not present, leave HGlobal as is
	        if (doc.getElementsByTagName("guilang").getLength() > 0) {
	        	HGlobal.nativeLanguage = doc.getElementsByTagName("guilang").item(0).getTextContent();
	        }
	        if (doc.getElementsByTagName("showwelcome").getLength() > 0) {
		        if (doc.getElementsByTagName("showwelcome").item(0).getTextContent().equals("true")) {
		        	HGlobal.showWelcome=true;
		        	}
	         		else {HGlobal.showWelcome=false;}
	        }
	        if (doc.getElementsByTagName("showcancelmsg").getLength() > 0) {
		        if (doc.getElementsByTagName("showcancelmsg").item(0).getTextContent().equals("true")) {
		        	HGlobal.showCancelmsg=true;
		        	}
	         		else {HGlobal.showCancelmsg=false;}
	        }
	        if (doc.getElementsByTagName("openlastproject").getLength() > 0) {
		        if (doc.getElementsByTagName("openlastproject").item(0).getTextContent().equals("true")) {
		        	HGlobal.openLastProject=true;
		        	}
	         		else {HGlobal.openLastProject=false;}
	        }
	        if (doc.getElementsByTagName("bkupactivproject").getLength() > 0) {
		        if (doc.getElementsByTagName("bkupactivproject").item(0).getTextContent().equals("true")) {
		        	HGlobal.backupActivProject=true;
		        	}
	         		else {HGlobal.backupActivProject=false;}
	        }
	        if (doc.getElementsByTagName("pluginEnabled").getLength() > 0) {
		        if (doc.getElementsByTagName("pluginEnabled").item(0).getTextContent().equals("true")) {
		        	HGlobal.pluginEnabled=true;
		        	}
	         		else {HGlobal.pluginEnabled=false;}
	        }
	        if (doc.getElementsByTagName("reloadPS").getLength() > 0) {
		        if (doc.getElementsByTagName("reloadPS").item(0).getTextContent().equals("true")) {
		        	HGlobal.reloadPS=true;
		        	}
	         		else {HGlobal.reloadPS=false;}
	        }
	        if (doc.getElementsByTagName("writelogs").getLength() > 0) {
		        if (doc.getElementsByTagName("writelogs").item(0).getTextContent().equals("true")) {
		        	HGlobal.writeLogs=true;
		        	}
	         		else {HGlobal.writeLogs=false;}
	        }
	        if (doc.getElementsByTagName("timing").getLength() > 0) {
		        if (doc.getElementsByTagName("timing").item(0).getTextContent().equals("true")) {
		        	HGlobal.TIME=true;
		        	}
	         		else {HGlobal.TIME=false;}
	        }

	        // Now get the Visibility section values and store them - if value not present, set HGlobal as shown
	        if (doc.getElementsByTagName("lafnumber").getLength() > 0) {
	        	HGlobal.lafNumber = Integer.valueOf(doc.getElementsByTagName("lafnumber").item(0).getTextContent());
	        	}
	        	else HGlobal.lafNumber = 5;			// default to Graphite

	        if (doc.getElementsByTagName("lafthemenumber").getLength() > 0) {
	        	HGlobal.lafThemeNumber = Integer.valueOf(doc.getElementsByTagName("lafthemenumber").item(0).getTextContent());
	        	}
	        	else HGlobal.lafThemeNumber = 10;	// default to Blue-medium

	        if (doc.getElementsByTagName("laftheme").getLength() > 0) {
	        	HGlobal.lafTheme = doc.getElementsByTagName("laftheme").item(0).getTextContent();
	        	}
	        	else HGlobal.lafTheme = "Blue-Medium-Font";		// default blue medium

	        if (doc.getElementsByTagName("fontname").getLength() > 0) {
	        	HGlobal.fontName = doc.getElementsByTagName("fontname").item(0).getTextContent();
	        	}
	        	else HGlobal.fontName = "Dialog";		// default Dialog

	        if (doc.getElementsByTagName("dateformat").getLength() > 0) {
	        	HGlobal.dateFormat = doc.getElementsByTagName("dateformat").item(0).getTextContent();
	        }
	        if (doc.getElementsByTagName("timeformat").getLength() > 0) {
	        	HGlobal.timeFormat = doc.getElementsByTagName("timeformat").item(0).getTextContent();
	        }
	        if (doc.getElementsByTagName("lifespan").getLength() > 0) {
	        	HGlobal.lifespan = doc.getElementsByTagName("lifespan").item(0).getTextContent();
	        }

        // Now get the HRE file location paths and store them - use defaults as shown
	        if (doc.getElementsByTagName("hrefiles").getLength() > 0) {
	        	HGlobal.pathHRElocation = doc.getElementsByTagName("hrefiles").item(0).getTextContent();
	        	}
	        	else HGlobal.pathHRElocation = userDir;		// ensure field not left empty
			if (doc.getElementsByTagName("hrereports").getLength() > 0) {
				HGlobal.pathHREreports = doc.getElementsByTagName("hrereports").item(0).getTextContent();
				}
				else HGlobal.pathHREreports = HGlobal.pathHRElocation;		// ensure field not left empty

			if (doc.getElementsByTagName("hrebackups").getLength() > 0) {
				HGlobal.pathHREbackups = doc.getElementsByTagName("hrebackups").item(0).getTextContent();
				}
				else HGlobal.pathHREbackups = HGlobal.pathHRElocation;		// ensure field not left empty

			if (doc.getElementsByTagName("hreextback").getLength() > 0) {
				HGlobal.pathExtbackups = doc.getElementsByTagName("hreextback").item(0).getTextContent();
				}
				else HGlobal.pathExtbackups = HGlobal.pathHRElocation;		// ensure field not left empty

		    // Now get the Accents section and store them
	        NodeList accentList = doc.getElementsByTagName("accent");
	        if (doc.getElementsByTagName("accentmode").getLength() > 0) {
		        if (doc.getElementsByTagName("accentmode").item(0).getTextContent().equals("on")) HGlobal.accentOn=true;
		        	else HGlobal.accentOn=false;
	        }
	        // Make sure the ArrayLists are empty first as this might be a reload
	        HGlobal.accentNames.clear();
	        HGlobal.accentRules.clear();
	        HGlobal.accentFillColors.clear();
	        HGlobal.accentFontColors.clear();
	        HGlobal.accentBorderColors.clear();
	        //Then load the Accent lists
			for (int i = 0; i < accentList.getLength(); i++) {		// set i=number of accent entries
				Node nNode = accentList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					if (eElement.getAttribute("name").length() > 0) {HGlobal.accentNames.add(i, eElement.getAttribute("name"));}
						else { HGlobal.accentNames.add(i, "unknown");}
					if (eElement.getElementsByTagName("rule").getLength() > 0) {HGlobal.accentRules.add(i, eElement.getElementsByTagName("rule").item(0).getTextContent());}
						else { HGlobal.accentRules.add(i, "unknown");}
					if (eElement.getElementsByTagName("fillcolor").getLength() > 0) {
						HGlobal.accentFillColors.add(i, Color.decode("0X" + doc.getElementsByTagName("fillcolor").item(i).getTextContent()));}
					if (eElement.getElementsByTagName("fontcolor").getLength() > 0) {
						HGlobal.accentFontColors.add(i, Color.decode("0X" + doc.getElementsByTagName("fontcolor").item(i).getTextContent()));}
					if (eElement.getElementsByTagName("bordercolor").getLength() > 0) {
						HGlobal.accentBorderColors.add(i, Color.decode("0X" + doc.getElementsByTagName("bordercolor").item(i).getTextContent()));}
					}
			}
			// If the Border ArrayList is empty, copy the Fill colors into it
			// (this is needed for transitioning to use of borders from a non-border UserAUX file)
			if (HGlobal.accentBorderColors.size() == 0)  {
				for (int i = 0; i < accentList.getLength(); i++) {
					HGlobal.accentBorderColors.add(i, HGlobal.accentFillColors.get(i));
				}
			}

			//***************************************************************************/
	        // Check the type of call - if only for Options, end processing at this point
	        if (callType.equals("OPT")) return;

	        // now get the other User items and save them - if value not present, leave HGlobal as is
	        if (doc.getElementsByTagName("username").getLength() > 0) {
	        	HGlobal.userCred[1] = doc.getElementsByTagName("username").item(0).getTextContent();
	        }
	        if (doc.getElementsByTagName("useremail").getLength() > 0) {
	        	HGlobal.userCred[3] = doc.getElementsByTagName("useremail").item(0).getTextContent();
	        }

	        // Now get the Miscellaneous values and store them, with appropriate defaults
	        if (doc.getElementsByTagName("lastusedfolder").getLength() > 0) {
	        	HGlobal.pathProjectFolder = doc.getElementsByTagName("lastusedfolder").item(0).getTextContent();
	        	}
	        	else HGlobal.pathProjectFolder = userDir;		// ensure field not left as null

	        if (doc.getElementsByTagName("lastprojectclosed").getLength() > 0) {
	        	HGlobal.lastProjectClosed = doc.getElementsByTagName("lastprojectclosed").item(0).getTextContent();
	        	}
	        	else HGlobal.lastProjectClosed = "";		// ensure field not left as null

	        // Now get the Mainmenu location values and store them - if value not present, leave HGlobal as is
	        if (doc.getElementsByTagName("mainmenux").getLength() > 0) {
	        	HGlobal.mainX = Integer.valueOf(doc.getElementsByTagName("mainmenux").item(0).getTextContent());
	        }
	        if (doc.getElementsByTagName("mainmenuy").getLength() > 0) {
	        	HGlobal.mainY = Integer.valueOf(doc.getElementsByTagName("mainmenuy").item(0).getTextContent());
	        }
	        if (doc.getElementsByTagName("mainmenuh").getLength() > 0) {
	        	HGlobal.mainH = Integer.valueOf(doc.getElementsByTagName("mainmenuh").item(0).getTextContent());
	        }
	        if (doc.getElementsByTagName("mainmenuw").getLength() > 0) {
	        	HGlobal.mainW = Integer.valueOf(doc.getElementsByTagName("mainmenuw").item(0).getTextContent());
	        }

	        //now get the Server entries and load them to the HGlobal.userServers ArrayList
	        NodeList servList = doc.getElementsByTagName("server");
				for (int i = 0; i < servList.getLength(); i++) {		// set i = number of server entries
					Node nNode = servList.item(i);
					String [] servIn = new String[3];					// <<-change if no. of elements change; also see HG0418
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						if (eElement.getAttribute("name").length() > 0) {servIn[0] = eElement.getAttribute("name");	}
							else { servIn[0] = "unknown";}
						if (eElement.getElementsByTagName("ipaddr").getLength() > 0) {
							servIn[1] = eElement.getElementsByTagName("ipaddr").item(0).getTextContent(); }
							else { servIn[1] = "unknown";}
						if (eElement.getElementsByTagName("port").getLength() > 0) {
							servIn[2] = eElement.getElementsByTagName("port").item(0).getTextContent(); }
							else { servIn[2] = "0";}
		            }
					HGlobal.userServers.add(servIn);
				}

			//now get the Project entries, load them to the HGlobal.userProjects ArrayList with unknown entries for missing components
		    NodeList projList = doc.getElementsByTagName("project");
				for (int i = 0; i < projList.getLength(); i++) {		// set i= number of project entries
					Node nNode = projList.item(i);
					String [] projIn = new String[7];	// <<-change if no. of elements change; see HG0401,0402,0404,0406,0408,0417,0418
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						if (eElement.getAttribute("name").length() > 0) {projIn[0] = eElement.getAttribute("name");	}
							else { projIn[0] = "unknown";}
						if (eElement.getElementsByTagName("filename").getLength() > 0) {
							projIn[1] = eElement.getElementsByTagName("filename").item(0).getTextContent(); }
							else { projIn[1] = "unknown";}
						if (eElement.getElementsByTagName("foldername").getLength() > 0) {
							projIn[2] = eElement.getElementsByTagName("foldername").item(0).getTextContent(); }
							else { projIn[2] = "unknown";}
						if (eElement.getElementsByTagName("servername").getLength() > 0) {
							projIn[3] = eElement.getElementsByTagName("servername").item(0).getTextContent(); }
							else { projIn[3] = "unknown";}
						if (eElement.getElementsByTagName("lastclosed").getLength() > 0) {
							projIn[4] = eElement.getElementsByTagName("lastclosed").item(0).getTextContent(); }
							else { projIn[4] = "unknown";}
						if (eElement.getElementsByTagName("dbtype").getLength() > 0) {
							projIn[5] = eElement.getElementsByTagName("dbtype").item(0).getTextContent(); }
							else { projIn[5] = "unknown";}
						if (eElement.getElementsByTagName("lastbackup").getLength() > 0) {
							projIn[6] = eElement.getElementsByTagName("lastbackup").item(0).getTextContent(); }
							else { projIn[6] = "unknown";}
			        }
					HGlobal.userProjects.add(projIn);
				}

		} catch (Exception ex) {
			 if (HGlobal.writeLogs)
				 HB0711Logging.logWrite("Error: User AUX file read error: " + ex.toString());
			 if (HGlobal.DEBUG)
				 System.out.println("ERROR:  AUX file read: \n" + ex.toString());
			 HGlobalCode.loguserAuxMessage(4,ex.getMessage());

			 ex.printStackTrace();
	      }
			if (HGlobal.writeLogs)
				HB0711Logging.logWrite("Action: exiting HB0744 UserAux");
	}   // End of readUserAUXfile

}	// End of HB0744UserAUX