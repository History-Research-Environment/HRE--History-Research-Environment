package hre.gui;
/****************************************************************************************
 * Edit Date - Specification 05.90 GUI_EditDate
 * v0.03.0030 2023-08-21 first draft (D Ferguson)
 * v0.03.0031 2024-02-19 add era, time, 2nd date area qualifier, etc (D Ferguson)
 * 			  2024-03-29 match d/m/y field order to user's reqd date format (D Ferguson)
 * 			  2024-04-23 modifed parameter names for clarity, added noQualifier (D Ferguson)
 * 			  2024-05-01 add validation tests for calendar compatibility (D Ferguson)
 * 			  2024-05-03 setup for conversion to output format (D Ferguson)
 * 			  2024-07-14 added SortString creation (D Ferguson)
 * 			  2024-07-14 modified SortString creation (N Tolleshaug)
 * 			  2024-07-17 fixed logic error in checking 2nd date after 1st (D Ferguson)
 * 			  2024-07-18 load month names table from T204/510 (N Tolleshaug)
 * 			  2024-07-20 load qualifier terms tables from T204/560 (D Ferguson)
 * 			  2024-07-21 NLS conversion (D Ferguson)
 * 			  2024-07-24 Updated for use of HG0590EditDate in HRE (N Tolleshaug)
 * 			  2024-08-07 Updated to handle BC dates correctly (D Ferguson)
 * 			  2024-08-11 At startup, set mouse to first date entry field (D Ferguson)
 * 			  2024-10-01 Organize imports, clean dead code (D Ferguson)
 * *************************************************************************************
 * Notes on incomplete functionality:
 * Does not yet support different calendar types
 ***************************************************************************************/

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Year;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import hre.nls.HG0590Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Edit Date
 * @author D Ferguson
 * @version v0.03.0031
 * @since 2023-08-21
 */

public class HG0590EditDate extends JDialog {
	private static final long serialVersionUID = 001L;
	// int dateType values 1 = start, 2 = end, 3 = sort,
	//  and codes date/sort for birth, bapt, death and burial
	public int dateType = 1;
	private JPanel contents;
	private String returnDate = "";	//$NON-NLS-1$
	HG0450SuperDialog pointOpenDisplay;

	// Set default data for the date fields of T170
	long dateYear = 0;
	String dateDetails = "N"; 	//$NON-NLS-1$
	long date2Year = 0;
	String date2Details = "0"; 	//$NON-NLS-1$
	String sortString = "11";	// Set start numbers //$NON-NLS-1$
	long dateYearCE = 0;
	long date2YearCE = 0;

	int dateMonth, dateDay, dateHr, dateMin, dateSec, dateMSec;
	int date2Month, date2Day, date2Hr, date2Min, date2Sec, date2MSec;

	String errorCode = "";	//$NON-NLS-1$

	String extract;
	JRadioButton radioCE, radioBC, radioCO, radioYA;
	JTextField txt_Day, txt_Month, txt_Year, txt_Hr, txt_Min, txt_Sec, txt_mSec,
			   txt_Day2, txt_Month2, txt_Year2, txt_Hr2, txt_Min2, txt_Sec2, txt_mSec2,
			   txt_Irreg;
	JLabel lbl_Qualify2;
	JCheckBox chk_Q;
	JComboBox<String> combo_Qualify, combo_Month, combo_Month2;
	String[] mths;		// built from T204, type 510
	int selectedQualifier = 0;
	String qualifierCodes = "XCSEBAWOF";	// DO NOT change the order of these letters!!!  //$NON-NLS-1$
	boolean timeFields;
	boolean dateTimeCleared = false;
	boolean monthMMM = false;
	boolean leapYear = false;
	boolean leap2Year = false;
	boolean timeIsPresent = false;

	JButton btn_Save;
	JButton btn_addTime;

/**
 * Create the dialog.
 * @time		if false, the Time entry fields are not displayed
 * @noDate2		if true, the Qualifer combobox entries for 'from', 'either', 'between' are not added
 * @noQualifier if true, the Qualifer combobox and Query checkbox are not displayed
 * @irregular	if false, the Irregular date entry field is not displayed
 */
	public HG0590EditDate (HG0450SuperDialog pointOpenDisplay, boolean time, boolean noDate2, boolean noQualifier, boolean irregular) {
		timeFields = time;
		this.pointOpenDisplay = pointOpenDisplay;

		// Make dialog undecorated (no title bar)
		setUndecorated(true);
		getRootPane().setWindowDecorationStyle(JRootPane.NONE);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		// Set up month translation table from table T204, type 510, abbreviations
		// Must start with a blank month so user can avoid selecting a month
		String[] abbrevMonths = pointOpenDisplay.pointOpenProject.getPersonHandler().setTranslatedData("00000", "510", true); //$NON-NLS-1$ //$NON-NLS-2$
		mths = new String[13];
		for (int i = 0; i < mths.length; i++) {
			if (i == 0) mths[i] = "";			//$NON-NLS-1$
			else mths[i] = abbrevMonths[i-1];
		}

		// Setup contentpane
		contents = new JPanel();
		setContentPane(contents);
		// Add a border
		contents.setBorder(new EtchedBorder(EtchedBorder.LOWERED, Color.BLACK, Color.BLACK));
		contents.setLayout(new MigLayout("insets 10", "[]5[]5[]5[]5[]5[]5[]5[]", "[]10[][][]10[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		// Define Era area
		JLabel lbl_Era = new JLabel(HG0590Msgs.Text_0);	// Date Era:
		contents.add(lbl_Era, "cell 0 0, center");	//$NON-NLS-1$
		radioCE = new JRadioButton("CE");			//$NON-NLS-1$
		radioCE.setToolTipText(HG0590Msgs.Text_1);  // Common Era
		radioCE.setSelected(true);
		contents.add(radioCE, "cell 1 0, center");	//$NON-NLS-1$
		radioBC = new JRadioButton("BC");			//$NON-NLS-1$
		radioBC.setToolTipText(HG0590Msgs.Text_2);  // Before Common Era
		contents.add(radioBC, "cell 2 0, center");	//$NON-NLS-1$
		radioCO = new JRadioButton("OS");			//$NON-NLS-1$
		radioCO.setToolTipText(HG0590Msgs.Text_3);	// Common Era with Old Style years
		contents.add(radioCO, "cell 3 0, center");	//$NON-NLS-1$
		radioYA = new JRadioButton("YA");			//$NON-NLS-1$
		radioYA.setToolTipText(HG0590Msgs.Text_4);	// Many Years Ago
		contents.add(radioYA, "cell 4 0, center");	//$NON-NLS-1$

		ButtonGroup eraGroup = new ButtonGroup();
		eraGroup.add(radioCE);
		eraGroup.add(radioBC);
		eraGroup.add(radioCO);
		eraGroup.add(radioYA);

		// Define Date/Time labels
		JLabel lbl_Qualify = new JLabel(HG0590Msgs.Text_5);	// Qualifier
		contents.add(lbl_Qualify, "cell 0 1, center");		//$NON-NLS-1$
		JLabel lbl_Day = new JLabel(HG0590Msgs.Text_6);		// Day
		JLabel lbl_Month = new JLabel(HG0590Msgs.Text_7);	// Month
		JLabel lbl_Year = new JLabel(HG0590Msgs.Text_8);	// Year
		JLabel lbl_Q = new JLabel("  ?");			//$NON-NLS-1$
		contents.add(lbl_Q, "cell 4 1, left");		//$NON-NLS-1$

		// Define Hr/Min/Secs/mSecs labels
		JLabel lbl_Hr = new JLabel(HG0590Msgs.Text_9);			// Hrs
		contents.add(lbl_Hr, "cell 6 1, center, hidemode 3");	//$NON-NLS-1$
		JLabel lbl_Min = new JLabel(HG0590Msgs.Text_10);		// Mins
		contents.add(lbl_Min, "cell 7 1, center, hidemode 3");	//$NON-NLS-1$
		JLabel lbl_Sec = new JLabel(HG0590Msgs.Text_11);		// Secs
		contents.add(lbl_Sec, "cell 8 1, center, hidemode 3");	//$NON-NLS-1$
		JLabel lbl_mSec = new JLabel(HG0590Msgs.Text_12);		// mSecs
		contents.add(lbl_mSec, "cell 9 1, center, hidemode 3");	//$NON-NLS-1$

		// Setup the Date qualifier combobox
		//WARNING: do not change the order of combo items - must match qualifierCodes order
		//AND Qualifier order MUST match the order of the qualifier terms in translation table T204/560
		// Load qualifier terms from table T204, type 560
		String[] qualTerms = pointOpenDisplay.pointOpenProject.getPersonHandler().setTranslatedData("00000", "560", false); //$NON-NLS-1$ //$NON-NLS-2$
		// Adjust the first term
		qualTerms[0] = HG0590Msgs.Text_13;	// at
		// Items 6-8 are multiple terms (like "between/and") and need to be split and adjusted
		String[] qualTerms2 = new String[3];
		String[] splitter = qualTerms[6].split("/"); //$NON-NLS-1$ // splits between/and
		qualTerms[6] = splitter[0];
		qualTerms2[0] = splitter[1];
		splitter = qualTerms[7].split("/"); //$NON-NLS-1$          // splits either/or
		qualTerms[7] = splitter[0];
		qualTerms2[1] = splitter[1];
		splitter = qualTerms[8].split("/"); //$NON-NLS-1$         // splits from/to
		qualTerms[8] = splitter[0];
		qualTerms2[2] = splitter[1];
		// Set the qualifier combo-box entries for at,circa,say,estimated,before,after
		combo_Qualify = new JComboBox<String>();
		for (int i = 0; i < 6; i++)
			combo_Qualify.addItem(qualTerms[i]);
		// and possibly the entries for between, either, from
		if (noDate2 == false) {
			for (int i = 6; i < 9; i++)
				combo_Qualify.addItem(qualTerms[i]);
		}
		combo_Qualify.setSelectedItem(0);
		combo_Qualify.setMaximumRowCount(9);
		contents.add(combo_Qualify, "cell 0 2, center, hidemode 3");	//$NON-NLS-1$
		// Suppress the combobox if it is not allowed
		if (noQualifier) {
			lbl_Qualify.setVisible(false);
			combo_Qualify.setVisible(false);
		}

		// Define the D/M/Y date entry fields
		txt_Day = new JTextField();
		txt_Day.setColumns(4);
		txt_Day.setDocument (new textLimit(2));
		txt_Day.setHorizontalAlignment(JTextField.CENTER);
		txt_Month = new JTextField();
		txt_Month.setColumns(4);
		txt_Month.setDocument (new textLimit(2));
		txt_Month.setHorizontalAlignment(JTextField.CENTER);
		txt_Year = new JTextField();
		txt_Year.setColumns(4);
		txt_Year.setDocument (new textLimit(4));
		txt_Year.setHorizontalAlignment(JTextField.CENTER);
		combo_Month = new JComboBox<String>(mths);
		combo_Month.setMaximumRowCount(13);
		combo_Month2 = new JComboBox<String>(mths);
		combo_Month2.setMaximumRowCount(13);
		combo_Month2.setVisible(false);

		// Define the Query checkbox
		chk_Q = new JCheckBox();
		contents.add(chk_Q, "cell 4 2, left");	//$NON-NLS-1$
		// Suppress this checkbox if noQualifier allowed
		if (noQualifier) {
			lbl_Q.setVisible(false);
			chk_Q.setVisible(false);
		}

		// Define the Hr/Min/Sec/mSec data entry fields
		JLabel lbl_At = new JLabel(HG0590Msgs.Text_14);		// at:
		contents.add(lbl_At, "cell 5 2, alignx right, hidemode 3");		//$NON-NLS-1$
		txt_Hr = new JTextField();
		txt_Hr.setColumns(4);
		txt_Hr.setDocument (new textLimit(2));
		txt_Hr.setHorizontalAlignment(JTextField.CENTER);
		contents.add(txt_Hr, "cell 6 2, center, hidemode 3");	//$NON-NLS-1$
		txt_Min = new JTextField();
		txt_Min.setColumns(4);
		txt_Min.setDocument (new textLimit(2));
		txt_Min.setHorizontalAlignment(JTextField.CENTER);
		contents.add(txt_Min, "cell 7 2, center, hidemode 3");	//$NON-NLS-1$
		txt_Sec = new JTextField();
		txt_Sec.setColumns(4);
		txt_Sec.setDocument (new textLimit(2));
		txt_Sec.setHorizontalAlignment(JTextField.CENTER);
		contents.add(txt_Sec, "cell 8 2, center, hidemode 3");	//$NON-NLS-1$
		txt_mSec = new JTextField();
		txt_mSec.setColumns(4);
		txt_mSec.setDocument (new textLimit(3));
		txt_mSec.setHorizontalAlignment(JTextField.CENTER);
		contents.add(txt_mSec, "cell 9 2, left, hidemode 3");	//$NON-NLS-1$

		// Define alternate Date/Time data areas
		lbl_Qualify2 = new JLabel();
		lbl_Qualify2.setVisible(false);
		contents.add(lbl_Qualify2, "cell 0 3, right, hidemode 3");	//$NON-NLS-1$
		txt_Day2 = new JTextField();
		txt_Day2.setColumns(4);
		txt_Day2.setDocument (new textLimit(2));
		txt_Day2.setHorizontalAlignment(JTextField.CENTER);
		txt_Day2.setVisible(false);
		txt_Month2 = new JTextField();
		txt_Month2.setColumns(4);
		txt_Month2.setDocument (new textLimit(2));
		txt_Month2.setHorizontalAlignment(JTextField.CENTER);
		txt_Month2.setVisible(false);

		txt_Year2 = new JTextField();
		txt_Year2.setColumns(4);
		txt_Year2.setDocument (new textLimit(4));
		txt_Year2.setHorizontalAlignment(JTextField.CENTER);
		txt_Year2.setVisible(false);
		txt_Hr2 = new JTextField();
		txt_Hr2.setColumns(4);
		txt_Hr2.setDocument (new textLimit(2));
		txt_Hr2.setHorizontalAlignment(JTextField.CENTER);
		txt_Hr2.setVisible(false);
		contents.add(txt_Hr2, "cell 6 3, center, hidemode 3");	//$NON-NLS-1$
		txt_Min2 = new JTextField();
		txt_Min2.setColumns(4);
		txt_Min2.setDocument (new textLimit(2));
		txt_Min2.setHorizontalAlignment(JTextField.CENTER);
		txt_Min2.setVisible(false);
		contents.add(txt_Min2, "cell 7 3, center, hidemode 3");	//$NON-NLS-1$
		txt_Sec2 = new JTextField();
		txt_Sec2.setColumns(4);
		txt_Sec2.setDocument (new textLimit(2));
		txt_Sec2.setHorizontalAlignment(JTextField.CENTER);
		txt_Sec2.setVisible(false);
		contents.add(txt_Sec2, "cell 8 3, center, hidemode 3");	//$NON-NLS-1$
		txt_mSec2 = new JTextField();
		txt_mSec2.setColumns(4);
		txt_mSec2.setDocument (new textLimit(3));
		txt_mSec2.setHorizontalAlignment(JTextField.CENTER);
		txt_mSec2.setVisible(false);
		contents.add(txt_mSec2, "cell 9 3, left, hidemode 3");	//$NON-NLS-1$

		// Based on the HGlobal date format, set the day/mth/year fields into
		// the correct cell locations in the layout
		// Valid date formatting codes in HRE are:
		//		"dd.mm.yyyy"
		//		"dd-mm-yyyy"
		//		"dd/mm/yyyy"
		//		"dd Mmm yyyy"
		//		"dd MMM yyyy"
		//		"yyyy.mm.dd"
		//		"yyyy-mm-dd"
		//		"Mmm dd, yyyy"
		//		"MMM dd, yyyy"
		//		"mm/dd/yyyy"
		// Also set the mouse cursor into whatever field is at 'cell 1 2'
		// First check if the Month format is non-numeric, in which
		// case we need to set the monthMMM boolean correctly
		if (HGlobal.dateFormat.contains("Mmm") || HGlobal.dateFormat.contains("MMM")) monthMMM = true; 	//$NON-NLS-1$	//$NON-NLS-2$
		// Now check for dateFormat starting with Year (so assume Y/M/D)
		if (HGlobal.dateFormat.startsWith("y") || HGlobal.dateFormat.startsWith("Y")) {	//$NON-NLS-1$	//$NON-NLS-2$
			contents.add(lbl_Day, "cell 3 1, center");		//$NON-NLS-1$
			contents.add(lbl_Month, "cell 2 1, center");	//$NON-NLS-1$
			contents.add(lbl_Year, "cell 1 1, center");		//$NON-NLS-1$
			contents.add(txt_Day, "cell 3 2");	//$NON-NLS-1$
			if (monthMMM) contents.add(combo_Month, "cell 2 2, center");	//$NON-NLS-1$
				else contents.add(txt_Month, "cell 2 2");	//$NON-NLS-1$
			contents.add(txt_Year, "cell 1 2");		//$NON-NLS-1$
			contents.add(txt_Day2, "cell 3 3, center, hidemode 3");	//$NON-NLS-1$
			if (monthMMM) contents.add(combo_Month2, "cell 2 3, center, hidemode 3");	//$NON-NLS-1$
				else contents.add(txt_Month2, "cell 2 3, center, hidemode 3");	//$NON-NLS-1$
			contents.add(txt_Year2, "cell 1 3, center, hidemode 3");		//$NON-NLS-1$
			// Set cursor at txt_Year when dialog loads
			EventQueue.invokeLater( () -> {txt_Year.requestFocusInWindow(); txt_Year.setCaretPosition(0);});
			}
		// Next, check if dateFormat starts with Mth (so assume M/D/Y)
		else if (HGlobal.dateFormat.startsWith("m") || HGlobal.dateFormat.startsWith("M")) {	//$NON-NLS-1$	//$NON-NLS-2$
				contents.add(lbl_Day, "cell 2 1, center");		//$NON-NLS-1$
				contents.add(lbl_Month, "cell 1 1, center");	//$NON-NLS-1$
				contents.add(lbl_Year, "cell 3 1, center");		//$NON-NLS-1$
				contents.add(txt_Day, "cell 2 2");	//$NON-NLS-1$
				if (monthMMM) contents.add(combo_Month, "cell 1 2, center");	//$NON-NLS-1$
					else contents.add(txt_Month, "cell 1 2");	//$NON-NLS-1$
				contents.add(txt_Year, "cell 3 2");		//$NON-NLS-1$
				contents.add(txt_Day2, "cell 2 3, center, hidemode 3");	//$NON-NLS-1$
				if (monthMMM) contents.add(combo_Month2, "cell 1 3, center, hidemode 3");	//$NON-NLS-1$
					else contents.add(txt_Month2, "cell 1 3, center, hidemode 3");	//$NON-NLS-1$
				contents.add(txt_Year2, "cell 3 3, center, hidemode 3");		//$NON-NLS-1$
				// Set cursor at txt_Month when dialog loads
				EventQueue.invokeLater( () -> {txt_Month.requestFocusInWindow(); txt_Month.setCaretPosition(0);});
		}
		// lastly, default to assuming format is D/M/Y
		else {
			contents.add(lbl_Day, "cell 1 1, center");		//$NON-NLS-1$
			contents.add(lbl_Month, "cell 2 1, center");	//$NON-NLS-1$
			contents.add(lbl_Year, "cell 3 1, center");		//$NON-NLS-1$
			contents.add(txt_Day, "cell 1 2");	//$NON-NLS-1$
			if (monthMMM) contents.add(combo_Month, "cell 2 2, center");	//$NON-NLS-1$
				else contents.add(txt_Month, "cell 2 2");	//$NON-NLS-1$
			contents.add(txt_Year, "cell 3 2");		//$NON-NLS-1$
			contents.add(txt_Day2, "cell 1 3, center, hidemode 3");	//$NON-NLS-1$
			if (monthMMM) contents.add(combo_Month2, "cell 2 3, center, hidemode 3");	//$NON-NLS-1$
				else contents.add(txt_Month2, "cell 2 3, center, hidemode 3");	//$NON-NLS-1$
			contents.add(txt_Year2, "cell 3 3, center, hidemode 3");		//$NON-NLS-1$
			// Set cursor at txt_Day when dialog loads
			EventQueue.invokeLater( () -> {txt_Day.requestFocusInWindow(); txt_Day.setCaretPosition(0);});
		}

		// Define Irregular date area
		JLabel lbl_Irreg = new JLabel(HG0590Msgs.Text_15);		// OR Irregular
		contents.add(lbl_Irreg, "cell 0 4, hidemode 3");		//$NON-NLS-1$
		txt_Irreg = new JTextField();
		txt_Irreg.setDocument (new textLimit(50));
		txt_Irreg.setHorizontalAlignment(JTextField.LEFT);
		contents.add(txt_Irreg, "cell 1 4 9 1, grow, hidemode 3");	//$NON-NLS-1$
		// Hide fields if irregular date not required
		if (!irregular) {
			lbl_Irreg.setVisible(false);
			txt_Irreg.setVisible(false);
		}

		// Adjust the Time field's visibility based on the 'time' parameter
		if (!time) {
			lbl_At.setVisible(false);
			lbl_Hr.setVisible(false);
			lbl_Min.setVisible(false);
			lbl_Sec.setVisible(false);
			lbl_mSec.setVisible(false);
			txt_Hr.setVisible(false);
			txt_Min.setVisible(false);
			txt_Sec.setVisible(false);
			txt_mSec.setVisible(false);
			txt_Hr2.setVisible(false);
			txt_Min2.setVisible(false);
			txt_Sec2.setVisible(false);
			txt_mSec2.setVisible(false);
		}
		// Set control buttons
		btn_addTime = new JButton(HG0590Msgs.Text_16);		// +Time
		contents.add(btn_addTime, "cell 0 5"); //$NON-NLS-1$
		// Don't show this button if Time fields already showing
		if (time) btn_addTime.setVisible(false);

		JButton btn_Cancel = new JButton(HG0590Msgs.Text_17);	// Cancel
		contents.add(btn_Cancel, "cell 1 5 9, alignx right, gapx 20, tag cancel"); //$NON-NLS-1$

		btn_Save = new JButton(HG0590Msgs.Text_18);		// Save & Close
		contents.add(btn_Save, "tag ok, cell 1 5 9, alignx right, gapx 20");	 //$NON-NLS-1$

		pack();

/***
 * CREATE ACTION LISTENERS
 **/
		// Listener for selection of entries in combo_Qualify
		combo_Qualify.addActionListener (new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				selectedQualifier = combo_Qualify.getSelectedIndex();
				if (selectedQualifier < 6) {	// make 2nd date entry row invisible
					lbl_Qualify2.setVisible(false);
					txt_Day2.setVisible(false);
					txt_Month2.setVisible(false);
					combo_Month2.setVisible(false);
					txt_Year2.setVisible(false);
					txt_Hr2.setVisible(false);
					txt_Min2.setVisible(false);
					txt_Sec2.setVisible(false);
					txt_mSec2.setVisible(false);
				}
				if (selectedQualifier == 6) lbl_Qualify2.setText(qualTerms2[0]);	// 'between' was selected so set "and"
				if (selectedQualifier == 7) lbl_Qualify2.setText(qualTerms2[1]);	// 'either' was selected so set "or"
				if (selectedQualifier == 8) lbl_Qualify2.setText(qualTerms2[2]);	// 'from' was selected so set "to"
				if (selectedQualifier >= 6)	{	// make 2nd date/time entry row visible
					lbl_Qualify2.setVisible(true);
					txt_Day2.setVisible(true);
					if (monthMMM) combo_Month2.setVisible(true);
						else txt_Month2.setVisible(true);
					txt_Year2.setVisible(true);
					// Only make time visible if 'time' = true
					if (timeFields == true) {
						txt_Hr2.setVisible(true);
						txt_Min2.setVisible(true);
						txt_Sec2.setVisible(true);
						txt_mSec2.setVisible(true);
					}
				}
				// and re-pack the screen
				pack();
			}
		});

		// Listener for +Time button
		// This allows user to add the Time entry areas if screen did not initially show them
		btn_addTime.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				lbl_At.setVisible(true);
				lbl_Hr.setVisible(true);
				lbl_Min.setVisible(true);
				lbl_Sec.setVisible(true);
				lbl_mSec.setVisible(true);
				txt_Hr.setVisible(true);
				txt_Min.setVisible(true);
				txt_Sec.setVisible(true);
				txt_mSec.setVisible(true);
			// Make 2nd set of time fields visible if 2nd date/time row needed
				if (selectedQualifier >= 6) {
					txt_Hr2.setVisible(true);
					txt_Min2.setVisible(true);
					txt_Sec2.setVisible(true);
					txt_mSec2.setVisible(true);
				}
			// Once executed, remove button and repack
				btn_addTime.setVisible(false);
				timeFields = true;
				timeIsPresent = false;
				pack();
			}
		});

		// Listener for date entry in Irregular date field
		// If irregular date entered NONE of the other fields can be valid, so clear them
		txt_Irreg.addCaretListener(e -> {
			// Don't clear fields if already done
			if (!dateTimeCleared) {
				txt_Day.setText(null);
				txt_Month.setText(null);
				combo_Month.setSelectedIndex(0);
				txt_Year.setText(null);
				txt_Hr.setText(null);
				txt_Min.setText(null);
				txt_Sec.setText(null);
				txt_mSec.setText(null);
				txt_Day2.setText(null);
				txt_Month2.setText(null);
				combo_Month2.setSelectedIndex(0);
				txt_Year2.setText(null);
				txt_Hr2.setText(null);
				txt_Min2.setText(null);
				txt_Sec2.setText(null);
				txt_mSec2.setText(null);
				chk_Q.setSelected(false);
				dateTimeCleared = true;
			}
		});

		// Listener for Save button
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// If not an irregular date, validate the entered values; do not Save if errors found
				if (txt_Irreg.getText().length() == 0) {
					validateInput();
					if (errorCode.length() > 0) return;
				}

				// then convert them into T170-compatible fields
				convertToHDate();

				// Following are just demo return values for testing
				if (HGlobal.DEBUG) {
					if (dateDetails.substring(0,1).equals("I")) 		//$NON-NLS-1$
						 returnDate = " " + dateDetails + "  Irregular date: IR "+date2Details; 	//$NON-NLS-1$	//$NON-NLS-2$
					else returnDate = " " + txt_Year.getText() + "  " + dateDetails;  	//$NON-NLS-1$	//$NON-NLS-2$
					if (selectedQualifier >= 6) returnDate = returnDate + " / " + txt_Year2.getText() 	//$NON-NLS-1$
					+ "  " + date2Details;	//$NON-NLS-1$
					System.out.println(" ReturnDate: " + returnDate);	//$NON-NLS-1$
				}
				if (dateType == 1) pointOpenDisplay.saveStartDate();
				if (dateType == 2) pointOpenDisplay.saveEndDate();
				if (dateType == 3) pointOpenDisplay.saveSortDate();
				if (dateType == 4) pointOpenDisplay.saveBirthDate();
				if (dateType == 5) pointOpenDisplay.saveBirthSortDate();
				if (dateType == 6) pointOpenDisplay.saveBaptDate();
				if (dateType == 7) pointOpenDisplay.saveBaptSortDate();
				if (dateType == 8) pointOpenDisplay.saveDeathDate();
				if (dateType == 9) pointOpenDisplay.saveDeathSortDate();
				if (dateType == 10) pointOpenDisplay.saveBurialDate();
				if (dateType == 11) pointOpenDisplay.saveBurialSortDate();
				if (dateType == 12) pointOpenDisplay.savePartnerDate();
				if (dateType == 13) pointOpenDisplay.savePartnerSortDate();
				dispose();
			}
		});

		// Listener for Cancel button
		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				returnDate = "Cancelled";		//$NON-NLS-1$
				dispose();
			}
		});

	}	// End HG0590EditDate constructor

/******************
 * CALLED ROUTINES
 *****************/
/**
 * returnXXXX - to return constructed date strings to caller
 */
	public String returnDateToGUI() {
	      return returnDate;
	}	// end of returnDateToGUI

	public long  returnYearToGUI() {
	      return dateYear;
	}	// end of returnYearToGUI

	public String returnDetailToGUI() {
	      return dateDetails;
	}	// end of returnDetailToGUI

	public long  returnYear2ToGUI() {
	      return date2Year;
	}	// end of returnYear2ToGUI

	public String returnDetail2ToGUI() {
	      return date2Details;
	}	// end of returnDetail2ToGUI

	public String returnSortString() {
	      return sortString;
	}	// end of returnSortString

/**
 * convertFromHDate
 * @dateYears		a Long with T170 MAIN_YEARS value
 * @dateDetails		a String with T170 MAIN_DETAILS values
 * @date2Years		a Long with T170 EXTRA_YEARS value
 * @date2Details	a String with T170 EXTRA_DETAILS values
 */
	public void convertFromHDate(long dateYears, String dateDetails, long date2Years, String date2Details) {
	// Decode the provided HDate data and put all the values into this GUI's fields
	// T170 HDate formats are as follows:
	// T170 MAIN_YEARS BIGINT
	// T170 MAIN_DETAILS CHAR(24). Unused fields are filled with '%'.
	//		Origin in position 0, normally N; if = "I" is irregular date
	//		Calendar type, position 1-2, normally "CE"; if "CO" = Old Style, "BC" = bef.CE, "YA" = mYrs ago
	// 		Months in positions 3-4
	//		Days in positions 5-6
	//		Hours in positions 7-8
	//		Minutes in positions 9-10
	//		Seconds in positions 11-12
	//		mSecs in positions 13-15
	//		Qualifier in position 16, X=exact; C=circa; S=say; E=estimated; B=before; A=after; W=between/and; O=either/or; F=from/to
	//		Question-mark in position 17 (Y or N)
	//		Offset code in position 18-19
	//		Offset units in position 20-22
	//		Spare at position 23
	// T170 EXTRA_YEARS BIGINT (either 0 or a secondary year)
	// T170 EXTRA_DETAILS CHAR(50), layout as for MAIN_DETAILS for 1st 23 CHARs only,
	//      OR, (for Irregular date) the irregular date text up to 50 characters

	// Test that dateDetails has content before proceeding
		if (dateDetails.length() == 0) return;
	// Test for irregular date, set into GUI and return field, exit
		if(dateDetails.substring(0,1).equals("I")) {		//$NON-NLS-1$
			txt_Irreg.setText(date2Details);
			returnDate = date2Details;
			return;
		}
	// Test that dateDetails is valid before proceeding
		if (dateDetails.length() < 23) return;
	// Test for Date Era date
		extract = dateDetails.substring(1,3);
		if(extract.equals("CE")) radioCE.setSelected(true);		//$NON-NLS-1$
		if(extract.equals("CO")) radioCO.setSelected(true);		//$NON-NLS-1$
		if(extract.equals("BC")) radioBC.setSelected(true);		//$NON-NLS-1$
		if(extract.equals("YA")) radioYA.setSelected(true);		//$NON-NLS-1$
	// Test for Question marked date
		if(dateDetails.substring(17,18).equals("Y")) chk_Q.setSelected(true);	//$NON-NLS-1$
	// Analyse Qualifier to set combo-box
		String qual = dateDetails.substring(16,17);
		switch(qual) {
		case "X" :		//$NON-NLS-1$
			combo_Qualify.setSelectedIndex(0);	// exact (combo = 'at')
			break;
		case "C" :		//$NON-NLS-1$
			combo_Qualify.setSelectedIndex(1);	// circa
			break;
		case "S" :		//$NON-NLS-1$
			combo_Qualify.setSelectedIndex(2);	// say
			break;
		case "E" :		//$NON-NLS-1$
			combo_Qualify.setSelectedIndex(3);	// estimated
			break;
		case "B" :		//$NON-NLS-1$
			combo_Qualify.setSelectedIndex(4);	// before
			break;
		case "A" :		//$NON-NLS-1$
			combo_Qualify.setSelectedIndex(5);	// after
			break;
		case "W" :		//$NON-NLS-1$
			combo_Qualify.setSelectedIndex(6);	// between/and
			break;
		case "O" :		//$NON-NLS-1$
			combo_Qualify.setSelectedIndex(7);	// either/or
			break;
		case "F" :		//$NON-NLS-1$
			combo_Qualify.setSelectedIndex(8);	// from/to
			break;
		}
	// Extract Years
		if (dateYears != 0) txt_Year.setText(String.valueOf(Math.abs(dateYears)));	// Get string version of absolute years
	// Extract Months
		extract = dateDetails.substring(3,5);
		if (!extract.equals("%%")) txt_Month.setText(extract);	//$NON-NLS-1$
	// combo_Month set to month abbrev
		if (!extract.equals("%%")) combo_Month.setSelectedIndex(Integer.parseInt(extract)); 	//$NON-NLS-1$
	// Extract Days
		extract = dateDetails.substring(5,7);
		if (!extract.equals("%%")) txt_Day.setText(extract);	//$NON-NLS-1$
	// Extract Hours
		extract = dateDetails.substring(7,9);
		if (!extract.equals("%%")) {	//$NON-NLS-1$
			timeIsPresent = true;
			txt_Hr.setText(extract);
		}
	// Extract Minutes
		extract = dateDetails.substring(9,11);
		if (!extract.equals("%%")) {	//$NON-NLS-1$
			timeIsPresent = true;
			txt_Min.setText(extract);
		}
	// Extract Secs
		extract = dateDetails.substring(11,13);
		if (!extract.equals("%%")) {	//$NON-NLS-1$
			timeIsPresent = true;
			txt_Sec.setText(extract);
		}
	// Extract mSecs
		extract = dateDetails.substring(13,16);
		if (!extract.equals("%%%")) {	//$NON-NLS-1$
			timeIsPresent = true;
			txt_mSec.setText(extract);
		}
	// Now test to see if the date2Year/Detail data is valid by checking both the combobox
	// setting AND the length of the date2Detail string. If OK, process it same way as dateDetails
		if (combo_Qualify.getSelectedIndex() > 5 && date2Details.length() > 22) {
			if (date2Years != 0) txt_Year2.setText(String.valueOf(Math.abs(date2Years)));	// Get string version of absolute years
			extract = date2Details.substring(3,5);
			if (!extract.equals("%%")) txt_Month2.setText(extract);		//$NON-NLS-1$

			// combo_Month2 set to month abbrev
			if (!extract.equals("%%")) combo_Month2.setSelectedIndex(Integer.parseInt(extract));	//$NON-NLS-1$

			extract = date2Details.substring(5,7);
			if (!extract.equals("%%")) txt_Day2.setText(extract);		//$NON-NLS-1$
			extract = date2Details.substring(7,9);
			if (!extract.equals("%%")) {		//$NON-NLS-1$
				timeIsPresent = true;
				txt_Hr2.setText(extract);
			}
			extract = date2Details.substring(9,11);
			if (!extract.equals("%%")) {		//$NON-NLS-1$
				timeIsPresent = true;
				txt_Min2.setText(extract);
			}
			extract = date2Details.substring(11,13);
			if (!extract.equals("%%")) {		//$NON-NLS-1$
				timeIsPresent = true;
				txt_Sec2.setText(extract);
			}
			extract = date2Details.substring(13,16);
			if (!extract.equals("%%%")) {		//$NON-NLS-1$
				timeIsPresent = true;
				txt_mSec2.setText(extract);
			}
		}
		// If time data was present, expand the GUI to show it
		if (timeIsPresent) btn_addTime.doClick();
	}		// End of convertFromHDate

/**
 * validateInput - validate all entered data for calendar compatibility
 */
	private void validateInput() {
	// Validate the input values for calendar compatibility
		// Clear the errorCode before starting
		errorCode = "";			//$NON-NLS-1$
		// First, convert year values to integer and test for leap year
		if (txt_Year.getText().length() > 0) dateYear = Integer.valueOf(txt_Year.getText());
			else dateYear = 0;
		if ((dateYear % 4 == 0) && (dateYear % 100 != 0 || dateYear % 400 == 0)) leapYear= true;
		    else leapYear = false;

		// if using the mth combobox, need to get setting and turn into correct mth integer
		if (monthMMM) dateMonth = combo_Month.getSelectedIndex();
			else if (txt_Month.getText().length() > 0) dateMonth = Integer.valueOf(txt_Month.getText());
					else dateMonth = 0;

		// Convert remaining values to integers
		if (txt_Day.getText().length() > 0) dateDay = Integer.valueOf(txt_Day.getText());
			else dateDay = 0;
		if (txt_Hr.getText().length() > 0) dateHr =  Integer.valueOf(txt_Hr.getText());
			else dateHr = 0;
		if (txt_Min.getText().length() > 0) dateMin = Integer.valueOf(txt_Min.getText());
			else dateMin = 0;
		if (txt_Sec.getText().length() > 0) dateSec = Integer.valueOf(txt_Sec.getText());
			else dateSec = 0;
		if (txt_mSec.getText().length() > 0) dateMSec = Integer.valueOf(txt_mSec.getText());
			else dateMSec = 0;

	// Start tests for validity
		// Test for year = 0, or month = 0 when day > 0
		if (dateYear == 0 || (dateMonth == 0 && dateDay > 0)) errorCode = errorCode + "O";	//$NON-NLS-1$
		// If year greater than current year, flag a problem
		if (dateYear > Year.now().getValue()) errorCode = errorCode + "Y";	//$NON-NLS-1$
		// Test month value
		if (dateMonth > 12) errorCode = errorCode + "M";	//$NON-NLS-1$
		// Test days of month, starting with Feb
		if (dateMonth == 2 && leapYear && dateDay > 29) errorCode = errorCode + "D";	//$NON-NLS-1$
			else if (dateMonth == 2 && !leapYear && dateDay > 28) errorCode = errorCode + "L";	//$NON-NLS-1$
		if (dateMonth == 2 && dateDay > 28 && dateYear < 1583) errorCode = errorCode + "G";	//$NON-NLS-1$
		if ((dateMonth == 9 || dateMonth == 4 || dateMonth == 6 || dateMonth == 11) && dateDay > 30 ) errorCode = errorCode + "D";	//$NON-NLS-1$
		if ((dateMonth == 1 || dateMonth == 3 || dateMonth == 5 || dateMonth == 7 ||
			 dateMonth == 8 || dateMonth == 10 || dateMonth == 12) 	&& dateDay > 31 ) errorCode = errorCode + "D";	//$NON-NLS-1$
		if (dateHr > 24) errorCode = errorCode + "H";	//$NON-NLS-1$
		if (dateMin > 60) errorCode = errorCode + "I";	//$NON-NLS-1$
		if (dateSec > 60) errorCode = errorCode + "S";	//$NON-NLS-1$

	// Now repeat all above tests for the date2 data IF Qualifier is from/between/either type
		if (selectedQualifier >= 6) {
			if (txt_Year2.getText().length() > 0) date2Year = Integer.valueOf(txt_Year2.getText());
				else date2Year = 0;
			if ((date2Year % 4 == 0) && (date2Year % 100 != 0 || date2Year % 400 == 0)) leap2Year= true;
		    	else leap2Year = false;

			// if using the mth combobox, need to get setting and turn into correct mth integer
			if (monthMMM) date2Month = combo_Month2.getSelectedIndex();
				else if (txt_Month2.getText().length() > 0) date2Month = Integer.valueOf(txt_Month2.getText());
						else date2Month = 0;

			// Convert remaining values to integers
			if (txt_Day2.getText().length() > 0) date2Day = Integer.valueOf(txt_Day2.getText());
				else date2Day = 0;
			if (txt_Hr2.getText().length() > 0) date2Hr =  Integer.valueOf(txt_Hr2.getText());
				else date2Hr = 0;
			if (txt_Min2.getText().length() > 0) date2Min = Integer.valueOf(txt_Min2.getText());
				else date2Min = 0;
			if (txt_Sec2.getText().length() > 0) date2Sec = Integer.valueOf(txt_Sec2.getText());
				else date2Sec = 0;
			if (txt_mSec2.getText().length() > 0) date2MSec = Integer.valueOf(txt_mSec2.getText());
				else date2MSec = 0;

			// Test for year = 0, or month = 0 when day > 0
			if (date2Year == 0 || (date2Month == 0 && date2Day > 0)) errorCode = errorCode + "O";	//$NON-NLS-1$
			// If year2 greater than current year, flag a problem
			if (date2Year > Year.now().getValue()) errorCode = errorCode + "Y";	//$NON-NLS-1$
			// Test month value
			if (date2Month > 12) errorCode = errorCode + "M";	//$NON-NLS-1$
			// Test days of month, starting with Feb
			if (date2Month == 2 && leap2Year && date2Day > 29) errorCode = errorCode + "D";	//$NON-NLS-1$
				else if (date2Month == 2 && !leap2Year && date2Day > 28) errorCode = errorCode + "P";	//$NON-NLS-1$
			if (date2Month == 2 && date2Day > 28 && date2Year < 1583) errorCode = errorCode + "G";	//$NON-NLS-1$
			if ((date2Month == 9 || date2Month == 4 || date2Month == 6 || date2Month == 11) && date2Day > 30 ) errorCode = errorCode + "D";	//$NON-NLS-1$
			if ((date2Month == 1 || date2Month == 3 || date2Month == 5 || date2Month == 7 ||
				 date2Month == 8 || date2Month == 10 || date2Month == 12) 	&& date2Day > 31 ) errorCode = errorCode + "D";	//$NON-NLS-1$
			if (date2Hr > 24) errorCode = errorCode + "H";	//$NON-NLS-1$
			if (date2Min > 60) errorCode = errorCode + "I";	//$NON-NLS-1$
			if (date2Sec > 60) errorCode = errorCode + "S";	//$NON-NLS-1$

			// Now check if the first date is later than the second date.
			// If any 2nd value > 1st value we must check next higher one for 2nd > 1st etc, etc
			boolean later = false;
			if (dateMSec > date2MSec) later = true;
			if (later && date2Sec > dateSec) later = false;

			if (dateSec > date2Sec) later = true;
			if (later && date2Min > dateMin) later = false;

			if (dateMin > date2Min) later = true;
			if (later && date2Hr > dateHr) later = false;

			if (dateHr > date2Hr) later = true;
			if (later && date2Day > dateDay) later = false;

			if (dateDay > date2Day) later = true;
			if (later && date2Month > dateMonth) later = false;

			if (dateMonth > date2Month) later = true;
			if (later && date2Year > dateYear) later = false;

			if (dateYear > date2Year) later = true;
			if (later) errorCode = errorCode + "Z";	//$NON-NLS-1$
		}

	// Test errorCode content and show error msg(s) if needed
		if (errorCode.length() > 0) {
			String errorMsg = HG0590Msgs.Text_19;	// Invalid data entered:
			if (errorCode.contains("Y")) errorMsg = errorMsg + HG0590Msgs.Text_20;	//$NON-NLS-1$ // - Year is in the future
			if (errorCode.contains("O")) errorMsg = errorMsg + HG0590Msgs.Text_21;	//$NON-NLS-1$ // - Date field invalid - cannot be zero
			if (errorCode.contains("Z")) errorMsg = errorMsg + HG0590Msgs.Text_22;	//$NON-NLS-1$ // - Second date/time is before the First
			if (errorCode.contains("M")) errorMsg = errorMsg + HG0590Msgs.Text_23;	//$NON-NLS-1$ // - Month value is invalid
			if (errorCode.contains("L")) errorMsg = errorMsg + HG0590Msgs.Text_24 + txt_Year.getText() + HG0590Msgs.Text_25; //$NON-NLS-1$ // - Day value invalid. is not a leap year
			if (errorCode.contains("P")) errorMsg = errorMsg + HG0590Msgs.Text_24 + txt_Year2.getText() + HG0590Msgs.Text_25; //$NON-NLS-1$ // - Day value invalid. is not a leap year
			if (errorCode.contains("G")) errorMsg = errorMsg + HG0590Msgs.Text_24 + HG0590Msgs.Text_28;	//$NON-NLS-1$ // - Day value invalid. Feb 29 did not exist before 1583
			if (errorCode.contains("D")) errorMsg = errorMsg + HG0590Msgs.Text_29;	//$NON-NLS-1$ // - Day value invalid for chosen month
			if (errorCode.contains("H")) errorMsg = errorMsg + HG0590Msgs.Text_30;	//$NON-NLS-1$ // - Hour value greater than 24
			if (errorCode.contains("I")) errorMsg = errorMsg + HG0590Msgs.Text_31;	//$NON-NLS-1$ // - Minute value greater than 60
			if (errorCode.contains("S")) errorMsg = errorMsg + HG0590Msgs.Text_32;	//$NON-NLS-1$ // - Second value greater than 60
			// Now show errormsg, anchored over Save button
			JOptionPane.showMessageDialog(btn_Save, errorMsg, HG0590Msgs.Text_33, // Date/Time Input Error
					JOptionPane.ERROR_MESSAGE);
		}
	}		// End of validateInput

/**
 * convertToHDate - convert the validated input data to the HDate fields
 */
	private void convertToHDate() {
	// Extract data from the entered fields and build the T170 date strings
	// First, test for an irregular date
		if (txt_Irreg.getText().length() > 0) {
			dateYear = 0;
			dateDetails ="I";  //$NON-NLS-1$
			date2Year = 0;
			date2Details = txt_Irreg.getText();
		} else {
	// Not irregular, so need to get all date/time data set into the T170 data strings
			// Set Years
			if (txt_Year.getText().length() > 0) dateYear = Integer.valueOf(txt_Year.getText());
				else dateYear = 0;
			// Set Date Origin
			dateDetails = "N";	// normal date //$NON-NLS-1$
			// Set calendar type
			if (radioCE.isSelected()) dateDetails = dateDetails + "CE";		//$NON-NLS-1$
			if (radioCO.isSelected()) dateDetails = dateDetails + "CO";		//$NON-NLS-1$
			if (radioBC.isSelected()) {
				dateDetails = dateDetails + "BC";		//$NON-NLS-1$
				dateYear = -dateYear;	// Make years negative if set to BC
			}
			if (radioYA.isSelected()) dateDetails = dateDetails + "YA";		//$NON-NLS-1$
			// Set Months, Days
			// if using the mth combobox, need to get setting and turn into correct 2-char mth,
			// but do not allow storing a month = 00.
			String monthNum;
			if (monthMMM) monthNum = String.format("%02d", combo_Month.getSelectedIndex());		//$NON-NLS-1$
				else monthNum = txtToString2(txt_Month);
			if (monthNum.equals("00")) monthNum = "%%";	//$NON-NLS-1$ //$NON-NLS-2$
			dateDetails = dateDetails + monthNum;
			dateDetails = dateDetails + txtToString2(txt_Day);
			// Set Hrs, Mins, Secs, mSecs
			dateDetails = dateDetails + txtToString2(txt_Hr);
			dateDetails = dateDetails + txtToString2(txt_Min);
			dateDetails = dateDetails + txtToString2(txt_Sec);
			dateDetails = dateDetails + txtToString3(txt_mSec);
			// Set Qualifier code
			dateDetails = dateDetails + qualifierCodes.substring(selectedQualifier, selectedQualifier+1);
			// Set Q-mark
			if (chk_Q.isSelected()) dateDetails = dateDetails + "Y";	//$NON-NLS-1$
			else dateDetails = dateDetails + "N";	//$NON-NLS-1$
			// Set offset code/number (not yet implemented)
			dateDetails = dateDetails + "__000";	//$NON-NLS-1$

		// Now repeat all above for the date2 data BUT ONLY if Qualifier is from/between/either type
			if (selectedQualifier >= 6) {
				// Set date2 Years
				if (txt_Year2.getText().length() > 0) date2Year = Integer.valueOf(txt_Year2.getText());
				else date2Year = 0;
				// Set Date Origin
				date2Details = "N";	// normal date //$NON-NLS-1$
				// Set calendar type
				if (radioCE.isSelected()) date2Details = date2Details + "CE";		//$NON-NLS-1$
				if (radioCO.isSelected()) date2Details = date2Details + "CO";		//$NON-NLS-1$
				if (radioBC.isSelected()) {
					date2Details = date2Details + "BC";		//$NON-NLS-1$
					date2Year = -date2Year;	// Make years negative if set to BC
				}
				if (radioYA.isSelected()) date2Details = date2Details + "YA";		//$NON-NLS-1$
				// Set Months, Days
				if (monthMMM) date2Details = date2Details + String.format("%02d", combo_Month2.getSelectedIndex());	//$NON-NLS-1$
				else date2Details = date2Details + txtToString2(txt_Month2);
				date2Details = date2Details + txtToString2(txt_Day2);
				// Set Hrs, Mins, Secs, mSecs
				date2Details = date2Details + txtToString2(txt_Hr2);
				date2Details = date2Details + txtToString2(txt_Min2);
				date2Details = date2Details + txtToString2(txt_Sec2);
				date2Details = date2Details + txtToString3(txt_mSec2);
				// Set Qualifier code
				date2Details = date2Details + qualifierCodes.substring(selectedQualifier, selectedQualifier + 1);
				// Set Q-mark
				if (chk_Q.isSelected()) date2Details = date2Details + "Y";	//$NON-NLS-1$
				else date2Details = date2Details + "N";	//$NON-NLS-1$
				// Set offset code/number (not yet implemented)
				date2Details = date2Details + "__000";	//$NON-NLS-1$

			} else {	// else set the date2 fields to nothing
				date2Year = 0;
				date2Details = "0";	//$NON-NLS-1$
			}
		}

	// Now build the HDate sortString
	// sortString is initialised with '11' as we're not yet processing calendars
	   sortString = "11"; 	//$NON-NLS-1$
	// Use a copy of date2Year and date2Details for processing sort string
		long date2YearCopy = 0L;
		String date2DetailsCopy = "";	//$NON-NLS-1$
		// First, test for an irregular date
		if (txt_Irreg.getText().length() > 0) {
			sortString = "3"; 	//$NON-NLS-1$
			return;
		}
		// If date2 data empty, first copy in the base date data
		if (date2Year == 0) date2YearCopy = dateYear;
		else date2YearCopy = date2Year;
		if (date2Details.equals("0")) date2DetailsCopy = dateDetails;	//$NON-NLS-1$
		else date2DetailsCopy = date2Details;

	// Next, (as we're only using BCE/CE calendars so far), adjust the years
		dateYearCE = dateYear + 5000000;
		date2YearCE = date2YearCopy + 5000000;
	// Convert years to character
		char[] dateYearCEchar = Long.toString(dateYearCE).toCharArray();
		char[] date2YearCEchar = Long.toString(date2YearCE).toCharArray();
		// add year characters to sortString in interleaved fashion
		for (int i = 0; i < dateYearCEchar.length; i++) {
			sortString = sortString + dateYearCEchar[i];
			sortString = sortString + date2YearCEchar[i];
		}
	// Now pick out the 13 chars for the months, days, hrs, mins, secs, msecs and
	// interleave them into the sortString.
	// This will run from the 3rd to the 15th char of the dateDetail strings.
        for(int m = 3; m < 16; m++) {
            sortString = sortString + dateDetails.charAt(m);
            sortString = sortString + date2DetailsCopy.charAt(m);
        }
     // This may pickup '%' entries (unused fields in the details), so
     // now replace them with '0'
        sortString = sortString.replace("%","0");	//$NON-NLS-1$ //$NON-NLS-2$
	// Now add Qualifier
        String sortQual = "4"; // default to exact;	//$NON-NLS-1$
		String qual = dateDetails.substring(16,17);
		switch(qual) {
		case "X" :		//$NON-NLS-1$
			sortQual = "4";	// exact (combo = 'at')	//$NON-NLS-1$
			break;
		case "C" :		//$NON-NLS-1$
			sortQual = "3";	// circa	//$NON-NLS-1$
			break;
		case "S" :		//$NON-NLS-1$
			sortQual = "2";	// say	//$NON-NLS-1$
			break;
		case "E" :		//$NON-NLS-1$
			sortQual = "9";	// estimated	//$NON-NLS-1$
			break;
		case "B" :		//$NON-NLS-1$
			sortQual =	"1";	// before	//$NON-NLS-1$
			break;
		case "A" :		//$NON-NLS-1$
			sortQual = "5";	// after	//$NON-NLS-1$
			break;
		case "W" :		//$NON-NLS-1$
			sortQual = "6";	// between/and	//$NON-NLS-1$
			break;
		case "O" :		//$NON-NLS-1$
			sortQual = "7";	// either/or	//$NON-NLS-1$
			break;
		case "F" :		//$NON-NLS-1$
			sortQual = "8"; 	// from/to	//$NON-NLS-1$
			break;
		}
		sortString = sortString + sortQual;
	// Now handle '?' case
		if (dateDetails.substring(17,18).equals("Y")) sortString = sortString + "1";	//$NON-NLS-1$ //$NON-NLS-2$
		else sortString = sortString + "0";	//$NON-NLS-1$
	// Add offsets and unused (all 0 at this stage)
		sortString = sortString + "000000000000";	//$NON-NLS-1$
	}		// End of convertToHDate

/**
 * txtToString2 - converts txt data to 2-Char String for use in dateDetails
 * txtIn2	text to be converted
 */
	private String txtToString2(JTextField txtIn2) {
		String charTwo = "";		//$NON-NLS-1$
		if (txtIn2.getText().length() == 0) charTwo = "%%";	//$NON-NLS-1$
		if (txtIn2.getText().length() == 1) charTwo = "0"+ txtIn2.getText();	//$NON-NLS-1$
		if (txtIn2.getText().length() == 2) charTwo = txtIn2.getText();
		return charTwo;
	}		// end of txtToString2

/**
 * txtToString3 - converts txt data to 3-Char String for use in dateDetails
 * txtIn3	text to be converted
 */
	private String txtToString3(JTextField txtIn3) {
		String charThree = "";		//$NON-NLS-1$
		if (txtIn3.getText().length() == 0) charThree = "%%%";	//$NON-NLS-1$
		if (txtIn3.getText().length() == 1) charThree = "00"+ txtIn3.getText();	//$NON-NLS-1$
		if (txtIn3.getText().length() == 2) charThree = "0"+ txtIn3.getText();	//$NON-NLS-1$
		if (txtIn3.getText().length() == 3) charThree = txtIn3.getText();
		return charThree;
	}		// end of txtToString3

/**
 * textLimit - limits entry in JTextField to X characters (and must be numeric if X < 5)
 * @limit	max no. of characters in the JTextField
 */
	class textLimit extends PlainDocument {
		private static final long serialVersionUID = 001L;
		private int limit;
		textLimit(int limit) {
			super();
			this.limit = limit;
		}
		public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
		// Intent of this code is to handle both a single typed character OR a pasted string
		// If the limit is < 5, it MUST be all numeric, otherwise can be anything (for irreg date).
		// AND, if limit < 5 (data entry for a small text field), ensure Irregular data field is cleared
			if (str == null) return;
			if (str.length() == 0) return;
			if (limit < 5) {
				txt_Irreg.setText(null);
				dateTimeCleared = false;
				for (char c : str.toCharArray()) {
		            if (!Character.isDigit(c)) return;
		        }
			}
			if ((getLength() + str.length()) <= limit) {
				super.insertString(offset, str, attr);
			}
		}
	}		// end of textLimit

}  // End of HG0590EditDate
