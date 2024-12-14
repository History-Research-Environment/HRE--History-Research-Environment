package hre.gui;
/**********************************************************************************
 * Application Settings - Specification 05.01 GUI_AppSettings 2020-03-10
 * v0.00.0007 2019-05-17 by D Ferguson
 * v0.00.0008 2019-07-21 fix bug in handling Cancel closing
 * v0.00.0010 2019-08-25 make GUI font an AppSetting variable, default Arial
 * v0.00.0013 2019-11-08 make GUI language an AppSetting variable
 * v0.00.0014 2019-11-18 changes for HB0711/0744 static classes (D Ferguson)
 * v0.00.0015 2019-12-07 changed code for HG0514 call (D Ferguson)
 * v0.00.0017 2019-01-09 language drop-down show selected language (D.Ferguson)
 * v0.00.0017 2020-01-11 code to support reset of GUIlanguage inflight (D. Ferguson)
 *            2020-01-14 add setting for 'open last project at startup'
 *            2020-01-18 change 0744 call to indicate Options only
 * v0.00.0018 2020-02-01 add 'DEBUG' option (not saved in UserAUX) (N. Tolleshaug)
 * v0.00.0020 2020-03-10 convert to tabbed pane; add color reset (D. Ferguson)
 * v0.00.0022 2020-07-05 add date/time format selectors (D Ferguson)
 *            2020-07-27 add screenID for Reminder, Help xref (D Ferguson)
 *            2020-08-02 remove showVPproject setting (D Ferguson)
 * v0.01.0024 2020-08-04 changed from JDialog to extend HG0450SuperDialog (D Ferguson)
 *            2020-09-11 removed Reminder icon from Toolbar (D Ferguson)
 *            2020-09-19 converted to MigLayout; add JTattoo LAF choice tab (D Ferguson)
 * v0.01.0025 2020-11-18 add File Locations tab and handling of settings (D Ferguson)
 * 			  2021-01-06 add 'Backup on close' setting (D Ferguson)
 * 			  2021-01-28 add new Language settings in new tab (D Ferguson)
 * 			  2021-02-06 partial NLS (2 tabs) and adjust Language settings (D Ferguson)
 * 			  2021-02-15 implement guiLangShort/Long language tables (D Ferguson)
 * 		      2021-03-21 Test file locations exist in Accept process (D Ferguson)
 * 			  2021-04-18 Fix File Location settings for empty location (D Ferguson)
 * v0.01.0026 2021-05-23 Setup for managing Data/Report language settings (D Ferguson)
 * 			  2021-05-27 Add Server Mode panel (D Ferguson) server setup (N Tolleshaug)
 * 			  2021-06-20 Rewrite Accents pane to allow multiple settings (D Ferguson)
 * 			  2021-08-07 Add ability to set font in Look & Feel panel (D Ferguson)
 * 			  2021-08-21 Fix reset of LAF etc on change cancel; add enablePlugin (D Ferguson)
 * 			  2021-09-15 Apply tag codes to screen control buttons (D Ferguson)
 * v0.01.0027 2022-02-20 Add Spanish, Italian as valid GUI languages (D Ferguson)
 *			  2022-02-21 Add setting of data, report languages (N Tolleshaug)
 *			  2022-02-26 Modified to use the NLS version of HGlobal (D Ferguson)
 *			  2022-03-06 Use HGlobal routines for Data language setting (D Ferguson)
 *			  2022-03-12 Add support of T201 for data, report languages (N Tolleshaug)
 *			  2022-03-22 Add setting Accent border color (D Ferguson)
 *			  2022-09-04 Add English(UK) support (D Ferguson)
 * v0.03.0030 2023-09-04 Implement Dutch NLS support (D Ferguson)
 * v0.03.0031 2023-12-17 Add Lifespan setting (D Ferguson)
 * 			  2024-04-07 Make Popupmenu font match Menu font on LAF change (D Ferguson)
 *			  2024-10-10 Modified change date format to use accept button (N Tolleshaug)
 *			  2024-10-11 Code cleanup (D Ferguson)
 *			  2024-10-11 Edited indented {} block paranthesis (N Tolleshaug)
 *			  2024-10-12 Add 'PS reload' User option (D Ferguson)
 *			  2024-10-12 Removed resetPersonSelect() from change dataformat (N Tolleshaug)
 *			  2024-11-15 Add 'prompt for Married Name' User option (D Ferguson)
 ***********************************************************************************/

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.Position;

import com.jtattoo.plaf.AbstractLookAndFeel;

import hre.bila.HB0711Logging;
import hre.bila.HB0744UserAUX;
import hre.bila.HBPersonHandler;
import hre.bila.HBProjectOpenData;
import hre.bila.HBToolHandler;
import hre.nls.HG0501Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Application Settings
 * @author D Ferguson
 * @version v0.03.0031
 * @since 2019-05-17
 */

public class HG0501AppSettings extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	private String screenID = "50100";  //$NON-NLS-1$
	public HG0401HREMain mainPanel;		// needed for GUI language reset
	private HBPersonHandler pointPersonHandler;
	private JPanel contents;

	private boolean changedPathLocation, changedPathReports, changedPathBackups, changedPathExtBackups = false;
	private boolean tcpModeChanged = false;
	private boolean dateFormatChanged = false;
	private String dataLang = HGlobal.dataLanguage,
			data2Lang = HGlobal.data2Language,
			reptLang = HGlobal.reptLanguage;  // to hold settings of Data/Report languages
	private boolean guiLangChanged = false;
	private boolean dataLangChanged = false;
	private boolean data2LangChanged = false;
	private boolean reptLangChanged = false;
	// Language table lists - we need a standard language literal to be able to set
	// HGlobal.nativelanguage in a form that NLS and Help understand. The 2 GUI tables must be
	// co-ordinated with the HA0001HREStart.guiLanguage method, and with each other, and
	// with each Help sub-folder name.
	private String[] guiLangCodes = new String[] {
//			"da", //$NON-NLS-1$		// Danish
			"de", //$NON-NLS-1$		// German
//			"el", //$NON-NLS-1$		// Greek
			"en", //$NON-NLS-1$		// English(US)
			"gb", //$NON-NLS-1$		// English(UK/AU)
			"es", //$NON-NLS-1$		// Spanish
			"fr", //$NON-NLS-1$		// French
			"it", //$NON-NLS-1$		// Italian
			"nl", //$NON-NLS-1$		// Dutch
			"no", //$NON-NLS-1$		// Norwegian
//			"pt"  //$NON-NLS-1$		// Portugese
			};
	// This table must match above IMPLEMENTED GUI languages. It loads the GUI language combobox.
	// The unused Text_xx entries have all been setup in NLS and are ready for use.
	private String[] guiLanguages = new String[] {
//			HG0501Msgs.Text_70, 	// Danish
			HG0501Msgs.Text_71, 	// German
//			HG0501Msgs.Text_72,		// Greek
			HG0501Msgs.Text_73, 	// English(US)
			HG0501Msgs.Text_69, 	// English(UK/AU/NZ)
			HG0501Msgs.Text_74, 	// Spanish
			HG0501Msgs.Text_75, 	// French
			HG0501Msgs.Text_76, 	// Italian
			HG0501Msgs.Text_77, 	// Dutch
			HG0501Msgs.Text_78, 	// Norwegian
//			HG0501Msgs.Text_79  	// Portugese
			};
	// The next 2 table entries must be the same order as 'guiLanguages' above but
	// the VALID entries must be ONLY those that are also IMPLEMENTED DATA LANGUAGES,
	// because the number of GUI languages is >= the number of Data/Rept languages.
	// These NLS values are copied into the STATIC version (dataReptLanguages)
	// when the GUI language changes to enable the data/rept comboboxes to
	// show the language as per the GUI setting and be referenced from other screens.
	private String[] dataReptNLSLanguages = new String[] {
//			HG0501Msgs.Text_70, 	// Danish
			HG0501Msgs.Text_71, 	// German
//			HG0501Msgs.Text_72,		// Greek
			HG0501Msgs.Text_73, 	// English(US)
			HG0501Msgs.Text_69, 	// English(UK)
//			HG0501Msgs.Text_74, 	// Spanish
			HG0501Msgs.Text_75, 	// French
//			HG0501Msgs.Text_76, 	// Italian
			HG0501Msgs.Text_77, 	// Dutch
			HG0501Msgs.Text_78, 	// Norwegian
//			HG0501Msgs.Text_79  	// Portugese
			};
	public static String[] dataReptLanguages = new String[] {
//			"Danish", 			//$NON-NLS-1$
			"German",			//$NON-NLS-1$
//			"Greek", 			//$NON-NLS-1$
			"English(US)",		//$NON-NLS-1$
			"English(UK)", 		//$NON-NLS-1$
//			"Spanish", 			//$NON-NLS-1$
			"French", 			//$NON-NLS-1$
//			"Italian",			//$NON-NLS-1$
			"Dutch",			//$NON-NLS-1$
			"Norwegian",		//$NON-NLS-1$
//			"Portugese" 		//$NON-NLS-1$
			};
	// The following table is used by Data and Report language settings
	// Number of entries and language order must match 'dataReptLanguages' above
	public static String[] dataReptLangCodes = new String[] {
//			"da-DK", //$NON-NLS-1$	// Danish
			"de-DE", //$NON-NLS-1$	// German
//			"el-GR", //$NON-NLS-1$	// Greek
			"en-US", //$NON-NLS-1$	// English(US)
			"en-GB", //$NON-NLS-1$	// English(UK/AU/NZ)
//			"es-ES", //$NON-NLS-1$	// Spanish
			"fr-FR", //$NON-NLS-1$	// French
//			"it-IT", //$NON-NLS-1$	// Italian
			"nl-NL", //$NON-NLS-1$	// Dutch
			"no-NB", //$NON-NLS-1$	// Norwegian
//			"pt-PT"  //$NON-NLS-1$	// Portugese
			};

	/**
	 * Available dateformats - must be in line with date format processing
	 */
	private String [] dateFormats = {"dd.mm.yyyy",	//$NON-NLS-1$
			"dd-mm-yyyy",	//$NON-NLS-1$
			"dd/mm/yyyy",	//$NON-NLS-1$
			"dd Mmm yyyy",	//$NON-NLS-1$
			"dd MMM yyyy",	//$NON-NLS-1$
			"yyyy.mm.dd",	//$NON-NLS-1$
			"yyyy-mm-dd",	//$NON-NLS-1$
			"Mmm dd, yyyy",	//$NON-NLS-1$
			"MMM dd, yyyy",	//$NON-NLS-1$
			"mm/dd/yyyy"};	//$NON-NLS-1$

  // ********* Lists for JTattoo Look and feel settings
    private JList<String> lafList = null;
    private int selectedLaf = 0;
    private ListSelectionListener lafListener = null;
    // List for associated Themes
    private JList<Object> themeList = null;
    private int selectedTheme = 0;
    private String theme = "Default";   //$NON-NLS-1$
    private ListSelectionListener themeListener = null;
    // The Look and feel names in JTattoo LAF system
    private String[] lafNames = new String[]
    { "Acryl", "Aero", "Aluminium", "Bernstein", "Fast", "Graphite",  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
      "HiFi", "Luna", "McWin", "Mint", "Noire", "Smart", "Texture"};  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
    // and fields for Fonts
    private JList<String> fontList = null;
    private int selectedFont = -1;
    private ListSelectionListener fontListener = null;

  // ********** Fields for Accent setting and manipulation
	private Color accentFillSaved = Color.white;			// set a default saved color
	private Color accentFontSaved = Color.black;			// set a default saved color
	private Color accentBorderSaved = Color.white;			// set a default saved color
	private boolean accentsSaved = false;
	private Color accentFillDflt = Color.decode("#82C8FF");	//$NON-NLS-1$ light blue; RGB 130,200,255
	private Color accentFontDflt = Color.black;				// Black font
	private Color accentBorderDflt = Color.white;			// white border
	private int selectedAccRow = -1;						// set NO accent selected
	private JTable tableAccentNames;
	private JTable tableAccentRules;
	private boolean isMovingRows = false;

/**
 * Create the dialog
***/
	public HG0501AppSettings(HBProjectOpenData openProject, HBToolHandler pointToolHand) {
		// Setup references for HG0450
		windowID = screenID;
		helpName = "settings"; //$NON-NLS-1$

		if (HGlobal.writeLogs) {HB0711Logging.logWrite("Action: entering HG0501 App Settings");}	 //$NON-NLS-1$
		setResizable(false);
		setTitle(HG0501Msgs.Text_16);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "10[]", "[][][]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	 // Toolbar setup
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
		toolBar.add(Box.createHorizontalGlue());
		// Add icons defined in HG0450
		toolBar.add(btn_Helpicon);

	 // *********** Define the tabbed Pane structure - tabbedPane size effectively sets screen size ***********
		JTabbedPane tabPane = new JTabbedPane(SwingConstants.TOP);
		tabPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
		tabPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		tabPane.setPreferredSize(new Dimension(650, 300));

	 // *********** Define HRE Look and Feel panel for JTattoo LAF and Themes ***********
        JPanel panelLAF = new JPanel();
        tabPane.addTab(HG0501Msgs.Text_20, null, panelLAF);
        tabPane.setMnemonicAt(0, KeyEvent.VK_H);
        panelLAF.setLayout(new MigLayout("insets 10", "[]10[]10[]", "15[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        // Create a list with all look and feel names
        lafList = new JList<String>(lafNames);						// <<- remove <String> to make WB Design mode work
        lafList.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        lafList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane lafScrollPane = new JScrollPane(lafList);
        lafScrollPane.setPreferredSize(new Dimension(200, 270));
        lafScrollPane.setBorder(BorderFactory.createTitledBorder (HG0501Msgs.Text_24));
        lafList.setSelectedIndex(HGlobal.lafNumber);
        lafList.ensureIndexIsVisible(lafList.getSelectedIndex());
        selectedLaf = HGlobal.lafNumber;
        panelLAF.add(lafScrollPane, "cell 0 0"); //$NON-NLS-1$

        // Create a list with all Themes
        themeList = new JList<Object>();
        themeList.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        themeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane themeScrollPane = new JScrollPane(themeList);
        themeScrollPane.setPreferredSize(new Dimension(220, 270));
        themeScrollPane.setBorder(BorderFactory.createTitledBorder(HG0501Msgs.Text_25));
        panelLAF.add(themeScrollPane, "cell 1 0");             //$NON-NLS-1$
        fillThemeList();
        themeList.setSelectedIndex(HGlobal.lafThemeNumber);
        themeList.ensureIndexIsVisible(themeList.getSelectedIndex());

        // Create a list to show all available system fonts on this PC;
        // then check if HGlobal.fontName is in the list on this PC,
        // and if so, set the selectedFont position in the fontList
        fontList = new JList<String>();
        fontList.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        fontList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        String availFonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        fontList.setListData(availFonts);
        JScrollPane fontScrollPane = new JScrollPane(fontList);
        fontScrollPane.setPreferredSize(new Dimension(220, 270));
        fontScrollPane.setBorder(BorderFactory.createTitledBorder(HG0501Msgs.Text_26));
        panelLAF.add(fontScrollPane, "cell 2 0");		 //$NON-NLS-1$
        // find font's position in fontList and show it
		selectedFont = fontList.getNextMatch(HGlobal.fontName,0,Position.Bias.Forward);
        if (selectedFont != -1) {
        	fontList.setSelectedIndex(selectedFont);
            fontList.ensureIndexIsVisible(fontList.getSelectedIndex());
        }

	// *********** Define Language panel ***********
        JPanel panelLang = new JPanel();
        tabPane.addTab(HG0501Msgs.Text_28, null, panelLang);
        tabPane.setMnemonicAt(1, KeyEvent.VK_L);
        panelLang.setLayout(new MigLayout("insets 10", "20[]10[]20[]10[]", "15[]15[]15[]15[]15[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel lbl_GUIlanguage = new JLabel(HG0501Msgs.Text_32);		// Screen language
		lbl_GUIlanguage.setBorder(null);
		lbl_GUIlanguage.setHorizontalAlignment(SwingConstants.RIGHT);
		panelLang.add(lbl_GUIlanguage, "cell 0 0,alignx right"); //$NON-NLS-1$

		JComboBox<String> combo_GUIlanguage = new JComboBox<String>(guiLanguages);
//		JComboBox<String> combo_GUIlanguage = new JComboBox(guiLanguages);	// use this version to make WB Design mode work
		// Match HGlobal.nativeLanguage to entry in langShort to enable setting of
		// correct language name to display from LangCodes
		for (int i=0; i < guiLangCodes.length; i++) {
			if (HGlobal.nativeLanguage.equals(guiLangCodes[i]))
				combo_GUIlanguage.setSelectedIndex(i);
		}
		combo_GUIlanguage.setToolTipText(HG0501Msgs.Text_39);	// Select the language from the drop-down list
		panelLang.add(combo_GUIlanguage, "cell 1 0, w 180!"); //$NON-NLS-1$

		// Now copy the current NLS'd dataReptNLSLanguages to overwrite the STATIC version,
		// in case the GUI language was changed in a previous use of this screen
		dataReptLanguages = dataReptNLSLanguages;

		JLabel lbl_Datalanguage = new JLabel(HG0501Msgs.Text_41);		// Data presentation language
		lbl_Datalanguage.setBorder(null);
		lbl_Datalanguage.setHorizontalAlignment(SwingConstants.RIGHT);
		panelLang.add(lbl_Datalanguage, "cell 0 1,alignx right"); //$NON-NLS-1$

		JComboBox<String> combo_Datalanguage = new JComboBox<String>(dataReptLanguages);
//		JComboBox<String> combo_Datalanguage = new JComboBox(dataReptLanguages); // use this version to make WB Design mode work
		combo_Datalanguage.setSelectedIndex(-1);		// Set as no value for starters
		if (HGlobal.numOpenProjects > 0) {
			for (int i=0; i < dataReptLangCodes.length; i++) {
				if (HGlobal.dataLanguage.equals(dataReptLangCodes[i]))
					combo_Datalanguage.setSelectedIndex(i);
			}
		}
		combo_Datalanguage.setToolTipText(HG0501Msgs.Text_46);	// Select the language from the drop-down list
		panelLang.add(combo_Datalanguage, "cell 1 1, w 180!"); //$NON-NLS-1$

		JLabel lbl_Data2language = new JLabel(HG0501Msgs.Text_48);		// Alternate Data language
		lbl_Data2language.setBorder(null);
		lbl_Data2language.setHorizontalAlignment(SwingConstants.RIGHT);
		panelLang.add(lbl_Data2language, "cell 0 2,alignx right"); //$NON-NLS-1$

		JComboBox<String> combo_Data2language = new JComboBox<String>(dataReptLanguages);
//		JComboBox<String> combo_Data2language = new JComboBox(dataReptLanguages);	// use this version to make WB Design mode work
		combo_Data2language.setSelectedIndex(-1);		// Set as no value for starters
		if (HGlobal.numOpenProjects > 0) {
			for (int i=0; i < dataReptLangCodes.length; i++) {
				if (HGlobal.data2Language.equals(dataReptLangCodes[i]))
					combo_Data2language.setSelectedIndex(i);
			}
		}
		combo_Data2language.setToolTipText(HG0501Msgs.Text_53);	// Select the language from the drop-down list
		panelLang.add(combo_Data2language, "cell 1 2, w 180!");	 //$NON-NLS-1$

		JLabel lbl_Reportlanguage = new JLabel(HG0501Msgs.Text_55);		// Report output language
		lbl_Reportlanguage.setBorder(null);
		lbl_Reportlanguage.setHorizontalAlignment(SwingConstants.RIGHT);
		panelLang.add(lbl_Reportlanguage, "cell 0 3,alignx right"); //$NON-NLS-1$

		JComboBox<String> combo_Reportlanguage = new JComboBox<String>(dataReptLanguages);
//		JComboBox<String> combo_Reportlanguage = new JComboBox(dataReptLanguages);	// use this version to make WB Design mode work
		combo_Reportlanguage.setSelectedIndex(-1);		// Set as no value for starters
		if (HGlobal.numOpenProjects > 0) {
			for (int i=0; i < dataReptLangCodes.length; i++) {
				if (HGlobal.reptLanguage.equals(dataReptLangCodes[i]))
					combo_Reportlanguage.setSelectedIndex(i);
				}
			}
		combo_Reportlanguage.setToolTipText(HG0501Msgs.Text_60);	// Select the language from the drop-down list
		panelLang.add(combo_Reportlanguage, "cell 1 3, w 180!"); //$NON-NLS-1$

	// *********** Define Date panel ***********
        JPanel panelFormats = new JPanel();
        tabPane.addTab(HG0501Msgs.Text_80, null, panelFormats);  // Date settings
        tabPane.setMnemonicAt(2, KeyEvent.VK_T);
        panelFormats.setLayout(new MigLayout("insets 10", "20[]10[]20[]10[]", "15[]15[]15[]15[]15[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        JLabel lbl_DateFormat = new JLabel(HG0501Msgs.Text_81);
        lbl_DateFormat.setHorizontalAlignment(SwingConstants.RIGHT);
        lbl_DateFormat.setBorder(null);
        panelFormats.add(lbl_DateFormat, "cell 0 0,alignx right"); //$NON-NLS-1$

        JComboBox<String> combo_DateFormat = new JComboBox<String>();
		for (int i = 0; i < dateFormats.length; i++)
			combo_DateFormat.addItem(dateFormats[i]);

		combo_DateFormat.setSelectedItem(HGlobal.dateFormat);				// make current setting the one displayed on screen
        combo_DateFormat.setToolTipText(HG0501Msgs.Text_82);
        panelFormats.add(combo_DateFormat, "cell 1 0, w 180!"); //$NON-NLS-1$

        JLabel lbl_TimeFormat = new JLabel(HG0501Msgs.Text_83);
        lbl_TimeFormat.setHorizontalAlignment(SwingConstants.RIGHT);
        lbl_TimeFormat.setBorder(null);
        panelFormats.add(lbl_TimeFormat, "cell 0 1,alignx right"); //$NON-NLS-1$

        JComboBox<String> combo_TimeFormat = new JComboBox<String>();
		combo_TimeFormat.addItem("HH:mm:ss.msec"); //$NON-NLS-1$
		combo_TimeFormat.addItem("hh:mm:ss.msec AA");	 //$NON-NLS-1$
		combo_TimeFormat.setSelectedItem(HGlobal.timeFormat);			// make current setting the one displayed on screen
        combo_TimeFormat.setToolTipText(HG0501Msgs.Text_84);
        panelFormats.add(combo_TimeFormat, "cell 1 1, w 180!"); //$NON-NLS-1$

        JLabel lbl_Lifespan = new JLabel(HG0501Msgs.Text_65);
        lbl_Lifespan.setHorizontalAlignment(SwingConstants.RIGHT);
        lbl_Lifespan.setBorder(null);
        panelFormats.add(lbl_Lifespan, "cell 0 2 2,alignx left"); //$NON-NLS-1$

        JTextField txt_Lifespan = new JTextField();
        txt_Lifespan.setColumns(4);
        txt_Lifespan.setText(HGlobal.lifespan);			// pickup current setting
        panelFormats.add(txt_Lifespan, "cell 0 2 2, gapx 10"); //$NON-NLS-1$

	// *********** Define User options panel ***********
        JPanel panelOpt = new JPanel();
        tabPane.addTab(HG0501Msgs.Text_85, null, panelOpt);
        tabPane.setMnemonicAt(3, KeyEvent.VK_U);
        panelOpt.setLayout(new MigLayout("insets 10", "20[]10[]20[]10[]", "15[]15[]15[]15[]15[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JCheckBox chkbx_Welcome = new JCheckBox(HG0501Msgs.Text_86);
		chkbx_Welcome.setHorizontalTextPosition(SwingConstants.LEFT);
		chkbx_Welcome.setHorizontalAlignment(SwingConstants.TRAILING);
		chkbx_Welcome.setToolTipText(HG0501Msgs.Text_87);
        panelOpt.add(chkbx_Welcome, "cell 0 0,alignx right"); //$NON-NLS-1$

		JCheckBox chkbx_CancelMsgs = new JCheckBox(HG0501Msgs.Text_88);
		chkbx_CancelMsgs.setToolTipText(HG0501Msgs.Text_89);
		chkbx_CancelMsgs.setHorizontalTextPosition(SwingConstants.LEFT);
		chkbx_CancelMsgs.setHorizontalAlignment(SwingConstants.TRAILING);
		panelOpt.add(chkbx_CancelMsgs, "cell 0 1,alignx right"); //$NON-NLS-1$

		JCheckBox chkbx_OpenLastProj = new JCheckBox(HG0501Msgs.Text_90);
		chkbx_OpenLastProj.setToolTipText(HG0501Msgs.Text_91);
		chkbx_OpenLastProj.setHorizontalTextPosition(SwingConstants.LEFT);
		chkbx_OpenLastProj.setHorizontalAlignment(SwingConstants.TRAILING);
		panelOpt.add(chkbx_OpenLastProj, "cell 0 2,alignx right"); //$NON-NLS-1$

		JCheckBox chkbx_BkupActProj = new JCheckBox(HG0501Msgs.Text_92);
		chkbx_BkupActProj.setToolTipText(HG0501Msgs.Text_93);
		chkbx_BkupActProj.setHorizontalTextPosition(SwingConstants.LEFT);
		chkbx_BkupActProj.setHorizontalAlignment(SwingConstants.TRAILING);
		panelOpt.add(chkbx_BkupActProj, "cell 0 3,alignx right"); //$NON-NLS-1$

		JCheckBox chkbx_EnablePlugins = new JCheckBox(HG0501Msgs.Text_145);
		chkbx_EnablePlugins.setToolTipText(HG0501Msgs.Text_146);
		chkbx_EnablePlugins.setHorizontalTextPosition(SwingConstants.LEFT);
		chkbx_EnablePlugins.setHorizontalAlignment(SwingConstants.TRAILING);
		panelOpt.add(chkbx_EnablePlugins, "cell 0 4,alignx right"); //$NON-NLS-1$

		JCheckBox chkbx_reloadPS = new JCheckBox(HG0501Msgs.Text_152);	// Always reload Person Selector
		chkbx_reloadPS.setToolTipText(HG0501Msgs.Text_153);				// If set, Person Selector always reloads from database
		chkbx_reloadPS.setHorizontalTextPosition(SwingConstants.LEFT);
		chkbx_reloadPS.setHorizontalAlignment(SwingConstants.TRAILING);
		panelOpt.add(chkbx_reloadPS, "cell 0 5,alignx right"); //$NON-NLS-1$

		JCheckBox chkbx_promptMarried = new JCheckBox(HG0501Msgs.Text_154);	// Prompt for Married Name
		chkbx_promptMarried.setToolTipText(HG0501Msgs.Text_155);			// If set, Prompt for a Married Name when saving a Marriage event
		chkbx_promptMarried.setHorizontalTextPosition(SwingConstants.LEFT);
		chkbx_promptMarried.setHorizontalAlignment(SwingConstants.TRAILING);
		panelOpt.add(chkbx_promptMarried, "cell 0 6,alignx right"); //$NON-NLS-1$

     // *********** Define Accent Colour settings panel ***********
        JPanel panelAccent = new JPanel();
        tabPane.addTab(HG0501Msgs.Text_94, null, panelAccent); 		// Accents
        tabPane.setMnemonicAt(4, KeyEvent.VK_A);
        panelAccent.setLayout(new MigLayout("insets 10", "[]10[]5[]10[]", "[]10[]10[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// Layout the left column with panes
	    JPanel topleftPane = new JPanel();
	    topleftPane.setBorder(UIManager.getBorder("TitledBorder.border"));  //$NON-NLS-1$
		topleftPane.setLayout(new MigLayout("insets 5", "[]", "[]10[]10")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		panelAccent.add(topleftPane, "cell 0 0,growx,aligny top");          //$NON-NLS-1$

	    JRadioButton accentOff = new JRadioButton(HG0501Msgs.Text_95);		// Do NOT aaply Accents
	    accentOff.setToolTipText(HG0501Msgs.Text_96);   					// If selected, Accents are not applied
	    accentOff.setHorizontalTextPosition(SwingConstants.RIGHT);
	    topleftPane.add(accentOff, "cell 0 0,alignx left"); //$NON-NLS-1$

	    JRadioButton accentFirst = new JRadioButton(HG0501Msgs.Text_97);	// Apply first matching accent
	    accentFirst.setSelected(true);
	    accentFirst.setToolTipText(HG0501Msgs.Text_98);  					// If selected, first matching Accent is applied
	    accentFirst.setHorizontalTextPosition(SwingConstants.RIGHT);
	    topleftPane.add(accentFirst, "cell 0 1,alignx left"); //$NON-NLS-1$

	    ButtonGroup accentGroup = new ButtonGroup();
	    accentGroup.add(accentOff);
	    accentGroup.add(accentFirst);

		JButton btn_AccentAdd = new JButton(HG0501Msgs.Text_99); 			// Add a New Accent
		btn_AccentAdd.setToolTipText(HG0501Msgs.Text_100);   				// Adds a new accent
		panelAccent.add(btn_AccentAdd, "cell 0 1,growx,aligny bottom"); //$NON-NLS-1$

		JButton btn_AccentDel = new JButton(HG0501Msgs.Text_101);			// Delete Selected Accent
		btn_AccentDel.setToolTipText(HG0501Msgs.Text_102); 					// Deletes the selected accent
		btn_AccentDel.setEnabled(false);
		panelAccent.add(btn_AccentDel, "cell 0 2,growx,aligny top"); //$NON-NLS-1$

	    JPanel btmleftPane = new JPanel();
	    btmleftPane.setBorder(UIManager.getBorder("TitledBorder.border"));      //$NON-NLS-1$
		btmleftPane.setLayout(new MigLayout("insets 5", "[]", "[]10[]5[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		panelAccent.add(btmleftPane, "cell 0 3, growx");                        //$NON-NLS-1$

		JLabel lbl_setAccentColor = new JLabel(HG0501Msgs.Text_103); 		// Edit Selected Accent
		lbl_setAccentColor.setEnabled(false);
		btmleftPane.add(lbl_setAccentColor, "cell 0 0,alignx center");  //$NON-NLS-1$

		JButton btn_AccentFill = new JButton(HG0501Msgs.Text_104);			// Set Fill Color
		btn_AccentFill.setToolTipText(HG0501Msgs.Text_105); 				// Set Accent background color
		btn_AccentFill.setEnabled(false);
		btmleftPane.add(btn_AccentFill, "cell 0 1"); //$NON-NLS-1$

		JButton btn_AccentFont = new JButton(HG0501Msgs.Text_106);  		// Set Font Color
		btn_AccentFont.setToolTipText(HG0501Msgs.Text_107); 				// Set Accent font color
		btn_AccentFont.setEnabled(false);
		btmleftPane.add(btn_AccentFont, "cell 0 1"); //$NON-NLS-1$

 		JButton btn_AccentBorder = new JButton(HG0501Msgs.Text_150);  		//    Set Border Color
		btn_AccentBorder.setToolTipText(HG0501Msgs.Text_151); 				// Set color for cell border
		btn_AccentBorder.setEnabled(false);
		btmleftPane.add(btn_AccentBorder, "cell 0 2, alignx center"); //$NON-NLS-1$

        JButton btn_AccentReset = new JButton(HG0501Msgs.Text_108);			// Reset to previous colors
        btn_AccentReset.setToolTipText(HG0501Msgs.Text_109); 				// Resets colors back to previous settings
		btn_AccentReset.setEnabled(false);
		btmleftPane.add(btn_AccentReset, "cell 0 3,growx");  //$NON-NLS-1$

		JScrollPane scrollAccentNames = new JScrollPane();
		panelAccent.add(scrollAccentNames, "cell 1 0 1 4,grow, pushy"); //$NON-NLS-1$

		JScrollPane scrollAccentRules = new JScrollPane();
		panelAccent.add(scrollAccentRules, "cell 2 0 1 4,grow, pushy"); //$NON-NLS-1$

		ImageIcon upArrow = new ImageIcon(getClass().getResource("/hre/images/arrow_up16.png")); //$NON-NLS-1$
		JButton btn_AccentUp = new JButton(HG0501Msgs.Text_111, upArrow);	// Move
		btn_AccentUp.setVerticalTextPosition(SwingConstants.BOTTOM);
		btn_AccentUp.setHorizontalTextPosition(SwingConstants.CENTER);
		btn_AccentUp.setEnabled(false);
		panelAccent.add(btn_AccentUp, "cell 3 1,alignx left");   //$NON-NLS-1$

		ImageIcon downArrow = new ImageIcon(getClass().getResource("/hre/images/arrow_down16.png")); //$NON-NLS-1$
		JButton btn_AccentDown = new JButton(HG0501Msgs.Text_111, downArrow);  // Move
		btn_AccentDown.setVerticalTextPosition(SwingConstants.TOP);
		btn_AccentDown.setHorizontalTextPosition(SwingConstants.CENTER);
		btn_AccentDown.setEnabled(false);
		panelAccent.add(btn_AccentDown, "cell 3 2,alignx left");  //$NON-NLS-1$

        // Setup Accent Name table for the defined Accents, with colors rendered from the color arrays
	    DefaultTableModel accentNameModel = new DefaultTableModel();
	    accentNameModel.addColumn(HG0501Msgs.Text_112, HGlobal.accentNames.toArray()); 	// Defined Accents
		tableAccentNames = new JTable(accentNameModel) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int column) {
				return true;
			}
			public Class<?> getColumnClass(int column) {
		        return String.class;
			}
			public Component prepareRenderer(TableCellRenderer renderer, int row, int col)  {
				Component comp = super.prepareRenderer(renderer, row, col);
				JComponent jcomp = (JComponent) comp;
				// Apply our colors to the cells (font, fill, border)
				comp.setBackground(HGlobal.accentFillColors.get(row));
				comp.setForeground(HGlobal.accentFontColors.get(row));
				int top = (row > 0 && isRowSelected(row-1))?1:2;
		        int left = col == 0?2:0;
		        int bottom = (row < getRowCount()-1 && isRowSelected(row + 1))?1:2;
		        int right = col == getColumnCount()-1?2:0;
				Color border = HGlobal.accentBorderColors.get(row);
				jcomp.setBorder(BorderFactory.createMatteBorder(top, left, bottom, right, border));
				// But for the Selected row we need to identify it, so make it Italic, Bold
				if (isRowSelected(row))	comp.setFont(comp.getFont().deriveFont(Font.ITALIC | Font.BOLD));
				return comp;
			}
		};
	    tableAccentNames.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
	    tableAccentNames.setShowGrid(false);
	    tableAccentNames.setIntercellSpacing(new Dimension(5, 2));	// pad width 5, top/btm 2
		// link table to scroll pane
		scrollAccentNames.setViewportView(tableAccentNames);
		tableAccentNames.setFillsViewportHeight(true);
		tableAccentNames.setPreferredScrollableViewportSize(new Dimension(150, 100));

        // Setup Accent Rules table for the defined Accent rules
	    DefaultTableModel accentRuleModel = new DefaultTableModel();
	    accentRuleModel.addColumn(HG0501Msgs.Text_113, HGlobal.accentRules.toArray());	// Accent Rules
		tableAccentRules = new JTable(accentRuleModel) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
			public Class<?> getColumnClass(int column) {
		        return String.class;
			}
		};
		// For now, don't allow selection on the Rules table
		tableAccentRules.setFocusable(false);
		tableAccentRules.setRowSelectionAllowed(false);
		tableAccentRules.setShowGrid(false);
		tableAccentRules.setIntercellSpacing(new Dimension(5, 2));	// pad width 5, top/btm 2
		// link table to scroll pane
		scrollAccentRules.setViewportView(tableAccentRules);
		tableAccentRules.setFillsViewportHeight(true);
		tableAccentRules.setPreferredScrollableViewportSize(new Dimension(240, 100));

/**
 *  This code is example code to enable implementing of different colours for each side of a cell.
 *  This will be required for full implementation of the desired Accent coloring approach in HRE
 *
		// Custom renderer - just adds a border to the cell
	    class CustomRenderer implements TableCellRenderer {
	        TableCellRenderer render;
	        Border b;
	        public CustomRenderer(TableCellRenderer r, Color top, Color left,Color bottom, Color right){
	            render = r;
	            // This allows setting of different colour borders around the cell
	            b = BorderFactory.createCompoundBorder();
	            b = BorderFactory.createCompoundBorder(b, BorderFactory.createMatteBorder(3,0,0,0,top));
	            b = BorderFactory.createCompoundBorder(b, BorderFactory.createMatteBorder(0,3,0,0,left));
	            b = BorderFactory.createCompoundBorder(b, BorderFactory.createMatteBorder(0,0,3,0,bottom));
	            b = BorderFactory.createCompoundBorder(b, BorderFactory.createMatteBorder(0,0,0,3,right));
	        }
	        @Override
	        public Component getTableCellRendererComponent(JTable table, Object value,
	        		boolean isSelected, boolean hasFocus, int row, int column) {
	            JComponent result = (JComponent)render.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	            result.setBorder(b);
	            return result;
	        }
	    }
	    // Example implementation of the renderer to the whole table (needs restricting to the selected cell)
        CustomRenderer cr = new CustomRenderer(tableAccentNames.getDefaultRenderer(Object.class), Color.red, Color.black, Color.blue, Color.white);
        tableAccentNames.setDefaultRenderer(Object.class, cr);
 */

      // *********** Define File Locations panel ***********
        JPanel panelFiles = new JPanel();
        tabPane.addTab(HG0501Msgs.Text_114, null, panelFiles);
        tabPane.setMnemonicAt(5, KeyEvent.VK_F);
        panelFiles.setLayout(new MigLayout("insets 10", "20[]10[]", "15[]15[]15[]15[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        JLabel lbl_HREloc = new JLabel(HG0501Msgs.Text_115);
        lbl_HREloc.setToolTipText(HG0501Msgs.Text_116);
		panelFiles.add(lbl_HREloc, "cell 0 0, alignx right"); //$NON-NLS-1$

        JLabel lbl_HREreport = new JLabel(HG0501Msgs.Text_117);
        lbl_HREreport.setToolTipText(HG0501Msgs.Text_118);
		panelFiles.add(lbl_HREreport, "cell 0 1, alignx right"); //$NON-NLS-1$

        JLabel lbl_HREbackup = new JLabel(HG0501Msgs.Text_119);
        lbl_HREbackup.setToolTipText(HG0501Msgs.Text_120);
		panelFiles.add(lbl_HREbackup, "cell 0 2, alignx right"); //$NON-NLS-1$

        JLabel lbl_HREbackExt = new JLabel(HG0501Msgs.Text_121);
        lbl_HREbackExt.setToolTipText(HG0501Msgs.Text_122);
		panelFiles.add(lbl_HREbackExt, "cell 0 3, alignx right"); //$NON-NLS-1$

		JTextField txt_HREloc = new JTextField(HGlobal.pathHRElocation);
		txt_HREloc.setColumns(50);
		panelFiles.add(txt_HREloc, "cell 1 0");		 //$NON-NLS-1$

		JTextField txt_HREreport = new JTextField(HGlobal.pathHREreports);
		txt_HREreport.setColumns(50);
		panelFiles.add(txt_HREreport, "cell 1 1"); //$NON-NLS-1$

		JTextField txt_HREbackup = new JTextField(HGlobal.pathHREbackups);
		txt_HREbackup.setColumns(50);
		panelFiles.add(txt_HREbackup, "cell 1 2"); //$NON-NLS-1$

		JTextField txt_HREbackExt = new JTextField(HGlobal.pathExtbackups);
		txt_HREbackExt.setColumns(50);
		panelFiles.add(txt_HREbackExt, "cell 1 3"); //$NON-NLS-1$

      // *********** Define Debug settings panel ***********
        JPanel panelDebug = new JPanel();
        tabPane.addTab(HG0501Msgs.Text_123, null, panelDebug);
        tabPane.setMnemonicAt(6, KeyEvent.VK_D);
        panelDebug.setLayout(new MigLayout("insets 10", "20[]10[]20[]10[]", "15[]15[]15[]15[]15[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        JCheckBox chkbx_WriteLogs = new JCheckBox(HG0501Msgs.Text_124);
        chkbx_WriteLogs.setToolTipText(HG0501Msgs.Text_125);
        chkbx_WriteLogs.setHorizontalTextPosition(SwingConstants.LEFT);
        chkbx_WriteLogs.setHorizontalAlignment(SwingConstants.TRAILING);
        panelDebug.add(chkbx_WriteLogs, "cell 0 0,alignx right"); //$NON-NLS-1$

		JCheckBox chkbxSetDebugOn = new JCheckBox(HG0501Msgs.Text_126);
		chkbxSetDebugOn.setToolTipText(HG0501Msgs.Text_127);
		chkbxSetDebugOn.setHorizontalTextPosition(SwingConstants.LEFT);
		chkbxSetDebugOn.setHorizontalAlignment(SwingConstants.TRAILING);
		panelDebug.add(chkbxSetDebugOn, "cell 0 1,alignx right");   //$NON-NLS-1$

		JCheckBox chkbxTime = new JCheckBox(HG0501Msgs.Text_147);
		chkbxTime.setToolTipText(HG0501Msgs.Text_148);
		chkbxTime.setHorizontalTextPosition(SwingConstants.LEFT);
		chkbxTime.setHorizontalAlignment(SwingConstants.TRAILING);
		panelDebug.add(chkbxTime, "cell 0 2,alignx right");   //$NON-NLS-1$

	  // *********** Define Server settings panel ***********
	    JPanel panelServer = new JPanel();
	    tabPane.addTab(HG0501Msgs.Text_128, null, panelServer);
	    tabPane.setMnemonicAt(7, KeyEvent.VK_S);
	    panelServer.setLayout(new MigLayout("insets 10", "20[]10[]20[]10[]", "15[]15[]15[]15[]15[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	    JRadioButton serverOn = new JRadioButton(HG0501Msgs.Text_129);
	    serverOn.setToolTipText(HG0501Msgs.Text_130);
	    serverOn.setHorizontalTextPosition(SwingConstants.LEFT);
	    serverOn.setHorizontalAlignment(SwingConstants.TRAILING);
	    panelServer.add(serverOn, "cell 0 0,alignx right"); //$NON-NLS-1$

	    JRadioButton serverOff = new JRadioButton(HG0501Msgs.Text_131);
	    serverOff.setToolTipText(HG0501Msgs.Text_132);
	    serverOff.setHorizontalTextPosition(SwingConstants.LEFT);
	    serverOff.setHorizontalAlignment(SwingConstants.TRAILING);
	    panelServer.add(serverOff, "cell 0 1,alignx right"); //$NON-NLS-1$

	    ButtonGroup serverGroup = new ButtonGroup();
	    serverGroup.add(serverOn);
	    serverGroup.add(serverOff);

	 // *********** Define panel at bottom for Cancel/Accept buttons ***********
		JPanel finalPane = new JPanel();
		finalPane.setLayout(new MigLayout("", "[]", "[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// Buttons and Layout within finalPane
		JButton btn_Cancel = new JButton(HG0501Msgs.Text_11);
		btn_Cancel.setToolTipText(HG0501Msgs.Text_12);
		finalPane.add(btn_Cancel, "cell 0 0, align right, gapx 20, tag cancel"); //$NON-NLS-1$

		JButton btn_Accept = new JButton(HG0501Msgs.Text_13);
		btn_Accept.setToolTipText(HG0501Msgs.Text_0);
		finalPane.add(btn_Accept, "cell 0 0, align right, gapx 20, tag ok");	//$NON-NLS-1$

	 // Setup simple MigLayout structure of Toolbar/tabPanes/finalPane for the JDialog
		contents.add(toolBar, "north"); //$NON-NLS-1$
		contents.add(tabPane, "cell 0 1"); //$NON-NLS-1$
		contents.add(finalPane, "cell 0 2, alignx right"); //$NON-NLS-1$
		pack();

/**************************************************
 * Setup all Listeners
**************************************************/
	 // *********** Listener for choice of LAF in LAF list
		lafListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (lafList.getSelectedIndex() != -1) {
                        if (selectedLaf != lafList.getSelectedIndex()) {
                            selectedLaf = lafList.getSelectedIndex();
                            // Only change look and feel after all pending events are dispatched,
                            // otherwise there will be some serious redrawing problems.
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    setLookAndFeel(true);
                                }
                            });
                        }
                    } else {
                        // We don't want the list to be unselected, so if user unselects the list
                        // we select the last selected entry
                        lafList.setSelectedIndex(selectedLaf);
                    }
                }
            }
        };
        lafList.addListSelectionListener(lafListener);

        // Listener for choice of Theme from theme list
        themeListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (themeList.getSelectedIndex() != -1) {
                        if (selectedTheme != themeList.getSelectedIndex()) {
                            selectedTheme = themeList.getSelectedIndex();
                            // Only change the look and feel after all pending events are dispatched,
                            // otherwise there will be some serious redrawing problems.
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    setLookAndFeel(false);
                                }
                            });
                        }
                    } else {
                        // We don't want the list to be unselected, so if user unselects the list
                        // we select the last selected entry
                        themeList.setSelectedIndex(selectedTheme);
                    }
                }
            }
        };
        themeList.addListSelectionListener(themeListener);

     // *********** Listener for choice of font in font list
     		fontListener = new ListSelectionListener() {
                 @Override
                 public void valueChanged(ListSelectionEvent e) {
                     if (!e.getValueIsAdjusting()) {
                         if (fontList.getSelectedIndex() != -1) {
                             if (selectedFont != fontList.getSelectedIndex()) {
                                 selectedFont = fontList.getSelectedIndex();
                                 // Set the font selected
                                 HGlobal.fontName = (String)fontList.getSelectedValue();
                                 // and implement across all font UIManager keys
                                 SwingUtilities.invokeLater(new Runnable() {
                                     @Override
                                     public void run() {
                                    	 setSelectedFont();
                                     }
                                 });
                             }
                         } else {
                             fontList.setSelectedIndex(selectedFont);
                         }
                     }
                 }
             };
             fontList.addListSelectionListener(fontListener);

	 // *********** Language combo-box listeners ***********
     	// GUI language combo-box listener
 		combo_GUIlanguage.addActionListener (new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				// Extract language from the languages list, NOT from the drop-down name list,
				// as we must have the short format for use elsewhere (like Help)
				int indxG = combo_GUIlanguage.getSelectedIndex();
				HGlobal.nativeLanguage = guiLangCodes[indxG];
				guiLangChanged = true;
				// Do NOT make other languages change when GUI language changes, as there will probably
				// always be more GUI languages than Data/Report languages
			}
		});

		// Data language combo-box listener
		combo_Datalanguage.addActionListener (new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				int indxD = combo_Datalanguage.getSelectedIndex();
				dataLang = dataReptLangCodes[indxD];
			// Store presentation data language in HGlobal
				dataLangChanged = true;
				HGlobal.dataLanguage = dataLang;
			}
		});

		// Data2 language combo-box listener
		combo_Data2language.addActionListener (new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				int indxD2 = combo_Data2language.getSelectedIndex();
				data2Lang = dataReptLangCodes[indxD2];
			// Store alternative data language in HGlobal
				data2LangChanged = true;
				HGlobal.data2Language = data2Lang;
			}
		});

		// Report language combo-box listener
		combo_Reportlanguage.addActionListener (new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				int indxR = combo_Reportlanguage.getSelectedIndex();
				reptLang = dataReptLangCodes[indxR];
				// Store report language in HGlobal
				reptLangChanged = true;
				HGlobal.reptLanguage = reptLang;
			}
		});

	 // *********** Date/time format listeners ***********
		// Date format combo-box listener
		combo_DateFormat.addActionListener (new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				dateFormatChanged = true;
			}
		});

		// Time format combo-box listener
		combo_TimeFormat.addActionListener (new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				HGlobal.timeFormat = (String) combo_TimeFormat.getSelectedItem(); // change global setting to selected format
			}
		});

		// Max. Lifespan setting listener
		DocumentListener spanListen = new DocumentListener() {
		    @Override
		    public void insertUpdate(DocumentEvent e) { updateFieldState(); }
		    public void removeUpdate(DocumentEvent e) { updateFieldState(); }
		    public void changedUpdate(DocumentEvent e) { updateFieldState(); }
		    protected void updateFieldState() {
		    	HGlobal.lifespan = txt_Lifespan.getText().trim();
		    }
		};
        txt_Lifespan.getDocument().addDocumentListener(spanListen);

	 // *********** Listener for 'Show Welcome screen' CheckBox ***********
		chkbx_Welcome.addActionListener(new ActionListener() {
		      @Override
			public void actionPerformed(ActionEvent e) {
		        if (chkbx_Welcome.isSelected()) {HGlobal.showWelcome = true;}
		        	else {HGlobal.showWelcome = false;}
		      }
		    });

		// Listener for 'Show Cancel msgs' CheckBox
		chkbx_CancelMsgs.addActionListener(new ActionListener() {
		      @Override
			public void actionPerformed(ActionEvent e) {
		        if (chkbx_CancelMsgs.isSelected()) {HGlobal.showCancelmsg = true; }
		        	else {HGlobal.showCancelmsg = false;}
		      }
		    });

		// Listener for 'Open last used project' CheckBox
		chkbx_OpenLastProj.addActionListener(new ActionListener() {
		      @Override
			public void actionPerformed(ActionEvent e) {
		        if (chkbx_OpenLastProj.isSelected()) {HGlobal.openLastProject = true; }
		        	else {HGlobal.openLastProject = false;}
		      }
		    });

		// Listener for 'Auto-backup closing project' CheckBox
		chkbx_BkupActProj.addActionListener(new ActionListener() {
		      @Override
			public void actionPerformed(ActionEvent e) {
		        if (chkbx_BkupActProj.isSelected()) {HGlobal.backupActivProject = true; }
		        	else {HGlobal.backupActivProject = false;}
		      }
		    });

		// Listener for 'Enable/disable plugins' CheckBox
		chkbx_EnablePlugins.addActionListener(new ActionListener() {
		      @Override
			public void actionPerformed(ActionEvent e) {
		        if (chkbx_EnablePlugins.isSelected()) {HGlobal.pluginEnabled = true; }
		        	else {HGlobal.pluginEnabled = false;}
		      }
		    });

		// Listener for 'PS Reload' CheckBox
		chkbx_reloadPS.addActionListener(new ActionListener() {
		      @Override
			public void actionPerformed(ActionEvent e) {
		        if (chkbx_reloadPS.isSelected()) {HGlobal.reloadPS = true; }
		        	else {HGlobal.reloadPS = false;}
		      }
		    });

		// Listener for 'prompt Married Name' CheckBox
		chkbx_promptMarried.addActionListener(new ActionListener() {
		      @Override
			public void actionPerformed(ActionEvent e) {
		        if (chkbx_promptMarried.isSelected()) {HGlobal.promptMarrName = true; }
		        	else {HGlobal.promptMarrName = false;}
		      }
		    });

	 // *********** Listener for 'Write log files' CheckBox ***********
        chkbx_WriteLogs.addActionListener(new ActionListener() {
              @Override
        	public void actionPerformed(ActionEvent e) {
                if (chkbx_WriteLogs.isSelected()) {HGlobal.writeLogs = true; }
                	else {HGlobal.writeLogs = false;}
              }
            });

        // Listener for 'DEBUG ON' CheckBox
		chkbxSetDebugOn.addActionListener(new ActionListener() {
		      @Override
			public void actionPerformed(ActionEvent e) {
		        if (chkbxSetDebugOn.isSelected()) {
		        	pointToolHand.setDebugOption(true);
		        } else {
		        	pointToolHand.setDebugOption(false);
		        }
		      }
		 });

		chkbxTime.addActionListener(new ActionListener() {
            @Override
      	public void actionPerformed(ActionEvent e) {
              if (chkbxTime.isSelected()) {HGlobal.TIME = true; }
              	else {HGlobal.TIME = false;}
            }
          });

	// *********** Accent mode radiobutton listeners ***********
		accentOff.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				HGlobal.accentOn = false;
			}
		});
		accentFirst.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				HGlobal.accentOn = true;
			}
		});

		// Listener for a selection in tableAccentNames
		tableAccentNames.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			@Override
			public void valueChanged(ListSelectionEvent ach) {
				// Ignore use of the MoveUp/Down functions
				if (isMovingRows) return;
		        if (!ach.getValueIsAdjusting()) {
		        	// Find which row selected
		        	selectedAccRow = tableAccentNames.getSelectedRow();
		        	// If none selected, turn off Delete
		            if (selectedAccRow != -1) btn_AccentDel.setEnabled(true);
		            // Turn off color Reset so its not done on wrong row
		            btn_AccentReset.setEnabled(false);
		            // Enable/disable buttons based on number of rows
		            int size = accentNameModel.getRowCount();
			        if (size == 0) {lbl_setAccentColor.setEnabled(false);
									btn_AccentFill.setEnabled(false);
									btn_AccentFont.setEnabled(false);
									btn_AccentBorder.setEnabled(false);
									btn_AccentReset.setEnabled(false);
			        			}
						else {lbl_setAccentColor.setEnabled(true);
						  btn_AccentFill.setEnabled(true);
						  btn_AccentFont.setEnabled(true);
						  btn_AccentBorder.setEnabled(true);
						  btn_AccentReset.setEnabled(true);
						}
					if (size > 1) {	btn_AccentUp.setEnabled(true);
									btn_AccentDown.setEnabled(true);
								}
						else {btn_AccentUp.setEnabled(false);
						      btn_AccentDown.setEnabled(false);
						}
		        }
		    }
		});

		// Listener for an Edit of an Accent Name
		tableAccentNames.getModel().addTableModelListener(new TableModelListener(){
			@Override
			public void tableChanged(TableModelEvent tme) {
				// Ignore use of the MoveUp/Down functions
				if (isMovingRows) return;
				// Now only proceed if this is an EDIT (ignore Add, Delete)
				if (tme.getType() == TableModelEvent.UPDATE) {
					// Get the changed data
					int row = tme.getFirstRow();
					int column = tme.getColumn();
					Object editedName = accentNameModel.getValueAt(row, column);
					// Save it into AccentNames
					HGlobal.accentNames.set(row, (String) editedName);
				}
			}
		});

		// Listener for adding a new Accent
		btn_AccentAdd.addActionListener(new ActionListener() {
		     @Override
			public void actionPerformed(ActionEvent e) {
		    	// first add default colours to new row in accentColors
		    	HGlobal.accentFillColors.add(accentFillDflt);
		    	HGlobal.accentFontColors.add(accentFontDflt);
		    	HGlobal.accentBorderColors.add(accentBorderDflt);
		    	// and add a new Name entry into accentNames and accentNameModel
		    	String accName = HG0501Msgs.Text_133;
		    	HGlobal.accentNames.add(accName);
		    	accentNameModel.insertRow(accentNameModel.getRowCount(), new Object[] { accName });
		    	// and repeat for Accent Rules
		    	String accRule = HG0501Msgs.Text_134;
		    	HGlobal.accentRules.add(accRule);
		    	accentRuleModel.insertRow(accentRuleModel.getRowCount(), new Object[] { accRule });
		      }
		});

		// Listener for deleting an Accent
		btn_AccentDel.addActionListener(new ActionListener() {
		     @Override
			public void actionPerformed(ActionEvent e) {
		    	// If no row selected, return
		    	selectedAccRow = tableAccentNames.getSelectedRow();
				if (selectedAccRow == -1) return;
  				// Copy selectedAccRow, as it gets corrupted below
  				int thisRow = selectedAccRow;
				// Remove selected row from the GUI and ALL Lists feeding the GUI (MUST do GUI LAST!)
				HGlobal.accentFillColors.remove(thisRow);
		    	HGlobal.accentFontColors.remove(thisRow);
		    	HGlobal.accentBorderColors.remove(thisRow);
		    	HGlobal.accentNames.remove(thisRow);
		    	HGlobal.accentRules.remove(thisRow);
		    	accentNameModel.removeRow(thisRow);
		    	accentRuleModel.removeRow(thisRow);
				// if no rows left, disable this button
				if (accentNameModel.getRowCount() == 0) btn_AccentDel.setEnabled(false);
		      }
		});

		// Listener for Accent fill color setting
		btn_AccentFill.addActionListener(new ActionListener() {
		     @Override
			public void actionPerformed(ActionEvent e) {
		    	// If no row selected, return
		    	selectedAccRow = tableAccentNames.getSelectedRow();
		 		if (selectedAccRow == -1) return;
		    	// Save the selected item's colors for Reset to use (if they haven't been saved already)
		    	if(!accentsSaved) { accentFillSaved = HGlobal.accentFillColors.get(selectedAccRow);
		    						accentFontSaved = HGlobal.accentFontColors.get(selectedAccRow);
		    						accentBorderSaved = HGlobal.accentBorderColors.get(selectedAccRow);
		    						btn_AccentReset.setEnabled(true);
		    					}
		    	//Display the color chooser, set to the current cell fill color
				HG0578ColorChooser
					colorScreen=new HG0578ColorChooser(HG0501Msgs.Text_135, accentFillSaved);
				colorScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				colorScreen.setTitle(HG0501Msgs.Text_136);
				Point xyColor = btn_AccentFill.getLocationOnScreen();
				colorScreen.setLocation(xyColor.x, xyColor.y);
				colorScreen.setVisible(true);
				HGlobal.accentFillColors.set(selectedAccRow, HGlobal.returnColor);	// save it into FillColor list
		      }
		});

		// Listener for Accent font color setting
		btn_AccentFont.addActionListener(new ActionListener() {
		     @Override
			public void actionPerformed(ActionEvent e) {
		    	// If no row selected, return
		    	selectedAccRow = tableAccentNames.getSelectedRow();
		 		if (selectedAccRow == -1) return;
			    // Save the selected item's colors for Reset to use  (if they haven't been saved already)
		 		if(!accentsSaved) { accentFillSaved = HGlobal.accentFillColors.get(selectedAccRow);
		 							accentFontSaved = HGlobal.accentFontColors.get(selectedAccRow);
		 							accentBorderSaved = HGlobal.accentBorderColors.get(selectedAccRow);
		 							btn_AccentReset.setEnabled(true);
		 						}
		    	//Display the color chooser, set to the current cell font color
				HG0578ColorChooser
					colorScreen=new HG0578ColorChooser(HG0501Msgs.Text_137, accentFontSaved); 	// Choose Accent font color; then close Color Chooser to continue
				colorScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				colorScreen.setTitle(HG0501Msgs.Text_136);										// Accent Color Chooser
				Point xyColor = btn_AccentFont.getLocationOnScreen();
				colorScreen.setLocation(xyColor.x, xyColor.y);
				colorScreen.setVisible(true);
				HGlobal.accentFontColors.set(selectedAccRow, HGlobal.returnColor);	// and save it into FontColor list
		      }
		 });

		// Listener for Accent Border color setting
		btn_AccentBorder.addActionListener(new ActionListener() {
		     @Override
			public void actionPerformed(ActionEvent e) {
		    	// If no row selected, return
		    	selectedAccRow = tableAccentNames.getSelectedRow();
		 		if (selectedAccRow == -1) return;
			    // Save the selected item's colors for Reset to use  (if they haven't been saved already)
		 		if(!accentsSaved) { accentFillSaved = HGlobal.accentFillColors.get(selectedAccRow);
		 							accentFontSaved = HGlobal.accentFontColors.get(selectedAccRow);
		 							accentBorderSaved = HGlobal.accentBorderColors.get(selectedAccRow);
		 							btn_AccentReset.setEnabled(true);
		 						}
		    	//Display the color chooser, set to the current cell font color
				HG0578ColorChooser
					colorScreen=new HG0578ColorChooser(HG0501Msgs.Text_138, accentBorderSaved); // Choose Accent border color; then close Color Chooser to continue
				colorScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				colorScreen.setTitle(HG0501Msgs.Text_136);										// Accent color chooser
				Point xyColor = btn_AccentBorder.getLocationOnScreen();
				colorScreen.setLocation(xyColor.x, xyColor.y);
				colorScreen.setVisible(true);
				HGlobal.accentBorderColors.set(selectedAccRow, HGlobal.returnColor);	// and save it into BorderColor list
		      }
		 });

		// Listener for Accent Reset button - pickup saved colors and restore them into the altered accent
		btn_AccentReset.addActionListener(new ActionListener() {
		    @Override
			public void actionPerformed(ActionEvent e) {
		    	// If no row selected, return
		    	selectedAccRow = tableAccentNames.getSelectedRow();
		 		if (selectedAccRow == -1) return;
		 		// Take the saved colors and restore them into last changed Accent
		    	HGlobal.accentFontColors.set(selectedAccRow, accentFontSaved);
		    	HGlobal.accentFillColors.set(selectedAccRow, accentFillSaved);
		    	HGlobal.accentBorderColors.set(selectedAccRow, accentBorderSaved);
		    	// Clear current selection so cell is properly visible
		    	tableAccentNames.clearSelection();
		    	btn_AccentReset.setEnabled(false);
		    }
		});

		// Listener for btn_AccentUp (move selected Accent/Rule upwards)
		btn_AccentUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
		    	// If no row selected, return
				selectedAccRow = tableAccentNames.getSelectedRow();
		 		if (selectedAccRow == -1) return;
  				// Copy selectedAccRow, as it gets corrupted below
  				int thisRow = selectedAccRow;
				// swap the ArrayList items and GUI items up 1 row
				if (thisRow > 0){
					isMovingRows = true;	// tell tableListeners to ignore these changes
					Collections.swap(HGlobal.accentFillColors, thisRow, thisRow - 1); // move (list, 1st, 2nd)
					Collections.swap(HGlobal.accentFontColors, thisRow, thisRow - 1);
					Collections.swap(HGlobal.accentBorderColors, thisRow, thisRow - 1);
					Collections.swap(HGlobal.accentNames, thisRow, thisRow - 1);
					Collections.swap(HGlobal.accentRules, thisRow, thisRow - 1);
		            accentNameModel.moveRow(thisRow, thisRow, thisRow - 1);	// move (start, end, to)
		            accentRuleModel.moveRow(thisRow, thisRow, thisRow - 1);
		            tableAccentNames.changeSelection(thisRow - 1, 0, false, false);	// reset selection to moved item
		            tableAccentRules.changeSelection(thisRow - 1, 0, false, false);
		            isMovingRows = false;
		        }
			}
		});

		// Listener for btn_AccentDown (move select Accent/Rule downwards)
		btn_AccentDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
		    	// If no row selected, return
				selectedAccRow = tableAccentNames.getSelectedRow();
		 		if (selectedAccRow == -1) return;
  				// Copy selectedAccRow, as it gets corrupted below
  				int thisRow = selectedAccRow;
				// swap the ArrayList items and GUI items down 1 row
				if (thisRow < accentNameModel.getRowCount() -1 ){
					isMovingRows = true;	// tell tableListeners to ignore these changes
					Collections.swap(HGlobal.accentFillColors, thisRow, thisRow + 1); // move (list, 1st, 2nd)
					Collections.swap(HGlobal.accentFontColors, thisRow, thisRow + 1);
					Collections.swap(HGlobal.accentBorderColors, thisRow, thisRow + 1);
					Collections.swap(HGlobal.accentNames, thisRow, thisRow + 1);
					Collections.swap(HGlobal.accentRules, thisRow, thisRow + 1);
					accentNameModel.moveRow(thisRow, thisRow, thisRow + 1);	// move (start, end, to)
					accentRuleModel.moveRow(thisRow, thisRow, thisRow + 1);
		            tableAccentNames.changeSelection(thisRow + 1, 0, false, false);	// reset selection to moved item
		            tableAccentRules.changeSelection(thisRow + 1, 0, false, false);
					isMovingRows = false;
		        }
			}
		});

	 // *********** Listener for entering data in HRE file location field ***********
		DocumentListener locListen = new DocumentListener() {
		    @Override
		    public void insertUpdate(DocumentEvent e) { updateFieldState(); }
		    public void removeUpdate(DocumentEvent e) { updateFieldState(); }
		    public void changedUpdate(DocumentEvent e) { updateFieldState(); }
		    protected void updateFieldState() {
		         String newHREloc = txt_HREloc.getText().trim();    // remove any leading/trailing blanks
            	 if (isValidPath(newHREloc) ) {
            		 	HGlobal.pathHRElocation = newHREloc;
            		 	changedPathLocation = true;
            		 	txt_HREloc.setForeground(UIManager.getColor("TextField.foreground")); //$NON-NLS-1$
            		 	txt_HREloc.setBackground(UIManager.getColor("TextField.background")); //$NON-NLS-1$
            			}
            	 else txt_HREloc.setBackground(Color.YELLOW);
		    }
		};
        txt_HREloc.getDocument().addDocumentListener(locListen);

		// Listener for entering data in HRE reports location field
		DocumentListener repListen = new DocumentListener() {
		    @Override
		    public void insertUpdate(DocumentEvent e) { updateFieldState(); }
		    public void removeUpdate(DocumentEvent e) { updateFieldState(); }
		    public void changedUpdate(DocumentEvent e) { updateFieldState(); }
		    protected void updateFieldState() {
		         String newHRErep = txt_HREreport.getText().trim();    // remove any leading/trailing blanks
            	 if (isValidPath(newHRErep) ) {
            		 	HGlobal.pathHREreports = newHRErep;
            		 	changedPathReports = true;
            		 	txt_HREreport.setForeground(UIManager.getColor("TextField.foreground")); //$NON-NLS-1$
            		 	txt_HREreport.setBackground(UIManager.getColor("TextField.background")); //$NON-NLS-1$
            			}
            	 else txt_HREreport.setBackground(Color.YELLOW);
		    }
		};
        txt_HREreport.getDocument().addDocumentListener(repListen);

		// Listener for entering data in HRE backup location field
		DocumentListener bakListen = new DocumentListener() {
		    @Override
		    public void insertUpdate(DocumentEvent e) { updateFieldState(); }
		    public void removeUpdate(DocumentEvent e) { updateFieldState(); }
		    public void changedUpdate(DocumentEvent e) { updateFieldState(); }
		    protected void updateFieldState() {
		         String newHREbak = txt_HREbackup.getText().trim();    // remove any leading/trailing blanks
            	 if (isValidPath(newHREbak) ) {
            		 	HGlobal.pathHREbackups = newHREbak;
            		 	changedPathBackups = true;
            		 	txt_HREbackup.setForeground(UIManager.getColor("TextField.foreground")); //$NON-NLS-1$
            		 	txt_HREbackup.setBackground(UIManager.getColor("TextField.background")); //$NON-NLS-1$
            			}
            	 else txt_HREbackup.setBackground(Color.YELLOW);
		    }
		};
        txt_HREbackup.getDocument().addDocumentListener(bakListen);

		// Listener for entering data in HRE external backup location field
		DocumentListener extListen = new DocumentListener() {
		    @Override
		    public void insertUpdate(DocumentEvent e) { updateFieldState(); }
		    public void removeUpdate(DocumentEvent e) { updateFieldState(); }
		    public void changedUpdate(DocumentEvent e) { updateFieldState(); }
		    protected void updateFieldState() {
		         String newHREext = txt_HREbackExt.getText().trim();    // remove any leading/trailing blanks
            	 if (isValidPath(newHREext) ) {
            		 	HGlobal.pathExtbackups = newHREext;
            		 	changedPathExtBackups = true;
            		 	txt_HREbackExt.setForeground(UIManager.getColor("TextField.foreground")); //$NON-NLS-1$
            		 	txt_HREbackExt.setBackground(UIManager.getColor("TextField.background")); //$NON-NLS-1$
            			}
            	 else txt_HREbackExt.setBackground(Color.YELLOW);
		    }
		};
        txt_HREbackExt.getDocument().addDocumentListener(extListen);

	 // *********** Server on/off button listeners ***********
		serverOn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				HGlobal.tcpServerOn = true;
				tcpModeChanged = true;
			}
		});
		serverOff.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				HGlobal.tcpServerOn = false;
				tcpModeChanged = true;
			}
		});

	 // *********** Listener for Accept button - check File Locations and write UserAUX file from HGlobal settings ***********
		btn_Accept.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// Ensure any edits in progress are completed
				if(null != tableAccentNames.getCellEditor()) {
					tableAccentNames.getCellEditor().stopCellEditing();
				}
		    	// Go through each File location Setting and test if it has changed;
				// if so, test that it exists.
				// An empty folder location field is accepted as 'existing'.
		    	// If folder does not exist, bring File Locations tab (#5) to the front,
				// then ask if user wants to create it:
		    	// - if YES, create the new directory and complete the Accept process
		    	// - if NO, highlight the field and break out of the Accept process.
				if (changedPathLocation) {
			    	if (!doesFolderExist(HGlobal.pathHRElocation)) {
			    		tabPane.setSelectedIndex(5);
			    		if (!confirmNewFolder(HGlobal.pathHRElocation)) {
			    				txt_HREloc.setBackground(Color.YELLOW);
			    				return;
			    				}
			    	}
				}
				if (changedPathReports) {
			    	if (!doesFolderExist(HGlobal.pathHREreports)) {
			    		tabPane.setSelectedIndex(5);
		    			if (!confirmNewFolder(HGlobal.pathHREreports)) {
		    					txt_HREreport.setBackground(Color.YELLOW);
		    					return;
		    					}
			    	}
				}
				if (changedPathBackups) {
			    	if (!doesFolderExist(HGlobal.pathHREbackups)) {
			    		tabPane.setSelectedIndex(5);
		    			if (!confirmNewFolder(HGlobal.pathHREbackups)) {
		    					txt_HREbackup.setBackground(Color.YELLOW);
		    					return;
		    					}
			    	}
				}
				if (changedPathExtBackups) {
			    	if (!doesFolderExist(HGlobal.pathExtbackups)) {
			    		tabPane.setSelectedIndex(5);
		    			if (!confirmNewFolder(HGlobal.pathExtbackups)) {
		    					txt_HREbackExt.setBackground(Color.YELLOW);
		    					return;
		    					}
			    	}
				}

			// Set data/report language codes for GUI, data, data2, rept
		    	String [] usedLangCodes = {HGlobal.nativeLanguage, dataLang, data2Lang, reptLang};
		    	// Perform Language resets just in case (even though might have been changed back to what they were before).
		    	// Following line drives HG0401Main resetGUILanguage (change menu language), then HA0001HREstart.setGUIlanguage() (NLS change)
		        if (guiLangChanged) pointToolHand.resetGUILanguage();

		        if (dataLangChanged) {
		        // Set boolean reload personSelect data
		        	if (openProject != null) openProject.setReloadPersonSelectData(true);
		        	dataLangChanged = true;
		        	if (openProject != null) openProject.setSelectedLanguage(usedLangCodes);
		        }

		        if (data2LangChanged) {
		        	data2LangChanged = true;
		        	if (openProject != null) openProject.setSelectedLanguage(usedLangCodes);
		        }
		        if (reptLangChanged) {
		        	reptLangChanged= true;
		        	if (openProject != null) openProject.setSelectedLanguage(usedLangCodes);
		        }

		   // If Date format changed
		        if (dateFormatChanged) {
					HGlobal.dateFormat = (String) combo_DateFormat.getSelectedItem(); // change global setting to selected format
					if (openProject != null) {
						pointPersonHandler = openProject.getPersonHandler();
						pointPersonHandler.newDateFormatSelected();
						pointPersonHandler.resetPersonManager();
						//pointPersonHandler.resetPersonSelect();
					}
		        }

		    // Accept setting of TCP server On/Off if it has changed
		        String serverStatus = "Server status is unknown";	//$NON-NLS-1$
		        if (tcpModeChanged) {
			        if (HGlobal.tcpServerOn) {
			        	serverStatus = pointToolHand.startTCPserver();
			        	if (HGlobal.DEBUG) System.out.println(serverStatus);
			        	// show start status via JOptionPane msg and in main Status Bar
						HGlobalCode.logTCPIPMessage(1, serverStatus);
			        	HG0401HREMain.mainFrame.setStatusAction(7);
			        } else
						if (pointToolHand.stopTCPserver()) {
							// show stop msg via JOptionPane msg and in main Status Bar
							HGlobalCode.logTCPIPMessage(2, "");		//$NON-NLS-1$
							HG0401HREMain.mainFrame.setStatusAction(8);
						} else {
							if (HGlobal.DEBUG) System.out.println(" Cannot stop TCP server - need to close projects first"); //$NON-NLS-1$
					    	JOptionPane.showMessageDialog(contents, HG0501Msgs.Text_139,
									HG0501Msgs.Text_140,JOptionPane.ERROR_MESSAGE);
					    	HGlobal.tcpServerOn = true;
						}
		        	}

		     // Writelogs
		        if (HGlobal.writeLogs)
		        	HB0711Logging.logWrite("Action: saving changes and exiting HG0501 App Settings"); //$NON-NLS-1$

		     // Write out all of UserAUX file and exit
				HB0744UserAUX.writeUserAUXfile();
				dispose();
			}
		});

		// Listener for Cancel button - as we don't know what settings might have been changed in HGlobal,
		// safest option is to reload them from the UserAUX file (but process Option settings ONLY)
		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (HGlobal.showCancelmsg)	{
					if (JOptionPane.showConfirmDialog(btn_Cancel, HG0501Msgs.Text_14, HG0501Msgs.Text_1,
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							if (HGlobal.writeLogs)
								HB0711Logging.logWrite("Action: cancelling changes in HG0501 App Settings"); //$NON-NLS-1$
							HB0744UserAUX.readUserAUXfile("OPT");	// yes option - reload (Options only) to wipe out changes and return to main menu //$NON-NLS-1$
							// User may have cancelled out of changing LAF/theme/font, so
							// we need to set the current GUI back to HGlobal values
							theme = HGlobal.lafTheme;
							selectedTheme = HGlobal.lafThemeNumber;
							selectedLaf = HGlobal.lafNumber;
							SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    setLookAndFeel(true);
                                    setSelectedFont();
                                }
                            });
							// Close any reminder display
							 if (reminderDisplay != null) reminderDisplay.dispose();
							dispose();										 }
					} else {
						if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: cancelling changes in HG0501 App Settings"); //$NON-NLS-1$
							HB0744UserAUX.readUserAUXfile("OPT"); //$NON-NLS-1$
				// close reminder display
					  if (reminderDisplay != null) reminderDisplay.dispose();
					 	dispose();
					 }
				}
		});

		// Listener for clicking 'X' on screen - make same as Cancel button
 		addWindowListener(new WindowAdapter() {
 		    @Override
			public void windowClosing(WindowEvent e) {
 		    	btn_Cancel.doClick();
 		    }
 			});

/***************************************************************
 * EXECUTION CODE - GET ACTUAL SETTINGS FROM HGLOBAL AT STARTUP
 **************************************************************/
 		// do radio buttons
 		if (HGlobal.tcpServerOn) serverOn.setSelected(true);
 			else serverOff.setSelected(true);
		if (HGlobal.accentOn) accentFirst.setSelected(true);
			else accentOff.setSelected(true);
 		// do check boxes
		if (HGlobal.showWelcome) chkbx_Welcome.setSelected(true);
		if (HGlobal.showCancelmsg) chkbx_CancelMsgs.setSelected(true);
		if (HGlobal.openLastProject) chkbx_OpenLastProj.setSelected(true);
		if (HGlobal.backupActivProject) chkbx_BkupActProj.setSelected(true);
		if (HGlobal.pluginEnabled) chkbx_EnablePlugins.setSelected(true);
		if (HGlobal.reloadPS) chkbx_reloadPS.setSelected(true);
		if (HGlobal.promptMarrName) chkbx_promptMarried.setSelected(true);
		if (HGlobal.writeLogs) chkbx_WriteLogs.setSelected(true);
		if (HGlobal.DEBUG) chkbxSetDebugOn.setSelected(true);
		if (HGlobal.TIME) chkbxTime.setSelected(true);

	} // End HG0501 constructor

/**
 * Set the JTattoo Look and Feel and/or Theme
 * @param loadThemes
 */
    private void setLookAndFeel(boolean loadThemes) {
        try {
          if (!loadThemes)
              theme = (String)themeList.getSelectedValue();

          HGlobal.lafTheme = theme;
          HGlobal.lafThemeNumber = selectedTheme;
          HGlobal.lafNumber = selectedLaf;

          switch (selectedLaf) {
          // First set the theme of the look and feel, then the LAF. Theme must be done first as
          // there is some static initialising (color values etc) when calling setTheme.
          // As the theme variables are shared with all look and feels, then without
          // calling the setTheme method first, the look and feel will have wrong colors.
          // Third param of setTheme sets logo beside drop-down menus - "" to remove it.
              case 0 :		// LAF ACRYL
                  com.jtattoo.plaf.acryl.AcrylLookAndFeel.setTheme(theme, "", "HRE"); //$NON-NLS-1$ //$NON-NLS-2$
                  UIManager.setLookAndFeel("com.jtattoo.plaf.acryl.AcrylLookAndFeel"); //$NON-NLS-1$
          	      UIManager.put("PopupMenu.font", UIManager.getFont("MenuItem.font")); //$NON-NLS-1$ //$NON-NLS-2$
                  UIManager.put("DesktopIcon.width", 400);	//$NON-NLS-1$
                  break;
              case 1 :		// LAF_AERO
                  com.jtattoo.plaf.aero.AeroLookAndFeel.setTheme(theme, "", "HRE"); //$NON-NLS-1$ //$NON-NLS-2$
                  UIManager.setLookAndFeel("com.jtattoo.plaf.aero.AeroLookAndFeel"); //$NON-NLS-1$
          	      UIManager.put("PopupMenu.font", UIManager.getFont("MenuItem.font")); //$NON-NLS-1$ //$NON-NLS-2$
                  UIManager.put("DesktopIcon.width", 400);	//$NON-NLS-1$
                  break;
              case 2 :		// LAF_ALUMINIUM
                  com.jtattoo.plaf.aluminium.AluminiumLookAndFeel.setTheme(theme, "", "HRE"); //$NON-NLS-1$ //$NON-NLS-2$
                  UIManager.setLookAndFeel("com.jtattoo.plaf.aluminium.AluminiumLookAndFeel"); //$NON-NLS-1$
          	      UIManager.put("PopupMenu.font", UIManager.getFont("MenuItem.font")); //$NON-NLS-1$ //$NON-NLS-2$
                  UIManager.put("DesktopIcon.width", 400);	//$NON-NLS-1$
                  break;
              case 3 : 		 // LAF_BERNSTEIN
                  com.jtattoo.plaf.bernstein.BernsteinLookAndFeel.setTheme(theme, "", "HRE"); //$NON-NLS-1$ //$NON-NLS-2$
                  UIManager.setLookAndFeel("com.jtattoo.plaf.bernstein.BernsteinLookAndFeel"); //$NON-NLS-1$
          	      UIManager.put("PopupMenu.font", UIManager.getFont("MenuItem.font")); //$NON-NLS-1$ //$NON-NLS-2$
                  UIManager.put("DesktopIcon.width", 400);	//$NON-NLS-1$
                  break;
              case 4 : 		 // LAF_FAST
                  com.jtattoo.plaf.fast.FastLookAndFeel.setTheme(theme, "", "HRE"); //$NON-NLS-1$ //$NON-NLS-2$
                  UIManager.setLookAndFeel("com.jtattoo.plaf.fast.FastLookAndFeel"); //$NON-NLS-1$
          	      UIManager.put("PopupMenu.font", UIManager.getFont("MenuItem.font")); //$NON-NLS-1$ //$NON-NLS-2$
                  UIManager.put("DesktopIcon.width", 400);	//$NON-NLS-1$
                  break;
              case 5 :		// LAF_GRAPHITE
                  com.jtattoo.plaf.graphite.GraphiteLookAndFeel.setTheme(theme, "", "HRE"); //$NON-NLS-1$ //$NON-NLS-2$
                  UIManager.setLookAndFeel("com.jtattoo.plaf.graphite.GraphiteLookAndFeel"); //$NON-NLS-1$
          	      UIManager.put("PopupMenu.font", UIManager.getFont("MenuItem.font")); //$NON-NLS-1$ //$NON-NLS-2$
                  UIManager.put("DesktopIcon.width", 400);	//$NON-NLS-1$
                  break;
              case 6 :		// LAF_HIFI
                  com.jtattoo.plaf.hifi.HiFiLookAndFeel.setTheme(theme, "", "HRE"); //$NON-NLS-1$ //$NON-NLS-2$
                  UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel"); //$NON-NLS-1$
          	      UIManager.put("PopupMenu.font", UIManager.getFont("MenuItem.font")); //$NON-NLS-1$ //$NON-NLS-2$
                  UIManager.put("DesktopIcon.width", 400);	//$NON-NLS-1$
                  break;
              case 7 :		// LAF_LUNA
                  com.jtattoo.plaf.luna.LunaLookAndFeel.setTheme(theme, "", "HRE"); //$NON-NLS-1$ //$NON-NLS-2$
                  UIManager.setLookAndFeel("com.jtattoo.plaf.luna.LunaLookAndFeel"); //$NON-NLS-1$
                  UIManager.put("ProgressBar.foreground", Color.LIGHT_GRAY);  //$NON-NLS-1$
                  UIManager.put("DesktopIcon.width", 400);	//$NON-NLS-1$
                  break;
              case 8 :		// LAF_MCWIN
                  com.jtattoo.plaf.mcwin.McWinLookAndFeel.setTheme(theme, "", "HRE"); //$NON-NLS-1$ //$NON-NLS-2$
                  UIManager.setLookAndFeel("com.jtattoo.plaf.mcwin.McWinLookAndFeel"); //$NON-NLS-1$
          	      UIManager.put("PopupMenu.font", UIManager.getFont("MenuItem.font")); //$NON-NLS-1$ //$NON-NLS-2$
                  UIManager.put("DesktopIcon.width", 400);	//$NON-NLS-1$
                  break;
              case 9 :		// LAF_MINT
                  com.jtattoo.plaf.mint.MintLookAndFeel.setTheme(theme, "", "HRE"); //$NON-NLS-1$ //$NON-NLS-2$
                  UIManager.setLookAndFeel("com.jtattoo.plaf.mint.MintLookAndFeel"); //$NON-NLS-1$
          	      UIManager.put("PopupMenu.font", UIManager.getFont("MenuItem.font")); //$NON-NLS-1$ //$NON-NLS-2$
                  UIManager.put("DesktopIcon.width", 400);	//$NON-NLS-1$
                  break;
              case 10 :		// LAF_NOIRE
                  com.jtattoo.plaf.noire.NoireLookAndFeel.setTheme(theme, "", "HRE"); //$NON-NLS-1$ //$NON-NLS-2$
                  UIManager.setLookAndFeel("com.jtattoo.plaf.noire.NoireLookAndFeel"); //$NON-NLS-1$
          	      UIManager.put("PopupMenu.font", UIManager.getFont("MenuItem.font")); //$NON-NLS-1$ //$NON-NLS-2$
                  UIManager.put("DesktopIcon.width", 400);	//$NON-NLS-1$
                  break;
              case 11 :		// LAF_SMART
                  com.jtattoo.plaf.smart.SmartLookAndFeel.setTheme(theme, "", "HRE"); //$NON-NLS-1$ //$NON-NLS-2$
                  UIManager.setLookAndFeel("com.jtattoo.plaf.smart.SmartLookAndFeel"); //$NON-NLS-1$
          	      UIManager.put("PopupMenu.font", UIManager.getFont("MenuItem.font")); //$NON-NLS-1$ //$NON-NLS-2$
                  UIManager.put("DesktopIcon.width", 400);	//$NON-NLS-1$
                  break;
              case 12 :		// LAF_TEXTURE
                  com.jtattoo.plaf.texture.TextureLookAndFeel.setTheme(theme, "", "HRE"); //$NON-NLS-1$ //$NON-NLS-2$
                  UIManager.setLookAndFeel("com.jtattoo.plaf.texture.TextureLookAndFeel"); //$NON-NLS-1$
          	      UIManager.put("PopupMenu.font", UIManager.getFont("MenuItem.font")); //$NON-NLS-1$ //$NON-NLS-2$
                  UIManager.put("DesktopIcon.width", 400);	//$NON-NLS-1$
                  break;
          }
          // Reset all visible windows for new LAF/theme
          Window windows[] = Window.getWindows();
          for (Window window : windows) {
              if (window.isDisplayable())
                  SwingUtilities.updateComponentTreeUI(window);
          }
          // If loadThemes true we have changed the LAF,
          // so need to reload the Themes for that LAF
          if (loadThemes)  fillThemeList();
          // (Re)set selected font (as JTattoo always uses Dialog)
          setSelectedFont();
          // Scroll all lists to make selected item visible
          scrollSelectedToVisible(lafList);
          scrollSelectedToVisible(themeList);
          scrollSelectedToVisible(fontList);
          // Re-pack screen (as a font size setting may have changed it)
          pack();
          // Reset default Theme for next time
          theme = "Default";		//$NON-NLS-1$
      }
      catch (Exception ex) {
          ex.printStackTrace();
      }
  } // End setLookAndFeel

/**
 * Fill the list of valid Themes from the JTattoo LAF code
 */
    private void fillThemeList() {
        // We don't want to get changed events while setting up the new theme
        // so we remove the selection listener
        themeList.removeListSelectionListener(themeListener);
        // Setup the theme list with data from the look and feel classes
        LookAndFeel laf = UIManager.getLookAndFeel();
        if (laf instanceof com.jtattoo.plaf.acryl.AcrylLookAndFeel) {
            themeList.setListData(com.jtattoo.plaf.acryl.AcrylLookAndFeel.getThemes().toArray());
        } else if (laf instanceof com.jtattoo.plaf.aero.AeroLookAndFeel) {
            themeList.setListData(com.jtattoo.plaf.aero.AeroLookAndFeel.getThemes().toArray());
        } else if (laf instanceof com.jtattoo.plaf.aluminium.AluminiumLookAndFeel) {
            themeList.setListData(com.jtattoo.plaf.aluminium.AluminiumLookAndFeel.getThemes().toArray());
        } else if (laf instanceof com.jtattoo.plaf.bernstein.BernsteinLookAndFeel) {
            themeList.setListData(com.jtattoo.plaf.bernstein.BernsteinLookAndFeel.getThemes().toArray());
        } else if (laf instanceof com.jtattoo.plaf.fast.FastLookAndFeel) {
            themeList.setListData(com.jtattoo.plaf.fast.FastLookAndFeel.getThemes().toArray());
        } else if (laf instanceof com.jtattoo.plaf.graphite.GraphiteLookAndFeel) {
            themeList.setListData(com.jtattoo.plaf.graphite.GraphiteLookAndFeel.getThemes().toArray());
        } else if (laf instanceof com.jtattoo.plaf.hifi.HiFiLookAndFeel) {
            themeList.setListData(com.jtattoo.plaf.hifi.HiFiLookAndFeel.getThemes().toArray());
        } else if (laf instanceof com.jtattoo.plaf.luna.LunaLookAndFeel) {
            themeList.setListData(com.jtattoo.plaf.luna.LunaLookAndFeel.getThemes().toArray());
        } else if (laf instanceof com.jtattoo.plaf.mcwin.McWinLookAndFeel) {
            themeList.setListData(com.jtattoo.plaf.mcwin.McWinLookAndFeel.getThemes().toArray());
        } else if (laf instanceof com.jtattoo.plaf.mint.MintLookAndFeel) {
            themeList.setListData(com.jtattoo.plaf.mint.MintLookAndFeel.getThemes().toArray());
        } else if (laf instanceof com.jtattoo.plaf.noire.NoireLookAndFeel) {
            themeList.setListData(com.jtattoo.plaf.noire.NoireLookAndFeel.getThemes().toArray());
        } else if (laf instanceof com.jtattoo.plaf.smart.SmartLookAndFeel) {
            themeList.setListData(com.jtattoo.plaf.smart.SmartLookAndFeel.getThemes().toArray());
        } else if (laf instanceof com.jtattoo.plaf.texture.TextureLookAndFeel) {
            themeList.setListData(com.jtattoo.plaf.texture.TextureLookAndFeel.getThemes().toArray());
        } else {
            themeList.setListData((Object[])null);
        }
        if (UIManager.getLookAndFeel() instanceof AbstractLookAndFeel) {
            themeList.setSelectedValue("Default", true); //$NON-NLS-1$
        }
        selectedTheme = themeList.getSelectedIndex();
        // Add back in the selection listener we have removed above
        themeList.addListSelectionListener(themeListener);
    } // End fillThemeList

/**
 * Sets the selected font across all Windows
 */
    public void setSelectedFont() {
        // Because the JTattoo code always sets font=Dialog,
        // we must have a method of resetting the font to the selected
    	// font if the LAF Theme is changed. This it.
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
        // Reset all visible windows for new font
        Window windows[] = Window.getWindows();
        for (Window window : windows) {
             if (window.isDisplayable())
                 SwingUtilities.updateComponentTreeUI(window);
         }
        // Ensure selected items visible in new lists
        scrollSelectedToVisible(themeList);
        scrollSelectedToVisible(fontList);
    } // End setSelectedFont

/**
 * Scroll the look & feel lists so selected item visible
 * @param list
 */
    private void scrollSelectedToVisible(JList<?> list) {
        // Because of the different font sizes, the selected item
        // may be out of the visible area. So we correct this.
        int idx = list.getLeadSelectionIndex();
        Rectangle rect = list.getCellBounds(idx, idx);
        if (rect != null)
            list.scrollRectToVisible(rect);
    } // End scrollSelectedToVisible

/**
 * Checks if a string is a valid path (all OS's)
 * @param path
 */
    private static boolean isValidPath(String path) {
    	// strip off any filename at the end
    	int i = path.lastIndexOf('.');
    	path = (i > -1) ? path.substring(0, i) : path;
    	// then check for a valid path
        try {
            Paths.get(path);
        } catch (InvalidPathException | NullPointerException ex) {
              return false;
        }
        return true;
    }	// End isValidPath

/**
 * Check if designated folder exists or location is null/empty
 * @param location is the full folder path
 */
    private static boolean doesFolderExist(String location) {
    	if(location == null || location.trim().isEmpty()) return true;
    	File dir = new File(location);
    	if (dir.exists()) return true;
		return false;
    }  	// End doesFolderExist

/**
 * Check if user wants to create the designated new Folder
 * @param location is the full folder path
 */
    private boolean confirmNewFolder (String location) {
    	// Message reads 'Folder xxxx does not exist. Do you want to create it?'
    	// Answer of No allows user to edit the entry, answer Yes means we create the folder
  		if(JOptionPane.showConfirmDialog(contents, HG0501Msgs.Text_141 + location + HG0501Msgs.Text_142 +
  				 								HG0501Msgs.Text_143, HG0501Msgs.Text_144,
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
  			 			File dir = new File(location);
  			 			dir.mkdirs();
  			 			return true;
					}
		return false;
    } 	// End confirmNewFolder

}  // End HG0501AppSettings