package hre.gui;
/****************************************************************************************
 * HRE Main menu - Specification 04.01 GUI_Main menu 2020-02-27
 * v0.00.0007 2019-05-13 added UserAUX and Logging capability (D Ferguson)
 * v0.00.0009 2019-07-26 fix startup logging error (D Ferguson)
 * v0.00.0010 2019-08-24 add code to save/restore mainMenu bounds (D Ferguson)
 * 					     and make GUI font an AppSetting variable, default Arial
 * v0.00.0012 2019-09-17 added call to HG0507EntitySelect (D Ferguson)
 * v0.00.0013 2019-11-07 split off startup code to HA001; add basic NLS (Nils T/D Ferguson)
 * v0.00.0014 2019-11-18 changes for HB0711/0744 static classes (D Ferguson)
 * v0.00.0015 2019-11-29 changed code for HG0514 call (D Ferguson)
 * v0.00.0016 2019-12-09 added pointer to ProjectHandler (N Tolleshaug)
 * v0.00.0017 2020-01-11 code to support reset of GUIlanguage inflight (DF & NT)
 * v0.00.0018 2020-02-20 added close project when Exit HRE (N Tolleshaug)
 * v0.00.0020 2020-03-17 added TMG to HRE convert in Tool menu (N Tolleshaug)
 *            2020-03-26 changes in above code returned to DF
 * v0.00.0021 2020-03-28 added code to open last project if option on (D Ferguson)
 *            2020-04-10 add/delete projects at bottom of Project menu (D Ferguson)
 *                       enforce maximum of 3 projects open at once
 *                       fix bug in Exit where could not cancel out of Close
 *                       Implement HG0417TMGProjectImport call
 * 	          2020-04-14 Implemented HG0507EntityPersonSelect (N Tolleshaug)
 *            			 Implemented HG0507EntityPlaceSelect (N Tolleshaug)
 *     		  2020-04-20 Implement call to HG0416ProjectStatus (D Ferguson)
 *            2020-05-03 Check if numProjectsOpen=0 before calling HG0507s (D Ferguson)
 * v0.00.0022 2020-05-10 Split Viewpoint calls into separate calls per VP (D Ferguson)
 *            2020-07-08 hre.pres classes now handled by HBTestPresPersonHandler(N Tolleshaug)
 *            2020-07-09 execute project close automagically if only 1 open (D Ferguson)
 *            2020-07-13 handle extra windowClose events create by clickProject (D Ferguson)
 *            2020-07-19 identify the 3 close cases and pass to ProjectClose (D Ferguson)
 *            2020-07-31 remove setAlwaysOnTop from HG0507 invocations (D Ferguson)
 *            2020-08-01 only open HG0507s once; else bring them to the front (D Ferguson)
 *            2020-08-02 disable menus that need an open project if none open (D Ferguson)
 * v0.01.0023 2020-09-01 added Report menu item for Ancestor/descendant (D Ferguson)
 *            2020-09-02 Implemented read/write reminder for HG0530ViewPeople (N Tolleshaug)
 *            2020-09-10 Calling initiatePersonSelect in HBPersonHandler (N Tolleshaug)
 *            2020-09-14 Call initiateLocationSelect in HBWhereWhenHandler (N Tolleshaug)
 *            2020-09-15 removed dead code in those areas (D Ferguson)
 *            2020-09-16 Moved UIManager setting to HA0001HREstart (D Ferguson)
 *            2020-09-30 Removed all font settings for JTattoo implement (D Ferguson)
 *            2020-10-03 Error fixed - init HRE with T302 table empty (N Tolleshaug)
 * v0.01.0025 2020-10-09 Now HG0507s are JIFs need change so only 1 can be open (D Ferguson)
 * 	          2020-10-26 Move P (status) icon to left of menu (except for Macs) (D Ferguson)
 * 			  2020-10-28 Remove temp PeopleRecent code (as pres package removed) (D Ferguson)
 * 			  2020-11-05 Add F1 support (D Ferguson)
 * 			  2020-11-16 Add status bar at bottom of Main frame (D Ferguson)
 * 			  2020-11-20 Put openLastClosed in own method, driven from HA0001 (N Tolleshaug)
 * 			  2021-01-20 Reset person ID and multiple PersonViewPoint (N Tolleshaug)
 * 			  2021-01-22 Implemented use of pointOpenProject for personVP (N Tolleshaug)
 * 			  2021-01-24 Opening of empty database - personVP initiated(N Tolleshaug)
 * 			  2021-01-28 Removed use of HGlobal.selectedProject (N Tollehaug)
 * 			  2021-01-28 Rewritten activate LocationSelect window (N Tolleshaug)
 * 			  2021-01-30 Used same code for PersonSelect window (D Ferguson)
 * 						 Also added Project Summary to Project menu (D Ferguson)
 * 		  	  2021-01-30 Fixed 13.3 opening only one pVP from main menu (N Tolleshaug)
 * 			  2021-02-05 revised NLS target property names (D Ferguson)
 * 			  2021-02-12 add Tools -> DBMaintenance menu item (D Ferguson)
 *            2021-03-06 Implemented handling of 5 Location VP (N Tolleshaug)
 *            2021-04-01 Status message for project updated (N Tolleshaug)
 *            2021-04-22 Fixed error msg when opening project from menu list (N Tolleshaug)
 * v0.01.0026 2021-04-23 Added call to HG0524PersonNameStyles (D Ferguson)
 * 			  2021-06-07 Adjust Project Status position when running on macOS (D Ferguson)
 * 			  2021-08-21 Added pf4j plugin handling (N Tolleshaug)
 * 			  2021-09-15 Added pf4j plugin for research, report, tool and VP (N Tolleshaug)
 * v0.01.0027 2021-10-01 Handling of local/remote connect to database (N Tolleshaug)
 * 			  2021-10-01 Added HG0418ProjectRemote as new Project menu entry (D Ferguson)
 * 			  2022-03-26 Add calls to HG0505 (AddPers) and HG0506 (ManagePers)(D Ferguson)
 * 			  2022-03-29 Support Person menu, Recently used function (D Ferguson)
 *			  2022-04-12 Implement male/female icons on Person/Recent list (D Ferguson)
 *			  2022-06-08 Revised VP menu and VP open/close handling (D Ferguson)
 *			  2022-08-02 Handling of HG0508ManageLocation (N. Tolleshaug)
 *			  2022-08-21 Remove redundant menu entries (D Ferguson)
 *			  2022-09-05 Activated HG0524ManageNameStyles (N. Tolleshaug)
 * v0.01.0028 2023-01-31 Activated HG0505AddPerson (N. Tolleshaug)
 * 			  2023-04-10 Support for 3 open projects (N. Tolleshaug)
 * v0.03.0030 2023-07-24 Adjust Evidence menu structure (D Ferguson)
 * 			  2023-09-04 Ensure 'Open Last Project failed' msg always visible (D Ferguson)
 * 			  2023-09-08 Support addPersonRecord (N. Tolleshaug)
 * 			  2023-10-05 Ensure Recent Person list retained if language changes (D Ferguson)
 * v0.03.0031 2023-11-29 Expand Add Person menu items (D Ferguson)
 * 			  2023-12-07 Implemented detailed Add Person menu items (N Tolleshaug)
 * 			  2024-01-01 Add routine for deletion of Person from Recents list (D Ferguson)
 * 			  2024-02-18 Clear Recent Person list when project open off proj list (D Ferguson)
 * 			  2024-07-27 Ensure only one HG0526 can be open at a time (D Ferguson)
 * v0.04.0032 2025-01-12 Implement Evidence menu items (D Ferguson)
 * 			  2025-01-17 Enable all Evidence menu items (D Ferguson)
 * 			  2025-01-18 Enable Event menu (D Ferguson)
 * 			  2025-01-21 Removed extension.setBusinessLayerPointer(pointBusinessLayer) (N Tolleshaug)
 * 			  2025-02-08 Add new Evidence Source Elements menu item (D Ferguson)
 * 			  2025-06-29 Set calls to Evidence screens with pointOpenProject (D Ferguson)
 * 			  2025-09-25 Add greyed-out logo to contentsPane to improve look (D Ferguson)
 *****************************************************************************************/

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.pf4j.DefaultExtensionFinder;
import org.pf4j.DefaultPluginManager;
import org.pf4j.ExtensionFinder;
import org.pf4j.PluginManager;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import hre.bila.HB0614Help;
import hre.bila.HB0711Logging;
import hre.bila.HB0744UserAUX;
import hre.bila.HBBusinessLayer;
import hre.bila.HBException;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectHandler;
import hre.bila.HBProjectOpenData;
import hre.bila.HBToolHandler;
import hre.bila.HBWhereWhenHandler;
import hre.nls.HG0401Msgs;
import hre.pf4j.ext.MainMenuExtensionPoint;
import hre.pf4j.ext.ReportMenuExtensionPoint;
import hre.pf4j.ext.ResearchMenuExtensionPoint;
import hre.pf4j.ext.ToolMenuExtensionPoint;
import hre.pf4j.ext.ViewpointMenuExtensionPoint;
import net.miginfocom.swing.MigLayout;

/**
 * HRE Main menu
 * @author D Ferguson
 * @version v0.04.0032
 * @since 2020-05-10
 */

public class HG0401HREMain extends JFrame {
	private static final long serialVersionUID = 001L;

	public static JDesktopPane mainPane; 		// Make public so all JIFs can be added to it
	public static HG0401HREMain mainFrame;
	private BufferedImage logo = null;
	private JMenuBar menuBar;
	private JMenu menuReports;					// for use by plugin code
	private JMenu menuTools;
	private JMenu menuViewpoint;
	private JMenu menuResearch;
	private JMenu menuProj;						// to allow manipulation of Project menu items
	private JMenu menuPerson;					// ditto Person
	private JMenuItem menuPersonPartner;		// and ditto these menu items for HG0506ManagePerson to control
	private JMenuItem menuPersonFather;
	private JMenuItem menuPersonMother;
	private JMenuItem menuPersonBrother;
	private JMenuItem menuPersonSister;
	private JMenuItem menuPersonSon;
	private JMenuItem menuPersonDau;

	private ArrayList<String> menuPerName = new ArrayList<>();	// For storing person Names for the Person recent used menu
	private ArrayList<String> menuPerSex = new ArrayList<>();	// For storing person Sex for the Person recent used menu
	private ArrayList<Long> menuPerPID = new ArrayList<>();		// For storing person PIDs for the Person recent used menu

	private boolean longMenu = false;
	private int countMenuOpenProjects = 0;		// counts the number of projects opened by clicking on the Project menu 'Recent Projects'
	private int exitCount = 0;					// counts the number of windowClosing events that are going to be triggered

	private JLabel statusProjName;
	private JLabel statusAction;
	Window windowInstance;
	Window[] allWins;
	public static HBBusinessLayer[] pointBusinessLayer = new HBBusinessLayer[10];

	boolean initiatedPersonWindows = false;
	boolean initiatedLocationWindows = false;
	final int maxPersonVPs = 5;
	final int maxLocationVPs = 5;

	long proOffset = 1000000000000000L;
	long null_RPID  = 1999999999999999L;

/**
* Setup HRE environment
*/
	public HG0401HREMain(HBBusinessLayer[] pointBusinessLayer) {
		HG0401HREMain.pointBusinessLayer = pointBusinessLayer;
		mainFrame = this;
		// Set up main window
		mainWindowSetup();
		// Setup the main menu
		mainMenuSetup();

	} // End HG0401HREMain constructor

/**
 *  For storing references to open selected project
 */
	private static HBProjectOpenData selectedOpenProject = null;

	public void setSelectedOpenProject(HBProjectOpenData newPointer) {
	        selectedOpenProject = newPointer;
	}
	public HBProjectOpenData getSelectedOpenProject() {
	       return selectedOpenProject;
	}

/**
 * Open last closed Project if user settings request it and project name valid
 **/
	public void openLastClosed() {
		if (HGlobal.openLastProject & HGlobal.lastProjectClosed.length() != 0) {
			// Open project action for opening lastProjectClosed project
			int errorCode = 1;
			try {
				int selectIndex = ((HBProjectHandler) pointBusinessLayer[0]).getUserProjectByName(HGlobal.lastProjectClosed);
				String [] projectData = ((HBProjectHandler) pointBusinessLayer[0]).getUserProjectByIndex(selectIndex);
		       // If local project
					if (HGlobal.thisComputer.equals(projectData[3])) {
						errorCode = ((HBProjectHandler) pointBusinessLayer[0]).openProjectLocal(HGlobal.lastProjectClosed);
		        	} else {
		        // Remote Login
		        		if (HGlobal.DEBUG) System.out.println("Remote server name: " + projectData[3]);	//$NON-NLS-1$
						HG0575DBLogon poinLogonScreen = new HG0575DBLogon((HBProjectHandler) pointBusinessLayer[0], projectData);
						poinLogonScreen.setModalityType(ModalityType.APPLICATION_MODAL);
						Point xymainPane = HG0401HREMain.mainPane.getLocationOnScreen();
						poinLogonScreen.setLocation(xymainPane.x + 100, xymainPane.y + 100);
						poinLogonScreen.setVisible(true);
						errorCode = poinLogonScreen.getErrorCode();
		        	}

				if (errorCode == 0) {
		            longMenu = true;
					mainMenuSetup();			// redraw the main menu for long menu
			        revalidate();				// revalidate it
	            } else {
	            	if (HGlobal.DEBUG)
	            		System.out.println("HG0401HREMain Failed to open last closed project: " + HGlobal.lastProjectClosed);   //$NON-NLS-1$
	                JOptionPane msgFail = new JOptionPane(HG0401Msgs.Text_0	+ HGlobal.lastProjectClosed,	// Open last closed project failed:
	        	            JOptionPane.ERROR_MESSAGE);
	        	    JDialog dialogFail = msgFail.createDialog(HG0401Msgs.Text_5);	// Open Last Project
	        	    // Ensure msg is under 'Opening last project' msg
	        		dialogFail.setLocation(HGlobal.mainX + 50, HGlobal.mainY + 250);
	        		dialogFail.setVisible(true);
	            }

	        } catch (HBException hbe) {
	        	System.out.println("HG0401HREMain setSelectedOpenProject Error: " +  HGlobal.lastProjectClosed); //$NON-NLS-1$
				if (HGlobal.writeLogs) {
					HB0711Logging.logWrite("ERROR opening last project: " + HGlobal.lastProjectClosed + hbe.getMessage()); //$NON-NLS-1$
					HB0711Logging.printStackTraceToFile(hbe);
				}
				userInfoOpenProject(errorCode, HGlobal.lastProjectClosed);
			}
		}
	} // End openLastClosed

/***********************
 * Build the Main JFrame
 ***********************/
	private void mainWindowSetup()  {
		this.windowInstance = this;
		HGlobal.mainWindowInstance = this.windowInstance;		// save this instance for use by JIFs
		setName(HG0401Msgs.Text_3);
		setSize(new Dimension(HGlobal.mainW, HGlobal.mainH));
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/hre/images/HRE-32.png"))); //$NON-NLS-1$
		setTitle("  HRE - History Research Engine"); //$NON-NLS-1$
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);    // set to do nothing as we have our own Close Handler

		// Create a bufferedimage from the current HRE logo
		try {
			logo = ImageIO.read(HG0401HREMain.class.getResourceAsStream("/hre/images/HRE-logo.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Setup screen mainPane as a JDesktopPane for Viewpoint JInternalFrames
		// and incorporate the logo in it, but greyed out
		mainPane = new JDesktopPane() {
			private static final long serialVersionUID = 1L;
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				int x = (getWidth() - logo.getWidth())  / 2;		// center the logo image
				int y = (getHeight() - logo.getHeight()) / 2;
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setComposite(AlphaComposite.SrcOver.derive(0.1f));  //<< set to 0.1 level of opaqueness
				g2.drawImage(logo, x, y, this);
				g2.dispose();
			}
		};
		mainPane.setBackground(SystemColor.control);
		mainPane.setLayout(null);								// absolute layout, so we control placement of Viewpoints, etc
		getContentPane().add(mainPane, BorderLayout.CENTER);	// place in centre - (JFrames are Border Layout by default)

		// Setup a Status Bar at bottom of frame
		JPanel statusBar = new JPanel(new MigLayout("insets 0", "[][grow][]", "[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		statusBar.setForeground(UIManager.getColor("MenuBar.foreground")); //$NON-NLS-1$
		statusBar.setBackground(UIManager.getColor("MenuBar.background")); //$NON-NLS-1$
		statusBar.setPreferredSize(new Dimension(0, 30));
		statusBar.setBorder(new CompoundBorder(new LineBorder(Color.DARK_GRAY), new EmptyBorder(4, 4, 4, 4)));
		JLabel statusProject = new JLabel(HG0401Msgs.Text_14);
		statusBar.add(statusProject, "cell 0 0"); //$NON-NLS-1$
		statusProjName = new JLabel(HG0401Msgs.Text_4);
		statusProjName.setPreferredSize(new Dimension(200,14));
		statusBar.add(statusProjName, "cell 0 0"); //$NON-NLS-1$
		statusAction = new JLabel(" "); //$NON-NLS-1$
		statusAction.setMinimumSize(new Dimension(400, 14));
		statusAction.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusBar.add(statusAction, "cell 1 0, center"); //$NON-NLS-1$
		JLabel statusHREID = new JLabel(" HRE "+HGlobal.buildNo+" "); //$NON-NLS-1$ //$NON-NLS-2$
		statusBar.add( statusHREID, "cell 2 0, alignx right"); //$NON-NLS-1$
		getContentPane().add(statusBar, BorderLayout.SOUTH);

		// If last position of screen is in HGlobal then use it, ELSE centre on system primary monitor
		if (HGlobal.mainX == 0) setLocationRelativeTo(null);
			else setBounds(HGlobal.mainX, HGlobal.mainY, HGlobal.mainW, HGlobal.mainH);
		HGlobal.mainX = getLocation().x;	// save the x,y position on the screen in case it never moves
		HGlobal.mainY = getLocation().y;

	}	// End mainWindowSetup

/************************************************
 * Methods to set Text into the Status Bar fields
 ***********************************************/
   // To set Project Name text into the Status Bar field
   public void setStatusProject(String projText) {
	   statusProjName.setText(projText);
   } 	// End setStatusProject(String)

// To set Action text into the Status Bar field
   public void setStatusAction(String actionText) {
	   statusAction.setText(actionText);
   } 	// End setStatusAction(String)

// To set Action text into the Status Bar field according to actionCode
   public void setStatusAction(int actionCode) {
	   setStatusAction(actionCode, ""); //$NON-NLS-1$
   } 	// End setStatusAction(code)

// To set Action text into the Status Bar field according to actionCode
   public void setStatusAction(int actionCode, String actionText) {
	   if (actionCode == 0) actionText = HG0401Msgs.Text_12;	// Project opening...
	   if (actionCode == 1) actionText = HG0401Msgs.Text_16;	// Project open rejected
	   if (actionCode == 2) actionText = HG0401Msgs.Text_18;	// Project open
	   if (actionCode == 3) actionText = HG0401Msgs.Text_20 + actionText.trim() + HG0401Msgs.Text_22;	// Project <name> closed
	   if (actionCode == 4) actionText = HG0401Msgs.Text_24;	// Project closed
	   if (actionCode == 5) actionText = " "; //$NON-NLS-1$
	   if (actionCode == 6) actionText = " "; //$NON-NLS-1$
	   if (actionCode == 7) actionText = HG0401Msgs.Text_26;	// TCP server activated
	   if (actionCode == 8) actionText = HG0401Msgs.Text_28;	// TCP server stopped
	   if (actionCode == 9) actionText = HG0401Msgs.Text_135 + actionText;	// Remote connected to
	   if (actionCode == 10) actionText = HG0401Msgs.Text_136 + actionText;	// TCP server start failed
	   setStatusAction(actionText);
   }    // End setStatusAction(code, String)

/*********************************************************
 * Setup all menus as per the GUI Mainmenu specification
 * (note: for those menus for HRE v0.2+, only stubs exist)
 *********************************************************/
	private void mainMenuSetup()  {
		// MAIN MENU BAR
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		// Project Status "P" menu item
		JMenu menuStatus= new JMenu("");  //$NON-NLS-1$
	    Icon im1= new ImageIcon(getClass().getResource("/hre/images/Proj_BW_24.png")); //$NON-NLS-1$
	    menuStatus.setIcon(im1);
		// If running on Windows or Linux add "P" icon at left of menu plus padding before next menu entry
		if (HGlobal.osType.contains("win") || HGlobal.osType.contains("ux")) {	//$NON-NLS-1$ //$NON-NLS-2$
					menuBar.add(menuStatus);
					menuBar.add(Box.createRigidArea(new Dimension(10,24)));
					}

		// PROJECT MENU
		// TAKE CARE HERE! currently there are 16 menu entries (incl. Separator) before dynamic items are added
		// If the number of menu entries are changed you MUST change code in Class removeProjMenuItem
		menuProj = new JMenu(HG0401Msgs.Text_6);			// Project
	    menuBar.add(menuProj);
	    	JMenuItem menuProjOpen = new JMenuItem(HG0401Msgs.Text_7, new ImageIcon(getClass().getResource("/hre/images/gear-16.png"))); //$NON-NLS-1$
	    	menuProj.add(menuProjOpen);
	    	JMenuItem menuProjNew = new JMenuItem(HG0401Msgs.Text_9, new ImageIcon(getClass().getResource("/hre/images/projectnew_BW_16.png"))); //$NON-NLS-1$
	    	menuProj.add(menuProjNew);
			JMenuItem menuProjRmt = new JMenuItem(HG0401Msgs.Text_30, new ImageIcon(getClass().getResource("/hre/images/projectrmt_BW_16.png")));  //$NON-NLS-1$
	    	menuProj.add(menuProjRmt);
	    	JMenuItem menuProjBackup = new JMenuItem(HG0401Msgs.Text_11, new ImageIcon(getClass().getResource("/hre/images/projectbkup_BW_16.png"))); //$NON-NLS-1$
	    	menuProj.add(menuProjBackup);
	    	JMenuItem menuProjRestore = new JMenuItem(HG0401Msgs.Text_13, new ImageIcon(getClass().getResource("/hre/images/projectrestore_BW_16.png"))); //$NON-NLS-1$
	    	menuProj.add(menuProjRestore);
	    	JMenuItem menuProjClose = new JMenuItem(HG0401Msgs.Text_15, new ImageIcon(getClass().getResource("/hre/images/projectclose_BW_16.png"))); //$NON-NLS-1$
	    	menuProjClose.setEnabled(false);							// disabled until a project setup or opened
	    	menuProj.add(menuProjClose);
	    	JMenuItem menuProjCompare = new JMenuItem(HG0401Msgs.Text_17, new ImageIcon(getClass().getResource("/hre/images/projectcomp_BW_16.png"))); //$NON-NLS-1$
	    	menuProjCompare.setEnabled(false);
	    	menuProj.add(menuProjCompare);
	    	JMenuItem menuProjMerge = new JMenuItem(HG0401Msgs.Text_19, new ImageIcon(getClass().getResource("/hre/images/projectmerge_BW_16.png"))); //$NON-NLS-1$
	    	menuProjMerge.setEnabled(false);
	    	menuProj.add(menuProjMerge);
	    	JMenuItem menuProjSplit = new JMenuItem(HG0401Msgs.Text_21, new ImageIcon(getClass().getResource("/hre/images/projectsplit_BW_16.png"))); //$NON-NLS-1$
	    	menuProjSplit.setEnabled(false);
	    	menuProj.add(menuProjSplit);
	    	JMenuItem menuProjCopy = new JMenuItem(HG0401Msgs.Text_23, new ImageIcon(getClass().getResource("/hre/images/projectcopy_BW_16.png"))); //$NON-NLS-1$
	    	menuProj.add(menuProjCopy);
	    	JMenuItem menuProjRename = new JMenuItem(HG0401Msgs.Text_25, new ImageIcon(getClass().getResource("/hre/images/projectrename_BW_16.png"))); //$NON-NLS-1$
	    	menuProj.add(menuProjRename);
	    	JMenuItem menuProjDelete = new JMenuItem(HG0401Msgs.Text_27, new ImageIcon(getClass().getResource("/hre/images/projectdel_BW_16.png"))); //$NON-NLS-1$
	    	menuProjDelete.setEnabled(true);
	    	menuProj.add(menuProjDelete);
	    	JMenuItem menuProjSummry = new JMenuItem(HG0401Msgs.Text_113, new ImageIcon(getClass().getResource("/hre/images/projectsumm_BW_16.png"))); //$NON-NLS-1$
	    	menuProjSummry.setEnabled(false);
	    	menuProj.add(menuProjSummry);
	    	JMenuItem menuExitHRE = new JMenuItem(HG0401Msgs.Text_29, new ImageIcon(getClass().getResource("/hre/images/exithre_16.png"))); //$NON-NLS-1$
	    	menuProj.add(menuExitHRE);
	    	menuProj.addSeparator();
	    	JMenuItem menuProjectText = new JMenuItem(HG0401Msgs.Text_124);
	    	menuProj.add(menuProjectText);
	    	// Add the contents of userProjects to the end of the Projects menu
	    	// using the clickProject Action response and HRE icon; BUT if
	    	// the project is open already, just show the open folder icon
		   int numProj = HGlobal.userProjects.size();
		   int numOpen = HGlobal.openProjects.size();
		   boolean projectIsOpen = false;
		   for (int i = 0; i < numProj; i++) {
				String projInfo[] = HGlobal.userProjects.get(i);
				String projName = projInfo[0];
				for (int j = 0; j < numOpen; j++) {
					String openName = HGlobal.openProjects.get(j).getProjectData()[0];
					if (openName.contentEquals(projName)) projectIsOpen = true;
					}
				if (projectIsOpen)
						 menuProj.add(new JMenuItem(projName, new ImageIcon(getClass().getResource("/hre/images/open_16.png")))); //$NON-NLS-1$
					else menuProj.add(new JMenuItem(new clickProject(projName, new ImageIcon(getClass().getResource("/hre/images/HRE-16.png")))));	//$NON-NLS-1$
				projectIsOpen = false;
		   		}

	    // Person MENU
		// Define OS-independent Keystroke accelerators for this menu
		KeyStroke ctrlM = KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()); // For opening ManagePerson
		// TAKE CARE HERE! currently there are 9 menu entries (incl. Separator) before dynamic items are added
		// If the number of menu entries are changed you MUST also change Classes updateRecentPersons, deleteRecentPerson, clickPeople, removeRecents
		menuPerson = new JMenu(HG0401Msgs.Text_31);							// Person
		menuPerson.setVisible(false);	// set not visible until 1st project opened
		menuBar.add(menuPerson);
			JMenuItem menuPersonSel = new JMenuItem(HG0401Msgs.Text_32);	// Select Person by...
			menuPerson.add(menuPersonSel);
			JMenuItem menuPersonMan = new JMenuItem(HG0401Msgs.Text_34);	// Manage Person
			menuPersonMan.setAccelerator(ctrlM);							// Ctrl-M
			menuPerson.add(menuPersonMan);
			JMenu menuPersonAdd = new JMenu(HG0401Msgs.Text_180);			// Add Person
			menuPerson.add(menuPersonAdd);
				JMenuItem menuPersonNew = new JMenuItem(HG0401Msgs.Text_181);	// Add New Person
				menuPersonAdd.add(menuPersonNew);
				menuPersonPartner = new JMenuItem(HG0401Msgs.Text_182);		// Add Partner
				menuPersonPartner.setEnabled(false);
				menuPersonAdd.add(menuPersonPartner);
				menuPersonFather = new JMenuItem(HG0401Msgs.Text_183);	// Add Father
				menuPersonFather.setEnabled(false);
				menuPersonAdd.add(menuPersonFather);
				menuPersonMother = new JMenuItem(HG0401Msgs.Text_184);	// Add Mother
				menuPersonMother.setEnabled(false);
				menuPersonAdd.add(menuPersonMother);
				menuPersonBrother = new JMenuItem(HG0401Msgs.Text_185);	// Add Brother
				menuPersonBrother.setEnabled(false);
				menuPersonAdd.add(menuPersonBrother);
				menuPersonSister = new JMenuItem(HG0401Msgs.Text_186);	// Add Sister
				menuPersonSister.setEnabled(false);
				menuPersonAdd.add(menuPersonSister);
				menuPersonSon = new JMenuItem(HG0401Msgs.Text_187);		// Add Son
				menuPersonSon.setEnabled(false);
				menuPersonAdd.add(menuPersonSon);
				menuPersonDau = new JMenuItem(HG0401Msgs.Text_188);		// Add Daughter
				menuPersonDau.setEnabled(false);
				menuPersonAdd.add(menuPersonDau);
			JMenuItem menuPersonManSty = new JMenuItem(HG0401Msgs.Text_35);	// Administer all Person Name Style
			menuPerson.add(menuPersonManSty);
			JMenuItem menuPersonFlag = new JMenuItem(HG0401Msgs.Text_36);	// Administer all Person Flags
			menuPersonFlag.setEnabled(false);
			menuPerson.add(menuPersonFlag);
			JMenuItem menuPersonNote = new JMenuItem(HG0401Msgs.Text_37);	// Administer all Person Notepads
			menuPersonNote.setEnabled(false);
			menuPerson.add(menuPersonNote);
			JMenuItem menuPersonAcc = new JMenuItem(HG0401Msgs.Text_38);	// Administer all Person Accents
			menuPersonAcc.setEnabled(false);
			menuPerson.add(menuPersonAcc);
	    	menuPerson.addSeparator();
			JMenuItem menuPersonRec = new JMenuItem(HG0401Msgs.Text_33);	// Recently Used
			menuPerson.add(menuPersonRec);
		// If language changes then the Menu gets re-created. This means the Recent Person list gets
		// deleted, so if the list contains entries we need to re-add them to the Person menu here.
			for (int i = 0; i < menuPerPID.size(); i++) {
				   if (menuPerSex.get(i).equals("M")) menuPerson.add(new JMenuItem(new clickPeople(menuPerName.get(i),	//$NON-NLS-1$
		   					new ImageIcon(getClass().getResource("/hre/images/male.png"))))); 	//$NON-NLS-1$
				   else {if (menuPerSex.get(i).equals("F")) menuPerson.add(new JMenuItem(new clickPeople(menuPerName.get(i), //$NON-NLS-1$
								new ImageIcon(getClass().getResource("/hre/images/female.png"))))); //$NON-NLS-1$
				   else menuPerson.add(new JMenuItem(new clickPeople(menuPerName.get(i), null)));
				   }
			}

		// Research Types MENU
		menuResearch = new JMenu(HG0401Msgs.Text_40);	// Research Types
		menuResearch.setVisible(false);		// set not visible until 1st project opened
		menuBar.add(menuResearch);
			JMenuItem menuRes1 = new JMenuItem(HG0401Msgs.Text_41);	// (to be added to HRE later)
			menuRes1.setEnabled(false);
			menuResearch.add(menuRes1);

		// Viewpoints MENU
		menuViewpoint = new JMenu(HG0401Msgs.Text_42);
		menuViewpoint.setVisible(false);	// set not visible until 1st project opened
		menuBar.add(menuViewpoint);
			JMenu menuVPPerson = new JMenu(HG0401Msgs.Text_43);			// Person
			menuViewpoint.add(menuVPPerson);
				JMenuItem menuVPPnext = new JMenuItem(HG0401Msgs.Text_145);				// Open next stored Viewpoint
				menuVPPerson.add(menuVPPnext);
				JMenuItem menuVPPopenAll = new JMenuItem(HG0401Msgs.Text_146);			// Open all Viewpoints
				menuVPPerson.add(menuVPPopenAll);
				JMenuItem menuVPPcloseAll = new JMenuItem(HG0401Msgs.Text_147);			// Close all Viewpoints
				menuVPPerson.add(menuVPPcloseAll);
			JMenu menuVPLocn = new JMenu(HG0401Msgs.Text_45);			// Location
			menuViewpoint.add(menuVPLocn);
				JMenuItem menuVPLnext = new JMenuItem(HG0401Msgs.Text_145);				// Open next stored Viewpoint
				menuVPLocn.add(menuVPLnext);
				JMenuItem menuVPLopenAll = new JMenuItem(HG0401Msgs.Text_146);			// Open all Viewpoints
				menuVPLocn.add(menuVPLopenAll);
				JMenuItem menuVPLcloseAll = new JMenuItem(HG0401Msgs.Text_147);			// Close all Viewpoints
				menuVPLocn.add(menuVPLcloseAll);
			JMenu menuVPEvent = new JMenu(HG0401Msgs.Text_44);			// Event
			menuViewpoint.add(menuVPEvent);
				JMenuItem menuVPEnext = new JMenuItem(HG0401Msgs.Text_145);				// Open next stored Viewpoint
				menuVPEvent.add(menuVPEnext);
				JMenuItem menuVPEopenAll = new JMenuItem(HG0401Msgs.Text_146);			// Open all Viewpoints
				menuVPEvent.add(menuVPEopenAll);
				JMenuItem menuVPEcloseAll = new JMenuItem(HG0401Msgs.Text_147);			// Close all Viewpoints
				menuVPEvent.add(menuVPEcloseAll);

		// Event/Tasks MENU
		JMenu menuET = new JMenu(HG0401Msgs.Text_46);
		menuET.setVisible(false);			// set not visible until 1st project opened
		menuBar.add(menuET);
			JMenuItem menuEvents = new JMenuItem(HG0401Msgs.Text_47);
			menuEvents.setEnabled(false);
			menuET.add(menuEvents);
			JMenuItem menuTasks = new JMenuItem(HG0401Msgs.Text_48);
			menuTasks.setEnabled(false);
			menuET.add(menuTasks);

		// Where/When MENU
		JMenu menuWhWh = new JMenu(HG0401Msgs.Text_49);				// Where/When
		menuWhWh.setVisible(false);			// set not visible until 1st project opened
		menuBar.add(menuWhWh);
			JMenu menuWhWhLoc = new JMenu(HG0401Msgs.Text_50);		// Locations
			menuWhWh.add(menuWhWhLoc);
				JMenuItem menuLocSelect = new JMenuItem(HG0401Msgs.Text_51);	// Select Locations by...
				menuWhWhLoc.add(menuLocSelect);
				JMenuItem menuManLoc = new JMenuItem(HG0401Msgs.Text_53);		// Manage Locations
				menuManLoc.setEnabled(false);
				menuWhWhLoc.add(menuManLoc);
				JMenuItem menuManLocStyle = new JMenuItem(HG0401Msgs.Text_57);	// Administer all Location Name Styles
				menuManLocStyle.setEnabled(false);
				menuWhWhLoc.add(menuManLocStyle);
				menuWhWhLoc.addSeparator();
				JMenuItem menuLocRecent = new JMenuItem(HG0401Msgs.Text_52);	// Recently Used
				menuLocRecent.setEnabled(false);
				menuWhWhLoc.add(menuLocRecent);
			JMenuItem menuWhWhHDate = new JMenuItem(HG0401Msgs.Text_63);		// Historical Dates
			menuWhWh.add(menuWhWhHDate);
			menuWhWhHDate.setEnabled(false);

		// Evidence MENU
		JMenu menuEvidence = new JMenu(HG0401Msgs.Text_65);		// Evidence
		menuEvidence.setVisible(false);			// set not visible until 1st project opened
		menuBar.add(menuEvidence);
			JMenuItem menuSources = new JMenuItem(HG0401Msgs.Text_66);		// Manage Sources
			menuSources.setEnabled(false);
			menuEvidence.add(menuSources);
			JMenuItem menuSourceTypes = new JMenuItem(HG0401Msgs.Text_67);	// Manage Source Types
			menuSourceTypes.setEnabled(false);
			menuEvidence.add(menuSourceTypes);
			JMenuItem menuSourceElmnts = new JMenuItem(HG0401Msgs.Text_64);	// Manage Source Elements
			menuSourceElmnts.setEnabled(false);
			menuEvidence.add(menuSourceElmnts);
			JMenuItem menuRepository = new JMenuItem(HG0401Msgs.Text_68);	// Manage Repositories
			menuRepository.setEnabled(false);
			menuEvidence.add(menuRepository);

		// Reports MENU
		menuReports = new JMenu(HG0401Msgs.Text_72);
		menuBar.add(menuReports);
			JMenuItem menuAncDes = new JMenuItem(HG0401Msgs.Text_126);
			menuAncDes.setEnabled(false);			//set disabled until a project opens
			menuReports.add(menuAncDes);
			JMenuItem menuRepList = new JMenuItem(HG0401Msgs.Text_73);
			menuRepList.setEnabled(false);			//set disabled until a project opens
			menuReports.add(menuRepList);
			JMenuItem menuRepLog = new JMenuItem(HG0401Msgs.Text_74);
			menuReports.add(menuRepLog);

		// Tools MENU
		menuTools = new JMenu(HG0401Msgs.Text_75);
		menuBar.add(menuTools);
			JMenu menuToolSet = new JMenu(HG0401Msgs.Text_76);
			menuToolSet.setIcon(new ImageIcon(getClass().getResource("/hre/images/setting_BW_16.png"))); //$NON-NLS-1$
			menuTools.add(menuToolSet);
				JMenuItem menuToolSetUser = new JMenuItem(HG0401Msgs.Text_78);
				menuToolSet.add(menuToolSetUser);
			JMenu menuToolImport = new JMenu(HG0401Msgs.Text_101);
			menuToolImport.setIcon(new ImageIcon(getClass().getResource("/hre/images/import_16.png"))); //$NON-NLS-1$
			menuTools.add(menuToolImport);
				JMenuItem menuToolImportTMG = new JMenuItem(HG0401Msgs.Text_103, new ImageIcon(getClass().getResource("/hre/images/tmg9_16.png"))); //$NON-NLS-1$
				menuToolImport.add(menuToolImportTMG);
			JMenu menuToolExport = new JMenu(HG0401Msgs.Text_104);
			menuToolExport.setIcon(new ImageIcon(getClass().getResource("/hre/images/export_16.png"))); //$NON-NLS-1$
			menuTools.add(menuToolExport);
				JMenuItem menuToolExportGED = new JMenuItem("GEDCOM 7");  //$NON-NLS-1$
				menuToolExportGED.setEnabled(false);
				menuToolExport.add(menuToolExportGED);
			JMenuItem menuToolAdminUsers = new JMenuItem(HG0401Msgs.Text_108);
			menuTools.add(menuToolAdminUsers);
			JMenuItem menuToolDBmaint = new JMenuItem(HG0401Msgs.Text_1);
			menuTools.add(menuToolDBmaint);

	    // HELP MENU
		JMenu menuHelp = new JMenu(HG0401Msgs.Text_109);
		menuBar.add(menuHelp);
			JMenuItem menuHelpContent = new JMenuItem(HG0401Msgs.Text_110,
					new ImageIcon(getClass().getResource("/hre/images/help_BW_16.png"))); //$NON-NLS-1$
			menuHelp.add(menuHelpContent);
			JMenuItem menuHREweb = new JMenuItem(HG0401Msgs.Text_112,
					new ImageIcon(getClass().getResource("/hre/images/www_BW_16.png"))); //$NON-NLS-1$
			menuHelp.add(menuHREweb);
			JMenuItem menuAboutHRE = new JMenuItem(HG0401Msgs.Text_114,
					new ImageIcon(getClass().getResource("/hre/images/helpabout_BW_16.png"))); //$NON-NLS-1$
			menuHelp.add(menuAboutHRE);

		// If running on a Mac, add glue and Project P icon at right of menu
		if (HGlobal.osType.contains("mac"))	{ //$NON-NLS-1$
			menuBar.add(Box.createGlue());
			menuBar.add(menuStatus);
			}

/******************************************
 * SETUP ALL MENU LISTENERS AND ACTIONS
 ******************************************/
		// LISTENER FOR CLICKING 'X" ON MAIN SCREEN
	    // Every time the clickProject method is entered it also creates another
	    // windowClosing event (for some unknown reason). So we need to execute
	    // all of these unneeded events and only action the last one (when the
	    // exitCount has decreased to 0)
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
		    	if (exitCount == 0) {
					 exitCount = countMenuOpenProjects;		// reset for next time X may be used
					 HGlobal.closeType = "X";				// flag this as click of 'X' on screen	 //$NON-NLS-1$
					 menuExitHRE.doClick();					// manually trigger ExitHRE code on last event
				} else exitCount = exitCount - 1;
		    }
		});

		// LISTENER FOR TRACKING MOVE OR RESIZE OF MAIN MENU
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(ComponentEvent move)     {
	        	 HGlobal.mainX = move.getComponent().getX();
	        	 HGlobal.mainY = move.getComponent().getY();
	        	 }
			@Override
			public void componentResized(ComponentEvent resz) {
	        	 HGlobal.mainH = resz.getComponent().getHeight();
	        	 HGlobal.mainW = resz.getComponent().getWidth();
	        	 }
		});

/****************************************************
 * LISTENERS FOR PROJECT MENU ITEMS
 ***************************************************/
		// Hover-over acts as doClick
		menuProj.addMouseListener(new MouseAdapter() {
	        @Override
			public void mouseEntered(MouseEvent evt) {
				menuProj.doClick();
	        }
	    });

		// Project -> Open Project
		menuProjOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			// Do not allow more than 3 open projects
	    	if (HGlobal.numOpenProjects == 3)
	    		{JOptionPane.showMessageDialog(HG0401HREMain.mainPane, HG0401Msgs.Text_133, HG0401Msgs.Text_134,
	    				JOptionPane.INFORMATION_MESSAGE);
	    		return;
	    		}
			HG0402ProjectOpen openScreen = new HG0402ProjectOpen((HBProjectHandler)pointBusinessLayer[0]);
			openScreen.setModalityType(ModalityType.APPLICATION_MODAL);
			Point xymainPane = mainPane.getLocationOnScreen();
			openScreen.setLocation(xymainPane.x + 50, xymainPane.y + 50);
			openScreen.setVisible(true);
			longMenu = true;
			mainMenuSetup();
			revalidate();
			}
		});

		// Project -> New Project
		menuProjNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			HG0404ProjectNew newScreen = new HG0404ProjectNew((HBProjectHandler)pointBusinessLayer[0]);
			newScreen.setModalityType(ModalityType.APPLICATION_MODAL);
			Point xymainPane = mainPane.getLocationOnScreen();
			newScreen.setLocation(xymainPane.x + 50, xymainPane.y + 50);
			newScreen.setVisible(true);
			menuProjBackup.setEnabled(true);		// enable Project menu items once a project defined
			menuProjClose.setEnabled(true);
			menuProjCopy.setEnabled(true);
			menuProjRename.setEnabled(true);
			menuProjSummry.setEnabled(true);
			longMenu = true;
			mainMenuSetup();
			revalidate();
			}
		});

		// Project -> Refine Remote Project
		menuProjRmt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			HG0418ProjectRemote rmtScreen = new HG0418ProjectRemote((HBProjectHandler)pointBusinessLayer[0]);
			rmtScreen.setModalityType(ModalityType.APPLICATION_MODAL);
			Point xymainPane = mainPane.getLocationOnScreen();
			rmtScreen.setLocation(xymainPane.x + 50, xymainPane.y + 50);
			rmtScreen.setVisible(true);
			}
		});

 		// Project -> Backup Project
		menuProjBackup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			HG0405ProjectBackup backScreen=new HG0405ProjectBackup((HBProjectHandler)pointBusinessLayer[0]);
			backScreen.setModalityType(ModalityType.APPLICATION_MODAL);
			Point xymainPane = mainPane.getLocationOnScreen();
			backScreen.setLocation(xymainPane.x + 50, xymainPane.y + 50);
			backScreen.setVisible(true);
			}
		});

		// Project -> Restore Project
		menuProjRestore.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			HG0406ProjectRestore restScreen=new HG0406ProjectRestore((HBProjectHandler)pointBusinessLayer[0]);
			restScreen.setModalityType(ModalityType.APPLICATION_MODAL);
			Point xymainPane = mainPane.getLocationOnScreen();
			restScreen.setLocation(xymainPane.x + 50, xymainPane.y + 50);
			restScreen.setVisible(true);
			menuProjBackup.setEnabled(true);		// enable Project menu items once a project restored
			menuProjClose.setEnabled(true);
			menuProjCopy.setEnabled(true);
			menuProjRename.setEnabled(true);
			menuProjSummry.setEnabled(true);
			longMenu = true;
			mainMenuSetup();
			revalidate();
			}
		});

		// Project -> Close Project
		menuProjClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			// set closeType if not already set by windowClosing or ExitHRE
			if (HGlobal.closeType.equals("N"))	HGlobal.closeType = "C";	 //$NON-NLS-1$ //$NON-NLS-2$
			HG0407ProjectClose closeScreen = new HG0407ProjectClose((HBProjectHandler)pointBusinessLayer[0]);
			closeScreen.setModalityType(ModalityType.APPLICATION_MODAL);
			Point xymainPane = mainPane.getLocationOnScreen();
			closeScreen.setLocation(xymainPane.x + 50, xymainPane.y + 50);

			// We may have got here by one of 3 possible paths:
			//     1. clicking 'X' -> ExitHRE -> menuProjClose (in which case closeType = "X")
			//     2. clicking ExitHRE -> menuProjClose (in which case closeType = "E")
			//     3. clicking menuProjClose (in which case closeType = "C")
			// If closeType = "E" or "C" we always want to show the HG0407ProjectClose screen
			// If closeType = "X" we only want to show this screen if numOpenProjects is > 1; otherwise
			//  	we want to execute the project closing 'silently' (no screen shown)

			if (HGlobal.closeType.equals("E") | HGlobal.closeType.equals("C")  //$NON-NLS-1$ //$NON-NLS-2$
				| (HGlobal.closeType.equals("X") & HGlobal.numOpenProjects > 1)) closeScreen.setVisible(true); //$NON-NLS-1$

			// If we've closed all projects (but kept HRE running) then many menu
			// items should be disabled as they need an open project - so do that now
			// (NB: keep these actions synchronised with those in the Project Open code)
			if (HGlobal.numOpenProjects == 0) {
						menuProjClose.setEnabled(false);
						menuProjSummry.setEnabled(false);
					// All People menu items
						menuPersonSel.setEnabled(false);
						menuPersonRec.setEnabled(false);
						menuPersonMan.setEnabled(false);
						menuPersonAdd.setEnabled(false);
						menuPersonManSty.setEnabled(false);
						menuPersonFlag.setEnabled(false);
						menuPersonNote.setEnabled(false);
						menuPersonAcc.setEnabled(false);
						menuPersonPartner.setEnabled(false);
						menuPersonFather.setEnabled(false);
						menuPersonMother.setEnabled(false);
						menuPersonBrother.setEnabled(false);
						menuPersonSister.setEnabled(false);
						menuPersonSon.setEnabled(false);
						menuPersonDau.setEnabled(false);
					// All Viewpoint menu items
						menuVPPerson.setEnabled(false);
						menuVPLocn.setEnabled(false);
						menuVPEvent.setEnabled(false);
					// All Event menu items
						menuEvents.setEnabled(false);
					// All Evidence menu items
						menuSources.setEnabled(false);
						menuSourceTypes.setEnabled(false);
						menuSourceElmnts.setEnabled(false);
						menuRepository.setEnabled(false);
					// All Where-When, Location items
						menuLocSelect.setEnabled(false);
						menuLocRecent.setEnabled(false);
						menuManLoc.setEnabled(false);
						menuManLocStyle.setEnabled(false);
					// Report items
						menuAncDes.setEnabled(false);
						}
			}
		});

		// Project -> Copy Project
		menuProjCopy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			HG0408ProjectCopyAs copyScreen=new HG0408ProjectCopyAs((HBProjectHandler)pointBusinessLayer[0]);
			copyScreen.setModalityType(ModalityType.APPLICATION_MODAL);
			Point xymainPane = mainPane.getLocationOnScreen();
			copyScreen.setLocation(xymainPane.x + 50, xymainPane.y + 50);
			copyScreen.setVisible(true);
			}
		});

		// Project -> Rename Project
		menuProjRename.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			HG0409ProjectRename renScreen=new HG0409ProjectRename((HBProjectHandler)pointBusinessLayer[0]);
			renScreen.setModalityType(ModalityType.APPLICATION_MODAL);
			Point xymainPane = mainPane.getLocationOnScreen();
			renScreen.setLocation(xymainPane.x + 50, xymainPane.y + 50);
			renScreen.setVisible(true);
			}
		});

		// Project -> Delete Project
		menuProjDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			HG0410ProjectDelete delScreen = new HG0410ProjectDelete((HBProjectHandler)pointBusinessLayer[0]);
			delScreen.setModalityType(ModalityType.APPLICATION_MODAL);
			Point xymainPane = mainPane.getLocationOnScreen();
			delScreen.setLocation(xymainPane.x +20, xymainPane.y +20);
			delScreen.setVisible(true);
			}
		});

		// Project -> Project Summary
		menuProjSummry.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			// If no projects open, return
			if (HGlobal.numOpenProjects == 0) return;
			// Get the current active project name and get it's Summary data
			HBProjectOpenData pointOpenProject = getSelectedOpenProject();
			String[][] summaryData = null;
			try {
				summaryData = ((HBProjectHandler) pointBusinessLayer[0]).getSummaryUserProjectAction(pointOpenProject.getProjectName());
			} catch (HBException hbe) {
				JOptionPane.showMessageDialog(null, HG0401Msgs.Text_8
						+  hbe.getMessage(), HG0401Msgs.Text_10, JOptionPane.ERROR_MESSAGE);
			}
			// Display the data
			HG0414ProjectSummary summScreen = new HG0414ProjectSummary(summaryData);
			summScreen.setModalityType(ModalityType.APPLICATION_MODAL);
			Point xymainPane = mainPane.getLocationOnScreen();
			summScreen.setLocation(xymainPane.x + 20, xymainPane.y + 20);
			summScreen.setVisible(true);
			}
		});

		// Project -> Exit (or from 'X' close of Main screen)
		menuExitHRE.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg) {
		// Close all open projects when exiting
			// set closeType if not already set by windowClosing event
			if (HGlobal.closeType.equals("N"))	HGlobal.closeType = "E";   //$NON-NLS-1$ //$NON-NLS-2$
			if (HGlobal.numOpenProjects >= 1)  menuProjClose.doClick();
			if (HGlobal.numOpenProjects == 0) {
					if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: exiting HRE");} //$NON-NLS-1$
					HB0744UserAUX.writeUserAUXfile(); // do final output of UserAux (captures final position of main Menu screen)
			// Stop tcp server to release port 9092
					HBToolHandler pointToolHandler = (HBToolHandler) pointBusinessLayer[8];
					pointToolHandler.stopTCPserver();
					System.exit(0);					// and exit
				}
			}
		});

		// Ensure menu items are enabled in the longMenu setting case
		// Also ensures longMenu stays complete after either a GUILanguage reset,
		// or if the number of Open Projects had gone to 0, which disables menu items
		// (NB: these need to be kept synchronised with the Project Close disable list)
		if (longMenu) {
			// Project menu items
			menuProjBackup.setEnabled(true);
			menuProjClose.setEnabled(true);
			menuProjCopy.setEnabled(true);
			menuProjRename.setEnabled(true);
			menuProjSummry.setEnabled(true);
		// People menu items
			menuPerson.setVisible(true);
			menuPersonSel.setEnabled(true);
			menuPersonAdd.setEnabled(true);
			menuPersonManSty.setEnabled(true);
			menuPersonMan.setEnabled(true);
			menuPersonFlag.setEnabled(true);
		// Now test if ManagePerson is running - if so, the addPerson sub-items can be enabled
			HBProjectOpenData pointOpenProject = getSelectedOpenProject();
		// If open project ??
			if (pointOpenProject != null) {
				int index = pointOpenProject.pointGuiData.getStoreIndex("50600"); //$NON-NLS-1$
				if  (pointOpenProject.getWindowPointer(index) != null) {
					menuPersonPartner.setEnabled(true);
					menuPersonFather.setEnabled(true);
					menuPersonMother.setEnabled(true);
					menuPersonBrother.setEnabled(true);
					menuPersonSister.setEnabled(true);
					menuPersonSon.setEnabled(true);
					menuPersonDau.setEnabled(true);
				}
			}
			// Viewpoint menu items
			menuViewpoint.setVisible(true);
			menuVPPerson.setEnabled(true);
			menuVPLocn.setEnabled(true);
			menuVPEvent.setEnabled(true);
			// Event menu items
			menuEvents.setEnabled(true);
			// Evidence menu items
			menuSources.setEnabled(true);
			menuSourceTypes.setEnabled(true);
			menuSourceElmnts.setEnabled(true);
			menuRepository.setEnabled(true);
			// Where/When menu items
			menuWhWh.setVisible(true);
			menuLocSelect.setEnabled(true);
			menuManLoc.setEnabled(true);
			menuManLocStyle.setEnabled(true);
			// Report menu items
			menuReports.setVisible(true);
			//menuRepList.setEnabled(true);
			menuAncDes.setEnabled(true);
			// Other menus
			menuResearch.setVisible(true);
			menuET.setVisible(true);
			menuEvidence.setVisible(true);
			}

/****************************************************
 * LISTENERS FOR PERSON MENU ITEMS
 ***************************************************/
		// Hover-over acts as doClick
		menuPerson.addMouseListener(new MouseAdapter() {
	        @Override
			public void mouseEntered(MouseEvent evt) {
				menuPerson.doClick();
	        }
	    });

		// Person -> Select Person by...
		menuPersonSel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			// Find the index for Person Select
                HBProjectOpenData pointOpenProject = getSelectedOpenProject();
				int index = pointOpenProject.pointGuiData.getStoreIndex("50700"); //$NON-NLS-1$

			// If no PersonSelect exists, then create it, in Front of other frames
				if  (pointOpenProject.getWindowPointer(index) == null) {
					pointOpenProject.getPersonHandler().initiatePersonSelect(pointOpenProject, "F"); //$NON-NLS-1$
				} else {
			//  else if Frame minimized, make it normal, or bring to front
					JInternalFrame pointIntFrame = (JInternalFrame)pointOpenProject.getWindowPointer(index);
			  		if(pointIntFrame.isIcon())  {
				  		try {pointIntFrame.setIcon(false);
				  			} catch (PropertyVetoException pve) {
				  				pve.printStackTrace();
				  			}
			  		}
					if (pointIntFrame.isShowing())
						pointIntFrame.toFront();
				}
			}
		});

		// Person -> Manage Person
		menuPersonMan.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
                HBProjectOpenData pointOpenProject = getSelectedOpenProject();
				int index = pointOpenProject.pointGuiData.getStoreIndex("50600"); //$NON-NLS-1$
			// If no Manage Person exists, then create it
				if (pointOpenProject.getWindowPointer(index) == null) {
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					pointOpenProject.getPersonHandler().activateManagePerson(pointOpenProject);
					return;
				}
				//  else if Frame minimized, make it normal, or bring to front
						JInternalFrame pointIntFrame = (JInternalFrame)pointOpenProject.getWindowPointer(index);
				  		if (pointIntFrame.isIcon())  {
					  		try {pointIntFrame.setIcon(false);
					  			} catch (PropertyVetoException pve) {
					  				pve.printStackTrace();
					  			}
				  		}
				  		else if (pointIntFrame.isShowing())
							pointIntFrame.toFront();
			}
		});

		// Person -> Add new Unrelated person
		menuPersonNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int sexIndex = 0; //unknown sex
	            HBProjectOpenData pointOpenProject = getSelectedOpenProject();
	            HBPersonHandler pointPersonHandler = pointOpenProject.getPersonHandler();
	            HG0505AddPerson addScreen = pointPersonHandler.activateAddPerson(pointOpenProject, sexIndex);
				addScreen.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
				Point xymainPane = mainPane.getLocationOnScreen();
				addScreen.setLocation(xymainPane.x + 50, xymainPane.y + 50);
				addScreen.setAlwaysOnTop(true);
				addScreen.setVisible(true);
			}
		});

		// Person -> Add Partner of person displayed in ManagePerson
		menuPersonPartner.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int sexIndex = 0;
	            HBProjectOpenData pointOpenProject = getSelectedOpenProject();
	            HBPersonHandler pointPersonHandler = pointOpenProject.getPersonHandler();
	            HG0505AddPerson addScreen = pointPersonHandler.
	            		activateAddPersonPartner(pointOpenProject, sexIndex);
				addScreen.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
				Point xymainPane = mainPane.getLocationOnScreen();
				addScreen.setLocation(xymainPane.x + 50, xymainPane.y + 50);
				addScreen.setAlwaysOnTop(true);
				addScreen.setVisible(true);
			}
		});

		// Person -> Add Father of person displayed in ManagePerson
		menuPersonFather.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int sexIndex = 2;	// male
	            HBProjectOpenData pointOpenProject = getSelectedOpenProject();
	            HBPersonHandler pointPersonHandler = pointOpenProject.getPersonHandler();
				HG0505AddPerson addScreen = pointPersonHandler.
						activateAddPersonParent(pointOpenProject, sexIndex);
				addScreen.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
				Point xymainPane = mainPane.getLocationOnScreen();
				addScreen.setLocation(xymainPane.x + 50, xymainPane.y + 50);
				addScreen.setAlwaysOnTop(true);
				addScreen.setVisible(true);
			}
		});

		// Person -> Add Mother of person displayed in ManagePerson
		menuPersonMother.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int sexIndex = 1;		// female
	            HBProjectOpenData pointOpenProject = getSelectedOpenProject();
	            HBPersonHandler pointPersonHandler = pointOpenProject.getPersonHandler();
				HG0505AddPerson addScreen = pointPersonHandler.
						activateAddPersonParent(pointOpenProject, sexIndex);
				addScreen.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
				Point xymainPane = mainPane.getLocationOnScreen();
				addScreen.setLocation(xymainPane.x + 50, xymainPane.y + 50);
				addScreen.setAlwaysOnTop(true);
				addScreen.setVisible(true);
			}
		});

		// Person -> Add Brother of person displayed in ManagePerson
		menuPersonBrother.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int sexIndex = 2;	// male
	            HBProjectOpenData pointOpenProject = getSelectedOpenProject();
	            HBPersonHandler pointPersonHandler = pointOpenProject.getPersonHandler();
				HG0505AddPerson addScreen = pointPersonHandler.
						activateAddPersonSibling(pointOpenProject, sexIndex);
				addScreen.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
				Point xymainPane = mainPane.getLocationOnScreen();
				addScreen.setLocation(xymainPane.x + 50, xymainPane.y + 50);
				addScreen.setAlwaysOnTop(true);
				addScreen.setVisible(true);
			}
		});

		// Person -> Add Sister of person displayed in ManagePerson
		menuPersonSister.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int sexIndex = 1;	// female
	            HBProjectOpenData pointOpenProject = getSelectedOpenProject();
	            HBPersonHandler pointPersonHandler = pointOpenProject.getPersonHandler();
				HG0505AddPerson addScreen = pointPersonHandler.
						activateAddPersonSibling(pointOpenProject, sexIndex);
				addScreen.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
				Point xymainPane = mainPane.getLocationOnScreen();
				addScreen.setLocation(xymainPane.x + 50, xymainPane.y + 50);
				addScreen.setAlwaysOnTop(true);
				addScreen.setVisible(true);
			}
		});

		// Person -> Add Son of person displayed in ManagePerson
		menuPersonSon.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int sexIndex = 2;	// male
	            HBProjectOpenData pointOpenProject = getSelectedOpenProject();
	            HBPersonHandler pointPersonHandler = pointOpenProject.getPersonHandler();
				HG0505AddPerson addScreen = pointPersonHandler.
						activateAddPersonChild(pointOpenProject, sexIndex);
				addScreen.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
				Point xymainPane = mainPane.getLocationOnScreen();
				addScreen.setLocation(xymainPane.x + 50, xymainPane.y + 50);
				addScreen.setAlwaysOnTop(true);
				addScreen.setVisible(true);
			}
		});

		// Person -> Add Daughter of person displayed in ManagePerson
		menuPersonDau.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int sexIndex = 1;	// female
	            HBProjectOpenData pointOpenProject = getSelectedOpenProject();
	            HBPersonHandler pointPersonHandler = pointOpenProject.getPersonHandler();
				HG0505AddPerson addScreen = pointPersonHandler.
						activateAddPersonChild(pointOpenProject,sexIndex);
				addScreen.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
				Point xymainPane = mainPane.getLocationOnScreen();
				addScreen.setLocation(xymainPane.x + 50, xymainPane.y + 50);
				addScreen.setAlwaysOnTop(true);
				addScreen.setVisible(true);
			}
		});

		// Person -> Manage Person Name Styles
		menuPersonManSty.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg) {
				HBProjectOpenData pointOpenProject = getSelectedOpenProject();
				allWins = null;
				// Check if this JDialog already open - if so, return
				allWins = Window.getWindows();
				for(Window w: allWins)
						if(w.getName().equals("HG0525") && w.isVisible()) return;	//$NON-NLS-1$
				// Otherwise, open HG0525ManagePersonNameStyles
				HG0525ManagePersonNameStyles persStyleScreen =
						pointOpenProject.getPersonHandler().activatePersonNameStyle(pointOpenProject);
				persStyleScreen.setName("HG0525");	//$NON-NLS-1$
				persStyleScreen.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
				Point xyPers = menuPerson.getLocationOnScreen();
				persStyleScreen.setLocation(xyPers.x, xyPers.y + 80);
				persStyleScreen.setAlwaysOnTop(true);
				persStyleScreen.setVisible(true);
			}
		});

		// Person -> Manage Person Flags
		menuPersonFlag.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg) {
				// Initiate Flagmanager
				HBProjectOpenData pointOpenProject = getSelectedOpenProject();
				HG0512FlagManager flagScreen = null;
				try {
					flagScreen = pointOpenProject.getPersonHandler().
							intitiateFlagManager(pointOpenProject);
				} catch (HBException hbe) {
					System.out.println(" Activate Flag Manager: " + hbe.getMessage());	//$NON-NLS-1$
					hbe.printStackTrace();
				}
				flagScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xyFlag = menuPerson.getLocationOnScreen();
				flagScreen.setLocation(xyFlag.x, xyFlag.y + 50);
				flagScreen.setVisible(true);
			}
		});

/****************************************************
 * LISTENERS FOR VIEWPOINT ITEMS
 ***************************************************/
		// Hover-over acts as doClick
		menuViewpoint.addMouseListener(new MouseAdapter() {
	        @Override
			public void mouseEntered(MouseEvent evt) {
				menuViewpoint.doClick();
	        }
	    });

		// Viewpoint -> Open next stored Person Viewpoint
		menuVPPnext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg) {
			// Open the Person ViewPoint for the selected project
				int errorCode = getSelectedOpenProject().getViewPointHandler().
						menuActionPersonVP(getSelectedOpenProject(), true);
				if (errorCode == 1)
					JOptionPane.showMessageDialog(null, HG0401Msgs.Text_152,				// No more initiated Person Viewpoints available for use
								  HG0401Msgs.Text_153,JOptionPane.INFORMATION_MESSAGE);		// Open Person Viewpoints
				if (errorCode == 2)
					JOptionPane.showMessageDialog(null, HG0401Msgs.Text_154 + maxPersonVPs + HG0401Msgs.Text_155,	// All   Person Viewpoints are open
								  HG0401Msgs.Text_153,JOptionPane.INFORMATION_MESSAGE);		// Open Person Viewpoints
			}
		});

		// Viewpoint -> Open all stored Person Viewpoints
		menuVPPopenAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg) {
			// Open the ViewPoints for the selected project
				int errorCode = getSelectedOpenProject().getViewPointHandler().
						menuActionPersonVP(getSelectedOpenProject(), false);
				if (errorCode > 0) {
					JOptionPane.showMessageDialog(null, HG0401Msgs.Text_139 + errorCode, // Open all Person Viewpoints, errorCode =
							HG0401Msgs.Text_146,JOptionPane.INFORMATION_MESSAGE);		 // Open All Viewpoints
				}
			}
		});

		// Viewpoint -> Close all open Person Viewpoints
		menuVPPcloseAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg) {
				int errorCode = getSelectedOpenProject().getViewPointHandler().
						closePersonVPs(getSelectedOpenProject());
				if (errorCode > 0)
					JOptionPane.showMessageDialog(null, HG0401Msgs.Text_140 + errorCode, //Close all Person Viewpoints, errorCode =
							HG0401Msgs.Text_147,JOptionPane.INFORMATION_MESSAGE);		 // Close All Viewpoints
			}
		});

		// Viewpoint -> Open next stored Location Viewpoint
		menuVPLnext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg) {
				// Open the Locn ViewPoint for the selected project
				int errorCode = getSelectedOpenProject().getViewPointHandler().
						menuActionLocationVP(getSelectedOpenProject(),true);
				if (errorCode == 1)
					JOptionPane.showMessageDialog(null, HG0401Msgs.Text_156,		// No more initiated Location Viewpoints available for use
						  HG0401Msgs.Text_157,JOptionPane.INFORMATION_MESSAGE);		// Open Location Viewpoints
				if (errorCode == 2)
					JOptionPane.showMessageDialog(null, HG0401Msgs.Text_158 + maxLocationVPs + HG0401Msgs.Text_159,	// All    Location Viewpoints are open
							  HG0401Msgs.Text_157,JOptionPane.INFORMATION_MESSAGE);		// Open Location Viewpoints
			}
		});

		// Viewpoint -> Open all stored Location Viewpoints
		menuVPLopenAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg) {
			// Open the ViewLocations for the selected project
				int errorCode = getSelectedOpenProject().getViewPointHandler().
						menuActionLocationVP(getSelectedOpenProject(), false);
				if (errorCode > 0) {
					JOptionPane.showMessageDialog(null, HG0401Msgs.Text_141 + errorCode, // Open all Location Viewpoints, errorCode =
							HG0401Msgs.Text_146,JOptionPane.INFORMATION_MESSAGE);		 // Open All Viewpoints
				}
			}
		});

		// Viewpoint -> Close all open Location Viewpoints
		menuVPLcloseAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg) {
				int errorCode = getSelectedOpenProject().getViewPointHandler().
						closeLocationVPs(getSelectedOpenProject());
				if (errorCode > 0)
					JOptionPane.showMessageDialog(null, HG0401Msgs.Text_142 + errorCode, // Close all Location Viewpoints, errorCode =
							HG0401Msgs.Text_147,JOptionPane.INFORMATION_MESSAGE);		 // Close All Viewpoints
			}
		});

		// Viewpoint -> Open next stored Event Viewpoint
		menuVPEnext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg) {
			// Open the ViewEvent for the selected project
				int errorCode = getSelectedOpenProject().getViewPointHandler().
						menuActionEventVP(getSelectedOpenProject(), true);
				if (errorCode > 0) {
					JOptionPane.showMessageDialog(null, HG0401Msgs.Text_150 + errorCode, 	// Opening single Event Viewpoint, errorCode =
							HG0401Msgs.Text_151,JOptionPane.INFORMATION_MESSAGE); 			// Opening Event Viewpoint
				}
			}
		});

		// Viewpoint -> Open all stored Event Viewpoints
		menuVPEopenAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg) {
			// Open the ViewEvent for the selected project
				int errorCode = getSelectedOpenProject().getViewPointHandler().
						menuActionEventVP(getSelectedOpenProject(), false);
				if (errorCode > 0) {
					JOptionPane.showMessageDialog(null, HG0401Msgs.Text_143 + errorCode, // Open all Event Viewpoints, errorCode =
							HG0401Msgs.Text_146,JOptionPane.INFORMATION_MESSAGE);		 // Open All Viewpoints
				}
			}
		});

		// Viewpoint -> Close all open Event Viewpoints
		menuVPEcloseAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg) {
				int errorCode = getSelectedOpenProject().getViewPointHandler().
						closeEventVPs(getSelectedOpenProject());
				if (errorCode > 0)
					JOptionPane.showMessageDialog(null, HG0401Msgs.Text_144 + errorCode, // Close all Event Viewpoints, errorCode =
							HG0401Msgs.Text_147,JOptionPane.INFORMATION_MESSAGE);		 // Close All Viewpoints
			}
		});

/****************************************************
 * LISTENERS FOR EVENT MENU ITEMS
 ***************************************************/
		// Hover-over acts as doClick
		menuET.addMouseListener(new MouseAdapter() {
	        @Override
			public void mouseEntered(MouseEvent evt) {
				menuET.doClick();
	        }
	     });

		// Events - Manage Events
		menuEvents.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HBProjectOpenData pointOpenProject = getSelectedOpenProject();
				HBWhereWhenHandler pointHBWhereWhenHandler = pointOpenProject.getWhereWhenHandler();
				HG0552ManageEvent eventScreen
						= pointHBWhereWhenHandler.activateAddSelectedEvent(pointOpenProject, 0);
				eventScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xyEvent = menuET.getLocationOnScreen();
				eventScreen.setLocation(xyEvent.x, xyEvent.y + 30);
				eventScreen.setVisible(true);
			}
		});

/****************************************************
 * LISTENERS FOR WHERE/WHEN MENU ITEMS
 ***************************************************/
		// Hover-over acts as doClick
		menuWhWh.addMouseListener(new MouseAdapter() {
	        @Override
			public void mouseEntered(MouseEvent evt) {
				menuWhWh.doClick();
	        }
	     });

		// Location -> Select Location by...
		menuLocSelect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Find the index for Location Select
                HBProjectOpenData pointOpenProject = getSelectedOpenProject();
				int index = pointOpenProject.pointGuiData.getStoreIndex("50750");	//$NON-NLS-1$
				// If no LocationSelect exists, then create it
				if  (pointOpenProject.getWindowPointer(index) == null) {
					pointOpenProject.getWhereWhenHandler().initiateLocationSelect(pointOpenProject);
				} else {
					//  else if Frame minimized, make it normal, or bring to front
					JInternalFrame pointIntFrame = (JInternalFrame)pointOpenProject.getWindowPointer(index) ;
			  		if(pointIntFrame.isIcon())  { 			//
				  		try {pointIntFrame.setIcon(false);
				  			} catch (PropertyVetoException pve) {
				  				pve.printStackTrace();
				  			}
			  		}
					if (pointIntFrame.isShowing())
						pointIntFrame.toFront();
				}
			}
		});

		// Location -> Manage Location
		menuManLoc.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
                HBProjectOpenData pointOpenProject = getSelectedOpenProject();
				int index = pointOpenProject.pointGuiData.getStoreIndex("50800"); //$NON-NLS-1$

			// If no Manage Location exists, then create it
			// ************** CODE NEEDED TO MAKE THIS ALWAYS ON TOP LIKE HG0526 *********
				if  (pointOpenProject.getWindowPointer(index) == null) {
					pointOpenProject.getWhereWhenHandler().activateManageLocation(pointOpenProject);
				}
			}
		});

		// Location -> Manage Location Name Styles
		menuManLocStyle.addActionListener(new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg) {
			HBProjectOpenData pointOpenProject = getSelectedOpenProject();
			allWins = null;
			// Check if this JDialog already open - if so, return
			allWins = Window.getWindows();
			for(Window w: allWins)
					if(w.getName().equals("HG0526") && w.isVisible()) return;	//$NON-NLS-1$
			// Otherwise, open HG0526ManageLocationNameStyles
			HG0526ManageLocationNameStyles locnStyleScreen =
					pointOpenProject.getWhereWhenHandler().
					activateLocationNameStyle(pointOpenProject);
			locnStyleScreen.setName("HG0526");	//$NON-NLS-1$
			locnStyleScreen.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
			Point xyWhWh = menuWhWh.getLocationOnScreen();
			locnStyleScreen.setLocation(xyWhWh.x - 70, xyWhWh.y + 80);
			locnStyleScreen.setAlwaysOnTop(true);
			locnStyleScreen.setVisible(true);
			}
		});

/****************************************************
 * LISTENERS FOR EVIDENCE MENU ITEMS
 ***************************************************/
		// Hover-over acts as doClick
		menuEvidence.addMouseListener(new MouseAdapter() {
	        @Override
			public void mouseEntered(MouseEvent evt) {
				menuEvidence.doClick();
	        }
	     });

		// Evidence - Manage Sources
		menuSources.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HBProjectOpenData pointOpenProject = getSelectedOpenProject();
				mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				HG0565ManageSource sourceScreen = new HG0565ManageSource(pointOpenProject, null);
				sourceScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xySource = menuEvidence.getLocationOnScreen();
				sourceScreen.setLocation(xySource.x, xySource.y + 30);
				sourceScreen.setVisible(true);
				mainFrame.setCursor(Cursor.getDefaultCursor());
			}
		});

		// Evidence - Manage Source Types
		menuSourceTypes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HBProjectOpenData pointOpenProject = getSelectedOpenProject();
				HG0567ManageSourceType sourceTypeScreen = new HG0567ManageSourceType(pointOpenProject);
				sourceTypeScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xySourceT = menuEvidence.getLocationOnScreen();
				sourceTypeScreen.setLocation(xySourceT.x, xySourceT.y + 30);
				sourceTypeScreen.setVisible(true);
			}
		});

		// Evidence - Manage Source Elements
		menuSourceElmnts.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HBProjectOpenData pointOpenProject = getSelectedOpenProject();
				HG0564ManageSrcElmnt srcElmntScreen;
				try {
					srcElmntScreen = new HG0564ManageSrcElmnt(pointOpenProject);
					srcElmntScreen.setModalityType(ModalityType.APPLICATION_MODAL);
					Point xySourceE = menuEvidence.getLocationOnScreen();
					srcElmntScreen.setLocation(xySourceE.x, xySourceE.y + 30);
					srcElmntScreen.setVisible(true);
				} catch (HBException e1) {
					e1.printStackTrace();
				}
			}
		});

		// Evidence - Manage Repositories
		menuRepository.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HBProjectOpenData pointOpenProject = getSelectedOpenProject();
				HG0569ManageRepos repoScreen = new HG0569ManageRepos(pointOpenProject);
				repoScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xyRepo = menuEvidence.getLocationOnScreen();
				repoScreen.setLocation(xyRepo.x, xyRepo.y + 30);
				repoScreen.setVisible(true);
			}
		});

/****************************************************
 * LISTENERS FOR REPORT ITEMS
 * NB: menuWhWh used as anchor location for Report
 * screens to avoid showing too far to the right
 * when longMenu is shown.
 ***************************************************/
		// Hover-over acts as doClick
		menuReports.addMouseListener(new MouseAdapter() {
	        @Override
			public void mouseEntered(MouseEvent evt) {
				menuReports.doClick();
	        }
	    });

		// Reports -> Ancestor/Descendant tree
		menuAncDes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg) {
			if (HGlobal.numOpenProjects == 0)  {
					JOptionPane.showMessageDialog(HG0401HREMain.mainPane,
							HG0401Msgs.Text_160,HG0401Msgs.Text_161,JOptionPane.WARNING_MESSAGE);
					return;
					}
			HG0660AncestorDescendant ancdesScreen;
			try {
				HBProjectOpenData pointOpenProject = getSelectedOpenProject();
				ancdesScreen = new HG0660AncestorDescendant(pointOpenProject,
						pointOpenProject.getPersonHandler());
				ancdesScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xyReport;
				if (longMenu) xyReport = menuWhWh.getLocationOnScreen();
				else xyReport = menuReports.getLocationOnScreen();
				ancdesScreen.setLocation(xyReport.x, xyReport.y + 50);
				ancdesScreen.setVisible(true);
				}
				catch (HBException mpe) {
					if (HGlobal.DEBUG) System.out.println("Menu Report error: " + mpe.getMessage()); //$NON-NLS-1$
						mpe.printStackTrace();
					}
			 	}
		});

		// Reports -> Logging
		menuRepLog.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg) {
			HG0520Logging logScreen=new HG0520Logging();
			logScreen.setModalityType(ModalityType.APPLICATION_MODAL);
			Point xyReport;
			if (longMenu) xyReport = menuWhWh.getLocationOnScreen();
			else xyReport = menuReports.getLocationOnScreen();
			logScreen.setLocation(xyReport.x, xyReport.y + 50);
			logScreen.setVisible(true);
			}
		});

/****************************************************
 * LISTENERS FOR TOOLS MENU ITEMS
 * NB: menuWhWh used as anchor location for Tools
 * screens to avoid showing too far to the right
 * if longMenu is shown.
 ***************************************************/
		// Hover-over acts as doClick
		menuTools.addMouseListener(new MouseAdapter() {
	        @Override
			public void mouseEntered(MouseEvent evt) {
				menuTools.doClick();
	        }
	     });

		// Tools - Import - Import TMG
		menuToolImportTMG.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HG0417TMGProjectImport importTMG = new HG0417TMGProjectImport((HBToolHandler) pointBusinessLayer[8]);
				importTMG.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xyTools;
				if (longMenu) xyTools = menuWhWh.getLocationOnScreen();
				else xyTools = menuTools.getLocationOnScreen();
				importTMG.setLocation(xyTools.x + 50, xyTools.y + 50);
				importTMG.setVisible(true);
			}
		});

		// Tools - Settings - User
		menuToolSetUser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HG0501AppSettings appsetScreen = new HG0501AppSettings(getSelectedOpenProject(),(HBToolHandler) pointBusinessLayer[8]);
				appsetScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xyTools;
				if (longMenu) xyTools = menuWhWh.getLocationOnScreen();
				else xyTools = menuTools.getLocationOnScreen();
				appsetScreen.setLocation(xyTools.x, xyTools.y + 30);
				appsetScreen.setVisible(true);
			}
		});

		// Tools - Administration - Edit Users and Rights
		menuToolAdminUsers.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HG0403ProjectUserAdmin adminauxScreen = new HG0403ProjectUserAdmin((HBToolHandler) pointBusinessLayer[8]);
				adminauxScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xyTools;
				if (longMenu) xyTools = menuWhWh.getLocationOnScreen();
				else xyTools = menuTools.getLocationOnScreen();
				adminauxScreen.setLocation(xyTools.x, xyTools.y + 30);
				adminauxScreen.setVisible(true);
			}
		});

		// Tools - DB Maintenance
		menuToolDBmaint.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HG0620DBMaintenance maintScreen = new HG0620DBMaintenance(getSelectedOpenProject());
				maintScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xyTools;
				if (longMenu) xyTools = menuWhWh.getLocationOnScreen();
				else xyTools = menuTools.getLocationOnScreen();
				maintScreen.setLocation(xyTools.x, xyTools.y + 30);
				maintScreen.setVisible(true);
			}
		});

/****************************************************
 * LISTENERS FOR HELP MENU ITEMS
 ***************************************************/
		// Hover-over acts as doClick
		menuHelp.addMouseListener(new MouseAdapter() {
	        @Override
			public void mouseEntered(MouseEvent evt) {
				menuHelp.doClick();
	          }
	       });

		// Help - Contents
		menuHelpContent.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HB0614Help.fullHelp(HGlobal.mainWindowInstance);
			}
		});

		// Help -> load HRE Website
		menuHREweb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(Desktop.isDesktopSupported())
				{
				  try {
					Desktop.getDesktop().browse(new URI("https://www.historyresearchenvironment.org")); //$NON-NLS-1$
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				}
			} }
		});

		// Help -> About HRE
		menuAboutHRE.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				HG0415HelpAboutHRE helpabout=new HG0415HelpAboutHRE();
				helpabout.setModalityType(ModalityType.APPLICATION_MODAL);
				helpabout.setLocationRelativeTo(mainPane);    // ensure HelpAbout always loads central to menu mainPane
				helpabout.setVisible(true);
			}
		});

/*****************************************************
 * Listener for Project Status ('P' symbol) hover-over
 *****************************************************/
		// Project Status item hover-over throws up the Status JDialog
		menuStatus.addMouseListener(new MouseAdapter() {
	        @Override
			public void mouseEntered(MouseEvent evt) {
				if (HGlobal.statusWindowOpen) return;	// allow only 1 instance open
				HGlobal.statusWindowOpen = true;
				HG0416ProjectStatus projStatus= new HG0416ProjectStatus();
				Point xyStatus = menuStatus.getLocationOnScreen();
			// Adjust position if running on a Mac, when 'P' is at right of menu
				if (HGlobal.osType.contains("mac")) xyStatus.x = xyStatus.x - 250;  //$NON-NLS-1$
				projStatus.setLocation(xyStatus.x, xyStatus.y+20);
				projStatus.setAlwaysOnTop(true);
				projStatus.setVisible(true);
	          }
	       });

/************************************
 * Listeners for Function Key Actions
 ************************************/
	    InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
	    ActionMap actionMap = rootPane.getActionMap();
	    // For F1 (Help) - invoke Help
	    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "F1");	//$NON-NLS-1$
	    actionMap.put("F1", new AbstractAction() {						//$NON-NLS-1$
	    	private static final long serialVersionUID = 1L;
	        @Override
			public void actionPerformed(ActionEvent arg0) {
	        	menuHelpContent.doClick();
	         	}
	    });
	    // For F2 (Search) - open PeopleSelect
	    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "F2");	//$NON-NLS-1$
	    actionMap.put("F2", new AbstractAction() {						//$NON-NLS-1$
	    	private static final long serialVersionUID = 1L;
	        @Override
			public void actionPerformed(ActionEvent arg0) {
	        	if (HGlobal.numOpenProjects > 0) menuPersonSel.doClick();
	         	}
	    });

/**************************************
 * Activate plugin for long menu
 * @param false - set plugin logging off
 **************************************/
	    if (HGlobal.pluginEnabled) {
		    if (longMenu) {
		    	initiatePlugin(menuBar, HGlobal.pluginLog);
		    	revalidate();
		    }
	    }

	} 	// End of mainMenuSetup

/****************************************************
 * Reset the Main menu & Help if GUI language changes
 ****************************************************/
	    public void resetGUILanguage() {
	        HA0001HREstart.setGUIlanguage();			// perform NLS language setup
	        mainMenuSetup();							// redraw the main menu
	        revalidate();								// revalidate the screen
	        HB0614Help.setupHelp();						// set the Help system to new language
	        if (HGlobal.DEBUG) System.out.println("Native language reset"); //$NON-NLS-1$
	    }	// End resetGUILanguage

/*****************************************************
 * Routines to enable/disable the Add Person sub-menus
 * (used by HG0506ManagePerson)
 *****************************************************/
	    public void enableRelatives() {
			menuPersonPartner.setEnabled(true);
			menuPersonFather.setEnabled(true);
			menuPersonMother.setEnabled(true);
			menuPersonBrother.setEnabled(true);
			menuPersonSister.setEnabled(true);
			menuPersonSon.setEnabled(true);
			menuPersonDau.setEnabled(true);
	    }
	    public void disableRelatives() {
			menuPersonPartner.setEnabled(false);
			menuPersonFather.setEnabled(false);
			menuPersonMother.setEnabled(false);
			menuPersonBrother.setEnabled(false);
			menuPersonSister.setEnabled(false);
			menuPersonSon.setEnabled(false);
			menuPersonDau.setEnabled(false);
	    }

/********************************
 * GUI user messages
 * @param errorCode
 * @param projectName
 ********************************/
       	private void userInfoOpenProject(int errorCode, String projectName) {
       		String errorMess = ""; //$NON-NLS-1$
       		String errorTitle = HG0401Msgs.Text_166;  // Open Project
       		if (errorCode == 0) {
       			errorMess = HG0401Msgs.Text_167;
       			JOptionPane.showMessageDialog(null, errorMess  + projectName, errorTitle,
       												 JOptionPane.INFORMATION_MESSAGE);
       		}
       		if (errorCode > 0) {
       			if (errorCode == 1)  errorMess = HG0401Msgs.Text_168 + projectName + HG0401Msgs.Text_169; // Failed
       			if (errorCode == 2)  errorMess = HG0401Msgs.Text_170 + projectName + HG0401Msgs.Text_171; // In use
       			if (errorCode < 9)		// errorCode = 9 = Cancel out of Remote login - needs no error msg
       					JOptionPane.showMessageDialog(null, errorMess, errorTitle, JOptionPane.ERROR_MESSAGE);
       		}
       	}	// End userInfoOpenProject

/*********************************************************
 * Method to remove 'Recents' lists on project Open/Close
 * ******************************************************/
  	   public void removeRecents() {
  		   // Empty Person menu Recently Used PID/Names/Sex lists
  		   menuPerPID.clear();
  		   menuPerName.clear();
  		   menuPerSex.clear();
  		   // Remove Recently Used entries from Person menu
  		   int numItems = menuPerson.getMenuComponentCount();
  		   for (int i = numItems-1; i >= 9; i--) {
  			 menuPerson.remove(i);
  		   }
  	   } 	// End removeRecents
/*******************************************************************
 * Open a project from a click on a Project Name in the project menu
 * @param projClicked - project name in Recent list that was clicked on
 * @param icon - project icon
 *******************************************************************/
	    class clickProject extends AbstractAction {
	    	private static final long serialVersionUID = 001L;

	    	public clickProject(String projClicked, Icon icon) {
	            super(projClicked, icon);
	        	}
	        @Override
			public void actionPerformed(ActionEvent e) {
	        	int errorCode = 1;
	    		// Do not allow more than 3 open projects
	    		if (HGlobal.numOpenProjects == 3) {
	    			JOptionPane.showMessageDialog(HG0401HREMain.mainPane,
	    					HG0401Msgs.Text_165,HG0401Msgs.Text_166,JOptionPane.INFORMATION_MESSAGE);
	    			return;
	    			}
	    		// Else open the clicked project
	        	String clickedProject = (String) getValue(Action.NAME);

	        	try {
		        	mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					int selectIndex = ((HBProjectHandler) pointBusinessLayer[0]).getUserProjectByName(clickedProject);
					String [] projectData = ((HBProjectHandler) pointBusinessLayer[0]).getUserProjectByIndex(selectIndex);

		       // If local
					if (HGlobal.thisComputer.equals(projectData[3])) {
		        		errorCode = ((HBProjectHandler) pointBusinessLayer[0]).openProjectLocal(clickedProject);
		        	} else {
		        // else Remote Login
		        		if (HGlobal.DEBUG) System.out.println("Remote server name: " + projectData[3]);	//$NON-NLS-1$
						HG0575DBLogon poinLogonScreen = new HG0575DBLogon((HBProjectHandler) pointBusinessLayer[0], projectData);
						poinLogonScreen.setModalityType(ModalityType.APPLICATION_MODAL);
						Point xymainPane = HG0401HREMain.mainPane.getLocationOnScreen();
						poinLogonScreen.setLocation(xymainPane.x + 100, xymainPane.y + 100);
						poinLogonScreen.setVisible(true);
						errorCode = poinLogonScreen.getErrorCode();
		        	}

		            mainFrame.setCursor(Cursor.getDefaultCursor());
		            if (errorCode == 0) {
		            		countMenuOpenProjects = countMenuOpenProjects + 1;		// add to the count of projects opened this way
		            		exitCount = countMenuOpenProjects;						// and copy it
		            		removeRecents();			// Clear the Recent Person list
		            		longMenu = true;
		            		mainMenuSetup(); 	// redraw the main menu for longmenu
		            		revalidate();		// revalidate it

		            		// Set the selectedProject and update pointer
			            	setSelectedOpenProject(HGlobalCode.pointOpenProjectByName(clickedProject));
			            	// Write message if selected
			            	if (HGlobal.DEBUG) userInfoOpenProject(errorCode, clickedProject);
		            	} else if (errorCode > 0)
		            			userInfoOpenProject(errorCode, clickedProject);

		        } catch (HBException hbe) {
		        	System.out.println("HG0401HREMain setSelectedOpenProject Error: " +  clickedProject); //$NON-NLS-1$
					if (HGlobal.writeLogs) {
						HB0711Logging.logWrite("Error opening project from menu list: " + hbe.getMessage()); //$NON-NLS-1$
						HB0711Logging.printStackTraceToFile(hbe);
					}
					userInfoOpenProject(errorCode, clickedProject);
				}
	         }
	    }	// End clickProject

/************************************************************************
 * Methods to remove and add Projects from the bottom of the Project menu
 * @param add/removeItem - item in Project/Recent list to be added/removed
 ************************************************************************/
	   // To remove an item from the Project menu list
	   public void removeProjMenuItem(String removeItem) {
		   int numItems = menuProj.getMenuComponentCount();		// get count of ALL entries in menu
		   // Search menuProj from AFTER the Separator and 'Recent Projects' text
		   // this means care needed with setting i to correct number if menu entries changed!!!!
		   for (int i = 16; i < numItems; i++) {
				JMenuItem menuItem = (JMenuItem) menuProj.getMenuComponent(i);
				String menuText = menuItem.getText();
				if(menuText.trim().equals(removeItem.trim())) {
					menuProj.remove(i);
					menuProj.revalidate();
					return;
				}
			}
	   }	// End removeProjMenuItem

	   // To add a new item to the Project menu list with HRE icon and click action
	   public void addProjMenuItem(String addItem) {
		   menuProj.add(new JMenuItem(new clickProject(addItem.trim(),
				   new ImageIcon(getClass().getResource("/hre/images/HRE-16.png"))))); //$NON-NLS-1$
		   menuProj.revalidate();
	   } 	// End addProjMenuItem

 	   // To add an item to the Project menu list with the 'folder open' icon and no action
	   public void resetProjMenuItem(String addItem) {
		   JMenuItem openP = new JMenuItem(addItem.trim(),
				   new ImageIcon(getClass().getResource("/hre/images/open_16.png"))); //$NON-NLS-1$
		   menuProj.add(openP);
		   menuProj.revalidate();
	   } 	// End resetProjMenuItem

/***********************************************************************************
 * Open ManagePerson screen from a click on a Person in the Person/Recently used list
 * @param persClicked - person name & ID number of person in Recent list who was clicked on
 * @param icon - male/female icon
 ************************************************************************************/
        class clickPeople extends AbstractAction {
       	private static final long serialVersionUID = 001L;
       	public clickPeople(String persClicked, Icon icon) {
               super(persClicked, icon);
           	}
        @Override
		public void actionPerformed(ActionEvent e) {
           	// Get the recently used entry that was clicked
           	String persClicked = (String) getValue(Action.NAME);
   	    	// match that value into the list of current Recent Person menuItems
   	    	int menuNum;
   	    	for (menuNum = 9; menuNum < menuPerson.getMenuComponentCount(); menuNum++) {
   			   	JMenuItem persItem = (JMenuItem) menuPerson.getMenuComponent(menuNum);
   			   	String persText = persItem.getText();
   			   	if(persText.trim().equals(persClicked.trim())) break;
   	    	}

   	    	// There can be a mis-match in the number of PIDs vs menu entries, so correct that first
   	    	while (menuPerPID.size() + 9 > menuPerson.getMenuComponentCount()) menuPerPID.remove(0);
   	    	// menuNum is a number in range of 10-19; set it for 0-9 range of menuPerPIDs
   	    	// and then extract the persPID that matches the persClicked name
   	    	Long persPID = menuPerPID.get(menuNum-9);

           	// Load ManagePerson for selected person
           	HBProjectOpenData pointOpenProject = getSelectedOpenProject();
           	HBPersonHandler pointPersonHandler = pointOpenProject.getPersonHandler();
           	if (persPID != null_RPID)
       					pointPersonHandler.initiateManagePerson(pointOpenProject, persPID, "50600");	//$NON-NLS-1$
            }
       }	// End clickPeople
/****************************************************************
 * Method to add PersonName to the Person menu Recently Used area
 * @param addName - person's name to add
 * @param sex - string 'M' or 'F' or null
 * @param persPID - PID of person being added to menu list
 ****************************************************************/
	   public void updateRecentPersons(String addName, String sex, Long persPID) {
		   String addPerson = "  " + addName.trim();	//$NON-NLS-1$
			// There can be a mis-match in the number of PIDs vs menu entries, so correct that first
	    	while (menuPerPID.size() + 9 > menuPerson.getMenuComponentCount()) {
	    		menuPerName.remove(0);
	    		menuPerSex.remove(0);
	    		menuPerPID.remove(0);
	    	}
		   // If Person already in the Recent list we don't want to add them again
		   // so compare PID with existing arraylist of PIDs
		   for (int i = 0; i < menuPerPID.size(); i++) {
			   if (persPID.equals(menuPerPID.get(i))) {
				   // Have found Person already in menu list, but name may have been
				   // edited by user, so update the menu list and name list with Person-name
				   menuPerson.getItem(i+9).setText(addPerson);
				   menuPerName.set(i, addPerson);
				   return;
			   }
		   }
		   // Have new Person; add them at end of Recent list with male or female menu icon (no icon if sex unknown)
		   if (sex.equals("M")) menuPerson.add(new JMenuItem(new clickPeople(addPerson,					//$NON-NLS-1$
				   					new ImageIcon(getClass().getResource("/hre/images/male.png"))))); 	//$NON-NLS-1$
		   else {if (sex.equals("F")) menuPerson.add(new JMenuItem(new clickPeople(addPerson,			//$NON-NLS-1$
  									new ImageIcon(getClass().getResource("/hre/images/female.png"))))); //$NON-NLS-1$
		   		else menuPerson.add(new JMenuItem(new clickPeople(addPerson, null)));
		   		}
		   // And add the new persName, Sex, PID to their Lists
		   menuPerName.add(addPerson);
		   menuPerSex.add(sex);
		   menuPerPID.add(persPID);
		   // But we only want 10 entries in the list, so delete the oldest
		   // entry if the menuPerson size has got to 9 (basic menu) + 11 = 20 entries
		   if (menuPerson.getMenuComponentCount() == 20) {
				   menuPerson.remove(9);	// remove oldest menu item
				   menuPerName.remove(0);	// remove matching Name
				   menuPerSex.remove(0);	// remove matching sex
				   menuPerPID.remove(0);	// remove matching PID
		   }

	   } 	// End updateRecentPersons
/****************************************************************
* Method to remove PersonName from Person menu Recently Used area
* @param persPID - PID of person being removed from Recents list
****************************************************************/
	   public void deleteRecentPerson(Long persPID) {
		   // Match persPID to entry in menuPerPID
		   for (int i = 0; i < menuPerPID.size(); i++) {
			   if (persPID.equals(menuPerPID.get(i))) {
				   // Delete this person from the menuPerXxxx Arraylists
				   menuPerName.remove(i);	// remove matching Name
				   menuPerSex.remove(i);	// remove matching sex
				   menuPerPID.remove(i);	// remove matching PID
				   // Delete from the menu list
				   menuPerson.remove(i+9);
				   return;
			   }
		   }
	   } 	// End deleteRecentPerson

/*****************************************************************************
 * Initiate the plugin environment - needs to be done once during HRE startup.
 * @param JMenuBar mainMenu - pointer to HRE main menu
 * @param boolean logging - true starts plugin logging
 ****************************************************************************/
	   public void initiatePlugin(JMenuBar mainMenu, boolean logging) {

		//  Path standard Plugin folder
		   String pathUserPlugin = System.getProperty("user.home") + File.separator + "HRE" + File.separator + "Plugins";  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		//  Set path user plugin folder
			System.setProperty("pf4j.pluginsDir", pathUserPlugin);   //$NON-NLS-1$
			if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: starting plugins from " + pathUserPlugin);  //$NON-NLS-1$

		// Turn off plugin logging, if not requested
	        if (!logging) {
	        	LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
	        	loggerContext.stop();
	        }

	     // Kickoff the PluginManager
	        final PluginManager pluginManager = new DefaultPluginManager() {
		        @Override
				protected ExtensionFinder createExtensionFinder() {
		            DefaultExtensionFinder extensionFinder = (DefaultExtensionFinder) super.createExtensionFinder();
		            extensionFinder.addServiceProviderExtensionFinder();
		            return extensionFinder;
		        }
	    	};

	 	// Plugin load and start
			pluginManager.loadPlugins();
			pluginManager.startPlugins();

		// Add to the menu bar by using the available extensions.
		// Find Main menu Extensions
	        List<MainMenuExtensionPoint> extensions = pluginManager.getExtensions(MainMenuExtensionPoint.class);
	        for (MainMenuExtensionPoint extension : extensions) {
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: starting HRE extension: " + extension.className());  //$NON-NLS-1$
	        	extension.setNationalLanguage(HGlobal.nativeLanguage);
	            extension.setMainPane(mainPane);
	            extension.setMainPointer(mainFrame);
	            //extension.setBusinessLayerPointer(pointBusinessLayer);
	            extension.buildMenuBar(mainMenu);
	        }
		// Find Research Type extensions
	        List<ResearchMenuExtensionPoint> researchExtensions = pluginManager.getExtensions(ResearchMenuExtensionPoint.class);
	        for (ResearchMenuExtensionPoint extension : researchExtensions) {
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: starting Research extension: " + extension.className());  //$NON-NLS-1$
	        	extension.setNationalLanguage(HGlobal.nativeLanguage);
	            extension.setMainPane(mainPane);
	            extension.setMainPointer(mainFrame);
	            extension.buildMenu(menuResearch);
	        }

		// Find Report extensions
	        List<ReportMenuExtensionPoint> reportExtensions = pluginManager.getExtensions(ReportMenuExtensionPoint.class);
	        for (ReportMenuExtensionPoint extension : reportExtensions) {
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: starting Report extension: " + extension.className());  //$NON-NLS-1$
				extension.setDEBUG(HGlobal.DEBUG);
				extension.setDDLversion(HGlobal.databaseVersion);
				extension.setNationalLanguage(HGlobal.nativeLanguage);
	            extension.setMainPane(mainPane);
	            extension.setMainPointer(mainFrame);
	            extension.buildMenu(menuReports);
	        }

		// Find Tool extensions
	        List<ToolMenuExtensionPoint> toolExtensions = pluginManager.getExtensions(ToolMenuExtensionPoint.class);
	        for (ToolMenuExtensionPoint extension : toolExtensions) {
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: starting Tool extension: " + extension.className());  //$NON-NLS-1$
	        	extension.setNationalLanguage(HGlobal.nativeLanguage);
	            extension.setMainPane(mainPane);
	            extension.setMainPointer(mainFrame);
	            extension.buildMenu(menuTools);
	        }

		// Find Viewpoint extensions
	        List<ViewpointMenuExtensionPoint> viewpointExtensions = pluginManager.getExtensions(ViewpointMenuExtensionPoint.class);
	        for (ViewpointMenuExtensionPoint extension : viewpointExtensions) {
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: starting Viewpoint extension: " + extension.className());  //$NON-NLS-1$
	        	extension.setNationalLanguage(HGlobal.nativeLanguage);
	            extension.setMainPane(mainPane);
	            extension.setMainPointer(mainFrame);
	            extension.buildMenu(menuViewpoint);
	        }

		}	// End initiatePlugin

}	// End of HG0401HREMain