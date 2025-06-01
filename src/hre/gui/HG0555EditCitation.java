package hre.gui;
/**************************************************************************************
 * Edit Citation
 * ***********************************************************************************
 * v0.04.0032 2025-01-17 Original draft (D Ferguson)
 *			  2025-01-19 Add Preview button; add Listeners for text fields (D Ferguson)
 *			  2025-01-31 Add ability to select Source by ### (D Ferguson)
 *			  2025-02-10 Change textAreas to textPanes (D Ferguson)
 *			  2025-02-20 Add Assessor field & ActionListeners (D Ferguson)
 *        	  2025-02-21 Populate citation data and save updates (N. Tolleshaug)
 *        	  2025-02-25 Added add new citation (N. Tolleshaug)
 *        	  2025-02-25 Add/Update and delete event citation (N. Tolleshaug)
 *        	  2025-02-26 Add/Update and delete name citation (N. Tolleshaug)
 *            2025-02-27 Fix InputVerifier, add Source Fidelity, close Reminder on exit (D Ferguson)
 *            2025-03-01 Fixes to turn on Save button as needed (D Ferguson)
 *            2025-03-02 Added update of fidelity source citation edit (N. Tolleshaug)
 *            2025-03-19 Numerical values for ACCURACY use TINYINT (N. Tolleshaug)
 *            2025-03-20 Added assessorName setting from findAssessorName() (N. Tolleshaug)
 *            2025-03-21 Improved hnadling of Surety field input (D Ferguson)
 *            2025-03-22 Handling add citation if source table empty (N. Tolleshaug)
 *            2025-03-23 Removed console print of fidelity (N. Tolleshaug)
 *            2025-04-10 Add parsing routine to display footnote/biblio output (D Ferguson)
 *            2025-04-28 Adjust editable Surety values based on citation owner type (D Ferguson)
 *            2025-05-09 Allow added source selection by number entry (D Ferguson)
 *            2025-05-19 NLS all code (D Ferguson)
 *            2025-05-25 Use EVNT_KEY_ASSOC_MIN to set acc2 use (D Ferguson)
 *            2025-05-26 Make Preview panel disappear on 2nd click of button (D Ferguson)
 *            2025-05-26 Fix Fidelity not set on screen if new citation (D Ferguson)
 *            2025-05-27 Fix handling of Assessor Name and PID in Citations (D Ferguson)
 *            2025-05-29 Make Assessor name = current owner if Surety changed (D Ferguson)
 *************************************************************************************
 * Notes for incomplete code still requiring attention
 * NOTE02 footnote/biblio templates not yet loaded from source data
 * NOTE03 parsing routine needs [  ] parameter substitution method
 ************************************************************************************/

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DocumentFilter;
import javax.swing.text.NumberFormatter;

import org.apache.commons.lang3.StringUtils;

import hre.bila.HB0711Logging;
import hre.bila.HBCitationSourceHandler;
import hre.bila.HBException;
import hre.bila.HBProjectOpenData;
import hre.nls.HG0555Msgs;
import net.miginfocom.swing.MigLayout;

/**
 * Edit Citation
 * @author D Ferguson
 * @version v0.04.0032
 * @since 2025-01-17
 */

public class HG0555EditCitation extends HG0450SuperDialog {
	private static final long serialVersionUID = 001L;

	public static final String screenID = "55500"; //$NON-NLS-1$
	long null_RPID  = 1999999999999999L;
	long proOffset  = 1000000000000000L;

	HG0555EditCitation pointEditCitation = this;
	HG0547EditEvent pointEditEvent = null;
	HG0509ManagePersonName pointManagePersonName = null;
	HG0507SelectPerson pointSelectPerson = null;

	public HBCitationSourceHandler pointCitationSourceHandler;

	private JPanel contents;
	boolean outputVisible = false;

	String citationRefer = ""; //$NON-NLS-1$
	String sourceTitle = ""; //$NON-NLS-1$
	String citationDetail = ""; //$NON-NLS-1$
	String citationMemo = ""; //$NON-NLS-1$
	Object[] ownerAssessorData;
	String assessorName;
	long assessorPID;
	int keyAssocMin;
	int sourceREF = 0;
	String[] accuracyData = {" ", " ", " ", " ", " "}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	String fidelityData = ""; //$NON-NLS-1$
	int[] accuracyEdited = null;
	Object[] citationEditData;
	Object[] citationSaveData;
	Object[][] objSourceData;
	JTextField fidelityText;

	long citationTablePID = null_RPID;
	long sourceTablePID = null_RPID;

	DocumentListener citeDetailListen, citeMemoListen;
	FocusListener accFocus;
	boolean citeDetailChanged, citeMemoChanged, refTextChanged, assessChanged = false;
	boolean accuracyChanged, fidelityChanged;
	boolean sourceFound = false;

	JTextArea citeMemoText, citeDetailText;
	JTextField sourceTitleText, refText, acc1, acc2, accD, accP, accM;
	JFormattedTextField srcNum;

	String templateFullFootnote, templateShortFootnote, templateBibliography = "";  // NOTE02 data still to be loaded //$NON-NLS-1$

	JButton btn_Save;

	public void setSourceSelectedData(Object[] objSourceDataToEdit) {
		sourceTablePID = (long) objSourceDataToEdit[3];
		sourceTitleText.setText((String) objSourceDataToEdit[1]);
		srcNum.setText(""+objSourceDataToEdit[0]); 		//$NON-NLS-1$
		fidelityData = (String) objSourceDataToEdit[4];
		fidelityText.setText(" " + fidAnalyse()); 		//$NON-NLS-1$
	}

/**
 * Create the entry points
 */
	public HG0555EditCitation(boolean addCitationRecord, HBProjectOpenData pointOpenProject, String citeOwnerType,
							  int keyAssocMin)  {
		this.pointOpenProject = pointOpenProject;
		this.keyAssocMin = keyAssocMin;
		createGUI(addCitationRecord, citeOwnerType);
	}

	public HG0555EditCitation(boolean addCitationRecord, HBProjectOpenData pointOpenProject, String citeOwnerType,
							  int keyAssocMin, long citationTablePID)  {
		this.pointOpenProject = pointOpenProject;
		this.keyAssocMin = keyAssocMin;
		this.citationTablePID = citationTablePID;
		createGUI(addCitationRecord, citeOwnerType);
	}
/**
 * Create the dialog
*/
	private void createGUI(boolean addCitationRecord, String citeOwnerType) {
		if (addCitationRecord) setTitle(HG0555Msgs.Text_7);		// Add Citiation
		else setTitle(HG0555Msgs.Text_8);		// Edit Citation

	// Setup references for HG0450
		windowID = screenID;
		helpName = "editcitation";		 //$NON-NLS-1$
		pointCitationSourceHandler = pointOpenProject.getCitationSourceHandler();

	// Collect citationData if NOT an Add Citation case
		try {
			if (!addCitationRecord) {
				citationEditData = pointCitationSourceHandler.getCitationEditData(citationTablePID);
				citationRefer = (String) citationEditData[0];
				sourceREF = (int) citationEditData[1];
				sourceTitle = (String) citationEditData[2];
				citationDetail = (String) citationEditData[3];
				citationMemo = (String) citationEditData[4];
				assessorName = (String) citationEditData[5];
				assessorPID = (long) citationEditData[6];
				fidelityData = (String) citationEditData[7];
				accuracyData = pointCitationSourceHandler.getAccuracyData();
			}
	// For ANY case, get the current Project IS_OWNER data (needed for an ADD or if Surety changes)
			ownerAssessorData = (Object[]) pointCitationSourceHandler.getAssessorData();
	// Then for ADD Citation case, use this current Project Assessor name and PID
			if (addCitationRecord) {
				assessorName = (String) ownerAssessorData[0];
				assessorPID = (long) ownerAssessorData[1];
			}
		} catch (HBException hbe) {
			System.out.println(" HG0555EditCitation data load error: " + hbe.getMessage()); //$NON-NLS-1$
			hbe.printStackTrace();
		}

	// Setup close and logging actions
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: entering HG0555EditCitation");	//$NON-NLS-1$

	// For Text area/panes font setting
	    Font font = UIManager.getFont("TextArea.font");		//$NON-NLS-1$

	// Setup dialog
		contents = new JPanel();
		setContentPane(contents);
		contents.setLayout(new MigLayout("insets 10", "[][]", "[]10[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	   	JToolBar toolBar = new JToolBar();
    	toolBar.setFloatable(false);
    	toolBar.setAlignmentX(Component.LEFT_ALIGNMENT);
    	toolBar.add(Box.createHorizontalGlue());
    	// Add the HG0450 icons
		toolBar.add(btn_Remindericon);
		toolBar.add(btn_Helpicon);
		contents.add(toolBar, "north");	//$NON-NLS-1$

	// Setup Citation panel for citation details
		JPanel citePanel = new JPanel();
		citePanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		citePanel.setLayout(new MigLayout("", "[]10[]", "[]10[]10[]10[]10[]5[]5[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JButton btn_sorcSelect = new JButton(HG0555Msgs.Text_11);	// Select Source
		citePanel.add(btn_sorcSelect, "cell 0 0, alignx right");	//$NON-NLS-1$

	    NumberFormat format = NumberFormat.getInstance();
	    NumberFormatter formatter = new NumberFormatter(format);
	    formatter.setValueClass(Integer.class);
	    formatter.setMinimum(1);
	    formatter.setMaximum(99999);
	    srcNum = new JFormattedTextField(formatter);
	    srcNum.setText("" + sourceREF); //$NON-NLS-1$
	    srcNum.setColumns(5);
	    srcNum.setHorizontalAlignment(SwingConstants.CENTER);
	    citePanel.add(srcNum, "cell 1 0, alignx left");		//$NON-NLS-1$
	    // Don't allow user entry of new source number for an Edit
	    if (!addCitationRecord)	srcNum.setEnabled(false);

		sourceTitleText = new JTextField(sourceTitle);
		sourceTitleText.setColumns(30);
		sourceTitleText.setEditable(false);
		citePanel.add(sourceTitleText, "cell 1 0, gapx 10, alignx left");	//$NON-NLS-1$

	// Citation text fields
		JLabel citeDetail = new JLabel(HG0555Msgs.Text_13);		// Citation Detail:
		citePanel.add(citeDetail, "cell 0 1, alignx right");	//$NON-NLS-1$
		citeDetailText = new JTextArea();
		citeDetailText.append(citationDetail);
		citeDetailText.setWrapStyleWord(true);
		citeDetailText.setLineWrap(true);
		citeDetailText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		citeDetailText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)citeDetailText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		citeDetailText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		citeDetailText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		citeDetailText.setBorder(new JTable().getBorder());		// match Table border
		JScrollPane citeDetailTextScroll = new JScrollPane(citeDetailText);
		citeDetailTextScroll.setMinimumSize(new Dimension(350, 75));
		citeDetailTextScroll.getViewport().setOpaque(false);
		citeDetailTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		citeDetailText.setCaretPosition(0);	// set scrollbar to top
		citePanel.add(citeDetailTextScroll, "cell 1 1, alignx left");	//$NON-NLS-1$

		JLabel citeMemo = new JLabel(HG0555Msgs.Text_14);		// Citation Memo:
		citePanel.add(citeMemo, "cell 0 2, alignx right");		//$NON-NLS-1$
		citeMemoText = new JTextArea();
		citeMemoText.append(citationMemo);
		citeMemoText.setWrapStyleWord(true);
		citeMemoText.setLineWrap(true);
		citeMemoText.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null); //kill tabs in text area
		citeMemoText.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);
		((DefaultCaret)citeMemoText.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		citeMemoText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		citeMemoText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		citeMemoText.setBorder(new JTable().getBorder());		// match Table border
		JScrollPane citeMemoTextScroll = new JScrollPane(citeMemoText);
		citeMemoTextScroll.setMinimumSize(new Dimension(350, 75));
		citeMemoTextScroll.getViewport().setOpaque(false);
		citeMemoTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		citeMemoText.setCaretPosition(0);	// set scrollbar to top
		citePanel.add(citeMemoTextScroll, "cell 1 2, alignx left");	//$NON-NLS-1$

		JLabel ref = new JLabel(HG0555Msgs.Text_15);	// Reference:
		citePanel.add(ref, "cell 0 3, alignx right");	//$NON-NLS-1$
		refText = new JTextField(citationRefer);
		refText.setColumns(30);
		citePanel.add(refText, "cell 1 3, alignx left");	//$NON-NLS-1$

		JLabel assess = new JLabel(HG0555Msgs.Text_16);		// Assessor:
		citePanel.add(assess, "cell 0 4, alignx right");	//$NON-NLS-1$
		JTextField assessText = new JTextField();
		assessText.setColumns(30);
		assessText.setText(assessorName);
		assessText.setEditable(false);
		assessText.setFocusable(false);
		citePanel.add(assessText, "cell 1 4, alignx left");	//$NON-NLS-1$

		JLabel sure = new JLabel(HG0555Msgs.Text_17);	// Surety:
		citePanel.add(sure, "cell 0 5, alignx right");	//$NON-NLS-1$

		JLabel srcFidLbl = new JLabel(HG0555Msgs.Text_18);	// Source Fidelity:
		citePanel.add(srcFidLbl, "cell 0 6, alignx right");	//$NON-NLS-1$
		fidelityText = new JTextField();
		fidelityText.setColumns(15);
		fidelityText.setEditable(false);
		fidelityText.setFocusable(false);
		fidelityText.setText(" " + fidAnalyse()); //$NON-NLS-1$
		citePanel.add(fidelityText, "cell 1 6, alignx left");	//$NON-NLS-1$

	// Define Surety panel
		JPanel surePanel = new JPanel();
		surePanel.setLayout(new MigLayout("", "[]5[]5[]5[]5[]20[]", "[]0[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel sure1 = new JLabel("1");		//$NON-NLS-1$
		surePanel.add(sure1, "cell 0 0,alignx center");		//$NON-NLS-1$
		JLabel sure2 = new JLabel("2");		//$NON-NLS-1$
		surePanel.add(sure2, "cell 1 0,alignx center");		//$NON-NLS-1$
		JLabel sureD = new JLabel(HG0555Msgs.Text_20);		// D
		surePanel.add(sureD, "cell 2 0,alignx center");		//$NON-NLS-1$
		JLabel sureP = new JLabel(HG0555Msgs.Text_21);		// P
		surePanel.add(sureP, "cell 3 0,alignx center");		//$NON-NLS-1$
		JLabel sureM = new JLabel(HG0555Msgs.Text_22);		// M
		surePanel.add(sureM, "cell 4 0,alignx center");		//$NON-NLS-1$

		acc1 = new JTextField(1);
		acc1.setColumns(1);
		acc1.setHorizontalAlignment(SwingConstants.CENTER);
		acc1.setText(accuracyData[0]);
		surePanel.add(acc1, "cell 0 1");	//$NON-NLS-1$

		acc2 = new JTextField();
		acc2.setColumns(1);
		acc2.setHorizontalAlignment(SwingConstants.CENTER);
		acc2.setText(accuracyData[1]);
		acc2.setEnabled(false);
		surePanel.add(acc2, "cell 1 1");	//$NON-NLS-1$

		accD = new JTextField();
		accD.setColumns(1);
		accD.setHorizontalAlignment(SwingConstants.CENTER);
		accD.setText(accuracyData[2]);
		surePanel.add(accD, "cell 2 1");	//$NON-NLS-1$

		accP = new JTextField();
		accP.setColumns(1);
		accP.setHorizontalAlignment(SwingConstants.CENTER);
		accP.setText(accuracyData[3]);
		surePanel.add(accP, "cell 3 1");	//$NON-NLS-1$

		accM = new JTextField();
		accM.setColumns(1);
		accM.setHorizontalAlignment(SwingConstants.CENTER);
		accM.setText(accuracyData[4]);
		surePanel.add(accM, "cell 4 1");	//$NON-NLS-1$

		// The above Surety values of 1, 2, D, P, M do not apply for all events
		// For Names (ownertype=T402) only 1, M apply
		// For Parent relationship (T405) only 1, D, M apply
		// For all others only 1, D, P, M apply EXCEPT if keyAssocMin = 2.
		// Hence acc2 is disabled by default, unless keyAssocMin is > 1.
		// So set appropriate values non-enabled to enforce these rules
		if (keyAssocMin > 1) acc2.setEnabled(true);
		if (citeOwnerType.equals("T402")) { //$NON-NLS-1$
			accD.setEnabled(false);
			accP.setEnabled(false);
		}
		if (citeOwnerType.equals("T405")) accP.setEnabled(false); //$NON-NLS-1$

		JLabel accHint = new JLabel(HG0555Msgs.Text_26);		// (Valid values are 3, 2, 1, 0, minus, blank)
		surePanel.add(accHint, "cell 5 1");	//$NON-NLS-1$

		citePanel.add(surePanel, "cell 1 5, alignx left");	//$NON-NLS-1$
		contents.add(citePanel, "cell 0 0, aligny top"); //$NON-NLS-1$

	// Setup Output panel for display of how citation footnote/biblio will look
		JPanel outputPanel = new JPanel();
		outputPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		outputPanel.setLayout(new MigLayout("", "[]", "[]5[]10[]5[]10[]5[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel fullFoot = new JLabel(HG0555Msgs.Text_27);	// Full footnote
		outputPanel.add(fullFoot, "cell 0 0, alignx left");	//$NON-NLS-1$
		JTextPane fullFootText = new JTextPane();
		fullFootText.setContentType("text/html"); //$NON-NLS-1$
		fullFootText.setEditable(false);
		fullFootText.setFocusable(false);
		fullFootText.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		fullFootText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		fullFootText.setBorder(new JTable().getBorder());		// match Table border
		fullFootText.setPreferredSize(new Dimension(200, 90));
		JScrollPane fullFootTextScroll = new JScrollPane(fullFootText);
		fullFootTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		outputPanel.add(fullFootTextScroll, "cell 0 1, alignx left");	//$NON-NLS-1$

		JLabel shortFoot = new JLabel(HG0555Msgs.Text_28);		// Short footnote
		outputPanel.add(shortFoot, "cell 0 2, alignx left");	//$NON-NLS-1$
		JTextPane shortFootText = new JTextPane();
		shortFootText.setContentType("text/html"); //$NON-NLS-1$
		shortFootText.setEditable(false);
		shortFootText.setFocusable(false);
		shortFootText.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		shortFootText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		shortFootText.setBorder(new JTable().getBorder());		// match Table border
		shortFootText.setPreferredSize(new Dimension(200, 75));
		JScrollPane shortFootTextScroll = new JScrollPane(shortFootText);
		shortFootTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		outputPanel.add(shortFootTextScroll, "cell 0 3, alignx left");	//$NON-NLS-1$

		JLabel biblio = new JLabel(HG0555Msgs.Text_29);		// Bibliography
		outputPanel.add(biblio, "cell 0 4, alignx left");	//$NON-NLS-1$
		JTextPane biblioText = new JTextPane();
		biblioText.setContentType("text/html"); //$NON-NLS-1$
		biblioText.setEditable(false);
		biblioText.setFocusable(false);
		biblioText.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		biblioText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		biblioText.setBorder(new JTable().getBorder());		// match Table border
		biblioText.setPreferredSize(new Dimension(200, 75));
		JScrollPane biblioTextScroll = new JScrollPane(biblioText);
		biblioTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		outputPanel.add(biblioTextScroll, "cell 0 5, alignx left");	//$NON-NLS-1$

		contents.add(outputPanel, "cell 1 0,aligny top, hidemode 3"); //$NON-NLS-1$
		outputPanel.setVisible(false);

	// Define control buttons
		JButton btn_Cancel = new JButton(HG0555Msgs.Text_30);		// Cancel
		btn_Cancel.setEnabled(true);
		contents.add(btn_Cancel, "cell 0 1, alignx right, gapx 10, tag cancel"); //$NON-NLS-1$
		JButton btn_Preview = new JButton(HG0555Msgs.Text_31);		// Preview
		btn_Preview.setEnabled(true);
		contents.add(btn_Preview, "tag left,cell 0 1,alignx right,gapx 10"); //$NON-NLS-1$
		btn_Save = new JButton(HG0555Msgs.Text_32);					// Save
		btn_Save.setEnabled(false);
		contents.add(btn_Save, "cell 0 1, alignx right, gapx 10, tag ok"); //$NON-NLS-1$

	// End of Panel Definitions

		// Focus Policy still to be setup!

		pack();

/*****************************
 * CREATE All ACTION LISTENERS
 *****************************/
		// Listener for clicking 'X' on screen
		addWindowListener(new WindowAdapter() {
		    @Override
			public void windowClosing(WindowEvent e)  {
		    	btn_Cancel.doClick();
			}
		});

		// Listener for Citation Detail textarea
		citeDetailListen = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {updateFieldState();}
            @Override
            public void removeUpdate(DocumentEvent e) {updateFieldState();}
            @Override
            public void changedUpdate(DocumentEvent e) {updateFieldState();}
            protected void updateFieldState() {
            	citeDetailChanged = true;
            	btn_Save.setEnabled(true);
            }
        };
        citeDetailText.getDocument().addDocumentListener(citeDetailListen);

		// Listener for Citation Memo textarea
		citeMemoListen = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {updateFieldState();}
            @Override
            public void removeUpdate(DocumentEvent e) {updateFieldState();}
            @Override
            public void changedUpdate(DocumentEvent e) {updateFieldState();}
            protected void updateFieldState() {
            	citeMemoChanged = true;
            	btn_Save.setEnabled(true);
            }
        };
        citeMemoText.getDocument().addDocumentListener(citeMemoListen);

        // Listener for Reference textfield
		refText.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {updateFieldState();}
            @Override
            public void removeUpdate(DocumentEvent e) {updateFieldState();}
            @Override
            public void changedUpdate(DocumentEvent e) {updateFieldState();}
            protected void updateFieldState() {
            	refTextChanged = true;
            	btn_Save.setEnabled(true);
            }
        });

        // Listener for Assessor textfield
		assessText.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
	          	assessChanged = true;
            	btn_Save.setEnabled(true);
			}
		});

		// Listener for Source Select button - get a new Source
		btn_sorcSelect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				HG0565ManageSource sorcScreen = new HG0565ManageSource(pointOpenProject, pointEditCitation);
				sorcScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xySorc = btn_sorcSelect.getLocationOnScreen();
				sorcScreen.setLocation(xySorc.x - 100, xySorc.y - 100);
				sorcScreen.setVisible(true);
				btn_Preview.setEnabled(true);
				setCursor(Cursor.getDefaultCursor());
				btn_Save.setEnabled(true);
			}
		});

		// Listener to detect srcNum entry has been exited without pressing enter
		srcNum.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
            	// Get whatever has been entered so far and process it
            	if (StringUtils.isNumeric(srcNum.getText()))
            					sourceNumberEntry(Integer.valueOf(srcNum.getText()));
            	else sourceNumberEntry(0);		// else pass across 0 to force error msg
            }
        });

		// Listener for pressing Enter after Selecting a Source number
		srcNum.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// Get the Src number and process it
				int num = ((Integer) srcNum.getValue()).intValue();
				sourceNumberEntry(num);
	        }
		});

		// Listener for Preview button
		btn_Preview.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				if (!outputVisible) {
				// Unhide the Output panel
					outputPanel.setVisible(true);
					outputVisible = true;
					pack();
				// Parse the full Footnote text into its output area
					fullFootText.setText(parseNoteBiblio(templateFullFootnote));
				// Parse the short Footnote text into its output area
					shortFootText.setText(parseNoteBiblio(templateShortFootnote));
				// Parse the Bibliography text into its output area
					biblioText.setText(parseNoteBiblio(templateBibliography));
				}
				else {
				// Hide the output panel if Preview button clicked again
					outputPanel.setVisible(false);
					outputVisible = false;
					pack();
				}
			}
		});

		// Listener for Save button
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				// Close reminder display, if open
				if (reminderDisplay != null) reminderDisplay.dispose();
				long newCitationTablePID = null_RPID;
				try {
					if (addCitationRecord) {
						newCitationTablePID = pointCitationSourceHandler.createCitationRecord(sourceTablePID);
						pointCitationSourceHandler.saveCitedData(newCitationTablePID, citationSaveData(), accuracyEdited);
					} else pointCitationSourceHandler.saveCitedData(citationTablePID, citationSaveData(), accuracyEdited);
					if (pointEditEvent != null) pointEditEvent.resetCitationTable(newCitationTablePID);
					if (pointManagePersonName != null) pointManagePersonName.resetCitationTable();
					if (pointSelectPerson != null) pointSelectPerson.resetCitationTable(citeOwnerType);

				} catch (HBException hbe) {
					System.out.println(" HG0555EditCitation save error: " + hbe.getMessage()); //$NON-NLS-1$
					hbe.printStackTrace();
				}
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: save and exit HG0555EditCitation");	//$NON-NLS-1$
				dispose();
			}
		});

		// Listener for Cancel button
		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				// Close reminder display, if open
				if (reminderDisplay != null) reminderDisplay.dispose();
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0555EditCitation");	//$NON-NLS-1$
				dispose();
			}
		});

		// Add Focus listener to all Accuracy fields so field is shown as selected
		accFocus = new FocusListener() {
			  @Override
			  public void focusGained(FocusEvent fe) {
			    JTextField txt = (JTextField)fe.getComponent();
			    txt.selectAll();
			  }
			  @Override
			  public void focusLost(FocusEvent e) {
			  }
			};
		acc1.addFocusListener(accFocus);
		acc2.addFocusListener(accFocus);
		accD.addFocusListener(accFocus);
		accP.addFocusListener(accFocus);
		accM.addFocusListener(accFocus);

		// Add a DocumentFilter to all Accuracy fields to validate input
        DocumentFilter accDocFilter = new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                // Allow only valid characters and ensure the field has max 1 character
                if (text != null && text.length() <= 1 && ("3210-".contains(text) || text.isBlank())) { //$NON-NLS-1$
                    fb.replace(0, fb.getDocument().getLength(), text, attrs); // Replace everything with the new character
                }
              // If this is NOT an ADD citation, recognise a Surety change by resetting the Assessor Name/PID to be saved
    			if (!addCitationRecord) {
	  				assessorName = (String) ownerAssessorData[0];
					assessorPID = (long) ownerAssessorData[1];
					assessText.setText(assessorName);
    			}
            }
            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attrs) throws BadLocationException {
            	replace(fb, offset, 0, text, attrs); // Redirect to replace to ensure only 1 char ever acceptedc
            }
        };
        ((AbstractDocument) acc1.getDocument()).setDocumentFilter(accDocFilter);
        ((AbstractDocument) acc2.getDocument()).setDocumentFilter(accDocFilter);
        ((AbstractDocument) accD.getDocument()).setDocumentFilter(accDocFilter);
        ((AbstractDocument) accP.getDocument()).setDocumentFilter(accDocFilter);
        ((AbstractDocument) accM.getDocument()).setDocumentFilter(accDocFilter);

        // Add a Document listener to all Accuracy fields to set Save button on
        DocumentListener accDocListen = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
            	accuracyChanged = true;
            	btn_Save.setEnabled(true);
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
            	accuracyChanged = true;
            	btn_Save.setEnabled(true);
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
            	accuracyChanged = true;
            	btn_Save.setEnabled(true);
            }
        };
        acc1.getDocument().addDocumentListener(accDocListen);
        acc2.getDocument().addDocumentListener(accDocListen);
        accD.getDocument().addDocumentListener(accDocListen);
        accP.getDocument().addDocumentListener(accDocListen);
        accM.getDocument().addDocumentListener(accDocListen);

	}	// End HG0555EditCitation constructor

/**
 * citationSaveData()
 * @return
 */
	private Object[] citationSaveData() {
		accuracyEdited = new int[5];
		citationSaveData = new Object[6];
		citationSaveData[0] = refText.getText();
		citationSaveData[1] = Integer.parseInt(srcNum.getText().trim());
		citationSaveData[2] = sourceTitleText.getText();
		citationSaveData[3] = citeDetailText.getText();
		citationSaveData[4] = citeMemoText.getText();
		citationSaveData[5] = assessorPID;
		accuracyEdited[0] = inputAccConvert(acc1.getText());
		accuracyEdited[1] = inputAccConvert(acc2.getText());
		accuracyEdited[2] = inputAccConvert(accD.getText());
		accuracyEdited[3] = inputAccConvert(accP.getText());
		accuracyEdited[4] = inputAccConvert(accM.getText());
		return citationSaveData;
	}

/**
 * fidAnalyse()
 * @return fidelity text
 */
	private String fidAnalyse() {
		String fidText = "-"; //$NON-NLS-1$
		if (fidelityData.equals("A")) fidText = HG0555Msgs.Text_37; 		//$NON-NLS-1$		// Original
		else if (fidelityData.equals("B")) fidText = HG0555Msgs.Text_39; 	//$NON-NLS-1$		// Copy
		else if (fidelityData.equals("C")) fidText = HG0555Msgs.Text_41; 	//$NON-NLS-1$		// Transcript
		else if (fidelityData.equals("D")) fidText = HG0555Msgs.Text_43; 	//$NON-NLS-1$		// Extract
		else if (fidelityData.equals("E")) fidText = HG0555Msgs.Text_45; 	//$NON-NLS-1$		// Other
		else
			if (HGlobal.DEBUG) System.out.println(" Soruce fidelity not defined for code: " + fidelityData); //$NON-NLS-1$
		return fidText;
	}

/**
 * private int inputAccConvert(String acc)
 * @param acc
 * @return
 */
	private int inputAccConvert(String acc) {
		if (acc.equals("3")) return 3; 		//$NON-NLS-1$
		if (acc.equals("2")) return 2; 		//$NON-NLS-1$
		if (acc.equals("1")) return 1; 		//$NON-NLS-1$
		if (acc.equals("0")) return 0; 		//$NON-NLS-1$
		if (acc.equals("-")) return -1; 	//$NON-NLS-1$
		if (acc.equals(" ")) return -2; 	//$NON-NLS-1$
		if (acc.isEmpty()) return -3;
		return -4;
	}

/**
 * private sourceNumberEntry(int enteredNum)
 * @param entered source number
 */
	private void sourceNumberEntry(int enteredNum) {
    	if (enteredNum > 0) {
    		try {
    			// Get source number, title, cited, PID, fidelity
				objSourceData = pointCitationSourceHandler.getSourceList();
			} catch (HBException hse) {
				System.out.println(" HG0555EditCitation get source data error: " + hse.getMessage()); //$NON-NLS-1$
				hse.printStackTrace();
			}
    		// Get the PID and title of the entered source number
    		for (int i = 0; i < objSourceData.length; i++) {
    			if ((int)objSourceData[i][0] == enteredNum) {
    				sourceTablePID = (long) objSourceData[i][3];
    				sourceTitleText.setText((String) objSourceData[i][1]);
    				fidelityData = (String) objSourceData[i][4];
    				fidelityText.setText(" " + fidAnalyse()); 		//$NON-NLS-1$
    				btn_Save.setEnabled(true);
    				sourceFound = true;
    				break;
    			}
    		}
    	}
		// No source number matched
		if (!sourceFound) JOptionPane.showMessageDialog(srcNum,
														HG0555Msgs.Text_54 + enteredNum,	// No Source exists with number:
														HG0555Msgs.Text_55, JOptionPane.ERROR_MESSAGE); // Select Source
		// reset for next time
		sourceFound = false;
	}

/**
 * parseNoteBiblio - converts footnote/bibliography template to HTML display form
 * inputText	template text to be converted
 * returns output - text in HTML format with formatting applied
 */
	private String parseNoteBiblio(String inputText) {
		// Define token workareas (150 entries should be enough)
		String[] tokensPh2 = new String[150];
		String[] tokensPh3 = new String[150];
		// and return string
		String output = "";		//$NON-NLS-1$
		// and token counters
		int tokenNumPh2 = 0;
		int tokenNumPh3 = 0;
		// and work string
		String workText = ""; //$NON-NLS-1$

		// If no input, return nothing
		if (inputText == null || inputText.isEmpty()) {
			output = "";	//$NON-NLS-1$
			return output;
		}

	/****************************
	 * PHASE 1 - clean the input
	 ***************************/
		// Both TMG/HRE templates and HTML use < > markers for different purposes, so we need to replace all
		// such markers in the input string to enable use of HTML formatting codes.
		// We have chosen to replace the template < and > markers with ≤ and ≥ - we do this now.
		String markers = inputText.replace("<", "≤").replace(">", "≥"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		// The format of input string can include TMG formatting values which should never be
		// part of citations. These are [SCAP:] [INDEX:] [SIZE:] and need to be removed.
		// Next, all the other [xxx:] and [:xxx} codes need to be converted to their
		// HTML equivalents.
		// To do this we use an iterative regex.
		Map<String, String> repl = new HashMap<>();
		repl.put("[BOLD:]", "<b>"); 		//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[:BOLD]", "</b>"); 		//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[ITAL:]", "<i>"); 		//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[:ITAL]", "</i>"); 		//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[UND:]", "<u>"); 			//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[:UND]", "</u>"); 		//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[SUP:]", "<sup>"); 		//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[:SUP]", "</sup>"); 		//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[SUB:]", "<sub>"); 		//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[:SUB]", "</sub>"); 		//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[HID:]", "<!"); 			//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[:HID]", "->"); 			//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[CAP:]", "<p style=\"text-transform: uppercase;\">"); //$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[:CAP]", "</p>"); 		//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[HTML:]", ""); 			//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[:HTML]", ""); 			//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[WEB:]", ""); 			//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[:WEB]", ""); 			//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[EMAIL:]", "<a href=mailto:"); //$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[:EMAIL]", "></a>"); 		//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[INDEX:]", ""); 			//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[:INDEX]", ""); 			//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[SIZE:]", ""); 			//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[:SIZE]", ""); 			//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[SCAP:]", ""); 			//$NON-NLS-1$ //$NON-NLS-2$
		repl.put("[:SCAP]", ""); 			//$NON-NLS-1$ //$NON-NLS-2$
		Pattern pattern1 = Pattern.compile
			("\\[BOLD:\\]|\\[:BOLD\\]|\\[ITAL:\\]|\\[:ITAL\\]|\\[UND:\\]|\\[:UND\\]|\\[SUP:\\]|\\[:SUP\\]|\\[SUB:\\]|\\[:SUB\\]|\\[HID:\\]|\\[:HID\\]|\\[CAP:\\]|\\[:CAP\\]|\\[HTML:\\]|\\[:HTML\\]|\\[WEB:\\]|\\[:WEB\\]|\\[EMAIL:\\]|\\[:EMAIL\\]|\\[INDEX:\\]|\\[:INDEX\\]|\\[SIZE:\\]|\\[:SIZE\\]|\\[SCAP:\\]|\\[:SCAP\\]"); //$NON-NLS-1$
		Matcher matcher1 = pattern1.matcher(markers);
		StringBuffer buffer1 = new StringBuffer();
		while(matcher1.find())
		    matcher1.appendReplacement(buffer1, repl.get(matcher1.group()));
		matcher1.appendTail(buffer1);
		workText = buffer1.toString();

		//System.out.println("End Phase 1: " + workText);

	/******************************
	 * PHASE 2 - tokenize the input
	 ******************************/
		// In Phase 2 we want to break the revised input string into an array of 'tokens',
		// each containing just 1 component of the input string.
		// So we setup a regex to break the text string into 'tokens' in tokensPh2[]
		String regex2 = "\\[[^\\]]*\\]|\\≤[^\\≥]*\\≥|[^≤≥\\[\\]]+"; 	//$NON-NLS-1$
		Pattern pattern2 = Pattern.compile(regex2);
        Matcher matcher2 = pattern2.matcher(workText);
        // This regex will extract each string enclosed in [ ] or ≤ ≥ or plain text
        // note that ≤ ≥ strings will enclose more [ ] strings - process them in Phase 3.
        while (matcher2.find()) {
            tokensPh2[tokenNumPh2] = matcher2.group();
            tokenNumPh2++ ;
        }

        // Display tokensPh2 for checking
  		//System.out.println("End Phase 2:");
  		//for (int i = 0; i < tokenNumPh2; i++) {
  		//	System.out.println("tokensPh2[" + i + "] = " + tokensPh2[i]);
  		//}

	/**********************************************************
	 * PHASE 3 - remove empty tokens and split apart < > tokens
	 **********************************************************/
        // This phase transfers all tokens to tokensPh3, ignoring null/empty tokens and tokens wrapped in ≤ ≥ (for now).
        for (int i=0; i < tokenNumPh2; i++) {
        	if (tokensPh2[i].trim().length() > 0 && tokensPh2[i] != null && !tokensPh2[i].substring(0, 1).equals("≤")) { //$NON-NLS-1$
        		tokensPh3[tokenNumPh3] = tokensPh2[i];
        		tokenNumPh3++;
        	}
            // Now break apart all ≤  ≥ entries in tokensPh2 to expose [  ] entries they
        	// will contain and load them into tokensPh3 as separate tokens
        	if (tokensPh2[i].startsWith("≤")) { //$NON-NLS-1$
        		// Remove the ≤, ≥ characters from beginning/end of this token
    			workText = tokensPh2[i].substring(1, tokensPh2[i].length() - 1);
    			// add the ≤ into tokensPh3
    			tokensPh3[tokenNumPh3] = "≤"; //$NON-NLS-1$
    			tokenNumPh3++;
    			// Break apart the ≤ ≥ contents using the previous regex pattern (now
    			// applied to workText) and add these into tokensPh3.
    			Matcher matcher3 = pattern2.matcher(workText);
                while (matcher3.find()) {
                	tokensPh3[tokenNumPh3] = matcher3.group();
                    tokenNumPh3++ ;
                }
    			// add the ending ≥
    			tokensPh3[tokenNumPh3] ="≥"; //$NON-NLS-1$
    			tokenNumPh3++;
        	}
        }

        // Display tokensPh3 for checking
  		//System.out.println("End Phase 3:");
  		//for (int i = 0; i < tokenNumPh3; i++) {
  		//	System.out.println("tokensPh3[" + i + "] = " + tokensPh3[i]);
  		//}

	/***************************************************
	 * PHASE 4 - substitute [  ] fields with real values
	 ***************************************************/
  		// At this stage we should be substituting the [xxx] tokens with their real values
  		// but at this facility isn't available yet, we'll carry on.


        // Display tokensPh3 for checking
  		//System.out.println("End Phase 4:");
  		//for (int i = 0; i < tokenNumPh3; i++) {
  		//	System.out.println("tokensPh3[" + i + "] = " + tokensPh3[i]);
  		//}

	/***************************************************
	 * PHASE 5 - process within the ≤ ≥ markers
	 ***************************************************/
   		// Now check the substitutions of tokens enclosed in the ≤ ≥ markers.
		// If there are unsubstituted tokens then the complete content
		// of these markers needs to be removed from the output.
    	// First, look for a start marker
    	int startMarker = 0;
    	int stopMarker = 0;
		for (int i = 0; i < tokenNumPh3; i++) {
			if (tokensPh3[i].startsWith("≤")) { //$NON-NLS-1$
				startMarker = i;
				// Once "≤" is found, look for a "≥"
				for (int j = i; j < tokenNumPh3; j++) {
					if (tokensPh3[j].startsWith("≥")) { //$NON-NLS-1$
						stopMarker = j;
						break;
					}
				}
				// Now check within the range of the start/stopMarkers for a  [  ] token.
				// If one exists, it hasn't been substituted, so delete the whole ≤ ≥ range.
				for (int k = startMarker+1; k < stopMarker; k++) {
					if (tokensPh3[k].startsWith("[")) { //$NON-NLS-1$
						for (int x = startMarker; x < stopMarker+1; x++) {
							tokensPh3[x] = ""; //$NON-NLS-1$
						}
					}
				}
			}
		}

        // Display tokensPh3 for checking
  		//System.out.println("End Phase 5:");
  		//for (int i = 0; i < tokenNumPh3; i++) {
  		//	System.out.println("tokensPh3[" + i + "] = " + tokensPh3[i]);
  		//}

	/****************************************************
	 * PHASE 6 - build final output string
	 ****************************************************/
        // Build the tokens into our workText string, ignoring blank tokens
		workText = ""; //$NON-NLS-1$
        for (int i=0; i < tokenNumPh3; i++) {
        	if (!tokensPh3[i].equals("")) workText = workText + tokensPh3[i] + " "; //$NON-NLS-1$ //$NON-NLS-2$
        }
        // Now clean the output of double blanks, blanks before/after punctuation and
        // the ≤ ≥ markers remaining after valid substitutions were made.
        // Use another iterative regex to do this.
		Map<String, String> clean = new HashMap<>();
		clean.put("  ", " ");		// change double blank to blank //$NON-NLS-1$ //$NON-NLS-2$
		clean.put("( ", "(");		// remove blank after ( 		//$NON-NLS-1$ //$NON-NLS-2$
		clean.put(" )", ")");		// remove blank before ) 		//$NON-NLS-1$ //$NON-NLS-2$
		clean.put(" :", ":");		// remove blank before : 		//$NON-NLS-1$ //$NON-NLS-2$
		clean.put(" ;", ";");		// remove blank before ; 		//$NON-NLS-1$ //$NON-NLS-2$
		clean.put(" ,", ",");		// remove blank before , 		//$NON-NLS-1$ //$NON-NLS-2$
		clean.put(" .", ".");		// remove blank before . 		//$NON-NLS-1$ //$NON-NLS-2$
		clean.put("≤", "");			// remove leftover ≤ marker 	//$NON-NLS-1$ //$NON-NLS-2$
		clean.put("≥", "");			// remove leftover ≥ marker 	//$NON-NLS-1$ //$NON-NLS-2$
		StringBuffer buffer3 = new StringBuffer(workText);
		for (Map.Entry<String, String> entry : clean.entrySet()) {
	        // Create the regex pattern from the map key
	        Pattern pattern3 = Pattern.compile(Pattern.quote(entry.getKey()));
	        Matcher matcher3 = pattern3.matcher(buffer3.toString());
	        // Perform replacement and update the StringBuffer
	        buffer3.setLength(0); 	// Clear the buffer
            while (matcher3.find()) {
                matcher3.appendReplacement(buffer3, entry.getValue());
            }
            matcher3.appendTail(buffer3);
	    }
		output = buffer3.toString();

        // and return it
		return output;

	}		// end of parseFootBiblio

}  // End of HG0555EditCitation
