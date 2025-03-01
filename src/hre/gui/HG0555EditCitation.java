package hre.gui;
/**************************************************************************************
 * Edit Citation
 * ***********************************************************************************
 * v0.04.0032 2025-01-17 Original draft (D Ferguson)
 *			  2025-01-19 Add Visualize button; add Listeners for text fields (D Ferguson)
 *			  2025-01-31 Add ability to select Source by ### (D Ferguson)
 *			  2025-02-10 Change textAreas to textPanes (D Ferguson)
 *			  2025-02-20 Add Assessor field & ActionListeners (D Ferguson)
 *        	  2025-02-21 Populate citation data and save updates (N. Tolleshaug)
 *        	  2025-02-25 Added add new citation (N. Tolleshaug)
 *        	  2025-02-25 Add/Update and delete event citation (N. Tolleshaug)
 *        	  2025-02-26 Add/Update and delete name citation (N. Tolleshaug)
 *            2025-02-27 Fix InputVerifier, add Source Fidelity, close Reminder on exit (D Ferguson)
 *************************************************************************************
 * Notes for incomplete code still requiring attention
 * NOTE03 Visualise button needs action written
 * NOTE04 Source selection by ## action incomplete
 *
 ************************************************************************************/

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;

import javax.swing.Box;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
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
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import javax.swing.text.NumberFormatter;
import javax.swing.text.PlainDocument;

import hre.bila.HB0711Logging;
import hre.bila.HBCitationSourceHandler;
import hre.bila.HBException;
import hre.bila.HBProjectOpenData;
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

	public HBCitationSourceHandler pointCitationSourceHandler;

	private JPanel contents;
	boolean outputVisible = false;

	String citationRefer = "", sourceTitle = "", citationDetail = "", citationMemo = "";
	int sourceREF = 0;
	String[] accuracyData = {"","","",""};
	String fidData = "";
	String[] accuracyEdited = null;
	Object[] citationEditData;
	Object[] citationSaveData;
	long citationTablePID = null_RPID;
	long sourceTablePID = null_RPID;

	DocumentListener citeDetailListen, citeMemoListen;
	FocusListener accFocus;
	boolean citeDetailChanged, citeMemoChanged, refTextChanged, assessChanged = false;
	boolean accuracyChanged, fidelityChanged;

	JTextArea citeMemoText, citeDetailText;
	JTextField sourceTitleText, refText, acc1, acc2, accD, accP, accM;
	JFormattedTextField srcNum;

	JButton btn_Save;

	public void setSourceSelectedData(Object[] objSourceDataToEdit) {
		sourceTablePID = (long) objSourceDataToEdit[3];
		sourceTitleText.setText((String) objSourceDataToEdit[1]);
		srcNum.setText("" + objSourceDataToEdit[0]);
	}

/**
 * Create the dialog
 */
	public HG0555EditCitation(boolean addCitationRecord, HBProjectOpenData pointOpenProject)  {
		this.pointOpenProject = pointOpenProject;
		createGUI(addCitationRecord);
	}

	public HG0555EditCitation(boolean addCitationRecord, HBProjectOpenData pointOpenProject, long citationTablePID)  {
		this.pointOpenProject = pointOpenProject;
		this.citationTablePID = citationTablePID;
		createGUI(addCitationRecord);
	}

	private void createGUI(boolean addCitationRecord) {

		if (addCitationRecord) setTitle("Add Citation");
		else setTitle("Edit Citation");

	// Setup references for HG0450
		windowID = screenID;
		helpName = "editcitation";		 //$NON-NLS-1$
		pointCitationSourceHandler = pointOpenProject.getCitationSourceHandler();

	// Collect citationData
		if (!addCitationRecord)
		try {
			citationEditData = pointCitationSourceHandler.getCitationEditData(citationTablePID);
			citationRefer = (String) citationEditData[0];
			sourceREF = (int) citationEditData[1];
			sourceTitle = (String) citationEditData[2];
			citationDetail = (String) citationEditData[3];
			citationMemo = (String) citationEditData[4];
			accuracyData = pointCitationSourceHandler.getAccoracyData();
		} catch (HBException hbe) {
			System.out.println(" HG0555EditCitation error: " + hbe.getMessage());
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

		JButton btn_sorcSelect = new JButton("Select Source");	// Select Source
		citePanel.add(btn_sorcSelect, "cell 0 0, alignx right");	//$NON-NLS-1$

	    NumberFormat format = NumberFormat.getInstance();
	    NumberFormatter formatter = new NumberFormatter(format);
	    formatter.setValueClass(Integer.class);
	    formatter.setMinimum(1);
	    formatter.setMaximum(99999);
	    srcNum = new JFormattedTextField(formatter);
	    srcNum.setText("" + sourceREF);
	    srcNum.setColumns(5);
	    srcNum.setHorizontalAlignment(SwingConstants.CENTER);
	    citePanel.add(srcNum, "cell 1 0, alignx left");		//$NON-NLS-1$

		sourceTitleText = new JTextField(sourceTitle);
		sourceTitleText.setColumns(30);
		sourceTitleText.setEditable(false);
		citePanel.add(sourceTitleText, "cell 1 0, gapx 10, alignx left");	//$NON-NLS-1$

	// Citation text fields
		JLabel citeDetail = new JLabel("Citation Detail:");
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

		JLabel citeMemo = new JLabel("Citation Memo:");
		citePanel.add(citeMemo, "cell 0 2, alignx right");	//$NON-NLS-1$
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

		JLabel ref = new JLabel("Reference:");
		citePanel.add(ref, "cell 0 3, alignx right");	//$NON-NLS-1$
		refText = new JTextField(citationRefer);
		refText.setColumns(30);
		citePanel.add(refText, "cell 1 3, alignx left");	//$NON-NLS-1$

		JLabel assess = new JLabel("Assessor:");
		citePanel.add(assess, "cell 0 4, alignx right");	//$NON-NLS-1$
		JTextField assessText = new JTextField();
		assessText.setColumns(30);
		citePanel.add(assessText, "cell 1 4, alignx left");	//$NON-NLS-1$

		JLabel sure = new JLabel("Surety:");
		citePanel.add(sure, "cell 0 5, alignx right");	//$NON-NLS-1$

		JLabel srcFidLbl = new JLabel("Source Fidelity: ");
		citePanel.add(srcFidLbl, "cell 0 6, alignx right");	//$NON-NLS-1$
		JTextField fidText = new JTextField();
		fidText.setColumns(15);
		fidText.setEditable(false);
		fidText.setFocusable(false);
		fidText.setText(fidAnalyse());
		citePanel.add(fidText, "cell 1 6, alignx left");	//$NON-NLS-1$

	// Define Surety panel
		JPanel surePanel = new JPanel();
		surePanel.setLayout(new MigLayout("", "[]10[]", "[]0[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel sure12PDM = new JLabel("  1    2    D    P   M");
		surePanel.add(sure12PDM, "cell 0 0, alignx left");	//$NON-NLS-1$

		acc1 = new JTextField();
		acc1.setColumns(1);
		acc1.setInputVerifier(new accVerifier());
		acc1.setDocument(new JTextFieldLimit(1));
		acc1.setHorizontalAlignment(SwingConstants.CENTER);
		acc1.setText(accuracyData[0]);
		surePanel.add(acc1, "cell 0 1");	//$NON-NLS-1$

		acc2 = new JTextField();		// <<<<<< not being set yet!!
		acc2.setColumns(1);
		acc2.setInputVerifier(new accVerifier());
		acc2.setDocument(new JTextFieldLimit(1));
		acc2.setHorizontalAlignment(SwingConstants.CENTER);
		surePanel.add(acc2, "cell 0 1");	//$NON-NLS-1$

		accD = new JTextField();
		accD.setColumns(1);
		accD.setInputVerifier(new accVerifier());
		accD.setDocument(new JTextFieldLimit(1));
		accD.setHorizontalAlignment(SwingConstants.CENTER);
		accD.setText(accuracyData[1]);
		surePanel.add(accD, "cell 0 1");	//$NON-NLS-1$

		accP = new JTextField();
		accP.setColumns(1);
		accP.setInputVerifier(new accVerifier());
		accP.setDocument(new JTextFieldLimit(1));
		accP.setHorizontalAlignment(SwingConstants.CENTER);
		accP.setText(accuracyData[2]);
		surePanel.add(accP, "cell 0 1");	//$NON-NLS-1$

		accM = new JTextField();
		accM.setColumns(1);
		accM.setInputVerifier(new accVerifier());
		accM.setDocument(new JTextFieldLimit(1));
		accM.setHorizontalAlignment(SwingConstants.CENTER);
		accM.setText(accuracyData[3]);
		surePanel.add(accM, "cell 0 1");	//$NON-NLS-1$

		JLabel accHint = new JLabel("(Valid values 3, 2, 1, 0, blank, minus)");
		surePanel.add(accHint, "cell 1 1");	//$NON-NLS-1$

		citePanel.add(surePanel, "cell 1 5, alignx left");	//$NON-NLS-1$
		contents.add(citePanel, "cell 0 0, aligny top"); //$NON-NLS-1$

	// Setup Output panel for display of how citation looks
		JPanel outputPanel = new JPanel();
		outputPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED, null, null));
		outputPanel.setLayout(new MigLayout("", "[]", "[]5[]10[]5[]10[]5[]")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		JLabel fullFoot = new JLabel("Full footnote");
		outputPanel.add(fullFoot, "cell 0 0, alignx left");	//$NON-NLS-1$
		JTextPane fullFootText = new JTextPane();
		fullFootText.setEditable(false);
		fullFootText.setFocusable(false);
		fullFootText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		fullFootText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		fullFootText.setBorder(new JTable().getBorder());		// match Table border
		fullFootText.setPreferredSize(new Dimension(200, 90));
		JScrollPane fullFootTextScroll = new JScrollPane(fullFootText);
		fullFootTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		outputPanel.add(fullFootTextScroll, "cell 0 1, alignx left");	//$NON-NLS-1$

		JLabel shortFoot = new JLabel("Short footnote");
		outputPanel.add(shortFoot, "cell 0 2, alignx left");	//$NON-NLS-1$
		JTextPane shortFootText = new JTextPane();
		shortFootText.setEditable(false);
		shortFootText.setFocusable(false);
		shortFootText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		shortFootText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		shortFootText.setBorder(new JTable().getBorder());		// match Table border
		shortFootText.setPreferredSize(new Dimension(200, 75));
		JScrollPane shortFootTextScroll = new JScrollPane(shortFootText);
		shortFootTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		outputPanel.add(shortFootTextScroll, "cell 0 3, alignx left");	//$NON-NLS-1$

		JLabel biblio = new JLabel("Bibliography");
		outputPanel.add(biblio, "cell 0 4, alignx left");	//$NON-NLS-1$
		JTextPane biblioText = new JTextPane();
		biblioText.setEditable(false);
		biblioText.setFocusable(false);
		biblioText.setFont(new Font(font.getName(), font.getStyle(), font.getSize()));  // Set text size/font to current JTattoo setting
		biblioText.setBackground(UIManager.getColor("Table.background"));	//$NON-NLS-1$	// match table background
		biblioText.setBorder(new JTable().getBorder());		// match Table border
		biblioText.setPreferredSize(new Dimension(200, 75));
		JScrollPane biblioTextScroll = new JScrollPane(biblioText);
		biblioTextScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);  // Vert scroll if needed
		outputPanel.add(biblioTextScroll, "cell 0 5, alignx left");	//$NON-NLS-1$

		contents.add(outputPanel, "cell 1 0,aligny top, hidemode 3"); //$NON-NLS-1$
		outputPanel.setVisible(false);

	// Define control buttons
		JButton btn_Cancel = new JButton("Cancel");		// Cancel
		btn_Cancel.setEnabled(true);
		contents.add(btn_Cancel, "cell 0 1, alignx right, gapx 10, tag cancel"); //$NON-NLS-1$
		JButton btn_Display = new JButton("Visualise");		// Visualise
		btn_Display.setEnabled(true);
		contents.add(btn_Display, "tag left,cell 0 1,alignx right,gapx 10"); //$NON-NLS-1$
		btn_Save = new JButton("Save");		// Save
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
		refText.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
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

		// Listener for Source Select button
		btn_sorcSelect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				HG0565ManageSource sorcScreen = new HG0565ManageSource(pointOpenProject, pointEditCitation);
				sorcScreen.setModalityType(ModalityType.APPLICATION_MODAL);
				Point xySorc = btn_sorcSelect.getLocationOnScreen();
				sorcScreen.setLocation(xySorc.x - 100, xySorc.y - 100);
				sorcScreen.setVisible(true);
				btn_Display.setEnabled(true);
				setCursor(Cursor.getDefaultCursor());
			}
		});

		// Listener for pressing Enter after entering a Source number
		srcNum.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int enteredNum = ((Integer) srcNum.getValue()).intValue();
	        	if (enteredNum > 0) {
	        		// NOTE04 Get the Source with the entered number and populate screen

	        		} else {
						JOptionPane.showMessageDialog(srcNum, "No Source with number: " + enteredNum,
													"Select Source", JOptionPane.ERROR_MESSAGE);
					}
	        	}
		});

		// Listener for Display (Visualise) button
		btn_Display.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
			// Unhide the Output panel
				outputPanel.setVisible(true);
				outputVisible = true;
				pack();
			// NOTE03 needs action code here to parse the Citation text
			// into the Source templates and display it in the new output panel
			}
		});

		// Listener for Save button
		btn_Save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				// Close reminder display, if open
				if (reminderDisplay != null) reminderDisplay.dispose();
				long newCitationTablePID;
				try {
					if (addCitationRecord) {
						newCitationTablePID = pointCitationSourceHandler.createCitationRecord(sourceTablePID);
						pointCitationSourceHandler.saveCitedData(newCitationTablePID, citationSaveData(), accuracyEdited);
					} else pointCitationSourceHandler.saveCitedData(citationTablePID, citationSaveData(), accuracyEdited);
					if (pointEditEvent != null) pointEditEvent.resetCitationTable();
					if (pointManagePersonName != null) pointManagePersonName.resetCitationTable();
				} catch (HBException hbe) {
					System.out.println(" HG0555EditCitation save error: " + hbe.getMessage());
					hbe.printStackTrace();
				}
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: save and exit HG0565ManageSource");	//$NON-NLS-1$
				dispose();
			}
		});

		// Listener for Cancel button
		btn_Cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actEvent) {
				// Close reminder display, if open
				if (reminderDisplay != null) reminderDisplay.dispose();
				if (HGlobal.writeLogs) HB0711Logging.logWrite("Action: exiting HG0565ManageSource");	//$NON-NLS-1$
				dispose();
			}
		});

		// Focus listener for all Accuracy fields
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

	}	// End HG0555EditCitation constructor

/**
 * citationSaveData()
 * @return
 */
	private Object[] citationSaveData() {
		accuracyEdited = new String[4];
		citationSaveData = new Object[5];
		citationSaveData[0] = refText.getText();
		citationSaveData[1] = Integer.parseInt(srcNum.getText().trim());
		citationSaveData[2] = sourceTitleText.getText();
		citationSaveData[3] = citeDetailText.getText();
		citationSaveData[4] = citeMemoText.getText();
		accuracyEdited[0] = acc1.getText();
		accuracyEdited[1] = accD.getText();
		accuracyEdited[2] = accP.getText();
		accuracyEdited[3] = accM.getText();
		return citationSaveData;
	}

/**
 * fidAnalyse()
 * @return fidelity text
 */
	private String fidAnalyse() {
		String fidText;
		if (fidData.equals("A")) fidText = "Original";
		if (fidData.equals("B")) fidText = "Copy";
		if (fidData.equals("C")) fidText = "Transcript";
		if (fidData.equals("D")) fidText = "Extract";
		if (fidData.equals("E")) fidText = "Other";
		else fidText = "";
		return fidText;
	}

/**
 * class accVerifier - Verify Accuracy textfield inputs (3,2,1,0," ", "-" or null)
 */
	public class accVerifier extends InputVerifier {
		@Override
		public boolean verify(JComponent input) {
		    String text = ((JTextField) input).getText();
			input.requestFocusInWindow();
	        ((JTextComponent) input).selectAll();
			if (text.equals("3") || text.equals("2") || text.equals("1") || text.equals("0")
								 || text.equals(" ") || text.equals("-") || text.isEmpty()) {
					accuracyChanged = true;
					btn_Save.setEnabled(true);
					return true;
				}
			return false;
		}
	}

/**
 * class JTextFieldLimit - limits data length of a JTextField
 */
	class JTextFieldLimit extends PlainDocument {
		private static final long serialVersionUID = 1L;
		private int limit ;
		   JTextFieldLimit(int limit) {
		      super();
		      this.limit = limit;
		   }
		   JTextFieldLimit(int limit, boolean upper) {
		      super();
		      this.limit = limit;
		   }
		   public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
		      if (str == null)
		         return;
		      if ((getLength() + str.length()) <= limit) {
		         super.insertString(offset, str, attr);
		      }
		   }
		}

}  // End of HG0555EditCitation
