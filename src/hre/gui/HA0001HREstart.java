package hre.gui;
/***********************************************************************************************
 * HRE Startup code.
 * Split off from HG0401HREMain 2019-10-15 to ensure initial
 *						 functions precede main menu display and to add NLS to Main
 * v0.00.0013 2019-11-07 and fix bugs in main menu open position (D.Ferguson)
 * v0.00.0014 2019-11-18 setup server/project ArrayLists properly (D Ferguson)
 *						 and changes for HB0711/0744 static classes (D Ferguson)
 * v0.00.0015 2019-12-08 add code to initialize Help via HG0514 (D Ferguson)
 * v0.00.0016 2019-12-09 added pointer to ProjectHandler (N. Tolleshaug)
 *            2019-12-18 added HBBusinessLayer[10] pointBusinessLayer (N. Tolleshaug)
 * v0.00.0018 2020-02-15 moved HG0514 Help setup to last action (D. Ferguson)
 * v0.00.0021 2020-03-28 moved Welcome screen display back to HG0401 (D Ferguson)
 * v0.00.0022 2020-06-28 add test for OS type and adjust HGlobal filepaths (D Ferguson)
 * v0.01.0023 2020-09-16 moved UIManager setup back from HG0401HREMain (D Ferguson)
 * 			  2020-09-20 converted to use JTattoo LAF (D Ferguson)
 * v0.01.0025 2020-11-22 modified open so Welcome and openLastClosed start here (D Ferguson)
 * 			  2021-02-25 converted to NLS text (D Ferguson)
 *            2021-03-02 Time consumption marking when startup (N. Tolleshaug)
 * v0.01.0026 2021-05-16 Welcome routine on own thread to enable Sample file setup (D Ferguson)
 *			  2021-05-16 Included both DDL20a and DDL21a (N. Tolleshaug)
 *			  2021-08-07 Set default Font to HGlobal stored value (D Ferguson)
 * v0.01.0027 2021-11-21 Started frame.openLastClosed(); as Runnable on EDT (N. Tolleshaug)
 * 			  2021-11-25 Show Wait msg and run openlastclosed on non-EDT Runnable (D Ferguson)
 * 			  2021-12-05 Start HRE on EDT, use SwingUtilities.invokeLater for OLP (N. Tolleshaug)
 * 			  2021-12-14 Includes DDL21c for NTo (N. Tolleshaug)
 *			  2022-09-04 Added English(UK) support (D Ferguson)
 * v0.01.0029 2023-04-10 Support for 3 open projects (N. Tolleshaug)
 * 			  2023-04-19 Removed NLS setup for HG0525/26Msgs (D Ferguson)
 * v0.03.0030 2023-09-04 Implement Dutch NLS support (D Ferguson)
 * 			  2023-09-04 Added new Log messsage with Build no. at startup (D Ferguson)
 * 			  2023-09-28 Added all known GUI to the setGUILanguage list (D Ferguson)
 **********************************************************************************************
 * NOTE - Special setting for user NTo for seed/sample files and help folders
 *********************************************************************************************/

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Locale.Category;

import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import hre.bila.HB0614Help;
import hre.bila.HB0711Logging;
import hre.bila.HB0744UserAUX;
import hre.bila.HBBusinessLayer;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectHandler;
import hre.bila.HBToolHandler;
import hre.dbla.HDDatabaseLayer;
import hre.nls.HG0401Msgs;
import hre.nls.HG0401WMsgs;
import hre.nls.HG0402Msgs;
import hre.nls.HG0403Msgs;
import hre.nls.HG0404Msgs;
import hre.nls.HG0405Msgs;
import hre.nls.HG0406Msgs;
import hre.nls.HG0407Msgs;
import hre.nls.HG0408Msgs;
import hre.nls.HG0409Msgs;
import hre.nls.HG0410Msgs;
import hre.nls.HG0414Msgs;
import hre.nls.HG0416Msgs;
import hre.nls.HG0417Msgs;
import hre.nls.HG0418Msgs;
import hre.nls.HG0450Msgs;
import hre.nls.HG0501Msgs;
import hre.nls.HG0505Msgs;
import hre.nls.HG0506Msgs;
import hre.nls.HG05070Msgs;
import hre.nls.HG05075Msgs;
import hre.nls.HG0508Msgs;
import hre.nls.HG0509Msgs;
import hre.nls.HG0512Msgs;
import hre.nls.HG0513Msgs;
import hre.nls.HG0520Msgs;
import hre.nls.HG0524Msgs;
import hre.nls.HG05300Msgs;
import hre.nls.HG05303Msgs;
import hre.nls.HG05306Msgs;
import hre.nls.HG0531Msgs;
import hre.nls.HG0540Msgs;
import hre.nls.HG0547Msgs;
import hre.nls.HG0551Msgs;
import hre.nls.HG0552Msgs;
import hre.nls.HG0575Msgs;
import hre.nls.HG0577Msgs;
import hre.nls.HG0581Msgs;
import hre.nls.HG0590Msgs;
import hre.nls.HG0660Msgs;
import hre.nls.HGJavaMsgs;
import hre.nls.HGlobalMsgs;

/**
 * HRE start code to initiate HRE system in Java
 * @author Nils Tolleshaug
 * @version v0.03.0030
 * @since 2019-10-15
 **/

public class HA0001HREstart {

	public static JDesktopPane mainPanel;
	public static HG0401HREMain mainFrame;
	// Pointers to HB.......Handler extends HBBusinessLayer
	private static HBBusinessLayer [] pointBusinessLayer = new HBBusinessLayer[11];

/*******************************************
 * Launch the HRE application.
 * @param args - main method start parameters (none defined yet)
 *******************************************/
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new HA0001HREstart();
            }
        });
	}

	public HA0001HREstart() {
		String dBversion = HGlobal.databaseVersion;
		HGlobalCode.recordStart();

/**
 * Start DEBUG entries (if debug = true)
 */
		if (HGlobal.DEBUG) {
			System.out.println("HRE version: " + HGlobal.buildNo + " / " + HGlobal.releaseDate);
			System.out.println("User name : " + System.getProperty("user.name"));
			System.out.println("Java version: " + System.getProperty("java.version"));
			System.out.println("Screen:   " + Toolkit.getDefaultToolkit().getScreenSize().toString());
			System.out.println("Language: " + Locale.getDefault(Category.DISPLAY).getDisplayLanguage());
			System.out.println("Country:  " + Locale.getDefault(Category.DISPLAY).getDisplayCountry());
		}

/**
 * Set Production Seed Database path for DDL 21b in HGlobal
 */
		//if (dBversion.startsWith("v21b")) HGlobal.seedProjectFile = HGlobal.seedProjectFile21b;

/**
 *  Set exclusive file paths for NTo or Nils - Override Production setting in HGlobal
 */
		if (System.getProperty("user.name").equals("Bruker") || System.getProperty("user.name").equals("nils")) {
			String pathUserHRE = System.getProperty("user.home") + File.separator + "HRE" + File.separator;
			HGlobal.helpPath = "file:\\" + pathUserHRE + "Help\\";
			HGlobal.keyStorePath = pathUserHRE + "KeyStore\\";
			if (dBversion.startsWith("v22b")) {
				HGlobal.seedProjectFile = pathUserHRE + "HRE Seed database v22b.mv.db";
				HGlobal.sampleProjectFile = pathUserHRE + "HRE Sample database v22b.mv.db";
			} else System.out.println("HA0001HREstart - selected DataBase not found! - " + dBversion);
		}

/**
 * Set HGlobal language so that any error msgs from HB0744 are in right language
 * First, get OS Language and convert to BCP-47 format (like 'en-US')
 * Then, if this is 'en-GB', 'en-AU' or 'en-NZ', make nativeLanguage=gb;
 *       else nativeLanguage is 1st 2 chars of systemLanguage
 */
		Locale locale = Locale.getDefault(Locale.Category.DISPLAY);
		HGlobal.systemLanguage = locale.toLanguageTag();
		// Assume for now that nativeLanguage = 1st 2 chars of systemLanguage
		HGlobal.nativeLanguage = HGlobal.systemLanguage.substring(0,2);
		// Now check systemLanguage for a code that implies GB English (like GB, AU, NZ)
		if (HGlobal.systemLanguage.endsWith("GB") |
				HGlobal.systemLanguage.endsWith("AU") |
				HGlobal.systemLanguage.endsWith("NZ")) HGlobal.nativeLanguage = "gb";
        if (HGlobal.DEBUG) System.out.println("Default GUI language: " + HGlobal.nativeLanguage);
		// and then set the GUI language
		setGUIlanguage();

/**
 *  Get OS type and reset filepaths as necessary (as HGlobal defaults are for Windows)
 */
		HGlobal.osType = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);

		// For Windows (only) adjust paths if NOT running from normal location (so we can run from a USB stick)
		// BUT we don't run this code for user=NTo or for User Directory having 'Eclipse' in its name
		// If OK, replace 'target' field with 'replacement', which is where HRE is really running from
		if (HGlobal.osType.contains("win")  															// if under Windows
			& !System.getProperty("user.name").equals("NTo") 											// and NOT user NTo
				& !System.getProperty("user.dir").toLowerCase(Locale.ENGLISH).contains("eclipse")) {	// and user.dir does NOT contain 'eclipse'
			String replacement = System.getProperty("user.dir");
			String target = "C:\\Program Files\\HRE";
			if (!replacement.equals(target)) {
				String start = HGlobal.helpPath;
				HGlobal.helpPath = start.replace(target, replacement);
				start = HGlobal.seedProjectFile;
				HGlobal.seedProjectFile = start.replace(target, replacement);
				start = HGlobal.sampleProjectFile;
				HGlobal.sampleProjectFile = start.replace(target, replacement);
				}
			}

		// Switch to Mac file paths for macOS
		if (HGlobal.osType.contains("mac")) {
			HGlobal.helpPath = HGlobal.helpPathMac;
			HGlobal.seedProjectFile = HGlobal.seedProjectMac;
			HGlobal.sampleProjectFile = HGlobal.sampleProjectMac;
			HGlobal.keyStorePath = HGlobal.keyStorePathMac;
			}
		// Switch to Linux file paths for any type of Linux
		if (HGlobal.osType.contains("ux")) {
			HGlobal.helpPath = HGlobal.helpPathUX;
			HGlobal.seedProjectFile = HGlobal.seedProjectUX;
			HGlobal.sampleProjectFile = HGlobal.sampleProjectUX;
			HGlobal.keyStorePath = HGlobal.keyStorePathUX;
			}

/**
 *  Set HGlobal User & Computer names
 */
		HGlobal.userID = System.getProperty("user.name");
		HGlobal.userCred[0] = HGlobal.userID;
		try {
		    InetAddress addr = InetAddress.getLocalHost();
		    HGlobal.thisComputer = addr.getHostName();			// Current computer ID
		}
		catch (UnknownHostException ex) {
			JOptionPane.showMessageDialog(null, "Hostname cannot be resolved: " + ex.getMessage(),
				"Unknown Hostname", JOptionPane.ERROR_MESSAGE);
		}

/**
 *  Invoke initial User AUX file handling and set GUI/Data languages
 *  This will create HRE sub-folder (if it doesn't exist) and either:
 *       write a new UserAUX file (if it doesn't exist);
 *    OR, if it exists, read UserAUX file to populate HGlobal
 */
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: HRE Build " + HGlobal.buildNo + " startup - initialising UserAUX");
		HB0744UserAUX.initUserAUXfile();
	    if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: loaded UserAUX settings");
		// Now that User settings are loaded, if the User's GUI language setting is different from
		// the language we guessed from the System Locale, then reset the GUIlanguage
		if (!HGlobal.systemLanguage.substring(0,2).equals(HGlobal.nativeLanguage)) {
			setGUIlanguage();
			if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: setting GUI language to " + HGlobal.nativeLanguage);
		}

/**
 * Set the look and feel using HGlobal settings from UserAUX
 * You can override the LAF by adding following type of code-line before the break statement:
 * 		UIManager.put(" component ", new ColorUIResource(UIManager.getColor(" component ").darker()));
 *   or UIManager.put("DesktopIcon.width", 400); as used below for more visible text in iconified frames
 */
		try {
			switch (HGlobal.lafNumber) {
	            case 0 :		// LAF_ACRYL
	            	com.jtattoo.plaf.acryl.AcrylLookAndFeel.setTheme(HGlobal.lafTheme, "", "HRE");
	                UIManager.setLookAndFeel("com.jtattoo.plaf.acryl.AcrylLookAndFeel");
	                UIManager.put("DesktopIcon.width", 400);
	                break;
	            case 1 :		// LAF_AERO
	                com.jtattoo.plaf.aero.AeroLookAndFeel.setTheme(HGlobal.lafTheme, "", "HRE");
	                UIManager.setLookAndFeel("com.jtattoo.plaf.aero.AeroLookAndFeel");
	                UIManager.put("DesktopIcon.width", 400);
	                break;
	            case 2 :		// LAF_ALUMINIUM
	                com.jtattoo.plaf.aluminium.AluminiumLookAndFeel.setTheme(HGlobal.lafTheme, "", "HRE");
	                UIManager.setLookAndFeel("com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");
	                UIManager.put("DesktopIcon.width", 400);
	                break;
	            case 3 : 		 // LAF_BERNSTEIN
	                com.jtattoo.plaf.bernstein.BernsteinLookAndFeel.setTheme(HGlobal.lafTheme, "", "HRE");
	                UIManager.setLookAndFeel("com.jtattoo.plaf.bernstein.BernsteinLookAndFeel");
	                UIManager.put("DesktopIcon.width", 400);
	                break;
	            case 4 : 		 // LAF_FAST
	                com.jtattoo.plaf.fast.FastLookAndFeel.setTheme(HGlobal.lafTheme, "", "HRE");
	                UIManager.setLookAndFeel("com.jtattoo.plaf.fast.FastLookAndFeel");
	                UIManager.put("DesktopIcon.width", 400);
	                break;
	            case 5 :		// LAF_GRAPHITE
	                com.jtattoo.plaf.graphite.GraphiteLookAndFeel.setTheme(HGlobal.lafTheme, "", "HRE");
	                UIManager.setLookAndFeel("com.jtattoo.plaf.graphite.GraphiteLookAndFeel");
	                UIManager.put("DesktopIcon.width", 400);
	                break;
	            case 6 :		// LAF_HIFI
	                com.jtattoo.plaf.hifi.HiFiLookAndFeel.setTheme(HGlobal.lafTheme, "", "HRE");
	                UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
	                UIManager.put("DesktopIcon.width", 400);
	                break;
	            case 7 :		// LAF_LUNA
	                com.jtattoo.plaf.luna.LunaLookAndFeel.setTheme(HGlobal.lafTheme, "", "HRE");
	                UIManager.setLookAndFeel("com.jtattoo.plaf.luna.LunaLookAndFeel");
	                UIManager.put("ProgressBar.foreground", Color.LIGHT_GRAY);
	                UIManager.put("DesktopIcon.width", 400);
	                break;
	            case 8 :		// LAF_MCWIN
	                com.jtattoo.plaf.mcwin.McWinLookAndFeel.setTheme(HGlobal.lafTheme, "", "HRE");
	                UIManager.setLookAndFeel("com.jtattoo.plaf.mcwin.McWinLookAndFeel");
	                UIManager.put("DesktopIcon.width", 400);
	                break;
	            case 9 :		// LAF_MINT
	                com.jtattoo.plaf.mint.MintLookAndFeel.setTheme(HGlobal.lafTheme, "", "HRE");
	                UIManager.setLookAndFeel("com.jtattoo.plaf.mint.MintLookAndFeel");
	                UIManager.put("DesktopIcon.width", 400);
	                break;
	            case 10 :		// LAF_NOIRE
	                com.jtattoo.plaf.noire.NoireLookAndFeel.setTheme(HGlobal.lafTheme, "", "HRE");
	                UIManager.setLookAndFeel("com.jtattoo.plaf.noire.NoireLookAndFeel");
	                UIManager.put("DesktopIcon.width", 400);
	                break;
	            case 11 :		// LAF_SMART
	                com.jtattoo.plaf.smart.SmartLookAndFeel.setTheme(HGlobal.lafTheme, "", "HRE");
	                UIManager.setLookAndFeel("com.jtattoo.plaf.smart.SmartLookAndFeel");
	                UIManager.put("DesktopIcon.width", 400);
	                break;
	            case 12 :		// LAF_TEXTURE
	                com.jtattoo.plaf.texture.TextureLookAndFeel.setTheme(HGlobal.lafTheme, "", "HRE");
	                UIManager.setLookAndFeel("com.jtattoo.plaf.texture.TextureLookAndFeel");
	                UIManager.put("DesktopIcon.width", 400);
	                break;
	        }
	        // Because the JTattoo code always sets the font=Dialog,
	        // we need to over-ride this to whatever is set in HGlobal.fontName
	     	Enumeration<?> keys = UIManager.getLookAndFeelDefaults().keys();
	        while (keys.hasMoreElements()) {
	             Object key = keys.nextElement();
	             Object value = UIManager.get(key);
	             // Find a font key resource
	             if (value != null && value instanceof javax.swing.plaf.FontUIResource) {
	                 FontUIResource origFontUI = (FontUIResource) value;
	                 // Make a new font resource with new font, but existing style, size
	                 Font font = new Font(HGlobal.fontName, origFontUI.getStyle(), origFontUI.getSize());
	                 FontUIResource newFontUI = new FontUIResource(font);
	                 // and reset the font
	                 UIManager.getLookAndFeelDefaults().put(key, newFontUI);
	             	}
	         	}
		} catch (Exception exc) {
			JOptionPane.showMessageDialog(null, "Look and Feel not available \n"
					+  exc.getMessage(),"Look and Feel Setting ",JOptionPane.ERROR_MESSAGE);
		}

/**
 *  Now (if WriteLogs=true) find or create Log File and write initial entry
 *  and also clean out old logs by a 'Manage' call
 */
		if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: HRE initialized");
		                        HB0711Logging.logManage(); }

/**
 * Set up BusinessLayer and xxxxxHandlers
 **/
		createBusinessLayer();
		HBProjectHandler pointProHand = (HBProjectHandler) pointBusinessLayer[0];
		if (HGlobal.DEBUG) System.out.println("Time is now: " + pointProHand.currentTime("yyyy-MM-dd / HH:mm:ss"));

/**
 * Display the Main menu
 **/
		HG0401HREMain frame = new HG0401HREMain(pointBusinessLayer);
		mainPanel = HG0401HREMain.mainPane;
		// Pointers to main panel in HB.....Handlers extends HBBusinessLayer
		pointBusinessLayer[0].mainPanel = mainPanel;
        pointBusinessLayer[0].mainFrame = HG0401HREMain.mainFrame;
        //pointBusinessLayer[1].mainPanel = mainPanel;
        //pointBusinessLayer[1].mainFrame = HG0401HREMain.mainFrame;
        //pointBusinessLayer[2].mainPanel = mainPanel;
        //pointBusinessLayer[3].mainPanel = mainPanel;
        //pointBusinessLayer[3].mainFrame = HG0401HREMain.mainFrame;
        //pointBusinessLayer[4].mainPanel = mainPanel;
        //pointBusinessLayer[5].mainPanel = mainPanel;
        //pointBusinessLayer[5].mainFrame = HG0401HREMain.mainFrame;
        //pointBusinessLayer[6].mainPanel = mainPanel;
        //pointBusinessLayer[7].mainPanel = mainPanel;
        pointBusinessLayer[8].mainPanel = mainPanel;
        pointBusinessLayer[8].mainFrame = HG0401HREMain.mainFrame;
        //pointBusinessLayer[9].mainPanel = mainPanel;
        //pointBusinessLayer[9].mainFrame = frame.mainFrame;
        //pointBusinessLayer[10].mainPanel = mainPanel;
        //pointBusinessLayer[10].mainFrame = HG0401HREMain.mainFrame;

        frame.setVisible(true);

/**
 * Display Welcome-screen, subject to 'showWelcome' option on own thread
 **/
        if (HGlobal.showWelcome)  {
        	new Thread(() -> {
				HG0401HREWelcome welcome=new HG0401HREWelcome();
				welcome.setLocationRelativeTo(mainPanel);
				welcome.setAlwaysOnTop(true);
				welcome.setVisible(true);
			}).start();
        }

/**
 * Start TCP server if set On in AUX file
 */
        String serverStatus = "";
        if (HGlobal.tcpServerOn) {
        	serverStatus = ((HBToolHandler)HG0401HREMain.pointBusinessLayer[8]).startTCPserver();
        	if (HGlobal.DEBUG) System.out.println(" " + serverStatus); //prints out the server's status
        	HGlobalCode.logTCPIPMessage(1, serverStatus);	// show status via JOptionPane msg
            if (serverStatus.contains("ERROR"))
            	HG0401HREMain.mainFrame.setStatusAction(10,"");
            else HG0401HREMain.mainFrame.setStatusAction(7);
        }

/**
 * Now show Wait msg and open last closed project on new thread to not hold up any other process
 * NB: only show/dispose the dialog if openLastProject is true
 */
        JOptionPane messagePane = new JOptionPane("Opening last project  \nPlease wait...",
	            JOptionPane.INFORMATION_MESSAGE);
	    JDialog dialogOpen = messagePane.createDialog("Open Project");
		dialogOpen.setLocation(HGlobal.mainX + 50, HGlobal.mainY + 80);
		dialogOpen.setModal(false);
		dialogOpen.setAlwaysOnTop(true);
		if (HGlobal.openLastProject) dialogOpen.setVisible(true);

		SwingUtilities.invokeLater(new Runnable() {
			    @Override
			    public void run() {
					frame.openLastClosed();
					if (HGlobal.openLastProject) dialogOpen.dispose();
					if (HGlobal.TIME) HGlobalCode.timeReport("complete creating all open windows");
			    }
		});

/**
 *  Now that User nativeLanguage set from UserAUX,
 *  initialize the Help system in that language.
 *  Run after Mainmenu display as it takes time to complete.
 **/
		HB0614Help.setupHelp();
		if (HGlobal.TIME) HGlobalCode.timeReport("complete HA0001HREstart in HRE");

	} // End Main constructor

/***********************************
 * Set NLS variable for GUI Language
 ***********************************/
	public static void setGUIlanguage() {
		// Some possible Native language strings are the following. Only those
		// marked have NLS tables built, so if we find anything else,
		// we have to change back to English.
		//		"da" 	 Danish
		//		"de"	 German - NLS tables exist
		//		"el"	 Greek
		//		"en"	 English(US) - NLS tables exist
		//		"gb"	 English(UK) - NLS tables exist
		//		"es"	 Spanish- NLS tables exist
		//		"fr"	 French - NLS tables exist
		//		"it"	 Italian - NLS tables exist
		//		"nl"	 Dutch - NLS tables exist
		//		"no"	 Norwegian - NLS tables exist
		//		"pt" 	 Portugese

		// So test the nativeLanguage string - if not valid, set it back to 'en'
		if (	!"en".equals(HGlobal.nativeLanguage)
			&& 	!"gb".equals(HGlobal.nativeLanguage)
			&& 	!"fr".equals(HGlobal.nativeLanguage)
			&&	!"de".equals(HGlobal.nativeLanguage)
			&&	!"es".equals(HGlobal.nativeLanguage)
			&&	!"it".equals(HGlobal.nativeLanguage)
			&&	!"nl".equals(HGlobal.nativeLanguage)
			&& 	!"no".equals(HGlobal.nativeLanguage)
			) 	HGlobal.nativeLanguage = "en";

		// Invoke language switch for Java-managed text items
		new HGJavaMsgs(HGlobal.nativeLanguage);
		// then invoke the NLS language switch for each screen
		new HG0401Msgs(HGlobal.nativeLanguage);
		new HG0401WMsgs(HGlobal.nativeLanguage);
		new HG0402Msgs(HGlobal.nativeLanguage);
		new HG0403Msgs(HGlobal.nativeLanguage);
		new HG0404Msgs(HGlobal.nativeLanguage);
		new HG0405Msgs(HGlobal.nativeLanguage);
		new HG0406Msgs(HGlobal.nativeLanguage);
		new HG0407Msgs(HGlobal.nativeLanguage);
		new HG0408Msgs(HGlobal.nativeLanguage);
		new HG0409Msgs(HGlobal.nativeLanguage);
		new HG0410Msgs(HGlobal.nativeLanguage);
		new HG0414Msgs(HGlobal.nativeLanguage);
		new HG0416Msgs(HGlobal.nativeLanguage);
		new HG0417Msgs(HGlobal.nativeLanguage);
		new HG0418Msgs(HGlobal.nativeLanguage);
		new HG0450Msgs(HGlobal.nativeLanguage);
		new HG0501Msgs(HGlobal.nativeLanguage);
		new HG0505Msgs(HGlobal.nativeLanguage);
		new HG0506Msgs(HGlobal.nativeLanguage);
		new HG05070Msgs(HGlobal.nativeLanguage);
		new HG05075Msgs(HGlobal.nativeLanguage);
		new HG0508Msgs(HGlobal.nativeLanguage);
		new HG0509Msgs(HGlobal.nativeLanguage);
		new HG0512Msgs(HGlobal.nativeLanguage);
		new HG0513Msgs(HGlobal.nativeLanguage);
		new HG0520Msgs(HGlobal.nativeLanguage);
		new HG0524Msgs(HGlobal.nativeLanguage);
		new HG05300Msgs(HGlobal.nativeLanguage);
		new HG05303Msgs(HGlobal.nativeLanguage);
		new HG05306Msgs(HGlobal.nativeLanguage);
		new HG0531Msgs(HGlobal.nativeLanguage);
		new HG0540Msgs(HGlobal.nativeLanguage);
		new HG0547Msgs(HGlobal.nativeLanguage);
		new HG0551Msgs(HGlobal.nativeLanguage);
		new HG0552Msgs(HGlobal.nativeLanguage);
		new HG0575Msgs(HGlobal.nativeLanguage);
		new HG0577Msgs(HGlobal.nativeLanguage);
		new HG0581Msgs(HGlobal.nativeLanguage);
		new HG0590Msgs(HGlobal.nativeLanguage);
		new HG0660Msgs(HGlobal.nativeLanguage);
		new HGlobalMsgs(HGlobal.nativeLanguage);

	}  // End setGUIlanguage

/*******************************************
 * 	Initiate BusinessLayer and DatabaseLayer
 *******************************************/
	private static void createBusinessLayer() {

		HDDatabaseLayer pointDBlayer;
		if (HGlobal.DEBUG)
			System.out.println("Create Business Layers");

/**
 * Each main menu point has its own HB....Handler extending HBBusinessLayer
 * Create and set up pointers to each main menu point Handler
 */
		pointBusinessLayer[0] = new HBProjectHandler();
		pointBusinessLayer[1] = new HBPersonHandler(null); // Needed for Plugin report person
		//pointBusinessLayer[2] = new HBResearchHandler();
		//pointBusinessLayer[3] = new HBViewPointHandler();
		//pointBusinessLayer[4] = new HBEventTaskHandler();
		//pointBusinessLayer[5] = new HBWhereWhenHandler();
		//pointBusinessLayer[6] = new HBEvidenceHandler();
		//pointBusinessLayer[7] = new HBReportHandler();
		pointBusinessLayer[8] = new HBToolHandler();
		//pointBusinessLayer[9] = new HBHelpHandler();
		//pointBusinessLayer[10] = new HBMediaHandler();

/**
 * Set up pointers from each Main menu HB.....Handler to DatabaseLayer
 */
		pointDBlayer = new HDDatabaseLayer();
		pointBusinessLayer[0].pointDBlayer = pointDBlayer;
		pointBusinessLayer[1].pointDBlayer = pointDBlayer;	// Needed for Plugin report person
		//pointBusinessLayer[2].pointDBlayer = pointDBlayer;
		//pointBusinessLayer[3].pointDBlayer = pointDBlayer;
		//pointBusinessLayer[4].pointDBlayer = pointDBlayer;
		//pointBusinessLayer[5].pointDBlayer = pointDBlayer;
		//pointBusinessLayer[6].pointDBlayer = pointDBlayer;
		//pointBusinessLayer[6].pointDBlayer = pointDBlayer;
		//pointBusinessLayer[7].pointDBlayer = pointDBlayer;
		pointBusinessLayer[8].pointDBlayer = pointDBlayer;
		//pointBusinessLayer[9].pointDBlayer = pointDBlayer;
		//pointBusinessLayer[10].pointDBlayer = pointDBlayer;

	} // End createBusinessLayer()

/**
 * Get array with all pointers to BusinessHandlers and Handlers
 * @return HBBusinessLayer[] pointer array
 */
		public static HBBusinessLayer[] getBusinessLayerPointers() {
			return pointBusinessLayer;
		}

} // End HA0001HREstart